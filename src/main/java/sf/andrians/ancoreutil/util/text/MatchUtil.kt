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
package sf.andrians.ancoreutil.util.text

import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList

/**
 * Utility class that make use of java.util.regex classes
 */
object MatchUtil {
    @get:Synchronized
    val identityTransformer: MatchTransformer
        get() = object : MatchTransformer {
            override fun transform(ret: StringBuilder, input: CharSequence?, matcher: Matcher): Boolean {
                ret.append(matcher.group(0))
                return true
            }
        }

    fun compile1(pat: String?): Pattern? {
        return if (pat == null) {
            null
        } else Pattern.compile(pat)
    }

    fun compile(vararg patterns: String): MutableList<Pattern> {
        val ret = ArrayList<Pattern>()
        for (pat in patterns) {
            ret.add(Pattern.compile(pat))
        }
        return ret
    }

    fun compile(patterns: Iterable<String>?): MutableList<Pattern>? {
        var ret: MutableList<Pattern>? = null
        if (patterns != null) {
            ret = ArrayList()
            for (pat in patterns) {
                ret.add(Pattern.compile(pat))
            }
        }
        return ret
    }

    fun greps(ret: MutableCollection<String>, include: String?, exclude: String?, inputs: Iterable<String>): Int {
        return grep(
                ret,
                if (include == null) null else Pattern.compile(include),
                if (exclude == null) null else Pattern.compile(exclude),
                inputs)
    }

    fun greps(
            ret: MutableCollection<String>, includes: Iterable<String>?, excludes: Iterable<String>?, inputs: Iterable<String>): Int {
        return grep(ret, compile(includes), compile(excludes), inputs)
    }

    fun grep(ret: MutableCollection<String>, include: Pattern?, exclude: Pattern?, inputs: Iterable<String>): Int {
        var count = 0
        for (input in inputs) {
            if (match(input, include, exclude)) {
                ret.add(input)
                ++count
            }
        }
        return count
    }

    /**
     * @param ret        Collection to hold input that match.
     * @param includes    If not null or empty, include only input that match the given patterns.
     * @param includes    If not null or empty, exclude input that match the given patterns.
     * @param inputs
     * @return Number of input that matches.
     */
    fun grep(
            ret: MutableCollection<String>?, includes: Iterable<Pattern>?, excludes: Iterable<Pattern>?, inputs: Iterable<String>): Int {
        var count = 0
        for (input in inputs) {
            if (match(input, includes, excludes)) {
                ret?.add(input)
                ++count
            }
        }
        return count
    }

    fun greps(ret: MutableCollection<String>?, include: String?, exclude: String?, vararg inputs: String): Int {
        return grep(
                ret,
                if (include == null) null else Pattern.compile(include),
                if (exclude == null) null else Pattern.compile(exclude),
                *inputs)
    }

    fun greps(
            ret: MutableCollection<String>?, includes: Iterable<String>?, excludes: Iterable<String>?, vararg inputs: String): Int {
        return grep(ret, compile(includes), compile(excludes), *inputs)
    }

    fun grep(ret: MutableCollection<String>?, include: Pattern?, exclude: Pattern?, vararg inputs: String): Int {
        var count = 0
        for (input in inputs) {
            if (match(input, include, exclude)) {
                ret?.add(input)
                ++count
            }
        }
        return count
    }

    /**
     * @param ret        Collection to hold input that match.
     * @param includes    If not null or empty, include only input that match the given patterns.
     * @param includes    If not null or empty, exclude input that match the given patterns.
     * @param inputs
     * @return Number of input that matches.
     */
    fun grep(
            ret: MutableCollection<String>?, includes: Iterable<Pattern>?, excludes: Iterable<Pattern>?, vararg inputs: String): Int {
        var count = 0
        for (input in inputs) {
            if (match(input, includes, excludes)) {
                ret?.add(input)
                ++count
            }
        }
        return count
    }

    fun match(input: CharSequence, includes: Pattern?, excludes: Pattern?): Boolean {
        if (includes != null) {
            if (!includes.matcher(input).matches()) {
                return false
            }
        }
        if (excludes != null) {
            if (excludes.matcher(input).matches()) {
                return false
            }
        }
        return true
    }

    fun match(input: CharSequence, includes: Iterable<Pattern>?, excludes: Iterable<Pattern>?): Boolean {
        OK@ while (includes != null) {
            val it = includes.iterator()
            if (it.hasNext()) {
                do {
                    if (it.next().matcher(input).matches()) {
                        break@OK
                    }
                } while (it.hasNext())
                return false
            }
            break
        }
        if (excludes != null) {
            for (pat in excludes) {
                if (pat.matcher(input).matches()) {
                    return false
                }
            }
        }
        return true
    }

    fun matches(input: CharSequence, include: String?, exclude: String?): Boolean {
        return match(
                input,
                if (include == null) null else Pattern.compile(include),
                if (exclude == null) null else Pattern.compile(exclude))
    }

    fun matches(input: CharSequence, includes: Array<String>, excludes: Array<String>): Boolean {
        return match(input, compile(*includes), compile(*excludes))
    }

    fun matches(input: CharSequence, includes: Iterable<String>?, excludes: Iterable<String>?): Boolean {
        return match(input, compile(includes), compile(excludes))
    }

    /**
     * @param regex
     * @param input
     * @return The first group of the match, if input matches the given regex, null if not matched.
     */
    fun group1(regex: String, input: String): String? {
        val pat = Pattern.compile(regex)
        val m = pat.matcher(input)
        return if (m.matches() && m.groupCount() > 0) {
            m.group(1)
        } else null
    }

    /**
     * @param regex
     * @param input
     * @return The first group of the match, if input matches the given regex, null if not matched.
     */
    fun group1(regex: String, input: Iterable<String>): Collection<String> {
        val pat = Pattern.compile(regex)
        val ret: MutableCollection<String> = ArrayList()
        for (s in input) {
            val m = pat.matcher(s)
            if (m.matches() && m.groupCount() > 0) {
                ret.add(m.group(1))
            }
        }
        return ret
    }

    /**
     * @param regex        java.util.regex pattern.
     * @param replacement    The replacement string, which may contains back references, $n.
     * @param input
     * @return input if not matched, else the replaced string.
     */
    fun replaceFirst(regex: String, replacement: String, input: CharSequence): String {
        return replaceFirst(Pattern.compile(regex), replacement, input)
    }

    fun replaceFirst(pat: Pattern, replacement: String, input: CharSequence): String {
        val m = pat.matcher(input)
        return m.replaceFirst(replacement)
    }

    /**
     * @param regex        java.util.regex pattern.
     * @param replacement    The replacement string, which may contains back references, $n.
     * @param input
     * @return input if not matched, else the replaced string.
     */
    fun replaceAll(regex: String, replacement: String, input: CharSequence): String {
        return replaceAll(Pattern.compile(regex), replacement, input)
    }

    fun replaceAll(pat: Pattern, replacement: String, input: CharSequence): String {
        val m = pat.matcher(input)
        return m.replaceAll(replacement)
    }

    fun replaceAll(input: CharSequence, pat: Pattern, transformer: MatchTransformer): String {
        val m = pat.matcher(input)
        m.reset()
        var lastposition = 0
        var result = m.find()
        if (result) {
            val sb = StringBuilder()
            do {
                sb.append(input, lastposition, m.start())
                if (!transformer.transform(sb, input, m)) {
                    return input.toString()
                }
                lastposition = m.end()
                result = m.find()
            } while (result)
            val len = input.length
            if (lastposition < len) {
                sb.append(input, lastposition, len)
            }
            return sb.toString()
        }
        return input.toString()
    }

    fun head(pat: Pattern, input: String): String {
        val m = pat.matcher(input)
        return if (m.matches()) {
            input.substring(0, m.start(0))
        } else input
    }

    fun head(regex: String, input: String): String {
        return head(Pattern.compile(regex), input)
    }

    fun tail(pat: Pattern, input: String): String {
        val m = pat.matcher(input)
        return if (m.matches()) {
            input.substring(m.start(0))
        } else input
    }

    fun tail(regex: String, input: String): String {
        return tail(Pattern.compile(regex), input)
    }

    ////////////////////////////////////////////////////////////////////////
    /* For migration from RegexUtil.subst(regex, input) which returns the number of substitution. */
    fun matchCount(regex: String, input: CharSequence): Int {
        return matchCount(Pattern.compile(regex), input)
    }

    fun matchCount(pat: Pattern, input: CharSequence): Int {
        var count = 0
        val matcher = pat.matcher(input)
        while (matcher.find()) {
            ++count
        }
        return count
    }

    /* For migration from RegexUtil.match(regex, input) */
    fun match(regex: String, input: CharSequence): Boolean {
        return match(Pattern.compile(regex), input)
    }

    fun match(pat: Pattern, input: CharSequence): Boolean {
        return pat.matcher(input).find()
    }

    /* For migration from RegexUtil.subst(regex, input) */
    fun subst(regex: String, replacement: String, input: CharSequence): String {
        return Pattern.compile(regex).matcher(input).replaceAll(replacement)
    }

    interface MatchTransformer {
        fun transform(ret: StringBuilder, input: CharSequence?, matcher: Matcher): Boolean
    }

    class CharSequenceFilter private constructor(private val includes: Iterable<Pattern>?, private val excludes: Iterable<Pattern>?) : ICharSequenceFilter {
        override fun filter(s: CharSequence): Boolean {
            return !match(s, includes, excludes)
        }

        companion object {
            fun getInstance(includes: String, excludes: String): ICharSequenceFilter {
                return CharSequenceFilter(compile(includes), compile(excludes))
            }

            fun getInstance(includes: Iterable<String>, excludes: Iterable<String>): ICharSequenceFilter {
                return CharSequenceFilter(compile(includes), compile(excludes))
            }

            fun getInstance(includes: Iterable<String>, vararg excludes: String): ICharSequenceFilter {
                return CharSequenceFilter(compile(includes), compile(*excludes))
            }

            fun getInstance(vararg excludes: String): ICharSequenceFilter {
                return CharSequenceFilter(null, compile(*excludes))
            }
        }

    }
}
