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

import com.cplusedition.bot.core.Basepath
import com.cplusedition.bot.core.ByteReader
import com.cplusedition.bot.core.FileUt
import com.cplusedition.bot.core.Fun01
import com.cplusedition.bot.core.Fun10
import com.cplusedition.bot.core.Fun11
import com.cplusedition.bot.core.Fun21
import com.cplusedition.bot.core.Hex
import com.cplusedition.bot.core.IOUt
import com.cplusedition.bot.core.LimitedInputStream
import com.cplusedition.bot.core.LimitedOutputStream
import com.cplusedition.bot.core.MyByteArrayOutputStream
import com.cplusedition.bot.core.PositionTrackingOutputStream
import com.cplusedition.bot.core.PositionTrackingOutputStreamAdapter
import com.cplusedition.bot.core.ResultException
import com.cplusedition.bot.core.StepWatch
import com.cplusedition.bot.core.StructUt
import com.cplusedition.bot.core.join
import com.cplusedition.bot.core.mkparentOrNull
import org.json.JSONObject
import sf.andrians.ancoreutil.util.io.ByteIOUtil
import sf.andrians.ancoreutil.util.io.ByteOutputStream
import sf.andrians.ancoreutil.util.struct.Empty
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.R.string
import sf.andrians.cplusedition.support.An.DEF
import sf.andrians.cplusedition.support.ArchiveFormat.Kind
import sf.andrians.cplusedition.support.handler.IResUtil
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.security.DigestInputStream
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.interfaces.RSAPublicKey
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import java.util.zip.Deflater
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.concurrent.withLock

class BackupUtil(
        private val rsrc: IResUtil,
        private val secUtil: ISecUtil
) {

    @Throws(SecureException::class)
    fun exportData(zipfile: IFileInfo, srcdir: IFileInfo): BackupRestoreResult {
        try {
            zipfile.content().getOutputStream().use { output ->
                ZipOutputStream(BufferedOutputStream(output)).use { zip ->
                    val result = BackupRestoreResult()
                    return BU.writeExport(zip, result, srcdir, srcdir.cpath)
                }
            }
        } catch (e: Throwable) {
            zipfile.delete()
            throw rsrc.secureException(e, R.string.ExportFailed)
        }
    }

    @Throws(SecureException::class)
    fun backupData(backupfile: IFileInfo, aliases: List<String>, srcdir: IFileInfo): BackupRestoreResult {
        try {
            val format = ArchiveFormat.latestBackupFormat()
            val incremental = backupfile.name.endsWith(DEF.ibackupSuffix)
            val infos = getLastBackupInfos(backupfile) ?: TreeMap()
            val param = BackupParam(format, incremental, infos, srcdir)
            backupData1(backupfile, aliases, param)
            return BackupRestoreResult(param.oks, param.skipped, param.fails)
        } catch (e: Throwable) {
            backupfile.delete()
            throw rsrc.secureException(e, R.string.BackupDataFailed)
        }
    }

    fun backupKey(keyfile: IFileInfo) {
        try {
            backupKey1(keyfile)
        } catch (e: Throwable) {
            keyfile.delete()
            throw rsrc.secureException(e, R.string.BackupKeyFailed)
        }
    }

    @Throws(SecureException::class)
    fun restoreData(destdir: IFileInfo, backupfile: IFileInfo, from: String, sync: Boolean = false): BackupRestoreResult {
        val backupfiles: MutableList<IFileInfo> = ArrayList()
        val filename = backupfile.name
        if (!filename.endsWith(DEF.ibackupSuffix)) {
            backupfiles.add(backupfile)
        } else {
            val dir = backupfile.parent ?: throw rsrc.secureException(R.string.InvalidPath)
            val names = BU.getIBackupFilenames(filename, dir)
            for (name in names) {
                backupfiles.add(dir.fileInfo(name))
                if (name == filename) break
            }
        }
        try {
            return if (backupfiles.size == 1) {
                restoreData1(backupfiles.first(), destdir, from, sync)
            } else {
                restoreData2(backupfiles, destdir, from, sync)
            }
        } catch (e: Throwable) {
            throw rsrc.secureException(e, R.string.RestoreDataFailed)
        }
    }

    fun readBackupFiletree(backupfile: IFileInfo): JSONObject {
        try {
            backupfile.content().getSeekableInputStream().use { input ->
                openBackupInputStream(input).let { (cis, format) ->
                    cis.use {
                        format.readPadding(cis)
                        return format.readFiletree(rsrc, cis)
                    }
                }
            }
        } catch (e: Throwable) {
            throw rsrc.secureException(e, R.string.ErrorReadingBackup)
        }
    }

    //////////////////////////////////////////////////////////////////////

    private fun backupData1(backupfile: IFileInfo, aliases: List<String>, param: BackupParam) {
        backupfile.content().getOutputStream().use { os ->
            openBackupOutputStream(os, aliases, param.format).use { cout ->
                BU.backupData(cout, param)
            }
        }
    }

    private fun backupKey1(keyfile: IFileInfo) {
        secUtil.invoke {
            it.getPublicKey(ISecUtil.ALIAS_BACKUP)
        }?.encoded?.inputStream()?.use {
            keyfile.content().write(it)
        }
    }

    @Throws(IOException::class)
    private fun openBackupOutputStream(out: OutputStream, aliases: List<String>, format: IArchiveFormat): PositionTrackingOutputStream {
        val key = KeyUtil.createAESKey()
        val key1 = KeyUtil.createAESKey()
        val cipher = format.fileFormat().createCipher(key)
        val cipher1 = format.fileFormat().createCipher(key1)
        val eeprefix = BU.writePadded(cipher1, format) { aesout ->
            val eprefix = secUtil.invoke {
                val rsakey = it.getPrivateKey()
                ByteOutputStream().use { output ->
                    val payload = ByteOutputStream().use { out ->
                        format.writePadding(out)
                        U.writeU8(out, format.fileFormat().encodeBlocksize())
                        format.writeCipher(out, key, cipher)
                        out
                    }.toByteArray()
                    val sign = secUtil.rsaSign(rsakey)
                    sign.update(payload)
                    val signature = sign.sign()
                    U.writeInt32Bytes(output, payload)
                    U.writeInt32Bytes(output, signature)
                    output
                }.toByteArray()
            }
            U.writeInt32Bytes(aesout, eprefix, eprefix.size)
            
        }
        
        val padding = V10Padding()
        val padding1 = U.randBytes(0, 128)
        val padding2 = U.randBytes(0, 128)
        val padding3 = U.randBytes(0, 128)
        val header = MyByteArrayOutputStream().use { output ->
            padding.writePadding(output) // 17..32
            output.write(ArchiveFormat.MAGIC) // 8
            U.writeU8(output, format.version()) // 1
            U.writeU8(output, aliases.size) // 1
            U.write32BE(output, eeprefix.size()) // 4
            U.writeU8(output, padding1.size) // 1
            U.writeU8(output, padding2.size) // 1
            U.writeU8(output, padding3.size) // 1
            U.writeU8Bytes(output, U.sha256(secUtil.invoke { it.getPublicKey(ISecUtil.ALIAS_BACKUP) }!!.encoded)) // 33
            format.writeCipher(output, key1, cipher1) // 16..20
            U.write(output, U.sha256(output.buffer(), 0, output.size())) // 32
            output
        }
        
        
        for (alias in aliases) {
            val cert = secUtil.invoke { it.getPublicKey(alias) }
                    ?: throw rsrc.secureException(R.string.BackupKeyInvalid)
            val rsa = secUtil.rsaEncryptionCipher(cert.publicKey)
            val a = U.random(512)
            rsa.doFinal(header.buffer(), 0, header.size(), a)
            out.write(a)
        }
        out.write(padding1)
        out.write(eeprefix.buffer(), 0, eeprefix.size())
        out.write(padding2)
        out.write(BU.marshalSalt(cipher.iv))
        return BlockOutputStream(StayOpenOutputStream(out), key, cipher, format.fileFormat(), padding3)
    }

    @Throws(IOException::class)
    private fun openBackupInputStream(input: ISeekableInputStream): Triple<ISeekableInputStream, IArchiveFormat, Certificate> {
        val rsakey = secUtil.invoke { it.getPrivateKey() }
        val keylen = secUtil.invoke { (it.getPublicKey(ISecUtil.ALIAS_BACKUP)!!.publicKey as RSAPublicKey).modulus.bitLength() }
        return openBackupInputStream(input, rsakey, keylen)
    }

    private fun openBackupInputStream(input: ISeekableInputStream, rsakey: PrivateKey, keylen: Int): Triple<ISeekableInputStream, IArchiveFormat, Certificate> {
        val padding = V10Padding()
        val buf = ByteArray(512)
        var offset = 0
        while (offset < 8 * 512) {
            offset += 512
            try {
                val header = readHeader(buf, input, rsakey, keylen)
                header.inputStream().use { hdrin ->
                    padding.readPadding(hdrin)
                    val magic = IOUt.readFully(hdrin, ByteArray(ArchiveFormat.MAGIC.size))
                    ByteIOUtil.equals(magic, ArchiveFormat.MAGIC) || throw SecureException()
                    val version = U.readU8(hdrin)
                    val format = ArchiveFormat.get(version) ?: throw SecureException()
                    val aliases = U.readU8(hdrin)
                    val prefixsize = U.read32BE(hdrin)
                    val padding1size = U.readU8(hdrin)
                    val padding2size = U.readU8(hdrin)
                    val padding3size = U.readU8(hdrin)
                    val keysig = U.readU8Bytes(hdrin)
                    val cipher1 = format.readCipher(hdrin).second
                    try {
                        var offset1 = offset
                        while (true) {
                            if (offset1 >= aliases * 512) break
                            offset1 += 512
                            IOUt.skipFully(input, 512)
                        }
                        ByteIOUtil.skipFully(input, padding1size.toLong())
                        val eeprefix = ByteIOUtil.readFully(input, ByteArray(prefixsize))
                        ByteIOUtil.skipFully(input, padding2size.toLong())
                        val eprefix = BU.readPadded(eeprefix, cipher1, format) { U.readInt32Bytes(it, 2048) }
                        for (cert in secUtil.invoke { it.getBackupKeyAliases() }.values) {
                            if (U.sha256(cert.encoded).contentEquals(keysig)) {
                                val prefix = eprefix.inputStream().use {
                                    val payload = U.readInt32Bytes(it, 2048)
                                    val signature = U.readInt32Bytes(it, 2048)
                                    val verify = secUtil.rsaVerify(cert.publicKey)
                                    verify.update(payload)
                                    if (!verify.verify(signature)) throw SecureException()
                                    payload
                                }
                                return prefix.inputStream().use {
                                    format.readPadding(it)
                                    val blocksize = format.fileFormat().decodeBlocksize(U.readU8(it))
                                    val (key, cipher) = format.readCipher(it)
                                    if (!BU.unmarshalSalt(ByteIOUtil.readFully(input, ByteArray(cipher.iv.size))).contentEquals(cipher.iv))
                                        throw SecureException()
                                    Triple(EncryptedInputStream(
                                            LimitedSeekableInputStream(input, input.getSize() - padding3size),
                                            key, format.fileFormat(), blocksize, cipher, input.getPosition()), format, cert)
                                }
                            }
                        }
                    } catch (e: Throwable) {
                        throw ResultException(rsrc.get(R.string.ErrorReadingBackup))
                    }
                    throw ResultException(rsrc.get(R.string.BackupPublicKeyNotFound_, Hex.encode(keysig).toString()))
                }
            } catch (e: ResultException) {
                throw SecureException(e.message!!)
            } catch (e: Throwable) {
            }
        }
        throw rsrc.secureException(R.string.ErrorReadingBackup)
    }

    private fun readHeader(buf: ByteArray, input: InputStream, rsakey: PrivateKey, keylen: Int): ByteArray {
        val rsa = secUtil.rsaDecryptionCipher(rsakey)
        IOUt.readFully(input, buf)
        val header = rsa.doFinal(buf, 0, keylen / 8)
        val size = header.size
        val expected = header.copyOfRange(size - K.SHA256_BUFSIZE, size)
        val actual = U.sha256(header.copyOfRange(0, size - K.SHA256_BUFSIZE))
        if (!expected.contentEquals(actual)) throw SecureException()
        
        return header
    }

    private fun restoreData2(
            backupfiles: MutableList<IFileInfo>,
            destdir: IFileInfo,
            from: String,
            sync: Boolean
    ): BackupRestoreResult {
        val filelist = readBackupFilelist(backupfiles.lastOrNull() ?: throw SecureException())
        val (infos, fromdir) = getFilelist(filelist, from)
        val result = BackupRestoreResult()
        val dirs = TreeMap<String, Info>()
        for (file in backupfiles.reversed()) {
            file.content().getSeekableInputStream().use { input ->
                openBackupInputStream(input).let { (cis, format) ->
                    val param = RestoreParam(format, destdir, fromdir, sync)
                    cis.use {
                        format.readPadding(cis)
                        restoreData3(dirs, cis, param, infos, format.readFilelist(rsrc, cis))
                        result.oks += param.oks
                        result.skipped += param.skipped
                        result.fails.addAll(param.fails)
                    }
                }
            }
        }
        syncDirs(destdir, dirs)
        return result
    }

    private fun restoreData1(
            backupfile: IFileInfo,
            destdir: IFileInfo,
            from: String,
            sync: Boolean
    ): BackupRestoreResult {
        backupfile.content().getSeekableInputStream().use { input ->
            openBackupInputStream(input).let { (cis, format) ->
                cis.use {
                    format.readPadding(cis)
                    val filelist = format.readFilelist(rsrc, cis)
                    val (infos, fromdir) = getFilelist(filelist, from)
                    val param = RestoreParam(format, destdir, fromdir, sync)
                    val dirs = TreeMap<String, Info>()
                    restoreData3(dirs, cis, param, infos, filelist)
                    syncDirs(param.restoredir, dirs)
                    return BackupRestoreResult(param.oks, param.skipped, param.fails)
                }
            }
        }
    }

    private fun getFilelist(infos0: MutableMap<String, Info>, from: String): Pair<MutableCollection<String>, String> {
        val frominfo = infos0[from] ?: throw rsrc.secureException(string.NotFound_, from)
        val infos = TreeSet<String>()
        val fromdir: String
        var totalsize = 0L
        if (frominfo.size >= 0) {
            infos.add(from)
            val dir = Basepath.dir(from)
            fromdir = if (dir == null || dir.isEmpty()) "" else "$dir/"
            totalsize += frominfo.size
        } else {
            fromdir = if (from.isEmpty()) from else "$from/"
            for ((rpath, info) in infos0.entries) {
                if (rpath.startsWith(fromdir)) {
                    infos.add(rpath)
                    if (info.size > 0) totalsize += info.size
                }
            }
        }
        return Pair(infos, fromdir)
    }

    /// @fromdir With trailing /.
    @Throws(Exception::class)
    private fun restoreData3(
            dirs: MutableMap<String, Info>,
            input: ISeekableInputStream,
            param: RestoreParam,
            infos: MutableCollection<String>,
            filelist: MutableMap<String, Info>
    ) {
        if (!param.tmpfile.mkparent()) throw SecureException()
        param.restoredir.root.transaction {
            for (rpath in ArrayList(infos)) {
                val info = filelist[rpath] ?: continue
                if (info.isDir) {
                    val path = if (rpath.startsWith(param.fromdir)) rpath.substring(param.fromdir.length) else rpath
                    if (!dirs.containsKey(path)) dirs.put(path, info)
                    infos.remove(rpath)
                    continue
                }
                if (!info.isFile) continue
                val file = param.restoredir.fileInfo(rpath.substring(param.fromdir.length))
                if (info.offset == 0L) continue
                val stat = file.stat()
                infos.remove(rpath)
                if (stat != null && (!dorestore(param.sync, stat, info) || issame(param, stat, file, info, input))) {
                    if (info.timestamp != stat.lastModified) file.setLastModified(info.timestamp)
                    ++param.skipped
                    continue
                }
                try {
                    input.setPosition(info.offset)
                    val actual = restoreFile(param.tmpfile, StayOpenInputStream(input), param) ?: continue
                    try {
                        if (info.checksum != null && !actual.contentEquals(info.checksum)) throw SecureException()
                        if (!prepareForWrite(file)) throw SecureException()
                        if (!param.tmpfile.content().renameTo(file, info.timestamp)) throw SecureException()
                        ++param.oks
                    } finally {
                        param.tmpfile.delete()
                    }
                } catch (e: java.lang.Exception) {
                    
                    param.fails.add(rpath)
                }
            }
        }
    }

    private fun syncDirs(destdir: IFileInfo, infos: Map<String, Info>) {
        for ((rpath, info) in infos.entries) {
            if (!info.isDir) continue
            try {
                val dir = destdir.fileInfo(rpath)
                var d: IFileInfo? = dir
                while (d != null && !d.exists) d = d.parent
                if (d?.stat()?.isFile == true) d.delete()
                if (!dir.exists && !dir.mkdirs()) continue
                dir.setLastModified(info.timestamp)
            } catch (e: Exception) {
            }
        }
    }

    fun prepareForWrite(file: IFileInfo): Boolean {
        if (file.exists) return FileInfoUtil.deleteTree(file)
        var d: IFileInfo? = file.parent
        while (d != null && !d.exists) d = d.parent
        if (d?.stat()?.isFile == true) d.delete()
        return file.mkparent()
    }

    private fun dorestore(sync: Boolean, stat: IFileStat, info: Info): Boolean {
        if (sync) return info.timestamp > stat.lastModified
        return (stat.lastModified != info.timestamp || stat.length != info.size)
    }

    private fun issame(param: RestoreParam, stat: IFileStat, file: IFileInfo, info: Info, input: ISeekableInputStream): Boolean {
        if (stat.length != info.size) return false
        val checksum = stat.checksumBytes
        if (checksum != null && info.checksum != null && checksum.size == info.checksum.size) {
            if (!checksum.contentEquals(info.checksum)) return false
        }
        input.setPosition(info.offset)
        var diff = false
        file.content().getInputStream().use { b ->
            BU.readBlocks(rsrc, param.tmpbuf, StayOpenInputStream(input)) { a, an ->
                val bn = b.read(param.copybuf, 0, an)
                if (an != bn || !StructUt.equals(param.copybuf, 0, an, param.diffbuf, 0, bn)) {
                    diff = true
                    true
                } else false
            }
        }
        return !diff
    }

    private fun restoreFile(outfile: IFileInfo, input: InputStream, param: RestoreParam): ByteArray? {
        when (BU.readtag(rsrc, input)) {
            Kind.Data -> {
                FileInfoUtil.deleteTree(outfile)
                return BU.copyBlocks(rsrc, outfile, param, input)
            }
            Kind.Etc -> {
                BU.skipBlocks(input)
                return null
            }
            else -> throw rsrc.secureException(string.ErrorReadingBackup)
        }
    }

    @Throws(SecureException::class)
    fun getLastBackupInfos(backupfile: IFileInfo): MutableMap<String, Info>? {
        if (!backupfile.name.endsWith(DEF.ibackupSuffix)) return null
        //// Find previous ibackup.
        val dir = backupfile.parent ?: return null
        if (!dir.exists) return null
        val lastibackup = BU.getLastBackupFile(dir, backupfile.name) ?: return null
        return readBackupFilelist(lastibackup)
    }

    fun readBackupFilelist(backupfile: IFileInfo): MutableMap<String, Info> {
        try {
            return backupfile.content().getSeekableInputStream().use { input ->
                readBackupFilelist(input)
            }
        } catch (e: Throwable) {
            throw rsrc.secureException(e, R.string.ErrorReadingBackup)
        }
    }

    private fun readBackupFilelist(input: ISeekableInputStream): MutableMap<String, Info> {
        openBackupInputStream(input).let { (cis, format) ->
            cis.use {
                format.readPadding(cis)
                return format.readFilelist(rsrc, cis)
            }
        }
    }

}

class BackupRestoreResult(
        var oks: Int = 0,
        var skipped: Int = 0,
        val fails: ArrayList<String> = ArrayList()
) {
}

private class BackupParam(
        val format: IArchiveFormat,
        val incremental: Boolean,
        val oinfos: MutableMap<String, Info>,
        vararg val roots: IFileInfo
) {
    val enableCompression = true
    var oks = 0
    var skipped = 0
    val fails = ArrayList<String>()
    val tmpbuf = ByteArray(K.BLOCKSIZE)
    val copybuf = ByteArray(K.BUFSIZE)
}

private class RestoreParam(
        val format: IArchiveFormat,
        val restoredir: IFileInfo,
        val fromdir: String,
        val sync: Boolean
) {
    var oks = 0
    var skipped = 0
    val fails = ArrayList<String>()
    val tmpbuf = ByteArray(K.BLOCKSIZE)
    val tmpfile = restoredir.fileInfo(K.RESTORE_TMP_FILE)
    val copybuf = ByteArray(K.BUFSIZE)
    val diffbuf = ByteArray(K.BUFSIZE)
}

private object BU {

    fun importPublicKey(keyfile: IFileInfo): PublicKey? {
        val factory = CertificateFactory.getInstance("X509")
        keyfile.content().getInputStream().use {
            val certpath = factory.generateCertPath(it)
            val cert = certpath.certificates.firstOrNull()
            return cert?.publicKey
        }
    }

    fun openEncryptedInputStream(seekable: ISeekableInputStream, key: SecretKey, format: IFileFormat): ISeekableInputStream {
        val blocksize = U.readU8(seekable) * K.ENCRYPTED_BLOCK_SIZE_MULTIPIER
        val cipher = format.readCipher(seekable, key)
        return EncryptedInputStream(seekable, key, format, blocksize, cipher, seekable.getPosition())
    }

    @Throws(IOException::class)
    fun readGzFilelist(input: InputStream, limit: Int, digester: MessageDigest): ByteArray {
        if (input.read() != Tag.FilelistGz.toInt()) throw IOException()
        val len = U.readInt32Length(input, limit)
        val ret = DigestInputStream(LimitedInputStream(input, len.toLong()), digester).use { dis ->
            GZIPInputStream(dis).use { gis ->
                gis.readBytes()
            }
        }
        val expected = U.readU8Bytes(input)
        if (!expected.contentEquals(digester.digest())) throw IOException()
        return ret
    }

    fun writePadded(cipher: Cipher, format: IPadding, content: Fun10<OutputStream>): MyByteArrayOutputStream {
        return U.writeByteArray { writePadded(it, cipher, format, content) }
    }

    fun writePadded(output: OutputStream, cipher1: Cipher, format: IPadding, content: Fun10<OutputStream>) {
        CipherOutputStream(output, cipher1).use {
            format.writePadding(it)
            content(it)
            format.writePadding(it)
        }
    }

    fun <T> readPadded(data: ByteArray, cipher: Cipher, format: IPadding, content: Fun11<InputStream, T>): T =
            U.readByteArray(data) { readPadded(it, cipher, format, content) }

    fun <T> readPadded(input: InputStream, cipher: Cipher, format: IPadding, content: Fun11<InputStream, T>): T =
            CipherInputStream(input, cipher).use {
                format.readPadding(it)
                val ret = content(it)
                format.readPadding(it)
                ret
            }

    @Throws(Exception::class)
    fun writeExport(out: ZipOutputStream, result: BackupRestoreResult, dir: IFileInfo, rpathx: String): BackupRestoreResult {
        if (dir.isDir) {
            writeExportDir(out, result, dir, rpathx)
        } else {
            writeExportFile(out, result, dir, rpathx)
        }
        return result
    }

    private fun writeExportDir(out: ZipOutputStream, result: BackupRestoreResult, dir: IFileInfo, rpathx: String) {
        val e = ZipEntry("$rpathx/")
        out.putNextEntry(e)
        out.closeEntry()
        for (file in dir.readDir(ArrayList())) {
            writeExport(out, result, file, "$rpathx/${file.name}")
        }
    }

    @Throws(Exception::class)
    private fun writeExportFile(out: ZipOutputStream, result: BackupRestoreResult, file: IFileInfo, rpathx: String) {
        try {
            val e = ZipEntry(rpathx)
            val compressing = BU.isCompressing(rpathx)
            out.setLevel(if (compressing) Deflater.DEFAULT_COMPRESSION else Deflater.NO_COMPRESSION)
            out.putNextEntry(e)
            file.content().getInputStream().use {
                FileUt.copy(out, it)
            }
            out.closeEntry()
            ++result.oks
        } catch (e: Exception) {
            
            result.fails.add(rpathx)
        }
    }

    @Throws(IOException::class)
    fun skipBlocks(input: InputStream): Long {
        var len = 0L
        while (true) {
            when (input.read()) {
                Tag.Block.toInt(), Tag.BlockGz.toInt() -> {
                    len += 1
                    len += U.skipInt32Bytes(input, K.BLOCKSIZE) + 1
                    len += U.skipU8Bytes(input) + 1
                }
                Tag.BlocksEnd.toInt() -> return len + 1
                else -> throw IOException()
            }
        }
    }

    //////////////////////////////////////////////////////////////////////

    //#END UPTO ArchiveFormat.V5

    fun marshalSalt(salt: ByteArray): ByteArray {
        //// NOTE: Must keep input salt intact.
        val len = salt.size
        val ret = ByteArray(len)
        var i = 0
        while (i + 1 < len) {
            ret[i] = salt[i + 1]
            ret[i + 1] = salt[i]
            i += 2
        }
        if (len and 0x01 == 1) {
            ret[len - 1] = salt[len - 1]
        }
        return ret
    }

    fun unmarshalSalt(salt: ByteArray): ByteArray {
        return marshalSalt(salt)
    }

    fun isCompressing(path: String): Boolean {
        val ext = Basepath.lcExt(path)
        return ext != null && K.COMPRESSING.contains(ext)
    }

    @Throws(SecureException::class)
    fun backupData(cout: PositionTrackingOutputStream, param: BackupParam) {
        param.format.writePadding(cout)
        val data = MyByteArrayOutputStream(K.BUFSIZE4).use { bos ->
            GZIPOutputStream(bos).use { gos ->
                for (root in param.roots) {
                    BU.backupData1(gos, cout, param, root)
                }
                U.write(gos, Tag.FilelistEnd.toInt())
            }
            bos
        }
        val digester = param.format.createDigester()
        digester.update(data.buffer(), 0, data.size())
        val checksum = digester.digest()
        val offset = cout.getPosition()
        U.write(cout, Tag.FilelistGz.toInt())
        U.writeInt32Bytes(cout, data.buffer(), data.size())
        U.writeU8Bytes(cout, checksum)
        U.write64BE(cout, offset)
        
    }

    @Throws(IOException::class)
    fun backupData1(filelist: OutputStream, out: PositionTrackingOutputStream, param: BackupParam, srcdir: IFileInfo) {
        if (param.incremental && srcdir.fileInfo(DEF.nobackup).exists) return
        val cpath = srcdir.cpath
        val segments = if (cpath.isEmpty()) listOf() else cpath.split(File.separator)
        for (name in segments) {
            U.write(filelist, Tag.Dir.toInt())
            U.writeUtf8(filelist, name)
            U.write64U(filelist, srcdir.stat()?.lastModified ?: 0L)
        }
        backupData2(filelist, out, param, srcdir, cpath)
        for (i in segments.indices) {
            U.write(filelist, Tag.DirEnd.toInt())
        }
    }

    @Throws(IOException::class)
    private fun backupData2(filelist: OutputStream, out: PositionTrackingOutputStream, param: BackupParam, dir: IFileInfo, cpath: String) {
        for (info in dir.readDir(ArrayList())) {
            val stat = info.stat() ?: continue
            val path = Basepath.joinRpath(cpath, info.name)
            try {
                if (stat.isDir) {
                    if (param.incremental && info.fileInfo(DEF.nobackup).exists) continue
                    U.write(filelist, Tag.Dir.toInt())
                    U.writeUtf8(filelist, info.name)
                    U.write64U(filelist, stat.lastModified)
                    backupData2(filelist, out, param, info, path)
                    U.write(filelist, Tag.DirEnd.toInt())
                } else if (stat.isFile) {
                    U.write(filelist, Tag.File2.toInt())
                    U.writeUtf8(filelist, info.name)
                    U.write64U(filelist, stat.lastModified)
                    U.write64U(filelist, stat.length)
                    val oinfo = param.oinfos.remove(path)
                    if (dobackup(param, path, oinfo, stat)) {
                        val offset = out.getPosition()
                        val checksum = writeBackupFile(out, param, Kind.Data, info)
                        U.writeU8Bytes(filelist, checksum)
                        U.write64U(filelist, offset)
                        ++param.oks
                    } else {
                        U.writeU8Bytes(filelist, oinfo?.checksum ?: Empty.BYTE_ARRAY)
                        U.write64U(filelist, 0)
                        ++param.skipped
                    }
                }
            } catch (e: java.lang.Exception) {
                
                param.fails.add(path)
            }
        }
    }

    private fun dobackup(param: BackupParam, path: String, oinfo: Info?, stat: IFileStat): Boolean {
        if (!param.incremental || oinfo == null) return true
        val checksum = stat.checksumBytes
        return stat.lastModified != oinfo.timestamp
                || stat.length != oinfo.size
                || checksum != null && !checksum.contentEquals(oinfo.checksum ?: Empty.BYTE_ARRAY)
    }

    @Throws(IOException::class)
    private fun writeBackupFile(out: OutputStream, param: BackupParam, kind: Byte, file: IFileInfo): ByteArray {
        return DigestInputStream(file.content().getInputStream(), param.format.createDigester()).use { input ->
            try {
                val compressing = param.enableCompression && isCompressing(file.cpath)
                writeBlocks(out, param, compressing, kind, input)
                input.messageDigest.digest()
            } catch (e: IOException) {
                
                throw e
            }
        }
    }

    @Throws(IOException::class)
    fun writeBlocks(output: OutputStream, param: BackupParam, compressing: Boolean, kind: Byte, content: InputStream) {
        U.write(output, kind.toInt())
        U.write(output, Tag.Blocks.toInt())
        if (compressing) writeGzBlocks(output, param, content) else writeBlocks(output, param, content)
    }

    @Throws(IOException::class)
    private fun writeBlocks(out: OutputStream, param: BackupParam, input: InputStream) {
        val buf = param.tmpbuf
        while (true) {
            val n = ByteIOUtil.readWhilePossible(input, buf)
            if (n > 0) {
                U.write(out, Tag.Block.toInt())
                U.writeInt32Bytes(out, buf, n)
            }
            if (n < buf.size) {
                U.write(out, Tag.BlocksEnd.toInt())
                break
            }
        }
    }

    @Throws(IOException::class)
    private fun writeGzBlocks(out: OutputStream, param: BackupParam, input: InputStream) {
        val buf = param.tmpbuf
        val limit = buf.size.toLong()
        while (true) {
            var count = 0
            val gzbuf = LimitedOutputStream(buf).use { bos ->
                GZIPOutputStream(bos).use { gos ->
                    LimitedInputStream(input, limit).use { input ->
                        while (true) {
                            val n = input.read(param.copybuf)
                            if (n < 0) break
                            gos.write(param.copybuf, 0, n)
                            count += n
                        }
                    }
                }
                bos
            }
            if (count > 0) {
                U.write(out, Tag.BlockGz.toInt())
                U.writeInt32Bytes(out, gzbuf.buffer, gzbuf.count)
            }
            if (count < limit) {
                U.write(out, Tag.BlocksEnd.toInt())
                break
            }
        }
    }

    fun readtag(res: IResUtil, input: InputStream): Byte {
        val tag = input.read()
        if (tag < 0) {
            throw res.secureException(R.string.ErrorReadingBackup)
        }
        return tag.toByte()
    }

    fun hasArchiveMagic(data: ByteArray): Boolean {
        return ByteIOUtil.equals(ArchiveFormat.MAGIC, data, 0, ArchiveFormat.MAGIC.size)
    }

    /// @return Set of all the .ibackup file names in the given directory in filename order.
    fun getIBackupFilenames(filename: String?, dir: IFileInfo): List<String> {
        val ret = ArrayList<String>()
        if (filename != null) ret.add(filename)
        for (file in FileInfoUtil.listFiles(dir)) {
            if (file.stat()?.isFile == true) {
                val name = file.name
                if (name.endsWith(DEF.ibackup)) {
                    ret.add(name)
                }
            }
        }
        Collections.sort(ret)
        return ret
    }

    /**
     * Remove any extra directories or files. Note that this must be performed before the actual restore since the file entry may
     * have changed type.
     */
    @Throws(Exception::class)
    private fun setupRestoreDir(restoredir: IFileInfo?, infos: Map<String, Info>?) {
        if (restoredir == null || infos == null) {
            return
        }
        FileInfoUtil.scan3(restoredir, "") { file, rpath, stat ->
            val info = infos[rpath]
            if (stat.isDir) {
                if (info == null || info.size >= 0) {
                    FileInfoUtil.deleteTree(file)
                    return@scan3 false
                }
                return@scan3 true
            }
            if (stat.isFile) {
                if (info == null || info.size < 0) {
                    file.delete()
                }
            }
            return@scan3 false
        }
    }

    /// Restore timestamps and empty directories.
    /// @throws Exception
    @Throws(Exception::class)
    public fun syncRestoreDir(res: IResUtil, restoredir: IFileInfo?, infos: Map<String, Info>?) {
        if (restoredir == null || infos == null) {
            return
        }
        FileInfoUtil.scan3(restoredir, "") { file, rpath, stat ->
            val info = infos[rpath]
            if (stat.isDir) {
                if (info == null || info.size >= 0) {
                    FileInfoUtil.deleteTree(file)
                    return@scan3 false
                } else {
                    file.setLastModified(info.timestamp)
                }
                return@scan3 true
            }
            if (stat.isFile) {
                if (info == null || info.size < 0) {
                    file.delete()
                } else {
                    file.setLastModified(info.timestamp)
                }
            }
            return@scan3 false
        }
        for ((rpath, info) in infos) {
            if (info.size >= 0) {
                continue
            }
            val rpathx = Support.getcleanrpathStrict(res, rpath).first
            if (rpathx != null) {
                val dir = restoredir.fileInfo(rpathx)
                if (!dir.exists) {
                    dir.mkdirs()
                    dir.setLastModified(info.timestamp)
                }
            }
        }
    }

    @Throws(IOException::class)
    fun readBlocks(res: IResUtil, tmpbuf: ByteArray, input: InputStream, callback: Fun21<ByteArray, Int, Boolean>) {
        if (readtag(res, input) != Tag.Blocks) throw SecureException()
        while (true) {
            when (val tag = readtag(res, input)) {
                Tag.Block, Tag.BlockGz -> {
                    val len = U.read32BE(input)
                    if (len > tmpbuf.size) {
                        throw IOException()
                    }
                    if (tag == Tag.Block) {
                        IOUt.readFully(input, tmpbuf, 0, len)
                        if (callback(tmpbuf, len)) return
                    } else if (tag == Tag.BlockGz) {
                        if (U.ungzip(input, tmpbuf, len, callback)) return
                    } else {
                        throw IOException()
                    }
                }
                Tag.BlocksEnd -> return
                else -> throw IOException()
            }
        }
    }

    @Throws(IOException::class)
    fun copyBlocks(res: IResUtil, outfile: IFileInfo, param: RestoreParam, input: InputStream): ByteArray {
        return DigestOutputStream(outfile.content().getOutputStream(), param.format.createDigester()).use { output ->
            readBlocks(res, param.tmpbuf, input) { buf, count ->
                output.write(buf, 0, count)
                false
            }
            output.messageDigest.digest()
        }
    }

    fun getLastBackupFile(dir: IFileInfo, filename: String?): IFileInfo? {
        val names = getIBackupFilenames(filename, dir)
        var name: String? = null
        for (s in names) {
            if (s == filename) {
                break
            }
            name = s
        }
        if (name == null) {
            return null
        }
        return dir.fileInfo(name)
    }
}

private
object U {
    private object UU {
        val srand = SecureRandom.getInstanceStrong()
        val SHA1 = MessageDigest.getInstance("SHA1")
        val SHA256 = MessageDigest.getInstance("SHA-256")
    }

    val lock = ReentrantLock()

    private fun <T> invoke(code: Fun11<UU, T>): T {
        lock.withLock {
            return code(UU)
        }
    }

    fun sha1(data: ByteArray, off: Int = 0, len: Int = data.size): ByteArray {
        return invoke {
            it.SHA1.update(data, off, len)
            it.SHA1.digest()
        }
    }

    fun sha256(data: ByteArray, off: Int = 0, len: Int = data.size): ByteArray {
        return invoke {
            it.SHA256.update(data, off, len)
            it.SHA256.digest()
        }
    }

    fun sha1HexString(data: ByteArray, off: Int = 0, len: Int = data.size): String {
        return Hex.encode(sha1(data, off, len), true).toString()
    }

    fun sha256HexString(data: ByteArray, off: Int = 0, len: Int = data.size): String {
        return Hex.encode(sha256(data, off, len), true).toString()
    }

    private fun randInt(uu: UU, min: Int, max: Int): Int {
        val range = max - min
        val r = uu.srand.nextInt(range)
        return min + r
    }

    fun randInt(min: Int, max: Int): Int {
        return invoke { randInt(it, min, max) }
    }

    fun randInts(min: Int, max: Int, len: Int): IntArray {
        return invoke { uu ->
            IntArray(len) { randInt(uu, min, max) }
        }
    }

    fun randBytes(ret: ByteArray): ByteArray {
        return invoke {
            it.srand.nextBytes(ret)
            ret
        }
    }

    fun randBytes(len: Int): ByteArray {
        return randBytes(ByteArray(len))
    }

    fun randBytes(min: Int, max: Int): ByteArray {
        return randBytes(randInt(min, max))
    }

    fun randU8(): Int {
        return randInt(0, 256)
    }

    /**
     * @param bits Key length in bits.
     * @return A random generated salt.
     */
    fun generateSalt(bits: Int): ByteArray {
        val len = bits / 8
        val salt = ByteArray(len)
        while (true) {
            randBytes(salt)
            if (isValidSalt(salt)) {
                break
            }
        }
        return salt
    }

    fun generateAESKey(bits: Int): SecretKey {
        return SecretKeySpec(generateSalt(bits), K.AES)
    }

    fun <T> readByteArray(data: ByteArray, content: Fun11<InputStream, T>): T {
        return data.inputStream().use { content(it) }
    }

    fun writeByteArray(content: Fun10<OutputStream>): MyByteArrayOutputStream {
        return MyByteArrayOutputStream().use {
            content(it)
            it
        }
    }

    @Throws(IOException::class)
    fun ungzip(input: InputStream, buf: ByteArray, length: Int, collector: Fun21<ByteArray, Int, Boolean>): Boolean {
        LimitedInputStream(input, length.toLong()).use {
            GZIPInputStream(it).use { gis ->
                while (true) {
                    val n = gis.read(buf, 0, buf.size)
                    if (n > 0) {
                        if (collector(buf, n)) return true
                    } else if (n < 0) break
                }
            }
        }
        return false
    }

    @Throws(IOException::class)
    fun readInt32Bytes(input: InputStream, buf: ByteArray): Int {
        val len = ByteIOUtil.read32BE(input)
        if (len < 0 || len > buf.size) {
            
            throw BeyondLimitException()
        }
        IOUt.readFully(input, buf, 0, len)
        return len
    }

    @Throws(IOException::class)
    fun readInt32Bytes(input: InputStream, limit: Int): ByteArray {
        val len = readInt32Length(input, limit)
        return IOUt.readFully(input, ByteArray(len))
    }

    fun readInt32Length(input: InputStream, limit: Int): Int {
        val len = ByteIOUtil.read32BE(input)
        if (len < 0 || len > limit) {
            
            throw BeyondLimitException()
        }
        return len
    }

    @Throws(IOException::class)
    fun skipInt32Bytes(input: InputStream, limit: Int): Int {
        val len = ByteIOUtil.read32BE(input)
        if (len < 0 || len > limit) {
            
            throw BeyondLimitException()
        }
        //#BEGIN FIXME Should use IOUt.skipFully(), but CipherInputStream.skip() don't seems to refill if not enough data to skip.
        ByteIOUtil.skipFully(input, len.toLong())
        return len
    }

    @Throws(IOException::class)
    fun skipU8Bytes(input: InputStream): Int {
        val len = U.readU8(input)
        //#BEGIN FIXME Should use IOUt.skipFully(), but CipherInputStream.skip() don't seems to refill if not enough data to skip.
        ByteIOUtil.skipFully(input, len.toLong())
        return len
    }

    @Throws(IOException::class)
    fun readU8Bytes(input: InputStream): ByteArray {
        val len = U.readU8(input)
        return IOUt.readFully(input, ByteArray(len))
    }

    @Throws(IOException::class)
    fun ungzip(compressed: ByteArray): ByteArray {
        ByteArrayInputStream(compressed).use { bis ->
            GZIPInputStream(bis).use { gis ->
                return gis.readBytes()
            }
        }
    }

    /**
     * @param limit Sanity check for reasonably length.
     */
    @Throws(IOException::class)
    fun readUtf8(input: InputStream, limit: Int): String {
        val bytes = readInt32Bytes(input, limit)
        return bytes.toString(Charsets.UTF_8)
    }

    @Throws(IOException::class)
    fun read64BE(input: InputStream, digest: MessageDigest, buf: ByteArray): Long {
        IOUt.readFully(input, buf)
        digest.update(buf)
        return ByteIOUtil.read64BE(buf, 0)
    }

    @Throws(IOException::class)
    fun readUtf8(input: InputStream, digest: MessageDigest, limit: Int): String {
        val bytes = readInt32Bytes(input, limit)
        digest.update(bytes)
        return bytes.toString(Charsets.UTF_8)
    }

    fun read32BE(input: InputStream): Int {
        return ByteIOUtil.read32BE(input)
    }

    fun readByte(input: InputStream): Byte {
        return ByteIOUtil.read8(input)
    }

    fun readU8(input: InputStream): Int {
        return ByteIOUtil.readU8(input)
    }

    @Throws(IOException::class)
    fun writeInt64Bytes(output: OutputStream, data: ByteArray) {
        ByteIOUtil.write64BE(output, data.size.toLong())
        output.write(data)
    }

    @Throws(IOException::class)
    fun writeInt32Bytes(output: OutputStream, data: ByteArray) {
        ByteIOUtil.write32BE(output, data.size)
        output.write(data)
    }

    @Throws(IOException::class)
    fun writeInt32Bytes(output: OutputStream, data: ByteArray, length: Int) {
        ByteIOUtil.write32BE(output, length)
        output.write(data, 0, length)
    }

    @Throws(IOException::class)
    fun writeU8Bytes(output: OutputStream, data: ByteArray) {
        if (data.size > 255) throw IOException()
        output.write(data.size)
        output.write(data)
    }

    @Throws(IOException::class)
    fun writeU8Bytes(output: OutputStream, u8: Int, data: ByteArray) {
        if (u8 > 255 || data.size > 255) throw IOException()
        output.write(u8)
        output.write(data)
    }

    @Throws(IOException::class)
    fun writeUtf8(out: OutputStream, s: String) {
        writeInt32Bytes(out, s.toByteArray())
    }

    fun write64U(out: OutputStream, value: Long) {
        if (value < 0) throw SecureException()
        var v = value
        while (true) {
            val lsb = (v and 0x7f).toInt()
            v = (v ushr 7)
            if (v == 0L) {
                out.write(lsb or 0x80)
                return
            }
            out.write(lsb)
        }
    }

    fun read64U(input: InputStream): Long {
        var ret = 0L
        var shift = 0
        while (true) {
            val v = input.read()
            if (v < 0) throw SecureException()
            ret += ((v and 0x7f).toLong() shl shift)
            if ((v and 0x80) == 0x80) break
            shift += 7
        }
        return ret
    }

    fun write64BE(out: OutputStream, value: Long) {
        ByteIOUtil.write64BE(out, value)
    }

    fun write32BE(out: OutputStream, value: Int) {
        ByteIOUtil.write32BE(out, value)
    }

    fun write16BE(out: OutputStream, value: Int) {
        ByteIOUtil.write16BE(out, value)
    }

    fun writeU8(out: OutputStream, value: Int) {
        out.write(value)
    }

    fun write(out: OutputStream, value: Int) {
        out.write(value)
    }

    fun write(out: OutputStream, value: ByteArray) {
        out.write(value, 0, value.size)
    }

    fun write(out: OutputStream, a: ByteArray, offset: Int, length: Int) {
        out.write(a, offset, length)
    }

    //// A sanity check, caller must has perform more vigorous checks.
    fun isValidLoginPass(pass: String?): Boolean {
        return pass != null && pass.length >= 4 && isvalid(pass)
    }

    fun isvalid(pass: String): Boolean {
        var i = 0
        val len = pass.length
        while (i < len) {
            val c = pass[i]
            if (c.toInt() <= 0x20 || c.toInt() >= 0x7f) {
                return false
            }
            ++i
        }
        return true
    }

    fun isValidKeyLength(keylength: Int): Boolean {
        return keylength == Best.AESKeyLength
    }

    fun isValidKeyLength(key: ByteArray): Boolean {
        return isValidKeyLength(key.size * 8)
    }

    fun isValidSalt(salt: ByteArray): Boolean {
        val len = salt.size
        if (len < K.MIN_SALT_LENGTH) {
            return false
        }
        val counts = IntArray(256)
        for (i in 0 until len) {
            counts[salt[i].toInt() and 0xff] += 1
        }
        for (i in 0..255) {
            if (counts[i] == len) {
                return false
            }
        }
        return true
    }

    fun random(count: Int): ByteArray {
        val ret = ByteArray(count)
        SecureRandom.getInstanceStrong().nextBytes(ret)
        return ret
    }

}

private object K {
    const val AES = "AES"
    const val SHA256 = "SHA-256"
    const val BUFSIZE = 16 * 1024
    const val BUFSIZE4 = BUFSIZE * 4
    const val BLOCKSIZE = 4 * 1024 * 1024
    const val MIN_SALT_LENGTH = 16
    const val PADDING_MIN = 64
    const val PADDING_LENGTH_MASK = 0x3f // max length min+64 bytes
    const val SHA1_BUFSIZE = 20
    const val SHA256_BUFSIZE = 32
    const val FILEPATH_BUFSIZE = 32 * 1024
    const val ENCRYPTED_BLOCK_SIZE_MULTIPIER = 4 * 1024
    const val SEEKABLE_POOL_SIZE = 3
    val COMPRESSING = setOf("html", "css", "js", "svg", "pdf", "xml", "cf", "json", "properties")
    const val loginCf = "login.cf"
    const val RESTORE_TMP_FILE = ".restore.tmp"
}

interface IPadding {
    fun padding(): ByteArray

    @Throws(Exception::class)
    fun readPadding(input: InputStream): Int

    @Throws(Exception::class)
    fun writePadding(output: OutputStream): Int

    fun readPadding(input: InputStream, len: Int): Int

    fun writePadding(output: OutputStream, len: Int): Int
}

open class V10Padding : IPadding {
    private val buf = ByteArray(256)
    private val sumBuf = ByteArray(K.SHA1_BUFSIZE)

    @Throws(Exception::class)
    override fun writePadding(output: OutputStream): Int {
        val u8 = U.randU8()
        val len = lengthFrom(u8)
        output.write(u8)
        return writePadding(output, len) + 1
    }

    @Throws(Exception::class)
    override fun writePadding(output: OutputStream, len: Int): Int {
        val bytes = U.randBytes(len)
        val checksum = U.sha1(bytes, 0, bytes.size)
        output.write(bytes)
        output.write(checksum)
        return len + checksum.size
    }

    @Throws(Exception::class)
    override fun readPadding(input: InputStream): Int {
        val len = readLength(input)
        return readPadding(input, len) + 1
    }

    @Throws(Exception::class)
    override fun readPadding(input: InputStream, len: Int): Int {
        IOUt.readFully(input, buf, 0, len)
        val actual = U.sha1(buf, 0, len)
        IOUt.readFully(input, sumBuf)
        if (!actual.contentEquals(sumBuf)) {
            throw IOException()
        }
        return len + sumBuf.size
    }

    override fun padding(): ByteArray {
        return U.randBytes(PADDING_MIN, PADDING_MAX)
    }

    private fun readLength(input: InputStream): Int {
        val u8 = U.readU8(input)
        val len = lengthFrom(u8)
        if (len < PADDING_MIN || len > PADDING_MAX) {
            throw IOException()
        }
        return len
    }

    private fun lengthFrom(u8: Int): Int {
        return PADDING_MIN + (u8 and PADDING_MASK)
    }

    companion object {
        const val PADDING_MIN = 16
        const val PADDING_MAX = 32
        const val PADDING_MASK = 0xf
    }
}

//////////////////////////////////////////////////////////////////////

interface IFileFormat : IPadding {
    val IV: Int
    val SUM: Int
    val PADDING: Int
    val PADSIZE: Int
    val BLOCKSIZE: Int
    fun version(): Int
    fun createDigester(): MessageDigest
    fun createCipher(key: SecretKey): Cipher
    fun createCipherOutputStream(out: OutputStream, cipher: Cipher): OutputStream
    fun writeCipher(output: OutputStream, cipher: Cipher)
    fun readCipher(input: InputStream, key: SecretKey): Cipher
    fun initCipher(cipher: Cipher, key: SecretKey, iv: ByteArray)
    fun checksumBlock(out: OutputStream): Int
    fun verifyBlock(buffer: ByteArray, length: Int): Int
    @Throws(IOException::class)
    fun openCipherOutputStream(out: OutputStream, key: SecretKey): OutputStream
    @Throws(IOException::class, SecureException::class)
    fun openCipherInputStream(seekable: ISeekableInputStream, key: SecretKey): Pair<ISeekableInputStream, IFileFormat>

    fun encodeBlocksize(): Int {
        return BLOCKSIZE / K.ENCRYPTED_BLOCK_SIZE_MULTIPIER
    }

    fun decodeBlocksize(encoded: Int): Int {
        return encoded * K.ENCRYPTED_BLOCK_SIZE_MULTIPIER
    }
}

/**
 * The IFileFormat factory.
 */
private object FileFormat {
    val MAGIC = byteArrayOf(0x61, 0x30, 0x63, 0x65) // a0ce

    const val V10 = 10
    const val V11 = 11

    fun latestVersion(): Int {
        return V11
    }

    fun latestFormat(): IFileFormat {
        return get(latestVersion()) ?: throw AssertionError()
    }

    fun isSupportedVersion(version: Int): Boolean {
        return when (version) {
            V10 -> true
            V11 -> true
            else -> false
        }
    }

    /**
     * @return The supported format or null.
     */
    operator fun get(version: Int): IFileFormat? {
        return when (version) {
            V10 -> FileFormatV10()
            V11 -> FileFormatV11()
            else -> null
        }
    }

    @Throws(IOException::class)
    fun openCipherInputStream(seekable: SeekableFileInputStream, key: SecretKey): Pair<ISeekableInputStream, IFileFormat> {
        val magic = IOUt.readFully(seekable, ByteArray(FileFormat.MAGIC.size))
        ByteIOUtil.equals(magic, FileFormat.MAGIC) || throw SecureException()
        val version = U.readU8(seekable)
        val format = FileFormat.get(version) ?: throw SecureException()
        return format.openCipherInputStream(seekable, key)
    }
}

private class FileFormatV10 : V10Padding(), IFileFormat {

    override val IV = 16
    override val SUM = 32
    override val PADDING = 16
    override val PADSIZE = 16
    override val BLOCKSIZE = 4 * K.ENCRYPTED_BLOCK_SIZE_MULTIPIER

    override fun version(): Int {
        return FileFormat.V10
    }

    override fun createDigester(): MessageDigest {
        return MessageDigest.getInstance("SHA-256")
    }

    override fun createCipher(key: SecretKey): Cipher {
        val cipher = Cipher.getInstance(Best.CBC)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher
    }

    override fun createCipherOutputStream(out: OutputStream, cipher: Cipher): OutputStream {
        val ret = MyDigestedOutputStream(CipherOutputStream(out, cipher), createDigester())
        ret.write(U.randBytes(PADDING))
        return ret
    }

    override fun writeCipher(output: OutputStream, cipher: Cipher) {
        U.write(output, Tag.Cipher.toInt()) // 1
        U.writeU8Bytes(output, byteArrayOf(0)) // 1, keyid
        U.writeU8Bytes(output, BU.marshalSalt(cipher.iv)) // 1+16
    }

    override fun readCipher(input: InputStream, key: SecretKey): Cipher {
        if (input.read() != Tag.Cipher.toInt()) throw SecureException()
        U.readU8Bytes(input) // keyid
        val iv = BU.unmarshalSalt(U.readU8Bytes(input)) // iv
        if (iv.size != IV) throw SecureException()
        val cipher = Cipher.getInstance(Best.CBC)
        initCipher(cipher, key, iv)
        return cipher
    }

    override fun initCipher(cipher: Cipher, key: SecretKey, iv: ByteArray) {
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
    }

    override fun checksumBlock(out: OutputStream): Int {
        out.flush()
        val digest = (out as MyDigestedOutputStream).messageDigest.digest()
        U.write(out, digest)
        return digest.size
    }

    override fun verifyBlock(buffer: ByteArray, length: Int): Int {
        val digester = createDigester()
        digester.update(buffer, 0, length - SUM)
        val actual = digester.digest()
        val expected = buffer.copyOfRange(length - SUM, length)
        if (!actual.contentEquals(expected))
            throw SecureException()
        return length - PADDING - SUM
    }

    @Throws(IOException::class)
    override fun openCipherOutputStream(out: OutputStream, key: SecretKey): OutputStream {
        return EncryptedOutputStream(out, key, this)
    }

    override fun openCipherInputStream(seekable: ISeekableInputStream, key: SecretKey): Pair<ISeekableInputStream, IFileFormat> {
        return Pair(BU.openEncryptedInputStream(seekable, key, this), this)
    }

    private class MyDigestedOutputStream(output: OutputStream, digester: MessageDigest) : DigestOutputStream(output, digester) {
        private var position = 0L
        fun getPosition(): Long {
            return position
        }

        override fun write(b: ByteArray) {
            super.write(b)
            ++position
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            val before = position
            super.write(b, off, len)
            position = before + len
        }
    }
}

private class FileFormatV11 : V10Padding(), IFileFormat {
    override val IV = 12
    override val SUM = 16
    override val PADDING = 8
    override val PADSIZE = 0
    override val BLOCKSIZE = 4 * K.ENCRYPTED_BLOCK_SIZE_MULTIPIER

    override fun version(): Int {
        return FileFormat.V11
    }

    override fun createDigester(): MessageDigest {
        return MessageDigest.getInstance("SHA-256")
    }

    override fun createCipher(key: SecretKey): Cipher {
        val cipher = Cipher.getInstance(Best.GCM)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher
    }

    override fun createCipherOutputStream(out: OutputStream, cipher: Cipher): OutputStream {
        val ret = CipherOutputStream(out, cipher)
        ret.write(U.randBytes(PADDING))
        return ret
    }

    override fun writeCipher(output: OutputStream, cipher: Cipher) {
        val param = cipher.parameters.getParameterSpec(GCMParameterSpec::class.java)
        U.write(output, Tag.Cipher.toInt()) // 1
        U.writeU8Bytes(output, byteArrayOf(0)) // 1, keyid
        U.writeU8(output, param.tLen) // 1, tlen
        U.writeU8Bytes(output, BU.marshalSalt(param.iv)) // 1+12
    }

    override fun readCipher(input: InputStream, key: SecretKey): Cipher {
        if (input.read() != Tag.Cipher.toInt()) throw SecureException()
        U.readU8Bytes(input) // keyid
        val tlen = U.readU8(input) // tlen
        val iv = BU.unmarshalSalt(U.readU8Bytes(input)) // iv
        if (iv.size != IV) throw SecureException()
        val cipher = Cipher.getInstance(Best.GCM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(tlen, iv))
        return cipher
    }

    override fun initCipher(cipher: Cipher, key: SecretKey, iv: ByteArray) {
        if (iv.size != IV) throw SecureException()
        val param = cipher.parameters.getParameterSpec(GCMParameterSpec::class.java)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(param.tLen, iv))
    }

    override fun checksumBlock(out: OutputStream): Int {
        return 0
    }

    override fun verifyBlock(buffer: ByteArray, length: Int): Int {
        return length - PADDING
    }

    @Throws(IOException::class)
    override fun openCipherOutputStream(out: OutputStream, key: SecretKey): OutputStream {
        return EncryptedOutputStream(out, key, this)
    }

    override fun openCipherInputStream(seekable: ISeekableInputStream, key: SecretKey): Pair<ISeekableInputStream, IFileFormat> {
        return Pair(BU.openEncryptedInputStream(seekable, key, this), this)
    }
}

internal
interface IArchiveFormat : IPadding {
    fun version(): Int
    fun iterations(): Int
    fun fileFormat(): IFileFormat
    fun createDigester(): MessageDigest
    fun createCipher(key: SecretKey): Cipher
    fun readCipher(input: InputStream): Pair<SecretKey, Cipher>
    fun writeCipher(output: OutputStream, key: SecretKey, cipher: Cipher)

    @Throws(SecureException::class)
    fun readFilelist(rsrc: IResUtil, input: ISeekableInputStream): MutableMap<String, Info>

    @Throws(SecureException::class)
    fun readFiletree(rsrc: IResUtil, input: ISeekableInputStream): JSONObject

    fun skipFilelist(input: ISeekableInputStream, limit: Int)
}

/**
 * The ArchveFormat factory.
 */
private object ArchiveFormat {

    const val V10 = 10
    const val V11 = 11

    val MAGIC = byteArrayOf(
            0x6e, 0x6f, 0x63, 0x61,  // noca
            0x01, 0x02, 0xab.toByte(), 0xac.toByte())

    fun latestBackupVersion(): Int {
        return V11
    }

    fun latestBackupFormat(): IArchiveFormat {
        return get(latestBackupVersion()) ?: throw AssertionError()
    }

    fun isSupportedVersion(version: Int): Boolean {
        return when (version) {
            V10 -> true
            V11 -> true
            else -> false
        }
    }

    /**
     * @return The supported format or null.
     */
    operator fun get(version: Int): IArchiveFormat? {
        return when (version) {
            V10 -> ArchiveFormatV10.singleton
            V11 -> ArchiveFormatV11.singleton
            else -> null
        }
    }

    object Kind {
        const val Invalid: Byte = 0
        const val Data: Byte = 1
        const val Etc: Byte = 2
    }
}

/**
 * Tag is a byte value from 0..255. Using int type to reduce casting only.
 */
private object Tag {
    const val Invalid: Byte = 0
    const val Cipher: Byte = 1

    //#BEGIN
    //#NOTE File and FileGz sections are obsoleted since Android v1.6 and
    //# not supported in any iOS versions.
    const val File: Byte = 2
    const val FileGz: Byte = 3

    //#END
    const val Blocks: Byte = 4
    const val Block: Byte = 5
    const val BlockGz: Byte = 6
    const val BlocksEnd: Byte = 7
    const val End: Byte = 8

    //#BEGIN SINCE v1.6.
    const val FilelistGz: Byte = 9
    const val FilelistEnd: Byte = 10
    const val Dir: Byte = 11
    const val DirEnd: Byte = 12

    //#END SINCE v1.6
    //#BEGIN SINCE v1.7
    const val DataGz: Byte = 13
    const val DataEnd: Byte = 14

    //#END SINCE v1.7
    //#BEGIN SINCE v1.8
    const val Data: Byte = 15 // public static final byte Filelist = 16; // For testing only.

    //#END SINCE v1.8
    //#BEGIN SINCE v2.16.0
    const val File2: Byte = 16 // With file checksum.
    //#END SINCE 2.16.0
}

private abstract class ArchiveFormatBase : IArchiveFormat {
    protected abstract fun _padding(): IPadding
    override fun iterations(): Int {
        return ITERATIONS
    }

    @Throws(Exception::class)
    override fun readPadding(input: InputStream): Int {
        return _padding().readPadding(input)
    }

    @Throws(Exception::class)
    override fun writePadding(output: OutputStream): Int {
        return _padding().writePadding(output)
    }

    @Throws(Exception::class)
    override fun readPadding(input: InputStream, len: Int): Int {
        return _padding().readPadding(input, len)
    }

    @Throws(Exception::class)
    override fun writePadding(output: OutputStream, len: Int): Int {
        return _padding().writePadding(output, len)
    }

    override fun padding(): ByteArray {
        return _padding().padding()
    }

    companion object {
        private const val ITERATIONS = 1024
    }
}

//////////////////////////////////////////////////////////////////////

private class ArchiveFormatV11 : ArchiveFormatV10() {
    companion object {
        var singleton = ArchiveFormatV11()
    }

    override fun version(): Int {
        return ArchiveFormat.V11
    }

    override fun fileFormat(): IFileFormat {
        return FileFormat.get(FileFormat.V11)!!
    }
}

private open class ArchiveFormatV10 : ArchiveFormatBase() {
    private val padding = V10Padding()

    override fun version(): Int {
        return ArchiveFormat.V10
    }

    override fun fileFormat(): IFileFormat {
        return FileFormat.get(FileFormat.V10)!!
    }

    override fun createCipher(key: SecretKey): Cipher {
        return fileFormat().createCipher(key)
    }

    override fun readCipher(input: InputStream): Pair<SecretKey, Cipher> {
        val key = SecretKeySpec(U.readU8Bytes(input), "AES") // key
        val cipher = fileFormat().readCipher(input, key)
        return Pair(key, cipher)
    }

    override fun writeCipher(output: OutputStream, key: SecretKey, cipher: Cipher) {
        U.writeU8Bytes(output, key.encoded)
        fileFormat().writeCipher(output, cipher)
    }

    override fun createDigester(): MessageDigest {
        return MessageDigest.getInstance("SHA-256")
    }

    override fun iterations(): Int {
        return super.iterations() * X
    }

    override fun _padding(): IPadding {
        return padding
    }

    @Throws(SecureException::class)
    override fun readFilelist(rsrc: IResUtil, input: ISeekableInputStream): MutableMap<String, Info> {
        try {
            return UV10.readV10Filelist(readGzFilelist(input))
        } catch (e: Throwable) {
            throw rsrc.secureException(e, R.string.ErrorReadingBackup)
        }
    }

    @Throws(SecureException::class)
    override fun readFiletree(rsrc: IResUtil, input: ISeekableInputStream): JSONObject {
        try {
            return UV10.readV10Filetree(readGzFilelist(input))
        } catch (e: Throwable) {
            throw rsrc.secureException(e, R.string.ErrorReadingBackup)
        }
    }

    override fun skipFilelist(input: ISeekableInputStream, limit: Int) {
        U.skipInt32Bytes(input, limit)
        U.skipU8Bytes(input)
    }

    private fun readGzFilelist(input: ISeekableInputStream): ByteArray {
        val size = input.getSize()
        val buf = ByteArray(8)
        if (input.readAt(size - 8, buf, 0, 8) != 8) throw SecureException()
        val offset = ByteReader(buf).i64BE()
        
        if (offset <= 0 || offset >= size) throw SecureException()
        input.setPosition(offset)
        return BU.readGzFilelist(input, (size - offset - 8).toInt(), createDigester())
    }

    companion object {
        private const val X = 9
        var singleton = ArchiveFormatV10()
    }

    private object UV10 {

        @Throws(IOException::class)
        fun readV10Filelist(data: ByteArray): MutableMap<String, Info> {
            val ret = TreeMap<String, Info>()
            ret.put("", Info(0, -1))
            readV10Filelist(data, object : FilelistCallback {
                override fun dir(rpath: List<String>, info: Info) {
                    ret.put(rpath.join(File.separator), info)
                }

                override fun dirend(rpath: List<String>) {
                }

                override fun file(rpath: List<String>, info: Info) {
                    ret.put(rpath.join(File.separator), info)
                }
            })
            return ret
        }

        @Throws(IOException::class)
        fun readV10Filetree(data: ByteArray): JSONObject {
            val dirstack = Stack<JSONObject>()
            val ret = Info(0, -1).toJSON("")
            var files = ret.getJSONObject(IFileInfo.Key.files)!!
            var dir = ret
            readV10Filelist(data, object : FilelistCallback {
                override fun dir(rpath: List<String>, info: Info) {
                    dirstack.push(dir)
                    val d = info.toJSON(rpath.last())
                    files.put(rpath.last(), d)
                    dir = d
                    files = d.getJSONObject(IFileInfo.Key.files)!!
                }

                override fun dirend(rpath: List<String>) {
                    dir = dirstack.pop()
                    files = dir.getJSONObject(IFileInfo.Key.files)!!
                }

                override fun file(rpath: List<String>, info: Info) {
                    files.put(rpath.last(), info.toJSON(rpath.last()))
                }
            })
            return ret
        }

        @Throws(IOException::class)
        fun readV10Filelist(data: ByteArray, callback: FilelistCallback) {
            val input: InputStream = ByteArrayInputStream(data)
            val rpath = LinkedList<String>()
            while (true) {
                val tag = U.readByte(input)
                when (tag) {
                    Tag.Dir -> {
                        val name = U.readUtf8(input, K.FILEPATH_BUFSIZE)
                        val timestamp = U.read64U(input)
                        rpath.add(name)
                        callback.dir(rpath, Info(timestamp, -1))
                    }
                    Tag.DirEnd -> {
                        callback.dirend(rpath)
                        rpath.removeLast()
                    }
                    Tag.File2 -> {
                        val name = U.readUtf8(input, K.FILEPATH_BUFSIZE)
                        val timestamp = U.read64U(input)
                        val size = U.read64U(input)
                        val checksum = U.readU8Bytes(input)
                        val offset = U.read64U(input)
                        rpath.add(name)
                        callback.file(rpath, Info(timestamp, size, offset, if (checksum.isEmpty()) null else checksum))
                        rpath.removeLast()
                    }
                    Tag.FilelistEnd -> {
                        return
                    }
                    else -> throw IOException()
                }
            }
        }
    }
}

private interface FilelistCallback {
    fun dir(rpath: List<String>, info: Info)
    fun dirend(rpath: List<String>)
    fun file(rpath: List<String>, info: Info)
}

//////////////////////////////////////////////////////////////////////

class Info(val timestamp: Long, val size: Long, val offset: Long = 0L, val checksum: ByteArray? = null) {
    val isDir: Boolean get() = size < 0
    val isFile: Boolean get() = size >= 0
    fun toJSON(name: String): JSONObject {
        val ret = JSONObject().put(IFileInfo.Key.name, name).put(IFileInfo.Key.lastModified, timestamp)
        if (isDir) ret.put(IFileInfo.Key.isdir, isDir).put(IFileInfo.Key.files, JSONObject())
        else ret.put(IFileInfo.Key.length, size)
        return ret
    }
}

object KeyUtil {
    fun createAESKey(): SecretKey {
        val blob = ByteArray(32)
        SecureRandom.getInstanceStrong().nextBytes(blob)
        return SecretKeySpec(blob, "AES")
    }

    /**
     * Export the public key certificate for creating backup in binary DER format.
     * @return null if alias not exists, false if export failed, true if OK.
     */
    @Throws
    fun exportPublicCert(outfile: IFileInfo, cert: Certificate): Boolean {
        try {
            outfile.delete()
            cert.encoded.inputStream().use { outfile.content().write(it) }
            return true
        } catch (e: Throwable) {
            return false
        }
    }

    /**
     * Export the public key certificate for creating backup in binary DER format.
     * @return null if alias not exists, false if export failed, true if OK.
     */
    @Throws
    fun exportPublicKey(outfile: IFileInfo, cert: Certificate): Boolean {
        try {
            outfile.delete()
            cert.publicKey.encoded.inputStream().use { outfile.content().write(it) }
            return true
        } catch (e: Throwable) {
            return false
        }
    }

    /**
     * Export a private key in binary DER format.
     * @return null if alias not exists, false if export failed, true if OK.
     */
    @Throws
    fun exportPrivateKey(outfile: IFileInfo, key: PrivateKey): Boolean {
        try {
            outfile.delete()
            key.encoded.inputStream().use { outfile.content().write(it) }
            return true
        } catch (e: Throwable) {
            return false
        }
    }
}

class BeyondLimitException : IOException() {
}

class EncryptedFileInfo @JvmOverloads constructor(
        override val root: EncryptedFileRootInfo,
        override val rpath: String,
        file: File = File(root.file, rpath).absoluteFile
) : FileInfoBase(file) {

    private val fileContent = EncryptedFileContent(this, f)

    //////////////////////////////////////////////////////////////////////

    override val name: String get() = f.name
    override val cpath = Basepath.joinRpath(root.cpath, rpath)
    override val apath = Basepath.joinPath(root.apath, rpath)

    override val parent: IFileInfo?
        get() = if (rpath.isEmpty()) null else EncryptedFileInfo(root, Basepath.dir(rpath) ?: "")

    override fun content(): IFileContent {
        return fileContent
    }

    override val length: Long
        get() {
            return if (file.isDirectory) file.length() else fileContent.getSeekableInputStream().use { it.getSize() }
        }

    override fun setLastModified(timestamp: Long): Boolean {
        return f.setLastModified(timestamp)
    }

    override fun setWritable(writable: Boolean): Boolean {
        return f.setWritable(writable)
    }

    protected override fun newfileinfo(rpath: String): IFileInfo {
        return EncryptedFileInfo(root, Basepath.joinRpath(this.rpath, rpath))
    }

    class EncryptedFileContent(
            private val info: EncryptedFileInfo,
            private
            val file: File
    ) : IFileContent {

        override fun getInputStream(): InputStream {
            return FileFormat.openCipherInputStream(SeekableFileInputStream(file), info.root.key).first
        }

        override fun getSeekableInputStream(): ISeekableInputStream {
            return FileFormat.openCipherInputStream(SeekableFileInputStream(file), info.root.key).first
        }

        override fun getOutputStream(): OutputStream {
            return FileFormat.latestFormat().openCipherOutputStream(BufferedOutputStream(file.outputStream()), info.root.key)
        }

        override fun readBytes(): ByteArray {
            return getInputStream().use { it.readBytes() }
        }

        override fun readText(charset: Charset): String {
            return getInputStream().reader(charset).use { it.readText() }
        }

        override fun write(data: ByteArray, offset: Int, length: Int, timestamp: Long?, xrefs: JSONObject?) {
            prepareToWrite()
            getOutputStream().use { it.write(data, offset, length) }
            file.setLastModified(timestamp ?: System.currentTimeMillis())
        }

        override fun write(data: InputStream, timestamp: Long?, xrefs: JSONObject?) {
            prepareToWrite()
            getOutputStream().use { FileUt.copy(it, data) }
            file.setLastModified(timestamp ?: System.currentTimeMillis())
        }

        override fun copyTo(dst: OutputStream) {
            getInputStream().use { input ->
                FileUt.copy(dst, input)
            }
        }

        override fun copyTo(dst: IFileInfo, timestamp: Long?) {
            getInputStream().use {
                dst.content().write(it, timestamp)
            }
        }

        override fun moveTo(dst: IFileInfo, timestamp: Long?) {
            dst.root.let { if (it === info.root && renameTo(dst, timestamp)) return }
            copyTo(dst, timestamp)
            if (!file.delete()) throw IOException()
        }

        override fun renameTo(dst: IFileInfo, timestamp: Long?): Boolean {
            val dstcontent = dst.content()
            if (dstcontent is EncryptedFileContent) {
                if (file.renameTo(dstcontent.file)) {
                    dstcontent.file.setLastModified(timestamp ?: System.currentTimeMillis())
                    return true
                }
            }
            return false
        }

        override fun writeRecovery(data: InputStream?, timestamp: Long?): Boolean {
            return false
        }

        private fun prepareToWrite() {
            if (file.exists()) {
                if (!file.isFile || !file.delete()) throw IOException()
                return
            }
            file.mkparentOrNull() ?: throw IOException()
        }
    }
}

////////////////////////////////////////////////////////////////

/// RootInfo back up by a file.
class EncryptedFileRootInfo @JvmOverloads constructor(
        val key: SecretKey,
        file: File,
        override val name: String = file.name
) : FileInfoBase(file), IRootInfo {

    override val root get() = this

    override val rpath = ""

    override val cpath = name

    override val apath = File.separator + name

    override val parent: IFileInfo? = null

    override fun setLastModified(timestamp: Long): Boolean {
        return false
    }

    override fun setWritable(writable: Boolean): Boolean {
        return false
    }

    override fun stat(): IFileStat {
        return this
    }

    @Throws(IOException::class)
    override fun content(): IFileContent {
        throw IOException()
    }

    override fun find(ret: MutableCollection<String>, rpathx: String, pattern: String) {
        FileInfoUtil.find1(ret, rpathx, pattern, f, name)
    }

    override fun <T> transaction(code: Fun01<T>): T {
        return code()
    }

    override fun history(rpath: String, listdir: Boolean): List<IDeletedFileStat> {
        return emptyList()
    }

    override fun searchHistory(rpath: String, predicate: Fun11<IDeletedFileStat, Boolean>): List<IDeletedFileStat> {
        return emptyList()
    }

    override fun pruneHistory(infos: List<JSONObject>, all: Boolean): Pair<Int, Int> {
        return Pair(0, 0)
    }

    override fun recover(dst: IFileInfo, infos: List<JSONObject>): Pair<Int, Int> {
        return Pair(0, 0)
    }

    override fun updateXrefs(from: IFileInfo, infos: JSONObject?) {
    }

    override fun newfileinfo(rpath: String): IFileInfo {
        return EncryptedFileInfo(this, Basepath.joinRpath(this.rpath, rpath))
    }
}

private class StayOpenOutputStream(output: OutputStream) : PositionTrackingOutputStreamAdapter(output) {
    override fun close() {
    }
}

private class StayOpenInputStream(input: InputStream) : FilterInputStream(input) {
    override fun close() {
    }
}

private class LimitedSeekableInputStream(
        private val input: ISeekableInputStream,
        private val limit: Long
) : ISeekableInputStream() {
    private var position = input.getPosition()
    private var remaining = limit - position
    private var closed = false

    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        if (position >= limit) return input.readAt(limit, buffer, offset, 0)
        if (position + size <= limit) return input.readAt(position, buffer, offset, size)
        return input.readAt(position, buffer, offset, (limit - position).toInt())
    }

    override fun getSize(): Long {
        return limit
    }

    override fun getPosition(): Long {
        return position
    }

    override fun setPosition(pos: Long) {
        input.setPosition(if (pos > limit) limit else pos)
    }

    override fun read(): Int {
        if (closed || remaining <= 0) return -1
        val ret = input.read()
        if (ret < 0) {
            remaining = 0
            position = limit
        } else {
            --remaining
            ++position
        }
        return ret
    }

    override fun read(b: ByteArray): Int {
        return this.read(b, 0, b.size)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (closed) return -1
        if (len == 0) return 0
        val oremaining = remaining
        val oposititon = position
        val ret = input.read(b, off, (if (len > remaining) remaining.toInt() else len))
        if (ret <= 0) {
            remaining = 0
            position = limit
            return -1
        }
        remaining = oremaining - ret
        position = oposititon + ret
        return ret
    }

    override fun close() {
        try {
            this.closed = true
            super.close()
        } catch (e: Exception) {
        }
    }
}

private class BlockOutputStream(
        private val output: OutputStream,
        private val key: SecretKey,
        private val cipher: Cipher,
        private val format: IFileFormat = FileFormat.latestFormat(),
        private val postfix: ByteArray = byteArrayOf(),
        private val blocksize: Int = format.BLOCKSIZE
) : PositionTrackingOutputStream() {

    private var closed = false
    private var position = 0
    private var remaining = blocksize
    private var cout = format.createCipherOutputStream(output, cipher)

    override fun getPosition(): Long {
        return position.toLong()
    }

    override fun write(b: Int) {
        if (closed) throw IOException()
        if (remaining == 0) flushnow()
        cout.write(b)
        position += 1
        remaining -= 1
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        if (closed) throw IOException()
        var offset = off
        var length = len
        while (length > 0) {
            if (remaining == 0) flushnow()
            val n = if (remaining < length) remaining else length
            cout.write(b, offset, n)
            position += n
            remaining -= n
            offset += n
            length -= n
        }
    }

    override fun flush() {
    }

    override fun close() {
        close1()
        output.write(postfix)
        output.close()
        closed = true
    }

    private fun close1() {
        format.checksumBlock(cout)
        cout.close()
    }

    private fun flushnow() {
        val oiv = cipher.iv
        close1()
        reinit(oiv)
    }

    private fun reinit(oiv: ByteArray): OutputStream {
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        if (Arrays.equals(oiv, iv)) throw IOException()
        U.write(output, BU.marshalSalt(iv))
        cout = format.createCipherOutputStream(output, cipher)
        remaining = blocksize
        return cout
    }
}

private class EncryptedOutputStream(
        private val output: OutputStream,
        private val key: SecretKey,
        private val format: IFileFormat,
        private val blocksize: Int = format.BLOCKSIZE
) : OutputStream() {
    private var cipher = format.createCipher(key)
    private var out = StayOpenOutputStream(output)
    private var cout: OutputStream

    init {
        out.write(FileFormat.MAGIC) // 4
        U.writeU8(out, format.version()) // 1
        U.writeU8(out, blocksize / K.ENCRYPTED_BLOCK_SIZE_MULTIPIER) // 1
        format.writeCipher(out, cipher)
        cout = BlockOutputStream(out, key, cipher, format, byteArrayOf(), blocksize)
    }

    override fun close() {
        cout.close()
        output.close()
    }

    override fun write(b: Int) {
        cout.write(b)
    }

    override fun write(b: ByteArray) {
        cout.write(b)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        cout.write(b, off, len)
    }
}

private class EncryptedInputStream(
        private val seekable: ISeekableInputStream,
        key: SecretKey,
        format: IFileFormat,
        blocksize: Int,
        cipher: Cipher,
        contentStart: Long
) : ISeekableInputStream() {

    private val pool = SeekablePool(seekable, key, format, cipher, blocksize, contentStart)
    val timer = StepWatch()

    @Throws(IOException::class)
    override fun read(): Int {
        return pool.input { it.read() }
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return pool.input { it.read(b, off, len) }
    }

    override fun getSize(): Long {
        return pool.input { pool.getSize() }
    }

    override fun getPosition(): Long {
        return pool.input { it.getPosition() }
    }

    override fun setPosition(pos: Long) {
        pool.input { it.setPosition(pos) }
    }

    override fun close() {
        pool.close()
        seekable.close()
    }

    /// NOTE that this change the sequential position.
    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        return pool.readAt(position, buffer, offset, size)
    }

    private class BlockInputStream(
            private val pool: SeekablePool,
            private val size: Long,
            private val blocksize: Int,
            position: Long
    ) : InputStream() {
        private var blockStart = 0L
        private var blockLength = 0
        private var offset = 0
        private var startPos = 0L
        private var endPos = 0L
        private val buffer = ByteArray(pool.PADDING + blocksize + pool.SUM + pool.PADSIZE)

        init {
            setPosition1(position)
        }

        val startPosition get() = startPos
        val endPosition get() = endPos

        fun getPosition(): Long {
            return startPosition + offset
        }

        override fun read(): Int {
            if (offset >= blockLength && load() <= 0) return -1
            return buffer[pool.PADDING + offset++].toInt() and 0xff
        }

        @Throws(IOException::class)
        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (len == 0) return 0
            var offset = off
            var length = len
            while (length > 0) {
                if (this.offset >= blockLength && load() <= 0) break
                val n = if (length > (blockLength - this.offset)) blockLength - this.offset else length
                System.arraycopy(buffer, pool.PADDING + this.offset, b, offset, n)
                this.offset += n
                offset += n
                length -= n
            }
            return if (offset > off) offset - off else -1
        }

        /// @return 0 if end of file is reached, otherwise number of bytes skipped.
        @Throws(IOException::class)
        override fun skip(count: Long): Long {
            if (count < 0L) throw IOException()
            if (count == 0L) return 0L
            val pos = getPosition()
            setPosition(pos + count)
            return getPosition() - pos
        }

        fun setPosition(pos: Long) {
            val p = if (pos > size) size else pos
            if (pos in startPosition until endPosition) {
                offset = (pos - startPosition).toInt()
                return
            }
            setPosition1(p)
        }

        private fun setPosition1(pos: Long) {
            val (start, offset) = pool.blockOf(pos)
            var n = pool.readBlock(buffer, start)
            if (n < 0) n = 0
            this.blockStart = start
            this.blockLength = n
            this.startPos = pos - offset
            this.endPos = this.startPos + n
            this.offset = if (offset > n) n else offset
        }

        private fun load(): Int {
            val start = this.blockStart + blocksize + pool.OVERHEAD
            val n = pool.readBlock(buffer, start)
            if (n < 0) return n
            this.blockStart = start
            this.blockLength = n
            this.startPos = this.startPos + blocksize
            this.endPos = this.startPos + n
            this.offset = 0
            return n
        }
    }

    private class SeekablePool(
            private val input: ISeekableInputStream,
            private val key: SecretKey,
            private val format: IFileFormat,
            private val cipher: Cipher,
            private val blocksize: Int,
            private val contentStart: Long
    ) {
        private val logicalSize: Long? = null

        init {
        }

        val IV = format.IV
        val SUM = format.SUM
        val PADDING = format.PADDING
        val PADSIZE = format.PADSIZE
        val OVERHEAD = IV + PADDING + SUM + PADSIZE
        private val lock = ReentrantLock()
        private val ibuffer = ByteArray(IV + PADDING + blocksize + SUM + PADSIZE)
        private val seekables = ArrayList<BlockInputStream>()
        private var inputStream: BlockInputStream? = null

        fun <T> input(callback: Fun11<BlockInputStream, T>): T {
            return lock.withLock {
                callback(inputStream ?: BlockInputStream(this, input.getSize(), blocksize, 0).also {
                    this.inputStream = it
                })
            }
        }

        fun readAt(position: Long, buffer: ByteArray, off: Int, len: Int): Int {
            return lock.withLock {
                for (s in seekables) {
                    if (position >= s.getPosition() && position <= s.endPosition) {
                        
                        if (seekables.size > 1) {
                            seekables.removeAt(0)
                            seekables.add(s)
                        }
                        s.skip(position - s.getPosition())
                        return s.read(buffer, off, len)
                    }
                }
                val s = if (seekables.size >= K.SEEKABLE_POOL_SIZE) {
                    
                    seekables.removeAt(0).also { it.setPosition(position) }
                } else {
                    
                    BlockInputStream(this, input.getSize(), blocksize, position).also {
                        seekables.add(it)
                    }
                }
                s.read(buffer, off, len)
            }
        }

        fun readBlock(obuffer: ByteArray, blockstart: Long): Int {
            lock.withLock {
                val n = input.readAt(blockstart - IV, ibuffer, 0, IV + PADDING + blocksize + SUM + PADSIZE)
                if (n < IV + PADDING + SUM + PADSIZE) return -1
                val iv = BU.unmarshalSalt(ibuffer.copyOfRange(0, IV))
                format.initCipher(cipher, key, iv)
                val outcount = cipher.doFinal(ibuffer, IV, n - IV, obuffer)
                return format.verifyBlock(obuffer, outcount)
            }
        }

        fun close() {
            lock.withLock {
                inputStream?.close()
                seekables.forEach { it.close() }
            }
        }

        fun blockOf(pos: Long): Pair<Long, Int> {
            lock.withLock {
                val n = pos / blocksize
                val start = contentStart + n * (OVERHEAD + blocksize)
                return Pair(start, (pos - n * blocksize).toInt())
            }
        }

        fun getSize(): Long {
            lock.withLock {
                return logicalSize ?: run {
                    val rawsize = input.getSize()
                    val n = (rawsize - contentStart) / (blocksize + OVERHEAD)
                    val start = contentStart + n * (blocksize + OVERHEAD)
                    val count = readBlock(ByteArray((rawsize - start).toInt()), start)
                    n * blocksize + count
                }
            }
        }
    }
}

