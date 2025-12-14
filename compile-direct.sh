#!/bin/bash
# Direct compilation using Windows JDK

set -e

JAVA_HOME="/mnt/c/Program Files/Java/jdk-25"
JAVAC="$JAVA_HOME/bin/javac.exe"
JAVA="$JAVA_HOME/bin/java.exe"

# Create build directory
mkdir -p build/classes/java/main

echo "Compiling with Windows JDK..."
echo ""

# Find all Java files and compile
find src -name "*.java" -type f > sources.txt

"$JAVAC" \
    --enable-preview \
    --release 25 \
    -d build/classes/java/main \
    -encoding UTF-8 \
    @sources.txt

rm sources.txt

echo ""
echo "Compilation complete!"
echo ""
echo "To run: ./run-wsl.sh"
