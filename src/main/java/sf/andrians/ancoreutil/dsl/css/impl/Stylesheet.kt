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

import sf.andrians.ancoreutil.dsl.css.api.*
import sf.andrians.ancoreutil.dsl.css.api.support.IStylesheetChild
import java.util.*

class Stylesheet : IStylesheet {
    private val children: MutableCollection<IStylesheetChild> = LinkedList()
    override val namespaces: MutableCollection<INamespace> = LinkedList()
    override val imports: MutableCollection<IImport> = LinkedList()
    override val medias: MutableCollection<IMedia> = LinkedList()
    override val pages: MutableCollection<IPage> = LinkedList()
    override val fontFaces: MutableCollection<IFontface> = LinkedList()
    override val rulesets: MutableCollection<IRuleset> = LinkedList()

    ////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////
    override fun add(vararg imports: IImport): IStylesheet {
        for (i in imports) {
            add(i)
        }
        return this
    }

    override fun add(vararg namespaces: INamespace): IStylesheet {
        for (n in namespaces) {
            add(n)
        }
        return this
    }

    override fun add(vararg medias: IMedia): IStylesheet {
        for (m in medias) {
            add(m)
        }
        return this
    }

    override fun add(vararg fontfaces: IFontface): IStylesheet? {
        for (f in fontfaces) {
            add(f)
        }
        return null
    }

    override fun add(vararg pages: IPage): IStylesheet {
        for (p in pages) {
            add(p)
        }
        return this
    }

    override fun add(vararg rulesets: IRuleset): IStylesheet {
        for (r in rulesets) {
            add(r)
        }
        return this
    }

    override fun add(vararg children: IRulesets): IStylesheet {
        for (child in children) {
            child.addTo(this)
        }
        return this
    }

    override fun add(vararg children: IStylesheetChild): IStylesheet {
        for (child in children) {
            child.addTo(this)
        }
        return this
    }

    ////////////////////////////////////////////////////////////////////////
    override fun add(iport: IImport): IStylesheet {
        children.add(iport)
        imports.add(iport)
        return this
    }

    override fun add(ns: INamespace): IStylesheet {
        children.add(ns)
        namespaces.add(ns)
        return this
    }

    override fun add(m: IMedia): IStylesheet {
        children.add(m)
        medias.add(m)
        return this
    }

    override fun add(f: IFontface): IStylesheet {
        children.add(f)
        fontFaces.add(f)
        return this
    }

    override fun add(p: IPage): IStylesheet {
        children.add(p)
        pages.add(p)
        return this
    }

    override fun add(r: IRuleset): IStylesheet {
        children.add(r)
        rulesets.add(r)
        return this
    }

    override fun add(rset: IRulesets): IStylesheet {
        for (child in rset) {
            child.addTo(this)
        }
        return this
    }

    override fun add(raw: IRaw): IStylesheet {
        children.add(raw)
        return this
    }

    override fun add(child: IStylesheetChild): IStylesheet {
        child.addTo(this)
        return this
    }

    ////////////////////////////////////////////////////////////////////////
    override fun iterator(): MutableIterator<IStylesheetChild> {
        return children.iterator()
    }

    override fun <T> accept(visitor: ICSSVisitor<T>, data: T) {
        visitor.visit(this, data)
    } ////////////////////////////////////////////////////////////////////////

    companion object {
        ////////////////////////////////////////////////////////////////////////
        private const val serialVersionUID = 1L
    }
}
