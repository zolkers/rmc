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

call :find_java21
if errorlevel 1 (
    echo Java 21 not found. Please install it.
    pause
    exit /b 1
)

echo JVM version info: 21
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
goto :eof

:find_java21
    set "JAVA_HOME="
    for /f "tokens=*" %%a in ('where java 2^>nul') do (
        for /f "tokens=3" %%b in ('"%%a" -version 2^>^&1') do (
            for /f "tokens=1 delims=." %%c in ('echo %%b') do (
                if "%%c"=="21" (
                    set "JAVA_HOME=%%~dpa"
                    goto :found_java
                )
            )
        )
    )

    for %%d in (
        "C:\Program Files\Java\jdk-21"
        "C:\Program Files\Amazon Corretto\jdk21"
    ) do (
        if exist "%%d\bin\java.exe" (
            set "JAVA_HOME=%%d"
            goto :found_java
        )
    )

    goto :not_found_java

:found_java
    echo Found Java 21 at: %JAVA_HOME%
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    exit /b 0

:not_found_java
    exit /b 1
