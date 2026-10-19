-- V12: 重组演示种子 — 待教师复核为主、少量已通过、无 Git 提交
-- 设计原则：
--   1) 教师待审 CONFIRMED 留给你亲自复核发布
--   2) 仅保留 1 条 PUBLISHED 作仪表盘样例
--   3) 含 AI 次数用尽(AI_READY)、驳回(REJECTED)、待确认、未完成 等学生侧场景
--   4) 暂时不用 Git：submit_type 统一 ZIP_ONLY，清空 git_url
SET NAMES utf8mb4;

-- ============================================================
-- 1. 任务：提交方式改为 ZIP，去掉 Git
-- ============================================================
UPDATE `training_task`
SET `submission_type` = 'ZIP_ONLY',
    `standard_id` = COALESCE(`standard_id`, 1000),
    `max_ai_analyze_count` = COALESCE(`max_ai_analyze_count`, 3)
WHERE `id` IN (1, 2, 3, 10, 11, 12, 13)
   OR `submission_type` LIKE '%GIT%';

-- ============================================================
-- 2. 清理旧演示复核/成绩（保留 servlet 任务一条已发布样例 10002）
-- ============================================================
DELETE FROM `review_item` WHERE `review_id` IN (8001, 8003, 8004, 8005);
DELETE FROM `teacher_review` WHERE `id` IN (8001, 8003, 8004, 8005);
DELETE FROM `score_record` WHERE `id` IN (10001, 10003);

DELETE FROM `ai_analysis_detail` WHERE `id` >= 7001 AND `id` <= 7099;
DELETE FROM `ai_analysis` WHERE `id` >= 6001 AND `id` <= 6099;

-- ============================================================
-- 3. 重置/规范化 submission 主记录
-- ============================================================
UPDATE `submission`
SET `submit_type` = 'ZIP_ONLY', `git_url` = NULL, `git_branch` = NULL
WHERE `git_url` IS NOT NULL OR `submit_type` LIKE '%GIT%';

-- student1 任务1：待教师审核
UPDATE `submission` SET
    `status` = 'CONFIRMED',
    `submit_count` = 2,
    `ai_analyze_used_count` = 2,
    `current_trial_id` = 8900000000000000003,
    `confirmed_trial_id` = 8900000000000000003,
    `confirmed_at` = '2026-07-06 14:35:00',
    `summary` = '图书管理系统终稿（ZIP+报告）'
WHERE `id` = 2;

-- student1 任务2：唯一已发布样例（仪表盘/成绩单）
UPDATE `submission` SET
    `status` = 'PUBLISHED',
    `submit_count` = 1,
    `ai_analyze_used_count` = 1,
    `current_trial_id` = 8900000000000000004,
    `confirmed_trial_id` = 8900000000000000004,
    `confirmed_at` = '2026-06-18 10:05:00',
    `summary` = 'Servlet 会话管理 ZIP 提交'
WHERE `id` = 3;

-- student2 任务3：待教师审核
UPDATE `submission` SET
    `status` = 'CONFIRMED',
    `submit_count` = 1,
    `ai_analyze_used_count` = 1,
    `current_trial_id` = 8900000000000000006,
    `confirmed_trial_id` = 8900000000000000006,
    `confirmed_at` = '2026-07-07 09:15:00',
    `submit_type` = 'ZIP_ONLY',
    `git_url` = NULL,
    `summary` = 'Vue3 组件库 ZIP 提交'
WHERE `id` = 4;

-- student1 任务3(V7#5)：试交完成待确认（非已发布）
UPDATE `submission` SET
    `status` = 'AI_READY',
    `submit_count` = 1,
    `ai_analyze_used_count` = 1,
    `current_trial_id` = 8900000000000000005,
    `confirmed_trial_id` = NULL,
    `confirmed_at` = NULL,
    `submit_type` = 'ZIP_ONLY',
    `git_url` = NULL,
    `summary` = 'Vue 组件库试交稿'
WHERE `id` = 5;

-- ============================================================
-- 4. 试交记录（ZIP，无 Git）
-- ============================================================
INSERT INTO `submission_trial`
(`id`, `submission_id`, `trial_no`, `submit_type`, `summary`, `submit_time`, `status`, `ai_reference_score`) VALUES
(8900000000000000002, 2, 1, 'ZIP_ONLY', '图书管理初稿', '2026-07-05 10:00:00', 'SUPERSEDED', 78.00),
(8900000000000000003, 2, 2, 'ZIP_ONLY', '图书管理系统终稿', '2026-07-06 14:30:00', 'CONFIRMED', 85.00),
(8900000000000000004, 3, 1, 'ZIP_ONLY', 'Servlet 会话管理', '2026-06-18 10:00:00', 'CONFIRMED', 80.00),
(8900000000000000005, 5, 1, 'ZIP_ONLY', 'Vue 组件库试交', '2026-07-05 16:00:00', 'AI_READY', 88.00),
(8900000000000000006, 4, 1, 'ZIP_ONLY', 'Vue3 组件库', '2026-07-07 09:00:00', 'CONFIRMED', 82.00)
ON DUPLICATE KEY UPDATE
    `submit_type` = VALUES(`submit_type`),
    `summary` = VALUES(`summary`),
    `status` = VALUES(`status`),
    `ai_reference_score` = VALUES(`ai_reference_score`);

-- ============================================================
-- 5. 扩展提交场景（V11 新增 id 100+）
-- ============================================================
INSERT INTO `submission`
(`id`, `training_task_id`, `user_id`, `submit_type`, `summary`, `submit_count`,
 `ai_analyze_used_count`, `current_trial_id`, `confirmed_trial_id`, `confirmed_at`, `submit_time`, `status`) VALUES
(100, 1, 4,  'ZIP_ONLY', '王芳-图书管理终稿', 1, 1, 8900000000000000100, 8900000000000000100, '2026-07-10 11:00:00', '2026-07-10 11:00:00', 'CONFIRMED'),
(105, 1, 13, 'ZIP_ONLY', '钱雪-图书管理终稿', 1, 1, 8900000000000000107, 8900000000000000107, '2026-07-12 10:30:00', '2026-07-12 10:30:00', 'CONFIRMED'),
(101, 1, 8,  'ZIP_ONLY', '赵强-首版缺借阅模块（已驳回）', 1, 0, NULL, NULL, NULL, '2026-07-08 09:00:00', 'REJECTED'),
(102, 2, 9,  'ZIP_ONLY', '孙丽-Servlet 完整实现', 1, 1, 8900000000000000102, NULL, NULL, '2026-06-19 14:00:00', 'AI_READY'),
(103, 1, 10, 'ZIP_ONLY', '周杰-第三版试交（次数已用尽）', 3, 3, 8900000000000000109, NULL, NULL, '2026-08-22 16:00:00', 'AI_READY'),
(104, 10, 12, 'ZIP_ONLY', '郑浩-Redis 草稿未确认', 1, 1, 8900000000000000105, NULL, NULL, '2026-05-28 20:00:00', 'INCOMPLETE')
ON DUPLICATE KEY UPDATE
    `status` = VALUES(`status`),
    `ai_analyze_used_count` = VALUES(`ai_analyze_used_count`),
    `current_trial_id` = VALUES(`current_trial_id`),
    `confirmed_trial_id` = VALUES(`confirmed_trial_id`),
    `confirmed_at` = VALUES(`confirmed_at`),
    `summary` = VALUES(`summary`);

DELETE FROM `submission` WHERE `id` = 106;

INSERT INTO `submission_trial`
(`id`, `submission_id`, `trial_no`, `submit_type`, `summary`, `submit_time`, `status`, `ai_reference_score`) VALUES
(8900000000000000100, 100, 1, 'ZIP_ONLY', '王芳-图书管理终稿', '2026-07-10 11:00:00', 'CONFIRMED', 86.00),
(8900000000000000107, 105, 1, 'ZIP_ONLY', '钱雪-图书管理终稿', '2026-07-12 10:30:00', 'CONFIRMED', 91.00),
(8900000000000000101, 101, 1, 'ZIP_ONLY', '赵强-首版', '2026-07-07 15:00:00', 'CONFIRMED', 62.00),
(8900000000000000102, 102, 1, 'ZIP_ONLY', '孙丽-Servlet 完整实现', '2026-06-19 14:00:00', 'AI_READY', 84.00),
(8900000000000000106, 103, 1, 'ZIP_ONLY', '周杰-第一版', '2026-08-19 10:00:00', 'SUPERSEDED', 70.00),
(8900000000000000108, 103, 2, 'ZIP_ONLY', '周杰-第二版', '2026-08-21 14:00:00', 'SUPERSEDED', 76.00),
(8900000000000000109, 103, 3, 'ZIP_ONLY', '周杰-第三版', '2026-08-22 16:00:00', 'AI_READY', 81.00),
(8900000000000000105, 104, 1, 'ZIP_ONLY', '郑浩-Redis 草稿', '2026-05-28 20:00:00', 'AI_READY', 55.00)
ON DUPLICATE KEY UPDATE
    `status` = VALUES(`status`),
    `ai_reference_score` = VALUES(`ai_reference_score`);

-- ============================================================
-- 6. AI 分析（仅服务待审/样例/试交场景，不含已替你审完的）
-- ============================================================
INSERT INTO `ai_analysis`
(`id`, `submission_id`, `trial_id`, `total_score`, `analysis_status`, `model_provider`, `model_name`, `complete_time`) VALUES
(6001, 2,   8900000000000000003, 85.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-06 14:32:00'),
(6002, 3,   8900000000000000004, 80.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-06-18 10:02:00'),
(6004, 4,   8900000000000000006, 82.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-07 09:05:00'),
(6005, 100, 8900000000000000100, 86.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-10 11:05:00'),
(6007, 102, 8900000000000000102, 84.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-06-19 14:05:00'),
(6009, 103, 8900000000000000109, 81.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-08-22 16:05:00'),
(6010, 105, 8900000000000000107, 91.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-12 10:35:00'),
(6011, 5,   8900000000000000005, 88.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-07-05 16:05:00')
ON DUPLICATE KEY UPDATE
    `total_score` = VALUES(`total_score`),
    `analysis_status` = VALUES(`analysis_status`),
    `trial_id` = VALUES(`trial_id`);

INSERT INTO `ai_analysis_detail`
(`id`, `ai_analysis_id`, `dimension_id`, `agent_type`, `issue_type`, `severity`, `reason`, `suggestion`, `suggest_deduct`, `confidence`, `sort_order`) VALUES
(7001, 6001, 1001, 'CODE', '功能缺失', 'MAJOR', '借阅归还状态机不完整', '补充 overdue 处理', 3.00, 0.88, 1),
(7002, 6005, 1001, 'CODE', '完成度良好', 'MINOR', '整体结构清晰', '可补充单元测试', 1.00, 0.90, 1),
(7003, 6009, 1002, 'CODE', '规范性', 'MINOR', '部分命名可优化', '统一 DTO 命名', 2.00, 0.85, 1),
(7004, 6010, 1001, 'REQ', '完成度', 'MINOR', '功能完整', '文档可再完善', 1.00, 0.92, 1)
ON DUPLICATE KEY UPDATE `reason` = VALUES(`reason`);

-- ============================================================
-- 7. 教师复核与成绩 — 仅 1 条已发布（student1 任务2）
-- ============================================================
INSERT INTO `teacher_review`
(`id`, `submission_id`, `reviewer_id`, `teacher_comment`, `status`, `review_time`, `publish_time`) VALUES
(8002, 3, 2, '会话管理实现正确，文档可再完善。', 'PUBLISHED', '2026-06-20 09:00:00', '2026-06-20 11:00:00')
ON DUPLICATE KEY UPDATE
    `teacher_comment` = VALUES(`teacher_comment`),
    `status` = VALUES(`status`);

UPDATE `score_record`
SET `status` = 'PUBLISHED', `total_score` = 82.00, `ai_total_score` = 80.00,
    `teacher_comment` = '会话管理实现正确，文档可再完善。'
WHERE `id` = 10002;

-- ============================================================
-- 8. 通知（待办导向）
-- ============================================================
DELETE FROM `notification` WHERE `id` BETWEEN 9001 AND 9010;

INSERT IGNORE INTO `notification`
(`id`, `user_id`, `title`, `content`, `notify_type`, `is_read`, `create_time`) VALUES
(9001, 2,  '待审核提醒', '学生王芳、钱雪等已确认提交《图书管理系统》，请进入审核工作台处理。', 'REMIND', 0, '2026-08-27 09:00:00'),
(9002, 3,  '成绩已发布', '《Servlet 会话管理实训》成绩 82 分已发布。', 'SCORE', 1, '2026-06-20 11:05:00'),
(9003, 8,  '提交被驳回', '《图书管理系统》已被驳回，请修改后重新试交（AI 次数已重置）。', 'SCORE', 0, '2026-07-09 10:35:00'),
(9004, 9,  '待确认提交', '《Servlet 会话管理》AI 分析已完成，请确认是否递交教师。', 'REMIND', 0, '2026-06-19 14:10:00'),
(9005, 10, 'AI 次数已用尽', '《图书管理系统》试交 3 次已完成，请确认提交或联系教师还原次数。', 'REMIND', 0, '2026-08-22 16:10:00'),
(9006, 3,  '待确认提交', '《Vue 组件库实训》试交完成，请确认是否递交教师。', 'REMIND', 0, '2026-07-05 16:10:00');
