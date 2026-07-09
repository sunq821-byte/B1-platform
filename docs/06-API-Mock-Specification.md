# API Mock Specification v1.0

## 基于大模型的软件实训教学检查评价与报表系统

| 文档信息 | |
|---|---|
| 文档版本 | v1.0 |
| 创建日期 | 2026-06-30 |
| 文档状态 | 正式发布 |
| 撰写人 | 架构团队 |
| 适用范围 | Mock.js / Axios / Spring Boot / OpenAPI 共同数据契约 |

---

## 目录

1. [API 设计原则](#第一章-api-设计原则)
2. [统一请求规范](#第二章-统一请求规范)
3. [统一响应格式](#第三章-统一响应格式)
4. [统一错误码规范](#第四章-统一错误码规范)
5. [认证接口](#第五章-认证接口)
6. [学生端接口](#第六章-学生端接口)
7. [教师端接口](#第七章-教师端接口)
8. [教师扩展接口（原教研负责人职能）](#第八章-教师扩展接口)
9. [管理员接口](#第九章-管理员接口)
10. [AI 接口](#第十章-ai-接口)
11. [文件上传接口](#第十一章-文件上传接口)
12. [Git 接口](#第十二章-git-接口)
13. [分页规范](#第十三章-分页规范)
14. [Mock 数据规范](#第十四章-mock-数据规范)
15. [AI Mock 规范](#第十五章-ai-mock-规范)
16. [图表 Mock 规范](#第十六章-图表-mock-规范)
17. [权限 Mock 规范](#第十七章-权限-mock-规范)
18. [接口命名规范](#第十八章-接口命名规范)
19. [接口开发顺序](#第十九章-接口开发顺序)
20. [API Checklist](#第二十章-api-checklist)

---

## 术语约定

| 术语 | 说明 |
|------|------|
| 资源 | RESTful 架构中的核心抽象，对应业务实体，如 tasks、students、courses |
| 集合资源 | 资源列表，URL 为名词复数，如 /api/v1/tasks |
| 单体资源 | 单个资源实例，URL 含唯一标识符，如 /api/v1/tasks/{taskId} |
| 子资源 | 从属于父资源的资源，通过 URL 层级表达，如 /api/v1/classes/{classId}/students |
| DTO | Data Transfer Object，接口传输的数据结构 |
| JWT | JSON Web Token，本系统认证令牌格式 |
| RBAC | Role-Based Access Control，基于角色的访问控制模型 |
| SSE | Server-Sent Events，服务器向客户端推送实时数据的技术 |
| 幂等性 | 多次相同请求产生相同结果，不产生额外副作用 |
| Snowflake | Twitter 开源的分布式 ID 生成算法，用于生成全局唯一 ID |
| Mock | 模拟数据，前端开发阶段替代真实后端接口 |
| Token | 大模型 API 调用中的计费单元，非认证令牌的 Token |

---

## 前置依赖文档

本文档基于以下已完成的文档编写，接口设计遵循这些文档中的业务逻辑、角色定义与功能范围：

- 《PRD v2.0》——产品需求规格说明书
- 《SDS v1.0》——系统设计说明书
- 《UI Design System v1.0》——UI 设计系统
- 《Component Library v1.0》——组件库规范
- 《Frontend Specification v1.0》——前端开发规范

---

---

# 第一章 API 设计原则

## 1.1 章节概述

本章定义本项目所有 API 设计必须遵循的 13 项核心原则。这些原则不是可选的"最佳实践建议"，而是强制性的设计约束。任何新增或修改的接口都必须通过这 13 项原则的检查。本章每一条原则均阐明"是什么"和"为什么采用"，确保团队成员理解原则背后的设计意图，而非机械执行规则。

## 1.2 原则一：RESTful 风格

### 是什么

REST（Representational State Transfer）是一种面向资源的架构风格。在本项目中，RESTful 的核心思想是：URL 代表资源（名词），HTTP 方法代表对资源的操作（动词），资源的状态通过表述（Representation）在客户端和服务器之间传递。系统不维护客户端会话状态，每次请求必须包含服务器理解请求所需的全部信息。

### 为什么采用

**原因一：前后端分离架构的天然适配**。本项目采用 Vue 3 前端 + Spring Boot 后端的 B/S 架构，前后端之间通过 HTTP 协议通信。RESTful 的无状态特性完美适配这种架构——前端无需维护服务端 Session，后端无需管理客户端状态，双方通过 Token 进行身份传递。

**原因二：降低团队沟通成本**。RESTful 的"资源 + 方法"模型具有高度可预测性。前端开发人员不需要查阅文档就能推断接口行为：GET /api/v1/tasks 查询任务列表，POST /api/v1/tasks 创建任务，DELETE /api/v1/tasks/{id} 删除任务。这种一致性大幅减少前后端联调时的误解。

**原因三：接口文档可自动生成**。RESTful 接口的结构化程度高，Spring Boot 可以通过注解自动生成 OpenAPI 文档，前端可以基于文档自动生成 TypeScript 类型定义和 Axios 调用代码。如果接口风格不统一（例如某些接口用 POST 做查询、URL 中包含动词），这些自动化工具将无法正常工作。

**原因四：与 HTTP 基础设施兼容**。CDN 缓存、反向代理、负载均衡器、API 网关等基础设施天然理解 HTTP 方法的语义。GET 请求可以被缓存，PUT 和 DELETE 请求不会被缓存，POST 请求不会被重复提交。遵循 RESTful 规范意味着这些基础设施可以零配置介入。

## 1.3 原则二：统一资源命名

### 是什么

所有 API 的 URL 路径遵循一套统一的命名规则：

- URL 全部使用小写字母
- 多个单词之间使用连字符（-）分隔，即 kebab-case
- 资源名使用名词复数形式
- 子资源通过 URL 路径层级表达父子关系
- 路径中不包含动词，操作语义由 HTTP 方法表达
- 路径中不包含文件后缀（如 .json、.xml）

### 为什么采用

**原因一：一致性的可预测性**。当所有接口遵循同一套命名规则时，前端开发人员可以根据资源名称直接推断出 URL。例如，知道资源叫 "score template"，就能推断出列表接口为 GET /api/v1/score-templates，详情接口为 GET /api/v1/score-templates/{id}。不需要每次翻文档。

**原因二：kebab-case 的跨平台兼容性**。URL 标准规定路径区分大小写，而不同操作系统、Web 服务器对大小写的处理不一致。全部小写 + 连字符的策略从根本上避免大小写问题。camelCase 在 URL 中视觉模糊（scoreTemplates 不易阅读），snake_case 与部分 Web 服务器的 URL 重写规则冲突。

**原因三：名词复数符合集合语义**。RESTful 将 URL 视为资源标识。/api/v1/tasks 表示"任务集合"，/api/v1/tasks/12345 表示"集合中的第 12345 号任务"。使用复数使集合语义明确——前端开发者不会混淆"查单个任务"和"查任务列表"的 URL 格式。

**原因四：URL 层级表达数据关系**。子资源嵌套在父资源路径中（如 /api/v1/classes/{classId}/students），比查询参数（如 /api/v1/students?classId=xxx）更清晰地表征数据归属关系。API 网关和监控系统也能基于 URL 路径来统计各模块的调用量。

## 1.4 原则三：统一 HTTP 方法

### 是什么

本项目的接口严格按以下语义使用 HTTP 方法：

| HTTP 方法 | 语义 | 典型 URL | 说明 |
|-----------|------|----------|------|
| GET | 查询资源 | GET /api/v1/tasks | 获取资源列表或单个资源，不修改服务端数据 |
| POST | 创建资源 | POST /api/v1/tasks | 在集合中创建新资源，服务器分配 ID |
| PUT | 全量更新 | PUT /api/v1/tasks/{taskId} | 替换资源的全部字段，客户端必须提交完整数据 |
| PATCH | 部分更新 | PATCH /api/v1/tasks/{taskId}/status | 仅更新资源的部分字段 |
| DELETE | 删除资源 | DELETE /api/v1/tasks/{taskId} | 删除指定资源 |

### 为什么采用

**原因一：语义明确，减少歧义**。如果所有写操作都用 POST，前端调用者无法从方法名判断操作是否会重复创建资源。区分 POST（创建）和 PUT（更新）使语义一目了然。

**原因二：幂等性保障**。GET、PUT、DELETE 是幂等的——多次调用结果相同。POST 和 PATCH 非幂等。前端可以利用这一特性实现安全重试：网络超时后可以安全重试 PUT 和 DELETE，而不能盲目重试 POST（可能导致重复创建）。HTTP 基础设施（如 Nginx 重试机制）也依赖此语义。

**原因三：权限粒度控制**。区分 GET（读）、POST（创建）、PUT/PATCH（写）、DELETE（删）后，权限系统可以按操作类型做细粒度控制。例如，学生角色可以 GET /api/v1/tasks 查看任务，但不能 POST /api/v1/tasks 创建任务。

**原因四：与 RESTful 生态一致**。Spring Boot 的 @GetMapping、@PostMapping 等注解直接对应 HTTP 方法。前端 Axios 的 axios.get()、axios.post() 等方法也直接对应。使用统一的方法语义使代码与框架无缝衔接。

## 1.5 原则四：统一状态码

### 是什么

所有接口必须返回正确的 HTTP 状态码，不将业务错误包装在 HTTP 200 中返回。本项目使用的状态码如下：

| 状态码 | 语义 | 使用场景 |
|--------|------|---------|
| 200 OK | 成功 | GET、PUT、PATCH 成功时的默认返回 |
| 201 Created | 已创建 | POST 成功创建资源后返回，Response Body 包含新资源 |
| 204 No Content | 无内容 | DELETE 成功后返回，Response Body 为空 |
| 400 Bad Request | 请求错误 | 请求参数格式错误、JSON 解析失败 |
| 401 Unauthorized | 未认证 | Token 缺失、无效或过期 |
| 403 Forbidden | 无权限 | Token 有效但当前用户无权访问该资源 |
| 404 Not Found | 未找到 | 请求的资源不存在（如 taskId 对应的任务不存在） |
| 409 Conflict | 冲突 | 资源状态冲突（如尝试删除已有提交记录的课程） |
| 422 Unprocessable Entity | 无法处理 | 参数校验失败，业务逻辑上无法处理（如提交已截止的任务） |
| 429 Too Many Requests | 请求过多 | 触发频率限制 |
| 500 Internal Server Error | 服务器错误 | 未预期的服务器内部异常 |
| 502 Bad Gateway | 网关错误 | 上游服务（如 AI 服务）不可达 |
| 503 Service Unavailable | 服务不可用 | 服务正在维护或过载 |

### 为什么采用

**原因一：HTTP 状态码是 Web 基础设施的通用语言**。浏览器 DevTools、API 网关、Nginx 日志、监控系统（Prometheus/Grafana）均按状态码进行分类统计和告警。如果所有错误都返回 200，监控系统无法区分正常请求和异常请求。

**原因二：前端可以根据状态码做分层处理**。401 统一跳转登录页，403 统一展示无权限页面，500 统一展示服务器错误页面。这些通用处理逻辑可以在 Axios 拦截器中一次性配置，不需要每个接口单独判断。

**原因三：业务错误码在 Body 中补充细节**。HTTP 状态码表达错误的大类（认证、权限、校验、服务端），具体的业务错误码（见第四章）在 Response Body 的 code 字段中表达。两者互补，既保持了 HTTP 协议的规范性，又提供了业务所需的细节信息。

## 1.6 原则五：统一版本管理

### 是什么

API 版本号通过 URL 路径前缀表达：/api/v1/、/api/v2/。不通过 Header（如 Accept: application/vnd.api+v1+json）或查询参数（?version=1）传递版本号。当大版本升级时（如 v1 升级到 v2），旧版本接口保留至少一个完整迭代周期（约 2-4 周），并返回 Deprecation 响应头提示调用方迁移。废弃的旧版本接口在过渡期结束后返回 410 Gone。

### 为什么采用

**原因一：URL 路径版本直观且易于调试**。在浏览器地址栏、DevTools Network 面板、Nginx 日志中可以看到完整的版本信息。Header 版本方式则需要在请求头中查找，不利于快速定位问题。

**原因二：API 网关路由简单**。Nginx 或 Spring Cloud Gateway 可以基于 URL 路径直接路由到不同的后端服务实例（如 /api/v1/ 路由到旧版本服务，/api/v2/ 路由到新版本服务）。基于 Header 的路由配置复杂且性能较差。

**原因三：前后端独立演进**。前端可以在切换环境变量后渐进迁移到新版本 API，无需后端配合改代码。移动端 App 的发版周期长，可以继续使用旧版本 API 直到强制升级。

## 1.7 原则六：统一分页

### 是什么

所有返回列表的接口必须支持分页。分页参数统一为 page（页码，从 1 开始）和 pageSize（每页条数，可选 10/20/50/100，默认 20）。分页响应统一包含 list（当前页数据）、page（当前页码）、pageSize（每页条数）、total（总记录数）、totalPages（总页数）。详见第十三章分页规范。

### 为什么采用

**原因一：防止数据库和前端内存溢出**。不设分页的列表接口会一次性返回所有数据。当班级学生数量达到数百人、提交记录数千条时，数据库查询耗时暴增，前端渲染大量 DOM 节点导致页面卡顿甚至崩溃。

**原因二：前端可以统一封装分页组件**。统一的分页参数和响应格式意味着前端只需要一个 Pagination 组件、一个 usePagination Composable，就能处理所有列表页面的分页逻辑。否则每个页面都要单独处理分页参数的命名差异。

**原因三：Mock 数据可以按页生成**。统一的分页参数使 Mock.js 可以根据 page 和 pageSize 动态生成不同的数据页，模拟真实的分页行为。详见第十四章 Mock 数据规范。

## 1.8 原则七：统一排序

### 是什么

所有列表接口支持通过 sortField 和 sortOrder 两个参数控制排序。sortField 为排序字段名（camelCase），sortOrder 为排序方向（asc 升序 / desc 降序）。默认排序为 createdAt desc（按创建时间倒序）。

### 为什么采用

**原因一：前端表格组件的通用交互模式**。Element Plus 的 el-table 组件通过 sort-change 事件传递 {prop, order} 参数，直接映射为 sortField 和 sortOrder。统一参数名使表格排序的前后端对接零摩擦。

**原因二：与分页参数协同**。排序 + 分页 + 过滤三者组合是列表页面的标准查询模式。统一参数名使得前端的请求构建逻辑和后端的查询条件拼接逻辑可以通用化。

## 1.9 原则八：统一过滤

### 是什么

列表接口支持通过 URL 查询参数进行过滤。过滤参数直接以字段名作为参数名，多个条件之间为 AND 关系。常用过滤参数包括 keyword（全局搜索）、status（状态筛选）、startDate / endDate（时间范围）。每个接口在定义中明确标注支持哪些过滤参数。

### 为什么采用

**原因一：URL 查询参数是 GET 请求的标准传参方式**。将过滤条件放在 URL 中（如 GET /api/v1/tasks?status=PUBLISHED&keyword=实训），前端可以直接通过浏览器地址栏查看和修改过滤条件，便于调试。

**原因二：与前端搜索表单直接映射**。前端的搜索表单字段名可以直接作为查询参数名，无需额外的参数名转换逻辑。Axios 的 params 选项自动将对象序列化为查询参数。

## 1.10 原则十：统一异常处理

### 是什么

所有异常（包括业务异常、参数校验异常、系统异常）都通过统一的响应结构返回（见第三章），不抛出 HTTP 500 裸错误页面，不在 Response Body 中暴露堆栈信息。后端通过全局异常处理器（Spring Boot @ControllerAdvice）拦截所有异常，转换为统一格式后返回。生产环境的 message 字段不包含内部错误详情，仅返回用户友好的提示信息。

### 为什么采用

**原因一：前端只需要处理一种错误格式**。如果不同的异常返回不同的 JSON 结构（有的返回 {error: "..."}，有的返回 {msg: "..."}），前端需要针对每种格式编写不同的解析逻辑。统一格式使 Axios 拦截器可以一次性处理所有错误。

**原因二：安全考虑**。数据库错误、SQL 语句、文件路径等内部信息如果暴露给前端，可能被恶意利用。统一异常处理在服务端就将敏感信息过滤，只返回安全的提示文案。

**原因三：运维可观测**。统一异常处理可以将完整的异常堆栈记录到服务端日志（关联 traceId），便于排查问题。前端收到的只有简洁的错误码，不影响运维通过 traceId 追溯完整异常。

## 1.11 原则十一：统一权限认证

### 是什么

系统采用 JWT Bearer Token 进行身份认证，采用 RBAC（基于角色的访问控制）进行权限管理。每个请求必须在 Authorization 请求头中携带 Token。后端从 Token 中解析出用户身份（userId、username、role）后，再根据接口所需的权限点进行鉴权。认证失败返回 401，权限不足返回 403。

### 为什么采用

**原因一：无状态认证适配分布式部署**。JWT 不依赖服务端 Session，Token 中自包含用户身份信息。当后端服务水平扩展为多个实例时，任意实例都可以独立验证 Token，无需共享 Session 存储。

**原因二：前后端分离架构的标准方案**。Vue 前端将 Token 存储在 localStorage 中，每次请求通过 Axios 拦截器自动附加到 Authorization Header。这是前后端分离架构下最成熟、社区支持最广泛的认证方案。

**原因三：RBAC 的灵活性和可维护性**。角色 + 权限点双层模型允许灵活配置——可以为某个角色批量分配权限点，也可以为特定用户单独调整权限。新增功能模块时只需定义新的权限点，不需要修改现有角色的权限配置。

## 1.12 原则十二：统一响应格式

### 是什么

所有接口（无论成功或失败）返回相同结构的 JSON 对象，包含 code、message、data、success、timestamp、traceId、requestId 等固定字段。详见第三章统一响应格式。

### 为什么采用

**原因一：前端可以编写通用的响应处理逻辑**。Axios 响应拦截器可以基于统一的响应结构做通用处理：检查 success 字段判断成功/失败，解析 code 字段分支处理不同错误类型，记录 traceId 用于问题排查。如果每个接口返回不同结构，这些通用逻辑无法实现。

**原因二：API 网关和监控系统可以直接解析**。API 网关可以基于统一响应结构中的 code 字段做业务级别的监控和告警（如 code 5000+ 比例超过阈值时告警）。如果响应格式不统一，网关需要为每个接口配置不同的解析规则。

**原因三：Mock.js 可以基于统一格式生成模拟数据**。Mock.js 的响应模板只需要定义 data 字段的内容，外层统一结构由拦截器自动包裹。详见第十四章。

## 1.13 原则十三：统一时间格式

### 是什么

所有接口中的时间字段统一使用 ISO 8601 格式，带时区偏移。格式为：yyyy-MM-ddTHH:mm:ss.SSS+08:00（毫秒 + 东八区偏移）。示例：2026-06-30T14:30:00.000+08:00。日期类型字段（不含时间）使用 yyyy-MM-dd 格式。

### 为什么采用

**原因一：ISO 8601 是国际标准，生态支持完善**。Java 的 java.time.Instant、JavaScript 的 Date 对象、MySQL 的 DATETIME 类型、JSON.stringify 都可以直接处理 ISO 8601 格式的时间字符串，无需自定义解析逻辑。

**原因二：带时区信息避免时区歧义**。本系统面向全国高校，用户可能分布在不同的时区（如新疆使用 UTC+6）。不带时区信息的时间字符串（如 2026-06-30T14:30:00）会被不同客户端解析为不同的本地时间，导致提交截止时间判定错误。

**原因三：可读性和可排序性兼具**。ISO 8601 格式的字符串在字典序下与时间先后顺序一致，可以直接用字符串比较进行排序。同时格式中包含人类可读的日期和时间部分，便于在日志和调试工具中查看。

## 1.14 原则十四：统一 ID 规范

### 是什么

所有资源的唯一标识符采用 Snowflake 雪花算法生成，类型为 64 位 Long（19 位十进制数字）。在前后端传输时，ID 字段统一使用 String 类型序列化，避免 JavaScript Number 类型精度丢失（JavaScript 的 Number 类型最大安全整数为 2^53 - 1 = 9007199254740991，而 Snowflake 最大值为 2^63 - 1 ≈ 9.22 × 10^18）。后端在 JSON 序列化时将 Long 转为 String，反序列化时从 String 转回 Long。

### 为什么采用

**原因一：Snowflake 支持分布式 ID 生成**。Snowflake 算法生成的 ID 全局唯一且趋势递增，不依赖数据库自增主键和 Redis 计数器。当后端服务部署多个实例时，每个实例可以独立生成 ID 而不会冲突。这是微服务架构下 ID 生成的标准方案。

**原因二：String 序列化避免 JavaScript 精度问题**。JavaScript 的 Number 类型基于 IEEE 754 双精度浮点数，最大安全整数为 9007199254740991（约 9 × 10^15）。Snowflake 生成的 ID 可达 19 位，超出 JavaScript 安全整数范围。如果后端返回 Number 类型，前端 JSON.parse 后会丢失精度，导致 ID 在前后端之间不一致。统一使用 String 类型彻底解决此问题。

**原因三：ID 不暴露业务信息**。Snowflake ID 仅包含时间戳、机器 ID 和序列号，不包含业务含义。相比之下，自增 ID 会暴露数据量级（竞争对手可以通过 ID 推算用户规模），UUID 虽然不暴露信息但字符串较长且无序（不利于数据库索引）。

---

---

# 第二章 统一请求规范

## 2.1 章节概述

本章定义所有 API 请求必须遵循的格式规范，包括 Base URL、版本前缀、请求头（Headers）、认证方式。所有前端 HTTP 请求和后端接口接收都必须严格遵守本章定义。这些规范的统一是实现前后端分离开发、API 网关路由、全链路追踪的基础。

## 2.2 Base URL

### 定义

Base URL 是 API 的根路径，环境之间通过不同的 Base URL 区分。

| 环境 | Base URL | 说明 |
|------|----------|------|
| 开发环境 | http://localhost:8080 | 本地开发，前端 Vite 代理至后端 Spring Boot |
| 测试环境 | http://test-api.example.com | 测试服务器，供前端联调和测试使用 |
| 预发布环境 | http://staging-api.example.com | 预发布验证，与生产环境配置一致 |
| 生产环境 | https://api.example.com | 正式生产环境，HTTPS 加密传输 |

### Mock 环境说明

前端本地开发时启用 Mock 模式，Base URL 切换为空字符串或 `/mock`，Axios 请求被 Mock.js 拦截，不发送真实 HTTP 请求。Mock 数据格式必须与真实接口完全一致（遵循本规范定义的响应结构）。

### Vite 代理配置

开发环境下，Vite 开发服务器通过代理转发 API 请求到后端，避免跨域问题。代理规则：

```
/api → http://localhost:8080/api
```

## 2.3 API Version

### 定义

当前 API 版本：v1。所有接口 URL 前缀：/api/v1/。

示例：

- GET /api/v1/tasks
- POST /api/v1/auth/login

### 版本管理规则

- 新版本号递增为 v2、v3...
- 大版本升级时，旧版本接口保留至少 2 个迭代周期（约 4 周）
- 旧版本接口在过渡期内在响应头中返回 Deprecation: true 和 Sunset: <过期日期>
- 过渡期结束后，旧版本接口返回 410 Gone，响应体中包含新接口地址

## 2.4 请求头（Headers）

### 2.4.1 Authorization（认证头）

| 属性 | 值 |
|------|-----|
| Header 名 | Authorization |
| 类型 | String |
| 是否必填 | 除公开接口外必填 |
| 格式 | Bearer {jwt_token} |
| 示例 | Authorization: Bearer eyJhbGciOiJIUzI1NiIs... |
| 用途 | 携带 JWT Token 进行身份认证 |

**详细说明**：

- Token 由登录接口（POST /api/v1/auth/login）返回
- accessToken 有效期 2 小时，过期后使用 refreshToken 刷新（POST /api/v1/auth/refresh）
- 前端 Axios 拦截器自动从 localStorage 获取 Token 并附加到请求头
- 401 响应触发自动刷新 Token 逻辑，刷新失败则跳转登录页

### 2.4.2 Content-Type（内容类型头）

| 属性 | 值 |
|------|-----|
| Header 名 | Content-Type |
| 类型 | String |
| 是否必填 | POST/PUT/PATCH 请求必填 |
| 默认值 | application/json |
| 可选值 | application/json、multipart/form-data |
| 用途 | 指定请求体的 MIME 类型 |

**详细说明**：

- JSON 请求体：Content-Type: application/json
- 文件上传：Content-Type: multipart/form-data（由浏览器自动设置，包含 boundary）
- GET 和 DELETE 请求不包含此 Header

### 2.4.3 Accept（接受类型头）

| 属性 | 值 |
|------|-----|
| Header 名 | Accept |
| 类型 | String |
| 是否必填 | 否 |
| 默认值 | application/json |
| 用途 | 告知服务器客户端接受的响应格式 |

**详细说明**：

- 所有接口默认返回 JSON 格式
- 文件下载接口返回 application/octet-stream 或具体文件类型
- Excel 导出返回 application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
- PDF 导出返回 application/pdf

### 2.4.4 X-Trace-Id（全链路追踪头）

| 属性 | 值 |
|------|-----|
| Header 名 | X-Trace-Id |
| 类型 | String（UUID v4） |
| 是否必填 | 推荐必填 |
| 默认值 | 前端自动生成 |
| 格式 | 550e8400-e29b-41d4-a716-446655440000 |
| 用途 | 全链路追踪 ID，贯穿前端、API 网关、后端服务、AI 服务、数据库的所有日志 |

**详细说明**：

- 由前端在 Axios 请求拦截器中生成（UUID v4）
- 后端接收到后写入日志的 MDC（Mapped Diagnostic Context）
- 调用 AI 服务（Python FastAPI）时透传
- 用户反馈问题时，提供 traceId 即可在日志系统中快速定位完整调用链

### 2.4.5 X-Request-Id（请求 ID 头）

| 属性 | 值 |
|------|-----|
| Header 名 | X-Request-Id |
| 类型 | String（UUID v4） |
| 是否必填 | 推荐必填 |
| 默认值 | 前端自动生成 |
| 格式 | 660e8400-e29b-41d4-a716-446655440001 |
| 用途 | 单次请求的唯一标识，用于幂等性控制和问题定位 |

**详细说明**：

- 每次请求生成唯一的 requestId
- 与 traceId 的区别：traceId 在一个用户操作链路中保持不变（如"登录→查任务→提交→查看评价"共用同一个 traceId），requestId 每个 HTTP 请求都不同
- 后端使用 requestId 实现幂等性（如防止重复提交），将 requestId 存入 Redis，重复请求直接返回已有结果

### 2.4.6 Accept-Language（语言偏好头）

| 属性 | 值 |
|------|-----|
| Header 名 | Accept-Language |
| 类型 | String |
| 是否必填 | 否 |
| 默认值 | zh-CN |
| 可选值 | zh-CN（简体中文）、en-US（英文） |
| 用途 | 指定响应消息的语言 |

**详细说明**：

- 当前阶段仅支持中文，英文为预留扩展
- 错误消息、提示信息根据此 Header 返回对应语言版本
- Mock 阶段固定使用中文

### 2.4.7 X-Timezone（时区头）

| 属性 | 值 |
|------|-----|
| Header 名 | X-Timezone |
| 类型 | String |
| 是否必填 | 否 |
| 默认值 | Asia/Shanghai |
| 用途 | 指定客户端的时区，用于时间显示转换 |

**详细说明**：

- 服务器统一以 UTC+8 存储和返回时间
- 此 Header 预留用于多时区部署场景
- Mock 阶段固定使用 Asia/Shanghai

## 2.5 请求参数规范

### 2.5.1 路径参数（Path Parameters）

路径参数嵌入在 URL 路径中，使用花括号标记。

格式：/api/v1/tasks/{taskId}

示例：GET /api/v1/tasks/1234567890123456789

路径参数命名使用 camelCase，类型为 String（对应 Snowflake ID）。

### 2.5.2 查询参数（Query Parameters）

查询参数附加在 URL 问号后，用于 GET 请求的过滤、排序、分页。

格式：?page=1&pageSize=20&sortField=createdAt&sortOrder=desc&status=PUBLISHED

查询参数命名使用 camelCase。

### 2.5.3 请求体（Request Body）

请求体用于 POST、PUT、PATCH 请求，格式为 JSON。

请求体字段命名使用 camelCase，类型必须明确（String、Integer、Long、Boolean、Array、Object）。

---

---

# 第三章 统一响应格式

## 3.1 章节概述

本章定义所有 API 接口返回的 JSON 响应结构。无论接口成功还是失败，无论哪个模块，返回的 JSON 对象顶层结构完全一致。这是 Mock.js、Axios、Spring Boot、OpenAPI 四方共同遵循的核心数据契约。

## 3.2 通用响应结构

### 3.2.1 结构定义

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "success": true,
  "timestamp": "2026-06-30T14:30:00.000+08:00",
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "requestId": "660e8400-e29b-41d4-a716-446655440001",
  "path": "/api/v1/tasks",
  "elapsed": 234
}
```

### 3.2.2 字段说明

| 字段 | 类型 | 必返 | 说明 |
|------|------|------|------|
| code | Integer | 是 | 业务状态码。0 表示成功，非 0 表示失败。具体错误码见第四章 |
| message | String | 是 | 人类可读的提示信息。成功时为 "success"，失败时为具体错误描述 |
| data | Object / Array / null | 是 | 响应数据体。成功时包含业务数据，失败时为 null |
| success | Boolean | 是 | 请求是否成功。true = 成功，false = 失败 |
| timestamp | String | 是 | 服务器响应时间，ISO 8601 格式，带时区 |
| traceId | String | 是 | 全链路追踪 ID，与请求头 X-Trace-Id 一致 |
| requestId | String | 是 | 请求 ID，与请求头 X-Request-Id 一致 |
| path | String | 是 | 请求的 URL 路径，便于问题定位 |
| elapsed | Long | 否 | 服务器处理耗时，单位毫秒。生产环境可关闭 |

## 3.3 六种响应形态

### 3.3.1 Success（成功响应）

成功响应用于 GET、POST、PUT、PATCH 操作成功完成时。code = 0，success = true，data 包含业务数据。

**示例一：查询单个资源（GET）**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "taskId": "1234567890123456789",
    "taskName": "Spring Boot 图书管理系统实训",
    "courseName": "Java Web 开发",
    "description": "基于 Spring Boot + MyBatis + MySQL 实现图书的增删改查功能",
    "deadline": "2026-07-15T23:59:59.000+08:00",
    "status": "PUBLISHED",
    "totalScore": 100,
    "createdAt": "2026-06-25T10:00:00.000+08:00",
    "updatedAt": "2026-06-28T16:30:00.000+08:00"
  },
  "success": true,
  "timestamp": "2026-06-30T14:30:00.123+08:00",
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "requestId": "660e8400-e29b-41d4-a716-446655440001",
  "path": "/api/v1/student/tasks/1234567890123456789",
  "elapsed": 45
}
```

**示例二：创建资源（POST）**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "taskId": "1234567890123456790",
    "taskName": "Vue 3 组件库实训",
    "createdAt": "2026-06-30T14:30:00.456+08:00"
  },
  "success": true,
  "timestamp": "2026-06-30T14:30:00.456+08:00",
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "requestId": "660e8400-e29b-41d4-a716-446655440002",
  "path": "/api/v1/teacher/tasks",
  "elapsed": 120
}
```

### 3.3.2 Fail（通用失败响应）

通用失败响应用于非特定分类的业务错误。code = 1，success = false，data = null，message 描述失败原因。

```json
{
  "code": 1,
  "message": "操作失败：任务状态不允许此操作",
  "data": null,
  "success": false,
  "timestamp": "2026-06-30T14:30:00.789+08:00",
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "requestId": "660e8400-e29b-41d4-a716-446655440003",
  "path": "/api/v1/teacher/submissions/9876543210987654321/score",
  "elapsed": 15
}
```

### 3.3.3 Validation Error（参数校验失败）

参数校验失败时，code = 4001（或具体校验错误码），success = false，data 返回 null，Response Body 的根级别增加 errors 数组提供字段级错误详情。

```json
{
  "code": 4001,
  "message": "参数校验失败",
  "data": null,
  "success": false,
  "timestamp": "2026-06-30T14:30:01.000+08:00",
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "requestId": "660e8400-e29b-41d4-a716-446655440004",
  "path": "/api/v1/teacher/tasks",
  "errors": [
    {
      "field": "taskName",
      "message": "任务名称不能为空",
      "rejectedValue": ""
    },
    {
      "field": "deadline",
      "message": "截止时间不能早于当前时间",
      "rejectedValue": "2026-01-01T00:00:00.000+08:00"
    }
  ],
  "elapsed": 8
}
```

errors 数组中每个元素包含：

| 字段 | 类型 | 说明 |
|------|------|------|
| field | String | 校验失败的字段名（camelCase） |
| message | String | 校验失败的具体原因（用户可读） |
| rejectedValue | Any | 被拒绝的输入值 |

### 3.3.4 Permission Error（权限不足）

权限不足时，HTTP 状态码返回 403，code 返回对应的权限错误码（3001 或 3003）。

```json
{
  "code": 3001,
  "message": "无权限访问：当前角色无此操作权限",
  "data": null,
  "success": false,
  "timestamp": "2026-06-30T14:30:01.100+08:00",
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "requestId": "660e8400-e29b-41d4-a716-446655440005",
  "path": "/api/v1/teacher/tasks",
  "elapsed": 3
}
```

### 3.3.5 Business Error（业务异常）

业务异常时，code 使用 1000-1999 范围内的具体错误码，message 包含面向用户的业务提示。

```json
{
  "code": 1006,
  "message": "提交失败：该实训任务已于 2026-07-15 截止",
  "data": null,
  "success": false,
  "timestamp": "2026-06-30T14:30:01.200+08:00",
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "requestId": "660e8400-e29b-41d4-a716-446655440006",
  "path": "/api/v1/student/tasks/1234567890123456789/submissions",
  "elapsed": 25
}
```

### 3.3.6 Server Error（服务器错误）

服务器内部错误时，HTTP 状态码返回 500，code = 5001。注意：message 不暴露内部错误详情（如堆栈信息、SQL 语句），仅返回通用提示。

```json
{
  "code": 5001,
  "message": "服务器繁忙，请稍后重试",
  "data": null,
  "success": false,
  "timestamp": "2026-06-30T14:30:01.500+08:00",
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "requestId": "660e8400-e29b-41d4-a716-446655440007",
  "path": "/api/v1/teacher/tasks",
  "elapsed": 3012
}
```

## 3.4 分页响应结构

分页响应的 data 字段包含分页元数据和数据列表。

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "taskId": "1234567890123456789",
        "taskName": "Spring Boot 图书管理系统实训",
        "deadline": "2026-07-15T23:59:59.000+08:00",
        "status": "PUBLISHED"
      }
    ],
    "page": 1,
    "pageSize": 20,
    "total": 150,
    "totalPages": 8
  },
  "success": true,
  "timestamp": "2026-06-30T14:30:00.123+08:00",
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "requestId": "660e8400-e29b-41d4-a716-446655440001"
}
```

分页数据结构说明：

| 字段 | 类型 | 说明 |
|------|------|------|
| list | Array | 当前页的数据列表 |
| page | Integer | 当前页码，从 1 开始 |
| pageSize | Integer | 每页记录数 |
| total | Long | 总记录数 |
| totalPages | Integer | 总页数，计算公式：Math.ceil(total / pageSize) |

---

---

# 第四章 统一错误码规范

## 4.1 章节概述

本章建立完整的错误码体系。每个错误码全局唯一，用于精准定位问题类型。前端根据错误码执行对应处理逻辑（跳转、提示、重试），后端根据错误码统一响应。错误码体系分为 10 个大类，覆盖认证、授权、业务、校验、系统、AI、文件、Git、导出等所有模块。

## 4.2 错误码分类体系

| 范围 | 分类 | 含义 | 典型场景 |
|------|------|------|---------|
| 0 | 成功 | 请求成功完成 | 所有正常响应 |
| 1000-1999 | 业务错误 | 业务逻辑相关异常 | 资源不存在、状态不允许、提交超限 |
| 2000-2999 | 认证错误 | 登录、Token、密码相关 | Token过期、密码错误、账号锁定 |
| 3000-3999 | 授权错误 | 角色权限、资源权限相关 | 无操作权限、资源越权访问 |
| 4000-4999 | 参数校验 | 字段校验、格式校验相关 | 必填项为空、格式不正确、值非法 |
| 5000-5999 | 系统错误 | 服务器内部异常 | 数据库异常、Redis异常、服务不可用 |
| 6000-6999 | AI 相关 | AI 分析、模型调用相关 | 模型超时、JSON解析失败、Token额度不足 |
| 7000-7999 | 文件相关 | 文件上传下载相关 | 文件过大、类型不支持、上传失败 |
| 8000-8999 | Git 相关 | Git 仓库操作相关 | 仓库不存在、克隆失败、无权限 |
| 9000-9999 | 导出相关 | 报表导出相关 | 导出失败、数据量过大、超时 |

## 4.3 错误码详情

每个错误码必须包含三个维度的信息：

- **错误原因**：技术层面的触发条件，供后端开发人员参考
- **前端处理方式**：前端收到该错误码后应执行的操作，供前端开发人员参考
- **用户提示**：面向最终用户的中文提示文案，直接可用于 Toast / MessageBox 展示

### 4.3.1 业务错误（1000-1999）

#### 1001 - 资源不存在

- **错误原因**：请求的资源 ID 在数据库中不存在（如 taskId 对应的任务已被删除或从未创建）
- **前端处理方式**：展示 404 页面或 Toast 提示后返回上一页
- **用户提示**："请求的资源不存在，可能已被删除"

#### 1002 - 资源已存在

- **错误原因**：尝试创建的资源与已有资源冲突（如用户名已存在、课程编号已使用）
- **前端处理方式**：在表单对应字段下方展示红色错误提示
- **用户提示**："{字段名}已存在，请更换后重试"

#### 1003 - 操作不允许

- **错误原因**：当前资源的状态不允许执行该操作（如尝试修改已截止的任务、删除有学生已提交的任务）
- **前端处理方式**：Toast 警告提示，按钮置灰或隐藏
- **用户提示**："当前状态下不允许执行此操作"

#### 1004 - 数据已被修改

- **错误原因**：乐观锁冲突，当前数据已被其他用户修改（数据版本号不匹配）
- **前端处理方式**：提示用户刷新页面获取最新数据后重新操作
- **用户提示**："数据已被他人修改，请刷新后重试"

#### 1005 - 超过提交次数限制

- **错误原因**：学生对同一任务的提交次数超过系统或教师设定的上限
- **前端处理方式**：提交按钮置灰，展示已提交次数和最大次数
- **用户提示**："已达到提交次数上限（{maxCount}次），无法再次提交"

#### 1006 - 任务已截止

- **错误原因**：学生尝试在任务 deadline 之后提交成果
- **前端处理方式**：提交按钮置灰，展示截止时间和当前时间；若教师在截止时间后打开补交通道，则提示补交模式
- **用户提示**："该实训任务已于 {deadline} 截止"

#### 1007 - 账号不存在

- **错误原因**：查询的用户 ID 在数据库中不存在
- **前端处理方式**：Toast 提示
- **用户提示**："账号不存在"

#### 1008 - 成绩已发布不可修改

- **错误原因**：教师尝试修改已发布成绩的评分
- **前端处理方式**：评分表单置为只读，展示"成绩已发布"状态标签
- **用户提示**："成绩已发布，如需修改请先撤回发布"

### 4.3.2 认证错误（2000-2999）

#### 2001 - 未登录

- **错误原因**：请求头中缺少 Authorization 字段或值为空
- **前端处理方式**：清除本地 Token，跳转登录页，登录成功后回到原页面
- **用户提示**："请先登录"

#### 2002 - Token 过期

- **错误原因**：accessToken 已超过 2 小时有效期
- **前端处理方式**：Axios 拦截器自动使用 refreshToken 调用刷新接口。若 refreshToken 也过期，则跳转登录页
- **用户提示**："登录已过期，请重新登录"

#### 2003 - Token 无效

- **错误原因**：Token 签名验证失败、Token 格式不正确、Token 已被服务端主动失效
- **前端处理方式**：清除本地 Token，跳转登录页
- **用户提示**："登录凭证无效，请重新登录"

#### 2004 - 用户名或密码错误

- **错误原因**：登录时用户名不存在或密码不匹配
- **前端处理方式**：在登录表单上方展示错误提示，密码框清空
- **用户提示**："用户名或密码错误，请重试"

#### 2005 - 账号已锁定

- **错误原因**：连续登录失败次数超过限制（默认 5 次），账号被临时锁定（默认 15 分钟）
- **前端处理方式**：展示锁定提示和剩余解锁时间，禁止提交登录表单
- **用户提示**："账号已被锁定，请 {unlockTime} 后再试，或联系管理员"

#### 2006 - 账号已禁用

- **错误原因**：管理员将该账号状态设置为"禁用"
- **前端处理方式**：展示禁用提示，跳转登录页
- **用户提示**："账号已被禁用，请联系管理员"

#### 2007 - 验证码错误

- **错误原因**：登录验证码输入错误
- **前端处理方式**：清空验证码输入框，刷新验证码图片
- **用户提示**："验证码错误，请重新输入"

#### 2008 - 刷新 Token 过期

- **错误原因**：refreshToken 已超过 7 天有效期
- **前端处理方式**：清除所有本地 Token，跳转登录页
- **用户提示**："登录已过期，请重新登录"

### 4.3.3 授权错误（3000-3999）

#### 3001 - 无权限访问

- **错误原因**：当前用户的角色不具备访问该接口所需的权限点
- **前端处理方式**：展示无权限页面或 Toast 提示；路由守卫拦截无权限的页面访问
- **用户提示**："您没有权限访问此功能"

#### 3002 - 角色不存在

- **错误原因**：Token 中携带的角色编码在系统中不存在
- **前端处理方式**：清除 Token，跳转登录页
- **用户提示**："用户角色异常，请联系管理员"

#### 3003 - 资源无权限

- **错误原因**：用户可访问接口但无权操作特定资源（如教师尝试查看其他班级的学生数据）
- **前端处理方式**：返回上一页并 Toast 提示
- **用户提示**："您没有权限访问该资源"

#### 3004 - IP 限制

- **错误原因**：系统配置了 IP 白名单，当前 IP 不在白名单中
- **前端处理方式**：展示 IP 限制提示页面
- **用户提示**："当前网络环境无法访问系统，请联系管理员"

### 4.3.4 参数校验（4000-4999）

#### 4001 - 参数缺失

- **错误原因**：必填参数未传递
- **前端处理方式**：在表单对应字段下方展示红色错误提示
- **用户提示**："{字段名}不能为空"

#### 4002 - 参数格式错误

- **错误原因**：参数格式不符合预期（如邮箱格式错误、日期格式错误）
- **前端处理方式**：在表单对应字段下方展示红色错误提示及正确格式说明
- **用户提示**："{字段名}格式不正确"

#### 4003 - 参数值非法

- **错误原因**：参数值不在允许范围内（如 pageSize 传 999、status 传不存在的枚举值）
- **前端处理方式**：前端表单应通过下拉选择等控件限制输入值，减少此错误
- **用户提示**："{字段名}取值不合法，允许值为：{allowedValues}"

#### 4004 - 字段长度超限

- **错误原因**：字符串字段超过数据库或业务定义的最大长度
- **前端处理方式**：表单字段设置 maxlength 属性，实时显示已输入/最大字符数
- **用户提示**："{字段名}长度不能超过 {maxLength} 个字符"

#### 4005 - 必填字段为空

- **错误原因**：Request Body 中必填字段值为 null 或空字符串
- **前端处理方式**：提交前进行表单校验，高亮空字段
- **用户提示**："{字段名}为必填项"

#### 4006 - 数据已过期

- **错误原因**：请求中包含的时间参数已过期（如过期验证码）
- **前端处理方式**：刷新/重新获取数据后重试
- **用户提示**："数据已过期，请刷新后重试"

### 4.3.5 系统错误（5000-5999）

#### 5001 - 服务器内部错误

- **错误原因**：未预期的服务器异常（NPE、数组越界等）
- **前端处理方式**：展示通用错误页面或 Toast 提示"服务器繁忙"
- **用户提示**："服务器繁忙，请稍后重试"

#### 5002 - 数据库错误

- **错误原因**：数据库连接失败、SQL 执行异常
- **前端处理方式**：展示通用错误提示
- **用户提示**："数据服务异常，请稍后重试"

#### 5003 - Redis 错误

- **错误原因**：Redis 连接失败或操作异常
- **前端处理方式**：展示通用错误提示（对用户透明）
- **用户提示**："系统繁忙，请稍后重试"

#### 5004 - 第三方服务不可用

- **错误原因**：外部依赖服务（如邮件服务、短信服务）调用失败
- **前端处理方式**：根据业务场景提示（如"验证码发送失败"）
- **用户提示**："{服务名}暂时不可用，请稍后重试"

#### 5005 - 请求超时

- **错误原因**：后端处理请求超过设定的超时时间（默认 30 秒）
- **前端处理方式**：展示超时提示，提供重试按钮
- **用户提示**："请求超时，请检查网络后重试"

#### 5006 - 请求过于频繁

- **错误原因**：触发接口限流（同一用户短时间内请求次数超过阈值）
- **前端处理方式**：前端应做防抖处理；收到此错误后等待 3 秒再重试
- **用户提示**："操作过于频繁，请稍后重试"

#### 5007 - 服务降级中

- **错误原因**：系统负载过高，非核心功能临时关闭（如导出功能在高峰期降级）
- **前端处理方式**：对应功能按钮置灰，展示降级提示
- **用户提示**："当前访问量较大，{功能名}暂时不可用，请稍后重试"

### 4.3.6 AI 相关（6000-6999）

#### 6001 - AI 服务不可用

- **错误原因**：AI 分析服务（Python FastAPI）未启动或无法连接
- **前端处理方式**：展示 AI 服务异常提示，隐藏 AI 相关功能入口
- **用户提示**："AI 分析服务暂时不可用，请稍后重试"

#### 6002 - AI 模型调用失败

- **错误原因**：调用大模型 API（DeepSeek / OpenAI）返回错误
- **前端处理方式**：展示重试按钮，提示用户可稍后重新触发分析
- **用户提示**："AI 模型调用失败，请稍后重试"

#### 6003 - AI 分析超时

- **错误原因**：AI 分析超过最大等待时间（默认 60 秒）
- **前端处理方式**：展示超时提示 + 重试按钮，建议拆分大文件
- **用户提示**："AI 分析超时，建议拆分提交内容后重试"

#### 6004 - AI 返回格式异常

- **错误原因**：大模型返回的内容无法解析为预期的 JSON 结构（如返回了 Markdown 格式而非 JSON）
- **前端处理方式**：展示"AI 返回结果解析失败"提示 + 重试按钮
- **用户提示**："AI 分析结果格式异常，请重试或联系管理员检查 Prompt 配置"

#### 6005 - Token 额度不足

- **错误原因**：大模型 API 的 Token 额度已耗尽
- **前端处理方式**：展示额度不足提示，仅管理员可见充值/扩容入口
- **用户提示**："AI 服务 Token 额度不足，已通知管理员处理"

#### 6006 - Prompt 模板异常

- **错误原因**：配置的 Prompt 模板存在语法错误或变量缺失
- **前端处理方式**：展示异常提示，引导教师检查 Prompt 配置
- **用户提示**："AI 评分模板配置异常，请联系教师检查"

#### 6007 - 模型不存在

- **错误原因**：配置的模型名称在 AI 服务中不存在
- **前端处理方式**：仅管理员可见，展示配置错误提示
- **用户提示**："AI 模型配置错误，请联系管理员"

#### 6008 - AI 分析进行中

- **错误原因**：同一提交已经有一个 AI 分析任务正在执行，不允许重复发起
- **前端处理方式**：禁用"发起 AI 分析"按钮，展示"分析中"状态和进度
- **用户提示**："AI 正在分析中，请等待分析完成"

#### 6009 - AI 分析已被取消

- **错误原因**：用户主动取消了正在进行的 AI 分析任务
- **前端处理方式**：恢复"发起 AI 分析"按钮为可用状态
- **用户提示**："AI 分析已取消"

### 4.3.7 文件相关（7000-7999）

#### 7001 - 文件大小超限

- **错误原因**：上传文件超过配置的最大大小限制
- **前端处理方式**：前端上传组件应在选择文件时即校验大小，拦截超限文件；收到此错误时提示具体限制
- **用户提示**："文件大小不能超过 {maxSize}MB，当前文件大小 {fileSize}MB"

#### 7002 - 文件类型不支持

- **错误原因**：上传的文件 MIME 类型或扩展名不在允许列表中
- **前端处理方式**：前端上传组件的 accept 属性限制可选择的文件类型；收到此错误时提示支持的类型
- **用户提示**："不支持的文件类型，支持的类型：{supportedTypes}"

#### 7003 - 文件上传失败

- **错误原因**：文件上传过程中网络中断或 MinIO 存储异常
- **前端处理方式**：展示上传失败提示 + 重新上传按钮
- **用户提示**："文件上传失败，请重试"

#### 7004 - 文件不存在

- **错误原因**：请求的 fileId 对应的文件在 MinIO 中不存在
- **前端处理方式**：展示"文件已失效"提示
- **用户提示**："文件不存在或已被删除"

#### 7005 - 文件已损坏

- **错误原因**：上传的文件无法正常打开或解析（如 ZIP 文件损坏）
- **前端处理方式**：提示用户重新上传
- **用户提示**："文件已损坏，请重新上传"

#### 7006 - 文件数量超限

- **错误原因**：单次批量上传的文件数量超过限制
- **前端处理方式**：前端上传组件限制单次选择的文件数量
- **用户提示**："单次最多上传 {maxCount} 个文件"

### 4.3.8 Git 相关（8000-8999）

#### 8001 - Git 仓库不存在

- **错误原因**：提供的 Git 仓库 URL 无法访问（404）
- **前端处理方式**：在 Git URL 输入框下方展示错误提示
- **用户提示**："仓库地址无效，请检查是否为有效的公开仓库地址"

#### 8002 - Git 仓库无权限

- **错误原因**：仓库为私有仓库，未提供访问 Token 或 Token 无效
- **前端处理方式**：展示"需要授权"提示，引导输入 Access Token
- **用户提示**："无法访问该仓库，请检查仓库权限或提供有效的访问令牌"

#### 8003 - Git 克隆失败

- **错误原因**：仓库 URL 有效但 clone 过程出错（如仓库过大、网络超时）
- **前端处理方式**：展示重试按钮，建议使用 ZIP 上传替代
- **用户提示**："代码仓库获取失败，请重试或改用 ZIP 上传"

#### 8004 - Git 分支不存在

- **错误原因**：指定的分支在仓库中不存在
- **前端处理方式**：展示分支列表，让用户选择有效分支
- **用户提示**："指定的分支不存在，请选择有效分支"

### 4.3.9 导出相关（9000-9999）

#### 9001 - 导出任务创建失败

- **错误原因**：导出任务无法创建（如模板文件缺失、MinIO 不可用）
- **前端处理方式**：展示失败提示 + 重试按钮
- **用户提示**："报表导出失败，请重试"

#### 9002 - 导出数据量过大

- **错误原因**：请求导出的数据量超过单次导出限制（如超过 10000 条记录）
- **前端处理方式**：提示用户缩小筛选范围后重试
- **用户提示**："导出数据量过大（{dataCount}条），请缩小筛选范围后重试"

#### 9003 - 导出超时

- **错误原因**：导出处理时间超过最大等待时间
- **前端处理方式**：展示超时提示，建议分批次导出
- **用户提示**："报表导出超时，请缩小范围或分批次导出"

#### 9004 - 导出格式不支持

- **错误原因**：请求的导出格式不在支持的格式列表中
- **前端处理方式**：前端导出按钮应限制可选格式
- **用户提示**："不支持的导出格式，支持：PDF、Excel、Word"

**Response（分页成功）**：
```json
{
  "code": 0, "message": "success",
  "data": {
    "list": [
      {
        "taskId": "1234567890123456789", "taskName": "Spring Boot图书管理系统实训",
        "courseName": "Java Web开发", "teacherName": "王建国",
        "deadline": "2026-07-15T23:59:59.000+08:00", "totalScore": 100,
        "submissionType": "GIT_ZIP", "status": "PUBLISHED",
        "mySubmissionStatus": "NOT_SUBMITTED",
        "createdAt": "2026-06-25T10:00:00.000+08:00"
      }
    ],
    "page": 1, "pageSize": 20, "total": 8, "totalPages": 1
  },
  "success": true, "timestamp": "2026-06-30T09:00:06.100+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678911",
  "path": "/api/v1/student/tasks", "elapsed": 34
}
```

**任务列表字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| taskId | String | 任务ID |
| taskName | String | 任务名称 |
| courseName | String | 所属课程名称 |
| teacherName | String | 授课教师姓名 |
| deadline | String | 截止时间 |
| totalScore | Integer | 任务总分 |
| submissionType | String | 提交方式：GIT_ZIP/ZIP_ONLY/ONLINE_CODE/FILE_UPLOAD |
| status | String | 任务状态：PUBLISHED（已发布）/CLOSED（已关闭） |
| mySubmissionStatus | String | 我的提交状态（见下方枚举） |
| createdAt | String | 任务创建时间 |

**我的提交状态枚举**：

| 状态值 | 说明 | 前端展示 |
|--------|------|---------|
| NOT_SUBMITTED | 未提交 | "待提交"标签（橙色），显示截止倒计时 |
| SUBMITTED | 已提交，等待分析 | "已提交"标签（蓝色） |
| AI_EVALUATING | AI分析中 | "AI分析中"标签+进度条 |
| AI_COMPLETED | AI分析完成，待教师评分 | "待评分"标签（绿色） |
| TEACHER_SCORING | 教师评分中 | "评分中"标签 |
| COMPLETED | 已完成 | "已完成"标签+分数 |
| REJECTED | 已退回 | "已退回"标签（红色）+退回原因 |

**Mock策略**：随机生成8-12条任务数据，涵盖不同mySubmissionStatus状态。支持按状态筛选。

### 6.3.3 任务详情

- **接口名称**：获取任务详细信息
- **Method + URL**：GET /api/v1/student/tasks/{taskId}
- **权限**：STUDENT
- **路径参数**：taskId（String，19位Snowflake ID）

**Response（成功）**：
```json
{
  "code": 0, "message": "success",
  "data": {
    "taskId": "1234567890123456789",
    "taskName": "Spring Boot图书管理系统实训",
    "courseName": "Java Web开发", "teacherName": "王建国",
    "teacherEmail": "wangjg@example.com",
    "description": "使用Spring Boot + MyBatis + MySQL实现图书管理系统的增删改查功能。要求：1.实现图书信息的CRUD操作；2.实现用户借阅归还功能；3.提供RESTful API接口；4.编写单元测试。",
    "deadline": "2026-07-15T23:59:59.000+08:00",
    "totalScore": 100, "submissionType": "GIT_ZIP", "submitLimit": 3,
    "evaluationDimensions": [
      {"dimensionName": "代码规范", "weight": 25, "maxScore": 25},
      {"dimensionName": "功能完成度", "weight": 30, "maxScore": 30},
      {"dimensionName": "创新性", "weight": 20, "maxScore": 20},
      {"dimensionName": "文档完整性", "weight": 15, "maxScore": 15},
      {"dimensionName": "Git规范", "weight": 10, "maxScore": 10}
    ],
    "attachments": [
      {"fileId": "3456789012345678901", "fileName": "实训任务要求.pdf", "fileSize": 204800, "fileType": "application/pdf"}
    ],
    "mySubmissionStatus": "NOT_SUBMITTED", "mySubmitCount": 0, "maxSubmitCount": 3,
    "createdAt": "2026-06-25T10:00:00.000+08:00",
    "updatedAt": "2026-06-28T16:30:00.000+08:00"
  },
  "success": true,
  "timestamp": "2026-06-30T09:00:06.200+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678912",
  "path": "/api/v1/student/tasks/1234567890123456789", "elapsed": 22
}
```

**评分维度说明**：评分维度由教师在创建任务时配置。所有权重之和必须等于100。各维度maxScore之和等于totalScore。学生查看任务详情时可看到评分维度，了解教师将如何评分。

**Mock策略**：固定5个评分维度。taskId不存在时返回code=1001。

### 6.3.4 提交成果

- **接口名称**：学生提交实训成果
- **Method + URL**：POST /api/v1/student/tasks/{taskId}/submissions
- **权限**：STUDENT
- **Content-Type**：application/json
- **路径参数**：taskId（String，任务ID）

**Request Body**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| submissionType | String | 是 | 提交方式：GIT_URL/ZIP_UPLOAD/ONLINE_CODE |
| gitUrl | String | 否 | Git仓库地址（submissionType=GIT_URL时必填） |
| gitBranch | String | 否 | 分支名（默认main） |
| zipFileId | String | 否 | ZIP文件ID（submissionType=ZIP_UPLOAD时必填） |
| onlineCode | String | 否 | 在线代码内容（submissionType=ONLINE_CODE时必填） |
| remark | String | 否 | 提交备注说明，最长500字符 |

```json
{
  "submissionType": "GIT_URL",
  "gitUrl": "https://github.com/zhangsan/book-manager.git",
  "gitBranch": "main",
  "remark": "完成所有基本功能，单元测试覆盖率85%"
}
```

**Response（成功）**：
```json
{
  "code": 0, "message": "提交成功",
  "data": {
    "submissionId": "4567890123456789012", "taskId": "1234567890123456789",
    "submissionType": "GIT_URL", "status": "SUBMITTED",
    "submitCount": 1, "maxSubmitCount": 3,
    "submittedAt": "2026-06-30T10:00:00.000+08:00"
  },
  "success": true,
  "timestamp": "2026-06-30T10:00:00.000+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678913",
  "path": "/api/v1/student/tasks/1234567890123456789/submissions", "elapsed": 156
}
```

**Response（失败：超过提交次数）**：
```json
{ "code": 1005, "message": "已达到提交次数上限（3次），无法再次提交", "data": null, "success": false }
```

**Response（失败：任务已截止）**：
```json
{ "code": 1006, "message": "提交失败：该实训任务已于2026-06-20截止", "data": null, "success": false }
```

**Mock策略**：无限制提交次数时始终成功。达到3次后返回code=1005。taskId对应的任务deadline已过时返回code=1006。

### 6.3.5 上传实训报告

- **接口名称**：学生上传实训报告文档
- **Method + URL**：POST /api/v1/student/tasks/{taskId}/reports
- **权限**：STUDENT
- **Content-Type**：multipart/form-data
- **路径参数**：taskId（String，任务ID）

**Request（Form Data）**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 报告文件（PDF或DOCX，最大20MB） |
| title | String | 否 | 报告标题（不传则使用文件名） |

**Response（成功）**：
```json
{
  "code": 0, "message": "报告上传成功",
  "data": {
    "reportId": "5678901234567890123", "fileId": "3456789012345678902",
    "fileName": "Spring_Boot实训报告_张三.docx", "fileSize": 512000,
    "fileType": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "uploadedAt": "2026-06-30T10:05:00.000+08:00"
  },
  "success": true
}
```

**Mock策略**：始终返回上传成功。文件校验（类型、大小）在Mock模式下宽松处理。

### 6.3.6 Git仓库验证

- **接口名称**：验证学生提交的Git仓库地址有效性
- **Method + URL**：POST /api/v1/student/tasks/{taskId}/git-verify
- **权限**：STUDENT
- **Content-Type**：application/json

**Request Body**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| gitUrl | String | 是 | Git仓库HTTPS或SSH地址 |
| gitBranch | String | 否 | 分支名，默认main |
| accessToken | String | 否 | 私有仓库访问令牌 |

```json
{ "gitUrl": "https://github.com/zhangsan/book-manager.git", "gitBranch": "main" }
```

**Response（验证成功）**：
```json
{
  "code": 0, "message": "仓库验证通过",
  "data": {
    "valid": true, "repoName": "book-manager", "defaultBranch": "main",
    "branches": ["main", "develop", "feature/login"],
    "latestCommit": {
      "commitId": "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0",
      "message": "feat: 完成图书借阅功能", "author": "张三",
      "committedAt": "2026-06-29T22:30:00.000+08:00"
    }
  },
  "success": true,
  "timestamp": "2026-06-30T10:00:05.000+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678915",
  "path": "/api/v1/student/tasks/1234567890123456789/git-verify", "elapsed": 2340
}
```

**Response（验证失败：仓库不存在）**：
```json
{
  "code": 8001, "message": "仓库地址无效，请检查是否为有效的公开仓库地址",
  "data": {"valid": false}, "success": false
}
```

**Mock策略**：URL包含"valid-repo"返回成功，包含"private-repo"返回8002（无权限），其他返回8001。模拟2-4秒延迟。

### 6.3.7 发起AI评价

- **接口名称**：学生提交后请求AI进行分析评价
- **Method + URL**：POST /api/v1/student/submissions/{submissionId}/ai-evaluate
- **权限**：STUDENT
- **路径参数**：submissionId（String，提交ID）
- **Request Body**：无

**Response（成功，AI任务已创建）**：
```json
{
  "code": 0, "message": "AI分析任务已创建",
  "data": {
    "analyzeId": "6789012345678901234", "submissionId": "4567890123456789012",
    "status": "PENDING", "estimatedSeconds": 30,
    "createdAt": "2026-06-30T10:01:00.000+08:00"
  },
  "success": true
}
```

**后续流程**：学生提交 -> 系统创建AI分析任务 -> 前端轮询6.8接口获取分析进度 -> 分析完成后展示结果。

**Mock策略**：创建任务后，Mock模拟5秒后状态变为PROCESSING，15秒后变为COMPLETED。

### 6.3.8 AI评价结果

- **接口名称**：获取AI分析评价的进度和结果
- **Method + URL**：GET /api/v1/student/submissions/{submissionId}/ai-result
- **权限**：STUDENT
- **路径参数**：submissionId（String，提交ID）

**Response（AI分析进行中）**：
```json
{
  "code": 0, "message": "success",
  "data": {
    "analyzeId": "6789012345678901234", "status": "PROCESSING",
    "progress": 60, "currentDimension": "功能完成度", "result": null,
    "startedAt": "2026-06-30T10:01:05.000+08:00"
  },
  "success": true
}
```

**Response（AI分析完成）**：
```json
{
  "code": 0, "message": "success",
  "data": {
    "analyzeId": "6789012345678901234", "status": "COMPLETED", "progress": 100,
    "currentDimension": null,
    "result": {
      "overallScore": 82,
      "dimensions": [
        {
          "dimensionName": "代码规范", "score": 20, "maxScore": 25, "weight": 25,
          "comment": "代码整体规范，方法命名符合Java规范，但部分方法缺少JavaDoc注释",
          "suggestions": ["建议为所有public方法添加JavaDoc注释","部分变量命名过于简略（如a、b、tmp），建议使用有意义的名字"],
          "codeReferences": ["BookController.java:45 - 缺少方法注释","BookService.java:23 - 变量名'a'不够语义化"]
        },
        {
          "dimensionName": "功能完成度", "score": 26, "maxScore": 30, "weight": 30,
          "comment": "基本功能全部实现，但缺少图书分类查询和借阅历史分页功能",
          "suggestions": ["增加图书按分类筛选的功能","借阅历史页面增加分页功能以提升用户体验"],
          "codeReferences": ["BookController.java:78 - 查询接口缺少分类参数","BorrowHistory.vue:34 - 列表未分页"]
        },
        {
          "dimensionName": "创新性", "score": 15, "maxScore": 20, "weight": 20,
          "comment": "使用了缓存优化热门图书查询，但整体设计缺乏亮点",
          "suggestions": ["可增加图书推荐功能（基于借阅历史）","建议引入Elasticsearch实现图书全文搜索"],
          "codeReferences": []
        },
        {
          "dimensionName": "文档完整性", "score": 12, "maxScore": 15, "weight": 15,
          "comment": "实训报告内容完整，包含需求分析、设计文档、测试报告，但缺少部署文档",
          "suggestions": ["补充Docker部署文档","API文档建议使用Swagger自动生成"],
          "codeReferences": []
        },
        {
          "dimensionName": "Git规范", "score": 9, "maxScore": 10, "weight": 10,
          "comment": "提交记录清晰，commit message规范，分支管理合理",
          "suggestions": ["部分commit粒度偏大，建议更细粒度的提交"],
          "codeReferences": []
        }
      ],
      "summary": "该同学较好地完成了图书管理系统的基本功能开发。代码整体规范，功能实现完整。主要改进方向：1）补充JavaDoc注释；2）增加图书分类查询功能；3）完善部署文档。建议在细节优化和文档完善方面继续提升。",
      "strengths": ["功能实现完整，核心CRUD和借阅归还功能均正常","Git提交规范，commit message清晰","使用了Redis缓存优化性能"],
      "weaknesses": ["代码注释不够完善","缺少部分高级功能（分类查询、全文搜索）","部署文档缺失"],
      "improvementPlan": "1.补充JavaDoc和关键逻辑注释；2.增加图书分类筛选和搜索功能；3.编写Docker部署文档；4.添加更多单元测试用例"
    },
    "startedAt": "2026-06-30T10:01:05.000+08:00",
    "completedAt": "2026-06-30T10:01:25.000+08:00"
  },
  "success": true,
  "timestamp": "2026-06-30T10:01:25.000+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678917",
  "path": "/api/v1/student/submissions/4567890123456789012/ai-result", "elapsed": 5
}
```

**AI结果核心字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| overallScore | Integer | AI评分总分（0-100） |
| dimensions | Array | 各维度评分详情数组 |
| dimensions[].dimensionName | String | 维度名称 |
| dimensions[].score | Integer | 该维度得分 |
| dimensions[].maxScore | Integer | 该维度满分 |
| dimensions[].weight | Integer | 权重（百分比） |
| dimensions[].comment | String | AI评语 |
| dimensions[].suggestions | Array[String] | 改进建议列表 |
| dimensions[].codeReferences | Array[String] | 引用的代码位置 |
| summary | String | 总评语 |
| strengths | Array[String] | 优点列表 |
| weaknesses | Array[String] | 不足列表 |
| improvementPlan | String | 改进计划 |

**Mock策略**：使用固定模拟数据，5个维度均包含完整评语和建议。轮询模式下，前3次返回PROCESSING（progress递增），第4次返回COMPLETED。

### 6.3.9 评价详情（含教师评分）

- **接口名称**：获取完整评价详情（AI评分+教师评分）
- **Method + URL**：GET /api/v1/student/submissions/{submissionId}/evaluation
- **权限**：STUDENT
- **路径参数**：submissionId（String，提交ID）

**Response（成功：已复核）**：
```json
{
  "code": 0, "message": "success",
  "data": {
    "submissionId": "4567890123456789012",
    "taskName": "Spring Boot图书管理系统实训", "courseName": "Java Web开发",
    "submittedAt": "2026-06-30T10:00:00.000+08:00",
    "aiEvaluation": {
      "overallScore": 82,
      "summary": "该同学较好地完成了图书管理系统的基本功能开发...",
      "completedAt": "2026-06-30T10:01:25.000+08:00"
    },
    "teacherEvaluation": {
      "overallScore": 80,
      "comment": "AI评价总体客观，功能完成度评分偏高，扣减2分。代码注释确实不足，需加强。其他维度评分合理。",
      "dimensions": [
        {"dimensionName": "代码规范", "score": 18, "maxScore": 25},
        {"dimensionName": "功能完成度", "score": 24, "maxScore": 30},
        {"dimensionName": "创新性", "score": 15, "maxScore": 20},
        {"dimensionName": "文档完整性", "score": 14, "maxScore": 15},
        {"dimensionName": "Git规范", "score": 9, "maxScore": 10}
      ],
      "scoredBy": "王建国", "scoredAt": "2026-07-01T14:00:00.000+08:00",
      "publishedAt": "2026-07-01T15:00:00.000+08:00"
    },
    "status": "COMPLETED", "finalScore": 80
  },
  "success": true
}
```

**Mock策略**：教师未评分时teacherEvaluation=null。已评分时展示完整评分数据。教师退回时status=REJECTED且包含rejectReason字段。

### 6.3.10 成长档案

- **接口名称**：获取学生个人成长档案（跨课程/跨学期能力发展数据）
- **Method + URL**：GET /api/v1/student/growth-profile
- **权限**：STUDENT

**Response（成功）**：
```json
{
  "code": 0, "message": "success",
  "data": {
    "studentInfo": {
      "userId": "1234567890123456789", "realName": "张三",
      "studentNo": "20240101001", "className": "软件技术2401班",
      "totalCourses": 6, "totalTasks": 15, "averageScore": 84.2
    },
    "radarData": {
      "categories": ["代码规范","功能完成度","创新设计","文档撰写","团队协作","工程素养"],
      "current": [78, 85, 72, 80, 75, 82],
      "classAverage": [72, 78, 68, 74, 70, 75]
    },
    "scoreTrend": {
      "xAxis": ["2026-03","2026-04","2026-05","2026-06"],
      "series": [
        {"name": "张三", "data": [78, 82, 85, 80]},
        {"name": "班级平均", "data": [72, 74, 76, 75]}
      ]
    },
    "dimensionHistory": [
      {"dimensionName": "代码规范", "scores": [70, 75, 78, 78], "trend": "UP"},
      {"dimensionName": "功能完成度", "scores": [80, 82, 85, 85], "trend": "UP"},
      {"dimensionName": "创新设计", "scores": [65, 68, 70, 72], "trend": "UP"}
    ],
    "recentTasks": [
      {"taskId": "1234567890123456789", "taskName": "Spring Boot图书管理系统实训", "courseName": "Java Web开发", "score": 80, "completedAt": "2026-07-01T15:00:00.000+08:00"}
    ]
  },
  "success": true,
  "timestamp": "2026-06-30T10:06:00.000+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678919",
  "path": "/api/v1/student/growth-profile", "elapsed": 45
}
```

**Mock策略**：固定随机化数据。雷达图数据、趋势图数据、维度历史数据使用预设模板。

### 6.3.11 导出个人报告PDF

- **接口名称**：导出学生个人成长报告为PDF
- **Method + URL**：GET /api/v1/student/reports/export
- **权限**：STUDENT
- **Query参数**：semester（可选，学期筛选，如"2026-SPRING"）

**Response**：二进制流，Content-Type: application/pdf，Content-Disposition: attachment; filename="成长报告_张三_2026春季学期.pdf"

**Mock策略**：返回一个小的示例PDF文件（Mock预置）。

### 6.3.12 消息通知列表

- **接口名称**：获取学生消息通知列表
- **Method + URL**：GET /api/v1/student/notifications
- **权限**：STUDENT
- **分页**：是
- **过滤**：支持isRead（Boolean）、type（通知类型）

**Response（分页成功）**：
```json
{
  "code": 0, "message": "success",
  "data": {
    "list": [
      {
        "notificationId": "7890123456789012345", "type": "SCORE_PUBLISHED",
        "title": "成绩已发布",
        "content": "您的Spring Boot图书管理系统实训最终成绩为80分，点击查看详情。",
        "relatedId": "4567890123456789012", "relatedType": "SUBMISSION",
        "isRead": false, "createdAt": "2026-07-01T15:00:00.000+08:00"
      },
      {
        "notificationId": "7890123456789012346", "type": "REMINDER",
        "title": "提交提醒",
        "content": "Java Web开发课程的任务Vue 3组件库实训即将于2026-07-10截止，请及时提交。",
        "relatedId": "1234567890123456790", "relatedType": "TASK",
        "isRead": true, "createdAt": "2026-07-01T10:00:00.000+08:00"
      }
    ],
    "page": 1, "pageSize": 20, "total": 12, "totalPages": 1
  },
  "success": true
}
```

**通知类型枚举**：

| 类型值 | 说明 |
|--------|------|
| SCORE_PUBLISHED | 成绩已发布 |
| TASK_REJECTED | 成果被退回 |
| DEADLINE_REMINDER | 截止提醒 |
| REMINDER | 催交提醒 |
| AI_COMPLETED | AI分析完成 |
| SYSTEM | 系统通知 |

**Mock策略**：随机生成10-15条通知，混合已读/未读状态。

### 6.3.13 标记消息已读

- **接口名称**：标记单条消息为已读
- **Method + URL**：PUT /api/v1/student/notifications/{notificationId}/read
- **权限**：STUDENT

**Response**：成功返回code=0, data=null。

### 6.3.14 全部标记已读

- **接口名称**：标记当前用户所有消息为已读
- **Method + URL**：PUT /api/v1/student/notifications/read-all
- **权限**：STUDENT

**Response**：成功返回code=0, data=null。
**Mock策略**：两个已读接口均始终返回成功。

---

---

# 第七章 教师端接口

## 7.1 章节概述

本章定义教师端（TEACHER角色）的所有接口。教师端是系统的核心业务模块，涵盖首页统计、班级管理、任务管理、学生管理、提交记录查看、AI初评审核、人工评分、成绩发布、退回整改、报表导出等功能。所有接口需要TEACHER角色权限。教师只能查看和管理自己授课的班级、任务和学生数据。

## 7.2 接口清单

| 序号 | 接口名称 | Method | URL | 权限 |
|------|---------|--------|-----|------|
| 7.1 | 教师首页统计 | GET | /api/v1/teacher/dashboard | TEACHER |
| 7.2 | 班级列表 | GET | /api/v1/teacher/classes | TEACHER |
| 7.3 | 班级详情 | GET | /api/v1/teacher/classes/{classId} | TEACHER |
| 7.4 | 任务列表 | GET | /api/v1/teacher/tasks | TEACHER |
| 7.5 | 创建任务 | POST | /api/v1/teacher/tasks | TEACHER |
| 7.6 | 修改任务 | PUT | /api/v1/teacher/tasks/{taskId} | TEACHER |
| 7.7 | 删除任务 | DELETE | /api/v1/teacher/tasks/{taskId} | TEACHER |
| 7.8 | 学生列表 | GET | /api/v1/teacher/classes/{classId}/students | TEACHER |
| 7.9 | 提交记录列表 | GET | /api/v1/teacher/tasks/{taskId}/submissions | TEACHER |
| 7.10 | AI初评结果 | GET | /api/v1/teacher/submissions/{submissionId}/ai-preview | TEACHER |
| 7.11 | 人工评分 | PUT | /api/v1/teacher/submissions/{submissionId}/score | TEACHER |
| 7.12 | 发布成绩 | POST | /api/v1/teacher/tasks/{taskId}/publish-scores | TEACHER |
| 7.13 | 退回整改 | POST | /api/v1/teacher/submissions/{submissionId}/reject | TEACHER |
| 7.14 | 班级报表 | GET | /api/v1/teacher/classes/{classId}/report | TEACHER |
| 7.15 | Excel导出 | GET | /api/v1/teacher/tasks/{taskId}/export-excel | TEACHER |
| 7.16 | 通知催交 | POST | /api/v1/teacher/tasks/{taskId}/remind | TEACHER |

## 7.3 接口详情

### 7.3.1 教师首页统计

- **接口名称**：获取教师首页统计概览
- **Method + URL**：GET /api/v1/teacher/dashboard
- **权限**：TEACHER

**Response（成功）**：
```json
{
  "code": 0, "message": "success",
  "data": {
    "overview": {
      "totalClasses": 3, "totalStudents": 120, "totalTasks": 8,
      "submissionRate": 0.85, "gradingRate": 0.62, "averageScore": 78.5,
      "pendingReviews": 15
    },
    "classRankings": [
      {"classId": "1111111111111111111", "className": "软件技术2401班", "averageScore": 82.3, "submissionRate": 0.92},
      {"classId": "2222222222222222222", "className": "软件技术2402班", "averageScore": 76.8, "submissionRate": 0.85}
    ],
    "recentSubmissions": [
      {"submissionId": "4567890123456789012", "studentName": "张三", "taskName": "Spring Boot图书管理系统实训", "status": "AI_COMPLETED", "submittedAt": "2026-06-30T10:00:00.000+08:00"}
    ],
    "deadlineTasks": [
      {"taskId": "1234567890123456789", "taskName": "Spring Boot图书管理系统实训", "deadline": "2026-07-15T23:59:59.000+08:00", "submissionCount": 35, "totalStudents": 40}
    ]
  },
  "success": true
}
```

**Mock策略**：随机生成统计数据，班级排名数据与Mock班级数据一致。

### 7.3.2 班级列表

- **接口名称**：获取教师授课班级列表
- **Method + URL**：GET /api/v1/teacher/classes
- **权限**：TEACHER
- **分页**：是

**Response（分页成功）**：
```json
{
  "code": 0, "message": "success",
  "data": {
    "list": [
      {"classId": "1111111111111111111", "className": "软件技术2401班", "college": "信息工程学院", "studentCount": 40, "courseName": "Java Web开发", "semester": "2026-SPRING", "createdAt": "2026-03-01T00:00:00.000+08:00"}
    ],
    "page": 1, "pageSize": 20, "total": 3, "totalPages": 1
  },
  "success": true
}
```

**Mock策略**：随机生成2-4个班级。

### 7.3.3 班级详情

- **接口名称**：获取班级详细信息
- **Method + URL**：GET /api/v1/teacher/classes/{classId}
- **权限**：TEACHER
- **路径参数**：classId（String）

**Response（成功）**：
```json
{
  "code": 0, "message": "success",
  "data": {
    "classId": "1111111111111111111", "className": "软件技术2401班",
    "college": "信息工程学院", "studentCount": 40,
    "courseName": "Java Web开发", "semester": "2026-SPRING",
    "tasks": [
      {"taskId": "1234567890123456789", "taskName": "Spring Boot图书管理系统实训", "deadline": "2026-07-15T23:59:59.000+08:00", "status": "PUBLISHED", "submissionRate": 0.88}
    ],
    "createdAt": "2026-03-01T00:00:00.000+08:00"
  },
  "success": true
}
```

### 7.3.4 任务列表（教师视角）

- **接口名称**：获取教师创建的任务列表
- **Method + URL**：GET /api/v1/teacher/tasks
- **权限**：TEACHER
- **分页**：是
- **过滤**：支持keyword、status、classId、courseName

**Response（分页成功）**：
```json
{
  "code": 0, "message": "success",
  "data": {
    "list": [
      {"taskId": "1234567890123456789", "taskName": "Spring Boot图书管理系统实训", "courseName": "Java Web开发", "classCount": 2, "totalStudents": 80, "submittedCount": 70, "gradedCount": 50, "status": "PUBLISHED", "deadline": "2026-07-15T23:59:59.000+08:00", "createdAt": "2026-06-25T10:00:00.000+08:00"}
    ],
    "page": 1, "pageSize": 20, "total": 8, "totalPages": 1
  },
  "success": true
}
```

### 7.3.5 创建任务

- **接口名称**：教师创建新实训任务
- **Method + URL**：POST /api/v1/teacher/tasks
- **权限**：TEACHER
- **Content-Type**：application/json

**Request Body**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| taskName | String | 是 | 任务名称，最长100字符 |
| courseId | String | 是 | 所属课程ID |
| classIds | Array[String] | 是 | 下发班级ID列表 |
| description | String | 是 | 任务描述和要求 |
| deadline | String | 是 | 截止时间（ISO 8601） |
| totalScore | Integer | 是 | 任务总分，默认100 |
| submissionType | String | 是 | 提交方式：GIT_ZIP/ZIP_ONLY/ONLINE_CODE/FILE_UPLOAD |
| submitLimit | Integer | 否 | 提交次数限制，默认3 |
| evaluationTemplateId | String | 否 | 评分模板ID，不传则使用默认模板 |
| attachments | Array[String] | 否 | 附件文件ID列表 |

```json
{
  "taskName": "Spring Boot微服务实训",
  "courseId": "2222222222222222222",
  "classIds": ["1111111111111111111"],
  "description": "基于Spring Cloud实现微服务架构的电商系统...",
  "deadline": "2026-08-01T23:59:59.000+08:00",
  "totalScore": 100,
  "submissionType": "GIT_ZIP",
  "submitLimit": 3
}
```

**Response（成功）**：
```json
{
  "code": 0, "message": "任务创建成功",
  "data": {"taskId": "1234567890123456790", "createdAt": "2026-06-30T14:30:00.000+08:00"},
  "success": true
}
```

**Mock策略**：校验必填字段，不全则返回4001。

### 7.3.6 修改任务

- **接口名称**：修改已有任务信息
- **Method + URL**：PUT /api/v1/teacher/tasks/{taskId}
- **权限**：TEACHER
- **Content-Type**：application/json

**Request Body**：同创建任务，所有字段可选（仅更新传入的字段）。
**Response（成功）**：
```json
{
  "code": 0, "message": "任务修改成功",
  "data": {"taskId": "1234567890123456789", "updatedAt": "2026-06-30T15:00:00.000+08:00"},
  "success": true
}
```

**注意**：已截止的任务或已有学生提交的任务，仅允许修改description和attachments，不允许修改deadline、totalScore、evaluationTemplateId。

### 7.3.7 删除任务

- **接口名称**：删除任务
- **Method + URL**：DELETE /api/v1/teacher/tasks/{taskId}
- **权限**：TEACHER

**Response（成功）**：code=0, data=null。
**Response（失败）**：有学生已提交的任务不允许删除，返回code=1003。

**Mock策略**：taskId末尾为"1"的返回成功，末尾为"2"的返回1003。

### 7.3.8 学生列表

- **接口名称**：获取班级学生列表
- **Method + URL**：GET /api/v1/teacher/classes/{classId}/students
- **权限**：TEACHER
- **分页**：是
- **过滤**：支持keyword（姓名/学号搜索）

**Response（分页成功）**：
```json
{
  "code": 0, "message": "success",
  "data": {
    "list": [
      {"userId": "1234567890123456789", "studentNo": "20240101001", "realName": "张三", "email": "zhangsan@example.com", "submittedTasks": 5, "totalTasks": 8, "averageScore": 84.2, "status": "ENABLED"}
    ],
    "page": 1, "pageSize": 20, "total": 40, "totalPages": 2
  },
  "success": true
}
```

**Mock策略**：随机生成30-45名学生数据。

### 7.3.9 提交记录列表

- **接口名称**：获取某任务的所有学生提交记录
- **Method + URL**：GET /api/v1/teacher/tasks/{taskId}/submissions
- **权限**：TEACHER
- **分页**：是
- **过滤**：支持status（提交状态）、keyword（学生姓名搜索）

**Response（分页成功）**：
```json
{
  "code": 0, "message": "success",
  "data": {
    "list": [
      {"submissionId": "4567890123456789012", "studentName": "张三", "studentNo": "20240101001", "submissionType": "GIT_URL", "aiScore": 82, "teacherScore": null, "status": "AI_COMPLETED", "submitCount": 1, "submittedAt": "2026-06-30T10:00:00.000+08:00"}
    ],
    "page": 1, "pageSize": 20, "total": 38, "totalPages": 2
  },
  "success": true
}
```

**提交状态流转**：SUBMITTED -> AI_EVALUATING -> AI_COMPLETED -> TEACHER_SCORING -> COMPLETED / REJECTED。

### 7.3.10 AI初评结果

- **接口名称**：查看AI初评结果（教师审核用）
- **Method + URL**：GET /api/v1/teacher/submissions/{submissionId}/ai-preview
- **权限**：TEACHER

**Response**：与6.3.8的AI评价结果结构相同，额外增加studentInfo字段（学生基本信息）。教师可查看AI给出的评分、评语、代码引用，作为人工评分的参考依据。

### 7.3.11 人工评分

- **接口名称**：教师对提交进行人工评分
- **Method + URL**：PUT /api/v1/teacher/submissions/{submissionId}/score
- **权限**：TEACHER
- **Content-Type**：application/json

**Request Body**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| overallScore | Integer | 是 | 总分（0到任务总分） |
| dimensions | Array | 是 | 各维度评分数组 |
| dimensions[].dimensionName | String | 是 | 维度名称 |
| dimensions[].score | Integer | 是 | 该维度得分 |
| comment | String | 是 | 教师总评语 |
| isPublished | Boolean | 否 | 是否同时发布成绩，默认false |

```json
{
  "overallScore": 80,
  "dimensions": [
    {"dimensionName": "代码规范", "score": 18},
    {"dimensionName": "功能完成度", "score": 24},
    {"dimensionName": "创新性", "score": 15},
    {"dimensionName": "文档完整性", "score": 14},
    {"dimensionName": "Git规范", "score": 9}
  ],
  "comment": "整体完成质量良好，功能基本实现。代码注释需要加强，建议补充JavaDoc。",
  "isPublished": false
}
```

**校验规则**：各维度分数之和必须等于overallScore。

**Response（成功）**：
```json
{
  "code": 0, "message": "评分保存成功",
  "data": {"submissionId": "4567890123456789012", "status": "TEACHER_SCORING", "scoredAt": "2026-07-01T14:00:00.000+08:00"},
  "success": true
}
```

**Mock策略**：校验维度分总和等于总分，不等则返回4003。

### 7.3.12 发布成绩

- **接口名称**：批量发布任务成绩
- **Method + URL**：POST /api/v1/teacher/tasks/{taskId}/publish-scores
- **权限**：TEACHER

**Request Body**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| submissionIds | Array[String] | 否 | 指定发布的提交ID列表。不传则发布所有已评分未发布的 |
| notifyStudents | Boolean | 否 | 是否通知学生，默认true |

**Response（成功）**：
```json
{
  "code": 0, "message": "成绩发布成功",
  "data": {"publishedCount": 35, "publishedAt": "2026-07-01T15:00:00.000+08:00"},
  "success": true
}
```

### 7.3.13 退回整改

- **接口名称**：退回学生提交要求整改
- **Method + URL**：POST /api/v1/teacher/submissions/{submissionId}/reject
- **权限**：TEACHER
- **Content-Type**：application/json

**Request Body**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| rejectReason | String | 是 | 退回原因，最长500字符 |

**Response（成功）**：
```json
{
  "code": 0, "message": "已退回，学生可重新提交",
  "data": {"status": "REJECTED", "rejectedAt": "2026-07-01T14:00:00.000+08:00"},
  "success": true
}
```

### 7.3.14 班级报表

- **接口名称**：获取班级实训统计报表
- **Method + URL**：GET /api/v1/teacher/classes/{classId}/report
- **权限**：TEACHER

**Response（成功）**：
```json
{
  "code": 0, "message": "success",
  "data": {
    "classInfo": {"className": "软件技术2401班", "studentCount": 40, "semester": "2026-SPRING"},
    "overallStats": {"submissionRate": 0.92, "averageScore": 82.3, "passRate": 0.95, "excellentRate": 0.35},
    "scoreDistribution": [
      {"range": "90-100", "count": 14, "label": "优秀"},
      {"range": "80-89", "count": 16, "label": "良好"},
      {"range": "60-79", "count": 8, "label": "及格"},
      {"range": "0-59", "count": 2, "label": "不及格"}
    ],
    "taskStatistics": [
      {"taskName": "Spring Boot图书管理系统实训", "averageScore": 80.5, "submissionRate": 0.95}
    ],
    "dimensionAverages": [
      {"dimensionName": "代码规范", "average": 20.5, "maxScore": 25},
      {"dimensionName": "功能完成度", "average": 25.2, "maxScore": 30}
    ]
  },
  "success": true
}
```

### 7.3.15 Excel导出

- **接口名称**：导出任务成绩为Excel
- **Method + URL**：GET /api/v1/teacher/tasks/{taskId}/export-excel
- **权限**：TEACHER
- **Query参数**：classId（可选，筛选班级）

**Response**：二进制流，Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet，Content-Disposition: attachment; filename="成绩表_SpringBoot实训_20260701.xlsx"

**Mock策略**：返回一个小的示例Excel文件。

### 7.3.16 通知催交

- **接口名称**：教师向未提交学生发送催交通知
- **Method + URL**：POST /api/v1/teacher/tasks/{taskId}/remind
- **权限**：TEACHER
- **Request Body**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| studentIds | Array[String] | 否 | 指定学生ID列表。不传则提醒所有未提交学生 |
| message | String | 否 | 自定义催交文案，最长200字符 |

**Response（成功）**：
```json
{
  "code": 0, "message": "催交通知已发送",
  "data": {"remindedCount": 5},
  "success": true
}
```

**Mock策略**：始终返回成功，remindedCount随机1-10。

---

---

# 第八章 教师扩展接口（原教研负责人职能）

## 8.1 章节概述

本章定义教师端扩展功能接口（原教研负责人职能，现归属于教师角色）。教师在原有教学管理职能基础上，扩展负责课程体系管理、评分标准制定、Prompt模板配置、全院教学数据分析和报表导出。所有接口需要TEACHER角色权限。

## 8.2 接口清单

| 序号 | 接口名称 | Method | URL | 权限 |
|------|---------|--------|-----|------|
| 8.1 | 教研首页统计 | GET | /api/v1/teacher/dashboard | TEACHER |
| 8.2 | 课程列表 | GET | /api/v1/teacher/courses | TEACHER |
| 8.3 | 创建课程 | POST | /api/v1/teacher/courses | TEACHER |
| 8.4 | 修改课程 | PUT | /api/v1/teacher/courses/{courseId} | TEACHER |
| 8.5 | 删除课程 | DELETE | /api/v1/teacher/courses/{courseId} | TEACHER |
| 8.6 | 评分模板列表 | GET | /api/v1/teacher/score-templates | TEACHER |
| 8.7 | 创建评分模板 | POST | /api/v1/teacher/score-templates | TEACHER |
| 8.8 | Prompt模板列表 | GET | /api/v1/teacher/prompt-templates | TEACHER |
| 8.9 | 创建Prompt模板 | POST | /api/v1/teacher/prompt-templates | TEACHER |
| 8.10 | Prompt历史版本 | GET | /api/v1/teacher/prompt-templates/{templateId}/versions | TEACHER |
| 8.11 | 评分权重配置 | PUT | /api/v1/teacher/courses/{courseId}/weights | TEACHER |
| 8.12 | 统计分析 | GET | /api/v1/teacher/statistics | TEACHER |
| 8.13 | 学院报表 | GET | /api/v1/teacher/reports/college | TEACHER |
| 8.14 | Word导出 | GET | /api/v1/teacher/reports/export-word | TEACHER |
| 8.15 | PDF导出 | GET | /api/v1/teacher/reports/export-pdf | TEACHER |

## 8.3 接口详情

### 8.3.1 教研首页统计

- **接口名称**：获取教师端全学院统计概览
- **Method + URL**：GET /api/v1/teacher/dashboard
- **权限**：TEACHER

**Response（成功）**：
```json
{
  "code": 0, "message": "success",
  "data": {
    "overview": {"totalCourses": 12, "totalClasses": 8, "totalTeachers": 6, "totalStudents": 320, "overallSubmissionRate": 0.88, "overallAverageScore": 79.3},
    "collegeTrend": {"xAxis": ["2025-FALL","2026-SPRING"], "series": [{"name":"平均分","data":[76.5,79.3]},{"name":"提交率","data":[0.85,0.88]}]},
    "courseRankings": [{"courseName":"Java Web开发","averageScore":82.1,"classCount":3}],
    "aiUsageStats": {"totalAnalyses": 520, "averageTime": 18.5, "successRate": 0.94, "tokenUsed": 12500000}
  },
  "success": true
}
```

### 8.3.2 课程列表

- **接口名称**：获取全院课程列表
- **Method + URL**：GET /api/v1/teacher/courses
- **权限**：TEACHER
- **分页**：是

**Response**：list包含courseId、courseName、courseCode、teacherName、classCount、studentCount、status、semester、createdAt。

### 8.3.3 创建课程

- **接口名称**：创建新课程
- **Method + URL**：POST /api/v1/teacher/courses
- **权限**：TEACHER
- **Request Body**：courseName（必填）、courseCode（必填）、description、teacherIds、semester、college

```json
{ "courseName": "软件工程实训", "courseCode": "SE301", "description": "软件工程综合实训课程", "teacherIds": ["3333333333333333333"], "semester": "2026-FALL", "college": "信息工程学院" }
```

**Response（成功）**：
```json
{ "code": 0, "message": "课程创建成功", "data": {"courseId": "4444444444444444444", "createdAt": "2026-06-30T16:00:00.000+08:00"}, "success": true }
```

### 8.3.4 修改课程 & 8.3.5 删除课程

**修改课程**：PUT /api/v1/teacher/courses/{courseId}，Request Body同创建（字段可选）。
**删除课程**：DELETE /api/v1/teacher/courses/{courseId}，有关联任务或学生的课程禁止删除（返回code=1003）。

### 8.3.6 评分模板列表

- **接口名称**：获取评分模板列表
- **Method + URL**：GET /api/v1/teacher/score-templates
- **权限**：TEACHER
- **分页**：是

**Response**：list每项包含templateId、templateName、dimensionCount（维度数）、isDefault、usedByCourses、createdAt。

### 8.3.7 创建评分模板

- **接口名称**：创建评分模板
- **Method + URL**：POST /api/v1/teacher/score-templates
- **权限**：TEACHER
- **Request Body**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| templateName | String | 是 | 模板名称 |
| description | String | 否 | 模板说明 |
| isDefault | Boolean | 否 | 是否设为默认模板 |
| dimensions | Array | 是 | 评分维度数组 |
| dimensions[].dimensionName | String | 是 | 维度名称 |
| dimensions[].weight | Integer | 是 | 权重（百分比） |
| dimensions[].maxScore | Integer | 是 | 满分值 |
| dimensions[].description | String | 否 | 维度评分说明 |

```json
{
  "templateName": "软件工程标准评分模板",
  "description": "适用于软件工程类实训的标准评分体系",
  "isDefault": false,
  "dimensions": [
    {"dimensionName": "代码规范","weight": 25,"maxScore": 25,"description": "代码风格、命名规范、注释完整性"},
    {"dimensionName": "功能完成度","weight": 30,"maxScore": 30,"description": "需求功能实现的完整性和正确性"},
    {"dimensionName": "创新性","weight": 20,"maxScore": 20,"description": "技术方案的新颖性和优化程度"},
    {"dimensionName": "文档完整性","weight": 15,"maxScore": 15,"description": "需求文档、设计文档、测试报告的完整性"},
    {"dimensionName": "Git规范","weight": 10,"maxScore": 10,"description": "提交记录规范性、分支管理合理性"}
  ]
}
```

**校验规则**：所有权重之和必须等于100。所有maxScore之和建议等于100（非强制）。
**Mock策略**：校验权重之和，不等则返回4003。

### 8.3.8 Prompt模板列表

- **接口名称**：获取Prompt模板列表
- **Method + URL**：GET /api/v1/teacher/prompt-templates
- **权限**：TEACHER
- **分页**：是
- **过滤**：支持type（CODE_ANALYSIS/DOC_ANALYSIS/COMPREHENSIVE_SCORING）

**Response**：list每项包含templateId、templateName、type、version、isActive、updatedAt。

### 8.3.9 创建Prompt模板

- **接口名称**：创建Prompt模板
- **Method + URL**：POST /api/v1/teacher/prompt-templates
- **权限**：TEACHER
- **Request Body**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| templateName | String | 是 | 模板名称 |
| type | String | 是 | CODE_ANALYSIS / DOC_ANALYSIS / COMPREHENSIVE_SCORING |
| content | String | 是 | Prompt内容，支持变量占位符如{{codeContent}} |
| variables | Array[String] | 否 | 模板变量列表 |
| description | String | 否 | 模板说明 |

```json
{
  "templateName": "代码规范分析Prompt",
  "type": "CODE_ANALYSIS",
  "content": "请分析以下代码的规范性：
1.命名规范
2.注释完整性
3.代码结构
4.异常处理

代码内容：
{{codeContent}}",
  "variables": ["codeContent"],
  "description": "用于分析学生代码规范性的标准Prompt"
}
```

### 8.3.10 Prompt历史版本

- **接口名称**：获取Prompt模板的历史版本列表
- **Method + URL**：GET /api/v1/teacher/prompt-templates/{templateId}/versions
- **权限**：TEACHER

**Response**：list每项包含versionId、versionNumber、content、changeNote、createdBy、createdAt。

### 8.3.11 评分权重配置

- **接口名称**：配置课程的评分维度和权重
- **Method + URL**：PUT /api/v1/teacher/courses/{courseId}/weights
- **权限**：TEACHER
- **Request Body**：与评分模板dimensions结构相同。

### 8.3.12 统计分析

- **接口名称**：获取全院多维度教学统计分析数据
- **Method + URL**：GET /api/v1/teacher/statistics
- **权限**：TEACHER
- **Query参数**：semester、college、startDate、endDate

**Response**：包含collegeOverview（全院总览）、courseComparison（课程对比数据）、dimensionAnalysis（维度分析数据）、trendData（趋势数据，含多学期对比图表数据）、teacherPerformance（教师教学效果数据）。

### 8.3.13 学院报表

- **接口名称**：获取学院综合教学报表数据
- **Method + URL**：GET /api/v1/teacher/reports/college
- **权限**：TEACHER

**Response**：综合统计+趋势分析+对比数据，包含collegeInfo、overallStats、scoreDistribution、courseDetail、semesterComparison、aiUsageSummary。

### 8.3.14 Word导出 & 8.3.15 PDF导出

**Word导出**：GET /api/v1/teacher/reports/export-word，Response为二进制流（application/vnd.openxmlformats-officedocument.wordprocessingml.document）。
**PDF导出**：GET /api/v1/teacher/reports/export-pdf，Response为二进制流（application/pdf）。
**Query参数**：semester、college、startDate、endDate。
**Mock策略**：返回预置示例文件。

**Mock策略（教师扩展全部接口）**：统计数据使用随机但内部一致的Mock数据。评分模板和Prompt模板使用固定Mock数据。

---

---

# 第九章 管理员接口

## 9.1 章节概述

本章定义平台管理员端（ADMIN角色）的所有接口。管理员负责系统的基础运维，包括用户账号管理、角色权限管理、菜单管理、系统监控、日志审计、数据备份和模型配置。所有接口需要ADMIN角色权限。

## 9.2 接口清单

| 序号 | 接口名称 | Method | URL | 权限 |
|------|---------|--------|-----|------|
| 9.1 | 管理员首页 | GET | /api/v1/admin/dashboard | ADMIN |
| 9.2 | 用户列表 | GET | /api/v1/admin/users | ADMIN |
| 9.3 | 创建用户 | POST | /api/v1/admin/users | ADMIN |
| 9.4 | 修改用户 | PUT | /api/v1/admin/users/{userId} | ADMIN |
| 9.5 | 删除用户 | DELETE | /api/v1/admin/users/{userId} | ADMIN |
| 9.6 | 角色列表 | GET | /api/v1/admin/roles | ADMIN |
| 9.7 | 创建角色 | POST | /api/v1/admin/roles | ADMIN |
| 9.8 | 权限列表 | GET | /api/v1/admin/permissions | ADMIN |
| 9.9 | 菜单列表 | GET | /api/v1/admin/menus | ADMIN |
| 9.10 | 日志查询 | GET | /api/v1/admin/logs | ADMIN |
| 9.11 | 系统监控 | GET | /api/v1/admin/monitor | ADMIN |
| 9.12 | Token监控 | GET | /api/v1/admin/token-monitor | ADMIN |
| 9.13 | 备份管理 | POST | /api/v1/admin/backup | ADMIN |
| 9.14 | 模型配置 | GET/PUT | /api/v1/admin/model-config | ADMIN |

## 9.3 接口详情

### 9.3.1 管理员首页

- **接口名称**：获取管理员首页系统概览
- **Method + URL**：GET /api/v1/admin/dashboard
- **权限**：ADMIN

**Response**：包含systemStatus（CPU/内存/磁盘/JVM指标）、userStats（各角色用户数）、dailyStats（今日活跃用户/请求量）、alertList（系统告警列表）。

### 9.3.2 用户列表

- **接口名称**：获取系统用户列表
- **Method + URL**：GET /api/v1/admin/users
- **权限**：ADMIN
- **分页**：是
- **过滤**：支持keyword、role、status、college

**Response**：list每项包含userId、username、realName、role、roleName、email、status（ENABLED/DISABLED/LOCKED）、lastLoginAt、createdAt。

### 9.3.3 创建用户

- **接口名称**：创建新用户
- **Method + URL**：POST /api/v1/admin/users
- **权限**：ADMIN
- **Request Body**：username、password、realName、role、email（必填）；phone、studentNo/teacherNo、classId、college（选填）

**Mock策略**：username已存在返回code=1002。

### 9.3.4 修改用户 & 9.3.5 删除用户

**修改用户**：PUT /api/v1/admin/users/{userId}，可修改realName、email、phone、role、status等字段。不可修改username。
**删除用户**：DELETE /api/v1/admin/users/{userId}，有关联数据的用户（已有提交记录等）禁止删除，建议禁用。

### 9.3.6 角色列表

- **接口名称**：获取系统角色列表
- **Method + URL**：GET /api/v1/admin/roles
- **权限**：ADMIN

**Response**：list每项包含roleId、roleCode、roleName、description、permissionCount、userCount、status、createdAt。

### 9.3.7 创建角色

- **接口名称**：创建自定义角色
- **Method + URL**：POST /api/v1/admin/roles
- **权限**：ADMIN
- **Request Body**：roleName、roleCode、description、permissionIds（权限ID列表）

### 9.3.8 权限列表

- **接口名称**：获取系统全部权限点（树形结构）
- **Method + URL**：GET /api/v1/admin/permissions
- **权限**：ADMIN

**Response**：树形结构，顶级为模块（如task、submission、evaluation），子级为具体操作（read、create、update、delete）。

### 9.3.9 菜单列表

- **接口名称**：获取系统菜单树（管理员可编辑）
- **Method + URL**：GET /api/v1/admin/menus
- **权限**：ADMIN

**Response**：完整菜单树（含所有角色的菜单），支持CRUD操作（对应POST/PUT/DELETE子接口）。

### 9.3.10 日志查询

- **接口名称**：查询系统操作日志
- **Method + URL**：GET /api/v1/admin/logs
- **权限**：ADMIN
- **分页**：是
- **过滤**：支持userId、operationType、startDate、endDate、keyword

**Response**：list每项包含logId、userId、username、operationType（LOGIN/LOGOUT/CREATE/UPDATE/DELETE/EXPORT）、targetType、targetId、detail、ip、userAgent、createdAt。

### 9.3.11 系统监控

- **接口名称**：获取系统实时监控数据
- **Method + URL**：GET /api/v1/admin/monitor
- **权限**：ADMIN

**Response**：包含cpu（使用率/核心数）、memory（已用/总量/使用率）、disk（已用/总量/使用率）、jvm（堆内存/线程数/GC次数）、uptime（运行时长）、activeSessions（活跃会话数）。

### 9.3.12 Token监控

- **接口名称**：查看AI API Token使用情况
- **Method + URL**：GET /api/v1/admin/token-monitor
- **权限**：ADMIN

**Response**：包含todayUsed、monthUsed、totalLimit、usageTrend（近30天每日用量）、modelBreakdown（各模型用量分布）。

### 9.3.13 备份管理

- **接口名称**：触发系统数据备份
- **Method + URL**：POST /api/v1/admin/backup
- **权限**：ADMIN
- **Request Body**：backupType（FULL/INCREMENTAL）、description（选填）

**Response**：backupId、status、startedAt、estimatedTime。

### 9.3.14 模型配置

- **接口名称**：配置AI模型参数
- **Method + URL**：GET/PUT /api/v1/admin/model-config
- **权限**：ADMIN

**GET Response**：包含models（模型列表，含modelId、modelName、provider、isActive、maxTokens、temperature、status）、defaultModel、rateLimit。
**PUT Request**：可激活/停用模型、切换默认模型、调整参数。切换模型时返回成功，并记录切换历史。

**Mock策略（管理员全部接口）**：用户列表随机生成20-50条。系统监控使用固定Mock数据。日志查询随机生成分页数据。所有操作接口校验参数完整性。


---

# 第十章 AI接口

## 10.1 章节概述

本章定义AI分析模块的所有接口。AI模块是系统区别于传统实训管理平台的核心能力，采用独立的Python服务（FastAPI）作为AI引擎，通过Spring Boot后端中转调用。

### 为什么AI必须作为独立模块

1. **技术异构性**：AI/LLM调用使用Python生态（LangChain、OpenAI SDK），与Java后端技术栈完全不同。独立微服务允许各自使用最优技术栈。
2. **独立扩缩容**：AI分析是CPU/GPU密集型任务，与常规CRUD接口的资源需求不同。独立部署可根据AI调用量单独扩缩容。
3. **故障隔离**：AI服务故障不应影响用户登录、任务查看等基本功能。独立部署确保AI异常时系统核心功能可用。
4. **迭代独立性**：Prompt模板、模型切换、分析策略的调整频繁，独立模块支持热更新，无需重启Java后端。
5. **成本核算**：独立模块便于精确追踪AI API调用的Token消耗和成本。

### AI异步处理模型

```
用户触发 -> 创建分析任务(PENDING) -> 进入队列 -> 开始处理(PROCESSING) -> 完成(COMPLETED) -> 教师复核(REVIEWED)
                                                                     -> 失败(FAILED) -> 重试或放弃
                                                                     -> 用户取消(CANCELLED)
```

前端通过轮询（GET /ai-result）或SSE（GET /stream）获取实时进度。

## 10.2 接口清单

| 序号 | 接口名称 | Method | URL | 权限 |
|------|---------|--------|-----|------|
| 10.1 | 发起AI分析 | POST | /api/v1/ai/analyze | TEACHER |
| 10.2 | AI分析状态查询 | GET | /api/v1/ai/analyze/{analyzeId}/status | Authenticated |
| 10.3 | AI分析结果 | GET | /api/v1/ai/analyze/{analyzeId}/result | Authenticated |
| 10.4 | AI评分（批量） | POST | /api/v1/ai/score | TEACHER |
| 10.5 | AI建议列表 | GET | /api/v1/ai/suggestions | TEACHER |
| 10.6 | AI分析历史 | GET | /api/v1/ai/history | TEACHER |
| 10.7 | Prompt模板CRUD | * | /api/v1/ai/prompts | TEACHER |
| 10.8 | Prompt测试 | POST | /api/v1/ai/prompts/{promptId}/test | TEACHER |
| 10.9 | 模型列表 | GET | /api/v1/ai/models | ADMIN |
| 10.10 | 模型状态 | GET | /api/v1/ai/models/{modelId}/status | ADMIN |
| 10.11 | 模型切换 | PUT | /api/v1/ai/models/active | ADMIN |
| 10.12 | Token统计 | GET | /api/v1/ai/token-stats | ADMIN/TEACHER |
| 10.13 | SSE分析进度 | GET | /api/v1/ai/analyze/{analyzeId}/stream | Authenticated |

## 10.3 接口详情

### 10.3.1 发起AI分析

- **接口名称**：发起AI分析任务
- **Method + URL**：POST /api/v1/ai/analyze
- **权限**：TEACHER
- **Content-Type**：application/json

**Request Body**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetType | String | 是 | SUBMISSION（分析提交成果）/ REPORT（分析实训报告） |
| targetId | String | 是 | 目标ID（submissionId或reportId） |
| promptTemplateId | String | 否 | Prompt模板ID，不传使用默认 |
| modelId | String | 否 | 模型ID，不传使用当前激活模型 |

**Response（成功）**：
```json
{
  "code": 0, "message": "AI分析任务已创建",
  "data": {"analyzeId": "6789012345678901234", "status": "PENDING", "estimatedSeconds": 30, "createdAt": "2026-06-30T10:01:00.000+08:00"},
  "success": true
}
```

### 10.3.2 AI分析状态查询

- **接口名称**：查询AI分析任务的当前状态
- **Method + URL**：GET /api/v1/ai/analyze/{analyzeId}/status
- **权限**：Authenticated

**Response**：
```json
{
  "code": 0, "message": "success",
  "data": {"analyzeId": "6789012345678901234", "status": "PROCESSING", "progress": 60, "currentStep": "正在分析功能完成度...", "startedAt": "2026-06-30T10:01:05.000+08:00"},
  "success": true
}
```

### 10.3.3 AI分析结果

- **接口名称**：获取AI分析的完整结果
- **Method + URL**：GET /api/v1/ai/analyze/{analyzeId}/result
- **权限**：Authenticated

**Response**：与6.3.8的result结构相同。status=COMPLETED时返回完整分析结果。status=FAILED时返回失败原因和errorCode。

### 10.3.4 AI评分（批量）

- **接口名称**：批量触发AI评分（教师对多个提交发起AI初评）
- **Method + URL**：POST /api/v1/ai/score
- **权限**：TEACHER
- **Request Body**：
```json
{
  "submissionIds": ["4567890123456789012", "4567890123456789013"],
  "promptTemplateId": "5555555555555555555"
}
```

**Response**：
```json
{
  "code": 0, "message": "批量评分任务已创建",
  "data": {"batchId": "7777777777777777777", "taskCount": 2, "createdAt": "2026-07-01T10:00:00.000+08:00"},
  "success": true
}
```

### 10.3.5 AI建议列表

- **接口名称**：获取AI基于学情数据生成的教学改进建议
- **Method + URL**：GET /api/v1/ai/suggestions
- **权限**：TEACHER
- **Query参数**：courseId（可选）

**Response**：list包含suggestionId、type（TEACHING/STUDENT/CURRICULUM）、content、priority（HIGH/MEDIUM/LOW）、generatedAt。

### 10.3.6 AI分析历史

- **接口名称**：查询AI分析历史记录
- **Method + URL**：GET /api/v1/ai/history
- **权限**：TEACHER
- **分页**：是
- **过滤**：支持status、targetType、startDate、endDate

**Response**：list每项包含analyzeId、targetType、targetName、status、overallScore、modelName、elapsed、createdAt。

### 10.3.7 Prompt模板CRUD

参考8.3.8和8.3.9。AI接口章节提供独立的Prompt管理入口，与教师扩展接口共享数据。

### 10.3.8 Prompt测试

- **接口名称**：测试Prompt模板效果
- **Method + URL**：POST /api/v1/ai/prompts/{promptId}/test
- **权限**：TEACHER
- **Request Body**：
```json
{
  "testContent": "public class HelloWorld { public static void main(String[] args) { System.out.println("Hello"); } }",
  "modelId": "6666666666666666666",
  "variables": {"codeContent": "public class HelloWorld {...}"}
}
```

**Response**：返回测试结果（AI原始输出+解析后结果+耗时+Token消耗）。

### 10.3.9 模型列表

- **接口名称**：获取可用AI模型列表
- **Method + URL**：GET /api/v1/ai/models
- **权限**：ADMIN

**Response**：list包含modelId、modelName、provider（DEEPSEEK/OPENAI）、isActive、maxTokens、defaultTemperature、supportedFeatures。

### 10.3.10 模型状态 & 10.3.11 模型切换

**模型状态**：GET /api/v1/ai/models/{modelId}/status，返回availability、latency、errorRate、dailyUsage。
**模型切换**：PUT /api/v1/ai/models/active，RequestBody: {"modelId": "xxx"}。切换后所有新AI请求使用新模型。

### 10.3.12 Token统计

- **接口名称**：获取AI API Token使用统计
- **Method + URL**：GET /api/v1/ai/token-stats
- **权限**：ADMIN / TEACHER
- **Query参数**：period（DAILY/WEEKLY/MONTHLY）、startDate、endDate

**Response**：totalUsed、totalLimit、usageByModel、usageByDay、costEstimate。

### 10.3.13 SSE分析进度

- **接口名称**：通过SSE实时获取分析进度
- **Method + URL**：GET /api/v1/ai/analyze/{analyzeId}/stream
- **权限**：Authenticated
- **Content-Type**：text/event-stream

**SSE事件格式**：
```
event: progress
data: {"analyzeId":"6789012345678901234","status":"PROCESSING","progress":45,"currentStep":"正在分析代码规范..."}

event: complete
data: {"analyzeId":"6789012345678901234","status":"COMPLETED","overallScore":82}

event: error
data: {"analyzeId":"6789012345678901234","status":"FAILED","errorCode":6004,"errorMessage":"AI返回格式异常"}
```

**Mock策略**：AI接口使用SSE+轮询混合模式。Mock模式下SSE以固定间隔发送进度事件，15秒后发complete事件。可通过URL参数控制Mock行为（如?mockStatus=FAILED模拟失败）。

### AI分析状态枚举

| 状态值 | 说明 | 前端展示 |
|--------|------|---------|
| PENDING | 排队中 | Loading + 预计等待时间 |
| PROCESSING | 分析中 | 进度条 + 当前分析维度 |
| COMPLETED | 分析完成 | 展示结果 |
| FAILED | 分析失败 | 错误提示 + 重试按钮 |
| CANCELLED | 已取消 | 提示已取消 |
| REVIEWED | 人工已复核 | 展示最终评分 |

---

---

# 第十一章 文件上传接口

## 11.1 章节概述

本章定义文件上传、下载、预览、删除的通用接口。文件存储使用MinIO对象存储，前端通过后端中转上传下载。

## 11.2 接口清单

| 序号 | 接口名称 | Method | URL |
|------|---------|--------|-----|
| 11.1 | 上传文件 | POST | /api/v1/files/upload |
| 11.2 | 上传头像 | POST | /api/v1/files/avatar |
| 11.3 | 批量上传 | POST | /api/v1/files/batch-upload |
| 11.4 | 下载文件 | GET | /api/v1/files/{fileId}/download |
| 11.5 | 预览文件 | GET | /api/v1/files/{fileId}/preview |
| 11.6 | 删除文件 | DELETE | /api/v1/files/{fileId} |

## 11.3 规范说明

### 文件限制

| 文件类型 | 最大大小 | 支持格式 |
|---------|---------|---------|
| 普通文件 | 50MB | PDF、DOCX、TXT、MD |
| ZIP压缩包 | 200MB | ZIP |
| 图片 | 10MB | PNG、JPG、JPEG、GIF |
| 头像 | 5MB | PNG、JPG、JPEG |

### 文件命名规范

格式：{userId}_{timestamp}_{uuid}.{ext}
示例：1234567890123456789_20260630143000_a1b2c3d4.pdf

### 11.3.1 上传文件

- **接口名称**：上传通用文件
- **Method + URL**：POST /api/v1/files/upload
- **权限**：Authenticated
- **Content-Type**：multipart/form-data
- **Request**：file（File，必填）、category（String，选填：SUBMISSION/REPORT/AVATAR/ATTACHMENT）

**Response（成功）**：
```json
{
  "code": 0, "message": "上传成功",
  "data": {"fileId": "3456789012345678901", "fileName": "实训报告.pdf", "fileSize": 512000, "fileType": "application/pdf", "fileUrl": "/api/v1/files/3456789012345678901/download", "uploadedAt": "2026-06-30T14:30:00.000+08:00"},
  "success": true
}
```

### 11.3.2 上传头像

与11.3.1相同接口逻辑，file自动标记为AVATAR类型，上传成功后自动更新用户头像URL。

### 11.3.3 批量上传

**Request**：files（File[]，必填，最多10个文件）。
**Response**：data为fileId数组和成功/失败统计。

### 11.3.4 下载文件

- **接口名称**：下载文件
- **Method + URL**：GET /api/v1/files/{fileId}/download
- **权限**：Authenticated

**Response**：二进制流，Content-Disposition: attachment; filename="原文件名"。Content-Type根据文件类型设置。

### 11.3.5 预览文件

- **接口名称**：在线预览文件（PDF、图片）
- **Method + URL**：GET /api/v1/files/{fileId}/preview
- **权限**：Authenticated

**Response**：二进制流，Content-Type为原始文件MIME类型（inline模式）。不支持预览的类型返回code=7002。

### 11.3.6 删除文件

- **接口名称**：删除文件
- **Method + URL**：DELETE /api/v1/files/{fileId}
- **权限**：Authenticated（仅文件上传者可删除）

**Response**：成功返回code=0。文件不存在返回code=7004。

**Mock策略（文件接口）**：上传接口始终返回成功（fileId随机生成）。下载接口返回预置示例文件。预览对PDF返回预置示例PDF。

---

---

# 第十二章 Git接口

## 12.1 章节概述

本章定义Git仓库操作相关接口。系统通过Git接口验证学生提交的仓库地址有效性、获取仓库基本信息、提交历史，为AI代码分析和教师评分提供数据基础。

### 为什么需要Git接口

1. **仓库验证**：在学生提交时验证Git URL的可达性和仓库有效性，避免无效提交。
2. **代码获取**：AI分析需要获取仓库代码，Git接口提供Clone和代码提取能力。
3. **提交历史分析**：通过Git提交历史分析学生的开发过程（提交频率、提交粒度、分支策略），辅助AI和教师评估学生的工程实践能力。
4. **Webhook预留**：为未来CI/CD集成和自动触发分析预留。

## 12.2 接口清单

| 序号 | 接口名称 | Method | URL |
|------|---------|--------|-----|
| 12.1 | 仓库验证 | POST | /api/v1/git/verify |
| 12.2 | 克隆仓库 | POST | /api/v1/git/clone |
| 12.3 | 获取分支列表 | GET | /api/v1/git/{repoId}/branches |
| 12.4 | 获取提交历史 | GET | /api/v1/git/{repoId}/commits |
| 12.5 | 获取提交详情 | GET | /api/v1/git/{repoId}/commits/{commitId} |
| 12.6 | Webhook回调 | POST | /api/v1/git/webhook |

## 12.3 接口详情

### 12.3.1 仓库验证

- **接口名称**：验证Git仓库地址有效性和可访问性
- **Method + URL**：POST /api/v1/git/verify
- **权限**：Authenticated

**Request Body**：gitUrl（必填）、gitBranch（选填，默认main）、accessToken（选填，私有仓库令牌）

**Response（成功）**：参考6.3.6。

### 12.3.2 克隆仓库

- **接口名称**：克隆Git仓库到服务器临时目录
- **Method + URL**：POST /api/v1/git/clone
- **权限**：Authenticated

**Request Body**：gitUrl（必填）、gitBranch（选填）、accessToken（选填）

**Response（成功）**：
```json
{
  "code": 0, "message": "仓库克隆成功",
  "data": {"repoId": "5555555555555555555", "repoName": "book-manager", "clonePath": "/tmp/repos/5555555555555555555", "totalFiles": 45, "totalLines": 3200, "language": "Java"},
  "success": true
}
```

### 12.3.3 获取分支列表

- **接口名称**：获取仓库分支列表
- **Method + URL**：GET /api/v1/git/{repoId}/branches
- **权限**：Authenticated

**Response**：list包含branchName、isDefault、latestCommitId、latestCommitMessage、updatedAt。

### 12.3.4 获取提交历史

- **接口名称**：获取仓库提交历史
- **Method + URL**：GET /api/v1/git/{repoId}/commits
- **权限**：Authenticated
- **Query参数**：branch（分支）、limit（返回数量，默认20）、since（起始日期）

**Response**：list每项包含commitId、shortId、message、author、authorEmail、committedAt、filesChanged、insertions、deletions。

### 12.3.5 获取提交详情

- **接口名称**：获取单个提交的详细信息
- **Method + URL**：GET /api/v1/git/{repoId}/commits/{commitId}
- **权限**：Authenticated

**Response**：包含完整diff（变更文件列表、每个文件的增删行数、具体diff内容）。

### 12.3.6 Webhook回调（预留）

- **接口名称**：接收Git平台的Webhook推送
- **Method + URL**：POST /api/v1/git/webhook
- **权限**：Public（需验证签名）

**用途**：未来对接GitHub/GitLab/Gitee的Webhook，实现仓库更新自动触发重新分析。

**Mock策略（Git接口）**：验证接口模拟验证URL有效性（含"valid"返回成功）。其他接口使用固定Mock数据模拟一个典型的学生实训仓库结构（约20次提交，含commit message、author、时间线）。


---

# 第十三章 分页规范

## 13.1 章节概述

本章定义所有列表接口统一的分页参数、排序参数、过滤参数和分页响应格式。所有返回列表数据的接口必须严格遵守本章规范。

## 13.2 统一分页参数（Request）

| 参数 | 类型 | 默认值 | 允许值 | 说明 |
|------|------|--------|--------|------|
| page | Integer | 1 | >=1 | 页码，从1开始 |
| pageSize | Integer | 20 | 10/20/50/100 | 每页记录数，超过100按100处理 |
| sortField | String | createdAt | camelCase字段名 | 排序字段 |
| sortOrder | String | desc | asc/desc | 排序方向 |

## 13.3 统一过滤参数（Request）

| 参数 | 类型 | 说明 |
|------|------|------|
| keyword | String | 全局搜索关键词，对名称、描述等Text字段做LIKE查询 |
| status | String | 按状态筛选（各接口具体状态枚举不同） |
| startDate | String | 按创建/提交时间筛选-起始（ISO 8601日期） |
| endDate | String | 按创建/提交时间筛选-结束（ISO 8601日期） |
| 业务字段 | Any | 按具体业务字段精确匹配筛选，如courseName、classId、role |

每个接口在定义中必须明确标注支持哪些过滤参数及其可选值。

## 13.4 统一分页响应（Response）

```json
{
  "list": [],
  "page": 1,
  "pageSize": 20,
  "total": 150,
  "totalPages": 8
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| list | Array | 当前页数据列表 |
| page | Integer | 当前页码 |
| pageSize | Integer | 每页记录数 |
| total | Long | 总记录数 |
| totalPages | Integer | 总页数 = Math.ceil(total / pageSize) |

## 13.5 前端请求构建规范（Axios）

前端所有列表查询统一使用以下参数构建方式：

```typescript
// 接口层
const params = {
  page: 1,
  pageSize: 20,
  sortField: 'createdAt',
  sortOrder: 'desc',
  keyword: '',
  status: '',
  startDate: '',
  endDate: ''
};
```

Axios自动将对象序列化为URL查询参数。

## 13.6 后端接收规范（Spring Boot）

后端Controller统一使用以下方式接收分页参数：

```java
// Controller方法参数
@RequestParam(defaultValue = "1") Integer page,
@RequestParam(defaultValue = "20") Integer pageSize,
@RequestParam(defaultValue = "createdAt") String sortField,
@RequestParam(defaultValue = "desc") String sortOrder
```

统一封装为PageQuery对象。Service层统一使用MyBatis-Plus分页插件。

## 13.7 Mock分页策略

Mock.js根据page和pageSize参数生成不同页的数据：

- 第1页：返回前pageSize条记录
- 第N页：返回(page-1)*pageSize到page*pageSize范围的记录
- 请求超出total范围时返回空list
- 总记录数total在Mock初始化时随机生成（50-200之间）

---

---

# 第十四章 Mock数据规范

## 14.1 章节概述

本章定义前端Mock数据的生成策略、组织方式和管理规范。Mock是前端独立开发的基础，必须覆盖正常数据、异常数据、边界数据和各种业务状态。

## 14.2 Mock数据类型

| 类型 | 说明 | 适用场景 | 示例 |
|------|------|---------|------|
| 随机数据 | 使用Mock.js随机生成 | 列表数据（姓名、邮箱、日期、分数） | `@cname`、`@email`、`@datetime` |
| 固定数据 | 预设的特定数据 | 登录账号、菜单、权限 | admin/admin123 |
| 分页数据 | 按page+pageSize动态生成 | 所有列表接口 | 每页返回不同数据 |
| 异常数据 | 模拟接口异常 | 超时、500错误、网络断开 | 延迟5秒后返回500 |
| 空数据 | 返回空list | 空状态UI测试 | `{list: [], total: 0}` |
| 权限数据 | 不同角色返回不同内容 | 菜单、按钮、数据范围 | 学生看不到管理菜单 |
| AI数据 | 模拟AI返回的评分和评语 | AI分析接口 | 固定5维度评分+评语 |
| 图表数据 | 模拟ECharts所需格式 | 统计图表 | 雷达图、趋势图、饼图 |

## 14.3 Mock数据组织原则

1. **模块化存放**：每个业务模块的Mock数据独立一个文件（如`src/mock/student.js`、`src/mock/teacher.js`）
2. **固定数据与随机数据分离**：固定Mock数据放在独立的`src/mock/fixed/`目录下
3. **延迟模拟**：所有Mock接口默认200-800ms随机延迟，模拟真实网络请求
4. **开关控制**：全局Mock开关（环境变量`VITE_USE_MOCK`）+ 单接口Mock开关
5. **数据结构与真实接口一致**：Mock返回的JSON结构必须与本文档定义的接口响应结构完全一致
6. **状态覆盖**：每个需要展示多种状态的接口，Mock数据必须覆盖所有可能状态

## 14.4 Mock开关机制

| 级别 | 控制方式 | 说明 |
|------|---------|------|
| 全局开关 | 环境变量 VITE_USE_MOCK=true/false | 开发环境启用，生产环境关闭 |
| 模块开关 | env配置 VITE_MOCK_AUTH=true 等 | 按模块单独开关 |
| 接口开关 | URL参数 ?mock=fail 或 ?mock=empty | 特定接口模拟异常场景 |

## 14.5 Mock异常场景覆盖

每个接口必须Mock以下异常场景（至少3种）：

| 场景 | 实现方式 | 用途 |
|------|---------|------|
| 网络超时 | 延迟>10秒后返回 | 测试超时处理和重试逻辑 |
| 服务端错误 | 返回500+code=5001 | 测试错误页面和Toast提示 |
| 认证失败 | 返回401+code=2002 | 测试Token过期自动刷新 |
| 权限不足 | 返回403+code=3001 | 测试权限拦截和无权限页面 |
| 参数校验失败 | 返回400+code=4001+errors | 测试表单校验错误展示 |
| AI分析失败 | 返回code=6002/6003/6004 | 测试AI异常状态的UI处理 |

---

---

# 第十五章 AI Mock规范

## 15.1 章节概述

本章是Mock规范的重点章节。AI分析结果具有高度不可预测性——模型可能返回格式异常的JSON、超时、Token耗尽、输出幻觉内容等。前端必须Mock所有可能的AI状态，确保UI在所有场景下都能正确处理和展示。

## 15.2 为什么AI Mock需要单独规范

1. **结果不可预测**：LLM的输出格式不确定，即使Prompt设计完美，也可能输出不符合JSON Schema的内容
2. **多状态流转**：AI分析有6种状态（PENDING/PROCESSING/COMPLETED/FAILED/CANCELLED/REVIEWED），每种状态前端展示不同
3. **异步特性**：分析不是即时完成的，涉及轮询和SSE推送
4. **失败场景多样**：超时、Token不足、模型宕机、JSON解析失败、Prompt模板错误等
5. **人工复核流程**：AI完成后教师可修改评分，前端需处理"AI评分"和"教师最终评分"两种数据

## 15.3 必须Mock的AI状态（14种）

| 序号 | Mock状态 | 触发方式 | 返回内容 | 前端处理 |
|------|----------|---------|---------|---------|
| 1 | 分析排队中 | 正常请求 | status=PENDING, estimatedSeconds=30 | 展示Loading+预计等待 |
| 2 | 分析进行中(30%) | 轮询第1次 | status=PROCESSING, progress=30 | 展示进度条30% |
| 3 | 分析进行中(60%) | 轮询第2次 | status=PROCESSING, progress=60 | 展示进度条60% |
| 4 | 分析进行中(90%) | 轮询第3次 | status=PROCESSING, progress=90 | 展示进度条90% |
| 5 | 分析成功-完整结果 | 轮询第4次 | status=COMPLETED, 完整评分JSON | 展示5维度评分+雷达图 |
| 6 | 分析成功-仅评分 | 特殊Mock模式 | COMPLETED但无详细评语 | 仅展示分数，无评语区 |
| 7 | 分析失败-模型错误 | LLM返回500 | status=FAILED, errorCode=6002 | Toast提示+重试按钮 |
| 8 | 分析失败-超时 | 60秒无响应 | status=FAILED, errorCode=6003 | 提示超时+重试 |
| 9 | 分析失败-JSON异常 | 返回非JSON | status=FAILED, errorCode=6004 | 提示解析失败+重试 |
| 10 | 分析失败-Token不足 | 额度耗尽 | status=FAILED, errorCode=6005 | 提示管理员处理 |
| 11 | 分析失败-Prompt异常 | 模板错误 | status=FAILED, errorCode=6006 | 提示教师检查 |
| 12 | 人工复核前 | AI完成，教师未评分 | aiScore有值, teacherScore=null | 展示"待教师评分"标签 |
| 13 | 人工复核后 | 教师评分完成 | aiScore和teacherScore均有值 | 展示对比和最终分数 |
| 14 | 取消分析 | 用户取消 | status=CANCELLED | 提示已取消 |
| 15 | SSE流式推送 | 建立SSE连接 | 逐条推送progress事件 | 实时更新进度条和文字 |

## 15.4 AI返回的完整JSON结构

```json
{
  "overallScore": 82,
  "dimensions": [{
    "dimensionName": "代码规范",
    "score": 20,
    "maxScore": 25,
    "weight": 25,
    "comment": "AI对该维度的评语...",
    "suggestions": ["建议1", "建议2"],
    "codeReferences": ["文件:行号 - 说明"]
  }],
  "summary": "总体评价...",
  "strengths": ["优点1", "优点2"],
  "weaknesses": ["不足1", "不足2"],
  "improvementPlan": "改进计划..."
}
```

## 15.5 AI Mock实现策略

### 轮询模式模拟

Mock通过在内存中维护分析任务状态，模拟异步过程：
- 首次请求（ai-evaluate）：创建任务，返回PENDING，内部计时器启动
- 第1次轮询（0-5秒）：返回PROCESSING, progress递增
- 第N次轮询（15秒后）：返回COMPLETED + 完整结果
- 超时模拟（60秒后）：返回FAILED + code=6003

### SSE模式模拟

Mock拦截EventSource请求，以固定时间间隔（1秒）推送progress事件：
```
0s: {status: "PENDING", progress: 0}
1s: {status: "PROCESSING", progress: 10, currentStep: "正在解析代码..."}
3s: {status: "PROCESSING", progress: 40, currentStep: "正在分析代码规范..."}
...
15s: {status: "COMPLETED", overallScore: 82}
```

### 异常模式切换

通过URL参数控制AI Mock行为：
- `?mockAi=fail_model`：模拟模型调用失败
- `?mockAi=fail_timeout`：模拟超时
- `?mockAi=fail_json`：模拟JSON解析失败
- `?mockAi=fail_token`：模拟Token不足
- `?mockAi=fail_prompt`：模拟Prompt异常
- `?mockAi=stream`：启用SSE模式

---

---

# 第十六章 图表Mock规范

## 16.1 章节概述

本章定义系统中所有统计图表的Mock数据结构。图表数据必须遵循ECharts（前端图表库）的数据格式要求，确保Mock数据可以直接绑定到ECharts图表组件展示。

## 16.2 支持的图表类型及数据结构

### 16.2.1 雷达图（Radar Chart）

**使用场景**：能力画像、多维度评分对比。

**数据结构**：
```json
{
  "radar": {
    "indicator": [
      {"name": "代码规范", "max": 100},
      {"name": "功能完成度", "max": 100},
      {"name": "创新设计", "max": 100},
      {"name": "文档撰写", "max": 100},
      {"name": "团队协作", "max": 100},
      {"name": "工程素养", "max": 100}
    ],
    "series": [
      {"name": "张三", "value": [78, 85, 72, 80, 75, 82]},
      {"name": "班级平均", "value": [72, 78, 68, 74, 70, 75]}
    ]
  }
}
```

### 16.2.2 折线图（Line Chart）

**使用场景**：成绩趋势、提交趋势、Token用量趋势。

**数据结构**：
```json
{
  "lineChart": {
    "xAxis": ["2026-03", "2026-04", "2026-05", "2026-06"],
    "series": [
      {"name": "张三", "type": "line", "data": [78, 82, 85, 80]},
      {"name": "班级平均", "type": "line", "data": [72, 74, 76, 75]}
    ]
  }
}
```

### 16.2.3 柱状图（Bar Chart）

**使用场景**：班级对比、课程对比、成绩分布。

**数据结构**：
```json
{
  "barChart": {
    "xAxis": ["软件2401", "软件2402", "软件2403"],
    "series": [
      {"name": "平均分", "type": "bar", "data": [82.3, 76.8, 79.1]},
      {"name": "提交率", "type": "bar", "data": [0.92, 0.85, 0.88]}
    ]
  }
}
```

### 16.2.4 饼图（Pie Chart）

**使用场景**：成绩分布、提交状态分布、Token使用分布。

**数据结构**：
```json
{
  "pieChart": {
    "data": [
      {"name": "优秀(90-100)", "value": 14},
      {"name": "良好(80-89)", "value": 16},
      {"name": "及格(60-79)", "value": 8},
      {"name": "不及格(0-59)", "value": 2}
    ]
  }
}
```

### 16.2.5 词云（Word Cloud）

**使用场景**：评语关键词、高频错误类型。

**数据结构**：
```json
{
  "wordCloud": {
    "data": [
      {"name": "代码规范", "value": 85},
      {"name": "注释缺失", "value": 72},
      {"name": "异常处理", "value": 65},
      {"name": "命名规范", "value": 58},
      {"name": "单元测试", "value": 50},
      {"name": "性能优化", "value": 42}
    ]
  }
}
```

### 16.2.6 热力图（Heatmap）

**使用场景**：提交时间分布（按星期几和小时段）。

**数据结构**：
```json
{
  "heatmap": {
    "xAxis": ["周一","周二","周三","周四","周五","周六","周日"],
    "yAxis": ["0-4时","4-8时","8-12时","12-16时","16-20时","20-24时"],
    "data": [[0,2,5],[1,2,12],[2,3,25],[3,3,18],[4,2,8],[5,0,3],[6,0,1]]
  }
}
```

### 16.2.7 仪表盘（Gauge）

**使用场景**：提交完成率、批改完成率、AI成功率。

**数据结构**：
```json
{
  "gauge": {
    "value": 85.5,
    "min": 0,
    "max": 100,
    "title": "提交完成率",
    "unit": "%"
  }
}
```

## 16.3 Mock策略

- 图表数据使用固定模板+随机扰动（±5%），确保数据看起来真实自然
- 雷达图的班级平均数据与个人数据保持合理的对比关系（个人高于平均10-15%）
- 趋势数据确保单调性（成绩总体上升趋势）
- 饼图数据总和等于总数

---

---

# 第十七章 权限Mock规范

## 17.1 章节概述

本章定义三种角色（学生/教师/管理员）的完整权限体系Mock数据，包括菜单树、权限点列表、按钮权限映射和路由权限映射。

## 17.2 三角色权限体系总览

| 角色 | 编码 | 一级菜单 | 二级菜单 | 权限点约数 |
|------|------|---------|---------|-----------|
| 学生 | STUDENT | 4个 | 2个 | 15个 |
| 教师 | TEACHER | 8个 | 10个 | 40个 |
| 管理员 | ADMIN | 3个 | 3个 | 15个 |

## 17.3 权限点命名规范

格式：`{模块}:{操作}`

| 模块 | 操作 | 示例 |
|------|------|------|
| dashboard | view | dashboard:view |
| task | read/create/update/delete | task:read |
| submission | create/read/delete | submission:create |
| evaluation | read/score/publish/reject | evaluation:score |
| report | upload/read/export | report:export |
| course | read/create/update/delete | course:create |
| score-template | read/create/update/delete | score-template:create |
| prompt-template | read/create/update/delete/test | prompt-template:test |
| statistics | view | statistics:view |
| user | read/create/update/delete | user:create |
| role | read/create/update/delete | role:read |
| menu | read/create/update/delete | menu:read |
| log | read | log:read |
| monitor | view | monitor:view |
| model | read/config | model:config |
| backup | create/restore | backup:create |
| file | upload/download/delete | file:upload |
| git | verify/clone | git:verify |
| notification | read/update | notification:read |
| growth | view | growth:view |
| ai | analyze/score | ai:analyze |

## 17.4 各角色完整菜单树

### 17.4.1 学生菜单树

```
├── 首页 (dashboard)
├── 我的任务 (tasks)
│   └── 任务详情 (task-detail, 隐藏菜单)
├── 成长档案 (growth)
└── 消息通知 (notifications)
```

### 17.4.2 教师菜单树

```
├── 首页 (teacher-dashboard)
├── 班级管理 (classes)
│   ├── 班级详情 (class-detail, 隐藏菜单)
│   └── 学生列表 (students, 隐藏菜单)
├── 任务管理 (tasks)
│   ├── 创建任务 (task-create)
│   ├── 任务详情 (task-detail, 隐藏菜单)
│   └── 提交记录 (submissions, 隐藏菜单)
├── 成绩管理 (scores)
│   └── 评分详情 (score-detail, 隐藏菜单)
├── 课程管理 (courses)
├── 评分模板 (score-templates)
├── Prompt管理 (prompts)
│   └── 版本历史 (prompt-versions, 隐藏菜单)
├── 统计分析 (statistics)
├── 报表导出 (reports)
└── 消息通知 (notifications)
```

### 17.4.3 管理员菜单树

```
├── 首页 (admin-dashboard)
├── 用户管理 (users)
├── 角色管理 (roles)
├── 菜单管理 (menus)
├── 系统监控 (monitor)
├── 日志查询 (logs)
└── 系统配置 (settings)
    ├── 模型配置 (model-config)
    ├── Token监控 (token-monitor)
    └── 备份管理 (backup)
```

## 17.5 按钮权限映射

| 页面 | 按钮 | 权限点 | 可见角色 |
|------|------|--------|---------|
| 任务列表 | 创建任务 | task:create | TEACHER |
| 任务列表 | 编辑任务 | task:update | TEACHER |
| 任务列表 | 删除任务 | task:delete | TEACHER |
| 提交记录 | AI评分 | ai:score | TEACHER |
| 提交记录 | 人工评分 | evaluation:score | TEACHER |
| 提交记录 | 发布成绩 | evaluation:publish | TEACHER |
| 提交记录 | 退回整改 | evaluation:reject | TEACHER |
| 课程列表 | 创建课程 | course:create | TEACHER |
| 评分模板 | 创建模板 | score-template:create | TEACHER |
| Prompt管理 | 测试Prompt | prompt-template:test | TEACHER |
| 用户列表 | 创建用户 | user:create | ADMIN |
| 系统配置 | 模型配置 | model:config | ADMIN |
| 系统配置 | 备份 | backup:create | ADMIN |

## 17.6 路由权限映射

前端Vue Router通过meta.permission字段控制路由访问权限：

```typescript
// 路由配置示例
{ path: '/teacher/tasks/create', meta: { permission: 'task:create', roles: ['TEACHER'] } }
{ path: '/teacher/courses', meta: { permission: 'course:read', roles: ['TEACHER'] } }
{ path: '/admin/users', meta: { permission: 'user:read', roles: ['ADMIN'] } }
```

路由守卫逻辑：检查用户permissions数组是否包含路由所需的permission，且role在roles列表中。

## 17.7 Mock策略

- 登录时根据角色返回预设的菜单树和权限列表
- 前端Pinia Store（useAuthStore）存储当前用户的menus和permissions
- 侧边栏菜单根据menus动态渲染
- 页面按钮根据permissions控制显示/隐藏
- 路由守卫根据permissions和role拦截越权访问


---

# 第十八章 接口命名规范

## 18.1 章节概述

本章定义所有API接口的URL命名规则、HTTP方法使用约定和版本管理策略。本章规范是RESTful设计原则（第一章）的具体化落地，供前后端开发人员在日常开发中直接查阅。

## 18.2 URL命名规则

| 规则 | 说明 | 正确示例 | 错误示例 |
|------|------|---------|---------|
| 全部小写 | URL路径全部使用小写字母 | /api/v1/student/tasks | /api/v1/Student/Tasks |
| 连字符分隔 | 多个单词使用-连接（kebab-case） | /api/v1/score-templates | /api/v1/scoreTemplates |
| 名词复数 | 集合资源使用名词复数形式 | /api/v1/students、/api/v1/tasks | /api/v1/student、/api/v1/task |
| 层级表达 | 子资源通过URL路径嵌套 | /api/v1/classes/{id}/students | /api/v1/students?classId=xxx |
| 无动词 | URL中不包含动词，操作由HTTP方法表达 | POST /api/v1/tasks | POST /api/v1/createTask |
| 无文件后缀 | URL不包含.json/.xml等后缀 | /api/v1/tasks | /api/v1/tasks.json |
| 版本前缀 | 版本号在/api/后第一段 | /api/v1/tasks | /v1/tasks 或 /api/tasks?v=1 |
| 无尾部斜杠 | URL不以/结尾 | /api/v1/tasks | /api/v1/tasks/ |
| 深度限制 | 嵌套深度不超过3层 | /api/v1/classes/{id}/students | /api/v1/courses/{id}/classes/{id}/students |

## 18.3 HTTP方法约定

| 方法 | 用途 | 幂等性 | 安全 | Request Body | Response Body |
|------|------|--------|------|-------------|---------------|
| GET | 查询资源 | 是 | 是 | 无 | 资源数据 |
| POST | 创建资源 | 否 | 否 | 资源数据 | 新创建的资源 |
| PUT | 全量更新 | 是 | 否 | 完整资源数据 | 更新后的资源 |
| PATCH | 部分更新 | 否 | 否 | 仅变更字段 | 更新后的资源 |
| DELETE | 删除资源 | 是 | 否 | 无 | 无（或删除确认） |

### 方法使用规则

1. **GET**：仅用于查询，不允许修改服务端数据。支持分页、排序、过滤参数。
2. **POST**：用于创建新资源。响应201 Created + Location头指向新资源URL。
3. **PUT**：用于全量替换资源。客户端必须提交资源的完整表示。未提交的字段会被设为默认值或null。
4. **PATCH**：用于部分更新资源。仅更新提交的字段，其他字段保持不变。状态变更优先使用PATCH（如PATCH /tasks/{id}/status）。
5. **DELETE**：用于删除资源。成功返回204 No Content。逻辑删除优于物理删除。

## 18.4 资源命名规范

### 资源类型

| 资源类型 | URL根路径 | 示例 |
|---------|----------|------|
| 认证相关 | /api/v1/auth/ | /api/v1/auth/login |
| 学生端 | /api/v1/student/ | /api/v1/student/tasks |
| 教师端 | /api/v1/teacher/ | /api/v1/teacher/classes |
| 管理端 | /api/v1/admin/ | /api/v1/admin/users |
| 通用文件 | /api/v1/files/ | /api/v1/files/upload |
| Git操作 | /api/v1/git/ | /api/v1/git/verify |
| AI服务 | /api/v1/ai/ | /api/v1/ai/analyze |

### 子资源命名

子资源通过URL路径嵌套表达层级关系：

- 班级的学生：/api/v1/teacher/classes/{classId}/students
- 任务的提交记录：/api/v1/teacher/tasks/{taskId}/submissions
- 提交的评价：/api/v1/student/submissions/{submissionId}/evaluation
- Prompt的版本：/api/v1/teacher/prompt-templates/{templateId}/versions

### 特殊操作命名

某些非标准CRUD操作使用"动词化名词"表达：

- 发布成绩：POST /api/v1/teacher/tasks/{taskId}/publish-scores
- 退回整改：POST /api/v1/teacher/submissions/{submissionId}/reject
- 催交提醒：POST /api/v1/teacher/tasks/{taskId}/remind
- 发起AI分析：POST /api/v1/ai/analyze
- 导出报表：GET /api/v1/teacher/tasks/{taskId}/export-excel

## 18.5 版本管理

### 版本格式

- 当前版本：v1
- 版本号在URL路径中：/api/v1/
- 大版本号递增：v1 -> v2 -> v3

### 版本生命周期

1. **开发阶段**：新版本API在独立路径下开发（如/api/v2/）
2. **并行运行**：v1和v2同时提供服务，至少保留1个迭代周期（2-4周）
3. **废弃通知**：v1接口返回Deprecation: true响应头和Sunset过期日期
4. **正式下线**：过渡期结束后v1接口返回410 Gone + 新接口地址

### 版本升级规则

- 以下变更需要升级大版本（v1 -> v2）：
  - 删除或重命名现有字段
  - 修改字段类型
  - 修改URL路径结构
  - 修改认证方式
- 以下变更不需要升级版本：
  - 新增接口
  - 新增可选字段
  - 新增查询参数
  - 新增响应字段

---

---

# 第十九章 接口开发顺序

## 19.1 章节概述

本章规划所有接口的开发优先级和顺序，确保MVP阶段能够快速交付核心功能，后续迭代有序推进。开发顺序基于业务依赖关系——被依赖的接口必须先开发。

## 19.2 MVP阶段（第1-2周，必须最先实现）

MVP阶段聚焦"学生提交 -> AI分析 -> 教师评分"的核心业务闭环。

### 第一优先级：认证与基础（第1-3天）

| 顺序 | 接口 | 原因 |
|------|------|------|
| 1 | POST /api/v1/auth/login | 所有接口的前置依赖，没有登录就无法测试 |
| 2 | POST /api/v1/auth/refresh | Token续期机制，防止开发中频繁重新登录 |
| 3 | GET /api/v1/auth/me | 获取当前用户信息，前端Header/侧边栏依赖 |
| 4 | GET /api/v1/auth/menus | 前端路由和侧边栏菜单渲染依赖 |
| 5 | GET /api/v1/auth/permissions | 前端权限控制（按钮显隐、路由守卫）依赖 |
| 6 | POST /api/v1/auth/logout | 基础功能，依赖度低 |

### 第二优先级：学生端核心（第4-6天）

| 顺序 | 接口 | 原因 |
|------|------|------|
| 7 | GET /api/v1/student/tasks | 学生首页核心数据 |
| 8 | GET /api/v1/student/tasks/{taskId} | 任务详情，提交入口的前提 |
| 9 | POST /api/v1/student/tasks/{taskId}/submissions | 核心业务——成果提交 |
| 10 | POST /api/v1/student/submissions/{submissionId}/ai-evaluate | 核心业务——触发AI分析 |
| 11 | GET /api/v1/student/submissions/{submissionId}/ai-result | 获取AI分析结果（含轮询） |
| 12 | GET /api/v1/student/dashboard | 学生首页概览 |

### 第三优先级：教师端核心（第7-10天）

| 顺序 | 接口 | 原因 |
|------|------|------|
| 13 | GET /api/v1/teacher/tasks | 教师任务列表 |
| 14 | GET /api/v1/teacher/dashboard | 教师首页统计 |
| 15 | GET /api/v1/teacher/tasks/{taskId}/submissions | 查看学生提交记录 |
| 16 | GET /api/v1/teacher/submissions/{submissionId}/ai-preview | 查看AI初评结果 |
| 17 | PUT /api/v1/teacher/submissions/{submissionId}/score | 人工评分（核心） |
| 18 | POST /api/v1/teacher/tasks/{taskId}/publish-scores | 发布成绩（核心） |

### 第四优先级：文件与基础支撑（第11-14天）

| 顺序 | 接口 | 原因 |
|------|------|------|
| 19 | POST /api/v1/files/upload | 文件上传（报告、ZIP提交） |
| 20 | GET /api/v1/files/{fileId}/download | 文件下载 |
| 21 | POST /api/v1/files/avatar | 头像上传 |
| 22 | POST /api/v1/student/tasks/{taskId}/reports | 实训报告上传 |
| 23 | POST /api/v1/student/tasks/{taskId}/git-verify | Git仓库验证 |

## 19.3 第二阶段（第3-4周）

### 教师端完善

| 顺序 | 接口 |
|------|------|
| 24 | GET /api/v1/teacher/classes |
| 25 | GET /api/v1/teacher/classes/{classId} |
| 26 | GET /api/v1/teacher/classes/{classId}/students |
| 27 | POST /api/v1/teacher/tasks（创建任务） |
| 28 | PUT /api/v1/teacher/tasks/{taskId}（修改任务） |
| 29 | DELETE /api/v1/teacher/tasks/{taskId}（删除任务） |
| 30 | POST /api/v1/teacher/submissions/{submissionId}/reject（退回整改） |
| 31 | GET /api/v1/teacher/classes/{classId}/report（班级报表） |
| 32 | GET /api/v1/teacher/tasks/{taskId}/export-excel（Excel导出） |
| 33 | POST /api/v1/teacher/tasks/{taskId}/remind（催交） |

### AI接口完善

| 顺序 | 接口 |
|------|------|
| 34 | GET /api/v1/ai/analyze/{analyzeId}/status |
| 35 | GET /api/v1/ai/analyze/{analyzeId}/result |
| 36 | GET /api/v1/ai/analyze/{analyzeId}/stream（SSE） |
| 37 | POST /api/v1/ai/score（批量评分） |
| 38 | GET /api/v1/ai/history |

### 消息与Git

| 顺序 | 接口 |
|------|------|
| 39 | GET /api/v1/student/notifications |
| 40 | PUT /api/v1/student/notifications/{id}/read |
| 41 | POST /api/v1/git/clone |
| 42 | GET /api/v1/git/{repoId}/commits |

## 19.4 第三阶段（第5-6周）

### 教师扩展端

| 顺序 | 接口 |
|------|------|
| 43 | GET /api/v1/teacher/dashboard |
| 44 | GET/POST /api/v1/teacher/courses |
| 45 | PUT/DELETE /api/v1/teacher/courses/{courseId} |
| 46 | GET/POST /api/v1/teacher/score-templates |
| 47 | GET/POST /api/v1/teacher/prompt-templates |
| 48 | GET /api/v1/teacher/prompt-templates/{id}/versions |
| 49 | PUT /api/v1/teacher/courses/{courseId}/weights |
| 50 | GET /api/v1/teacher/statistics |
| 51 | GET /api/v1/teacher/reports/college |
| 52 | GET /api/v1/teacher/reports/export-word |
| 53 | GET /api/v1/teacher/reports/export-pdf |

### 学生端完善

| 顺序 | 接口 |
|------|------|
| 54 | GET /api/v1/student/submissions/{id}/evaluation（含教师评分） |
| 55 | GET /api/v1/student/growth-profile |
| 56 | GET /api/v1/student/reports/export |

### 管理员端

| 顺序 | 接口 |
|------|------|
| 57 | GET /api/v1/admin/dashboard |
| 58 | GET/POST /api/v1/admin/users |
| 59 | PUT/DELETE /api/v1/admin/users/{userId} |
| 60 | GET/POST /api/v1/admin/roles |
| 61 | GET /api/v1/admin/permissions |
| 62 | GET /api/v1/admin/menus |
| 63 | GET /api/v1/admin/logs |
| 64 | GET /api/v1/admin/monitor |
| 65 | GET /api/v1/admin/token-monitor |
| 66 | POST /api/v1/admin/backup |
| 67 | GET/PUT /api/v1/admin/model-config |
| 68 | GET /api/v1/ai/models |
| 69 | PUT /api/v1/ai/models/active |
| 70 | GET /api/v1/ai/token-stats |

## 19.5 未来扩展

以下接口为预留扩展，不在MVP交付范围内：

- WebSocket实时通知（替代轮询）
- 第三方平台对接（企业微信、钉钉、飞书）消息推送
- 多语言国际化支持
- 微服务拆分后的服务间内部接口
- 数据仓库/BI系统的数据导出接口
- 在线代码编辑器WebSocket协同接口
- Code Runner（在线编译运行）接口

---

---

# 第二十章 API Checklist

## 20.1 章节概述

本章提供完整的API设计检查清单（70项），用于接口设计评审、代码审查和Mock数据验证。每一项为布尔型检查项，全部通过方可进入下一阶段。此Checklist同时适用于前后端开发人员的自检。

## 20.2 设计规范（15项）

| # | 检查项 | 说明 |
|---|--------|------|
| 1 | □ 是否符合RESTful风格 | URL为资源名词，HTTP方法表达操作 |
| 2 | □ URL是否全部小写 | 不允许任何大写字母 |
| 3 | □ 资源名是否使用复数 | /tasks 而非 /task |
| 4 | □ 是否使用kebab-case命名 | /score-templates 而非 /scoreTemplates |
| 5 | □ 是否统一使用/api/v1/前缀 | 所有接口统一版本前缀 |
| 6 | □ HTTP方法是否语义正确 | GET查询/POST创建/PUT全量更新/PATCH部分更新/DELETE删除 |
| 7 | □ URL中是否避免包含动词 | POST /tasks 而非 POST /createTask |
| 8 | □ 子资源层级关系是否合理 | /classes/{id}/students 深度≤3 |
| 9 | □ URL中是否避免文件后缀 | /tasks 而非 /tasks.json |
| 10 | □ 接口命名是否全局一致 | 同一资源在所有模块中URL一致 |
| 11 | □ 版本号是否在URL路径中 | /api/v1/ 非Header传版本 |
| 12 | □ 是否定义了废弃接口策略 | 旧版本返回410 Gone+新地址 |
| 13 | □ 是否遵循统一ID规范 | ID为String类型19位Snowflake |
| 14 | □ 时间格式是否统一ISO 8601 | yyyy-MM-ddTHH:mm:ss.SSS+08:00 |
| 15 | □ 字段命名是否统一camelCase | taskName非task_name |

## 20.3 请求规范（8项）

| # | 检查项 | 说明 |
|---|--------|------|
| 16 | □ 是否定义了Base URL | 开发/测试/生产环境Base URL明确 |
| 17 | □ Authorization Header是否规范 | Bearer {token}格式 |
| 18 | □ Content-Type是否正确设置 | JSON用application/json，文件用multipart/form-data |
| 19 | □ Accept Header是否定义 | application/json |
| 20 | □ X-Trace-Id是否支持 | 全链路追踪ID |
| 21 | □ X-Request-Id是否支持 | 请求幂等ID |
| 22 | □ Accept-Language是否支持 | zh-CN/en-US |
| 23 | □ X-Timezone是否支持 | Asia/Shanghai等 |

## 20.4 响应规范（10项）

| # | 检查项 | 说明 |
|---|--------|------|
| 24 | □ 是否使用统一响应结构 | code/message/data/success四要素 |
| 25 | □ code/message/data三要素是否完整 | 每个响应都包含 |
| 26 | □ success字段是否正确 | 成功true，失败false |
| 27 | □ timestamp是否返回 | ISO 8601格式 |
| 28 | □ traceId是否返回 | 贯穿全链路 |
| 29 | □ requestId是否返回 | 每次请求唯一 |
| 30 | □ path是否返回 | 请求路径 |
| 31 | □ 分页响应是否包含totalPages | 前端计算总页数 |
| 32 | □ 错误响应是否返回具体错误码 | 非0的code值 |
| 33 | □ 校验错误是否返回字段级错误详情 | errors数组含field/message/rejectedValue |
| 34 | □ 是否避免在message中暴露敏感信息 | 生产环境不返回堆栈/SQL |

## 20.5 错误码规范（6项）

| # | 检查项 | 说明 |
|---|--------|------|
| 35 | □ 错误码是否全局唯一 | 每个错误码唯一含义 |
| 36 | □ 错误码分类是否合理 | 0/1000/2000/3000/4000/5000/6000/7000/8000/9000 |
| 37 | □ 每个错误码是否有对应的用户提示 | 中文友好提示 |
| 38 | □ 每个错误码是否有前端处理方案 | 跳转/Toast/重试/置灰 |
| 39 | □ AI错误码是否独立分类 | 6000-6999范围 |
| 40 | □ 是否区分了认证错误和授权错误 | 2000 vs 3000 |

## 20.6 分页排序过滤（6项）

| # | 检查项 | 说明 |
|---|--------|------|
| 41 | □ 所有列表接口是否支持分页 | 无例外 |
| 42 | □ 分页参数命名是否统一 | page/pageSize |
| 43 | □ 是否支持排序 | sortField/sortOrder |
| 44 | □ 是否支持关键词搜索 | keyword参数 |
| 45 | □ 是否支持状态筛选 | status参数 |
| 46 | □ 分页响应格式是否统一 | list/page/pageSize/total/totalPages |

## 20.7 权限规范（5项）

| # | 检查项 | 说明 |
|---|--------|------|
| 47 | □ 每个接口是否标注所需权限 | 文档中明确标注 |
| 48 | □ 权限点命名是否统一 | {模块}:{操作}格式 |
| 49 | □ 菜单权限是否按角色返回 | 不同角色不同菜单树 |
| 50 | □ 按钮权限是否定义 | 操作按钮对应权限点 |
| 51 | □ 路由权限是否定义 | 页面路由对应权限点 |

## 20.8 Mock规范（6项）

| # | 检查项 | 说明 |
|---|--------|------|
| 52 | □ 是否定义了Mock数据策略 | 固定/随机/分页/异常 |
| 53 | □ 是否支持随机数据和固定数据 | 列表随机，登录固定 |
| 54 | □ 是否支持延迟模拟 | 200-800ms |
| 55 | □ AI状态是否全部Mock | 14种状态全覆盖 |
| 56 | □ 图表数据是否定义完整 | 7种图表类型 |
| 57 | □ 是否支持异常场景Mock | 超时/500/401/403/空数据 |

## 20.9 文件规范（5项）

| # | 检查项 | 说明 |
|---|--------|------|
| 58 | □ 是否定义了文件大小限制 | 50MB/200MB/10MB/5MB |
| 59 | □ 是否定义了支持的文件类型 | PDF/DOCX/ZIP/PNG/JPG/JPEG/GIF |
| 60 | □ 文件命名规范是否统一 | {userId}_{timestamp}_{uuid}.{ext} |
| 61 | □ 是否定义了文件上传Content-Type | multipart/form-data |
| 62 | □ 文件下载是否返回正确的Content-Disposition | attachment; filename="..." |

## 20.10 AI规范（5项）

| # | 检查项 | 说明 |
|---|--------|------|
| 63 | □ AI接口是否独立模块 | /api/v1/ai/路径 |
| 64 | □ AI状态枚举是否完整 | PENDING/PROCESSING/COMPLETED/FAILED/CANCELLED/REVIEWED |
| 65 | □ 是否支持SSE流式推送 | /api/v1/ai/analyze/{id}/stream |
| 66 | □ AI返回JSON结构是否定义 | overallScore/dimensions/summary/strengths等 |
| 67 | □ Token统计接口是否定义 | /api/v1/ai/token-stats |

## 20.11 安全规范（4项）

| # | 检查项 | 说明 |
|---|--------|------|
| 68 | □ 敏感接口是否需要认证 | 除login/forgot-password外均需认证 |
| 69 | □ Token是否有过期时间 | accessToken 2h, refreshToken 7d |
| 70 | □ 是否支持刷新Token | /api/v1/auth/refresh |
| 71 | □ 密码是否不在响应中返回 | 任何接口不返回password字段 |

## 20.12 文档与一致性（4项）

| # | 检查项 | 说明 |
|---|--------|------|
| 72 | □ 每个接口是否包含Request示例 | JSON格式 |
| 73 | □ 每个接口是否包含Response示例 | 至少成功+1种失败 |
| 74 | □ 字段类型是否全部标注 | String/Integer/Long/Boolean/Array/Object |
| 75 | □ 必填/可选是否全部标注 | 每个字段明确标出 |

---

## 修订历史

| 版本 | 日期 | 修订内容 | 修订人 |
|------|------|---------|--------|
| v1.0 | 2026-06-30 | 初始版本，完成20章全部接口规范定义 | 架构团队 |

## 文档签收

| 角色 | 签收人 | 签收日期 | 备注 |
|------|--------|---------|------|
| 前端开发 | | | 依此文档进行Mock.js和Axios开发 |
| 后端开发 | | | 依此文档进行Spring Boot接口实现 |
| 测试工程师 | | | 依此文档编写接口测试用例 |
| 项目经理 | | | 依此文档跟踪接口开发进度 |

---

**文档结束**

本文档是 Mock.js / Axios / Spring Boot / OpenAPI 四方共同遵循的唯一数据契约。任何接口变更必须首先更新本文档，经评审通过后方可实施代码变更。
