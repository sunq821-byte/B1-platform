-- V7: 学生仪表盘图表演示数据（为 student1 补齐已评分记录）
SET NAMES utf8mb4;

-- 将 student1 的两条提交标为已复核
UPDATE `submission`
SET `status` = 'REVIEWED'
WHERE `id` IN (2, 3) AND `user_id` = 3;

-- 成绩记录（幂等）
INSERT IGNORE INTO `score_record`
(`id`, `submission_id`, `user_id`, `training_task_id`, `total_score`, `ai_total_score`, `teacher_comment`, `status`, `publish_time`)
VALUES
(10001, 2, 3, 1, 88.00, 85.00, '功能完整，分层清晰，建议补充异常处理。', 'PUBLISHED', '2026-07-08 15:00:00'),
(10002, 3, 3, 2, 82.00, 80.00, '会话管理实现正确，文档可再完善。', 'PUBLISHED', '2026-06-20 11:00:00');

-- 为 student1 的 Vue 课程任务也补一条已评分提交，便于趋势图有 3 个点
INSERT IGNORE INTO `submission`
(`id`, `training_task_id`, `user_id`, `submit_type`, `status`, `submit_count`, `git_url`, `summary`, `submit_time`)
VALUES
(5, 3, 3, 'GIT_URL', 'REVIEWED', 1, 'https://gitee.com/liming/vue-components', '完成 Vue3 组件库基础组件实现。', '2026-07-05 16:00:00');

INSERT IGNORE INTO `score_record`
(`id`, `submission_id`, `user_id`, `training_task_id`, `total_score`, `ai_total_score`, `teacher_comment`, `status`, `publish_time`)
VALUES
(10003, 5, 3, 3, 90.00, 88.00, '组件设计合理，TypeScript 类型较完整。', 'PUBLISHED', '2026-07-06 10:00:00');
