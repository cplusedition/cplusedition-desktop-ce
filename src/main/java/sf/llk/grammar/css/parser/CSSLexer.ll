%OPTIONS {
	Language = "Java";
}

%LEXER (CSSLexer)

import java.util.HashMap;
import java.util.Map;

import sf.llk.share.support.DefaultTokenHandler;
import sf.llk.share.support.ILLKConstants;
import sf.llk.share.support.ILLKLexer;
import sf.llk.share.support.ILLKLexerInput;
import sf.llk.share.support.ILLKLifeCycleListener;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.support.ILLKTokenHandler;
import sf.llk.share.support.LLKLexerBase;
import sf.llk.share.support.LLKParseException;

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
public class CSSLexer extends LLKLexerBase implements ILLKLexer, ILLKCSSLexer {

	//	private static final String NAME = "CSSLexer";
	//	private static final boolean DEBUG = true;
	//	private static boolean VERBOSE = false;

	public CSSLexer(ILLKLexerInput input) {
	    this(input, DefaultTokenHandler.getInstance(
            input,
            LLK_SPECIAL_TOKENS,
            LLK_TOKEN_USE_TEXT,
            LLK_TOKEN_USE_INTERN));
    }

	public CSSLexer(ILLKLexerInput input, ILLKTokenHandler tokenhandler) {
		llkInput = input;
		llkMain = input.getMain();
		llkLocator = input.getLocator();
        llkInput.setIgnoreCase(LLK_IGNORE_CASE);
		llkTokenHandler = tokenhandler;
		llkInit();
	}

	////////////////////////////////////////////////////////////////////////

	// protected char[] ignoreCaseSource;

	//////////////////////////////////////////////////////////////////////

	protected void llkInit() {
		// ignoreCaseSource = llkInput.getIgnoreCaseSource();
	}

	private ILLKToken createToken(int type) {
		return llkTokenHandler.createToken(type, llkTokenStart, llkGetOffset());
	}

    public static int[] specialTokens() {
        return (int[])LLK_SPECIAL_TOKENS.clone();
    }

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private void warn(MsgId id, Throwable e, int offset) {
		llkMain.warn(id.getMessage(), e, offset);
	}

	//////////////////////////////////////////////////////////////////////
}

%OPTIONS {
	// Vocabulary = 0x10ffff;
	Vocabulary = 0xffff;
	StaticKeyword = false;
	TokenUseText = true;
	ResetHook = true;
	IgnoreCase = true;
	GenHelper = false;
	GenerateConstructor = "none";
}

////////////////////////////////////////////////////////////////////////

%KEYWORDS {
	IMPORT = "@import";
	PAGE = "@page";
	MEDIA = "@media";
	FONT_FACE = "@font-face";
	CHARSET = "@charset";
	KEYFRAMES = "@keyframes";
	WEBKIT_KEYFRAMES = "@-webkit-keyframes";
	LENGTH = "em" | "ex" | "ch" | "rem" | "vw" | "vh" | "vmax" | "vmin" | "cm" | "mm" | "in" | "pt" | "pc" | "px";
	ANGLE = "rad" | "deg" | "grad" | "turn";
	TIME = "ms" | "s";
	FREQ = "Hz" | "kHz";
	RESOLUTION = "dpi" | "dpcm" | "dppx";
	IMPORTANT = "important";
	BUILTIN = "auto" | "inherit" | "initial" | "unset" | "inset" | "outset";
	ONLY = "only";
	NOT = "not";
	AND = "and";
	OF = "of";
	FROM = "from";
	TO = "to";
	AT = "at";
	CENTER = "center";
	SIDE = "left" | "right" | "top" | "bottom";
	SHAPE = "circle" | "ellipse";
	EXTENT_KEYWORD = "closest-corner" | "closest-side" | "farthest-corner" | "farthest-side";
}

PERCENT : "%";
CDO : "<!--";
TILDE : "~";
WORDMATCH : "~=";
LANGMATCH : "|=";
HEADMATCH : "^=";
TAILMATCH : "$=";
SUBSTRMATCH : "*=";
EXCLAIMATION : "!";
// UNDERSCORE : "_";
OR : "|";
COLUMN : "||";
COMMA : ",";
COLON:  ":";
SEMICOLON : ";";
STAR : "*";
PLUS : "+";
GT : ">";
LT : "<";
LPAREN : "(";
RPAREN : ")";
LBRACE : "{";
RBRACE : "}";
LBRACKET : "[";
RBRACKET : "]";
EQUAL : "=";

HEX;
DECIMAL;
REAL;
DOT;
IDENTIFIER;
SPACES;
NEWLINE;
COMMENT;
SLASH; // "/"
// COLON; //  ":";
// COLONCOLON; // "::"
// NOT; // :not(
// MATCHES; // :matches(
// CURRENT; // :current(
DASH; // "-"
DASHDASH; // "--"
CDC; //  "-->"
RGB; //  "rgb("
RGBA; //  "rgba("
LINEAR_GRADIENT; // "linear-gradient("
RADIAL_GRADIENT; // "radial-gradient("
REPEATING_LINEAR_GRADIENT; // "repeating-linear-gradient("
REPEATING_RADIAL_GRADIENT; // "repeating-radial-gradient("
URI; // "url("
VAR; // "var("
CALC; // "calc("
ATTR; // "attr("
FUNCTION;

////////////////////////////////////////////////////////////////////////

ILLKToken S() {
	ILLKToken ret = null;
	int type = S;
}
{
	%LA = 2, GREEDY :
	type=Space()
	|
	type=Newline()
	|
	"\\\n"
	{
		type = NEWLINE;
		llkInput.newline();
	}
	|
	type=Comment()
	|
	"/"
	{ type = SLASH; }
	|
	ret=Identifier(llkGetOffset())
}
{
	if (ret == null) ret = createToken(type);
	return ret;
}

ILLKToken dash() {
    ILLKToken ret = null;
    int type = DASH;
    int start = llkGetOffset();
}
{
    "-"
    [
        "-"
        [
            ">" { type = CDC; }
            |
            { type = DASHDASH; }
        ]
        |
        %LA(IdentifierStart())
        ret = Identifier(start)
    ]
}
{
	if (ret == null) ret = createToken(type);
	return ret;
}

private int Space() :
{
	( ' ' | '\t' { llkInput.tab(); } )+
}
{ return SPACES; }

private int Newline() :
{
	( '\r' ( %LA = GREEDY : '\n' )? | '\n' | '\f' )
	{ llkInput.newline(); }
}
{ return NEWLINE; }

protected int Comment() :
{
	"/*"
	(
		%LOOKAHEAD(1, { LA(2) != '/' })
		'*'
		|
		<'\n' | '\f'>
		{ llkInput.newline(); }
		|
		'\r' ( %LA = GREEDY : '\n' )?
		{ llkInput.newline(); }
		|
		~<'*' | '\r' | '\n' | '\f'>
	)*
	"*/"
}
{ return COMMENT; }

////////////////////////////////////////////////////////////////////////
// NUMBERS

int NUMBER() {
	int type = NUMBER;
}
{
	%LA = 2, GREEDY :
	( Digit() )+
	{ type = DECIMAL; }
	(
		'.' ( Digit() )+
		{ type = REAL; }
	)?
	|
	type=Dot()
	|
	Hex()
	{ type = HEX; }
}
{ return type; }

protected int Dot() {
	int type = DOT;
}
{
	'.'
	(
		( Digit() )+
		{ type = REAL; }
	)?
}
{ return type; }

protected int Hex() {
	int ret = 0;
	int d;
}
{
	'0' 'x'
	(
		d=HexDigit() { ret = (ret << 4) | d; }
	)+
}
{ return ret; }

protected int Digit() :
{
	'0'..'9'
}
{ return LT0() - '0'; }

protected int HexDigit() {
	int ret = 0;
}
{
	'a'..'f' { ret = LT0() - 'a' + 10; }
	| ret=Digit()
}
{ return ret; }

////////////////////////////////////////////////////////////////////////
// LITERALS

void STRING() :
{
	'"'
	(
		Escape()
		|
		~<'"' | '\r' | '\n' | '\f' | '\\'>
	)*
	'"'
	|
	'\''
	(
		Escape()
		|
		~<'\'' | '\r' | '\n' | '\f' | '\\'>
	)*
	'\''
}

protected void Escape() :
{
	%LA = 2 :
	Unicode()
	|
	'\\' <'\u0020'..'~', '\u0080'..'\uffff' - '0'..'9', 'a'..'f', 'A'..'F'>
}

protected void Unicode() :
{
	"\\" ( %LA = GREEDY : HexDigit() ):1,6
}

////////////////////////////////////////////////////////////////////////
// IDENTIFIERS

protected ILLKToken Identifier(int start) {
	int end;
	int type;
	String text;
}
{
	IdentifierStart()
	( IdentifierPart() )*
	[
		'('
		{
			end = llkGetOffset();
			text = llkGetSource(start, end).toString().toLowerCase();
			if ("rgb(".equals(text)) {
				type = RGB;
			} else if ("rgba(".equals(text)) {
				type = RGBA;
			} else if ("linear-gradient(".equals(text)) {
				type = LINEAR_GRADIENT;
			} else if ("radial-gradient(".equals(text)) {
				type = RADIAL_GRADIENT;
			} else if ("repeating-linear-gradient(".equals(text)) {
				type = REPEATING_LINEAR_GRADIENT;
			} else if ("repeating-radial-gradient(".equals(text)) {
				type = REPEATING_RADIAL_GRADIENT;
			} else if ("var(".equals(text)) {
				type = VAR;
			} else if ("url(".equals(text)) {
				type = URI;
			} else if ("calc(".equals(text)) {
				type = CALC;
			} else if ("attr(".equals(text)) {
				type = ATTR;
			} else {
				type = FUNCTION;
			}
		}
		[
			%LA({ type == URI })
			uri()
			{
				end = llkGetOffset();
				text = llkGetSource(start, end).toString();
			}
		]
		|
		{
			end = llkGetOffset();
			text = llkGetSource(start, end).toString();
			type = llkLookupKeyword(text.toLowerCase(), IDENTIFIER);
		}
	]
}
{ return llkTokenHandler.createToken(type, start, end, text, null); }

ILLKToken AT_KEYWORD() {
	int start = llkGetOffset();
	int end;
	String text;
}
{
	"@" [ "-" ] Identifier(llkGetOffset())
}
{
	end = llkGetOffset();
	text = llkGetSource(start, end).toString();
	int type = llkLookupKeyword(text.toLowerCase(), AT_KEYWORD);
	return llkTokenHandler.createToken(type, start, end, text, null);
}

private void IdentifierStart() :
{
	<'a'..'z'>
	|
	Escape()
	|
	~<'\u0000'..'\u007f'>
	|
	'_'
	{ warn(MsgId.LexerInvalidUnderscore, null, llkGetOffset() - 1); }
}

private void IdentifierPart() :
{
	<'a'..'z', '0'..'9', '-'>
	|
	Escape()
	|
	~<'\u0000'..'\u007f'>
	|
	'_'
	{ warn(MsgId.LexerInvalidUnderscore, null, llkGetOffset() - 1); }
}

////////////////////////////////////////////////////////////////////////

void HASH() :
{
	'#' Name()
}

private void Name() :
{
	( IdentifierPart() )+
}

void UNICODE_RANGE() :
{
	"U+"
	(
		%LA(( HexDigit() ):1,6 "-")
		( HexDigit() ):1,6 "-" ( HexDigit() ):1,6
		|
		Range()
	)
}


private void uri() #void :
{
	[
		%LA({ LA1() != '\\' || LA1() == '\\' && LA(2) == '\n' })
		URI_S()
	]
	( STRING() | URL_PART() )
	[ URI_S() ]
	")"
}

private void URI_S() :
{
	(
		' '
		| '\t' { llkInput.tab(); }
		| Newline()
		| %LA("\\\n")
		"\\\n" { llkInput.newline(); }
	)+
}

private void URL_PART() :
{
	(
		<'!', '#', '$', '%', '&', '*'..'~' - '\\'>
		|
		~<'\u0000'..'\u007f'>
		|
		%LA({ LA1() == '\\' && LA(2) != '\n' })
		Escape()
	)+
}

private void Range() :
{
	HexDigit() ( ( '?' ):5
		| HexDigit() ( ( '?' ):4
			| HexDigit() ( ( '?' ):3
				| HexDigit() ( ( '?' ):2
					| HexDigit() ( ( '?' )
						| HexDigit()
					)
				)
			)
		)
	)
}

////////////////////////////////////////////////////////////////////////
