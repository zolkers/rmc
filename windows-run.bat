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

call :find_java21_or_higher
if errorlevel 1 (
    echo Java 21+ not found. Attempting to install using winget...
    where winget >nul 2>nul
    if errorlevel 1 (
        echo winget not found. Please install Java 21 or a higher version manually.
        pause
        exit /b 1
    )
    winget install Microsoft.OpenJDK.21
    call :find_java21_or_higher
    if errorlevel 1 (
        echo Java 21+ installation failed. Please install it manually.
        pause
        exit /b 1
    )
)

echo JVM version info: 21+
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

:find_java21_or_higher
    set "JAVA_HOME_ORIG=%JAVA_HOME%"
    if defined JAVA_HOME_ORIG (
        if exist "%JAVA_HOME_ORIG%\bin\java.exe" (
            set "JAVA_HOME=%JAVA_HOME_ORIG%"
            for /f "tokens=3" %%b in ('"%JAVA_HOME%\bin\java.exe" -version 2^>^&1') do (
                for /f "tokens=1 delims=." %%c in ('echo %%b') do (
                    set "JAVA_VERSION=%%c"
                )
            )
            goto :found_java
        )
    )

    set "JAVA_HOME="
    for /f "tokens=*" %%a in ('where java 2^>nul') do (
        for /f "tokens=3" %%b in ('"%%a" -version 2^>^&1') do (
            for /f "tokens=1 delims=." %%c in ('echo %%b') do (
                if %%c GEQ 21 (
                    call :get_parent_dir "%%~dpa"
                    set "JAVA_VERSION=%%c"
                    goto :found_java
                )
            )
        )
    )

    for /l %%v in (21,1,25) do (
      for %%d in (
          "C:\Program Files\Java\jdk-%%v"
          "C:\Program Files\Amazon Corretto\jdk%%v"
          "C:\Users\%USERNAME%\.jdks\temurin-%%v*"
      ) do (
          if exist "%%d\bin\java.exe" (
              set "JAVA_HOME=%%d"
              set "JAVA_VERSION=%%v"
              goto :found_java
          )
      )
    )

    goto :not_found_java

:get_parent_dir
    set "JAVA_HOME=%~dp1"
    if "%JAVA_HOME:~-1%"=="\" set "JAVA_HOME=%JAVA_HOME:~0,-1%"
    for %%f in ("%JAVA_HOME%") do set "JAVA_HOME=%%~dpf"
    if "%JAVA_HOME:~-1%"=="\" set "JAVA_HOME=%JAVA_HOME:~0,-1%"
    exit /b

:found_java
    echo Found Java %JAVA_VERSION% at: %JAVA_HOME%
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    exit /b 0

:not_found_java
    exit /b 1
