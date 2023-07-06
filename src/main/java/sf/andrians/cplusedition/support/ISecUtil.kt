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
package sf.andrians.cplusedition.support

import com.cplusedition.bot.core.DateUt
import com.cplusedition.bot.core.Fun11
import com.cplusedition.bot.core.Hex
import com.cplusedition.bot.core.IBotResult
import java.io.InputStream
import java.io.OutputStream
import java.security.Key
import java.security.KeyStore.PrivateKeyEntry
import java.security.KeyStore.TrustedCertificateEntry
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey

interface ISecUtilAccessor {

    fun reset()

    fun deleteAlias(alias: String)

    fun getBackupPublicKeys(): Map<String, Certificate>

    /**
     * Get secret key for database access, create if not exists.
     */
    fun getDbKey(): SecretKey

    /**
     * Create a public/private key pair for backups, replace existing.
     * @param keylen in bits. Only 2048 or 4096 is supported.
     * @throw SecureException if create failed.
     */
    fun createBackupKey(keylen: Int): BackupKeyPair

    /**
     * Get private key for reading backups, create on if not exists.
     * @throw SecureException if create failed.
     */
    @Throws(SecureException::class)
    fun getBackupKeyPair(): BackupKeyPair
    fun getBackupPublicKey(): Certificate

    /**
     * Get public key for creating backups.
     * @return null if not exists.
     */
    fun getPublicKey(alias: String): Certificate?

    /**
     * Import additional public key for reading backups created elsewhere, replace existing.
     * @return cert if import OK, error msgId if failed.
     */
    @Throws
    fun importPublicKey(alias: String, input: InputStream): IBotResult<TrustedCertificateEntry, StringId>

    /**
     * @data null to delete entry.
     */
    @Throws
    fun putCf(name: String): OutputStream?

    fun getCf(name: String): InputStream?

    fun deleteCf(name: String): Boolean

    /**
     * @return [alias, valid, signature, barchart(sha256)]
     */
    fun descOf(alias: String, cert: Certificate): Array<String>

}

data class BackupKeyPair constructor(
    val privateKey: PrivateKey,
    val certificate: Certificate,
)

interface ISecUtil {
    object EtcPaths {
        const val eventsCf = "events.cf"
        const val preferencesCf = "preferences.cf"
        const val sessionCf = "session.cf"
        const val settingsCf = "settings.cf"
        const val xrefsCf = "xrefs.cf"
        fun isValid(rpath: String): Boolean {
            return rpath == eventsCf
                    || rpath == preferencesCf
                    || rpath == sessionCf
                    || rpath == settingsCf
                    || rpath == xrefsCf
        }
    }

    companion object {
        const val ALIAS_BACKUP = "#self"
        const val ALIAS_DB = "#db"
        const val MAX_BACKUP_KEYS = 8
        private val sha256 = MessageDigest.getInstance("SHA-256")!!

        fun sha256Of(cert: Certificate): Pair<ByteArray, String> {
            val sha256 = sha256.digest(cert.encoded)
            val signature = Hex.encode(sha256, true).chunked(8).joinToString("-")
            return Pair(sha256, signature)
        }

        fun dateOf(cert: Certificate): String {
            return if (cert is X509Certificate) DateUt.dateString(cert.notAfter) else "????????"
        }
    }

    fun <R> invoke(code: Fun11<ISecUtilAccessor, R>): R
    fun rsaEncryptionCipher(key: Key): Cipher
    fun rsaDecryptionCipher(key: Key): Cipher
    fun rsaSigner(key: PrivateKey): Signature
    fun rsaVerifier(key: PublicKey): Signature
}

object Best {
    //// Note that as of API-23, KeyProperties only listed PKCS7Padding, which do not work in desktop chrome.
    //// While not listed, PKCS5Padding seems to be working fine so far. So stay with PKCS5Padding for now.
    //// However, Apple seems to support PKCS7Padding only also. So stay with PKCS5Padding for desktop testing,
    //// but patch to PKCS7Padding for mobile releases.
    const val CBC = "AES/CBC/PKCS5Padding"
    const val GCM = "AES/GCM/NoPadding"
    const val CBC_PADDING = "PKCS5Padding"
    const val GCM_PADDING = "NoPadding"
    const val AES_MODE = "GCM"
    const val AES_PADDING = "NoPadding"
    const val AESKeyLength = 256
    //// Apparently, some device not having hardware support for keylength of 4096 yet.
    //// Using 2048 for now. Should try 4096 later.
    const val RSAKeylength = 2048
    private val nameToCode: MutableMap<String, String> = TreeMap()
    private val codeToName: MutableMap<String, String> = TreeMap()

    init {
        nameToCode[CBC] = "0"
        nameToCode[GCM] = "1"
        codeToName["0"] = CBC
        codeToName["1"] = GCM
        nameToCode["AES/CBC/PKCS7Padding"] = "0"
    }

    @Throws(SecureException::class)
    fun encodeAlgorithm(name: String?): String {
        return nameToCode[name] ?: throw SecureException("$name")
    }

    @Throws(SecureException::class)
    fun decodeAlgorithm(code: String, allowname: Boolean): String {
        val ret = codeToName[code]
        if (ret != null) {
            return ret
        }
        if (allowname && nameToCode[code] != null) {
            return code
        }
        throw SecureException(code)
    }

    fun codeOf(algo: String): Int? {
        when (algo) {
            Best.CBC -> return 0
            Best.GCM -> return 1
            else -> return null
        }
    }

    @Throws(SecureException::class)
    fun nameOf(algo: Int): String? {
        when (algo) {
            0 -> return CBC
            1 -> return GCM
            else -> return null
        }
    }
}
