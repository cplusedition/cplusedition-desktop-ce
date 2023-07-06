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

import com.cplusedition.anjson.JSONUtil.jsonObjectOrNull
import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.bot.core.Basepath
import com.cplusedition.bot.core.BotResult
import com.cplusedition.bot.core.ByteReader
import com.cplusedition.bot.core.ByteWriter
import com.cplusedition.bot.core.CountedTaskGroup
import com.cplusedition.bot.core.DateUt
import com.cplusedition.bot.core.Empty
import com.cplusedition.bot.core.FS
import com.cplusedition.bot.core.FileUt
import com.cplusedition.bot.core.Fun01
import com.cplusedition.bot.core.Fun10
import com.cplusedition.bot.core.Fun11
import com.cplusedition.bot.core.Fun21
import com.cplusedition.bot.core.Fun30
import com.cplusedition.bot.core.Fun31
import com.cplusedition.bot.core.Hex
import com.cplusedition.bot.core.HexDumper
import com.cplusedition.bot.core.IBotResult
import com.cplusedition.bot.core.ICoreLogger
import com.cplusedition.bot.core.IOUt
import com.cplusedition.bot.core.LimitedInputStream
import com.cplusedition.bot.core.MyByteOutputStream
import com.cplusedition.bot.core.NullOutputStream
import com.cplusedition.bot.core.ObjectPool
import com.cplusedition.bot.core.ObjectPoolDelegate
import com.cplusedition.bot.core.PositionTrackingOutputStream
import com.cplusedition.bot.core.PositionTrackingOutputStreamAdapter
import com.cplusedition.bot.core.RandomUt
import com.cplusedition.bot.core.StayOpenInputStream
import com.cplusedition.bot.core.StayOpenOutputStream
import com.cplusedition.bot.core.StepWatch
import com.cplusedition.bot.core.StructUt
import com.cplusedition.bot.core.TaskUt
import com.cplusedition.bot.core.Without
import com.cplusedition.bot.core.bot
import com.cplusedition.bot.core.mkparentOrNull
import org.json.JSONArray
import org.json.JSONObject
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An.DEF
import sf.andrians.cplusedition.support.An.Key
import sf.andrians.cplusedition.support.ArchiveFormat.Kind
import sf.andrians.cplusedition.support.BackupUtil.SigInfo
import sf.andrians.cplusedition.support.RestoreParam.RestoreParamBuilder
import sf.andrians.cplusedition.support.StorageBase.ReadOnlyJSONRoot
import sf.andrians.cplusedition.support.handler.IResUtil
import sf.andrians.cplusedition.support.media.IBarcodeUtil
import sf.andrians.cplusedition.support.media.ImageCropInfo
import sf.andrians.cplusedition.support.media.ImageUtil
import sf.andrians.cplusedition.support.media.MimeUtil
import sf.andrians.cplusedition.support.media.MimeUtil.Suffix
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.io.Reader
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.file.attribute.FileTime
import java.security.DigestInputStream
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Signature
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.interfaces.RSAPublicKey
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.concurrent.withLock
import kotlin.math.min

class BackupUtil(
    val rsrc: IResUtil,
    private val secUtil: ISecUtil,
) {

    companion object {
        private val CRLF = byteArrayOf('\r'.code.toByte(), '\n'.code.toByte())

        fun derToPem(der: ByteArray): String {
            return ("-----BEGIN CERTIFICATE-----\r\n"
                    + Base64.getMimeEncoder(64, CRLF).encodeToString(der)
                    + "\r\n-----END CERTIFICATE-----\r\n")
        }

        fun pemToDer(pem: String): ByteArray {
            val base64 = pem.lines().filter {
                val line = it.trim()
                line.isNotEmpty() && !line.startsWith("-----")
            }.joinToString("")
            return Base64.getDecoder().decode(base64)
        }

        fun getRSAKeyLength(cert: Certificate): Int {
            return (cert.publicKey as RSAPublicKey).modulus.bitLength()
        }

        @Throws(SecureException::class)
        fun writeZip(zipfile: IFileInfo, srcdir: IFileInfo, usenobackup: Boolean): BackupRestoreResult {
            return zipfile.content().outputStream().use { output ->
                writeZip(output, srcdir, usenobackup)
            }
        }

        fun writeZip(output: OutputStream, srcdir: IFileInfo, usenobackup: Boolean): BackupRestoreResult {
            return ZipOutputStream(output).use { zip ->
                val result = BackupRestoreResult()
                BU.writeExportDirChildren(zip, result, srcdir, "", usenobackup)
            }
        }

        @Throws(Exception::class)
        fun unzip(
            zipfile: IFileInfo,
            srcpath: String,
            dir: IFileInfo
        ): BackupRestoreResult {
            val result = BackupRestoreResult()
            val prefix = Basepath.trimLeadingSlash(srcpath).toString()
            dir.root.transaction {
                if (zipfile.file != null) {
                    unzip(result, dir, prefix, zipfile.file!!)
                } else {
                    zipfile.content().inputStream().use { input ->
                        unzip(result, dir, prefix, input)
                    }
                }
            }
            return result
        }

        private fun unzip(
            result: BackupRestoreResult,
            dir: IFileInfo,
            prefix: String,
            zipfile: File,
        ) {
            ZipFile(zipfile).use { zip ->
                zip.stream().forEach { entry ->
                    val rpath = Basepath.cleanRpath(entry.name)
                    if (rpath == null) {
                        result.fails.add(entry.name)
                        return@forEach
                    }
                    if (!rpath.startsWith(prefix)) return@forEach
                    val file = dir.fileInfo(rpath)
                    if (entry.isDirectory) {
                        if (!file.mkdirs()) result.fails.add(rpath)
                        return@forEach
                    }
                    try {
                        file.content().write(zip.getInputStream(entry))
                        val timestamp = entry.lastModifiedTime ?: entry.creationTime
                        if (timestamp != null) file.setLastModified(timestamp.toMillis())
                        result.oks.add(rpath)
                    } catch (e: Exception) {
                        result.fails.add(rpath)
                    }
                }
            }
        }

        private fun unzip(
            result: BackupRestoreResult,
            dir: IFileInfo,
            prefix: String,
            s: InputStream,
        ) {
            ZipInputStream(s).use { input ->
                while (true) {
                    val entry = input.nextEntry ?: break
                    val rpath = Basepath.cleanRpath(entry.name)
                    if (rpath == null) {
                        result.fails.add(entry.name)
                        continue
                    }
                    if (!rpath.startsWith(prefix)) continue
                    val file = dir.fileInfo(rpath)
                    if (entry.isDirectory) {
                        if (!file.mkdirs()) result.fails.add(rpath)
                        continue
                    }
                    try {
                        file.content().outputStream().use { out ->
                            FileUt.copy(DEFAULT_BUFFER_SIZE, out, input)
                        }
                        val timestamp = entry.lastModifiedTime ?: entry.creationTime
                        if (timestamp != null) {
                            file.setLastModified(timestamp.toMillis())
                        }
                        result.oks.add(rpath)
                    } catch (e: Exception) {
                        result.fails.add(rpath)
                    } finally {
                        input.closeEntry()
                    }
                }
            }
        }

        @Throws(Exception::class)
        fun verifyZip(zipfile: IFileInfo): BackupRestoreResult {
            val result = BackupRestoreResult()
            if (zipfile.file != null) {
                ZipFile(zipfile.file!!).use { zip ->
                    zip.stream().forEach { entry ->
                        if (entry.isDirectory) {
                            return@forEach
                        }
                        val rpath = Basepath.cleanRpath(entry.name)
                        if (rpath == null) {
                            result.fails.add(entry.name)
                            return@forEach
                        }
                        try {
                            FileUt.copy(NullOutputStream(), zip.getInputStream(entry))
                            result.oks.add(rpath)
                        } catch (e: Exception) {
                            result.fails.add(rpath)
                        }
                    }
                }
            } else {
                ZipInputStream(zipfile.content().inputStream()).use { input ->
                    while (true) {
                        val entry = input.nextEntry ?: break
                        if (entry.isDirectory) {
                            continue
                        }
                        val rpath = Basepath.cleanRpath(entry.name)
                        if (rpath == null) {
                            result.fails.add(entry.name)
                            continue
                        }
                        try {
                            FileUt.copy(512, NullOutputStream(), input)
                            result.oks.add(rpath)
                        } catch (e: Exception) {
                            result.fails.add(rpath)
                        } finally {
                            input.closeEntry()
                        }
                    }
                }
            }
            return result
        }

        fun listZip(storage: IStorage, zipfilepath: String): JSONObject {
            val rsrc = storage.rsrc
            try {
                val fileinfo = storage.fileInfoAt(zipfilepath).result()
                    ?: return rsrc.jsonObjectError(R.string.InvalidPath_, zipfilepath)
                if (!fileinfo.exists) return rsrc.jsonObjectError(R.string.FileNotFound_, zipfilepath)
                val result = listZip(fileinfo)
                return JSONObject().put(Key.result, result)
            } catch (e: Throwable) {
                return rsrc.jsonObjectError(e, R.string.CommandFailed)
            }
        }

        fun listZip(zipfile: IFileInfo, callback: Fun21<ZipEntry, Fun01<InputStream>, Long>? = null): JSONObject {
            fun toJSON(ret: JSONObject, entry: ZipEntry, size: Long, name: String): JSONObject {
                val isdir = entry.isDirectory
                ret.put(IFileInfo.Key.name, name)
                    .put(IFileInfo.Key.isdir, isdir)
                    .put(IFileInfo.Key.isfile, !isdir)
                    .put(IFileInfo.Key.length, size)
                    .put(IFileInfo.Key.lastModified, entry.lastModifiedTime.toMillis())
                if (isdir) ret.put(IFileInfo.Key.files, JSONObject())
                return ret
            }

            fun createdir(name: String, files: JSONObject, lastmodified: Long): JSONObject {
                return JSONObject().put(IFileInfo.Key.name, name)
                    .put(IFileInfo.Key.isdir, true)
                    .put(IFileInfo.Key.isfile, false)
                    .put(IFileInfo.Key.length, 0L)
                    .put(IFileInfo.Key.lastModified, lastmodified)
                    .put(IFileInfo.Key.files, files)
            }

            fun getdir(files: JSONObject, entry: ZipEntry, basepath: Basepath): JSONObject? {
                var ret = files
                val path = if (entry.isDirectory) entry.name else (basepath.dir ?: "")
                if (path.isEmpty()) return files
                for (s in path.split(FS)) {
                    if (s.isEmpty()) continue
                    val d = ret.jsonObjectOrNull(s)
                    if (d == null) {
                        ret = JSONObject().also { ret.put(s, createdir(s, it, entry.lastModifiedTime.toMillis())) }
                        continue
                    }
                    ret = d.jsonObjectOrNull(IFileInfo.Key.files) ?: return null
                }
                return ret
            }

            val files = JSONObject()
            zipfile.root.transaction {
                zipfile.file?.let {
                    ZipFile(it).use { zipfile ->
                        for (entry in zipfile.entries()) {
                            val basepath = Basepath.from(entry.name)
                            val dir = getdir(files, entry, basepath)
                                ?: throw IOException()
                            if (callback != null) callback(entry) {
                                zipfile.getInputStream(entry)
                            }
                            if (!entry.isDirectory) dir.put(
                                basepath.name,
                                toJSON(JSONObject(), entry, entry.size, basepath.name)
                            )
                        }
                    }
                } ?: run {
                    ZipInputStream(zipfile.content().inputStream()).use { zip ->
                        while (true) {
                            val entry = zip.nextEntry ?: break
                            try {
                                val basepath = Basepath.from(entry.name)
                                val dir = getdir(files, entry, basepath)
                                    ?: throw IOException()
                                if (!entry.isDirectory) {
                                    val size = if (callback != null) callback(entry) {
                                        StayOpenInputStream(zip)
                                    } else FileUt.copy(512, NullOutputStream(), StayOpenInputStream(zip))
                                    dir.put(
                                        basepath.name,
                                        toJSON(JSONObject(), entry, size, basepath.name)
                                    )
                                }
                            } finally {
                                zip.closeEntry()
                            }
                        }
                    }
                }
            }
            return createdir("", files, zipfile.stat()!!.lastModified)
        }

        private fun ensureSlots(min: Int, aliases: List<String>): List<String> {
            if (aliases.size >= min) return aliases
            val ret = ArrayList(aliases)
            while (ret.size < min) {
                ret.add(if (ret.size > 0) ret[0] else ISecUtil.ALIAS_BACKUP)
            }
            return ret
        }

        fun actionBackupData1(
            storage: IStorage,
            st: IStorageAccessor,
            backupfile: IFileInfo,
            srcdir: IFileInfo,
            aliases: Array<String>,
        ): IBotResult<BackupRestoreResult, JSONObject> {
            val rsrc = storage.rsrc
            if (backupfile.cpath.startsWith(srcdir.cpath + FS)) {
                return BotResult.fail(rsrc.jsonObjectError(R.string.BackupFileMustNotUndirSourceDirectory))
            }
            return try {
                val result = st.backupData(backupfile, aliases.toList(), srcdir)
                BotResult.ok(result)
            } catch (e: Throwable) {
                backupfile.delete()
                val msg = rsrc.actionFailed(
                    if (backupfile.name.endsWith(Suffix.ZIP)) R.string.Export else R.string.Backup
                )
                BotResult.fail(rsrc.jsonObjectError(e, msg))
            }
        }

        fun actionBackupConversion0(
            storage: IStorage,
            st: IStorageAccessor,
            dstpath: String,
            srcpath: String,
            aliases: Array<String>,
            cut: Boolean,
        ): IBotResult<BackupRestoreResult, JSONObject> {
            return actionBackupConversion1(storage, st, dstpath, srcpath).mapOK { (dst, src, readonlysrc) ->
                val rsrc = storage.rsrc
                val backupresult = if (readonlysrc is ICloseableRootInfo) {
                    readonlysrc.use { actionBackupData1(storage, st, dst, it, aliases) }
                } else if (dst.lcSuffix == Suffix.BACKUP) {
                    actionBackupData1(storage, st, dst, readonlysrc, aliases)
                } else return@mapOK BotResult.fail(rsrc.jsonObjectError(R.string.UnsupportedOperation))
                backupresult.mapOK { result ->
                    src.stat()?.let { dst.setLastModified(it.lastModified) }
                    if (cut && result.fails.isEmpty()) src.delete()
                    BotResult.ok(result)
                }
            }
        }

        fun actionBackupConversion1(
            storage: IStorage,
            st: IStorageAccessor,
            dstpath: String,
            srcpath: String,
        ): IBotResult<Triple<IFileInfo, IFileInfo, IFileInfo>, JSONObject> {
            val rsrc = storage.rsrc
            val srcfile = storage.fileInfoAt(srcpath).let {
                it.result() ?: return BotResult.fail(rsrc.jsonObjectError(it.failure()!!))
            }
            val dstfile = storage.fileInfoAt(dstpath).let {
                it.result() ?: return BotResult.fail(rsrc.jsonObjectError(it.failure()!!))
            }
            val isvalid = { dst: String, src: String ->
                src == Suffix.ZIP && dst == Suffix.BACKUP
                        || src == Suffix.BACKUP && (dst == Suffix.BACKUP || dst == Suffix.ZIP)
                        || src == Suffix.IBACKUP && (dst == Suffix.BACKUP || dst == Suffix.ZIP)
            }
            val lcsrc = srcfile.lcSuffix
            val lcdst = dstfile.lcSuffix
            if (!isvalid(lcdst, lcsrc)) {
                return BotResult.fail(rsrc.jsonObjectError(rsrc.actionFailed(R.string.UnsupportedOperation)))
            }
            if (dstfile.cpath == srcfile.cpath)
                return BotResult.fail(rsrc.jsonObjectError(R.string.DestinationMustNotBeSameAsSource))
            if (dstfile.stat()?.isFile == false)
                return BotResult.fail(rsrc.jsonObjectInvalidPath(dstpath))
            if (srcfile.stat()?.isFile != true)
                return BotResult.fail(rsrc.jsonObjectInvalidPath(srcpath))
            val readonlysrc = if (lcsrc == Suffix.ZIP) srcfile else {
                st.backupFileRoot(srcfile)
                    ?: return BotResult.fail(rsrc.jsonObjectInvalidPath(srcpath))
            }
            return BotResult.ok(Triple(dstfile, srcfile, readonlysrc))
        }

        fun actionVerifyBackup1(st: IStorageAccessor, backupfile: IFileInfo): BackupRestoreResult {
            val fails = ConcurrentLinkedDeque<String>()
            val warns = ConcurrentLinkedDeque<String>()
            val oks = ConcurrentLinkedDeque<String>()
            TaskUt.forkJoinTasks { pool ->
                backupfile.walk3 { file, rpath, stat ->
                    if (!stat.isFile) return@walk3
                    val lcsuffix = Basepath.lcSuffix(file.name)
                    if (!MimeUtil.isBackupLcSuffix(lcsuffix)) return@walk3
                    pool.submit {
                        try {
                            val result = if (lcsuffix == Suffix.ZIP) {
                                BackupUtil.verifyZip(file)
                            } else if (lcsuffix == Suffix.BACKUP || lcsuffix == Suffix.IBACKUP) {
                                st.verifyBackup(file)
                            } else return@submit
                            if (result.fails.isNotEmpty()) fails.add(rpath) else oks.add(rpath)
                            warns.addAll(result.warns)
                        } catch (e: Throwable) {
                            fails.add(rpath)
                        }
                    }
                }
            }
            return BackupRestoreResult(oks, fails, warns)
        }

        fun importBackupKey(
            storage: IStorage,
            st: IStorageAccessor,
            barcodeutil: IBarcodeUtil,
            alias: String,
            cpath: String,
        ): JSONObject {
            val cpath1 = if (cpath.isEmpty() || cpath == FS) An.PATH.Internal else cpath
            val fileinfo = storage.fileInfoAt(cpath1).let {
                it.result() ?: return storage.rsrc.jsonObjectError(it.failure()!!)
            }
            return importBackupKey(storage.rsrc, st, barcodeutil, alias, fileinfo)
        }

        private fun importBackupKey(
            rsrc: IResUtil,
            st: IStorageAccessor,
            barcodeutil: IBarcodeUtil,
            alias: String,
            fileinfo: IFileInfo
        ): JSONObject {
            if (fileinfo.isDir) {
                return rsrc.jsonObjectError(R.string.ErrorToBeImplemented)
            }
            return st.secAction { sec ->
                Without.exceptionOrNull {
                    importBackupKey(rsrc, barcodeutil, fileinfo).onResult({
                        it
                    }, {
                        it.use { input ->
                            sec.importPublicKey(alias, input).onResult({ msgid ->
                                rsrc.jsonObjectError(msgid)
                            }, { entry ->
                                val desc = sec.descOf(alias, entry.trustedCertificate as Certificate)
                                JSONObject().put(Key.result, JSONArray(desc))
                            })
                        }
                    })
                } ?: rsrc.jsonObjectError(rsrc.actionFailed(R.string.Import))
            }
        }

        fun importBackupKey(
            rsrc: IResUtil,
            barcodeutil: IBarcodeUtil,
            fileinfo: IFileInfo
        ): IBotResult<InputStream, JSONObject> {
            val lcsuffix = Basepath.lcSuffix(fileinfo.name)
            val content = if (lcsuffix == Suffix.DER) {
                fileinfo.content().inputStream()
            } else if (lcsuffix == Suffix.PEM) {
                pemToDer(IOUt.readText(fileinfo.content().inputStream())).inputStream()
            } else {
                val mime = MimeUtil.imageMimeFromLcSuffix(lcsuffix)
                    ?: return BotResult.fail(rsrc.jsonObjectError(R.string.UnsupportedInputFormat_, lcsuffix))
                fileinfo.content().inputStream().use { input ->
                    val base64 = barcodeutil.detectQRCode(input, mime, ImageCropInfo.none())
                        ?.stringOrNull(Key.result)
                        ?: return BotResult.fail(rsrc.jsonObjectError(R.string.BarcodeNotFound))
                    Base64.getDecoder().decode(base64).inputStream()
                }
            }
            return BotResult.ok(content)
        }

        fun exportBackupKey(
            storage: IStorage,
            st: IStorageAccessor,
            barcodeutil: IBarcodeUtil,
            alias: String,
            cpath: String
        ): JSONObject {
            return st.secAction { sec ->
                val cpath1 = if (cpath.isEmpty() || cpath == FS) An.PATH.Internal else cpath
                val fileinfo = storage.fileInfoAt(cpath1).let {
                    it.result() ?: return@secAction storage.rsrc.jsonObjectError(it.failure()!!)
                }
                Without.exceptionOrNull {
                    return@exceptionOrNull exportBackupKey(sec, storage.rsrc, barcodeutil, alias, fileinfo)
                } ?: storage.rsrc.jsonObjectError(R.string.ImportFailed)
            }
        }

        fun exportBackupKey(
            sec: ISecUtilAccessor,
            rsrc: IResUtil,
            barcodeutil: IBarcodeUtil,
            alias: String,
            fileinfo: IFileInfo
        ): JSONObject {
            val cert = sec.getPublicKey(alias)
                ?: return rsrc.jsonObjectError(R.string.BackupPublicKeyNotFound_, alias)
            val encoded = cert.encoded
                ?: return rsrc.jsonObjectError(rsrc.actionFailed(R.string.Export))
            if (fileinfo.isDir) {
                val base64 = Base64.getEncoder().encodeToString(encoded)
                val dataurl = barcodeutil.generateQRCode(base64)?.let {
                    ImageUtil.toPngDataUrl(it)
                } ?: return rsrc.jsonObjectError(rsrc.actionFailed(R.string.Export))
                return JSONObject().put(Key.data, dataurl)
            }
            val lcsuffix = Basepath.lcSuffix(fileinfo.name)
            if (lcsuffix == Suffix.DER) {
                fileinfo.content().write(encoded)
            } else if (lcsuffix == Suffix.PEM) {
                fileinfo.content().write(derToPem(encoded).toByteArray())
            } else {
                val mime = MimeUtil.imageMimeFromLcSuffix(lcsuffix)
                    ?: return rsrc.jsonObjectError(R.string.UnsupportedOutputFormat_, lcsuffix)
                val base64 = Base64.getEncoder().encodeToString(encoded)
                val image = barcodeutil.generateQRCode(base64, mime)
                    ?: return rsrc.jsonObjectError(rsrc.actionFailed(R.string.Export))
                fileinfo.content().write(image)
            }
            return JSONObject()
        }

        fun backupRestoreResult(res: IResUtil, stringid: Int, result: BackupRestoreResult): JSONObject {
            val warns = if (result.warns.isEmpty()) JSONArray(result.fails) else {
                val ret = JSONArray()
                result.fails.forEach { ret.put(it) }
                result.warns.forEach { ret.put(it) }
                ret
            }
            val msg = res.get(stringid) + res.format(R.string.ResultOK, result.oks.size) +
                    (if (result.ignores.isNotEmpty()) res.format(R.string.ResultIgnored, result.ignores.size) else "") +
                    (if (result.warns.isNotEmpty()) res.format(R.string.ResultWarns, result.warns.size) else "") +
                    (if (result.fails.isNotEmpty()) res.format(R.string.ResultFailed, result.fails.size) else "")
            return JSONObject()
                .put(Key.result, msg)
                .put(Key.warns, warns)
        }
    }

    @Throws(SecureException::class)
    fun exportData(zipfile: IFileInfo, srcdir: IFileInfo): BackupRestoreResult {
        try {
            return writeZip(zipfile, srcdir, true)
        } catch (e: Throwable) {
            zipfile.delete()
            throw rsrc.secureException(e, rsrc.actionFailed(R.string.Export))
        }
    }

    @Throws(SecureException::class)
    fun backupData(backupfile: IFileInfo, aliases: List<String>, srcdir: IFileInfo): BackupRestoreResult {
        return backupData(backupfile, ArchiveFormat.latestArchiveFormat(), aliases, srcdir)
    }

    private fun backupData(
        backupfile: IFileInfo,
        format: IArchiveFormat,
        aliases: List<String>,
        srcdir: IFileInfo
    ): BackupRestoreResult {
        try {
            if (aliases.isEmpty())
                throw IOException()
            val incremental = backupfile.name.endsWith(Suffix.IBACKUP)
            val infos = getLastBackupInfos(backupfile) ?: TreeMap()
            val param = BackupParam(format, incremental, infos, srcdir)
            backupData1(backupfile, ensureSlots(2, aliases), param)
            return BackupRestoreResult(
                param.oks,
                param.fails,
                param.warns,
                param.ignores,
            )
        } catch (e: Throwable) {
            backupfile.delete()
            throw rsrc.secureException(e, rsrc.actionFailed(R.string.Backup))
        }
    }

    fun backupKey(keyfile: IFileInfo) {
        try {
            backupKey1(keyfile)
        } catch (e: Throwable) {
            keyfile.delete()
            throw rsrc.secureException(e, rsrc.actionFailed(R.string.Backup))
        }
    }

    private fun collectBackupFiles(backupfile: IFileInfo): MutableList<IFileInfo> {
        val backupfiles: MutableList<IFileInfo> = ArrayList()
        val filename = backupfile.name
        if (!filename.endsWith(Suffix.IBACKUP)) {
            backupfiles.add(backupfile)
        } else {
            val dir = backupfile.parent
                ?: throw rsrc.secureException(R.string.InvalidPath)
            val names = BU.getIBackupFilenames(filename, dir)
            for (name in names) {
                backupfiles.add(dir.fileInfo(name))
                if (name == filename) break
            }
        }
        return backupfiles
    }

    @Throws(SecureException::class)
    fun verifyBackup(backupfile: IFileInfo): BackupRestoreResult {
        val backupfiles = collectBackupFiles(backupfile)
        return try {
            if (backupfiles.size == 1) {
                verifyBackup1(backupfiles.first())
            } else {
                verifyBackup2(backupfiles)
            }
        } catch (e: Throwable) {
            throw rsrc.secureException(e, rsrc.actionFailed(R.string.BackupVerify))
        }
    }

    @Throws(SecureException::class)
    fun restoreData(destdir: IFileInfo, backupfile: IFileInfo, from: String, sync: Boolean = false): BackupRestoreResult {
        val backupfiles = collectBackupFiles(backupfile)
        try {
            return if (backupfiles.size == 1) {
                restoreData1(backupfiles.first(), destdir, from, sync)
            } else {
                restoreData2(backupfiles, destdir, from, sync)
            }
        } catch (e: Throwable) {
            throw rsrc.secureException(e, rsrc.actionFailed(R.string.Restore))
        }
    }

    fun verifyMagic(magic: ByteArray) {
        if (!ArchiveFormat.MAGIC.contentEquals(magic))
            throw SecureException()
    }

    //////////////////////////////////////////////////////////////////////

    private fun backupData1(backupfile: IFileInfo, aliases: List<String>, param: BackupParam) {
        backupfile.content().outputStream().use { output ->
            val keypair = secUtil.invoke { it.getBackupKeyPair() }
            openBackupOutputStream(output, keypair, aliases, param.format) { cout, siginfo ->
                BU.backupData(cout, secUtil, rsrc, param, siginfo)
            }
        }?.let { headers ->
            backupfile.content().seekableOutputStream(false)!!.use {
                it.writeAt(0, headers)
            }
        }
    }

    private fun backupKey1(keyfile: IFileInfo) {
        secUtil.invoke {
            it.getBackupPublicKey()
        }.encoded.inputStream().use {
            keyfile.content().write(it)
        }
    }

    @Throws(IOException::class)
    private fun <T> openBackupOutputStream(
        output: OutputStream,
        keypair: BackupKeyPair,
        aliases: List<String>,
        format: IArchiveFormat,
        callback: Fun21<PositionTrackingOutputStream, SigInfo?, T>,
    ): T {
        val key = KeyUtil.createAESKey()
        val cipher = format.fileFormat().createEncryptionCipher(key)
        val padding1 = U.randBytes(96, 160)
        val padding2 = U.randBytes(0, 128)
        val padding3 = U.randBytes(0, 128)
        val keysig = U.sha256(secUtil.invoke { it.getBackupPublicKey() }.encoded)
        val out = PositionTrackingOutputStreamAdapter(output)
        if (format.version() < ArchiveFormat.V13) {
            val key1 = KeyUtil.createAESKey()
            val cipher1 = format.fileFormat().createEncryptionCipher(key1)
            val payload = createPayload(format, key, cipher)
            val epayload = BU.encryptPadded(cipher1, format.fileFormat()) { aesout ->
                val signer = secUtil.rsaSigner(keypair.privateKey)
                val signed = format.signPayload(signer, payload)
                ByteWriter(aesout).write32BEBytes(signed)
            }
            val info = HeaderInfo(
                format,
                getRSAKeyLength(keypair.certificate),
                0L,
                aliases.size,
                epayload.size(),
                padding1.size,
                padding2.size,
                padding3.size,
                keysig,
                key1,
                cipher1,
                BU.serializeCipher(format, key1, cipher1),
            )
            val headers = ByteArrayOutputStream().use {
                BU.writeEncryptedHeaders(secUtil, rsrc, it, aliases, info)
                it
            }.toByteArray()
            out.write(headers)
            out.write(padding1)
            out.write(epayload.buffer(), 0, epayload.size())
            val signer = secUtil.rsaSigner(keypair.privateKey)
            out.write(format.signHeaders(signer, info.key, headers, payload))
            out.write(padding2)
            out.write(BU.marshalSalt(cipher.iv))
            return BlockOutputStream(StayOpenOutputStream(out), key, cipher, format.fileFormat(), padding3).use {
                callback(it, null)
            }
        }
        val info = HeaderInfo(
            format,
            getRSAKeyLength(keypair.certificate),
            0L,
            aliases.size,
            0,
            padding1.size,
            format.fileFormat().encodeBlocksize(),
            padding3.size,
            keysig,
            key,
            cipher,
            BU.serializeCipher(format, key, cipher),
        )
        val headers = ByteArrayOutputStream().use {
            BU.writeEncryptedHeaders(secUtil, rsrc, it, aliases, info)
            it
        }.toByteArray()
        out.write(headers)
        out.write(padding1)
        out.write(BU.marshalSalt(cipher.iv))
        val siginfo = SigInfo(keypair, aliases, info, headers, byteArrayOf())
        return BlockOutputStream(StayOpenOutputStream(out), key, cipher, format.fileFormat(), padding3).use {
            callback(it, siginfo)
        }
    }

    private fun createPayload(
        format: IArchiveFormat,
        key: SecretKey,
        cipher: Cipher,
    ): ByteArray {

        val payload = ByteArrayOutputStream().use { out ->
            format.fileFormat().writePadding(out)
            IOUt.writeU8(out, format.fileFormat().encodeBlocksize())
            format.writeCipher(out, key, cipher)
            out
        }.toByteArray()
        return payload
    }

    @Throws(SecureException::class)
    private fun <T> openBackupInputStream(
        backupfile: IFileInfo,
        callback: Fun31<MySeekableInputStream, BackupFileInfo, ByteArray, T>
    ): T {
        return backupfile.content().seekableInputStream()?.use {
            openBackupInputStream(it, callback)
        } ?: throw errorReadingBackup()
    }

    @Throws(SecureException::class)
    private fun <T> openBackupInputStream(
        input: MySeekableInputStream,
        callback: Fun31<MySeekableInputStream, BackupFileInfo, ByteArray, T>
    ): T {
        return try {
            val keypair = secUtil.invoke { it.getBackupKeyPair() }
            openBackupInputStream(input, keypair) { cis, backupfileinfo, directory ->
                cis.use {
                    callback(it, backupfileinfo, directory)
                }
            }
        } catch (e: Throwable) {
            throw errorReadingBackup(e)
        }
    }

    internal
    class HeaderInfo constructor(
        val format: IArchiveFormat,
        val rsaKeyLength: Int,
        var directoryOffset: Long,
        val aliasCount: Int,
        val epayloadSize: Int,
        val padding1Size: Int,
        val padding2Size: Int,
        val padding3Size: Int,
        val keySig: ByteArray,
        val key: SecretKey,
        val cipher: Cipher,
        val cipherdata: ByteArray,
    ) {
    }

    private
    class PayloadInfo constructor(
        val backupfileinfo: BackupFileInfo,
        val blocksize: Int,
        val key: SecretKey,
        val cipher: Cipher,
    ) {
    }

    private
    class SignedPayloadInfo constructor(
        val payload: ByteArray,
        val alias: String,
        val cert: Certificate,
        val padding1Size: Int,
        val padding2Size: Int
    ) {
    }

    internal class SigInfo(
        val keypair: BackupKeyPair,
        val aliases: List<String>,
        val info: HeaderInfo,
        val headers: ByteArray,
        val payload: ByteArray
    )

    private fun errorReadingBackup(e: Throwable? = null): SecureException {
        return rsrc.secureException(e, R.string.ErrorReadingBackup)
    }

    private fun readHeaderInfo(
        input: MySeekableInputStream,
        keypair: BackupKeyPair,
    ): Pair<HeaderInfo, Int>? {
        val keylen = BackupUtil.getRSAKeyLength(keypair.certificate)
        val padding = V10Padding()
        val buf = ByteArray(512)
        var offset = 0
        while (offset < 8 * 512) {
            offset += 512
            try {
                readHeader(buf, input, keypair, keylen).inputStream().use { hdrin ->
                    padding.readPadding(hdrin)
                    val magic = IOUt.readFully(hdrin, ByteArray(ArchiveFormat.MAGIC.size))
                    StructUt.equals(magic, ArchiveFormat.MAGIC)
                            || throw errorReadingBackup()
                    val r = ByteReader(hdrin)
                    val version = r.readU8()
                    val format = ArchiveFormat.get(version)
                        ?: throw errorReadingBackup()
                    val aliascount = r.readU8()
                    val payloadsize = r.read32BE()
                    val (padding1size, padding2size, padding3size) = format.readPaddings(hdrin)
                    val diroffset = if (format.version() >= ArchiveFormat.V14) {
                        r.read63UV()
                    } else 0L
                    val keysig = r.readU8Bytes()
                    val (key1, cipher1) = format.readCipher(PositionTrackingInputStreamAdapter(hdrin))
                    val info = HeaderInfo(
                        format,
                        getRSAKeyLength(keypair.certificate),
                        diroffset,
                        aliascount,
                        payloadsize,
                        padding1size,
                        padding2size,
                        padding3size,
                        keysig,
                        key1,
                        cipher1,
                        BU.serializeCipher(format, key1, cipher1)
                    )
                    return Pair(info, offset)
                }
            } catch (e: Throwable) {
                
            }
        }
        return null
    }

    private fun readPayload(
        input: MySeekableInputStream,
        endoffset: Int,
        info: HeaderInfo,
    ): SignedPayloadInfo? {
        
        var offset = endoffset
        val end = info.aliasCount * 512
        while (true) {
            if (offset >= end) break
            IOUt.skipFully(input, 512)
            offset += 512
        }
        IOUt.skipFully(input, info.padding1Size.toLong())
        return verifyPayloadInfo(input, info)
    }

    private fun verifyPayloadInfo(
        input: MySeekableInputStream,
        info: HeaderInfo
    ): SignedPayloadInfo? {
        for ((alias, cert) in secUtil.invoke { it.getBackupPublicKeys() }.entries) {
            if (U.sha256(cert.encoded).contentEquals(info.keySig)) {
                val verifier = secUtil.rsaVerifier(cert.publicKey)
                val keylen = getRSAKeyLength(cert)
                val epayload = IOUt.readFully(input, ByteArray(info.epayloadSize))
                val (signed, padding1, padding2) = BU.readPadded(epayload, info.cipher, info.format.fileFormat()) {
                    ByteReader(it).read32BEBytes(2048)
                }
                val payload = info.format.verifyPayload(verifier, signed)
                    ?: throw errorReadingBackup()
                val size = info.format.fileFormat().let { 4 + 512 + it.SUM + it.PADSIZE }
                val headers = ByteArray(info.aliasCount * 512)
                input.readFullyAt(0, headers, 0, headers.size)
                if (!info.format.verifyHeaders(
                        input,
                        size.toLong(),
                        verifier,
                        keylen,
                        info.cipher,
                        info.key,
                        headers,
                        payload
                    )
                ) throw errorReadingBackup()
                return SignedPayloadInfo(payload, alias, cert, padding1, padding2)
            }
        }
        return null
    }

    private fun readPayloadInfo(
        input: MySeekableInputStream,
        endoffset: Int,
        info: HeaderInfo,
    ): PayloadInfo {
        readPayload(input, endoffset, info)?.let { signedinfo ->
            return signedinfo.payload.inputStream().use {
                val format = info.format
                val fileformat = format.fileFormat()
                fileformat.readPadding(it)
                val blocksize = fileformat.decodeBlocksize(IOUt.readU8(it))
                val (key, cipher) = format.readCipher(PositionTrackingInputStreamAdapter(it))
                IOUt.skipFully(input, info.padding2Size.toLong())
                if (!BU.unmarshalSalt(IOUt.readFully(input, ByteArray(cipher.iv.size))).contentEquals(cipher.iv)
                ) throw errorReadingBackup()
                var slots = info.aliasCount
                var padding1 = info.padding1Size
                while (padding1 > 512) {
                    padding1 -= 512
                    slots += 1
                }
                val backupfileinfo = BackupFileInfo(info.format, slots, info.aliasCount, signedinfo.alias, signedinfo.cert)
                PayloadInfo(backupfileinfo, blocksize, key, cipher)
            }
        } ?: throw rsrc.secureException(R.string.BackupPublicKeyNotFound_, Hex.encode(info.keySig).toString())
    }

    internal fun <T> openBackupInputStream(
        input: MySeekableInputStream,
        keypair: BackupKeyPair,
        callback: Fun31<MySeekableInputStream, BackupFileInfo, ByteArray, T>
    ): T {
        val (headerinfo, endoffset) = readHeaderInfo(input, keypair)
            ?: throw rsrc.secureException(R.string.BackupPublicKeyNotFound_)
        val format = headerinfo.format
        val fileformat = format.fileFormat()
        if (format.version() < ArchiveFormat.V13) {
            val payloadinfo = readPayloadInfo(input, endoffset, headerinfo)
            val pos = input.getPosition()
            val cis = EncryptedInputStream(
                LimitedSeekableInputStream(input, input.getSize() - headerinfo.padding3Size),
                payloadinfo.key,
                fileformat,
                payloadinfo.blocksize,
                payloadinfo.cipher,
                pos
            )
            fileformat.readPadding(cis)
            val position = cis.getPosition()
            val (directory, _) = format.readDirectory(cis)
            cis.setPosition(position)
            return callback(cis, payloadinfo.backupfileinfo, directory)
        }
        val headers = ByteArray(headerinfo.aliasCount * 512)
        input.readFullyAt(0, headers, 0, headers.size)
        input.setPosition(headers.size.toLong())
        IOUt.skipFully(input, headerinfo.padding1Size.toLong())
        val actual = BU.unmarshalSalt(IOUt.readFully(input, ByteArray(headerinfo.cipher.iv.size)))
        val expected = headerinfo.cipher.iv
        if (!expected.contentEquals(actual))
            throw errorReadingBackup()
        val pos = input.getPosition()
        val cis = EncryptedInputStream(
            LimitedSeekableInputStream(input, input.getSize() - headerinfo.padding3Size),
            headerinfo.key,
            fileformat,
            fileformat.decodeBlocksize(headerinfo.padding2Size),
            headerinfo.cipher,
            pos
        )
        fileformat.readPadding(cis)
        val (directory, backupfileinfo) =
            if (format.version() < ArchiveFormat.V14) {
                verifyV13(cis, headerinfo, headers)
            } else {
                verifyV14(cis, headerinfo, headers)
            }
        return callback(cis, backupfileinfo, directory)
    }

    private fun verifyV14(input: MySeekableInputStream, info: HeaderInfo, headers: ByteArray): Pair<ByteArray, BackupFileInfo> {
        val position = input.getPosition()
        input.setPosition(info.directoryOffset)
        val (directory, checksum, signature) = info.format.readSignature(input)
        input.setPosition(position)
        val backupfileinfo = readBackupFileInfo(info)
            ?: throw SecureException(rsrc.get(R.string.BackupPublicKeyNotFound_, Hex.encode(info.keySig).toString()))
        val verifier = secUtil.rsaVerifier(backupfileinfo.cert.publicKey)
        val keylen = getRSAKeyLength(backupfileinfo.cert)
        verifier.update(headers)
        verifier.update(checksum)
        if (!verifier.verify(signature, 0, keylen / 8))
            throw errorReadingBackup()
        return Pair(directory, backupfileinfo)
    }

    private fun verifyV13(input: MySeekableInputStream, info: HeaderInfo, headers: ByteArray): Pair<ByteArray, BackupFileInfo> {
        val position = input.getPosition()
        val (directory, checksum, signature) = info.format.readSignature(input)
        input.setPosition(position)
        val backupfileinfo = readBackupFileInfo(info)
            ?: throw SecureException(rsrc.get(R.string.BackupPublicKeyNotFound_, Hex.encode(info.keySig).toString()))
        val verifier = secUtil.rsaVerifier(backupfileinfo.cert.publicKey)
        val keylen = getRSAKeyLength(backupfileinfo.cert)
        verifier.update(headers)
        verifier.update(checksum)
        if (!verifier.verify(signature, 0, keylen / 8))
            throw errorReadingBackup()
        return Pair(directory, backupfileinfo)
    }

    private fun readBackupFileInfo(info: HeaderInfo): BackupFileInfo? {
        for ((alias, cert) in secUtil.invoke { it.getBackupPublicKeys() }.entries) {
            if (U.sha256(cert.encoded).contentEquals(info.keySig)) {
                var slots = info.aliasCount
                var padding1 = info.padding1Size
                while (padding1 > 512) {
                    padding1 -= 512
                    slots += 1
                }
                return BackupFileInfo(info.format, slots, info.aliasCount, alias, cert)
            }
        }
        return null
    }
    @Throws(SecureException::class)
    private fun readBackupFileInfo(
        input: MySeekableInputStream,
        keypair: BackupKeyPair,
    ): IBotResult<BackupFileInfo, String> {
        return readHeaderInfo(input, keypair)?.let { (info, _) ->
            readBackupFileInfo(info)?.let {
                BotResult.ok(it)
            } ?: BotResult.fail(rsrc.get(R.string.BackupPublicKeyNotFound_, Hex.encode(info.keySig).toString()))
        } ?: BotResult.fail(rsrc.get(R.string.ErrorReadingBackup))
    }

    private fun readHeader(buf: ByteArray, input: InputStream, keypair: BackupKeyPair, keylen: Int): ByteArray {
        val rsa = secUtil.rsaDecryptionCipher(keypair.privateKey)
        IOUt.readFully(input, buf)
        val header = rsa.doFinal(buf, 0, keylen / 8)
        val size = header.size
        val expected = header.copyOfRange(size - K.SHA256_BUFSIZE, size)
        val actual = U.sha256(header.copyOfRange(0, size - K.SHA256_BUFSIZE))
        if (!expected.contentEquals(actual))
            throw errorReadingBackup()
        return header
    }

    private fun <T> readBackupFile(
        backupfile: IFileInfo,
        callback: Fun31<MySeekableInputStream, BackupFileInfo, ByteArray, T>
    ): T {
        return openBackupInputStream(backupfile) { cis, backupfileinfo, directory ->
            callback(cis, backupfileinfo, directory)
        }
    }

    fun readBackupFiletree(backupfile: IFileInfo): JSONObject {
        return readBackupFile(backupfile) { _, backupfileinfo, directory ->
            backupfileinfo.format.readFiletree(rsrc, directory)
        }
    }

    fun readBackupFileInfo(backupfile: IFileInfo): JSONObject {
        return Without.exceptionOrNull {
            backupfile.content().seekableInputStream()?.use { input ->
                val keypair = secUtil.invoke { it.getBackupKeyPair() }
                readBackupFileInfo(input, keypair).onResult({
                    rsrc.jsonObjectError(it)
                }, { info ->
                    val warns = ArrayList<String>()
                    JSONObject()
                        .put(Key.result, info.aliasCount)
                        .put(Key.version, info.version)
                        .put(Key.key, info.alias)
                        .put(Key.expire, info.expire)
                        .put(Key.checksum, info.sha256.second)
                        .put(Key.warns, JSONArray(warns))
                })
            }
        } ?: return rsrc.jsonObjectError(R.string.ErrorReadingBackup)
    }

    fun forwardBackup(backupfile: IFileInfo, aliases: List<String>): JSONObject {
        return Without.exceptionOrNull {
            val keypair = secUtil.invoke { it.getBackupKeyPair() }
            forwardBackup(backupfile, aliases, keypair)
        } ?: return rsrc.jsonObjectError(R.string.ErrorReadingBackup)
    }

    @Throws(SecureException::class)
    private fun forwardBackup(
        backupfile: IFileInfo,
        aliases: List<String>,
        keypair: BackupKeyPair,
    ): JSONObject {
        val errorReadingBackup = rsrc.jsonObjectError(R.string.ErrorReadingBackup)
        val (info, signedinfo, length) = backupfile.content().seekableInputStream()?.use { input ->
            val (info, endoffset) = readHeaderInfo(input, keypair) ?: return errorReadingBackup
            
            if (aliases.isEmpty()) return rsrc.jsonObjectError(R.string.InvalidArguments)
            val signedinfo = readPayload(input, endoffset, info) ?: return errorReadingBackup
            val length = input.getPosition().toInt()
            val headers = ByteArray(info.aliasCount * 512)
            input.readFullyAt(0, headers, 0, headers.size)
            Triple(info, signedinfo, length)
        } ?: return errorReadingBackup
        if (info.format.version() < ArchiveFormat.V13) {
            val cipher = info.format.createEncryptionCipher(info.key)
            val epayload =
                BU.encryptPadded(
                    cipher,
                    info.format.fileFormat(),
                    signedinfo.padding1Size,
                    signedinfo.padding2Size
                ) { aesout ->
                    val signer = secUtil.rsaSigner(keypair.privateKey)
                    val signed = info.format.signPayload(signer, signedinfo.payload)
                    ByteWriter(aesout).write32BEBytes(signed)
                }
            val padding1size = length - (aliases.size * 512) - epayload.size() -
                    (if (info.format.version() >= ArchiveFormat.V12) {
                        info.format.fileFormat().let { it.IV + 4 + 512 + it.SUM + it.PADSIZE }
                    } else 0)
            if (padding1size <= 0 || padding1size > info.format.PADDING1_MAX) return rsrc.jsonObjectError(R.string.BackupForwardNotFit)
            val keysig = U.sha256(secUtil.invoke { it.getBackupPublicKey() }.encoded)
            val newinfo = HeaderInfo(
                info.format,
                info.rsaKeyLength,
                info.directoryOffset,
                aliases.size,
                epayload.size(),
                padding1size,
                info.padding2Size,
                info.padding3Size,
                keysig,
                info.key,
                cipher,
                BU.serializeCipher(info.format, info.key, cipher),
            )
            val headers = ByteArrayOutputStream().use { out ->
                BU.writeEncryptedHeaders(secUtil, rsrc, out, aliases, newinfo)
                out
            }.toByteArray()
            val data = MyByteOutputStream().use { out ->
                out.write(headers)
                out.write(U.randBytes(padding1size))
                out.write(epayload.buffer(), 0, epayload.size())
                val signer = secUtil.rsaSigner(keypair.privateKey)
                out.write(info.format.signHeaders(signer, info.key, headers, signedinfo.payload))
                out
            }.toByteArray()
            if (data.size != length) {
                
                return errorReadingBackup
            }
            backupfile.content().seekableOutputStream(false)?.use { output ->
                output.writeAt(0, data)
            } ?: return rsrc.jsonObjectError(R.string.ErrorReadingBackup)
            return JSONObject()
        }
        return rsrc.jsonObjectError(R.string.UnsupportedOperation)
    }

    private fun restoreData1(
        backupfile: IFileInfo,
        destdir: IFileInfo,
        from: String,
        sync: Boolean
    ): BackupRestoreResult {
        return readBackupFile(backupfile) { cis, backupfileinfo, directory ->
            val format = backupfileinfo.format
            val filelist = format.readFilelist(rsrc, directory)
            val (rpaths, fromdir) = getFilelist(filelist, from)
            RestoreParam.RestoreParamBuilder(format, destdir, fromdir, sync).use { param ->
                restoreData3(cis, param, rpaths, filelist)
                BackupRestoreResult(
                    param.oks,
                    param.fails,
                    param.warns,
                    param.ignores,
                )
            }
        }
    }

    /// @param backupfiles Incremental backup files sorted in ascending order.
    private fun restoreData2(
        backupfiles: MutableList<IFileInfo>,
        destdir: IFileInfo,
        from: String,
        sync: Boolean
    ): BackupRestoreResult {
        val filelist = readBackupFilelist(
            backupfiles.lastOrNull()
                ?: throw errorReadingBackup()
        )
        val (rpaths, fromdir) = getFilelist(filelist, from)
        val result = BackupRestoreResult()
        val dstpaths = TreeSet<String>()
        var dstpaths1: Collection<String>? = null
        for (backupfile in backupfiles) {
            readBackupFile(backupfile) { cis, backupfileinfo, directory ->
                val format = backupfileinfo.format
                RestoreParamBuilder(format, destdir, fromdir, sync).use { param ->
                    val filelist1 = format.readFilelist(rsrc, directory)
                    dstpaths1 = restoreData3(cis, param, TreeSet(rpaths), filelist1).also {
                        dstpaths.addAll(it)
                    }
                    result.add(param)
                }
            }
        }
        dstpaths1?.let { paths ->
            for (rpath in dstpaths) {
                if (paths.contains(rpath)) continue
                destdir.fileInfo(rpath).deleteTree(null)
            }
        }
        return result
    }

    private fun verifyBackup1(backupfile: IFileInfo): BackupRestoreResult {
        return readBackupFile(backupfile) { cis, backupfileinfo, directory ->
            val format = backupfileinfo.format
            val filelist = format.readFilelist(rsrc, directory)
            val (rpaths) = getFilelist(filelist, "")
            val param = VerifyParam(format)
            val dirs = TreeMap<String, Info>()
            verifyData3(dirs, cis, param, rpaths, filelist)
            BackupRestoreResult(
                param.oks,
                param.fails,
                param.warns,
                param.ignores,
            )
        }
    }

    private fun verifyBackup2(backupfiles: MutableList<IFileInfo>): BackupRestoreResult {
        val filelist = readBackupFilelist(
            backupfiles.lastOrNull()
                ?: throw errorReadingBackup()
        )
        val (rpaths) = getFilelist(filelist, "")
        val result = BackupRestoreResult()
        val dirs = TreeMap<String, Info>()
        for (backupfile in backupfiles.reversed()) {
            readBackupFile(backupfile) { cis, backupfileinfo, directory ->
                val format = backupfileinfo.format
                val param = VerifyParam(format)
                val filelist1 = format.readFilelist(rsrc, directory)
                verifyData3(dirs, cis, param, rpaths, filelist1)
                result.add(param)
            }
        }
        return result
    }

    private fun getFilelist(infos0: MutableMap<String, Info>, from: String): Pair<MutableCollection<String>, String> {
        val frominfo = infos0[from]
            ?: throw rsrc.secureException(R.string.NotFound_, from)
        val rpaths = TreeSet<String>()
        val fromdir: String
        var totalsize = 0L
        if (frominfo.size >= 0) {
            rpaths.add(from)
            val dir = Basepath.dir(from)
            fromdir = if (dir.isNullOrEmpty()) "" else "$dir/"
            totalsize += frominfo.size
        } else {
            fromdir = if (from.isEmpty()) from else "$from/"
            for ((key, info) in infos0.entries) {
                val rpath = Basepath.cleanRpath(key)
                if (rpath != null && rpath.startsWith(fromdir)) {
                    rpaths.add(rpath)
                    if (info.size > 0) totalsize += info.size
                }
            }
        }
        return Pair(rpaths, fromdir)
    }

    /// @fromdir With trailing /.
    @Throws(Exception::class)
    private fun restoreData3(
        input: MySeekableInputStream,
        param: RestoreParam,
        rpaths: MutableCollection<String>,
        filelist: MutableMap<String, Info>
    ): Set<String> {
        fun ensureParentIsADir(file: IFileInfo): Boolean {
            var parent: IFileInfo? = file.parent
            while (true) {
                if (parent == null || parent.isDir)
                    return true
                if (parent.exists && !parent.delete())
                    return false
                parent = parent.parent
            }
        }

        fun prepareToWriteFile(file: IFileInfo): Boolean {
            if (file.isFile)
                return true
            if (file.exists)
                return file.deleteTree(null)
            if (!ensureParentIsADir(file))
                return false
            return file.mkparent()
        }

        fun prepareDir(dir: IFileInfo, info: Info): Boolean {
            if (dir.isDir)
                return true
            if (dir.exists && !dir.delete())
                return false
            if (!ensureParentIsADir(dir))
                return false
            val ok = dir.mkdirs()
            if (ok) dir.setLastModified(info.timestamp)
            return ok
        }

        val dstpaths = TreeSet<String>()
        param.restoredir.root.transaction {
            for (rpath in ArrayList(rpaths)) {
                val info = filelist[rpath]
                if (info == null || !(rpath.startsWith(param.fromdir))) {
                    rpaths.remove(rpath)
                    continue
                }
                fun fail(rpath: String, e: Throwable?) {
                    
                    rpaths.remove(rpath)
                    param.fail(rpath)
                }
                try {
                    val dstpath = rpath.substring(param.fromdir.length)
                    val dst = param.restoredir.fileInfo(dstpath)
                    dstpaths.add(dstpath)
                    if (info.isDir) {
                        rpaths.remove(rpath)
                        if (!prepareDir(dst, info)) {
                            fail(rpath, null)
                        }
                    } else if (info.isFile && info.offset != 0L) {
                        if (!prepareToWriteFile(dst)) {
                            fail(rpath, null)
                        }
                    }
                } catch (e: Exception) {
                    fail(rpath, e)
                }
            }
            val lock = Any()
            val group = CountedTaskGroup(rpaths.size)
            for (rpath in ArrayList(rpaths)) {
                val info = filelist[rpath]
                if (info == null || info.offset == 0L || !info.isFile) {
                    group.leave(group.enter())
                    continue
                }
                workerThreadPool.submit(group) {
                    ShadowSeekableInputStream(input, lock).use { sin ->
                        try {
                            val dst = param.restoredir.fileInfo(rpath.substring(param.fromdir.length))
                            val stat = dst.stat()
                            param.filePool.use X@{ tmpfile ->
                                val actual = param.bufPool.use { tmpbuf ->
                                    param.bufPool.use { diffbuf ->
                                        if (stat != null
                                            && (!dorestore(param.sync, stat, info)
                                                    || issame(stat, dst, info, tmpbuf, diffbuf, sin))
                                        ) {
                                            param.ignore(rpath)
                                            if (info.timestamp != stat.lastModified) {
                                                dst.setLastModified(info.timestamp)
                                            }
                                            null
                                        } else {
                                            sin.setPosition(info.offset)
                                            restoreFile(tmpfile, sin, param, tmpbuf) ?: run {
                                                param.ignore(rpath)
                                                null
                                            }
                                        }
                                    }
                                } ?: return@X
                                if (info.checksum != null && !actual.contentEquals(info.checksum))
                                    throw errorReadingBackup()
                                if (!tmpfile.exists || !tmpfile.content().renameTo(dst, info.timestamp))
                                    throw SecureException(rsrc.actionFailed(R.string.Restore))
                                param.ok(rpath)
                            }
                        } catch (e: Exception) {
                            
                            param.fail(rpath)
                        }
                    }
                }
            }
            group.awaitDone(1, TimeUnit.DAYS)
        }
        return dstpaths
    }

    @Throws(Exception::class)
    private fun verifyData3(
        dirs: MutableMap<String, Info>,
        input: MySeekableInputStream,
        param: VerifyParam,
        rpaths: MutableCollection<String>,
        filelist: MutableMap<String, Info>
    ) {
        for (rpath in ArrayList(rpaths)) {
            val info = filelist[rpath] ?: continue
            if (info.isDir) {
                if (!dirs.containsKey(rpath)) dirs.put(rpath, info)
                rpaths.remove(rpath)
                continue
            }
            if (!info.isFile) continue
            if (info.offset == 0L) continue
            rpaths.remove(rpath)
            try {
                input.setPosition(info.offset)
                val actual = verifyFile(StayOpenInputStream(input), param) ?: continue
                if (info.checksum != null && !actual.contentEquals(info.checksum))
                    throw SecureException(rsrc.actionFailed(R.string.BackupVerify))
                param.ok(rpath)
            } catch (e: Exception) {
                
                param.fail(rpath)
            }
        }
    }

    private fun dorestore(sync: Boolean, stat: IFileStat, info: Info): Boolean {
        if (sync) return info.timestamp > stat.lastModified
        return (stat.lastModified != info.timestamp || stat.length != info.size)
    }

    private fun issame(
        stat: IFileStat,
        file: IFileInfo,
        info: Info,
        tmpbuf: ByteArray,
        diffbuf: ByteArray,
        input: MySeekableInputStream
    ): Boolean {
        if (stat.length != info.size) return false
        val checksum = stat.checksumBytes
        if (checksum != null && info.checksum != null && checksum.size == info.checksum.size) {
            if (!checksum.contentEquals(info.checksum)) return false
        }
        try {
            var diff = false
            input.setPosition(info.offset)
            BU.readtag(rsrc, input)
            file.content().inputStream().use { b ->
                BU.readBlocks(rsrc, tmpbuf, input) {
                    val bn = IOUt.readWhilePossible(b, diffbuf, 0, it)
                    if (it != bn || !StructUt.equals(tmpbuf, 0, it, diffbuf, 0, bn)) {
                        diff = true
                        false
                    } else true
                }
            }
            return !diff
        } catch (e: Exception) {
            
            return false
        }
    }

    private fun restoreFile(outfile: IFileInfo, input: InputStream, param: RestoreParam, tmpbuf: ByteArray): ByteArray? {
        when (BU.readtag(rsrc, input)) {
            Kind.Data -> {
                return BU.copyBlocks(rsrc, outfile, param, tmpbuf, input)
            }

            Kind.Etc -> {
                BU.skipBlocks(input)
                return null
            }

            else -> throw errorReadingBackup()
        }
    }

    private fun verifyFile(input: InputStream, param: VerifyParam): ByteArray? {
        when (BU.readtag(rsrc, input)) {
            Kind.Data -> {
                return BU.verifyBlocks(rsrc, param, input)
            }

            Kind.Etc -> {
                BU.skipBlocks(input)
                return null
            }

            else -> throw errorReadingBackup()
        }
    }

    internal fun readFileAt(
        offset: Long,
        input: MySeekableInputStream,
        format: IArchiveFormat,
        checksum: ByteArray?
    ): InputStream? {
        try {
            input.setPosition(offset)
            val param = VerifyParam(format)
            val digester = format.createDigester()
            return MyByteOutputStream().use { output ->
                readFile(output, StayOpenInputStream(input), param, digester)
                if (checksum != null && !digester.digest().contentEquals(checksum)) return null
                output
            }.inputStream()
        } catch (e: Throwable) {
            
            return null
        }
    }

    private fun readFile(output: OutputStream, input: InputStream, param: VerifyParam, digester: MessageDigest) {
        when (BU.readtag(rsrc, input)) {
            Kind.Data -> {
                DigestOutputStream(output, digester).use { out ->
                    param.bufPool.use { tmpbuf ->
                        BU.readBlocks(rsrc, tmpbuf, input) {
                            out.write(tmpbuf, 0, it)
                            true
                        }
                    }
                }
            }

            else -> throw errorReadingBackup()
        }
    }

    @Throws(SecureException::class)
    fun getLastBackupInfos(backupfile: IFileInfo): MutableMap<String, Info>? {
        if (!backupfile.name.endsWith(Suffix.IBACKUP)) return null
        //// Find previous ibackup.
        val dir = backupfile.parent ?: return null
        if (!dir.exists) return null
        val lastibackup = BU.getLastBackupFile(dir, backupfile.name) ?: return null
        return readBackupFilelist(lastibackup)
    }

    fun readBackupFilelist(backupfile: IFileInfo): MutableMap<String, Info> {
        return openBackupInputStream(backupfile) { _, backupfileinfo, directory ->
            val format = backupfileinfo.format
            format.readFilelist(rsrc, directory)
        }
    }

    fun actionBackupData1(
        storage: IStorage,
        st: IStorageAccessor,
        backupfile: IFileInfo,
        srcdir: IFileInfo,
        aliases: Array<String>,
    ): IBotResult<BackupRestoreResult, JSONObject> {
        val rsrc = storage.rsrc
        if (backupfile.cpath.startsWith(srcdir.cpath + FS)) {
            return BotResult.fail(rsrc.jsonObjectError(R.string.BackupFileMustNotUndirSourceDirectory))
        }
        return try {
            val result = st.backupData(backupfile, aliases.toList(), srcdir)
            BotResult.ok(result)
        } catch (e: Throwable) {
            backupfile.delete()
            val msg = rsrc.actionFailed(
                if (backupfile.name.endsWith(Suffix.ZIP))
                    R.string.Export else R.string.Backup
            )
            BotResult.fail(rsrc.jsonObjectError(e, msg))
        }
    }

    fun actionBackupConversion0(
        storage: IStorage,
        st: IStorageAccessor,
        dstpath: String,
        srcpath: String,
        aliases: Array<String>,
        cut: Boolean,
    ): IBotResult<BackupRestoreResult, JSONObject> {
        return actionBackupConversion1(storage, st, dstpath, srcpath).mapOK { (dst, src) ->
            val rsrc = storage.rsrc
            val backupresult = if (src is ICloseableRootInfo) {
                src.use { actionBackupData1(storage, st, dst, it, aliases) }
            } else if (dst.lcSuffix == Suffix.BACKUP) {
                actionBackupData1(storage, st, dst, src, aliases)
            } else {
                return@mapOK BotResult.fail(rsrc.jsonObjectError(rsrc.actionFailed(R.string.Backup)))
            }
            backupresult.ifOK { result ->
                src.stat()?.let { dst.setLastModified(it.lastModified) }
                if (cut && result.fails.isEmpty()) {
                    src.delete()
                }
            }
            backupresult
        }
    }

    fun actionBackupConversion1(
        storage: IStorage,
        st: IStorageAccessor,
        dstpath: String,
        srcpath: String,
    ): IBotResult<Pair<IFileInfo, IFileInfo>, JSONObject> {
        val rsrc = storage.rsrc
        val srcfile = storage.fileInfoAt(srcpath).let {
            it.result() ?: return BotResult.fail(rsrc.jsonObjectError(it.failure()!!))
        }
        val dstfile = storage.fileInfoAt(dstpath).let {
            it.result() ?: return BotResult.fail(rsrc.jsonObjectError(it.failure()!!))
        }
        if (dstfile.cpath == srcfile.cpath)
            return BotResult.fail(rsrc.jsonObjectError(R.string.DestinationMustNotBeSameAsSource))
        val lcsrc = srcfile.lcSuffix
        val lcdst = dstfile.lcSuffix
        if (dstfile.stat()?.isFile == false)
            return BotResult.fail(rsrc.jsonObjectInvalidPath(dstpath))
        if (srcfile.stat()?.isFile != true)
            return BotResult.fail(rsrc.jsonObjectInvalidPath(srcpath))
        if (!(lcdst == Suffix.ZIP && lcsrc == Suffix.BACKUP
                    || lcdst == Suffix.BACKUP && (lcsrc == Suffix.ZIP || lcsrc == Suffix.BACKUP))
        )
            return BotResult.fail(rsrc.jsonObjectError(R.string.UnsupportedOperation))
        val src = if (lcsrc == Suffix.ZIP) srcfile else {
            st.backupFileRoot(srcfile) ?: return BotResult.fail(rsrc.jsonObjectInvalidPath(srcpath))
        }
        return BotResult.ok(Pair(dstfile, src))
    }

    fun actionVerifyBackup1(st: IStorageAccessor, backupfile: IFileInfo): BackupRestoreResult {
        val fails = ConcurrentLinkedDeque<String>()
        val oks = ConcurrentLinkedDeque<String>()
        TaskUt.forkJoinTasks { pool ->
            backupfile.walk3 { file, rpath, stat ->
                if (!stat.isFile) return@walk3
                val lcsuffix = Basepath.lcSuffix(file.name)
                if (!MimeUtil.isBackupLcSuffix(lcsuffix)) return@walk3
                pool.submit {
                    try {
                        if (lcsuffix == Suffix.ZIP) {
                            val result = verifyZip(file)
                            if (result.fails.isNotEmpty()) fails.add(rpath) else oks.add(rpath)
                        } else if (lcsuffix == Suffix.BACKUP || lcsuffix == Suffix.IBACKUP) {
                            val result = st.verifyBackup(file)
                            if (result.fails.isNotEmpty()) fails.add(rpath) else oks.add(rpath)
                        }
                    } catch (e: Throwable) {
                        fails.add(rpath)
                    }
                }
            }
        }
        return BackupRestoreResult(oks, fails)
    }

}

class BackupRestoreResult constructor(
    oks: Collection<String>? = null,
    fails: Collection<String>? = null,
    warns: Collection<String>? = null,
    ignores: Collection<String>? = null
) {
    val oks: MutableCollection<String> = TreeSet()
    val fails: MutableCollection<String> = TreeSet()
    val warns: MutableCollection<String> = ArrayList()
    val ignores: MutableCollection<String> = TreeSet()

    init {
        if (oks != null) this.oks.addAll(oks)
        if (fails != null) this.fails.addAll(fails)
        if (warns != null) this.warns.addAll(warns)
        if (ignores != null) this.ignores.addAll(ignores)
    }

    fun add(param: ParamBase): BackupRestoreResult {
        this.oks.addAll(param.oks)
        this.fails.addAll(param.fails)
        this.warns.addAll(param.warns)
        this.ignores.addAll(param.ignores)
        return this
    }
}

internal
class BackupFileInfo constructor(
    val format: IArchiveFormat,
    val aliasSlots: Int,
    val aliasCount: Int,
    val alias: String,
    val cert: Certificate,
) {
    val version get() = format.version()
    val expire get() = ISecUtil.dateOf(cert)
    val sha256 get() = ISecUtil.sha256Of(cert)

}

abstract class ParamBase(
    private val bufsize: Int,
) {
    private val _oks = ConcurrentSkipListSet<String>()
    private val _fails = ConcurrentSkipListSet<String>()
    private val _warns = ConcurrentLinkedQueue<String>()
    private var _ignores = ConcurrentLinkedQueue<String>()

    val bufPool = ObjectPool(object : ObjectPoolDelegate<ByteArray>() {
        override fun ctor(): ByteArray {
            return ByteArray(bufsize)
        }
    })

    val oks: Collection<String> get() = this._oks
    val fails: Collection<String> get() = this._fails
    val warns: Collection<String> get() = this._warns
    val ignores: Collection<String> get() = this._ignores

    fun ok(msg: String) {
        _oks.add(msg)
    }

    fun ignore(msg: String) {
        _ignores.add(msg)
    }

    fun fail(msg: String) {
        _fails.add(msg)
    }

    fun warn(msg: String) {
        _warns.add(msg)
    }
}

private class BackupParam constructor(
    val format: IArchiveFormat,
    val incremental: Boolean,
    val oinfos: MutableMap<String, Info>,
    vararg val roots: IFileInfo
) : ParamBase(K.BUFSIZE16) {
    val enableCompression = true
    val copybuf = ByteArray(K.BUFSIZE4)
}

private class RestoreParam private constructor(
    val format: IArchiveFormat,
    val restoredir: IFileInfo,
    fromdir: String,
    val sync: Boolean
) : ParamBase(K.BUFSIZE16), AutoCloseable {
    val fromdir = Basepath.cleanRpath(fromdir) ?: throw IOException()
    private val tmpdir = TmpUt.tmpdir(restoredir.root)

    class RestoreParamBuilder constructor(
        format: IArchiveFormat,
        restoredir: IFileInfo,
        fromdir: String,
        sync: Boolean
    ) {
        private val param = RestoreParam(format, restoredir, fromdir, sync)
        fun <R> use(code: Fun11<RestoreParam, R>): R {
            return param.use(code)
        }
    }

    val filePool = ObjectPool(object : ObjectPoolDelegate<IFileInfo>() {
        override fun ctor(): IFileInfo {
            return TmpUt.tmpfile(tmpdir)
        }

    })

    override fun close() {
        TmpUt.deleteTree(tmpdir)
    }
}

private class VerifyParam constructor(
    val format: IArchiveFormat
) : ParamBase(K.BUFSIZE16)

private object BU {

    fun serializeCipher(format: IArchiveFormat, key: SecretKey, cipher: Cipher): ByteArray {
        return MyByteOutputStream().use {
            format.writeCipher(it, key, cipher)
            it
        }.toByteArray()
    }

    fun importPublicKey(keyfile: IFileInfo): PublicKey? {
        val factory = CertificateFactory.getInstance("X509")
        keyfile.content().inputStream().use {
            val certpath = factory.generateCertPath(it)
            val cert = certpath.certificates.firstOrNull()
            return cert?.publicKey
        }
    }

    fun createEncryptedSeekableInputStream(
        seekable: MySeekableInputStream,
        key: SecretKey,
        format: IFileFormat
    ): MySeekableInputStream {
        val blocksize = IOUt.readU8(seekable) * K.ENCRYPTED_BLOCK_SIZE_MULTIPIER
        val cipher = format.readCipher(seekable, key)
        return EncryptedInputStream(seekable, key, format, blocksize, cipher, seekable.getPosition())
    }

    @Throws(IOException::class)
    fun readGzFilelist(input: InputStream, limit: Int, digester: MessageDigest): Pair<ByteArray, ByteArray> {
        if (input.read() != Tag.FilelistGz.toInt())
            throw IOException()
        val len = U.read32BELength(input, limit)
        val ret = DigestInputStream(LimitedInputStream(input, len.toLong()), digester).use { dis ->
            GZIPInputStream(dis).use { gis ->
                gis.readBytes()
            }
        }
        val expected = U.readU8Bytes(input)
        if (!expected.contentEquals(digester.digest()))
            throw IOException()
        return Pair(ret, expected)
    }

    fun encryptPadded(cipher: Cipher, format: IPadding, content: Fun10<OutputStream>): MyByteOutputStream {
        return U.writeByteArray { encryptPadded(it, cipher, format, content) }
    }

    fun encryptPadded(
        cipher: Cipher,
        format: IPadding,
        padding1: Int,
        padding2: Int,
        content: Fun10<OutputStream>
    ): MyByteOutputStream {
        return U.writeByteArray { encryptPadded(it, cipher, format, padding1, padding2, content) }
    }

    fun encryptPadded(output: OutputStream, cipher1: Cipher, format: IPadding, content: Fun10<OutputStream>) {
        CipherOutputStream(output, cipher1).use {
            format.writePadding(it)
            content(it)
            format.writePadding(it)
        }
    }

    fun encryptPadded(
        output: OutputStream,
        cipher1: Cipher,
        format: IPadding,
        padding1: Int,
        padding2: Int,
        content: Fun10<OutputStream>
    ) {
        CipherOutputStream(output, cipher1).use {
            format.writePadding(it, padding1)
            content(it)
            format.writePadding(it, padding2)
        }
    }

    fun <T> readPadded(data: ByteArray, cipher: Cipher, format: IPadding, content: Fun11<InputStream, T>): Triple<T, Int, Int> {
        return U.readByteArray(data) { readPadded(it, cipher, format, content) }
    }

    fun <T> readPadded(
        input: InputStream,
        cipher: Cipher,
        format: IPadding,
        content: Fun11<InputStream, T>
    ): Triple<T, Int, Int> {
        return CipherInputStream(input, cipher).use {
            val padding1 = format.readPadding(it) - 1 - format.checksumSize
            val ret = content(it)
            val padding2 = format.readPadding(it) - 1 - format.checksumSize
            Triple(ret, padding1, padding2)
        }
    }

    @Throws(Exception::class)
    fun writeExportDirChildren(
        out: ZipOutputStream,
        result: BackupRestoreResult,
        dir: IFileInfo,
        rpathx: String,
        usenobackup: Boolean
    ): BackupRestoreResult {
        val files = FileInfoUtil.filesByName(dir)
        for (file in files) {
            if (file.isDir) {
                val rpath = Basepath.joinRpath(rpathx, file.name)
                writeExportDir(out, result, file, rpath, usenobackup)
            }
        }
        for (file in files) {
            if (file.isFile) {
                val rpath = Basepath.joinRpath(rpathx, file.name)
                writeExportFile(out, result, file, rpath)
            }
        }
        return result
    }

    private fun writeExportDir(
        out: ZipOutputStream,
        result: BackupRestoreResult,
        dir: IFileInfo,
        rpathx: String,
        usenobackup: Boolean
    ) {
        if (usenobackup && dir.fileInfo(DEF.nobackup).exists) return
        val e = FileUt.zipentry(rpathx, true)
        e.lastModifiedTime = FileTime.fromMillis(dir.stat()!!.lastModified)
        out.putNextEntry(e)
        out.closeEntry()
        writeExportDirChildren(out, result, dir, rpathx, usenobackup)
    }

    @Throws(Exception::class)
    private fun writeExportFile(
        out: ZipOutputStream,
        result: BackupRestoreResult,
        file: IFileInfo,
        rpathx: String
    ) {
        try {
            val stat = file.stat()!!
            val e = FileUt.zipentry(rpathx)
            val method = ZipEntry.DEFLATED
            e.method = method
            e.size = stat.length
            e.lastModifiedTime = FileTime.fromMillis(stat.lastModified)
            out.putNextEntry(e)
            file.content().inputStream().use {
                FileUt.copy(out, it)
            }
            out.closeEntry()
            result.oks.add(rpathx)
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
                    len += U.skipInt32Bytes(input, K.BACKUP_BLOCKSIZE) + 1
                    len += U.skipU8Bytes(input) + 1
                }

                Tag.BlocksEnd.toInt() -> return len + 1
                else -> throw IOException()
            }
        }
    }

    //////////////////////////////////////////////////////////////////////

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
    fun backupData(
        cout: PositionTrackingOutputStream,
        secUtil: ISecUtil,
        rsrc: IResUtil,
        param: BackupParam,
        siginfo: SigInfo?
    ): ByteArray? {
        param.format.fileFormat().writePadding(cout)
        val data = MyByteOutputStream(K.BUFSIZE4).use { bos ->
            GZIPOutputStream(bos).use { gos ->
                for (root in param.roots) {
                    if (root.stat()?.isFile == true) {
                        val lc = Basepath.lcSuffix(root.name)
                        if (lc == Suffix.ZIP) BU.backupZip1(gos, cout, param, root)
                        else param.ignore(root.name)
                    } else BU.backupData1(gos, cout, param, root)
                }
                IOUt.writeByte(gos, Tag.FilelistEnd)
            }
            bos
        }
        val digester = param.format.createDigester()
        digester.update(data.buffer(), 0, data.size())
        val checksum = digester.digest()
        val offset = cout.getPosition()
        val w = ByteWriter(cout)
            .write(Tag.FilelistGz)
            .write32BEBytes(data.buffer(), 0, data.size())
            .writeU8Bytes(checksum)
        return if (param.format.version() >= ArchiveFormat.V14 && siginfo != null) {
            siginfo.info.directoryOffset = offset
            val headers = ByteArrayOutputStream().use {
                BU.writeEncryptedHeaders(secUtil, rsrc, it, siginfo.aliases, siginfo.info)
                it
            }.toByteArray()
            val signer = secUtil.rsaSigner(siginfo.keypair.privateKey)
            w.write32BEBytes(
                param.format.signHeaders(
                    signer, siginfo.info.key, headers, siginfo.payload, checksum
                )
            )
            
            headers
        } else {
            if (siginfo != null) {
                val signer = secUtil.rsaSigner(siginfo.keypair.privateKey)
                w.write32BEBytes(
                    param.format.signHeaders(
                        signer, siginfo.info.key, siginfo.headers, siginfo.payload, checksum
                    )
                )
            }
            w.write64BE(offset)
            
            null
        }
    }

    fun writeEncryptedHeaders(
        secutil: ISecUtil,
        rsrc: IResUtil,
        out: OutputStream,
        aliases: List<String>,
        info: BackupUtil.HeaderInfo,
    ) {
        val header = createHeader(info)
        for (alias in aliases) {
            out.write(encryptHeader(secutil, rsrc, alias, header, info.rsaKeyLength))
        }
    }

    private fun encryptHeader(
        secutil: ISecUtil,
        rsrc: IResUtil,
        alias: String,
        header: MyByteOutputStream,
        rsakeylen: Int
    ): ByteArray {
        val padding = V10Padding()
        val cert = secutil.invoke { it.getPublicKey(alias) }
            ?: throw rsrc.secureException(R.string.BackupKeyInvalid, ": $alias")
        val rsa = secutil.rsaEncryptionCipher(cert.publicKey)
        val b = MyByteOutputStream().use {
            padding.writePadding(it)
            it.write(header.buffer(), 0, header.size())
            it.write(U.sha256(it.buffer(), 0, it.size()))
            it
        }
        
        if (b.size() >= rsakeylen / 8 - 16) {
            throw SecureException()
        }
        val a = U.random(512)
        rsa.doFinal(b.buffer(), 0, b.size(), a)
        return a
    }

    private fun createHeader(info: BackupUtil.HeaderInfo): MyByteOutputStream {
        return MyByteOutputStream().use { output ->
            val w = ByteWriter(output)
                .write(ArchiveFormat.MAGIC)
                .writeU8(info.format.version())
                .writeU8(info.aliasCount)
                .write32BE(info.epayloadSize)
            info.format.writePaddings(output, info.padding1Size, info.padding2Size, info.padding3Size)
            if (info.format.version() >= ArchiveFormat.V14) {
                w.write63UV(info.directoryOffset)
            }
            w.writeU8Bytes(info.keySig)
            output.write(info.cipherdata)
            output
        }
    }

    fun backupZip1(filelist: OutputStream, out: PositionTrackingOutputStream, param: BackupParam, zipfile: IFileInfo) {
        val outinfo = TreeMap<String, Pair<Long, ByteArray>>()
        val filetree = BackupUtil.listZip(zipfile) { entry, contentprovider ->
            if (entry.isDirectory) return@listZip 0L
            val rpath = Basepath.cleanRpath(entry.name) ?: return@listZip 0L
            val offset = out.getPosition()
            val (checksum, size) = writeBackupZipEntry(out, param, Kind.Data, rpath, contentprovider())
            outinfo.put(rpath, Pair(offset, checksum))
            return@listZip size
        }

        fun writefilelist(w: ByteWriter, dir: IFileInfo, dirpath: String) {
            for (file in dir.filesOrEmpty()) {
                val rpath = Basepath.joinRpath(dirpath, file.name)
                if (file.isDir) {
                    w.write(Tag.Dir)
                    w.write32BEBytes(file.name.toByteArray())
                    w.write63UV((file.stat()?.lastModified ?: 0L))
                    writefilelist(w, file, rpath)
                    w.write(Tag.DirEnd)
                    continue
                }
                val info = outinfo.get(rpath)
                val stat = file.stat()
                if (info == null || stat == null) {
                    param.fail(rpath)
                    continue
                }
                w.write(Tag.File2)
                w.write32BEBytes(file.name.toByteArray())
                w.write63UV((stat.lastModified))
                w.write63UV((stat.length))
                val oinfo = param.oinfos.remove(rpath)
                if (dobackup(param, oinfo, stat)) {
                    w.writeU8Bytes(info.second)
                    w.write63UV((info.first))
                    param.ok(rpath)
                } else {
                    w.writeU8Bytes(oinfo?.checksum ?: Empty.byteArray)
                    w.write63UV((0))
                    param.ignore(rpath)
                }
            }
        }

        val srctree = object : ReadOnlyJSONRoot(filetree, "", ReadOnlyJSONRoot::createFileInfoTree) {
            override fun inputStream(rpath: String): InputStream {
                return NullInputStream.singleton
            }
        }
        writefilelist(ByteWriter(filelist), srctree, "")
    }

    @Throws(IOException::class)
    fun backupData1(filelist: OutputStream, out: PositionTrackingOutputStream, param: BackupParam, srcdir: IFileInfo) {
        if (param.incremental && srcdir.fileInfo(DEF.nobackup).exists) return
        backupDataDir(filelist, out, param, srcdir, "")
    }

    @Throws(IOException::class)
    private fun backupDataDir(
        filelist: OutputStream,
        out: PositionTrackingOutputStream,
        param: BackupParam,
        dir: IFileInfo,
        dirpath: String
    ) {
        val w = ByteWriter(filelist)
        for (info in FileInfoUtil.filesByName(dir)) {
            val stat = info.stat() ?: continue
            val rpath = Basepath.joinRpath(dirpath, info.name)
            try {
                if (stat.isDir) {
                    if (param.incremental && info.fileInfo(DEF.nobackup).exists) continue
                    w.write(Tag.Dir)
                        .write32BEBytes(info.name.toByteArray())
                        .write63UV((stat.lastModified))
                    backupDataDir(filelist, out, param, info, rpath)
                    w.write(Tag.DirEnd)
                } else if (stat.isFile) {
                    w.write(Tag.File2)
                    w.write32BEBytes(info.name.toByteArray())
                    w.write63UV((stat.lastModified))
                    w.write63UV((stat.length))
                    val oinfo = param.oinfos.remove(rpath)
                    if (dobackup(param, oinfo, stat)) {
                        val offset = out.getPosition()
                        val checksum = writeBackupFile(out, param, Kind.Data, info)
                        w.writeU8Bytes(checksum)
                        w.write63UV((offset))
                        param.ok(rpath)
                    } else {
                        w.writeU8Bytes(oinfo?.checksum ?: Empty.byteArray)
                        w.write63UV((0))
                        param.ignore(rpath)
                    }
                }
            } catch (e: java.lang.Exception) {
                
                param.fail(rpath)
            }
        }
    }

    private fun dobackup(param: BackupParam, oinfo: Info?, stat: IFileStat): Boolean {
        if (!param.incremental || oinfo == null) return true
        val checksum = stat.checksumBytes
        return stat.lastModified != oinfo.timestamp
                || stat.length != oinfo.size
                || checksum != null && !checksum.contentEquals(oinfo.checksum ?: Empty.byteArray)
    }

    @Throws(IOException::class)
    private fun writeBackupFile(out: OutputStream, param: BackupParam, kind: Byte, file: IFileInfo): ByteArray {
        return DigestInputStream(file.content().inputStream(), param.format.createDigester()).use { input ->
            try {
                val compressing = param.enableCompression && isCompressing(file.cpath)
                writeBlocks(out, param, compressing, kind, input)
                input.messageDigest.digest()
            } catch (e: IOException) {
                
                throw e
            }
        }
    }

    private fun writeBackupZipEntry(
        out: OutputStream,
        param: BackupParam,
        kind: Byte,
        rpath: String,
        entry: InputStream
    ): Pair<ByteArray, Long> {
        return DigestInputStream(entry, param.format.createDigester()).use { input ->
            try {
                val compressing = param.enableCompression && isCompressing(rpath)
                val size = writeBlocks(out, param, compressing, kind, input)
                val checksum = input.messageDigest.digest()
                Pair(checksum, size)
            } catch (e: IOException) {
                
                throw e
            }
        }
    }

    @Throws(IOException::class)
    fun writeBlocks(output: OutputStream, param: BackupParam, compressing: Boolean, kind: Byte, content: InputStream): Long {
        IOUt.writeByte(output, kind)
        IOUt.writeByte(output, Tag.Blocks)
        return if (compressing) writeGzBlocks(output, param, content) else writeBlocks(output, param, content)
    }

    @Throws(IOException::class)
    private fun writeBlocks(out: OutputStream, param: BackupParam, input: InputStream): Long {
        var size = 0L
        param.bufPool.use { tmpbuf ->
            val w = ByteWriter(out)
            while (true) {
                val n = IOUt.readWhilePossible(input, tmpbuf)
                if (n > 0) {
                    size += n
                    w.write(Tag.Block)
                    w.write32BEBytes(tmpbuf, 0, n)
                }
                if (n < tmpbuf.size) {
                    w.write(Tag.BlocksEnd)
                    break
                }
            }
        }
        return size
    }

    @Throws(IOException::class)
    private fun writeGzBlocks(out: OutputStream, param: BackupParam, input: InputStream): Long {
        var size = 0L
        val w = ByteWriter(out)
        param.bufPool.use { buf ->
            while (true) {
                var count = 0
                val gzbuf = MyByteOutputStream().use { bos ->
                    GZIPOutputStream(bos).use { gos ->
                        LimitedInputStream(input, buf.size.toLong()).use { input ->
                            IOUt.copyAll(buf, input) { n ->
                                gos.write(buf, 0, n)
                                count += n
                            }
                        }
                    }
                    bos
                }
                size += count
                if (count > 0) {
                    w.write(Tag.BlockGz)
                    w.write32BEBytes(gzbuf.buffer(), 0, gzbuf.size())
                }
                if (count < buf.size)
                    break
            }
            w.write(Tag.BlocksEnd)
        }
        return size
    }

    fun readtag(res: IResUtil, input: InputStream): Byte {
        val tag = input.read()
        if (tag < 0) {
            throw res.secureException(R.string.ErrorReadingBackup)
        }
        return tag.toByte()
    }

    fun hasArchiveMagic(data: ByteArray): Boolean {
        return StructUt.equals(ArchiveFormat.MAGIC, data, 0, ArchiveFormat.MAGIC.size)
    }

    /// @return Set of all the .ibackup file names in the given directory in filename ascending order.
    fun getIBackupFilenames(filename: String?, dir: IFileInfo): Collection<String> {
        val ret = TreeSet<String>()
        if (filename != null) ret.add(filename)
        for (file in dir.filesOrEmpty()) {
            if (file.stat()?.isFile == true) {
                val name = file.name
                if (name.endsWith(Suffix.IBACKUP)) {
                    ret.add(name)
                }
            }
        }
        return ret
    }

    @Throws(IOException::class)
    fun copyBlocks(res: IResUtil, outfile: IFileInfo, param: RestoreParam, tmpbuf: ByteArray, input: InputStream): ByteArray {
        val digester = param.format.createDigester()
        outfile.content().outputStream().use { output ->
            readBlocks(res, tmpbuf, input) {
                output.write(tmpbuf, 0, it)
                digester.update(tmpbuf, 0, it)
                true
            }
        }
        return digester.digest()
    }

    /// Read blocks until Tag.BlockEnd.
    /// @param callback(ByteArray, Int): Boolean This is called on each tmpbuf worth of data read.
    /// The call parameters is the data and length of data read. The length may be anywhere from 0 to tmpbuf.size.
    /// If callback() return false, aborted immediately. Otherwise the blocks including Tag.BlockEnd
    /// would be consumed when the method returns.
    @Throws(IOException::class)
    fun readBlocks(rsrc: IResUtil, tmpbuf: ByteArray, input: InputStream, callback: Fun11<Int, Boolean>) {
        if (readtag(rsrc, input) != Tag.Blocks)
            throw rsrc.secureException(R.string.ErrorReadingBackup)
        val r = ByteReader(input)
        while (true) {
            when (readtag(rsrc, input)) {
                Tag.Block -> {
                    val len = r.read32BE().toLong()
                    if (!IOUt.copyFor(tmpbuf, input, len, callback)) return
                }

                Tag.BlockGz -> {
                    val len = r.read32BE().toLong()
                    if (!copyGzBlock(tmpbuf, input, len, callback)) return
                }

                Tag.BlocksEnd -> return
                else -> throw IOException()
            }
        }
    }

    @Throws(IOException::class)
    fun copyGzBlock(tmpbuf: ByteArray, input: InputStream, length: Long, collector: Fun11<Int, Boolean>): Boolean {
        return StayOpenLimitedInputStream(input, length).use { sin ->
            GZIPInputStream(sin).use { gis ->
                IOUt.copyWhile(tmpbuf, gis, collector)
            }
        }
    }

    @Throws(IOException::class)
    fun verifyBlocks(res: IResUtil, param: VerifyParam, input: InputStream): ByteArray {
        val digester = param.format.createDigester()
        param.bufPool.use { tmpbuf ->
            readBlocks(res, tmpbuf, input) {
                digester.update(tmpbuf, 0, it)
                true
            }
        }
        return digester.digest()
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

    fun writeByteArray(content: Fun10<OutputStream>): MyByteOutputStream {
        return MyByteOutputStream().use {
            content(it)
            it
        }
    }

    fun read32BELength(input: InputStream, limit: Int): Int {
        val len = ByteReader(input).read32BE()
        if (len < 0 || len > limit) {
            
            throw BeyondLimitException()
        }
        return len
    }

    @Throws(IOException::class)
    fun skipInt32Bytes(input: InputStream, limit: Int): Int {
        val len = ByteReader(input).read32BE()
        if (len < 0 || len > limit) {
            
            throw BeyondLimitException()
        }
        IOUt.skipFully(input, len.toLong())
        return len
    }

    @Throws(IOException::class)
    fun skipU8Bytes(input: InputStream): Int {
        val len = IOUt.readU8(input)
        IOUt.skipFully(input, len.toLong())
        return len
    }

    @Throws(IOException::class)
    fun readU8Bytes(input: IInputStream): ByteArray {
        val len = InputUt.readU8(input)
        return InputUt.readFully(input, ByteArray(len))
    }

    @Throws(IOException::class)
    fun readU8Bytes(input: InputStream): ByteArray {
        val len = IOUt.readU8(input)
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

    ///**

    //// A sanity check, caller must has perform more vigorous checks.
    fun isValidLoginPass(pass: String?): Boolean {
        return pass != null && pass.length >= 4 && isvalid(pass)
    }

    fun isvalid(pass: String): Boolean {
        var i = 0
        val len = pass.length
        while (i < len) {
            val c = pass[i]
            if (c.code <= 0x20 || c.code >= 0x7f) {
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
    const val BUFSIZE = 16 * 1024
    const val BUFSIZE2 = BUFSIZE * 2
    const val BUFSIZE4 = BUFSIZE * 4
    const val BUFSIZE16 = BUFSIZE * 16
    const val BACKUP_BLOCKSIZE = 4 * 1024 * 1024
    const val MIN_SALT_LENGTH = 16
    const val SHA1_BUFSIZE = 20
    const val SHA256_BUFSIZE = 32
    const val FILEPATH_BUFSIZE = 32 * 1024
    const val ENCRYPTED_BLOCK_SIZE_MULTIPIER = 4 * 1024
    const val ENCRYPTED_BLOCK_SIZE = 16 * ENCRYPTED_BLOCK_SIZE_MULTIPIER
    const val SEEKABLE_POOL_SIZE = 5
    const val RESTORE_TMPDIR = ".restore"
    val COMPRESSING = setOf("html", "css", "js", "svg", "pdf", "xml", "cf", "json", "properties")
}

interface IPadding {
    val checksumSize: Int

    fun padding(): ByteArray

    /// @return Total bytes read.
    @Throws(Exception::class)
    fun readPadding(input: InputStream): Int

    /// @return Total bytes written.
    @Throws(Exception::class)
    fun writePadding(output: OutputStream): Int

    /// @return Total bytes read.
    fun readPadding(input: InputStream, len: Int): Int

    /// @return Total bytes written.
    fun writePadding(output: OutputStream, len: Int): Int
}

open class V10Padding : IPadding {
    private val buf = ByteArray(256)
    private val sumBuf = ByteArray(K.SHA1_BUFSIZE)

    override val checksumSize: Int get() = sumBuf.size

    @Throws(Exception::class)
    override fun writePadding(output: OutputStream): Int {
        val u8 = U.randU8()
        val len = lengthFrom(u8)
        output.write(u8)
        return writePadding1(output, len) + 1
    }

    @Throws(Exception::class)
    override fun writePadding(output: OutputStream, len: Int): Int {
        val u8 = u8From(len)
        output.write(u8)
        return writePadding1(output, len) + 1
    }

    private fun writePadding1(output: OutputStream, len: Int): Int {
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
        val u8 = IOUt.readU8(input)
        val len = lengthFrom(u8)
        if (len < PADDING_MIN || len > PADDING_MAX) {
            throw IOException()
        }
        return len
    }

    private fun lengthFrom(u8: Int): Int {
        return PADDING_MIN + (u8 and PADDING_MASK)
    }

    private fun u8From(len: Int): Int {
        return U.randU8() and (PADDING_MASK xor 0xff) or ((len - PADDING_MIN) and PADDING_MASK)
    }

    companion object {
        const val PADDING_MIN = 16
        const val PADDING_MAX = 32
        const val PADDING_MASK = 0xf
    }
}

//////////////////////////////////////////////////////////////////////

interface IFileFormat : IPadding {
    val CONTENT_START: Int
    val IV: Int
    val SUM: Int
    val PREFIX: Int
    val PADSIZE: Int
    val PADMASK: Int
    val BLOCKSIZE: Int
    val OVERHEAD get() = IV + PREFIX + SUM + PADSIZE
    fun version(): Int
    fun createDigester(): MessageDigest
    fun createEncryptionCipher(key: SecretKey): Cipher
    fun createCipherOutputStream(out: OutputStream, cipher: Cipher): OutputStream
    fun writeCipher(output: OutputStream, cipher: Cipher)
    fun readCipher(input: IInputStream, key: SecretKey): Cipher
    fun initCipher(cipher: Cipher, key: SecretKey, iv: ByteArray)
    fun checksumBlock(out: OutputStream): Int
    fun verifyBlock(buffer: ByteArray, length: Int): Int
    /// Note that the input stream stay open on return.
    /// @input Must be at position ready to read the blocksize.
    fun unencryptedSizeOf(input: MySeekableInputStream, key: SecretKey, rawsize: Long): Long

    fun encodeBlocksize(): Int {
        return BLOCKSIZE / K.ENCRYPTED_BLOCK_SIZE_MULTIPIER
    }

    fun decodeBlocksize(encoded: Int): Int {
        return encoded * K.ENCRYPTED_BLOCK_SIZE_MULTIPIER
    }

    companion object {
        /// @input Postion should be 0, ready to read the MAGIC block.
        fun unencryptedSizeOf(input: MySeekableInputStream, key: SecretKey, rawsize: Long): Long {
            if (rawsize == 0L) return 0L
            try {
                val magic = IOUt.readFully(input, ByteArray(FileFormat.MAGIC.size))
                StructUt.equals(magic, FileFormat.MAGIC) ||
                        throw SecureException()
                val version = IOUt.readU8(input)
                val format = FileFormat.get(version)
                    ?: throw SecureException()
                return format.unencryptedSizeOf(input, key, rawsize)
            } catch (e: Exception) {
                return 0L
            }
        }
    }
}

/**
 * The IFileFormat factory.
 */
private object FileFormat {
    val MAGIC = byteArrayOf(0x61, 0x30, 0x63, 0x65)

    const val V10 = 10
    const val V11 = 11

    fun latestVersion(): Int {
        return V11
    }

    fun latestFormat(): IFileFormat {
        return get(latestVersion())
            ?: throw AssertionError()
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
}

private class FileFormatV10 : V10Padding(), IFileFormat {

    override val CONTENT_START = FileFormat.MAGIC.size + 2 + 4
    override val IV = 16
    override val PREFIX = 16
    override val SUM = 32
    override val PADSIZE = 16
    override val PADMASK = 0x0f
    override val BLOCKSIZE = K.ENCRYPTED_BLOCK_SIZE

    override fun version(): Int {
        return FileFormat.V10
    }

    override fun createDigester(): MessageDigest {
        return MessageDigest.getInstance("SHA-256")
    }

    override fun createEncryptionCipher(key: SecretKey): Cipher {
        val cipher = Cipher.getInstance(Best.CBC)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher
    }

    override fun createCipherOutputStream(out: OutputStream, cipher: Cipher): OutputStream {
        val ret = MyDigestedOutputStream(CipherOutputStream(out, cipher), createDigester())
        ret.write(U.randBytes(PREFIX))
        return ret
    }

    override fun writeCipher(output: OutputStream, cipher: Cipher) {
        ByteWriter(output)
            .write(Tag.Cipher.toInt())
            .writeU8Bytes(byteArrayOf(0))
            .writeU8Bytes(BU.marshalSalt(cipher.iv))
    }

    override fun readCipher(input: IInputStream, key: SecretKey): Cipher {
        if (input.read() != Tag.Cipher.toInt())
            throw SecureException()
        U.readU8Bytes(input)
        val iv = BU.unmarshalSalt(U.readU8Bytes(input))
        if (iv.size != IV)
            throw SecureException()
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
        out.write(digest)
        return digest.size
    }

    override fun verifyBlock(buffer: ByteArray, length: Int): Int {
        val digester = createDigester()
        digester.update(buffer, 0, length - SUM)
        val actual = digester.digest()
        val expected = buffer.copyOfRange(length - SUM, length)
        if (!actual.contentEquals(expected))
            throw SecureException()
        return length - PREFIX - SUM
    }

    override fun unencryptedSizeOf(
        input: MySeekableInputStream,
        key: SecretKey,
        rawsize: Long
    ): Long {
        try {
            val blocksize = IOUt.readU8(input) * K.ENCRYPTED_BLOCK_SIZE_MULTIPIER
            if (blocksize == 0) {
                return SimpleEncryptedInputStream.unencryptedSizeOf(this, input, rawsize)
            }
            val cipher = readCipher(PositionTrackingInputStreamAdapter(input), key)
            val pos = input.getPosition()
            return EncryptedInputStream(
                StayOpenSeekableInputStream(input),
                key,
                this,
                blocksize,
                cipher,
                pos
            ).use { it.getSize() }
        } catch (e: Exception) {
            return 0L
        }
    }
}

private class FileFormatV11 : V10Padding(), IFileFormat {
    override val CONTENT_START = FileFormat.MAGIC.size + 2 + 5
    override val IV = 12
    override val SUM = 16
    override val PREFIX = 8
    override val PADSIZE = 0
    override val PADMASK = 0
    override val BLOCKSIZE = K.ENCRYPTED_BLOCK_SIZE

    override fun version(): Int {
        return FileFormat.V11
    }

    override fun createDigester(): MessageDigest {
        return MessageDigest.getInstance("SHA-256")
    }

    override fun createEncryptionCipher(key: SecretKey): Cipher {
        val cipher = Cipher.getInstance(Best.GCM)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher
    }

    override fun createCipherOutputStream(out: OutputStream, cipher: Cipher): OutputStream {
        val ret = CipherOutputStream(out, cipher)
        ret.write(U.randBytes(PREFIX))
        return ret
    }

    override fun writeCipher(output: OutputStream, cipher: Cipher) {
        val param = cipher.parameters.getParameterSpec(GCMParameterSpec::class.java)
        ByteWriter(output)
            .write(Tag.Cipher.toInt())
            .writeU8Bytes(byteArrayOf(0))
            .writeU8(param.tLen)
            .writeU8Bytes(BU.marshalSalt(param.iv))
    }

    override fun readCipher(input: IInputStream, key: SecretKey): Cipher {
        if (input.read() != Tag.Cipher.toInt())
            throw SecureException()
        U.readU8Bytes(input)
        val tlen = InputUt.readU8(input)
        val iv = BU.unmarshalSalt(U.readU8Bytes(input))
        if (iv.size != IV)
            throw SecureException()
        val cipher = Cipher.getInstance(Best.GCM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(tlen, iv))
        return cipher
    }

    override fun initCipher(cipher: Cipher, key: SecretKey, iv: ByteArray) {
        if (iv.size != IV)
            throw SecureException()
        val param = cipher.parameters.getParameterSpec(GCMParameterSpec::class.java)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(param.tLen, iv))
    }

    override fun checksumBlock(out: OutputStream): Int {
        return 0
    }

    override fun verifyBlock(buffer: ByteArray, length: Int): Int {
        return length - PREFIX
    }

    override fun unencryptedSizeOf(
        input: MySeekableInputStream,
        key: SecretKey,
        rawsize: Long
    ): Long {
        try {
            val blocksize = IOUt.readU8(input) * K.ENCRYPTED_BLOCK_SIZE_MULTIPIER
            if (blocksize == 0) return rawsize - CONTENT_START - OVERHEAD
            return unencryptedSizeOf(blocksize, rawsize)
        } catch (e: Exception) {
            return 0L
        }
    }

    private fun unencryptedSizeOf(blocksize: Int, rawsize: Long): Long {
        val contentStart = CONTENT_START
        val n = (rawsize - contentStart) / (blocksize + OVERHEAD)
        val start = contentStart + n * (blocksize + OVERHEAD)
        val count = maxOf(0, rawsize - start - OVERHEAD)
        return n * blocksize + count
    }
}

internal
interface IArchiveFormat {
    val PADDING1_MAX: Int
    fun version(): Int
    fun iterations(): Int
    fun fileFormat(): IFileFormat
    fun createDigester(): MessageDigest
    fun createEncryptionCipher(key: SecretKey): Cipher
    fun writePaddings(output: OutputStream, padding1: Int, padding2: Int, padding3: Int)
    fun readPaddings(input: InputStream): Triple<Int, Int, Int>
    fun readCipher(input: IInputStream): Pair<SecretKey, Cipher>
    fun writeCipher(output: OutputStream, key: SecretKey, cipher: Cipher)
    fun signPayload(signer: Signature, payload: ByteArray): ByteArray
    fun verifyPayload(verifier: Signature, signed: ByteArray): ByteArray?
    fun signHeaders(signer: Signature, key: SecretKey, vararg blobs: ByteArray): ByteArray
    fun verifyHeaders(
        input: InputStream,
        size: Long,
        verifier: Signature,
        keylen: Int,
        cipher: Cipher,
        key: SecretKey,
        vararg blobs: ByteArray
    ): Boolean

    @Throws(SecureException::class)
    fun readFilelist(rsrc: IResUtil, directory: ByteArray): MutableMap<String, Info>

    @Throws(SecureException::class)
    fun readFiletree(rsrc: IResUtil, directory: ByteArray): JSONObject

    fun readDirectory(input: MySeekableInputStream): Pair<ByteArray, ByteArray>

    fun readSignature(input: MySeekableInputStream): Triple<ByteArray, ByteArray, ByteArray>
}

/**
 * The ArchveFormat factory.
 */
private object ArchiveFormat {

    const val V10 = 10
    const val V11 = 11
    const val V12 = 12
    const val V13 = 13
    const val V14 = 14

    const val SIG_SIZE = 512

    val MAGIC = byteArrayOf(
        0x6e, 0x6f, 0x63, 0x61,
        0x01, 0x02, 0xab.toByte(), 0xac.toByte()
    )

    fun latestArchiveVersion(): Int {
        return V14
    }

    fun latestArchiveFormat(): IArchiveFormat {
        return get(latestArchiveVersion())
            ?: throw AssertionError()
    }

    fun isSupportedVersion(version: Int): Boolean {
        return when (version) {
            V10 -> true
            V11 -> true
            V12 -> true
            V13 -> true
            V14 -> true
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
            V12 -> ArchiveFormatV12.singleton
            V13 -> ArchiveFormatV13.singleton
            V14 -> ArchiveFormatV14.singleton
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

    const val File: Byte = 2
    const val FileGz: Byte = 3

    const val Blocks: Byte = 4
    const val Block: Byte = 5
    const val BlockGz: Byte = 6
    const val BlocksEnd: Byte = 7
    const val End: Byte = 8

    const val FilelistGz: Byte = 9
    const val FilelistEnd: Byte = 10
    const val Dir: Byte = 11
    const val DirEnd: Byte = 12

    const val DataGz: Byte = 13
    const val DataEnd: Byte = 14

    const val Data: Byte = 15

    const val File2: Byte = 16
}

private abstract class ArchiveFormatBase : IArchiveFormat {

    override fun iterations(): Int {
        return ITERATIONS * 9
    }

    override fun createDigester(): MessageDigest {
        return MessageDigest.getInstance("SHA-256")
    }

    override fun createEncryptionCipher(key: SecretKey): Cipher {
        return fileFormat().createEncryptionCipher(key)
    }

    override fun readCipher(input: IInputStream): Pair<SecretKey, Cipher> {
        val key = SecretKeySpec(U.readU8Bytes(input), "AES")
        val cipher = fileFormat().readCipher(input, key)
        return Pair(key, cipher)
    }

    override fun writeCipher(output: OutputStream, key: SecretKey, cipher: Cipher) {
        IOUt.writeU8Bytes(output, key.encoded)
        fileFormat().writeCipher(output, cipher)
    }

    @Throws(SecureException::class)
    override fun readFilelist(rsrc: IResUtil, directory: ByteArray): MutableMap<String, Info> {
        try {
            return UV10.readV10Filelist(directory)
        } catch (e: Throwable) {
            throw rsrc.secureException(e, R.string.ErrorReadingBackup)
        }
    }

    @Throws(SecureException::class)
    override fun readFiletree(rsrc: IResUtil, directory: ByteArray): JSONObject {
        try {
            return UV10.readV10Filetree(directory)
        } catch (e: Throwable) {
            throw rsrc.secureException(e, R.string.ErrorReadingBackup)
        }
    }

    override fun readDirectory(input: MySeekableInputStream): Pair<ByteArray, ByteArray> {
        val size = input.getSize()
        val buf = ByteArray(8)
        if (input.readWhilePossibleAt(size - 8, buf, 0, 8) != 8)
            throw SecureException()
        val offset = IOUt.read64BE(buf)
        
        if (offset <= 0 || offset >= size)
            throw SecureException()
        input.setPosition(offset)
        return BU.readGzFilelist(input, (size - offset - 8).toInt(), createDigester())
    }

    override fun readSignature(input: MySeekableInputStream): Triple<ByteArray, ByteArray, ByteArray> {
        val (directory, checksum) = readDirectory(input)
        val signature = ByteReader(input).read32BEBytes(ArchiveFormat.SIG_SIZE)
        return Triple(directory, checksum, signature)
    }

    override fun toString(): String {
        return "${version()}"
    }

    companion object {
        private const val ITERATIONS = 1024
    }
}

//////////////////////////////////////////////////////////////////////

private open class ArchiveFormatV14 : ArchiveFormatBase() {
    companion object {
        var singleton = ArchiveFormatV14()
        private const val X = 9
    }

    override fun version(): Int {
        return ArchiveFormat.V14
    }

    override fun readSignature(input: MySeekableInputStream): Triple<ByteArray, ByteArray, ByteArray> {
        val (directory, checksum) = BU.readGzFilelist(input, (input.getSize() - input.getPosition()).toInt(), createDigester())
        val signature = ByteReader(input).read32BEBytes(ArchiveFormat.SIG_SIZE)
        return Triple(directory, checksum, signature)
    }

    override fun signHeaders(signer: Signature, key: SecretKey, vararg blobs: ByteArray): ByteArray {
        for (blob in blobs) signer.update(blob)
        val signature = U.randBytes(512)
        signer.sign(signature, 0, 512)
        return signature
    }

    override fun verifyHeaders(
        input: InputStream,
        size: Long,
        verifier: Signature,
        keylen: Int,
        cipher: Cipher,
        key: SecretKey,
        vararg blobs: ByteArray
    ): Boolean {
        return true
    }

    override val PADDING1_MAX = 0x7fff

    override fun writePaddings(output: OutputStream, padding1: Int, padding2: Int, padding3: Int) {
        ByteWriter(output)
            .write16BE(padding1)
            .writeU8(padding2)
            .writeU8(padding3)
    }

    override fun readPaddings(input: InputStream): Triple<Int, Int, Int> {
        val r = ByteReader(input)
        return Triple(r.readU16BE(), r.readU8(), r.readU8())
    }

    override fun signPayload(signer: Signature, payload: ByteArray): ByteArray {
        return payload
    }

    override fun verifyPayload(
        verifier: Signature,
        signed: ByteArray,
    ): ByteArray? {
        return signed
    }

    override fun fileFormat(): IFileFormat {
        return FileFormat.get(FileFormat.V11)!!
    }
}

private open class ArchiveFormatV13 : ArchiveFormatV12() {
    companion object {
        var singleton = ArchiveFormatV13()
    }

    override fun version(): Int {
        return ArchiveFormat.V13
    }

    override fun signHeaders(signer: Signature, key: SecretKey, vararg blobs: ByteArray): ByteArray {
        for (blob in blobs) signer.update(blob)
        val signature = U.randBytes(512)
        signer.sign(signature, 0, 512)
        return signature
    }

    override fun verifyHeaders(
        input: InputStream,
        size: Long,
        verifier: Signature,
        keylen: Int,
        cipher: Cipher,
        key: SecretKey,
        vararg blobs: ByteArray
    ): Boolean {
        return true
    }
}

private open class ArchiveFormatV12 : ArchiveFormatBase() {
    companion object {
        var singleton = ArchiveFormatV12()
    }

    override val PADDING1_MAX = 0x7fff

    override fun version(): Int {
        return ArchiveFormat.V12
    }

    override fun fileFormat(): IFileFormat {
        return FileFormat.get(FileFormat.V11)!!
    }

    override fun writePaddings(output: OutputStream, padding1: Int, padding2: Int, padding3: Int) {
        ByteWriter(output)
            .write16BE(padding1)
            .writeU8(padding2)
            .writeU8(padding3)
    }

    override fun readPaddings(input: InputStream): Triple<Int, Int, Int> {
        val r = ByteReader(input)
        return Triple(r.readU16BE(), r.readU8(), r.readU8())
    }

    override fun signPayload(signer: Signature, payload: ByteArray): ByteArray {
        return payload
    }

    override fun verifyPayload(
        verifier: Signature,
        signed: ByteArray,
    ): ByteArray? {
        return signed
    }

    override fun signHeaders(signer: Signature, key: SecretKey, vararg blobs: ByteArray): ByteArray {
        val cipher = fileFormat().createEncryptionCipher(key)
        return ByteArrayOutputStream().use { out ->
            out.write(BU.marshalSalt(cipher.iv))
            CipherOutputStream(StayOpenOutputStream(out), cipher).use { cout ->
                for (blob in blobs) signer.update(blob)
                val signature = U.randBytes(512)
                signer.sign(signature, 0, 512)
                ByteWriter(cout).write32BEBytes(signature)
            }
            out
        }.toByteArray()
    }

    override fun verifyHeaders(
        input: InputStream,
        size: Long,
        verifier: Signature,
        keylen: Int,
        cipher: Cipher,
        key: SecretKey,
        vararg blobs: ByteArray
    ): Boolean {
        val format = fileFormat()
        val iv = BU.marshalSalt(IOUt.readFully(input, ByteArray(format.IV)))
        format.initCipher(cipher, key, iv)
        val signature = CipherInputStream(StayOpenLimitedInputStream(input, size), cipher).use { cin ->
            ByteReader(cin).read32BEBytes(512)
        }
        for (blob in blobs) verifier.update(blob)
        return verifier.verify(signature, 0, keylen / 8)
    }
}

private open class ArchiveFormatV11 : ArchiveFormatV10() {
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
    override val PADDING1_MAX = 0xff

    override fun version(): Int {
        return ArchiveFormat.V10
    }

    override fun fileFormat(): IFileFormat {
        return FileFormat.get(FileFormat.V10)!!
    }

    override fun writePaddings(output: OutputStream, padding1: Int, padding2: Int, padding3: Int) {
        IOUt.writeU8(output, padding1)
        IOUt.writeU8(output, padding2)
        IOUt.writeU8(output, padding3)
    }

    override fun readPaddings(input: InputStream): Triple<Int, Int, Int> {
        return Triple(IOUt.readU8(input), IOUt.readU8(input), IOUt.readU8(input))
    }

    override fun signPayload(signer: Signature, payload: ByteArray): ByteArray {
        val signed = ByteArrayOutputStream().use { output ->
            val w = ByteWriter(output)
            w.write32BEBytes(payload)
            signer.update(payload)
            w.write32BEBytes(signer.sign())
            output
        }.toByteArray()
        return signed
    }

    override fun verifyPayload(
        verifier: Signature,
        signed: ByteArray,
    ): ByteArray? {
        return signed.inputStream().use {
            val r = ByteReader(it)
            val payload = r.read32BEBytes(2048)
            val signature = r.read32BEBytes(2048)
            verifier.update(payload)
            if (!verifier.verify(signature)) null else payload
        }
    }

    override fun signHeaders(signer: Signature, key: SecretKey, vararg blobs: ByteArray): ByteArray {
        return byteArrayOf()
    }

    override fun verifyHeaders(
        input: InputStream,
        size: Long,
        verifier: Signature,
        keylen: Int,
        cipher: Cipher,
        key: SecretKey,
        vararg blobs: ByteArray
    ): Boolean {
        return true
    }

    companion object {
        var singleton = ArchiveFormatV10()
    }
}

private object UV10 {

    @Throws(IOException::class)
    fun readV10Filelist(data: ByteArray): MutableMap<String, Info> {
        val ret = TreeMap<String, Info>()
        ret.put("", Info(0, -1))
        readV10Filelist(data, object : FilelistCallback {
            override fun dir(rpath: List<String>, info: Info) {
                ret.put(rpath.bot.joinPath(), info)
            }

            override fun dirend(rpath: List<String>) {
            }

            override fun file(rpath: List<String>, info: Info) {
                ret.put(rpath.bot.joinPath(), info)
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
        val r = ByteReader(input)
        val rpath = LinkedList<String>()
        while (true) {
            val tag = r.readByte()
            when (tag) {
                Tag.Dir -> {
                    val name = r.read32BEUtf8(K.FILEPATH_BUFSIZE).toString()
                    val timestamp = r.read63UV()
                    rpath.add(name)
                    callback.dir(rpath, Info(timestamp, -1))
                }

                Tag.DirEnd -> {
                    callback.dirend(rpath)
                    rpath.removeLast()
                }

                Tag.File2 -> {
                    val name = r.read32BEUtf8(K.FILEPATH_BUFSIZE).toString()
                    val timestamp = r.read63UV()
                    val size = r.read63UV()
                    val checksum = r.readU8Bytes()
                    val offset = r.read63UV()
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

private interface FilelistCallback {
    fun dir(rpath: List<String>, info: Info)
    fun dirend(rpath: List<String>)
    fun file(rpath: List<String>, info: Info)
}

//////////////////////////////////////////////////////////////////////

class Info constructor(val timestamp: Long, val size: Long, val offset: Long = 0L, val checksum: ByteArray? = null) {
    val isDir: Boolean get() = size < 0
    val isFile: Boolean get() = size >= 0
    fun toJSON(name: String): JSONObject {
        val ret = JSONObject().put(IFileInfo.Key.name, name)
            .put(IFileInfo.Key.lastModified, timestamp)
            .put(IFileInfo.Key.offset, offset)
        if (checksum != null) ret.put(IFileInfo.Key.checksum, Hex.encode(checksum))
        if (isDir) ret.put(IFileInfo.Key.isdir, true)
            .put(IFileInfo.Key.length, 0)
            .put(IFileInfo.Key.files, JSONObject())
        else ret.put(IFileInfo.Key.isfile, true)
            .put(IFileInfo.Key.length, size)
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

open class EncryptedFileInfo @JvmOverloads constructor(
    final override val root: EncryptedRootInfo,
    final override val rpath: String,
    file: File = File(root.file, rpath).absoluteFile
) : FileInfoBase(file) {
    init {
        if (rpath.isEmpty() || rpath == ".." || rpath.startsWith("..$FS")) throw AssertionError()
    }

    private val fileContent = EncryptedFileContent(this, f)
    override val supportHistory = root.supportHistory

    override val file: File? get() = null

    //////////////////////////////////////////////////////////////////////

    override val name: String get() = f.name
    override val cpath = Basepath.joinRpath(root.cpath, rpath)
    override val apath = Basepath.joinPath(root.apath, rpath)

    override val parent: IFileInfo?
        get() = (Basepath.dir(rpath)).let { path ->
            if (path.isNullOrEmpty()) root else
                root.fileInfo(path)
        }

    override fun content(): IFileContent {
        return fileContent
    }

    override val length: Long
        get() {
            return f.length()
        }

    override fun setLastModified(timestamp: Long): Boolean {
        return f.setLastModified(timestamp)
    }

    override fun setWritable(writable: Boolean): Boolean {
        return f.setWritable(writable)
    }

    override fun newfileinfo(rpath: String): IFileInfo {
        return if (rpath.isEmpty()) this else EncryptedFileInfo(root, Basepath.joinRpath(this.rpath, rpath))
    }

    class EncryptedFileContent(
        private val info: EncryptedFileInfo,
        private
        val file: File
    ) : IFileContent {

        override fun getContentLength(): Long {
            return if (file.isDirectory) file.length() else SeekableFileInputStream(file).use {
                IFileFormat.unencryptedSizeOf(it, info.root.key, file.length())
            }
        }

        override fun inputStream(): InputStream {
            return EncryptedFileContent.openCipherInputStream(SeekableFileInputStream(file), info.root.key).first
        }

        override fun seekableInputStream(): MySeekableInputStream? {
            return EncryptedFileContent.openCipherSeekableInputStream(SeekableFileInputStream(file), info.root.key).first
        }

        override fun outputStream(): OutputStream {
            val output = file.outputStream()
            return EncryptedOutputStream(output, info.root.key, info.root.format)
        }

        override fun seekableOutputStream(truncate: Boolean): ISeekableOutputStream? {
            return Without.throwableOrNull {
                EncryptedFileContent.openCipherSeekableOutputStream(
                    RandomAccessFile(file, "rw"),
                    info.root.key
                )
            }
        }

        override fun readBytes(): ByteArray {
            return IOUt.readBytes(inputStream())
        }

        override fun readText(charset: Charset): String {
            return IOUt.readText(inputStream(), charset)
        }

        override fun write(data: ByteArray, offset: Int, length: Int, timestamp: Long?) {
            prepareToWrite()
            this.outputStream().use { it.write(data, offset, length) }
            file.setLastModified(timestamp ?: System.currentTimeMillis())
        }

        override fun write(data: CharArray, offset: Int, length: Int, timestamp: Long?, charset: Charset) {
            prepareToWrite()
            this.outputStream().bufferedWriter(charset).use { it.write(data, offset, length) }
            file.setLastModified(timestamp ?: System.currentTimeMillis())
        }

        override fun write(data: InputStream, timestamp: Long?) {
            prepareToWrite()
            this.outputStream().use { FileUt.copy(it, data) }
            file.setLastModified(timestamp ?: System.currentTimeMillis())
        }

        override fun write(data: Reader, timestamp: Long?, charset: Charset) {
            prepareToWrite()
            this.outputStream().bufferedWriter(charset).use { w ->
                val tmpbuf = CharArray(IOUt.BUFSIZE)
                IOUt.copyAll(tmpbuf, data) {
                    w.write(tmpbuf, 0, it)
                }
                file.setLastModified(timestamp ?: System.currentTimeMillis())
            }
        }

        override fun copyTo(dst: OutputStream) {
            inputStream().use { input ->
                FileUt.copy(dst, input)
            }
        }

        override fun copyTo(dst: IFileInfo, timestamp: Long?) {
            inputStream().use {
                dst.content().write(it, timestamp)
            }
        }

        override fun moveTo(dst: IFileInfo, timestamp: Long?) {
            dst.root.let { if (it === info.root && renameTo(dst, timestamp)) return }
            copyTo(dst, timestamp)
            if (!file.delete())
                throw IOException()
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
                if (!file.isFile || !file.delete())
                    throw IOException()
                return
            }
            file.mkparentOrNull()
                ?: throw IOException()
        }

        companion object {
            @Throws(IOException::class)
            fun openCipherInputStream(seekable: SeekableFileInputStream, key: SecretKey): Pair<InputStream, IFileFormat> {
                val magic = IOUt.readFully(seekable, ByteArray(FileFormat.MAGIC.size))
                StructUt.equals(magic, FileFormat.MAGIC)
                        || throw SecureException()
                val version = IOUt.readU8(seekable)
                val format = FileFormat.get(version)
                    ?: throw SecureException()
                val blocksize = IOUt.readU8(seekable) * K.ENCRYPTED_BLOCK_SIZE_MULTIPIER
                val cipher = format.readCipher(seekable, key)
                return Pair(
                    (if (blocksize == 0) SimpleEncryptedInputStream(seekable, cipher, format, seekable.getSize())
                    else EncryptedInputStream(seekable, key, format, blocksize, cipher, seekable.getPosition())), format
                )
            }

            @Throws(IOException::class)
            fun openCipherSeekableInputStream(
                seekable: SeekableFileInputStream,
                key: SecretKey
            ): Pair<MySeekableInputStream?, IFileFormat> {
                val magic = IOUt.readFully(seekable, ByteArray(FileFormat.MAGIC.size))
                StructUt.equals(magic, FileFormat.MAGIC)
                        || throw SecureException()
                val version = IOUt.readU8(seekable)
                val format = FileFormat.get(version)
                    ?: throw SecureException()
                val blocksize = IOUt.readU8(seekable) * K.ENCRYPTED_BLOCK_SIZE_MULTIPIER
                if (blocksize == 0) return Pair<MySeekableInputStream?, IFileFormat>(null, format)
                val cipher = format.readCipher(seekable, key)
                return Pair(EncryptedInputStream(seekable, key, format, blocksize, cipher, seekable.getPosition()), format)
            }

            @Throws(IOException::class)
            fun openCipherSeekableOutputStream(
                file: RandomAccessFile,
                key: SecretKey
            ): ISeekableOutputStream {
                val input = PositionTrackingRandomFileAdapter(file)
                val magic = InputUt.readFully(input, ByteArray(FileFormat.MAGIC.size))
                StructUt.equals(magic, FileFormat.MAGIC)
                        || throw SecureException()
                val version = InputUt.readU8(input)
                val format = FileFormat.get(version)
                    ?: throw SecureException()
                val blocksize = InputUt.readU8(input) * K.ENCRYPTED_BLOCK_SIZE_MULTIPIER
                if (blocksize == 0)
                    throw SecureException()
                val cipher = format.readCipher(input, key)
                return RewritingEncryptedOutputStream(file, key, format, blocksize, input.getPosition(), cipher)
            }
        }
    }
}

////////////////////////////////////////////////////////////////

/// RootInfo back up by a file.
open class EncryptedRootInfo @JvmOverloads constructor(
    private var _key: SecretKey,
    file: File,
    final override val name: String = file.name,
    private val isvalidpath: Fun11<String, Boolean> = { true }
) : FileInfoBase(file), IRootInfo {

    val format = FileFormat.latestFormat()

    override val supportHistory = false

    override val file: File get() = f

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

    override fun find(ret: MutableCollection<String>, subdir: String, searchtext: String) {
        FileInfoUtil.find(ret, f, subdir, name, searchtext)
    }

    override fun <T> transaction(code: Fun01<T>): T {
        return code()
    }

    override fun history(
        dir: String,
        name: String,
        listdir: Boolean,
        callback: Fun10<IDeletedFileStat>
    ) {
    }

    override fun scanTrash(callback: Fun10<IDeletedFileStat>) {
    }

    override fun cleanupTrash(predicate: Fun11<IDeletedFileStat, Boolean>?): CleanupTrashResult {
        return CleanupTrashResult(0, 0, 0L)
    }

    override fun recover(dst: IFileInfo, infos: List<JSONObject>): Pair<Int, Int> {
        return Pair(0, 0)
    }

    override fun fileInfo(rpath: String): IFileInfo {
        return if (isvalidpath(rpath)) super.fileInfo(rpath)
        else FileInfoUtil.createNotexists(this, rpath)
    }

    override fun newfileinfo(rpath: String): IFileInfo {
        return if (rpath.isEmpty()) this else EncryptedFileInfo(this, Basepath.joinRpath(this.rpath, rpath))
    }

    internal val key get() = _key

    fun setKey(key: SecretKey) {
        this._key = key
    }
}

class RewritingEncryptedOutputStream constructor(
    private val file: RandomAccessFile,
    private val key: SecretKey,
    private val format: IFileFormat,
    private val blocksize: Int,
    private val contentStart: Long,
    private val cipher: Cipher
) : ISeekableOutputStream {
    private val lock = ReentrantLock()
    private val overhead = format.OVERHEAD
    private val ibuffer = ByteArray(overhead + blocksize)
    private var closed = false
    fun readBlock(obuffer: ByteArray, blockstart: Long): Int {
        lock.withLock {
            file.seek(blockstart - format.IV)
            val n = IOUt.readWhilePossible(file, ibuffer, 0, overhead + blocksize)
            if (n < overhead) return -1
            val iv = BU.unmarshalSalt(ibuffer.copyOfRange(0, format.IV))
            format.initCipher(cipher, key, iv)
            val outcount = cipher.doFinal(ibuffer, format.IV, n - format.IV, obuffer)
            return format.verifyBlock(obuffer, outcount)
        }
    }

    override fun writeAt(pos: Long, b: ByteArray, off: Int, len: Int) {
        writeAt(pos, ByteBuffer.wrap(b, off, len))
    }

    override fun writeAt(pos: Long, b: ByteBuffer) {
        lock.withLock {
            var remaining = b.remaining()
            var p = pos
            while (true) {
                val (start, offset) = blockOf(p)
                val buf = ByteArray(blocksize + overhead)
                val n = readBlock(buf, start)
                val len = min(n - offset, b.remaining())
                b.get(buf, format.PREFIX + offset, len)
                flushnow(start - format.IV, buf, n)
                remaining -= len
                if (remaining == 0)
                    break
                p += len
            }
        }
    }

    private fun flushnow(pos: Long, buf: ByteArray, len: Int) {
        file.seek(pos)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        
        file.write(BU.marshalSalt(iv))
        val encypted = MyByteOutputStream().use { out ->
            format.createCipherOutputStream(out, cipher).use { cout ->
                cout.write(buf, format.PREFIX, len)
                format.checksumBlock(cout)
            }
            out
        }.toByteArray()
        file.write(encypted)
    }

    override fun close() {
        if (!closed) {
            closed = true
            file.close()
        }
    }

    fun blockOf(pos: Long): Pair<Long, Int> {
        lock.withLock {
            val n = pos / blocksize
            val start = contentStart + n * (format.OVERHEAD + blocksize)
            return Pair(start, (pos - n * blocksize).toInt())
        }
    }
}

class SimpleEncryptedOutputStream(
    private val output: OutputStream,
    key: SecretKey,
    private val format: IFileFormat
) : OutputStream() {
    private var cposition = 0L
    private val cout: OutputStream

    init {
        output.write(FileFormat.MAGIC)
        IOUt.writeU8(output, format.version())
        IOUt.writeU8(output, 0)
        val cipher = format.createEncryptionCipher(key)
        format.writeCipher(output, cipher)
        cout = format.createCipherOutputStream(StayOpenOutputStream(output), cipher)
    }

    override fun close() {
        format.checksumBlock(cout)
        cout.close()
        val cmod = U.randU8() and (0xff xor format.PADMASK) or (cposition % format.PADSIZE).toInt() and format.PADMASK
        output.write(cmod)
        output.close()
    }

    override fun write(b: Int) {
        val oposition = cposition
        cout.write(b)
        cposition = oposition + 1
    }

    override fun write(b: ByteArray) {
        this.write(b, 0, b.size)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        val oposition = cposition
        cout.write(b, off, len)
        cposition = oposition + len
    }
}

/// @input File position should be right after the cipher block.
class SimpleEncryptedInputStream(
    input: MySeekableInputStream,
    cipher: Cipher,
    private val format: IFileFormat,
    rawsize: Long
) : InputStream() {

    private val cinput: CipherInputStream
    private val cin: DigestInputStream

    init {
        val limit = unencryptedSizeOf(format, input, rawsize) + format.PREFIX
        cinput = CipherInputStream(
            LimitedInputStream(
                input,
                rawsize - 1 - format.CONTENT_START - format.IV
            ), cipher
        )
        cin = DigestInputStream(
            StayOpenLimitedInputStream(cinput, limit),
            format.createDigester()
        )
        IOUt.readFully(cin, ByteArray(format.PREFIX))
    }

    companion object {
        fun unencryptedSizeOf(format: IFileFormat, input: MySeekableInputStream, rawsize: Long): Long {
            val a = ByteArray(1)
            input.readFullyAt(rawsize - 1, a, 0, 1)
            val cmod = a[0].toInt()
            return rawsize - 1 - format.CONTENT_START - format.OVERHEAD + (cmod and format.PADMASK)
        }
    }

    override fun read(): Int {
        return cin.read()
    }

    override fun read(b: ByteArray): Int {
        return cin.read(b, 0, b.size)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return cin.read(b, off, len)
    }

    override fun close() {
        val buf = ByteArray(K.BUFSIZE)
        while (true) {
            val n = cin.read(buf)
            if (n < 0) break
        }
        val actual = cin.messageDigest.digest()
        val expected = IOUt.readFully(cinput, ByteArray(format.SUM))
        cin.close()
        cinput.close()
        if (!actual.contentEquals(expected))
            throw IOException()
    }
}

class MyDigestedOutputStream(output: OutputStream, digester: MessageDigest) : DigestOutputStream(output, digester) {
    private var position = 0L
    fun getPosition(): Long {
        return position
    }

    override fun write(b: Int) {
        super.write(b)
        ++position
    }

    override fun write(b: ByteArray) {
        this.write(b, 0, b.size)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        val before = position
        super.write(b, off, len)
        position = before + len
    }
}

private class StayOpenLimitedInputStream(
    input: InputStream,
    limit: Long
) : LimitedInputStream(input, limit) {
    override fun close() {
    }
}

private class LimitedSeekableInputStream(
    private val input: MySeekableInputStream,
    private val limit: Long
) : MySeekableInputStream() {
    private var position = input.getPosition()
    private var remaining = limit - position
    private var closed = false

    override fun readAt(pos: Long, b: ByteArray, off: Int, size: Int): Int {
        if (pos >= limit) return -1
        if (pos + size <= limit) return input.readAt(pos, b, off, size)
        return input.readAt(pos, b, off, (limit - pos).toInt())
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

    override fun skip(n: Long): Long {
        val pos = getPosition()
        val len = min(n, getSize() - pos)
        setPosition(pos + len)
        return len
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
        if (remaining == 0L) return -1
        val oremaining = remaining
        val oposititon = position
        val ret = input.read(b, off, (if (len > remaining) remaining.toInt() else len))
        if (ret < 0) {
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
    private var position = 0L
    private var remaining = blocksize
    private var cout = format.createCipherOutputStream(output, cipher)

    override fun getPosition(): Long {
        return position
    }

    override fun write(b: Int) {
        if (closed)
            throw IOException()
        if (remaining == 0) flushnow()
        cout.write(b)
        position += 1
        remaining -= 1
    }

    override fun write(b: ByteArray) {
        this.write(b, 0, b.size)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        if (closed)
            throw IOException()
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
        if (Arrays.equals(oiv, iv))
            throw IOException()
        output.write(BU.marshalSalt(iv))
        cout = format.createCipherOutputStream(output, cipher)
        remaining = blocksize
        return cout
    }
}

private class EncryptedOutputStream(
    private val output: OutputStream,
    key: SecretKey,
    private val format: IFileFormat,
    blocksize: Int = format.BLOCKSIZE
) : OutputStream() {
    private var cipher = format.createEncryptionCipher(key)
    private var out = StayOpenOutputStream(output)
    private var cout: OutputStream

    init {
        out.write(FileFormat.MAGIC)
        IOUt.writeU8(out, format.version())
        IOUt.writeU8(
            out, if (blocksize > 255 * K.ENCRYPTED_BLOCK_SIZE_MULTIPIER) 0
            else blocksize / K.ENCRYPTED_BLOCK_SIZE_MULTIPIER
        )
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
        this.write(b, 0, b.size)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        cout.write(b, off, len)
    }
}

private class EncryptedInputStream constructor(
    private val seekable: MySeekableInputStream,
    key: SecretKey,
    format: IFileFormat,
    blocksize: Int,
    cipher: Cipher,
    contentStart: Long
) : MySeekableInputStream() {

    private val pool = EncryptedInputStreamPool(seekable, key, format, cipher, blocksize, contentStart)

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

    override fun skip(n: Long): Long {
        return pool.input { it.skip(n) }
    }

    override fun close() {
        pool.close()
        seekable.close()
    }

    /// NOTE that this change the sequential position.
    override fun readAt(pos: Long, b: ByteArray, off: Int, size: Int): Int {
        return pool.readAt(pos, b, off, size)
    }

    private class BlockInputStream(
        private val pool: EncryptedInputStreamPool,
        private val size: Long,
        private val blocksize: Int,
        position: Long
    ) : InputStream() {
        private var blockStart = 0L
        private var blockLength = 0
        private var offset = 0
        private var startPos = 0L
        private var endPos = 0L
        private val buffer = ByteArray(pool.PREFIX + pool.SUM + pool.PADSIZE + blocksize)

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
            return buffer[pool.PREFIX + offset++].toInt() and 0xff
        }

        @Throws(IOException::class)
        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (len == 0) return 0
            var offset = off
            var length = len
            while (length > 0) {
                if (this.offset >= blockLength && load() <= 0) break
                val n = if (length > (blockLength - this.offset)) blockLength - this.offset else length
                System.arraycopy(buffer, pool.PREFIX + this.offset, b, offset, n)
                this.offset += n
                offset += n
                length -= n
            }
            return if (offset > off) offset - off else -1
        }

        /// @return 0 if end of file is reached, otherwise number of bytes skipped.
        @Throws(IOException::class)
        override fun skip(count: Long): Long {
            if (count < 0L)
                throw IOException()
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

    private class EncryptedInputStreamPool constructor(
        private val input: MySeekableInputStream,
        private val key: SecretKey,
        private val format: IFileFormat,
        private val cipher: Cipher,
        private val blocksize: Int,
        private val contentStart: Long
    ) {
        private var logicalSize: Long? = null

        init {
        }

        val IV = format.IV
        val SUM = format.SUM
        val PREFIX = format.PREFIX
        val PADSIZE = format.PADSIZE
        val OVERHEAD = format.OVERHEAD
        private val lock = ReentrantLock()
        private val ibuffer = ByteArray(blocksize + format.OVERHEAD)
        private val seekables = ArrayList<BlockInputStream>()
        private var inputStream: BlockInputStream? = null

        fun <T> input(callback: Fun11<BlockInputStream, T>): T {
            return lock.withLock {
                callback(inputStream
                    ?: BlockInputStream(this, input.getSize(), blocksize, 0).also {
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
                        val n = position - s.getPosition()
                        if (s.skip(n) != n) return -1
                        return s.read(buffer, off, len)
                    }
                }
                val s = if (seekables.size >= K.SEEKABLE_POOL_SIZE) {
                    seekables.removeAt(0).also {
                        it.setPosition(position)
                        seekables.add(it)
                    }
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
                val n = input.readWhilePossibleAt(blockstart - IV, ibuffer, 0, OVERHEAD + blocksize)
                if (n < OVERHEAD) return -1
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
                    val count = if (start >= rawsize) 0 else readBlock(ByteArray((rawsize - start).toInt()), start)
                    n * blocksize + count
                }.also { logicalSize = it }
            }
        }
    }
}

class ReadOnlyBackupFileRoot private constructor(
    private val backupUtil: BackupUtil,
    private val seekable: MySeekableInputStream,
    private val archiveFormat: IArchiveFormat,
    jsontree: JSONObject
) : ReadOnlyJSONRoot(jsontree, "", ReadOnlyJSONRoot::createFileInfoTree), ICloseableRootInfo {
    companion object {
        fun of(
            sec: ISecUtilAccessor,
            backupUtil: BackupUtil,
            backupfile: IFileInfo
        ): ReadOnlyBackupFileRoot? {
            try {
                val input = backupfile.content().seekableInputStream() ?: return null
                val keypair = sec.getBackupKeyPair()
                return backupUtil.openBackupInputStream(
                    input, keypair
                ) { cis, backupfileinfo, dir ->
                    val format = backupfileinfo.format
                    val filetree = format.readFiletree(backupUtil.rsrc, dir)
                    ReadOnlyBackupFileRoot(backupUtil, cis, format, filetree)
                }
            } catch (e: Throwable) {
                return null
            }
        }
    }

    override fun inputStream(rpath: String): InputStream {
        val json = jsonInfo(rpath)
            ?: throw IOException()
        val offset = json.optLong(IFileInfo.Key.offset, -1)
        if (offset <= 0)
            throw IOException()
        val checksum = json.stringOrNull(IFileInfo.Key.checksum)?.let { Hex.decode(it) }
        return backupUtil.readFileAt(offset, seekable, archiveFormat, checksum)
            ?: throw IOException()
    }

    override fun close() {
        FileUt.closeAndIgnoreError(seekable)
    }
}

class NullInputStream : InputStream() {
    companion object {
        val singleton = NullInputStream()
    }

    override fun read(): Int {
        return -1
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return if (len == 0) 0 else -1
    }

    override fun read(b: ByteArray): Int {
        return read(b, 0, b.size)
    }
}
