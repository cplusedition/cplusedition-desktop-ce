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

import sf.llk.grammar.html.IHtmlConstants;
import sf.llk.grammar.html.IHtmlLexicalHandler;
import sf.llk.grammar.html.MsgId;
import sf.llk.share.support.ILLKConstants;
import sf.llk.share.support.ILLKLexerInput;
import sf.llk.share.support.ILLKLifeCycleListener;
import sf.llk.share.support.ILLKMain;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.support.ILLKTokenHandler;
import sf.llk.share.support.INamePool;
import sf.llk.share.support.IntList;
import sf.llk.share.support.LLKLexerBase;
import sf.llk.share.support.LLKLexerInput;
import sf.llk.share.support.LLKParseException;
import sf.llk.share.support.LexerSourceTokenHandler;
import sf.llk.share.support.NamePool;

public class HtmlLexer extends LLKLexerBase implements IHtmlLexer, ILLKHtmlLexer {

	////////////////////////////////////////////////////////////////////////

	private static final int[] END_PI = new int[] { '?', '>' };
	private static final int[] END_ASP = new int[] { '%', '>' };
	private static final int[] END_JSTE = new int[] { '#', '>' };
	private static final int[] END_CDATA = new int[] { ']', ']', '>' };
	private static final int[] END_COND = new int[] { ']', '>' };
	private static final int[] END_DECLARATION = new int[] { '>' };
	private static final int[] END_SCRIPT = new int[] { '<', '/' };

	////////////////////////////////////////////////////////////////////////

	protected IHtmlLexicalHandler lexHandler;
	protected INamePool namePool;
	protected boolean attlistDeclaration;
	protected boolean relax;
	protected IntList contextStack;

	//////////////////////////////////////////////////////////////////////

	public HtmlLexer(char[] source, ILLKMain main) {
		llkInput = new LLKLexerInput(source, main);
		llkMain = llkInput.getMain();
		llkLocator = llkInput.getLocator();
		llkTokenHandler = new LexerSourceTokenHandler(source, new NamePool(1024, 2.0f), LLK_SPECIAL_TOKENS);
		llkInit();
	}

	public HtmlLexer(ILLKLexerInput input, ILLKTokenHandler tokenhandler) {
		llkInput = input;
		llkMain = llkInput.getMain();
		llkLocator = llkInput.getLocator();
		llkTokenHandler = tokenhandler;
		namePool = tokenhandler.namePool();
		llkInit();
	}

	//////////////////////////////////////////////////////////////////////

	public static boolean isNameStartChar(int c) {
		return llkGetBit(c, NameStartChar.bitset);
	}

	public static boolean isNameChar(int c) {
		return llkGetBit(c, NameChar.bitset);
	}

	public static int[] specialTokens() {
		return LLK_SPECIAL_TOKENS.clone();
	}

	//////////////////////////////////////////////////////////////////////

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public void llkInit() {
		this.relax = llkMain.getOptBool(IHtmlConstants.OPT_RELAX);
		namePool = llkTokenHandler.namePool();
		contextStack = new IntList();
		pushContext(CONTEXT_TEXT);
	}

	public ILLKToken createDataToken(int type, Object data) {
		return llkTokenHandler.createToken(type, llkTokenStart, llkGetOffset(), data);
	}

	public void setLexicalHandler(IHtmlLexicalHandler handler) {
		this.lexHandler = handler;
	}

	public IHtmlLexicalHandler getLexicalHandler() {
		return lexHandler;
	}

	//////////////////////////////////////////////////////////////////////

	public int getLine() {
		return llkInput.getLocator().getLinear(llkGetOffset());
	}

	public String getLineString() {
		return "@" + getLine();
	}

	public void pushContext(int context) {
		contextStack.push(llkGetContext(CONTEXT));
		llkSetContext(CONTEXT, context);
	}

	public void popContext() {
		llkSetContext(CONTEXT, contextStack.pop());
	}

	//////////////////////////////////////////////////////////////////////

	protected ILLKToken checkEOF() throws LLKParseException {
		if (LA1() == ILLKConstants.LEXER_EOF)
			return llkCreateEOF(llkTokenStart);
		throw llkParseException("Unexpected token");
	}

	private boolean matchIgnoreCase(int offset, String name) {
		for (int i = 0; i < name.length(); ++i) {
			int la = LA(offset + i);
			if (la < 0 || Character.toLowerCase(la) != name.charAt(i))
				return false;
		}
		return true;
	}

	private int optionalSpaces(int n) {
		for (int la = LA(n); la == ' ' || la == '\t';) {
			la = LA(++n);
		}
		return n;
	}

	private void adjustLocation(int la) {
		if (la == '\t')
			llkInput.tab();
		else if (la == '\n')
			llkInput.newline();
		else if (la == '\r') {
			if (LA1() == '\n')
				llkConsume();
			llkInput.newline();
		}
	}

	private boolean isValidTag() {
		llkInput.mark();
		int start = llkInput.getOffset();
		boolean ret = llkSyn_NAME();
		llkInput.rewind();
		return ret;
	}

	private boolean isEndOfAttributes() {
		int la1 = LA1();
		if (la1 == '>')
			return true;
		if ((la1 == '/' || la1 == '?' || la1 == '%' || la1 == '#') && LA(2) == '>')
			return true;
		return false;
	}

	private void warn(MsgId id, Exception e, int offset) {
		llkMain.warn(id.getMessage(), e, offset);
	}

	private void warn(MsgId id, String msg, Exception e, int offset) {
		llkMain.warn(id.getMessage() + ": " + msg, e, offset);
	}

	private void error(MsgId id, Exception e, int offset) {
		llkMain.error(id.getMessage(), e, offset);
	}

	////////////////////////////////////////////////////////////////////////

	public static final boolean LLK_HAS_SPECIAL_TOKEN = true;
	public static final boolean LLK_TOKEN_USE_TEXT = true;
	public static final boolean LLK_TOKEN_USE_INTERN = false;
	public static final boolean LLK_IGNORE_CASE = false;
	public static final int LLK_INPUT_VOCAB_SIZE = 0x10000;
	public static class Keyword {
		protected int type;
		public Keyword(int type) {
			this.type=type;
		}
	}
	static final char[] LLK_STRING_script = {
		's', 'c', 'r', 'i', 'p', 't'
	};
	static final char[] LLK_STRING_0000 = new char[] {
		']', ']', '>'
	};
	static final char[] LLK_STRING_0001 = new char[] {
		'C', 'D', 'A', 'T', 'A', '['
	};
	static final int[] LLK_SPECIAL_TOKENS = new int[] {
		0x00000002, 0x00000c00, 
	};
	static final int[] LLK_LITERAL_TOKENS = new int[] {
		0x003ffff0, 
	};
	static final int[] LLK_KEYWORD_TOKENS = new int[] {
		0x00000030, 
	};

	protected int[] llkContexts = new int[1];

	public ILLKToken llkReset() {
		llkInput.reset();
		llkTokenHandler.reset();
		llkSeenEOF = false;
		llkInit();
		for (ILLKLifeCycleListener l: llkLifeCycleListeners) {
			l.reset();
		}
		return llkTokenHandler.getToken0();
	}

	////////////////////////////////////////////////////////////

	public ILLKToken llkNextToken() {
		while (true) {
			if (llkNextToken1()) {
				return llkTokenHandler.getToken0();
			}
		}
	}

	public int llkNextType() {
		while (true) {
			if (llkNextToken1()) {
				return llkTokenHandler.getType0();
			}
		}
	}

	public boolean llkNextToken1() {
		ILLKToken llktoken1 = null;
		int llktype1 = _INVALID_;
		llkTokenStart = llkGetOffset();
		try {
			switch (llkContexts[CONTEXT]) {
				case CONTEXT_TEXT:
					if (LA1() == '<') {
						llktoken1 = START_TAG();
					} else if (llkInvertedBitTest(LA1(), 65536, '<')) {
						_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
							switch (LA1()) {
								case '\t':
									llkInput.consume();
									llkInput.tab();
									break;
								case '\n':
									llkInput.consume();
									llkInput.newline();
									break;
								case '\r':
									llkInput.consume();
									llkInput.matchOpt('\n');
									llkInput.newline();
									break;
								case '&':
									REFERENCE();
									break;
								default:
									if (llkGetBitInverted(LA1(), LLKTokenSet4.bitset)) {
										llkInput.consume();
									} else {
										if (!_cnt1) {
											throw llkParseException("()+ expected at least one occuence");
										}
										break _loop1;
									}
							}
						}
						llktype1 = TEXT;
					} else {
						llktoken1 = checkEOF();
					}
					break;
				case CONTEXT_TAG:
					switch (LA1()) {
						case '<':
							llktoken1 = START_TAG();
							break;
						case '/':
							llkInput.consume();
							if (relax && llkGetBit(LA1(), Whitespaces.bitset)) {
								_loop2: for (boolean _cnt2 = false;; _cnt2 = true) {
									if (llkBitTest(LA1(), '\t', '\u000c', ' ', '\u200b')) {
										SPACES();
									} else if (LA1() == '\n' || LA1() == '\r') {
										NEWLINE();
									} else {
										if (!_cnt2) {
											throw llkParseException("()+ expected at least one occuence");
										}
										break _loop2;
									}
								}
							}
							llkInput.match('>');
							llktype1 = ENDEMPTY;
							popContext();
							break;
						case '>':
							llkInput.consume();
							llktype1 = GT;
							popContext();
							break;
						case '-':
							llkInput.consume();
							llkInput.match('-');
							if (llkBitTest(LA1(), '\t', '\u000c', ' ', '\u200b')) {
								SPACES();
							}
							llkInput.match('>');
							llktype1 = RCOMMENT;
							popContext();
							break;
						case '?':
							llkInput.consume();
							llkInput.match('>');
							llktype1 = RPI;
							popContext();
							break;
						case '%':
							llkInput.consume();
							llkInput.match('>');
							llktype1 = RASP;
							popContext();
							break;
						case '#':
							llkInput.consume();
							llkInput.match('>');
							llktype1 = RJSTE;
							popContext();
							break;
						case '=':
							llkInput.consume();
							llktype1 = ASSIGN;
							if (!(relax && isEndOfAttributes()))
								pushContext(CONTEXT_ATTRVALUE);
							break;
						case '"':  case '\'':
							STRING();
							llktype1 = STRING;
							break;
						case '\n':  case '\r':
							NEWLINE();
							llktype1 = NEWLINE;
							break;
						case '\t':  case '\u000c':  case ' ':  case '\u200b':
							SPACES();
							llktype1 = SPACES;
							break;
						case 'A':  case 'B':  case 'C':  case 'D':
						case 'E':  case 'F':  case 'G':  case 'H':
						case 'I':  case 'J':  case 'K':  case 'L':
						case 'M':  case 'N':  case 'O':  case 'P':
						case 'Q':  case 'R':  case 'S':  case 'T':
						case 'U':  case 'V':  case 'W':  case 'X':
						case 'Y':  case 'Z':  case '_':  case 'a':
						case 'b':  case 'c':  case 'd':  case 'e':
						case 'f':  case 'g':  case 'h':  case 'i':
						case 'j':  case 'k':  case 'l':  case 'm':
						case 'n':  case 'o':  case 'p':  case 'q':
						case 'r':  case 's':  case 't':  case 'u':
						case 'v':  case 'w':  case 'x':  case 'y':
						case 'z':
							llktoken1 = NAME();
							break;
						default:
							if ((LA1() == ']') && (LA(2) == ']') && (LA(3) == '>')) {
								llkInput.consume(3);
								llktype1 = RCDATA;
								popContext();
							} else if (LA1() == ']') {
								llkInput.consume();
								llkInput.match('>');
								llktype1 = RCOND;
								popContext();
							} else {
								llktoken1 = checkEOF();
							}
					}
					break;
				case CONTEXT_DTD:
					switch (LA1()) {
						case ']':
							llkInput.match(LLK_STRING_0000);
							llktype1 = RCDATA;
							popContext();
							break;
						case '?':
							llkInput.consume();
							llkInput.match('>');
							llktype1 = RPI;
							popContext();
							break;
						case '<':
							llktoken1 = START_TAG();
							break;
						case '/':
							llkInput.consume();
							llkInput.match('>');
							llktype1 = ENDEMPTY;
							popContext();
							break;
						case '>':
							llkInput.consume();
							llktype1 = GT;
							popContext();
							break;
						case '-':
							llkInput.consume();
							llkInput.match('-');
							if (llkSynPredict0()) {
								if (llkBitTest(LA1(), '\t', '\u000c', ' ', '\u200b')) {
									SPACES();
								}
								llkInput.match('>');
								llktype1 = RCOMMENT;
								popContext();
							} else {
								llktype1 = DTDCOMMENT;
								popContext();
								pushContext(CONTEXT_COMMENT);
							}
							break;
						case '\n':  case '\r':
							NEWLINE();
							llktype1 = NEWLINE;
							break;
						case '\t':  case '\u000c':  case ' ':  case '\u200b':
							SPACES();
							llktype1 = SPACES;
							break;
						case 'A':  case 'B':  case 'C':  case 'D':
						case 'E':  case 'F':  case 'G':  case 'H':
						case 'I':  case 'J':  case 'K':  case 'L':
						case 'M':  case 'N':  case 'O':  case 'P':
						case 'Q':  case 'R':  case 'S':  case 'T':
						case 'U':  case 'V':  case 'W':  case 'X':
						case 'Y':  case 'Z':  case '_':  case 'a':
						case 'b':  case 'c':  case 'd':  case 'e':
						case 'f':  case 'g':  case 'h':  case 'i':
						case 'j':  case 'k':  case 'l':  case 'm':
						case 'n':  case 'o':  case 'p':  case 'q':
						case 'r':  case 's':  case 't':  case 'u':
						case 'v':  case 'w':  case 'x':  case 'y':
						case 'z':
							llktoken1 = NAME();
							String name = llktoken1.getText().toString();
							if ("SYSTEM".equalsIgnoreCase(name)) {
								if (!"SYSTEM".equals(name)) {
									warn(MsgId.LexerSystem, null, llktoken1.getOffset());
								}
								llktoken1.setType(SYSTEM);
								pushContext(CONTEXT_SYSTEM);
							} else if ("PUBLIC".equalsIgnoreCase(name)) {
								if (!"PUBLIC".equals(name)) {
									warn(MsgId.LexerPublic, null, llktoken1.getOffset());
								}
								llktoken1.setType(PUBLIC);
								pushContext(CONTEXT_PUBID);
							}
							break;
						default:
							llktoken1 = checkEOF();
					}
					break;
				case CONTEXT_ATTRVALUE:
					if (llkBitTest(LA1(), '\t', '\u000c', ' ', '\u200b')) {
						SPACES();
						llktype1 = SPACES;
					} else if (LA1() == '\n' || LA1() == '\r') {
						NEWLINE();
						llktype1 = NEWLINE;
					} else if (llkGetBitInverted(LA1(), LLKTokenSet6.bitset)) {
						ATTVALUE();
						llktype1 = ATTVALUE;
						popContext();
					} else {
						llktoken1 = checkEOF();
					}
					break;
				case CONTEXT_PUBID:
					switch (LA1()) {
						case '"':  case '\'':
							PUBID_LITERAL();
							llktype1 = PUBID_LITERAL;
							popContext();
							pushContext(CONTEXT_SYSTEM);
							break;
						case '\n':  case '\r':
							NEWLINE();
							llktype1 = NEWLINE;
							break;
						case '\t':  case '\u000c':  case ' ':  case '\u200b':
							SPACES();
							llktype1 = SPACES;
							break;
						default:
							llktoken1 = checkEOF();
					}
					break;
				case CONTEXT_SYSTEM:
					switch (LA1()) {
						case '>':
							llkInput.consume();
							llktype1 = GT;
							popContext();
							popContext();
							break;
						case '"':  case '\'':
							SYSTEM_LITERAL();
							llktype1 = SYSTEM_LITERAL;
							popContext();
							break;
						case '\n':  case '\r':
							NEWLINE();
							llktype1 = NEWLINE;
							break;
						case '\t':  case '\u000c':  case ' ':  case '\u200b':
							SPACES();
							llktype1 = SPACES;
							break;
						default:
							llktoken1 = checkEOF();
					}
					break;
				case CONTEXT_COMMENT:
					endComment();
					llktype1 = COMMENT;
					popContext();
					break;
				case CONTEXT_PI:
					skip(END_PI);
					llktype1 = PI;
					popContext();
					break;
				case CONTEXT_ASP:
					skip(END_ASP);
					llktype1 = ASP;
					popContext();
					break;
				case CONTEXT_JSTE:
					skip(END_JSTE);
					llktype1 = JSTE;
					popContext();
					break;
				case CONTEXT_CDATA:
					skip(END_CDATA);
					llktype1 = CDATA;
					popContext();
					break;
				case CONTEXT_COND:
					skip(END_COND);
					llktype1 = COND;
					popContext();
					break;
				case CONTEXT_DECLARATION:
					skip(END_DECLARATION);
					llktype1 = DECLARATION;
					popContext();
					break;
				case CONTEXT_SCRIPT:
					endScript(END_SCRIPT, "script");
					llktype1 = SCRIPT;
					popContext();
					break;
				case CONTEXT_STYLE:
					endScript(END_SCRIPT, "style");
					llktype1 = STYLE;
					popContext();
					break;
				default:
					if (LA1() == ILLKConstants.LEXER_EOF) {
							llktoken1 = llkCreateEOF(llkTokenStart);
					} else {
						throw llkParseException("Unexpected token");
					}
			}
			if (llktoken1 != null) 
				return llkTokenHandler.yieldToken(llktoken1);
			return llkTokenHandler.yieldToken(llktype1, llkTokenStart, llkInput.getOffset());
		} catch (Throwable _e) {
			throw llkParseError("Unexpceted character @"+llkInput.getLocation() + ": LA1=" + LA1()
				+ ", contexts=\n\t" + dumpContexts(), _e, llkInput.getLocation());
		}
	}

	public ILLKToken llkCreateEOF(int offset) throws LLKParseException {
		return llkCreateEOF1(offset);
	}

	protected ILLKToken START_TAG() throws LLKParseException {
		ILLKToken t = null;
		ILLKToken ret = null;
		int type1;
		boolean isdtd = llkGetContext(CONTEXT) == CONTEXT_DTD;
		if (isdtd) {
			pushContext(CONTEXT_DTD);
		} else {
			pushContext(CONTEXT_TAG);
		}
		llkInput.match('<');
		type1 = LT;
		switch (LA1()) {
			case '/':
				llkInput.consume();
				type1 = ENDTAG;
				break;
			case '?':
				llkInput.consume();
				if (llkGetBit(LA1(), LLKTokenSet7.bitset)) {
					t = NAME();
				}
				type1 = LPI;
				pushContext(CONTEXT_PI);
				ret = createDataToken(type1, t);
				break;
			case '%':
				llkInput.consume();
				if (llkGetBit(LA1(), LLKTokenSet7.bitset)) {
					t = NAME();
				}
				type1 = LASP;
				pushContext(CONTEXT_ASP);
				ret = createDataToken(type1, t);
				break;
			case '#':
				llkInput.consume();
				if (llkGetBit(LA1(), LLKTokenSet7.bitset)) {
					t = NAME();
				}
				type1 = LJSTE;
				pushContext(CONTEXT_JSTE);
				ret = createDataToken(type1, t);
				break;
			case '!':
				llkInput.consume();
				type1 = LDECL;
				if (llkInput.matchOpt('-')) {
					if (llkInput.matchOpt('-')) {
					} else {
						error(MsgId.LexerMissingDash, null, llkGetOffset());
					}
					type1 = LCOMMENT;
					pushContext(CONTEXT_COMMENT);
				} else if ((LA1() == '[') && (!isdtd)) {
					llkInput.consume();
					if ((LA1() == 'C') && (llkInput.LA(LLK_STRING_0001))) {
						llkInput.match(LLK_STRING_0001);
						type1 = LCDATA;
						pushContext(CONTEXT_CDATA);
					} else {
						type1 = LCOND;
						pushContext(CONTEXT_COND);
					}
				} else {
					if (llkGetBit(LA1(), LLKTokenSet7.bitset)) {
						t = NAME();
						type1 = LDECL;
						ret = createDataToken(type1, t);
						String text = t.getText().toString();
						if (!isdtd) {
							if ("DOCTYPE".equalsIgnoreCase(text)) {
								popContext();
								pushContext(CONTEXT_DTD);
							} else {
								pushContext(CONTEXT_DECLARATION);
							}
						}
					} else {
						if (!isdtd)
							pushContext(CONTEXT_DECLARATION);
					}
				}
				break;
			default:
				if (!isdtd && !isValidTag()) {
					warn(MsgId.LexerEscLT, null, llkGetOffset() - 1);
					type1 = TEXT;
					ret = createDataToken(type1, null);
					popContext();
				}
		}
		if (ret == null)
			ret = createDataToken(type1, null);
		return ret;
	}

	public void skip(int[] endtag) {
		for (int la1 = LA1(); la1 >= 0; la1 = LA1()) {
			NOTMATCH: if (la1 == endtag[0]) {
				int lax;
				for (int i = 1; i < endtag.length; ++i) {
					lax = LA(i + 1);
					if (lax < 0 || lax != endtag[i])
						break NOTMATCH;
				}
				return;
			}
			llkConsume();
			adjustLocation(la1);
		}
	}

	public void endScript(int[] endtag, String name) {
		for (int la1 = LA1(); la1 >= 0; la1 = LA1()) {
			NOTMATCH: if (la1 == endtag[0]) {
				int i = 1;
				int lax;
				for (; i < endtag.length; ++i) {
					lax = LA(i + 1);
					if (lax < 0 || lax != endtag[i])
						break NOTMATCH;
				}
				if (!llkGetBit(LA(i + 1), NameStartChar.bitset))
					break NOTMATCH;
				if (!matchIgnoreCase(i + 1, name)) {
					warn(MsgId.LexerEscEndTag, "element=" + name, null, llkGetOffset());
					break NOTMATCH;
				}
				return;
			}
			llkConsume();
			adjustLocation(la1);
		}
	}

	public void endComment() {
		for (int la1 = LA1(); la1 >= 0; la1 = LA1()) {
			NOTMATCH: if (la1 == '-') {
				if (LA(2) == '-') {
					int i = optionalSpaces(3);
					if (i < 0)
						break NOTMATCH;
					if (LA(i) == '>')
						return;
				}
			}
			llkConsume();
			adjustLocation(la1);
		}
	}

	public void SPACES() throws LLKParseException {
		_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
			if (llkBitTest(LA1(), '\u000c', ' ', '\u200b')) {
				llkInput.consume();
			} else if (llkInput.matchOpt('\t')) {
				llkInput.tab();
			} else {
				if (!_cnt1) {
					throw llkParseException("()+ expected at least one occuence");
				}
				break _loop1;
			}
		}
	}

	public void NEWLINE() throws LLKParseException {
		if (llkInput.matchOpt('\r')) {
			llkInput.matchOpt('\n');
			llkInput.newline();
		} else if (llkInput.matchOpt('\n')) {
			llkInput.newline();
		} else {
			throw llkParseException("Unexpected token");
		}
	}

	public ILLKToken NAME() throws LLKParseException {
		int start = llkGetOffset();
		if (llkGetBit(LA1(), NameStartChar.bitset)) {
			llkInput.consume();
		} else if (llkInput.matchOpt('_')) {
			error(MsgId.Lexer_NotValid, null, llkGetOffset());
		} else {
			throw llkParseException("Unexpected token");
		}
		_loop1: while (true) {
			if (llkGetBit(LA1(), NameChar.bitset)) {
				llkInput.consume();
			} else {
				break _loop1;
			}
		}
		int end = llkGetOffset();
		return llkTokenHandler.createTextToken(NAME, start, end, namePool.intern(llkInput, start, end));
	}

	public void ATTVALUE() throws LLKParseException {
		if (LA1() == '"' || LA1() == '\'') {
			STRING();
		} else if ((llkGetBitInverted(LA1(), LLKTokenSet8.bitset)) && (relax)) {
			llkInput.consume();
			_loop1: while (true) {
				if (llkGetBitInverted(LA1(), LLKTokenSet6.bitset)) {
					llkInput.consume();
				} else if (llkSynPredict1()) {
					_loop2: for (boolean _cnt2 = false;; _cnt2 = true) {
						if (llkGetBit(LA1(), Whitespaces.bitset)) {
							llkInput.consume();
						} else {
							if (!_cnt2) {
								throw llkParseException("()+ expected at least one occuence");
							}
							break _loop2;
						}
					}
					_loop3: for (boolean _cnt3 = false;; _cnt3 = true) {
						if (llkGetBitInverted(LA1(), LLKTokenSet9.bitset)) {
							llkInput.consume();
						} else {
							if (!_cnt3) {
								throw llkParseException("()+ expected at least one occuence");
							}
							break _loop3;
						}
					}
				} else {
					break _loop1;
				}
			}
		} else {
			if (llkGetBit(LA1(), LLKTokenSet10.bitset)) {
				ATTVALUE_LITERAL();
				_loop4: while (true) {
					if (llkGetBitInverted(LA1(), LLKTokenSet6.bitset)) {
						llkInput.consume();
					} else {
						break _loop4;
					}
				}
			} else {
				throw llkParseException("Unexpected token");
			}
		}
	}

	public void STRING() throws LLKParseException {
		if (llkInput.matchOpt('"')) {
			_loop1: while (true) {
				if (llkBitTest(LA1(), '\t', '\n', '\r', '&')) {
					ATTVALUE1();
				} else if (llkGetBitInverted(LA1(), LLKTokenSet11.bitset)) {
					llkInput.consume();
				} else {
					break _loop1;
				}
			}
			llkInput.match('"');
		} else if (llkInput.matchOpt('\'')) {
			_loop2: while (true) {
				if (llkBitTest(LA1(), '\t', '\n', '\r', '&')) {
					ATTVALUE1();
				} else if (llkGetBitInverted(LA1(), LLKTokenSet12.bitset)) {
					llkInput.consume();
				} else {
					break _loop2;
				}
			}
			llkInput.match('\'');
		} else {
			throw llkParseException("Unexpected token");
		}
	}

	protected void ATTVALUE_LITERAL() throws LLKParseException {
		int offset;
		_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
			if (llkGetBit(LA1(), LLKTokenSet13.bitset)) {
				llkInput.consume();
			} else if (llkGetBit(LA1(), LLKTokenSet14.bitset)) {
				llkInput.consume();
				offset = llkGetOffset() - 1;
				warn(MsgId.LexerQuoteChar, llkGetSource(offset, offset + 1).toString(), null, offset);
			} else {
				if (!_cnt1) {
					throw llkParseException("()+ expected at least one occuence");
				}
				break _loop1;
			}
		}
	}

	public void SYSTEM_LITERAL() throws LLKParseException {
		if (llkInput.matchOpt('"')) {
			_loop1: while (true) {
				if (llkInvertedBitTest(LA1(), 65536, '"')) {
					llkInput.consume();
				} else {
					break _loop1;
				}
			}
			llkInput.match('"');
		} else if (llkInput.matchOpt('\'')) {
			_loop2: while (true) {
				if (llkInvertedBitTest(LA1(), 65536, '\'')) {
					llkInput.consume();
				} else {
					break _loop2;
				}
			}
			llkInput.match('\'');
		} else {
			throw llkParseException("Unexpected token");
		}
	}

	public void PUBID_LITERAL() throws LLKParseException {
		if (llkInput.matchOpt('"')) {
			_loop1: while (true) {
				if (llkGetBit(LA1(), PubChar.bitset)) {
					llkInput.consume();
				} else {
					break _loop1;
				}
			}
			llkInput.match('"');
		} else if (llkInput.matchOpt('\'')) {
			_loop2: while (true) {
				if (llkGetBit(LA1(), LLKTokenSet15.bitset)) {
					llkInput.consume();
				} else {
					break _loop2;
				}
			}
			llkInput.match('\'');
		} else {
			throw llkParseException("Unexpected token");
		}
	}

	protected void ATTVALUE1() throws LLKParseException {
		switch (LA1()) {
			case '\t':
				llkInput.consume();
				llkInput.tab();
				break;
			case '\r':
				llkInput.consume();
				llkInput.matchOpt('\n');
				llkInput.newline();
				break;
			case '\n':
				llkInput.consume();
				llkInput.newline();
				break;
			case '&':
				REFERENCE();
				break;
			default:
				throw llkParseException("Unexpected token");
		}
	}

	protected void REFERENCE() throws LLKParseException {
		ILLKToken name = null;
		int start = llkGetOffset();
		llkInput.match('&');
		if (llkInput.matchOpt('#')) {
			start = llkGetOffset();
			if (LA1() >= '0' && LA1() <= '9') {
				_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
					if (LA1() >= '0' && LA1() <= '9') {
						llkInput.consume();
					} else {
						if (!_cnt1) {
							throw llkParseException("()+ expected at least one occuence");
						}
						break _loop1;
					}
				}
			} else if (llkInput.matchOpt('x')) {
				llkInput.match(LLKTokenSet16.bitset);
			} else {
				throw llkParseException("Unexpected token");
			}
			if (llkInput.matchOpt(';')) {
				name = llkTokenHandler.createToken(REFERENCE, start, llkGetOffset() - 1);
			} else {
				start -= 2;
			}
		} else if (llkSynPredict2()) {
			name = NAME();
			llkInput.match(';');
		}
		if (name == null) {
			if (!relax)
				warn(MsgId.LexerEscAmp, null, start);
		} else if (lexHandler != null)
			lexHandler.reference(name);
	}

	////////////////////////////////////////////////////////////

	static class LLKTokenSet {
		public boolean inverted;
		public int start;
		public int end;
		public int[] bitset;
		public LLKTokenSet(boolean inverted, int start, int end, int[] bitset) {
			this.inverted=inverted;
			this.start=start;
			this.end=end;
			this.bitset=bitset;
		}
	}
	static final class Whitespaces {
		static final boolean inverted=false;
		static final int start = 0;
		static final int end = 257;
		static final int[] bitset = new int[] {
			0x00003600, 0x00000001, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000800, 
		};
	}
	static final LLKTokenSet NameChar = new LLKTokenSet(
		false,
		0,
		4,
		new int[] {
			0x00000000, 0x07ff6000, 0x87fffffe, 0x07fffffe, 
		}
	);
	static final LLKTokenSet NameStartChar = new LLKTokenSet(
		false,
		0,
		4,
		new int[] {
			0x00000000, 0x00000000, 0x07fffffe, 0x07fffffe, 
		}
	);
	static final LLKTokenSet PubChar = new LLKTokenSet(
		false,
		0,
		4,
		new int[] {
			0x00002400, 0xafffffbb, 0x87ffffff, 0x07fffffe, 
		}
	);
	static final LLKTokenSet LLKTokenSet4 = new LLKTokenSet(
		true,
		0,
		2,
		new int[] {
			0x00002600, 0x10000040, 
		}
	);
	static final class LLKTokenSet5 {
		static final boolean inverted=false;
		static final int start = 0;
		static final int end = 257;
		static final int[] bitset = new int[] {
			0x00001200, 0x40000001, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000800, 
		};
	}
	static final class LLKTokenSet6 {
		static final boolean inverted=true;
		static final int start = 0;
		static final int end = 257;
		static final int[] bitset = new int[] {
			0x00003600, 0x50000001, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000800, 
		};
	}
	static final LLKTokenSet LLKTokenSet7 = new LLKTokenSet(
		false,
		0,
		4,
		new int[] {
			0x00000000, 0x00000000, 0x87fffffe, 0x07fffffe, 
		}
	);
	static final class LLKTokenSet8 {
		static final boolean inverted=true;
		static final int start = 0;
		static final int end = 257;
		static final int[] bitset = new int[] {
			0x00003600, 0x50000085, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000000, 0x00000000, 0x00000000, 0x00000000, 
			0x00000800, 
		};
	}
	static final LLKTokenSet LLKTokenSet9 = new LLKTokenSet(
		true,
		0,
		4,
		new int[] {
			0x00000000, 0x50002000, 0x87fffffe, 0x07fffffe, 
		}
	);
	static final LLKTokenSet LLKTokenSet10 = new LLKTokenSet(
		false,
		0,
		4,
		new int[] {
			0x00000000, 0x8fffef3a, 0xffffffff, 0x3ffffffe, 
		}
	);
	static final LLKTokenSet LLKTokenSet11 = new LLKTokenSet(
		true,
		0,
		2,
		new int[] {
			0x00002600, 0x00000044, 
		}
	);
	static final LLKTokenSet LLKTokenSet12 = new LLKTokenSet(
		true,
		0,
		2,
		new int[] {
			0x00002600, 0x000000c0, 
		}
	);
	static final LLKTokenSet LLKTokenSet13 = new LLKTokenSet(
		false,
		0,
		4,
		new int[] {
			0x00000000, 0x07ff6000, 0x07fffffe, 0x07fffffe, 
		}
	);
	static final LLKTokenSet LLKTokenSet14 = new LLKTokenSet(
		false,
		0,
		4,
		new int[] {
			0x00000000, 0x88008f3a, 0xf8000001, 0x38000000, 
		}
	);
	static final LLKTokenSet LLKTokenSet15 = new LLKTokenSet(
		false,
		0,
		4,
		new int[] {
			0x00002400, 0xafffff3b, 0x87ffffff, 0x07fffffe, 
		}
	);
	static final LLKTokenSet LLKTokenSet16 = new LLKTokenSet(
		false,
		0,
		4,
		new int[] {
			0x00000000, 0x03ff0000, 0x0000007e, 0x0000007e, 
		}
	);

	public final String llkGetTokenName(int type) {
		return LLKTOKENS.nameOf(type);
	}
	
	protected final boolean llkGetBitInverted(int n, int[] bset) {
		int mask = (n & ILLKConstants.MODMASK);
		return n >= 0
			&& n < LLK_INPUT_VOCAB_SIZE
			&& ((n >>= ILLKConstants.LOGBITS) >= bset.length || (bset[n] & (1 << mask)) == 0);
	}
	
	public int llkGetContext(int context) {
		return llkContexts[context];
	}
	
	public void llkSetContext(int context, int state) {
		llkContexts[context] = state;
	}
	
	public void llkSetContext(int context, int state, ILLKToken lt0) {
		llkContexts[context] = state;
		llkRewind(lt0);
	}
	
	////////////////////////////////////////////////////////////

	public final boolean llkIsKeyword(CharSequence src) {
		return (llkLookupKeyword(src, -1) >= 0);
	}

	public final int llkLookupKeyword(CharSequence src, int def) {
		int start = 0;switch (src.length()) {
			case 5:
				if (src.charAt(start) != 's' && src.charAt(start) != 'S'
				|| src.charAt(++start) != 't' && src.charAt(start) != 'T'
				|| src.charAt(++start) != 'y' && src.charAt(start) != 'Y'
				|| src.charAt(++start) != 'l' && src.charAt(start) != 'L'
				|| src.charAt(++start) != 'e' && src.charAt(start) != 'E'
				) return def;
				return LSTYLE;
			case 6:
				if (!llkKeywordMatch(src, start, LLK_STRING_script, 0, 6))
					return def;
				return LSCRIPT;
			default:
				return def;
		}
	}

	public final boolean llkKeywordMatch(CharSequence src, int index, char[] keyword, int start, int end) {
		while (start < end) {
			if (src.charAt(index++) != keyword[start++])
				return false;
		}
		return true;
	}

	public int llkGetKeywordContexts() {
		throw new UnsupportedOperationException("There are no keyword context.");
	}

	public void llkSetKeywordContexts(int context, ILLKToken lt0) {
		throw new UnsupportedOperationException("There are no keyword context.");
	}

	private final boolean llkSynPredict0() {
		boolean llkRet = false;
		llkInput.mark();
		_rule: {
			if (llkBitTest(LA1(), '\t', '\u000c', ' ', '\u200b')) {
				if (!llkSyn_SPACES())
					break _rule;
			}
			if (!llkSynMatch('>'))
				break _rule;
			llkRet = true;
		}
		llkInput.rewind();
		return llkRet;
	}

	private final boolean llkSyn_SPACES() {
		_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
			if (llkBitTest(LA1(), '\u000c', ' ', '\u200b')) {
				llkInput.consume();
			} else if (llkInput.matchOpt('\t')) {
			} else {
				if (!_cnt1) {
					return false;
				}
				break _loop1;
			}
		}
		return true;
	}

	private final boolean llkSynPredict1() {
		boolean llkRet = false;
		llkInput.mark();
		_rule: {
			if (!llkSyn_laSpaceNotName())
				break _rule;
			llkRet = true;
		}
		llkInput.rewind();
		return llkRet;
	}

	private final boolean llkSyn_laSpaceNotName() {
		_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
			if (llkGetBit(LA1(), Whitespaces.bitset)) {
				llkInput.consume();
			} else {
				if (!_cnt1) {
					return false;
				}
				break _loop1;
			}
		}
		if (!llkSynMatchNot(LLKTokenSet9.bitset))
			return false;
		return true;
	}

	private final boolean llkSynPredict2() {
		boolean llkRet = false;
		llkInput.mark();
		_rule: {
			if (!llkSyn_NAME())
				break _rule;
			if (!llkSynMatch(';'))
				break _rule;
			llkRet = true;
		}
		llkInput.rewind();
		return llkRet;
	}

	private final boolean llkSyn_NAME() {
		if (llkGetBit(LA1(), NameStartChar.bitset)) {
			llkInput.consume();
		} else if (llkInput.matchOpt('_')) {
		} else {
			return false;
		}
		_loop1: while (true) {
			if (llkGetBit(LA1(), NameChar.bitset)) {
				llkInput.consume();
			} else {
				break _loop1;
			}
		}
		return true;
	}

	protected String dumpContexts() {
		StringBuilder b = new StringBuilder();
		for (int c : llkContexts) {
			if (b.length() > 0)
			b.append(' ');
			b.append(c);
		}
		return b.toString();
	}

	////////////////////////////////////////////////////////////
}
