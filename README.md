# Clojure Windows CLI installer
## What is it ?
This project aims at facilitating the installation process for clojure on windows.
## Project goal
My goal is to have this or something like it be adopted by Cognitect for the windows clojure distribution.
## Several choices were made 
### Inno setup
We're relying on the Inno setup install builder. While this tool isn't open source, it is free, full featured, well maintained and has been free for longer than clojure existed. It allows for GUI or silent installations (/SILENT /VERYSILENT).
The competition in the free area was either very hard to get into (Wix). Or introduced a pretty foreign language (NSIS).
### Install location
The current clojure installer leaves that choice to the user, a bewildering list of install paths to the uninitiated user.
In this project, we're removing that choice, prefering to install clojure in a canonical and idiomatic programfiles\clojure location.
As to the location of the powershell modules, we're following [Microsoft's advice](https://docs.microsoft.com/en-us/powershell/developer/module/installing-a-powershell-module#installing-modules-in-a-product-directory) and install it in our "product directory".
### Old ClojureTools installations
These are just deleted. It remains an open question if we should preserve the old deps.edn files located in the powershell module.
We're currently deleting it. I'm of the opinion that it has no business being there in the first place, or at least should not
be the place where we configure our system/user wide deps.edn.
## Include
There are two part to this. 
- building the installer
- the actual installer
## Pre requesits
- [Inno setup 6](http://www.jrsoftware.org/isdl.php) must be installed and available in the path.
## Building the installer
This clojure project will 
- download ClojureTools
- download the [exploratory cli wrapper](https://github.com/cark/clojure-win-cli-wrap)
- build the installer executable

First the `install.edn` file must be updated with the current versions of ClojureTools and clojure-win-cli-wrap
```clojure
{:wrapper-version "v0.0.4"
 :clojure-tools-version "1.10.1.466"}
```

Then, from the project root, the install builder must be launched:
```
clojure -A:run
```
## The installer itself
### Installer Features
- Your typical Windows installer and uninstaller
- Silent install possible
- An EPL confirmation box.
- Contains the Cognitect provided ClojureTools + my cmd cli wrapper
- Removes any pre-existing ClojureTools installation
- Checks for java, sends to the openjdk oracle build page if not.
- Sets the path and PSModulePath, and cleanup these on unistall
- Enables powershell scripts with a policy of RemoteSigned
# License
Copyright (c) Sacha De Vos and contributors. All rights reserved.

The use and distribution terms for this software are covered by the Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can be found in the file LICENSE.html at the root of this distribution. By using this software in any fashion, you are agreeing to be bound by the terms of this license. You must not remove this notice, or any other, from this software.

