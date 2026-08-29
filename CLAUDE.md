# CLAUDE.md

**先读根目录的 `AGENTS.md`**——那是本仓库唯一的交接手册：本机环境的坑、代码约定、
验证命令、API 表、路线图全在里面。本文件只重复最高频的几条硬规则：

- 禁止 `git push`（用户明确要求时除外）；本地 commit / merge 正常做。
- 数据库密码只在 `dev-env.bat`（已 gitignore），任何会提交的文件里不得出现密码。
- `*.bat` 是 GBK+CRLF 编码，直接用工具写会乱码，改法见 AGENTS.md §4。
- e2e 测试用 9998 端口（`SERVER_PORT=9998`），别抢 9999；8080 上的
  `ApplicationWebServer` 是无关软件，不要杀它。
- Maven 不在 PATH，路径见 AGENTS.md §5；提交前必跑前端 build + 后端 test-compile。
- 每完成一个大功能：更新 PROJECT_OVERVIEW.md 与 AGENTS.md，然后停下向用户汇报。
