#!/bin/bash

if [ ! -f ./pom.xml ]; then
  echo 'Wrong working directory'
  exit 1
fi

/opt/app/apache-maven-3.6.3/bin/mvn clean package
mkdir -p ./out/
rm -f ./BitHeroes.jar
cp ./target/BitHeroes-*-jar-with-dependencies.jar ./BitHeroes.jar
touch user-config.properties
chmod +x ./*.sh

# Generating mini client
rm -f ./mini-game-on-chrome*.sh
rm -f mini
java -jar ./BitHeroes.jar client
chmod +x ./mini-game-on-chrome*.sh
