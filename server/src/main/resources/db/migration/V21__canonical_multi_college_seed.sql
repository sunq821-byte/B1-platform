-- V21: 多学院 canonical 演示种子 — 清理旧 demo 并按四学院重建完整闭环
-- 密码均为 123456（BCrypt）；不含 Git 提交；支持 ZIP / 在线代码 / PDF / Word / MD / 图片
-- 学院：计算机 · 经管 · 机械 · 校企（企业端以 teacher/student 角色模拟）
SET NAMES utf8mb4;

-- ============================================================
-- 0. 清理全部演示业务数据
-- ============================================================
DELETE ri FROM `review_item` ri
INNER JOIN `teacher_review` tr ON ri.`review_id` = tr.`id`;

DELETE FROM `teacher_review`;
DELETE FROM `score_record`;
DELETE FROM `ai_analysis_detail`;
DELETE FROM `ai_analysis`;
DELETE FROM `submission_file`;
DELETE FROM `submission_trial`;
DELETE FROM `submission`;
DELETE FROM `notification` WHERE `id` >= 9000;
DELETE FROM `operation_log` WHERE `id` >= 9100;
DELETE FROM `course_student`;
DELETE FROM `course_teacher`;
DELETE FROM `course_class`;
DELETE FROM `training_task`;
DELETE FROM `course`;
DELETE FROM `class`;

DELETE FROM `user_role` WHERE `user_id` > 1;
DELETE FROM `user` WHERE `id` > 1;

DELETE FROM `standard_dimension` WHERE `standard_id` IN (1, 2, 3);
DELETE FROM `evaluation_standard` WHERE `id` IN (1, 2, 3);

-- ============================================================
-- 1. 用户（一账号一角色）
-- ============================================================
INSERT INTO `user` (`id`, `username`, `password`, `real_name`, `email`, `phone`, `status`) VALUES
(2,  'teacher1', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '张教授', 'zhang@b1.edu.cn',    '13800001111', 1),
(3,  'student1', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '李明',   'liming@b1.edu.cn',     '13800002222', 1),
(4,  'student2', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '王芳',   'wangfang@b1.edu.cn',   '13800003333', 1),
(6,  'teacher2', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '陈副教授', 'chen@b1.edu.cn',     '13800004444', 1),
(7,  'teacher3', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '刘讲师', 'liu@b1.edu.cn',      '13800005555', 1),
(8,  'student3', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '赵强',   'zhaoqiang@b1.edu.cn', '13800006666', 1),
(9,  'student4', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '孙丽',   'sunli@b1.edu.cn',     '13800007777', 1),
(10, 'student5', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '周杰',   'zhoujie@b1.edu.cn',   '13800008888', 1),
(11, 'student6', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '吴敏',   'wumin@b1.edu.cn',     '13800009999', 1),
(12, 'student7', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '郑浩',   'zhenghao@b1.edu.cn',  '13800010001', 1),
(13, 'student8', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '钱雪',   'qianxue@b1.edu.cn',   '13800010002', 1),
(14, 'teacher4', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '王导师', 'mentor@b1.com',       '13800010003', 1),
(15, 'student9', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '韩磊',   'hanlei@b1.edu.cn',    '13800010004', 1),
(16, 'student10', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '林涛', 'lintao@b1.edu.cn',    '13800010005', 1),
(17, 'student11', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '陈实训', 'chenlab@b1.com',     '13800010006', 1),
(18, 'student12', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '刘实训', 'liulab@b1.com',      '13800010007', 1);

INSERT INTO `user_role` (`id`, `user_id`, `role_id`) VALUES
(2, 2, 2), (3, 3, 3), (4, 4, 3), (6, 6, 2), (7, 7, 2),
(8, 8, 3), (9, 9, 3), (10, 10, 3), (11, 11, 3), (12, 12, 3), (13, 13, 3),
(14, 14, 2), (15, 15, 3), (16, 16, 3), (17, 17, 3), (18, 18, 3);

-- ============================================================
-- 2. 班级 · 课程 · 关联
-- ============================================================
INSERT INTO `class` (`id`, `class_code`, `class_name`, `grade`, `major`, `department`, `student_count`) VALUES
(1, 'SE2023-01', '2023级软件工程1班',       '2023', '软件工程',                 '计算机学院',   32),
(2, 'BA2023-01', '2023级工商管理1班',       '2023', '工商管理',                 '经济管理学院', 30),
(3, 'ME2023-01', '2023级机械设计1班',       '2023', '机械设计制造及其自动化',   '机械工程学院', 28),
(4, 'ENT2024-01', '2024校企联合实训班',     '2024', '软件工程（校企）',         '校企合作基地', 24);

INSERT INTO `course` (`id`, `course_code`, `course_name`, `semester`, `credits`, `description`, `status`) VALUES
(1, 'CS301',  'Java Web 开发',     '2025-2026-2', 4, '计算机学院：Java Web 全栈实训。',           'ACTIVE'),
(2, 'BUS301', '管理学原理',         '2025-2026-2', 3, '经管学院：市场调研与报告撰写实践。',         'ACTIVE'),
(3, 'ME301',  '机械设计基础',       '2025-2026-2', 4, '机械学院：减速器课程设计。',                 'ACTIVE'),
(4, 'ENT301', '校企联合项目实训',   '2025-2026-2', 4, '校企合作基地：企业项目阶段交付与导师复核。', 'ACTIVE');

INSERT INTO `course_teacher` (`id`, `course_id`, `user_id`) VALUES
(1, 1, 2), (2, 2, 6), (3, 3, 7), (4, 4, 14);

INSERT INTO `course_class` (`id`, `course_id`, `class_id`) VALUES
(1, 1, 1), (2, 2, 2), (3, 3, 3), (4, 4, 4);

INSERT INTO `course_student` (`id`, `course_id`, `user_id`) VALUES
(1, 1, 3), (2, 1, 4), (3, 1, 8), (4, 1, 9),
(5, 2, 10), (6, 2, 11), (7, 2, 12),
(8, 3, 13), (9, 3, 15), (10, 3, 16),
(11, 4, 17), (12, 4, 18);

-- ============================================================
-- 3. 实训任务（无 Git）
-- ============================================================
INSERT INTO `training_task`
(`id`, `course_id`, `standard_id`, `task_name`, `description`, `requirement`, `grading_rule`,
 `end_time`, `submission_type`, `max_ai_analyze_count`, `max_score`, `status`, `publish_time`) VALUES
(1, 1, 1000, 'Spring Boot 图书管理系统实训',
 '开发图书管理系统，支持 CRUD、借阅与认证。',
 '1. Spring Boot 3.x\n2. 提交源码 ZIP + 实训报告 MD\n3. 附运行截图 PNG/JPG',
 '完成度：CRUD+借阅；规范性：分层架构；质量：事务与异常；表达：报告+截图',
 '2026-09-30 23:59:59', 'FILE_CODE', 3, 100, 'PUBLISHED', '2026-06-01 10:00:00'),
(2, 1, 1000, 'Servlet 会话管理实训',
 '基于 Servlet 实现登录、登出与会话超时。',
 '1. 原生 Servlet API\n2. 提交 ZIP 或在线 Java 代码\n3. 附运行截图',
 '完成度：会话流程完整；规范性：Filter 使用；质量：异常处理；表达：README',
 '2026-08-15 23:59:59', 'FILE_CODE', 3, 80, 'PUBLISHED', '2026-05-20 10:00:00'),
(3, 2, 1000, '市场调研报告',
 '完成校园消费行为调研并撰写报告。',
 '1. 提交 PDF + Word + Markdown 报告\n2. 含问卷与数据分析\n3. 禁止 Git 提交',
 '完成度：样本量达标；规范性：引用格式；质量：论证逻辑；表达：图表齐全',
 '2026-09-20 23:59:59', 'ZIP_UPLOAD', 3, 100, 'PUBLISHED', '2026-06-10 10:00:00'),
(4, 3, 1000, '减速器设计说明书与图纸',
 '完成单级斜齿圆柱齿轮减速器设计。',
 '1. 提交设计说明书 MD\n2. 提交 CAD/ZIP 打包\n3. 附装配示意图 PNG',
 '完成度：参数齐全；规范性：国标标注；质量：强度校核；表达：图纸清晰',
 '2026-09-25 23:59:59', 'ZIP_UPLOAD', 3, 100, 'PUBLISHED', '2026-06-15 10:00:00'),
(5, 4, 1000, '企业项目阶段交付',
 '校企联合实训：库存预警模块阶段交付。',
 '1. 提交可运行 ZIP\n2. 提交 PDF 总结\n3. 附 Markdown 说明',
 '完成度：MVP 可运行；规范性：部署文档；质量：测试覆盖；表达：总结完整',
 '2026-10-10 23:59:59', 'ZIP_UPLOAD', 3, 100, 'PUBLISHED', '2026-07-01 10:00:00');

-- ============================================================
-- 4. 文件存储元数据（跨学院素材 5020+）
-- ============================================================
INSERT INTO `file_storage`
(`id`, `bucket`, `object_key`, `original_name`, `content_type`, `file_size`, `access_url`) VALUES
(5001, 'submissions', 'material/seed/servlet-session.zip',        'servlet-session.zip',     'application/zip', 748, '/api/v1/files/5001/download'),
(5002, 'submissions', 'material/seed/book-mgr-source.zip',        'book-mgr-source.zip',     'application/zip', 601, '/api/v1/files/5002/download'),
(5004, 'submissions', 'material/seed/book-mgr-report.md',         'book-mgr-实训报告.md',    'text/markdown', 1200, '/api/v1/files/5004/download'),
(5005, 'submissions', 'material/seed/servlet-session-report.md',  'servlet-session-实训报告.md', 'text/markdown', 800, '/api/v1/files/5005/download'),
(5009, 'submissions', 'material/seed/LoginServlet.java',          'LoginServlet.java',       'text/plain', 1300, '/api/v1/files/5009/download'),
(5011, 'submissions', 'material/seed/BaseButton.vue',             'BaseButton.vue',          'text/plain', 498, '/api/v1/files/5011/download'),
(5013, 'submissions', 'material/seed/book-mgr-ui.png',            'book-mgr-ui.png',         'image/png', 8606, '/api/v1/files/5013/download'),
(5016, 'submissions', 'material/seed/servlet-session-run.jpg',    'servlet-session-run.jpg', 'image/jpeg', 23453, '/api/v1/files/5016/download'),
(5020, 'submissions', 'material/seed/market-research-report.md',  '市场调研报告.md',         'text/markdown', 1800, '/api/v1/files/5020/download'),
(5021, 'submissions', 'material/seed/market-research-report.pdf', '市场调研报告.pdf',        'application/pdf', 120000, '/api/v1/files/5021/download'),
(5022, 'submissions', 'material/seed/market-research-report.docx','市场调研报告.docx',       'application/vnd.openxml', 45000, '/api/v1/files/5022/download'),
(5023, 'submissions', 'material/seed/mechanical-design-report.md','减速器设计说明书.md',     'text/markdown', 1500, '/api/v1/files/5023/download'),
(5024, 'submissions', 'material/seed/mechanical-design.zip',      'mechanical-design.zip',   'application/zip', 748, '/api/v1/files/5024/download'),
(5025, 'submissions', 'material/seed/enterprise-summary.md',      '项目阶段总结.md',         'text/markdown', 900, '/api/v1/files/5025/download'),
(5026, 'submissions', 'material/seed/enterprise-delivery.zip',    'enterprise-delivery.zip', 'application/zip', 601, '/api/v1/files/5026/download'),
(5027, 'submissions', 'material/seed/enterprise-summary.pdf',     '项目阶段总结.pdf',        'application/pdf', 120000, '/api/v1/files/5027/download')
ON DUPLICATE KEY UPDATE
    `original_name` = VALUES(`original_name`),
    `content_type` = VALUES(`content_type`),
    `file_size` = VALUES(`file_size`);

-- ============================================================
-- 5. 提交主记录 — 四学院完整闭环
-- ============================================================
INSERT INTO `submission`
(`id`, `training_task_id`, `user_id`, `submit_type`, `git_url`, `summary`, `submit_count`,
 `ai_analyze_used_count`, `current_trial_id`, `confirmed_trial_id`, `confirmed_at`, `submit_time`, `status`) VALUES
-- 计算机学院
(101, 1, 3,  'ZIP_UPLOAD',   NULL, '李明-图书管理终稿（ZIP+报告+截图）', 2, 2, 9100000000000000101, 9100000000000000101, '2026-07-06 14:35:00', '2026-07-06 14:35:00', 'PUBLISHED'),
(102, 1, 4,  'ZIP_UPLOAD',   NULL, '王芳-图书管理待审（ZIP+报告+截图）', 1, 1, 9100000000000000102, 9100000000000000102, '2026-07-10 11:00:00', '2026-07-10 11:00:00', 'CONFIRMED'),
(103, 1, 8,  'ZIP_UPLOAD',   NULL, '赵强-首版缺借阅模块（已驳回）',       1, 0, NULL,                NULL,                NULL,                  '2026-07-08 09:00:00', 'REJECTED'),
(104, 2, 9,  'ONLINE_CODE',  NULL, '孙丽-Servlet 在线代码待确认',         1, 1, 9100000000000000104, NULL,                NULL,                  '2026-06-19 14:00:00', 'AI_READY'),
(105, 2, 3,  'ZIP_UPLOAD',   NULL, '李明-Servlet 已发布',                 1, 1, 9100000000000000105, 9100000000000000105, '2026-06-18 10:05:00', '2026-06-18 10:05:00', 'PUBLISHED'),
-- 经管学院
(201, 3, 10, 'ZIP_UPLOAD',   NULL, '周杰-市场调研报告已发布',             1, 1, 9100000000000000201, 9100000000000000201, '2026-07-05 16:00:00', '2026-07-05 16:00:00', 'PUBLISHED'),
(202, 3, 11, 'ZIP_UPLOAD',   NULL, '吴敏-市场调研报告待审（PDF+Word+MD）', 1, 1, 9100000000000000202, 9100000000000000202, '2026-07-10 15:30:00', '2026-07-10 15:30:00', 'CONFIRMED'),
(203, 3, 12, 'ZIP_UPLOAD',   NULL, '郑浩-调研报告草稿待确认',             1, 1, 9100000000000000203, NULL,                NULL,                  '2026-07-08 20:00:00', 'AI_READY'),
-- 机械学院
(301, 4, 13, 'ZIP_UPLOAD',   NULL, '钱雪-减速器设计已发布',               1, 1, 9100000000000000301, 9100000000000000301, '2026-07-12 10:00:00', '2026-07-12 10:00:00', 'PUBLISHED'),
(302, 4, 15, 'ZIP_UPLOAD',   NULL, '韩磊-减速器设计待审（ZIP+MD+PNG）',   1, 1, 9100000000000000302, 9100000000000000302, '2026-07-14 11:20:00', '2026-07-14 11:20:00', 'CONFIRMED'),
-- 校企基地
(401, 5, 17, 'ZIP_UPLOAD',   NULL, '陈实训-企业项目阶段已发布',           1, 1, 9100000000000000401, 9100000000000000401, '2026-07-16 09:00:00', '2026-07-16 09:00:00', 'PUBLISHED'),
(402, 5, 18, 'ZIP_UPLOAD',   NULL, '刘实训-企业项目阶段待审（ZIP+PDF+MD）', 1, 1, 9100000000000000402, 9100000000000000402, '2026-07-18 14:00:00', '2026-07-18 14:00:00', 'CONFIRMED');

-- ============================================================
-- 6. 试交记录
-- ============================================================
INSERT INTO `submission_trial`
(`id`, `submission_id`, `trial_no`, `submit_type`, `git_url`, `summary`, `submit_time`, `status`, `ai_reference_score`) VALUES
(9100000000000000100, 101, 1, 'ZIP_UPLOAD',  NULL, '李明-图书管理初稿',     '2026-07-05 10:00:00', 'SUPERSEDED', 78.00),
(9100000000000000101, 101, 2, 'ZIP_UPLOAD',  NULL, '李明-图书管理终稿',     '2026-07-06 14:30:00', 'CONFIRMED',  85.00),
(9100000000000000102, 102, 1, 'ZIP_UPLOAD',  NULL, '王芳-图书管理终稿',     '2026-07-10 11:00:00', 'CONFIRMED',  86.00),
(9100000000000000103, 103, 1, 'ZIP_UPLOAD',  NULL, '赵强-首版',             '2026-07-07 15:00:00', 'CONFIRMED',  62.00),
(9100000000000000104, 104, 1, 'ONLINE_CODE', NULL, '孙丽-LoginServlet 在线代码', '2026-06-19 14:00:00', 'AI_READY', 84.00),
(9100000000000000105, 105, 1, 'ZIP_UPLOAD',  NULL, '李明-Servlet 提交',     '2026-06-18 10:00:00', 'CONFIRMED',  80.00),
(9100000000000000201, 201, 1, 'ZIP_UPLOAD',  NULL, '周杰-市场调研终稿',     '2026-07-05 16:00:00', 'CONFIRMED',  88.00),
(9100000000000000202, 202, 1, 'ZIP_UPLOAD',  NULL, '吴敏-市场调研终稿',     '2026-07-10 15:30:00', 'CONFIRMED',  90.00),
(9100000000000000203, 203, 1, 'ZIP_UPLOAD',  NULL, '郑浩-调研草稿',         '2026-07-08 20:00:00', 'AI_READY',   72.00),
(9100000000000000301, 301, 1, 'ZIP_UPLOAD',  NULL, '钱雪-减速器终稿',       '2026-07-12 10:00:00', 'CONFIRMED',  91.00),
(9100000000000000302, 302, 1, 'ZIP_UPLOAD',  NULL, '韩磊-减速器待审稿',     '2026-07-14 11:20:00', 'CONFIRMED',  87.00),
(9100000000000000401, 401, 1, 'ZIP_UPLOAD',  NULL, '陈实训-阶段交付',       '2026-07-16 09:00:00', 'CONFIRMED',  89.00),
(9100000000000000402, 402, 1, 'ZIP_UPLOAD',  NULL, '刘实训-阶段交付待审',   '2026-07-18 14:00:00', 'CONFIRMED',  92.00);

-- ============================================================
-- 7. 附件（多格式）
-- ============================================================
INSERT INTO `submission_file`
(`id`, `submission_id`, `file_id`, `file_name`, `file_size`, `file_type`, `sort_order`) VALUES
(1,  101, 5002, 'book-mgr-source.zip',      601,   'application/zip',  1),
(2,  101, 5004, 'book-mgr-实训报告.md',     1200,  'text/markdown',    2),
(3,  101, 5013, 'book-mgr-ui.png',          8606,  'image/png',        3),
(4,  102, 5002, 'book-mgr-source.zip',      601,   'application/zip',  1),
(5,  102, 5004, 'book-mgr-实训报告.md',     1200,  'text/markdown',    2),
(6,  102, 5013, 'book-mgr-ui.png',          8606,  'image/png',        3),
(7,  103, 5002, 'book-mgr-source.zip',      601,   'application/zip',  1),
(8,  104, 5009, 'LoginServlet.java',        1300,  'text/plain',       1),
(9,  104, 5016, 'servlet-session-run.jpg',  23453, 'image/jpeg',       2),
(10, 105, 5001, 'servlet-session.zip',      748,   'application/zip',  1),
(11, 105, 5005, 'servlet-session-实训报告.md', 800, 'text/markdown', 2),
(12, 105, 5016, 'servlet-session-run.jpg',  23453, 'image/jpeg',       3),
(20, 201, 5021, '市场调研报告.pdf',         120000,'application/pdf',  1),
(21, 201, 5022, '市场调研报告.docx',        45000, 'application/vnd.openxml', 2),
(22, 201, 5020, '市场调研报告.md',          1800,  'text/markdown',    3),
(23, 202, 5021, '市场调研报告.pdf',         120000,'application/pdf',  1),
(24, 202, 5022, '市场调研报告.docx',        45000, 'application/vnd.openxml', 2),
(25, 202, 5020, '市场调研报告.md',          1800,  'text/markdown',    3),
(26, 203, 5020, '市场调研报告.md',          1800,  'text/markdown',    1),
(30, 301, 5024, 'mechanical-design.zip',    748,   'application/zip',  1),
(31, 301, 5023, '减速器设计说明书.md',      1500,  'text/markdown',    2),
(32, 301, 5013, '装配示意图.png',           8606,  'image/png',        3),
(33, 302, 5024, 'mechanical-design.zip',    748,   'application/zip',  1),
(34, 302, 5023, '减速器设计说明书.md',      1500,  'text/markdown',    2),
(35, 302, 5013, '装配示意图.png',           8606,  'image/png',        3),
(40, 401, 5026, 'enterprise-delivery.zip',  601,   'application/zip',  1),
(41, 401, 5027, '项目阶段总结.pdf',         120000,'application/pdf',  2),
(42, 401, 5025, '项目阶段总结.md',          900,   'text/markdown',    3),
(43, 402, 5026, 'enterprise-delivery.zip',  601,   'application/zip',  1),
(44, 402, 5027, '项目阶段总结.pdf',         120000,'application/pdf',  2),
(45, 402, 5025, '项目阶段总结.md',          900,   'text/markdown',    3);

-- ============================================================
-- 8. AI 分析
-- ============================================================
INSERT INTO `ai_analysis`
(`id`, `submission_id`, `trial_id`, `total_score`, `analysis_status`, `model_provider`, `model_name`, `complete_time`) VALUES
(6001, 101, 9100000000000000101, 85.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-06 14:32:00'),
(6002, 102, 9100000000000000102, 86.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-10 11:05:00'),
(6003, 103, 9100000000000000103, 62.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-07 15:10:00'),
(6004, 104, 9100000000000000104, 84.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-06-19 14:05:00'),
(6005, 105, 9100000000000000105, 80.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-06-18 10:02:00'),
(6101, 201, 9100000000000000201, 88.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-05 16:05:00'),
(6102, 202, 9100000000000000202, 90.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-10 15:35:00'),
(6103, 203, 9100000000000000203, 72.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-08 20:05:00'),
(6201, 301, 9100000000000000301, 91.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-12 10:05:00'),
(6202, 302, 9100000000000000302, 87.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-14 11:25:00'),
(6301, 401, 9100000000000000401, 89.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-16 09:05:00'),
(6302, 402, 9100000000000000402, 92.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-18 14:05:00');

INSERT INTO `ai_analysis_detail`
(`id`, `ai_analysis_id`, `dimension_id`, `agent_type`, `issue_type`, `severity`, `reason`, `suggestion`, `suggest_deduct`, `confidence`, `sort_order`) VALUES
(7001, 6001, 1001, 'CODE', '完成度', 'MINOR', '借阅模块完整', '补充单元测试', 1.00, 0.90, 1),
(7002, 6002, 1001, 'CODE', '完成度', 'MINOR', '功能结构清晰', '补充集成测试', 1.00, 0.88, 1),
(7003, 6003, 1001, 'REQ',  '缺失',   'CRITICAL', '缺少借阅模块', '补全 BorrowService', 8.00, 0.95, 1),
(7004, 6102, 1004, 'DOC',  '表达',   'MINOR', '报告结构完整', '增加对比图表', 1.00, 0.92, 1),
(7005, 6202, 1003, 'DESIGN', '质量', 'MINOR', '强度校核合理', '补充润滑说明', 1.00, 0.89, 1),
(7006, 6302, 1001, 'REQ',  '完成度', 'MINOR', 'MVP 可运行', '提高测试覆盖率', 1.00, 0.91, 1);

-- ============================================================
-- 9. 教师复核与成绩
-- ============================================================
INSERT INTO `teacher_review`
(`id`, `submission_id`, `reviewer_id`, `teacher_comment`, `status`, `review_time`, `publish_time`) VALUES
(8001, 101, 2,  '功能完整，分层清晰。', 'PUBLISHED', '2026-07-08 10:00:00', '2026-07-08 15:00:00'),
(8002, 105, 2,  '会话管理实现正确。',   'PUBLISHED', '2026-06-20 09:00:00', '2026-06-20 11:00:00'),
(8005, 103, 2,  '借阅模块缺失，请修改后重新试交。', 'RETURNED', '2026-07-09 10:00:00', NULL),
(8101, 201, 6,  '调研方法规范，结论清晰。', 'PUBLISHED', '2026-07-06 09:00:00', '2026-07-06 10:00:00'),
(8201, 301, 7,  '设计参数合理，图纸齐全。', 'PUBLISHED', '2026-07-13 09:00:00', '2026-07-13 10:00:00'),
(8301, 401, 14, '阶段交付达标，可进入下一阶段。', 'PUBLISHED', '2026-07-17 09:00:00', '2026-07-17 10:00:00');

INSERT INTO `score_record`
(`id`, `submission_id`, `user_id`, `training_task_id`, `total_score`, `ai_total_score`,
 `score_details`, `teacher_comment`, `status`, `publish_time`) VALUES
(10001, 101, 3,  1, 88.00, 85.00,
 '[{"dimensionId":1001,"score":26,"remark":""},{"dimensionId":1002,"score":22,"remark":""},{"dimensionId":1003,"score":21,"remark":""},{"dimensionId":1004,"score":19,"remark":""}]',
 '功能完整，分层清晰。', 'PUBLISHED', '2026-07-08 15:00:00'),
(10002, 105, 3,  2, 82.00, 80.00,
 '[{"dimensionId":1001,"score":25,"remark":""},{"dimensionId":1002,"score":20,"remark":""},{"dimensionId":1003,"score":20,"remark":""},{"dimensionId":1004,"score":17,"remark":""}]',
 '会话管理实现正确。', 'PUBLISHED', '2026-06-20 11:00:00'),
(10101, 201, 10, 3, 90.00, 88.00,
 '[{"dimensionId":1001,"score":27,"remark":""},{"dimensionId":1002,"score":22,"remark":""},{"dimensionId":1003,"score":21,"remark":""},{"dimensionId":1004,"score":20,"remark":""}]',
 '调研方法规范，结论清晰。', 'PUBLISHED', '2026-07-06 10:00:00'),
(10201, 301, 13, 4, 93.00, 91.00,
 '[{"dimensionId":1001,"score":28,"remark":""},{"dimensionId":1002,"score":23,"remark":""},{"dimensionId":1003,"score":22,"remark":""},{"dimensionId":1004,"score":20,"remark":""}]',
 '设计参数合理，图纸齐全。', 'PUBLISHED', '2026-07-13 10:00:00'),
(10301, 401, 17, 5, 91.00, 89.00,
 '[{"dimensionId":1001,"score":27,"remark":""},{"dimensionId":1002,"score":23,"remark":""},{"dimensionId":1003,"score":21,"remark":""},{"dimensionId":1004,"score":20,"remark":""}]', '阶段交付达标，可进入下一阶段。', 'PUBLISHED', '2026-07-17 10:00:00');

-- ============================================================
-- 10. 通知 · 操作日志
-- ============================================================
INSERT INTO `notification`
(`id`, `user_id`, `title`, `content`, `notify_type`, `is_read`, `create_time`) VALUES
(9001, 2,  '待审核提醒', '王芳已确认提交《图书管理系统》，请进入审核工作台。', 'REMIND', 0, '2026-08-27 09:00:00'),
(9002, 6,  '待审核提醒', '吴敏已提交《市场调研报告》（PDF+Word），请复核。', 'REMIND', 0, '2026-08-27 09:05:00'),
(9003, 7,  '待审核提醒', '韩磊已提交《减速器设计说明书与图纸》，请复核。', 'REMIND', 0, '2026-08-27 09:10:00'),
(9004, 14, '待审核提醒', '刘实训已提交企业项目阶段交付，请导师复核。', 'REMIND', 0, '2026-08-27 09:15:00'),
(9005, 8,  '提交被驳回', '《图书管理系统》已被驳回，AI 次数已重置。', 'SCORE', 0, '2026-07-09 10:35:00'),
(9006, 9,  '待确认提交', '《Servlet 会话管理》在线代码试交完成，请确认是否递交教师。', 'REMIND', 0, '2026-06-19 14:10:00'),
(9007, 12, '待确认提交', '《市场调研报告》试交完成，请确认是否递交教师。', 'REMIND', 0, '2026-07-08 20:10:00');

INSERT INTO `operation_log`
(`id`, `user_id`, `username`, `module`, `operation`, `target_type`, `target_id`, `ip_address`, `result`, `create_time`) VALUES
(9101, 1,  'admin',    'AUTH', 'LOGIN', 'User', 1, '127.0.0.1', 'SUCCESS', '2026-08-27 08:00:00'),
(9102, 6,  'teacher2', 'REVIEW', 'PUBLISH', 'Submission', 201, '192.168.1.20', 'SUCCESS', '2026-07-06 10:00:00'),
(9103, 11, 'student6', 'SUBMISSION', 'CONFIRM', 'Submission', 202, '192.168.1.106', 'SUCCESS', '2026-07-10 15:30:00'),
(9104, 14, 'teacher4', 'REVIEW', 'PUBLISH', 'Submission', 401, '192.168.1.50', 'SUCCESS', '2026-07-17 10:00:00'),
(9105, 2,  'teacher1', 'REVIEW', 'RETURN', 'Submission', 103, '192.168.1.10', 'SUCCESS', '2026-07-09 10:30:00');
