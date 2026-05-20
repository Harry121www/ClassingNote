@echo off
title ClassNote
echo Starting ClassNote...
echo.
echo 数据存储在: %USERPROFILE%\.classnote
echo 附件存储在: %USERPROFILE%\classnote-files
echo.
echo 浏览器打开 http://localhost:8080 即可使用
echo.
echo 按 Ctrl+C 停止服务

java -jar "%~dp0ClassNote.jar" --spring.profiles.active=h2

pause
