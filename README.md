# C+edition for Desktop, Community Edition #

## Introduction ##
**C+edition** is an offline notes taking app built on web technology. It comes with HTML and CSS editor, templates and file manager to help you create rich documents in HTML/CSS along with images, audio and video files and keep them organized. This is a desktop version that is released as an open source project.

>*NOTE: The project is currently developed and tested to work in Ubuntu 22.04 only. It may works in other Linux distributions, but keep your fingers crossed.*

## Build Instruction ##
The application use `Kotlin` for the server side and `Electron / Typescript / Javascript / HTML / CSS` at the client side.

The server side code in the `src/` directory comes with the `build.gradle` file and you can build it using `Idea` or `gradle` at the project directory.
```
    gradle PublishToMavenLocal
```
and then copy the result `cplusedition-desktop-ce-<version>.jar` file to `ROOT/lib/`.

The client side code is in `ROOT/assets/js/` and the `ts/` directories. Compile the Typescript code with `VS Code` or simply `tsc` at the `ts/` directory:
```
   tsc --build .
``` 
The client is bridged to the server through Unix socket using a small JNI library in `unixsocket-cc/`. To build the library, you need `autoreconf` and `make`, simply run the `reconfig.sh` script in the `scripts/` directory:
```
    bash scripts/reconfig.sh
```
and then copy the result library `libunixsocket-cc.so` to `ROOT/lib/`.

## Running ##
The `ROOT/` directory contains the files to run the application. `ROOT/lib/` contains the prebuilt libraries for the server and `ROOT/assets/js/` contains the client side code. You need the `java` binary from `JDK11` to run the server and the `electron` binary from `Electron v18+` to run the client. Unpack the `Electron` distribution to the `bin/electron/` directory and make sure java is in your executable `PATH`, then at the project directory run:
```
   bash scripts/start.sh
```
would start the application. The script should shutdown the server when the client exit. If it does not, run:
```
   bash scripts/shutdown.sh
```
to shutdown the server. While the application is running, you may bring up the `Developer Tools` at the Electron client by `Ctrl+Shift+i`.  See documents at `assets/manual/` to get started.

> *NOTE: By default the password to unlock the keystore is stored at `ROOT/changeit.txt`*
> *and document data at `ROOT/Home/` is stored in plain text. You should put the app and all its data in an encrypted disk if necessary.*

## License ##
Fonts under `assets/fonts/` are distributed under their own free licenses, see `assets/manual/credits/credits.html` for details.
Documentation under `assets/manual/` is released under CC BY-NC 4.0 license.
The code is released under Apache License 2.0, see [`NOTICE`](NOTICE) and [`LICENSE`](LICENSE).

Copyright (c) Cplusedition Limited. All rights reserved.