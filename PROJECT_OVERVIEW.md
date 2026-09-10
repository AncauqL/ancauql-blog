# AncauqL Blog 项目说明

这是一套前后端分离的个人博客项目。当前形态既包含面向访客的博客展示页面，也包含用于维护文章和分类的轻量后台管理页面。后续迭代时，可以把本文档当作项目地图使用。

## 目录结构

```text
AncauqL_blog/
├─ blog_backend/                 # Spring Boot 后端
│  ├─ pom.xml                    # Maven 依赖与 Java 版本配置
│  └─ src/
│     ├─ main/java/com/example/blog_backend/
│     │  ├─ controller/          # HTTP 接口
│     │  ├─ service/             # 业务接口
│     │  ├─ service/impl/        # 业务实现
│     │  ├─ mapper/              # MyBatis-Plus Mapper
│     │  ├─ entity/              # 数据实体
│     │  ├─ common/Result.java   # 统一响应结构
│     │  └─ config/              # MyBatis-Plus 配置
│     └─ main/resources/
│        ├─ application.yml      # 服务端口、数据库、MyBatis-Plus 配置
│        └─ mappers/             # XML Mapper，当前主要保留 UserMapper.xml
├─ blog_frontend/vue/            # Vue CLI 前端
│  ├─ package.json               # 前端依赖和脚本
│  ├─ vue.config.js              # Vue CLI 配置
│  ├─ public/                    # 静态入口资源
│  └─ src/
│     ├─ App.vue                 # 顶层布局切换器（前台 / 后台）
│     ├─ layouts/                # FrontLayout（顶栏+页脚）与 AdminLayout（侧边栏）
│     ├─ config/site.js          # 站点名、作者、slogan 等常量
│     ├─ main.js                 # Vue 入口
│     ├─ router/index.js         # 前端路由（meta.layout 区分前后台）
│     ├─ utils/request.js        # Axios 实例、API_BASE、resolveAsset
│     ├─ utils/auth.js           # 登录态读取 / 退出等共享工具
│     ├─ utils/markdown.js       # Markdown 渲染管线
│     ├─ views/                  # 页面组件
│     └─ assets/                 # 样式与 logo
└─ database/
   └─ blog_system.sql            # 当前博客主库脚本
```

## 技术栈

### 后端

- Java 17：`pom.xml` 中配置的目标 Java 版本。
- Spring Boot 3.5.15：应用启动、Web MVC、依赖管理。
- Spring Web：提供 REST API。
- MyBatis-Plus 3.5.14：实体映射、通用 CRUD、分页查询。
- MyBatis-Plus JSQLParser：分页插件依赖。
- MySQL Connector/J：连接 MySQL。
- Maven：后端构建工具；当前仓库没有 Maven Wrapper。
- JUnit / Spring Boot Test：保留了基础上下文加载测试。

### 前端

- Vue 2.6.14：页面组件框架。
- Vue Router 3.5.1：前端路由，当前使用 `history` 模式。
- Element UI 2.15.14：布局、按钮、表格、弹窗、表单、消息提示等 UI 组件（后台主力；前台新设计不依赖它）。
- Tailwind CSS 3.4：前台工具类样式（preflight 关闭以兼容 Element UI，缺省 border-style 在 global.css 手工补齐）。
- @fontsource/inter：Inter 字体自托管（前台设计指定字体，国内不走 Google Fonts CDN）。
- lucide 图标：静态内联 SVG 组件（`src/components/icons/`），不引 iconify 运行时。
- Axios 1.18.0：前后端请求。
- markdown-it 15：文章正文 Markdown 渲染，集成 `markdown-it-texmath` + **KaTeX** 支持 LaTeX 数学（`$…$` 行内 / `$$…$$` 块级），经 DOMPurify 放行公式内联样式消毒。
- highlight.js 11：代码块语法高亮，按需注册常用语言。
- DOMPurify 3：渲染后 HTML 消毒，防 XSS。
- Vue CLI 5：本地开发、构建、Babel 编译。
- core-js / Babel：浏览器兼容处理。

### 数据库

- MySQL 8：SQL dump 来自 MySQL 8.0.46。
- 主要库名：`blog_system`。
- 字符集：`utf8mb4`。

## 当前功能

### 访客侧页面

前台使用独立布局（`FrontLayout`，墨白极简设计）：固定顶部导航（滚动 >20px 加毛玻璃背景；含文章 / 归档 / 关于 + 搜索 + 极简登录入口）+ 页脚 + 回到顶部按钮；移动端为右侧滑入全屏菜单；全局搜索覆盖层（Ctrl+K 打开、ESC 关闭、300ms 防抖走后端站内搜索，展示前 6 条并可「查看全部结果」→ 跳 `/search`）。样式基于 Tailwind 工具类 + 自托管 Inter 字体。

- 首页：`/`
  - Hero 区：大标题（“思考，记录，然后遗忘。”，末段刻意用 `text-neutral-300` 灰色）+ 描述 + 双 CTA 按钮 + 三组统计数字（文章数 / 总阅读来自 `/article/stats`，写作年数按建站年份计算）。
  - “置顶”大卡：后台把文章“置顶”后，首页顶部出现深色大卡展示该文（多篇置顶时取最新一篇；无置顶则不显示）。
  - 文章网格：三列（移动一列 / 平板两列），`gap-6`，卡片图 `aspect-[4/3]`、hover `scale(1.05)` 0.6s 缓动。
  - 分类筛选条：真实分类 `categoryId` 服务端过滤（选中态黑底白字圆角按钮）。
  - 标签筛选条：分类下方一行小胶囊，展示所有**有已发布文章**的标签（带篇数）。点击后筛选写进地址栏 `/?tag=标签ID`，可直接分享 / 前进后退；选中时标题变为「标签 · 名称」并出现「清除标签筛选」。
  - 文章卡片底部展示该文标签（`#标签名` 小圆角标签）。
  - “加载更多”：真分页追加（服务端 selectPage 翻页），到底显示“已加载全部文章”。
  - 关于我区：左图右文两栏，简介文案来自 `site.aboutLines`；未设肖像图时显示站名首字母占位（不裂图）；提供“完整介绍 →”跳转 `/aboutme`，并展示社交入口。
  - 保持联系区：RSS 订阅（真实 `/feed.xml`）+ 社交关注 + 邮件写信；已移除原先“假订阅”表单（不再采集访客邮箱）。
  - 分割线动画：滚动进入视口时从 0 展开 to 48px。
- 文章详情页：`/post/:id`
  - 请求 `/article/detail?id=文章ID`。
  - 正文按 Markdown 渲染：标题、列表、引用、表格、图片、代码块。
  - 代码块带语言标签、语法高亮和一键复制按钮。
  - 宽屏下右侧显示自动生成的目录，支持点击跳转和滚动高亮。
  - 元信息展示创建时间、分类名、阅读数、全文字数、预计阅读时长。
  - 摘要下方展示文章标签（`#标签名`），点击跳到首页该标签的筛选视图。
  - 有封面时展示头图。
  - 底部提供上一篇 / 下一篇导航（仅限已发布文章）。
  - 游客每次打开已发布文章，阅读数自动 +1；管理员预览不计数。
  - 管理员查看草稿时显示“草稿预览”标记。
- 归档页：`/archive`
  - 请求 `/article/archive`，按年份分组展示全部已发布文章（倒序）。
- 搜索结果页：`/search?q=关键词`
  - 请求 `/article/search`，关键词同时匹配**标题 / 摘要 / 正文**，服务端分页 + 「加载更多」。
  - 命中关键词在标题与摘要里高亮（切片段渲染成纯文本 span，不解析 HTML）。
  - 每条结果展示分类、日期与标签；关键词写进地址栏，可分享、可前进后退；顶栏覆盖层回车直达本页。
- 关于我：`/aboutme`
  - 极简风格详情页，内容全部来自 `config/site.js` 的 `profile`（姓名 / 身份 / motto / bio / skills / interests / favorites / journey）。空版块自动隐藏，往数组里填数据即可扩展；目前占位内容待站主日后补充。
- 404 页：访问不存在的路径显示简洁 404（`/` 兜底路由，不再白屏）。

### 管理侧页面

后台使用独立布局（`AdminLayout`，`meta.layout: 'admin'` 的路由）：深色侧边栏（文章管理 / 分类管理 / 标签管理 / 评论管理 / 关于我编辑 / 账号设置 / 账号管理 / 查看前台）。前台顶栏对管理员显示「进入后台」入口。

- 分类管理：`/category`
  - 展示所有分类，按 `sort` 升序排序。
  - 支持新增、编辑、删除分类。
  - 字段包括分类名称、分类描述、排序值。
- 标签管理：`/tag`
  - 展示全部标签（按名称升序）与各自的**已发布文章数**。
  - 支持新增、改名、删除；重名 / 空名 / 超 50 字会给出中文提示。
  - 删除标签会同时移除它与所有文章的关联（删除前会提示还挂着几篇文章）。
- 文章管理：`/article`
  - 服务端分页表格，支持按标题搜索、按状态（已发布 / 草稿）过滤，按标签筛选。
  - 展示标题、摘要、分类、标签、状态、阅读数、创建时间。
  - 新增 / 编辑跳转到全屏编辑器页，删除带确认。
- 文章编辑器：`/article/edit/:id?`
  - 左右分栏：左侧 Markdown 输入，右侧实时预览（与详情页同一套渲染管线）。
  - 工具栏：插入图片、加粗、行内代码、代码块、链接、引用，以及 公式（LaTeX）、公式块、标题、列表、删除线、表格。
  - 图片上传：按钮选择、粘贴、拖拽三种方式，自动上传后插入 Markdown。
  - 封面上传与预览。
  - 标签选择：多选框，可从已有标签里选，也可直接输入新标签名回车即新建并挂上。
  - 本地草稿：自动保存到 localStorage，意外关闭后可恢复；离开页面有未保存确认。
  - 快捷键：Tab 缩进、Ctrl+S 保存。
  - 状态切换（草稿 / 发布）+ 保存 / 保存并返回。
- 用户管理：`src/views/User.vue`
  - 已重构为账号管理页。
  - 仅 `SUPER_ADMIN` 可访问。
  - 支持新增、编辑、删除管理员账号。
  - 字段包括账号、昵称、邮箱、角色、密码。

- 评论管理：`/comment`
  - 展示全部评论（昵称 / 内容 / 所属文章 / 时间），可删除，或跳转到对应原文页。
- 关于我编辑：`/about/edit`
  - 管理员用 Markdown 编辑 AboutMe 正文（左编辑右预览，实时渲染），保存到 `about_me` 表；公开页优先显示之，空则回退 site.js。
- 站点信息：`/site`
  - 可视化编辑站点信息：站名 / 作者署名 / slogan / 身份一行 / 建站年份 / 备案号、首页 Hero（大标题两行 + 描述 + 首页简介）、关于我（肖像图上传、姓名、身份、座右铭、自我介绍、技术栈、兴趣爱好、喜欢的作品、经历）、社交（GitHub / Bilibili / 邮箱 / QQ）。
  - 保存到 `site_config` 表（JSON），前台启动时拉取并覆盖 `config/site.js` 里同名配置；顶栏标记「当前使用后台配置 / 当前使用代码默认值」，可一键「恢复代码默认」。
  - 列表类字段按行填写：一行一条；技术栈 / 经历按 `左 | 右` 写成「名称 | 说明」「时间 | 事件」。

### 登录与权限

- 登录页：`/login`
  - 请求 `/auth/login`。
  - 登录成功后前端保存 `blog_token` 和 `blog_user`。
  - Axios 请求自动携带 `Authorization: Bearer token`。
- 游客：
  - 可访问首页、文章详情、关于我。
  - 只能看到 `published` 文章。
- 管理员：`ADMIN`
  - 可访问文章管理和分类管理。
- 超级管理员：`SUPER_ADMIN`
  - 拥有管理员权限。
  - 可访问账号管理。
- 普通用户：`USER`
  - 通过 `/register` 用邮箱+密码注册（注册成功即登录），也可在 `/login` 用邮箱登录。
  - **无后台权限**：不能进后台，不能管理文章/分类/用户；仅可登录后发表/删除自己的评论。
- 评论权限：发表评论需登录（任意角色）；删除评论＝管理员任意 / 普通用户仅本人；游客只能读、不能发。
- 注册防灌：蜜罐字段 + 按 IP 限流；暂未做邮箱验证与找回密码。

## 后端接口概览

统一基础地址：`http://localhost:9999`

统一响应结构：

```json
{
  "code": "200",
  "msg": "请求成功",
  "data": {}
}
```

### 基础接口

- `GET /hello`：返回 `hello world`，可用于快速确认后端是否启动。
- `GET /feed.xml`：RSS 2.0 订阅源（公开），只收录已发布文章（最多 50 篇，按发布时间倒序），每篇文章的链接按 `blog.site-url` 拼前台地址。

### 登录接口

- `POST /auth/login`：登录，参数为 `username`、`password`（管理员用账号、普通用户用注册邮箱）。
- `POST /auth/register`：注册普通用户（USER），参数 `email/nickname/password`（+蜜罐 `website`），成功即登录；邮箱即账号名。
- `POST /auth/profile`：当前登录用户自助更新资料（任意角色），参数 `email/currentPassword/newPassword`；须验当前密码，新邮箱全站唯一。
- `POST /auth/login` 兼容用邮箱登录：账号名（如 admin）或邮箱均可。
- `GET /auth/me`：获取当前登录用户。
- `POST /auth/logout`：退出登录。

### 文章接口

- `GET /article/selectAll`：查询全部文章。
- `GET /article/detail?id=文章ID`：按 ID 查询文章详情；游客访问已发布文章时阅读数 +1。
- `GET /article/neighbors?id=文章ID`：查询上一篇 / 下一篇（按发布时间排序，仅返回已发布文章的 id 和标题）。
- `GET /article/search?keyword=关键词&pageNum=1&pageSize=10`：站内搜索（公开）。
  - 关键词同时匹配标题 / 摘要 / 正文（任一命中即可）；关键词为空时返回空页，不会退化成全站列表。
  - 游客只能搜到已发布文章，管理员登录后还能搜到草稿（便于找旧稿）。
  - 按创建时间倒序、id 倒序；**同样不返回 content**，每条记录带 `tagNames`。
- `GET /article/selectSearch?articleTitle=关键词`：按标题模糊搜索（旧接口，返回全文，建议改用 `/article/search`）。
- `GET /article/selectPage?pageNum=1&pageSize=10&articleTitle=关键词&status=状态&categoryId=分类ID&tagId=标签ID`：分页查询。
  - 所有参数可选（pageNum 默认 1，pageSize 默认 10）。
  - 按置顶优先、创建时间倒序、id 倒序排列；**返回结果不含 content 大字段**，正文用 detail 单查。
  - status 过滤仅对管理员生效；游客恒定只看到已发布文章。categoryId / tagId 对所有人生效（tagId 命中 `article_tag` 关联）。
  - 每条记录额外带 `tagNames`（标签名数组），供列表直接展示。
- `GET /article/archive`：归档数据，已发布文章按年份分组（年份与组内均倒序），元素为 `{year, articles:[{id,title,createTime}]}`。
- `GET /article/stats`：站点统计，返回 `{articleCount, totalViews}`（仅统计已发布文章；写作年数由前端按建站年份计算）。
- `POST /article`：新增或编辑文章；请求体带 `id` 时编辑，不带 `id` 时新增。**返回带 id 的完整文章对象**。
  - `tagIds`（标签 ID 数组）：传了才更新该文标签（先清后插）；不传则保持原样，因此“只改置顶”这类局部更新不会清空标签。
- `DELETE /article/delete?id=文章ID`：删除文章（同时清掉该文的标签关联）。

### 评论接口

- `GET /comment?articleId=文章ID`：某篇文章的评论列表（公开，按时间升序）。
- `POST /comment`：发表评论（**需登录，任意角色**，即发即显）。请求体含 `articleId/content`（昵称取账号，服务端定名防伪造）+ 蜜罐 `website`；按 IP 每分钟限流；内容 ≤2000 字。
- `GET /comment/list`：全部评论（管理员，新的在前）。
- `DELETE /comment/delete?id=评论ID`：删除评论（任意登录：管理员任意、普通用户仅本人）。

### 关于我接口

- `GET /about`：AboutMe 正文（Markdown，公开）。AboutMe 页优先用它，为空则回退 site.js。
- `PUT /about`：保存 AboutMe 正文 Markdown（管理员）。

### 站点信息接口

- `GET /site`：读取站点信息配置（公开，JSON）。从未保存过后台配置时返回 `{}`，前端按 key 回退 `config/site.js` 的代码默认值。
- `PUT /site`：保存站点信息（管理员，整段覆盖）。后端做 key 白名单 + 类型校验：未知键、类型不符、年份越界、单条超 500 字、总大小超 64KB 都会返回中文错误。传 `{}` 表示清空后台配置、恢复代码默认值。
  - 顶层字段：`name / author / slogan / identity / startYear / icp / heroTitleLine1 / heroTitleLine2 / heroText / portrait / aboutLines / socials{github,bilibili,email,qq} / profile{name,identity,motto,bio,skills,interests,favorites,journey}`。
  - `skills` 是 `[{label,desc}]`，`journey` 是 `[{period,text}]`。

### 文件接口

- `POST /file/upload`：图片上传（仅管理员），multipart 字段名 `file`。
  - 只允许 jpg / jpeg / png / gif / webp（不含 svg，防 XSS），大小 ≤ 10MB。
  - 落盘到 `blog.upload-dir`（默认 `./uploads`，相对后端工作目录），按月份分目录，文件名为 UUID。
  - 返回相对路径字符串，如 `/uploads/202608/xxx.png`；前端渲染时自行拼接后端地址。
- `GET /uploads/**`：上传图片的公开静态访问。

### 分类接口

- `GET /category/selectAll`：查询全部分类。
- `GET /category/selectSearch?categoryName=关键词`：按名称模糊搜索。
- `GET /category/selectPage?pageNum=1&pageSize=10&categoryName=关键词`：分页查询。
- `POST /category`：新增或编辑分类；请求体带 `id` 时编辑，不带 `id` 时新增。
- `DELETE /category/delete?id=分类ID`：删除分类。

### 标签接口

- `GET /tag/selectAll`：查询全部标签（公开），返回 `[{id, name, count}]`，`count` 为该标签下**已发布**文章数，按名称升序。
- `POST /tag`：新增或改名标签（管理员）；请求体 `{id?, name}`，带 `id` 为改名。空名 / 超 50 字 / 重名返回中文错误提示。
- `DELETE /tag/delete?id=标签ID`：删除标签（管理员），同时清理 `article_tag` 中的关联行。

### 用户接口

- `GET /user/selectAll`：查询全部用户。
- `GET /user/selectSearch?username=关键词`：按账号模糊搜索。
- `GET /user/selectPage?pageNum=1&pageSize=10&username=关键词`：分页查询。
- `POST /user`：新增或编辑用户。
- `DELETE /user/delete?id=用户ID`：删除用户。

注意：`/user/**` 当前只允许 `SUPER_ADMIN` 访问。

## 数据表与注意事项

### 当前主库：`database/blog_system.sql`

`article` 表：

- `id`：文章 ID。
- `title`：标题。
- `summary`：摘要。
- `content`：正文。
- `cover`：封面图片地址，首页文章卡片、精选大卡与详情页头图均已实际展示（相对路径 `/uploads/...`）。
- `category_id`：分类 ID。
- `user_id`：作者 ID。
- `status`：文章状态，当前使用 `published` / `draft`。
- `is_top`：是否置顶（1=置顶；列表与首页“置顶”大卡优先），后台文章管理可一键置顶/取消。
- `view_count`：阅读数，游客打开已发布文章详情时自动递增（数据库端原子自增）。
- `create_time` / `update_time`：创建与更新时间。

`category` 表：

- `id`：分类 ID。
- `name`：分类名称。
- `description`：分类描述。
- `sort`：排序值。
- `create_time`：创建时间。

`user` 表：

- `username`：账号，带唯一索引。
- `password`：密码。保存为 `BCRYPT:` 前缀（bcrypt + 随机盐）；更早的 `SHA256:` / 明文密码在首次登录成功后自动升级为 bcrypt。
- `nickname`：昵称。
- `role`：角色，当前使用 `SUPER_ADMIN` / `ADMIN` / `USER`（注册访客，仅可评论）。
- `email`：邮箱。
- `create_time`：创建时间。

`comment` 表（自建轻量评论，即发即显）：

- `id`：评论 ID。
- `article_id`：所属文章 ID。
- `user_id`：发表评论的用户 ID（登录后发；空=早期游客评论）。
- `nickname`：昵称（≤40 字）。
- `content`：评论内容（≤2000 字）。
- `create_time`：发表时间。

`site_config` 表（站点信息，单行 id=1）：

- `content`：站点信息 JSON（站名 / Hero / 首页简介 / 社交 / AboutMe 资料），后台「站点信息」页编辑。
- 前端启动时 `GET /site` 拉取，按 key 覆盖 `config/site.js` 默认值；JSON 损坏或为空时静默回退代码默认值。
- `update_time`：最后保存时间。

`tag` 表（标签）：

- `id`：标签 ID。
- `name`：标签名（≤50 字，唯一索引，重名会被拒绝）。
- `create_time`：创建时间。

`article_tag` 表（文章-标签多对多关联）：

- `article_id` + `tag_id`：复合主键。
- 文章保存时带上 `tagIds` 会先删后插；删除文章或删除标签时，对应关联行由后端清理。

## 启动方式

### 0. 一键启动（推荐）

- 双击根目录 `start-dev.bat`：自动定位 Maven（先查 PATH，再扫 `~/.m2/wrapper/dists`）、检查 MySQL 连通性，然后在两个独立窗口分别启动后端和前端。
- 数据库密码：复制 `dev-env.example.bat` 为 `dev-env.bat` 并填入本机密码（该文件已被 git 忽略，不会提交）；没有该文件时启动器会提示手动输入。
- `start-dev.bat /check`：只做环境检查，不启动任何服务。
- 双击 `stop-dev.bat` 停止服务：按「端口 + 进程名」双重匹配，只停 9999 端口的 java 和 8080/8081 端口的 node；端口被其他软件占用时会跳过，不会误杀。
- 端口约定：前端 8080（被占用时 Vue CLI 自动顺延到 8081 等，以前端窗口输出为准）、后端 9999、MySQL 3306。
- 脚本采用 GBK 编码 + CRLF 行尾，保证中文 Windows 控制台正常显示；编辑时请保持该编码。

### 1. 准备数据库

确保 MySQL 已启动，然后导入主库脚本：

```bash
mysql -u root -p < database/blog_system.sql
```

后端默认读取以下数据库配置：

```yaml
server:
  port: 9999

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog_system?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 123456
```

也可以通过环境变量覆盖：

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

### 2. 启动后端

方式 A：使用 IDE

1. 用 IntelliJ IDEA 或其他 Java IDE 打开 `blog_backend`。
2. 等待 Maven 依赖导入完成。
3. 运行 `com.example.blog_backend.BlogBackendApplication`。
4. 访问 `http://localhost:9999/hello`，返回 `hello world` 即启动成功。

方式 B：使用命令行 Maven

当前仓库没有 `mvnw` / `mvnw.cmd`，命令行运行前需要本机已安装 Maven。

```bash
cd blog_backend
mvn spring-boot:run
```

打包运行：

```bash
cd blog_backend
mvn clean package
java -jar target/blog_backend-0.0.1-SNAPSHOT.jar
```

### 3. 启动前端

```bash
cd blog_frontend/vue
npm install
npm run serve
```

Vue CLI 默认会启动到 `http://localhost:8080`。前端 Axios 基础地址写在 `src/utils/request.js`，当前固定为：

```js
baseURL: 'http://localhost:9999'
```

生产构建：

```bash
cd blog_frontend/vue
npm run build
```

构建产物输出到 `blog_frontend/vue/dist/`。

## 开发与迭代提示

- 跨域已收敛为白名单：`blog.cors-allowed-origins`（环境变量 `CORS_ALLOWED_ORIGINS`）；生产建议同域反代（前端走 `/api`）后可留空。
- 登录防爆破：同 IP 或同账号 15 分钟内失败 5 次即临时锁定 15 分钟；密码使用 BCrypt（存量 SHA256/明文登录后自动升级）。
- 部署材料见 `deploy/DEPLOY.md`：nginx 同域反代（登录限流 + 安全头）、systemd 服务、MySQL 备份脚本、低权限建库脚本。
- 前端访问后端的基址走 `process.env.VUE_APP_API_BASE`（见 `blog_frontend/vue/.env.example`），未配置时回退 `http://localhost:9999`；换域名/部署只需改 `.env`，不再改源码。
- 前端路由使用 `history` 模式，部署到 Nginx 或其他静态服务器时，需要配置 fallback 到 `index.html`。
- 当前已有轻量登录和后台访问控制，但 Token 保存在后端内存中，后端重启后需要重新登录。
- 文章正文按 Markdown 渲染，渲染结果经 DOMPurify 消毒；写作时可放心使用标准 Markdown 语法。
- 正文与封面中的站内图片存**相对路径** `/uploads/...`，前端渲染时拼接 `request.js` 导出的 `API_BASE`；部署换域名只改一处。
- 站点信息集中在 `src/config/site.js`（代码默认值）+ 后台 `site_config` 表（运行时覆盖）：站点名 / slogan / identity / 首页简介 `aboutLines` / 详情页个人资料 `profile` / 肖像 `portrait` / 社交链接 `socials` / 备案号；前台统一读 `src/store/site.js` 的 `siteState`（`App.vue` 启动时 `GET /site` 合并）。社交入口与页脚图标均由这份配置驱动，留空的项自动隐藏；后台「站点信息」页可可视化修改，无需改代码。
- RSS 订阅源 `/feed.xml` 的站点标题与前台基址配置在后端 `application.yml` 的 `blog.site-title` / `blog.site-url`（站点地址可用环境变量 `BLOG_SITE_URL` 覆盖）。
- 首页与文章管理已是服务端分页；`selectAll` / `selectSearch` 旧接口仍返回全文，仅保留兼容（站内搜索已改用 `/article/search`）。
- 站内搜索当前用 `LIKE '%关键词%'` 扫标题/摘要/正文，文章量上千后建议换 MySQL 全文索引或外部检索。
- 标签：`/tag/selectAll` 公开读，写操作（新增/改名/删除）需管理员；文章通过 `article_tag` 关联多个标签，标签计数只统计已发布文章；首页支持 `/?tag=标签ID` 直达筛选。
- 游客直接访问草稿文章详情会返回 `403`。
- `.gitignore` 已忽略 `target/`、`node_modules/`、`dist/`、IDE 配置、日志和环境文件。

## 当前本机检查结果

- 已确认本机可用：
  - Java 21.0.10
  - Node.js 22.17.1
  - npm 10.9.2
  - MySQL CLI 8.0.46
- 当前命令行未找到 `mvn`，但 `~/.m2/wrapper/dists` 下有可直接调用的 Apache Maven 3.9.16（`bin/mvn.cmd`），可用于命令行编译验证。
- 已执行 `npm run build`，前端构建成功。
- 构建时有 vendor 包体积警告，主要来自 Element UI 依赖；不影响当前运行，但属于后续性能优化点。
