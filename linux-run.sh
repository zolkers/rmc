#!/bin/bash

clear

RESET="\e[0m"

C1="\e[38;5;94m"
C2="\e[38;5;130m"
C3="\e[38;5;166m"
C4="\e[38;5;172m"
C5="\e[38;5;178m"
C6="\e[38;5;214m"
C7="\e[38;5;220m"

echo -e "${C1}██████╗ ${C2}███╗   ███╗ ${C3}██████╗"
echo -e "${C2}██╔══██╗${C3}████╗ ████║${C4}██╔════╝"
echo -e "${C3}██████╔╝${C4}██╔████╔██║${C5}██║     "
echo -e "${C4}██╔══██╗${C5}██║╚██╔╝██║${C6}██║     "
echo -e "${C5}██║  ██║${C6}██║ ╚═╝ ██║${C7}╚██████╗"
echo -e "${C6}╚═╝  ╚═╝${C7}╚═╝     ╚═╝  ╚═════╝${RESET}"
echo
sleep 0.8

# Initialize gradlew if needed (fix line endings and permissions)
init_gradlew() {
    if [ ! -x "./gradlew" ] || file gradlew 2>/dev/null | grep -q "CRLF"; then
        echo -e "${C4}Initializing Gradle wrapper...${RESET}"

        # Fix line endings
        if [ -f "./gradlew" ]; then
            sed -i 's/\r$//' gradlew 2>/dev/null || dos2unix gradlew 2>/dev/null || true
        fi

        # Make executable
        chmod +x gradlew 2>/dev/null || true

        echo -e "${C4}✓ Gradle wrapper ready${RESET}"
    fi
}

find_java21() {
    # Unset any existing JAVA_HOME to start fresh
    unset JAVA_HOME

    # Check if java in PATH is version 21
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" = "21" ]; then
            # Found Java 21 in PATH, set JAVA_HOME
            # shellcheck disable=SC2046
            JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
            export JAVA_HOME
            export PATH="$JAVA_HOME/bin:$PATH"
            return 0
        fi
    fi

    # Search common Java 21 installation directories
    for dir in \
        "/usr/lib/jvm/java-21-openjdk-amd64" \
        "/usr/lib/jvm/java-21-openjdk" \
        "/usr/lib/jvm/temurin-21-jdk-amd64" \
        "/mnt/c/Program Files/Java/jdk-21"* \
        "/mnt/c/Users/$USER/.jdks/temurin-21"* \
        "/mnt/c/Users/$USER/.jdks/corretto-21"*; do
        if [ -d "$dir" ] && [ -x "$dir/bin/java" ]; then
            export JAVA_HOME="$dir"
            export PATH="$JAVA_HOME/bin:$PATH"
            echo -e "${C4}Found Java 21 at: $JAVA_HOME${RESET}"
            return 0
        fi
    done

    return 1
}

# Initialize environment
init_gradlew

# Try to find Java 21
if ! find_java21; then
    echo -e "${C3}Java 21 not found. Attempting to install...${RESET}"
    if command -v apt-get &> /dev/null; then
        sudo apt-get update && sudo apt-get install -y openjdk-21-jdk
        find_java21
    else
        echo -e "${C3}Error: Java 21 is required but not found.${RESET}"
        echo -e "${C3}Please install Java 21 manually.${RESET}"
        exit 1
    fi
fi

echo -e "${C4}JVM version info: 21${RESET}"
echo -e "${C4}Building for MC 1.21.4${RESET}"
echo
sleep 0.5

echo -e "${C5}Building shadow JAR...${RESET}"
./gradlew shadowJar -q

# shellcheck disable=SC2181
if [ $? -ne 0 ]; then
    echo -e "${C3}Build failed!${RESET}"
    exit 1
fi

echo -e "${C6}Starting application...${RESET}"
java -Djava.library.path="natives" \
     --enable-native-access=ALL-UNNAMED \
     -jar build/libs/RiegeTerminal-1.21.4.jar