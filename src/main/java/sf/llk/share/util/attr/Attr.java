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
/*
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.llk.share.util.attr;

public class Attr {

    //////////////////////////////////////////////////////////////////////

    protected final String name;
    protected final IAttrFactory factory;
    protected final Object def;

    //////////////////////////////////////////////////////////////////////

    public Attr(String name, IAttrFactory factory, Object def) {
        this.name = name;
        this.factory = factory;
        this.def = def;
    }

    public Attr(String name, String def) {
        this.name = name;
        this.factory = StringAttrFactory.getDefault();
        this.def = def;
    }

    public Attr(String name, boolean def) {
        this.name = name;
        this.factory = BooleanAttrFactory.getDefault();
        this.def = def;
    }

    public Attr(String name, int def) {
        this.name = name;
        this.factory = IntAttrFactory.getDefault();
        this.def = def;
    }

    public Attr(String name, long def) {
        this.name = name;
        this.factory = LongAttrFactory.getDefault();
        this.def = def;
    }

    public Attr(String name, float def) {
        this.name = name;
        this.factory = FloatAttrFactory.getDefault();
        this.def = def;
    }

    public Attr(String name, double def) {
        this.name = name;
        this.factory = DoubleAttrFactory.getDefault();
        this.def = def;
    }

    //////////////////////////////////////////////////////////////////////

    public String getName() {
        return name;
    }

    public IAttrFactory getFactory() {
        return factory;
    }

    public Object getDefaultValue() {
        return def;
    }

    //////////////////////////////////////////////////////////////////////
}
