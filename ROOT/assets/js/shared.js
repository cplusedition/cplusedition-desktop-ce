"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.HttpReasonPhrase = exports.HttpStatus = exports.HttpHeader = void 0;
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
const botcore_1 = require("./bot/botcore");
class HttpHeader {
}
HttpHeader.Accept = "Accept";
HttpHeader.AcceptCharset = "Accept-Charset";
HttpHeader.AcceptEncoding = "Accept-Encoding";
HttpHeader.AcceptLanguage = "Accept-Language";
HttpHeader.Authorization = "Authorization";
HttpHeader.CacheControl = "Cache-Control";
HttpHeader.Connection = "Connection";
HttpHeader.Cookie = "Cookie";
HttpHeader.ContentLength = "Content-Length";
HttpHeader.ContentType = "Content-Type";
HttpHeader.Date = "Date";
HttpHeader.Expect = "Expect";
HttpHeader.Forwarded = "Forwarded";
HttpHeader.From = "From";
HttpHeader.Host = "Host";
HttpHeader.IfMatch = "If-Match";
HttpHeader.IfModifiedSince = "If-Modified-Since";
HttpHeader.IfNoneMatch = "If-None-Match";
HttpHeader.IfRange = "If-Range";
HttpHeader.IfUnmodifiedSince = "If-Unmodified-Since";
HttpHeader.MaxForwards = "Max-Forwards";
HttpHeader.Origin = "Origin";
HttpHeader.Pragma = "Pragma";
HttpHeader.ProxyAuthorization = "Proxy-Authorization";
HttpHeader.Range = "Range";
HttpHeader.Referrer = "Referrer";
HttpHeader.TransferCodingExceptions = "Transfer-Coding-Exceptions";
HttpHeader.UserAgent = "User-Agent";
HttpHeader.Upgrade = "Upgrade";
HttpHeader.Via = "Via";
HttpHeader.Warning = "Warning";
HttpHeader.AcceptPatch = "Accept-Patch";
HttpHeader.AcceptRanges = "Accept-Ranges";
HttpHeader.Age = "Age";
HttpHeader.Allow = "Allow";
HttpHeader.AlternativeServices = "Alternative-Services";
HttpHeader.ContentDisposition = "Content-Disposition";
HttpHeader.ContentEncoding = "Content-Encoding";
HttpHeader.ContentLanguage = "Content-Language";
HttpHeader.ContentLocation = "Content-Location";
HttpHeader.ContentRange = "Content-Range";
HttpHeader.ContentSecurityPolicy = "Content-Security-Policy";
HttpHeader.Etag = "Etag";
HttpHeader.Expires = "Expires";
HttpHeader.KeepAlive = "Keep-Alive";
HttpHeader.LastModified = "Last-Modified";
HttpHeader.Link = "Link";
HttpHeader.Location = "Location";
HttpHeader.NoCache = "No-Cache";
HttpHeader.ProxyAuthenticate = "Proxy-Authenticate";
HttpHeader.PublicKeyPins = "Public-Key-Pins";
HttpHeader.RetryAfter = "Retry-After";
HttpHeader.Server = "Server";
HttpHeader.SetCookie = "Set-Cookie";
HttpHeader.StrictTransportSecurity = "Strict-Transport-Security";
HttpHeader.Trailer = "Trailer";
HttpHeader.TransferEncoding = "Transfer-Encoding";
HttpHeader.TrackingStatusValue = "Tracking-Status-Value";
HttpHeader.Vary = "Vary";
HttpHeader.WwwAuthenticate = "WWW-Authenticate";
exports.HttpHeader = HttpHeader;
class HttpStatus {
}
HttpStatus.Continue = 100;
HttpStatus.SwitchingProtocols = 101;
HttpStatus.Processing = 102;
HttpStatus.Ok = 200;
HttpStatus.Created = 201;
HttpStatus.Accepted = 202;
HttpStatus.NonAuthoritativeInformation = 203;
HttpStatus.NoContent = 204;
HttpStatus.ResetContent = 205;
HttpStatus.PartialContent = 206;
HttpStatus.MultipleChoices = 300;
HttpStatus.MovedPermanently = 301;
HttpStatus.Found = 302;
HttpStatus.SeeOther = 303;
HttpStatus.NotModified = 304;
HttpStatus.UseProxy = 305;
HttpStatus.SwitchProxy = 306;
HttpStatus.TemporaryRedirect = 307;
HttpStatus.PermanentRedirect = 308;
HttpStatus.BadRequest = 400;
HttpStatus.Unauthorized = 401;
HttpStatus.PaymentRequired = 402;
HttpStatus.Forbidden = 403;
HttpStatus.NotFound = 404;
HttpStatus.MethodNotAllowed = 405;
HttpStatus.NotAcceptable = 406;
HttpStatus.ProxyAuthenticationRequired = 407;
HttpStatus.RequestTimeout = 408;
HttpStatus.Conflict = 409;
HttpStatus.Gone = 410;
HttpStatus.LengthRequired = 411;
HttpStatus.PreconditionFailed = 412;
HttpStatus.RequestEntityTooLarge = 413;
HttpStatus.RequestURITooLong = 414;
HttpStatus.UnsupportedMediaType = 415;
HttpStatus.RequestedRangeNotSatisfiable = 416;
HttpStatus.ExpectationFailed = 417;
HttpStatus.ImATeapot = 418;
HttpStatus.AuthenticationTimeout = 419;
HttpStatus.EnhanceYourCalm = 420;
HttpStatus.UnprocessableEntity = 422;
HttpStatus.Locked = 423;
HttpStatus.FailedDependency = 424;
HttpStatus.PreconditionRequired = 428;
HttpStatus.TooManyRequests = 429;
HttpStatus.RequestHeaderFieldsTooLarge = 431;
HttpStatus.InternalServerError = 500;
HttpStatus.NotImplemented = 501;
HttpStatus.BadGateway = 502;
HttpStatus.ServiceUnavailable = 503;
HttpStatus.GatewayTimeout = 504;
HttpStatus.HttpVersionNotSupported = 505;
HttpStatus.VariantAlsoNegotiates = 506;
HttpStatus.InsufficientStorage = 507;
HttpStatus.LoopDetected = 508;
HttpStatus.NotExtended = 510;
HttpStatus.NetworkAuthenticationRequired = 511;
exports.HttpStatus = HttpStatus;
exports.HttpReasonPhrase = (0, botcore_1.map_)([HttpStatus.Continue, "Continue"], [HttpStatus.SwitchingProtocols, "Switching Protocols"], [HttpStatus.Processing, "Processing"], [HttpStatus.Ok, "OK"], [HttpStatus.Created, "Created"], [HttpStatus.Accepted, "Accepted"], [HttpStatus.NonAuthoritativeInformation, "Non Authoritative Information"], [HttpStatus.NoContent, "No Content"], [HttpStatus.ResetContent, "Reset Content"], [HttpStatus.PartialContent, "Partial Content"], [HttpStatus.MultipleChoices, "Multiple Choices"], [HttpStatus.MovedPermanently, "Moved Permanently"], [HttpStatus.Found, "Found"], [HttpStatus.SeeOther, "See Other"], [HttpStatus.NotModified, "Not Modified"], [HttpStatus.UseProxy, "Use Proxy"], [HttpStatus.SwitchProxy, "Switch Proxy"], [HttpStatus.TemporaryRedirect, "Temporary Redirect"], [HttpStatus.PermanentRedirect, "Permanent Redirect"], [HttpStatus.BadRequest, "Bad Request"], [HttpStatus.Unauthorized, "Unauthorized"], [HttpStatus.PaymentRequired, "Payment Required"], [HttpStatus.Forbidden, "Forbidden"], [HttpStatus.NotFound, "Not Found"], [HttpStatus.MethodNotAllowed, "Method Not Allowed"], [HttpStatus.NotAcceptable, "Not Acceptable"], [HttpStatus.ProxyAuthenticationRequired, "Proxy Authentication Required"], [HttpStatus.RequestTimeout, "Request Timeout"], [HttpStatus.Conflict, "Conflict"], [HttpStatus.Gone, "Gone"], [HttpStatus.LengthRequired, "Length Required"], [HttpStatus.PreconditionFailed, "Precondition Failed"], [HttpStatus.RequestEntityTooLarge, "Request Entity Too Large"], [HttpStatus.RequestURITooLong, "Request URI Too Long"], [HttpStatus.UnsupportedMediaType, "Unsupported Media Type"], [HttpStatus.RequestedRangeNotSatisfiable, "Requested Range Not Satisfiable"], [HttpStatus.ExpectationFailed, "Expectation Failed"], [HttpStatus.ImATeapot, "I'm A Teapot"], [HttpStatus.AuthenticationTimeout, "Authentication Timeout"], [HttpStatus.EnhanceYourCalm, "Enhance Your Calm"], [HttpStatus.UnprocessableEntity, "Unprocessable Entity"], [HttpStatus.Locked, "Locked"], [HttpStatus.FailedDependency, "Failed Dependency"], [HttpStatus.PreconditionRequired, "PreconditionR equired"], [HttpStatus.TooManyRequests, "Too Many Requests"], [HttpStatus.RequestHeaderFieldsTooLarge, "Request Header Fields Too Large"], [HttpStatus.InternalServerError, "Internal Server Error"], [HttpStatus.NotImplemented, "Not Implemented"], [HttpStatus.BadGateway, "Bad Gateway"], [HttpStatus.ServiceUnavailable, "Service Unavailable"], [HttpStatus.GatewayTimeout, "Gateway Timeout"], [HttpStatus.HttpVersionNotSupported, "HTTP Version Not Supported"], [HttpStatus.VariantAlsoNegotiates, "Variant Also Negotiates"], [HttpStatus.InsufficientStorage, "Insufficient Storage"], [HttpStatus.LoopDetected, "Loop Detected"], [HttpStatus.NotExtended, "Not Extended"], [HttpStatus.NetworkAuthenticationRequired, "Network Authentication Required"]);
//# sourceMappingURL=shared.js.map