# Page Analysis: 学生课程列表页 (Student Course List)

**文档版本**: v1.0
**创建日期**: 2026-07-03
**分析范围**: 学生课程列表页面及搜索、分页完整流程

---

# Section 1: Page Identity

| 属性 | 值 |
|------|-----|
| 页面名称 | 学生课程列表页 |
| 页面文件 | src/pages/student/CourseListPage.vue |
| 路由路径 | /student/courses |
| 路由名称 | StudentCourseList |
| 所属 Sprint | Sprint 2 |
| 所属 Feature | F-2-01（参见《Sprint 2 Spec》） |
| 页面角色 | 学生 |
| 页面复杂度 | L2（表格列表 + 关键词搜索 + 分页 + 行点击导航，业务逻辑标准） |
| 原型参考 | 参见《UI Design System v1.0》学生课程列表原型 |

页面职责：学生登录后查看自己已选修的所有课程列表。支持按课程名称或课程编号进行关键词搜索，支持分页浏览。点击表格行或"查看详情"按钮可跳转到课程详情页。

---

# Section 2: Page Layout Structure

页面嵌套在 AppLayout 中。AppLayout 提供 Sidebar + Navbar + PageContainer 骨架，本页面仅负责 PageContainer 内部的内容渲染。

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
|  |           |  |  标题: "我的课程"               |  | |
|  |           |  |  extra slot: BaseInput         |  | |
|  |           |  |  (搜索框, width: 280px)       |  | |
|  |           |  +-----------------------------+  | |
|  |           |  [间距: 24px]                     |  | |
|  |           |  +-----------------------------+  | |
|  |           |  |  BaseTable (100% 宽度)         |  | |
|  |           |  |  列: 课程名称, 课程编号,       |  | |
|  |           |  |  授课教师, 学期, 学分,         |  | |
|  |           |  |  任务数, 操作                   |  | |
|  |           |  |  stripe: true                  |  | |
|  |           |  |  bordered: true                |  | |
|  |           |  |  size: "default"               |  | |
|  |           |  +-----------------------------+  | |
|  |           |  [间距: 16px]                     |  | |
|  |           |  +-----------------------------+  | |
|  |           |  |  BasePagination (右对齐)       |  | |
|  |           |  |  size: "default"               |  | |
|  |           |  |  showTotal: true               |  | |
|  |           |  |  showJumper: true              |  | |
|  |           |  +-----------------------------+  | |
|  |           |                                    | |
|  +-----------+-----------------------------------+ |
+--------------------------------------------------+
```

**区域说明**：

| 区域 | 宽度 | 高度 | 对齐 | 滚动策略 | 说明 |
|------|------|------|------|---------|------|
| PageHeader | 100% 父容器 | auto (约 48px 内容高度) | flex space-between | 不滚动 | 左侧标题"我的课程"，右侧搜索框 |
| 搜索框 | 280px (固定) | 36px | 右对齐 | 不滚动 | BaseInput 组件，type="text"，placeholder="搜索课程名称或编号..."，clearable=true，prefixIcon=Search 图标。输入后按 Enter 或点击清除按钮右侧的搜索图标触发搜索 |
| 表格区域 | 100% 父容器 | auto | 左对齐 | 不滚动（表格内容随页面滚动） | BaseTable 组件，7 列数据列 |
| 分页区 | 100% 父容器 | auto | 右对齐 | 不滚动 | BasePagination 组件，位于表格下方 |

**响应式行为**：
- 1920px 分辨率：表格各列按比例分配宽度，搜索框 280px
- 1366px 分辨率：表格列宽按比例缩小，搜索框 240px，学分列和任务数列使用 JetBrains Mono 字体等宽展示
- 小于 768px：搜索框宽度 100%（换行至标题下方），表格开启横向滚动（maxHeight 控制）

---

# Section 3: Component Tree & Specification

## 3.1 组件树

```
CourseListPage.vue（页面组件，在 AppLayout 内渲染）
├── PageHeader（来自 Component Library，src/components/layout/PageHeader.vue）
│   ├── 标题文字: "我的课程"（硬编码）
│   └── extra slot
│       └── BaseInput（来自 Component Library，src/components/base/BaseInput.vue）
│           搜索输入框，v-model="searchKeyword"，@enter="handleSearch"，@clear="handleSearch"
├── <div> 表格容器
│   ├── LoadingState（条件渲染: loading=true 且 data.length=0）
│   │   来自 Component Library，src/components/common/LoadingState.vue
│   ├── ErrorState（条件渲染: error 状态）
│   │   来自 Component Library，src/components/common/ErrorState.vue
│   │   Props: message="课程列表加载失败，请稍后重试", showRetry=true
│   │   @retry="fetchCourses()"
│   ├── EmptyState（条件渲染: data.length=0 且 loading=false 且 error=null）
│   │   来自 Component Library，src/components/common/EmptyState.vue
│   │   Props: description="暂无课程数据，请联系管理员选课"
│   ├── BaseTable（条件渲染: Success 状态）
│   │   来自 Component Library，src/components/base/BaseTable.vue
│   │   Props: columns=courseColumns, data=courseList, loading=false, stripe=true, bordered=true, size="default"
│   │   @rowClick="handleRowClick"
│   │   ├── 列1: 课程名称 (prop: "courseName", label: "课程名称", minWidth: 180)
│   │   │   渲染为普通文本，cursor: pointer，hover 时显示 Primary 色 #3B82F6
│   │   ├── 列2: 课程编号 (prop: "courseCode", label: "课程编号", width: 140)
│   │   │   渲染为普通文本，使用 JetBrains Mono 等宽字体
│   │   ├── 列3: 授课教师 (prop: "teacherName", label: "授课教师", width: 120)
│   │   │   渲染为普通文本
│   │   ├── 列4: 学期 (prop: "semester", label: "学期", width: 120)
│   │   │   渲染为普通文本
│   │   ├── 列5: 学分 (prop: "credits", label: "学分", width: 80, align: "center")
│   │   │   渲染为数字，使用 JetBrains Mono 等宽字体，格式 "{credits} 学分"
│   │   ├── 列6: 任务数 (prop: "taskCount", label: "任务数", width: 80, align: "center")
│   │   │   渲染为数字，使用 JetBrains Mono 等宽字体，格式 "{taskCount} 个"
│   │   └── 列7: 操作 (label: "操作", width: 120, align: "center", fixed: "right")
│   │       渲染为 BaseButton（type="primary", size="small", plain）
│   │       按钮文案: "查看详情"，@click="handleViewDetail(row)"
│   └── BasePagination（条件渲染: Success 状态且 total > 0）
│       来自 Component Library，src/components/base/BasePagination.vue
│       Props: v-model:currentPage="pagination.page", v-model:pageSize="pagination.pageSize", total="pagination.total", pageSizes="[10, 20, 50]", size="default"
│       @change="handlePageChange"
└── NoPermission（条件渲染: 路由守卫未拦截但 Store 判断无权限时）
    来自 Component Library，src/components/common/NoPermission.vue
```

## 3.2 本页面需要新建的子组件

**本页面无新建子组件**。课程列表页逻辑简洁（列表 + 搜索 + 分页），使用已有 Base 组件和 Common 组件组装。业务逻辑（搜索、分页、导航）内聚在页面组件中，不超过 150 行。页面组件直接使用 Composition API 管理本地状态，不引入额外 Composable。如果后续课程列表页增加更多交互（如筛选、排序、批量操作），再抽取 `src/composables/useCourseList.ts`。

## 3.3 已有组件引用

**引用组件 1: PageHeader（来自 Component Library，src/components/layout/PageHeader.vue）**

关键 Props 配置:
| Prop | 值 | 说明 |
|------|-----|------|
| title | "我的课程" | 页面标题，硬编码中文 |

关键 Slots 配置:
| Slot | 内容 | 说明 |
|------|------|------|
| extra | BaseInput 搜索框 | 标题栏右侧操作区 |

**引用组件 2: BaseInput（来自 Component Library，src/components/base/BaseInput.vue）**

关键 Props 配置:
| Prop | 值 | 说明 |
|------|-----|------|
| modelValue | searchKeyword | 双向绑定搜索关键词 |
| placeholder | "搜索课程名称或编号..." | 占位提示文案 |
| size | "default" | 默认尺寸 |
| clearable | true | 显示一键清除按钮 |
| prefixIcon | "Search" | 搜索图标（Lucide Icons） |

关键 Events 配置:
| Event | 处理函数 | 说明 |
|------|---------|------|
| update:modelValue | searchKeyword 更新 | 输入值变化 |
| enter | handleSearch() | 按 Enter 键触发搜索 |
| clear | handleSearch() | 点击清除按钮触发搜索（清空关键词后重新加载） |

**引用组件 3: BaseTable（来自 Component Library，src/components/base/BaseTable.vue）**

关键 Props 配置:
| Prop | 值 | 说明 |
|------|-----|------|
| columns | courseColumns | 课程表格列定义数组 |
| data | courseList | 课程列表数据 |
| loading | false | 表格本身不显示 loading，Loading 状态由外层 LoadingState 处理 |
| stripe | true | 斑马纹行样式 |
| bordered | true | 显示边框 |
| size | "default" | 默认尺寸 |
| rowKey | "courseId" | 行唯一标识字段 |

关键 Events 配置:
| Event | 处理函数 | 说明 |
|------|---------|------|
| rowClick | handleRowClick(row) | 点击行导航至课程详情页 |

**引用组件 4: BaseButton（来自 Component Library，src/components/base/BaseButton.vue）**

关键 Props 配置（操作列按钮）:
| Prop | 值 | 说明 |
|------|-----|------|
| type | "primary" | 主色调 #3B82F6 |
| size | "small" | 小尺寸，适配表格行高 |
| plain | true | 朴素样式（仅边框 + 文字，背景透明） |

**引用组件 5: BasePagination（来自 Component Library，src/components/base/BasePagination.vue）**

关键 Props 配置:
| Prop | 值 | 说明 |
|------|-----|------|
| currentPage | pagination.page | 双向绑定当前页码 |
| pageSize | pagination.pageSize | 双向绑定每页条数 |
| total | pagination.total | 总记录数 |
| pageSizes | [10, 20, 50] | 每页条数可选值 |
| size | "default" | 默认尺寸 |
| showTotal | true | 显示"共 N 条" |
| showJumper | true | 显示页码跳转输入框 |

**引用组件 6: LoadingState（来自 Component Library，src/components/common/LoadingState.vue）**

关键 Props 配置:
| Prop | 值 | 说明 |
|------|-----|------|
| text | "正在加载课程列表..." | 加载中文案 |

**引用组件 7: ErrorState（来自 Component Library，src/components/common/ErrorState.vue）**

关键 Props 配置:
| Prop | 值 | 说明 |
|------|-----|------|
| message | "课程列表加载失败，请稍后重试" | 错误提示文案 |
| showRetry | true | 显示重试按钮 |

**引用组件 8: EmptyState（来自 Component Library，src/components/common/EmptyState.vue）**

关键 Props 配置:
| Prop | 值 | 说明 |
|------|-----|------|
| description | "暂无课程数据，请联系管理员选课" | 空数据时搜索无结果时为不同文案 |

---

# Section 4: State Design（页面级）

| 状态 | 触发条件 | UI 表现 | 持续时间 | 退出条件 |
|------|---------|--------|---------|---------|
| Loading | 页面 onMounted 时触发首次数据加载；或搜索/翻页操作触发数据刷新 | 页面展示 LoadingState 组件（三个跳动圆点动画 + "正在加载课程列表..."文字），LoadingState 居中于表格区域。PageHeader 正常渲染。BaseTable 和 BasePagination 不渲染 | 直到 API 返回数据或超时（超过 10 秒显示"加载超时，请刷新重试"） | API 返回数据（进入 Success 或 Empty）或 API 返回错误（进入 Error） |
| Empty | API 返回成功但 list 为空数组（total=0），且 loading=false，且 error=null | PageHeader 正常渲染（搜索框清空状态下）。表格区域展示 EmptyState 组件：上传图标（默认 SVG）+ "暂无课程数据，请联系管理员选课"文案。不展示 BaseTable 和 BasePagination | 持续直到数据变更或用户执行刷新 | 用户刷新后 API 返回非空数据 |
| Empty (搜索无结果) | API 返回成功但 list 为空数组（total=0），且 loading=false，且 searchKeyword 不为空 | PageHeader 正常渲染（搜索框保留关键词）。表格区域展示 EmptyState 组件：上传图标 + "未找到匹配的课程，请尝试其他关键词"文案 + "清除搜索"操作按钮（actionText="清除搜索"）。不展示 BaseTable 和 BasePagination | 持续直到用户清除搜索或更改关键词 | 用户点击"清除搜索"按钮：清空 searchKeyword，重新加载全部数据 |
| Error | API 请求失败（网络错误、服务端 500、业务错误码） | PageHeader 正常渲染。表格区域展示 ErrorState 组件：红色 AlertCircle 图标 + 错误文案（根据错误类型区分："课程列表加载失败，请稍后重试" / "网络连接失败，请检查网络"）+ "重试"按钮 | 持续直到重试成功或用户离开页面 | 用户点击"重试"按钮且 API 返回成功 |
| Success | API 返回成功且 list 非空（total > 0） | PageHeader + 搜索框 + BaseTable（含全部 7 列和所有数据行）+ BasePagination（右对齐，显示"共 N 条"）完整渲染。表格首行高亮 hover 效果。课程名称列鼠标悬浮变色 | 持续直到用户离开页面或触发搜索/翻页刷新 | 用户离开页面或触发数据刷新操作 |
| NoPermission | 不适用 | 学生角色已通过路由守卫 meta.roles=["student"] 控制。已登录非学生用户由路由守卫拦截，不会到达本页面。若极端情况下 Store 判断角色非 student，渲染 NoPermission 组件 | - | - |
| Offline | navigator.onLine 变为 false（浏览器检测到网络断开） | AppLayout 级全局处理（不在本页面组件范围内）：顶部固定黄色提示条"网络连接已断开"，高度 40px，不遮挡内容区，内容区下移 40px。本页面所有操作（搜索、翻页、行点击）弹出 ElMessage.warning("网络连接已断开，请检查网络")，不发送 API 请求 | 直到网络恢复 | navigator.onLine 变为 true，提示条自动消失，自动重新加载当前页数据 |

**Offline 状态实现细节**：
- Offline 状态由 AppLayout 或全局 Composable（useOnlineStatus）统一管理，本页面组件通过 inject 或 Pinia Store 获取 online 状态
- 本页面 onMounted 中监听 online/offline 状态变化：online 从 false 变为 true 时自动调用 fetchCourses() 刷新数据
- 离线时搜索框和分页组件不禁用，但操作时直接提示用户而非发送请求

---

# Section 5: Data Flow

## 5.1 页面加载数据流

```
学生导航到 /student/courses（点击侧边栏菜单"我的课程"）
→ 路由守卫 beforeEach 触发
→ auth.ts: Token 存在且有效 → 放行
→ permission.ts: 检查 meta.roles=["student"] → 当前用户角色为 student → 放行
→ AppLayout 渲染（Sidebar + Navbar + PageContainer）
→ CourseListPage.vue onMounted()
→ 初始化本地状态:
  - loading = true
  - searchKeyword = ''（或从路由 query 恢复：route.query.keyword）
  - pagination = { page: 1, pageSize: 20, total: 0 }
  - courseList = []
  - error = null
→ 调用 fetchCourses()
→ fetchCourses() 内部:
  1. 设置 loading = true
  2. 构造请求参数: { page: pagination.page, pageSize: pagination.pageSize, keyword: searchKeyword || undefined }
  3. 调用 courseApi.getStudentCourses(params)
  4. Axios 发送 GET /api/v1/student/courses?page=1&pageSize=20
  5. Mock 层拦截（VITE_USE_MOCK=true）→ Mock 返回分页数据（300ms 延迟）
  6. 响应成功 (code=0):
     a. courseList = response.data.list
     b. pagination.total = response.data.total
     c. pagination.page = response.data.page
     d. pagination.pageSize = response.data.pageSize
     e. loading = false
     f. error = null
  7. 响应失败 (code!=0):
     a. loading = false
     b. error = response.message || "课程列表加载失败，请稍后重试"
→ Success 状态: 渲染 BaseTable + BasePagination
```

## 5.2 用户操作数据流

### 操作 1: 搜索课程

```
触发: 用户在搜索框输入关键词后按 Enter 键，或输入后点击搜索图标
→ handleSearch()
  1. pagination.page = 1（搜索时重置到第一页）
  2. 调用 fetchCourses()（keywords 参数为当前 searchKeyword.trim()）
  → 数据刷新流程同 5.1
  → 搜索框保留已输入的关键词，右侧显示清除按钮（clearable=true）
  → 如 searchKeyword 为空字符串（用户点击清除按钮），等效于加载全部数据
  → 搜索后返回空数据 → Empty（搜索无结果）状态，文案变为"未找到匹配的课程，请尝试其他关键词"
```

### 操作 2: 分页切换

```
触发: 用户点击 BasePagination 的页码按钮、上一页/下一页按钮、或修改每页条数
→ handlePageChange({ page, pageSize })
  1. pagination.page = page
  2. pagination.pageSize = pageSize
  3. 调用 fetchCourses()
  → 数据刷新流程同 5.1
  → 表格行数据更新为新页数据
  → 分页组件更新当前页码和总条数
  → pageSize 变更时，page 自动重置为 1（由 BasePagination 内部处理）
```

### 操作 3: 点击表格行或"查看详情"按钮

```
触发: 用户点击表格行或操作列"查看详情"按钮
→ handleRowClick(row) / handleViewDetail(row)
  1. 获取 row.courseId
  2. router.push({ name: 'StudentCourseDetail', params: { courseId: row.courseId } })
  3. 页面导航到 /student/courses/{courseId}
  4. 课程详情页组件加载，发起详情 API 请求
```

### 操作 4: 网络错误后重试

```
触发: 用户在 Error 状态下点击 ErrorState 的"重试"按钮
→ ErrorState @retry 事件 → fetchCourses()
  → 数据刷新流程同 5.1
  → 重试成功 → Success 或 Empty 状态
  → 重试失败 → 保持 Error 状态，更新错误文案
```

---

# Section 6: API & Mock Specification（页面级精确值）

## 接口 1: GET /api/v1/student/courses

用途: 获取学生已选课程列表（分页 + 关键词搜索）

请求参数:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| page | number | 否 | 页码，从 1 开始，默认 1 | 1 |
| pageSize | number | 否 | 每页条数，可选 10/20/50，默认 20 | 20 |
| keyword | string | 否 | 搜索关键词（匹配课程名称或课程编号） | "Java" |

请求 Headers: Authorization: Bearer {jwt_token}

响应数据结构:
```
{
  code: number,          // 0=成功
  message: string,       // "success"
  data: {
    list: ICourse[],     // 课程列表
    page: number,        // 当前页码
    pageSize: number,    // 每页条数
    total: number,       // 总记录数
    totalPages: number   // 总页数
  },
  success: boolean,      // true
  timestamp: string,     // ISO 8601
  traceId: string,       // UUID v4
  requestId: string      // UUID v4
}
```

响应数据中的 ICourse 结构:
```
{
  courseId: string,      // 课程 ID（19位 Snowflake ID，以 String 传输）
  courseName: string,    // 课程名称
  courseCode: string,    // 课程编号（如 "CS101"）
  teacherName: string,   // 授课教师姓名
  semester: string,      // 学期（如 "2025-2026-2"）
  credits: number,       // 学分（整数，如 3、4）
  taskCount: number,     // 该课程下的任务总数
  description: string    // 课程简介（列表页不展示，但数据结构包含）
}
```

Mock 数据示例 1（正常数据 - 6 条课程，第 1 页，每页 20 条）:
```
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "courseId": "1834567890123456789",
        "courseName": "Java 程序设计",
        "courseCode": "CS101",
        "teacherName": "王建国",
        "semester": "2025-2026-2",
        "credits": 4,
        "taskCount": 5,
        "description": "本课程系统讲授 Java 编程语言的核心语法、面向对象程序设计思想、集合框架、IO 流、多线程及网络编程等核心内容。通过大量编程实训任务，培养学生扎实的 Java 开发能力。"
      },
      {
        "courseId": "1834567890123456790",
        "courseName": "Web 前端开发技术",
        "courseCode": "CS201",
        "teacherName": "李老师",
        "semester": "2025-2026-2",
        "credits": 3,
        "taskCount": 4,
        "description": "本课程涵盖 HTML5、CSS3、JavaScript ES6+、Vue.js 框架等前端核心技术。通过项目驱动方式，让学生掌握现代 Web 前端开发完整技能。"
      },
      {
        "courseId": "1834567890123456791",
        "courseName": "数据结构与算法",
        "courseCode": "CS301",
        "teacherName": "张教授",
        "semester": "2025-2026-1",
        "credits": 4,
        "taskCount": 6,
        "description": "讲解线性表、栈、队列、树、图等核心数据结构及排序、查找、图算法等经典算法。培养学生算法设计能力和计算思维。"
      },
      {
        "courseId": "1834567890123456792",
        "courseName": "数据库原理与应用",
        "courseCode": "CS401",
        "teacherName": "赵老师",
        "semester": "2025-2026-2",
        "credits": 3,
        "taskCount": 3,
        "description": "系统讲授关系数据库理论、SQL 语言、数据库设计、MySQL 数据库管理及 JDBC 编程。理论与实践并重。"
      },
      {
        "courseId": "1834567890123456793",
        "courseName": "软件工程",
        "courseCode": "SE101",
        "teacherName": "王建国",
        "semester": "2025-2026-2",
        "credits": 3,
        "taskCount": 2,
        "description": "介绍软件生命周期、需求分析、系统设计、测试、项目管理等软件工程核心知识。以小组项目形式进行实践。"
      },
      {
        "courseId": "1834567890123456794",
        "courseName": "Python 数据分析",
        "courseCode": "CS501",
        "teacherName": "周老师",
        "semester": "2025-2026-1",
        "credits": 2,
        "taskCount": 3,
        "description": "学习 Python 编程基础、NumPy、Pandas、Matplotlib 等数据分析工具，掌握数据清洗、可视化和基本分析方法。"
      }
    ],
    "page": 1,
    "pageSize": 20,
    "total": 6,
    "totalPages": 1
  },
  "success": true,
  "timestamp": "2026-07-03T10:00:00.123+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "path": "/api/v1/student/courses",
  "elapsed": 45
}
```

Mock 数据示例 2（空数据 - 学生尚未选课）:
```
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [],
    "page": 1,
    "pageSize": 20,
    "total": 0,
    "totalPages": 0
  },
  "success": true,
  "timestamp": "2026-07-03T10:00:00.456+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678902",
  "path": "/api/v1/student/courses",
  "elapsed": 12
}
```

Mock 数据示例 3（搜索匹配 - keyword="Java" 返回 1 条）:
```
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "courseId": "1834567890123456789",
        "courseName": "Java 程序设计",
        "courseCode": "CS101",
        "teacherName": "王建国",
        "semester": "2025-2026-2",
        "credits": 4,
        "taskCount": 5,
        "description": "本课程系统讲授 Java 编程语言的核心语法..."
      }
    ],
    "page": 1,
    "pageSize": 20,
    "total": 1,
    "totalPages": 1
  },
  "success": true,
  "timestamp": "2026-07-03T10:00:01.789+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678903",
  "path": "/api/v1/student/courses?keyword=Java",
  "elapsed": 28
}
```

Mock 数据示例 4（搜索无结果 - keyword="不存在课程" 返回 0 条）:
```
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [],
    "page": 1,
    "pageSize": 20,
    "total": 0,
    "totalPages": 0
  },
  "success": true,
  "timestamp": "2026-07-03T10:00:02.100+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678904",
  "path": "/api/v1/student/courses?keyword=不存在课程",
  "elapsed": 15
}
```

错误场景:
- 401: 响应体 { "code": 2001, "message": "请先登录", "data": null, "success": false } → 路由守卫拦截，跳转登录页
- 403: 响应体 { "code": 3001, "message": "您没有权限访问此功能", "data": null, "success": false } → 展示 NoPermission 状态
- 500: 响应体 { "code": 5001, "message": "服务器繁忙，请稍后重试", "data": null, "success": false } → 展示 Error 状态，"课程列表加载失败，请稍后重试" + 重试按钮
- Network Error: Axios 捕获网络错误 → 展示 Error 状态，"网络连接失败，请检查网络" + 重试按钮

Mock 延迟: 300ms（所有列表接口统一延迟，模拟真实网络环境）

Mock 实现位置: src/mock/modules/course.ts

Mock 搜索逻辑:
- keyword 为空: 返回全部 6 条 Mock 课程数据
- keyword 不为空: 对 courseName 和 courseCode 字段执行 includes() 过滤（大小写不敏感）
- 分页逻辑: 根据 page 和 pageSize 对过滤后数组做 slice()
- 关键词"全部"或"all": 返回全部数据（等效于空关键词）

---

# Section 7: Interaction Flows

## 交互 1: 正常加载课程列表

Given: 学生已登录，当前在 Dashboard 页面

When:
1. 学生点击侧边栏菜单"我的课程"
2. 路由导航到 /student/courses
3. CourseListPage.vue 组件 onMounted 触发

Then:
1. 页面进入 Loading 状态：PageHeader 显示标题"我的课程"，表格区域显示 LoadingState（三个跳动圆点 + "正在加载课程列表..."文字）
2. GET /api/v1/student/courses?page=1&pageSize=20 请求发送
3. Mock 返回 6 条课程数据（300ms 延迟）
4. Loading 状态退出
5. BaseTable 渲染 6 行课程数据，7 列（课程名称、课程编号、授课教师、学期、学分、任务数、操作）
6. BasePagination 显示"共 6 条"，页码为 1
7. 搜索框为空，placeholder 显示"搜索课程名称或编号..."
8. 表格行 hover 时背景色变浅灰，课程名称列文字变为 Primary 色 #3B82F6

## 交互 2: 搜索课程（有结果）

Given: 学生在课程列表页，当前展示全部 6 条课程

When:
1. 学生在搜索框输入"Java"
2. 学生按 Enter 键

Then:
1. 表格区域进入 Loading 状态（数据正在刷新）
2. GET /api/v1/student/courses?page=1&pageSize=20&keyword=Java 请求发送
3. Mock 返回 1 条匹配结果（"Java 程序设计"）
4. Loading 状态退出
5. BaseTable 渲染 1 行数据
6. BasePagination 显示"共 1 条"，页码为 1
7. 搜索框保留"Java"文字，右侧显示清除按钮（X 图标）
8. 页面未滚动到顶部（维持原位）

## 交互 3: 搜索课程（无结果）

Given: 学生在课程列表页，当前展示全部 6 条课程

When:
1. 学生在搜索框输入"不存在课程"
2. 学生按 Enter 键

Then:
1. 表格区域进入 Loading 状态
2. GET /api/v1/student/courses?page=1&pageSize=20&keyword=不存在课程 请求发送
3. Mock 返回 0 条数据（total=0, list=[]）
4. Loading 状态退出
5. 进入 Empty (搜索无结果) 状态：表格区域展示 EmptyState 组件，文案"未找到匹配的课程，请尝试其他关键词"，底部显示"清除搜索"按钮
6. 搜索框保留"不存在课程"文字
7. BaseTable 和 BasePagination 不渲染

When（后续操作）:
4. 学生点击"清除搜索"按钮

Then:
5. 搜索框清空
6. 重新加载全部课程数据（GET /api/v1/student/courses?page=1&pageSize=20）
7. 返回 Success 状态，展示全部 6 条课程

## 交互 4: 点击行进入课程详情

Given: 学生在课程列表页，列表中有 6 条课程数据

When:
1. 学生点击第 1 行"Java 程序设计"课程的任意列（或操作列"查看详情"按钮）

Then:
1. 获取该行的 courseId = "1834567890123456789"
2. router.push({ name: 'StudentCourseDetail', params: { courseId: '1834567890123456789' } })
3. 浏览器 URL 变为 /student/courses/1834567890123456789
4. StudentCourseDetailPage.vue 组件加载
5. 课程列表页组件保持存活（keepAlive 如配置）或销毁（路由离开）

## 交互 5: 分页切换

Given: 学生课程列表页，当前展示第 1 页，共 26 条课程（total=26, pageSize=10）

When:
1. 学生点击 BasePagination 的第 2 页按钮

Then:
1. 表格区域进入 Loading 状态
2. GET /api/v1/student/courses?page=2&pageSize=10 请求发送
3. Mock 返回第 2 页的 10 条数据
4. Loading 状态退出
5. BaseTable 渲染新页的 10 条数据
6. BasePagination 当前页码更新为 2，仍显示"共 26 条"
7. 页面滚动到表格顶部（可选：smooth scroll 行为）

## 交互 6: 课程列表加载失败（网络错误）

Given: 学生访问课程列表页

When:
1. 页面 onMounted 触发数据加载
2. Mock 层模拟 Network Error（Axios 抛出无响应错误）

Then:
1. Loading 状态退出
2. 进入 Error 状态：表格区域展示 ErrorState 组件，红色 AlertCircle 图标 + "网络连接失败，请检查网络"文案 + "重试"按钮
3. BaseTable 和 BasePagination 不渲染
4. 搜索框保持可用（用户可以输入搜索关键词，重试时可带关键词）

When（后续操作）:
4. 学生点击"重试"按钮

Then:
5. 重新进入 Loading 状态
6. 重新发送 API 请求
7. 请求成功 → 进入 Success 或 Empty 状态
8. 请求仍失败 → 保持 Error 状态

---

# Section 8: Permission Design

| 属性 | 值 |
|------|-----|
| 页面权限标识 | student:course:view |
| 页面允许角色 | ["student"] |
| 路由权限检查 | 路由配置 meta.roles = ["student"]，由 permission.ts 导航守卫检查 |
| 按钮级权限 | 无（课程列表页无差异化操作按钮，所有学生可见的操作完全相同） |

课程列表页的"查看详情"按钮对所有学生可见，不存在根据权限隐藏按钮的场景。操作列的 BaseButton 永远渲染。

路由配置（追加到 src/router/routes/student.ts）:
```
{
  path: "courses",
  name: "StudentCourseList",
  component: () => import("@/pages/student/CourseListPage.vue"),
  meta: {
    title: "我的课程",
    icon: "BookOpen",
    sort: 2,
    keepAlive: true,
    roles: ["student"]
  }
}
```

---

# Section 9: Acceptance Criteria

AC-1: Given 学生已登录 When 点击侧边栏"我的课程"菜单 Then 页面路由导航到 /student/courses，PageHeader 标题显示"我的课程"，右侧显示搜索框，表格区域显示课程列表（课程名称、课程编号、授课教师、学期、学分、任务数、操作列），底部显示分页组件

AC-2: Given 课程列表页加载中 When API 响应超过 300ms（Mock 延迟） Then 表格区域展示 LoadingState 组件（跳动圆点动画 + "正在加载课程列表..."文字），而非空白页面，PageHeader 和搜索框正常渲染

AC-3: Given 学生课程列表为空（total=0） When 页面加载完成 Then 展示 EmptyState 组件，文案"暂无课程数据，请联系管理员选课"，不展示表格和分页组件

AC-4: Given 学生在搜索框输入关键词"Java"按 Enter When 搜索完成 Then 表格仅展示匹配的课程（courseName 或 courseCode 包含"Java"），分页显示"共 N 条"，搜索框右侧显示清除按钮

AC-5: Given 学生搜索"不存在课程"关键词 When 搜索完成返回 0 条结果 Then 展示 EmptyState 组件，文案"未找到匹配的课程，请尝试其他关键词"，底部显示"清除搜索"按钮

AC-6: Given 学生点击课程行或"查看详情"按钮 When 触发导航 Then 浏览器 URL 跳转到 /student/courses/{courseId}，加载课程详情页

AC-7: Given 课程列表加载失败（网络错误或服务端错误） When 页面进入 Error 状态 Then 展示 ErrorState 组件（红色图标 + 错误文案 + "重试"按钮），点击"重试"按钮后重新加载

AC-8: Given 学生在第 2 页 When 在搜索框输入关键词并搜索 Then 分页自动重置为第 1 页，展示搜索结果的第一页数据

---

# Section 10: Files to Create / Modify

## 新建文件

```
├── src/pages/student/CourseListPage.vue          # 课程列表页面组件
├── src/api/modules/course.ts                      # 课程相关 API 函数（getStudentCourses、getStudentCourseDetail）
├── src/mock/modules/course.ts                     # 课程接口 Mock 数据
├── src/types/course.ts                            # 课程相关 TypeScript 类型定义（ICourse、ICourseDetail、ITaskBrief）
└── docs/page-analysis/student-course-list.md      # 本文件
```

## 修改文件

```
├── src/router/routes/student.ts                   # 追加 StudentCourseList 路由和 StudentCourseDetail 路由
├── src/mock/index.ts                              # 汇总：引入 course Mock 模块
└── src/api/request.ts                             # 确认：无需修改（通用 Axios 实例已存在）
```

## 依赖的已有组件（本页面不修改它们，但依赖它们的存在）

```
├── src/layouts/AppLayout.vue                      # 页面外层布局
├── src/components/layout/PageHeader.vue           # 页面标题栏
├── src/components/layout/PageContainer.vue        # 页面内容容器
├── src/components/base/BaseInput.vue              # 搜索输入框
├── src/components/base/BaseTable.vue              # 课程列表表格
├── src/components/base/BaseButton.vue             # 操作列按钮
├── src/components/base/BasePagination.vue         # 分页组件
├── src/components/common/LoadingState.vue         # 加载中状态
├── src/components/common/ErrorState.vue           # 错误状态
├── src/components/common/EmptyState.vue           # 空数据状态
├── src/stores/useUserStore.ts                     # 用户信息 Store
└── src/stores/useAppStore.ts                      # 应用全局状态 Store
```

## 类型定义文件内容（src/types/course.ts）

需要定义以下接口:

ICourse（课程列表项）:
- courseId: string
- courseName: string
- courseCode: string
- teacherName: string
- semester: string
- credits: number
- taskCount: number
- description: string

ICourseListResponse（课程列表响应）:
- list: ICourse[]
- page: number
- pageSize: number
- total: number
- totalPages: number

ICourseListParams（课程列表请求参数）:
- page?: number
- pageSize?: number
- keyword?: string

## 注意事项

1. 本页面组件是纯展示 + 导航页面，不涉及表单提交和数据修改。业务逻辑不超过 150 行，无需抽取 Composable。
2. 课程名称列的 hover 效果通过 CSS 实现（cursor: pointer, color: #3B82F6），不依赖额外组件。
3. searchKeyword 状态不从路由 query 中读取，保持页面本地状态。页面刷新后搜索关键词丢失，回归默认全部数据，这是预期行为。
4. BaseTable 的 columns 定义使用常量对象数组，定义在页面组件内的 <script setup> 中。操作列的按钮文案和样式通过 columns 的 slot 或 render 函数配置。
5. Mock 搜索过滤逻辑对 courseName 和 courseCode 做 includes() 匹配（大小写不敏感），不涉及后端全文搜索。
