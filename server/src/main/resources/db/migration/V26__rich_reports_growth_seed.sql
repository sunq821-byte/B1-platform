-- V26: 丰富教师报表与学情分析演示数据（teacher1 多课程/多班/跨学期）
SET NAMES utf8mb4;

-- ============================================================
-- 1. 新增班级与课程（张教授 teacher1）
-- ============================================================
INSERT INTO `class` (`id`, `class_code`, `class_name`, `grade`, `major`, `department`, `student_count`) VALUES
(5, 'SE2023-02', '2023级软件工程2班', '2023', '软件工程', '计算机学院', 30)
ON DUPLICATE KEY UPDATE
    `class_name` = VALUES(`class_name`),
    `department` = VALUES(`department`);

INSERT INTO `course` (`id`, `course_code`, `course_name`, `semester`, `credits`, `description`, `status`) VALUES
(5, 'CS302', 'Web 前端工程实践', '2025-2026-2', 3, 'Vue3 组件化开发与工程化实践。', 'ACTIVE'),
(6, 'CS301-H', 'Java Web 开发', '2024-2025-2', 4, '上学期 Java Web 全栈实训归档数据。', 'ACTIVE')
ON DUPLICATE KEY UPDATE
    `course_name` = VALUES(`course_name`),
    `semester` = VALUES(`semester`);

INSERT INTO `course_teacher` (`id`, `course_id`, `user_id`) VALUES
(5, 5, 2),
(6, 6, 2)
ON DUPLICATE KEY UPDATE `course_id` = VALUES(`course_id`);

INSERT INTO `course_class` (`id`, `course_id`, `class_id`) VALUES
(5, 5, 5),
(6, 6, 1)
ON DUPLICATE KEY UPDATE `class_id` = VALUES(`class_id`);

INSERT INTO `course_student` (`id`, `course_id`, `user_id`) VALUES
(13, 5, 3),
(14, 5, 4),
(15, 5, 8),
(16, 5, 9),
(17, 6, 3),
(18, 6, 4)
ON DUPLICATE KEY UPDATE `course_id` = VALUES(`course_id`);

-- ============================================================
-- 2. 新增实训任务
-- ============================================================
INSERT INTO `training_task`
(`id`, `course_id`, `standard_id`, `task_name`, `description`, `requirement`, `grading_rule`,
 `end_time`, `submission_type`, `max_ai_analyze_count`, `max_score`, `status`, `publish_time`) VALUES
(11, 5, 1000, 'Vue 管理后台页面开发',
 '完成登录页、列表页与表单页三屏联动。',
 '1. Vue3 + TypeScript\n2. 在线代码或 ZIP\n3. 附界面截图',
 '完成度：三屏联动；规范性：组件拆分；质量：交互体验；表达：README',
 '2026-09-20 23:59:59', 'ONLINE_CODE', 3, 100, 'PUBLISHED', '2026-06-01 10:00:00'),
(12, 6, 1000, 'Spring Boot 图书管理系统（归档）',
 '上学期图书管理系统综合实训归档任务。',
 '1. 提交源码 ZIP\n2. 附实训报告',
 '完成度：CRUD；规范性：分层；质量：事务；表达：报告',
 '2025-12-30 23:59:59', 'ZIP_UPLOAD', 3, 100, 'PUBLISHED', '2025-09-01 10:00:00')
ON DUPLICATE KEY UPDATE `task_name` = VALUES(`task_name`);

-- ============================================================
-- 3. 提交与试交（跨学期 + 多班样本）
-- ============================================================
INSERT INTO `submission`
(`id`, `training_task_id`, `user_id`, `submit_type`, `summary`, `submit_count`,
 `ai_analyze_used_count`, `current_trial_id`, `confirmed_trial_id`, `confirmed_at`, `submit_time`, `status`) VALUES
(1501, 11, 3,  'ONLINE_CODE', '李明-Vue 管理后台终稿', 1, 1, 9100000000000001501, 9100000000000001501, '2026-07-12 10:00:00', '2026-07-12 10:00:00', 'PUBLISHED'),
(1502, 11, 4,  'ONLINE_CODE', '王芳-Vue 管理后台', 1, 1, 9100000000000001502, 9100000000000001502, '2026-07-15 14:00:00', '2026-07-15 14:00:00', 'PUBLISHED'),
(1503, 11, 8,  'ONLINE_CODE', '赵强-Vue 管理后台草稿', 1, 1, 9100000000000001503, NULL, NULL, '2026-07-18 09:00:00', 'AI_READY'),
(1504, 11, 9,  'ONLINE_CODE', '孙丽-Vue 管理后台', 1, 1, 9100000000000001504, 9100000000000001504, '2026-07-20 16:00:00', '2026-07-20 16:00:00', 'PUBLISHED'),
(1505, 12, 3,  'ZIP_UPLOAD',  '李明-上学期图书管理归档', 1, 1, 9100000000000001505, 9100000000000001505, '2025-11-20 10:00:00', '2025-11-20 10:00:00', 'PUBLISHED'),
(1506, 12, 4,  'ZIP_UPLOAD',  '王芳-上学期图书管理归档', 1, 1, 9100000000000001506, 9100000000000001506, '2025-11-22 11:00:00', '2025-11-22 11:00:00', 'PUBLISHED')
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);

INSERT INTO `submission_trial`
(`id`, `submission_id`, `trial_no`, `submit_type`, `summary`, `submit_time`, `status`, `ai_reference_score`) VALUES
(9100000000000001501, 1501, 1, 'ONLINE_CODE', '李明-Vue 管理后台终稿', '2026-07-12 10:00:00', 'CONFIRMED', 91.00),
(9100000000000001502, 1502, 1, 'ONLINE_CODE', '王芳-Vue 管理后台', '2026-07-15 14:00:00', 'CONFIRMED', 88.00),
(9100000000000001503, 1503, 1, 'ONLINE_CODE', '赵强-Vue 管理后台草稿', '2026-07-18 09:00:00', 'AI_READY', 73.00),
(9100000000000001504, 1504, 1, 'ONLINE_CODE', '孙丽-Vue 管理后台', '2026-07-20 16:00:00', 'CONFIRMED', 84.00),
(9100000000000001505, 1505, 1, 'ZIP_UPLOAD',  '李明-上学期图书管理归档', '2025-11-20 10:00:00', 'CONFIRMED', 82.00),
(9100000000000001506, 1506, 1, 'ZIP_UPLOAD',  '王芳-上学期图书管理归档', '2025-11-22 11:00:00', 'CONFIRMED', 79.00)
ON DUPLICATE KEY UPDATE `ai_reference_score` = VALUES(`ai_reference_score`);

INSERT INTO `submission_file`
(`id`, `submission_id`, `file_id`, `file_name`, `file_size`, `file_type`, `sort_order`) VALUES
(60, 1501, 5011, 'BaseButton.vue', 498, 'text/plain', 1),
(61, 1501, 5015, 'vue-component-preview.jpg', 24034, 'image/jpeg', 2),
(62, 1502, 5011, 'BaseButton.vue', 498, 'text/plain', 1),
(63, 1504, 5011, 'BaseButton.vue', 498, 'text/plain', 1),
(64, 1505, 5002, 'book-mgr-source.zip', 601, 'application/zip', 1),
(65, 1506, 5002, 'book-mgr-source.zip', 601, 'application/zip', 1)
ON DUPLICATE KEY UPDATE `file_name` = VALUES(`file_name`);

INSERT INTO `teacher_review`
(`id`, `submission_id`, `reviewer_id`, `teacher_comment`, `status`, `review_time`, `publish_time`) VALUES
(8501, 1501, 2, '页面结构清晰，组件拆分合理。', 'PUBLISHED', '2026-07-14 09:00:00', '2026-07-14 10:00:00'),
(8502, 1502, 2, '交互完整，样式规范。', 'PUBLISHED', '2026-07-17 09:00:00', '2026-07-17 10:00:00'),
(8503, 1504, 2, '功能达标，文档可再完善。', 'PUBLISHED', '2026-07-22 09:00:00', '2026-07-22 10:00:00'),
(8504, 1505, 2, '上学期归档：整体完成度良好。', 'PUBLISHED', '2025-11-25 09:00:00', '2025-11-25 10:00:00'),
(8505, 1506, 2, '上学期归档：借阅模块待加强。', 'PUBLISHED', '2025-11-28 09:00:00', '2025-11-28 10:00:00')
ON DUPLICATE KEY UPDATE `teacher_comment` = VALUES(`teacher_comment`);

INSERT INTO `score_record`
(`id`, `submission_id`, `user_id`, `training_task_id`, `total_score`, `ai_total_score`,
 `score_details`, `teacher_comment`, `status`, `publish_time`) VALUES
(10501, 1501, 3, 11, 91.00, 89.00,
 '[{"dimensionId":1001,"score":27,"remark":""},{"dimensionId":1002,"score":23,"remark":""},{"dimensionId":1003,"score":22,"remark":""},{"dimensionId":1004,"score":19,"remark":""}]',
 '页面结构清晰，组件拆分合理。', 'PUBLISHED', '2026-07-14 10:00:00'),
(10502, 1502, 4, 11, 88.00, 86.00,
 '[{"dimensionId":1001,"score":26,"remark":""},{"dimensionId":1002,"score":22,"remark":""},{"dimensionId":1003,"score":21,"remark":""},{"dimensionId":1004,"score":19,"remark":""}]',
 '交互完整，样式规范。', 'PUBLISHED', '2026-07-17 10:00:00'),
(10503, 1504, 9, 11, 84.00, 82.00,
 '[{"dimensionId":1001,"score":24,"remark":""},{"dimensionId":1002,"score":21,"remark":""},{"dimensionId":1003,"score":20,"remark":""},{"dimensionId":1004,"score":19,"remark":""}]',
 '功能达标，文档可再完善。', 'PUBLISHED', '2026-07-22 10:00:00'),
(10504, 1505, 3, 12, 82.00, 80.00,
 '[{"dimensionId":1001,"score":24,"remark":""},{"dimensionId":1002,"score":20,"remark":""},{"dimensionId":1003,"score":20,"remark":""},{"dimensionId":1004,"score":18,"remark":""}]',
 '上学期归档：整体完成度良好。', 'PUBLISHED', '2025-11-25 10:00:00'),
(10505, 1506, 4, 12, 79.00, 77.00,
 '[{"dimensionId":1001,"score":22,"remark":""},{"dimensionId":1002,"score":20,"remark":""},{"dimensionId":1003,"score":19,"remark":""},{"dimensionId":1004,"score":18,"remark":""}]',
 '上学期归档：借阅模块待加强。', 'PUBLISHED', '2025-11-28 10:00:00')
ON DUPLICATE KEY UPDATE `total_score` = VALUES(`total_score`);

-- 补充近月提交，使 student1 月度趋势更饱满
INSERT INTO `submission`
(`id`, `training_task_id`, `user_id`, `submit_type`, `summary`, `submit_count`,
 `ai_analyze_used_count`, `current_trial_id`, `confirmed_trial_id`, `confirmed_at`, `submit_time`, `status`) VALUES
(1507, 2, 4, 'ZIP_UPLOAD', '王芳-Servlet 终稿', 1, 1, 9100000000000001507, 9100000000000001507, '2026-06-05 10:00:00', '2026-06-05 10:00:00', 'PUBLISHED')
ON DUPLICATE KEY UPDATE `submit_time` = VALUES(`submit_time`);

INSERT INTO `submission_trial`
(`id`, `submission_id`, `trial_no`, `submit_type`, `summary`, `submit_time`, `status`, `ai_reference_score`) VALUES
(9100000000000001507, 1507, 1, 'ZIP_UPLOAD', '王芳-Servlet 终稿', '2026-06-05 10:00:00', 'CONFIRMED', 76.00)
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);

INSERT INTO `teacher_review`
(`id`, `submission_id`, `reviewer_id`, `teacher_comment`, `status`, `review_time`, `publish_time`) VALUES
(8506, 1507, 2, '会话流程完整，Filter 使用规范。', 'PUBLISHED', '2026-06-07 09:00:00', '2026-06-07 10:00:00')
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);

INSERT INTO `score_record`
(`id`, `submission_id`, `user_id`, `training_task_id`, `total_score`, `ai_total_score`,
 `score_details`, `teacher_comment`, `status`, `publish_time`) VALUES
(10506, 1507, 4, 2, 76.00, 74.00,
 '[{"dimensionId":1001,"score":21,"remark":""},{"dimensionId":1002,"score":19,"remark":""},{"dimensionId":1003,"score":18,"remark":""},{"dimensionId":1004,"score":18,"remark":""}]',
 '会话流程完整，Filter 使用规范。', 'PUBLISHED', '2026-06-07 10:00:00')
ON DUPLICATE KEY UPDATE `total_score` = VALUES(`total_score`);
