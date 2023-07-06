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
package sf.llk.share.util;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

public class ObjectPool<T> {

    protected final Class<T> type;
    protected final int quota;
    protected final List<T> pool;
    protected int size;

    public ObjectPool(Class<T> type) {
        this(type, 0);
    }

    /**
     * @param type  Object class.
     * @param quota Pool size limit, 0 for no limit.
     */
    public ObjectPool(Class<T> type, int quota) {
        this.type = type;
        this.quota = quota;
        this.size = 0;
        this.pool = new LinkedList<>();
    }

    public T get() {
        if (size > 0) {
            return pool.remove(--size);
        }
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) {
        }
        throw new RuntimeException("Fail to create new instance for: type=" + type.getName());
    }

    public void unget(T a) {
        if (quota <= 0 || size < quota) {
            pool.add(a);
            ++size;
        }
    }

    public void clear() {
        pool.clear();
        size = 0;
    }
}
