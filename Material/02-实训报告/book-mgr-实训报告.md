# Spring Boot 图书管理系统 — 实训报告（演示素材）

**学生**：李明（student1）  
**课程**：Java Web 开发（CS301）  
**任务**：Spring Boot 图书管理系统实训

## 1. 项目概述

基于 Spring Boot 3 + MyBatis Plus 实现图书 CRUD、借阅管理与用户认证，RESTful API 设计。

## 2. 功能完成度

| 功能 | 状态 | 说明 |
|------|------|------|
| 图书 CRUD | ✅ | `/api/books` 完整实现 |
| 借阅管理 | ✅ | 借还记录表设计完成 |
| 用户认证 | ⚠️ | JWT 基础版，待完善刷新令牌 |
| 单元测试 | ⚠️ | Service 层覆盖率约 45% |

## 3. 规范性

- 包结构按 controller / service / mapper 分层
- 统一异常处理 `@ControllerAdvice`
- Git 提交遵循 Conventional Commits

## 4. 质量

- 分层清晰，事务边界明确
- 建议补充借阅并发控制（乐观锁）

## 5. 表达与完整性

- 本报告含功能清单与改进计划
- 附录：API 列表见 `05-设计文档/api-design-sample.md`

---

> 本文件为演示素材，AI 参考分非最终成绩。
