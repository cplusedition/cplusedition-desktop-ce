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

import java.util.HashMap;
import java.util.Map;

import sf.llk.grammar.html.HtmlSpaceUtil;
import sf.llk.share.formatter.FormatBuilder;
import sf.llk.share.formatter.IOnelinerFormatter;
import sf.llk.share.formatter.OnelinerMain;
import sf.llk.share.formatter.OnelinerUtil;
import sf.llk.share.support.IIntList;
import sf.llk.share.support.ILLKParserInput;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.support.LLKParseError;
import sf.llk.share.support.LLKParserInput;
import sf.llk.share.util.ISpaceUtil;

public class HtmlOnelinerFormatter implements IOnelinerFormatter, ILLKHtmlLexer {

	//////////////////////////////////////////////////////////////////////

	private static final boolean DEBUG = false;
	private static ISpaceUtil spaceUtil;
	private static HtmlOnelinerFormatter singleton;
	private static HtmlOnelinerFormatter relaxedSingleton;

	public static HtmlOnelinerFormatter getSingleton(boolean relax) {
		if (relax) {
			if (relaxedSingleton == null)
				relaxedSingleton = new HtmlOnelinerFormatter(relax);
			return relaxedSingleton;
		}
		if (singleton == null)
			singleton = new HtmlOnelinerFormatter(relax);
		return singleton;
	}

	//////////////////////////////////////////////////////////////////////

	private boolean relax;

	public HtmlOnelinerFormatter(boolean relax) {
		this.relax = relax;
		if (spaceUtil == null)
			spaceUtil = HtmlSpaceUtil.getSingleton();
	}

	//////////////////////////////////////////////////////////////////////

	public String toOneliner(
		CharSequence s, int start, int end, boolean wasspace, int columns, IIntList softbreaks) {
		return toOneliner(s, start, end, wasspace, columns, softbreaks, CONTEXT_TEXT);
	}

	public String toOneliner(
		CharSequence s, int start, int end, boolean wasspace, int columns, IIntList softbreaks, int context) {
		char[] source = OnelinerUtil.getSource(s, start, end);
		StringBuilder ret = new StringBuilder(end - start);
		try {
			HtmlLexer lexer = new HtmlLexer(
				source, new OnelinerMain(DEBUG ? new String(source) : null, createLexerOptions()));
			lexer.pushContext(context);
			ILLKParserInput input = new LLKParserInput(lexer);
			int type = _EOF_;
			while (true) {
				ILLKToken t = input.LT1();
				int toffset = t.getOffset();
				if (toffset < start)
					continue;
				type = t.getType();
				for (ILLKToken st = t.getSpecial(); st != null; st = st.getNext()) {
					int soffset = st.getOffset();
					if (soffset < start)
						continue;
					switch (st.getType()) {
					case SPACES:
					case NEWLINE:
						if (!wasspace) {
							if (softbreaks != null && isSoftBreak(soffset, softbreaks)) {
								st = skipSoftBreak(st);
								break;
							}
							ret.append(' ');
							wasspace = true;
						}
						break;
					default :
						ret.append(st.getText());
						wasspace = false;
						break;
				}}
				if (type == _EOF_)
					break;
				CharSequence str = t.getText();
				switch (type) {
				case TEXT:
				case COMMENT: {
					boolean spacing = type == COMMENT;
					str = trimText(str, wasspace, spacing, toffset, softbreaks);
					break;
				}
				case VOID:
					break;
				case SYSTEM_LITERAL:
				case PUBID_LITERAL:
				case DECLARATION:
				case PI:
				case CDATA:
				case SCRIPT:
				case STYLE:
				case ASP:
				case JSTE:
				case ATTVALUE:
				case STRING:
					if (spaceUtil.hasLineBreak(str))
						return null;
					break;
				case PUBLIC:
				case SYSTEM:
				case LT:
				case GT:
				case ENDTAG:
				case LCOMMENT:
				case RCOMMENT:
				case ASSIGN:
				case LCDATA:
				case RCDATA:
				case LSCRIPT:
				case LSTYLE:
				case RASP:
				case RJSTE:
				case LASP:
				case LJSTE:
				case LPI:
				case RPI:
				case LDECL:
				case NAME:
					break;
				case ENDEMPTY:
					str = "/>";
					break;
				default :
					throw new AssertionError("Invalid token type: " + t);
				}
				ret.append(str);
				if (ret.length() > columns)
					return null;
				wasspace = false;
				input.consume();
		}} catch (RuntimeException e) {
			throw new LLKParseError("Source=" + s, e);
		}
		if (spaceUtil.hasLineBreak(ret))
			return null;
		return ret.toString();
	}

	public void setBreaks(FormatBuilder buf, int context) {
		StringBuilder line = buf.getLineBuffer();
		int end = line.length();
		int start = spaceUtil.skipSpaces(line, 0, end);
		while (end > start && spaceUtil.isSpace(line.charAt(end - 1)))
			--end;
		int len = end - start;
		char[] source = new char[len];
		for (int i = 0; i < len; ++i)
			source[i] = line.charAt(start + i);
		HtmlLexer lexer = new HtmlLexer(
			source, new OnelinerMain(DEBUG ? new String(source) : null, createLexerOptions()));
		lexer.pushContext(context);
		ILLKParserInput input = new LLKParserInput(lexer);
		int type = _EOF_;
		boolean wasspace = true;
		IIntList softbreaks = buf.getSoftBreaks();
		while (true) {
			ILLKToken t = input.LT1();
			int toffset = t.getOffset();
			type = t.getType();
			for (ILLKToken st = t.getSpecial(); st != null; st = st.getNext()) {
				int soffset = st.getOffset();
				switch (st.getType()) {
				case SPACES:
				case NEWLINE:
					if (!wasspace) {
						if (softbreaks != null && isSoftBreak(soffset, softbreaks)) {
							st = skipSoftBreak(st);
							break;
						}
						buf.insertBreak(start + st.getOffset());
						wasspace = true;
					}
					break;
				default :
					wasspace = false;
					break;
			}}
			if (type == _EOF_)
				break;
			switch (type) {
			case TEXT:
			case VOID:
			case COMMENT:
				breakWhitespaces(buf, t.getText(), start + toffset);
				break;
			case SYSTEM_LITERAL:
			case PUBID_LITERAL:
			case PUBLIC:
			case SYSTEM:
			case DECLARATION:
			case PI:
			case SCRIPT:
			case STYLE:
			case ASP:
			case JSTE:
				buf.insertBreak(start + toffset);
				buf.insertBreak(start + t.getEndOffset());
				break;
			case LCOMMENT:
			case ATTVALUE:
				buf.insertBreak(start + t.getEndOffset());
				break;
			case GT:
			case RCOMMENT:
				buf.insertBreak(start + toffset);
				break;
			case LT:
			case ENDEMPTY:
			case ENDTAG:
			case LCDATA:
			case RCDATA:
			case LDECL:
			case LPI:
			case LASP:
			case LJSTE:
			case RPI:
			case RASP:
			case RJSTE:
			case NAME:
			case STRING:
			case LSCRIPT:
			case LSTYLE:
			case ASSIGN:
			case CDATA:
				break;
			default :
				throw new AssertionError("Invalid token type: " + t);
			}
			wasspace = false;
			input.consume();
	}}

	public CharSequence trimText(
		CharSequence str, boolean wasspace, boolean spacing, int offset, IIntList softbreaks) {
		StringBuilder b = new StringBuilder();
		int start = 0;
		int end = str.length();
		char c;
		for (; start < end; ++start) {
			c = str.charAt(start);
			if (spaceUtil.isWhitespace(c)) {
				if (!wasspace) {
					if (softbreaks != null && isSoftBreak(offset + start, softbreaks)) {
						if (c == '\r' && start + 1 < end && str.charAt(start + 1) == '\n')
							++start;
						while (start + 1 < end && spaceUtil.isSpace(str.charAt(start + 1)))
							++start;
						continue;
					}
					b.append(' ');
					wasspace = true;
				}
				if (c == '\r' && start + 1 < end && str.charAt(start + 1) == '\n')
					++start;
				continue;
			}
			wasspace = false;
			b.append(c);
		}
		if (spacing && b.length() > 0) {
			if (!spaceUtil.isWhitespace(b.charAt(0)))
				b.insert(0, ' ');
			if (!spaceUtil.isWhitespace(b.charAt(b.length() - 1)))
				b.append(' ');
		}
		return b;
	}

	//////////////////////////////////////////////////////////////////////

	private static boolean isSoftBreak(int i, IIntList breaks) {
		return breaks.binarySearch(i) >= 0;
	}

	private static ILLKToken skipSoftBreak(ILLKToken st) {
		for (ILLKToken n = st.getNext(); n != null; st = n, n = n.getNext()) {
			int next = n.getType();
			if (next != SPACES)
				break;
		}
		return st;
	}

	private void breakWhitespaces(FormatBuilder buf, CharSequence text, int startoffset) {
		boolean wasspace = false;
		int start = 0;
		int end = text.length();
		for (char c; start < end; ++start) {
			c = text.charAt(start);
			if (spaceUtil.isWhitespace(c)) {
				if (!wasspace) {
					buf.insertBreak(startoffset + start);
					wasspace = true;
				}
				if (c == '\r' && start + 1 < end && text.charAt(start + 1) == '\n')
					++start;
				continue;
			}
			wasspace = false;
	}}

	private Map<String, Object> createLexerOptions() {
		if (!relax)
			return null;
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("relax", "true");
		return ret;
	}
	//////////////////////////////////////////////////////////////////////
}
