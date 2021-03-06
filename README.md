# YTViewBot - just a YouTube view bot
[![](https://img.shields.io/travis/divisionind/ytviewbot/master.svg?style=flat-square)](https://travis-ci.org/divisionind/ytviewbot)
![](https://img.shields.io/badge/platform-Linux-blue.svg?style=flat-square)
![](https://img.shields.io/badge/license-GPLv3-green.svg?style=flat-square)
![](https://img.shields.io/badge/dev%20status-inactive-red.svg?style=flat-square)
![](https://img.shields.io/github/repo-size/divisionind/ytviewbot.svg?style=flat-square)

## Depreciated
This project was just a little fun to reverse how YouTube signifies a "view". However,
the overhead of running selenium instances, or any virtual browser process for that matter, 
is extreme given the amount of views these processes generate. The only practical method for 
view botting on modern YouTube is to:
a) reverse how a "view" is calculated 
b) only process these bare minimum view requirements

Furthurmore one would have to continually update such a bot to keep up with the changing 
view requirements put out by YouTube.

## Installation / Building
_Note: This project only supports Linux and is completely headless._

#### For dummies - installing from releases
1. Download the [latest version](https://github.com/divisionind/ytviewbot/releases/latest)
2. Extract the tarball with `tar -xzf ytviewbot-VERSION.tar.gz` and enter it `cd ytviewbot`
3. Install with `chmod +x install && sudo ./install`
4. Now run `ytviewbot` for further help.

#### Advanced - building from source
1. Clone repo and enter it `git clone https://github.com/divisionind/ytviewbot.git && cd ytviewbot`
2. Install prerequisites `chmod +x installDepends && sudo ./installDepends`
3. Build the project `./gradlew linux`
4. Now run `./build/libs/ytviewbot` for further help.

##### You can also choose to install from source
1. Clone repo and enter it `git clone https://github.com/divisionind/ytviewbot.git && cd ytviewbot`
2. Install `chmod +x install && sudo ./install`. This will install all dependencies, build the source, and install ytviewbot.
3. Now run `ytviewbot` for further help.

##### Uninstalling
You can uninstall ytviewbot by running the uninstall script located in either
the root of the github repo or packaged along with every release. `sudo ./uninstall`

### Using Windows?
No sweat. You can run Linux on Windows. Just look up how to install the Windows Subsystem for Linux (WSL).

### Using Mac?
Use a better OS.

## Donate
If you like the project, please share the wealth. I'm poor. I need money. Pls help.

- XMR: `83vzgeeKebLh6pj2YtBqn7PqxY47CkyzmLzUhmHfhTCQdj9Mfad4FUF12Yu9ry5uUh5JASTcXg5Fwji5ibjUngw9LomnH6Z`
- ETH: `0x1bdA7dB6484802DFf4945edc52363B4A8FAcb470`
- ETC: `0x4a368bb4cd854f650169ce207268c303ffecafb2`

## Contributing
All contributions must follow 
[Java's standard code conventions.](https://www.oracle.com/technetwork/java/codeconventions-150003.pdf) 
Failure to comply to these conventions will result in the denial of your contribution, regardless 
of the final functioning of the code.
