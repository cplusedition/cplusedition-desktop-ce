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
package sf.andrians.cplusedition.support.handler

import com.cplusedition.anjson.JSONUtil.stringOrNull
import org.json.JSONException
import org.json.JSONObject
import sf.andrians.cplusedition.R
import sf.andrians.cplusedition.support.An
import java.net.URI

class LinkVerifier(private val context: ICpluseditionContext) : ILinkVerifier {

    private val storage = context.getStorage()

    override fun handle(cmd: Int, data: String): String {
        return when (cmd) {
            ILinkVerifier.Cmd.LINKINFOS -> linkInfos(data)
            ILinkVerifier.Cmd.LINKINFO -> linkInfo(data)
            else -> jsonerror("Invalid cmd: $cmd")
        }
    }

    /**
     * @param data {baseurl: string, links: {link: null, ...}
     * where baseurl is the context relative URL of the iframe document.
     * @return {An.Key.errors: errors, An.Key.result: {link: status, ...}
     */
    private fun linkInfos(data: String): String {
        return try {
            val infos = JSONObject(data)
            val base = infos.stringOrNull(An.Key.baseurl)
                    ?: return storage.rsrc.jsonError(R.string.MissingBaseurlParameter)
            val baseuri = URI(base)
            val links = infos.getJSONObject(An.Key.links)
            val result = JSONObject()
            val it = links.keys()
            while (it.hasNext()) {
                val link = it.next()
                linkinfo(result, baseuri, link)
            }
            val ret = JSONObject()
            ret.put(An.Key.result, result)
            ret.toString()
        } catch (e: Exception) {
            storage.rsrc.jsonError(R.string.Error)
        }
    }

    private fun linkInfo(data: String): String {
        return try {
            val infos = JSONObject(data)
            val base = infos.stringOrNull(An.Key.baseurl)
                    ?: return storage.rsrc.jsonError(R.string.MissingBaseurlParameter)
            val baseuri = URI(base)
            val link = infos.getString(An.Key.path)
            val status = linkStatus(baseuri, link)
            val ret = JSONObject()
            ret.put(An.Key.linkinfo, status)
            ret.toString()
        } catch (e: Exception) {
            storage.rsrc.jsonError(R.string.Error)
        }
    }

    @Throws(JSONException::class)
    private fun linkinfo(result: JSONObject, baseuri: URI, link: String) {
        try {
            val status = linkStatus(baseuri, link)
            result.put(link, status)
        } catch (e: Throwable) {
            result.put(link, An.LinkInfoStatus.INVALID)
        }
    }

    /**
     * @param link A relative url.
     */
    fun linkStatus(baseuri: URI, link: String): String? {
        return linkStatus(baseuri.resolve(link))
    }

    /** @param uri An absolute, ie. resolved, URI.
     */
    fun linkStatus(uri: URI): String? {
        return storage.linkInfo(uri)
    }

    private fun jsonerror(msg: String): String {
        context.w(msg)
        val ret = JSONObject()
        return try {
            ret.put(An.Key.errors, msg)
            ret.toString()
        } catch (ex: JSONException) {
            context.e("ASSERT: Unexpected exception: " + ex.message, ex)
            "{'" + An.Key.errors + "': 'error'}"
        }
    }
}