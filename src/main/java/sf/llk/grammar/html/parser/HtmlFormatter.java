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

import java.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import sf.llk.grammar.html.HtmlTag;
import sf.llk.grammar.html.parser.HtmlFormatOptions.Style;
import sf.llk.share.formatter.FormatBuilder;
import sf.llk.share.support.ILLKConstants;
import sf.llk.share.support.ILLKLifeCycleListener;
import sf.llk.share.support.ILLKMain;
import sf.llk.share.support.ILLKNode;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.support.ILLKTreeBuilder;
import sf.llk.share.support.ILLKTreeParserInput;
import sf.llk.share.support.ISourceLocation;
import sf.llk.share.support.LLKParseError;
import sf.llk.share.support.LLKParseException;
import sf.llk.share.support.LLKTreeParserInput;

public class HtmlFormatter extends HtmlFormatterBase {

	public static CharSequence format(char[] source, ILLKMain main, HtmlFormatOptions formatoptions)
		throws LLKParseException {
		HtmlLexer lexer = new HtmlLexer(source, main);
		HtmlDomParser parser = new HtmlDomParser(lexer);
		ASTdocument doc = parser.document();
		HtmlFormatter formatter = new HtmlFormatter(doc, main, formatoptions);
		FormatBuilder buf = new FormatBuilder(
			formatoptions.lineWidth,
			formatoptions.tabWidth,
			"",
			formatoptions.tabString,
			formatoptions.lineBreak);
		try {
			formatter.document(buf);
		} catch (Throwable e) {
			ILLKNode n = formatter.LT1();
			if (n != null)
				throw new LLKParseException(
					"Format error", e, main.getLocator().getLocation(n.getOffset()));
			throw new LLKParseException("Format error", e);
		}
		buf.flush();
		return buf.getFormatted();
	}

	public HtmlFormatter(ILLKNode root, ILLKMain main, HtmlFormatOptions options) {
		this(new LLKTreeParserInput(main));
		init(root.getOffset(), root.getEndOffset(), options);
		llkParent = root;
		LT1 = root.getFirst();
	}

	public ILLKMain getMain() {
		return llkMain;
	}

	final ILLKToken followParent() {
		return llkParent.getLastToken().getNext();
	}

	final ILLKToken followLT1() {
		return LT1.getLastToken().getNext();
	}

	final ILLKToken lt1First() {
		return (LT1 != null ? LT1.getFirstToken() : followParent());
	}

	final int lt1Offset() {
		return (LT1 != null ? LT1.getOffset() : llkParent.getEndOffset());
	}

	boolean notEndTag() {
		int n = 1;
		int type = LA1();
		while (type == ASTtext)
			type = LA(++n);
		return (type != ASTendTag);
	}

	public static final boolean LLK_OPTION_VERIFY = false;
	public static final int LLK_INPUT_VOCAB_SIZE = 70;

	private HtmlFormatter(ILLKTreeParserInput input) {
		llkInput = input;
		llkMain = input.getMain();
	}

	////////////////////////////////////////////////////////////

	public final void llkPush() {
		llkStack.push(llkParent);
		llkParent = LT1;
		LT1 = LT1.getFirst();
	}

	public final void llkPop() {
		LT1 = llkParent.getNext();
		llkParent = llkStack.pop();
	}

	public void document(FormatBuilder buf) throws LLKParseException {
		boolean wasbreak = false;
		if (LA1() == ASTtext) {
			wasbreak = text(buf, Mode.INLINE, true, false);
		}
		_loop1: while (true) {
			if (llkGetBit(LA1(), LLKTokenSet0.bitset)) {
				element(buf, wasbreak, Mode.INLINE);
				if (LA1() == ASTtext) {
					wasbreak = text(buf, Mode.INLINE, false, false);
				}
			} else {
				break _loop1;
			}
		}
		wrap(buf, CONTEXT_TEXT, false);
		ILLKToken t = llkParent.getLastToken();
		emit(buf, t, 0, 0);
	}

	public Mode element(FormatBuilder buf, boolean wasbreak, Mode mode) throws LLKParseException {
		switch (LA1()) {
			case ASTstartTag:
				llkPush();
				mode = startTag(buf, mode);
				llkPop();
				break;
			case ASTcomment:
				mode = comment(buf, wasbreak, mode);
				break;
			case ASTextraEndTag:
				extraEndTag(buf, mode);
				break;
			case ASTpi:
				mode = pi(buf, wasbreak, mode);
				break;
			case ASTasp:
				mode = asp(buf, wasbreak, mode);
				break;
			case ASTjste:
				mode = jste(buf, wasbreak, mode);
				break;
			case ASTcdata:
				cdata(buf, mode);
				break;
			case ASTcond:
				cond(buf, mode);
				break;
			case ASTdeclaration:
				mode = declaration(buf, wasbreak, mode);
				break;
			case ASTdoctype:
				llkPush();
				doctype(buf);
				llkPop();
				break;
			default:
				throw llkParseException("Unexpected token", LT1);
		}
		return mode;
	}

	public Mode startTag(FormatBuilder buf, Mode mode) throws LLKParseException {
		ASTstartTag stag = ((ASTstartTag)llkParent);
		HtmlTag sinfo = stag.getTag();
		Style style = formatOptions.getStyle(sinfo);
		boolean empty = stag.isEmpty();
		boolean noindent = formatOptions.isNoIndentTag(stag.getName().toString());
		boolean reformat = (mode != Mode.PRE) && !empty && (style == Style.BLOCK || style == Style.BLOCK1);
		boolean oneliner = reformat;
		Mode cmode = Mode.BLOCK;
		if (mode == Mode.PRE || style == Style.PREFORMATTED)
			cmode = Mode.PRE;
		else
			cmode = Mode.INLINE;
		boolean indent = (cmode != Mode.PRE) && !empty && style.breakContent();
		FormatBuilder b = buf;
		ASTattributes attrs = stag.getAttributes();
		ILLKToken t = stag.getFirstToken();
		if (mode == Mode.PRE) {
			t = emitPre(b, 2, t);
			if (attrs != null && attrs.hasChildren()) {
				for (ILLKNode attr: attrs.children())
					emitPre(b, attr);
			}
			emitPre(b, t);
		} else {
			convertTagCase(stag);
			if (style.breakBefore()) {
				if (mode == Mode.INLINE)
					wrap(buf, CONTEXT_TEXT, false);
				else
					emitRestOfLine(buf, t, false);
			} else if (mode == Mode.INLINE && cmode != Mode.INLINE) {
				wrap(buf, CONTEXT_TEXT, false);
			}
			if (reformat)
				b = cloneBuf(buf);
			if (style == Style.PREFORMATTED)
				b.setTempIndentWidth(0);
			if (attrs != null && attrs.hasChildren()) {
				boolean inline = (style == Style.INLINE || style == Style.PARAGRAPH);
				oneliner &= emitAttributes(b, t, attrs, inline);
				t = t.getNext().getNext();
			} else {
				t = emitn(b, 2, t, 0, 1);
			}
			if (t.getType() == ENDEMPTY && attrs != null && attrs.hasChildren())
				b.space();
			emit(b, t, 0, 0);
			if (reformat && !oneliner)
				emitRestOfLine(b, t.getNext(), true);
			if (indent) {
				if (mode == Mode.INLINE)
					wrap(b, CONTEXT_TEXT, true);
				else
					emitRestOfLine(b, t.getNext(), true);
				if (!noindent)
					b.indent();
			}
		}
		Mode emode;
		int elms = 0;
		boolean wasspace = true;
		boolean wasbreak = false;
		if (LA1() == ASTscript) {
			script(b, cmode);
			oneliner = false;
		} else if (LA1() == ASTstyle) {
			style(b, cmode);
			oneliner = false;
		} else if (!empty && notEndTag()) {
			_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
				if (notEndTag()) {
					if (LA1() == ASTtext) {
						wasbreak = text(b, cmode, wasspace, false);
					}
					emode = element(b, wasbreak, cmode);
					wasbreak = false;
					wasspace = false;
					++elms;
					if (emode != Mode.INLINE
						|| !(style == Style.INLINE || style == Style.PARAGRAPH || style == Style.BLOCK)
							&& elms > 1)
						oneliner = false;
				} else {
					if (!_cnt1) {
						throw llkParseException("()+ expected at least one occuence", LT1);
					}
					break _loop1;
				}
			}
		}
		if (LA1() == ASTendTag || LA1() == ASTtext) {
			if (LA1() == ASTtext) {
				text(b, cmode, wasspace, true);
			}
			if (indent) {
				t = lt1First();
				if (cmode == Mode.INLINE)
					wrap(b, CONTEXT_TEXT, true);
				else
					emitRestOfLine(b, t, true);
				if (!noindent)
					b.unIndent();
			}
			endTag(b, cmode);
			if (cmode == Mode.PRE && mode != Mode.PRE && !b.endsWithLineBreak()) {
				b.newLine(false);
			}
			if (reformat) {
				if (!oneliner) {
					buf.appendFormatted(b, false, false, true, true, 0);
				} else {
					b.flush();
					StringBuilder formatted = b.getFormatted();
					String line = HtmlOnelinerFormatter.getSingleton(relax).toOneliner(
						formatted,
						0,
						formatted.length(),
						false,
						buf.getLineWidth(),
						b.getSoftBreaks(),
						CONTEXT_TEXT);
					if (line != null && buf.canFit(line, 0)) {
						buf.append(line);
					} else {
						buf.flushLine();
						buf.appendFormatted(b, false, false, true, true, 0);
						oneliner = false;
					}
				}
			}
		}
		if (mode == Mode.INLINE && !oneliner)
			mode = Mode.BLOCK;
		if (mode != Mode.PRE && style.breakAfter()) {
			if (cmode == Mode.INLINE)
				wrap(buf, CONTEXT_TEXT, true);
			else
				emitRestOfLine(buf, followParent(), true);
		}
		if (style == Style.INLINE || style == Style.PARAGRAPH || style == Style.BLOCK || style == Style.BLOCK1) {
			if (reformat && !oneliner || (sinfo.hasModifier(HtmlTag.C_BLOCK) && !formatOptions.isCompact))
				return Mode.BLOCK;
			return Mode.INLINE;
		} else if (style == Style.BREAKAFTER)
			return Mode.BLOCK;
		else
			return Mode.PRE;
	}

	public void endTag(FormatBuilder buf, Mode mode) throws LLKParseException {
		ILLKToken t = LT1.getFirstToken();
		if (mode == Mode.PRE) {
			emitPre(buf, LT1);
			return;
		}
		ASTendTag etag = (ASTendTag)LT1;
		convertTagCase(etag);
		if (formatOptions.isFixing && etag.isImplicit()) {
			buf.append("</");
			buf.append(etag.getName());
			buf.append(">");
			if (formatOptions.warnImplict)
				buf.append("<!-- implicit -->");
		} else {
			emit(buf, t, LT1.getEndOffset(), 0, 0);
		}
		LT1 = LT1.getNext();
	}

	public void extraEndTag(FormatBuilder buf, Mode mode) throws LLKParseException {
		ILLKToken t = LT1.getFirstToken();
		if (mode == Mode.PRE)
			emitPre(buf, LT1);
		else {
			convertTagCase((ASTendTag)LT1);
			emit(buf, t, LT1.getEndOffset(), 0, 1);
		}
		LT1 = LT1.getNext();
	}

	public Mode comment(FormatBuilder buf, boolean wasbreak, Mode mode) throws LLKParseException {
		if (formatOptions.isFixing) {
			LT1.getFirstToken().setText("<!--");
			LT1.getLastToken().setText("-->");
		}
		mode = emitComment(buf, LT1, wasbreak, mode);
		LT1 = LT1.getNext();
		return mode;
	}

	public Mode pi(FormatBuilder buf, boolean wasbreak, Mode mode) throws LLKParseException {
		mode = emitPI(buf, LT1, wasbreak, mode);
		LT1 = LT1.getNext();
		return mode;
	}

	public Mode asp(FormatBuilder buf, boolean wasbreak, Mode mode) throws LLKParseException {
		emitPI(buf, LT1, wasbreak, mode);
		LT1 = LT1.getNext();
		return mode;
	}

	public Mode jste(FormatBuilder buf, boolean wasbreak, Mode mode) throws LLKParseException {
		emitPI(buf, LT1, wasbreak, mode);
		LT1 = LT1.getNext();
		return mode;
	}

	public Mode declaration(FormatBuilder buf, boolean wasbreak, Mode mode) throws LLKParseException {
		emitDeclaration(buf, LT1, wasbreak, mode);
		LT1 = LT1.getNext();
		return mode;
	}

	public void cdata(FormatBuilder buf, Mode mode) throws LLKParseException {
		emitCData(buf, LT1, mode);
		LT1 = LT1.getNext();
	}

	public void cond(FormatBuilder buf, Mode mode) throws LLKParseException {
		emitCond(buf, LT1, mode);
		LT1 = LT1.getNext();
	}

	public boolean text(FormatBuilder buf, Mode mode, boolean ltrim, boolean rtrim) throws LLKParseException {
		boolean hasbreak = false;
		_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
			if (LA1() == ASTtext) {
				hasbreak |= emitText(buf, LT1, mode, ltrim, rtrim);
				LT1 = LT1.getNext();
			} else {
				if (!_cnt1) {
					throw llkParseException("()+ expected at least one occuence", LT1);
				}
				break _loop1;
			}
		}
		return hasbreak;
	}

	public void script(FormatBuilder buf, Mode mode) throws LLKParseException {
		emitScript(buf, LT1, mode);
		LT1 = LT1.getNext();
	}

	public void style(FormatBuilder buf, Mode mode) throws LLKParseException {
		emitStyle(buf, LT1, mode);
		LT1 = LT1.getNext();
	}

	public void doctype(FormatBuilder buf) throws LLKParseException {
		ILLKToken t = llkParent.getFirstToken();
		FormatBuilder b = null;
		if (LT1 == null) {
			emit(buf, t, 0, 0);
			t = t.getNext();
			if (t.getType() == NAME) {
				emit(buf, t, 1, 0);
				t = t.getNext();
			}
			emit(buf, t, 0, 0);
		} else {
			b = cloneBuf(buf);
			t = emit(b, t, LT1.getOffset(), 1, 0);
			emitRestOfLine(b, t);
			b.indent();
		}
		if (LA1() == ASTdtdId) {
			dtdId(b);
		}
		t = llkParent.getLastToken();
		if (b != null) {
			emit(b, t, 0, 0);
			format(buf, b, CONTEXT_DTD);
		}
		emitRestOfLine(buf, t.getNext());
	}

	public void dtdId(FormatBuilder buf) throws LLKParseException {
		emitDtdId(buf, LT1);
		LT1 = LT1.getNext();
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
			0x00000000, 0x7fa00000, 0x00000010, 
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
	
	/*
	 * The software in this package is distributed under the GNU General Public
	 * License version 2, as published by the Free Software Foundation, but with
	 * the Classpath exception.  You should have received a copy of the GNU General
	 * Public License (GPL) and the Classpath exception along with this program.
	 */
	
	///////////////////////////////////////////////////////////////////////
	
	protected ILLKTreeParserInput llkInput;
	protected ILLKMain llkMain;
	protected ILLKTreeBuilder llkTree;
	protected ILLKNode LT1;
	protected ILLKNode llkParent;
	protected final Stack<ILLKNode> llkStack = new Stack<>();
	protected final List<ILLKLifeCycleListener> llkLifeCycleListeners = new ArrayList<>(4);
	
	///////////////////////////////////////////////////////////////////////
	
	public ILLKTreeParserInput llkGetInput() {
	    return llkInput;
	}
	
	public void llkAddLifeCycleListener(final ILLKLifeCycleListener l) {
	    llkLifeCycleListeners.add(l);
	}
	
	public void llkRemoveLifeCycleListener(final ILLKLifeCycleListener l) {
	    llkLifeCycleListeners.remove(l);
	}
	
	///////////////////////////////////////////////////////////////////////
	
	/**
	 * @return The next token ahead, null if there is none.
	 */
	public final ILLKNode LT1() {
	    return LT1;
	}
	
	/**
	 * @return The nth tokens ahead where 1th is the next token ahead.
	 */
	public final ILLKNode LT(int n) {
	    if (n <= 0) {
	        llkError("ASSERT(n>0): n=" + n);
	        return null;
	    }
	    ILLKNode t = LT1;
	    for (; n > 1 && t != null; --n) {
	        t = t.getNext();
	    }
	    return t;
	}
	
	/**
	 * @return Token type of the next token ahead, EOF if next token is null.
	 */
	public final int LA1() {
	    return LT1 == null ? ILLKTreeParserInput.EOF : LT1.getType();
	}
	
	/**
	 * @return Token type of the next token ahead, EOF if next token is null.
	 */
	public final int LA(int n) {
	    if (n <= 0) {
	        llkError("ASSERT(n>0): n=" + n);
	        return ILLKTreeParserInput.EOF;
	    }
	    ILLKNode t = LT1;
	    for (; n > 1 && t != null; --n) {
	        t = t.getNext();
	    }
	    return (t == null) ? ILLKTreeParserInput.EOF : t.getType();
	}
	
	////////////////////////////////////////////////////////////
	
	/**
	 * @return The parent node for LT1, null if none.
	 */
	public final ILLKNode llkGetParent() {
	    return llkParent;
	}
	
	////////////////////////////////////////////////////////////
	
	/**
	 * Consume chars until one matches the given char
	 */
	public final ILLKNode llkConsumeUntil(final int type) throws LLKParseError {
	    if (LT1 != null) {
	        LT1 = LT1.getNext();
	    }
	    while (LT1 != null && LT1.getType() != type) {
	        LT1 = LT1.getNext();
	    }
	    return LT1;
	}
	
	/**
	 * Consume chars until one matches the given token type.
	 */
	public final ILLKNode llkConsumeUntil(final int[] a) throws LLKParseError {
	    if (LT1 != null) {
	        LT1 = LT1.getNext();
	    }
	    int la1;
	    while (LT1 != null) {
	        la1 = LT1.getType();
	        for (int i = 0; i < a.length; ++i) {
	            if (la1 == a[i]) {
	                return LT1;
	            }
	        }
	        LT1 = LT1.getNext();
	    }
	    return LT1;
	}
	
	/**
	 * Consume chars until one matches the given token type.
	 */
	public final ILLKNode llkConsumeUntil(final int[] bitset, final boolean inverted) throws LLKParseError {
	    if (LT1 != null) {
	        LT1 = LT1.getNext();
	    }
	    int la1;
	    while (LT1 != null) {
	        la1 = LT1.getType();
	        if (inverted ? llkGetBitInverted(la1, bitset) : llkGetBit(la1, bitset)) {
	            return LT1;
	        }
	        LT1 = LT1.getNext();
	    }
	    return LT1;
	}
	
	////////////////////////////////////////////////////////////
	
	protected ISourceLocation llkMapLocation(final ILLKNode n) {
	    return (n == null ? null : llkInput.getLocator().getLocation(n.getOffset()));
	}
	
	protected void llkError(final String msg) {
	    ILLKNode lt1 = LT1;
	    if (lt1 == null && !llkStack.empty()) {
	        lt1 = llkStack.peek();
	    }
	    if (lt1 != null) {
	        llkMain.error(msg, null, lt1.getOffset());
	    } else {
	        llkMain.error(msg);
	    }
	}
	
	protected LLKParseException llkParseException(final String msg, final ILLKNode lt1) {
	    final ILLKNode p = (llkStack.empty() ? null : llkStack.peek());
	    final ILLKNode loc = (lt1 != null ? lt1 : (p != null) ? p : null);
	    return new LLKParseException(msg + ":\n\t" + lt1 + "\n\tparent=" + p, llkMapLocation(loc));
	}
	
	protected LLKParseException llkExtraChildrenException(final ILLKNode child, final ILLKNode parent) {
	    return new LLKParseException(
	        "Extra children not matched:\n\textra child=" + child + "\n\tparent=" + parent,
	        llkMapLocation(child));
	}
	
	protected LLKParseException llkMismatchException(final String msg, final int actual, final ILLKNode lt1) {
	    return llkParseException(msg + ", actual='" + llkGetTokenName(actual) + '\'', lt1);
	}
	
	protected LLKParseException llkMismatchException(
	    final String msg, final int[] bset, final int actual, final ILLKNode lt1) {
	    return llkParseException(msg + llkToString(bset) + ", actual=" + llkToString(actual), lt1);
	}
	
	protected LLKParseException llkMismatchException(
	    final String msg, final int type, final int actual, final ILLKNode lt1) {
	    return llkParseException(
	        msg + '\'' + llkToString(type) + "', actual='" + llkToString(actual) + '\'', lt1);
	}
	
	protected LLKParseException llkMismatchException(
	    final String msg, final int c1, final int c2, final int actual, final ILLKNode lt1) {
	    return llkParseException(
	        msg + "(" + llkToString(c1) + ", " + llkToString(c2) + "), actual=" + llkToString(actual), lt1);
	}
	
	protected LLKParseException llkMismatchException(
	    final String msg, final int c1, final int c2, final int c3, final int actual, final ILLKNode lt1) {
	    return llkParseException(
	        msg
	            + "("
	            + llkToString(c1)
	            + ", "
	            + llkToString(c2)
	            + ", "
	            + llkToString(c3)
	            + "), actual="
	            + llkToString(actual),
	        lt1
	    );
	}
	
	////////////////////////////////////////////////////////////
	
	public final void llkMatch(final ILLKNode lt1, final int type) throws LLKParseException {
	    final int la1 = lt1.getType();
	    if (la1 != type) {
	        throw llkMismatchException("match(int): expected=" + llkGetTokenName(type), la1, lt1);
	    }
	}
	
	public final void llkMatch(final int type) throws LLKParseException {
	    final int la1 = LT1.getType();
	    if (la1 != type) {
	        throw llkMismatchException("match(int): expected=", type, la1, LT1);
	    }
	    LT1 = LT1.getNext();
	}
	
	public final void llkMatchNot(final int type) throws LLKParseException {
	    final int la1 = LT1.getType();
	    if (la1 == type) {
	        throw llkMismatchException("matchNot(int): not expected=", type, la1, LT1);
	    }
	    LT1 = LT1.getNext();
	}
	
	public final void llkMatch(final int type1, final int type2) throws LLKParseException {
	    final int la1 = LT1.getType();
	    if (la1 != type1 && la1 != type2) {
	        throw llkMismatchException("match(int, int): expected=", type1, type2, la1, LT1);
	    }
	    LT1 = LT1.getNext();
	}
	
	public final void llkMatchNot(final int type1, final int type2) throws LLKParseException {
	    final int la1 = LT1.getType();
	    if (la1 == type1 || la1 == type2) {
	        throw llkMismatchException("matchNot(int, int): not expected=", type1, type2, la1, LT1);
	    }
	    LT1 = LT1.getNext();
	}
	
	public final void llkMatch(final int type1, final int type2, final int type3) throws LLKParseException {
	    final int la1 = LT1.getType();
	    if (la1 != type1 && la1 != type2 && la1 != type3) {
	        throw llkMismatchException("match(int, int, int): expected=", type1, type2, type3, la1, LT1);
	    }
	    LT1 = LT1.getNext();
	}
	
	public final void llkMatchNot(final int type1, final int type2, final int type3) throws LLKParseException {
	    final int la1 = LT1.getType();
	    if (la1 == type1 || la1 == type2 || la1 == type3) {
	        throw llkMismatchException(
	            "matchNot(int, int, int): not expected=", type1, type2, type3, la1, LT1);
	    }
	    LT1 = LT1.getNext();
	}
	
	public final void llkMatchRange(final int first, final int last) throws LLKParseException {
	    final int la1 = LT1.getType();
	    if (la1 < first || la1 > last) {
	        throw llkMismatchException("matchRange(int, int): range=", first, last, la1, LT1);
	    }
	    LT1 = LT1.getNext();
	}
	
	public final void llkMatchNotRange(final int first, final int last) throws LLKParseException {
	    final int la1 = LT1.getType();
	    if (la1 >= first && la1 <= last) {
	        throw llkMismatchException("matchNotRange(int, int): range=", first, last, la1, LT1);
	    }
	    LT1 = LT1.getNext();
	}
	
	public final void llkMatch(final int[] bset) throws LLKParseException {
	    final int la1 = LT1.getType();
	    if (llkGetBit(la1, bset)) {
	        LT1 = LT1.getNext();
	    } else {
	        throw llkMismatchException("match(BitSet): expected=", bset, la1, LT1);
	    }
	}
	
	public final void llkMatchNot(final int[] bset) throws LLKParseException {
	    final int la1 = LT1.getType();
	    if (llkGetBitInverted(la1, bset)) {
	        LT1 = LT1.getNext();
	    } else {
	        throw llkMismatchException("match(BitSet): expected=", bset, la1, LT1);
	    }
	}
	
	////////////////////////////////////////////////////////////
	
	public final boolean llkSynMatch(final int type) {
	    if (LT1.getType() != type) {
	        return false;
	    }
	    LT1 = LT1.getNext();
	    return true;
	}
	
	public final boolean llkSynMatchNot(final int type) {
	    if (LT1.getType() == type) {
	        return false;
	    }
	    LT1 = LT1.getNext();
	    return true;
	}
	
	public final boolean llkSynMatch(final int type1, final int type2) {
	    final int la1 = LT1.getType();
	    if (la1 != type1 && la1 != type2) {
	        return false;
	    }
	    LT1 = LT1.getNext();
	    return true;
	}
	
	public final boolean llkSynMatchNot(final int type1, final int type2) {
	    final int la1 = LT1.getType();
	    if (la1 == type1 || la1 == type2) {
	        return false;
	    }
	    LT1 = LT1.getNext();
	    return true;
	}
	
	public final boolean llkSynMatch(final int type1, final int type2, final int type3) {
	    final int la1 = LT1.getType();
	    if (la1 != type1 && la1 != type2 && la1 != type3) {
	        return false;
	    }
	    LT1 = LT1.getNext();
	    return true;
	}
	
	public final boolean llkSynMatchNot(final int type1, final int type2, final int type3) {
	    final int la1 = LT1.getType();
	    if (la1 == type1 || la1 == type2 || la1 == type3) {
	        return false;
	    }
	    LT1 = LT1.getNext();
	    return true;
	}
	
	public final boolean llkSynMatchRange(final int first, final int last) {
	    final int la1 = LT1.getType();
	    if (la1 < first || la1 > last) {
	        return false;
	    }
	    LT1 = LT1.getNext();
	    return true;
	}
	
	public final boolean llkSynMatchNotRange(final int first, final int last) {
	    final int la1 = LT1.getType();
	    if (la1 >= first && la1 <= last) {
	        return false;
	    }
	    LT1 = LT1.getNext();
	    return true;
	}
	
	public final boolean llkSynMatch(final int[] bset) {
	    final int la1 = LT1.getType();
	    if (llkGetBit(la1, bset)) {
	        LT1 = LT1.getNext();
	        return true;
	    }
	    return false;
	}
	
	public final boolean llkSynMatchNot(final int[] bset) {
	    final int la1 = LT1.getType();
	    if (llkGetBitInverted(la1, bset)) {
	        LT1 = LT1.getNext();
	        return true;
	    }
	    return false;
	}
	
	protected final boolean llkSynMark() {
	    llkInput.mark(LT1);
	    return true;
	}
	
	protected final boolean llkSynRewind(final boolean b) {
	    LT1 = llkInput.rewind();
	    return b;
	}
	
	protected final boolean llkSynMatchOrRewind(final boolean b) {
	    if (!b) {
	        LT1 = llkInput.rewind();
	    }
	    return b;
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

	public void llkReset() {
		llkInput.reset();
		for (ILLKLifeCycleListener l: llkLifeCycleListeners) {
			l.reset();
		}
	}

	////////////////////////////////////////////////////////////
}
