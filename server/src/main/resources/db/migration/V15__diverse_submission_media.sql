-- V15: 丰富演示提交 — 在线代码(ONLINE_CODE) + PNG/JPG 截图附件
SET NAMES utf8mb4;

-- submission_trial.submit_type 仍为 VARCHAR(8)（V8），需与 submission 表一致以支持 ONLINE_CODE / ZIP_UPLOAD
ALTER TABLE `submission_trial`
    MODIFY COLUMN `submit_type` VARCHAR(20) NOT NULL COMMENT '提交方式：ZIP_UPLOAD, GIT_URL, ONLINE_CODE, ZIP_ONLY';

-- ============================================================
-- 1. 文件存储元数据（字节由 MaterialSeedRunner 上传 MinIO）
-- ============================================================
INSERT IGNORE INTO `file_storage`
(`id`, `bucket`, `object_key`, `original_name`, `content_type`, `file_size`, `access_url`) VALUES
(5009, 'submissions', 'material/seed/LoginServlet.java',           'LoginServlet.java',           'text/plain',     1300, '/api/v1/files/5009/download'),
(5010, 'submissions', 'material/seed/RedisCacheService.java',      'RedisCacheService.java',      'text/plain',     1295, '/api/v1/files/5010/download'),
(5011, 'submissions', 'material/seed/BaseButton.vue',              'BaseButton.vue',              'text/plain',      498, '/api/v1/files/5011/download'),
(5012, 'submissions', 'material/seed/LinkedListDemo.java',         'LinkedListDemo.java',         'text/plain',      837, '/api/v1/files/5012/download'),
(5013, 'submissions', 'material/seed/book-mgr-ui.png',             'book-mgr-ui.png',             'image/png',      8606, '/api/v1/files/5013/download'),
(5014, 'submissions', 'material/seed/redis-cache-screenshot.png',  'redis-cache-screenshot.png',  'image/png',      9869, '/api/v1/files/5014/download'),
(5015, 'submissions', 'material/seed/vue-component-preview.jpg',     'vue-component-preview.jpg',   'image/jpeg',    24034, '/api/v1/files/5015/download'),
(5016, 'submissions', 'material/seed/servlet-session-run.jpg',     'servlet-session-run.jpg',     'image/jpeg',    23453, '/api/v1/files/5016/download');

-- ============================================================
-- 2. 在线代码提交 (ONLINE_CODE)
-- ============================================================

-- student4 任务2：Servlet 在线代码，待确认
UPDATE `submission` SET
    `submit_type` = 'ONLINE_CODE',
    `summary` = 'Servlet 会话管理在线代码提交'
WHERE `id` = 102;

UPDATE `submission_trial` SET
    `submit_type` = 'ONLINE_CODE',
    `summary` = '在线代码：LoginServlet 会话实现'
WHERE `id` = 8900000000000000102;

DELETE FROM `submission_file` WHERE `submission_id` = 102;
INSERT IGNORE INTO `submission_file`
(`id`, `submission_id`, `file_id`, `file_name`, `file_size`, `file_type`, `sort_order`) VALUES
(11, 102, 5009, 'LoginServlet.java', 1300, 'text/plain', 1);

-- student5 任务1：Redis 相关在线代码（第三版试交）
UPDATE `submission` SET
    `submit_type` = 'ONLINE_CODE',
    `summary` = '图书管理 + Redis 缓存在线代码'
WHERE `id` = 103;

UPDATE `submission_trial` SET
    `submit_type` = 'ONLINE_CODE',
    `summary` = '在线代码：RedisCacheService 缓存层'
WHERE `id` = 8900000000000000109;

DELETE FROM `submission_file` WHERE `submission_id` = 103;
INSERT IGNORE INTO `submission_file`
(`id`, `submission_id`, `file_id`, `file_name`, `file_size`, `file_type`, `sort_order`) VALUES
(19, 103, 5010, 'RedisCacheService.java', 1295, 'text/plain', 1);

-- student1 任务3：Vue 组件在线代码，待确认
UPDATE `submission` SET
    `submit_type` = 'ONLINE_CODE',
    `summary` = 'Vue3 BaseButton 在线代码提交'
WHERE `id` = 5;

UPDATE `submission_trial` SET
    `submit_type` = 'ONLINE_CODE',
    `summary` = '在线代码：BaseButton 组件实现'
WHERE `id` = 8900000000000000005;

INSERT IGNORE INTO `submission_file`
(`id`, `submission_id`, `file_id`, `file_name`, `file_size`, `file_type`, `sort_order`) VALUES
(25, 5, 5011, 'BaseButton.vue', 498, 'text/plain', 1);

-- student6 任务13：链表实验在线代码，待确认
INSERT INTO `submission`
(`id`, `training_task_id`, `user_id`, `submit_type`, `summary`, `submit_count`,
 `ai_analyze_used_count`, `current_trial_id`, `confirmed_trial_id`, `confirmed_at`, `submit_time`, `status`) VALUES
(107, 13, 11, 'ONLINE_CODE', '单链表实现在线代码', 1,
 1, 8900000000000000110, NULL, NULL, '2026-08-26 15:00:00', 'AI_READY')
ON DUPLICATE KEY UPDATE
    `submit_type` = VALUES(`submit_type`),
    `status` = VALUES(`status`),
    `summary` = VALUES(`summary`);

INSERT IGNORE INTO `submission_trial`
(`id`, `submission_id`, `trial_no`, `submit_type`, `summary`, `submit_time`, `status`, `ai_reference_score`) VALUES
(8900000000000000110, 107, 1, 'ONLINE_CODE', '在线代码：LinkedListDemo', '2026-08-26 15:00:00', 'AI_READY', 79.00);

INSERT IGNORE INTO `submission_file`
(`id`, `submission_id`, `file_id`, `file_name`, `file_size`, `file_type`, `sort_order`) VALUES
(26, 107, 5012, 'LinkedListDemo.java', 837, 'text/plain', 1);

INSERT IGNORE INTO `ai_analysis`
(`id`, `submission_id`, `trial_id`, `total_score`, `analysis_status`, `model_provider`, `model_name`, `complete_time`) VALUES
(6012, 107, 8900000000000000110, 79.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-08-26 15:05:00');

-- ============================================================
-- 3. PNG / JPG 截图附件（ZIP 提交 + 运行截图）
-- ============================================================
INSERT IGNORE INTO `submission_file`
(`id`, `submission_id`, `file_id`, `file_name`, `file_size`, `file_type`, `sort_order`) VALUES
(20, 2,  5013, 'book-mgr-ui.png',            8606,  'image/png',  3),
(21, 3,  5016, 'servlet-session-run.jpg',    23453, 'image/jpeg', 2),
(22, 4,  5015, 'vue-component-preview.jpg',  24034, 'image/jpeg', 2),
(23, 100, 5013, 'book-mgr-ui.png',           8606,  'image/png',  3),
(24, 105, 5013, 'book-mgr-ui.png',           8606,  'image/png',  3),
(27, 104, 5014, 'redis-cache-screenshot.png', 9869,  'image/png',  2);

-- 同步附件元数据与 file_storage
UPDATE `submission_file` sf
JOIN `file_storage` fs ON sf.`file_id` = fs.`id`
SET sf.`file_name` = fs.`original_name`,
    sf.`file_type` = fs.`content_type`,
    sf.`file_size` = fs.`file_size`
WHERE sf.`file_id` IN (5009, 5010, 5011, 5012, 5013, 5014, 5015, 5016);
