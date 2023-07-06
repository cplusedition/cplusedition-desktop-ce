%OPTIONS {
	Language = "Java";
}

%LEXER (HtmlLexer)

import sf.llk.grammar.html.IHtmlConstants;
import sf.llk.grammar.html.IHtmlLexicalHandler;
import sf.llk.grammar.html.MsgId;
import sf.llk.share.support.ILLKConstants;
import sf.llk.share.support.ILLKLexerInput;
import sf.llk.share.support.ILLKLifeCycleListener;
import sf.llk.share.support.ILLKMain;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.support.ILLKTokenHandler;
import sf.llk.share.support.INamePool;
import sf.llk.share.support.IntList;
import sf.llk.share.support.LLKLexerBase;
import sf.llk.share.support.LLKLexerInput;
import sf.llk.share.support.LLKParseException;
import sf.llk.share.support.LexerSourceTokenHandler;
import sf.llk.share.support.NamePool;

/*
 * Copyright (c) 2005, Chris Leung. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2 with a special exception, the Classpath exception.
 * You should have received a copy of the GNU General Public License (GPL)
 * and the Classpath exception along with this library.
 */
public class HtmlLexer extends LLKLexerBase implements IHtmlLexer, ILLKHtmlLexer {

	////////////////////////////////////////////////////////////////////////

	private static final int[] END_PI = new int[] { '?', '>' };
	private static final int[] END_ASP = new int[] { '%', '>' };
	private static final int[] END_JSTE = new int[] { '#', '>' };
	private static final int[] END_CDATA = new int[] { ']', ']', '>' };
	private static final int[] END_COND = new int[] { ']', '>' };
	private static final int[] END_DECLARATION = new int[] { '>' };
	private static final int[] END_SCRIPT = new int[] { '<', '/' };

	////////////////////////////////////////////////////////////////////////

	// protected char[] source;
	protected IHtmlLexicalHandler lexHandler;
	protected INamePool namePool;
	protected boolean attlistDeclaration;
	protected boolean relax;
	protected IntList contextStack;

	//////////////////////////////////////////////////////////////////////

	public HtmlLexer(char[] source, ILLKMain main) {
		llkInput = new LLKLexerInput(source, main);
		llkMain = llkInput.getMain();
		llkLocator = llkInput.getLocator();
		llkTokenHandler = new LexerSourceTokenHandler(source, new NamePool(1024, 2.0f), LLK_SPECIAL_TOKENS);
		llkInit();
	}

	public HtmlLexer(ILLKLexerInput input, ILLKTokenHandler tokenhandler) {
		llkInput = input;
		llkMain = llkInput.getMain();
		llkLocator = llkInput.getLocator();
		llkTokenHandler = tokenhandler;
		namePool = tokenhandler.namePool();
		llkInit();
	}

	//////////////////////////////////////////////////////////////////////

	public static boolean isNameStartChar(int c) {
		return llkGetBit(c, NameStartChar.bitset);
	}

	public static boolean isNameChar(int c) {
		return llkGetBit(c, NameChar.bitset);
	}

	public static int[] specialTokens() {
		return LLK_SPECIAL_TOKENS.clone();
	}

	//////////////////////////////////////////////////////////////////////

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public void llkInit() {
		this.relax = llkMain.getOptBool(IHtmlConstants.OPT_RELAX);
		// source = llkInput.getSource();
		namePool = llkTokenHandler.namePool();
		contextStack = new IntList();
		pushContext(CONTEXT_TEXT);
	}

	public ILLKToken createDataToken(int type, Object data) {
		return llkTokenHandler.createToken(type, llkTokenStart, llkGetOffset(), data);
	}

	public void setLexicalHandler(IHtmlLexicalHandler handler) {
		this.lexHandler = handler;
	}

	public IHtmlLexicalHandler getLexicalHandler() {
		return lexHandler;
	}

	//////////////////////////////////////////////////////////////////////

	public int getLine() {
		return llkInput.getLocator().getLinear(llkGetOffset());
	}

	public String getLineString() {
		return "@" + getLine();
	}

	public void pushContext(int context) {
		contextStack.push(llkGetContext(CONTEXT));
		llkSetContext(CONTEXT, context);
	}

	public void popContext() {
		llkSetContext(CONTEXT, contextStack.pop());
	}

	//////////////////////////////////////////////////////////////////////

	protected ILLKToken checkEOF() throws LLKParseException {
		if (LA1() == ILLKConstants.LEXER_EOF)
			return llkCreateEOF(llkTokenStart);
		throw llkParseException("Unexpected token");
	}

	private boolean matchIgnoreCase(int offset, String name) {
		for (int i = 0; i < name.length(); ++i) {
			int la = LA(offset + i);
			if (la < 0 || Character.toLowerCase(la) != name.charAt(i))
				return false;
		}
		return true;
	}

	private int optionalSpaces(int n) {
		for (int la = LA(n); la == ' ' || la == '\t';) {
			la = LA(++n);
		}
		return n;
	}

	private void adjustLocation(int la) {
		if (la == '\t')
			llkInput.tab();
		else if (la == '\n')
			llkInput.newline();
		else if (la == '\r') {
			if (LA1() == '\n')
				llkConsume();
			llkInput.newline();
		}
	}

	private boolean isValidTag() {
		llkInput.mark();
		int start = llkInput.getOffset();
		boolean ret = llkSyn_NAME();
		//		if (isname) {
		//			String name = llkInput.getSource(start).toString();
		//			HtmlTag tag = HtmlTag.get(name);
		//			ret = (tag != null && tag != HtmlTag.UNKNOWN);
		//		}
		llkInput.rewind();
		return ret;
	}

	private boolean isEndOfAttributes() {
		int la1 = LA1();
		if (la1 == '>')
			return true;
		if ((la1 == '/' || la1 == '?' || la1 == '%' || la1 == '#') && LA(2) == '>')
			return true;
		return false;
	}

	private void warn(MsgId id, Exception e, int offset) {
		llkMain.warn(id.getMessage(), e, offset);
	}

	private void warn(MsgId id, String msg, Exception e, int offset) {
		llkMain.warn(id.getMessage() + ": " + msg, e, offset);
	}

	private void error(MsgId id, Exception e, int offset) {
		llkMain.error(id.getMessage(), e, offset);
	}

	//	private void error(MsgId id, String msg, Exception e, int offset) {
	//		llkMain.error(id.getMessage() + ": " + msg, e, offset);
	//	}

	////////////////////////////////////////////////////////////////////////
}

%OPTIONS {
	DefaultErrorHandler = false;
	Greedy = true;
	IgnoreCase = false;
	TokenUseText = true;
	Lookahead = 1;
	GenerateConstructor = "none";
	ResetHook = true; // false;
	StaticKeywordLevel = 8;
	StaticKeyword = true; // false
	KeywordIgnoreCase = true;
	Vocabulary = 0xffff;
	GenHelper = false;
	//
	GreedyMatch = false;
}

////////////////////////////////////////////////////////////////////////
// Keywords

%KEYWORDS {
	LSCRIPT = "script";
	LSTYLE = "style";
}

////////////////////////////////////////////////////////////////////////

ENDEMPTY : "/>";

LT : "<";
GT : ">";
ASSIGN : "=";
ENDTAG : "</";
DTDCOMMENT : "--";
LCOMMENT : "<!--";
RCOMMENT : "-->";
LCDATA : "<![CDATA[";
RCDATA : "]]>";
RCOND : "]>";
RPI : "?>";
RASP : "%>";
RJSTE : "#>";
PUBLIC : "PUBLIC";
SYSTEM : "SYSTEM";

LCOND; // "<!["
LDECL; // : "<!xxx";
LPI; // "<?xxx";
LASP; // "<%xxx";
LJSTE; // "<#xxx";

DECLARATION;
COMMENT;
PI;
ASP;
JSTE;
CDATA;
COND;
SCRIPT;
STYLE;
TEXT;

VOID;

////////////////////////////////////////////////////////////////////////

// HTML 4.0
// ID and NAME tokens must begin with a letter ([A-Za-z]) and may be
// followed by any number of letters, digits ([0-9]), hyphens ("-"), underscores ("_"), colons (":"), and periods (".").
NameStartChar = <'A'..'Z', 'a'..'z'>;

NameChar = <
	NameStartChar
	| '0'..'9'
	| '-'
	| '_'
	| ':'
	| '.'
>;

PubChar = <
	NameStartChar
	| '0'..'9'
	| " \r\n-\'()+,./:=?;!*#@$_%"
>;

Whitespaces = <" \t\n\r\f\u200b">;

void llkNextToken() :
{
	switch (CONTEXT) {
		case TEXT :
		[
			(
				~<"<&\t\n\r">
				|
				'\t'
				{ llkInput.tab(); }
				|
				'\n'
				{ llkInput.newline(); }
				|
				'\r' [ '\n' ]
				{ llkInput.newline(); }
				|
				REFERENCE()
			)+
			{ llktype1 = TEXT; }
			|
			llktoken1=START_TAG()
			|
			{ llktoken1 = checkEOF(); }
		]
		|
		case TAG :
		[
			llktoken1=START_TAG()
			|
			'/'
			[
				%LA(0, { relax && llkGetBit(LA1(), Whitespaces.bitset) })
				( SPACES() | NEWLINE() )+
			]
			'>'
			{
				llktype1 = ENDEMPTY;
				popContext();
			}
			|
			'>'
			{
				llktype1 = GT;
				popContext();
			}
			|
			"--" [ SPACES() ] '>'
			{
				llktype1 = RCOMMENT;
				popContext();
			}
			|
			"?>"
			{
				llktype1 = RPI;
				popContext();
			}
			|
			"%>"
			{
				llktype1 = RASP;
				popContext();
			}
			|
			"#>"
			{
				llktype1 = RJSTE;
				popContext();
			}
			|
			%LA("]]>")
			"]]>"
			{
				llktype1 = RCDATA;
				popContext();
			}
			|
			"]>"
			{
				llktype1 = RCOND;
				popContext();
			}
			|
			llktoken1=NAME()
			|
			'='
			{
				llktype1 = ASSIGN;
				if (!(relax && isEndOfAttributes()))
					pushContext(CONTEXT_ATTRVALUE);
			}
			|
			STRING()
			{ llktype1 = STRING; }
			|
			SPACES()
			{ llktype1 = SPACES; }
			|
			NEWLINE()
			{ llktype1 = NEWLINE; }
			|
			{ llktoken1 = checkEOF(); }
		]
		|
		case DTD :
		[
			"]]>"
			{
				llktype1 = RCDATA;
				popContext();
			}
			|
			"?>"
			{
				llktype1 = RPI;
				popContext();
			}
			|
			llktoken1=START_TAG()
			|
			"/>"
			{
				llktype1 = ENDEMPTY;
				popContext();
			}
			|
			'>'
			{
				llktype1 = GT;
				popContext();
			}
			|
			"--"
			[
				%LA([ SPACES() ] '>')
				[ SPACES() ] '>'
				{
					llktype1 = RCOMMENT;
					popContext();
				}
				|
				{
					llktype1 = DTDCOMMENT;
					popContext();
					pushContext(CONTEXT_COMMENT);
				}
			]
			|
			llktoken1=NAME()
			{
				String name = llktoken1.getText().toString();
				if ("SYSTEM".equalsIgnoreCase(name)) {
					if (!"SYSTEM".equals(name)) {
						warn(MsgId.LexerSystem, null, llktoken1.getOffset());
					}
					llktoken1.setType(SYSTEM);
					pushContext(CONTEXT_SYSTEM);
				} else if ("PUBLIC".equalsIgnoreCase(name)) {
					if (!"PUBLIC".equals(name)) {
						warn(MsgId.LexerPublic, null, llktoken1.getOffset());
					}
					llktoken1.setType(PUBLIC);
					pushContext(CONTEXT_PUBID);
				}
			}
			//			|
			//			'=' { llktype1 = ASSIGN; }
			//			|
			//			%LA({ attlistDeclaration })
			//			ATTVALUE()
			|
			SPACES()
			{ llktype1 = SPACES; }
			|
			NEWLINE()
			{ llktype1 = NEWLINE; }
			|
			{ llktoken1 = checkEOF(); }
		]
		|
		case ATTRVALUE :
		[
			ATTVALUE()
			{
				llktype1 = ATTVALUE;
				popContext();
			}
			|
			SPACES()
			{ llktype1 = SPACES; }
			|
			NEWLINE()
			{ llktype1 = NEWLINE; }
			|
			{ llktoken1 = checkEOF(); }
		]
		|
		case PUBID :
		[
			PUBID_LITERAL()
			{
				llktype1 = PUBID_LITERAL;
				popContext();
				pushContext(CONTEXT_SYSTEM);
			}
			|
			SPACES()
			{ llktype1 = SPACES; }
			|
			NEWLINE()
			{ llktype1 = NEWLINE; }
			|
			{ llktoken1 = checkEOF(); }
		]
		|
		case SYSTEM :
		[
			SYSTEM_LITERAL()
			{
				llktype1 = SYSTEM_LITERAL;
				popContext();
			}
			|
			SPACES()
			{ llktype1 = SPACES; }
			|
			NEWLINE()
			{ llktype1 = NEWLINE; }
			| ">"
			{
				llktype1 = GT;
				popContext(); // SYSTEM
				popContext(); // DTD
			}
			|
			{ llktoken1 = checkEOF(); }
		]
		|
		case COMMENT :
		endComment()
		{
			llktype1 = COMMENT;
			popContext();
		}
		|
		case PI :
		skip(END_PI)
		{
			llktype1 = PI;
			popContext();
		}
		|
		case ASP :
		skip(END_ASP)
		{
			llktype1 = ASP;
			popContext();
		}
		|
		case JSTE :
		skip(END_JSTE)
		{
			llktype1 = JSTE;
			popContext();
		}
		|
		case CDATA :
		skip(END_CDATA)
		{
			llktype1 = CDATA;
			popContext();
		}
		|
		case COND :
		skip(END_COND)
		{
			llktype1 = COND;
			popContext();
		}
		|
		case DECLARATION :
		skip(END_DECLARATION)
		{
			llktype1 = DECLARATION;
			popContext();
		}
		|
		case SCRIPT :
		endScript(END_SCRIPT, "script")
		{
			llktype1 = SCRIPT;
			popContext();
		}
		|
		case STYLE :
		endScript(END_SCRIPT, "style")
		{
			llktype1 = STYLE;
			popContext();
		}
	}
}

protected ILLKToken START_TAG() #void {
	ILLKToken ret = null;
	int type1;
	boolean isdtd = llkGetContext(CONTEXT) == CONTEXT_DTD;
	if (isdtd) {
		pushContext(CONTEXT_DTD);
	} else {
		pushContext(CONTEXT_TAG);
	}
}
{
	'<' { type1 = LT; }
	[
		'/'
		{ type1 = ENDTAG; }
		|
		'?' [ t:NAME() ]
		{
			type1 = LPI;
			pushContext(CONTEXT_PI);
			ret = createDataToken(type1, t);
		}
		|
		'%' [ t:NAME() ]
		{
			type1 = LASP;
			pushContext(CONTEXT_ASP);
			ret = createDataToken(type1, t);
		}
		|
		'#' [ t:NAME() ]
		{
			type1 = LJSTE;
			pushContext(CONTEXT_JSTE);
			ret = createDataToken(type1, t);
		}
		|
		'!' { type1 = LDECL; }
		[
			'-'
			[
				'-'
				|
				{ error(MsgId.LexerMissingDash, null, llkGetOffset()); }
			]
			{
				type1 = LCOMMENT;
				pushContext(CONTEXT_COMMENT);
			}
			|
			%LA({ !isdtd })
			"["
			[
				%LA("CDATA[")
				"CDATA["
				{
					type1 = LCDATA;
					pushContext(CONTEXT_CDATA);
				}
				|
				{
					type1 = LCOND;
					pushContext(CONTEXT_COND);
				}
			]
			|
			t:NAME()
			{
				type1 = LDECL;
				ret = createDataToken(type1, t);
				String text = t.getText().toString();
				if (!isdtd) {
					if ("DOCTYPE".equalsIgnoreCase(text)) {
						popContext();
						pushContext(CONTEXT_DTD);
					} else {
						pushContext(CONTEXT_DECLARATION);
					}
				}
			}
			|
			{
				if (!isdtd)
					pushContext(CONTEXT_DECLARATION);
			}
		]
		|
		{
			if (!isdtd && !isValidTag()) {
				warn(MsgId.LexerEscLT, null, llkGetOffset() - 1);
				type1 = TEXT;
				ret = createDataToken(type1, null);
				popContext();
			}
		}
	]
}
{
	if (ret == null)
		ret = createDataToken(type1, null);
	return ret;
}

%CODE void skip(int[] endtag) {
	for (int la1 = LA1(); la1 >= 0; la1 = LA1()) {
		NOTMATCH: if (la1 == endtag[0]) {
			int lax;
			for (int i = 1; i < endtag.length; ++i) {
				lax = LA(i + 1);
				if (lax < 0 || lax != endtag[i])
					break NOTMATCH;
			}
			return;
		}
		llkConsume();
		adjustLocation(la1);
	}
}

%CODE void endScript(int[] endtag, String name) {
	for (int la1 = LA1(); la1 >= 0; la1 = LA1()) {
		NOTMATCH: if (la1 == endtag[0]) {
			int i = 1;
			int lax;
			for (; i < endtag.length; ++i) {
				lax = LA(i + 1);
				if (lax < 0 || lax != endtag[i])
					break NOTMATCH;
			}
			if (!llkGetBit(LA(i + 1), NameStartChar.bitset))
				break NOTMATCH;
			if (!matchIgnoreCase(i + 1, name)) {
				warn(MsgId.LexerEscEndTag, "element=" + name, null, llkGetOffset());
				break NOTMATCH;
			}
			return;
		}
		llkConsume();
		adjustLocation(la1);
	}
}

%CODE void endComment() {
	for (int la1 = LA1(); la1 >= 0; la1 = LA1()) {
		NOTMATCH: if (la1 == '-') {
			if (LA(2) == '-') {
				int i = optionalSpaces(3);
				if (i < 0)
					break NOTMATCH;
				if (LA(i) == '>')
					return;
			}
		}
		llkConsume();
		adjustLocation(la1);
	}
}

%SPECIAL void SPACES() :
{
	( ' ' | '\f' | '\u200b' | '\t' { llkInput.tab(); } )+
}

%SPECIAL void NEWLINE() :
{
	'\r' [ '\n' ]
	{ llkInput.newline(); }
	|
	'\n'
	{ llkInput.newline(); }
}

////////////////////////////////////////////////////////////////////////
// IDENTIFIERS

ILLKToken NAME() {
	int start = llkGetOffset();
}
{
	(
		<NameStartChar>
		|
		'_'
		{ error(MsgId.Lexer_NotValid, null, llkGetOffset()); }
	)
	<NameChar>*
}
{
	int end = llkGetOffset();
	return llkTokenHandler.createTextToken(NAME, start, end, namePool.intern(llkInput, start, end));
}

////////////////////////////////////////////////////////////////////////
// LITERALS

void ATTVALUE() :
{
	STRING()
	|
	(
		%LA({ relax })
		~<"\"'<>", Whitespaces>
		(
			~<"<>", Whitespaces>
			|
			%LA(laSpaceNotName())
			<Whitespaces>+
			~<NameStartChar, "_-<>">+
		)*
		|
		ATTVALUE_LITERAL()
		( ~<"<>", Whitespaces> )*
	)
}

void STRING() :
{
	'"'
	(
		~<"\"\t\r\n&">
		|
		ATTVALUE1()
	)*
	'"'
	|
	'\''
	(
		~<"'\t\r\n&">
		|
		ATTVALUE1()
	)*
	'\''
}

protected void ATTVALUE_LITERAL() {
	int offset;
}
{
	(
		<
			NameStartChar
			| "0123456789-."
			| ':' // HTML4.0.1
		>
		|
		<
			"+#%!@$^*()_[]{};|?/\\"
			// |
			// ':' // HTML4.0
		>
		{
			offset = llkGetOffset() - 1;
			warn(MsgId.LexerQuoteChar, llkGetSource(offset, offset + 1).toString(), null, offset);
		}
	)+
}

void SYSTEM_LITERAL() :
{
	'"' ( ~<'"'> )* '"'
	|
	'\'' ( ~<'\''> )* '\''
}

void PUBID_LITERAL() :
{
	'"' ( <PubChar> )* '"'
	|
	'\'' ( <PubChar - '\''> )* '\''
}

////////////////////////////////////////////////////////////////////////

protected void ATTVALUE1() #void :
{
	'\t'
	{ llkInput.tab(); }
	|
	'\r' [ '\n' ]
	{ llkInput.newline(); }
	|
	'\n'
	{ llkInput.newline(); }
	|
	REFERENCE()
}

protected void REFERENCE() {
	int start = llkGetOffset();
}
{
	'&'
	[
		'#'
		{ start = llkGetOffset(); }
		(
			<'0'..'9'>+
			|
			'x' <'0'..'9', 'a'..'f', 'A'..'F'>
		)
		[
			';'
			{ name = llkTokenHandler.createToken(REFERENCE, start, llkGetOffset() - 1); }
			|
			{ start -= 2; }
		]
		|
		%LA(NAME() ';')
		name:NAME() ';'
	]
}
{
	if (name == null) {
		if (!relax)
			warn(MsgId.LexerEscAmp, null, start);
	} else if (lexHandler != null)
		lexHandler.reference(name);
}

private void laSpaceNotName() :
{
	<Whitespaces>+
	~<NameStartChar, "_-<>">
}

////////////////////////////////////////////////////////////////////////
