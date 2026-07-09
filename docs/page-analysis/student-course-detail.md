# Page Analysis: 学生课程详情页 (Student Course Detail)

**文档版本**: v1.0
**创建日期**: 2026-07-03
**分析范围**: 学生课程详情页，含课程基本信息展示和关联任务列表

---

# Section 1: Page Identity

| 属性 | 值 |
|------|-----|
| 页面名称 | 学生课程详情页 |
| 页面文件 | src/pages/student/CourseDetailPage.vue |
| 路由路径 | /student/courses/:courseId |
| 路由名称 | StudentCourseDetail |
| 所属 Sprint | Sprint 2 |
| 所属 Feature | F-2-02（参见《Sprint 2 Spec》） |
| 页面角色 | 学生 |
| 页面复杂度 | L2（课程信息展示 + 任务子表 + 状态标签 + 错误处理，中等复杂度） |
| 原型参考 | 参见《UI Design System v1.0》学生课程详情原型 |

页面职责：展示学生所选课程的完整信息（课程名、编号、授课教师、教师邮箱、学期、学分、课程简介）。同时展示该课程下所有实训任务的列表，包含任务名称、截止时间、总分、我的提交状态（TaskStatusBadge）、我的分数。点击任务行可跳转到任务详情页。处理课程不存在的错误场景（code=1001）。

---

# Section 2: Page Layout Structure

页面嵌套在 AppLayout 中。AppLayout 提供 Sidebar + Navbar + PageContainer 骨架。

```
+--------------------------------------------------+
|  AppLayout                                         |
|  +-----------+-----------------------------------+ |
|  | Sidebar   |  Navbar                            | |
|  +-----------+-----------------------------------+ |
|  |           |  PageContainer (padding: 24px)    | |
|  |           |                                    | |
|  |           |  +-----------------------------+  | |
|  |           |  |  PageHeader                   |  | |
|  |           |  |  标题: 课程名称                 |  | |
|  |           |  |  extra slot: BaseButton        |  | |
|  |           |  |  "返回课程列表"                 |  | |
|  |           |  +-----------------------------+  | |
|  |           |  [间距: 24px]                     |  | |
|  |           |  +-----------------------------+  | |
|  |           |  |  课程信息卡片 (Card, 100%)    |  | |
|  |           |  |  bg: --color-card (#FFFFFF)   |  | |
|  |           |  |  border-radius: --radius-lg   |  | |
|  |           |  |  padding: --spacing-lg (24px) |  | |
|  |           |  |  box-shadow: --shadow-sm      |  | |
|  |           |  |                               |  | |
|  |           |  |  课程编号: CS101               |  | |
|  |           |  |  授课教师: 王建国              |  | |
|  |           |  |  教师邮箱: wangjg@example.com  |  | |
|  |           |  |  学期: 2025-2026-2 学分: 4     |  | |
|  |           |  |  简介: (多行文本)              |  | |
|  |           |  +-----------------------------+  | |
|  |           |  [间距: 24px]                     |  | |
|  |           |  +-----------------------------+  | |
|  |           |  |  关联任务标题 (h2)             |  | |
|  |           |  |  "共 N 个任务"                 |  | |
|  |           |  +-----------------------------+  | |
|  |           |  [间距: 16px]                     |  | |
|  |           |  +-----------------------------+  | |
|  |           |  |  BaseTable (关联任务表, 100%)  |  | |
|  |           |  |  列: 任务名称, 截止时间,       |  | |
|  |           |  |  总分, 我的提交状态, 我的分数  |  | |
|  |           |  +-----------------------------+  | |
|  |           |                                    | |
|  +-----------+-----------------------------------+ |
+--------------------------------------------------+
```

**区域说明**：

| 区域 | 宽度 | 高度 | 对齐 | 滚动策略 | 说明 |
|------|------|------|------|---------|------|
| PageHeader | 100% 父容器 | auto (约 48px) | flex space-between | 不滚动 | 左侧标题为课程名称（动态），右侧"返回课程列表"按钮 |
| 课程信息卡片 | 100% 父容器 | auto (由内容撑开) | 左对齐 | 不滚动 | 白色卡片，展示课程全部基本信息。使用 CSS Grid 两列布局（左侧标签，右侧值） |
| 任务表区域 | 100% 父容器 | auto | 左对齐 | 不滚动 | 包含子标题"关联任务"（h2）和 BaseTable |
| 任务表格 | 100% 父容器 | auto | 左对齐 | 不滚动 | 5 列数据表：任务名称、截止时间、总分、我的提交状态（TaskStatusBadge）、我的分数 |

**课程信息卡片内部布局（CSS Grid 两列）**:

第一行（两列）:
- 左侧: 课程编号标签 + 值
- 右侧: 学期标签 + 值

第二行（两列）:
- 左侧: 授课教师标签 + 值
- 右侧: 学分配额标签 + 值

第三行（全宽）:
- 教师邮箱标签 + 值

第四行（全宽）:
- 课程简介标签 + 多行文本值

**响应式行为**：
- 1920px 分辨率：卡片两列布局，每列宽度 50%
- 1366px 分辨率：卡片两列布局保持不变
- 小于 768px：卡片改为单列布局（CSS Grid grid-template-columns: 1fr 替代 1fr 1fr）

---

# Section 3: Component Tree & Specification

## 3.1 组件树

```
CourseDetailPage.vue（页面组件，在 AppLayout 内渲染）
├── PageHeader（来自 Component Library，src/components/layout/PageHeader.vue）
│   ├── 标题文字: courseInfo.courseName（动态数据）
│   └── extra slot
│       └── BaseButton（type="default", size="default", @click="handleBack"）
│           按钮文案: "返回课程列表"，prefixIcon="ArrowLeft"
├── LoadingState（条件渲染: loading=true）
│   来自 Component Library，src/components/common/LoadingState.vue
│   Props: text="正在加载课程信息..."
├── ErrorState（条件渲染: error 状态）
│   来自 Component Library，src/components/common/ErrorState.vue
│   Props: message=errorMessage, showRetry=true
│   @retry="fetchCourseDetail()"
│   │
│   ├── [code=1001 时] ErrorState 特殊配置
│   │   Props: message="课程不存在或已被删除", showRetry=false
│   │   底部额外渲染: BaseButton（type="primary", @click="handleBack"）
│   │   按钮文案: "返回课程列表"
│   │
│   └── [其他错误码时] ErrorState 通用配置
│       Props: message="课程信息加载失败，请稍后重试", showRetry=true
│
├── <div> Success 状态内容容器（条件渲染: loading=false 且 error=null 且 courseInfo 非空）
│   ├── <div> 课程信息卡片（class="course-info-card"）
│   │   ├── <div> 卡片 grid 容器（display: grid, grid-template-columns: 1fr 1fr, gap: 16px 32px）
│   │   │   ├── <div> 课程编号区
│   │   │   │   ├── <label> "课程编号"（color: --color-text-secondary, font-size: --font-size-sm）
│   │   │   │   └── <span> courseInfo.courseCode（font-family: JetBrains Mono, font-weight: 500）
│   │   │   ├── <div> 学期区
│   │   │   │   ├── <label> "学期"
│   │   │   │   └── <span> courseInfo.semester
│   │   │   ├── <div> 授课教师区
│   │   │   │   ├── <label> "授课教师"
│   │   │   │   └── <span> courseInfo.teacherName
│   │   │   ├── <div> 学分区
│   │   │   │   ├── <label> "学分"
│   │   │   │   └── <span> courseInfo.credits（font-family: JetBrains Mono）
│   │   │   ├── <div> 教师邮箱区（跨两列, grid-column: 1 / -1）
│   │   │   │   ├── <label> "教师邮箱"
│   │   │   │   └── <a> :href="'mailto:' + courseInfo.teacherEmail"
│   │   │   │       courseInfo.teacherEmail（color: --color-primary, text-decoration: none, hover 下划线）
│   │   │   └── <div> 课程简介区（跨两列, grid-column: 1 / -1）
│   │   │       ├── <label> "课程简介"
│   │   │       └── <p> courseInfo.description（line-height: 1.6, white-space: pre-wrap）
│   │   └── </div>
│   │
│   ├── [间距: 24px]
│   │
│   ├── <div> 任务表标题行（flex, space-between, align-items: center）
│   │   ├── <h2> "关联任务"（font-size: --font-size-lg, font-weight: 600）
│   │   └── <span> "共 {tasks.length} 个任务"（font-size: --font-size-sm, color: --color-text-secondary）
│   │
│   ├── [间距: 16px]
│   │
│   ├── EmptyState（条件渲染: tasks.length=0）
│   │   来自 Component Library，src/components/common/EmptyState.vue
│   │   Props: description="该课程暂无实训任务"
│   │
│   └── BaseTable（条件渲染: tasks.length > 0）
│       来自 Component Library，src/components/base/BaseTable.vue
│       Props: columns=taskColumns, data=tasks, stripe=true, bordered=true, size="default", rowKey="taskId"
│       @rowClick="handleTaskClick"
│       ├── 列1: 任务名称 (prop: "taskName", label: "任务名称", minWidth: 200)
│       │   渲染为普通文本，cursor: pointer，hover 时显示 Primary 色 #3B82F6
│       ├── 列2: 截止时间 (prop: "deadline", label: "截止时间", width: 180)
│       │   渲染为格式化日期字符串，格式 "YYYY-MM-DD HH:mm"
│       │   如已过期，文字颜色变为 Danger #EF4444
│       │   字体: JetBrains Mono
│       ├── 列3: 总分 (prop: "totalScore", label: "总分", width: 80, align: "center")
│       │   渲染为 "{totalScore} 分"，字体: JetBrains Mono
│       ├── 列4: 我的提交状态 (prop: "mySubmissionStatus", label: "我的状态", width: 130, align: "center")
│       │   渲染 TaskStatusBadge（新建 Business 组件，见 3.2）: 根据 status 值展示不同颜色标签
│       └── 列5: 我的分数 (prop: "myScore", label: "我的分数", width: 100, align: "center")
│           渲染逻辑:
│           - myScore 为 null 或 undefined → 显示 "-"（灰色文字，color: --color-text-placeholder）
│           - myScore 有值 → 显示 "{myScore} 分"（font-weight: 600, font-family: JetBrains Mono, color: --color-primary）
│
└── NoPermission（条件渲染: 路由守卫未拦截但 Store 判断无权限时）
    来自 Component Library，src/components/common/NoPermission.vue
```

## 3.2 新建子组件定义

### Component: TaskStatusBadge.vue

```
类型: Business
文件路径: src/components/business/TaskStatusBadge.vue
用途: 展示学生提交状态的 7 种状态标签
复杂度: S（<80 行，纯展示组件，无业务逻辑）

Props:
| 名称 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| status | TaskSubmissionStatus | 是 | - | 提交状态枚举值 |
| size | 'default' \| 'small' | 否 | 'default' | 标签尺寸 |

TaskSubmissionStatus 类型定义（src/types/task.ts）:
  'NOT_SUBMITTED' | 'SUBMITTED' | 'AI_EVALUATING' | 'AI_COMPLETED' | 'TEACHER_SCORING' | 'COMPLETED' | 'REJECTED'

Emits: 无（纯展示组件）

Slots: 无

依赖 Store: 无

依赖 API: 无

状态映射表（组件内部常量）:
| status 值 | 标签文案 | 背景色 | 文字色 | 备注意义 |
|-----------|---------|--------|--------|---------|
| NOT_SUBMITTED | "待提交" | #FEF3C7 | #D97706 | 琥珀色/警告色，学生尚未提交 |
| SUBMITTED | "已提交" | #DBEAFE | #2563EB | 蓝色，已提交等待 AI 分析 |
| AI_EVALUATING | "AI分析中" | #EDE9FE | #7C3AED | 紫色，AI 正在分析代码 |
| AI_COMPLETED | "待评分" | #D1FAE5 | #059669 | 绿色，AI 分析完成，等待教师评分 |
| TEACHER_SCORING | "评分中" | #FFF7ED | #EA580C | 橙色，教师正在评分 |
| COMPLETED | "已完成" | #D1FAE5 | #059669 | 绿色，最终成绩已确定 |
| REJECTED | "已退回" | #FEE2E2 | #DC2626 | 红色，教师退回要求修改 |

实现方式: 使用 Element Plus el-tag 组件封装，根据 status prop 从映射表中获取对应文案和颜色。背景色和文字色通过内联样式或 CSS 绑定。size=small 时使用 el-tag size="small"。
```

## 3.3 已有组件引用

**引用组件 1: PageHeader（来自 Component Library，src/components/layout/PageHeader.vue）**

关键 Props 配置:
| Prop | 值 | 说明 |
|------|-----|------|
| title | courseInfo.courseName（或 "课程详情" 在 Loading/Error 状态） | 动态课程名 |

**引用组件 2: BaseButton（来自 Component Library，src/components/base/BaseButton.vue）**

关键 Props 配置（返回按钮）:
| Prop | 值 | 说明 |
|------|-----|------|
| type | "default" | 默认样式 |
| size | "default" | 默认尺寸 |
| icon | "ArrowLeft" | 左侧返回箭头（Lucide Icons） |

关键 Props 配置（code=1001 错误页返回按钮）:
| Prop | 值 | 说明 |
|------|-----|------|
| type | "primary" | 主色调 |
| size | "default" | 默认尺寸 |

**引用组件 3: BaseTable（来自 Component Library，src/components/base/BaseTable.vue）**

关键 Props 配置:
| Prop | 值 | 说明 |
|------|-----|------|
| columns | taskColumns | 任务表格列定义 |
| data | tasks | 任务数据数组 |
| stripe | true | 斑马纹 |
| bordered | true | 边框 |
| size | "default" | 默认尺寸 |
| rowKey | "taskId" | 行唯一标识 |

关键 Events 配置:
| Event | 处理函数 | 说明 |
|------|---------|------|
| rowClick | handleTaskClick(row) | 点击行导航到 /student/tasks/{taskId} |

**引用组件 4: LoadingState（来自 Component Library，src/components/common/LoadingState.vue）**

关键 Props 配置:
| Prop | 值 | 说明 |
|------|-----|------|
| text | "正在加载课程信息..." | 加载中文案 |

**引用组件 5: ErrorState（来自 Component Library，src/components/common/ErrorState.vue）**

关键 Props 配置（通用错误）:
| Prop | 值 | 说明 |
|------|-----|------|
| message | "课程信息加载失败，请稍后重试" | 通用错误文案 |
| showRetry | true | 显示重试按钮 |

关键 Props 配置（课程不存在 code=1001）:
| Prop | 值 | 说明 |
|------|-----|------|
| message | "课程不存在或已被删除" | 资源不存在文案 |
| showRetry | false | 不显示重试按钮 |

**引用组件 6: EmptyState（来自 Component Library，src/components/common/EmptyState.vue）**

关键 Props 配置:
| Prop | 值 | 说明 |
|------|-----|------|
| description | "该课程暂无实训任务" | 空任务列表文案 |

---

# Section 4: State Design（页面级）

| 状态 | 触发条件 | UI 表现 | 持续时间 | 退出条件 |
|------|---------|--------|---------|---------|
| Loading | 页面 onMounted，路由参数 courseId 就绪，发起 API 请求 | PageHeader 标题显示"课程详情"（无课程名）。页面内容区展示 LoadingState 组件（三个跳动圆点 + "正在加载课程信息..."文字）。课程信息卡片和任务表格均不渲染。返回按钮正常渲染但点击不拦截（允许用户在 Loading 期间返回） | 直到 API 返回数据或超时 | API 返回数据（Success）或返回错误（Error） |
| Empty (任务列表) | 课程详情加载成功（code=0），但课程下 tasks 数组为空（length=0） | 课程信息卡片正常渲染（所有字段展示正确）。任务表区域子标题显示"关联任务"+"共 0 个任务"。表格位置展示 EmptyState 组件，文案"该课程暂无实训任务"。不渲染 BaseTable | 持续直到课程新增任务（数据刷新） | 用户刷新后 tasks 数组非空 |
| Error (课程不存在) | API 返回 code=1001（资源不存在），courseId 对应的课程在数据库中不存在 | PageHeader 标题显示"课程详情"。内容区展示 ErrorState 组件：红色 AlertCircle 图标 + "课程不存在或已被删除"文案。不显示重试按钮（showRetry=false）。ErrorState 下方显示 BaseButton（type="primary"），文案"返回课程列表"，点击后 router.push({ name: 'StudentCourseList' }) | 持续直到用户离开页面 | 用户点击"返回课程列表"按钮导航离开 |
| Error (通用) | API 返回其他错误码（5001 服务器错误 / 网络错误 / 其他业务错误） | PageHeader 标题显示"课程详情"。内容区展示 ErrorState 组件：红色 AlertCircle 图标 + "课程信息加载失败，请稍后重试"文案 + "重试"按钮。点击重试后重新调用 fetchCourseDetail() | 持续直到重试成功或用户离开 | 用户点击重试且请求成功（进入 Success） |
| Success | API 返回 code=0，courseInfo 非空 | PageHeader 标题显示 courseInfo.courseName。课程信息卡片完整渲染（课程编号、学期、授课教师、学分、教师邮箱、课程简介）。任务表区域渲染：子标题"关联任务"+"共 N 个任务" + BaseTable（5 列数据）+ 每行任务状态使用 TaskStatusBadge 渲染。截止时间过期行文字变红 | 持续直到用户离开页面 | 用户离开或手动刷新 |
| NoPermission | 不适用 | 路由守卫 meta.roles=["student"] 已控制。非学生用户由守卫拦截。极端情况下 Store 角色异常时渲染 NoPermission 组件 | - | - |
| Offline | navigator.onLine 变为 false | AppLayout 级全局处理（见 student-course-list.md Section 4 同名状态说明）。本页面离线时点击任务行弹出 ElMessage.warning("网络连接已断开，请检查网络")，不发送请求 | 直到网络恢复 | navigator.onLine=true，自动刷新 |

---

# Section 5: Data Flow

## 5.1 页面加载数据流

```
学生从课程列表页点击某课程行或"查看详情"按钮
→ router.push({ name: 'StudentCourseDetail', params: { courseId: '1834567890123456789' } })
→ 路由守卫 beforeEach 触发 → 认证检查 → 权限检查 → 放行
→ CourseDetailPage.vue 渲染
→ onMounted():
  1. 从 route.params 提取 courseId
  2. 验证 courseId 存在（不存在则 router.push 到课程列表页）
  3. 初始化本地状态:
     - loading = true
     - courseInfo = null
     - tasks = []
     - error = null
     - errorCode = null
  4. 调用 fetchCourseDetail(courseId)
→ fetchCourseDetail() 内部:
  1. loading = true
  2. try:
     a. 调用 courseApi.getStudentCourseDetail(courseId)
     b. Axios 发送 GET /api/v1/student/courses/{courseId}
     c. Mock 层拦截（VITE_USE_MOCK=true）→ Mock 返回课程详情 + 任务数组（300ms 延迟）
     d. 响应成功 (code=0):
        - courseInfo = { courseId, courseName, courseCode, teacherName, teacherEmail, semester, credits, description }
        - tasks = response.data.tasks（包含 taskId, taskName, deadline, totalScore, mySubmissionStatus, myScore）
        - error = null
        - errorCode = null
  3. catch (error):
     a. 判断错误类型:
        - code=1001: error = '课程不存在或已被删除', errorCode = 1001
        - code=5001: error = '课程信息加载失败，请稍后重试', errorCode = 5001
        - Network Error: error = '网络连接失败，请检查网络', errorCode = 0
        - 其他: error = error.message || '课程信息加载失败，请稍后重试'
     b. loading = false
  4. loading = false
→ Success: 渲染课程信息卡片 + 任务表格
→ Error (1001): 渲染"课程不存在"错误状态 + 返回按钮
→ Error (其他): 渲染通用错误状态 + 重试按钮
```

## 5.2 用户操作数据流

### 操作 1: 点击任务行跳转到任务详情

```
触发: 用户点击任务表格中的某一行
→ handleTaskClick(row)
  1. 获取 row.taskId
  2. router.push({ name: 'StudentTaskDetail', params: { taskId: row.taskId } })
  3. 页面导航到 /student/tasks/{taskId}
  4. 任务详情页组件加载
```

### 操作 2: 返回课程列表

```
触发: 用户点击 PageHeader 右侧的"返回课程列表"按钮
→ handleBack()
  1. router.push({ name: 'StudentCourseList' })
  2. 或 router.back()（根据导航历史决定，优先使用 push 到命名路由）
  3. 导航到 /student/courses
```

### 操作 3: 课程不存在时返回

```
触发: 用户看到 code=1001 错误页面，点击"返回课程列表"按钮
→ handleBack()
  1. router.push({ name: 'StudentCourseList' })
  2. 课程列表页重新加载（如果有 keepAlive，直接从缓存恢复）
```

### 操作 4: 通用错误重试

```
触发: 用户在通用 Error 状态下点击 ErrorState 的"重试"按钮
→ fetchCourseDetail(courseId)
  → 数据刷新流程同 5.1
  → 重试成功 → Success 状态
  → 重试仍失败 → 保持 Error 状态，更新错误文案
```

---

# Section 6: API & Mock Specification（页面级精确值）

## 接口 1: GET /api/v1/student/courses/{courseId}

用途: 获取学生所选课程的详细信息及关联任务列表

路径参数:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| courseId | string | 是 | 课程 ID（19位 Snowflake ID） | "1834567890123456789" |

请求 Headers: Authorization: Bearer {jwt_token}

响应数据结构:
```
{
  code: number,          // 0=成功, 1001=课程不存在
  message: string,       // 提示信息
  data: {
    // 课程基本信息
    courseId: string,
    courseName: string,
    courseCode: string,
    teacherName: string,
    teacherEmail: string,
    semester: string,
    credits: number,
    description: string,
    // 关联任务列表
    tasks: ITaskBrief[]
  },
  success: boolean,
  timestamp: string,
  traceId: string,
  requestId: string
}
```

响应数据中的 ITaskBrief 结构:
```
{
  taskId: string,          // 任务 ID（19位 Snowflake ID）
  taskName: string,        // 任务名称
  deadline: string,        // 截止时间（ISO 8601）
  totalScore: number,      // 任务总分（整数）
  mySubmissionStatus: string,  // 我的提交状态（枚举值）
  myScore: number | null   // 我的分数（null 表示尚未评分）
}
```

提交状态枚举值（mySubmissionStatus 有效值）:
- "NOT_SUBMITTED": 未提交
- "SUBMITTED": 已提交
- "AI_EVALUATING": AI 分析中
- "AI_COMPLETED": AI 分析完成，待教师评分
- "TEACHER_SCORING": 教师评分中
- "COMPLETED": 已完成
- "REJECTED": 已退回

Mock 数据示例 1（正常数据 - 课程"Java 程序设计"，5 个任务）:
```
{
  "code": 0,
  "message": "success",
  "data": {
    "courseId": "1834567890123456789",
    "courseName": "Java 程序设计",
    "courseCode": "CS101",
    "teacherName": "王建国",
    "teacherEmail": "wangjg@example.com",
    "semester": "2025-2026-2",
    "credits": 4,
    "description": "本课程系统讲授 Java 编程语言的核心语法、面向对象程序设计思想、集合框架、IO 流、多线程及网络编程等核心内容。通过大量编程实训任务，培养学生扎实的 Java 开发能力。课程涵盖从基础语法到企业级开发的全链路知识体系。",
    "tasks": [
      {
        "taskId": "2834567890123456789",
        "taskName": "Java 基础语法练习",
        "deadline": "2026-03-15T23:59:59.000+08:00",
        "totalScore": 100,
        "mySubmissionStatus": "COMPLETED",
        "myScore": 88
      },
      {
        "taskId": "2834567890123456790",
        "taskName": "面向对象编程实训",
        "deadline": "2026-04-20T23:59:59.000+08:00",
        "totalScore": 100,
        "mySubmissionStatus": "COMPLETED",
        "myScore": 92
      },
      {
        "taskId": "2834567890123456791",
        "taskName": "集合与泛型应用",
        "deadline": "2026-05-10T23:59:59.000+08:00",
        "totalScore": 100,
        "mySubmissionStatus": "TEACHER_SCORING",
        "myScore": null
      },
      {
        "taskId": "2834567890123456792",
        "taskName": "文件 IO 与序列化",
        "deadline": "2026-06-01T23:59:59.000+08:00",
        "totalScore": 80,
        "mySubmissionStatus": "AI_COMPLETED",
        "myScore": null
      },
      {
        "taskId": "2834567890123456793",
        "taskName": "多线程编程实战",
        "deadline": "2026-07-20T23:59:59.000+08:00",
        "totalScore": 100,
        "mySubmissionStatus": "NOT_SUBMITTED",
        "myScore": null
      }
    ]
  },
  "success": true,
  "timestamp": "2026-07-03T10:05:00.123+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678905",
  "path": "/api/v1/student/courses/1834567890123456789",
  "elapsed": 35
}
```

Mock 数据示例 2（课程存在但无任务）:
```
{
  "code": 0,
  "message": "success",
  "data": {
    "courseId": "1834567890123456795",
    "courseName": "人工智能导论",
    "courseCode": "AI101",
    "teacherName": "孙教授",
    "teacherEmail": "sunprof@example.com",
    "semester": "2026-2027-1",
    "credits": 2,
    "description": "本课程介绍人工智能的基本概念、发展历史、主要研究领域和应用场景。涵盖搜索算法、知识表示、机器学习基础、神经网络入门等内容。",
    "tasks": []
  },
  "success": true,
  "timestamp": "2026-07-03T10:05:00.456+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678906",
  "path": "/api/v1/student/courses/1834567890123456795",
  "elapsed": 22
}
```

Mock 数据示例 3（courseId 不存在 - code=1001）:
```
{
  "code": 1001,
  "message": "课程不存在或已被删除",
  "data": null,
  "success": false,
  "timestamp": "2026-07-03T10:05:00.789+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678907",
  "path": "/api/v1/student/courses/9999999999999999999",
  "elapsed": 12
}
```

Mock 数据示例 4（课程有已过期和即将到期的任务）:
```
{
  "code": 0,
  "message": "success",
  "data": {
    "courseId": "1834567890123456790",
    "courseName": "Web 前端开发技术",
    "courseCode": "CS201",
    "teacherName": "李老师",
    "teacherEmail": "lilaoshi@example.com",
    "semester": "2025-2026-2",
    "credits": 3,
    "description": "本课程涵盖 HTML5、CSS3、JavaScript ES6+、Vue.js 框架等前端核心技术。",
    "tasks": [
      {
        "taskId": "2834567890123456794",
        "taskName": "HTML5 与 CSS3 页面布局",
        "deadline": "2026-03-01T23:59:59.000+08:00",
        "totalScore": 100,
        "mySubmissionStatus": "COMPLETED",
        "myScore": 85
      },
      {
        "taskId": "2834567890123456795",
        "taskName": "JavaScript DOM 操作练习",
        "deadline": "2026-04-15T23:59:59.000+08:00",
        "totalScore": 80,
        "mySubmissionStatus": "REJECTED",
        "myScore": null
      },
      {
        "taskId": "2834567890123456796",
        "taskName": "Vue 3 组件开发实训",
        "deadline": "2026-07-10T23:59:59.000+08:00",
        "totalScore": 100,
        "mySubmissionStatus": "SUBMITTED",
        "myScore": null
      },
      {
        "taskId": "2834567890123456797",
        "taskName": "前端项目综合实战",
        "deadline": "2026-07-25T23:59:59.000+08:00",
        "totalScore": 100,
        "mySubmissionStatus": "NOT_SUBMITTED",
        "myScore": null
      }
    ]
  },
  "success": true,
  "timestamp": "2026-07-03T10:05:01.000+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678908",
  "path": "/api/v1/student/courses/1834567890123456790",
  "elapsed": 28
}
```

错误场景:
- 1001: { "code": 1001, "message": "课程不存在或已被删除", "data": null, "success": false } → 展示"课程不存在"Error 状态 + "返回课程列表"按钮
- 401: { "code": 2001, "message": "请先登录", "data": null, "success": false } → 路由守卫拦截
- 403: { "code": 3003, "message": "您没有权限访问该资源", "data": null, "success": false } → 展示 NoPermission（学生不属于该课程）
- 5001: { "code": 5001, "message": "服务器繁忙，请稍后重试", "data": null, "success": false } → 展示通用 Error 状态 + 重试按钮
- Network Error: Axios 捕获 → 展示通用 Error 状态

Mock 延迟: 300ms

Mock 实现位置: src/mock/modules/course.ts（与课程列表共享同一个 Mock 文件）

Mock 逻辑:
- 根据路径参数 courseId 查找预定义的 Mock 课程数据
- courseId 匹配 "1834567890123456789" → 返回 Java 程序设计（5 个任务）
- courseId 匹配 "1834567890123456790" → 返回 Web 前端开发技术（4 个任务，含已过期）
- courseId 匹配 "1834567890123456795" → 返回人工智能导论（0 个任务）
- 其他 courseId → 返回 code=1001

---

# Section 7: Interaction Flows

## 交互 1: 正常加载课程详情

Given: 学生在课程列表页，点击"Java 程序设计"课程的"查看详情"按钮

When:
1. 路由导航到 /student/courses/1834567890123456789
2. CourseDetailPage.vue 组件 onMounted 执行

Then:
1. 页面进入 Loading 状态：PageHeader 标题显示"课程详情"，内容区展示 LoadingState
2. GET /api/v1/student/courses/1834567890123456789 请求发送
3. Mock 返回课程详情 + 5 个任务（300ms 延迟）
4. Loading 退出
5. PageHeader 标题更新为"Java 程序设计"
6. 课程信息卡片渲染：课程编号 CS101、学期 2025-2026-2、授课教师 王建国、教师邮箱 wangjg@example.com（可点击 mailto 链接）、学分 4、课程简介（多行文本）
7. 任务表标题显示"关联任务"+"共 5 个任务"
8. BaseTable 渲染 5 行任务数据:
   - 第 1 行: "Java 基础语法练习", 截止时间 2026-03-15(已过期-红色), 总分 100, TaskStatusBadge=已完成(绿色), 分数 88
   - 第 2 行: "面向对象编程实训", 截止时间 2026-04-20(已过期-红色), 总分 100, TaskStatusBadge=已完成(绿色), 分数 92
   - 第 3 行: "集合与泛型应用", 截止时间 2026-05-10(已过期-红色), 总分 100, TaskStatusBadge=评分中(橙色), 分数 "-"
   - 第 4 行: "文件 IO 与序列化", 截止时间 2026-06-01(已过期-红色), 总分 80, TaskStatusBadge=待评分(绿色), 分数 "-"
   - 第 5 行: "多线程编程实战", 截止时间 2026-07-20(未过期-正常色), 总分 100, TaskStatusBadge=待提交(琥珀色), 分数 "-"

## 交互 2: 课程不存在

Given: 学生尝试访问一个不存在的课程 ID（courseId 不在 Mock 数据中）

When:
1. 学生直接导航到 /student/courses/9999999999999999999
2. CourseDetailPage.vue onMounted 发起 API 请求

Then:
1. 页面进入 Loading 状态
2. GET /api/v1/student/courses/9999999999999999999 请求发送
3. Mock 返回 code=1001, message="课程不存在或已被删除"
4. Loading 退出
5. 进入 Error (code=1001) 状态：ErrorState 组件展示红色 AlertCircle 图标 + "课程不存在或已被删除"文案。不显示重试按钮
6. ErrorState 下方显示 BaseButton（type="primary"），文案"返回课程列表"
7. 学生点击"返回课程列表"按钮 → 导航到 /student/courses

## 交互 3: 点击任务行跳转

Given: 学生在课程详情页，任务表格中有 5 个任务

When:
1. 学生点击第 5 行"多线程编程实战"任务

Then:
1. 获取该行的 taskId = "2834567890123456793"
2. router.push({ name: 'StudentTaskDetail', params: { taskId: '2834567890123456793' } })
3. 浏览器 URL 变为 /student/tasks/2834567890123456793
4. 学生任务详情页组件加载

## 交互 4: 服务端错误后重试

Given: 学生在课程详情页，Mock 返回 server error（模拟条件触发）

When:
1. 页面加载时 Mock 返回 code=5001
2. 页面进入通用 Error 状态：ErrorState 显示"课程信息加载失败，请稍后重试"+ "重试"按钮

When（后续操作）:
3. 学生点击"重试"按钮

Then:
4. 页面重新进入 Loading 状态
5. 重新发送 GET /api/v1/student/courses/{courseId} 请求
6. 本次 Mock 返回成功数据（正常）
7. 页面进入 Success 状态，完整渲染课程信息和任务表格

## 交互 5: 课程无关联任务

Given: 学生选择"人工智能导论"课程（Mock 中该课程 tasks 为空数组）

When:
1. 导航到该课程的详情页
2. API 返回课程信息正常但 tasks 为空数组

Then:
1. Loading 退出
2. 课程信息卡片正常渲染（课程名、编号、教师等）
3. 任务表标题显示"关联任务"+"共 0 个任务"
4. 表格位置展示 EmptyState 组件，文案"该课程暂无实训任务"
5. BaseTable 不渲染

## 交互 6: 点击教师邮箱

Given: 学生在课程详情页，课程信息卡片展示教师邮箱

When:
1. 学生点击教师邮箱链接 wangjg@example.com

Then:
1. 浏览器默认邮件客户端打开（mailto:wangjg@example.com）
2. 页面保持在当前状态，不发生导航

---

# Section 8: Permission Design

| 属性 | 值 |
|------|-----|
| 页面权限标识 | student:course:view |
| 页面允许角色 | ["student"] |
| 路由权限检查 | 路由配置 meta.roles = ["student"]，由 permission.ts 导航守卫检查 |
| 按钮级权限 | 无（所有操作按钮对所有学生可见） |

路由配置（追加到 src/router/routes/student.ts）:
```
{
  path: "courses/:courseId",
  name: "StudentCourseDetail",
  component: () => import("@/pages/student/CourseDetailPage.vue"),
  meta: {
    title: "课程详情",
    icon: "BookOpen",
    sort: 0,
    hidden: true,
    keepAlive: false,
    roles: ["student"]
  }
}
```

注意: meta.hidden = true 表示此路由不在侧边栏菜单中展示（通过课程列表页导航进入）。meta.keepAlive = false 表示每次进入页面都重新加载数据，确保课程信息是最新的。

---

# Section 9: Acceptance Criteria

AC-1: Given 学生已登录，在课程列表页 When 点击某课程的"查看详情"按钮 Then 页面导航到 /student/courses/{courseId}，展示课程完整信息（课程名称、编号、授课教师、教师邮箱、学期、学分、简介）和关联任务表格（任务名称、截止时间、总分、提交状态标签、分数）

AC-2: Given 课程详情页加载中 When API 响应超过 300ms Then 页面展示 LoadingState（跳动圆点 + "正在加载课程信息..."），课程信息卡片和任务表格不渲染

AC-3: Given 关联任务列表非空 When 页面加载完成 Then 任务表格每行的"我的提交状态"列使用 TaskStatusBadge 组件渲染，不同状态展示不同颜色标签（待提交=琥珀色、已提交=蓝色、AI分析中=紫色、待评分/已完成=绿色、评分中=橙色、已退回=红色）

AC-4: Given 关联任务列表为空（tasks=[]） When 页面加载完成 Then 课程信息卡片正常渲染，任务区域展示 EmptyState 组件，"该课程暂无实训任务"

AC-5: Given 课程 ID 无效（code=1001） When 页面加载完成 Then 展示 ErrorState 组件，"课程不存在或已被删除"文案，"返回课程列表"按钮，无重试按钮

AC-6: Given 任务表中某任务的截止时间已过期 When 页面渲染 Then 该行截止时间文字显示为红色（#EF4444），以警示学生

AC-7: Given 学生点击某个任务行 When 触发导航 Then 浏览器跳转到 /student/tasks/{taskId}，加载任务详情页

AC-8: Given 学生点击 PageHeader 右侧"返回课程列表"按钮 When 触发导航 Then 返回 /student/courses，课程列表页重新展示

AC-9: Given 学生点击教师邮箱链接 When 触发 mailto Then 系统默认邮件客户端打开，页面不跳转

---

# Section 10: Files to Create / Modify

## 新建文件

```
├── src/pages/student/CourseDetailPage.vue            # 课程详情页面组件
├── src/components/business/TaskStatusBadge.vue       # 任务提交状态标签组件（新建 Business 组件）
├── src/types/task.ts                                  # 任务相关 TypeScript 类型定义（ITaskBrief, TaskSubmissionStatus）
├── src/types/course.ts                                # 课程详情类型定义（ICourseDetail, ICourseInfo）
└── docs/page-analysis/student-course-detail.md        # 本文件
```

## 修改文件

```
├── src/router/routes/student.ts                       # 追加 StudentCourseDetail 路由
├── src/mock/modules/course.ts                         # 追加 GET /api/v1/student/courses/{courseId} Mock（与课程列表共享文件）
├── src/api/modules/course.ts                          # 追加 getStudentCourseDetail API 函数
└── src/types/course.ts                                # 追加 ICourseDetail 类型
```

## 依赖的已有组件（本页面不修改它们，但依赖它们的存在）

```
├── src/layouts/AppLayout.vue                          # 页面外层布局
├── src/components/layout/PageHeader.vue               # 页面标题栏
├── src/components/layout/PageContainer.vue            # 页面内容容器
├── src/components/base/BaseButton.vue                 # 返回按钮
├── src/components/base/BaseTable.vue                  # 任务列表表格
├── src/components/common/LoadingState.vue             # 加载中状态
├── src/components/common/ErrorState.vue               # 错误状态
├── src/components/common/EmptyState.vue               # 空任务列表状态
├── src/stores/useUserStore.ts                         # 用户信息 Store
└── src/stores/useAppStore.ts                          # 应用全局状态 Store
```

## 类型定义补充（src/types/course.ts 追加）

ICourseDetail（课程详情）:
- courseId: string
- courseName: string
- courseCode: string
- teacherName: string
- teacherEmail: string
- semester: string
- credits: number
- description: string
- tasks: ITaskBrief[]

## 类型定义文件（src/types/task.ts 新建）

ITaskBrief（任务简表项）:
- taskId: string
- taskName: string
- deadline: string
- totalScore: number
- mySubmissionStatus: TaskSubmissionStatus
- myScore: number | null

TaskSubmissionStatus（提交状态枚举）:
- type TaskSubmissionStatus = 'NOT_SUBMITTED' | 'SUBMITTED' | 'AI_EVALUATING' | 'AI_COMPLETED' | 'TEACHER_SCORING' | 'COMPLETED' | 'REJECTED'

## 注意事项

1. TaskStatusBadge 是学生端和教师端共用组件。7 种状态的文案和颜色映射硬编码在组件内部常量 STATUS_MAP 中。组件不依赖任何 Pinia Store 或 API 调用，是纯展示组件。
2. 截止时间过期判断：当前日期 > deadline 日期（不考虑时分秒）时，文字颜色设为 #EF4444。当前日期 <= deadline 日期时，使用默认文字颜色。判断逻辑在页面组件 computed 属性中完成，或通过 BaseTable columns 的 cell render 函数判断。
3. 课程信息卡片使用简单的 div + CSS Grid 布局，不使用 Element Plus el-card 组件（现有 BaseCard 未在 Component Library 最终确认，为避免依赖不确定组件，直接使用原生 div + Design Tokens）。
4. myScore 为 null 时的展示：显示 "-" 占位符，color 使用 --color-text-placeholder (#94A3B8)。区分"尚未评分"（null）和"得分 0"（数字 0）。
5. TaskStatusBadge 在表格中的 size 使用 "small"，以适配表格行高度（约 40-44px）。
