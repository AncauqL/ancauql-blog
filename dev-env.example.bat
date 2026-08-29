@echo off
rem =====================================================
rem 本地数据库配置模板
rem 使用方法：复制本文件为 dev-env.bat，填入你的真实配置。
rem dev-env.bat 已加入 .gitignore，不会被提交。
rem =====================================================

set "DB_USERNAME=root"
set "DB_PASSWORD=在这里填你的MySQL密码"

rem 如需自定义连接地址（一般不用改），取消下面一行注释：
rem set "DB_URL=jdbc:mysql://localhost:3306/blog_system?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
