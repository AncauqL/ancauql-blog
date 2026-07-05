# 用户与权限系统调试记录

创建日期：2026-07-05

本文档用于人工 debug。每次权限系统相关改动，都把做了什么、为什么做、如何验证、可能的坑记录在这里。

## 目标

实现三类访问身份：

- 游客：无需登录，只能访问前台公开页面，只能查看已发布文章。
- 管理员：登录后可以管理文章和分类。
- 超级管理员：登录后拥有全部后台权限，包括账号管理。

当前内置初始账号：

```text
账号：admin
密码：123456
角色：SUPER_ADMIN
```

## 实施步骤记录

### 1. 建立 Git 安全基线

- 提交了 `PROJECT_OVERVIEW.md`。
- 提交信息：`docs: add project overview`。
- 从该提交创建功能分支：`feature/auth-permission`。

注意：

- `.gitignore` 在开工前已有未提交改动：`.报告/` 改为 `报告/`。
- 为避免混入未知来源改动，基线提交没有包含 `.gitignore`。

### 2. 后端用户模型重构

变更文件：

- `blog_backend/src/main/java/com/example/blog_backend/entity/User.java`
- `blog_backend/src/main/java/com/example/blog_backend/service/IUserService.java`
- `blog_backend/src/main/java/com/example/blog_backend/service/impl/UserServiceImpl.java`
- `blog_backend/src/main/resources/mappers/UserMapper.xml`

变更内容：

- 将早期练习字段 `userName`、`age`、`sex`、`phone`、`address` 改为博客账号字段。
- 当前账号字段为 `username`、`password`、`nickname`、`role`、`email`、`createTime`。
- 返回用户列表时隐藏 `password`。
- 新增 `login` 方法。

原因：

- 当前默认数据库是 `blog_system`，其中 `user` 表字段与旧 Java 实体不一致。
- 如果不先统一模型，后续登录和权限会建立在错误字段上。

### 3. 密码兼容与升级

新增文件：

- `blog_backend/src/main/java/com/example/blog_backend/common/PasswordUtil.java`

规则：

- 新密码保存为 `SHA256:哈希值`。
- 老数据库中的明文密码仍可登录。
- 明文密码登录成功后，会自动更新为 `SHA256:` 格式。

调试点：

- 如果第一次用 `admin / 123456` 登录成功，数据库中 `user.password` 会从 `123456` 变成 `SHA256:...`。
- 如果登录失败，先检查本地库里 `user` 表是否来自 `blog_system.sql`。

### 4. 角色规则

新增文件：

- `blog_backend/src/main/java/com/example/blog_backend/common/RoleUtil.java`

角色：

- `SUPER_ADMIN`：超级管理员。
- `ADMIN`：管理员。
- `GUEST`：游客，不写入数据库，仅作为概念。

兼容规则：

- `admin` 账号如果数据库中角色仍是旧值 `admin`，后端会兼容识别为 `SUPER_ADMIN`。
- 其他未知角色默认按 `ADMIN` 处理。

### 5. Token 登录态

新增文件：

- `blog_backend/src/main/java/com/example/blog_backend/service/AuthTokenService.java`
- `blog_backend/src/main/java/com/example/blog_backend/common/AuthContext.java`
- `blog_backend/src/main/java/com/example/blog_backend/dto/LoginRequest.java`
- `blog_backend/src/main/java/com/example/blog_backend/dto/LoginResponse.java`
- `blog_backend/src/main/java/com/example/blog_backend/dto/UserProfile.java`

实现方式：

- 登录成功后生成随机 Token。
- Token 保存在后端内存中，有效期 8 小时。
- 前端后续请求通过 `Authorization: Bearer token` 传回后端。

限制：

- 后端重启后 Token 会失效，需要重新登录。
- 当前不是 JWT，不适合多后端实例共享登录态。

### 6. 后端权限拦截器

新增文件：

- `blog_backend/src/main/java/com/example/blog_backend/config/AuthInterceptor.java`
- `blog_backend/src/main/java/com/example/blog_backend/config/WebConfig.java`

放行：

- `POST /auth/login`
- `GET /hello`
- `GET /article/**`
- `GET /category/**`

需要管理员：

- `POST /article`
- `DELETE /article/delete`
- `POST /category`
- `DELETE /category/delete`
- `GET /auth/me`
- `POST /auth/logout`

需要超级管理员：

- `/user/**`

返回规则：

- 未登录：`code = 401`
- 权限不足：`code = 403`

### 7. 文章接口游客过滤

变更文件：

- `blog_backend/src/main/java/com/example/blog_backend/controller/ArticleController.java`
- `blog_backend/src/main/java/com/example/blog_backend/service/IArticleService.java`
- `blog_backend/src/main/java/com/example/blog_backend/service/impl/ArticleServiceImpl.java`

行为：

- 游客请求 `/article/selectAll` 时，只返回 `status = published` 的文章。
- 管理员请求 `/article/selectAll` 时，返回全部文章。
- 游客直接访问草稿详情时返回 `403`。

调试点：

- 如果游客还能看到草稿，检查请求头是否带了管理员 Token。
- 如果后台文章管理看不到草稿，检查前端是否正确带上 `Authorization`。

### 8. 数据库脚本更新

变更文件：

- `database/blog_system.sql`

变更内容：

- `user.role` 默认值从 `admin` 改为 `ADMIN`。
- 初始化账号 `admin` 的角色改为 `SUPER_ADMIN`。
- 初始化密码仍保留为 `123456`，由首次登录自动升级为哈希。
- `user.username` 增加唯一索引 `uk_user_username`。

本地已有数据库时的手动修正 SQL：

```sql
USE blog_system;

UPDATE user
SET role = 'SUPER_ADMIN'
WHERE username = 'admin';

ALTER TABLE user
ADD UNIQUE KEY uk_user_username (username);
```

如果添加唯一索引失败，通常说明本地 `user` 表中已有重复 `username`，需要先清理重复账号。

### 9. 前端登录态接入

变更文件：

- `blog_frontend/vue/src/utils/request.js`
- `blog_frontend/vue/src/router/index.js`
- `blog_frontend/vue/src/App.vue`
- `blog_frontend/vue/src/views/Login.vue`

变更内容：

- 登录成功后保存：
  - `localStorage.blog_token`
  - `localStorage.blog_user`
- Axios 请求自动带：
  - `Authorization: Bearer token`
- 新增 `/login` 页面。
- `/article`、`/category` 需要 `SUPER_ADMIN` 或 `ADMIN`。
- `/user` 只允许 `SUPER_ADMIN`。
- 未登录访问后台页面时跳转到 `/login?redirect=原地址`。
- 已登录用户打开 `/login` 会跳到 `/article`。
- 侧边栏按角色显示菜单：
  - 游客：系统首页、关于我、登录。
  - 管理员：分类管理、文章管理。
  - 超级管理员：额外显示账号管理。

调试点：

- 如果登录后菜单没刷新，检查 `localStorage.blog_user` 是否存在。
- 如果后台接口返回 `401`，检查请求头是否有 `Authorization`。
- 如果页面路由能进但接口失败，说明前端路由守卫通过了，但后端 Token 可能已过期或服务重启过。
- 如果浏览器残留 Token 但没有用户信息，路由守卫会清理本地登录态并要求重新登录。
- 如果接口返回 `401`，Axios 响应拦截器会清理 `blog_token` 和 `blog_user`。

### 10. 账号管理页重构

变更文件：

- `blog_frontend/vue/src/views/User.vue`

变更内容：

- 旧的姓名、年龄、电话用户页改为账号管理页。
- 字段改为账号、昵称、邮箱、角色、密码。
- 新增账号必须填写密码。
- 编辑账号时密码留空表示不修改。
- 禁止在前端删除当前登录账号。
- 后端也增加了当前账号不可删除的保护。
- 后端禁止修改当前登录账号的角色，避免超级管理员把自己降级后锁出账号管理。

## 当前待完成

- 后端编译验证。
- 如本机 MySQL 已有旧库，需要手动执行数据库修正 SQL。

## 已执行验证

### 前端构建

命令：

```bash
cd blog_frontend/vue
npm run build
```

结果：

- 构建成功。
- 仍有 Element UI vendor 包体积警告。
- 该警告在权限系统改动前已存在，不影响当前功能验证。

### 后端编译

当前结果：

- 本机命令行未找到 `mvn`。
- 仓库没有 `mvnw.cmd`。
- 因此本轮尚未通过命令行执行 `mvn test` 或 `mvn package`。

后续验证方式：

- 在 IDE 中刷新 Maven 并运行 `BlogBackendApplication`。
- 或安装 Maven 后执行：

```bash
cd blog_backend
mvn test
```

## 快速后端验证命令

登录：

```bash
curl -X POST http://localhost:9999/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"123456\"}"
```

拿到 Token 后访问用户列表：

```bash
curl http://localhost:9999/user/selectAll \
  -H "Authorization: Bearer 你的Token"
```

游客访问文章：

```bash
curl http://localhost:9999/article/selectAll
```

游客尝试删除文章，应该返回 `401`：

```bash
curl -X DELETE "http://localhost:9999/article/delete?id=1"
```
