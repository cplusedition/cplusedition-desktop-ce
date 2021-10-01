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
package sf.andrians.cplusedition.support

import com.cplusedition.bot.core.ILog
import java.util.logging.Level
import java.util.logging.Logger

open class JavaLoggerAdapter : ILog {

    private val logger = Logger.getLogger(this::class.java.getPackage()!!.name)

    override fun d(msg: String, e: Throwable?) {
    }

    override fun i(msg: String, e: Throwable?) {
        log(Level.INFO, msg, e);
    }

    override fun w(msg: String, e: Throwable?) {
        log(Level.WARNING, msg, e);
    }

    override fun e(msg: String, e: Throwable?) {
        log(Level.SEVERE, msg, e);
    }

    private fun log(level: Level, msg: String, e: Throwable?) {
        if (e == null) {
            logger.log(level, msg);
            return
        }
        logger.log(level, "$msg: ${e.message}");
    }
}

open class ConsoleLoggerAdapter : ILog {

    override fun d(msg: String, e: Throwable?) {
    }

    override fun i(msg: String, e: Throwable?) {
        println(msg);
        System.out.flush()
        System.err.flush()
    }

    override fun w(msg: String, e: Throwable?) {
        i(msg, e)
    }

    override fun e(msg: String, e: Throwable?) {
        i(msg, e)
    }
}
