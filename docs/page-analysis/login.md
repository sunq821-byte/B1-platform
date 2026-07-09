# Page Analysis: 登录页 (Login)

**文档版本**: v1.0
**创建日期**: 2026-07-02
**分析范围**: 登录页面及完整认证流程

---

# Section 1: Page Identity

| 属性 | 值 |
|------|-----|
| 页面名称 | 登录页 |
| 页面文件 | src/pages/auth/LoginPage.vue |
| 路由路径 | /login |
| 路由名称 | Login |
| 所属 Sprint | Sprint 1 |
| 所属 Feature | F-1-08（参见《Sprint 1 Spec》Section 3） |
| 页面角色 | 全部用户（公开页面，无需认证） |
| 页面复杂度 | L2（UI 简洁，但包含完整认证流程逻辑、记住密码、角色跳转分发） |
| 原型参考 | 参见《UI Design System v1.0》登录页原型 |

页面职责：用户输入凭据进行身份认证。登录成功后自动跳转到角色对应首页。本页面是全站唯一不需要认证即可访问的入口页面（404/403 除外）。

---

# Section 2: Page Layout Structure

登录页使用独立全屏居中布局，不嵌套在 AppLayout 中。整个页面分为两个视觉层次：背景层和登录卡片层。

```
+--------------------------------------------------+
|  全屏背景 (min-height: 100vh, bg: --color-bg)       |
|                                                    |
|                                                    |
|            +--------------------------+            |
|            |  登录卡片 (width: 420px)   |            |
|            |  bg: --color-card         |            |
|            |  border-radius: --radius-xl|           |
|            |  box-shadow: --shadow-xl  |            |
|            |  padding: --spacing-2xl   |            |
|            |                           |            |
|            |  [Brand 品牌区]            |            |
|            |  占位高度: 48px            |            |
|            |  内容: Logo 图标 + 项目名称 |           |
|            |  项目名称: --font-size-xl  |            |
|            |  副标题: --font-size-sm    |            |
|            |  color: --color-text-      |            |
|            |          secondary         |            |
|            |                           |            |
|            |  [间距: --spacing-xl]       |            |
|            |                           |            |
|            |  [LoginForm 表单区]         |            |
|            |  宽度: 100%                |            |
|            |  内容:                     |            |
|            |  - 用户名输入框 (el-input)  |            |
|            |  - 密码输入框 (el-input,    |            |
|            |    show-password)          |            |
|            |  - 记住密码 (el-checkbox)   |            |
|            |  - 登录按钮 (BaseButton,    |            |
|            |    width: 100%)            |            |
|            |                           |            |
|            |  [间距: --spacing-md]       |            |
|            |                           |            |
|            |  [Footer 底部]              |            |
|            |  赛事信息 + 版权文字         |            |
|            |  font-size: --font-size-xs |            |
|            |  color: --color-text-      |            |
|            |          placeholder       |            |
|            +--------------------------+            |
|                                                    |
+--------------------------------------------------+
```

**区域说明**：

| 区域 | 宽度 | 高度 | 对齐 | 滚动策略 | 说明 |
|------|------|------|------|---------|------|
| 背景层 | 100vw | 100vh (min) | - | 固定 | 使用 Design System `--color-bg` 背景色。不设背景图片 |
| 登录卡片 | 420px (固定) | auto (由内容撑开) | 水平垂直居中 | 随内容撑开 | 白色卡片。最小宽度保证在 320px 屏幕上不溢出 |
| 品牌区 | 100% 父容器 | auto | 水平居中 | 不滚动 | Logo 使用 BookOpen 图标（Lucide Icons，尺寸 48px，颜色 `--color-primary`）。项目名称使用 `--font-size-xl` + `--color-text-primary`。副标题"软件实训教学检查评价与报表系统"使用 `--font-size-sm` + `--color-text-secondary` |
| 表单区 | 100% 父容器 | auto | 左对齐 | 不滚动 | 用户名和密码输入框使用 Element Plus el-input（large 尺寸）。登录按钮使用 BaseButton（type=primary, size=large, width=100%） |
| 底部 | 100% 父容器 | auto | 水平居中 | 不滚动 | 展示"第十五届中国软件杯 B1 赛题"和版权信息 |

**响应式行为**：
- 1920px 分辨率：卡片尺寸 420px，居中展示
- 1366px 分辨率：卡片尺寸 420px，居中展示（卡片宽度在 1366px 下仍有充裕空间）
- 小于 480px（小屏手机）：卡片宽度改为 100% - 32px（margin: 16px），避免溢出

---

# Section 3: Component Tree & Specification

## 3.1 组件树

```
LoginPage.vue（页面组件，不使用 AppLayout）
├── <div> 背景容器（全屏居中 flex 布局）
│   └── <div> 登录卡片容器
│       ├── <div> 品牌区
│       │   ├── BookOpen 图标（Lucide Icons，直接使用 <BookOpen :size="48" />）
│       │   ├── <h1> 项目名称硬编码文字
│       │   └── <p> 副标题硬编码文字
│       ├── <form> 表单区（el-form，ref="loginFormRef"，model="loginForm"，rules="loginRules"）
│       │   ├── <el-form-item> 用户名输入项（prop="username"）
│       │   │   └── BaseInput（v-model="loginForm.username"，placeholder="请输入用户名"，size="large"，clearable，prefix-icon=User 图标）
│       │   ├── <el-form-item> 密码输入项（prop="password"）
│       │   │   └── BaseInput（v-model="loginForm.password"，type="password"，placeholder="请输入密码"，size="large"，show-password，prefix-icon=Lock 图标）
│       │   ├── <div> 操作行（flex: space-between）
│       │   │   ├── <el-checkbox> 记住密码（v-model="rememberMe"）
│       │   │   └── <span> 忘记密码链接（文字按钮，当前版本不跳转，hover 时 tooltip"请联系管理员重置密码"）
│       │   └── BaseButton（type="primary"，size="large"，loading="submitting"，@click="handleLogin"，block）
│       │       按钮文案：submitting 为 true 时展示"正在登录..."，否则展示"登录"
│       └── <div> 底部信息
│           ├── <p> 赛事信息："第十五届中国软件杯 · B1 赛题"
│           └── <p> 版权信息："© 2026 软件实训教学检查评价与报表系统"
```

页面不需要任何 Slots 和 Emits（本页面是叶子节点，不对外提供组件接口）。

## 3.2 本页面需要新建的子组件

**本页面无新建子组件**。登录页直接使用 Sprint 1 的 Base 组件（BaseInput、BaseButton）和 Element Plus 原生组件（el-form、el-form-item、el-checkbox）组装而成。登录页的业务逻辑（表单验证、登录调用、记住密码）内聚在页面组件中，不超过 150 行，无需抽取 Composable。

如果后续登录页增加更多交互（如验证码、SSO 跳转、MFA），再将业务逻辑抽取到 `src/composables/useLogin.ts`。当前 MVP 阶段保持简单。

## 3.3 已有组件引用

**引用组件 1: BaseInput（来自 Sprint 1 Base Components，src/components/base/BaseInput.vue）**

关键 Props 配置：
| Prop | 值 | 说明 |
|------|-----|------|
| modelValue | loginForm.username / loginForm.password | 双向绑定 |
| placeholder | "请输入用户名" / "请输入密码" | 占位提示 |
| size | "large" | 统一大尺寸 |
| clearable | true（用户名） / false（密码） | 用户名可一键清除 |
| type | "text"（用户名） / "password"（密码） | 密码框类型 |
| showPassword | true（仅密码框） | 密码可见切换 |
| prefixIcon | User 图标 / Lock 图标 | 输入框前缀图标（通过 slot 或 el-input 原生的 prefix-icon） |

注意：BaseInput 是 el-input 的封装。prefix-icon 如果 BaseInput 未明确暴露，则通过 `v-bind="$attrs"` 透传给 el-input。本页面使用 `<template #prefix>` 插槽方式透传 Lucide 图标以保证图标样式统一。

**引用组件 2: BaseButton（来自 Sprint 1 Base Components，src/components/base/BaseButton.vue）**

关键 Props 配置：
| Prop | 值 | 说明 |
|------|-----|------|
| type | "primary" | 使用 Design System `--color-primary` |
| size | "large" | 登录按钮为大尺寸 |
| loading | submitting | 提交中禁用并展示 loading 动画 |
| nativeType | "submit" | 触发表单原生 submit 事件 |

**引用组件 3: el-form（来自 Element Plus，无需封装）**

关键配置：
- model: loginForm（响应式表单数据对象）
- rules: loginRules（验证规则对象）
- ref: loginFormRef（用于调用 validate 方法）
- label-position: "top"（标签在输入框上方，视觉简洁）
- status-icon: true（验证失败时展示状态图标）

**引用组件 4: el-checkbox（来自 Element Plus，无需封装）**

关键配置：
- v-model: rememberMe（记住密码勾选状态）
- label: "记住密码"

---

# Section 4: State Design（页面级）

| 状态 | 触发条件 | UI 表现 | 持续时间 | 退出条件 |
|------|---------|--------|---------|---------|
| Loading | 用户点击登录按钮，表单验证通过，开始调用 login API | 登录按钮进入 loading 状态，按钮文字变为"正在登录..."，按钮禁用。用户名和密码输入框禁用（防止编辑）。el-form 的 status-icon 隐藏 | 直到 API 返回结果（Mock 约 500ms，超时 15s） | API 返回成功或失败 |
| Empty | 不适用 | 登录页无空数据场景 | - | - |
| Error | 登录 API 返回错误（code≠0） | 展示 ElMessage.error。文案根据错误码：(a) code=2004 用户名或密码错误："用户名或密码错误，请重试"；(b) code=2006 账号被禁用："账号已被禁用，请联系管理员"；(c) code=2005 账号被锁定："账号已被临时锁定，请 15 分钟后重试"；(d) 网络错误："网络连接失败，请检查网络"；(e) code=5001 服务端错误："服务异常，请稍后重试"。密码输入框清空（安全考虑：不保留错误密码）。用户名输入框保留已输入内容。登录按钮恢复可点击状态。输入框恢复可编辑状态 | ElMessage.error 展示 3 秒后自动消失 | 用户修正信息后重新点击登录 |
| Success | 登录 API 返回成功（code=0），User Store 完成 token/refreshToken/userInfo 存储 | 展示 ElMessage.success("登录成功")。1.5 秒后自动跳转。跳转前不展示过渡页面（直接跳转）。跳转目标：redirect 参数存在 → 跳转到 redirect 路径；学生 → /student/dashboard；教师 → /teacher/dashboard；管理员 → /admin/dashboard | ElMessage.success 展示 1.5 秒 | 页面自动跳转（跳转后本页面被销毁） |
| NoPermission | 不适用 | 登录页无权限限制，所有用户均可访问。已登录用户访问 /login 时由路由守卫自动重定向到角色首页，不会到达本页面 | - | - |
| Offline | 浏览器 navigator.onLine 变为 false（用户网络断开） | 登录卡片上方（卡片外部）展示黄色提示条："网络连接已断开，请检查网络"。提示条高度 44px，宽度与卡片一致，使用 `--color-warning-light` 背景 + `--color-warning` 文字。登录按钮仍可点击，但点击后 API 调用失败，展示"网络连接失败，请检查网络"错误 | 直到网络恢复 | navigator.onLine 变为 true，提示条自动消失 |

**Offline 状态的实现细节**：
- 在 LoginPage 的 onMounted 中注册 window 'online' 和 'offline' 事件监听
- 在 onUnmounted 中移除监听，防止内存泄漏
- 离线提示条使用 CSS transition（`--transition-normal`）做淡入淡出动画

---

# Section 5: Data Flow

## 5.1 页面加载数据流

```
用户导航到 /login
→ 路由守卫 beforeEach 触发
→ auth.ts: 从 User Store 获取 token → token 存在且有效 → 重定向到角色首页（不展示登录页）
→ auth.ts: token 不存在或无效 → 放行（公开路由）
→ LoginPage.vue 渲染
→ onMounted:
  → 注册 window online/offline 事件监听
  → 检查 localStorage 是否有 remembered_credentials
  → 有: 自动填充 username + password → 设置 rememberMe = true
  → 无: 保持空白表单
→ 页面展示（Success 状态：空白表单等待用户输入）
```

## 5.2 登录提交数据流

```
操作: 用户登录

触发: 用户填写用户名和密码后，点击"登录"按钮（或按 Enter 键）
→ el-form 的 validate() 方法验证表单
→ 验证失败: 展示表单字段级别的红色错误提示（Element Plus 原生行为），数据流终止
→ 验证通过:
  1. submitting = true（按钮 Loading + 输入框禁用）
  2. 调用 useUserStore().login(loginForm.username, loginForm.password)
  3. User Store login Action:
     a. 调用 authApi.login(username, password)
     b. Axios 发送 POST /api/v1/auth/login { username, password }
     c. Mock 层拦截（VITE_USE_MOCK=true）
     d. Mock 返回成功数据: { token, refreshToken, userInfo }
     e. User Store 更新 token, refreshToken, userInfo State
  4. login Action 返回成功
  5. submitting = false
  6. 如果 rememberMe 为 true:
     → localStorage.setItem('remembered_credentials', JSON.stringify({ username, password: btoa(password) }))
  7. 如果 rememberMe 为 false:
     → 清除 localStorage 中的 remembered_credentials
  8. ElMessage.success("登录成功")
  9. setTimeout 1500ms
  10. 根据 userInfo.role 计算跳转路径
  11. router.push(redirectPath || roleHomePath)
  12. 路由跳转触发导航守卫 → 权限加载 → Dashboard 渲染
```

## 5.3 登录失败数据流

```
操作: 登录失败（错误处理）

触发: 登录 API 返回错误
→ User Store login Action 抛出异常
→ LoginPage 的 handleLogin 中 catch 错误:
  1. submitting = false（恢复按钮和输入框）
  2. 根据错误码展示 ElMessage.error:
     - code=2004: "用户名或密码错误，请重试"
     - code=2006: "账号已被禁用，请联系管理员"
     - code=2005: "账号已被临时锁定，请 15 分钟后重试"
     - Network Error: "网络连接失败，请检查网络"
     - code=5001: "服务异常，请稍后重试"
     - 其他: 使用后端返回的 message 字段内容
  3. 清空密码输入框（loginForm.password = ''）
  4. 保留用户名输入框内容
  5. 不跳转（用户可修正后重新提交）
```

## 5.4 记住密码数据流

```
操作: 记住密码自动填充

触发: 页面 onMounted
→ 读取 localStorage.getItem('remembered_credentials')
→ 无数据: 不填充
→ 有数据:
  1. 解析 JSON: { username, password }
  2. password = atob(password)（Base64 解码）
  3. loginForm.username = username
  4. loginForm.password = password
  5. rememberMe = true
```

---

# Section 6: API & Mock Specification（页面级精确值）

## 接口 1: POST /api/v1/auth/login

用途: 用户登录认证

请求参数:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| username | string | 是 | 用户名 | "student01" |
| password | string | 是 | 密码（明文） | "password123" |

请求 Content-Type: application/json

响应数据结构:
```
{
  code: number,          // 0=成功, 2004=密码错误, 2006=账号禁用, 2005=账号锁定
  message: string,       // 提示信息
  data: {
    token: string,       // JWT Access Token（2 小时有效）
    refreshToken: string, // JWT Refresh Token（7 天有效）
    userInfo: {
      id: number,        // 用户 ID
      username: string,  // 用户名
      realName: string,  // 真实姓名
      role: string,      // 角色: student / teacher / admin
      avatar: string,    // 头像 URL（空字符串表示无头像）
      email: string,     // 邮箱
      phone: string      // 手机号
    }
  } | null                // 失败时 data 为 null
}
```

Mock 数据示例 1（学生登录成功）:
```
{
  "code": 0,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.mock_student_access_token",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.mock_student_refresh_token",
    "userInfo": {
      "id": 1,
      "username": "student01",
      "realName": "张三",
      "role": "student",
      "avatar": "",
      "email": "zhangsan@example.com",
      "phone": "13800138001"
    }
  }
}
```

Mock 数据示例 2（教师登录成功）:
```
{
  "code": 0,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.mock_teacher_access_token",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.mock_teacher_refresh_token",
    "userInfo": {
      "id": 2,
      "username": "teacher01",
      "realName": "李老师",
      "role": "teacher",
      "avatar": "",
      "email": "lilaoshi@example.com",
      "phone": "13900139001"
    }
  }
}
```

Mock 数据示例 3（管理员登录成功）:
```
{
  "code": 0,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.mock_admin_access_token",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.mock_admin_refresh_token",
    "userInfo": {
      "id": 4,
      "username": "admin01",
      "realName": "赵管理员",
      "role": "admin",
      "avatar": "",
      "email": "zhaoadmin@example.com",
      "phone": "13600136001"
    }
  }
}
```

Mock 数据示例 4（登录失败 - 密码错误）:
```
{
  "code": 2004,
  "message": "用户名或密码错误",
  "data": null
}
```

Mock 数据示例 5（登录失败 - 账号禁用）:
```
{
  "code": 2006,
  "message": "账号已被禁用，请联系管理员",
  "data": null
}
```

Mock 数据示例 6（登录失败 - 账号锁定）:
```
{
  "code": 2005,
  "message": "账号已被临时锁定，请 15 分钟后重试",
  "data": null
}
```

Mock 错误场景:
- 2004: { "code": 2004, "message": "用户名或密码错误", "data": null } → ElMessage.error("用户名或密码错误，请重试")，密码输入框清空
- 2006: { "code": 2006, "message": "账号已被禁用，请联系管理员", "data": null } → ElMessage.error("账号已被禁用，请联系管理员")
- 2005: { "code": 2005, "message": "账号已被临时锁定，请 15 分钟后重试", "data": null } → ElMessage.error("账号已被临时锁定，请 15 分钟后重试")
- 5001: 响应体: { "code": 5001, "message": "服务器繁忙，请稍后重试", "data": null } → ElMessage.error("服务异常，请稍后重试")
- Network Error: 无响应体，Axios 捕获网络错误 → ElMessage.error("网络连接失败，请检查网络")

Mock 延迟: 500ms（所有登录 Mock 统一使用 500ms 延迟，模拟真实网络延迟和身份验证处理时间）

Mock 实现位置: src/mock/auth.ts

Mock 逻辑: 根据请求体中的 username 字段分发 Mock 响应：
- username 以 "student" 开头 → 返回学生角色数据
- username 以 "teacher" 开头 → 返回教师角色数据
- username 以 "admin" 开头 → 返回管理员角色数据
- username 为 "locked" → 返回 2005 错误
- username 为 "disabled" → 返回 2006 错误
- 其他 → 返回 2004 错误（密码错误）

此 Mock 逻辑覆盖了三种角色的登录成功场景、三种登录失败场景和一种网络错误场景。

---

# Section 7: Interaction Flows

## 交互 1: 正常登录流程（学生）

Given: 用户在 /login 页面，表单为空，未勾选"记住密码"

When:
1. 用户在用户名输入框输入"student01"
2. 用户在密码输入框输入"password123"
3. 用户点击"登录"按钮（或按 Enter 键）

Then:
1. el-form 表单验证通过（两个字段均非空）
2. 登录按钮进入 Loading 状态，文字变为"正在登录..."
3. 用户名和密码输入框变为 disabled 状态
4. Mock 层拦截 POST /api/v1/auth/login，500ms 后返回学生登录成功数据
5. User Store 的 token、refreshToken、userInfo 被更新
6. 登录按钮恢复为正常状态
7. 展示 ElMessage.success("登录成功")，持续 1.5 秒
8. 1.5 秒后 router.push("/student/dashboard")
9. 路由守卫触发，加载学生权限，渲染学生 Dashboard

## 交互 2: 登录失败 - 密码错误

Given: 用户在 /login 页面，已输入用户名"student01"和错误密码"wrongpassword"

When:
1. 用户点击"登录"按钮
2. Mock 根据 username 判断非已知测试账号 → 返回 2004

Then:
1. 登录按钮恢复为可点击状态（退出 Loading）
2. 用户名和密码输入框恢复为可编辑状态
3. 展示 ElMessage.error("用户名或密码错误，请重试")
4. 密码输入框内容被清空（loginForm.password = ''）
5. 用户名输入框保留"student01"内容（不清空）
6. 用户可修改密码后重新提交
7. 不跳转页面

## 交互 3: 记住密码自动填充

Given: 用户上一次登录时勾选了"记住密码"并登录成功（凭据已存储到 localStorage）

When:
1. 用户打开浏览器访问 /login
2. LoginPage 组件 onMounted 执行

Then:
1. localStorage 中读取到 remembered_credentials
2. 用户名输入框自动填充为上次的用户名
3. 密码输入框自动填充为 Base64 解码后的密码
4. "记住密码"复选框为勾选状态
5. 用户可直接点击"登录"（无需重新输入）

## 交互 4: 已登录用户访问登录页

Given: 用户已登录（User Store 的 token 有效），当前在 Dashboard 页面

When:
1. 用户在地址栏手动输入 /login 并回车

Then:
1. 路由守卫 beforeEach 触发
2. auth.ts 检查 User Store → token 存在且有效
3. 路由守卫调用 router.push(roleHomePath) 重定向
4. 浏览器地址栏变为角色首页 URL
5. 登录页根本不会被渲染

## 交互 5: 登录时网络断开

Given: 用户在 /login 页面，已输入用户名和密码

When:
1. 用户点击"登录"按钮
2. 请求发送后网络断开（Mock 层模拟 Network Error：延迟 500ms 后抛出无响应错误）

Then:
1. 登录按钮恢复为可点击状态
2. 密码输入框清空
3. 展示 ElMessage.error("网络连接失败，请检查网络")
4. 如果浏览器检测到 offline 事件，卡片上方展示黄色"网络连接已断开"提示条
5. 用户恢复网络后，黄色提示条自动消失
6. 用户可重新输入密码并登录

## 交互 6: 表单验证失败

Given: 用户在 /login 页面

When:
1. 用户未输入用户名，直接在密码框输入"password123"
2. 用户点击"登录"按钮

Then:
1. el-form 的 validate() 方法检测到用户名为空（验证规则: required: true）
2. 用户名输入框下方展示红色错误提示"请输入用户名"
3. 密码输入框不变
4. 不触发 API 请求（未通过前端校验）
5. 登录按钮不进入 Loading 状态

---

# Section 8: Permission Design

登录页是全站公开页面，不涉及权限控制。

| 属性 | 值 |
|------|-----|
| 页面权限标识 | 无（公开页面） |
| 页面允许角色 | []（空数组，所有用户可访问，含未登录用户） |
| 路由权限检查 | meta.roles = [] |
| 按钮级权限 | 无（登录页无权限差异化按钮） |

登录页的路由配置（参见《Sprint 1 Spec》Section 6）：
- 路由 meta.hidden = true（不展示在侧边栏菜单）
- 路由 meta.roles = []（公开路由，导航守卫放行未认证用户）

已登录用户访问 /login 的行为由导航守卫 auth.ts 控制：检测到 Token 有效 → 重定向到角色首页，不渲染登录页。

---

# Section 9: Acceptance Criteria

AC-1: Given 用户在浏览器打开 /login When 登录页加载完成 Then 页面展示居中白色卡片，包含品牌区（Logo + 项目名称 + 副标题）、用户名输入框、密码输入框、"记住密码"复选框、"登录"按钮、底部赛事信息。页面背景为 `--color-bg`，卡片无侧边栏和顶部导航

AC-2: Given 用户输入正确的测试账号凭据（如 student01 / password123）When 点击"登录"按钮 Then 登录按钮进入 Loading 状态（"正在登录..."），输入框禁用。Mock 返回成功后展示"登录成功"提示，1.5 秒后自动跳转到 /student/dashboard

AC-3: Given 用户输入错误的密码 When 点击"登录"按钮 Then 展示"用户名或密码错误，请重试"错误提示。密码输入框清空，用户名输入框保留已输入内容，登录按钮恢复可点击状态，页面不跳转

AC-4: Given 用户勾选"记住密码"并登录成功 When 用户退出登录后重新访问 /login Then 用户名和密码自动填充，"记住密码"保持勾选状态

AC-5: Given 用户已登录（Token 有效）When 用户手动在地址栏输入 /login 并回车 Then 自动重定向到角色对应首页，不展示登录页

AC-6: Given 登录页已加载 When 用户网络断开 Then 卡片上方展示黄色"网络连接已断开，请检查网络"提示条。网络恢复后提示条自动消失

AC-7: Given 用户未输入用户名 When 直接点击"登录"按钮 Then 用户名输入框下方展示红色"请输入用户名"验证提示，不发送 API 请求

---

# Section 10: Files to Create / Modify

## 新建文件

```
├── src/pages/auth/LoginPage.vue          # 登录页面组件
├── src/api/auth.ts                        # 认证 API 函数（login、refreshToken、logout、fetchUserInfo）
├── src/mock/auth.ts                       # 认证接口 Mock 数据（login、refreshToken、logout、fetchUserInfo、fetchPermissions）
└── docs/page-analysis/login.md            # 本文件
```

## 修改文件

```
├── src/router/routes/public.ts            # 追加 Login 路由（/login → LoginPage.vue）和 NotFound 路由
├── src/router/guards/auth.ts              # 新建：认证守卫（Token 检查逻辑）
├── src/router/guards/permission.ts        # 新建：权限守卫（权限加载 + 动态路由添加）
├── src/router/index.ts                    # 修改：注册公开路由、注册导航守卫 beforeEach
├── src/stores/useUserStore.ts             # 修改：重构为完整的 User Store（当前为 useAuthStore 骨架，需重命名并按 Feature F-1-03 完整实现）
├── src/stores/usePermissionStore.ts       # 新建：权限 Store
├── src/stores/useAppStore.ts              # 新建：应用 Store
├── src/types/user.ts                      # 确认 IUserInfo 类型字段完整（当前已有多数字段，需补充 refreshToken 相关类型）
├── src/mock/index.ts                      # 修改：汇总所有 Mock 模块入口
└── src/main.ts                            # 确认：Mock 条件引入逻辑（VITE_USE_MOCK=true 时引入 mock/index.ts）
```

## 依赖的 Sprint 1 组件（本页面不修改它们，但依赖它们的存在）

```
├── src/components/base/BaseInput.vue      # 用户名和密码输入框
├── src/components/base/BaseButton.vue     # 登录按钮
├── src/components/common/ErrorState.vue   # （不直接在登录页使用，但属于 Sprint 1 基础设施）
├── src/components/common/LoadingState.vue # （不直接在登录页使用）
├── src/components/common/EmptyState.vue   # （不直接在登录页使用）
├── src/components/common/NoPermission.vue # （不直接在登录页使用）
```

## 注意事项

1. LoginPage.vue 是首个实现页面，其开发顺序在 Layout 组件和 Common 组件完成之后。但在开发 LoginPage 之前，需要先完成 F-1-03（Store）、F-1-02（HTTP 层）、F-1-04（路由框架）、F-1-07（Base 组件）。
2. 登录页开发不需要等待 F-1-05（Layout 组件）和 F-1-06（Common 组件）完成，因为登录页不使用 AppLayout。
3. 登录页的记住密码功能使用 Base64 编码存储密码，安全性较低。在 MVP 阶段可接受，后续可替换为更安全的方案（如 AES 加密 + 服务端校验）。
4. 当前版本"忘记密码"功能不做实现，链接展示 tooltip 提示"请联系管理员重置密码"。此功能属于 Sprint 5 管理员模块范围。
