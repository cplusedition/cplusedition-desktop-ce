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

import sf.llk.share.support.*;

public class HtmlTreeParser extends LLKTreeParserBase implements ILLKHtmlTreeParser {

	public static final boolean LLK_OPTION_VERIFY = false;
	public static final int LLK_INPUT_VOCAB_SIZE = 70;

	public HtmlTreeParser(ILLKTreeParserInput input) {
		llkInput = input;
		llkMain = input.getMain();
	}

	////////////////////////////////////////////////////////////

	public final void llkPush() {
		llkStack.push(llkParent);
		llkParent = LT1;
		LT1 = LT1.getFirst();
	}

	public final void llkPop() {
		LT1 = llkParent.getNext();
		llkParent = llkStack.pop();
	}

	public void document() throws LLKParseException {
		if (LA1() == ASTtext) {
			LT1 = LT1.getNext();
		}
		_loop1: while (true) {
			if (llkGetBit(LA1(), LLKTokenSet0.bitset)) {
				element();
			} else {
				break _loop1;
			}
		}
	}

	public void element() throws LLKParseException {
		if (LA1() == ASTstartTag) {
			llkPush();
			startTag();
			llkPop();
		} else if (llkGetBit(LA1(), LLKTokenSet1.bitset)) {
			switch (LA1()) {
				case ASTextraEndTag:
					LT1 = LT1.getNext();
					break;
				case ASTcomment:
					LT1 = LT1.getNext();
					break;
				case ASTpi:
					LT1 = LT1.getNext();
					break;
				case ASTasp:
					LT1 = LT1.getNext();
					break;
				case ASTjste:
					LT1 = LT1.getNext();
					break;
				case ASTcdata:
					LT1 = LT1.getNext();
					break;
				case ASTdeclaration:
					LT1 = LT1.getNext();
					break;
				case ASTdoctype:
					llkPush();
					doctype();
					llkPop();
					break;
				default:
					throw llkParseException("Unexpected token", LT1);
			}
			if (LA1() == ASTtext) {
				LT1 = LT1.getNext();
			}
		} else {
			throw llkParseException("Unexpected token", LT1);
		}
	}

	public void startTag() throws LLKParseException {
		switch (LA1()) {
			case ASTscript:
				LT1 = LT1.getNext();
				break;
			case ASTstyle:
				LT1 = LT1.getNext();
				break;
			case ASTtext:
				LT1 = LT1.getNext();
				_loop1: while (true) {
					if (llkGetBit(LA1(), LLKTokenSet0.bitset)) {
						element();
					} else {
						break _loop1;
					}
				}
				break;
			case ASTstartTag:
			case ASTextraEndTag:
			case ASTcomment:
			case ASTpi:
			case ASTasp:
			case ASTjste:
			case ASTcdata:
			case ASTdeclaration:
			case ASTdoctype:
				_loop2: for (boolean _cnt2 = false;; _cnt2 = true) {
					if (llkGetBit(LA1(), LLKTokenSet0.bitset)) {
						element();
					} else {
						if (!_cnt2) {
							throw llkParseException("()+ expected at least one occuence", LT1);
						}
						break _loop2;
					}
				}
				break;
			default:
		}
		if (LA1() == ASTendTag) {
			LT1 = LT1.getNext();
			if (LA1() == ASTtext) {
				LT1 = LT1.getNext();
			}
		}
	}

	public void attributes() throws LLKParseException {
		_loop1: for (boolean _cnt1 = false;; _cnt1 = true) {
			if (LA1() == ASTattribute) {
				LT1 = LT1.getNext();
			} else {
				if (!_cnt1) {
					throw llkParseException("()+ expected at least one occuence", LT1);
				}
				break _loop1;
			}
		}
	}

	public void doctype() throws LLKParseException {
		if (LA1() == ASTdtdId) {
			LT1 = LT1.getNext();
		}
	}

	////////////////////////////////////////////////////////////

	static class LLKTokenSet {
		public boolean inverted;
		public int start;
		public int end;
		public int[] bitset;
		public LLKTokenSet(boolean inverted, int start, int end, int[] bitset) {
			this.inverted=inverted;
			this.start=start;
			this.end=end;
			this.bitset=bitset;
		}
	}
	static final LLKTokenSet LLKTokenSet0 = new LLKTokenSet(
		false,
		0,
		3,
		new int[] {
			0x00000000, 0x5fa00000, 0x00000010, 
		}
	);
	static final LLKTokenSet LLKTokenSet1 = new LLKTokenSet(
		false,
		0,
		3,
		new int[] {
			0x00000000, 0x5f800000, 0x00000010, 
		}
	);

	public final String llkGetTokenName(int type) {
		return LLKTOKENS.nameOf(type);
	}
	
	protected final boolean llkGetBitInverted(int n, int[] bset) {
		int mask = (n & ILLKConstants.MODMASK);
		return n >= 0
			&& n < LLK_INPUT_VOCAB_SIZE
			&& ((n >>= ILLKConstants.LOGBITS) >= bset.length || (bset[n] & (1 << mask)) == 0);
	}
	
	////////////////////////////////////////////////////////////

	public void llkReset() {
		llkInput.reset();
		for (ILLKLifeCycleListener l: llkLifeCycleListeners) {
			l.reset();
		}
	}

	////////////////////////////////////////////////////////////
}
