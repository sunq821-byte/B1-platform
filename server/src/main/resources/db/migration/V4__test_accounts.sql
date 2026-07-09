-- V4: 测试账号 — 教师和学生账户，及额外测试数据

-- 1. 测试账号 (password: 123456)
INSERT IGNORE INTO `user` (`id`, `username`, `password`, `real_name`, `email`, `phone`, `status`) VALUES
(2, 'teacher1', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '张教授', 'zhang@b1.edu.cn', '13800001111', 1),
(3, 'student1', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '李明', 'liming@b1.edu.cn', '13800002222', 1),
(4, 'student2', '$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva', '王芳', 'wangfang@b1.edu.cn', '13800003333', 1);

-- 2. 分配角色
INSERT IGNORE INTO `user_role` (`id`, `user_id`, `role_id`) VALUES
(2, 1, 2),  -- admin 也赋予教师角色
(3, 2, 2),  -- teacher1 → 教师
(4, 3, 3),  -- student1 → 学生
(5, 4, 3);  -- student2 → 学生

-- 3. 教师-课程关联
INSERT IGNORE INTO `course_teacher` (`id`, `course_id`, `user_id`) VALUES
(3, 1, 2),
(4, 2, 2);

-- 4. 学生-课程关联
INSERT IGNORE INTO `course_student` (`id`, `course_id`, `user_id`) VALUES
(3, 1, 3),
(4, 2, 3),
(5, 1, 4);

-- 5. 新增评价标准
INSERT IGNORE INTO `evaluation_standard` (`id`, `standard_name`, `description`, `course_type`, `status`, `is_template`) VALUES
(2, '前端开发标准模板', '前端框架课程默认评价标准', 'CS302', 'PUBLISHED', 1),
(3, '软件工程通用标准', '软件工程类课程通用评价标准', 'GENERAL', 'PUBLISHED', 1);

-- 6. 前端标准维度
INSERT IGNORE INTO `standard_dimension` (`id`, `standard_id`, `dim_name`, `dim_description`, `weight`, `max_score`, `sort_order`) VALUES
(6, 2, '组件设计', '组件 API 设计、复用性、封装合理性', 30, 30, 1),
(7, 2, '代码质量', 'TypeScript 类型安全、ESLint 规范', 25, 25, 2),
(8, 2, 'UI 还原度', '与设计稿一致性、响应式适配', 20, 20, 3),
(9, 2, '性能优化', '渲染性能、打包优化、懒加载', 15, 15, 4),
(10, 2, '测试覆盖', '单元测试、组件测试覆盖率', 10, 10, 5);

-- 7. 通用标准维度
INSERT IGNORE INTO `standard_dimension` (`id`, `standard_id`, `dim_name`, `dim_description`, `weight`, `max_score`, `sort_order`) VALUES
(11, 3, '需求理解', '对实训任务需求的理解和分析', 20, 20, 1),
(12, 3, '技术实现', '技术选型合理性和实现质量', 30, 30, 2),
(13, 3, '创新能力', '解决方案的创新性和扩展性', 15, 15, 3),
(14, 3, '团队协作', 'Git 协作、任务分配、沟通记录', 15, 15, 4),
(15, 3, '文档规范', '报告质量、API文档、注释规范', 20, 20, 5);

-- 8. 学生提交记录
INSERT IGNORE INTO `submission` (`id`, `training_task_id`, `user_id`, `submit_type`, `status`, `submit_count`, `git_url`, `summary`, `submit_time`) VALUES
(2, 1, 3, 'GIT_URL', 'SUBMITTED', 1, 'https://gitee.com/liming/book-mgr', '实现了图书管理系统的核心功能，包括图书CRUD、借阅管理和用户认证。', '2026-07-06T14:30:00'),
(3, 2, 3, 'ZIP_ONLY', 'SUBMITTED', 1, null, '完成了Servlet会话管理的所有要求。', '2026-06-18T10:00:00'),
(4, 3, 4, 'GIT_URL', 'SUBMITTED', 1, 'https://github.com/wangfang/vue-components', '实现了一套基础Vue3组件库，包含6个常用组件。', '2026-07-07T09:00:00');

-- 9. 提交文件
INSERT IGNORE INTO `submission_file` (`id`, `submission_id`, `file_id`, `file_name`, `file_size`, `file_type`, `sort_order`) VALUES
(1, 2, null, 'book-mgr-source.zip', 2456789, 'application/zip', 1),
(2, 2, null, '实训报告.pdf', 1523456, 'application/pdf', 2),
(3, 3, null, 'servlet-session.zip', 1834567, 'application/zip', 1),
(4, 4, null, 'vue-components.zip', 3214567, 'application/zip', 1);
