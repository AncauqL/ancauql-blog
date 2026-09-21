# AGENTS.md — AncauqL Blog 开发交接手册

> **任何 AI agent 或开发者接手本仓库前，必须先通读本文件。**
> 本文件的目标：让能力较弱的模型也能安全、正确地继续开发。所有本机环境的坑、
> 项目约定、验证命令、后续规划都在这里显式写死。**每完成一个任务必须回来更新本文件,向其他agent同步目前的进度。**

最后更新：2026-09-21（前批：…、**评论增强**、**部署脚本 deploy.sh**；本批：**部署落地（进行中）+ IP 先行模式**——站主服务器已到位：腾讯云大陆 Ubuntu 24.04（82.156.129.27），域名 ancauql.com（计划用 blog.ancauql.com 子域），**未备案**（备案由站主在腾讯云控制台提交，约 1-2 周）。deploy.sh 新增可选 `DEPLOY_SITE_URL`：备案前 `DEPLOY_DOMAIN` 填 IP + `DEPLOY_SITE_URL=http://IP` 走 HTTP；备案通过后改回域名、重跑 init + cert。当前 `deploy/deploy.env`（本机、gitignore）已按 IP 模式填好，等站主配好 SSH 免密与安全组 80/443 后跑 init。注意：站主本机跑着 Clash 类代理（DNS fake-ip），本机 nslookup 域名全是 198.18.x 假 IP，判断真实解析要去注册商 DNS 控制台或用在线工具）

---

## 1. 项目一句话

前后端分离的个人博客：Spring Boot 3.5 (Java 17, MyBatis-Plus, MySQL 8) + Vue 2 (Element UI)。
定位：**站主长期使用的主力博客**，不是课程演示。质量要求按真实产品对待。

## 2. 目录地图

```text
AncauqL_blog/
├─ AGENTS.md                  ← 本文件（交接手册 + 规划），必读必维护
├─ CLAUDE.md                  ← Claude Code 自动加载的入口，指向本文件
├─ PROJECT_OVERVIEW.md        ← 正式项目说明（功能/接口/启动），改功能后必须同步
├─ ITERATION_BASE.md          ← 本地迭代笔记（已 gitignore，只在本机存在）
├─ start-dev.bat / stop-dev.bat / dev-env.example.bat  ← 一键启动/停止脚本（GBK 编码！）
├─ dev-env.bat                ← 本机数据库密码（gitignore，勿提交勿外传）
├─ deploy/                    ← 服务器部署材料：deploy.sh（一键部署/更新脚本）+ deploy.env.example
│                                + DEPLOY.md + nginx/ + systemd/ + mysql/ + backup/
├─ blog_backend/              ← Spring Boot，端口 9999
│  └─ src/main/java/com/example/blog_backend/
│     ├─ controller/          ← Hello / Auth / Article / Category / Tag / Comment / About / Site / Visit / User / File / Feed / Seo
│     ├─ service/ + impl/     ← 业务层
│     ├─ mapper/              ← MyBatis-Plus Mapper（基本无 XML，UserMapper.xml 除外）
│     ├─ entity/ dto/         ← 实体与传输对象（dto 含 ArchiveGroup / ArticleNeighbors）
│     ├─ common/              ← Result / AuthContext / PasswordUtil / RoleUtil / VisitorKeyUtil
│     └─ config/              ← WebConfig（拦截器+静态资源）/ AuthInterceptor / MybatisPlusConfig
├─ blog_frontend/vue/         ← Vue 2 + Element UI，开发端口 8080（本机实际 8081，见 §5）
│  └─ src/
│     ├─ App.vue              ← 顶层布局切换器（按 $route.meta.layout 选前后台布局）
│     ├─ layouts/             ← FrontLayout（顶栏+页脚）/ AdminLayout（侧边栏）
│     ├─ config/site.js       ← 站点信息【代码默认值】（站名/简介/Hero/社交/AboutMe 资料）
│     ├─ store/site.js        ← 站点信息运行时状态 siteState（默认值 + 后台 site_config 合并）
│     ├─ utils/request.js     ← axios 实例，导出 API_BASE / resolveAsset
│     ├─ utils/auth.js        ← 登录态工具（getStoredUser / isManager / logout 等）
│     ├─ utils/markdown.js    ← Markdown 渲染管线（渲染/高亮/消毒/字数统计），渲染必须复用它
│     ├─ utils/seo.js         ← 按路由改 title/description/OG/canonical（SPA 客户端 SEO）
│     ├─ assets/css/markdown.css ← 正文排版样式（markdown.js 引入，详情页+编辑器共用）
│     ├─ router/index.js      ← 路由 + 登录/角色守卫 + meta.layout
│     └─ views/               ← HomeView / Archive / SearchView / ArticleDetail / ArticleEditor / Article / Category / Tag / CommentAdmin / Dashboard / AboutMe / AboutEditor / SiteEditor / AccountSettings / User / Login / Register
└─ database/blog_system.sql   ← 主库脚本（含种子数据）
```

## 3. 当前完成状态（按里程碑）

- [x] **M1 稳定版**：文章/分类/账号 CRUD、三级权限（游客/ADMIN/SUPER_ADMIN）、登录 Token
- [x] **M2 可写作版**（2026-08-29 完成）：
  - Markdown 渲染（markdown-it + highlight.js + DOMPurify），代码块带语言标签+复制按钮
  - 详情页：目录 TOC + 滚动高亮、字数/阅读时长、上一篇/下一篇、阅读量自增（游客访问已发布文章才计数）
  - 全屏分栏 Markdown 编辑器 `/article/edit/:id?`：实时预览、粘贴/拖拽图片自动上传、
    工具栏、Tab 缩进、Ctrl+S、本地草稿自动保存与恢复、离开确认
  - 图片上传 `POST /file/upload` → 本地 `./uploads`，`/uploads/**` 静态访问
  - 首页与文章管理均为服务端分页；列表接口不返回 content 大字段
  - 一键启动/停止脚本
- [x] **M3 可上线版**：
  - [x] ④ 前台换脸（2026-08-31 完成）：
    - 布局拆分：`App.vue` 只做切换器，`layouts/FrontLayout.vue`（顶部极简导航+页脚）
      与 `layouts/AdminLayout.vue`（深色侧边栏）按 `$route.meta.layout` 渲染
    - 站点信息常量 `config/site.js`（站名/作者/slogan/备案号，M4 改配置表）
    - 登录态工具 `utils/auth.js`（getStoredUser/isManager/isSuperAdmin/logout 等共用）
    - 首页：门面区（站名+slogan）、分类筛选条（服务端 categoryId 过滤）、封面缩略图卡片
    - 详情页：cover 头图 + 分类名展示
    - 归档页 `/archive` + 新接口 `GET /article/archive`（按年份分组倒序，仅已发布）
    - 路由全部前台化：`/post/:id` 详情、`/archive`、`/aboutme`、`/login` 均无管理布局
    - 移动端 ≤640px 适配（导航收纳、封面缩小）
  - 注：原规划项 M3-⑤「安全硬化 + 部署上线」已于 2026-09-03 按站主意愿移除，不再执行（背景见 §13 决策表）
- [x] **M4 长期增强**：RSS / 评论 / 标签 / 站内搜索 / 站点配置表 / 访问统计 / sitemap+SEO 全部完成（2026-09-03 ~ 2026-09-11）
- sitemap 与 SEO（2026-09-11 完成）：后端 `GET /sitemap.xml`（首页 + 归档 + 关于我 + 每篇已发布文章含 lastmod + 有文章的标签页 `/?tag=id`，自己声明 `application/xml`）与 `GET /robots.txt`（放行前台、屏蔽后台与 /search、声明 Sitemap 绝对地址），站点基址复用 `blog.site-url`；前端 `utils/seo.js` 在首页/文章页/归档/关于我/搜索/404 里改 `document.title`、description、OG/twitter 卡片、canonical，搜索页与 404 标 `noindex`，草稿详情页也标 noindex；`public/index.html` 补静态兜底 meta（lang=zh-CN、description、theme-color、og 默认值）与 noscript 提示；nginx 配置增加 `/sitemap.xml`、`/robots.txt` 反代
- 访问统计 + 数据看板（2026-09-11 完成）：`visit_log` 表按天存原始记录（只存 `md5(IP+UA+盐)` 访客标识，不落原始 IP）；`POST /visit` 公开上报（前台 `App.vue` 每次路由切换报一次，管理员自己的浏览由后端忽略，爬虫 UA 忽略，同访客同路径 3 秒内去重）；`GET /visit/dashboard?days=` 管理员返回总览(今日/区间/累计 PV·UV) + 每日趋势(缺日补 0) + 热门文章 Top10（文章被删则标题显示「（文章已删除）」）；后台新增「数据统计」页 `/dashboard`（纯 CSS 双色柱状图，未引图表库）
- 站点信息后台可编辑（2026-09-10 完成）：`site_config` 单行表存 JSON；`GET /site` 公开、`PUT /site` 管理员（key 白名单 + 类型校验，超 64KB 拒绝）；前端 `store/site.js` 维护 `siteState`（`config/site.js` 默认值 + 后台配置按 key 合并，对象一层深合并、数组整段替换），`App.vue` 启动时拉一次；前台四页（FrontLayout/HomeView/AboutMe/AdminLayout）改读 siteState；后台新增「站点信息」页 `/site`（站名/作者/slogan/年份/备案号/Hero 三段/首页简介/肖像上传/AboutMe 资料/社交），支持一键「恢复代码默认」
- 站内搜索（2026-09-10 完成）：新接口 `GET /article/search`（标题/摘要/正文三处匹配，分页，列表不含 content，游客只搜已发布、管理员能搜到草稿）；前台新增 `/search?q=` 结果页（命中关键词高亮、分类/时间/标签、加载更多）；顶栏 Ctrl+K 覆盖层改为「前 6 条预览 + 查看全部 N 条结果 →」，回车直接进结果页；高亮用切片段渲染，不碰 v-html
- 标签系统（2026-09-10 完成）：`tag` + `article_tag` 两表（多对多）；`/tag` 接口（公开读、管理员写，重名/空名给出中文报错）；文章可挂多个标签（编辑器多选，可直接输入新名字回车即建）；后台「标签管理」页（`/tag`）增删改 + 各标签已发布文章数；首页标签筛选条（写进地址栏 `/?tag=id`，详情页标签可点进来）；首页卡片、文章管理列表、详情页均显示标签；标签按已发布文章计数（草稿不计）
- 体验/工程细节（2026-09-03 完成）：文章可后台“置顶”(is_top)→首页“置顶”大卡；优雅 404 页；文章图片懒加载；时间解析统一到 `utils/datetime.js`；归档条目改 `<router-link>`；`API_BASE` 走 `.env`(VUE_APP_API_BASE，见 .env.example)；前台阅读缩放(1.1，正文详情页再 1.1)
- 评论系统（2026-09-09 完成）：自建轻量评论——`comment` 表、详情页评论区（即发即显）、蜜罐字段 + IP 限流、后台“评论管理”页（列表/删除/跳原文）
- 评论增强：回复 + 点赞（2026-09-11 完成）：`comment` 增加 `parent_id`（回复统一挂到顶层评论，只做两级）/`reply_to_nickname`（冗余昵称，对方评论删了也能显示「回复 @某某」）/`like_count`；新增 `comment_like`（comment_id + visitor_key 复合主键）做点赞去重；`POST /comment/like` **公开**（未登录也能点，同访客再点即取消），`GET /comment` 按当前访客回填每条评论的 `liked` 并按「顶层 + 紧跟其回复」排序；点赞数走数据库端原子加减且不低于 0；删除顶层评论会**连带删除其回复与相关点赞**（前端确认框有提示）；后台评论管理页新增「类型（评论/回复）」与「点赞」列；访客标识统一抽到 `common/VisitorKeyUtil`（登录 `u{id}`、未登录 md5(IP+UA+盐)），访问统计也改用它
- 用户体系（2026-09-09 完成）：开放注册普通用户(角色 USER)——`/auth/register` 邮箱注册(蜜罐+限流)；评论改**登录后可发并绑定账号(user_id)**，删除=管理员或本人；修复 RoleUtil 将未知角色误归 ADMIN 的越权隐患；USER 无后台权限，SUPER_ADMIN/ADMIN 不受影响

## 4. 铁律（违反任何一条都算事故）

1. **禁止 `git push`**，除非用户明确说"推送"。本地 commit / merge 可以做。
2. **禁止把密码写进任何会提交的文件**。数据库密码只存在 `dev-env.bat`（已 gitignore）。
   文档、代码、提交信息中一律不出现真实密码。
3. **`*.bat` 文件是 GBK 编码 + CRLF**。工具写出的是 UTF-8，直接改会乱码。
   修改流程：用 UTF-8 写好 → `iconv -f UTF-8 -t GBK file | sed 's/\r*$/\r/' > tmp && mv tmp file`。
4. **不要动 8080 端口上的 `ApplicationWebServer` 进程**——那是站主机器上的无关常驻软件。
   杀进程必须"端口 + 进程名"双匹配（参考 stop-dev.bat 的写法）。
5. **e2e 测试用 9998 端口**（`SERVER_PORT=9998`），不要抢 9999——9999 可能跑着站主自己的实例。
6. **改了功能必须同步文档**：PROJECT_OVERVIEW.md（正式说明）+ 本文件 §3/§11/§12。
7. **提交前必须跑通两条验证命令**（见 §6），前端 build + 后端 test-compile 全绿才能 commit。
8. **每完成一个大功能就停下来向用户汇报**，不要连续做多个大步。
9. 用户内容（如根目录的 wireguard `.md` 文章）**不要擅自提交或删除**，归站主自己处置。
10. Markdown 渲染必须走 `utils/markdown.js`（含 DOMPurify 消毒），**禁止**在任何地方
    直接 `v-html` 未消毒的内容。

## 5. 本机环境事实（实测，直接用）

| 事实 | 值 |
|---|---|
| 操作系统 | Windows 11，工具 shell 是 Git Bash（MSYS） |
| Java | 21（PATH 可用），项目 target 17 |
| Node / npm | 22.x / 10.x（PATH 可用） |
| **Maven** | PATH 里**没有** mvn！用 `$USERPROFILE/.m2/wrapper/dists/apache-maven-3.9.16-bin/5grr65jo27hi51sujmtcldfovl/apache-maven-3.9.16/bin/mvn.cmd` |
| MySQL | 8.0.46 已作为服务运行；库 `blog_system`；密码在 `dev-env.bat`，CLI 用 `MYSQL_PWD=$(读取)` 方式传 |
| 端口 9999 | 后端；站主可能自己开着实例 |
| **端口 8080** | 被无关软件 `ApplicationWebServer`（提权进程）常驻占用 → **Vue 开发服务器实际落在 8081** |
| 端口 9998 | 约定给 agent 做 e2e 测试 |
| 初始账号 | admin / 123456（角色 SUPER_ADMIN） |

**Git Bash 调用 Windows 命令的坑（都踩过，别再踩）：**
- `cmd /c` 的 `/c` 会被 MSYS 转成路径 → 加前缀 `MSYS_NO_PATHCONV=1 cmd /c "..."`。
- 传给 bat 的 `/check` 之类参数同样会被转换 → 同上处理。
- PowerShell 内联脚本用 bash 双引号包裹时 `$var` 会被 bash 吃掉 → **外层用单引号**。
- bat 输出是 GBK，管道后接 `| iconv -f GBK -t UTF-8` 才能读。
- Bash 工具的工作目录会跨命令保留，`cd` 用绝对路径。

## 6. 常用命令（可直接复制）

```bash
MVN="$USERPROFILE/.m2/wrapper/dists/apache-maven-3.9.16-bin/5grr65jo27hi51sujmtcldfovl/apache-maven-3.9.16/bin/mvn.cmd"

# 后端编译验证（必跑）
"$MVN" -q -f "C:/Workspace/GitHub-AncauqL/AncauqL_blog/blog_backend/pom.xml" test-compile

# 前端构建验证（必跑）
cd /c/Workspace/GitHub-AncauqL/AncauqL_blog/blog_frontend/vue && npm run build

# e2e：在 9998 起后端（后台运行），密码从 dev-env.bat 拿
cd /c/Workspace/GitHub-AncauqL/AncauqL_blog/blog_backend \
  && export DB_USERNAME=root DB_PASSWORD=<见dev-env.bat> SERVER_PORT=9998 \
  && "$MVN" -q spring-boot:run

# e2e 常用请求
curl -s http://localhost:9998/hello
TOKEN=$(curl -s -X POST http://localhost:9998/auth/login -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:9998/article/selectPage?status=draft"

# 结束 e2e：只杀 9998 上的 java
powershell -NoProfile -Command 'Get-NetTCPConnection -LocalPort 9998 -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique | ForEach-Object { $p = Get-Process -Id $_ -ErrorAction SilentlyContinue; if ($p -and $p.ProcessName -eq "java") { Stop-Process -Id $_ -Force } }'

# MySQL 查询（密码不进命令行历史）
MYSQL_PWD=<见dev-env.bat> mysql -uroot -D blog_system -e "SELECT id,title,status FROM article;"

# 服务器部署/更新（Ubuntu；配置文件 deploy/deploy.env 本机自建，不提交）
bash deploy/deploy.sh init              # 首次部署（装依赖/建库导数据/systemd/nginx/上传/启动）
bash deploy/deploy.sh update            # 日常更新（可 update front / update back）
bash deploy/deploy.sh cert              # 域名解析生效后：证书 + HTTPS + 强制跳转
bash deploy/deploy.sh logs              # 跟随后端日志；status 查看运行状态
```

## 7. 代码约定（必须遵守的既有风格）

- **统一响应** `common/Result`：`{code, msg, data}`，**code 是字符串**（`'200'` / `'401'` / `'403'` / `'500'`）。
  前端判断一律 `res.code === '200'`（注意引号）。HTTP 状态码恒为 200，错误语义在 code 里。
- **时间字段**：后端 `LocalDateTime` 序列化后可能是数组 `[y,m,d,h,mi,s]` 或 ISO 字符串，
  前端 `formatTime` 两种都要兼容（现有页面有参考实现）。
- **文章状态**：只有 `published` / `draft` 两个值，字符串直存。
- **角色**：`SUPER_ADMIN` / `ADMIN`（后台）/ `USER`（注册的普通用户，仅可评论）/ 游客无账号。权限判断后端在 `AuthContext`（ThreadLocal），
  前端在 router meta.roles + localStorage `blog_user`。
- **分页返回**：MyBatis-Plus `IPage` 原样返回，前端取 `res.data.records` / `res.data.total`。
- **列表接口不返回 content**（大字段），需要正文时用 `/article/detail` 单查。
- **图片地址**：数据库和 Markdown 里存**相对路径** `/uploads/yyyyMM/xxx.ext`；
  前端渲染时由 `markdown.js`（正文）或 `resolveAsset()`（封面）拼上 `API_BASE`。
  换域名/部署只需改 `request.js` 的 `API_BASE`。
- **后端新增接口时**必须同步考虑 `AuthInterceptor` 的权限矩阵（放行/管理员/超管）。
- 前端消息提示用 `this.$message`，确认框用 `this.$confirm`，跟随现有页面写法。
- Java 代码风格：4 空格缩进、字段私有 + getter/setter、无 Lombok（保持现状，别引入）。

## 8. API 一览（基址 http://localhost:9999）

| 方法+路径 | 权限 | 说明 |
|---|---|---|
| GET /hello | 公开 | 健康检查 |
| GET /feed.xml | 公开 | RSS 2.0 订阅源：仅已发布文章、按 create_time desc（最多 50），文章链接按 `blog.site-url` 拼前台地址；标题用 `blog.site-title` |
| GET /sitemap.xml | 公开 | 站点地图：首页 / 归档 / 关于我 / 每篇已发布文章（带 lastmod）/ 有已发布文章的标签页 `/?tag=id`；Content-Type 为 application/xml；基址用 `blog.site-url` |
| GET /robots.txt | 公开 | 放行前台，Disallow 登录/注册/搜索/后台各页，末尾声明 `Sitemap: {site-url}/sitemap.xml`（绝对地址） |
| POST /auth/login | 公开 | `{username,password}` → `{token,user}`；管理员用账号、普通用户用注册邮箱(=username) |
| POST /auth/register | 公开 | 注册普通用户(USER)：email/nickname/password + 蜜罐 website + IP 限流；成功即登录 |
| GET /auth/me · POST /auth/logout | 任意登录 | 仅需登录(USER 亦可)；Token 在内存，重启失效 |
| POST /auth/profile | 任意登录 | 自助绑/改邮箱、改密码：`email`+`currentPassword`+`newPassword?`；验当前密码，邮箱全站唯一 |
| GET /article/search | 公开* | 站内搜索分页：pageNum=1, pageSize=10, keyword（必填，空关键词返回空页）；关键词同时匹配**标题/摘要/正文**；游客只搜已发布(仅 published)，管理员可搜到草稿；按 create_time desc, id desc；不含 content；records 带 `tagNames` |
| GET /article/selectAll | 公开* | 游客只见 published；管理员见全部（旧接口，新代码请用 selectPage） |
| GET /article/selectPage | 公开* | 参数全可选：pageNum=1, pageSize=10, articleTitle, status（status 仅管理员生效，游客恒 published）, categoryId, tagId（按标签筛选，命中 article_tag）；按 is_top desc, create_time desc, id desc；不含 content；records 额外带 `tagNames` |
| GET /article/detail?id= | 公开* | 草稿仅管理员可见(403)；游客访问已发布文章时 view_count 原子 +1，管理员预览不计数 |
| GET /article/neighbors?id= | 公开 | 已发布文章的上一篇/下一篇 `{prev:{id,title},next:{...}}`，按 create_time asc, id asc |
| GET /article/archive | 公开 | 归档：已发布文章按年分组 `[{year, articles:[{id,title,createTime}]}]`，年份与组内均倒序 |
| POST /article | 管理员 | 带 id 更新 / 无 id 新增；**返回带 id 的完整对象** |
| DELETE /article/delete?id= | 管理员 | |
| GET /comment?articleId= | 公开 | 某文章评论列表：顶层按时间升序、每条顶层后面紧跟它的回复；每条带 `parentId`/`replyToNickname`/`likeCount`/`liked`（liked 按当前访客回填） |
| POST /comment | 任意登录 | 发表评论/回复（需登录，USER 亦可）：`articleId/content/parentId?` + 蜜罐 website；IP 限流；昵称取账号；回复会校验上级存在且同文章并统一挂到顶层 |
| POST /comment/like | 公开 | 点赞/取消点赞 `{commentId}`：同一访客（登录 `u{id}`、未登录 md5(IP+UA+盐)）只能点一次，再点即取消；返回 `{likeCount, liked}` |
| GET /comment/list | 管理员 | 全部评论（新在前） |
| DELETE /comment/delete?id= | 任意登录 | 删除评论：管理员任意，普通用户仅本人；删顶层评论会连带删掉它的回复与相关点赞 |
| GET /about | 公开 | AboutMe 正文 Markdown（无则前端回退 site.js） |
| PUT /about | 管理员 | 保存 AboutMe 正文 Markdown（about_me 单行 upsert） |
| GET /tag/selectAll | 公开 | 标签列表 `[{id,name,count}]`（count=该标签下**已发布**文章数），按 name 升序 |
| POST /tag | 管理员 | 新建/改名 `{id?, name}`；空名、超 50 字、重名返回中文错误 msg |
| DELETE /tag/delete?id= | 管理员 | 删标签，并同时清掉 `article_tag` 里对应关联行 |
| GET /site | 公开 | 站点信息配置 JSON（站名/Hero/首页简介/社交/AboutMe 资料）；从未保存过返回 `{}`，前端按 key 回退 `config/site.js` 默认值 |
| PUT /site | 管理员 | 保存站点信息（整段覆盖）；key 白名单 + 类型校验（未知键、类型不符、年份越界、超 64KB 均返回中文错误）；传 `{}` 即清空后台配置、恢复代码默认值 |
| POST /visit | 公开 | 前台页面访问上报 `{path, articleId?}`：管理员自己的浏览忽略、爬虫 UA 忽略、同访客同路径 3 秒内去重；IP 只用于生成访客标识（md5），不落库 |
| GET /visit/dashboard?days=30 | 管理员 | 统计看板：`{days, overview:{todayPv,todayUv,rangePv,rangeUv,totalPv,totalUv}, daily:[{statDate,pv,uv}], topArticles:[{articleId,title,pv}]}`；days 限制在 1-365，趋势缺的日期补 0 |
| GET /category/selectAll 等 | 公开读/管理员写 | 同 article 模式 |
| /user/** 全部 | 仅超管 | 不可删除/降级当前登录账号 |
| POST /file/upload | 管理员 | multipart `file`；仅 jpg/jpeg/png/gif/webp（无 svg，防 XSS）；≤10MB；返回相对路径字符串 |
| GET /uploads/** | 公开 | 上传图片静态访问，落盘 `blog.upload-dir`（默认 ./uploads，相对后端工作目录） |

## 9. 数据表速览（database/blog_system.sql）

- `article`: id, title, summary(500), content(longtext), cover(500), category_id, user_id,
  status('published'默认), is_top(是否置顶,1=置顶), view_count, create_time, update_time(自动更新)
- `category`: id, name, description, sort, create_time
- `user`: id, username(唯一索引), password(`SHA256:`前缀哈希，明文旧数据首次登录自动升级),
  nickname, role(`SUPER_ADMIN`/`ADMIN`/`USER`), email, create_time
- `comment`: id, article_id, user_id(发表用户，空=旧游客评论), nickname(40), content(2000),
  parent_id(所属顶层评论，空=顶层), reply_to_nickname(被回复者昵称，冗余), like_count, create_time
- `comment_like`: comment_id + visitor_key(复合主键，登录 `u{id}` / 未登录 md5(IP+UA+盐)), create_time（点赞去重，删评论时一并清理）
- `about_me`: id(固定1), content(longtext, AboutMe 正文 Markdown), update_time
- `tag`: id, name(唯一索引, ≤50), create_time
- `article_tag`: article_id + tag_id（复合主键，多对多；删标签时后端会清掉这里的关联）
- `site_config`: id(固定1), content(longtext，站点信息 JSON), update_time（后台「站点信息」页编辑；读坏时回退代码默认值）
- `visit_log`: id(bigint), stat_date(date，按它分组), path(200), article_id(可空，文章页才有), visitor_key(char32, md5(IP+UA+盐)), create_time；索引 (stat_date) 与 (article_id, stat_date)；**存原始记录**，量大时可定期清理旧行

## 10. 工作流程（每个任务照此执行）

1. **开工前**：读本文件 → 读 PROJECT_OVERVIEW.md → `git status` + `git log --oneline -5` 确认起点干净、在 main 上。
2. 从 main 开 feature 分支：`feature/<主题>`。
3. 实现。改动保持小步，风格跟随现有代码。
4. **验证**：§6 的前端 build + 后端 test-compile 必跑；涉及接口行为的改动做 9998 端口 e2e。
5. 更新文档：PROJECT_OVERVIEW.md、本文件（§3 状态、§11 划掉完成项、§12 增删技术债）、
   ITERATION_BASE.md（若存在，勾任务 + 记决策）。
6. Commit（信息用英文 `feat:/fix:/chore:/docs:` 前缀，正文可中文），merge 回 main，**不 push**。
7. 向用户汇报：做了什么 / 怎么验证的 / 需要用户手动做什么 / 下一步建议。

## 11. 路线图（按优先级；做之前把本节对应任务读三遍）

### ④ 前台换脸（✅ 2026-08-31 完成，见 §3）

**目标**：访客看到的是真正的博客门面，不是管理系统。前后台布局分离。

**目标**：访客看到的是真正的博客门面，不是管理系统。前后台布局分离。

- 布局拆分：`App.vue` 改为按 `$route.meta.layout` 渲染两种布局——
  `front`（默认，顶部极简导航：首页/归档/关于我 + 登录入口，无侧边栏）与
  `admin`（现有侧边栏布局，/article /category /user /article/edit 用它）。
  建议新建 `src/layouts/FrontLayout.vue` 与 `src/layouts/AdminLayout.vue`，App.vue 只做切换。
- 首页升级：站点标题/署名/slogan 区块（先写死常量，配置表是 M4 的事）、
  文章卡片展示封面缩略图（`cover` 字段已可上传，用 `resolveAsset` 拼地址）、分类筛选条。
- 详情页：头图展示 cover；分类名展示（需拉分类列表或后端 join，选简单的前端映射）。
- 新增归档页 `/archive`：按年分组列出全部已发布文章（新后端接口或 selectPage 大页拉取均可，
  建议新接口 `GET /article/archive` 返回 `[{year, articles:[{id,title,createTime}]}]`）。
- 移动端：≤768px 时导航折叠、正文左右留白收窄。详情页 TOC 已自动隐藏，无需处理。
- **验收**：游客打开 8081 看不到任何管理入口；管理员登录后后台四页正常；
  两条验证命令全绿；手机宽度（375px）下首页/详情页可正常阅读。
- **暂不做**：深色模式、Vue3 迁移、评论。

### ⑤ 内容生态（长期）

**本批已完成（2026-09-03）**：
- RSS `/feed.xml` 真实订阅源已做（后端出 XML，`blog.site-title` / `blog.site-url` 可配；前端页脚与首页「保持联系」已接真实地址）。
- 站点信息集中到 `config/site.js`（identity / aboutLines / socials / portrait / profile），首页简介与 AboutMe 同源不再打架。
- AboutMe 详情页重做：极简风格，内容读 `site.profile`，空版块自动隐藏，占位文字待站主日后填充。
- 移除首页「假订阅」表单，改真实可用的「RSS + 社交关注 + 邮件写信」。
- 页脚 / 移动菜单社交入口按配置真实可用（github/bilibili/email/QQ），原先的死链接移除；Bilibili 为实心品牌图标（`IconBase` 增加 `filled` 支持）。

**未做（后续）**：
- sitemap.xml 与 SEO meta：✅ 已完成（2026-09-11，见 §3；后端出 `/sitemap.xml` + `/robots.txt`，前端按路由改 title/OG。仍是客户端改 meta，**要更好的收录效果后续可加预渲染**）。
- 标签系统：✅ 已完成（2026-09-10，见 §3；tag + article_tag 两表 + 前后台 UI）。
- 评论：✅ 已自建轻量（comment 表、即发即显 + 后台管理，2026-09-09，见 §3）；回复 + 点赞增强 ✅ 2026-09-11（见 §3）；Giscus 仅作未来上线后的可选替代（需 GitHub 账号 + 公网）。
- 访问统计：✅ 已完成（2026-09-11，见 §3；`visit_log` 原始记录 + `POST /visit` 上报 + `/dashboard` 看板，未引第三方统计服务）。
- 站点配置表（site_config）：✅ 已完成（2026-09-10，见 §3；前台信息与 AboutMe 资料后台可视化编辑，代码默认值仍作兜底）。
- Vue 2 → Vue 3 + Vite + Element Plus 迁移（页面少时做，越拖越贵）。

### ⑥ 上线安全硬化与部署（✅ 代码侧已完成 2026-09-09；服务器操作待站主执行）

- ① admin 默认口令：**站主已自行修改** ✅
- ② **登录失败限流/锁定**：已做——同 IP 或同账号 15 分钟内失败 5 次即锁定 15 分钟，成功登录清零（`AuthController`）✅
- ③ **密码 bcrypt**：已做——`PasswordUtil` 用 `BCRYPT:` 前缀 + 随机盐；兼容存量 `SHA256:`/明文并在登录成功后就地升级；只引 `spring-security-crypto` ✅
- ④ **全局异常兜底 + CORS 收敛**：已做——`GlobalExceptionHandler` 统一返回 `Result.error`（不再裸抛堆栈）；CORS 改为 `blog.cors-allowed-origins` 白名单（env `CORS_ALLOWED_ORIGINS`），各控制器 `@CrossOrigin("*")` 已全部移除 ✅
- ⑤ **数据库密码**：`application.yml` 不再提供默认密码（必须由 env 提供）；低权限建号脚本 `deploy/mysql/create-app-user.sql` ✅（账号需在服务器上创建）
- ⑥ **部署材料**：`deploy/` 下含 `DEPLOY.md`（步骤+检查清单）、nginx（同域反代 `/api`、`/uploads`、`/feed.xml`、登录限流、安全头）、`systemd/blog-backend.service`、`backup/mysql-backup.sh`；**实际服务器操作由站主执行或明确授权后进行**
- ⑦ **一键部署脚本**（2026-09-16）：`deploy/deploy.sh` 自动化 ①–⑥ 的机械步骤——`init`（apt 装包、blog 用户/目录、`blog_app` 随机密码低权限账号、导入本机 MySQL 全量数据、`/etc/blog/blog.env`(600)、systemd、nginx HTTP 引导配置、上传 jar/dist/uploads、探活、每日备份 cron）、`update [front|back|all]`、`cert`（certbot 证书 + 切仓库完整 HTTPS 配置 + 80→443）、`logs`/`status`。目标信息放 `deploy/deploy.env`（模板 `deploy.env.example`，gitignore）。服务器仅跑 Ubuntu 22.04/24.04 + 免密 sudo 登录用户 ✅
- **待办（服务器侧，站主执行）**：填 `deploy/deploy.env` → `bash deploy/deploy.sh init` → 域名 A 记录 → `bash deploy/deploy.sh cert` → `ufw` 防火墙 → `mysql_secure_installation` → 过一遍 DEPLOY.md 检查清单。脚本远程流程尚未在真服务器上实跑过（2026-09-16 本机只验证了构建/错误路径/密码提取），首跑时如遇报错回来改脚本并同步本文件

## 12. 已知问题 / 技术债（接手时先看这里）

- Token 存后端内存，重启即掉线。
- `selectAll` / `selectSearch` 旧接口仍返回全文 content（前端已改用 `/article/search` 与 `selectPage`），暂留兼容。
- 站内搜索用 `LIKE '%kw%'` 匹配标题/摘要/正文，正文为 longtext 全表扫；文章上千篇后需要换 MySQL FULLTEXT 或外部索引。关键词里的 `%` / `_` 未转义（只影响匹配范围，无注入风险）。
- 编辑器左右分栏无滚动同步（体验项，有空再做）。
- uploads 目录无孤儿图片清理机制（文章删了图还在，暂不处理）。
- 访问统计存**原始记录**（`visit_log`），没有归档/清理任务：按每天 ~1000 PV 估，一年约 36 万行，暂可承受；再大就加「按天汇总表 + 清理 90 天前原始行」。
- 统计上报依赖前端 JS（`App.vue` 每次路由切换 POST 一次）：禁用 JS 或用 curl 的访问不会被统计；`rangeUv` 是「按天去重后求和」，跨天同一访客会重复计入（`totalUv` 才是全站去重）。
- SEO 的 meta/OG 是**前端运行时改 DOM**：Google/Bing 能跑 JS，但大部分社交平台抓取卡片时不执行 JS（只会看到 `public/index.html` 的静态兜底）。要分享卡片精确到文章，后续需要预渲染（prerender）或把 `/post/:id` 做成服务端渲染页。
- `/sitemap.xml`、`/robots.txt` 由后端提供，nginx 必须按 `deploy/nginx/blog-site.conf` 里新增的两个 location 反代到后端，否则线上拿不到。
- 评论回复只做**两级**（回复的回复也挂到同一顶层下，用 `reply_to_nickname` 表明对象）；如需无限层级嵌套要改表和前端渲染。
- 删除顶层评论会连带删掉别人的回复（线程归属顶层作者），前端确认框已提示；如果以后想「保留回复只删顶层」，需要把回复的 `parent_id` 重新指向或允许孤儿回复。
- 点赞按访客标识去重：同一台设备换 UA / 清缓存 / 换 IP 就能重复点，属于轻量方案的已知边界（没有账号级风控）。
- Element UI vendor 包 1.2MB（按需引入或 Vue3 迁移时一并解决）。
- e2e 起后端时 `DB_PASSWORD` 必须加引号导出：密码含特殊字符，经 grep/cut 管道
  后未加引号会被 shell 拆坏（2026-08-31 踩坑：Access denied）。
- 部署无自动 schema 迁移（无 Flyway）：`deploy.sh init` 只做首次全量导入；以后本机改了表结构，
  需手动在服务器执行对应 ALTER（或重导）并同步 `database/blog_system.sql`。
- `deploy.sh` 的远程流程（init/update/cert 在真服务器上的表现）尚未实跑验证过；
  本机已验证：bash -n、help、build（真实打包）、缺配置报错、dev-env.bat 密码提取、SSH 失败路径。

## 13. 关键决策历史（为什么是现在这样）

| 决策 | 原因 |
|---|---|
| Markdown 选 markdown-it + highlight.js 按需注册 + DOMPurify | 轻量可控防 XSS，不引整套编辑器组件库；渲染逻辑集中一处复用 |
| 图片存相对路径 `/uploads/...` | 未来换域名/部署不用改存量文章，只改 API_BASE |
| 列表接口排除 content | 种子数据里就有 1 万字长文，全文进列表是明确的性能坑 |
| 阅读量仅游客访问已发布文章时 +1（SQL 原子自增） | 作者预览不污染数据；并发不丢计数 |
| 上一篇/下一篇按 create_time asc, id asc | 种子数据存在同秒文章，必须 id 兜底 |
| bat 脚本 GBK 编码 | 实测 UTF-8 + chcp 65001 在本机 cmd 解析错乱（命令被截断执行） |
| stop 脚本端口+进程名双匹配 | 实测本机 8080 被无关软件占用，纯按端口杀会误伤 |
| 新文章 POST 后返回实体 | 编辑器保存新文章后需要 id 才能原地继续编辑 |
| 移除 M3-⑤「安全硬化 + 部署上线」规划（2026-09-03） | 站主拍板：不按原预设路线执行该大步，改为与站主商量确定要新增的附加/拓展功能；原规划涉及的各项安全/部署事项，此后一律按新的商量结果再定 |
| 站点信息集中化 + AboutMe 详情页 + 移除首页“假订阅”（2026-09-03） | 首页简介与 AboutMe 文案此前两套不一致；假订阅收集邮箱却不做任何事，属误导。集中到 `config/site.js` 一处维护，AboutMe 空版块自动隐藏待填充，联系区改为真实可用的 RSS + 社交 + 写信 |
| 评论用自建轻量（comment 表），暂不上 Giscus（2026-09-09） | 未上线到公网、访客无需账号留言、数据 100% 自持；Giscus 需公开仓库 + GitHub 登录 + 公网，仅作未来可选替代 |
| 标签用两张表 + 前端按名字反查 id（2026-09-10） | 多对多必须走关联表，删标签要顺手清关联；详情页只回标签名（够展示），点标签筛选再按 `/tag/selectAll` 映射回 id，避免为少数场景改接口结构 |
| 首页标签筛选写进地址栏 `?tag=id`（2026-09-10） | 筛选结果可分享/可回退；详情页标签直接链到首页该标签，省一个独立的标签页 |
| 文章保存时按“传了 tagIds 才更新标签”语义（2026-09-10） | 后台列表的“置顶/取消置顶”只 POST `{id, top}`，若不判断就会把文章标签清空 |
| 搜索单独开 `GET /article/search` 而不是扩 `selectPage`（2026-09-10） | selectPage 的 `articleTitle` 语义是「只搜标题」，两者混在一起会让列表接口越来越难懂；搜索要正文匹配且结果页更重，独立接口能各自演进 |
| 搜索结果页高亮用「切片段 + v-for 渲染 span」而不是 `v-html`（2026-09-10） | 铁律禁止未消毒的 v-html；切片段天然不解析 HTML，正文再脏也只会当纯文本显示 |
| 站点信息用「JSON 单行表 + 前端合并」而不是拆成一堆列（2026-09-10） | skills/journey 这类 `[{a,b}]` 结构拆列很丑，以后加字段还要改表；JSON + 后端 key 白名单/类型校验既灵活又不失控。代价是后端只做类型校验、不做逐字段渲染约束 |
| 后台配置「按 key 覆盖」代码默认值，数组整段替换（2026-09-10） | 代码默认值让首次部署 / 配置损坏都不会白屏；数组整段替换才能让「清空 aboutLines」真的在前台隐藏版块，而不是被默认值填回来 |
| 访问统计用「前端主动上报 + 后端自己筛」而不是在 API 拦截器里记（2026-09-11） | SPA 一个页面会打好几个接口（首页要拉文章/分类/标签/统计），按接口记会重复计数且分不清页面；由前端在路由切换时 POST 一次，path/articleId 都准 |
| 统计忽略管理员自己的浏览，且不存原始 IP（2026-09-11） | 与文章 view_count 的口径一致（作者预览不计数），否则站主自己调试就把数据刷满；访客标识只用 md5(IP+UA+盐)，够去重又不留个人信息 |
| 看板图表用纯 CSS 柱状图，不引 echarts（2026-09-11） | 只画「近 N 天 PV/UV 双柱 + hover 看数值」，引 echarts 会多 1MB 级依赖；Element UI 里没有现成图表，自己写 40 行 CSS 就够 |
| SEO 分两半：后端出 sitemap/robots，前端改 meta（2026-09-11） | 后端能给出爬虫真正需要的「有哪些 URL」（sitemap 是静态 XML，最可靠）；而 per-page meta 在 SPA 里只能靠 JS 改 DOM，先把能做的做掉，预渲染等真有需求再上，避免过早引入 SSR 复杂度 |
| sitemap 单独加 `selectPublishedBriefs` 而不复用 `selectPublishedAll`（2026-09-11） | 后者会把每篇正文（longtext）都读出来，只为生成几行 URL 不值得 |
| 评论回复只做两级：`parent_id` 一律指向顶层评论（2026-09-11） | 个人博客不需要无限嵌套；两级在手机上可读性最好，前端渲染和后端排序都简单（列表接口直接按「顶层 + 紧跟其回复」返回，前端不用拼树） |
| 点赞允许未登录，用访客标识去重（2026-09-11） | 访客随手点赞门槛最低；`comment_like(comment_id, visitor_key)` 复合主键让「再点即取消」实现干净，`like_count` 用 SQL 原子加减避免并发丢计数 |
| 删除顶层评论连带删回复（2026-09-11） | 线程归属顶层作者，留一半回复会变成孤儿；前端确认框明确提示后果，站主在后台也有同样提示 |
| 部署走「本机构建 + scp/tar 上传产物」，服务器不装 Node/Maven（2026-09-16） | 服务器只跑 JRE/nginx/MySQL，面小好维护；产物在开发机验证过才上服务器，避免服务器上构建的不确定性；git push 仍被铁律禁止，产物直传不依赖远程仓库 |
| deploy.sh 用 bash 而不是 bat，init 先 HTTP 引导、cert 再切 HTTPS（2026-09-16） | 脚本只在 Git Bash 里跑（服务器不需要脚本本体）；certbot 签证书要求 80 端口的 ACME 路径先可用，两阶段让「没域名也能先跑 HTTP」与「有域名一键 HTTPS」都成立；init 幂等可重跑（库有表跳过导入、密码复用 blog.env） |
| nginx 的 /api/、/uploads/ 用 `^~` 前缀匹配 + index.html 禁缓存（2026-09-16） | 修复原配置 bug：静态资源正则 location 会抢占普通前缀 location，导致 `/uploads/x.png` 404；index.html 不禁缓存则发版后浏览器可能一直用旧入口页（hash 文件名的意义就没了） |
