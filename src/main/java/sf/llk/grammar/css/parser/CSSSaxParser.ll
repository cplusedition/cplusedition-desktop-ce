%OPTIONS {
	Language = "Java";
}

%PARSER (CSSSaxParser)

import sf.llk.grammar.css.ICSSSaxHandler;
import sf.llk.share.support.ILLKConstants;
import sf.llk.share.support.ILLKLifeCycleListener;
import sf.llk.share.support.ILLKMain;
import sf.llk.share.support.ILLKNode;
import sf.llk.share.support.ILLKParserInput;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.support.LLKLexerInput;
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
public class CSSSaxParser extends CSSSaxParserBase {
    public CSSSaxParser(ILLKParserInput input, ICSSSaxHandler handler) {
        this(input);
        this.handler = handler;
    }
	public CSSSaxParser(char[] source, ILLKMain main, ICSSSaxHandler handler) {
		this(new LLKParserInput(new CSSLexer(new LLKLexerInput(source, main))));
		this.handler = handler;
	}
}

%OPTIONS {
	Import = "ILLKCSSLexer.xml";
	BuildVisitor = false;
	BuildAST = false;
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

void styleSheet():
{
	[ charset() ]
	ssc()
	( Import() ssc() )*
	( ( ruleset() | media() | page() | font_face() | keyframes() | atRule() ) ssc() )*
}

void charset() :
{
	<CHARSET> ss() <STRING> ss() ";"
}

void Import():
{
	try {
		"@import"
		ss()
		(
			<STRING>
			| <URI> { handler.handleUri(LT0()); }
		) ss()
		( medium() ( "," ss() medium() )* )? ";"
	} catch (Throwable e) {
		ILLKToken first = LT0();
		ILLKToken last = skip(0);
		warn(MsgId.ParserInvalidConstructIgnored, e, first, last);
	}
}

void media() {
	int level = 0;
}
{
	try {
		"@media" ss() [ media_query_list() ]
		"{" { ++level; }
		ss() (ruleset() ss())*
		"}" { --level; }
	} catch (Throwable e) {
		ILLKToken first = LT0();
		ILLKToken last = skip(level);
		warn(MsgId.ParserInvalidConstructIgnored, e, first, last);
	}
}

void media_query_list() :
{
    media_query() ( "," ss() media_query() )*
}

void media_query() :
{
    ["only" ss() | "not" ss()]  media_type() ss() ( "and" media_expr() )*
    | media_expr() ( "and" ss() media_expr() )*
}

void media_type() :
{
    <IDENTIFIER>
}

void media_expr() :
{
    "(" ss() media_feature() ss() [ ":" ss() expr() ] ")" ss()
}

void media_feature() :
{
    <IDENTIFIER>
}

void medium() :
{
	Identifier() ss()
}

void keyframes() :
{
    <"@keyframes" | "@-webkit-keyframes"> ss() <IDENTIFIER> ss() "{" ss() (keyframes_block())* "}" ss()
}

void keyframes_block() :
{
    keyframe_selector() "{" ss() [ declaration() ss() ] ( ";" ss() [ declaration() ss() ] )* "}" ss()
}

void keyframe_selector() :
{
    ( "from" | "to" | Number() "%" ) ss() ( "," ss() ( "from" | "to" | Number() "%" ) ss() )*
}

void page() {
	int level = 0;
}
{
	try {
		"@page"
		ss() [ Identifier() ] pseudo_page()
		ss()
		"{" { ++level; }
		ss() [ declaration() ss() ]
		( ";" ss() [ declaration() ss() ])*
		"}" { --level; }
		(
			%LA = GREEDY :
			ss()
		)?
	} catch (Throwable e) {
		ILLKToken first = LT0();
		ILLKToken last = skip(level);
		warn(MsgId.ParserInvalidConstructIgnored, e, first, last);
	}
}

void pseudo_page() :
{
	":" Identifier()
}

void font_face() {
	int level = 0;
}
{
	try {
		"@font-face"
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
		ILLKToken first = LT0();
		ILLKToken last = skip(level);
		warn(MsgId.ParserInvalidConstructIgnored, e, first, last);
	}
}

void atRule() :
{
	<AT_KEYWORD>
	{
		ILLKToken first = LT0();
		ILLKToken last = skip(0);
		warn(MsgId.ParserInvalidConstructIgnored, null, first, last);
	}
}

void operator() :
{
	[
	    %LA(<"+" | DASH> ss())
	    <"+" | DASH> ss()
	    |
	    <SLASH | "*" | ","> ss()
	]
}

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

void unary_operator() :
{
	<DASH> | "+"
}

void ruleset() {
	int level = 0;
	ILLKNode n;
}
{
	try {
		selector()
		( "," ss() [ selector() ] )*
		"{" { level = 1; }
		ss() [ declaration() ss() ]
		( ";" ss() [ declaration() ss() ] )*
		"}" { level = 0; }
		// (
		// 	%LA = GREEDY :
		// 	ss()
		// )?
	} catch (Throwable e) {
		ILLKToken first = LT0();
		ILLKToken last = skip(level);
		warn(MsgId.ParserInvalidConstructIgnored, e, first, last);
	}
}

void selector() :
{
	simple_selector()
	(
		combinator()
		simple_selector()
	)*
}

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

void Class() :
{
	<DOT> Identifier()
}

void qname() :
{
	(Identifier() | "*")
	[ "|" (Identifier() | "*") ]
}

void attrib() :
{
	"[" ss()
	Identifier() ss()
	(
        <"="  | "~=" | "|=" | "^=" | "$=" | "*="> ss()
		(
			Identifier()
			| <STRING>
		) ss()
	)?
	"]"
}

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

void declaration() {
    ILLKToken name;
    ILLKToken value = null;
}
{
	try {
        name=property() ss() ":" ss()
        [
            { value = LT1(); }
            expr()
        ]
        [ prio() ]
        { handler.handleDeclaration(name, value); }
	} catch (Exception e) {
		ILLKToken first = LT0();
		ILLKToken last = skipTo(new int[] { SEMICOLON, RBRACE });
		warn(MsgId.ParserInvalidConstructIgnored, e, first, last);
	}
}

ILLKToken property() :
{
	Identifier()
}
{ return LT0(); }

void prio() :
{
	"!" ss() "important" ss()
}

void expr() :
{
	term() ( operator() term() )*
}

void term() :
{
    (
        unary_operator() units()
        |
        units()
        |
        <URI> { handler.handleUri(LT0()); }
        |
        <STRING | UNICODE_RANGE>
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

void side_or_corner() :
{
    <SIDE> [ <SIDE> ]
}

void position() :
{
    position1()
    [
        %LA(s() ~<",">)
        s() position1()
    ]
}

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

void calc():
{
	<CALC> ss()
	sum() ss()
	")"
}

void sum():
{
   product()
   (
        %LA(s() <"+" | DASH>)
        s() <"+" | DASH>
        s() product()
   )*
}

void product() :
{
    unit() (
        %LA(ss() <"*" | SLASH>)
        ss() ( "*" ss() unit() | <SLASH> ss() Number())
    )*
}

void attr() :
{
    <ATTR> ss()
    qname()
    [ s() [ Identifier()  ss() ]]
    [ "," ss() unit() ss() ]
     ")"
}

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

void function() :
{
	<FUNCTION> ss() [ expr() ] ")"
}
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
