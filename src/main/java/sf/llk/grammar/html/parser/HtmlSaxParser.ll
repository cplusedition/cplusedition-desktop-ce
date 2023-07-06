%PARSER (HtmlSaxParser)

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

/*
 * Copyright (c) 2005, Chris Leung. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2 with a special exception, the Classpath exception.
 * You should have received a copy of the GNU General Public License (GPL)
 * and the Classpath exception along with this library.
 */
public class HtmlSaxParser extends LLKParserBase implements ILLKParser, ILLKHtmlSaxParser {

	////////////////////////////////////////////////////////////////////////

	protected ILLKNode theDocument;
	// protected char[] source;
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
		// source = llkGetSource();
	}

	//	private void llkOpenNode(LLKNode node) {
	//		llkTree.open();
	//		if (node != null)
	//			node.setFirstToken(LT1());
	//	}
	//
	//	private void llkCloseNode(LLKNode node, boolean create) {
	//		llkTree.close(node, create);
	//		if (create) {
	//			ILLKToken t = LT0();
	//			if (t.getOffset() < node.getOffset()) {
	//				node.setFirstToken(null);
	//			}
	//			node.setLastToken(t);
	//		}
	//	}
	//
	//	private void checkLiteral(String expected) {
	//		ILLKToken lt0 = LT0();
	//		if (!expected.equals(lt0.getText()))
	//			error("Unexpected literal: expected=" + expected + ", actual=" + lt0.getText(), lt0);
	//	}

	private boolean optionalMissing(ASTstartTag stag) {
		int la1 = LA1();
		if (la1 != LT && la1 != ENDTAG || LA(2) != NAME)
			return false;
		ILLKToken tag = LT(2);
		HtmlTag sinfo = stag.getTag();
		HtmlTag by = HtmlTag.get(tag.getText().toString());
		if (by == null || la1 == ENDTAG && !tagStack.contains(by)) {
			// error("Unknown element: " + tag.getText(), tag);
			return false;
		}
		if (!sinfo.optionalEndTag()) {
			int stacksize = tagStack.size();
			// Relax mode, outer end tag ends inline tag automatically.
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
		// HACK: end any optional on any other end tag.
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
}

%OPTIONS {
	Import = "ILLKHtmlLexer.xml";
	BuildAST = false;
	BuildVisitor = false;
	BuildVisitorAdapter = false;
	Multi = true;
	NodeDefaultVoid = false;
	NodeScopeHook = true;
	ResetHook = true;
	ExplicitNodePrefix = "";
	GenHelper = false;
}

ASTdocument document() {
	ASTdocument llkThis = new ASTdocument();
	theDocument = llkThis;
	llkThis.setFirstToken(LT1());
	handler.startDocument(llkThis);
}
{
	[ text() ]
	(
		element()
	)*
	<_EOF_>
}
{
	llkThis.setLastToken(LT0());
	handler.endDocument(llkThis);
	return llkThis;
}

void element() {
}
{
	startTag()
	|
	(
		endTag(null, true)
		|
		comment()
		|
		pi()
		|
		asp()
		|
		jste()
		|
		cdata()
		|
		cond()
		|
		(
			%LA(2)
			declaration()
			|
			doctype()
		)
	)
	[ text() ]
}

void startTag() {
	ASTstartTag llkThis = new ASTstartTag();
	llkThis.setFirstToken(LT1());
	boolean empty = false;
	boolean optmissing = false;
	HtmlTag sinfo = null;
}
{
	"<" t:<NAME> [ attrs:attributes() ] ( ">" | "/>" { empty = true; } )
	{
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
	}
	(
		%LA({ !empty })
		script()
		|
		%LA({ !empty })
		style()
		|
		[ text() ]
		(
			%LA({ !empty && LA1() != ENDTAG && !(optmissing = optionalMissing(llkThis)) })
			element()
			|
			%LA({ !empty && !(optmissing = optionalMissing(llkThis)) && extraOptional(sinfo) })
			endTag(llkThis, true)
			[ text() ]
		)*
	)
	[
		%LA({ !empty && !optmissing })
		endTag(llkThis, false)
		[ text() ]
		|
		{
			// Implicit end tag.
			if (!empty) {
				ASTendTag etag = new ASTendTag(true);
				etag.init(llkThis.getName().toString());
				etag.setFirstToken(LT1());
				etag.setLastToken(LT0());
				handler.endTag(etag);
			}
		}
	]
}

void endTag(ASTstartTag stag, boolean extra) {
	ASTendTag llkThis = new ASTendTag();
	llkThis.setFirstToken(LT1());
	ISourceText sname = (stag == null ? null : stag.getNamePosition());
	if (stag != null && LA1() != ENDTAG) {
		throw new LLKParseException(
			"Expected end tag for: " + sname.getText() + "@" + sname.getOffset(),
			llkGetLocation(sname.getOffset()));
	}
}
{
	"</" t:<NAME>
	[
		">"
		|
		{
			error(MsgId.DomGTExpected, null, LT0());
			lexer.popContext();
		}
	]
}
{
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

ASTattributes attributes() {
	ASTattributes llkThis = new ASTattributes();
	llkThis.setFirstToken(LT1());
	ILLKNode prev = null;
}
{
	(
		a:attribute()
		{
			llkThis.add(a, prev);
			prev = a;
		}
	)+
}
{
	llkThis.setLastToken(LT0());
	return llkThis;
}

ASTattribute attribute() {
	ASTattribute llkThis = new ASTattribute();
	llkThis.setFirstToken(LT1());
}
{
	t:<NAME>
	[
		"="
		[
			%GREEDY :
			<ATTVALUE | NAME>
			|
			{ relaxError(MsgId.DomMissingValue, t.getText(), null, t); }
		]
		|
		v:<STRING>
		{ relaxError(MsgId.DomMissingAssign, t.getText(), null, v); }
	]
}
{
	llkThis.setLastToken(LT0());
	return llkThis;
}

////////////////////////////////////////////////////////////////////////

void comment() {
	ASTcomment llkThis = new ASTcomment();
	llkThis.setFirstToken(LT1());
}
{
	"<!--" <COMMENT> "-->"
}
{
	llkThis.setLastToken(LT0());
	handler.comment(llkThis);
}

void pi() {
	ASTpi llkThis = new ASTpi();
}
{
	<LPI> t:<NAME> <PI> "?>"
}
{
	llkThis.setLastToken(LT0());
	llkThis.init(t);
	handler.pi(llkThis);
}

void asp() {
	ASTasp llkThis = new ASTasp();
}
{
	<LASP> <ASP> "%>"
}
{
	llkThis.setLastToken(LT0());
	handler.pi(llkThis);
}

void jste() {
	ASTjste llkThis = new ASTjste();
}
{
	<LJSTE> <JSTE> "#>"
}
{
	llkThis.setLastToken(LT0());
	handler.pi(llkThis);
}

void cdata() {
	ASTcdata llkThis = new ASTcdata();
}
{
	"<![CDATA[" <CDATA> "]]>"
}
{
	llkThis.setLastToken(LT0());
	handler.cdata(llkThis);
}

void cond() {
	ASTcond llkThis = new ASTcond();
}
{
	<LCOND> <COND> "]>"
}
{
	llkThis.setLastToken(LT0());
	handler.cond(llkThis);
}

void declaration() {
	ASTdeclaration llkThis = new ASTdeclaration();
}
{
	t:<LDECL>
	{
		ILLKToken data = (ILLKToken)t.getData();
		CharSequence name = (data == null ? "" : data.getText());
		warn(MsgId.DomUnknownDeclaration, name + "@" + t.getOffset(), null, t);
	}
	<DECLARATION> ">"
}
{
	llkThis.setLastToken(LT0());
	handler.declaration(llkThis);
}

////////////////////////////////////////////////////////////////////////

void text() {
	ASTtext llkThis = new ASTtext();
	llkThis.setFirstToken(LT1());
}
{
	<TEXT>
}
{
	llkThis.setLastToken(LT0());
	handler.text(llkThis);
}

void script() {
	ASTscript llkThis = new ASTscript();
	llkThis.setFirstToken(LT1());
}
{
	<SCRIPT>
}
{
	llkThis.setLastToken(LT0());
	handler.script(llkThis);
}

void style() {
	ASTstyle llkThis = new ASTstyle();
	llkThis.setFirstToken(LT1());
}
{
	<STYLE>
}
{
	llkThis.setLastToken(LT0());
	handler.script(llkThis);
}

////////////////////////////////////////////////////////////////////////

void doctype() {
	ASTdoctype llkThis = new ASTdoctype();
	llkThis.setFirstToken(LT1());
}
{
	<LDECL> [ name:<NAME> ] [ id:dtdId() ] ">"
}
{
	llkThis.setLastToken(LT0());
	llkThis.init(name, id);
	handler.doctype(llkThis);
}

ASTdtdId dtdId() {
	ASTdtdId llkThis = new ASTdtdId();
	llkThis.setFirstToken(LT1());
}
{
	<SYSTEM>
	sysid:<SYSTEM_LITERAL>
	|
	<PUBLIC>
	pubid:<PUBID_LITERAL>
	[ sysid:<SYSTEM_LITERAL> ]
}
{
	llkThis.setLastToken(LT0());
	llkThis.init(pubid, sysid);
	return llkThis;
}

////////////////////////////////////////////////////////////////////////
