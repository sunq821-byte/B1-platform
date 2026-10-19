-- V27: 补充多学期归档课程与成绩，使学院报告学期趋势图更饱满
SET NAMES utf8mb4;

INSERT INTO `course` (`id`, `course_code`, `course_name`, `semester`, `credits`, `description`, `status`) VALUES
(7,  'CS301-23', 'Java Web 开发',     '2023-2024-2', 4, '2023-2024 学年第二学期归档。', 'ACTIVE'),
(8,  'CS301-24', 'Java Web 开发',     '2024-2025-1', 4, '2024-2025 学年第一学期归档。', 'ACTIVE'),
(9,  'CS302-24', 'Web 前端工程实践',  '2024-2025-2', 3, '2024-2025 学年第二学期 2 班归档。', 'ACTIVE')
ON DUPLICATE KEY UPDATE `semester` = VALUES(`semester`);

INSERT INTO `course_teacher` (`id`, `course_id`, `user_id`) VALUES
(7, 7, 2), (8, 8, 2), (9, 9, 2)
ON DUPLICATE KEY UPDATE `course_id` = VALUES(`course_id`);

INSERT INTO `course_class` (`id`, `course_id`, `class_id`) VALUES
(7, 7, 1), (8, 8, 1), (9, 9, 5)
ON DUPLICATE KEY UPDATE `class_id` = VALUES(`class_id`);

INSERT INTO `course_student` (`id`, `course_id`, `user_id`) VALUES
(19, 7, 3), (20, 7, 4), (21, 7, 8), (22, 7, 9),
(23, 8, 3), (24, 8, 4), (25, 8, 8), (26, 8, 9),
(27, 9, 4), (28, 9, 8), (29, 9, 9)
ON DUPLICATE KEY UPDATE `course_id` = VALUES(`course_id`);

INSERT INTO `training_task`
(`id`, `course_id`, `standard_id`, `task_name`, `description`, `requirement`, `grading_rule`,
 `end_time`, `submission_type`, `max_ai_analyze_count`, `max_score`, `status`, `publish_time`) VALUES
(13, 7, 1000, 'Java Web 综合实训（2023-2024-2）',
 '2023-2024 学年 Java Web 归档任务。', '提交 ZIP + 报告', '四维度评分', '2024-06-30 23:59:59', 'ZIP_UPLOAD', 3, 100, 'PUBLISHED', '2024-03-01 10:00:00'),
(14, 8, 1000, 'Java Web 综合实训（2024-2025-1）',
 '2024-2025 学年第一学期归档任务。', '提交 ZIP + 报告', '四维度评分', '2025-01-15 23:59:59', 'ZIP_UPLOAD', 3, 100, 'PUBLISHED', '2024-09-01 10:00:00'),
(15, 9, 1000, 'Vue 前端基础实训（2024-2025-2）',
 '2024-2025 学年 2 班前端归档。', '在线代码 + 截图', '四维度评分', '2025-06-30 23:59:59', 'ONLINE_CODE', 3, 100, 'PUBLISHED', '2025-03-01 10:00:00')
ON DUPLICATE KEY UPDATE `task_name` = VALUES(`task_name`);

INSERT INTO `submission`
(`id`, `training_task_id`, `user_id`, `submit_type`, `summary`, `submit_count`,
 `ai_analyze_used_count`, `current_trial_id`, `confirmed_trial_id`, `confirmed_at`, `submit_time`, `status`) VALUES
(1601, 13, 3, 'ZIP_UPLOAD', '李明-2023JavaWeb归档', 1, 1, 9100000000000001601, 9100000000000001601, '2024-05-20 10:00:00', '2024-05-20 10:00:00', 'PUBLISHED'),
(1602, 13, 4, 'ZIP_UPLOAD', '王芳-2023JavaWeb归档', 1, 1, 9100000000000001602, 9100000000000001602, '2024-05-22 11:00:00', '2024-05-22 11:00:00', 'PUBLISHED'),
(1603, 13, 8, 'ZIP_UPLOAD', '赵强-2023JavaWeb归档', 1, 1, 9100000000000001603, 9100000000000001603, '2024-05-25 09:00:00', '2024-05-25 09:00:00', 'PUBLISHED'),
(1604, 13, 9, 'ZIP_UPLOAD', '孙丽-2023JavaWeb归档', 1, 1, 9100000000000001604, 9100000000000001604, '2024-05-28 14:00:00', '2024-05-28 14:00:00', 'PUBLISHED'),
(1605, 14, 3, 'ZIP_UPLOAD', '李明-2024上JavaWeb归档', 1, 1, 9100000000000001605, 9100000000000001605, '2024-12-10 10:00:00', '2024-12-10 10:00:00', 'PUBLISHED'),
(1606, 14, 4, 'ZIP_UPLOAD', '王芳-2024上JavaWeb归档', 1, 1, 9100000000000001606, 9100000000000001606, '2024-12-12 11:00:00', '2024-12-12 11:00:00', 'PUBLISHED'),
(1607, 14, 8, 'ZIP_UPLOAD', '赵强-2024上JavaWeb归档', 1, 1, 9100000000000001607, 9100000000000001607, '2024-12-15 09:00:00', '2024-12-15 09:00:00', 'PUBLISHED'),
(1608, 14, 9, 'ZIP_UPLOAD', '孙丽-2024上JavaWeb归档', 1, 1, 9100000000000001608, 9100000000000001608, '2024-12-18 14:00:00', '2024-12-18 14:00:00', 'PUBLISHED'),
(1609, 15, 4, 'ONLINE_CODE', '王芳-2024下前端归档', 1, 1, 9100000000000001609, 9100000000000001609, '2025-05-20 10:00:00', '2025-05-20 10:00:00', 'PUBLISHED'),
(1610, 15, 8, 'ONLINE_CODE', '赵强-2024下前端归档', 1, 1, 9100000000000001610, 9100000000000001610, '2025-05-22 11:00:00', '2025-05-22 11:00:00', 'PUBLISHED'),
(1611, 15, 9, 'ONLINE_CODE', '孙丽-2024下前端归档', 1, 1, 9100000000000001611, 9100000000000001611, '2025-05-25 14:00:00', '2025-05-25 14:00:00', 'PUBLISHED')
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);

INSERT INTO `submission_trial`
(`id`, `submission_id`, `trial_no`, `submit_type`, `summary`, `submit_time`, `status`, `ai_reference_score`) VALUES
(9100000000000001601, 1601, 1, 'ZIP_UPLOAD', '李明-2023JavaWeb', '2024-05-20 10:00:00', 'CONFIRMED', 76.00),
(9100000000000001602, 1602, 1, 'ZIP_UPLOAD', '王芳-2023JavaWeb', '2024-05-22 11:00:00', 'CONFIRMED', 74.00),
(9100000000000001603, 1603, 1, 'ZIP_UPLOAD', '赵强-2023JavaWeb', '2024-05-25 09:00:00', 'CONFIRMED', 68.00),
(9100000000000001604, 1604, 1, 'ZIP_UPLOAD', '孙丽-2023JavaWeb', '2024-05-28 14:00:00', 'CONFIRMED', 72.00),
(9100000000000001605, 1605, 1, 'ZIP_UPLOAD', '李明-2024上', '2024-12-10 10:00:00', 'CONFIRMED', 81.00),
(9100000000000001606, 1606, 1, 'ZIP_UPLOAD', '王芳-2024上', '2024-12-12 11:00:00', 'CONFIRMED', 79.00),
(9100000000000001607, 1607, 1, 'ZIP_UPLOAD', '赵强-2024上', '2024-12-15 09:00:00', 'CONFIRMED', 75.00),
(9100000000000001608, 1608, 1, 'ZIP_UPLOAD', '孙丽-2024上', '2024-12-18 14:00:00', 'CONFIRMED', 78.00),
(9100000000000001609, 1609, 1, 'ONLINE_CODE', '王芳-2024下前端', '2025-05-20 10:00:00', 'CONFIRMED', 83.00),
(9100000000000001610, 1610, 1, 'ONLINE_CODE', '赵强-2024下前端', '2025-05-22 11:00:00', 'CONFIRMED', 80.00),
(9100000000000001611, 1611, 1, 'ONLINE_CODE', '孙丽-2024下前端', '2025-05-25 14:00:00', 'CONFIRMED', 85.00)
ON DUPLICATE KEY UPDATE `ai_reference_score` = VALUES(`ai_reference_score`);

INSERT INTO `teacher_review`
(`id`, `submission_id`, `reviewer_id`, `teacher_comment`, `status`, `review_time`, `publish_time`) VALUES
(8601, 1601, 2, '2023 归档：基础扎实。', 'PUBLISHED', '2024-05-22 09:00:00', '2024-05-22 10:00:00'),
(8602, 1602, 2, '2023 归档：文档规范。', 'PUBLISHED', '2024-05-24 09:00:00', '2024-05-24 10:00:00'),
(8603, 1603, 2, '2023 归档：功能待完善。', 'PUBLISHED', '2024-05-27 09:00:00', '2024-05-27 10:00:00'),
(8604, 1604, 2, '2023 归档：整体良好。', 'PUBLISHED', '2024-05-30 09:00:00', '2024-05-30 10:00:00'),
(8605, 1605, 2, '2024 上归档：进步明显。', 'PUBLISHED', '2024-12-14 09:00:00', '2024-12-14 10:00:00'),
(8606, 1606, 2, '2024 上归档：表现稳定。', 'PUBLISHED', '2024-12-16 09:00:00', '2024-12-16 10:00:00'),
(8607, 1607, 2, '2024 上归档：需加强测试。', 'PUBLISHED', '2024-12-19 09:00:00', '2024-12-19 10:00:00'),
(8608, 1608, 2, '2024 上归档：代码质量提升。', 'PUBLISHED', '2024-12-21 09:00:00', '2024-12-21 10:00:00'),
(8609, 1609, 2, '2024 下 2 班前端归档。', 'PUBLISHED', '2025-05-22 09:00:00', '2025-05-22 10:00:00'),
(8610, 1610, 2, '2024 下 2 班前端归档。', 'PUBLISHED', '2025-05-24 09:00:00', '2025-05-24 10:00:00'),
(8611, 1611, 2, '2024 下 2 班前端归档。', 'PUBLISHED', '2025-05-27 09:00:00', '2025-05-27 10:00:00')
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);

INSERT INTO `score_record`
(`id`, `submission_id`, `user_id`, `training_task_id`, `total_score`, `ai_total_score`,
 `score_details`, `teacher_comment`, `status`, `publish_time`) VALUES
(10601, 1601, 3, 13, 78.0, 76.0, '[{"dimensionId":1001,"score":24},{"dimensionId":1002,"score":20},{"dimensionId":1003,"score":18},{"dimensionId":1004,"score":16}]', '2023 归档', 'PUBLISHED', '2024-05-22 10:00:00'),
(10602, 1602, 4, 13, 76.0, 74.0, '[{"dimensionId":1001,"score":23},{"dimensionId":1002,"score":19},{"dimensionId":1003,"score":18},{"dimensionId":1004,"score":16}]', '2023 归档', 'PUBLISHED', '2024-05-24 10:00:00'),
(10603, 1603, 8, 13, 70.0, 68.0, '[{"dimensionId":1001,"score":20},{"dimensionId":1002,"score":18},{"dimensionId":1003,"score":17},{"dimensionId":1004,"score":15}]', '2023 归档', 'PUBLISHED', '2024-05-27 10:00:00'),
(10604, 1604, 9, 13, 74.0, 72.0, '[{"dimensionId":1001,"score":22},{"dimensionId":1002,"score":19},{"dimensionId":1003,"score":17},{"dimensionId":1004,"score":16}]', '2023 归档', 'PUBLISHED', '2024-05-30 10:00:00'),
(10605, 1605, 3, 14, 82.0, 81.0, '[{"dimensionId":1001,"score":25},{"dimensionId":1002,"score":21},{"dimensionId":1003,"score":19},{"dimensionId":1004,"score":17}]', '2024 上归档', 'PUBLISHED', '2024-12-14 10:00:00'),
(10606, 1606, 4, 14, 80.0, 79.0, '[{"dimensionId":1001,"score":24},{"dimensionId":1002,"score":20},{"dimensionId":1003,"score":19},{"dimensionId":1004,"score":17}]', '2024 上归档', 'PUBLISHED', '2024-12-16 10:00:00'),
(10607, 1607, 8, 14, 76.0, 75.0, '[{"dimensionId":1001,"score":22},{"dimensionId":1002,"score":19},{"dimensionId":1003,"score":18},{"dimensionId":1004,"score":17}]', '2024 上归档', 'PUBLISHED', '2024-12-19 10:00:00'),
(10608, 1608, 9, 14, 79.0, 78.0, '[{"dimensionId":1001,"score":23},{"dimensionId":1002,"score":20},{"dimensionId":1003,"score":19},{"dimensionId":1004,"score":17}]', '2024 上归档', 'PUBLISHED', '2024-12-21 10:00:00'),
(10609, 1609, 4, 15, 84.0, 83.0, '[{"dimensionId":1001,"score":25},{"dimensionId":1002,"score":21},{"dimensionId":1003,"score":20},{"dimensionId":1004,"score":18}]', '2024 下 2 班', 'PUBLISHED', '2025-05-22 10:00:00'),
(10610, 1610, 8, 15, 81.0, 80.0, '[{"dimensionId":1001,"score":24},{"dimensionId":1002,"score":20},{"dimensionId":1003,"score":19},{"dimensionId":1004,"score":18}]', '2024 下 2 班', 'PUBLISHED', '2025-05-24 10:00:00'),
(10611, 1611, 9, 15, 86.0, 85.0, '[{"dimensionId":1001,"score":26},{"dimensionId":1002,"score":22},{"dimensionId":1003,"score":20},{"dimensionId":1004,"score":18}]', '2024 下 2 班', 'PUBLISHED', '2025-05-27 10:00:00')
ON DUPLICATE KEY UPDATE `total_score` = VALUES(`total_score`);
