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

import sf.llk.share.util.AbstractSpaceUtil;

public class CSSSpaceUtil extends AbstractSpaceUtil {

	private static CSSSpaceUtil singleton;

	public static CSSSpaceUtil getSingleton() {
		if (singleton == null)
			singleton = new CSSSpaceUtil();
		return singleton;
	}

	//////////////////////////////////////////////////////////////////////

	public boolean isSpace(char c) {
		return (c == ' ' || c == '\t' || c == '\f');
	}

	public boolean isLineBreak(char c) {
		return c == '\n' || c == '\r';
	}

	public boolean isWhitespace(char c) {
		return Character.isWhitespace(c);
	}

	public int skipLineBreak(CharSequence s, int start, int end) {
		if (start < end) {
			char c = s.charAt(start);
			if (c == '\n')
				return start + 1;
			if (c == '\r') {
				if (start + 1 < end && s.charAt(start + 1) == '\n')
					return start + 2;
				return start + 1;
		}}
		return start;
	}

	public int rskipLineBreak(CharSequence s, int start, int end) {
		if (end > start) {
			char c = s.charAt(end - 1);
			if (c == '\n') {
				--end;
				if (end - 1 >= start && s.charAt(end - 1) == '\r')
					--end;
			} else if (c == '\r') {
				--end;
		}}
		return end;
	}

	//////////////////////////////////////////////////////////////////////
}
