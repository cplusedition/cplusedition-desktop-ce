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

import java.util.HashMap;
import java.util.Map;

import sf.llk.share.support.DefaultTokenHandler;
import sf.llk.share.support.ILLKConstants;
import sf.llk.share.support.ILLKLexer;
import sf.llk.share.support.ILLKLexerInput;
import sf.llk.share.support.ILLKLifeCycleListener;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.support.ILLKTokenHandler;
import sf.llk.share.support.LLKLexerBase;
import sf.llk.share.support.LLKParseException;

public class CSSLexer extends LLKLexerBase implements ILLKLexer, ILLKCSSLexer {

	public CSSLexer(ILLKLexerInput input) {
	    this(input, DefaultTokenHandler.getInstance(
            input,
            LLK_SPECIAL_TOKENS,
            LLK_TOKEN_USE_TEXT,
            LLK_TOKEN_USE_INTERN));
    }

	public CSSLexer(ILLKLexerInput input, ILLKTokenHandler tokenhandler) {
		llkInput = input;
		llkMain = input.getMain();
		llkLocator = input.getLocator();
        llkInput.setIgnoreCase(LLK_IGNORE_CASE);
		llkTokenHandler = tokenhandler;
		llkInit();
	}

	////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////////////////////////////////

	protected void llkInit() {
	}

	private ILLKToken createToken(int type) {
		return llkTokenHandler.createToken(type, llkTokenStart, llkGetOffset());
	}

    public static int[] specialTokens() {
        return (int[])LLK_SPECIAL_TOKENS.clone();
    }

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private void warn(MsgId id, Throwable e, int offset) {
		llkMain.warn(id.getMessage(), e, offset);
	}

	//////////////////////////////////////////////////////////////////////

	public static final boolean LLK_HAS_SPECIAL_TOKEN = false;
	public static final boolean LLK_TOKEN_USE_TEXT = true;
	public static final boolean LLK_TOKEN_USE_INTERN = false;
	public static final boolean LLK_IGNORE_CASE = true;
	public static final int LLK_INPUT_VOCAB_SIZE = 0x10000;
	public static class Keyword {
		protected int type;
		public Keyword(int type) {
			this.type=type;
		}
	}
	static final char[] LLK_STRING_0000 = new char[] {
		'<', '!', '-', '-'
	};
	static final char[] LLK_STRING_0001 = new char[] {
		'/', '*'
	};
	static final char[] LLK_STRING_0002 = new char[] {
		'*', '/'
	};
	static final char[] LLK_STRING_0003 = new char[] {
		'U', '+'
	};
	static final int[] LLK_SPECIAL_TOKENS = new int[] {
		0x00000002, 
	};
	static final int[] LLK_LITERAL_TOKENS = new int[] {
		0xfffffff0, 0x003fffff, 
	};
	static final int[] LLK_KEYWORD_TOKENS = new int[] {
		0x1ffffff0, 
	};

	protected  final Map<String, Keyword> llkKeywordTable = new HashMap<String, Keyword>(50);
	{
		llkKeywordTable.put("left", new Keyword(26));
		llkKeywordTable.put("right", new Keyword(26));
		llkKeywordTable.put("top", new Keyword(26));
		llkKeywordTable.put("bottom", new Keyword(26));
		llkKeywordTable.put("rad", new Keyword(12));
		llkKeywordTable.put("deg", new Keyword(12));
		llkKeywordTable.put("grad", new Keyword(12));
		llkKeywordTable.put("turn", new Keyword(12));
		llkKeywordTable.put("dpi", new Keyword(15));
		llkKeywordTable.put("dpcm", new Keyword(15));
		llkKeywordTable.put("dppx", new Keyword(15));
		llkKeywordTable.put("@page", new Keyword(5));
		llkKeywordTable.put("em", new Keyword(11));
		llkKeywordTable.put("ex", new Keyword(11));
		llkKeywordTable.put("ch", new Keyword(11));
		llkKeywordTable.put("rem", new Keyword(11));
		llkKeywordTable.put("vw", new Keyword(11));
		llkKeywordTable.put("vh", new Keyword(11));
		llkKeywordTable.put("vmax", new Keyword(11));
		llkKeywordTable.put("vmin", new Keyword(11));
		llkKeywordTable.put("cm", new Keyword(11));
		llkKeywordTable.put("mm", new Keyword(11));
		llkKeywordTable.put("in", new Keyword(11));
		llkKeywordTable.put("pt", new Keyword(11));
		llkKeywordTable.put("pc", new Keyword(11));
		llkKeywordTable.put("px", new Keyword(11));
		llkKeywordTable.put("@media", new Keyword(6));
		llkKeywordTable.put("and", new Keyword(20));
		llkKeywordTable.put("important", new Keyword(16));
		llkKeywordTable.put("not", new Keyword(19));
		llkKeywordTable.put("@charset", new Keyword(8));
		llkKeywordTable.put("from", new Keyword(22));
		llkKeywordTable.put("@-webkit-keyframes", new Keyword(10));
		llkKeywordTable.put("auto", new Keyword(17));
		llkKeywordTable.put("inherit", new Keyword(17));
		llkKeywordTable.put("initial", new Keyword(17));
		llkKeywordTable.put("unset", new Keyword(17));
		llkKeywordTable.put("inset", new Keyword(17));
		llkKeywordTable.put("outset", new Keyword(17));
		llkKeywordTable.put("center", new Keyword(25));
		llkKeywordTable.put("@import", new Keyword(4));
		llkKeywordTable.put("@font-face", new Keyword(7));
		llkKeywordTable.put("of", new Keyword(21));
		llkKeywordTable.put("at", new Keyword(24));
		llkKeywordTable.put("hz", new Keyword(14));
		llkKeywordTable.put("khz", new Keyword(14));
		llkKeywordTable.put("@keyframes", new Keyword(9));
		llkKeywordTable.put("to", new Keyword(23));
		llkKeywordTable.put("closest-corner", new Keyword(28));
		llkKeywordTable.put("closest-side", new Keyword(28));
		llkKeywordTable.put("farthest-corner", new Keyword(28));
		llkKeywordTable.put("farthest-side", new Keyword(28));
		llkKeywordTable.put("ms", new Keyword(13));
		llkKeywordTable.put("s", new Keyword(13));
		llkKeywordTable.put("only", new Keyword(18));
		llkKeywordTable.put("circle", new Keyword(27));
		llkKeywordTable.put("ellipse", new Keyword(27));
	}

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
			switch (LA1()) {
				case '-':
					llktoken1 = dash();
					break;
				case '@':
					llktoken1 = AT_KEYWORD();
					break;
				case '#':
					llktype1 = HASH;
					HASH();
					break;
				case 'U':
					llktype1 = UNICODE_RANGE;
					UNICODE_RANGE();
					break;
				case '%':
					llkInput.consume();
					llktype1 = PERCENT;
					break;
				case '<':
					if (LA(2) == '!') {
						llkInput.match(LLK_STRING_0000, 2);
						llktype1 = CDO;
					} else {
						llkInput.consume();
						llktype1 = LT;
					}
					break;
				case '~':
					if (LA(2) == '=') {
						llkInput.consume(2);
						llktype1 = WORDMATCH;
					} else {
						llkInput.consume();
						llktype1 = TILDE;
					}
					break;
				case '|':
					if (LA(2) == '=') {
						llkInput.consume(2);
						llktype1 = LANGMATCH;
					} else if (LA(2) == '|') {
						llkInput.consume(2);
						llktype1 = COLUMN;
					} else {
						llkInput.consume();
						llktype1 = OR;
					}
					break;
				case '^':
					llkInput.consume();
					llkInput.match('=');
					llktype1 = HEADMATCH;
					break;
				case '$':
					llkInput.consume();
					llkInput.match('=');
					llktype1 = TAILMATCH;
					break;
				case '*':
					if (LA(2) == '=') {
						llkInput.consume(2);
						llktype1 = SUBSTRMATCH;
					} else {
						llkInput.consume();
						llktype1 = STAR;
					}
					break;
				case '!':
					llkInput.consume();
					llktype1 = EXCLAIMATION;
					break;
				case ',':
					llkInput.consume();
					llktype1 = COMMA;
					break;
				case ':':
					llkInput.consume();
					llktype1 = COLON;
					break;
				case ';':
					llkInput.consume();
					llktype1 = SEMICOLON;
					break;
				case '+':
					llkInput.consume();
					llktype1 = PLUS;
					break;
				case '>':
					llkInput.consume();
					llktype1 = GT;
					break;
				case '(':
					llkInput.consume();
					llktype1 = LPAREN;
					break;
				case ')':
					llkInput.consume();
					llktype1 = RPAREN;
					break;
				case '{':
					llkInput.consume();
					llktype1 = LBRACE;
					break;
				case '}':
					llkInput.consume();
					llktype1 = RBRACE;
					break;
				case '[':
					llkInput.consume();
					llktype1 = LBRACKET;
					break;
				case ']':
					llkInput.consume();
					llktype1 = RBRACKET;
					break;
				case '=':
					llkInput.consume();
					llktype1 = EQUAL;
					break;
				case '"':  case '\'':
					llktype1 = STRING;
					STRING();
					break;
				case '.':  case '0':  case '1':  case '2':
				case '3':  case '4':  case '5':  case '6':
				case '7':  case '8':  case '9':
					llktype1 = NUMBER();
					break;
				default:
					if (llkGetBitInverted(LA1(), LLKTokenSet0.bitset)) {
						llktoken1 = S();
					} else {
						if (LA1() == ILLKConstants.LEXER_EOF) {
								llktoken1 = llkCreateEOF(llkTokenStart);
						} else {
							throw llkParseException("Unexpected token");
						}
					}
			}
			if (llktoken1 != null) 
				return llkTokenHandler.yieldToken(llktoken1);
			return llkTokenHandler.yieldToken(llktype1, llkTokenStart, llkInput.getOffset());
		} catch (Throwable _e) {
			throw llkParseError("Unexpceted character @"+llkInput.getLocation() + ": LA1=" + LA1(), _e, llkInput.getLocation());
		}
	}

	public ILLKToken llkCreateEOF(int offset) throws LLKParseException {
		return llkCreateEOF1(offset);
	}

	public ILLKToken S() throws LLKParseException {
		ILLKToken ret = null;
		int type = S;
		switch (LA1()) {
			case '/':
				if (LA(2) == '*') {
					type = Comment();
				} else {
					llkInput.consume();
					type = SLASH;
				}
				break;
			case '\t':  case ' ':
				type = Space();
				break;
			case '\n':  case '\u000c':  case '\r':
				type = Newline();
				break;
			default:
				if ((LA1() == '\\') && (LA(2) == '\n')) {
					llkInput.consume(2);
					type = NEWLINE;
					llkInput.newline();
				} else if (llkGetBitInverted(LA1(), LLKTokenSet1.bitset)) {
					ret = Identifier(llkGetOffset());
				} else {
					throw llkParseException("Unexpected token");
				}
		}
		if (ret == null) ret = createToken(type);
		return ret;
	}

	public ILLKToken dash() throws LLKParseException {
		ILLKToken ret = null;
		int type = DASH;
		int start = llkGetOffset();
		llkInput.match('-');
		if (llkInput.matchOpt('-')) {
			if (llkInput.matchOpt('>')) {
				type = CDC;
			} else {
				type = DASHDASH;
			}
		} else if (llkSynPredict0()) {
			ret = Identifier(start);
		}
		if (ret == null) ret = createToken(type);
		return ret;
	}

	private int Space() throws LLKParseException {
		_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
			if (LA1() == ' ') {
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
		return SPACES;
	}

	private int Newline() throws LLKParseException {
		if (llkInput.matchOpt('\r')) {
			llkInput.matchOpt('\n');
		} else if (LA1() == '\n' || LA1() == '\u000c') {
			llkInput.consume();
		} else {
			throw llkParseException("Unexpected token");
		}
		llkInput.newline();
		return NEWLINE;
	}

	protected int Comment() throws LLKParseException {
		llkInput.match(LLK_STRING_0001);
		_loop1: while (true) {
			if ((LA1() == '*') && (LA(2) != '/')) {
				llkInput.consume();
			} else {
				if (LA1() == '\n' || LA1() == '\u000c') {
					llkInput.consume();
					llkInput.newline();
				} else if (llkInput.matchOpt('\r')) {
					llkInput.matchOpt('\n');
					llkInput.newline();
				} else if (llkInvertedBitTest(LA1(), 65536, '\n', '\u000c', '\r', '*')) {
					llkInput.consume();
				} else {
					break _loop1;
				}
			}
		}
		llkInput.match(LLK_STRING_0002);
		return COMMENT;
	}

	public int NUMBER() throws LLKParseException {
		int type = NUMBER;
		switch (LA1()) {
			case '.':
				type = Dot();
				break;
			case '0':
				if (LA(2) == 'x') {
					Hex();
					type = HEX;
				} else {
					_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
						if (LA1() >= '0' && LA1() <= '9') {
							Digit();
						} else {
							if (!_cnt1) {
								throw llkParseException("()+ expected at least one occuence");
							}
							break _loop1;
						}
					}
					type = DECIMAL;
					if (llkInput.matchOpt('.')) {
						_loop2: for (boolean _cnt2 = false;; _cnt2 = true) {
							if (LA1() >= '0' && LA1() <= '9') {
								Digit();
							} else {
								if (!_cnt2) {
									throw llkParseException("()+ expected at least one occuence");
								}
								break _loop2;
							}
						}
						type = REAL;
					}
				}
				break;
			case '1':  case '2':  case '3':  case '4':
			case '5':  case '6':  case '7':  case '8':
			case '9':
				_loop3: for (boolean _cnt3 = false;; _cnt3 = true) {
					if (LA1() >= '0' && LA1() <= '9') {
						Digit();
					} else {
						if (!_cnt3) {
							throw llkParseException("()+ expected at least one occuence");
						}
						break _loop3;
					}
				}
				type = DECIMAL;
				if (llkInput.matchOpt('.')) {
					_loop4: for (boolean _cnt4 = false;; _cnt4 = true) {
						if (LA1() >= '0' && LA1() <= '9') {
							Digit();
						} else {
							if (!_cnt4) {
								throw llkParseException("()+ expected at least one occuence");
							}
							break _loop4;
						}
					}
					type = REAL;
				}
				break;
			default:
				throw llkParseException("Unexpected token");
		}
		return type;
	}

	protected int Dot() throws LLKParseException {
		int type = DOT;
		llkInput.match('.');
		if (LA1() >= '0' && LA1() <= '9') {
			_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
				if (LA1() >= '0' && LA1() <= '9') {
					Digit();
				} else {
					if (!_cnt1) {
						throw llkParseException("()+ expected at least one occuence");
					}
					break _loop1;
				}
			}
			type = REAL;
		}
		return type;
	}

	protected int Hex() throws LLKParseException {
		int ret = 0;
		int d;
		llkInput.match('0');
		llkInput.match('x');
		_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
			if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
				d = HexDigit();
				ret = (ret << 4) | d;
			} else {
				if (!_cnt1) {
					throw llkParseException("()+ expected at least one occuence");
				}
				break _loop1;
			}
		}
		return ret;
	}

	protected int Digit() throws LLKParseException {
		llkInput.matchRange('0', '9');
		return LT0() - '0';
	}

	protected int HexDigit() throws LLKParseException {
		int ret = 0;
		if (LA1() >= 'a' && LA1() <= 'f') {
			llkInput.consume();
			ret = LT0() - 'a' + 10;
		} else if (LA1() >= '0' && LA1() <= '9') {
			ret = Digit();
		} else {
			throw llkParseException("Unexpected token");
		}
		return ret;
	}

	public void STRING() throws LLKParseException {
		if (llkInput.matchOpt('"')) {
			_loop1: while (true) {
				if (LA1() == '\\') {
					Escape();
				} else if (llkGetBitInverted(LA1(), LLKTokenSet5.bitset)) {
					llkInput.consume();
				} else {
					break _loop1;
				}
			}
			llkInput.match('"');
		} else if (llkInput.matchOpt('\'')) {
			_loop2: while (true) {
				if (LA1() == '\\') {
					Escape();
				} else if (llkGetBitInverted(LA1(), LLKTokenSet6.bitset)) {
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

	protected void Escape() throws LLKParseException {
		if ((LA1() == '\\') && (llkGetBit(LA(2), LLKTokenSet2.bitset))) {
			Unicode();
		} else if ((LA1() == '\\') && (llkGetBitInverted(LA(2), LLKTokenSet3.bitset))) {
			llkInput.consume();
			llkInput.consume();
		} else {
			throw llkParseException("Unexpected token");
		}
	}

	protected void Unicode() throws LLKParseException {
		llkInput.match('\\');
		_loop1: for (int _cnt1 = 0; _cnt1 < 6; ++_cnt1) {
			if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
				HexDigit();
			} else {
				if (_cnt1 < 1) {
					throw llkParseException("(Choices):min,max expected {1,6} occurence: actual=" + _cnt1);
				}
				break _loop1;
			}
		}
	}

	protected ILLKToken Identifier(int start) throws LLKParseException {
		int end;
		int type;
		String text;
		IdentifierStart();
		_loop1: while (true) {
			if (llkGetBitInverted(LA1(), LLKTokenSet7.bitset)) {
				IdentifierPart();
			} else {
				break _loop1;
			}
		}
		if (llkInput.matchOpt('(')) {
			end = llkGetOffset();
			text = llkGetSource(start, end).toString().toLowerCase();
			if ("rgb(".equals(text)) {
				type = RGB;
			} else if ("rgba(".equals(text)) {
				type = RGBA;
			} else if ("linear-gradient(".equals(text)) {
				type = LINEAR_GRADIENT;
			} else if ("radial-gradient(".equals(text)) {
				type = RADIAL_GRADIENT;
			} else if ("repeating-linear-gradient(".equals(text)) {
				type = REPEATING_LINEAR_GRADIENT;
			} else if ("repeating-radial-gradient(".equals(text)) {
				type = REPEATING_RADIAL_GRADIENT;
			} else if ("var(".equals(text)) {
				type = VAR;
			} else if ("url(".equals(text)) {
				type = URI;
			} else if ("calc(".equals(text)) {
				type = CALC;
			} else if ("attr(".equals(text)) {
				type = ATTR;
			} else {
				type = FUNCTION;
			}
			if ((llkGetBitInverted(LA1(), LLKTokenSet8.bitset)) && (type == URI)) {
				uri();
				end = llkGetOffset();
				text = llkGetSource(start, end).toString();
			}
		} else {
			end = llkGetOffset();
			text = llkGetSource(start, end).toString();
			type = llkLookupKeyword(text.toLowerCase(), IDENTIFIER);
		}
		return llkTokenHandler.createToken(type, start, end, text, null);
	}

	public ILLKToken AT_KEYWORD() throws LLKParseException {
		int start = llkGetOffset();
		int end;
		String text;
		llkInput.match('@');
		llkInput.matchOpt('-');
		Identifier(llkGetOffset());
		end = llkGetOffset();
		text = llkGetSource(start, end).toString();
		int type = llkLookupKeyword(text.toLowerCase(), AT_KEYWORD);
		return llkTokenHandler.createToken(type, start, end, text, null);
	}

	private void IdentifierStart() throws LLKParseException {
		if (LA1() == '\\') {
			Escape();
		} else if (llkInput.matchOpt('_')) {
			warn(MsgId.LexerInvalidUnderscore, null, llkGetOffset() - 1);
		} else if (llkGetBitInverted(LA1(), LLKTokenSet9.bitset)) {
			llkInput.consume();
		} else {
			throw llkParseException("Unexpected token");
		}
	}

	private void IdentifierPart() throws LLKParseException {
		if (LA1() == '\\') {
			Escape();
		} else if (llkInput.matchOpt('_')) {
			warn(MsgId.LexerInvalidUnderscore, null, llkGetOffset() - 1);
		} else if (llkGetBitInverted(LA1(), LLKTokenSet10.bitset)) {
			llkInput.consume();
		} else {
			throw llkParseException("Unexpected token");
		}
	}

	public void HASH() throws LLKParseException {
		llkInput.match('#');
		Name();
	}

	private void Name() throws LLKParseException {
		_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
			if (llkGetBitInverted(LA1(), LLKTokenSet7.bitset)) {
				IdentifierPart();
			} else {
				if (!_cnt1) {
					throw llkParseException("()+ expected at least one occuence");
				}
				break _loop1;
			}
		}
	}

	public void UNICODE_RANGE() throws LLKParseException {
		llkInput.match(LLK_STRING_0003);
		if (llkSynPredict1()) {
			_loop1: for (int _cnt1 = 0; _cnt1 < 6; ++_cnt1) {
				if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
					HexDigit();
				} else {
					if (_cnt1 < 1) {
						throw llkParseException("(Choices):min,max expected {1,6} occurence: actual=" + _cnt1);
					}
					break _loop1;
				}
			}
			llkInput.match('-');
			_loop2: for (int _cnt2 = 0; _cnt2 < 6; ++_cnt2) {
				if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
					HexDigit();
				} else {
					if (_cnt2 < 1) {
						throw llkParseException("(Choices):min,max expected {1,6} occurence: actual=" + _cnt2);
					}
					break _loop2;
				}
			}
		} else if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
			Range();
		} else {
			throw llkParseException("Unexpected token");
		}
	}

	private void uri() throws LLKParseException {
		if ((llkGetBit(LA1(), LLKTokenSet11.bitset)) && (LA1() != '\\' || LA1() == '\\' && LA(2) == '\n')) {
			URI_S();
		}
		if (LA1() == '"' || LA1() == '\'') {
			STRING();
		} else if (llkGetBitInverted(LA1(), LLKTokenSet12.bitset)) {
			URL_PART();
		} else {
			throw llkParseException("Unexpected token");
		}
		if (llkGetBit(LA1(), LLKTokenSet11.bitset)) {
			URI_S();
		}
		llkInput.match(')');
	}

	private void URI_S() throws LLKParseException {
		_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
			switch (LA1()) {
				case ' ':
					llkInput.consume();
					break;
				case '\t':
					llkInput.consume();
					llkInput.tab();
					break;
				case '\n':  case '\u000c':  case '\r':
					Newline();
					break;
				default:
					if ((LA1() == '\\') && (LA(2) == '\n')) {
						llkInput.consume(2);
						llkInput.newline();
					} else {
						if (!_cnt1) {
							throw llkParseException("()+ expected at least one occuence");
						}
						break _loop1;
					}
			}
		}
	}

	private void URL_PART() throws LLKParseException {
		_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
			if (llkGetBitInverted(LA1(), LLKTokenSet13.bitset)) {
				llkInput.consume();
			} else if ((LA1() == '\\') && (LA1() == '\\' && LA(2) != '\n')) {
				Escape();
			} else {
				if (!_cnt1) {
					throw llkParseException("()+ expected at least one occuence");
				}
				break _loop1;
			}
		}
	}

	private void Range() throws LLKParseException {
		HexDigit();
		if (LA1() == '?') {
			_loop1: for (int _cnt1 = 0; _cnt1 < 5; ++_cnt1) {
				if (llkInput.matchOpt('?')) {
				} else {
					if (_cnt1 < 5) {
						throw llkParseException("(Choices):min,max expected {5,5} occurence: actual=" + _cnt1);
					}
					break _loop1;
				}
			}
		} else if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
			HexDigit();
			if (LA1() == '?') {
				llkInput.consume();
				llkInput.consume();
				llkInput.consume();
				llkInput.consume();
			} else if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
				HexDigit();
				if (LA1() == '?') {
					llkInput.consume();
					llkInput.consume();
					llkInput.consume();
				} else if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
					HexDigit();
					if (LA1() == '?') {
						llkInput.consume();
						llkInput.consume();
					} else if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
						HexDigit();
						if (llkInput.matchOpt('?')) {
						} else if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
							HexDigit();
						} else {
							throw llkParseException("Unexpected token");
						}
					} else {
						throw llkParseException("Unexpected token");
					}
				} else {
					throw llkParseException("Unexpected token");
				}
			} else {
				throw llkParseException("Unexpected token");
			}
		} else {
			throw llkParseException("Unexpected token");
		}
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
	static final LLKTokenSet LLKTokenSet0 = new LLKTokenSet(
		true,
		0,
		4,
		new int[] {
			0xffffc9ff, 0xffff7ffe, 0x6fffffff, 0xf8000001, 
		}
	);
	static final LLKTokenSet LLKTokenSet1 = new LLKTokenSet(
		true,
		0,
		4,
		new int[] {
			0xffffffff, 0xffffffff, 0x6fffffff, 0xf8000001, 
		}
	);
	static final LLKTokenSet LLKTokenSet2 = new LLKTokenSet(
		false,
		0,
		4,
		new int[] {
			0x00000000, 0x03ff0000, 0x00000000, 0x0000007e, 
		}
	);
	static final LLKTokenSet LLKTokenSet3 = new LLKTokenSet(
		true,
		0,
		4,
		new int[] {
			0xffffffff, 0x03ff0000, 0x0000007e, 0x8000007e, 
		}
	);
	static final LLKTokenSet LLKTokenSet4 = new LLKTokenSet(
		true,
		0,
		4,
		new int[] {
			0xffffffff, 0xffffffff, 0x7fffffff, 0xf8000001, 
		}
	);
	static final LLKTokenSet LLKTokenSet5 = new LLKTokenSet(
		true,
		0,
		3,
		new int[] {
			0x00003400, 0x00000004, 0x10000000, 
		}
	);
	static final LLKTokenSet LLKTokenSet6 = new LLKTokenSet(
		true,
		0,
		3,
		new int[] {
			0x00003400, 0x00000080, 0x10000000, 
		}
	);
	static final LLKTokenSet LLKTokenSet7 = new LLKTokenSet(
		true,
		0,
		4,
		new int[] {
			0xffffffff, 0xfc00dfff, 0x6fffffff, 0xf8000001, 
		}
	);
	static final LLKTokenSet LLKTokenSet8 = new LLKTokenSet(
		true,
		0,
		4,
		new int[] {
			0xffffc9ff, 0x00000300, 0x00000000, 0x80000000, 
		}
	);
	static final LLKTokenSet LLKTokenSet9 = new LLKTokenSet(
		true,
		0,
		4,
		new int[] {
			0xffffffff, 0xffffffff, 0xffffffff, 0xf8000001, 
		}
	);
	static final LLKTokenSet LLKTokenSet10 = new LLKTokenSet(
		true,
		0,
		4,
		new int[] {
			0xffffffff, 0xfc00dfff, 0xffffffff, 0xf8000001, 
		}
	);
	static final LLKTokenSet LLKTokenSet11 = new LLKTokenSet(
		false,
		0,
		3,
		new int[] {
			0x00003600, 0x00000001, 0x10000000, 
		}
	);
	static final LLKTokenSet LLKTokenSet12 = new LLKTokenSet(
		true,
		0,
		4,
		new int[] {
			0xffffffff, 0x00000385, 0x00000000, 0x80000000, 
		}
	);
	static final LLKTokenSet LLKTokenSet13 = new LLKTokenSet(
		true,
		0,
		4,
		new int[] {
			0xffffffff, 0x00000385, 0x10000000, 0x80000000, 
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
		throw new UnsupportedOperationException("Grammar has no context");
	}
	
	public void llkSetContext(int context, int state) {
		throw new UnsupportedOperationException("Grammar has no context");
	}
	
	public void llkSetContext(int context, int state, ILLKToken lt0) {
		throw new UnsupportedOperationException("Grammar has no context");
	}
	
	////////////////////////////////////////////////////////////

	public boolean llkIsKeyword(String str) {
		return (llkKeywordTable.get(str) != null);
	}

	public int llkLookupKeyword(String str, int def) {
		Keyword ret = llkKeywordTable.get(str);
		if (ret == null)
			return def;
		return ret.type;
	}

	public int llkLookupKeyword(char[] src, int start, int end, int def) {
		return llkLookupKeyword(new String(src, start, end-start), def);
	}

	public void llkAddKeyword(String name, int type) {
		llkKeywordTable.put(name, new Keyword(type));
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
			if (!llkSyn_IdentifierStart())
				break _rule;
			llkRet = true;
		}
		llkInput.rewind();
		return llkRet;
	}

	private final boolean llkSyn_IdentifierStart() {
		if (LA1() == '\\') {
			if (!llkSyn_Escape())
				return false;
		} else if (llkGetBitInverted(LA1(), LLKTokenSet4.bitset)) {
			llkInput.consume();
		} else {
			return false;
		}
		return true;
	}

	private final boolean llkSyn_Escape() {
		if ((LA1() == '\\') && (llkGetBit(LA(2), LLKTokenSet2.bitset))) {
			if (!llkSyn_Unicode())
				return false;
		} else if ((LA1() == '\\') && (llkGetBitInverted(LA(2), LLKTokenSet3.bitset))) {
			llkInput.consume();
			llkInput.consume();
		} else {
			return false;
		}
		return true;
	}

	private final boolean llkSyn_Unicode() {
		if (!llkSynMatch('\\'))
			return false;
		_loop1: for (int _cnt1 = 0; _cnt1 < 6; ++_cnt1) {
			if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
				llkInput.consume();
			} else {
				if (_cnt1 < 1) {
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
			_loop1: for (int _cnt1 = 0; _cnt1 < 6; ++_cnt1) {
				if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
					llkInput.consume();
				} else {
					if (_cnt1 < 1) {
						break _rule;
					}
					break _loop1;
				}
			}
			if (!llkSynMatch('-'))
				break _rule;
			llkRet = true;
		}
		llkInput.rewind();
		return llkRet;
	}

	////////////////////////////////////////////////////////////
}
