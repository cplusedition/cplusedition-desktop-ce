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

public class CSSFormatOptions {

	public int lineWidth = 120;
	public int tabWidth = 8;
	public String tabString = "\t";
	public String lineBreak = "\n";
	public boolean isCompact = true;
	public boolean breakOpenBrace = false;

	public CSSFormatOptions() {
	}

	public CSSFormatOptions(boolean compact) {
		this.isCompact = compact;
	}

	public CSSFormatOptions(
		int linewidth, int tabwidth, String tab, String linebreak, boolean compact, boolean breakopenbrace) {
		this.lineWidth = linewidth;
		this.tabWidth = tabwidth;
		this.tabString = tab;
		this.lineBreak = linebreak;
		this.isCompact = compact;
		this.breakOpenBrace = breakopenbrace;
	}

	public boolean isCompact() {
		return isCompact;
	}

	public FormatBuilder createBuffer() {
		return new FormatBuilder(
			lineWidth,
			tabWidth,
			"",
			tabString,
			lineBreak,
			CSSOnelinerFormatter.getSingleton(),
			CSSSpaceUtil.getSingleton());
	}
}
