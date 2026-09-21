#!/usr/bin/env bash
# ============================================================================
# AncauqL Blog 部署脚本 —— 在本机（Git Bash）运行，目标机为 Ubuntu 22.04/24.04
#
# 首次部署：
#   1. cp deploy/deploy.env.example deploy/deploy.env   # 填服务器地址与域名
#   2. bash deploy/deploy.sh init      # 装依赖/建库导数据/配 systemd+nginx/上传产物/启动
#   3. 域名解析生效后：bash deploy/deploy.sh cert   # 申请 HTTPS 证书 + 强制跳转
#   4. （建议）服务器开防火墙：sudo ufw allow OpenSSH &&
#      sudo ufw allow 80,443/tcp && sudo ufw enable
#
# 日常更新（本机开发完 → 发布）：
#   bash deploy/deploy.sh update        # 前后端一起（默认，构建+上传+重启）
#   bash deploy/deploy.sh update front  # 只发前端（不重启后端、不掉登录态）
#   bash deploy/deploy.sh update back   # 只发后端（会重启，登录态失效）
#
# 其他：
#   build   只构建本地产物（后端 jar + 前端 dist）
#   logs    跟随后端日志（journalctl -f）
#   status  查看后端 / nginx 运行状态
#
# 前提：本机可 ssh 登录服务器（推荐先 ssh-copy-id），服务器登录用户有免密 sudo。
# 服务器目录约定与 deploy/DEPLOY.md 一致：jar → /opt/blog，前端 → /var/www/ancauql-blog/dist。
# ============================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

ENV_FILE="deploy/deploy.env"
JAR_NAME="blog_backend-0.0.1-SNAPSHOT.jar"   # 与 pom.xml 的 version 保持一致
JAR_PATH="blog_backend/target/$JAR_NAME"
DIST_DIR="blog_frontend/vue/dist"
UPLOADS_DIR="blog_backend/uploads"
REMOTE_JAR="/opt/blog/blog_backend.jar"
REMOTE_DIST="/var/www/ancauql-blog/dist"
REMOTE_UPLOADS="/opt/blog/uploads"
REMOTE_ENV="/etc/blog/blog.env"

info() { printf '\033[1;34m[部署]\033[0m %s\n' "$*"; }
ok()   { printf '\033[1;32m[ OK ]\033[0m %s\n' "$*"; }
warn() { printf '\033[1;33m[提醒]\033[0m %s\n' "$*"; }
err()  { printf '\033[1;31m[错误]\033[0m %s\n' "$*" >&2; }
die()  { printf '\033[1;31m[错误]\033[0m %s\n' "$*" >&2; exit 1; }

load_env() {
    [ -f "$ENV_FILE" ] || die "未找到 $ENV_FILE：请复制 deploy/deploy.env.example 为 deploy/deploy.env 并填写服务器信息"
    # shellcheck disable=SC1090
    . "$ENV_FILE"
    [ -n "${DEPLOY_HOST:-}" ] || die "deploy.env 缺少 DEPLOY_HOST（如 ubuntu@1.2.3.4）"
    [ -n "${DEPLOY_DOMAIN:-}" ] || die "deploy.env 缺少 DEPLOY_DOMAIN（你的域名）"
    DEPLOY_SSH_PORT="${DEPLOY_SSH_PORT:-22}"
}

# 仅对 ssh/scp 关闭 MSYS 路径转换：远程命令里的 /xxx 不能被当成本地 Windows 路径改写；
# 不全局关闭是因为 mvn.cmd 反而需要正常传参
SSH()  { MSYS_NO_PATHCONV=1 MSYS2_ARG_CONV_EXCL='*' ssh -o ConnectTimeout=10 -p "$DEPLOY_SSH_PORT" "$DEPLOY_HOST" "$@"; }
SCPC() { MSYS_NO_PATHCONV=1 MSYS2_ARG_CONV_EXCL='*' scp -o ConnectTimeout=10 -P "$DEPLOY_SSH_PORT" "$@"; }

find_mvn() {
    if command -v mvn >/dev/null 2>&1; then command -v mvn; return 0; fi
    local found
    found="$(find "$HOME/.m2/wrapper/dists" -name mvn.cmd 2>/dev/null | head -n 1)"
    [ -n "$found" ] || die "未找到 Maven：PATH 无 mvn，且 $HOME/.m2/wrapper/dists 下无发行版"
    printf '%s' "$found"
}

build_backend() {
    info "构建后端（Maven 打包，跳过测试）..."
    local mvn pom
    mvn="$(find_mvn)"
    # mvn.cmd 是 Windows 程序，需要 C:/ 风格路径（cygpath -m）；非 Git Bash 环境原样回退
    pom="$(cygpath -m "$ROOT/blog_backend/pom.xml" 2>/dev/null || echo "$ROOT/blog_backend/pom.xml")"
    "$mvn" -q -f "$pom" -DskipTests clean package
    [ -f "$JAR_PATH" ] || die "后端打包失败：未生成 $JAR_PATH"
}

build_frontend() {
    info "构建前端（npm run build，使用 .env.production 的 /api 基址）..."
    (cd "$ROOT/blog_frontend/vue" && { [ -d node_modules ] || npm ci; } && npm run build)
    [ -d "$DIST_DIR" ] || die "前端构建失败：未生成 $DIST_DIR"
}

upload_backend() {
    info "上传后端 jar（服务器保留上一版为 blog_backend.jar.bak）..."
    SCPC "$JAR_PATH" "$DEPLOY_HOST:/tmp/$JAR_NAME"
    SSH "sudo cp $REMOTE_JAR $REMOTE_JAR.bak 2>/dev/null || true; sudo install -m 644 /tmp/$JAR_NAME $REMOTE_JAR && rm -f /tmp/$JAR_NAME"
}

upload_frontend() {
    info "上传前端静态文件..."
    # --no-same-owner：MSYS tar 打包的 uid 是 Windows 映射值，解包时忽略，保持 root 属主
    tar -C "$DIST_DIR" -cf - . | SSH "sudo mkdir -p $REMOTE_DIST && sudo tar --no-same-owner -C $REMOTE_DIST -xf -"
}

upload_uploads() {
    if [ ! -d "$UPLOADS_DIR" ]; then
        warn "本机无 $UPLOADS_DIR，跳过图片同步"
        return 0
    fi
    info "同步本机图片到服务器（只新增/覆盖，不删除服务器上的）..."
    tar -C "$UPLOADS_DIR" -cf - . | SSH "sudo mkdir -p $REMOTE_UPLOADS && sudo tar --no-same-owner -C $REMOTE_UPLOADS -xf - && sudo chown -R blog:blog $REMOTE_UPLOADS"
}

restart_backend() {
    info "重启后端并探活（/hello）..."
    SSH "sudo systemctl restart blog-backend"
    local i
    for i in $(seq 1 30); do
        if SSH "curl -fsS http://127.0.0.1:9999/hello >/dev/null 2>&1"; then
            ok "后端已启动"
            return 0
        fi
        sleep 1
    done
    err "后端 30 秒内未通过健康检查，最近日志："
    SSH "sudo journalctl -u blog-backend -n 40 --no-pager" || true
    die "请根据日志排查后重试"
}

check_site_from_local() {
    local base="${DEPLOY_SITE_URL:-https://$DEPLOY_DOMAIN}"
    local code
    code="$(curl -s -o /dev/null -w '%{http_code}' --max-time 10 "$base/" || true)"
    if [ "$code" = "200" ]; then
        ok "公网验证：$base/ → 200"
    else
        warn "本机访问 $base/ 返回 ${code:-不可达}（若尚未配 DNS/证书/防火墙，属正常）"
    fi
}

# ---------------------------------------------------------------- 子命令 ----

cmd_build() {
    build_backend
    build_frontend
    ok "构建完成：$JAR_PATH 与 $DIST_DIR"
}

cmd_update() {
    local what="${1:-all}"
    case "$what" in
        front|back|all) ;;
        *) die "用法：deploy.sh update [front|back|all]，默认 all" ;;
    esac
    load_env

    if ! git diff --quiet 2>/dev/null || ! git diff --cached --quiet 2>/dev/null; then
        warn "工作区有未提交改动，将按当前文件内容部署"
    fi

    case "$what" in
        front)
            build_frontend
            upload_frontend
            ;;
        back)
            build_backend
            upload_backend
            upload_uploads
            restart_backend
            ;;
        all)
            build_backend
            build_frontend
            upload_backend
            upload_frontend
            upload_uploads
            restart_backend
            ;;
    esac
    check_site_from_local
    ok "更新完成（$what）"
}

cmd_init() {
    load_env
    info "目标：$DEPLOY_HOST  域名：$DEPLOY_DOMAIN  SSH 端口：$DEPLOY_SSH_PORT"

    info "步骤 1/8 检查 SSH 连通与免密 sudo..."
    SSH "echo ok" >/dev/null || die "无法 SSH 登录 $DEPLOY_HOST（先确认 ssh 能通，推荐 ssh-copy-id）"
    SSH "sudo -n true" >/dev/null 2>&1 || die "服务器登录用户需要免密 sudo（云主机默认用户通常满足）"

    info "步骤 2/8 安装系统依赖（nginx / mysql-server / JRE17 / certbot / curl）..."
    SSH "sudo apt-get update -qq && sudo apt-get install -y -qq nginx mysql-server openjdk-17-jre-headless curl"
    SSH "sudo apt-get install -y -qq certbot python3-certbot-nginx || sudo snap install --classic certbot" || warn "certbot 安装失败，可稍后手动安装再运行 deploy.sh cert"

    info "步骤 3/8 创建运行用户与目录..."
    SSH "sudo useradd -r -s /usr/sbin/nologin blog 2>/dev/null || true; sudo mkdir -p /opt/blog/uploads /var/www/ancauql-blog/dist /etc/blog"

    info "步骤 4/8 初始化数据库..."
    # 应用账号密码：已有 blog.env 就复用，否则生成随机密码
    local db_pass
    db_pass="$(SSH "sudo cat $REMOTE_ENV 2>/dev/null | grep '^DB_PASSWORD=' | head -n1 | cut -d= -f2- || true")"
    if [ -z "$db_pass" ]; then
        db_pass="$(openssl rand -hex 16)"
        info "已为 blog_app 生成随机密码（只保存在服务器 $REMOTE_ENV）"
    fi
    { sed "s/换成强密码/$db_pass/" deploy/mysql/create-app-user.sql
      echo "ALTER USER 'blog_app'@'localhost' IDENTIFIED BY '$db_pass';"
    } | SSH "sudo mysql"
    ok "数据库账号 blog_app 就绪（仅 blog_system 库 CRUD 权限）"

    # 导入本机数据：服务器库里已有表则跳过（init 可安全重跑）
    local tbl
    tbl="$(SSH "sudo mysql -N -e \"SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='blog_system'\"")"
    if [ "${tbl:-0}" -gt 0 ]; then
        warn "服务器 blog_system 已有 ${tbl} 张表，跳过数据导入（如需重灌请手动处理）"
    else
        info "从本机 MySQL 导出 blog_system 全量数据..."
        # dev-env.bat 为 GBK+CRLF；set 行是纯 ASCII，按行提取即可
        local dev_user dev_pass
        dev_user="$(sed -n 's/\r$//;s/^set "DB_USERNAME=\([^"]*\)"$/\1/p' dev-env.bat | head -n 1)"
        dev_pass="$(sed -n 's/\r$//;s/^set "DB_PASSWORD=\([^"]*\)"$/\1/p' dev-env.bat | head -n 1)"
        [ -n "$dev_pass" ] || die "未能从 dev-env.bat 读取 DB_PASSWORD：请确认根目录存在该文件且格式为 set \"DB_PASSWORD=...\""
        dev_user="${dev_user:-root}"
        MYSQL_PWD="$dev_pass" mysqldump --single-transaction --quick -u "$dev_user" blog_system | SSH "sudo mysql blog_system"
        ok "本机数据已导入服务器（含账号与文章，admin 密码与本机一致）"
    fi

    info "步骤 5/8 写入后端环境变量 /etc/blog/blog.env..."
    local tmp_env
    tmp_env="$(mktemp)"
    cat > "$tmp_env" <<EOF
DB_URL=jdbc:mysql://localhost:3306/blog_system?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
DB_USERNAME=blog_app
DB_PASSWORD=$db_pass
BLOG_SITE_URL=${DEPLOY_SITE_URL:-https://$DEPLOY_DOMAIN}
CORS_ALLOWED_ORIGINS=
EOF
    SCPC "$tmp_env" "$DEPLOY_HOST:/tmp/blog.env.init"
    SSH "sudo install -m 600 -o root -g root /tmp/blog.env.init $REMOTE_ENV && rm -f /tmp/blog.env.init"
    rm -f "$tmp_env"

    info "步骤 6/8 安装 systemd 服务与 nginx 配置（先 HTTP，证书用 deploy.sh cert 补）..."
    SCPC deploy/systemd/blog-backend.service "$DEPLOY_HOST:/tmp/"
    SSH "sudo install -m 644 /tmp/blog-backend.service /etc/systemd/system/ && sudo systemctl daemon-reload && sudo chown -R blog:blog /opt/blog"

    # nginx：限流区 + 反代公共头 + HTTP 引导配置（cert 阶段会替换成完整 HTTPS 配置）
    local tmp_conf
    tmp_conf="$(mktemp)"
    cat > "$tmp_conf" <<'EOF'
# 由 deploy/deploy.sh init 生成的 HTTP 引导配置；deploy.sh cert 会替换为完整 HTTPS 配置
server {
    listen 80;
    listen [::]:80;
    server_name __DOMAIN__;

    client_max_body_size 12m;

    add_header X-Content-Type-Options nosniff always;
    add_header X-Frame-Options SAMEORIGIN always;
    add_header Referrer-Policy strict-origin-when-cross-origin always;

    root /var/www/ancauql-blog/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # index.html 禁缓存，保证发版后浏览器立刻拿到新页面（本 location 需重复安全头）
    location = /index.html {
        add_header X-Content-Type-Options nosniff always;
        add_header X-Frame-Options SAMEORIGIN always;
        add_header Referrer-Policy strict-origin-when-cross-origin always;
        add_header Cache-Control "no-cache";
    }

    location = /api/auth/login {
        limit_req zone=blog_login burst=5 nodelay;
        proxy_pass http://127.0.0.1:9999/auth/login;
        include /etc/nginx/conf.d/blog-proxy-common.inc;
    }
    location = /api/auth/register {
        limit_req zone=blog_login burst=3 nodelay;
        proxy_pass http://127.0.0.1:9999/auth/register;
        include /etc/nginx/conf.d/blog-proxy-common.inc;
    }
    # ^~：防止下面的静态资源正则抢占 /api/、/uploads/ 前缀（否则 /uploads/x.png 会 404）
    location ^~ /api/ {
        proxy_pass http://127.0.0.1:9999/;
        include /etc/nginx/conf.d/blog-proxy-common.inc;
    }
    location ^~ /uploads/ {
        proxy_pass http://127.0.0.1:9999/uploads/;
        include /etc/nginx/conf.d/blog-proxy-common.inc;
    }
    location = /feed.xml {
        proxy_pass http://127.0.0.1:9999/feed.xml;
        include /etc/nginx/conf.d/blog-proxy-common.inc;
    }
    location = /sitemap.xml {
        proxy_pass http://127.0.0.1:9999/sitemap.xml;
        include /etc/nginx/conf.d/blog-proxy-common.inc;
    }
    location = /robots.txt {
        proxy_pass http://127.0.0.1:9999/robots.txt;
        include /etc/nginx/conf.d/blog-proxy-common.inc;
    }

    location ~* \.(js|css|png|jpg|jpeg|gif|webp|svg|woff2?)$ {
        expires 30d;
        add_header Cache-Control "public, max-age=2592000";
        try_files $uri =404;
    }
}
EOF
    sed -i "s/__DOMAIN__/$DEPLOY_DOMAIN/" "$tmp_conf"
    SCPC "$tmp_conf" "$DEPLOY_HOST:/tmp/blog-site.conf.bootstrap"
    SCPC deploy/nginx/00-blog-limit.conf "$DEPLOY_HOST:/tmp/"
    SCPC deploy/nginx/blog-proxy-common.inc "$DEPLOY_HOST:/tmp/"
    SSH "sudo install -m 644 /tmp/00-blog-limit.conf /tmp/blog-proxy-common.inc /etc/nginx/conf.d/ && sudo install -m 644 /tmp/blog-site.conf.bootstrap /etc/nginx/conf.d/blog-site.conf && sudo rm -f /tmp/blog-site.conf.bootstrap /tmp/00-blog-limit.conf /tmp/blog-proxy-common.inc /tmp/blog-backend.service /etc/nginx/sites-enabled/default && sudo nginx -t && sudo systemctl reload nginx"
    rm -f "$tmp_conf"

    info "步骤 7/8 构建并上传产物（jar + 前端 dist + 图片）..."
    build_backend
    build_frontend
    upload_backend
    upload_frontend
    upload_uploads

    info "步骤 8/8 启动服务与安装每日备份..."
    SSH "sudo systemctl enable blog-backend >/dev/null"
    restart_backend

    # 备份脚本 + 每日 3 点 cron（幂等：已有条目则跳过）
    SCPC deploy/backup/mysql-backup.sh "$DEPLOY_HOST:/tmp/"
    SSH "sudo install -m 755 /tmp/mysql-backup.sh /usr/local/bin/ && rm -f /tmp/mysql-backup.sh"
    SSH "sudo crontab -l 2>/dev/null | grep -q mysql-backup.sh || (sudo crontab -l 2>/dev/null; echo '0 3 * * * /usr/local/bin/mysql-backup.sh') | sudo crontab -"

    info "验证 nginx → 后端链路（服务器本机）..."
    SSH "curl -fsS -o /dev/null http://127.0.0.1/ && curl -fsS http://127.0.0.1/api/hello" >/dev/null
    ok "初始化完成！DNS 解析生效后 http://$DEPLOY_DOMAIN 即可访问"

    echo
    echo "==================================================================="
    echo " 剩余步骤（需要人工确认）："
    echo "  1. 域名 A 记录 → 服务器公网 IP（生效后继续下一步）"
    echo "  2. bash deploy/deploy.sh cert     # 申请证书 + 切换 HTTPS + 强制跳转"
    echo "  3. 服务器防火墙：sudo ufw allow OpenSSH && sudo ufw allow 80,443/tcp && sudo ufw enable"
    echo "  4. 建议跑一次：sudo mysql_secure_installation 与 sudo /usr/local/bin/mysql-backup.sh"
    echo "  5. 后台登录账号密码与本机一致（admin 密码是你本机改过的那个）"
    echo "==================================================================="
}

cmd_cert() {
    load_env
    info "申请证书：$DEPLOY_DOMAIN（前提：域名 A 记录已指向服务器公网 IP）"
    # certbot 首次运行需要交互输入邮箱/同意条款，用 -t 分配 TTY
    MSYS_NO_PATHCONV=1 MSYS2_ARG_CONV_EXCL='*' ssh -t -o ConnectTimeout=10 -p "$DEPLOY_SSH_PORT" "$DEPLOY_HOST" \
        "sudo certbot certonly --nginx -d $DEPLOY_DOMAIN" \
        || die "certbot 未成功（检查 DNS 是否生效、80 端口是否可从公网访问）"

    SSH "sudo test -d /etc/letsencrypt/live/$DEPLOY_DOMAIN" \
        || die "未找到证书目录 /etc/letsencrypt/live/$DEPLOY_DOMAIN"

    info "切换 nginx 到完整 HTTPS 配置（仓库 deploy/nginx/blog-site.conf，含 80→443 跳转）..."
    local tmp_conf
    tmp_conf="$(mktemp)"
    sed "s/your-domain\.com/$DEPLOY_DOMAIN/g" deploy/nginx/blog-site.conf > "$tmp_conf"
    SCPC "$tmp_conf" "$DEPLOY_HOST:/tmp/blog-site.conf.full"
    SSH "sudo install -m 644 /tmp/blog-site.conf.full /etc/nginx/conf.d/blog-site.conf && rm -f /tmp/blog-site.conf.full && sudo nginx -t && sudo systemctl reload nginx"
    rm -f "$tmp_conf"

    info "验证 HTTPS（服务器本机，绕过 DNS）..."
    SSH "curl -fsS --resolve $DEPLOY_DOMAIN:443:127.0.0.1 -o /dev/null https://$DEPLOY_DOMAIN/" \
        || die "HTTPS 本机验证失败：sudo journalctl -u nginx -n 20 查看 nginx 日志"
    ok "服务器侧 HTTPS 正常"

    check_site_from_local
    echo
    echo " 建议再执行："
    echo "   sudo certbot renew --dry-run        # 验证证书自动续期"
    echo "   sudo ufw allow OpenSSH && sudo ufw allow 80,443/tcp && sudo ufw enable"
}

cmd_logs() {
    load_env
    SSH "sudo journalctl -u blog-backend -f --no-pager -n 100"
}

cmd_status() {
    load_env
    SSH "sudo systemctl status blog-backend --no-pager -l; echo; sudo systemctl is-active nginx && echo 'nginx: active'"
}

usage() {
    # 打印文件头部（两个 “# ====” 注释行之间）的说明
    sed -n '/^# =\{10,\}/,/^# =\{10,\}/p' "$0" | sed 's/^#\{1\} \{0,1\}//'
}

main() {
    local cmd="${1:-help}"
    [ $# -gt 0 ] && shift || true
    case "$cmd" in
        init)   cmd_init "$@" ;;
        update) cmd_update "$@" ;;
        build)  cmd_build ;;
        cert)   cmd_cert ;;
        logs)   cmd_logs ;;
        status) cmd_status ;;
        help|-h|--help) usage ;;
        *) usage; exit 1 ;;
    esac
}

main "$@"
