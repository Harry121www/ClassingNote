@echo off
echo === ClassNote EXE Builder ===
echo.

set JDK17=C:\Program Files\Microsoft\jdk-17.0.12.7-hotspot
if not exist "%JDK17%\bin\java.exe" (
    echo JDK 17 not found at %JDK17%
    pause
    exit /b 1
)

:: JavaFX on D drive to avoid C drive space issues
set JAVAFX_ROOT=D:\ClassNoteDeps\javafx
if not exist "%JAVAFX_ROOT%" mkdir "%JAVAFX_ROOT%"
set JAVAFX_SDK=%JAVAFX_ROOT%\javafx-sdk-17.0.6
set JAVAFX_JMODS=%JAVAFX_ROOT%\javafx-jmods-17.0.6
set DOWNLOAD_TMP=D:\ClassNoteDeps\tmp
if not exist "%DOWNLOAD_TMP%" mkdir "%DOWNLOAD_TMP%"

if not exist "%JAVAFX_SDK%\lib\javafx.web.jar" (
    echo JavaFX SDK not found! Downloading to D:\ClassNoteDeps...
    powershell -Command "Invoke-WebRequest -Uri 'https://download2.gluonhq.com/openjfx/17.0.6/openjfx-17.0.6_windows-x64_bin-sdk.zip' -OutFile '%DOWNLOAD_TMP%\javafx-sdk.zip'"
    powershell -Command "Expand-Archive -Path '%DOWNLOAD_TMP%\javafx-sdk.zip' -DestinationPath '%JAVAFX_ROOT%' -Force"
    del "%DOWNLOAD_TMP%\javafx-sdk.zip"
)
if not exist "%JAVAFX_JMODS%\javafx.web.jmod" (
    echo JavaFX JMODs not found! Downloading to D:\ClassNoteDeps...
    powershell -Command "Invoke-WebRequest -Uri 'https://download2.gluonhq.com/openjfx/17.0.6/openjfx-17.0.6_windows-x64_bin-jmods.zip' -OutFile '%DOWNLOAD_TMP%\javafx-jmods.zip'"
    powershell -Command "Expand-Archive -Path '%DOWNLOAD_TMP%\javafx-jmods.zip' -DestinationPath '%JAVAFX_ROOT%' -Force"
    del "%DOWNLOAD_TMP%\javafx-jmods.zip"
)

echo [1/4] Building custom JRE with JavaFX...
set JRE_OUT=%~dp0target\javafx-jre

echo [1/4] Building custom JRE with JavaFX...
set JRE_OUT=%~dp0target\javafx-jre
if exist "%JRE_OUT%" rmdir /s /q "%JRE_OUT%"
echo   Running jlink...
"%JDK17%\bin\jlink.exe" --output "%JRE_OUT%" --module-path "%JDK17%\jmods" --module-path "%JAVAFX_JMODS%" --add-modules java.base,java.compiler,java.datatransfer,java.desktop,java.instrument,java.logging,java.management,java.management.rmi,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.se,java.security.jgss,java.security.sasl,java.sql,java.transaction.xa,java.xml,java.xml.crypto,jdk.unsupported,jdk.crypto.ec,jdk.jartool,jdk.net,jdk.zipfs,javafx.web,javafx.controls --bind-services
if errorlevel 1 (
    echo jlink errorlevel=%errorlevel%
    pause & exit /b 1
)
if not exist "%JRE_OUT%\bin\java.exe" (
    echo jlink produced no output at %JRE_OUT%
    pause & exit /b 1
)
echo   JRE created successfully.

echo [2/4] Building frontend...
cd /d "%~dp0frontend"
call npm run build
if %errorlevel% neq 0 ( echo Frontend build failed! & pause & exit /b 1 )

echo [3/4] Building backend with JDK 17...
cd /d "%~dp0"
set JAVA_HOME=%JDK17%
set PATH=%JDK17%\bin;%PATH%
call mvn clean package -DskipTests -P h2 -q
if %errorlevel% neq 0 ( echo Backend build failed! & pause & exit /b 1 )

echo [4/4] Creating EXE with JavaFX desktop window...
set OUTPUT=%~dp0dist\ClassNote-App
if exist "%OUTPUT%" rmdir /s /q "%OUTPUT%"

"%JDK17%\bin\jpackage.exe" --name ClassNote --input target --main-jar classnote-local-1.0.0.jar --type app-image --dest "%OUTPUT%" --runtime-image "%JRE_OUT%" --java-options "-Dspring.profiles.active=h2 -Djava.awt.headless=false" --app-version 1.0.0
if %errorlevel% neq 0 ( echo jpackage failed! & pause & exit /b 1 )

echo.
echo === Done ===
echo EXE: %OUTPUT%\ClassNote\ClassNote.exe
echo.
pause
