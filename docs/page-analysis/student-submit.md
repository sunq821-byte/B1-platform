# Page Analysis: 学生提交页 (Student Submit)

**文档版本**: v1.0
**创建日期**: 2026-07-03
**分析范围**: 学生端任务提交页面，包含 Git URL 提交、ZIP 上传、提交历史、确认弹窗完整提交流程

---

# Section 1: Page Identity

| 属性 | 值 |
|------|-----|
| 页面名称 | 学生提交页 |
| 页面文件 | src/pages/student/SubmitPage.vue |
| 路由路径 | /student/submit/:taskId |
| 路由名称 | StudentSubmit |
| 所属 Sprint | Sprint 2 |
| 所属 Feature | F-2-04（学生提交任务） |
| 页面角色 | 学生（Student） |
| 页面复杂度 | L3（复杂表单交互，含 Git 验证、文件上传进度、多 tab、确认弹窗、提交历史） |
| 原型参考 | prototypes/student/submit/ 目录下原型文件 |

页面职责：学生在此页面提交实训任务。支持 Git URL 提交和 ZIP 压缩包上传两种方式。包含 Git 仓库验证、文件上传进度、提交次数统计、确认提交弹窗、历史提交记录等完整功能。

---

# Section 2: Page Layout Structure

学生提交页嵌套在 AppLayout 中，使用 PageHeader + PageContainer 标准布局。页面从上到下分为标题区、任务信息头、提交方式 Tab 切换区、Git URL 表单/ZIP 上传区、公共区域（备注 + 提交次数 + 提交按钮）、提交历史表格区。

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
|  |  |  title: "提交任务"                           | |
|  |  |  breadcrumb: 我的任务 > [taskName] > 提交   | | |
|  |  +------------------------------------------+  | |
|  |                                                | |
|  |  +------------------------------------------+  | |
|  |  |  Task Info Header Bar (mb: --spacing-md)    | |
|  |  |  bg: #FFFFFF, border-radius: --radius-md     | |
|  |  |  padding: --spacing-md                       | |
|  |  |  height: 52px, display: flex, align-items    | | |
|  |  |  justify-content: space-between              | | |
|  |  |                                              | |
|  |  |  +---------------------+  +---------------+  | |
|  |  |  | 任务: [taskName]     |  | 截止时间:      |  | |
|  |  |  | font-weight: 600     |  | [CountdownTimer]|  | |
|  |  |  | font-size: 16px      |  |                |  | |
|  |  |  +---------------------+  +---------------+  | |
|  |  +------------------------------------------+  | |
|  |                                                | |
|  |  +------------------------------------------+  | |
|  |  |  Submit Method Tabs (提交方式切换)           | |
|  |  |  mb: --spacing-md                           |  | |
|  |  |  el-tabs (v-model="activeTab")              | | |
|  |  |  +------------------+---------------------+  | |
|  |  |  | Tab: Git URL     | Tab: ZIP 上传       |  | |
|  |  |  +------------------+---------------------+  | |
|  |  +------------------------------------------+  | |
|  |                                                | |
|  |  +---- Git URL Tab Content (v-if activeTab=0)--+ |
|  |  |  bg: #FFFFFF, border-radius: --radius-md     | |
|  |  |  padding: --spacing-xl, mb: --spacing-md    |  | |
|  |  |                                              | |
|  |  |  [Git URL 输入行]                             | |
|  |  |  BaseInput(v-model="gitUrl") + BaseButton    | | |
|  |  |  placeholder: "请输入 Git 仓库地址"           | |
|  |  |  "验证仓库"按钮 (type="default")              | |
|  |  |                                              | |
|  |  |  [Git 分支输入行]                             | |
|  |  |  BaseInput(v-model="gitBranch",              | | |
|  |  |            placeholder="main")               | |
|  |  |  width: 300px                                | |
|  |  |                                              | |
|  |  |  [Access Token 区 (可折叠)]                   | |
|  |  |  el-collapse / v-if toggle                   | |
|  |  |  BaseInput(v-model="accessToken",            | | |
|  |  |            type="password",                  | |
|  |  |            placeholder="私有仓库需填写")      | |
|  |  |  折叠开关文案: "私有仓库需要 Access Token?"   | |
|  |  |                                              | |
|  |  |  [仓库验证结果展示区 (v-if verifyResult)]     | |
|  |  |  bg: #F0FDF4 (成功) / #FEF2F2 (失败)         | |
|  |  |  padding: --spacing-md                       | |
|  |  |  border-radius: --radius-sm                  | | |
|  |  |  成功: 仓库名 + 默认分支 + 分支数 + 最新提交  | |
|  |  |  失败: 错误原因（8001/8002/8003/8004）       | |
|  |  +----------------------------------------------+ |
|  |                                                | |
|  |  +---- ZIP Upload Tab Content (v-if activeTab=1)--+ |
|  |  |  bg: #FFFFFF, border-radius: --radius-md     | |
|  |  |  padding: --spacing-xl, mb: --spacing-md    |  | |
|  |  |                                              | |
|  |  |  FileUploader 组件                            | |
|  |  |  +----------------------------------------+  | |
|  |  |  |  [拖拽上传区域]                          |  | |
|  |  |  |  border: 2px dashed #CBD5E1              |  | |
|  |  |  |  border-radius: --radius-md              |  | |
|  |  |  |  min-height: 180px                       |  | |
|  |  |  |  display: flex, flex-direction: column  |  | |
|  |  |  |  align-items: center, justify-content   |  | |
|  |  |  |                                          |  | |
|  |  |  |  [UploadCloud 图标 (48px, #94A3B8)]      |  | |
|  |  |  |  [文本: "点击或拖拽文件到此区域上传"]     |  | |
|  |  |  |  [文本: "支持 .zip 格式，最大 50MB"]    |  | |
|  |  |  +----------------------------------------+  | |
|  |  |                                              | |
|  |  |  (文件已选择时切换显示)                       | |
|  |  |  +----------------------------------------+  | |
|  |  |  |  [文件信息条]                            |  | |
|  |  |  |  [FileIcon] [fileName] [fileSize]       |  | |
|  |  |  |  [上传进度条 el-progress]                |  | |
|  |  |  |  [状态文字: 上传中... / 上传成功 / 失败] |  | |
|  |  |  |  [BaseButton "移除" size=small]          |  | |
|  |  |  +----------------------------------------+  | |
|  |  +----------------------------------------------+ |
|  |                                                | |
|  |  +------------------------------------------+  | |
|  |  |  Common Area 公共区域                       | |
|  |  |  bg: #FFFFFF, border-radius: --radius-md     | |
|  |  |  padding: --spacing-xl, mb: --spacing-md    |  | |
|  |  |                                              | |
|  |  |  [备注输入区]                                | |
|  |  |  BaseInput(type="textarea",                  | | |
|  |  |            v-model="remark",                 | |
|  |  |            placeholder="提交备注（选填）",    | |
|  |  |            maxlength=500,                    | |
|  |  |            show-word-limit=true)             | |
|  |  |  rows: 4, resize: vertical                  |  | |
|  |  |                                              | |
|  |  |  [提交次数指示行]                             | |
|  |  |  <span> 已提交 {{ mySubmitCount }} /         | | |
|  |  |          {{ maxSubmitCount }} 次             | |
|  |  |  font-mono class (JetBrains Mono)            | |
|  |  |                                              | |
|  |  |  [确认提交按钮]                               | |
|  |  |  BaseButton(type="primary",                  | | |
|  |  |            size="large",                     | |
|  |  |            disabled: mySubmitCount>=maxSubmitCount | |
|  |  |            || deadline expired,               | |
|  |  |            @click: openSubmitConfirmModal)    | |
|  |  |  innerText: "确认提交"                        | |
|  |  +------------------------------------------+  | |
|  |                                                | |
|  |  +------------------------------------------+  | |
|  |  |  Submission History 提交历史表              | |
|  |  |  (v-if="submissionHistory.length > 0")       | |
|  |  |  bg: #FFFFFF, border-radius: --radius-md     | |
|  |  |  padding: --spacing-xl                       | |
|  |  |                                              | |
|  |  |  Section Title: "提交记录"                   | |
|  |  |  BaseTable: 4 列                             | |
|  |  |  提交时间 | 提交方式 | 状态 | 操作          | |
|  |  |  (无分页, max-height: 300px, overflow-y)     | |
|  |  +------------------------------------------+  | |
|  +----------------------------------------------+ |
+--------------------------------------------------+
```

**区域说明**：

| 区域 | 宽度 | 高度 | 对齐 | 滚动策略 | 说明 |
|------|------|------|------|---------|------|
| TopNav | 100% | 56px | - | 不滚动 | 使用 AppLayout |
| PageContainer | 100% | flex: 1 | - | y轴滚动 | 内容区 |
| PageHeader | max-content(800px)居中 | auto | 左对齐 | 不滚动 | 标题"提交任务"，面包屑含任务名 |
| Task Info Header | max-content(800px)居中 | 52px | flex space-between | 不滚动 | 左侧任务名，右侧倒计时。一行紧凑横幅 |
| Submit Tabs | max-content(800px)居中 | auto | - | 不滚动 | el-tabs 组件。两个 tab 标签宽度均分 |
| Git URL Form | max-content(800px)居中 | auto | 左对齐 | 不滚动 | Git 提交方式的表单区。URL 输入、分支输入、Token、验证结果 |
| ZIP Upload Area | max-content(800px)居中 | auto | 居中 | 不滚动 | 文件拖拽上传区。包含 FileUploader 组件 |
| Common Area | max-content(800px)居中 | auto | 左对齐 | 不滚动 | 备注 + 提交次数 + 确认提交按钮。两个 Tab 共用 |
| Submission History | max-content(800px)居中 | auto (max-height: 300px) | 左对齐 | 表格区域内 y 轴滚动 | 提交历史表格。超过 5 条记录时表格内部滚动 |

**响应式行为**：
- 1920px 分辨率：内容区最大宽度 800px 居中（比列表页和详情页更窄，让提交表单更聚焦）
- 1366px 分辨率：内容区最大宽度 800px 居中
- 小于 768px：内容区 100% 宽度，padding 调整为 --spacing-md。Git URL 输入和验证按钮纵向堆叠。FileUploader 拖拽区高度调整为 140px
- 小于 480px：所有输入框和按钮宽度 100%，纵向排列

---

# Section 3: Component Tree & Specification

## 3.1 组件树

```
SubmitPage.vue（页面组件，嵌套在 AppLayout 中）
├── PageHeader
│   props: { title: "提交任务", breadcrumb: [...], showBack: true }
├── LoadingState（v-if="loading"）
│   props: { text: "正在加载任务信息..." }
├── ErrorState（v-else-if="error"）
│   props: { message: errorMessage, @retry: fetchTaskInfo }
├── EmptyState（v-else-if="!taskInfo"）
│   props: { description: "任务不存在" }
├── <template v-else>
│   ├── Task Info Header Bar（纯 div 布局）
│   │   ├── <span> 任务: {{ taskInfo.taskName }}（font-weight: 600）
│   │   └── CountdownTimer（deadline: taskInfo.deadline, size: "small", showIcon: true）
│   │       @expired → handleDeadlineExpired()
│   ├── <el-tabs v-model="activeTab">
│   │   ├── <el-tab-pane label="Git URL">
│   │   │   ├── <div> Git URL 输入行（flex, gap）
│   │   │   │   ├── BaseInput（v-model="gitForm.url", placeholder="请输入 Git 仓库地址（如 https://github.com/user/repo.git）", clearable）
│   │   │   │   └── BaseButton（type="default", @click: verifyGitRepo, loading: gitVerifying, innerText: "验证仓库"）
│   │   │   ├── <div> Git 分支输入行（mt: --spacing-md）
│   │   │   │   └── BaseInput（v-model="gitForm.branch", placeholder="main", style: width: 300px）
│   │   │   │       下方辅助文字（font-size: --font-size-xs, color: --color-text-placeholder）: "默认分支为 main，可根据需要修改"
│   │   │   ├── <div> Access Token 折叠区（mt: --spacing-md）
│   │   │   │   ├── 折叠开关（BaseButton type="text" 或 <a> 链接样式）: "私有仓库需要 Access Token?" + ChevronDown/ChevronUp 图标
│   │   │   │   └── <div v-if="showAccessToken">
│   │   │   │       └── BaseInput（v-model="gitForm.accessToken", type="password", placeholder="请输入 Access Token", showPassword, size="default"）
│   │   │   └── Git 验证结果区（v-if="gitVerifyResult", mt: --spacing-md）
│   │   │       ├── 验证成功（v-if="gitVerifyResult.valid"）
│   │   │       │   background: #F0FDF4, border: 1px solid #BBF7D0, padding: --spacing-md, border-radius: --radius-sm
│   │   │       │   - CheckCircle 图标 (green) + "仓库验证成功"
│   │   │       │   - 仓库名: {{ gitVerifyResult.repoName }}
│   │   │       │   - 默认分支: {{ gitVerifyResult.defaultBranch }}
│   │   │       │   - 分支列表: {{ gitVerifyResult.branches.join(', ') }}（最多展示 5 个分支名，超出显示"...等 N 个分支"）
│   │   │       │   - 最新提交: {{ gitVerifyResult.latestCommit?.substring(0,7) }} - {{ latestCommitMessage }}（short hash + message）
│   │   │       └── 验证失败（v-else）
│   │   │           background: #FEF2F2, border: 1px solid #FECACA, padding: --spacing-md, border-radius: --radius-sm
│   │   │           - XCircle 图标 (red) + 错误文案映射（依据错误码 8001-8004）
│   │   └── <el-tab-pane label="ZIP 上传">
│   │       └── FileUploader（props 见 3.2.1）
│   │           @upload-success → handleZipUploadSuccess
│   │           @upload-error → handleZipUploadError
│   │           @upload-progress → handleZipUploadProgress
│   │           @file-removed → handleFileRemoved
│   │
│   ├── Common Area（公共区域，两个 Tab 共用）
│   │   ├── <div> 备注输入区
│   │   │   └── el-input（type="textarea", v-model="remark", maxlength="500", show-word-limit, rows="4", resize="vertical", placeholder="提交备注（选填，可补充说明提交内容、修改点等）"）
│   │   ├── <div> 提交次数行（mt: --spacing-md, display: flex, align-items: center, justify-content: space-between）
│   │   │   ├── <span class="font-mono"> 已提交 {{ submitCountInfo.mySubmitCount }} / {{ submitCountInfo.maxSubmitCount }} 次
│   │   │   │   （等宽字体，颜色: submitCountInfo.mySubmitCount >= submitCountInfo.maxSubmitCount → #EF4444，否则 → --color-text-secondary）
│   │   │   └── <span v-if="submitCountInfo.mySubmitCount >= submitCountInfo.maxSubmitCount" style="color: #EF4444"> 已达到最大提交次数
│   │   └── BaseButton（type="primary", size="large", @click: openSubmitConfirmModal, disabled: !canSubmit, style: width: 100%, mt: --spacing-md）
│   │       innerText: submitCountInfo.mySubmitCount > 0 ? "重新提交" : "确认提交"
│   │       disabled tooltip: (a) deadlineExpired → "任务已截止，不能再提交"
│   │                         (b) submitCountExceeded → "已达到最大提交次数（N次）"
│   │                         (c) noFileSelected (ZIP tab) → "请先选择要上传的文件"
│   │                         (d) noGitUrl (Git tab) → "请先输入 Git 仓库地址"
│   │
│   ├── BaseModal（提交确认弹窗，v-model="submitConfirmModalVisible"）
│   │   props: { title: "确认提交", width: "480px", @confirm: handleConfirmSubmit, @cancel: closeSubmitConfirmModal, confirmText: "确认提交", confirmLoading: submitting }
│   │   └── 弹窗内容:
│   │       - 提交方式: {{ activeTab === 0 ? 'Git URL' : 'ZIP 上传' }}
│   │       - 仓库地址 (Git)/ 文件名 (ZIP): {{ submitSummary }}
│   │       - 分支 (Git only): {{ gitForm.branch || 'main' }}
│   │       - 备注: {{ remark || '无' }}
│   │       - 提交次数: 第 {{ submitCountInfo.mySubmitCount + 1 }} 次提交（共 {{ submitCountInfo.maxSubmitCount }} 次）
│   │       - 提示文字（font-size: --font-size-sm, color: --color-text-placeholder）: "提交后将进入 AI 自动分析流程，请确认提交内容无误"
│   │
│   └── Submission History（v-if="submissionHistory.length > 0", mt: --spacing-md）
│       ├── Section Title: "提交记录"（font-size: --font-size-md, font-weight: 600, mb: --spacing-md）
│       └── BaseTable（data: submissionHistory, maxHeight: 300, showPagination: false, size: "small"）
│           4 列:
│           submittedAt (提交时间) | submissionType (提交方式, formatter: Git/ZIP) |
│           status (状态, slot → TaskStatusBadge size=small) |
│           action (操作, slot → BaseButton "查看详情" size=small → /student/grades/:submissionId)
```

## 3.2 本页面需要新建的子组件

### 3.2.1 FileUploader（文件上传组件）

文件路径: src/components/common/FileUploader.vue

组件类型: Common 通用组件（在多个页面复用，包括学生提交、教师上传资料等）

组件职责: 提供文件拖拽上传功能，包含拖拽区、文件信息展示、上传进度条、错误提示、文件移除操作。

**Props 规格**:

| Prop | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| accept | string | 否 | '.zip' | 接受的文件类型（MIME type 或扩展名），多个用逗号分隔。例如 '.zip' 或 '.zip,.rar,.7z' |
| maxSize | number | 否 | 52428800 (50MB) | 最大文件大小（字节） |
| disabled | boolean | 否 | false | 禁用上传功能（如截止时间已过 或 提交次数达上限） |
| uploadAction | Function | 是 | - | 上传执行函数，返回 Promise。函数签名: (file: File) => Promise<UploadResult>。UploadResult: { fileId: number, fileName: string, fileSize: number, fileUrl: string } |
| multiple | boolean | 否 | false | 是否允许多文件上传。当前 MVP 阶段默认 false（一次提交一个 ZIP） |

**Emits 规格**:

| Emit | 参数 | 触发时机 | 说明 |
|------|------|---------|------|
| upload-success | result: UploadResult | 文件上传成功（uploadAction Promise resolve） | 传递上传结果对象给父组件 |
| upload-error | error: { message: string, code?: string } | 文件上传失败（uploadAction Promise reject）或客户端校验失败 | 传递错误信息 |
| upload-progress | progress: number (0-100) | 上传过程中进度更新 | 百分比进度。如果 uploadAction 不提供进度回调，使用模拟进度（0-90% 快速到达，100% 在 resolve 时） |
| file-removed | - | 用户点击"移除"按钮清除已选文件 | 通知父组件文件已被移除 |

**Slots 规格**:

| Slot | Props | 说明 |
|------|-------|------|
| default | - | 自定义拖拽上传区域内容。默认提供 UploadCloud 图标 + 提示文字 |

**Store 依赖**: 无

**API 依赖**: 无（uploadAction 由父组件注入，组件本身不直接调用 API）

**内部状态**:

| State | 类型 | 初始值 | 说明 |
|-------|------|--------|------|
| selectedFile | File \| null | null | 当前选中的文件对象 |
| uploadProgress | number | 0 | 上传进度百分比 |
| uploadStatus | string | 'idle' | 上传状态: 'idle' / 'uploading' / 'success' / 'error' |
| errorMessage | string | '' | 错误信息文案 |
| uploadResult | UploadResult \| null | null | 上传成功的结果 |

**客户端校验逻辑**（在 uploadAction 调用之前执行）:
1. 文件类型校验: 检查文件扩展名是否匹配 accept 属性。不匹配 → emit('upload-error', { message: '文件格式不支持，请上传 ' + accept + ' 格式的文件' })
2. 文件大小校验: 检查 file.size <= maxSize。超限 → emit('upload-error', { message: '文件大小超过限制（最大 ' + formatFileSize(maxSize) + '）' })
3. 校验通过 → uploadStatus = 'uploading', 调用 uploadAction(file)
4. uploadAction 返回的 Promise 可选的包含 onUploadProgress 回调，用于更新进度

**拖拽区域 UI**:
- 默认状态（idle）: 虚线边框区域（2px dashed #CBD5E1），居中展示 UploadCloud 图标（48px, #94A3B8）、"点击或拖拽文件到此区域上传"文字、"支持 {accept} 格式，最大 {maxSizeText}"辅助文字
- 拖拽悬停（dragover）: 虚线边框颜色变为 #3B82F6（--color-primary），背景变为 #EFF6FF。UploadCloud 图标颜色变为 #3B82F6
- 上传中（uploading）: 展示文件信息条 + 进度条（el-progress）+ "上传中..."文字 + "取消"按钮（可选，MVP 阶段简化不做取消）
- 上传成功（success）: 展示文件信息条 + CheckCircle 绿色图标 + "上传成功"文字 + "移除"按钮（点击触发 file-removed）
- 上传失败（error）: 展示文件信息条 + XCircle 红色图标 + 错误信息文字 + "重新上传"按钮（点击重置为 idle 状态）+ "移除"按钮

**样式规范**:
- 拖拽区 min-height: 180px, transition: border-color 0.2s ease, background-color 0.2s ease
- 文件信息条: 背景 #F1F5F9, padding: --spacing-md, border-radius: --radius-sm, display: flex, align-items: center, gap: --spacing-md
- 进度条: 使用 Element Plus el-progress（percentage, stroke-width: 6px, color: #3B82F6）

---

## 3.3 已有组件引用

**引用组件 1: AppLayout（src/components/layout/AppLayout.vue）**
通过 router-view 嵌套，无显式 props

**引用组件 2: PageHeader（src/components/common/PageHeader.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| title | "提交任务" | 页面主标题 |
| breadcrumb | [ { label: '我的任务', to: '/student/tasks' }, { label: taskInfo.taskName, to: '/student/tasks/' + taskId }, { label: '提交' } ] | 面包屑 |
| showBack | true | 展示返回按钮 |

**引用组件 3: PageContainer（src/components/common/PageContainer.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| maxWidth | "800px" | 提交表单使用 800px 最大宽度 |

**引用组件 4: LoadingState（src/components/common/LoadingState.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| text | "正在加载任务信息..." | 加载文案 |

**引用组件 5: ErrorState（src/components/common/ErrorState.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| message | 动态错误文案 | - |
| @retry | fetchTaskInfo | - |

**引用组件 6: EmptyState（src/components/common/EmptyState.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| description | "任务不存在或已被删除" | - |

**引用组件 7: CountdownTimer（src/components/common/CountdownTimer.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| deadline | taskInfo.deadline | 任务截止时间 |
| size | "small" | 小尺寸倒计时，适合在 Header Bar 中展示 |
| showIcon | true | 展示时钟图标 |
| showLabel | false | Header Bar 中不展示"剩余"前缀，只展示时间数字 |
| @expired | handleDeadlineExpired | 截止时间过期回调 |

**引用组件 8: TaskStatusBadge（src/components/common/TaskStatusBadge.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| status | submission.status | 提交记录中的状态值 |
| size | "small" | 表格中使用小尺寸 |

**引用组件 9: BaseTable（src/components/base/BaseTable.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| data | submissionHistory | 提交历史数据 |
| columns | historyColumns | 4 列配置 |
| maxHeight | 300 | 超过 5 行时内部滚动 |
| showPagination | false | 不展示分页 |
| size | "small" | 小尺寸 |
| emptyText | "暂无提交记录" | 空数据文案 |

historyColumns 配置:
```
[
  { prop: 'submittedAt', label: '提交时间', width: '170px', formatter: formatDateTime },
  { prop: 'submissionType', label: '提交方式', width: '100px', align: 'center',
    formatter: (val) => val === 'GIT' ? 'Git 提交' : val === 'ZIP' ? '压缩包' : val },
  { prop: 'status', label: '状态', width: '130px', align: 'center', slot: 'status' },
  { prop: 'action', label: '操作', width: '120px', align: 'center', slot: 'action' }
]
```

**引用组件 10: BaseInput（src/components/base/BaseInput.vue）**

多处使用，见组件树中的各表单输入项

**引用组件 11: BaseButton（src/components/base/BaseButton.vue）**

多处使用，见组件树中的各按钮

**引用组件 12: BaseModal（src/components/base/BaseModal.vue）**

| Prop | 值 | 说明 |
|------|-----|------|
| modelValue | submitConfirmModalVisible | 控制弹窗显示 |
| title | "确认提交" | 弹窗标题 |
| width | "480px" | 弹窗宽度 |
| @confirm | handleConfirmSubmit | 确认提交回调 |
| confirmText | "确认提交" | 确认按钮文案 |
| confirmLoading | submitting | 按钮 loading 状态 |

---

# Section 4: State Design（页面级）

| 状态 | 触发条件 | UI 表现 | 持续时间 | 退出条件 |
|------|---------|--------|---------|---------|
| Loading | 页面挂载，调用 API 获取任务基本信息。loading = true | PageHeader 面包屑正常展示。内容区展示 LoadingState("正在加载任务信息...")。所有表单、历史记录隐藏 | 直到 API 返回（Mock 300ms） | API 返回 success 或 error |
| Empty | API 返回 taskInfo 为 null 或 4004（任务不存在） | PageHeader 面包屑展示。内容区展示 EmptyState("任务不存在或已被删除") + "返回列表"按钮 | 持续直到用户离开 | 用户点击返回按钮 |
| Error | API 请求异常 | PageHeader 面包屑展示。内容区展示 ErrorState + 错误文案 + 重试按钮 | 持续直到重试成功或离开 | 重试成功 → Success |
| Success (Idle) | taskInfo 加载完成，用户尚未进行任何操作。表单区域就绪，等待用户输入 | 完整页面展示: Task Info Header（任务名 + 倒计时）、Tabs（Git URL / ZIP 上传）、FileUploader（idle 状态）、备注输入框、提交次数指示器、确认提交按钮（disabled 直到满足提交条件）、提交历史表格（如有历史记录） | 持续直到用户开始操作 | 用户输入 Git URL / 选择文件 |
| Success (Git Verifying) | 用户在 Git Tab 点击"验证仓库"按钮 |
gitVerifying = true | "验证仓库"按钮进入 loading 状态。Git URL 和分支输入框 disabled。验证结果区隐藏（如果是重新验证）。页面其余部分正常 | 直到 Git 验证 API 返回 | API 返回成功 → 展示验证结果（Success）；返回失败 → 展示验证错误（Form Error） |
| Success (File Uploading) | 用户在 ZIP Tab 选择文件后自动开始上传。uploadStatus = 'uploading' | FileUploader 展示文件信息条 + 进度条 + "上传中..."文字。备注和提交按钮正常展示但提交按钮可能 disabled（等待上传完成） | 直到文件上传 API 返回 | 上传成功 → uploadStatus = 'success'；上传失败 → uploadStatus = 'error' |
| Success (Submitting) | 用户在确认弹窗点击"确认提交"。submitting = true | BaseModal 确认按钮进入 loading 状态（"提交中..."）。弹窗外的页面被遮罩层覆盖不可操作。Modal 不可关闭（closeOnClickModal = false） | 直到提交流程完成（文件上传 + 创建提交记录）或失败 | API 返回成功 → 提交成功状态；返回失败 → Form Error |
| Success (Submit Success) | 提交 API 返回成功（code=0） | 1. 关闭确认弹窗。2. ElMessage.success("任务提交成功，AI 将在稍后进行分析")。3. 重置表单（清空 Git URL、文件选择、备注）。4. 刷新提交历史表格。5. 提交次数 +1。6. 如果达到最大次数，确认提交按钮 disabled。7. 页面保持在当前页，不自动跳转 | 持续直到用户离开或再次提交 | 用户可继续操作（如仍有提交次数） |
| Form Error | 表单校验失败或 API 返回业务错误 | 具体表现因场景而异: (a) Git 验证失败 → 红色错误区域展示错误原因（8001-8004）；(b) 文件上传失败 → FileUploader 展示红色错误信息；(c) 提交失败 → ElMessage.error 展示对应错误文案；(d) 1005 提交次数达上限 → ElMessage.warning("已达到最大提交次数，无法继续提交")；(e) 1006 截止时间已过 → ElMessage.warning("任务已截止，不能再提交") | 直到错误被修正或用户主动处理 | 用户修正输入后重试；或错误提示自动消失（ElMessage 3秒） |
| NoPermission | 路由守卫检测到非 student 角色 | 内容区替换为 NoPermission 组件 | 持续直到切换账号或离开 | 用户点击返回首页 |
| Offline | 网络断开 | PageContainer 顶部黄色离线提示条。现有表单数据保持。验证、上传、提交按钮点击后 API 失败 → 切换到 Form Error | 直到网络恢复 | 网络恢复后黄色提示条消失 |

**确认提交按钮 disabled 逻辑（canSubmit 计算属性）**:

canSubmit 为 true 需要同时满足以下条件:
1. deadline 未过期（deadlineExpired = false）
2. mySubmitCount < maxSubmitCount
3. 当前 Tab 的提交内容已就绪:
   - Git Tab: gitForm.url 非空（不需要必须验证成功，但建议验证）
   - ZIP Tab: uploadedFileId 非空（文件已上传成功）

**deadlineExpired 判断**: 由 CountdownTimer 的 @expired 事件触发。或通过计算属性判断 `new Date(taskInfo.deadline) < Date.now()`

---

# Section 5: Data Flow

## 5.1 页面加载数据流

```
学生从任务详情页点击"去提交"/"重新提交"按钮
→ 路由 /student/submit/:taskId 匹配到 SubmitPage
→ SubmitPage.vue 挂载:
  1. 从 route.params.taskId 获取任务 ID
  2. loading = true
  3. 调用 fetchTaskInfo(taskId):
     - 调用 taskApi.getStudentTaskDetail(taskId)（复用详情页 API，获取 deadline、submitLimit、submissionType 等基本信息）
     - 或调用专用的 GET /api/v1/student/tasks/{taskId}/submit-info（轻量接口，返回提交所需的最小信息集）
  4. Mock 拦截，300ms 后返回数据
  5. 成功:
     - taskInfo.value = response.data
     - loading = false
     - 根据 taskInfo.submissionType 设置 activeTab（GIT → 0, ZIP → 1, ONLINE → 特殊处理）
     - maxSubmitCount = taskInfo.submitLimit || 3
     - mySubmitCount = taskInfo.mySubmitCount || 0
     - 调用 fetchSubmissionHistory(taskId) 加载历史提交记录
     - 渲染页面
  6. 失败:
     - error = true, errorMessage 映射
```

## 5.2 Git 仓库验证数据流

```
Given: 用户在 Git Tab，输入仓库 URL "https://github.com/student/my-project.git"

When: 用户点击"验证仓库"按钮

Then:
1. gitVerifying = true
2. 调用 submitApi.verifyGitRepo(taskId, { gitUrl: gitForm.url, gitBranch: gitForm.branch || 'main', accessToken: gitForm.accessToken || undefined })
3. Axios 发送 POST /api/v1/student/tasks/{taskId}/git-verify
4. Mock 500ms 后返回:
   成功: { valid: true, repoName: "my-project", defaultBranch: "main", branches: ["main", "dev", "feature-1"], latestCommit: { hash: "a1b2c3d4e5f6", message: "Initial commit", author: "student", timestamp: "..." } }
   失败: { valid: false, errorCode: 8001, message: "Git 仓库不存在或无法访问" }
5. gitVerifying = false
6. 展示验证结果:
   成功 → 绿色验证成功区域，显示仓库信息
   失败 → 红色错误区域，展示错误原因（8001-8004 映射）
```

## 5.3 ZIP 文件上传数据流

```
Given: 用户在 ZIP Tab，通过拖拽或点击选择了文件 "project.zip"

When: FileUploader 组件捕获文件选择事件

Then:
1. FileUploader 客户端校验: 扩展名 === '.zip'? 文件大小 <= 50MB?
2. 校验通过 → uploadStatus = 'uploading'
3. emit('upload-progress', 0)
4. 调用 uploadAction(file):
   - 构建 FormData: { file: file }
   - 调用 fileApi.uploadFile(formData, { onUploadProgress: (e) => emit('upload-progress', e.progress) })
   - Axios 发送 POST /api/v1/files/upload (multipart/form-data)
   - Mock 800ms 后返回: { fileId: 55, fileName: "project.zip", fileSize: 10485760, fileUrl: "/files/55/download" }
5. uploadAction Promise resolve:
   - emit('upload-success', result)
   - uploadStatus = 'success'
   - emit('upload-progress', 100)
6. 父组件 SubmitPage 收到 upload-success:
   - uploadedFileId.value = result.fileId
   - uploadedFileName.value = result.fileName
   - uploadedFileSize.value = result.fileSize
   - canSubmit 计算属性重新计算 → true（文件就绪）

Given: 用户选择了 "project.rar"（不支持的格式）

When: FileUploader 校验

Then:
1. 客户端校验: 扩展名 .rar 不在 accept('.zip') 中
2. emit('upload-error', { message: '文件格式不支持，请上传 .zip 格式的文件' })
3. uploadStatus = 'error'
4. FileUploader 展示红色错误信息 + "重新上传"按钮
5. 不调用 uploadAction（不发送 API 请求）

Given: 用户上传超大文件 "large.zip" (60MB)

When: FileUploader 校验

Then:
1. 客户端校验: file.size (62914560) > maxSize (52428800)
2. emit('upload-error', { message: '文件大小超过限制（最大 50MB）' })
3. uploadStatus = 'error'
4. 不调用 uploadAction
```

## 5.4 确认提交数据流

```
Given: 用户在 Git Tab，已输入 URL 并验证通过，备注已填写，mySubmitCount = 0, maxSubmitCount = 3

When: 用户点击"确认提交"按钮

Then:
1. 调用 openSubmitConfirmModal()
2. submitConfirmModalVisible = true
3. BaseModal 弹出:
   - 标题: "确认提交"
   - 内容: 提交方式 Git URL, 仓库地址 https://github.com/..., 分支 main, 备注内容
   - 底部按钮: "取消" + "确认提交"

When: 用户在确认弹窗点击"确认提交"

Then:
1. submitting = true
2. 调用 handleConfirmSubmit():
   - 构建请求体:
     {
       submissionType: activeTab === 0 ? 'GIT' : 'ZIP',
       gitUrl: activeTab === 0 ? gitForm.url : undefined,
       gitBranch: activeTab === 0 ? (gitForm.branch || 'main') : undefined,
       zipFileId: activeTab === 1 ? uploadedFileId : undefined,
       remark: remark || undefined
     }
   - 调用 submitApi.createSubmission(taskId, requestBody)
   - Axios 发送 POST /api/v1/student/tasks/{taskId}/submissions
   - Mock 500ms 后返回:
     成功: { code: 0, message: "提交成功", data: { submissionId: 201, status: 'SUBMITTED', submittedAt: "..." } }
3. submitting = false
4. submitConfirmModalVisible = false（关闭弹窗）
5. ElMessage.success("任务提交成功，AI 将在稍后进行分析")
6. 重置表单:
   - activeTab === Git → gitForm.url = '', gitForm.branch = '', gitForm.accessToken = '', gitVerifyResult = null
   - activeTab === ZIP → uploadedFileId = null, uploadedFileName = '', uploadedFileSize = 0（FileUploader 重置）
   - remark = ''
7. mySubmitCount += 1
8. 调用 fetchSubmissionHistory(taskId) 刷新提交历史表格（新提交记录出现在列表顶部）
9. 如果 mySubmitCount >= maxSubmitCount: 确认提交按钮 disabled, tooltip "已达到最大提交次数（N次）"

Given: 提交失败，错误码 1005（提交次数达上限）

When: API 返回 { code: 1005, message: "已达到最大提交次数", data: null }

Then:
1. submitting = false
2. submitConfirmModalVisible = true（弹窗保持打开，让用户看到错误）
3. ElMessage.warning("已达到最大提交次数，无法继续提交")
4. 确认弹窗的确认按钮恢复为可点击
```

## 5.5 提交历史加载数据流

```
Given: 学生在提交页，之前已提交过 2 次

When: fetchSubmissionHistory(taskId) 被调用（页面加载时 或 提交成功后）

Then:
1. 调用 submitApi.getSubmissionHistory(taskId)
2. Axios 发送 GET /api/v1/student/tasks/{taskId}/submissions（或其他历史接口）
3. Mock 200ms 后返回历史记录数组（按提交时间倒序）
4. submissionHistory.value = response.data
5. 渲染提交历史表格（4 列，TaskStatusBadge 显示每条记录状态）
6. 每条记录的"操作"列有"查看详情"按钮 → /student/grades/:submissionId
```

## 5.6 截止时间已过数据流

```
Given: taskInfo.deadline = "2026-07-01T23:59:59.000Z", 当前时间已超过

When: CountdownTimer @expired 事件触发 → handleDeadlineExpired()

Then:
1. deadlineExpired = true
2. CountdownTimer 显示"已截止"红色文字
3. canSubmit = false（deadline 过期条件不满足）
4. "确认提交"按钮 disabled，tooltip 显示"任务已截止，不能再提交"
5. FileUploader disabled 属性变为 true（不接受新文件选择）
6. Git Tab 的"验证仓库"按钮仍可点击但提交按钮 disabled
7. 如果学生之前已提交过（mySubmitCount > 0），tooltip 显示"任务已截止，不能再提交"
```

---

# Section 6: API & Mock Specification

## 接口 1: GET /api/v1/student/tasks/{taskId}/submit-info

用途: 获取任务提交所需的基本信息（轻量接口，用于提交页初始化）

请求方式: GET

路径参数: taskId (number)

响应数据结构:
```
{
  code: number,
  message: string,
  data: {
    taskId: number,
    taskName: string,        // 任务名称
    deadline: string,        // 截止时间
    submissionType: string,  // 提交方式: GIT / ZIP / ONLINE
    submitLimit: number,     // 最大提交次数
    mySubmitCount: number,   // 当前已提交次数
    maxSubmitCount: number   // 最大提交次数（同 submitLimit）
  } | null
}
```

**Mock 数据示例 1（正常任务，支持 Git 提交，可提交）**:

```
{
  "code": 0,
  "message": "获取成功",
  "success": true,
  "timestamp": "2026-07-03T10:30:00.000Z",
  "traceId": "trace-submit-info-001",
  "data": {
    "taskId": 1,
    "taskName": "Java 基础编程练习",
    "deadline": "2026-07-15T23:59:59.000Z",
    "submissionType": "GIT",
    "submitLimit": 3,
    "mySubmitCount": 1,
    "maxSubmitCount": 3
  }
}
```

**Mock 数据示例 2（ZIP 提交任务，已提交 2 次）**:

```
{
  "code": 0,
  "message": "获取成功",
  "success": true,
  "timestamp": "2026-07-03T10:31:00.000Z",
  "traceId": "trace-submit-info-002",
  "data": {
    "taskId": 3,
    "taskName": "Vue 前端页面开发",
    "deadline": "2026-07-10T23:59:59.000Z",
    "submissionType": "ZIP",
    "submitLimit": 3,
    "mySubmitCount": 2,
    "maxSubmitCount": 3
  }
}
```

## 接口 2: POST /api/v1/student/tasks/{taskId}/submissions

用途: 创建新的任务提交

请求方式: POST

请求 Content-Type: application/json

请求体参数:

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| submissionType | string | 是 | 提交方式: GIT / ZIP / ONLINE | "GIT" |
| gitUrl | string | 条件必填 | Git 仓库地址，submissionType=GIT 时必填 | "https://github.com/student/my-project.git" |
| gitBranch | string | 否 | Git 分支名，默认 main | "main" |
| zipFileId | number | 条件必填 | ZIP 文件 ID，submissionType=ZIP 时必填 | 55 |
| remark | string | 否 | 提交备注，最大 500 字符 | "完成了第一和第二题" |

响应数据结构:
```
{
  code: number,           // 0=成功, 1005=提交次数达上限, 1006=截止时间已过
  message: string,
  data: {
    submissionId: number,
    taskId: number,
    studentId: number,
    submissionType: string,
    gitUrl: string | null,
    gitBranch: string | null,
    zipFileId: number | null,
    remark: string | null,
    status: string,        // SUBMITTED（提交后默认状态）
    submittedAt: string,   // ISO 8601
    submitCount: number    // 本次提交是第几次
  } | null
}
```

**Mock 数据示例 1（Git 提交成功）**:

```
{
  "code": 0,
  "message": "提交成功",
  "success": true,
  "timestamp": "2026-07-03T14:00:00.000Z",
  "traceId": "trace-submit-001",
  "data": {
    "submissionId": 201,
    "taskId": 1,
    "studentId": 1,
    "submissionType": "GIT",
    "gitUrl": "https://github.com/student/my-project.git",
    "gitBranch": "main",
    "zipFileId": null,
    "remark": "完成了第一和第二题",
    "status": "SUBMITTED",
    "submittedAt": "2026-07-03T14:00:00.000Z",
    "submitCount": 2
  }
}
```

**Mock 数据示例 2（ZIP 提交成功）**:

```
{
  "code": 0,
  "message": "提交成功",
  "success": true,
  "timestamp": "2026-07-03T14:05:00.000Z",
  "traceId": "trace-submit-002",
  "data": {
    "submissionId": 202,
    "taskId": 3,
    "studentId": 1,
    "submissionType": "ZIP",
    "gitUrl": null,
    "gitBranch": null,
    "zipFileId": 55,
    "remark": "",
    "status": "SUBMITTED",
    "submittedAt": "2026-07-03T14:05:00.000Z",
    "submitCount": 3
  }
}
```

**Mock 错误场景**:

| 错误场景 | 错误码 | Mock 响应 | UI 表现 |
|---------|--------|----------|--------|
| 提交次数达上限 | 1005 | { "code": 1005, "message": "已达到最大提交次数（3次），无法继续提交", "data": null } | ElMessage.warning, 提交按钮保持 disabled |
| 截止时间已过 | 1006 | { "code": 1006, "message": "任务已截止，不能再提交", "data": null } | ElMessage.warning, 提交按钮保持 disabled |
| 服务端错误 | 5001 | { "code": 5001, "message": "服务器繁忙，请稍后重试", "data": null } | ElMessage.error, 可重新提交 |
| 网络错误 | Network Error | 无响应体 | ElMessage.error("网络连接失败，请检查网络") |

## 接口 3: POST /api/v1/student/tasks/{taskId}/git-verify

用途: 验证 Git 仓库地址的可访问性

请求方式: POST

Content-Type: application/json

请求体参数:

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| gitUrl | string | 是 | Git 仓库地址 | "https://github.com/student/my-project.git" |
| gitBranch | string | 否 | 要验证的分支名，默认 main | "main" |
| accessToken | string | 否 | 私有仓库访问令牌 | "ghp_xxxxxxxxxxxx" |

响应数据结构:
```
{
  code: number,           // 0=成功, 8001-8004=Git 相关错误
  message: string,
  data: {
    valid: boolean,        // 仓库是否有效
    repoName: string,      // 仓库名（有效时返回）
    defaultBranch: string, // 默认分支（有效时返回）
    branches: string[],    // 分支列表（有效时返回）
    latestCommit: {        // 最新提交信息（有效时返回）
      hash: string,        // commit hash（短格式，7位）
      message: string,     // commit message（截取前 80 字符）
      author: string,      // 提交作者
      timestamp: string    // 提交时间 ISO 8601
    } | null
  } | null
}
```

**Mock 数据示例 1（验证成功，公开仓库）**:

```
{
  "code": 0,
  "message": "仓库验证成功",
  "success": true,
  "timestamp": "2026-07-03T13:55:00.000Z",
  "traceId": "trace-git-verify-001",
  "data": {
    "valid": true,
    "repoName": "student/my-project",
    "defaultBranch": "main",
    "branches": ["main", "dev", "feature-login", "feature-api"],
    "latestCommit": {
      "hash": "a1b2c3d",
      "message": "feat: 完成学生成绩管理系统基本功能",
      "author": "student01",
      "timestamp": "2026-07-03T10:00:00.000Z"
    }
  }
}
```

**Mock 数据示例 2（验证失败 - 仓库不存在 8001）**:

```
{
  "code": 8001,
  "message": "Git 仓库不存在或无法访问，请检查地址是否正确",
  "success": false,
  "timestamp": "2026-07-03T13:56:00.000Z",
  "traceId": "trace-git-verify-002",
  "data": {
    "valid": false,
    "repoName": "",
    "defaultBranch": "",
    "branches": [],
    "latestCommit": null
  }
}
```

**Mock 错误码映射**:

| 错误码 | 含义 | Mock 响应 message | UI 错误文案 |
|--------|------|-------------------|-----------|
| 8001 | 仓库不存在或无法访问 | "Git 仓库不存在或无法访问，请检查地址是否正确" | "仓库不存在或无法访问，请检查地址是否正确" |
| 8002 | 认证失败（Token 无效） | "Access Token 无效或已过期" | "Access Token 无效或已过期，请检查后重新输入" |
| 8003 | 仓库为空（无提交） | "仓库为空，请先提交代码后再试" | "仓库为空，请先提交代码到仓库" |
| 8004 | 超时 | "访问仓库超时，请检查网络或稍后重试" | "访问仓库超时，请稍后重试" |

## 接口 4: POST /api/v1/files/upload

用途: 上传文件到服务器（MinIO/S3）

请求方式: POST

Content-Type: multipart/form-data

请求体参数:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File (binary) | 是 | 文件二进制数据（表单字段名 "file"）|

响应数据结构:
```
{
  code: number,
  message: string,
  data: {
    fileId: number,        // 文件 ID（后续提交时使用）
    fileName: string,      // 原始文件名
    fileSize: number,      // 文件大小（字节）
    fileUrl: string        // 文件访问/下载 URL
  } | null
}
```

**Mock 数据示例（上传成功）**:

```
{
  "code": 0,
  "message": "文件上传成功",
  "success": true,
  "timestamp": "2026-07-03T13:50:00.000Z",
  "traceId": "trace-upload-001",
  "data": {
    "fileId": 55,
    "fileName": "project.zip",
    "fileSize": 10485760,
    "fileUrl": "https://storage.example.com/files/55/project.zip"
  }
}
```

**Mock 数据示例（上传失败 - 文件类型错误）**:

```
{
  "code": 7001,
  "message": "不支持的文件格式，仅允许上传 .zip 文件",
  "success": false,
  "timestamp": "2026-07-03T13:51:00.000Z",
  "traceId": "trace-upload-002",
  "data": null
}
```

## 接口 5: GET /api/v1/student/tasks/{taskId}/submissions

用途: 获取当前学生在此任务下的所有提交历史记录

请求方式: GET

路径参数: taskId (number)

响应数据结构:
```
{
  code: number,
  message: string,
  data: Array<{
    submissionId: number,
    taskId: number,
    submissionType: string,  // GIT / ZIP
    status: string,           // SUBMITTED / AI_EVALUATING / AI_COMPLETED / TEACHER_SCORING / COMPLETED / REJECTED
    submittedAt: string,      // ISO 8601
    remark: string | null,
    gitUrl: string | null,
    gitBranch: string | null
  }>
}
```

**Mock 数据示例（有 2 条提交记录）**:

```
{
  "code": 0,
  "message": "获取提交记录成功",
  "success": true,
  "timestamp": "2026-07-03T14:10:00.000Z",
  "traceId": "trace-history-001",
  "data": [
    {
      "submissionId": 202,
      "taskId": 1,
      "submissionType": "GIT",
      "status": "SUBMITTED",
      "submittedAt": "2026-07-03T14:00:00.000Z",
      "remark": "第二次提交，修复了反馈的问题",
      "gitUrl": "https://github.com/student/my-project.git",
      "gitBranch": "main"
    },
    {
      "submissionId": 201,
      "taskId": 1,
      "submissionType": "GIT",
      "status": "COMPLETED",
      "submittedAt": "2026-07-01T10:30:00.000Z",
      "remark": "第一次提交",
      "gitUrl": "https://github.com/student/my-project.git",
      "gitBranch": "main"
    }
  ]
}
```

**Mock 数据示例（无提交记录）**:

```
{
  "code": 0,
  "message": "获取提交记录成功",
  "success": true,
  "timestamp": "2026-07-03T14:10:00.000Z",
  "traceId": "trace-history-002",
  "data": []
}
```

## 接口 6: POST /api/v1/student/tasks/{taskId}/reports

用途: 上传实训报告文件（可选功能，与任务提交关联）

请求方式: POST

Content-Type: multipart/form-data

请求体参数: file (File binary)

响应数据结构:
```
{
  code: number,
  message: string,
  data: {
    reportId: number,
    fileId: number,
    fileName: string,
    fileSize: number,
    fileType: string     // pdf / docx
  } | null
}
```

注意: 此接口在 MVP 阶段可能不作为必选项。如果 MVP 不包含报告上传功能，此接口暂不实现。

## 接口 7: POST /api/v1/student/submissions/{submissionId}/ai-evaluate

用途: 手动触发 AI 分析（提交成功后自动触发，此接口用于手动重新评估）

请求方式: POST

路径参数: submissionId (number)

响应数据结构:
```
{
  code: number,
  message: string,      // "AI 分析任务已提交，请稍后查看结果"
  data: {
    evaluationId: number,
    status: string      // "PENDING"
  } | null
}
```

注意: 正常提交流程中 AI 自动触发，此接口作为备用。提交成功后 student 通常不需要手动调用此接口。

**Mock 延迟汇总**:
- getSubmitInfo: 300ms
- createSubmission: 500ms
- gitVerify: 500ms（模拟网络延迟 + Git 操作处理）
- fileUpload: 800ms（模拟文件传输耗时）
- getSubmissionHistory: 200ms

**Mock 实现位置**:
- src/mock/studentSubmit.ts（所有提交相关 Mock，包括 git-verify、file-upload、create-submission、submission-history）
- src/mock/studentTasks.ts（submit-info 接口，可能复用 task detail Mock）

---

# Section 7: Interaction Flows

## 交互 1: Git URL 方式成功提交流程

Given: 学生从任务详情页点击"去提交"到达 /student/submit/1。taskInfo 加载完成，submissionType=GIT，mySubmitCount=0，maxSubmitCount=3，deadline 未过期

When:
1. 页面默认展示 Git URL Tab（根据 submissionType=GIT 自动选中）
2. 学生在 Git URL 输入框输入 "https://github.com/student/my-project.git"
3. 学生点击"验证仓库"按钮
4. 500ms 后验证结果返回: valid=true, repoName="student/my-project", defaultBranch="main", branches=["main","dev"], latestCommit={hash:"a1b2c3d", message:"feat: ..."}
5. 绿色验证成功区域展示仓库信息
6. 学生在备注输入框输入"完成所有基础功能"
7. 学生点击"确认提交"按钮
8. 确认弹窗弹出，展示提交摘要信息

Then:
1. 学生在确认弹窗点击"确认提交"
2. 弹窗按钮进入 loading（"提交中..."）
3. 500ms 后提交成功
4. 弹窗关闭，ElMessage.success("任务提交成功，AI 将在稍后进行分析")
5. 表单重置（Git URL、分支、Token、验证结果、备注全部清空）
6. 提交次数更新为"已提交 1/3 次"
7. 提交历史表格新增一条记录（submissionId=201, status=SUBMITTED, 时间=最新）
8. 确认提交按钮变为"重新提交"（mySubmitCount>0）

## 交互 2: ZIP 上传方式成功提交流程

Given: 学生从任务详情页到达 /student/submit/3，submissionType=ZIP，mySubmitCount=2，maxSubmitCount=3

When:
1. 页面展示 ZIP Tab（根据 submissionType=ZIP 自动选中）
2. FileUploader 展示拖拽上传区域（虚线边框 + UploadCloud 图标）
3. 学生从文件管理器拖拽 "vue-project.zip" 文件到拖拽区域
4. FileUploader 校验通过（扩展名 .zip，大小 15MB < 50MB）
5. 开始上传: 展示文件信息条 + 进度条从 0% 到 100%
6. 800ms 后上传成功: CheckCircle 绿色图标 + "上传成功" + 文件信息
7. 学生在备注输入框输入"第三次提交，更新了 UI 组件"
8. 学生点击"确认提交"

Then:
1. 确认弹窗弹出，显示提交方式 "ZIP 上传"、文件名 "vue-project.zip"、备注、提交次数 "第 3 次提交（共 3 次）"
2. 点击"确认提交" → 500ms → 提交成功
3. 弹窗关闭，ElMessage.success("任务提交成功")
4. 表单重置（FileUploader 回到 idle 状态，文件被清除）
5. 提交次数更新为"已提交 3/3 次"（红色文字，"已达到最大提交次数"）
6. "确认提交"按钮 disabled，tooltip "已达到最大提交次数（3次）"
7. 提交历史表格新增记录

## 交互 3: Git 仓库验证失败

Given: 学生在 Git Tab，输入了无效的仓库地址 "https://github.com/student/not-exist.git"

When:
1. 学生点击"验证仓库"按钮
2. gitVerifying = true，按钮 loading
3. 500ms 后 API 返回 { code: 8001, message: "Git 仓库不存在或无法访问" }

Then:
1. gitVerifying = false
2. 红色错误区域展示: XCircle 图标 + "仓库不存在或无法访问，请检查地址是否正确"
3. gitVerifyResult = { valid: false, errorCode: 8001 }
4. 学生可修改 URL 后重新验证
5. "确认提交"按钮仍然 disabled（gitVerifyResult.valid = false，提交内容未就绪）

## 交互 4: 提交失败 - 截止时间已过

Given: 学生打开提交页，taskInfo.deadline = "2026-07-01T23:59:59.000Z"（已过期），mySubmitCount=0

When:
1. CountdownTimer 组件检测到 deadline 已过
2. @expired 事件触发 → handleDeadlineExpired()
3. deadlineExpired = true

Then:
1. Task Info Header 中倒计时显示"已截止"红色文字
2. "确认提交"按钮 disabled，tooltip 显示"任务已截止，不能再提交"
3. FileUploader disabled 属性为 true（不接受新文件）
4. Git URL 输入框和验证按钮仍可用（可查看仓库信息但不能提交）
5. 如果学生在确认弹窗输入了内容并点击"确认提交"（绕过前端 disabled），API 返回 1006 错误 → ElMessage.warning("任务已截止，不能再提交")

## 交互 5: 文件上传失败后重新上传

Given: 学生在 ZIP Tab 选择了文件，上传过程中网络中断

When:
1. uploadAction Promise reject: 网络错误
2. FileUploader emit('upload-error', { message: '网络连接失败，文件上传中断' })

Then:
1. FileUploader 展示红色错误状态: XCircle 图标 + "网络连接失败，文件上传中断" + "重新上传"按钮 + "移除"按钮
2. uploadedFileId 保持 null，"确认提交"按钮 disabled（文件未就绪）
3. 学生点击"重新上传"按钮 → FileUploader 重置为 idle 状态 → 可重新选择文件
4. 学生点击"移除"按钮 → 清除文件信息，回到 idle 状态

## 交互 6: 提交次数达上限时无法提交

Given: mySubmitCount = 3, maxSubmitCount = 3

When:
1. 学生到达提交页

Then:
1. 提交次数行显示"已提交 3/3 次"（红色文字）+ "已达到最大提交次数"红色提示
2. "确认提交"按钮 disabled，tooltip "已达到最大提交次数（3次），如需继续提交请联系教师"
3. FileUploader disabled = true
4. Git Tab 输入框和验证按钮仍然可点击（可查看），但提交按钮无法激活
5. 如果学生设法提交（绕过前端），API 返回 1005 → ElMessage.warning("已达到最大提交次数，无法继续提交")

## 交互 7: 切换 Tab 时重置状态

Given: 学生在 Git Tab 输入了 URL 并验证通过，然后切换到 ZIP Tab

When:
1. 学生点击"ZIP 上传" Tab

Then:
1. activeTab 切换为 1
2. ZIP Tab 的 FileUploader 正常展示（idle 状态）
3. Git Tab 的表单状态保留在内存中（但不可见），切换回 Git Tab 时数据仍在
4. Common Area（备注、提交次数、提交按钮）保持不变
5. canSubmit 计算属性重新计算: ZIP Tab 下检查 uploadedFileId 是否非空

## 交互 8: 查看提交历史详情

Given: 提交历史表格展示 2 条历史记录

When:
1. 学生点击第 2 条记录（submissionId=201, status=COMPLETED）的"查看详情"按钮

Then:
1. router.push('/student/grades/201')
2. 导航到学生成绩详情页，查看该次提交的评分结果
3. SubmitPage 组件被新页面替换（keepAlive 为 false，不缓存提交页表单状态）

## 交互 9: 备注字数超限

Given: 学生在备注输入框输入文本

When:
1. 学生输入了 498 个字符，继续输入

Then:
1. 到达 500 字符时，输入被截断（maxlength=500 的原生行为）
2. 字数计数器显示 "500 / 500"（红色变体，表示已达上限）
3. 继续输入无效（超出部分不被接受）

## 交互 10: 返回按钮行为

Given: 学生在提交页已输入 Git URL 和备注，点击 PageHeader 的返回按钮

When:
1. 学生点击返回按钮

Then:
1. 如果表单中有未提交的内容（gitForm.url 非空 或 uploadedFileId 非空 或 remark 非空）:
   - 弹出确认对话框（使用 ElMessageBox.confirm）: "您有未提交的内容，离开后数据将丢失，确认离开？"
   - 用户点击"确认" → router.back()
   - 用户点击"取消" → 留在当前页
2. 如果表单全部为空 → 直接 router.back()

---

# Section 8: Permission Design

| 属性 | 值 |
|------|-----|
| 页面权限标识 | student:task:submit |
| 页面允许角色 | ['student'] |
| 路由权限检查 | meta.roles = ['student'] |
| 路由 meta | { title: '提交任务', icon: 'Upload', roles: ['student'], keepAlive: false } |
| 按钮级权限 | 无（按钮 disabled 状态由业务逻辑决定而非角色权限） |

权限控制详情:

1. **页面级权限**: 仅学生角色可访问。路由守卫检查 role === 'student'。

2. **数据级权限**:
   - 任务必须属于当前学生（API 层面根据 JWT token 的 userId 验证）
   - 如果学生 A 尝试提交学生 B 的任务（通过在 URL 输入 taskId），后端返回 4003（无权访问）

3. **按钮 disabled 逻辑（非权限相关）**:
   - "确认提交": 当 deadlineExpired || mySubmitCount >= maxSubmitCount || 提交内容未就绪时 disabled
   - "验证仓库": 当 gitUrl 为空或 deadlineExpired 时 disabled（但非 disabled 状态下也不隐藏）
   - FileUploader: 当 deadlineExpired || mySubmitCount >= maxSubmitCount 时整体 disabled

4. **keepAlive: false**: 提交页不缓存。原因：(a) 每次进入提交页应该是最新状态（提交次数、截止时间可能变化）；(b) 存在表单数据泄露风险（不同任务之间表单数据不应共享）；(c) 提交成功后表单重置和页面清理更简单

5. **未登录用户访问**: 路由守卫重定向到 /login

---

# Section 9: Acceptance Criteria

AC-1: Given 学生从任务详情页点击"去提交"到达 /student/submit/1 When 页面加载完成 Then 展示 PageHeader("提交任务" + 面包屑)、Task Info Header（任务名 + 倒计时）、提交方式 Tabs（Git URL / ZIP 上传）、对应 Tab 的表单内容、备注输入框（max 500 字符 + 字数统计）、提交次数指示器、确认提交按钮、提交历史表格（如有历史记录，4 列）

AC-2: Given 学生在 Git Tab 输入有效的 Git 仓库地址 When 点击"验证仓库"按钮 Then "验证仓库"按钮进入 loading 状态，500ms 后展示绿色验证成功区域（含仓库名、默认分支、分支列表、最新提交信息）。如果验证失败，展示红色错误区域并标明错误原因（8001-8004）

AC-3: Given 学生在 ZIP Tab 选择 .zip 文件 When 文件通过客户端校验（格式和大小） Then FileUploader 展示上传进度条（0% → 100%），800ms 后上传成功展示绿色"上传成功" + 文件信息条。如果文件格式错误或超大小，展示红色错误信息且不调用上传 API

AC-4: Given 学生已填写提交内容并点击"确认提交" When 确认弹窗展示提交摘要信息，学生点击"确认提交" Then 弹窗按钮进入 loading，500ms 后提交成功：关闭弹窗、ElMessage.success("任务提交成功，AI 将在稍后进行分析")、表单重置、提交次数 +1、提交历史刷新。如果提交次数达到上限，"确认提交"按钮 disabled

AC-5: Given 学生尝试提交但已达到最大提交次数（3/3） When 学生查看提交页 Then 提交次数显示红色"已提交 3/3 次" + "已达到最大提交次数"，"确认提交"按钮 disabled 且 tooltip 显示"已达到最大提交次数（3次），如需继续提交请联系教师"，FileUploader 整体 disabled

AC-6: Given 任务截止时间已过期 When 学生查看提交页 Then 倒计时显示"已截止"红色文字，"确认提交"按钮 disabled 且 tooltip 显示"任务已截止，不能再提交"。如果学生绕过前端限制提交，API 返回 1006 错误并提示

AC-7: Given 学生正在填写提交表单 When 点击返回按钮 Then 如果表单有未提交内容，弹出确认对话框"您有未提交的内容，离开后数据将丢失，确认离开？"。如果表单为空，直接返回上一页

AC-8: Given 提交页加载中 When API 请求未返回 Then 展示 LoadingState("正在加载任务信息...")，面包屑可见。加载失败时展示 ErrorState 和重试按钮

AC-9: Given 学生已提交 1 次任务 When 查看页面底部的提交历史表格 Then 展示提交记录列表（按时间倒序），每条包含提交时间、提交方式、状态（TaskStatusBadge）、"查看详情"操作按钮。点击"查看详情"跳转到 /student/grades/:submissionId

AC-10: Given 学生上传的文件为 .rar 格式（不在 accept 列表中） When FileUploader 捕获文件 Then 客户端校验拒绝该文件，展示红色错误"文件格式不支持，请上传 .zip 格式的文件"，不调用上传 API

---

# Section 10: Files to Create / Modify

## 新建文件

```
├── src/pages/student/SubmitPage.vue              # 学生提交页面组件
├── src/components/common/FileUploader.vue        # 文件上传通用组件（拖拽 + 进度 + 校验）
├── src/api/studentSubmit.ts                      # 学生提交相关 API 函数（getSubmitInfo、createSubmission、verifyGitRepo、uploadFile、getSubmissionHistory、uploadReport、triggerAiEvaluate）
├── src/api/file.ts                               # 文件上传 API 函数（uploadFile）—— 如果与提交 API 分开管理
├── src/mock/studentSubmit.ts                     # 提交相关 Mock 数据（submit-info、create-submission、git-verify、file-upload、submission-history）
└── docs/page-analysis/student-submit.md          # 本文件
```

## 修改文件

```
├── src/router/routes/student.ts                  # 追加 StudentSubmit 路由（/student/submit/:taskId → SubmitPage.vue, meta: { keepAlive: false }）
├── src/api/studentTask.ts                        # 确认 getStudentTaskDetail 或 getSubmitInfo 方法可用
├── src/mock/studentTasks.ts                      # 确认 submit-info Mock 接口已覆盖
├── src/mock/index.ts                             # 追加 import './studentSubmit'
└── src/types/task.ts                             # 追加 SubmitForm、GitVerifyResult、UploadResult 等类型定义
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
├── src/components/common/CountdownTimer.vue      # 新建组件，本页面引用
├── src/components/common/TaskStatusBadge.vue     # 新建组件，本页面引用
├── src/components/base/BaseTable.vue
├── src/components/base/BaseInput.vue
├── src/components/base/BaseButton.vue
├── src/components/base/BaseModal.vue
└── src/stores/useUserStore.ts
```

## 开发顺序

1. 完成 src/types/task.ts（追加类型: SubmitForm, GitVerifyResult, UploadResult, SubmissionRecord）
2. 完成 src/components/common/FileUploader.vue —— 独立通用组件，可并行
3. 完成 src/mock/studentSubmit.ts —— 并行
4. 完成 src/api/studentSubmit.ts —— 依赖类型
5. 完成 src/api/file.ts —— 依赖类型
6. 完成 src/pages/student/SubmitPage.vue —— 依赖以上全部
7. 完成路由配置

注意事项:
1. FileUploader 是通用组件，虽然首次在 SubmitPage 使用，但需要在教师上传资料等其他场景复用。因此放在 src/components/common/ 而非 src/components/student/。
2. 提交成功后不自动跳转，保持在当前页面，让学生可以看到更新后的提交次数和历史记录。自动跳转会让学生困惑。
3. Git 仓库验证是非阻塞操作——学生可以在不验证的情况下直接提交（后端会再次验证），但建议前端引导验证以提升用户体验。如果产品要求验证必须通过才能提交，则将 gitVerifyResult.valid 加入 canSubmit 条件。
4. 提交确认弹窗（BaseModal）在提交过程中不可关闭（closeOnClickModal: false），防止用户误操作中断提交。
5. Access Token 使用 type="password" 输入框（showPassword 可切换），且不会缓存在 localStorage 中。页面卸载时自动清除 token 内存数据。
6. 表单重置时保留 taskInfo 数据（任务名、截止时间、提交限制、提交次数），仅清空用户输入内容。
7. 提交历史表格的查看详情功能需要 submissionId 存在。对于某些未进入评分流程就退回的提交，submissionId 可能仍然有效（后端会保留所有提交记录）。
8. MVP 阶段暂不实现在线编码（ONLINE）提交方式的 Tab。当 submissionType=ONLINE 时，页面展示"此任务使用在线编码方式提交，请前往编码环境操作"提示文字和跳转按钮。
