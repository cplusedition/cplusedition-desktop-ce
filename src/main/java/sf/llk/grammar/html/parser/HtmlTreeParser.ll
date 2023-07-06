%TREEPARSER (HtmlTreeParser)

import sf.llk.share.support.*;

/*
 * Copyright (c) 2005, Chris Leung. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2 with a special exception, the Classpath exception.
 * You should have received a copy of the GNU General Public License (GPL)
 * and the Classpath exception along with this library.
 */
public class HtmlTreeParser extends LLKTreeParserBase implements ILLKHtmlTreeParser {
}

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
	GenerateConstructor = "public";
	ResetHook = false;
	GenHelper = false;
}

void document() :
{
	[ #(ASTtext /* text() */ ) ]
	( element() )*
	/* <_EOF_> */
}

void element() #void :
{
	#(ASTstartTag startTag())
	|
	(
		#(ASTextraEndTag /* extraEndTag() */ )
		|
		#(ASTcomment /* comment() */ )
		|
		#(ASTpi /* pi() */ )
		|
		#(ASTasp /* asp() */ )
		|
		#(ASTjste /* jste() */ )
		|
		#(ASTcdata /* cdata() */ )
		|
		(
			#(ASTdeclaration /* declaration() */ )
			|
			#(ASTdoctype doctype())
		)
	)
	[ #(ASTtext /* text() */ ) ]
}

void startTag() :
{
	/* "<" */
	/* <NAME> */
	//	[
	//		#(ASTattributes attributes())
	//	]
	//	( /* ">" */ | /* "/>" */ )
	[
		#(ASTscript /* script() */ )
		|
		#(ASTstyle /* style() */ )
		|
		#(ASTtext /* text() */ )
		(
			element()
			//			|
			//			#(ASTextraEndTag /* extraEndTag() */)
			//			[ #(ASTtext /* text() */) ]
		)*
		|
		(
			element()
			//			|
			//			#(ASTextraEndTag /* extraEndTag() */)
			//			[ #(ASTtext /* text() */) ]
		)+
	]
	[
		#(ASTendTag /* endTag() */ )
		[ #(ASTtext /* text() */ ) ]
	]
}

//void endTag() :
//{
//	/* "</" */
//	/* <NAME> */
//	[
//	/* ">" */
//	]
//}
//
//void extraEndTag() :
//{
//	/* "</" */
//	/* <NAME> */
//	[
//	/* ">" */
//	]
//}
//
//void comment() :
//{
//	/* "<!--" */
//	/* <COMMENT> */
//	/* "-->" */
//}
//
//void pi() :
//{
//	/* <LPI> */
//	/* <PI> */
//	/* "?>" */
//}
//
//void asp() :
//{
//	/* <LASP> */
//	/* <ASP> */
//	/* "%>" */
//}
//
//void jste() :
//{
//	/* <LJSTE> */
//	/* <JSTE> */
//	/* "#>" */
//}
//
//void cdata() :
//{
//	/* "<![CDATA[" */
//	/* <CDATA> */
//	/* "]]>" */
//}
//
//void declaration() :
//{
//	/* <LDECL> */
//	/* <DECLARATION> */
//	/* ">" */
//}

void attributes() :
{
	( #(ASTattribute /* attribute() */ ) )+
}

//void attribute() :
//{
//	/* <NAME> */
//	[
//		/* "=" */
//		/* <ATTVALUE | NAME> */
//		|
//		/* <STRING> */
//	]
//}
//
//void text() :
//{
//	(
//	/* <TEXT> */
//	)+
//}
//
//void script() :
//{
//	/* <SCRIPT> */
//}
//
//void style() :
//{
//	/* <STYLE> */
//}

void doctype() :
{
	//	/* <LDECL> */
	//	[
	//	/* <NAME> */
	//	]
	[ #(ASTdtdId /* dtdId() */ ) ]
	/* ">" */
}

//void dtdId() :
//{
//	/* <SYSTEM> */
//	/* <SYSTEM_LITERAL> */
//	|
//	/* <PUBLIC> */
//	/* <PUBID_LITERAL> */
//	[
//	/* <SYSTEM_LITERAL> */
//	]
//}
