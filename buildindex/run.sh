#!/bin/bash
export MAVEN_OPTS=-Xms10000m -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8420
mvn clean clean
mvn jetty:run
