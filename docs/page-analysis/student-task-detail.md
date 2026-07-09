# Page Analysis: 学生任务详情页 (Student Task Detail)

**文档版本**: v1.0
**创建日期**: 2026-07-03
**分析范围**: 学生端任务详情页面，包含任务信息展示、评价维度、附件下载、提交状态与操作按钮，以及动态按钮逻辑

---

# Section 1: Page Identity

| 属性 | 值 |
|------|-----|
| 页面名称 | 学生任务详情页 |
| 页面文件 | src/pages/student/TaskDetailPage.vue |
| 路由路径 | /student/tasks/:taskId |
| 路由名称 | StudentTaskDetail |
| 所属 Sprint | Sprint 2 |
| 所属 Feature | F-2-03（学生任务详情） |
| 页面角色 | 学生（Student） |
| 页面复杂度 | L2（信息展示页面，含动态按钮逻辑、多状态分支、倒计时） |
| 原型参考 | prototypes/student/task-detail/ 目录下原型文件 |

页面职责：展示单个任务的完整信息，包括任务基本信息、评价维度列表、附件下载列表、当前学生的提交状态。根据 mySubmissionStatus 不同状态动态展示不同的操作按钮组，引导学生完成提交、查看进度、查看成绩等后续操作。

---

# Section 2: Page Layout Structure

学生任务详情页嵌套在 AppLayout 中，使用 PageHeader + PageContainer 标准布局。页面从上到下分为标题区、任务信息卡片、评价维度卡片、附件卡片、提交状态卡片五个区域。

```
+--------------------------------------------------+
|  AppLayout (全屏高度, flex column)                  |
|  +----------------------------------------------+ |
|  |  TopNav 顶部导航栏 (height: 56px)               | |
|  +----------------------------------------------+ |
|  +----------------------------------------------+ |
|  |  PageContainer (flex: 1, overflow-y: auto)     | |
|  |  bg: #F8FAFC, padding: --spacing-lg            | |
|  |                                                | |
|  |  +------------------------------------------+  | |
|  |  |  PageHeader (mb: --spacing-lg)              | |
|  |  |  title: taskName（如"Java 基础编程练习"）    |  | |
|  |  |  breadcrumb: 我的任务 > 任务详情             |  | |
|  |  |  extra: 返回按钮 BaseButton                  |  | |
|  |  +------------------------------------------+  | |
|  |                                                | |
|  |  +------------------------------------------+  | |
|  |  |  Task Info Card (任务信息卡片)               | |
|  |  |  bg: #FFFFFF, border-radius: --radius-lg     | |
|  |  |  padding: --spacing-xl, mb: --spacing-md    |  | |
|  |  |  min-height: auto                           |  | |
|  |  |                                              | |
|  |  |  +-------+-----------------------------------+ |
|  |  |  | 基本信息 |  详细字段（2 列 Grid 布局）     | |
|  |  |  | (section |  - taskName（主标题，h2）      | |
|  |  |  |  title)  |  - courseName + teacherName    | |
|  |  |  |          |  - teacherEmail（可点击 mailto）| |
|  |  |  |          |  - description（多行文本区）    | |
|  |  |  |          |  - deadline + countdown timer  | |
|  |  |  |          |  - totalScore                  | |
|  |  |  |          |  - submissionType               | |
|  |  |  |          |  - submitLimit（如"最多3次"）   | |
|  |  |  +-------+-----------------------------------+ |
|  |  +------------------------------------------+  | |
|  |                                                | |
|  |  +------------------------------------------+  | |
|  |  |  Evaluation Dimensions Card（评价维度卡片）   | |
|  |  |  bg: #FFFFFF, border-radius: --radius-lg     | |
|  |  |  padding: --spacing-xl, mb: --spacing-md    |  | |
|  |  |                                              | |
|  |  |  Section Title: "评分维度"                   | |
|  |  |  维度列表（每行一条）:                         | |
|  |  |  +---+-----------------+--------+--------+  | |
|  |  |  | # | 维度名称         | 权重   | 最高分 |  | |
|  |  |  +---+-----------------+--------+--------+  | |
|  |  |  | 1 | 代码质量         | 30%    | 30 分  |  | |
|  |  |  | 2 | 功能完成度       | 25%    | 25 分  |  | |
|  |  |  | 3 | 文档规范         | 15%    | 15 分  |  | |
|  |  |  | 4 | 创新性           | 20%    | 20 分  |  | |
|  |  |  | 5 | 答辩表现         | 10%    | 10 分  |  | |
|  |  |  +---+-----------------+--------+--------+  | |
|  |  |  权重合计: 100%（底部统计行）                  | |
|  |  +------------------------------------------+  | |
|  |                                                | |
|  |  +------------------------------------------+  | |
|  |  |  Attachments Card（附件卡片，v-if 有附件）    | |
|  |  |  bg: #FFFFFF, border-radius: --radius-lg     | |
|  |  |  padding: --spacing-xl, mb: --spacing-md    |  | |
|  |  |                                              | |
|  |  |  Section Title: "附件资料"（附下载图标）      | |
|  |  |  附件列表（每行一个文件链接）:                 | |
|  |  |  [文件图标] 文件名.pdf  [下载按钮]            | |
|  |  |  [文件图标] 参考资料.zip  [下载按钮]          | |
|  |  +------------------------------------------+  | |
|  |                                                | |
|  |  +------------------------------------------+  | |
|  |  |  My Submission Card（我的提交状态卡片）       | |
|  |  |  bg: #FFFFFF, border-radius: --radius-lg     | |
|  |  |  padding: --spacing-xl                       | |
|  |  |  border-left: 4px solid（颜色随状态变化）     | |
|  |  |                                              | |
|  |  |  +----------------------------------------+  | |
|  |  |  | 提交状态: [TaskStatusBadge/large]       |  | |
|  |  |  | 已提交次数: 1 / 3（"已提交 N/M 次"）    |  | |
|  |  |  | 最新提交时间: 2026-07-03 14:30（如有）  |  | |
|  |  |  +----------------------------------------+  | |
|  |  |                                              | |
|  |  |  [Action Buttons 操作按钮区]（动态渲染）      | |
|  |  |  Gap: --spacing-md, flex wrap               |  | |
|  |  |  （按钮组合由 mySubmissionStatus 决定，      | |
|  |  |    详见 7 种状态的按钮映射表）                 | |
|  |  +------------------------------------------+  | |
|  +----------------------------------------------+ |
+--------------------------------------------------+
```

**区域说明**：

| 区域 | 宽度 | 高度 | 对齐 | 滚动策略 | 说明 |
|------|------|------|------|---------|------|
| TopNav | 100% | 56px | - | 不滚动 | 使用 AppLayout 内置顶部导航栏 |
| PageContainer | 100% | flex: 1 | - | y轴滚动 | 整体内容区，内容过长时滚动 |
| PageHeader | max-content(960px)居中 | auto | 左对齐 | 不滚动 | 标题为任务名称。面包屑导航："我的任务 > 任务详情"。extra 区域放置返回按钮跳回任务列表 |
| Task Info Card | max-content(960px)居中 | auto | 左对齐 | 不滚动 | 白色卡片。内部使用 2 列 Grid 布局（label: value），在 768px 以下改为单列 |
| Evaluation Dimensions Card | max-content(960px)居中 | auto | 左对齐 | 不滚动 | 白色卡片。维度列表使用 el-table（无分页、无边框）或纯 div 布局。底部显示权重合计校验行 |
| Attachments Card | max-content(960px)居中 | auto | 左对齐 | 不滚动 | 白色卡片。仅当 attachments 数组非空时渲染。每个附件一个下载链接行 |
| My Submission Card | max-content(960px)居中 | auto | 左对齐 | 不滚动 | 白色卡片 + 左侧 4px 彩色边框。边框颜色随 mySubmissionStatus 变化，与 TaskStatusBadge 文字色一致。卡片内包含状态标签、提交次数、提交时间、操作按钮 |

**响应式行为**：
- 1920px 分辨率：内容区最大宽度 960px 居中（详情页内容少于列表页，使用更窄的宽度以提升可读性）
- 1366px 分辨率：内容区最大宽度 960px 居中，左右留有充裕空白
- 小于 768px：Task Info Card 的 2 列 Grid 改为单列堆叠。Evaluation Dimensions 表格启用横向滚动
- 小于 480px：操作按钮改为 block（宽度 100%）纵向排列

---

# Section 3: Component Tree & Specification

## 3.1 组件树

```
TaskDetailPage.vue（页面组件，嵌套在 AppLayout 中）
├── PageHeader
│   props: { title: taskDetail.taskName, breadcrumb: [...], extra: <返回按钮> }
│   └── extra slot: BaseButton（type="default", @click: router.back(), innerText: "返回列表"）
├── LoadingState（v-if="loading"）
│   props: { text: "正在加载任务详情..." }
├── ErrorState（v-else-if="error"）
│   props: { message: errorMessage, @retry: fetchTaskDetail }
├── EmptyState（v-else-if="!taskDetail"）
│   props: { description: "任务不存在或已被删除" }
├── <template v-else>
│   ├── Task Info Card
│   │   ├── <h2> {{ taskDetail.taskName }}
│   │   ├── <dl> 描述列表（2 列 Grid）
│   │   │   ├── 所属课程: {{ taskDetail.courseName }}
│   │   │   ├── 授课教师: {{ taskDetail.teacherName }}
│   │   │   ├── 教师邮箱: <a href="mailto:{{ taskDetail.teacherEmail }}">{{ taskDetail.teacherEmail }}</a>
│   │   │   ├── 任务描述: <p> {{ taskDetail.description }}
│   │   │   ├── 截止时间: {{ formatDateTime(taskDetail.deadline) }} + CountdownTimer 组件（剩余时间倒计时）
│   │   │   ├── 总分: {{ taskDetail.totalScore }} 分（等宽字体 JetBrains Mono）
│   │   │   ├── 提交方式: {{ submissionTypeLabel }}（Git 提交 / 压缩包 / 在线编码）
│   │   │   └── 提交限制: 最多 {{ taskDetail.submitLimit || taskDetail.maxSubmitCount || 3 }} 次
│   │   └── CountdownTimer 组件（行内使用，或嵌入在 deadline 行内）
│   ├── Evaluation Dimensions Card
│   │   ├── Section Title: "评分维度"（el-divider 分隔线或 section-title class）
│   │   ├── BaseTable（data: evaluationDimensions, 无分页, border: false）
│   │   │   3 列: 维度名称(name) | 权重(weight%, 进度条 + 百分比) | 最高分(maxScore)
│   │   │   底部 summary 行: 权重合计 = 100%（使用 el-table 的 summary-method 或单独 tr）
│   │   └── 如果维度列表为空: <p>暂无评分维度</p>
│   ├── Attachments Card（v-if="attachments.length > 0"）
│   │   ├── Section Title: "附件资料"（+ Paperclip 图标）
│   │   └── <ul> 附件列表
│   │       └── <li v-for="att in attachments">
│   │           ├── [FileIcon 文件图标]
│   │           ├── <span> {{ att.fileName }}
│   │           ├── <span> ({{ formatFileSize(att.fileSize) }})
│   │           └── BaseButton（type="default", size="small", @click: downloadFile(att.fileUrl), innerText: "下载"）
│   └── My Submission Card
│       ├── <div> 状态信息区
│       │   ├── TaskStatusBadge（status: taskDetail.mySubmissionStatus, size: "large"）
│       │   ├── <span> 已提交 {{ mySubmitCount }} / {{ maxSubmitCount }} 次
│       │   └── <span> 最新提交时间: {{ formatDateTime(lastSubmitTime) }}（仅当 mySubmitCount > 0 时显示）
│       └── <div> 操作按钮区（flex wrap, gap: --spacing-md）
│           └── [动态按钮组合，由 mySubmissionStatus 决定，见下方状态按钮映射表]
```

## 3.2 本页面需要新建的子组件

### 3.2.1 CountdownTimer（倒计时组件）

文件路径: src/components/common/CountdownTimer.vue

组件类型: Common 通用组件（在多个页面复用）

组件职责: 显示距离截止时间的实时倒计时。支持多种时间单位显示，超时后展示特殊样式。

**Props 规格**:

| Prop | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| deadline | string | 是 | - | 截止时间（ISO 8601 格式字符串，如 "2026-07-15T23:59:59.000Z"） |
| showIcon | boolean | 否 | true | 是否展示时钟图标（Clock 图标，来自 Lucide Icons） |
| showLabel | boolean | 否 | true | 是否展示"剩余"前缀文字 |
| size | string | 否 | 'default' | 尺寸：'small' / 'default' / 'large' |

**Emits 规格**:

| Emit | 参数 | 触发时机 | 说明 |
|------|------|---------|------|
| expired | - | 倒计时归零时（deadline 已过） | 通知父组件截止时间已过，父组件可据此调整 UI（如禁用提交按钮） |

**Slots 规格**:

| Slot | Props | 说明 |
|------|-------|------|
| expired | - | 自定义超时后的显示内容。默认显示"已截止"红色文字 |

**内部逻辑计算**:
- 使用 setInterval 每 1 秒更新一次剩余时间
- 时间计算: remaining = new Date(deadline) - Date.now()
- 展示格式（size='default'）:
  - remaining > 7 天: "剩余 X 天"
  - remaining > 1 天: "剩余 X 天 Y 小时"
  - remaining > 1 小时: "剩余 X 小时 Y 分钟"
  - remaining > 1 分钟: "剩余 X 分钟"
  - remaining > 0: "剩余 不足 1 分钟"（紧急状态，红色文字 #EF4444，脉冲动画）
  - remaining <= 0: 触发 expired 事件，展示 "已截止" 或 expired slot 内容
- 超时后停止 setInterval（清除定时器）
- 组件销毁时（onUnmounted）清除 setInterval，防止内存泄漏

**Store 依赖**: 无

**API 依赖**: 无

**样式规范**:
- 默认: color: --color-text-secondary, font-size: --font-size-sm
- 紧急（< 1 分钟及已过期）: color: #EF4444（--color-danger），font-weight: 600，添加 CSS pulse 动画（opacity 在 1 和 0.6 之间循环，1s 周期）
- 正常（> 1 天）: color: --color-success (#10B981)

### 3.2.2 （本页面不需要其他新建组件）

TaskStatusBadge 已在 TaskListPage 中定义，本页面直接复用。

## 3.3 已有组件引用

**引用组件 1: AppLayout（src/components/layout/AppLayout.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| 无显式 props | - | 通过 router-view 嵌套 |

**引用组件 2: PageHeader（src/components/common/PageHeader.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| title | taskDetail.taskName | 动态标题，任务名称 |
| breadcrumb | [ { label: '我的任务', to: '/student/tasks' }, { label: '任务详情' } ] | 面包屑导航 |
| showBack | true | 展示返回按钮（作为 extra 区域的备选，也可在 extra slot 中自定义） |

**引用组件 3: PageContainer（src/components/common/PageContainer.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| maxWidth | "960px" | 详情页使用更窄的 960px 限制以提升可读性 |

**引用组件 4: LoadingState（src/components/common/LoadingState.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| text | "正在加载任务详情..." | Loading 提示文字 |

**引用组件 5: ErrorState（src/components/common/ErrorState.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| message | errorMessage | 动态错误文案 |
| @retry | fetchTaskDetail | 重试回调 |

**引用组件 6: EmptyState（src/components/common/EmptyState.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| description | "任务不存在或已被删除" | 任务不存在时的提示 |

**引用组件 7: TaskStatusBadge（src/components/common/TaskStatusBadge.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| status | taskDetail.mySubmissionStatus | 当前学生提交状态 |
| size | "large" | 详情页使用大号标签 |

**引用组件 8: BaseTable（src/components/base/BaseTable.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| data | evaluationDimensions | 评价维度数据 |
| columns | dimColumns | 维度列定义：[{prop:'index',label:'序号',width:'60px'}, {prop:'name',label:'维度名称',minWidth:'150px'}, {prop:'weight',label:'权重',width:'120px',slot:'weight'}, {prop:'maxScore',label:'最高分',width:'100px',align:'center',className:'font-mono'}] |
| border | false | 无外边框 |
| stripe | false | 不启用斑马纹 |
| showHeader | true | 展示表头 |
| showPagination | false | 不展示分页 |
| size | "small" | 小尺寸行高 |

**引用组件 9: BaseButton（src/components/base/BaseButton.vue）**

多处使用，详见下方操作按钮映射表。

**引用组件 10: BaseModal（src/components/base/BaseModal.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| 在详情页中用于"退回原因"弹窗（status=REJECTED 时点击"查看退回原因"） | - | 展示退回事由内容 |
| title | "退回原因" | 弹窗标题 |
| width | "520px" | 弹窗宽度 |

---

# Section 4: State Design（页面级）

| 状态 | 触发条件 | UI 表现 | 持续时间 | 退出条件 |
|------|---------|--------|---------|---------|
| Loading | 页面挂载后根据路由参数 taskId 调用 API。loading = true | PageHeader 展示标题和面包屑（title 使用路由 params 或占位文字）。卡片区域展示 LoadingState 组件。My Submission Card 不渲染（等数据就绪后才确定按钮组合）。所有卡片区域被 LoadingState 替代 | 直到 API 返回（Mock 约 300ms，超时 15s） | API 返回 success 或 error |
| Empty | API 返回 data 为 null 或 code ≠ 0 且 message 为"任务不存在" | PageHeader 展示基本面包屑。内容区展示 EmptyState: "任务不存在或已被删除"。所有卡片隐藏。提供一个"返回列表"按钮（router.back() 或跳转 /student/tasks） | 持续直到用户离开此页面 | 用户点击返回按钮离开 |
| Error | API 请求异常（网络错误、5001、超时） | PageHeader 展示面包屑。内容区展示 ErrorState 组件。所有卡片隐藏 | 持续直到用户点击重试或离开 | 用户点击"重试"按钮 → API 返回 success |
| Success (NOT_SUBMITTED) | API 返回成功，mySubmissionStatus = 'NOT_SUBMITTED' | 全部 4 个卡片正常展示。My Submission Card 左侧边框颜色: #92400E（与 Badge 文字色一致）。操作按钮区展示: [BaseButton primary "去提交" @click → /student/submit/:taskId]。提交次数显示"已提交 0/{maxSubmitCount} 次"。提交时间不显示 | 持续直到用户触发操作或状态变更 | 用户点击"去提交"跳转；或外部事件触发状态变更 |
| Success (SUBMITTED/AI_EVALUATING) | API 返回成功，mySubmissionStatus = 'SUBMITTED' 或 'AI_EVALUATING' | My Submission Card 左侧边框颜色: 与 Badge 文字色一致。操作按钮区展示: [BaseButton primary "查看提交进度" @click → /student/grades/:submissionId]。提交次数显示"已提交 N/M 次"。提交时间显示 | 持续直到用户触发操作或状态变更 | 用户点击"查看提交进度"跳转；或 AI 评估完成状态自动变更 |
| Success (AI_COMPLETED) | API 返回成功，mySubmissionStatus = 'AI_COMPLETED' | My Submission Card 左侧边框颜色: #065F46。操作按钮区展示: [BaseButton primary "查看 AI 分析" @click → /student/grades/:submissionId] + [BaseButton default "重新提交" @click → /student/submit/:taskId]。提交次数显示。如果 mySubmitCount >= maxSubmitCount，"重新提交"按钮 disabled + tooltip"已达到最大提交次数" | 持续直到用户操作 | 用户点击按钮跳转 |
| Success (COMPLETED) | API 返回成功，mySubmissionStatus = 'COMPLETED' | My Submission Card 左侧边框颜色: #065F46。操作按钮区展示: [BaseButton primary "查看成绩" @click → /student/grades/:submissionId]。只有查看按钮，无重新提交按钮。提交次数显示 | 持续直到用户操作 | 用户点击"查看成绩"跳转 |
| Success (TEACHER_SCORING) | API 返回成功，mySubmissionStatus = 'TEACHER_SCORING' | My Submission Card 左侧边框颜色: #9A3412。操作按钮区展示: [BaseButton default "查看详情" @click → /student/grades/:submissionId]。无重新提交按钮（教师评分中不允许覆盖提交） | 持续直到用户操作 | 用户点击按钮跳转 |
| Success (REJECTED) | API 返回成功，mySubmissionStatus = 'REJECTED' | My Submission Card 左侧边框颜色: #991B1B。操作按钮区展示: [BaseButton primary "重新提交" @click → /student/submit/:taskId] + [BaseButton default "查看退回原因" @click → openRejectReasonModal]。提交次数显示。退回原因弹窗使用 BaseModal 展示 rejectReason 字段内容 | 持续直到用户操作 | 用户提交新版本后状态变更 |
| NoPermission | 路由守卫检测到当前用户角色不是 student | 内容区替换为 NoPermission 组件。PageHeader 仍然展示。所有卡片隐藏 | 持续直到用户切换账号或离开 | 用户点击返回首页跳转 |
| Offline | 网络断开 | PageContainer 顶部展示黄色离线提示条。已加载的数据保持可见。操作按钮可点击但会因 API 失败进入 Error 状态 | 直到网络恢复 | 网络恢复，提示条消失，数据不自动刷新（详情页离线恢复后不自动刷新，由用户手动操作） |

**状态按钮映射表（mySubmissionStatus → Action Buttons）**:

| 状态 | 按钮 1（主按钮） | 按钮 2（次按钮） | 说明 |
|------|-----------------|-----------------|------|
| NOT_SUBMITTED | "去提交" primary → /student/submit/:taskId | - | 学生还未提交 |
| SUBMITTED | "查看提交进度" primary → /student/grades/:submissionId | - | 已提交，等待 AI 分析 |
| AI_EVALUATING | "查看提交进度" primary → /student/grades/:submissionId | - | AI 正在分析中 |
| AI_COMPLETED | "查看 AI 分析" primary → /student/grades/:submissionId | "重新提交" default → /student/submit/:taskId | AI 分析完成，可查看分析或重交 |
| TEACHER_SCORING | "查看详情" default → /student/grades/:submissionId | - | 教师评分中，只可查看不可重新提交 |
| COMPLETED | "查看成绩" primary → /student/grades/:submissionId | - | 流程完成，只可查看最终成绩 |
| REJECTED | "重新提交" primary → /student/submit/:taskId | "查看退回原因" default → openModal | 提交被退回，需重新提交 |

**CountdownTimer 过期行为**:
- 当 deadline 已过且 mySubmissionStatus = 'NOT_SUBMITTED': "去提交"按钮 disabled，tooltip 显示"任务已截止"，CountdownTimer 显示"已截止"（红色）
- 当 deadline 已过且其他状态（已提交过）: "去提交"/"重新提交"按钮仍可点击，但提交 API 会返回错误码 1006（deadline passed），由提交页面处理错误提示

---

# Section 5: Data Flow

## 5.1 页面加载数据流

```
用户从任务列表页点击"查看详情"，路由携带 taskId
→ 路由 /student/tasks/:taskId 匹配到 TaskDetailPage
→ TaskDetailPage.vue 挂载:
  1. 从 route.params.taskId 获取任务 ID
  2. loading = true
  3. 调用 fetchTaskDetail(taskId)
→ fetchTaskDetail(taskId):
  1. 调用 taskApi.getStudentTaskDetail(taskId)
  2. Axios 发送 GET /api/v1/student/tasks/{taskId}
  3. Mock 拦截，300ms 后返回任务详情数据
  4. 成功:
     - taskDetail.value = response.data
     - loading = false
     - 计算 mySubmitCount: 从 mySubmissionStatus 和相关字段提取（如果 status = NOT_SUBMITTED 则为 0）
     - 计算 maxSubmitCount: taskDetail.submitLimit || 3（默认值）
     - 启动 CountdownTimer 计算剩余时间
     - 渲染全部卡片
  5. 失败:
     - error = true
     - errorMessage = 根据错误码映射
     - 如果 4004（任务不存在）→ 展示 EmptyState
```

## 5.2 任务存在但已删除或无权访问数据流

```
Given: 学生 A 尝试查看不属于自己的任务 taskId=999（后端返回 data=null）

When: API 返回 { code: 4004, message: "任务不存在或已被删除", data: null }

Then:
1. loading = false
2. error = false（不属于 Error 状态）
3. 展示 EmptyState: "任务不存在或已被删除" + "返回列表"按钮
4. 用户点击"返回列表" → router.push('/student/tasks')
```

## 5.3 根据状态动态渲染按钮数据流

```
Given: taskDetail 加载完成，mySubmissionStatus = 'AI_COMPLETED'

When: 模板渲染 My Submission Card 操作按钮区

Then:
1. v-if / computed 判断 mySubmissionStatus
2. 匹配到 AI_COMPLETED 的分支
3. 渲染:
   - BaseButton primary: "查看 AI 分析", @click → router.push(`/student/grades/${submissionId}`)
   - BaseButton default: "重新提交", @click → router.push(`/student/submit/${taskId}`)
4. 如果 mySubmitCount >= maxSubmitCount:
   - "重新提交"按钮添加 disabled 属性
   - tooltip: "已达到最大提交次数（N次）"
5. 提交次数区域展示: "已提交 2/3 次"（等宽字体）
```

## 5.4 附件下载数据流

```
Given: attachments = [{ fileId: 1, fileName: "需求文档.pdf", fileSize: 2048000, fileUrl: "/files/1/download" }]

When: 用户点击"下载"按钮

Then:
1. 调用 downloadFile(att.fileUrl)
2. window.open(fileUrl, '_blank') 或创建 <a> 标签 download 属性触发下载
3. 文件在浏览器新标签页打开（PDF 等可预览类型）或直接下载（ZIP 等不可预览类型）
4. 如果下载失败（CDN 不可用等）: ElMessage.error("文件下载失败，请稍后重试")
```

## 5.5 退回原因弹窗数据流

```
Given: mySubmissionStatus = 'REJECTED', taskDetail.rejectReason 或 submissionList 中有退回原因

When: 用户点击"查看退回原因"按钮

Then:
1. rejectReasonModalVisible = true
2. BaseModal 展示:
   - title: "退回原因"
   - body: taskDetail.rejectReason（纯文本或多行文本，保留换行）
   - footer: BaseButton "关闭" @click → rejectReasonModalVisible = false
3. 用户阅读后退原因后点击"关闭"关闭弹窗
```

---

# Section 6: API & Mock Specification

## 接口 1: GET /api/v1/student/tasks/{taskId}

用途: 获取单个任务的完整详情，包括评价维度、附件、我的提交状态

请求方式: GET

路径参数:

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| taskId | number | 是 | 任务 ID，从路由 params 获取 | 1 |

响应数据结构:
```
{
  code: number,
  message: string,
  data: {
    taskId: number,
    taskName: string,
    courseName: string,
    teacherName: string,
    teacherEmail: string,     // 教师邮箱，用于联系教师
    description: string,      // 任务描述（HTML 或纯文本，支持换行）
    deadline: string,         // ISO 8601
    totalScore: number,
    submissionType: string,   // GIT / ZIP / ONLINE
    submitLimit: number,      // 最大可提交次数（默认 3）
    status: string,           // 任务整体状态
    mySubmissionStatus: string,  // 当前学生的提交状态
    mySubmitCount: number,    // 已提交次数
    maxSubmitCount: number,   // 最大可提交次数（同 submitLimit）
    lastSubmitTime: string | null,  // 最新提交时间，未提交时为 null
    submissionId: number | null,    // 当前最新提交ID，未提交时为 null
    rejectReason: string | null,    // 退回原因（status=REJECTED 时有值）
    evaluationDimensions: Array<{
      dimensionId: number,
      name: string,           // 维度名称
      weight: number,         // 权重百分比（如 30 表示 30%）
      maxScore: number,       // 该维度最高分
      description: string     // 维度描述
    }>,
    attachments: Array<{
      fileId: number,
      fileName: string,
      fileSize: number,       // 字节数
      fileUrl: string,        // 下载 URL
      fileType: string        // 文件类型扩展名（如 pdf, zip, docx）
    }>,
    createdAt: string,
    updatedAt: string
  } | null
}
```

**Mock 数据示例 1（任务详细数据，status = NOT_SUBMITTED）**:

```
{
  "code": 0,
  "message": "获取任务详情成功",
  "success": true,
  "timestamp": "2026-07-03T10:30:00.000Z",
  "traceId": "trace-task-detail-001",
  "data": {
    "taskId": 1,
    "taskName": "Java 基础编程练习",
    "courseName": "Java 程序设计",
    "teacherName": "李老师",
    "teacherEmail": "lilaoshi@example.com",
    "description": "请完成以下 Java 基础编程练习：\n\n1. 实现一个学生成绩管理系统，包含增删改查功能\n2. 使用面向对象编程思想，至少包含 3 个类\n3. 添加单元测试，覆盖率不低于 60%\n4. 编写完整的项目文档\n\n要求：\n- 使用 JDK 17+\n- 构建工具使用 Maven\n- 代码风格遵循阿里巴巴 Java 开发手册",
    "deadline": "2026-07-15T23:59:59.000Z",
    "totalScore": 100,
    "submissionType": "GIT",
    "submitLimit": 3,
    "status": "PUBLISHED",
    "mySubmissionStatus": "NOT_SUBMITTED",
    "mySubmitCount": 0,
    "maxSubmitCount": 3,
    "lastSubmitTime": null,
    "submissionId": null,
    "rejectReason": null,
    "evaluationDimensions": [
      {
        "dimensionId": 1,
        "name": "代码质量",
        "weight": 30,
        "maxScore": 30,
        "description": "评估代码的规范性、可读性、注释质量和编码风格"
      },
      {
        "dimensionId": 2,
        "name": "功能完成度",
        "weight": 25,
        "maxScore": 25,
        "description": "评估需求功能的完成情况和正确性"
      },
      {
        "dimensionId": 3,
        "name": "文档规范",
        "weight": 15,
        "maxScore": 15,
        "description": "评估项目文档的完整性和规范性"
      },
      {
        "dimensionId": 4,
        "name": "创新性",
        "weight": 20,
        "maxScore": 20,
        "description": "评估解决方案的创新程度和技术亮点"
      },
      {
        "dimensionId": 5,
        "name": "答辩表现",
        "weight": 10,
        "maxScore": 10,
        "description": "评估现场答辩的表达能力和技术理解深度"
      }
    ],
    "attachments": [
      {
        "fileId": 1,
        "fileName": "Java 编程练习-需求文档.pdf",
        "fileSize": 2048000,
        "fileUrl": "https://example.com/files/java-exercise-requirements.pdf",
        "fileType": "pdf"
      },
      {
        "fileId": 2,
        "fileName": "参考资料-代码模板.zip",
        "fileSize": 5120000,
        "fileUrl": "https://example.com/files/template-code.zip",
        "fileType": "zip"
      },
      {
        "fileId": 3,
        "fileName": "评分标准说明.docx",
        "fileSize": 1024000,
        "fileUrl": "https://example.com/files/scoring-rubric.docx",
        "fileType": "docx"
      }
    ],
    "createdAt": "2026-06-01T08:00:00.000Z",
    "updatedAt": "2026-06-15T10:00:00.000Z"
  }
}
```

**Mock 数据示例 2（任务已提交，status = AI_COMPLETED）**:

```
{
  "code": 0,
  "message": "获取任务详情成功",
  "success": true,
  "timestamp": "2026-07-03T10:31:00.000Z",
  "traceId": "trace-task-detail-002",
  "data": {
    "taskId": 2,
    "taskName": "Spring Boot 项目实战",
    "courseName": "Web 后端开发",
    "teacherName": "王老师",
    "teacherEmail": "wanglaoshi@example.com",
    "description": "请使用 Spring Boot 3 开发一个完整的 RESTful API 服务。\n\n功能要求：\n1. 用户注册与登录（JWT 认证）\n2. CRUD 操作\n3. 分页查询\n4. 文件上传下载\n5. API 文档（Swagger/OpenAPI）\n\n技术要求：\n- Spring Boot 3.x + JDK 21\n- MyBatis Plus\n- MySQL + Redis\n- Docker 部署",
    "deadline": "2026-07-20T23:59:59.000Z",
    "totalScore": 150,
    "submissionType": "GIT",
    "submitLimit": 3,
    "status": "PUBLISHED",
    "mySubmissionStatus": "AI_COMPLETED",
    "mySubmitCount": 1,
    "maxSubmitCount": 3,
    "lastSubmitTime": "2026-07-03T14:30:00.000Z",
    "submissionId": 101,
    "rejectReason": null,
    "evaluationDimensions": [
      {
        "dimensionId": 6,
        "name": "API 设计规范",
        "weight": 30,
        "maxScore": 45,
        "description": "RESTful API 设计是否符合规范，命名、状态码使用是否合理"
      },
      {
        "dimensionId": 7,
        "name": "代码架构",
        "weight": 25,
        "maxScore": 37.5,
        "description": "分层架构是否清晰，依赖注入使用是否合理"
      },
      {
        "dimensionId": 8,
        "name": "安全性",
        "weight": 20,
        "maxScore": 30,
        "description": "认证授权、参数校验、SQL 注入防护等安全措施"
      },
      {
        "dimensionId": 9,
        "name": "测试覆盖",
        "weight": 15,
        "maxScore": 22.5,
        "description": "单元测试和集成测试的覆盖率和质量"
      },
      {
        "dimensionId": 10,
        "name": "文档完整性",
        "weight": 10,
        "maxScore": 15,
        "description": "README、API 文档、部署文档的完整性"
      }
    ],
    "attachments": [
      {
        "fileId": 4,
        "fileName": "Spring Boot 项目需求说明书.pdf",
        "fileSize": 3500000,
        "fileUrl": "https://example.com/files/springboot-requirements.pdf",
        "fileType": "pdf"
      }
    ],
    "createdAt": "2026-06-05T08:00:00.000Z",
    "updatedAt": "2026-07-03T14:30:00.000Z"
  }
}
```

**Mock 数据示例 3（任务被退回，status = REJECTED）**:

```
{
  "code": 0,
  "message": "获取任务详情成功",
  "success": true,
  "timestamp": "2026-07-03T10:32:00.000Z",
  "traceId": "trace-task-detail-003",
  "data": {
    "taskId": 6,
    "taskName": "操作系统原理报告",
    "courseName": "操作系统",
    "teacherName": "赵老师",
    "teacherEmail": "zhaolaoshi@example.com",
    "description": "完成一篇关于现代操作系统内存管理机制的实验报告。字数不少于 3000 字。",
    "deadline": "2026-07-14T23:59:59.000Z",
    "totalScore": 90,
    "submissionType": "ZIP",
    "submitLimit": 2,
    "status": "PUBLISHED",
    "mySubmissionStatus": "REJECTED",
    "mySubmitCount": 1,
    "maxSubmitCount": 2,
    "lastSubmitTime": "2026-07-01T10:00:00.000Z",
    "submissionId": 106,
    "rejectReason": "报告内容过于简略，字数不达标（实际 1800 字，要求 3000 字以上）。缺少内存分配算法的对比实验数据。请补充完整的实验过程和分析后重新提交。",
    "evaluationDimensions": [
      {
        "dimensionId": 11,
        "name": "内容完整性",
        "weight": 40,
        "maxScore": 36,
        "description": "报告内容是否覆盖所有要求的主题"
      },
      {
        "dimensionId": 12,
        "name": "分析与深度",
        "weight": 30,
        "maxScore": 27,
        "description": "分析的深度和技术理解程度"
      },
      {
        "dimensionId": 13,
        "name": "格式规范",
        "weight": 20,
        "maxScore": 18,
        "description": "排版、引用、图表格式的规范性"
      },
      {
        "dimensionId": 14,
        "name": "创新见解",
        "weight": 10,
        "maxScore": 9,
        "description": "是否有独立的创新观点或独特分析视角"
      }
    ],
    "attachments": [],
    "createdAt": "2026-06-25T08:00:00.000Z",
    "updatedAt": "2026-07-01T10:00:00.000Z"
  }
}
```

**Mock 数据示例 4（任务无附件）**:

与示例 1 相同，但 attachments 字段为 `"attachments": []`。Attachments Card 不渲染。

**Mock 错误场景**:

| 错误场景 | 错误码 | Mock 响应 | UI 表现 |
|---------|--------|----------|--------|
| 任务不存在 | 4004 | { "code": 4004, "message": "任务不存在或已被删除", "data": null } | EmptyState |
| 无权访问 | 4003 | { "code": 4003, "message": "无权访问此任务", "data": null } | NoPermission 或 EmptyState |
| 服务端错误 | 5001 | { "code": 5001, "message": "服务器繁忙，请稍后重试", "data": null } | ErrorState + 重试 |
| 网络错误 | Network Error | 无响应体 | ErrorState + 重试 |

**Mock 延迟**: 300ms

**Mock 实现位置**: src/mock/studentTasks.ts（任务列表和详情共用同一个 Mock 文件）

**Mock 逻辑**: 根据路径参数 taskId 从预定义的 Mock 数据集中查找。taskId 1 → 示例 1（NOT_SUBMITTED），taskId 2 → 示例 2（AI_COMPLETED），taskId 6 → 示例 3（REJECTED），其他 → 404 错误。

---

# Section 7: Interaction Flows

## 交互 1: 从任务列表跳转至详情页

Given: 学生在任务列表页 /student/tasks，展示 20 条任务

When:
1. 学生点击 taskId=1 行的"查看详情"按钮
2. router.push('/student/tasks/1')

Then:
1. 浏览器地址栏变为 /student/tasks/1
2. TaskDetailPage 组件挂载
3. route.params.taskId = '1'
4. LoadingState 展示
5. 300ms 后 API 返回 taskId=1 详情数据
6. LoadingState 消失
7. PageHeader 标题变为"Java 基础编程练习"
8. 面包屑: 我的任务 > 任务详情
9. Task Info Card 展示: 课程名、教师、邮箱、描述、截止时间（倒计时 "剩余 12 天"）、总分 100、提交方式 "Git 提交"、提交限制 "最多 3 次"
10. Evaluation Dimensions Card 展示 5 个维度表格，底部显示"权重合计: 100%"
11. Attachments Card 展示 3 个文件下载链接
12. My Submission Card 左侧边框 #92400E，状态 Badge "未提交"，提交次数 "已提交 0/3 次"
13. 操作按钮: 一个蓝色 primary 按钮"去提交"

## 交互 2: NOT_SUBMITTED 状态点击"去提交"

Given: 任务详情页 mySubmissionStatus = NOT_SUBMITTED，deadline 未过期

When: 点击"去提交" primary 按钮

Then:
1. router.push('/student/submit/1')
2. 跳转到提交页面

## 交互 3: AI_COMPLETED 状态查看 AI 分析

Given: 任务详情页 mySubmissionStatus = AI_COMPLETED，submissionId = 101

When:
1. 点击"查看 AI 分析" primary 按钮
2. router.push('/student/grades/101')

Then:
1. 导航到学生成绩详情页（该页面展示 AI 评分结果和分析报告）
2. 学生可查看 AI 给出的各维度评分和详细分析意见

## 交互 4: REJECTED 状态查看退回原因

Given: 任务详情页 mySubmissionStatus = REJECTED，rejectReason = "报告内容过于简略..."

When:
1. 点击"查看退回原因" default 按钮
2. rejectReasonModalVisible = true

Then:
1. BaseModal 弹出，宽度 520px
2. 标题: "退回原因"
3. 内容区展示 rejectReason 纯文本（保留换行）
4. 底部一个"关闭"按钮
5. 用户阅读完毕后点击"关闭" → 弹窗关闭

## 交互 5: 截止时间倒计时过期

Given: 任务详情页，deadline = "2026-07-13T23:59:59.000Z"，当前时间已超过截止时间

When:
1. CountdownTimer 组件检测到 remaining <= 0
2. 触发 @expired 事件

Then:
1. CountdownTimer 显示"已截止"红色文字
2. PageHeader 中或 Task Info Card 中倒计时区域显示红色"已截止"
3. 如果 mySubmissionStatus = NOT_SUBMITTED:
   - "去提交"按钮变为 disabled 状态
   - 鼠标 hover 按钮时 tooltip: "任务已截止，不能再提交"
4. 如果 mySubmissionStatus 不是 NOT_SUBMITTED（已提交过）:
   - "重新提交" / "去提交"按钮保持可点击（学生可能已在截止前提交过，需要重交）
   - 但如果实际提交时 API 返回 1006（deadline passed），则由提交页面处理错误

## 交互 6: 下载附件

Given: 任务详情页有 3 个附件

When:
1. 用户点击第一个附件"Java 编程练习-需求文档.pdf"的"下载"按钮

Then:
1. 触发文件下载（window.open 或 <a> download 属性）
2. 文件开始下载到用户本地
3. 如果网络问题导致下载失败: ElMessage.error("文件下载失败，请稍后重试")

## 交互 7: 已达最大提交次数

Given: mySubmissionStatus = AI_COMPLETED, mySubmitCount = 3, maxSubmitCount = 3

When:
1. 用户查看 My Submission Card

Then:
1. "已提交 3/3 次"显示
2. "重新提交"按钮 disabled，颜色变灰
3. 鼠标 hover "重新提交"按钮时 tooltip: "已达到最大提交次数（3次），如需继续提交请联系教师"

## 交互 8: 页面加载失败

Given: 网络异常

When:
1. 页面调用 GET /api/v1/student/tasks/1
2. API 请求超时

Then:
1. ErrorState 展示
2. 错误信息: "请求超时，请稍后重试"
3. "重试"按钮可用
4. 用户点击"重试" → 重新调用 fetchTaskDetail(1)

---

# Section 8: Permission Design

| 属性 | 值 |
|------|-----|
| 页面权限标识 | student:task:detail |
| 页面允许角色 | ['student'] |
| 路由权限检查 | meta.roles = ['student'] |
| 路由 meta | { title: '任务详情', icon: 'FileText', roles: ['student'], keepAlive: false } |
| 按钮级权限 | 无（所有按钮对所有学生可见，disabled 状态由业务逻辑决定而非权限） |

权限控制详情:

1. **页面级权限**: 路由守卫 permission.ts 检查 role 是否为 student。非学生角色访问重定向到对应首页。

2. **数据级权限**: API 层面根据 JWT token 中的 userId 验证学生是否有权访问该任务：
   - 任务属于该学生 → 返回数据
   - 任务不属于该学生 → 返回 4003（无权访问）→ NoPermission 或 EmptyState
   - 任务 ID 无效 → 返回 4004（任务不存在）→ EmptyState

3. **按钮 disabled 逻辑（非权限相关）**:
   - "去提交"/"重新提交"按钮: 当 mySubmitCount >= maxSubmitCount 时 disabled
   - "去提交"按钮: 当 deadline 已过期且从未提交过时 disabled
   - 其他按钮: 所有状态下均可点击

4. **keepAlive 行为**: keepAlive = false，页面不缓存。每次从列表进入详情页时重新请求最新数据，确保提交状态是最新的。从提交页返回详情页时也重新加载（通过 onActivated 或 beforeRouteEnter）。

---

# Section 9: Acceptance Criteria

AC-1: Given 学生从任务列表点击 taskId=1 的"查看详情" When 详情页加载完成 Then 页面展示完整的任务信息：面包屑("我的任务 > 任务详情")、任务名称标题、基本信息（课程、教师、邮箱、描述、截止时间含倒计时、总分、提交方式、提交限制）、5 个评价维度表格（含权重百分比和最高分，权重合计 100%）、3 个附件下载链接、"我的提交状态"卡片（含 TaskStatusBadge、提交次数、操作按钮）

AC-2: Given 任务状态为 NOT_SUBMITTED 且 deadline 未过期 When 学生查看详情页 Then My Submission Card 展示"未提交"Badge、"已提交 0/3 次"、一个蓝色 primary 按钮"去提交"。点击"去提交"后跳转到 /student/submit/1

AC-3: Given 任务状态为 AI_COMPLETED 且已提交 1 次 When 学生查看详情页 Then 展示 2 个按钮："查看 AI 分析"(primary) 和"重新提交"(default)。提交次数"已提交 1/3 次"。如果已提交次数达到上限(3/3)，"重新提交"按钮 disabled + tooltip 提示

AC-4: Given 任务状态为 REJECTED When 学生查看详情页 Then 展示"已退回"Badge(红色)、2 个按钮"重新提交"(primary) 和"查看退回原因"(default)。点击"查看退回原因"弹出 BaseModal 展示退回事由内容

AC-5: Given 任务状态为 COMPLETED When 学生查看详情页 Then 展示"已完成"Badge(绿色)、只有"查看成绩"按钮(primary)，无重新提交按钮

AC-6: Given 任务截止时间已过期且学生还从未提交 When 学生查看详情页 Then CountdownTimer 显示"已截止"红色文字，"去提交"按钮 disabled 且 tooltip 显示"任务已截止，不能再提交"

AC-7: Given 任务有 3 个附件 When 学生点击某个附件的"下载"按钮 Then 触发浏览器文件下载。如果下载失败，提示"文件下载失败，请稍后重试"

AC-8: Given 详情页加载中 When API 请求未返回 Then 展示 LoadingState("正在加载任务详情...")，PageHeader 面包屑仍可见

AC-9: Given 学生尝试查看不存在的任务 taskId=999 When API 返回 4004 Then 展示 EmptyState("任务不存在或已被删除")和"返回列表"按钮

---

# Section 10: Files to Create / Modify

## 新建文件

```
├── src/pages/student/TaskDetailPage.vue          # 学生任务详情页面组件
├── src/components/common/CountdownTimer.vue      # 倒计时通用组件
├── src/api/studentTask.ts                        # 追加 getStudentTaskDetail 方法（如果与 TaskListPage 共用同文件）
├── src/mock/studentTasks.ts                      # 追加任务详情 Mock 数据（如果与 TaskListPage 共用同文件）
└── docs/page-analysis/student-task-detail.md     # 本文件
```

## 修改文件

```
├── src/router/routes/student.ts                  # 追加 StudentTaskDetail 路由（/student/tasks/:taskId → TaskDetailPage.vue, meta: { keepAlive: false }）
├── src/api/studentTask.ts                        # 追加 getStudentTaskDetail API 函数
├── src/mock/studentTasks.ts                      # 追加 /api/v1/student/tasks/:taskId 的 Mock 拦截
└── src/mock/index.ts                             # 确认 Mock 模块已注册
```

## 依赖的已有组件（本页面不修改它们）

```
├── src/components/layout/AppLayout.vue
├── src/components/common/PageHeader.vue
├── src/components/common/PageContainer.vue
├── src/components/common/LoadingState.vue
├── src/components/common/ErrorState.vue
├── src/components/common/EmptyState.vue
├── src/components/common/NoPermission.vue
├── src/components/common/TaskStatusBadge.vue     # 新建组件，本页面引用
├── src/components/base/BaseTable.vue
├── src/components/base/BaseButton.vue
├── src/components/base/BaseModal.vue
└── src/stores/useUserStore.ts
```

## 开发顺序

1. 完成 src/types/task.ts（类型定义，优先）—— TaskDetail, EvaluationDimension, Attachment 等接口
2. 完成 src/components/common/CountdownTimer.vue —— 独立组件，可并行
3. 完成 src/mock/studentTasks.ts（追加详情 Mock）—— 并行
4. 完成 src/api/studentTask.ts（追加详情 API）—— 依赖类型
5. 完成 src/pages/student/TaskDetailPage.vue —— 依赖以上全部
6. 完成路由配置

注意事项:
1. TaskDetailPage 的 keepAlive 设置为 false，确保每次进入都重新获取最新数据（提交状态可能随时变化）。
2. CountdownTimer 在 TaskDetailPage 中使用，但在 onUnmounted 中必须清除 setInterval，防止内存泄漏。
3. 评价维度权重合计必须等于 100%。如果 API 返回的维度权重合计不为 100%，在底部统计行展示红色警告提示："权重合计异常（XX%），请联系教师确认"。
4. 附件下载使用 window.open 方式。如果后端要求鉴权，需要在下载 URL 中拼接 token 参数，或通过 Axios 以 blob 方式下载。
5. 退回原因弹窗（BaseModal）中的文本保留原始换行符（使用 CSS white-space: pre-wrap）。
