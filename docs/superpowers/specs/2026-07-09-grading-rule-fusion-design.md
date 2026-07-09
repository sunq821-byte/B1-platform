# 设计文档：移除标准管理页 + 任务级 R/S/R/O 评分细则融合

- **日期：** 2026-07-09
- **状态：** 已批准（待实现）
- **关联 ADR：** ADR-012

---

## 1. 背景与目标

### 1.1 现状

当前教师端评分标准通过两个独立页面管理：

- **评价标准页**（`StandardsPage.vue`，路由 `/teacher/standards`）
- **标准库页**（`StandardsLibraryPage.vue`，路由 `/teacher/standards-library`）

评分链路是**维度驱动**的：`TrainingTask.standardId` → 加载 `standard_dimension`（维度名/权重/满分）→ `PromptBuilder.buildSystemPrompt(dimensions)` 生成 system prompt → AI 返回按维度分解的固定 JSON → 前端雷达图、`AiAnalysisDetail`（按维度存扣分）依赖此结构。

### 1.2 问题

- 教师建任务前需先去标准库配置/挑选标准，操作链路长、割裂
- 标准管理页对单个教师而言是重操作，与"建任务"场景脱节
- 大模型评审更自然的方式是结构化提示词（Role/Skill/Rule/Output）

### 1.3 目标

- 删除教师端两个标准管理页面，简化教师操作
- 教师在**建任务弹窗**直接填写 **Role / Skill / Rule** 三段文本，作为该任务的个性化评分细则
- **保留**四维度结构化评分模型（雷达图、维度扣分、可追溯性——赛事核心卖点）
- 二者融合：固定四维度为"骨架"，教师文本为"血肉"，在 `PromptBuilder` 一处汇合

### 1.4 关键决策（brainstorming 结论）

| 决策点 | 选择 |
|---|---|
| R/S/R/O 与四维度的关系 | **B**：文本作为维度的补充说明，二者叠加注入，AI 输出仍按维度分解 |
| 删页面后四维度从哪来 | **A**：系统内置固定四维度基线，教师不再管理维度 |
| 后端 standard 模块处理 | **A**：保留表结构，建"系统默认标准"seed 数据，AI 链路零改动 |
| R/S/R/O 存储 | **A**：`training_task` 加单个 `grading_rule` longtext 字段，存原文 |
| Output Format 处理 | **固定为系统预设 JSON**，教师只填 Role/Skill/Rule 三段 |
| `TeacherStandardController` | **删除**（保留 `EvaluationStandard`/`StandardDimension` 实体+Mapper 供 AI 读取） |
| 文档同步范围 | **A**：本设计文档 + ADR-012；PRD/FIP 等实现后统一同步 |

---

## 2. 架构与数据流

### 2.1 新评分数据流

```
教师建任务(填 Role/Skill/Rule 三段文本)
   → 前端拼成一段 R/S/R/O 文本
   → grading_rule 存入 training_task
   → 任务 standardId 固定指向"系统默认标准"(id=1, 四维度基线)
   → AI 评分时 PromptBuilder 拼接:
        [系统四维度基线]        ← 保雷达图/维度扣分/可追溯(赛事卖点)
      + [教师 R/S/R 文本]       ← 本任务个性化细则(灵活)
      + [系统固定 Output JSON]  ← 保解析链路不破
   → AI 返回固定 JSON(维度分解)
   → 雷达图 / AiAnalysisDetail 照常工作
```

### 2.2 核心原则

固定四维度是"骨架"（保结构化、保卖点），教师 R/S/R 是"血肉"（保灵活）。两者在 `PromptBuilder` 一处汇合，**AI 输出结构完全不变**——现有 AI 解析、雷达图、成绩链路零改动。

---

## 3. 前端改动

### 3.1 删除

| 项 | 路径 |
|---|---|
| 评价标准页 | `B1_Platform/src/pages/teacher/StandardsPage.vue` |
| 标准库页 | `B1_Platform/src/pages/teacher/StandardsLibraryPage.vue` |
| 路由 | `router/routes/teacher.ts` 中 `standards`、`standards-library` 两条（菜单基于路由自动生成，同步消失） |
| Store/API | `useTeacherStore` / `api/modules/teacher.ts` 中仅服务这两页的 standard 方法（保留 AI 评分链路所需部分） |

### 3.2 新增组件：`GradingRuleForm.vue`

- 位置：`B1_Platform/src/components/business/GradingRuleForm.vue`
- 职责：评分细则输入区，可复用于建任务/编辑任务
- 抽出理由：`TrainingPage.vue` 现 228 行，内联后逼近 300 行红线（CLAUDE.md 规范）

组件结构：

```
┌─ 评分细则(用于 AI 评审) ──────────────────────┐
│ Role   角色    [textarea] 如:资深阅卷教师…       │
│ Skill  能力    [textarea] 精准客观打分、分项统计、出具评语 │
│ Rule   细则    [textarea] 详细打分细则、扣分标准(重点)   │
│ ───────────────────────────────────────── │
│ Output 输出格式:系统已固定为标准四维度JSON,无需填写(只读提示) │
└──────────────────────────────────────────┘
```

- Props：`modelValue`（3 段字段对象 `{ roleText, skillText, ruleText }`）
- Emits：`update:modelValue`

### 3.3 建/编辑任务弹窗改造（`TrainingPage.vue`）

- 移除原"选择评价标准"下拉（如有）
- 引入 `GradingRuleForm.vue`
- `form` 新增字段：`roleText / skillText / ruleText`（**方案 X**：前端存 3 段）
- **提交前**拼成一段 R/S/R/O 文本传后端 `gradingRule`
- **编辑模式**：拉取任务详情的 `gradingRule` 后拆回三段回填（与提交拼接对称）

### 3.4 R/S/R/O 文本格式（前后端约定）

前端提交时拼接为：

```
Role：{roleText}
Skill：{skillText}
Rule：{ruleText}
```

（Output Format 段不由教师填写，PromptBuilder 侧用系统固定 JSON 格式）

---

## 4. 后端改动

### 4.1 数据库迁移 `V5__default_standard_and_grading_rule.sql`（新建）

- `training_task` 新增字段：
  `grading_rule LONGTEXT NULL COMMENT '教师自定义评分细则(R/S/R/O原文)'`
- 插入 **id=1 的"系统默认标准"** `evaluation_standard`（`is_template=1`，名称"系统通用四维度标准"）
- 为其插入**四维度** `standard_dimension`（呼应 PRD）：

  | 维度 | 权重 | 满分 |
  |---|---|---|
  | 代码规范 | 30 | 30 |
  | 功能完成度 | 30 | 30 |
  | 设计质量 | 20 | 20 |
  | 文档完整性 | 20 | 20 |

- 使用 `INSERT IGNORE` 防重（V3/V4 已有 id=2/3 模板标准，不冲突）

### 4.2 实体 `TrainingTask.java`

新增：`@TableField("grading_rule") private String gradingRule;`

### 4.3 DTO `TaskCreateDTO` / `TaskUpdateDTO`

- 新增 `private String gradingRule;`
- `standardId` 字段保留但**不再由前端传**（教师不选标准）

### 4.4 常量

`SystemConstants` 新增 `DEFAULT_STANDARD_ID = 1L`

### 4.5 `TeacherTaskServiceImpl.createTask / updateTask`

- `task.setGradingRule(dto.getGradingRule())`
- `task.setStandardId(SystemConstants.DEFAULT_STANDARD_ID)` —— 固定指向系统默认标准，替代原 `dto.getStandardId()`

### 4.6 `PromptBuilder.buildSystemPrompt`（核心融合点）

- 方法签名增加参数：`buildSystemPrompt(List<StandardDimension> dimensions, String gradingRule)`
- 在 `{dimensions}` 之后、`## 输出格式` 之前，注入教师文本（有则拼，空则跳过）：

```
## 本任务特定评分细则(教师指定)
{gradingRule}
```

- Output Format 段保持系统固定 JSON（教师文本里的 Output 不覆盖它）
- `gradingRule` 需做 `escape`（`%` → `%%`），与现有 `escape()` 一致

### 4.7 `AiServiceImpl`（约第 167-168 行）

- 取出 `task.getGradingRule()`
- 传入 `buildSystemPrompt(dimensions, gradingRule)`
- `getDimensions(task)` 逻辑不变（standardId 恒为 1，稳定返回四维度）

### 4.8 删除 standard 管理接口

- **删除** `TeacherStandardController`（standards / standards-library 增删改查接口）
- **删除** `TeacherService` 中仅服务标准管理的方法（listStandards / createStandard / updateStandard / copyStandard / getStandardDetail 等）及相关 DTO/VO（`StandardCreateDTO` / `StandardUpdateDTO` / `StandardListVO` / `StandardDetailVO`，若无其他引用）
- **保留** `EvaluationStandard` / `StandardDimension` 实体 + Mapper（AI 评分仍读维度）

---

## 5. 影响范围与风险

### 5.1 零改动保证

- AI 返回 JSON 结构不变 → 雷达图、`AiAnalysisDetail`、成绩链路不受影响
- `getDimensions(task)` 逻辑不变，仅数据来源固定

### 5.2 风险点

| 风险 | 缓解 |
|---|---|
| 历史任务 `standardId` 指向已删除/模板标准 | 历史任务不受影响（仍读其原 standardId 的维度）；新任务固定指向 id=1 |
| 教师文本注入导致 prompt 过长/注入攻击 | `escape()` 转义；Rule 段长度前端限制 |
| 删除 `TeacherService` 方法牵连其他调用方 | 实现前 grep 确认无其他引用 |

---

## 6. 文档影响清单（实现后统一同步——决策 A）

**本设计阶段仅产出**：本文档 + ADR-012。以下文档实现完成后同步：

**必改**

| 文档 | 改动点 |
|---|---|
| `docs/01-PRD.md` | REQ-004；模块划分删"评价标准配置页"；教研管理模块改为固定四维度+任务级细则 |
| `docs/07-FIP.md` | 4.2"评分标准管理页面"移除；页面数 25→23；教师页面 11→9；Sprint4 人天 |
| `docs/13-API-Contract.md` | 删 `/teacher/standards*` 接口；TaskCreate/Update 加 `gradingRule` |
| `docs/11-Database-Design.md` | `training_task` 加 `grading_rule`；标注系统默认标准 id=1 |
| `docs/12-Backend-Specification.md` | standard 模块职责调整；PromptBuilder 融合逻辑 |

**建议改**：`docs/02-SDS.md`、`docs/08-MVP.md`、`docs/09-Definition-of-Done.md`、`docs/05-Frontend-Specification-v1.0.md`、`docs/00-Architecture-Baseline.md`、`docs/10-Backend-Architecture-Design.md`

**不改**：UI Design System、Component Library、原型目录（pt/）等纯视觉规范

---

## 7. 实现顺序建议

1. 后端：迁移 `V5` → 实体/DTO/常量 → `PromptBuilder` → `TeacherTaskServiceImpl` → 删 `TeacherStandardController`+Service 方法
2. 前端：`GradingRuleForm.vue` → `TrainingPage.vue` 建/编辑改造 → 删两页面+路由+Store/API 清理
3. 联调：建任务填 R/S/R → 学生提交 → AI 评分 → 确认雷达图/维度扣分正常
4. 文档：按第 6 节同步
