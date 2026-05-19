# ClassNote Local

本地优先的课堂笔记管理应用，基于 Spring Boot + MySQL + Vue 3 构建。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 后端框架，内嵌 Tomcat |
| MySQL | 8.0 | 本地数据库 |
| JPA/Hibernate | - | ORM，自动建表 |
| Vue 3 | 3.4+ | 前端框架 |
| Element Plus | 2.7+ | UI 组件库 |
| Pinia | 2.1+ | 状态管理 |
| Vue Router | 4.3+ | 前端路由 |
| Axios | 1.7+ | HTTP 客户端 |
| Marked | 12.0+ | Markdown 渲染 |
| Vite | 5.2+ | 构建工具 |

## 功能

- 笔记本管理（创建、编辑、删除，支持级联删除笔记及附件）
- 笔记管理（Markdown 编辑 + 实时预览，单条/批量删除）
- 标签管理（全局唯一标签，颜色标识，按标签筛选笔记）
- 全文搜索（标题+内容匹配，关键词高亮，支持笔记本/标签组合筛选）
- 附件管理（上传图片/文件，自动生成 Markdown 引用，清理无引用文件）
- 数据导出（单篇笔记或整个笔记本导出为 Markdown 文件）
- 草稿自动保存（可配置间隔，覆盖式保存最近一份）
- 主题切换（浅色/深色模式，偏好持久化）

## 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Node.js 18+（前端开发）

## 快速开始

### 1. 创建数据库

```sql
CREATE DATABASE classnote DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

或者直接执行项目中的 `schema.sql`：

```bash
mysql -u root -p < schema.sql
```

### 2. 配置数据库连接

编辑 `src/main/resources/application.yml`，修改数据库用户名和密码：

```yaml
spring:
  datasource:
    username: root
    password: your_password
```

### 3. 启动后端

```bash
mvn spring-boot:run
```

后端启动后访问 `http://localhost:8080`，接口基础路径为 `/api/v1`。

### 4. 启动前端（开发模式）

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器运行在 `http://localhost:5173`，API 请求自动代理到后端。

### 5. 生产部署

```bash
cd frontend
npm run build
cd ..
mvn package -DskipTests
java -jar target/classnote-local-1.0.0.jar
```

前端静态资源打包到 `src/main/resources/static`，由 Spring Boot 直接提供服务，访问 `http://localhost:8080` 即可。

## 项目结构

```
ClassingNote/
├── pom.xml
├── schema.sql                          # 建库建表 SQL
├── src/main/java/com/classnote/
│   ├── ClassNoteApplication.java       # 启动类
│   ├── common/
│   │   └── ApiResponse.java            # 统一响应封装
│   ├── config/
│   │   ├── WebConfig.java              # CORS + 静态资源配置
│   │   └── FileStorageConfig.java       # 文件存储路径配置
│   ├── entity/                         # JPA 实体
│   ├── repository/                     # 数据访问层
│   ├── dto/                            # 请求/响应 DTO
│   ├── service/                        # 业务逻辑层
│   ├── controller/                     # REST 控制器（25 个接口）
│   └── exception/                      # 异常处理
├── src/main/resources/
│   └── application.yml                 # 应用配置
├── frontend/                           # Vue 3 前端
│   ├── src/
│   │   ├── api/                        # API 封装
│   │   ├── router/                     # 路由配置
│   │   ├── stores/                     # Pinia 状态管理
│   │   ├── views/                      # 页面视图
│   │   ├── components/                 # 公共组件
│   │   └── styles/                     # 全局样式
│   └── vite.config.js
└── docs/
    ├── API-DOCUMENTATION.md            # API 接口文档
    └── superpowers/plans/              # 实现计划
```

## API 接口

详见 [API-DOCUMENTATION.md](docs/API-DOCUMENTATION.md)，共 25 个 REST 接口：

- `POST /api/v1/notebooks` — 创建笔记本
- `GET /api/v1/notebooks` — 笔记本列表
- `GET /api/v1/notebooks/{id}` — 笔记本详情
- `PUT /api/v1/notebooks/{id}` — 编辑笔记本
- `DELETE /api/v1/notebooks/{id}` — 删除笔记本
- `POST /api/v1/notes` — 新建笔记
- `GET /api/v1/notes` — 笔记列表
- `GET /api/v1/notes/{id}` — 笔记详情
- `PUT /api/v1/notes/{id}` — 编辑笔记
- `DELETE /api/v1/notes/{id}` — 删除笔记
- `DELETE /api/v1/notes/batch` — 批量删除笔记
- `POST /api/v1/tags` — 创建标签
- `GET /api/v1/tags` — 标签列表
- `PUT /api/v1/tags/{id}` — 编辑标签
- `DELETE /api/v1/tags/{id}` — 删除标签
- `GET /api/v1/search` — 全文搜索
- `POST /api/v1/attachments/upload` — 上传附件
- `GET /api/v1/notes/{noteId}/attachments` — 笔记附件列表
- `DELETE /api/v1/attachments/{id}` — 删除附件
- `GET /api/v1/export/notes/{id}` — 导出笔记
- `GET /api/v1/export/notebooks/{id}` — 导出笔记本
- `POST /api/v1/drafts` — 保存草稿
- `GET /api/v1/drafts/latest` — 获取草稿
- `DELETE /api/v1/drafts` — 清除草稿
- `GET /api/v1/preferences` — 获取偏好
- `PUT /api/v1/preferences` — 更新偏好
- `GET /api/v1/config` — 应用配置
- `GET /api/v1/config/db-status` — 数据库连接状态

## 配置说明

所有配置位于 `src/main/resources/application.yml`：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `server.port` | 8080 | 服务端口 |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/classnote` | 数据库连接 |
| `spring.jpa.hibernate.ddl-auto` | `update` | 自动建表策略 |
| `spring.servlet.multipart.max-file-size` | 50MB | 上传文件大小限制 |
| `app.attachment-root-path` | `~/classnote-files` | 附件存储根目录 |

## 许可

MIT
