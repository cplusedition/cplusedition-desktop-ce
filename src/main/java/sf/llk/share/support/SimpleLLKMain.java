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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * A primitive implementation of ILLKMain used for testing.
 */
public class SimpleLLKMain implements ILLKMain, Cloneable {

    ////////////////////////////////////////////////////////////////////////

    public static final int MAX_ERRORS = 25;
    public static final int MAX_WARNS = 25;
    private static final int DEF_TABWIDTH = 8;
    private static final int BUFSIZE = 32 * 1024;

    ////////////////////////////////////////////////////////////////////////

    protected Map<String, Object> options = new HashMap<>();
    protected ISourceLocator locator;
    protected String filePath;
    protected int maxErrors;
    protected int maxWarns;
    protected boolean isQuiet;
    protected int errors;
    protected int warns;
    protected PrintStream outStream;
    protected PrintStream errStream;

    ////////////////////////////////////////////////////////////////////////

    public static char[] getFileContent(String filepath) throws IOException {
        return getFileContent(filepath, null);
    }

    public static char[] getFileContent(String filepath, Charset charset) throws IOException {
        if (charset == null)
            charset = Charset.defaultCharset();
        if (filepath.equals("-")) {
            return asChars(new InputStreamReader(System.in, charset), -1);
        }
        return asChars(new File(filepath), charset);
    }

    public static char[] asChars(File file) throws IOException {
        Reader r = null;
        try {
            return asChars(r = new FileReader(file), (int) file.length());
        } finally {
            if (r != null)
                r.close();
        }
    }

    public static char[] asChars(File file, Charset charset) throws IOException {
        Reader r = null;
        try {
            InputStream s = new FileInputStream(file);
            if (file.getName().toLowerCase().endsWith(".gz"))
                s = new GZIPInputStream(s);
            return asChars(r = new InputStreamReader(s, charset), (int) file.length());
        } finally {
            if (r != null)
                r.close();
        }
    }

    public static char[] asChars(Reader r, int length) throws IOException {
        List<char[]> list = new ArrayList<>();
        int total = asArrayList(list, r, length);
        if (list.size() == 1) {
            char[] a = list.get(0);
            if (a.length == total)
                return a;
        }
        char[] ret = new char[total];
        int offset = 0;
        for (char[] a : list) {
            int len = (offset + a.length > total) ? total - offset : a.length;
            System.arraycopy(a, 0, ret, offset, len);
            offset += len;
        }
        return ret;
    }

    public static ICharSequence asTextRange(File file) throws IOException {
        Reader r = null;
        try {
            return asTextRange(r = new FileReader(file), (int) file.length());
        } finally {
            if (r != null)
                r.close();
        }
    }

    public static ICharSequence asTextRange(File file, Charset charset) throws IOException {
        Reader r = null;
        try {
            InputStream s = new FileInputStream(file);
            if (file.getName().toLowerCase().endsWith(".gz"))
                s = new GZIPInputStream(s);
            return asTextRange(r = new InputStreamReader(s, charset), (int) file.length());
        } finally {
            if (r != null)
                r.close();
        }
    }

    public static ICharSequence asTextRange(Reader r, int length) throws IOException {
        List<char[]> list = new ArrayList<>();
        int total = asArrayList(list, r, BUFSIZE);
        return new TextRange(list, BUFSIZE, total);
    }

    public static int asArrayList(List<char[]> list, Reader r, int length) throws IOException {
        if (length <= 0)
            length = BUFSIZE;
        char[] b = new char[length];
        list.add(b);
        int size = 0;
        int start = 0;
        int n;
        int c = r.read();
        if (c == -1)
            return 0;
        if (c != '\ufeff' && c != '\ufffe') {
            b[0] = (char) c;
            ++start;
            ++size;
        }
        while ((n = r.read(b, start, length - start)) != -1) {
            size += n;
            start += n;
            if (start == length) {
                start = 0;
                length = BUFSIZE;
                list.add(b = new char[BUFSIZE]);
            }
        }
        return size;
    }

    public static String simpleFilename(String path) {
        if (path == null)
            return "";
        int index = path.lastIndexOf('/');
        if (index >= 0)
            return path.substring(index + 1);
        return path;
    }

    ////////////////////////////////////////////////////////////////////////

    public SimpleLLKMain() {
        this(null, null, null);
    }

    public SimpleLLKMain(String filepath) {
        this(filepath, null, null);
    }

    public SimpleLLKMain(Map<String, Object> opts) {
        this(null, opts, null);
    }

    public SimpleLLKMain(String filepath, Map<String, Object> opts) {
        this(filepath, opts, null);
    }

    public SimpleLLKMain(String filepath, Map<String, Object> opts, ISourceLocator locator) {
        this(filepath, opts, locator, System.out, System.err);
    }

    public SimpleLLKMain(
        String filepath, Map<String, Object> opts, ISourceLocator locator, PrintStream out, PrintStream err) {
        this.options = (opts == null ? new HashMap<>() : opts);
        this.locator = (locator == null) ? new SourceLocator(DEF_TABWIDTH) : locator;
        this.outStream = out;
        this.errStream = err;
        maxErrors = getOptInt("errors", MAX_ERRORS);
        maxWarns = getOptInt("warns", MAX_WARNS);
        setFilepath(filepath);
    }

    public Object clone() throws CloneNotSupportedException {
        SimpleLLKMain ret = (SimpleLLKMain) super.clone();
        ret.options = new HashMap<>(options);
        ret.locator = (ISourceLocator) locator.clone();
        return ret;
    }

    public ILLKMain setFilepath(String path) {
        this.filePath = path;
        locator.reset(path, null);
        return this;
    }

    public ILLKMain setQuiet(boolean b) {
        isQuiet = b;
        return this;
    }

    public ILLKMain setMaxErrors(int n) {
        maxErrors = n;
        return this;
    }

    public ILLKMain setMaxWarns(int n) {
        maxWarns = n;
        return this;
    }

    public ILLKMain putOpt(String name, Object value) {
        options.put(name, value);
        return this;
    }

    public boolean isQuiet() {
        return isQuiet;
    }

    public boolean errorsSuppressed() {
        return isQuiet || errors >= maxErrors;
    }

    public boolean warningsSuppressed() {
        return isQuiet || warns >= maxWarns;
    }

    public String getFilepath() {
        return filePath;
    }

    public ISourceLocator getLocator() {
        return locator;
    }

    public boolean hasErrors() {
        return errors != 0;
    }

    public boolean hasWarnings() {
        return warns != 0;
    }

    public char[] getFileContent() throws IOException {
        return getFileContent(filePath, null);
    }

    public char[] getFileContent(Charset charset) throws IOException {
        return getFileContent(filePath, charset);
    }

    public void reset() {
        warns = 0;
        errors = 0;
    }

    ////////////////////////////////////////////////////////////////////////

    public Map<String, Object> getOptions() {
        return options;
    }

    public Object getOpt(String name) {
        return options.get(name);
    }

    public boolean getOptBool(String name) {
        Object ret = options.get(name);
        if (ret != null) {
            if (ret instanceof Boolean)
                return (Boolean) ret;
            if (ret instanceof String)
                return Boolean.valueOf((String) ret);
            return true;
        }
        return false;
    }

    public int getOptInt(String name) {
        return getOptInt(name, -1);
    }

    public int getOptInt(String name, int def) {
        Object ret = options.get(name);
        if (ret != null) {
            if (ret instanceof Integer)
                return (Integer) ret;
            return Integer.parseInt(ret.toString());
        }
        return def;
    }

    public long getOptLong(String name) {
        return getOptLong(name, -1);
    }

    public long getOptLong(String name, long def) {
        Object ret = options.get(name);
        if (ret != null) {
            if (ret instanceof Long)
                return ((Long) ret).intValue();
            return Long.parseLong(ret.toString());
        }
        return def;
    }

    public float getOptFloat(String name) {
        return getOptFloat(name, -1f);
    }

    public float getOptFloat(String name, float def) {
        Object ret = options.get(name);
        if (ret != null) {
            if (ret instanceof Float)
                return ((Float) ret).intValue();
            return Float.parseFloat(ret.toString());
        }
        return def;
    }

    public double getOptDouble(String name) {
        return getOptDouble(name, -1);
    }

    public double getOptDouble(String name, double def) {
        Object ret = options.get(name);
        if (ret != null) {
            if (ret instanceof Double)
                return (Double) ret;
            return Double.parseDouble(ret.toString());
        }
        return def;
    }

    public String getOptString(String name) {
        return getOptString(name, null);
    }

    public String getOptString(String name, String def) {
        Object ret = options.get(name);
        if (ret == null)
            return def;
        return ret.toString();
    }

    @SuppressWarnings("unchecked")
    public List<String> getOptStringList(String name) {
        return (List<String>) options.get(name);
    }

    ////////////////////////////////////////////////////////////////////////

    public void info(String msg) {
        info(msg, null, null);
    }

    public void info(String msg, int offset) {
        info(msg, null, locator.getLocation(offset));
    }

    public void info(String msg, Throwable e) {
        info(msg, e, (e instanceof ILLKParseException ? ((ILLKParseException) e).getLocation() : null));
    }

    public void info(String msg, Throwable e, int offset) {
        info(msg, e, locator.getLocation(offset));
    }

    private void info(String msg, Throwable e, ISourceLocation loc) {
        if (isQuiet)
            return;
        outStream.println("INFO: " + (loc == null ? "" : loc + ": ") + msg);
        if (e != null)
            e.printStackTrace(outStream);
    }

    public void warn(String msg) {
        warn(msg, null, null);
    }

    public void warn(String msg, int offset) {
        warn(msg, null, locator.getLocation(offset));
    }

    public void warn(String msg, Throwable e) {
        warn(msg, e, (e instanceof ILLKParseException ? ((ILLKParseException) e).getLocation() : null));
    }

    public void warn(String msg, Throwable e, int offset) {
        warn(msg, e, locator.getLocation(offset));
    }

    public void warn(String msg, Throwable e, ISourceLocation loc) {
        ++warns;
        if (isQuiet)
            return;
        if (warns == maxWarns) {
            outStream.println("WARN: Too many warnings, warning messages are suppressed.");
            return;
        } else if (warns > maxWarns && maxWarns >= 0)
            return;
        outStream.println("WARN: " + (loc == null ? "" : loc + ": ") + msg);
        if (e != null)
            e.printStackTrace(outStream);
    }

    public void error(String msg) {
        error(msg, null, null);
    }

    public void error(String msg, int offset) {
        error(msg, new Throwable(), locator.getLocation(offset));
    }

    public void error(String msg, Throwable e) {
        error(msg, e, (e instanceof ILLKParseException ? ((ILLKParseException) e).getLocation() : null));
    }

    public void error(String msg, Throwable e, int offset) {
        error(msg, e, locator.getLocation(offset));
    }

    public void error(String msg, Throwable e, ISourceLocation loc) {
        ++errors;
        if (isQuiet)
            return;
        if (errors == maxErrors) {
            errStream.println("ERROR: Too many errors, error messages are suppressed.");
            return;
        } else if (errors > maxErrors && maxErrors >= 0)
            return;
        errStream.println("ERROR: " + (loc == null ? "" : loc + ": ") + msg);
        if (e != null)
            e.printStackTrace(errStream);
    }

    public void exit(int code) {
        System.exit(code);
    }

    ////////////////////////////////////////////////////////////////////////

    protected void warn() {
        ++warns;
    }

    protected void error() {
        ++errors;
    }

    protected int getOffset(Throwable e) {
        if (e != null && e instanceof ILLKParseException) {
            ISourceLocation loc = ((ILLKParseException) e).getLocation();
            if (loc != null)
                return loc.getOffset();
        }
        return -1;
    }

    ////////////////////////////////////////////////////////////////////////

    private static class TextRange implements ICharSequence {

        private List<char[]> buffers = new ArrayList<>(16);
        private final int bufferSize;
        private final int totalSize;

        public TextRange(List<char[]> buffers, int bufsize, int totalsize) {
            this.buffers = buffers;
            this.bufferSize = bufsize;
            this.totalSize = totalsize;
        }

        public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
            if (srcEnd > totalSize)
                throw new IndexOutOfBoundsException("totalSize=" + totalSize + ", srcEnd=" + srcEnd);
            int n = srcBegin / bufferSize;
            while (srcBegin < srcEnd) {
                char[] a = buffers.get(n);
                int start = n * bufferSize;
                int offset = srcBegin - start;
                int len = bufferSize;
                if (start + len > srcEnd)
                    len = srcEnd - start;
                System.arraycopy(a, offset, dst, dstBegin, len);
                dstBegin += len;
                srcBegin += len;
                ++n;
            }
        }

        public char charAt(int index) {
            int n = index / bufferSize;
            char[] a = buffers.get(n);
            return a[index - (n * bufferSize)];
        }

        public int length() {
            return totalSize;
        }

        public CharSequence subSequence(int start, int end) {
            throw new UnsupportedOperationException();
        }
    }

    ////////////////////////////////////////////////////////////////////////
}
