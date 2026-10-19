-- V19: 报表演示数据 — 多学生、多课程、跨月成绩（不影响待审工作流 demo）
SET NAMES utf8mb4;

-- ============================================================
-- 0. 补齐 student1 软件工程课程选修（任务 12 归属 course 10）
-- ============================================================
INSERT IGNORE INTO `course_student` (`id`, `course_id`, `user_id`) VALUES
(23, 10, 3);

-- ============================================================
-- 1. student1 历史已发布提交（跨月趋势 + 多课程雷达）
-- ============================================================
INSERT INTO `submission`
(`id`, `training_task_id`, `user_id`, `submit_type`, `summary`, `submit_count`,
 `ai_analyze_used_count`, `submit_time`, `status`, `confirmed_at`) VALUES
(20001, 10, 3, 'ZIP_UPLOAD', 'Redis 缓存集成终稿', 1, 1, '2026-03-18 15:20:00', 'PUBLISHED', '2026-03-18 15:20:00'),
(20002, 11, 3, 'ZIP_UPLOAD', 'OpenAPI 文档补全', 1, 1, '2026-04-22 10:40:00', 'PUBLISHED', '2026-04-22 10:40:00'),
(20003, 12, 3, 'ZIP_UPLOAD', '团队里程碑 M1 交付', 1, 1, '2026-05-16 16:00:00', 'PUBLISHED', '2026-05-16 16:00:00')
ON DUPLICATE KEY UPDATE
    `status` = VALUES(`status`),
    `submit_time` = VALUES(`submit_time`),
    `summary` = VALUES(`summary`);

-- 保持 id=2 CONFIRMED、id=5 AI_READY 供审核/试交流程 demo

INSERT INTO `teacher_review`
(`id`, `submission_id`, `reviewer_id`, `teacher_comment`, `status`, `review_time`, `publish_time`) VALUES
(8010, 20001, 2, '缓存策略合理，穿透方案描述清晰。', 'PUBLISHED', '2026-03-20 09:00:00', '2026-03-20 10:00:00'),
(8011, 20002, 2, '接口文档覆盖完整，示例准确。', 'PUBLISHED', '2026-04-24 09:00:00', '2026-04-24 10:00:00'),
(8012, 20003, 2, '里程碑交付物齐全，协作记录完整。', 'PUBLISHED', '2026-05-18 09:00:00', '2026-05-18 10:00:00')
ON DUPLICATE KEY UPDATE
    `teacher_comment` = VALUES(`teacher_comment`),
    `status` = VALUES(`status`),
    `publish_time` = VALUES(`publish_time`);

INSERT INTO `score_record`
(`id`, `submission_id`, `user_id`, `training_task_id`, `total_score`, `ai_total_score`,
 `score_details`, `teacher_comment`, `status`, `publish_time`) VALUES
(10010, 20001, 3, 10, 78.00, 76.00,
 '[{"dimensionId":1001,"score":24,"remark":""},{"dimensionId":1002,"score":19,"remark":""},{"dimensionId":1003,"score":18,"remark":""},{"dimensionId":1004,"score":17,"remark":""}]',
 '缓存策略合理，穿透方案描述清晰。', 'PUBLISHED', '2026-03-20 10:00:00'),
(10011, 20002, 3, 11, 85.00, 83.00,
 '[{"dimensionId":1001,"score":26,"remark":""},{"dimensionId":1002,"score":22,"remark":""},{"dimensionId":1003,"score":20,"remark":""},{"dimensionId":1004,"score":17,"remark":""}]',
 '接口文档覆盖完整，示例准确。', 'PUBLISHED', '2026-04-24 10:00:00'),
(10012, 20003, 3, 12, 88.00, 86.00,
 '[{"dimensionId":1001,"score":27,"remark":""},{"dimensionId":1002,"score":22,"remark":""},{"dimensionId":1003,"score":21,"remark":""},{"dimensionId":1004,"score":18,"remark":""}]',
 '里程碑交付物齐全，协作记录完整。', 'PUBLISHED', '2026-05-18 10:00:00')
ON DUPLICATE KEY UPDATE
    `total_score` = VALUES(`total_score`),
    `score_details` = VALUES(`score_details`),
    `publish_time` = VALUES(`publish_time`);

UPDATE `score_record`
SET `score_details` = '[{"dimensionId":1001,"score":25,"remark":""},{"dimensionId":1002,"score":20,"remark":""},{"dimensionId":1003,"score":20,"remark":""},{"dimensionId":1004,"score":17,"remark":""}]',
    `total_score` = 82.00,
    `publish_time` = '2026-06-20 11:00:00'
WHERE `id` = 10002;

-- ============================================================
-- 2. 班级其他学生成绩（教师报表分布）
-- ============================================================
INSERT INTO `submission`
(`id`, `training_task_id`, `user_id`, `submit_type`, `summary`, `submit_count`,
 `ai_analyze_used_count`, `submit_time`, `status`, `confirmed_at`) VALUES
(20010, 2,  4,  'ZIP_UPLOAD', '王芳-Servlet 实现', 1, 1, '2026-06-22 14:00:00', 'PUBLISHED', '2026-06-22 14:00:00'),
(20015, 2,  13, 'ZIP_UPLOAD', '钱雪-Servlet 实现', 1, 1, '2026-06-28 10:00:00', 'PUBLISHED', '2026-06-28 10:00:00'),
(20016, 3,  13, 'ZIP_UPLOAD', '钱雪-Vue 组件库', 1, 1, '2026-07-14 15:00:00', 'PUBLISHED', '2026-07-14 15:00:00'),
(20017, 10, 9,  'ZIP_UPLOAD', '孙丽-Redis 缓存', 1, 1, '2026-05-30 11:00:00', 'PUBLISHED', '2026-05-30 11:00:00')
ON DUPLICATE KEY UPDATE
    `status` = VALUES(`status`),
    `submit_time` = VALUES(`submit_time`);

UPDATE `submission` SET
    `status` = 'PUBLISHED',
    `submit_time` = '2026-07-08 11:00:00',
    `confirmed_at` = '2026-07-08 11:00:00',
    `summary` = '王芳-Vue 组件库终稿'
WHERE `id` = 4;

UPDATE `submission` SET
    `status` = 'PUBLISHED',
    `submit_time` = '2026-07-15 10:00:00',
    `confirmed_at` = '2026-07-15 10:00:00',
    `summary` = '赵强-图书管理（二稿）'
WHERE `id` = 101;

UPDATE `submission` SET
    `status` = 'PUBLISHED',
    `submit_time` = '2026-06-25 16:00:00',
    `confirmed_at` = '2026-06-25 16:00:00',
    `summary` = '孙丽-Servlet 终稿'
WHERE `id` = 102;

UPDATE `submission` SET
    `status` = 'PUBLISHED',
    `submit_time` = '2026-08-25 09:00:00',
    `confirmed_at` = '2026-08-25 09:00:00',
    `summary` = '周杰-图书管理终稿'
WHERE `id` = 103;

UPDATE `submission` SET
    `status` = 'PUBLISHED',
    `submit_time` = '2026-06-05 20:00:00',
    `confirmed_at` = '2026-06-05 20:00:00',
    `summary` = '郑浩-Redis 草稿（补交）'
WHERE `id` = 104;

INSERT INTO `teacher_review`
(`id`, `submission_id`, `reviewer_id`, `teacher_comment`, `status`, `review_time`, `publish_time`) VALUES
(8020, 20010, 2, '实现规范，测试覆盖较好。', 'PUBLISHED', '2026-06-23 09:00:00', '2026-06-23 10:00:00'),
(8021, 4,     2, '组件抽象合理，文档完整。', 'PUBLISHED', '2026-07-09 09:00:00', '2026-07-09 10:00:00'),
(8022, 101,   2, '借阅模块已补全，整体良好。', 'PUBLISHED', '2026-07-16 09:00:00', '2026-07-16 10:00:00'),
(8023, 102,   2, '会话管理正确，代码整洁。', 'PUBLISHED', '2026-06-26 09:00:00', '2026-06-26 10:00:00'),
(8024, 103,   2, '第三版质量明显提升。', 'PUBLISHED', '2026-08-26 09:00:00', '2026-08-26 10:00:00'),
(8025, 20015, 2, '基础扎实，文档可再完善。', 'PUBLISHED', '2026-06-29 09:00:00', '2026-06-29 10:00:00'),
(8026, 20016, 2, '完成度高，类型定义规范。', 'PUBLISHED', '2026-07-15 09:00:00', '2026-07-15 10:00:00'),
(8027, 20017, 2, '缓存读写正确，方案说明清楚。', 'PUBLISHED', '2026-05-31 09:00:00', '2026-05-31 10:00:00'),
(8028, 104,   2, '基础功能完成，过期策略待优化。', 'PUBLISHED', '2026-06-06 09:00:00', '2026-06-06 10:00:00')
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`), `publish_time` = VALUES(`publish_time`);

INSERT INTO `score_record`
(`id`, `submission_id`, `user_id`, `training_task_id`, `total_score`, `ai_total_score`,
 `teacher_comment`, `status`, `publish_time`) VALUES
(10020, 20010, 4,  2,  76.00, 74.00, '实现规范，测试覆盖较好。', 'PUBLISHED', '2026-06-23 10:00:00'),
(10021, 4,     4,  3,  91.00, 89.00, '组件抽象合理，文档完整。', 'PUBLISHED', '2026-07-09 10:00:00'),
(10022, 101,   8,  1,  72.00, 70.00, '借阅模块已补全，整体良好。', 'PUBLISHED', '2026-07-16 10:00:00'),
(10023, 102,   9,  2,  84.00, 82.00, '会话管理正确，代码整洁。', 'PUBLISHED', '2026-06-26 10:00:00'),
(10024, 103,   10, 1,  81.00, 79.00, '第三版质量明显提升。', 'PUBLISHED', '2026-08-26 10:00:00'),
(10025, 20015, 13, 2,  79.00, 77.00, '基础扎实，文档可再完善。', 'PUBLISHED', '2026-06-29 10:00:00'),
(10026, 20016, 13, 3,  93.00, 91.00, '完成度高，类型定义规范。', 'PUBLISHED', '2026-07-15 10:00:00'),
(10027, 20017, 9,  10, 86.00, 84.00, '缓存读写正确，方案说明清楚。', 'PUBLISHED', '2026-05-31 10:00:00'),
(10028, 104,   12, 10, 58.00, 55.00, '基础功能完成，过期策略待优化。', 'PUBLISHED', '2026-06-06 10:00:00')
ON DUPLICATE KEY UPDATE
    `total_score` = VALUES(`total_score`),
    `publish_time` = VALUES(`publish_time`);

-- ============================================================
-- 3. AI 分析明细（成长雷达维度）
-- ============================================================
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
