#!/bin/bash

if [ ! -f ./pom.xml ]; then
  echo 'Wrong working directory'
  exit 2
fi

/opt/app/apache-maven-3.6.3/bin/mvn clean package
EXIT=$?

if [ $EXIT -ne 0 ]; then
  echo '** ERROR ** maven build failure'
  exit 1
fi

mkdir -p ./out/
rm -f ./BitHeroes.jar
cp ./target/99bot-*-jar-with-dependencies.jar ./BitHeroes.jar
touch user-config.properties
chmod +x ./*.sh

if [ ! -f ./im.dev ]; then
  # Generating mini client
  rm -f ./mini-game-on-chrome*.sh
  rm -f mini
  java -jar ./BitHeroes.jar client
  chmod +x ./mini-game-on-chrome*.sh
fi