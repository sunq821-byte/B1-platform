-- V25: 四学院教师知识库（纯 Markdown，对齐 V21 课程与资料类型标签）
SET NAMES utf8mb4;
INSERT IGNORE INTO `file_storage`
(`id`, `bucket`, `object_key`, `original_name`, `content_type`, `file_size`, `access_url`) VALUES
(6010, 'knowledge', 'material/seed/knowledge/servlet-session-grading.md',      'Servlet 会话管理评分细则.md',   'text/markdown', 0, '/api/v1/files/6010/download'),
(6020, 'knowledge', 'material/seed/knowledge/bus301/market-research-grading.md', '市场调研报告评分细则.md',     'text/markdown', 0, '/api/v1/files/6020/download'),
(6021, 'knowledge', 'material/seed/knowledge/bus301/market-research-faq.md',     '市场调研 FAQ.md',             'text/markdown', 0, '/api/v1/files/6021/download'),
(6022, 'knowledge', 'material/seed/knowledge/bus301/market-research-template.md', '市场调研报告提交模板.md',   'text/markdown', 0, '/api/v1/files/6022/download'),
(6023, 'knowledge', 'material/seed/knowledge/bus301/market-research-sample.md',  '优秀样例-校园便利店调研.md',  'text/markdown', 0, '/api/v1/files/6023/download'),
(6030, 'knowledge', 'material/seed/knowledge/me301/mechanical-design-grading.md',  '减速器设计评分细则.md',   'text/markdown', 0, '/api/v1/files/6030/download'),
(6031, 'knowledge', 'material/seed/knowledge/me301/mechanical-design-faq.md',        '减速器设计 FAQ.md',       'text/markdown', 0, '/api/v1/files/6031/download'),
(6032, 'knowledge', 'material/seed/knowledge/me301/mechanical-design-standard.md', '机械制图与说明书规范.md', 'text/markdown', 0, '/api/v1/files/6032/download'),
(6033, 'knowledge', 'material/seed/knowledge/me301/mechanical-design-sample.md',   '减速器说明书结构参考.md', 'text/markdown', 0, '/api/v1/files/6033/download'),
(6040, 'knowledge', 'material/seed/knowledge/ent301/enterprise-delivery-grading.md',  '阶段交付评分细则.md', 'text/markdown', 0, '/api/v1/files/6040/download'),
(6041, 'knowledge', 'material/seed/knowledge/ent301/enterprise-delivery-faq.md',        '校企实训 FAQ.md',     'text/markdown', 0, '/api/v1/files/6041/download'),
(6042, 'knowledge', 'material/seed/knowledge/ent301/enterprise-delivery-standard.md', '企业项目交付规范.md', 'text/markdown', 0, '/api/v1/files/6042/download'),
(6043, 'knowledge', 'material/seed/knowledge/ent301/enterprise-delivery-sample.md',   '阶段交付总结参考.md', 'text/markdown', 0, '/api/v1/files/6043/download');

UPDATE `file_storage` SET
    `original_name` = 'Servlet 实训报告参考.md',
    `object_key` = 'material/seed/knowledge/servlet-session-report.md'
WHERE `id` = 6005;

UPDATE `file_storage` SET
    `original_name` = '实训设计文档规范模板.md'
WHERE `id` = 6004;

UPDATE `file_storage` SET
    `original_name` = '图书管理系统评分细则.md'
WHERE `id` = 6002;

DELETE FROM `knowledge_document` WHERE `id` BETWEEN 7001 AND 7099;

INSERT INTO `knowledge_document`
(`id`, `course_id`, `file_storage_id`, `title`, `tag`, `visibility_scope`, `visibility_class_ids`, `download_count`, `create_by`) VALUES
-- teacher1 张教授 · Java Web 开发 (course 1)
(7001, 1, 6001, 'Java Web 常见问题 FAQ',           'faq',       'COURSE', NULL, 156, 2),
(7002, 1, 6002, '图书管理系统评分细则',             'grading',   'COURSE', NULL, 234, 2),
(7003, 1, 6004, '实训设计文档规范模板',             'template',  'COURSE', NULL,  89, 2),
(7004, 1, 6005, 'Servlet 实训报告参考',             'reference', 'COURSE', NULL, 112, 2),
(7005, 1, 6010, 'Servlet 会话管理评分细则',         'grading',   'COURSE', NULL,  67, 2),
-- teacher2 陈副教授 · 管理学原理 (course 2)
(7020, 2, 6020, '市场调研报告评分细则',             'grading',   'COURSE', NULL,  45, 6),
(7021, 2, 6021, '市场调研 FAQ',                     'faq',       'COURSE', NULL,  78, 6),
(7022, 2, 6022, '市场调研报告提交模板',             'template',  'COURSE', NULL, 123, 6),
(7023, 2, 6023, '优秀样例：校园便利店调研',         'reference', 'COURSE', NULL,  56, 6),
-- teacher3 刘讲师 · 机械设计基础 (course 3)
(7030, 3, 6030, '减速器设计评分细则',               'grading',   'COURSE', NULL,  34, 7),
(7031, 3, 6031, '减速器设计 FAQ',                   'faq',       'COURSE', NULL,  41, 7),
(7032, 3, 6032, '机械制图与说明书规范',             'standard',  'COURSE', NULL,  29, 7),
(7033, 3, 6033, '减速器说明书结构参考',             'reference', 'COURSE', NULL,  38, 7),
-- teacher4 王导师 · 校企联合项目实训 (course 4)
(7040, 4, 6040, '阶段交付评分细则',                 'grading',   'COURSE', NULL,  22, 14),
(7041, 4, 6041, '校企实训 FAQ',                     'faq',       'COURSE', NULL,  31, 14),
(7042, 4, 6042, '企业项目交付规范',                 'standard',  'COURSE', NULL,  27, 14),
(7043, 4, 6043, '阶段交付总结参考',                 'reference', 'COURSE', NULL,  19, 14);
