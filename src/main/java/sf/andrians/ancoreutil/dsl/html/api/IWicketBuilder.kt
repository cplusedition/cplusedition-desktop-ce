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

interface IWicketBuilder : IXHtmlBuilder<IElement?> {
    fun wicket(name: Any?, vararg children: Any): IWicketElement?

    fun wicketPanel(vararg children: Any): IWicketElement?
    fun component(wicketid: String?, tag: IElement?): IWicketComponent?
    fun component(wicketid: Any?, tag: IElement?): IWicketComponent?

    /**
     * Create IWicketComponent using component(id, tag, children(...)) syntax.
     * This is more efficient.
     */
    fun component(wicketid: String?, tagname: Any?, vararg children: Any): IWicketComponent?

    fun component(wicketid: Any?, tagname: Any?, vararg children: Any): IWicketComponent?

    /**
     * Create IWicketComponent using component(id).tag(...) syntax.
     * This is less efficient, but has more compact syntax and allow using dsl methods instead of textual tag name.
     * @param wicketid Auto-generated if null.
     * @return A custom IXHtmlDocument that generate IWicketComponent instead of IElement.
     */
    fun component(wicketid: Any?): IXHtmlBuilder<IWicketComponent?>? // public abstract IRootMarkup serialize(IElement e);
}
