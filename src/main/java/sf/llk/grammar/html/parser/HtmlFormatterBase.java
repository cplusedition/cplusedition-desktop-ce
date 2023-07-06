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
package sf.llk.grammar.html.parser;

import java.io.IOException;

import sf.llk.share.formatter.FormatBuilder;
import sf.llk.share.formatter.FormatterBase;
import sf.llk.share.support.ILLKMain;
import sf.llk.share.support.ILLKNode;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.support.INamePosition;
import sf.llk.share.support.LLKParseException;
import sf.llk.share.support.SimpleLLKMain;
import sf.llk.share.util.ISpaceUtil;

public abstract class HtmlFormatterBase extends FormatterBase implements ILLKHtmlFormatter {

	////////////////////////////////////////////////////////////////////////

	public static enum Mode {
		INLINE, BLOCK, PRE 
	}

	////////////////////////////////////////////////////////////////////////

	boolean relax;
	HtmlFormatOptions formatOptions;

	////////////////////////////////////////////////////////////////////////

	public abstract ILLKMain getMain();

	protected void init(int start, int end, HtmlFormatOptions options) {
		super.init(start, end, SPACES, NEWLINE);
		this.formatOptions = options;
		this.relax = getMain().getOptBool("relax");
	}

	////////////////////////////////////////////////////////////////////////

	protected void emitSpecial(FormatBuilder buf, ILLKToken t) {
		throw new AssertionError("Shoud not reach here.");
	}

	protected ILLKToken emitRestOfLine(FormatBuilder buf, ILLKToken t, boolean softbreak) {
		if (buf.endsWithLineBreak())
			return null;
		DONE: for (ILLKToken s = t.getSpecial(); s != null; s = s.getNext()) {
			int offset = s.getOffset();
			if (offset >= endOffset)
				break;
			if (offset < startOffset)
				continue;
			switch (s.getType()) {
			case SPACES:
				break;
			case NEWLINE:
				buf.newLine(softbreak);
				skipToken(s);
				return s;
			default :
				break DONE;
		}}
		buf.flushLine(softbreak);
		return null;
	}

	//////////////////////////////////////////////////////////////////////

	protected void convertTagCase(NamedNode node) {
		if (formatOptions.convertTagCase == HtmlFormatOptions.Case.LOWER) {
			node.setName(node.getName().toString().toLowerCase());
			if (node.getEndOffset() > node.getOffset()) {
				ILLKToken t = node.getFirstToken().getNext();
				t.setText(t.getText().toString().toLowerCase());
		}} else if (formatOptions.convertTagCase == HtmlFormatOptions.Case.UPPER) {
			node.setName(node.getName().toString().toUpperCase());
			if (node.getEndOffset() > node.getOffset()) {
				ILLKToken t = node.getFirstToken().getNext();
				t.setText(t.getText().toString().toUpperCase());
	}}}

	protected void convertAttrCase(INamePosition name) {
		if (formatOptions.convertAttrCase == HtmlFormatOptions.Case.LOWER) {
			name.setText(name.getText().toString().toLowerCase());
		} else if (formatOptions.convertAttrCase == HtmlFormatOptions.Case.UPPER) {
			name.setText(name.getText().toString().toUpperCase());
	}}

	//////////////////////////////////////////////////////////////////////

	protected boolean wrap(FormatBuilder buf, int context, boolean softbreak) {
		if (buf.canFit(0)) {
			buf.flushLine(softbreak);
			return false;
		}
		HtmlOnelinerFormatter.getSingleton(relax).setBreaks(buf, context);
		buf.wrap(null, null, softbreak);
		return true;
	}

	protected boolean inline(FormatBuilder buf, FormatBuilder b, int context) {
		return inline(buf, b, context, true);
	}

	protected boolean inline(FormatBuilder buf, FormatBuilder b, int context, boolean wasspace) {
		b.flush();
		StringBuilder formatted = b.getFormatted();
		try {
			String oneliner = HtmlOnelinerFormatter.getSingleton(relax).toOneliner(
				formatted,
				0,
				formatted.length(),
				wasspace,
				Integer.MAX_VALUE,
				b.getSoftBreaks(),
				context);
			if (oneliner != null) {
				if (!buf.canFit(oneliner, 0))
					wrap(buf, CONTEXT_TEXT, false);
				if (buf.canFit(oneliner, 0)) {
					buf.append(oneliner);
					return true;
				}
				buf.appendFormatted(b, false, false, true, wasspace, 0);
			} else {
				wrap(buf, CONTEXT_TEXT, false);
				buf.appendFormatted(b, false, false, true, wasspace, 0);
			}
			return false;
		} catch (Exception e) {
			getMain().error("Oneliner error: formatted=" + formatted, e, startOffset);
			return false;
	}}

	protected boolean format(FormatBuilder buf, FormatBuilder b, int context) {
		return format(buf, b, true, context);
	}

	protected boolean format(FormatBuilder buf, FormatBuilder b, boolean wasspace, int context) {
		b.flush();
		StringBuilder formatted = b.getFormatted();
		try {
			String oneliner = HtmlOnelinerFormatter.getSingleton(relax).toOneliner(
				formatted,
				0,
				formatted.length(),
				wasspace,
				buf.getLineWidth() - buf.getIndentWidth(),
				b.getSoftBreaks(),
				context);
			if (oneliner != null && buf.canFit(oneliner, 0)) {
				buf.appendFormattedOneLiner(formatted, oneliner);
				return true;
			}
			buf.appendFormatted(b, false, false, true, wasspace, 0);
			return false;
		} catch (Exception e) {
			getMain().error("Oneliner error: formatted=" + formatted, e, startOffset);
			return false;
	}}

	//////////////////////////////////////////////////////////////////////

	protected ILLKToken emitPre(FormatBuilder buf, int n, ILLKToken t) {
		StringBuilder b = new StringBuilder();
		for (; n > 0; --n) {
			getSpecial(b, t);
			b.append(t.getText());
			t = t.getNext();
		}
		buf.appendFormatted(b);
		return t;
	}

	protected void emitPre(FormatBuilder buf, ILLKToken t) {
		buf.appendFormatted(getSpecial(t));
		buf.appendFormatted(t.getText());
	}

	protected void emitPre(FormatBuilder buf, ILLKNode node) {
		StringBuilder b = new StringBuilder();
		for (ILLKToken t: node.tokens()) {
			getSpecial(b, t);
			b.append(t.getText());
		}
		buf.appendFormatted(b);
	}

	/** @return true if ended with line break. */
	protected boolean emitText(FormatBuilder buf, ILLKNode node, Mode mode, boolean ltrim, boolean rtrim) {
		StringBuilder b = new StringBuilder();
		for (ILLKToken t: node.tokens()) {
			b.append(t.getText());
		}
		if (mode == Mode.PRE) {
			buf.appendFormatted(b);
			return true;
		}
		ISpaceUtil util = buf.getSpaceUtil();
		int n = util.rcountBlankLines(b);
		boolean trim = ltrim || rtrim;
		if (util.isWhitespaces(b)) {
			if (n >= 1) {
				if (mode == Mode.INLINE) {
					wrap(buf, CONTEXT_TEXT, trim);
				} else {
					buf.flushLine(trim);
				}
				buf.rtrimBlankLines();
				buf.newLine(trim);
				return true;
			}
			if (!trim && b.length() > 0) {
				buf.space();
			}
			return n >= 0;
		}
		String s = HtmlOnelinerFormatter.getSingleton(relax).trimText(
			b, ltrim || buf.endsWithSpaces(), false, 0, null).toString();
		int end = s.length();
		if (rtrim) {
			end = util.rskipWhitespaces(s, 0, s.length());
		}
		buf.append(s, 0, end);
		if (!rtrim && n > 0) {
			if (mode == Mode.INLINE)
				wrap(buf, CONTEXT_TEXT, trim);
			for (int i = n; i > 0; --i)
				buf.newLine();
		}
		return n >= 0;
	}

	protected void emitScript(FormatBuilder buf, ILLKNode node, Mode mode) {
		ILLKToken t = node.getFirstToken();
		StringBuilder b = new StringBuilder(t.getText());
		b.append(buf.getLineBreak());
		ISpaceUtil util = buf.getSpaceUtil();
		util.ltrimBlankLines(b);
		util.rtrimBlankLines(b);
		buf.flushLine();
		buf.unIndentBlock(b);
		buf.appendBlock(b);
		buf.flushLine();
	}

	protected void emitStyle(FormatBuilder buf, ILLKNode node, Mode mode) {
		ILLKToken t = node.getFirstToken();
		StringBuilder b = new StringBuilder(t.getText());
		b.append(buf.getLineBreak());
		ISpaceUtil util = buf.getSpaceUtil();
		util.ltrimBlankLines(b);
		util.rtrimBlankLines(b);
		buf.flushLine();
		buf.unIndentBlock(b);
		buf.appendBlock(b);
		buf.flushLine();
	}

	/* "<!--" */
	/* <COMMENT> */
	/* "-->" */
	protected Mode emitComment(FormatBuilder buf, ILLKNode node, boolean wasbreak, Mode mode) {
		if (mode == Mode.PRE) {
			emit(buf, node, -1, -1);
			return Mode.PRE;
		}
		ILLKToken t = node.getFirstToken();
		if (wasbreak) {
			if (mode == Mode.INLINE)
				wrap(buf, CONTEXT_TEXT, false);
			else
				buf.flushLine();
		} else {
			buf.space();
		}
		FormatBuilder b = getBuf(buf);
		emit(b, t, 0, 0);
		b.flushLine();
		b.indent();
		t = t.getNext();
		CharSequence text = t.getText();
		int len = text.length();
		ISpaceUtil util = buf.getSpaceUtil();
		b.indentLines(text, util.skipWhitespaces(text, 0, len), len);
		b.flushLine();
		b.unIndent();
		emit(b, t.getNext(), 0, 0);
		boolean hasbreak = hasBreak(node.getNext(), util);
		if (mode == Mode.INLINE) {
			if (!wasbreak && !buf.getSpaceUtil().hasLineBreak(text)) {
				inline(buf, b, CONTEXT_TEXT);
				if (hasbreak)
					wrap(buf, CONTEXT_TEXT, false);
			} else {
				wrap(buf, CONTEXT_TEXT, false);
				format(buf, b, true, CONTEXT_TEXT);
		}} else {
			format(buf, b, true, CONTEXT_TEXT);
		}
		if (hasbreak) {
			buf.flushLine();
		} else {
			buf.space();
		}
		return (wasbreak || hasbreak ? Mode.BLOCK : Mode.INLINE);
	}

	/* "<LPI> */
	/* <PI> */
	/* "?>" */
	protected Mode emitPI(FormatBuilder buf, ILLKNode node, boolean wasbreak, Mode mode) {
		ILLKToken t = node.getFirstToken();
		ILLKToken target = (ILLKToken)t.getData();
		if (target != null) {
			CharSequence s = target.getText();
			if ("xml".contentEquals(s)) {
				emitXmlDeclaration(buf, node);
				return mode;
		}}
		if (mode == Mode.PRE) {
			emit(buf, node, -1, -1);
			return Mode.PRE;
		}
		if (wasbreak) {
			if (mode == Mode.INLINE)
				wrap(buf, CONTEXT_TEXT, false);
			else
				buf.flushLine();
		} else {
			buf.space();
		}
		FormatBuilder b = getBuf(buf);
		emit(b, node, -1, -1);
		CharSequence text = t.getNext().getText();
		boolean hasbreak = hasBreak(node.getNext(), buf.getSpaceUtil());
		if (mode == Mode.INLINE) {
			if (!wasbreak && !buf.getSpaceUtil().hasLineBreak(text)) {
				inline(buf, b, CONTEXT_TEXT);
				if (hasbreak)
					wrap(buf, CONTEXT_TEXT, false);
			} else {
				wrap(buf, CONTEXT_TEXT, false);
				format(buf, b, true, CONTEXT_TEXT);
		}} else {
			format(buf, b, true, CONTEXT_TEXT);
		}
		if (hasbreak) {
			buf.flushLine();
		} else {
			buf.space();
		}
		return (wasbreak || hasbreak ? Mode.BLOCK : Mode.INLINE);
	}

	/* <! */
	/* [ <IDENTIFIER> ] */
	/* <DECLARATION> */
	/* ">" */
	protected Mode emitDeclaration(FormatBuilder buf, ILLKNode node, boolean wasbreak, Mode mode) {
		emit(buf, node, -1, -1);
		buf.flushLine(true);
		return Mode.BLOCK;
	}

	/* "<![CDATA[" */
	/* <CDATA> */
	/* "]]>" */
	protected void emitCData(FormatBuilder buf, ILLKNode node, Mode mode) {
		emit(buf, node, -1, -1);
	}

	/* "<![" */
	/* <COND> */
	/* "]>" */
	protected void emitCond(FormatBuilder buf, ILLKNode node, Mode mode) {
		emit(buf, node, -1, -1);
	}

	protected void emitDocType(FormatBuilder buf, ILLKNode node) {
		emit(buf, node, -1, -1);
		buf.flushLine(true);
	}

	protected void emitXmlDeclaration(FormatBuilder buf, ILLKNode node) {
		ILLKToken t = node.getFirstToken();
		emit(buf, t, 0, 0);
		buf.space();
		FormatBuilder b = getBuf(buf);
		t = t.getNext();
		char[] source = t.getText().toString().toCharArray();
		try {
			ILLKNode attrs = lexAttributes(source);
			int startsaved = startOffset;
			int endsaved = endOffset;
			startOffset = 0;
			endOffset = source.length;
			validateXmlDeclaration(attrs);
			emitAttributes(b, attrs, false, false);
			startOffset = startsaved;
			endOffset = endsaved;
			format(buf, b, CONTEXT_TAG);
			t = t.getNext();
			emit(buf, t, 1, 0);
		} catch (Exception e) {
			getMain().error("Error formatting xml declaration: " + node, e);
			emitn(buf, 2, t, 1, 0);
		}
		buf.flushLine();
	}

	protected boolean emitAttributes(FormatBuilder b, ILLKToken t, ASTattributes attrs, boolean inline) {
		FormatBuilder bb = cloneBuf(b);
		emitSpecial(bb, t, 0, 1);
		emitn(bb, 2, t, 0, 0);
		emitAttributes(bb, attrs, inline, false);
		if (inline)
			return inline(b, bb, CONTEXT_TEXT);
		return format(b, bb, CONTEXT_TEXT);
	}

	protected void emitAttributes(FormatBuilder buf, ILLKNode attrs, boolean inline, boolean indented) {
		for (ILLKNode c: attrs.children()) {
			ASTattribute attr = (ASTattribute)c;
			convertAttrCase(attr.getNameToken());
			emitAttribute(buf, attr, inline, indented);
			indented = true;
		}
		if (indented)
			buf.unIndentAfterBreak();
	}

	/* <IDENTIFIER> */
	/* "=" */
	/* <STRING | IDENTIFIER> */
	protected void emitAttribute(FormatBuilder buf, ASTattribute node, boolean inline, boolean indented) {
		ILLKToken t = node.getFirstToken();
		if (inline) {
			buf.space();
		} else {
			buf.flushLine();
			if (!indented)
				buf.indent();
		}
		emit(buf, t, 0, 0);
		t = t.getNext();
		int end = node.getEndOffset();
		if (t.getOffset() < end)
			emit(buf, t, 0, 0);
		t = t.getNext();
		if (node.getValueToken() == null)
			return;
		emitSpecial(buf, t, 0, 0);
		if (t.getType() != ATTVALUE) {
			emitText(buf, t);
			return;
		}
		ISpaceUtil util = buf.getSpaceUtil();
		CharSequence s = t.getText();
		int len = s.length();
		int start = util.skipSpaces(s, 0, len);
		int index = util.skipLine(s, start, len);
		if (index < 0) {
			buf.append(s, start, len);
			return;
		}
		StringBuilder b = new StringBuilder();
		b.append(s.charAt(start));
		b.append(s, util.skipSpaces(s, start + 1, index), index);
		if (inline) {
			HtmlOnelinerFormatter.getSingleton(relax).setBreaks(buf, CONTEXT_TEXT);
			buf.flush();
			buf.unshiftNonTerminatedLine();
			if (!buf.canFit(b, 0))
				buf.flushLine();
		}
		buf.append(b);
		buf.flushLine();
		if (!formatOptions.indentAttributeValue) {
			buf.appendFormatted(s, index + 1, len);
			return;
		}
		buf.indent();
		buf.indentLines(s, index + 1, len);
		if (buf.isBlank())
			buf.unIndent();
		else
			buf.unIndentAfterBreak();
	}

	protected void emitDtdBlock(FormatBuilder buf, ILLKNode node) {
		ILLKToken start = node.getFirstToken();
		FormatBuilder b = cloneBuf(buf);
		int end = node.getEndOffset();
		b.flushLine();
		for (ILLKToken t = start; t.getOffset() < end;) {
			emit(b, t, 0, 0);
			t = t.getNext();
			if (t.getOffset() < end)
				b.flushLine();
		}
		format(buf, b, CONTEXT_DTD);
	}

	protected void emitDtdId(FormatBuilder buf, ILLKNode node) {
		emitDtdBlock(buf, node);
	}

	protected void emitDtdPublicID(FormatBuilder buf, ILLKNode node) {
		emitDtdBlock(buf, node);
	}

	//////////////////////////////////////////////////////////////////////

	private ASTattributes lexAttributes(char[] source) throws IOException, LLKParseException {
		HtmlLexer lexer = new HtmlLexer(source, new SimpleLLKMain());
		lexer.llkSetContext(CONTEXT, CONTEXT_TAG);
		HtmlDomParser parser = new HtmlDomParser(lexer);
		return parser.attributes();
	}

	private void validateXmlDeclaration(ILLKNode attrs) {
	}

	boolean hasBreak(ILLKNode node, ISpaceUtil util) {
		if (node == null || node.getType() != ASTtext)
			return false;
		StringBuilder b = new StringBuilder();
		for (ILLKToken t: node.tokens()) {
			b.append(t.getText());
		}
		int len = b.length();
		for (int i = 0; i < len; ++i) {
			if (util.isSpace(b.charAt(i)))
				continue;
			if (util.skipLineBreak(b, i, len) != i)
				return true;
			return false;
		}
		return false;
	}

	//////////////////////////////////////////////////////////////////////
}
