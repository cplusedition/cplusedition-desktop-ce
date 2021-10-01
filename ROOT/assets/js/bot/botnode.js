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
"use strict";
/// Utilities that works with nodejs.
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.Encoding = exports.IOUt = exports.Filepath = exports.Basepath = void 0;
const botcore_1 = require("./botcore");
const fs = require("fs");
const Path = require("path");
class Basepath {
    constructor(...segments) {
        this._path = null;
        this._basepath = Path.parse(segments.join(Path.sep));
    }
    get path$() {
        if (this._path == null) {
            this._path = Path.join(this._basepath.dir, this._basepath.base);
        }
        return this._path;
    }
    get dir$() {
        return this._basepath.dir;
    }
    /// @return filename with suffix.
    get nameWithSuffix$() {
        return this._basepath.base;
    }
    /// @return filename without suffix.
    get nameWithoutSuffix$() {
        return this._basepath.name;
    }
    get suffix$() {
        return this._basepath.ext;
    }
    get lcSuffix$() {
        return this._basepath.ext.toLowerCase();
    }
    get ext$() {
        const suffix = this.suffix$;
        return suffix.length == 0 ? null : suffix.substring(1);
    }
    get lcExt$() {
        const lcsuffix = this.lcSuffix$;
        return lcsuffix.length == 0 ? null : lcsuffix.substring(1);
    }
    get parent$() {
        return new Basepath(this.dir$);
    }
    get dirAndNameWithoutSuffix$() {
        const dir = this.dir$;
        const name = this.nameWithoutSuffix$;
        return dir.length == 0 ? name : dir + Path.sep + name;
    }
    sibling_(base) {
        return new Basepath(this.dir$, base);
    }
    toString() {
        return this.path$;
    }
    /// If dir is null return name. If dir empty return /name. If dir == "." return ./name
    static joinPath(dir, name) {
        if (dir == null)
            return name;
        if (name.length == 0)
            return dir;
        return this.joinPath0(dir, name);
    }
    /// Like joinPath() but return a relative path if directory is null, empty or ".".
    static joinRpath(dir, name) {
        if (dir == null || dir.length == 0 || dir == ".")
            return name;
        if (name.length == 0)
            return dir;
        return this.joinPath0(dir, name);
    }
    /// Like joinPath() but return dir/ if name is empty.
    static joinPath1(dir, name) {
        if (dir == null)
            return name;
        return this.joinPath0(dir, name);
    }
    /// Like joinRpath() but return dir/ if name is empty.
    static joinRpath1(dir, name) {
        if (dir == null || dir.length == 0 || dir == ".")
            return name;
        return this.joinPath0(dir, name);
    }
    static joinPath0(dir, name) {
        if (dir.endsWith(Path.sep) || name.startsWith(Path.sep))
            return `${dir}${name}`;
        return `${dir}${Path.sep}${name}`;
    }
    /** This differ from Basepath() in that it returns name as "" if input has trailing /. */
    static splitPath1(path) {
        const cleanpath = Path.normalize(path);
        const index = cleanpath.lastIndexOf(Path.sep);
        if (index < 0)
            return [null, cleanpath.toString()];
        return [cleanpath.substring(0, index), cleanpath.substring(index + 1)];
    }
    static splitName(name) {
        const index = name.lastIndexOf('.');
        if (index <= 0)
            return [name, ""];
        return [name.substring(0, index), name.substring(index + 1)];
    }
}
exports.Basepath = Basepath;
class Filepath extends Basepath {
    constructor(...segments) {
        let base = Path.sep;
        let path = Path.normalize(segments.join(Path.sep));
        if (!path.startsWith(Path.sep)) {
            base = process.cwd();
            path = Path.normalize(base + Path.sep + path);
        }
        super(path);
    }
    static resolve_(basedir, ...segments) {
        let path = Path.normalize(segments.join(Path.sep));
        if (!path.startsWith(Path.sep)) {
            path = basedir + Path.sep + path;
        }
        return new Filepath(path);
    }
    static pwd_() {
        return new Filepath(process.cwd());
    }
    static aMktmpdir_(prefix = "temp") {
        return new Promise((resolve, reject) => {
            fs.mkdtemp(prefix, (err, path) => __awaiter(this, void 0, void 0, function* () {
                if (err != null)
                    reject(err);
                resolve(new Filepath(path));
            }));
        });
    }
    static aTmpdir_(code) {
        return __awaiter(this, void 0, void 0, function* () {
            return Filepath.aMktmpdir_().then((tmpdir) => __awaiter(this, void 0, void 0, function* () {
                yield code(tmpdir).finally(() => __awaiter(this, void 0, void 0, function* () {
                    yield tmpdir.aRmdirTree_();
                }));
            }));
        });
    }
    static mktmpdir_(prefix = "temp") {
        return new Filepath(fs.mkdtempSync(prefix));
    }
    static tmpdir_(code) {
        const tmpdir = new Filepath(fs.mkdtempSync("temp"));
        try {
            code(tmpdir);
        }
        finally {
            tmpdir.deleteTree_();
        }
    }
    /// @return An relative path of this file under the given basedir 
    /// or null if this file is not under the given basedir.
    rpathUnder_(basedir) {
        let path = this.path$;
        let basepath = basedir.path$ + Path.sep;
        if (path.startsWith(basepath)) {
            return path.substring(basepath.length);
        }
        return null;
    }
    file_(...segments) {
        return new Filepath(this.path$, segments.join(Path.sep));
    }
    /// @param base Filename with suffix.
    changeNameWithSuffix_(base) {
        return new Filepath(this.dir$, base);
    }
    /// @param name Filename without suffix.
    changeNameWithoutSuffix_(name) {
        return new Filepath(this.dir$, name + this.suffix$);
    }
    /// @param suffix Filename suffix with ".".
    changeSuffix_(suffix) {
        return new Filepath(this.dir$, this.nameWithoutSuffix$ + suffix);
    }
    toString() {
        return this.path$;
    }
    ///////////////////////////////////////////////////////////////
    get parentFilepath$() {
        const dir = this.dir$;
        return dir == null ? null : new Filepath(dir);
    }
    get exists$() {
        return this.access_(fs.constants.F_OK);
    }
    get existsOrFail$() {
        fs.accessSync(this.path$, fs.constants.F_OK);
        return this;
    }
    get canRead$() {
        return this.access_(fs.constants.R_OK);
    }
    get canWrite$() {
        return this.access_(fs.constants.W_OK);
    }
    get lstatOrNull$() {
        try {
            return fs.lstatSync(this.path$);
        }
        catch (_e) {
            return null;
        }
    }
    get lstatOrFail$() {
        return fs.lstatSync(this.path$);
    }
    get statOrNull$() {
        try {
            return fs.statSync(this.path$);
        }
        catch (_e) {
            return null;
        }
    }
    get statOrFail$() {
        return fs.statSync(this.path$);
    }
    get isFile$() {
        var _a, _b;
        return (_b = (_a = this.statOrNull$) === null || _a === void 0 ? void 0 : _a.isFile()) !== null && _b !== void 0 ? _b : false;
    }
    get isDir$() {
        var _a, _b;
        return (_b = (_a = this.statOrNull$) === null || _a === void 0 ? void 0 : _a.isDirectory()) !== null && _b !== void 0 ? _b : false;
    }
    get isSymLink$() {
        var _a, _b;
        return (_b = (_a = this.lstatOrNull$) === null || _a === void 0 ? void 0 : _a.isSymbolicLink()) !== null && _b !== void 0 ? _b : false;
    }
    get isEmptyDir$() {
        return this.listOrEmpty_().length == 0;
    }
    get lastModified$() {
        const stat = this.statOrNull$;
        if (stat == null)
            return 0;
        return stat.mtimeMs;
    }
    get lastModifiedOrFail$() {
        return this.statOrFail$.mtimeMs;
    }
    get length$() {
        var _a, _b;
        return (_b = (_a = this.statOrNull$) === null || _a === void 0 ? void 0 : _a.mtimeMs) !== null && _b !== void 0 ? _b : 0;
    }
    get lengthOrFail$() {
        return this.statOrFail$.size;
    }
    ///////////////////////////////////////////////////////////////
    access_(flag) {
        try {
            fs.accessSync(this.path$, flag);
            return true;
        }
        catch (_e) {
            return false;
        }
    }
    isNewerThan_(other) {
        return this.lastModifiedOrFail$ > other.length$;
    }
    /// Remove this file if this is a file.
    delete_() {
        try {
            this.deleteOrFail_();
            return true;
        }
        catch (e) {
            return false;
        }
    }
    deleteOrFail_() {
        if (fs.existsSync(this.path$)) {
            fs.unlinkSync(this.path$);
        }
        return this;
    }
    /// Remove this directory if it is an empty directory.
    deleteDir_() {
        try {
            fs.rmdirSync(this.path$);
            return true;
        }
        catch (e) {
            return false;
        }
    }
    deleteDirOrFail_() {
        if (fs.existsSync(this.path$)) {
            fs.rmdirSync(this.path$);
        }
        return this;
    }
    /// Remove everything under this directory.
    deleteSubtrees_() {
        let ret = true;
        for (const name of this.listOrEmpty_()) {
            const file = this.file_(name);
            const stat = file.statOrFail$;
            if (stat.isFile()) {
                if (!file.delete_())
                    ret = false;
            }
            else if (stat.isDirectory()) {
                if (!file.deleteSubtrees_())
                    ret = false;
                if (!file.deleteDir_())
                    ret = false;
            }
        }
        return ret;
    }
    deleteSubtreesOrFail_() {
        if (!this.deleteSubtrees_())
            throw this.path$;
        return this;
    }
    /// Remove this directory and everything under this directory.
    deleteTree_() {
        if (!this.deleteSubtrees_())
            return false;
        if (!this.deleteDir_())
            return false;
        return true;
    }
    deleteTreeOrFail_() {
        if (!this.deleteTree_())
            throw this.path$;
        return this;
    }
    rename_(to) {
        const tofile = (to instanceof Filepath) ? to : new Filepath(to);
        try {
            fs.renameSync(this.path$, tofile.path$);
            return true;
        }
        catch (e) {
            return false;
        }
    }
    copyFileAs_(tofile, dirmode = Filepath.DIRMODE) {
        const tofilepath = (tofile instanceof Filepath) ? tofile : new Filepath(tofile);
        tofilepath.mkparentOrFail_(dirmode);
        fs.copyFileSync(this.path$, tofilepath.path$);
    }
    copyFileToDir_(todir, dirmode = Filepath.DIRMODE) {
        const dstdir = (todir instanceof Filepath) ? todir : new Filepath(todir);
        dstdir.mkdirsOrFail_(dirmode);
        fs.copyFileSync(this.path$, dstdir.file_(this.nameWithSuffix$).path$);
    }
    /// @return Number of files, not including directories, copied.
    copyDirToDir_(todir, accept = null, dirmode = Filepath.DIRMODE) {
        let ret = 0;
        const dstdir = (todir instanceof Filepath) ? todir : new Filepath(todir);
        this.walk_((src, rpath, stat) => {
            if (accept != null && !accept(src, rpath, stat))
                return;
            const dst = dstdir.file_(rpath);
            if (stat.isFile()) {
                dst.mkparentOrFail_(dirmode);
                fs.copyFileSync(src.path$, dst.path$);
                ++ret;
            }
            else if (stat.isDirectory()) {
                dst.mkdirsOrFail_(dirmode);
            }
        });
        return ret;
    }
    readBytes_() {
        return fs.readFileSync(this.path$);
    }
    readText_(encoding = Encoding.utf8$) {
        return fs.readFileSync(this.path$).toString(encoding);
    }
    differ_(other) {
        return fs.readFileSync(this.path$).compare(fs.readFileSync(other.path$)) != 0;
    }
    writeText_(data, options) {
        fs.writeFileSync(this.path$, data, options);
    }
    writeData_(data, options) {
        fs.writeFileSync(this.path$, data, options);
    }
    getOutputStream_(options) {
        return fs.createWriteStream(this.path$, options);
    }
    getInputStream_(options) {
        return fs.createReadStream(this.path$, options);
    }
    chmod_(mode) {
        try {
            fs.chmodSync(this.path$, mode);
            ;
            return true;
        }
        catch (_e) {
            return false;
        }
    }
    chmodOrFail_(mode) {
        fs.chmodSync(this.path$, mode);
        return this;
    }
    /// @param time in sec.
    setLastModified_(time) {
        try {
            fs.utimesSync(this.path$, time, time);
            return true;
        }
        catch (e) {
            return false;
        }
    }
    setLastModifiedOrFail_(time) {
        fs.utimesSync(this.path$, time, time);
        return this;
    }
    /// @return true if directory already exists or created.
    mkdirs_(mode = Filepath.DIRMODE) {
        if (this.exists$)
            return true;
        try {
            fs.mkdirSync(this.path$, { recursive: true, mode: mode });
            return true;
        }
        catch (_e) {
            return false;
        }
    }
    /// @return true if parent directory already exists or created.
    mkparent_(mode = Filepath.DIRMODE) {
        return new Filepath(this.dir$).mkdirs_(mode);
    }
    /// @return Ths file if directory already exists or created, otherwise throw an exception.
    mkdirsOrFail_(mode = Filepath.DIRMODE) {
        if (this.exists$)
            return this;
        fs.mkdirSync(this.path$, { recursive: true, mode: mode });
        return this.existsOrFail$;
    }
    /// @return This file if parent directory already exists or created, otherwse throw an exception.
    mkparentOrFail_(mode = Filepath.DIRMODE) {
        new Filepath(this.dir$).mkdirsOrFail_(mode);
        return this;
    }
    listOrEmpty_() {
        try {
            return fs.readdirSync(this.path$);
        }
        catch (e) {
            return [];
        }
    }
    walk_(callback) {
        this.walk1_("", this.statOrFail$, callback);
    }
    scan_(callback) {
        this.scan1_("", this.statOrFail$, callback);
    }
    ///////////////////////////////////////////////////////////////
    aStat_() {
        return __awaiter(this, void 0, void 0, function* () {
            return new Promise((resolve, _reject) => {
                fs.stat(this.path$, (err, stat) => {
                    if (err)
                        resolve(null);
                    else
                        resolve(stat);
                });
            });
        });
    }
    aLstat_() {
        return __awaiter(this, void 0, void 0, function* () {
            return new Promise((resolve, _reject) => {
                fs.lstat(this.path$, (err, stat) => {
                    if (err)
                        resolve(null);
                    else
                        resolve(stat);
                });
            });
        });
    }
    /// Remove everything under this directory.
    aRmdirSubtrees_() {
        let ret = true;
        return this.aWalk_((file, _rpath, stat, done) => {
            let ok = (ok) => {
                if (!ok)
                    ret = false;
                done();
            };
            if (stat.isFile()) {
                ok(file.delete_());
            }
            else {
                ok(file.deleteDir_());
            }
        }).then(() => {
            return ret;
        });
    }
    /// Remove this directory and everything under this directory.
    aRmdirTree_() {
        return this.aRmdirSubtrees_().then((ok) => {
            if (!ok)
                return false;
            return this.deleteDir_();
        });
    }
    aCopyFileAs_(tofile, dirmode = Filepath.DIRMODE) {
        return new Promise((resolve, reject) => {
            const tofilepath = (tofile instanceof Filepath) ? tofile : new Filepath(tofile);
            tofilepath.mkparentOrFail_(dirmode);
            fs.copyFile(this.path$, tofilepath.path$, (err) => {
                if (err == null)
                    resolve();
                else
                    reject(err);
            });
        });
    }
    aCopyFileToDir_(todir, dirmode = Filepath.DIRMODE) {
        return new Promise((resolve, reject) => {
            const dstdir = (todir instanceof Filepath) ? todir : new Filepath(todir);
            fs.copyFile(this.path$, dstdir.mkdirsOrFail_(dirmode).file_(this.nameWithSuffix$).path$, (err) => {
                if (err == null)
                    resolve();
                else
                    reject(err);
            });
        });
    }
    /// @return Number of files, not including directories, copied.
    aCopyDirToDir_(todir, accept = null, dirmode = Filepath.DIRMODE) {
        const dstdir = (todir instanceof Filepath) ? todir : new Filepath(todir);
        let ret = 0;
        return this.aWalk_((src, rpath, stat, done) => {
            let copy1 = (yes) => {
                if (!yes) {
                    done();
                    return;
                }
                let dst = dstdir.file_(rpath);
                this.acopy1_(dst, src, stat, dirmode, (count) => {
                    ret += count;
                    done();
                });
            };
            if (accept == null) {
                copy1(true);
            }
            else {
                accept(src, rpath, stat, copy1);
            }
        }).then(() => {
            return ret;
        });
    }
    /// Update file/directory under this directory from another directory if file differ.
    /// @return [copied, missing] Number of files, not including directories, copied 
    /// and number of files missing at the other directory.
    aUpdateDirFromDir_(srcdir, accept = null, dirmode = Filepath.DIRMODE) {
        const fromdir = (srcdir instanceof Filepath) ? srcdir : new Filepath(srcdir);
        let copied = 0;
        let missing = 0;
        return this.aWalk_((file, rpath, stat, done) => {
            let copy1 = (yes) => __awaiter(this, void 0, void 0, function* () {
                if (!yes) {
                    done();
                    return;
                }
                let src = fromdir.file_(rpath);
                if (src.exists$) {
                    ++missing;
                    done();
                    return;
                }
                this.acopy1_(file, src, stat, dirmode, (count) => {
                    copied += count;
                    done();
                });
            });
            if (accept == null) {
                copy1(true);
            }
            else {
                accept(file, rpath, stat, copy1);
            }
        }).then(() => {
            return [copied, missing];
        });
    }
    aContentEquals_(another) {
        return this.aReadFileOrFail_().then((a) => {
            return another.aReadFileOrFail_().then((b) => {
                return a.equals(b);
            });
        });
    }
    aReadFileOrFail_() {
        return new Promise((resolve, reject) => {
            fs.readFile(this.path$, (err, content) => {
                if (err == null)
                    resolve(content);
                else
                    reject(err);
            });
        });
    }
    aWriteFileOrFail_(data, options) {
        return new Promise((resolve, reject) => {
            fs.writeFile(this.path$, data, options, (err) => {
                if (err == null)
                    resolve();
                else
                    reject(err);
            });
        });
    }
    aReadTextOrNull_() {
        return new Promise((resolve, _reject) => {
            fs.readFile(this.path$, (err, content) => {
                if (err == null)
                    resolve(content.toString());
                else
                    resolve(null);
            });
        });
    }
    aReadTextOrFail_() {
        return new Promise((resolve, reject) => {
            fs.readFile(this.path$, (err, content) => {
                if (err == null)
                    resolve(content.toString());
                else
                    reject(err);
            });
        });
    }
    aReadLinesOrFail_(code) {
        return __awaiter(this, void 0, void 0, function* () {
            return this.aReadTextOrFail_().then((content) => {
                return Promise.resolve(botcore_1.With.lines_(content, code));
            });
        });
    }
    aRewriteTextOrFail_(code, options) {
        return __awaiter(this, void 0, void 0, function* () {
            return this.aReadTextOrFail_().then((content) => {
                let output = code(content);
                if (output != content) {
                    this.aWriteText_(output, options);
                }
            });
        });
    }
    aRewriteLinesOrFail_(code, options) {
        return __awaiter(this, void 0, void 0, function* () {
            return this.aReadTextOrFail_().then((content) => {
                let output = botcore_1.With.lines_(content, code).join(botcore_1.TextUt.lineSep$);
                if (output != content) {
                    this.aWriteText_(output, options);
                }
            });
        });
    }
    aWriteText_(data, options = {
        encoding: Encoding.utf8$,
        mode: 0o744,
        flag: "w",
    }) {
        return new Promise((resolve, reject) => {
            fs.writeFile(this.path$, data, options, (err) => {
                if (err == null)
                    resolve();
                else
                    reject(err);
            });
        });
    }
    aListOrEmpty_() {
        return new Promise((resolve, reject) => {
            fs.readdir(this.path$, (err, files) => {
                if (err != null)
                    reject(err);
                else
                    resolve(files);
            });
        });
    }
    /// Post order walk of the directory tree under this directory recursively.
    aWalk_(callback) {
        return new Promise((resolve, reject) => __awaiter(this, void 0, void 0, function* () {
            let stat = yield this.aLstat_();
            if (stat == null || !stat.isDirectory) {
                reject(this.path$);
            }
            this.awalk1_((yield this.aListOrEmpty_()).sort(), 0, "", callback, resolve, reject);
        }));
    }
    /// Pre order scan of the directory tree under this directory.
    /// Recurse into a directory only if callback returns true.
    aScan_(callback) {
        return new Promise((resolve, reject) => __awaiter(this, void 0, void 0, function* () {
            let stat = yield this.aLstat_();
            if (stat == null || !stat.isDirectory) {
                reject(this.path$);
            }
            this.ascan1_((yield this.aListOrEmpty_()).sort(), 0, "", callback, resolve, reject);
        }));
    }
    ///////////////////////////////////////////////////////////////
    awalk1_(entries, index, dirpath, callback, resolve, reject) {
        return __awaiter(this, void 0, void 0, function* () {
            if (index >= entries.length) {
                resolve();
                return;
            }
            const dir = this;
            const name = entries[index];
            const file = new Filepath(this.path$, name);
            const filepath = dirpath.length == 0 ? name : dirpath + Path.sep + name;
            const filestat = yield file.aLstat_();
            if (filestat == null) {
                reject(file.path$);
                return;
            }
            if (filestat.isDirectory()) {
                setTimeout(() => __awaiter(this, void 0, void 0, function* () {
                    file.awalk1_((yield file.aListOrEmpty_()).sort(), 0, filepath, callback, () => {
                        callback(file, filepath, filestat, () => {
                            dir.awalk1_(entries, index + 1, dirpath, callback, resolve, reject);
                        });
                    }, reject);
                }), 0);
            }
            else {
                callback(file, filepath, filestat, () => {
                    dir.awalk1_(entries, index + 1, dirpath, callback, resolve, reject);
                });
            }
        });
    }
    ascan1_(entries, index, dirpath, callback, resolve, reject) {
        return __awaiter(this, void 0, void 0, function* () {
            if (index >= entries.length) {
                resolve();
                return;
            }
            const dir = this;
            const name = entries[index];
            const file = new Filepath(this.path$, name);
            const filepath = dirpath.length == 0 ? name : dirpath + Path.sep + name;
            const filestat = yield file.aLstat_();
            if (filestat == null) {
                reject(file.path$);
                return;
            }
            callback(file, filepath, filestat, (recurse) => {
                if (recurse && filestat.isDirectory()) {
                    setTimeout(() => __awaiter(this, void 0, void 0, function* () {
                        file.ascan1_((yield file.aListOrEmpty_()).sort(), 0, filepath, callback, () => {
                            dir.ascan1_(entries, index + 1, dirpath, callback, resolve, reject);
                        }, reject);
                    }), 0);
                }
                else {
                    dir.ascan1_(entries, index + 1, dirpath, callback, resolve, reject);
                }
            });
        });
    }
    acopy1_(dst, src, srcstat, dirmode, done) {
        return __awaiter(this, void 0, void 0, function* () {
            if (srcstat.isDirectory()) {
                dst.mkdirsOrFail_(dirmode);
                done(0);
                return;
            }
            if (srcstat.isFile()) {
                if (dst.isFile$ && (yield dst.aContentEquals_(src))) {
                    done(0);
                    return;
                }
                dst.mkparentOrFail_(dirmode);
                src.aCopyFileAs_(dst, dirmode).then(() => {
                    done(1);
                });
                return;
            }
            done(0);
        });
    }
    walk1_(dir, stat, callback) {
        if (!stat.isDirectory())
            return;
        for (const name of this.listOrEmpty_().sort()) {
            const file = new Filepath(this.path$, name);
            const filepath = dir.length == 0 ? name : dir + Path.sep + name;
            const stat = file.statOrFail$;
            callback(file, filepath, stat);
            file.walk1_(filepath, stat, callback);
        }
    }
    scan1_(dir, stat, callback) {
        if (!stat.isDirectory())
            return;
        for (const name of fs.readdirSync(this.path$).sort()) {
            const file = new Filepath(this.path$, name);
            const filepath = dir.length == 0 ? name : dir + Path.sep + name;
            const stat = file.statOrFail$;
            if (callback(file, filepath, stat))
                file.scan1_(filepath, stat, callback);
        }
    }
}
exports.Filepath = Filepath;
Filepath.DIRMODE = 0o755;
Filepath.FILEMODE = 0o644;
class IOUt {
    static readAndClose_(input, reduce) {
        return __awaiter(this, void 0, void 0, function* () {
            const ret = new Array();
            return new Promise((resolve, reject) => {
                input
                    .on("data", (chunk) => ret.push(chunk))
                    .on("error", (err) => reject(err))
                    .on("end", () => {
                    input.close();
                    resolve(reduce(ret));
                });
            });
        });
    }
    static copyAndClose_(output, input) {
        return __awaiter(this, void 0, void 0, function* () {
            return new Promise((resolve, reject) => {
                input
                    .on("data", (chunk) => output.write(chunk, (err) => reject(err)))
                    .on("error", (err) => reject(err))
                    .on("end", () => {
                    output.close();
                    input.close();
                    resolve();
                });
            });
        });
    }
    /// Like copyAndClose_() but not closing the output stream.
    static copy_(output, input) {
        return __awaiter(this, void 0, void 0, function* () {
            return new Promise((resolve, reject) => {
                input
                    .on("data", (chunk) => output.write(chunk, (err) => reject(err)))
                    .on("error", (err) => reject(err))
                    .on("end", () => {
                    input.close();
                    resolve();
                });
            });
        });
    }
}
exports.IOUt = IOUt;
class Encoding {
}
exports.Encoding = Encoding;
Encoding.ascii$ = "ascii";
Encoding.utf8$ = "utf8";
Encoding.utf16le$ = "utf16le";
Encoding.ucs2$ = "ucs2";
Encoding.binary$ = "binary";
Encoding.latin1$ = "latin1";
Encoding.win1252$ = "win-1252";
Encoding.iso88591$ = "ISO-8859-1";
Encoding.base64$ = "base64";
Encoding.hex$ = "hex";
//# sourceMappingURL=botnode.js.map