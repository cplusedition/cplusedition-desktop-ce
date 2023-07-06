%TREEPARSER (HtmlFormatter)

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

/*
 * Copyright (c) 2005, Chris Leung. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2 with a special exception, the Classpath exception.
 * You should have received a copy of the GNU General Public License (GPL)
 * and the Classpath exception along with this library.
 */
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
}

////////////////////////////////////////////////////////////////////////

%OPTIONS {
	Import = "ILLKHtmlDomParser.xml";
	ExplicitNodePrefix = ""; // "AST"
	Validate = false;
	//
	BuildAST = false;
	BuildVisitor = false;
	BuildVisitorAdapter = false;
	VisitorException = null;
	DefaultErrorHandler = false;
	DelayConditional = false;
	Lookahead = 1;
	Multi = false;
	NodeDefaultVoid = false;
	NodeFactory = false;
	NodePrefix = "AST";
	NodeScopeHook = false;
	GenerateConstructor = "private";
	ResetHook = false;
}

////////////////////////////////////////////////////////////////////////

void document(FormatBuilder buf) {
	boolean wasbreak = false;
}
{
	[
		/* #(ASTtext */
		wasbreak=text(buf, Mode.INLINE, true, false) /* ) */
		// { wrap(buf, CONTEXT_TEXT, false); }
	]
	(
		element(buf, wasbreak, Mode.INLINE)
		[ /* #(ASTtext */ wasbreak=text(buf, Mode.INLINE, false, false) /* ) */ ]
	)*
	{
		wrap(buf, CONTEXT_TEXT, false);
		ILLKToken t = llkParent.getLastToken();
		emit(buf, t, 0, 0);
	}
	/* <_EOF_> */
}

Mode element(FormatBuilder buf, boolean wasbreak, Mode mode) #void {
}
{
	#(ASTstartTag mode=startTag(buf, mode))
	|
	/* #(ASTcomment */ mode=comment(buf, wasbreak, mode) /* ) */
	|
	/* #(ASTextraEndTag */ extraEndTag(buf, mode) /* ) */
	|
	/* #(ASTpi */ mode=pi(buf, wasbreak, mode) /* ) */
	|
	/* #(ASTasp */ mode=asp(buf, wasbreak, mode) /* ) */
	|
	/* #(ASTjste */ mode=jste(buf, wasbreak, mode) /* ) */
	|
	/* #(ASTcdata */ cdata(buf, mode) /* ) */
	|
	/* #(ASTcond */ cond(buf, mode) /* ) */
	|
	/* #(ASTdeclaration */ mode=declaration(buf, wasbreak, mode) /* ) */
	|
	#(ASTdoctype doctype(buf))
}
{ return mode; }

Mode startTag(FormatBuilder buf, Mode mode) {
	ASTstartTag stag = ((ASTstartTag)llkParent);
	HtmlTag sinfo = stag.getTag();
	Style style = formatOptions.getStyle(sinfo);
	boolean empty = stag.isEmpty();
	// boolean soft = sinfo.hasModifier(HtmlTag.C_BLOCK | HtmlTag.C_ELEMENT);
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
			// Looks dumb, but this allow tokens of attributes not in a single chain.
			for (ILLKNode attr: attrs.children())
				emitPre(b, attr);
		}
		emitPre(b, t);
	} else {
		convertTagCase(stag);
		// Break before start tag.
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
		// Emit start tag and attributes.
		if (style == Style.PREFORMATTED)
			b.setTempIndentWidth(0);
		if (attrs != null && attrs.hasChildren()) {
			boolean inline = (style == Style.INLINE || style == Style.PARAGRAPH);
			oneliner &= emitAttributes(b, t, attrs, inline);
			t = t.getNext().getNext();
		} else {
			t = emitn(b, 2, t, 0, 1);
		}
		// '>' | "/>"
		if (t.getType() == ENDEMPTY && attrs != null && attrs.hasChildren())
			b.space();
		emit(b, t, 0, 0);
		if (reformat && !oneliner)
			emitRestOfLine(b, t.getNext(), true); // soft);
		// Break before content.
		if (indent) {
			if (mode == Mode.INLINE)
				wrap(b, CONTEXT_TEXT, true); // soft);
			else
				emitRestOfLine(b, t.getNext(), true); // soft);
			if (!noindent)
				b.indent();
		}
	}
	Mode emode;
	int elms = 0;
	boolean wasspace = true;
	boolean wasbreak = false;
}
{
	// "<" <NAME> [ #(ASTattributes attributes(buf)) ] ( ">" | "/>" )
	[
		/* #(ASTscript */ script(b, cmode) /* ) */
		{ oneliner = false; }
		|
		/* #(ASTstyle */ style(b, cmode) /* ) */
		{ oneliner = false; }
		|
		%LA(0, { !empty && notEndTag() })
		(
			%LA(0, { notEndTag() })
			[ wasbreak=text(b, cmode, wasspace, false /* soft */ ) ]
			emode=element(b, wasbreak, cmode)
			{
				wasbreak = false;
				wasspace = false;
				++elms;
				if (emode != Mode.INLINE
					|| !(style == Style.INLINE || style == Style.PARAGRAPH || style == Style.BLOCK)
						&& elms > 1)
					oneliner = false;
			}
		)+
	]
	[
		[ text(b, cmode, wasspace, true /* soft) */ ) ]
		{
			if (indent) {
				t = lt1First();
				if (cmode == Mode.INLINE)
					wrap(b, CONTEXT_TEXT, true);
				else
					emitRestOfLine(b, t, true);
				if (!noindent)
					b.unIndent();
			}
		}
		/* #(ASTendTag */ endTag(b, cmode) /* ) */
		{
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
	]
}
{
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

void endTag(FormatBuilder buf, Mode mode) {
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
}
{
	//	/* "</" */
	//	/* <NAME> */
	//	[
	//	/* ">" */
	//	]
	#(ASTendTag)
}

void extraEndTag(FormatBuilder buf, Mode mode) {
	ILLKToken t = LT1.getFirstToken();
	if (mode == Mode.PRE)
		emitPre(buf, LT1);
	else {
		convertTagCase((ASTendTag)LT1);
		emit(buf, t, LT1.getEndOffset(), 0, 1);
	}
}
{
	//	/* "</" */
	//	/* <NAME> */
	//	[
	//	/* ">" */
	//	]
	#(ASTextraEndTag)
}

////////////////////////////////////////////////////////////////////////

Mode comment(FormatBuilder buf, boolean wasbreak, Mode mode) {
	// Fix any nonstandard start comment tag.
	if (formatOptions.isFixing) {
		LT1.getFirstToken().setText("<!--");
		LT1.getLastToken().setText("-->");
	}
	mode = emitComment(buf, LT1, wasbreak, mode);
}
{
	//	/* "<!--" */
	//	/* <COMMENT> */
	//	/* "-->" */
	#(ASTcomment)
}
{ return mode; }

Mode pi(FormatBuilder buf, boolean wasbreak, Mode mode) {
	mode = emitPI(buf, LT1, wasbreak, mode);
}
{
	//	/* <LPI> */
	//	/* <PI> */
	//	/* "?>" */
	#(ASTpi)
}
{ return mode; }

Mode asp(FormatBuilder buf, boolean wasbreak, Mode mode) {
	emitPI(buf, LT1, wasbreak, mode);
}
{
	//	/* <LASP> */
	//	/* <ASP> */
	//	/* "%>" */
	#(ASTasp)
}
{ return mode; }

Mode jste(FormatBuilder buf, boolean wasbreak, Mode mode) {
	emitPI(buf, LT1, wasbreak, mode);
}
{
	//	/* <LJSTE> */
	//	/* <JSTE> */
	//	/* "#>" */
	#(ASTjste)
}
{ return mode; }

Mode declaration(FormatBuilder buf, boolean wasbreak, Mode mode) {
	emitDeclaration(buf, LT1, wasbreak, mode);
}
{
	//	/* <LDECL> */
	//	/* <DECLARATION> */
	//	/* ">" */
	#(ASTdeclaration)
}
{ return mode; }

void cdata(FormatBuilder buf, Mode mode) {
	emitCData(buf, LT1, mode);
}
{
	//	/* "<![CDATA[" */
	//	/* <CDATA> */
	//	/* "]]>" */
	#(ASTcdata)
}

void cond(FormatBuilder buf, Mode mode) {
	emitCond(buf, LT1, mode);
}
{
	//	/* "<![" */
	//	/* <COND> */
	//	/* "]>" */
	#(ASTcond)
}

//void attributes(FormatBuilder buf) {
//	boolean indented = false;
//}
//{
//	(
//		/* #(ASTattribute */ attribute(buf, indented) /* ) */
//		{ indented = true; }
//	)+
//	{ buf.unIndentAfterBreak(); }
//}
//
//void attribute(FormatBuilder buf, boolean indented) {
//	convertAttrCase(LT1);
//	emitAttribute(buf, LT1, indented);
//}
//{
//	//	/* <NAME> */
//	//	[
//	//		/* "=" */
//	//		/* <ATTVALUE | NAME> */
//	//		|
//	//		/* <STRING> */
//	//	]
//	#(ASTattribute)
//}

boolean text(FormatBuilder buf, Mode mode, boolean ltrim, boolean rtrim) {
	boolean hasbreak = false;
}
{
	//	(
	//	/* <TEXT> */
	//	)+
	(
		{ hasbreak |= emitText(buf, LT1, mode, ltrim, rtrim); }
		#(ASTtext)
	)+
}
{ return hasbreak; }

void script(FormatBuilder buf, Mode mode) :
{
	{ emitScript(buf, LT1, mode); }
	/* <SCRIPT> */
	#(ASTscript)
}

void style(FormatBuilder buf, Mode mode) :
{
	{ emitStyle(buf, LT1, mode); }
	/* <STYLE> */
	#(ASTstyle)
}

////////////////////////////////////////////////////////////////////////

void doctype(FormatBuilder buf) {
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
}
{
	//	/* <LDECL> */
	//	[
	//	/* <NAME> */
	//	]
	[ /* #(ASTdtdId */ dtdId(b) /* ) */ ]
	/* ">" */
	{
		t = llkParent.getLastToken();
		if (b != null) {
			emit(b, t, 0, 0);
			format(buf, b, CONTEXT_DTD);
		}
		emitRestOfLine(buf, t.getNext());
	}
}

void dtdId(FormatBuilder buf) :
{
	{ emitDtdId(buf, LT1); }
	//	/* <SYSTEM> */
	//	/* <SYSTEM_LITERAL> */
	//	|
	//	/* <PUBLIC> */
	//	/* <PUBID_LITERAL> */
	//	[
	//	/* <SYSTEM_LITERAL> */
	//	]
	#(ASTdtdId)
}

////////////////////////////////////////////////////////////////////////
