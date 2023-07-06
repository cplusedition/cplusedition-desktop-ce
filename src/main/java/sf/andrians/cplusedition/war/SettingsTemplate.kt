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
package sf.andrians.cplusedition.war

import com.cplusedition.bot.core.StringPrintWriter
import com.cplusedition.bot.dsl.html.api.IElement
import com.cplusedition.bot.dsl.html.impl.Html5Builder
import com.cplusedition.bot.dsl.html.impl.Html5Serializer
import sf.andrians.cplusedition.support.handler.ICpluseditionContext
import java.io.PrintWriter

class SettingsTemplate : Html5Builder() {
    fun serialze(context: ICpluseditionContext?, js: String?): String {
        val ret = StringPrintWriter()
        serialize(ret, js)
        return ret.toString()
    }

    fun serialize(ret: PrintWriter, js: String?) {
        build(js).accept(Html5Serializer<PrintWriter>("    ").indent("").noXmlEndTag(true), ret)
    }

    fun build(js: String?): IElement {
        return fragment(
            doctype(),
            html(
                head(
                    contenttype("text/html; charset=UTF-8"),
                    meta(
                        name("viewport"),
                        super.content("width=device-width, height=device-height, initial-scale=1.0, user-scalable=no")),
                    stylesheet("assets/css/annocloud-host.css"),
                    javascript("assets/js/jquery.js"),
                    javascript("assets/js/andrians.js"),
                    javascript("assets/config/config.js"),
                    javascript(js!!))
            ),
            body())
    }
}
