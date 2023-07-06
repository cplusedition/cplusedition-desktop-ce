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
package sf.llk.grammar.css.parser;

public interface ILLKCSSFormatter {
	int _EOF_=0;
	int _IGNORE_=1;
	int _INVALID_=2;
	int _NULL_=3;
	int IMPORT=4;
	int PAGE=5;
	int MEDIA=6;
	int FONT_FACE=7;
	int CHARSET=8;
	int KEYFRAMES=9;
	int WEBKIT_KEYFRAMES=10;
	int LENGTH=11;
	int ANGLE=12;
	int TIME=13;
	int FREQ=14;
	int RESOLUTION=15;
	int IMPORTANT=16;
	int BUILTIN=17;
	int ONLY=18;
	int NOT=19;
	int AND=20;
	int OF=21;
	int FROM=22;
	int TO=23;
	int AT=24;
	int CENTER=25;
	int SIDE=26;
	int SHAPE=27;
	int EXTENT_KEYWORD=28;
	int PERCENT=29;
	int CDO=30;
	int TILDE=31;
	int WORDMATCH=32;
	int LANGMATCH=33;
	int HEADMATCH=34;
	int TAILMATCH=35;
	int SUBSTRMATCH=36;
	int EXCLAIMATION=37;
	int OR=38;
	int COLUMN=39;
	int COMMA=40;
	int COLON=41;
	int SEMICOLON=42;
	int STAR=43;
	int PLUS=44;
	int GT=45;
	int LT=46;
	int LPAREN=47;
	int RPAREN=48;
	int LBRACE=49;
	int RBRACE=50;
	int LBRACKET=51;
	int RBRACKET=52;
	int EQUAL=53;
	int HEX=54;
	int DECIMAL=55;
	int REAL=56;
	int DOT=57;
	int IDENTIFIER=58;
	int SPACES=59;
	int NEWLINE=60;
	int COMMENT=61;
	int SLASH=62;
	int DASH=63;
	int DASHDASH=64;
	int CDC=65;
	int RGB=66;
	int RGBA=67;
	int LINEAR_GRADIENT=68;
	int RADIAL_GRADIENT=69;
	int REPEATING_LINEAR_GRADIENT=70;
	int REPEATING_RADIAL_GRADIENT=71;
	int URI=72;
	int VAR=73;
	int CALC=74;
	int ATTR=75;
	int FUNCTION=76;
	int S=77;
	int dash=78;
	int Space=79;
	int Newline=80;
	int Comment=81;
	int NUMBER=82;
	int Dot=83;
	int Hex=84;
	int Digit=85;
	int HexDigit=86;
	int STRING=87;
	int Escape=88;
	int Unicode=89;
	int Identifier=90;
	int AT_KEYWORD=91;
	int IdentifierStart=92;
	int IdentifierPart=93;
	int HASH=94;
	int Name=95;
	int UNICODE_RANGE=96;
	int URI_S=97;
	int URL_PART=98;
	int Range=99;
	int ASTstyleSheet=100;
	int ASTcharset=101;
	int ASTImport=102;
	int ASTmedia=103;
	int ASTmedia_query_list=104;
	int ASTmedia_query=105;
	int ASTmedia_type=106;
	int ASTmedia_expr=107;
	int ASTmedia_feature=108;
	int ASTmedium=109;
	int ASTkeyframes=110;
	int ASTkeyframes_block=111;
	int ASTkeyframe_selector=112;
	int ASTpage=113;
	int ASTpseudo_page=114;
	int ASTfont_face=115;
	int ASTatRule=116;
	int ASToperator=117;
	int ASTcombinator=118;
	int ASTunary_operator=119;
	int ASTproperty=120;
	int ASTruleset=121;
	int ASTselector=122;
	int ASTsimple_selector=123;
	int ASTClass=124;
	int ASTqname=125;
	int ASTattrib=126;
	int ASTpseudo=127;
	int ASTdeclaration=128;
	int ASTprio=129;
	int ASTexpr=130;
	int ASTterm=131;
	int ASTcolor=132;
	int ASTlinear_gradient=133;
	int ASTradial_gradient=134;
	int ASTside_or_corner=135;
	int ASTposition=136;
	int ASTcolor_stop=137;
	int ASTcalc=138;
	int ASTsum=139;
	int ASTproduct=140;
	int ASTattr=141;
	int ASTunit=142;
	int ASTfunction=143;
	int ASThexcolor=144;
	int LLK_TOKENTYPE_COUNT = 145;
	
	public static class LLKTOKENS {

		////////////////////////////////////////////////////////////

		private static final String[] NAMES = new String[] {
			"_EOF_",
			"_IGNORE_",
			"_INVALID_",
			"_NULL_",
			"IMPORT",
			"PAGE",
			"MEDIA",
			"FONT_FACE",
			"CHARSET",
			"KEYFRAMES",
			"WEBKIT_KEYFRAMES",
			"LENGTH",
			"ANGLE",
			"TIME",
			"FREQ",
			"RESOLUTION",
			"IMPORTANT",
			"BUILTIN",
			"ONLY",
			"NOT",
			"AND",
			"OF",
			"FROM",
			"TO",
			"AT",
			"CENTER",
			"SIDE",
			"SHAPE",
			"EXTENT_KEYWORD",
			"PERCENT",
			"CDO",
			"TILDE",
			"WORDMATCH",
			"LANGMATCH",
			"HEADMATCH",
			"TAILMATCH",
			"SUBSTRMATCH",
			"EXCLAIMATION",
			"OR",
			"COLUMN",
			"COMMA",
			"COLON",
			"SEMICOLON",
			"STAR",
			"PLUS",
			"GT",
			"LT",
			"LPAREN",
			"RPAREN",
			"LBRACE",
			"RBRACE",
			"LBRACKET",
			"RBRACKET",
			"EQUAL",
			"HEX",
			"DECIMAL",
			"REAL",
			"DOT",
			"IDENTIFIER",
			"SPACES",
			"NEWLINE",
			"COMMENT",
			"SLASH",
			"DASH",
			"DASHDASH",
			"CDC",
			"RGB",
			"RGBA",
			"LINEAR_GRADIENT",
			"RADIAL_GRADIENT",
			"REPEATING_LINEAR_GRADIENT",
			"REPEATING_RADIAL_GRADIENT",
			"URI",
			"VAR",
			"CALC",
			"ATTR",
			"FUNCTION",
			"S",
			"dash",
			"Space",
			"Newline",
			"Comment",
			"NUMBER",
			"Dot",
			"Hex",
			"Digit",
			"HexDigit",
			"STRING",
			"Escape",
			"Unicode",
			"Identifier",
			"AT_KEYWORD",
			"IdentifierStart",
			"IdentifierPart",
			"HASH",
			"Name",
			"UNICODE_RANGE",
			"URI_S",
			"URL_PART",
			"Range",
			"ASTstyleSheet",
			"ASTcharset",
			"ASTImport",
			"ASTmedia",
			"ASTmedia_query_list",
			"ASTmedia_query",
			"ASTmedia_type",
			"ASTmedia_expr",
			"ASTmedia_feature",
			"ASTmedium",
			"ASTkeyframes",
			"ASTkeyframes_block",
			"ASTkeyframe_selector",
			"ASTpage",
			"ASTpseudo_page",
			"ASTfont_face",
			"ASTatRule",
			"ASToperator",
			"ASTcombinator",
			"ASTunary_operator",
			"ASTproperty",
			"ASTruleset",
			"ASTselector",
			"ASTsimple_selector",
			"ASTClass",
			"ASTqname",
			"ASTattrib",
			"ASTpseudo",
			"ASTdeclaration",
			"ASTprio",
			"ASTexpr",
			"ASTterm",
			"ASTcolor",
			"ASTlinear_gradient",
			"ASTradial_gradient",
			"ASTside_or_corner",
			"ASTposition",
			"ASTcolor_stop",
			"ASTcalc",
			"ASTsum",
			"ASTproduct",
			"ASTattr",
			"ASTunit",
			"ASTfunction",
			"ASThexcolor",
		};

		////////////////////////////////////////////////////////////

		public static String nameOf(int type) {
			if (type >= NAMES.length)
				return null;
			return NAMES[type];
		}

		public static String[] names() {
			return NAMES.clone();
		}

		////////////////////////////////////////////////////////////
	}
	
	int KEYWORD_CONTEXT_NONE = 1;
}
