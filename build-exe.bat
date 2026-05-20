@echo off
echo === ClassNote EXE Builder ===
echo.

set JDK17=C:\Program Files\Microsoft\jdk-17.0.12.7-hotspot
if not exist "%JDK17%\bin\java.exe" (
    echo JDK 17 not found at %JDK17%
    pause
    exit /b 1
)

set JAVAFX=%~dp0javafx-sdk-17.0.6
if not exist "%JAVAFX%\lib\javafx.web.jar" (
    echo JavaFX SDK not found at %JAVAFX%
    echo Download from: https://gluonhq.com/products/javafx/
    echo Extract to: %~dp0
    pause
    exit /b 1
)

echo [1/3] Building frontend...
cd /d "%~dp0frontend"
call npm run build
if %errorlevel% neq 0 ( echo Frontend build failed! & pause & exit /b 1 )

echo [2/3] Building backend with JDK 17...
cd /d "%~dp0"
set JAVA_HOME=%JDK17%
set PATH=%JDK17%\bin;%PATH%
call mvn clean package -DskipTests -P h2 -q
if %errorlevel% neq 0 ( echo Backend build failed! & pause & exit /b 1 )

echo [3/3] Creating EXE with JavaFX desktop window...
set OUTPUT=%~dp0dist\ClassNote-App
if exist "%OUTPUT%" rmdir /s /q "%OUTPUT%"

%JDK17%\bin\jpackage.exe --name ClassNote --input target --main-jar classnote-local-1.0.0.jar --type app-image --dest "%OUTPUT%" --module-path "%JAVAFX%\lib" --add-modules javafx.web,javafx.controls --java-options "-Dspring.profiles.active=h2 -Djava.awt.headless=false" --app-version 1.0.0
if %errorlevel% neq 0 ( echo jpackage failed! & pause & exit /b 1 )

echo.
echo === Done ===
echo EXE: %OUTPUT%\ClassNote\ClassNote.exe
echo.
pause
