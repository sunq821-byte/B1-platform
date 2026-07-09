# Page Analysis: 学生成绩详情 (Student Grades)

**文档版本**: v1.0
**创建日期**: 2026-07-03
**分析范围**: 学生提交物成绩详情页及 AI 分析与教师评分全流程

---

# Section 1: Page Identity

| 属性 | 值 |
|------|-----|
| 页面名称 | 学生成绩详情 |
| 页面文件 | src/pages/student/GradesPage.vue |
| 路由路径 | /student/grades/:submissionId |
| 路由名称 | StudentGrades |
| 所属 Sprint | Sprint 2 |
| 所属 Feature | F-2-03（参见《Sprint 2 Spec》Section 3） |
| 页面角色 | 学生（student） |
| 页面复杂度 | L3（AI 轮询状态机、多区域条件渲染、教师评分动态展示、图表对比） |
| 原型参考 | 参见《UI Design System v1.0》学生成绩详情原型 |

页面职责：展示单个提交物的完整评分详情，包括 AI 分析结果（含轮询等待）、维度得分对比柱状图、教师评价、最终成绩。本页面根据提交物的评审状态（AI_EVALUATING / AI_COMPLETED / TEACHER_SCORING / COMPLETED / REJECTED）动态渲染不同区域。AI 分析中时启动 3 秒轮询，最长 2 分钟后超时提示。

---

# Section 2: Page Layout Structure

页面嵌套在 AppLayout 中，使用顶部导航栏。页面内容从上到下分为：PageHeader 返回导航区、状态横幅区（条件渲染）、AI 分析结果面板（条件渲染）、得分对比图表区、教师评价区（条件渲染）、最终成绩区。

```
+------------------------------------------------------------------+
|  AppLayout                                                        |
|  +---------------------------------------------------------------+|
|  |  Navbar (height: 56px, bg: #FFFFFF)                           ||
|  +---------------------------------------------------------------+|
|  |  PageContainer (max-width: 960px, margin: 0 auto,             ||
|  |                 padding: --spacing-xl)                         ||
|  |                                                                ||
|  |  +-----------------------------------------------------------+  |
|  |  |  PageHeader                                                |  |
|  |  |  title: "成绩详情 - {taskName}"                              |  |
|  |  |  breadcrumb: 首页 > 仪表盘 > 成绩详情                         |  |
|  |  |  description: 提交物信息摘要（课程名 + 提交时间）              |  |
|  |  +-----------------------------------------------------------+  |
|  |  |  spacing: --spacing-lg (24px)                              |  |
|  |  +-----------------------------------------------------------+  |
|  |  |  [条件区域 A] Status Banner（根据 evaluation.status 渲染）    |  |
|  |  |                                                             |  |
|  |  |  Case AI_EVALUATING:                                        |  |
|  |  |  +------------------------------------------------------+  |  |
|  |  |  |  AI 分析中横幅                                          |  |  |
|  |  |  |  bg: #EDE9FE, border-left: 4px solid #8B5CF6          |  |  |
|  |  |  |  icon: Loader (旋转动画, Lucide Icons)                  |  |  |
|  |  |  |  text: "AI 正在分析你的提交..."                          |  |  |
|  |  |  |  sub-text: "正在分析: {currentDimension}"               |  |  |
|  |  |  |  progress: ElProgressBar (percentage=pollingProgress)   |  |  |
|  |  |  |  elapsed: "已用时 {elapsed} 秒"                         |  |  |
|  |  |  +------------------------------------------------------+  |  |
|  |  |                                                             |  |
|  |  |  Case AI_COMPLETED:                                         |  |
|  |  |  +------------------------------------------------------+  |  |
|  |  |  |  AI 分析完成提示条                                       |  |  |
|  |  |  |  bg: #D1FAE5, border-left: 4px solid #10B981           |  |  |
|  |  |  |  icon: CheckCircle (Lucide Icons)                       |  |  |
|  |  |  |  text: "AI 分析已完成，等待教师评分"                       |  |  |
|  |  |  +------------------------------------------------------+  |  |
|  |  |                                                             |  |
|  |  |  Case TEACHER_SCORING:                                      |  |
|  |  |  +------------------------------------------------------+  |  |
|  |  |  |  评分中提示条                                            |  |  |
|  |  |  |  bg: #FFF7ED, border-left: 4px solid #F59E0B           |  |  |
|  |  |  |  icon: Clock (Lucide Icons)                             |  |  |
|  |  |  |  text: "教师正在评分中，请耐心等待..."                       |  |  |
|  |  |  +------------------------------------------------------+  |  |
|  |  |                                                             |  |
|  |  |  Case COMPLETED / REJECTED:                                 |  |
|  |  |  不展示状态横幅（直接展示完整评价内容）                      |  |
|  |  +-----------------------------------------------------------+  |
|  |  |  spacing: --spacing-lg (24px)                              |  |
|  |  +-----------------------------------------------------------+  |
|  |  |  [条件区域 B] AIScorePanel（AI 分析结果面板）                |  |
|  |  |  AI_EVALUATING 状态: 展示轮询态                              |  |
|  |  |  AI_COMPLETED / TEACHER_SCORING / COMPLETED / REJECTED:     |  |
|  |  |    展示 AI 维度评分详情                                     |  |
|  |  |                                                             |  |
|  |  |  +------------------------------------------------------+  |  |
|  |  |  |  AIScorePanel                                          |  |  |
|  |  |  |  Panel Header: "AI 分析结果" + AI 模型名称标签            |  |  |
|  |  |  |                                                         |  |  |
|  |  |  |  [轮询态] isPolling=true:                                |  |  |
|  |  |  |  - 进度条: ElProgressBar (percentage)                    |  |  |
|  |  |  |  - 当前维度: currentDimension (如 "正在分析代码质量...")  |  |  |
|  |  |  |  - 维度列表: 6 个维度每一项展示 spinner 或 check 图标     |  |  |
|  |  |  |  - 超时/错误: pollingError 时展示错误信息 + 重试按钮      |  |  |
|  |  |  |                                                         |  |  |
|  |  |  |  [完成态] aiResult 有数据:                               |  |  |
|  |  |  |  - 6 个维度卡片 Grid（3 列 x 2 行）                      |  |  |
|  |  |  |    每个维度卡片:                                         |  |  |
|  |  |  |    [维度名称] [AI 得分/100]  [进度条色条]                |  |  |
|  |  |  |    [AI 评语摘要（2-3 行截断，展开详情按钮）]              |  |  |
|  |  |  |  - 总体评价: 底部 AI 综合评价文字（全宽展示，不限行数）    |  |  |
|  |  |  +------------------------------------------------------+  |  |
|  |  +-----------------------------------------------------------+  |
|  |  |  spacing: --spacing-lg (24px)                              |  |
|  |  +-----------------------------------------------------------+  |
|  |  |  [条件区域 C] ScoreComparisonChart（维度得分对比）           |  |
|  |  |  AI_COMPLETED 及以上状态展示                                |  |
|  |  |                                                             |  |
|  |  |  +------------------------------------------------------+  |  |
|  |  |  |  BarChart（双系列分组柱状图）                             |  |  |
|  |  |  |  title: "维度得分对比"                                   |  |  |
|  |  |  |  height: 300px                                          |  |  |
|  |  |  |  xAxisData: 6 个维度名称                                 |  |  |
|  |  |  |  seriesData: [                                         |  |  |
|  |  |  |    { name: "AI 评分", data: [], color: "#8B5CF6" },    |  |  |
|  |  |  |    { name: "教师评分", data: [], color: "#3B82F6" }    |  |  |
|  |  |  |  ]                                                      |  |  |
|  |  |  |  注意: TEACHER_SCORING 状态时教师评分系列可能为 null，     |  |  |
|  |  |  |        该状态不展示此区域                                 |  |  |
|  |  |  +------------------------------------------------------+  |  |
|  |  +-----------------------------------------------------------+  |
|  |  |  spacing: --spacing-lg (24px)                              |  |
|  |  +-----------------------------------------------------------+  |
|  |  |  [条件区域 D] Teacher Evaluation Section                    |  |
|  |  |  COMPLETED / REJECTED 状态展示                              |  |
|  |  |                                                             |  |
|  |  |  +------------------------------------------------------+  |  |
|  |  |  |  教师评价卡片                                            |  |  |
|  |  |  |  Card bg: #FFFFFF, border-radius: --radius-md           |  |  |
|  |  |  |  padding: --spacing-lg                                  |  |  |
|  |  |  |                                                         |  |  |
|  |  |  |  Section Header: "教师评价"                               |  |  |
|  |  |  |  meta: "评分教师: {teacherName} | 评分时间: {time}"      |  |  |
|  |  |  |                                                         |  |  |
|  |  |  |  evaluation.evaluation 字段为 null 的情况:               |  |  |
|  |  |  |    EmptyState("教师暂未填写文字评价")                     |  |  |
|  |  |  |                                                         |  |  |
|  |  |  |  evaluation.evaluation 字段有内容:                       |  |  |
|  |  |  |    {evaluation.evaluation} 文字内容（保持段落格式，       |  |  |
|  |  |  |    white-space: pre-wrap）                               |  |  |
|  |  |  +------------------------------------------------------+  |  |
|  |  +-----------------------------------------------------------+  |
|  |  |  spacing: --spacing-lg (24px)                              |  |
|  |  +-----------------------------------------------------------+  |
|  |  |  [条件区域 E] Final Score Section                           |  |
|  |  |  COMPLETED / REJECTED 状态展示                              |  |
|  |  |                                                             |  |
|  |  |  +------------------------------------------------------+  |  |
|  |  |  |  最终成绩展示卡片                                        |  |  |
|  |  |  |  Card bg: #FFFFFF, border-left: 4px solid              |  |  |
|  |  |  |  (COMPLETED→#10B981, REJECTED→#EF4444)                  |  |  |
|  |  |  |                                                         |  |  |
|  |  |  |  flex 布局: 左侧文字信息 + 右侧大数字成绩                  |  |  |
|  |  |  |  Left:                                                  |  |  |
|  |  |  |  - 评审状态标签: TaskStatusBadge (COMPLETED/REJECTED)    |  |  |
|  |  |  |  - AI 综合评分: {aiScore} 分                             |  |  |
|  |  |  |  - 教师评分: {teacherScore} 分 (或 "未评分")             |  |  |
|  |  |  |  Right:                                                 |  |  |
|  |  |  |  - 最终成绩: {finalScore} 分                             |  |  |
|  |  |  |    font: JetBrains Mono, --font-size-4xl,              |  |  |
|  |  |  |    color: --color-primary                               |  |  |
|  |  |  +------------------------------------------------------+  |  |
|  |  +-----------------------------------------------------------+  |
|  +------------------------------------------------------------------+
```

**区域说明**：

| 区域 | 宽度 | 高度 | 对齐 | 条件渲染 | 说明 |
|------|------|------|------|---------|------|
| 导航栏 | 100vw | 56px | - | 始终 | AppLayout 内置 |
| PageHeader | 100% 父容器 | auto | 左对齐 | 始终 | 标题动态拼接 taskName。面包屑路径: 首页 > 仪表盘(/student/dashboard) > 成绩详情 |
| Status Banner | 100% 父容器 | auto | - | 非 COMPLETED/REJECTED 时展示 | 4 种不同颜色和文案的状态横幅。AI_EVALUATING 额外展示进度条和已用时间 |
| AIScorePanel | 100% 父容器 | auto | - | 始终（AI_EVALUATING 时展示轮询态，其他展示结果态）。若 evaluation 接口返回后 status 为 NOT_SUBMITTED 或 SUBMITTED 则此区域不展示 | 轮询态：进度条 + 维度分析动画。结果态：6 维度卡片 Grid + 综合评语 |
| ScoreComparisonChart | 100% 父容器 | 300px | - | AI_COMPLETED / COMPLETED / REJECTED 时展示。TEACHER_SCORING 时教师评分可能为 null，不展示此区域 | 双系列分组柱状图。AI 评分紫色柱 + 教师评分蓝色柱 |
| Teacher Evaluation | 100% 父容器 | auto | - | 仅 COMPLETED / REJECTED 展示 | 白色卡片。支持文字为空时展示 EmptyState |
| Final Score | 100% 父容器 | auto(约 120px) | 左侧信息 + 右侧大数字 | 仅 COMPLETED / REJECTED 展示 | 左侧装饰线颜色区分合格/退回。最终成绩大号 JetBrains Mono 字体 |

**响应式行为**：
- 大于等于 960px：PageContainer max-width 960px，区域正常展示
- 768px 至 959px：AIScorePanel 维度卡片由 3 列变为 2 列。最终成绩区域 flex 方向不变但间距压缩
- 小于 768px：AIScorePanel 维度卡片单列堆叠。最终成绩区域 flex 方向变为 column（文字在上，大数字在下）。柱状图高度降为 250px

---

# Section 3: Component Tree & Specification

## 3.1 组件树

```
GradesPage.vue（页面组件，嵌套在 AppLayout 内）
├── PageHeader（title="成绩详情 - {taskName}"，breadcrumb items，description=提交物信息摘要）
├── <div> Status Banner 区（v-if="shouldShowBanner"）
│   ├── [AI_EVALUATING] AI 分析横幅（bg #EDE9FE，Loader 旋转图标，进度条，已用时间计数器）
│   ├── [AI_COMPLETED] 分析完成提示（bg #D1FAE5，CheckCircle 图标）
│   └── [TEACHER_SCORING] 评分中提示（bg #FFF7ED，Clock 图标）
├── <section> AIScorePanel 区域（v-if="evaluation && status !== 'NOT_SUBMITTED' && status !== 'SUBMITTED'"）
│   └── AIScorePanel（:aiResult，:isPolling，:pollingProgress，:currentDimension，:pollingError，@retry）
│       ├── [轮询态] ElProgressBar + 维度分析动画列表 + 重试按钮
│       └── [完成态] 6 维度评分卡片 Grid + 综合评价文字
├── <section> ScoreComparisonChart 区域（v-if="showComparisonChart"）
│   └── BarChart（title="维度得分对比"，xAxisData，seriesData，height=300，showLegend=true）
│       seriesData: [{ name:"AI 评分", data:[], color:"#8B5CF6" }, { name:"教师评分", data:[], color:"#3B82F6" }]
├── <section> 教师评价区域（v-if="status==='COMPLETED' || status==='REJECTED'"）
│   ├── <div> Section Header: "教师评价" + 评分教师信息
│   ├── [teacherEval 有内容] <div> 评价文字（white-space: pre-wrap）
│   └── [teacherEval 为 null/空] EmptyState（message="教师暂未填写文字评价"）
└── <section> 最终成绩区域（v-if="status==='COMPLETED' || status==='REJECTED'"）
    └── <div> Final Score Card
        ├── Left: TaskStatusBadge(status) + AI 综合评分 + 教师评分
        └── Right: 最终成绩大数字（JetBrains Mono, --font-size-4xl）
```

## 3.2 本页面需要新建的子组件

### 新组件 1: AIScorePanel（src/components/business/AIScorePanel.vue）

| 项 | 内容 |
|------|------|
| 组件职责 | 展示 AI 分析结果，包含轮询等待态和完成结果态两种模式。轮询态展示进度条和当前分析维度；完成态展示 6 维评分卡片和综合评语 |
| 复用场景 | 学生成绩详情页、教师批阅页（展示 AI 分析结果供教师参考） |

**Props**:

| Prop | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| aiResult | Object \| null | 否 | null | AI 分析结果对象。结构: { dimensions: Array<{ name: string, score: number, comment: string }>, overallComment: string, modelName: string, analyzedAt: string }。为 null 时配合 isPolling 展示轮询态 |
| isPolling | boolean | 否 | false | 是否正在轮询等待 AI 分析结果 |
| pollingProgress | number | 否 | 0 | 轮询进度百分比（0-100）。页面端计算: 由时间或后端返回的 progress 字段确定 |
| currentDimension | string | 否 | "" | 当前正在分析的维度名称，如"代码质量"。轮询态展示，完成态不展示 |
| pollingError | string \| null | 否 | null | 轮询失败或超时的错误信息。不为 null 时展示错误提示替代进度条 |
| modelName | string | 否 | "" | AI 模型名称标签，如 "DeepSeek-V3"、"Qwen-Max"。完成态在 Panel Header 中展示 |

**Emits**:

| 事件名 | 参数 | 说明 |
|--------|------|------|
| retry | 无 | 用户点击轮询错误区域中的"重新分析"按钮时触发。父组件重新发起轮询 |

**Slots**:
无。

**Store 依赖**:
无 Store 依赖。纯展示组件，数据由父组件通过 Props 传入。

**API 依赖**:
无 API 依赖。轮询逻辑由父组件（GradesPage）管理。

**状态覆盖**:

| 状态 | 条件 | 表现 |
|------|------|------|
| Polling | isPolling=true, aiResult=null, pollingError=null | 展示 Panel Header（"AI 分析中" + modelName 标签）。ElProgressBar 展示 pollingProgress 百分比。6 个维度名称列表，当前分析维度前有旋转 Loader 图标，已完成维度前有 CheckCircle 绿色图标，未开始维度前有灰色 Circle 图标。currentDimension 文字蓝色脉冲动画 |
| PollingTimeout | isPolling=true, pollingError="AI 分析超时，请稍后重试" | 进度条消失。展示 ErrorState（error icon + pollingError 消息 + "重新分析"按钮）。点击按钮触发 @retry |
| PollingError | isPolling=true, pollingError 非空且非超时（如"AI 服务异常，分析中断"） | 展示 ErrorState（error icon + pollingError 消息 + "重新分析"按钮）。点击按钮触发 @retry |
| Completed | isPolling=false, aiResult 有数据 | Panel Header "AI 分析结果" + modelName 标签（灰色小标签）。6 个维度卡片 Grid（3 列 x 2 行），每卡片包含：维度名称（--font-size-base）、AI 得分/100（--font-size-lg, JetBrains Mono）、进度色条（宽度=得分%，颜色根据得分区间: >=80 绿色 #10B981，60-79 橙色 #F59E0B，<60 红色 #EF4444）、AI 评语摘要（2-3 行截断，超长加"展开详情"按钮）。底部综合评价全宽展示，不限行数 |
| Empty | isPolling=false, aiResult=null | 不展示面板内容（此状态不会发生，因为 GradesPage 在无 AI 结果时不挂载此组件） |
| Error | aiResult 收到后数据格式错误（dimensions 数组为空、scores 越界等） | 静默降级：展示提示"AI 分析结果数据异常，请联系管理员"，不阻塞其他区域渲染 |

**维度卡片"展开详情"交互**:
每个维度卡片内的评语默认截断 3 行（CSS line-clamp: 3）。超过 3 行时展示"展开详情"文字按钮。点击后弹出 ElDialog（width: 560px），title 为维度名称，body 为完整评语文字。

**progress 色条颜色逻辑**（分值区间映射）:

| 得分区间 | 色条颜色 | CSS 变量 |
|---------|---------|---------|
| >= 80 | 绿色 | #10B981 (--color-success) |
| 60-79 | 橙色 | #F59E0B (--color-warning) |
| < 60 | 红色 | #EF4444 (--color-danger) |

**轮询动画细节**:
- currentDimension 文字使用 CSS animation pulse（opacity 1 → 0.5 → 1，1.5s 循环）
- 维度列表中未开始的维度图标使用灰色 #CBD5E1，已完成的维度图标使用绿色 #10B981，当前分析维度图标使用紫色 #8B5CF6 并旋转（animation spin 1s linear infinite）
- 已用时间计数器每秒自增 1，由父组件通过 setInterval 控制并传入

---

### 新组件 2: BarChart（src/components/chart/BarChart.vue）

| 项 | 内容 |
|------|------|
| 组件职责 | 基于 ECharts 封装分组柱状图，支持多系列对比 |
| 复用场景 | 学生成绩详情维度对比、教师端班级成绩分布、管理员数据大盘 |

**Props**:

| Prop | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| xAxisData | string[] | 是 | [] | X 轴标签数组（维度名称） |
| seriesData | Array<{ name: string, data: number[], color: string }> | 是 | [] | 系列数据，name=图例标签+分组名，data=数值数组，color=柱状颜色 |
| height | number | 否 | 300 | 图表容器高度 |
| showLegend | boolean | 否 | true | 是否显示图例 |
| title | string | 否 | "" | 图表标题 |
| barWidth | number | 否 | 30 | 柱状条宽度（px），分组间距由 ECharts 自动计算 |

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
| Empty | xAxisData 长度为 0 或 seriesData 为空数组 | 展示占位区域 + 居中提示"暂无对比数据" |
| Success | xAxisData 和 seriesData 均有数据，seriesData 中每项的 data 数值有效 | 渲染 ECharts 分组柱状图。柱子间距 20%（barGap: '20%'）。hover 展示 tooltip（浮层展示系列名+维度名+具体数值）。若某系列 data 中某项为 null，该位置柱子不绘制（ECharts 默认忽略 null 值） |
| Error | ECharts 初始化失败 | 静默降级：展示占位区域 + 提示"图表加载失败" |

**柱状图样式规范**:
- 柱子圆角: 顶部 4px（itemStyle.borderRadius: [4, 4, 0, 0]）
- 柱子之间存在间隙（barGap: '20%'）
- Y 轴范围: 0-100（评分范围固定）
- Y 轴刻度线: 每 20 分一条网格线（splitLine）
- 图例位置: 底部居中（legend.bottom: 0）
- Tooltip 触发方式: axis（浮层展示该轴所有系列数值）

---

## 3.3 已有组件引用

**引用组件 1: PageHeader（src/components/common/PageHeader.vue）**

关键 Props 配置：

| Prop | 值 | 说明 |
|------|-----|------|
| title | computed: "成绩详情 - " + evaluation.taskName | 动态拼接任务名 |
| breadcrumb | [{ path: "/student/dashboard", label: "首页" }, { path: "/student/dashboard", label: "仪表盘" }, { path: route.fullPath, label: "成绩详情" }] | 三级面包屑 |
| description | computed: evaluation.courseName + " | 提交时间: " + formatDate(evaluation.submittedAt) | 课程名和提交时间 |

**引用组件 2: TaskStatusBadge（src/components/business/TaskStatusBadge.vue）**

在最终成绩卡片左侧展示。

| Prop | 值 | 说明 |
|------|-----|------|
| status | evaluation.status | COMPLETED 或 REJECTED |

**引用组件 3: ErrorState（src/components/common/ErrorState.vue）**

两个使用场景：
1. 页面级 API 调用失败时全页展示（evaluation 接口失败）
2. AIScorePanel 内轮询超时/错误时展示（小尺寸，不占全页）

**引用组件 4: LoadingState（src/components/common/LoadingState.vue）**

页面首次加载 evaluation 接口时全页展示。轮询期间不展示 LoadingState（AIScorePanel 自行管理轮询态）。

**引用组件 5: EmptyState（src/components/common/EmptyState.vue）**

教师评价文字为空时展示。

**引用组件 6: BaseButton（src/components/base/BaseButton.vue）**

AIScorePanel 内"重新分析"按钮和"展开详情"按钮使用。

**引用组件 7: ElProgressBar（来自 Element Plus，无需封装）**

AIScorePanel 轮询态进度条。Props: percentage, stroke-width=8, color="#8B5CF6"。

**引用组件 8: ElDialog（来自 Element Plus，无需封装）**

维度评语"展开详情"弹出框。Props: title=维度名称, width="560px", destroy-on-close。

---

# Section 4: State Design（页面级）

| 状态 | 触发条件 | UI 表现 | 持续时间 | 退出条件 |
|------|---------|--------|---------|---------|
| Loading | 页面进入，evaluation API 开始调用，首次获取提交物评价数据 | 展示 LoadingState 组件（居中 Spinner + "加载成绩数据..."）。PageHeader 区使用路由参数 submissionId 展示骨架 (title 灰色脉冲矩形, breadcrumb 灰色矩形)。AIScorePanel 区域不渲染。其他区域不渲染 | 直到 evaluation API 返回。Mock 延迟 500ms，超时 15s | evaluation API 返回成功或失败 |
| AI Evaluating (Polling) | evaluation API 返回后，status 为 AI_EVALUATING | Status Banner 展示紫色 AI 分析中横幅（Loader 旋转图标 + 进度条 + 已用时间）。AIScorePanel 展示轮询态（进度条 + 维度分析动画列表）。每 3 秒调用 GET /api/v1/student/submissions/{submissionId}/ai-result。其他区域不渲染（ScoreComparisonChart、Teacher Evaluation、Final Score 均隐藏） | 最长 2 分钟（40 次轮询）。每次轮询更新进度（progress 值）。已用时间每秒自增 1 | 三种退出路径：(1) AI 返回 COMPLETED → 状态转为 AI_COMPLETED；(2) 轮询 2 分钟超时 → pollingError="AI 分析超时，请稍后重试"，AIScorePanel 展示 ErrorState + 重试按钮；(3) AI 服务返回错误 → pollingError=错误消息，AIScorePanel 展示 ErrorState + 重试按钮 |
| AI Completed | evaluation API 返回 status 为 AI_COMPLETED，或轮询检测到 ai-result 状态变为 COMPLETED 后重新获取 evaluation 发现 status 变更 | Status Banner 展示绿色"AI 分析已完成"提示条。AIScorePanel 展示完成态（6 维评分卡片 Grid + 综合评价）。得分对比柱状图展示（此时仅 AI 评分系列有数据，教师评分系列为空或所有值为 null）。教师评价区域和最终成绩区域不渲染 | 持续直到教师完成评分（status 变为 TEACHER_SCORING 或 COMPLETED） | 用户手动刷新页面后检测到 status 变更，或页面接收到 WebSocket 通知（Sprint 4 功能） |
| Teacher Scoring | evaluation API 返回 status 为 TEACHER_SCORING | Status Banner 展示橙色"教师正在评分中"提示条。AIScorePanel 展示完成态（AI 结果已稳定）。得分对比柱状图不展示（教师评分数据不完整）。教师评价区域不渲染。最终成绩区域不渲染 | 持续直到教师提交评分 | 教师提交评分后 status 变为 COMPLETED 或 REJECTED |
| Completed (Final) | evaluation API 返回 status 为 COMPLETED | Status Banner 不展示。AIScorePanel 展示完成态（AI 结果）。得分对比柱状图展示双系列（AI 评分紫色 + 教师评分蓝色）。教师评价区域展示：若有评价文字则渲染文字内容，若无则展示 EmptyState("教师暂未填写文字评价")。最终成绩区域展示：左侧 TaskStatusBadge(COMPLETED) + AI 评分 + 教师评分，右侧最终成绩大数字（JetBrains Mono, --color-primary, --font-size-4xl）。卡片左侧装饰线绿色 #10B981 | 持续直到用户离开页面 | 用户导航离开 |
| Rejected | evaluation API 返回 status 为 REJECTED | Status Banner 不展示。AIScorePanel 展示完成态。得分对比柱状图展示双系列。教师评价区域展示教师评语（通常包含退回原因）。最终成绩区域展示：左侧 TaskStatusBadge(REJECTED) + AI 评分 + 教师评分（可能为 0 或不展示），右侧最终成绩大数字（--color-danger）。卡片左侧装饰线红色 #EF4444 | 持续直到用户重新提交后状态变更 | 用户导航离开或重新提交（重新提交后 status 变回 SUBMITTED） |
| Error | evaluation API 返回 code != 0 或网络超时 | 页面展示 ErrorState（error icon + 错误消息 + "返回仪表盘"按钮）。全页不渲染任何业务区域。错误消息根据 code：(a) 1001 → "提交物不存在或已被删除"；(b) 5001 → "服务繁忙，请稍后重试"；(c) 网络错误 → "网络连接失败，请检查网络"；(d) 2001 → "认证已过期，请重新登录" | 直到用户点击"返回仪表盘"或手动刷新 | 用户点击"返回仪表盘"→ router.push("/student/dashboard")。或手动 F5 刷新 |
| NoPermission | 非 student 角色用户访问，或学生尝试查看其他学生的提交物 | 路由守卫拦截，重定向到角色首页。若守卫被绕过，页面展示 NoPermission 组件（message="你没有权限查看此成绩详情"） | - | 用户返回上一页 |
| Offline | 浏览器网络断开 | Navbar 下方黄色全局提示条。轮询期间网络断开：轮询请求失败，AIScorePanel 展示 pollingError="网络连接已断开"，不终止轮询定时器（网络恢复后继续轮询）。非轮询期间网络断开：已加载数据保持可见 | 直到网络恢复 | navigator.onLine 变为 true |

**AI 轮询状态机流转图**（页面级核心状态逻辑）:

```
Loading
  → evaluation.status === "AI_EVALUATING" → AI Evaluating (启动轮询)
  → evaluation.status === "AI_COMPLETED"  → AI Completed
  → evaluation.status === "TEACHER_SCORING" → Teacher Scoring
  → evaluation.status === "COMPLETED"     → Completed
  → evaluation.status === "REJECTED"      → Rejected
  → API Error                             → Error

AI Evaluating (3s 轮询)
  → ai-result.status === "COMPLETED"      → 获取最新 evaluation → AI Completed / Teacher Scoring / Completed
  → 轮询超时 (2 min)                       → pollingError, 仍在 AI Evaluating（展示错误但保留轮询态）
  → 用户点击"重新分析"                      → 重新启动轮询，pollingError=null

AI Completed
  → 用户刷新页面                           → 重新获取 evaluation → 可能进入 Teacher Scoring / Completed / Rejected

Teacher Scoring
  → 用户刷新页面                           → 重新获取 evaluation → Completed / Rejected

Completed / Rejected
  → 最终状态，不自动流转                    → 用户离开页面
```

---

# Section 5: Data Flow

## 5.1 页面加载数据流

```
用户从 Dashboard 任务表格点击行或直接访问 /student/grades/{submissionId}
Step 1: 路由导航触发
  → 路由守卫 beforeEnter:
    → 检查 Token 有效且 role === "student"
    → 通过 → 放行
  → GradesPage.vue 挂载

Step 2: onMounted 执行
  → 页面进入 Loading 状态
  → 从路由参数获取 submissionId = route.params.submissionId
  → 调用 GET /api/v1/student/submissions/{submissionId}/evaluation
  → 等待响应

Step 3: evaluation API 响应处理
  Case A: 响应成功 (code=0)
    → 解析 evaluation = response.data
    → 根据 evaluation.status 进入对应状态:
      → status === "AI_EVALUATING"  → 进入 AI Evaluating 状态 → 启动 AI 轮询
      → status === "AI_COMPLETED"   → 进入 AI Completed 状态 → 渲染 AI 结果 + 对比图
      → status === "TEACHER_SCORING" → 进入 Teacher Scoring 状态 → 渲染 AI 结果
      → status === "COMPLETED"      → 进入 Completed 状态 → 渲染全量内容
      → status === "REJECTED"       → 进入 Rejected 状态 → 渲染全量内容
    → 退出 Loading 状态

  Case B: 响应失败 (code != 0 或网络错误)
    → 进入 Error 状态
    → 展示 ErrorState + "返回仪表盘"按钮

Step 4 (仅 AI Evaluating 状态): 启动 AI 轮询
  → pollingTimer = setInterval(() => { ... }, 3000)
  → elapsedTimer = setInterval(() => { elapsedSeconds++ }, 1000)
  → timeoutTimer = setTimeout(() => { pollingError = "超时" }, 120000)
  → 每次轮询:
    → GET /api/v1/student/submissions/{submissionId}/ai-result
    → 响应 status === "PROCESSING":
      → pollingProgress = response.data.progress (0-100)
      → currentDimension = response.data.currentDimension
      → 继续轮询
    → 响应 status === "COMPLETED":
      → 清除所有定时器
      → 重新调用 GET evaluation 接口获取完整评价数据
      → 根据新的 evaluation.status 进入对应状态
    → 响应失败:
      → pollingError = "AI 服务异常，分析中断"
      → 清除 elapsedTimer，保留 pollingTimer（可重试）
```

## 5.2 用户交互数据流 - 轮询超时后重试

```
Given: AI Evaluating 状态，轮询已超过 2 分钟，AIScorePanel 展示"AI 分析超时，请稍后重试"和"重新分析"按钮
When: 用户点击"重新分析"按钮
Then:
  → AIScorePanel 发出 @retry 事件
  → GradesPage.handleRetryAnalysis() 执行:
    1. pollingError = null
    2. pollingProgress = 0
    3. elapsedSeconds = 0
    4. 清除旧的 timeoutTimer
    5. 重新启动 pollingTimer (setInterval 3s) 和 timeoutTimer (setTimeout 120s)
    6. 立即发起第一次轮询请求（不等 3 秒）
  → AIScorePanel 回到轮询态（进度条 + 维度动画）
```

## 5.3 用户交互数据流 - 维度评语展开详情

```
Given: AIScorePanel 处于完成态，某个维度卡片内评语超过 3 行被截断
When: 用户点击该维度卡片中的"展开详情"按钮
Then:
  → AIScorePanel 内部状态: dialogVisible = true, selectedDimension = 该维度的数据对象
  → ElDialog 弹出（width: 560px）:
    title: selectedDimension.name
    body: selectedDimension.comment（完整文字，white-space: pre-wrap, line-height: 1.6）
    底部有"关闭"按钮（ElDialog 原生 footer）
  → 用户点击"关闭"或遮罩层 → dialogVisible = false
When: 用户点击其他维度卡片的"展开详情"
Then:
  → selectedDimension 切换为对应维度数据（复用同一个 ElDialog 实例）
```

## 5.4 用户交互数据流 - 点击面包屑返回仪表盘

```
Given: GradesPage 处于任意状态（Loading / AI Evaluating / Completed 等）
When: 用户点击面包屑中的"仪表盘"链接
Then:
  → router.push("/student/dashboard")
  → 路由导航离开 GradesPage
  → onUnmounted 执行:
    → 清除所有定时器（pollingTimer, elapsedTimer, timeoutTimer）
    → 清除 window online/offline 监听
  → 仪表盘页面可能从 keep-alive 缓存恢复（若有缓存配置）
```

## 5.5 用户交互数据流 - 教师评价文字为空时

```
Given: status 为 COMPLETED，evaluation.teacherEvaluation 或 evaluation.evaluation 字段为 null
When: GradesPage 渲染教师评价区域
Then:
  → 条件判断: evaluation.evaluation === null || evaluation.evaluation === ""
  → 展示 EmptyState 组件:
    message: "教师暂未填写文字评价"
    actionButton: 不展示（学生没有权限请求教师填写评价）
  → 不展示"评分教师: xxx | 评分时间: xxx" meta 信息（没有评价则 meta 无意义）
  → 最终成绩区域仍正常展示（最终成绩与教师文字评价解耦）
```

---

# Section 6: API & Mock Specification

## 接口 1: GET /api/v1/student/submissions/{submissionId}/evaluation

用途: 获取提交物的完整评价数据（含 AI 分析和教师评分）。学生进入成绩详情页时一次性获取

请求方式: GET

请求参数:

| 参数名 | 类型 | 必填 | 位置 | 说明 | 示例值 |
|--------|------|------|------|------|--------|
| submissionId | number | 是 | Path | 提交物 ID | 100 |

请求 Header: Authorization: Bearer {token}

响应数据结构:

```
{
  code: number,
  message: string,
  data: {
    submissionId: number,                // 提交物 ID
    taskName: string,                    // 任务名称
    courseName: string,                  // 课程名称
    submittedAt: string,                 // 提交时间（ISO 8601）
    status: string,                      // 评审状态: AI_EVALUATING / AI_COMPLETED / TEACHER_SCORING / COMPLETED / REJECTED
    aiResult: {                          // AI 分析结果（AI_EVALUATING 时可能为 null 或不完整）
      modelName: string,                 // AI 模型名称
      analyzedAt: string | null,         // 分析完成时间
      dimensions: Array<{
        name: string,                    // 维度名称，如"代码质量"
        score: number,                   // AI 评分（0-100）
        comment: string                  // AI 评语
      }>,
      overallComment: string             // AI 综合评价
    } | null,
    teacherEvaluation: {                 // 教师评价
      teacherName: string | null,        // 评分教师姓名
      scoredAt: string | null,           // 评分时间
      evaluation: string | null,         // 教师文字评语
      dimensions: Array<{                // 教师各维度评分
        name: string,
        score: number
      }> | null
    } | null,
    finalScore: {                        // 最终成绩
      aiScore: number,                   // AI 综合评分（0-100）
      teacherScore: number | null,       // 教师评分（0-100），未评分时为 null
      finalScore: number | null          // 加权最终成绩（0-100），未完成全部评分流程时为 null
    }
  } | null,
  success: boolean,
  timestamp: string,
  traceId: string
}
```

Mock 数据示例 1（AI 分析中 - 刚提交，AI 正在分析）:

```
{
  "code": 0,
  "message": "success",
  "data": {
    "submissionId": 100,
    "taskName": "在线商城数据库设计",
    "courseName": "数据库原理",
    "submittedAt": "2026-07-03T08:30:00+08:00",
    "status": "AI_EVALUATING",
    "aiResult": null,
    "teacherEvaluation": null,
    "finalScore": {
      "aiScore": 0,
      "teacherScore": null,
      "finalScore": null
    }
  },
  "success": true,
  "timestamp": "2026-07-03T09:00:00+08:00",
  "traceId": "trace-eval-mock-001"
}
```

Mock 数据示例 2（AI 分析完成 - AI 已评分，等待教师评分）:

```
{
  "code": 0,
  "message": "success",
  "data": {
    "submissionId": 100,
    "taskName": "在线商城数据库设计",
    "courseName": "数据库原理",
    "submittedAt": "2026-07-03T08:30:00+08:00",
    "status": "AI_COMPLETED",
    "aiResult": {
      "modelName": "DeepSeek-V3",
      "analyzedAt": "2026-07-03T08:35:00+08:00",
      "dimensions": [
        { "name": "代码质量", "score": 88, "comment": "代码结构清晰，命名规范良好。部分方法体过长，建议使用短函数拆分逻辑。异常处理覆盖了主要场景但缺少对边缘输入值（null/空字符串）的校验。建议增加单元测试覆盖率。代码注释较为完整，但部分注释存在中英文混用问题，建议统一使用中文注释风格以保持团队一致性。" },
        { "name": "需求理解", "score": 82, "comment": "数据库设计基本覆盖了商城核心业务需求。用户-订单-商品的关系模型清晰，但未考虑商品分类的多级嵌套场景。建议补充商品规格（SKU）的扩展设计以支持更复杂的电商场景。" },
        { "name": "项目管理", "score": 75, "comment": "项目文件结构合理，但缺少 README 文档说明项目启动方式。Git 提交记录显示初期提交频率较高但后期集中式提交较多，建议保持每日提交的习惯。Branch 命名不够规范，建议采用 feature/前缀命名方式。" },
        { "name": "团队协作", "score": 90, "comment": "PR Review 参与度高，代码评审意见具体且建设性。团队分工明确，无明显的代码重复和冲突。沟通记录显示积极参与每日站会和 Sprint Review。" },
        { "name": "文档规范", "score": 85, "comment": "数据库 ER 图和接口文档齐全。API 文档使用 Swagger 自动生成，格式规范。缺少数据库表字段的详细中文注释说明，建议在 DDL 脚本中补充字段含义注释。" },
        { "name": "创新能力", "score": 78, "comment": "使用了 Redis 缓存热点商品数据，展现了性能优化的意识。索引设计考虑了常见查询场景但未覆盖联合查询的复合索引优化。建议引入读写分离方案以应对高并发场景。" }
      ],
      "overallComment": "整体完成度较高，数据库设计规范，代码质量良好。主要改进方向：加强输入校验和异常处理，补充 SKU 扩展设计以支持更复杂的电商业务场景，优化索引设计以提升查询性能。继续保持团队协作和代码评审的积极性。"
    },
    "teacherEvaluation": null,
    "finalScore": {
      "aiScore": 83,
      "teacherScore": null,
      "finalScore": null
    }
  },
  "success": true,
  "timestamp": "2026-07-03T09:00:00+08:00",
  "traceId": "trace-eval-mock-002"
}
```

Mock 数据示例 3（教师评分中）:

```
{
  "code": 0,
  "message": "success",
  "data": {
    "submissionId": 100,
    "taskName": "在线商城数据库设计",
    "courseName": "数据库原理",
    "submittedAt": "2026-07-03T08:30:00+08:00",
    "status": "TEACHER_SCORING",
    "aiResult": {
      "modelName": "DeepSeek-V3",
      "analyzedAt": "2026-07-03T08:35:00+08:00",
      "dimensions": [
        { "name": "代码质量", "score": 88, "comment": "代码结构清晰，命名规范良好。" },
        { "name": "需求理解", "score": 82, "comment": "基本覆盖核心需求。" },
        { "name": "项目管理", "score": 75, "comment": "项目结构合理。" },
        { "name": "团队协作", "score": 90, "comment": "PR Review 参与度高。" },
        { "name": "文档规范", "score": 85, "comment": "文档齐全规范。" },
        { "name": "创新能力", "score": 78, "comment": "使用了缓存优化。" }
      ],
      "overallComment": "整体完成度较高。"
    },
    "teacherEvaluation": null,
    "finalScore": {
      "aiScore": 83,
      "teacherScore": null,
      "finalScore": null
    }
  },
  "success": true,
  "timestamp": "2026-07-03T09:30:00+08:00",
  "traceId": "trace-eval-mock-003"
}
```

Mock 数据示例 4（已完成 - 完整评分流程结束）:

```
{
  "code": 0,
  "message": "success",
  "data": {
    "submissionId": 100,
    "taskName": "在线商城数据库设计",
    "courseName": "数据库原理",
    "submittedAt": "2026-07-03T08:30:00+08:00",
    "status": "COMPLETED",
    "aiResult": {
      "modelName": "DeepSeek-V3",
      "analyzedAt": "2026-07-03T08:35:00+08:00",
      "dimensions": [
        { "name": "代码质量", "score": 88, "comment": "代码结构清晰，命名规范良好。部分方法体过长，建议拆分。" },
        { "name": "需求理解", "score": 82, "comment": "基本覆盖商城核心业务需求。" },
        { "name": "项目管理", "score": 75, "comment": "项目文件结构合理。" },
        { "name": "团队协作", "score": 90, "comment": "PR Review 参与度高，代码评审意见具体。" },
        { "name": "文档规范", "score": 85, "comment": "ER 图和接口文档齐全。" },
        { "name": "创新能力", "score": 78, "comment": "使用了 Redis 缓存热点数据。" }
      ],
      "overallComment": "整体完成度较高，数据库设计规范。"
    },
    "teacherEvaluation": {
      "teacherName": "李老师",
      "scoredAt": "2026-07-03T09:15:00+08:00",
      "evaluation": "数据库设计整体完成度较高，ER 图清晰表达了业务关系。需要注意以下几点：\n1. 订单表的 status 字段建议使用枚举类型而非字符串，保证数据一致性。\n2. 用户地址表缺少默认地址标识字段，影响用户体验。\n3. 商品表缺少上下架状态字段，不利于后台管理。\n4. 建议补充软删除（deleted_at）设计，便于数据恢复和审计。\n\n总体表现优秀，继续保持。",
      "dimensions": [
        { "name": "代码质量", "score": 85 },
        { "name": "需求理解", "score": 80 },
        { "name": "项目管理", "score": 72 },
        { "name": "团队协作", "score": 88 },
        { "name": "文档规范", "score": 82 },
        { "name": "创新能力", "score": 76 }
      ]
    },
    "finalScore": {
      "aiScore": 83,
      "teacherScore": 80.5,
      "finalScore": 81.8
    }
  },
  "success": true,
  "timestamp": "2026-07-03T09:30:00+08:00",
  "traceId": "trace-eval-mock-004"
}
```

Mock 数据示例 5（已退回 - 教师退回要求修改）:

```
{
  "code": 0,
  "message": "success",
  "data": {
    "submissionId": 103,
    "taskName": "图书管理系统需求分析文档",
    "courseName": "软件工程导论",
    "submittedAt": "2026-07-02T16:20:00+08:00",
    "status": "REJECTED",
    "aiResult": {
      "modelName": "DeepSeek-V3",
      "analyzedAt": "2026-07-02T16:25:00+08:00",
      "dimensions": [
        { "name": "代码质量", "score": 65, "comment": "代码存在较多冗余逻辑，圈复杂度过高。" },
        { "name": "需求理解", "score": 55, "comment": "需求分析文档缺少核心用例场景描述。" },
        { "name": "项目管理", "score": 70, "comment": "项目计划时间估算偏差较大。" },
        { "name": "团队协作", "score": 80, "comment": "协作沟通正常。" },
        { "name": "文档规范", "score": 50, "comment": "文档格式不符合模板要求，缺少 UML 时序图。" },
        { "name": "创新能力", "score": 60, "comment": "方案较为常规，缺乏创新点。" }
      ],
      "overallComment": "需求分析文档质量有待提升，建议参考模板重新编写。"
    },
    "teacherEvaluation": {
      "teacherName": "李老师",
      "scoredAt": "2026-07-03T10:00:00+08:00",
      "evaluation": "需求分析文档不满足基本要求，退回修改。主要问题：\n1. 缺少核心用例场景的详细描述，需补充主成功场景和异常场景。\n2. UML 用例图和时序图缺失，需按模板要求补充。\n3. 非功能性需求章节过于简略，需补充性能、安全性、可维护性相关要求。\n4. 文档整体格式不规范，标题编号混乱，表格样式不统一。\n\n请于 7 月 10 日前完成修改并重新提交。",
      "dimensions": [
        { "name": "代码质量", "score": 62 },
        { "name": "需求理解", "score": 50 },
        { "name": "项目管理", "score": 68 },
        { "name": "团队协作", "score": 78 },
        { "name": "文档规范", "score": 45 },
        { "name": "创新能力", "score": 58 }
      ]
    },
    "finalScore": {
      "aiScore": 63.3,
      "teacherScore": 60.2,
      "finalScore": 45
    }
  },
  "success": true,
  "timestamp": "2026-07-03T10:05:00+08:00",
  "traceId": "trace-eval-mock-005"
}
```

Mock 数据示例 6（教师评语为空 - 教师仅打分未写评语）:

```
{
  "code": 0,
  "message": "success",
  "data": {
    "submissionId": 105,
    "taskName": "Spring Boot 单元测试实践",
    "courseName": "软件测试",
    "submittedAt": "2026-07-01T10:00:00+08:00",
    "status": "COMPLETED",
    "aiResult": {
      "modelName": "DeepSeek-V3",
      "analyzedAt": "2026-07-01T10:05:00+08:00",
      "dimensions": [
        { "name": "代码质量", "score": 92, "comment": "测试代码质量高，用例覆盖全面。" },
        { "name": "需求理解", "score": 88, "comment": "测试用例覆盖了主要业务场景。" },
        { "name": "项目管理", "score": 85, "comment": "测试计划执行良好。" },
        { "name": "团队协作", "score": 90, "comment": "测试报告分享及时。" },
        { "name": "文档规范", "score": 87, "comment": "测试文档规范清晰。" },
        { "name": "创新能力", "score": 82, "comment": "引入了自动化测试流程。" }
      ],
      "overallComment": "测试执行质量优秀。"
    },
    "teacherEvaluation": {
      "teacherName": "王老师",
      "scoredAt": "2026-07-02T14:00:00+08:00",
      "evaluation": null,
      "dimensions": [
        { "name": "代码质量", "score": 90 },
        { "name": "需求理解", "score": 86 },
        { "name": "项目管理", "score": 84 },
        { "name": "团队协作", "score": 88 },
        { "name": "文档规范", "score": 85 },
        { "name": "创新能力", "score": 80 }
      ]
    },
    "finalScore": {
      "aiScore": 87.3,
      "teacherScore": 85.5,
      "finalScore": 86.4
    }
  },
  "success": true,
  "timestamp": "2026-07-03T10:05:00+08:00",
  "traceId": "trace-eval-mock-006"
}
```

Mock 错误场景:
- 1001 提交物不存在: { "code": 1001, "message": "提交物不存在或已被删除", "data": null, "success": false, "timestamp": "2026-07-03T09:00:00+08:00", "traceId": "trace-eval-err-001" }
- 2001 认证过期: { "code": 2001, "message": "认证已过期，请重新登录", "data": null, "success": false, "timestamp": "2026-07-03T09:00:00+08:00", "traceId": "trace-eval-err-002" }
- 5001 服务器错误: { "code": 5001, "message": "服务器繁忙，请稍后重试", "data": null, "success": false, "timestamp": "2026-07-03T09:00:00+08:00", "traceId": "trace-eval-err-003" }

Mock 延迟: 500ms

Mock 实现位置: src/mock/evaluation.ts

Mock 逻辑:
- submissionId 为 100 → 返回 Mock 示例 2（AI_COMPLETED）或 4（COMPLETED），通过查询参数 `?mockState=` 切换（`ai_completed`, `completed`, `teacher_scoring`, `rejected`）
- submissionId 为 101 → 返回 Mock 示例 1（AI_EVALUATING）
- submissionId 为 103 → 返回 Mock 示例 5（REJECTED）
- submissionId 为 105 → 返回 Mock 示例 6（COMPLETED，评语为空）
- submissionId 为 999 → 返回 1001 错误
- 其他 → 返回正常 COMPLETED 数据

---

## 接口 2: GET /api/v1/student/submissions/{submissionId}/ai-result

用途: AI 分析轮询接口。学生在提交物状态为 AI_EVALUATING 时每 3 秒调用一次，获取当前分析进度和维度信息

请求方式: GET

请求参数:

| 参数名 | 类型 | 必填 | 位置 | 说明 | 示例值 |
|--------|------|------|------|------|--------|
| submissionId | number | 是 | Path | 提交物 ID | 100 |

请求 Header: Authorization: Bearer {token}

响应数据结构:

```
{
  code: number,
  message: string,
  data: {
    status: string,             // PROCESSING（分析中）| COMPLETED（分析完成）| FAILED（分析失败）
    progress: number,           // 进度 0-100
    currentDimension: string,   // 当前正在分析的维度名称，如"代码质量"。COMPLETED 时为空字符串
    analyzedDimensions: number, // 已完成分析的维度数量（0-6）
    totalDimensions: number     // 总维度数量（固定为 6）
  } | null,
  success: boolean,
  timestamp: string,
  traceId: string
}
```

Mock 数据示例 1（分析中 - 进度 40%）:

```
{
  "code": 0,
  "message": "success",
  "data": {
    "status": "PROCESSING",
    "progress": 40,
    "currentDimension": "需求理解",
    "analyzedDimensions": 2,
    "totalDimensions": 6
  },
  "success": true,
  "timestamp": "2026-07-03T09:01:00+08:00",
  "traceId": "trace-ai-result-mock-001"
}
```

Mock 数据示例 2（分析中 - 进度 75%）:

```
{
  "code": 0,
  "message": "success",
  "data": {
    "status": "PROCESSING",
    "progress": 75,
    "currentDimension": "文档规范",
    "analyzedDimensions": 4,
    "totalDimensions": 6
  },
  "success": true,
  "timestamp": "2026-07-03T09:02:00+08:00",
  "traceId": "trace-ai-result-mock-002"
}
```

Mock 数据示例 3（分析完成）:

```
{
  "code": 0,
  "message": "success",
  "data": {
    "status": "COMPLETED",
    "progress": 100,
    "currentDimension": "",
    "analyzedDimensions": 6,
    "totalDimensions": 6
  },
  "success": true,
  "timestamp": "2026-07-03T09:03:00+08:00",
  "traceId": "trace-ai-result-mock-003"
}
```

Mock 数据示例 4（分析失败）:

```
{
  "code": 0,
  "message": "success",
  "data": {
    "status": "FAILED",
    "progress": 25,
    "currentDimension": "",
    "analyzedDimensions": 1,
    "totalDimensions": 6
  },
  "success": true,
  "timestamp": "2026-07-03T09:01:30+08:00",
  "traceId": "trace-ai-result-mock-004"
}
```

Mock 轮询策略:
- submissionId 为 101（AI_EVALUATING 状态）：轮询 1-4 次后自动转为 COMPLETED（模拟真实分析过程）。每次轮询返回递增的 progress：第 1 次 25，第 2 次 50，第 3 次 75，第 4 次 100（COMPLETED）。维度顺序循环: 代码质量 → 需求理解 → 项目管理 → 团队协作 → 文档规范 → 创新能力。
- submissionId 为 102（模拟分析失败）：第 1 次 progress=25，第 2 次直接返回 FAILED。
- 其他 submissionId：直接返回 COMPLETED（表示 AI 已分析完成，不触发轮询）。

Mock 延迟: 300ms

Mock 实现位置: src/mock/evaluation.ts（与 evaluation 接口同文件）

---

# Section 7: Interaction Flows

## 交互 1: 查看已完成评分的结果（正常完整流程）

Given: 学生张三从 Dashboard 近期任务表格点击 id=100 的 COMPLETED 任务行
When:
1. 路由导航到 /student/grades/100
2. GradesPage onMounted 调用 GET /api/v1/student/submissions/100/evaluation
3. 返回 Mock 示例 4（COMPLETED 状态）

Then:
1. PageHeader 展示"成绩详情 - 在线商城数据库设计"，面包屑: 首页 > 仪表盘 > 成绩详情
2. 不展示 Status Banner（COMPLETED 状态跳过）
3. AIScorePanel 展示完成态: Panel Header "AI 分析结果" + "DeepSeek-V3" 标签。6 个维度卡片 3x2 Grid，每张卡片有维度名 + AI 得分/100 + 颜色进度条 + 评语摘要。底部综合评价全宽展示
4. 得分对比柱状图展示两组柱子: 紫色"AI 评分"和蓝色"教师评分"，每维度一组。hover tooltip 展示详细数值
5. 教师评价区域展示: "教师评价"标题 + "评分教师: 李老师 | 评分时间: 2026-07-03 09:15" + 教师评语文字（保持换行格式）
6. 最终成绩卡片: 左侧 TaskStatusBadge(COMPLETED 绿色) + AI 综合评分 83 分 + 教师评分 80.5 分。右侧大数字"81.8 分"（JetBrains Mono，蓝色）
7. 卡片左侧装饰线绿色 #10B981

## 交互 2: 进入 AI 分析中的页面并等待分析完成

Given: 学生刚提交了 id=101 的提交物，Dashboard 显示状态为"AI分析中"
When:
1. 路由导航到 /student/grades/101
2. evaluation API 返回 status: AI_EVALUATING
3. 页面进入 AI Evaluating 状态，启动 3 秒轮询

Then:
1. Status Banner 展示紫色横幅: Loader 旋转图标 + "AI 正在分析你的提交..." + ElProgressBar（percentage=0） + "已用时 0 秒"
2. AIScorePanel 展示轮询态: 6 个维度列表，每个维度前有灰色 Circle 图标。第 1 个维度"代码质量"前显示旋转 Loader（紫色），文字"正在分析代码质量..."蓝色脉冲
3. 其他区域全部隐藏（ScoreComparisonChart、Teacher Evaluation、Final Score 均不渲染）
4. 3 秒后第 1 次轮询: progress=25, currentDimension="需求理解", analyzedDimensions=1。维度列表中第 1 个维度图标变为绿色 CheckCircle，第 2 个维度开始脉冲
5. 已用时间计数器每秒递增 1
6. 继续轮询: 第 2 次 progress=50, 第 3 次 progress=75
7. 第 4 次轮询: status="COMPLETED", progress=100
8. 清除所有定时器，重新调用 evaluation 接口
9. evaluation 返回 status: AI_COMPLETED（或 COMPLETED/ TEACHER_SCORING 取决于 Mock 配置）
10. 页面转入对应状态，渲染完整评分内容。轮询总耗时约 12 秒（4 次 x 3 秒）

## 交互 3: AI 分析轮询超时

Given: 页面处于 AI Evaluating 状态，轮询已持续 120 秒仍未收到 COMPLETED
When:
1. timeoutTimer 触发（setTimeout 120000ms 到期）
2. pollingError 设置为 "AI 分析超时，请稍后重试"

Then:
1. AIScorePanel 轮询态消失
2. AIScorePanel 内部展示 ErrorState: error icon + "AI 分析超时，请稍后重试" + "重新分析"按钮
3. elapsedTimer 清除（已用时间停止更新）
4. pollingTimer 保留继续运行（后台继续轮询，即使展示超时提示也不停止，防止后端实际已完成但前端已放弃的情况）
5. Status Banner 仍展示紫色横幅但进度条停在最后一次进度值
6. 用户点击"重新分析"按钮 → pollingError 清空 → 重置 elapsedSeconds=0 → 重新启动 timeoutTimer → AIScorePanel 恢复轮询态
7. 如果此时轮询恰好返回 COMPLETED → 正常流转到完成状态

## 交互 4: 查看被退回的成绩

Given: 学生张三的 id=103 提交物被教师退回
When:
1. 路由导航到 /student/grades/103
2. evaluation API 返回 Mock 示例 5（status: REJECTED）

Then:
1. PageHeader 正常展示
2. 不展示 Status Banner
3. AIScorePanel 展示完成态。AI 各维度得分偏低（代码质量 65，文档规范 50），进度条颜色为橙色和红色
4. 得分对比柱状图展示两组柱子（AI 评分和教师评分均偏低）
5. 教师评价区域展示退回原因和修改要求（5 点详细说明），"评分教师: 李老师"
6. 最终成绩卡片: 左侧 TaskStatusBadge(REJECTED 红色) + AI 综合评分 63.3 分 + 教师评分 60.2 分。右侧大数字"45 分"（红色 #EF4444）
7. 卡片左侧装饰线红色 #EF4444

## 交互 5: 评价数据加载失败

Given: 学生尝试查看不存在的提交物（如 submissionId=999）
When:
1. 路由导航到 /student/grades/999
2. evaluation API 返回 1001 错误 "提交物不存在或已被删除"

Then:
1. Loading 状态退出
2. 页面全页展示 ErrorState: error icon + "提交物不存在或已被删除" + "返回仪表盘"按钮（type=primary）
3. PageHeader 展示骨架（灰色脉冲矩形替代标题）
4. 所有业务区域不渲染
5. 用户点击"返回仪表盘"→ router.push("/student/dashboard")
6. 用户点击浏览器返回按钮 → 回到上一页（Dashboard 或 Task List）

## 交互 6: 教师未填写文字评语

Given: 学生查看 id=105 的提交物，教师已完成评分但未填写文字评语
When:
1. 进入 /student/grades/105，获取到 Mock 示例 6（evaluation: null）

Then:
1. 所有其他区域正常渲染（AIScorePanel、ScoreComparisonChart、Final Score）
2. 教师评价区域 Section Header "教师评价"正常展示
3. meta 信息展示: "评分教师: 王老师 | 评分时间: 2026-07-02 14:00"
4. 评语内容区域展示 EmptyState: "教师暂未填写文字评价"（不带 actionButton，学生无权操作）

## 交互 7: 轮询期间网络断开

Given: 页面处于 AI Evaluating 状态，正在 3 秒轮询
When:
1. 浏览器网络断开（navigator.onLine 变为 false）
2. 下一次轮询请求因网络错误失败

Then:
1. Navbar 下方展示全局黄色离线提示条
2. AIScorePanel 内 pollingError 设置为 "网络连接已断开"
3. AIScorePanel 展示 ErrorState + "网络连接已断开" + "重新分析"按钮
4. pollingTimer 不清除（网络恢复后可继续轮询）
5. 已用时间计数器停止更新
6. Status Banner 保留当前状态
When: 网络恢复
7. 全局离线提示条消失
8. 用户点击"重新分析"→ pollingError 清空 → 立即发起轮询请求 → 恢复正常轮询流程

---

# Section 8: Permission Design

| 属性 | 值 |
|------|-----|
| 页面权限标识 | student:grades |
| 页面允许角色 | ["student"] |
| 路由权限检查 | meta.roles = ["student"] |

| 权限点 | 类型 | 允许角色 | 说明 |
|--------|------|---------|------|
| 浏览成绩详情 | 页面访问 | student | 学生仅可查看自己提交物的成绩。后端 API 通过当前登录用户 Token 过滤数据，前端不传 userId |
| 查看 AI 分析结果 | 数据可见 | student | AI 分析完成后即可查看 |
| 查看教师评价 | 数据可见 | student | 仅在 COMPLETED / REJECTED 状态可见 |
| "重新分析"按钮 | 操作 | student | AI 分析超时或失败时展示，触发重新轮询 |
| "返回仪表盘"按钮 | 导航 | student | Error 状态展示，返回 /student/dashboard |
| 维度评语"展开详情" | 操作 | student | 查看完整 AI 评语 |

**权限异常处理**:
- 学生尝试查看其他学生的提交物: 后端 API 根据 Token 中的 userId 过滤。若 submissionId 不属于当前学生 → 后端返回 1001"提交物不存在"（不暴露其他学生信息）。前端展示 ErrorState
- 路由守卫 role 校验失败 → 重定向到角色首页
- 未登录用户访问 → 重定向到 /login

**路由守卫特殊逻辑**:
- 路由 beforeEnter 守卫中获取 submissionId 参数
- 验证 submissionId 为有效数字（正则 /^\d+$/），无效则 redirect 到 /student/dashboard
- 此校验为前端防御性编程，后端仍需独立校验

---

# Section 9: Acceptance Criteria

AC-1: Given 学生从 Dashboard 点击一条 COMPLETED 状态的任务 When 进入 /student/grades/{submissionId} Then 页面展示 (a) PageHeader 含任务名称和课程信息；(b) AIScorePanel 展示 6 个维度 AI 评分卡片含评语摘要和色条；(c) 得分对比柱状图展示 AI 评分和教师评分双系列柱子；(d) 教师评价区域展示教师评语文字；(e) 最终成绩卡片展示 COMPLETED 状态标签和最终成绩大数字

AC-2: Given 学生查看一条 AI_EVALUATING 状态的提交物 When 进入成绩详情页 Then 页面展示紫色 AI 分析中横幅（含进度条和已用时间计数器）。AIScorePanel 展示 6 个维度轮询动画列表。每 3 秒调用一次 ai-result 接口。当 ai-result 返回 COMPLETED 后，页面自动刷新为完成态展示完整评分内容

AC-3: Given AI 分析轮询已持续 2 分钟仍未完成 When 轮询超时 Then AIScorePanel 展示"AI 分析超时，请稍后重试"错误提示和"重新分析"按钮。点击"重新分析"重置计时器并重新开始轮询

AC-4: Given 学生查看一条 REJECTED 状态的提交物 When 进入成绩详情页 Then 最终成绩卡片左侧装饰线为红色（#EF4444）。TaskStatusBadge 展示"已退回"状态（红色背景）。教师评价区域包含退回原因和修改要求。最终成绩数字颜色为红色

AC-5: Given 教师已完成评分但未填写文字评语 When 学生进入成绩详情页 Then 教师评价区域 meta 信息正常展示（评分教师和评分时间），但评语内容区展示 EmptyState("教师暂未填写文字评价")。最终成绩区域不受影响正常展示

AC-6: Given 学生尝试查看不存在的提交物（submissionId=999）When 进入成绩详情页 Then 页面展示 ErrorState("提交物不存在或已被删除")和"返回仪表盘"按钮。点击按钮跳转回 /student/dashboard

AC-7: Given AI 分析已完成但教师尚未评分 When 学生进入成绩详情页 Then Status Banner 展示绿色"AI 分析已完成，等待教师评分"。AIScorePanel 展示 AI 分析结果。得分对比柱状图展示（仅 AI 评分系列有数据）。教师评价和最终成绩区域不展示

AC-8: Given 轮询期间浏览器网络断开 When 下一次轮询请求失败 Then AIScorePanel 展示"网络连接已断开"错误提示。Navbar 下方展示黄色全局离线提示条。定时器不清除。网络恢复后用户点击"重新分析"即可继续轮询

---

# Section 10: Files to Create / Modify

## 新建文件

```
├── src/pages/student/GradesPage.vue              # 学生成绩详情页面组件
├── src/components/business/AIScorePanel.vue       # AI 分析结果面板组件（轮询态 + 完成态）
├── src/components/chart/BarChart.vue               # 通用分组柱状图组件（ECharts 封装）
├── src/api/evaluation.ts                           # 评价 API 函数（fetchEvaluation, fetchAIResult）
├── src/stores/useGradesStore.ts                    # 成绩详情 Pinia Store（evaluation 数据 + 轮询状态管理）
├── src/mock/evaluation.ts                          # 评价接口 Mock 数据（evaluation + ai-result 轮询）
├── src/types/evaluation.ts                         # 评价相关类型定义（IEvaluationResponse, IAIResult, ITeacherEvaluation, IFinalScore 等）
└── docs/page-analysis/student-grades.md            # 本文件
```

## 修改文件

```
├── src/router/routes/student.ts                   # 追加 Grades 路由（/student/grades/:submissionId, beforeEnter 守卫）
├── src/router/index.ts                            # 注册 Student Grades 路由
└── src/components/business/TaskStatusBadge.vue    # 确认 7 种状态均实现（含 REJECTED 和 AI_EVALUATING）
```

## 依赖的 Sprint 1 组件（本页面不修改它们，但依赖它们的存在）

```
├── src/components/layout/AppLayout.vue            # 页面外层布局
├── src/components/common/PageHeader.vue            # 页面标题栏
├── src/components/common/LoadingState.vue          # 全页加载骨架
├── src/components/common/ErrorState.vue            # 错误展示（含 retry 按钮）
├── src/components/common/EmptyState.vue            # 空数据展示
├── src/components/common/NoPermission.vue          # 权限不足展示（边界场景）
├── src/components/base/BaseButton.vue               # 按钮组件（重试、返回、展开详情）
├── src/components/business/TaskStatusBadge.vue     # 任务状态标签
├── src/components/chart/BarChart.vue                # 本 Sprint 新建，本页面作为首次使用场景
```

## 注意事项

1. 轮询逻辑集中在 GradesPage 的 Composable `usePolling` 中管理（src/composables/usePolling.ts），不散落在页面组件中。usePolling 接受: pollingFn（轮询函数）、interval（间隔 ms，默认 3000）、timeout（超时 ms，默认 120000）、onCompleted（完成回调）、onError（错误回调）。返回值: { start, stop, reset, isPolling, pollingProgress, currentDimension, pollingError, elapsedSeconds }。
2. GradesPage 的 onUnmounted 必须清除所有定时器（pollingTimer、elapsedTimer、timeoutTimer），防止内存泄漏和页面卸载后的无效 API 请求。
3. evaluation 接口中的 `aiResult.dimensions` 和 `teacherEvaluation.dimensions` 数组顺序必须一致（均按 6 个维度固定顺序排列），否则对比柱状图数据错位。前端做防御性校验：若两数组长度不一致，优先使用 aiResult.dimensions 的顺序和数量，teacherEvaluation.dimensions 缺失的维度填 null。
4. AIScorePanel 维度卡片内的评语截断使用 CSS `display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden;`。点击"展开详情"弹出 ElDialog 展示完整评语。ElDialog 内文字使用 `white-space: pre-wrap; line-height: 1.6;` 保持段落格式和可读性。
5. 最终成绩计算公式（在最终成绩卡片 Tooltip 中展示，不显式展示在卡片表面）: `finalScore = aiScore * 0.4 + teacherScore * 0.6`（后端计算，前端仅展示）。若 teacherScore 为 null，finalScore 为 null。
6. 本页面不涉及 WebSocket。AI 轮询使用 HTTP 短轮询（3s 间隔）。WebSocket 实时通知方案归入 Sprint 4。
