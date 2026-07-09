# Backend Implementation Plan v1.0

## 基于大模型的软件实训教学检查评价与报表系统

---

**文档性质**：后端实施蓝图。描述后端"如何构建"。所有后端开发工作以本文档为执行依据。

**权威来源**：本文档遵循 `docs/00-Architecture-Baseline.md` v2.0。如发现冲突，以 Architecture Baseline 为准。

---

## 1. Project Goals

### 1.1 Backend Responsibilities

本系统后端承担以下核心职责：

| 职责 | 说明 |
|---|---|
| **业务逻辑处理** | 所有核心业务规则在服务端实现。前端仅负责 UI 交互和展示 |
| **数据持久化** | MySQL 存储业务数据，Redis 缓存热数据，MinIO 存储文件。后端是数据的唯一权威来源 |
| **认证与授权** | Sa-Token 实现 RBAC 三角色权限体系。后端是权限校验的最终执行者（前端权限控制仅为 UI 优化） |
| **AI 分析引擎** | 调用 LLM API 进行代码/文档/需求分析，输出结构化诊断报告。AI 结果需经教师确认方可生效 |
| **文件管理** | 文件上传校验、MinIO 存储、预签名 URL 生成、临时文件清理 |
| **报表生成** | 异步生成 PDF/Excel 报表，支持个人/班级/学院三级报表 |
| **Git 集成** | Git 仓库验证、代码克隆（供 AI 分析用）、临时工作区管理 |
| **操作审计** | 全平台操作日志记录，满足教学合规审计要求 |

### 1.2 System Scope

#### 1.2.1 MVP Scope（Sprint 1-7，必须交付）

| 功能域 | 范围 |
|---|---|
| **认证授权** | 用户名密码登录、Sa-Token 认证、三角色 RBAC 权限隔离、Token 刷新与黑名单 |
| **用户管理** | 个人信息查询修改、密码修改、头像上传。管理员：用户 CRUD、角色分配、状态管理 |
| **课程管理** | 课程 CRUD、教师/学生/班级关联、学期管理、课程归档 |
| **实训任务** | 任务创建/发布/结束、班级分发、截止日期管理、提交方式配置（ZIP/GIT/BOTH） |
| **成果提交** | ZIP 文件上传、Git URL 提交、重提交次数限制、逾期检测、提交状态跟踪 |
| **AI 分析** | 代码规范检查、文档完整性分析、需求匹配度评估、多维度评分、扣分项明细、置信度标注 |
| **教师复核** | AI 结果展示、逐项采纳/拒绝/调整、手动加扣分、评语编辑、成绩发布/退回 |
| **报表统计** | 个人成绩趋势、班级成绩分布、学院跨班对比、PDF/Excel 导出 |
| **评价标准** | 标准模板 CRUD、维度权重配置、评分规则定义、标准库管理、模板复制 |
| **系统管理** | 班级管理、系统配置、仪表盘统计、操作日志查询、服务健康监控 |
| **文件服务** | 上传/下载、类型校验、MinIO 存储、预签名 URL、临时文件清理 |

#### 1.2.2 Future Scope（v1.1+）

| 功能域 | 优先级 | 触发条件 |
|---|---|---|
| RAG 知识库检索 | P1 | v1.1：知识文档上传 → Embedding → 向量检索 → AI Prompt 增强 |
| 智能 Agent 辅导 | P1 | v1.1：SecurityAgent（安全检测）、PlagiarismAgent（查重）、TutorAgent（学习建议） |
| MOSS 查重集成 | P1 | v1.1：代码/报告相似度检测 |
| 知识图谱 | P2 | v2.0：学生知识点掌握图谱 |
| 多租户 | P2 | v2.0：多学校独立部署 |
| 微服务拆分 | P3 | v3.0：按业务域拆分 |

### 1.3 Success Metrics

| 指标 | 目标 |
|---|---|
| 核心业务流程闭环 | 学生提交→AI 分析→教师复核→成绩发布 全链路可用 |
| API 接口完整性 | 覆盖 API Mock Specification 中 100% 的接口 |
| 单元测试覆盖率 | Service 层 80%+，Controller 层 60%+ |
| 性能 | Dashboard 查询 <500ms；AI 分析异步不阻塞提交；列表分页查询 <200ms |
| 安全性 | 所有 API 权限校验 100% 覆盖；SQL 注入零风险；密码 BCrypt 不可逆 |
| 代码质量 | ESLint 级别标准：命名规范、分层规范、异常规范零违反 |

---

## 2. Architecture

### 2.1 Overall Backend Architecture

本系统采用 **Modular Monolith（模块化单体）** 架构。

```
┌─────────────────────────────────────────────────────┐
│                  Spring Boot 3.5                     │
│                                                      │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐   │
│  │  Auth   │ │  User   │ │ Course  │ │Training │   │
│  │ Module  │ │ Module  │ │ Module  │ │ Module  │   │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘   │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐   │
│  │Submission│ │   AI    │ │ Review  │ │ Report  │   │
│  │ Module  │ │ Module  │ │ Module  │ │ Module  │   │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘   │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐   │
│  │Standard │ │ System  │ │  File   │ │  Git    │   │
│  │ Module  │ │ Module  │ │ Module  │ │ Module  │   │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘   │
│  ┌─────────┐ ┌─────────┐                           │
│  │   Log   │ │  Notify │                           │
│  │ Module  │ │ Module  │                           │
│  └─────────┘ └─────────┘                           │
│                                                      │
│  Cross-cutting:                                     │
│  Sa-Token │ Validation │ Exception Handler │ AOP    │
└──────────────────────┬──────────────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
   ┌─────────┐  ┌─────────┐  ┌─────────┐
   │ MySQL 8 │  │ Redis 7 │  │  MinIO  │
   └─────────┘  └─────────┘  └─────────┘
```

**模块间通信**：
- 同步调用：Service 接口 → Service 实现（本地方法调用）
- 异步事件：Spring ApplicationEvent（`SubmissionCompletedEvent` → `AIAnalysisListener`；`ReviewPublishedEvent` → `ReportRefreshListener` + `NotificationListener`）

### 2.2 Layered Architecture

每个模块内部严格遵循四层架构：

```
┌────────────────────────────────────┐
│         Controller Layer           │
│  - @RestController                 │
│  - @Valid 参数校验                 │
│  - 调用 Service                     │
│  - 返回 Result<T>                   │
│  - 禁止：业务逻辑、直接调 Mapper    │
└──────────────┬─────────────────────┘
               │ DTO → Entity (MapStruct)
               ▼
┌────────────────────────────────────┐
│          Service Layer             │
│  - 业务逻辑编排                     │
│  - @Transactional 事务管理         │
│  - 调用 Mapper / Provider / Event  │
│  - 抛出 BusinessException          │
│  - 禁止：处理 HTTP、直接 JDBC      │
└──────────────┬─────────────────────┘
               │
               ▼
┌────────────────────────────────────┐
│          Mapper Layer              │
│  - MyBatis-Plus BaseMapper         │
│  - 复杂查询 XML                     │
│  - 禁止：业务逻辑、调 Service       │
└──────────────┬─────────────────────┘
               │
               ▼
┌────────────────────────────────────┐
│           Data Layer               │
│  MySQL 8 / Redis 7 / MinIO         │
└────────────────────────────────────┘
```

### 2.3 Cross-Cutting Concerns

| 关注点 | 实现方式 |
|---|---|
| 认证鉴权 | Sa-Token 拦截器 + `@SaCheckRole` / `@SaCheckPermission` 注解 |
| 参数校验 | Jakarta Validation + `@Valid` + 自定义校验注解 |
| 异常处理 | `@RestControllerAdvice` GlobalExceptionHandler 统一处理 |
| 操作日志 | `@OperationLog` 注解 + AOP 切面异步写入 |
| AI 日志 | `@AILog` 注解 + AOP 切面记录 Token 用量和耗时 |
| 接口限流 | `@RateLimit` 注解 + Interceptor + Redis 计数器 |
| 链路追踪 | LogInterceptor 注入 traceId 到 MDC，全链路传递 |
| 响应包装 | `Result<T>` / `PageResult<T>` 统一格式 |

### 2.4 Key Architectural Decisions

| 决策 | 选择 | 理由 |
|---|---|---|
| 架构模式 | Modular Monolith | 团队 3-5 人，MVP 快速交付。包结构预留微服务拆分路径 |
| 认证框架 | Sa-Token | 比 Spring Security 轻量 80%，配置更简洁，功能覆盖 MVC 全部场景 |
| 模块通信 | 本地调用 + Spring Event | 单体架构无需 RPC。事件驱动解耦 AI 分析和通知 |
| AI 调用 | Java 内嵌（RestTemplate/WebClient） | MVP 不部署独立 Python 服务。通过接口抽象隔离 LLM 厂商 |
| 静态分析 | Checkstyle only | 代码风格统一够用。PMD/SpotBugs 可在 CI 中按需添加 |
| Excel | EasyExcel | 比 Apache POI 内存占用更低，API 更简洁 |
| PDF | OpenPDF | 开源、轻量、满足报表导出需求 |

---

## 3. Module Breakdown

### 3.1 Module Overview

| # | 模块 | 包路径 | MVP 优先级 | 核心接口数 | 依赖 |
|---|---|---|---|---|---|
| 1 | Auth | `module.auth` | P0（Blocking） | 3 | User |
| 2 | User | `module.user` | P0（Blocking） | 4 | File |
| 3 | Course | `module.course` | P0（Blocking） | 8 | User |
| 4 | Training | `module.training` | P1（High） | 8 | Course, Standard |
| 5 | Submission | `module.submission` | P1（High） | 6 | Training, File, Git |
| 6 | AI | `module.ai` | P1（High） | 5 | Submission, File, Standard |
| 7 | Review | `module.review` | P1（High） | 6 | Submission, AI |
| 8 | Report | `module.report` | P2（Medium） | 6 | Submission, Review, File |
| 9 | Standard | `module.standard` | P2（Medium） | 6 | — |
| 10 | System | `module.system` | P2（Medium） | 10 | User, Log |
| 11 | File | `module.file` | P0（Blocking） | 4 | — |
| 12 | Git | `module.git` | P1（High） | 3 | — |
| 13 | Log | `module.log` | P2（Medium） | 2 | — |
| 14 | Notification | `module.notification` | P3（Low） | 3 | User |

**优先级说明**：
- **P0 (Blocking)**：其他模块依赖它。必须最先完成
- **P1 (High)**：核心业务闭环。Sprint 前期集中交付
- **P2 (Medium)**：展现代码质量和完整性的模块。Sprint 中期交付
- **P3 (Low)**：锦上添花。MVP 在 Sprint 后期或 v1.1 完成

### 3.2 Auth Module（认证模块）

| 属性 | 值 |
|---|---|
| **职责** | 用户登录认证、Token 签发与刷新、登出与 Token 失效、登录失败锁定 |
| **优先级** | P0（Blocking） |
| **依赖** | User Module（用户查询） |

**Core APIs**：

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | `/api/v1/auth/login` | 用户登录 | 公开 |
| POST | `/api/v1/auth/logout` | 用户登出 | 已登录 |
| POST | `/api/v1/auth/refresh` | 刷新 Token | 公开（需 Refresh Token） |

**核心逻辑**：
- 用户名密码校验 → BCrypt 对比 → Sa-Token `StpUtil.login()` → 生成 Token → 缓存用户信息到 Redis → 发布 `LoginSuccessEvent` → 返回 `LoginResponse`（token + refreshToken + userInfo）
- 登录失败 5 次锁定 30 分钟（`lock_expire_time`）
- 登出：Token 加入 Redis 黑名单（TTL = Token 剩余有效期）
- Access Token 有效期 2h，Refresh Token 有效期 7d

### 3.3 User Module（用户模块）

| 属性 | 值 |
|---|---|
| **职责** | 当前用户信息查询、个人信息修改、密码修改、头像上传 |
| **优先级** | P0（Blocking） |
| **依赖** | File Module（头像存储） |

**Core APIs**：

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| GET | `/api/v1/user/profile` | 获取当前用户信息 | 已登录 |
| PUT | `/api/v1/user/profile` | 修改个人信息 | 已登录 |
| PUT | `/api/v1/user/password` | 修改密码 | 已登录 |
| POST | `/api/v1/user/avatar` | 上传头像 | 已登录 |

**核心逻辑**：
- 修改密码需验证旧密码，BCrypt 加密新密码
- 密码强度校验：8+ 位，包含大小写字母和数字
- 个人信息修改记录到 `operation_log`

### 3.4 Course Module（课程模块）

| 属性 | 值 |
|---|---|
| **职责** | 课程 CRUD、教师/学生/班级关联、学期管理、课程归档 |
| **优先级** | P0（Blocking） |
| **依赖** | User Module |

**Core APIs**：

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | `/api/v1/teacher/courses` | 创建课程 | Teacher |
| GET | `/api/v1/teacher/courses` | 教师课程列表 | Teacher |
| GET | `/api/v1/teacher/courses/{id}` | 课程详情 | Teacher |
| PUT | `/api/v1/teacher/courses/{id}` | 修改课程 | Teacher |
| DELETE | `/api/v1/teacher/courses/{id}` | 删除课程（归档） | Teacher |
| GET | `/api/v1/student/courses` | 学生课程列表 | Student |
| GET | `/api/v1/student/courses/{id}` | 学生课程详情 | Student |

**核心逻辑**：
- 课程创建 + 教师分配 + 班级关联 + 学生批量导入为一个原子事务
- 课程删除为逻辑删除（归档），已有提交记录的课程不可物理删除
- 学生只能查看自己选课的课程；教师只能查看自己授课的课程

### 3.5 Training Module（实训任务模块）

| 属性 | 值 |
|---|---|
| **职责** | 任务 CRUD、发布/草稿/结束状态机、班级分发、截止日期管理、进度统计 |
| **优先级** | P1（High） |
| **依赖** | Course Module, Standard Module |

**Core APIs**：

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | `/api/v1/teacher/tasks` | 创建任务 | Teacher |
| GET | `/api/v1/teacher/tasks` | 教师任务列表 | Teacher |
| GET | `/api/v1/teacher/tasks/{id}` | 任务详情 | Teacher |
| PUT | `/api/v1/teacher/tasks/{id}` | 修改任务 | Teacher |
| DELETE | `/api/v1/teacher/tasks/{id}` | 删除任务 | Teacher |
| POST | `/api/v1/teacher/tasks/{id}/publish` | 发布任务 | Teacher |
| GET | `/api/v1/teacher/tasks/{id}/progress` | 任务进度 | Teacher |
| GET | `/api/v1/student/tasks` | 学生任务列表 | Student |
| GET | `/api/v1/student/tasks/{id}` | 学生任务详情 | Student |

**状态机**：`DRAFT → PUBLISHED → ENDED`

**核心逻辑**：
- 发布任务时同时写入 `training_class` 分发班级
- 截止日期过后自动/手动结束任务（`end_actual_time`）
- 进度统计：`submittedCount / totalCount`，使用 `statistics_snapshot` 缓存（5 分钟刷新）

### 3.6 Submission Module（提交模块）

| 属性 | 值 |
|---|---|
| **职责** | 学生提交实训成果（ZIP/Git/文本）、提交历史查询、截止日期校验、重提交限制 |
| **优先级** | P1（High） |
| **依赖** | Training Module, File Module, Git Module |

**Core APIs**：

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | `/api/v1/student/tasks/{taskId}/submissions` | 创建提交 | Student |
| GET | `/api/v1/student/submissions` | 学生提交历史 | Student |
| GET | `/api/v1/student/submissions/{id}` | 提交详情 | Student |
| GET | `/api/v1/teacher/submissions` | 教师查看提交列表 | Teacher |
| GET | `/api/v1/teacher/submissions/{id}` | 教师查看提交详情 | Teacher |

**状态机**：`SUBMITTED → ANALYZING → COMPLETED → REVIEWED`

**核心逻辑**：
- 提交前校验：截止日期（`is_late` 标记）、重提交次数（`max_submit_count`）
- ZIP 提交：先上传文件到 MinIO（`POST /api/v1/files/upload`）→ 得到 fileId → 创建提交
- Git 提交：先验证 Git 仓库（`POST /api/v1/student/tasks/{taskId}/git-verify`）→ 验证通过后创建提交
- 提交成功后发布 `SubmissionCompletedEvent`（异步触发 AI 分析）

### 3.7 AI Module（AI 分析模块）

| 属性 | 值 |
|---|---|
| **职责** | 三层递进分析、多 Agent 协作、结构化诊断报告生成、评分计算 |
| **优先级** | P1（High） |
| **依赖** | Submission Module, File Module, Standard Module |

**Core APIs**：

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | `/api/v1/student/submissions/{sid}/ai-evaluate` | 手动触发 AI 分析 | Student |
| GET | `/api/v1/student/submissions/{sid}/ai-result` | 获取 AI 分析结果摘要 | Student |
| GET | `/api/v1/student/submissions/{sid}/ai-result-detail` | 获取 AI 分析扣分明细 | Student |
| GET | `/api/v1/teacher/submissions/{sid}/ai-result` | 教师查看 AI 结果 | Teacher |
| GET | `/api/v1/teacher/submissions/{sid}/ai-result-detail` | 教师查看 AI 扣分明细 | Teacher |

**三层递进分析流程**：
1. **Checkstyle 静态分析**（确定性规则：命名规范、格式问题）→ 零幻觉
2. **规则引擎检查**（文件存在性、目录结构、必填项）→ 零幻觉
3. **LLM 语义分析**（DocAgent + CodeAgent + ReqAgent）→ 结构化 JSON 输出

**AI Provider 抽象**：
- `AIAnalysisProvider` 接口定义：`chat()`、`chatWithJsonSchema()`、`estimateTokens()`、`healthCheck()`
- 实现类：`DeepSeekProvider`（默认）、`OpenAICompatProvider`（备用）
- 通过配置 `b1.ai.provider` 切换，无需修改业务代码

**Prompt 管理**：
- Prompt 模板集中存储在 `resources/ai/prompts/` 目录
- `PromptManager` 负责加载模板并填充变量
- 模板变量：`{submissionContent}`、`{standardJson}`、`{taskRequirement}`、`{language}`

**Token 与 Retry**：
- 调用前估算 Token 数（超出 80% 上下文窗口时分批处理）
- 失败自动重试 3 次（指数退避 1s → 2s → 4s）
- 超时 120s
- 超过重试次数标记 `analysis_status = FAILED`，学生可手动重新触发

### 3.8 Review Module（教师复核模块）

| 属性 | 值 |
|---|---|
| **职责** | AI 结果展示、逐项复核操作、手动加扣分、评语编辑、成绩发布/退回 |
| **优先级** | P1（High） |
| **依赖** | Submission Module, AI Module |

**Core APIs**：

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| GET | `/api/v1/teacher/reviews` | 待复核列表 | Teacher |
| GET | `/api/v1/teacher/reviews/{id}` | 复核详情（含 AI 结果） | Teacher |
| PUT | `/api/v1/teacher/reviews/{id}/items/{detailId}` | 逐项复核 | Teacher |
| POST | `/api/v1/teacher/reviews/{id}/items` | 手动添加扣分项 | Teacher |
| POST | `/api/v1/teacher/reviews/{id}/publish` | 发布成绩 | Teacher |
| POST | `/api/v1/teacher/reviews/{id}/return` | 退回重提 | Teacher |

**复核操作**：`ADOPT`（采纳 AI 建议）/ `REJECT`（拒绝 AI 建议）/ `ADJUST`（调整扣分值）/ `MANUAL_ADD`（教师手动添加扣分项）

**核心逻辑**：
- 复核发布为原子事务：更新 `teacher_review` 状态 + 写入 `review_item` + 写入 `score_record` + 更新 `submission` 状态
- 发布后触发 `ReviewPublishedEvent`：异步刷新报表缓存 + 推送通知给学生
- 隔离级别：`REPEATABLE_READ`（确保复核期间数据一致性）

### 3.9 Report Module（报表模块）

| 属性 | 值 |
|---|---|
| **职责** | 个人/班级/学院三级报表、成绩趋势图、能力雷达图、PDF/Excel 导出 |
| **优先级** | P2（Medium） |
| **依赖** | Submission Module, Review Module, File Module |

**Core APIs**：

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| GET | `/api/v1/student/reports` | 个人报表数据 | Student |
| GET | `/api/v1/student/reports/growth` | 成长趋势数据 | Student |
| GET | `/api/v1/teacher/reports` | 班级/学院报表数据 | Teacher |
| POST | `/api/v1/teacher/reports/export` | 导出报表文件 | Teacher |
| GET | `/api/v1/teacher/reports/{id}/download` | 下载已生成的报表 | Teacher |

**核心逻辑**：
- 报表数据优先从 `statistics_snapshot` 表读取（避免实时聚合）
- 导出为异步任务：创建 `report` 记录（status=GENERATING）→ `@Async` 生成文件 → 上传 MinIO → 更新状态为 COMPLETED → 返回下载 URL
- 报表文件 90 天自动过期清理

### 3.10 Standard Module（评价标准模块）

| 属性 | 值 |
|---|---|
| **职责** | 评价标准模板 CRUD、维度权重配置、评分规则管理、标准库、模板复制 |
| **优先级** | P2（Medium） |
| **依赖** | 无（独立模块） |

**Core APIs**：

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | `/api/v1/teacher/standards` | 创建标准模板 | Teacher |
| GET | `/api/v1/teacher/standards` | 标准列表 | Teacher |
| GET | `/api/v1/teacher/standards/{id}` | 标准详情（含维度和规则） | Teacher |
| PUT | `/api/v1/teacher/standards/{id}` | 修改标准 | Teacher |
| DELETE | `/api/v1/teacher/standards/{id}` | 删除标准（归档） | Teacher |
| POST | `/api/v1/teacher/standards/{id}/copy` | 复制标准模板 | Teacher |

**三层结构**：`evaluation_standard（模板） → standard_dimension（维度） → standard_rule（规则）`

**约束**：
- 同一标准下所有维度权重之和必须 = 100%（应用层校验）
- 标准模板修改后版本号 +1
- 已被任务引用的标准不可删除（归档）

### 3.11 System Module（系统管理模块）

| 属性 | 值 |
|---|---|
| **职责** | 用户管理、班级管理、系统配置、仪表盘统计、操作日志查询、服务健康监控 |
| **优先级** | P2（Medium） |
| **依赖** | User Module, Log Module |

**Core APIs**：

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| GET | `/api/v1/admin/dashboard` | 仪表盘数据 | Admin |
| POST | `/api/v1/admin/users` | 创建用户 | Admin |
| GET | `/api/v1/admin/users` | 用户列表 | Admin |
| PUT | `/api/v1/admin/users/{id}` | 修改用户 | Admin |
| PUT | `/api/v1/admin/users/{id}/status` | 启停用户 | Admin |
| POST | `/api/v1/admin/classes` | 创建班级 | Admin |
| GET | `/api/v1/admin/classes` | 班级列表 | Admin |
| PUT | `/api/v1/admin/classes/{id}` | 修改班级 | Admin |
| GET | `/api/v1/admin/configs` | 系统配置列表 | Admin |
| PUT | `/api/v1/admin/configs/{key}` | 修改配置 | Admin |
| GET | `/api/v1/admin/monitor` | 服务健康状态 | Admin |

**核心逻辑**：
- 用户创建为事务：`user` + `user_role` 原子写入
- 仪表盘数据从 `statistics_snapshot` 读取
- 系统配置变更记录 `operation_log`

### 3.12 File Module（文件模块）

| 属性 | 值 |
|---|---|
| **职责** | 文件上传/下载、类型/大小/魔数安全校验、MD5 去重、预签名 URL、过期清理 |
| **优先级** | P0（Blocking） |
| **依赖** | MinIO |

**Core APIs**：

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | `/api/v1/files/upload` | 上传文件 | 已登录 |
| GET | `/api/v1/files/{id}/download` | 下载文件 | 已登录（+ 权限校验） |
| GET | `/api/v1/files/{id}/preview` | 预览文件（预签名 URL） | 已登录（+ 权限校验） |

**安全校验链**：文件大小 → 扩展名白名单 → 魔数校验 → ZIP 炸弹检测 → 路径穿越检测 → MinIO 上传 → file_storage 记录写入

### 3.13 Git Module（Git 模块）

| 属性 | 值 |
|---|---|
| **职责** | Git URL 验证、仓库元信息查询、代码克隆（供 AI 分析用）、临时工作区管理 |
| **优先级** | P1（High） |
| **依赖** | JGit |

**Core APIs**：

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | `/api/v1/student/tasks/{taskId}/git-verify` | 验证 Git 仓库 | Student |
| GET | `/api/v1/student/tasks/{taskId}/git-info` | 获取仓库信息 | Student |

**核心逻辑**：
- 验证：HTTPS URL 格式校验 → `ls-remote` 可达性检查 → 分支存在性检查
- 克隆：`{b1.git.clone-dir}/{userId}/{submissionId}/` → 60s 超时 → 分析完成后删除
- 异常映射：`GIT_REPO_NOT_FOUND(8001)`、`GIT_NO_PERMISSION(8002)`、`GIT_CLONE_FAILED(8003)`、`GIT_BRANCH_NOT_FOUND(8004)`

### 3.14 Log Module（日志模块）

| 属性 | 值 |
|---|---|
| **职责** | 操作日志异步写入、日志查询与筛选、日志归档 |
| **优先级** | P2（Medium） |
| **依赖** | 无（事件驱动） |

**Core APIs**：

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| GET | `/api/v1/admin/logs` | 操作日志列表（筛选+分页） | Admin |

**实现方式**：`LogEventListener` 监听全局 `OperationLogEvent` → `@Async("log-executor")` 异步写入 `operation_log` 表。日志表只有 INSERT 权限，无 UPDATE/DELETE。

### 3.15 Notification Module（通知模块）

| 属性 | 值 |
|---|---|
| **职责** | 系统通知推送、已读/未读管理、通知列表 |
| **优先级** | P3（Low，Sprint 后期或 v1.1） |
| **依赖** | User Module |

**Core APIs**：

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| GET | `/api/v1/user/notifications` | 通知列表 | 已登录 |
| PUT | `/api/v1/user/notifications/{id}/read` | 标记已读 | 已登录 |

**触发场景**：成绩发布 → `ReviewPublishedEvent` → `NotificationListener` → 写入 `notification` 表

---

## 4. Sprint Planning

### 4.1 Sprint Overview

| Sprint | 周期 | 主题 | 模块 | 预计人天 |
|---|---|---|---|---|
| Sprint 1 | 2 周 | 基础设施 + 认证授权 | common, infrastructure, Auth, User, File | 12-15 |
| Sprint 2 | 2 周 | 课程 + 实训任务 | Course, Training, Standard | 12-15 |
| Sprint 3 | 2 周 | 提交 + Git | Submission, Git, File（扩展） | 12-15 |
| Sprint 4 | 2 周 | AI 分析引擎 | AI, Static Analysis | 14-18 |
| Sprint 5 | 2 周 | 教师复核 + 成绩 | Review, Notification（基础） | 12-15 |
| Sprint 6 | 2 周 | 报表 + 系统管理 | Report, System, Log | 12-15 |
| Sprint 7 | 2 周 | 联调 + 测试 + 部署 | 全模块 | 10-12 |
| Sprint 8 | 2 周 | 优化 + 文档 + 竞赛准备 | 全模块 | 8-10 |

**总预计人天**：92-115 人天（2-3 后端开发，约 8 周完成 Sprint 1-7）

### 4.2 Sprint 1 — 基础设施 + 认证授权

**Goal**：搭建项目脚手架，完成认证授权闭环。用户能用账号密码登录，Token 正常签发和校验。

**Modules**：`common/`（全部基础设施）、Auth、User、File（上传部分）

**Estimated Workload**：12-15 人天

**Key Deliverables**：
- Spring Boot 项目脚手架（pom.xml、application.yml 多环境、Dockerfile）
- Sa-Token 集成（登录/登出/Token 刷新/黑名单/权限注解）
- GlobalExceptionHandler + ErrorCode 枚举（完整 50+ 错误码）
- Result/PageResult 统一响应体
- MyBatis-Plus 配置（分页插件、乐观锁插件、自动填充、逻辑删除）
- Redis 配置（序列化、连接池、Spring Cache 集成）
- Knife4j 配置（OpenAPI 3.0 文档可访问）
- LogInterceptor + traceId 全链路注入
- 文件上传基础设施（MinIO 客户端、安全校验链、FileStorage 写入）
- Auth 接口：POST login、POST logout、POST refresh
- User 接口：GET/PUT profile、PUT password、POST avatar
- 预置 3 角色数据（admin/teacher/student）+ 1 个 admin 账号

**Dependencies**：无前置依赖（Sprint 1 是起点）

**Acceptance Criteria**：
- AC-1：用户用 admin/admin123 登录成功，返回 Token 和用户信息
- AC-2：Token 有效期内可访问受保护接口；过期后返回 2001 错误码
- AC-3：无权限角色访问接口返回 3001 错误码
- AC-4：参数校验失败返回 4001 错误码 + 具体字段错误信息
- AC-5：Knife4j 文档页面（/doc.html）正常展示所有已实现的接口
- AC-6：文件上传通过安全校验（类型+大小+魔数），写入 file_storage
- AC-7：所有接口返回 Result 格式，包含 traceId

### 4.3 Sprint 2 — 课程 + 实训任务

**Goal**：教师能创建课程、关联班级和学生、发布实训任务。学生能看到自己的课程和任务。

**Modules**：Course、Training、Standard

**Estimated Workload**：12-15 人天

**Key Deliverables**：
- Course CRUD（含教师/学生/班级关联的原子事务）
- Course 数据隔离（教师看自己的课程，学生看自己选的课程）
- Training CRUD + 状态机（DRAFT→PUBLISHED→ENDED）
- Training 班级分发
- Training 进度统计（submittedCount/totalCount）
- Standard 三层结构 CRUD（模板→维度→规则）
- Standard 维度权重校验（总和=100%）
- Standard 模板复制

**Dependencies**：Sprint 1（基础设施 + Auth + User）

**Acceptance Criteria**：
- AC-1：教师创建课程并关联班级和学生，一个事务成功或全部回滚
- AC-2：学生看到的课程列表仅包含自己所选课程
- AC-3：教师发布任务后，对应班级的学生能在任务列表中看到
- AC-4：截止日期过后任务状态自动/手动转为 ENDED
- AC-5：评价标准维度权重之和超过或不足 100%时返回校验错误

### 4.4 Sprint 3 — 提交 + Git

**Goal**：学生能提交实训成果（ZIP 文件或 Git 仓库）。提交完成后触发异步 AI 分析。

**Modules**：Submission、Git、File（扩展：ZIP 解压）

**Estimated Workload**：12-15 人天

**Key Deliverables**：
- Submission 创建（ZIP 模式：先上传文件再创建提交）
- Submission 创建（Git 模式：先验证仓库再创建提交）
- Submission 状态机（SUBMITTED→ANALYZING→COMPLETED→REVIEWED）
- 截止日期校验 + 重提交次数限制
- Submission 查询（学生看自己的提交，教师看所授班级的提交）
- `SubmissionCompletedEvent` 发布（异步触发 AI 分析队列）
- Git URL 验证（格式+可达性+分支）
- Git 仓库克隆（60s 超时、临时目录、分析后清理）
- ZIP 文件解压与文件清单提取
- 文件下载权限校验（学生只能下载自己的文件，教师只能下载所授班级的文件）

**Dependencies**：Sprint 2（Course + Training）

**Acceptance Criteria**：
- AC-1：学生上传 ZIP 文件后创建提交，submission 和 submission_file 原子写入
- AC-2：Git 仓库 URL 有效但无权限时返回 8002 错误码
- AC-3：超过重提交次数后创建提交返回 1005 错误码
- AC-4：逾期提交标记 is_late=1
- AC-5：提交成功后 submission 状态为 SUBMITTED，并发布 SubmissionCompletedEvent

### 4.5 Sprint 4 — AI 分析引擎

**Goal**：AI 自动分析学生提交（Checkstyle 静态分析 + LLM 语义分析），生成结构化诊断报告。

**Modules**：AI

**Estimated Workload**：14-18 人天（含 LLM Prompt 调试时间）

**Key Deliverables**：
- `AIAnalysisProvider` 接口 + `DeepSeekProvider` 实现 + `OpenAICompatProvider` 实现
- `PromptManager`：模板加载、变量填充、JSON Schema 生成
- `RequestBuilder`：组装 HTTP 请求（含 API Key、System Prompt、User Content）
- `ResponseParser`：解析 LLM 返回的 JSON → 校验 JSON Schema → 提取扣分项
- `RetryStrategy`：3 次重试 + 指数退避 + 错误分类（可重试/不可重试）
- `AgentOrchestrator`：Checkstyle 静态分析 → 规则引擎 → CodeAgent → DocAgent → ReqAgent → SummaryAgent（合并去重）→ ScoreAgent（按维度计算扣分）
- `CheckstyleRunner`：调用 Checkstyle 对代码进行风格检查
- `RuleEngine`：文件存在性、目录结构、必填项规则检查
- `StreamingSupport`：SSE 推送分析进度（stage + percentage + message）
- `StructuredOutput`：JSON Schema 约束 LLM 输出格式，校验失败自动重试
- `AIAnalysisListener`：监听 `SubmissionCompletedEvent` → 更新 `ai_analysis.status=ANALYZING` → 执行分析 → 写入 `ai_analysis` + `ai_analysis_detail`

**Dependencies**：Sprint 3（Submission）

**Acceptance Criteria**：
- AC-1：提交完成后 AI 分析自动触发（异步），submission 状态由 SUBMITTED 变为 ANALYZING
- AC-2：Checkstyle 静态分析结果和 LLM 语义分析结果都被包含在诊断报告中
- AC-3：LLM 返回的 JSON 不符合 Schema 时自动重试 1 次
- AC-4：AI 调用失败 3 次后标记 analysis_status=FAILED，学生可手动重试
- AC-5：Token 用量记录到 ai_analysis 表（input/output/total）
- AC-6：前端可通过轮询 ai-result 接口获取分析进度和结果
- AC-7：低置信度（confidence<0.5）的扣分项在报告中标注

### 4.6 Sprint 5 — 教师复核 + 成绩

**Goal**：教师能查看 AI 诊断报告，逐项复核，发布最终成绩。学生收到成绩通知。

**Modules**：Review、Notification（基础版）

**Estimated Workload**：12-15 人天

**Key Deliverables**：
- Review 待复核列表（按状态+教师筛选）
- Review 详情（提交详情 + AI 诊断报告 + 复核状态 三栏数据）
- 逐项复核操作（ADOPT/REJECT/ADJUST/MANUAL_ADD）
- 成绩计算（最终分数 = 满分 - 所有复核通过的扣分项之和）
- 成绩发布（`teacher_review` + `review_item` + `score_record` + `submission.status` 原子事务）
- 成绩退回（RETURNED 状态，学生重新提交后重新进入 AI 分析流程）
- `ReviewPublishedEvent` 发布 → 异步刷新报表缓存 + 异步推送通知
- 通知列表查询 + 已读标记（`notification` 表）

**Dependencies**：Sprint 4（AI）+ Sprint 3（Submission）

**Acceptance Criteria**：
- AC-1：教师能看到 AI 各维度评分和扣分项明细，置信度低的项目突出显示
- AC-2：教师采纳/拒绝/调整扣分项后，复核状态实时更新
- AC-3：成绩发布为原子操作，四表同时成功或同时回滚
- AC-4：成绩发布后学生收到通知（notification 表写入）
- AC-5：成绩退回后学生可重新提交，重新触发 AI 分析
- AC-6：同一提交不能被两个教师同时复核（乐观锁 version 校验）

### 4.7 Sprint 6 — 报表 + 系统管理

**Goal**：教师和管理员能查看和导出报表。管理员能管理用户、班级、系统配置。

**Modules**：Report、System、Log

**Estimated Workload**：12-15 人天

**Key Deliverables**：
- 个人报表（成绩趋势 + 能力雷达数据）
- 班级报表（成绩分布 + 均分对比）
- 学院报表（跨班对比 + 学期趋势）
- PDF 导出（OpenPDF 生成，含图表图片）
- Excel 导出（EasyExcel 生成，含多 Sheet）
- 报表异步生成（GENERATING→COMPLETED/FAILED）+ MinIO 上传 + 下载 URL
- Admin Dashboard（从 statistics_snapshot 读取）
- 用户管理 CRUD（含角色分配）
- 班级管理 CRUD
- 系统配置管理（Key-Value 修改 + 乐观锁防并发覆盖）
- 操作日志查询（按模块/时间/操作人筛选+分页）
- 服务健康监控接口（数据库连接池、Redis 连通性、MinIO 连通性、磁盘空间）

**Dependencies**：Sprint 5（Review）+ Sprint 1（基础设施）

**Acceptance Criteria**：
- AC-1：导出按钮点击后异步生成，生成完成后返回下载 URL
- AC-2：Excel 报表多 Sheet 格式正确（数据 Sheet + 图表 Sheet）
- AC-3：PDF 报表含图表和文字，格式可直接打印
- AC-4：Admin 创建用户时 user+user_role 原子写入
- AC-5：系统配置修改使用乐观锁，并发修改被拒绝并提示刷新
- AC-6：操作日志按模块+时间筛选查询结果正确

### 4.8 Sprint 7 — 联调 + 测试 + 部署

**Goal**：前后端联调通过，核心业务流程端到端可跑通。Docker 部署脚本可用。

**Modules**：全模块

**Estimated Workload**：10-12 人天

**Key Deliverables**：
- 前后端联调（所有 25 个页面接口对接通过）
- 核心业务流程集成测试（登录→提交→AI 分析→复核→报表导出）
- 边界场景测试（空数据、大数据量、并发提交、Token 过期）
- Flyway 迁移脚本完整（V1 schema + V2 seed data + V3 indexes）
- application-prod.yml 配置完成
- Dockerfile + docker-compose.yml 可成功启动全套服务
- Knife4j API 文档完整（所有接口标注 Tag/Operation/Schema）
- 性能测试（Dashboard 查询 <500ms，分页查询 <200ms）

**Dependencies**：Sprint 1-6 全部

**Acceptance Criteria**：
- AC-1：端到端核心流程无阻断（MVP Definition of Done 所有检查项通过）
- AC-2：Integration Test 覆盖 Submission→AI→Review→Score 全链路
- AC-3：Docker Compose 一键启动（MySQL + Redis + MinIO + Spring Boot）
- AC-4：Knife4j 文档中所有接口均已实现并可通过 Try-It 测试

### 4.9 Sprint 8 — 优化 + 文档 + 竞赛准备

**Goal**：性能调优，文档完善，竞赛答辩准备。

**Modules**：全模块

**Estimated Workload**：8-10 人天

**Key Deliverables**：
- 慢查询优化（检查所有 SQL 的 EXPLAIN 结果）
- 缓存命中率优化（调整 TTL，增加缓存预热）
- Redis Key 命名规范全面检查
- 异常处理覆盖率审查（确保所有异常都有 ErrorCode）
- 日志脱敏审查（确保密码/Token/API Key 不出现在日志中）
- 后端架构文档终稿（与 Architecture Baseline 同步）
- 竞赛答辩 PPT 技术部分
- 演示环境部署 + 预置演示数据

**Dependencies**：Sprint 7

**Acceptance Criteria**：
- AC-1：所有慢查询（>200ms）已优化或有优化说明
- AC-2：文档与代码一致（API 文档、架构图、数据库 ER 图）
- AC-3：演示环境可在竞赛现场 5 分钟内启动

---

## 5. Development Workflow

### 5.1 Recommended Workflow Per Feature

每个 Feature 的开发遵循以下工作流（自上而下、逐层构建）：

```
Step 1: Entity + Mapper
    定义数据库实体 + MyBatis-Plus Mapper 接口
    ↓
Step 2: DTO + VO
    定义请求参数对象 + 响应视图对象
    ↓
Step 3: Convert (MapStruct)
    定义 Entity ↔ DTO/VO 自动转换接口
    ↓
Step 4: Service Interface
    定义业务逻辑接口（方法签名 + JavaDoc）
    ↓
Step 5: Service Implementation
    实现业务逻辑（参数校验→业务编排→Mapper 调用→事件发布→VO 返回）
    ↓
Step 6: Controller
    实现 REST 接口（参数绑定→@Valid 校验→Service 调用→Result 包装）
    ↓
Step 7: Unit Test
    Service 单元测试（Mock Mapper + Mock Provider）
    Controller 切片测试（MockMvc）
    ↓
Step 8: Integration Test
    端到端流程测试（@SpringBootTest + H2）
    ↓
Step 9: Code Review
    对照 Code Review Checklist 逐项检查
    ↓
Step 10: Knife4j Doc Verification
    启动应用 → 访问 /doc.html → 验证接口文档完整
```

### 5.2 Development Order Per Module

模块开发顺序遵循依赖关系：

```
Phase 1 (Blocking):
  common/ → infrastructure/ → File → Auth → User

Phase 2 (Core Business):
  Course → Standard → Training → Submission → Git

Phase 3 (Core Intelligence):
  AI → Review

Phase 4 (Value-Add):
  Report → System → Log → Notification
```

同一 Phase 内模块可并行开发（如 Phase 2 中 Course 和 Standard 可并行）。

### 5.3 Testing Strategy

| 测试层级 | 范围 | 工具 | 执行频率 |
|---|---|---|---|
| 单元测试 | Service 层（Mock 所有依赖） | JUnit 5 + Mockito | 每次提交前 |
| 切片测试 | Controller 层（MockMvc） | @WebMvcTest | 每次提交前 |
| 集成测试 | 关键业务流程 | @SpringBootTest + H2 | 每次 Push 前 |
| API 契约测试 | 与 API Mock Spec 一致性 | 手动 + Knife4j Try-It | 每个 Sprint 结束 |

### 5.4 Environment Pipeline

```
Dev (本地)          → 每个开发者的本地环境
  ↓ 通过单元测试 + 切片测试
Test (共享测试服)    → Sprint 完成后部署，联调 + 集成测试
  ↓ 通过集成测试 + 前后端联调
Staging (预发布)     → 竞赛前部署，全量回归测试
  ↓ 通过全量测试 + 性能测试
Prod (生产/演示)     → 竞赛现场 / 正式上线
```

---

## 6. Coding Standards

以下规范为强制性要求，所有后端代码必须遵守。详细完整规范参见 `docs/12-Backend-Specification.md`。本节列出核心要求。

### 6.1 Naming

| 元素 | 规则 | 示例 |
|---|---|---|
| 类名 | PascalCase | `AuthController` |
| 方法名 | camelCase | `findUserByUsername` |
| 变量名 | camelCase | `submissionId` |
| 常量 | UPPER_SNAKE_CASE | `MAX_UPLOAD_SIZE` |
| URL | kebab-case | `/api/v1/teacher/tasks` |
| DB 表 | lower_snake_case | `training_task` |
| DB 字段 | lower_snake_case | `create_time` |
| Redis Key | `域:类型:标识` | `user:info:{id}` |

### 6.2 Layering

- Controller → Service (interface) → ServiceImpl → Mapper → DB
- 禁止跨层调用
- 禁止 Entity 直接返回前端（必须 VO）
- 禁止请求参数直接传入 Mapper（必须 DTO）

### 6.3 Exception Handling

- 所有业务异常：`throw new BusinessException(ErrorCode.xxx)`
- Controller 禁止 try-catch
- Service 中校验使用：`Assert.notNull()`、`Assert.isTrue()`
- GlobalExceptionHandler 统一处理 5 类异常

### 6.4 Transaction

- 写操作：`@Transactional`
- 只读操作：`@Transactional(readOnly = true)`
- 事务内禁止：LLM API 调用、MinIO 上传、Git 克隆、文件 I/O、通知推送
- 默认隔离级别：READ_COMMITTED

### 6.5 Logging

- `@Slf4j` 注入日志对象
- INFO：关键业务节点
- ERROR：需人工介入（传入异常对象）
- 禁止记录：密码、Token、API Key、完整手机号
- 格式包含 traceId

### 6.6 Validation

- Controller 参数使用 `@Valid`
- DTO 字段使用 Jakarta Validation 注解
- 业务校验在 Service 层（Assert 断言）

### 6.7 Response Wrapper

- 所有接口返回 `Result<T>`（code + message + data + success + timestamp + traceId）
- 列表返回 `PageResult<T>`（继承 Result，增加 page/pageSize/total/totalPages）

### 6.8 DTO Conversion

- 使用 MapStruct `@Mapper(componentModel = "spring")` 接口
- 禁止手写 `BeanUtils.copyProperties`
- 禁止逐字段 set/get

---

## 7. Database Strategy

### 7.1 Entity Mapping

- `@TableName("{table_name}")` 映射数据库表
- `@TableId(type = IdType.ASSIGN_ID)` Snowflake 主键
- `@TableLogic` 逻辑删除（deleted 字段）
- `@Version` 乐观锁（version 字段）
- `@TableField(fill = FieldFill.INSERT)` / `@TableField(fill = FieldFill.INSERT_UPDATE)` 审计字段自动填充

### 7.2 Foreign Keys

**不使用数据库外键约束**。引用完整性在应用层通过以下方式保证：
- Service 层代码校验引用是否存在
- 唯一约束使用 UNIQUE INDEX（如 `uk_username`、`uk_submission_user_task`）
- 定期数据一致性检查脚本

### 7.3 Indexes

所有索引定义在 Flyway 迁移脚本中。索引命名：
- 普通索引：`idx_{字段名}`
- 联合索引：`idx_{字段1}_{字段2}`
- 唯一索引：`uk_{字段名}`

核心查询字段（WHERE/JOIN/ORDER BY 列）必须建索引。

### 7.4 Logical Delete

- 字段名：`deleted`，类型：`TINYINT`，默认：`0`
- `@TableLogic(value = "0", delval = "1")`
- 适用范围：所有主数据表
- 不适用范围：关联表（物理删除）、日志表（不可删除）

### 7.5 Audit Fields

所有业务表包含：
- `create_time` DATETIME — INSERT 自动填充
- `update_time` DATETIME — INSERT + UPDATE 自动填充
- `create_by` BIGINT — INSERT 自动填充（Sa-Token 当前用户）
- `update_by` BIGINT — UPDATE 自动填充（Sa-Token 当前用户）

实现：`MyMetaObjectHandler implements MetaObjectHandler`

### 7.6 Flyway Migration

数据库版本管理使用 Flyway：
- `V1__init_schema.sql` — 全部 26 张表 DDL
- `V2__init_data.sql` — 预置数据（3 角色 + 1 管理员 + 系统配置）
- `V3__add_indexes.sql` — 全部索引创建

所有 DDL 变更通过 Flyway 脚本执行，禁止手动修改数据库结构。

---

## 8. AI Integration Strategy

### 8.1 Architecture

```
Business Module (Submission)
    ↓ 发布 SubmissionCompletedEvent
AI Module (Listener)
    ↓
AgentOrchestrator
    ├── StaticAnalyzer (Checkstyle)
    ├── RuleEngine (file/dir/rules)
    └── LLM Pipeline
        ├── PromptManager (template → filled prompt)
        ├── RequestBuilder (HTTP request + JSON Schema)
        ├── AIAnalysisProvider (DeepSeek / OpenAI compat)
        ├── ResponseParser (JSON → structured result)
        └── RetryStrategy (3 retries + backoff)
    ↓
ScoreAgent (dimension scoring)
    ↓
DB: ai_analysis + ai_analysis_detail
```

### 8.2 Prompt Management

**存储位置**：`src/main/resources/ai/prompts/`

**文件组织**：
```
ai/prompts/
├── system/
│   ├── code-agent.txt        # CodeAgent 系统指令
│   ├── doc-agent.txt         # DocAgent 系统指令
│   └── req-agent.txt         # ReqAgent 系统指令
├── tasks/
│   ├── code-analysis.txt     # 代码分析任务 Prompt
│   ├── doc-analysis.txt      # 文档分析任务 Prompt
│   └── req-analysis.txt      # 需求分析任务 Prompt
└── schemas/
    ├── deduction-output.json  # 扣分项输出 JSON Schema
    └── summary-output.json    # 综合分析输出 JSON Schema
```

**模板变量**：
- `{submissionContent}` — 学生提交的代码/文档内容
- `{taskRequirement}` — 任务要求描述
- `{standardJson}` — 评价标准（维度+规则，JSON 格式）
- `{language}` — 编程语言（java/python/c/cpp）
- `{staticAnalysisResult}` — Checkstyle 静态分析结果

### 8.3 LLM Abstraction

`AIAnalysisProvider` 接口方法：
- `chat(prompt, modelName)` → 自由文本对话
- `chatWithJsonSchema(prompt, jsonSchema, modelName)` → 结构化 JSON 输出
- `estimateTokens(text)` → Token 估算
- `healthCheck()` → 模型可用性检查

**Provider 切换**：通过 `b1.ai.provider` 配置项（deepseek / openai-compat），启动时加载对应 Provider Bean。

### 8.4 Retry Strategy

| 错误类型 | 是否重试 | 最大重试次数 | 退避策略 |
|---|---|---|---|
| 网络超时 (read timeout) | 是 | 3 | 指数退避 (1s→2s→4s) |
| 服务端 5xx | 是 | 3 | 指数退避 |
| 速率限制 429 | 是 | 3 | 指数退避 + 额外等 Retry-After |
| 认证失败 401 | 否 | 0 | — |
| 参数错误 400 | 否 | 0 | — |
| JSON Schema 校验失败 | 是 | 1 | 立即重试 |

### 8.5 Token Management

- 调用前：`estimateTokens()` 估算输入 Token → 超过模型上下文 80% 时分批处理
- 调用后：记录实际 Token 用量到 `ai_analysis.token_input/output/total`
- 定期统计 Token 消耗总量，评估成本

### 8.6 Structured Output

- LLM 输出通过 JSON Schema 约束格式
- JSON Schema 在 `resources/ai/prompts/schemas/` 中定义
- `ResponseParser` 负责：
  1. 解析 LLM 返回的 JSON 字符串
  2. 对照 JSON Schema 校验必填字段和类型
  3. 校验失败 → 自动重试 1 次
  4. 仍然失败 → 标记 confidence=0，返回部分结果

---

## 9. Git Integration Strategy

### 9.1 Validation Flow

```
学生在提交页面输入 Git URL
    ↓
POST /api/v1/student/tasks/{taskId}/git-verify
    ↓
GitService.verify(gitUrl, branch, accessToken)
    ├── Step 1: URL 格式校验（必须是 HTTPS URL）
    ├── Step 2: JGit LsRemoteCommand 可达性检查
    ├── Step 3: 分支存在性检查
    └── Step 4: 返回仓库元信息（repoName, branches[], latestCommit）
    ↓
返回 GitVerifyResult { valid, repoName, defaultBranch, branches[], latestCommit }
```

### 9.2 Clone Flow

```
AI 分析模块触发
    ↓
GitService.clone(gitUrl, branch, targetDir)
    ├── Step 1: 检查磁盘空间（至少 1GB 可用）
    ├── Step 2: JGit CloneCommand
    │   ├── setURI(gitUrl)
    │   ├── setBranch(branch)
    │   ├── setDirectory(targetDir)
    │   ├── setTimeout(60s)
    │   └── setCredentialsProvider (if accessToken)
    ├── Step 3: 克隆成功 → 返回仓库文件列表
    └── Step 4: 克隆失败 → GitException + 清理部分下载文件
```

### 9.3 Cleanup Strategy

- 克隆目录：`{b1.git.clone-dir}/{userId}/{submissionId}/`
- AI 分析完成后立即删除克隆目录（在 `AIAnalysisListener` 中 finally 块执行）
- 定时任务（每 1 小时）：扫描 `b1.git.clone-dir/` 下超过 2 小时未删除的目录并清理
- 目录权限：700（仅应用用户可访问）

### 9.4 Security

- 不存储用户 Git 密码（仅支持 HTTPS Token 方式，Token 不持久化到数据库）
- Git URL 在日志中脱敏（移除 Token 部分）
- 克隆的代码文件不暴露通过 HTTP 直接访问
- 克隆目录位于 `/tmp` 子目录（应用容器重启后自动清除）

---

## 10. Future Expansion

### 10.1 RAG Knowledge Base（v1.1）

**目标**：将实训课程文档、代码规范手册、优秀案例库索引化，增强 AI 分析的知识锚定能力。

**新增模块**：`module/knowledge/`

**新增表**：`knowledge_base`、`knowledge_document`、`document_chunk`

**流程**：
```
文档上传 → 文本提取（Tika/PDFBox）→ 分块（512 tokens + 64 overlap）
→ Embedding（向量化）→ 存储（PGVector / Milvus）
→ 检索：用户查询 → Embedding → 向量相似度 → Top-K → 注入 AI Prompt
```

**接口预留**：
- `POST /api/v1/knowledge/documents` — 上传知识文档
- `GET /api/v1/knowledge/search` — 检索知识片段
- `DELETE /api/v1/knowledge/documents/{id}` — 删除文档

### 10.2 AI Agent Extension（v1.1）

**新增 Agent**：
- `SecurityAgent`：代码安全漏洞检测（SQL 注入、XSS、硬编码密钥）
- `PlagiarismAgent`：代码/报告相似度检测（JPlag / MOSS 集成）
- `TutorAgent`：基于扣分项生成个性化学习建议和修复方案

**Agent 热插拔**：通过 Spring Bean 动态注册/移除 Agent，`AgentOrchestrator` 自动发现并调用。

### 10.3 Knowledge Graph（v2.0）

**目标**：基于学生历史提交和扣分明细构建知识点掌握图谱。

**新增表**：`knowledge_point`、`student_knowledge_state`、`knowledge_point_relation`

**可视化**：前端 ECharts 力导向图展示知识点掌握程度。

### 10.4 Microservice Evolution（v3.0）

**触发条件**：团队规模 > 8 人、单模块代码量 > 10000 行、需要独立扩缩容。

**拆分路径**：
1. AI 模块 → 独立 Python FastAPI 服务
2. File 模块 → 独立文件服务
3. Report 模块 → 独立报表服务
4. 其余模块按业务域拆分

**当前预留**：每个模块有独立的 controller/service/mapper 包，拆分时仅需新建应用并引入模块依赖。

### 10.5 MCP Protocol（v2.1+）

**目标**：将 AI 分析能力通过 MCP（Model Context Protocol）暴露，支持 AI 编码助手直接调用分析能力。

**预留接口**：`AIAnalysisProvider` 的 `chat` 和 `chatWithJsonSchema` 方法天然适配 MCP Tool 契约。

---

## 11. Risks

### 11.1 Technical Risks

| 风险 | 概率 | 影响 | 缓解措施 |
|---|---|---|---|
| **LLM API 不稳定** | 中 | 高 | 多 Provider 切换（DeepSeek → OpenAI Compat）；3 次重试 + 指数退避；失败不阻塞主流程 |
| **LLM 输出格式不符合预期** | 高 | 中 | JSON Schema 严格约束；输出校验 + 自动重试 1 次；失败后 confidence=0 降级处理 |
| **AI 分析 Token 成本过高** | 中 | 中 | Token 估算 + 80% 阈值分批；Checkstyle 静态分析前置（免费）；定期成本统计 |
| **大文件上传超时** | 低 | 中 | Nginx + Spring Boot 超时配置；前端分片上传（P1）；50MB 上限 |
| **数据库连接池耗尽** | 低 | 高 | HikariCP 最大 20 连接；事务不包含外部 API 调用；30s 超时 |
| **Redis 缓存雪崩** | 低 | 中 | TTL 加随机偏移（±10%）；热点 Key 永不过期 + 主动更新；降级查 DB |
| **Git 仓库克隆超时** | 中 | 低 | 60s 超时配置；克隆前检查网络可达性；异常映射为明确错误码 |
| **前后端接口对接偏差** | 中 | 中 | 严格遵循 API Mock Specification；Knife4j 文档实时可查；Sprint 7 联调周 |
| **银河麒麟 + LoongArch 兼容性** | 中 | 高 | JGit 在 ARM 架构测试；Docker 镜像支持 ARM64；MinIO Client 兼容性验证；提前在目标环境部署测试 |

### 11.2 Schedule Risks

| 风险 | 概率 | 影响 | 缓解措施 |
|---|---|---|---|
| **AI 模块开发超时** | 高 | 高 | Prompt 调试预留 4-6 人天缓冲（Sprint 4 上限 18 人天）；可先用静态分析结果演示部分 AI 能力 |
| **联调期间大量 Bug** | 中 | 中 | 每个 Sprint 结束时做模块内集成测试；Mock 数据结构与真实 API 一致 |
| **人员变动** | 低 | 高 | 文档驱动开发（Codex 可从文档直接生成代码）；模块独立性强（可单独交接） |

### 11.3 Competition Risks

| 风险 | 概率 | 影响 | 缓解措施 |
|---|---|---|---|
| **演示环境启动失败** | 低 | 高 | Docker Compose 一键启动；预置演示数据；现场备用本地环境 |
| **AI 分析演示时间过长** | 中 | 中 | 预先生成演示用的分析结果并缓存；演示时直接展示已完成的分析 |
| **网络环境无外网访问** | 低 | 高 | 准备离线 Mock 模式（Mock AI 返回预置的 JSON 结果） |

---

## Appendix A: Architecture Synchronization Notes

根据 Architecture Baseline v2.0 的发布，以下文档中的部分条款已被本 BIP 和 Architecture Baseline 更新。这些文档应在后续有时间时同步，但同步不影响新基线即刻生效。

| 文档 | 冲突/过期内容 | 应更新为 |
|---|---|---|
| `10-Backend-Architecture-Design.md` Appendix C | Apache POI 5.x | EasyExcel（最新） |
| `10-Backend-Architecture-Design.md` §6.2.6 | 静态分析：Checkstyle/PMD/SpotBugs | Checkstyle only |
| `10-Backend-Architecture-Design.md` Appendix C | iText / OpenPDF | OpenPDF |
| `10-Backend-Architecture-Design.md` §7.2 | 权限注解包含 `@SaCheckOr`、`@SaCheckPermission` | MVP 仅使用 `@SaCheckRole`。`@SaCheckPermission` 和组合注解为 P1 |
| `10-Backend-Architecture-Design.md` §13.5 | JDK 21 Virtual Threads 配置 | 确认 Spring Boot 3.5 默认虚拟线程配置。如已默认启用，移除显式配置 |
| `12-Backend-Specification.md` §17.7 (Appendix A) | 引用 PMD/SpotBugs | 移除 PMD/SpotBugs，仅保留 Checkstyle |
| `12-Backend-Specification.md` Appendix B | Apache POI、iText/OpenPDF | EasyExcel、OpenPDF |
| `12-Backend-Specification.md` §3.1 AI Module | 静态分析目录 `staticanalysis/` 含 PMD | 简化为仅 CheckstyleRunner |
| `engineering/ADR.md` | 缺少 EasyExcel 替换 POI 的决策记录 | 建议新增 ADR-012 |
| `engineering/ADR.md` | 缺少 Checkstyle-only 简化决策记录 | 建议新增 ADR-013 |
| `02-SDS.md` §1.1-1.4 | 架构图仍显示 Python AI Gateway | 改为 Java-native AI 调用。Python 服务推迟至 v1.1 |
| `02-SDS.md` §1.1 | 架构图显示 Gateway 微服务层 | 改为 Modular Monolith 无网关层 |

---

## Appendix B: Document Cross-Reference

| BIP 章节 | 关联文档 | 关联内容 |
|---|---|---|
| 1. Project Goals | MVP, PRD §1-5 | 功能范围、角色定义 |
| 2. Architecture | Backend Architecture Design §3-5, Architecture Baseline §3 | 架构模式、分层设计、技术栈 |
| 3. Module Breakdown | Backend Architecture Design §6, API Mock Specification §5-12 | 模块职责、核心 API |
| 4. Sprint Planning | FIP §11, SPRINT-SPEC-GUIDE | Sprint 计划方法 |
| 5. Development Workflow | Backend Specification §2-5, DEVELOPMENT-WORKFLOW | 开发流程、分层构建 |
| 6. Coding Standards | Backend Specification §4-22, Architecture Baseline §6 | 编码规范 |
| 7. Database Strategy | Database Design §2-8, Architecture Baseline §6.9 | 数据库设计、Entity 映射 |
| 8. AI Integration | Backend Architecture Design §6.2.6, Architecture Baseline §3.7 | AI 架构、Provider 抽象 |
| 9. Git Integration | Backend Architecture Design §6.2.12 | Git Module 设计 |
| 10. Future Expansion | Backend Architecture Design §14, MVP §2 | 扩展路线图 |
| 11. Risks | 无直接关联 | 全新内容 |

---

*本后端实施计划遵循 Architecture Baseline v2.0。当本文档与其他文档冲突时，以 Architecture Baseline 为准。所有后端开发以本文档为执行依据。*
