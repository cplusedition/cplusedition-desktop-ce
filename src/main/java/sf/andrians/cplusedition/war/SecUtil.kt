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
import com.cplusedition.bot.core.DateUt
import com.cplusedition.bot.core.Fun10
import com.cplusedition.bot.core.Fun11
import com.cplusedition.bot.core.Fun30
import com.cplusedition.bot.core.Hex
import com.cplusedition.bot.core.IBotResult
import com.cplusedition.bot.core.ProcessUtBuilder
import com.cplusedition.bot.core.StepWatch
import com.cplusedition.bot.core.join
import com.cplusedition.bot.core.mkparentOrFail
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.Best
import sf.andrians.cplusedition.support.EncryptedFileRootInfo
import sf.andrians.cplusedition.support.IFileInfo
import sf.andrians.cplusedition.support.ISecUtil
import sf.andrians.cplusedition.support.ISecUtilAccessor
import sf.andrians.cplusedition.support.SecureException
import sf.andrians.cplusedition.support.StringId
import sf.andrians.cplusedition.war.support.MediaUtil
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.Key
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStore.PasswordProtection
import java.security.KeyStore.PrivateKeyEntry
import java.security.KeyStore.ProtectionParameter
import java.security.KeyStore.SecretKeyEntry
import java.security.KeyStore.TrustedCertificateEntry
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Signature
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.locks.ReentrantLock
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.imageio.ImageIO

class SecUtil(datadir: File, pass: CharArray) : ISecUtil {
    private val lock = ReentrantLock()
    private val impl = SecUtilImpl(datadir, pass)

    private object K {
        val KEY_STORE = "PKCS12"
        val RSA_SIGN = "SHA512withRSA"
        val RSA_CIPHER = "RSA/ECB/PKCS1Padding"
        val TIMEOUT = 30L // sec.
        val CN = "self"
        val PREFIX = "#"
    }

    companion object {
        val srand = SecureRandom.getInstanceStrong()
        val sha256 = MessageDigest.getInstance("SHA-256")!!

        fun random(count: Int): ByteArray {
            val ret = ByteArray(count)
            srand.nextBytes(ret)
            return ret
        }

        fun sanityCheck() {
            val timer = StepWatch()
            kotlin.run {
                val seen = ArrayList<ByteArray>()
                var count = 0
                for (iter in 0 until 10 * 1000) {
                    val a = random(16)
                    if (seen.any { it.contentEquals(a) }) ++count
                    seen.add(a)
                }
                if (count > 1) throw SecureException()
                
            }
            kotlin.run {
                val key = SecretKeySpec(random(32), "AES")
                val cipher = Cipher.getInstance(Best.CBC)
                val seen = ArrayList<ByteArray>()
                var count = 0
                for (iter in 0 until 10 * 1000) {
                    cipher.init(Cipher.ENCRYPT_MODE, key)
                    val a = cipher.iv
                    if (seen.any { it.contentEquals(a) }) ++count
                    seen.add(a)
                }
                if (count > 1) throw SecureException()
                
            }
            kotlin.run {
                val key = SecretKeySpec(random(32), "AES")
                val cipher = Cipher.getInstance(Best.GCM)
                val seen = ArrayList<ByteArray>()
                var count = 0
                for (iter in 0 until 10 * 1000) {
                    cipher.init(Cipher.ENCRYPT_MODE, key)
                    val a = cipher.iv
                    if (seen.any { it.contentEquals(a) }) ++count
                    seen.add(a)
                }
                if (count > 1) throw SecureException()
                
            }
            kotlin.run {
                val seen = ArrayList<ByteArray>()
                for (iter in 0 until 10) {
                    val keypair = KeyPairGenerator.getInstance("RSA").genKeyPair()
                    val privatekey = keypair.private.encoded
                    if (seen.any { it.contentEquals(privatekey) }) throw SecureException()
                    seen.add(privatekey)
                    val publickey = keypair.public.encoded
                    if (seen.any { it.contentEquals(publickey) }) throw SecureException()
                    seen.add(publickey)
                }
                
            }
        }
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

    override fun rsaSign(key: PrivateKey): Signature {
        val cipher = Signature.getInstance(K.RSA_SIGN)
        cipher.initSign(key)
        return cipher
    }

    override fun rsaVerify(key: PublicKey): Signature {
        val cipher = Signature.getInstance(K.RSA_SIGN)
        cipher.initVerify(key)
        return cipher
    }

    private class SecUtilImpl(private val datadir: File, private val pass: CharArray) : ISecUtilAccessor {
        companion object {
            private val ALIAS_ETC = "#etc"
        }

        private var keystore: KeyStore? = null
        private var protection = PasswordProtection(pass)
        private val etcRootInfo = EncryptedFileRootInfo(getEtcKey(), Conf.getEtcDir(datadir), "etc")

        override fun reset() {
            val keystore = loadKeystore()
            for (alias in keystore.aliases().toList()) {
                keystore.deleteEntry(alias)
            }
            createAesKey(keystore, ISecUtil.ALIAS_DB)
            createAesKey(keystore, ALIAS_ETC)
            createBackupKey(Best.RSAKeylength)
        }

        override fun deleteAlias(alias: String) {
            loadKeystore().deleteEntry(alias)
            saveKeystore()
        }

        override fun getBackupKeyAliases(): Map<String, Certificate> {
            try {
                val keystore = loadKeystore()
                if (!keystore.containsAlias(ISecUtil.ALIAS_BACKUP)) createBackupKey(Best.RSAKeylength)
                return mapOf(*(keystore.aliases().asSequence().map {
                    val entry = keystore.getEntry(it, protectionFor(it))
                    if (entry is PrivateKeyEntry) Pair(it, entry.certificate)
                    else if (entry is TrustedCertificateEntry) Pair(it, entry.trustedCertificate)
                    else null
                }.filterNotNull().toList().toTypedArray()))
            } catch (e: Throwable) {
                
                throw e
            }
        }

        override fun getDbKey(): SecretKey {
            val keystore = loadKeystore()
            return (keystore.getEntry(ISecUtil.ALIAS_DB, protection) as SecretKeyEntry?)?.secretKey
                    ?: createAesKey(keystore, ISecUtil.ALIAS_DB)
        }

        @Throws(SecureException::class)
        override fun getPrivateKey(): PrivateKey {
            return ((loadKeystore().getEntry(ISecUtil.ALIAS_BACKUP, protection) as PrivateKeyEntry?)
                    ?: createBackupKey(Best.RSAKeylength)).privateKey
        }

        override fun getPublicKey(alias: String): Certificate? {
            if (alias.isEmpty()) return null
            val isbackup = (alias == ISecUtil.ALIAS_BACKUP)
            val ret = loadKeystore().getEntry(alias, protectionFor(alias))
                    ?: (if (isbackup) createBackupKey(Best.RSAKeylength) else null)
            return if (ret is PrivateKeyEntry) ret.certificate
            else if (ret is TrustedCertificateEntry) ret.trustedCertificate
            else null
        }

        override fun createBackupKey(keylen: Int): PrivateKeyEntry {
            try {
                loadKeystore().deleteEntry(ISecUtil.ALIAS_BACKUP)
                saveKeystore()
                val file = Conf.getKeystore(datadir)
                ProcessUtBuilder("keytool",
                        "-keystore", file.absolutePath, "-storetype", K.KEY_STORE,
                        "-alias", ISecUtil.ALIAS_BACKUP, "-storepass", String(pass), "-keypass", String(pass),
                        "-genkeypair", "-keyalg", "rsa", "-keysize", "$keylen", "-validity", "36500", "-dname", "cn=${K.CN}"
                ).asyncOrFail().get(K.TIMEOUT, SECONDS)
                return reloadKeystore().getEntry(ISecUtil.ALIAS_BACKUP, protection) as PrivateKeyEntry
            } catch (e: Throwable) {
                throw SecureException("", e)
            }
        }

        override fun importPublicKey(alias: String, publickeyfile: IFileInfo): IBotResult<TrustedCertificateEntry, StringId> {
            if (!publickeyfile.exists) return BotResult.fail(R.string.FileNotFound)
            if (alias.isEmpty() || alias.startsWith(K.PREFIX)) return BotResult.fail(R.string.InvalidKeyAlias)
            val keystore = loadKeystore()
            try {
                val aliases = getBackupKeyAliases()
                if (aliases.containsKey(alias)) return BotResult.fail(R.string.BackupKeyAliasAlreadyExists)
                val max = ISecUtil.MAX_BACKUP_KEYS - (if (aliases.contains(ISecUtil.ALIAS_BACKUP)) 0 else 1)
                if (aliases.size >= max && !aliases.contains(alias)) return BotResult.fail(R.string.BackupKeyNoMoreSlot)
                val cert = publickeyfile.content().getInputStream().use { CertificateFactory.getInstance("X509").generateCertificate(it) }
                for (value in aliases.values) {
                    if (cert.encoded.contentEquals(value.encoded)) return BotResult.fail(R.string.BackupKeyAlreadyExists)
                }
                val entry = TrustedCertificateEntry(cert)
                keystore.deleteEntry(alias)
                keystore.setEntry(alias, entry, null)
                saveKeystore()
                return BotResult.ok(entry)
            } catch (e: Throwable) {
                return BotResult.fail(R.string.ImportFailed)
            }
        }

        override fun exportPublicKey(outfile: IFileInfo): IBotResult<Certificate, StringId> {
            try {
                val cert = getPublicKey(ISecUtil.ALIAS_BACKUP) ?: return BotResult.fail(R.string.ExportFailed)
                val encoded = cert.encoded ?: return BotResult.fail(R.string.ExportFailed)
                outfile.content().write(encoded)
                return BotResult.ok(cert)
            } catch (e: Throwable) {
                return BotResult.fail(R.string.ExportFailed)
            }
        }

        override fun putCf(name: String, data: ByteArray) {
            etcRootInfo.fileInfo(name).content().write(data)
        }

        override fun getCf(name: String): ByteArray? {
            try {
                val file = etcRootInfo.fileInfo(name)
                if (!file.exists) return null
                return file.content().readBytes()
            } catch (e: Exception) {
                return null
            }
        }

        override fun descOf(alias: String, cert: X509Certificate): Array<String> {
            val valid = DateUt.dateString(cert.notAfter)
            val sha256 = sha256.digest(cert.encoded)
            val signature = Hex.encode(sha256, true).chunked(8).join(":")
            return arrayOf(alias, valid, signature, barchart(sha256))
        }

        private fun createAesKey(keystore: KeyStore, alias: String): SecretKey {
            val blob = ByteArray(Best.AESKeyLength / 8)
            SecureRandom.getInstanceStrong().nextBytes(blob)
            val key = SecretKeySpec(blob, "AES")
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
                return MediaUtil.toDataUrl("image/png", ByteArrayOutputStream().use {
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

