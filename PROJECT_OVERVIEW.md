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
│     ├─ App.vue                 # 顶层布局、侧边导航
│     ├─ main.js                 # Vue 入口
│     ├─ router/index.js         # 前端路由
│     ├─ utils/request.js        # Axios 实例
│     ├─ views/                  # 页面组件
│     └─ assets/                 # 样式与 logo
└─ database/
   ├─ blog_system.sql            # 当前博客主库脚本
   └─ springboot.sql             # 早期用户管理练习库脚本
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
- Element UI 2.15.14：布局、按钮、表格、弹窗、表单、消息提示等 UI 组件。
- Axios 1.18.0：前后端请求。
- markdown-it 15：文章正文 Markdown 渲染。
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

- 首页文章列表：`/`
  - 请求 `/article/selectAll`。
  - 前端过滤掉 `status === 'draft'` 的文章。
  - 按创建时间倒序展示。
  - 客户端分页，每页 6 篇。
- 文章详情页：`/post/:id`
  - 请求 `/article/detail?id=文章ID`。
  - 正文按 Markdown 渲染：标题、列表、引用、表格、图片、代码块。
  - 代码块带语言标签、语法高亮和一键复制按钮。
  - 宽屏下右侧显示自动生成的目录，支持点击跳转和滚动高亮。
  - 元信息展示创建时间、阅读数、全文字数、预计阅读时长。
  - 底部提供上一篇 / 下一篇导航（仅限已发布文章）。
  - 游客每次打开已发布文章，阅读数自动 +1；管理员预览不计数。
  - 管理员查看草稿时显示“草稿预览”标记。
- 关于我：`/aboutme`
  - 静态个人介绍页面。

### 管理侧页面

- 分类管理：`/category`
  - 展示所有分类，按 `sort` 升序排序。
  - 支持新增、编辑、删除分类。
  - 字段包括分类名称、分类描述、排序值。
- 文章管理：`/article`
  - 展示文章表格。
  - 支持按标题搜索。
  - 支持按状态在前端过滤：已发布 / 草稿。
  - 支持新增、编辑、删除文章。
  - 字段包括标题、摘要、正文、分类、封面地址、状态、作者 ID、阅读数。
- 用户管理：`src/views/User.vue`
  - 已重构为账号管理页。
  - 仅 `SUPER_ADMIN` 可访问。
  - 支持新增、编辑、删除管理员账号。
  - 字段包括账号、昵称、邮箱、角色、密码。

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

### 登录接口

- `POST /auth/login`：登录，参数为 `username`、`password`。
- `GET /auth/me`：获取当前登录用户。
- `POST /auth/logout`：退出登录。

### 文章接口

- `GET /article/selectAll`：查询全部文章。
- `GET /article/detail?id=文章ID`：按 ID 查询文章详情；游客访问已发布文章时阅读数 +1。
- `GET /article/neighbors?id=文章ID`：查询上一篇 / 下一篇（按发布时间排序，仅返回已发布文章的 id 和标题）。
- `GET /article/selectSearch?articleTitle=关键词`：按标题模糊搜索。
- `GET /article/selectPage?pageNum=1&pageSize=10&articleTitle=关键词`：分页查询。
- `POST /article`：新增或编辑文章；请求体带 `id` 时编辑，不带 `id` 时新增。
- `DELETE /article/delete?id=文章ID`：删除文章。

### 分类接口

- `GET /category/selectAll`：查询全部分类。
- `GET /category/selectSearch?categoryName=关键词`：按名称模糊搜索。
- `GET /category/selectPage?pageNum=1&pageSize=10&categoryName=关键词`：分页查询。
- `POST /category`：新增或编辑分类；请求体带 `id` 时编辑，不带 `id` 时新增。
- `DELETE /category/delete?id=分类ID`：删除分类。

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
- `cover`：封面图片地址，当前前端还没有实际展示。
- `category_id`：分类 ID。
- `user_id`：作者 ID。
- `status`：文章状态，当前使用 `published` / `draft`。
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
- `password`：密码。新密码保存为 `SHA256:` 前缀格式；旧明文密码首次登录成功后会自动升级。
- `nickname`：昵称。
- `role`：角色，当前使用 `SUPER_ADMIN` / `ADMIN`。
- `email`：邮箱。
- `create_time`：创建时间。

### 早期库：`database/springboot.sql`

- 库名为 `springboot`。
- 只有 `user` 表。
- 字段结构对应早期用户管理练习，不再对应当前账号系统。
- 当前后端默认不连接该库。

## 启动方式

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

- 后端跨域当前使用 `@CrossOrigin(origins = "*")`，开发方便，但上线前建议改成明确域名。
- 前端请求地址目前硬编码为 `http://localhost:9999`，后续建议改为 `.env` 配置。
- 前端路由使用 `history` 模式，部署到 Nginx 或其他静态服务器时，需要配置 fallback 到 `index.html`。
- 当前已有轻量登录和后台访问控制，但 Token 保存在后端内存中，后端重启后需要重新登录。
- 文章正文按 Markdown 渲染，渲染结果经 DOMPurify 消毒；写作时可放心使用标准 Markdown 语法。
- `cover` 字段已经存在，但首页、详情页、文章管理表格当前都没有真正使用封面展示。
- 首页是客户端分页；后端已经有 `/article/selectPage`，后续文章变多后可以切成服务端分页。
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
