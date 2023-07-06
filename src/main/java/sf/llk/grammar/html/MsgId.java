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
package sf.llk.grammar.html;

public enum MsgId {

	LexerMissingDash("Possibly missing '-' for comment start tag"),
	Lexer_NotValid("'_' is not a valid NameStartChar"),
	LexerQuoteValue("Illegal character for unquoted attribute value"),
	LexerEscAmp("Dangle '&' character possibly should be escaped as &amp;"),
	LexerEscLT("Dangle '<' character possibly should be escaped as &lt;"),
	LexerEscEndTag("</ should be escaped"),
	LexerSystem("SYSTEM keyword must be in upper case"),
	LexerPublic("PUBLIC keyword must be in upper case"),
	LexerQuoteChar("Character should be quoted"),
	DomUnexpectedLiteral("Unexpected literal"),
	DomMissingEndTag("End tag do not match start tag (possibly missing end tag)"),
	DomMissingOptionalEndTag("End tag do not match start tag (possibly missing optional end tag)"),
	DomUnknownStartTag("Unknown start tag"),
	DomUnknownEndTag("Unknown end tag (possibly mis-spelled)"),
	DomGTExpected("'>' is expected"),
	DomMismatchEndTag("End tag do not match start tag"),
	DomMissingAssign("Missing '=' for attribute value"),
	DomMissingValue("Missing attribute value"),
	DomEndEmptyTag("End tag for empty tag ignored"),
	DomImplicitEndTag("Implicit optional end tag"),
	DomXmlEmptyTag("XML style empty tag used"),
	DomExtraEndTag("Extra end tag (possibly for optional end tag that has been asserted implicitly)"),
	DomUnknownDeclaration("Unknown declaration"),
	;

	private String message;

	MsgId(String msg) {
		this.message = msg;
	}

	public String getMessage() {
		return message;
	}
}
