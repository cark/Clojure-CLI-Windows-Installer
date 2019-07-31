# Clojure CLI Windows Installer
## What is it ?
This unofficial project aims at facilitating the installation process for clojure on windows. 
## Project goal
My goal is to have this or something like it be adopted by Cognitect for the windows clojure distribution.
## Included
There are two part to this. 
- the actual installer
- building the installer
## The installer itself
If some people want to help and test this, I'll put a ready to go installer in the [releases](https://github.com/cark/Clojure-CLI-Windows-Installer/releases).
### Installer Features
- Your typical Windows installer and uninstaller
- Silent install possible
- An EPL confirmation box.
- Contains exerything needed to use Clojure with deps.edn
- Checks for java, sends to the openjdk oracle build page if not present.
- Adds both Clojure and clj to the start menu (not quite usefull but hey)
- Adds a link to https://clojure.org/ to the start menu
## Install Builder
### Prerequired
- A windows computer
- [Nullsoft Scriptable Install System](https://nsis.sourceforge.io/Main_Page) must be installed and available in the path.
### Building the installer
The builder will
- download the [Clojure-cli-portable](https://github.com/cark/clojure-cli-portable) release
- build the installer executable

First the `install.edn` file (in the project root directory) must be updated with the current versions of ClojureTools and clojure-win-cli-wrap
```clojure
{:portable-cli-version "0.0.3"
 :clojure-version "1.10.1.466"}
```

Then, from the project root, the install builder must be launched:
```
clojure -A:run
```
## Several choices were made 
### Inno setup
We're relying on the NSIS install builder. Because it may be built on linux and produce windows installers from there. It allows for GUI or silent installations (/S).
### Install location
The current powershell Clojure installer presents the user with a bewildering list of install paths. In this project, we're removing that choice, prefering to install Clojure in a canonical and Windows idiomatic programfiles\clojure location.
This version does not include the official Powershell modules, but replaces these instead with a binary implementation.
### Old ClojureTools installations
You're advised to uninstall previously existing clojure CLIs
## License
Copyright (c) Sacha De Vos and contributors. All rights reserved.

The use and distribution terms for this software are covered by the Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can be found in the file LICENSE.html at the root of this distribution. By using this software in any fashion, you are agreeing to be bound by the terms of this license. You must not remove this notice, or any other, from this software.

