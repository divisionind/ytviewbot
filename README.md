# YTViewBot - just a YouTube view bot

## Installation
_Note: This project only supports Linux and is completely headless._

#### For dummies - installing for use from CLI
1. Clone repo and enter it `git clone http://192.168.0.2:3000/ahoward/ytviewbot.git && cd ytviewbot`
1. Run `sudo ./install`. If you get an error, change the permissions with `sudo chmod +x install`
2. Enjoy! The command is `ytviewbot`

#### Advanced - portable use
1. Install prerequisites `sudo apt update && sudo apt install tor openjdk-8-jre firefox-esr`
2. Clone repo and enter it `git clone http://192.168.0.2:3000/ahoward/ytviewbot.git && cd ytviewbot`
3. Build the project `./gradlew linux` if you get an error, change the permissions with `sudo chmod +x gradlew` and try again.
4. Download the gecko driver `wget https://github.com/mozilla/geckodriver/releases/download/v0.24.0/geckodriver-v0.24.0-linux64.tar.gz`
5. Extract the tarball `sudo tar -x geckodriver -zf geckodriver-v0.24.0-linux64.tar.gz -O > /usr/bin/geckodriver`
6. Give execute permission `sudo chmod +x /usr/bin/geckodriver`
7. Remove tarball `rm geckodriver-v0.24.0-linux64.tar.gz`
8. Now run `./build/libs/ytviewbot` for further help.

You may skip steps 1 and 4-7 by running `sudo ./installDepends`

### Using Windows?
No sweat. You can run Linux on Windows. Just look up how to install the Windows Subsystem for Linux (WSL).

### Using Mac?
Use a better OS.

## Support
If you like the project, please share the wealth. I'm poor. I need money. Pls help.

- BTC: 1FpywKn3H2CrGUR1tziq5wjhwLeXHSet9C
- BCH: bitcoincash:qz32f4h83dn9fpju594eafm4hytr528l4c4utgyw66