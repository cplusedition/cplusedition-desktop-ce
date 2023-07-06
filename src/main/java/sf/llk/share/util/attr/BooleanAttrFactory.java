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

/**
 * Boolean attribute factory.
 */
public class BooleanAttrFactory extends AbstractAttrFactory {

    ////////////////////////////////////////////////////////////////////////

    private static BooleanAttrFactory instance = null;

    ////////////////////////////////////////////////////////////////////////

    private BooleanAttrFactory() {
    }

    public static BooleanAttrFactory getDefault() {
        if (instance == null) {
            instance = new BooleanAttrFactory();
        }
        return instance;
    }

    public static boolean create(String stringvalue) {
        if (instance == null) {
            instance = new BooleanAttrFactory();
        }
        return ((Boolean) instance.createObject(stringvalue, null));
    }

    ////////////////////////////////////////////////////////////////////////

    @Override
    public Object createObject(String stringvalue, Object def) {
        if (stringvalue == null) {
            return Boolean.FALSE;
        }
        try {
            return Boolean.valueOf(stringvalue);
        } catch (Exception e) {
            return def;
        }
    }

    @Override
    public Class<?> getObjectClass() {
        return Boolean.class;
    }

    ////////////////////////////////////////////////////////////////////////
}
