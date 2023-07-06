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
package com.cplusedition.bot.dsl.html.api

interface IHtml5Builder<E : IElement> : IXHtmlBuilder<E> {
    fun source(vararg attrs: IAttr): E
    fun command(vararg attrs: IAttr): E
    fun mark(vararg attrs: IAttr): E
    fun track(vararg attrs: IAttr): E
    fun wbr(vararg attrs: IAttr): E

    fun article(vararg children: Any): E
    fun aside(vararg children: Any): E
    fun audio(vararg children: Any): E
    fun bdi(vararg children: Any): E
    fun canvas(vararg children: Any): E
    fun datalist(vararg children: Any): E
    fun details(vararg children: Any): E
    fun figcaption(vararg children: Any): E
    fun figure(vararg children: Any): E
    fun footer(vararg children: Any): E
    fun header(vararg children: Any): E
    fun hgroup(vararg children: Any): E
    fun keygen(vararg children: Any): E
    fun meter(vararg children: Any): E
    fun nav(vararg children: Any): E
    fun output(vararg children: Any): E
    fun progress(vararg children: Any): E
    fun rp(vararg children: Any): E
    fun rt(vararg children: Any): E
    fun ruby(vararg children: Any): E
    fun section(vararg children: Any): E
    fun summary(vararg children: Any): E
    fun time(vararg children: Any): E
    fun video(vararg children: Any): E
    fun template(vararg children: Any): E
}
