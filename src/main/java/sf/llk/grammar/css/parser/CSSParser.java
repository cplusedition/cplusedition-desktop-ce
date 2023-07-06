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

import sf.llk.share.support.ILLKConstants;
import sf.llk.share.support.ILLKLifeCycleListener;
import sf.llk.share.support.ILLKNode;
import sf.llk.share.support.ILLKParser;
import sf.llk.share.support.LLKLexerInput;
import sf.llk.share.support.ILLKParserInput;
import sf.llk.share.support.LLKParserInput;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.support.LLKParseException;
import sf.llk.share.support.LLKParserBase;
import sf.llk.share.support.LLKTree;
import sf.llk.share.support.ILLKMain;

public class CSSParser extends LLKParserBase implements ILLKParser, ILLKCSSParser {

	////////////////////////////////////////////////////////////////////////

	public static final int ERROR = 1;

	////////////////////////////////////////////////////////////////////////

    public CSSParser(char[] source, ILLKMain main) {
        this(new LLKParserInput(new CSSLexer(new LLKLexerInput(source, main))));
    }

    public CSSParser(CSSLexer lexer) {
        this(new LLKParserInput(lexer));
    }

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private void llkOpenNode(LLKNode node) throws LLKParseException {
		llkTree.open();
		node.setFirstToken(LT(1));
	}

	private void llkCloseNode(LLKNode node, boolean create) throws LLKParseException {
		llkTree.close(node, create);
		if (create) {
			if (LT1() == node.getFirstToken()) {
				node.setFirstToken(null);
				node.setLastToken(null);
			} else {
				node.setLastToken(LT0());
			}
		}
	}

	private ILLKToken skipTo(int[] types) {
		DONE: for (int la1; (la1 = LA1()) != _EOF_; llkConsume()) {
			for (int type: types) {
				if (la1 == type) {
					break DONE;
				}
			}
		}
		return LT0();
	}

	private ILLKToken skip(int level) {
		for (int la1; (la1 = LA1()) != _EOF_; llkConsume()) {
			if (la1 == SEMICOLON && level == 0) {
				llkConsume();
				break;
			}
			if (la1 == LBRACE) {
				++level;
			} else if (la1 == RBRACE) {
				--level;
				if (level == 0) {
					llkConsume();
					break;
				}
			}
		}
		return LT0();
	}

	private void warn(MsgId id, Throwable e, ILLKToken first, ILLKToken last) {
		llkMain.warn(
			id.getMessage() + ": first@" + first.getLocationString() + ", last@" + last.getLocationString(),
			e,
			first.getOffset());
	}

	////////////////////////////////////////////////////////////////////////

	public String getLineno() {
		return "@" + (llkInput.getLocator().getLinear(LT0().getOffset()));
	}

	////////////////////////////////////////////////////////////////////////

	public static final int LLK_INPUT_VOCAB_SIZE = 100;

	public CSSParser(ILLKParserInput input) {
		llkInput = input;
		llkMain = input.getMain();
		llkTree = new LLKTree();
	}

	public void llkReset() {
		llkInput.reset();
		llkTree.reset();
		for (ILLKLifeCycleListener l: llkLifeCycleListeners) {
			l.reset();
		}
	}

	////////////////////////////////////////////////////////////

	private void s() throws LLKParseException {
		_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
			if (LA1() >= SPACES && LA1() <= COMMENT) {
				llkInput.consume();
			} else {
				if (!_cnt1) {
					throw llkParseException("()+ expected at least one occuence");
				}
				break _loop1;
			}
		}
	}

	private void ss() throws LLKParseException {
		_loop1: while (true) {
			if (LA1() >= SPACES && LA1() <= COMMENT) {
				llkInput.consume();
			} else {
				break _loop1;
			}
		}
	}

	private void ssc() throws LLKParseException {
		_loop1: while (true) {
			if (llkGetBit(LA1(), LLKTokenSet0.bitset)) {
				llkInput.consume();
			} else {
				break _loop1;
			}
		}
	}

	public ASTstyleSheet styleSheet() throws LLKParseException {
		ASTstyleSheet llkThis = new ASTstyleSheet();
		llkOpenNode(llkThis);
		if (LA1() == CHARSET) {
			charset();
		}
		ssc();
		_loop1: while (true) {
			if (LA1() == IMPORT) {
				Import();
				ssc();
			} else {
				break _loop1;
			}
		}
		_loop2: while (true) {
			if (llkGetBit(LA1(), LLKTokenSet1.bitset)) {
				switch (LA1()) {
					case MEDIA:
						media();
						break;
					case PAGE:
						page();
						break;
					case FONT_FACE:
						font_face();
						break;
					case AT_KEYWORD:
						atRule();
						break;
					case KEYFRAMES:
					case WEBKIT_KEYFRAMES:
						keyframes();
						break;
					case LENGTH:
					case ANGLE:
					case TIME:
					case FREQ:
					case RESOLUTION:
					case IMPORTANT:
					case BUILTIN:
					case ONLY:
					case NOT:
					case AND:
					case OF:
					case FROM:
					case TO:
					case AT:
					case CENTER:
					case SIDE:
					case SHAPE:
					case EXTENT_KEYWORD:
					case COLON:
					case STAR:
					case DOT:
					case IDENTIFIER:
					case HASH:
						ruleset();
						break;
					default:
						throw llkParseException("Unexpected token");
				}
				ssc();
			} else {
				break _loop2;
			}
		}
		llkCloseNode(llkThis, true);
		return llkThis;
	}

	public void charset() throws LLKParseException {
		ASTcharset llkThis = new ASTcharset();
		llkOpenNode(llkThis);
		llkMatch(CHARSET);
		ss();
		llkMatch(STRING);
		ss();
		llkMatch(SEMICOLON);
		llkCloseNode(llkThis, true);
	}

	public void Import() throws LLKParseException {
		ASTImport llkThis = new ASTImport();
		StringBuilder b = new StringBuilder();
		llkOpenNode(llkThis);
		try {
			llkMatch(IMPORT);
			b.append(LT0().getText());
			ss();
			if (llkInput.matchOpt(STRING)) {
				b.append(LT0().getText());
			} else if (llkInput.matchOpt(URI)) {
				b.append(LT0().getText());
			} else {
				throw llkParseException("Unexpected token");
			}
			ss();
			if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
				medium();
				_loop1: while (true) {
					if (llkInput.matchOpt(COMMA)) {
						ss();
						medium();
					} else {
						break _loop1;
					}
				}
			}
			llkMatch(SEMICOLON);
		} catch (Throwable e) {
			llkThis.setModifiers(ERROR);
			ILLKToken first = LT0();
			ILLKToken last = skip(0);
			warn(MsgId.ParserInvalidConstructIgnored, e, first, last);
		} finally {
		}
		llkCloseNode(llkThis, true);
		llkThis.setText(b.toString());
	}

	public void media() throws LLKParseException {
		ASTmedia llkThis = new ASTmedia();
		int level = 0;
		StringBuilder b = new StringBuilder();
		llkOpenNode(llkThis);
		try {
			llkMatch(MEDIA);
			ss();
			if (llkBitTest(LA1(), ONLY, NOT, LPAREN, IDENTIFIER)) {
				media_query_list();
			}
			llkMatch(LBRACE);
			++level;
			ss();
			_loop1: while (true) {
				if (llkGetBit(LA1(), LLKTokenSet3.bitset)) {
					ruleset();
					ss();
				} else {
					break _loop1;
				}
			}
			llkMatch(RBRACE);
			--level;
		} catch (Throwable e) {
			llkThis.setModifiers(ERROR);
			ILLKToken first = LT0();
			ILLKToken last = skip(level);
			warn(MsgId.ParserInvalidConstructIgnored, e, first, last);
		} finally {
		}
		llkCloseNode(llkThis, true);
	}

	public void media_query_list() throws LLKParseException {
		ASTmedia_query_list llkThis = new ASTmedia_query_list();
		llkOpenNode(llkThis);
		media_query();
		_loop1: while (true) {
			if (llkInput.matchOpt(COMMA)) {
				ss();
				media_query();
			} else {
				break _loop1;
			}
		}
		llkCloseNode(llkThis, true);
	}

	public void media_query() throws LLKParseException {
		ASTmedia_query llkThis = new ASTmedia_query();
		llkOpenNode(llkThis);
		if (llkBitTest(LA1(), ONLY, NOT, IDENTIFIER)) {
			if (llkInput.matchOpt(ONLY)) {
				ss();
			} else if (llkInput.matchOpt(NOT)) {
				ss();
			}
			media_type();
			ss();
			_loop1: while (true) {
				if (llkInput.matchOpt(AND)) {
					media_expr();
				} else {
					break _loop1;
				}
			}
		} else if (LA1() == LPAREN) {
			media_expr();
			_loop2: while (true) {
				if (llkInput.matchOpt(AND)) {
					ss();
					media_expr();
				} else {
					break _loop2;
				}
			}
		} else {
			throw llkParseException("Unexpected token");
		}
		llkCloseNode(llkThis, true);
	}

	public void media_type() throws LLKParseException {
		ASTmedia_type llkThis = new ASTmedia_type();
		llkOpenNode(llkThis);
		llkMatch(IDENTIFIER);
		llkCloseNode(llkThis, true);
	}

	public void media_expr() throws LLKParseException {
		ASTmedia_expr llkThis = new ASTmedia_expr();
		llkOpenNode(llkThis);
		llkMatch(LPAREN);
		ss();
		media_feature();
		ss();
		if (llkInput.matchOpt(COLON)) {
			ss();
			expr();
		}
		llkMatch(RPAREN);
		ss();
		llkCloseNode(llkThis, true);
	}

	public void media_feature() throws LLKParseException {
		ASTmedia_feature llkThis = new ASTmedia_feature();
		llkOpenNode(llkThis);
		llkMatch(IDENTIFIER);
		llkCloseNode(llkThis, true);
	}

	public void medium() throws LLKParseException {
		ASTmedium llkThis = new ASTmedium();
		llkOpenNode(llkThis);
		llkMatch(LLKTokenSet2.bitset);
		ss();
		llkCloseNode(llkThis, true);
	}

	public void keyframes() throws LLKParseException {
		ASTkeyframes llkThis = new ASTkeyframes();
		llkOpenNode(llkThis);
		llkMatch(KEYFRAMES, WEBKIT_KEYFRAMES);
		ss();
		llkMatch(IDENTIFIER);
		ss();
		llkMatch(LBRACE);
		ss();
		_loop1: while (true) {
			if (llkGetBit(LA1(), LLKTokenSet4.bitset)) {
				keyframes_block();
			} else {
				break _loop1;
			}
		}
		llkMatch(RBRACE);
		ss();
		llkCloseNode(llkThis, true);
	}

	public void keyframes_block() throws LLKParseException {
		ASTkeyframes_block llkThis = new ASTkeyframes_block();
		llkOpenNode(llkThis);
		keyframe_selector();
		llkMatch(LBRACE);
		ss();
		if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
			declaration();
			ss();
		}
		_loop1: while (true) {
			if (llkInput.matchOpt(SEMICOLON)) {
				ss();
				if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
					declaration();
					ss();
				}
			} else {
				break _loop1;
			}
		}
		llkMatch(RBRACE);
		ss();
		llkCloseNode(llkThis, true);
	}

	public void keyframe_selector() throws LLKParseException {
		ASTkeyframe_selector llkThis = new ASTkeyframe_selector();
		llkOpenNode(llkThis);
		if (LA1() == FROM || LA1() == TO) {
			llkInput.consume();
		} else if (llkBitTest(LA1(), HEX, DECIMAL, REAL, CALC)) {
			Number();
			llkMatch(PERCENT);
		} else {
			throw llkParseException("Unexpected token");
		}
		ss();
		_loop1: while (true) {
			if (llkInput.matchOpt(COMMA)) {
				ss();
				if (LA1() == FROM || LA1() == TO) {
					llkInput.consume();
				} else if (llkBitTest(LA1(), HEX, DECIMAL, REAL, CALC)) {
					Number();
					llkMatch(PERCENT);
				} else {
					throw llkParseException("Unexpected token");
				}
				ss();
			} else {
				break _loop1;
			}
		}
		llkCloseNode(llkThis, true);
	}

	public void page() throws LLKParseException {
		ASTpage llkThis = new ASTpage();
		int level = 0;
		StringBuilder b = new StringBuilder();
		llkOpenNode(llkThis);
		try {
			llkMatch(PAGE);
			b.append(LT0().getText());
			ss();
			if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
				llkInput.consume();
			}
			pseudo_page();
			b.append(LT0().getText());
			ss();
			llkMatch(LBRACE);
			level = 1;
			ss();
			if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
				declaration();
				ss();
			}
			_loop1: while (true) {
				if (llkInput.matchOpt(SEMICOLON)) {
					ss();
					if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
						declaration();
						ss();
					}
				} else {
					break _loop1;
				}
			}
			llkMatch(RBRACE);
			level = 0;
			if (true) {
				ss();
			}
		} catch (Throwable e) {
			llkThis.setModifiers(ERROR);
			ILLKToken first = LT0();
			ILLKToken last = skip(level);
			warn(MsgId.ParserInvalidConstructIgnored, e, first, last);
		} finally {
		}
		llkCloseNode(llkThis, true);
		llkThis.setText(b.toString());
	}

	public void pseudo_page() throws LLKParseException {
		ASTpseudo_page llkThis = new ASTpseudo_page();
		llkOpenNode(llkThis);
		llkMatch(COLON);
		llkMatch(LLKTokenSet2.bitset);
		llkCloseNode(llkThis, true);
	}

	public void font_face() throws LLKParseException {
		ASTfont_face llkThis = new ASTfont_face();
		int level = 0;
		llkOpenNode(llkThis);
		try {
			llkMatch(FONT_FACE);
			llkThis.setText(LT0().getText());
			ss();
			llkMatch(LBRACE);
			++level;
			ss();
			if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
				declaration();
				ss();
			}
			_loop1: while (true) {
				if (llkInput.matchOpt(SEMICOLON)) {
					ss();
					if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
						declaration();
						ss();
					}
				} else {
					break _loop1;
				}
			}
			llkMatch(RBRACE);
			--level;
			if (true) {
				ss();
			}
		} catch (Throwable e) {
			llkThis.setModifiers(ERROR);
			ILLKToken first = LT0();
			ILLKToken last = skip(level);
			warn(MsgId.ParserInvalidConstructIgnored, e, first, last);
		} finally {
		}
		llkCloseNode(llkThis, true);
	}

	public void atRule() throws LLKParseException {
		ASTatRule llkThis = new ASTatRule();
		llkOpenNode(llkThis);
		llkMatch(AT_KEYWORD);
		llkThis.setText(LT0().getText());
		llkThis.setModifiers(ERROR);
		ILLKToken first = LT0();
		ILLKToken last = skip(0);
		warn(MsgId.ParserInvalidConstructIgnored, null, first, last);
		llkCloseNode(llkThis, true);
	}

	public void operator() throws LLKParseException {
		ASToperator llkThis = new ASToperator();
		llkOpenNode(llkThis);
		if (llkBitTest(LA1(), COMMA, STAR, SLASH)) {
			llkInput.consume();
			ss();
		} else if (llkSynPredict0()) {
			llkInput.consume();
			ss();
		}
		llkCloseNode(llkThis, true);
	}

	public void combinator() throws LLKParseException {
		ASTcombinator llkThis = new ASTcombinator();
		llkOpenNode(llkThis);
		if (llkGetBit(LA1(), LLKTokenSet5.bitset)) {
			if (llkBitTest(LA1(), TILDE, COLUMN, PLUS, GT)) {
				llkInput.consume();
			} else if (llkInput.matchOpt(SLASH)) {
				if ((LA1() == IDENTIFIER) && (LA(2) == SLASH)) {
					llkInput.consume();
					llkInput.consume();
				}
			} else {
				throw llkParseException("Unexpected token");
			}
			ss();
		}
		llkCloseNode(llkThis, true);
	}

	public void unary_operator() throws LLKParseException {
		ASTunary_operator llkThis = new ASTunary_operator();
		llkOpenNode(llkThis);
		llkMatch(PLUS, DASH);
		llkCloseNode(llkThis, true);
	}

	public void property() throws LLKParseException {
		ASTproperty llkThis = new ASTproperty();
		llkOpenNode(llkThis);
		llkMatch(LLKTokenSet2.bitset);
		llkCloseNode(llkThis, true);
	}

	public void ruleset() throws LLKParseException {
		ASTruleset llkThis = new ASTruleset();
		int level = 0;
		ILLKNode n;
		llkOpenNode(llkThis);
		try {
			selector();
			_loop1: while (true) {
				if (llkInput.matchOpt(COMMA)) {
					ss();
					if (llkGetBit(LA1(), LLKTokenSet3.bitset)) {
						selector();
					}
				} else {
					break _loop1;
				}
			}
			llkMatch(LBRACE);
			++level;
			ss();
			if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
				declaration();
				ss();
			}
			_loop2: while (true) {
				if (llkInput.matchOpt(SEMICOLON)) {
					ss();
					if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
						declaration();
						ss();
					}
				} else {
					break _loop2;
				}
			}
			llkMatch(RBRACE);
			--level;
		} catch (Throwable e) {
			llkThis.setModifiers(ERROR);
			ILLKToken first = LT0();
			ILLKToken last = skip(level);
			warn(MsgId.ParserInvalidConstructIgnored, e, first, last);
		} finally {
		}
		llkCloseNode(llkThis, true);
	}

	public void selector() throws LLKParseException {
		ASTselector llkThis = new ASTselector();
		llkOpenNode(llkThis);
		simple_selector();
		_loop1: while (true) {
			if (llkGetBit(LA1(), LLKTokenSet6.bitset)) {
				combinator();
				simple_selector();
			} else {
				break _loop1;
			}
		}
		llkCloseNode(llkThis, true);
		llkThis.setText(llkGetSource(llkThis.getOffset(), llkThis.getEndOffset()));
	}

	public void simple_selector() throws LLKParseException {
		ASTsimple_selector llkThis = new ASTsimple_selector();
		llkOpenNode(llkThis);
		switch (LA1()) {
			case HASH:
				llkInput.consume();
				_loop1: while (true) {
					switch (LA1()) {
						case HASH:
							llkInput.consume();
							break;
						case DOT:
							Class();
							break;
						case COLON:
							pseudo();
							break;
						case LBRACKET:
							attrib();
							break;
						default:
							break _loop1;
					}
				}
				break;
			case COLON:
				pseudo();
				_loop2: while (true) {
					switch (LA1()) {
						case HASH:
							llkInput.consume();
							break;
						case DOT:
							Class();
							break;
						case COLON:
							pseudo();
							break;
						case LBRACKET:
							attrib();
							break;
						default:
							break _loop2;
					}
				}
				break;
			case DOT:
				Class();
				_loop3: while (true) {
					switch (LA1()) {
						case HASH:
							llkInput.consume();
							break;
						case DOT:
							Class();
							break;
						case COLON:
							pseudo();
							break;
						case LBRACKET:
							attrib();
							break;
						default:
							break _loop3;
					}
				}
				break;
			case LENGTH:
			case ANGLE:
			case TIME:
			case FREQ:
			case RESOLUTION:
			case IMPORTANT:
			case BUILTIN:
			case ONLY:
			case NOT:
			case AND:
			case OF:
			case FROM:
			case TO:
			case AT:
			case CENTER:
			case SIDE:
			case SHAPE:
			case EXTENT_KEYWORD:
			case STAR:
			case IDENTIFIER:
				qname();
				_loop4: while (true) {
					switch (LA1()) {
						case HASH:
							llkInput.consume();
							break;
						case DOT:
							Class();
							break;
						case COLON:
							pseudo();
							break;
						case LBRACKET:
							attrib();
							break;
						default:
							break _loop4;
					}
				}
				break;
			default:
				throw llkParseException("Unexpected token");
		}
		ss();
		llkCloseNode(llkThis, true);
	}

	public void Class() throws LLKParseException {
		ASTClass llkThis = new ASTClass();
		llkOpenNode(llkThis);
		llkMatch(DOT);
		llkMatch(LLKTokenSet2.bitset);
		llkCloseNode(llkThis, true);
	}

	public void qname() throws LLKParseException {
		ASTqname llkThis = new ASTqname();
		llkOpenNode(llkThis);
		llkMatch(LLKTokenSet7.bitset);
		if (llkInput.matchOpt(OR)) {
			llkMatch(LLKTokenSet7.bitset);
		}
		llkCloseNode(llkThis, true);
	}

	public void attrib() throws LLKParseException {
		ASTattrib llkThis = new ASTattrib();
		llkOpenNode(llkThis);
		llkMatch(LBRACKET);
		ss();
		llkMatch(LLKTokenSet2.bitset);
		ss();
		if (llkGetBit(LA1(), LLKTokenSet8.bitset)) {
			llkInput.consume();
			ss();
			llkMatch(LLKTokenSet9.bitset);
			ss();
		}
		llkMatch(RBRACKET);
		llkCloseNode(llkThis, true);
	}

	public void pseudo() throws LLKParseException {
		ASTpseudo llkThis = new ASTpseudo();
		llkOpenNode(llkThis);
		llkMatch(COLON);
		if ((LA1() == COLON) && (true)) {
			llkInput.consume();
		}
		if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
			llkInput.consume();
		} else if (llkInput.matchOpt(FUNCTION)) {
			ss();
			switch (LA1()) {
				case LBRACKET:
					attrib();
					ss();
					break;
				case HEX:
				case DECIMAL:
				case REAL:
				case CALC:
					Number();
					ss();
					if (llkInput.matchOpt(OF)) {
						selector();
					}
					break;
				case LENGTH:
				case ANGLE:
				case TIME:
				case FREQ:
				case RESOLUTION:
				case IMPORTANT:
				case BUILTIN:
				case ONLY:
				case NOT:
				case AND:
				case OF:
				case FROM:
				case TO:
				case AT:
				case CENTER:
				case SIDE:
				case SHAPE:
				case EXTENT_KEYWORD:
				case COLON:
				case STAR:
				case DOT:
				case IDENTIFIER:
				case HASH:
					selector();
					ss();
					_loop1: while (true) {
						if (llkInput.matchOpt(COMMA)) {
							ss();
							selector();
							ss();
						} else {
							break _loop1;
						}
					}
					break;
				default:
					throw llkParseException("Unexpected token");
			}
			llkMatch(RPAREN);
		}
		llkCloseNode(llkThis, true);
	}

	public void declaration() throws LLKParseException {
		ASTdeclaration llkThis = new ASTdeclaration();
		llkOpenNode(llkThis);
		try {
			property();
			ss();
			llkMatch(COLON);
			ss();
			if (llkGetBit(LA1(), LLKTokenSet10.bitset)) {
				expr();
			}
			if (LA1() == EXCLAIMATION) {
				prio();
			}
		} catch (Exception e) {
			ILLKToken first = LT0();
			llkThis.setModifiers(ERROR);
			ILLKToken last = skipTo(new int[] { SEMICOLON, RBRACE });
			warn(MsgId.ParserInvalidConstructIgnored, e, first, last);
		} finally {
		}
		llkCloseNode(llkThis, true);
	}

	public void prio() throws LLKParseException {
		ASTprio llkThis = new ASTprio();
		llkOpenNode(llkThis);
		llkMatch(EXCLAIMATION);
		ss();
		llkMatch(IMPORTANT);
		ss();
		llkCloseNode(llkThis, true);
	}

	public void expr() throws LLKParseException {
		ASTexpr llkThis = new ASTexpr();
		llkOpenNode(llkThis);
		term();
		_loop1: while (true) {
			if (llkGetBit(LA1(), LLKTokenSet11.bitset)) {
				operator();
				term();
			} else {
				break _loop1;
			}
		}
		llkCloseNode(llkThis, true);
	}

	public void term() throws LLKParseException {
		ASTterm llkThis = new ASTterm();
		llkOpenNode(llkThis);
		switch (LA1()) {
			case PLUS:
			case DASH:
				unary_operator();
				units();
				break;
			case RGB:
			case RGBA:
			case HASH:
				color();
				break;
			case LINEAR_GRADIENT:
			case RADIAL_GRADIENT:
			case REPEATING_LINEAR_GRADIENT:
			case REPEATING_RADIAL_GRADIENT:
				gradient();
				break;
			case HEX:
			case DECIMAL:
			case REAL:
			case VAR:
			case CALC:
			case ATTR:
			case FUNCTION:
				units();
				break;
			case LENGTH:
			case ANGLE:
			case TIME:
			case FREQ:
			case RESOLUTION:
			case IMPORTANT:
			case BUILTIN:
			case ONLY:
			case NOT:
			case AND:
			case OF:
			case FROM:
			case TO:
			case AT:
			case CENTER:
			case SIDE:
			case SHAPE:
			case EXTENT_KEYWORD:
			case IDENTIFIER:
			case URI:
			case STRING:
			case UNICODE_RANGE:
				llkInput.consume();
				break;
			default:
				throw llkParseException("Unexpected token");
		}
		ss();
		llkCloseNode(llkThis, true);
	}

	private void units() throws LLKParseException {
		switch (LA1()) {
			case VAR:
				varref();
				break;
			case CALC:
				calc();
				break;
			case ATTR:
				attr();
				break;
			case FUNCTION:
				function();
				break;
			case HEX:
			case DECIMAL:
			case REAL:
				llkInput.consume();
				if (llkGetBit(LA1(), LLKTokenSet12.bitset)) {
					llkInput.consume();
				}
				break;
			default:
				throw llkParseException("Unexpected token");
		}
	}

	private void varref() throws LLKParseException {
		llkMatch(VAR);
		ss();
		llkMatch(DASHDASH);
		llkMatch(IDENTIFIER);
		ss();
		llkMatch(RPAREN);
	}

	public void color() throws LLKParseException {
		ASTcolor llkThis = new ASTcolor();
		llkOpenNode(llkThis);
		switch (LA1()) {
			case HASH:
				hexcolor();
				break;
			case RGB:
				llkInput.consume();
				ss();
				Number();
				ss();
				llkMatch(COMMA);
				ss();
				Number();
				ss();
				llkMatch(COMMA);
				ss();
				Number();
				ss();
				if (llkInput.matchOpt(COMMA)) {
					ss();
				}
				llkMatch(RPAREN);
				break;
			case RGBA:
				llkInput.consume();
				ss();
				Number();
				ss();
				llkMatch(COMMA);
				ss();
				Number();
				ss();
				llkMatch(COMMA);
				ss();
				Number();
				ss();
				llkMatch(COMMA);
				ss();
				Number();
				ss();
				if (llkInput.matchOpt(COMMA)) {
					ss();
				}
				llkMatch(RPAREN);
				break;
			default:
				throw llkParseException("Unexpected token");
		}
		llkCloseNode(llkThis, true);
	}

	private void gradient() throws LLKParseException {
		if (LA1() == LINEAR_GRADIENT || LA1() == REPEATING_LINEAR_GRADIENT) {
			linear_gradient();
		} else if (LA1() == RADIAL_GRADIENT || LA1() == REPEATING_RADIAL_GRADIENT) {
			radial_gradient();
		} else {
			throw llkParseException("Unexpected token");
		}
	}

	public void linear_gradient() throws LLKParseException {
		ASTlinear_gradient llkThis = new ASTlinear_gradient();
		llkOpenNode(llkThis);
		llkMatch(LINEAR_GRADIENT, REPEATING_LINEAR_GRADIENT);
		ss();
		if (llkBitTest(LA1(), TO, HEX, DECIMAL, REAL)) {
			if (LA1() >= HEX && LA1() <= REAL) {
				llkInput.consume();
				llkMatch(ANGLE);
			} else if (llkInput.matchOpt(TO)) {
				ss();
				side_or_corner();
			} else {
				throw llkParseException("Unexpected token");
			}
			ss();
			llkMatch(COMMA);
			ss();
		}
		color_stop();
		_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
			if (llkInput.matchOpt(COMMA)) {
				ss();
				color_stop();
			} else {
				if (!_cnt1) {
					throw llkParseException("()+ expected at least one occuence");
				}
				break _loop1;
			}
		}
		llkMatch(RPAREN);
		llkCloseNode(llkThis, true);
	}

	public void radial_gradient() throws LLKParseException {
		ASTradial_gradient llkThis = new ASTradial_gradient();
		llkOpenNode(llkThis);
		llkMatch(RADIAL_GRADIENT, REPEATING_RADIAL_GRADIENT);
		ss();
		if (llkGetBit(LA1(), LLKTokenSet13.bitset)) {
			if (llkInput.matchOpt(SHAPE)) {
				ss();
				if (llkGetBit(LA1(), LLKTokenSet14.bitset)) {
					if (llkGetBit(LA1(), LLKTokenSet15.bitset)) {
						length_or_percent();
					} else if (llkInput.matchOpt(EXTENT_KEYWORD)) {
					} else {
						throw llkParseException("Unexpected token");
					}
					ss();
					if (llkGetBit(LA1(), LLKTokenSet15.bitset)) {
						length_or_percent();
						ss();
					}
				}
			} else if (llkGetBit(LA1(), LLKTokenSet14.bitset)) {
				if (llkGetBit(LA1(), LLKTokenSet15.bitset)) {
					length_or_percent();
				} else if (llkInput.matchOpt(EXTENT_KEYWORD)) {
				} else {
					throw llkParseException("Unexpected token");
				}
				ss();
				if (llkGetBit(LA1(), LLKTokenSet15.bitset)) {
					length_or_percent();
					ss();
				}
				if (llkInput.matchOpt(SHAPE)) {
					ss();
				}
			}
			if (llkInput.matchOpt(AT)) {
				ss();
				position();
				ss();
			}
			llkMatch(COMMA);
			ss();
		}
		color_stop();
		_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
			if (llkInput.matchOpt(COMMA)) {
				ss();
				color_stop();
			} else {
				if (!_cnt1) {
					throw llkParseException("()+ expected at least one occuence");
				}
				break _loop1;
			}
		}
		llkMatch(RPAREN);
		llkCloseNode(llkThis, true);
	}

	public void side_or_corner() throws LLKParseException {
		ASTside_or_corner llkThis = new ASTside_or_corner();
		llkOpenNode(llkThis);
		llkMatch(SIDE);
		llkInput.matchOpt(SIDE);
		llkCloseNode(llkThis, true);
	}

	public void position() throws LLKParseException {
		ASTposition llkThis = new ASTposition();
		llkOpenNode(llkThis);
		position1();
		if (llkSynPredict1()) {
			s();
			position1();
		}
		llkCloseNode(llkThis, true);
	}

	public void color_stop() throws LLKParseException {
		ASTcolor_stop llkThis = new ASTcolor_stop();
		llkOpenNode(llkThis);
		if (llkInput.matchOpt(IDENTIFIER)) {
		} else if (llkBitTest(LA1(), RGB, RGBA, HASH)) {
			color();
		} else {
			throw llkParseException("Unexpected token");
		}
		ss();
		if (llkGetBit(LA1(), LLKTokenSet15.bitset)) {
			length_or_percent();
			ss();
		}
		llkCloseNode(llkThis, true);
	}

	private void position1() throws LLKParseException {
		if (LA1() == CENTER || LA1() == SIDE) {
			llkInput.consume();
		} else if (llkGetBit(LA1(), LLKTokenSet15.bitset)) {
			length_or_percent();
		} else {
			throw llkParseException("Unexpected token");
		}
	}

	private void length_or_percent() throws LLKParseException {
		if (LA1() == PLUS || LA1() == DASH) {
			llkInput.consume();
		}
		if (LA1() >= HEX && LA1() <= REAL) {
			llkInput.consume();
			llkMatch(LENGTH, PERCENT);
		} else if (LA1() == CALC) {
			calc();
		} else {
			throw llkParseException("Unexpected token");
		}
	}

	public void calc() throws LLKParseException {
		ASTcalc llkThis = new ASTcalc();
		llkOpenNode(llkThis);
		llkMatch(CALC);
		ss();
		sum();
		ss();
		llkMatch(RPAREN);
		llkCloseNode(llkThis, true);
	}

	public void sum() throws LLKParseException {
		ASTsum llkThis = new ASTsum();
		llkOpenNode(llkThis);
		product();
		_loop1: while (true) {
			if (llkSynPredict2()) {
				s();
				llkMatch(PLUS, DASH);
				s();
				product();
			} else {
				break _loop1;
			}
		}
		llkCloseNode(llkThis, true);
	}

	public void product() throws LLKParseException {
		ASTproduct llkThis = new ASTproduct();
		llkOpenNode(llkThis);
		unit();
		_loop1: while (true) {
			if (llkSynPredict3()) {
				ss();
				if (llkInput.matchOpt(STAR)) {
					ss();
					unit();
				} else if (llkInput.matchOpt(SLASH)) {
					ss();
					Number();
				} else {
					throw llkParseException("Unexpected token");
				}
			} else {
				break _loop1;
			}
		}
		llkCloseNode(llkThis, true);
	}

	public void attr() throws LLKParseException {
		ASTattr llkThis = new ASTattr();
		llkOpenNode(llkThis);
		llkMatch(ATTR);
		ss();
		qname();
		if (LA1() >= SPACES && LA1() <= COMMENT) {
			s();
			if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
				llkInput.consume();
				ss();
			}
		}
		if (llkInput.matchOpt(COMMA)) {
			ss();
			unit();
			ss();
		}
		llkMatch(RPAREN);
		llkCloseNode(llkThis, true);
	}

	public void unit() throws LLKParseException {
		ASTunit llkThis = new ASTunit();
		llkOpenNode(llkThis);
		switch (LA1()) {
			case BUILTIN:
				llkInput.consume();
				break;
			case VAR:
				varref();
				break;
			case CALC:
				calc();
				break;
			case ATTR:
				attr();
				break;
			case LPAREN:
				llkInput.consume();
				ss();
				sum();
				ss();
				llkMatch(RPAREN);
				break;
			case HEX:
			case DECIMAL:
			case REAL:
				llkInput.consume();
				if (llkGetBit(LA1(), LLKTokenSet12.bitset)) {
					llkInput.consume();
				}
				break;
			default:
				throw llkParseException("Unexpected token");
		}
		llkCloseNode(llkThis, true);
	}

	public void function() throws LLKParseException {
		ASTfunction llkThis = new ASTfunction();
		llkOpenNode(llkThis);
		llkMatch(FUNCTION);
		ss();
		if (llkGetBit(LA1(), LLKTokenSet10.bitset)) {
			expr();
		}
		llkMatch(RPAREN);
		llkCloseNode(llkThis, true);
	}

	public void hexcolor() throws LLKParseException {
		ASThexcolor llkThis = new ASThexcolor();
		llkOpenNode(llkThis);
		llkMatch(HASH);
		ss();
		llkCloseNode(llkThis, true);
	}

	private void Number() throws LLKParseException {
		if (LA1() >= HEX && LA1() <= REAL) {
			llkInput.consume();
		} else if (LA1() == CALC) {
			calc();
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
		false,
		0,
		3,
		new int[] {
			0x40000000, 0x38000000, 0x00000002, 
		}
	);
	static final LLKTokenSet LLKTokenSet1 = new LLKTokenSet(
		false,
		0,
		3,
		new int[] {
			0x1ffffee0, 0x06000a00, 0x48000000, 
		}
	);
	static final LLKTokenSet LLKTokenSet2 = new LLKTokenSet(
		false,
		0,
		2,
		new int[] {
			0x1ffff800, 0x04000000, 
		}
	);
	static final LLKTokenSet LLKTokenSet3 = new LLKTokenSet(
		false,
		0,
		3,
		new int[] {
			0x1ffff800, 0x06000a00, 0x40000000, 
		}
	);
	static final LLKTokenSet LLKTokenSet4 = new LLKTokenSet(
		false,
		0,
		3,
		new int[] {
			0x00c00000, 0x01c00000, 0x00000400, 
		}
	);
	static final LLKTokenSet LLKTokenSet5 = new LLKTokenSet(
		false,
		0,
		2,
		new int[] {
			0x80000000, 0x40003080, 
		}
	);
	static final LLKTokenSet LLKTokenSet6 = new LLKTokenSet(
		false,
		0,
		3,
		new int[] {
			0x9ffff800, 0x46003a80, 0x40000000, 
		}
	);
	static final LLKTokenSet LLKTokenSet7 = new LLKTokenSet(
		false,
		0,
		2,
		new int[] {
			0x1ffff800, 0x04000800, 
		}
	);
	static final LLKTokenSet LLKTokenSet8 = new LLKTokenSet(
		false,
		0,
		2,
		new int[] {
			0x00000000, 0x0020001f, 
		}
	);
	static final LLKTokenSet LLKTokenSet9 = new LLKTokenSet(
		false,
		0,
		3,
		new int[] {
			0x1ffff800, 0x04000000, 0x00800000, 
		}
	);
	static final LLKTokenSet LLKTokenSet10 = new LLKTokenSet(
		false,
		0,
		4,
		new int[] {
			0x1ffff800, 0x85c01000, 0x40801ffc, 0x00000001, 
		}
	);
	static final LLKTokenSet LLKTokenSet11 = new LLKTokenSet(
		false,
		0,
		4,
		new int[] {
			0x1ffff800, 0xc5c01900, 0x40801ffc, 0x00000001, 
		}
	);
	static final LLKTokenSet LLKTokenSet12 = new LLKTokenSet(
		false,
		0,
		1,
		new int[] {
			0x2000f800, 
		}
	);
	static final LLKTokenSet LLKTokenSet13 = new LLKTokenSet(
		false,
		0,
		3,
		new int[] {
			0x19000000, 0x81c01100, 0x00000400, 
		}
	);
	static final LLKTokenSet LLKTokenSet14 = new LLKTokenSet(
		false,
		0,
		3,
		new int[] {
			0x10000000, 0x81c01000, 0x00000400, 
		}
	);
	static final LLKTokenSet LLKTokenSet15 = new LLKTokenSet(
		false,
		0,
		3,
		new int[] {
			0x00000000, 0x81c01000, 0x00000400, 
		}
	);
	static final LLKTokenSet LLKTokenSet16 = new LLKTokenSet(
		false,
		0,
		2,
		new int[] {
			0x00000000, 0x78000800, 
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
	
	////////////////////////////////////////////////////////////

	private final boolean llkSynPredict0() {
		boolean llkRet = false;
		llkInput.mark();
		_rule: {
			if (!llkSynMatch(PLUS, DASH))
				break _rule;
			if (!llkSyn_ss())
				break _rule;
			llkRet = true;
		}
		llkInput.rewind();
		return llkRet;
	}

	private final boolean llkSyn_ss() {
		_loop1: while (true) {
			if (LA1() >= SPACES && LA1() <= COMMENT) {
				llkInput.consume();
			} else {
				break _loop1;
			}
		}
		return true;
	}

	private final boolean llkSynPredict1() {
		boolean llkRet = false;
		llkInput.mark();
		_rule: {
			if (!llkSyn_s())
				break _rule;
			if (!llkSynMatchNot(COMMA))
				break _rule;
			llkRet = true;
		}
		llkInput.rewind();
		return llkRet;
	}

	private final boolean llkSyn_s() {
		_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
			if (LA1() >= SPACES && LA1() <= COMMENT) {
				llkInput.consume();
			} else {
				if (!_cnt1) {
					return false;
				}
				break _loop1;
			}
		}
		return true;
	}

	private final boolean llkSynPredict2() {
		boolean llkRet = false;
		llkInput.mark();
		_rule: {
			if (!llkSyn_s())
				break _rule;
			if (!llkSynMatch(PLUS, DASH))
				break _rule;
			llkRet = true;
		}
		llkInput.rewind();
		return llkRet;
	}

	private final boolean llkSynPredict3() {
		boolean llkRet = false;
		llkInput.mark();
		_rule: {
			if (!llkSyn_ss())
				break _rule;
			if (!llkSynMatch(STAR, SLASH))
				break _rule;
			llkRet = true;
		}
		llkInput.rewind();
		return llkRet;
	}

	////////////////////////////////////////////////////////////
}
