-- V8: 方案 A 试交+确认 — submission_trial、聚合字段、AI 分析关联试交
-- 幂等设计：支持失败后重跑；试交 ID 使用独立序号段，避免 Snowflake ID * 1000 溢出

SET @db = DATABASE();

-- 1. training_task: max_submit_count → max_ai_analyze_count
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'training_task' AND COLUMN_NAME = 'max_submit_count') > 0,
    'ALTER TABLE `training_task` CHANGE COLUMN `max_submit_count` `max_ai_analyze_count` INT NOT NULL DEFAULT 3 COMMENT ''每学生每任务 AI 分析次数上限（教师可配，默认 3）''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. submission: 方案 A 聚合字段
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'submission' AND COLUMN_NAME = 'ai_analyze_used_count') = 0,
    'ALTER TABLE `submission`
        ADD COLUMN `ai_analyze_used_count` INT NOT NULL DEFAULT 0 COMMENT ''已消耗 AI 分析次数'' AFTER `submit_count`,
        ADD COLUMN `current_trial_id` BIGINT DEFAULT NULL COMMENT ''当前最新试交 ID'' AFTER `ai_analyze_used_count`,
        ADD COLUMN `confirmed_trial_id` BIGINT DEFAULT NULL COMMENT ''确认递交的试交 ID'' AFTER `current_trial_id`,
        ADD COLUMN `confirmed_at` DATETIME DEFAULT NULL COMMENT ''确认提交时间'' AFTER `confirmed_trial_id`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. submission_trial 试交明细
CREATE TABLE IF NOT EXISTS `submission_trial` (
    `id`                 BIGINT       NOT NULL COMMENT '主键 (Snowflake)',
    `submission_id`      BIGINT       NOT NULL COMMENT '所属提交聚合 ID',
    `trial_no`           INT          NOT NULL COMMENT '第几次试交（从 1 递增）',
    `submit_type`        VARCHAR(8)   NOT NULL COMMENT '提交方式：ZIP, GIT, CODE, TEXT',
    `git_url`            VARCHAR(512) DEFAULT NULL COMMENT 'Git 仓库 URL',
    `git_branch`         VARCHAR(128) DEFAULT NULL COMMENT 'Git 分支',
    `git_commit_id`      VARCHAR(64)  DEFAULT NULL COMMENT '克隆时 Commit ID',
    `summary`            TEXT         DEFAULT NULL COMMENT '试交备注',
    `submit_time`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '试交时间',
    `status`             VARCHAR(16)  NOT NULL DEFAULT 'ANALYZING' COMMENT 'ANALYZING, AI_READY, CONFIRMED, SUPERSEDED',
    `ai_reference_score` DECIMAL(5,2) DEFAULT NULL COMMENT 'AI 参考分（非最终成绩）',
    `create_time`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_submission_trial_no` (`submission_id`, `trial_no`),
    INDEX `idx_submission_id` (`submission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='试交记录';

-- 4. 迁移既有 submission → submission_trial（独立 ID 段，不与 Snowflake 相乘）
INSERT INTO `submission_trial`
    (`id`, `submission_id`, `trial_no`, `submit_type`, `git_url`, `git_branch`, `summary`, `submit_time`, `status`)
SELECT
    8900000000000000000 + ROW_NUMBER() OVER (ORDER BY s.`id`),
    s.`id`,
    COALESCE(s.`submit_count`, 1),
    s.`submit_type`,
    s.`git_url`,
    s.`git_branch`,
    s.`summary`,
    s.`submit_time`,
    CASE
        WHEN s.`status` IN ('SUBMITTED', 'COMPLETED', 'REVIEWED', 'CONFIRMED', 'REVIEWING', 'PUBLISHED') THEN 'CONFIRMED'
        WHEN s.`status` = 'ANALYZING' THEN 'ANALYZING'
        ELSE 'AI_READY'
    END
FROM `submission` s
WHERE NOT EXISTS (
    SELECT 1 FROM `submission_trial` t WHERE t.`submission_id` = s.`id`
);

UPDATE `submission` s
INNER JOIN `submission_trial` t ON t.`submission_id` = s.`id` AND t.`trial_no` = COALESCE(s.`submit_count`, 1)
SET
    s.`current_trial_id` = t.`id`,
    s.`confirmed_trial_id` = CASE
        WHEN s.`status` IN ('SUBMITTED', 'COMPLETED', 'REVIEWED', 'CONFIRMED', 'REVIEWING', 'PUBLISHED') THEN t.`id`
        ELSE NULL
    END,
    s.`confirmed_at` = CASE
        WHEN s.`status` IN ('SUBMITTED', 'COMPLETED', 'REVIEWED', 'CONFIRMED', 'REVIEWING', 'PUBLISHED') THEN s.`submit_time`
        ELSE NULL
    END,
    s.`ai_analyze_used_count` = COALESCE(s.`submit_count`, 1);

-- 5. 状态映射到方案 A
UPDATE `submission` SET `status` = 'CONFIRMED' WHERE `status` = 'SUBMITTED';
UPDATE `submission` SET `status` = 'PUBLISHED' WHERE `status` IN ('COMPLETED', 'REVIEWED', 'GRADED');

-- 6. ai_analysis 关联试交
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'ai_analysis' AND COLUMN_NAME = 'trial_id') = 0,
    'ALTER TABLE `ai_analysis` ADD COLUMN `trial_id` BIGINT DEFAULT NULL COMMENT ''关联试交 ID'' AFTER `submission_id`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `ai_analysis` aa
INNER JOIN `submission` s ON aa.`submission_id` = s.`id`
SET aa.`trial_id` = s.`current_trial_id`
WHERE s.`current_trial_id` IS NOT NULL AND aa.`trial_id` IS NULL;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'ai_analysis' AND INDEX_NAME = 'uk_ai_submission') > 0,
    'ALTER TABLE `ai_analysis` DROP INDEX `uk_ai_submission`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'ai_analysis' AND INDEX_NAME = 'uk_ai_trial') = 0,
    'ALTER TABLE `ai_analysis` ADD UNIQUE INDEX `uk_ai_trial` (`trial_id`)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
