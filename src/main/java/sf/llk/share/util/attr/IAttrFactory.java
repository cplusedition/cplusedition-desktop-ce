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
 * Attribute factory interface provide methods for attribute
 * creation, type conversion and user interface.
 * <p>
 * . The attribute registry maintains a name->AttrFactory table.
 * . The attribute type methods are used to serialize and
 * de-serialize attributes.
 */
public interface IAttrFactory {

    ////////////////////////////////////////////////////////////////////////

    /**
     * Create an appropriate object from the String representation of the attribute.
     */
    Object createObject(String value, Object def);

    /**
     * Check that given object is valid value for this factory.
     */
    boolean isValid(Object object);

    /**
     * Check that given object is valid value for this factory or throws an AssertionError.
     */
    void assertValid(Object object);

    /**
     * @return The simple name of the valid type.
     */
    Class<?> getObjectClass();

    /**
     * The String representation of an attribute value.
     */
    String toString(Object object);

    /**
     * The String representation of attribute type itself.
     */
    @Override
    String toString();

    /**
     * Prompt user and present a user interface to obtain an
     * attribute value from user.
     */
    Object promptUser(String prompt);

    ////////////////////////////////////////////////////////////////////////
}
