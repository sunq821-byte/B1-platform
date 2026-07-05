-- ============================================================
-- V1: Initialize database schema — all 26 tables
-- Based on: docs/11-Database-Design.md §4
-- ============================================================

-- 4.1 用户与权限
-- -----------------------------------------------------------

CREATE TABLE `user` (
    `id`                BIGINT      NOT NULL COMMENT '主键，用户ID (Snowflake)',
    `username`          VARCHAR(64) NOT NULL COMMENT '用户名，登录凭据，全局唯一',
    `password`          VARCHAR(256) NOT NULL COMMENT 'BCrypt加密后的密码哈希',
    `real_name`         VARCHAR(64) NOT NULL COMMENT '真实姓名',
    `email`             VARCHAR(128) DEFAULT NULL COMMENT '电子邮箱',
    `phone`             VARCHAR(20)  DEFAULT NULL COMMENT '手机号码',
    `avatar_url`        VARCHAR(512) DEFAULT NULL COMMENT '头像访问URL',
    `status`            TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1=启用, 0=禁用',
    `lock_expire_time`  DATETIME     DEFAULT NULL COMMENT '账户锁定到期时间',
    `login_fail_count`  INT          NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    `last_login_time`   DATETIME     DEFAULT NULL COMMENT '最后一次登录时间',
    `last_login_ip`     VARCHAR(64)  DEFAULT NULL COMMENT '最后一次登录IP地址',
    `deleted`           TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常, 1=已删除',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    `create_by`         BIGINT       DEFAULT NULL COMMENT '创建人ID',
    `update_by`         BIGINT       DEFAULT NULL COMMENT '最后修改人ID',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_username` (`username`),
    INDEX `idx_status` (`status`),
    INDEX `idx_real_name` (`real_name`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户';

CREATE TABLE `role` (
    `id`            BIGINT       NOT NULL COMMENT '主键 (Snowflake)',
    `role_code`     VARCHAR(32)  NOT NULL COMMENT '角色编码：admin, teacher, student',
    `role_name`     VARCHAR(64)  NOT NULL COMMENT '角色名称：管理员, 教师, 学生',
    `description`   VARCHAR(256) DEFAULT NULL COMMENT '角色描述',
    `sort_order`    INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色定义';

CREATE TABLE `user_role` (
    `id`      BIGINT NOT NULL COMMENT '主键 (Snowflake)',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_role` (`user_id`, `role_id`),
    INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联';

-- 4.2 课程与班级
-- -----------------------------------------------------------

CREATE TABLE `course` (
    `id`            BIGINT       NOT NULL COMMENT '主键，课程ID (Snowflake)',
    `course_code`   VARCHAR(32)  NOT NULL COMMENT '课程代码，全局唯一',
    `course_name`   VARCHAR(128) NOT NULL COMMENT '课程名称',
    `semester`      VARCHAR(32)  NOT NULL COMMENT '学期，如 2025-2026-2',
    `credits`       DECIMAL(3,1) DEFAULT 0.0 COMMENT '学分',
    `description`   TEXT         DEFAULT NULL COMMENT '课程描述（Markdown）',
    `syllabus`      TEXT         DEFAULT NULL COMMENT '教学大纲（Markdown）',
    `objectives`    TEXT         DEFAULT NULL COMMENT '教学目标与能力指标',
    `status`        VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE, ARCHIVED',
    `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `version`       INT          NOT NULL DEFAULT 0 COMMENT '乐观锁',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    `create_by`     BIGINT       DEFAULT NULL COMMENT '创建人ID（教师）',
    `update_by`     BIGINT       DEFAULT NULL COMMENT '最后修改人ID',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_course_code` (`course_code`),
    INDEX `idx_semester` (`semester`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程信息';

CREATE TABLE `class` (
    `id`             BIGINT       NOT NULL COMMENT '主键，班级ID (Snowflake)',
    `class_code`     VARCHAR(32)  NOT NULL COMMENT '班级编号，全局唯一',
    `class_name`     VARCHAR(128) NOT NULL COMMENT '班级名称',
    `grade`          VARCHAR(16)  DEFAULT NULL COMMENT '年级',
    `major`          VARCHAR(64)  DEFAULT NULL COMMENT '专业',
    `department`     VARCHAR(64)  DEFAULT NULL COMMENT '院系',
    `student_count`  INT          NOT NULL DEFAULT 0 COMMENT '班级学生人数（冗余缓存）',
    `deleted`        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    `create_by`      BIGINT       DEFAULT NULL COMMENT '创建人ID（管理员）',
    `update_by`      BIGINT       DEFAULT NULL COMMENT '最后修改人ID',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_class_code` (`class_code`),
    INDEX `idx_grade` (`grade`),
    INDEX `idx_major` (`major`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级信息';

CREATE TABLE `course_teacher` (
    `id`        BIGINT NOT NULL COMMENT '主键 (Snowflake)',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `user_id`   BIGINT NOT NULL COMMENT '教师用户ID',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_course_teacher` (`course_id`, `user_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程-教师关联';

CREATE TABLE `course_student` (
    `id`        BIGINT NOT NULL COMMENT '主键 (Snowflake)',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `user_id`   BIGINT NOT NULL COMMENT '学生用户ID',
    `class_id`  BIGINT DEFAULT NULL COMMENT '所在班级ID（冗余）',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_course_student` (`course_id`, `user_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_class_id` (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程-学生关联';

CREATE TABLE `course_class` (
    `id`        BIGINT NOT NULL COMMENT '主键 (Snowflake)',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `class_id`  BIGINT NOT NULL COMMENT '班级ID',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_course_class` (`course_id`, `class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程-班级关联';

-- 4.3 实训任务
-- -----------------------------------------------------------

CREATE TABLE `training_task` (
    `id`                BIGINT       NOT NULL COMMENT '主键，任务ID (Snowflake)',
    `course_id`         BIGINT       NOT NULL COMMENT '所属课程ID',
    `standard_id`       BIGINT       DEFAULT NULL COMMENT '引用的评价标准模板ID',
    `task_name`         VARCHAR(256) NOT NULL COMMENT '任务名称',
    `description`       TEXT         DEFAULT NULL COMMENT '任务描述（Markdown）',
    `requirement`       TEXT         DEFAULT NULL COMMENT '任务要求（Markdown）',
    `start_time`        DATETIME     DEFAULT NULL COMMENT '任务开始时间',
    `end_time`          DATETIME     DEFAULT NULL COMMENT '截止日期（提交DDL）',
    `allow_late`        TINYINT      NOT NULL DEFAULT 0 COMMENT '是否允许逾期提交：0=不允许, 1=允许',
    `late_penalty`      DECIMAL(5,2) DEFAULT 0.00 COMMENT '逾期扣分百分比',
    `submission_type`   VARCHAR(32)  NOT NULL DEFAULT 'ZIP' COMMENT '提交方式：ZIP, GIT, BOTH',
    `max_submit_count`  INT          NOT NULL DEFAULT 3 COMMENT '最大重提交次数',
    `max_score`         DECIMAL(5,2) NOT NULL DEFAULT 100.00 COMMENT '满分分值',
    `weight`            DECIMAL(5,2) DEFAULT 1.00 COMMENT '在课程总评中的权重',
    `priority`          VARCHAR(16)  NOT NULL DEFAULT 'MEDIUM' COMMENT '优先级：HIGH, MEDIUM, LOW',
    `status`            VARCHAR(16)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT, PUBLISHED, ENDED',
    `publish_time`      DATETIME     DEFAULT NULL COMMENT '发布时间',
    `end_actual_time`   DATETIME     DEFAULT NULL COMMENT '实际结束时间',
    `deleted`           TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `version`           INT          NOT NULL DEFAULT 0 COMMENT '乐观锁',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    `create_by`         BIGINT       DEFAULT NULL COMMENT '创建人ID（教师）',
    `update_by`         BIGINT       DEFAULT NULL COMMENT '最后修改人ID',
    PRIMARY KEY (`id`),
    INDEX `idx_course_id` (`course_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_end_time` (`end_time`),
    INDEX `idx_course_status` (`course_id`, `status`),
    INDEX `idx_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实训任务';

CREATE TABLE `training_class` (
    `id`          BIGINT NOT NULL COMMENT '主键 (Snowflake)',
    `training_id` BIGINT NOT NULL COMMENT '任务ID',
    `class_id`    BIGINT NOT NULL COMMENT '班级ID',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_training_class` (`training_id`, `class_id`),
    INDEX `idx_class_id` (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务-班级分发';

-- 4.4 提交与文件
-- -----------------------------------------------------------

CREATE TABLE `submission` (
    `id`               BIGINT       NOT NULL COMMENT '主键，提交ID (Snowflake)',
    `training_task_id` BIGINT       NOT NULL COMMENT '所属任务ID',
    `user_id`          BIGINT       NOT NULL COMMENT '提交学生ID',
    `submit_type`      VARCHAR(8)   NOT NULL COMMENT '提交方式：ZIP, GIT, CODE, TEXT',
    `git_url`          VARCHAR(512) DEFAULT NULL COMMENT 'Git仓库URL',
    `git_branch`       VARCHAR(128) DEFAULT NULL COMMENT 'Git分支名',
    `git_commit_id`    VARCHAR(64)  DEFAULT NULL COMMENT '克隆时的最新Commit ID',
    `summary`          TEXT         DEFAULT NULL COMMENT '提交备注说明',
    `submit_count`     INT          NOT NULL DEFAULT 1 COMMENT '第几次提交（累计）',
    `submit_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `is_late`          TINYINT      NOT NULL DEFAULT 0 COMMENT '是否逾期提交：0=否, 1=是',
    `status`           VARCHAR(32)  NOT NULL DEFAULT 'SUBMITTED' COMMENT '状态：SUBMITTED, ANALYZING, COMPLETED, REVIEWED',
    `deleted`          TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `version`          INT          NOT NULL DEFAULT 0 COMMENT '乐观锁',
    `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    `create_by`        BIGINT       DEFAULT NULL COMMENT '创建人ID（学生本人）',
    `update_by`        BIGINT       DEFAULT NULL COMMENT '最后修改人ID',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_submission_user_task` (`user_id`, `training_task_id`),
    INDEX `idx_training_task_id` (`training_task_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_submit_time` (`submit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生提交';

CREATE TABLE `submission_file` (
    `id`            BIGINT       NOT NULL COMMENT '主键 (Snowflake)',
    `submission_id` BIGINT       NOT NULL COMMENT '提交ID',
    `file_id`       BIGINT       NOT NULL COMMENT '文件存储ID',
    `file_name`     VARCHAR(256) NOT NULL COMMENT '原始文件名（冗余）',
    `file_type`     VARCHAR(32)  NOT NULL COMMENT '文件类型（冗余）：ZIP, JAVA, PDF, DOC, PNG等',
    `file_size`     BIGINT       NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    `sort_order`    INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    PRIMARY KEY (`id`),
    INDEX `idx_submission_id` (`submission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提交文件';

CREATE TABLE `file_storage` (
    `id`            BIGINT        NOT NULL COMMENT '主键，文件ID (Snowflake)',
    `bucket`        VARCHAR(64)   NOT NULL COMMENT '存储桶名称',
    `object_key`    VARCHAR(512)  NOT NULL COMMENT 'MinIO对象Key（路径）',
    `original_name` VARCHAR(256)  NOT NULL COMMENT '原始文件名',
    `content_type`  VARCHAR(128)  DEFAULT NULL COMMENT 'MIME类型',
    `file_size`     BIGINT        NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    `file_md5`      VARCHAR(64)   DEFAULT NULL COMMENT '文件MD5哈希值',
    `access_url`    VARCHAR(1024) DEFAULT NULL COMMENT '文件访问URL（预签名URL）',
    `expire_time`   DATETIME      DEFAULT NULL COMMENT 'URL过期时间 / 文件自动删除时间',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `create_by`     BIGINT        DEFAULT NULL COMMENT '上传人ID',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_object_key` (`object_key`),
    INDEX `idx_bucket` (`bucket`),
    INDEX `idx_file_md5` (`file_md5`),
    INDEX `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件存储';

-- 4.5 AI分析
-- -----------------------------------------------------------

CREATE TABLE `ai_analysis` (
    `id`               BIGINT        NOT NULL COMMENT '主键，分析ID (Snowflake)',
    `submission_id`    BIGINT        NOT NULL COMMENT '关联的提交ID（一对一）',
    `total_score`      DECIMAL(5,2)  DEFAULT NULL COMMENT 'AI综合评分（0-100）',
    `analysis_status`  VARCHAR(16)   NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING, ANALYZING, COMPLETED, FAILED',
    `error_message`    VARCHAR(1024) DEFAULT NULL COMMENT '失败原因',
    `analysis_time_ms` INT           DEFAULT NULL COMMENT '分析耗时（毫秒）',
    `model_provider`   VARCHAR(32)   DEFAULT NULL COMMENT '模型厂商：deepseek, openai',
    `model_name`       VARCHAR(64)   DEFAULT NULL COMMENT '模型名称：deepseek-chat, gpt-4o',
    `token_input`      INT           DEFAULT 0 COMMENT '输入Token数',
    `token_output`     INT           DEFAULT 0 COMMENT '输出Token数',
    `token_total`      INT           DEFAULT 0 COMMENT '总Token数',
    `retry_count`      INT           NOT NULL DEFAULT 0 COMMENT '重试次数（最多3次）',
    `raw_response`     JSON          DEFAULT NULL COMMENT 'LLM原始响应（完整JSON）',
    `start_time`       DATETIME      DEFAULT NULL COMMENT '分析开始时间',
    `complete_time`    DATETIME      DEFAULT NULL COMMENT '分析完成时间',
    `version`          INT           NOT NULL DEFAULT 0 COMMENT '乐观锁',
    `create_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_ai_submission` (`submission_id`),
    INDEX `idx_analysis_status` (`analysis_status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI分析结果';

CREATE TABLE `ai_analysis_detail` (
    `id`              BIGINT        NOT NULL COMMENT '主键，明细ID (Snowflake)',
    `ai_analysis_id`  BIGINT        NOT NULL COMMENT '所属AI分析ID',
    `dimension_id`    BIGINT        DEFAULT NULL COMMENT '关联的评价维度ID',
    `rule_id`         BIGINT        DEFAULT NULL COMMENT '触发的评分规则ID',
    `agent_type`      VARCHAR(8)    NOT NULL COMMENT '分析Agent：DOC, CODE, REQ',
    `file_path`       VARCHAR(512)  DEFAULT NULL COMMENT '问题所在文件路径',
    `line_number`     INT           DEFAULT NULL COMMENT '问题所在行号',
    `issue_type`      VARCHAR(128)  NOT NULL COMMENT '问题类型',
    `severity`        VARCHAR(16)   NOT NULL DEFAULT 'MINOR' COMMENT '严重级别：MINOR, MAJOR, CRITICAL',
    `reason`          TEXT          NOT NULL COMMENT '扣分理由和问题描述',
    `suggestion`      TEXT          DEFAULT NULL COMMENT '改进建议',
    `suggest_deduct`  DECIMAL(5,2)  NOT NULL DEFAULT 0.00 COMMENT 'AI建议扣分分值',
    `confidence`      DECIMAL(3,2)  NOT NULL DEFAULT 1.00 COMMENT 'AI置信度（0.00-1.00）',
    `is_adopted`      TINYINT       DEFAULT NULL COMMENT '教师复核结果：NULL=未处理, 1=采纳, 0=拒绝',
    `adjusted_deduct` DECIMAL(5,2)  DEFAULT NULL COMMENT '教师调整后的扣分',
    `sort_order`      INT           NOT NULL DEFAULT 0 COMMENT '排序号',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_ai_analysis_id` (`ai_analysis_id`),
    INDEX `idx_agent_type` (`agent_type`),
    INDEX `idx_severity` (`severity`),
    INDEX `idx_is_adopted` (`is_adopted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI扣分项明细';

-- 4.6 评价标准
-- -----------------------------------------------------------

CREATE TABLE `evaluation_standard` (
    `id`            BIGINT       NOT NULL COMMENT '主键，标准ID (Snowflake)',
    `standard_name` VARCHAR(256) NOT NULL COMMENT '标准名称',
    `description`   TEXT         DEFAULT NULL COMMENT '标准描述',
    `course_type`   VARCHAR(32)  DEFAULT NULL COMMENT '适用课程类型',
    `version`       INT          NOT NULL DEFAULT 1 COMMENT '版本号（递增）',
    `status`        VARCHAR(16)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT, PUBLISHED, ARCHIVED',
    `is_template`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否为标准库模板：1=可被复制引用, 0=任务专属',
    `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    `create_by`     BIGINT       DEFAULT NULL COMMENT '创建人ID（教师）',
    `update_by`     BIGINT       DEFAULT NULL COMMENT '最后修改人ID',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_is_template` (`is_template`),
    INDEX `idx_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价标准模板';

CREATE TABLE `standard_dimension` (
    `id`              BIGINT        NOT NULL COMMENT '主键，维度ID (Snowflake)',
    `standard_id`     BIGINT        NOT NULL COMMENT '所属标准ID',
    `dim_name`        VARCHAR(128)  NOT NULL COMMENT '维度名称',
    `dim_description` VARCHAR(512)  DEFAULT NULL COMMENT '维度说明',
    `weight`          DECIMAL(5,2)  NOT NULL DEFAULT 0.00 COMMENT '权重百分比',
    `max_score`       DECIMAL(5,2)  NOT NULL DEFAULT 0.00 COMMENT '该维度满分值',
    `sort_order`      INT           NOT NULL DEFAULT 0 COMMENT '排序号',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    PRIMARY KEY (`id`),
    INDEX `idx_standard_id` (`standard_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价维度';

CREATE TABLE `standard_rule` (
    `id`            BIGINT        NOT NULL COMMENT '主键，规则ID (Snowflake)',
    `dimension_id`  BIGINT        NOT NULL COMMENT '所属维度ID',
    `rule_name`     VARCHAR(256)  NOT NULL COMMENT '规则名称',
    `rule_type`     VARCHAR(8)    NOT NULL DEFAULT 'AUTO' COMMENT '检测类型：AUTO, MANUAL',
    `check_method`  VARCHAR(32)   DEFAULT NULL COMMENT '检测方法：REGEX, FILE_EXISTS, DIR_STRUCT, AI_SEMANTIC',
    `check_config`  JSON          DEFAULT NULL COMMENT '检测配置（JSON格式）',
    `max_deduct`    DECIMAL(5,2)  NOT NULL DEFAULT 0.00 COMMENT '本条规则最大扣分值',
    `severity`      VARCHAR(16)   NOT NULL DEFAULT 'MINOR' COMMENT '扣分严重级别：MINOR, MAJOR, CRITICAL',
    `enabled`       TINYINT       NOT NULL DEFAULT 1 COMMENT '是否启用：1=启用, 0=禁用',
    `sort_order`    INT           NOT NULL DEFAULT 0 COMMENT '排序号',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    PRIMARY KEY (`id`),
    INDEX `idx_dimension_id` (`dimension_id`),
    INDEX `idx_check_method` (`check_method`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评分规则';

-- 4.7 教师复核与成绩
-- -----------------------------------------------------------

CREATE TABLE `teacher_review` (
    `id`              BIGINT       NOT NULL COMMENT '主键，复核ID (Snowflake)',
    `submission_id`   BIGINT       NOT NULL COMMENT '关联的提交ID（一对一）',
    `reviewer_id`     BIGINT       NOT NULL COMMENT '复核教师ID',
    `teacher_comment` TEXT         DEFAULT NULL COMMENT '教师评语（个性化反馈）',
    `status`          VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING, REVIEWING, PUBLISHED, RETURNED',
    `review_time`     DATETIME     DEFAULT NULL COMMENT '复核开始时间',
    `publish_time`    DATETIME     DEFAULT NULL COMMENT '成绩发布时间',
    `version`         INT          NOT NULL DEFAULT 0 COMMENT '乐观锁',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    `create_by`       BIGINT       DEFAULT NULL COMMENT '创建人ID（教师）',
    `update_by`       BIGINT       DEFAULT NULL COMMENT '最后修改人ID',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_review_submission` (`submission_id`),
    INDEX `idx_reviewer_id` (`reviewer_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师复核';

CREATE TABLE `review_item` (
    `id`           BIGINT        NOT NULL COMMENT '主键 (Snowflake)',
    `review_id`    BIGINT        NOT NULL COMMENT '所属复核ID',
    `ai_detail_id` BIGINT        DEFAULT NULL COMMENT '关联的AI扣分项ID（手动加扣时为NULL）',
    `dimension_id` BIGINT        DEFAULT NULL COMMENT '关联的评价维度ID',
    `action`       VARCHAR(8)    NOT NULL COMMENT '复核操作：ADOPT, REJECT, ADJUST, MANUAL_ADD',
    `deduct_score` DECIMAL(5,2)  NOT NULL DEFAULT 0.00 COMMENT '最终扣分值',
    `reason`       VARCHAR(1024) DEFAULT NULL COMMENT '复核理由（教师补充说明）',
    `is_manual`    TINYINT       NOT NULL DEFAULT 0 COMMENT '是否为教师手动添加：0=AI产生, 1=教师手动添加',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_review_id` (`review_id`),
    INDEX `idx_ai_detail_id` (`ai_detail_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='复核意见';

CREATE TABLE `score_record` (
    `id`               BIGINT        NOT NULL COMMENT '主键 (Snowflake)',
    `submission_id`    BIGINT        NOT NULL COMMENT '关联的提交ID（一对一）',
    `user_id`          BIGINT        NOT NULL COMMENT '学生ID（冗余）',
    `training_task_id` BIGINT        NOT NULL COMMENT '任务ID（冗余）',
    `total_score`      DECIMAL(5,2)  NOT NULL DEFAULT 0.00 COMMENT '最终总分',
    `ai_total_score`   DECIMAL(5,2)  DEFAULT NULL COMMENT 'AI原始总分（对比用）',
    `score_details`    JSON          DEFAULT NULL COMMENT '各维度得分快照（JSON）',
    `teacher_comment`  TEXT          DEFAULT NULL COMMENT '教师评语（冗余）',
    `status`           VARCHAR(16)   NOT NULL DEFAULT 'PUBLISHED' COMMENT '状态：PUBLISHED, RETURNED',
    `publish_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `version`          INT           NOT NULL DEFAULT 0 COMMENT '乐观锁',
    `create_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_score_submission` (`submission_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_training_task_id` (`training_task_id`),
    INDEX `idx_user_task` (`user_id`, `training_task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='最终成绩';

-- 4.8 报表与统计
-- -----------------------------------------------------------

CREATE TABLE `report` (
    `id`               BIGINT      NOT NULL COMMENT '主键 (Snowflake)',
    `user_id`          BIGINT      NOT NULL COMMENT '生成报表的用户ID',
    `training_task_id` BIGINT      DEFAULT NULL COMMENT '关联任务ID',
    `report_type`      VARCHAR(16) NOT NULL COMMENT '报表类型：STUDENT, CLASS, COLLEGE',
    `format`           VARCHAR(8)  NOT NULL COMMENT '文件格式：PDF, EXCEL',
    `report_params`    JSON        DEFAULT NULL COMMENT '生成参数快照（JSON）',
    `file_id`          BIGINT      DEFAULT NULL COMMENT '关联的文件存储ID',
    `status`           VARCHAR(16) NOT NULL DEFAULT 'GENERATING' COMMENT '状态：GENERATING, COMPLETED, FAILED',
    `create_time`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    `create_by`        BIGINT      DEFAULT NULL COMMENT '生成人ID',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_report_type` (`report_type`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报表记录';

CREATE TABLE `statistics_snapshot` (
    `id`               BIGINT      NOT NULL COMMENT '主键 (Snowflake)',
    `training_task_id` BIGINT      DEFAULT NULL COMMENT '关联任务ID（NULL=全局统计）',
    `class_id`         BIGINT      DEFAULT NULL COMMENT '关联班级ID（NULL=全院统计）',
    `snapshot_type`    VARCHAR(32) NOT NULL COMMENT '快照类型：DASHBOARD, CLASS_PROGRESS, AI_QUALITY',
    `snapshot_data`    JSON        NOT NULL COMMENT '快照数据（JSON格式）',
    `snapshot_time`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '快照时间',
    `create_time`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_snapshot_query` (`snapshot_type`, `training_task_id`, `snapshot_time`),
    INDEX `idx_snapshot_time` (`snapshot_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统计快照';

-- 4.9 系统管理
-- -----------------------------------------------------------

CREATE TABLE `system_config` (
    `id`           BIGINT       NOT NULL COMMENT '主键 (Snowflake)',
    `config_key`   VARCHAR(64)  NOT NULL COMMENT '配置项Key',
    `config_value` VARCHAR(512) NOT NULL COMMENT '配置项Value',
    `config_type`  VARCHAR(16)  NOT NULL DEFAULT 'STRING' COMMENT '值类型：STRING, NUMBER, BOOLEAN, JSON',
    `description`  VARCHAR(256) DEFAULT NULL COMMENT '配置说明',
    `version`      INT          NOT NULL DEFAULT 0 COMMENT '乐观锁',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    `update_by`    BIGINT       DEFAULT NULL COMMENT '最后修改人ID',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置';

CREATE TABLE `operation_log` (
    `id`          BIGINT        NOT NULL COMMENT '主键 (Snowflake)',
    `user_id`     BIGINT        DEFAULT NULL COMMENT '操作用户ID（系统操作为NULL）',
    `username`    VARCHAR(64)   DEFAULT NULL COMMENT '操作用户名（冗余）',
    `module`      VARCHAR(32)   NOT NULL COMMENT '操作模块：AUTH, COURSE, TRAINING, SUBMISSION等',
    `operation`   VARCHAR(32)   NOT NULL COMMENT '操作类型：LOGIN, LOGOUT, CREATE, UPDATE, DELETE等',
    `target_type` VARCHAR(32)   DEFAULT NULL COMMENT '操作对象类型：User, Course, Task, Submission等',
    `target_id`   BIGINT        DEFAULT NULL COMMENT '操作对象ID',
    `detail`      JSON          DEFAULT NULL COMMENT '操作详情（JSON格式）',
    `ip_address`  VARCHAR(64)   DEFAULT NULL COMMENT '操作IP地址',
    `user_agent`  VARCHAR(512)  DEFAULT NULL COMMENT '浏览器User-Agent',
    `duration_ms` INT           DEFAULT NULL COMMENT '操作耗时（毫秒）',
    `result`      VARCHAR(8)    NOT NULL DEFAULT 'SUCCESS' COMMENT '操作结果：SUCCESS, FAILURE',
    `error_msg`   VARCHAR(1024) DEFAULT NULL COMMENT '失败原因',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '日志产生时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_module` (`module`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_result` (`result`),
    INDEX `idx_module_time` (`module`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志';

CREATE TABLE `notification` (
    `id`          BIGINT       NOT NULL COMMENT '主键 (Snowflake)',
    `user_id`     BIGINT       NOT NULL COMMENT '接收用户ID',
    `title`       VARCHAR(256) NOT NULL COMMENT '通知标题',
    `content`     TEXT         NOT NULL COMMENT '通知正文',
    `notify_type` VARCHAR(16)  NOT NULL DEFAULT 'SYSTEM' COMMENT '通知类型：REMIND, SCORE, SYSTEM',
    `is_read`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读：0=未读, 1=已读',
    `read_time`   DATETIME     DEFAULT NULL COMMENT '阅读时间',
    `target_type` VARCHAR(32)  DEFAULT NULL COMMENT '关联业务类型',
    `target_id`   BIGINT       DEFAULT NULL COMMENT '关联业务ID',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '通知创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_unread` (`user_id`, `is_read`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知';
