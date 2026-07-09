# ADR-011：角色体系简化（4 角色 → 3 角色）

- **日期：** 2026-07-02
- **状态：** Accepted
- **决策者：** 项目负责人

---

## 上下文

系统最初设计为四角色体系：

| 角色 | 职责范围 |
|------|---------|
| 学生（Student） | 查看课程、完成实训任务、提交成果、查看个人成长报告 |
| 教师（Teacher） | 创建课程、发布实训、配置评价标准、审核学生成果、查看班级报表 |
| 教研负责人（Director） | 管理教师团队、教学质量分析、跨班级统计、教学资源统筹 |
| 管理员（Administrator） | 用户管理、系统配置、角色权限管理、运维监控 |

在原型评审（PT 原型）与 MVP 范围分析过程中发现，教研负责人（Director）角色与教师（Teacher）角色存在显著功能重叠——教研负责人的核心操作（课程管理、实训发布、评价标准配置、成果审核）与教师角色高度重合，差异仅在于教研负责人可跨班级进行统计分析与教师团队管理。

独立维护教研负责人角色将引入以下问题：

- **页面冗余：** 需额外开发 5+ 个独立页面（教研仪表盘、教师管理、跨班统计等），而其中约 80% 的 UI 与教师端页面结构相似
- **权限复杂度：** 四角色 RBAC 权限矩阵在 MVP 阶段过度设计，增加 Sprint 4 约 7 人天的实现成本
- **MVP 价值稀释：** 协作类功能（教师团队管理、教学资源统筹）依赖多教师协同数据，在 MVP 阶段缺乏真实使用场景
- **与 PT 原型不一致：** PT 原型经用户确认仅包含 3 个角色、24 个页面，继续保留四角色将导致实现偏离已确认的原型

## 决策

**将教研负责人（Director）职能合并至教师（Teacher）角色，最终 MVP 角色体系简化为三种角色。**

| 角色 | 职责范围 |
|------|---------|
| 学生（Student） | 查看课程、完成实训任务、提交成果、查看个人成长报告 |
| 教师（Teacher） | 创建课程、发布实训、配置评价标准、审核成果、查看班级报表、**教学管理（原教研负责人职能）** |
| 管理员（Administrator） | 用户管理、系统配置、角色权限管理、运维监控 |

具体措施：

1. 前端不再开发独立的教研负责人模块（`src/pages/director/` 目录废弃）
2. 原教研负责人拥有的教学管理、跨班统计、报表查看等功能由教师角色承载
3. 所有原 `/api/v1/director/` 端点迁移至 `/api/v1/teacher/`，权限码 `DIRECTOR` 合并至 `TEACHER`
4. 角色枚举从 `STUDENT / TEACHER / DIRECTOR / ADMIN` 变更为 `STUDENT / TEACHER / ADMIN`

## 备选方案

| 方案 | 未采纳原因 |
|------|-----------|
| 保留四角色体系 | 增加 5+ 页面、权限矩阵复杂度上升约 30%，MVP 交付周期延长约 7 人天；教研负责人与教师约 80% 功能重叠，MVP 阶段无独立存在必要 |
| 将教研负责人合并至管理员 | 违反职责分离原则——管理员应聚焦系统运维与用户管理，不应承担教学业务职能（课程管理、实训发布、成果审核等） |
| 完全移除教研负责人功能 | 教学管理类功能（跨班统计、教学质量分析、报表导出）是产品核心差异化能力，不可移除，应由教师角色承接 |

## 权衡

| 优势 | 代价 |
|------|------|
| 权限模型从 4 角色简化为 3 角色，RBAC 矩阵复杂度显著降低 | 未来企业版（v2.0+）若需要独立教研负责人角色，需重新拆分教师角色职能 |
| 页面总数从约 35 页缩减至约 24 页，与 PT 原型精确对齐 | 教师角色职责范围扩大，需在 UI 信息架构中清晰区分"教学操作"与"教学管理操作" |
| MVP 总人天从约 140.5 降至 129.5，Sprint 4 可聚焦教师扩展功能而非独立角色模块 | 教师端页面数量增多（约 9 页），需合理组织导航结构 |
| 与已确认的 PT 原型保持一致，降低需求变更风险 | |
| Sprint 4 从"教研负责人模块"重命名为"教师扩展模块"，语义更准确 | |

## 后果

- ✅ 权限模型简化，RBAC 实现更清晰，`meta.roles` 可选项从 4 项缩减为 3 项
- ✅ 页面总数减少，MVP 交付风险降低，Sprint 规划更紧凑
- ✅ Sprint 4 重命名为"教师扩展模块（含教研负责人职能）"，聚焦教学管理增强功能
- ✅ 所有 API Mock 端点已从 `/api/v1/director/` 迁移至 `/api/v1/teacher/`
- ✅ 所有开发规范文档（FIP、FIP-GUIDE、API-MOCK-SPEC-GUIDE、SPRINT-SPEC-GUIDE、PAGE-ANALYSIS-GUIDE、FRONTEND-SPEC-GUIDE、COMPONENT-LIBRARY-GUIDE、DESIGN-SYSTEM-GUIDE、SDS-GUIDE、PRD-RESTRUCTURE）已同步更新为三角色体系
- ⚠️ PRD v2.0（`docs/01-PRD.md`）和 SDS v1.0（`docs/02-SDS.md`）当前仍描述四角色体系，需在下一轮文档修订中同步更新
- ⚠️ 若竞赛评委或企业用户期望独立教研负责人角色，需在 v2.0 中重新评估并拆分教师角色

## 受影响的文档

| 文档 | 路径 | 更新状态 |
|------|------|---------|
| PRD | `docs/01-PRD.md` | ⚠️ 待更新（仍为四角色） |
| SDS | `docs/02-SDS.md` | ⚠️ 待更新（仍含 ResearchService 与 RESEARCH 角色码） |
| FIP | `docs/07-FIP.md` | ✅ 已更新 |
| Sprint 1 计划 | `sprints/sprint-1.md` | ✅ 已更新 |
| API Mock Specification | `docs/06-API-Mock-Specification.md` | ✅ 已更新 |
| MVP | `docs/08-MVP.md` | ✅ 已更新 |
| Definition of Done | `docs/09-Definition-of-Done.md` | ✅ 已更新 |
| UI Design System | `docs/03-UI-Design-System.md` | 无需修改（不含角色引用） |
| Component Library | `docs/04-Component-Library-v1.0.md` | 无需修改（不含角色引用） |
| Frontend Spec Guide | `rules/FRONTEND-SPEC-GUIDE.md` | ✅ 已更新 |
| Component Library Guide | `rules/COMPONENT-LIBRARY-GUIDE.md` | ✅ 已更新 |
| Design System Guide | `rules/DESIGN-SYSTEM-GUIDE.md` | ✅ 已更新 |
| SDS Guide | `rules/SDS-GUIDE.md` | ✅ 已更新 |
| FIP Guide | `rules/FIP-GUIDE.md` | ✅ 已更新 |
| API Mock Spec Guide | `rules/API-MOCK-SPEC-GUIDE.md` | ✅ 已更新 |
| Sprint Spec Guide | `docs/engineering/SPRINT-SPEC-GUIDE.md` | ✅ 已更新 |
| Page Analysis Guide | `docs/engineering/PAGE-ANALYSIS-GUIDE.md` | ✅ 已更新 |
| PRD Restructure Guide | `rules/PRD-RESTRUCTURE.md` | ✅ 已更新 |
| Codex Rules | `.codex/rules.md` | ✅ 已更新 |
| ADR | `docs/engineering/ADR.md` | ✅ ADR-001 已引用三角色 |

## 后续行动

1. **PRD 更新（下一轮文档修订）：** 将 `docs/01-PRD.md` 从四角色更新为三角色体系，移除第 5.3 节中的教研负责人独立描述，更新第 10.3、14、15 节中的角色引用
2. **SDS 更新（下一轮文档修订）：** 移除 `docs/02-SDS.md` 中的 `ResearchService`、`RESEARCH` 角色码及相关时序图中的教研负责人参与者
3. **清理遗留目录：** 删除空目录 `B1_Platform/src/pages/director/`
4. **CHANGELOG 补录：** 在下一版本 CHANGELOG 中记录本次角色体系变更

---

> **参考资料：**
> - `docs/01-PRD.md`（v2.0，待更新为三角色）
> - `docs/02-SDS.md`（v1.0，待更新为三角色）
> - `docs/07-FIP.md`（已更新为三角色体系）
> - `docs/06-API-Mock-Specification.md`（已更新）
> - `.codex/rules.md`（已修正 GUIDE 文件路径）
> - `docs/engineering/ADR.md`（ADR-001 已引用三角色）
