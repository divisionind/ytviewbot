#!/bin/sh
mkdir ytviewbot
cp libs/ytviewbot ytviewbot/ytviewbot
cp ../installPackaged ytviewbot/install
cp ../installDepends ytviewbot/installDepends
chmod +x ytviewbot/install
chmod +x ytviewbot/installDepends
tar -czvf ytviewbot-$1.tar.gz ytviewbot/