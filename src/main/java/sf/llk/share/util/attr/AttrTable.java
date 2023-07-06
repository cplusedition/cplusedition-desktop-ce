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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A base class to access an attribute table.
 * <p>
 * . Attribute table always store attributes in their native format,
 * eg. color as Color object. Clients accessing the attribute table
 * should knows the attribute type it is accessing.
 * . For convenient, attributes can always be get/store through a
 * String representation. attrString() always return the String
 * representation of the attribute object and createAttr() method
 * converts the String to the appropriate object type by consulting
 * the AttrRegistry.
 *
 * @see IAttrTable
 */
public class AttrTable implements IAttrTable {

    private static final String NAME = "AttrTable";
    private static final int CAPACITY = 7;

    private final IAttrRegistry attrRegistry;
    private final Map<String, Object> attrTable;
    protected IAttrTable parentAttrTable;

    public AttrTable() {
        this(null, null);
    }

    public AttrTable(IAttrTable parent) {
        this(parent, null);
    }

    public AttrTable(IAttrRegistry r) {
        this(null, r);
    }

    public AttrTable(IAttrTable parent, IAttrRegistry r) {
        attrTable = new HashMap<>(CAPACITY);
        parentAttrTable = parent;
        attrRegistry = r;
    }

    public AttrTable(IAttrTable parent, IAttrRegistry r, int capacity) {
        attrTable = new HashMap<>(capacity);
        parentAttrTable = parent;
        attrRegistry = r;
    }

    @Override
    public IAttrRegistry getAttrRegistry() {
        if (attrRegistry != null) {
            return attrRegistry;
        }
        if (parentAttrTable != null) {
            return parentAttrTable.getAttrRegistry();
        }
        return null;
    }

    @Override
    public IAttrTable getParentTable() {
        return parentAttrTable;
    }

    @Override
    public void setParentTable(IAttrTable parent) {
        parentAttrTable = parent;
    }

    ////////////////////////////////////////////////////////////////////////

    /**
     * Get attribute. Automatically get the default attribute if
     * local attribute is not defined.
     */
    @Override
    public Object getAttr(String name) {
        Object ret;
        if ((ret = attrTable.get(name)) != null) {
            return ret;
        }
        if (parentAttrTable != null && (ret = parentAttrTable.getAttr(name)) != null) {
            return ret;
        }
        if (attrRegistry != null) {
            return attrRegistry.getDefault(name);
        }
        return null;
    }

    @Override
    public String getAttrString(String name) {
        String ret;
        if ((ret = (String) attrTable.get(name)) != null) {
            return ret;
        }
        if (parentAttrTable != null && (ret = parentAttrTable.getAttrString(name)) != null) {
            return ret;
        }
        if (attrRegistry != null) {
            return attrRegistry.getDefaultString(name);
        }
        return null;
    }

    @Override
    public boolean getAttrBool(String name) {
        Object ret;
        if ((ret = attrTable.get(name)) != null) {
            return (Boolean) ret;
        }
        if (parentAttrTable != null && (ret = parentAttrTable.getAttr(name)) != null) {
            return (Boolean) ret;
        }
        if (attrRegistry != null) {
            return attrRegistry.getDefaultBool(name);
        }
        return false;
    }

    @Override
    public int getAttrInt(String name) {
        Object ret;
        if ((ret = attrTable.get(name)) != null) {
            return (Integer) ret;
        }
        if (parentAttrTable != null && (ret = parentAttrTable.getAttr(name)) != null) {
            return (Integer) ret;
        }
        if (attrRegistry != null) {
            return attrRegistry.getDefaultInt(name);
        }
        return -1;
    }

    @Override
    public long getAttrLong(String name) {
        Object ret;
        if ((ret = attrTable.get(name)) != null) {
            return ((Number) ret).intValue();
        }
        if (parentAttrTable != null && (ret = parentAttrTable.getAttr(name)) != null) {
            return ((Number) ret).intValue();
        }
        if (attrRegistry != null) {
            return attrRegistry.getDefaultLong(name);
        }
        return -1L;
    }

    @Override
    public float getAttrFloat(String name) {
        Object ret;
        if ((ret = attrTable.get(name)) != null) {
            return ((Number) ret).intValue();
        }
        if (parentAttrTable != null && (ret = parentAttrTable.getAttr(name)) != null) {
            return ((Number) ret).intValue();
        }
        if (attrRegistry != null) {
            return attrRegistry.getDefaultFloat(name);
        }
        return -1f;
    }

    @Override
    public double getAttrDouble(String name) {
        Object ret;
        if ((ret = attrTable.get(name)) != null) {
            return ((Number) ret).intValue();
        }
        if (parentAttrTable != null && (ret = parentAttrTable.getAttr(name)) != null) {
            return ((Number) ret).intValue();
        }
        if (attrRegistry != null) {
            return attrRegistry.getDefaultDouble(name);
        }
        return -1;
    }

    ////////////////////////////////////////////////////////////////////////

    @Override
    public Object getAttr(String name, Object def) {
        Object ret = attrTable.get(name);
        if (ret != null) {
            return ret;
        }
        if (parentAttrTable != null) {
            return parentAttrTable.getAttr(name, def);
        }
        return def;
    }

    @Override
    public String getAttrString(String name, String def) {
        String ret = (String) attrTable.get(name);
        if (ret != null) {
            return ret;
        }
        if (parentAttrTable != null) {
            return parentAttrTable.getAttrString(name, def);
        }
        return def;
    }

    @Override
    public boolean getAttrBool(String name, boolean def) {
        Boolean ret = (Boolean) attrTable.get(name);
        if (ret != null) {
            return ret;
        }
        if (parentAttrTable != null) {
            return parentAttrTable.getAttrBool(name, def);
        }
        return def;
    }

    @Override
    public int getAttrInt(String name, int def) {
        Integer ret = (Integer) attrTable.get(name);
        if (ret != null) {
            return ret;
        }
        if (parentAttrTable != null) {
            return parentAttrTable.getAttrInt(name, def);
        }
        return def;
    }

    @Override
    public long getAttrLong(String name, long def) {
        Long ret = (Long) attrTable.get(name);
        if (ret != null) {
            return ret;
        }
        if (parentAttrTable != null) {
            return parentAttrTable.getAttrLong(name, def);
        }
        return def;
    }

    @Override
    public float getAttrFloat(String name, float def) {
        Float ret = (Float) attrTable.get(name);
        if (ret != null) {
            return ret;
        }
        if (parentAttrTable != null) {
            return parentAttrTable.getAttrFloat(name, def);
        }
        return def;
    }

    @Override
    public double getAttrDouble(String name, double def) {
        Double ret = (Double) attrTable.get(name);
        if (ret != null) {
            return ret;
        }
        if (parentAttrTable != null) {
            return parentAttrTable.getAttrDouble(name, def);
        }
        return def;
    }

    ////////////////////////////////////////////////////////////////////////

    /**
     * Setting attributes always affect the local table only.
     * If attribute name is registered, attribute value must match the registered type.
     * Unregistered attribute values can be any type.
     *
     * @return true if success, false if attribute or value is invalid.
     */

    @Override
    public Object setAttr(String name, Object value) throws UnsupportedOperationException {
        IAttrRegistry r = getAttrRegistry();
        if (r != null) {
            IAttrFactory f = r.get(name);
            if (f != null && !f.isValid(value)) {
                throw new UnsupportedOperationException(
                    NAME
                        + ".setAttr(object): invalid value"
                        + ": attrname="
                        + name
                        + ": attrFactory="
                        + f
                );
            }
        }
        return attrTable.put(name, value);
    }

    /**
     * @return true if success, false if attribute or value is invalid.
     */
    @Override
    public Object setAttr(String name, boolean value) throws UnsupportedOperationException {
        IAttrRegistry r = getAttrRegistry();
        if (r != null) {
            IAttrFactory f = r.get(name);
            if (f != null && !(f instanceof BooleanAttrFactory)) {
                throw new UnsupportedOperationException(
                    NAME
                        + ".setAttr(boolean): expected BooleanAttrFactory"
                        + ": attrname="
                        + name
                        + ": attrFactory="
                        + f
                );
            }
        }
        return attrTable.put(name, value);
    }

    /**
     * @return true if success, false if attribute or value is invalid.
     */
    @Override
    public Object setAttr(String name, int value) throws UnsupportedOperationException {
        IAttrRegistry r = getAttrRegistry();
        if (r != null) {
            IAttrFactory f = r.get(name);
            if (f != null && !(f instanceof IntAttrFactory)) {
                throw new UnsupportedOperationException(
                    NAME
                        + ".setAttr(int): expected IntAttrFactory"
                        + ": attrname="
                        + name
                        + ": attrFactory="
                        + f
                );
            }
        }
        return attrTable.put(name, value);
    }

    /**
     * @return true if success, false if attribute or value is invalid.
     */
    @Override
    public Object setAttr(String name, long value) throws UnsupportedOperationException {
        IAttrRegistry r = getAttrRegistry();
        if (r != null) {
            IAttrFactory f = r.get(name);
            if (f != null && !(f instanceof LongAttrFactory)) {
                throw new UnsupportedOperationException(
                    NAME
                        + ".setAttr(long): expected LongAttrFactory"
                        + ": attrname="
                        + name
                        + ": attrFactory="
                        + f
                );
            }
        }
        return attrTable.put(name, value);
    }

    /**
     * @return true if success, false if attribute or value is invalid.
     */
    @Override
    public Object setAttr(String name, float value) throws UnsupportedOperationException {
        IAttrRegistry r = getAttrRegistry();
        if (r != null) {
            IAttrFactory f = r.get(name);
            if (f != null && !(f instanceof FloatAttrFactory)) {
                throw new UnsupportedOperationException(
                    NAME
                        + ".setAttr(float): expected FloatAttrFactory"
                        + ": attrname="
                        + name
                        + ": attrFactory="
                        + f
                );
            }
        }
        return attrTable.put(name, value);
    }

    /**
     * @return true if success, false if attribute or value is invalid.
     */
    @Override
    public Object setAttr(String name, double value) throws UnsupportedOperationException {
        IAttrRegistry r = getAttrRegistry();
        if (r != null) {
            IAttrFactory f = r.get(name);
            if (f != null && !(f instanceof DoubleAttrFactory)) {
                throw new UnsupportedOperationException(
                    NAME
                        + ".setAttr(Double): expected DoubleAttrFactory"
                        + ": attrname="
                        + name
                        + ": attrFactory="
                        + f
                );
            }
        }
        return attrTable.put(name, value);
    }

    ////////////////////////////////////////////////////////////////////////

    @Override
    public boolean hasAttr(String name) {
        return attrTable.containsKey(name);
    }

    /**
     * Attribute keys backed by original data.
     */
    @Override
    public Collection<String> attrKeys() {
        return attrTable.keySet();
    }

    @Override
    public Object removeAttr(String name) {
        return attrTable.remove(name);
    }

    /**
     * Remove all unregistered attributes.  Remove everything if no attribute registry defined.
     */
    @Override
    public void removeUnregisteredAttrs() {
        IAttrRegistry r = getAttrRegistry();
        if (r == null) {
            attrTable.clear();
        } else {
            for (Iterator<String> it = attrTable.keySet().iterator(); it.hasNext(); ) {
                String attrname = it.next();
                if (r.get(attrname) == null) {
                    it.remove();
                }
            }
        }
    }

    @Override
    public void clearAttrs() {
        attrTable.clear();
    }

    ////////////////////////////////////////////////////////////////////////

    /**
     * Return a String representation of the specified attribute object.
     */
    @Override
    public String getAttrAsString(String name) {
        Object attr = getAttr(name);
        if (attr != null) {
            IAttrRegistry r = getAttrRegistry();
            IAttrFactory f;
            if (r != null && (f = r.get(name)) != null) {
                return f.toString(attr);
            }
            return attr.toString();
        }
        return null;
    }

    /**
     * @return Object created from the string value or null if object not created.
     */
    @Override
    public Object setAttrFromString(String name, String value) {
        IAttrRegistry r = getAttrRegistry();
        if (r != null) {
            IAttrFactory f = r.get(name);
            if (f != null) {
                Object ret = f.createObject(value, null);
                attrTable.put(name, ret);
                return ret;
            }
        }
        attrTable.put(name, value);
        return value;
    }

    public void initAttr(String name, IAttrFactory factory) {
        Object ret = getAttrRegistry().set(name, factory);
        checkOverwritting("IAttrFactory,Object", name, ret, factory);
    }

    public void initAttr(String name, IAttrFactory factory, Object value) {
        Object ret = getAttrRegistry().set(name, factory);
        attrTable.put(name, value);
        checkOverwritting("IAttrFactory,Object", name, ret, factory);
    }

    public void initAttr(String name, String value) {
        IAttrFactory factory = StringAttrFactory.getDefault();
        Object ret = getAttrRegistry().set(name, factory);
        attrTable.put(name, value);
        checkOverwritting("String", name, ret, factory);
    }

    public void initAttr(String name, boolean value) {
        IAttrFactory factory = BooleanAttrFactory.getDefault();
        Object ret = getAttrRegistry().set(name, factory);
        attrTable.put(name, value);
        checkOverwritting("boolean", name, ret, factory);
    }

    public void initAttr(String name, int value) {
        IAttrFactory factory = IntAttrFactory.getDefault();
        Object ret = getAttrRegistry().set(name, factory);
        attrTable.put(name, value);
        checkOverwritting("int", name, ret, factory);
    }

    public void initAttr(String name, long value) {
        IAttrFactory factory = LongAttrFactory.getDefault();
        Object ret = getAttrRegistry().set(name, factory);
        attrTable.put(name, value);
        checkOverwritting("long", name, ret, factory);
    }

    public void initAttr(String name, float value) {
        IAttrFactory factory = FloatAttrFactory.getDefault();
        Object ret = getAttrRegistry().set(name, factory);
        attrTable.put(name, value);
        checkOverwritting("float", name, ret, factory);
    }

    public void initAttr(String name, double value) {
        IAttrFactory factory = DoubleAttrFactory.getDefault();
        Object ret = getAttrRegistry().set(name, factory);
        attrTable.put(name, value);
        checkOverwritting("double", name, ret, factory);
    }

    private void checkOverwritting(String methodname, String attrname, Object oldfactory, IAttrFactory newfactory) {
        if (oldfactory != null) {
            throw new UnsupportedOperationException(
                NAME
                    + ".initAttr("
                    + methodname
                    + "): registry already initialized: attr name="
                    + attrname
                    + ", old factory="
                    + oldfactory
                    + ", new factor="
                    + newfactory
            );
        }
    }

    ////////////////////////////////////////////////////////////////////////
}
