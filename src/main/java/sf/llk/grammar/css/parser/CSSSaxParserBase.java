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
import sf.llk.grammar.css.ICSSSaxHandler;
import sf.llk.share.support.ILLKParser;
import sf.llk.share.support.ILLKToken;
import sf.llk.share.support.LLKParseException;
import sf.llk.share.support.LLKParserBase;

public abstract class CSSSaxParserBase extends LLKParserBase implements ILLKParser, ILLKCSSParser {

    ////////////////////////////////////////////////////////////////////////

    public static final int ERROR = 1;
    protected ICSSSaxHandler handler;

////////////////////////////////////////////////////////////////////////

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    protected void llkOpenNode(LLKNode node) throws LLKParseException {
        node.setFirstToken(LT(1));
    }

    protected void llkCloseNode(LLKNode node, boolean create) throws LLKParseException {
        if (create) {
            if (LT1() == node.getFirstToken()) {
                node.setFirstToken(null);
                node.setLastToken(null);
            } else {
                node.setLastToken(LT0());
            }
        }
    }

    protected ILLKToken skipTo(int[] types) {
        DONE:
        for (int la1; (la1 = LA1()) != _EOF_; llkConsume()) {
            for (int type : types) {
                if (la1 == type) {
                    break DONE;
                }
            }
        }
        return LT0();
    }

    protected ILLKToken skip(int level) {
        for (int la1; (la1 = LA1()) != _EOF_; llkConsume()) {
            if (la1 == SEMICOLON && level == 0) {
                llkConsume();
                break;
            }
            if (la1 == LBRACE) {
                ++level;
            } else if (la1 == RBRACE) {
                --level;
                if (level == 0) {
                    llkConsume();
                    break;
                }
            }
        }
        return LT0();
    }

    protected void warn(MsgId id, Throwable e, ILLKToken first, ILLKToken last) {
        llkMain.warn(
            id.getMessage() + ": first@" + first.getLocationString() + ", last@" + last.getLocationString(),
            e,
            first.getOffset());
    }

    ////////////////////////////////////////////////////////////////////////

    public String getLineno() {
        return "@" + (llkInput.getLocator().getLinear(LT0().getOffset()));
    }

    ////////////////////////////////////////////////////////////////////////

}
