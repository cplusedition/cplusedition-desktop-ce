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

import com.cplusedition.anjson.JSONUtil.stringOrDef
import com.cplusedition.bot.core.Fun11
import com.cplusedition.bot.core.StringPrintWriter
import org.json.JSONArray
import org.json.JSONObject
import sf.andrians.cplusedition.support.handler.ICpluseditionResponse
import sf.andrians.cplusedition.support.handler.IResUtil
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.nio.charset.Charset
import java.util.concurrent.Future

class TemplateInfo(private val config: JSONObject) {
    val name: String get() = config.stringOrDef(An._TemplatesJSONKey.name, "")
    val cat: String get() = config.stringOrDef(An._TemplatesJSONKey.category, "")
    val filename: String get() = config.stringOrDef(An._TemplatesJSONKey.template, "")
}

interface ISettingsStore {
    fun <T> call(code: Fun11<ISettingsStoreAccessor, T>): T
}

interface ISettingsStoreAccessor {
    fun reset()
    fun getSettings(): JSONObject
    fun getCurrentSettings(): JSONObject
    fun getDefaultSettings(): JSONObject
    fun getPreferences(): JSONObject
    fun updateSettings(res: IResUtil, update: JSONObject): List<String>
    fun updatePreferences(res: IResUtil, update: JSONObject): List<String>
    fun getTemplatesJson(ret: JSONObject, key: String): JSONObject
    fun getTemplateInfo(name: String?): TemplateInfo?
    fun updateFilePosition(cpath: String, position: JSONArray)
    fun getFilePosition(cpath: String): JSONArray?
}

interface IStorage {

    val rsrc: IResUtil

    fun onPause()
    fun onResume()
    fun onDestroy()
    fun factoryReset()

    fun getSettingsStore(): ISettingsStore
    fun regenerateCustomResources()
    fun getHostCss(): String
    fun getCustomResourcesTimestamp(): Long
    fun getButtonSize(): Int
    fun isLoggedIn(): Boolean

    fun fileInfo(cleanrpath: String?): IFileInfo?
    fun documentFileInfo(cleanrpath: String?): IFileInfo?
    fun fileInfoAt(cpath: String?): Pair<IFileInfo?, Collection<String>>
    fun documentFileInfoAt(cpath: String?): Pair<IFileInfo?, Collection<String>>
    fun getAssetsRoot(): IRootInfo
    fun getHomeRoot(): IRootInfo
    fun getInternalBackupRoot(): IRootInfo
    fun getExternalBackupRoot(): IRootInfo?
    fun getRoots(): List<IRootInfo>

    fun getCached(type: String, since: Long, cpath: String): InputStream?
    fun putCached(type: String, cpath: String, data: InputStream): Boolean
    fun clearCached(includediskfiles: Boolean)

    fun getShared(filename: String): File?
    fun deleteShared(name: String)
    fun clearShared(excludes: Collection<String>?)

    fun getStaging(cpath: String): Triple<String, File, File?>?
    fun closeStaging(tmpfile: File)
    fun saveStaging(tmpfile: File)

    /**
     * @param uri A context relative url, with or without leading slash.
     */
    fun linkInfo(uri: URI): String?

    fun postSearch(filterignorecase: Boolean, filefilter: String, searchignorecase: Boolean, searchtext: String /*, boolean isregex*/): Long

    /**
     * @return null if result is not ready, other the serialized JSONObject: {
     * An.Key.errors: errors,
     * An.Key.result: {
     * path -> [offsets],
     * ...
     * }}
     */
    fun getSearchResult(id: Long /*, boolean isregex*/): String

    //#BEGIN SINCE 2.16.0
    fun <R> submit(task: Fun11<IStorageAccessor, R>): Future<R>

    //#END SINCE 2.16.0
}

interface IStorageAccessor : IStorage {
    /**
     * Copy file at given context relative path to the given OutputStream.
     * NOTE that this allow access to all custom resources NOT PRESENT in assetsRoot.
     */
    fun resourceResponse(response: ICpluseditionResponse, info: IFileInfo)

    @Throws(IOException::class)
    fun readBytes(cpath: String): ByteArray

    @Throws(IOException::class)
    fun readText(cpath: String, charset: Charset = Charsets.UTF_8): String

    /**
     * @param fromdir Context relative path, with leading /, of directory to start search from.
     * @return Context relative paths, with leading /, of resource paths under the fromdir that contains the given searchtext, ignore case.
     */
    fun <T : MutableCollection<String>> find(ret: T, fromdir: String, searchtext: String): T

    @Throws(SecureException::class)
    fun backupData(backupfile: IFileInfo, aliases: List<String>, srcdirpath: String): BackupRestoreResult

    fun backupKey(keyfile: IFileInfo)

    fun restoreData(destdir: IFileInfo, backupfile: IFileInfo, srcpath: String, sync: Boolean): BackupRestoreResult

    @Throws(SecureException::class)
    fun readBackupFiletree(backupfile: IFileInfo): JSONObject?

    fun <R> secAction(task: Fun11<ISecUtilAccessor, R>): R
}

open class SecureException(
        msg: String = "",
        e: Throwable? = null
) : Exception(msg, null, false, false) {
    init {
        //#IF ENABLE_LOG
        if (e == null) {
            
        } else {
            val w = StringPrintWriter()
            w.println("# SecureException: $msg");
            w.println("# " + e.message);
            e.printStackTrace(w)
            
        }
        //#ENDIF ENABLE_LOG
    }
}

class StorageException(
        msg: String = "",
        e: Throwable? = null
) : SecureException(msg, e)

