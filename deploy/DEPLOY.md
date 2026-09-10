# AncauqL Blog 部署说明（Ubuntu 服务器）

> 目标：把前端静态产物 + Spring Boot 后端 + MySQL 跑在一台 Ubuntu 服务器上，
> 用 nginx 做同域反代（前端走 `/api`，避免跨域并顺带解决 CORS），并启用 HTTPS。
> **服务器操作由站主执行或明确授权后进行。**

## 0. 前置

- Ubuntu 22.04/24.04，已装：`openjdk-17-jre-headless`、`nginx`、`mysql-server`、`git`、`nodejs`(构建前端用，可只在本地构建)。
- 一个域名解析到服务器（用于 Let's Encrypt）。
- 规划目录：
  - `/opt/blog/` ：后端 jar、`uploads/`、env 文件
  - `/var/www/ancauql-blog/dist/` ：前端静态产物

## 1. 创建数据库与低权限账号

用 root 连上 MySQL，执行 `deploy/mysql/create-app-user.sql`（把里面的占位密码换成强密码）：

```bash
mysql -uroot -p < deploy/mysql/create-app-user.sql
mysql -uroot -p blog_system < database/blog_system.sql   # 首次导入；已有数据请勿重复执行
```

> 应用**不要**用 root 连库。只为应用账号开放 `blog_system` 库的增删改查权限。

## 2. 构建产物（可在本地构建后上传）

```bash
# 后端
cd blog_backend && mvn clean package        # 产物 target/blog_backend-0.0.1-SNAPSHOT.jar

# 前端（生产走 /api 同域反代）
cd blog_frontend/vue && npm ci && npm run build   # 产物 dist/
```

## 3. 服务器目录与环境变量

```bash
sudo useradd -r -s /usr/sbin/nologin blog || true
sudo mkdir -p /opt/blog/uploads /var/www/ancauql-blog
sudo cp blog_backend-0.0.1-SNAPSHOT.jar /opt/blog/blog_backend.jar
sudo cp -r blog_frontend/vue/dist/* /var/www/ancauql-blog/dist/
sudo chown -R blog:blog /opt/blog
```

`/etc/blog/blog.env`（权限 `chmod 600`，**不要提交进仓库**）：

```ini
DB_URL=jdbc:mysql://localhost:3306/blog_system?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
DB_USERNAME=blog_app
DB_PASSWORD=换成你的强密码
BLOG_SITE_URL=https://你的域名
CORS_ALLOWED_ORIGINS=
# 同域反代时留空即可（前端走 /api）
```

## 4. systemd 服务

```bash
sudo mkdir -p /etc/blog
sudo cp deploy/systemd/blog-backend.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now blog-backend
sudo systemctl status blog-backend
```

## 5. nginx + HTTPS

```bash
sudo cp deploy/nginx/00-blog-limit.conf /etc/nginx/conf.d/
sudo cp deploy/nginx/blog-site.conf /etc/nginx/conf.d/      # 记得改 server_name / 证书路径
sudo nginx -t && sudo systemctl reload nginx

# 证书（首次）
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d 你的域名
```

`blog-site.conf` 已配置：`/` 静态 + history fallback、`/api/` 反代到 9999、`/uploads/` 与 `/feed.xml` 反代、
登录接口限流、常用安全响应头。

## 6. 备份

```bash
sudo cp deploy/backup/mysql-backup.sh /usr/local/bin/ && sudo chmod +x /usr/local/bin/mysql-backup.sh
sudo crontab -e    # 加入：0 3 * * * /usr/local/bin/mysql-backup.sh
```

备份落在 `/var/backups/blog/`，默认保留 14 天。

## 7. 上线前检查清单

- [ ] `admin` 默认口令已改（后台 →「账号设置」）。
- [ ] 数据库用 `blog_app` 低权限账号，密码只存在于 `/etc/blog/blog.env`（600）。
- [ ] HTTPS 生效，`http` 自动跳 `https`；证书自动续期（`certbot renew --dry-run`）。
- [ ] 防火墙只放行 22/80/443（`sudo ufw allow 22,80,443/tcp && sudo ufw enable`）。
- [ ] 登录限流生效（连错 5 次应提示临时锁定）。
- [ ] 评论/注册仍开着**蜜罐+IP 限流**；如不需要公开注册，可先关闭注册入口。
- [ ] 备份任务跑通一次（手动执行脚本确认产出文件）。
- [ ] `GET /feed.xml`、图片 `/uploads/...`、后台登录均正常。

## 8. 日常

```bash
sudo systemctl restart blog-backend      # 重启后端
sudo journalctl -u blog-backend -f       # 看后端日志
sudo systemctl reload nginx              # 改完 nginx 配置后
```
