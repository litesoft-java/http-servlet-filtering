#!/bin/bash
if [ ! -f ./pom.xml ]
then
    echo "No pom file: ./pom.xml"
    exit 1
fi
if [ ! -f ./target/http-servlet-filtering.jar ]
then
    echo "No jar file: ./target/http-servlet-filtering.jar"
    exit 2
fi
if [ ! -d ./artifacts ]
then
    mkdir artifacts
fi
cp target/http-servlet-filtering.jar artifacts/http-servlet-filtering.jar
cp pom.xml artifacts/pom.xml