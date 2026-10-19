-- V11: 丰富演示数据 — 全角色、全流程状态、可交互 Mock（方案 A）
-- 密码：123456（BCrypt）；admin 仍为 admin123
SET NAMES utf8mb4;

-- ============================================================
-- 1. 扩展用户
-- ============================================================
INSERT IGNORE INTO `user` (`id`, `username`, `password`, `real_name`, `email`, `phone`, `status`) VALUES
(6,  'teacher2', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '陈副教授', 'chen@b1.edu.cn',   '13800004444', 1),
(7,  'teacher3', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '刘讲师',   'liu@b1.edu.cn',    '13800005555', 1),
(8,  'student3', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '赵强',     'zhaoqiang@b1.edu.cn', '13800006666', 1),
(9,  'student4', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '孙丽',     'sunli@b1.edu.cn',     '13800007777', 1),
(10, 'student5', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '周杰',     'zhoujie@b1.edu.cn',   '13800008888', 1),
(11, 'student6', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '吴敏',     'wumin@b1.edu.cn',     '13800009999', 1),
(12, 'student7', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '郑浩',     'zhenghao@b1.edu.cn',  '13800010001', 1),
(13, 'student8', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '钱雪',     'qianxue@b1.edu.cn',   '13800010002', 1);

INSERT IGNORE INTO `user_role` (`id`, `user_id`, `role_id`) VALUES
(10, 6, 2), (11, 7, 2),
(12, 8, 3), (13, 9, 3), (14, 10, 3), (15, 11, 3), (16, 12, 3), (17, 13, 3);

-- ============================================================
-- 2. 班级与课程
-- ============================================================
INSERT IGNORE INTO `class` (`id`, `class_code`, `class_name`, `grade`, `major`, `department`, `student_count`) VALUES
(20, 'SE2023-01', '2023级软件工程1班', '2023', '软件工程', '计算机学院', 32),
(21, 'SE2023-02', '2023级软件工程2班', '2023', '软件工程', '计算机学院', 30);

INSERT IGNORE INTO `course` (`id`, `course_code`, `course_name`, `semester`, `credits`, `description`, `status`) VALUES
(10, 'CS303', '软件工程实践', '2025-2026-2', 3, '团队协作、里程碑交付与工程规范训练。', 'ACTIVE'),
(11, 'CS304', '数据结构实验', '2025-2026-2', 2, '线性表、树、图等结构的实现与复杂度分析。', 'ACTIVE');

INSERT IGNORE INTO `course_teacher` (`id`, `course_id`, `user_id`) VALUES
(10, 10, 6), (11, 11, 7), (12, 10, 2);

INSERT IGNORE INTO `course_student` (`id`, `course_id`, `user_id`) VALUES
(10, 1, 8), (11, 1, 9), (12, 1, 10), (13, 1, 11), (14, 1, 12), (15, 1, 13),
(16, 2, 8), (17, 2, 9), (18, 10, 8), (19, 10, 9), (20, 10, 10),
(21, 11, 11), (22, 11, 12);

INSERT IGNORE INTO `course_class` (`id`, `course_id`, `class_id`) VALUES
(1, 1, 20), (2, 2, 20), (3, 10, 21), (4, 11, 21);

-- ============================================================
-- 3. 实训任务（含新任务 + 更新细则）
-- ============================================================
UPDATE `training_task` SET
    `standard_id` = 1000,
    `grading_rule` = '完成度：CRUD+借阅+认证；规范性：分层+Git；质量：事务与异常；表达：报告+API文档',
    `max_ai_analyze_count` = 3
WHERE `id` IN (1, 2, 3);

INSERT IGNORE INTO `training_task`
(`id`, `course_id`, `standard_id`, `task_name`, `description`, `requirement`, `grading_rule`,
 `end_time`, `submission_type`, `max_ai_analyze_count`, `max_score`, `status`, `publish_time`) VALUES
(10, 1, 1000, 'Redis 缓存集成实训',
 '为图书管理系统增加 Redis 缓存层，缓存热点图书与会话数据。',
 '1. 集成 Redis\n2. 缓存穿透/击穿方案说明\n3. 提交源码 ZIP',
 '完成度：缓存读写正确；规范性：配置外置；质量：过期策略合理；表达：压测截图',
 '2026-06-01 23:59:59', 'ZIP_ONLY', 3, 80, 'PUBLISHED', '2026-05-01 10:00:00'),
(11, 1, 1000, 'API 文档与 OpenAPI 补全',
 '补全 Swagger/OpenAPI 文档并导出 PDF。',
 '1. 全部接口有文档\n2. 导出 openapi.json\n3. 试交后可确认提交',
 '完成度：接口覆盖率>90%；规范性：统一响应体；质量：示例准确；表达：文档可读',
 '2026-09-30 23:59:59', 'GIT_ZIP', 3, 100, 'PUBLISHED', '2026-08-01 10:00:00'),
(12, 10, 1000, '团队项目里程碑 M1',
 '软件工程实践第一阶段：需求分析与原型设计。',
 '1. 需求规格说明书\n2. 原型图\n3. Git 协作记录',
 '完成度：里程碑交付物齐全；规范性：模板符合课程要求；质量：需求可测；表达：答辩 PPT',
 '2026-10-15 23:59:59', 'ZIP_ONLY', 3, 100, 'PUBLISHED', '2026-08-15 10:00:00'),
(13, 11, 1000, '链表与栈的实现',
 '实现单链表、双链表与栈，完成复杂度分析实验报告。',
 '1. Java 实现\n2. 单元测试\n3. 实验报告',
 '完成度：三种结构实现；规范性：代码风格；质量：边界测试；表达：复杂度推导',
 '2026-09-20 23:59:59', 'ZIP_ONLY', 3, 80, 'PUBLISHED', '2026-08-10 10:00:00');

-- ============================================================
-- 4. 文件存储元数据（字节由 MaterialSeedRunner 上传 MinIO）
-- ============================================================
INSERT IGNORE INTO `file_storage`
(`id`, `bucket`, `object_key`, `original_name`, `content_type`, `file_size`, `access_url`) VALUES
(5001, 'submissions', 'material/seed/servlet-session.zip',     'servlet-session.zip',     'application/zip',      748, '/api/v1/files/5001/download'),
(5002, 'submissions', 'material/seed/book-mgr-source.zip',   'book-mgr-source.zip',     'application/zip',      601, '/api/v1/files/5002/download'),
(5003, 'submissions', 'material/seed/vue-components.zip',    'vue-components.zip',      'application/zip',      433, '/api/v1/files/5003/download'),
(5004, 'submissions', 'material/seed/book-mgr-report.md',    'book-mgr-实训报告.md',    'text/markdown',       1200, '/api/v1/files/5004/download'),
(5005, 'submissions', 'material/seed/servlet-session-report.md', 'servlet-session-实训报告.md', 'text/markdown', 800, '/api/v1/files/5005/download'),
(5006, 'submissions', 'material/seed/vue-components-report.md',  'vue-components-实训报告.md',  'text/markdown', 900, '/api/v1/files/5006/download');

-- 关联已有 submission_file
UPDATE `submission_file` SET `file_id` = 5002 WHERE `id` = 1;
UPDATE `submission_file` SET `file_id` = 5004 WHERE `id` = 2;
UPDATE `submission_file` SET `file_id` = 5001 WHERE `id` = 3;
UPDATE `submission_file` SET `file_id` = 5003 WHERE `id` = 4;

-- ============================================================
-- 5. 规范化既有提交 → 方案 A + 试交记录
-- ============================================================

-- 5.1 已发布（student1 任务 1/2/3）
INSERT IGNORE INTO `submission_trial`
(`id`, `submission_id`, `trial_no`, `submit_type`, `git_url`, `summary`, `submit_time`, `status`, `ai_reference_score`) VALUES
(8900000000000000002, 2, 1, 'GIT_URL', 'https://gitee.com/liming/book-mgr', '初版：基础 CRUD', '2026-07-05 10:00:00', 'SUPERSEDED', 78.00),
(8900000000000000003, 2, 2, 'GIT_URL', 'https://gitee.com/liming/book-mgr', '实现了图书管理系统的核心功能，包括图书CRUD、借阅管理和用户认证。', '2026-07-06 14:30:00', 'CONFIRMED', 85.00),
(8900000000000000004, 3, 1, 'ZIP_ONLY', NULL, '完成了Servlet会话管理的所有要求。', '2026-06-18 10:00:00', 'CONFIRMED', 80.00),
(8900000000000000005, 5, 1, 'GIT_URL', 'https://gitee.com/liming/vue-components', '完成 Vue3 组件库基础组件实现。', '2026-07-05 16:00:00', 'CONFIRMED', 88.00);

UPDATE `submission` SET
    `status` = 'PUBLISHED',
    `ai_analyze_used_count` = 2,
    `current_trial_id` = 8900000000000000003,
    `confirmed_trial_id` = 8900000000000000003,
    `confirmed_at` = '2026-07-06 14:35:00'
WHERE `id` = 2;

UPDATE `submission` SET
    `status` = 'PUBLISHED',
    `ai_analyze_used_count` = 1,
    `current_trial_id` = 8900000000000000004,
    `confirmed_trial_id` = 8900000000000000004,
    `confirmed_at` = '2026-06-18 10:05:00'
WHERE `id` = 3;

UPDATE `submission` SET
    `status` = 'PUBLISHED',
    `ai_analyze_used_count` = 1,
    `current_trial_id` = 8900000000000000005,
    `confirmed_trial_id` = 8900000000000000005,
    `confirmed_at` = '2026-07-05 16:10:00'
WHERE `id` = 5;

-- 5.2 教师复核中（student2 任务 3）
INSERT IGNORE INTO `submission_trial`
(`id`, `submission_id`, `trial_no`, `submit_type`, `git_url`, `summary`, `submit_time`, `status`, `ai_reference_score`) VALUES
(8900000000000000006, 4, 1, 'GIT_URL', 'https://github.com/wangfang/vue-components', '实现了一套基础Vue3组件库，包含6个常用组件。', '2026-07-07 09:00:00', 'CONFIRMED', 82.00);

UPDATE `submission` SET
    `status` = 'REVIEWING',
    `ai_analyze_used_count` = 1,
    `current_trial_id` = 8900000000000000006,
    `confirmed_trial_id` = 8900000000000000006,
    `confirmed_at` = '2026-07-07 09:15:00'
WHERE `id` = 4;

-- ============================================================
-- 6. 新增提交 — 覆盖各流程状态
-- ============================================================
INSERT IGNORE INTO `submission`
(`id`, `training_task_id`, `user_id`, `submit_type`, `git_url`, `summary`, `submit_count`,
 `ai_analyze_used_count`, `current_trial_id`, `confirmed_trial_id`, `confirmed_at`, `submit_time`, `status`) VALUES
-- student2 任务1：待教师审核 CONFIRMED
(100, 1, 4, 'GIT_URL', 'https://github.com/wangfang/book-mgr-fork', 'Fork 后完成借阅模块', 1,
 1, 8900000000000000100, 8900000000000000100, '2026-07-10 11:00:00', '2026-07-10 11:00:00', 'CONFIRMED'),
-- student3 任务1：已驳回 REJECTED（可重新试交）
(101, 1, 8, 'ZIP_ONLY', NULL, '首版提交缺少借阅模块', 2,
 0, NULL, NULL, NULL, '2026-07-08 09:00:00', 'REJECTED'),
-- student4 任务2：待确认 AI_READY
(102, 2, 9, 'ZIP_ONLY', NULL, 'Servlet 会话管理完整实现', 1,
 1, 8900000000000000102, NULL, NULL, '2026-06-19 14:00:00', 'AI_READY'),
-- student5 任务1：AI 分析中 ANALYZING
(103, 1, 10, 'GIT_URL', 'https://gitee.com/zhoujie/book-mgr', '正在试交第二版', 2,
 2, 8900000000000000104, NULL, NULL, '2026-08-20 16:00:00', 'ANALYZING'),
-- student7 任务10：截止未完成 INCOMPLETE
(104, 10, 12, 'ZIP_ONLY', NULL, '仅完成本地草稿未确认', 1,
 1, 8900000000000000105, NULL, NULL, '2026-05-28 20:00:00', 'INCOMPLETE'),
-- student8 任务1：待审核 CONFIRMED
(105, 1, 13, 'GIT_URL', 'https://gitee.com/qianxue/book-mgr', '完成全部功能与测试', 1,
 1, 8900000000000000107, 8900000000000000107, '2026-07-12 10:30:00', '2026-07-12 10:30:00', 'CONFIRMED'),
-- student1 任务11：试交完成待确认 AI_READY
(106, 11, 3, 'ZIP_ONLY', NULL, 'OpenAPI 文档已补全', 1,
 1, 8900000000000000108, NULL, NULL, '2026-08-25 09:00:00', 'AI_READY');

INSERT IGNORE INTO `submission_trial`
(`id`, `submission_id`, `trial_no`, `submit_type`, `git_url`, `summary`, `submit_time`, `status`, `ai_reference_score`) VALUES
(8900000000000000100, 100, 1, 'GIT_URL', 'https://github.com/wangfang/book-mgr-fork', 'Fork 后完成借阅模块', '2026-07-10 11:00:00', 'CONFIRMED', 86.00),
(8900000000000000101, 101, 1, 'ZIP_ONLY', NULL, '首版提交缺少借阅模块', '2026-07-07 15:00:00', 'CONFIRMED', 62.00),
(8900000000000000102, 102, 1, 'ZIP_ONLY', NULL, 'Servlet 会话管理完整实现', '2026-06-19 14:00:00', 'AI_READY', 84.00),
(8900000000000000103, 103, 1, 'GIT_URL', 'https://gitee.com/zhoujie/book-mgr', '第一版试交', '2026-08-19 10:00:00', 'SUPERSEDED', 70.00),
(8900000000000000104, 103, 2, 'GIT_URL', 'https://gitee.com/zhoujie/book-mgr', '正在试交第二版', '2026-08-20 16:00:00', 'ANALYZING', NULL),
(8900000000000000105, 104, 1, 'ZIP_ONLY', NULL, '仅完成本地草稿未确认', '2026-05-28 20:00:00', 'AI_READY', 55.00),
(8900000000000000107, 105, 1, 'GIT_URL', 'https://gitee.com/qianxue/book-mgr', '完成全部功能与测试', '2026-07-12 10:30:00', 'CONFIRMED', 91.00),
(8900000000000000108, 106, 1, 'ZIP_ONLY', NULL, 'OpenAPI 文档已补全', '2026-08-25 09:00:00', 'AI_READY', 87.00);

INSERT IGNORE INTO `submission_file` (`id`, `submission_id`, `file_id`, `file_name`, `file_size`, `file_type`, `sort_order`) VALUES
(10, 101, 5002, 'book-mgr-source.zip', 601, 'application/zip', 1),
(11, 102, 5001, 'servlet-session.zip', 748, 'application/zip', 1),
(12, 104, 5002, 'book-mgr-source.zip', 601, 'application/zip', 1),
(13, 106, 5002, 'book-mgr-source.zip', 601, 'application/zip', 1);

-- ============================================================
-- 7. AI 分析（关联 trial_id）
-- ============================================================
INSERT IGNORE INTO `ai_analysis`
(`id`, `submission_id`, `trial_id`, `total_score`, `analysis_status`, `model_provider`, `model_name`, `complete_time`) VALUES
(6001, 2,  8900000000000000003, 85.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-06 14:32:00'),
(6002, 3,  8900000000000000004, 80.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-06-18 10:02:00'),
(6003, 5,  8900000000000000005, 88.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-05 16:05:00'),
(6004, 4,  8900000000000000006, 82.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-07 09:05:00'),
(6005, 100, 8900000000000000100, 86.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-10 11:05:00'),
(6006, 101, 8900000000000000101, 62.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-07 15:10:00'),
(6007, 102, 8900000000000000102, 84.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-06-19 14:05:00'),
(6008, 103, 8900000000000000104, NULL,  'ANALYZING', 'DEEPSEEK', 'deepseek-chat', NULL),
(6009, 106, 8900000000000000108, 87.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-08-25 09:05:00');

INSERT IGNORE INTO `ai_analysis_detail`
(`id`, `ai_analysis_id`, `dimension_id`, `agent_type`, `issue_type`, `severity`, `reason`, `suggestion`, `suggest_deduct`, `confidence`, `sort_order`) VALUES
(7001, 6001, 1001, 'CODE', '功能缺失', 'MAJOR', '借阅归还状态机不完整', '补充归还 overdue 处理', 3.00, 0.88, 1),
(7002, 6001, 1002, 'CODE', '命名不规范', 'MINOR', '部分 DTO 未遵循驼峰', '统一命名并补充 JavaDoc', 1.00, 0.92, 2),
(7003, 6006, 1001, 'REQ', '需求未覆盖', 'CRITICAL', '缺少借阅模块', '按任务书实现 BorrowService', 8.00, 0.95, 1),
(7004, 6007, 1003, 'CODE', '会话超时', 'MINOR', 'Session 超时未配置说明', '在 README 中说明 timeout', 2.00, 0.85, 1);

-- ============================================================
-- 8. 教师复核与成绩
-- ============================================================
INSERT IGNORE INTO `teacher_review`
(`id`, `submission_id`, `reviewer_id`, `teacher_comment`, `status`, `review_time`, `publish_time`) VALUES
(8001, 2,  2, '功能完整，分层清晰，建议补充异常处理。', 'PUBLISHED', '2026-07-08 10:00:00', '2026-07-08 15:00:00'),
(8002, 3,  2, '会话管理实现正确，文档可再完善。', 'PUBLISHED', '2026-06-20 09:00:00', '2026-06-20 11:00:00'),
(8003, 5,  2, '组件设计合理，TypeScript 类型较完整。', 'PUBLISHED', '2026-07-06 09:00:00', '2026-07-06 10:00:00'),
(8004, 4,  2, '组件 API 设计良好，Storybook 待补充。', 'REVIEWING', '2026-07-08 14:00:00', NULL),
(8005, 101, 2, '借阅模块缺失，请按意见修改后重新试交。', 'RETURNED', '2026-07-09 10:00:00', NULL);

UPDATE `score_record` SET `status` = 'PUBLISHED' WHERE `id` IN (10001, 10002, 10003);

-- ============================================================
-- 9. 通知
-- ============================================================
INSERT IGNORE INTO `notification`
(`id`, `user_id`, `title`, `content`, `notify_type`, `is_read`, `create_time`) VALUES
(9001, 3,  '成绩已发布', '《Spring Boot 图书管理系统实训》成绩 88 分已发布，请查看成绩单。', 'SCORE', 1, '2026-07-08 15:05:00'),
(9002, 3,  'AI 分析完成', '《API 文档与 OpenAPI 补全》试交 AI 参考分 87，请确认是否提交教师审核。', 'REMIND', 0, '2026-08-25 09:10:00'),
(9003, 8,  '提交被驳回', '《Spring Boot 图书管理系统实训》已被驳回：借阅模块缺失，请修改后重新试交。', 'SCORE', 0, '2026-07-09 10:35:00'),
(9004, 4,  '待教师复核', '《Vue 3 组件库实训》已确认提交，教师正在复核中。', 'SYSTEM', 0, '2026-07-07 09:20:00'),
(9005, 2,  '新的待审核提交', '钱雪 确认了《Spring Boot 图书管理系统实训》提交，请及时复核。', 'REMIND', 0, '2026-07-12 10:35:00'),
(9006, 13, '提交已收到', '您的成果已递交教师，请等待复核。', 'SYSTEM', 0, '2026-07-12 10:35:00'),
(9007, 9,  '待确认提交', '《Servlet 会话管理实训》AI 分析已完成，请确认是否提交教师。', 'REMIND', 0, '2026-06-19 14:10:00');

-- ============================================================
-- 10. 操作日志（管理员仪表盘）
-- ============================================================
INSERT IGNORE INTO `operation_log`
(`id`, `user_id`, `username`, `module`, `operation`, `target_type`, `target_id`, `ip_address`, `result`, `create_time`) VALUES
(9101, 1, 'admin', 'AUTH', 'LOGIN', 'User', 1, '127.0.0.1', 'SUCCESS', '2026-08-27 08:00:00'),
(9102, 2, 'teacher1', 'TRAINING', 'CREATE', 'TrainingTask', 11, '192.168.1.10', 'SUCCESS', '2026-08-01 10:05:00'),
(9103, 3, 'student1', 'SUBMISSION', 'CREATE', 'Submission', 106, '192.168.1.101', 'SUCCESS', '2026-08-25 09:00:00'),
(9104, 2, 'teacher1', 'REVIEW', 'UPDATE', 'Submission', 101, '192.168.1.10', 'SUCCESS', '2026-07-09 10:30:00'),
(9105, 1, 'admin', 'SYSTEM', 'UPDATE', 'SystemConfig', 1, '127.0.0.1', 'SUCCESS', '2026-08-20 14:00:00');
