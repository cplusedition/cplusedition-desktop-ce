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

import com.cplusedition.bot.core.Fun11
import com.cplusedition.bot.core.IBotResult
import java.security.Key
import java.security.KeyStore.PrivateKeyEntry
import java.security.KeyStore.TrustedCertificateEntry
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

    fun getBackupKeyAliases(): Map<String, Certificate>

    /**
     * Get secret key for database access, create if not exists.
     */
    fun getDbKey(): SecretKey

    /**
     * Create a public/private key pair for backups, replace existing.
     * @param keylen in bits. Only 2048 or 4096 is supported.
     * @throw SecureException if create failed.
     */
    fun createBackupKey(keylen: Int): PrivateKeyEntry

    /**
     * Get private key for reading backups, create on if not exists.
     * @throw SecureException if create failed.
     */
    @Throws(SecureException::class)
    fun getPrivateKey(): PrivateKey

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
    fun importPublicKey(alias: String, publickeyfile: IFileInfo): IBotResult<TrustedCertificateEntry, StringId>

    fun exportPublicKey(outfile: IFileInfo): IBotResult<Certificate, StringId>

    @Throws
    fun putCf(name: String, data: ByteArray)

    fun getCf(name: String): ByteArray?

    /**
     * @return [alias, valid, signature, barchart(sha256)]
     */
    fun descOf(alias: String, cert: X509Certificate): Array<String>

}

interface ISecUtil {
    companion object {
        val ALIAS_BACKUP = "#self"
        val ALIAS_DB = "#db"
        val MAX_BACKUP_KEYS = 8
    }

    fun <R> invoke(code: Fun11<ISecUtilAccessor, R>): R
    fun rsaEncryptionCipher(key: Key): Cipher
    fun rsaDecryptionCipher(key: Key): Cipher
    fun rsaSign(key: PrivateKey): Signature
    fun rsaVerify(key: PublicKey): Signature
}

object Best {
    //// Note that as of API-23, KeyProperties only listed PKCS7Padding, which do not work in desktop chrome.
    //// While not listed, PKCS5Padding seems to be working fine so far. So stay with PKCS5Padding for now.
    //// However, Apple seems to support PKCS7Padding only also. So stay with PKCS5Padding for desktop testing,
    //// but patch to PKCS7Padding for mobile releases.
    const val CBC = "AES/CBC/PKCS5Padding"
    const val GCM = "AES/GCM/NoPadding"
    const val AESKeyLength = 256
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

