-- V5: 任务级评分细则字段 + 系统默认评价标准（固定四维度基线）
-- 关联 ADR-012 / 设计文档 2026-07-09-grading-rule-fusion-design.md

-- 1. training_task 新增评分细则字段（存教师 R/S/R/O 原文）
ALTER TABLE `training_task`
    ADD COLUMN `grading_rule` LONGTEXT NULL COMMENT '教师自定义评分细则(R/S/R/O原文)' AFTER `requirement`;

-- 2. 系统默认评价标准（id=1000，规避 V3 已占用的 id=1）
INSERT IGNORE INTO `evaluation_standard`
    (`id`, `standard_name`, `description`, `course_type`, `status`, `is_template`) VALUES
    (1000, '系统通用四维度标准', '平台内置固定评价基线，所有实训任务默认使用', 'GENERAL', 'PUBLISHED', 1);

-- 3. 四维度（id 1001-1004，权重和=100）
INSERT IGNORE INTO `standard_dimension`
    (`id`, `standard_id`, `dim_name`, `dim_description`, `weight`, `max_score`, `sort_order`) VALUES
    (1001, 1000, '代码规范',   '代码风格、命名规范、注释完整性、可读性',       30, 30, 1),
    (1002, 1000, '功能完成度', '需求实现程度、功能完整性、正确性',             30, 30, 2),
    (1003, 1000, '设计质量',   '架构设计、模块划分、算法与性能、扩展性',       20, 20, 3),
    (1004, 1000, '文档完整性', 'README、接口文档、设计说明、提交规范',         20, 20, 4);
