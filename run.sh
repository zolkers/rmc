#!/bin/bash
# Script pour compiler et lancer l'application avec un vrai terminal

echo "Building shadow JAR..."
./gradlew shadowJar -q

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo "Starting application..."
java -Djava.library.path="natives" \
     --enable-native-access=ALL-UNNAMED \
     -jar build/libs/RiegeTerminal-1.21.4.jar
