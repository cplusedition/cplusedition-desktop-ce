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
package sf.andrians.cplusedition.war.support

import sf.andrians.ancoreutil.dsl.html.api.IElement
import sf.andrians.ancoreutil.dsl.html.impl.Html5Builder
import sf.andrians.ancoreutil.dsl.html.impl.Html5Serializer
import sf.andrians.ancoreutil.util.io.StringPrintWriter
import sf.andrians.cplusedition.support.handler.ICpluseditionContext
import java.io.PrintWriter

class SettingsTemplate : Html5Builder() {
    fun serialze(context: ICpluseditionContext?, js: String?): String {
        val ret = StringPrintWriter()
        serialize(ret, context, js)
        return ret.toString()
    }

    fun serialize(ret: PrintWriter, context: ICpluseditionContext?, js: String?) {
        build(context, js).accept(Html5Serializer<PrintWriter>("    ").indent("").noXmlEndTag(true), ret)
    }

    fun build(context: ICpluseditionContext?, js: String?): IElement {
        return top(
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
