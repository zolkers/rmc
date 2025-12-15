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