#!/bin/bash

VERSION=$1

if [[ -z $VERSION ]];
then
  echo 'Specific version'
  exit 1
fi

# Include version info
echo $VERSION > ./src/main/resources/current-version.txt

# Build
./build.sh
exit=$?
if [ $exit -ne 0 ]; then
  echo 'Build failure'
  exit 1
fi

# Check binary after build
BINARY=./target/99bot-$VERSION-jar-with-dependencies.jar
if [ ! -f $BINARY ];
then
  echo 'Binary version '$VERSION' does not exist'
  echo 'File not found: '$BINARY
  exit 1
fi

# Cleanup
rm -rf ./release/

# Remake release directory
mkdir -p ./release/

# Copy binary
cp $BINARY ./release/BitHeroes.jar

# Provide empty properties file, help players override configuration
touch ./release/user-config.properties

cat <<EOF > ./release/user-config.properties
# Screen offset
# from left
#offset.screen.x=0
# from top
#offset.screen.y=57

# Game tokens
#1.game.kong.user.id=
#1.game.kong.user.name=
#1.game.auth.token=

# Configure telegram
#telegram.token=
#telegram.channel-id=
#telegram.instance-id=

# Tolerant
#tolerant.position=40
#tolerant.color=0

# Alter interval between loops of checking images, if you believe your PC is fast, you can use this key for a faster progression. Default interval for most functions is 5 seconds (please see at --help of each function), accepted format is: "number" = number of seconds / "number" + "s" = number of seconds / "number" + "ms" = number of milliseconds, eg: 50ms = loop every 50 milliseconds, or 5000ms equals to 5 seconds
#interval.loop.main=1s

# Google Chrome path, for Windows only
#external.application.chrome.path=C:\\\\Program Files (x86)\\\\Google\\\\Chrome\\\\Application\\\\chrome.exe

# Google Chrome user dir, specify an external directory help you dont need to move the 'chrome-user-dir' folder next time when you update this Bit Heroes bot
#external.mini-client.user.dir=D:\\\\Data\\\\chrome-user-dir

# Disable sub-features
#disable.jansi=true
#disable.auto-update=true
EOF

# Copy files for mini client
cp ./prepare-mini-chrome-client.txt ./release/

# Copy launch scripts for Linux/Mac
cp ./bot.sh ./release/
cp ./client.sh ./release/
cp ./help.sh ./release/
cp ./setting.sh ./release/

# Copy launch scripts for Windows
cp ./_.bat ./release/
cp ./web.bot.bat ./release/
cp ./steam.bot.bat ./release/
cp ./client.bat ./release/
cp ./help.bat ./release/
cp ./setting.bat ./release/
cp ./AFK.steam.bat ./release/
cp ./AFK.web.bat ./release/

# Remove secret
rm -f ./release/bh-client/*.html
rm -f ./release/readonly.*.properties

# Compress output
FILE=download-this-file.zip
rm -f $FILE

DIR=./99bot-v$VERSION
rm -rf $DIR
mv ./release $DIR
zip -r $FILE $DIR

rm -f ./latest.README.md
cp README.md ./latest.README.md

echo 'clean up'
rm -rf $DIR

echo 'Done '$FILE
