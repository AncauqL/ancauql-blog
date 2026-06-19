# 个人博客 Java 大作业爆改思路

## 1. 当前项目是什么

当前项目本质上是一个前后端分离的信息管理系统模板。

- 后端：Spring Boot + MyBatis-Plus + MySQL
- 前端：Vue 2 + Element UI + Axios
- 已有功能：`user` 用户信息的分页查询、新增、编辑、删除
- 已有页面：后台布局、侧边栏、用户管理页面
- 已有数据库：`springboot` 库，当前只有 `user` 表

现在它还不是完整博客系统，但它已经具备一个大作业最核心的模板结构：

```text
Vue 页面 -> Axios 请求 -> Controller -> Service -> Mapper -> MySQL 表
```

后面改博客系统，不需要推倒重来。最稳的办法是：把现有 `User` 这一套当作样板，复制改造成 `Article`、`Category`、`Comment` 等模块。

## 2. 目标项目定位

建议把项目定位为：

```text
个人博客后台管理系统 + 简单博客前台展示
```

如果只做后台管理，像一个普通 CRUD 作业；如果加一个前台展示页面，就更像真正的个人博客。

最终项目可以叫：

```text
个人博客信息管理系统
```

或者：

```text
基于 Spring Boot + Vue 的个人博客系统
```

## 3. 可交差版本应该有哪些内容

最小可交差版本建议包含这些功能：

- 登录功能：管理员登录后台
- 首页仪表盘：显示文章数、分类数、评论数、访问量等统计信息
- 文章管理：文章新增、编辑、删除、分页查询、按标题搜索
- 分类管理：分类新增、编辑、删除、查询
- 评论管理：查看评论、删除评论、按文章筛选评论
- 用户管理：保留当前 `user` 表，改成管理员/用户账号管理
- 前台博客首页：展示文章列表
- 前台文章详情：展示文章标题、时间、分类、正文
- 关于我页面：展示个人介绍

如果时间有限，优先级如下：

```text
必须做：登录、文章管理、分类管理、前台文章列表、文章详情
建议做：评论管理、首页统计、关于我
加分做：标签、文章封面、Markdown 编辑器、浏览量、状态发布/草稿
```

## 4. 网站内容设计

### 4.1 前台页面

前台是给访客看的博客页面，建议做得简单但完整。

页面可以包括：

- 博客首页：文章列表、分类导航、搜索框
- 文章详情页：标题、发布时间、分类、正文内容、浏览量
- 分类文章页：点击分类后，只看该分类下的文章
- 关于我页面：个人介绍、技术栈、联系方式
- 留言/评论区：可选，时间紧可以只在后台管理评论

前台布局建议：

```text
顶部导航：博客首页 / 分类 / 关于我 / 后台入口
主体区域：文章卡片列表
侧边区域：个人简介 / 分类列表 / 最新文章
底部区域：版权信息
```

文章列表卡片可以显示：

- 文章标题
- 摘要
- 分类
- 发布时间
- 浏览量
- 阅读全文按钮

### 4.2 后台页面

后台是管理员维护数据的地方，沿用现在的 Element UI 后台布局。

侧边栏建议改成：

```text
系统首页
内容管理
  文章管理
  分类管理
  评论管理
用户管理
  管理员信息
系统管理
  关于我设置
```

后台页面核心是 CRUD：

- 查询：分页表格
- 搜索：按标题、分类、状态搜索
- 新增：弹窗或独立表单
- 编辑：回显数据后修改
- 删除：带确认框

## 5. 数据库设计

### 5.1 是否还用 user 表

要用，但 `user` 表不再是博客文章本身，而是用于存储用户或管理员账号。

当前项目的 `user` 表字段偏“学生信息/用户信息”，比如姓名、年龄、性别、电话、地址。改成博客系统后，可以把它改成账号表：

```sql
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(100) NOT NULL COMMENT '账号',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `nickname` varchar(100) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
  `role` varchar(50) DEFAULT 'admin' COMMENT '角色',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8mb4;
```

简单大作业可以先不做密码加密，演示时用明文密码即可。但答辩时如果老师问，可以说正式项目应该使用 BCrypt 等方式加密。

### 5.2 文章如何存储

博客文章应该单独用 `article` 表存储。

文章正文一般有两种存储方式：

```text
方式一：存 HTML
方式二：存 Markdown 原文
```

个人博客更推荐 Markdown：

- 数据库里存 Markdown 文本
- 前端展示时把 Markdown 渲染成 HTML
- 后台编辑时用文本框或 Markdown 编辑器

文章表可以这样设计：

```sql
CREATE TABLE `article` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `title` varchar(255) NOT NULL COMMENT '文章标题',
  `summary` varchar(500) DEFAULT NULL COMMENT '文章摘要',
  `content` longtext NOT NULL COMMENT '文章正文，建议存Markdown',
  `cover` varchar(500) DEFAULT NULL COMMENT '封面图片地址',
  `category_id` int DEFAULT NULL COMMENT '分类ID',
  `user_id` int DEFAULT NULL COMMENT '作者ID',
  `status` varchar(20) DEFAULT 'published' COMMENT '状态：draft草稿，published已发布',
  `view_count` int DEFAULT 0 COMMENT '浏览量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8mb4;
```

正文示例：

```markdown
# 我的第一篇博客

今天学习了 Spring Boot 和 Vue。

## 学习内容

- Controller
- Service
- Mapper
- MySQL
```

数据库里存的就是上面这段 Markdown 文本。

### 5.3 分类表

文章分类单独建 `category` 表。

```sql
CREATE TABLE `category` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(100) NOT NULL COMMENT '分类名称',
  `description` varchar(255) DEFAULT NULL COMMENT '分类描述',
  `sort` int DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8mb4;
```

示例分类：

```text
Java
前端
数据库
生活随笔
学习笔记
```

### 5.4 评论表

评论功能可选，但做出来会更像博客。

```sql
CREATE TABLE `comment` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `article_id` int NOT NULL COMMENT '文章ID',
  `nickname` varchar(100) DEFAULT NULL COMMENT '评论者昵称',
  `email` varchar(100) DEFAULT NULL COMMENT '评论者邮箱',
  `content` varchar(1000) NOT NULL COMMENT '评论内容',
  `status` varchar(20) DEFAULT 'visible' COMMENT '状态：visible显示，hidden隐藏',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8mb4;
```

如果时间紧，评论只做后台列表和删除，不做前台提交。

### 5.5 标签表，可选

标签比分类更灵活，但实现比分类稍复杂。大作业时间紧可以不做。

如果要做：

```text
tag：标签表
article_tag：文章和标签的中间表
```

这属于加分项，不建议一开始就做。

## 6. 后端改造思路

后端按照当前 `User` 模块复制扩展。

当前结构：

```text
entity/User.java
mapper/UserMapper.java
service/IUserService.java
service/impl/UserServiceImpl.java
controller/UserController.java
resources/mappers/UserMapper.xml
```

改造后增加：

```text
entity/Article.java
mapper/ArticleMapper.java
service/IArticleService.java
service/impl/ArticleServiceImpl.java
controller/ArticleController.java
resources/mappers/ArticleMapper.xml

entity/Category.java
mapper/CategoryMapper.java
service/ICategoryService.java
service/impl/CategoryServiceImpl.java
controller/CategoryController.java
resources/mappers/CategoryMapper.xml

entity/Comment.java
mapper/CommentMapper.java
service/ICommentService.java
service/impl/CommentServiceImpl.java
controller/CommentController.java
resources/mappers/CommentMapper.xml
```

接口风格可以保持当前项目的写法：

```text
GET    /article/selectPage
GET    /article/selectAll
GET    /article/{id}
POST   /article
DELETE /article/delete?id=1

GET    /category/selectAll
GET    /category/selectPage
POST   /category
DELETE /category/delete?id=1
```

文章模块重点接口：

- 分页查询文章
- 按标题搜索文章
- 按分类查询文章
- 根据 ID 查询文章详情
- 新增文章
- 编辑文章
- 删除文章
- 修改文章状态

登录模块可以简单做：

```text
POST /login
```

请求参数：

```json
{
  "username": "admin",
  "password": "123456"
}
```

返回：

```json
{
  "code": "200",
  "msg": "登录成功",
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "管理员"
  }
}
```

大作业可以先用 localStorage 保存登录状态，不一定上 JWT。想做得更正规，可以后面再加 token。

## 7. 前端改造思路

当前前端的核心样板是：

```text
src/views/User.vue
src/utils/request.js
src/router/index.js
src/App.vue
```

保留当前后台布局，把菜单改成博客系统菜单。

建议新增页面：

```text
src/views/Login.vue
src/views/Dashboard.vue
src/views/Article.vue
src/views/ArticleEdit.vue
src/views/Category.vue
src/views/Comment.vue
src/views/AboutMe.vue

src/views/front/BlogHome.vue
src/views/front/ArticleDetail.vue
src/views/front/About.vue
```

后台文章管理页面可以参考 `User.vue` 的结构：

```text
搜索栏：标题、分类、状态
表格：标题、分类、状态、浏览量、创建时间、操作
分页：沿用现有分页
操作：新增、编辑、删除、发布/草稿
```

文章编辑页面建议做成独立页面，不建议塞在小弹窗里。因为文章正文会很长。

文章编辑字段：

- 标题
- 摘要
- 分类
- 封面图地址
- 状态
- 正文

正文输入先用普通 `<el-input type="textarea">` 就能交差。后面想漂亮一点，再换 Markdown 编辑器。

前台博客首页可以不使用复杂布局，先做到：

```text
文章列表 + 分类筛选 + 搜索 + 点击进入详情
```

## 8. 数据流怎么理解

以“文章列表”为例：

```text
Article.vue 页面加载
-> 调用 request.get('/article/selectPage')
-> ArticleController 接收请求
-> ArticleServiceImpl 处理分页和搜索条件
-> ArticleMapper 查询 article 表
-> 返回 Result.success(pageData)
-> 前端把 records 填进 el-table
```

以“新增文章”为例：

```text
后台填写文章表单
-> 点击提交
-> request.post('/article', form)
-> ArticleController 判断 id 是否为空
-> id 为空则新增，id 不为空则更新
-> 保存到 article 表
-> 前端提示提交成功并刷新列表
```

这和当前用户管理逻辑几乎一样，只是字段从 `userName/age/sex` 换成了 `title/summary/content/categoryId`。

## 9. 推荐改造顺序

不要一口气全改，按这个顺序最稳。

### 第一步：备份当前项目

先确保当前用户管理能跑通。

把当前数据库导出到：

```text
database/springboot.sql
```

后面每改完一个阶段，都可以再导出一次 SQL。

### 第二步：调整数据库

新建或改造这些表：

```text
user
category
article
comment
```

建议新建一个更像博客项目的库：

```text
blog_system
```

然后修改 `application.yml`：

```text
jdbc:mysql://localhost:3306/blog_system
```

### 第三步：先做分类管理

分类字段少，最容易成功。

做完后你会得到：

```text
Category.java
CategoryMapper.java
CategoryServiceImpl.java
CategoryController.java
Category.vue
```

分类管理跑通后，说明你已经会复制一套 CRUD 模块了。

### 第四步：做文章管理

文章管理是核心。

先做最简单版本：

- 标题
- 摘要
- 正文
- 分类
- 状态

先不要急着做图片上传。封面图可以直接存图片 URL。

### 第五步：做前台文章展示

前台先做两个页面：

```text
/blog
/blog/article/:id
```

首页查文章列表，详情页按 ID 查文章内容。

### 第六步：做登录

登录可以晚一点做，因为它会影响路由守卫、localStorage、菜单显示。

简单版本：

- 输入账号密码
- 后端查 `user` 表
- 成功后前端保存用户信息
- 未登录时不允许访问后台页面

### 第七步：做评论和统计

评论和首页统计属于完善项目。

首页统计可以显示：

- 文章总数
- 分类总数
- 评论总数
- 总浏览量

## 10. 页面命名建议

为了让大作业看起来像“信息管理系统”，菜单和页面名称可以这样定：

```text
系统首页
文章管理
分类管理
评论管理
用户管理
关于我管理
博客前台
```

前台导航可以这样：

```text
首页
分类
归档
关于我
后台管理
```

如果时间不够，“归档”可以不做。

## 11. 大作业答辩时可以怎么介绍

可以按这个逻辑讲：

```text
本系统是一个基于 Spring Boot 和 Vue 的个人博客信息管理系统。
系统分为前台博客展示和后台内容管理两部分。
前台用于展示文章列表、文章详情和个人介绍。
后台用于管理员维护文章、分类、评论和用户信息。
后端使用 Spring Boot 提供 REST 接口，使用 MyBatis-Plus 操作 MySQL 数据库。
前端使用 Vue 和 Element UI 实现管理界面，通过 Axios 调用后端接口。
```

核心技术点：

- Spring Boot 接口开发
- MyBatis-Plus 数据库操作
- MySQL 表设计
- Vue 组件化页面
- Element UI 表格、表单、分页
- Axios 前后端交互
- 前后端分离架构

## 12. 可以写进论文/报告的系统模块

系统功能模块：

```text
用户登录模块
文章管理模块
分类管理模块
评论管理模块
前台展示模块
系统统计模块
```

数据库模块：

```text
用户表 user
文章表 article
分类表 category
评论表 comment
```

项目结构模块：

```text
controller：接收前端请求
service：处理业务逻辑
mapper：操作数据库
entity：数据库实体类
common：统一返回结果
views：前端页面
utils/request.js：Axios 请求封装
```

## 13. 最终交付物清单

建议最终项目里准备这些东西：

- 完整源码
- `database/blog_system.sql`
- 项目启动说明 `README.md`
- 功能截图
- 数据库表结构截图
- 课程设计报告或说明文档

启动说明至少写清楚：

```text
1. 导入数据库 SQL
2. 修改 application.yml 里的数据库账号密码
3. 启动 Spring Boot 后端
4. 启动 Vue 前端
5. 浏览器访问前端地址
```

## 14. 最小可交差版本总结

如果只剩几天，按这个范围做：

```text
后端：
user 登录
article CRUD
category CRUD

前端：
登录页
后台文章管理
后台分类管理
博客首页
文章详情页

数据库：
user
article
category
```

这个版本已经能说明：

- 有数据库设计
- 有前后端交互
- 有核心业务
- 有管理系统
- 有博客展示
- 有可演示流程

如果还有时间，再补：

```text
评论管理
浏览量统计
Markdown 渲染
首页统计卡片
文章封面
关于我管理
```
