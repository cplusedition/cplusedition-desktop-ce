/*!            
    C+edition for Desktop, Community Edition.
    Copyright (C) 2021 Cplusedition Limited.  All rights reserved.
    
    The author licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package sf.andrians.cplusedition.war

import com.cplusedition.bot.core.BotResult
import com.cplusedition.bot.core.Fun10
import com.cplusedition.bot.core.Fun11
import com.cplusedition.bot.core.Fun30
import com.cplusedition.bot.core.IBotResult
import com.cplusedition.bot.core.ProcessUtBuilder
import com.cplusedition.bot.core.ProcessUtil
import com.cplusedition.bot.core.Without
import com.cplusedition.bot.core.mkparentOrFail
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.R.string
import sf.andrians.cplusedition.support.BackupKeyPair
import sf.andrians.cplusedition.support.Best
import sf.andrians.cplusedition.support.EncryptedRootInfo
import sf.andrians.cplusedition.support.IFileInfo
import sf.andrians.cplusedition.support.ISecUtil
import sf.andrians.cplusedition.support.ISecUtil.Companion.dateOf
import sf.andrians.cplusedition.support.ISecUtil.Companion.sha256Of
import sf.andrians.cplusedition.support.ISecUtil.EtcPaths
import sf.andrians.cplusedition.support.ISecUtilAccessor
import sf.andrians.cplusedition.support.SecureException
import sf.andrians.cplusedition.support.StringId
import sf.andrians.cplusedition.support.media.ImageUtil
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.Key
import java.security.KeyFactory
import java.security.KeyStore
import java.security.KeyStore.Entry
import java.security.KeyStore.PasswordProtection
import java.security.KeyStore.PrivateKeyEntry
import java.security.KeyStore.ProtectionParameter
import java.security.KeyStore.SecretKeyEntry
import java.security.KeyStore.TrustedCertificateEntry
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Signature
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.locks.ReentrantLock
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.imageio.ImageIO

class SecUtil(
    datadir: File,
    pass: CharArray,
) : ISecUtil {
    private val lock = ReentrantLock()
    private val impl = SecUtilImpl(
        datadir, pass,
    )

    private object K {
        val KEY_STORE = "PKCS12"
        val RSA_SIGN = "SHA512withRSA"
        val RSA_CIPHER = "RSA/ECB/PKCS1Padding"
        val TIMEOUT = 30L
        val CN = "self"
        val PREFIX = "#"
    }

    override fun <R> invoke(code: Fun11<ISecUtilAccessor, R>): R {
        lock.lock()
        try {
            return code(impl)
        } finally {
            lock.unlock()
        }
    }

    override fun rsaEncryptionCipher(key: Key): Cipher {
        val cipher = Cipher.getInstance(K.RSA_CIPHER)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher
    }

    override fun rsaDecryptionCipher(key: Key): Cipher {
        val cipher = Cipher.getInstance(K.RSA_CIPHER)
        cipher.init(Cipher.DECRYPT_MODE, key)
        return cipher
    }

    override fun rsaSigner(key: PrivateKey): Signature {
        val cipher = Signature.getInstance(K.RSA_SIGN)
        cipher.initSign(key)
        return cipher
    }

    override fun rsaVerifier(key: PublicKey): Signature {
        val cipher = Signature.getInstance(K.RSA_SIGN)
        cipher.initVerify(key)
        return cipher
    }

    companion object {
        fun createAesKey(): SecretKey {
            val blob = ByteArray(Best.AESKeyLength / 8)
            SecureRandom.getInstanceStrong().nextBytes(blob)
            return SecretKeySpec(blob, "AES")
        }
    }

    private class SecUtilImpl(
        private val datadir: File,
        private val pass: CharArray,
        private val backupAlias: String = ISecUtil.ALIAS_BACKUP,
    ) : ISecUtilAccessor {
        companion object {
            private val ALIAS_ETC = "#etc"
        }

        private var keystore: KeyStore? = null
        private var protection = PasswordProtection(pass)
        private val etcRootInfo = EncryptedRootInfo(getEtcKey(), Conf.getEtcDir(datadir), "etc") {
            EtcPaths.isValid(it)
        }

        override fun reset() {
            val keystore = loadKeystore()
            for (alias in keystore.aliases().toList()) {
                keystore.deleteEntry(alias)
            }
            keystore.setEntry(ISecUtil.ALIAS_DB, SecretKeyEntry(SecUtil.createAesKey()), protection)
            keystore.setEntry(ALIAS_ETC, SecretKeyEntry(SecUtil.createAesKey()), protection)
            createbackupkey1(Best.RSAKeylength)
            saveKeystore()
        }

        override fun deleteAlias(alias: String) {
            loadKeystore().deleteEntry(alias)
            saveKeystore()
        }

        @Throws
        override fun getBackupPublicKeys(): Map<String, Certificate> {
            try {
                val keystore = loadKeystore()
                if (!keystore.containsAlias(backupAlias)) createBackupKey(Best.RSAKeylength)
                return mapOf(*(keystore.aliases().asSequence().map {
                    val cert = certOf(keystore.getEntry(it, protectionFor(it)))
                    if (cert != null) Pair(it, cert) else null
                }.filterNotNull().toList().toTypedArray()))
            } catch (e: Throwable) {
                
                throw e
            }
        }

        @Throws
        override fun getDbKey(): SecretKey {
            val keystore = loadKeystore()
            return (keystore.getEntry(ISecUtil.ALIAS_DB, protection) as SecretKeyEntry?)?.secretKey
                ?: createAesKey(keystore, ISecUtil.ALIAS_DB)
        }

        @Throws
        override fun getBackupKeyPair(): BackupKeyPair {
            return ((loadKeystore().getEntry(backupAlias, protection) as PrivateKeyEntry?)?.let {
                BackupKeyPair(it.privateKey, it.certificate as Certificate)
            } ?: createBackupKey(Best.RSAKeylength))
        }

        override fun getBackupPublicKey(): Certificate {
            return getBackupKeyPair().certificate
        }

        @Throws
        override fun getPublicKey(alias: String): Certificate? {
            if (alias.isEmpty()) return null
            return loadKeystore().getEntry(alias, protectionFor(alias))?.let {
                certOf(it)
            } ?: if (alias == backupAlias) createBackupKey(Best.RSAKeylength).certificate else null
        }

        override fun createBackupKey(keylen: Int): BackupKeyPair {
            try {
                loadKeystore().deleteEntry(backupAlias)
                return createbackupkey1(keylen)
            } catch (e: Throwable) {
                throw SecureException("", e)
            }
        }

        @Throws
        private fun createbackupkey1(keylen: Int): BackupKeyPair {
            val file = Conf.getKeystore(datadir)
            var bits = keylen
            while (bits >= 2048) {
                try {
                    saveKeystore()
                    ProcessUtBuilder(
                        "keytool",
                        "-keystore", file.absolutePath, "-storetype", K.KEY_STORE,
                        "-alias", backupAlias, "-storepass", String(pass), "-keypass", String(pass),
                        "-genkeypair", "-keyalg", "rsa", "-keysize", "$keylen", "-validity", "36500", "-dname", "cn=${K.CN}"
                    ).backtick { rc, out, err ->
                        if (rc != 0) {
                            throw AssertionError("ERROR: rc=$rc")
                        }
                    }.get(K.TIMEOUT, SECONDS)
                    
                    return (reloadKeystore().getEntry(backupAlias, protection) as PrivateKeyEntry).let {
                        BackupKeyPair(it.privateKey, it.certificate as Certificate)
                    }
                } catch (e: Throwable) {
                    reloadKeystore().deleteEntry(backupAlias)
                    bits -= 1024
                }
            }
            throw AssertionError()
        }

        override fun importPublicKey(alias: String, input: InputStream): IBotResult<TrustedCertificateEntry, StringId> {
            if (alias.isEmpty() || alias.startsWith(K.PREFIX)) return BotResult.fail(R.string.InvalidKeyAlias)
            val keystore = loadKeystore()
            try {
                val aliases = getBackupPublicKeys()
                if (aliases.containsKey(alias)) return BotResult.fail(string.BackupKeyAliasAlreadyExists)
                val max = ISecUtil.MAX_BACKUP_KEYS - (if (aliases.contains(backupAlias)) 0 else 1)
                if (aliases.size >= max && !aliases.contains(alias)) return BotResult.fail(string.BackupKeyNoMoreSlot)
                val cert = CertificateFactory.getInstance("X509").generateCertificate(input)
                for (value in aliases.values) {
                    if (cert.encoded.contentEquals(value.encoded)) return BotResult.fail(string.BackupKeyAlreadyExists)
                }
                val entry = TrustedCertificateEntry(cert)
                keystore.deleteEntry(alias)
                keystore.setEntry(alias, entry, null)
                saveKeystore()
                return BotResult.ok(entry)
            } catch (e: Throwable) {
                return BotResult.fail(string.ImportFailed)
            }
        }

        override fun putCf(name: String): OutputStream? {
            return Without.throwableOrNull {
                val file = etcRootInfo.fileInfo(name)
                try {
                    if (file.exists)
                        file.delete()
                    return@throwableOrNull file.content().outputStream()
                } catch (e: Throwable) {
                    file.delete()
                }
                null
            }
        }

        override fun getCf(name: String): InputStream? {
            return Without.throwableOrNull {
                val file = etcRootInfo.fileInfo(name)
                if (file.exists) {
                    try {
                        return@throwableOrNull file.content().inputStream()
                    } catch (e: Throwable) {
                        file.delete()
                    }
                }
                null
            }
        }

        override fun deleteCf(name: String): Boolean {
            return etcRootInfo.fileInfo(name).delete()
        }

        override fun descOf(alias: String, cert: Certificate): Array<String> {
            val valid = dateOf(cert)
            val signature = sha256Of(cert)
            return arrayOf(alias, valid, signature.second, barchart(signature.first))
        }

        private fun certOf(entry: Entry?): Certificate? {
            if (entry == null) return null
            return if (entry is PrivateKeyEntry) {
                val cert = entry.certificate
                if (cert is Certificate) cert else null
            } else if (entry is TrustedCertificateEntry) {
                val cert = entry.trustedCertificate
                if (cert is Certificate) cert else null
            } else null
        }

        private fun createAesKey(
            keystore: KeyStore,
            alias: String,
            mode: String = Best.AES_MODE,
            padding: String = Best.AES_PADDING
        ): SecretKey {
            val key = SecUtil.createAesKey()
            keystore.deleteEntry(alias)
            keystore.setEntry(alias, SecretKeyEntry(key), protection)
            saveKeystore()
            return key
        }

        private fun loadKeystore(): KeyStore {
            return keystore ?: reloadKeystore()
        }

        private fun saveKeystore() {
            keystore?.let { keystore ->
                val file = Conf.getKeystore(datadir)
                file.mkparentOrFail()
                file.outputStream().use {
                    keystore.store(it, pass)
                }
                file.setReadable(true, true)
            }
        }

        private fun reloadKeystore(): KeyStore {
            val keystore = KeyStore.getInstance(K.KEY_STORE).also { this.keystore = it }
            val file = Conf.getKeystore(datadir)
            file.mkparentOrFail()
            if (!file.exists()) {
                keystore.load(null, pass)
                saveKeystore()
            } else {
                file.inputStream().use {
                    keystore.load(it, pass)
                }
            }
            return keystore
        }

        private fun protectionFor(alias: String): ProtectionParameter? {
            return if (alias.startsWith(K.PREFIX)) protection else null
        }

        @Throws
        private fun getEtcKey(): SecretKey {
            val keystore = loadKeystore()
            return (keystore.getEntry(ALIAS_ETC, protection) as SecretKeyEntry?)?.secretKey
                ?: createAesKey(keystore, ALIAS_ETC)
        }

        private fun barchart(blob: ByteArray): String {
            fun gray(w: Int, h: Int, generator: Fun10<Fun30<Int, Int, Int>>): String {
                val image = BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR)
                val gfx = image.createGraphics()
                gfx.background = Color(255, 255, 255, 0)
                gfx.clearRect(0, 0, w, h)
                val raster = image.raster
                generator { x, y, value ->
                    raster.setPixel(x, y, intArrayOf(value, value, value, 0xff))
                }
                return ImageUtil.toPngDataUrl(ByteArrayOutputStream().use {
                    ImageIO.write(image, "png", it)
                    it
                }.toByteArray())
            }
            return gray(64, 32) { setter ->
                val sorted = Array(16) { 0 }
                blob.flatMap {
                    val v = it.toInt()
                    listOf((v ushr 4) and 0xf, v and 0xf)
                }.forEach {
                    ++sorted[it]
                }
                for ((x, value) in sorted.withIndex()) {
                    val y = if (value > 7) 7 else value
                    for (yy in 0 until y) {
                        val x0 = (15 - x) * 4
                        val y0 = (7 - yy) * 4
                        for (dx in 0..3) {
                            for (dy in 0..3) {
                                setter(x0 + dx, y0 + dy, 0)
                            }
                        }
                    }
                }
            }
        }

    }
}
