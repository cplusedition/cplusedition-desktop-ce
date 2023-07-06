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

public interface ILLKHtmlLexer {
	int _EOF_=0;
	int _IGNORE_=1;
	int _INVALID_=2;
	int _NULL_=3;
	int LSCRIPT=4;
	int LSTYLE=5;
	int ENDEMPTY=6;
	int LT=7;
	int GT=8;
	int ASSIGN=9;
	int ENDTAG=10;
	int DTDCOMMENT=11;
	int LCOMMENT=12;
	int RCOMMENT=13;
	int LCDATA=14;
	int RCDATA=15;
	int RCOND=16;
	int RPI=17;
	int RASP=18;
	int RJSTE=19;
	int PUBLIC=20;
	int SYSTEM=21;
	int LCOND=22;
	int LDECL=23;
	int LPI=24;
	int LASP=25;
	int LJSTE=26;
	int DECLARATION=27;
	int COMMENT=28;
	int PI=29;
	int ASP=30;
	int JSTE=31;
	int CDATA=32;
	int COND=33;
	int SCRIPT=34;
	int STYLE=35;
	int TEXT=36;
	int VOID=37;
	int llkNextToken=38;
	int skip=39;
	int endScript=40;
	int endComment=41;
	int SPACES=42;
	int NEWLINE=43;
	int NAME=44;
	int ATTVALUE=45;
	int STRING=46;
	int ATTVALUE_LITERAL=47;
	int SYSTEM_LITERAL=48;
	int PUBID_LITERAL=49;
	int REFERENCE=50;
	int laSpaceNotName=51;
	int LLK_TOKENTYPE_COUNT = 52;
	
	public static class LLKTOKENS {

		////////////////////////////////////////////////////////////

		private static final String[] NAMES = new String[] {
			"_EOF_",
			"_IGNORE_",
			"_INVALID_",
			"_NULL_",
			"LSCRIPT",
			"LSTYLE",
			"ENDEMPTY",
			"LT",
			"GT",
			"ASSIGN",
			"ENDTAG",
			"DTDCOMMENT",
			"LCOMMENT",
			"RCOMMENT",
			"LCDATA",
			"RCDATA",
			"RCOND",
			"RPI",
			"RASP",
			"RJSTE",
			"PUBLIC",
			"SYSTEM",
			"LCOND",
			"LDECL",
			"LPI",
			"LASP",
			"LJSTE",
			"DECLARATION",
			"COMMENT",
			"PI",
			"ASP",
			"JSTE",
			"CDATA",
			"COND",
			"SCRIPT",
			"STYLE",
			"TEXT",
			"VOID",
			"llkNextToken",
			"skip",
			"endScript",
			"endComment",
			"SPACES",
			"NEWLINE",
			"NAME",
			"ATTVALUE",
			"STRING",
			"ATTVALUE_LITERAL",
			"SYSTEM_LITERAL",
			"PUBID_LITERAL",
			"REFERENCE",
			"laSpaceNotName",
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
	int CONTEXT = 0;
	int CONTEXT_NONE = 0;
	int CONTEXT_ASP = 1;
	int CONTEXT_ATTRVALUE = 2;
	int CONTEXT_CDATA = 3;
	int CONTEXT_COMMENT = 4;
	int CONTEXT_COND = 5;
	int CONTEXT_DECLARATION = 6;
	int CONTEXT_DTD = 7;
	int CONTEXT_JSTE = 8;
	int CONTEXT_PI = 9;
	int CONTEXT_PUBID = 10;
	int CONTEXT_SCRIPT = 11;
	int CONTEXT_STYLE = 12;
	int CONTEXT_SYSTEM = 13;
	int CONTEXT_TAG = 14;
	int CONTEXT_TEXT = 15;
}
