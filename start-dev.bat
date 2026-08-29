@echo off
setlocal
chcp 936 >nul
title AncauqL Blog 启动器

set "ROOT=%~dp0"
set "CHECK=0"
if /i "%~1"=="/check" set "CHECK=1"

echo ==========================================
echo   AncauqL Blog 本地开发环境启动
echo ==========================================
echo.

rem ---------- 1. 读取本地数据库配置 ----------
rem 优先读取 dev-env.bat（复制 dev-env.example.bat 修改，已被 git 忽略）
if exist "%ROOT%dev-env.bat" (
    call "%ROOT%dev-env.bat"
    echo [环境] 已加载 dev-env.bat
)
if not defined DB_USERNAME set "DB_USERNAME=root"

if "%CHECK%"=="1" goto :resolve_mvn
if defined DB_PASSWORD goto :resolve_mvn
echo [提示] 未设置 DB_PASSWORD。建议复制 dev-env.example.bat 为 dev-env.bat 并填入密码，
echo        以后启动就不用每次输入。
set /p DB_PASSWORD=请输入 MySQL %DB_USERNAME% 用户密码（直接回车 = 默认 123456）:
if not defined DB_PASSWORD set "DB_PASSWORD=123456"

:resolve_mvn
rem ---------- 2. 定位 Maven ----------
rem 优先用 PATH 里的 mvn；找不到就扫描 ~/.m2/wrapper/dists 下的发行版
set "MVN_CMD="
where mvn >nul 2>nul && set "MVN_CMD=mvn"
if defined MVN_CMD goto :mvn_found

for /f "delims=" %%i in ('dir /b /s "%USERPROFILE%\.m2\wrapper\dists\mvn.cmd" 2^>nul') do (
    set "MVN_CMD=%%i"
    goto :mvn_found
)

echo [错误] 没有找到 Maven：PATH 中无 mvn，%USERPROFILE%\.m2\wrapper\dists 下也没有发行版。
echo        请安装 Maven 或先用 IDEA 打开 blog_backend 触发一次 Maven 下载。
if "%CHECK%"=="0" pause
exit /b 1

:mvn_found
echo [环境] Maven   : %MVN_CMD%
echo [环境] 数据库  : %DB_USERNAME%@localhost:3306/blog_system

rem ---------- 3. 前端依赖检查 ----------
set "FRONT_CMD=npm run serve"
if not exist "%ROOT%blog_frontend\vue\node_modules" (
    echo [环境] 前端首次启动，将先执行 npm install
    set "FRONT_CMD=npm install && npm run serve"
)

if "%CHECK%"=="1" (
    echo.
    echo [check] 环境检查通过，未启动任何服务。
    exit /b 0
)

rem ---------- 4. 数据库连通性检查 ----------
where mysql >nul 2>nul || (
    echo [警告] PATH 中没有 mysql 命令，跳过数据库连通性检查。
    goto :launch
)
mysql -u%DB_USERNAME% -p%DB_PASSWORD% -D blog_system -e "SELECT 1;" >nul 2>nul || (
    echo.
    echo [警告] 无法连接 blog_system 数据库！可能原因：
    echo        1. MySQL 服务没有启动（services.msc 检查 MySQL80）
    echo        2. 密码不对（当前使用用户 %DB_USERNAME%）
    echo        3. 还没导入 database/blog_system.sql
    echo        后端启动会失败，可关闭本窗口处理后重试；也可按任意键强行继续。
    pause >nul
)

:launch
rem ---------- 5. 启动前后端（各自新窗口，关窗口即停服务） ----------
echo.
echo [启动] 后端窗口：Spring Boot（端口 9999）...
start "blog-backend :9999" /d "%ROOT%blog_backend" cmd /k "call %MVN_CMD% spring-boot:run"

echo [启动] 前端窗口：Vue CLI（端口 8080）...
start "blog-frontend :8080" /d "%ROOT%blog_frontend\vue" cmd /k "%FRONT_CMD%"

echo.
echo ==========================================
echo   全部启动完毕，访问地址：
echo.
echo   博客前台   http://localhost:8080
echo   后台登录   http://localhost:8080/login   （初始账号 admin / 123456）
echo   后端接口   http://localhost:9999         （健康检查 /hello）
echo   MySQL      localhost:3306 / blog_system
echo.
echo   说明：
echo   - 后端就绪标志：后端窗口出现 "Started BlogBackendApplication"
echo   - 8080 被占用时 Vue 会自动换端口，以前端窗口实际输出为准
echo   - 停止服务：关闭对应窗口，或运行 stop-dev.bat
echo ==========================================
echo.
pause
exit /b 0
