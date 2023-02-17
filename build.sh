#!/usr/bin/env bash

cd $(dirname "${BASH_SOURCE[0]}")
./gradlew jar
cp app/build/libs/tankPhysics.jar .
