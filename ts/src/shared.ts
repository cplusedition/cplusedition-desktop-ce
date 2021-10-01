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
import { map_ } from "./bot/botcore";

export abstract class HttpHeader {
    static readonly Accept = "Accept";
    static readonly AcceptCharset = "Accept-Charset";
    static readonly AcceptEncoding = "Accept-Encoding";
    static readonly AcceptLanguage = "Accept-Language";
    static readonly Authorization = "Authorization";
    static readonly CacheControl = "Cache-Control";
    static readonly Connection = "Connection";
    static readonly Cookie = "Cookie";
    static readonly ContentLength = "Content-Length";
    static readonly ContentType = "Content-Type";
    static readonly Date = "Date";
    static readonly Expect = "Expect";
    static readonly Forwarded = "Forwarded";
    static readonly From = "From";
    static readonly Host = "Host";
    static readonly IfMatch = "If-Match";
    static readonly IfModifiedSince = "If-Modified-Since";
    static readonly IfNoneMatch = "If-None-Match";
    static readonly IfRange = "If-Range";
    static readonly IfUnmodifiedSince = "If-Unmodified-Since";
    static readonly MaxForwards = "Max-Forwards";
    static readonly Origin = "Origin";
    static readonly Pragma = "Pragma";
    static readonly ProxyAuthorization = "Proxy-Authorization";
    static readonly Range = "Range";
    static readonly Referrer = "Referrer";
    static readonly TransferCodingExceptions = "Transfer-Coding-Exceptions";
    static readonly UserAgent = "User-Agent";
    static readonly Upgrade = "Upgrade";
    static readonly Via = "Via";
    static readonly Warning = "Warning";

    static readonly AcceptPatch = "Accept-Patch";
    static readonly AcceptRanges = "Accept-Ranges";
    static readonly Age = "Age";
    static readonly Allow = "Allow";
    static readonly AlternativeServices = "Alternative-Services";
    static readonly ContentDisposition = "Content-Disposition";
    static readonly ContentEncoding = "Content-Encoding";
    static readonly ContentLanguage = "Content-Language";
    static readonly ContentLocation = "Content-Location";
    static readonly ContentRange = "Content-Range";
    static readonly ContentSecurityPolicy = "Content-Security-Policy";
    static readonly Etag = "Etag";
    static readonly Expires = "Expires";
    static readonly KeepAlive = "Keep-Alive";
    static readonly LastModified = "Last-Modified";
    static readonly Link = "Link";
    static readonly Location = "Location";
    static readonly NoCache = "No-Cache";
    static readonly ProxyAuthenticate = "Proxy-Authenticate";
    static readonly PublicKeyPins = "Public-Key-Pins";
    static readonly RetryAfter = "Retry-After";
    static readonly Server = "Server";
    static readonly SetCookie = "Set-Cookie";
    static readonly StrictTransportSecurity = "Strict-Transport-Security";
    static readonly Trailer = "Trailer";
    static readonly TransferEncoding = "Transfer-Encoding";
    static readonly TrackingStatusValue = "Tracking-Status-Value";
    static readonly Vary = "Vary";
    static readonly WwwAuthenticate = "WWW-Authenticate";
}

export abstract class HttpStatus {
    static readonly Continue = 100;
    static readonly SwitchingProtocols = 101;
    static readonly Processing = 102;

    static readonly Ok = 200;
    static readonly Created = 201;
    static readonly Accepted = 202;
    static readonly NonAuthoritativeInformation = 203;
    static readonly NoContent = 204;
    static readonly ResetContent = 205;
    static readonly PartialContent = 206;

    static readonly MultipleChoices = 300;
    static readonly MovedPermanently = 301;
    static readonly Found = 302;
    static readonly SeeOther = 303;
    static readonly NotModified = 304;
    static readonly UseProxy = 305;
    static readonly SwitchProxy = 306;
    static readonly TemporaryRedirect = 307;
    static readonly PermanentRedirect = 308;

    static readonly BadRequest = 400;
    static readonly Unauthorized = 401;
    static readonly PaymentRequired = 402;
    static readonly Forbidden = 403;
    static readonly NotFound = 404;
    static readonly MethodNotAllowed = 405;
    static readonly NotAcceptable = 406;
    static readonly ProxyAuthenticationRequired = 407;
    static readonly RequestTimeout = 408;
    static readonly Conflict = 409;
    static readonly Gone = 410;
    static readonly LengthRequired = 411;
    static readonly PreconditionFailed = 412;
    static readonly RequestEntityTooLarge = 413;
    static readonly RequestURITooLong = 414;
    static readonly UnsupportedMediaType = 415;
    static readonly RequestedRangeNotSatisfiable = 416;
    static readonly ExpectationFailed = 417;
    static readonly ImATeapot = 418;
    static readonly AuthenticationTimeout = 419;
    static readonly EnhanceYourCalm = 420;
    static readonly UnprocessableEntity = 422;
    static readonly Locked = 423;
    static readonly FailedDependency = 424;
    static readonly PreconditionRequired = 428;
    static readonly TooManyRequests = 429;
    static readonly RequestHeaderFieldsTooLarge = 431;

    static readonly InternalServerError = 500;
    static readonly NotImplemented = 501;
    static readonly BadGateway = 502;
    static readonly ServiceUnavailable = 503;
    static readonly GatewayTimeout = 504;
    static readonly HttpVersionNotSupported = 505;
    static readonly VariantAlsoNegotiates = 506;
    static readonly InsufficientStorage = 507;
    static readonly LoopDetected = 508;
    static readonly NotExtended = 510;
    static readonly NetworkAuthenticationRequired = 511;
}

export const HttpReasonPhrase = map_(
    [HttpStatus.Continue, "Continue"],
    [HttpStatus.SwitchingProtocols, "Switching Protocols"],
    [HttpStatus.Processing, "Processing"],

    [HttpStatus.Ok, "OK"],
    [HttpStatus.Created, "Created"],
    [HttpStatus.Accepted, "Accepted"],
    [HttpStatus.NonAuthoritativeInformation, "Non Authoritative Information"],
    [HttpStatus.NoContent, "No Content"],
    [HttpStatus.ResetContent, "Reset Content"],
    [HttpStatus.PartialContent, "Partial Content"],

    [HttpStatus.MultipleChoices, "Multiple Choices"],
    [HttpStatus.MovedPermanently, "Moved Permanently"],
    [HttpStatus.Found, "Found"],
    [HttpStatus.SeeOther, "See Other"],
    [HttpStatus.NotModified, "Not Modified"],
    [HttpStatus.UseProxy, "Use Proxy"],
    [HttpStatus.SwitchProxy, "Switch Proxy"],
    [HttpStatus.TemporaryRedirect, "Temporary Redirect"],
    [HttpStatus.PermanentRedirect, "Permanent Redirect"],

    [HttpStatus.BadRequest, "Bad Request"],
    [HttpStatus.Unauthorized, "Unauthorized"],
    [HttpStatus.PaymentRequired, "Payment Required"],
    [HttpStatus.Forbidden, "Forbidden"],
    [HttpStatus.NotFound, "Not Found"],
    [HttpStatus.MethodNotAllowed, "Method Not Allowed"],
    [HttpStatus.NotAcceptable, "Not Acceptable"],
    [HttpStatus.ProxyAuthenticationRequired, "Proxy Authentication Required"],
    [HttpStatus.RequestTimeout, "Request Timeout"],
    [HttpStatus.Conflict, "Conflict"],
    [HttpStatus.Gone, "Gone"],
    [HttpStatus.LengthRequired, "Length Required"],
    [HttpStatus.PreconditionFailed, "Precondition Failed"],
    [HttpStatus.RequestEntityTooLarge, "Request Entity Too Large"],
    [HttpStatus.RequestURITooLong, "Request URI Too Long"],
    [HttpStatus.UnsupportedMediaType, "Unsupported Media Type"],
    [HttpStatus.RequestedRangeNotSatisfiable, "Requested Range Not Satisfiable"],
    [HttpStatus.ExpectationFailed, "Expectation Failed"],
    [HttpStatus.ImATeapot, "I'm A Teapot"],
    [HttpStatus.AuthenticationTimeout, "Authentication Timeout"],
    [HttpStatus.EnhanceYourCalm, "Enhance Your Calm"],
    [HttpStatus.UnprocessableEntity, "Unprocessable Entity"],
    [HttpStatus.Locked, "Locked"],
    [HttpStatus.FailedDependency, "Failed Dependency"],
    [HttpStatus.PreconditionRequired, "PreconditionR equired"],
    [HttpStatus.TooManyRequests, "Too Many Requests"],
    [HttpStatus.RequestHeaderFieldsTooLarge, "Request Header Fields Too Large"],

    [HttpStatus.InternalServerError, "Internal Server Error"],
    [HttpStatus.NotImplemented, "Not Implemented"],
    [HttpStatus.BadGateway, "Bad Gateway"],
    [HttpStatus.ServiceUnavailable, "Service Unavailable"],
    [HttpStatus.GatewayTimeout, "Gateway Timeout"],
    [HttpStatus.HttpVersionNotSupported, "HTTP Version Not Supported"],
    [HttpStatus.VariantAlsoNegotiates, "Variant Also Negotiates"],
    [HttpStatus.InsufficientStorage, "Insufficient Storage"],
    [HttpStatus.LoopDetected, "Loop Detected"],
    [HttpStatus.NotExtended, "Not Extended"],
    [HttpStatus.NetworkAuthenticationRequired, "Network Authentication Required"],
);
