# B1 平台架构图（As-Is / To-Be）

| 项 | 值 |
|---|---|
| 日期 | 2026-08-03 |
| 状态 | 诊断用 / 可作软著说明书附图底稿 |
| 架构模式 | Modular Monolith（模块化单体） |
| 说明 | As-Is 按当前代码整理；To-Be 为冲 8.5+ 的小改目标，非微服务拆分 |

---

## 1. 系统上下文（Context）

```mermaid
flowchart LR
  subgraph Users["用户"]
    S[学生]
    T[教师]
    A[管理员]
  end

  subgraph Platform["B1 智慧实训平台"]
    WEB[Vue3 前端<br/>B1_Platform]
    API[Spring Boot 后端<br/>server]
  end

  subgraph External["外部依赖"]
    LLM[大模型 API<br/>DeepSeek / Qwen]
    OBJ[MinIO 对象存储]
    DB[(MySQL 8)]
    REDIS[(Redis 7)]
  end

  S --> WEB
  T --> WEB
  A --> WEB
  WEB -->|REST /api/v1| API
  API --> DB
  API --> REDIS
  API --> OBJ
  API -->|OpenAI Compatible| LLM
```

---

## 2. As-Is：当前真实架构

### 2.1 逻辑分层

```mermaid
flowchart TB
  FE["前端 Vue3<br/>pages / api / stores / components / mock"]

  subgraph BE["后端 Modular Monolith"]
    direction TB
    CTRL["Controller 层<br/>按角色前缀暴露 API"]
    SVC["Service 层<br/>业务编排 + 部分领域逻辑"]
    MAP["Mapper / Entity<br/>MyBatis Plus"]
    COMMON["common<br/>异常 / Result / 枚举 / 工具"]
    INFRA["infrastructure<br/>Sa-Token / Redis / MinIO / MP"]
  end

  FE --> CTRL
  CTRL --> SVC
  SVC --> MAP
  CTRL -.->|现存破窗：部分直调| MAP
  CTRL --> COMMON
  SVC --> COMMON
  SVC --> INFRA
  MAP --> INFRA
```

### 2.2 后端模块全景（按代码包）

```mermaid
flowchart TB
  subgraph Facades["角色门面（偏胖）"]
    teacher["teacher ★最重<br/>课程/任务/复核/报表/催办"]
    student["student<br/>仪表盘/成长/报告聚合"]
    admin["admin<br/>用户/班级/系统/日志"]
  end

  subgraph Domain["领域模块（厚薄不一）"]
    auth[auth]
    user[user]
    course[course]
    task[task]
    submission[submission]
    ai[ai]
    standard[standard]
    file[file]
    export[export]
  end

  subgraph Shells["偏空壳（多仅 Entity+Mapper）"]
    review[review]
    score[score]
    notification[notification]
  end

  teacher --> course
  teacher --> task
  teacher --> submission
  teacher --> ai
  teacher --> auth
  teacher --> review
  teacher --> score

  student --> course
  student --> task
  student --> submission
  student --> ai
  student --> review
  student --> score
  student --> notification
  student --> export

  admin --> auth
  admin --> user

  submission --> file
  ai --> submission
  ai --> standard
  ai --> file
  task --> course
  task --> standard
```

### 2.3 API 前缀与角色（As-Is，保持）

```mermaid
flowchart LR
  subgraph Public["公开"]
    AUTH["/api/v1/auth/**"]
  end
  subgraph Authed["已登录"]
    USER["/api/v1/user/**"]
    FILES["/api/v1/files/**"]
  end
  subgraph Role["角色隔离"]
    STU["/api/v1/student/**"]
    TEA["/api/v1/teacher/**"]
    ADM["/api/v1/admin/**"]
  end

  AUTH --> USER
  USER --> STU
  USER --> TEA
  USER --> ADM
  USER --> FILES
```

### 2.4 核心业务链路（As-Is）

```mermaid
sequenceDiagram
  participant T as 教师
  participant S as 学生
  participant API as 后端
  participant AI as AI 模块
  participant LLM as 大模型

  T->>API: 创建课程 / 发布任务 / 配置标准
  S->>API: 提交 ZIP / Git / 报告
  API->>API: submission 落库 + MinIO
  API->>AI: 触发分析
  AI->>LLM: Prompt + 代码/材料
  LLM-->>AI: JSON 初评结果
  AI-->>API: 可解释扣分项
  T->>API: 查看初评并复核终裁
  T/S->>API: 报表导出 PDF/Excel
```

### 2.5 As-Is 问题标注

| 问题 | 表现 |
|---|---|
| 双轴划分 | 领域包 + 角色包并存，逻辑多沉在 teacher/student |
| 空壳模块 | review / score / notification 缺少完整 Service 边界 |
| 分层破窗 | 部分 Controller 直接注入多模块 Mapper |
| 文档漂移 | 基线模块名与代码包名不一致 |
| 可验证性弱 | 自动化测试几乎为 0 |

---

## 3. To-Be：冲 8.5+ 目标架构（小改）

原则：**不拆微服务、不换栈**；门面变薄、领域变实、分层变严、测试补齐。

### 3.1 目标逻辑分层

```mermaid
flowchart TB
  FE["前端 Vue3<br/>薄页面 + composable + 可复用区块组件"]

  subgraph BE["后端 Modular Monolith"]
    direction TB
    FACADE["角色门面 Application Service<br/>teacher / student / admin<br/>只做：鉴权 · 用例编排 · VO 组装"]
    DOMAIN["领域 Service<br/>course / task / submission / ai / ...<br/>只做：本域写入与不变式"]
    MAP["Mapper / Entity<br/>仅被所属领域 Service 使用"]
    COMMON["common"]
    INFRA["infrastructure"]
  end

  FE --> FACADE
  FACADE --> DOMAIN
  DOMAIN --> MAP
  FACADE --> COMMON
  DOMAIN --> COMMON
  DOMAIN --> INFRA
```

### 3.2 目标模块依赖（允许方向）

```mermaid
flowchart TB
  subgraph Facades["角色门面（薄）"]
    teacher2[teacher]
    student2[student]
    admin2[admin]
  end

  subgraph Core["领域核"]
    auth2[auth]
    user2[user]
    course2[course]
    task2[task]
    submission2[submission]
    ai2[ai]
    standard2[standard]
    file2[file]
    export2[export]
    notify2[notification]
  end

  teacher2 --> course2
  teacher2 --> task2
  teacher2 --> submission2
  teacher2 --> ai2
  teacher2 --> export2
  teacher2 --> notify2

  student2 --> course2
  student2 --> task2
  student2 --> submission2
  student2 --> ai2
  student2 --> export2
  student2 --> notify2

  admin2 --> user2
  admin2 --> auth2

  task2 --> course2
  task2 --> standard2
  submission2 --> task2
  submission2 --> file2
  ai2 --> submission2
  ai2 --> standard2
  ai2 --> file2
  export2 --> submission2
  export2 --> ai2
```

### 3.3 空壳模块归并建议

```mermaid
flowchart LR
  subgraph Before["As-Is"]
    r1[review]
    s1[score]
    n1[notification]
  end

  subgraph After["To-Be"]
    sub[submission<br/>含复核与成绩记录]
    n2[notification<br/>补齐 NotificationService]
  end

  r1 --> sub
  s1 --> sub
  n1 --> n2
```

> 若不想立刻改包名：可保留 `review`/`score` 包路径，但 **对外只允许经 `submission` Service 访问**，禁止门面直调 Mapper。

### 3.4 前端目标结构（配合架构分）

```mermaid
flowchart TB
  pages["pages/* 容器页 &lt;300 行"]
  comp["components/business | common | chart"]
  hooks["composables 业务逻辑"]
  api["api/* 接口"]
  stores["pinia 会话/权限/少量共享态"]

  pages --> hooks
  pages --> comp
  hooks --> api
  hooks --> stores
  comp --> hooks
```

优先拆分：`LoginPage` / `SubmitPage` / `AIResultPage` / `SubmissionsPage`。

### 3.5 部署拓扑（软著主叙事，不含国产化）

```mermaid
flowchart LR
  U[浏览器] --> NG[Nginx / Vite Preview]
  NG --> API[Spring Boot :8080]
  API --> MY[(MySQL)]
  API --> RD[(Redis)]
  API --> MN[MinIO]
  API --> LLM[LLM API]
```

麒麟 + LoongArch 相关降级为归档附录，不进入主架构图。

---

## 4. As-Is vs To-Be 对照

| 维度 | As-Is | To-Be |
|---|---|---|
| 模式 | 模块化单体 | 同左（不变） |
| 模块轴 | 领域 + 角色混杂，角色偏胖 | 领域核实、角色门面薄 |
| 分层 | 部分 Controller→Mapper | 严格 Facade→Domain→Mapper |
| review/score | 空壳 | 归入 submission 边界 |
| notification | 空壳 | 独立 Service |
| 测试 | 近乎 0 | 主闭环自动化 |
| 文档 | 与代码漂移 | 以本图为准同步基线 |
| 部署 | 含竞赛国产化叙事 | 标准 Docker 为主 |

---

## 5. 软著说明书建议用法

1. **系统结构**：用 §1 上下文图 + §3.1 分层图  
2. **功能模块**：用 §3.2 模块依赖图（比 As-Is 更清晰，也代表优化目标）  
3. **若材料需反映现状**：可附 §2 As-Is，并注明「实现优化中」  

当前若立刻截源码申请，说明书模块名务必与 **现有包名** 一致，避免写基线里已过时的 `training/report/system`。
