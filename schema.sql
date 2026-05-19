-- ==========================================
-- ClassNote Local - 建库建表脚本
-- 兼容 MySQL 8.0+
-- ==========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS classnote
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE classnote;

-- ==========================================
-- 笔记本表
-- ==========================================
CREATE TABLE IF NOT EXISTS notebooks (
    id          VARCHAR(36)  NOT NULL PRIMARY KEY COMMENT 'UUID 主键',
    name        VARCHAR(100) NOT NULL COMMENT '笔记本名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    INDEX idx_updated_at (updated_at),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记本';

-- ==========================================
-- 标签表
-- ==========================================
CREATE TABLE IF NOT EXISTS tags (
    id         VARCHAR(36)  NOT NULL PRIMARY KEY COMMENT 'UUID 主键',
    name       VARCHAR(30)  NOT NULL COMMENT '标签名称（全局唯一）',
    color      VARCHAR(7)   DEFAULT '#409EFF' COMMENT '颜色，十六进制格式',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签';

-- ==========================================
-- 笔记表
-- ==========================================
CREATE TABLE IF NOT EXISTS notes (
    id          VARCHAR(36)  NOT NULL PRIMARY KEY COMMENT 'UUID 主键',
    notebook_id VARCHAR(36)  NOT NULL COMMENT '所属笔记本 ID',
    title       VARCHAR(200) NOT NULL COMMENT '笔记标题',
    content     MEDIUMTEXT   DEFAULT NULL COMMENT 'Markdown 内容',
    plain_text  MEDIUMTEXT   DEFAULT NULL COMMENT '纯文本（用于搜索）',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    INDEX idx_notebook_id (notebook_id),
    INDEX idx_updated_at (updated_at),
    INDEX idx_created_at (created_at),
    INDEX idx_title (title),
    FULLTEXT INDEX ft_title_content (title, plain_text),
    CONSTRAINT fk_note_notebook FOREIGN KEY (notebook_id) REFERENCES notebooks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记';

-- ==========================================
-- 笔记-标签关联表（多对多）
-- ==========================================
CREATE TABLE IF NOT EXISTS note_tags (
    note_id VARCHAR(36) NOT NULL COMMENT '笔记 ID',
    tag_id  VARCHAR(36) NOT NULL COMMENT '标签 ID',
    PRIMARY KEY (note_id, tag_id),
    INDEX idx_tag_id (tag_id),
    CONSTRAINT fk_note_tags_note FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
    CONSTRAINT fk_note_tags_tag  FOREIGN KEY (tag_id)  REFERENCES tags(id)  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记-标签关联';

-- ==========================================
-- 附件表
-- ==========================================
CREATE TABLE IF NOT EXISTS attachments (
    id         VARCHAR(36)  NOT NULL PRIMARY KEY COMMENT 'UUID 主键',
    note_id    VARCHAR(36)  NOT NULL COMMENT '关联笔记 ID',
    file_name  VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path  VARCHAR(500) NOT NULL COMMENT '存储路径',
    file_size  BIGINT       NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    mime_type  VARCHAR(100) DEFAULT NULL COMMENT 'MIME 类型',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_note_id (note_id),
    CONSTRAINT fk_attachment_note FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='附件';

-- ==========================================
-- 草稿表
-- ==========================================
CREATE TABLE IF NOT EXISTS drafts (
    id          VARCHAR(36)  NOT NULL PRIMARY KEY COMMENT 'UUID 主键',
    note_id     VARCHAR(36)  DEFAULT NULL COMMENT '关联笔记 ID（编辑已有笔记时）',
    title       VARCHAR(200) NOT NULL COMMENT '笔记标题',
    content     MEDIUMTEXT   DEFAULT NULL COMMENT 'Markdown 内容',
    notebook_id VARCHAR(36)  DEFAULT NULL COMMENT '所属笔记本 ID',
    saved_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '保存时间',
    INDEX idx_saved_at (saved_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='草稿';

-- ==========================================
-- 用户偏好表
-- ==========================================
CREATE TABLE IF NOT EXISTS user_preferences (
    id                   VARCHAR(36)  NOT NULL PRIMARY KEY DEFAULT 'default' COMMENT '固定主键',
    theme                VARCHAR(10)  DEFAULT 'light' COMMENT '主题: light / dark',
    auto_save_interval   INT          NOT NULL DEFAULT 30 COMMENT '自动保存间隔（秒）',
    editor_font_size     INT          NOT NULL DEFAULT 14 COMMENT '编辑器字号',
    attachment_root_path VARCHAR(500) DEFAULT NULL COMMENT '附件存储根目录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户偏好';

-- ==========================================
-- 初始化默认偏好
-- ==========================================
INSERT INTO user_preferences (id, theme, auto_save_interval, editor_font_size)
VALUES ('default', 'light', 30, 14)
ON DUPLICATE KEY UPDATE id = id;
