# Page Analysis: 学生仪表盘 (Student Dashboard)

**文档版本**: v1.0
**创建日期**: 2026-07-03
**分析范围**: 学生端仪表盘首页及全体数据概览

---

# Section 1: Page Identity

| 属性 | 值 |
|------|-----|
| 页面名称 | 学生仪表盘 |
| 页面文件 | src/pages/student/DashboardPage.vue |
| 路由路径 | /student/dashboard |
| 路由名称 | StudentDashboard |
| 所属 Sprint | Sprint 2 |
| 所属 Feature | F-2-01（参见《Sprint 2 Spec》Section 3） |
| 页面角色 | 学生（student） |
| 页面复杂度 | L3（多图表渲染、多 API 并行加载、多状态覆盖） |
| 原型参考 | 参见《UI Design System v1.0》学生仪表盘原型 |

页面职责：学生登录后的首页。展示个人学习概况（统计卡片）、近期任务列表、成绩趋势折线图、六维能力雷达图。所有数据来自 `GET /api/v1/student/dashboard`，欢迎语中的用户姓名来自 `GET /api/v1/auth/me`。

---

# Section 2: Page Layout Structure

学生仪表盘嵌套在 AppLayout 中，使用顶部导航栏（无侧边栏）。页面内分为 PageHeader 欢迎区、StatCard 统计行、双图表区（左折线图 + 右雷达图）、RecentTask 表格区四个区域。

```
+------------------------------------------------------------------+
|  AppLayout                                                        |
|  +---------------------------------------------------------------+|
|  |  Navbar (height: 56px, bg: #FFFFFF, box-shadow, fixed top)   ||
|  |  [Logo] [导航菜单]              [通知图标] [用户头像+姓名] [退出] ||
|  +---------------------------------------------------------------+|
|  |  PageContainer (max-width: 1280px, margin: 0 auto,            ||
|  |                 padding: --spacing-xl)                         ||
|  |                                                                ||
|  |  +-----------------------------------------------------------+  |
|  |  |  PageHeader                                                |  |
|  |  |  title: "欢迎回来，{realName}"（来自 /api/v1/auth/me）       |  |
|  |  |  breadcrumb: 首页 > 仪表盘                                   |  |
|  |  |  description: 展示当天日期（如"2026年7月3日 星期五"）         |  |
|  |  +-----------------------------------------------------------+  |
|  |  |  spacing: --spacing-lg (24px)                              |  |
|  |  +-----------------------------------------------------------+  |
|  |  |  StatCard Row (grid: repeat(4, 1fr), gap: --spacing-md)   |  |
|  |  |  +------------+ +------------+ +------------+ +----------+ |  |
|  |  |  | StatCard   | | StatCard   | | StatCard   | | StatCard | |  |
|  |  |  | 待完成任务 | | 已完成任务 | | 平均成绩   | | AI分析中 | |  |
|  |  |  | icon:      | | icon:      | | icon:      | | icon:    | |  |
|  |  |  | Clock      | | CheckCircle| | Award      | | Cpu      | |  |
|  |  |  | color:     | | color:     | | color:     | | color:   | |  |
|  |  |  | #F59E0B   | | #10B981   | | #3B82F6   | | #8B5CF6 | |  |
|  |  |  | loading?   | | loading?   | | loading?   | | loading? | |  |
|  |  +------------+ +------------+ +------------+ +----------+ |  |
|  |  +-----------------------------------------------------------+  |
|  |  |  spacing: --spacing-lg (24px)                              |  |
|  |  +-----------------------------------------------------------+  |
|  |  |  Chart Row (grid: 2fr 1fr, gap: --spacing-md)             |  |
|  |  |  +----------------------------------+ +------------------+ |  |
|  |  |  |  LineChart                        | |  RadarChart      | |  |
|  |  |  |  title: "成绩趋势"                  | |  title: "能力维度" |  |
|  |  |  |  height: 320px                    | |  height: 320px   | |  |
|  |  |  |  xAxis: ["1月","2月",...,"7月"]    | |  categories:     | |  |
|  |  |  |  seriesData: [                   | |   ["代码质量",   | |  |
|  |  |  |    { name: "我的成绩",             | |    "需求理解",   | |  |
|  |  |  |      data: [85,88,90,...],       | |    "项目管理",   | |  |
|  |  |  |      color: #3B82F6 },           | |    "团队协作",   | |  |
|  |  |  |    { name: "班级平均",             | |    "文档规范",   | |  |
|  |  |  |      data: [78,80,82,...],       | |    "创新能力"]   | |  |
|  |  |  |      color: #94A3B8 }            | |  seriesData:     | |  |
|  |  |  |  ]                               | |    { name: "我的得分", |  |
|  |  |  |  showLegend: true                | |      data: [...],| |  |
|  |  |  +----------------------------------+ |      color:      | |  |
|  |  |                                       |      #3B82F6 },  | |  |
|  |  |                                       |    { name: "班级平均", |  |
|  |  |                                       |      data: [...],| |  |
|  |  |                                       |      color:      | |  |
|  |  |                                       |      #94A3B8 }   | |  |
|  |  |                                       |  showLegend: true| |  |
|  |  |                                       +------------------+ |  |
|  |  +-----------------------------------------------------------+  |
|  |  |  spacing: --spacing-lg (24px)                              |  |
|  |  +-----------------------------------------------------------+  |
|  |  |  Recent Tasks Section                                      |  |
|  |  |  Section Header: "近期任务" + "查看全部"链接（→ /student/    |  |
|  |  |                  tasks）                                    |  |
|  |  |  +-------------------------------------------------------+  |  |
|  |  |  |  BaseTable (stripe, hover, border)                     |  |  |
|  |  |  |  columns:                                              |  |  |
|  |  |  |  - 任务名称 (taskName)          min-width: 200px       |  |  |
|  |  |  |  - 所属课程 (courseName)        width: 160px           |  |  |
|  |  |  |  - 截止日期 (deadline)          width: 140px           |  |  |
|  |  |  |    格式化: YYYY-MM-DD, 临近/逾期高亮                     |  |  |
|  |  |  |  - 状态 (status)                width: 120px           |  |  |
|  |  |  |    TaskStatusBadge组件                                   |  |  |
|  |  |  |  - 成绩 (score)                 width: 100px           |  |  |
|  |  |  |    font: JetBrains Mono,        align: center           |  |  |
|  |  |  |    未评分显示 "--"                                      |  |  |
|  |  |  |  dataSource: recentTasks (最多 5 条)                    |  |  |
|  |  |  |  empty slot: EmptyState 组件                             |  |  |
|  |  |  +-------------------------------------------------------+  |  |
|  |  +-----------------------------------------------------------+  |
|  +------------------------------------------------------------------+
```

**区域说明**：

| 区域 | 宽度 | 高度 | 对齐 | 滚动策略 | 说明 |
|------|------|------|------|---------|------|
| 导航栏 | 100vw | 56px | - | fixed | 使用 AppLayout 内置 Navbar |
| PageHeader | 100% 父容器 | auto | 左对齐 | 不滚动 | 标题字体大小 --font-size-2xl，欢迎语动态拼接 realName。日期使用 new Date().toLocaleDateString('zh-CN', { year:'numeric', month:'long', day:'numeric', weekday:'long' }) |
| StatCard Row | 100% 父容器 | auto (约 120px) | 四列等宽 | 不滚动 | 使用 CSS Grid，gap: --spacing-md。小屏（<1024px）变为 2 列 |
| Chart Row | 100% 父容器 | 320px | 两列，左 2fr 右 1fr | 不滚动 | 小屏（<1024px）变为单列堆叠 |
| Recent Tasks | 100% 父容器 | auto | - | 不滚动 | 表格最大高度 400px，超出时内部滚动防止页面滚动条爆炸 |

**响应式行为**：
- 大于等于 1280px：StatCard 四列，Chart 两列，表格正常展示
- 1024px 至 1279px：StatCard 四列，Chart 两列，PageContainer padding 减为 --spacing-md
- 768px 至 1023px：StatCard 两列，Chart 单列堆叠（折线图在上，雷达图在下）
- 小于 768px：StatCard 两列（卡片内部文字缩小），Chart 单列堆叠，表格横向滚动

---

# Section 3: Component Tree & Specification

## 3.1 组件树

```
DashboardPage.vue（页面组件，嵌套在 AppLayout 内）
├── PageHeader（title="欢迎回来，{realName}"，breadcrumb items，date description）
├── <div> StatCard 容器（CSS Grid: repeat(4, 1fr)）
│   ├── StatCard（label="待完成任务"，value=stats.pendingTasks，icon=Clock，color="#F59E0B"，loading）
│   ├── StatCard（label="已完成任务"，value=stats.completedTasks，icon=CheckCircle，color="#10B981"，loading）
│   ├── StatCard（label="平均成绩"，value=stats.averageScore，icon=Award，color="#3B82F6"，loading，format: 数字+分）
│   └── StatCard（label="AI分析中"，value=stats.analyzingCount，icon=Cpu，color="#8B5CF6"，loading）
├── <div> Chart 容器（CSS Grid: 2fr 1fr）
│   ├── LineChart（title="成绩趋势"，xAxisData，seriesData，height=320，showLegend=true）
│   │   seriesData: [{ name:"我的成绩", data:[], color:"#3B82F6" }, { name:"班级平均", data:[], color:"#94A3B8" }]
│   └── RadarChart（title="能力维度"，categories，seriesData，height=320，showLegend=true）
│       seriesData: [{ name:"我的得分", data:[], color:"#3B82F6" }, { name:"班级平均", data:[], color:"#94A3B8" }]
├── <section> 近期任务区域
│   ├── <div> 区域头部（<h3>"近期任务"，<router-link to="/student/tasks">"查看全部"）
│   └── BaseTable（columns 配置，dataSource=recentTasks，max-height=400）
│       └── <template #status="{ row }">
│           └── TaskStatusBadge（status=row.status）
│       └── <template #score="{ row }">
│           └── <span> 数字（JetBrains Mono）或 "--"（无成绩时）
│       └── <template #empty>
│           └── EmptyState（message="暂无任务记录"）
```

## 3.2 本页面需要新建的子组件

### 新组件 1: LineChart（src/components/chart/LineChart.vue）

| 项 | 内容 |
|------|------|
| 组件职责 | 基于 ECharts 封装折线图，暴露最少 Props 供页面使用 |
| 复用场景 | 学生仪表盘成绩趋势、教师端班级成绩趋势、管理员数据大盘 |

**Props**:

| Prop | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| xAxisData | string[] | 是 | [] | X 轴标签数组，如 ["1月","2月","3月","4月","5月","6月","7月"] |
| seriesData | Array<{ name: string, data: number[], color: string }> | 是 | [] | 系列数据，name=图例标签，data=Y 轴数值数组，color=折线颜色 |
| height | number | 否 | 320 | 图表容器高度（px），不含 padding |
| showLegend | boolean | 否 | true | 是否显示图例 |
| title | string | 否 | "" | 图表标题，传入空字符串则不渲染标题区域 |

**Emits**:
无。LineChart 纯展示组件，不做交互事件抛出。

**Slots**:
无默认插槽或具名插槽。所有内容通过 Props 控制。

**Store 依赖**:
无 Store 依赖。LineChart 是纯展示组件。

**API 依赖**:
无 API 依赖。数据由父组件通过 Props 传入。

**状态覆盖**:

| 状态 | 条件 | 表现 |
|------|------|------|
| Empty | xAxisData 长度为 0 或 seriesData 为空数组 | 展示图表占位区域（保留高度 + 居中提示"暂无趋势数据"），不渲染 ECharts 实例 |
| Success | xAxisData 和 seriesData 均有数据 | 渲染 ECharts 折线图，支持 hover 显示 tooltip（交叉十字线 + 数据点浮层），支持 legend 点击切换系列显隐 |
| Error | ECharts 实例化失败（DOM 未就绪、数据格式错误等边缘情况） | 静默降级：展示占位区域 + 提示"图表加载失败"，不阻塞页面其他区域渲染 |

---

### 新组件 2: RadarChart（src/components/chart/RadarChart.vue）

| 项 | 内容 |
|------|------|
| 组件职责 | 基于 ECharts 封装雷达图，暴露最少 Props 供页面使用 |
| 复用场景 | 学生仪表盘能力维度、教师端学生综合评价对比 |

**Props**:

| Prop | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| categories | string[] | 是 | [] | 雷达图维度名称数组，如 ["代码质量","需求理解","项目管理","团队协作","文档规范","创新能力"] |
| seriesData | Array<{ name: string, data: number[], color: string }> | 是 | [] | 系列数据，name=图例标签，data=各维度数值数组（顺序与 categories 一致），color=填充和边框颜色 |
| height | number | 否 | 320 | 图表容器高度（px） |
| showLegend | boolean | 否 | true | 是否显示图例 |
| title | string | 否 | "" | 图表标题 |

**Emits**:
无。

**Slots**:
无。

**Store 依赖**:
无。

**API 依赖**:
无。

**状态覆盖**:

| 状态 | 条件 | 表现 |
|------|------|------|
| Empty | categories 长度为 0 或 seriesData 为空数组 | 展示占位区域 + 居中提示"暂无维度数据" |
| Success | categories 和 seriesData 均有数据 | 渲染 ECharts 雷达图，多边形填充区域半透明（opacity: 0.2），边框实线。hover 时 tooltip 展示维度名称和数值 |
| Error | ECharts 实例化失败 | 静默降级：展示占位区域 + 提示"图表加载失败" |

---

### 新组件 3: StatCard（src/components/business/StatCard.vue）

| 项 | 内容 |
|------|------|
| 组件职责 | 统计数字卡片，含图标、标签、数值。Loading 时展示骨架屏 |
| 复用场景 | 学生仪表盘、教师仪表盘、管理员数据大盘 |

**Props**:

| Prop | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| label | string | 是 | "" | 卡片标签文字，如"待完成任务" |
| value | number \| string | 是 | 0 | 卡片数值。字符串类型支持格式化后的文本（如"85 分"） |
| icon | string | 否 | "" | Lucide 图标名称，如 "Clock"、"CheckCircle"、"Award"、"Cpu"。通过动态组件渲染对应 Lucide 图标 |
| color | string | 否 | "#3B82F6" | 图标颜色和数值强调色（图标左侧装饰条颜色） |
| loading | boolean | 否 | false | 是否处于加载态。为 true 时展示骨架屏（灰色脉冲色块替代图标和数值） |

**Emits**:
无。StatCard 纯展示组件。

**Slots**:
无。

**Store 依赖**:
无。

**API 依赖**:
无。

**状态覆盖**:

| 状态 | 条件 | 表现 |
|------|------|------|
| Loading | loading=true | 图标区域展示 40x40px 灰色脉冲圆，数值区域展示 80x24px 灰色脉冲矩形，标签正常展示 |
| Success | loading=false, value 有效 | 图标左侧 4px 宽色条（color），图标 40px，数值大字（--font-size-3xl, JetBrains Mono），标签小字（--font-size-sm, --color-text-secondary） |
| Empty | value 为 0 或空字符串且非加载态 | 正常展示数值 0 或 "--"（由父组件决定传值） |

---

### 新组件 4: TaskStatusBadge（src/components/business/TaskStatusBadge.vue）

| 项 | 内容 |
|------|------|
| 组件职责 | 任务状态标签，7 种状态对应不同颜色和文案 |
| 复用场景 | 学生仪表盘近期任务、学生任务列表页、教师批阅列表 |

**Props**:

| Prop | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| status | string | 是 | "NOT_SUBMITTED" | 状态枚举值，可选: NOT_SUBMITTED / SUBMITTED / AI_EVALUATING / AI_COMPLETED / TEACHER_SCORING / COMPLETED / REJECTED |

**Emits**:
无。

**Slots**:
无。

**Store 依赖**:
无。

**API 依赖**:
无。

**状态映射表**（颜色格式为 背景色/文字色）:

| status 值 | 背景色 | 文字色 | 展示文案 |
|-----------|--------|--------|---------|
| NOT_SUBMITTED | #FEF3C7 | #92400E | 待提交 |
| SUBMITTED | #DBEAFE | #1E40AF | 已提交 |
| AI_EVALUATING | #EDE9FE | #6D28D9 | AI分析中 |
| AI_COMPLETED | #D1FAE5 | #065F46 | 待评分 |
| TEACHER_SCORING | #FFF7ED | #9A3412 | 评分中 |
| COMPLETED | #D1FAE5 | #065F46 | 已完成 |
| REJECTED | #FEE2E2 | #991B1B | 已退回 |

**未知 status 降级策略**:
当传入的 status 不在上述 7 种枚举值中时，展示灰色标签（背景 #F1F5F9，文字 #64748B），文案使用原始 status 值（作为 fallback 展示）。同时在控制台输出 warning 级别日志，格式为 `[TaskStatusBadge] Unknown status: {status}`。

---

## 3.3 已有组件引用

**引用组件 1: AppLayout（src/components/layout/AppLayout.vue）**

由路由配置外层包裹，DashboardPage 不需要手动引用。

**引用组件 2: PageHeader（src/components/common/PageHeader.vue）**

关键 Props 配置：

| Prop | 值 | 说明 |
|------|-----|------|
| title | computed: "欢迎回来，" + userStore.realName | 动态拼接欢迎语 |
| breadcrumb | [{ path: "/student/dashboard", label: "仪表盘" }, { path: "/student/dashboard", label: "首页" }] | 面包屑数组 |
| description | computed: 当天日期格式化字符串 | 如"2026年7月3日 星期五" |

**引用组件 3: BaseTable（src/components/base/BaseTable.vue）**

关键 Props 配置：

| Prop | 值 | 说明 |
|------|-----|------|
| dataSource | recentTasks (Array) | 近期任务列表，最多 5 条 |
| columns | 列配置数组 | 见下方 columns 配置 |
| stripe | true | 斑马纹 |
| hover | true | 行 hover 高亮 |
| border | true | 外边框 |
| maxHeight | 400 | 最大高度 400px，超出内部滚动 |

columns 配置详情：

| 列 key | 标题 | 宽度 | 对齐 | 自定义渲染 |
|--------|------|------|------|-----------|
| taskName | 任务名称 | min-width: 200px | left | 无，纯文本 |
| courseName | 所属课程 | width: 160px | left | 无，纯文本 |
| deadline | 截止日期 | width: 140px | center | 格式化 YYYY-MM-DD。若 deadline 已过且 status 为 NOT_SUBMITTED，文字颜色改为 --color-danger |
| status | 状态 | width: 120px | center | 使用 TaskStatusBadge 组件，slot: status |
| score | 成绩 | width: 100px | center | 字体 JetBrains Mono。若 score 为 null/undefined，展示 "--"（颜色 --color-text-placeholder） |

**引用组件 4: LoadingState（src/components/common/LoadingState.vue）**

页面级 Loading 骨架。仪表盘全页 Loading 时替代整个内容区展示居中 Spinner + "加载中..."

**引用组件 5: ErrorState（src/components/common/ErrorState.vue）**

页面级错误展示。Dashboard API 调用失败时展示错误图标 + 错误信息 + "重新加载"按钮。

**引用组件 6: EmptyState（src/components/common/EmptyState.vue）**

空数据展示。表格空列表或图表无数据时使用。Props 包括 message 和可选的 actionButton。

---

# Section 4: State Design（页面级）

| 状态 | 触发条件 | UI 表现 | 持续时间 | 退出条件 |
|------|---------|--------|---------|---------|
| Loading | 页面首次进入，`onMounted` 中发起 `GET /api/v1/student/dashboard` 和 `GET /api/v1/auth/me`，任一 API 未返回 | 页面内容区展示 LoadingState 组件（居中 Spinner + "加载中..."）。Navbar 正常展示（不进入 Loading）。4 个 StatCard 展示骨架屏（loading=true）。图表区域展示占位灰色矩形（320px 高）。表格区域展示骨架屏（3 行灰色脉动矩形） | 直到两个 API 均返回（成功或失败）。Mock 延迟约 500ms，超时时间 15s | 两个 API 均完成。若 auth/me 先返回，PageHeader 的欢迎语先展示（title 更新为真实姓名），其余区域继续 Loading。若 dashboard 先返回，所有区域同时进入对应状态 |
| Empty | Dashboard API 返回成功，各项数据均为空/零值：(a) recentTasks 为空数组；(b) scoreTrend.xAxis 为空数组；(c) radarData.categories 为空数组且 stats 全为 0 | PageHeader 正常展示欢迎语和日期。4 个 StatCard 展示数值 0（非 Loading 骨架）。折线图区域展示 EmptyState（"暂无趋势数据"）。雷达图区域展示 EmptyState（"暂无维度数据"）。表格区域展示 EmptyState（"暂无任务记录"），可选 actionButton："去完成第一个任务"（跳转 /student/tasks） | 持续直到用户有任务数据（完成首次提交后数据更新） | 用户刷新页面且此时后端有数据，或用户进入有数据的其他页面再返回 |
| Error | Dashboard API 返回 code != 0，或网络超时（15s），或 auth/me 接口失败 | 若 auth/me 失败：PageHeader title 降级为"欢迎回来"（不拼接 realName）。若 dashboard 接口失败：StatCard Row 替换为 ErrorState 组件（居中展示，error icon + 错误消息 + "重新加载"按钮）。图表区域和表格区域不渲染。错误消息根据 code 值展示：(a) code=5001 → "服务繁忙，请稍后重试"；(b) 网络错误 → "网络连接失败，请检查网络"；(c) 其他 → 使用后端返回的 message 字段 | 直到用户点击"重新加载"按钮或手动刷新页面 | 用户点击"重新加载"→ 重新调用 dashboard API，进入 Loading 状态。或用户手动 F5 刷新 |
| Success | Dashboard API 返回成功（code=0），各项数据填充完整 | PageHeader 展示真实姓名。4 个 StatCard 展示具体数值。折线图渲染 ECharts 实例，hover 展示 tooltip。雷达图渲染 ECharts 实例。表格展示最多 5 条近期任务（含 TaskStatusBadge）。所有数值字体使用 JetBrains Mono，表格成绩列 "--" 表示未评分 | 持续直到用户离开页面或手动刷新 | 用户导航离开（组件卸载）或再次刷新 |
| NoPermission | 非 student 角色用户尝试访问 /student/dashboard。路由守卫在 beforeEach 中检测 User Store 的 role !== "student" | 不渲染 DashboardPage。路由守卫调用 router.replace({ name: "NoPermission" }) 或重定向到角色首页。若守卫被绕过（理论上不应当），页面内展示 NoPermission 组件 | - | - |
| Offline | 浏览器 navigator.onLine 变为 false | Navbar 下方展示黄色全局提示条（宽度 100%，高度 44px，bg #FEF3C7，文字 #92400E，内容"网络连接已断开，数据可能不是最新"）。已加载的区域保持可见（数据为上次缓存）。图表仍可交互（ECharts 不依赖网络）。表格数据保持。不阻塞用户浏览已加载数据 | 直到网络恢复 | navigator.onLine 变为 true，提示条自动消失。全局提示条通过 AppLayout 级别管理，DashboardPage 内通过 provide/inject 或 Store 感知 |

---

# Section 5: Data Flow

## 5.1 页面加载数据流

```
用户导航到 /student/dashboard
Step 1: 路由守卫 beforeEach 触发
  → auth.ts: 检查 Token 有效性
  → Token 无效 → 重定向 /login，终止
  → Token 有效，role 非 student → 重定向角色首页，终止
  → Token 有效，role 为 student → 放行

Step 2: DashboardPage.vue 开始渲染
  → Loading State: 全页展示 LoadingState
  → onMounted 执行:
    Step 2a: 并行发起两个 API 请求
      → Promise.all([
          GET /api/v1/student/dashboard,
          GET /api/v1/auth/me
        ])
    Step 2b: 注册 window 'online' / 'offline' 事件监听
    Step 2c: 从 Pinia Store 读取 userInfo 作为降级数据源（如 realName 降级）

Step 3: API 响应处理
  → /api/v1/auth/me 返回成功:
    → dashboardStore.userName = response.data.realName
    → PageHeader title 更新为 "欢迎回来，{realName}"
  → /api/v1/student/dashboard 返回成功:
    → dashboardStore.stats = response.data.stats
    → dashboardStore.recentTasks = response.data.recentTasks
    → dashboardStore.scoreTrend = response.data.scoreTrend
    → dashboardStore.radarData = response.data.radarData
    → Loading 退出 → Success 状态

Step 4: 渲染各区域
  → StatCard 区域: 绑定 stats 各字段，loading=false
  → LineChart 区域: 绑定 scoreTrend.xAxis 和 scoreTrend 的 myScores/classAvg
  → RadarChart 区域: 绑定 radarData.categories 和 myScores/classAvg
  → BaseTable 区域: 绑定 recentTasks，status 列使用 TaskStatusBadge 组件
```

## 5.2 用户交互数据流 - 点击"重新加载"

```
Given: Dashboard API 调用失败，页面处于 Error 状态
When: 用户点击 ErrorState 组件中的"重新加载"按钮
Then:
  → ErrorState 发出 @retry 事件
  → DashboardPage.handleRetry() 执行
  → 进入 Loading 状态（StatCard 骨架屏，图表区域灰色占位）
  → 重新调用 GET /api/v1/student/dashboard
  → 成功 → 进入 Success 状态，各区域渲染数据
  → 失败 → 回到 Error 状态，ErrorState 展示新错误信息
```

## 5.3 用户交互数据流 - 点击"查看全部"跳转任务列表

```
Given: Dashboard 页面处于 Success 状态，表格展示了 5 条近期任务
When: 用户点击"近期任务"区域头部的"查看全部"链接
Then:
  → router.push("/student/tasks")
  → 导航到学生任务列表页（/student/tasks 页面组件处理后续逻辑）
  → DashboardPage 组件被 keep-alive 缓存（若路由配置了 keepAlive）
```

## 5.4 用户交互数据流 - 图表 Legend 交互

```
Given: 折线图和雷达图正常渲染，Legend 显示"我的成绩"和"班级平均"
When: 用户点击 Legend 中的"班级平均"（隐藏该系列）
Then:
  → ECharts 实例内部处理 series 显隐（不通过 Vue 数据流）
  → "班级平均"折线/多边形从图表区域消失
  → 图例中"班级平均"文字变为灰色半透明（ECharts 默认 legend 行为）
  → Y 轴或雷达轴范围可能重新计算（根据 ECharts 配置）
When: 用户再次点击 Legend 中的"班级平均"
Then:
  → 该系列恢复显示
  → 图例文字恢复原始颜色
```

## 5.5 用户交互数据流 - 网络断开后恢复

```
Given: Dashboard 页面处于 Success 状态，浏览器网络断开
When: 页面展示黄色离线提示条，数据保留
When: 网络恢复（navigator.onLine 变为 true）
Then:
  → 全局离线提示条自动消失
  → DashboardPage 不需要重新请求数据（已加载数据仍然有效）
  → 若用户在离线期间点击了"查看全部"，跳转到 /student/tasks 后由该页面处理离线状态
```

---

# Section 6: API & Mock Specification

## 接口 1: GET /api/v1/student/dashboard

用途: 获取学生仪表盘全体数据（统计、近期任务、趋势、雷达）

请求方式: GET

请求参数: 无（当前版本不需要分页或筛选参数。Dashboard 数据由后端按当前登录用户自动聚合）

请求 Header: Authorization: Bearer {token}

响应数据结构:

```
{
  code: number,
  message: string,
  data: {
    stats: {
      pendingTasks: number,      // 待完成任务数
      completedTasks: number,    // 已完成任务数
      averageScore: number,      // 平均成绩（0-100，保留1位小数）
      analyzingCount: number     // 当前 AI 分析中的任务数
    },
    recentTasks: Array<{
      id: number,                // 任务 ID
      taskName: string,          // 任务名称
      courseName: string,        // 所属课程名称
      deadline: string,          // 截止日期（ISO 8601 格式: YYYY-MM-DDTHH:mm:ss）
      status: string,            // 任务状态: NOT_SUBMITTED / SUBMITTED / AI_EVALUATING / AI_COMPLETED / TEACHER_SCORING / COMPLETED / REJECTED
      score: number | null       // 最终成绩（0-100），未评分时为 null
    }>,
    scoreTrend: {
      xAxis: string[],           // X 轴月份标签，如 ["1月","2月","3月","4月","5月","6月","7月"]
      myScores: number[],        // 我的月均成绩数组（0-100）
      classAvg: number[]         // 班级月均成绩数组（0-100）
    },
    radarData: {
      categories: string[],      // 维度名称数组，6 个维度: ["代码质量","需求理解","项目管理","团队协作","文档规范","创新能力"]
      myScores: number[],        // 我的各维度得分（0-100），顺序与 categories 一致
      classAvg: number[]         // 班级各维度均分（0-100）
    }
  } | null,
  success: boolean,
  timestamp: string,
  traceId: string
}
```

Mock 数据示例 1（正常数据 - 有丰富数据的学生）:

```
{
  "code": 0,
  "message": "success",
  "data": {
    "stats": {
      "pendingTasks": 3,
      "completedTasks": 12,
      "averageScore": 86.5,
      "analyzingCount": 1
    },
    "recentTasks": [
      {
        "id": 101,
        "taskName": "图书管理系统需求分析文档",
        "courseName": "软件工程导论",
        "deadline": "2026-07-10T23:59:59",
        "status": "AI_EVALUATING",
        "score": null
      },
      {
        "id": 100,
        "taskName": "在线商城数据库设计",
        "courseName": "数据库原理",
        "deadline": "2026-06-28T23:59:59",
        "status": "COMPLETED",
        "score": 92
      },
      {
        "id": 99,
        "taskName": "简单计算器单元测试",
        "courseName": "软件测试",
        "deadline": "2026-06-20T23:59:59",
        "status": "COMPLETED",
        "score": 88
      },
      {
        "id": 98,
        "taskName": "团队协作 Git 工作流实践",
        "courseName": "软件工程导论",
        "deadline": "2026-06-15T23:59:59",
        "status": "COMPLETED",
        "score": 85
      },
      {
        "id": 97,
        "taskName": "Spring Boot 入门项目",
        "courseName": "Java 企业级开发",
        "deadline": "2026-07-15T23:59:59",
        "status": "NOT_SUBMITTED",
        "score": null
      }
    ],
    "scoreTrend": {
      "xAxis": ["1月","2月","3月","4月","5月","6月","7月"],
      "myScores": [78, 82, 85, 88, 90, 92, null],
      "classAvg": [72, 74, 76, 78, 80, 82, null]
    },
    "radarData": {
      "categories": ["代码质量","需求理解","项目管理","团队协作","文档规范","创新能力"],
      "myScores": [90, 85, 78, 92, 88, 82],
      "classAvg": [78, 80, 75, 82, 80, 76]
    }
  },
  "success": true,
  "timestamp": "2026-07-03T09:30:00+08:00",
  "traceId": "trace-dashboard-mock-001"
}
```

Mock 数据示例 2（空数据 - 新生刚入学，无任何任务记录）:

```
{
  "code": 0,
  "message": "success",
  "data": {
    "stats": {
      "pendingTasks": 0,
      "completedTasks": 0,
      "averageScore": 0,
      "analyzingCount": 0
    },
    "recentTasks": [],
    "scoreTrend": {
      "xAxis": [],
      "myScores": [],
      "classAvg": []
    },
    "radarData": {
      "categories": [],
      "myScores": [],
      "classAvg": []
    }
  },
  "success": true,
  "timestamp": "2026-07-03T09:30:00+08:00",
  "traceId": "trace-dashboard-mock-002"
}
```

Mock 数据示例 3（边缘情况 - 有趋势数据但近期无任务，或趋势 xAxis 中有 null 值月份）:

```
{
  "code": 0,
  "message": "success",
  "data": {
    "stats": {
      "pendingTasks": 0,
      "completedTasks": 8,
      "averageScore": 84.0,
      "analyzingCount": 0
    },
    "recentTasks": [],
    "scoreTrend": {
      "xAxis": ["1月","2月","3月","4月","5月","6月","7月"],
      "myScores": [80, 82, null, 85, null, 90, null],
      "classAvg": [76, 78, 79, 80, 82, 83, null]
    },
    "radarData": {
      "categories": ["代码质量","需求理解","项目管理","团队协作","文档规范","创新能力"],
      "myScores": [88, 84, 76, 90, 86, 80],
      "classAvg": [76, 78, 74, 80, 78, 74]
    }
  },
  "success": true,
  "timestamp": "2026-07-03T09:30:00+08:00",
  "traceId": "trace-dashboard-mock-003"
}
```

Mock 错误场景:
- 5001 服务端错误: { "code": 5001, "message": "服务器繁忙，请稍后重试", "data": null, "success": false, "timestamp": "2026-07-03T09:30:00+08:00", "traceId": "trace-dashboard-err-001" }
- 2001 未认证 (Token 过期): { "code": 2001, "message": "认证已过期，请重新登录", "data": null, "success": false, "timestamp": "2026-07-03T09:30:00+08:00", "traceId": "trace-dashboard-err-002" }
- Network Error: Axios 捕获网络错误，无响应体。触发条件：Mock 层随机 10% 概率模拟网络超时

Mock 延迟: 600ms（比登录多 100ms，模拟聚合查询的复杂度）

Mock 实现位置: src/mock/dashboard.ts

Mock 逻辑: 根据 Authorization Header 中的 Token 解析用户名（Mock 层从 Token 中 mock 解析 username），返回对应用户的 Mock 数据。若 Token 包含 "empty" 关键字则返回空数据 Mock 2。若 Token 包含 "error" 关键字则返回 5001 错误。默认返回正常数据 Mock 1。

---

## 接口 2: GET /api/v1/auth/me

用途: 获取当前登录用户信息（仅用于欢迎语中的 realName）

请求方式: GET

请求参数: 无

请求 Header: Authorization: Bearer {token}

响应数据结构:

```
{
  code: number,
  message: string,
  data: {
    id: number,
    username: string,
    realName: string,
    role: string,
    avatar: string,
    email: string,
    phone: string
  } | null,
  success: boolean,
  timestamp: string,
  traceId: string
}
```

Mock 数据示例 1（学生用户）:

```
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "username": "student01",
    "realName": "张三",
    "role": "student",
    "avatar": "",
    "email": "zhangsan@example.com",
    "phone": "13800138001"
  },
  "success": true,
  "timestamp": "2026-07-03T09:30:00+08:00",
  "traceId": "trace-me-mock-001"
}
```

Mock 错误场景:
- 2001 Token 过期: { "code": 2001, "message": "认证已过期，请重新登录", "data": null, "success": false, "timestamp": "2026-07-03T09:30:00+08:00", "traceId": "trace-me-err-001" }

Mock 延迟: 200ms

Mock 实现位置: src/mock/auth.ts（与登录 Mock 同文件）

---

# Section 7: Interaction Flows

## 交互 1: 首次登录后进入仪表盘（正常完整加载）

Given: 学生张三（student01）刚完成登录，Token 已存储到 User Store
When:
1. 登录页 1.5 秒后执行 router.push("/student/dashboard")
2. DashboardPage 组件挂载，onMounted 触发
3. 并行发起 GET /api/v1/student/dashboard 和 GET /api/v1/auth/me

Then:
1. 初始展示 LoadingState（全页居中 Spinner + 4 个 StatCard 骨架屏）
2. auth/me 先返回（200ms Mock 延迟），PageHeader title 更新为"欢迎回来，张三"
3. dashboard API 随后返回（600ms Mock 延迟），4 个 StatCard 更新为具体数值（待完成任务 3，已完成 12，平均 86.5 分，AI 分析中 1）
4. 折线图渲染 1-7 月成绩趋势（我的成绩蓝色折线 + 班级平均灰色折线）
5. 雷达图渲染六维能力对比（我的得分蓝色 + 班级平均灰色）
6. 近期任务表格展示 5 条记录，每条含 TaskStatusBadge 状态标签
7. 所有数值列使用 JetBrains Mono 字体

## 交互 2: 新生首次进入仪表盘（空数据）

Given: 新生李四（student99）刚入学，未分配任何课程和任务
When:
1. 登录后跳转 /student/dashboard
2. Dashboard API 返回空数据（Mock 2: stats 全 0，recentTasks []，趋势 xAxis []，雷达 categories []）

Then:
1. PageHeader 正常展示"欢迎回来，李四"
2. 4 个 StatCard 展示数值 0（loading=false，正常展示而非骨架屏）
3. 折线图区域展示 EmptyState（message="暂无趋势数据"）
4. 雷达图区域展示 EmptyState（message="暂无维度数据"）
5. 表格区域展示 EmptyState（message="暂无任务记录"，actionButton text="去完成第一个任务"，点击跳转 /student/tasks）

## 交互 3: 网络请求失败后重试

Given: 学生张三已登录，但 Dashboard API 调用因网络波动失败
When:
1. 页面展示 ErrorState（error icon + "网络连接失败，请检查网络" + "重新加载"按钮）
2. 用户等待片刻后网络恢复
3. 用户点击"重新加载"按钮

Then:
1. ErrorState 替换为 LoadingState
2. 重新调用 GET /api/v1/student/dashboard
3. 此次调用成功返回数据
4. LoadingState 替换为 Success 状态，所有区域渲染数据
5. ElMessage.success("数据加载成功") 不展示（用户手动重试不需要成功提示，数据自然出现即可）

## 交互 4: 折线图 Tooltip 交互

Given: 折线图正常渲染了 1-7 月成绩数据，我的成绩蓝色折线上 7 月数据为 null（该月尚未结束）
When:
1. 用户将鼠标悬停在折线图 6 月数据点上

Then:
1. ECharts tooltip 浮层展示："2026年6月" + "我的成绩: 92 分" + "班级平均: 82 分"
2. 十字准星线（axisPointer type: cross）辅助定位
3. 当用户将鼠标移至 7 月位置时，tooltip 不展示 null 数据点（ECharts connectNulls: false 默认行为，null 值不连线也不展示数据点）
4. Tooltip 浮层字体使用系统默认（ECharts 原生，不强制 JetBrains Mono）

## 交互 5: 表格行点击跳转成绩详情

Given: 近期任务表格展示了 5 条记录，第 2 条是 COMPLETED 状态的任务（id=100，成绩 92）
When:
1. 用户点击表格中 id=100 的任务行

Then:
1. 该行在 click 事件前有 hover 高亮（--color-primary-light 背景，BaseTable hover 属性自动处理）
2. router.push("/student/grades/100")
3. 导航到学生成绩详情页，URL 中 submissionId=100
4. DashboardPage 被 keep-alive 缓存（若路由配置）、不会重新挂载

## 交互 6: 页面在后台 Tab 中打开，焦点回来后数据保持

Given: 学生张三在 Dashboard 页面，数据已正常展示
When:
1. 用户打开新 Tab 访问其他网站
2. 5 分钟后切回 Dashboard Tab

Then:
1. 页面数据保持不变（Vue 响应式数据未被销毁）
2. 不自动刷新（MVP 阶段不做自动刷新。定时刷新属于 Sprint 4 实时通知 Feature）
3. 若用户在后台期间网络断开，切回时离线提示条已展示（全局监听，切换 Tab 不改变离线状态）
4. 用户可手动 F5 刷新获取最新数据

---

# Section 8: Permission Design

| 属性 | 值 |
|------|-----|
| 页面权限标识 | student:dashboard |
| 页面允许角色 | ["student"] |
| 路由权限检查 | meta.roles = ["student"] |

| 权限点 | 类型 | 允许角色 | 说明 |
|--------|------|---------|------|
| 浏览仪表盘 | 页面访问 | student | 学生角色可访问本页面全部数据 |
| "查看全部"链接 | 导航 | student | 跳转到 /student/tasks，该页面有独立权限检查 |
| "重新加载"按钮 | 操作 | student | 在 ErrorState 中展示，所有学生可用 |
| "去完成第一个任务"按钮 | 操作 | student | 在 EmptyState 中展示，跳转 /student/tasks |
| StatCard 数据 | 数据可见 | student | 仅展示当前学生的个人数据，不涉及跨学生数据 |

**权限异常处理**:
- 路由守卫检测 role !== "student" → 重定向到对应角色首页，不渲染 DashboardPage
- 若守卫被绕过（编辑 localStorage 篡改 role），后端 API 返回的角色数据仍为真实角色 → 前端 show 的数据可能是其他角色的，但 MVP 阶段不做前端 role 二次校验（由后端保证数据隔离）

---

# Section 9: Acceptance Criteria

AC-1: Given 学生张三完成登录 When 自动跳转到 /student/dashboard Then 页面展示 (a) 顶部导航栏；(b) PageHeader 显示"欢迎回来，张三"和当天日期；(c) 4 个 StatCard 展示待完成任务数、已完成任务数、平均成绩、AI 分析中任务数；(d) 折线图展示 1-7 月成绩趋势（含班级平均对比线）；(e) 雷达图展示六维能力对比；(f) 近期任务表格展示最多 5 条记录含 TaskStatusBadge 状态标签

AC-2: Given 新学生无任何任务数据 When 访问 Dashboard Then 4 个 StatCard 均展示 0。折线图和雷达图区域展示 EmptyState（"暂无趋势数据"/"暂无维度数据"）。表格区域展示 EmptyState（"暂无任务记录"）并包含"去完成第一个任务"按钮，点击跳转 /student/tasks

AC-3: Given Dashboard API 调用失败（网络错误或 5001）When 页面加载 Then StatCard 区域不展示。图表和表格区域不渲染。页面中央展示 ErrorState（error icon + 错误消息 + "重新加载"按钮）。点击"重新加载"后重新调用 API

AC-4: Given Dashboard 正常展示 When 用户点击"近期任务"区域头部的"查看全部"链接 Then 导航到 /student/tasks 学生任务列表页

AC-5: Given Dashboard 正常展示 When 用户将鼠标悬停在折线图数据点上 Then ECharts tooltip 浮层展示该月的"我的成绩"和"班级平均"具体数值，并使用十字准星辅助线

AC-6: Given Dashboard 正常展示 When 用户点击近期任务表格中某条 COMPLETED 状态的任务行 Then 导航到 /student/grades/{submissionId} 成绩详情页

AC-7: Given 浏览器网络断开 When 用户正在浏览 Dashboard Then Navbar 下方展示黄色离线提示条"网络连接已断开，数据可能不是最新"。已加载的数据和图表保持可见。网络恢复后提示条自动消失

---

# Section 10: Files to Create / Modify

## 新建文件

```
├── src/pages/student/DashboardPage.vue           # 学生仪表盘页面组件
├── src/components/chart/LineChart.vue             # 通用折线图组件（ECharts 封装）
├── src/components/chart/RadarChart.vue            # 通用雷达图组件（ECharts 封装）
├── src/components/business/StatCard.vue           # 统计数字卡片组件
├── src/components/business/TaskStatusBadge.vue    # 任务状态标签组件
├── src/api/dashboard.ts                           # Dashboard API 函数（fetchStudentDashboard）
├── src/api/auth.ts                                # Auth API 函数（fetchCurrentUser /auth/me）
├── src/stores/useDashboardStore.ts                # Dashboard 数据 Pinia Store
├── src/mock/dashboard.ts                          # Dashboard 接口 Mock 数据
├── src/mock/auth.ts                               # Auth 接口 Mock 数据（含 /auth/me, /auth/login）
├── src/mock/index.ts                              # Mock 汇总入口
└── docs/page-analysis/student-dashboard.md        # 本文件
```

## 修改文件

```
├── src/router/routes/student.ts                   # 追加 Dashboard 路由（/student/dashboard）
├── src/router/index.ts                            # 注册 Student Dashboard 路由
├── src/types/dashboard.ts                         # 新建：Dashboard 响应类型定义（IStudentDashboardResponse, IDashboardStats, IRecentTask, IScoreTrend, IRadarData）
└── src/main.ts                                    # 确认 Mock 引入逻辑和 ECharts 依赖注册
```

## 依赖的 Sprint 1 组件（本页面不修改它们，但依赖它们的存在）

```
├── src/components/layout/AppLayout.vue            # 页面外层布局（Navbar + PageContainer）
├── src/components/common/PageHeader.vue            # 页面标题栏（title + breadcrumb + description）
├── src/components/base/BaseTable.vue               # 近期任务表格
├── src/components/common/LoadingState.vue          # 全页 Loading 骨架
├── src/components/common/ErrorState.vue            # 全页错误展示（含 retry 按钮）
├── src/components/common/EmptyState.vue            # 空数据展示（含 actionButton）
```

## 注意事项

1. DashboardPage 是学生登录后的落地页，需要优先实现。在 Sprint 2 开发顺序中排在 Base 组件和 Chart 组件完成之后。
2. LineChart 和 RadarChart 基于 ECharts 封装，需要在 package.json 中添加 echarts 和 vue-echarts（或直接使用 echarts 原生 API + ref）。MVP 阶段推荐直接使用 echarts 原生 init API 保证灵活性。
3. 趋势数据中的 null 值表示该月份尚无数据（如当前月份未结束）。ECharts 的 connectNulls 默认为 false，null 值不会连线也不会展示数据点。前端不需要特殊处理 null —— 直接传给 ECharts 即可。
4. 表格行点击跳转成绩详情使用 @row-click 事件，通过 row.status 判断：仅 COMPLETED、AI_COMPLETED、TEACHER_SCORING 状态的行可点击（已有成绩或即将有成绩）。NOT_SUBMITTED、SUBMITTED、AI_EVALUATING 状态的行点击不跳转（无成绩可查看）。
5. PageHeader 的实时日期使用 computed 属性 + new Date()，不使用后端返回的 timestamp。日期在页面挂载时计算一次，不会随时间自动更新（避免性能浪费）。
6. 本页面不涉及 WebSocket 实时推送。所有数据在页面首次加载时获取一次。后续实时更新功能归入 Sprint 4。
