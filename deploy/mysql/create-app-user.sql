-- 为博客应用创建低权限数据库账号（把 '换成强密码' 换成你自己的强密码）
-- 用法：mysql -uroot -p < deploy/mysql/create-app-user.sql
-- 之后在 /etc/blog/blog.env 里配置 DB_USERNAME=blog_app 与 DB_PASSWORD=同一个密码

CREATE DATABASE IF NOT EXISTS `blog_system`
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE USER IF NOT EXISTS 'blog_app'@'localhost' IDENTIFIED BY '换成强密码';

-- 只给该库的增删改查权限，不授予建库/授权等
GRANT SELECT, INSERT, UPDATE, DELETE ON `blog_system`.* TO 'blog_app'@'localhost';
FLUSH PRIVILEGES;
