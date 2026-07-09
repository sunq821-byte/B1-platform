# Page Analysis: 学生任务列表页 (Student Task List)

**文档版本**: v1.0
**创建日期**: 2026-07-03
**分析范围**: 学生端任务列表页面及完整筛选分页交互

---

# Section 1: Page Identity

| 属性 | 值 |
|------|-----|
| 页面名称 | 学生任务列表页 |
| 页面文件 | src/pages/student/TaskListPage.vue |
| 路由路径 | /student/tasks |
| 路由名称 | StudentTasks |
| 所属 Sprint | Sprint 2 |
| 所属 Feature | F-2-02（学生任务列表） |
| 页面角色 | 学生（Student） |
| 页面复杂度 | L2（标准表格页面，含筛选、分页、状态标签组件） |
| 原型参考 | prototypes/student/task-list/ 目录下原型文件 |

页面职责：学生登录后查看分配到自己的全部实训任务。支持按提交状态筛选、按关键词搜索、分页浏览。每个任务显示当前提交状态并提供"查看详情"入口。

---

# Section 2: Page Layout Structure

学生任务列表页嵌套在 AppLayout 中，使用 PageHeader + PageContainer 标准布局。页面从上到下分为导航区、标题区、筛选区、表格区、分页区五个区域。

```
+--------------------------------------------------+
|  AppLayout (全屏高度, flex column)                  |
|  +----------------------------------------------+ |
|  |  TopNav 顶部导航栏 (height: 56px)               | |
|  |  bg: #FFFFFF, border-bottom: 1px #E2E8F0      | |
|  +----------------------------------------------+ |
|  +----------------------------------------------+ |
|  |  PageContainer (flex: 1, overflow-y: auto)     | |
|  |  bg: #F8FAFC, padding: --spacing-lg            | |
|  |                                                | |
|  |  +------------------------------------------+  | |
|  |  |  PageHeader (height: auto, mb: --spacing-lg)| |
|  |  |  title: "我的任务", icon: ListChecks       |  | |
|  |  |  description: "查看和管理所有分配的实训任务"  |  | |
|  |  +------------------------------------------+  | |
|  |                                                | |
|  |  +------------------------------------------+  | |
|  |  |  FilterBar 筛选区 (height: auto, 48px)      | |
|  |  |  bg: #FFFFFF, border-radius: --radius-md   |  | |
|  |  |  padding: --spacing-md, mb: --spacing-md   |  | |
|  |  |  display: flex, align-items: center,       |  | |
|  |  |  gap: --spacing-md                          |  | |
|  |  |  +----------+  +----------------+  +------+|  | |
|  |  |  |BaseSelect|  |BaseInput(keyword|  |BaseB ||  | |
|  |  |  |(提交状态) |  |搜索框)         |  |utton ||  | |
|  |  |  |w: 200px  |  |w: 280px        |  |(搜索)||  | |
|  |  |  +----------+  +----------------+  +------+|  | |
|  |  +------------------------------------------+  | |
|  |                                                | |
|  |  +------------------------------------------+  | |
|  |  |  Table 表格区 (height: auto, flex: 1)       | |
|  |  |  bg: #FFFFFF, border-radius: --radius-md   |  | |
|  |  |  padding: --spacing-md                      |  | |
|  |  |  min-height: 400px（无数据时至少占位）       |  | |
|  |  |                                              | |
|  |  |  BaseTable（7 列）:                           | |
|  |  |  taskName | courseName | deadline |         | |
|  |  |  totalScore | submissionType |             | |
|  |  |  mySubmissionStatus(TaskStatusBadge) |      | |
|  |  |  action("查看详情"按钮)                       | |
|  |  +------------------------------------------+  | |
|  |                                                | |
|  |  +------------------------------------------+  | |
|  |  |  Pagination 分页区 (height: auto, 52px)     | |
|  |  |  bg: #FFFFFF, border-radius: --radius-md   |  | |
|  |  |  padding: --spacing-md, mt: --spacing-md   |  | |
|  |  |  display: flex, justify-content: flex-end  |  | |
|  |  |  BasePagination（page, pageSize, total）    |  | |
|  |  +------------------------------------------+  | |
|  +----------------------------------------------+ |
+--------------------------------------------------+
```

**区域说明**：

| 区域 | 宽度 | 高度 | 对齐 | 滚动策略 | 说明 |
|------|------|------|------|---------|------|
| TopNav | 100% | 56px (固定) | flex | 不滚动 | 使用 AppLayout 内置顶部导航栏，不在本页面单独处理 |
| PageContainer | 100% | flex: 1 | - | y轴滚动(overflow-y: auto) | 整体内容区。内容不足一屏时不滚动，表格数据过多时滚动 |
| PageHeader | max-content(1200px)居中 | auto | 左对齐 | 不滚动 | 标题"我的任务"，图标使用 Lucide Icons 的 ListChecks。描述文字在标题下方，单行 |
| FilterBar | max-content(1200px)居中 | auto (最小48px) | flex,水平居中 | 不滚动 | 三个子元素在一行水平排列。小屏时换行（flex-wrap）。BaseSelect 宽度 200px，BaseInput 宽度 280px，BaseButton 自适应 |
| Table | max-content(1200px)居中 | auto | 左对齐 | 不滚动（表格内部行数据由分页控制） | 使用 BaseTable 组件。7列布局，列宽自适应。操作列固定宽度 120px，不换行 |
| Pagination | max-content(1200px)居中 | auto (最小52px) | flex-end | 不滚动 | BasePagination 组件右对齐 |

**响应式行为**：
- 1920px 分辨率：内容区最大宽度 1200px 居中，两侧留白
- 1366px 分辨率：内容区最大宽度 1200px 居中，仍有足够空间展示 7 列表格
- 小于 1024px（平板）：FilterBar 换行，BaseSelect 和 BaseInput 各占一行。表格启用横向滚动（el-table 原生支持）
- 小于 768px（手机）：FilterBar 元素全部纵向堆叠，宽度 100%。表格横向滚动。分页组件简化

---

# Section 3: Component Tree & Specification

## 3.1 组件树

```
TaskListPage.vue（页面组件，嵌套在 AppLayout 中）
├── PageHeader
│   props: { title: "我的任务", icon: "ListChecks", description: "查看和管理所有分配的实训任务" }
├── <div> FilterBar 筛选栏容器（纯 div 布局，无独立组件封装）
│   ├── BaseSelect（v-model="filters.status"）
│   │   props: { placeholder: "提交状态", clearable: true, options: statusOptions }
│   ├── BaseInput（v-model="filters.keyword"）
│   │   props: { placeholder: "搜索任务名称/课程名称", clearable: true, @keyup.enter: handleSearch }
│   └── BaseButton
│       props: { type: "primary", @click: handleSearch, innerText: "搜索" }
├── LoadingState（v-if="loading"）
│   props: { text: "正在加载任务列表..." }
├── ErrorState（v-else-if="error"）
│   props: { message: errorMessage, @retry: fetchTasks }
├── EmptyState（v-else-if="taskList.length === 0"）
│   props: { description: "暂无任务数据", image: "empty-task" }
├── <div v-else> 表格区域容器
│   └── BaseTable
│       props: { data: taskList, columns: columns, loading: loading }
│       └── 列 "mySubmissionStatus" 的 Cell Slot
│           └── TaskStatusBadge（status=row.mySubmissionStatus）
│       └── 列 "action" 的 Cell Slot
│           └── BaseButton（type="primary", size="small", text: true → 链接样式）
│               props: { @click: router.push(`/student/tasks/${row.taskId}`) }
│               innerText: "查看详情"
└── BasePagination（v-if="taskList.length > 0"）
    props: { currentPage: filters.page, pageSize: filters.pageSize, total: pagination.total, @change: handlePageChange }
```

## 3.2 本页面需要新建的子组件

### 3.2.1 TaskStatusBadge（状态标签组件）

文件路径: src/components/common/TaskStatusBadge.vue

组件类型: Common 通用组件（在多个页面复用，包括学生列表、教师列表、管理员列表）

组件职责: 根据任务提交状态枚举值渲染对应颜色和文案的标签。7 种状态各有独立的背景色和文字色。

**Props 规格**:

| Prop | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| status | string | 是 | - | 提交状态枚举值，取值范围：NOT_SUBMITTED / SUBMITTED / AI_EVALUATING / AI_COMPLETED / TEACHER_SCORING / COMPLETED / REJECTED |
| size | string | 否 | 'default' | 标签尺寸：'small'（表格内用）/ 'large'（详情页用）/ 'default'。small 对应 font-size: --font-size-xs, padding: 2px 8px。large 对应 font-size: --font-size-md, padding: 6px 16px。default 取 small |
| label | string | 否 | '' | 自定义文案。为空时使用 statusMap 默认映射文案。非空时使用自定义文案覆盖 |

**Emits 规格**:

| Emit | 参数 | 触发时机 | 说明 |
|------|------|---------|------|
| 无 | - | - | 纯展示组件，无交互事件 |

**Slots 规格**:

| Slot | Props | 说明 |
|------|-------|------|
| default | - | 无默认插槽。如需自定义内容使用 label prop |

**Store 依赖**:

| Store | Read | Write | 说明 |
|-------|------|-------|------|
| 无 | - | - | 纯展示组件，无 Store 依赖 |

**API 依赖**:

| API | 用途 | 说明 |
|-----|------|------|
| 无 | - | 纯展示组件，无 API 依赖 |

**颜色映射表（statusMap）**:

| 状态枚举值 | 中文文案 | 背景色 (CSS) | 文字色 (CSS) | 说明 |
|-----------|---------|-------------|-------------|------|
| NOT_SUBMITTED | 未提交 | #FEF3C7 | #92400E | 学生尚未提交任务 |
| SUBMITTED | 已提交 | #DBEAFE | #1E40AF | 学生已提交，等待 AI 分析 |
| AI_EVALUATING | AI 分析中 | #EDE9FE | #6D28D9 | AI 正在分析提交内容 |
| AI_COMPLETED | AI 分析完成 | #D1FAE5 | #065F46 | AI 分析完成，等待教师评分 |
| TEACHER_SCORING | 教师评分中 | #FFF7ED | #9A3412 | 教师正在评分 |
| COMPLETED | 已完成 | #D1FAE5 | #065F46 | 成绩已确认，流程结束 |
| REJECTED | 已退回 | #FEE2E2 | #991B1B | 提交被退回，需要重新提交 |

**内部计算属性**:
- `statusConfig`: 根据 `status` prop 从 statusMap 查找对应配置。找不到时 fallback 为灰色标签（bg: #F1F5F9, text: #64748B, label: "未知状态"）
- `displayLabel`: label prop 为空时取 statusConfig.label，非空时取 label prop

**渲染模板**:
- 外层 `<span>` 标签，内联样式 `background-color` 和 `color` 来自 statusConfig
- CSS class: `task-status-badge`，统一使用 `display: inline-flex; align-items: center; border-radius: --radius-sm(6px); font-weight: 500; white-space: nowrap`
- 尺寸通过动态 class 控制: `.task-status-badge--small`, `.task-status-badge--large`, `.task-status-badge--default`

**样式规范**:
- 不使用 Element Plus el-tag，独立实现以保证颜色精确控制
- 字体使用 Inter，不加粗（font-weight: 500）
- 不设置 border，纯背景色区分
- 不设置 box-shadow

---

## 3.3 已有组件引用

**引用组件 1: AppLayout（src/components/layout/AppLayout.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| 无显式 props | - | 通过 router-view 自动嵌套。TaskListPage 作为子路由渲染在 AppLayout 的 `<router-view />` 中 |

**引用组件 2: PageHeader（src/components/common/PageHeader.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| title | "我的任务" | 页面主标题 |
| icon | "ListChecks" | Lucide Icons 图标名，渲染在标题左侧 |
| description | "查看和管理所有分配的实训任务" | 标题下方描述文字 |

**引用组件 3: PageContainer（src/components/common/PageContainer.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| 无显式 props | - | 提供统一的内边距、最大宽度限制、背景色 |

**引用组件 4: BaseTable（src/components/base/BaseTable.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| data | taskList (ref<TaskItem[]>) | 表格数据源 |
| columns | columns (ref<Column[]>) | 列定义数组，见下方 column 配置 |
| loading | loading (ref<boolean>) | 表格加载状态 |
| stripe | true | 斑马纹行 |
| border | false | 无外边框，采用卡片风格 |
| size | "default" | 默认行高 |
| emptyText | "暂无任务数据" | 无数据时展示文案 |

Column 定义（columns 数组）:
```
[
  { prop: 'taskName', label: '任务名称', minWidth: '200px', showOverflowTooltip: true },
  { prop: 'courseName', label: '所属课程', width: '150px' },
  { prop: 'deadline', label: '截止时间', width: '170px', formatter: formatDateTime },
  { prop: 'totalScore', label: '总分', width: '80px', align: 'center', className: 'font-mono' },
  { prop: 'submissionType', label: '提交方式', width: '100px', align: 'center',
    formatter: (val) => val === 'GIT' ? 'Git 提交' : val === 'ZIP' ? '压缩包' : val },
  { prop: 'mySubmissionStatus', label: '我的状态', width: '130px', align: 'center',
    slot: 'mySubmissionStatus' },
  { prop: 'action', label: '操作', width: '120px', align: 'center', fixed: 'right', slot: 'action' }
]
```
- `font-mono` class 使用 JetBrains Mono 等宽字体
- `showOverflowTooltip: true` 时超出列宽内容省略并 hover 显示 tooltip
- `formatter` 函数在渲染前对数据进行转换

**引用组件 5: BasePagination（src/components/base/BasePagination.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| currentPage | filters.page | 当前页码，从 1 开始 |
| pageSize | filters.pageSize | 每页条数，默认 20 |
| total | pagination.total | 数据总条数 |
| pageSizes | [10, 20, 50, 100] | 可选每页条数 |
| layout | "total, sizes, prev, pager, next, jumper" | 分页布局 |
| background | true | 带背景色的页码按钮 |

**引用组件 6: BaseSelect（src/components/base/BaseSelect.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| modelValue | filters.status | 双向绑定当前筛选项 |
| placeholder | "提交状态" | 占位提示 |
| clearable | true | 可清除选择恢复"全部" |
| options | statusOptions | 下拉选项数组 |
| style | { width: '200px' } | 固定宽度 |

statusOptions 数组:
```
[
  { label: '全部', value: '' },
  { label: '未提交', value: 'NOT_SUBMITTED' },
  { label: '已提交', value: 'SUBMITTED' },
  { label: 'AI 分析中', value: 'AI_EVALUATING' },
  { label: 'AI 分析完成', value: 'AI_COMPLETED' },
  { label: '教师评分中', value: 'TEACHER_SCORING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已退回', value: 'REJECTED' }
]
```

**引用组件 7: BaseInput（src/components/base/BaseInput.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| modelValue | filters.keyword | 双向绑定搜索关键词 |
| placeholder | "搜索任务名称/课程名称" | 占位提示 |
| clearable | true | 可一键清除 |
| style | { width: '280px' } | 固定宽度 |
| 事件 | @keyup.enter | 回车触发搜索 |

**引用组件 8: BaseButton（src/components/base/BaseButton.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| type | "primary" | 搜索按钮使用主色调 |
| @click | handleSearch | 点击触发搜索 |

**引用组件 9: LoadingState（src/components/common/LoadingState.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| text | "正在加载任务列表..." | Loading 提示文字 |
| size | "default" | 默认尺寸 |

**引用组件 10: ErrorState（src/components/common/ErrorState.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| message | errorMessage (ref<string>) | 错误描述文字 |
| @retry | fetchTasks | 点击重试触发重新加载 |

**引用组件 11: EmptyState（src/components/common/EmptyState.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| description | "暂无任务数据" | 空状态提示文字 |
| image | "empty-task" | 空状态插画类型 |

---

# Section 4: State Design（页面级）

| 状态 | 触发条件 | UI 表现 | 持续时间 | 退出条件 |
|------|---------|--------|---------|---------|
| Loading | 页面初次加载或筛选/翻页触发 API 请求。loading = true | PageHeader 正常展示，FilterBar 正常展示但 BaseButton 加 loading 状态。表格区域展示 LoadingState 组件（居中 spinner + "正在加载任务列表..."文字）。PageContainer 内 FilterBar 以下区域被 LoadingState 占满，不展示表格骨架。BasePagination 隐藏 | 直到 API 返回结果（Mock 约 300ms，超时 15s） | API 返回 success 或 error |
| Empty | API 返回成功但 data.records 为空数组（total = 0）。可能原因：(a) 学生暂无分配任务；(b) 筛选条件过于严格，过滤掉全部结果 | PageHeader、FilterBar 正常展示。表格区域展示 EmptyState 组件（"暂无任务数据"插图 + 文字）。如果筛选条件非空（filters.status !== '' 或 filters.keyword !== ''），EmptyState 下方额外展示"没有符合筛选条件的任务，请尝试调整筛选条件"提示文字（font-size: --font-size-sm, color: --color-text-placeholder）。如果筛选条件全空，只展示"暂无任务数据"。BasePagination 隐藏 | 持续直到用户修改筛选条件或页面数据更新 | (a) 用户修改筛选条件为更宽松的条件重新搜索；(b) 管理员/教师分配了新任务 |
| Error | API 请求异常：网络错误、5001 服务端错误、超时无响应 | PageHeader、FilterBar 正常展示。表格区域展示 ErrorState 组件（errorMessage + 重试按钮）。errorMessage 文案根据错误类型：(a) 网络错误："网络连接失败，请检查网络"；(b) code=5001："服务异常，请稍后重试"；(c) 超时："请求超时，请稍后重试"；(d) 其他："加载失败：{message}"。重试按钮调用 fetchTasks() 重新请求。BasePagination 隐藏 | 持续直到用户点击重试或手动刷新页面 | 重试后 API 返回 success |
| Success | API 返回 code=0，data.records 包含任务列表数据 | PageHeader、FilterBar、BaseTable、BasePagination 均正常展示。表格行数据渲染完成。TaskStatusBadge 在每个任务行中正确显示对应状态颜色和文案。操作列"查看详情"按钮可点击。BasePagination 显示正确页码和总条数 | 持续直到用户触发新的筛选条件或翻页操作 | 用户发起新的 API 请求（筛选/翻页），进入 Loading 状态 |
| NoPermission | 路由守卫检测到当前用户角色不是 student | 整个内容区替换为 NoPermission 组件。PageHeader 仍然展示（标题不变）。FilterBar、Table、Pagination 全部隐藏。NoPermission 展示锁图标 + "当前账号无权限访问此页面，请使用学生账号登录"文字 + "返回首页"按钮。不展示具体任务数据 | 持续直到用户切换账号或离开此页面 | 用户点击"返回首页"跳转到登录页所属角色首页 |
| Offline | 浏览器 navigator.onLine 变为 false 或在 API 请求过程中网络断开 | PageContainer 顶部（PageHeader 和 FilterBar 之间）展示黄色离线提示条："网络连接已断开，数据可能不是最新"。提示条高度 40px，bg: #FEF3C7, color: #92400E, text-align: center。当前已加载的数据仍然展示（表格不隐藏，但数据可能是旧数据）。筛选、翻页按钮仍可点击，但点击后 API 失败 → 进入 Error 状态。BasePagination 保持可见 | 直到网络恢复 | navigator.onLine 变为 true，黄色提示条自动消失。自动触发 fetchTasks() 刷新数据 |

**Offline 状态实现细节**：
- 在 TaskListPage 的 onMounted 中注册 'online' 和 'offline' 事件监听
- 在 onUnmounted 中移除监听
- 网络恢复时（online 事件触发）自动调用 fetchTasks() 刷新为最新数据
- 离线提示条使用 CSS transition（--transition-normal, 300ms ease）做淡入淡出

---

# Section 5: Data Flow

## 5.1 页面加载数据流

```
用户导航到 /student/tasks
→ 路由守卫 beforeEach 触发
→ auth.ts: 检查 User Store token → 有效且角色为学生 → 放行
→ permission.ts: 加载学生权限 → 添加动态路由
→ TaskListPage.vue 渲染（AppLayout 内）
→ onMounted:
  1. 注册 window online/offline 事件监听
  2. 调用 fetchTasks()
→ fetchTasks():
  1. loading.value = true, error.value = null
  2. 构建请求参数: { page: filters.page, pageSize: filters.pageSize, status: filters.status || undefined, keyword: filters.keyword || undefined }
  3. 调用 taskApi.getStudentTasks(params)
  4. Axios 发送 GET /api/v1/student/tasks?page=1&pageSize=20
  5. Mock 层拦截（VITE_USE_MOCK=true），300ms 后返回分页数据
  6. API 成功:
     - taskList.value = response.data.records
     - pagination.total = response.data.total
     - pagination.page = response.data.page
     - loading.value = false
     - 表格渲染完成
  7. API 失败:
     - loading.value = false
     - error.value = true
     - errorMessage.value = 根据错误码映射的错误文案
```

## 5.2 筛选操作数据流

```
操作: 用户选择提交状态筛选 + 输入关键词搜索

触发: 用户在 BaseSelect 中选择了"已提交"，在 BaseInput 输入"Java"，点击"搜索"按钮（或按 Enter）

Given: 当前页面 taskList 有 50 条全部任务数据，pagination.total = 50，page = 1

When:
1. 用户点击 BaseSelect 下拉框 → 展开 8 个选项（全部 + 7 种状态）→ 点击"已提交" (value: 'SUBMITTED')
2. 用户在 BaseInput 中输入关键词 "Java"
3. 用户点击"搜索"按钮

Then:
1. filters.status = 'SUBMITTED', filters.keyword = 'Java', filters.page = 1（筛选操作强制重置到第 1 页）
2. loading = true，表格区域展示 LoadingState
3. 调用 GET /api/v1/student/tasks?page=1&pageSize=20&status=SUBMITTED&keyword=Java
4. Mock 返回筛选后的结果: total = 5, records 包含 5 条匹配的任务
5. loading = false
6. taskList = 5 条筛选结果
7. pagination.total = 5, pagination.page = 1
8. BasePagination 更新为显示 5 条总计，当前第 1 页
9. 表格重新渲染，仅显示 5 条数据

Given: 筛选条件 "status=SUBMITTED, keyword=Java" 返回 0 条结果

When: API 返回 { records: [], total: 0 }

Then:
1. loading = false
2. BaseTable 隐藏，展示 EmptyState
3. EmptyState 下方展示"没有符合筛选条件的任务，请尝试调整筛选条件"提示
4. BasePagination 隐藏（total = 0 时不展示分页）
```

## 5.3 翻页操作数据流

```
操作: 用户翻页浏览更多任务

触发: 用户在 BasePagination 点击"下一页"按钮

Given: 当前 pagination.total = 120, page = 1, pageSize = 20, 已展示第 1 页

When:
1. 用户点击分页组件的"下一页"（或点击页码 2）
2. BasePagination 触发 @change 事件，payload: { page: 2, pageSize: 20 }

Then:
1. filters.page = 2
2. loading = true
3. 调用 GET /api/v1/student/tasks?page=2&pageSize=20&status=&keyword=
4. Mock 返回第 2 页数据: records 包含 20 条，total = 120, page = 2
5. loading = false
6. taskList = 第 2 页的 20 条新数据（覆盖旧数据）
7. BasePagination 高亮第 2 页按钮
8. 页面不滚动到顶部（保持用户当前视口位置，这是 BaseTable 的默认行为。如需讨论是否加 scrollToTop，以原型为准）

Given: 用户修改每页条数 pageSize 为 50

When: 用户在 BasePagination 的 pageSize 下拉选择 50

Then:
1. filters.pageSize = 50, filters.page = 1（修改 pageSize 时重置为第 1 页）
2. 调用 GET /api/v1/student/tasks?page=1&pageSize=50&status=&keyword=
3. 渲染 50 条数据
```

## 5.4 清空筛选条件数据流

```
操作: 用户清空当前筛选条件

触发: 用户点击 BaseSelect 和 BaseInput 的 clearable 清空按钮

Given: 当前 filters.status = 'SUBMITTED', filters.keyword = 'Java'

When:
1. 用户点击 BaseSelect 右侧 X 图标清空 → filters.status = ''
2. 用户点击 BaseInput 右侧 X 图标清空 → filters.keyword = ''
3. 用户点击"搜索"按钮

Then:
1. filters.page = 1（重置为第 1 页）
2. 调用 GET /api/v1/student/tasks?page=1&pageSize=20
3. 返回全部未筛选的任务列表
```

## 5.5 查看任务详情跳转数据流

```
操作: 用户点击某行的"查看详情"按钮

触发: 用户在表格操作列点击"查看详情" BaseButton

Given: 表格第 3 行任务 taskId = 42, taskName = "Java 基础编程练习"

When:
1. 用户点击第 3 行"查看详情"按钮
2. 调用 router.push(`/student/tasks/42`)

Then:
1. 浏览器导航到 /student/tasks/42
2. 路由匹配到 StudentTaskDetailPage 组件
3. TaskListPage 组件被缓存（keep-alive 机制，如果路由配置了 keepAlive）
4. 用户在详情页按返回时回到 TaskListPage，筛选条件和分页状态保持

注意: keep-alive 需要在路由配置中设置 meta.keepAlive = true。TaskListPage 需要配合 onActivated / onDeactivated 生命周期（如果在 keep-alive 中）处理数据更新场景。
```

---

# Section 6: API & Mock Specification

## 接口 1: GET /api/v1/student/tasks

用途: 获取学生任务分页列表，支持按提交状态和关键词筛选

请求方式: GET

请求参数（Query Parameters）:

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| page | number | 否 | 页码，从 1 开始，默认 1 | 1 |
| pageSize | number | 否 | 每页条数，默认 20，可选 10/20/50/100 | 20 |
| status | string | 否 | 提交状态筛选。空字符串表示不过滤。可选值: NOT_SUBMITTED / SUBMITTED / AI_EVALUATING / AI_COMPLETED / TEACHER_SCORING / COMPLETED / REJECTED | SUBMITTED |
| keyword | string | 否 | 搜索关键词，模糊匹配任务名称(taskName)和课程名称(courseName)。空字符串表示不过滤 | Java |

响应数据结构:
```
{
  code: number,             // 0=成功
  message: string,          // 提示信息
  data: {
    page: number,           // 当前页码
    pageSize: number,       // 每页条数
    total: number,          // 总条数
    pages: number,          // 总页数
    records: Array<{
      taskId: number,       // 任务ID
      taskName: string,     // 任务名称
      courseName: string,   // 所属课程名称
      teacherName: string,  // 授课教师姓名
      deadline: string,     // 截止时间 (ISO 8601格式)
      totalScore: number,   // 任务总分
      submissionType: string, // 提交方式: GIT / ZIP / ONLINE
      status: string,        // 任务整体状态
      mySubmissionStatus: string, // 当前学生的提交状态: NOT_SUBMITTED / SUBMITTED / AI_EVALUATING / AI_COMPLETED / TEACHER_SCORING / COMPLETED / REJECTED
      createdAt: string     // 创建时间 (ISO 8601格式)
    }>,
    hasNext: boolean,       // 是否有下一页
    hasPrev: boolean        // 是否有上一页
  } | null
}
```

**Mock 数据示例 1（正常分页数据，第 1 页，共 50 条）**:

```
{
  "code": 0,
  "message": "获取任务列表成功",
  "success": true,
  "timestamp": "2026-07-03T10:30:00.000Z",
  "traceId": "trace-task-list-001",
  "data": {
    "page": 1,
    "pageSize": 20,
    "total": 50,
    "pages": 3,
    "records": [
      {
        "taskId": 1,
        "taskName": "Java 基础编程练习",
        "courseName": "Java 程序设计",
        "teacherName": "李老师",
        "deadline": "2026-07-15T23:59:59.000Z",
        "totalScore": 100,
        "submissionType": "GIT",
        "status": "PUBLISHED",
        "mySubmissionStatus": "NOT_SUBMITTED",
        "createdAt": "2026-06-01T08:00:00.000Z"
      },
      {
        "taskId": 2,
        "taskName": "Spring Boot 项目实战",
        "courseName": "Web 后端开发",
        "teacherName": "王老师",
        "deadline": "2026-07-20T23:59:59.000Z",
        "totalScore": 150,
        "submissionType": "GIT",
        "status": "PUBLISHED",
        "mySubmissionStatus": "SUBMITTED",
        "createdAt": "2026-06-05T08:00:00.000Z"
      },
      {
        "taskId": 3,
        "taskName": "Vue 前端页面开发",
        "courseName": "Web 前端开发",
        "teacherName": "赵老师",
        "deadline": "2026-07-10T23:59:59.000Z",
        "totalScore": 80,
        "submissionType": "ZIP",
        "status": "PUBLISHED",
        "mySubmissionStatus": "AI_EVALUATING",
        "createdAt": "2026-06-10T08:00:00.000Z"
      },
      {
        "taskId": 4,
        "taskName": "数据库设计与优化",
        "courseName": "数据库原理",
        "teacherName": "李老师",
        "deadline": "2026-07-18T23:59:59.000Z",
        "totalScore": 120,
        "submissionType": "ZIP",
        "status": "PUBLISHED",
        "mySubmissionStatus": "AI_COMPLETED",
        "createdAt": "2026-06-15T08:00:00.000Z"
      },
      {
        "taskId": 5,
        "taskName": "算法分析与设计",
        "courseName": "数据结构与算法",
        "teacherName": "王老师",
        "deadline": "2026-07-12T23:59:59.000Z",
        "totalScore": 100,
        "submissionType": "ONLINE",
        "status": "PUBLISHED",
        "mySubmissionStatus": "COMPLETED",
        "createdAt": "2026-06-20T08:00:00.000Z"
      },
      {
        "taskId": 6,
        "taskName": "操作系统原理报告",
        "courseName": "操作系统",
        "teacherName": "赵老师",
        "deadline": "2026-07-14T23:59:59.000Z",
        "totalScore": 90,
        "submissionType": "ZIP",
        "status": "PUBLISHED",
        "mySubmissionStatus": "REJECTED",
        "createdAt": "2026-06-25T08:00:00.000Z"
      },
      {
        "taskId": 7,
        "taskName": "计算机网络实验",
        "courseName": "计算机网络",
        "teacherName": "李老师",
        "deadline": "2026-07-22T23:59:59.000Z",
        "totalScore": 100,
        "submissionType": "GIT",
        "status": "PUBLISHED",
        "mySubmissionStatus": "TEACHER_SCORING",
        "createdAt": "2026-07-01T08:00:00.000Z"
      },
      {
        "taskId": 8,
        "taskName": "Python 数据分析",
        "courseName": "Python 编程",
        "teacherName": "王老师",
        "deadline": "2026-07-25T23:59:59.000Z",
        "totalScore": 110,
        "submissionType": "GIT",
        "status": "PUBLISHED",
        "mySubmissionStatus": "NOT_SUBMITTED",
        "createdAt": "2026-07-02T08:00:00.000Z"
      },
      {
        "taskId": 9,
        "taskName": "软件工程文档",
        "courseName": "软件工程",
        "teacherName": "赵老师",
        "deadline": "2026-07-28T23:59:59.000Z",
        "totalScore": 80,
        "submissionType": "ZIP",
        "status": "PUBLISHED",
        "mySubmissionStatus": "NOT_SUBMITTED",
        "createdAt": "2026-07-03T08:00:00.000Z"
      },
      {
        "taskId": 10,
        "taskName": "Linux Shell 编程",
        "courseName": "Linux 系统管理",
        "teacherName": "李老师",
        "deadline": "2026-07-30T23:59:59.000Z",
        "totalScore": 70,
        "submissionType": "ONLINE",
        "status": "PUBLISHED",
        "mySubmissionStatus": "SUBMITTED",
        "createdAt": "2026-07-03T10:00:00.000Z"
      },
      {
        "taskId": 11,
        "taskName": "C++ 面向对象设计",
        "courseName": "C++ 程序设计",
        "teacherName": "王老师",
        "deadline": "2026-08-01T23:59:59.000Z",
        "totalScore": 120,
        "submissionType": "GIT",
        "status": "PUBLISHED",
        "mySubmissionStatus": "NOT_SUBMITTED",
        "createdAt": "2026-07-03T09:00:00.000Z"
      },
      {
        "taskId": 12,
        "taskName": "需求分析报告",
        "courseName": "软件工程",
        "teacherName": "赵老师",
        "deadline": "2026-08-05T23:59:59.000Z",
        "totalScore": 90,
        "submissionType": "ZIP",
        "status": "PUBLISHED",
        "mySubmissionStatus": "AI_COMPLETED",
        "createdAt": "2026-07-03T08:30:00.000Z"
      },
      {
        "taskId": 13,
        "taskName": "NoSQL 数据库实践",
        "courseName": "数据库原理",
        "teacherName": "李老师",
        "deadline": "2026-08-10T23:59:59.000Z",
        "totalScore": 100,
        "submissionType": "GIT",
        "status": "PUBLISHED",
        "mySubmissionStatus": "NOT_SUBMITTED",
        "createdAt": "2026-07-03T07:00:00.000Z"
      },
      {
        "taskId": 14,
        "taskName": "UI 界面设计作业",
        "courseName": "Web 前端开发",
        "teacherName": "赵老师",
        "deadline": "2026-08-15T23:59:59.000Z",
        "totalScore": 60,
        "submissionType": "ZIP",
        "status": "PUBLISHED",
        "mySubmissionStatus": "COMPLETED",
        "createdAt": "2026-07-02T16:00:00.000Z"
      },
      {
        "taskId": 15,
        "taskName": "API 接口开发",
        "courseName": "Web 后端开发",
        "teacherName": "王老师",
        "deadline": "2026-08-18T23:59:59.000Z",
        "totalScore": 130,
        "submissionType": "GIT",
        "status": "PUBLISHED",
        "mySubmissionStatus": "SUBMITTED",
        "createdAt": "2026-07-02T14:00:00.000Z"
      },
      {
        "taskId": 16,
        "taskName": "数据结构课程设计",
        "courseName": "数据结构与算法",
        "teacherName": "王老师",
        "deadline": "2026-08-20T23:59:59.000Z",
        "totalScore": 150,
        "submissionType": "GIT",
        "status": "PUBLISHED",
        "mySubmissionStatus": "REJECTED",
        "createdAt": "2026-07-02T12:00:00.000Z"
      },
      {
        "taskId": 17,
        "taskName": "MySQL 查询优化",
        "courseName": "数据库原理",
        "teacherName": "李老师",
        "deadline": "2026-08-22T23:59:59.000Z",
        "totalScore": 85,
        "submissionType": "ONLINE",
        "status": "PUBLISHED",
        "mySubmissionStatus": "NOT_SUBMITTED",
        "createdAt": "2026-07-02T10:00:00.000Z"
      },
      {
        "taskId": 18,
        "taskName": "React 组件开发",
        "courseName": "Web 前端开发",
        "teacherName": "赵老师",
        "deadline": "2026-08-25T23:59:59.000Z",
        "totalScore": 95,
        "submissionType": "ZIP",
        "status": "PUBLISHED",
        "mySubmissionStatus": "TEACHER_SCORING",
        "createdAt": "2026-07-01T18:00:00.000Z"
      },
      {
        "taskId": 19,
        "taskName": "Spring Security 配置",
        "courseName": "Web 后端开发",
        "teacherName": "王老师",
        "deadline": "2026-08-28T23:59:59.000Z",
        "totalScore": 100,
        "submissionType": "GIT",
        "status": "PUBLISHED",
        "mySubmissionStatus": "NOT_SUBMITTED",
        "createdAt": "2026-07-01T16:00:00.000Z"
      },
      {
        "taskId": 20,
        "taskName": "单元测试编写",
        "courseName": "Java 程序设计",
        "teacherName": "李老师",
        "deadline": "2026-08-30T23:59:59.000Z",
        "totalScore": 80,
        "submissionType": "GIT",
        "status": "PUBLISHED",
        "mySubmissionStatus": "AI_EVALUATING",
        "createdAt": "2026-07-01T14:00:00.000Z"
      }
    ],
    "hasNext": true,
    "hasPrev": false
  }
}
```

**Mock 数据示例 2（筛选后的数据，status=SUBMITTED, keyword="Java", total=3）**:

```
{
  "code": 0,
  "message": "获取任务列表成功",
  "success": true,
  "timestamp": "2026-07-03T10:31:00.000Z",
  "traceId": "trace-task-list-002",
  "data": {
    "page": 1,
    "pageSize": 20,
    "total": 3,
    "pages": 1,
    "records": [
      {
        "taskId": 12,
        "taskName": "Java Spring Boot 微服务开发",
        "courseName": "Java 高级程序设计",
        "teacherName": "李老师",
        "deadline": "2026-07-25T23:59:59.000Z",
        "totalScore": 120,
        "submissionType": "GIT",
        "status": "PUBLISHED",
        "mySubmissionStatus": "SUBMITTED",
        "createdAt": "2026-06-20T08:00:00.000Z"
      },
      {
        "taskId": 18,
        "taskName": "Java 并发编程实践",
        "courseName": "Java 高级程序设计",
        "teacherName": "李老师",
        "deadline": "2026-07-28T23:59:59.000Z",
        "totalScore": 100,
        "submissionType": "GIT",
        "status": "PUBLISHED",
        "mySubmissionStatus": "SUBMITTED",
        "createdAt": "2026-06-22T08:00:00.000Z"
      },
      {
        "taskId": 25,
        "taskName": "Java EE 企业应用",
        "courseName": "Web 后端开发",
        "teacherName": "王老师",
        "deadline": "2026-07-30T23:59:59.000Z",
        "totalScore": 140,
        "submissionType": "GIT",
        "status": "PUBLISHED",
        "mySubmissionStatus": "SUBMITTED",
        "createdAt": "2026-06-25T08:00:00.000Z"
      }
    ],
    "hasNext": false,
    "hasPrev": false
  }
}
```

**Mock 数据示例 3（空数据，学生无任务）**:

```
{
  "code": 0,
  "message": "获取任务列表成功",
  "success": true,
  "timestamp": "2026-07-03T10:32:00.000Z",
  "traceId": "trace-task-list-003",
  "data": {
    "page": 1,
    "pageSize": 20,
    "total": 0,
    "pages": 0,
    "records": [],
    "hasNext": false,
    "hasPrev": false
  }
}
```

**Mock 错误场景**:

| 错误场景 | 错误码 | 响应数据 | UI 表现 |
|---------|--------|---------|--------|
| 服务端错误 | 5001 | { "code": 5001, "message": "服务器繁忙，请稍后重试", "data": null } | ErrorState: "服务异常，请稍后重试" + 重试按钮 |
| 网络错误 | Network Error | 无响应体，Axios 捕获网络异常 | ErrorState: "网络连接失败，请检查网络" + 重试按钮 |
| 超时 | Timeout | Axios 超时错误(15s) | ErrorState: "请求超时，请稍后重试" + 重试按钮 |
| 未认证 | 2001 | { "code": 2001, "message": "未登录或 Token 已过期", "data": null } | 由 Axios 拦截器统一处理，跳转到 /login |

**Mock 延迟**: 300ms（模拟正常网络延迟）

**Mock 实现位置**: src/mock/studentTasks.ts

**Mock 逻辑**:
- 根据 query 参数 status 过滤 records: 非空时筛选 mySubmissionStatus === status，为空时不过滤
- 根据 query 参数 keyword 过滤 records: 非空时检查 taskName.includes(keyword) 或 courseName.includes(keyword)（忽略大小写）
- 分页逻辑: 从筛选后的数组中按 page 和 pageSize 切片返回
- status 参数无效值时（非 7 种合法枚举）忽略该筛选条件，按不过滤处理

---

# Section 7: Interaction Flows

## 交互 1: 正常加载任务列表

Given: 学生用户已登录，User Store 中角色为 student，首次访问 /student/tasks

When:
1. 浏览器导航到 /student/tasks
2. TaskListPage 组件挂载，onMounted 执行
3. filters 初始值: page=1, pageSize=20, status='', keyword=''
4. fetchTasks() 被调用

Then:
1. PageHeader 展示标题"我的任务"和描述文字
2. FilterBar 展示，BaseSelect 显示"提交状态"占位文字，BaseInput 为空，搜索按钮可点击
3. LoadingState 居中展示 spinner + "正在加载任务列表..."文字
4. 300ms 后 API 返回 20 条任务数据
5. LoadingState 消失，BaseTable 展示 7 列 20 行数据
6. 每行"我的状态"列渲染 TaskStatusBadge 组件，颜色和文案正确对应
7. 每行"操作"列展示蓝色"查看详情"文字链接按钮
8. BasePagination 展示 "共 50 条"、页码按钮 [1, 2, 3]、上一页/下一页、跳转输入框
9. 第 1 页按钮高亮

## 交互 2: 按状态筛选并搜索

Given: 当前 taskList 展示 50 条全部任务，pagination.total=50

When:
1. 用户点击 BaseSelect → 下拉框出现 8 个选项 → 点击"AI 分析中"(AI_EVALUATING)
2. BaseSelect 显示"AI 分析中"
3. 用户在 BaseInput 输入"前端"（filters.keyword = '前端'）
4. 用户点击"搜索"按钮

Then:
1. filters.status = 'AI_EVALUATING', filters.keyword = '前端', filters.page = 1
2. LoadingState 展示
3. GET /api/v1/student/tasks?page=1&pageSize=20&status=AI_EVALUATING&keyword=前端
4. 300ms 后 API 返回 2 条匹配结果
5. BaseTable 展示 2 行数据
6. BasePagination 更新: 共 2 条，只显示 1 页
7. 每行的 TaskStatusBadge 颜色均为紫色（#EDE9FE 背景 / #6D28D9 文字）

## 交互 3: 筛选结果为空

Given: 当前有 50 条任务数据

When:
1. 用户选择状态"已完成" + 输入关键词"zzzznotexistzzzz"
2. 点击"搜索"

Then:
1. API 返回: records=[], total=0
2. LoadingState 消失
3. 表格区域替换为 EmptyState: 空白插图 + "暂无任务数据"
4. EmptyState 下方出现一行提示文字: "没有符合筛选条件的任务，请尝试调整筛选条件"（灰色小字）
5. BasePagination 完全隐藏

## 交互 4: 翻页浏览

Given: pagination.total=120, 当前在第 1 页，展示 20 条数据

When:
1. 用户点击 BasePagination 的"下一页"按钮
2. filters.page 变为 2

Then:
1. LoadingState 展示
2. GET /api/v1/student/tasks?page=2&pageSize=20
3. 返回第 2 页 20 条新数据
4. BaseTable 更新渲染第 2 页数据
5. BasePagination 第 2 页按钮高亮，"上一页"按钮变为可用
6. 用户继续点击页码 3 → 重复上述流程加载第 3 页

## 交互 5: 查看任务详情

Given: 表格展示了 20 条任务数据，第 5 行任务: taskId=7, taskName="计算机网络实验"

When:
1. 用户点击第 5 行"查看详情"文字按钮

Then:
1. router.push('/student/tasks/7')
2. 浏览器导航到 /student/tasks/7
3. StudentTaskDetailPage 组件开始加载
4. 用户在此页面按浏览器返回 → 回到 /student/tasks → TaskListPage 重新激活（keep-alive 场景下保持筛选和分页状态不变）

## 交互 6: API 加载失败后重试

Given: 用户访问 /student/tasks，网络异常导致 API 请求失败

When:
1. API 请求超时或网络断开
2. loading = false, error = true

Then:
1. ErrorState 展示: 错误图标 + "网络连接失败，请检查网络" + "重试"按钮
2. FilterBar 仍然可见可操作
3. 用户点击"重试"按钮 → 触发 fetchTasks() → 重新请求 API
4. 如果重试时网络已恢复 → 正常展示数据
5. 如果重试仍失败 → 继续展示 ErrorState

## 交互 7: 离线后恢复

Given: 学生已打开任务列表页面，展示 50 条数据，一切正常

When:
1. 用户将电脑网络断开（Wi-Fi 关闭或拔网线）
2. window 'offline' 事件触发

Then:
1. PageContainer 顶部出现黄色提示条: "网络连接已断开，数据可能不是最新"
2. 现有数据仍然可见（不会消失）
3. 用户尝试翻页 → API 失败 → 切换为 ErrorState
4. 用户恢复网络连接
5. window 'online' 事件触发
6. 黄色提示条淡出消失
7. 自动调用 fetchTasks() 重新加载当前页数据

## 交互 8: 回车键触发搜索

Given: 用户在 BaseInput 中输入关键词后，不点击鼠标

When:
1. 用户在 BaseInput 中输入 "Python"
2. 用户按下 Enter 键

Then:
1. BaseInput 的 @keyup.enter 事件触发 handleSearch()
2. 行为与点击"搜索"按钮完全一致
3. 如果 BaseInput 为空（filters.keyword = ''），也触发搜索（相当于清空关键词搜索）

---

# Section 8: Permission Design

| 属性 | 值 |
|------|-----|
| 页面权限标识 | student:task:list |
| 页面允许角色 | ['student'] |
| 路由权限检查 | meta.roles = ['student'] |
| 路由 meta | { title: '我的任务', icon: 'ListChecks', roles: ['student'], keepAlive: true } |
| 按钮级权限 | 无（所有按钮对所有学生可见，无权限差异） |

权限控制详情:

1. **页面级权限**: 路由守卫 permission.ts 检查 User Store 中 role 是否包含在 meta.roles 中。非学生角色访问时重定向到对应角色首页。管理员和教师通过各自的独立路由（/admin/tasks, /teacher/tasks）查看任务，不经过此页面。

2. **按钮级权限**: 本页面无按钮级权限差异。"查看详情"按钮对所有学生可见，disabled 场景不适用（没有不允许查看自己任务详情的业务场景）。

3. **数据级权限**: API 层面自动根据当前登录用户的 token 返回该学生的任务数据。后端从 JWT token 解析 userId，只返回该学生的任务。前端不传入 userId 参数，防止横向越权。

4. **keepAlive 行为**: meta.keepAlive = true 表示页面组件在切换路由时保持缓存，筛选条件和分页状态不丢失。从任务详情页返回后，列表仍保持之前的筛选和滚动位置。

---

# Section 9: Acceptance Criteria

AC-1: Given 学生用户登录后导航到 /student/tasks When 页面加载完成 Then 页面展示 PageHeader("我的任务")、筛选栏（状态选择器 + 搜索框 + 搜索按钮）、任务表格（7 列: 任务名称、所属课程、截止时间、总分、提交方式、我的状态、操作）、分页组件。表格内每一行的"我的状态"列正确显示 TaskStatusBadge 组件，颜色和文案与状态值对应

AC-2: Given 任务列表页已加载 50 条数据 When 用户在状态选择器中选择"已提交"，输入关键词"Java"，点击搜索 Then 表格更新为筛选后的结果（仅显示 mySubmissionStatus 为 SUBMITTED 且 taskName 或 courseName 包含"Java"的任务）。分页组件更新 total 为筛选结果总条数。如果筛选结果为空，展示 EmptyState 并提示"没有符合筛选条件的任务，请尝试调整筛选条件"

AC-3: Given 任务列表页当前展示第 1 页 20 条数据，共 120 条 When 用户点击分页组件"下一页" Then 表格更新为第 2 页的 20 条数据，分页组件第 2 页按钮高亮。不需要页面滚动到顶部

AC-4: Given 任务列表页展示数据 When 用户点击某一行"查看详情"按钮 Then 浏览器导航到 /student/tasks/{taskId} 详情页。从详情页返回后，列表页保持之前的筛选条件和分页位置（keep-alive 生效）

AC-5: Given 任务列表页 API 请求失败 When 用户看到 ErrorState 组件并点击"重试"按钮 Then 重新发起 API 请求。如果成功，正常展示表格数据；如果仍然失败，继续展示 ErrorState

AC-6: Given 任务列表页已正常加载数据 When 用户断开网络 Then 页面顶部展示黄色"网络连接已断开，数据可能不是最新"提示条，现有数据保持可见。网络恢复后提示条消失并自动刷新数据

AC-7: Given 页面加载中 When API 请求尚未返回 Then FilterBar 可见，表格区域展示 LoadingState（spinner 动画 + "正在加载任务列表..."文字），分页组件隐藏

AC-8: Given 任务列表页展示数据 When 用户在搜索框中输入关键词后按 Enter 键 Then 触发搜索，行为与点击"搜索"按钮一致

---

# Section 10: Files to Create / Modify

## 新建文件

```
├── src/pages/student/TaskListPage.vue           # 学生任务列表页面组件
├── src/components/common/TaskStatusBadge.vue    # 任务状态标签通用组件（7 种状态颜色映射）
├── src/api/studentTask.ts                       # 学生任务相关 API 函数（getStudentTasks）
├── src/api/studentSubmit.ts                     # 学生提交相关 API 函数（createSubmission, gitVerify, uploadFile, uploadReport, triggerAiEvaluate）—— TaskListPage 本身不直接引用，但与详情页/提交页共享
├── src/mock/studentTasks.ts                     # 学生任务列表 Mock 数据
├── src/types/task.ts                            # 任务相关 TypeScript 类型定义（TaskItem, SubmissionType, TaskStatus 等枚举）
└── docs/page-analysis/student-task-list.md      # 本文件
```

## 修改文件

```
├── src/router/routes/student.ts                 # 追加 StudentTasks 路由（/student/tasks → TaskListPage.vue，meta: { keepAlive: true }）
├── src/router/index.ts                          # 确认动态路由注册逻辑
├── src/mock/index.ts                            # 追加 import './studentTasks' 注册新 Mock 模块
└── src/types/common.ts                          # 确认 PaginationParams 和 PaginatedResult 泛型类型已定义
```

## 依赖的已有组件（本页面不修改它们，但依赖它们的存在）

```
├── src/components/layout/AppLayout.vue          # 整体布局框架
├── src/components/common/PageHeader.vue         # 页面标题组件
├── src/components/common/PageContainer.vue      # 页面内容容器
├── src/components/common/LoadingState.vue       # Loading 状态组件
├── src/components/common/ErrorState.vue         # Error 状态组件
├── src/components/common/EmptyState.vue         # Empty 状态组件
├── src/components/common/NoPermission.vue       # 无权限组件
├── src/components/base/BaseTable.vue            # 基础表格组件
├── src/components/base/BasePagination.vue       # 基础分页组件
├── src/components/base/BaseSelect.vue           # 基础选择器组件
├── src/components/base/BaseInput.vue            # 基础输入框组件
├── src/components/base/BaseButton.vue           # 基础按钮组件
└── src/stores/useUserStore.ts                   # 用户 Store（获取角色信息用于权限判断）
```

## 开发顺序

1. 先完成 src/types/task.ts（类型定义）—— 被所有后续文件依赖
2. 完成 src/mock/studentTasks.ts（Mock 数据）—— 并行开发，不依赖组件
3. 完成 src/api/studentTask.ts（API 函数）—— 依赖类型定义
4. 完成 src/components/common/TaskStatusBadge.vue（状态标签组件）—— 独立组件，可并行
5. 完成 src/pages/student/TaskListPage.vue（页面组件）—— 依赖以上全部
6. 完成 src/router/routes/student.ts（路由配置）

注意事项:
1. TaskStatusBadge 组件虽然首次在 TaskListPage 使用，但它是 Common 组件，后续在教师后台、管理员后台等页面也会复用。因此放在 src/components/common/ 而非 src/components/student/。
2. TaskListPage 需要支持 keepAlive 缓存，需要在 onActivated/onDeactivated 中处理数据刷新逻辑。
3. 筛选条件中的 status 字段传给 API 时，值为 ''（空字符串）时不传该参数（或传 undefined），避免后端误解为空字符串筛选。
