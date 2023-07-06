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

import java.util.Collection;

/**
 * Interface to access attribute table.
 */
public interface IAttrTable {

    ////////////////////////////////////////////////////////////////////////

    IAttrRegistry getAttrRegistry();

    IAttrTable getParentTable();

    void setParentTable(IAttrTable parent);

    /**
     * Get attribute with given name.  If attribute is not defined
     * locally, get from the default table in the graph element
     * factory.
     */
    Object getAttr(String name);

    String getAttrString(String name);

    String getAttrAsString(String name);

    boolean getAttrBool(String name);

    int getAttrInt(String name);

    long getAttrLong(String name);

    float getAttrFloat(String name);

    double getAttrDouble(String name);

    Object getAttr(String name, Object def);

    String getAttrString(String name, String def);

    boolean getAttrBool(String name, boolean def);

    int getAttrInt(String name, int def);

    long getAttrLong(String name, long def);

    float getAttrFloat(String name, float def);

    double getAttrDouble(String name, double def);

    /**
     * Set local attribute.
     */
    Object setAttr(String name, Object value) throws UnsupportedOperationException;

    Object setAttr(String name, boolean value) throws UnsupportedOperationException;

    Object setAttr(String name, int value) throws UnsupportedOperationException;

    Object setAttr(String name, long value) throws UnsupportedOperationException;

    Object setAttr(String name, float value) throws UnsupportedOperationException;

    Object setAttr(String name, double value) throws UnsupportedOperationException;

    Object setAttrFromString(String name, String value) throws UnsupportedOperationException;

    /**
     * Remove a local attribute. @return attribute removed.
     */
    Object removeAttr(String name);

    /**
     * Remove all unregistered local attributes.
     */
    void removeUnregisteredAttrs();

    /**
     * Remove all local attributes.
     */
    void clearAttrs();

    /**
     * Check if given attribute is defined locally. @return true if exists locally (include value==null).
     */
    boolean hasAttr(String name);

    /**
     * All local attribute names (include value==null). Don't modify.
     */
    Collection<String> attrKeys();

    ////////////////////////////////////////////////////////////////////////
}
