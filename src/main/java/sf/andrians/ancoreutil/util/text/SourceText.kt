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
package sf.andrians.ancoreutil.util.text

import java.util.*

open class SourceText : ISourceText {
    private var text_: CharSequence? = null
    override val text: CharSequence?
        get() = text_
    private var offset_ = 0
    override var offset: Int
        get() = offset_
        protected set(value) {
            offset_ = value
        }
    private var length_ = 0
    override var length: Int
        get() = length_
        protected set(value) {
            length_ = value
        }

    constructor(text: CharSequence?, start: Int, len: Int) {
        text_ = text
        offset_ = start
        length_ = len
    }

    constructor(text: CharSequence, start: Int) {
        text_ = text
        offset_ = start
        length_ = text.length
    }

    constructor(sourcetext: ISourceText) {
        text_ = sourcetext.text
        offset_ = sourcetext.offset
        length_ = sourcetext.length
    }

    override val endOffset: Int
        get() = offset + length

    override fun toString(): String {
        return text.toString()
    }

    override fun hashCode(): Int {
        return offset * 31 + text.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is SourceText) return false
        val aa = other
        return offset == aa.offset && length == aa.length && (text == null && aa.text == null || text != null && text == aa.text)
    }

    protected open fun setText(text: CharSequence?) {
        this.text_ = text
    }

    companion object {
        var ascendingComparator: Comparator<SourceText>? = null
            get() {
                if (field == null) {
                    field = Comparator { a, b ->
                        val oa = a.offset
                        val ob = b.offset
                        if (oa > ob) 1 else if (oa < ob) -1 else 0
                    }
                }
                return field
            }
            private set
    }
}