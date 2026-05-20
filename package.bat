@echo off
echo === ClassNote 打包脚本 ===
echo.

:: 1. Build frontend
echo [1/3] 构建前端...
cd /d "%~dp0frontend"
call npm run build
if %errorlevel% neq 0 (
    echo 前端构建失败！
    exit /b 1
)

:: 2. Build backend with H2 profile
echo [2/3] 构建后端 (H2 嵌入式数据库)...
cd /d "%~dp0"
call mvn clean package -DskipTests -P h2
if %errorlevel% neq 0 (
    echo 后端构建失败！
    exit /b 1
)

:: 3. Create distribution
echo [3/3] 创建发布包...
set DIST=%~dp0dist\ClassNote
if exist "%DIST%" rmdir /s /q "%DIST%"
mkdir "%DIST%"

copy "target\classnote-local-1.0.0.jar" "%DIST%\ClassNote.jar"
copy "package\start.vbs" "%DIST%\start.vbs"
copy "package\README.txt" "%DIST%\README.txt"

echo.
echo === 打包完成 ===
echo 发布包位置: %DIST%
echo 双击 start.bat 即可运行
echo.
echo 如果要分发给别人，把 dist\ClassNote 文件夹打包成 zip 即可。
pause
