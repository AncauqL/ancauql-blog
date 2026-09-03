# AGENTS.md — AncauqL Blog 开发交接手册

> **任何 AI agent 或开发者接手本仓库前，必须先通读本文件。**
> 本文件的目标：让能力较弱的模型也能安全、正确地继续开发。所有本机环境的坑、
> 项目约定、验证命令、后续规划都在这里显式写死。**每完成一个任务必须回来更新本文件,向其他agent同步目前的进度。**

最后更新：2026-09-03（移除 M3-⑤ 安全/部署规划；站点信息集中 + AboutMe 详情页 + 真实社交/联系方式 + 去假订阅 + RSS /feed.xml；第二批：文章置顶 is_top→首页置顶大卡、优雅 404、图片懒加载、时间解析收敛 utils/datetime.js、归档条目改链接、API_BASE 走 .env、前台阅读缩放；第三批：正文支持 LaTeX 数学(KaTeX + markdown-it-texmath)、编辑器工具栏扩充）

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
├─ blog_backend/              ← Spring Boot，端口 9999
│  └─ src/main/java/com/example/blog_backend/
│     ├─ controller/          ← Hello / Auth / Article / Category / User / File
│     ├─ service/ + impl/     ← 业务层
│     ├─ mapper/              ← MyBatis-Plus Mapper（基本无 XML，UserMapper.xml 除外）
│     ├─ entity/ dto/         ← 实体与传输对象（dto 含 ArchiveGroup / ArticleNeighbors）
│     ├─ common/              ← Result / AuthContext / PasswordUtil / RoleUtil
│     └─ config/              ← WebConfig（拦截器+静态资源）/ AuthInterceptor / MybatisPlusConfig
├─ blog_frontend/vue/         ← Vue 2 + Element UI，开发端口 8080（本机实际 8081，见 §5）
│  └─ src/
│     ├─ App.vue              ← 顶层布局切换器（按 $route.meta.layout 选前后台布局）
│     ├─ layouts/             ← FrontLayout（顶栏+页脚）/ AdminLayout（侧边栏）
│     ├─ config/site.js       ← 站名/作者/slogan/备案号常量
│     ├─ utils/request.js     ← axios 实例，导出 API_BASE / resolveAsset
│     ├─ utils/auth.js        ← 登录态工具（getStoredUser / isManager / logout 等）
│     ├─ utils/markdown.js    ← Markdown 渲染管线（渲染/高亮/消毒/字数统计），渲染必须复用它
│     ├─ assets/css/markdown.css ← 正文排版样式（markdown.js 引入，详情页+编辑器共用）
│     ├─ router/index.js      ← 路由 + 登录/角色守卫 + meta.layout
│     └─ views/               ← HomeView / Archive / ArticleDetail / ArticleEditor / Article / Category / User / Login / AboutMe
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
- [ ] **M4 长期增强**：RSS、评论、标签、统计
- 体验/工程细节（2026-09-03 完成）：文章可后台“置顶”(is_top)→首页“置顶”大卡；优雅 404 页；文章图片懒加载；时间解析统一到 `utils/datetime.js`；归档条目改 `<router-link>`；`API_BASE` 走 `.env`(VUE_APP_API_BASE，见 .env.example)；前台阅读缩放(1.1，正文详情页再 1.1)

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
```

## 7. 代码约定（必须遵守的既有风格）

- **统一响应** `common/Result`：`{code, msg, data}`，**code 是字符串**（`'200'` / `'401'` / `'403'` / `'500'`）。
  前端判断一律 `res.code === '200'`（注意引号）。HTTP 状态码恒为 200，错误语义在 code 里。
- **时间字段**：后端 `LocalDateTime` 序列化后可能是数组 `[y,m,d,h,mi,s]` 或 ISO 字符串，
  前端 `formatTime` 两种都要兼容（现有页面有参考实现）。
- **文章状态**：只有 `published` / `draft` 两个值，字符串直存。
- **角色**：`SUPER_ADMIN` / `ADMIN`，游客无账号。权限判断后端在 `AuthContext`（ThreadLocal），
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
| POST /auth/login | 公开 | `{username,password}` → `{token,user}` |
| GET /auth/me · POST /auth/logout | 登录 | Token 在内存，后端重启失效 |
| GET /article/selectAll | 公开* | 游客只见 published；管理员见全部（旧接口，新代码请用 selectPage） |
| GET /article/selectPage | 公开* | 参数全可选：pageNum=1, pageSize=10, articleTitle, status（status 仅管理员生效，游客恒 published）；按 create_time desc, id desc；不含 content |
| GET /article/detail?id= | 公开* | 草稿仅管理员可见(403)；游客访问已发布文章时 view_count 原子 +1，管理员预览不计数 |
| GET /article/neighbors?id= | 公开 | 已发布文章的上一篇/下一篇 `{prev:{id,title},next:{...}}`，按 create_time asc, id asc |
| GET /article/archive | 公开 | 归档：已发布文章按年分组 `[{year, articles:[{id,title,createTime}]}]`，年份与组内均倒序 |
| POST /article | 管理员 | 带 id 更新 / 无 id 新增；**返回带 id 的完整对象** |
| DELETE /article/delete?id= | 管理员 | |
| GET /category/selectAll 等 | 公开读/管理员写 | 同 article 模式 |
| /user/** 全部 | 仅超管 | 不可删除/降级当前登录账号 |
| POST /file/upload | 管理员 | multipart `file`；仅 jpg/jpeg/png/gif/webp（无 svg，防 XSS）；≤10MB；返回相对路径字符串 |
| GET /uploads/** | 公开 | 上传图片静态访问，落盘 `blog.upload-dir`（默认 ./uploads，相对后端工作目录） |

## 9. 数据表速览（database/blog_system.sql）

- `article`: id, title, summary(500), content(longtext), cover(500), category_id, user_id,
  status('published'默认), is_top(是否置顶,1=置顶), view_count, create_time, update_time(自动更新)
- `category`: id, name, description, sort, create_time
- `user`: id, username(唯一索引), password(`SHA256:`前缀哈希，明文旧数据首次登录自动升级),
  nickname, role, email, create_time

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
- sitemap.xml 与 SEO meta（依赖 SSR / 预渲染，评估后再定）。
- 标签系统：tag 表 + article_tag 关联表 + 前后台 UI。
- 评论：先挂 Giscus（GitHub Discussions，零后端）试运行，再决定是否自建。
- 访问统计：自托管 Umami 或简单 access_log 表。
- 站点配置表（site_config），关于我页面后台可编辑。
- Vue 2 → Vue 3 + Vite + Element Plus 迁移（页面少时做，越拖越贵）。

## 12. 已知问题 / 技术债（接手时先看这里）

- Token 存后端内存，重启即掉线。
- 密码是无盐 SHA256。
- CORS 全开 `*`。
- `selectAll` / `selectSearch` 旧接口仍返回全文 content，前端已不用于列表，暂留兼容。
- 编辑器左右分栏无滚动同步（体验项，有空再做）。
- uploads 目录无孤儿图片清理机制（文章删了图还在，暂不处理）。
- Element UI vendor 包 1.2MB（按需引入或 Vue3 迁移时一并解决）。
- e2e 起后端时 `DB_PASSWORD` 必须加引号导出：密码含特殊字符，经 grep/cut 管道
  后未加引号会被 shell 拆坏（2026-08-31 踩坑：Access denied）。

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
