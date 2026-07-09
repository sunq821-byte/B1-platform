# Database Design v1.0

## 基于大模型的软件实训教学检查评价与报表系统

---

## 1. Document Information

| 字段 | 值 |
|---|---|
| **文档名称** | Database Design |
| **文档版本** | v1.0 |
| **文档状态** | Formal Release |
| **作者** | Senior Database Architect |
| **审核人** | TBD |
| **最后更新** | 2026-07-04 |
| **适用 Sprint** | Sprint 1（基础设施） → Sprint 7（上线） |
| **前置文档** | PRD v2.0, SDS v1.0, Backend Architecture Design v1.0, API Mock Specification v1.0, MVP, ADR-001 ~ ADR-011 |
| **关联文档** | Backend Architecture Design v1.0（模块定义、数据流）, SDS v1.0（ER 图、表结构初稿） |
| **适用范围** | 后端全部数据库开发工作 |

### Revision History

| 版本 | 日期 | 作者 | 变更说明 |
|---|---|---|---|
| v1.0 | 2026-07-04 | Senior Database Architect | 初始版本，覆盖 MVP 全部 26 张表 |

---

## 2. Database Design Principles

### 2.1 命名规范

#### 2.1.1 通用规则

| 规则 | 示例 | 说明 |
|---|---|---|
| **全部小写** | `training_task` | 数据库名、表名、字段名均使用小写字母 |
| **下划线分隔** | `standard_dimension` | 多个单词使用下划线 `_` 分隔（snake_case） |
| **禁止保留字** | 不使用 `order`、`group`、`select` 等 SQL 保留字 | 避免查询时需加反引号 |
| **禁止拼音** | 不使用 `xuehao`、`kecheng` 等拼音 | 全部英文命名 |
| **单数名词** | `user` 而非 `users` | 每行是一条记录，表名用单数 |
| **前缀统一** | 不使用 `tbl_`、`tb_` 等前缀 | 表名本身已足够区分 |

#### 2.1.2 表名分类

| 分类 | 命名模式 | 示例 |
|---|---|---|
| **主数据表** | 业务名词 | `user`, `course`, `class`, `role` |
| **关联表** | `{表A}_{表B}` | `user_role`, `course_teacher`, `training_class` |
| **事务表** | 业务名词 | `submission`, `teacher_review`, `ai_analysis` |
| **明细表** | `{父表}_detail` 或 `{父表}_item` | `ai_analysis_detail`, `review_item` |
| **配置表** | `{模块}_config` | `system_config` |
| **日志表** | `{模块}_log` | `operation_log` |
| **快照表** | `{模块}_snapshot` | `statistics_snapshot` |
| **存储表** | `{对象}_storage` | `file_storage` |

#### 2.1.3 字段名分类

| 分类 | 命名模式 | 示例 |
|---|---|---|
| **主键** | `id` | `id BIGINT` |
| **外键** | `{关联表}_id` | `user_id`, `course_id`, `training_task_id` |
| **业务字段** | 描述性名词 | `real_name`, `submission_type`, `due_date` |
| **状态字段** | `status` 或 `{前缀}_status` | `status`, `analysis_status` |
| **布尔字段** | `is_{描述}` | `is_late`, `is_adopted` |
| **时间字段** | `{动作}_time` | `create_time`, `submit_time`, `review_time` |
| **审计字段** | `create_by`, `update_by` | 记录操作人 ID |
| **金额/分数字段** | `{名称}` + 精度后缀 | `total_score DECIMAL(5,2)` |
| **计数/百分比** | `{名称}_count`, `{名称}_rate` | `submit_count`, `completion_rate` |

### 2.2 字段规范

#### 2.2.1 主键策略

| 规则 | 值 |
|---|---|
| **主键名称** | 统一使用 `id` |
| **主键类型** | `BIGINT` |
| **ID 生成算法** | Snowflake（Twitter 开源的分布式 ID 算法） |
| **生成方式** | MyBatis Plus `@TableId(type = IdType.ASSIGN_ID)` |
| **ID 特性** | 全局唯一、趋势递增（非严格连续）、含时间戳信息 |

**选型理由**：

Snowflake 生成的 64 位（8 字节）ID 由以下部分组成：

- 1 位符号位（始终为 0）
- 41 位时间戳（毫秒级，可用约 69 年）
- 10 位工作机器 ID（支持 1024 个节点）
- 12 位序列号（单节点每毫秒支持 4096 个 ID）

优势：
1. **全局唯一**：分布式环境下无需协调即可生成不重复的 ID
2. **高性能**：纯内存生成，不需要数据库自增锁或 Redis 原子操作
3. **趋势递增**：ID 随时间增长，对 MySQL InnoDB 的 B+Tree 索引写入友好（减少页分裂）
4. **含时间信息**：可从 ID 反解出生成时间，无需额外字段记录创建时间
5. **安全**：非连续 ID，外部无法通过 ID 推测数据规模

对比方案：

| 方案 | 优点 | 缺点 | 选择 |
|---|---|---|---|
| **数据库自增 (AUTO_INCREMENT)** | 简单，天然递增 | 分布式冲突、迁移困难、暴露数据规模 | ❌ |
| **UUID** | 真正全局唯一，无中心化 | 128 位长，字符串存储效率低，随机无序导致索引频繁页分裂 | ❌ |
| **Snowflake** | 64 位数字，趋势递增，高性能 | 依赖机器时钟（时钟回拨需处理） | ✅ |
| **Leaf（美团开源）** | 高可用，双号段缓冲 | 引入额外组件（ZooKeeper），MVP 过度设计 | ❌ P1 |
| **Redis INCR** | 简单，严格递增 | 依赖 Redis 可用性，单点故障后 ID 可能重复 | ❌ |

#### 2.2.2 时间字段

| 字段 | 类型 | 说明 |
|---|---|---|
| `create_time` | `DATETIME` | 记录创建时间，MyBatis Plus 自动填充 |
| `update_time` | `DATETIME` | 记录最后修改时间，MyBatis Plus 自动填充 |
| 业务时间字段 | `DATETIME` | 如 `submit_time`、`review_time`、`publish_time`、`due_date` |

**时间类型选型**：

| 类型 | 范围 | 存储 | 时区 | 选择 |
|---|---|---|---|---|
| `DATETIME` | '1000-01-01' ~ '9999-12-31' | 8 字节（5 字节 MySQL 5.6.4+） | 无时区信息 | ✅ |
| `TIMESTAMP` | '1970-01-01' ~ '2038-01-19' | 4 字节 | 自动转换为 UTC | ❌（2038 问题） |
| `BIGINT`（毫秒时间戳） | 极大范围 | 8 字节 | N/A | ❌（不可读，调试困难） |

选择 `DATETIME` 的理由：范围足够（不受 2038 问题影响），可读性好（SELECT 直接看到日期），存储空间在 MySQL 5.6.4+ 已优化为 5 字节。所有时间存储以应用层统一时区（Asia/Shanghai, UTC+8）为准，避免时区转换问题。

#### 2.2.3 枚举字段

| 规则 | 值 |
|---|---|
| **存储类型** | `VARCHAR(32)` 存储枚举名称（如 `PENDING`、`COMPLETED`） |
| **禁止行为** | 不使用 MySQL 原生 `ENUM` 类型 |
| **禁止行为** | 不使用 `TINYINT` 存储枚举（如 `0=待提交 1=已提交`） |

**禁止 MySQL ENUM 的理由**：
1. ENUM 值添加/修改需要 `ALTER TABLE`，大表可能锁表
2. ENUM 排序行为与直觉不一致（按索引排序而非字符串）
3. 不同 RDBMS 对 ENUM 实现差异大，迁移困难
4. VARCHAR 存储枚举名称可以直接查询时（包括后端日志）直接理解数据含义，不需要查映射表

#### 2.2.4 字符集与排序规则

| 配置项 | 值 | 理由 |
|---|---|---|
| 字符集 | `utf8mb4` | 支持完整 Unicode（含 emoji、生僻汉字） |
| 排序规则 | `utf8mb4_unicode_ci` | 大小写不敏感，中文排序合理 |
| 单表默认 | 继承数据库 | 无需逐字段指定 |

**禁止 `utf8`（MySQL alias）**：MySQL 的 `utf8` 实际上是 `utf8mb3`，只支持 3 字节 Unicode（BMP 平面），不支持 emoji（🎉）和部分 CJK 扩展字符。使用 `utf8mb4` 是唯一正确选项。

#### 2.2.5 金额 / 分数字段

| 规则 | 值 |
|---|---|
| **类型** | `DECIMAL(M, D)`，其中 M = 总位数，D = 小数位数 |
| **分数示例** | `DECIMAL(5,2)` — 支持 -999.99 到 999.99 |
| **权重示例** | `DECIMAL(5,2)` — 支持 0.00 到 100.00（百分比） |
| **禁止行为** | 不使用 `FLOAT` / `DOUBLE` 存储分数或比例 |

**禁止浮点数的理由**：浮点数存在精度问题（0.1 + 0.2 ≠ 0.3）。评分和权重涉及求和、百分比计算，任何精度偏差都会导致教师端的分数不一致。`DECIMAL` 提供定点精度，确保计算结果可复现。

#### 2.2.6 JSON 字段

| 规则 | 值 |
|---|---|
| **类型** | `JSON`（MySQL 8.0 原生类型） |
| **使用场景** | 结构化但 schema 不固定的数据（AI 分析原始结果、报表查询参数、通知额外数据） |
| **禁止行为** | 不将 JSON 用 TEXT 存储 |
| **禁止行为** | 不在 JSON 字段上建普通索引（应使用虚拟列 + 索引 或 Generated Column） |

MySQL 8.0 的 `JSON` 类型提供二进制存储（比 TEXT 更紧凑、更快），并支持 JSON 路径表达式（`JSON_EXTRACT`、`->`、`->>`）进行查询。

### 2.3 逻辑删除

| 规则 | 值 |
|---|---|
| **字段名** | `deleted` |
| **类型** | `TINYINT` |
| **默认值** | `0`（未删除） |
| **删除标记** | `1`（已删除） |
| **实现方式** | MyBatis Plus `@TableLogic` |
| **适用范围** | 所有主数据表（user、course、class、training_task、evaluation_standard 等） |
| **不适用范围** | 关联表（user_role、course_teacher 等）使用物理删除；日志表（operation_log）不可删除 |

**选型理由**：

业务数据不可物理删除是教育系统的合规要求。实训记录、成绩数据、操作日志可能作为教学评估、学术争议的审计证据，必须保留。逻辑删除保证数据不丢失，同时业务查询自动过滤已删除记录（MyBatis Plus 自动追加 `WHERE deleted = 0`）。

**物理删除白名单**（以下数据允许物理删除）：
- 关联表记录（`user_role`、`course_teacher`、`course_student`、`training_class`）：仅是关系解除，不涉及数据追溯
- 临时文件（`file_storage` 中 bucket='temp' 的记录）：24 小时过期后自动清理
- 测试数据（dev 环境）：不涉及合规要求

### 2.4 乐观锁

| 规则 | 值 |
|---|---|
| **字段名** | `version` |
| **类型** | `INT` |
| **默认值** | `0` |
| **更新策略** | 每次 UPDATE 时 `version = version + 1`，WHERE 条件校验 `version = 原值` |
| **实现方式** | MyBatis Plus `@Version` |
| **适用范围** | 写频繁且并发冲突风险高的表 |

**适用范围**：

| 表 | 是否需要 version | 理由 |
|---|---|---|
| `submission` | ✅ 是 | 学生可能同时在多个页面操作提交 |
| `ai_analysis` | ✅ 是 | AI 回调更新分析结果，可能与重试冲突 |
| `teacher_review` | ✅ 是 | 教师复核操作，多个教师不应同时修改同一份复核 |
| `score_record` | ✅ 是 | 最终成绩写入，防止并发覆盖 |
| `system_config` | ✅ 是 | 管理员修改系统参数时防止丢失更新 |
| `user` | 否 | 用户同一时间只有一个操作者在修改个人信息 |
| `course` | 否 | 课程修改频率低，冲突概率极低 |
| `training_task` | 否 | 任务修改通常由创建者本人操作 |

### 2.5 审计字段

| 字段 | 类型 | 说明 | 填充时机 |
|---|---|---|---|
| `create_time` | `DATETIME` | 记录创建时间 | INSERT 时自动填充 |
| `update_time` | `DATETIME` | 记录最后修改时间 | INSERT + UPDATE 时自动填充 |
| `create_by` | `BIGINT` | 创建人用户 ID | INSERT 时自动填充（从当前登录用户获取） |
| `update_by` | `BIGINT` | 最后修改人用户 ID | UPDATE 时自动填充 |

**实现方式**：MyBatis Plus `MetaObjectHandler` 自动填充 `create_time` / `update_time`，结合 Sa-Token `StpUtil.getLoginId()` 填充 `create_by` / `update_by`。

**适用范围**：所有业务表（主数据表 + 事务表）。关联表（`user_role`、`course_teacher` 等）和日志表不需要审计字段。

### 2.6 索引原则

#### 2.6.1 何时建索引

| 场景 | 示例 |
|---|---|
| **WHERE 条件列** | `WHERE status = 'PENDING'` → 对 `status` 建索引 |
| **外键列** | `WHERE user_id = 10001` → 对 `user_id` 建索引 |
| **排序列** | `ORDER BY create_time DESC` → 对 `create_time` 建索引 |
| **联合查询列** | `WHERE training_id = X AND user_id = Y` → 对 `(training_id, user_id)` 建联合索引 |
| **唯一约束列** | `username` → 建 UNIQUE 索引 |

#### 2.6.2 何时不建索引

| 场景 | 原因 |
|---|---|
| **区分度低的列** | `deleted`（仅 0/1 两值）、`status`（若 90% 记录为同一状态）— 索引无效 |
| **频繁更新的列** | 每次 UPDATE 都需要维护索引，开销大 |
| **小表（< 1000 行）** | 全表扫描比索引查找更快 |
| **TEXT/JSON 列** | 不能建普通 B-Tree 索引（JSON 用虚拟列 + 索引） |
| **不在查询条件中的列** | 索引不参与查询就浪费存储和写入性能 |

#### 2.6.3 联合索引最左前缀原则

联合索引 `(a, b, c)` 的生效条件：
- `WHERE a = ?` → ✅ 使用索引
- `WHERE a = ? AND b = ?` → ✅ 使用索引
- `WHERE a = ? AND c = ?` → ✅ 部分使用（仅 a 列）
- `WHERE b = ? AND c = ?` → ❌ 不使用索引（缺少最左列 a）
- `WHERE a = ? AND b > ? AND c = ?` → 部分使用（a + b range，c 不生效）

设计联合索引时，**区分度最高的列放在最左侧**。

#### 2.6.4 索引命名

| 索引类型 | 命名规则 | 示例 |
|---|---|---|
| 普通索引 | `idx_{字段名}` | `idx_status` |
| 联合索引 | `idx_{字段1}_{字段2}` | `idx_training_id_user_id` |
| 唯一索引 | `uk_{字段名}` | `uk_username` |
| 主键索引 | 自动命名 `PRIMARY` | — |

### 2.7 事务原则

#### 2.7.1 事务边界

| 规则 | 说明 |
|---|---|
| **事务范围最小化** | 事务只包裹必须保持原子性的数据库操作，不包含 HTTP 调用、文件 I/O、大模型 API 调用 |
| **事务超时** | 默认 30 秒，通过 `@Transactional(timeout = 30)` 控制 |
| **回滚策略** | `RuntimeException` 及其子类触发回滚，checked Exception 默认不回滚（除非指定 `rollbackFor`） |
| **隔离级别** | 默认 `READ_COMMITTED`（MySQL InnoDB 默认），防脏读，允许不可重复读和幻读 |
| **传播行为** | 默认 `REQUIRED`（有事务则加入，无则新建） |

#### 2.7.2 需要事务的业务

| 业务 | 涉及表 | 理由 |
|---|---|---|
| **用户注册** | user + user_role | 用户和角色必须同时创建成功 |
| **课程创建 + 关联** | course + course_teacher + course_student | 课程与关联关系原子写入 |
| **任务发布 + 分发** | training_task + training_class | 任务与班级分发原子写入 |
| **提交创建** | submission + submission_file | 提交与文件记录原子写入 |
| **教师评分发布** | teacher_review + review_item + score_record | 复核结论与最终成绩原子写入 |
| **标准模板 + 维度** | evaluation_standard + standard_dimension + standard_rule | 模板和配置原子写入 |

#### 2.7.3 不能开启事务的业务

| 业务 | 原因 |
|---|---|
| **AI 分析** | AI 调用耗时 30-120 秒，开启事务会导致数据库连接被长时间占用；AI 分析结果写入与 LLM 调用无关，可以单独事务 |
| **文件上传到 MinIO** | MinIO 不支持 MySQL 事务协议，上传失败时需通过补偿逻辑处理 |
| **Git 仓库克隆** | 网络 I/O 操作，超时不可控 |
| **报表 PDF/Excel 生成** | 文件生成是 CPU 密集型操作，不应占用数据库连接 |
| **通知推送** | 推送失败不应回滚业务操作 |

### 2.8 数据一致性

#### 2.8.1 外键策略

**决策：不使用数据库外键约束（FOREIGN KEY），应用层保证引用完整性。**

理由：
1. **性能**：外键在 INSERT/UPDATE/DELETE 时需要校验引用完整性，高并发下成为瓶颈
2. **灵活性**：AI 分析结果可以先写入（user_id=NULL），后续关联到具体用户
3. **数据归档**：外键约束阻止数据迁移和归档操作
4. **分库分表**：未来拆分时外键无法跨库工作

替代方案：
- 应用层在 Service 层通过代码验证引用完整性
- 关联查询使用 JOIN，不影响查询功能
- 定期运行数据一致性检查脚本（定时任务），发现孤立记录时告警

#### 2.8.2 唯一约束

| 表 | 唯一约束 | 类型 | 理由 |
|---|---|---|---|
| user | `uk_username` | UNIQUE INDEX | 用户名全局唯一 |
| user_role | `uk_user_role` (user_id, role_id) | UNIQUE INDEX | 同一用户不能重复分配同一角色 |
| course_teacher | `uk_course_teacher` (course_id, user_id) | UNIQUE INDEX | 同一教师不能重复分配到同一课程 |
| course_student | `uk_course_student` (course_id, user_id) | UNIQUE INDEX | 同一学生不能重复加入同一课程 |
| training_class | `uk_training_class` (training_id, class_id) | UNIQUE INDEX | 同一班级不能重复分发到同一任务 |
| submission | `uk_submission_user_task` (user_id, training_task_id) | UNIQUE INDEX | 同一学生对同一任务只有一条提交记录（多次提交覆盖） |
| ai_analysis | `uk_ai_submission` (submission_id) | UNIQUE INDEX | 一次提交只有一条 AI 分析结果 |
| teacher_review | `uk_review_submission` (submission_id) | UNIQUE INDEX | 一次提交只有一条教师复核记录 |

### 2.9 扩展性

#### 2.9.1 预留扩展字段

| 策略 | 说明 | 示例 |
|---|---|---|
| **JSON 扩展列** | 不稳定字段不单独建列，存在 JSON 列中 | `ai_analysis.raw_response JSON` 存储 LLM 原始响应 |
| **稀疏列放 JSON** | 不是每行都有的属性放在 JSON 中 | `user.metadata JSON` 存储角色特定属性（学生学号、教师职称等） |
| **附加属性表** | 动态 key-value 表 | `entity_attribute(entity_type, entity_id, attr_key, attr_value)` |

#### 2.9.2 多租户预留

当前 MVP 为单学校部署。预留 `tenant_id` 字段（默认 `BIGINT DEFAULT 1`），所有主数据表包含该字段。未来多学校部署时按 `tenant_id` 进行数据隔离，无需大规模表结构变更。

#### 2.9.3 分库分表预留

Snowflake 主键天然支持分库分表（全局唯一，不依赖数据库自增）。分片键选择 `user_id` 或 `training_task_id`（高频查询条件）。分片中间件预留 ShardingSphere 集成点。

---

## 3. Overall ER Design

### 3.1 Entity-Relationship Diagram

```mermaid
erDiagram
    USER ||--o{ USER_ROLE : has
    ROLE ||--o{ USER_ROLE : assigned-to

    USER ||--o{ COURSE_TEACHER : teaches
    USER ||--o{ COURSE_STUDENT : enrolled-in
    COURSE ||--o{ COURSE_TEACHER : has
    COURSE ||--o{ COURSE_STUDENT : has
    COURSE ||--o{ COURSE_CLASS : includes
    CLASS ||--o{ COURSE_CLASS : belongs-to

    COURSE ||--o{ TRAINING_TASK : contains
    TRAINING_TASK ||--o{ TRAINING_CLASS : distributed-to
    CLASS ||--o{ TRAINING_CLASS : receives
    EVALUATION_STANDARD ||--o{ TRAINING_TASK : referenced-by

    TRAINING_TASK ||--o{ SUBMISSION : receives
    USER ||--o{ SUBMISSION : submits
    SUBMISSION ||--o{ SUBMISSION_FILE : contains
    FILE_STORAGE ||--o{ SUBMISSION_FILE : stores

    SUBMISSION ||--|| AI_ANALYSIS : analyzed-by
    AI_ANALYSIS ||--o{ AI_ANALYSIS_DETAIL : breakdown

    SUBMISSION ||--|| TEACHER_REVIEW : reviewed-by
    TEACHER_REVIEW ||--o{ REVIEW_ITEM : contains
    AI_ANALYSIS_DETAIL ||--o{ REVIEW_ITEM : references
    USER ||--o{ TEACHER_REVIEW : performs-review

    TEACHER_REVIEW ||--|| SCORE_RECORD : generates
    USER ||--o{ SCORE_RECORD : receives

    EVALUATION_STANDARD ||--o{ STANDARD_DIMENSION : has
    STANDARD_DIMENSION ||--o{ STANDARD_RULE : has

    REPORT ||--o{ FILE_STORAGE : exported-to
    USER ||--o{ REPORT : generates

    USER ||--o{ OPERATION_LOG : triggers
    USER ||--o{ NOTIFICATION : receives

    SYSTEM_CONFIG ||--|| USER : configured-by

    USER ||--o{ STATISTICS_SNAPSHOT : belongs-to
    TRAINING_TASK ||--o{ STATISTICS_SNAPSHOT : summarizes
    CLASS ||--o{ STATISTICS_SNAPSHOT : aggregates
```

### 3.2 Core Entity Relationships

| 实体 A | 关系 | 实体 B | 基数 | 说明 |
|---|---|---|---|---|
| User | has | Role | M:N | 通过 user_role 关联表实现。一个用户可有多个角色（当前 MVP 每人一个角色） |
| User | teaches | Course | M:N | 通过 course_teacher 关联表实现 |
| User | enrolled-in | Course | M:N | 通过 course_student 关联表实现 |
| Course | includes | Class | M:N | 通过 course_class 关联表实现 |
| TrainingTask | belongs-to | Course | M:1 | 一个课程下有多个实训任务 |
| TrainingTask | references | EvaluationStandard | M:1 | 一个任务引用一个评价标准模板 |
| TrainingTask | distributed-to | Class | M:N | 通过 training_class 关联表实现 |
| Submission | belongs-to | TrainingTask | M:1 | 一个任务接收多个提交 |
| Submission | belongs-to | User | M:1 | 一个学生有多个提交 |
| Submission | includes | FileStorage | 1:N | 一个提交包含多个文件 |
| AIAnalysis | analyzes | Submission | 1:1 | 一个提交生成一份 AI 分析 |
| AIAnalysisDetail | belongs-to | AIAnalysis | M:1 | 一份分析含多个扣分项 |
| TeacherReview | reviews | Submission | 1:1 | 一个提交对应一份教师复核 |
| ReviewItem | belongs-to | TeacherReview | M:1 | 一份复核含多个复核意见 |
| ReviewItem | references | AIAnalysisDetail | M:1 | 复核基于 AI 分析明细 |
| ScoreRecord | generated-from | TeacherReview | 1:1 | 一份复核生成一条最终成绩 |
| StandardDimension | belongs-to | EvaluationStandard | M:1 | 一个标准含多个维度 |
| StandardRule | belongs-to | StandardDimension | M:1 | 一个维度含多条规则 |
| Report | belongs-to | User | M:1 | 一个用户可生成多份报表 |
| OperationLog | belongs-to | User | M:1 | 一个用户有多条操作日志 |
| Notification | belongs-to | User | M:1 | 一个用户有多条通知 |
| StatisticsSnapshot | belongs-to | TrainingTask | M:1 | 按任务定期快照统计 |

---

## 4. Table Design

### 4.1 用户与权限

#### 4.1.1 `user` — 系统用户

| 属性 | 值 |
|---|---|
| **表用途** | 存储系统中所有用户账号的基础信息（学生、教师、管理员） |
| **数据来源** | 管理员手动创建 / 批量导入（Excel） |
| **关联表** | user_role, course_teacher, course_student, submission, teacher_review, operation_log, notification |
| **数据生命周期** | 用户禁用后保留数据（不物理删除），毕业生数据可归档 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键，用户 ID |
| `username` | `VARCHAR(64)` | ❌ | — | 用户名，登录凭据，全局唯一 |
| `password` | `VARCHAR(256)` | ❌ | — | BCrypt 加密后的密码哈希，不可逆 |
| `real_name` | `VARCHAR(64)` | ❌ | — | 真实姓名，用于页面展示 |
| `email` | `VARCHAR(128)` | ✅ | NULL | 电子邮箱 |
| `phone` | `VARCHAR(20)` | ✅ | NULL | 手机号码 |
| `avatar_url` | `VARCHAR(512)` | ✅ | NULL | 头像访问 URL（MinIO 预签名 URL 或相对路径） |
| `status` | `TINYINT` | ❌ | `1` | 状态：1=启用, 0=禁用 |
| `lock_expire_time` | `DATETIME` | ✅ | NULL | 账户锁定到期时间（登录失败 5 次后锁定 30 分钟） |
| `login_fail_count` | `INT` | ❌ | `0` | 连续登录失败次数（登录成功后重置） |
| `last_login_time` | `DATETIME` | ✅ | NULL | 最后一次登录时间 |
| `last_login_ip` | `VARCHAR(64)` | ✅ | NULL | 最后一次登录 IP 地址 |
| `deleted` | `TINYINT` | ❌ | `0` | 逻辑删除标记：0=正常, 1=已删除 |
| `create_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 账户创建时间 |
| `update_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 最后修改时间 |
| `create_by` | `BIGINT` | ✅ | NULL | 创建人 ID（管理员创建时为管理员 ID，注册时为 0） |
| `update_by` | `BIGINT` | ✅ | NULL | 最后修改人 ID |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `uk_username` | `username` | UNIQUE | 用户名唯一，登录查询核心索引 |
| `idx_status` | `status` | 普通 | 管理员按状态筛选用户列表 |
| `idx_real_name` | `real_name` | 普通 | 管理员按姓名搜索用户 |
| `idx_create_time` | `create_time` | 普通 | 按创建时间排序和分页 |

#### 4.1.2 `role` — 角色定义

| 属性 | 值 |
|---|---|
| **表用途** | 定义系统中的角色（学生 / 教师 / 管理员）及其权限码集合 |
| **数据来源** | 系统初始化脚本预置 3 条记录 |
| **关联表** | user_role |
| **数据生命周期** | 静态数据，几乎不变（除非未来增加角色类型） |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键 |
| `role_code` | `VARCHAR(32)` | ❌ | — | 角色编码：admin, teacher, student |
| `role_name` | `VARCHAR(64)` | ❌ | — | 角色名称：管理员, 教师, 学生 |
| `description` | `VARCHAR(256)` | ✅ | NULL | 角色描述 |
| `sort_order` | `INT` | ❌ | `0` | 排序号 |
| `deleted` | `TINYINT` | ❌ | `0` | 逻辑删除标记 |
| `create_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 最后修改时间 |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `uk_role_code` | `role_code` | UNIQUE | 角色编码唯一 |

**预置数据**：

| role_code | role_name | 说明 |
|---|---|---|
| `admin` | 管理员 | 最高权限，系统配置和管理 |
| `teacher` | 教师 | 课程管理、任务发布、复核评分、报表查看 |
| `student` | 学生 | 查看任务、提交成果、查看个人成绩和报告 |

#### 4.1.3 `user_role` — 用户角色关联

| 属性 | 值 |
|---|---|
| **表用途** | 将用户与角色关联，实现 RBAC 用户-角色映射 |
| **数据来源** | 创建用户时自动分配角色 |
| **关联表** | 关联 user(id) 和 role(id) |
| **数据生命周期** | 随用户删除而删除（物理删除），随用户角色变更而更新 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键 |
| `user_id` | `BIGINT` | ❌ | — | 用户 ID |
| `role_id` | `BIGINT` | ❌ | — | 角色 ID |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `uk_user_role` | `(user_id, role_id)` | UNIQUE | 防重复分配 |
| `idx_role_id` | `role_id` | 普通 | 按角色查询用户 |

### 4.2 课程与班级

#### 4.2.1 `course` — 课程信息

| 属性 | 值 |
|---|---|
| **表用途** | 存储课程基本信息，是教学资源的核心组织单元 |
| **数据来源** | 教师创建课程 |
| **关联表** | course_teacher, course_student, course_class, training_task |
| **数据生命周期** | 学期结束后归档（删除标记），不物理删除 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键，课程 ID |
| `course_code` | `VARCHAR(32)` | ❌ | — | 课程代码，如 `CS101`，全局唯一 |
| `course_name` | `VARCHAR(128)` | ❌ | — | 课程名称，如 "Java 企业级开发实训" |
| `semester` | `VARCHAR(32)` | ❌ | — | 学期，如 `2025-2026-2` |
| `credits` | `DECIMAL(3,1)` | ✅ | `0.0` | 学分 |
| `description` | `TEXT` | ✅ | NULL | 课程描述（支持 Markdown） |
| `syllabus` | `TEXT` | ✅ | NULL | 教学大纲（支持 Markdown） |
| `objectives` | `TEXT` | ✅ | NULL | 教学目标与能力指标 |
| `status` | `VARCHAR(16)` | ❌ | `ACTIVE` | 状态：ACTIVE（进行中）, ARCHIVED（已归档） |
| `deleted` | `TINYINT` | ❌ | `0` | 逻辑删除标记 |
| `version` | `INT` | ❌ | `0` | 乐观锁 |
| `create_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 最后修改时间 |
| `create_by` | `BIGINT` | ✅ | NULL | 创建人 ID（教师） |
| `update_by` | `BIGINT` | ✅ | NULL | 最后修改人 ID |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `uk_course_code` | `course_code` | UNIQUE | 课程代码唯一 |
| `idx_semester` | `semester` | 普通 | 按学期筛选课程 |
| `idx_status` | `status` | 普通 | 按状态筛选课程 |
| `idx_create_by` | `create_by` | 普通 | 按创建教师筛选课程 |

#### 4.2.2 `class` — 班级信息

| 属性 | 值 |
|---|---|
| **表用途** | 存储班级信息，是学生分组和任务分发的单位 |
| **数据来源** | 管理员创建 / 批量导入 |
| **关联表** | course_class, training_class |
| **数据生命周期** | 长期保留，学生毕业后可归档 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键，班级 ID |
| `class_code` | `VARCHAR(32)` | ❌ | — | 班级编号，如 `CS2101`，全局唯一 |
| `class_name` | `VARCHAR(128)` | ❌ | — | 班级名称，如 "计算机科学 2021 级 1 班" |
| `grade` | `VARCHAR(16)` | ✅ | NULL | 年级，如 `2021` |
| `major` | `VARCHAR(64)` | ✅ | NULL | 专业，如 "计算机科学与技术" |
| `department` | `VARCHAR(64)` | ✅ | NULL | 院系，如 "信息科学与工程学院" |
| `student_count` | `INT` | ❌ | `0` | 班级学生人数（冗余缓存，定时刷新） |
| `deleted` | `TINYINT` | ❌ | `0` | 逻辑删除标记 |
| `create_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 最后修改时间 |
| `create_by` | `BIGINT` | ✅ | NULL | 创建人 ID（管理员） |
| `update_by` | `BIGINT` | ✅ | NULL | 最后修改人 ID |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `uk_class_code` | `class_code` | UNIQUE | 班级编号唯一 |
| `idx_grade` | `grade` | 普通 | 按年级筛选 |
| `idx_major` | `major` | 普通 | 按专业筛选 |

#### 4.2.3 `course_teacher` — 课程-教师关联

| 属性 | 值 |
|---|---|
| **表用途** | 记录教师与课程的授课关联关系 |
| **数据来源** | 创建课程时指定授课教师 |
| **关联表** | 关联 course(id) 和 user(id)（role=teacher） |
| **数据生命周期** | 解除授课关系时物理删除 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键 |
| `course_id` | `BIGINT` | ❌ | — | 课程 ID |
| `user_id` | `BIGINT` | ❌ | — | 教师用户 ID |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `uk_course_teacher` | `(course_id, user_id)` | UNIQUE | 防重复关联 |
| `idx_user_id` | `user_id` | 普通 | 按教师查询其所授课程 |

#### 4.2.4 `course_student` — 课程-学生关联

| 属性 | 值 |
|---|---|
| **表用途** | 记录学生与课程的选课关联关系 |
| **数据来源** | 管理员 / 教师批量导入学生名单 |
| **关联表** | 关联 course(id) 和 user(id)（role=student） |
| **数据生命周期** | 退课时物理删除 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键 |
| `course_id` | `BIGINT` | ❌ | — | 课程 ID |
| `user_id` | `BIGINT` | ❌ | — | 学生用户 ID |
| `class_id` | `BIGINT` | ✅ | NULL | 所在班级 ID（冗余，方便查询） |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `uk_course_student` | `(course_id, user_id)` | UNIQUE | 防重复选课 |
| `idx_user_id` | `user_id` | 普通 | 按学生查询其所选课程 |
| `idx_class_id` | `class_id` | 普通 | 按班级查询学生 |

#### 4.2.5 `course_class` — 课程-班级关联

| 属性 | 值 |
|---|---|
| **表用途** | 记录课程面向哪些班级开设 |
| **数据来源** | 创建课程时指定授课班级 |
| **关联表** | 关联 course(id) 和 class(id) |
| **数据生命周期** | 解除关联时物理删除 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键 |
| `course_id` | `BIGINT` | ❌ | — | 课程 ID |
| `class_id` | `BIGINT` | ❌ | — | 班级 ID |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `uk_course_class` | `(course_id, class_id)` | UNIQUE | 防重复关联 |

### 4.3 实训任务

#### 4.3.1 `training_task` — 实训任务

| 属性 | 值 |
|---|---|
| **表用途** | 存储教师发布的实训任务，是提交和评分的基本单位 |
| **数据来源** | 教师创建并发布 |
| **关联表** | course, evaluation_standard, training_class, submission |
| **数据生命周期** | 任务结束后状态变更为 ENDED，长期保留 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键，任务 ID |
| `course_id` | `BIGINT` | ❌ | — | 所属课程 ID |
| `standard_id` | `BIGINT` | ✅ | NULL | 引用的评价标准模板 ID |
| `task_name` | `VARCHAR(256)` | ❌ | — | 任务名称，如 "Spring Boot 在线商城系统开发" |
| `description` | `TEXT` | ✅ | NULL | 任务描述（支持 Markdown） |
| `requirement` | `TEXT` | ✅ | NULL | 任务要求（支持 Markdown，详细列出交付物、评分要点） |
| `start_time` | `DATETIME` | ✅ | NULL | 任务开始时间 |
| `end_time` | `DATETIME` | ✅ | NULL | 截止日期（提交 DDL） |
| `allow_late` | `TINYINT` | ❌ | `0` | 是否允许逾期提交：0=不允许, 1=允许 |
| `late_penalty` | `DECIMAL(5,2)` | ✅ | `0.00` | 逾期扣分百分比（如 10.00 表示扣 10%） |
| `submission_type` | `VARCHAR(32)` | ❌ | `ZIP` | 提交方式：ZIP（文件上传）, GIT（Git 仓库）, BOTH（两者均可） |
| `max_submit_count` | `INT` | ❌ | `3` | 最大重提交次数 |
| `max_score` | `DECIMAL(5,2)` | ❌ | `100.00` | 满分分值 |
| `weight` | `DECIMAL(5,2)` | ✅ | `1.00` | 在课程总评中的权重 |
| `priority` | `VARCHAR(16)` | ❌ | `MEDIUM` | 优先级：HIGH, MEDIUM, LOW |
| `status` | `VARCHAR(16)` | ❌ | `DRAFT` | 状态：DRAFT（草稿）, PUBLISHED（已发布）, ENDED（已结束） |
| `publish_time` | `DATETIME` | ✅ | NULL | 发布时间 |
| `end_actual_time` | `DATETIME` | ✅ | NULL | 实际结束时间 |
| `deleted` | `TINYINT` | ❌ | `0` | 逻辑删除标记 |
| `version` | `INT` | ❌ | `0` | 乐观锁 |
| `create_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 最后修改时间 |
| `create_by` | `BIGINT` | ✅ | NULL | 创建人 ID（教师） |
| `update_by` | `BIGINT` | ✅ | NULL | 最后修改人 ID |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `idx_course_id` | `course_id` | 普通 | 按课程查询任务 |
| `idx_status` | `status` | 普通 | 按状态筛选（PUBLISHED 查询频率最高） |
| `idx_end_time` | `end_time` | 普通 | 按截止日期查询和排序 |
| `idx_course_status` | `(course_id, status)` | 联合 | 按课程+状态联合查询 |
| `idx_create_by` | `create_by` | 普通 | 按创建教师查询 |

#### 4.3.2 `training_class` — 任务-班级分发

| 属性 | 值 |
|---|---|
| **表用途** | 记录实训任务分发到了哪些班级 |
| **数据来源** | 教师发布任务时选择目标班级 |
| **关联表** | 关联 training_task(id) 和 class(id) |
| **数据生命周期** | 任务结束时保留，任务删除时物理删除 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键 |
| `training_id` | `BIGINT` | ❌ | — | 任务 ID |
| `class_id` | `BIGINT` | ❌ | — | 班级 ID |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `uk_training_class` | `(training_id, class_id)` | UNIQUE | 防重复分发 |
| `idx_class_id` | `class_id` | 普通 | 按班级查询任务 |

### 4.4 提交与文件

#### 4.4.1 `submission` — 学生提交

| 属性 | 值 |
|---|---|
| **表用途** | 记录学生对实训任务的每次提交，是 AI 分析和教师评分的起点 |
| **数据来源** | 学生通过提交页面创建 |
| **关联表** | training_task, user, submission_file, ai_analysis, teacher_review |
| **数据生命周期** | 长期保留（成绩追溯），不可删除 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键，提交 ID |
| `training_task_id` | `BIGINT` | ❌ | — | 所属任务 ID |
| `user_id` | `BIGINT` | ❌ | — | 提交学生 ID |
| `submit_type` | `VARCHAR(8)` | ❌ | — | 提交方式：ZIP（文件上传）, GIT（Git 仓库 URL）, CODE（在线代码）, TEXT（文本） |
| `git_url` | `VARCHAR(512)` | ✅ | NULL | Git 仓库 URL（submit_type=GIT 时必填） |
| `git_branch` | `VARCHAR(128)` | ✅ | NULL | Git 分支名（默认 main） |
| `git_commit_id` | `VARCHAR(64)` | ✅ | NULL | 克隆时的最新 Commit ID（记录提交版本） |
| `summary` | `TEXT` | ✅ | NULL | 提交备注说明 |
| `submit_count` | `INT` | ❌ | `1` | 第几次提交（累计） |
| `submit_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 提交时间 |
| `is_late` | `TINYINT` | ❌ | `0` | 是否逾期提交：0=否, 1=是 |
| `status` | `VARCHAR(32)` | ❌ | `SUBMITTED` | 状态：SUBMITTED（已提交）, ANALYZING（AI 分析中）, COMPLETED（分析完成）, REVIEWED（教师已评分） |
| `deleted` | `TINYINT` | ❌ | `0` | 逻辑删除标记 |
| `version` | `INT` | ❌ | `0` | 乐观锁 |
| `create_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 最后修改时间 |
| `create_by` | `BIGINT` | ✅ | NULL | 创建人 ID（学生本人） |
| `update_by` | `BIGINT` | ✅ | NULL | 最后修改人 ID |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `uk_submission_user_task` | `(user_id, training_task_id)` | UNIQUE | 一个学生一个任务一条记录 |
| `idx_training_task_id` | `training_task_id` | 普通 | 按任务查询所有提交（教师查看提交列表） |
| `idx_user_id` | `user_id` | 普通 | 按学生查询提交历史 |
| `idx_status` | `status` | 普通 | 按状态筛选（待分析 / 待复核） |
| `idx_submit_time` | `submit_time` | 普通 | 按提交时间排序和分页 |

#### 4.4.2 `submission_file` — 提交文件

| 属性 | 值 |
|---|---|
| **表用途** | 记录一次提交中所包含的文件清单 |
| **数据来源** | 学生上传文件后，FileService 解析后写入 |
| **关联表** | 关联 submission(id) 和 file_storage(id) |
| **数据生命周期** | 随提交记录保留 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键 |
| `submission_id` | `BIGINT` | ❌ | — | 提交 ID |
| `file_id` | `BIGINT` | ❌ | — | 文件存储 ID |
| `file_name` | `VARCHAR(256)` | ❌ | — | 原始文件名（冗余，方便查询） |
| `file_type` | `VARCHAR(32)` | ❌ | — | 文件类型（冗余）：ZIP, JAVA, PDF, DOC, PNG 等 |
| `file_size` | `BIGINT` | ❌ | `0` | 文件大小（字节） |
| `sort_order` | `INT` | ❌ | `0` | 排序号 |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `idx_submission_id` | `submission_id` | 普通 | 按提交查询所含文件 |

#### 4.4.3 `file_storage` — 文件存储

| 属性 | 值 |
|---|---|
| **表用途** | 记录所有上传到 MinIO（或其他对象存储）的文件元数据 |
| **数据来源** | FileService 上传文件成功后写入 |
| **关联表** | submission_file, report |
| **数据生命周期** | 根据 bucket 不同：submissions 永久保留，reports 90 天过期，temp 24 小时自动清理 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键，文件 ID |
| `bucket` | `VARCHAR(64)` | ❌ | — | 存储桶名称：submissions, knowledge, reports, avatars, temp |
| `object_key` | `VARCHAR(512)` | ❌ | — | MinIO 中的对象 Key（路径），如 `submissions/1001/2001/3001/source.zip` |
| `original_name` | `VARCHAR(256)` | ❌ | — | 原始文件名 |
| `content_type` | `VARCHAR(128)` | ✅ | NULL | MIME 类型，如 `application/zip` |
| `file_size` | `BIGINT` | ❌ | `0` | 文件大小（字节） |
| `file_md5` | `VARCHAR(64)` | ✅ | NULL | 文件 MD5 哈希值（用于去重和完整性校验） |
| `access_url` | `VARCHAR(1024)` | ✅ | NULL | 文件访问 URL（预签名 URL 或公开 URL） |
| `expire_time` | `DATETIME` | ✅ | NULL | URL 过期时间 / 文件自动删除时间 |
| `create_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 上传时间 |
| `create_by` | `BIGINT` | ✅ | NULL | 上传人 ID |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `uk_object_key` | `object_key` | UNIQUE | 对象路径唯一 |
| `idx_bucket` | `bucket` | 普通 | 按存储桶查询 |
| `idx_file_md5` | `file_md5` | 普通 | MD5 去重查询 |
| `idx_expire_time` | `expire_time` | 普通 | 定时任务扫描过期文件 |

### 4.5 AI 分析

#### 4.5.1 `ai_analysis` — AI 分析结果

| 属性 | 值 |
|---|---|
| **表用途** | 存储 AI 分析的综合结果（总分、模型信息、Token 用量），是 AI 分析的核心记录 |
| **数据来源** | AI 分析任务完成后写入 |
| **关联表** | 关联 submission(id)，被 ai_analysis_detail 引用 |
| **数据生命周期** | 长期保留，用于 AI 效果评估和问题追溯 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键，分析 ID |
| `submission_id` | `BIGINT` | ❌ | — | 关联的提交 ID（一对一） |
| `total_score` | `DECIMAL(5,2)` | ✅ | NULL | AI 综合评分（0-100） |
| `analysis_status` | `VARCHAR(16)` | ❌ | `PENDING` | 状态：PENDING（排队中）, ANALYZING（分析中）, COMPLETED（已完成）, FAILED（失败） |
| `error_message` | `VARCHAR(1024)` | ✅ | NULL | 失败原因（analysis_status=FAILED 时记录） |
| `analysis_time_ms` | `INT` | ✅ | NULL | 分析耗时（毫秒） |
| `model_provider` | `VARCHAR(32)` | ✅ | NULL | 模型厂商：deepseek, openai |
| `model_name` | `VARCHAR(64)` | ✅ | NULL | 模型名称：deepseek-chat, gpt-4o |
| `token_input` | `INT` | ✅ | `0` | 输入 Token 数 |
| `token_output` | `INT` | ✅ | `0` | 输出 Token 数 |
| `token_total` | `INT` | ✅ | `0` | 总 Token 数 |
| `retry_count` | `INT` | ❌ | `0` | 重试次数（失败后自动重试，最多 3 次） |
| `raw_response` | `JSON` | ✅ | NULL | LLM 原始响应（完整 JSON，含模型元数据） |
| `start_time` | `DATETIME` | ✅ | NULL | 分析开始时间 |
| `complete_time` | `DATETIME` | ✅ | NULL | 分析完成时间 |
| `version` | `INT` | ❌ | `0` | 乐观锁 |
| `create_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 创建时间（任务创建时间） |
| `update_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 最后修改时间 |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `uk_ai_submission` | `submission_id` | UNIQUE | 一次提交一条分析结果 |
| `idx_analysis_status` | `analysis_status` | 普通 | 按状态查询（PENDING 用于轮询任务队列） |
| `idx_create_time` | `create_time` | 普通 | 按创建时间排序 |

#### 4.5.2 `ai_analysis_detail` — AI 扣分项明细

| 属性 | 值 |
|---|---|
| **表用途** | 存储 AI 分析的每条扣分/问题明细，是教师复核的基本操作对象 |
| **数据来源** | AI 分析过程中逐条写入 |
| **关联表** | 关联 ai_analysis(id), standard_dimension(id), standard_rule(id) |
| **数据生命周期** | 长期保留，每条对应一个可追溯的扣分理由 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键，明细 ID |
| `ai_analysis_id` | `BIGINT` | ❌ | — | 所属 AI 分析 ID |
| `dimension_id` | `BIGINT` | ✅ | NULL | 关联的评价维度 ID |
| `rule_id` | `BIGINT` | ✅ | NULL | 触发的评分规则 ID |
| `agent_type` | `VARCHAR(8)` | ❌ | — | 分析 Agent：DOC（文档）, CODE（代码）, REQ（需求） |
| `file_path` | `VARCHAR(512)` | ✅ | NULL | 问题所在文件路径 |
| `line_number` | `INT` | ✅ | NULL | 问题所在行号 |
| `issue_type` | `VARCHAR(128)` | ❌ | — | 问题类型：命名不规范, 缺少异常处理, SQL 注入风险, 文档结构不完整 等 |
| `severity` | `VARCHAR(16)` | ❌ | `MINOR` | 严重级别：MINOR（轻微）, MAJOR（严重）, CRITICAL（致命） |
| `reason` | `TEXT` | ❌ | — | 扣分理由和问题描述（AI 生成的问题说明） |
| `suggestion` | `TEXT` | ✅ | NULL | 改进建议（AI 生成的修复方案） |
| `suggest_deduct` | `DECIMAL(5,2)` | ❌ | `0.00` | AI 建议扣分分值 |
| `confidence` | `DECIMAL(3,2)` | ❌ | `1.00` | AI 置信度（0.00-1.00），低置信度项需教师重点关注 |
| `is_adopted` | `TINYINT` | ✅ | NULL | 教师复核结果：NULL（未处理）, 1（采纳）, 0（拒绝） |
| `adjusted_deduct` | `DECIMAL(5,2)` | ✅ | NULL | 教师调整后的扣分（拒绝时为 0，手动调整时为新值） |
| `sort_order` | `INT` | ❌ | `0` | 排序号（按文件/行号排序） |
| `create_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 创建时间 |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `idx_ai_analysis_id` | `ai_analysis_id` | 普通 | 按分析查询所有扣分项 |
| `idx_agent_type` | `agent_type` | 普通 | 按 Agent 类型筛选 |
| `idx_severity` | `severity` | 普通 | 按严重级别筛选 |
| `idx_is_adopted` | `is_adopted` | 普通 | 查询未处理的扣分项 |

### 4.6 评价标准

#### 4.6.1 `evaluation_standard` — 评价标准模板

| 属性 | 值 |
|---|---|
| **表用途** | 存储评价标准模板，定义评分框架（引用于 training_task） |
| **数据来源** | 教师创建 / 从标准库复制 |
| **关联表** | standard_dimension, training_task |
| **数据生命周期** | 长期保留，支持版本迭代和回滚 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键，标准 ID |
| `standard_name` | `VARCHAR(256)` | ❌ | — | 标准名称，如 "Java Web 实训评分标准 v1" |
| `description` | `TEXT` | ✅ | NULL | 标准描述 |
| `course_type` | `VARCHAR(32)` | ✅ | NULL | 适用课程类型（用于标准库分类） |
| `version` | `INT` | ❌ | `1` | 版本号（递增） |
| `status` | `VARCHAR(16)` | ❌ | `DRAFT` | 状态：DRAFT（草稿）, PUBLISHED（已发布）, ARCHIVED（已归档） |
| `is_template` | `TINYINT` | ❌ | `0` | 是否为标准库模板（1=可被复制引用, 0=任务专属） |
| `deleted` | `TINYINT` | ❌ | `0` | 逻辑删除标记 |
| `create_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 最后修改时间 |
| `create_by` | `BIGINT` | ✅ | NULL | 创建人 ID（教师） |
| `update_by` | `BIGINT` | ✅ | NULL | 最后修改人 ID |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `idx_status` | `status` | 普通 | 按状态筛选 |
| `idx_is_template` | `is_template` | 普通 | 查询标准库模板列表 |
| `idx_create_by` | `create_by` | 普通 | 按创建者查询 |

#### 4.6.2 `standard_dimension` — 评价维度

| 属性 | 值 |
|---|---|
| **表用途** | 存储评价标准中的评分维度（如代码规范、文档完整度、需求匹配度） |
| **数据来源** | 创建/编辑评价标准时一起配置 |
| **关联表** | 关联 evaluation_standard(id) |
| **数据生命周期** | 随所属标准保留 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键，维度 ID |
| `standard_id` | `BIGINT` | ❌ | — | 所属标准 ID |
| `dim_name` | `VARCHAR(128)` | ❌ | — | 维度名称：代码规范性, 文档完整性, 设计质量, 需求匹配度, 创新性 |
| `dim_description` | `VARCHAR(512)` | ✅ | NULL | 维度说明（评价要点） |
| `weight` | `DECIMAL(5,2)` | ❌ | `0.00` | 权重百分比（如 30.00 表示 30%）。同一标准下所有维度权重之和 = 100% |
| `max_score` | `DECIMAL(5,2)` | ❌ | `0.00` | 该维度满分值 |
| `sort_order` | `INT` | ❌ | `0` | 排序号 |
| `create_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 最后修改时间 |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `idx_standard_id` | `standard_id` | 普通 | 按标准查询所有维度 |

**约束**：同一 `standard_id` 下所有 `weight` 之和必须 = 100.00（应用层校验）。

#### 4.6.3 `standard_rule` — 评分规则

| 属性 | 值 |
|---|---|
| **表用途** | 存储每个评价维度下的具体评分规则（如"变量命名不符合驼峰规范扣 2 分"） |
| **数据来源** | 创建/编辑评价标准时一起配置 |
| **关联表** | 关联 standard_dimension(id) |
| **数据生命周期** | 随所属维度保留 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键，规则 ID |
| `dimension_id` | `BIGINT` | ❌ | — | 所属维度 ID |
| `rule_name` | `VARCHAR(256)` | ❌ | — | 规则名称 |
| `rule_type` | `VARCHAR(8)` | ❌ | `AUTO` | 检测类型：AUTO（自动检测）, MANUAL（人工判断） |
| `check_method` | `VARCHAR(32)` | ✅ | NULL | 检测方法：REGEX（正则匹配）, FILE_EXISTS（文件存在）, DIR_STRUCT（目录结构）, AI_SEMANTIC（AI 语义分析） |
| `check_config` | `JSON` | ✅ | NULL | 检测配置（JSON 格式，不同 check_method 有不同的字段结构） |
| `max_deduct` | `DECIMAL(5,2)` | ❌ | `0.00` | 本条规则最大扣分值 |
| `severity` | `VARCHAR(16)` | ❌ | `MINOR` | 扣分严重级别：MINOR, MAJOR, CRITICAL |
| `enabled` | `TINYINT` | ❌ | `1` | 是否启用：1=启用, 0=禁用 |
| `sort_order` | `INT` | ❌ | `0` | 排序号 |
| `create_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 最后修改时间 |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `idx_dimension_id` | `dimension_id` | 普通 | 按维度查询规则 |
| `idx_check_method` | `check_method` | 普通 | 按检测方法筛选（AI 分析选择规则时使用） |

### 4.7 教师复核与成绩

#### 4.7.1 `teacher_review` — 教师复核

| 属性 | 值 |
|---|---|
| **表用途** | 记录教师对 AI 分析结果的复核过程和结论 |
| **数据来源** | 教师在复核工作台逐项复核后创建/更新 |
| **关联表** | 关联 submission(id) 和 user(id)（教师），被 review_item 引用 |
| **数据生命周期** | 长期保留，作为成绩发布依据和争议追溯凭证 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键，复核 ID |
| `submission_id` | `BIGINT` | ❌ | — | 关联的提交 ID（一对一） |
| `reviewer_id` | `BIGINT` | ❌ | — | 复核教师 ID |
| `teacher_comment` | `TEXT` | ✅ | NULL | 教师评语（个性化反馈） |
| `status` | `VARCHAR(16)` | ❌ | `PENDING` | 状态：PENDING（待复核）, REVIEWING（复核中）, PUBLISHED（已发布）, RETURNED（退回重提） |
| `review_time` | `DATETIME` | ✅ | NULL | 复核开始时间 |
| `publish_time` | `DATETIME` | ✅ | NULL | 成绩发布时间 |
| `version` | `INT` | ❌ | `0` | 乐观锁 |
| `create_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 最后修改时间 |
| `create_by` | `BIGINT` | ✅ | NULL | 创建人 ID（教师） |
| `update_by` | `BIGINT` | ✅ | NULL | 最后修改人 ID |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `uk_review_submission` | `submission_id` | UNIQUE | 一次提交一条复核 |
| `idx_reviewer_id` | `reviewer_id` | 普通 | 按教师查询待复核列表 |
| `idx_status` | `status` | 普通 | 按状态筛选（PENDING 查询频率最高） |

#### 4.7.2 `review_item` — 复核意见

| 属性 | 值 |
|---|---|
| **表用途** | 记录教师对每条 AI 扣分项的复核意见（采纳/拒绝/调整），是教师复核的操作记录 |
| **数据来源** | 教师逐项复核操作 |
| **关联表** | 关联 teacher_review(id) 和 ai_analysis_detail(id) |
| **数据生命周期** | 随复核记录保留 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键 |
| `review_id` | `BIGINT` | ❌ | — | 所属复核 ID |
| `ai_detail_id` | `BIGINT` | ✅ | NULL | 关联的 AI 扣分项 ID（手动加扣时可为 NULL） |
| `dimension_id` | `BIGINT` | ✅ | NULL | 关联的评价维度 ID |
| `action` | `VARCHAR(8)` | ❌ | — | 复核操作：ADOPT（采纳）, REJECT（拒绝）, ADJUST（调整）, MANUAL_ADD（手动添加） |
| `deduct_score` | `DECIMAL(5,2)` | ❌ | `0.00` | 最终扣分值 |
| `reason` | `VARCHAR(1024)` | ✅ | NULL | 复核理由（教师补充说明） |
| `is_manual` | `TINYINT` | ❌ | `0` | 是否为教师手动添加的扣分项：0=AI 产生的, 1=教师手动添加的 |
| `create_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 创建时间 |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `idx_review_id` | `review_id` | 普通 | 按复核查询所有复核项 |
| `idx_ai_detail_id` | `ai_detail_id` | 普通 | 按 AI 扣分项查询复核意见 |

#### 4.7.3 `score_record` — 最终成绩

| 属性 | 值 |
|---|---|
| **表用途** | 存储教师确认后的最终成绩，是不可更改的成绩记录（仅可退回重新发布） |
| **数据来源** | 教师发布复核时生成 |
| **关联表** | 关联 submission(id) 和 user(id)（学生） |
| **数据生命周期** | 永久保留（成绩档案） |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键 |
| `submission_id` | `BIGINT` | ❌ | — | 关联的提交 ID（一对一） |
| `user_id` | `BIGINT` | ❌ | — | 学生 ID（冗余，方便个人报表查询） |
| `training_task_id` | `BIGINT` | ❌ | — | 任务 ID（冗余，方便班级报表查询） |
| `total_score` | `DECIMAL(5,2)` | ❌ | `0.00` | 最终总分（教师确认后的最终分值） |
| `ai_total_score` | `DECIMAL(5,2)` | ✅ | NULL | AI 原始总分（对比教师最终分用） |
| `score_details` | `JSON` | ✅ | NULL | 各维度得分快照（JSON），包含 `dimension_name, weight, score` |
| `teacher_comment` | `TEXT` | ✅ | NULL | 教师评语（冗余，方便快速查询） |
| `status` | `VARCHAR(16)` | ❌ | `PUBLISHED` | 状态：PUBLISHED（已发布）, RETURNED（已退回） |
| `publish_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 发布时间 |
| `version` | `INT` | ❌ | `0` | 乐观锁 |
| `create_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 最后修改时间 |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `uk_score_submission` | `submission_id` | UNIQUE | 一次提交一条最终成绩 |
| `idx_user_id` | `user_id` | 普通 | 按学生查询所有成绩（个人报表） |
| `idx_training_task_id` | `training_task_id` | 普通 | 按任务查询所有成绩（班级报表） |
| `idx_user_task` | `(user_id, training_task_id)` | 联合 | 学生+任务联合查询 |

### 4.8 报表与统计

#### 4.8.1 `report` — 报表记录

| 属性 | 值 |
|---|---|
| **表用途** | 记录已生成的报表元数据，报表文件存储在 MinIO |
| **数据来源** | 用户点击导出时生成 |
| **关联表** | 关联 user(id), training_task(id), file_storage(id) |
| **数据生命周期** | 90 天过期自动清理 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键 |
| `user_id` | `BIGINT` | ❌ | — | 生成报表的用户 ID |
| `training_task_id` | `BIGINT` | ✅ | NULL | 关联任务 ID（任务报表时） |
| `report_type` | `VARCHAR(16)` | ❌ | — | 报表类型：STUDENT（个人）, CLASS（班级）, COLLEGE（学院） |
| `format` | `VARCHAR(8)` | ❌ | — | 文件格式：PDF, EXCEL |
| `report_params` | `JSON` | ✅ | NULL | 生成参数快照（JSON）：{className, semester, filters...} |
| `file_id` | `BIGINT` | ✅ | NULL | 关联的文件存储 ID（报表文件） |
| `status` | `VARCHAR(16)` | ❌ | `GENERATING` | 状态：GENERATING（生成中）, COMPLETED（已完成）, FAILED（失败） |
| `create_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 生成时间 |
| `create_by` | `BIGINT` | ✅ | NULL | 生成人 ID |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `idx_user_id` | `user_id` | 普通 | 按用户查询报表历史 |
| `idx_report_type` | `report_type` | 普通 | 按报表类型筛选 |
| `idx_create_time` | `create_time` | 普通 | 按生成时间排序 |

#### 4.8.2 `statistics_snapshot` — 统计快照

| 属性 | 值 |
|---|---|
| **表用途** | 存储定时生成的统计数据快照，避免仪表盘实时聚合查询（每 5 分钟刷新一次） |
| **数据来源** | 定时任务从 submission、score_record、ai_analysis 聚合 |
| **关联表** | 关联 training_task(id), class(id) |
| **数据生命周期** | 保留最近 30 天，过期自动删除 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键 |
| `training_task_id` | `BIGINT` | ✅ | NULL | 关联任务 ID（NULL=全局统计） |
| `class_id` | `BIGINT` | ✅ | NULL | 关联班级 ID（NULL=全院统计） |
| `snapshot_type` | `VARCHAR(32)` | ❌ | — | 快照类型：DASHBOARD（仪表盘）, CLASS_PROGRESS（班级进度）, AI_QUALITY（AI 质量） |
| `snapshot_data` | `JSON` | ❌ | — | 快照数据（JSON 格式，存储聚合后的统计指标） |
| `snapshot_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 快照时间 |
| `create_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 创建时间 |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `idx_snapshot_query` | `(snapshot_type, training_task_id, snapshot_time)` | 联合 | 仪表盘快照查询核心索引 |
| `idx_snapshot_time` | `snapshot_time` | 普通 | 清理过期快照 |

### 4.9 系统管理

#### 4.9.1 `system_config` — 系统配置

| 属性 | 值 |
|---|---|
| **表用途** | 存储系统级别的配置参数（系统名称、学期、上传限制等） |
| **数据来源** | 管理员通过系统配置页面修改 |
| **关联表** | 无 |
| **数据生命周期** | 永久保留（单行配置键值对） |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键 |
| `config_key` | `VARCHAR(64)` | ❌ | — | 配置项 Key：system_name, current_semester, max_upload_size 等 |
| `config_value` | `VARCHAR(512)` | ❌ | — | 配置项 Value |
| `config_type` | `VARCHAR(16)` | ❌ | `STRING` | 值类型：STRING, NUMBER, BOOLEAN, JSON |
| `description` | `VARCHAR(256)` | ✅ | NULL | 配置说明 |
| `version` | `INT` | ❌ | `0` | 乐观锁 |
| `create_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 最后修改时间 |
| `update_by` | `BIGINT` | ✅ | NULL | 最后修改人 ID |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `uk_config_key` | `config_key` | UNIQUE | 配置 Key 唯一 |

**预置配置项**：

| config_key | config_value | config_type | 说明 |
|---|---|---|---|
| `system_name` | B1 智慧实训平台 | STRING | 系统名称 |
| `current_semester` | 2025-2026-2 | STRING | 当前学期 |
| `semester_start` | 2026-02-24 | STRING | 学期开始日期 |
| `semester_end` | 2026-07-10 | STRING | 学期结束日期 |
| `max_upload_size` | 50 | NUMBER | 最大上传大小（MB） |
| `ai_model_version` | v2.1.0 | STRING | AI 模型版本 |
| `auto_analyze` | true | BOOLEAN | 提交后自动 AI 分析 |
| `notification_enabled` | true | BOOLEAN | 启用系统通知 |
| `maintenance_mode` | false | BOOLEAN | 维护模式 |

#### 4.9.2 `operation_log` — 操作日志

| 属性 | 值 |
|---|---|
| **表用途** | 记录全平台用户操作日志，支持管理员审计和问题追溯 |
| **数据来源** | AOP 切面自动拦截 @OperationLog 注解的方法 |
| **关联表** | 关联 user(id) |
| **数据生命周期** | 永久保留（满足合规审计要求），可手动归档 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键 |
| `user_id` | `BIGINT` | ✅ | NULL | 操作用户 ID（系统自动操作为 NULL） |
| `username` | `VARCHAR(64)` | ✅ | NULL | 操作用户名（冗余，用户被删除后仍可识别） |
| `module` | `VARCHAR(32)` | ❌ | — | 操作模块：AUTH, COURSE, TRAINING, SUBMISSION, AI, REVIEW, REPORT, SYSTEM |
| `operation` | `VARCHAR(32)` | ❌ | — | 操作类型：LOGIN, LOGOUT, CREATE, UPDATE, DELETE, PUBLISH, EXPORT, UPLOAD |
| `target_type` | `VARCHAR(32)` | ✅ | NULL | 操作对象类型：User, Course, Task, Submission 等 |
| `target_id` | `BIGINT` | ✅ | NULL | 操作对象 ID |
| `detail` | `JSON` | ✅ | NULL | 操作详情（JSON 格式，存储变更前后的值对比） |
| `ip_address` | `VARCHAR(64)` | ✅ | NULL | 操作 IP 地址 |
| `user_agent` | `VARCHAR(512)` | ✅ | NULL | 浏览器 User-Agent |
| `duration_ms` | `INT` | ✅ | NULL | 操作耗时（毫秒） |
| `result` | `VARCHAR(8)` | ❌ | `SUCCESS` | 操作结果：SUCCESS, FAILURE |
| `error_msg` | `VARCHAR(1024)` | ✅ | NULL | 失败原因（result=FAILURE 时） |
| `create_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 日志产生时间 |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `idx_user_id` | `user_id` | 普通 | 按用户查询操作记录 |
| `idx_module` | `module` | 普通 | 按模块筛选（管理员查看日志） |
| `idx_create_time` | `create_time` | 普通 | 按时间范围查询和排序 |
| `idx_result` | `result` | 普通 | 按操作结果筛选（查询失败操作） |
| `idx_module_time` | `(module, create_time)` | 联合 | 按模块+时间联合查询 |

#### 4.9.3 `notification` — 系统通知

| 属性 | 值 |
|---|---|
| **表用途** | 存储系统推送给用户的通知消息（成绩发布、任务提醒等） |
| **数据来源** | 业务事件触发（ReviewPublishedEvent → NotificationListener） |
| **关联表** | 关联 user(id) |
| **数据生命周期** | 已读通知 90 天自动清理，未读通知永久保留 |

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|---|---|---|---|---|
| `id` | `BIGINT` | ❌ | Snowflake | 主键 |
| `user_id` | `BIGINT` | ❌ | — | 接收用户 ID |
| `title` | `VARCHAR(256)` | ❌ | — | 通知标题 |
| `content` | `TEXT` | ❌ | — | 通知正文 |
| `notify_type` | `VARCHAR(16)` | ❌ | `SYSTEM` | 通知类型：REMIND（提交提醒）, SCORE（成绩通知）, SYSTEM（系统通知） |
| `is_read` | `TINYINT` | ❌ | `0` | 是否已读：0=未读, 1=已读 |
| `read_time` | `DATETIME` | ✅ | NULL | 阅读时间 |
| `target_type` | `VARCHAR(32)` | ✅ | NULL | 关联业务类型（点击通知可跳转） |
| `target_id` | `BIGINT` | ✅ | NULL | 关联业务 ID |
| `create_time` | `DATETIME` | ❌ | CURRENT_TIMESTAMP | 通知创建时间 |

**索引设计**：

| 索引名 | 字段 | 类型 | 说明 |
|---|---|---|---|
| `PRIMARY` | `id` | 主键 | — |
| `idx_user_unread` | `(user_id, is_read)` | 联合 | 查询用户未读通知列表（最高频查询） |
| `idx_create_time` | `create_time` | 普通 | 按创建时间排序 |

---

## 5. Index Strategy

### 5.1 索引设计总览

| 表 | 主键 | 唯一索引 | 普通索引 | 联合索引 |
|---|---|---|---|---|
| user | id | uk_username (username) | idx_status, idx_real_name, idx_create_time | — |
| role | id | uk_role_code (role_code) | — | — |
| user_role | id | uk_user_role (user_id, role_id) | idx_role_id | — |
| course | id | uk_course_code (course_code) | idx_semester, idx_status, idx_create_by | — |
| class | id | uk_class_code (class_code) | idx_grade, idx_major | — |
| course_teacher | id | uk_course_teacher (course_id, user_id) | idx_user_id | — |
| course_student | id | uk_course_student (course_id, user_id) | idx_user_id, idx_class_id | — |
| course_class | id | uk_course_class (course_id, class_id) | — | — |
| training_task | id | — | idx_course_id, idx_status, idx_end_time, idx_create_by | idx_course_status (course_id, status) |
| training_class | id | uk_training_class (training_id, class_id) | idx_class_id | — |
| submission | id | uk_submission_user_task (user_id, training_task_id) | idx_training_task_id, idx_user_id, idx_status, idx_submit_time | — |
| submission_file | id | — | idx_submission_id | — |
| file_storage | id | uk_object_key (object_key) | idx_bucket, idx_file_md5, idx_expire_time | — |
| ai_analysis | id | uk_ai_submission (submission_id) | idx_analysis_status, idx_create_time | — |
| ai_analysis_detail | id | — | idx_ai_analysis_id, idx_agent_type, idx_severity, idx_is_adopted | — |
| evaluation_standard | id | — | idx_status, idx_is_template, idx_create_by | — |
| standard_dimension | id | — | idx_standard_id | — |
| standard_rule | id | — | idx_dimension_id, idx_check_method | — |
| teacher_review | id | uk_review_submission (submission_id) | idx_reviewer_id, idx_status | — |
| review_item | id | — | idx_review_id, idx_ai_detail_id | — |
| score_record | id | uk_score_submission (submission_id) | idx_user_id, idx_training_task_id | idx_user_task (user_id, training_task_id) |
| report | id | — | idx_user_id, idx_report_type, idx_create_time | — |
| statistics_snapshot | id | — | idx_snapshot_time | idx_snapshot_query (snapshot_type, training_task_id, snapshot_time) |
| system_config | id | uk_config_key (config_key) | — | — |
| operation_log | id | — | idx_user_id, idx_module, idx_create_time, idx_result | idx_module_time (module, create_time) |
| notification | id | — | idx_create_time | idx_user_unread (user_id, is_read) |

### 5.2 查询优化策略

#### 5.2.1 高频查询 TOP 10

| # | 查询场景 | SQL 模式 | 使用索引 | 优化手段 |
|---|---|---|---|---|
| 1 | 教师查看待复核列表 | `WHERE status = 'PENDING' AND reviewer_id = ?` | idx_reviewer_id + idx_status | 联合索引 + 分页 |
| 2 | 学生查看自己的任务列表 | `WHERE user_id = ? AND status = 'PUBLISHED' ORDER BY create_time` | idx_course_student.user_id → idx_training_task.status | 先定位课程 → 再查任务 |
| 3 | 教师查看班级提交进度 | `SELECT COUNT(*) FROM submission WHERE training_task_id = ? GROUP BY status` | idx_training_task_id | 统计快照缓存，避免实时聚合 |
| 4 | 管理员查看操作日志 | `WHERE module = ? AND create_time BETWEEN ? AND ? ORDER BY create_time` | idx_module_time | 分页 + 时间范围限制（最多 30 天） |
| 5 | 学生查看提交历史 | `WHERE user_id = ? ORDER BY submit_time DESC` | idx_user_id + idx_submit_time | 分页 |
| 6 | 教师查看某任务全部提交 | `WHERE training_task_id = ? ORDER BY submit_time` | idx_training_task_id + idx_submit_time | 分页 |
| 7 | 用户登录 | `WHERE username = ?` | uk_username | 唯一索引，常量时间 |
| 8 | 仪表盘统计数据 | 多表聚合 COUNT, AVG, SUM | 各表相关索引 | **使用 statistics_snapshot 表替代实时查询** |
| 9 | AI 分析查询 PENDING 任务 | `WHERE analysis_status = 'PENDING' ORDER BY create_time LIMIT 10` | idx_analysis_status | 任务队列轮询 |
| 10 | 报表导出数据查询 | 多表 JOIN + GROUP BY + 聚合 | idx_user_id, idx_training_task_id | 异步生成，结果缓存 Redis |

#### 5.2.2 分页优化

**问题**：传统 `LIMIT offset, size` 在 offset 较大时性能急剧下降（需要扫描 offset + size 行）。

**优化方案**：

1. **游标分页（Cursor Pagination）**：对于按时间排序的列表（提交列表、操作日志），使用 `WHERE create_time < ? ORDER BY create_time DESC LIMIT ?` 替代 `LIMIT offset, size`。前端传递上一页最后一条记录的 `create_time` 作为游标。

2. **覆盖索引**：分页查询的 ORDER BY 和 WHERE 列完全被索引覆盖，避免回表查询。

3. **限制页数**：最大允许查询 100 页（page × pageSize ≤ 2000 条），超出提示用户细化筛选条件。

4. **延迟关联**：对于 JOIN 多表的分页查询，先在索引上定位主键 ID 列表，再 JOIN 获取完整数据。

#### 5.2.3 统计查询优化

| 统计场景 | 直接查询方案 | 优化方案 |
|---|---|---|
| 仪表盘用户总数 | `SELECT COUNT(*) FROM user` | statistics_snapshot 定时刷新（5 分钟） |
| 班级提交率 | `SELECT COUNT(*) FROM submission ... GROUP BY status` | statistics_snapshot 定时刷新 |
| 全院平均分 | `SELECT AVG(total_score) FROM score_record` | statistics_snapshot 定时刷新 |
| 学生成绩趋势 | 多表聚合查询 | Redis 缓存（TTL 30 分钟）+ 异步刷新 |

---

## 6. Transaction Design

### 6.1 需要事务的业务

| 业务 | 涉及表 | 隔离级别 | 超时 | 说明 |
|---|---|---|---|---|
| **用户创建 + 角色分配** | user + user_role | READ_COMMITTED | 10s | 用户和角色必须原子写入。任一失败则全部回滚 |
| **课程创建 + 教师/班级关联** | course + course_teacher + course_class + course_student | READ_COMMITTED | 15s | 课程创建、教师分配、班级关联、学生批量导入为一个原子操作 |
| **任务发布 + 班级分发** | training_task + training_class | READ_COMMITTED | 10s | 状态从 DRAFT→PUBLISHED，同时写入分发班级 |
| **提交创建 + 文件关联** | submission + submission_file | READ_COMMITTED | 10s | 提交记录和文件清单原子写入 |
| **教师评分发布** | teacher_review + review_item + score_record + submission(更新状态) | READ_COMMITTED | 15s | 复核结论、评分明细、最终成绩、提交状态四者原子更新 |
| **评价标准创建 + 维度 + 规则** | evaluation_standard + standard_dimension + standard_rule | READ_COMMITTED | 15s | 三层嵌套结构的原子写入 |
| **文件上传 + 存储记录** | file_storage | READ_COMMITTED | 5s | 文件上传到 MinIO 成功后再写数据库记录，失败则删除 MinIO 文件（补偿事务） |

### 6.2 不能开启事务的业务（带原因和补偿方案）

| 业务 | 涉及系统 | 原因 | 补偿方案 |
|---|---|---|---|
| **AI 分析** | LLM API (30-120s) | 事务持有数据库连接时间过长，阻塞连接池 | 1. 先在单独事务中写入 ai_analysis（status=PENDING）<br>2. 异步调用 LLM<br>3. 结果返回后在单独事务中更新 ai_analysis + 写入 ai_analysis_detail<br>4. LLM 调用失败时标记 FAILED，记录重试次数 |
| **文件上传到 MinIO** | MinIO | MinIO 不支持 XA/2PC 分布式事务 | 1. 先上传到 MinIO<br>2. 上传成功 → 写入 file_storage 记录<br>3. 上传失败 → 不写入记录<br>4. 写入 DB 失败 → 删除 MinIO 文件 + 重试 |
| **Git 仓库克隆** | Git 服务器 | 网络 I/O，超时不可控 | 1. 不开启事务<br>2. 克隆到临时目录<br>3. 克隆成功 → 写入提交信息<br>4. 克隆失败 → 返回错误信息，不写入数据库 |
| **报表 PDF/Excel 生成** | 本地文件系统 / MinIO | CPU 密集型操作，耗时较长 | 1. 写入 report 记录（status=GENERATING）<br>2. 异步生成文件<br>3. 上传 MinIO → 更新 report（status=COMPLETED, file_id）<br>4. 生成失败 → 更新 report（status=FAILED） |
| **通知推送** | 用户浏览器 | 推送失败不应回滚业务操作 | 1. 业务操作完成后发布事件<br>2. 异步插入 notification 记录<br>3. 推送失败 → 记录已保留在数据库，用户下次登录可查看 |

### 6.3 事务隔离级别选择

| 隔离级别 | 防脏读 | 防不可重复读 | 防幻读 | 并发性能 | 选择 |
|---|---|---|---|---|---|
| READ UNCOMMITTED | ❌ | ❌ | ❌ | 最高 | ❌（脏读不可接受） |
| READ COMMITTED | ✅ | ❌ | ❌ | 高 | ✅ **默认选择** |
| REPEATABLE READ | ✅ | ✅ | ❌（InnoDB 通过 Gap Lock 防） | 中 | 仅在需要一致性读的场景使用 |
| SERIALIZABLE | ✅ | ✅ | ✅ | 最低 | ❌（性能太差） |

**REPEATABLE READ 适用场景**：
- 教师评分发布：确保读取的 AI 分析结果在事务期间不被修改
- 成绩计算：确保 `SELECT SUM(deduct_score) FROM review_item WHERE review_id = ?` 的结果在事务期间一致

```java
@Transactional(isolation = Isolation.REPEATABLE_READ, timeout = 15)
public void publishReview(Long reviewId, String comment) {
    // 1. 查询复核结论
    // 2. 计算最终分数
    // 3. 写入 score_record
    // 4. 更新 teacher_review 状态
}
```

---

## 7. Data Security

### 7.1 密码安全

| 策略 | 实现 |
|---|---|
| **加密算法** | BCrypt（strength=10，即 2^10=1024 次迭代） |
| **存储位置** | `user.password` VARCHAR(256)，存储 BCrypt 哈希值 |
| **传输安全** | HTTPS 加密传输；密码从不明文存储在前端（登录后销毁） |
| **密码强度** | 最少 8 位，包含大写字母、小写字母、数字（应用层校验） |
| **密码重置** | 管理员重置 → 生成随机密码 → 用户首次登录强制修改 |
| **Token 安全** | Sa-Token UUID 格式；有效期 2 小时；Refresh Token 7 天滚动刷新 |

**为什么不使用 MD5 / SHA-256**：
- MD5 已被破解：彩虹表可以在秒级时间内反查 80% 以上的常见密码
- SHA-256 是快速哈希：攻击者可以用 GPU 每秒尝试数十亿次
- BCrypt 是慢速哈希：通过 strength 参数控制迭代次数，将攻击成本提高数万倍
- BCrypt 内置 Salt：防止相同密码产生相同哈希值

### 7.2 Token 安全

| 策略 | 实现 |
|---|---|
| **Token 格式** | UUID（128 位随机字符串），不可推测 |
| **Token 传输** | `Authorization: Bearer {token}` 请求头，非 Cookie |
| **Token 存储（服务端）** | Redis `token:access:{uuid}` → userId，TTL 2 小时 |
| **Token 刷新** | Redis `token:refresh:{userId}` → uuid，TTL 7 天。刷新时旧 Token 加入黑名单 |
| **Token 黑名单** | Redis `token:blacklist:{uuid}` → 1，TTL = Token 剩余有效时间 |
| **Token 泄露应对** | 服务端可主动删除 Redis 中的 Token 记录使 Token 失效 |

### 7.3 文件安全

| 策略 | 实现 |
|---|---|
| **访问控制** | 文件下载/预览通过后端接口中转（不暴露 MinIO 直接 URL）。后端校验当前用户是否有权访问该文件 |
| **预签名 URL** | MinIO 生成带签名的临时 URL（有效期 15 分钟），不允许永久公开访问 |
| **上传校验** | 文件类型白名单（扩展名 + 魔数双重校验）；文件大小上限 50MB；压缩炸弹防护（检查解压后总大小） |
| **文件隔离** | 不同 bucket 物理隔离（submissions / reports / temp / avatars） |
| **病毒扫描** | P1 集成 ClamAV（预留异步扫描接口 `VirusScanListener`） |

### 7.4 日志安全

| 策略 | 实现 |
|---|---|
| **密码不记录** | 登录请求的 password 字段不能出现在任何日志中 |
| **Token 不记录** | 请求头中的 Authorization 不输出到日志 |
| **文件内容不记录** | 上传文件的二进制内容和文本内容不输出到日志 |
| **日志访问控制** | 操作日志查询仅限 admin 角色 |
| **日志防篡改** | operation_log 表只有 INSERT 权限（应用层），无 UPDATE/DELETE 权限 |

### 7.5 敏感字段脱敏

| 字段 | 存储方式 | 展示方式 | 日志 |
|---|---|---|---|
| `password` | BCrypt 哈希（单向） | 不展示（永远不返回给前端） | 不记录 |
| `email` | 明文 | 完整展示（仅用户本人和管理员可见） | 部分掩码 `j***@***.com` |
| `phone` | 明文 | 完整展示（仅用户本人和管理员可见） | 部分掩码 `138****5678` |
| `real_name` | 明文 | 完整展示（教师和管理员可见） | 不脱敏 |
| Token | Redis（服务端） | 不展示（仅传输使用） | 不记录 |
| IP 地址 | 明文 | 完整展示（管理员可见） | 不脱敏 |

### 7.6 SQL 注入防护

| 层面 | 防护措施 |
|---|---|
| **应用层** | MyBatis `#{}` 预编译参数（使用 PreparedStatement），100% 防止 SQL 注入 |
| **禁止行为** | 禁止在 Mapper XML 中使用 `${}` 拼接用户输入（`${}` 仅用于动态表名/列名等非输入场景） |
| **输入校验** | 所有用户输入在 Controller 层经过 `@Valid` / `@Validated` 校验 |
| **最小权限** | 应用连接数据库的账号仅拥有 DML 权限（SELECT, INSERT, UPDATE, DELETE），无 DDL 权限（CREATE, ALTER, DROP 由 Flyway 迁移工具使用独立账号执行） |

---

## 8. Performance

### 8.1 冷热数据分离

| 分类 | 定义 | 示例 | 存储策略 |
|---|---|---|---|
| **热数据** | 当前学期，频繁读写 | 当前学期课程、进行中的任务、待复核的提交 | MySQL 主表，充分索引，Redis 缓存 |
| **温数据** | 近 1-2 年，偶尔查询 | 历史学期课程、已归档任务、已发布成绩 | MySQL 主表，较少索引（无频繁写入），按需查询 |
| **冷数据** | 2 年前，几乎不查询 | 毕业生数据、过期报表、旧操作日志 | 归档表（`_archive` 后缀）或独立归档数据库；或导出为文件存储到 MinIO |

### 8.2 数据归档策略

| 归档对象 | 归档条件 | 归档方式 | 频率 |
|---|---|---|---|
| **操作日志** | `create_time < NOW() - INTERVAL 1 YEAR` | 迁移到 `operation_log_archive` 表（独立数据库） | 每月 1 次 |
| **通知** | `is_read = 1 AND create_time < NOW() - INTERVAL 90 DAY` | 物理删除 | 每天 |
| **报表文件** | `create_time < NOW() - INTERVAL 90 DAY` | 删除 MinIO 文件 + file_storage 记录 + report 记录 | 每天 |
| **过期文件** | `expire_time < NOW()` | 删除 MinIO 文件 + file_storage 记录 | 每天 |
| **统计快照** | `snapshot_time < NOW() - INTERVAL 30 DAY` | 物理删除 | 每天 |

### 8.3 读写分离（P1）

**MVP 阶段**：单 MySQL 实例，所有读写走同一连接。

**P1 扩容**：MySQL 主从复制 + MyBatis Plus 多数据源配置：

```yaml
spring:
  datasource:
    master:  # 写库
      url: jdbc:mysql://master:3306/b1
    slave:   # 读库
      url: jdbc:mysql://slave:3306/b1
```

- 写操作（INSERT/UPDATE/DELETE）：走 master
- 读操作（SELECT）：走 slave
- 实时性要求高的读（登录后立即查询权限）：走 master

### 8.4 缓存策略

| 缓存对象 | Redis Key | TTL | 更新策略 |
|---|---|---|---|
| 用户信息 | `user:info:{userId}` | 30 min | 用户修改信息时主动删除 |
| 用户权限 | `user:perm:{userId}` | 30 min | 权限变更时主动删除 |
| 评价标准 | `standard:{standardId}` | 1 hour | 标准修改时主动删除 |
| 课程信息 | `course:{courseId}` | 1 hour | 课程修改时主动删除 |
| 仪表盘统计 | `stats:dashboard:{role}` | 5 min | 短 TTL 自然过期 |
| 任务进度 | `stats:progress:{trainingTaskId}` | 5 min | 短 TTL 自然过期 |
| 报表数据 | `report:{reportType}:{paramsHash}` | 30 min | 成绩发布时批量失效 |
| AI 分析状态 | `ai:status:{submissionId}` | 1 hour | 分析完成时更新 |

### 8.5 连接池配置

| 参数 | 值 | 说明 |
|---|---|---|
| `maximum-pool-size` | 20 | 最大连接数 |
| `minimum-idle` | 5 | 最小空闲连接 |
| `idle-timeout` | 300000ms (5 min) | 空闲超时 |
| `max-lifetime` | 1800000ms (30 min) | 连接最大存活时间 |
| `connection-timeout` | 30000ms (30s) | 等待连接超时 |
| `leak-detection-threshold` | 60000ms (60s) | 连接泄露检测 |

### 8.6 慢查询监控

| 参数 | 值 | 说明 |
|---|---|---|
| `slow_query_log` | ON | 开启慢查询日志 |
| `long_query_time` | 0.2 (200ms) | 慢查询阈值 |
| `log_queries_not_using_indexes` | ON | 记录未使用索引的查询 |

每 Sprint 结束时检查慢查询日志，优化高频慢查询。

---

## 9. Future Scalability

### 9.1 RAG 知识库扩展（P1）

**新增表**：

| 表名 | 用途 |
|---|---|
| `knowledge_base` | 知识库基本信息（名称、类型、关联课程） |
| `knowledge_document` | 知识库文档（关联 file_storage，记录索引状态） |
| `document_chunk` | 文档分块（文本片段 + Embedding 向量） |

**Design considerations**:
- `document_chunk.embedding` 列类型：`JSON`（存储 float 数组）或使用 MySQL 8.0 的 `VECTOR` 类型（如果安装了向量索引插件），或使用专用向量数据库（Milvus / PGVector）
- 分块策略：固定大小（512 tokens）+ 重叠（64 tokens），chunk 元数据记录 source_document_id 和位置
- 检索流程：用户查询 → Embedding → 向量相似度搜索 → Top-K 文档块 → 注入 AI Prompt

### 9.2 Agent 扩展（P1）

**修改表**：

| 变更 | 说明 |
|---|---|
| `ai_analysis_detail.agent_type` | 扩展枚举：在 DOC/CODE/REQ 基础上增加 SECURITY（安全检测）、PLAGIARISM（查重） |
| `standard_rule.check_method` | 扩展枚举：增加 VECTOR_SIMILARITY（向量相似度匹配）、LLM_CHAIN（LangChain 链式检测） |

**新增表**：

| 表名 | 用途 |
|---|---|
| `agent_execution_log` | Agent 执行日志（agent_type, execution_time_ms, input_tokens, output_tokens, status） |

### 9.3 MOSS 查重集成（P1）

**新增表**：

| 表名 | 用途 |
|---|---|
| `plagiarism_check` | 查重任务记录（submission_id, status, similarity_percentage） |
| `plagiarism_match` | 查重匹配明细（source_submission_id, target_submission_id, matched_lines, similarity_score） |

**扩展字段**：
- `submission` 表增加 `plagiarism_status` 字段（PENDING / CHECKING / COMPLETED / FLAGGED）
- `score_record` 表预留 `plagiarism_deduct` 字段（查重扣分，默认 0.00）

### 9.4 知识图谱扩展（P2）

**新增表**：

| 表名 | 用途 |
|---|---|
| `knowledge_point` | 知识点节点（名称、类型、父节点、难度等级） |
| `student_knowledge_state` | 学生知识点掌握状态（user_id, knowledge_point_id, mastery_level, last_updated） |
| `knowledge_point_relation` | 知识点之间的依赖关系（前置知识点、关联知识点） |

### 9.5 多学校 / 多租户扩展

#### 9.5.1 方案：Shared Database, Shared Schema + tenant_id 隔离

**当前预留**：所有主数据表预留 `tenant_id BIGINT DEFAULT 1` 字段。

**扩展方式**：
1. 新增 `tenant` 表（学校/机构信息）
2. 所有业务查询追加 `WHERE tenant_id = ?`（通过 MyBatis Plus 租户插件自动注入）
3. `user.username` 唯一约束从全局唯一改为 `(tenant_id, username)` 联合唯一

**租户数据隔离级别**：

| 表 | 隔离方式 | 说明 |
|---|---|---|
| 核心业务表（user, course, submission, score_record 等） | tenant_id 隔离 | 每个学校只能看到自己的数据 |
| 系统配置表（system_config） | tenant_id 隔离 | 每个学校可自定义系统参数 |
| 全局共享表（role, ai_analysis 模型配置） | 全局共享 | 所有学校共用 |

#### 9.5.2 多学院 / 多专业扩展

**当前已有**：`class.major` 和 `class.department` 字段记录专业和院系。

**扩展方式**：
1. 新增 `department` 表（学院信息）
2. 新增 `major` 表（专业信息）
3. `class.department` 和 `class.major` 改为外键引用
4. 报表模块支持按学院/专业聚合

### 9.6 分库分表（大规模扩展）

**触发条件**（以下任一）：
- 单表记录数 > 5000 万（如 operation_log、submission）
- 单库 QPS > 10,000
- 单库存储 > 500GB

**分片策略**：

| 表 | 分片键 | 分片算法 | 理由 |
|---|---|---|---|
| user | id | HASH | 按用户 ID 均匀分布 |
| submission | user_id | HASH | 按学生 ID 分片（查询主要按学生进行） |
| operation_log | create_time | RANGE（按年） | 按时间分片便于归档 |
| ai_analysis | submission_id | HASH | 同 submission 共分片 |
| score_record | user_id | HASH | 同 user 共分片 |

**工具**：Apache ShardingSphere（ShardingSphere-JDBC 内嵌模式，无需额外部署中间件）。

---

## Appendix A: 全量表清单

| # | 表名 | 表类型 | 核心索引数 | 预估行数（学期） | 增长速率 |
|---|---|---|---|---|---|
| 1 | `user` | 主数据 | 5 | ~500 | 每学期 +100 |
| 2 | `role` | 主数据 | 2 | 3 | 几乎不变 |
| 3 | `user_role` | 关联表 | 2 | ~500 | 与 user 同步 |
| 4 | `course` | 主数据 | 4 | ~30 | 每学期 +15 |
| 5 | `class` | 主数据 | 3 | ~20 | 每学期 +10 |
| 6 | `course_teacher` | 关联表 | 2 | ~60 | 与 course 同步 |
| 7 | `course_student` | 关联表 | 3 | ~500 | 每学期 +100 |
| 8 | `course_class` | 关联表 | 2 | ~60 | 与 course 同步 |
| 9 | `training_task` | 事务表 | 5 | ~50 | 每学期 +25 |
| 10 | `training_class` | 关联表 | 2 | ~100 | 与 task 同步 |
| 11 | `submission` | 事务表 | 5 | ~2000 | 每学期 +1000 |
| 12 | `submission_file` | 明细表 | 1 | ~5000 | 每学期 +2500 |
| 13 | `file_storage` | 事务表 | 4 | ~8000 | 每学期 +4000 |
| 14 | `evaluation_standard` | 主数据 | 3 | ~10 | 几乎不变 |
| 15 | `standard_dimension` | 明细表 | 1 | ~50 | 每学期 +10 |
| 16 | `standard_rule` | 明细表 | 2 | ~200 | 每学期 +50 |
| 17 | `ai_analysis` | 事务表 | 3 | ~2000 | 与 submission 同步 |
| 18 | `ai_analysis_detail` | 明细表 | 4 | ~10000 | 每学期 +5000 |
| 19 | `teacher_review` | 事务表 | 3 | ~2000 | 与 submission 同步 |
| 20 | `review_item` | 明细表 | 2 | ~10000 | 与 detail 同步 |
| 21 | `score_record` | 事务表 | 4 | ~2000 | 与 submission 同步 |
| 22 | `report` | 事务表 | 3 | ~200 | 每学期 +100 |
| 23 | `statistics_snapshot` | 快照表 | 2 | ~10000 | 每 5 分钟 +6 |
| 24 | `system_config` | 配置表 | 2 | ~10 | 几乎不变 |
| 25 | `operation_log` | 日志表 | 6 | ~50000 | 每天 +200 |
| 26 | `notification` | 事务表 | 2 | ~10000 | 每天 +50 |

---

## Appendix B: 与已有文档的一致性检查

| 本文档章节 | 关联文档 | 一致性 |
|---|---|---|
| 2. Design Principles | SDS §9.1 Field Conventions, Backend Architecture §5 Layer Design | ✅ 一致。命名规范补充了审计字段和乐观锁（SDS 缺失） |
| 3. ER Design | SDS §8.1 ER Diagram | ✅ 一致。增加了 statistics_snapshot、review_item（SDS 未详细定义） |
| 4. Table Design | SDS §9.2 Core Tables, API Mock Spec §5-12 | ✅ 一致。所有 Mock 接口的数据结构均可在表中找到对应字段 |
| 5. Index Strategy | Backend Architecture §13 Performance | ✅ 一致。索引设计支撑了高频查询的性能目标 |
| 6. Transaction Design | Backend Architecture §6 Core Modules | ✅ 一致。事务边界与模块职责匹配 |
| 7. Data Security | Backend Architecture §12 Security | ✅ 一致。数据库安全策略与后端安全设计对齐 |
| 8. Performance | Backend Architecture §13 Performance, SDS §10 Redis | ✅ 一致。缓存 Key 设计与 Redis 设计对齐 |
| 9. Future Scalability | Backend Architecture §14 Scalability, MVP §2 Optional Extensions | ✅ 一致。扩展预留与 P1/P2 路线图对齐 |
| 角色数量 | ADR-011, PRD v2.0 | ✅ 一致。3 角色：Student / Teacher / Admin |

---

*本数据库设计文档基于 PRD v2.0、SDS v1.0、Backend Architecture Design v1.0、API Mock Specification v1.0、MVP、ADR-001~011 编写，所有表结构设计、索引策略、事务方案与已有文档保持一致。冲突项已在本文档开头标注并给出处理方案。*
