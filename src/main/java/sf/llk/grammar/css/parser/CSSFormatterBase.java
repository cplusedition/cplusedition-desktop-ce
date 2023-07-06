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

import sf.llk.share.formatter.FormatBuilder;
import sf.llk.share.formatter.FormatterBase;
import sf.llk.share.formatter.FormatterBase2;
import sf.llk.share.support.ILLKParser;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.support.LLKParseException;
import sf.llk.share.util.ISpaceUtil;

/**
 * @author chrisl
 */
public abstract class CSSFormatterBase extends FormatterBase2 implements ILLKParser, ILLKCSSFormatterParser {

	////////////////////////////////////////////////////////////////////////

	CSSFormatOptions formatOptions;

	////////////////////////////////////////////////////////////////////////

	void init(int offset, int end, CSSFormatOptions options) {
		super.init(offset, end, SPACES, NEWLINE);
		formatOptions = options;
	}

	//////////////////////////////////////////////////////////////////////

	public CSSFormatOptions getOptions() {
		return formatOptions;
	}

	////////////////////////////////////////////////////////////////////////

	protected void emitSpecial(FormatBuilder buf, ILLKToken t) {
		ILLKToken head = t.getSpecial();
		ISpaceUtil util = buf.getSpaceUtil();
		for (ILLKToken s = head; s != null && s != t; s = s.getNext()) {
			int offset = s.getOffset();
			if (offset >= endOffset)
				break;
			if (offset < startOffset)
				continue;
			int type;
			type = s.getType();
			switch (type) {
			case NEWLINE:
				buf.newLine();
				break;
			case SPACES:
				if (s == head && s.getNext() != null && s.getNext() != t) {
					if (isComment(s.getNext())) {
						buf.rtrimSpaces();
						buf.append(s.getText());
						buf.space();
				}}
				break;
			case COMMENT:
				CharSequence ss = s.getText();
				if (util.skipLine(ss, 0, ss.length()) >= 0) {
					buf.flushLine();
					buf.appendBlock(
						FormatterBase.indentComment(
							s,
							formatOptions.lineBreak,
							llkGetMain().getLocator(),
							buf.getSpaceUtil()));
					s = skipSpaces(s.getNext(), t);
				} else {
					buf.space();
					buf.append(s.getText());
				}
				buf.space();
				break;
			case CDO:
			case CDC:
				buf.append(s.getText());
				break;
			default :
				throw new AssertionError("Invalid special token: " + s);
		}}
		skip(t.getOffset());
	}

	public ILLKToken emitRestOfLine(FormatBuilder buf, ILLKToken t, boolean softbreak) {
		if (buf.endsWithLineBreak())
			return null;
		ISpaceUtil util = buf.getSpaceUtil();
		DONE: for (ILLKToken s = t.getSpecial(); s != null && s != t; s = s.getNext()) {
			int offset = s.getOffset();
			if (offset >= endOffset)
				break;
			if (offset < startOffset)
				continue;
			switch (s.getType()) {
			case SPACES:
				break;
			case COMMENT:
				CharSequence ss = s.getText();
				if (util.skipLine(ss, 0, ss.length()) >= 0)
					break DONE;
				buf.space();
				buf.append(s.getText());
				skipToken(s);
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

	private boolean isComment(ILLKToken t) {
		return t.getType() == COMMENT;
	}

	public void flushSpecial(FormatBuilder buf, int spaces, int breaks) {
		ILLKToken lt1 = scanSpecial();
		emitSpecial(buf, lt1, spaces, breaks);
	}

	private ILLKToken scanSpecial() {
		ILLKToken lt0 = LT0();
		ILLKToken lt1 = LT1();
		ILLKToken start = null;
		for (int type = lt1.getType();
			type == SPACES || type == NEWLINE || type == COMMENT;
			lt1 = LT1(),
			type = lt1.getType()) {
			if (start == null)
				start = LT0();
		}
		if (start != null) {
			LT0().setNext(null);
			lt1.setSpecial(start);
			lt0.setNext(lt1);
		}
		return lt1;
	}

	public ILLKToken startBlock(FormatBuilder buf, ILLKToken t) {
		if (formatOptions.breakOpenBrace) {
			emitSpecial(buf, t, 1, 1);
			buf.flushLine();
			emitText(buf, t);
		} else {
			emitSpace(buf, t);
		}
		ILLKToken lt1 = t.getNext();
		emitRestOfLine(buf, lt1);
		buf.indent();
		return lt1;
	}

	protected ILLKToken skipSpaces(ILLKToken special, ILLKToken t) {
		for (; special != null && special != t; special = special.getNext()) {
			if (special.getType() != SPACES)
				break;
		}
		return special;
	}

	public void error(LLKParseException e) {
		getMain().error(null, e);
	}

	public void error(String msg, Exception e) {
		getMain().error(msg, e);
	}

	public void error(String msg, ILLKToken t) {
		getMain().error(msg, null, t.getOffset());
	}

	////////////////////////////////////////////////////////////////////////
}
