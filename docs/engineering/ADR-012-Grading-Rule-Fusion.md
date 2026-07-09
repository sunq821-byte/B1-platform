# ADR-012：移除标准管理页，改任务级 R/S/R/O 评分细则融合

- **日期：** 2026-07-09
- **状态：** Accepted
- **决策者：** 项目负责人
- **关联设计文档：** `docs/superpowers/specs/2026-07-09-grading-rule-fusion-design.md`

---

## 上下文

系统最初通过两个独立的教师端页面管理评分标准：

| 页面 | 路由 | 职责 |
|------|------|------|
| 评价标准页 | `/teacher/standards` | 配置四维度、权重 |
| 标准库页 | `/teacher/standards-library` | 标准模板复用 |

评分链路是维度驱动的：`TrainingTask.standardId` → `standard_dimension`（维度名/权重/满分）→ `PromptBuilder.buildSystemPrompt(dimensions)` → AI 返回按维度分解的固定 JSON → 前端雷达图、`AiAnalysisDetail` 依赖此结构。

存在的问题：

- **操作割裂：** 教师建任务前需先去标准库配置或挑选标准，链路长
- **场景脱节：** 标准管理页对单个教师是重操作，与"建任务"场景分离
- **不契合大模型工作方式：** 大模型评审更自然的方式是结构化提示词（Role/Skill/Rule/Output）

## 决策

**删除教师端两个标准管理页面；四维度评分下沉为系统固定基线；教师在建任务弹窗填写 Role/Skill/Rule 三段文本作为该任务的个性化评分细则，与固定四维度融合注入 AI。**

具体措施：

1. **前端删除** `StandardsPage.vue`、`StandardsLibraryPage.vue` 及对应路由、菜单、Store/API 方法
2. **新增** `GradingRuleForm.vue` 组件（Role/Skill/Rule 三段输入 + Output 只读说明），复用于建/编辑任务
3. **后端** `training_task` 加 `grading_rule` LONGTEXT 字段存 R/S/R/O 原文
4. **后端**建一条 id=1 的"系统默认标准"（四维度：代码规范30/功能完成度30/设计质量20/文档完整性20），所有新任务 `standardId` 固定指向它
5. **`PromptBuilder`** 在四维度之后、Output 格式之前注入教师 R/S/R 文本；Output Format 保持系统固定 JSON
6. **删除** `TeacherStandardController` 及 `TeacherService` 标准管理方法；**保留** `EvaluationStandard`/`StandardDimension` 实体+Mapper（AI 评分仍读维度）

## 备选方案

| 方案 | 未采纳原因 |
|------|-----------|
| R/S/R/O 完全取代维度 | 丢失统一四维度与强追溯性，雷达图不稳定，破坏赛事核心卖点（"统一评价标准"痛点） |
| 建任务时切换"自由文本/结构化"双模式 | 两套逻辑并存，复杂度最高，违反 KISS |
| 维度由 AI 从文本自行归纳 | 追溯性弱、雷达图维度动态不稳定 |
| 彻底移除 standard 模块（删表） | 需改 `AiServiceImpl` 维度加载与 `AiAnalysisDetail` 外键，回归风险高，清理收益不值 |
| 仅前端删页面、后端不动 | 留下无 UI 的孤儿接口与技术债 |

## 权衡

| 优势 | 代价 |
|------|------|
| 教师操作简化：建任务一步到位，无需先配标准 | 教师失去自定义四维度权重的能力（改为系统固定基线） |
| 保留结构化四维度：雷达图/维度扣分/可追溯全部保留，守住赛事卖点 | 四维度权重固定，个性化仅通过文本细则实现 |
| 现有 AI 解析、雷达图、成绩链路零改动（AI 输出结构不变） | `PromptBuilder` prompt 变长，需注意 token 与转义 |
| 契合大模型结构化提示词工作方式 | R/S/R/O 文本质量依赖教师，需前端引导 |

## 后果

- ✅ 教师端页面数减少 2 页（25→23，教师 11→9）
- ✅ 固定四维度成为平台统一基线，呼应 PRD"统一评价标准"卖点
- ✅ 现有 AI 评分/雷达图/成绩链路零改动
- ✅ `EvaluationStandard`/`StandardDimension` 实体+Mapper 保留供 AI 读取
- ⚠️ PRD/FIP/API-Contract/Database-Design/Backend-Specification 等文档待实现后同步（决策 A）
- ⚠️ 历史任务仍读其原 `standardId` 的维度，不受影响；新任务固定指向 id=1

## 受影响的文档

| 文档 | 路径 | 更新状态 |
|------|------|---------|
| 设计文档 | `docs/superpowers/specs/2026-07-09-grading-rule-fusion-design.md` | ✅ 已创建 |
| PRD | `docs/01-PRD.md` | ⚠️ 待更新（实现后） |
| FIP | `docs/07-FIP.md` | ⚠️ 待更新（实现后） |
| API Contract | `docs/13-API-Contract.md` | ⚠️ 待更新（实现后） |
| Database Design | `docs/11-Database-Design.md` | ⚠️ 待更新（实现后） |
| Backend Specification | `docs/12-Backend-Specification.md` | ⚠️ 待更新（实现后） |
| SDS / MVP / DoD / Frontend Spec / Architecture Baseline / Backend Arch | `docs/02,08,09,05,00,10-*.md` | ⚠️ 建议更新（实现后） |
| UI Design System / Component Library / 原型 | `docs/03,04-*.md`、`pt/` | 无需修改 |

## 后续行动

1. **实现**：按设计文档第 7 节顺序（后端→前端→联调）
2. **文档同步**：实现完成后按决策 A 更新"必改"5 份文档
3. **CHANGELOG 补录**：记录本次变更
