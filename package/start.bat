@echo off
title ClassNote

cd /d "%~dp0"

set JAVA=%JAVA_HOME%\bin\java.exe
if not exist "%JAVA%" set JAVA=C:\Program Files\Microsoft\jdk-17.0.12.7-hotspot\bin\java.exe
if not exist "%JAVA%" (
    echo Looking for java on PATH...
    set JAVA=java
)

set JAR=%~dp0ClassNote.jar
if not exist "%JAR%" set JAR=%~dp0..\target\classnote-local-1.0.0.jar

if not exist "%JAR%" (
    echo JAR file not found!
    echo Please run package.bat from project root first.
    pause
    exit /b 1
)

echo Java: %JAVA%
echo JAR: %JAR%
echo Starting ClassNote on http://localhost:8080
echo Close this window to stop.
echo.

"%JAVA%" -jar "%JAR%" --spring.profiles.active=h2 2>&1

pause
