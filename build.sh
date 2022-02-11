#!/bin/bash

if [ ! -f ./pom.xml ]; then
  echo 'Wrong working directory'
  exit 2
fi

unameOut="$(uname -s)"
case "${unameOut}" in
    Linux*)     machine=Linux;;
    Darwin*)    machine=Mac;;
    CYGWIN*)    machine=Cygwin;;
    MINGW*)     machine=MinGw;;
    *)          machine="UNKNOWN:${unameOut}"
esac
echo ${machine}

if [ "$machine" = "Mac" ]
then
	mvn clean package
else
	/opt/app/apache-maven-3.6.3/bin/mvn clean package
fi

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
