-- V16: 知识库资料管理（按课程隔离）
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `knowledge_document` (
    `id`                 BIGINT       NOT NULL COMMENT '主键',
    `course_id`          BIGINT       NOT NULL COMMENT '所属课程ID',
    `file_storage_id`    BIGINT       NOT NULL COMMENT '关联文件存储ID',
    `title`              VARCHAR(256) NOT NULL COMMENT '资料名称',
    `tag`                VARCHAR(32)  NOT NULL DEFAULT 'doc' COMMENT '标签：java, frontend, db, doc',
    `visibility_scope`   VARCHAR(16)  NOT NULL DEFAULT 'COURSE' COMMENT '可见范围：COURSE=课程全体, CLASS=指定班级',
    `visibility_class_ids` VARCHAR(256) DEFAULT NULL COMMENT '班级ID列表（逗号分隔，scope=CLASS 时有效）',
    `download_count`     INT          NOT NULL DEFAULT 0 COMMENT '下载次数',
    `deleted`            TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`          BIGINT       DEFAULT NULL,
    `update_by`          BIGINT       DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_course_id` (`course_id`),
    INDEX `idx_tag` (`tag`),
    INDEX `idx_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师知识库资料';

-- 演示文件元数据（字节由 MaterialSeedRunner 同步至 MinIO knowledge 桶）
INSERT IGNORE INTO `file_storage`
(`id`, `bucket`, `object_key`, `original_name`, `content_type`, `file_size`, `access_url`) VALUES
(6001, 'knowledge', 'material/seed/java-web-faq.md',           'Java Web 常见问题.md',           'text/markdown',  1800, '/api/v1/files/6001/download'),
(6002, 'knowledge', 'material/seed/book-mgr-grading-rule.md',  'Spring Boot 开发规范手册.md',    'text/markdown',  2400, '/api/v1/files/6002/download'),
(6003, 'knowledge', 'material/seed/vue-components-report.md',  'Vue3 组件设计实战指南.md',       'text/markdown',   900, '/api/v1/files/6003/download'),
(6004, 'knowledge', 'material/seed/api-design-sample.md',      '软件设计文档规范模板.md',        'text/markdown',  1100, '/api/v1/files/6004/download'),
(6005, 'knowledge', 'material/seed/servlet-session-report.md', 'MyBatis-Plus 分页插件指南.md',   'text/markdown',   800, '/api/v1/files/6005/download');

-- teacher1 课程：1=Java Web, 2=前端框架, 10=软件工程实践
INSERT IGNORE INTO `knowledge_document`
(`id`, `course_id`, `file_storage_id`, `title`, `tag`, `visibility_scope`, `visibility_class_ids`, `download_count`, `create_by`) VALUES
(7001, 1, 6001, 'Java Web 常见问题 FAQ', 'java', 'COURSE', NULL, 156, 2),
(7002, 1, 6002, 'Spring Boot 开发规范手册', 'java', 'CLASS', '20', 234, 2),
(7003, 2, 6003, 'Vue3 组件设计实战指南', 'frontend', 'COURSE', NULL, 78, 2),
(7004, 2, 6004, '软件设计文档规范模板', 'doc', 'COURSE', NULL, 89, 2),
(7005, 10, 6005, 'MyBatis-Plus 分页插件使用指南', 'java', 'CLASS', '21', 312, 2);
