-- V17: 修复知识库演示文件元数据（V16 种子 file_storage 可能未写入）
SET NAMES utf8mb4;

INSERT IGNORE INTO `file_storage`
(`id`, `bucket`, `object_key`, `original_name`, `content_type`, `file_size`, `access_url`) VALUES
(6001, 'knowledge', 'material/seed/java-web-faq.md',           'Java Web 常见问题.md',           'text/markdown',  1800, '/api/v1/files/6001/download'),
(6002, 'knowledge', 'material/seed/book-mgr-grading-rule.md',  'Spring Boot 开发规范手册.md',    'text/markdown',  2400, '/api/v1/files/6002/download'),
(6003, 'knowledge', 'material/seed/vue-components-report.md',  'Vue3 组件设计实战指南.md',       'text/markdown',   900, '/api/v1/files/6003/download'),
(6004, 'knowledge', 'material/seed/api-design-sample.md',      '软件设计文档规范模板.md',        'text/markdown',  1100, '/api/v1/files/6004/download'),
(6005, 'knowledge', 'material/seed/servlet-session-report.md', 'MyBatis-Plus 分页插件指南.md',   'text/markdown',   800, '/api/v1/files/6005/download');
