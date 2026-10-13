@echo off
chcp 936 >nul 2>nul
setlocal EnableExtensions EnableDelayedExpansion
title 电子商务平台 - 一键启动

rem ================================================================
rem  电子商务平台 · 一键启动
rem  后端 Spring Boot : http://localhost:8443/eBusiness
rem  前端 Vue 3       : http://localhost:8080
rem  用法：双击本文件（本文件须放在「电子商务平台」文件夹根目录）
rem
rem  实现说明：
rem    1. 脚本内所有系统命令都写成绝对路径（%SystemRoot%\System32\...），
rem       避免被 Git Bash / MSYS 等环境里的同名程序劫持。
rem    2. 等待用 ping 而不是 timeout —— timeout 在 stdin 被重定向时
rem       会立即报错退出，导致等待循环空转直接超时。
rem    3. Java / Node 都做多路径探测，不写死单一安装位置。
rem ================================================================

set "SYS=%SystemRoot%\System32"
set "ROOT=%~dp0"
set "SERVER_DIR=%ROOT%codes\ebusiness-server"
set "VUE_DIR=%ROOT%codes\ebusiness-vue"
set "JAR=%SERVER_DIR%\target\ebusiness-server-1.0.0.jar"
set "WEB_URL=http://localhost:8080/"

echo.
echo ================================================================
echo    电子商务平台   一键启动
echo ================================================================
echo    后端  : http://localhost:8443/eBusiness
echo    前端  : http://localhost:8080
echo    后台  : http://localhost:8080/adminLogin
echo ================================================================
echo.

rem ---------- 0. 目录检查 ----------
if not exist "%SERVER_DIR%" goto :err_dir
if not exist "%VUE_DIR%"    goto :err_dir
echo [1/6] 工程目录检查通过

rem ---------- 1. 定位 Java ----------
set "JAVA="
if exist "D:\JDK11\bin\java.exe" set "JAVA=D:\JDK11\bin\java.exe"
if not defined JAVA for /f "delims=" %%i in ('"%SYS%\where.exe" java 2^>nul') do if not defined JAVA set "JAVA=%%i"
if not defined JAVA goto :err_java
echo [2/6] Java  : %JAVA%

rem ---------- 2. 定位 Node / npm ----------
set "NODE_DIR="
"%SYS%\where.exe" npm >nul 2>nul
if not errorlevel 1 goto :npm_ok
for %%D in (
  "C:\Program Files\nodejs"
  "C:\Program Files (x86)\nodejs"
  "D:\nodejs"
  "D:\Program Files\nodejs"
  "%USERPROFILE%\AppData\Roaming\npm"
  "%USERPROFILE%\.workbuddy\binaries\node\versions\22.22.2-6"
  "%USERPROFILE%\.ai-manager\runtimes\node\24.20.0"
) do if not defined NODE_DIR if exist "%%~D\npm.cmd" set "NODE_DIR=%%~D"
if not defined NODE_DIR goto :err_node
set "PATH=!NODE_DIR!;!PATH!"
echo         Node  : !NODE_DIR!
:npm_ok
echo [3/6] Node/npm 就绪

rem ---------- 3. 检查 MySQL ----------
"%SYS%\netstat.exe" -an | "%SYS%\findstr.exe" /C:":3306 " | "%SYS%\findstr.exe" /C:"LISTENING" >nul
if errorlevel 1 (
  echo [4/6] [提示] 未检测到 MySQL 3306 端口，后端可能启动失败，请先启动 MySQL 服务
) else (
  echo [4/6] MySQL : 运行中
)

rem ---------- 4. 检查后端 jar ----------
if exist "%JAR%" goto :jar_ok
echo [信息] 未找到后端 jar，正在用 Maven 打包（首次较慢，请耐心等待）...
set "MVN="
if exist "D:\Maven\apache-maven-3.9.16\bin\mvn.cmd" set "MVN=D:\Maven\apache-maven-3.9.16\bin\mvn.cmd"
if not defined MVN for /f "delims=" %%i in ('"%SYS%\where.exe" mvn 2^>nul') do if not defined MVN set "MVN=%%i"
if defined MVN if not exist "!MVN!" set "MVN=!MVN!.cmd"
if not defined MVN goto :err_mvn
pushd "%SERVER_DIR%"
call "!MVN!" clean package -DskipTests
set "RC=!errorlevel!"
popd
if not "!RC!"=="0" goto :err_build
if not exist "%JAR%" goto :err_build
:jar_ok
echo [5/6] 后端 : jar 就绪

rem ---------- 5. 检查前端依赖 ----------
if exist "%VUE_DIR%\node_modules" goto :nm_ok
echo [信息] 未找到 node_modules，正在安装前端依赖（首次较慢）...
pushd "%VUE_DIR%"
call npm install
set "RC=!errorlevel!"
popd
if not "!RC!"=="0" goto :err_npm_install
:nm_ok
echo [6/6] 前端 : 依赖就绪

rem ---------- 6. 启动两个服务（已在运行则跳过） ----------
echo.
"%SYS%\netstat.exe" -an | "%SYS%\findstr.exe" /C:":8443 " | "%SYS%\findstr.exe" /C:"LISTENING" >nul
if errorlevel 1 (
  echo 正在启动后端服务 ^(8443^)...
  start "电子商务平台-后端-8443" /D "%SERVER_DIR%" cmd /k ""%JAVA%" -jar "%JAR%" --server.port=8443"
) else (
  echo [提示] 后端 8443 已在运行，跳过启动
)

"%SYS%\netstat.exe" -an | "%SYS%\findstr.exe" /C:":8080 " | "%SYS%\findstr.exe" /C:"LISTENING" >nul
if errorlevel 1 (
  echo 正在启动前端服务 ^(8080^)...
  start "电子商务平台-前端-8080" /D "%VUE_DIR%" cmd /k "npm run serve"
) else (
  echo [提示] 前端 8080 已在运行，跳过启动
)

rem ---------- 7. 等待服务就绪（最多约 120 秒） ----------
echo.
echo 等待服务就绪（前端首次编译约 20~60 秒）...
set /a TRY=0
:wait_be
set /a TRY+=1
if !TRY! GTR 60 goto :err_timeout
%SYS%\ping.exe -n 3 127.0.0.1 >nul
"%SYS%\netstat.exe" -an | "%SYS%\findstr.exe" /C:":8443 " | "%SYS%\findstr.exe" /C:"LISTENING" >nul
if errorlevel 1 goto :wait_be
echo   - 后端 8443 已就绪

set /a TRY=0
:wait_fe
set /a TRY+=1
if !TRY! GTR 60 goto :err_timeout
%SYS%\ping.exe -n 3 127.0.0.1 >nul
"%SYS%\netstat.exe" -an | "%SYS%\findstr.exe" /C:":8080 " | "%SYS%\findstr.exe" /C:"LISTENING" >nul
if errorlevel 1 goto :wait_fe
echo   - 前端 8080 已就绪

rem ---------- 8. 打开浏览器 ----------
start "" "%WEB_URL%"
echo.
echo ================================================================
echo    启动完成，浏览器已打开：%WEB_URL%
echo.
echo    管理员后台：http://localhost:8080/adminLogin
echo    演示账号  ：管理员 admin / 123456
echo.
echo    关闭本窗口不会停止服务；要停止服务，请关掉
echo    「电子商务平台-后端-8443」和「电子商务平台-前端-8080」两个窗口。
echo ================================================================
echo.
pause
exit /b 0

rem ================= 错误提示 =================
:err_dir
echo [错误] 没有找到工程目录，请确认本文件放在「电子商务平台」文件夹根目录。
goto :fail
:err_java
echo [错误] 没有找到 Java。请安装 JDK 11，或把 java.exe 所在目录加入系统 PATH。
goto :fail
:err_node
echo [错误] 没有找到 Node.js / npm。请先安装 Node.js（建议 18 及以上）后重试。
goto :fail
:err_mvn
echo [错误] 既没有后端 jar，也没有找到 Maven，无法自动打包。
echo        请先执行 mvn clean package 打包后端，或安装并配置 Maven。
goto :fail
:err_build
echo [错误] 后端打包失败，请查看上方 Maven 的输出信息。
goto :fail
:err_npm_install
echo [错误] 前端依赖安装失败，请检查网络或 npm 配置。
goto :fail
:err_timeout
echo.
echo [警告] 等待超时：服务可能还在启动，或启动过程中报错了。
echo        请查看「电子商务平台-后端-8443」「电子商务平台-前端-8080」两个窗口里的日志。
goto :fail
:fail
echo.
pause
exit /b 1
