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
package sf.llk.share.formatter;

import sf.llk.share.support.IIntList;
import sf.llk.share.support.ILLKConstants;
import sf.llk.share.support.ILLKLexer;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.support.LLKParseError;
import sf.llk.share.util.ISpaceUtil;

public abstract class AbstractLexerOneliner implements IOnelinerFormatter {

	//////////////////////////////////////////////////////////////////////

	protected final ISpaceUtil spaceUtil;
	protected final int spaces;
	protected final int newline;
	protected final int comment1;
	protected final int comment;
	protected final int doccomment;

	public AbstractLexerOneliner(
		ISpaceUtil spaceutil, int spaces, int newline, int comment1, int comment, int doccomment) {
		this.spaceUtil = spaceutil;
		this.spaces = spaces;
		this.newline = newline;
		this.comment1 = comment1;
		this.comment = comment;
		this.doccomment = doccomment;
	}

	protected abstract ILLKLexer createLexer(char[] source);

	public CharSequence toOneliner(
		CharSequence s, int start, int end, boolean wasspace, int columns, IIntList softbreaks) {
		CharSequence word = OnelinerUtil.simpleWord(s, start, end, wasspace, softbreaks, spaceUtil);
		if (word != null)
			return word;
		char[] source = OnelinerUtil.getSource(s, start, end);
		StringBuilder ret = new StringBuilder(end - start);
		try {
			ILLKLexer lexer = createLexer(source);
			ILLKToken t = lexer.llkNextToken();
			ILLKToken next = null;
			int type = ILLKConstants.TOKEN_TYPE_EOF;
			while (true) {
				type = t.getType();
				next = (type != ILLKConstants.TOKEN_TYPE_EOF) ? lexer.llkNextToken() : null;
				for (ILLKToken st = t.getSpecial(); st != null; st = st.getNext()) {
					int stype = st.getType();
					if (stype == spaces) {
						if (isComment(st.getNext()) && !spaceUtil.isWhitespaces(ret)) {
							spaceUtil.rtrimSpaces(ret);
							ret.append(st.getText());
							if (!spaceUtil.endsWithSpaces(ret))
								ret.append(' ');
							wasspace = true;
						} else if (!wasspace) {
							int offset = st.getOffset();
							if (softbreaks != null
								&& OnelinerUtil.isSoftBreak(offset, softbreaks)) {
								st = skipSoftBreak(st);
								continue;
							}
							ret.append(' ');
							wasspace = true;
						}
					} else if (stype == newline) {
						if (wasspace)
							continue;
						int offset = st.getOffset();
						if (softbreaks != null && OnelinerUtil.isSoftBreak(offset, softbreaks)) {
							st = skipSoftBreak(st);
							continue;
						}
						ret.append(' ');
						wasspace = true;
					} else if (stype == comment1) {
						ILLKToken stt = st.getNext();
						if (stt != null) {
							if (stt.getType() != newline)
								throw new AssertionError(
									"Single line comment must be followed by a NEWLINE token: token="
										+ stt);
							if (stt.getNext() != null
								|| next != null && type != ILLKConstants.TOKEN_TYPE_EOF)
								return null;
						}
						if (!wasspace)
							ret.append(' ');
						ret.append(st.getText());
						wasspace = false;
					} else if (stype == comment || stype == doccomment) {
						if (spaceUtil.isSpaces(ret) && (followedByLineBreak(st)))
							return null;
						if (spaceUtil.hasLineBreak(st.getText()))
							return null;
						if (!wasspace)
							ret.append(' ');
						ret.append(st.getText());
						ret.append(' ');
						wasspace = true;
					} else {
						ret.append(st.getText());
						wasspace = false;
					}
				}
				if (type == ILLKConstants.TOKEN_TYPE_EOF)
					break;
				ret.append(t.getText());
				if (ret.length() > columns)
					return null;
				wasspace = false;
				t = next;
			}
		} catch (Exception e) {
			throw new LLKParseError("Source = " + s, e);
		}
		return ret;
	}

	//////////////////////////////////////////////////////////////////////

	private ILLKToken skipSoftBreak(ILLKToken st) {
		for (ILLKToken n = st.getNext(); n != null; st = n, n = n.getNext()) {
			if (n.getType() != spaces)
				break;
		}
		return st;
	}

	private boolean followedByLineBreak(ILLKToken s) {
		for (ILLKToken ss = s.getNext(); ss != null; ss = ss.getNext()) {
			int type = ss.getType();
			if (type == spaces)
				continue;
            return type == newline;
        }
		return false;
	}

	public boolean isComment(ILLKToken s) {
		if (s == null)
			return false;
		int tt = s.getType();
		return (tt == comment1 || tt == comment || tt == doccomment);
	}

	//////////////////////////////////////////////////////////////////////
}
