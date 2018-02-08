#!/usr/bin/env bash

BASEDIR=$(dirname "$0")

java -jar "${BASEDIR}/csv-crypt-<VERSION>-standalone.jar" $@
