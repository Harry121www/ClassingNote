@echo off
chcp 65001 >nul
echo === ClassNote EXE Builder ===
echo.

set JDK17=C:\Program Files\Microsoft\jdk-17.0.12.7-hotspot
if not exist "%JDK17%\bin\java.exe" (
    echo JDK 17 not found at %JDK17%
    echo Please install JDK 17 or edit this file to set correct path.
    pause
    exit /b 1
)

set JPACKAGE=%JDK17%\bin\jpackage.exe

:: Check / download JavaFX SDK
set JAVAFX=%~dp0javafx-sdk-17.0.6
if not exist "%JAVAFX%\lib\javafx.web.jar" (
    echo Downloading JavaFX SDK (one-time, ~60MB)...
    powershell -Command "Invoke-WebRequest -Uri 'https://download2.gluonhq.com/openjfx/17.0.6/openjfx-17.0.6_windows-x64_bin-sdk.zip' -OutFile '%TEMP%\javafx-sdk.zip'"
    powershell -Command "Expand-Archive -Path '%TEMP%\javafx-sdk.zip' -DestinationPath '%~dp0' -Force"
    del "%TEMP%\javafx-sdk.zip"
    if not exist "%JAVAFX%\lib\javafx.web.jar" (
        echo JavaFX SDK download failed! Please download manually from https://gluonhq.com/products/javafx/
        pause & exit /b 1
    )
)

echo [1/4] Building frontend...
cd /d "%~dp0frontend"
call npm run build
if %errorlevel% neq 0 ( echo Frontend build failed! & pause & exit /b 1 )

echo [2/4] Building backend JAR with JDK 17 (H2 profile)...
cd /d "%~dp0"
set JAVA_HOME=%JDK17%
set PATH=%JDK17%\bin;%PATH%
call mvn clean package -DskipTests -P h2 -q
if %errorlevel% neq 0 ( echo Backend build failed! & pause & exit /b 1 )

echo [3/4] Creating app image with bundled JRE...
set OUTPUT=%~dp0dist\ClassNote-App
if exist "%OUTPUT%" rmdir /s /q "%OUTPUT%"

"%JPACKAGE%" --name ClassNote --input target --main-jar classnote-local-1.0.0.jar --type app-image --dest "%OUTPUT%" --module-path "%JAVAFX%\lib" --add-modules javafx.web,javafx.controls --java-options "-Dspring.profiles.active=h2 -Djava.awt.headless=false -Djavafx.allowjs=true" --app-version 1.0.0

if %errorlevel% neq 0 (
    echo jpackage failed! Make sure JDK 17 is installed.
    pause
    exit /b 1
)

echo [4/4] Creating ZIP distribution...
powershell -Command "Compress-Archive -Path '%OUTPUT%\ClassNote' -DestinationPath '%~dp0dist\ClassNote.zip' -Force"

echo.
echo === Done! ===
echo EXE location: %OUTPUT%\ClassNote\ClassNote.exe
echo ZIP package: dist\ClassNote.zip
echo.
pause
