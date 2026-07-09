# 评分细则融合 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 删除教师端标准管理页，四维度评分下沉为系统固定基线，教师建任务时填 Role/Skill/Rule 三段文本作为任务级评分细则，与固定四维度融合注入 AI。

**Architecture:** 后端新增 `grading_rule` 字段存教师文本，新任务固定指向 id=1000 的"系统默认标准"（四维度基线）；`PromptBuilder` 在四维度之后注入教师文本，AI 输出结构不变，雷达图/成绩链路零改动。前端删两个标准页，建/编辑任务弹窗引入 `GradingRuleForm` 组件收集 R/S/R。

**Tech Stack:** Spring Boot 3.5 + MyBatis Plus + Flyway + JDK21（后端）；Vue3 + TS + Vite + Pinia + Element Plus（前端）

**测试策略说明:** 本项目无单元测试框架（server 无 `src/test`，前端无 vitest）。因此本计划以**编译/类型检查 + 手动联调**替代 TDD：后端每阶段用 `mvn -f server/pom.xml compile` 验证，前端用 `npm run build`（含 vue-tsc 类型检查）与 `npm run lint` 验证，最后一节做端到端手动联调。

**关键约束:**
- 系统默认标准固定 id=**1000**（id=1 已被 V3 种子占用），维度 id 1001-1004
- `SystemConstants.DEFAULT_STANDARD_ID = 1000L`
- Output Format 段由系统固定，教师只填 Role/Skill/Rule
- 保留 `EvaluationStandard`/`StandardDimension` 实体+Mapper（AI 仍读维度）

---

## 文件结构

**后端 创建:**
- `server/src/main/resources/db/migration/V5__default_standard_and_grading_rule.sql` — 迁移：加字段 + 系统默认标准 seed

**后端 修改:**
- `server/src/main/java/com/b1/common/constant/SystemConstants.java` — 加 `DEFAULT_STANDARD_ID`
- `server/src/main/java/com/b1/module/task/entity/TrainingTask.java` — 加 `gradingRule` 字段
- `server/src/main/java/com/b1/module/teacher/dto/TaskCreateDTO.java` — 加 `gradingRule`
- `server/src/main/java/com/b1/module/teacher/dto/TaskUpdateDTO.java` — 加 `gradingRule`
- `server/src/main/java/com/b1/module/teacher/vo/TaskDetailVO.java` — 加 `gradingRule`（编辑回填用）
- `server/src/main/java/com/b1/module/teacher/service/impl/TeacherTaskServiceImpl.java` — createTask/updateTask/getTaskDetail 处理 gradingRule + 固定 standardId
- `server/src/main/java/com/b1/module/ai/service/PromptBuilder.java` — buildSystemPrompt 加 gradingRule 参数
- `server/src/main/java/com/b1/module/ai/service/impl/AiServiceImpl.java` — 传入 gradingRule

**后端 删除:**
- `server/src/main/java/com/b1/module/teacher/controller/TeacherStandardController.java`
- `TeacherService`/`TeacherServiceImpl` 中标准管理方法 + 相关 DTO/VO（Task 12 详列）

**前端 创建:**
- `B1_Platform/src/components/business/GradingRuleForm.vue` — R/S/R 三段输入组件

**前端 修改:**
- `B1_Platform/src/types/teacher.ts` — `ITaskFormData` 加三段字段；`ITeacherTaskItem` 加 gradingRule（可选）
- `B1_Platform/src/api/modules/teacher.ts` — `toTaskPayload` 拼接 R/S/R/O；删标准 API
- `B1_Platform/src/stores/useTeacherStore.ts` — 删标准相关 state/action
- `B1_Platform/src/pages/teacher/TrainingPage.vue` — 弹窗引入 GradingRuleForm
- `B1_Platform/src/router/routes/teacher.ts` — 删两条标准路由

**前端 删除:**
- `B1_Platform/src/pages/teacher/StandardsPage.vue`
- `B1_Platform/src/pages/teacher/StandardsLibraryPage.vue`

---

## 阶段一：后端数据层与实体

### Task 1: 数据库迁移

**Files:**
- Create: `server/src/main/resources/db/migration/V5__default_standard_and_grading_rule.sql`

- [ ] **Step 1: 写迁移文件**

```sql
-- V5: 任务级评分细则字段 + 系统默认评价标准（固定四维度基线）
-- 关联 ADR-012 / 设计文档 2026-07-09-grading-rule-fusion-design.md

-- 1. training_task 新增评分细则字段（存教师 R/S/R/O 原文）
ALTER TABLE `training_task`
    ADD COLUMN `grading_rule` LONGTEXT NULL COMMENT '教师自定义评分细则(R/S/R/O原文)' AFTER `requirement`;

-- 2. 系统默认评价标准（id=1000，规避 V3 已占用的 id=1）
INSERT IGNORE INTO `evaluation_standard`
    (`id`, `standard_name`, `description`, `course_type`, `status`, `is_template`) VALUES
    (1000, '系统通用四维度标准', '平台内置固定评价基线，所有实训任务默认使用', 'GENERAL', 'PUBLISHED', 1);

-- 3. 四维度（id 1001-1004，权重和=100）
INSERT IGNORE INTO `standard_dimension`
    (`id`, `standard_id`, `dim_name`, `dim_description`, `weight`, `max_score`, `sort_order`) VALUES
    (1001, 1000, '代码规范',   '代码风格、命名规范、注释完整性、可读性',       30, 30, 1),
    (1002, 1000, '功能完成度', '需求实现程度、功能完整性、正确性',             30, 30, 2),
    (1003, 1000, '设计质量',   '架构设计、模块划分、算法与性能、扩展性',       20, 20, 3),
    (1004, 1000, '文档完整性', 'README、接口文档、设计说明、提交规范',         20, 20, 4);
```

- [ ] **Step 2: 验证迁移能被 Flyway 识别（编译期不执行，启动时执行）**

Run: `mvn -f server/pom.xml -q compile`
Expected: BUILD SUCCESS（编译通过即可，迁移在应用启动时才执行）

- [ ] **Step 3: 提交**

```bash
git add server/src/main/resources/db/migration/V5__default_standard_and_grading_rule.sql
git commit -m "feat(db): 新增任务评分细则字段与系统默认四维度标准(V5)"
```

---

### Task 2: SystemConstants 常量

**Files:**
- Modify: `server/src/main/java/com/b1/common/constant/SystemConstants.java`

- [ ] **Step 1: 加常量**

在 `MAX_UPLOAD_SIZE_MB` 行之后新增：

```java
    public static final int MAX_UPLOAD_SIZE_MB = 50;

    /** 系统默认评价标准 ID（固定四维度基线，见 V5 迁移）。所有新建任务固定引用此标准。 */
    public static final long DEFAULT_STANDARD_ID = 1000L;
```

- [ ] **Step 2: 编译验证**

Run: `mvn -f server/pom.xml -q compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/b1/common/constant/SystemConstants.java
git commit -m "feat: 新增系统默认评价标准 ID 常量"
```

---

### Task 3: TrainingTask 实体加字段

**Files:**
- Modify: `server/src/main/java/com/b1/module/task/entity/TrainingTask.java`

- [ ] **Step 1: 加字段**

在 `requirement` 字段之后新增：

```java
    @TableField("requirement")
    private String requirement;

    @TableField("grading_rule")
    private String gradingRule;
```

- [ ] **Step 2: 编译验证**

Run: `mvn -f server/pom.xml -q compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/b1/module/task/entity/TrainingTask.java
git commit -m "feat: TrainingTask 新增 gradingRule 字段"
```

---

## 阶段二：后端 DTO/VO 与服务

### Task 4: TaskCreateDTO / TaskUpdateDTO 加字段

**Files:**
- Modify: `server/src/main/java/com/b1/module/teacher/dto/TaskCreateDTO.java`
- Modify: `server/src/main/java/com/b1/module/teacher/dto/TaskUpdateDTO.java`

- [ ] **Step 1: TaskCreateDTO 加 gradingRule**

在 `private Long standardId;` 之前新增：

```java
    private String gradingRule;

    private Long standardId;
```

- [ ] **Step 2: TaskUpdateDTO 加 gradingRule**

在 `private Long standardId;` 之前新增：

```java
    private String gradingRule;

    private Long standardId;
```

- [ ] **Step 3: 编译验证**

Run: `mvn -f server/pom.xml -q compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add server/src/main/java/com/b1/module/teacher/dto/TaskCreateDTO.java server/src/main/java/com/b1/module/teacher/dto/TaskUpdateDTO.java
git commit -m "feat: 任务创建/更新 DTO 新增 gradingRule"
```

---

### Task 5: TaskDetailVO 加字段（编辑回填用）

**Files:**
- Modify: `server/src/main/java/com/b1/module/teacher/vo/TaskDetailVO.java`

- [ ] **Step 1: 加字段**

在 `private Long standardId;` 之前新增：

```java
    private String gradingRule;

    private Long standardId;
```

- [ ] **Step 2: 编译验证**

Run: `mvn -f server/pom.xml -q compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/b1/module/teacher/vo/TaskDetailVO.java
git commit -m "feat: TaskDetailVO 新增 gradingRule 字段"
```

---

### Task 6: TeacherTaskServiceImpl.createTask — 固定 standardId + 存 gradingRule

**Files:**
- Modify: `server/src/main/java/com/b1/module/teacher/service/impl/TeacherTaskServiceImpl.java:243-254`

- [ ] **Step 1: 确认 import 有 SystemConstants**

在文件顶部 import 区确认存在（无则新增）：

```java
import com.b1.common.constant.SystemConstants;
```

- [ ] **Step 2: 修改 createTask 中 task 赋值段**

将现有（约 243-254 行）：

```java
        TrainingTask task = new TrainingTask();
        task.setCourseId(courseId);
        task.setTaskName(dto.getTaskName());
        task.setSubmissionType(dto.getSubmissionType());
        task.setMaxSubmitCount(dto.getMaxSubmitCount());
        task.setMaxScore(dto.getTotalScore());
        task.setDescription(dto.getDescription());
        task.setRequirement(dto.getRequirement());
        task.setEndTime(dto.getEndTime());
        task.setAllowLate(dto.getAllowLate() != null ? dto.getAllowLate() : 0);
        task.setStandardId(dto.getStandardId());
        task.setStatus("DRAFT");
```

改为：

```java
        TrainingTask task = new TrainingTask();
        task.setCourseId(courseId);
        task.setTaskName(dto.getTaskName());
        task.setSubmissionType(dto.getSubmissionType());
        task.setMaxSubmitCount(dto.getMaxSubmitCount());
        task.setMaxScore(dto.getTotalScore());
        task.setDescription(dto.getDescription());
        task.setRequirement(dto.getRequirement());
        task.setGradingRule(dto.getGradingRule());
        task.setEndTime(dto.getEndTime());
        task.setAllowLate(dto.getAllowLate() != null ? dto.getAllowLate() : 0);
        task.setStandardId(SystemConstants.DEFAULT_STANDARD_ID);
        task.setStatus("DRAFT");
```

- [ ] **Step 3: 编译验证**

Run: `mvn -f server/pom.xml -q compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add server/src/main/java/com/b1/module/teacher/service/impl/TeacherTaskServiceImpl.java
git commit -m "feat: 建任务固定指向系统默认标准并保存评分细则"
```

---

### Task 7: TeacherTaskServiceImpl.updateTask — 存 gradingRule

**Files:**
- Modify: `server/src/main/java/com/b1/module/teacher/service/impl/TeacherTaskServiceImpl.java:324-326`

- [ ] **Step 1: 修改 updateTask 中 standardId 处理段**

将现有（约 324-326 行）：

```java
        if (dto.getStandardId() != null) {
            task.setStandardId(dto.getStandardId());
        }
```

改为（移除前端 standardId 写入，改为写 gradingRule）：

```java
        if (dto.getGradingRule() != null) {
            task.setGradingRule(dto.getGradingRule());
        }
```

> 说明：教师不再选标准，standardId 恒为默认值（建任务时已设），编辑时不改。

- [ ] **Step 2: 编译验证**

Run: `mvn -f server/pom.xml -q compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/b1/module/teacher/service/impl/TeacherTaskServiceImpl.java
git commit -m "feat: 更新任务保存评分细则"
```

---

### Task 8: TeacherTaskServiceImpl.getTaskDetail — 回填 gradingRule

**Files:**
- Modify: `server/src/main/java/com/b1/module/teacher/service/impl/TeacherTaskServiceImpl.java:150-151`

- [ ] **Step 1: 在 vo.setStandardId 之前加回填**

将现有（约 150-151 行）：

```java
        vo.setStatus(task.getStatus());
        vo.setStandardId(task.getStandardId());
```

改为：

```java
        vo.setStatus(task.getStatus());
        vo.setGradingRule(task.getGradingRule());
        vo.setStandardId(task.getStandardId());
```

- [ ] **Step 2: 编译验证**

Run: `mvn -f server/pom.xml -q compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/b1/module/teacher/service/impl/TeacherTaskServiceImpl.java
git commit -m "feat: 任务详情返回评分细则供编辑回填"
```

---

## 阶段三：后端 AI 融合

### Task 9: PromptBuilder.buildSystemPrompt 加 gradingRule 参数

**Files:**
- Modify: `server/src/main/java/com/b1/module/ai/service/PromptBuilder.java:78-93`

- [ ] **Step 1: 修改 SYSTEM_TEMPLATE，在评价维度后加占位**

将现有模板中的：

```java
            ## 评价维度
            {dimensions}

            ## 输出格式
```

改为：

```java
            ## 评价维度
            {dimensions}
            {gradingRule}
            ## 输出格式
```

- [ ] **Step 2: 修改 buildSystemPrompt 方法签名与实现**

将现有（78-93 行）：

```java
    public String buildSystemPrompt(List<StandardDimension> dimensions) {
        StringBuilder dimDesc = new StringBuilder();
        for (int i = 0; i < dimensions.size(); i++) {
            StandardDimension dim = dimensions.get(i);
            dimDesc.append((i + 1) + ". **")
                    .append(escape(dim.getDimName()))
                    .append("** (权重:")
                    .append(escape(dim.getWeight() != null ? dim.getWeight().toString() : "0"))
                    .append(", 满分:")
                    .append(escape(dim.getMaxScore() != null ? dim.getMaxScore().toString() : "0"))
                    .append("): ")
                    .append(escape(dim.getDimDescription() != null ? dim.getDimDescription() : "无描述"))
                    .append("\n");
        }
        return SYSTEM_TEMPLATE.replace("{dimensions}", dimDesc.toString());
    }
```

改为：

```java
    public String buildSystemPrompt(List<StandardDimension> dimensions, String gradingRule) {
        StringBuilder dimDesc = new StringBuilder();
        for (int i = 0; i < dimensions.size(); i++) {
            StandardDimension dim = dimensions.get(i);
            dimDesc.append((i + 1) + ". **")
                    .append(escape(dim.getDimName()))
                    .append("** (权重:")
                    .append(escape(dim.getWeight() != null ? dim.getWeight().toString() : "0"))
                    .append(", 满分:")
                    .append(escape(dim.getMaxScore() != null ? dim.getMaxScore().toString() : "0"))
                    .append("): ")
                    .append(escape(dim.getDimDescription() != null ? dim.getDimDescription() : "无描述"))
                    .append("\n");
        }
        String ruleSection = "";
        if (gradingRule != null && !gradingRule.isBlank()) {
            ruleSection = "\n## 本任务特定评分细则(教师指定)\n" + escape(gradingRule) + "\n";
        }
        return SYSTEM_TEMPLATE
                .replace("{dimensions}", dimDesc.toString())
                .replace("{gradingRule}", ruleSection);
    }
```

- [ ] **Step 3: 编译验证（此时 AiServiceImpl 调用点会报错，Task 10 修复）**

Run: `mvn -f server/pom.xml -q compile`
Expected: FAIL — `AiServiceImpl.java:168` 报 `buildSystemPrompt(List)` 参数不匹配（预期，下一 Task 修复）

- [ ] **Step 4: 不单独提交，与 Task 10 一起提交**

（本 Task 与 Task 10 是原子改动，合并提交）

---

### Task 10: AiServiceImpl 传入 gradingRule

**Files:**
- Modify: `server/src/main/java/com/b1/module/ai/service/impl/AiServiceImpl.java:167-168`

- [ ] **Step 1: 修改 dimensions 加载后的 prompt 构建**

将现有（167-168 行）：

```java
            List<StandardDimension> dimensions = getDimensions(task);
            String systemPrompt = promptBuilder.buildSystemPrompt(dimensions);
```

改为：

```java
            List<StandardDimension> dimensions = getDimensions(task);
            String gradingRule = task != null ? task.getGradingRule() : null;
            String systemPrompt = promptBuilder.buildSystemPrompt(dimensions, gradingRule);
```

- [ ] **Step 2: 编译验证**

Run: `mvn -f server/pom.xml -q compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交（Task 9 + Task 10 合并）**

```bash
git add server/src/main/java/com/b1/module/ai/service/PromptBuilder.java server/src/main/java/com/b1/module/ai/service/impl/AiServiceImpl.java
git commit -m "feat: AI 评分融合任务级评分细则"
```

---

## 阶段四：后端删除标准管理接口

### Task 11: 删除 TeacherStandardController

**Files:**
- Delete: `server/src/main/java/com/b1/module/teacher/controller/TeacherStandardController.java`

- [ ] **Step 1: 删除文件**

```bash
git rm server/src/main/java/com/b1/module/teacher/controller/TeacherStandardController.java
```

- [ ] **Step 2: 编译验证（TeacherService 标准方法此时变成无调用方，但仍存在，编译应通过）**

Run: `mvn -f server/pom.xml -q compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git commit -m "refactor: 移除教师端标准管理接口 Controller"
```

---

### Task 12: 清理 TeacherService 标准管理方法

**Files:**
- Modify: `server/src/main/java/com/b1/module/teacher/service/TeacherService.java`
- Modify: `server/src/main/java/com/b1/module/teacher/service/impl/TeacherServiceImpl.java`

- [ ] **Step 1: 先定位所有标准管理方法与其它调用方**

Run: `grep -rn "listStandards\|getStandardDetail\|createStandard\|updateStandard\|copyStandard" server/src/main/java`
Expected: 仅出现在 `TeacherService.java` / `TeacherServiceImpl.java`（TeacherStandardController 已删）。若有其它调用方，STOP 并报告。

- [ ] **Step 2: 从 TeacherService 接口删除标准管理方法声明**

删除以下方法签名（仅这些，保留其它教师方法）：
`listStandards`、`getStandardDetail`、`createStandard`、`updateStandard`、`copyStandard`

- [ ] **Step 3: 从 TeacherServiceImpl 删除对应实现**

删除上述方法的 `@Override` 实现体。若产生未使用的 import（如 StandardCreateDTO/StandardListVO 等），一并删除。

- [ ] **Step 4: 编译验证**

Run: `mvn -f server/pom.xml -q compile`
Expected: BUILD SUCCESS

- [ ] **Step 5: 删除无引用的 DTO/VO（先确认无引用）**

Run: `grep -rln "StandardCreateDTO\|StandardUpdateDTO\|StandardListVO\|StandardDetailVO" server/src/main/java`
Expected: 无输出。若无输出则删除这些文件：

```bash
git rm server/src/main/java/com/b1/module/teacher/dto/StandardCreateDTO.java server/src/main/java/com/b1/module/teacher/dto/StandardUpdateDTO.java server/src/main/java/com/b1/module/teacher/vo/StandardListVO.java server/src/main/java/com/b1/module/teacher/vo/StandardDetailVO.java
```

若某文件仍被引用（有输出），则保留该文件，STOP 报告。

- [ ] **Step 6: 再次编译验证**

Run: `mvn -f server/pom.xml -q compile`
Expected: BUILD SUCCESS

- [ ] **Step 7: 提交**

```bash
git add -A server/src/main/java/com/b1/module/teacher/
git commit -m "refactor: 移除教师端标准管理服务方法与相关 DTO/VO"
```

---

### Task 13: 后端启动冒烟验证

- [ ] **Step 1: 启动后端（需 MySQL/Redis 运行，V5 迁移将自动执行）**

Run: `mvn -f server/pom.xml spring-boot:run`
Expected: 启动成功，日志含 Flyway 执行 V5，无异常；Tomcat started on port 8080

- [ ] **Step 2: 验证系统默认标准已入库**

Run: `curl -s -m 5 -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'`
Expected: 返回 token（确认应用正常）

- [ ] **Step 3: 确认标准管理接口已下线**

Run: `curl -s -m 5 http://localhost:8080/api/v1/teacher/standards -H "Authorization: Bearer <token>"`
Expected: 404（接口已删除）

- [ ] **Step 4: 停止后端，无需提交（仅验证）**

---

## 阶段五：前端类型与 API 层

### Task 14: 更新 teacher 类型

**Files:**
- Modify: `B1_Platform/src/types/teacher.ts:48-71`

- [ ] **Step 1: ITaskFormData 加三段字段**

将现有（64-71 行）：

```typescript
export interface ITaskFormData {
  taskName: string
  courseId: string
  description: string
  dueDate: string
  weight: number
  priority: "high" | "medium" | "low"
}
```

改为：

```typescript
export interface ITaskFormData {
  taskName: string
  courseId: string
  description: string
  dueDate: string
  weight: number
  priority: "high" | "medium" | "low"
  roleText: string
  skillText: string
  ruleText: string
}
```

- [ ] **Step 2: ITeacherTaskItem 加 gradingRule（可选，用于编辑回填）**

在 `ITeacherTaskItem` 的 `reviewedCount: number` 之后加：

```typescript
  reviewedCount: number
  gradingRule?: string
```

- [ ] **Step 3: 类型检查**

Run: `cd B1_Platform && npm run build`
Expected: 可能因 TrainingPage/store 尚未适配而报错——记录报错点，阶段五结束时统一消除。若仅 TrainingPage.vue 报 roleText 缺失属正常。

> 注：阶段五各 Task 会引入临时类型不一致，直到 Task 17（TrainingPage）完成才全绿。每个后端 Task 独立提交；前端阶段五建议按 Task 提交，最终 Task 17 后跑通 build。

- [ ] **Step 4: 提交**

```bash
git add B1_Platform/src/types/teacher.ts
git commit -m "feat(fe): 任务表单类型新增 R/S/R 三段字段"
```

---

### Task 15: API 层 — 拼接 R/S/R/O + 删标准 API

**Files:**
- Modify: `B1_Platform/src/api/modules/teacher.ts:39-56, 66-73, 131-148`

- [ ] **Step 1: 修改 toTaskPayload 拼接 R/S/R/O**

将现有（66-73 行）：

```typescript
function toTaskPayload(data: ITaskFormData): Record<string, unknown> {
  const { dueDate, ...rest } = data
  const payload: Record<string, unknown> = { ...rest }
  if (dueDate) {
    payload.endTime = dueDate.length <= 10 ? `${dueDate}T23:59:59` : dueDate
  }
  return payload
}
```

改为：

```typescript
function toTaskPayload(data: ITaskFormData): Record<string, unknown> {
  const { dueDate, roleText, skillText, ruleText, weight, ...rest } = data
  const payload: Record<string, unknown> = { ...rest }
  payload.totalScore = weight
  if (dueDate) {
    payload.endTime = dueDate.length <= 10 ? `${dueDate}T23:59:59` : dueDate
  }
  const parts: string[] = []
  if (roleText?.trim()) parts.push(`Role：${roleText.trim()}`)
  if (skillText?.trim()) parts.push(`Skill：${skillText.trim()}`)
  if (ruleText?.trim()) parts.push(`Rule：${ruleText.trim()}`)
  payload.gradingRule = parts.join("\n")
  return payload
}
```

> 说明：后端 DTO 用 `totalScore`（非 `weight`），旧代码 spread 时字段名不符，本次一并修正。

- [ ] **Step 2: 删除标准相关 API 函数**

删除以下函数（39-56 行的 Standards 段 + 131-148 行的 Standards Library 段）：
`fetchStandards`、`fetchStandardDimensions`、`updateStandardDimensions`、`fetchStandardTemplates`、`copyStandard`、`createStandardTemplate`

同时删除文件顶部 import 中已无用的类型：`IStandardItem`、`IStandardTemplate`、`IDimensionConfig`、`IDimensionItem`（仅删 teacher.ts 中确实不再引用的）。

- [ ] **Step 3: 类型检查（store 仍引用被删函数，预期报错，Task 16 修复）**

Run: `cd B1_Platform && npm run build`
Expected: FAIL — useTeacherStore.ts 引用已删除的 teacherApi.fetchStandards 等（预期，下一 Task 修复）

- [ ] **Step 4: 不单独提交，与 Task 16 合并**

---

### Task 16: Store — 删除标准相关 state/action

**Files:**
- Modify: `B1_Platform/src/stores/useTeacherStore.ts`

- [ ] **Step 1: 定位所有标准相关代码**

Run: `grep -n "standard\|Standard\|Dimension\|dimension\|Template" B1_Platform/src/stores/useTeacherStore.ts`
Expected: 列出 imports、state（standards/standardsLoading/standardTemplates/templatesLoading/currentDimensions/isEditingDimensions）、mapStandard/mapTemplate、fetch/save/copy/create 等 action、return 导出。

- [ ] **Step 2: 删除标准相关内容**

删除以下（保留 tasks/courses/students/reports 等其它功能）：
- import 的标准类型：`IStandardItem`、`IStandardTemplate`、`IDimensionItem`（确认无其它使用）
- state：`standards`、`standardsLoading`、`standardTemplates`、`templatesLoading`、`currentDimensions`、`isEditingDimensions`
- 函数：`mapStandard`、`fetchStandards`、`fetchStandardDimensions`、`saveDimensions`、`resetDimensions`、`mapTemplate`、`fetchStandardTemplates`、`copyStandardTemplate`、`createStandardTemplate`
- return 中对应导出项

- [ ] **Step 3: 类型检查**

Run: `cd B1_Platform && npm run build`
Expected: 可能仍因 StandardsPage/StandardsLibraryPage 引用 store 报错（这两页 Task 18 删除）。若仅这两页报错属预期。

- [ ] **Step 4: 提交（Task 15 + Task 16 合并）**

```bash
git add B1_Platform/src/api/modules/teacher.ts B1_Platform/src/stores/useTeacherStore.ts
git commit -m "refactor(fe): 移除标准管理 API 与 Store 逻辑"
```

---

## 阶段六：前端组件与页面

### Task 17: 创建 GradingRuleForm 组件

**Files:**
- Create: `B1_Platform/src/components/business/GradingRuleForm.vue`

- [ ] **Step 1: 写组件**

```vue
<script setup lang="ts">
interface IGradingRuleModel {
  roleText: string
  skillText: string
  ruleText: string
}

const props = defineProps<{ modelValue: IGradingRuleModel }>()
const emit = defineEmits<{ "update:modelValue": [value: IGradingRuleModel] }>()

function update(key: keyof IGradingRuleModel, value: string) {
  emit("update:modelValue", { ...props.modelValue, [key]: value })
}
</script>

<template>
  <div class="grading-rule">
    <div class="grading-rule__hint">评分细则（用于 AI 评审，可留空使用系统默认四维度）</div>

    <div class="form-group">
      <label class="form-label">Role · 角色</label>
      <textarea
        :value="modelValue.roleText"
        class="form-textarea"
        rows="2"
        placeholder="如：资深阅卷教师身份"
        @input="update('roleText', ($event.target as HTMLTextAreaElement).value)"
      />
    </div>

    <div class="form-group">
      <label class="form-label">Skill · 能力要求</label>
      <textarea
        :value="modelValue.skillText"
        class="form-textarea"
        rows="2"
        placeholder="如：精准客观打分、分项统计、出具评语"
        @input="update('skillText', ($event.target as HTMLTextAreaElement).value)"
      />
    </div>

    <div class="form-group">
      <label class="form-label">Rule · 打分细则（重点）</label>
      <textarea
        :value="modelValue.ruleText"
        class="form-textarea"
        rows="4"
        placeholder="详细打分细则、扣分标准、分值权重"
        @input="update('ruleText', ($event.target as HTMLTextAreaElement).value)"
      />
    </div>

    <div class="grading-rule__readonly">
      Output Format · 输出格式：系统已固定为标准四维度 JSON，无需填写
    </div>
  </div>
</template>

<style scoped>
.grading-rule { border-top: 1px solid var(--color-border, #e2e8f0); padding-top: 16px; margin-top: 4px; }
.grading-rule__hint { font-size: 13px; font-weight: 600; color: var(--color-text-secondary, #64748b); margin-bottom: 12px; }
.grading-rule__readonly { font-size: 12px; color: var(--color-text-placeholder, #94a3b8); background: #f8fafc; border: 1px dashed var(--color-border, #e2e8f0); border-radius: var(--radius-sm, 4px); padding: 8px 12px; }
.form-group { margin-bottom: 16px; }
.form-label { display: block; font-size: 13px; font-weight: 500; color: var(--color-text-secondary, #64748b); margin-bottom: 5px; }
.form-textarea { width: 100%; padding: 8px 12px; font-size: 14px; border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-sm, 4px); background: var(--color-card, #fff); color: var(--color-text-primary, #1e293b); resize: vertical; outline: none; font-family: inherit; box-sizing: border-box; }
.form-textarea:focus { border-color: var(--color-primary, #3b82f6); }
</style>
```

- [ ] **Step 2: 类型检查**

Run: `cd B1_Platform && npm run build`
Expected: 组件本身无类型错误（其它页面报错见 Task 18/19）

- [ ] **Step 3: 提交**

```bash
git add B1_Platform/src/components/business/GradingRuleForm.vue
git commit -m "feat(fe): 新增评分细则输入组件 GradingRuleForm"
```

---

### Task 18: 删除两个标准页面 + 路由

**Files:**
- Delete: `B1_Platform/src/pages/teacher/StandardsPage.vue`
- Delete: `B1_Platform/src/pages/teacher/StandardsLibraryPage.vue`
- Modify: `B1_Platform/src/router/routes/teacher.ts:37-42, 55-60`

- [ ] **Step 1: 删除两个页面文件**

```bash
git rm B1_Platform/src/pages/teacher/StandardsPage.vue B1_Platform/src/pages/teacher/StandardsLibraryPage.vue
```

- [ ] **Step 2: 删除路由 — 移除 standards 条目（37-42 行）**

删除：

```typescript
      {
        path: "standards",
        name: "TeacherStandards",
        component: () => import("@/pages/teacher/StandardsPage.vue"),
        meta: { title: "评价标准", icon: "ClipboardCheck", sort: 3 },
      },
```

- [ ] **Step 3: 删除路由 — 移除 standards-library 条目（55-60 行）**

删除：

```typescript
      {
        path: "standards-library",
        name: "TeacherStandardsLibrary",
        component: () => import("@/pages/teacher/StandardsLibraryPage.vue"),
        meta: { title: "标准库", icon: "Library", sort: 6 },
      },
```

- [ ] **Step 4: 类型检查**

Run: `cd B1_Platform && npm run build`
Expected: 仅剩 TrainingPage.vue 相关类型错误（Task 19 修复），无 StandardsPage 引用错误

- [ ] **Step 5: 提交**

```bash
git add -A B1_Platform/src/pages/teacher/ B1_Platform/src/router/routes/teacher.ts
git commit -m "refactor(fe): 删除评价标准/标准库页面及路由"
```

---

### Task 19: TrainingPage 建/编辑弹窗引入 GradingRuleForm

**Files:**
- Modify: `B1_Platform/src/pages/teacher/TrainingPage.vue`

- [ ] **Step 1: import 组件**

在 `import ErrorState ...` 之后加：

```typescript
import ErrorState from "@/components/common/ErrorState.vue"
import GradingRuleForm from "@/components/business/GradingRuleForm.vue"
```

- [ ] **Step 2: form 初始值加三段字段（26-33 行）**

将：

```typescript
const form = ref({
  taskName: "",
  courseId: "",
  description: "",
  dueDate: "2026-07-15",
  weight: 25,
  priority: "medium" as "high" | "medium" | "low",
})
```

改为：

```typescript
const form = ref({
  taskName: "",
  courseId: "",
  description: "",
  dueDate: "2026-07-15",
  weight: 25,
  priority: "medium" as "high" | "medium" | "low",
  roleText: "",
  skillText: "",
  ruleText: "",
})
```

- [ ] **Step 3: resetForm 补三段（38 行）**

将：

```typescript
  form.value = { taskName: "", courseId: store.courses[0]?.courseId || "", description: "", dueDate: "2026-07-15", weight: 25, priority: "medium" }
```

改为：

```typescript
  form.value = { taskName: "", courseId: store.courses[0]?.courseId || "", description: "", dueDate: "2026-07-15", weight: 25, priority: "medium", roleText: "", skillText: "", ruleText: "" }
```

- [ ] **Step 4: openEdit 回填三段（改为 async 拉详情拆分）**

将现有 openEdit（48-60 行）：

```typescript
function openEdit(task: ITeacherTaskItem) {
  isEdit.value = true
  editingId.value = task.taskId
  form.value = {
    taskName: task.taskName,
    courseId: store.courses.find((c) => c.courseName === task.courseName)?.courseId || "",
    description: task.description,
    dueDate: task.deadline ? task.deadline.substring(0, 10) : "",
    weight: task.totalScore,
    priority: task.priority,
  }
  showModal.value = true
}
```

改为：

```typescript
function parseGradingRule(text?: string): { roleText: string; skillText: string; ruleText: string } {
  const result = { roleText: "", skillText: "", ruleText: "" }
  if (!text) return result
  const roleMatch = text.match(/Role：([\s\S]*?)(?=\nSkill：|\nRule：|$)/)
  const skillMatch = text.match(/Skill：([\s\S]*?)(?=\nRole：|\nRule：|$)/)
  const ruleMatch = text.match(/Rule：([\s\S]*?)(?=\nRole：|\nSkill：|$)/)
  if (roleMatch) result.roleText = roleMatch[1].trim()
  if (skillMatch) result.skillText = skillMatch[1].trim()
  if (ruleMatch) result.ruleText = ruleMatch[1].trim()
  return result
}

function openEdit(task: ITeacherTaskItem) {
  isEdit.value = true
  editingId.value = task.taskId
  const parsed = parseGradingRule(task.gradingRule)
  form.value = {
    taskName: task.taskName,
    courseId: store.courses.find((c) => c.courseName === task.courseName)?.courseId || "",
    description: task.description,
    dueDate: task.deadline ? task.deadline.substring(0, 10) : "",
    weight: task.totalScore,
    priority: task.priority,
    roleText: parsed.roleText,
    skillText: parsed.skillText,
    ruleText: parsed.ruleText,
  }
  showModal.value = true
}
```

> 说明：`task.gradingRule` 来自列表项。若列表 VO 未含该字段（当前 mapTask 未映射），编辑时三段为空——可接受（教师重填即可）。完整回填需列表接口返回 gradingRule，属后续增强，本期 YAGNI。

- [ ] **Step 5: 弹窗内加入组件（在优先级 form-group 之后，`<template v-if="isEdit" #footer>` 之前，约 186 行）**

在：

```vue
        <div class="form-group">
          <label class="form-label">优先级</label>
          <select v-model="form.priority" class="form-select">
            <option value="high">高</option>
            <option value="medium">中</option>
            <option value="low">低</option>
          </select>
        </div>
```

之后加：

```vue
        <GradingRuleForm
          v-model="form"
        />
```

> 说明：GradingRuleForm 的 modelValue 期望 `{roleText, skillText, ruleText}`，form 是超集，v-model 传整个 form 时组件只读写这三个键（组件内 `{ ...props.modelValue, [key]: value }` 会保留其它键）。类型上 form 满足 IGradingRuleModel 的三字段要求。

- [ ] **Step 6: 类型检查 + Lint**

Run: `cd B1_Platform && npm run build && npm run lint`
Expected: BUILD SUCCESS，lint 无 error（此时全项目类型应全绿）

- [ ] **Step 7: 提交**

```bash
git add B1_Platform/src/pages/teacher/TrainingPage.vue
git commit -m "feat(fe): 建/编辑任务弹窗集成评分细则输入"
```

---

## 阶段七：端到端联调

### Task 20: 前后端联调验证

- [ ] **Step 1: 启动后端**

Run: `mvn -f server/pom.xml spring-boot:run`（后台）
Expected: 启动成功，V5 已执行

- [ ] **Step 2: 启动前端**

Run: `cd B1_Platform && npm run dev`
Expected: http://localhost:3000 可访问

- [ ] **Step 3: 教师端手动验证（浏览器）**

以教师账号登录，验证：
1. 左侧菜单**不再有**"评价标准""标准库"两项
2. 进入"实训任务管理" → 点"新建任务"，弹窗出现 **Role/Skill/Rule 三段输入 + Output 只读说明**
3. 填写任务信息 + R/S/R 文本，保存成功
4. 编辑该任务，R/S/R 文本能回填（若列表未带 gradingRule 则为空，符合 Step4 说明）

- [ ] **Step 4: AI 评分链路验证（关键）**

以学生账号提交该任务作业 → 教师触发 AI 分析 → 确认：
1. AI 分析成功返回
2. 雷达图正常显示四维度（代码规范/功能完成度/设计质量/文档完整性）
3. 维度扣分明细正常

> 若 AI 返回异常，检查 `application-local.yml` 密钥、后端日志中的 systemPrompt 是否含教师细则段。

- [ ] **Step 5: 记录联调结果**

如全部通过，本期功能完成。如有问题，用 systematic-debugging 定位。

---

## Self-Review 检查（写计划者已核对）

- ✅ **Spec 覆盖**：设计文档 §3（前端删除/组件/弹窗）→ Task 14-19；§4（后端迁移/实体/DTO/VO/Service/PromptBuilder/AiService/删Controller）→ Task 1-12；§5 风险（历史任务/转义/引用检查）→ Task 7 说明/Task 9 escape/Task 12 Step1&5 grep
- ✅ **id 冲突**：设计文档 id=1 已修正为 1000，计划 Task 1/2 一致使用 1000
- ✅ **类型一致**：`gradingRule`（后端）↔ `roleText/skillText/ruleText`（前端 form）↔ 拼接后 `gradingRule`（payload），命名贯穿一致；`buildSystemPrompt(dimensions, gradingRule)` 签名在 Task 9 定义、Task 10 调用一致
- ✅ **totalScore 修正**：发现前端 form 用 `weight` 但后端 DTO 用 `totalScore`，Task 15 Step1 显式映射修复
- ✅ **无占位符**：所有代码步骤含完整代码
- ⚠️ **已知取舍**：编辑回填依赖列表 VO 的 gradingRule（当前 mapTask 未映射），Task 19 Step4 已注明为可接受的 YAGNI 边界
