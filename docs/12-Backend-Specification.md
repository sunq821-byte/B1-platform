# Backend Specification v1.0

## 基于大模型的软件实训教学检查评价与报表系统

---

## 1. Document Information

| 字段 | 值 |
|---|---|
| **文档名称** | Backend Specification（后端开发规范） |
| **文档版本** | v1.0 |
| **文档状态** | Formal Release |
| **作者** | Principal Java Architect |
| **审核人** | TBD |
| **最后更新** | 2026-07-04 |
| **适用 Sprint** | Sprint 1（基础设施）→ Sprint 7（上线） |
| **前置文档** | PRD v2.0, SDS v1.0, Backend Architecture Design v1.0, Database Design v1.0, UI Design System v1.0, Component Library v1.0, Frontend Specification v1.0, API Mock Specification v1.0, FIP v1.0, ADR-001~011, Definition of Done v1.0, MVP |
| **适用范围** | 后端全部开发工作（Claude、Codex、所有开发人员必须严格遵守） |
| **文档定位** | 本项目后端唯一编码规范。所有后端代码必须通过本规范定义的 Code Review Checklist 方可合并 |

### Revision History

| 版本 | 日期 | 作者 | 变更说明 |
|---|---|---|---|
| v1.0 | 2026-07-04 | Principal Java Architect | 初始版本，覆盖 MVP 全部后端开发规范 |

---

## 2. General Principles

### 2.1 SOLID

| 原则 | 全称 | 后端实践要求 |
|---|---|---|
| **S** | Single Responsibility | 每个类只有一个变化原因。Service 不处理 HTTP 请求，Controller 不编写业务逻辑，Mapper 只做数据访问 |
| **O** | Open/Closed | 对扩展开放，对修改关闭。通过 `AIAnalysisProvider`、`FileStorageProvider` 接口扩展新实现，不修改已有代码 |
| **L** | Liskov Substitution | 子类必须可以完全替换父类。接口实现类必须 100% 满足接口契约，不得抛出接口未声明的异常 |
| **I** | Interface Segregation | 接口应小而专一。不创建包含 10+ 方法的"万能接口"。`AIAnalysisProvider` 仅定义 4 个方法 |
| **D** | Dependency Inversion | 高层模块不依赖低层模块，都依赖抽象。Service 依赖 `AIAnalysisProvider` 接口，不依赖 `DeepSeekProvider` 实现类 |

### 2.2 KISS（Keep It Simple, Stupid）

- 优先使用标准库和框架内置能力，不引入不必要的第三方库
- 一个方法只做一件事，方法体不超过 50 行
- 避免过度设计：不需要为"可能"的扩展预留抽象层
- 能用 MyBatis Plus BaseMapper 解决的 CRUD 不写 XML
- 能用 MapStruct 自动生成的转换不手写

### 2.3 DRY（Don't Repeat Yourself）

- 相同逻辑出现 3 次必须抽取为公共方法或工具类
- 公共常量、枚举、异常码定义在 `common` 包，各模块引用而非各自定义
- 相同的校验逻辑提取为自定义 `@Constraint` 注解
- 相同的配置属性提取为 `@ConfigurationProperties` 类

### 2.4 YAGNI（You Ain't Gonna Need It）

- 不为"可能用到"的功能提前编写代码
- 不为"未来拆分微服务"提前引入 Spring Cloud 依赖
- MVP 阶段：模块化单体足够，不引入服务发现、配置中心、分布式事务
- 只在确实需要时才添加抽象层

### 2.5 Clean Architecture

- 依赖方向：Controller → Service（接口） → Mapper → Database
- 内层不依赖外层：Service 不引用 Controller，Entity 不引用 DTO
- 模块间通过接口和事件通信，不直接依赖实现类
- 外部依赖（LLM API、MinIO、Git）通过 Provider 接口隔离

### 2.6 Clean Code

- 命名即文档：方法名准确描述行为（`findUserByUsername` 而非 `getUser`）
- 方法短小：单一职责，不超过 50 行
- 参数精简：方法参数不超过 4 个，超过则封装为 DTO
- 异常明确：不 catch Exception 后吞掉，不返回 null 表示错误
- 禁止注释描述"做什么"（代码本身已表达），仅注释"为什么这样做"

---

## 3. Directory Specification

### 3.1 完整目录结构

```
server/src/main/java/com/b1/
├── B1Application.java                    # Spring Boot 入口
│
├── common/                                # 公共基础设施层
│   ├── config/                            # Spring 配置类
│   ├── exception/                         # 全局异常处理
│   ├── result/                            # 统一响应体
│   ├── interceptor/                       # HTTP 拦截器
│   ├── aspect/                            # AOP 切面
│   ├── util/                              # 无状态工具类
│   ├── constant/                          # 系统常量
│   ├── enums/                             # 公共枚举
│   └── validation/                        # 自定义校验注解
│
├── module/                                # 业务模块
│   ├── auth/                              # 认证模块
│   ├── user/                              # 用户模块
│   ├── course/                            # 课程模块
│   ├── training/                          # 实训任务模块
│   ├── submission/                        # 提交模块
│   ├── ai/                                # AI 分析模块
│   ├── review/                            # 教师复核模块
│   ├── report/                            # 报表模块
│   ├── standard/                          # 评价标准模块
│   ├── system/                            # 系统管理模块
│   ├── file/                              # 文件模块
│   ├── git/                               # Git 模块
│   ├── log/                               # 日志模块
│   └── notification/                      # 通知模块
│
└── infrastructure/                        # 基础设施实现层
    ├── security/                          # Sa-Token 认证配置
    ├── persistence/                       # MyBatis Plus 配置
    ├── redis/                             # Redis 配置
    └── minio/                             # MinIO 配置
```

### 3.2 每个业务模块内部结构

```
module/{name}/
├── controller/        # REST 控制器（一个模块可有多个 Controller）
├── service/           # 业务接口（接口声明）
│   └── impl/          # 业务实现（接口实现）
├── mapper/            # MyBatis Mapper 接口
├── entity/            # 数据库实体（Entity）
├── dto/               # 请求参数对象（Data Transfer Object）
├── vo/                # 响应视图对象（View Object）
├── convert/           # MapStruct 转换器接口
├── event/             # Spring 事件定义（如有）
└── listener/          # Spring 事件监听器（如有）
```

### 3.3 各目录职责与约束

| 目录 | 职责 | 允许放置 | 禁止放置 |
|---|---|---|---|
| `controller/` | 接收 HTTP 请求，参数校验，调用 Service，返回 Result | REST Controller 类（`@RestController`） | 业务逻辑、直接调用 Mapper、SQL 语句 |
| `service/` | 业务逻辑定义（接口） | 接口声明，不含实现 | 实现代码（必须在 impl/ 中） |
| `service/impl/` | 业务逻辑实现 | 接口实现类（`@Service`），事务管理 | HTTP 请求/响应处理、直接 JDBC 操作 |
| `mapper/` | 数据库访问接口 | MyBatis Mapper 接口（`@Mapper`），继承 `BaseMapper` | 业务逻辑、Service 调用 |
| `entity/` | 数据库实体映射 | POJO + Lombok `@Data` + `@TableName` | 业务逻辑、校验注解（校验在 DTO 层） |
| `dto/` | 客户端→服务端请求参数 | POJO + Lombok `@Data` + `@Schema` + Jakarta Validation | 数据库映射注解、业务逻辑 |
| `vo/` | 服务端→客户端响应数据 | POJO + Lombok `@Data` + `@Schema` | 数据库映射注解、密码等敏感字段 |
| `convert/` | Entity ↔ DTO/VO 对象转换 | MapStruct `@Mapper(componentModel = "spring")` 接口 | 业务逻辑、SQL 操作 |
| `event/` | 模块间异步通信事件 | Spring ApplicationEvent 子类 | 业务逻辑 |
| `listener/` | 异步事件处理 | `@EventListener` 或 `@TransactionalEventListener` 方法 | 同步业务逻辑（异步处理不应阻塞主流程） |
| `common/config/` | Spring Bean 注册和第三方库初始化 | `@Configuration` 类 | 业务逻辑 |
| `common/exception/` | 异常体系 | BusinessException 及其子类、ErrorCode 枚举、GlobalExceptionHandler | 业务逻辑 |
| `common/result/` | 统一 API 响应格式 | Result、PageResult | 业务逻辑 |
| `common/interceptor/` | HTTP 请求拦截 | HandlerInterceptor 实现 | 业务逻辑 |
| `common/aspect/` | 横切关注点 | `@Aspect` 切面类（操作日志、AI 日志） | 业务逻辑 |
| `common/util/` | 纯工具函数 | 静态方法工具类 | 有状态 Bean、依赖 Spring 管理的类 |
| `common/constant/` | 系统常量 | Redis Key 常量、系统配置常量 | 业务枚举（放 enums/） |
| `common/enums/` | 跨模块公共枚举 | RoleEnum、SubmissionStatus、AnalysisStatus | 模块专属枚举（放模块内） |
| `common/validation/` | 自定义校验注解 | `@Constraint` 注解 + Validator 实现 | 业务逻辑 |
| `infrastructure/` | 基础设施实现 | Sa-Token 配置、MyBatis Plus 自动填充、Redis 序列化 | 业务逻辑 |

### 3.4 文件命名规范

| 类型 | 命名规则 | 示例 |
|---|---|---|
| Controller | `{Entity}Controller.java` | `AuthController.java`, `CourseController.java` |
| Service 接口 | `{Entity}Service.java` | `AuthService.java`, `SubmissionService.java` |
| Service 实现 | `{Entity}ServiceImpl.java` | `AuthServiceImpl.java` |
| Mapper | `{Entity}Mapper.java` | `UserMapper.java`, `CourseMapper.java` |
| Entity | `{TableName}.java`（PascalCase） | `User.java`, `TrainingTask.java` |
| DTO | `{Action}{Entity}Request.java` | `LoginRequest.java`, `CreateCourseRequest.java` |
| VO | `{Entity}{View}VO.java` | `UserInfoVO.java`, `CourseDetailVO.java` |
| Convert | `{Entity}Convert.java` | `UserConvert.java`, `CourseConvert.java` |
| Event | `{Action}{Entity}Event.java` | `SubmissionCompletedEvent.java` |
| Listener | `{Action}Listener.java` | `AIAnalysisListener.java` |
| Config | `{Technology}Config.java` | `SaTokenConfig.java`, `RedisConfig.java` |
| Util | `{Function}Util.java` | `FileUtil.java`, `SnowflakeUtil.java` |

---

## 4. Controller Specification

### 4.1 职责

Controller 层只做三件事：
1. 接收并校验 HTTP 请求参数
2. 调用 Service 层方法
3. 将 Service 返回结果包装为 `Result<T>` 返回

### 4.2 命名规范

| 规则 | 示例 |
|---|---|
| 类名 | `{Entity}Controller`，`@RestController` 注解 |
| 类级别 `@RequestMapping` | `@RequestMapping("/api/v1/{role-prefix}/{resource}")` |
| 方法名 | 动词 + 名词，如 `createCourse`, `listTasks`, `getSubmissionDetail`, `deleteUser` |
| URL 路径 | kebab-case 小写连字符，如 `/api/v1/teacher/tasks/:taskId/submissions` |

### 4.3 返回值规范

- 所有接口方法返回 `Result<T>`，永远不返回裸数据
- 列表接口返回 `PageResult<T>`（继承 `Result<List<T>>` 并包含 page/pageSize/total/totalPages）
- 文件下载接口返回 `ResponseEntity<Resource>`（不包装为 Result）
- 禁止返回 `void`、`String`、裸 `Entity`

### 4.4 参数规范

- 路径参数使用 `@PathVariable`
- 查询参数使用 `@RequestParam`（非必选参数设 `required = false` + `defaultValue`）
- 请求体使用 `@Valid @RequestBody` DTO 对象
- 当前用户 ID 从 Sa-Token 获取：`StpUtil.getLoginIdAsLong()`，不从请求参数传入
- 禁止使用 `HttpServletRequest` / `HttpServletResponse` 作为方法参数

### 4.5 REST 规范

| HTTP 方法 | 语义 | Controller 示例 |
|---|---|---|
| `@GetMapping` | 查询资源（幂等、无副作用） | `getTask(Long taskId)`, `listTasks(PageQuery query)` |
| `@PostMapping` | 创建资源（非幂等） | `createTask(@Valid CreateTaskRequest req)` |
| `@PutMapping` | 全量更新资源（幂等） | `updateTask(Long taskId, @Valid UpdateTaskRequest req)` |
| `@PatchMapping` | 部分更新资源 | `updateTaskStatus(Long taskId, @Valid StatusRequest req)` |
| `@DeleteMapping` | 删除资源（幂等，逻辑删除） | `deleteTask(Long taskId)` |

### 4.6 Controller 禁止事项

- 禁止直接调用 Mapper（必须通过 Service）
- 禁止在 Controller 中编写业务逻辑（条件判断、循环、计算）
- 禁止在 Controller 中手动开启事务
- 禁止在 Controller 中捕获异常（交给 GlobalExceptionHandler）
- 禁止返回未经包装的 Entity 对象
- 禁止在 `@RequestMapping` 中使用动词（如 `/createTask`，操作语义由 HTTP 方法表达）

### 4.7 Knife4j 文档注解规范

| 注解 | 位置 | 示例 |
|---|---|---|
| `@Tag(name = "模块名", description = "模块描述")` | Controller 类 | `@Tag(name = "认证模块", description = "登录、登出、Token 刷新")` |
| `@Operation(summary = "接口描述")` | Controller 方法 | `@Operation(summary = "用户登录")` |
| `@Schema(description = "字段描述")` | DTO/VO 字段 | `@Schema(description = "用户名", example = "zhangsan")` |
| `@Parameter(description = "参数描述")` | 方法参数 | `@Parameter(description = "任务ID") @PathVariable Long taskId` |

---

## 5. Service Specification

### 5.1 职责

Service 层是业务逻辑的核心。职责包括：
1. 业务逻辑编排（调用 Mapper、其他 Service、外部 Provider）
2. 事务管理（`@Transactional` 注解）
3. 业务异常抛出（`throw new BusinessException(ErrorCode.xxx)`）
4. Spring Event 发布（模块间异步通信）

### 5.2 接口与实现分离

- 接口定义在 `service/` 包，实现定义在 `service/impl/` 包
- 接口命名：`{Entity}Service.java`
- 实现命名：`{Entity}ServiceImpl.java`
- 实现类标注 `@Service` 和 `@Slf4j`
- 接口方法声明 JavaDoc 描述"做什么"
- 实现方法内部注释描述关键步骤"怎么做"

### 5.3 事务规范

| 规则 | 说明 |
|---|---|
| **注解位置** | `@Transactional` 注解在 Service 实现类的方法上，不在接口上 |
| **写操作** | INSERT、UPDATE、DELETE 操作必须加 `@Transactional` |
| **只读操作** | SELECT 操作使用 `@Transactional(readOnly = true)` |
| **超时设置** | 默认 30 秒，通过 `timeout` 属性配置 |
| **回滚策略** | `RuntimeException` 及其子类自动回滚；checked Exception 需显式指定 `rollbackFor` |
| **禁止事项** | 不在事务中调用外部 API（LLM、MinIO、Git）；不在事务中执行文件 I/O；不在事务中发送消息/通知 |

### 5.4 依赖规范

- Service 通过构造函数注入依赖（Lombok `@RequiredArgsConstructor`），不使用 `@Autowired` 字段注入
- Service 依赖其他 Service 接口，不依赖实现类
- Service 依赖 Mapper 接口、Provider 接口
- Service 不依赖 Controller、HttpServletRequest、HttpServletResponse

### 5.5 禁止事项

- 禁止 Service 调用 Controller
- 禁止 Service 处理 HTTP 请求或响应
- 禁止 Service 返回 JSON 字符串
- 禁止在 Service 中使用 `try-catch` 吞掉异常不处理
- 禁止 Service 方法返回 null（空列表返回 `Collections.emptyList()`，空对象抛出 BusinessException）

---

## 6. Mapper Specification

### 6.1 MyBatis Plus 规范

- 所有 Mapper 接口继承 `BaseMapper<Entity>`，自动获得基础 CRUD 方法
- Mapper 接口标注 `@Mapper` 注解
- 单表操作：直接使用 BaseMapper 提供的方法（`selectById`、`insert`、`updateById`、`deleteById`）
- 条件查询：使用 `LambdaQueryWrapper` 构建类型安全的查询条件，禁止字符串硬编码字段名
- 分页查询：使用 MyBatis Plus `Page<Entity>` + `IPage<VO>`

### 6.2 XML 规范

- 仅复杂查询使用 XML（多表 JOIN、聚合统计、批量操作）
- XML 文件路径：`src/main/resources/mapper/{Entity}Mapper.xml`
- XML 命名空间：`com.b1.module.{name}.mapper.{Entity}Mapper`
- XML 中 100% 使用 `#{}` 预编译参数，禁止 `${}` 拼接用户输入
- `${}` 仅用于动态表名/列名等非用户输入场景

### 6.3 分页规范

- 所有列表查询必须分页
- 分页参数封装为 `PageQuery` 基类（`page` + `pageSize`，默认 `page=1, pageSize=20`）
- MyBatis Plus `PaginationInnerInterceptor` 自动处理分页
- `pageSize` 上限硬截断为 100（防止恶意请求耗尽数据库资源）
- 分页查询结果使用 MyBatis Plus `IPage<T>` 接收
- 分页查询同时返回 `total` 总数

### 6.4 禁止事项

- 禁止在 Mapper 接口中编写业务逻辑
- 禁止在 Mapper XML 中编写业务判断
- 禁止 Mapper 调用 Service
- 禁止在 Mapper 中手动管理数据库连接
- 禁止使用 `${}` 拼接用户输入（SQL 注入风险）

---

## 7. DTO Specification

### 7.1 何时使用 DTO

- 所有 Controller 接收的请求参数必须使用 DTO（禁止直接接收 Entity）
- 每个 API 接口应有自己专属的 DTO 类
- 不同接口的 DTO 不共用（即使字段完全相同），保持接口独立性

### 7.2 命名规范

- 命名模式：`{Action}{Entity}Request.java`
- 示例：`LoginRequest`、`CreateCourseRequest`、`UpdateTaskRequest`、`PageQueryRequest`
- 位置：`module/{name}/dto/`

### 7.3 字段规范

- 使用 Lombok `@Data` 注解
- 字段使用 `@Schema(description = "...", example = "...")` 生成 API 文档
- 字段使用 Jakarta Validation 注解声明校验规则
- 字段类型优先使用 Java 基本类型的包装类（`Long` 而非 `long`），以便区分 null 和默认值
- 日期时间字段：前端传入使用 `String`（ISO 8601 格式），后端在 Service 层转换为 `LocalDateTime`

### 7.4 Validation 规范

- 必填字段标注 `@NotNull`（引用类型）或 `@NotBlank`（字符串）
- 字符串长度标注 `@Size(min, max)`
- 邮箱标注 `@Email`
- 数值范围标注 `@Min` / `@Max`
- 枚举值使用自定义 `@EnumValue` 校验（确保值在枚举定义范围内）
- 禁止仅依赖前端校验，后端必须独立校验

### 7.5 禁止事项

- 禁止使用 Entity 直接接收请求参数
- 禁止 DTO 中包含数据库映射注解
- 禁止 DTO 中包含 `id` 字段的 Snowflake 生成逻辑
- 禁止 DTO 字段不加 Validation 注解

---

## 8. VO Specification

### 8.1 返回规范

- 所有 Controller 的返回数据必须使用 VO（禁止直接返回 Entity）
- 每个 API 接口应有自己专属的 VO 类
- VO 仅包含前端需要展示的字段，不暴露内部实现细节

### 8.2 命名规范

- 命名模式：`{Entity}{View}VO.java`
- 示例：`UserInfoVO`、`CourseDetailVO`、`SubmissionListVO`、`DashboardStatsVO`
- 位置：`module/{name}/vo/`

### 8.3 字段规范

- 使用 Lombok `@Data` 注解
- 字段使用 `@Schema(description = "...")` 生成 API 文档
- 日期时间字段使用 `String` 类型（ISO 8601 格式），由 Convert 层将 `LocalDateTime` 转为字符串
- 敏感字段（password、token）永远不包含在 VO 中

### 8.4 禁止事项

- 禁止 Entity 直接返回给前端
- 禁止 VO 包含数据库映射注解
- 禁止 VO 包含密码字段（`password`）
- 禁止 VO 包含 Token 字段
- 禁止 VO 包含不需要前端展示的内部字段

---

## 9. Entity Specification

### 9.1 字段规范

- 使用 Lombok `@Data` 注解，禁止手写 getter/setter
- 使用 `@TableName("{table_name}")` 映射数据库表
- 主键使用 `@TableId(type = IdType.ASSIGN_ID)`（Snowflake 算法自动生成）
- 逻辑删除字段使用 `@TableLogic`
- 审计字段（`create_time`、`update_time`）使用 `@TableField(fill = FieldFill.INSERT)` 和 `@TableField(fill = FieldFill.INSERT_UPDATE)` 自动填充
- 乐观锁字段使用 `@Version`
- 时间字段使用 `LocalDateTime` 类型
- 枚举字段使用 `String` 类型存储枚举名称
- 金额/分数字段使用 `BigDecimal` 类型

### 9.2 审计字段

所有业务表 Entity 必须包含以下字段：

| 字段 | 类型 | 注解 | 说明 |
|---|---|---|---|
| `createTime` | `LocalDateTime` | `@TableField(fill = FieldFill.INSERT)` | 自动填充创建时间 |
| `updateTime` | `LocalDateTime` | `@TableField(fill = FieldFill.INSERT_UPDATE)` | 自动填充更新时间 |
| `createBy` | `Long` | `@TableField(fill = FieldFill.INSERT)` | 自动填充创建人 ID |
| `updateBy` | `Long` | `@TableField(fill = FieldFill.INSERT_UPDATE)` | 自动填充更新人 ID |

实现：`MyMetaObjectHandler implements MetaObjectHandler`，结合 `StpUtil.getLoginIdAsLong()` 自动填充。

### 9.3 逻辑删除

- 字段名：`deleted`
- 类型：`Integer`
- 注解：`@TableLogic`（`value = "0"`, `delval = "1"`）
- 适用范围：所有主数据表（user、course、class、training_task、evaluation_standard 等）
- 不适用范围：关联表（物理删除）、日志表（不可删除）

### 9.4 乐观锁

- 字段名：`version`
- 类型：`Integer`
- 注解：`@Version`
- 默认值：`0`
- 适用范围：写频繁且并发冲突风险高的表（submission、ai_analysis、teacher_review、score_record、system_config）

### 9.5 禁止事项

- 禁止在 Entity 中使用 `@JsonIgnore` 等 JSON 序列化注解（序列化由 VO 负责）
- 禁止在 Entity 中使用校验注解
- 禁止在 Entity 中包含业务逻辑
- 禁止使用 MySQL 关键字作为字段名

---

## 10. Convert Specification

### 10.1 MapStruct 规范

- 所有 Entity ↔ DTO/VO 转换必须使用 MapStruct，禁止手写 `BeanUtils.copyProperties` 或手动逐字段 set
- 转换接口定义在 `module/{name}/convert/` 包
- 命名：`{Entity}Convert.java`
- 注解：`@Mapper(componentModel = "spring")`
- MapStruct 接口注入 Service 层使用，Controller 不直接使用 Convert

### 10.2 转换方法命名

| 转换方向 | 方法名 | 说明 |
|---|---|---|
| DTO → Entity | `toEntity(Dto dto)` | 请求参数转为数据库实体 |
| Entity → VO | `toVO(Entity entity)` | 数据库实体转为响应视图 |
| List<Entity> → List<VO> | `toVOList(List<Entity> entities)` | 批量转换 |

### 10.3 自定义映射

- 当源字段与目标字段名不同时，使用 `@Mapping(source = "...", target = "...")` 注解
- 当需要自定义转换逻辑时，使用 `@Mapping(target = "...", expression = "java(...)")` 或 `qualifiedByName`
- 当需要忽略某个字段时，使用 `@Mapping(target = "...", ignore = true)`
- 日期格式化：`@Mapping(target = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")`

### 10.4 禁止事项

- 禁止手写 Bean 属性拷贝代码（`BeanUtils.copyProperties`、手动 set/get）
- 禁止在 Convert 接口中包含业务逻辑
- 禁止 Convert 调用 Mapper、Service

---

## 11. Exception Specification

### 11.1 BusinessException

- 所有业务异常抛出 `BusinessException` 或其子类
- `BusinessException` 构造参数：`ErrorCode` 枚举值
- 子类继承体系（按业务域划分）：
  - `AuthException` — 认证异常（2000-2999）
  - `PermissionException` — 权限异常（3000-3999）
  - `ValidationException` — 参数校验异常（4000-4999）
  - `AIException` — AI 调用异常（6000-6999）
  - `FileException` — 文件异常（7000-7999）
  - `GitException` — Git 异常（8000-8999）
  - `ExportException` — 导出异常（9000-9999）

### 11.2 ErrorCode 枚举

所有错误码定义在 `common/exception/ErrorCode.java` 中，不在各模块分散定义。

**错误码编码规则**：

| 范围 | 类别 | 示例 |
|---|---|---|
| 0 | 成功 | `SUCCESS(0, "success")` |
| 1000-1999 | 通用业务错误 | `NOT_FOUND(1001, "资源不存在")`, `SUBMIT_LIMIT_EXCEEDED(1005, "提交次数已达上限")` |
| 2000-2999 | 认证错误 | `NOT_LOGGED_IN(2001, "未登录")`, `TOKEN_EXPIRED(2002, "Token 已过期")` |
| 3000-3999 | 权限错误 | `NO_PERMISSION(3001, "无访问权限")` |
| 4000-4999 | 参数校验错误 | `PARAM_ERROR(4001, "参数校验失败")` |
| 5000-5999 | 系统错误 | `INTERNAL_ERROR(5001, "服务器内部错误")`, `DB_ERROR(5002, "数据库操作失败")` |
| 6000-6999 | AI 错误 | `AI_MODEL_CALL_FAILED(6002, "AI 模型调用失败")`, `AI_ANALYSIS_TIMEOUT(6003, "AI 分析超时")` |
| 7000-7999 | 文件错误 | `FILE_TOO_LARGE(7001, "文件大小超过限制")`, `FILE_TYPE_UNSUPPORTED(7002, "不支持的文件类型")` |
| 8000-8999 | Git 错误 | `GIT_REPO_NOT_FOUND(8001, "Git 仓库不存在")`, `GIT_CLONE_FAILED(8003, "Git 仓库克隆失败")` |
| 9000-9999 | 导出错误 | `EXPORT_FAILED(9001, "报表导出失败")`, `EXPORT_DATA_TOO_LARGE(9002, "导出数据量过大")` |

**每个 ErrorCode 必须包含**：
- `code`（int）：错误码数值
- `message`（String）：面向用户的错误描述（中文）
- 错误码全局唯一，不可重复

### 11.3 GlobalExceptionHandler

`@RestControllerAdvice` 全局异常处理器处理以下异常：

| 异常类型 | 处理方式 | HTTP 状态码 |
|---|---|---|
| `BusinessException` | 返回 `Result.err(e.getCode(), e.getMessage())` | 200（业务错误不应改变 HTTP 状态码，通过 Result.code 区分） |
| `MethodArgumentNotValidException` | 拼接字段校验错误信息 | 200 |
| `NotLoginException`（Sa-Token） | 返回 `Result.err(2001, "未登录或 Token 已过期")` | 200 |
| `NotPermissionException`（Sa-Token） | 返回 `Result.err(3001, "无访问权限")` | 200 |
| `Exception`（兜底） | 打印 ERROR 日志，返回 `Result.err(5001, "服务器内部错误")` | 200 |

### 11.4 Assert 业务断言工具

`common/exception/Assert.java` 提供静态断言方法：

- `Assert.notNull(obj, ErrorCode)` — 断言非空
- `Assert.notBlank(str, ErrorCode)` — 断言字符串非空
- `Assert.isTrue(condition, ErrorCode)` — 断言条件为真
- `Assert.notExceed(value, max, ErrorCode)` — 断言不超过上限

目的是将 Service 中的 `if (condition) throw new BusinessException(...)` 简化为一行断言调用。

---

## 12. Result Specification

### 12.1 统一返回格式

所有 API 接口返回 `Result<T>`：

```json
{
  "code": 0,
  "message": "success",
  "data": { ... },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | `int` | 业务状态码：0=成功，非 0=失败 |
| `message` | `String` | 提示信息：成功时 "success"，失败时错误描述 |
| `data` | `T`（泛型） | 业务数据：成功时返回数据，失败时 null |
| `success` | `boolean` | 是否成功：code==0 时为 true |
| `timestamp` | `long` | 响应时间戳（毫秒） |
| `traceId` | `String` | 分布式链路追踪 ID（UUID 前 8 位），通过 LogInterceptor 注入 MDC |

### 12.2 成功响应

- 查询单个：`Result.ok(vo)`
- 查询列表：`Result.ok(pageResult)` — PageResult 继承 Result，包含分页元数据
- 创建成功：`Result.ok(createdVO)` — 返回创建的完整对象
- 更新成功：`Result.ok(updatedVO)` — 返回更新后的完整对象
- 删除成功：`Result.ok(null)` 或 `Result.ok()` — 无数据返回
- 操作成功无需返回数据：`Result.ok()` — data 为 null

### 12.3 分页响应

`PageResult<T>` 继承 `Result<List<T>>`：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [ ... ],
    "page": 1,
    "pageSize": 20,
    "total": 156,
    "totalPages": 8
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

### 12.4 错误响应

```json
{
  "code": 2004,
  "message": "用户名或密码错误",
  "data": null,
  "success": false,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

### 12.5 禁止事项

- 禁止自定义返回格式绕过 `Result<T>`
- 禁止在 Controller 中直接返回 JSON 字符串
- 禁止在 Result 成功时将错误信息放入 data 字段

---

## 13. Validation

### 13.1 Jakarta Validation 注解

| 注解 | 适用类型 | 说明 |
|---|---|---|
| `@NotNull` | 所有引用类型 | 字段不能为 null |
| `@NotBlank` | `String` | 字符串不能为 null、空串、纯空格 |
| `@NotEmpty` | `String`、`Collection`、`Map`、`Array` | 不能为 null 或空 |
| `@Size(min, max)` | `String`、`Collection`、`Map`、`Array` | 长度/大小范围 |
| `@Min(value)` | 数值类型 | 最小值 |
| `@Max(value)` | 数值类型 | 最大值 |
| `@Email` | `String` | 邮箱格式 |
| `@Pattern(regexp)` | `String` | 正则匹配 |
| `@Valid` | 嵌套对象 | 级联校验嵌套对象内的校验注解 |

### 13.2 校验位置

- Controller 方法参数使用 `@Valid` 或 `@Validated`
- DTO 字段使用 Jakarta Validation 注解
- 校验失败时 `MethodArgumentNotValidException` 被 GlobalExceptionHandler 统一处理
- 业务逻辑校验（如"用户名已存在"）在 Service 层通过 Assert 断言处理

### 13.3 自定义校验

- 自定义校验注解定义在 `common/validation/` 包
- 使用 `@Constraint(validatedBy = XxxValidator.class)` 绑定校验逻辑
- 示例场景：
  - `@EnumValue(enumClass = RoleEnum.class)` — 校验值在枚举定义范围内
  - `@FileType(allow = {"zip", "pdf", "doc"})` — 校验文件扩展名
  - `@StrongPassword` — 校验密码强度（8+ 位，大小写+数字）

### 13.4 分组校验

- 创建和更新场景使用不同的校验分组
- 定义 `CreateGroup` 和 `UpdateGroup` 接口（空标记接口）
- 示例：`@NotNull(groups = CreateGroup.class)` — 仅创建时必填；更新时可为 null 表示不修改

### 13.5 禁止事项

- 禁止仅在前端做校验，后端不做校验
- 禁止在 Controller 中手动 if-else 做参数校验（使用 Validation 注解）
- 禁止校验逻辑写在 Entity 层

---

## 14. Logging

### 14.1 Slf4j 规范

- 使用 Lombok `@Slf4j` 注解注入日志对象
- 日志对象名统一为 `log`
- 禁止使用 `System.out.println`、`System.err.println`
- 禁止创建多个日志实例

### 14.2 日志等级使用

| 级别 | 使用场景 | 示例 |
|---|---|---|
| **ERROR** | 需要人工介入的系统错误、异常 | 数据库连接失败、AI 调用异常、文件存储失败 |
| **WARN** | 潜在问题、降级处理 | Token 即将过期、缓存未命中、重试操作 |
| **INFO** | 关键业务节点 | 用户登录/登出、提交创建、AI 分析完成、成绩发布 |
| **DEBUG** | 调试信息（仅 dev 环境开启） | 方法入参出参、SQL 参数、中间计算结果 |
| **TRACE** | 最细粒度追踪（生产环境禁止） | 循环内日志、逐行执行日志 |

### 14.3 日志内容规范

- 日志格式（Logback 配置）：`%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId}] %-5level %logger{50} - %msg%n`
- traceId 由 `LogInterceptor` 在请求入口注入 MDC，全链路传递
- 日志消息使用占位符（`log.info("用户登录: userId={}", userId)`），不使用字符串拼接
- 异常日志使用 `log.error("AI 分析失败: submissionId={}", submissionId, exception)`，传入异常对象确保堆栈完整打印

### 14.4 禁止记录的信息

| 禁止记录 | 原因 |
|---|---|
| 用户密码（明文或密文） | 安全合规 |
| Token（Access Token / Refresh Token） | 安全合规 |
| 完整身份证号 | 隐私保护 |
| 完整手机号（记录前脱敏为 `138****5678`） | 隐私保护 |
| 文件二进制内容 | 日志膨胀 |
| LLM API Key | 安全合规 |
| 超过 1000 字符的 LLM 响应（记录前截断） | 日志膨胀 |

### 14.5 AI 调用日志

- AI 调用入口：记录 `submissionId`、模型名、输入 Token 估算值
- AI 调用出口：记录分析耗时（ms）、实际 Token 用量（input/output/total）、分析状态（COMPLETED/FAILED）
- AI 调用失败：记录失败原因、重试次数、使用的 Provider 名称
- AI 日志通过 `@AILog` 注解 + `AILogAspect` 切面自动记录，不侵入业务代码

### 14.6 操作日志

- 通过 `@OperationLog(module = "...", operation = "...", description = "...")` 注解记录
- 由 `OperationLogAspect` 切面自动拦截并异步写入 `operation_log` 表
- 操作日志记录：操作人、所属模块、操作类型、操作对象、操作详情、IP 地址、操作耗时、操作结果

---

## 15. Transaction

### 15.1 @Transactional 使用规范

| 规则 | 说明 |
|---|---|
| **注解位置** | Service 实现类的方法上 |
| **默认传播** | `REQUIRED`（有事务则加入，无则新建） |
| **默认隔离级别** | `READ_COMMITTED`（MySQL InnoDB 默认级别） |
| **默认超时** | 30 秒 |
| **回滚规则** | `RuntimeException` 及其子类自动回滚 |

### 15.2 需要事务的业务

| 业务 | 涉及表 | 超时 |
|---|---|---|
| 用户创建 + 角色分配 | `user` + `user_role` | 10s |
| 课程创建 + 教师/班级关联 | `course` + `course_teacher` + `course_class` + `course_student` | 15s |
| 任务发布 + 班级分发 | `training_task` + `training_class` | 10s |
| 提交创建 + 文件关联 | `submission` + `submission_file` | 10s |
| 教师评分发布 | `teacher_review` + `review_item` + `score_record` + `submission` | 15s |
| 评价标准创建 + 维度 + 规则 | `evaluation_standard` + `standard_dimension` + `standard_rule` | 15s |

### 15.3 不能开启事务的业务

| 业务 | 原因 | 替代方案 |
|---|---|---|
| AI 分析 | LLM 调用耗时 30-120s，阻塞连接池 | 异步处理 + 独立事务更新状态 |
| 文件上传到 MinIO | MinIO 不支持 MySQL 事务协议 | 补偿逻辑（上传成功写 DB，失败删 MinIO 文件） |
| Git 仓库克隆 | 网络 I/O 超时不可控 | 非事务 + 结果回写 |
| 报表导出 | CPU 密集型 + 文件 I/O | 异步生成 + 状态更新 |
| 通知推送 | 推送失败不应回滚主业务 | 事件驱动异步推送 |

### 15.4 事务隔离级别

| 级别 | 使用场景 |
|---|---|
| `READ_COMMITTED`（默认） | 95% 的业务场景 |
| `REPEATABLE_READ` | 教师评分发布（确保读取的 AI 分析结果在事务期间不被修改） |

### 15.5 禁止事项

- 禁止在事务中调用外部 API（LLM、MinIO、Git、邮件、短信）
- 禁止在事务中执行文件 I/O
- 禁止在事务中发送消息/通知
- 禁止使用 `PROPAGATION_REQUIRES_NEW` 而不理解其含义
- 禁止在 Controller 层开启事务
- 禁止大事务（涉及超过 5 张表的操作应拆分）

---

## 16. Redis

### 16.1 缓存 Key 命名规范

Key 命名格式：`{业务域}:{实体类型}:{标识符}`

| 模式 | 示例 | 说明 |
|---|---|---|
| `user:info:{userId}` | `user:info:1234567890` | 用户信息缓存 |
| `user:perm:{userId}` | `user:perm:1234567890` | 用户权限缓存 |
| `token:access:{tokenId}` | `token:access:a1b2c3d4` | Access Token 存储 |
| `token:refresh:{userId}` | `token:refresh:1234567890` | Refresh Token 存储 |
| `token:blacklist:{tokenId}` | `token:blacklist:a1b2c3d4` | Token 黑名单 |
| `standard:{standardId}` | `standard:1234567890` | 评价标准缓存 |
| `course:{courseId}` | `course:1234567890` | 课程信息缓存 |
| `stats:dashboard:{role}` | `stats:dashboard:admin` | 仪表盘统计缓存 |
| `stats:progress:{taskId}` | `stats:progress:1234567890` | 任务进度缓存 |
| `report:{reportType}:{paramsHash}` | `report:class:a1b2` | 报表数据缓存 |
| `ai:status:{submissionId}` | `ai:status:1234567890` | AI 分析状态缓存 |
| `lock:{resourceKey}` | `lock:submission:1234567890` | 分布式锁 |
| `ratelimit:{userId}:{uri}` | `ratelimit:1234567890:login` | 接口限流计数器 |

### 16.2 TTL 策略

| 缓存类型 | TTL | 理由 |
|---|---|---|
| 用户信息 | 30 分钟 | 平衡数据一致性和查询性能 |
| 用户权限 | 30 分钟 | 权限变更不频繁 |
| Access Token | 2 小时 | 安全要求 |
| Refresh Token | 7 天 | 用户体验 |
| Token 黑名单 | Token 剩余有效时间 | 精确控制 |
| 评价标准 | 1 小时 | 标准修改不频繁 |
| 课程信息 | 1 小时 | 课程修改不频繁 |
| 仪表盘统计 | 5 分钟 | 实时性要求，短 TTL 自然过期 |
| 任务进度 | 5 分钟 | 实时统计，短 TTL 自然过期 |
| 报表数据 | 30 分钟 | 报表生成耗时，缓存减轻 DB 压力 |
| AI 分析状态 | 1 小时 | 分析过程可长可短，按需更新 |
| 分布式锁 | 30 秒 | 防止死锁 |
| 限流计数器 | 等于时间窗口 | 精确限流 |

### 16.3 缓存更新策略

| 策略 | 适用场景 | 实现 |
|---|---|---|
| **主动删除**（Cache-Aside） | 数据修改时 | Service 更新 DB 后调用 `redisTemplate.delete(key)` |
| **自然过期** | 统计类数据 | 设置 TTL，到期自动清除 |
| **批量失效** | 报表缓存 | 成绩发布时按 `report:*` 模式批量删除 |
| **永不过期 + 主动更新** | 极少但重要的场景 | 仅在系统配置变更时更新 |
| **写穿透**（Write-Through） | 不适用 MVP | 暂不使用 |

### 16.4 序列化规范

- Key 使用 `StringRedisSerializer`
- Value 使用 `GenericJackson2JsonRedisSerializer`（支持 JSON 序列化与反序列化，带类型信息）
- Hash Key 使用 `StringRedisSerializer`
- Hash Value 使用 `GenericJackson2JsonRedisSerializer`

### 16.5 缓存穿透/击穿/雪崩防护

| 问题 | 防护措施 |
|---|---|
| **缓存穿透**（查询不存在的数据） | 空值缓存（TTL 1 分钟）；布隆过滤器（P1） |
| **缓存击穿**（热点 Key 过期） | 分布式锁保证只有一个线程回源查 DB，其他线程等待 |
| **缓存雪崩**（大量 Key 同时过期） | TTL 加随机偏移（基础 TTL ± 10%） |

### 16.6 分布式锁

- 使用 Redis `SET key value NX EX timeout` 原子命令
- 锁的 Value 使用 UUID，释放时校验 Value 是否匹配（防止误删其他线程的锁）
- 锁超时时间设为 30 秒（短于业务超时时间）
- 使用 `redisson` 或 Hutool `RedisLock` 封装（不手写加锁/释放逻辑）

### 16.7 禁止事项

- 禁止在 Key 中使用特殊字符（空格、换行、中文）
- 禁止 Key 过长（超过 128 字符）
- 禁止不设 TTL（防止内存泄漏）
- 禁止在 Redis 中存储大对象（单 Value 超过 1MB）
- 禁止在 Redis 中存储密码、Token 明文以外的敏感信息

---

## 17. AI Service

### 17.1 Prompt 规范

- 所有 Prompt 模板集中管理在 `ai/provider/prompt/` 目录下的 YAML 或 properties 文件中
- Prompt 模板使用占位符（`{submissionContent}`、`{standardJson}`）动态填充
- Prompt 模板内容通过 `@ConfigurationProperties` 加载为配置 Bean
- 禁止在 Java 代码中硬编码 Prompt 文本（超过 2 行的 Prompt 必须抽取到模板文件）
- 每次 Prompt 模板变更需经过评审（记录的 Prompt 历史版本）

### 17.2 Token 管理

- AI 调用前估算输入 Token 数（`AIAnalysisProvider.estimateTokens(String text)`）
- 如果输入 Token 超过模型上下文窗口 80%，采取以下策略之一：
  - 截断输入（代码文件优先截断，评价标准完整保留）
  - 分批分析（将提交拆分为多个批次，每批次独立分析后合并结果）
- 每次调用记录实际 Token 用量到 `ai_analysis` 表（`token_input`、`token_output`、`token_total`）
- 定期统计 Token 消耗，评估成本

### 17.3 Retry 策略

- AI API 调用失败自动重试，最多 3 次（记录 `retry_count`）
- 重试间隔：指数退避（1s → 2s → 4s）
- 可重试错误：网络超时、服务端 5xx、速率限制 429
- 不可重试错误：认证失败 401、权限不足 403、参数错误 400
- 超过重试次数后标记 `analysis_status = FAILED`，记录 `error_message`
- 前端提供"手动重新分析"按钮触发重试

### 17.4 Timeout 配置

- AI API 调用超时：120 秒（`b1.ai.{provider}.timeout` 配置项）
- 超时后抛出 `AIException(ErrorCode.AI_ANALYSIS_TIMEOUT)`
- 超时不阻塞主流程（AI 分析异步执行）

### 17.5 日志规范

- AI 调用入口日志（INFO）：`log.info("开始 AI 分析: submissionId={}, provider={}, model={}", submissionId, provider, model)`
- AI 调用完成日志（INFO）：`log.info("AI 分析完成: submissionId={}, 耗时={}ms, token={}", submissionId, durationMs, tokenTotal)`
- AI 调用失败日志（ERROR）：`log.error("AI 分析失败: submissionId={}, 重试次数={}", submissionId, retryCount, exception)`
- AI 调用详细日志（DEBUG 级别，生产关闭）：Request Prompt（截断至 500 字符）、Response（截断至 500 字符）

### 17.6 Provider 切换

- 通过配置 `b1.ai.provider` 切换 LLM 厂商（deepseek / openai-compat / custom）
- 切换无需修改业务代码（依赖 `AIAnalysisProvider` 接口而非具体实现）
- 支持运行时通过 System Config 动态切换 Provider（P1）

### 17.7 幻觉防控

- LLM 输出通过 JSON Schema 约束格式（`chatWithJsonSchema` 方法）
- JSON Schema 校验失败时记录错误并重试（最多 1 次额外重试）
- 仍然失败则返回部分结果（静态分析 + 规则引擎的结果），标记置信度为 0
- 低置信度项（confidence < 0.5）在教师复核界面突出标记

### 17.8 禁止事项

- 禁止在 AI Prompt 中包含用户密码、Token 等敏感信息
- 禁止在 AI Response 未校验的情况下直接写入数据库
- 禁止在生产环境打印完整 Prompt 和 Response（超过 500 字符截断）
- 禁止 AI 直接输出最终成绩（必须经教师确认）
- 禁止 LLM 调用阻塞用户请求（必须异步处理）

---

## 18. Git Service

### 18.1 JGit 规范

- Git 操作使用 JGit 库，禁止通过 `Runtime.exec()` 调用系统 Git 命令行
- Git 操作封装在 `module/git/service/GitService.java` 中
- 所有 Git 操作在工作线程池中执行（不阻塞主请求线程）

### 18.2 Clone 规范

- 克隆目标目录：`{b1.git.clone-dir}/{userId}/{submissionId}/`（临时目录，分析完成后清理）
- 克隆超时：60 秒（`b1.git.clone-timeout` 配置项）
- 克隆前检查磁盘空间（至少保留 1GB 可用空间）
- 克隆支持认证：通过 `accessToken` 访问私有仓库（HTTPS + Token 方式）
- 克隆失败时清理已下载的部分文件

### 18.3 Git URL 验证

- 提交时验证 Git URL 格式（必须是合法的 HTTPS Git URL）
- 验证 Git URL 可达性（尝试 `ls-remote`，不完整克隆）
- 验证指定分支是否存在
- 验证结果（仓库名、默认分支、分支列表、最新提交信息）返回给前端

### 18.4 异常处理

- Git 操作异常全部转换为 `GitException`（ErrorCode 8000-8999）
- 常见异常映射：
  - 仓库不存在 → `GIT_REPO_NOT_FOUND(8001)`
  - 无访问权限 → `GIT_NO_PERMISSION(8002)`
  - 克隆失败 → `GIT_CLONE_FAILED(8003)`
  - 分支不存在 → `GIT_BRANCH_NOT_FOUND(8004)`
  - 克隆超时 → `GIT_CLONE_FAILED(8003)` + "克隆超时"
- Git 异常详细信息不暴露给前端（仅显示通用错误信息，防止泄露仓库结构）

### 18.5 安全规范

- 不存储用户的 Git 密码（仅支持 Token 方式，Token 不在服务端持久化）
- 克隆的代码文件在分析完成后立即删除（默认 1 小时内清理定时任务）
- Git 克隆临时目录权限设置为 700（仅当前用户可访问）

### 18.6 禁止事项

- 禁止使用 `Runtime.exec()` 调用系统命令
- 禁止在应用服务器上持久化存储克隆的代码仓库
- 禁止将 Git 仓库 URL 输出到日志（可能包含 Token 信息）
- 禁止 Git 操作阻塞 HTTP 请求线程（必须异步）
- 禁止在事务中执行 Git 操作

---

## 19. File Upload

### 19.1 MinIO 规范

- 文件上传/下载通过 `FileStorageProvider` 接口操作
- MVP 阶段使用 MinIO 实现（`MinioStorageProvider`）
- 文件操作通过 MinIO Java Client SDK，禁止通过 HTTP 手动构造请求

### 19.2 文件命名规范

- MinIO 对象 Key 格式：`{bucket}/{businessType}/{entityId}/{timestamp}_{uuid}.{ext}`
- 示例：`submissions/task/12345/20260704120000_a1b2c3d4.zip`
- 不保留原始文件名作为存储路径（防止特殊字符和冲突），原始文件名存储在 `file_storage.original_name` 中

### 19.3 存储桶（Bucket）设计

| Bucket | 用途 | 访问权限 | 生命周期 |
|---|---|---|---|
| `submissions` | 学生提交的实训成果文件 | 私有（通过后端接口中转下载） | 永久保留 |
| `reports` | 报表导出文件（PDF/Excel） | 私有 | 90 天过期自动删除 |
| `avatars` | 用户头像 | 私有 | 永久保留 |
| `knowledge` | 知识库文档（P1） | 私有 | 永久保留 |
| `temp` | 临时文件 | 私有 | 24 小时过期自动删除 |

### 19.4 安全校验规范

| 校验项 | 规则 |
|---|---|
| **文件大小** | 上限 50MB（`b1.upload.max-size` 配置项），超过拒绝上传 |
| **文件类型** | 白名单制：`zip, pdf, doc, docx, xls, xlsx, java, py, c, cpp, txt, md, png, jpg, jpeg`（`b1.upload.allowed-types` 配置项） |
| **扩展名校验** | 提取文件扩展名，匹配白名单 |
| **魔数校验** | 读取文件头字节（Magic Number），验证扩展名与真实文件类型是否一致（防止伪造扩展名） |
| **压缩炸弹防护** | 解压前检查 ZIP 文件总解压后大小，超过源文件 100 倍时拒绝 |
| **路径穿越防护** | 解压 ZIP 时校验每个条目的路径，防止 `../../etc/passwd` 路径穿越攻击 |
| **文件名清洗** | 移除文件名中的特殊字符（`/ \ : * ? " < > \|`） |

### 19.5 文件访问控制

- 文件下载/预览通过后端接口中转（不暴露 MinIO 直接访问 URL）
- 后端接口校验：当前用户是否有权访问该文件（学生只能访问自己的提交文件，教师只能访问所授班级的文件）
- 预签名 URL 有效期 15 分钟（临时授权，过期自动失效）

### 19.6 禁止事项

- 禁止将 MinIO 直接暴露给前端（前端不持有 MinIO 的直接访问凭证）
- 禁止使用原始文件名作为存储路径
- 禁止上传可执行文件（`.exe`, `.sh`, `.bat`, `.jar`, `.war`）
- 禁止跳过魔数校验只校验扩展名
- 禁止在校验完成前将文件写入永久存储（先写临时目录，校验通过后移动到 MinIO）

---

## 20. Code Style

### 20.1 命名规范

| 元素 | 规则 | 示例 |
|---|---|---|
| 包名 | 全小写，单数，点分隔 | `com.b1.module.course.controller` |
| 类名 | PascalCase（首字母大写驼峰） | `AuthController`, `UserServiceImpl` |
| 接口名 | PascalCase | `AIAnalysisProvider`, `UserService` |
| 方法名 | camelCase（首字母小写驼峰） | `findUserByUsername`, `createTask` |
| 变量名 | camelCase | `userId`, `submissionList` |
| 常量名 | UPPER_SNAKE_CASE（全大写下划线） | `MAX_UPLOAD_SIZE`, `DEFAULT_PAGE_SIZE` |
| 枚举值 | UPPER_SNAKE_CASE | `PENDING`, `COMPLETED`, `FAILED` |
| 数据库字段 | lower_snake_case | `create_time`, `training_task_id` |
| URL 路径 | kebab-case | `/api/v1/teacher/tasks/:taskId` |

### 20.2 方法长度

- 方法体不超过 50 行（不含空行和注释行）
- 超过 50 行必须拆分为更小的私有方法
- 私有方法按调用顺序排列在调用者之后

### 20.3 类长度

- Controller 类不超过 200 行
- Service 实现类不超过 300 行
- Mapper 接口不超过 50 行
- Entity / DTO / VO 类无行数上限（纯数据定义）

### 20.4 参数数量

- 方法参数不超过 4 个
- 超过 4 个参数必须封装为 DTO 对象
- Boolean 参数禁止：不使用 `boolean flag` 作为参数（难以理解含义），改用枚举或拆分为两个方法

### 20.5 注释规范

- **必须注释**：非显而易见的业务规则、特殊处理的原因、已知限制或 workaround
- **不注释**：代码本身已清晰表达的"做什么"（如 `// 保存用户` 在 `userMapper.insert(user)` 上方）
- 类级别 JavaDoc：描述类的职责和主要依赖
- 接口方法 JavaDoc：描述方法的契约（输入、输出、异常）
- 不注释的代码直接删除（Git 历史可恢复），禁用注释块包裹的"备用代码"

### 20.6 Magic Number / Magic String

- 禁止在代码中使用魔数（直接出现的数字字面量如 `5`、`3`、`30`）
- 所有数字常量提取为命名常量或枚举
- 配置型数字提取到 `application.yml` 中（如超时时间、大小限制）
- 业务相关字符串提取到 `common/constant/` 常量类中（如 `RedisKeys`、`SystemConstants`）

### 20.7 Optional 使用规范

- 返回值可能为空时使用 `Optional<T>` 包装（如 `findByUsername` 返回 `Optional<User>`）
- 禁止将 `Optional` 用作方法参数
- 禁止将 `Optional` 用作类的字段
- 禁止 `Optional.get()` 不经过 `isPresent()` 检查
- 推荐使用 `orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND))` 链式调用

### 20.8 Stream / Lambda 规范

- 集合操作优先使用 Stream API（替代传统的 for 循环）
- Lambda 表达式超过 3 行时，抽取为具名方法
- Stream 链式调用每行一个操作（提高可读性）：
  ```java
  list.stream()
      .filter(Objects::nonNull)
      .map(this::convert)
      .sorted(Comparator.comparing(Entity::getCreateTime).reversed())
      .toList();
  ```
- 禁止在 Stream 中修改外部变量（副作用）
- 禁止在 Lambda 中抛出 checked Exception
- 禁止嵌套 Stream 超过 2 层

### 20.9 Lombok 使用规范

| 注解 | 使用场景 | 禁止场景 |
|---|---|---|
| `@Data` | Entity、DTO、VO、ConfigProperties | 需要自定义 equals/hashCode 的类 |
| `@Slf4j` | Service、Controller、Util | — |
| `@Builder` | 构造复杂对象（如测试数据、配置对象） | — |
| `@RequiredArgsConstructor` | Service 依赖注入 | 超过 5 个 final 字段时转为 `@AllArgsConstructor` |
| `@AllArgsConstructor` | DTO、VO（配合 @Builder） | — |
| `@NoArgsConstructor` | DTO、VO（JSON 反序列化需要） | — |
| `@Getter` / `@Setter` | 仅部分字段需要时单独注解 | 能使用 @Data 时不单独注解 |

### 20.10 集合返回值

- 查询列表方法绝不返回 null，空列表返回 `Collections.emptyList()` 或 `List.of()`
- 查询单个对象方法返回 `Optional<T>` 包装
- 禁止返回未初始化的集合字段

---

## 21. Testing

### 21.1 单元测试

- 测试框架：JUnit 5（Jupiter）+ Mockito
- 测试类命名：`{被测类名}Test.java`
- 测试类位置：`src/test/java/com/b1/`，镜像源代码包结构
- Service 层必须编写单元测试（Mock Mapper 和外部 Provider）
- Controller 层使用 `@WebMvcTest` + `MockMvc` 编写切片测试

### 21.2 测试覆盖率目标

| 层级 | 目标覆盖率 | 说明 |
|---|---|---|
| Service 层 | 80%+ 行覆盖率 | 核心业务逻辑必须全覆盖 |
| Controller 层 | 60%+ 行覆盖率 | 接口参数校验和异常处理路径 |
| Util 工具类 | 90%+ 行覆盖率 | 纯函数，易测试 |
| Mapper 层 | 通过集成测试覆盖 | 不单独测（依赖数据库） |

### 21.3 Mock 规范

- Service 单元测试中 Mock 所有外部依赖（Mapper、其他 Service、Provider）
- 使用 `@Mock` 创建 Mock 对象，`@InjectMocks` 注入被测对象
- Mock 返回值使用 `when(...).thenReturn(...)` 明确指定
- 禁止 Mock 被测对象本身
- 禁止在单元测试中启动 Spring 容器（不使用 `@SpringBootTest`）

### 21.4 集成测试

- 数据库相关测试使用 `@DataJpaTest` 或 `@MybatisPlusTest` + H2 内存数据库
- 关键业务流程编写端到端集成测试（如：登录 → 创建提交 → AI 分析 → 教师复核 → 成绩发布）
- 集成测试类命名：`{业务流程}FlowTest.java`
- 集成测试使用 `@SpringBootTest` + Testcontainers（MySQL + Redis + MinIO）或 H2 + 嵌入式 Redis

### 21.5 测试数据结构

- 测试数据使用 Builder 模式构造（Lombok `@Builder`）
- 复杂测试数据抽取为测试 Fixture 工厂方法（`createTestUser()`）
- 禁止测试方法之间共享可变状态（每个测试方法独立准备数据）

### 21.6 命名规范

测试方法命名：`{方法名}_{场景}_{预期结果}`

- 示例：`login_WithCorrectPassword_ReturnsToken`
- 示例：`login_WithWrongPassword_ThrowsAuthException`
- 示例：`listTasks_WhenEmpty_ReturnsEmptyPage`

### 21.7 禁止事项

- 禁止跳过测试提交代码
- 禁止编写依赖执行顺序的测试（各测试方法必须完全独立）
- 禁止在测试中连接真实的外部服务（生产数据库、生产 AI API）
- 禁止测试方法无断言（无断言的测试等于没有测试）
- 禁止使用 `Thread.sleep()` 等待异步操作（使用 `Awaitility` 或 `CountDownLatch`）

---

## 22. Code Review Checklist

### 22.1 Controller 检查

| # | 检查项 | 说明 |
|---|---|---|
| C-1 | 类标注 `@RestController` + `@RequestMapping` + `@Tag` | Knife4j 文档注解完整 |
| C-2 | 方法标注 `@Operation` + HTTP 方法注解 + 权限注解 | GET/POST/PUT/PATCH/DELETE 语义正确 |
| C-3 | 请求参数使用 `@Valid` / `@Validated` | 输入校验不可少 |
| C-4 | 返回值为 `Result<T>` 或 `PageResult<T>` | 统一响应格式 |
| C-5 | 不含业务逻辑 | 仅参数绑定、Service 调用、Result 包装 |
| C-6 | 不直接调用 Mapper | 必须通过 Service |
| C-7 | 不捕获异常 | 交给 GlobalExceptionHandler |
| C-8 | URL 路径使用 kebab-case | 全小写 + 连字符 |

### 22.2 Service 检查

| # | 检查项 | 说明 |
|---|---|---|
| S-1 | 接口 + 实现分离 | 接口在 service/，实现在 service/impl/ |
| S-2 | 写操作标注 `@Transactional` | 事务范围合理 |
| S-3 | 不在事务中调用外部 API | LLM、MinIO、Git、HTTP |
| S-4 | 异常使用 `BusinessException` | 不使用 RuntimeException 或自定义异常 |
| S-5 | 日志记录关键节点 | INFO 级别记录登录、创建、发布等操作 |
| S-6 | 不返回 null | 空列表返回 `Collections.emptyList()` |
| S-7 | 不处理 HTTP 请求/响应 | 不注入 HttpServletRequest |
| S-8 | 通过构造函数注入依赖 | 不使用 @Autowired 字段注入 |

### 22.3 Mapper 检查

| # | 检查项 | 说明 |
|---|---|---|
| M-1 | 继承 `BaseMapper<Entity>` | 基础 CRUD 自动获得 |
| M-2 | 标注 `@Mapper` | MyBatis 自动扫描 |
| M-3 | 复杂查询写在 XML 中 | 多表 JOIN、聚合统计 |
| M-4 | `#{}` 预编译参数 | 禁止 `${}` 拼接用户输入 |
| M-5 | 分页查询使用 `IPage<T>` | MyBatis Plus 分页插件 |
| M-6 | 不包含业务逻辑 | 纯数据访问 |
| M-7 | 不调用 Service | 单向依赖 Service → Mapper |

### 22.4 DTO 检查

| # | 检查项 | 说明 |
|---|---|---|
| D-1 | 使用 Lombok `@Data` | 消样板代码 |
| D-2 | 字段标注 Jakarta Validation | `@NotNull`、`@NotBlank`、`@Size` 等 |
| D-3 | 字段标注 `@Schema` | 生成 API 文档 |
| D-4 | 不使用 Entity 接收请求 | Entity 不能直接作为 Controller 参数 |
| D-5 | 字段类型正确 | 日期 String、数值包装类、枚举 String |
| D-6 | 不含数据库映射注解 | DTO 与 Entity 完全独立 |

### 22.5 VO 检查

| # | 检查项 | 说明 |
|---|---|---|
| V-1 | 使用 Lombok `@Data` | 消样板代码 |
| V-2 | 字段标注 `@Schema` | 生成 API 文档 |
| V-3 | 不返回密码字段 | password 永远不在 VO 中出现 |
| V-4 | 不返回 Token | Token 通过 Header 传输，不放在 Body |
| V-5 | 日期字段为 String | 使用 ISO 8601 格式 |
| V-6 | 不直接返回 Entity | Entity 不能直接作为 Controller 返回值 |

### 22.6 Entity 检查

| # | 检查项 | 说明 |
|---|---|---|
| E-1 | 使用 Lombok `@Data` | 消样板代码 |
| E-2 | 标注 `@TableName` + `@TableId` + `@TableLogic` | MyBatis Plus 映射 |
| E-3 | 主键策略 `IdType.ASSIGN_ID` | Snowflake 分布式 ID |
| E-4 | 审计字段配置自动填充 | `@TableField(fill = ...)` |
| E-5 | 时间字段使用 `LocalDateTime` | 不使用 Date、String |
| E-6 | 金额/分数字段使用 `BigDecimal` | 不使用 Float、Double |
| E-7 | 枚举字段使用 String | 不使用 MySQL ENUM 或 TINYINT |
| E-8 | 不包含业务逻辑 | Entity 是纯数据载体 |
| E-9 | 不包含 JSON 序列化注解 | 序列化由 VO 负责 |

### 22.7 Transaction 检查

| # | 检查项 | 说明 |
|---|---|---|
| T-1 | `@Transactional` 在 Service 实现上 | 不在接口上或 Controller 上 |
| T-2 | 事务范围最小化 | 仅包裹必须保证原子性的 DB 操作 |
| T-3 | 不含外部 API 调用 | LLM、MinIO、Git、邮件 |
| T-4 | 不含文件 I/O | 读文件、写文件 |
| T-5 | 不含消息/通知发送 | 发送事件或消息不放在事务内 |
| T-6 | 超时设置合理 | 默认 30s，长业务适当调整 |
| T-7 | 回滚策略明确 | RuntimeException 自动回滚 |

### 22.8 Exception 检查

| # | 检查项 | 说明 |
|---|---|---|
| EX-1 | 使用 `BusinessException` + `ErrorCode` | 不抛裸 RuntimeException |
| EX-2 | ErrorCode 不重复 | 全局唯一错误码 |
| EX-3 | ErrorCode 语义清晰 | 中文 message 面向用户 |
| EX-4 | 异常被 GlobalExceptionHandler 处理 | 不被中间层吞掉 |
| EX-5 | 异常信息不含敏感数据 | 不泄露密码、Token、文件路径 |
| EX-6 | 不在 Controller 中 try-catch | 交给异常处理器 |

### 22.9 Performance 检查

| # | 检查项 | 说明 |
|---|---|---|
| P-1 | 列表查询强制分页 | pageSize 上限 100 |
| P-2 | 高频查询数据使用缓存 | Redis 缓存热数据 |
| P-3 | N+1 查询已避免 | 使用批量查询或 JOIN 替代循环查询 |
| P-4 | 大数据量导出异步处理 | 报表生成不阻塞 HTTP 请求 |
| P-5 | 数据库连接及时释放 | 事务不长时间持有连接 |
| P-6 | 慢查询 SQL 已优化 | 核心查询使用索引、EXPLAIN 验证 |

### 22.10 Security 检查

| # | 检查项 | 说明 |
|---|---|---|
| SE-1 | 密码 BCrypt 加密存储 | 强度 10，不可逆 |
| SE-2 | SQL 使用 `#{}` 预编译 | 不使用 `${}` 拼接用户输入 |
| SE-3 | 文件上传校验类型和大小 | 白名单 + 魔数 + 大小限制 |
| SE-4 | API 权限注解完整 | `@SaCheckRole` 或 `@SaCheckPermission` 控制 |
| SE-5 | 数据隔离在 Service 层实现 | 学生只能访问自己的数据 |
| SE-6 | 敏感字段不返回前端 | password、Token、内部 ID |
| SE-7 | 日志不记录敏感信息 | password、Token、完整手机号、API Key |
| SE-8 | Token 使用 UUID 格式 | 不可推测、不可伪造 |

---

## Appendix A: Cross-Document Consistency Matrix

| 本规范章节 | 关联文档 | 关联内容 | 一致性 |
|---|---|---|---|
| 3. Directory Specification | Backend Architecture §4 | 目录结构和模块划分 | ✅ 一致 |
| 4. Controller Specification | Backend Architecture §5.2.1 | Controller Layer 设计 | ✅ 一致 |
| 5. Service Specification | Backend Architecture §5.2.2 | Service Layer 设计 | ✅ 一致 |
| 6. Mapper Specification | Backend Architecture §5.2.3 | Mapper Layer 设计 | ✅ 一致 |
| 7. DTO Specification | Backend Architecture §5.2.5 | DTO / VO 设计 | ✅ 一致 |
| 8. VO Specification | Backend Architecture §5.2.5 | DTO / VO 设计 | ✅ 一致 |
| 9. Entity Specification | Database Design §2.2、§2.3、§2.4 | 字段规范、逻辑删除、乐观锁 | ✅ 一致 |
| 11. Exception Specification | Backend Architecture §9 | 异常体系设计 | ✅ 一致 |
| 12. Result Specification | Backend Architecture §9.4 | Result 响应体设计 | ✅ 一致 |
| 12. Result Specification | API Mock Specification §3 | 统一响应格式 | ✅ 一致 |
| 15. Transaction | Database Design §6 | 事务设计 | ✅ 一致 |
| 16. Redis | Backend Architecture §13.1 | 缓存策略 | ✅ 一致 |
| 17. AI Service | Backend Architecture §6.2.6 | AI Analysis Module 设计 | ✅ 一致 |
| 18. Git Service | Backend Architecture §6.2.12 | Git Module 设计 | ✅ 一致 |
| 19. File Upload | Backend Architecture §6.2.11 | File Module 设计 | ✅ 一致 |
| 19. File Upload | Database Design §7.3 | 文件安全 | ✅ 一致 |
| 20. Code Style | Frontend Specification §22 | 代码规范（命名约定对齐） | ✅ 一致 |
| 21. Testing | Definition of Done §A.5 | 代码质量检查 | ✅ 一致 |
| 22. Code Review Checklist | Definition of Done §Part A | 页面完成标准 | ✅ 一致 |

---

## Appendix B: Technology Stack Version List

| 技术 | 版本 | 用途 | 必选/可选 |
|---|---|---|---|
| Java | 21 (LTS) | 运行环境 | 必选 |
| Spring Boot | 3.x | 应用框架 | 必选 |
| MyBatis Plus | 3.5.x | ORM / CRUD 增强 | 必选 |
| MySQL | 8.0 | 主数据库 | 必选 |
| Redis | 7.x | 缓存 / Token 存储 / 分布式锁 | 必选 |
| Sa-Token | 1.38+ | 认证与权限框架 | 必选 |
| Knife4j | 4.x | OpenAPI 3.0 文档自动生成 | 必选 |
| Lombok | 1.18.x | 样板代码消除 | 必选 |
| MapStruct | 1.5.x | Entity ↔ VO 对象映射 | 必选 |
| Spring Validation | (内置) | 参数校验 | 必选 |
| Hutool | 5.8.x | 通用工具集 | 必选 |
| JGit | 6.x | Git 仓库操作 | 必选 |
| MinIO Client | 8.x | 对象存储客户端 | 必选 |
| Apache POI | 5.x | Excel 报表生成 | 必选 |
| iText / OpenPDF | (最新) | PDF 报表生成 | 必选 |
| Flyway | 9.x | 数据库版本迁移 | 必选 |
| Maven | 3.9+ | 构建与依赖管理 | 必选 |
| Docker | 24+ | 容器化部署 | 必选 |
| JUnit 5 | (内置) | 单元测试框架 | 必选 |
| Mockito | 5.x | Mock 测试框架 | 必选 |

---

*本后端开发规范基于 PRD v2.0、SDS v1.0、Backend Architecture Design v1.0、Database Design v1.0、API Mock Specification v1.0、Frontend Specification v1.0、FIP v1.0、ADR-001~011、Definition of Done v1.0、MVP 编写。所有规范条款与已有文档保持一致。本规范是后端唯一编码标准，Claude、Codex、所有开发人员必须严格遵守。*
