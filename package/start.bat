@echo off
title ClassNote
chcp 65001 >nul

set JAR=%~dp0ClassNote.jar
if not exist "%JAR%" set JAR=%~dp0..\target\classnote-local-1.0.0.jar

if not exist "%JAR%" (
    echo JAR not found. Run package.bat first.
    pause
    exit /b 1
)

echo Starting ClassNote...
echo URL: http://localhost:8080
echo.

java -jar "%JAR%" --spring.profiles.active=h2

pause
