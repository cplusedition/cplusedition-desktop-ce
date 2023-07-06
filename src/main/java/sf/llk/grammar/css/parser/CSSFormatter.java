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

import java.util.*;

import java.util.ArrayList;
import java.util.List;

import sf.llk.grammar.css.CSSInfo;
import sf.llk.share.formatter.FormatBuilder;
import sf.llk.share.support.ILLKConstants;
import sf.llk.share.support.ILLKLexer;
import sf.llk.share.support.ILLKLifeCycleListener;
import sf.llk.share.support.ILLKMain;
import sf.llk.share.support.ILLKParserInput;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.support.ILLKTreeBuilder;
import sf.llk.share.support.ISourceLocation;
import sf.llk.share.support.LLKLexerInput;
import sf.llk.share.support.LLKParseError;
import sf.llk.share.support.LLKParseException;
import sf.llk.share.support.LLKParserInput;

public class CSSFormatter extends CSSFormatterBase {

	////////////////////////////////////////////////////////////////////////

	public CSSFormatter(ILLKLexer lexer, CSSFormatOptions options) {
		this(new LLKParserInput(lexer));
		init(0, Integer.MAX_VALUE, options);
	}

	public CSSFormatter(char[] source, ILLKMain main, CSSFormatOptions options) {
		this(new CSSLexer(new LLKLexerInput(source, main)), options);
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public void llkInit() {
	}

	public void checkPropertyName(ILLKToken t) {
		String name = t.getText().toString();
		if (!CSSInfo.isValidProperty(name)) {
			llkMain.warn("WARN: Unknown property: " + name, null, t.getOffset());
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

	private ILLKToken skipTokens(int level) {
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

	////////////////////////////////////////////////////////////////////////

	public String getLineno() {
		return "@" + (llkInput.getLocator().getLinear(LT0().getOffset()));
	}

	public ILLKMain getMain() {
		return llkMain;
	}

	////////////////////////////////////////////////////////////////////////

	public static final int LLK_INPUT_VOCAB_SIZE = 145;

	private CSSFormatter(ILLKParserInput input) {
		llkInput = input;
		llkMain = input.getMain();
		llkInit();
	}

	public void llkReset() {
		llkInput.reset();
		llkInit();
		for (ILLKLifeCycleListener l: llkLifeCycleListeners) {
			l.reset();
		}
	}

	////////////////////////////////////////////////////////////

	private void s() throws LLKParseException {
		ILLKToken lt0 = LT0();
		llkInput.mark();
		ILLKToken start = null;
		_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
			if (LA1() >= SPACES && LA1() <= COMMENT) {
				llkInput.consume();
				if (start == null)
					start = LT0();
			} else {
				if (!_cnt1) {
					throw llkParseException("()+ expected at least one occuence");
				}
				break _loop1;
			}
		}
		if (start != null) {
			ILLKToken lt1 = LT0().getNext();
			LT0().setNext(null);
			lt1.setSpecial(start);
			lt0.setNext(lt1);
			llkInput.rewind();
		} else {
			llkInput.unmark();
		}
	}

	private void ss() throws LLKParseException {
		ILLKToken lt0 = LT0();
		llkInput.mark();
		ILLKToken start = null;
		_loop1: while (true) {
			if (LA1() >= SPACES && LA1() <= COMMENT) {
				llkInput.consume();
				if (start == null)
					start = LT0();
			} else {
				break _loop1;
			}
		}
		if (start != null) {
			ILLKToken lt1 = LT0().getNext();
			LT0().setNext(null);
			lt1.setSpecial(start);
			lt0.setNext(lt1);
			llkInput.rewind();
		} else {
			llkInput.unmark();
		}
	}

	private void ssc() throws LLKParseException {
		ILLKToken lt0 = LT0();
		llkInput.mark();
		ILLKToken start = null;
		_loop1: while (true) {
			if (llkGetBit(LA1(), LLKTokenSet0.bitset)) {
				llkInput.consume();
				if (start == null)
					start = LT0();
			} else {
				break _loop1;
			}
		}
		if (start != null) {
			ILLKToken lt1 = LT0().getNext();
			LT0().setNext(null);
			lt1.setSpecial(start);
			lt0.setNext(lt1);
			llkInput.rewind();
		} else {
			llkInput.unmark();
		}
	}

	public void styleSheet(FormatBuilder buf) throws LLKParseException {
		if (LA1() == CHARSET) {
			charset(buf);
		}
		ssc();
		_loop1: while (true) {
			if (LA1() == IMPORT) {
				Import(buf);
				ssc();
			} else {
				break _loop1;
			}
		}
		_loop2: while (true) {
			if (llkGetBit(LA1(), LLKTokenSet1.bitset)) {
				switch (LA1()) {
					case MEDIA:
						media(buf);
						break;
					case PAGE:
						page(buf);
						break;
					case FONT_FACE:
						font_face(buf);
						break;
					case AT_KEYWORD:
						atRule(buf);
						break;
					case KEYFRAMES:
					case WEBKIT_KEYFRAMES:
						keyframes(buf);
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
						ruleset(buf);
						break;
					default:
						throw llkParseException("Unexpected token");
				}
				ssc();
			} else {
				break _loop2;
			}
		}
		llkMatch(_EOF_);
		emitSpecial(buf, LT0(), 0, 1);
		buf.flushLine();
		buf.rtrimBlankLines();
	}

	public void charset(FormatBuilder buf) throws LLKParseException {
		flushSpecial(buf, 1, 2);
		llkMatch(CHARSET);
		emit(buf, 1);
		ss();
		llkMatch(STRING);
		emit(buf, 1);
		ss();
		llkMatch(SEMICOLON);
		emit(buf);
	}

	public void Import(FormatBuilder buf) throws LLKParseException {
		flushSpecial(buf, 1, 2);
		try {
			llkMatch(IMPORT);
			emitSpecial(buf, LT0(), 1);
			emitText(buf);
			ss();
			if (llkInput.matchOpt(STRING)) {
				emit(buf, 1);
			} else if (llkInput.matchOpt(URI)) {
				emit(buf, 1);
			} else {
				throw llkParseException("Unexpected token");
			}
			ss();
			if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
				medium(buf);
				_loop1: while (true) {
					if (llkInput.matchOpt(COMMA)) {
						emit(buf);
						ss();
						medium(buf);
					} else {
						break _loop1;
					}
				}
			}
			llkMatch(SEMICOLON);
			emit(buf);
		} catch (Throwable e) {
			ILLKToken lt0 = LT0();
			ILLKToken last = skipTokens(0);
			warn(MsgId.ParserInvalidConstructIgnored, e, lt0, last);
			emitTokens(buf, lt0, last);
		} finally {
		}
	}

	public void media(FormatBuilder buf) throws LLKParseException {
		ILLKToken t;
		flushSpecial(buf, 1, 2);
		FormatBuilder b = startSession(buf);
		int level = 0;
		try {
			llkMatch(MEDIA);
			emitSpecial(b, LT0(), 1);
			emit(b, 1);
			ss();
			if (llkBitTest(LA1(), ONLY, NOT, LPAREN, IDENTIFIER)) {
				media_query_list(b);
			}
			t = LT1();
			llkMatch(LBRACE);
			++level;
			ss();
			endSession(buf, b);
			startBlock(buf, t);
			_loop1: while (true) {
				if (llkGetBit(LA1(), LLKTokenSet3.bitset)) {
					ruleset(buf);
					ss();
				} else {
					break _loop1;
				}
			}
			t = LT1();
			llkMatch(RBRACE);
			--level;
			if (true) {
				ss();
			}
			endBlock(buf, t);
		} catch (Throwable e) {
			ILLKToken lt0 = LT0();
			ILLKToken last = skipTokens(level);
			warn(MsgId.ParserInvalidConstructIgnored, e, lt0, last);
			emitTokens(buf, lt0, last);
		} finally {
		}
	}

	public void media_query_list(FormatBuilder buf) throws LLKParseException {
		media_query(buf);
		_loop1: while (true) {
			if (llkInput.matchOpt(COMMA)) {
				emit(buf);
				ss();
				media_query(buf);
			} else {
				break _loop1;
			}
		}
	}

	public void media_query(FormatBuilder buf) throws LLKParseException {
		if (llkBitTest(LA1(), ONLY, NOT, IDENTIFIER)) {
			if (LA1() == ONLY || LA1() == NOT) {
				llkInput.consume();
				emit(buf, 1);
				ss();
			}
			media_type(buf);
			ss();
			_loop1: while (true) {
				if (llkInput.matchOpt(AND)) {
					emit(buf, 1);
					media_expr(buf);
				} else {
					break _loop1;
				}
			}
		} else if (LA1() == LPAREN) {
			media_expr(buf);
			_loop2: while (true) {
				if (llkInput.matchOpt(AND)) {
					emit(buf, 1);
					ss();
					media_expr(buf);
				} else {
					break _loop2;
				}
			}
		} else {
			throw llkParseException("Unexpected token");
		}
	}

	public void media_type(FormatBuilder buf) throws LLKParseException {
		llkMatch(IDENTIFIER);
		emit(buf, 1);
	}

	public void media_expr(FormatBuilder buf) throws LLKParseException {
		llkMatch(LPAREN);
		emit(buf, 1);
		ss();
		media_feature(buf);
		ss();
		if (llkInput.matchOpt(COLON)) {
			emit(buf, 1);
			ss();
			expr(buf, 1);
		}
		llkMatch(RPAREN);
		emit(buf, 1);
		ss();
	}

	public void media_feature(FormatBuilder buf) throws LLKParseException {
		llkMatch(IDENTIFIER);
		emit(buf, 1);
	}

	public void medium(FormatBuilder buf) throws LLKParseException {
		llkMatch(LLKTokenSet2.bitset);
		emit(buf, 1);
		ss();
	}

	public void keyframes(FormatBuilder buf) throws LLKParseException {
		ILLKToken t;
		flushSpecial(buf, 1, 2);
		llkMatch(KEYFRAMES, WEBKIT_KEYFRAMES);
		emit(buf, 1);
		ss();
		llkMatch(IDENTIFIER);
		emit(buf, 1);
		ss();
		t = LT1();
		llkMatch(LBRACE);
		ss();
		startBlock(buf, t);
		_loop1: while (true) {
			if (llkGetBit(LA1(), LLKTokenSet4.bitset)) {
				keyframes_block(buf);
			} else {
				break _loop1;
			}
		}
		t = LT1();
		llkMatch(RBRACE);
		ss();
		endBlock(buf, t);
	}

	public void keyframes_block(FormatBuilder buf) throws LLKParseException {
		ILLKToken t;
		keyframe_selector(buf);
		t = LT1();
		llkMatch(LBRACE);
		ss();
		startBlock(buf, t);
		if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
			declaration(buf);
			ss();
		}
		_loop1: while (true) {
			if (llkInput.matchOpt(SEMICOLON)) {
				ss();
				emit(buf);
				emitRestOfLine(buf, LT1());
				if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
					declaration(buf);
					ss();
				}
			} else {
				break _loop1;
			}
		}
		t = LT1();
		llkMatch(RBRACE);
		ss();
		endBlock(buf, t);
	}

	public void keyframe_selector(FormatBuilder buf) throws LLKParseException {
		switch (LA1()) {
			case FROM:
				llkInput.consume();
				emit(buf, 1);
				break;
			case TO:
				llkInput.consume();
				emit(buf, 1);
				break;
			case HEX:
			case DECIMAL:
			case REAL:
			case CALC:
				Number(buf, 1);
				llkMatch(PERCENT);
				emit(buf);
				break;
			default:
				throw llkParseException("Unexpected token");
		}
		ss();
		_loop1: while (true) {
			if (llkInput.matchOpt(COMMA)) {
				emit(buf);
				ss();
				switch (LA1()) {
					case FROM:
						llkInput.consume();
						emit(buf, 1);
						break;
					case TO:
						llkInput.consume();
						emit(buf, 1);
						break;
					case HEX:
					case DECIMAL:
					case REAL:
					case CALC:
						Number(buf, 1);
						llkMatch(PERCENT);
						emit(buf);
						break;
					default:
						throw llkParseException("Unexpected token");
				}
				ss();
			} else {
				break _loop1;
			}
		}
	}

	public void page(FormatBuilder buf) throws LLKParseException {
		ILLKToken t;
		flushSpecial(buf, 1, 2);
		FormatBuilder b = formatOptions.isCompact() ? startSession(buf) : buf;
		int level = 0;
		try {
			llkMatch(PAGE);
			emitSpecial(b, LT0(), 1);
			emit(b, 1);
			ss();
			if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
				llkInput.consume();
				emit(b, 1);
			}
			pseudo_page(b);
			ss();
			t = LT1();
			llkMatch(LBRACE);
			++level;
			ss();
			startBlock(b, t);
			if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
				declaration(b);
				ss();
			}
			_loop1: while (true) {
				if (llkInput.matchOpt(SEMICOLON)) {
					emit(b);
					ss();
					flushSpecial(b, 1, 1);
					b.flushLine();
					if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
						declaration(b);
						ss();
					}
				} else {
					break _loop1;
				}
			}
			t = LT1();
			llkMatch(RBRACE);
			--level;
			if (true) {
				ss();
			}
			endBlock(b, t);
		} catch (Throwable e) {
			ILLKToken lt0 = LT0();
			ILLKToken last = skipTokens(level);
			warn(MsgId.ParserInvalidConstructIgnored, e, lt0, last);
			emitTokens(b, lt0, last);
		} finally {
		}
		if (b != buf)
			endSession(buf, b);
	}

	public void pseudo_page(FormatBuilder buf) throws LLKParseException {
		llkMatch(COLON);
		emit(buf);
		llkMatch(LLKTokenSet2.bitset);
		emit(buf);
	}

	public void font_face(FormatBuilder buf) throws LLKParseException {
		ILLKToken t;
		flushSpecial(buf, 1, 2);
		FormatBuilder b = formatOptions.isCompact() ? startSession(buf) : buf;
		int level = 0;
		try {
			llkMatch(FONT_FACE);
			emitSpecial(b, LT0(), 1);
			emit(b, 1);
			ss();
			t = LT1();
			llkMatch(LBRACE);
			++level;
			ss();
			startBlock(b, t);
			if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
				declaration(b);
				ss();
			}
			_loop1: while (true) {
				if (llkInput.matchOpt(SEMICOLON)) {
					emit(b);
					ss();
					flushSpecial(b, 1, 1);
					b.flushLine();
					if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
						declaration(b);
						ss();
					}
				} else {
					break _loop1;
				}
			}
			t = LT1();
			llkMatch(RBRACE);
			--level;
			if (true) {
				ss();
			}
			endBlock(b, t);
		} catch (Throwable e) {
			ILLKToken lt0 = LT0();
			ILLKToken last = skipTokens(level);
			warn(MsgId.ParserInvalidConstructIgnored, e, lt0, last);
			emitTokens(b, lt0, last);
		} finally {
		}
		if (b != buf)
			endSession(buf, b);
	}

	public void atRule(FormatBuilder buf) throws LLKParseException {
		ILLKToken t = null;
		flushSpecial(buf, 1, 2);
		t = LT1();
		llkMatch(AT_KEYWORD);
		ILLKToken last = skipTokens(0);
		warn(MsgId.ParserInvalidConstructIgnored, null, t, last);
		emitTokens(buf, t, last);
	}

	public void operator(FormatBuilder buf, int level) throws LLKParseException {
		if (LA1() == STAR || LA1() == SLASH) {
			llkInput.consume();
			emit(buf);
			ss();
		} else if (llkInput.matchOpt(COMMA)) {
			ss();
			emit(buf);
			flushSpecial(buf, 0, 1);
			buf.flushLine();
			if (buf.getIndentLevel() == level) buf.indent();
		} else if (llkSynPredict0()) {
			llkInput.consume();
			emit(buf);
			ss();
		}
	}

	public void combinator(FormatBuilder buf) throws LLKParseException {
		if (llkGetBit(LA1(), LLKTokenSet5.bitset)) {
			if (llkBitTest(LA1(), TILDE, COLUMN, PLUS, GT)) {
				llkInput.consume();
				emit(buf, 1);
			} else if (llkInput.matchOpt(SLASH)) {
				emit(buf, 1);
				if ((LA1() == IDENTIFIER) && (LA(2) == SLASH)) {
					llkInput.consume();
					emit(buf);
					llkInput.consume();
					emit(buf);
				}
			} else {
				throw llkParseException("Unexpected token");
			}
			ss();
		}
	}

	public void unary_operator(FormatBuilder buf, int spaces) throws LLKParseException {
		llkMatch(PLUS, DASH);
		emit(buf, spaces);
	}

	public void property(FormatBuilder buf) throws LLKParseException {
		llkMatch(LLKTokenSet2.bitset);
		emit(buf, 1);
		checkPropertyName(LT0());
	}

	public void ruleset(FormatBuilder buf) throws LLKParseException {
		ILLKToken t;
		flushSpecial(buf, 1, 2);
		FormatBuilder b = formatOptions.isCompact() ? startSession(buf) : buf;
		FormatBuilder bb = startSession(b);
		int level = 0;
		try {
			selector(bb, 0);
			_loop1: while (true) {
				if (llkInput.matchOpt(COMMA)) {
					ss();
					emit(bb);
					emitRestOfLine(bb, LT1());
					if (llkGetBit(LA1(), LLKTokenSet3.bitset)) {
						selector(bb, 0);
					}
				} else {
					break _loop1;
				}
			}
			t = LT1();
			llkMatch(LBRACE);
			++level;
			ss();
			endSession(b, bb);
			startBlock(b, t);
			if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
				declaration(b);
				ss();
			}
			_loop2: while (true) {
				if (llkInput.matchOpt(SEMICOLON)) {
					emit(b);
					ss();
					flushSpecial(b, 1, 1);
					b.flushLine();
					if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
						declaration(b);
						ss();
					}
				} else {
					break _loop2;
				}
			}
			t = LT1();
			llkMatch(RBRACE);
			--level;
			if (true) {
				ss();
			}
			endBlock(b, t);
		} catch (Throwable e) {
			ILLKToken lt0 = LT0();
			ILLKToken last = skipTokens(level);
			warn(MsgId.ParserInvalidConstructIgnored, e, lt0, last);
			emitTokens(b, lt0, last);
		} finally {
		}
		if (b != buf)
			endSession(buf, b);
	}

	public void selector(FormatBuilder buf, int spaces) throws LLKParseException {
		simple_selector(buf, spaces);
		_loop1: while (true) {
			if (llkGetBit(LA1(), LLKTokenSet6.bitset)) {
				combinator(buf);
				simple_selector(buf, 1);
			} else {
				break _loop1;
			}
		}
	}

	public void simple_selector(FormatBuilder buf, int spaces) throws LLKParseException {
		switch (LA1()) {
			case HASH:
				llkInput.consume();
				emit(buf, 1);
				_loop1: while (true) {
					switch (LA1()) {
						case HASH:
							llkInput.consume();
							emit(buf);
							break;
						case DOT:
							Class(buf, 0);
							break;
						case COLON:
							pseudo(buf, 0);
							break;
						case LBRACKET:
							attrib(buf, 0);
							break;
						default:
							break _loop1;
					}
				}
				break;
			case COLON:
				pseudo(buf, spaces);
				_loop2: while (true) {
					switch (LA1()) {
						case HASH:
							llkInput.consume();
							emit(buf);
							break;
						case DOT:
							Class(buf, 0);
							break;
						case COLON:
							pseudo(buf, 0);
							break;
						case LBRACKET:
							attrib(buf, 0);
							break;
						default:
							break _loop2;
					}
				}
				break;
			case DOT:
				Class(buf, spaces);
				_loop3: while (true) {
					switch (LA1()) {
						case HASH:
							llkInput.consume();
							emit(buf);
							break;
						case DOT:
							Class(buf, 0);
							break;
						case COLON:
							pseudo(buf, 0);
							break;
						case LBRACKET:
							attrib(buf, 0);
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
				qname(buf, spaces);
				_loop4: while (true) {
					switch (LA1()) {
						case HASH:
							llkInput.consume();
							emit(buf);
							break;
						case DOT:
							Class(buf, 0);
							break;
						case COLON:
							pseudo(buf, 0);
							break;
						case LBRACKET:
							attrib(buf, 0);
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
	}

	public void Class(FormatBuilder buf, int spaces) throws LLKParseException {
		llkMatch(DOT);
		emit(buf, spaces);
		llkMatch(LLKTokenSet2.bitset);
		emit(buf);
	}

	public void qname(FormatBuilder buf, int spaces) throws LLKParseException {
		llkMatch(LLKTokenSet7.bitset);
		emit(buf, spaces);
		if (llkInput.matchOpt(OR)) {
			emit(buf);
			llkMatch(LLKTokenSet7.bitset);
			emit(buf);
		}
	}

	public void attrib(FormatBuilder buf, int spaces) throws LLKParseException {
		llkMatch(LBRACKET);
		emit(buf, spaces);
		ss();
		llkMatch(LLKTokenSet2.bitset);
		emit(buf);
		ss();
		if (llkGetBit(LA1(), LLKTokenSet8.bitset)) {
			llkInput.consume();
			emit(buf);
			ss();
			llkMatch(LLKTokenSet9.bitset);
			emit(buf);
			ss();
		}
		llkMatch(RBRACKET);
		emit(buf);
	}

	public void pseudo(FormatBuilder buf, int spaces) throws LLKParseException {
		llkMatch(COLON);
		emit(buf, spaces);
		if ((LA1() == COLON) && (true)) {
			llkInput.consume();
			emit(buf);
		}
		if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
			llkInput.consume();
			emit(buf);
		} else if (llkInput.matchOpt(FUNCTION)) {
			emit(buf);
			ss();
			if (llkGetBit(LA1(), LLKTokenSet3.bitset)) {
				selector(buf, 0);
				ss();
				_loop1: while (true) {
					if (llkInput.matchOpt(COMMA)) {
						emit(buf);
						ss();
						selector(buf, 1);
						ss();
					} else {
						break _loop1;
					}
				}
			} else if (LA1() == LBRACKET) {
				attrib(buf, 0);
				ss();
			} else {
				throw llkParseException("Unexpected token");
			}
			llkMatch(RPAREN);
			emit(buf);
		}
	}

	public void declaration(FormatBuilder buf) throws LLKParseException {
		try {
			property(buf);
			ss();
			llkMatch(COLON);
			emit(buf);
			ss();
			if (llkGetBit(LA1(), LLKTokenSet10.bitset)) {
				expr(buf, 1);
			}
			if (LA1() == EXCLAIMATION) {
				prio(buf);
			}
		} catch (Exception e) {
			ILLKToken lt0 = LT0();
			ILLKToken last = skipTo(new int[] { SEMICOLON, RBRACE });
			warn(MsgId.ParserInvalidConstructIgnored, e, lt0, last);
			emitTokens(buf, lt0, last);
		} finally {
		}
	}

	public void prio(FormatBuilder buf) throws LLKParseException {
		llkMatch(EXCLAIMATION);
		emit(buf, 1);
		ss();
		llkMatch(IMPORTANT);
		emit(buf);
		ss();
	}

	public void expr(FormatBuilder buf, int spaces) throws LLKParseException {
		int level = buf.getIndentLevel();
		FormatBuilder b = startSession(buf);
		term(b, spaces);
		_loop1: while (true) {
			if (llkGetBit(LA1(), LLKTokenSet11.bitset)) {
				operator(b, level);
				term(b, 1);
			} else {
				break _loop1;
			}
		}
		endSession(buf, b);
	}

	public void term(FormatBuilder buf, int spaces) throws LLKParseException {
		switch (LA1()) {
			case PLUS:
			case DASH:
				unary_operator(buf, spaces);
				units(buf, 0);
				break;
			case URI:
			case STRING:
			case UNICODE_RANGE:
				llkInput.consume();
				emit(buf, spaces);
				break;
			case RGB:
			case RGBA:
			case HASH:
				color(buf, spaces);
				break;
			case LINEAR_GRADIENT:
			case RADIAL_GRADIENT:
			case REPEATING_LINEAR_GRADIENT:
			case REPEATING_RADIAL_GRADIENT:
				gradient(buf, spaces);
				break;
			case HEX:
			case DECIMAL:
			case REAL:
			case VAR:
			case CALC:
			case ATTR:
			case FUNCTION:
				units(buf, spaces);
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
				llkInput.consume();
				emit(buf, spaces);
				break;
			default:
				throw llkParseException("Unexpected token");
		}
		ss();
	}

	private void units(FormatBuilder buf, int spaces) throws LLKParseException {
		switch (LA1()) {
			case VAR:
				varref(buf, spaces);
				break;
			case CALC:
				calc(buf, spaces);
				break;
			case ATTR:
				attr(buf, spaces);
				break;
			case FUNCTION:
				function(buf, spaces);
				break;
			case HEX:
			case DECIMAL:
			case REAL:
				llkInput.consume();
				emit(buf, spaces);
				if (llkGetBit(LA1(), LLKTokenSet12.bitset)) {
					llkInput.consume();
					emit(buf);
				}
				break;
			default:
				throw llkParseException("Unexpected token");
		}
	}

	private void varref(FormatBuilder buf, int spaces) throws LLKParseException {
		llkMatch(VAR);
		emit(buf, spaces);
		ss();
		llkMatch(DASHDASH);
		emit(buf);
		llkMatch(IDENTIFIER);
		emit(buf);
		ss();
		llkMatch(RPAREN);
		emit(buf);
	}

	public void color(FormatBuilder buf, int spaces) throws LLKParseException {
		switch (LA1()) {
			case HASH:
				hexcolor(buf, spaces);
				break;
			case RGB:
				llkInput.consume();
				emit(buf, spaces);
				ss();
				Number(buf, 0);
				ss();
				llkMatch(COMMA);
				emit(buf);
				ss();
				Number(buf, 1);
				ss();
				llkMatch(COMMA);
				emit(buf);
				ss();
				Number(buf, 1);
				ss();
				if (llkInput.matchOpt(COMMA)) {
					emit(buf);
					ss();
				}
				llkMatch(RPAREN);
				emit(buf);
				break;
			case RGBA:
				llkInput.consume();
				emit(buf, spaces);
				ss();
				Number(buf, 0);
				ss();
				llkMatch(COMMA);
				emit(buf);
				ss();
				Number(buf, 1);
				ss();
				llkMatch(COMMA);
				emit(buf);
				ss();
				Number(buf, 1);
				ss();
				llkMatch(COMMA);
				emit(buf);
				ss();
				Number(buf, 1);
				ss();
				if (llkInput.matchOpt(COMMA)) {
					emit(buf);
					ss();
				}
				llkMatch(RPAREN);
				emit(buf);
				break;
			default:
				throw llkParseException("Unexpected token");
		}
	}

	private void gradient(FormatBuilder buf, int spaces) throws LLKParseException {
		if (LA1() == LINEAR_GRADIENT || LA1() == REPEATING_LINEAR_GRADIENT) {
			linear_gradient(buf, spaces);
		} else if (LA1() == RADIAL_GRADIENT || LA1() == REPEATING_RADIAL_GRADIENT) {
			radial_gradient(buf, spaces);
		} else {
			throw llkParseException("Unexpected token");
		}
	}

	public void linear_gradient(FormatBuilder buf, int spaces) throws LLKParseException {
		llkMatch(LINEAR_GRADIENT, REPEATING_LINEAR_GRADIENT);
		emit(buf, spaces);
		ss();
		if (llkBitTest(LA1(), TO, HEX, DECIMAL, REAL)) {
			if (LA1() >= HEX && LA1() <= REAL) {
				llkInput.consume();
				emit(buf);
				llkMatch(ANGLE);
				emit(buf);
			} else if (llkInput.matchOpt(TO)) {
				emit(buf);
				ss();
				side_or_corner(buf);
			} else {
				throw llkParseException("Unexpected token");
			}
			ss();
			llkMatch(COMMA);
			emit(buf);
			ss();
		}
		color_stop(buf, lt0Type(LINEAR_GRADIENT, REPEATING_LINEAR_GRADIENT) ? 0 : 1);
		_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
			if (llkInput.matchOpt(COMMA)) {
				emit(buf);
				ss();
				color_stop(buf, 1);
			} else {
				if (!_cnt1) {
					throw llkParseException("()+ expected at least one occuence");
				}
				break _loop1;
			}
		}
		llkMatch(RPAREN);
		emit(buf);
	}

	public void radial_gradient(FormatBuilder buf, int spaces) throws LLKParseException {
		llkMatch(RADIAL_GRADIENT, REPEATING_RADIAL_GRADIENT);
		emit(buf, spaces);
		ss();
		if (llkGetBit(LA1(), LLKTokenSet13.bitset)) {
			if (llkInput.matchOpt(SHAPE)) {
				emit(buf);
				ss();
				if (llkGetBit(LA1(), LLKTokenSet14.bitset)) {
					if (llkGetBit(LA1(), LLKTokenSet15.bitset)) {
						length_or_percent(buf, 1);
					} else if (llkInput.matchOpt(EXTENT_KEYWORD)) {
						emit(buf, 1);
					} else {
						throw llkParseException("Unexpected token");
					}
					ss();
					if (llkGetBit(LA1(), LLKTokenSet15.bitset)) {
						length_or_percent(buf, 1);
						ss();
					}
				}
			} else if (llkGetBit(LA1(), LLKTokenSet14.bitset)) {
				if (llkGetBit(LA1(), LLKTokenSet15.bitset)) {
					length_or_percent(buf, 0);
				} else if (llkInput.matchOpt(EXTENT_KEYWORD)) {
					emit(buf, 0);
				} else {
					throw llkParseException("Unexpected token");
				}
				ss();
				if (llkGetBit(LA1(), LLKTokenSet15.bitset)) {
					length_or_percent(buf, 1);
					ss();
				}
				if (llkInput.matchOpt(SHAPE)) {
					emit(buf, 1);
					ss();
				}
			}
			if (llkInput.matchOpt(AT)) {
				emit(buf, lt0Type(RADIAL_GRADIENT, REPEATING_RADIAL_GRADIENT) ? 0 : 1);
				ss();
				position(buf, 1);
				ss();
			}
			llkMatch(COMMA);
			emit(buf);
			ss();
		}
		color_stop(buf, lt0Type(RADIAL_GRADIENT, REPEATING_RADIAL_GRADIENT) ? 0 : 1);
		_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
			if (llkInput.matchOpt(COMMA)) {
				emit(buf);
				ss();
				color_stop(buf, 1);
			} else {
				if (!_cnt1) {
					throw llkParseException("()+ expected at least one occuence");
				}
				break _loop1;
			}
		}
		llkMatch(RPAREN);
		emit(buf);
	}

	public void side_or_corner(FormatBuilder buf) throws LLKParseException {
		llkMatch(SIDE);
		emit(buf, 1);
		if (llkInput.matchOpt(SIDE)) {
			emit(buf, 1);
		}
	}

	public void position(FormatBuilder buf, int spaces) throws LLKParseException {
		position1(buf, spaces);
		if (llkSynPredict1()) {
			s();
			position1(buf, 1);
		}
	}

	public void color_stop(FormatBuilder buf, int spaces) throws LLKParseException {
		if (llkInput.matchOpt(IDENTIFIER)) {
			emit(buf, spaces);
		} else if (llkBitTest(LA1(), RGB, RGBA, HASH)) {
			color(buf, spaces);
		} else {
			throw llkParseException("Unexpected token");
		}
		ss();
		if (llkGetBit(LA1(), LLKTokenSet15.bitset)) {
			length_or_percent(buf, 1);
			ss();
		}
	}

	private void position1(FormatBuilder buf, int spaces) throws LLKParseException {
		if (LA1() == CENTER || LA1() == SIDE) {
			llkInput.consume();
			emit(buf, spaces);
		} else if (llkGetBit(LA1(), LLKTokenSet15.bitset)) {
			length_or_percent(buf, spaces);
		} else {
			throw llkParseException("Unexpected token");
		}
	}

	private void length_or_percent(FormatBuilder buf, int spaces) throws LLKParseException {
		if (llkGetBit(LA1(), LLKTokenSet16.bitset)) {
			if (LA1() == PLUS || LA1() == DASH) {
				llkInput.consume();
				emit(buf, spaces); spaces = 0;
			}
			llkMatch(HEX, DECIMAL, REAL);
			emit(buf, spaces);
			llkMatch(LENGTH, PERCENT);
			emit(buf);
		} else if (LA1() == CALC) {
			calc(buf, spaces);
		} else {
			throw llkParseException("Unexpected token");
		}
	}

	public void calc(FormatBuilder buf, int spaces) throws LLKParseException {
		llkMatch(CALC);
		emit(buf, spaces);
		ss();
		sum(buf, 0);
		ss();
		llkMatch(RPAREN);
		emit(buf);
	}

	public void sum(FormatBuilder buf, int spaces) throws LLKParseException {
		product(buf, spaces);
		_loop1: while (true) {
			if (llkSynPredict2()) {
				s();
				llkMatch(PLUS, DASH);
				emit(buf, 1);
				s();
				product(buf, 1);
			} else {
				break _loop1;
			}
		}
	}

	public void product(FormatBuilder buf, int spaces) throws LLKParseException {
		unit(buf, spaces);
		_loop1: while (true) {
			if (llkSynPredict3()) {
				ss();
				if (llkInput.matchOpt(STAR)) {
					emit(buf, 1);
					ss();
					unit(buf, 1);
				} else if (llkInput.matchOpt(SLASH)) {
					emit(buf, 1);
					ss();
					Number(buf, 1);
				} else {
					throw llkParseException("Unexpected token");
				}
			} else {
				break _loop1;
			}
		}
	}

	public void attr(FormatBuilder buf, int spaces) throws LLKParseException {
		llkMatch(ATTR);
		emit(buf, spaces);
		ss();
		qname(buf, 0);
		if (LA1() >= SPACES && LA1() <= COMMENT) {
			s();
			if (llkGetBit(LA1(), LLKTokenSet2.bitset)) {
				llkInput.consume();
				emit(buf, 1);
				ss();
			}
		}
		if (llkInput.matchOpt(COMMA)) {
			emit(buf);
			ss();
			unit(buf, 1);
			ss();
		}
		llkMatch(RPAREN);
		emit(buf);
	}

	public void unit(FormatBuilder buf, int spaces) throws LLKParseException {
		switch (LA1()) {
			case BUILTIN:
				llkInput.consume();
				emit(buf, spaces);
				break;
			case VAR:
				varref(buf, spaces);
				break;
			case CALC:
				calc(buf, spaces);
				break;
			case ATTR:
				attr(buf, spaces);
				break;
			case LPAREN:
				llkInput.consume();
				emit(buf, spaces);
				ss();
				sum(buf, 0);
				ss();
				llkMatch(RPAREN);
				emit(buf);
				break;
			case HEX:
			case DECIMAL:
			case REAL:
				llkInput.consume();
				emit(buf, spaces);
				if (llkGetBit(LA1(), LLKTokenSet18.bitset)) {
					llkInput.consume();
					emit(buf);
				}
				break;
			default:
				throw llkParseException("Unexpected token");
		}
	}

	public void function(FormatBuilder buf, int spaces) throws LLKParseException {
		llkMatch(FUNCTION);
		emit(buf, spaces);
		if (llkGetBit(LA1(), LLKTokenSet10.bitset)) {
			expr(buf, 0);
		}
		llkMatch(RPAREN);
		emit(buf);
	}

	public void hexcolor(FormatBuilder buf, int spaces) throws LLKParseException {
		llkMatch(HASH);
		emit(buf, spaces);
	}

	private void Number(FormatBuilder buf, int spaces) throws LLKParseException {
		if (LA1() >= HEX && LA1() <= REAL) {
			llkInput.consume();
			emit(buf, spaces);
		} else if (LA1() == CALC) {
			calc(buf, spaces);
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
			0x20007800, 
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
			0x00000000, 0x81c01000, 
		}
	);
	static final LLKTokenSet LLKTokenSet17 = new LLKTokenSet(
		false,
		0,
		2,
		new int[] {
			0x00000000, 0x78000800, 
		}
	);
	static final LLKTokenSet LLKTokenSet18 = new LLKTokenSet(
		false,
		0,
		1,
		new int[] {
			0x2000f800, 
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
	
	public ILLKTreeBuilder llkGetTreeBuilder() {
		return null;
	}
	
	public void llkSetTreeBuilder(ILLKTreeBuilder builder) {
	}
	
	///////////////////////////////////////////////////////////////////////
	
	protected ILLKMain llkMain;
	protected ILLKParserInput llkInput;
	protected final List<ILLKLifeCycleListener> llkLifeCycleListeners = new ArrayList<>(4);
	
	///////////////////////////////////////////////////////////////////////
	
	public ILLKMain llkGetMain() {
	    return llkMain;
	}
	
	public ILLKParserInput llkGetInput() {
	    return llkInput;
	}
	
	public void llkAddLifeCycleListener(final ILLKLifeCycleListener l) {
	    llkLifeCycleListeners.add(l);
	}
	
	public void llkRemoveLifeCycleListener(final ILLKLifeCycleListener l) {
	    llkLifeCycleListeners.remove(l);
	}
	
	///////////////////////////////////////////////////////////////////////
	
	public final int LA0() {
	    return llkInput.LA0();
	}
	
	public final int LA1() {
	    return llkInput.LA1();
	}
	
	public final int LA(final int n) {
	    return llkInput.LA(n);
	}
	
	public final ILLKToken LT0() {
	    return llkInput.LT0();
	}
	
	public final ILLKToken LT1() {
	    return llkInput.LT1();
	}
	
	public final ILLKToken LT(final int n) {
	    return llkInput.LT(n);
	}
	
	public final void llkConsume() {
	    llkInput.consume();
	}
	
	public final void llkConsume(final int n) {
	    llkInput.consume(n);
	}
	
	/**
	 * Consume chars until one matches the given char
	 */
	public final void llkConsumeUntil(final int c) throws LLKParseError {
	    int la1 = LA1();
	    if (la1 != ILLKParserInput.EOF) {
	        llkInput.consume();
	        while ((la1 = LA1()) != c && la1 != ILLKParserInput.EOF) {
	            llkInput.consume();
	        }
	    }
	}
	
	/**
	 * Consume chars until one matches the given token type.
	 */
	public final void llkConsumeUntil(final int[] a) throws LLKParseError {
	    int la1 = LA1();
	    if (la1 != ILLKParserInput.EOF) {
	        llkInput.consume();
	        while ((la1 = LA1()) != ILLKParserInput.EOF) {
	            for (int i = 0; i < a.length; ++i) {
	                if (la1 == a[i]) {
	                    return;
	                }
	            }
	            llkInput.consume();
	        }
	    }
	}
	
	/**
	 * Consume chars until one matches the given token type.
	 */
	public final void llkConsumeUntil(final int[] bitset, final boolean inverted) throws LLKParseError {
	    int la1 = LA1();
	    if (la1 != ILLKParserInput.EOF) {
	        llkInput.consume();
	        while ((la1 = LA1()) != ILLKParserInput.EOF) {
	            if (inverted ? llkGetBitInverted(la1, bitset) : llkGetBit(la1, bitset)) {
	                return;
	            }
	            llkInput.consume();
	        }
	    }
	}
	
	public ISourceLocation llkGetLocation(final int offset) {
	    return llkInput.getLocator().getLocation(offset);
	}
	
	////////////////////////////////////////////////////////////
	
	public final void getKeywordContexts() {
	    llkInput.getKeywordContexts();
	}
	
	public final void setKeywordContexts(final int context) {
	    llkInput.setKeywordContexts(context);
	}
	
	public final void llkSetContext(final int context, final int state) {
	    llkInput.setContext(context, state);
	}
	
	public CharSequence llkGetSource() {
	    return llkInput.getSource();
	}
	
	public CharSequence llkGetSource(final ILLKToken t) {
	    return llkInput.getSource(t.getOffset(), t.getEndOffset());
	}
	
	public CharSequence llkGetSource(final int start, final int end) {
	    return llkInput.getSource(start, end);
	}
	
	////////////////////////////////////////////////////////////
	
	protected LLKParseException llkParseException(final String msg) {
	    return llkParseException(msg, null, LT1());
	}
	
	protected LLKParseException llkParseException(final String msg, final Throwable e, final ILLKToken t) {
	    final int type = t.getType();
	    final int offset = t.getOffset();
	    return new LLKParseException(
	        msg
	            + ": "
	            + t.getClass().getName()
	            + "(type="
	            + type
	            + ", name="
	            + llkGetTokenName(type)
	            + ", start="
	            + offset
	            + ", text="
	            + t.getText(),
	        e,
	        llkGetLocation(offset)
	    );
	}
	
	protected LLKParseException llkMismatchException(final String msg, final int[] bset, final int actual) {
	    return new LLKParseException(
	        msg + llkToString(bset) + ", actual=" + llkToString(actual), llkGetLocation(LT1().getOffset()));
	}
	
	protected LLKParseException llkMismatchException(final String msg, final int expected, final int actual) {
	    return new LLKParseException(
	        msg + llkToString(expected) + ", actual=" + llkToString(actual),
	        llkGetLocation(LT1().getOffset()));
	}
	
	protected LLKParseException llkMismatchException(
	    final String msg, final int c1, final int c2, final int actual) {
	    return new LLKParseException(
	        msg + "(" + llkToString(c1) + ", " + llkToString(c2) + "), actual=" + llkToString(actual),
	        llkGetLocation(LT1().getOffset()));
	}
	
	protected LLKParseException llkMismatchException(
	    final String msg, final int c1, final int c2, final int c3, final int actual) {
	    return new LLKParseException(
	        msg
	            + "("
	            + llkToString(c1)
	            + ", "
	            + llkToString(c2)
	            + ", "
	            + llkToString(c3)
	            + "), actual="
	            + llkToString(actual),
	        llkGetLocation(LT1().getOffset())
	    );
	}
	
	////////////////////////////////////////////////////////////
	
	protected final String llkToString(final int type) {
	    return llkGetTokenName(type);
	}
	
	protected final String llkToString(final int[] bset) {
	    final StringBuilder buf = new StringBuilder();
	    for (int i = 0; i < bset.length; ++i) {
	        if (i != 0) {
	            buf.append(", 0x");
	        } else {
	            buf.append("0x");
	        }
	        buf.append(Integer.toHexString(bset[i]));
	    }
	    return buf.toString();
	}
	
	protected final void llkMatch(final int type) throws LLKParseException, LLKParseError {
	    if (LA1() != type) {
	        throw llkMismatchException("match(int): expected=", type, LA1());
	    }
	    llkInput.consume();
	}
	
	protected final void llkMatchNot(final int type) throws LLKParseException, LLKParseError {
	    if (LA1() == type) {
	        throw llkMismatchException("matchNot(int): not expected=", type, LA1());
	    }
	    llkInput.consume();
	}
	
	protected final void llkMatch(final int type1, final int type2) throws LLKParseException, LLKParseError {
	    final int la1 = LA1();
	    if (la1 != type1 && la1 != type2) {
	        throw llkMismatchException("match(int, int): expected=", type1, type2, la1);
	    }
	    llkInput.consume();
	}
	
	protected final void llkMatchNot(final int type1, final int type2) throws LLKParseException, LLKParseError {
	    final int la1 = LA1();
	    if (la1 == type1 || la1 == type2) {
	        throw llkMismatchException("matchNot(int, int): not expected=", type1, type2, LA1());
	    }
	    llkInput.consume();
	}
	
	protected final void llkMatch(final int type1, final int type2, final int type3)
	    throws LLKParseException, LLKParseError {
	    final int la1 = LA1();
	    if (la1 != type1 && la1 != type2 && la1 != type3) {
	        throw llkMismatchException("match(int, int, int): expected=", type1, type2, type3, la1);
	    }
	    llkInput.consume();
	}
	
	protected final void llkMatchNot(final int type1, final int type2, final int type3)
	    throws LLKParseException, LLKParseError {
	    final int la1 = LA1();
	    if (la1 == type1 || la1 == type2 || la1 == type3) {
	        throw llkMismatchException(
	            "matchNot(int, int, int): not expected=", type1, type2, type3, LA1());
	    }
	    llkInput.consume();
	}
	
	protected final void llkMatchRange(final int first, final int last) throws LLKParseException, LLKParseError {
	    final int la1 = LA1();
	    if (la1 < first || la1 > last) {
	        throw llkMismatchException("matchRange(int, int): range=", first, last, la1);
	    }
	    llkInput.consume();
	}
	
	protected final void llkMatchNotRange(final int first, final int last) throws LLKParseException, LLKParseError {
	    final int la1 = LA1();
	    if (la1 >= first && la1 <= last) {
	        throw llkMismatchException("matchNotRange(int, int): range=", first, last, la1);
	    }
	    llkInput.consume();
	}
	
	protected final void llkMatch(final int[] bset) throws LLKParseException, LLKParseError {
	    if (llkGetBit(LA1(), bset)) {
	        llkInput.consume();
	    } else {
	        throw llkMismatchException("match(BitSet): expected=", bset, LA1());
	    }
	}
	
	protected final void llkMatchNot(final int[] bset) throws LLKParseException, LLKParseError {
	    if (llkGetBitInverted(LA1(), bset)) {
	        llkInput.consume();
	    } else {
	        throw llkMismatchException("match(BitSet): expected=", bset, LA1());
	    }
	}
	
	///////////////////////////////////////////////////////////////////////
	
	protected final boolean llkSynMark() {
	    llkInput.mark();
	    return true;
	}
	
	protected final boolean llkSynRewind(final boolean b) {
	    llkInput.rewind();
	    return b;
	}
	
	protected final boolean llkSynRewind(final boolean mark, final boolean b) {
	    llkInput.rewind();
	    return b;
	}
	
	protected final boolean llkSynMatchOrRewind(final boolean b) {
	    if (!b) {
	        llkInput.rewind();
	    } else {
	        llkInput.unmark();
	    }
	    return b;
	}
	
	protected final boolean llkSynMatch(final int type) {
	    if (LA1() != type) {
	        return false;
	    }
	    llkInput.consume();
	    return true;
	}
	
	protected final boolean llkSynMatchNot(final int type) {
	    if (LA1() == type) {
	        return false;
	    }
	    llkInput.consume();
	    return true;
	}
	
	protected final boolean llkSynMatch(final int type1, final int type2) {
	    final int la1 = LA1();
	    if (la1 != type1 && la1 != type2) {
	        return false;
	    }
	    llkInput.consume();
	    return true;
	}
	
	protected final boolean llkSynMatchNot(final int type1, final int type2) {
	    final int la1 = LA1();
	    if (la1 == type1 || la1 == type2) {
	        return false;
	    }
	    llkInput.consume();
	    return true;
	}
	
	protected final boolean llkSynMatch(final int type1, final int type2, final int type3) {
	    final int la1 = LA1();
	    if (la1 != type1 && la1 != type2 && la1 != type3) {
	        return false;
	    }
	    llkInput.consume();
	    return true;
	}
	
	protected final boolean llkSynMatchNot(final int type1, final int type2, final int type3) {
	    final int la1 = LA1();
	    if (la1 == type1 || la1 == type2 || la1 == type3) {
	        return false;
	    }
	    llkInput.consume();
	    return true;
	}
	
	protected final boolean llkSynMatchRange(final int first, final int last) {
	    final int c = LA1();
	    if (c < first || c > last) {
	        return false;
	    }
	    llkInput.consume();
	    return true;
	}
	
	protected final boolean llkSynMatchNotRange(final int first, final int last) {
	    final int c = LA1();
	    if (c >= first || c <= last) {
	        return false;
	    }
	    llkInput.consume();
	    return true;
	}
	
	protected final boolean llkSynMatch(final int[] bset) {
	    if (llkGetBit(LA1(), bset)) {
	        llkInput.consume();
	        return true;
	    }
	    return false;
	}
	
	protected final boolean llkSynMatchNot(final int[] bset) {
	    if (llkGetBitInverted(LA1(), bset)) {
	        llkInput.consume();
	        return true;
	    }
	    return false;
	}
	
	////////////////////////////////////////////////////////////
	
	protected static boolean llkBitTest(final int lak, final int c1, final int c2, final int c3) {
	    return (lak == c1 || lak == c2 || lak == c3);
	}
	
	protected static boolean llkBitTest(
	    final int lak, final int c1, final int c2, final int c3, final int c4) {
	    return (lak == c1 || lak == c2 || lak == c3 || lak == c4);
	}
	
	protected static boolean llkInvertedBitTest(final int lak, final int size, final int c1) {
	    return (lak >= 0 && lak < size && lak != c1);
	}
	
	protected static boolean llkInvertedBitTest(final int lak, final int size, final int c1, final int c2) {
	    return (lak >= 0 && lak < size && lak != c1 && lak != c2);
	}
	
	protected static boolean llkInvertedBitTest(
	    final int lak, final int size, final int c1, final int c2, final int c3) {
	    return (lak >= 0 && lak < size && lak != c1 && lak != c2 && lak != c3);
	}
	
	protected static boolean llkInvertedBitTest(
	    final int lak, final int size, final int c1, final int c2, final int c3, final int c4) {
	    return (lak >= 0 && lak < size && lak != c1 && lak != c2 && lak != c3 && lak != c4);
	}
	
	protected static boolean llkGetBit(int n, final int[] bset) {
	    final int mask = (n & ILLKConstants.MODMASK);
	    return n >= 0 && (n >>= ILLKConstants.LOGBITS) < bset.length && (bset[n] & (1 << mask)) != 0;
	}
	
	protected static boolean llkGetBitInverted(int n, final int[] bset, final int vocab_size) {
	    final int mask = (n & ILLKConstants.MODMASK);
	    return n >= 0
	        && n < vocab_size
	        && ((n >>= ILLKConstants.LOGBITS) >= bset.length || (bset[n] & (1 << mask)) == 0);
	}
	
	////////////////////////////////////////////////////////////
	
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
