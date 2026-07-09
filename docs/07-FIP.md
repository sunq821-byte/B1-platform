# Frontend Implementation Plan v1.0

## 基于大模型的软件实训教学检查评价与报表系统

---

**文档状态**：正式发布

**文档版本**：v1.0

**适用范围**：前端开发团队实施执行依据

**文档定位**：本文档为项目前端实施蓝图，聚焦"如何开发"。产品功能定义参见《PRD》，系统架构设计参见《SDS》，编码规范参见《Frontend Specification v1.0》。

**前置文档**：PRD v1.0 / SDS v1.0 / UI Design System v1.0 / Component Library v1.0 / Frontend Specification v1.0 / API Mock Specification v1.0

**技术栈**：Vue 3 + TypeScript + Vite + Pinia + Vue Router + Axios + Element Plus + ECharts + Lucide Icons + Mock.js

**角色体系**：学生 / 教师（含科研负责人职能） / 管理员

**总开发周期**：14 周（7 个 Sprint，每 Sprint 2 周）

**开发人力**：前端 2-3 人

---

## 目录

1. [开发目标](#第一章-开发目标)
2. [总体前端架构](#第二章-总体前端架构)
3. [项目目录](#第三章-项目目录)
4. [技术栈](#第四章-技术栈)
5. [路由策略](#第五章-路由策略)
6. [状态管理](#第六章-状态管理)
7. [API 集成策略](#第七章-api-集成策略)
8. [权限策略](#第八章-权限策略)
9. [组件开发策略](#第九章-组件开发策略)
10. [页面开发工作流](#第十章-页面开发工作流)
11. [Sprint 计划](#第十一章-sprint-计划)
12. [开发顺序](#第十二章-开发顺序)
13. [编码标准](#第十三章-编码标准)
14. [质量保证](#第十四章-质量保证)
15. [风险管理](#第十五章-风险管理)

---

## 术语约定

全文统一使用以下术语：

| 术语 | 说明 |
|------|------|
| 页面 | Vue Router 对应的页面级组件，存放于 src/pages/ |
| 组件 | 可复用的 Vue 组件，存放于 src/components/ |
| Store | Pinia Store 实例，存放于 src/stores/ |
| Composable | Vue Composition API 的可复用逻辑单元，存放于 src/composables/ |
| Hooks | 业务逻辑 Hooks，存放于 src/hooks/ |
| Utils | 纯工具函数，存放于 src/utils/，不依赖 Vue 响应式系统 |
| API 层 | src/api/ 目录下的接口封装模块，页面不可绕过 API 层直接调用 axios |
| Mock | Mock.js 模拟数据，存放于 src/mock/ |
| DTO | Data Transfer Object，接口传输对象 |
| VO | View Object，视图展示对象 |
| AI 模块 | 与大模型交互的前端模块，具有独立的状态管理和错误处理体系 |
| Token | JWT 认证令牌 |
| RBAC | 基于角色的访问控制模型 |
| Sprint | 一个开发冲刺周期（2 周 / 10 个工作日） |
| 人天 | 一个开发者一天的工作量（8 小时） |
| SSE | Server-Sent Events，服务器向客户端推送流式数据的技术 |

---

# 第一章 开发目标

## 1.1 章节概述

本章定义前端开发的总体目标、交付范围和成功标准。所有后续 Sprint 的执行和验收均以本章定义的目标为准。

## 1.2 前端开发总体目标

**目标一**：完成三角色（学生、教师含科研负责人、管理员）全部业务页面的开发，总计 25 个页面，所有页面通过 UI Review 和功能测试。

**目标二**：建成完整的前端基础设施体系，包括路由框架、权限体系、API 层、Mock 层、Store 体系、组件库，支撑所有业务页面的高效开发和稳定运行。

**目标三**：实现 AI 模块的前端交互体系，包括 SSE 流式响应接收、AI 分析进度展示、AI 结果人工确认面板、Token 用量展示，AI 相关的前端交互体验达到竞赛一等奖水平。

**目标四**：交付物达到企业级质量标准。首屏加载时间小于 2 秒，打包体积（gzip 后）小于 300KB，所有页面覆盖 Loading / Empty / Error 三种状态，通过 ESLint 零错误检查。

**目标五**：完成 Docker 镜像构建和部署脚本，支持在银河麒麟 + LoongArch 环境下运行。

## 1.3 交付范围

### 1.3.1 基础设施交付

| 交付项 | 说明 |
|--------|------|
| 项目脚手架 | Vite + Vue 3 + TypeScript 完整项目结构 |
| 路由体系 | 公开路由 + 动态路由 + 导航守卫 + 菜单生成 + 面包屑 |
| API 层 | Axios 封装 + 拦截器 + Token 刷新 + 错误处理 + 重试 |
| Mock 层 | Mock.js 配置 + 全模块 Mock 数据 + 环境变量切换 |
| Store 体系 | User / App / Permission / Training / Review / Dashboard / Report 七大 Store |
| Layout 组件 | AppLayout / Navbar / Sidebar / PageContainer / PageHeader |
| 权限体系 | v-permission 指令 + 路由权限 + 菜单权限 + 按钮权限 |
| 全局组件 | Loading / Error / Empty / NoPermission 状态组件 |
| Lint 配置 | ESLint + Prettier + Husky + lint-staged |

### 1.3.2 页面交付

| 角色 | 页面数 | 列表 |
|------|--------|------|
| 认证 | 1 | 登录页 |
| 学生 | 7 | Dashboard / 任务中心 / 任务详情 / 提交 / AI 结果 / 个人报表 / 成长中心 |
| 教师 | 11 | Dashboard / 课程管理 / 任务管理 / 评价标准 / 标准库 / 提交审核 / AI 预览 / 评分工作台 / 班级报表 / 学院报表 / 学生管理 |
| 管理员 | 3 | Dashboard / 用户管理 / 系统配置 |
| 公共 | 3 | 403 / 404 / 入口页 |
| **合计** | **25** | |

### 1.3.3 组件交付

| 分类 | 数量 | 说明 |
|------|------|------|
| Base 基础组件 | 15-20 | Element Plus 二次封装 |
| Business 业务组件 | 20-30 | 跨页面复用的业务逻辑组件 |
| Layout 布局组件 | 5-8 | 全局布局 |
| Chart 图表组件 | 6-8 | ECharts 封装 |
| AI 专用组件 | 8-12 | AI 分析展示和交互 |
| **合计** | **54-78** | |

## 1.4 成功标准

### 1.4.1 性能指标

| 指标 | 目标 | 测量方式 |
|------|------|---------|
| 首屏加载时间（FCP） | < 2s | Lighthouse |
| 最大内容绘制（LCP） | < 3s | Lighthouse |
| 首次输入延迟（FID） | < 100ms | Lighthouse |
| 页面路由切换时间 | < 500ms | Chrome DevTools Performance |
| 打包总体积（gzip） | < 300KB | Vite build 输出 |
| AI 流式响应首字延迟 | < 3s | 手动测试计时 |

### 1.4.2 质量指标

| 指标 | 目标 |
|------|------|
| ESLint 检查 | 零错误 |
| TypeScript 类型检查 | 零错误 |
| 页面状态覆盖 | Loading / Empty / Error / Success 四种状态全部覆盖 |
| 组件复用率 | 业务组件中 60% 以上被 2 个及以上页面复用 |
| Mock 与 API 规范一致性 | 100% 字段名和数据结构一致 |

### 1.4.3 体验指标

| 指标 | 目标 |
|------|------|
| 表单提交 | 所有表单有防重复提交机制 |
| 错误提示 | 所有错误提供重试或返回操作 |
| AI 确认 | AI 分析结果 100% 经人工确认后可生效 |
| 权限体验 | 无权限时展示降级页面或隐藏操作按钮 |

## 1.5 不交付范围

以下内容不在前端交付范围内：

- 后端 API 实现（Spring Boot 代码）
- 数据库设计和数据迁移脚本
- AI 模型训练和 Prompt 工程（Prompt 模板整理由协作完成）
- 部署文档和运维手册（由后端团队负责）
- 移动端适配（本项目仅支持桌面端：1920px / 1366px / 1024px）

## 1.6 前后端分工边界

| 层面 | 前端负责 | 后端负责 | 协作方式 |
|------|---------|---------|---------|
| 页面渲染 | 全部 | 无 | 前端独立 |
| 业务逻辑 | UI 交互逻辑、表单校验、前端状态管理 | 核心业务规则、数据校验、权限校验（最终权威） | 后端提供 API，前端调用 |
| 数据 | 前端 Store 状态、页面缓存 | 数据库 CRUD、数据一致性 | 通过 API 契约对接 |
| 权限 | UI 层权限控制（按钮/菜单/路由） | 接口层权限校验（最终安全控制） | 前端按后端返回的权限列表做 UI 控制 |
| AI | 请求触发、SSE 接收、结果展示、人工确认面板 | AI 调用、Prompt 管理、结果存储 | 前端通过 AI API 层对接后端 SSE 接口 |
| 文件 | 上传组件、预览组件、下载触发 | 文件存储、格式校验、病毒扫描 | 前端通过文件 API 层对接 |

---

# 第二章 总体前端架构

## 2.1 章节概述

本章阐述前端整体架构设计，包括分层架构、模块依赖关系、应用生命周期和启动流程。系统架构的完整设计参见《SDS》第二章，本章仅聚焦前端视角的实施架构。

## 2.2 前端分层架构

前端采用四层架构，自上而下为：

**第一层：展示层（Presentation Layer）**

职责：页面渲染和用户交互。包含 pages/ 目录下的页面组件和 layouts/ 目录下的布局组件。展示层不直接调用 axios，不包含复杂业务逻辑。展示层通过调用 Composable 和 Store 获取数据和状态。

**第二层：业务层（Business Logic Layer）**

职责：业务逻辑封装。包含 composables/ 和 hooks/ 目录。业务层处理表单校验逻辑、数据转换逻辑、交互流程控制。业务层可以调用 API 层和 Store，但不可直接操作 DOM。

**第三层：数据层（Data Layer）**

职责：数据获取、状态管理和缓存。包含 stores/ 和 api/ 目录。数据层负责与后端通信（或 Mock 通信）、数据缓存策略、全局状态维护。数据层不包含 UI 逻辑。

**第四层：服务层（Service Layer）**

职责：基础设施服务。包含 router/（路由服务）、plugins/（插件配置）、utils/（工具函数）、mock/（Mock 服务）。服务层为上层提供基础能力支撑。

### 2.2.1 层间通信规则

| 通信方向 | 方式 | 示例 |
|---------|------|------|
| 展示层 -> 业务层 | 调用 Composable | 页面调用 useTable() 获取表格逻辑 |
| 展示层 -> 数据层 | 调用 Store Action | 页面调用 useTrainingStore().fetchTasks() |
| 业务层 -> 数据层 | 调用 Store Action 或 API 函数 | Composable 调用 Store |
| 数据层 -> 服务层 | API 层调用 axios 实例 | api/training.ts 调用 http 实例 |
| 服务层 -> 外部 | 发送 HTTP 请求 | axios 发送请求到后端或 Mock |

禁止反向通信：数据层不可导入展示层组件，业务层不可操作 DOM，服务层不可依赖业务层。

## 2.3 模块依赖关系

**路由模块 -> 权限模块 -> 页面模块**

路由守卫触发权限检查，权限检查通过后动态注册路由，路由匹配成功后渲染页面组件。

**页面模块 -> Store 模块 -> API 模块**

页面通过 Store Action 发起数据请求，Store Action 调用 API 层函数，API 层通过 Axios 实例发送 HTTP 请求。开发期请求被 Mock 拦截，生产期请求发送到后端。

**API 模块 -> Mock 模块（开发期）/ 后端服务（生产期）**

通过环境变量 VITE_USE_MOCK 控制。开启时 Mock.js 拦截请求返回模拟数据，关闭时请求正常发送到后端。

**组件模块 -> Design System**

所有组件通过 Design System Token 获取颜色、间距、圆角、阴影等样式变量，确保视觉一致性。Token 定义参见《UI Design System v1.0》第 2 章。

## 2.4 应用目录职责矩阵

| 目录 | 职责 | 主要文件 | 依赖目录 | 被依赖方 |
|------|------|---------|---------|---------|
| api/ | HTTP 请求封装 | 按模块拆分的 API 函数文件 | utils/（http 实例）、types/（DTO 类型） | stores/、composables/ |
| assets/ | 静态资源 | 图片、字体、图标 | 无 | 全局 |
| components/ | 全局可复用组件 | base/、business/、layout/、chart/、ai/ | stores/、composables/、styles/ | pages/ |
| composables/ | 全局 Composable | 跨页面复用的响应式逻辑 | stores/、api/、utils/ | pages/、components/ |
| config/ | 应用配置 | 环境变量映射、全局常量 | 无 | 全局 |
| constants/ | 常量定义 | 枚举、选项列表、静态映射表 | 无 | 全局 |
| hooks/ | 业务 Hooks | 特定业务场景的响应式逻辑 | stores/、api/ | pages/ |
| layouts/ | 布局组件 | AppLayout、Navbar、Sidebar | components/、stores/ | router/ |
| mock/ | Mock 数据 | 按模块拆分的 Mock 文件 | types/ | api/（开发期） |
| pages/ | 页面组件 | 按角色组织的页面文件 | components/、stores/、composables/、hooks/ | router/ |
| plugins/ | 插件配置 | Element Plus、全局组件注册 | components/ | main.ts |
| router/ | 路由配置 | 路由表、导航守卫 | stores/、pages/、layouts/ | main.ts |
| stores/ | Pinia Store | 全局状态管理 | api/、types/、utils/ | pages/、composables/ |
| styles/ | 全局样式 | CSS 变量、全局重置、工具类 | 无 | 全局 |
| types/ | 全局类型 | interface、type、enum 定义 | 无 | 全局 |
| utils/ | 纯工具函数 | 格式化、校验、转换函数 | types/ | 全局 |

## 2.5 应用生命周期

从用户打开浏览器到页面可交互，经历以下五个阶段：

**阶段一：资源加载**

浏览器加载 index.html，解析 script 标签加载打包后的 JS 和 CSS 文件。Vite 生产构建使用 code-splitting，按路由分割代码块，首屏仅加载核心框架和当前页面代码。

**阶段二：Vue 应用实例化**

main.ts 执行。按顺序：创建 Vue 应用实例 -> 注册 Pinia -> 注册 Router -> 注册 Element Plus -> 注册全局组件 -> 挂载应用到 DOM。

**阶段三：路由守卫触发**

Router 的 beforeEach 守卫执行。按顺序：检查 Token 存在性 -> Token 有效则获取用户信息和权限 -> Token 无效则跳转登录页 -> 权限加载完成后动态添加路由 -> 放行目标路由。

**阶段四：页面渲染**

Vue Router 匹配路由，加载对应页面组件。页面组件的 onMounted 中触发数据请求，Store Action 调用 API 层获取数据。数据返回前展示 Loading 状态（骨架屏或加载动画），数据返回后渲染页面内容。

**阶段五：页面可交互**

数据渲染完成，用户可进行点击、输入、滚动等操作。后续交互通过 Store Action 或 Composable 驱动，遵循"用户操作 -> 状态变更 -> 视图更新"的单向数据流。

## 2.6 启动流程

main.ts 中的执行顺序：

1. 导入全局样式（CSS 变量、重置样式）
2. 创建 Pinia 实例并注册
3. 创建 Router 实例并注册
4. 注册 Element Plus（按需引入的组件和插件）
5. 注册全局组件（Base Components、Layout Components）
6. 注册自定义指令（v-permission）
7. 挂载应用到 #app DOM 节点
8. Router 解析当前 URL，触发导航守卫

初始化失败处理策略：

- Pinia 初始化失败：应用无法启动，展示"系统初始化失败"错误页面，提供刷新按钮。
- Router 初始化失败：同上，应用无法启动。
- Element Plus 注册失败：组件降级展示原生 HTML 元素，控制台输出错误日志。
- 全局组件注册失败：受影响页面降级展示，不影响其他页面。

---

# 第三章 项目目录

## 3.1 章节概述

本章提供完整的前端项目目录树，定义每个目录的职责、存放内容和边界。本章目录结构在《Frontend Specification v1.0》第二章目录规范的基础上，增加了实施层面的细化说明。

## 3.2 完整目录树

```
B1_Platform/
├── .env.development              # 开发环境变量
├── .env.production               # 生产环境变量
├── .env.mock                     # Mock 环境变量
├── .eslintrc.cjs                 # ESLint 配置
├── .prettierrc.json              # Prettier 配置
├── index.html                    # HTML 入口
├── package.json                  # 依赖和脚本
├── tsconfig.json                 # TypeScript 配置
├── vite.config.ts                # Vite 构建配置
└── src/
    ├── main.ts                   # 应用入口
    ├── App.vue                   # 根组件
    ├── api/                      # API 接口封装层
    │   ├── http.ts               # Axios 实例和拦截器
    │   ├── auth.ts               # 认证接口
    │   ├── course.ts             # 课程接口
    │   ├── training.ts           # 实训接口
    │   ├── submission.ts         # 提交接口
    │   ├── review.ts             # 复核接口
    │   ├── report.ts             # 报表接口
    │   ├── dashboard.ts          # 仪表盘接口
    │   ├── user.ts               # 用户管理接口
    │   ├── system.ts             # 系统配置接口
    │   ├── knowledge.ts          # 知识库接口
    │   └── ai.ts                 # AI 接口（SSE）
    ├── assets/                   # 静态资源
    │   ├── images/               # 图片
    │   ├── fonts/                # 字体文件
    │   └── icons/                # SVG 图标
    ├── components/               # 全局通用组件
    │   ├── base/                 # 基础组件（Element Plus 二次封装）
    │   │   ├── BaseButton.vue
    │   │   ├── BaseInput.vue
    │   │   ├── BaseModal.vue
    │   │   ├── BaseTable.vue
    │   │   ├── BaseForm.vue
    │   │   ├── BaseSelect.vue
    │   │   ├── BaseDatePicker.vue
    │   │   ├── BaseUpload.vue
    │   │   └── BasePagination.vue
    │   ├── business/             # 业务组件（跨页面复用）
    │   │   ├── UserSelector.vue
    │   │   ├── CourseCard.vue
    │   │   ├── TaskStatusBadge.vue
    │   │   ├── AIScorePanel.vue
    │   │   ├── FileUploader.vue
    │   │   ├── ReviewTimeline.vue
    │   │   ├── ScoreDetailTable.vue
    │   │   ├── SubmissionPreview.vue
    │   │   └── ConfirmDialog.vue
    │   ├── layout/               # 布局组件
    │   │   ├── AppLayout.vue
    │   │   ├── Navbar.vue
    │   │   ├── Sidebar.vue
    │   │   ├── PageHeader.vue
    │   │   └── PageContainer.vue
    │   ├── chart/                 # 图表组件（ECharts 封装）
    │   │   ├── BaseChart.vue
    │   │   ├── LineChart.vue
    │   │   ├── BarChart.vue
    │   │   ├── PieChart.vue
    │   │   ├── RadarChart.vue
    │   │   └── GaugeChart.vue
    │   ├── ai/                    # AI 专用组件
    │   │   ├── AIAnalysisProgress.vue
    │   │   ├── AIResultCard.vue
    │   │   ├── AIScoreDisplay.vue
    │   │   ├── AIConfirmPanel.vue
    │   │   ├── StreamingTextDisplay.vue
    │   │   ├── AITokenUsage.vue
    │   │   └── AIErrorRetry.vue
    │   └── common/                # 通用展示组件
    │       ├── LoadingState.vue
    │       ├── ErrorState.vue
    │       ├── EmptyState.vue
    │       ├── NoPermission.vue
    │       └── PageSkeleton.vue
    ├── composables/               # 全局 Composable
    │   ├── useAuth.ts            # 认证逻辑
    │   ├── usePermission.ts      # 权限检查
    │   ├── useTable.ts           # 表格通用逻辑
    │   ├── usePagination.ts      # 分页通用逻辑
    │   ├── useForm.ts            # 表单通用逻辑
    │   ├── useFileUpload.ts      # 文件上传逻辑
    │   └── useSSE.ts             # SSE 流式接收逻辑
    ├── config/                    # 应用配置
    │   └── index.ts              # 环境变量和全局配置
    ├── constants/                 # 常量定义
    │   ├── roles.ts              # 角色常量
    │   ├── permissions.ts        # 权限常量
    │   └── enums.ts              # 业务枚举
    ├── hooks/                     # 业务 Hooks
    │   ├── useTaskList.ts        # 任务列表业务逻辑
    │   ├── useReview.ts          # 复核业务逻辑
    │   └── useAIAnalysis.ts      # AI 分析业务逻辑
    ├── layouts/                   # 布局组件（别名，指向 components/layout/）
    ├── mock/                      # Mock 数据
    │   ├── index.ts              # Mock 初始化
    │   ├── auth.ts               # 认证 Mock
    │   ├── course.ts             # 课程 Mock
    │   ├── training.ts           # 实训 Mock
    │   ├── submission.ts         # 提交 Mock
    │   ├── review.ts             # 复核 Mock
    │   ├── dashboard.ts          # 仪表盘 Mock
    │   ├── report.ts             # 报表 Mock
    │   ├── user.ts               # 用户管理 Mock
    │   ├── system.ts             # 系统配置 Mock
    │   └── ai.ts                 # AI Mock（含 SSE 模拟）
    ├── pages/                     # 页面组件
    │   ├── auth/                 # 认证页面
    │   │   └── LoginPage.vue
    │   ├── student/              # 学生端页面
    │   │   ├── StudentDashboardPage.vue
    │   │   ├── CourseListPage.vue
    │   │   ├── CourseDetailPage.vue
    │   │   ├── TaskListPage.vue
    │   │   ├── TaskDetailPage.vue
    │   │   ├── SubmissionPage.vue
    │   │   ├── MyScorePage.vue
    │   │   └── ProfilePage.vue
    │   ├── teacher/              # 教师端页面
    │   │   ├── TeacherDashboardPage.vue
    │   │   ├── CourseManagePage.vue
    │   │   ├── TaskManagePage.vue
    │   │   ├── SubmissionListPage.vue
    │   │   ├── SubmissionDetailPage.vue
    │   │   ├── AIAnalysisPage.vue
    │   │   ├── ReviewPage.vue
    │   │   └── ScoreManagePage.vue
    │   ├── admin/                # 管理员端页面
    │   │   ├── AdminDashboardPage.vue
    │   │   ├── UserManagePage.vue
    │   │   └── SystemConfigPage.vue
    │   └── common/               # 公共页面
    │       ├── NotFoundPage.vue
    │       └── ProfilePage.vue
    ├── plugins/                   # 插件配置
    │   ├── element-plus.ts       # Element Plus 按需注册
    │   ├── global-components.ts  # 全局组件注册
    │   └── directives.ts        # 全局指令注册
    ├── router/                    # 路由配置
    │   ├── index.ts              # Router 实例 + 导航守卫
    │   ├── routes/               # 路由配置
    │   │   ├── public.ts         # 公开路由
    │   │   ├── student.ts        # 学生路由
    │   │   ├── teacher.ts        # 教师路由

    │   │   └── admin.ts          # 管理员路由
    │   └── guards/               # 导航守卫
    │       ├── auth.ts           # 认证守卫
    │       └── permission.ts     # 权限守卫
    ├── stores/                    # Pinia Store
    │   ├── useUserStore.ts       # 用户 Store
    │   ├── useAppStore.ts        # 应用 Store
    │   ├── usePermissionStore.ts # 权限 Store
    │   ├── useTrainingStore.ts   # 实训 Store
    │   ├── useReviewStore.ts     # 复核 Store
    │   ├── useDashboardStore.ts  # 仪表盘 Store
    │   └── useReportStore.ts     # 报表 Store
    ├── styles/                    # 全局样式
    │   ├── variables.css         # CSS 变量（Design Token）
    │   ├── reset.css             # 全局重置样式
    │   ├── global.css            # 全局样式
    │   └── utils.css             # 工具类
    ├── types/                     # 全局 TypeScript 类型
    │   ├── api.ts                # API 通用类型（分页、响应格式）
    │   ├── user.ts               # 用户类型
    │   ├── course.ts             # 课程类型
    │   ├── training.ts           # 实训类型
    │   ├── review.ts             # 复核类型
    │   ├── dashboard.ts          # 仪表盘类型
    │   ├── report.ts             # 报表类型
    │   ├── ai.ts                 # AI 类型
    │   └── common.ts             # 通用类型
    └── utils/                     # 纯工具函数
        ├── format.ts             # 格式化工具（日期、数字、文件大小）
        ├── validate.ts           # 校验工具（邮箱、手机号、密码强度）
        ├── storage.ts            # 本地存储封装
        └── request.ts            # Axios 实例（重新导出 api/http.ts）
```

## 3.3 关键目录边界说明

### 3.3.1 components/ 与 pages/ 的边界

components/ 存放可复用的组件，不得包含路由相关的逻辑（如 router.push）、不得引用 pages/ 中的任何文件。pages/ 存放页面级组件，可以引用 components/ 中的任意组件、stores/ 中的 Store、composables/ 中的 Composable。页面组件负责编排子组件、处理路由参数、触发初始数据加载。

### 3.3.2 composables/ 与 hooks/ 的边界

composables/ 存放通用的、与技术领域无关的 Composable（如 useTable、usePagination），可被任何页面引用。hooks/ 存放特定业务场景的 Hooks（如 useTaskList、useAIAnalysis），仅在特定业务页面中使用。技术判断标准：如果一个 Composable 的函数体中不包含业务术语（如 Task、Review、AI Score），则放入 composables/，否则放入 hooks/。

### 3.3.3 utils/ 与 composables/ 的边界

utils/ 存放纯函数，不依赖 Vue 响应式系统（不使用 ref、reactive、watch、computed 等），不导入 Vue 相关的任何模块。composables/ 依赖 Vue 响应式系统，使用 Vue 的响应式 API。判断标准：如果一个函数在非 Vue 环境下（如 Node.js 测试）也可以正常运行，则属于 utils/。

### 3.3.4 types/ 与 api/ 中类型的边界

types/ 存放全局共享的类型定义（如 DTO、VO、枚举），可被 api/、stores/、components/、pages/ 引用。api/ 目录中仅包含与具体 API 函数直接相关的请求参数和响应类型，不对外导出。如果某个类型被 api/ 以外的文件引用，则必须提取到 types/。

---

# 第四章 技术栈

## 4.1 章节概述

本章说明每个技术选型的决策理由，聚焦于"本项目为什么选择这个技术"。关于技术栈的详细规范定义，参见《Frontend Specification v1.0》第一章。

## 4.2 Vue 3

**选型理由**：

本项目选择 Vue 3，因为 Vue 3 的 Composition API 提供了比 Vue 2 Options API 更强的逻辑复用能力。在三角色、25 个页面的企业级平台中，大量业务逻辑（表单校验、列表筛选、AI 分析交互）需要在不同页面间复用，Composition API 的 Composable 模式天然适合这种场景。Vue 3 的响应式系统基于 Proxy 重写，性能优于 Vue 2 的 Object.defineProperty，对于包含大量 ECharts 图表和 AI 流式数据更新的页面有显著的性能优势。Vue 3 的 TypeScript 支持比 Vue 2 更完善，与项目的 TypeScript 严格模式要求一致。

本项目不选择 React 的理由：Vue 的模板语法更接近 HTML，对于复杂表单和多状态页面的开发效率更高；Pinia 的状态管理比 Redux/Zustand 更简洁，学习成本更低；Element Plus 是 Vue 3 生态中最成熟的企业级 UI 库。

**潜在风险**：Vue 3 的响应式系统重构可能导致部分第三方库不兼容。应对：优先选择明确标注支持 Vue 3 的库，对关键依赖进行预验证。

## 4.3 TypeScript

**选型理由**：

本项目选择 TypeScript，因为项目涉及约 25 个页面、7 个 Store、50+ 组件、10+ 个 API 模块。在如此规模的工程中，没有静态类型检查将导致：API 响应字段拼写错误无法在编译期发现、Store State 结构变更后引用点遗漏更新、跨组件的 Props 传递缺少约束。TypeScript 的 interface 和 type 可以在编译期捕获这些错误。

TypeScript 与后端 Java 类型系统的协作：后端 Java 实体类通过 API Mock Specification 映射为前端 TypeScript 接口，DTO 结构在前后端保持字段名和类型一致性，减少联调阶段的类型不匹配问题。

**严格模式配置**：本项目启用 TypeScript strict 模式，包括 strictNullChecks、noImplicitAny、strictFunctionTypes。禁止使用 any 类型，确需使用时必须添加行内注释说明理由。

## 4.4 Vite

**选型理由**：

本项目选择 Vite，因为 Vite 基于 ES Module 的开发服务器提供了亚秒级的热更新（HMR），对于频繁的组件开发和样式调试场景，Vite 的冷启动和热更新速度显著优于 Webpack。Vite 基于 Rollup 的生产构建支持良好的代码分割和 Tree Shaking，与 Vue 3 的 SFC（单文件组件）有最佳的集成支持。

Vite 在 ARM64/LoongArch 架构下的兼容性需要在 Sprint 1 中提前验证。如发现问题，准备使用 esbuild 替代方案或调整构建配置。

**构建配置要点**：配置 Element Plus 按需引入插件（unplugin-vue-components）、配置路径别名（@ -> src/）、配置开发服务器代理（代理 /api 到后端服务）、配置代码分割策略（按路由分块）。

## 4.5 Pinia

**选型理由**：

本项目选择 Pinia，因为 Pinia 是 Vue 3 官方推荐的状态管理库，相比 Vuex 4 具有以下优势：完全的类型推导（无需手动定义 Action 类型）、模块化设计（每个 Store 是独立的函数调用，不存在嵌套模块概念）、去除了 Mutation（直接通过 Action 修改 State，简化代码）、支持组合式 Store（与 Composition API 风格一致）。

与 Vuex 对比：Pinia 的 setup 语法更接近 Composition API，团队成员无需学习额外的概念；Pinia 的 Store 之间可以直接互相引用（需小心循环依赖），而 Vuex 需要通过 rootGetters 跨模块访问。

**本项目 Store 设计策略**：按功能模块拆分 7 个 Store，而非按页面拆分。仅跨页面共享的数据放入 Store，页面私有的数据通过 Composable 管理。Store 之间避免循环依赖。

## 4.6 Element Plus

**选型理由**：

本项目选择 Element Plus，因为它是 Vue 3 生态中最成熟、文档最完善的企业级 UI 组件库。Element Plus 提供 70+ 组件，覆盖表单、表格、弹窗、导航、反馈等全部 B 端场景。其设计风格与项目 Design System 的"克制的专业主义"哲学一致：组件外观克制、信息密度高、交互标准。

与其他 UI 库对比：Ant Design Vue 的功能完整性不足，部分组件存在 Vue 3 兼容性问题；Naive UI 的生态不够成熟，社区资源少；Arco Design 的更新频率低。

**按需引入策略**：使用 unplugin-vue-components 实现自动按需引入，仅打包实际使用的组件，控制打包体积。不全局注册所有 Element Plus 组件。

**与 Design System 的配合**：通过 CSS 变量覆盖 Element Plus 的默认主题变量，将 Design System 的 Design Token 映射到 Element Plus 组件上。具体映射关系参见《UI Design System v1.0》第 3 章。

## 4.7 Axios

**选型理由**：

本项目选择 Axios，因为它是前端最成熟的 HTTP 客户端库，提供拦截器机制（请求拦截器和响应拦截器天然适配 Token 注入和错误处理）、取消请求（支持 AbortController，用于重复请求取消和页面离开时清理）、超时配置（区分默认超时和特殊接口超时）、进度监听（用于文件上传进度展示）。

**封装策略**：本项目创建单一 Axios 实例（src/api/http.ts），所有 API 模块通过该实例发送请求。不在组件中直接使用 axios。封装层负责：Token 注入、错误码解析、请求重试、超时处理、响应数据解包。

## 4.8 ECharts

**选型理由**：

本项目选择 ECharts，因为项目包含大量图表需求：仪表盘（各角色首页含 4-6 个图表）、教研分析页（趋势图、对比图、雷达图）、报表中心（多种图表类型）。ECharts 提供 30+ 图表类型，覆盖本项目的全部图表需求。ECharts 支持 Canvas 和 SVG 双渲染引擎，大数据量场景下 Canvas 渲染性能优于 SVG。

与其他图表库对比：Chart.js 图表类型不够丰富，缺少雷达图和仪表盘；AntV G2 的学习曲线太陡；Highcharts 为商业授权，不符合项目开源定位。

**图表封装策略**：封装 BaseChart 组件统一处理初始化、Resize 响应、Loading 状态、主题注入。各图表类型（Line、Bar、Pie、Radar、Gauge）继承 BaseChart 或通过 Props 区分类型。

## 4.9 Lucide Icons

**选型理由**：

本项目选择 Lucide Icons 作为主要图标库，因为它提供 1000+ 的 SVG 图标，风格简洁一致，与 Design System 的极简风格匹配。Lucide 支持 Tree Shaking，仅打包实际使用的图标。

与 Element Plus Icons 的分工：Element Plus Icons 用于 Element Plus 组件内部的图标（如 Input 的 prefix-icon、Button 的 icon 属性）；Lucide Icons 用于自定义组件和业务场景中的图标。两者不互相替代。

## 4.10 Mock.js

**选型理由**：

本项目选择 Mock.js 实现 Mock First 开发策略。Mock First 是前端不依赖后端进度的核心保障：后端接口开发周期通常为 6-8 周，前端如果等待后端接口完成再开发，将严重压缩开发和测试时间。Mock First 策略允许前端在 Sprint 1 完成后即开始独立开发所有业务页面。

Mock.js 拦截 XMLHttpRequest 和 Fetch 请求，返回预定义的模拟数据。结合环境变量 VITE_USE_MOCK，开发期开启 Mock，联调期关闭 Mock，无需修改任何业务代码。

**Mock First 策略**：每个模块开发前，先完善对应的 Mock 数据。Mock 数据必须与 API Mock Specification 的数据结构完全一致。Mock 数据覆盖正常数据、空数据、分页数据、错误响应四种场景。

---

# 第五章 路由策略

## 5.1 章节概述

本章定义完整的前端路由实施方案。路由规范的定义参见《Frontend Specification v1.0》第五章，本章聚焦实施层面的具体方案和技术决策。

## 5.2 路由分类

| 路由类型 | 特征 | 是否需要登录 | 示例 |
|---------|------|-------------|------|
| 公开路由 | 无需认证即可访问 | 否 | /login、/404 |
| 认证路由 | 需要有效的 Token | 是 | 所有业务页面 |
| 动态路由 | 登录后根据权限动态添加 | 是 | 各角色的业务页面 |
| 权限路由 | 需要特定角色的权限 | 是 | /admin/* |

## 5.3 路由文件组织

```
router/
├── index.ts              # 创建 Router 实例、注册导航守卫
├── routes/
│   ├── public.ts         # 公开路由（/login、/404）
│   ├── student.ts        # 学生端路由
│   ├── teacher.ts        # 教师端路由
│   └── admin.ts          # 管理员端路由
└── guards/
    ├── auth.ts           # 认证守卫（Token 检查）
    └── permission.ts     # 权限守卫（角色检查）
```

## 5.4 路由配置结构

所有路由配置使用统一的 RouteRecordRaw 格式。路由 meta 字段定义如下：

| meta 字段 | 类型 | 默认值 | 用途 |
|-----------|------|--------|------|
| title | string | '' | 页面标题，用于 document.title 和面包屑 |
| icon | string | '' | 菜单图标，对应 Lucide Icons 图标名 |
| roles | string[] | [] | 允许访问的角色列表，空数组表示所有认证用户可访问 |
| keepAlive | boolean | false | 是否缓存页面组件 |
| hidden | boolean | false | 是否在菜单中隐藏（如详情页、编辑页） |
| sort | number | 0 | 菜单排序权重，数值越小越靠前 |
| activeMenu | string | '' | 高亮的父级菜单路径（用于详情页高亮对应列表菜单项） |

## 5.5 动态路由方案

**为什么选择前端静态路由配置 + 权限过滤，而非后端动态下发路由？**

本项目路由总数约为 35 条，体量适中，前端静态配置路由表 + 登录后按角色过滤的方案足以满足需求。后端动态下发路由的优势在于权限变更无需前端发版，但本项目处于竞赛阶段，权限模型已知且稳定，后端下发路由增加了前后端耦合度和调试复杂度。前端静态配置可以独立于后端开发和调试路由跳转逻辑。

**动态路由添加流程**：

1. 用户登录成功后，User Store 调用 Permission Store 的 fetchPermissions Action
2. Permission Store 从后端获取当前用户的角色和权限列表
3. Permission Store 调用 generateMenus Action，根据角色过滤前端静态路由表
4. 过滤后的路由通过 router.addRoute 逐条添加到 Router 实例
5. 添加完成后，导航守卫放行目标路由

**路由重置方案**：用户退出登录时，调用 router.removeRoute 清除所有动态添加的路由，重置 Permission Store 中的权限数据，跳转到登录页。

## 5.6 导航守卫

Router 的 beforeEach 守卫按以下顺序执行：

**第一步：Token 检查**

从 User Store 获取 Token。Token 不可用：目标路由为公开路由则放行，否则重定向到 /login 并携带 redirect 参数。

**第二步：权限检查**

从 Permission Store 获取权限数据。权限数据为空且 Token 有效：调用 fetchPermissions 获取权限，然后动态添加路由。权限数据获取过程中展示全局 Loading。

**第三步：页面标题设置**

将目标路由的 meta.title 赋值给 document.title，格式为"{页面标题} - 软件实训教学检查评价与报表系统"。

**第四步：加载进度条**

触发 NProgress（或自定义进度条）开始动画，在路由加载完成后结束动画。

## 5.7 菜单生成

菜单数据直接来源于路由配置，不维护独立的菜单配置。路由的 meta 字段（title、icon、roles、hidden、sort）提供生成菜单所需的所有信息。

菜单生成逻辑位于 Permission Store 的 generateMenus Action：
- 过滤 hidden 为 true 的路由（如详情页）
- 过滤当前角色不在 roles 列表中的路由
- 按 sort 字段升序排列
- 通过 children 字段支持多级菜单嵌套

菜单折叠/展开状态存储在 App Store 的 sidebarCollapsed 字段中，持久化到 localStorage。

## 5.8 面包屑

面包屑从路由的 matched 数组自动生成。matched 数组包含从根路由到当前路由的完整层级链路。取每个路由的 meta.title 作为面包屑项的名称。

面包屑项可点击导航（除最后一项，最后一项为当前页面，不可点击）。详情页的面包屑通过 meta.activeMenu 高亮对应的父级菜单项。

## 5.9 页面缓存（KeepAlive）

通过 KeepAlive 组件的 include 属性动态控制缓存列表。缓存列表存储在 App Store 的 cachedPages 数组中。

缓存策略：
- 列表页（如课程列表、任务列表）开启缓存，保留用户筛选条件和滚动位置
- 详情页和编辑页不缓存，每次进入加载最新数据
- Tab 标签页关闭时从缓存列表移除
- 缓存数量上限为 10 个页面，超出时移除最早缓存的页面

## 5.10 Tab 标签页

Tab 标签页的数据结构包含：path（路由路径）、title（页面标题）、query（路由参数）、closable（是否可关闭）。

Tab 行为：
- 点击菜单打开新 Tab（如 Tab 已存在则激活对应 Tab）
- 点击 Tab 关闭按钮关闭 Tab（首页 Tab 不可关闭）
- 关闭 Tab 时自动跳转到相邻 Tab
- Tab 列表与 KeepAlive 缓存列表同步
- Tab 状态存储在 App Store 中，不持久化

---

# 第六章 状态管理

## 6.1 章节概述

本章设计完整的 Pinia Store 体系。Store 规范的定义参见《Frontend Specification v1.0》第六章，本章聚焦每个 Store 的具体设计和实施细节。

## 6.2 Store 拆分原则

- 按功能模块拆分，而非按页面拆分：一个 Store 服务一个业务领域（如实训、复核），而非服务一个特定页面
- 仅跨页面共享的数据放入 Store：页面私有的数据通过 Composable 管理
- Store 之间避免循环依赖：如果 Store A 和 Store B 相互引用，抽取公共逻辑到 Composable 或 Utils

## 6.3 七大 Store 详细设计

### 6.3.1 User Store（useUserStore）

**文件**：src/stores/useUserStore.ts

**职责**：管理用户信息、登录状态、Token。不管理权限数据（由 Permission Store 负责）。不管理 UI 状态（由 App Store 负责）。

**State**：

| 字段 | 类型 | 说明 |
|------|------|------|
| token | string \| null | JWT Access Token |
| refreshToken | string \| null | JWT Refresh Token |
| userInfo | IUserInfo \| null | 用户基本信息（id、name、role、avatar） |
| isLoggedIn | boolean | 是否已登录（派生自 token） |

**Getter**：

| 名称 | 返回值 | 说明 |
|------|--------|------|
| isLoggedIn | boolean | token 不为 null 且未过期 |
| userRole | string | 当前用户的角色名 |
| userName | string | 当前用户的显示名 |

**Action**：

| 名称 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| login | { username, password } | Promise\<void\> | 登录，存储 Token，获取用户信息 |
| logout | 无 | void | 退出登录，清除 Token 和用户信息 |
| refreshAccessToken | 无 | Promise\<void\> | 使用 Refresh Token 刷新 Access Token |
| fetchUserInfo | 无 | Promise\<void\> | 获取当前用户信息 |
| updateProfile | Partial\<IUserInfo\> | Promise\<void\> | 更新用户个人信息 |

**持久化策略**：Token 仅存在内存中（Pinia State），不写入 localStorage 或 sessionStorage。刷新页面后 Token 丢失，需通过 Refresh Token 恢复或重新登录。userInfo 同样仅在内存中。

**设计理由**：Token 存入 localStorage 存在 XSS 攻击风险（恶意脚本可读取 localStorage），存入内存虽刷新丢失但安全性更高。参赛项目需要体现安全意识。

### 6.3.2 Permission Store（usePermissionStore）

**文件**：src/stores/usePermissionStore.ts

**职责**：管理角色、权限列表、菜单列表。不管理用户信息（由 User Store 负责）。

**State**：

| 字段 | 类型 | 说明 |
|------|------|------|
| role | string \| null | 当前角色（student / teacher / admin） |
| permissions | string[] | 权限标识列表 |
| menus | IMenuItem[] | 根据角色过滤后的菜单列表 |
| isPermissionLoaded | boolean | 权限是否已加载 |

**Getter**：

| 名称 | 返回值 | 说明 |
|------|--------|------|
| hasPermission | (perm: string) => boolean | 检查是否拥有指定权限 |

**Action**：

| 名称 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| fetchPermissions | 无 | Promise\<void\> | 从后端获取角色和权限列表 |
| generateMenus | 无 | void | 根据角色和权限生成菜单列表 |
| hasRole | (role: string) => boolean | 检查是否为指定角色 |
| resetPermissions | 无 | void | 退出登录时重置权限数据 |

**持久化策略**：不持久化。刷新页面后重新从后端获取权限数据，确保权限变更实时生效。

### 6.3.3 App Store（useAppStore）

**文件**：src/stores/useAppStore.ts

**职责**：管理全局 UI 状态。不管理业务数据。

**State**：

| 字段 | 类型 | 说明 |
|------|------|------|
| sidebarCollapsed | boolean | 侧边栏是否折叠 |
| currentTheme | string | 当前主题（light，预留 dark） |
| globalLoading | boolean | 全局 Loading 状态 |
| pageTitle | string | 当前页面标题 |
| breadcrumbs | IBreadcrumb[] | 当前页面面包屑 |
| cachedPages | string[] | KeepAlive 缓存的页面组件名列表 |
| openedTabs | ITab[] | 已打开的 Tab 标签页列表 |

**Action**：

| 名称 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| toggleSidebar | 无 | void | 切换侧边栏折叠状态 |
| setTheme | (theme: string) | void | 设置主题 |
| setGlobalLoading | (loading: boolean) | void | 设置全局 Loading |
| setPageTitle | (title: string) | void | 设置页面标题 |
| openTab | (tab: ITab) | void | 打开 Tab 标签页 |
| closeTab | (path: string) | void | 关闭 Tab 标签页 |
| closeAllTabs | 无 | void | 关闭所有 Tab |
| addCachedPage | (name: string) | void | 添加缓存页面 |
| removeCachedPage | (name: string) | void | 移除缓存页面 |

**持久化策略**：sidebarCollapsed 和 currentTheme 持久化到 localStorage。其他字段不持久化。

### 6.3.4 Training Store（useTrainingStore）

**文件**：src/stores/useTrainingStore.ts

**职责**：管理实训任务相关的全局数据。不管理提交数据（由 Review Store 部分覆盖）。不管理课程数据（轻量数据通过 Composable 管理）。

**State**：

| 字段 | 类型 | 说明 |
|------|------|------|
| taskList | ITrainingTask[] | 实训任务列表 |
| currentTask | ITrainingTask \| null | 当前选中的任务详情 |
| filters | ITaskFilters | 筛选条件 |
| pagination | IPagination | 分页信息 |
| isLoading | boolean | 加载状态 |
| lastFetchTime | number \| null | 上次获取任务列表的时间戳 |

**Action**：

| 名称 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| fetchTasks | (filters?: ITaskFilters) | Promise\<void\> | 获取任务列表（带缓存） |
| fetchTaskDetail | (taskId: string) | Promise\<void\> | 获取任务详情 |
| createTask | (data: ICreateTaskDTO) | Promise\<void\> | 创建任务 |
| updateTask | (taskId: string, data: IUpdateTaskDTO) | Promise\<void\> | 更新任务 |
| deleteTask | (taskId: string) | Promise\<void\> | 删除任务 |
| setFilters | (filters: ITaskFilters) | void | 更新筛选条件 |
| invalidateCache | 无 | void | 清除缓存，强制下次刷新 |

**缓存策略**：taskList 缓存 5 分钟。5 分钟内重复请求直接返回缓存数据，超过 5 分钟后重新请求。currentTask（任务详情）不缓存，每次进入详情页都请求最新数据。

### 6.3.5 Review Store（useReviewStore）

**文件**：src/stores/useReviewStore.ts

**职责**：管理教师复核相关的全局数据。这是数据实时性要求最高的 Store，所有数据均不缓存。

**State**：

| 字段 | 类型 | 说明 |
|------|------|------|
| reviewList | IReviewItem[] | 待复核列表 |
| currentReview | IReviewDetail \| null | 当前复核详情 |
| aiScores | IAIScore[] | AI 评分数据 |
| teacherScores | ITeacherScore[] | 教师评分数据 |
| reviewStatus | 'idle' \| 'loading' \| 'submitting' \| 'error' | 复核操作状态 |

**Action**：

| 名称 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| fetchReviewList | (filters?: IReviewFilters) | Promise\<void\> | 获取复核列表 |
| fetchReviewDetail | (reviewId: string) | Promise\<void\> | 获取复核详情（含 AI 评分） |
| submitReview | (reviewId: string, data: IReviewSubmitDTO) | Promise\<void\> | 提交复核结果 |
| batchReview | (reviewIds: string[], data: IReviewSubmitDTO) | Promise\<void\> | 批量复核 |
| updateTeacherScore | (scoreId: string, score: number) | void | 更新单个评分项的教师评分 |
| addComment | (scoreId: string, comment: string) | void | 添加评语 |

**缓存策略**：不缓存。复核数据涉及教师对 AI 评分的修改和覆盖，数据实时性要求极高。每次进入复核页面都请求最新数据。

### 6.3.6 Dashboard Store（useDashboardStore）

**文件**：src/stores/useDashboardStore.ts

**职责**：管理各角色仪表盘的统计数据。不同角色的 Dashboard 展示不同维度的数据。

**State**：

| 字段 | 类型 | 说明 |
|------|------|------|
| statsData | IDashboardStats \| null | 统计卡片数据 |
| chartData | IDashboardChartData \| null | 图表数据 |
| recentActivities | IActivity[] | 最近动态列表 |
| lastFetchTime | number \| null | 上次获取数据的时间戳 |

**Action**：

| 名称 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| fetchStats | 无 | Promise\<void\> | 获取统计数据 |
| fetchChartData | (period: string) | Promise\<void\> | 获取图表数据（支持时间范围切换） |
| fetchActivities | 无 | Promise\<void\> | 获取最近动态 |
| refreshDashboard | 无 | Promise\<void\> | 刷新全部 Dashboard 数据 |

**缓存策略**：缓存 10 分钟。Dashboard 数据更新频率低，10 分钟内的重复访问使用缓存。提供手动刷新按钮供用户主动更新。

### 6.3.7 Report Store（useReportStore）

**文件**：src/stores/useReportStore.ts

**职责**：管理报表数据和导出状态。报表数据实时生成，不做缓存。

**State**：

| 字段 | 类型 | 说明 |
|------|------|------|
| reportList | IReport[] | 报表列表 |
| currentReport | IReportDetail \| null | 当前报表详情 |
| exportStatus | 'idle' \| 'generating' \| 'downloading' \| 'error' | 导出状态 |
| exportProgress | number | 导出进度百分比 |

**Action**：

| 名称 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| fetchReports | (filters?: IReportFilters) | Promise\<void\> | 获取报表列表 |
| generateReport | (params: IReportGenerateDTO) | Promise\<void\> | 生成报表 |
| exportPDF | (reportId: string) | Promise\<void\> | 导出 PDF |
| exportExcel | (reportId: string) | Promise\<void\> | 导出 Excel |
| fetchReportDetail | (reportId: string) | Promise\<void\> | 获取报表详情 |

**缓存策略**：不缓存。报表数据需要实时生成，不应返回过期数据。

## 6.4 全局状态管理

### 6.4.1 Loading 状态

- 全局 Loading：由 App Store 的 globalLoading 字段控制，用于页面级数据加载（如仪表盘首次加载），通过顶部进度条展示
- 局部 Loading：由各业务 Store 的 isLoading 字段控制，用于列表加载、表单提交等局部操作，通过骨架屏或组件 Loading 展示
- 全局 Loading 和局部 Loading 互斥：同一时刻只显示一种 Loading，优先显示局部 Loading

### 6.4.2 Theme 状态

- 当前仅支持亮色主题（light），由 App Store 的 currentTheme 字段管理
- 预留暗色主题扩展点：CSS 变量已按亮暗主题分离命名（如 --color-bg-primary、--color-bg-primary-dark），切换 Theme 时仅需替换 CSS 变量值
- 主题配置持久化到 localStorage

### 6.4.3 错误状态

- 全局错误捕获：Vue 的 errorHandler + window.onerror 全局捕获未处理错误
- 全局错误展示：通过 Element Plus 的 ElMessage 或 ElNotification 展示错误信息
- 错误不阻断用户体验：全局错误不弹出 Modal 阻断操作，使用可自动消失的提示

---

# 第七章 API 集成策略

## 7.1 章节概述

本章定义 HTTP 请求层的完整实施方案。API 集成的规范定义参见《Frontend Specification v1.0》第八章，Mock 规范参见第九章。本章聚焦实施层面的技术决策和配置方案。

## 7.2 Axios 实例配置

本项目使用单一 Axios 实例，创建于 src/api/http.ts。配置如下：

| 配置项 | 值 | 说明 |
|--------|-----|------|
| baseURL | 环境变量 VITE_API_BASE_URL | 开发环境指向开发服务器，生产环境指向实际 API 地址 |
| timeout | 30000（30s） | 默认超时时间，特殊接口可覆盖 |
| headers | { 'Content-Type': 'application/json' } | 默认请求头 |
| withCredentials | false | 不使用 Cookie 方式传递 Token |

不使用多实例策略的原因：本项目后端为单体服务，不存在多后端服务场景。单一实例足以满足需求，多实例增加维护复杂度。

## 7.3 请求拦截器

请求拦截器按以下顺序执行：

1. **Token 注入**：从 User Store 获取 Token，添加到 Authorization 请求头（Bearer 格式）。如 Token 不存在且目标接口非公开接口，跳过 Token 注入
2. **请求 ID 生成**：为每个请求生成唯一 Request ID（UUID v4），添加到 X-Request-ID 请求头，用于链路追踪和错误定位
3. **时间戳注入**：为 GET 请求添加 _t 参数（当前时间戳），防止浏览器缓存 GET 请求结果
4. **特殊请求头处理**：文件上传接口设置 Content-Type 为 multipart/form-data（由浏览器自动设置，不手动覆盖）；SSE 接口设置 Accept 为 text/event-stream

## 7.4 响应拦截器

响应拦截器按以下逻辑处理：

**成功响应（HTTP 2xx）**：
- 从 axios response 中提取 data 字段
- 检查业务 code 字段：code 为 0 或 200 表示成功，直接返回 data；code 非成功值表示业务错误，触发业务错误处理

**HTTP 错误（非 2xx）**：

| HTTP 状态码 | 含义 | 处理方式 |
|------------|------|---------|
| 400 | 请求参数错误 | 提取后端返回的错误信息，以 Message 形式提示用户 |
| 401 | 未认证 | 尝试 Refresh Token 刷新，刷新失败则跳转登录页 |
| 403 | 无权限 | 展示"无权限访问"提示页面 |
| 404 | 资源不存在 | 展示 404 页面或"资源不存在"提示 |
| 500 | 服务端异常 | 展示"服务异常，请稍后重试"提示，提供重试按钮 |
| 502/503 | 服务不可用 | 展示"服务暂时不可用"提示，提供重试按钮 |

**网络错误**：
- 超时：展示"请求超时，请检查网络后重试"，提供重试按钮
- 断网：展示"网络连接已断开"提示，监听网络恢复后自动重试
- DNS 解析失败：展示"无法连接到服务器"提示

## 7.5 Token 管理

**Token 存储位置**：Token 存储在 Pinia User Store 中（内存），不写入 localStorage。此决策的原因：localStorage 可被同源 JavaScript 脚本读取，存在 XSS 攻击风险。Token 存于内存虽在刷新页面后丢失，但安全性更高。

**Refresh Token 刷新机制**：
- 定时刷新：在 Access Token 过期前 5 分钟，自动调用 refreshAccessToken 接口获取新 Token
- 拦截器触发：当请求返回 401 时，尝试使用 Refresh Token 刷新 Access Token，刷新成功后重放原请求
- 并发请求优化：多个请求同时触发刷新时，共享同一个刷新 Promise，防止重复刷新

**Refresh Token 过期处理**：Refresh Token 过期或刷新失败时，清除 User Store 和 Permission Store 数据，跳转登录页，并在登录页展示"登录已过期，请重新登录"提示。

## 7.6 请求重试

| 重试场景 | 重试次数 | 重试间隔 | 说明 |
|---------|---------|---------|------|
| 网络超时 | 3 次 | 指数退避（1s、2s、4s） | 网络短暂波动后可恢复 |
| 5xx 服务端错误 | 3 次 | 指数退避（1s、2s、4s） | 服务短暂不可用后可恢复 |
| 4xx 客户端错误 | 不重试 | 无 | 请求本身有问题，重试无意义 |
| 业务错误 | 不重试 | 无 | 业务逻辑错误，重试无意义 |
| AI 接口错误 | 2 次 | 固定间隔（3s） | AI 服务可能负载高，给恢复时间 |

## 7.7 请求超时

| 接口类型 | 超时时间 | 理由 |
|---------|---------|------|
| 默认 | 30s | 常规 CRUD 接口应在 30 秒内完成 |
| AI 分析接口 | 120s | AI 分析耗时较长，需要更长的等待时间 |
| 文件导出接口 | 60s | PDF/Excel 生成耗时较长 |
| 文件上传接口 | 120s | 大文件上传需要更长超时时间 |
| SSE 连接 | 300s | SSE 为长连接，超时时间需足够长 |

超时后处理：展示"请求超时"提示，提供重试按钮。不自动重试超时请求（重试可能导致重复提交）。

## 7.8 Mock 切换机制

通过环境变量 VITE_USE_MOCK 控制 Mock 开关：

- 开发环境（.env.development）：VITE_USE_MOCK=true —— 默认启用 Mock
- 生产环境（.env.production）：VITE_USE_MOCK=false —— 强制禁用 Mock
- Mock 环境（.env.mock）：VITE_USE_MOCK=true —— 用于独立 Mock 测试

Mock 切换为无侵入式：开启 Mock 时，Mock.js 拦截 XMLHttpRequest 请求并返回 Mock 数据，业务代码无需任何修改。关闭 Mock 时，请求正常发送到后端，Mock 模块不被加载到打包产物中。

Mock 模式下的混合请求：通过配置 Mock 白名单，允许特定接口绕过 Mock 发送真实请求（如调试阶段的真实后端接口）。白名单通过 VITE_MOCK_WHITELIST 环境变量配置。

## 7.9 API 层组织

API 层按功能模块组织，每个模块导出函数：

| 文件 | 对应功能 | 主要接口 |
|------|---------|---------|
| api/auth.ts | 认证 | login / logout / refreshToken |
| api/course.ts | 课程 | CRUD / 学生列表 |
| api/training.ts | 实训任务 | CRUD / 发布 / 归档 |
| api/submission.ts | 学生提交 | 提交 / 列表 / 详情 |
| api/review.ts | 教师复核 | 复核列表 / 提交复核 / 批量复核 |
| api/dashboard.ts | 仪表盘 | 统计数据 / 图表数据 / 动态 |
| api/report.ts | 报表 | 生成 / 预览 / 导出 |
| api/user.ts | 用户管理 | CRUD / 角色分配 |
| api/system.ts | 系统配置 | LLM 配置 / 系统参数 / 监控 |
| api/knowledge.ts | 知识库 | CRUD / 搜索 |
| api/ai.ts | AI | 发起分析 / SSE 连接 / 结果查询 |

每个 API 模块的请求参数和响应数据类型引用 types/ 目录中对应的 TypeScript 类型定义。

## 7.10 环境变量

| 变量名 | 用途 | 开发环境值 | 生产环境值 |
|--------|------|-----------|-----------|
| VITE_API_BASE_URL | API 基础路径 | http://localhost:8080/api/v1 | /api/v1 |
| VITE_USE_MOCK | 是否启用 Mock | true | false |
| VITE_MOCK_DELAY | Mock 响应延迟（ms） | 200 | （不适用） |
| VITE_APP_TITLE | 应用标题 | 软件实训教学检查评价与报表系统（Dev） | 软件实训教学检查评价与报表系统 |
| VITE_SSE_TIMEOUT | SSE 连接超时（ms） | 300000 | 300000 |
| VITE_ENABLE_LOG | 是否启用前端日志 | true | false |

---

# 第八章 权限策略

## 8.1 章节概述

本章定义基于 RBAC 的前端权限控制实施方案。权限规范定义参见《Frontend Specification v1.0》第十三章，本章聚焦实施层面的具体方案。

## 8.2 RBAC 模型

本项目采用 RBAC（Role-Based Access Control）模型，定义三个角色：

| 角色 | 标识 | 权限范围 |
|------|------|---------|
| 学生 | student | 查看课程、提交实训、查看个人成绩 |
| 教师 | teacher | 管理课程和实训、查看提交、AI 分析、复核评分、管理成绩、评分标准管理、教研分析、报表中心、知识库 |
| 管理员 | admin | 用户管理、系统配置、日志、系统监控 |

每个角色拥有一组权限标识。权限标识为字符串，格式为 `module:action`（如 `course:create`、`review:submit`）。

## 8.3 三种角色权限矩阵

| 功能模块 | 权限标识 | 学生 | 教师 | 管理员 |
|---------|---------|------|------|--------|
| 查看个人课程 | course:view:own | 有 | 有 | 有 |
| 创建课程 | course:create | - | 有 | 有 |
| 编辑课程 | course:update | - | 有 | 有 |
| 删除课程 | course:delete | - | 有 | 有 |
| 查看个人实训任务 | training:view:own | 有 | 有 | 有 |
| 创建实训任务 | training:create | - | 有 | 有 |
| 编辑实训任务 | training:update | - | 有 | 有 |
| 删除实训任务 | training:delete | - | 有 | 有 |
| 提交代码 | submission:create | 有 | - | - |
| 查看个人提交 | submission:view:own | 有 | 有 | 有 |
| 查看全部提交 | submission:view:all | - | 有 | 有 |
| 发起 AI 分析 | ai:analyze | - | 有 | 有 |
| 查看 AI 分析结果 | ai:view | 有（仅自己） | 有（全班+全局） | 有（全局） |
| 教师复核 | review:submit | - | 有 | - |
| 查看复核结果 | review:view | 有（仅自己） | 有（全班+全局） | 有（全局） |
| 管理评分标准 | scoring:manage | - | 有 | 有 |
| 查看教研分析 | analysis:view | - | 有 | 有 |
| 生成报表 | report:generate | - | 有 | 有 |
| 导出报表 | report:export | - | 有 | 有 |
| 管理知识库 | knowledge:manage | - | 有 | 有 |
| 用户管理 | user:manage | - | - | 有 |
| 班级管理 | class:manage | - | - | 有 |
| 系统配置 | system:config | - | - | 有 |
| 查看操作日志 | log:view | - | - | 有 |
| 文件管理 | file:manage | - | - | 有 |
| 系统监控 | system:monitor | - | - | 有 |

## 8.4 路由权限

**实现方案**：前端静态配置完整路由表，登录后根据后端返回的角色和权限列表过滤路由，通过 router.addRoute 逐条添加有权访问的路由。

**无权限路由处理**：用户在地址栏手动输入无权限的 URL 时，导航守卫检测到路由未注册（或用户角色不在路由 meta.roles 列表中），重定向到 403 页面或上一页面并提示"无权限访问"。

**路由权限检查时机**：
- 导航守卫 beforeEach 中检查（每次路由跳转前）
- 动态路由添加前检查（登录后仅添加有权访问的路由）

## 8.5 菜单权限

菜单基于路由配置动态生成。生成逻辑：
- 遍历所有路由，过滤 meta.hidden 为 true 的路由
- 检查当前用户角色是否在路由 meta.roles 列表中
- 按 meta.sort 排序
- 输出菜单树

菜单权限数据来源于 Permission Store 的 menus 字段。菜单项不展示用户无权访问的页面，而非展示为灰色禁用状态。

## 8.6 按钮权限

**v-permission 指令实现方案**：自定义 Vue 指令 v-permission，接收权限标识字符串。指令的 mounted 钩子中从 Permission Store 获取 hasPermission 函数，检查当前用户是否拥有指定权限。无权限时移除该 DOM 元素（使用 el.remove()），而非隐藏（display: none）。

**权限检查函数**：Permission Store 的 hasPermission 函数，接收权限标识字符串，返回布尔值。函数内部检查 permissions 数组中是否包含指定权限标识。

**按钮权限策略**：按钮无权限时采用"隐藏"策略（而非"禁用"策略）。理由：禁用的按钮会引发用户疑惑（为什么我不能点？），而隐藏按钮直接减少视觉噪音。

## 8.7 页面权限

页面级权限通过路由守卫 + 动态路由双重保障：
- 动态路由控制：无权页面不会被注册到 Router
- 路由守卫控制：beforeEach 中检查目标路由的 meta.roles

页面内部的权限控制通过 v-permission 指令 + hasPermission 函数实现：
- 局部内容（如"导出报表"按钮）：通过 v-permission 指令控制显示
- 条件逻辑（如"显示 AI 分析入口"）：通过 hasPermission 函数判断

## 8.8 权限数据管理

**权限获取时机**：用户登录成功后，在导航守卫 beforeEach 中检查 Permission Store 的 isPermissionLoaded，如未加载则调用 fetchPermissions。

**权限存储位置**：Permission Store（Pinia State 内存中），不持久化。

**权限刷新策略**：页面刷新后 isPermissionLoaded 重置为 false，导航守卫自动重新获取权限数据。权限不会过期，仅在退出登录时重置。

**权限变更处理**：如果用户权限被管理员修改（如降级），前端无法实时感知（Token 仍有效）。解决方案：在关键操作（如提交复核）的后端接口中进行最终权限校验，后端返回 403 时前端展示"权限已变更，请刷新页面"提示。

**为什么前端权限不能作为最终安全控制**：前端权限仅做 UI 控制，提升用户体验，不具备安全性。恶意用户可通过浏览器 DevTools 修改 JavaScript 代码绕过前端权限检查。真正的安全控制必须在后端接口层面实现。前端权限的作用是减少用户看到不可操作的元素，而非保护数据安全。

---

# 第九章 组件开发策略

## 9.1 章节概述

本章定义组件开发的分层策略、开发优先级和组织方案。组件的完整清单和 Props 定义参见《Component Library v1.0》，本章聚焦实施层面的开发策略和优先级。

## 9.2 原子设计在本项目的应用

本项目采用简化版原子设计模型，五层对应关系：

| 原子层级 | 本项目映射 | 举例 |
|---------|-----------|------|
| Atoms 原子 | Base 基础组件 | BaseButton、BaseInput、BaseModal |
| Molecules 分子 | 基础组件组合 + Chart 组件 | BaseSearchBar、LineChart |
| Organisms 有机体 | Business 业务组件 + AI 组件 | TaskCard、AIScorePanel、ReviewTimeline |
| Templates 模板 | Layout 布局组件 | AppLayout、PageContainer |
| Pages 页面 | pages/ 目录下的页面组件 | StudentDashboardPage、ReviewPage |

## 9.3 组件分层策略

### 9.3.1 Base 基础组件

**定义**：对 Element Plus 组件进行二次封装，统一项目风格和默认行为。

**目的**：
- 统一应用 Design System 的 Design Token（颜色、圆角、间距）
- 减少每个页面中传递相同 Props 的重复代码
- 隔离 Element Plus 版本升级的影响
- 提供统一的默认行为（如所有按钮点击后自动防抖）

**开发规则**：
- Props 命名与 Element Plus 原生组件保持一致，便于团队切换
- 不支持的功能通过 v-bind="$attrs" 透传到 Element Plus 组件
- 每个 Base 组件导出与 Element Plus 原生组件相同的插槽
- 不修改 Element Plus 组件的默认事件行为

**首批开发列表（Sprint 1）**：BaseButton、BaseInput、BaseModal、BaseTable、BaseForm、BaseSelect、BaseDatePicker、BaseUpload、BasePagination

**数量预估**：15-20 个

### 9.3.2 Business 业务组件

**定义**：包含业务逻辑、可在多个页面复用的组件。

**目的**：
- 避免在多个页面中重复实现相同的业务 UI 模式
- 统一业务数据的展示格式和交互方式
- 业务需求变更时只需修改一个组件

**开发规则**：
- 自包含业务逻辑：组件内部调用 Store 或 API 获取数据
- 通过 Props 接收配置参数（如数据 ID、显示模式）
- 通过 Emits 向外输出事件（如提交成功、操作取消）
- 严格禁止引用 pages/ 目录下的任何文件
- 支持 Loading / Empty / Error 三种状态

**典型组件及所属 Sprint**：

| 组件 | 首次使用页面 | 所属 Sprint |
|------|-------------|------------|
| CourseCard | 课程列表 | Sprint 2 |
| TaskStatusBadge | 实训任务列表 | Sprint 2 |
| FileUploader | 代码提交页 | Sprint 2 |
| AIScorePanel | AI 分析结果页 | Sprint 3 |
| ReviewTimeline | 教师复核页 | Sprint 3 |
| ScoreDetailTable | 成绩管理页 | Sprint 3 |
| SubmissionPreview | 学生提交详情页 | Sprint 3 |
| UserSelector | 用户管理页 | Sprint 5 |
| ConfirmDialog | 全局 | Sprint 1 |

**数量预估**：20-30 个

### 9.3.3 Layout 布局组件

**定义**：控制页面整体布局结构的组件。

**目的**：
- 统一三角色的页面框架（Navbar + Sidebar + Content + Footer）
- 响应式适配不同屏幕尺寸
- 提供统一的页面标题、面包屑渲染

**开发列表**：

| 组件 | 职责 |
|------|------|
| AppLayout | 全局布局容器，组合 Navbar + Sidebar + 内容区 |
| Navbar | 顶部导航栏（Logo、用户头像、通知图标） |
| Sidebar | 侧边栏菜单（根据角色动态渲染菜单项） |
| PageHeader | 页面标题栏（标题 + 面包屑 + 操作按钮区） |
| PageContainer | 页面内容容器（统一内边距和最大宽度） |

**数量预估**：5 个

### 9.3.4 Chart 图表组件

**定义**：对 ECharts 的封装组件，提供统一的初始化、Resize、主题注入。

**开发规则**：
- 所有图表组件基于 BaseChart 实现，BaseChart 负责 ECharts 实例创建、销毁、Resize 处理
- 图表颜色通过 Design System 的 Design Token 注入，不使用 ECharts 默认颜色
- 所有图表组件支持 Loading 状态（图表区域骨架屏）和 Empty 状态（无数据提示）

**开发列表**：BaseChart（基础封装）、LineChart（折线图）、BarChart（柱状图）、PieChart（饼图）、RadarChart（雷达图）、GaugeChart（仪表盘）

**数量预估**：6 个

### 9.3.5 AI 专用组件

**定义**：与大模型分析交互相关的专用组件。

**开发规则**：
- AI 组件必须体现"AI 建议 -> 人类决策"的协作交互模式
- AI 分析结果使用区别于用户输入的视觉样式（通过 AI 主题色标识）
- AI 结果必须经过人工"确认"操作后才能生效

**开发列表**：

| 组件 | 职责 |
|------|------|
| AIAnalysisProgress | AI 分析进度展示（阶段名称 + 进度条 + 已耗时） |
| AIResultCard | AI 分析结果卡片（置信度、推理过程、建议评分） |
| AIScoreDisplay | AI 评分的可视化展示（数值 + 颜色指示 + 评分维度分解） |
| AIConfirmPanel | AI 结果确认面板（确认 / 驳回 / 修改评分） |
| StreamingTextDisplay | SSE 流式文本实时渲染（打字机效果） |
| AITokenUsage | Token 用量展示（当前消耗 / 总量 / 百分比） |
| AIErrorRetry | AI 异常重试面板（错误原因 + 重试按钮 + 手动输入备选） |

**数量预估**：7 个

## 9.4 组件开发优先级

| 阶段 | Sprint | 组件类型 | 理由 |
|------|--------|---------|------|
| Phase 1 | Sprint 1 | Base 组件 + Layout 组件 + Common 组件 | 基础设施，所有页面依赖 |
| Phase 2 | Sprint 2 | Chart 组件 + 部分 Business 组件 | 学生端页面需要 |
| Phase 3 | Sprint 3 | AI 组件 + 核心 Business 组件 | 教师端核心交互依赖 |
| Phase 4 | Sprint 4 | 剩余 Business 组件 | 按页面需求增量开发 |

## 9.5 组件命名规范

| 实体 | 命名格式 | 示例 |
|------|---------|------|
| 组件文件名 | PascalCase.vue | UserSelector.vue |
| 组件名 | 与文件名一致 | UserSelector |
| 组件目录名 | kebab-case | user-selector/ |
| Base 组件 | Base 前缀 | BaseButton.vue |
| Business 组件 | 功能名 + 类型后缀 | UserSelector.vue、TaskStatusBadge.vue |
| Layout 组件 | 无特殊前缀 | AppLayout.vue、Navbar.vue |
| Chart 组件 | 图表类型 + Chart 后缀 | LineChart.vue、PieChart.vue |
| AI 组件 | AI 前缀 | AIAnalysisProgress.vue、AIResultCard.vue |
| Common 组件 | 状态 + State 后缀 | LoadingState.vue、ErrorState.vue |
| 页面组件 | 功能名 + Page 后缀 | DashboardPage.vue |

---

# 第十章 页面开发工作流

## 10.1 章节概述

本章定义页面开发的标准化七步流程，确保每个页面的开发质量一致。页面开发规范参见《Frontend Specification v1.0》第四章，本章聚焦工作流程和检查机制。

## 10.2 页面开发七步法

### 第 1 步：需求分析（0.5 人天）

**输入**：PRD 对应章节、SDS 对应接口设计、UI Design System 对应页面原型

**活动**：
- 阅读 PRD，提取页面的用户故事和验收标准
- 阅读 SDS，确定页面依赖的后端接口和数据字段
- 对照 UI Design System 的页面原型，确认布局结构和交互细节
- 确认页面对应的角色权限
- 列出页面的数据输入和输出

**输出**：页面需求 Checklist（包含所有必须实现的用户故事和交互）

### 第 2 步：组件拆分（0.5 人天）

**输入**：页面需求 Checklist、Component Library 组件清单

**活动**：
- 将页面 UI 拆分为组件树（根节点为页面，叶节点为 Base 组件）
- 检查 Component Library，标记已有可复用组件
- 标记需要新建的 Business 组件
- 确定每个组件的 Props 接口和 Emits 事件

**输出**：组件拆分方案（组件树 + 新组件开发清单 + 组件 Props/Emits 定义草案）

### 第 3 步：Mock 数据集成（1 人天）

**输入**：API Mock Specification、组件拆分方案

**活动**：
- 列出页面需要的所有 API 接口
- 检查 API Mock Specification，确认 Mock 接口已定义
- 补充缺失的 Mock 数据和接口
- 配置 Mock 数据的四种覆盖场景：正常数据、空数据、错误响应、分页数据
- 配置 Mock 延迟（正常 200ms、慢网络 2000ms）

**输出**：可用的 Mock 接口，覆盖所有数据场景

### 第 4 步：页面编码（2-3 人天）

**输入**：组件拆分方案、Mock 数据

**活动**：
- 实现本页需要新建的 Business 组件（如有）
- 实现页面组件（编排子组件、对接 Store、处理路由参数）
- 对接 API 层（通过 Store Action 调用）
- 处理所有状态：Loading（骨架屏）、Empty（空状态提示）、Error（错误提示 + 重试）、Success（正常展示）
- 实现权限控制（v-permission 指令、hasPermission 检查）
- 实现表单校验和防重复提交

**输出**：功能完整的页面（所有状态已覆盖、Mock 数据可运行）

### 第 5 步：测试（1 人天）

**输入**：功能完整的页面

**活动**：
- 测试所有用户交互流程（正常流程 + 异常流程）
- 测试所有 UI 状态切换（Loading -> Error / Empty / Success）
- 测试权限控制（无权角色是否看到正确降级内容）
- 测试数据边界（空数据、超长文本、特殊字符）
- 测试响应式布局（1920px / 1366px / 1024px）
- 测试浏览器兼容性（Chrome、Edge）

**输出**：自测通过的页面 + 测试记录（记录已测场景和发现的问题）

### 第 6 步：Review（0.5 人天）

**输入**：自测通过的页面

**活动**：
- 代码 Review：检查是否遵循 Frontend Specification 规范
- UI Review：检查是否遵循 UI Design System（颜色、间距、圆角、阴影）
- 组件 Review：检查是否复用了 Component Library 中的组件
- 性能 Review：检查首屏加载时间、页面切换时间
- 状态 Review：检查 Loading / Empty / Error 状态是否全部实现

**输出**：Review 通过标记 + Review 记录（记录发现的问题和修复状态）

### 第 7 步：归档（纳入 Sprint 收尾）

**输入**：Review 通过的页面

**活动**：
- 更新页面开发状态（Sprint 进度表）
- 如有新增组件，更新 Component Library 清单
- 如有新增或修改的 Mock 接口，同步更新 API Mock Specification
- 提交代码并关联对应的任务编号

**输出**：开发状态更新、代码已提交

## 10.3 页面复杂度分级

| 复杂度 | 特征 | 标准开发周期 | 典型页面 |
|--------|------|------------|---------|
| L1 简单 | 纯展示为主，无表单，无复杂交互，1-2 个子组件 | 2 人天 | 个人中心、404 页面、操作日志 |
| L2 中等 | 包含表单或表格，有基础增删改查交互，3-5 个子组件 | 4 人天 | 课程列表、实训任务列表、班级管理 |
| L3 复杂 | 多组件编排，含图表，复杂状态管理，6-10 个子组件 | 6 人天 | Dashboard、AI 分析页、任务管理页 |
| L4 极复杂 | 流式数据，实时交互，复杂业务逻辑，10+ 子组件 | 8 人天 | 教师复核页、教研分析页、报表中心 |

## 10.4 页面开发前置条件检查清单

每个页面开始编码前，必须确认以下四项：

1. 对应 UI 原型已评审通过（参见《UI Design System v1.0》第 19 章页面模板）
2. 对应 Mock 接口已在 API Mock Specification 中定义且覆盖了四种数据场景（正常/空/错误/分页）
3. 依赖的 Base 组件和 Layout 组件已完成开发（Component Library Phase 1）
4. 依赖的 Store 已定义 State 和 Action 接口签名

四项前置条件中任何一项不满足，不得开始页面编码。替代方案：先完成缺失的前置条件。

## 10.5 页面开发与前后端协作约定

- 页面开发期间使用 Mock 数据，不依赖后端接口的实际可用性
- 页面提交 Review 前，Mock 数据必须与最新 API Mock Specification 保持一致
- Sprint 6 联调阶段，关闭 Mock，逐个接口验证前后端对接
- 联调发现的接口不一致问题，优先以 API Mock Specification 为准，除非后端有不可兼容的变更理由

---

# 第十一章 Sprint 计划

## 11.1 章节概述

本章制定 7 个 Sprint 的详细实施计划。每个 Sprint 包含明确的目标、任务分解、人天估算和交付物定义。Sprint 周期为 2 周（10 个工作日），总开发周期为 14 周，前端开发人力为 2-3 人。

## 11.2 总体概览

| Sprint | 周期 | 主题 | 人天 | 人力 |
|--------|------|------|------|------|
| Sprint 1 | 第 1-2 周 | 基础设施搭建 | 13.5 | 2 人 |
| Sprint 2 | 第 3-4 周 | 学生模块 | 20 | 3 人 |
| Sprint 3 | 第 5-6 周 | 教师核心模块 | 26 | 3 人 |
| Sprint 4 | 第 7-8 周 | 教师扩展模块（含科研负责人职能） | 24 | 3 人 |
| Sprint 5 | 第 9-10 周 | 管理员模块 | 10 | 2 人 |
| Sprint 6 | 第 11-12 周 | 集成与联调 | 20 | 2 人 |
| Sprint 7 | 第 13-14 周 | 优化与交付 | 16 | 2 人 |
| **合计** | **14 周** | | **129.5** | |

## 11.3 Sprint 1：基础设施搭建（第 1-2 周）

**目标**：完成项目初始化、核心基础设施、开发环境搭建，具备独立开发业务页面的能力。

| 编号 | 任务 | 人天 | 交付物 | 依赖 |
|------|------|------|--------|------|
| 1.1 | Vite + Vue 3 + TypeScript 项目初始化 | 1 | 可运行的空项目，TypeScript 严格模式通过 | 无 |
| 1.2 | Element Plus 按需引入 + Design System 主题配置 | 1 | Element Plus 组件可用，主题颜色与 Design System 一致 | 1.1 |
| 1.3 | Axios 封装（实例、拦截器、Token、错误处理、重试） | 1.5 | 完整的 HTTP 层，支持 Mock 切换 | 1.1 |
| 1.4 | Pinia 核心 Store（User、App、Permission） | 1.5 | 三个核心 Store 可用，包含完整的 State/Action/Getter | 1.1 |
| 1.5 | 路由框架（公开路由、角色路由、导航守卫、菜单生成） | 2 | 完整的路由体系，三角色路由框架就绪 | 1.4 |
| 1.6 | Layout 组件（AppLayout、Navbar、Sidebar、PageContainer、PageHeader） | 2 | 全局布局组件可用 | 1.2 |
| 1.7 | Mock 基础设施（Mock.js 配置、Mock 切换、Mock 延迟） | 1 | Mock 框架可用，支持环境变量切换 | 1.3 |
| 1.8 | ESLint + Prettier + Husky + lint-staged 配置 | 0.5 | 提交前自动 Lint 检查就绪 | 1.1 |
| 1.9 | 登录页面 + 登录逻辑 + Token 管理 + 导航守卫 | 2 | 可完成完整的登录流程（含 Token 刷新） | 1.3, 1.4, 1.5, 1.6 |
| 1.10 | 全局状态组件（Loading、Error、Empty、NoPermission） | 1 | 四种全局状态组件可用 | 1.2 |
| | **合计** | **13.5** | | |

**里程碑**：Sprint 1 完成后，项目已具备开发任意业务页面的基础设施，开发者可通过 `npm run dev` 启动开发服务器，在 Mock 模式下独立开发页面。

## 11.4 Sprint 2：学生模块（第 3-4 周）

**目标**：完成学生端全部 7 个页面，覆盖学生从登录到查看成绩的完整业务流程。

| 编号 | 任务 | 复杂度 | 人天 | 交付物 |
|------|------|--------|------|--------|
| 2.1 | 学生端 Dashboard 页面 | L3 | 4 | 学生首页（含图表卡片、近期任务、成绩概览） |
| 2.2 | 课程列表页面 + 课程详情页面 | L2 | 3 | 课程浏览（列表 + 搜索 + 分页 + 详情展示） |
| 2.3 | 实训任务列表页面 + 任务详情页面 | L2 | 4 | 任务浏览（列表 + 筛选 + 状态标签 + 详情 + 提交入口） |
| 2.4 | 代码提交页面（含 FileUploader 组件） | L3 | 4 | 代码提交（文件上传 + 进度展示 + 提交确认 + Git 推送） |
| 2.5 | 我的成绩页面（含 AIScorePanel 组件） | L3 | 3 | 成绩查看（AI 评分展示 + 教师评分展示 + 评语查看） |
| 2.6 | 个人中心页面 | L1 | 2 | 个人信息管理（查看 + 编辑 + 修改密码） |
| | **合计** | | **20** | |

**里程碑**：Sprint 2 完成后，学生端所有页面可用（Mock 模式下），学生端完整业务流程可跑通。

## 11.5 Sprint 3：教师模块（第 5-6 周）

**目标**：完成教师端全部 7 个页面，实现教师管理课程、查看学生提交、AI 分析和教师复核功能。

| 编号 | 任务 | 复杂度 | 人天 | 交付物 |
|------|------|--------|------|--------|
| 3.1 | 教师端 Dashboard 页面 | L3 | 4 | 教师首页（实训概览、提交统计、待复核提醒、图表） |
| 3.2 | 课程管理页面 | L2 | 3 | 课程 CRUD（创建、编辑、删除、关联班级） |
| 3.3 | 实训任务管理页面 | L3 | 4 | 任务 CRUD（创建、编辑、发布、截止时间、评分策略） |
| 3.4 | 学生提交列表 + 详情页面 | L2 | 3 | 提交查看（列表、筛选、详情、代码预览） |
| 3.5 | AI 分析结果查看页面（含 StreamingTextDisplay + AIResultCard） | L3 | 3 | AI 分析结果（流式展示、多维评分、推理过程、置信度） |
| 3.6 | 教师复核页面（含 AIConfirmPanel + ReviewTimeline） | L4 | 6 | 教师复核（AI 评分对照、人工评分覆盖、评语编辑、确认面板） |
| 3.7 | 成绩管理页面 | L3 | 3 | 成绩管理（列表、筛选、批量发布、导出 Excel） |
| | **合计** | | **26** | |

**里程碑**：Sprint 3 完成后，教师端核心业务流程可用：创建任务 -> AI 分析 -> 教师复核 -> 成绩发布。

## 11.6 Sprint 4：教师扩展模块（第 7-8 周）

**目标**：完成教师端扩展功能全部 6 个页面，实现教研分析、报表中心和知识库功能（原科研负责人职能，现归属于教师角色）。

| 编号 | 任务 | 复杂度 | 人天 | 交付物 |
|------|------|--------|------|--------|
| 4.1 | 实训全局概览页面 | L3 | 3 | 实训概览（全院实训任务汇总、筛选、对比、下钻） |
| 4.2 | 评分标准管理页面 | L3 | 4 | 评分标准 CRUD（维度定义、权重配置、标准绑定实训） |
| 4.3 | 教研分析页面（含 RadarChart + 趋势对比） | L4 | 6 | 教研分析（跨课程对比、趋势分析、多维度雷达图、下钻） |
| 4.4 | 报表中心页面（含学院报表） | L4 | 6 | 报表中心（报表生成、预览、PDF 导出、Excel 导出） |
| 4.5 | 知识库管理页面 | L2 | 3 | 知识库 CRUD（文章、代码示例、最佳实践、搜索） |
| 4.6 | 学生成长中心页面 | L2 | 2 | 学生成长轨迹查看（学习历程、能力雷达图、成长建议） |
| | **合计** | | **24** | |

**里程碑**：Sprint 4 完成后，教师端全部页面可用，教研分析和报表功能完整。

## 11.7 Sprint 5：管理员模块（第 9-10 周）

**目标**：完成管理员端全部 3 个页面，实现系统管理和配置功能。

| 编号 | 任务 | 复杂度 | 人天 | 交付物 |
|------|------|--------|------|--------|
| 5.1 | 管理员 Dashboard 页面 | L3 | 3 | 管理员首页（系统概览、用户统计、服务状态、图表） |
| 5.2 | 用户管理页面 | L2 | 3 | 用户 CRUD + 角色分配 + 批量导入 |
| 5.3 | 系统配置页面（LLM 配置 + 系统参数 + 日志 + 监控） | L3 | 4 | 系统配置（LLM 模型选择、API Key、参数调整、系统变量、操作日志、服务监控） |
| | **合计** | | **10** | |

**里程碑**：Sprint 5 完成后，全部 25 个业务页面开发完成（Mock 模式下）。

## 11.8 Sprint 6：集成与联调（第 11-12 周）

**目标**：前后端联调、AI 模块 SSE 流式响应对接、端到端测试。

| 编号 | 任务 | 人天 | 交付物 |
|------|------|------|--------|
| 6.1 | 前后端接口联调（全模块逐一对接） | 5 | 所有接口调通，Mock 模式切换为真实接口 |
| 6.2 | AI 模块 SSE 流式响应对接 | 3 | SSE 流式渲染可用，断线重连机制验证 |
| 6.3 | 文件上传/下载联调 | 2 | 文件功能可用（上传、预览、下载） |
| 6.4 | 权限全流程测试（三角色各自登录测试） | 2 | 权限矩阵验证通过 |
| 6.5 | 端到端业务流程测试（学生提交 -> 教师复核与教研分析 -> 报表导出） | 3 | 全业务流程可跑通 |
| 6.6 | 问题修复 + 体验优化 | 3 | 联调发现的问题全部修复 |
| 6.7 | 性能测试 + 优化 | 2 | 性能指标达标（FCP < 2s，打包 < 300KB gzip） |
| | **合计** | **20** | |

**里程碑**：Sprint 6 完成后，全系统前后端联通，核心业务流程可端到端运行，性能达标。

## 11.9 Sprint 7：优化与交付（第 13-14 周）

**目标**：最终优化、文档完善、打包部署。

| 编号 | 任务 | 人天 | 交付物 |
|------|------|------|--------|
| 7.1 | UI/UX 最终走查与优化 | 3 | 交互细节优化，视觉一致性验证 |
| 7.2 | 兼容性测试（Chrome、Edge、银河麒麟浏览器） | 2 | 兼容性测试报告，兼容性问题修复 |
| 7.3 | 性能优化（首屏加载、打包体积、代码分割） | 2 | 性能指标确认达标 |
| 7.4 | Docker 镜像构建 + 部署脚本 | 2 | 可部署的前端 Docker 镜像 + Nginx 配置 |
| 7.5 | 用户手册（前端操作说明） | 2 | 面向三角色用户的操作手册 |
| 7.6 | 竞赛文档整理 | 2 | 竞赛评审所需文档齐备 |
| 7.7 | Bug 修复 + 最终打磨 | 3 | 已知问题全部修复 |
| | **合计** | **16** | |

**里程碑**：Sprint 7 完成后，项目达到可竞赛评审和可部署状态。

---

# 第十二章 开发顺序

## 12.1 章节概述

本章提供精确的页面开发顺序编号，作为每日开发工作的执行依据。开发顺序遵循五大原则：基础设施先行、核心流程优先、简单页面先做、依赖关系驱动、数据流向驱动。

## 12.2 全局开发顺序

### 优先级 P0 -- Sprint 1，必须最先完成

| 序号 | 任务 | 所属 Sprint |
|------|------|------------|
| 1 | Vite + Vue 3 + TypeScript 项目初始化 + ESLint/Prettier 配置 | Sprint 1 |
| 2 | Element Plus 主题配置 + 环境变量配置 | Sprint 1 |
| 3 | 全局状态组件（LoadingState、ErrorState、EmptyState、NoPermission） | Sprint 1 |
| 4 | Axios 封装 + Mock 基础设施 | Sprint 1 |
| 5 | Pinia 核心 Store（User、App、Permission） | Sprint 1 |
| 6 | 路由框架 + 导航守卫 | Sprint 1 |
| 7 | Layout 组件（AppLayout、Navbar、Sidebar、PageContainer、PageHeader） | Sprint 1 |
| 8 | 登录页面 + 登录逻辑 + Token 管理 | Sprint 1 |

### 优先级 P1 -- Sprint 2，学生端

| 序号 | 页面 | 复杂度 |
|------|------|--------|
| 9 | 学生 Dashboard 页面 | L3 |
| 10 | 课程列表页面 | L2 |
| 11 | 课程详情页面 | L2 |
| 12 | 实训任务列表页面 | L2 |
| 13 | 实训任务详情页面 | L2 |
| 14 | 代码提交页面 | L3 |
| 15 | 我的成绩页面 | L3 |
| 16 | 个人中心页面 | L1 |

### 优先级 P2 -- Sprint 3，教师端

| 序号 | 页面 | 复杂度 |
|------|------|--------|
| 17 | 教师 Dashboard 页面 | L3 |
| 18 | 课程管理页面 | L2 |
| 19 | 实训任务管理页面 | L3 |
| 20 | 学生提交列表页面 | L2 |
| 21 | 学生提交详情页面 | L2 |
| 22 | AI 分析结果查看页面 | L3 |
| 23 | 教师复核页面 | L4 |
| 24 | 成绩管理页面 | L3 |

### 优先级 P3 -- Sprint 4，教师扩展端

| 序号 | 页面 | 复杂度 |
|------|------|--------|
| 25 | 实训全局概览页面 | L3 |
| 26 | 评分标准管理页面 | L3 |
| 27 | 教研分析页面 | L4 |
| 28 | 报表中心页面（含学院报表） | L4 |
| 29 | 知识库管理页面 | L2 |
| 30 | 学生成长中心页面 | L2 |

### 优先级 P4 -- Sprint 5，管理员端

| 序号 | 页面 | 复杂度 |
|------|------|--------|
| 31 | 管理员 Dashboard 页面 | L3 |
| 32 | 用户管理页面 | L2 |
| 33 | 系统配置页面 | L3 |
| 34 | 404 页面 | L1 |

## 12.3 开发顺序五大原则

**原则一：基础设施先行**。Layout、路由、权限、API 层是所有页面的基础。如果不完成基础设施，每个页面都需要自行处理路由跳转、权限判断和 API 调用，导致大量重复代码和后期重构成本。

**原则二：核心流程优先**。学生提交实训 -> 教师利用 AI 分析 -> 教师复核评分 -> 学生查看成绩，这是系统的核心业务链路。优先开发这个链路可以尽早验证核心交互逻辑的正确性。

**原则三：简单页面先做**。每个 Sprint 内优先开发 L1/L2 简单页面，建立团队信心和开发节奏，再攻克 L3/L4 复杂页面。如果一开始就开发复杂页面，可能在 Sprint 早期就面临进度压力。

**原则四：依赖关系驱动**。组件 A 被页面 B 依赖时，组件 A 先开发。例如：AIResultCard 组件必须在 AI 分析结果查看页面之前完成，ReviewTimeline 组件必须在教师复核页面之前完成，FileUploader 组件必须在代码提交页面之前完成。

**原则五：数据流向驱动**。数据生产者先于数据消费者。例如：教师创建实训任务（数据生产）在 Sprint 3，学生查看和提交实训任务（数据消费）在 Sprint 2。虽然顺序不严格遵循数据流方向，但在单个 Sprint 内部优先完成数据生产相关的页面。

## 12.4 并行开发策略

| Sprint | 并行策略 |
|--------|---------|
| Sprint 1 | Axios 封装（开发者 A）+ 路由框架（开发者 B）+ Pinia Store（开发者 C）可并行 |
| Sprint 2 | 3 个开发者各自独立负责 2-3 个页面的完整七步流程 |
| Sprint 3 | 开发者 A：教师复核页（L4，核心，6 天）+ 成绩管理页（L3，3 天）；开发者 B：AI 分析结果页（L3，3 天）+ 课程管理页（L2，3 天）+ 实训任务管理页（L3，4 天）；开发者 C：Dashboard（L3，4 天）+ 学生提交列表+详情（L2，3 天） |
| Sprint 4 | 开发者 A：报表中心（L4，6 天）+ 知识库管理（L2，3 天）；开发者 B：教研分析（L4，6 天）+ 实训概览（L3，3 天）；开发者 C：评分标准管理（L3，4 天）+ 学生成长中心（L2，2 天） |
| Sprint 5 | 2 个开发者分配 3 个页面 |
| Sprint 6 | 前后端联调协作，需至少 1 名后端配合 |
| Sprint 7 | Docker/文档/优化各自并行 |

---

# 第十三章 编码标准

## 13.1 章节概述

本章定义项目特有的编码标准补充。完整的编码规范参见《Frontend Specification v1.0》全部 24 章。本章仅列出实施层面的关键速查表和补充标准，不重复规范文档中的内容。

## 13.2 命名规范速查表

| 实体 | 命名格式 | 示例 | 说明 |
|------|---------|------|------|
| Vue 组件文件 | PascalCase.vue | UserSelector.vue | 多个单词组成，每个单词首字母大写 |
| 页面文件 | PascalCase.vue | DashboardPage.vue | 必须以 Page 后缀结尾 |
| 目录 | kebab-case | user-management/ | 全部小写，单词间用连字符分隔 |
| TypeScript 接口 | PascalCase | IUserInfo | 以 I 前缀开头 |
| TypeScript 类型别名 | PascalCase | TUserRole | 以 T 前缀开头 |
| TypeScript 枚举 | PascalCase | UserRole | 枚举成员为 UPPER_SNAKE_CASE |
| Pinia Store | camelCase | useUserStore | use 前缀 + 功能名 + Store 后缀 |
| Composable | camelCase | usePermission | use 前缀 + 功能描述 |
| Hooks | camelCase | useTaskList | use 前缀 + 业务场景描述 |
| Utils 函数 | camelCase | formatDate | 动词 + 名词 |
| API 函数 | camelCase | fetchUserList | fetch/create/update/delete + 资源名 |
| 常量 | UPPER_SNAKE_CASE | API_BASE_URL | 全大写，单词间用下划线分隔 |
| CSS 类名 | kebab-case | .user-list-container | 全小写，单词间用连字符分隔 |
| Props | camelCase | userId | 布尔值 Props 用 is/has/can 前缀 |
| Emits | kebab-case | update:model-value | 与 Vue 3 v-model 约定一致 |
| 路由 path | kebab-case | /user-management | 全小写，单词间用连字符分隔 |
| 路由 name | camelCase | UserManagement | PascalCase 风格，与页面组件名对应 |

## 13.3 每个页面必须覆盖的状态

每个页面开发时必须逐一确认以下六种状态：

**Loading 状态**：数据加载中展示骨架屏或 Loading 动画。骨架屏的形状应接近实际内容布局（卡片骨架屏、表格骨架屏），而非单一的旋转动画。加载时间超过 5 秒展示"加载中，请耐心等待"文字提示。

**Empty 状态**：无数据时展示空状态插图和引导文案，而非空白区域。空状态必须提供明确的后续操作引导（如"点击创建第一个实训任务"）。禁止仅展示"暂无数据"文字。

**Error 状态**：请求失败时展示错误原因和重试按钮，而非静默失败或仅展示"加载失败"。网络错误、服务端错误、权限错误分别展示不同的提示文案和操作入口。

**Success 状态**：数据正常展示。表格、卡片、图表等数据展示组件按照 Design System 规范渲染。

**NoPermission 状态**：无权限时展示"无权限访问"页面或隐藏对应功能入口，而非展示报错或空白页面。用户手动输入无权限 URL 时展示 403 页面。

**Offline 状态**：网络断开时展示"网络连接已断开"全局提示条（顶部固定），网络恢复后自动消失并重新加载数据。

## 13.4 每个表单必须覆盖的场景

**新增模式**：表单字段使用默认值填充，必填字段以星号标记，提交按钮文案为"创建"或"提交"，提交成功后跳转到列表页或详情页。

**编辑模式**：表单字段回填已有数据，主键字段只读（如 ID、创建时间），提交按钮文案为"保存"，提交成功后刷新当前页面数据。

**查看模式**：所有字段只读，不展示提交按钮，可提供"编辑"按钮切换到编辑模式。

**提交中**：提交按钮置为 Loading 状态并禁用，防止重复提交。表单区域展示半透明遮罩，阻止用户修改表单内容。

**提交成功**：Message 成功提示（自动消失），根据业务场景选择跳转列表页或刷新当前页。

**提交失败**：Message 错误提示（展示错误原因），保留表单中用户已填写的数据，不清空表单。

**表单重置**：点击重置按钮时弹出确认对话框"确定要重置表单吗？已填写的内容将丢失"。

## 13.5 AI 功能特有标准

**AI 请求前**：展示预计耗时提示（如"AI 分析预计需要 30-60 秒"）和 Token 消耗估算。

**AI 分析中**：展示分阶段的进度动画（如"正在分析代码结构 -> 正在评估代码质量 -> 正在生成评分建议"），每个阶段完成时更新进度状态。

**AI 结果展示前**：校验 AI 返回的 JSON 数据格式。格式正确则渲染 AI 结果组件，格式错误则展示"AI 返回格式异常"提示并提供重试按钮。

**AI 结果确认**：AI 结果必须展示"确认"和"驳回"两个操作按钮。确认后 AI 评分写入 Store 中的 teacherScores，驳回后 AI 评分不生效并记录驳回原因。AI 结果不可不经确认直接生效。

**AI 错误处理**：AI 超时、Token 耗尽、返回格式错误分别展示不同的提示内容和重试按钮。Token 耗尽时额外展示当前 Token 用量和充值指引。

**Token 用量展示**：在 AI 分析结果面板底部展示本次调用消耗的 Token 数量。

## 13.6 与 Frontend Specification 的关系

本文档第十三章为实施层面的编码标准补充速查。完整的编码规范、原理说明、正反示例和详细规则，参见《Frontend Specification v1.0》。两条规则如有冲突，以《Frontend Specification v1.0》为准。

---

# 第十四章 质量保证

## 14.1 章节概述

本章定义前端质量保证的实施方案，覆盖代码质量、Review 流程、Mock 验证、功能测试、性能测试和可达性检查。

## 14.2 代码质量

### 14.2.1 静态检查

| 工具 | 配置 | 触发时机 |
|------|------|---------|
| ESLint | @vue/eslint-config-typescript | 保存时自动检查 + 提交前强制检查 |
| Prettier | 统一配置文件 .prettierrc.json | 保存时自动格式化 + 提交前强制检查 |
| TypeScript | tsconfig.json strict 模式 | 构建时强制检查 |
| lint-staged | 仅检查暂存区文件 | git commit 前自动执行 |

**提交前检查流程**：git commit -> Husky 触发 pre-commit hook -> lint-staged 执行 -> ESLint 检查暂存区文件 -> Prettier 格式化检查 -> TypeScript 类型检查 -> 全部通过则允许提交，任一失败则阻止提交。

### 14.2.2 质量门禁

代码合并到主分支前必须通过以下门禁：

1. ESLint 检查零错误（警告可由 Reviewer 判断是否放行）
2. TypeScript 类型检查零错误
3. 所有受影响的 Mock 数据与 API Mock Specification 一致
4. 至少一名 Reviewer 的 Code Review 通过

## 14.3 代码 Review

### 14.3.1 Review 时机

- 每个页面开发完成后（第十章第五步）：开发者自测完成后提交 Review
- 每个 Sprint 结束前：Sprint 内所有页面汇总 Review
- 关键基础设施变更时：路由框架、API 层、Store 设计的变更必须 Review

### 14.3.2 Review 要点（Checklist）

**规范遵循**：代码是否遵循《Frontend Specification v1.0》的规范？组件命名是否正确？目录放置是否正确？

**设计一致性**：UI 实现是否与《UI Design System v1.0》一致？颜色、间距、圆角、阴影是否正确？字体和字号是否匹配？

**组件复用**：是否复用了《Component Library v1.0》中的已有组件？新建的组件是否可以替换为已有组件的组合？新组件是否值得加入 Component Library？

**状态覆盖**：页面是否覆盖了 Loading / Empty / Error / Success / NoPermission / Offline 六种状态？表单是否覆盖了新增/编辑/查看/提交中/提交成功/提交失败/重置七种场景？

**异常处理**：所有可能失败的异步操作是否有 try-catch？所有 API 调用是否考虑了超时和重试？AI 分析结果是否经过人工确认？

**性能**：首屏加载时间是否在 2s 以内？是否有不必要的重复渲染？列表数据量较大时是否使用了虚拟滚动或分页？

### 14.3.3 Review 通过标准

- 无阻断性问题（Functional Blocker）：影响功能的 Bug 或规范严重违反
- 建议性问题（Suggestion）：代码优化建议、命名改进建议，由开发者判断是否采纳
- 所有阻断性问题修复完成后方可合并代码

## 14.4 Mock 验证

### 14.4.1 Mock 数据一致性验证

每个 Sprint 结束前，对照《API Mock Specification v1.0》逐一验证 Mock 数据：

- 字段名是否与 API Mock Specification 一致（含大小写）
- 字段类型是否与 API Mock Specification 一致（string/number/boolean/array/object）
- 嵌套结构是否与 API Mock Specification 一致
- 枚举值是否与 API Mock Specification 的枚举定义一致

### 14.4.2 Mock 场景覆盖验证

每个 Mock 接口必须覆盖以下场景：

| 场景 | Mock 实现 | 验证方法 |
|------|---------|---------|
| 正常数据（有数据） | 返回 2-3 条或更多模拟数据 | 页面正常展示列表或详情 |
| 空数据 | 返回空数组或空对象 | 页面展示 Empty 状态 |
| 分页数据 | 返回带 total 字段的分页响应 | 分页组件正常翻页 |
| 网络错误 | Mock 返回 500 状态码 | 页面展示 Error 状态和重试按钮 |
| 认证错误 | Mock 返回 401 状态码 | 页面跳转登录页 |
| 权限错误 | Mock 返回 403 状态码 | 页面展示 NoPermission 状态 |

### 14.4.3 Mock 延迟验证

| 场景 | 延迟值 | 验证方法 |
|------|--------|---------|
| 正常网络 | 200ms | Loading 状态正常闪现 |
| 慢网络 | 2000ms | Loading 状态持续展示，不出现白屏 |
| 超时 | 超过接口超时时间 | 触发超时错误处理，展示重试按钮 |

## 14.5 功能测试

### 14.5.1 页面级功能测试

每个页面按以下清单进行功能测试：

1. 页面首次加载：Loading 状态是否正确展示？数据加载后是否正确渲染？
2. 页面交互：按钮点击是否触发正确的操作？表单提交是否成功？
3. 页面跳转：路由跳转是否正确？面包屑是否正确更新？
4. 数据刷新：刷新按钮是否重新加载数据？筛选条件是否保留？

### 14.5.2 权限测试

每种角色分别登录测试：

1. 菜单是否只展示有权访问的页面？
2. 页面内的按钮是否按权限正确显示/隐藏？
3. 手动输入无权限 URL 是否展示 403 页面或跳转？

### 14.5.3 响应式测试

三种分辨率逐一验证：

| 分辨率 | 测试要点 |
|--------|---------|
| 1920px | 标准桌面：全功能展示，侧边栏展开 |
| 1366px | 常见笔记本：侧边栏可折叠，表格横向滚动 |
| 1024px | 小屏幕：侧边栏默认折叠，表单为单列布局 |

### 14.5.4 浏览器兼容性测试

| 浏览器 | 测试优先级 | 说明 |
|--------|-----------|------|
| Chrome 最新版 | 主力测试 | 开发和调试环境 |
| Edge 最新版 | 兼容测试 | Windows 用户常用 |
| 银河麒麟自带浏览器 | 验证测试 | 部署环境验证，Sprint 7 介入 |

## 14.6 性能测试

### 14.6.1 性能指标目标

| 指标 | 目标值 | 测试工具 |
|------|--------|---------|
| First Contentful Paint（FCP） | < 2s | Lighthouse |
| Largest Contentful Paint（LCP） | < 3s | Lighthouse |
| First Input Delay（FID） | < 100ms | Lighthouse |
| Cumulative Layout Shift（CLS） | < 0.1 | Lighthouse |
| 页面路由切换时间 | < 500ms | Chrome DevTools Performance |
| 打包总体积（未压缩） | < 1MB | Vite build 输出 |
| 打包总体积（gzip 后） | < 300KB | Vite build 输出 |

### 14.6.2 性能测试时机

- Sprint 1 结束后：首屏性能基线测试
- 每个 Sprint 结束后：比较性能趋势，防止性能退化
- Sprint 6 性能专项：全量性能测试和优化
- Sprint 7 最终验证：确认所有性能指标达标

## 14.7 可达性

- 颜色对比度：所有文本与背景的颜色对比度符合 WCAG AA 标准（普通文本 4.5:1，大文本 3:1）
- 表单 Label：所有表单输入元素有明确的 label 关联（使用 label for 属性或 aria-labelledby）
- 按钮文本：所有按钮有明确的文本或 aria-label（图标按钮必须有 aria-label）
- 错误提示：错误提示通过 aria-live="polite" 区域播报
- 键盘导航：支持 Tab 键在表单元素间导航，Enter 键提交表单，Escape 键关闭弹窗

---

# 第十五章 风险管理

## 15.1 章节概述

本章识别前端开发过程中的潜在风险，评估其发生概率和影响程度，并制定应对策略。风险管理贯穿整个开发周期，每个 Sprint 结束前复查风险状态。

## 15.2 技术风险

| 风险 | 概率 | 影响 | 应对策略 |
|------|------|------|---------|
| Element Plus 版本不兼容 | 低 | 高 | 项目初始化时锁定 Element Plus 版本号（在 package.json 中使用精确版本号，不使用 ^ 前缀）。升级 Element Plus 前在独立分支验证，所有页面通过回归测试后才合并 |
| ECharts 大数据量渲染性能（Dashboard 多图表同时渲染） | 中 | 中 | 使用 ECharts 的 dataZoom 组件限制可视数据范围，大数据量时启用数据采样。图表懒加载：仅渲染视口内的图表，视口外的图表延迟渲染。Canvas 渲染模式（默认）而非 SVG 模式 |
| SSE 流式响应兼容性（银河麒麟浏览器） | 中 | 高 | Sprint 1 期间完成银河麒麟浏览器 SSE 支持的技术预研。如不支持 SSE，准备 HTTP 长轮询降级方案。降级方案的切换通过环境变量控制，业务代码不变 |
| TypeScript 严格模式与第三方库类型冲突 | 中 | 低 | 在 src/types/ 目录下建立第三方库类型补充声明文件（*.d.ts）。冲突的类型定义通过 declare module 扩展解决。不影响开发进度 |
| Vite 构建在 LoongArch 架构下异常 | 中 | 高 | Sprint 1 期间在 LoongArch 开发环境上执行一次完整构建验证。如 Vite 构建失败，评估 esbuild 替代方案或降级到 Webpack |

## 15.3 依赖风险

| 风险 | 概率 | 影响 | 应对策略 |
|------|------|------|---------|
| 后端接口延迟交付（教师复核接口优先度高但后端开发周期长） | 高 | 高 | Mock First 策略是核心应对手段。前端不依赖后端接口的实际可用性，Mock 数据完全模拟后端行为。接口延迟仅影响 Sprint 6 联调，不影响 Sprint 1-5 的前端独立开发 |
| AI API 不稳定或接口变更 | 中 | 高 | 前端 AI 模块通过 src/api/ai.ts 和 src/composables/useSSE.ts 两层抽象与后端 AI 服务隔离。AI API 变更仅需修改 API 适配层，不影响 AI 组件和页面的业务逻辑 |
| 设计原型变更（评审或竞赛准备中发现交互不合理） | 中 | 中 | 组件化开发降低变更影响范围：布局调整仅修改 Layout 组件，样式调整仅修改 CSS 变量，交互流程调整仅修改 Composable。单个页面级别的原型重构影响范围可控（不超过该页面的 6 人天预算） |
| 关键依赖库停止维护 | 低 | 高 | 选择社区活跃度高的成熟依赖库（Vue 3、Element Plus、ECharts 均属于大生态核心库，停止维护概率极低）。避免使用小众依赖库。每个新增依赖需在团队内讨论并记录选型理由 |

## 15.4 UI/UX 风险

| 风险 | 概率 | 影响 | 应对策略 |
|------|------|------|---------|
| Design System Token 与 Element Plus 默认主题不完全匹配 | 中 | 中 | 在 Sprint 1 Element Plus 主题配置任务中完成所有 Token 映射和验证。建立 Design Token 与 Element Plus CSS 变量的对照表，确保每个 Token 都有对应的 Element Plus 变量覆盖 |
| 复杂页面（教师复核、教研分析）交互体验不达标 | 中 | 中 | 在 Sprint 3 和 Sprint 4 开始前，对两个最复杂页面做交互走查（Walkthrough），模拟完整的用户操作流程。如有条件，准备交互原型或低保真页面提前验证 |
| 银河麒麟浏览器 CSS 渲染异常（Flexbox/Grid 兼容性） | 中 | 高 | Sprint 7 兼容性测试尽早介入。使用 CSS 渐进增强策略：核心布局使用兼容性好的 CSS 属性，视觉效果使用新特性增强。银河麒麟浏览器基于 Chromium，大部分现代 CSS 特性应可正常支持 |
| 响应式布局在 1024px 分辨率下信息展示不全 | 低 | 低 | 优先保证 1920px 和 1366px 的体验，1024px 为降级展示（侧边栏折叠、表格横向滚动）。非目标用户的移动端不在测试范围 |

## 15.5 后端依赖风险

| 风险 | 概率 | 影响 | 应对策略 |
|------|------|------|---------|
| API 接口字段名或数据结构在联调阶段发现与 Mock 不一致 | 高 | 中 | Sprint 6 联调前，对照 API Mock Specification 与后端实际接口进行字段比对。不一致处以 API Mock Specification 为准，与后端协商修正。前端 API 层封装隔离字段映射，页面不直接依赖接口原始字段名 |
| 后端认证方案在开发中后期调整（如从 JWT 改为 Session） | 低 | 高 | Token 管理封装在 src/api/http.ts 和 User Store 中，不散落在各页面。认证方案变更仅需修改 Token 管理模块和导航守卫，页面代码不受影响 |
| 权限模型变更（如新增角色或权限粒度调整） | 低 | 高 | 权限控制通过 Permission Store 和 v-permission 指令集中管理。权限标识字符串是前端和后端的共同契约，变更需前后端同步 |
| 后端接口响应时间不达标导致前端频繁超时 | 中 | 中 | 前端设置分级超时时间（常规 30s，AI 120s，导出 60s）。超时后提供明确的重试入口，而非静默失败。如特定接口持续超时，与后端协作分析性能瓶颈 |

## 15.6 AI 集成风险

| 风险 | 概率 | 影响 | 应对策略 |
|------|------|------|---------|
| AI 响应时间过长（超过 120s 超时时间） | 高 | 中 | 前端展示阶段性分析进度（分阶段展示"正在分析"状态，而非单一等待状态）。超过 120s 后展示"AI 分析超时"提示，提供"重新分析"和"改为手动评分"两个操作入口 |
| AI 返回的 JSON 格式不稳定（非标准格式或缺少必要字段） | 高 | 高 | 前端在 AIConfirmPanel 组件中做严格的 JSON 解析和字段校验。解析失败时展示"AI 返回格式异常"提示和原始返回内容（供调试），提供重试按钮。JSON Schema 定义在 types/ai.ts 中，校验逻辑在 AI 组件中 |
| AI API Token 配额耗尽 | 中 | 中 | 在 AITokenUsage 组件中实时展示 Token 用量。Token 低于 10% 阈值时展示警告提示。Token 耗尽时后端返回特定错误码，前端展示"AI 服务 Token 已用完"友好提示，引导用户联系管理员充值 |
| AI 分析结果质量差（评分与人工判断偏差大） | 中 | 高 | 强制人工确认环节是核心保障：AI 评分不可直接作为最终成绩，教师必须在 AIConfirmPanel 中逐项确认或修改。Teacher Override 后以教师评分为准。系统设计上 AI 是"辅助建议"而非"自动决策" |
| SSE 连接在分析过程中意外中断 | 中 | 中 | useSSE Composable 实现断线重连逻辑：中断后自动尝试重连（最多 3 次），重连成功后从上次中断的进度继续接收。重连失败后展示"连接中断"提示和手动重试按钮。SSE 连接状态通过 AIAnalysisProgress 组件可视化展示 |

## 15.7 进度风险

| 风险 | 概率 | 影响 | 应对策略 |
|------|------|------|---------|
| Sprint 任务无法在 2 周内完成 | 中 | 高 | 每个 Sprint 预留 20% 缓冲时间（2 天 / Sprint）。Sprint 内任务按优先级排序，Sprint 结束前优先完成核心任务，非核心任务可推迟到下一 Sprint。每个 Sprint 结束时的 Demo 不需要 100% 完成度，但核心流程必须可演示 |
| 开发人力临时减少（如参赛队员其他事务） | 低 | 高 | 代码规范和文档完善降低人员交接成本。每个页面有独立的 Mock 数据和自测记录，新接手者可快速上手。关键基础设施（Sprint 1）和核心流程（Sprint 2-3）优先保障人力 |
| 需求变更导致已开发页面需要返工 | 中 | 中 | 组件化开发降低变更影响范围。需求变更时优先评估影响面，变更限于单个组件或单个页面的情况容易处理。跨角色、跨模块的需求变更在 Sprint 评审中讨论，推迟到下一 Sprint 或通过架构扩展点实现 |

## 15.8 风险跟踪

每个 Sprint 结束前的 Sprint Review 中，复查本章风险清单：

1. 已发生的风险：记录实际影响和应对效果
2. 概率或影响变化的风险：更新评估
3. 新识别风险：补充到风险清单

风险状态的变化更新到 Sprint 报告中。

---

# 附录 A：文档引用索引

| 文档 | 版本 | 主要引用章节 |
|------|------|-------------|
| PRD | v1.0 | 产品功能定义、用户故事、验收标准 |
| SDS | v1.0 | 第二章（系统架构）、第六章（流程设计）、第十二章（接口设计）、第十三章（权限设计） |
| UI Design System | v1.0 | 第 2 章（Design Tokens）、第 3 章（颜色系统）、第 5 章（8pt Spacing）、第 19 章（页面模板） |
| Component Library | v1.0 | 第 4 章（Base Components）、第 5 章（Layout Components）、第 6 章（Business Components）、第 7 章（AI Components）、第 8 章（Chart Components） |
| Frontend Specification | v1.0 | 全部 24 章（开发规范最高文档） |
| API Mock Specification | v1.0 | 第 3 章（统一响应格式）、第 5-9 章（各端接口）、第 13 章（分页规范）、第 14 章（Mock 数据规范）、第 15 章（AI Mock 规范） |

---

# 附录 B：Sprint 日历视图

| 周次 | 日期范围 | Sprint | 关键里程碑 |
|------|---------|--------|-----------|
| 第 1 周 | - | Sprint 1 | 项目初始化、技术栈集成 |
| 第 2 周 | - | Sprint 1 | 基础设施全部完成 |
| 第 3 周 | - | Sprint 2 | 学生端 Dashboard + 课程 + 任务列表 |
| 第 4 周 | - | Sprint 2 | 学生端提交 + 成绩 + 个人中心 |
| 第 5 周 | - | Sprint 3 | 教师端 Dashboard + 课程管理 + 任务管理 |
| 第 6 周 | - | Sprint 3 | 教师端复核 + 成绩管理（核心流程） |
| 第 7 周 | - | Sprint 4 | 教师扩展：实训概览 + 评分标准 + 教研分析 |
| 第 8 周 | - | Sprint 4 | 教师扩展：报表中心 + 知识库 + 学生成长中心 |
| 第 9 周 | - | Sprint 5 | 管理员端 Dashboard + 用户管理 |
| 第 10 周 | - | Sprint 5 | 管理员端系统配置 |
| 第 11 周 | - | Sprint 6 | 前后端联调 + AI 模块对接 |
| 第 12 周 | - | Sprint 6 | 端到端测试 + 性能优化 |
| 第 13 周 | - | Sprint 7 | UI/UX 走查 + 兼容性测试 |
| 第 14 周 | - | Sprint 7 | Docker 部署 + 文档 + 最终交付 |

---

**文档结束**

本文档为《Frontend Implementation Plan v1.0》，是项目前端开发的实施蓝图。所有 Sprint 执行必须严格依照本文档。任何计划调整需经过团队评审并更新本文档版本。
