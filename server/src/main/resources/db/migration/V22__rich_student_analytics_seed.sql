-- V22: 学情分析演示数据 — 扩展任务/成绩/跨月趋势/班级雷达对比
SET NAMES utf8mb4;

ALTER TABLE `submission_file`
    MODIFY COLUMN `file_type` VARCHAR(128) NOT NULL COMMENT '文件 MIME 类型（冗余）';

ALTER TABLE `file_storage`
    MODIFY COLUMN `content_type` VARCHAR(128) NOT NULL COMMENT 'MIME 类型';

-- ============================================================
-- 1. 补充实训任务（每学院多 1 条，用于跨月趋势）
-- ============================================================
INSERT INTO `training_task`
(`id`, `course_id`, `standard_id`, `task_name`, `description`, `requirement`, `grading_rule`,
 `end_time`, `submission_type`, `max_ai_analyze_count`, `max_score`, `status`, `publish_time`) VALUES
(6, 1, 1000, 'Redis 缓存集成实训',
 '为业务模块增加 Redis 缓存层。',
 '1. 提交 ZIP 源码\n2. 附压测截图 PNG',
 '完成度：缓存读写；规范性：配置外置；质量：过期策略；表达：说明文档',
 '2026-08-30 23:59:59', 'ZIP_UPLOAD', 3, 100, 'PUBLISHED', '2026-04-01 10:00:00'),
(7, 1, 1000, 'Vue 组件库在线实验',
 '实现 BaseButton 等基础组件。',
 '1. 在线代码提交\n2. 附组件预览 JPG',
 '完成度：组件 API；规范性：TypeScript；质量：可复用性；表达：Props 文档',
 '2026-09-15 23:59:59', 'ONLINE_CODE', 3, 100, 'PUBLISHED', '2026-07-01 10:00:00'),
(8, 2, 1000, '商业计划书撰写',
 '撰写创新创业商业计划书。',
 '1. 提交 PDF + Word\n2. 附 Markdown 摘要',
 '完成度：结构完整；规范性：引用格式；质量：论证深度；表达：图表齐全',
 '2026-09-25 23:59:59', 'ZIP_UPLOAD', 3, 100, 'PUBLISHED', '2026-05-01 10:00:00'),
(9, 3, 1000, '机械制图综合练习',
 '完成零件图与装配图综合练习。',
 '1. 提交 ZIP 图纸包\n2. 附 PNG 截图',
 '完成度：标注齐全；规范性：国标；质量：公差合理；表达：说明清晰',
 '2026-09-20 23:59:59', 'ZIP_UPLOAD', 3, 100, 'PUBLISHED', '2026-05-15 10:00:00'),
(10, 4, 1000, '企业项目终期交付',
 '校企联合实训终期成果交付。',
 '1. 提交 ZIP + PDF 总结\n2. 部署文档齐全',
 '完成度：可部署运行；规范性：接口文档；质量：测试覆盖；表达：总结完整',
 '2026-10-20 23:59:59', 'ZIP_UPLOAD', 3, 100, 'PUBLISHED', '2026-08-01 10:00:00');

-- ============================================================
-- 2. 扩展提交 — 跨月分布 + 班级对比样本
-- ============================================================
INSERT INTO `submission`
(`id`, `training_task_id`, `user_id`, `submit_type`, `summary`, `submit_count`,
 `ai_analyze_used_count`, `current_trial_id`, `confirmed_trial_id`, `confirmed_at`, `submit_time`, `status`) VALUES
-- 计科 · 李明：4月 Redis + 8月 Vue（优等生跨月）
(1101, 6, 3,  'ZIP_UPLOAD',   '李明-Redis 缓存集成', 1, 1, 9100000000000001101, 9100000000000001101, '2026-04-18 15:00:00', '2026-04-18 15:00:00', 'PUBLISHED'),
(1102, 7, 3,  'ONLINE_CODE',  '李明-Vue 组件在线实验', 1, 1, 9100000000000001102, 9100000000000001102, '2026-08-22 16:00:00', '2026-08-22 16:00:00', 'PUBLISHED'),
-- 计科 · 王芳/赵强/孙丽：班级雷达对比
(1103, 6, 4,  'ZIP_UPLOAD',   '王芳-Redis 缓存', 1, 1, 9100000000000001103, 9100000000000001103, '2026-04-25 11:00:00', '2026-04-25 11:00:00', 'PUBLISHED'),
(1104, 7, 9,  'ONLINE_CODE',  '孙丽-Vue 组件实验', 1, 1, 9100000000000001104, 9100000000000001104, '2026-08-10 14:00:00', '2026-08-10 14:00:00', 'PUBLISHED'),
(1105, 6, 8,  'ZIP_UPLOAD',   '赵强-Redis 初版', 1, 1, 9100000000000001105, 9100000000000001105, '2026-05-08 09:00:00', '2026-05-08 09:00:00', 'PUBLISHED'),
-- 经管 · 周杰/吴敏
(1201, 8, 10, 'ZIP_UPLOAD',   '周杰-商业计划书', 1, 1, 9100000000000001201, 9100000000000001201, '2026-05-20 10:00:00', '2026-05-20 10:00:00', 'PUBLISHED'),
(1202, 8, 11, 'ZIP_UPLOAD',   '吴敏-商业计划草稿', 1, 1, 9100000000000001202, NULL, NULL, '2026-05-15 16:00:00', 'AI_READY'),
-- 机械 · 钱雪/韩磊
(1301, 9, 13, 'ZIP_UPLOAD',   '钱雪-制图综合练习', 1, 1, 9100000000000001301, 9100000000000001301, '2026-06-02 11:00:00', '2026-06-02 11:00:00', 'PUBLISHED'),
(1302, 9, 15, 'ZIP_UPLOAD',   '韩磊-制图练习草稿', 1, 1, 9100000000000001302, NULL, NULL, '2026-06-10 09:00:00', 'AI_READY'),
-- 校企 · 陈实训/刘实训
(1401, 10, 17, 'ZIP_UPLOAD',  '陈实训-终期交付', 1, 1, 9100000000000001401, 9100000000000001401, '2026-08-28 10:00:00', '2026-08-28 10:00:00', 'PUBLISHED'),
(1402, 10, 18, 'ZIP_UPLOAD',  '刘实训-终期草稿', 1, 1, 9100000000000001402, NULL, NULL, '2026-08-25 15:00:00', 'AI_READY');

INSERT INTO `submission_trial`
(`id`, `submission_id`, `trial_no`, `submit_type`, `summary`, `submit_time`, `status`, `ai_reference_score`) VALUES
(9100000000000001101, 1101, 1, 'ZIP_UPLOAD',  '李明-Redis 缓存集成', '2026-04-18 15:00:00', 'CONFIRMED', 86.00),
(9100000000000001102, 1102, 1, 'ONLINE_CODE', '李明-Vue 组件在线实验', '2026-08-22 16:00:00', 'CONFIRMED', 92.00),
(9100000000000001103, 1103, 1, 'ZIP_UPLOAD',  '王芳-Redis 缓存', '2026-04-25 11:00:00', 'CONFIRMED', 79.00),
(9100000000000001104, 1104, 1, 'ONLINE_CODE', '孙丽-Vue 组件实验', '2026-08-10 14:00:00', 'CONFIRMED', 85.00),
(9100000000000001105, 1105, 1, 'ZIP_UPLOAD',  '赵强-Redis 初版', '2026-05-08 09:00:00', 'CONFIRMED', 68.00),
(9100000000000001201, 1201, 1, 'ZIP_UPLOAD',  '周杰-商业计划书', '2026-05-20 10:00:00', 'CONFIRMED', 87.00),
(9100000000000001202, 1202, 1, 'ZIP_UPLOAD',  '吴敏-商业计划草稿', '2026-05-15 16:00:00', 'AI_READY', 74.00),
(9100000000000001301, 1301, 1, 'ZIP_UPLOAD',  '钱雪-制图综合练习', '2026-06-02 11:00:00', 'CONFIRMED', 89.00),
(9100000000000001302, 1302, 1, 'ZIP_UPLOAD',  '韩磊-制图练习草稿', '2026-06-10 09:00:00', 'AI_READY', 71.00),
(9100000000000001401, 1401, 1, 'ZIP_UPLOAD',  '陈实训-终期交付', '2026-08-28 10:00:00', 'CONFIRMED', 94.00),
(9100000000000001402, 1402, 1, 'ZIP_UPLOAD',  '刘实训-终期草稿', '2026-08-25 15:00:00', 'AI_READY', 78.00);

INSERT INTO `submission_file`
(`id`, `submission_id`, `file_id`, `file_name`, `file_size`, `file_type`, `sort_order`) VALUES
(50, 1101, 5010, 'RedisCacheService.java', 1295, 'text/plain', 1),
(51, 1101, 5014, 'redis-cache-screenshot.png', 9869, 'image/png', 2),
(52, 1102, 5011, 'BaseButton.vue', 498, 'text/plain', 1),
(53, 1102, 5015, 'vue-component-preview.jpg', 24034, 'image/jpeg', 2),
(54, 1103, 5010, 'RedisCacheService.java', 1295, 'text/plain', 1),
(55, 1201, 5021, '市场调研报告.pdf', 120000, 'application/pdf', 1),
(56, 1201, 5020, '商业计划摘要.md', 1800, 'text/markdown', 2),
(57, 1301, 5024, 'mechanical-design.zip', 748, 'application/zip', 1),
(58, 1401, 5026, 'enterprise-delivery.zip', 601, 'application/zip', 1),
(59, 1401, 5027, '项目阶段总结.pdf', 120000, 'application/pdf', 2);

INSERT INTO `ai_analysis`
(`id`, `submission_id`, `trial_id`, `total_score`, `analysis_status`, `model_provider`, `model_name`, `complete_time`) VALUES
(6401, 1101, 9100000000000001101, 86.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-04-18 15:05:00'),
(6402, 1102, 9100000000000001102, 92.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-08-22 16:05:00'),
(6403, 1103, 9100000000000001103, 79.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-04-25 11:05:00'),
(6404, 1104, 9100000000000001104, 85.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-08-10 14:05:00'),
(6405, 1105, 9100000000000001105, 68.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-05-08 09:05:00'),
(6406, 1201, 9100000000000001201, 87.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-05-20 10:05:00'),
(6407, 1202, 9100000000000001202, 74.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-05-15 16:05:00'),
(6408, 1301, 9100000000000001301, 89.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-06-02 11:05:00'),
(6409, 1401, 9100000000000001401, 94.00, 'COMPLETED', 'DEEPSEEK', 'deepseek-chat', '2026-08-28 10:05:00');

INSERT INTO `ai_analysis_detail`
(`id`, `ai_analysis_id`, `dimension_id`, `agent_type`, `issue_type`, `severity`, `reason`, `suggestion`, `suggest_deduct`, `confidence`, `sort_order`) VALUES
(7201, 6401, 1001, 'CODE', '完成度', 'MINOR', '缓存读写逻辑完整', '补充击穿测试', 1.00, 0.90, 1),
(7202, 6402, 1002, 'CODE', '规范性', 'MINOR', 'Props 类型完整', '补充 emit 文档', 1.00, 0.91, 1),
(7203, 6406, 1004, 'DOC', '表达', 'MINOR', '计划书结构清晰', '增加财务预测', 1.00, 0.88, 1),
(7204, 6408, 1003, 'DESIGN', '质量', 'MINOR', '公差标注规范', '补充表面粗糙度', 1.00, 0.89, 1);

INSERT INTO `teacher_review`
(`id`, `submission_id`, `reviewer_id`, `teacher_comment`, `status`, `review_time`, `publish_time`) VALUES
(8401, 1101, 2,  '缓存方案合理，穿透处理到位。', 'PUBLISHED', '2026-04-20 09:00:00', '2026-04-20 10:00:00'),
(8402, 1102, 2,  '组件抽象良好，类型定义规范。', 'PUBLISHED', '2026-08-24 09:00:00', '2026-08-24 10:00:00'),
(8403, 1103, 2,  '基础功能完成，文档可加强。', 'PUBLISHED', '2026-04-26 09:00:00', '2026-04-26 10:00:00'),
(8404, 1104, 2,  '组件实现正确，预览图清晰。', 'PUBLISHED', '2026-08-12 09:00:00', '2026-08-12 10:00:00'),
(8405, 1105, 2,  '缓存配置有误，已指出改进点。', 'PUBLISHED', '2026-05-10 09:00:00', '2026-05-10 10:00:00'),
(8406, 1201, 6,  '商业逻辑清晰，数据支撑充分。', 'PUBLISHED', '2026-05-22 09:00:00', '2026-05-22 10:00:00'),
(8407, 1301, 7,  '制图规范，标注完整。', 'PUBLISHED', '2026-06-04 09:00:00', '2026-06-04 10:00:00'),
(8408, 1401, 14, '终期交付质量高，部署文档齐全。', 'PUBLISHED', '2026-08-30 09:00:00', '2026-08-30 10:00:00');

INSERT INTO `score_record`
(`id`, `submission_id`, `user_id`, `training_task_id`, `total_score`, `ai_total_score`,
 `score_details`, `teacher_comment`, `status`, `publish_time`) VALUES
(10401, 1101, 3,  6, 86.00, 84.00,
 '[{"dimensionId":1001,"score":25,"remark":""},{"dimensionId":1002,"score":21,"remark":""},{"dimensionId":1003,"score":20,"remark":""},{"dimensionId":1004,"score":20,"remark":""}]',
 '缓存方案合理，穿透处理到位。', 'PUBLISHED', '2026-04-20 10:00:00'),
(10402, 1102, 3,  7, 92.00, 90.00,
 '[{"dimensionId":1001,"score":28,"remark":""},{"dimensionId":1002,"score":24,"remark":""},{"dimensionId":1003,"score":21,"remark":""},{"dimensionId":1004,"score":19,"remark":""}]',
 '组件抽象良好，类型定义规范。', 'PUBLISHED', '2026-08-24 10:00:00'),
(10403, 1103, 4,  6, 79.00, 77.00,
 '[{"dimensionId":1001,"score":22,"remark":""},{"dimensionId":1002,"score":20,"remark":""},{"dimensionId":1003,"score":19,"remark":""},{"dimensionId":1004,"score":18,"remark":""}]',
 '基础功能完成，文档可加强。', 'PUBLISHED', '2026-04-26 10:00:00'),
(10404, 1104, 9,  7, 85.00, 83.00,
 '[{"dimensionId":1001,"score":24,"remark":""},{"dimensionId":1002,"score":22,"remark":""},{"dimensionId":1003,"score":20,"remark":""},{"dimensionId":1004,"score":19,"remark":""}]',
 '组件实现正确，预览图清晰。', 'PUBLISHED', '2026-08-12 10:00:00'),
(10405, 1105, 8,  6, 68.00, 66.00,
 '[{"dimensionId":1001,"score":18,"remark":""},{"dimensionId":1002,"score":16,"remark":""},{"dimensionId":1003,"score":17,"remark":""},{"dimensionId":1004,"score":17,"remark":""}]',
 '缓存配置有误，已指出改进点。', 'PUBLISHED', '2026-05-10 10:00:00'),
(10406, 1201, 10, 8, 87.00, 85.00,
 '[{"dimensionId":1001,"score":26,"remark":""},{"dimensionId":1002,"score":21,"remark":""},{"dimensionId":1003,"score":20,"remark":""},{"dimensionId":1004,"score":20,"remark":""}]',
 '商业逻辑清晰，数据支撑充分。', 'PUBLISHED', '2026-05-22 10:00:00'),
(10407, 1301, 13, 9, 89.00, 87.00,
 '[{"dimensionId":1001,"score":27,"remark":""},{"dimensionId":1002,"score":22,"remark":""},{"dimensionId":1003,"score":21,"remark":""},{"dimensionId":1004,"score":19,"remark":""}]',
 '制图规范，标注完整。', 'PUBLISHED', '2026-06-04 10:00:00'),
(10408, 1401, 17, 10, 94.00, 92.00,
 '[{"dimensionId":1001,"score":29,"remark":""},{"dimensionId":1002,"score":24,"remark":""},{"dimensionId":1003,"score":22,"remark":""},{"dimensionId":1004,"score":19,"remark":""}]',
 '终期交付质量高，部署文档齐全。', 'PUBLISHED', '2026-08-30 10:00:00');

-- 更新 V21 已有成绩的维度明细（若 V21 已写入）
UPDATE `score_record` SET `score_details` = '[{"dimensionId":1001,"score":27,"remark":""},{"dimensionId":1002,"score":22,"remark":""},{"dimensionId":1003,"score":21,"remark":""},{"dimensionId":1004,"score":20,"remark":""}]'
WHERE `id` = 10101 AND (`score_details` IS NULL OR `score_details` = '');
