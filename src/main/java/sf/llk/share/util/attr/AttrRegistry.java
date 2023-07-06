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

import java.util.HashMap;
import java.util.Map;

/**
 * Basic attribute registry.
 */
public class AttrRegistry implements IAttrRegistry {

    ////////////////////////////////////////////////////////////////////////

    final Map<String, IAttrFactory> registry = new HashMap<>();
    final Map<String, Object> defaultTable = new HashMap<>();

    ////////////////////////////////////////////////////////////////////////

    @Override
    public IAttrFactory get(String name) {
        return registry.get(name);
    }

    ////////////////////////////////////////////////////////////////////////

    @Override
    public Object getDefault(String name) {
        return defaultTable.get(name);
    }

    @Override
    public String getDefaultString(String name) {
        return (String) defaultTable.get(name);
    }

    @Override
    public boolean getDefaultBool(String name) {
        Object a = defaultTable.get(name);
        if (a == null) {
            return false;
        }
        return ((Boolean) a);
    }

    @Override
    public int getDefaultInt(String name) {
        Object a = defaultTable.get(name);
        if (a == null) {
            return -1;
        }
        return ((Integer) a);
    }

    @Override
    public long getDefaultLong(String name) {
        Object a = defaultTable.get(name);
        if (a == null) {
            return -1L;
        }
        return ((Long) a);
    }

    @Override
    public float getDefaultFloat(String name) {
        Object a = defaultTable.get(name);
        if (a == null) {
            return -1f;
        }
        return ((Float) a);
    }

    @Override
    public double getDefaultDouble(String name) {
        Object a = defaultTable.get(name);
        if (a == null) {
            return -1.0;
        }
        return ((Double) a);
    }

    @Override
    public String getDefaultAsString(String name) {
        Object a = defaultTable.get(name);
        if (a == null) {
            return null;
        }
        IAttrFactory f = get(name);
        if (f != null) {
            return f.toString(a);
        }
        return a.toString();
    }

    ////////////////////////////////////////////////////////////////////////

    @Override
    public void setDefault(String name, Object value) {
        IAttrFactory f = get(name);
        if (f != null && !f.isValid(value)) {
            throw new UnsupportedOperationException(
                "Invalid value: factory=" + f + ": name=" + name + ", value=" + value);
        }
        defaultTable.put(name, value);
    }

    @Override
    public void setDefault(String name, boolean value) {
        IAttrFactory f = get(name);
        if (f != null && !(f instanceof BooleanAttrFactory)) {
            throw new UnsupportedOperationException(
                "Invalid value: factory=" + f + ": name=" + name + ", value=" + value);
        }
        defaultTable.put(name, (value ? Boolean.TRUE : Boolean.FALSE));
    }

    @Override
    public void setDefault(String name, int value) {
        IAttrFactory f = get(name);
        if (f != null && !(f instanceof IntAttrFactory)) {
            throw new UnsupportedOperationException(
                "Invalid value: factory=" + f + ": name=" + name + ", value=" + value);
        }
        defaultTable.put(name, value);
    }

    @Override
    public void setDefault(String name, long value) {
        IAttrFactory f = get(name);
        if (f != null && !(f instanceof LongAttrFactory)) {
            throw new UnsupportedOperationException(
                "Invalid value: factory=" + f + ": name=" + name + ", value=" + value);
        }
        defaultTable.put(name, value);
    }

    @Override
    public void setDefault(String name, float value) {
        IAttrFactory f = get(name);
        if (f != null && !(f instanceof FloatAttrFactory)) {
            throw new UnsupportedOperationException(
                "Invalid value: factory=" + f + ": name=" + name + ", value=" + value);
        }
        defaultTable.put(name, value);
    }

    @Override
    public void setDefault(String name, double value) {
        IAttrFactory f = get(name);
        if (f != null && !(f instanceof DoubleAttrFactory)) {
            throw new UnsupportedOperationException(
                "Invalid value: factory=" + f + ": name=" + name + ", value=" + value);
        }
        defaultTable.put(name, value);
    }

    ////////////////////////////////////////////////////////////////////////

    @Override
    public IAttrFactory set(String name, IAttrFactory factory) {
        if (registry.containsKey(name)) {
            throw new RuntimeException(
                "Change of attribute factory not allowed:\n\told="
                    + registry.get(name)
                    + "\n\tnew="
                    + factory
            );
        }
        return registry.put(name, factory);
    }

    @Override
    public void set(String name, IAttrFactory factory, Object def) {
        set(name, factory);
        setDefault(name, def);
    }

    @Override
    public void set(String name, boolean def) {
        set(name, BooleanAttrFactory.getDefault());
        setDefault(name, def);
    }

    @Override
    public void set(String name, int def) {
        set(name, IntAttrFactory.getDefault());
        setDefault(name, def);
    }

    @Override
    public void set(String name, long def) {
        set(name, LongAttrFactory.getDefault());
        setDefault(name, def);
    }

    @Override
    public void set(String name, float def) {
        set(name, FloatAttrFactory.getDefault());
        setDefault(name, def);
    }

    @Override
    public void set(String name, double def) {
        set(name, DoubleAttrFactory.getDefault());
        setDefault(name, def);
    }

    @Override
    public IAttrFactory set(Attr attr) {
        String name = attr.getName();
        IAttrFactory ret = set(name, attr.getFactory());
        setDefault(name, attr.getDefaultValue());
        return ret;
    }

    ////////////////////////////////////////////////////////////////////////
}
