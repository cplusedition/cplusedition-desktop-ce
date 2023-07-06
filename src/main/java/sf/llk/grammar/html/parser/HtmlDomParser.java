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
import sf.llk.share.support.LLKTree;
import sf.llk.share.util.MarkedStack;

public class HtmlDomParser extends LLKParserBase implements ILLKParser, ILLKHtmlDomParser {

	////////////////////////////////////////////////////////////////////////

	protected IHtmlLexer lexer;
	protected MarkedStack<HtmlTag> tagStack;
	protected boolean relax;
	protected boolean warnImplicit;
	protected boolean warnXml;

	////////////////////////////////////////////////////////////////////////

	public HtmlDomParser(char[] source, ILLKMain main) throws IOException {
		this(new HtmlLexer(source, main));
	}

	public HtmlDomParser(ILLKMain main) throws IOException {
		this(new HtmlLexer(main.getFileContent(), main));
	}

	public HtmlDomParser(IHtmlLexer lexer) {
		this(new LLKParserInput(lexer));
		this.lexer = lexer;
		this.tagStack = new MarkedStack<HtmlTag>();
		this.relax = llkMain.getOptBool(IHtmlConstants.OPT_RELAX);
		this.warnImplicit = llkMain.getOptBool(IHtmlConstants.OPT_WARN_IMPLICIT);
		this.warnXml = llkMain.getOptBool(IHtmlConstants.OPT_WARN_XML);
	}

	////////////////////////////////////////////////////////////////////////

	public int getCharCount() {
		return llkGetSource().length();
	}

	////////////////////////////////////////////////////////////////////////

	protected void llkInit() {
	}

	private void llkOpenNode(LLKNode node) throws LLKParseException {
		llkTree.open();
		if (node != null)
			node.setFirstToken(LT1());
	}

	private void llkCloseNode(LLKNode node, boolean create) throws LLKParseException {
		llkTree.close(node, create);
		if (create) {
			ILLKToken t = LT0();
			if (t.getOffset() < node.getOffset())
				node.setFirstToken(null);
			node.setLastToken(LT0());
		}
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

	private HtmlDomParser(ILLKParserInput input) {
		llkInput = input;
		llkMain = input.getMain();
		llkTree = new LLKTree();
		llkInit();
	}

	public void llkReset() {
		llkInput.reset();
		llkTree.reset();
		llkInit();
		for (ILLKLifeCycleListener l: llkLifeCycleListeners) {
			l.reset();
		}
	}

	////////////////////////////////////////////////////////////

	public ASTdocument document() throws LLKParseException {
		ASTdocument llkThis = new ASTdocument();
		llkOpenNode(llkThis);
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
		llkCloseNode(llkThis, true);
		return llkThis;
	}

	public void element() throws LLKParseException {
		if (LA1() == LT) {
			startTag();
		} else if (llkGetBit(LA1(), LLKTokenSet1.bitset)) {
			switch (LA1()) {
				case ENDTAG:
					extraEndTag(null);
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
		boolean empty = false;
		boolean optmissing = false;
		boolean extraoptional = false;
		HtmlTag sinfo = null;
		ILLKNode text = null;
		ILLKToken last = null;
		llkOpenNode(llkThis);
		llkMatch(LT);
		t = LT1();
		llkMatch(NAME);
		if (LA1() == NAME) {
			attrs = attributes();
			llkTree.pop();
		}
		if (llkInput.matchOpt(GT)) {
		} else if (llkInput.matchOpt(ENDEMPTY)) {
			empty = true;
		} else {
			throw llkParseException("Unexpected token");
		}
		last = LT0();
		t.setNext(last);
		if (empty && warnXml) {
			warn(MsgId.DomXmlEmptyTag, t.getText(), null, t);
		}
		llkThis.init(attrs, empty);
		empty = llkThis.isEmpty();
		int type = lexer.llkLookupKeyword(llkGetSource(t.getOffset(), t.getEndOffset()), NAME);
		if (!empty) {
			if (type == LSCRIPT)
				lexer.pushContext(CONTEXT_SCRIPT);
			else if (type == LSTYLE)
				lexer.pushContext(CONTEXT_STYLE);
		}
		sinfo = llkThis.getTag();
		if (sinfo == HtmlTag.UNKNOWN ) {
			ISourceText sname = llkThis.getNamePosition();
			relaxError(MsgId.DomUnknownStartTag, sname.getText(), null, sname);
		}
		if (!empty)
			tagStack.push(sinfo);
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
				} else if ((LA1() == ENDTAG) && (!empty
					&& !(optmissing = optionalMissing(llkThis))
					&& (extraoptional = extraOptional(sinfo)))) {
					extraEndTag(extraoptional ? null : llkThis);
					if (LA1() == TEXT) {
						text();
					}
				} else {
					break _loop1;
				}
			}
		}
		if ((LA1() == ENDTAG) && (!empty && !optmissing)) {
			endTag(llkThis);
			if (LA1() == TEXT) {
				text();
			}
		} else {
			if (!empty) {
				ASTendTag etag = new ASTendTag(true);
				etag.init(llkThis.getName().toString());
				etag.setFirstToken(LT1());
				etag.setLastToken(LT0());
				llkTree.push(etag);
			}
		}
		if (llkTree.childCount() > 0) {
			ILLKNode n = llkTree.peek();
			if (n.getType() == ASTtext) {
				llkTree.pop();
				text = n;
				if (llkTree.childCount() > 0)
					last = llkTree.peek().getLastToken();
			}
		}
		llkCloseNode(llkThis, true);
		if (text != null) {
			llkThis.setLastToken(last);
			llkTree.push(text);
		}
	}

	public void endTag(ASTstartTag stag) throws LLKParseException {
		ILLKToken t = null;
		ASTendTag llkThis = new ASTendTag();
		ISourceText stoken = stag.getNamePosition();
		String sname = stoken.getText().toString();
		if (LA1() != ENDTAG) {
			throw new LLKParseException(
				"Expected end tag for: " + sname + "@" + stoken.getOffset(), llkGetLocation(stoken.getOffset()));
		}
		llkOpenNode(llkThis);
		llkMatch(ENDTAG);
		t = LT1();
		llkMatch(NAME);
		if (llkInput.matchOpt(GT)) {
		} else {
			error(MsgId.DomGTExpected, null, LT0());
			lexer.popContext();
		}
		llkThis.init(t, false);
		String ename = t.getText().toString();
		boolean pop = true;
		HtmlTag info = HtmlTag.get(ename);
		if (info.isEmpty()) {
			warn(MsgId.DomEndEmptyTag, ename, null, LT0());
			pop = false;
		} else if (info == HtmlTag.UNKNOWN) {
			relaxError(MsgId.DomUnknownEndTag, ename, null, t);
		} else if (!sname.equalsIgnoreCase(ename)) {
			String msg = "start="
				+ sname
				+ "@"
				+ stoken.getOffset()
				+ ", end="
				+ ename
				+ "@"
				+ t.getOffset();
			relaxError(MsgId.DomMismatchEndTag, msg, null, t);
			pop = false;
		}
		if (pop)
			tagStack.pop();
		llkCloseNode(llkThis, true);
	}

	public void extraEndTag(ASTstartTag stag) throws LLKParseException {
		ILLKToken t = null;
		ASTextraEndTag llkThis = new ASTextraEndTag();
		ISourceText stoken = (stag == null ? null : stag.getNamePosition());
		if (stag != null && LA1() != ENDTAG) {
			throw new LLKParseException(
				"Expected end tag for: " + stoken.getText() + "@" + stoken.getOffset(),
				llkGetLocation(stoken.getOffset()));
		}
		llkOpenNode(llkThis);
		llkMatch(ENDTAG);
		t = LT1();
		llkMatch(NAME);
		if (llkInput.matchOpt(GT)) {
		} else {
			error(MsgId.DomGTExpected, null, LT0());
			lexer.popContext();
		}
		llkCloseNode(llkThis, true);
		llkThis.init(t, true);
		String ename = t.getText().toString();
		HtmlTag info = HtmlTag.get(ename);
		warn((info.isEmpty() ? MsgId.DomEndEmptyTag : MsgId.DomExtraEndTag), ename + "@" + t.getOffset(), null, t);
		return;
	}

	public void comment() throws LLKParseException {
		ASTcomment llkThis = new ASTcomment();
		llkOpenNode(llkThis);
		llkMatch(LCOMMENT);
		llkMatch(COMMENT);
		llkMatch(RCOMMENT);
		llkCloseNode(llkThis, true);
	}

	public void pi() throws LLKParseException {
		ASTpi llkThis = new ASTpi();
		llkOpenNode(llkThis);
		llkMatch(LPI);
		llkMatch(PI);
		llkMatch(RPI);
		llkCloseNode(llkThis, true);
	}

	public void asp() throws LLKParseException {
		ASTasp llkThis = new ASTasp();
		llkOpenNode(llkThis);
		llkMatch(LASP);
		llkMatch(ASP);
		llkMatch(RASP);
		llkCloseNode(llkThis, true);
	}

	public void jste() throws LLKParseException {
		ASTjste llkThis = new ASTjste();
		llkOpenNode(llkThis);
		llkMatch(LJSTE);
		llkMatch(JSTE);
		llkMatch(RJSTE);
		llkCloseNode(llkThis, true);
	}

	public void cdata() throws LLKParseException {
		ASTcdata llkThis = new ASTcdata();
		llkOpenNode(llkThis);
		llkMatch(LCDATA);
		llkMatch(CDATA);
		llkMatch(RCDATA);
		llkCloseNode(llkThis, true);
	}

	public void cond() throws LLKParseException {
		ASTcond llkThis = new ASTcond();
		llkOpenNode(llkThis);
		llkMatch(LCOND);
		llkMatch(COND);
		llkMatch(RCOND);
		llkCloseNode(llkThis, true);
	}

	public void declaration() throws LLKParseException {
		ILLKToken t = null;
		ASTdeclaration llkThis = new ASTdeclaration();
		llkOpenNode(llkThis);
		t = LT1();
		llkMatch(LDECL);
		ILLKToken data = (ILLKToken)t.getData();
		CharSequence name = (data == null ? "" : data.getText());
		warn(MsgId.DomUnknownDeclaration, name + "@" + t.getOffset(), null, t);
		llkMatch(DECLARATION);
		llkMatch(GT);
		llkCloseNode(llkThis, true);
	}

	public ASTattributes attributes() throws LLKParseException {
		ASTattributes llkThis = new ASTattributes();
		llkOpenNode(llkThis);
		_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
			if (LA1() == NAME) {
				attribute();
			} else {
				if (!_cnt1) {
					throw llkParseException("()+ expected at least one occuence");
				}
				break _loop1;
			}
		}
		llkCloseNode(llkThis, true);
		return llkThis;
	}

	public void attribute() throws LLKParseException {
		ILLKToken t = null;
		ILLKToken v = null;
		ASTattribute llkThis = new ASTattribute();
		llkOpenNode(llkThis);
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
		llkCloseNode(llkThis, true);
	}

	public void text() throws LLKParseException {
		ASTtext llkThis = new ASTtext();
		llkOpenNode(llkThis);
		_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
			if (llkInput.matchOpt(TEXT)) {
			} else {
				if (!_cnt1) {
					throw llkParseException("()+ expected at least one occuence");
				}
				break _loop1;
			}
		}
		llkCloseNode(llkThis, true);
	}

	public void script() throws LLKParseException {
		ASTscript llkThis = new ASTscript();
		llkOpenNode(llkThis);
		llkMatch(SCRIPT);
		llkCloseNode(llkThis, true);
	}

	public void style() throws LLKParseException {
		ASTstyle llkThis = new ASTstyle();
		llkOpenNode(llkThis);
		llkMatch(STYLE);
		llkCloseNode(llkThis, true);
	}

	public void doctype() throws LLKParseException {
		ILLKToken t = null;
		ASTdtdId id = null;
		ASTdoctype llkThis = new ASTdoctype();
		llkOpenNode(llkThis);
		llkMatch(LDECL);
		if (LA1() == NAME) {
			t = LT1();
			llkInput.consume();
		}
		if (LA1() == PUBLIC || LA1() == SYSTEM) {
			id = dtdId();
		}
		llkMatch(GT);
		llkCloseNode(llkThis, true);
		llkThis.init(t, id);
	}

	public ASTdtdId dtdId() throws LLKParseException {
		ILLKToken sysid = null;
		ILLKToken pubid = null;
		ASTdtdId llkThis = new ASTdtdId();
		llkOpenNode(llkThis);
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
		llkCloseNode(llkThis, true);
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
