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

import com.cplusedition.anjson.JSONUtil
import com.cplusedition.anjson.JSONUtil.stringMapOrNull
import com.cplusedition.anjson.JSONUtil.stringOrNull
import com.cplusedition.bot.core.*

import org.json.JSONObject
import sf.andrians.cplusedition.support.Backend
import sf.andrians.cplusedition.support.Http.HttpHeader
import sf.andrians.cplusedition.support.Http.HttpStatus
import sf.andrians.cplusedition.support.ServerKey
import sf.andrians.cplusedition.support.handler.ICpluseditionRequest
import sf.andrians.cplusedition.support.handler.ICpluseditionResponse
import sf.unixsocket.UnixClientSocket
import sf.unixsocket.UnixServerSocket
import sf.unixsocket.UnixSocket
import sf.unixsocket.UnixSocket.SockType
import sf.unixsocket.UnixSocket.SockType.STREAM
import java.io.*
import java.net.URI
import java.net.URLDecoder
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.system.exitProcess

open class StandaloneServer(datadir: File, backend: Backend, pass: CharArray, sockdir: File, private val sockfile: File) {
    private val socket: UnixServerSocket
    private val delegate: ServerDelegate = ServerDelegate(datadir, backend, pass)

    companion object {
        const val SERVER = ".server"
        const val SERVER_PID = "server.pid"

        init {
            System.load(File("lib/libunixsocket-cc.so").absolutePath)
        }
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.isEmpty()) {
                System.err.println("Usage: cat passfile | ${this::class.simpleName} datadir [--db]")
                exitProcess(1)
            }
            val datadir = File(args[0])
            if (!datadir.exists()) {
                System.err.println("Directory not found: ${args[0]}")
                exitProcess(2)
            }
            val backend = if (args.size > 1 && args[1] == "--db") Backend.DB
            else if (args.size > 1 && args[1] == "--aes") Backend.AES
            else Backend.PLAIN
            val sockdir = datadir.file("run").mkdirsOrFail()
            val sockfile = sockdir.file(SERVER)
            if (sockfile.exists()) {
                System.err.println("Server is already running, abort.")
                System.err.println("To force starting, delete ${sockfile.absolutePath} and try again.");
                exitProcess(3)
            }
            val pass = System.`in`.reader().readText().trim().toCharArray()
            sockfile.resolveSibling(SERVER_PID).deleteOrFail().outputStream().bufferedWriter().use {
                try {
                    val pid = ProcessHandle.current().pid()
                    it.append("$pid")
                } catch (e: Throwable) {
                    System.err.println("Error writing pid file")
                }
            }
            StandaloneServer(datadir, backend, pass, sockdir, sockfile)
        }
    }

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            this.destroy()
        })
        val path = sockfile.path
        this.socket = UnixServerSocket(path, SockType.STREAM).onClose {
            
            
        }
        try {
            this.socket.listen(512)
            FileUt.setOwnerOnly1(sockdir)
            FileUt.setOwnerOnly1(sockfile)
            
        } catch (e: Throwable) {
            Conf.e("# Error starting server at: $path", e)
            this.destroy()
            throw AssertionError()
        }
        
        Thread {
            while (true) {
                try {
                    val client = this.socket.accept()
                    handle(client)
                } catch (e: InterruptedIOException) {
                    
                    break
                } catch (e: IOException) {
                    
                }
            }
        }.start()
    }

    fun destroy() {
        delegate.destroy()
        socket.close()
        sockfile.delete()
        sockfile.resolveSibling(SERVER_PID).delete()
    }

    @Throws(Exception::class)
    protected fun createClient(path: String): IBotResult<UnixClientSocket, Throwable> {
        try {
            return BotResult.ok(UnixClientSocket(path, STREAM).connect())
        } catch (e: Throwable) {
            return BotResult.fail(e)
        }
    }

    protected fun writeString(output: OutputStream, s: String) {
        val w = ByteWriter(output)
        val b = s.toByteArray()
        w.write32BE(b.size)
        w.write(b)
    }

    private fun handle(client: UnixSocket): Boolean {
        fun badrequest(input: String): Boolean {
            val response = ResponseAdapter(client, null)
            
            badrequest(response)
            return true
        }

        val input = client.inputStream.use {
            String(ByteReader(it).read32BEBytes())
        }
        val json = JSONUtil.jsonObjectOrNull(input)
        if (json != null) {
            val url = json.stringOrNull(ServerKey.url) ?: return badrequest(input)
            val headers = json.stringMapOrNull(ServerKey.headers) ?: return badrequest(input)
            val data = json.stringOrNull(ServerKey.data) ?: return badrequest(input)
            val uri = Without.exceptionOrNull { URI(url) } ?: return badrequest(input)
            val isipc = json.getBoolean(ServerKey.ipc)
            val request = RequestAdapter(uri, headers, Base64.getDecoder().decode(data))
            val response = ResponseAdapter(client, request)
            if (isipc) delegate.handlea(response, request)
            else delegate.handle1(response, request)
        }
        return true
    }

    private fun badrequest(res: ICpluseditionResponse) {
        res.setStatus(HttpStatus.BadRequest)
    }

    private class RequestAdapter(
        private val url: URI,
        private val headers: Map<String, String>,
        private val data: ByteArray
    ) : ICpluseditionRequest {
        private val queries = run {
            val ret = TreeMap<String, String>()
            url.rawQuery?.split("&")?.map {
                val kv = it.split("=", limit = 2)
                ret[decode(kv[0])] = if (kv.size > 1) decode(kv[1]) else ""
            }
            ret
        }

        private fun decode(value: String): String {
            return URLDecoder.decode(value, "UTF-8")
        }

        override fun getPathInfo(): String {
            return url.path
        }

        override fun getParam(name: String): String? {
            return queries.get(name)
        }

        override fun getHeader(name: String): String? {
            return headers.get(name)
        }

        @Throws(IOException::class)
        override fun getInputStream(): InputStream {
            return data.inputStream()
        }
    }

    private class ResponseAdapter(
        private val socket: UnixSocket,
        private val request: ICpluseditionRequest?
    ) : ICpluseditionResponse {

        companion object {
            val TIMEOUT = 60 * 1000L
        }

        private var watchdog: WatchDog? = null
        private var status = HttpStatus.Ok
        private val headers = mutableMapOf<String, String>(
            HttpHeader.ContentType to "text/html;charset=UTF-8",
            HttpHeader.Connection to "keep-alive",
            HttpHeader.KeepAlive to "timeout=20",
            HttpHeader.Date to DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now())
        )

        override fun setContentLength(length: Long) {
            setHeader(HttpHeader.ContentLength, "$length")
        }

        override fun setContentType(type: String) {
            setHeader(HttpHeader.ContentType, type)
        }

        override fun setData(data: InputStream) {
            Thread {
                val total = arrayOf(0L)
                try {
                    socket.outputStream.use { output ->
                        data.use { input ->
                            watchdog?.close()
                            watchdog = WatchDog(TIMEOUT) {
                                socket.close()
                                input.close()
                            }
                            total[0] = total[0] + writeJSONHeader(output)
                            copy(output, input, total)
                        }
                    }
                } catch (e: Exception) {
                    
                    socket.close()
                } finally {
                    
                    watchdog?.close()
                    watchdog = null
                }
            }.start()
        }

        private fun copy(output: OutputStream, input: InputStream, total: Array<Long>) {
            val bufsize = Conf.COPY_BUFSIZE
            val b = ByteArray(bufsize)
            while (true) {
                watchdog?.watch()
                val n = input.read(b)
                if (n < 0) break
                output.write(b, 0, n)
                total[0] = total[0] + n
            }
        }

        override fun setHeader(name: String, value: String) {
            headers[name] = value
        }

        override fun setStatus(status: Int) {
            this.status = status
            if (status >= HttpStatus.MultipleChoices) {
                socket.outputStream.use { output ->
                    writeJSONHeader(output)
                }
            }
        }

        override fun setupHtmlResponse() {
            setHeader(HttpHeader.NoCache, "true")
            setHeader(HttpHeader.CacheControl, "no-cache")
            setContentType("text/html;charset=UTF-8")
            setHeader(
                "Content-Security-Policy",
                "default-src 'self';" +
                        " script-src 'self' 'unsafe-inline';" +
                        " style-src 'self' 'unsafe-inline';" +
                        " img-src data: 'self';" +
                        " object-src 'none';" +
                        " navigate-to 'self';" +
                        " form-action 'none';" +
                        " frame-ancestors 'self';"
            )
        }

        override fun getStatus(): Int {
            return status
        }

        override fun getRequest(): ICpluseditionRequest? {
            return request
        }

        private fun writeJSONHeader(output: OutputStream): Int {
            fun jsonheaders(): JSONObject {
                val ret = JSONObject()
                for ((key, value) in headers.entries) {
                    ret.put(key, value)
                }
                return ret
            }

            val b = JSONObject()
                .put(ServerKey.statusCode, status)
                .put(ServerKey.headers, jsonheaders())
                .toString().toByteArray(Charsets.UTF_8)
            ByteWriter(output).write32BE(b.size)
            output.write(b)
            return b.size
        }

        private fun writeHttpStatusLine(output: OutputStream) {
            "HTTP/1.1 ${status}\r\n".byteInputStream().use {
                FileUt.copy(output, it)
            }
        }

        private fun writeHttpHeaders(output: OutputStream) {
            val content = headers.map { (k, v) ->
                "${k}: ${v}\r\n"
            }.joinToString("") + "\r\n"
            content.byteInputStream().use { FileUt.copy(output, it) }
        }
    }
}
