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
package sf.andrians.ancoreutil.dsl.css.impl

import sf.andrians.ancoreutil.dsl.css.api.ICSS
import sf.andrians.ancoreutil.dsl.html.api.IAttribute
import sf.andrians.ancoreutil.dsl.html.api.IAttributes
import sf.andrians.ancoreutil.dsl.html.api.IElement

/**
 * enum can be used for CSS classes, but that cannot be extended.
 * This is a convenient substitution.
 */
class CSS : ICSS {
    protected val name: String
    protected val ref: String?

    constructor(name: String) {
        this.name = name
        ref = ".$name"
    }

    constructor(name: Any) {
        this.name = name.toString()
        ref = "." + this.name
    }

    ////////////////////////////////////////////////////////////////////////
    override fun name(): String {
        return name
    }

    override fun ref(): String? {
        return ref
    }

    override fun att(): IAttribute? {
        return this
    }

    ////////////////////////////////////////////////////////////////////////
    override fun addTo(attributes: IAttributes) {
        attributes.a(this)
    }

    override fun addTo(e: IElement) {
        e.a(this)
    }

    override fun aname(): String {
        return "class"
    }

    override fun avalue(): String? {
        return name
    }

    override fun toString(): String {
        return name
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
