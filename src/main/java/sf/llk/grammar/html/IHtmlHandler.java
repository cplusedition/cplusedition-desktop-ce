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

import sf.llk.grammar.html.parser.ASTcdata;
import sf.llk.grammar.html.parser.ASTcomment;
import sf.llk.grammar.html.parser.ASTcond;
import sf.llk.grammar.html.parser.ASTdeclaration;
import sf.llk.grammar.html.parser.ASTdoctype;
import sf.llk.grammar.html.parser.ASTdocument;
import sf.llk.grammar.html.parser.ASTendTag;
import sf.llk.grammar.html.parser.ASTpi;
import sf.llk.grammar.html.parser.ASTstartTag;
import sf.llk.grammar.html.parser.ASTtext;
import sf.llk.share.support.LLKParseException;

public interface IHtmlHandler {

	void startDocument(ASTdocument node) throws LLKParseException;
	void doctype(ASTdoctype node) throws LLKParseException;
	void comment(ASTcomment node) throws LLKParseException;
	void pi(ASTpi node) throws LLKParseException;
	void cdata(ASTcdata node) throws LLKParseException;
	void cond(ASTcond node) throws LLKParseException;
	void declaration(ASTdeclaration node) throws LLKParseException;
	void startTag(ASTstartTag node) throws LLKParseException;
	void endTag(ASTendTag node) throws LLKParseException;
	void script(ASTtext node) throws LLKParseException;
	void text(ASTtext node) throws LLKParseException;
	void endDocument(ASTdocument node) throws LLKParseException;
}
