# Architecture Baseline v2.0

## 基于大模型的软件实训教学检查评价与报表系统

---

**文档性质**：本项目最高优先级技术权威文档。所有其他文档、所有开发人员、所有 AI 助手（Claude、Codex）必须遵循本基线。

**冲突解决规则**：当任何文档与本基线冲突时，以本基线为准。禁止以其他文档为依据推翻本基线中的决策。

**更新规则**：本基线仅在首席架构师明确授权下可修改。任何其他人不得修改。

---

## 1. Document Identity

| 字段 | 值 |
|---|---|
| **文档名称** | Architecture Baseline（架构基线） |
| **文档版本** | v2.0 |
| **文档状态** | Authoritative（权威发布） |
| **作者** | Lead Backend Architect |
| **最后更新** | 2026-07-04 |
| **适用范围** | 项目全生命周期。所有 Sprint、所有模块、所有文档 |
| **优先级** | 最高。覆盖所有其他文档中的冲突条款 |

---

## 2. Project Identity

| 属性 | 值 |
|---|---|
| 项目名称 | 基于大模型的软件实训教学检查评价与报表系统 |
| 项目代号 | B1 Platform |
| 架构模式 | Modular Monolith（模块化单体） |
| 开发方法 | AI Native Development（Claude 规划 + Codex 实现） |
| 开发阶段 | 全产品开发（Post-MVP） |
| 部署平台 | Docker + 银河麒麟 + LoongArch |

---

## 3. Final Technology Stack

### 3.1 Core Framework

| 技术 | 版本 | 用途 | 状态 |
|---|---|---|---|
| Java | 21 (LTS) | 运行环境 | 必选 |
| Spring Boot | 3.5.x | 应用框架 | 必选 |
| Maven | 3.9+ | 构建与依赖管理 | 必选 |

### 3.2 Persistence & Cache

| 技术 | 版本 | 用途 | 状态 |
|---|---|---|---|
| MySQL | 8.0 | 主数据库 | 必选 |
| MyBatis-Plus | 3.5.x | ORM / CRUD 增强 | 必选 |
| Redis | 7.x | 缓存 / Token 存储 / 分布式锁 | 必选 |
| Spring Cache | (内置) | 声明式缓存抽象 | 必选 |

### 3.3 Authentication & API

| 技术 | 版本 | 用途 | 状态 |
|---|---|---|---|
| Sa-Token | 1.38+ | 认证与权限框架 | 必选 |
| Knife4j | 4.x | OpenAPI 3.0 文档自动生成 | 必选 |
| Jakarta Validation | (内置) | 参数校验 | 必选 |

### 3.4 Data Mapping & Utilities

| 技术 | 版本 | 用途 | 状态 |
|---|---|---|---|
| MapStruct | 1.5.x | Entity ↔ VO 对象映射 | 必选 |
| Lombok | 1.18.x | 样板代码消除 | 必选 |
| Hutool | 5.8.x | 通用工具集 | 必选 |
| Jackson | (内置) | JSON 序列化/反序列化 | 必选 |

### 3.5 Storage & Messaging

| 技术 | 版本 | 用途 | 状态 |
|---|---|---|---|
| MinIO | (服务端) | 对象存储 | 必选 |
| MinIO Client | 8.x | MinIO Java SDK | 必选 |

### 3.6 Async & Scheduling

| 技术 | 版本 | 用途 | 状态 |
|---|---|---|---|
| Spring @Async | (内置) | 异步任务处理 | 必选 |
| Spring @Scheduled | (内置) | 定时任务调度 | 必选 |

### 3.7 AI & Analysis

| 技术 | 版本 | 用途 | 状态 |
|---|---|---|---|
| OpenAI Compatible API | — | LLM 调用协议 | 必选 |
| Prompt Engine | 自研 | Prompt 模板管理与组装 | 必选 |
| JGit | 6.x | Git 仓库操作 | 必选 |
| Checkstyle | (最新) | 代码风格静态分析 | 必选 |

### 3.8 Reporting

| 技术 | 版本 | 用途 | 状态 |
|---|---|---|---|
| EasyExcel | (最新) | Excel 报表生成与导出 | 必选 |
| OpenPDF | (最新) | PDF 报表生成与导出 | 必选 |

### 3.9 Testing

| 技术 | 版本 | 用途 | 状态 |
|---|---|---|---|
| JUnit 5 | (内置) | 单元测试框架 | 必选 |
| Mockito | 5.x | Mock 测试框架 | 必选 |
| H2 | (最新) | 测试用内存数据库 | 必选 |

### 3.10 DevOps

| 技术 | 版本 | 用途 | 状态 |
|---|---|---|---|
| Docker | 24+ | 容器化部署 | 必选 |
| Flyway | 9.x | 数据库版本迁移 | 必选 |
| SLF4J + Logback | (内置) | 日志框架 | 必选 |

---

## 4. Final Role Model

| 角色 | 角色码 | 权限范围 | 说明 |
|---|---|---|---|
| 学生 | `student` | 仅自己的数据 | 查看任务、提交成果、查看个人成绩与报告 |
| 教师 | `teacher` | 所授班级 + 全院统计 | 管理课程/任务/标准、复核评分、查看报表（含原科研负责人职能） |
| 管理员 | `admin` | 全部数据 | 用户管理、班级管理、系统配置、日志审计、系统监控 |

**角色数量**：3 个。教研负责人角色已合并至教师角色（ADR-011）。不得新增角色。

**API 路径前缀**：
- `/api/v1/auth/**` — 公开
- `/api/v1/user/**` — 已登录
- `/api/v1/student/**` — Student only
- `/api/v1/teacher/**` — Teacher only
- `/api/v1/admin/**` — Admin only
- `/api/v1/files/**` — 已登录（下载需权限校验）

---

## 5. Final Directory Structure

### 5.1 Top-Level

```
server/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── src/
    ├── main/
    │   ├── java/com/b1/
    │   │   ├── B1Application.java
    │   │   ├── common/          # 公共基础设施
    │   │   ├── module/          # 业务模块（14个）
    │   │   └── infrastructure/  # 基础设施实现
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-test.yml
    │       ├── application-prod.yml
    │       ├── mapper/          # MyBatis XML（复杂查询）
    │       └── db/migration/    # Flyway 迁移脚本
    └── test/
        └── java/com/b1/
```

### 5.2 Common Infrastructure

```
common/
├── config/          # Spring 配置类（SaToken、MyBatisPlus、Redis、MinIO、WebMvc、Knife4j、Async、Jackson）
├── exception/       # BusinessException、ErrorCode 枚举、GlobalExceptionHandler、Assert
├── result/          # Result<T>、PageResult<T>
├── interceptor/     # LogInterceptor、RateLimitInterceptor
├── aspect/          # OperationLogAspect、AILogAspect
├── util/            # 无状态工具类（FileUtil、ZipUtil、Md5Util、SnowflakeUtil）
├── constant/        # RedisKeys、SystemConstants
├── enums/           # 公共枚举（RoleEnum、SubmissionStatus、AnalysisStatus）
└── validation/      # 自定义校验注解
```

### 5.3 Business Modules

```
module/
├── auth/            # 认证模块
├── user/            # 用户模块
├── course/          # 课程模块
├── training/        # 实训任务模块
├── submission/      # 提交模块
├── ai/              # AI 分析模块
├── review/          # 教师复核模块
├── report/          # 报表模块
├── standard/        # 评价标准模块
├── system/          # 系统管理模块
├── file/            # 文件模块
├── git/             # Git 模块
├── log/             # 日志模块
└── notification/    # 通知模块
```

### 5.4 Each Module Internal Structure

```
module/{name}/
├── controller/      # REST 控制器
├── service/         # 业务接口
│   └── impl/        # 业务实现
├── mapper/          # MyBatis Mapper
├── entity/          # 数据库实体
├── dto/             # 请求 DTO
├── vo/              # 响应 VO
├── convert/         # MapStruct Converter
├── event/           # Spring Event（如有）
└── listener/        # Event Listener（如有）
```

### 5.5 Infrastructure

```
infrastructure/
├── security/        # SaTokenConfig、StpInterfaceImpl、GlobalSaTokenListener
├── persistence/     # MyMetaObjectHandler、MyBatisPlusConfig
├── redis/           # RedisTemplateConfig、RedisCacheManager
└── minio/           # MinioProperties
```

---

## 6. Final Coding Standards

### 6.1 Layering Rules

```
Controller → Service (interface) → ServiceImpl → Mapper → Database
```

| 规则 | 说明 |
|---|---|
| Controller 不调 Mapper | 必须通过 Service |
| Service 不调 Controller | 单向依赖 |
| Mapper 不调 Service | 单向依赖 |
| Entity 不返回前端 | 通过 VO 返回 |
| 请求不直接用 Entity | 通过 DTO 接收 |

### 6.2 Naming Conventions

| 元素 | 规则 | 示例 |
|---|---|---|
| 包名 | 全小写单数 | `com.b1.module.course.controller` |
| 类名 | PascalCase | `AuthController` |
| 方法名 | camelCase | `findUserByUsername` |
| URL 路径 | kebab-case | `/api/v1/teacher/tasks/:taskId` |
| 数据库表 | lower_snake_case | `training_task` |
| 数据库字段 | lower_snake_case | `create_time` |

### 6.3 File Naming

| 类型 | 模式 | 示例 |
|---|---|---|
| Controller | `{Entity}Controller.java` | `CourseController.java` |
| Service 接口 | `{Entity}Service.java` | `CourseService.java` |
| Service 实现 | `{Entity}ServiceImpl.java` | `CourseServiceImpl.java` |
| Mapper | `{Entity}Mapper.java` | `CourseMapper.java` |
| Entity | `{TableName}.java` (PascalCase) | `TrainingTask.java` |
| DTO | `{Action}{Entity}Request.java` | `CreateCourseRequest.java` |
| VO | `{Entity}{View}VO.java` | `CourseDetailVO.java` |
| Convert | `{Entity}Convert.java` | `CourseConvert.java` |

### 6.4 Method & Class Constraints

| 约束 | 上限 |
|---|---|
| 方法体行数 | 50 行 |
| Service 实现类行数 | 300 行 |
| Controller 类行数 | 200 行 |
| 方法参数数量 | 4 个 |
| Mapper 接口行数 | 50 行 |

### 6.5 General Principles

- **SOLID** — 每个类单一职责；接口隔离；依赖倒置
- **KISS** — 优先使用框架内置能力；一个方法只做一件事
- **DRY** — 相同逻辑出现 3 次必须抽取
- **YAGNI** — 不为"可能"需要的功能提前写代码
- **Clean Code** — 命名即文档；不注释"做什么"（代码已表达）；注释"为什么"

### 6.6 Response Format

所有 API 返回 `Result<T>`：

```json
{
  "code": 0,
  "message": "success",
  "data": { },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

分页返回 `PageResult<T>`（继承 Result，含 page/pageSize/total/totalPages）。

### 6.7 Exception Handling

- 所有业务异常 → `BusinessException(ErrorCode.xxx)`
- 全局统一处理 → `GlobalExceptionHandler`（`@RestControllerAdvice`）
- 错误码范围：0=成功，1000-9999=错误（按业务域分段）
- Controller 禁止 try-catch

### 6.8 Transaction Rules

| 规则 | 说明 |
|---|---|
| 写操作必须 | `@Transactional` |
| 事务内禁止 | 调用外部 API（LLM、MinIO、Git） |
| 事务内禁止 | 文件 I/O |
| 默认隔离级别 | READ_COMMITTED |
| 默认超时 | 30s |

### 6.9 Database Rules

| 规则 | 值 |
|---|---|
| 主键策略 | Snowflake BIGINT（`IdType.ASSIGN_ID`） |
| 逻辑删除 | `deleted` TINYINT + `@TableLogic` |
| 乐观锁 | `version` INT + `@Version` |
| 审计字段 | `create_time` / `update_time` / `create_by` / `update_by` |
| 无外键约束 | 应用层保证引用完整性 |
| 枚举存储 | VARCHAR(32) 存储枚举名称 |

### 6.10 Redis Key Convention

`{业务域}:{实体类型}:{标识符}`

| 示例 | 说明 |
|---|---|
| `user:info:{userId}` | 用户信息缓存 |
| `token:access:{uuid}` | Access Token |
| `standard:{standardId}` | 评价标准缓存 |
| `ai:status:{submissionId}` | AI 分析状态 |

所有 Key 必须设置 TTL。

### 6.11 Logging Rules

- 使用 `@Slf4j`（Lombok）
- INFO：关键业务节点（登录、提交、发布）
- ERROR：需要人工介入的异常
- 禁止记录：密码、Token、API Key、完整手机号
- 格式：`%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId}] %-5level %logger{50} - %msg%n`

---

## 7. Technologies Explicitly NOT Allowed

以下技术在项目全生命周期中明确禁止引入。此列表具有排他性——不在允许列表中的技术默认禁止。

### 7.1 Never Allowed

| 禁止技术 | 理由 |
|---|---|
| Spring Security | 已由 Sa-Token 替代。比 Sa-Token 重，配置复杂 |
| Spring Cloud / Dubbo | 模块化单体不需要微服务框架 |
| OpenFeign | 单体架构中模块间本地调用，无需 RPC |
| Quartz | Spring @Scheduled 满足所有定时任务需求 |
| gRPC | 单体架构不需要 |
| Apache Shiro | 与 Sa-Token 功能重叠 |
| JJWT / Nimbus JOSE | Sa-Token 内置 Token 管理 |
| Elasticsearch | MVP 不需要全文检索（P2 评估） |
| RabbitMQ / Kafka | 模块间通过 Spring Event 通信，无需消息队列 |
| MongoDB | 全部数据使用 MySQL，无文档型存储需求 |
| Neo4j | 知识图谱为 P2 功能，不在当前技术栈 |

### 7.2 P1/P2 Consideration Only (NOT MVP)

| 技术 | 触发条件 |
|---|---|
| Spring AI | v1.1：作为 LLM 调用统一抽象层引入 |
| Elasticsearch | v2.1：知识库全文检索需求 |
| Milvus / PGVector | v2.2：RAG 向量检索需求 |
| Spring Cloud | v3.0：微服务拆分后引入 |
| Kubernetes | v3.0：多服务编排需求 |

### 7.3 Deprecated from Previous Specs

| 技术 | 原文档 | 替代 |
|---|---|---|
| Apache POI | Backend Architecture Appendix C | EasyExcel |
| PMD | Backend Architecture §6.2.6 | Checkstyle only |
| SpotBugs | Backend Architecture §6.2.6 | Checkstyle only |
| iTextPDF | Backend Architecture Appendix C | OpenPDF |

---

## 8. Authority & Compliance

### 8.1 Document Hierarchy

```
Architecture Baseline (this document)
    ├── Backend Architecture Design
    ├── Database Design
    ├── Backend Specification
    ├── Backend Implementation Plan
    ├── API Mock Specification
    └── Sprint Specifications
```

当任何下层文档与本基线冲突时，必须修改下层文档以符合本基线。禁止以"某文档已经写了"为由保留与本基线冲突的条款。

### 8.2 Compliance for AI Assistants

Claude、Codex、及未来所有 AI 编码助手在执行任何任务前必须：

1. 首先阅读本文档（`docs/00-Architecture-Baseline.md`）
2. 如果发现任何其他文档与本基线冲突，以本基线为准
3. 不再向用户询问"技术选型 A 还是 B"——答案在本基线中
4. 如果本基线未覆盖某个技术选型，使用 Spring Boot 生态标准方案，并向用户提议更新本基线

### 8.3 Amendment Process

1. 仅首席架构师可提议修改本基线
2. 修改前必须评估对所有下游文档的影响
3. 修改后必须在 Revision History 中记录
4. 修改后必须标记需要同步的下游文档

---

## Appendix A: Revision History

| 版本 | 日期 | 作者 | 变更说明 |
|---|---|---|---|
| v1.0 | 2026-07-04 | Lead Backend Architect | 初始版本（隐含在 Backend Architecture Design 中） |
| v2.0 | 2026-07-04 | Lead Backend Architect | 正式基线发布。明确技术栈、角色模型、目录结构、编码标准、禁止技术列表。取代 Backend Architecture Design 中冲突条款 |

---

## Appendix B: Documents Requiring Synchronization

以下文档中有条款与本基线冲突，应尽快同步（但不影响本基线的即刻生效）：

| 文档 | 冲突点 | 需要同步的内容 |
|---|---|---|
| `10-Backend-Architecture-Design.md` | 多处 | 技术栈附录：POI→EasyExcel；PMD/SpotBugs→Checkstyle only；iTextPDF→OpenPDF；Sa-Token 为唯一认证方案 |
| `12-Backend-Specification.md` | §17.7 | 静态分析工具列表：移除 PMD/SpotBugs 引用，仅保留 Checkstyle |
| `engineering/ADR.md` | 无直接冲突 | 建议新增 ADR-012 记录 EasyExcel 替换 POI、ADR-013 记录 Checkstyle-only 静态分析策略 |
| `11-Database-Design.md` | 无冲突 | 数据库设计与本基线完全一致，无需同步 |

---

*本文档是 B1 Platform 项目的最高技术权威。所有代码、所有文档、所有决策最终追溯至本基线。*
