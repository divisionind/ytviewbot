#!/bin/sh
mkdir ytviewbot
cp libs/ytviewbot ytviewbot/ytviewbot
cp ../installPackaged ytviewbot/install
cp ../installDepends ytviewbot/installDepends
cp ../uninstall ytviewbot/uninstall
chmod +x ytviewbot/install
chmod +x ytviewbot/installDepends
chmod +x ytviewbot/uninstall
tar -czvf ytviewbot-$1.tar.gz ytviewbot/