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
If some people want to help and test this, I'll put a ready to go installer in the releases.
### Installer Features
- Your typical Windows installer and uninstaller
- Silent install possible
- An EPL confirmation box.
- Contains the Cognitect provided ClojureTools + my cmd cli wrapper
- Removes any pre-existing ClojureTools installation
- Checks for java, sends to the openjdk oracle build page if not present.
- Sets the path and PSModulePath, and cleanup these on uninstall
- Enables powershell scripts with a policy of RemoteSigned (required to Invoke-Clojure)
- Adds both Clojure and clj to the start menu (not quite usefull but hey)
- Adds a link to https://clojure.org/ to the start menu
## Install Builder
### Prerequired
- A windows computer
- [Inno setup 6](http://www.jrsoftware.org/isdl.php) must be installed and available in the path.
### Building the installer
The builder will
- download ClojureTools
- download the [exploratory cli wrapper](https://github.com/cark/clojure-win-cli-wrap)
- build the installer executable

First the `install.edn` file (in the project root directory) must be updated with the current versions of ClojureTools and clojure-win-cli-wrap
```clojure
{:wrapper-version "v0.0.4"
 :clojure-tools-version "1.10.1.466"}
```

Then, from the project root, the install builder must be launched:
```
clojure -A:run
```
## Several choices were made 
### Inno setup
We're relying on the Inno setup install builder. While this tool isn't open source, it is full featured, well established and maintained, and has been free for longer than Clojure existed. It allows for GUI or silent installations (/SILENT /VERYSILENT).
The competition in the free area was either very hard to get into (Wix). Or introduced a pretty foreign language (NSIS).
### Install location
The current powershell Clojure installer presents the user with a bewildering list of install paths. In this project, we're removing that choice, prefering to install Clojure in a canonical and Windows idiomatic programfiles\clojure location.
As to the location of the Powershell modules, we're following [Microsoft's advice](https://docs.microsoft.com/en-us/powershell/developer/module/installing-a-powershell-module#installing-modules-in-a-product-directory) and install it in our "product directory".
### Old ClojureTools installations
These are just deleted. It remains an open question if we should preserve the old deps.edn files located in the Powershell module.
We're currently deleting it. I'm of the opinion that it has no business being there in the first place, or at least should not
be the place where users configure their system/user wide deps.edn.
## License
Copyright (c) Sacha De Vos and contributors. All rights reserved.

The use and distribution terms for this software are covered by the Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can be found in the file LICENSE.html at the root of this distribution. By using this software in any fashion, you are agreeing to be bound by the terms of this license. You must not remove this notice, or any other, from this software.

