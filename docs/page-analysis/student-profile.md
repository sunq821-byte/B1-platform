# Page Analysis: 学生个人中心页 (Student Profile)

**文档版本**: v1.0
**创建日期**: 2026-07-03
**分析范围**: 学生个人中心页面，含个人信息查看/编辑、头像上传、修改密码

---

# Section 1: Page Identity

| 属性 | 值 |
|------|-----|
| 页面名称 | 学生个人中心页 |
| 页面文件 | src/pages/student/ProfilePage.vue |
| 路由路径 | /student/profile |
| 路由名称 | StudentProfile |
| 所属 Sprint | Sprint 2 |
| 所属 Feature | F-2-03（参见《Sprint 2 Spec》） |
| 页面角色 | 学生 |
| 页面复杂度 | L1（基础表单页面：信息展示 + 编辑切换 + 头像上传 + 修改密码，复杂度低但涉及 4 个 API 端点） |
| 原型参考 | 参见《UI Design System v1.0》学生个人中心原型 |

页面职责：学生查看和编辑个人基本信息（真实姓名、邮箱、手机号）。支持头像上传（圆形头像，默认展示姓名首字符）。提供修改密码功能。页面分为信息展示/编辑区和密码修改区两个独立区域。

---

# Section 2: Page Layout Structure

页面嵌套在 AppLayout 中。布局采用上下两个独立卡片区域。

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
|  |           |  |  标题: "个人中心"              |  | |
|  |           |  +-----------------------------+  | |
|  |           |  [间距: 24px]                     |  | |
|  |           |                                    | |
|  |           |  +-----------------------------+  | |
|  |           |  |  卡片 1: 基本信息              |  | |
|  |           |  |  max-width: 640px              |  | |
|  |           |  |  bg: --color-card              |  | |
|  |           |  |  padding: --spacing-xl (32px)  |  | |
|  |           |  |  border-radius: --radius-lg    |  | |
|  |           |  |                                |  | |
|  |           |  |  +--------------------------+ |  | |
|  |           |  |  | 头像区 (左侧 120px)       | |  | |
|  |           |  |  | BaseAvatar (size=80px)    | |  | |
|  |           |  |  | 圆形, 可点击上传           | |  | |
|  |           |  |  | Upload 提示文字            | |  | |
|  |           |  |  +--------------------------+ |  | |
|  |           |  |  | 信息区 (右侧 flex: 1)     | |  | |
|  |           |  |  | Form 字段:                | |  | |
|  |           |  |  | - 真实姓名                 | |  | |
|  |           |  |  | - 学号 (只读)             | |  | |
|  |           |  |  | - 邮箱                     | |  | |
|  |           |  |  | - 手机号                   | |  | |
|  |           |  |  | - 班级 (只读)             | |  | |
|  |           |  |  +--------------------------+ |  | |
|  |           |  |  [切换按钮: 编辑/保存/取消]   |  | |
|  |           |  +-----------------------------+  | |
|  |           |  [间距: 24px]                     |  | |
|  |           |                                    | |
|  |           |  +-----------------------------+  | |
|  |           |  |  卡片 2: 修改密码              |  | |
|  |           |  |  max-width: 640px              |  | |
|  |           |  |  bg: --color-card              |  | |
|  |           |  |  padding: --spacing-xl (32px)  |  | |
|  |           |  |                                |  | |
|  |           |  |  标题: "修改密码"              |  | |
|  |           |  |  表单字段:                     |  | |
|  |           |  |  - 原密码 (type=password)      |  | |
|  |           |  |  - 新密码 (type=password)      |  | |
|  |           |  |  - 确认密码 (type=password)    |  | |
|  |           |  |  提交按钮: BaseButton           |  | |
|  |           |  +-----------------------------+  | |
|  |           |                                    | |
|  +-----------+-----------------------------------+ |
+--------------------------------------------------+
```

**区域说明**：

| 区域 | 宽度 | 高度 | 对齐 | 滚动策略 | 说明 |
|------|------|------|------|---------|------|
| PageHeader | 100% 父容器 | auto | flex start | 不滚动 | 标题"个人中心" |
| 基本信息卡片 | max-width: 640px (居中) | auto | 左对齐 | 不滚动 | 白色卡片，包含头像区和信息表单区的横向 flex 布局 |
| 头像区 | 120px 宽度（固定） | auto | 垂直居中，水平居中 | 不滚动 | 包含 BaseAvatar 和 BaseUpload 触发区域。头像尺寸 80px，圆形 |
| 信息区 | flex: 1 | auto | 左对齐 | 不滚动 | 5 个信息字段，垂直排列，间距 16px。编辑模式/查看模式切换 |
| 操作按钮区 | 全卡片宽度 | auto | 右对齐 | 不滚动 | "编辑信息"按钮（查看模式）或"保存"/"取消"按钮组（编辑模式） |
| 修改密码卡片 | max-width: 640px (居中) | auto | 左对齐 | 不滚动 | 白色卡片，3 个密码输入字段 + 提交按钮 |

**基本信息卡片内部布局（横向 flex）**:

```
+----------------------------------------------+
|  [BaseAvatar (80px)]  |  真实姓名: 张三        |
|   [点击更换头像]      |  学号: 20240101001     |
|                       |  邮箱: zhangsan@...    |
|                       |  手机号: 13800138001    |
|                       |  班级: 软件技术2401班    |
|                       |                        |
|                       |  [编辑信息] 按钮       |
+----------------------------------------------+
```

**响应式行为**：
- 1920px 分辨率：两张卡片 max-width 640px，水平居中
- 1366px 分辨率：两张卡片 max-width 600px，水平居中
- 小于 768px：卡片 max-width 100%，头像区和信息区改为纵向堆叠（flex-direction: column），头像居中

---

# Section 3: Component Tree & Specification

## 3.1 组件树

```
ProfilePage.vue（页面组件，在 AppLayout 内渲染）
├── PageHeader（来自 Component Library）
│   └── 标题文字: "个人中心"（硬编码）
│
├── <div> 内容容器（max-width: 640px, margin: 0 auto）
│   │
│   ├── LoadingState（条件渲染: loading=true）
│   │   来自 Component Library，src/components/common/LoadingState.vue
│   │   Props: text="正在加载个人信息..."
│   │
│   ├── ErrorState（条件渲染: error 状态）
│   │   来自 Component Library，src/components/common/ErrorState.vue
│   │   Props: message=errorMessage, showRetry=true
│   │   @retry="fetchUserProfile()"
│   │
│   └── <div> Success 状态内容（条件渲染: loading=false 且 error=null 且 userInfo 非空）
│       │
│       ├── 卡片 1: 基本信息卡片（div.card）
│       │   ├── <div> 卡片标题（可选，"基本信息"）
│       │   ├── <div> 卡片内容区（display: flex, gap: 24px, align-items: flex-start）
│       │   │   ├── <div> 头像区（width: 120px, text-align: center）
│       │   │   │   ├── BaseAvatar（来自 Component Library, src/components/base/BaseAvatar.vue）
│       │   │   │   │   Props: src=avatarUrl, name=userInfo.realName, size=80, shape="circle"
│       │   │   │   │   无头像时: 显示 realName 首字符（背景色由 name hash 生成）
│       │   │   │   │   有头像时: 显示头像图片
│       │   │   │   │   @click="triggerAvatarUpload"（点击触发隐藏的 BaseUpload）
│       │   │   │   │
│       │   │   │   ├── BaseUpload（来自 Component Library, src/components/base/BaseUpload.vue）
│       │   │   │   │   隐藏渲染（display: none 或 v-show=false）
│       │   │   │   │   Props: accept="image/jpeg,image/png", maxSize=2, maxCount=1, autoUpload=false, showFileList=false
│       │   │   │   │   ref="avatarUploadRef"
│       │   │   │   │   @change="handleAvatarChange"
│       │   │   │   │
│       │   │   │   └── <p> 提示文字
│       │   │   │       "点击更换头像"（font-size: --font-size-xs, color: --color-text-placeholder）
│       │   │   │       "支持 JPG/PNG，最大 2MB"
│       │   │   │
│       │   │   └── <div> 信息区（flex: 1）
│       │   │       │
│       │   │       ├── [查看模式: isEditing=false] 信息展示区
│       │   │       │   ├── <div> 字段行 × 5（display: grid, grid-template-columns: 80px 1fr, gap: 12px 16px）
│       │   │       │   │   ├── <label> "真实姓名" → <span> userInfo.realName
│       │   │       │   │   ├── <label> "学号" → <span> userInfo.studentNo（font-family: JetBrains Mono, color: --color-text-secondary）
│       │   │       │   │   ├── <label> "邮箱" → <span> userInfo.email
│       │   │       │   │   ├── <label> "手机号" → <span> userInfo.phone
│       │   │       │   │   └── <label> "班级" → <span> userInfo.className（color: --color-text-secondary）
│       │   │       │
│       │   │       ├── [编辑模式: isEditing=true] 信息编辑区
│       │   │       │   ├── el-form（ref="profileFormRef", model="editForm", rules="profileRules", label-position="top"）
│       │   │       │   │   ├── el-form-item（prop="realName", label="真实姓名"）
│       │   │       │   │   │   └── BaseInput（v-model="editForm.realName", placeholder="请输入真实姓名", maxlength=50）
│       │   │       │   │   ├── el-form-item（label="学号"）
│       │   │       │   │   │   └── BaseInput（v-model="userInfo.studentNo", disabled, readonly）
│       │   │       │   │   │       提示: 学号不可修改
│       │   │       │   │   ├── el-form-item（prop="email", label="邮箱"）
│       │   │       │   │   │   └── BaseInput（v-model="editForm.email", placeholder="请输入邮箱", type="email", maxlength=100）
│       │   │       │   │   ├── el-form-item（prop="phone", label="手机号"）
│       │   │       │   │   │   └── BaseInput（v-model="editForm.phone", placeholder="请输入手机号", type="tel", maxlength=20）
│       │   │       │   │   └── el-form-item（label="班级"）
│       │   │       │   │       └── BaseInput（v-model="userInfo.className", disabled, readonly）
│       │   │       │
│       │   │       └── [操作按钮行]（flex, justify-content: flex-end, gap: 12px, margin-top: 24px）
│       │   │           │
│       │   │           ├── [查看模式: isEditing=false]
│       │   │           │   └── BaseButton（type="primary", size="default", @click="enterEditMode"）
│       │   │           │       文案: "编辑信息", icon="Pencil"
│       │   │           │
│       │   │           └── [编辑模式: isEditing=true]
│       │   │               ├── BaseButton（type="default", size="default", @click="cancelEdit"）
│       │   │               │   文案: "取消"
│       │   │               └── BaseButton（type="primary", size="default", loading="saving", @click="handleSave"）
│       │   │                   文案: saving 为 true 时"保存中..."，否则"保存"
│       │   │
│       │   └── </div> 卡片内容区结束
│       │
│       ├── [间距: 24px]
│       │
│       └── 卡片 2: 修改密码卡片（div.card）
│           ├── <h3> 卡片标题: "修改密码"（font-size: --font-size-lg, font-weight: 600, margin-bottom: 20px）
│           ├── el-form（ref="passwordFormRef", model="passwordForm", rules="passwordRules", label-position="top"）
│           │   ├── el-form-item（prop="oldPassword", label="原密码"）
│           │   │   └── BaseInput（v-model="passwordForm.oldPassword", type="password", placeholder="请输入原密码", showPassword=true）
│           │   ├── el-form-item（prop="newPassword", label="新密码"）
│           │   │   └── BaseInput（v-model="passwordForm.newPassword", type="password", placeholder="请输入新密码（6-20位）", showPassword=true, maxlength=20）
│           │   ├── el-form-item（prop="confirmPassword", label="确认密码"）
│           │   │   └── BaseInput（v-model="passwordForm.confirmPassword", type="password", placeholder="请再次输入新密码", showPassword=true, maxlength=20）
│           │   └── <div> 操作按钮行（flex, justify-content: flex-end）
│           │       └── BaseButton（type="primary", size="default", loading="changingPassword", @click="handleChangePassword"）
│           │           文案: changingPassword 为 true 时"修改中..."，否则"修改密码"
│           └── </div>
│
└── NoPermission（条件渲染: 路由守卫未拦截但 Store 判断无权限时）
    来自 Component Library，src/components/common/NoPermission.vue
```

## 3.2 本页面需要新建的子组件

**本页面无新建子组件**。个人中心页面所有功能（信息展示/编辑切换、头像上传、修改密码）使用现有 Base 组件和 Element Plus 原生组件组装。业务逻辑内聚在页面组件中，预计 200-250 行（含两个表单的验证规则和三个 API 调用逻辑）。如果后续代码行数超过 300 行，将头像上传逻辑抽取到 `src/composables/useAvatarUpload.ts`，将密码修改逻辑抽取到 `src/composables/useChangePassword.ts`。当前 MVP 阶段保持内聚在页面组件中。

## 3.3 已有组件引用

**引用组件 1: PageHeader（来自 Component Library，src/components/layout/PageHeader.vue）**

关键 Props 配置:
| Prop | 值 | 说明 |
|------|-----|------|
| title | "个人中心" | 页面标题，硬编码中文 |

**引用组件 2: BaseAvatar（来自 Component Library，src/components/base/BaseAvatar.vue）**

关键 Props 配置:
| Prop | 值 | 说明 |
|------|-----|------|
| src | avatarUrl | 头像 URL（空字符串表示无头像） |
| name | userInfo.realName | 用于生成默认首字符头像 |
| size | 80 | 80px 圆形头像（对应 BaseAvatar 的 number 类型 size） |
| shape | "circle" | 圆形头像 |

**引用组件 3: BaseUpload（来自 Component Library，src/components/base/BaseUpload.vue）**

关键 Props 配置:
| Prop | 值 | 说明 |
|------|-----|------|
| accept | "image/jpeg,image/png" | 仅允许 JPG 和 PNG 格式 |
| maxSize | 2 | 最大 2MB |
| maxCount | 1 | 单次上传 1 个文件 |
| autoUpload | false | 不自动上传，由页面控制上传时机 |
| showFileList | false | 不展示文件列表 |
| multiple | false | 单文件上传 |

关键 Events 配置:
| Event | 处理函数 | 说明 |
|------|---------|------|
| change | handleAvatarChange(file) | 文件选择后触发上传逻辑 |

**引用组件 4: BaseInput（来自 Component Library，src/components/base/BaseInput.vue）**

关键 Props 配置（信息编辑表单）:
| 字段 | modelValue | type | placeholder | disabled | readonly | maxlength | 其他 |
|------|-----------|------|-------------|----------|----------|-----------|------|
| 真实姓名 | editForm.realName | "text" | "请输入真实姓名" | - | - | 50 | - |
| 学号 | userInfo.studentNo | "text" | - | true | true | - | - |
| 邮箱 | editForm.email | "email" | "请输入邮箱" | - | - | 100 | - |
| 手机号 | editForm.phone | "tel" | "请输入手机号" | - | - | 20 | - |
| 班级 | userInfo.className | "text" | - | true | true | - | - |

关键 Props 配置（修改密码表单）:
| 字段 | modelValue | type | placeholder | showPassword | maxlength |
|------|-----------|------|-------------|-------------|-----------|
| 原密码 | passwordForm.oldPassword | "password" | "请输入原密码" | true | - |
| 新密码 | passwordForm.newPassword | "password" | "请输入新密码（6-20位）" | true | 20 |
| 确认密码 | passwordForm.confirmPassword | "password" | "请再次输入新密码" | true | 20 |

**引用组件 5: BaseButton（来自 Component Library，src/components/base/BaseButton.vue）**

关键 Props 配置（编辑信息按钮）:
| Prop | 值 | 说明 |
|------|-----|------|
| type | "primary" | 主色调 |
| size | "default" | 默认尺寸 |
| icon | "Pencil" | 铅笔编辑图标 |

关键 Props 配置（保存按钮）:
| Prop | 值 | 说明 |
|------|-----|------|
| type | "primary" | 主色调 |
| size | "default" | 默认尺寸 |
| loading | saving | 保存中禁用并显示 loading |

关键 Props 配置（取消按钮）:
| Prop | 值 | 说明 |
|------|-----|------|
| type | "default" | 默认样式 |
| size | "default" | 默认尺寸 |

关键 Props 配置（修改密码按钮）:
| Prop | 值 | 说明 |
|------|-----|------|
| type | "primary" | 主色调 |
| size | "default" | 默认尺寸 |
| loading | changingPassword | 提交中禁用并显示 loading |

**引用组件 6: LoadingState（来自 Component Library，src/components/common/LoadingState.vue）**

关键 Props 配置:
| Prop | 值 | 说明 |
|------|-----|------|
| text | "正在加载个人信息..." | 加载中文案 |

**引用组件 7: ErrorState（来自 Component Library，src/components/common/ErrorState.vue）**

关键 Props 配置:
| Prop | 值 | 说明 |
|------|-----|------|
| message | "个人信息加载失败，请稍后重试" | 错误文案 |
| showRetry | true | 显示重试按钮 |

---

# Section 4: State Design（页面级）

| 状态 | 触发条件 | UI 表现 | 持续时间 | 退出条件 |
|------|---------|--------|---------|---------|
| Loading | 页面 onMounted，调用 GET /api/v1/auth/me 获取用户信息 | PageHeader 标题"个人中心"正常渲染。内容区展示 LoadingState 组件（三个跳动圆点 + "正在加载个人信息..."）。两张卡片均不渲染 | 直到 API 返回数据或超时 | API 返回数据（Success）或返回错误（Error） |
| Empty | 不适用 | 个人中心页永远有用户数据（当前登录用户），不存在空数据场景 | - | - |
| Error | GET /api/v1/auth/me 请求失败 | PageHeader 正常渲染。内容区展示 ErrorState 组件：红色 AlertCircle 图标 + "个人信息加载失败，请稍后重试"文案 + "重试"按钮。两张卡片均不渲染 | 持续直到重试成功或用户离开 | 用户点击重试且请求成功 |
| Success | API 返回用户信息成功 | 完整渲染两张卡片。基本信息卡片：头像（圆形，80px）+ 5 个信息字段（默认查看模式，只读显示）+ "编辑信息"按钮。修改密码卡片：3 个密码输入框 + "修改密码"按钮 | 持续直到用户离开页面 | 用户离开页面 |
| Edit (子状态) | 用户在 Success 状态下点击"编辑信息"按钮 | isEditing 变为 true。信息区从 Grid 查看模式切换为 el-form 编辑模式。真实姓名/邮箱/手机号变为 BaseInput 可编辑状态，学号/班级保持 disabled+readonly。操作按钮区切换为"保存"+"取消"按钮组。头像区域保持不变 | 持续直到用户保存、取消、或离开页面 | 点击"保存"成功（回到查看模式）或点击"取消"（恢复原数据，回到查看模式） |
| Saving (子状态) | 用户在 Edit 状态下点击"保存"按钮 | "保存"按钮进入 Loading 状态（文字变为"保存中..."），按钮禁用。表单所有输入框禁用（disabled=true），防止编辑。"取消"按钮也禁用（防止取消操作争抢状态） | 直到 API 返回结果 | API 返回成功（ElMessage.success + 回到查看模式）或失败（ElMessage.error + 保持编辑模式，恢复按钮和输入框） |
| ChangingPassword (子状态) | 用户点击"修改密码"按钮，表单验证通过 | "修改密码"按钮进入 Loading 状态（文字变为"修改中..."），按钮禁用。三个密码输入框禁用。"修改密码"卡片内的其他元素不变 | 直到 API 返回结果 | API 返回成功（ElMessage.success + 清空密码表单）或失败（ElMessage.error + 恢复按钮和输入框） |
| NoPermission | 不适用 | 路由守卫 meta.roles=["student"] 已控制。极端情况下 Store 角色异常时渲染 NoPermission 组件 | - | - |
| Offline | navigator.onLine 变为 false | AppLayout 级全局处理（见前面页面分析文档说明）。本页面离线时：编辑模式"保存"按钮点击后弹出 ElMessage.warning("网络连接已断开，请检查网络")；修改密码按钮同理；头像上传点击后弹出 ElMessage.warning("网络连接已断开") | 直到网络恢复 | navigator.onLine=true，自动刷新用户信息 |

---

# Section 5: Data Flow

## 5.1 页面加载数据流

```
学生导航到 /student/profile（点击侧边栏菜单"个人中心"或 Header 用户下拉菜单）
→ 路由守卫 beforeEach 触发 → 认证检查 → 权限检查 → 放行
→ ProfilePage.vue 渲染
→ onMounted():
  1. 初始化本地状态:
     - loading = true
     - userInfo = null
     - avatarUrl = ''
     - isEditing = false
     - saving = false
     - changingPassword = false
     - error = null
     - editForm = { realName: '', email: '', phone: '' }
     - passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
  2. 调用 fetchUserProfile()
→ fetchUserProfile() 内部:
  1. loading = true
  2. try:
     a. 调用 userApi.fetchCurrentUser()
        (或从 useUserStore 获取缓存: userStore.userInfo —— 如果 Store 中已有数据且未过期，直接使用，减少 API 请求)
     b. 优先策略: 检查 useUserStore().userInfo 是否存在且非空
        - 存在: 直接使用 userInfo = userStore.userInfo，avatarUrl = userStore.userInfo.avatar || ''
          loading = false，进入 Success 状态
        - 不存在或为空: 发送 API 请求
     c. API 请求: GET /api/v1/auth/me
     d. Mock 层拦截（300ms 延迟）
     e. 响应成功 (code=0):
        - userInfo = response.data
        - avatarUrl = response.data.avatar || ''
        - editForm = { realName: userInfo.realName, email: userInfo.email, phone: userInfo.phone }
        - loading = false
        - error = null
  3. catch (error):
     - error = error.message || '个人信息加载失败，请稍后重试'
     - loading = false
→ Success: 渲染基本信息卡片 + 修改密码卡片
```

## 5.2 用户操作数据流

### 操作 1: 进入编辑模式

```
触发: 用户在查看模式下点击"编辑信息"按钮
→ enterEditMode()
  1. isEditing = true
  2. 确保 editForm 与当前 userInfo 同步（防止数据不一致）:
     - editForm.realName = userInfo.realName
     - editForm.email = userInfo.email
     - editForm.phone = userInfo.phone
  3. 信息区从 Grid 查看布局切换为 el-form 编辑布局
  4. 操作按钮从单个"编辑信息"切换为"保存"+"取消"按钮组
  5. 页面不滚动（原位切换）
```

### 操作 2: 保存编辑

```
触发: 用户在编辑模式下点击"保存"按钮
→ handleSave()
  1. el-form 的 validate() 方法验证 editForm:
     - 验证失败: 展示字段级红色错误提示（Element Plus 原生行为），数据流终止
     - 验证通过: 继续
  2. saving = true（"保存"按钮 Loading + 所有输入框禁用）
  3. 调用 userApi.updateProfile(editForm)
  4. Axios 发送 PUT /api/v1/user/profile
     Request Body: { realName: "张三", email: "zhangsan@example.com", phone: "13800138001" }
  5. Mock 返回成功 (code=0):
     a. ElMessage.success("个人信息更新成功")
     b. 更新本地 userInfo 对象: realName, email, phone = editForm 对应值
     c. 同步更新 useUserStore().userInfo（如果 Store 中有缓存）
     d. isEditing = false
     e. saving = false
     f. 回到查看模式，展示更新后的信息
  6. Mock 返回失败 (code!=0):
     a. ElMessage.error(response.message || "保存失败，请稍后重试")
     b. saving = false
     c. 保持编辑模式（用户可修正后重试）
     d. 网络错误: ElMessage.error("网络连接失败，请检查网络")
```

### 操作 3: 取消编辑

```
触发: 用户在编辑模式下点击"取消"按钮
→ cancelEdit()
  1. isEditing = false
  2. 恢复 editForm 为当前 userInfo 的值（丢弃未保存的修改）:
     - editForm.realName = userInfo.realName
     - editForm.email = userInfo.email
     - editForm.phone = userInfo.phone
  3. el-form 的验证状态通过 formRef.resetFields() 清除
  4. 回到查看模式
```

### 操作 4: 头像上传

```
触发: 用户点击 BaseAvatar 区域
→ triggerAvatarUpload()
  1. 如果 !navigator.onLine: ElMessage.warning("网络连接已断开") 并终止
  2. 通过 ref 触发 BaseUpload 的文件选择对话框: avatarUploadRef.value.openFileDialog()
     （或使用 BaseUpload 的 click 触发）
  3. 用户选择文件后，BaseUpload 的 change 事件触发 → handleAvatarChange(file)
  4. handleAvatarChange(file):
     a. 前端校验:
        - 文件类型: file.type 必须是 'image/jpeg' 或 'image/png'，否则 ElMessage.error("仅支持 JPG 和 PNG 格式的图片") + 终止
        - 文件大小: file.size / 1024 / 1024 > 2，否则 ElMessage.error("图片大小不能超过 2MB") + 终止
     b. 构造 FormData:
        - formData.append('file', file.raw)
     c. ElMessage.info("头像上传中...")
     d. 调用 userApi.uploadAvatar(formData)
     e. POST /api/v1/user/avatar（multipart/form-data）
     f. Mock 返回成功 (code=0):
        - avatarUrl = response.data.avatar
        - ElMessage.success("头像更新成功")
        - 更新 useUserStore().userInfo.avatar（如果 Store 中有缓存）
        - BaseAvatar 自动更新显示新头像
     g. Mock 返回失败:
        - code=7003: ElMessage.error("文件上传失败，请重试")
        - code=7004: ElMessage.error("文件类型不支持，仅支持 JPG/PNG")
        - 其他: ElMessage.error("头像上传失败，请稍后重试")
```

### 操作 5: 修改密码

```
触发: 用户填写原密码、新密码、确认密码后点击"修改密码"按钮
→ handleChangePassword()
  1. el-form (passwordFormRef) 的 validate() 方法验证 passwordForm:
     - 验证规则:
       a. oldPassword: required, message="请输入原密码"
       b. newPassword: required, message="请输入新密码", minLength=6, message="新密码至少6位"
       c. confirmPassword: required, message="请确认新密码", validator=必须与 newPassword 一致, message="两次输入的密码不一致"
     - 验证失败: 展示字段级红色错误提示，数据流终止
     - 验证通过: 继续
  2. changingPassword = true（"修改密码"按钮 Loading + 所有密码输入框禁用）
  3. 调用 userApi.changePassword({ oldPassword, newPassword })
  4. Axios 发送 PUT /api/v1/user/password
     Request Body: { oldPassword: "oldPass123", newPassword: "newPass456" }
  5. Mock 返回成功 (code=0):
     a. ElMessage.success("密码修改成功")
     b. changingPassword = false
     c. 清空密码表单: passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
     d. el-form 通过 formRef.resetFields() 清除验证状态
  6. Mock 返回失败:
     - code=2009: ElMessage.error("原密码错误，请重新输入")
       → 仅清空 oldPassword 字段，保留新密码和确认密码
       → 不重置整个表单
     - code=4001: ElMessage.error(response.message)（展示后端返回的具体校验错误信息）
     - 其他: ElMessage.error("密码修改失败，请稍后重试")
     - 恢复: changingPassword = false，保持所有输入框可编辑
```

---

# Section 6: API & Mock Specification（页面级精确值）

## 接口 1: GET /api/v1/auth/me

用途: 获取当前登录用户信息（包含角色和头像 URL）

说明: 此接口在 Sprint 1 已完成，本页面复用。用于获取用户基本信息和头像。

请求参数: 无（从 JWT Token 中解析用户身份）

响应数据结构:
```
{
  code: number,
  message: string,
  data: {
    id: number,          // 用户 ID
    username: string,    // 用户名
    realName: string,    // 真实姓名
    email: string,       // 邮箱
    phone: string,       // 手机号
    role: string,        // 角色: "student"
    avatar: string       // 头像 URL（空字符串表示无头像）
  },
  success: boolean,
  timestamp: string,
  traceId: string,
  requestId: string
}
```

注意: GET /api/v1/auth/me 返回的基础用户信息不包含 studentNo 和 className 字段。本页面需要这两个额外字段用于展示。有两种处理方式：
1. 前端从 Store 扩展获取（如果 useUserStore 已有）
2. 新增独立 API GET /api/v1/student/profile 返回完整学生信息

本页面分析采用方案 1（复用 GET /api/v1/auth/me），额外需要的 studentNo 和 className 从 Mock 中扩展 data 字段返回。实际联调时，后端需在 /api/v1/auth/me 的 userInfo 中增加这两个字段，或新增 /api/v1/student/profile 接口。

Mock 数据示例 1（学生用户 - 有头像）:
```
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "username": "student01",
    "realName": "张三",
    "email": "zhangsan@example.com",
    "phone": "13800138001",
    "role": "student",
    "avatar": "https://example.com/avatars/student01.jpg",
    "studentNo": "20240101001",
    "className": "软件技术2401班"
  },
  "success": true,
  "timestamp": "2026-07-03T10:10:00.123+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678910",
  "path": "/api/v1/auth/me",
  "elapsed": 18
}
```

Mock 数据示例 2（学生用户 - 无头像）:
```
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 2,
    "username": "student02",
    "realName": "李四",
    "email": "lisi@example.com",
    "phone": "13900139002",
    "role": "student",
    "avatar": "",
    "studentNo": "20240101002",
    "className": "软件技术2401班"
  },
  "success": true,
  "timestamp": "2026-07-03T10:10:00.456+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678911",
  "path": "/api/v1/auth/me",
  "elapsed": 15
}
```

错误场景:
- 401: { "code": 2001, "message": "请先登录", "data": null } → 路由守卫拦截
- 5001: { "code": 5001, "message": "服务器繁忙，请稍后重试", "data": null } → Error 状态 + 重试

Mock 延迟: 300ms

---

## 接口 2: PUT /api/v1/user/profile

用途: 更新当前用户的基本信息（真实姓名、邮箱、手机号）

请求 Headers: Authorization: Bearer {jwt_token}, Content-Type: application/json

请求参数:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| realName | string | 是 | 真实姓名，1-50 字符 | "张三" |
| email | string | 是 | 邮箱地址 | "zhangsan@example.com" |
| phone | string | 是 | 手机号码 | "13800138001" |

请求 Body:
```
{
  "realName": "张三",
  "email": "zhangsan@newemail.com",
  "phone": "13800138002"
}
```

响应数据结构:
```
{
  code: number,
  message: string,
  data: {
    id: number,
    realName: string,
    email: string,
    phone: string
  } | null
}
```

Mock 数据示例 1（更新成功）:
```
{
  "code": 0,
  "message": "个人信息更新成功",
  "data": {
    "id": 1,
    "realName": "张三",
    "email": "zhangsan@newemail.com",
    "phone": "13800138002"
  },
  "success": true,
  "timestamp": "2026-07-03T10:12:00.123+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678912",
  "path": "/api/v1/user/profile",
  "elapsed": 45
}
```

Mock 数据示例 2（参数校验失败 - 邮箱格式错误）:
```
{
  "code": 4001,
  "message": "参数校验失败",
  "data": null,
  "success": false,
  "timestamp": "2026-07-03T10:12:01.000+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678913",
  "path": "/api/v1/user/profile",
  "errors": [
    {
      "field": "email",
      "message": "邮箱格式不正确",
      "rejectedValue": "invalid-email"
    }
  ],
  "elapsed": 8
}
```

错误场景:
- 4001: 参数校验失败 → ElMessage.error 展示具体错误信息（如"邮箱格式不正确"）
- 5001: { "code": 5001, "message": "服务器繁忙，请稍后重试", "data": null } → ElMessage.error("保存失败，请稍后重试")

Mock 延迟: 200ms

---

## 接口 3: PUT /api/v1/user/password

用途: 修改当前用户的登录密码

请求 Headers: Authorization: Bearer {jwt_token}, Content-Type: application/json

请求参数:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| oldPassword | string | 是 | 原密码（明文） | "oldPass123" |
| newPassword | string | 是 | 新密码（明文，6-20 位） | "newPass456" |

请求 Body:
```
{
  "oldPassword": "oldPass123",
  "newPassword": "newPass456"
}
```

响应数据结构:
```
{
  code: number,       // 0=成功, 2009=原密码错误, 4001=校验失败
  message: string,
  data: null
}
```

Mock 数据示例 1（修改成功）:
```
{
  "code": 0,
  "message": "密码修改成功",
  "data": null,
  "success": true,
  "timestamp": "2026-07-03T10:15:00.123+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678914",
  "path": "/api/v1/user/password",
  "elapsed": 120
}
```

Mock 数据示例 2（原密码错误 - code=2009）:
```
{
  "code": 2009,
  "message": "原密码错误，请重新输入",
  "data": null,
  "success": false,
  "timestamp": "2026-07-03T10:15:01.000+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678915",
  "path": "/api/v1/user/password",
  "elapsed": 55
}
```

Mock 数据示例 3（新密码长度不足 - code=4001）:
```
{
  "code": 4001,
  "message": "参数校验失败",
  "data": null,
  "success": false,
  "timestamp": "2026-07-03T10:15:02.000+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678916",
  "path": "/api/v1/user/password",
  "errors": [
    {
      "field": "newPassword",
      "message": "新密码长度不能少于6位",
      "rejectedValue": "123"
    }
  ],
  "elapsed": 6
}
```

错误场景:
- 2009: 原密码错误 → ElMessage.error("原密码错误，请重新输入")，清空 oldPassword 字段，保留 newPassword 和 confirmPassword
- 4001: 参数校验失败 → ElMessage.error(response.message) 或展示具体校验信息
- 5001: 服务器错误 → ElMessage.error("密码修改失败，请稍后重试")

Mock 逻辑:
- oldPassword 为 "wrongpass" 时返回 2009
- newPassword 长度不足 6 位时返回 4001
- 其他情况返回 200（成功）

Mock 延迟: 300ms

---

## 接口 4: POST /api/v1/user/avatar

用途: 上传用户头像

请求 Headers: Authorization: Bearer {jwt_token}, Content-Type: multipart/form-data

请求参数（Form Data）:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| file | File | 是 | 头像文件（JPG/PNG, 最大 2MB） | avatar.jpg |

响应数据结构:
```
{
  code: number,       // 0=成功, 7003=上传失败, 7004=类型不支持
  message: string,
  data: {
    avatar: string,    // 新头像 URL
    fileId: string     // 文件 ID
  } | null
}
```

Mock 数据示例 1（上传成功）:
```
{
  "code": 0,
  "message": "头像上传成功",
  "data": {
    "avatar": "https://example.com/avatars/student01_20260703.jpg",
    "fileId": "3834567890123456789"
  },
  "success": true,
  "timestamp": "2026-07-03T10:11:00.123+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678917",
  "path": "/api/v1/user/avatar",
  "elapsed": 350
}
```

Mock 数据示例 2（文件类型不支持 - code=7003）:
```
{
  "code": 7003,
  "message": "不支持的文件类型，仅支持 JPG 和 PNG 格式",
  "data": null,
  "success": false,
  "timestamp": "2026-07-03T10:11:01.000+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678918",
  "path": "/api/v1/user/avatar",
  "elapsed": 5
}
```

Mock 数据示例 3（文件大小超限 - code=7004）:
```
{
  "code": 7004,
  "message": "文件大小不能超过2MB",
  "data": null,
  "success": false,
  "timestamp": "2026-07-03T10:11:02.000+08:00",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678919",
  "path": "/api/v1/user/avatar",
  "elapsed": 3
}
```

错误场景:
- 7003: 文件类型不支持 → ElMessage.error("不支持的文件类型，仅支持 JPG 和 PNG 格式")
- 7004: 文件大小超限 → ElMessage.error("图片大小不能超过 2MB")
- 5001: 服务端错误 → ElMessage.error("头像上传失败，请稍后重试")
- Network Error: → ElMessage.error("网络连接失败，请检查网络")

Mock 逻辑:
- 正常文件 → 返回成功，avatar URL 为固定 Mock URL
- 文件扩展名非 jpg/png → 返回 7003
- 文件大于 2MB → 返回 7004（前端应优先校验并拦截）

Mock 延迟: 500ms（模拟文件上传延迟）

---

# Section 7: Interaction Flows

## 交互 1: 正常加载个人中心

Given: 学生已登录，当前在 Dashboard 页面

When:
1. 学生点击侧边栏菜单"个人中心"
2. 路由导航到 /student/profile

Then:
1. 页面进入 Loading 状态：PageHeader 标题"个人中心"，内容区展示 LoadingState
2. GET /api/v1/auth/me 请求发送（或从 useUserStore 缓存获取）
3. Mock 返回学生用户信息（300ms 延迟）
4. Loading 退出
5. 基本信息卡片渲染:
   - 头像区: BaseAvatar 显示圆形头像（80px）。如有头像 URL 展示图片，无头像展示"张"（realName 首字符）
   - 头像下方显示"点击更换头像"提示文字 + "支持 JPG/PNG，最大 2MB"
   - 信息区（查看模式）: 真实姓名"张三"、学号"20240101001"（等宽字体）、邮箱"zhangsan@example.com"、手机号"13800138001"、班级"软件技术2401班"
   - 操作按钮区: "编辑信息"按钮（Pencil 图标 + 文字）
6. 修改密码卡片渲染:
   - 标题"修改密码"
   - 原密码输入框（placeholder="请输入原密码"）
   - 新密码输入框（placeholder="请输入新密码（6-20位）"）
   - 确认密码输入框（placeholder="请再次输入新密码"）
   - "修改密码"按钮
7. 所有表单输入框为空，等待用户操作

## 交互 2: 编辑并保存个人信息

Given: 学生在个人中心页，当前为查看模式

When:
1. 学生点击"编辑信息"按钮

Then:
1. 信息区切换为编辑模式: 真实姓名、邮箱、手机号变为 BaseInput 可编辑状态，内容预填当前值。学号和班级保持 disabled+readonly（灰色背景，不可编辑）
2. 操作按钮切换为"取消"（左侧）+ "保存"（右侧）

When（后续操作）:
3. 学生将邮箱修改为"zhangsan@newemail.com"
4. 学生将手机号修改为"13800138002"
5. 学生点击"保存"按钮

Then:
5. el-form 验证通过
6. "保存"按钮进入 Loading 状态（"保存中..."），所有输入框和"取消"按钮禁用
7. PUT /api/v1/user/profile 请求发送，Body: { realName: "张三", email: "zhangsan@newemail.com", phone: "13800138002" }
8. Mock 返回成功 (code=0)
9. 展示 ElMessage.success("个人信息更新成功")
10. 本地 userInfo 更新，信息区切换回查看模式，展示新的邮箱和手机号
11. "保存"按钮恢复，操作按钮切换回"编辑信息"

## 交互 3: 编辑后取消

Given: 学生在个人中心页，已进入编辑模式，修改了邮箱但未保存

When:
1. 学生点击"取消"按钮

Then:
1. isEditing 变为 false
2. editForm 恢复为编辑前的 userInfo 原始值
3. el-form 验证状态通过 formRef.resetFields() 清除
4. 信息区切换回查看模式，展示编辑前的原始信息（邮箱未变）
5. 操作按钮切换回"编辑信息"

## 交互 4: 上传头像

Given: 学生在个人中心页，当前为查看模式

When:
1. 学生点击 BaseAvatar 头像区域
2. 系统弹出文件选择对话框（仅显示 JPG/PNG 文件）
3. 学生选择一张本地图片"my_photo.jpg"（大小 1.5MB）

Then:
1. 前端校验: 文件类型正确（image/jpeg），大小 1.5MB <= 2MB，通过
2. 展示 ElMessage.info("头像上传中...")
3. POST /api/v1/user/avatar 请求发送（multipart/form-data，含文件）
4. Mock 返回成功 (code=0)，含新头像 URL
5. ElMessage.info 被 ElMessage.success("头像更新成功") 替换
6. avatarUrl 更新为新 URL，BaseAvatar 自动展示新头像图片
7. 页面其他部分不变（查看模式保持）

## 交互 5: 上传头像失败（文件过大）

Given: 学生在个人中心页

When:
1. 学生点击头像区域
2. 选择一张 3MB 的图片

Then:
1. 前端 BaseUpload 的 maxSize 校验（或 handleAvatarChange 中的手动校验）检测到文件超限
2. 展示 ElMessage.error("图片大小不能超过 2MB，当前文件大小 3MB")
3. 不发送 API 请求（前端拦截）
4. 头像保持不变

## 交互 6: 修改密码成功

Given: 学生在个人中心页，修改密码表单可见

When:
1. 学生在原密码输入框输入"oldPass123"
2. 学生在新密码输入框输入"newPass456"
3. 学生在确认密码输入框输入"newPass456"
4. 学生点击"修改密码"按钮

Then:
1. el-form 验证通过（所有字段非空，新密码>=6位，确认密码与新密码一致）
2. "修改密码"按钮进入 Loading 状态（"修改中..."），三个密码输入框禁用
3. PUT /api/v1/user/password 请求发送，Body: { oldPassword: "oldPass123", newPassword: "newPass456" }
4. Mock 返回成功 (code=0)
5. 展示 ElMessage.success("密码修改成功")
6. "修改密码"按钮恢复
7. 三个密码输入框全部清空
8. el-form 验证状态通过 formRef.resetFields() 清除
9. 学生可继续修改密码（如需）

## 交互 7: 修改密码失败（原密码错误）

Given: 学生在个人中心页

When:
1. 学生输入原密码"wrongpass"（Mock 中触发 code=2009 的关键词）
2. 学生输入新密码"newPass456"
3. 学生输入确认密码"newPass456"
4. 学生点击"修改密码"按钮

Then:
1. el-form 验证通过
2. "修改密码"按钮进入 Loading 状态
3. PUT /api/v1/user/password 请求发送
4. Mock 返回 code=2009, message="原密码错误，请重新输入"
5. "修改密码"按钮恢复
6. 展示 ElMessage.error("原密码错误，请重新输入")
7. 原密码输入框清空（oldPassword=""），新密码和确认密码保留
8. 学生需重新输入正确原密码后再次提交

## 交互 8: 修改密码失败（两次密码不一致）

Given: 学生在个人中心页

When:
1. 学生输入原密码"oldPass123"
2. 学生输入新密码"newPass456"
3. 学生输入确认密码"differentPass789"
4. 学生点击"修改密码"按钮

Then:
1. el-form 验证: confirmPassword 的 validator 检测到与 newPassword 不一致
2. 确认密码输入框下方展示红色错误提示"两次输入的密码不一致"
3. 不发送 API 请求（前端校验拦截）
4. "修改密码"按钮不进入 Loading 状态

---

# Section 8: Permission Design

| 属性 | 值 |
|------|-----|
| 页面权限标识 | student:profile:view |
| 页面允许角色 | ["student"] |
| 路由权限检查 | 路由配置 meta.roles = ["student"]，由 permission.ts 导航守卫检查 |
| 按钮级权限 | 无（个人中心页所有操作对所有学生可见且可执行） |

路由配置（追加到 src/router/routes/student.ts）:
```
{
  path: "profile",
  name: "StudentProfile",
  component: () => import("@/pages/student/ProfilePage.vue"),
  meta: {
    title: "个人中心",
    icon: "User",
    sort: 99,
    keepAlive: false,
    roles: ["student"]
  }
}
```

注意: meta.sort = 99 确保"个人中心"排在侧边栏最后。meta.keepAlive = false 确保每次进入都重新加载用户信息。

---

# Section 9: Acceptance Criteria

AC-1: Given 学生已登录 When 访问 /student/profile Then 页面展示个人中心，包含基本信息卡片（头像 + 真实姓名 + 学号 + 邮箱 + 手机号 + 班级，默认查看模式）和修改密码卡片（原密码 + 新密码 + 确认密码输入框 + 提交按钮）

AC-2: Given 用户无头像（avatar 为空字符串） When 页面加载 Then 头像区展示圆形默认头像，背景色由姓名 hash 生成，显示 realName 第一个字符

AC-3: Given 用户点击"编辑信息"按钮 When 进入编辑模式 Then 真实姓名、邮箱、手机号变为可编辑输入框（预填当前值），学号和班级保持不可编辑（灰色背景），操作按钮切换为"保存"+"取消"

AC-4: Given 用户修改信息后点击"保存" When API 返回成功 Then 展示"个人信息更新成功"提示，切换回查看模式，展示更新后的信息数据

AC-5: Given 用户进入编辑模式，修改了信息但点击"取消" When 取消操作 Then 恢复为编辑前的原始信息，切换回查看模式

AC-6: Given 用户点击头像区域 When 选择 JPG/PNG 文件（<=2MB）并上传成功 Then 头像更新为新图片，展示"头像更新成功"提示

AC-7: Given 用户选择超过 2MB 的图片 When 前端校验拦截 Then 展示"图片大小不能超过 2MB"错误提示，不发送上传请求

AC-8: Given 用户填写正确的原密码、新密码和确认密码 When 点击"修改密码"按钮并成功 Then 展示"密码修改成功"提示，三个密码输入框全部清空

AC-9: Given 用户填写错误的原密码 When 点击"修改密码"按钮 Then 展示"原密码错误"提示，原密码输入框清空，新密码和确认密码保留

AC-10: Given 用户两次输入的新密码不一致 When 点击"修改密码"按钮 Then 前端校验拦截，确认密码输入框展示"两次输入的密码不一致"红色提示，不发送 API 请求

---

# Section 10: Files to Create / Modify

## 新建文件

```
├── src/pages/student/ProfilePage.vue                  # 个人中心页面组件
├── src/api/modules/user.ts                            # 用户相关 API 函数（updateProfile、changePassword、uploadAvatar）
├── src/mock/modules/user.ts                           # 用户接口 Mock 数据（PUT /api/v1/user/profile、PUT /api/v1/user/password、POST /api/v1/user/avatar）
└── docs/page-analysis/student-profile.md              # 本文件
```

## 修改文件

```
├── src/router/routes/student.ts                       # 追加 StudentProfile 路由配置
├── src/mock/index.ts                                  # 汇总：引入 user Mock 模块
├── src/mock/modules/auth.ts                           # 修改：确保 GET /api/v1/auth/me 返回 studentNo 和 className 字段
├── src/types/user.ts                                  # 修改：IUserInfo 增加 studentNo、className 可选字段
└── src/stores/useUserStore.ts                         # 确认：userInfo State 包含 studentNo 和 className
```

## 依赖的已有组件（本页面不修改它们，但依赖它们的存在）

```
├── src/layouts/AppLayout.vue                          # 页面外层布局
├── src/components/layout/PageHeader.vue               # 页面标题栏
├── src/components/layout/PageContainer.vue            # 页面内容容器
├── src/components/base/BaseInput.vue                  # 表单输入框
├── src/components/base/BaseButton.vue                 # 操作按钮
├── src/components/base/BaseAvatar.vue                 # 头像展示组件
├── src/components/base/BaseUpload.vue                 # 头像上传组件（隐藏触发）
├── src/components/common/LoadingState.vue             # 加载中状态
├── src/components/common/ErrorState.vue               # 错误状态
├── src/stores/useUserStore.ts                         # 用户信息 Store
└── src/stores/useAppStore.ts                          # 应用全局状态 Store
```

## 类型定义修改（src/types/user.ts）

IUserInfo 增加字段:
- studentNo?: string（学号，学生角色专用）
- className?: string（班级名称，学生角色专用）

## 注意事项

1. 头像上传使用 BaseUpload 的隐藏触发模式。BaseUpload 渲染为 display:none，用户点击 BaseAvatar 时通过 ref 调用 BaseUpload 的 openFileDialog 方法触发文件选择。BaseUpload 仅负责文件选择和 change 事件通知，上传逻辑（FormData 构造、API 调用）由页面组件 handleAvatarChange 函数处理。
2. 修改密码成功后清空表单时，使用 el-form 的 resetFields() 方法而非手动清空 model 值。resetFields() 同时清除验证状态和表单值，确保无残留的错误提示。
3. 编辑模式下，"取消"按钮的行为是丢弃所有修改并恢复到编辑前的原始值。editForm 在 enterEditMode 时备份为原始值，cancelEdit 时恢复。
4. 信息编辑表单的验证规则使用 Element Plus 的 el-form rules，在 <script setup> 中定义为常量对象 profileRules。不引入第三方验证库。
5. 确认密码的"两次输入一致"校验通过自定义 validator 函数实现：validator(rule, value, callback) => value === passwordForm.newPassword ? callback() : callback(new Error('两次输入的密码不一致'))。
6. 如果 useUserStore 中已有用户信息且不为空，页面优先使用 Store 缓存数据（减少一次 API 请求）。但如果 Store 数据为空（如页面首次加载时 Store 尚未初始化），则发送 GET /api/v1/auth/me 请求。
7. BaseAvatar 的默认首字符通过 name prop 传入的 realName 取值。背景色由 BaseAvatar 内部根据 name 字符串 hash 生成（确保同一用户每次看到的背景色一致）。头像上传成功后 avatarUrl 更新，BaseAvatar 自动从 name 模式切换到 src 模式。
