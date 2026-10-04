#!/bin/sh
# Нужна Java 21 и gradle wrapper в каждой папке (см. README.txt)
set -e
(cd fabric && ./gradlew build)
(cd forge  && ./gradlew build)
mkdir -p jars
cp fabric/build/libs/livecamera-1.0.0.jar jars/livecamera-fabric-1.0.0.jar
cp forge/build/libs/livecamera-forge-1.0.0.jar jars/livecamera-forge-1.0.0.jar
echo "Готово: папка jars/"
