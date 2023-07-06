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

public abstract class AbstractAttrFactory implements IAttrFactory {

    @Override
    public boolean isValid(Object a) {
        return a == null || getObjectClass().isInstance(a);
    }

    @Override
    public void assertValid(Object a) {
        if (!isValid(a)) {
            throw new AssertionError(
                "Expected type: " + getObjectClass().getName() + ", actual: " + a.getClass().getName());
        }
    }

    @Override
    public String toString(Object a) {
        assertValid(a);
        return a == null ? null : a.toString();
    }

    @Override
    public String toString() {
        return getClass().getName();
    }

    @Override
    public Object promptUser(String prompt) {
        return null;
    }
}
