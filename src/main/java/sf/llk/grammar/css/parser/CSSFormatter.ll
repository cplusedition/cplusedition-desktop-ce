%OPTIONS {
	Language = "Java";
}

%PARSER (CSSFormatter)

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

/*
 * Copyright (c) 2003-2005, Chris Leung. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2 with a special exception, the Classpath exception.
 * You should have received a copy of the GNU General Public License (GPL)
 * and the Classpath exception along with this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 */
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

	//	private void llkOpenNode(LLKNode node) {
	//		llkTree.open();
	//		node.firstToken = LT(1);
	//	}
	//
	//	private void llkCloseNode(LLKNode node, boolean create) {
	//		llkTree.close(node, create);
	//		if (create) {
	//			if (LT(1) == node.firstToken) {
	//				node.firstToken = null;
	//				node.lastToken = null;
	//			} else {
	//				node.lastToken = LT0();
	//			}
	//		}
	//	}

	////////////////////////////////////////////////////////////////////////

	public String getLineno() {
		return "@" + (llkInput.getLocator().getLinear(LT0().getOffset()));
	}

	public ILLKMain getMain() {
		return llkMain;
	}

	////////////////////////////////////////////////////////////////////////
}

%OPTIONS {
	Import = "ILLKCSSParser.xml";
	BuildVisitor = false;
	BuildAST = false;
	Multi = false;
	NodeDefaultVoid = false;
	NodeScopeHook = false;
	ResetHook = true;
	ExplicitNodePrefix = "";
	GenerateConstructor = "private";
}

private void s() #void {
	ILLKToken lt0 = LT0();
	llkInput.mark();
	ILLKToken start = null;
}
{
	(
		%LA = GREEDY :
		<SPACES | NEWLINE | COMMENT>
		{
			if (start == null)
				start = LT0();
		}
	)+
}
{
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

private void ss() #void {
	ILLKToken lt0 = LT0();
	llkInput.mark();
	ILLKToken start = null;
}
{
	(
		%LA = GREEDY :
		<SPACES | NEWLINE | COMMENT>
		{
			if (start == null)
				start = LT0();
		}
	)*
}
{
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

private void ssc() #void {
	ILLKToken lt0 = LT0();
	llkInput.mark();
	ILLKToken start = null;
}
{
	(
		%LA = GREEDY :
		<SPACES | NEWLINE | COMMENT | CDO | CDC>
		{
			if (start == null)
				start = LT0();
		}
	)*
}
{
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

// stylesheet
//   : [ CHARSET_SYM S* STRING S* ';' ]?
//     [S|CDO|CDC]* [ import [S|CDO|CDC]* ]*
//     [ [ ruleset | media | page | font_face ] [S|CDO|CDC]* ]*
//   ;
void styleSheet(FormatBuilder buf)  #void:
{
	[ charset(buf) ]
	ssc()
	(
		Import(buf)
		ssc()
	)*
	(
		( ruleset(buf) | media(buf) | page(buf) | font_face(buf)  | keyframes(buf) | atRule(buf) )
		ssc()
	)*
	<_EOF_>
	{
		emitSpecial(buf, LT0(), 0, 1);
		buf.flushLine();
		buf.rtrimBlankLines();
	}
}

void charset(FormatBuilder buf)  #void {
	flushSpecial(buf, 1, 2);
}
{
	<CHARSET> { emit(buf, 1); }
	ss() <STRING> { emit(buf, 1); }
	ss() ";" { emit(buf); }
}

// import
//   : IMPORT_SYM S*
//     [STRING|URI] S* [ medium [ ',' S* medium]* ]? ';' S*
//   ;
void Import(FormatBuilder buf)  #void {
	flushSpecial(buf, 1, 2);
}
{
	try {
		"@import"
		{
			emitSpecial(buf, LT0(), 1);
			emitText(buf);
		}
		ss()
		(
			<STRING> { emit(buf, 1); }
			|
			<URI> { emit(buf, 1); }
		)
		ss()
		(
			medium(buf)
			(
				"," { emit(buf); }
				ss()
				medium(buf)
			)*
		)?
		";" { emit(buf); }
	} catch (Throwable e) {
		ILLKToken lt0 = LT0();
		ILLKToken last = skipTokens(0);
		warn(MsgId.ParserInvalidConstructIgnored, e, lt0, last);
		emitTokens(buf, lt0, last);
	}
}

// media
//   : MEDIA_SYM S* medium [ ',' S* medium ]* '{' S* ruleset* '}' S*
//   ;
void media(FormatBuilder buf)  #void {
	ILLKToken t;
	flushSpecial(buf, 1, 2);
	FormatBuilder b = startSession(buf);
	int level = 0;
}
{
	try {
		"@media"
		{
			emitSpecial(b, LT0(), 1);
			emit(b, 1);
		}
		ss() [ media_query_list(b) ]
        // 		ss() medium(b)
        // 		(
        // 			"," ss()
        // 			{
        // 				emit(b);
        // 				emitRestOfLine(b, LT1());
        // 			}
        // 			medium(b)
        // 		)*
 		t="{" { ++level; }
 		ss()
		{
			endSession(buf, b);
			startBlock(buf, t);
		}
		( ruleset(buf) ss() )*
		t="}" { --level; }
		(
			%LA = GREEDY :
			ss()
		)?
		{ endBlock(buf, t); }
	} catch (Throwable e) {
		ILLKToken lt0 = LT0();
		ILLKToken last = skipTokens(level);
		warn(MsgId.ParserInvalidConstructIgnored, e, lt0, last);
		emitTokens(buf, lt0, last);
	}
}

// media_query_list
//  : S* [media_query [ ',' S* media_query ]* ]?
//  ;
void media_query_list(FormatBuilder buf)  #void:
{
    media_query(buf)
    (
        "," { emit(buf); }
        ss()
        media_query(buf)
    )*
}

// media_query
//  : [ONLY | NOT]? S* media_type S* [ AND S* expression ]*
//  | expression [ AND S* expression ]*
//  ;
void media_query(FormatBuilder buf)  #void:
{
    [
        ("only" | "not") { emit(buf, 1); }
        ss()
    ]
    media_type(buf)
    ss()
    (
        "and" { emit(buf, 1); }
        media_expr(buf)
    )*
    |
    media_expr(buf)
    (
        "and" { emit(buf, 1); }
        ss()
        media_expr(buf)
    )*
}

// media_type
//  : IDENT
//  ;
void media_type(FormatBuilder buf)  #void:
{
    <IDENTIFIER> { emit(buf, 1); }
}

// expression
//  : '(' S* media_feature S* [ ':' S* expr ]? ')' S*
//  ;
void media_expr(FormatBuilder buf) #void:
{
    "(" { emit(buf, 1); }
    ss()
    media_feature(buf)
    ss()
    [
        ":" { emit(buf, 1); }
        ss()
        expr(buf, 1)
    ]
    ")" { emit(buf, 1); }
    ss()
}

// media_feature
//  : IDENT
//  ;
void media_feature(FormatBuilder buf) #void:
{
    <IDENTIFIER> { emit(buf, 1); }
}

// medium
//   : IDENT S*
//   ;
void medium(FormatBuilder buf) #void:
{
	Identifier() { emit(buf, 1); }
	ss()
}

// keyframes_rule: KEYFRAMES_SYM S+ IDENT S* '{' S* keyframes_blocks '}' S*;
void keyframes(FormatBuilder buf) #void{
    ILLKToken t;
	flushSpecial(buf, 1, 2);
}
{
    <"@keyframes" | "@-webkit-keyframes"> { emit(buf, 1); }
    ss()
    <IDENTIFIER> { emit(buf, 1); }
    ss()
    t="{" ss() { startBlock(buf, t); }
    (
        keyframes_block(buf)
    )*
    t="}" ss() { endBlock(buf, t); }
}

// keyframes_blocks: [ keyframe_selector '{' S* declaration? [ ';' S* declaration? ]* '}' S* ]* ;
void keyframes_block(FormatBuilder buf) #void{
    ILLKToken t;
}
{
    keyframe_selector(buf)
    t = "{" ss() { startBlock(buf, t); }
    [
        declaration(buf)
        ss()
    ]
    (
        ";" ss()
        {
            emit(buf);
            emitRestOfLine(buf, LT1());
        }
        [
            declaration(buf)
            ss()
        ]
    )*
    t="}" ss() { endBlock(buf, t); }
}

// keyframe_selector: [ FROM_SYM | TO_SYM | PERCENTAGE ] S* [ ',' S* [ FROM_SYM | TO_SYM | PERCENTAGE ] S* ]*;
void keyframe_selector(FormatBuilder buf) #void:
{
    (
        "from" { emit(buf, 1); }
        | "to" { emit(buf, 1); }
        | Number(buf, 1)
        "%" { emit(buf); }
    )
    ss()
    (
        "," { emit(buf); }
        ss()
        (
            "from" { emit(buf, 1); }
            | "to" { emit(buf, 1); }
            | Number(buf, 1)
            "%" { emit(buf); }
        )
        ss()
    )*
}

// page
//   : PAGE_SYM S* IDENT? pseudo_page? S*
//     '{' S* declaration [ ';' S* declaration ]* '}' S*
//   ;
void page(FormatBuilder buf) #void{
	ILLKToken t;
	flushSpecial(buf, 1, 2);
	FormatBuilder b = formatOptions.isCompact() ? startSession(buf) : buf;
	int level = 0;
}
{
	try {
		"@page"
		{
			emitSpecial(b, LT0(), 1);
			emit(b, 1);
		}
		ss()
		[
			Identifier() { emit(b, 1); }
		]
		pseudo_page(b) ss()
		t="{" { ++level; }
		ss() { startBlock(b, t); }
		[ declaration(b) ss() ]
		(
			";" { emit(b); }
			ss()
			{
				flushSpecial(b, 1, 1);
				b.flushLine();
			}
			[ declaration(b) ss() ]
		)*
		t="}" { --level; }
		(
			%LA = GREEDY :
			ss()
		)? { endBlock(b, t); }
	} catch (Throwable e) {
		ILLKToken lt0 = LT0();
		ILLKToken last = skipTokens(level);
		warn(MsgId.ParserInvalidConstructIgnored, e, lt0, last);
		emitTokens(b, lt0, last);
	}
}
{
	if (b != buf)
		endSession(buf, b);
}

// pseudo_page
//   : ':' IDENT
//   ;
void pseudo_page(FormatBuilder buf) #void:
{
	":" { emit(buf); }
	Identifier() { emit(buf); }
}

// font_face
//   : FONT_FACE_SYM S*
//     '{' S* declaration [ ';' S* declaration ]* '}' S*
//   ;
void font_face(FormatBuilder buf) #void{
	ILLKToken t;
	flushSpecial(buf, 1, 2);
	FormatBuilder b = formatOptions.isCompact() ? startSession(buf) : buf;
	int level = 0;
}
{
	try {
		"@font-face"
		{
			emitSpecial(b, LT0(), 1);
			emit(b, 1);
		}
		ss()
		t="{" { ++level; }
		ss() { startBlock(b, t); }
		[ declaration(b) ss() ]
		(
			";" { emit(b); }
			ss()
			{
				flushSpecial(b, 1, 1);
				b.flushLine();
			}
			[ declaration(b) ss() ]
		)*
		t="}" { --level; }
		(
			%LA = GREEDY :
			ss()
		)? { endBlock(b, t); }
	} catch (Throwable e) {
		ILLKToken lt0 = LT0();
		ILLKToken last = skipTokens(level);
		warn(MsgId.ParserInvalidConstructIgnored, e, lt0, last);
		emitTokens(b, lt0, last);
	}
}
{
	if (b != buf)
		endSession(buf, b);
}

void atRule(FormatBuilder buf) #void{
	flushSpecial(buf, 1, 2);
}
{
	t:<AT_KEYWORD>
	{
		ILLKToken last = skipTokens(0);
		warn(MsgId.ParserInvalidConstructIgnored, null, t, last);
		emitTokens(buf, t, last);
	}
}

// operator
//   : '/' S* | ',' S* | // empty
//   ;
void operator(FormatBuilder buf, int level) #void {
}
{
	[
        %LA(<"+" | DASH> ss())
        <"+" | DASH> { emit(buf); }
        ss()
        |
        <SLASH | "*" > { emit(buf); }
        ss()
        |
        ","
        ss()
        {
            emit(buf);
            flushSpecial(buf, 0, 1);
            buf.flushLine();
            if (buf.getIndentLevel() == level) buf.indent();
        }
	]
}

// combinator
//   : '+' S* | '>' S* | // empty
//   ;
void combinator(FormatBuilder buf) #void:
{
	[
        (
            <"+"  | ">" | "~" | "||"> { emit(buf, 1); }
             |
              <SLASH> { emit(buf, 1); }
             [
        	    %LA(<IDENTIFIER> <SLASH>)
                <IDENTIFIER>  { emit(buf); }
                <SLASH> { emit(buf); }
             ]
        )
		ss()
	]
}

// unary_operator
//   : '-' | '+'
//   ;
void unary_operator(FormatBuilder buf, int spaces) #void:
{
	<DASH> | "+"
}
{ emit(buf, spaces); }

// property
//   : IDENT S*
//   ;
void property(FormatBuilder buf) #void:
 {
	Identifier()
	{
		emit(buf, 1);
        checkPropertyName(LT0());
	}
}

// ruleset
//   : selector [ ',' S* selector ]*
//     '{' S* declaration [ ';' S* declaration ]* '}' S*
//   ;
void ruleset(FormatBuilder buf) #void {
	ILLKToken t;
	flushSpecial(buf, 1, 2);
	FormatBuilder b = formatOptions.isCompact() ? startSession(buf) : buf;
	FormatBuilder bb = startSession(b);
	int level = 0;
}
{
	try {
		selector(bb, 0)
		(
			"," ss()
			{
				emit(bb);
				emitRestOfLine(bb, LT1());
			}
			[ selector(bb, 0) ]
		)*
		t="{" { ++level; }
		ss()
		{
			endSession(b, bb);
			startBlock(b, t);
		}
		[ declaration(b) ss() ]
		(
			";" { emit(b); }
			ss()
			{
				flushSpecial(b, 1, 1);
				b.flushLine();
			}
			[ declaration(b) ss() ]
		)*
		t="}" { --level; }
		(
			%LA = GREEDY :
			ss()
		)?
		{ endBlock(b, t); }
	} catch (Throwable e) {
		ILLKToken lt0 = LT0();
		ILLKToken last = skipTokens(level);
		warn(MsgId.ParserInvalidConstructIgnored, e, lt0, last);
		emitTokens(b, lt0, last);
	}
}
{
	if (b != buf)
		endSession(buf, b);
}

// selector
//   : simple_selector [ combinator simple_selector ]*
//   ;
void selector(FormatBuilder buf, int spaces) #void:
{
	simple_selector(buf, spaces)
	( combinator(buf) simple_selector(buf, 1) )*
}

// simple_selector
//   : element_name [ HASH | class | attrib | pseudo ]* S*
//   | HASH [ class | attrib | pseudo ]* S*
//   | pseudo [ HASH | class | attrib | pseudo ]* S*
//   | class [ HASH | class | attrib | pseudo ]* S*
//   ;
void simple_selector(FormatBuilder buf, int spaces) #void:
{
	(
		qname(buf, spaces)
		(
			%LA = GREEDY :
			<HASH> { emit(buf); }
			| Class(buf, 0) | pseudo(buf, 0) | attrib(buf, 0)
		)*
		|
		<HASH> { emit(buf, 1); }
		(
			%LA = GREEDY :
			<HASH> { emit(buf); }
			| Class(buf, 0) | pseudo(buf, 0) | attrib(buf, 0)
		)*
		|
		pseudo(buf, spaces)
		(
			%LA = GREEDY :
			<HASH> { emit(buf); }
			| Class(buf, 0) | pseudo(buf, 0) | attrib(buf, 0)
		)*
		|
		Class(buf, spaces)
		(
			%LA = GREEDY :
			<HASH> { emit(buf); }
			| Class(buf, 0) | pseudo(buf, 0) | attrib(buf, 0)
		)*
	)
	ss()
}

// class
//   : '.' IDENT
//   ;
void Class(FormatBuilder buf, int spaces) #void:
{
	<DOT> { emit(buf, spaces); }
	Identifier() { emit(buf); }
}

// element_name
//   : IDENT | '*'
//   ;
void qname(FormatBuilder buf, int spaces) #void:
{
	( Identifier() | "*" ) { emit(buf, spaces); }
	[
	    "|" { emit(buf); }
	    (Identifier() | "*") { emit(buf); }
	]
}

// attrib
//   : '[' S* IDENT S* [ [ '=' | MATCH ] S*
//     [ IDENT | STRING ] S* ]? ']'
//   ;
void attrib(FormatBuilder buf, int spaces) #void:
{
	"[" { emit(buf, spaces); }
	ss()
	Identifier() { emit(buf); }
	ss()
	(
		(
			"="
			| <WORDMATCH | LANGMATCH | HEADMATCH | TAILMATCH | SUBSTRMATCH>
		)
		{ emit(buf); }
		ss()
		(
			Identifier()
			|
			<STRING>
		)
		{ emit(buf); }
		ss()
	)?
	"]" { emit(buf); }
}

// pseudo
//   : ':' [ IDENT | FUNCTION S* IDENT S* ')' ]
//   ;
void pseudo(FormatBuilder buf, int spaces) #void:
{
	":" { emit(buf, spaces); }
	[ %LA(":") ":" { emit(buf); } ]
	(
		%GREEDY :
		Identifier() { emit(buf); }
		|
		<FUNCTION> { emit(buf); }
		ss()
		(
			// HACK: extension, should be Identifier()
			selector(buf, 0)
            ss()
            (
			    "," { emit(buf); }
			    ss()
			    selector(buf, 1)
                ss()
			)*
			|
			attrib(buf, 0)
            ss()
		)
		")" { emit(buf); }
	)?
}

// declaration
//   : property ':' S* expr prio?
//   | // empty
//   ;
void declaration(FormatBuilder buf) #void:
{
	try {
        property(buf) ss() ":" { emit(buf); }
        ss() [ expr(buf, 1) ]
        [ prio(buf) ]
	} catch (Exception e) {
		ILLKToken lt0 = LT0();
		ILLKToken last = skipTo(new int[] { SEMICOLON, RBRACE });
		warn(MsgId.ParserInvalidConstructIgnored, e, lt0, last);
		emitTokens(buf, lt0, last);
	}
}

// prio
//   : IMPORTANT_SYM S*
//   ;
void prio(FormatBuilder buf) #void:
{
	"!" { emit(buf, 1); }
	ss()
	"important" { emit(buf); }
	ss()
}

// expr
//   : term [ [ operator ] term ]*
//   ;
void expr(FormatBuilder buf, int spaces) #void {
    int level = buf.getIndentLevel();
	FormatBuilder b = startSession(buf);
}
{
	term(b, spaces) ( operator(b, level) term(b, 1) )*
}
{
    endSession(buf, b);
}

// term
//   : unary_operator?
//     [ NUMBER S* | PERCENTAGE S* | LENGTH S* | EMS S* | EXS S* | ANGLE S* | TIME S* | FREQ S* | function ]
//   | STRING S* | IDENT S* | URI S* | RGB S* | UNICODERANGE S* | hexcolor
//   ;
void term(FormatBuilder buf, int spaces) #void:
{
	(
	    unary_operator(buf, spaces) units(buf, 0)
        |
        units(buf, spaces)
        |
        <STRING | URI | UNICODE_RANGE> { emit(buf, spaces); }
    	|
        Identifier() { emit(buf, spaces); }
        |
        color(buf, spaces)
        |
        gradient(buf, spaces)
	)
	 ss()
}

private void units(FormatBuilder buf, int spaces) #void: {
	<DECIMAL | REAL | HEX> { emit(buf, spaces); }
	(
		%LA = GREEDY :
		<PERCENT | LENGTH | "em" | "ex" | ANGLE | TIME | FREQ>
		{ emit(buf); }
	)?
	|
	varref(buf, spaces)
	|
	calc(buf, spaces)
    |
    attr(buf, spaces)
    |
    function(buf, spaces)
}

private void varref(FormatBuilder buf, int spaces) #void: {
    <VAR> { emit(buf, spaces); }
    ss() <DASHDASH>  { emit(buf); }
    <IDENTIFIER> { emit(buf); }
    ss() ")" { emit(buf); }
}

void color(FormatBuilder buf, int spaces) #void:
{
	hexcolor(buf, spaces)
	|
	<RGB> { emit(buf, spaces); }
	ss() Number(buf, 0)
	ss() "," { emit(buf); }
	ss() Number(buf, 1)
	ss() "," { emit(buf); }
	ss() Number(buf, 1)
	ss() ["," { emit(buf); } ss() ]
	")" { emit(buf); }
	|
	<RGBA> { emit(buf, spaces); }
	ss() Number(buf, 0)
	ss() "," { emit(buf); }
	ss() Number(buf, 1)
	ss() "," { emit(buf); }
	ss() Number(buf, 1)
	ss() "," { emit(buf); }
	ss() Number(buf, 1)
	ss() ["," { emit(buf); } ss() ]
	")" { emit(buf); }
}

// <linear-gradient> = linear-gradient(
// 	[ [ <angle> | to <side-or-corner> ] ,]?
// 	<color-stop>[, <color-stop>]+
// )
private void gradient(FormatBuilder buf, int spaces)  #void: {
    linear_gradient(buf, spaces)
    |
    radial_gradient(buf, spaces)
}

void linear_gradient(FormatBuilder buf, int spaces) #void:
{
    <LINEAR_GRADIENT | REPEATING_LINEAR_GRADIENT> { emit(buf, spaces); }
    ss()
    [
        (
            <DECIMAL | REAL | HEX> { emit(buf); }
            <ANGLE> { emit(buf); }
            |
            "to" { emit(buf); }
            ss()
            side_or_corner(buf)
        )
        ss() ","  { emit(buf); }
        ss()
    ]
    color_stop(buf, lt0Type(LINEAR_GRADIENT, REPEATING_LINEAR_GRADIENT) ? 0 : 1)
    (
        "," { emit(buf); }
        ss()
        color_stop(buf, 1)
    )+
    ")" { emit(buf); }
}

// <radial-gradient> = radial-gradient(
//   [ [ <shape> || <size> ] [ at <position> ]? , |
//     at <position>,
//   ]?
//   <color-stop> [ , <color-stop> ]+
// )
// <extent-keyword> = closest-corner | closest-side | farthest-corner | farthest-side
void radial_gradient(FormatBuilder buf, int spaces) #void:
{
    <RADIAL_GRADIENT | REPEATING_RADIAL_GRADIENT> { emit(buf, spaces); }
    ss()
    [
        [
            <SHAPE> { emit(buf); }
            ss()
            [
                (
                    length_or_percent(buf, 1)
                    |
                    <EXTENT_KEYWORD> { emit(buf, 1); }
                )
                ss()
                [
                    length_or_percent(buf, 1)
                    ss()
                ]
            ]
            |
            (
                length_or_percent(buf, 0)
                |
                <EXTENT_KEYWORD> { emit(buf, 0); }
            )
            ss()
            [
                length_or_percent(buf, 1)
                ss()
            ]
            [
                <SHAPE> { emit(buf, 1); }
                ss()
            ]
        ]
        [
            "at" { emit(buf, lt0Type(RADIAL_GRADIENT, REPEATING_RADIAL_GRADIENT) ? 0 : 1); }
            ss() position(buf, 1)
            ss()
        ]
        "," { emit(buf); }
        ss()
    ]
    color_stop(buf, lt0Type(RADIAL_GRADIENT, REPEATING_RADIAL_GRADIENT) ? 0 : 1)
    (
        "," { emit(buf); }
        ss() color_stop(buf, 1)
    )+
    ")" { emit(buf); }
}

// <side-or-corner> = [left | right] || [top | bottom]
void side_or_corner(FormatBuilder buf) #void:
{
    <SIDE> { emit(buf, 1); }
    [ <SIDE> { emit(buf, 1); } ]
}

// <position> = [
//   [ left | center | right | top | bottom | <percentage> | <length> ]
// |
//   [ left | center | right | <percentage> | <length> ]
//   [ top | center | bottom | <percentage> | <length> ]
// |
//   [ center | [ left | right ] [ <percentage> | <length> ]? ] &&
//   [ center | [ top | bottom ] [ <percentage> | <length> ]? ]
// ]
void position(FormatBuilder buf, int spaces) #void:
{
    position1(buf, spaces)
    [
        %LA(s() ~",")
        s() position1(buf, 1)
    ]
}

// <color-stop> = <color> [ <percentage> | <length> ]?
void color_stop(FormatBuilder buf, int spaces) #void:
{
    (
        <IDENTIFIER> { emit(buf, spaces); }
        |
        color(buf, spaces)
    )
    ss() [ length_or_percent(buf, 1) ss() ]
}

private void position1(FormatBuilder buf, int spaces)  #void: {
    <CENTER | SIDE> { emit(buf, spaces); }
    |
    length_or_percent(buf, spaces)
}

private void length_or_percent(FormatBuilder buf, int spaces)  #void: {
    [
        <DASH | PLUS> { emit(buf, spaces); spaces = 0; }
    ]
    <DECIMAL | REAL | HEX> { emit(buf, spaces); }
    <PERCENT | LENGTH> { emit(buf); }
    |
    calc(buf, spaces)
}

// math    : calc S*;
// calc    : "calc(" S* sum S* ")";
void calc(FormatBuilder buf, int spaces) #void:
{
	<CALC> { emit(buf, spaces); }
	ss() sum(buf, 0)
	ss() ")" { emit(buf); }
}

// sum     : product [ S+ [ "+" | "-" ] S+ product ]*;
void sum(FormatBuilder buf, int spaces) #void:
{
   product(buf, spaces)
   (
        %LA(s() <"+" | DASH>)
        s() <"+" | DASH>  { emit(buf, 1); }
        s() product(buf, 1)
   )*
}

// product : unit [ S* [ "*" S* unit | "/" S* NUMBER ] ]*;
void product(FormatBuilder buf, int spaces) #void:
{
    unit(buf, spaces) (
        %LA(ss() <"*" | SLASH>)
        ss()
        (
            "*" { emit(buf, 1); }
            ss() unit(buf, 1)
            |
            <SLASH> { emit(buf, 1); }
            ss() Number(buf, 1)
        )
    )*
}

// attr    : "attr(" S* qname [ S+ type-keyword ]? S* [ "," [ unit | calc ] S* ]? ")";
void attr(FormatBuilder buf, int spaces) #void:
{
    <ATTR> { emit(buf, spaces); }
    ss() qname(buf, 0)
    [
        s()
        [
            Identifier() { emit(buf, 1); }
            ss()
        ]
    ]
    [
        "," { emit(buf); }
        ss() unit(buf, 1)
        ss()
    ]
     ")" { emit(buf); }
}

// unit    : [ NUMBER | DIMENSION | PERCENTAGE | "(" S* sum S* ")" | calc | attr ];
void unit(FormatBuilder buf, int spaces) #void:
{
    <DECIMAL | REAL | HEX> { emit(buf, spaces); }
	[
		%LA = GREEDY :
		<PERCENT | LENGTH | ANGLE | TIME | FREQ | RESOLUTION> { emit(buf); }
	]
    |
    <BUILTIN> { emit(buf, spaces); }
	|
	varref(buf, spaces)
	|
	calc(buf, spaces)
    |
    attr(buf, spaces)
    |
    "(" { emit(buf, spaces); }
    ss() sum(buf, 0)
    ss()
    ")" { emit(buf); }
}

// function
//   : FUNCTION S* expr ')' S*
//   ;
void function(FormatBuilder buf, int spaces) #void:
{
	<FUNCTION> { emit(buf, spaces); }
	[ expr(buf, 0) ]
	")" { emit(buf); }
}

//
//  // There is a constraint on the color that it must
//  // have either 3 or 6 hex-digits (i.e., [0-9a-fA-F])
//  // after the "#"; e.g., "#000" is OK, but "#abcd" is not.
//
// hexcolor
//   : HASH S*
//   ;
void hexcolor(FormatBuilder buf, int spaces) #void:
{
	<HASH> { emit(buf, spaces); }
}

private void Number(FormatBuilder buf, int spaces) #void: {
	<DECIMAL | REAL | HEX> { emit(buf, spaces); }
	|
	calc(buf, spaces)
}

private void Identifier() #void: {
	<IDENTIFIER
	| LENGTH | ANGLE | TIME | FREQ | RESOLUTION | IMPORTANT
	| BUILTIN | ONLY | NOT | AND | OF | FROM | TO | AT
	| CENTER | SIDE | SHAPE | EXTENT_KEYWORD
	>
}

////////////////////////////////////////////////////////////////////////
