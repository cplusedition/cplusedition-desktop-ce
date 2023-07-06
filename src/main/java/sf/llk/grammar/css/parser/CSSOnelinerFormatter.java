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
package sf.llk.grammar.css.parser;

import sf.llk.share.formatter.OnelinerMain;
import sf.llk.share.formatter.AbstractParserInputOneliner;
import sf.llk.share.support.ILLKLexerInput;
import sf.llk.share.support.ILLKParserInput;
import sf.llk.share.support.LLKLexerInput;
import sf.llk.share.support.LLKParserInput;

public class CSSOnelinerFormatter extends AbstractParserInputOneliner implements ILLKCSSLexer {

	//////////////////////////////////////////////////////////////////////

	private static final boolean DEBUG = false;
	private static CSSOnelinerFormatter singleton;

	public static CSSOnelinerFormatter getSingleton() {
		if (singleton == null)
			singleton = new CSSOnelinerFormatter();
		return singleton;
	}

	public CSSOnelinerFormatter() {
		super(CSSSpaceUtil.getSingleton(), SPACES, NEWLINE, -1, COMMENT);
	}

	protected ILLKParserInput createParserInput(char[] source) {
		ILLKLexerInput input = new LLKLexerInput(source, new OnelinerMain(DEBUG ? new String(source) : null));
		CSSLexer lexer = new CSSLexer(input);
		return new LLKParserInput(lexer);
	}

	//////////////////////////////////////////////////////////////////////
}
