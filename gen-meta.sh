#!/bin/bash

/opt/app/apache-maven-3.6.3/bin/mvn clean package
rm -f ./BitHeroes.jar
cp ./target/99bot-*-jar-with-dependencies.jar ./BitHeroes.jar

java -jar BitHeroes.jar gen-meta $@
