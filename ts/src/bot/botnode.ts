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
/// Utilities that works with nodejs.

import { Fun00, Fun10, Fun11, Long, stringX, TextUt, With } from "./botcore";
import fs = require("fs");
import Path = require("path");

export class Basepath {

    private _basepath: Path.ParsedPath;
    private _path: string | null = null;

    constructor(...segments: string[]) {
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

    sibling_(base: string): Basepath {
        return new Basepath(this.dir$, base);
    }

    toString(): string {
        return this.path$;
    }

    /// If dir is null return name. If dir empty return /name. If dir == "." return ./name
    static joinPath(dir: stringX, name: string): string {
        if (dir == null) return name;
        if (name.length == 0) return dir;
        return this.joinPath0(dir, name);
    }

    /// Like joinPath() but return a relative path if directory is null, empty or ".".
    static joinRpath(dir: stringX, name: string): string {
        if (dir == null || dir.length == 0 || dir == ".") return name;
        if (name.length == 0) return dir;
        return this.joinPath0(dir, name);
    }

    /// Like joinPath() but return dir/ if name is empty.
    static joinPath1(dir: stringX, name: string): string {
        if (dir == null) return name;
        return this.joinPath0(dir, name);
    }

    /// Like joinRpath() but return dir/ if name is empty.
    static joinRpath1(dir: stringX, name: string): string {
        if (dir == null || dir.length == 0 || dir == ".") return name;
        return this.joinPath0(dir, name);
    }

    static joinPath0(dir: string, name: string): string {
        if (dir.endsWith(Path.sep) || name.startsWith(Path.sep)) return `${dir}${name}`;
        return `${dir}${Path.sep}${name}`;
    }

    /** This differ from Basepath() in that it returns name as "" if input has trailing /. */
    static splitPath1(path: string): [stringX, string] {
        const cleanpath = Path.normalize(path);
        const index = cleanpath.lastIndexOf(Path.sep);
        if (index < 0) return [null, cleanpath.toString()];
        return [cleanpath.substring(0, index), cleanpath.substring(index + 1)];
    }

    static splitName(name: string): [string, string] {
        const index = name.lastIndexOf('.');
        if (index <= 0) return [name, ""];
        return [name.substring(0, index), name.substring(index + 1)];
    }
}

export type FilepathX = Filepath | null;
export type FilepathXX = Filepath | null | undefined;

export type FileWalkerSyncCallback = (filepath: Filepath, rpath: string, stat: fs.Stats) => void;
export type FileWalkerSyncPredicate = (filepath: Filepath, rpath: string, stat: fs.Stats) => boolean;
export type FileWalkerAsyncCallback = (filepath: Filepath, rpath: string, stat: fs.Stats, done: Fun00) => void;
export type FileWalkerAsyncPredicate = (filepath: Filepath, rpath: string, stat: fs.Stats, done: Fun10<boolean>) => void;

export class Filepath extends Basepath {

    static readonly DIRMODE = 0o755;
    static readonly FILEMODE = 0o644;

    constructor(...segments: string[]) {
        let base: string = Path.sep;
        let path = Path.normalize(segments.join(Path.sep));
        if (!path.startsWith(Path.sep)) {
            base = process.cwd();
            path = Path.normalize(base + Path.sep + path);
        }
        super(path);
    }

    static resolve_(basedir: string, ...segments: string[]): Filepath {
        let path = Path.normalize(segments.join(Path.sep));
        if (!path.startsWith(Path.sep)) {
            path = basedir + Path.sep + path;
        }
        return new Filepath(path);
    }

    static pwd_(): Filepath {
        return new Filepath(process.cwd());
    }

    static aMktmpdir_(prefix: string = "temp"): Promise<Filepath> {
        return new Promise<Filepath>((resolve, reject) => {
            fs.mkdtemp(prefix, async (err, path) => {
                if (err != null) reject(err);
                resolve(new Filepath(path));
            });
        });
    }

    static async aTmpdir_(code: Fun11<Filepath, Promise<void>>): Promise<void> {
        return Filepath.aMktmpdir_().then(async (tmpdir) => {
            await code(tmpdir).finally(async () => {
                await tmpdir.aRmdirTree_();
            });
        });
    }

    static mktmpdir_(prefix: string = "temp"): Filepath {
        return new Filepath(fs.mkdtempSync(prefix));
    }

    static tmpdir_(code: Fun10<Filepath>) {
        const tmpdir = new Filepath(fs.mkdtempSync("temp"));
        try {
            code(tmpdir);
        } finally {
            tmpdir.deleteTree_();
        }
    }

    /// @return An relative path of this file under the given basedir 
    /// or null if this file is not under the given basedir.
    rpathUnder_(basedir: Filepath): string | null {
        let path = this.path$;
        let basepath = basedir.path$ + Path.sep;
        if (path.startsWith(basepath)) {
            return path.substring(basepath.length);
        }
        return null;
    }

    file_(...segments: string[]): Filepath {
        return new Filepath(this.path$, segments.join(Path.sep));
    }

    /// @param base Filename with suffix.
    changeNameWithSuffix_(base: string): Filepath {
        return new Filepath(this.dir$, base);
    }

    /// @param name Filename without suffix.
    changeNameWithoutSuffix_(name: string): Filepath {
        return new Filepath(this.dir$, name + this.suffix$);
    }

    /// @param suffix Filename suffix with ".".
    changeSuffix_(suffix: string): Filepath {
        return new Filepath(this.dir$, this.nameWithoutSuffix$ + suffix);
    }

    toString(): string {
        return this.path$;
    }

    ///////////////////////////////////////////////////////////////

    get parentFilepath$(): Filepath | null {
        const dir = this.dir$;
        return dir == null ? null : new Filepath(dir);
    }

    get exists$(): boolean {
        return this.access_(fs.constants.F_OK);
    }

    get existsOrFail$(): this | never {
        fs.accessSync(this.path$, fs.constants.F_OK);
        return this;
    }

    get canRead$(): boolean {
        return this.access_(fs.constants.R_OK);
    }

    get canWrite$(): boolean {
        return this.access_(fs.constants.W_OK);
    }

    get lstatOrNull$(): fs.Stats | null {
        try {
            return fs.lstatSync(this.path$);
        } catch (_e) {
            return null;
        }
    }

    get lstatOrFail$(): fs.Stats {
        return fs.lstatSync(this.path$);
    }

    get statOrNull$(): fs.Stats | null {
        try {
            return fs.statSync(this.path$);
        } catch (_e) {
            return null;
        }
    }

    get statOrFail$(): fs.Stats {
        return fs.statSync(this.path$);
    }

    get isFile$(): boolean {
        return this.statOrNull$?.isFile() ?? false;
    }

    get isDir$(): boolean {
        return this.statOrNull$?.isDirectory() ?? false;
    }

    get isSymLink$(): boolean {
        return this.lstatOrNull$?.isSymbolicLink() ?? false;
    }

    get isEmptyDir$(): boolean {
        return this.listOrEmpty_().length == 0;
    }

    get lastModified$(): Long {
        const stat = this.statOrNull$;
        if (stat == null) return 0;
        return stat.mtimeMs;
    }

    get lastModifiedOrFail$(): number {
        return this.statOrFail$.mtimeMs;
    }

    get length$(): number {
        return this.statOrNull$?.mtimeMs ?? 0;
    }

    get lengthOrFail$(): number {
        return this.statOrFail$.size;
    }

    ///////////////////////////////////////////////////////////////

    access_(flag: number): boolean {
        try {
            fs.accessSync(this.path$, flag);
            return true;
        } catch (_e) {
            return false;
        }
    }

    isNewerThan_(other: Filepath): boolean {
        return this.lastModifiedOrFail$ > other.length$;
    }

    /// Remove this file if this is a file.
    delete_(): boolean {
        try {
            this.deleteOrFail_();
            return true;
        } catch (e) {
            return false;
        }
    }

    deleteOrFail_(): this {
        if (fs.existsSync(this.path$)) {
            fs.unlinkSync(this.path$);
        }
        return this;
    }

    /// Remove this directory if it is an empty directory.
    deleteDir_(): boolean {
        try {
            fs.rmdirSync(this.path$);
            return true;
        } catch (e) {
            return false;
        }
    }

    deleteDirOrFail_(): this {
        if (fs.existsSync(this.path$)) {
            fs.rmdirSync(this.path$);
        }
        return this;
    }

    /// Remove everything under this directory.
    deleteSubtrees_(): boolean {
        let ret = true;
        for (const name of this.listOrEmpty_()) {
            const file = this.file_(name);
            const stat = file.statOrFail$;
            if (stat.isFile()) {
                if (!file.delete_()) ret = false;
            } else if (stat.isDirectory()) {
                if (!file.deleteSubtrees_()) ret = false;
                if (!file.deleteDir_()) ret = false;
            }
        }
        return ret;
    }

    deleteSubtreesOrFail_(): this {
        if (!this.deleteSubtrees_()) throw this.path$;
        return this;
    }

    /// Remove this directory and everything under this directory.
    deleteTree_(): boolean {
        if (!this.deleteSubtrees_()) return false;
        if (!this.deleteDir_()) return false;
        return true;
    }

    deleteTreeOrFail_(): this {
        if (!this.deleteTree_()) throw this.path$;
        return this;
    }

    rename_(to: string | Filepath): boolean {
        const tofile = (to instanceof Filepath) ? to : new Filepath(to);
        try {
            fs.renameSync(this.path$, tofile.path$);
            return true;
        } catch (e: any) {
            return false;
        }
    }

    copyFileAs_(tofile: string | Filepath, dirmode: number = Filepath.DIRMODE) {
        const tofilepath = (tofile instanceof Filepath) ? tofile : new Filepath(tofile);
        tofilepath.mkparentOrFail_(dirmode);
        fs.copyFileSync(this.path$, tofilepath.path$);
    }

    copyFileToDir_(todir: string | Filepath, dirmode: number = Filepath.DIRMODE) {
        const dstdir = (todir instanceof Filepath) ? todir : new Filepath(todir);
        dstdir.mkdirsOrFail_(dirmode);
        fs.copyFileSync(this.path$, dstdir.file_(this.nameWithSuffix$).path$);
    }

    /// @return Number of files, not including directories, copied.
    copyDirToDir_(
        todir: string | Filepath,
        accept: FileWalkerSyncPredicate | null = null,
        dirmode: number = Filepath.DIRMODE
    ): number {
        let ret = 0;
        const dstdir = (todir instanceof Filepath) ? todir : new Filepath(todir);
        this.walk_((src, rpath, stat) => {
            if (accept != null && !accept(src, rpath, stat)) return;
            const dst = dstdir.file_(rpath);
            if (stat.isFile()) {
                dst.mkparentOrFail_(dirmode);
                fs.copyFileSync(src.path$, dst.path$);
                ++ret;
            } else if (stat.isDirectory()) {
                dst.mkdirsOrFail_(dirmode);
            }
        });
        return ret;
    }

    readBytes_(): Buffer {
        return fs.readFileSync(this.path$);
    }

    readText_(encoding: string = Encoding.utf8$): string {
        return fs.readFileSync(this.path$).toString(encoding);
    }

    differ_(other: Filepath): boolean {
        return fs.readFileSync(this.path$).compare(fs.readFileSync(other.path$)) != 0;
    }

    writeText_(data: string, options?: fs.WriteFileOptions) {
        fs.writeFileSync(this.path$, data, options);
    }

    writeData_(data: any, options?: fs.WriteFileOptions) {
        fs.writeFileSync(this.path$, data, options);
    }

    getOutputStream_(options?: string | {
        flags?: string;
        encoding?: string;
        fd?: number;
        mode?: number;
        autoClose?: boolean;
        emitClose?: boolean;
        start?: number;
        highWaterMark?: number;
    }): fs.WriteStream {
        return fs.createWriteStream(this.path$, options);
    }

    getInputStream_(options?: string | {
        flags?: string;
        encoding?: string;
        fd?: number;
        mode?: number;
        autoClose?: boolean;
        emitClose?: boolean;
        start?: number;
        end?: number;
        highWaterMark?: number;
    }): fs.ReadStream {
        return fs.createReadStream(this.path$, options);
    }

    chmod_(mode: string | number): boolean {
        try {
            fs.chmodSync(this.path$, mode);;
            return true;
        } catch (_e) {
            return false;
        }
    }

    chmodOrFail_(mode: string | number): this {
        fs.chmodSync(this.path$, mode);
        return this;
    }

    /// @param time in sec.
    setLastModified_(time: number | string | Date): boolean {
        try {
            fs.utimesSync(this.path$, time, time);
            return true;
        } catch (e) {
            return false;
        }
    }

    setLastModifiedOrFail_(time: number | string | Date): this {
        fs.utimesSync(this.path$, time, time);
        return this;
    }

    /// @return true if directory already exists or created.
    mkdirs_(mode: number = Filepath.DIRMODE): boolean {
        if (this.exists$) return true;
        try {
            fs.mkdirSync(this.path$, { recursive: true, mode: mode });
            return true;
        } catch (_e) {
            return false;
        }
    }

    /// @return true if parent directory already exists or created.
    mkparent_(mode: number = Filepath.DIRMODE): boolean {
        return new Filepath(this.dir$).mkdirs_(mode);
    }

    /// @return Ths file if directory already exists or created, otherwise throw an exception.
    mkdirsOrFail_(mode: number = Filepath.DIRMODE): this | never {
        if (this.exists$) return this;
        fs.mkdirSync(this.path$, { recursive: true, mode: mode });
        return this.existsOrFail$;
    }

    /// @return This file if parent directory already exists or created, otherwse throw an exception.
    mkparentOrFail_(mode: number = Filepath.DIRMODE): this | never {
        new Filepath(this.dir$).mkdirsOrFail_(mode);
        return this;
    }

    listOrEmpty_(): string[] {
        try {
            return fs.readdirSync(this.path$);
        } catch (e) {
            return [];
        }
    }

    walk_(callback: FileWalkerSyncCallback) {
        this.walk1_("", this.statOrFail$, callback);
    }

    scan_(callback: FileWalkerSyncPredicate) {
        this.scan1_("", this.statOrFail$, callback);
    }

    ///////////////////////////////////////////////////////////////

    async aStat_(): Promise<fs.Stats | null> {
        return new Promise((resolve, _reject) => {
            fs.stat(this.path$, (err, stat) => {
                if (err) resolve(null);
                else resolve(stat);
            });
        });
    }

    async aLstat_(): Promise<fs.Stats | null> {
        return new Promise((resolve, _reject) => {
            fs.lstat(this.path$, (err, stat) => {
                if (err) resolve(null);
                else resolve(stat);
            });
        });
    }

    /// Remove everything under this directory.
    aRmdirSubtrees_(): Promise<boolean> {
        let ret = true;
        return this.aWalk_((file, _rpath, stat, done) => {
            let ok = (ok: boolean) => {
                if (!ok) ret = false;
                done();
            };
            if (stat.isFile()) {
                ok(file.delete_());
            } else {
                ok(file.deleteDir_());
            }
        }).then(() => {
            return ret;
        });
    }

    /// Remove this directory and everything under this directory.
    aRmdirTree_(): Promise<boolean> {
        return this.aRmdirSubtrees_().then((ok) => {
            if (!ok) return false;
            return this.deleteDir_();
        });
    }

    aCopyFileAs_(
        tofile: string | Filepath,
        dirmode: number = Filepath.DIRMODE
    ): Promise<void> {
        return new Promise((resolve, reject) => {
            const tofilepath = (tofile instanceof Filepath) ? tofile : new Filepath(tofile);
            tofilepath.mkparentOrFail_(dirmode);
            fs.copyFile(this.path$, tofilepath.path$, (err) => {
                if (err == null) resolve(); else reject(err);
            });
        });
    }

    aCopyFileToDir_(
        todir: string | Filepath,
        dirmode: number = Filepath.DIRMODE
    ): Promise<void> {
        return new Promise((resolve, reject) => {
            const dstdir = (todir instanceof Filepath) ? todir : new Filepath(todir);
            fs.copyFile(this.path$, dstdir.mkdirsOrFail_(dirmode).file_(this.nameWithSuffix$).path$, (err) => {
                if (err == null) resolve(); else reject(err);
            });
        });
    }

    /// @return Number of files, not including directories, copied.
    aCopyDirToDir_(
        todir: string | Filepath,
        accept: FileWalkerAsyncPredicate | null = null,
        dirmode: number = Filepath.DIRMODE
    ): Promise<number> {
        const dstdir = (todir instanceof Filepath) ? todir : new Filepath(todir);
        let ret = 0;
        return this.aWalk_((src, rpath, stat, done) => {
            let copy1 = (yes: boolean) => {
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
            } else {
                accept(src, rpath, stat, copy1);
            }
        }).then(() => {
            return ret;
        });
    }

    /// Update file/directory under this directory from another directory if file differ.
    /// @return [copied, missing] Number of files, not including directories, copied 
    /// and number of files missing at the other directory.
    aUpdateDirFromDir_(
        srcdir: string | Filepath,
        accept: FileWalkerAsyncPredicate | null = null,
        dirmode: number = Filepath.DIRMODE
    ): Promise<[number, number]> {
        const fromdir = (srcdir instanceof Filepath) ? srcdir : new Filepath(srcdir);
        let copied = 0;
        let missing = 0;
        return this.aWalk_((file, rpath, stat, done) => {
            let copy1 = async (yes: boolean) => {
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
            };
            if (accept == null) {
                copy1(true);
            } else {
                accept(file, rpath, stat, copy1);
            }
        }).then(() => {
            return [copied, missing];
        });
    }

    aContentEquals_(another: Filepath): Promise<boolean> {
        return this.aReadFileOrFail_().then((a) => {
            return another.aReadFileOrFail_().then((b) => {
                return a.equals(b);
            });
        });
    }

    aReadFileOrFail_(): Promise<Buffer> {
        return new Promise<Buffer>((resolve, reject) => {
            fs.readFile(this.path$, (err, content) => {
                if (err == null) resolve(content); else reject(err);
            });
        });
    }

    aWriteFileOrFail_(data: any, options: fs.WriteFileOptions): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            fs.writeFile(this.path$, data, options, (err) => {
                if (err == null) resolve(); else reject(err);
            });
        });
    }

    aReadTextOrNull_(): Promise<string | null> {
        return new Promise<string | null>((resolve, _reject) => {
            fs.readFile(this.path$, (err, content) => {
                if (err == null) resolve(content.toString()); else resolve(null);
            });
        });
    }

    aReadTextOrFail_(): Promise<string> | never {
        return new Promise<string>((resolve, reject) => {
            fs.readFile(this.path$, (err, content) => {
                if (err == null) resolve(content.toString()); else reject(err);
            });
        });
    }

    async aReadLinesOrFail_(code: Fun11<string, string | null>): Promise<string[]> {
        return this.aReadTextOrFail_().then((content) => {
            return Promise.resolve(With.lines_(content, code));
        });
    }

    async aRewriteTextOrFail_(code: Fun11<string, string | null>, options?: fs.WriteFileOptions): Promise<void> {
        return this.aReadTextOrFail_().then((content) => {
            let output = code(content);
            if (output != content) {
                this.aWriteText_(output!, options);
            }
        });
    }

    async aRewriteLinesOrFail_(code: Fun11<string, string | null>, options?: fs.WriteFileOptions): Promise<void> {
        return this.aReadTextOrFail_().then((content) => {
            let output = With.lines_(content, code).join(TextUt.lineSep$);
            if (output != content) {
                this.aWriteText_(output, options);
            }
        });
    }

    aWriteText_(data: string, options: fs.WriteFileOptions = {
        encoding: Encoding.utf8$,
        mode: 0o744,
        flag: "w",
    }): Promise<void> | never {
        return new Promise<void>((resolve, reject) => {
            fs.writeFile(this.path$, data, options, (err) => {
                if (err == null) resolve(); else reject(err);
            });
        });
    }

    aListOrEmpty_(): Promise<string[]> {
        return new Promise((resolve, reject) => {
            fs.readdir(this.path$, (err, files) => {
                if (err != null) reject(err); else resolve(files);
            });
        });
    }

    /// Post order walk of the directory tree under this directory recursively.
    aWalk_(callback: FileWalkerAsyncCallback): Promise<void> {
        return new Promise(async (resolve, reject) => {
            let stat = await this.aLstat_();
            if (stat == null || !stat.isDirectory) {
                reject(this.path$);
            }
            this.awalk1_((await this.aListOrEmpty_()).sort(), 0, "", callback, resolve, reject);
        });
    }

    /// Pre order scan of the directory tree under this directory.
    /// Recurse into a directory only if callback returns true.
    aScan_(callback: FileWalkerAsyncPredicate): Promise<void> {
        return new Promise(async (resolve, reject) => {
            let stat = await this.aLstat_();
            if (stat == null || !stat.isDirectory) {
                reject(this.path$);
            }
            this.ascan1_((await this.aListOrEmpty_()).sort(), 0, "", callback, resolve, reject);
        });
    }

    ///////////////////////////////////////////////////////////////

    private async awalk1_(
        entries: string[],
        index: number,
        dirpath: string,
        callback: FileWalkerAsyncCallback,
        resolve: Fun00,
        reject: Fun10<string>
    ) {
        if (index >= entries.length) {
            resolve();
            return;
        }
        const dir = this;
        const name = entries[index];
        const file = new Filepath(this.path$, name);
        const filepath = dirpath.length == 0 ? name : dirpath + Path.sep + name;
        const filestat = await file.aLstat_();
        if (filestat == null) {
            reject(file.path$);
            return;
        }
        if (filestat.isDirectory()) {
            setTimeout(async () => {
                file.awalk1_((await file.aListOrEmpty_()).sort(), 0, filepath, callback, () => {
                    callback(file, filepath, filestat, () => {
                        dir.awalk1_(entries, index + 1, dirpath, callback, resolve, reject);
                    });
                }, reject);
            }, 0);
        } else {
            callback(file, filepath, filestat, () => {
                dir.awalk1_(entries, index + 1, dirpath, callback, resolve, reject);
            });
        }
    }

    private async ascan1_(
        entries: string[],
        index: number,
        dirpath: string,
        callback: FileWalkerAsyncPredicate,
        resolve: Fun00,
        reject: Fun10<string>
    ) {
        if (index >= entries.length) {
            resolve();
            return;
        }
        const dir = this;
        const name = entries[index];
        const file = new Filepath(this.path$, name);
        const filepath = dirpath.length == 0 ? name : dirpath + Path.sep + name;
        const filestat = await file.aLstat_();
        if (filestat == null) {
            reject(file.path$);
            return;
        }
        callback(file, filepath, filestat, (recurse) => {
            if (recurse && filestat.isDirectory()) {
                setTimeout(async () => {
                    file.ascan1_((await file.aListOrEmpty_()).sort(), 0, filepath, callback, () => {
                        dir.ascan1_(entries, index + 1, dirpath, callback, resolve, reject);
                    }, reject);
                }, 0);
            } else {
                dir.ascan1_(entries, index + 1, dirpath, callback, resolve, reject);
            }
        });
    }

    private async acopy1_(dst: Filepath, src: Filepath, srcstat: fs.Stats, dirmode: number, done: Fun10<number>) {
        if (srcstat.isDirectory()) {
            dst.mkdirsOrFail_(dirmode);
            done(0);
            return;
        }
        if (srcstat.isFile()) {
            if (dst.isFile$ && await dst.aContentEquals_(src)) {
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
    }

    private walk1_(dir: string, stat: fs.Stats, callback: FileWalkerSyncCallback) {
        if (!stat.isDirectory()) return;
        for (const name of this.listOrEmpty_().sort()) {
            const file = new Filepath(this.path$, name);
            const filepath = dir.length == 0 ? name : dir + Path.sep + name;
            const stat = file.statOrFail$;
            callback(file, filepath, stat);
            file.walk1_(filepath, stat, callback);
        }
    }

    private scan1_(dir: string, stat: fs.Stats, callback: (filepath: Filepath, rpath: string, stat: fs.Stats) => boolean) {
        if (!stat.isDirectory()) return;
        for (const name of fs.readdirSync(this.path$).sort()) {
            const file = new Filepath(this.path$, name);
            const filepath = dir.length == 0 ? name : dir + Path.sep + name;
            const stat = file.statOrFail$;
            if (callback(file, filepath, stat)) file.scan1_(filepath, stat, callback);
        }
    }
}

export abstract class IOUt {
    static async readAndClose_<T>(input: fs.ReadStream, reduce: Fun11<T[], T>): Promise<T> {
        const ret = new Array<T>();
        return new Promise((resolve, reject) => {
            (input as fs.ReadStream)
                .on("data", (chunk: T) => ret.push(chunk))
                .on("error", (err: Error) => reject(err))
                .on("end", () => {
                    input.close();
                    resolve(reduce(ret));
                });
        });
    }

    static async copyAndClose_<T>(output: fs.WriteStream, input: fs.ReadStream): Promise<void> {
        return new Promise((resolve, reject) => {
            (input as fs.ReadStream)
                .on("data", (chunk: T) => (output as fs.WriteStream).write(chunk, (err) => reject(err)))
                .on("error", (err: Error) => reject(err))
                .on("end", () => {
                    output.close();
                    input.close();
                    resolve();
                });
        });
    }

    /// Like copyAndClose_() but not closing the output stream.
    static async copy_<T>(output: fs.WriteStream, input: fs.ReadStream): Promise<void> {
        return new Promise((resolve, reject) => {
            (input as fs.ReadStream)
                .on("data", (chunk: T) => (output as fs.WriteStream).write(chunk, (err) => reject(err)))
                .on("error", (err: Error) => reject(err))
                .on("end", () => {
                    input.close();
                    resolve();
                });
        });
    }
}

export class Encoding {
    static ascii$ = "ascii";
    static utf8$ = "utf8";
    static utf16le$ = "utf16le";
    static ucs2$ = "ucs2";
    static binary$ = "binary";
    static latin1$ = "latin1";
    static win1252$ = "win-1252";
    static iso88591$ = "ISO-8859-1";
    static base64$ = "base64";
    static hex$ = "hex";
}
