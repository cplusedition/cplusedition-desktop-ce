%PARSER (HtmlDomParser)

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

/*
 * Copyright (c) 2005, Chris Leung. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2 with a special exception, the Classpath exception.
 * You should have received a copy of the GNU General Public License (GPL)
 * and the Classpath exception along with this library.
 */
public class HtmlDomParser extends LLKParserBase implements ILLKParser, ILLKHtmlDomParser {

	////////////////////////////////////////////////////////////////////////

	//	private static final boolean DEBUG = false;

	// protected char[] source;
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
		// source = llkGetSource();
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

	//	private void checkLiteral(String expected) {
	//		ILLKToken lt0 = LT0();
	//		if (!expected.equals(lt0.getText()))
	//			error(
	//				MsgId.DomUnexpectedLiteral,
	//				"expected=" + expected + ", actual=" + lt0.getText(),
	//				null,
	//				lt0.getOffset());
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
	BuildAST = true; // false
	BuildVisitor = true; // false;
	BuildVisitorAdapter = true; // false;
	DefaultErrorHandler = false;
	DelayConditional = false;
	ExplicitNodePrefix = ""; // "AST";
	Greedy = false;
	Lookahead = 1;
	Multi = true; // false;
	NodeDefaultVoid = false;
	NodeFactory = false;
	NodePrefix = "AST";
	NodeScopeHook = true; // false;
	GenerateConstructor = "private";
	ResetHook = true; // false;
	VisitorException = null;
	GenHelper = false;
	// Debug options
	GreedyMatch = false;
}

ASTdocument document() :
{
	[ text() ]
	(
		element()
	)*
	<_EOF_>
}
{ return llkThis; }

void element() #void :
{
	startTag()
	|
	(
		extraEndTag(null)
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
	boolean empty = false;
	boolean optmissing = false;
	boolean extraoptional = false;
	HtmlTag sinfo = null;
	ILLKNode text = null;
	ILLKToken last = null;
}
{
	"<" t:<NAME>
	[
		attrs:attributes()
		{ llkTree.pop(); }
	]
	( ">" | "/>" { empty = true; } )
	{
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
		//
		sinfo = llkThis.getTag();
		if (sinfo == HtmlTag.UNKNOWN ) {
			ISourceText sname = llkThis.getNamePosition();
			relaxError(MsgId.DomUnknownStartTag, sname.getText(), null, sname);
		}
		if (!empty)
			tagStack.push(sinfo);
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
			%LA(
			{
				!empty
					&& !(optmissing = optionalMissing(llkThis))
					&& (extraoptional = extraOptional(sinfo))
			})
			extraEndTag(extraoptional ? null : llkThis)
			[ text() ]
		)*
	)
	[
		%LA({ !empty && !optmissing })
		endTag(llkThis)
		[ text() ]
		|
		{
			if (!empty) {
				ASTendTag etag = new ASTendTag(true);
				etag.init(llkThis.getName().toString());
				etag.setFirstToken(LT1());
				etag.setLastToken(LT0());
				llkTree.push(etag);
			}
		}
	]
	{
		// Move any trailing child TEXT node as sibling.
		if (llkTree.childCount() > 0) {
			ILLKNode n = llkTree.peek();
			if (n.getType() == ASTtext) {
				llkTree.pop();
				text = n;
				if (llkTree.childCount() > 0)
					last = llkTree.peek().getLastToken();
			}
		}
	}
}
{
	if (text != null) {
		llkThis.setLastToken(last);
		llkTree.push(text);
	}
}

void endTag(ASTstartTag stag) {
	ISourceText stoken = stag.getNamePosition();
	String sname = stoken.getText().toString();
	if (LA1() != ENDTAG) {
		throw new LLKParseException(
			"Expected end tag for: " + sname + "@" + stoken.getOffset(), llkGetLocation(stoken.getOffset()));
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
	{
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
	}
}

void extraEndTag(ASTstartTag stag) {
	ISourceText stoken = (stag == null ? null : stag.getNamePosition());
	if (stag != null && LA1() != ENDTAG) {
		throw new LLKParseException(
			"Expected end tag for: " + stoken.getText() + "@" + stoken.getOffset(),
			llkGetLocation(stoken.getOffset()));
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
	llkThis.init(t, true);
	String ename = t.getText().toString();
	HtmlTag info = HtmlTag.get(ename);
	warn((info.isEmpty() ? MsgId.DomEndEmptyTag : MsgId.DomExtraEndTag), ename + "@" + t.getOffset(), null, t);
	return;
}

void comment() :
{
	"<!--"
	// { LT0().setText("<!--"); }
	<COMMENT>
	"-->"
	// { LT0().setText("-->"); }
}

void pi() :
{
	<LPI> <PI> "?>"
}

void asp() :
{
	<LASP> <ASP> "%>"
}

void jste() :
{
	<LJSTE> <JSTE> "#>"
}

void cdata() :
{
	"<![CDATA[" <CDATA> "]]>"
}

void cond() :
{
	<LCOND> <COND> "]>"
}

void declaration() :
{
	t:<LDECL>
	{
		ILLKToken data = (ILLKToken)t.getData();
		CharSequence name = (data == null ? "" : data.getText());
		warn(MsgId.DomUnknownDeclaration, name + "@" + t.getOffset(), null, t);
	}
	<DECLARATION> ">"
}

ASTattributes attributes() :
{
	attribute()+
}
{ return llkThis; }

void attribute() :
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

void text() :
{
	<TEXT>+
}

void script() :
{
	<SCRIPT>
}

void style() :
{
	<STYLE>
}

void doctype() :
{
	<LDECL>
	[ t:<NAME> ]
	[ id:dtdId() ] ">"
}
{ llkThis.init(t, id); }

ASTdtdId dtdId() :
{
	<SYSTEM>
	sysid:<SYSTEM_LITERAL>
	|
	<PUBLIC>
	pubid:<PUBID_LITERAL>
	[ sysid:<SYSTEM_LITERAL> ]
}
{
	llkThis.init(pubid, sysid);
	return llkThis;
}
