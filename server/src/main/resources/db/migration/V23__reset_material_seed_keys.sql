-- V23: 重置素材种子 object_key — 按 bucket 前缀去重，清理冲突记录
SET NAMES utf8mb4;

DELETE FROM `file_storage` WHERE `id` IN (5007, 5008);

UPDATE `file_storage` SET `object_key` = 'material/seed/submissions/servlet-session.zip'     WHERE `id` = 5001;
UPDATE `file_storage` SET `object_key` = 'material/seed/submissions/book-mgr-source.zip'   WHERE `id` = 5002;
UPDATE `file_storage` SET `object_key` = 'material/seed/submissions/vue-components.zip'      WHERE `id` = 5003;
UPDATE `file_storage` SET `object_key` = 'material/seed/submissions/book-mgr-report.md'    WHERE `id` = 5004;
UPDATE `file_storage` SET `object_key` = 'material/seed/submissions/servlet-session-report.md' WHERE `id` = 5005;
UPDATE `file_storage` SET `object_key` = 'material/seed/submissions/vue-components-report.md'  WHERE `id` = 5006;
UPDATE `file_storage` SET `object_key` = 'material/seed/submissions/LoginServlet.java'       WHERE `id` = 5009;
UPDATE `file_storage` SET `object_key` = 'material/seed/submissions/RedisCacheService.java'  WHERE `id` = 5010;
UPDATE `file_storage` SET `object_key` = 'material/seed/submissions/BaseButton.vue'          WHERE `id` = 5011;
UPDATE `file_storage` SET `object_key` = 'material/seed/submissions/LinkedListDemo.java'     WHERE `id` = 5012;
UPDATE `file_storage` SET `object_key` = 'material/seed/submissions/book-mgr-ui.png'        WHERE `id` = 5013;
UPDATE `file_storage` SET `object_key` = 'material/seed/submissions/redis-cache-screenshot.png' WHERE `id` = 5014;
UPDATE `file_storage` SET `object_key` = 'material/seed/submissions/vue-component-preview.jpg' WHERE `id` = 5015;
UPDATE `file_storage` SET `object_key` = 'material/seed/submissions/servlet-session-run.jpg' WHERE `id` = 5016;

UPDATE `file_storage` SET `object_key` = 'material/seed/knowledge/java-web-faq.md',           `original_name` = 'Java Web 常见问题.md'           WHERE `id` = 6001;
UPDATE `file_storage` SET `object_key` = 'material/seed/knowledge/book-mgr-grading-rule.md',  `original_name` = 'Spring Boot 开发规范手册.md'    WHERE `id` = 6002;
UPDATE `file_storage` SET `object_key` = 'material/seed/knowledge/vue-components-report.md',  `original_name` = 'Vue3 组件设计实战指南.md'       WHERE `id` = 6003;
UPDATE `file_storage` SET `object_key` = 'material/seed/knowledge/api-design-sample.md',      `original_name` = '软件设计文档规范模板.md'        WHERE `id` = 6004;
UPDATE `file_storage` SET `object_key` = 'material/seed/knowledge/servlet-session-report.md', `original_name` = 'MyBatis-Plus 分页插件指南.md'   WHERE `id` = 6005;

INSERT IGNORE INTO `file_storage`
(`id`, `bucket`, `object_key`, `original_name`, `content_type`, `file_size`, `access_url`) VALUES
(5020, 'submissions', 'material/seed/submissions/market-research-report.md',   '市场调研报告.md',     'text/markdown', 0, '/api/v1/files/5020/download'),
(5021, 'submissions', 'material/seed/submissions/market-research-report.pdf',   '市场调研报告.pdf',     'application/pdf', 0, '/api/v1/files/5021/download'),
(5022, 'submissions', 'material/seed/submissions/market-research-report.docx',  '市场调研报告.docx',    'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 0, '/api/v1/files/5022/download'),
(5023, 'submissions', 'material/seed/submissions/mechanical-design-report.md', '减速器设计说明书.md', 'text/markdown', 0, '/api/v1/files/5023/download'),
(5024, 'submissions', 'material/seed/submissions/mechanical-design.zip',        'mechanical-design.zip', 'application/zip', 0, '/api/v1/files/5024/download'),
(5025, 'submissions', 'material/seed/submissions/enterprise-summary.md',        '项目阶段总结.md',     'text/markdown', 0, '/api/v1/files/5025/download'),
(5026, 'submissions', 'material/seed/submissions/enterprise-delivery.zip',      'enterprise-delivery.zip', 'application/zip', 0, '/api/v1/files/5026/download'),
(5027, 'submissions', 'material/seed/submissions/enterprise-summary.pdf',       '项目阶段总结.pdf',     'application/pdf', 0, '/api/v1/files/5027/download');
