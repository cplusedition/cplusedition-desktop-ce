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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public interface ILLKMain extends ILogger {

    /**
     * Set the top level file and reset the source locator.
     */
    ILLKMain setFilepath(String path);

    /**
     * @param b true to suppress warning and error messages.
     */
    ILLKMain setQuiet(boolean b);

    ILLKMain setMaxWarns(int max);

    ILLKMain setMaxErrors(int max);

    ILLKMain putOpt(String name, Object value);

    String getFilepath();

    char[] getFileContent() throws IOException;

    char[] getFileContent(Charset charset) throws IOException;

    ISourceLocator getLocator();

    boolean isQuiet();

    boolean hasErrors();

    boolean hasWarnings();

    boolean errorsSuppressed();

    boolean warningsSuppressed();

    Map<String, Object> getOptions();

    Object getOpt(String name);

    boolean getOptBool(String name);

    int getOptInt(String name);

    int getOptInt(String name, int def);

    long getOptLong(String name);

    long getOptLong(String name, long def);

    float getOptFloat(String name);

    float getOptFloat(String name, float def);

    double getOptDouble(String name);

    double getOptDouble(String name, double def);

    String getOptString(String name);

    String getOptString(String name, String def);

    /**
     * @return null if not found.
     */
    List<String> getOptStringList(String name);

    /**
     * Clears error and warning counters but do NOT reset locator.
     */
    void reset();

    Object clone() throws CloneNotSupportedException;
}
