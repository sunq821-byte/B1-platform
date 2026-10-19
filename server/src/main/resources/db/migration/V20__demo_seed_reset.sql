-- V20: 演示种子 canonical 重置 — 11 账号矩阵 + 教师审核台多类型提交
-- 密码均为 123456；清理 V12/V15/V19 叠层冲突，保留报表演示所需历史成绩
-- 设计要点：
--   teacher1 待审仅 2 条（王芳 GIT + 钱雪 ZIP），附件含源码/报告/截图
--   student1 优等生：多条 PUBLISHED + 一条 AI_READY，不占待审/驳回态
--   其余账号覆盖 REJECTED / AI_READY / INCOMPLETE / 未提交 等边缘场景
SET NAMES utf8mb4;

-- ============================================================
-- 0. 任务提交方式（支持 Git / ZIP / 在线代码演示）
-- ============================================================
UPDATE `training_task`
SET `submission_type` = 'GIT_ZIP',
    `standard_id` = COALESCE(`standard_id`, 1000),
    `max_ai_analyze_count` = COALESCE(`max_ai_analyze_count`, 3)
WHERE `id` = 1;

UPDATE `training_task`
SET `submission_type` = 'ZIP_UPLOAD',
    `standard_id` = COALESCE(`standard_id`, 1000),
    `max_ai_analyze_count` = COALESCE(`max_ai_analyze_count`, 3)
WHERE `id` IN (2, 3, 10, 12, 13);

UPDATE `training_task`
SET `submission_type` = 'GIT_ZIP',
    `standard_id` = COALESCE(`standard_id`, 1000),
    `max_ai_analyze_count` = COALESCE(`max_ai_analyze_count`, 3)
WHERE `id` = 11;

-- ============================================================
-- 1. 清理冲突数据（先子表后主表）
-- ============================================================
DELETE ri FROM `review_item` ri
INNER JOIN `teacher_review` tr ON ri.`review_id` = tr.`id`
WHERE tr.`submission_id` IN (2, 3, 4, 5, 100, 101, 102, 103, 104, 105, 107,
                             20001, 20002, 20003, 20010, 20015, 20016, 20017);

DELETE FROM `teacher_review`
WHERE `submission_id` IN (2, 3, 4, 5, 100, 101, 102, 103, 104, 105, 107,
                          20001, 20002, 20003, 20010, 20015, 20016, 20017)
   OR `id` IN (8001, 8003, 8004, 8005, 8021, 8022, 8023, 8024, 8028);

DELETE FROM `score_record`
WHERE `submission_id` IN (4, 5, 101, 102, 103, 104, 107)
   OR `id` IN (10003, 10021, 10022, 10023, 10024, 10028);

DELETE FROM `ai_analysis_detail`
WHERE `ai_analysis_id` IN (
    SELECT `id` FROM (
        SELECT `id` FROM `ai_analysis`
        WHERE `submission_id` IN (2, 3, 4, 5, 100, 101, 102, 103, 104, 105, 107, 106)
           OR `id` IN (6004, 6008, 6012)
    ) t
);

DELETE FROM `ai_analysis`
WHERE `submission_id` IN (2, 3, 4, 5, 100, 101, 102, 103, 104, 105, 107, 106)
   OR `id` IN (6004, 6008, 6012);

DELETE FROM `submission_file`
WHERE `submission_id` IN (2, 3, 4, 5, 100, 101, 102, 103, 104, 105, 107, 106);

DELETE FROM `submission_trial`
WHERE `submission_id` IN (4, 107, 106);

DELETE FROM `submission` WHERE `id` IN (4, 106, 107);

-- ============================================================
-- 2. submission 主记录 — canonical 矩阵
-- ============================================================

-- student1 李明：图书管理已发布（Git + 附件，历史优等生）
UPDATE `submission` SET
    `training_task_id` = 1,
    `user_id` = 3,
    `submit_type` = 'GIT_URL',
    `git_url` = 'https://gitee.com/liming/book-mgr',
    `git_branch` = 'main',
    `summary` = '图书管理系统终稿：CRUD + 借阅 + JWT 认证',
    `submit_count` = 2,
    `ai_analyze_used_count` = 2,
    `current_trial_id` = 8900000000000000003,
    `confirmed_trial_id` = 8900000000000000003,
    `confirmed_at` = '2026-07-06 14:35:00',
    `submit_time` = '2026-07-06 14:35:00',
    `status` = 'PUBLISHED'
WHERE `id` = 2;

-- student1：Servlet 已发布（ZIP + 运行截图）
UPDATE `submission` SET
    `training_task_id` = 2,
    `user_id` = 3,
    `submit_type` = 'ZIP_UPLOAD',
    `git_url` = NULL,
    `git_branch` = NULL,
    `summary` = 'Servlet 会话管理 ZIP 提交',
    `submit_count` = 1,
    `ai_analyze_used_count` = 1,
    `current_trial_id` = 8900000000000000004,
    `confirmed_trial_id` = 8900000000000000004,
    `confirmed_at` = '2026-06-18 10:05:00',
    `submit_time` = '2026-06-18 10:05:00',
    `status` = 'PUBLISHED'
WHERE `id` = 3;

-- student1：Vue 在线代码，试交完成待确认
UPDATE `submission` SET
    `training_task_id` = 3,
    `user_id` = 3,
    `submit_type` = 'ONLINE_CODE',
    `git_url` = NULL,
    `git_branch` = NULL,
    `summary` = 'Vue3 BaseButton 在线代码提交',
    `submit_count` = 1,
    `ai_analyze_used_count` = 1,
    `current_trial_id` = 8900000000000000005,
    `confirmed_trial_id` = NULL,
    `confirmed_at` = NULL,
    `submit_time` = '2026-07-05 16:00:00',
    `status` = 'AI_READY'
WHERE `id` = 5;

-- student2 王芳：图书管理待审（Git 主提交 + ZIP/报告/截图附件）
INSERT INTO `submission`
(`id`, `training_task_id`, `user_id`, `submit_type`, `git_url`, `git_branch`, `summary`, `submit_count`,
 `ai_analyze_used_count`, `current_trial_id`, `confirmed_trial_id`, `confirmed_at`, `submit_time`, `status`) VALUES
(100, 1, 4, 'GIT_URL', 'https://github.com/wangfang/book-mgr-fork', 'main',
 '王芳-图书管理：Fork 后补全借阅模块与单元测试', 1, 1,
 8900000000000000100, 8900000000000000100, '2026-07-10 11:00:00', '2026-07-10 11:00:00', 'CONFIRMED')
ON DUPLICATE KEY UPDATE
    `submit_type` = VALUES(`submit_type`),
    `git_url` = VALUES(`git_url`),
    `git_branch` = VALUES(`git_branch`),
    `summary` = VALUES(`summary`),
    `status` = VALUES(`status`),
    `ai_analyze_used_count` = VALUES(`ai_analyze_used_count`),
    `current_trial_id` = VALUES(`current_trial_id`),
    `confirmed_trial_id` = VALUES(`confirmed_trial_id`),
    `confirmed_at` = VALUES(`confirmed_at`);

-- student8 钱雪：图书管理待审（ZIP 主提交 + 报告 + 双截图）
INSERT INTO `submission`
(`id`, `training_task_id`, `user_id`, `submit_type`, `git_url`, `git_branch`, `summary`, `submit_count`,
 `ai_analyze_used_count`, `current_trial_id`, `confirmed_trial_id`, `confirmed_at`, `submit_time`, `status`) VALUES
(105, 1, 13, 'ZIP_UPLOAD', NULL, NULL,
 '钱雪-图书管理：源码 ZIP + 实训报告 + 界面截图', 1, 1,
 8900000000000000107, 8900000000000000107, '2026-07-12 10:30:00', '2026-07-12 10:30:00', 'CONFIRMED')
ON DUPLICATE KEY UPDATE
    `submit_type` = VALUES(`submit_type`),
    `git_url` = VALUES(`git_url`),
    `summary` = VALUES(`summary`),
    `status` = VALUES(`status`),
    `current_trial_id` = VALUES(`current_trial_id`),
    `confirmed_trial_id` = VALUES(`confirmed_trial_id`),
    `confirmed_at` = VALUES(`confirmed_at`);

-- student3 赵强：已驳回，AI 次数已重置
INSERT INTO `submission`
(`id`, `training_task_id`, `user_id`, `submit_type`, `git_url`, `git_branch`, `summary`, `submit_count`,
 `ai_analyze_used_count`, `current_trial_id`, `confirmed_trial_id`, `confirmed_at`, `submit_time`, `status`) VALUES
(101, 1, 8, 'ZIP_UPLOAD', NULL, NULL,
 '赵强-首版缺借阅模块（已驳回）', 1, 0, NULL, NULL, NULL, '2026-07-08 09:00:00', 'REJECTED')
ON DUPLICATE KEY UPDATE
    `submit_type` = VALUES(`submit_type`),
    `status` = VALUES(`status`),
    `ai_analyze_used_count` = VALUES(`ai_analyze_used_count`),
    `current_trial_id` = VALUES(`current_trial_id`),
    `confirmed_trial_id` = VALUES(`confirmed_trial_id`),
    `confirmed_at` = VALUES(`confirmed_at`),
    `summary` = VALUES(`summary`);

-- student4 孙丽：Servlet 在线代码，待确认
INSERT INTO `submission`
(`id`, `training_task_id`, `user_id`, `submit_type`, `git_url`, `git_branch`, `summary`, `submit_count`,
 `ai_analyze_used_count`, `current_trial_id`, `confirmed_trial_id`, `confirmed_at`, `submit_time`, `status`) VALUES
(102, 2, 9, 'ONLINE_CODE', NULL, NULL,
 '孙丽-Servlet 在线代码 + 运行截图', 1, 1,
 8900000000000000102, NULL, NULL, '2026-06-19 14:00:00', 'AI_READY')
ON DUPLICATE KEY UPDATE
    `submit_type` = VALUES(`submit_type`),
    `status` = VALUES(`status`),
    `summary` = VALUES(`summary`),
    `current_trial_id` = VALUES(`current_trial_id`),
    `confirmed_trial_id` = VALUES(`confirmed_trial_id`),
    `confirmed_at` = VALUES(`confirmed_at`);

-- student5 周杰：三次试交后在线代码，AI 3/3 用尽，仍待确认
INSERT INTO `submission`
(`id`, `training_task_id`, `user_id`, `submit_type`, `git_url`, `git_branch`, `summary`, `submit_count`,
 `ai_analyze_used_count`, `current_trial_id`, `confirmed_trial_id`, `confirmed_at`, `submit_time`, `status`) VALUES
(103, 1, 10, 'ONLINE_CODE', NULL, NULL,
 '周杰-第三版在线代码（AI 次数已用尽）', 3, 3,
 8900000000000000109, NULL, NULL, '2026-08-22 16:00:00', 'AI_READY')
ON DUPLICATE KEY UPDATE
    `submit_type` = VALUES(`submit_type`),
    `status` = VALUES(`status`),
    `submit_count` = VALUES(`submit_count`),
    `ai_analyze_used_count` = VALUES(`ai_analyze_used_count`),
    `current_trial_id` = VALUES(`current_trial_id`),
    `summary` = VALUES(`summary`);

-- student7 郑浩：Redis 截止未完成
INSERT INTO `submission`
(`id`, `training_task_id`, `user_id`, `submit_type`, `git_url`, `git_branch`, `summary`, `submit_count`,
 `ai_analyze_used_count`, `current_trial_id`, `confirmed_trial_id`, `confirmed_at`, `submit_time`, `status`) VALUES
(104, 10, 12, 'ZIP_UPLOAD', NULL, NULL,
 '郑浩-Redis 草稿未确认（已截止）', 1, 1,
 8900000000000000105, NULL, NULL, '2026-05-28 20:00:00', 'INCOMPLETE')
ON DUPLICATE KEY UPDATE
    `submit_type` = VALUES(`submit_type`),
    `status` = VALUES(`status`),
    `summary` = VALUES(`summary`),
    `current_trial_id` = VALUES(`current_trial_id`);

-- student6 吴敏：无提交（确保无残留）
-- （已在 §1 删除 id=107）

-- ============================================================
-- 3. 试交记录
-- ============================================================
INSERT INTO `submission_trial`
(`id`, `submission_id`, `trial_no`, `submit_type`, `git_url`, `git_branch`, `summary`, `submit_time`, `status`, `ai_reference_score`) VALUES
(8900000000000000002, 2, 1, 'GIT_URL', 'https://gitee.com/liming/book-mgr', 'dev', '图书管理初稿', '2026-07-05 10:00:00', 'SUPERSEDED', 78.00),
(8900000000000000003, 2, 2, 'GIT_URL', 'https://gitee.com/liming/book-mgr', 'main', '图书管理系统终稿', '2026-07-06 14:30:00', 'CONFIRMED', 85.00),
(8900000000000000004, 3, 1, 'ZIP_UPLOAD', NULL, NULL, 'Servlet 会话管理', '2026-06-18 10:00:00', 'CONFIRMED', 80.00),
(8900000000000000005, 5, 1, 'ONLINE_CODE', NULL, NULL, '在线代码：BaseButton 组件', '2026-07-05 16:00:00', 'AI_READY', 88.00),
(8900000000000000100, 100, 1, 'GIT_URL', 'https://github.com/wangfang/book-mgr-fork', 'main', 'Fork 后完成借阅模块', '2026-07-10 11:00:00', 'CONFIRMED', 86.00),
(8900000000000000101, 101, 1, 'ZIP_UPLOAD', NULL, NULL, '赵强-首版', '2026-07-07 15:00:00', 'CONFIRMED', 62.00),
(8900000000000000102, 102, 1, 'ONLINE_CODE', NULL, NULL, '在线代码：LoginServlet 会话实现', '2026-06-19 14:00:00', 'AI_READY', 84.00),
(8900000000000000106, 103, 1, 'GIT_URL', 'https://gitee.com/zhoujie/book-mgr', 'v1', '周杰-第一版 Git', '2026-08-19 10:00:00', 'SUPERSEDED', 70.00),
(8900000000000000108, 103, 2, 'ZIP_UPLOAD', NULL, NULL, '周杰-第二版 ZIP', '2026-08-21 14:00:00', 'SUPERSEDED', 76.00),
(8900000000000000109, 103, 3, 'ONLINE_CODE', NULL, NULL, '在线代码：RedisCacheService 缓存层', '2026-08-22 16:00:00', 'AI_READY', 81.00),
(8900000000000000105, 104, 1, 'ZIP_UPLOAD', NULL, NULL, '郑浩-Redis 草稿', '2026-05-28 20:00:00', 'AI_READY', 55.00),
(8900000000000000107, 105, 1, 'ZIP_UPLOAD', NULL, NULL, '钱雪-图书管理终稿', '2026-07-12 10:30:00', 'CONFIRMED', 91.00)
ON DUPLICATE KEY UPDATE
    `submit_type` = VALUES(`submit_type`),
    `git_url` = VALUES(`git_url`),
    `git_branch` = VALUES(`git_branch`),
    `summary` = VALUES(`summary`),
    `status` = VALUES(`status`),
    `ai_reference_score` = VALUES(`ai_reference_score`);

-- ============================================================
-- 4. 附件 — 审核台丰富：ZIP / 报告 MD / PNG / JPG / 在线源码
-- ============================================================
INSERT INTO `submission_file`
(`id`, `submission_id`, `file_id`, `file_name`, `file_size`, `file_type`, `sort_order`) VALUES
-- student1 已发布
(1,  2, 5002, 'book-mgr-source.zip',     601,   'application/zip',  1),
(2,  2, 5004, 'book-mgr-实训报告.md',    1200,  'text/markdown',    2),
(30, 2, 5013, 'book-mgr-ui.png',         8606,  'image/png',        3),
(3,  3, 5001, 'servlet-session.zip',     748,   'application/zip',  1),
(31, 3, 5005, 'servlet-session-实训报告.md', 800, 'text/markdown',  2),
(21, 3, 5016, 'servlet-session-run.jpg', 23453, 'image/jpeg',       3),
(25, 5, 5011, 'BaseButton.vue',          498,   'text/plain',       1),
-- 待审：王芳 Git + 备份包/报告/截图
(14, 100, 5002, 'book-mgr-source.zip',    601,   'application/zip',  1),
(15, 100, 5004, 'book-mgr-实训报告.md',   1200,  'text/markdown',    2),
(23, 100, 5013, 'book-mgr-ui.png',        8606,  'image/png',        3),
-- 待审：钱雪 ZIP + 报告 + 双截图
(16, 105, 5002, 'book-mgr-source.zip',    601,   'application/zip',  1),
(17, 105, 5004, 'book-mgr-实训报告.md',   1200,  'text/markdown',    2),
(24, 105, 5013, 'book-mgr-ui.png',        8606,  'image/png',        3),
(32, 105, 5016, 'servlet-session-run.jpg', 23453, 'image/jpeg',     4),
-- 驳回
(10, 101, 5002, 'book-mgr-source.zip',     601,   'application/zip',  1),
(33, 101, 5004, 'book-mgr-实训报告.md',    1200,  'text/markdown',    2),
-- 在线代码 + 截图
(11, 102, 5009, 'LoginServlet.java',       1300,  'text/plain',       1),
(34, 102, 5016, 'servlet-session-run.jpg', 23453, 'image/jpeg',       2),
(19, 103, 5010, 'RedisCacheService.java',  1295,  'text/plain',       1),
-- 未完成 Redis
(12, 104, 5002, 'book-mgr-source.zip',      601,   'application/zip',  1),
(27, 104, 5014, 'redis-cache-screenshot.png', 9869, 'image/png',       2)
ON DUPLICATE KEY UPDATE
    `file_id` = VALUES(`file_id`),
    `file_name` = VALUES(`file_name`),
    `file_size` = VALUES(`file_size`),
    `file_type` = VALUES(`file_type`),
    `sort_order` = VALUES(`sort_order`);

-- ============================================================
-- 5. AI 分析
-- ============================================================
INSERT INTO `ai_analysis`
(`id`, `submission_id`, `trial_id`, `total_score`, `analysis_status`, `model_provider`, `model_name`, `complete_time`) VALUES
(6001, 2,   8900000000000000003, 85.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-06 14:32:00'),
(6002, 3,   8900000000000000004, 80.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-06-18 10:02:00'),
(6005, 100, 8900000000000000100, 86.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-10 11:05:00'),
(6006, 101, 8900000000000000101, 62.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-07 15:10:00'),
(6007, 102, 8900000000000000102, 84.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-06-19 14:05:00'),
(6009, 103, 8900000000000000109, 81.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-08-22 16:05:00'),
(6010, 105, 8900000000000000107, 91.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-12 10:35:00'),
(6011, 5,   8900000000000000005, 88.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-05 16:05:00')
ON DUPLICATE KEY UPDATE
    `total_score` = VALUES(`total_score`),
    `analysis_status` = VALUES(`analysis_status`),
    `trial_id` = VALUES(`trial_id`),
    `complete_time` = VALUES(`complete_time`);

INSERT INTO `ai_analysis_detail`
(`id`, `ai_analysis_id`, `dimension_id`, `agent_type`, `issue_type`, `severity`, `reason`, `suggestion`, `suggest_deduct`, `confidence`, `sort_order`) VALUES
(7001, 6001, 1001, 'CODE', '功能缺失', 'MAJOR', '借阅归还状态机不完整', '补充 overdue 处理', 3.00, 0.88, 1),
(7002, 6001, 1002, 'CODE', '命名规范', 'MINOR', '部分 DTO 未遵循驼峰', '统一命名并补充 JavaDoc', 1.00, 0.92, 2),
(7003, 6006, 1001, 'REQ', '需求未覆盖', 'CRITICAL', '缺少借阅模块', '按任务书实现 BorrowService', 8.00, 0.95, 1),
(7004, 6007, 1003, 'CODE', '会话超时', 'MINOR', 'Session 超时未配置说明', '在 README 中说明 timeout', 2.00, 0.85, 1),
(7005, 6005, 1001, 'CODE', '完成度良好', 'MINOR', 'Fork 后功能完整', '可补充集成测试', 1.00, 0.90, 1),
(7006, 6005, 1004, 'DOC', '表达', 'MINOR', '报告结构清晰', '补充 API 时序图', 1.00, 0.88, 2),
(7007, 6010, 1001, 'REQ', '完成度', 'MINOR', '功能完整、测试充分', '文档可再完善', 1.00, 0.92, 1),
(7008, 6009, 1002, 'CODE', '规范性', 'MINOR', '缓存 key 命名可统一', '抽取常量类', 2.00, 0.85, 1),
(7009, 6011, 1002, 'CODE', '组件设计', 'MINOR', 'Props 类型定义完整', '补充 emit 文档', 1.00, 0.91, 1)
ON DUPLICATE KEY UPDATE `reason` = VALUES(`reason`), `suggestion` = VALUES(`suggestion`);

-- ============================================================
-- 6. 教师复核与成绩
-- ============================================================
INSERT INTO `teacher_review`
(`id`, `submission_id`, `reviewer_id`, `teacher_comment`, `status`, `review_time`, `publish_time`) VALUES
(8001, 2,  2, '功能完整，分层清晰，建议补充异常处理。', 'PUBLISHED', '2026-07-08 10:00:00', '2026-07-08 15:00:00'),
(8002, 3,  2, '会话管理实现正确，文档可再完善。', 'PUBLISHED', '2026-06-20 09:00:00', '2026-06-20 11:00:00'),
(8005, 101, 2, '借阅模块缺失，请按意见修改后重新试交（AI 次数已重置）。', 'RETURNED', '2026-07-09 10:00:00', NULL),
(8010, 20001, 2, '缓存策略合理，穿透方案描述清晰。', 'PUBLISHED', '2026-03-20 09:00:00', '2026-03-20 10:00:00'),
(8011, 20002, 2, '接口文档覆盖完整，示例准确。', 'PUBLISHED', '2026-04-24 09:00:00', '2026-04-24 10:00:00'),
(8012, 20003, 2, '里程碑交付物齐全，协作记录完整。', 'PUBLISHED', '2026-05-18 09:00:00', '2026-05-18 10:00:00'),
(8020, 20010, 2, '实现规范，测试覆盖较好。', 'PUBLISHED', '2026-06-23 09:00:00', '2026-06-23 10:00:00'),
(8025, 20015, 2, '基础扎实，文档可再完善。', 'PUBLISHED', '2026-06-29 09:00:00', '2026-06-29 10:00:00'),
(8026, 20016, 2, '完成度高，类型定义规范。', 'PUBLISHED', '2026-07-15 09:00:00', '2026-07-15 10:00:00'),
(8027, 20017, 2, '缓存读写正确，方案说明清楚。', 'PUBLISHED', '2026-05-31 09:00:00', '2026-05-31 10:00:00')
ON DUPLICATE KEY UPDATE
    `teacher_comment` = VALUES(`teacher_comment`),
    `status` = VALUES(`status`),
    `publish_time` = VALUES(`publish_time`);

INSERT INTO `score_record`
(`id`, `submission_id`, `user_id`, `training_task_id`, `total_score`, `ai_total_score`,
 `score_details`, `teacher_comment`, `status`, `publish_time`) VALUES
(10001, 2,  3,  1,  88.00, 85.00,
 '[{"dimensionId":1001,"score":26,"remark":""},{"dimensionId":1002,"score":22,"remark":""},{"dimensionId":1003,"score":21,"remark":""},{"dimensionId":1004,"score":19,"remark":""}]',
 '功能完整，分层清晰，建议补充异常处理。', 'PUBLISHED', '2026-07-08 15:00:00'),
(10002, 3,  3,  2,  82.00, 80.00,
 '[{"dimensionId":1001,"score":25,"remark":""},{"dimensionId":1002,"score":20,"remark":""},{"dimensionId":1003,"score":20,"remark":""},{"dimensionId":1004,"score":17,"remark":""}]',
 '会话管理实现正确，文档可再完善。', 'PUBLISHED', '2026-06-20 11:00:00'),
(10010, 20001, 3, 10, 78.00, 76.00,
 '[{"dimensionId":1001,"score":24,"remark":""},{"dimensionId":1002,"score":19,"remark":""},{"dimensionId":1003,"score":18,"remark":""},{"dimensionId":1004,"score":17,"remark":""}]',
 '缓存策略合理，穿透方案描述清晰。', 'PUBLISHED', '2026-03-20 10:00:00'),
(10011, 20002, 3, 11, 85.00, 83.00,
 '[{"dimensionId":1001,"score":26,"remark":""},{"dimensionId":1002,"score":22,"remark":""},{"dimensionId":1003,"score":20,"remark":""},{"dimensionId":1004,"score":17,"remark":""}]',
 '接口文档覆盖完整，示例准确。', 'PUBLISHED', '2026-04-24 10:00:00'),
(10012, 20003, 3, 12, 88.00, 86.00,
 '[{"dimensionId":1001,"score":27,"remark":""},{"dimensionId":1002,"score":22,"remark":""},{"dimensionId":1003,"score":21,"remark":""},{"dimensionId":1004,"score":18,"remark":""}]',
 '里程碑交付物齐全，协作记录完整。', 'PUBLISHED', '2026-05-18 10:00:00'),
(10020, 20010, 4,  2,  76.00, 74.00, NULL, '实现规范，测试覆盖较好。', 'PUBLISHED', '2026-06-23 10:00:00'),
(10025, 20015, 13, 2,  79.00, 77.00, NULL, '基础扎实，文档可再完善。', 'PUBLISHED', '2026-06-29 10:00:00'),
(10026, 20016, 13, 3,  93.00, 91.00, NULL, '完成度高，类型定义规范。', 'PUBLISHED', '2026-07-15 10:00:00'),
(10027, 20017, 9,  10, 86.00, 84.00, NULL, '缓存读写正确，方案说明清楚。', 'PUBLISHED', '2026-05-31 10:00:00')
ON DUPLICATE KEY UPDATE
    `total_score` = VALUES(`total_score`),
    `ai_total_score` = VALUES(`ai_total_score`),
    `score_details` = VALUES(`score_details`),
    `teacher_comment` = VALUES(`teacher_comment`),
    `status` = VALUES(`status`),
    `publish_time` = VALUES(`publish_time`);

-- V19 历史提交（student1 跨月趋势 + 班级分布）— 保持 PUBLISHED
INSERT INTO `submission`
(`id`, `training_task_id`, `user_id`, `submit_type`, `summary`, `submit_count`,
 `ai_analyze_used_count`, `submit_time`, `status`, `confirmed_at`) VALUES
(20001, 10, 3,  'ZIP_UPLOAD', 'Redis 缓存集成终稿', 1, 1, '2026-03-18 15:20:00', 'PUBLISHED', '2026-03-18 15:20:00'),
(20002, 11, 3,  'GIT_URL',    'OpenAPI 文档补全', 1, 1, '2026-04-22 10:40:00', 'PUBLISHED', '2026-04-22 10:40:00'),
(20003, 12, 3,  'ZIP_UPLOAD', '团队里程碑 M1 交付', 1, 1, '2026-05-16 16:00:00', 'PUBLISHED', '2026-05-16 16:00:00'),
(20010, 2,  4,  'ZIP_UPLOAD', '王芳-Servlet 实现', 1, 1, '2026-06-22 14:00:00', 'PUBLISHED', '2026-06-22 14:00:00'),
(20015, 2,  13, 'ZIP_UPLOAD', '钱雪-Servlet 实现', 1, 1, '2026-06-28 10:00:00', 'PUBLISHED', '2026-06-28 10:00:00'),
(20016, 3,  13, 'ONLINE_CODE', '钱雪-Vue 组件库在线代码', 1, 1, '2026-07-14 15:00:00', 'PUBLISHED', '2026-07-14 15:00:00'),
(20017, 10, 9,  'ONLINE_CODE', '孙丽-Redis 缓存在线代码', 1, 1, '2026-05-30 11:00:00', 'PUBLISHED', '2026-05-30 11:00:00')
ON DUPLICATE KEY UPDATE
    `status` = VALUES(`status`),
    `submit_type` = VALUES(`submit_type`),
    `submit_time` = VALUES(`submit_time`),
    `summary` = VALUES(`summary`);

INSERT INTO `ai_analysis`
(`id`, `submission_id`, `total_score`, `analysis_status`, `model_provider`, `model_name`, `complete_time`) VALUES
(6101, 20001, 76.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-03-18 15:25:00'),
(6102, 20002, 83.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-04-22 10:45:00'),
(6103, 20003, 86.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-05-16 16:05:00')
ON DUPLICATE KEY UPDATE `total_score` = VALUES(`total_score`);

INSERT INTO `ai_analysis_detail`
(`id`, `ai_analysis_id`, `dimension_id`, `agent_type`, `issue_type`, `severity`, `reason`, `suggestion`, `suggest_deduct`, `confidence`, `sort_order`) VALUES
(7101, 6101, 1001, 'CODE', '完成度', 'MINOR', '缓存命中逻辑完整', '补充击穿测试', 1.00, 0.90, 1),
(7102, 6101, 1002, 'CODE', '规范性', 'MINOR', '配置外置良好', '统一常量命名', 1.50, 0.88, 2),
(7103, 6102, 1001, 'DOC', '完成度', 'MINOR', '接口覆盖率高', '补充错误码说明', 1.00, 0.91, 1),
(7104, 6102, 1004, 'DOC', '表达', 'MINOR', '文档结构清晰', '增加时序图', 1.00, 0.89, 2),
(7105, 6103, 1001, 'REQ', '完成度', 'MINOR', '里程碑齐全', '风险清单可细化', 1.00, 0.92, 1)
ON DUPLICATE KEY UPDATE `reason` = VALUES(`reason`);

-- ============================================================
-- 7. 通知
-- ============================================================
DELETE FROM `notification` WHERE `id` BETWEEN 9001 AND 9010;

INSERT IGNORE INTO `notification`
(`id`, `user_id`, `title`, `content`, `notify_type`, `is_read`, `create_time`) VALUES
(9001, 2,  '待审核提醒', '王芳（Git 仓库）、钱雪（ZIP 附件）已确认提交《图书管理系统》，请进入审核工作台处理。', 'REMIND', 0, '2026-08-27 09:00:00'),
(9002, 3,  '成绩已发布', '《Servlet 会话管理实训》成绩 82 分已发布。', 'SCORE', 1, '2026-06-20 11:05:00'),
(9003, 8,  '提交被驳回', '《图书管理系统》已被驳回，请修改后重新试交（AI 次数已重置）。', 'SCORE', 0, '2026-07-09 10:35:00'),
(9004, 9,  '待确认提交', '《Servlet 会话管理》AI 分析已完成，请确认是否递交教师。', 'REMIND', 0, '2026-06-19 14:10:00'),
(9005, 10, 'AI 次数已用尽', '《图书管理系统》试交 3 次已完成，请确认提交或联系教师还原次数。', 'REMIND', 0, '2026-08-22 16:10:00'),
(9006, 3,  '待确认提交', '《Vue 组件库实训》在线代码试交完成，请确认是否递交教师。', 'REMIND', 0, '2026-07-05 16:10:00'),
(9007, 13, '提交已收到', '您的《图书管理系统》成果已递交教师，请等待复核。', 'SYSTEM', 0, '2026-07-12 10:35:00'),
(9008, 4,  '提交已收到', '您的 Git 仓库提交已递交教师，请等待复核。', 'SYSTEM', 0, '2026-07-10 11:05:00');

-- ============================================================
-- 8. 操作日志（管理员/教师仪表盘）
-- ============================================================
DELETE FROM `operation_log` WHERE `id` BETWEEN 9101 AND 9110;

INSERT IGNORE INTO `operation_log`
(`id`, `user_id`, `username`, `module`, `operation`, `target_type`, `target_id`, `ip_address`, `result`, `create_time`) VALUES
(9101, 1, 'admin', 'AUTH', 'LOGIN', 'User', 1, '127.0.0.1', 'SUCCESS', '2026-08-27 08:00:00'),
(9102, 2, 'teacher1', 'TRAINING', 'CREATE', 'TrainingTask', 11, '192.168.1.10', 'SUCCESS', '2026-08-01 10:05:00'),
(9103, 4, 'student2', 'SUBMISSION', 'CONFIRM', 'Submission', 100, '192.168.1.102', 'SUCCESS', '2026-07-10 11:00:00'),
(9104, 2, 'teacher1', 'REVIEW', 'RETURN', 'Submission', 101, '192.168.1.10', 'SUCCESS', '2026-07-09 10:30:00'),
(9105, 13, 'student8', 'SUBMISSION', 'CONFIRM', 'Submission', 105, '192.168.1.108', 'SUCCESS', '2026-07-12 10:30:00'),
(9106, 3, 'student1', 'SUBMISSION', 'TRIAL', 'Submission', 5, '192.168.1.101', 'SUCCESS', '2026-07-05 16:00:00');
