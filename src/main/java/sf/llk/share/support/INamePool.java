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
package sf.llk.share.support;

/*
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */

public interface INamePool extends Iterable<IName> {
	IName intern(char[] s);
	IName intern(char[] s, int start, int end);
	IName intern(CharSequence s);
	IName intern(CharSequence s, int start, int end);
	IName get(char[] s);
	IName get(char[] s, int start, int end);
	IName get(CharSequence s);
	IName get(CharSequence s, int start, int end);
	IName get(int index);
	int size();
	void clear();
}
