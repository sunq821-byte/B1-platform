-- V9: 一账号一角色 — 移除 admin 双角色配置（PRD V2.2）

DELETE FROM `user_role` WHERE `user_id` = 1 AND `role_id` = 2;
