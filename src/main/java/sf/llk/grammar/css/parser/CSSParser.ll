%OPTIONS {
	Language = "Java";
}

%PARSER (CSSParser)

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

	//	private boolean hasWhitespaces(ILLKToken lt0) {
	//		int type = lt0.getType();
	//		return (type == SPACES || type == NEWLINE || type == COMMENT);
	//	}

	////////////////////////////////////////////////////////////////////////

	public String getLineno() {
		return "@" + (llkInput.getLocator().getLinear(LT0().getOffset()));
	}

	////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////

%OPTIONS {
	Import = "ILLKCSSLexer.xml";
	BuildVisitor = true;
	BuildAST = true;
	Multi = true;
	NodeDefaultVoid = false;
	NodeScopeHook = true;
	ExplicitNodePrefix = "";
	GenHelper = false;
}

private void s()  #void: {
	(
		%LA = GREEDY :
		<SPACES | NEWLINE | COMMENT>
	)+
}

private void ss()  #void: {
	(
		%LA = GREEDY :
		<SPACES | NEWLINE | COMMENT>
	)*
}

private void ssc()  #void: {
	(
		%LA = GREEDY :
		<SPACES | NEWLINE | COMMENT | CDO | CDC>
	)*
}

// stylesheet
//   : [ CHARSET_SYM S* STRING S* ';' ]?
//     [S|CDO|CDC]* [ import [S|CDO|CDC]* ]*
//     [ [ ruleset | media | page | font_face ] [S|CDO|CDC]* ]*
//   ;
ASTstyleSheet styleSheet() :
{
	[ charset() ]
	ssc()
	( Import() ssc() )*
	( ( ruleset() | media() | page() | font_face() | keyframes() | atRule() ) ssc() )*
}
{ return llkThis; }


void charset() :
{
	<CHARSET> ss() <STRING> ss() ";"
}

// import
//   : IMPORT_SYM S*
//     [STRING|URI] S* [ medium [ ',' S* medium]* ]? ';' S*
//   ;
void Import() {
	StringBuilder b = new StringBuilder();
}
{
	try {
		"@import" { b.append(LT0().getText()); }
		ss()
		(
			<STRING> { b.append(LT0().getText()); }
			| <URI> { b.append(LT0().getText()); }
		) ss()
		( medium() ( "," ss() medium() )* )? ";"
	} catch (Throwable e) {
		llkThis.setModifiers(ERROR);
		ILLKToken first = LT0();
		ILLKToken last = skip(0);
		warn(MsgId.ParserInvalidConstructIgnored, e, first, last);
	}
}
{ llkThis.setText(b.toString()); }

// media
//   : MEDIA_SYM  media_query_list '{' S* ruleset* '}' S*
//   ;
//
void media() {
	int level = 0;
	StringBuilder b = new StringBuilder();
}
{
	try {
		"@media" ss() [ media_query_list() ]
		"{" { ++level; }
		ss() (ruleset() ss())*
		"}" { --level; }
	} catch (Throwable e) {
		llkThis.setModifiers(ERROR);
		ILLKToken first = LT0();
		ILLKToken last = skip(level);
		warn(MsgId.ParserInvalidConstructIgnored, e, first, last);
	}
}

// media_query_list
//  : S* [media_query [ ',' S* media_query ]* ]?
//  ;
void media_query_list() :
{
    media_query() ( "," ss() media_query() )*
}

// media_query
//  : [ONLY | NOT]? S* media_type S* [ AND S* expression ]*
//  | expression [ AND S* expression ]*
//  ;
void media_query() :
{
    ["only" ss() | "not" ss()]  media_type() ss() ( "and" media_expr() )*
    | media_expr() ( "and" ss() media_expr() )*
}

// media_type
//  : IDENT
//  ;
void media_type() :
{
    <IDENTIFIER>
}

// expression
//  : '(' S* media_feature S* [ ':' S* expr ]? ')' S*
//  ;
void media_expr() :
{
    "(" ss() media_feature() ss() [ ":" ss() expr() ] ")" ss()
}

// media_feature
//  : IDENT
//  ;
void media_feature() :
{
    <IDENTIFIER>
}

// // medium
// //   : IDENT S*
// //   ;
void medium() :
{
	Identifier() ss()
}

// keyframes_rule: KEYFRAMES_SYM S+ IDENT S* '{' S* keyframes_blocks '}' S*;
void keyframes() :
{
    <"@keyframes" | "@-webkit-keyframes"> ss() <IDENTIFIER> ss() "{" ss() (keyframes_block())* "}" ss()
}

// keyframes_blocks: [ keyframe_selector '{' S* declaration? [ ';' S* declaration? ]* '}' S* ]* ;
void keyframes_block() :
{
    keyframe_selector() "{" ss() [ declaration() ss() ] ( ";" ss() [ declaration() ss() ] )* "}" ss()
}

// keyframe_selector: [ FROM_SYM | TO_SYM | PERCENTAGE ] S* [ ',' S* [ FROM_SYM | TO_SYM | PERCENTAGE ] S* ]*;
void keyframe_selector() :
{
    ( "from" | "to" | Number() "%" ) ss() ( "," ss() ( "from" | "to" | Number() "%" ) ss() )*
}

// page
//   : PAGE_SYM S* IDENT? pseudo_page? S*
//     '{' S* declaration [ ';' S* declaration ]* '}' S*
//   ;
void page() {
	int level = 0;
	StringBuilder b = new StringBuilder();
}
{
	try {
		"@page" { b.append(LT0().getText()); }
		ss() [ Identifier() ] pseudo_page() { b.append(LT0().getText()); }
		ss()
		"{" { level = 1; }
		ss() [ declaration() ss() ]
		( ";" ss() [ declaration() ss() ])*
		"}" { level = 0; }
		(
			%LA = GREEDY :
			ss()
		)?
	} catch (Throwable e) {
		llkThis.setModifiers(ERROR);
		ILLKToken first = LT0();
		ILLKToken last = skip(level);
		warn(MsgId.ParserInvalidConstructIgnored, e, first, last);
	}
}
{ llkThis.setText(b.toString()); }

// pseudo_page
//   : ':' IDENT
//   ;
void pseudo_page() :
{
	":" Identifier()
}
// font_face
//   : FONT_FACE_SYM S*
//     '{' S* declaration [ ';' S* declaration ]* '}' S*
//   ;
void font_face() {
	int level = 0;
}
{
	try {
		"@font-face" { llkThis.setText(LT0().getText()); }
		ss()
		"{" { ++level; }
		ss()
		[ declaration() ss() ]
		( ";" ss() [ declaration() ss() ])*
		"}" { --level; }
		(
			%LA = GREEDY :
			ss()
		)?
	} catch (Throwable e) {
		llkThis.setModifiers(ERROR);
		ILLKToken first = LT0();
		ILLKToken last = skip(level);
		warn(MsgId.ParserInvalidConstructIgnored, e, first, last);
	}
}

void atRule() :
{
	<AT_KEYWORD>
	{
		llkThis.setText(LT0().getText());
		llkThis.setModifiers(ERROR);
		ILLKToken first = LT0();
		ILLKToken last = skip(0);
		warn(MsgId.ParserInvalidConstructIgnored, null, first, last);
	}
}

// operator
//   : '/' S* | ',' S* | // empty
//   ;
void operator() :
{
	[
	    %LA(<"+" | DASH> ss())
	    <"+" | DASH> ss()
	    |
	    <SLASH | "*" | ","> ss()
	]
}

// combinator
//   : '+' S* | '>' S* | // empty
//   ;
void combinator() :
{
	[
	    (
	        "+"	| ">" | "~" | "||" | <SLASH> [
	            %LA(<IDENTIFIER> <SLASH>)
	            <IDENTIFIER> <SLASH>
	        ]
	    )
	    ss()
	]
}

// unary_operator
//   : '-' | '+'
//   ;
void unary_operator() :
{
	<DASH> | "+"
}

// property
//   : IDENT S*
//   ;
void property() :
{
	Identifier()
}

// ruleset
//   : selector [ ',' S* selector ]*
//     '{' S* declaration [ ';' S* declaration ]* '}' S*
//   ;
void ruleset() {
	int level = 0;
	ILLKNode n;
}
{
	try {
		selector()
		( "," ss() [ selector() ] )*
		"{" { ++level; }
		ss() [ declaration() ss() ]
		( ";" ss() [ declaration() ss() ] )*
		"}" { --level; }
		// (
		// 	%LA = GREEDY :
		// 	ss()
		// )?
	} catch (Throwable e) {
		llkThis.setModifiers(ERROR);
		ILLKToken first = LT0();
		ILLKToken last = skip(level);
		warn(MsgId.ParserInvalidConstructIgnored, e, first, last);
	}
}

// selector
//   : simple_selector [ combinator simple_selector ]*
//   ;
void selector() :
{
	simple_selector()
	(
		combinator()
		simple_selector()
	)*
}
{ llkThis.setText(llkGetSource(llkThis.getOffset(), llkThis.getEndOffset())); }

//simple_selector
//  : qname? [ HASH | class | attrib | pseudo ]* S*
//  ;
void simple_selector() :
{
	(
		qname()
		(
			%LA = GREEDY :
			<HASH> | Class() | pseudo() | attrib()
		)*
		|
		<HASH>
		(
			%LA = GREEDY :
			<HASH> | Class() | pseudo() | attrib()
		)*
		|
		pseudo()
		(
			%LA = GREEDY :
			<HASH> | Class() | pseudo() | attrib()
		)*
		|
		Class()
		(
			%LA = GREEDY :
			<HASH> | Class() | pseudo() | attrib()
		)*
	)
	ss()
}

// class
//   : '.' IDENT
//   ;
void Class() :
{
	<DOT> Identifier()
}

// element_name
//   : IDENT | '*'
//   ;
void qname() :
{
	(Identifier() | "*")
	[ "|" (Identifier() | "*") ]
}

// attrib
//   : '[' S* IDENT S* [ [ '=' | MATCH ] S*
//     [ IDENT | STRING ] S* ]? ']'
//   ;
void attrib() :
{
	"[" ss()
	Identifier() ss()
	(
		(
			"="
			| <WORDMATCH | LANGMATCH | HEADMATCH | TAILMATCH | SUBSTRMATCH>
		) ss()
		(
			Identifier()
			| <STRING>
		) ss()
	)?
	"]"
}
// pseudo
//   : ':' [ IDENT | FUNCTION S* IDENT S* ')' ]
//   ;
void pseudo() :
{
	":"
	[ %LA(":") ":" ]
	[
		%GREEDY :
		Identifier()
		|
		<FUNCTION> ss()
		(
			selector() ss() ("," ss() selector() ss())*
			| attrib() ss()
			| Number() ss() [ "of" selector() ]
		)
		")"
	]
}

// declaration
//   : property ':' S* expr prio?
//   | // empty
//   ;
void declaration() :
{
	try {
        property() ss() ":" ss() [ expr() ] [ prio() ]
	} catch (Exception e) {
		ILLKToken first = LT0();
		llkThis.setModifiers(ERROR);
		ILLKToken last = skipTo(new int[] { SEMICOLON, RBRACE });
		warn(MsgId.ParserInvalidConstructIgnored, e, first, last);
	}
}

// prio
//   : "!" IMPORTANT_SYM S*
//   ;
void prio() :
{
	"!" ss() "important" ss()
}

// expr
//   : term [ operator term ]*
//   ;
void expr() :
{
	term() ( operator() term() )*
}

// term
//   : unary_operator?
//     [ NUMBER S* | PERCENTAGE S* | LENGTH S* | EMS S* | EXS S* | ANGLE S* | TIME S* | FREQ S* | function ]
//   | STRING S* | IDENT S* | URI S* | RGB S* | UNICODERANGE S* | hexcolor
//   ;
void term() :
{
	(
	    unary_operator() units()
        |
        units()
        |
        <STRING | URI | UNICODE_RANGE>
	    |
	    Identifier()
        |
        color()
        |
        gradient()
	)
	ss()
}

private void units()  #void: {
    <DECIMAL | REAL | HEX>
    [
        %LA = GREEDY :
        <PERCENT | LENGTH | ANGLE | TIME | FREQ | RESOLUTION>
    ]
    |
    varref()
    |
    calc()
    |
    attr()
    |
    function()
}

private void varref() #void: {
    <VAR> ss()
    <DASHDASH> <IDENTIFIER> ss()
    ")"
}

void color() :
{
	hexcolor()
	|
	<RGB> ss()
	Number() ss()
	"," ss() Number() ss()
	"," ss() Number() ss()
	["," ss() ]
	")"
	|
	<RGBA> ss()
	Number() ss()
	"," ss() Number() ss()
	"," ss() Number() ss()
	"," ss() Number() ss()
	["," ss() ]
	")"
}

// <linear-gradient> = linear-gradient(
// 	[ [ <angle> | to <side-or-corner> ] ,]?
// 	<color-stop>[, <color-stop>]+
// )
private void gradient()  #void: {
    linear_gradient()
    | radial_gradient()
}

void linear_gradient() :
{
    <LINEAR_GRADIENT | REPEATING_LINEAR_GRADIENT> ss()
    [
        ( <DECIMAL | REAL | HEX> <ANGLE> | "to" ss() side_or_corner() )
        ss() "," ss()
    ]
    color_stop() ( "," ss() color_stop() )+
    ")"
}

// <radial-gradient> = radial-gradient(
//   [ [ <shape> || <size> ] [ at <position> ]? , |
//     at <position>,
//   ]?
//   <color-stop> [ , <color-stop> ]+
// )
// <radial-gradient> = radial-gradient(
//   [ [ circle               || <length> ]                          [ at <position> ]? , |
//     [ ellipse              || [ <length> | <percentage> ]{2} ]    [ at <position> ]? , |
//     [ [ circle | ellipse ] || <extent-keyword> ]                  [ at <position> ]? , |
//     at <position> ,
//   ]?
//   <color-stop> [ , <color-stop> ]+
// )
// <extent-keyword> = closest-corner | closest-side | farthest-corner | farthest-side
void radial_gradient() :
{
    <RADIAL_GRADIENT | REPEATING_RADIAL_GRADIENT> ss()
    [
        [
            <SHAPE> ss()
            [
                ( length_or_percent() | <EXTENT_KEYWORD> ) ss()
                [ length_or_percent() ss() ]
            ]
            |
            ( length_or_percent()  | <EXTENT_KEYWORD> ) ss()
            [ length_or_percent() ss() ]
            [ <SHAPE> ss() ]
        ]
        [ "at" ss() position() ss() ] "," ss()
    ]
    color_stop() ( "," ss() color_stop() )+
    ")"
}

// <side-or-corner> = [left | right] || [top | bottom]
void side_or_corner() :
{
    <SIDE> [ <SIDE> ]
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
void position() :
{
    position1()
    [
        %LA(s() ~<",">)
        s() position1()
    ]
}

// <color-stop> = <color> [ <percentage> | <length> ]?
void color_stop():
{
    (<IDENTIFIER> | color()) ss() [ length_or_percent() ss() ]
}

private void position1()  #void: {
    <CENTER | SIDE> | length_or_percent()
}

private void length_or_percent()  #void: {
    [<DASH | PLUS>]
    ( <DECIMAL | REAL | HEX> <PERCENT | LENGTH> | calc() )
}

// math    : calc S*;
// calc    : "calc(" S* sum S* ")";
void calc():
{
	<CALC> ss()
	sum() ss()
	")"
}

// sum     : product [ S+ [ "+" | "-" ] S+ product ]*;
void sum():
{
   product()
   (
        %LA(s() <"+" | DASH>)
        s() <"+" | DASH>
        s() product()
   )*
}

// product : unit [ S* [ "*" S* unit | "/" S* NUMBER ] ]*;
void product() :
{
    unit() (
        %LA(ss() <"*" | SLASH>)
        ss() ( "*" ss() unit() | <SLASH> ss() Number())
    )*
}

// attr    : "attr(" S* qname [ S+ type-keyword ]? S* [ "," [ unit | calc ] S* ]? ")";
void attr() :
{
    <ATTR> ss()
    qname()
    [ s() [ Identifier()  ss() ]]
    [ "," ss() unit() ss() ]
     ")"
}

// unit    : [ NUMBER | DIMENSION | PERCENTAGE | "(" S* sum S* ")" | calc | attr ];
void unit() :
{
    <DECIMAL | REAL | HEX>
	[
		%LA = GREEDY :
		<PERCENT | LENGTH | ANGLE | TIME | FREQ | RESOLUTION>
	]
    |
    <BUILTIN>
    |
    varref()
	|
	calc()
    |
    attr()
    |
    "(" ss() sum() ss() ")"
}

// function
//   : FUNCTION S* expr ')' S*
//   ;
void function() :
{
	<FUNCTION> ss() [ expr() ] ")"
}

//
//  // There is a constraint on the color that it must
//  // have either 3 or 6 hex-digits (i.e., [0-9a-fA-F])
//  // after the "#"; e.g., "#000" is OK, but "#abcd" is not.
//
// hexcolor
//   : HASH S*
//   ;
void hexcolor() :
{
	<HASH> ss()
}

private void Number() #void: {
	<DECIMAL | REAL | HEX> | calc()
}

private void Identifier()  #void: {
	<IDENTIFIER
	| LENGTH | ANGLE | TIME | FREQ | RESOLUTION | IMPORTANT
	| BUILTIN | ONLY | NOT | AND | OF | FROM | TO | AT
	| CENTER | SIDE | SHAPE | EXTENT_KEYWORD
	>
}

////////////////////////////////////////////////////////////////////////
