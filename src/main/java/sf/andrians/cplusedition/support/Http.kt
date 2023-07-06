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

import com.cplusedition.bot.core.Without

object Http {
    object HttpHeader {
        val Accept = "Accept"
        val AcceptCharset = "Accept-Charset"
        val AcceptEncoding = "Accept-Encoding"
        val AcceptLanguage = "Accept-Language"
        val Authorization = "Authorization"
        val CacheControl = "Cache-Control"
        val Connection = "Connection"
        val Cookie = "Cookie"
        val ContentLength = "Content-Length"
        val ContentType = "Content-Type"
        val Date = "Date"
        val Expect = "Expect"
        val Forwarded = "Forwarded"
        val From = "From"
        val Host = "Host"
        val IfMatch = "If-Match"
        val IfModifiedSince = "If-Modified-Since"
        val IfNoneMatch = "If-None-Match"
        val IfRange = "If-Range"
        val IfUnmodifiedSince = "If-Unmodified-Since"
        val MaxForwards = "Max-Forwards"
        val Origin = "Origin"
        val Pragma = "Pragma"
        val ProxyAuthorization = "Proxy-Authorization"
        val Range = "Range"
        val Referrer = "Referrer"
        val TransferCodingExceptions = "Transfer-Coding-Exceptions"
        val UserAgent = "User-Agent"
        val Upgrade = "Upgrade"
        val Via = "Via"
        val Warning = "Warning"

        val AcceptPatch = "Accept-Patch"
        val AcceptRanges = "Accept-Ranges"
        val Age = "Age"
        val Allow = "Allow"
        val AlternativeServices = "Alternative-Services"
        val ContentDisposition = "Content-Disposition"
        val ContentEncoding = "Content-Encoding"
        val ContentLanguage = "Content-Language"
        val ContentLocation = "Content-Location"
        val ContentRange = "Content-Range"
        val ContentSecurityPolicy = "Content-Security-Policy"
        val Etag = "Etag"
        val Expires = "Expires"
        val KeepAlive = "Keep-Alive"
        val LastModified = "Last-Modified"
        val Link = "Link"
        val Location = "Location"
        val NoCache = "No-Cache"
        val ProxyAuthenticate = "Proxy-Authenticate"
        val PublicKeyPins = "Public-Key-Pins"
        val RetryAfter = "Retry-After"
        val Server = "Server"
        val SetCookie = "Set-Cookie"
        val StrictTransportSecurity = "Strict-Transport-Security"
        val Trailer = "Trailer"
        val TransferEncoding = "Transfer-Encoding"
        val TrackingStatusValue = "Tracking-Status-Value"
        val Vary = "Vary"
        val WwwAuthenticate = "WWW-Authenticate"
    }

    object HttpStatus {
        val Continue = 100
        val SwitchingProtocols = 101
        val Processing = 102

        val Ok = 200
        val Created = 201
        val Accepted = 202
        val NonAuthoritativeInformation = 203
        val NoContent = 204
        val ResetContent = 205
        val PartialContent = 206

        val MultipleChoices = 300
        val MovedPermanently = 301
        val Found = 302
        val SeeOther = 303
        val NotModified = 304
        val UseProxy = 305
        val SwitchProxy = 306
        val TemporaryRedirect = 307
        val PermanentRedirect = 308

        val BadRequest = 400
        val Unauthorized = 401
        val PaymentRequired = 402
        val Forbidden = 403
        val NotFound = 404
        val MethodNotAllowed = 405
        val NotAcceptable = 406
        val ProxyAuthenticationRequired = 407
        val RequestTimeout = 408
        val Conflict = 409
        val Gone = 410
        val LengthRequired = 411
        val PreconditionFailed = 412
        val RequestEntityTooLarge = 413
        val RequestURITooLong = 414
        val UnsupportedMediaType = 415
        val RequestedRangeNotSatisfiable = 416
        val ExpectationFailed = 417
        val ImATeapot = 418
        val AuthenticationTimeout = 419
        val EnhanceYourCalm = 420
        val UnprocessableEntity = 422
        val Locked = 423
        val FailedDependency = 424
        val PreconditionRequired = 428
        val TooManyRequests = 429
        val RequestHeaderFieldsTooLarge = 431

        val InternalServerError = 500
        val NotImplemented = 501
        val BadGateway = 502
        val ServiceUnavailable = 503
        val GatewayTimeout = 504
        val HttpVersionNotSupported = 505
        val VariantAlsoNegotiates = 506
        val InsufficientStorage = 507
        val LoopDetected = 508
        val NotExtended = 510
        val NetworkAuthenticationRequired = 511
    }

    val HttpReasonPhrase = mapOf<Int, String>(
            HttpStatus.Continue to "Continue",
            HttpStatus.SwitchingProtocols to "Switching Protocols",
            HttpStatus.Processing to "Processing",

            HttpStatus.Ok to "OK",
            HttpStatus.Created to "Created",
            HttpStatus.Accepted to "Accepted",
            HttpStatus.NonAuthoritativeInformation to "Non Authoritative Information",
            HttpStatus.NoContent to "No Content",
            HttpStatus.ResetContent to "Reset Content",
            HttpStatus.PartialContent to "Partial Content",

            HttpStatus.MultipleChoices to "Multiple Choices",
            HttpStatus.MovedPermanently to "Moved Permanently",
            HttpStatus.Found to "Found",
            HttpStatus.SeeOther to "See Other",
            HttpStatus.NotModified to "Not Modified",
            HttpStatus.UseProxy to "Use Proxy",
            HttpStatus.SwitchProxy to "Switch Proxy",
            HttpStatus.TemporaryRedirect to "Temporary Redirect",
            HttpStatus.PermanentRedirect to "Permanent Redirect",

            HttpStatus.BadRequest to "Bad Request",
            HttpStatus.Unauthorized to "Unauthorized",
            HttpStatus.PaymentRequired to "Payment Required",
            HttpStatus.Forbidden to "Forbidden",
            HttpStatus.NotFound to "Not Found",
            HttpStatus.MethodNotAllowed to "Method Not Allowed",
            HttpStatus.NotAcceptable to "Not Acceptable",
            HttpStatus.ProxyAuthenticationRequired to "Proxy Authentication Required",
            HttpStatus.RequestTimeout to "Request Timeout",
            HttpStatus.Conflict to "Conflict",
            HttpStatus.Gone to "Gone",
            HttpStatus.LengthRequired to "Length Required",
            HttpStatus.PreconditionFailed to "Precondition Failed",
            HttpStatus.RequestEntityTooLarge to "Request Entity Too Large",
            HttpStatus.RequestURITooLong to "Request URI Too Long",
            HttpStatus.UnsupportedMediaType to "Unsupported Media Type",
            HttpStatus.RequestedRangeNotSatisfiable to "Requested Range Not Satisfiable",
            HttpStatus.ExpectationFailed to "Expectation Failed",
            HttpStatus.ImATeapot to "I'm A Teapot",
            HttpStatus.AuthenticationTimeout to "Authentication Timeout",
            HttpStatus.EnhanceYourCalm to "Enhance Your Calm",
            HttpStatus.UnprocessableEntity to "Unprocessable Entity",
            HttpStatus.Locked to "Locked",
            HttpStatus.FailedDependency to "Failed Dependency",
            HttpStatus.PreconditionRequired to "PreconditionR equired",
            HttpStatus.TooManyRequests to "Too Many Requests",
            HttpStatus.RequestHeaderFieldsTooLarge to "Request Header Fields Too Large",

            HttpStatus.InternalServerError to "Internal Server Error",
            HttpStatus.NotImplemented to "Not Implemented",
            HttpStatus.BadGateway to "Bad Gateway",
            HttpStatus.ServiceUnavailable to "Service Unavailable",
            HttpStatus.GatewayTimeout to "Gateway Timeout",
            HttpStatus.HttpVersionNotSupported to "HTTP Version Not Supported",
            HttpStatus.VariantAlsoNegotiates to "Variant Also Negotiates",
            HttpStatus.InsufficientStorage to "Insufficient Storage",
            HttpStatus.LoopDetected to "Loop Detected",
            HttpStatus.NotExtended to "Not Extended",
            HttpStatus.NetworkAuthenticationRequired to "Network Authentication Required"
    )

    fun reasonPhrase(status: Int): String {
        return HttpReasonPhrase[status] ?: "Unknown status"
    }

    class HttpRange(
            val first: Long,
            val last: Long,
            val contentLength: Long
    ) {
        companion object {
            private val range1pat = Regex("^bytes=(-?[\\d]+)(-([-\\d+]+)?)?$")
            fun parse(value: String?, contentlength: Long, max: Long): HttpRange? {
                if (value == null) return null
                val match = range1pat.matchEntire(value) ?: return null
                var first = Without.exceptionOrNull { java.lang.Long.parseLong(match.groupValues[1]) } ?: return null
                var last = first
                if (match.groupValues.size > 2 && match.groupValues[2].isNotEmpty()) {
                    last = contentlength - 1
                    if (match.groupValues.size > 3 && match.groupValues[3].isNotEmpty()) {
                        last = Without.exceptionOrNull { java.lang.Long.parseLong(match.groupValues[3]) } ?: return null
                    }
                }
                if (first < 0) first += contentlength
                if (last < 0) last += contentlength
                if (last >= contentlength) last = contentlength - 1
                if (first > last) return null
                if (first >= contentlength) return null
                if (last - first + 1 > max) last = first + max - 1
                return HttpRange(first, last, contentlength)
            }
        }

        fun contentRange(): String {
            return "bytes $first-$last/$contentLength"
        }

        fun isWholeRange(): Boolean {
            return first == 0L && last >= contentLength - 1
        }

        fun size(): Long {
            return last - first + 1
        }
    }
}
