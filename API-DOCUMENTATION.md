# ClassNote Local — API 接口文档

**版本**: 1.0.0  
**基础路径**: `http://localhost:8080/api/v1`  
**数据格式**: JSON (Content-Type: application/json)  
**字符编码**: UTF-8

---

## 通用约定

### 响应格式

所有接口统一返回以下结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 业务状态码，200 表示成功 |
| message | string | 提示信息 |
| data | object/array/null | 响应数据 |

### 分页响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [],
    "page": 1,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

### 错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 409 | 资源冲突（如标签名重复） |
| 500 | 服务器内部错误 |

---

## 1. 笔记本管理

### 1.1 创建笔记本

**POST** `/notebooks`

**请求体**:

```json
{
  "name": "高等数学",
  "description": "大一上学期高数笔记"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 笔记本名称，1-100 字符 |
| description | string | 否 | 描述，最长 500 字符 |

**响应**:

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "高等数学",
    "description": "大一上学期高数笔记",
    "noteCount": 0,
    "createdAt": "2026-05-19T10:30:00",
    "updatedAt": "2026-05-19T10:30:00"
  }
}
```

---

### 1.2 获取笔记本列表

**GET** `/notebooks`

**请求参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 20 | 每页条数（最大 50） |
| sort | string | 否 | updatedAt,desc | 排序字段，可选: name/createdAt/updatedAt |

**响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": "a1b2c3d4-...",
        "name": "高等数学",
        "description": "大一上学期高数笔记",
        "noteCount": 15,
        "createdAt": "2026-05-19T10:30:00",
        "updatedAt": "2026-05-19T18:00:00"
      }
    ],
    "page": 1,
    "size": 20,
    "totalElements": 5,
    "totalPages": 1
  }
}
```

---

### 1.3 获取单个笔记本

**GET** `/notebooks/{id}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 笔记本 ID (UUID) |

**响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "a1b2c3d4-...",
    "name": "高等数学",
    "description": "大一上学期高数笔记",
    "noteCount": 15,
    "createdAt": "2026-05-19T10:30:00",
    "updatedAt": "2026-05-19T18:00:00"
  }
}
```

---

### 1.4 编辑笔记本

**PUT** `/notebooks/{id}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 笔记本 ID |

**请求体**:

```json
{
  "name": "高等数学（修订版）",
  "description": "更新后的描述"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 否 | 笔记本名称 |
| description | string | 否 | 描述 |

**响应**: 返回更新后的笔记本对象（同 1.3）

---

### 1.5 删除笔记本

**DELETE** `/notebooks/{id}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 笔记本 ID |

**响应**:

```json
{
  "code": 200,
  "message": "删除成功，已同时删除 15 篇笔记",
  "data": null
}
```

> 删除笔记本时级联删除其下所有笔记及关联附件，需前端二次确认。

---

## 2. 笔记管理

### 2.1 新建笔记

**POST** `/notes`

**请求体**:

```json
{
  "notebookId": "a1b2c3d4-...",
  "title": "第一章 极限与连续",
  "content": "# 极限\n\n## 定义\n...",
  "tagIds": ["tag-id-1", "tag-id-2"]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| notebookId | string | 是 | 所属笔记本 ID |
| title | string | 是 | 笔记标题，1-200 字符 |
| content | string | 否 | Markdown 内容 |
| tagIds | string[] | 否 | 关联标签 ID 列表 |

**响应**:

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": "note-uuid-...",
    "notebookId": "a1b2c3d4-...",
    "notebookName": "高等数学",
    "title": "第一章 极限与连续",
    "content": "# 极限\n\n## 定义\n...",
    "plainText": "极限 定义 ...",
    "tags": [
      { "id": "tag-id-1", "name": "数学", "color": "#409EFF" }
    ],
    "createdAt": "2026-05-19T10:30:00",
    "updatedAt": "2026-05-19T10:30:00"
  }
}
```

---

### 2.2 获取笔记列表

**GET** `/notes`

**请求参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| notebookId | string | 否 | - | 按笔记本筛选 |
| tagId | string | 否 | - | 按标签筛选 |
| keyword | string | 否 | - | 关键词搜索（匹配标题+内容） |
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 20 | 每页条数（最大 50） |
| sort | string | 否 | updatedAt,desc | 排序: title/createdAt/updatedAt |

**响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": "note-uuid-...",
        "notebookId": "a1b2c3d4-...",
        "notebookName": "高等数学",
        "title": "第一章 极限与连续",
        "plainText": "极限 定义 ...",
        "tags": [
          { "id": "tag-id-1", "name": "数学", "color": "#409EFF" }
        ],
        "createdAt": "2026-05-19T10:30:00",
        "updatedAt": "2026-05-19T18:00:00"
      }
    ],
    "page": 1,
    "size": 20,
    "totalElements": 42,
    "totalPages": 3
  }
}
```

> 列表不返回完整 `content` 字段，仅返回 `plainText` 摘要（前 200 字符）。

---

### 2.3 获取笔记详情

**GET** `/notes/{id}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 笔记 ID |

**响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "note-uuid-...",
    "notebookId": "a1b2c3d4-...",
    "notebookName": "高等数学",
    "title": "第一章 极限与连续",
    "content": "# 极限\n\n## 定义\n...",
    "plainText": "极限 定义 ...",
    "tags": [
      { "id": "tag-id-1", "name": "数学", "color": "#409EFF" },
      { "id": "tag-id-2", "name": "期末考试重点", "color": "#F56C6C" }
    ],
    "attachments": [
      {
        "id": "att-uuid-...",
        "fileName": "limit-graph.png",
        "filePath": "/attachments/2026/05/limit-graph.png",
        "fileSize": 245760,
        "mimeType": "image/png"
      }
    ],
    "createdAt": "2026-05-19T10:30:00",
    "updatedAt": "2026-05-19T18:00:00"
  }
}
```

---

### 2.4 编辑笔记

**PUT** `/notes/{id}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 笔记 ID |

**请求体**:

```json
{
  "notebookId": "a1b2c3d4-...",
  "title": "第一章 极限与连续（修订）",
  "content": "# 极限\n\n更新后的内容...",
  "tagIds": ["tag-id-1"]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| notebookId | string | 否 | 更换所属笔记本 |
| title | string | 否 | 标题 |
| content | string | 否 | Markdown 内容 |
| tagIds | string[] | 否 | 覆盖式更新标签关联 |

**响应**: 返回更新后的笔记详情（同 2.3）

---

### 2.5 删除笔记

**DELETE** `/notes/{id}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 笔记 ID |

**响应**:

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 2.6 批量删除笔记

**DELETE** `/notes/batch`

**请求体**:

```json
{
  "ids": ["note-id-1", "note-id-2", "note-id-3"]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | string[] | 是 | 笔记 ID 数组，最多 100 条 |

**响应**:

```json
{
  "code": 200,
  "message": "已成功删除 3 篇笔记",
  "data": {
    "deletedCount": 3,
    "cleanedAttachments": 5
  }
}
```

---

## 3. 标签管理

### 3.1 创建标签

**POST** `/tags`

**请求体**:

```json
{
  "name": "期末考试重点",
  "color": "#F56C6C"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 标签名称，全局唯一，1-30 字符 |
| color | string | 否 | 颜色标识，十六进制格式，默认 #409EFF |

**响应**:

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": "tag-uuid-...",
    "name": "期末考试重点",
    "color": "#F56C6C",
    "noteCount": 0,
    "createdAt": "2026-05-19T10:30:00"
  }
}
```

---

### 3.2 获取标签列表

**GET** `/tags`

**请求参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| sort | string | 否 | name,asc | 排序: name/noteCount/createdAt |

**响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "tag-uuid-...",
      "name": "期末考试重点",
      "color": "#F56C6C",
      "noteCount": 8,
      "createdAt": "2026-05-19T10:30:00"
    }
  ]
}
```

> 标签列表不分页，全量返回。

---

### 3.3 编辑标签

**PUT** `/tags/{id}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 标签 ID |

**请求体**:

```json
{
  "name": "期末重点",
  "color": "#E6A23C"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 否 | 标签名称 |
| color | string | 否 | 颜色 |

**响应**: 返回更新后的标签对象

---

### 3.4 删除标签

**DELETE** `/tags/{id}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 标签 ID |

**响应**:

```json
{
  "code": 200,
  "message": "删除成功，已解除与 8 篇笔记的关联",
  "data": null
}
```

> 删除标签时仅解除关联，不删除笔记。

---

## 4. 搜索

### 4.1 全文搜索

**GET** `/search`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 是 | 搜索关键词，最少 1 字符 |
| notebookId | string | 否 | 限定笔记本范围 |
| tagId | string | 否 | 限定标签范围 |
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 20，最大 50 |

**响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": "note-uuid-...",
        "notebookId": "a1b2c3d4-...",
        "notebookName": "高等数学",
        "title": "第一章 <em>极限</em>与连续",
        "plainText": "...<em>极限</em>的定义是...",
        "tags": [
          { "id": "tag-id-1", "name": "数学", "color": "#409EFF" }
        ],
        "createdAt": "2026-05-19T10:30:00",
        "updatedAt": "2026-05-19T18:00:00"
      }
    ],
    "page": 1,
    "size": 20,
    "totalElements": 15,
    "totalPages": 1
  }
}
```

> 命中关键词的部分使用 `<em>` 标签包裹，前端据此高亮显示。

---

## 5. 附件管理

### 5.1 上传附件

**POST** `/attachments/upload`

**请求**: `multipart/form-data`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 上传文件，最大 50MB |
| noteId | string | 是 | 关联笔记 ID |

**响应**:

```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "id": "att-uuid-...",
    "fileName": "limit-graph.png",
    "filePath": "/attachments/2026/05/limit-graph.png",
    "fileSize": 245760,
    "mimeType": "image/png",
    "markdownRef": "![limit-graph.png](/attachments/2026/05/limit-graph.png)"
  }
}
```

> `markdownRef` 可直接插入笔记 Markdown 内容中。

---

### 5.2 获取笔记附件列表

**GET** `/notes/{noteId}/attachments`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| noteId | string | 笔记 ID |

**响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "att-uuid-...",
      "fileName": "limit-graph.png",
      "filePath": "/attachments/2026/05/limit-graph.png",
      "fileSize": 245760,
      "mimeType": "image/png"
    }
  ]
}
```

---

### 5.3 删除附件

**DELETE** `/attachments/{id}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 附件 ID |

**响应**:

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

> 若该附件未被其他笔记引用，同步删除物理文件。

---

## 6. 数据导出

### 6.1 导出单篇笔记为 Markdown

**GET** `/export/notes/{id}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 笔记 ID |

**请求参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| targetDir | string | 是 | - | 导出目标目录（绝对路径） |

**响应**:

```json
{
  "code": 200,
  "message": "导出成功",
  "data": {
    "exportPath": "/Users/xxx/Desktop/第一章-极限与连续.md",
    "attachmentsCopied": 3
  }
}
```

---

### 6.2 导出整个笔记本

**GET** `/export/notebooks/{id}`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 笔记本 ID |

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetDir | string | 是 | 导出目标目录（绝对路径） |

**响应**:

```json
{
  "code": 200,
  "message": "导出成功",
  "data": {
    "exportDir": "/Users/xxx/Desktop/高等数学/",
    "notesExported": 15,
    "attachmentsCopied": 42
  }
}
```

> 导出时创建以笔记本名命名的子目录，每篇笔记一个 `.md` 文件，附件复制到子目录下的 `attachments/` 文件夹。

---

## 7. 草稿管理

### 7.1 保存草稿

**POST** `/drafts`

**请求体**:

```json
{
  "noteId": "note-uuid-...",
  "title": "第一章 极限与连续",
  "content": "# 极限\n\n正在编辑中...",
  "notebookId": "a1b2c3d4-..."
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| noteId | string | 否 | 已有笔记 ID（编辑已有笔记时传入） |
| title | string | 是 | 标题 |
| content | string | 否 | Markdown 内容 |
| notebookId | string | 否 | 所属笔记本 ID |

**响应**:

```json
{
  "code": 200,
  "message": "草稿已保存",
  "data": {
    "id": "draft-uuid-...",
    "savedAt": "2026-05-19T10:30:30"
  }
}
```

---

### 7.2 获取草稿

**GET** `/drafts/latest`

**响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "draft-uuid-...",
    "noteId": "note-uuid-...",
    "title": "第一章 极限与连续",
    "content": "# 极限\n\n正在编辑中...",
    "notebookId": "a1b2c3d4-...",
    "savedAt": "2026-05-19T10:30:30"
  }
}
```

> 仅保留最近一份草稿，新的草稿会覆盖旧的。

---

### 7.3 删除草稿

**DELETE** `/drafts`

**响应**:

```json
{
  "code": 200,
  "message": "草稿已清除",
  "data": null
}
```

---

## 8. 用户偏好

### 8.1 获取偏好

**GET** `/preferences`

**响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "theme": "dark",
    "autoSaveInterval": 30,
    "editorFontSize": 14,
    "attachmentRootPath": "/Users/xxx/classnote-files"
  }
}
```

---

### 8.2 更新偏好

**PUT** `/preferences`

**请求体**:

```json
{
  "theme": "light",
  "autoSaveInterval": 60,
  "editorFontSize": 16,
  "attachmentRootPath": "/Users/xxx/classnote-files"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| theme | string | 否 | 主题: light / dark |
| autoSaveInterval | int | 否 | 自动保存间隔（秒），最小 10 |
| editorFontSize | int | 否 | 编辑器字号 |
| attachmentRootPath | string | 否 | 附件存储根目录 |

**响应**: 返回更新后的偏好对象

---

## 9. 系统配置

### 9.1 获取应用配置

**GET** `/config`

**响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "version": "1.0.0",
    "dataDirectory": "~/.classnote",
    "attachmentRootPath": "~/classnote-files",
    "maxUploadSize": 52428800,
    "dbConnected": true
  }
}
```

---

### 9.2 检查 MySQL 连接状态

**GET** `/config/db-status`

**响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "connected": true,
    "url": "jdbc:mysql://localhost:3306/classnote",
    "version": "8.0.33"
  }
}
```

---

## 附录 A：数据库实体关系

```
┌──────────┐       ┌──────────────┐       ┌──────────┐
│ Notebook  │ 1───n │     Note     │ n───m │   Tag    │
│           │       │              │       │          │
│ id (PK)   │       │ id (PK)      │       │ id (PK)  │
│ name      │       │ notebookId   │       │ name     │
│ desc      │       │ title        │       │ color    │
│ createdAt │       │ content      │       │ createdAt│
│ updatedAt │       │ plainText    │       └──────────┘
└──────────┘       │ createdAt    │            │
                   │ updatedAt    │            │
                   └──────────────┘            │
                          │                    │
                          │ n───m              │
                          │                    │
                   ┌──────┴───────┐    ┌───────┴──────┐
                   │ note_tags    │    │     Tag      │
                   │ note_id (FK) │    │              │
                   │ tag_id (FK)  │    └──────────────┘
                   └──────────────┘
                          │
                          │ 1───n
                   ┌──────┴───────┐
                   │  Attachment  │
                   │ id (PK)      │
                   │ noteId (FK)  │
                   │ fileName     │
                   │ filePath     │
                   │ fileSize     │
                   │ mimeType     │
                   │ createdAt    │
                   └──────────────┘
```

---

## 附录 B：接口汇总

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/notebooks` | 创建笔记本 |
| GET | `/api/v1/notebooks` | 笔记本列表 |
| GET | `/api/v1/notebooks/{id}` | 笔记本详情 |
| PUT | `/api/v1/notebooks/{id}` | 编辑笔记本 |
| DELETE | `/api/v1/notebooks/{id}` | 删除笔记本 |
| POST | `/api/v1/notes` | 新建笔记 |
| GET | `/api/v1/notes` | 笔记列表 |
| GET | `/api/v1/notes/{id}` | 笔记详情 |
| PUT | `/api/v1/notes/{id}` | 编辑笔记 |
| DELETE | `/api/v1/notes/{id}` | 删除笔记 |
| DELETE | `/api/v1/notes/batch` | 批量删除笔记 |
| POST | `/api/v1/tags` | 创建标签 |
| GET | `/api/v1/tags` | 标签列表 |
| PUT | `/api/v1/tags/{id}` | 编辑标签 |
| DELETE | `/api/v1/tags/{id}` | 删除标签 |
| GET | `/api/v1/search` | 全文搜索 |
| POST | `/api/v1/attachments/upload` | 上传附件 |
| GET | `/api/v1/notes/{noteId}/attachments` | 笔记附件列表 |
| DELETE | `/api/v1/attachments/{id}` | 删除附件 |
| GET | `/api/v1/export/notes/{id}` | 导出笔记 |
| GET | `/api/v1/export/notebooks/{id}` | 导出笔记本 |
| POST | `/api/v1/drafts` | 保存草稿 |
| GET | `/api/v1/drafts/latest` | 获取草稿 |
| DELETE | `/api/v1/drafts` | 清除草稿 |
| GET | `/api/v1/preferences` | 获取偏好 |
| PUT | `/api/v1/preferences` | 更新偏好 |
| GET | `/api/v1/config` | 应用配置 |
| GET | `/api/v1/config/db-status` | 数据库连接状态 |

**共计 25 个接口**，覆盖笔记本 CRUD、笔记 CRUD + 批量删除、标签 CRUD、全文搜索、附件上传/管理、数据导出、草稿自动保存、用户偏好和应用配置。
