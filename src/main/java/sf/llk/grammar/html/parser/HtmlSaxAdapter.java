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
package sf.llk.grammar.html.parser;

import java.io.IOException;

import sf.llk.grammar.html.IHtmlHandler;
import sf.llk.share.support.ILLKMain;
import sf.llk.share.support.LLKParseException;

public class HtmlSaxAdapter implements IHtmlHandler {

    ////////////////////////////////////////////////////////////////////////

    HtmlSaxParser parser;

    ////////////////////////////////////////////////////////////////////////

    public HtmlSaxAdapter(IHtmlLexer lexer) throws IOException {
        this.parser = new HtmlSaxParser(lexer, this);
    }

    public HtmlSaxAdapter(char[] source, ILLKMain main) throws IOException {
        this(new HtmlLexer(source, main));
    }

    ////////////////////////////////////////////////////////////////////////

    public ASTdocument parse() throws LLKParseException {
        return parser.document();
    }

    ////////////////////////////////////////////////////////////////////////

    public void startDocument(ASTdocument node) throws LLKParseException {
    }

    public void doctype(ASTdoctype node) throws LLKParseException {
    }

    public void comment(ASTcomment node) throws LLKParseException {
    }

    public void pi(ASTpi node) throws LLKParseException {
    }

    public void cdata(ASTcdata node) throws LLKParseException {
    }

    public void cond(ASTcond node) throws LLKParseException {
    }

    public void declaration(ASTdeclaration node) throws LLKParseException {
    }

    public void startTag(ASTstartTag node) throws LLKParseException {
    }

    public void endTag(ASTendTag node) throws LLKParseException {
    }

    public void script(ASTtext node) throws LLKParseException {
    }

    public void text(ASTtext node) throws LLKParseException {
    }

    public void endDocument(ASTdocument node) throws LLKParseException {
    }
}
