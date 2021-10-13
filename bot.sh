#!/bin/bash

if [ ! -f ./BitHeroes.jar ]; then
  echo 'You are using 99bot from source code version which required to run ./build.sh in order to compile binary first'
  echo 'If you are not a developer and you only want to use the bot, please download the zip file with name download-this-file.zip from my website at download.bh99bot.com'
  exit 1
fi

java -jar BitHeroes.jar $@
exit=$?

if [ $exit -ne 0 ]; then
  echo 'Exit code '$exit
fi
