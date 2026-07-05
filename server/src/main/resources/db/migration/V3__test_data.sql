-- V3: 测试数据 — 课程、任务、学生关联、评价标准

-- 1. 课程（2门）
INSERT INTO `course` (`id`, `course_code`, `course_name`, `semester`, `credits`, `description`, `status`) VALUES
(1, 'CS301', 'Java Web 开发', '2025-2026-2', 4, '本课程教授 Java Web 开发技术，包括 Servlet、JSP、Spring Boot 等框架。', 'ACTIVE'),
(2, 'CS302', '前端框架技术', '2025-2026-2', 3, '学习 Vue.js、React 等现代前端框架的核心概念与实践。', 'ACTIVE');

-- 2. 课程-教师关联（admin 作为授课教师）
INSERT INTO `course_teacher` (`id`, `course_id`, `user_id`) VALUES
(1, 1, 1),
(2, 2, 1);

-- 3. 课程-学生关联（admin 双角色，同时作为学生）
INSERT INTO `course_student` (`id`, `course_id`, `user_id`) VALUES
(1, 1, 1),
(2, 2, 1);

-- 4. 评价标准模板
INSERT INTO `evaluation_standard` (`id`, `standard_name`, `description`, `course_type`, `status`, `is_template`) VALUES
(1, 'Java Web 标准模板', 'Java Web 课程默认评价标准', 'CS301', 'PUBLISHED', 1);

-- 5. 评价维度（5个维度，权重和=100%）
INSERT INTO `standard_dimension` (`id`, `standard_id`, `dim_name`, `dim_description`, `weight`, `max_score`, `sort_order`) VALUES
(1, 1, '代码规范', '代码风格、命名规范、注释完整性', 25, 25, 1),
(2, 1, '功能完成度', '需求实现程度、功能是否完整', 30, 30, 2),
(3, 1, '创新设计', '架构设计、算法优化、创新点', 20, 20, 3),
(4, 1, '文档撰写', 'README、接口文档、设计文档', 15, 15, 4),
(5, 1, 'Git规范', '提交记录、分支管理、代码审查', 10, 10, 5);

-- 6. 实训任务（3个）
INSERT INTO `training_task` (`id`, `course_id`, `standard_id`, `task_name`, `description`, `requirement`, `end_time`, `submission_type`, `max_submit_count`, `max_score`, `status`, `publish_time`) VALUES
(1, 1, 1, 'Spring Boot 图书管理系统实训', '开发一个基于 Spring Boot 的图书管理系统，支持图书的增删改查、借阅归还、用户管理等功能。', '1. 使用 Spring Boot 3.x 框架\n2. 集成 MyBatis Plus 进行数据访问\n3. RESTful API 设计\n4. 提交源码 ZIP + 实训报告', '2026-07-15T23:59:59', 'GIT_ZIP', 3, 100, 'PUBLISHED', '2026-06-25T10:00:00'),
(2, 1, 1, 'Servlet 会话管理实训', '实现基于 Servlet 的用户会话管理，包括登录、登出、会话超时处理、单点登录限制。', '1. 使用原生 Servlet API\n2. 实现 Session 管理\n3. 完成 Filter 拦截器\n4. 提交源码 ZIP', '2026-06-20T23:59:59', 'ZIP_ONLY', 3, 80, 'PUBLISHED', '2026-05-20T10:00:00'),
(3, 2, null, 'Vue 3 组件库实训', '设计并实现一套基础的 Vue 3 组件库，包括 Button、Input、Modal、Table 等常用组件。', '1. 使用 Vue 3 Composition API\n2. TypeScript 开发\n3. 每个组件包含 Props 文档\n4. 提交源码 + Storybook 文档', '2026-07-08T23:59:59', 'GIT_ZIP', 3, 100, 'PUBLISHED', '2026-06-15T10:00:00');