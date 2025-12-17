@echo off
cls

echo ██████╗ ███╗   ███╗ ██████╗
echo ██╔══██╗████╗ ████║██╔════╝
echo ██████╔╝██╔████╔██║██║
echo ██╔══██╗██║╚██╔╝██║██║
echo ██║  ██║██║ ╚═╝ ██║╚██████╗
echo ╚═╝  ╚═╝╚═╝     ╚═╝ ╚═════╝
echo.
timeout /t 1 /nobreak > nul

echo Building for MC 1.21.4
echo.
timeout /t 1 /nobreak > nul

echo Building shadow JAR...
call gradlew.bat shadowJar -q

if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)

echo Starting application...
java -Djava.library.path="natives" --enable-native-access=ALL-UNNAMED -jar build/libs/RiegeTerminal-1.21.4.jar
