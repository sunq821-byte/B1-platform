# Frontend Specification v1.0

## 基于大模型的软件实训教学检查评价与报表系统

---

**文档状态**：正式发布

**文档版本**：v1.0

**适用范围**：本项目全部前端开发工作

**文档定位**：本项目前端开发最高规范文档，所有开发人员必须严格遵守

**前置文档**：PRD v1.0、SDS v1.0、UI Design System v1.0、Component Library v1.0

**技术栈**：Vue 3 + TypeScript + Vite + Pinia + Vue Router + Axios + Element Plus + Mock.js + Lucide Icons + ECharts

**角色体系**：学生 / 教师（含科研负责人职能） / 管理员

---

## 目录

1. [开发原则](#第一章-开发原则)
2. [项目目录规范](#第二章-项目目录规范)
3. [Vue组件开发规范](#第三章-vue组件开发规范)
4. [页面开发规范](#第四章-页面开发规范)
5. [路由规范](#第五章-路由规范)
6. [Pinia规范](#第六章-pinia规范)
7. [Composable规范](#第七章-composable规范)
8. [Axios规范](#第八章-axios规范)
9. [Mock规范](#第九章-mock规范)
10. [TypeScript规范](#第十章-typescript规范)
11. [状态管理规范](#第十一章-状态管理规范)
12. [组件通信规范](#第十二章-组件通信规范)
13. [权限控制规范](#第十三章-权限控制规范)
14. [表单开发规范](#第十四章-表单开发规范)
15. [列表开发规范](#第十五章-列表开发规范)
16. [图表开发规范](#第十六章-图表开发规范)
17. [AI模块开发规范](#第十七章-ai模块开发规范)
18. [性能优化规范](#第十八章-性能优化规范)
19. [安全开发规范](#第十九章-安全开发规范)
20. [国际化规范（预留）](#第二十章-国际化规范预留)
21. [日志规范](#第二十一章-日志规范)
22. [代码规范](#第二十二章-代码规范)
23. [Git协作规范](#第二十三章-git协作规范)
24. [开发Checklist](#第二十四章-开发checklist)


---

## 术语约定

全文统一使用以下术语，不得混用或自行创造新术语：

| 术语 | 说明 |
|------|------|
| 页面 | Vue Router 对应的页面级组件，位于 src/pages/ |
| 组件 | 可复用的 Vue 组件，位于 src/components/ |
| Store | Pinia Store 实例，位于 src/stores/ |
| Composable | Vue Composition API 的可复用逻辑单元，位于 src/composables/ |
| Hooks | 业务逻辑 Hooks，位于 src/hooks/ |
| Utils | 纯工具函数，位于 src/utils/，不依赖 Vue 响应式系统 |
| API 层 | src/api/ 目录下的接口封装模块，页面不得绕过 API 层直接调用 axios |
| Mock | Mock.js 模拟数据，位于 src/mock/ |
| DTO | Data Transfer Object，接口传输对象，定义在 src/types/ |
| VO | View Object，视图展示对象，定义在 src/types/ |
| AI 模块 | 与大模型交互的前端模块，包含特定角色 |
| Token | JWT 认证令牌 |
| RBAC | 基于角色的访问控制模型 |
| Design System | 指 UI Design System v1.0 文档中定义的视觉标准 |
| Component Library | 指 Component Library v1.0 文档中定义的组件规范 |

---


# 第一章 开发原则

## 1.1 章节概述

本章阐述项目技术选型的核心理由和开发方法论。每一个技术选择都基于项目实际需求、团队规模和长远目标进行判断。后续所有章节的规范均以本章原则为理论基础。

## 1.2 核心原则

- **工程化优先**：所有技术选择服务于团队协作和长期维护，不追求新奇。
- **类型安全**：TypeScript 是强制要求，不是可选项。所有代码必须在编译期消除类型错误。
- **逻辑复用**：Composition API 与 Composable 是实现逻辑复用的核心手段，禁止出现粘贴复制式代码。
- **规范驱动**：开发行为在规范约束下进行，不写没有规范定义的代码。
- **质量内建**：质量是开发过程的一部分，不是开发完成后的检查项。

## 1.3 为什么采用 Vue 3

选择 Vue 3 作为前端框架，基于以下理由：

**性能优势**：Vue 3 的 Proxy-based 响应式系统相比 Vue 2 的 Object.defineProperty，在大型数据表格和频繁数据更新的场景下有显著的性能优势。虚拟 DOM 重写后渲染速度提升约 1.3 至 2 倍，对于本项目的 Dashboard 多图表渲染和 AI 流式输出场景至关重要。

**Composition API**：Vue 3 的 Composition API 解决了 Vue 2 Options API 在大型组件中的逻辑分散问题。在本项目的教师复核页面（预计超过 300 行逻辑）、AI 分析页面等复杂场景下，Composition API 能够将相关逻辑组织在一起，而非分散在 data、methods、computed 等选项中。

**TypeScript 支持**：Vue 3 从底层使用 TypeScript 重写，提供完整类型推导。对于本项目需要严格类型约束的 DTO/VO 定义、API 响应类型校验、Pinia Store 类型推导等场景，Vue 3 的类型支持是决定性的选型因素。

**生态成熟度**：Vue 3 的配套工具链（Vite、Pinia、Vue Router 4、Vue DevTools）均已达到生产可用级别，社区活跃，问题解决路径清晰。

**对比 React**：团队在 Composition API 和 React Hooks 之间选择了前者，原因包括：Vue 的响应式系统无需手动管理依赖数组，模板语法对 HTML/CSS 开发者更友好，单文件组件（SFC）天然将模板、逻辑和样式组织在一起，降低认知切换成本。

## 1.4 为什么采用 Composition API

**逻辑聚合**：对于本项目的复杂页面（如教师复核页面需同时处理评分列表、AI 置信度展示、覆写面板、批量操作），Composition API 将同一功能的相关代码聚合到一个 Composable 中，而非散落在 Options API 的多个选项中。

**更好的类型推导**：Composition API 在 TypeScript 下有更准确、更简洁的类型推导，不需要 Options API 中繁琐的 this 类型声明。

**代码复用**：通过 Composable 实现逻辑复用，避免 Mixin 的命名冲突和来源模糊问题。本项目的分页逻辑、表单校验、权限检查等通用逻辑均通过 Composable 实现。

**Tree-Shaking 友好**：按需引入 API（ref、computed、watch 等），打包体积更小。

**可测试性**：Composable 是纯函数组合，天然支持单元测试，无需挂载 Vue 组件即可测试业务逻辑。

## 1.5 为什么采用 TypeScript

**类型安全**：本项目的核心数据流（API 响应 → Store → 组件 Props）路径长且数据量大（每条评分记录含 10+ 维度），TypeScript 在编译期捕获类型错误，避免运行时因字段拼写错误或类型不匹配导致的 Bug。

**接口契约**：DTO 和 VO 的类型定义作为前后端接口契约的一部分。后端定义的返回结构必须与前端 TypeScript 类型声明一致，从工具层面确保接口规范落地。

**智能提示**：在 VS Code 中，TypeScript 提供精确的属性名和类型提示。对于组件 Props 多达 10+ 的复杂业务组件（如 AIScoreCard、TeacherOverridePanel），类型提示显著降低开发人员记忆负担。

**重构安全**：TypeScript 的静态分析使得重命名属性、调整接口结构等重构操作能够被安全执行，IDE 自动追踪所有引用并更新。

**禁止 any**：本项目禁止使用 any 类型。对于确实无法确定类型的情况，使用 unknown 并通过类型守卫收窄。


## 1.6 为什么采用 Pinia

**对比 Vuex 的选择**：

- Pinia 提供完整的 TypeScript 类型推导，Vuex 的类型支持需要大量模板代码。
- Pinia 移除了 mutations 概念，actions 支持异步操作，概念更简洁。
- Pinia 支持多个 Store 实例，无需 modules 嵌套，符合本项目按业务域（用户、课程、实训、AI 等）划分 Store 的架构需求。
- Pinia 的 DevTools 支持时间旅行调试、状态快照等功能，调试体验优于 Vuex。
- Pinia 是 Vue 官方推荐的下一代状态管理方案，Vuex 已进入维护模式。

## 1.7 为什么采用 Mock First

**前后端并行开发**：Mock.js 在前端开发初期模拟全部 API 接口，前端不等待后端接口就绪即可完成页面开发和联调。Mock 数据结构必须与真实 API 规范一致，届时仅需切换 baseURL。

**边界场景覆盖**：Mock 可以模拟空数据、错误响应、超时、大数据量等边界场景，前端在开发阶段即可完成全面的状态处理逻辑。

**文档即规范**：Mock 模块的接口定义本身成为 API 规范的具象化表达，比纯文档更直观、更不易产生歧义。

**禁止事项**：禁止在 Mock 中使用与真实 API 不一致的数据结构；禁止在生产环境中保留 Mock 代码。

## 1.8 为什么采用 Component First

**复用优先**：任何页面开发前，必须先检查 Component Library 中是否已有可用组件。优先使用已有组件组合页面，而非从零开始。

**原子化构建**：页面由组件组合而成，组件由 Base 组件组合而成。遵循 Atomic Design 的分层理念（Atoms → Molecules → Organisms → Templates → Pages），确保从最小粒度到页面级的代码复用。

**禁止重复 UI**：相同 UI 模式在一个地方定义、全局使用。如果发现两个页面存在相同的 UI 模式，必须抽取为组件，不得保留两份代码。

**组件分类**：Base（基础组件）、Layout（布局组件）、Business（业务组件）、AI（AI 组件）、Chart（图表组件）、Form（表单组件）、Feedback（反馈组件）、Permission（权限组件）、Utility（工具组件），共九大类，参见 Component Library v1.0。

## 1.9 为什么采用 Specification First

**规范先行**：任何开发动作之前，必须先完成对应规范的定义。规范文档是代码的前置条件，不得跳过设计阶段直接编写代码。

**一致性保障**：统一规范消除个人编码风格差异，确保 50+ 页面的一致性。新成员通过阅读规范文档即可理解项目约定，降低上手成本。

**可审查性**：代码审查以规范文档为基准，审查者只需检查代码是否符合规范，而非争论风格偏好。

## 1.10 为什么采用 MVP 开发

**最小可行产品**：先完成核心业务流程的端到端闭环（学生提交实训 → AI 分析 → 教师复核 → 成绩发布），再逐步增加辅助功能（通知系统、高级报表、知识库等）。

**进度可控**：MVP 策略确保在每个迭代周期内都能交付可演示的成果，降低项目风险。

**禁止提前开发非核心功能**：任何功能的开发前必须回答"该功能是否属于 MVP"。如果不属于，则推迟至后续迭代。

## 1.11 本章禁止事项

- 禁止跳过规范阶段直接编写代码。
- 禁止使用 Options API。
- 禁止使用 JavaScript 编写业务代码。
- 禁止使用 Vuex。
- 禁止使用 any 类型。
- 禁止页面直接复制已有组件代码而非复用。
- 禁止等待后端接口就绪才启动前端开发。

## 1.12 本章检查清单

- 是否理解每个技术选型的"为什么"？
- 是否确认所有代码基于 Composition API + TypeScript + Pinia 开发？
- 是否确认遵循 Mock First → Component First → Specification First 的开发顺序？
- 是否确认 MVP 范围并在其框架内规划工作？


---

# 第二章 项目目录规范

## 2.1 章节概述

本章定义前端项目的完整目录结构，明确每个目录的职责边界和使用规则。目录结构是项目组织的基础骨架，所有新增目录必须符合本章规范。

## 2.2 核心原则

- **职责单一**：每个目录只承担一种职责，不出现职责交叉或模糊的目录。
- **命名统一**：目录使用 kebab-case 命名，组件目录使用 PascalCase 命名。
- **扁平化上限**：目录嵌套深度不超过 4 层，超过 4 层说明组织方式需要调整。
- **禁止随意创建**：新增目录必须经过评审并更新本章规范文档。

## 2.3 完整目录结构

```text
B1_Platform/
├── public/                   # 静态资源，直接复制到构建产物
│   └── favicon.ico
├── src/
│   ├── api/                  # API 接口封装层
│   │   ├── modules/          # 按业务域分模块
│   │   │   ├── user.api.ts
│   │   │   ├── course.api.ts
│   │   │   ├── training.api.ts
│   │   │   ├── submission.api.ts
│   │   │   ├── review.api.ts
│   │   │   ├── report.api.ts
│   │   │   ├── research.api.ts
│   │   │   ├── system.api.ts
│   │   │   └── ai.api.ts
│   │   ├── request.ts        # Axios 实例和拦截器
│   │   └── index.ts          # 统一导出
│   ├── assets/               # 静态资源，经构建工具处理
│   │   ├── images/
│   │   └── icons/
│   ├── components/           # 全局可复用组件
│   │   ├── base/             # Base Components（基础组件）
│   │   ├── layout/           # Layout Components（布局组件）
│   │   ├── business/         # Business Components（业务组件）
│   │   ├── ai/               # AI Components（AI 组件）
│   │   ├── charts/           # Chart Components（图表组件）
│   │   ├── forms/            # Form Components（表单组件）
│   │   ├── feedback/         # Feedback Components（反馈组件）
│   │   ├── permission/       # Permission Components（权限组件）
│   │   └── utility/          # Utility Components（工具组件）
│   ├── composables/          # 全局 Composable
│   │   ├── usePagination.ts
│   │   ├── useTable.ts
│   │   ├── useForm.ts
│   │   ├── usePermission.ts
│   │   ├── useUpload.ts
│   │   └── useExport.ts
│   ├── hooks/                # 业务 Hooks
│   │   ├── useUser.ts
│   │   ├── useCourse.ts
│   │   └── useAI.ts
│   ├── layouts/              # 布局框架
│   │   ├── DefaultLayout.vue
│   │   ├── AdminLayout.vue
│   │   └── AuthLayout.vue
│   ├── pages/                # 页面级组件
│   │   ├── dashboard/        # 仪表盘
│   │   ├── training/         # 实训中心
│   │   ├── submission/       # 成果提交
│   │   ├── review/           # 教师复核
│   │   ├── report/           # 报表中心
│   │   ├── system/           # 系统管理
│   │   └── profile/          # 个人中心
│   ├── router/               # 路由配置
│   │   ├── index.ts
│   │   ├── routes.ts
│   │   └── guards.ts
│   ├── stores/               # Pinia Store
│   │   ├── user.store.ts
│   │   ├── app.store.ts
│   │   ├── training.store.ts
│   │   ├── review.store.ts
│   │   └── permission.store.ts
│   ├── types/                # 全局类型定义
│   │   ├── global.d.ts
│   │   ├── api.d.ts
│   │   ├── user.d.ts
│   │   ├── training.d.ts
│   │   └── ai.d.ts
│   ├── constants/            # 全局常量
│   │   ├── index.ts
│   │   └── enums.ts
│   ├── utils/                # 纯工具函数
│   │   ├── format.ts
│   │   ├── validate.ts
│   │   ├── storage.ts
│   │   └── date.ts
│   ├── mock/                 # Mock 数据
│   │   ├── user.mock.ts
│   │   ├── training.mock.ts
│   │   ├── review.mock.ts
│   │   └── index.ts
│   ├── styles/               # 全局样式
│   │   ├── variables.css
│   │   ├── reset.css
│   │   └── global.css
│   ├── plugins/              # 插件配置
│   │   ├── element-plus.ts
│   │   └── echarts.ts
│   ├── config/               # 应用配置
│   │   └── index.ts
│   ├── App.vue               # 根组件
│   └── main.ts               # 入口文件
├── .env.development          # 开发环境变量
├── .env.production           # 生产环境变量
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
└── README.md
```

## 2.4 目录职责说明

| 路径 | 职责 | 依赖方向 |
|------|------|----------|
| src/api/ | 封装所有 HTTP 请求，页面和组件不得直接调用 axios | 被 pages/stores/hooks 依赖 |
| src/assets/ | 存放图片、图标等静态资源，经 Vite 构建处理 | 被所有组件引用 |
| src/components/ | 存放所有可复用组件，分类管理 | 只依赖 types/、composables/、utils/ |
| src/composables/ | 存放全局 Composable，实现跨组件逻辑复用 | 只依赖 types/、utils/ |
| src/hooks/ | 存放业务 Hooks，封装特定业务域的操作逻辑 | 依赖 stores/、api/ |
| src/layouts/ | 存放页面布局框架，定义页面骨架 | 依赖 components/、router/ |
| src/pages/ | 存放页面级组件，一个路由对应一个目录 | 依赖 components/、hooks/、stores/ |
| src/router/ | 定义所有路由和导航守卫 | 依赖 pages/、stores/ |
| src/stores/ | 管理全局状态 | 依赖 api/、types/ |
| src/types/ | 定义全局 TypeScript 类型 | 不依赖任何其他目录 |
| src/constants/ | 定义全局常量、枚举 | 不依赖任何其他目录 |
| src/utils/ | 存放纯工具函数，不依赖 Vue 响应式系统 | 不依赖任何其他目录 |
| src/mock/ | 模拟后端接口响应 | 不依赖任何其他目录（开发环境） |
| src/styles/ | 定义全局样式变量和基础样式 | 不依赖任何其他目录 |
| src/plugins/ | 配置第三方插件 | 不依赖其他目录 |
| src/config/ | 应用级配置 | 不依赖其他目录 |

## 2.5 组件目录结构规范

每个组件目录必须包含以下结构：

```text
ComponentName/
├── index.vue           # 组件主文件（必需）
├── types.ts            # 组件专属类型定义（当 Props/Emits 超过 3 个时必需）
├── composables/        # 组件专属 Composable（当逻辑超过 50 行时需抽取）
└── __tests__/          # 组件单元测试（必需）
    └── ComponentName.spec.ts
```

## 2.6 页面目录结构规范

每个页面目录必须包含以下结构：

```text
page-name/
├── index.vue           # 页面主入口（必需）
├── components/         # 页面专属组件（非全局复用组件）
├── composables/        # 页面专属 Composable（逻辑超过 100 行时必须拆分）
├── types.ts            # 页面专属类型定义
└── __tests__/          # 页面测试
```

## 2.7 本章禁止事项

- 禁止创建 temp、test2、new、copy、final_final 等无意义目录名。
- 禁止目录层级超过 4 层。
- 禁止组件目录缺少 index.vue 主文件。
- 禁止在 pages/ 下直接编写可被其他页面复用的组件。
- 禁止页面组件直接引用 src/components/ 以外的任意页面目录下的文件。

## 2.8 本章检查清单

- 所有目录命名是否遵循 kebab-case（目录）和 PascalCase（组件目录）？
- 每个目录是否职责单一，不存在职责重叠？
- 是否确认没有超过 4 层的目录嵌套？
- 新增目录是否已更新本章规范文档？


---

# 第三章 Vue组件开发规范

## 3.1 章节概述

本章定义 Vue 组件开发的技术标准，涵盖组件结构、Props 定义、Emits 声明、Slots 使用、模板规范和样式规范。所有 Vue 组件必须严格遵守本章规定。

## 3.2 核心原则

- **Composition API 强制**：所有组件使用 script setup 语法编写。
- **类型驱动**：Props、Emits、Slots 必须使用 TypeScript 类型声明。
- **单一职责**：每个组件只负责一件事，职责边界清晰。
- **状态覆盖**：每个数据展示型组件必须覆盖 Loading、Empty、Error、正常渲染四种状态。
- **样式隔离**：组件样式使用 scoped 或 CSS Modules，禁止样式泄漏到父级或兄弟组件。

## 3.3 组件结构规范

组件文件组织顺序必须为：

1. script setup（逻辑层）：使用 Composition API 编写组件逻辑。
2. template（模板层）：使用 Vue 模板语法编写 UI 结构。
3. style scoped（样式层）：使用 scoped CSS 编写组件样式。

每个区块之间空一行分隔。组件内逻辑组织顺序：TypeScript 类型导入 → Props/Emits/Slots 定义 → Composable 引用 → Store 引用 → 响应式状态 → 计算属性 → 方法 → 生命周期钩子。

## 3.4 Props 规范

**类型声明**：所有 Props 必须使用 TypeScript 的 interface 或 type 进行类型声明，使用 defineProps 的泛型语法。禁止使用 Vue 2 风格的 PropTypes 字符串声明。

**必填标记**：所有必填 Props 必须标记 required: true。非必填 Props 必须提供合理的默认值。

**命名规范**：Props 使用 camelCase 命名，在模板中通过 kebab-case 传递。

**数量限制**：单个组件的 Props 数量不得超过 8 个。超过 8 个说明组件职责不单一，需拆分子组件或合并为配置对象。

**禁止直接修改**：组件内部禁止直接修改 Props 值。如需修改，应通过 Emits 通知父组件。

## 3.5 Emits 规范

**类型声明**：所有 Emits 必须使用 TypeScript 类型声明，使用 defineEmits 的泛型语法。

**命名规范**：Emits 事件名使用 kebab-case，如 update:model-value、item-delete、form-submit。

**负载类型**：每个 Emit 事件的负载类型必须明确声明，不得使用 any。

**v-model 支持**：对于需要双向绑定的组件，必须同时声明 update:modelValue 事件和 modelValue prop，支持 Vue 3 的 v-model 语法。

## 3.6 Slots 规范

**类型声明**：使用 defineSlots 声明组件接受的 Slot 名称和作用域参数类型。

**命名规范**：具名 Slot 使用 kebab-case 命名，如 header-actions、table-empty、card-footer。

**文档要求**：每个 Slot 的用途和传递的作用域变量必须在组件 types.ts 中以 JSDoc 注释说明。

**默认内容**：对于非必需的 Slot，应提供合理的默认内容或 fallback 渲染。

## 3.7 模板规范

**指令使用**：优先使用 v-if/v-else-if/v-else 进行条件渲染，v-show 仅用于频繁切换的场景。v-for 必须配合 :key 使用，key 值使用数据中的唯一标识字段，禁止使用 index 作为 key。

**表达式复杂度**：模板中的表达式不得超过一行。复杂的逻辑判断必须抽取为计算属性或方法。

**指令缩写**：统一使用 : 替代 v-bind:，@ 替代 v-on:。

**元素限制**：模板中不得出现超过 3 层的标签嵌套。超过 3 层说明组件需要拆分子组件。

**属性换行**：当元素属性超过 3 个时，每个属性必须独占一行。

## 3.8 样式规范

**Scoped 强制**：所有组件样式必须使用 scoped 属性，禁止使用全局样式影响其他组件。

**Design Tokens 引用**：所有颜色值、间距值、圆角值必须引用 Design System 中定义的 CSS 变量，禁止硬编码具体数值。例如：使用 var(--color-primary) 而非 #3B82F6。

**深度选择器**：需要穿透子组件样式时，使用 :deep() 伪类选择器，禁止使用已废弃的 ::v-deep 或 /deep/。

**CSS 预处理器**：本项目使用原生 CSS 配合 CSS 变量。如需使用预处理器，统一使用 SCSS。

**响应式规范**：使用 Design System 定义的断点进行响应式布局，禁止自定义断点数值。

## 3.9 组件命名规范

**文件命名**：组件文件使用 PascalCase 命名。单文件组件命名为 index.vue，放在以组件名命名的目录中。

**组件注册名**：组件 name 属性使用 PascalCase，与文件名保持一致。

**分类前缀**：基础组件使用 Base 前缀（如 BaseButton），AI 组件使用 AI 前缀（如 AIScoreCard），布局组件、业务组件、图表组件等不使用前缀。

**递归组件**：需要递归使用的组件必须明确设置 name 属性。

## 3.10 全局状态覆盖规范

每个数据展示型组件必须完整覆盖以下状态：

| 状态 | 说明 | UI 表现 |
|------|------|---------|
| Loading | 数据加载中 | 骨架屏（BaseSkeleton）或旋转加载指示器 |
| Empty | 数据为空 | 空状态插图 + 描述文案 + 操作引导（BaseEmpty） |
| Error | 加载或操作失败 | 错误提示 + 重试按钮 |
| 正常渲染 | 数据正常展示 | 按照设计规范正常渲染数据 |

对于 AI 相关组件，还需额外覆盖：

| 状态 | 说明 | UI 表现 |
|------|------|---------|
| AIAnalyzing | AI 分析中 | 脉冲动画 + "AI 分析中..." 提示（AIStatusBadge） |
| AIConfidence | AI 置信度展示 | 三级置信度标识（高/中/低）配合对应颜色（AIConfidenceTag） |

## 3.11 本章禁止事项

- 禁止使用 Options API。
- 禁止使用 Vue 2 的属性声明方式。
- 禁止在模板中使用超过一行的表达式。
- 禁止硬编码颜色、间距、圆角数值。
- 禁止组件样式不使用 scoped。
- 禁止使用 index 作为 v-for 的 key。
- 禁止 Props 数量超过 8 个。
- 禁止模板嵌套超过 3 层。
- 禁止数据展示型组件不处理 Loading/Empty/Error 状态。
- 禁止组件内部直接修改 Props。

## 3.12 本章检查清单

- 是否使用 script setup 语法？
- Props/Emits/Slots 是否使用 TypeScript 类型声明？
- 是否覆盖 Loading/Empty/Error 三种状态？
- 样式是否引用了 Design Tokens CSS 变量？
- v-for 的 key 是否使用唯一标识字段？
- 模板表达式是否不超过一行？


---

# 第四章 页面开发规范

## 4.1 章节概述

本章定义页面级组件的开发流程和技术标准。页面是用户直接交互的完整视图单元，对应 Vue Router 的一条路由。页面开发必须在完成 Component Library 中组件评估后进行。

## 4.2 核心原则

- **组件组合**：页面由组件组合而成，页面本身仅负责组装和编排，不包含可复用 UI 逻辑。
- **布局一致**：所有页面使用统一的布局框架（参见 Layout Components），确保导航和操作区域位置一致。
- **流程标准化**：页面开发遵循明确步骤：需求分析 → Component Library 检查 → 页面设计 → Mock 接口 → 页面开发 → 测试。
- **状态驱动**：页面状态由 Store 管理，页面逻辑由 Composable 封装，页面模板仅做渲染。

## 4.3 页面开发流程

**步骤一：需求分析**。确认页面的业务目标、用户角色、核心操作流程。输出页面状态图，明确页面有哪些状态及状态间的转换。

**步骤二：Component Library 检查**。列出页面所需的所有 UI 元素，逐一在 Component Library 中查找已有组件。对于找到的组件，记录组件名称和使用方式；对于未找到的，标记为"需新建组件"或"需扩展已有组件"。

**步骤三：页面设计**。基于 UI Design System 和 Component Library 进行页面布局设计。输出页面线框图，标注使用的组件、数据流向和交互逻辑。

**步骤四：Mock 接口**。根据 API 规范编写 Mock 接口，模拟页面所需的所有数据请求。Mock 数据结构必须与真实 API 承诺的结构完全一致。

**步骤五：页面开发**。使用 Layout Components 搭建页面骨架，使用 Business/Base/AI 组件填充内容区域。页面逻辑通过 Composable 和 Store 实现。

## 4.4 页面文件组织

每个页面目录必须包含：

- index.vue：页面主入口，负责组装布局和组件。
- components/：页面专属组件目录，存放仅在当前页面使用的组件。
- composables/：页面专属 Composable，页面逻辑超过 100 行时必须拆分到此目录。
- types.ts：页面专属类型定义，包含页面状态类型、表单数据类型等。

## 4.5 页面模板规范

**布局使用**：所有页面必须使用 AppLayout 布局组件包裹，不允许自定义全局布局结构。特殊布局需求（如登录页）使用 AuthLayout。

**页面头部**：每个页面必须包含 PageHeader 组件，展示页面标题、面包屑导航和主要操作按钮。

**内容区域**：使用 PageContainer 包裹页面主要内容，确保一致的页面内边距和最大宽度。

**禁止事项**：页面模板中禁止编写复杂的条件渲染逻辑（超过两层嵌套的 v-if/v-else）。禁止在页面模板中直接调用 API。禁止页面模板超过 200 行。

## 4.6 页面逻辑规范

**Composable 封装**：页面业务逻辑必须封装在 Composable 中，页面 index.vue 的 script setup 部分仅负责调用 Composable 并将返回值绑定到模板。

**Store 使用**：页面通过 Store 管理跨组件共享的状态。页面级临时状态（如表单输入、筛选条件）使用页面专属 Composable 管理，不放入全局 Store。

**API 调用**：页面不直接调用 API 层函数。API 调用封装在 Store actions 或 Hooks 中，页面通过调用 Store actions 或 Hooks 触发数据请求。

**生命周期使用**：优先使用组合式函数（watch、watchEffect）代替生命周期钩子处理副作用。onMounted 仅用于初始化逻辑。

## 4.7 页面状态管理

每个页面必须定义以下状态并在模板中处理：

- Loading：首次进入页面时数据加载状态。
- Empty：数据列表为空时的展示。
- Error：接口请求失败时的错误提示和重试机制。
- Refreshing：下拉刷新或手动刷新时的加载状态。
- Submitting：表单提交中的状态，防止重复提交。
- 正常状态：数据正常加载完成的展示。

## 4.8 页面 KeepAlive 策略

对于列表-详情类型的页面组合：

- 列表页配置 KeepAlive 缓存，避免返回时重新加载和重置筛选条件。
- 详情页不缓存，确保每次进入展示最新数据。
- 搜索条件和分页位置通过 Store 或路由 query 参数持久化。

## 4.9 本章禁止事项

- 禁止页面直接调用 axios 或 API 层函数。
- 禁止页面模板超过 200 行。
- 禁止页面逻辑超过 100 行（script setup 部分）。
- 禁止页面内直接定义可被其他页面复用的组件。
- 禁止页面模板出现超过两层嵌套的条件渲染。
- 禁止跳过 Component Library 检查直接开发新组件。
- 禁止页面不处理 Loading/Empty/Error 状态。

## 4.10 本章检查清单

- 是否在开发前完成了 Component Library 检查？
- 页面模板是否不超过 200 行？
- 页面逻辑是否封装在 Composable 中？
- 是否覆盖 Loading/Empty/Error 状态？
- 是否使用了 AppLayout 和 PageHeader？
- 列表页是否配置了 KeepAlive？


---

# 第五章 路由规范

## 5.1 章节概述

本章定义 Vue Router 的配置标准，涵盖路由结构、命名规范、导航守卫和权限控制。路由是应用的骨架，其设计直接影响用户体验和应用可维护性。

## 5.2 核心原则

- **声明式配置**：路由通过静态配置定义，不支持后端动态下发路由。
- **权限前置**：路由访问权限在路由守卫中统一拦截，不在每个页面内独立判断。
- **懒加载强制**：所有页面级路由必须使用动态 import 实现懒加载。
- **语义化命名**：路由 name 必须具有明确的业务语义。

## 5.3 路由结构设计

路由按业务域和角色组织为三级结构：

- 一级路由：布局框架（DefaultLayout、AdminLayout、AuthLayout）
- 二级路由：业务域模块（dashboard、training、submission、review、report、research、system）
- 三级路由：具体功能页面（列表页、详情页、编辑页）

## 5.4 路由命名规范

**Path 命名**：路由路径使用 kebab-case，如 /training-tasks、/review/detail/:id。

**Name 命名**：路由名称使用 PascalCase，遵循"模块-页面"格式，如 TrainingList、ReviewDetail。

**Meta 信息**：每个路由必须声明 meta 对象，包含以下字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| title | string | 页面标题，用于 PageHeader 和浏览器标题栏 |
| requiresAuth | boolean | 是否需要登录认证 |
| roles | string[] | 允许访问的角色列表（student、teacher、research、admin） |
| keepAlive | boolean | 是否缓存页面组件 |
| icon | string | 侧边栏导航图标名称（Lucide Icons） |
| hideInMenu | boolean | 是否在侧边栏导航中隐藏 |

## 5.5 导航守卫规范

**全局前置守卫（beforeEach）**：检查用户认证状态。未登录用户访问需要认证的页面时，重定向到登录页并记录目标路径，登录后自动跳转回目标页。

**全局解析守卫（beforeResolve）**：加载用户权限信息。如果权限数据尚未加载，在此阶段从后端获取并更新 Permission Store。

**路由独享守卫（beforeEnter）**：在路由配置中定义角色检查。如果用户角色不在路由声明的允许角色列表中，重定向到 403 无权限页面。

**组件内守卫**：原则上不使用组件内守卫（beforeRouteEnter/beforeRouteUpdate）。路由相关逻辑在全局守卫中处理。

## 5.6 动态路由

动态路由使用冒号参数语法定义，如 /training/:id。参数获取通过 useRoute 的 params 属性，而不是从 URL 中手动解析。路由参数类型通过 TypeScript 类型扩展定义。

## 5.7 404 和错误路由

- 未匹配路由：定义 catch-all 路由（/:pathMatch(.*)*）跳转到 404 页面。
- 403 页面：当用户访问无权限页面时显示。
- 500 页面：当应用发生未捕获错误时显示。

## 5.8 本章禁止事项

- 禁止路由组件不使用懒加载。
- 禁止在页面内通过角色判断控制访问权限（应在路由守卫中处理）。
- 禁止路由 name 命名不遵循"模块-页面"格式。
- 禁止 Hard Code 路由字符串到页面代码中。
- 禁止使用组件内路由守卫。

## 5.9 本章检查清单

- 所有页面路由是否配置了懒加载？
- 所有路由是否声明了完整的 meta 信息？
- 权限控制是否在路由守卫中统一处理？
- 是否配置了 404/403 路由？
- 路由 name 和 path 是否符合命名规范？


---

# 第六章 Pinia规范

## 6.1 章节概述

本章定义 Pinia Store 的设计和使用规范。Pinia 是本项目的全局状态管理方案，所有跨组件共享的状态必须通过 Pinia Store 管理。

## 6.2 核心原则

- **按域拆分**：每个业务域用一个独立的 Store 管理，不将所有状态放入单一 Store。
- **扁平化状态**：Store 状态保持扁平化结构，嵌套深度不超过 3 层。
- **Actions 封装**：所有状态修改通过 actions 完成，组件不直接修改 state。
- **类型完备**：所有 Store 的状态、getter、action 参数和返回值必须有完整的 TypeScript 类型。

## 6.3 Store 定义规范

**Setup Store 语法**：使用 Setup Store 语法（函数式写法）定义 Store，与 Composition API 风格保持一致。

**Store 命名**：Store 文件名使用 kebab-case.store.ts 格式。Store 实例名使用 use + PascalCase + Store 格式，如 useUserStore、useTrainingStore。

**State 定义**：状态字段使用 ref 定义。简单值使用 ref，复杂对象使用 reactive。状态字段命名使用 camelCase。

**Getters 定义**：Getters 使用 computed 定义。Getters 仅用于派生状态，不包含副作用。Getters 必须有明确的返回类型声明。

**Actions 定义**：Actions 使用普通函数定义，支持异步操作。Actions 是唯一可以修改 state 的地方。外部调用 Store 的 actions 来触发状态变更，不得直接操作 state。

## 6.4 Store 使用规范

**组件引用**：组件通过调用 useXxxStore() 获取 Store 实例。组件内使用 storeToRefs 解构需要响应式的状态，使用普通解构获取 actions。

**调用位置**：组件内 Store 的调用放在 script setup 顶部，紧随 import 语句之后。

**Actions 调用**：所有跨组件的数据操作必须通过 Store actions 完成。actions 内部负责调用 API 层函数并更新状态。

## 6.5 Store 间通信

**依赖方向**：Store 之间可以相互引用，但必须遵循单向依赖。User Store 和 App Store 作为基础 Store，可被其他 Store 引用。业务 Store 之间避免循环依赖。

**共享状态**：如果多个 Store 需要共享同一份数据，将数据提升到更高层级的 Store 中，或通过 Composable 管理共享逻辑。

## 6.6 Store 持久化

**Token 存储**：Token 存储在 sessionStorage 中，不在 Store 内持久化。每次应用初始化时从 sessionStorage 读取 Token。

**页面状态恢复**：筛选条件、分页位置等页面状态通过路由 query 参数持久化，不存储在 Store 中。

**禁止事项**：禁止将敏感信息存入 Store 持久化（如密码、Token、用户隐私数据）。

## 6.7 本章禁止事项

- 禁止使用 Options Store 语法。
- 禁止组件直接修改 Store 的 state。
- 禁止 Store 之间形成循环依赖。
- 禁止将 API 调用逻辑写在组件中而不通过 Store。
- 禁止 Store 状态嵌套超过 3 层。
- 禁止使用 any 定义 Store 中的任何类型。

## 6.8 本章检查清单

- Store 是否使用 Setup Store 语法定义？
- Store 命名是否符合 use + PascalCase + Store 格式？
- 组件是否通过 actions 修改状态而非直接操作 state？
- 是否不存在 Store 间循环依赖？
- 所有 Store 状态和 actions 是否有完整类型？

---

# 第七章 Composable规范

## 7.1 章节概述

本章定义 Composable 的编写和使用规范。Composable 是 Vue Composition API 逻辑复用的核心机制，所有可复用的响应式逻辑必须封装为 Composable。

## 7.2 核心原则

- **单一职责**：每个 Composable 只负责一个明确的逻辑领域。
- **无副作用构造**：Composable 的初始化不应产生不可预期的副作用。
- **可组合**：Composable 之间可以相互调用，形成逻辑组合。
- **类型安全**：所有 Composable 的输入参数和返回值必须有完整的 TypeScript 类型。

## 7.3 Composable 定义规范

**命名规范**：Composable 函数名必须使用 use 前缀，后跟 PascalCase 描述名称，如 usePagination、useFormValidation。文件名使用 kebab-case.ts 格式。

**参数规范**：Composable 接收参数时，当参数超过 2 个时，必须使用 Options Object 模式（一个配置对象参数而非多个独立参数）。

**返回值规范**：Composable 返回值必须使用明确的 TypeScript 接口定义。返回对象而非数组，以便调用方按需解构。

**清理逻辑**：如果 Composable 内部注册了事件监听、定时器、或使用了 watch/watchEffect，必须返回清理函数或利用 Vue 的自动清理机制（onUnmounted）。

## 7.4 Composable 分类

**全局 Composable**（src/composables/）：跨业务域、跨页面使用的通用逻辑。如 usePagination（分页逻辑）、useTable（表格操作）、useUpload（文件上传）、usePermission（权限检查）。

**页面 Composable**（pages/xxx/composables/）：仅当前页面使用的业务逻辑。页面逻辑超过 100 行时必须拆分到此目录。

**组件 Composable**（components/xxx/composables/）：复杂组件的内部逻辑，当组件逻辑超过 50 行时抽取。

## 7.5 Composable 与 Hooks 的区别

- Composable：包含 Vue 响应式（ref、computed、watch），与 Vue 组件生命周期绑定。
- Hooks：封装特定业务域的操作，可能包含 Store 和 API 调用。
- Utils：完全不依赖 Vue 响应式系统的纯函数。

三者边界：Utils → Composable → Hooks，从底层到上层依次调用。Hooks 可以调用 Composable 和 Utils；Composable 可以调用 Utils，不能调用 Hooks；Utils 不调用任何其他层。

## 7.6 本章禁止事项

- 禁止 Composable 命名不以 use 开头。
- 禁止 Composable 参数超过 3 个时不使用 Options Object 模式。
- 禁止 Composable 返回值使用数组而非对象。
- 禁止在 Composable 中直接调用 API 层函数（应由 Store actions 或 Hooks 调用）。
- 禁止 Composable 内部遗留未清理的副作用（定时器、事件监听）。

## 7.7 本章检查清单

- Composable 命名是否以 use 开头？
- 是否使用 Options Object 模式传递多个参数？
- 返回值是否有明确的 TypeScript 类型？
- 是否存在未清理的副作用？
- 职责是否单一？


---

# 第八章 Axios规范

## 8.1 章节概述

本章定义 Axios 的配置、拦截器、请求封装和错误处理规范。所有 HTTP 请求必须通过统一的 Axios 实例和 API 层发送。

## 8.2 核心原则

- **统一实例**：全局使用单一 Axios 实例，统一配置 baseURL、超时时间、请求头和拦截器。
- **拦截器分层**：请求拦截器处理 Token 注入，响应拦截器处理数据解包、错误统一处理和 Token 刷新。
- **API 层封装**：每个业务域的接口封装在独立的 api 模块中，页面和组件不直接调用 axios 方法。
- **类型安全**：所有 API 函数的请求参数和响应数据必须有完整的 TypeScript 类型。

## 8.3 Axios 实例配置

在 src/api/request.ts 中创建并导出一个 Axios 实例。必须配置以下内容：

- baseURL：根据环境变量配置，开发环境指向本地代理，生产环境指向真实 API 地址。
- timeout：请求超时时间为 15000 毫秒（15 秒）。
- headers：默认 Content-Type 为 application/json。
- withCredentials：跨域请求不携带 Cookie（本项目使用 Token 认证）。

## 8.4 请求拦截器规范

请求拦截器按顺序执行以下操作：

1. 从 sessionStorage 获取 Token。
2. 如果 Token 存在，添加到请求头 Authorization: Bearer {token}。
3. 处理请求参数（如移除空值参数、格式化日期字段）。

## 8.5 响应拦截器规范

响应拦截器按顺序执行以下操作：

1. 检查 HTTP 状态码。非 2xx 状态码进入错误处理流程。
2. 解包响应数据，统一返回 response.data 而非完整 response 对象。
3. 检查业务状态码。业务错误（code !== 200）通过统一错误提示组件展示。

**Token 刷新机制**：当响应状态码为 401 且非登录接口时，自动尝试刷新 Token。刷新失败后清除 Token 并跳转登录页。

## 8.6 API 层封装规范

**模块划分**：src/api/modules/ 下按业务域拆分文件，每个文件封装一个业务域的接口。文件命名使用 kebab-case.api.ts 格式。

**函数命名**：API 函数命名遵循"动词 + 名词"格式，动词限定为：get（查询）、create（新增）、update（更新）、delete（删除）、upload（上传）、export（导出）。

**参数类型**：每个 API 函数的请求参数必须定义专门的 TypeScript 接口。

**返回值类型**：每个 API 函数的返回值必须声明类型，使用项目中定义的泛型响应类型包装。

**禁止绕过**：页面和组件禁止直接调用 axios 实例或 API 层的底层 request 函数。所有 HTTP 请求必须通过 api/modules/ 下定义的具名函数发起。

## 8.7 错误处理规范

**网络错误**：网络超时或连接失败时，显示"网络连接失败，请检查网络"提示。

**HTTP 错误**：根据状态码显示对应提示。400 显示"请求参数错误"，401 跳转登录，403 显示"无权限访问"，404 显示"请求资源不存在"，500 显示"服务器内部错误"。

**业务错误**：接口返回的业务错误码和错误消息通过统一提示组件展示。

**全局错误捕获**：通过响应拦截器统一捕获所有错误，禁止在每个 API 调用处重复编写错误处理代码。特殊情况（如需要自定义错误处理）通过配置参数跳过全局处理。

## 8.8 本章禁止事项

- 禁止创建多个 Axios 实例。
- 禁止页面或组件直接调用 axios 方法。
- 禁止 API 函数不使用 TypeScript 类型。
- 禁止在每个请求处重复编写错误处理逻辑。
- 禁止将 Token 存储在 localStorage。

## 8.9 本章检查清单

- 是否使用统一的 Axios 实例？
- 请求拦截器是否正确注入 Token？
- 响应拦截器是否统一处理错误？
- API 函数是否有完整的类型定义？
- 是否没有页面/组件直接调用 axios？


---

# 第九章 Mock规范

## 9.1 章节概述

本章定义 Mock.js 的使用规范。Mock 用于前端独立开发阶段模拟后端接口，是 Mock First 开发策略的技术基础。

## 9.2 核心原则

- **结构一致**：Mock 生成的数据结构必须与真实 API 文档承诺的响应结构完全一致。
- **可开关**：Mock 必须能够通过环境变量一键开启或关闭，不影响生产构建。
- **场景覆盖**：Mock 必须覆盖正常数据、空数据、分页、错误响应等边界场景。
- **真实性**：Mock 数据应尽量模拟真实业务数据的格式、长度和分布。

## 9.3 Mock 文件组织

Mock 文件统一放置在 src/mock/ 目录下，按业务域拆分文件，命名使用 kebab-case.mock.ts 格式。src/mock/index.ts 作为统一入口，负责汇总所有 Mock 模块并根据环境变量决定是否启用。

## 9.4 Mock 数据规范

**数据结构**：Mock 生成的响应数据必须遵守 API 规范文档中定义的统一响应格式。统一的响应格式包含 code（业务状态码）、message（响应消息）、data（业务数据）三个字段。

**数据生成规则**：使用 Mock.js 的占位符规则生成符合业务语义的数据。例如：姓名使用 @cname，日期使用 @date，ID 使用 @guid。

**分页数据**：Mock 接口必须支持分页参数，根据传入的 page 和 pageSize 参数生成对应页码和数量的数据。

**延迟模拟**：Mock 接口统一设置 200-500 毫秒的随机延迟，模拟真实网络请求的响应时间，避免开发时对即时响应的错误预期。

## 9.5 Mock 开关机制

通过 Vite 环境变量控制 Mock 的开启和关闭。开发环境下默认开启 Mock。当后端接口就绪后，关闭 Mock 开关即可切换到真实 API。

**禁止事项**：禁止在生产环境打包中包含 Mock 代码。通过 Vite 的条件编译确保 Mock 模块在生产构建时被完全移除。

## 9.6 本章禁止事项

- 禁止 Mock 数据结构与 API 规范不一致。
- 禁止在生产环境中保留 Mock 代码。
- 禁止 Mock 接口不支持分页参数。
- 禁止 Mock 接口不设置响应延迟。
- 禁止 Mock 接口不支持错误场景模拟。

## 9.7 本章检查清单

- Mock 数据格式是否与 API 规范一致？
- 是否支持分页参数？
- 是否设置了合理的网络延迟？
- 是否可以通过环境变量关闭 Mock？
- 生产构建是否移除了 Mock 代码？

---

# 第十章 TypeScript规范

## 10.1 章节概述

本章定义 TypeScript 的类型系统使用规范。TypeScript 是本项目的强制要求，所有 .ts、.vue 文件必须通过严格模式的类型检查。

## 10.2 核心原则

- **严格模式**：tsconfig.json 中开启 strict: true，所有严格的类型检查选项全部启用。
- **零 any**：项目中不允许出现 any 类型。无法确定类型时使用 unknown 并通过类型守卫收窄。
- **接口优先**：优先使用 interface 定义对象类型，使用 type 定义联合类型、交叉类型和工具类型。
- **显式优于隐式**：函数返回值类型必须显式声明，不依赖类型推导。

## 10.3 类型定义规范

**DTO 和 VO**：接口请求参数定义为 DTO（Data Transfer Object），接口响应数据定义为 VO（View Object），统一放置在 src/types/ 目录下。

**接口命名**：接口使用 I 前缀 + PascalCase 命名，如 IUserInfo、ITrainingTask。类型别名使用 PascalCase 命名，如 ApiResponse。

**组件类型**：组件 Props、Emits、Slots 的类型定义放在组件目录下的 types.ts 文件中。

**全局类型**：通用的、跨模块使用的类型定义在 src/types/ 目录下，按业务域拆分文件。

**环境声明**：第三方库的类型补充声明放在 src/types/global.d.ts 中。

## 10.4 类型使用规范

**类型导入**：使用 import type 语法导入仅用于类型检查的声明，确保类型导入在编译后被完全移除。

**类型断言**：使用 as 语法进行类型断言，禁止使用尖括号语法。禁止滥用类型断言绕过类型检查。

**类型守卫**：对于 unknown 类型，必须通过类型守卫（typeof、instanceof、in 操作符、自定义类型谓词）收窄类型后才能使用。

**泛型使用**：泛型命名使用有意义的名称，单个泛型参数使用 T，多个泛型参数使用描述性名称（如 TData、TParams）。

**工具类型**：善用 TypeScript 内置工具类型（Partial、Required、Pick、Omit、Record、Readonly），减少重复类型定义。

## 10.5 枚举使用规范

枚举使用 const enum 或字符串联合类型定义，优先使用字符串联合类型。枚举值命名使用 UPPER_SNAKE_CASE。枚举定义统一放置在 src/constants/enums.ts 中。

## 10.6 本章禁止事项

- 禁止使用 any 类型。
- 禁止使用 var 声明变量。
- 禁止使用 @ts-ignore 或 @ts-nocheck 绕过类型检查。
- 禁止函数返回值依赖类型推导而不显式声明。
- 禁止使用 Namespace。
- 禁止类型定义文件与业务代码混合放置。

## 10.7 本章检查清单

- tsconfig.json 是否开启 strict: true？
- 是否不存在 any 类型？
- 所有函数返回值是否显式声明了类型？
- 类型导入是否使用了 import type？
- 是否使用了 unknown 代替 any？


---

# 第十一章 状态管理规范

## 11.1 章节概述

本章定义前端应用状态的分类和管理策略。明确哪些状态属于全局 Store、哪些属于 Composable、哪些属于组件内部状态，避免状态管理的混乱。

## 11.2 核心原则

- **分层管理**：状态按作用域分为全局状态、页面状态、组件状态三层，每层有明确的管理方式。
- **最小共享**：只有真正需要跨组件共享的状态才提升到全局 Store，优先保持状态局部化。
- **单向数据流**：状态修改通过 actions 完成，确保数据流向可追踪。

## 11.3 状态分层

**全局状态（Pinia Store）**：跨页面或跨组件共享的状态。包括：用户信息、认证 Token、权限列表、全局配置、通知未读数。

**页面状态（Composable）**：仅在当前页面内共享的状态。包括：表单数据、筛选条件、分页信息、页面级 Loading 状态。

**组件状态（ref/reactive）**：仅在当前组件内使用的状态。包括：UI 交互状态（展开/折叠、选中项）、临时输入值、组件级 Loading 状态。

## 11.4 状态提升判断标准

在决定将状态放在哪里时，按以下顺序判断：

1. 状态是否只在一个组件中使用？→ 组件内部 ref。
2. 状态是否在多个同页面组件中使用？→ 页面 Composable。
3. 状态是否跨页面使用或需要持久化？→ Pinia Store。
4. 状态是否可以通过路由参数传递？→ 优先使用路由参数。

## 11.5 Loading 状态管理

**请求级 Loading**：每个 API 请求对应的 Loading 状态独立管理。Store 中为每个 action 维护对应的 isLoading 状态。组件通过 Store 的 isLoading 状态控制骨架屏或加载动画的显示。

**全局 Loading**：页面切换时的顶部进度条通过路由守卫自动控制，不需要手动管理。

**防重复提交**：表单提交时，在提交 action 执行期间禁用提交按钮，通过 isLoading 状态控制。禁止通过额外变量手动标记防重复。

## 11.6 Error 状态管理

**请求级 Error**：每个 API 请求的 Error 状态独立管理。Store 中为每个 action 维护对应的 error 状态。Error 状态包含错误码和错误信息。

**全局 Error**：未捕获的 Promise 错误通过全局错误处理器捕获并显示统一提示。网络断开等全局性异常通过全局状态管理。

**错误恢复**：每个 Error 状态对应一个重试操作。错误展示组件中必须包含重试按钮，调用对应的 action 重新请求数据。

## 11.7 本章禁止事项

- 禁止将页面级临时状态放入 Pinia Store。
- 禁止在组件间通过 Props 多层传递共享状态（Prop Drilling），超过两层传递应考虑提升到 Store 或 Composable。
- 禁止手动管理防重复提交标记（应使用 Loading 状态）。
- 禁止忽略 API 请求的 Error 状态。

## 11.8 本章检查清单

- 全局状态是否只包含真正跨页面共享的数据？
- 页面状态是否通过 Composable 管理？
- 是否存在 Prop Drilling 超过两层的情况？
- Loading 状态是否与 API 请求一一对应？
- Error 状态是否包含重试机制？

---

# 第十二章 组件通信规范

## 12.1 章节概述

本章定义 Vue 组件间的通信方式和选择标准。组件通信是组件化架构的关键环节，选择正确的通信方式直接影响代码的可维护性。

## 12.2 核心原则

- **明确方向**：数据向下（Props），事件向上（Emits），跨层级通信通过 Store 或依赖注入。
- **最少知识**：组件只与直接父组件和直接子组件通信，不跨越多个层级直接通信。
- **类型安全**：所有通信接口（Props、Emits、Provide/Inject）必须有 TypeScript 类型。

## 12.3 通信方式选择

按通信场景选择对应的方式：

| 场景 | 方式 | 使用条件 |
|------|------|----------|
| 父 → 子 | Props | 父子组件层级不超过 2 层 |
| 子 → 父 | Emits | 父子组件层级不超过 2 层 |
| 跨层级（祖先 → 后代） | Provide/Inject | 明确的"生产者-消费者"关系 |
| 跨组件共享状态 | Pinia Store | 多个不相关组件需要访问同一状态 |
| 跨组件事件通知 | EventBus（通过 Composable） | 非父子关系的组件间松耦合通信 |
| 路由传参 | Route Params / Query | 页面间数据传递 |

## 12.4 Props 向下传递

**单向数据流**：数据通过 Props 从父组件流向子组件。子组件不得修改 Props 的值。

**Props 数量限制**：单个组件的 Props 数量不超过 8 个。超过 8 个时，将相关 Props 合并为一个配置对象 Props。

**禁止 Prop Drilling**：当 Props 需要跨越 2 层以上传递时，改用 Provide/Inject 或 Store。在中间组件中不应出现"仅用于透传"的 Props。

## 12.5 Emits 向上通知

**事件命名**：Emits 事件名使用 kebab-case，描述具体行为而非状态变化。使用 item-delete 而非 delete-click，使用 form-submit 而非 submit。

**v-model 双向绑定**：对于表单类组件，使用 v-model 实现双向绑定，同时声明 modelValue prop 和 update:modelValue emit。

## 12.6 Provide/Inject 规范

**使用场景**：仅在"一组紧密协作的组件"中使用 Provide/Inject，如表单组件树、列表组件树。

**类型安全**：使用 InjectionKey 为 provide 的值提供类型标注。inject 必须提供默认值或声明为可空。

**禁止滥用**：Provide/Inject 不应作为 Store 的替代品。跨页面共享的状态必须使用 Pinia Store。

## 12.7 本章禁止事项

- 禁止子组件直接修改 Props。
- 禁止 Props 透传超过 2 层（Prop Drilling）。
- 禁止使用已废弃的事件总线全局单例。
- 禁止 inject 不提供默认值且不声明可空。
- 禁止组件间通过 ref 直接调用对方的方法。

## 12.8 本章检查清单

- 通信方式是否与场景匹配？
- 是否存在 Props 透传超过 2 层？
- Emits 命名是否使用 kebab-case？
- Provide/Inject 是否使用 InjectionKey 提供类型？


---

# 第十三章 权限控制规范

## 13.1 章节概述

本章定义基于 RBAC 的前端权限控制方案。权限控制覆盖路由访问、页面元素显示和功能操作三个层面。

## 13.2 核心原则

- **前端权限为 UX 优化**：前端权限控制是用户体验层面的优化（隐藏不可用功能），真正的安全控制在后端实现。
- **三层控制**：权限在路由层、页面层和组件层三层分别实施控制。
- **声明式优先**：权限判断通过声明式组件或指令实现，避免在代码中散布 if-else 权限判断。
- **角色明确**：系统包含三种角色：学生、教师（含科研负责人职能）、管理员，每种角色的权限边界清晰。

## 13.3 路由层权限控制

路由 meta 信息中声明允许访问的角色列表。路由守卫在导航前检查用户角色是否在允许列表中。不在列表中的用户重定向到 403 页面。

## 13.4 页面层权限控制

页面中的功能操作按钮通过 Permission Components 控制显示和隐藏。Permission Components 接收所需权限标识作为 Props，根据用户权限决定是否渲染插槽内容。

## 13.5 组件层权限控制

对于需要根据权限显示不同内容的区域，使用 Permission Components 的条件渲染能力。禁止在模板中使用 v-if 手动判断用户角色。

## 13.6 角色定义

| 角色 | 标识 | 核心权限 |
|------|------|----------|
| 学生 | student | 查看个人仪表盘、提交实训成果、查看个人成绩和 AI 评价 |
| 教师 | teacher | 班级管理、实训任务管理、AI 评价结果复核、成绩发布 |
| 教师（含教研） | teacher | 全院数据查看、评价标准管理、Prompt 模板管理、教学数据分析、班级管理、实训任务管理、AI 评价复核 |
| 管理员 | admin | 用户管理、角色分配、系统配置、日志查看 |

## 13.7 Permission Store

权限数据存储在 Permission Store 中。包含当前用户角色列表、当前用户的权限标识列表、权限检查方法（hasPermission、hasRole）。权限数据在用户登录后从后端获取并缓存到 Store 中。

## 13.8 本章禁止事项

- 禁止在模板中直接使用 v-if 判断用户角色控制 UI 显示。
- 禁止仅依赖前端权限控制（后端必须独立校验）。
- 禁止将权限判断逻辑硬编码在多个页面中。

## 13.9 本章检查清单

- 路由 meta 是否声明了允许访问的角色？
- 页面按钮权限是否通过 Permission Components 控制？
- 权限数据是否存储在 Permission Store 中？
- 是否存在 v-if 硬编码角色判断？

---

# 第十四章 表单开发规范

## 14.1 章节概述

本章定义表单类页面的开发规范。表单是本项目中高频出现的交互模式，需要统一交互标准以保障用户体验一致性。

## 14.2 核心原则

- **组件驱动**：表单使用 Element Plus 的 Form 组件配合项目 Form Components 构建。
- **即时校验**：表单校验在输入时实时触发，不等到提交时才校验。
- **状态覆盖**：表单覆盖编辑、新增、查看（只读）三种模式。
- **防重复提交**：提交按钮在提交过程中禁用，防止重复提交。

## 14.3 表单布局规范

**标签位置**：表单标签采用顶置布局（Top-aligned Label），标签在输入框上方，提升扫描效率。

**列数规范**：简单表单（字段 ≤ 4 个）使用单列布局。中等表单（字段 5-8 个）使用双列布局。复杂表单（字段 > 8 个）使用双列布局并配合分组卡片。

**操作按钮**：表单操作按钮（提交、取消、重置）固定在表单底部或页面底部的操作栏中。

**间距规范**：表单项之间的间距使用 --space-lg（16px）。分组之间的间距使用 --space-xl（20px）。

## 14.4 表单校验规范

**校验规则**：使用 Element Plus Form 的 rules 配置校验规则。校验规则在 Composable 中定义，不在模板中硬编码。

**校验时机**：校验触发时机使用 blur + change（失焦时校验 + 值变化时校验）。

**自定义校验**：对于 Element Plus 内置校验规则无法满足的业务校验，使用自定义 validator 函数实现。

**错误提示**：校验错误信息显示在对应输入框下方。错误信息文案必须清晰、可操作，说明错误原因和修复方式。

## 14.5 表单模式切换

**新增模式**：所有字段为空，可编辑。提交按钮文案为"创建"或"提交"。

**编辑模式**：字段预填充现有数据，可编辑。提交按钮文案为"保存"或"更新"。

**查看模式（只读）**：所有字段不可编辑，显示为纯文本样式。隐藏提交按钮，显示"编辑"按钮用于切换到编辑模式。

## 14.6 表单提交流程

1. 用户点击提交按钮。
2. 触发表单校验。校验失败则显示错误提示并停止提交。
3. 校验通过后，提交按钮进入 Loading 状态并禁用。
4. 调用 Store action 提交数据。
5. 提交成功后显示成功提示，执行后续操作（跳转列表页、刷新数据、清空表单）。
6. 提交失败后显示错误提示，恢复提交按钮可用状态。

## 14.7 本章禁止事项

- 禁止表单校验等到提交时才触发。
- 禁止不处理提交 Loading 状态。
- 禁止提交按钮在提交过程中可重复点击。
- 禁止校验错误信息模糊不清。
- 禁止表单布局不遵循 Design System 间距规范。

## 14.8 本章检查清单

- 表单是否使用 Element Plus Form 组件构建？
- 校验规则是否在 Composable 中定义？
- 是否覆盖新增/编辑/查看三种模式？
- 提交按钮是否有 Loading 状态？


---

# 第十五章 列表开发规范

## 15.1 章节概述

本章定义表格列表类页面的开发规范。表格是本项目中最常见的数据展示形式，需要统一交互标准。

## 15.2 核心原则

- **分页强制**：所有列表页面必须支持分页，禁止一次性加载全部数据。
- **搜索优先**：列表页必须提供搜索和筛选功能。
- **操作一致**：行操作的触发方式和位置保持一致。
- **状态覆盖**：列表覆盖加载中、空数据、错误、正常渲染四种状态。

## 15.3 表格使用规范

**表格组件**：使用 Element Plus 的 Table 组件或项目封装的基础表格组件（BaseTable）。当数据量超过 500 行时，使用虚拟滚动表格替代普通表格。

**列定义**：表格列定义在 Composable 中配置，不在模板中硬编码。列配置包含：列标题、数据字段、列宽、对齐方式、是否可排序、是否可筛选、自定义渲染函数。

**操作列**：操作列固定在表格右侧（fixed="right"）。操作项超过 3 个时，前 2 个直接展示，其余收起到"更多"下拉菜单中。

**行交互**：点击行默认进入详情页。如果行有复选框选择功能，点击行触发选中切换。

**表头固定**：表格高度超出视口时，表头固定（sticky header）。

## 15.4 搜索和筛选规范

**搜索栏**：列表页顶部使用 BaseSearchBar 组件提供搜索功能。搜索栏包含：关键词搜索输入框、筛选条件、搜索按钮和重置按钮。

**筛选条件**：筛选条件变化时自动触发列表刷新，重置分页到第一页。

**搜索持久化**：搜索条件和筛选条件通过路由 query 参数持久化。

## 15.5 分页规范

**分页组件**：使用 BasePagination 组件，位于表格下方。默认每页显示 20 条数据，提供 10/20/50/100 四种每页条数选项。搜索条件或筛选条件变化时，分页重置到第一页。

## 15.6 批量操作规范

列表第一列为复选框列，支持单选、全选和跨页选择。选中数据后，在表格上方显示批量操作栏。批量删除等不可逆操作必须弹出二次确认对话框。

## 15.7 本章禁止事项

- 禁止表格绑定全部数据而不分页。
- 禁止搜索栏不提供重置按钮。
- 禁止操作列不固定在右侧。
- 禁止批量删除不弹出二次确认。
- 禁止表格列定义硬编码在模板中。

## 15.8 本章检查清单

- 列表是否支持分页？
- 是否提供搜索和筛选功能？
- 搜索条件是否通过路由持久化？
- 是否覆盖 Loading/Empty/Error 状态？
- 操作列是否固定在右侧？

---

# 第十六章 图表开发规范

## 16.1 章节概述

本章定义 ECharts 图表的使用规范。图表主要应用于 Dashboard 数据概览和报表中心的数据可视化场景。

## 16.2 核心原则

- **组件封装**：每个图表配置封装为独立的 Chart Component，不直接在页面中编写 ECharts 配置。
- **响应式**：图表大小随容器自适应，监听窗口 resize 事件自动重绘。
- **数据驱动**：图表数据通过 Props 传入，图表组件不直接获取数据。
- **交互一致**：所有图表的 Tooltip 样式、动画效果、颜色方案保持一致。

## 16.3 图表组件规范

使用 Chart Components 目录下的封装组件。每个图表组件接收数据和配置 Props，内部管理 ECharts 实例的创建、更新和销毁。图表实例在组件挂载时创建，在组件卸载时销毁。监听数据变化自动更新图表。

## 16.4 图表样式规范

**颜色方案**：图表使用 Design System 定义的颜色 Token。主色调对应 --color-primary（#3B82F6），辅助色依次使用 Design System 定义的颜色值。

**Tooltip 样式**：统一使用浅色背景、圆角边框的 Tooltip。

**动画效果**：图表初始加载时使用默认的渐入动画，动画时长不超过 1000 毫秒。

## 16.5 本章禁止事项

- 禁止在页面中直接编写 ECharts 配置代码。
- 禁止图表不处理容器尺寸变化。
- 禁止图表实例不销毁导致内存泄漏。
- 禁止图表颜色不使用 Design Tokens。

## 16.6 本章检查清单

- 图表是否使用 Chart Components 封装？
- 图表是否响应容器尺寸变化？
- 图表实例是否在组件卸载时正确销毁？
- 图表颜色是否引用 Design Tokens？


---

# 第十七章 AI模块开发规范

## 17.1 章节概述

本章定义 AI 模块的前端开发规范。AI 模块是本项目的核心差异化能力，涉及 AI 评分展示、置信度标识、流式输出、教师覆写、Prompt 管理等特殊交互模式。

## 17.2 核心原则

- **可解释性**：AI 的每个输出结果必须附带推理过程和数据来源，确保教师能够理解和验证。
- **可覆写性**：AI 的结果是建议而非决定，教师必须有能力修改和覆写 AI 的评分和反馈。
- **可追溯性**：所有 AI 操作记录完整操作日志，包含输入参数、输出结果、Token 用量和耗时。
- **AI 标识强制**：所有 AI 相关 UI 区域必须使用统一的 AI 视觉标识（Sparkles 图标 + "AI" 标签），使用户一眼区分 AI 建议和人工操作。
- **人工确认**：AI 的分析结果必须经过教师确认后才能作为最终成绩发布，禁止 AI 结果自动生效。

## 17.3 AI 组件使用规范

AI 模块必须使用以下 Component Library 中的 AI 专用组件，不得自行实现：

| 组件 | 用途 |
|------|------|
| AIScoreCard | 展示 AI 评分结果，包含分数、置信度和各维度评分 |
| AIConfidenceTag | 三级置信度标识（高/中/低），配合对应颜色 |
| AIAnalysisCard | AI 分析结果卡片，包含推理过程和改进建议 |
| AIReasonCard | 展示 AI 推理的结构化理由 |
| AIDeductionCard | 扣分项明细，按影响程度从高到低排序 |
| AIStreamingOutput | 流式输出容器，支持打字机效果和 Markdown 实时渲染 |
| AIThinkingProcess | AI 思考过程折叠面板，默认折叠，教师可展开 |
| AISuggestionCard | AI 改进建议卡片，支持采纳/修改/不采纳三种操作 |
| TeacherOverridePanel | 教师覆写面板，支持维度级覆写和原因填写 |
| ReviewComparison | AI 评分与教师评分的并排对比 |
| PromptPreview | Prompt 模板 + 实际数据拼接预览 |
| PromptVersionCard | Prompt 版本管理和 Diff 对比 |
| JSONViewer | AI 响应的 JSON 结构化查看器 |
| AIStatusBadge | AI 分析状态标识（分析中/已完成/失败） |
| LLMResponseViewer | 大模型原始响应查看器（调试和审计用） |
| AIHistoryTimeline | AI 分析历史时间线 |
| KnowledgeRecommendation | 知识库推荐卡片 |
| GrowthArchiveCard | 学生成长档案卡片 |

## 17.4 AI 状态管理

**AI 分析状态**：AI 模块必须管理以下状态：

- IDLE：初始状态，尚未触发 AI 分析。
- ANALYZING：AI 分析进行中，展示 AIStatusBadge 组件和脉冲动画。
- COMPLETED：分析完成，展示分析结果。
- FAILED：分析失败，展示错误信息和重试按钮。

**流式输出状态**：使用 AIStreamingOutput 组件管理流式内容渲染。支持手动停止流式输出。

## 17.5 教师复核流程

教师对 AI 评价结果的复核必须遵循以下流程：

1. 查看 AI 分析结果：AIScoreCard 展示评分和置信度。
2. 查看推理过程：展开 AIReasonCard 查看 AI 的推理逻辑。
3. 逐维度审核：通过 TeacherOverridePanel 对每个维度进行确认或覆写。
4. 覆写必填原因：教师修改 AI 评分时必须填写覆写原因。
5. 查看对比：通过 ReviewComparison 并排对比 AI 原始评分和教师调整后的评分。
6. 最终确认：教师确认最终成绩后发布。

## 17.6 Prompt 管理规范

**Prompt 模板**：教师可以通过 PromptVersionCard 管理 Prompt 模板的版本。

**版本控制**：Prompt 模板支持版本管理，保留历史版本，支持版本回滚和 Diff 对比。

**预览功能**：通过 PromptPreview 预览 Prompt 模板和实际数据拼接后的完整 Prompt。

## 17.7 AI 日志规范

每次 AI 调用必须记录以下信息用于调试和审计：

- 请求时间戳。
- 使用的模型名称。
- 请求的 Token 数量。
- 响应的 Token 数量。
- 响应耗时（毫秒）。
- 请求参数（已脱敏）。
- 响应内容（原始 JSON）。
- 是否成功。

通过 LLMResponseViewer 组件可查看完整的原始请求和响应数据。

## 17.8 本章禁止事项

- 禁止 AI 分析结果不经人工确认直接发布为最终成绩。
- 禁止 AI 相关区域不使用统一的 AI 视觉标识。
- 禁止自行实现 AI 组件而非使用 Component Library 中的 AI Components。
- 禁止教师覆写时不填写覆写原因。
- 禁止 AI 输出结果不附带推理过程。
- 禁止在生产环境中输出完整 Token 到 Console。

## 17.9 本章检查清单

- 所有 AI 区域是否使用统一的 AI 视觉标识？
- AI 分析结果是否附带推理过程？
- 是否支持教师覆写并填写原因？
- 是否记录 AI 调用日志（模型、Token、耗时）？
- 是否使用 Component Library 中的 AI Components？
- 流式输出是否支持手动停止？


---

# 第十八章 性能优化规范

## 18.1 章节概述

本章定义前端性能优化的标准和实践。目标为确保页面加载时间不超过 2 秒、交互响应不超过 100 毫秒。

## 18.2 核心原则

- **懒加载默认**：路由组件、重型第三方库默认使用懒加载。
- **缓存策略**：合理使用 KeepAlive、数据缓存和浏览器缓存。
- **体积控制**：控制打包体积，单个页面初始加载的 JS 不超过 500KB（压缩后）。
- **渲染优化**：大列表使用虚拟滚动，避免一次性渲染大量 DOM 节点。

## 18.3 加载性能优化

**路由懒加载**：所有页面级组件使用动态 import 实现懒加载。禁止在路由配置中使用同步 import。

**组件异步加载**：对于非首屏必需的组件，使用 defineAsyncComponent 异步加载。

**第三方库按需引入**：Element Plus、ECharts、Lucide Icons 等第三方库必须按需引入，禁止全量导入。

**图片优化**：静态图片使用 WebP 格式，大图使用懒加载。

## 18.4 运行时性能优化

**虚拟滚动**：列表数据超过 500 条时，使用虚拟滚动技术只渲染可视区域内的 DOM 节点。

**计算属性缓存**：模板中多次使用的派生数据使用 computed 缓存计算结果。

**防抖和节流**：搜索输入使用防抖，窗口 resize 和滚动事件使用节流。

**KeepAlive 缓存**：列表页等高频访问页面配置 KeepAlive 缓存，避免重复渲染。

## 18.5 打包优化

**代码分割**：通过 Vite 的代码分割功能，将第三方库、公共组件和业务代码分离打包。

**Tree Shaking**：确保未使用的代码在构建时被移除。避免使用副作用导入。

**Gzip 压缩**：生产构建启用 Gzip 压缩。

## 18.6 本章禁止事项

- 禁止路由组件不使用懒加载。
- 禁止全量导入第三方库。
- 禁止 500 条以上列表不使用虚拟滚动。
- 禁止高频事件不进行防抖或节流。
- 禁止生产环境保留 Source Map。

## 18.7 本章检查清单

- 所有路由组件是否使用懒加载？
- 第三方库是否按需引入？
- 大列表是否使用虚拟滚动？
- 高频事件是否使用防抖/节流？
- 首屏 JS 体积是否小于 500KB？

---

# 第十九章 安全开发规范

## 19.1 章节概述

本章定义前端安全开发的规范和防御策略。安全是企业的生命线，前端安全防护必须从开发阶段开始。

## 19.2 核心原则

- **最小权限**：用户只能访问其角色允许的功能和数据。
- **输入验证**：所有用户输入必须在前端和后端分别验证。
- **输出编码**：所有用户生成的内容必须在展示前进行安全处理。
- **Token 安全**：Token 存储在 sessionStorage，设置合理的过期时间。

## 19.3 XSS 防护

**禁止 v-html**：禁止使用 v-html 渲染用户输入的内容。对于必须渲染 HTML 的场景（如 AI Markdown 输出），使用 DOMPurify 进行内容净化后再渲染。

**输入过滤**：所有用户输入在提交前进行格式校验和特殊字符过滤。

**URL 安全**：外部链接使用 rel="noopener noreferrer"，禁止使用 javascript: 伪协议。

## 19.4 Token 安全

**存储位置**：Token 存储在 sessionStorage，禁止存储在 localStorage（localStorage 在浏览器关闭后仍保留，增加 Token 泄漏风险）。

**自动刷新**：Token 过期前自动刷新。刷新失败后清除 Token 并跳转登录页。

**传输安全**：Token 通过 HTTP Header 的 Authorization 字段传输，不在 URL 参数中传递。

## 19.5 敏感信息保护

**Console 清理**：生产环境禁止在 Console 中输出敏感信息（Token、用户密码、个人信息、AI 完整 Prompt）。

**数据脱敏**：用户敏感信息在展示时进行脱敏处理（如手机号中间四位显示为星号）。

**文件上传校验**：文件上传在前端校验文件类型和大小，后端再次校验。

## 19.6 本章禁止事项

- 禁止使用 v-html 渲染用户输入内容。
- 禁止将 Token 存储在 localStorage。
- 禁止在 URL 参数中传递 Token。
- 禁止生产环境 Console 输出敏感信息。
- 禁止文件上传不校验类型和大小。

## 19.7 本章检查清单

- 是否存在 v-html 渲染用户输入？
- Token 是否存储在 sessionStorage？
- 生产环境是否移除了 Console 日志？
- 文件上传是否有类型和大小校验？
- 外部链接是否使用 rel="noopener noreferrer"？


---

# 第二十章 国际化规范（预留）

## 20.1 章节概述

本章为国际化功能的预留规范。当前版本（v1.0）不实现国际化功能，所有界面使用简体中文。本章规范的目的是为未来版本的国际化改造提供技术标准。

## 20.2 核心原则

- **预留接口**：项目的文案组织和组件设计预留 i18n 接口，避免未来改造时的大规模重构。
- **硬编码禁止**：即使当前不使用国际化，组件中的用户可见文案也不能硬编码在模板中。应将文案统一管理，为未来迁移做准备。

## 20.3 文案管理规范

**集中管理**：所有用户可见的文案（按钮文字、提示信息、错误消息、空状态描述等）定义在统一的常量文件中。

**组件文案**：组件通过 Props 接收文案或使用统一常量，不在模板中直接写死中文字符串。

**文案命名**：文案常量使用 UPPER_SNAKE_CASE 命名，按功能模块分组。

## 20.4 未来国际化方案

当项目需要支持多语言时，将采用 Vue I18n 作为国际化方案。届时需完成以下工作：

- 安装和配置 Vue I18n。
- 将集中管理的文案迁移到语言包文件中。
- 为日期、数字等格式提供本地化处理。
- 支持语言切换并持久化用户语言偏好。

## 20.5 本章禁止事项

- 禁止在组件模板中硬编码用户可见中文文案。
- 禁止使用字符串拼接构造用户可见文案。

## 20.6 本章检查清单

- 用户可见文案是否集中管理在常量文件中？
- 组件是否通过 Props 或常量获取文案？

---

# 第二十一章 日志规范

## 21.1 章节概述

本章定义前端日志的管理标准。日志用于问题追踪、性能监控和用户行为分析。

## 21.2 核心原则

- **分级管理**：日志按严重程度分为 DEBUG、INFO、WARN、ERROR 四个级别。
- **生产清理**：生产环境移除 DEBUG 和 INFO 级别日志，仅保留 WARN 和 ERROR。
- **敏感信息脱敏**：日志中不得包含密码、Token、用户隐私数据。
- **可追溯**：ERROR 日志必须包含足够的上下文信息用于问题定位。

## 21.3 日志级别定义

| 级别 | 使用场景 | 生产环境 |
|------|----------|----------|
| DEBUG | 开发调试信息，如接口请求参数、状态变更追踪 | 移除 |
| INFO | 关键操作记录，如页面跳转、用户操作、AI 调用统计 | 移除 |
| WARN | 非阻断性异常，如接口重试、降级处理 | 保留 |
| ERROR | 阻断性错误，如接口失败、组件渲染异常 | 保留 |

## 21.4 Console 使用规范

**开发环境**：使用统一封装的 Logger 工具类进行日志输出，不直接使用 console.log。

**生产环境**：通过 Vite 的条件编译移除所有 DEBUG 和 INFO 级别日志。禁止生产环境保留 console.log。

**日志格式**：统一使用 "[模块名]" + 日志内容的格式，便于按模块过滤日志。

## 21.5 AI 调用日志

AI 调用必须记录：请求的模型名称、请求和响应的 Token 数量、响应耗时（毫秒）、是否成功。仅在开发环境输出。

## 21.6 本章禁止事项

- 禁止生产环境保留 DEBUG 和 INFO 级别日志。
- 禁止日志中包含 Token、密码、用户个人信息。
- 禁止无级别的日志输出。
- 禁止直接使用 console.log 而非 Logger 工具类。

## 21.7 本章检查清单

- 是否使用 Logger 工具类而非直接 console.log？
- 生产环境是否移除了 DEBUG 和 INFO 日志？
- 日志中是否不包含敏感信息？
- ERROR 日志是否包含足够的上下文信息？


---

# 第二十二章 代码规范

## 22.1 章节概述

本章定义代码层面的统一规范，涵盖命名、格式、注释和代码组织。

## 22.2 核心原则

- **一致性**：所有代码风格统一，新代码看起来像是同一个人编写的。
- **可读性优先**：代码首先是给人阅读的，其次才是机器执行。
- **自动化检查**：通过 ESLint 和 Prettier 自动检查和格式化代码。

## 22.3 命名规范

### 完整命名对照表

| 实体 | 命名方式 | 示例 |
|------|----------|------|
| 组件文件 | PascalCase.vue | UserTable.vue |
| 页面目录 | kebab-case | teaching-check/ |
| TypeScript 文件 | kebab-case.ts | use-request.ts |
| Store 文件 | kebab-case.store.ts | user.store.ts |
| Mock 文件 | kebab-case.mock.ts | check.mock.ts |
| API 文件 | kebab-case.api.ts | user.api.ts |
| Interface | I + PascalCase | IUserInfo |
| Type 别名 | PascalCase | ApiResponse |
| 常量 | UPPER_SNAKE_CASE | MAX_RETRY_COUNT |
| 变量 | camelCase | isLoading |
| 函数/方法 | camelCase | handleSubmit |
| Composable | use + PascalCase | usePagination |
| Store 实例 | use + PascalCase + Store | useUserStore |
| Props | camelCase | userList |
| Emits | kebab-case | item-delete |
| 枚举值 | UPPER_SNAKE_CASE | ROLE_ADMIN |
| CSS Class | kebab-case | user-card-header |

## 22.4 Import 排序规范

Import 语句按以下顺序分组，组间空一行：

1. Vue 核心库（vue、vue-router、pinia）。
2. 第三方 UI 库（element-plus、echarts）。
3. 第三方工具库（axios、mockjs、lucide-icons）。
4. 项目内部 Store（@/stores/）。
5. 项目内部 Composable（@/composables/）。
6. 项目内部组件（@/components/）。
7. 项目内部 API（@/api/）。
8. 项目内部工具（@/utils/）。
9. 类型导入（放在最后，使用 import type）。
10. 相对路径导入（./、../）。

## 22.5 变量声明规范

- 优先使用 const，其次是 let。禁止使用 var。
- 变量声明的顺序：Props → Emits → Store → Composable → 响应式状态 → 计算属性 → 方法。

## 22.6 函数规范

- 每个函数只做一件事（单一职责原则）。
- 函数参数不超过 3 个。超过 3 个时使用 Options Object 模式。
- 优先使用纯函数。
- 函数内部嵌套不超过 3 层。

## 22.7 注释规范

**JSDoc**：公共 API 函数、Composable、Store actions 必须使用 JSDoc 格式注释。

**行内注释**：仅在代码逻辑复杂、意图不明显时使用。不注释代码"做了什么"，注释"为什么这样做"。

**文件头注释**：不需要文件头注释（作者、日期等通过 Git 追踪）。

**TODO 规范**：使用 TODO(姓名): 描述 格式标记待办事项。

## 22.8 Magic Number 禁止

禁止在代码中出现 Magic Number（无明确含义的数字字面量）。所有数字常量必须定义为命名常量并放置在 src/constants/ 目录下。

## 22.9 Lint 规范

**ESLint**：使用 ESLint 进行代码规范检查。配置文件继承 Vue 3 和 TypeScript 推荐规则。

**Prettier**：使用 Prettier 进行代码格式化。

**提交前检查**：通过 lint-staged 在 Git 提交前自动运行 ESLint 和 Prettier。

## 22.10 本章禁止事项

- 禁止使用 var。
- 禁止 Magic Number。
- 禁止无意义的注释。
- 禁止函数内部嵌套超过 3 层。
- 禁止函数参数超过 3 个时不使用 Options Object 模式。
- 禁止超长组件（Vue 组件超过 300 行必须拆分）。
- 禁止超长函数（函数超过 50 行应拆分为子函数）。

## 22.11 本章检查清单

- 命名是否遵循命名对照表？
- Import 排序是否正确？
- 是否全部使用 const/let 而非 var？
- 是否存在 Magic Number？
- 函数参数是否不超过 3 个？
- 所有代码是否通过了 ESLint 和 Prettier？
- 组件是否不超过 300 行？


---

# 第二十三章 Git协作规范

## 23.1 章节概述

本章定义团队的 Git 协作标准，确保多人协作时的代码管理有序、可追溯。

## 23.2 核心原则

- **分支管理**：严格遵守分支策略，不直接在 main 分支上开发。
- **Commit 规范**：每次提交有明确的语义和范围。
- **Code Review**：所有代码合并前必须经过审查。
- **可追溯**：任何代码变更都能追溯到对应的需求和审查记录。

## 23.3 分支策略

**分支类型**：

| 分支 | 用途 | 命名格式 |
|------|------|----------|
| main | 生产就绪代码，只接受 merge | main |
| develop | 开发主分支，功能分支合并的目标 | develop |
| feature | 新功能开发 | feature/模块-功能描述 |
| bugfix | Bug 修复 | bugfix/问题描述 |
| release | 发布准备 | release/v1.0.0 |
| hotfix | 生产紧急修复 | hotfix/问题描述 |

**分支生命周期**：功能分支从 develop 创建，完成后合并回 develop。release 分支从 develop 创建，完成后合并到 main 和 develop。hotfix 分支从 main 创建，完成后合并到 main 和 develop。

## 23.4 Commit 规范

**格式**：使用 Conventional Commits 规范：type(scope): description。

**Type 枚举**：

| Type | 说明 |
|------|------|
| feat | 新功能 |
| fix | Bug 修复 |
| docs | 文档更新 |
| style | 代码格式调整（不影响逻辑） |
| refactor | 代码重构（不改变功能） |
| perf | 性能优化 |
| test | 测试相关 |
| build | 构建系统或依赖变更 |
| chore | 杂项（配置文件、脚本等） |

**Scope**：使用模块名，如 auth、training、review、report、ai、dashboard。

**Description**：使用中文简洁描述变更内容，不超过 50 字。

**禁止**：禁止使用"update"、"modify"、"fix bug"等无意义描述。

## 23.5 Merge 策略

- 功能分支合并到 develop：使用 Squash Merge，将多条 Commit 压缩为一条。
- Release 分支合并到 main：使用 Merge Commit，保留完整历史。
- Hotfix 分支合并：使用 Merge Commit。

## 23.6 Pull Request 规范

**标题**：与 Commit 格式一致：type(scope): description。

**描述**：必须包含：变更说明、测试情况、截图（如有 UI 变更）、关联 Issue 编号。

**Review 要求**：至少 1 名 Reviewer 审查通过后才能合并。

**Checklist**：PR 提交前必须通过：ESLint 检查、Prettier 格式化、单元测试通过、无 Merge Conflict。

## 23.7 Code Review 规范

**审查要点**：代码是否符合规范、逻辑是否正确、是否存在安全隐患、是否有性能问题。

**审查时间**：Reviewer 应在 24 小时内完成审查。

**通过标准**：所有评论已解决、无阻断性问题、至少 1 人 Approve。

## 23.8 本章禁止事项

- 禁止直接 push 到 main 分支。
- 禁止 Commit 信息无意义（如 "update"、"fix"）。
- 禁止未经 Code Review 的代码合并。
- 禁止跳过 PR Checklist。
- 禁止在 main 分支上直接修改代码。

## 23.9 本章检查清单

- 是否从正确的分支创建新分支？
- Commit 信息是否符合 type(scope): description 格式？
- PR 描述是否包含必要信息？
- 是否经过 Code Review？
- PR 是否通过所有 CI 检查？


---

# 第二十四章 开发Checklist

## 24.1 章节概述

本章输出完整的开发自检清单，覆盖前 23 章的所有规范要点。开发人员应在每个开发阶段完成后对照本清单自检。

## 24.2 编码前检查

- □ 是否已阅读并理解 Frontend Specification 全部规范？
- □ 是否已完成 Component Library 检查，确认需要的组件是否已存在？
- □ 是否已确认功能的 MVP 优先级？
- □ 是否已确认页面所需的数据结构和 API 接口？
- □ 是否已编写对应的 Mock 接口？
- □ 是否已确认页面的路由配置和权限要求？
- □ 是否确认需要新建的组件已通过组件评审？

## 24.3 组件开发检查

- □ 组件是否使用 script setup 语法和 Composition API？
- □ Props、Emits、Slots 是否使用 TypeScript 类型声明？
- □ Props 数量是否不超过 8 个？
- □ 组件命名是否符合 PascalCase 规范？
- □ 基础组件是否使用 Base 前缀？AI 组件是否使用 AI 前缀？
- □ 组件是否覆盖 Loading/Empty/Error 状态？
- □ 样式是否使用 scoped？颜色和间距是否引用 Design Tokens？
- □ 组件文件是否包含单元测试？
- □ 组件是否不超过 300 行？
- □ 子组件 Props 透传是否不超过 2 层？
- □ 模板嵌套是否不超过 3 层？

## 24.4 页面开发检查

- □ 页面是否使用 AppLayout 或指定布局组件？
- □ 页面是否包含 PageHeader 组件？
- □ 页面逻辑是否封装在 Composable 中？
- □ 页面模板是否不超过 200 行？
- □ 页面 script setup 部分是否不超过 100 行？
- □ 页面是否覆盖 Loading/Empty/Error 状态？
- □ 列表页是否支持分页？是否包含搜索和筛选功能？
- □ 路由是否配置了懒加载？路由 meta 是否声明了完整信息？
- □ 是否配置了 KeepAlive（如需要）？
- □ 表单是否支持新增/编辑/查看三种模式？
- □ 表单校验是否在输入时实时触发？
- □ 提交按钮是否有 Loading 状态和防重复？

## 24.5 状态管理检查

- □ 全局状态是否存储在 Pinia Store？
- □ Store 是否使用 Setup Store 语法？
- □ Store 命名是否符合 use + PascalCase + Store 格式？
- □ 组件是否通过 actions 修改状态？
- □ 是否存在 Store 间循环依赖？
- □ 页面临时状态是否使用 Composable 而非 Store？
- □ 是否存在 Prop Drilling 超过 2 层？

## 24.6 接口对接检查

- □ 所有 API 调用是否通过 API 层（src/api/modules/）？
- □ API 函数是否有完整的 TypeScript 类型？
- □ Mock 数据结构是否与 API 规范一致？
- □ 是否使用统一的 Axios 实例？
- □ 响应拦截器是否统一处理错误？
- □ Token 是否通过请求拦截器自动注入？
- □ Token 是否存储在 sessionStorage？

## 24.7 AI 模块检查

- □ AI 相关区域是否使用统一的 AI 视觉标识？
- □ 是否使用 Component Library 中的 AI Components？
- □ AI 评分是否附带置信度展示（AIConfidenceTag）？
- □ AI 分析是否附带推理过程（AIReasonCard）？
- □ 是否支持教师逐维度覆写（TeacherOverridePanel）？
- □ 教师覆写是否必填覆写原因？
- □ AI 输出是否支持流式渲染（AIStreamingOutput）？
- □ 是否记录 AI 调用日志（模型、Token、耗时）？
- □ AI 结果是否经人工确认后才发布？
- □ Prompt 模板是否支持版本管理？

## 24.8 性能检查

- □ 所有路由是否使用懒加载？
- □ 第三方库是否按需引入？
- □ 500 条以上列表是否使用虚拟滚动？
- □ 高频事件是否使用防抖或节流？
- □ 是否使用 computed 缓存派生数据？
- □ 首屏 JS 体积是否小于 500KB？
- □ 静态图片是否优化（WebP 格式、懒加载）？

## 24.9 安全检查

- □ 是否不存在 v-html 渲染用户输入？
- □ 外部链接是否使用 rel="noopener noreferrer"？
- □ Token 是否存储在 sessionStorage 而非 localStorage？
- □ 生产环境是否移除所有 console.log？
- □ 文件上传是否有类型和大小校验？
- □ 日志中是否不包含敏感信息？

## 24.10 代码质量检查

- □ 是否全部使用 const/let 而非 var？
- □ 是否存在 any 类型？
- □ 是否存在 Magic Number？
- □ 函数参数是否不超过 3 个？
- □ Import 排序是否符合规范？
- □ 命名是否遵循命名对照表？
- □ 函数是否不超过 50 行？
- □ 所有代码是否通过 ESLint 和 Prettier？

## 24.11 提交前检查

- □ 代码是否通过 ESLint 检查？
- □ 代码是否通过 Prettier 格式化？
- □ 是否移除了所有 console.log 调试代码？
- □ 是否移除了未使用的 import 和变量？
- □ Commit Message 是否符合 Conventional Commits 格式？
- □ PR 描述是否包含变更说明和截图？
- □ 是否在 Chrome 最新版本测试通过？
- □ 单次 PR 代码行数是否不超过 500 行（重构类 PR 除外）？


---

## 文档修订历史

| 版本 | 日期 | 修订内容 | 修订人 |
|------|------|----------|--------|
| v1.0 | 2026-07-02 | 初始发布，含24章完整开发规范 | 前端架构组 |

---

## 附录

### 附录 A：命名规范速查表

| 实体 | 规范 | 示例 |
|------|------|------|
| 组件文件 | PascalCase.vue | UserTable.vue |
| 页面目录 | kebab-case | teaching-check/ |
| TS 文件 | kebab-case.ts | use-request.ts |
| Store 文件 | kebab-case.store.ts | user.store.ts |
| Mock 文件 | kebab-case.mock.ts | check.mock.ts |
| API 文件 | kebab-case.api.ts | user.api.ts |
| interface | I + PascalCase | IUserInfo |
| type | PascalCase | ApiResponse |
| 常量 | UPPER_SNAKE_CASE | MAX_RETRY_COUNT |
| 变量 | camelCase | isLoading |
| 函数 | camelCase | handleSubmit |
| Composable | use + PascalCase | usePagination |
| Store 实例 | use + PascalCase + Store | useUserStore |
| Props | camelCase | userList |
| Emits | kebab-case | item-delete |

### 附录 B：文件目录速查表

| 路径 | 职责 |
|------|------|
| src/api/ | API 接口封装 |
| src/assets/ | 静态资源 |
| src/components/ | 全局可复用组件 |
| src/layouts/ | 布局框架 |
| src/pages/ | 页面级组件 |
| src/router/ | 路由配置 |
| src/stores/ | Pinia Store |
| src/composables/ | 全局 Composable |
| src/hooks/ | 业务 Hooks |
| src/utils/ | 纯工具函数 |
| src/types/ | 全局类型定义 |
| src/constants/ | 全局常量 |
| src/mock/ | Mock 数据 |
| src/styles/ | 全局样式 |
| src/plugins/ | 插件配置 |
| src/config/ | 应用配置 |

### 附录 C：禁止事项清单

1. 禁止使用 Options API，全面采用 Composition API。
2. 禁止使用 JavaScript，全面采用 TypeScript。
3. 禁止使用 Vuex，全面采用 Pinia。
4. 禁止使用 any 类型。
5. 禁止使用 var，全面采用 const/let。
6. 禁止页面/组件直接使用 axios。
7. 禁止 Token 存储在 localStorage。
8. 禁止 v-html 渲染用户输入。
9. 禁止组件逻辑超过 300 行。
10. 禁止页面业务逻辑超过 100 行。
11. 禁止 Store 循环依赖。
12. 禁止全局导入 Element Plus。
13. 禁止 Table 数据超 500 行不使用虚拟滚动。
14. 禁止 AI 结果不经人工确认直接使用。
15. 禁止 Mock 结构与真实 API 不一致。
16. 禁止生产环境保留 console.log。
17. 禁止生产环境保留 Mock。
18. 禁止直接 push main 分支。
19. 禁止未经 Code Review 合并代码。
20. 禁止使用 Magic Number。

### 附录 D：角色权限矩阵

| 功能 | 学生 | 教师 | 管理员 |
|------|:--:|:--:|:--------:|:--:|
| 个人仪表盘 | ✓ | ✓ | ✓ | ✓ |
| 提交实训成果 | ✓ | | | |
| 查看个人成绩 | ✓ | | | |
| 班级数据查看 | | ✓ | ✓ | |
| AI 评价复核 | | ✓ | ✓ | |
| 全院数据查看 | | | ✓ | |
| 成绩发布 | | ✓ | | |
| 评价标准管理 | | | ✓ | |
| Prompt 模板管理 | | | ✓ | |
| 学生管理 | | | ✓ | |
| 教师管理 | | | ✓ | ✓ |
| 角色管理 | | | | ✓ |
| 系统配置 | | | | ✓ |
| 日志查看 | | | | ✓ |

---

*本文档为《基于大模型的软件实训教学检查评价与报表系统》前端开发最高规范文档。*

*所有开发人员必须严格遵循本文档的各项规定。*

*规范修订需经前端架构组评审和团队讨论通过。*
