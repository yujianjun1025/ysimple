#!/bin/bash
export MAVEN_OPTS=-Xms10000m 
mvn clean clean
mvn jetty:run
