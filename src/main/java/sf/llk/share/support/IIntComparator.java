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
package sf.llk.share.support;

public interface IIntComparator {
    int compare(int a, int b);

    /**
     * Comparator that sort in descending order, ie. dequeue highest value first.
     */
    public static class DescendingComparator implements IIntComparator {
        private static DescendingComparator singleton;

        public static DescendingComparator getSingleton() {
            if (singleton == null) {
                singleton = new DescendingComparator();
            }
            return singleton;
        }

        @Override
        public int compare(int a, int b) {
            return a - b;
        }
    }

    /**
     * Comparator that sort in ascending order, ie. dequeue lowest value first.
     */
    public static class AscendingComparator implements IIntComparator {
        private static AscendingComparator singleton;

        public static AscendingComparator getSingleton() {
            if (singleton == null) {
                singleton = new AscendingComparator();
            }
            return singleton;
        }

        @Override
        public int compare(int a, int b) {
            return b - a;
        }
    }

    public static class ReverseComparator implements IIntComparator {
        private final IIntComparator comparator;

        public ReverseComparator(IIntComparator c) {
            this.comparator = c;
        }

        @Override
        public int compare(int a, int b) {
            return -comparator.compare(a, b);
        }
    }
}
