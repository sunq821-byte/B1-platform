# UI Design System v1.0

> 《基于大模型的软件实训教学检查评价与报表系统》
>
> 版本：v1.0 | 日期：2026-06-30 | 状态：正式发布

---

## 目录

1. [设计理念](#1-设计理念design-principles)
2. [Design Tokens](#2-design-tokens)
3. [颜色系统](#3-颜色系统)
4. [字体系统](#4-字体系统)
5. [8pt Spacing System](#5-8pt-spacing-system)
6. [Layout System](#6-layout-system)
7. [Button Design](#7-button-design)
8. [Input Design](#8-input-design)
9. [Card Design](#9-card-design)
10. [Table Design](#10-table-design)
11. [Form Design](#11-form-design)
12. [Navigation Design](#12-navigation-design)
13. [Feedback Design](#13-feedback-design)
14. [AI Components](#14-ai-components)
15. [Chart Design](#15-chart-design)
16. [Icon Design](#16-icon-design)
17. [Motion Design](#17-motion-design)
18. [Accessibility](#18-accessibility)
19. [页面模板](#19-页面模板)
20. [Design Checklist](#20-design-checklist)

---

## 1 设计理念（Design Principles）

### 1.1 设计哲学

本设计系统遵循 **\\"克制的专业主义\\"** 设计哲学：以最小视觉噪音承载最大信息价值。设计服务于内容，而非内容服务于设计。

核心原则：

| 原则 | 说明 |
|---|---|
| **内容优先** | 界面是信息的容器，装饰不应干扰信息获取 |
| **一致性** | 相同功能相同表现，降低用户认知负荷 |
| **效率导向** | B端产品的核心价值是操作效率，每步交互都应有明确目的 |
| **可预测** | 所有交互结果应在用户预期之内，不制造惊喜 |
| **包容性** | 为不同能力、不同设备、不同网络环境的用户提供可用体验 |

### 1.2 为什么采用 AI Native 风格

AI Native 不是视觉风格，而是**交互范式**。传统后台是"人操作机器"，AI Native 后台是"人审核 AI 建议"。

设计体现：

- AI 输出需要区别于用户输入，通过专用卡片、专属颜色、特定排版标识
- AI 给出的结论必须是**可质疑、可覆盖、可追溯**的，因此需要 Confidence 指示器、Reason Block、Teacher Override 机制
- AI 是协作者而非替代者，界面应体现"AI 建议 → 人类决策"的协作流程
- 流式输出（Streaming）是 AI 交互的核心模态，需要专门设计

### 1.3 为什么采用 Apple 极简

Apple 极简不是"少即是多"的简单粗暴，而是**每个元素都有存在的理由**。

设计体现：

- 去除分割线：用留白（Whitespace）而非线条分隔内容
- 扁平层次：最多三层视觉层级（背景 → 卡片 → 内容）
- 统一圆角体系：所有组件共用一套圆角规则
- 精确间距：严格遵循 8pt Grid，不允许"差不多"的间距
- 字体克制：全文仅用一种字体家族，通过字重和字号区分层级

### 1.4 为什么后台需要高信息密度

B端后台用户（教师、教研负责人、管理员）的操作目标是**快速完成任务**，而非浏览内容。

设计体现：

- 一屏展示更多有效信息，减少滚动操作
- Table 采用紧凑行高（40px），但保持可读性
- Dashboard 采用卡片网格布局，一屏展示 4-6 个关键指标
- 表单采用单列或双列布局，标签顶置（Top-aligned Label）以提升扫描效率
- 顶部导航栏固定，不折叠，保证导航始终可见；内容区独占剩余宽度

### 1.5 设计价值观

| 价值观 | 含义 | 反面案例 |
|---|---|---|
| 真实 | 数据准确呈现，不做视觉误导 | 截断坐标轴让差异看起来更大 |
| 即时 | 操作即时反馈，不超过 200ms | 点击后无响应，用户重复点击 |
| 清晰 | 信息层级分明，一眼可辨主次 | 所有文字一样大小、一样颜色 |
| 容错 | 重要操作可撤销，危险操作需确认 | 删除无二次确认 |
| 高效 | 减少操作步数，常用功能一键可达 | 查看成绩需要5步操作 |

---

## 2 Design Tokens

### 2.1 Color Token

| Token | Value | Usage | Description |
|---|---|---|---|
| --color-primary | #3B82F6 | 主按钮、选中态、链接、焦点环 | 品牌主色，蓝色系传达专业与科技感 |
| --color-primary-hover | #2563EB | 主按钮悬停 | 比主色深一级，提供明确交互反馈 |
| --color-primary-light | #EFF6FF | 选中背景、标签底色 | 主色的 10% 透明度等效色 |
| --color-primary-dark | #1D4ED8 | 主按钮按下 | 比主色深两级 |
| --color-secondary | #64748B | 次要按钮、辅助图标 | 石板灰，中性且专业 |
| --color-secondary-hover | #475569 | 次要按钮悬停 | 比次要色深一级 |
| --color-success | #10B981 | 成功状态、通过标记、正向指标 | 翠绿，传达确认与通过感 |
| --color-success-light | #ECFDF5 | 成功标签底色 | 成功色的 10% 透明度等效色 |
| --color-warning | #F59E0B | 警告状态、待审核标记 | 琥珀，传达需要关注 |
| --color-warning-light | #FFFBEB | 警告标签底色 | 警告色的 10% 透明度等效色 |
| --color-danger | #EF4444 | 危险按钮、删除、错误状态、不通过标记 | 红色，传达停止与警示 |
| --color-danger-hover | #DC2626 | 危险按钮悬停 | 比危险色深一级 |
| --color-danger-light | #FEF2F2 | 错误标签底色 | 危险色的 10% 透明度等效色 |
| --color-info | #06B6D4 | 信息提示、帮助文字 | 青色，中性信息传达 |
| --color-info-light | #ECFEFF | 信息标签底色 | 信息色的 10% 透明度等效色 |

### 2.2 Typography Token

| Token | Font Family | Font Size | Font Weight | Line Height | Usage |
|---|---|---|---|---|---|
| --font-display | Inter | 36px | 700 | 1.2 | Dashboard 核心数据（如总分） |
| --font-h1 | Inter | 28px | 600 | 1.3 | 页面标题 |
| --font-h2 | Inter | 22px | 600 | 1.3 | 区块标题、Card 标题 |
| --font-h3 | Inter | 18px | 600 | 1.4 | 子区块标题 |
| --font-title | Inter | 16px | 600 | 1.4 | 列表项标题、Modal 标题 |
| --font-body | Inter | 14px | 400 | 1.5 | 正文、表单标签、表格内容 |
| --font-body-bold | Inter | 14px | 500 | 1.5 | 强调正文、表格表头 |
| --font-caption | Inter | 12px | 400 | 1.5 | 辅助说明、时间戳、次要信息 |
| --font-label | Inter | 12px | 500 | 1.4 | 标签、Badge、Tag |
| --font-button | Inter | 14px | 500 | 1.0 | 按钮文字 |
| --font-code | JetBrains Mono | 13px | 400 | 1.6 | 代码块、代码评分结果 |

### 2.3 Spacing Token

| Token | Value | Usage |
|---|---|---|
| --space-xs | 4px | 图标与文字间距、Tag 内边距 |
| --space-sm | 8px | 组件内部间距、表单行间距 |
| --space-md | 12px | 卡片内边距（紧凑）、列表项间距 |
| --space-lg | 16px | 卡片内边距（标准）、区域间距 |
| --space-xl | 20px | 区块间距、Section 间距 |
| --space-2xl | 24px | 页面内容区 padding、大区块间距 |
| --space-3xl | 32px | Dashboard 卡片间距 |
| --space-4xl | 40px | 页面顶部/底部留白 |
| --space-5xl | 48px | 大区块分隔 |
| --space-6xl | 64px | 极少使用，特大分隔 |
| --space-7xl | 80px | 极少使用 |
| --space-8xl | 96px | 极少使用 |

### 2.4 Radius Token

| Token | Value | Usage |
|---|---|---|
| --radius-none | 0 | 表格、分割线、嵌套卡片内部 |
| --radius-sm | 4px | Tag、Badge、小型标签 |
| --radius-md | 8px | Button、Input、Select、Dropdown |
| --radius-lg | 12px | Card、Panel、Tooltip |
| --radius-xl | 16px | Dialog、Drawer、Modal |
| --radius-full | 9999px | Avatar、圆形按钮、开关 |

### 2.5 Shadow Token

| Token | Value | Usage |
|---|---|---|
| --shadow-none | none | 默认状态、扁平元素 |
| --shadow-sm | 0 1px 2px rgba(0,0,0,0.05) | 卡片默认、Table 行悬浮 |
| --shadow-md | 0 4px 6px -1px rgba(0,0,0,0.07), 0 2px 4px -2px rgba(0,0,0,0.05) | 卡片悬浮、下拉菜单 |
| --shadow-lg | 0 10px 15px -3px rgba(0,0,0,0.08), 0 4px 6px -4px rgba(0,0,0,0.04) | Dialog、Drawer |
| --shadow-xl | 0 20px 25px -5px rgba(0,0,0,0.10), 0 8px 10px -6px rgba(0,0,0,0.05) | 极少使用，超大弹窗 |

### 2.6 Opacity Token

| Token | Value | Usage |
|---|---|---|
| --opacity-disabled | 0.4 | 禁用状态文字和图标 |
| --opacity-disabled-bg | 0.6 | 禁用状态背景 |
| --opacity-hover | 0.8 | 悬浮覆盖层 |
| --opacity-overlay | 0.5 | Dialog/Drawer 背景遮罩 |
| --opacity-skeleton | 0.15 | 骨架屏闪烁层 |

### 2.7 Border Token

| Token | Value | Usage |
|---|---|---|
| --border-width | 1px | 默认边框宽度 |
| --border-width-lg | 2px | Focus 状态、选中态边框 |
| --border-color | #E2E8F0 | 默认边框色 |
| --border-color-hover | #CBD5E1 | 悬停边框色 |
| --border-color-focus | #3B82F6 | 聚焦边框色（Primary） |
| --border-color-error | #EF4444 | 错误边框色 |

### 2.8 Animation Token

| Token | Value | Usage |
|---|---|---|
| --duration-fast | 150ms | 按钮 hover、图标变化 |
| --duration-normal | 200ms | 页面切换、组件显隐、展开收起 |
| --duration-slow | 300ms | Dialog 打开、Drawer 滑入、提示消失 |
| --easing-standard | ease-in-out | 所有过渡动画 |
| --easing-enter | ease-out | 元素进入 |
| --easing-exit | ease-in | 元素退出 |

### 2.9 Z-index Token

| Token | Value | Usage |
|---|---|---|
| --z-dropdown | 1000 | Select 下拉、DatePicker 弹窗 |
| --z-sticky | 1020 | 表头固定、顶部导航栏 |
| --z-overlay | 1040 | Dialog/Drawer 遮罩 |
| --z-modal | 1060 | Dialog 弹窗 |
| --z-popover | 1080 | Popover、Tooltip |
| --z-notification | 1100 | 消息通知、全局提示 |
| --z-max | 9999 | 最高层级（保留） |

---

## 3 颜色系统

### 3.1 Primary 主色系

基于 #3B82F6（蓝色）构建，传达专业、可信、科技感。

| 色阶 | HEX | 使用场景 |
|---|---|---|
| Primary-50 | #EFF6FF | 选中行背景、信息底色 |
| Primary-100 | #DBEAFE | Tag 背景（信息类） |
| Primary-200 | #BFDBFE | 次要强调区域背景 |
| Primary-300 | #93C5FD | 图表辅助色 |
| Primary-400 | #60A5FA | 禁用态的主色元素 |
| **Primary-500** | **#3B82F6** | **主按钮、链接、选中态、品牌色** |
| Primary-600 | #2563EB | 主按钮 Hover |
| Primary-700 | #1D4ED8 | 主按钮 Active |
| Primary-800 | #1E40AF | 深色背景上的主色元素 |
| Primary-900 | #1E3A8A | 极少使用，图表暗色 |

### 3.2 Secondary 次要色系

基于 #64748B（石板灰），用于不重要的操作和辅助元素。

| 色阶 | HEX | 使用场景 |
|---|---|---|
| Secondary-50 | #F8FAFC | 页面背景 |
| Secondary-100 | #F1F5F9 | 表格斑马纹 |
| Secondary-200 | #E2E8F0 | 边框、分割线 |
| Secondary-300 | #CBD5E1 | 禁用态边框 |
| **Secondary-500** | **#64748B** | **次要按钮、占位文字、辅助图标** |
| Secondary-700 | #475569 | 次要按钮 Hover |
| Secondary-900 | #334155 | 次要按钮 Active |

### 3.3 Success 成功色系

基于 #10B981（翠绿），传达通过、完成、正向结果。

| 色阶 | HEX | 使用场景 |
|---|---|---|
| Success-50 | #ECFDF5 | 通过标签背景 |
| Success-100 | #D1FAE5 | 进度条已完成段 |
| **Success-500** | **#10B981** | **成功按钮、通过状态、正向指标** |
| Success-600 | #059669 | 成功按钮 Hover |
| Success-700 | #047857 | 成功按钮 Active |

### 3.4 Warning 警告色系

基于 #F59E0B（琥珀），传达需关注、待处理。

| 色阶 | HEX | 使用场景 |
|---|---|---|
| Warning-50 | #FFFBEB | 警告提示背景 |
| Warning-100 | #FEF3C7 | 待审核标签背景 |
| **Warning-500** | **#F59E0B** | **警告图标、待审核状态、黄色指标** |
| Warning-600 | #D97706 | 警告按钮 Hover |
| Warning-700 | #B45309 | 警告按钮 Active |

### 3.5 Danger 危险色系

基于 #EF4444（红色），传达错误、删除、不通过。

| 色阶 | HEX | 使用场景 |
|---|---|---|
| Danger-50 | #FEF2F2 | 错误提示背景、不通过标签背景 |
| Danger-100 | #FEE2E2 | 删除确认区域背景 |
| **Danger-500** | **#EF4444** | **危险按钮、删除、错误状态、不通过** |
| Danger-600 | #DC2626 | 危险按钮 Hover |
| Danger-700 | #B91C1C | 危险按钮 Active |

### 3.6 Info 信息色系

基于 #06B6D4（青色），传达中性信息。

| 色阶 | HEX | 使用场景 |
|---|---|---|
| Info-50 | #ECFEFF | 信息提示背景 |
| **Info-500** | **#06B6D4** | **信息图标、帮助提示** |

### 3.7 Gray 灰度系统

| 色阶 | HEX | 使用场景 |
|---|---|---|
| Gray-50 | #F9FAFB | 极浅背景 |
| Gray-100 | #F3F4F6 | 浅背景、骨架屏 |
| Gray-200 | #E5E7EB | 边框、分割线 |
| Gray-300 | #D1D5DB | 禁用态边框 |
| Gray-400 | #9CA3AF | 占位文字、禁用文字 |
| Gray-500 | #6B7280 | 辅助文字、次要图标 |
| Gray-600 | #4B5563 | 次要正文 |
| Gray-700 | #374151 | 正文文字 |
| Gray-800 | #1F2937 | 主要文字、标题 |
| Gray-900 | #111827 | 最深文字、高强调 |

### 3.8 语义化颜色 Token

| Token | HEX | 说明 |
|---|---|---|
| --text-primary | #1F2937 (Gray-800) | 主要文字 |
| --text-secondary | #6B7280 (Gray-500) | 次要文字 |
| --text-disabled | #9CA3AF (Gray-400) | 禁用文字 |
| --text-placeholder | #9CA3AF (Gray-400) | 输入框占位文字 |
| --text-link | #3B82F6 (Primary-500) | 链接文字 |
| --bg-page | #F8FAFC (Secondary-50) | 页面背景 |
| --bg-card | #FFFFFF | 卡片背景 |
| --bg-card-hover | #F9FAFB (Gray-50) | 卡片悬浮背景 |
| --bg-selected | #EFF6FF (Primary-50) | 选中态背景 |
| --bg-disabled | #F3F4F6 (Gray-100) | 禁用态背景 |
| --bg-overlay | rgba(15, 23, 42, 0.5) | 遮罩层 |
| --border-default | #E2E8F0 (Secondary-200) | 默认边框 |
| --border-light | #F1F5F9 (Secondary-100) | 浅边框/分割 |
| --divider | #E5E7EB (Gray-200) | 内容分割线 |

---

## 4 字体系统

### 4.1 字体栈

`
font-family: "Inter", "HarmonyOS Sans", "Noto Sans SC", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
`

代码字体：

`
font-family: "JetBrains Mono", "Fira Code", "Consolas", "Monaco", monospace;
`

### 4.2 字体层级

| 层级 | 字号 | 字重 | 行高 | 字间距 | 使用场景 |
|---|---|---|---|---|---|
| **Display** | 36px | 700 | 1.2 (43px) | -0.5px | Dashboard 核心 KPI 数字（如平均分 85.6） |
| **H1** | 28px | 600 | 1.3 (36px) | -0.3px | 页面主标题 |
| **H2** | 22px | 600 | 1.3 (29px) | -0.2px | 区块标题、Card 标题 |
| **H3** | 18px | 600 | 1.4 (25px) | 0 | 子区块标题、Table 组标题 |
| **Title** | 16px | 600 | 1.4 (22px) | 0 | Modal 标题、列表项标题、Form Section 标题 |
| **Body** | 14px | 400 | 1.5 (21px) | 0 | 正文、表单标签、表格内容、描述文字 |
| **Body-Bold** | 14px | 500 | 1.5 (21px) | 0 | 强调正文、表格表头、Tab 标签 |
| **Caption** | 12px | 400 | 1.5 (18px) | 0 | 辅助说明、时间戳、图表标注、次要元数据 |
| **Label** | 12px | 500 | 1.4 (17px) | 0.2px | Badge、Tag、状态标签、按钮组标签 |
| **Button** | 14px | 500 | 1.0 (14px) | 0.2px | 按钮内文字 |
| **Code** | 13px | 400 | 1.6 (21px) | 0 | 代码块、JSON 结果、AI 评分依据中的代码片段 |

### 4.3 字体使用原则

- **同一页面最多 4 个字号层级**：避免字号碎片化
- **强调优先用字重而非颜色**：Body-Bold (500) 优于更大字号或更艳颜色
- **正文不小于 14px**：保证可读性，符合 WCAG AA 标准
- **英文数字用 Tabular Figures**：表格中数字使用等宽数字变体，保证对齐
- **代码块使用 JetBrains Mono**：专为阅读代码设计的等宽字体，辨识度高

### 4.4 行高与段落

| 场景 | 行高 | 段间距 |
|---|---|---|
| 正文段落 | 1.5 (21px) | 16px |
| 列表项 | 1.5 (21px) | 8px |
| 表格内容 | 1.4 (20px) | 0 |
| 标签/Badge | 1.0 | 0 |
| 代码块 | 1.6 (21px) | 0 |

---

## 5 8pt Spacing System

基于 8px 基准网格，所有间距均为 8 的倍数或半倍数（4px），确保视觉节奏统一。

### 5.1 间距阶梯

| Token | 值 | 适用场景 |
|---|---|---|
| 4px | --space-xs | 图标与文字间距、Tag 内边距（水平）、Radio/Checkbox 与标签间距 |
| 8px | --space-sm | 表单行间距、按钮组按钮间距、表格单元格内边距（垂直） |
| 12px | --space-md | 卡片内边距（紧凑模式）、表格单元格内边距（水平） |
| 16px | --space-lg | 卡片内边距（标准模式）、表单区域间距、列表项间距 |
| 20px | --space-xl | 区块间距、Section 与 Section 间距、Table Toolbar 与表格间距 |
| 24px | --space-2xl | 页面内容区 Padding、导航菜单项间距、Dialog 内容 Padding |
| 32px | --space-3xl | Dashboard 卡片间距、Form 大区块间距 |
| 40px | --space-4xl | 页面顶部/底部 Padding |
| 48px | --space-5xl | 页面内主要视觉分区 |
| 64px | --space-6xl | Header 高度（64px）、特殊场景大分隔 |
| 80px | --space-7xl | 极少使用，登录页等特殊布局 |
| 96px | --space-8xl | 极少使用 |

### 5.2 8pt 原则

1. **组件高度为 8 的倍数**：Button 40px、Input 40px、Table Row 40px、Tag 24px
2. **内边距优先使用 8/12/16/24**：卡片 Padding 16px-24px，表单 Padding 16px
3. **外边距优先使用 8/16/24/32**：组件间距 16px-24px，区块间距 24px-32px
4. **4px 仅用于微调**：图标与文字间距、密集型布局微调，不用于组件间距
5. **ICON 尺寸为 8 的倍数**：16px、20px、24px、32px

### 5.3 图标尺寸规范

| Icon Size | 使用场景 |
|---|---|
| 14px | 行内图标（表格操作列图标） |
| 16px | Button 内前置/后置图标、Input 内图标、Tag 内图标 |
| 20px | 导航菜单图标、Card 标题图标 |
| 24px | 页面级图标、Empty 状态图标 |
| 32px | Dashboard 统计卡片图标 |


---

## 6 Layout System

### 6.1 整体布局结构

`
┌─────────────────────────────────────────────────┐
│                   HEADER (64px)                   │
├─────────────────────────────────────────────────┤
│               TOP NAV BAR (52px)                  │
├─────────────────────────────────────────────────┤
│                                                  │
│                    CONTENT                       │
│                  自适应宽度                       │
│                                                  │
├─────────────────────────────────────────────────┤
│                  FOOTER (48px)                    │
└─────────────────────────────────────────────────┘
`

### 6.2 Header

| 属性 | 值 |
|---|---|
| 高度 | 64px |
| 背景色 | #FFFFFF |
| 底部边框 | 1px solid #E2E8F0 |
| 水平 Padding | 24px |
| 内容布局 | 左侧 Logo + 项目名，右侧用户头像/通知/设置 |
| 阴影 | --shadow-sm |
| Z-index | --z-sticky (1020) |
| 位置 | Fixed top |

Logo 区域规范：

- Logo 高度 32px，左侧无额外 margin
- Logo 右侧 12px 间距后为项目名称，使用 Title 字号 (16px, 600)
- 项目名称颜色 Gray-800

右侧操作区规范：

- 通知图标（Bell）+ 未读红点（8px 直径，Danger-500）
- 用户头像（32px 圆形）+ 用户名（Body，Gray-700）
- 元素间距 16px


### 6.3 Top Navigation Bar（顶部导航栏）

| 属性 | 值 |
|---|---|
| 高度 | 52px |
| 背景色 | #FFFFFF |
| 底部边框 | 1px solid #E2E8F0 |
| 水平 Padding | 20px |
| 位置 | Header 下方，Fixed top |
| Z-index | --z-sticky (1020) |
| 内容布局 | 左侧 Logo + 导航链接 + 下拉菜单，右侧搜索/通知/头像 |

导航链接状态：

| 状态 | 背景色 | 文字色 | 字重 |
|---|---|---|---|
| Default | transparent | Gray-600 | 450 |
| Hover | Gray-50 | Gray-800 | 450 |
| Active/Selected | transparent | Primary-500 | 450 |

导航链接规范：

- 导航项 Padding：8px 12px
- 导航项圆角：6px
- 导航项字号：13.5px
- Active 指示器：底部 2px Primary-500 下划线（left:12px, right:12px）
- 导航项间距：2px
- 动画过渡：120ms ease

下拉菜单规范：

- 触发方式：Hover 展开
- 菜单宽度：最小 180px，自适应内容
- 菜单圆角：10px
- 菜单阴影：0 10px 40px rgba(0,0,0,0.08)
- 菜单 Padding：6px
- 分组标题：10.5px、600 字重、Gray-400、全大写、Padding 6px 10px 4px
- 菜单项高度：自动（Padding 7px 10px）
- 菜单项圆角：6px
- 菜单项字号：13px
- 分组间距：4px

Logo 区域规范：

- Logo 图标：28px × 28px，Primary-500 背景，7px 圆角，白色文字
- Logo 文字：15px、600 字重、Gray-900
- Logo 与导航链接间距：20px

右侧操作区规范：

- 搜索快捷键提示：Mono 字体、10.5px、圆角边框
- 通知图标（Bell）+ 未读蓝点（6px 直径，Primary-500）
- 用户头像：30px 圆形、文字首字母
- 元素间距：8px

### 6.4 Content

| 属性 | 值 |
|---|---|
| 背景色 | #F8FAFC |
| Padding | 24px（水平 + 垂直） |
| 最小宽度 | 视口宽度 |
| 最大内容宽度 | 无限制（表格页面自适应），详情页 960px |
| Padding | 24px（水平 + 垂直） |


### 6.5 Footer

| 属性 | 值 |
|---|---|
| 高度 | 48px |
| 背景色 | #FFFFFF |
| 上边框 | 1px solid #E2E8F0 |
| 文字 | Caption (12px)、Gray-400、居中 |
| 内容 | Copyright 信息 |

### 6.6 Drawer

| 属性 | 值 |
|---|---|
| 宽度 | 480px（默认）/ 640px（大）/ 320px（小） |
| 背景色 | #FFFFFF |
| 圆角 | 16px（左侧） |
| Header 高度 | 56px |
| Header Padding | 24px 水平 |
| Body Padding | 24px |
| Footer Padding | 16px 24px |
| 遮罩 | --bg-overlay |
| Z-index | --z-modal (1060) |
| 动画 | 从右侧滑入，300ms ease-out |

### 6.7 Dialog

| 属性 | 值 |
|---|---|
| 宽度 | 480px（默认）/ 640px（大）/ 320px（小） |
| 最大高度 | 80vh |
| 背景色 | #FFFFFF |
| 圆角 | 16px |
| Header Padding | 24px 24px 0 |
| Body Padding | 24px |
| Footer Padding | 16px 24px 24px |
| 遮罩 | --bg-overlay |
| Z-index | --z-modal (1060) |
| 动画 | 缩放 + 淡入，300ms ease-out |

### 6.8 Grid System

采用 12 列 Grid，24px Gutter。

| Breakpoint | 最小宽度 | 列数 | 典型场景 |
|---|---|---|---|
| XS | < 768px | 4 | 移动端（暂不支持） |
| SM | ≥ 768px | 8 | 平板（暂不支持） |
| MD | ≥ 1024px | 12 | 小屏笔记本 |
| LG | ≥ 1280px | 12 | 标准桌面 |
| XL | ≥ 1440px | 12 | 大屏桌面 |
| XXL | ≥ 1920px | 12 | 超宽屏，内容区最大 1600px 居中 |

### 6.9 响应式策略

当前阶段（v1.0）仅支持桌面端（≥1280px），不做移动端适配。最低兼容 1280x720 分辨率。

- 顶部导航栏固定 52px + Header 64px，合计 116px 顶部固定区域
- Content 区域独占剩余宽度
- 表格超出时水平滚动，不压缩列宽
- Dashboard 卡片网格：2列（min 1280px）/ 3列（min 1600px）/ 4列（min 1920px）

---

## 7 Button Design

### 7.1 通用规范

| 属性 | 值 |
|---|---|
| 高度 | 40px（默认）/ 32px（Small）/ 48px（Large） |
| 最小宽度 | 80px |
| 圆角 | 8px |
| 字体 | Button (14px, 500) |
| 内边距 | 水平 16px（默认）/ 12px（Small）/ 20px（Large） |
| 图标尺寸 | 16px |
| 图标与文字间距 | 8px |
| 过渡 | 150ms ease-in-out |
| Focus 环 | 2px solid Primary-500，offset 2px |
| Cursor | pointer（正常）/ not-allowed（禁用） |

### 7.2 Button 类型

#### Primary Button

| 状态 | 背景色 | 文字色 | 边框 |
|---|---|---|---|
| Default | Primary-500 | #FFFFFF | 无 |
| Hover | Primary-600 | #FFFFFF | 无 |
| Active | Primary-700 | #FFFFFF | 无 |
| Focus | Primary-500 | #FFFFFF | 2px Primary-500 offset |
| Loading | Primary-500 | #FFFFFF | 无（图标旋转） |
| Disabled | Gray-100 | Gray-400 | 无 |

适用场景：主操作（提交、保存、确认、下一步），每屏最多一个 Primary Button。

#### Secondary Button

| 状态 | 背景色 | 文字色 | 边框 |
|---|---|---|---|
| Default | #FFFFFF | Gray-700 | 1px #E2E8F0 |
| Hover | Gray-50 | Gray-800 | 1px #CBD5E1 |
| Active | Gray-100 | Gray-800 | 1px #CBD5E1 |
| Focus | #FFFFFF | Gray-700 | 2px Primary-500 offset |
| Disabled | #FFFFFF | Gray-400 | 1px #F1F5F9 |

适用场景：次要操作（取消、返回、重置），与 Primary 配合使用。

#### Ghost Button

| 状态 | 背景色 | 文字色 | 边框 |
|---|---|---|---|
| Default | transparent | Gray-600 | 无 |
| Hover | Gray-50 | Gray-800 | 无 |
| Active | Gray-100 | Gray-800 | 无 |
| Focus | transparent | Gray-600 | 2px Primary-500 offset |
| Disabled | transparent | Gray-400 | 无 |

适用场景：工具栏按钮、表格操作列按钮（图标+文字）、Card 操作区按钮。

#### Danger Button

| 状态 | 背景色 | 文字色 | 边框 |
|---|---|---|---|
| Default | Danger-500 | #FFFFFF | 无 |
| Hover | Danger-600 | #FFFFFF | 无 |
| Active | Danger-700 | #FFFFFF | 无 |
| Disabled | Gray-100 | Gray-400 | 无 |

适用场景：不可逆操作（删除、清空、强制下线）。

#### Text Button

| 状态 | 背景色 | 文字色 | 下划线 |
|---|---|---|---|
| Default | transparent | Primary-500 | 无 |
| Hover | transparent | Primary-600 | 无（背景 Primary-50） |
| Active | transparent | Primary-700 | 无 |
| Disabled | transparent | Gray-400 | 无 |

适用场景：表格内链接跳转、文字级操作（展开/收起），不占据按钮空间。

#### Icon Button

| 属性 | 值 |
|---|---|
| 尺寸 | 40x40px（默认）/ 32x32px（Small）/ 48x48px（Large） |
| 圆角 | 8px |
| 图标尺寸 | 20px |
| 背景 | transparent → Gray-50 (Hover) → Gray-100 (Active) |
| 文字色 | Gray-500 → Gray-700 (Hover) |
| Tooltip | 必须提供（因为无文字标签） |

适用场景：工具栏图标操作（刷新、导出、筛选）。

### 7.3 按钮组

- 水平排列，按钮间距 8px
- Primary 按钮在最右侧（符合阅读流）
- Danger 按钮与常规按钮间距 16px（视觉分组）

### 7.4 Loading 状态

- 显示旋转 Spinner（14px）+ 文字不变
- 按钮宽度不变，防止布局抖动
- 按钮不可点击

---

## 8 Input Design

### 8.1 通用规范

| 属性 | 值 |
|---|---|
| 高度 | 40px（默认）/ 32px（Small）/ 48px（Large） |
| 最小宽度 | 160px |
| 圆角 | 8px |
| 字体 | Body (14px, 400) |
| 内边距（水平） | 12px |
| 边框 | 1px solid #E2E8F0 |
| 背景 | #FFFFFF |
| 文字色 | Gray-800 |
| 占位文字色 | Gray-400 |
| 过渡 | 150ms ease-in-out |

### 8.2 Input 状态

| 状态 | 边框色 | 背景色 | 说明 |
|---|---|---|---|
| Default | #E2E8F0 | #FFFFFF | 初始状态 |
| Hover | #CBD5E1 | #FFFFFF | 鼠标悬浮 |
| Focus | Primary-500 | #FFFFFF | 边框 2px，发蓝光阴影 0 0 0 3px rgba(59,130,246,0.1) |
| Error | Danger-500 | #FEF2F2 | 边框红色，背景极浅红 |
| Disabled | #E2E8F0 | Gray-100 | 文字 Gray-400，不可交互 |
| Readonly | #E2E8F0 | Gray-50 | 文字正常，不可编辑但可选中复制 |

### 8.3 各输入组件规范

#### Input（文本框）

- 单行输入
- 可带前缀/后缀图标（16px，Gray-400）
- 可带清除按钮（x 图标，文字输入后出现）

#### Textarea

| 属性 | 值 |
|---|---|
| 最小高度 | 80px（约 3 行） |
| 最大高度 | 200px（超出滚动） |
| 可拖拽调整 | 仅垂直方向 |
| 字数统计 | 右下角 Caption + Gray-400 |

#### Search

| 属性 | 值 |
|---|---|
| 宽度 | 240px（默认） |
| 前置图标 | Search 图标 16px，Gray-400 |
| 占位文字 | "搜索..." |
| 触发 | Enter 键或输入 300ms 防抖后自动搜索 |
| 清除 | 输入后显示清除按钮 |

#### Password

- 切换显示/隐藏图标（Eye / EyeOff 图标）
- 其他规范同 Input

#### Number

- 右侧步进按钮（上下箭头），仅在聚焦时显示
- 步长默认 1
- 支持 min/max 限制

#### Date / DateRange Picker

| 属性 | 值 |
|---|---|
| 触发 | Input + Calendar 图标 |
| 弹出面板 | 白色背景，12px 圆角，--shadow-md |
| 今日日期 | Primary-500 圆点标记 |
| 选中日期 | Primary-500 背景，白色文字 |
| 范围选择 | 范围区间 Primary-50 背景 |
| 快捷选项 | Footer 区：今天/昨天/本周/本月 |

#### Select

| 属性 | 值 |
|---|---|
| 触发 | Input + ChevronDown 图标（展开时旋转 180°） |
| 弹出面板 | 白色背景，8px 圆角，--shadow-md |
| 选项高度 | 36px |
| 选项 Padding | 水平 12px |
| 选中态 | Primary-50 背景，Primary-500 文字，右侧 Check 图标 |
| 多选 | 选中项显示为 Tag（24px 高，4px 圆角） |
| 搜索 | 面板顶部内嵌搜索框 |
| 空选项 | "暂无数据"（Gray-400 居中） |

#### TreeSelect

- 继承 Select 基本规范
- 展开/折叠箭头 16px
- 缩进层级：每级 24px
- 父子联动：选中父节点可选全部子节点（可配置）

#### Upload

| 属性 | 值 |
|---|---|
| 拖拽区 | 虚线边框 #E2E8F0，背景 Gray-50，160px x 160px |
| Hover | 边框 Primary-500，背景 Primary-50 |
| 图标 | Upload 图标 32px，Gray-400 |
| 文字 | "点击或拖拽文件到此处上传" (Body, Gray-500) |
| 文件列表 | 每项高度 40px，显示文件名 + 大小 + 进度条/状态 |
| 进度条 | 4px 高，Primary-500，8px 圆角 |
| 成功 | CheckCircle 图标 + Success-500 |
| 失败 | XCircle 图标 + Danger-500 + 错误信息 Caption |
| 类型限制提示 | Caption Gray-400："支持 .zip, .pdf, .docx, .jpg, .png，最大 50MB" |

---

## 9 Card Design

### 9.1 通用规范

| 属性 | 值 |
|---|---|
| 背景 | #FFFFFF |
| 圆角 | 12px |
| 阴影（默认） | --shadow-sm |
| 阴影（Hover） | --shadow-md |
| 边框 | 无（默认）/ 1px #E2E8F0（需明确边界时） |
| 过渡 | 200ms ease-in-out |
| Hover 效果 | 仅阴影加深，不做 Scale 变换（Card 承载内容，非按钮） |

### 9.2 Dashboard Card（仪表盘卡片）

| 属性 | 值 |
|---|---|
| Padding | 20px（四边） |
| 最小宽度 | 280px |
| 布局 | 顶部 Title (Body-Bold, Gray-500) + 数值 (Display, Gray-900) + 底部辅助信息 (Caption, Gray-400) |
| 图标 | 可选右上角 24px 图标（Gray-400） |
| 趋势指示 | 数值旁可带 ↑/↓ 箭头 + 百分比（Success-500 / Danger-500, 12px） |

结构：

`
┌──────────────────────────┐
│  实训完成率          ??  │  ← Title + Icon
│                         │
│  92.6%                  │  ← Display 数值
│  ↑ 3.2% 较上月          │  ← Caption 辅助
└──────────────────────────┘
`

### 9.3 Statistics Card（统计卡片）

| 属性 | 值 |
|---|---|
| Padding | 16px（四边） |
| 最小高度 | 120px |
| 布局 | 左侧图标区（40x40 圆角 8px 彩色背景 + 24px 白色图标）+ 右侧数据 |
| 彩色背景 | Primary-50 / Success-50 / Warning-50 / Danger-50 |

结构：

`
┌──────────────────────────────┐
│  ┌──────┐                    │
│  │ ??   │  待审核实训          │
│  │ 蓝底 │  12 项              │
│  └──────┘  含 3 项紧急        │
└──────────────────────────────┘
`

### 9.4 AI Analysis Card（AI 分析卡片）

**本项目核心卡片**。专用于承载 AI 分析结果。

| 属性 | 值 |
|---|---|
| Padding | 20px |
| 背景 | #FFFFFF |
| 左边框 | 4px solid Primary-500（标识 AI 内容） |
| 顶部 | AI 徽标 + 模型名称（Caption, Gray-400）+ Confidence 指示器 |
| 内容区 | AI 分析正文 |
| 底部 | 操作区（采纳/覆盖/重新分析） |
| 动画 | AI 流式输出时，文字逐行出现（300ms 淡入） |

结构：

`
┌──────────────────────────────────────┐
│ ?? AI 分析 · DeepSeek-V3  · 置信度 92% │
│ ──────────────────────────────────── │
│                                      │
│  ?? 评分依据：                        │
│  · 代码规范得分 85/100               │
│  · 功能完成度 90/100                 │
│  · 测试覆盖率 78/100                 │
│                                      │
│  ?? 改进建议：                        │
│  · 补充异常处理逻辑                   │
│  · 增加单元测试覆盖                   │
│                                      │
│  [采纳建议] [教师覆盖] [重新分析]      │
└──────────────────────────────────────┘
`

### 9.5 Task Card（任务卡片）

| 属性 | 值 |
|---|---|
| Padding | 16px |
| 布局 | 标题 + 描述（1行截断）+ 底部元数据（截止日期、状态 Tag、负责人头像） |
| 状态 Tag | 详见 10.8 |
| Hover | 阴影加深 + 标题变 Primary-500（可点击跳转） |

### 9.6 User Card（用户卡片）

| 属性 | 值 |
|---|---|
| Padding | 16px |
| 布局 | 头像（40px 圆形）+ 姓名 + 角色/班级 + 统计数据 |
| 场景 | 学生列表、教师列表中的个人信息卡片 |

### 9.7 Report Card（报表卡片）

| 属性 | 值 |
|---|---|
| Padding | 16px |
| 内容 | 嵌入小型 ECharts（柱状图/折线图），200px 高度 |
| 标题 | 左上角 Body-Bold |
| 操作 | 右上角"查看详情"Text Button |

---

## 10 Table Design

### 10.1 通用规范

| 属性 | 值 |
|---|---|
| 表头背景 | Gray-50 |
| 表头高度 | 44px |
| 表头文字 | Body-Bold (14px, 500), Gray-700 |
| 表头 Padding | 水平 12px |
| 数据行高度 | 40px |
| 数据行文字 | Body (14px, 400), Gray-700 |
| 数据行 Padding | 水平 12px |
| 边框 | 水平分割线 1px #F1F5F9（仅行间，无纵向边框） |
| 斑马纹 | 偶数行背景 Gray-50/FAFAFA（可选） |
| 行 Hover | 背景 Primary-50 |
| 圆角 | 0（表格不设圆角，与容器 Card 形成对比） |

### 10.2 Toolbar

位置：表格上方，与表格间距 16px。

布局：

`
┌─────────────────────────────────────────────┐
│ [新建实训] [批量删除]  │  ?? 搜索... [筛选▼] │
│   左侧操作区           │     右侧筛选区       │
└─────────────────────────────────────────────┘
`

- 左侧：Primary Button + Ghost/Danger Button 组，间距 8px
- 右侧：Search (240px) + Select 筛选器（可选），间距 8px
- Toolbar 不设背景色和外边距，直接位于表格上方

### 10.3 Pagination

| 属性 | 值 |
|---|---|
| 位置 | 表格下方，右对齐，与表格间距 16px |
| 显示 | 总条数 + 每页条数选择 + 页码 + 前后翻页 |
| 每页条数选项 | 10 / 20 / 50 / 100 |
| 默认每页 | 20 |
| 页码按钮 | 32x32px，8px 圆角 |
| 当前页按钮 | Primary-500 背景，白色文字 |
| 其他页按钮 | 白色背景，Gray-700 文字 |
| 快速跳转 | 输入框 + "跳转"文字按钮 |

### 10.4 Sort（排序）

- 表头可排序列显示上下箭头图标（12px，Gray-400）
- 激活排序：箭头变 Primary-500
- 三态切换：无排序 → 升序 → 降序 → 无排序

### 10.5 Filter（筛选）

- 表头筛选图标：Filter 图标 14px，Gray-400
- 激活筛选：图标变 Primary-500，下方显示筛选条件 Tag
- 筛选条件 Tag：显示在 Toolbar 下方，可单独关闭

### 10.6 Selection（选择）

- 复选框：16x16px，4px 圆角
- 选中态：Primary-500 背景 + 白色 Check 图标
- 表头全选：三态（全选 / 部分选中 / 全不选）
- 选中行背景：Primary-50

### 10.7 Empty State

| 属性 | 值 |
|---|---|
| 图标 | Inbox 图标 48px，Gray-300 |
| 文字 | "暂无数据" (Body, Gray-400) |
| 操作 | 可选"新建"按钮（Primary） |

### 10.8 Loading / Skeleton State

**Loading**：表格区域居中显示 Spinner (24px, Primary-500) + "加载中..." (Body, Gray-400)

**Skeleton**：
- 表头正常显示
- 数据区显示 8 行骨架：每行高度 40px，灰色脉冲动画条（Gray-100 → Gray-200）
- 每个单元格骨架宽度为列宽的 60%~90%（随机，模拟内容长短）

### 10.9 操作列规范

- 固定在最右侧（fixed: right）
- 宽度自适应内容（通常 120px~180px）
- 操作项间距 12px
- 操作类型：
  - 主要操作：Text Button（Primary-500），如"查看""审核"
  - 次要操作：Text Button（Gray-500），如"编辑""详情"
  - 危险操作：Text Button（Danger-500），如"删除"
- 超过 3 个操作：前 2 个可见 + "更多▼"Dropdown

### 10.10 状态 Tag 规范

| 状态类型 | Tag 样式 | 使用场景 |
|---|---|---|
| 通过/已完成 | 背景 Success-50，文字 Success-600，圆角 4px | 审核通过、任务完成 |
| 不通过/失败 | 背景 Danger-50，文字 Danger-600，圆角 4px | 审核不通过、提交失败 |
| 待审核/进行中 | 背景 Warning-50，文字 Warning-600，圆角 4px | 待教师审核、实训进行中 |
| 未开始/草稿 | 背景 Gray-100，文字 Gray-500，圆角 4px | 实训未发布、草稿状态 |
| AI 分析中 | 背景 Primary-50，文字 Primary-500，圆角 4px，带 Spinner 图标 | AI 正在分析 |


---

## 11 Form Design

### 11.1 通用规范

| 属性 | 值 |
|---|---|
| 标签位置 | 顶部对齐（Top-aligned Label），扫描效率最高 |
| 标签文字 | Body (14px, 500), Gray-700 |
| 标签与输入框间距 | 8px |
| 表单项垂直间距 | 20px |
| 表单最大宽度 | 640px（单列）/ 960px（双列） |
| 必填标识 | 红色星号 *（Danger-500），标签文字后 4px |
| 帮助文字 | Caption (12px), Gray-400，输入框下方 4px |
| 验证错误 | Caption (12px), Danger-500，输入框下方 4px |

### 11.2 表单布局

#### 单列表单（推荐）

- 适用于创建/编辑详情页、Dialog Form
- 最大宽度 480px
- 标签在上，输入框在下

`
┌──────────────────────┐
│  实训名称 *           │
│  ┌──────────────────┐│
│  │ 请输入实训名称    ││
│  └──────────────────┘│
│                      │
│  截止日期 *           │
│  ┌──────────────────┐│
│  │ 请选择日期  ??   ││
│  └──────────────────┘│
│  帮助文字说明         │
│                      │
│  [取消]  [确认创建]   │
└──────────────────────┘
`

#### 双列表单

- 适用于字段较多的筛选表单、设置页
- 每列最小宽度 280px，列间距 24px
- 相关字段放在同一行

### 11.3 Required（必填）

- 必填字段标签后加红色星号：实训名称 *
- 星号样式：Danger-500，14px，标签文字后 4px
- 提交时未填写的必填字段：输入框 Error 状态 + 错误信息"请输入xxx"

### 11.4 Validation（验证）

| 验证时机 | 说明 |
|---|---|
| On Blur | 失去焦点时验证（推荐） |
| On Change | 每次输入变化验证（密码强度等实时反馈场景） |
| On Submit | 提交时统一验证（兜底） |

验证规则：
- 必填验证："请输入xxx" / "请选择xxx"
- 格式验证："请输入正确的xxx格式"（邮箱、手机号、URL）
- 长度验证："至少x个字符" / "最多x个字符"
- 自定义验证：业务规则提示

### 11.5 Error Message

- 位置：输入框下方 4px
- 样式：Caption（12px），Danger-500
- 图标：AlertCircle 14px，输入框右侧（Error 状态时）
- 输入框 Error 状态：边框 Danger-500 + 背景 #FEF2F2

### 11.6 Submit / Reset Button

| 按钮 | 类型 | 位置 |
|---|---|---|
| 提交按钮 | Primary Button | 表单底部右对齐 |
| 重置按钮 | Secondary Button | 提交按钮左侧，间距 8px |
| 取消按钮 | Ghost/Text Button | 重置按钮左侧（Dialog 中） |

提交状态：

- **正常**：可点击
- **提交中**：Loading 状态 + 文字"提交中..."
- **提交成功**：Success Message 提示 + 关闭 Dialog / 跳转
- **提交失败**：Error Message 提示 + 按钮恢复可点击

### 11.7 Dialog Form

| 属性 | 值 |
|---|---|
| Dialog 宽度 | 480px（小）/ 640px（标准） |
| Header | Title (16px, 600), "新建xxx" / "编辑xxx" |
| Body | 表单内容区，最大高度 60vh 内部滚动 |
| Footer | 取消 + 确定按钮，右对齐 |
| 关闭行为 | 点击遮罩/右上角 X 不关闭（防止误操作丢失数据），仅取消按钮关闭 |

### 11.8 Drawer Form

| 属性 | 值 |
|---|---|
| Drawer 宽度 | 480px（标准）/ 640px（复杂表单） |
| Header | Title (16px, 600) |
| Body | 表单内容区，内部滚动 |
| Footer | 固定在底部，取消 + 确定按钮 |

### 11.9 表单分组

当表单字段超过 8 个时，使用分组提升可读性：

- 分组标题：H3 (18px, 600), Gray-800，与上一组间距 32px，与下一字段间距 16px
- 分组分割：可选水平分割线 1px #F1F5F9

---

## 12 Navigation Design


### 12.1 Top Navigation Bar（顶部导航）

详细规范见 6.3。补充导航逻辑：

- 导航层级：一级菜单（顶部导航链接） → 二级菜单（Hover 下拉面板）
- 下拉面板分组：分组标题 + 菜单项列表
- 当前页面：对应一级或二级菜单项高亮（Active 状态，底部下划线或文字色 Primary-500）
- 菜单展开：Hover 触发下拉面板，无需点击
- 菜单 Badge：右侧显示数字角标（如未审核数），样式：16px 高，Danger-500 背景，白色文字，4px 圆角
- 权限控制：无权限的菜单项不渲染（前端通过 v-if + permission 控制）

位置：Content 区域顶部，Page Header 上方。

| 属性 | 值 |
|---|---|
| 字体 | Caption (12px), Gray-400 |
| 分隔符 | ChevronRight 图标 12px, Gray-300 |
| 当前页 | Gray-700，不可点击 |
| 上级页 | Gray-400，可点击（Hover → Primary-500） |

示例：实训管理 > 软件工程实训 > 学生提交详情

### 12.3 Tabs（标签页）

| 属性 | 值 |
|---|---|
| 位置 | Page Header 下方 |
| Tab 高度 | 40px |
| Tab 内边距 | 水平 16px |
| Tab 文字 | Body (14px, 500) |
| Default | 文字 Gray-500，无边框 |
| Hover | 文字 Gray-700 |
| Active | 文字 Primary-500，底部 2px Primary-500 指示条 |
| Tab 间距 | 0（紧邻排列） |
| 底部边框 | 1px #E2E8F0（整行） |

### 12.4 Page Header

| 属性 | 值 |
|---|---|
| 布局 | Breadcrumb 在上 + Page Title 在下 |
| Page Title | H1 (28px, 600), Gray-900 |
| 可选操作区 | Title 右侧：操作按钮组 |
| 与内容间距 | 24px |

结构：

`
┌──────────────────────────────────────────────┐
│ 实训管理 > 软件工程实训 > 学生提交详情          │  ← Breadcrumb
│                                              │
│ 学生提交详情              [导出] [批量审核]    │  ← Title + Actions
│                                              │
│ ──────────────────────────────────────────── │  ← 内容开始
`

### 12.5 Back Button

| 属性 | 值 |
|---|---|
| 样式 | Ghost Button + ArrowLeft 图标 + 文字 |
| 位置 | Page Title 左侧，或 Page Header 左上角 |
| 行为 | 返回上一页（浏览器 history.back()） |

### 12.6 Page Title 规范

- 列表页：{模块名}管理（如"实训管理"）
- 详情页：{对象名}详情（如"学生提交详情"）
- 创建页：新建{对象名}（如"新建实训"）
- 编辑页：编辑{对象名}（如"编辑实训"）
- AI 分析页：AI 分析：{对象名}（如"AI 分析：张三-软件工程实训"）

---

## 13 Feedback Design

### 13.1 Message（全局提示）

| 属性 | 值 |
|---|---|
| 位置 | 页面顶部居中，距顶 24px |
| 宽度 | 自适应（最大 480px） |
| 高度 | 40px |
| 圆角 | 8px |
| 阴影 | --shadow-md |
| 动画 | 从顶部滑入，300ms ease-out；3s 后淡出消失 |
| 图标 | 16px，左侧 12px |

类型：

| 类型 | 背景色 | 文字色 | 图标 | 图标色 |
|---|---|---|---|---|
| Success | #F0FDF4 | Success-700 | CheckCircle | Success-500 |
| Warning | #FFFBEB | Warning-700 | AlertTriangle | Warning-500 |
| Error | #FEF2F2 | Danger-700 | XCircle | Danger-500 |
| Info | #EFF6FF | Primary-700 | Info | Primary-500 |

### 13.2 Notification（通知提醒）

| 属性 | 值 |
|---|---|
| 位置 | 页面右上角，距顶 16px + Header 64px = 80px |
| 宽度 | 360px |
| 圆角 | 12px |
| 阴影 | --shadow-lg |
| 结构 | 图标 + 标题 + 描述 + 时间 + 关闭按钮 |
| Z-index | --z-notification (1100) |
| 堆叠 | 多条通知向下堆叠，间距 8px |
| 关闭 | 手动关闭 / 10s 自动消失 |

### 13.3 Alert（警告提示）

| 属性 | 值 |
|---|---|
| 位置 | 页面内容区顶部 |
| 宽度 | 100% |
| 圆角 | 8px |
| Padding | 12px 16px |
| 结构 | 图标 + 文字 + 可选操作链接 + 关闭按钮 |
| 边框 | 左边框 3px（对应类型色） |

类型：

| 类型 | 背景色 | 边框色 | 图标 |
|---|---|---|---|
| Info | Primary-50 | Primary-500 | Info |
| Success | Success-50 | Success-500 | CheckCircle |
| Warning | Warning-50 | Warning-500 | AlertTriangle |
| Error | Danger-50 | Danger-500 | AlertCircle |

### 13.4 Confirm（确认对话框）

| 属性 | 值 |
|---|---|
| 类型 | Dialog |
| 宽度 | 400px |
| 结构 | 图标（可选）+ 标题 + 描述 + 取消/确认按钮 |
| 危险确认 | 图标 AlertTriangle (Danger-500)，确认按钮为 Danger Button |
| 按钮文案 | 明确操作："确认删除" 而非 "确定" |

### 13.5 Progress（进度条）

| 属性 | 值 |
|---|---|
| 高度 | 8px（默认）/ 4px（紧凑）/ 16px（带文字） |
| 圆角 | 4px（外）/ 4px（内） |
| 底色 | Gray-100 |
| 填充色 | Primary-500 |
| 成功填充色 | Success-500（100% 时） |
| 百分比文字 | Caption (12px)，进度条右侧 8px，Gray-500 |

适用场景：文件上传进度、实训完成进度、AI 分析进度。

### 13.6 Loading（加载中）

#### 全屏 Loading

- 半透明遮罩 + 居中 Spinner (32px) + "加载中..." (Body, Gray-500)
- 用于页面初始化数据加载

#### 局部 Loading

- 组件区域内居中 Spinner (24px)
- 用于表格刷新、卡片数据加载

#### 按钮 Loading

- 见 7.4

#### Spinner 规范

- 动画：旋转 1s linear infinite
- 颜色：Primary-500
- 尺寸：16px（按钮）/ 24px（局部）/ 32px（全屏）/ 14px（Tag）

### 13.7 Skeleton（骨架屏）

详见 10.8。补充通用 Skeleton 规范：

- 形状：圆角 4px
- 颜色：Gray-100
- 动画：1.5s ease-in-out infinite 脉冲（Gray-100 → Gray-200 → Gray-100）
- 仅用于首次加载，不用于刷新

### 13.8 Empty State（空状态）

| 属性 | 值 |
|---|---|
| 图标 | 48px，Gray-300，与场景相关（Inbox / FileText / Users 等） |
| 标题 | Body-Bold (14px, 500)，Gray-500 |
| 描述 | Caption (12px)，Gray-400 |
| 操作 | 可选 Primary/Secondary Button |
| 间距 | 图标与标题 16px，标题与描述 8px，描述与操作 16px |

### 13.9 异常状态页

#### 404（页面不存在）

`
        ?? (Search 图标 64px, Gray-300)

        页面不存在

        您访问的页面可能已被删除或地址有误

        [返回首页]
`

#### 500（服务器错误）

`
        ?? (AlertTriangle 图标 64px, Warning-500)

        服务器开小差了

        请稍后重试，或联系管理员

        [重新加载]
`

#### 权限不足（403）

`
        ?? (Lock 图标 64px, Gray-300)

        暂无访问权限

        如需访问请联系教研负责人或管理员

        [返回首页]
`

#### 网络异常

`
        ?? (WifiOff 图标 64px, Gray-300)

        网络连接异常

        请检查网络后重试

        [重新连接]
`

#### AI 分析中

`
        ?? (Bot 图标 64px, Primary-500 + 脉冲动画)

        AI 正在分析中...

        预计还需 30 秒，请耐心等待

        (进度条 + 已分析 3/5 项)
`

---

## 14 AI Components（本项目核心）

> AI 组件是本系统的核心交互单元。设计原则：AI 输出必须**可读、可解释、可追溯、可覆盖**。

### 14.1 AI Score Card（AI 评分卡片）

**用途**：展示 AI 对一次实训提交的综合评分。

**布局**：

`
┌─────────────────────────────────────────────┐
│ ?? AI 综合评分           置信度 92%          │
│                                              │
│       ┌───────────┐                          │
│       │           │                          │
│       │    85.6   │   ← Display 数值         │
│       │    /100   │                          │
│       └───────────┘                          │
│                                              │
│  代码规范 ████████████?? 87                  │
│  功能完成 ██████████???? 90                  │
│  测试覆盖 ████████?????? 78                  │
│  文档质量 ███████████??? 85                  │
│                                              │
│  [查看评分详情] [教师覆盖评分]               │
└─────────────────────────────────────────────┘
`

**组成**：
- 总分圆环：Primary-500 环，Gray-100 底色，Display 字体
- 分项得分条：8px 高进度条，4px 圆角，Primary-500 填充
- 置信度：Percentage Badge（详见 14.2）
- 操作：查看详情 Text Button + 教师覆盖 Secondary Button

**交互**：
- Hover 分项进度条 → Tooltip 显示具体扣分原因
- 点击"查看详情"→ 展开 AI Reason Block
- 点击"教师覆盖"→ 打开 Teacher Override Card

**设计理由**：总分 + 分项 + 操作的三层结构，教师可快速判断评分是否合理，并一键进入覆盖流程。

### 14.2 AI Confidence（AI 置信度指示器）

**用途**：直观显示 AI 对本次分析结果的自信程度，帮助教师判断是否需要复核。

**布局**：

`
置信度：████████?? 85% 高
`

**组成**：
- 进度条：8px 高，4px 圆角
- 颜色映射：
  - ≥85%（高置信度）：Success-500
  - 60%-84%（中置信度）：Warning-500
  - <60%（低置信度）：Danger-500
- 文字标注：Caption，"高" / "中" / "低"

**状态**：
| 置信度 | 进度条色 | 标识 | 建议操作 |
|---|---|---|---|
| ≥85% | Success-500 | 绿色 + "高" | 可快速确认 |
| 60%-84% | Warning-500 | 橙色 + "中" | 建议抽查复核 |
| <60% | Danger-500 | 红色 + "低" | 必须教师人工评分 |

**设计理由**：量化 AI 的不确定性，而非隐藏它。教师根据置信度决定审核深度。

### 14.3 AI Suggestion Card（AI 建议卡片）

**用途**：AI 针对提交内容给出的改进建议列表。

**布局**：

`
┌─────────────────────────────────────────────┐
│ ?? AI 改进建议                 共 3 条建议   │
│                                              │
│ ● [代码规范] 第 3 行的变量命名不符合规范      │
│   建议修改为 camelCase 格式，参见 Google       │
│   Java Style Guide。                         │
│                                [采纳] [忽略] │
│                                              │
│ ● [功能实现] 缺少异常处理逻辑                  │
│   建议在文件读取处增加 try-catch。             │
│                                [采纳] [忽略] │
│                                              │
│ ● [测试] 核心方法缺少对应的单元测试            │
│   建议对 calculateScore() 添加测试。           │
│                                [采纳] [忽略] │
└─────────────────────────────────────────────┘
`

**组成**：
- 每条建议：类型标签 + 描述 + 采纳/忽略操作
- 类型标签：Small Tag（代码规范 / 功能实现 / 测试 / 文档 / 安全）
- "采纳" → Text Button (Success-500)
- "忽略" → Text Button (Gray-400)

**交互**：
- 采纳后该条变浅绿背景（Success-50），标记为"已采纳"
- 忽略后该条变灰（Gray-50），标记为"已忽略"
- 教师可批量采纳/忽略

**设计理由**：建议列表而非评分列表，每条建议都是可操作的。教师逐个审核，而非被动接受全部建议。

### 14.4 AI Analysis Timeline（AI 分析时间线）

**用途**：展示多次 AI 分析的演进过程，对比同一学生在不同实训中的进步。

**布局**：

`
○ 2026-06-01  软件工程实训    78.5  →  +5% ↑
│
● 2026-06-15  Web 开发实训    82.3  →  +3% ↑
│
○ 2026-06-28  移动开发实训    86.1  →  +2% ↑
│
○ 当前实训    数据库实训      进行中...
`

**组成**：
- 时间线竖线：Gray-200，2px 宽
- 节点：12px 直径圆形，Gray-300（历史）/ Primary-500（当前选中）/ Primary-500 空心 + 脉冲（进行中）
- 每行：日期 + 实训名 + 分数 + 趋势
- 趋势箭头：↑ Success-500 / ↓ Danger-500 / → Gray-400

**交互**：点击节点跳转对应分析详情。

**设计理由**：时间线展示成长轨迹，将单次评分升华为能力成长曲线，体现教学闭环。

### 14.5 AI Streaming Output（AI 流式输出）

**用途**：AI 逐字输出分析结果时的界面呈现。

**视觉规范**：
- 文字逐行淡入（300ms），模拟真实打字效果
- 输出区底部闪烁光标 ▍（Primary-500，1s 闪烁）
- 已完成文字：Gray-800，正常显示
- 输出中：背景 Primary-50（极浅蓝），右侧 Pulse 动画指示器
- 输出完成：背景恢复白色，指示器变 Success-500 Check

**容器规范**：
- 最大高度 400px，超出滚动
- 自动滚动至底部
- Padding 16px
- 背景 Primary-50（输出中）→ #FFFFFF（完成）

**状态**：
1. **等待中**：Spinner + "AI 正在准备分析..."/Caption
2. **输出中**：文字流式出现 + 光标闪烁 + 背景浅蓝
3. **完成**：文字全部显示 + 绿色勾 + 背景恢复白色
4. **中断**：文字截至中断点 + 黄色警告 + "分析中断，可重新分析"

**设计理由**：流式输出是 LLM 交互的核心体验。视觉上区分"输出中"和"已完成"两个状态，让用户感知进度。

### 14.6 AI Reason Block（AI 推理块）

**用途**：展示 AI 评分背后的推理过程和依据。

**布局**：

`
┌─────────────────────────────────────────────┐
│ ?? AI 推理过程                              │
│                                              │
│ 1. 代码规范分析                              │
│    检测到 3 处不规范命名，2 处缺少注释。      │
│    依据：Google Java Style Guide v1.6        │
│    证据：[查看代码位置 ↗]                    │
│                                              │
│ 2. 功能完成度分析                            │
│    与需求文档对比，核心功能均以实现。          │
│    差异：[查看差异对比 ↗]                    │
│                                              │
│ 3. 测试覆盖率分析                            │
│    AI 估算覆盖率为 78%，主要缺失异常分支测试。 │
│    方法：基于代码路径分析                    │
└─────────────────────────────────────────────┘
`

**组成**：
- 步骤编号 + 标题（Body-Bold）
- 正文描述（Body）
- "依据"（Caption, Gray-400）+ 依据来源
- "证据"（Text Button, Primary-500）+ 跳转链接

**设计理由**：AI 的不透明性是其最大信任障碍。Reason Block 将"黑盒"变为"白盒"，让教师能追溯每个评分的依据。

### 14.7 AI Deduction Card（AI 扣分卡片）

**用途**：逐条展示扣分明细，精确到代码行。

**布局**：

`
┌─────────────────────────────────────────────┐
│ 扣分明细                                    │
│                                              │
│ -2  命名不规范 (src/main/User.java:15)       │
│     变量名 'a' 不符合规范，建议改为 'userAge' │
│     ┌────────────────────────────────────┐  │
│     │ 15 │ int a = user.getAge();        │  │
│     └────────────────────────────────────┘  │
│     规则来源：Google Java Style §5.2         │
│                                              │
│ -5  缺少异常处理 (src/main/FileUtil.java:32)  │
│     文件读取未捕获 IOException               │
│     ┌────────────────────────────────────┐  │
│     │ 32 │ String content = Files.read...│  │
│     └────────────────────────────────────┘  │
│     建议：添加 try-catch 块                  │
└─────────────────────────────────────────────┘
`

**组成**：
- 扣分值（Danger-500, Body-Bold）
- 问题标题（Body）
- 代码展示（Code 字体，Gray-50 背景，4px 圆角，Padding 8px）
- 规则来源 / 建议（Caption, Gray-400）

**设计理由**：精准到代码行 + 代码展示 + 规则来源，让扣分有理有据，教师可直接定位问题。

### 14.8 Teacher Override Card（教师覆盖卡片）

**用途**：教师对 AI 评分进行人工覆盖的交互组件。

**布局**：

`
┌─────────────────────────────────────────────┐
│ ?? 教师覆盖评分                             │
│                                              │
│ AI 评分：85.6    教师评分：[____]            │
│                                              │
│ 覆盖原因：                                   │
│ ┌─────────────────────────────────────────┐ │
│ │ 例如：AI 对学生创新性评分偏低...          │ │
│ └─────────────────────────────────────────┘ │
│                                              │
│ 覆盖范围：                                   │
│ ○ 仅覆盖总分  ● 覆盖所有分项  ○ 自定义      │
│                                              │
│ [取消] [预览] [确认覆盖]                     │
└─────────────────────────────────────────────┘
`

**组成**：
- 原始 AI 评分（只读，Gray-400）
- 教师评分输入（Number Input）
- 覆盖原因（Textarea，必填）
- 覆盖范围（Radio 组）
- 预览按钮（Ghost）+ 确认覆盖（Primary）

**状态**：
- 未覆盖：AI 评分为主
- 已覆盖：显示教师评分 + 覆盖标识 + 教师姓名 + 时间
- 覆盖历史：可查看历次覆盖记录

**设计理由**：AI 是建议者，教师是决策者。覆盖机制保证了"人在回路"（Human-in-the-Loop）。要求填写覆盖原因，便于教研负责人审计。

### 14.9 Prompt Preview（Prompt 预览）

**用途**：向教师/管理员展示当前 AI 评分使用的 Prompt 模板（只读预览，非编辑器）。

**布局**：

`
┌─────────────────────────────────────────────┐
│ ?? 评分策略预览     "代码规范评分标准 v2.1"   │
│                                              │
│ ┌─────────────────────────────────────────┐ │
│ │ ## 评分维度                              │ │
│ │ 1. 代码规范 (30%)                        │ │
│ │    - 命名规范 (10%)                      │ │
│ │    - 注释完整度 (10%)                    │ │
│ │    - 格式规范 (10%)                      │ │
│ │ 2. 功能完成度 (40%)                      │ │
│ │ 3. 测试覆盖率 (20%)                      │ │
│ │ 4. 文档质量 (10%)                        │ │
│ │                                          │ │
│ │ ## 扣分规则                              │ │
│ │ ...                                      │ │
│ └─────────────────────────────────────────┘ │
│                                              │
│                   版本：v2.1 | 更新：2026-06 │
└─────────────────────────────────────────────┘
`

**组成**：
- 策略名称 + 版本
- 只读内容区（Code 字体，Gray-50 背景，内部 scroll）
- 版本 + 更新时间（Caption）

**设计理由**：Prompt 作为评分标准的产品化呈现，不暴露 Prompt 编辑器，而是以"评分策略"的形式展示。教研负责人可在策略管理中配置（非此组件职责）。

### 14.10 JSON Result Viewer（JSON 结果查看器）

**用途**：以结构化方式展示 AI 分析原始 JSON 结果，供技术人员或教研负责人排查。

**布局**：

`
┌─────────────────────────────────────────────┐
│ { } AI 分析原始结果       [展开全部] [复制]  │
│                                              │
│ {                                            │
│   "overall_score": 85.6,                     │
│   "confidence": 0.92,                        │
│   "dimensions": {                            │
│     "code_quality": {                        │
│       "score": 87,                           │
│       "deductions": [...]                    │
│     },                                       │
│     "functionality": { ... }                 │
│   },                                         │
│   "suggestions": [ ... ],                    │
│   "evidence": [ ... ]                        │
│ }                                            │
└─────────────────────────────────────────────┘
`

**组成**：
- Code 字体 (JetBrains Mono, 13px)
- 语法高亮：Key 为 Primary-500，String 为 Success-600，Number 为 Warning-600，Boolean 为 Danger-500
- 默认折叠嵌套超过 3 层的节点
- 顶栏：展开全部 / 复制 操作

**交互**：
- 点击嵌套对象 Key → 展开/折叠
- "复制"→ 复制完整 JSON 到剪贴板 + Success Message

**设计理由**：JSON 是 AI 与系统的通用数据格式。可折叠 + 语法高亮使其在需要时可用，但不占据主要视觉空间。

---

## 15 Chart Design

### 15.1 核心原则

- **数据第一，装饰最后**：去除 3D 效果、渐变填充、多余网格线
- **统一色板**：所有图表使用相同的颜色映射
- **Tooltip 优先于 Legend**：用户更关注数据点详情，Legend 仅辅助
- **坐标轴从 0 开始**：避免视觉误导

### 15.2 图表色板

| 序号 | HEX | 变量 | 用途 |
|---|---|---|---|
| 1 | #3B82F6 | Primary-500 | 主要数据系列 |
| 2 | #10B981 | Success-500 | 第二数据系列 / 正向指标 |
| 3 | #F59E0B | Warning-500 | 第三数据系列 / 需关注 |
| 4 | #EF4444 | Danger-500 | 第四数据系列 / 负向指标 |
| 5 | #8B5CF6 | Purple-500 | 第五数据系列 |
| 6 | #06B6D4 | Info-500 | 第六数据系列 |

超过 6 个数据系列时，使用上述颜色的浅色变体（lighten 30%）。

### 15.3 Legend（图例）

| 属性 | 值 |
|---|---|
| 位置 | 图表下方居中（默认）/ 右侧（数据系列 ≤3 时） |
| 图标 | 圆角矩形 12x8px，4px 圆角 |
| 文字 | Caption (12px), Gray-600 |
| 间距 | 图标与文字 4px，图例项间距 16px |
| 交互 | 点击图例切换数据系列显隐 |

### 15.4 Tooltip（提示框）

| 属性 | 值 |
|---|---|
| 触发 | Hover / Click |
| 背景 | #FFFFFF |
| 边框 | 1px #E2E8F0 |
| 圆角 | 8px |
| 阴影 | --shadow-md |
| Padding | 12px |
| 文字 | Caption (12px) |
| 排序 | 按数值降序 |

### 15.5 Grid（网格线）

| 属性 | 值 |
|---|---|
| 水平线 | 显示（虚线 1px #F1F5F9） |
| 垂直线 | 隐藏（默认）/ 显示（数据密集时） |
| Y 轴分割数 | 4-6 条 |

### 15.6 Axis（坐标轴）

| 属性 | 值 |
|---|---|
| 轴线 | 隐藏 |
| 刻度线 | 隐藏 |
| 刻度标签 | Caption (11px), Gray-400 |
| Y 轴标签 | 左侧，Gray-500 |
| X 轴标签 | 底部，Gray-500（过长自动旋转 30°） |

### 15.7 Label（数据标签）

- 默认不显示（数据密集时标签重叠）
- 仅在数据点 ≤8 时显示
- 样式：Caption (11px), Gray-600，数据点上 8px

### 15.8 各图表类型规范

#### Bar（柱状图）

- 柱宽：自适应，最小 20px
- 柱间距：柱宽的 30%
- 圆角：顶部 4px（左上方 + 右上方）
- 柱子颜色：Primary-500
- 多系列：并排排列，间距 4px

#### Line（折线图）

- 线宽：2px
- 数据点：6px 直径圆，Hover 8px
- 曲线：smooth（monotone）
- 面积：可选半透明填充（opacity 0.1）
- 无数据点：虚线连接，Gray-300

#### Pie（饼图）

- 类型：环形图（半径 70%，内径 50%）
- 中心文字：总计数值（H2 字重 600）+ 标签（Caption Gray-400）
- 标签：外侧引导线 + 标签（百分比 + 名称）
- 少于 3 项不用饼图

#### Radar（雷达图）

- 形状：多边形
- 填充：半透明 Primary-500 (opacity 0.15)
- 描边：Primary-500，2px
- 网格：Gray-200，1px
- 场景：能力画像雷达图

#### Heatmap（热力图）

- 色阶：Gray-50 → Primary-100 → Primary-300 → Primary-500 → Primary-700
- 单元格：圆角 4px，间距 2px
- Tooltip：必须（单元格太小无法直接标注）
- 场景：学生活跃度热力图、提交时间分布

#### WordCloud（词云图）

- 颜色：从图表色板中随机选取（去重后最多 6 色）
- 字体：最小 12px，最大 48px
- 单词间距：4px
- 场景：AI 分析关键词、常见错误词云


---

## 16 Icon Design

### 16.1 图标库

统一使用 **Lucide Icons**，版本跟随最新稳定版。

- 尺寸规范：14px / 16px / 20px / 24px / 32px（见 5.3）
- 描边宽度：统一 2px（默认）
- 颜色：继承当前文字颜色（currentColor）

### 16.2 导航图标

| 场景 | 推荐图标 | 说明 |
|---|---|---|
| 首页 / Dashboard | LayoutDashboard | 仪表盘，标准后台首页图标 |
| 课程管理 | BookOpen | 书本打开，教育语境 |
| 实训管理 | ClipboardList | 任务列表，实训任务管理 |
| 任务中心 | ListTodo | 待办列表，学生任务视图 |
| 成果提交 | Upload | 上传，提交成果入口 |
| AI 分析 | Sparkles | 星芒，AI/Magic 的通用隐喻 |
| 教师审核 | ClipboardCheck | 审核勾选，教师审核场景 |
| 成长中心 | TrendingUp | 趋势上升，能力成长 |
| 评价标准 | Ruler | 尺子，标准/度量 |
| AI 知识库 | Database | 数据库，知识库 |
| 教学分析 | BarChart3 | 柱状图，数据分析 |
| 报表中心 | FileText | 文档，报表 |
| 系统管理 | Settings | 齿轮，系统设置 |
| 运维中心 | Activity | 活动日志，运维监控 |
| 通知中心 | Bell | 铃铛，通知 |

### 16.3 AI 图标

| 场景 | 推荐图标 | 说明 |
|---|---|---|
| AI 评分 | Sparkles | AI 分析入口 |
| AI 置信度 | ShieldCheck | 置信度高 / ShieldAlert 置信度低 |
| AI 建议 | Lightbulb | 灯泡，灵感/建议 |
| AI 推理过程 | Brain | 大脑，推理/思考 |
| AI 流式输出 | Wand2 | 魔棒，AI 生成中 |
| AI 扣分 | ThumbsDown | 拇指向下，扣分/问题 |
| AI 知识库搜索 | Search + Database | 组合使用 |
| AI 模型选择 | Cpu | CPU 芯片，模型 |

### 16.4 用户图标

| 场景 | 推荐图标 | 说明 |
|---|---|---|
| 个人信息 | User | 用户 |
| 学生 | GraduationCap | 学位帽，学生身份 |
| 教师 | UserCheck | 带勾用户，教师/认证身份 |
| 教研负责人 | Users | 用户组，管理者 |
| 管理员 | Shield | 盾牌，管理员权限 |
| 用户管理 | Users | 用户组 |
| 角色管理 | UserCog | 用户+齿轮，角色配置 |
| 退出登录 | LogOut | 退出 |

### 16.5 任务/操作图标

| 场景 | 推荐图标 | 说明 |
|---|---|---|
| 新建/创建 | Plus | 加号 |
| 编辑 | Pencil | 铅笔，编辑 |
| 删除 | Trash2 | 垃圾桶 |
| 查看详情 | Eye | 眼睛，查看 |
| 复制 | Copy | 复制 |
| 下载 | Download | 下载 |
| 导出 | FileDown | 文件下载，导出 |
| 导入 | FileUp | 文件上传，导入 |
| 刷新 | RotateCw | 顺时针旋转 |
| 搜索 | Search | 搜索 |
| 筛选 | Filter | 漏斗，筛选 |
| 排序 | ArrowUpDown | 上下箭头 |
| 更多 | MoreHorizontal | 水平三点 |
| 关闭 | X | 叉号 |
| 返回 | ArrowLeft | 左箭头 |

### 16.6 文件图标

| 场景 | 推荐图标 | 说明 |
|---|---|---|
| 代码文件 | FileCode2 | 尖括号文件 |
| 文档文件 | FileText | 文本文件 |
| PDF 文件 | FileType | 通用文件类型 |
| 图片文件 | FileImage | 图片文件 |
| 压缩包 | FileArchive | 压缩文件 |
| 文件夹 | Folder | 文件夹 |
| Git 仓库 | GitBranch | Git 分支 |

### 16.7 统计/报表图标

| 场景 | 推荐图标 | 说明 |
|---|---|---|
| 柱状图 | BarChart3 | 柱状图 |
| 折线图 | LineChart | 折线图 |
| 饼图 | PieChart | 饼图 |
| 趋势上升 | TrendingUp | 上升趋势 |
| 趋势下降 | TrendingDown | 下降趋势 |
| 统计面板 | Gauge | 仪表盘 |
| 数据表格 | Table | 表格 |
| 导出报表 | FileSpreadsheet | 电子表格 |

### 16.8 日志/状态图标

| 场景 | 推荐图标 | 说明 |
|---|---|---|
| 操作日志 | ScrollText | 卷轴文本，日志 |
| 成功 | CheckCircle2 | 圆形勾选 |
| 失败 | XCircle | 圆形叉号 |
| 警告 | AlertTriangle | 三角警告 |
| 信息 | Info | 圆形 i |
| 帮助 | HelpCircle | 圆形问号 |
| 时钟/历史 | Clock | 时钟 |
| 日历 | Calendar | 日历 |

### 16.9 权限/安全图标

| 场景 | 推荐图标 | 说明 |
|---|---|---|
| 权限 | Shield | 盾牌 |
| 锁定 | Lock | 锁 |
| 解锁 | Unlock | 开锁 |
| 密钥 | Key | 钥匙 |
| 可见 | Eye | 眼睛 |
| 不可见 | EyeOff | 闭眼 |

---

## 17 Motion Design

### 17.1 核心原则

- **动画服务于功能，而非装饰**
- **统一时长 200ms**，保持一致的节奏感
- **统一曲线 ease-in-out**，自然的加速和减速
- **避免**：弹跳、回弹、旋转过多、连续动画链条、自动播放的循环动画

### 17.2 动画时长与曲线

| 动画类型 | 时长 | 曲线 | 说明 |
|---|---|---|---|
| 微交互（Hover、Focus） | 150ms | ease-in-out | 按钮 Hover 变色、图标变色 |
| 标准过渡（显隐、展开、切换） | 200ms | ease-in-out | 组件显示/隐藏、切换 |
| 进入动画 | 200ms | ease-out | 元素进入画面（快进慢停） |
| 退出动画 | 200ms | ease-in | 元素退出画面（慢起快出） |
| 大型面板（Dialog、Drawer） | 300ms | ease-out | 较大面板需要更多时间 |
| Loading 循环 | 1s | linear | Spinner 持续旋转 |

### 17.3 页面切换

- 路由切换：无动画（瞬间切换）
- 理由：B端后台追求效率，页面切换动画增加等待感

### 17.4 Dialog

- 进入：缩放 0.95 → 1 + 透明度 0 → 1，300ms ease-out
- 退出：缩放 1 → 0.95 + 透明度 1 → 0，200ms ease-in
- 遮罩：透明度 0 → 1，200ms ease-out

### 17.5 Drawer

- 进入：从右侧平移 100% → 0，300ms ease-out
- 退出：从 0 → 右侧平移 100%，200ms ease-in
- 遮罩：同 Dialog

### 17.6 Loading / Skeleton

- Spinner：旋转 360°，1s，linear，无限循环
- Skeleton：脉冲透明度 1 → 0.5 → 1，1.5s，ease-in-out，无限循环

### 17.7 Button

- Hover：背景色过渡 150ms ease-in-out
- Active：背景色过渡 + scale 0.98（按下感），150ms ease-in-out
- Loading：Spinner 淡入 + 文字保持

### 17.8 Table / Card

- Table 行 Hover：背景色过渡 150ms
- Table 排序：无动画（即时更新数据）
- Card Hover：阴影过渡 200ms ease-in-out（不做 Scale，Card 不是按钮）
- 统计卡片 Dashboard Card：无 Hover 动画（仅展示数据）

### 17.9 Chart

- 图表初始加载：柱子从 0 生长到目标值，600ms ease-out（一次性，进入视口时触发）
- 图表数据更新：柱子平滑过渡到新值，400ms ease-in-out
- Tooltip：淡入 150ms ease-out

### 17.10 Hover

- 链接/Text Button：文字色过渡 + 可选下划线淡入，150ms
- 图标按钮：背景出现/消失，150ms
- 菜单项：背景色过渡，150ms
- Dropdown 展开：高度 0 → auto + 透明度 0 → 1，200ms ease-out

---

## 18 Accessibility

### 18.1 颜色对比度

遵循 WCAG 2.1 AA 标准。

| 元素 | 要求 | 达标情况 |
|---|---|---|
| 正文文字 (14px) vs 白色背景 | ≥ 4.5:1 | Gray-700 (#374151): 5.9:1 ? |
| 大文字 (≥18px 或 ≥14px Bold) | ≥ 3:1 | Gray-800 (#1F2937): 12.6:1 ? |
| 占位文字 | ≥ 3:1（非必需但建议） | Gray-400 (#9CA3AF): 3.0:1 ??（临界） |
| Primary 按钮白色文字 | ≥ 4.5:1 | #3B82F6 上白色: 4.6:1 ? |
| 焦点环 vs 背景 | ≥ 3:1 | Primary-500 vs 白色: 4.6:1 ? |
| 错误文字 (#EF4444) vs 白色 | ≥ 4.5:1 | 3.9:1 ?? → 实际使用 Danger-600 (#DC2626): 4.6:1 ? |

### 18.2 键盘操作

| 场景 | 键盘操作 |
|---|---|
| Tab 导航 | Tab 键顺序移动焦点，Shift+Tab 反向移动 |
| 按钮 | Enter / Space 激活 |
| 链接 | Enter 激活 |
| 复选框/单选框 | Space 切换 |
| Select 下拉 | Enter 打开，↑↓ 选择，Enter 确认，Escape 关闭 |
| Dialog | Escape 关闭，Tab 在 Dialog 内循环 |
| Table | ↑↓ 切换行，Enter 进入详情（如有） |
| 搜索 | Ctrl+K 聚焦搜索框（全局快捷键） |

### 18.3 Focus 状态

- 可见焦点环：2px solid Primary-500，offset 2px
- 焦点环圆角：继承元素圆角 + 2px
- 所有可交互元素必须有 Focus 样式
- 不使用 outline: none 除非提供替代方案

### 18.4 ARIA 属性

| 组件 | ARIA 属性 | 说明 |
|---|---|---|
| Button | 
ole="button", ria-label (Icon Button 必填) | 标识按钮和可访问名称 |
| Dialog | 
ole="dialog", ria-modal="true", ria-labelledby | 标识对话框 |
| Drawer | 
ole="dialog", ria-modal="true", ria-label | 标识抽屉 |
| Table | 
ole="table", ria-label, ria-sort | 标识表格和排序状态 |
| Tab | 
ole="tablist", 
ole="tab", ria-selected | 标识标签页 |
| Select | 
ole="listbox", 
ole="option", ria-selected | 标识下拉选择 |
| Alert | 
ole="alert" | 屏幕阅读器自动播报 |
| Progress | 
ole="progressbar", ria-valuenow, ria-valuemin, ria-valuemax | 标识进度条 |
| Loading/Spinner | 
ole="status", ria-label="加载中" | 标识加载状态 |
| Breadcrumb | ria-label="面包屑导航" | 导航标识 |
| 表单 | <label> 关联 or + id，ria-required，ria-invalid | 表单可访问性 |

### 18.5 字体大小

- 最小字号 12px（Caption），符合可读性要求
- 正文 14px，符合 B端信息密度需求
- 不使用 px 外的单位（rem 仅用于根字体）
- 用户浏览器缩放（Ctrl+/-）正常运作

### 18.6 其他

- 图片需要 lt 属性（即使为空）
- 仅用颜色传达信息的元素（如状态 Tag）需配合文字
- 表单错误信息需关联 ria-describedby

---

## 19 页面模板

### 19.1 统一页面骨架

所有页面遵循以下骨架结构：

`
┌─────────────────────────────────────────────────┐
│                    HEADER                        │
├─────────────────────────────────────────────────┤
│               TOP NAV BAR                        │
├─────────────────────────────────────────────────┤
│  Breadcrumb                                      │
│  Page Title                         [操作按钮]   │
│  ────────────────────────────────────────────── │
│                                                  │
│                CONTENT AREA                      │
│                                                  │
│                                                  │
├─────────────────────────────────────────────────┤
│                    FOOTER                        │
└─────────────────────────────────────────────────┘

### 19.2 学生首页模板

**目标**：学生登录后第一眼看到与自己相关的实训任务和进度。

`
┌──────────────────────────────────────────────┐
│ Home > 首页                                  │
│ 你好，张三                                   │
│ ─────────────────────────────────────────── │
│                                              │
│ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ │
│ │进行中  │ │待提交  │ │已通过  │ │平均分  │ │
│ │  3 项  │ │  2 项  │ │  8 项  │ │  85.6  │ │
│ └────────┘ └────────┘ └────────┘ └────────┘ │
│                                              │
│ ?? 待完成任务                  [查看全部 →]  │
│ ┌──────────────────────────────────────────┐ │
│ │ 软件工程实训 · 截止 2026-07-05  [提交]   │ │
│ │ Web开发实训 · 截止 2026-07-10     [提交]  │ │
│ └──────────────────────────────────────────┘ │
│                                              │
│ ?? 最近成绩                                  │
│ ┌──────────────────────────────────────────┐ │
│ │ [折线图：近5次实训成绩趋势]               │ │
│ └──────────────────────────────────────────┘ │
│                                              │
│ ?? AI 学习建议                               │
│ ┌──────────────────────────────────────────┐ │
│ │ 基于你的实训表现，建议重点加强：           │ │
│ │ · 异常处理规范                            │ │
│ │ · 单元测试编写                            │ │
│ └──────────────────────────────────────────┘ │
└──────────────────────────────────────────────┘
`

### 19.3 教师首页模板

**目标**：教师快速了解待审核任务和班级整体情况。

`
┌──────────────────────────────────────────────┐
│ Home > 首页                                  │
│ 你好，李老师                                 │
│ ─────────────────────────────────────────── │
│                                              │
│ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ │
│ │待审核  │ │进行中  │ │已完成  │ │班级均分│ │
│ │  12 项 │ │  5 项  │ │  36 项 │ │  78.3  │ │
│ └────────┘ └────────┘ └────────┘ └────────┘ │
│                                              │
│ ? 待审核队列                  [全部 →]      │
│ ┌──────────────────────────────────────────┐ │
│ │ 张三 · 软件工程实训 · 提交于 2小时前      │ │
│ │ AI评分 85.6 · 置信度 92%   [审核]        │ │
│ │ ──────────────────────────────────────── │ │
│ │ 李四 · Web开发实训 · 提交于 5小时前       │ │
│ │ AI评分 72.1 · 置信度 68%   [审核·低置信]  │ │
│ └──────────────────────────────────────────┘ │
│                                              │
│ ?? 班级成绩分布                              │
│ ┌──────────────────────────────────────────┐ │
│ │ [柱状图：各分数段人数分布]                 │ │
│ └──────────────────────────────────────────┘ │
└──────────────────────────────────────────────┘
`

### 19.4 教研负责人首页模板

**目标**：全局教学质量监控和课程优化依据。

`
┌──────────────────────────────────────────────┐
│ Home > 首页                                  │
│ 教学数据总览                                 │
│ ─────────────────────────────────────────── │
│                                              │
│ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ │
│ │总实训  │ │参与学生│ │教师审核│ │AI准确率│ │
│ │ 24 项  │ │ 156 人 │ │ 率 94% │ │ 89.2%  │ │
│ └────────┘ └────────┘ └────────┘ └────────┘ │
│                                              │
│ ?? 教学质量趋势                              │
│ ┌──────────────────────────────────────────┐ │
│ │ [折线图：近6个月分数分布 + 审核率]         │ │
│ └──────────────────────────────────────────┘ │
│                                              │
│ ?? AI 评分 vs 教师评分 偏离分析              │
│ ┌──────────────────────────────────────────┐ │
│ │ [散点图：AI分 vs 教师分，偏离 > 15 标红]   │ │
│ └──────────────────────────────────────────┘ │
│                                              │
│ ?? 需要关注的课程                            │
│ ┌──────────────────────────────────────────┐ │
│ │ 软件工程实训 · 均分下降 8% · 3人未通过    │ │
│ │ 数据库实训 · AI偏差 > 20% · 策略待优化    │ │
│ └──────────────────────────────────────────┘ │
└──────────────────────────────────────────────┘
`

### 19.5 管理员首页模板

**目标**：系统运行状态和用户管理总览。

`
┌──────────────────────────────────────────────┐
│ Home > 首页                                  │
│ 系统总览                                     │
│ ─────────────────────────────────────────── │
│                                              │
│ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ │
│ │总用户  │ │活跃用户│ │AI调用  │ │存储    │ │
│ │ 245 人 │ │ 89 人  │ │ 1.2K次 │ │ 4.8GB  │ │
│ └────────┘ └────────┘ └────────┘ └────────┘ │
│                                              │
│ ??? 系统运行状态                              │
│ ┌──────────────────────────────────────────┐ │
│ │ AI服务 ?? 正常  · 响应时间 320ms          │ │
│ │ 数据库 ?? 正常  · 连接池 12/20            │ │
│ │ 存储   ?? 正常  · 使用率 34%              │ │
│ └──────────────────────────────────────────┘ │
│                                              │
│ ?? 最近操作日志                              │
│ ┌──────────────────────────────────────────┐ │
│ │ [分页表格：时间 · 用户 · 操作 · IP · 结果]  │ │
│ └──────────────────────────────────────────┘ │
└──────────────────────────────────────────────┘
`

### 19.6 详情页模板

`
┌──────────────────────────────────────────────┐
│ 模块 > 对象 > 详情                            │
│ 实训名称：软件工程实训          [编辑] [删除] │
│ ─────────────────────────────────────────── │
│                                              │
│ ┌─ 基本信息 ───────────────────────────────┐ │
│ │ 创建人：李老师    创建时间：2026-06-01    │ │
│ │ 截止日期：2026-07-05  状态：进行中        │ │
│ │ 描述：完成一个完整的 CRUD 应用...         │ │
│ └──────────────────────────────────────────┘ │
│                                              │
│ ┌─ 提交情况 ───────────────────────────────┐ │
│ │ [表格：学生 · 提交时间 · AI评分 · 状态]    │ │
│ └──────────────────────────────────────────┘ │
│                                              │
│ ┌─ 成绩分布 ───────────────────────────────┐ │
│ │ [柱状图]                                  │ │
│ └──────────────────────────────────────────┘ │
└──────────────────────────────────────────────┘
`

### 19.7 表格页模板

`
┌──────────────────────────────────────────────┐
│ 模块 > 列表                                  │
│ 实训管理                        [新建实训]   │
│ ─────────────────────────────────────────── │
│                                              │
│ [批量删除] [导出]  │  ?? 搜索... [状态▼]    │
│                                              │
│ ┌──────────────────────────────────────────┐ │
│ │ 名称 │ 创建人 │ 截止日期 │ 状态 │ 操作   │ │
│ │ ─────────────────────────────────────── │ │
│ │ 软件..│ 李老师 │ 07-05   │ 进行中│ 查看..│ │
│ │ Web.. │ 王老师 │ 07-10   │ 未开始│ 查看..│ │
│ │ ...   │ ...    │ ...     │ ...  │ ...   │ │
│ └──────────────────────────────────────────┘ │
│                         共 24 条  < 1 2 3 >  │
└──────────────────────────────────────────────┘
`

### 19.8 Dashboard 模板

`
┌──────────────────────────────────────────────┐
│ Home > Dashboard                             │
│ 数据总览                                     │
│ ─────────────────────────────────────────── │
│                                              │
│ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐        │
│ │ KPI1 │ │ KPI2 │ │ KPI3 │ │ KPI4 │        │
│ └──────┘ └──────┘ └──────┘ └──────┘        │
│                                              │
│ ┌─────────────────┐ ┌──────────────────────┐ │
│ │  趋势图          │ │  分布图              │ │
│ │  (60% 宽度)      │ │  (40% 宽度)          │ │
│ └─────────────────┘ └──────────────────────┘ │
│                                              │
│ ┌──────────────────────────────────────────┐ │
│ │  详情列表 / 表格（全宽）                   │ │
│ └──────────────────────────────────────────┘ │
└──────────────────────────────────────────────┘
`

### 19.9 AI 分析模板

`
┌──────────────────────────────────────────────┐
│ 实训 > AI 分析：张三-软件工程实训             │
│ AI 分析详情                                   │
│ ─────────────────────────────────────────── │
│                                              │
│ ┌──────────────────┐ ┌─────────────────────┐ │
│ │  AI Score Card    │ │  AI Confidence      │ │
│ │  综合评分 85.6     │ │  置信度 92% 高      │ │
│ └──────────────────┘ └─────────────────────┘ │
│                                              │
│ ┌─ AI 扣分明细 ─────────────────────────────┐ │
│ │ AI Deduction Card × N                     │ │
│ └──────────────────────────────────────────┘ │
│                                              │
│ ┌─ AI 改进建议 ─────────────────────────────┐ │
│ │ AI Suggestion Card                        │ │
│ └──────────────────────────────────────────┘ │
│                                              │
│ ┌─ AI 推理过程 ─────────────────────────────┐ │
│ │ AI Reason Block                           │ │
│ └──────────────────────────────────────────┘ │
│                                              │
│ [教师覆盖评分]  [采纳全部建议]  [重新分析]    │
└──────────────────────────────────────────────┘
`

---

## 20 Design Checklist

开发任何页面/组件前，请逐项检查：

### 颜色与视觉

- □ 是否使用了 Design Token 中的颜色值（而非硬编码 HEX）
- □ 主色使用 #3B82F6（Primary-500）
- □ 状态颜色正确：成功=#10B981 / 警告=#F59E0B / 错误=#EF4444
- □ 文字颜色：主要=Gray-800 / 次要=Gray-500 / 禁用=Gray-400
- □ 无渐变背景
- □ 无玻璃拟态效果
- □ 无霓虹发光效果

### 间距与布局

- □ 所有间距为 8 的倍数（4px 仅用于微调）
- □ 组件高度为 8 的倍数
- □ 页面内容区 Padding = 24px
- □ 导航栏高度 = 52px（顶部导航栏）
- □ Header 高度 = 64px
- □ Card Padding 使用 16px / 20px / 24px

### 字体

- □ 使用 Inter 字体栈（不混用其他字体）
- □ 字号符合层级：Display/H1/H2/H3/Title/Body/Caption/Label
- □ 正文 ≥ 14px
- □ 代码使用 JetBrains Mono
- □ 同一页面字号不超过 4 个层级

### 圆角

- □ Button/Input/Select = 8px
- □ Card = 12px
- □ Dialog/Drawer = 16px
- □ Tag/Badge = 4px

### 动画

- □ 动画时长 200ms（特殊场景 150ms / 300ms）
- □ 动画曲线 ease-in-out
- □ 无弹跳/回弹动画
- □ 无自动播放的循环动画
- □ 无页面切换动画

### 阴影

- □ 使用 Design Token 中的 Shadow 值
- □ 不自定义阴影参数
- □ Card 默认 --shadow-sm，悬浮 --shadow-md

### 组件规范

- □ Button 高度 40px / 32px(Small) / 48px(Large)
- □ Button 圆角 8px
- □ Icon Button 有 Tooltip
- □ Input 有完整的 Default/Hover/Focus/Error/Disabled 状态
- □ Table 行高 40px，表头 44px
- □ Table 操作列固定右侧
- □ Form Label 顶置对齐
- □ Form 必填字段有红色星号
- □ Dialog 宽度 480px/640px
- □ Message 3s 自动消失
- □ Notification 10s 自动消失

### AI 组件规范

- □ AI 内容左边框 4px Primary-500
- □ AI 评分有 Confidence 指示器
- □ AI 建议每条可单独采纳/忽略
- □ AI 扣分精确到代码行
- □ AI 推理过程可展开查看
- □ 教师可覆盖 AI 评分（需填写原因）
- □ AI 流式输出有区分标识（背景色 + 指示器）

### 图表规范

- □ 使用统一图表色板（蓝绿橙红紫青）
- □ 无 3D 效果
- □ 无渐变填充
- □ 坐标轴从 0 开始
- □ Tooltip 背景白色 + 圆角 + 阴影
- □ Legend 位置下方或右侧

### 无障碍

- □ 颜色对比度达标（正文 ≥ 4.5:1）
- □ 所有可交互元素有 Focus 样式
- □ Icon Button 有 aria-label
- □ 表单有 label 关联
- □ 图片有 alt 属性

### 页面模板

- □ 使用统一的页面骨架（Header + Top Nav + Content + Footer）
- □ 有 Breadcrumb 导航
- □ Page Title 规范命名
- □ 空状态使用 Empty 组件

### 图标

- □ 使用 Lucide Icons
- □ 图标尺寸符合场景（16/20/24/32px）
- □ 图标描边 2px
- □ 不混用其他图标库

---

> **本文档版本**：v1.0
>
> **适用范围**：《基于大模型的软件实训教学检查评价与报表系统》全部前端页面
>
> **最后更新**：2026-06-30
>
> **维护者**：UI Design System Working Group
