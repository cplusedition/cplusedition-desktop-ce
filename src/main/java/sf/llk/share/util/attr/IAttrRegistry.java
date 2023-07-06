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
 * Attribute registry interface.
 */
public interface IAttrRegistry {

    /**
     * @return the attribute factory that defines the attribute type.
     */
    public IAttrFactory get(String name);

    public IAttrFactory set(Attr attr);

    public IAttrFactory set(String name, IAttrFactory factory);

    public void set(String name, IAttrFactory factory, Object def);

    public void set(String name, boolean def);

    public void set(String name, int def);

    public void set(String name, long def);

    public void set(String name, float def);

    public void set(String name, double def);

    public Object getDefault(String name);

    public String getDefaultString(String name);

    public boolean getDefaultBool(String name);

    public int getDefaultInt(String name);

    public long getDefaultLong(String name);

    public float getDefaultFloat(String name);

    public double getDefaultDouble(String name);

    public String getDefaultAsString(String name);

    public void setDefault(String name, Object value);

    public void setDefault(String name, boolean value);

    public void setDefault(String name, int value);

    public void setDefault(String name, long value);

    public void setDefault(String name, float value);

    public void setDefault(String name, double value);
}
