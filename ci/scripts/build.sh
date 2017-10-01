#!/usr/bin/env bash

set -e -u -x

cd src/main/resources/static && npm install && cd -
mvn -DskipTests clean package
