# 常用指令手册（新手版）

> 给站主日常查用的速查表：遇到什么场景，翻到对应小节，整行复制命令即可。
> 除标注「双击」的操作外，所有命令都在 **Git Bash** 里敲（开始菜单搜 "Git Bash"）。
> 更技术的细节见 AGENTS.md（AI/开发者交接手册）与 deploy/DEPLOY.md（部署原理）。

## 0. 先建立一个总印象：两台机器

- **本机**（你的电脑）：写代码的地方。代码以这里为准。
- **服务器**（腾讯云 82.156.129.27）：访客看到的地方。文章、评论、图片数据以这里为准。

平时只关心两个方向的流动：

| 方向 | 命令 | 拉动什么 |
|---|---|---|
| 本机 → 服务器 | `update` | 代码（新功能、改样式） |
| 服务器 → 本机 | `pull` | 数据（文章、图片，覆盖本机） |

## 1. 命令敲在哪？

| 打开方式 | 用来做什么 |
|---|---|
| 资源管理器双击 `start-dev.bat` / `stop-dev.bat` | 启动 / 停止本机开发环境，不用敲命令 |
| 开始菜单搜 "Git Bash" | 本文其余所有命令 |

Git Bash 打开后，**每次新窗口**先复制这行进入项目目录：

```bash
cd /c/Workspace/GitHub-AncauqL/AncauqL_blog
```

## 2. 本机开发：启动 / 停止 / 访问

| 场景 | 操作 |
|---|---|
| 想在本机看网站、改代码 | 双击 `start-dev.bat`，等两个黑窗口就绪（后端窗口出现 `Started BlogBackendApplication`） |
| 用完了 | 双击 `stop-dev.bat`（或直接关掉那两个窗口） |

启动成功后访问：

- 前台首页：http://localhost:8081
  （8080 被本机其他软件占用，Vue 会自动挪到 8081——**以前端黑窗口打印的地址为准**）
- 后台登录：http://localhost:8081/#/login
  （初始账号 admin / 123456；若本机库是从线上 pull 回来的，就用线上账号密码）
- 后端自检：浏览器打开 http://localhost:9999/hello 有响应即正常

## 3. 改完代码、提交之前：两条验证命令

平时开发交给 AI agent 时它们会自己跑；**只有你亲手改了代码**才需要手动跑这两条，全绿才算过：

```bash
# 后端编译检查（这串路径就是本机 Maven 的实际位置，别背，复制）
"$USERPROFILE/.m2/wrapper/dists/apache-maven-3.9.16-bin/5grr65jo27hi51sujmtcldfovl/apache-maven-3.9.16/bin/mvn.cmd" -q -f "C:/Workspace/GitHub-AncauqL/AncauqL_blog/blog_backend/pom.xml" test-compile

# 前端构建检查
cd /c/Workspace/GitHub-AncauqL/AncauqL_blog/blog_frontend/vue && npm run build
```

跑完第二条记得回根目录：`cd /c/Workspace/GitHub-AncauqL/AncauqL_blog`

## 4. 发布到服务器（最常用！）

按「你刚干了什么」对号入座（都在仓库根目录敲）：

| 刚做了什么 | 敲什么 | 会发生什么 |
|---|---|---|
| 改了页面 / 样式 / 前端逻辑 | `bash deploy/deploy.sh update front` | 只发前端；后端不重启，线上登录态不掉 |
| 改了 Java 后端 / 接口 | `bash deploy/deploy.sh update back` | 重启后端（登录态会失效），约 1 分钟 |
| 前后端都改了 / 拿不准 | `bash deploy/deploy.sh update` | 全量构建+上传+重启，最稳 |
| 想看服务器后端日志 | `bash deploy/deploy.sh logs` | 实时滚动；按 Ctrl+C 退出 |
| 想看服务器跑没跑着 | `bash deploy/deploy.sh status` | 后端 + nginx 状态一览 |

一次性操作（首次部署 `init`、域名备案后上 HTTPS 的 `cert`）日常用不到，
需要时见 `deploy/DEPLOY.md`。

## 5. 线上数据拉回本机

场景：想在本机用**真实文章**调试，或手动把数据备份到本机。

```bash
bash deploy/deploy.sh pull      # 会要求输入 yes 确认；加 -y 免确认
```

- 方向是 **服务器 → 本机**，会覆盖本机的 `blog_system` 库和 uploads 图片；
  本机独有的测试文章/数据会丢（服务器不受影响）。
- **反方向（把本机库导到服务器）是禁止的**——写文章请直接在线上后台写。

## 6. Git 最小生存包

平时让 AI agent 干活时基本用不到；自己动手时按这个顺序：

```bash
git status                # 看板：哪些文件改了 / 待提交
git log --oneline -5      # 最近 5 条提交
git add <文件名>           # 把某文件加入待提交
git commit -m "说明"       # 提交到本地
```

**铁律：默认永远不 `git push`。** push 会把本地提交同步到 GitHub，
只有你亲口说「推送」时才执行。commit 只存在本机，随便提交不伤任何人。

## 7. 常见问题速查

| 症状 | 原因 / 办法 |
|---|---|
| http://localhost:8080 打不开 | 本机前端实际在 **8081**；看前端黑窗口打印的地址 |
| start-dev.bat 窗口中文乱码 | 正常现象（bat 是 GBK 编码），功能不受影响 |
| 后端 9999 报端口占用 | 可能已有一个实例在跑：先双击 stop-dev.bat，再重新启动 |
| deploy.sh 连不上服务器 | 看 `deploy/deploy.env` 地址对不对；服务器是否在线；本机网络/代理 |
| pull 之后本机后台登录不上 | pull 把线上账号拉回来了，用**线上**的账号密码登本机即可 |
| 忘记本机 MySQL 密码 | 根目录 `dev-env.bat` 里写着（该文件已被 git 忽略，不会上传） |
| update 报构建失败 | 九成是代码本身编译不过：回 §3 跑两条验证命令看报错 |
