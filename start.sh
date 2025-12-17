#!/bin/bash

# Détecter le système d'exploitation
OS="$(uname -s)"

case "${OS}" in
    Linux*)     ./linux-start.sh;;
    Darwin*)    ./macos-start.sh;;
    CYGWIN*|MINGW*|MSYS*) ./windows-start.bat;;
    *)          echo "Unsupported OS: ${OS}" && exit 1;;
esac