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

import com.cplusedition.bot.core.*
import sf.andrians.org.apache.http.NameValuePair
import sf.andrians.org.apache.http.client.utils.URLEncodedUtils
import sf.andrians.org.apache.http.message.BasicNameValuePair
import java.net.MalformedURLException
import java.net.URI

typealias BaseUri = AnUri

/// A custom URI implmentation that ignore scheme, userinfo, host, port.
/// Keeping only path, query and fragment.
class AnUri private constructor(
    private val url: URI,
    private val basedir: String,
    private val decodedPath: String,
    val isRelative: Boolean,
) {
    val protocol: String? get() = this.url.scheme?.let { "$it:" }
    val host: String? get() = this.url.host
    val port: String? get() = this.url.port.let { if (it >= 0) "$it" else null }
    val hostname: String? get() = this.url.rawAuthority
    val encodedPath: String get() = this.path.split(SEP).map { fixBrackets(encodePath(it)) }.joinToString(SEP)
    val humanPath: String get() = this.path.split(SEP).map { fixBrackets(encodeHumanPath(it)) }.joinToString(SEP)
    val encodedQuery: String get() = this.url.rawQuery?.let { "?$it" } ?: ""
    val encodedFragment: String get() = this.url.rawFragment?.let { "#$it" } ?: ""
    val queryParameters get() = this.url.rawQuery?.let { queryParameters(it) } ?: listOf()
    val fragment: String? get() = if (encodedFragment.isEmpty()) null else decode(encodedFragment.substring(1))
    val hasQuery: Boolean get() = this.url.rawQuery?.isNotEmpty() == true
    val hasFragment: Boolean get() = this.url.fragment?.isNotEmpty() == true
    /// Absolute URL String including scheme, host, port, ... etc..
    val href: String get() = this.url.toString()
    /// @return The decoded absolute or relative path.
    /// @throws If Uri is invald.
    /// Absolute encoded URL path.
    val path: String
        get() {
            val ret = if (this.isRelative) {
                FileUt.rpath(this.decodedPath, this.basedir) ?: throw AssertionError()
            } else this.decodedPath
            return if (ret.isNotEmpty() && this.decodedPath.endsWith(SEP) && !ret.endsWith(SEP)) ret + SEP else ret
        }
    val pathSegments get() = this.path.split(SEP)
    val isDataUrl: Boolean get() = isDataUrl(this.protocol ?: "")
    /// @return If already relative, return self, otherwise a new Uri relative to the baseurl.
    /// @throws If baseurl is not valid.
    fun toRelative(): AnUri {
        if (this.isRelative) return this
        return AnUri(this.url, this.basedir, this.decodedPath, true)
    }
    /// @return If already absolute, return self, otherwise a new absolute Uri.
    fun toAbsolute(): AnUri {
        if (!this.isRelative) return this
        return AnUri(this.url, this.basedir, this.decodedPath, false)
    }

    /// @param href A href or Uri.
    /// @return A relative Uri relative to this Uri if href is relative, an absolute Uri if href is absolute.
    /// @throws On error.
    fun resolveUri(href: String): AnUri {
        return AnUri.parse(href, this)
    }

    fun resolveUri(u: AnUri): AnUri {
        return resolveUri(u.toString())
    }

    fun relativize(u: AnUri): AnUri {
        val apath = u.toAbsolute().path
        val basepath = this.toAbsolute().path
        val basedir = if (basepath.endsWith(FS)) basepath else (Basepath.dir(basepath) ?: "")
        val rpath = FileUt.rpath(apath, basedir) ?: apath
        return UriBuilder(this).from(u).path(rpath).build()
    }

    fun equals(other: AnUri): Boolean {
        return this.toString() == other.toString()
    }

    fun toShortString(): String {
        return (this.pathSegments.lastOrNull() ?: "") + this.encodedQuery + this.encodedFragment
    }

    override fun toString(): String {
        if (this.isDataUrl) return this.protocol + this.decodedPath + this.encodedQuery + this.encodedFragment
        return this.encodedPath + this.encodedQuery + this.encodedFragment
    }

    companion object {
        private const val SEP = "/"
        private const val SCHEME = "http"
        private const val SCHEME_ = "http://"
        private const val HOST = "localhost"
        private val BASEURI = URI("$SCHEME://$HOST/")
        private val PAT = Regex("^(\\w*:)?(//[\\w@]+)?(:\\d+)?(.*?)(\\?.*)?(#.*?)?\$")
        val ROOT: AnUri get() = AnUri.parse("/")

        /// @param href A relative href if baseurl is specified, an absolute href if baseurl is not specified.
        /// @param baseurl Base Uri for the href. If not specified, href is taken as absolute and default scheme://host:port/ is used as basepath.
        /// @return A relative Uri if href is relative, an absolute Uri if href is absolute.
        /// @throw Error on error.
        fun parse(href: String, baseurl: BaseUri? = null): AnUri {
            val m = PAT.matchEntire(href) ?: throw MalformedURLException()
            val scheme = m.groupValues[1].ifEmpty { null }
            val host = m.groupValues[2].ifEmpty { null }
            val port = m.groupValues[3].ifEmpty { null }
            val path = fixBrackets(m.groupValues[4])
            val query = fixBrackets(m.groupValues[5]).ifEmpty { null }?.substring(1)
            val fragment = fixBrackets(m.groupValues[6]).ifEmpty { null }?.substring(1)
            val isrel = scheme.isNullOrEmpty() && host.isNullOrEmpty() && port.isNullOrEmpty() && !path.startsWith("/")
            val base = if (baseurl === null) this.BASEURI else baseurl.url
            val u = base.resolve(
                URI(
                    null,
                    null,
                    decodeOrNull(path),
                    decodeOrNull(query),
                    decodeOrNull(fragment)
                )
            )
            val isdataurl = isDataUrl(scheme ?: "")
            val decodedpath = if (isdataurl) u.rawSchemeSpecificPart else u.path
                ?: throw MalformedURLException()
            val decoded = if (isdataurl) decodedpath else Basepath.cleanPath(decodedpath)
            val basedir = Basepath.dir(base.path)?.let { if (it.startsWith(SEP)) it else "/$it" } ?: SEP
            return AnUri(u, Basepath.cleanPath(basedir), decoded, isrel)
        }

        /// Create Uri from decoded parts.
        /// If baseuri is provided and not null, the path is considered as relative to baseuri.
        /// @param path Decoded path segments as Array<string>, null or empty if not present.
        /// @param search Decoded key/value pairs, null or empty if not present.
        /// @param hash Decoded fragment without # prefix, null if not present.
        /// @param baseurl Must provided for relative url, ignored if url is absolute.
        /// @throws MalformedURLException.
        class UriBuilder constructor(private var baseUri: BaseUri? = null) {
            private var pathSegments: List<String>? = null
            private var nameValues: List<NameValuePair>? = null
            private var fragment: String? = null

            fun baseUri(baseuri: BaseUri?): UriBuilder {
                this.baseUri = baseuri
                return this
            }

            fun from(uri: AnUri): UriBuilder {
                this.pathSegments = uri.pathSegments
                this.nameValues = uri.queryParameters
                this.fragment = uri.fragment
                return this
            }

            fun path(segments: List<String>): UriBuilder {
                this.pathSegments = segments
                return this
            }

            /// @param path A decoded filepath.
            fun path(path: String): UriBuilder {
                this.pathSegments = path.split(FSC)
                return this
            }

            /// @param namevalues Decoded name and value query parameters.
            fun query(namevalues: List<NameValuePair>): UriBuilder {
                this.nameValues = namevalues
                return this
            }

            /// @param namevalues Decoded name and value query parameters.
            fun query(namevalues: Map<String, String>): UriBuilder {
                this.nameValues = namevalues.map { BasicNameValuePair(it.key, it.value) }
                return this
            }

            /// @param fragment Decoded hash, without leading "#", null for no fragment part.
            fun fragament(fragment: String?): UriBuilder {
                this.fragment = fragment
                return this
            }

            fun build(): AnUri {
                val pathstr = pathSegments?.map {
                    encodePath(it)
                }?.joinToString(SEP) ?: ""
                val querystr = nameValues?.let { nvs ->
                    val b = StringBuilder()
                    for (nv in nvs) {
                        if (b.isEmpty()) b.append("?")
                        else if (b.length > 1) b.append("&")
                        b.append(encode(nv.name))
                        nv.value?.let { b.append("=" + encode(it)) }
                    }
                    b.toString()
                } ?: ""
                val fragmentstr = fragment?.let { "#${encode(it)}" } ?: ""
                return AnUri.parse(pathstr + querystr + fragmentstr, baseUri)
            }

            fun buildOrNull(): AnUri? {
                return Without.throwableOrNull { build() }
            }
        }

        fun parseOrNull(href: String?, baseurl: BaseUri? = null): AnUri? {
            return if (href == null) null else Without.throwableOrNull { this.parse(href, baseurl) }
        }

        fun queryParameters(query: String): List<NameValuePair> {
            return URLEncodedUtils.parse(query, Charsets.UTF_8)
        }

        fun isDataUrl(scheme: String): Boolean {
            return scheme == "data:" || scheme == "blob:"
        }

        fun split(href: String): Triple<String, String, String> {
            var (path, search) = TextUt.splitAt(href, href.indexOf("?"))
            val hash: String?
            if (search.isNullOrEmpty()) {
                TextUt.splitAt(path, path.indexOf("#")).let {
                    path = it.first
                    hash = it.second
                }
            } else {
                TextUt.splitAt(search, search.indexOf("#")).let {
                    search = it.first
                    hash = it.second
                }
            }
            path = this.stripSchemeHost(path)
            return Triple(path, search ?: "", hash ?: "")
        }

        /// Strip scheme://host:port, or //host:port
        fun stripSchemeHost(href: String): String {
            if (href.startsWith("data:") || href.startsWith("blob:")) return href
            val match = PAT.matchEntire(href) ?: return href
            return match.groupValues[4] + match.groupValues[5] + match.groupValues[6]
        }

        fun filename_(uri: AnUri?): String {
            if (uri == null) return ""
            return uri.pathSegments.lastOrNull() ?: ""
        }

        fun stem_(uri: AnUri?): String {
            return Basepath.splitName(this.filename_(uri)).first
        }

        fun suffix_(uri: AnUri?): String {
            return Basepath.splitName(this.filename_(uri)).second
        }

        fun lcSuffix_(uri: AnUri?): String {
            return this.suffix_(uri).lowercase()
        }

        fun lcSuffixOfSegments_(segments: Array<String>): String {
            return Basepath.lcSuffix(segments.lastOrNull() ?: "")
        }

        private fun encodePath(part: String): String {
            return URLEncodedUtils.encPath(part, Charsets.UTF_8)
        }

        private fun encodeHumanPath(part: String): String {
            return URLEncodedUtils.encHumanPath(part)
        }

        private fun encode(part: String): String {
            return URLEncodedUtils.encUric(part, Charsets.UTF_8)
        }

        private fun decode(part: String, plusasblank: Boolean = false): String {
            return URLEncodedUtils.urlDecode(part, Charsets.UTF_8, plusasblank)
        }

        private fun decodeOrNull(part: String?, plusasblank: Boolean = false): String? {
            return if (part == null) null else decode(part, plusasblank)
        }

        private fun fixBrackets(part: String): String {
            return part.replace("[", "%5B").replace("]", "%5D")
        }
    }
}
