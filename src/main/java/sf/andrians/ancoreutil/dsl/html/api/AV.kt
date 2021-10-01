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
package sf.andrians.ancoreutil.dsl.html.api

class AV protected constructor(private val name: String, private val value: String) : IAttribute {
    override fun aname(): String {
        return name
    }

    override fun avalue(): String {
        return value
    }

    override fun addTo(e: IElement) {
        e.a(this)
    }

    override fun addTo(attributes: IAttributes) {
        attributes.a(this)
    }

    override fun toString(): String {
        return avalue()
    }

    //////////////////////////////////////////////////////////////////////
    interface Align {
        companion object {
            const val NAME = "align"
            val Left: IAttribute = AV(NAME, "left")
            val Center: IAttribute = AV(NAME, "center")
            val Right: IAttribute = AV(NAME, "right")
        }
    }

    //////////////////////////////////////////////////////////////////////
    interface Button {
        interface Type {
            companion object {
                const val NAME = "type"
                val Button: IAttribute = AV(NAME, "button")
                val Submit: IAttribute = AV(NAME, "submit")
                val Reset: IAttribute = AV(NAME, "reset")
            }
        }
    }

    //////////////////////////////////////////////////////////////////////
    interface Cell {
        interface Align {
            companion object {
                const val NAME = "align"
                val Top: IAttribute = AV(NAME, "top")
                val Middle: IAttribute = AV(NAME, "middle")
                val Bottom: IAttribute = AV(NAME, "bottom")
                val Baseline: IAttribute = AV(NAME, "baseline")
            }
        }

        interface VAlign {
            companion object {
                const val NAME = "valign"
                val Left: IAttribute = AV(NAME, "left")
                val Center: IAttribute = AV(NAME, "center")
                val Right: IAttribute = AV(NAME, "right")
                val Justify: IAttribute = AV(NAME, "justify")
                val Char: IAttribute = AV(NAME, "char")
            }
        }
    }

    //////////////////////////////////////////////////////////////////////
    interface Event {
        companion object {
            const val NAME = "event"
            val Onclick: IAttribute = AV(NAME, "onclick")
            val Ondblclick: IAttribute = AV(NAME, "ondblclick")
            val Onmousedown: IAttribute = AV(NAME, "onmousedown")
            val Onmouseup: IAttribute = AV(NAME, "onmouseup")
            val Onmouseover: IAttribute = AV(NAME, "onmouseover")
            val Onmousemove: IAttribute = AV(NAME, "onmousemove")
            val Onmouseout: IAttribute = AV(NAME, "onmouseout")
            val Onkeypress: IAttribute = AV(NAME, "onkeypress")
            val Onkeydown: IAttribute = AV(NAME, "onkeydown")
            val Onkeyup: IAttribute = AV(NAME, "onkeyup")
        }
    }

    //////////////////////////////////////////////////////////////////////
    interface HttpEquiv {
        companion object {
            const val NAME = "http-equiv"
            val Refresh: IAttribute = AV(NAME, "refresh")
            val Defaultstyle: IAttribute = AV(NAME, "default-style")
            val Contenttype: IAttribute = AV(NAME, "content-type")
        }
    }

    //////////////////////////////////////////////////////////////////////
    interface Input {
        interface Type {
            companion object {
                const val NAME = "type"
                val Text: IAttribute = AV(NAME, "text")
                val Password: IAttribute = AV(NAME, "password")
                val Checkbox: IAttribute = AV(NAME, "checkbox")
                val Radio: IAttribute = AV(NAME, "radio")
                val Submit: IAttribute = AV(NAME, "submit")
                val Reset: IAttribute = AV(NAME, "reset")
                val File: IAttribute = AV(NAME, "file")
                val Hidden: IAttribute = AV(NAME, "hidden")
                val Image: IAttribute = AV(NAME, "image")
                val Button: IAttribute = AV(NAME, "button")

                val Datetime: IAttribute = AV(NAME, "datetime")
                val Datetimelocal: IAttribute = AV(NAME, "datetime-local")
                val Date: IAttribute = AV(NAME, "date")
                val Month: IAttribute = AV(NAME, "month")
                val Time: IAttribute = AV(NAME, "time")
                val Week: IAttribute = AV(NAME, "week")
                val Number: IAttribute = AV(NAME, "number")
                val Range: IAttribute = AV(NAME, "range")
                val Email: IAttribute = AV(NAME, "email")
                val Url: IAttribute = AV(NAME, "url")
                val Search: IAttribute = AV(NAME, "search")
                val Tel: IAttribute = AV(NAME, "tel")
                val Color: IAttribute = AV(NAME, "color")
            }
        }
    }

    //////////////////////////////////////////////////////////////////////
    interface Command {
        interface Type {
            companion object {
                const val NAME = "type"
                val Command: IAttribute = AV(NAME, "command")
                val Radio: IAttribute = AV(NAME, "radio")
                val Checkbox: IAttribute = AV(NAME, "checkbox")
            }
        }
    } //////////////////////////////////////////////////////////////////////

    companion object {
        private const val serialVersionUID = 1L
    }

}
