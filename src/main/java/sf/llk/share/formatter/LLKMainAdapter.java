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
package sf.llk.share.formatter;

import java.util.Map;

import sf.llk.share.support.ILogger;
import sf.llk.share.support.SimpleLLKMain;

public class LLKMainAdapter extends SimpleLLKMain {

	ILogger logger;

	public LLKMainAdapter(ILogger logger) {
		this(logger, null, null);
	}

	public LLKMainAdapter(ILogger logger, String path, Map<String, Object> options) {
		super(path, options);
		this.logger = logger;
		if (logger == null)
			setQuiet(true);
	}

	public Object clone() throws CloneNotSupportedException {
		LLKMainAdapter ret = (LLKMainAdapter)super.clone();
		ret.logger = logger;
		return ret;
	}

	public void info(String msg) {
		if (logger != null)
			logger.info(msg);
	}

	public void info(String msg, int offset) {
		if (logger != null)
			logger.info("@" + offset + ": " + msg);
	}

	public void warn(String msg) {
		++warns;
		if (logger != null)
			logger.warn(msg);
	}

	public void warn(String msg, Throwable e) {
		++warns;
		if (logger != null)
			logger.warn(msg, e, getOffset(e));
	}

	public void warn(String msg, Throwable e, int offset) {
		++warns;
		if (logger != null)
			logger.warn("@" + offset + ": " + msg, e);
	}

	public void error(String msg) {
		++errors;
		if (logger != null)
			logger.error(msg);
	}

	public void error(String msg, Throwable e) {
		++errors;
		if (logger != null)
			logger.error(msg, e, getOffset(e));
	}

	public void error(String msg, Throwable e, int offset) {
		++errors;
		if (logger != null)
			logger.error("@" + offset + ": " + msg, e);
	}
}