#!/usr/bin/env bash
# MySQL 备份脚本：每天备份 blog_system 到 /var/backups/blog，保留 14 天
# 建议通过 root crontab 执行：0 3 * * * /usr/local/bin/mysql-backup.sh
# 密码来源：/etc/blog/blog.env 里的 DB_PASSWORD（文件权限 600）

set -euo pipefail

ENV_FILE="/etc/blog/blog.env"
BACKUP_DIR="/var/backups/blog"
DB_NAME="blog_system"
DB_USER="blog_app"
KEEP_DAYS=14

# shellcheck disable=SC1090
[ -f "$ENV_FILE" ] && . "$ENV_FILE"

if [ -z "${DB_PASSWORD:-}" ]; then
  echo "DB_PASSWORD 未设置（检查 $ENV_FILE）" >&2
  exit 1
fi

mkdir -p "$BACKUP_DIR"
STAMP="$(date +%Y%m%d-%H%M%S)"
OUT="$BACKUP_DIR/${DB_NAME}-${STAMP}.sql.gz"

MYSQL_PWD="$DB_PASSWORD" mysqldump \
  --single-transaction --quick --routines --events \
  -u "$DB_USER" "$DB_NAME" | gzip > "$OUT"

echo "backup done: $OUT"

# 清理过期备份
find "$BACKUP_DIR" -name "${DB_NAME}-*.sql.gz" -type f -mtime "+${KEEP_DAYS}" -delete
