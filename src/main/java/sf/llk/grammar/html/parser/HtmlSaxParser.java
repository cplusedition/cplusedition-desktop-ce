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

import sf.llk.grammar.html.HtmlTag;
import sf.llk.grammar.html.IHtmlConstants;
import sf.llk.grammar.html.IHtmlHandler;
import sf.llk.grammar.html.MsgId;
import sf.llk.share.support.ILLKConstants;
import sf.llk.share.support.ILLKLifeCycleListener;
import sf.llk.share.support.ILLKMain;
import sf.llk.share.support.ILLKNode;
import sf.llk.share.support.ILLKParser;
import sf.llk.share.support.ILLKParserInput;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.support.ISourceText;
import sf.llk.share.support.LLKParseException;
import sf.llk.share.support.LLKParserBase;
import sf.llk.share.support.LLKParserInput;
import sf.llk.share.util.MarkedStack;

public class HtmlSaxParser extends LLKParserBase implements ILLKParser, ILLKHtmlSaxParser {

	////////////////////////////////////////////////////////////////////////

	protected ILLKNode theDocument;
	protected IHtmlLexer lexer;
	protected IHtmlHandler handler;
	protected MarkedStack<HtmlTag> tagStack;
	protected boolean relax;
	protected boolean warnImplicit;
	protected boolean warnXml;

	////////////////////////////////////////////////////////////////////////

	public HtmlSaxParser(char[] source, ILLKMain main, IHtmlHandler handler) throws IOException {
		this(new HtmlLexer(source, main), handler);
	}

	public HtmlSaxParser(ILLKMain main, IHtmlHandler handler) throws IOException {
		this(new HtmlLexer(main.getFileContent(), main), handler);
	}

	public HtmlSaxParser(IHtmlLexer lexer, IHtmlHandler handler) throws IOException {
		this(new LLKParserInput(lexer));
		this.lexer = lexer;
		this.handler = handler;
		this.tagStack = new MarkedStack<HtmlTag>();
		this.relax = llkMain.getOptBool(IHtmlConstants.OPT_RELAX);
		this.warnImplicit = llkMain.getOptBool(IHtmlConstants.OPT_WARN_IMPLICIT);
		this.warnXml = llkMain.getOptBool(IHtmlConstants.OPT_WARN_XML);
	}

	////////////////////////////////////////////////////////////////////////

	public ILLKNode getDocument() {
		return theDocument;
	}

	public int getCharCount() {
		return llkGetSource().length();
	}

	////////////////////////////////////////////////////////////////////////

	protected void llkInit() {
	}

	private boolean optionalMissing(ASTstartTag stag) {
		int la1 = LA1();
		if (la1 != LT && la1 != ENDTAG || LA(2) != NAME)
			return false;
		ILLKToken tag = LT(2);
		HtmlTag sinfo = stag.getTag();
		HtmlTag by = HtmlTag.get(tag.getText().toString());
		if (by == null || la1 == ENDTAG && !tagStack.contains(by)) {
			return false;
		}
		if (!sinfo.optionalEndTag()) {
			int stacksize = tagStack.size();
			if (relax && la1 == ENDTAG && sinfo.isInline() && sinfo != by && stacksize > 1) {
				for (int i = 0; i < stacksize; ++i) {
					if (tagStack.get(i) == by) {
						return true;
					}
				}
			}
			if (la1 == ENDTAG && sinfo != by && stacksize > 1 && tagStack.peek(2) == by) {
				String msg = "start="
					+ sinfo
					+ "@"
					+ stag.getOffset()
					+ ", end="
					+ by
					+ "@"
					+ tag.getOffset();
				relaxError(MsgId.DomMissingEndTag, msg, null, tag);
				return true;
			}
			return false;
		}
		if (sinfo.endOptional(by, la1 == ENDTAG)) {
			if (warnImplicit) {
				warn(
					MsgId.DomImplicitEndTag,
					sinfo
						+ "@"
						+ stag.getOffset()
						+ ", ended implicitly by: "
						+ by
						+ "@"
						+ tag.getOffset(),
					null,
					tag);
			}
			return true;
		}
		if (la1 == ENDTAG && sinfo != by) {
			warn(
				MsgId.DomImplicitEndTag,
				sinfo + "@" + stag.getOffset() + ", ended by end tag: " + by + "@" + tag.getOffset(),
				null,
				tag);
			return true;
		}
		if (la1 == ENDTAG && sinfo != by && tagStack.size() > 1 && tagStack.peek(2) == by) {
			warn(
				MsgId.DomMissingOptionalEndTag,
				"start=" + sinfo + "@" + stag.getOffset() + ", end=" + by + "@" + tag.getOffset(),
				null,
				tag);
			return true;
		}
		return false;
	}

	private boolean extraOptional(HtmlTag sinfo) {
		int la1 = LA1();
		if (la1 != ENDTAG || LA(2) != NAME)
			return false;
		ILLKToken etag = LT(2);
		HtmlTag einfo = HtmlTag.get(etag.getText().toString());
		if (einfo != null
			&& (relax && (einfo.isEmpty() || einfo != sinfo) || einfo.optionalEndTag() && einfo != sinfo)) {
			return true;
		}
		return false;
	}

	private void warn(MsgId id, CharSequence msg, Throwable e, ISourceText t) {
		llkMain.warn(id.getMessage() + ": " + msg, e, t.getOffset());
	}

	private void error(MsgId id, Throwable e, ISourceText t) {
		llkMain.error(id.getMessage(), e, t.getOffset());
	}

	private void relaxError(MsgId id, CharSequence msg, Throwable e, ISourceText t) {
		String s = id.getMessage() + ": " + msg;
		if (relax)
			llkMain.warn(s, e, t.getOffset());
		else
			llkMain.error(s, e, t.getOffset());
	}

	////////////////////////////////////////////////////////////////////////

	public static final int LLK_INPUT_VOCAB_SIZE = 52;

	public HtmlSaxParser(ILLKParserInput input) {
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

	public ASTdocument document() throws LLKParseException {
		ASTdocument llkThis = new ASTdocument();
		theDocument = llkThis;
		llkThis.setFirstToken(LT1());
		handler.startDocument(llkThis);
		if (LA1() == TEXT) {
			text();
		}
		_loop1: while (true) {
			if (llkGetBit(LA1(), LLKTokenSet0.bitset)) {
				element();
			} else {
				break _loop1;
			}
		}
		llkMatch(_EOF_);
		llkThis.setLastToken(LT0());
		handler.endDocument(llkThis);
		return llkThis;
	}

	public void element() throws LLKParseException {
		if (LA1() == LT) {
			startTag();
		} else if (llkGetBit(LA1(), LLKTokenSet1.bitset)) {
			switch (LA1()) {
				case ENDTAG:
					endTag(null, true);
					break;
				case LCOMMENT:
					comment();
					break;
				case LPI:
					pi();
					break;
				case LASP:
					asp();
					break;
				case LJSTE:
					jste();
					break;
				case LCDATA:
					cdata();
					break;
				case LCOND:
					cond();
					break;
				default:
					if ((LA1() == LDECL) && (LA(2) == DECLARATION)) {
						declaration();
					} else if (LA1() == LDECL) {
						doctype();
					} else {
						throw llkParseException("Unexpected token");
					}
			}
			if (LA1() == TEXT) {
				text();
			}
		} else {
			throw llkParseException("Unexpected token");
		}
	}

	public void startTag() throws LLKParseException {
		ILLKToken t = null;
		ASTattributes attrs = null;
		ASTstartTag llkThis = new ASTstartTag();
		llkThis.setFirstToken(LT1());
		boolean empty = false;
		boolean optmissing = false;
		HtmlTag sinfo = null;
		llkMatch(LT);
		t = LT1();
		llkMatch(NAME);
		if (LA1() == NAME) {
			attrs = attributes();
		}
		if (llkInput.matchOpt(GT)) {
		} else if (llkInput.matchOpt(ENDEMPTY)) {
			empty = true;
		} else {
			throw llkParseException("Unexpected token");
		}
		if (empty && warnXml) {
			warn(MsgId.DomXmlEmptyTag, t.getText(), null, t);
		}
		llkThis.setLastToken(LT0());
		llkThis.init(attrs, empty);
		empty = llkThis.isEmpty();
		sinfo = llkThis.getTag();
		int type = lexer.llkLookupKeyword(llkGetSource(t.getOffset(), t.getEndOffset()), NAME);
		if (type == LSCRIPT)
			lexer.pushContext(CONTEXT_SCRIPT);
		else if (type == LSTYLE)
			lexer.pushContext(CONTEXT_STYLE);
		handler.startTag(llkThis);
		if ((LA1() == SCRIPT) && (!empty)) {
			script();
		} else if ((LA1() == STYLE) && (!empty)) {
			style();
		} else {
			if (LA1() == TEXT) {
				text();
			}
			_loop1: while (true) {
				if ((llkGetBit(LA1(), LLKTokenSet0.bitset)) && (!empty && LA1() != ENDTAG && !(optmissing = optionalMissing(llkThis)))) {
					element();
				} else if ((LA1() == ENDTAG) && (!empty && !(optmissing = optionalMissing(llkThis)) && extraOptional(sinfo))) {
					endTag(llkThis, true);
					if (LA1() == TEXT) {
						text();
					}
				} else {
					break _loop1;
				}
			}
		}
		if ((LA1() == ENDTAG) && (!empty && !optmissing)) {
			endTag(llkThis, false);
			if (LA1() == TEXT) {
				text();
			}
		} else {
			if (!empty) {
				ASTendTag etag = new ASTendTag(true);
				etag.init(llkThis.getName().toString());
				etag.setFirstToken(LT1());
				etag.setLastToken(LT0());
				handler.endTag(etag);
			}
		}
	}

	public void endTag(ASTstartTag stag, boolean extra) throws LLKParseException {
		ILLKToken t = null;
		ASTendTag llkThis = new ASTendTag();
		llkThis.setFirstToken(LT1());
		ISourceText sname = (stag == null ? null : stag.getNamePosition());
		if (stag != null && LA1() != ENDTAG) {
			throw new LLKParseException(
				"Expected end tag for: " + sname.getText() + "@" + sname.getOffset(),
				llkGetLocation(sname.getOffset()));
		}
		llkMatch(ENDTAG);
		t = LT1();
		llkMatch(NAME);
		if (llkInput.matchOpt(GT)) {
		} else {
			error(MsgId.DomGTExpected, null, LT0());
			lexer.popContext();
		}
		llkThis.setLastToken(LT0());
		llkThis.init(t, extra);
		if (!extra) {
			String ename = t.getText().toString();
			if (!ename.equalsIgnoreCase(sname.getText().toString())) {
				String msg = "start="
					+ sname
					+ "@"
					+ sname.getOffset()
					+ ", end="
					+ ename
					+ "@"
					+ t.getOffset();
				relaxError(MsgId.DomMismatchEndTag, msg, null, t);
			}
		}
		handler.endTag(llkThis);
	}

	public ASTattributes attributes() throws LLKParseException {
		ASTattribute a = null;
		ASTattributes llkThis = new ASTattributes();
		llkThis.setFirstToken(LT1());
		ILLKNode prev = null;
		_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
			if (LA1() == NAME) {
				a = attribute();
				llkThis.add(a, prev);
				prev = a;
			} else {
				if (!_cnt1) {
					throw llkParseException("()+ expected at least one occuence");
				}
				break _loop1;
			}
		}
		llkThis.setLastToken(LT0());
		return llkThis;
	}

	public ASTattribute attribute() throws LLKParseException {
		ILLKToken t = null;
		ILLKToken v = null;
		ASTattribute llkThis = new ASTattribute();
		llkThis.setFirstToken(LT1());
		t = LT1();
		llkMatch(NAME);
		if (llkInput.matchOpt(ASSIGN)) {
			if (LA1() == NAME || LA1() == ATTVALUE) {
				llkInput.consume();
			} else {
				relaxError(MsgId.DomMissingValue, t.getText(), null, t);
			}
		} else if (LA1() == STRING) {
			v = LT1();
			llkInput.consume();
			relaxError(MsgId.DomMissingAssign, t.getText(), null, v);
		}
		llkThis.setLastToken(LT0());
		return llkThis;
	}

	public void comment() throws LLKParseException {
		ASTcomment llkThis = new ASTcomment();
		llkThis.setFirstToken(LT1());
		llkMatch(LCOMMENT);
		llkMatch(COMMENT);
		llkMatch(RCOMMENT);
		llkThis.setLastToken(LT0());
		handler.comment(llkThis);
	}

	public void pi() throws LLKParseException {
		ILLKToken t = null;
		ASTpi llkThis = new ASTpi();
		llkMatch(LPI);
		t = LT1();
		llkMatch(NAME);
		llkMatch(PI);
		llkMatch(RPI);
		llkThis.setLastToken(LT0());
		llkThis.init(t);
		handler.pi(llkThis);
	}

	public void asp() throws LLKParseException {
		ASTasp llkThis = new ASTasp();
		llkMatch(LASP);
		llkMatch(ASP);
		llkMatch(RASP);
		llkThis.setLastToken(LT0());
		handler.pi(llkThis);
	}

	public void jste() throws LLKParseException {
		ASTjste llkThis = new ASTjste();
		llkMatch(LJSTE);
		llkMatch(JSTE);
		llkMatch(RJSTE);
		llkThis.setLastToken(LT0());
		handler.pi(llkThis);
	}

	public void cdata() throws LLKParseException {
		ASTcdata llkThis = new ASTcdata();
		llkMatch(LCDATA);
		llkMatch(CDATA);
		llkMatch(RCDATA);
		llkThis.setLastToken(LT0());
		handler.cdata(llkThis);
	}

	public void cond() throws LLKParseException {
		ASTcond llkThis = new ASTcond();
		llkMatch(LCOND);
		llkMatch(COND);
		llkMatch(RCOND);
		llkThis.setLastToken(LT0());
		handler.cond(llkThis);
	}

	public void declaration() throws LLKParseException {
		ILLKToken t = null;
		ASTdeclaration llkThis = new ASTdeclaration();
		t = LT1();
		llkMatch(LDECL);
		ILLKToken data = (ILLKToken)t.getData();
		CharSequence name = (data == null ? "" : data.getText());
		warn(MsgId.DomUnknownDeclaration, name + "@" + t.getOffset(), null, t);
		llkMatch(DECLARATION);
		llkMatch(GT);
		llkThis.setLastToken(LT0());
		handler.declaration(llkThis);
	}

	public void text() throws LLKParseException {
		ASTtext llkThis = new ASTtext();
		llkThis.setFirstToken(LT1());
		llkMatch(TEXT);
		llkThis.setLastToken(LT0());
		handler.text(llkThis);
	}

	public void script() throws LLKParseException {
		ASTscript llkThis = new ASTscript();
		llkThis.setFirstToken(LT1());
		llkMatch(SCRIPT);
		llkThis.setLastToken(LT0());
		handler.script(llkThis);
	}

	public void style() throws LLKParseException {
		ASTstyle llkThis = new ASTstyle();
		llkThis.setFirstToken(LT1());
		llkMatch(STYLE);
		llkThis.setLastToken(LT0());
		handler.script(llkThis);
	}

	public void doctype() throws LLKParseException {
		ILLKToken name = null;
		ASTdtdId id = null;
		ASTdoctype llkThis = new ASTdoctype();
		llkThis.setFirstToken(LT1());
		llkMatch(LDECL);
		if (LA1() == NAME) {
			name = LT1();
			llkInput.consume();
		}
		if (LA1() == PUBLIC || LA1() == SYSTEM) {
			id = dtdId();
		}
		llkMatch(GT);
		llkThis.setLastToken(LT0());
		llkThis.init(name, id);
		handler.doctype(llkThis);
	}

	public ASTdtdId dtdId() throws LLKParseException {
		ILLKToken sysid = null;
		ILLKToken pubid = null;
		ASTdtdId llkThis = new ASTdtdId();
		llkThis.setFirstToken(LT1());
		if (llkInput.matchOpt(SYSTEM)) {
			sysid = LT1();
			llkMatch(SYSTEM_LITERAL);
		} else if (llkInput.matchOpt(PUBLIC)) {
			pubid = LT1();
			llkMatch(PUBID_LITERAL);
			if (LA1() == SYSTEM_LITERAL) {
				sysid = LT1();
				llkInput.consume();
			}
		} else {
			throw llkParseException("Unexpected token");
		}
		llkThis.setLastToken(LT0());
		llkThis.init(pubid, sysid);
		return llkThis;
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
		1,
		new int[] {
			0x07c05480, 
		}
	);
	static final LLKTokenSet LLKTokenSet1 = new LLKTokenSet(
		false,
		0,
		1,
		new int[] {
			0x07c05400, 
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
}
