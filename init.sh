#!/bin/bash
set -x
mkdir -p /tmp/search/log/buildIndex/
mkdir -p /tmp/search/log/indexServer/
mkdir -p /tmp/search/data/
mkdir -p /tmp/search/invert/
# tar -xzvf ./buildindex/src/main/resources/search_data.txt.tar.gz -C /search/data/
CUR_PATH=`pwd`
DATA_PATH=$CUR_PATH"/buildindex/src/main/resources/search_data.txt.tar.gz"
cd  /tmp/search/data/
tar -xzvf $DATA_PATH 
cd $CUR_PATH
mvn clean compile
cd $CUR_PATH"/adminapi/"
mvn clean install
#mvn insatll -pl .
