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
package sf.andrians.ancoreutil.dsl.html.impl

import sf.andrians.ancoreutil.dsl.html.api.IAttr
import sf.andrians.ancoreutil.dsl.html.api.IElement

open class Html5Builder : Html5BuilderBase<IElement>() {

    //////////////////////////////////////////////////////////////////////

    override fun empty(name: String, vararg attrs: IAttr): IElement {
        return EmptyElement(name, *attrs)
    }

    override fun elm(name: String, vararg children: Any): IElement {
        return Element(name, *children)
    }

    //////////////////////////////////////////////////////////////////////

    fun serialize(indent: String = "", tab: String = "    ", noemptytag: Boolean = true, e: IElement): String {
        return Html5Serializer.serialize(indent, tab, noemptytag, e)
    }

    //////////////////////////////////////////////////////////////////////
}
