-- ============================================================
-- V2: Initialize seed data — roles, admin user, system config
-- Based on: docs/11-Database-Design.md §4
-- ============================================================

-- 3 roles (IDs use Snowflake-style values)
-- -----------------------------------------------------------
INSERT INTO `role` (`id`, `role_code`, `role_name`, `description`, `sort_order`) VALUES
(1, 'admin',   '管理员', '最高权限，系统配置和管理',              0),
(2, 'teacher', '教师',   '课程管理、任务发布、复核评分、报表查看', 1),
(3, 'student', '学生',   '查看任务、提交成果、查看个人成绩和报告', 2);

-- Default admin user (password: admin123, BCrypt hashed)
-- Generate a new hash for production use via:
--   cn.hutool.crypto.digest.BCrypt.hashpw("yourPassword")
-- -----------------------------------------------------------
INSERT INTO `user` (`id`, `username`, `password`, `real_name`, `status`) VALUES
(1, 'admin', '$2a$10$5J5cLC7aY5V83tcmOUXNpuRwRWi9X/jPrYOFtTjC2EtJjPQGSizue', '系统管理员', 1);

-- Assign admin role to admin user
-- -----------------------------------------------------------
INSERT INTO `user_role` (`id`, `user_id`, `role_id`) VALUES
(1, 1, 1);

-- System configuration (10 entries)
-- -----------------------------------------------------------
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `config_type`, `description`) VALUES
(1, 'system_name',          'B1 智慧实训平台', 'STRING',  '系统名称'),
(2, 'current_semester',     '2025-2026-2',     'STRING',  '当前学期'),
(3, 'semester_start',       '2026-02-24',      'STRING',  '学期开始日期'),
(4, 'semester_end',         '2026-07-10',      'STRING',  '学期结束日期'),
(5, 'max_upload_size',      '50',              'NUMBER',  '最大上传大小（MB）'),
(6, 'ai_model_version',     'v2.1.0',          'STRING',  'AI 模型版本'),
(7, 'auto_analyze',         'true',            'BOOLEAN', '提交后自动 AI 分析'),
(8, 'notification_enabled', 'true',            'BOOLEAN', '启用系统通知'),
(9, 'maintenance_mode',     'false',           'BOOLEAN', '维护模式');
