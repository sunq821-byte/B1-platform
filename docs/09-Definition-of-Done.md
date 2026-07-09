# Definition of Done v1.0

## 基于大模型的软件实训教学检查评价与报表系统

---

**文档版本**：v1.0

**适用范围**：所有页面、Sprint 和项目交付的完成标准

**使用方式**：每完成一个页面或 Sprint 后，逐项核对本清单。所有项目必须打勾才算完成。

---

# Part A: Page-Level DoD（单页面完成标准）

每个页面开发完成后必须通过以下所有检查项。

## A.1 功能完整性

```
□ A.1.1  页面所有 AC（Acceptance Criteria）均已实现。
          来源：Page Analysis Section 9
          验证：逐条执行 Given/When/Then，确认预期结果与实际行为一致。

□ A.1.2  页面所有交互流程均已实现。
          来源：Page Analysis Section 7
          验证：按交互流程描述逐一操作，确认每一步的行为与文档一致。

□ A.1.3  页面所有按钮和操作均可用（或按权限正确隐藏/禁用）。
          验证：以不同角色登录，确认按钮权限矩阵正确。
```

## A.2 状态覆盖

```
□ A.2.1  Loading 状态已实现。
          验证：清除浏览器缓存，首次加载页面，确认展示骨架屏或 Loading 动画而非空白。

□ A.2.2  Empty 状态已实现。
          验证：Mock 返回空数据（list=[]），确认展示空状态组件。
          空状态必须包含：插图 + 说明文案 + 引导操作按钮。
          禁止仅展示"暂无数据"文字。

□ A.2.3  Error 状态已实现。
          验证：Mock 返回 500 错误，确认展示 Error 组件。
          Error 组件必须包含：错误原因 + 重试按钮。
          禁止静默失败。

□ A.2.4  Success 状态已实现。
          验证：Mock 返回正常数据，确认页面按设计展示。

□ A.2.5  NoPermission 状态已实现（如适用）。
          验证：以无权限角色访问页面 URL，确认展示 NoPermission 页面或重定向。
          如页面无权限限制，此项标注 N/A。

□ A.2.6  Offline 状态已实现。
          验证：打开 DevTools Network > Offline，确认展示网络断开提示。
          恢复网络后提示自动消失并刷新数据。
```

## A.3 表单完整性（如页面包含表单）

```
□ A.3.1  新增模式：默认值正确，必填字段标记星号，提交按钮文案为"创建"或"提交"。

□ A.3.2  编辑模式：数据正确回填，主键字段只读，提交按钮文案为"保存"。

□ A.3.3  查看模式：所有字段只读，无提交按钮，可提供"编辑"按钮。

□ A.3.4  提交中：按钮 Loading + 禁用，防止重复提交。

□ A.3.5  提交成功：ElMessage.success 提示，列表刷新或页面跳转。

□ A.3.6  提交失败：ElMessage.error 提示错误原因，表单数据保留不清空。

□ A.3.7  表单重置：弹出确认对话框"确定要重置吗？"。
```

## A.4 AI 功能完整性（如页面包含 AI 功能）

```
□ A.4.1  AI 分析前展示预计耗时提示。

□ A.4.2  AI 分析中展示分阶段进度。

□ A.4.3  AI 结果展示前校验 JSON 格式。

□ A.4.4  AI 结果提供"确认"和"驳回"操作。

□ A.4.5  AI 结果不可不经确认直接生效。

□ A.4.6  AI 错误提供重试按钮和错误原因。

□ A.4.7  AI 调用展示 Token 消耗。
```

## A.5 代码质量

```
□ A.5.1  ESLint 检查零错误。
          命令：npm run lint
          不允许：任何 error 级别问题。

□ A.5.2  TypeScript 类型检查零错误。
          命令：vue-tsc --noEmit
          不允许：any 类型（除非有明确的行内注释说明理由）。

□ A.5.3  组件行数不超过 300 行。
          超过 300 行的组件必须拆分。
          验证：wc -l src/components/xxx/ComponentName.vue

□ A.5.4  页面组件不包含超过 100 行业务逻辑。
          业务逻辑已抽取到 Composable 或 Store。

□ A.5.5  无硬编码的颜色值、间距值、字体大小。
          全部使用 Design System Token（CSS 变量）。

□ A.5.6  无魔法数字。
          所有数字常量已提取到 constants/ 或使用具名变量。

□ A.5.7  页面未直接 import axios。
          所有 API 调用通过 api/ 层。

□ A.5.8  页面未直接操作 localStorage / sessionStorage。
          持久化逻辑在 Store 或 Utils 中。
```

## A.6 文件与结构

```
□ A.6.1  所有新建文件在正确的目录位置。
          核对 Page Analysis Section 10 的文件清单。

□ A.6.2  文件命名符合 Frontend Specification 规范。
          组件: PascalCase.vue
          目录: kebab-case
          Store: use 前缀 + camelCase
          API 函数: camelCase

□ A.6.3  组件导入路径使用 @ 别名（@/components/xxx），不使用相对路径（../../../components/xxx）。

□ A.6.4  类型导入使用 type import：import type { IUser } from '@/types/user'。
```

## A.7 Mock 数据

```
□ A.7.1  Mock 数据字段名与 API Mock Specification 完全一致（含大小写）。

□ A.7.2  Mock 数据字段类型与 API Mock Specification 一致。

□ A.7.3  Mock 数据的嵌套结构与 API Mock Specification 一致。

□ A.7.4  Mock 覆盖三种响应场景：
          正常数据（200 + data）
          空数据（200 + [] 或 {}）
          服务端错误（500）

□ A.7.5  Mock 延迟配置正确：正常 200ms，慢网络 2000ms。
```

## A.8 UI 与交互

```
□ A.8.1  页面在 1920px 分辨率下布局正确。
          验证：调整浏览器窗口到 1920px 宽度，截图对比原型。

□ A.8.2  页面在 1366px 分辨率下布局正确。
          验证：调整浏览器窗口到 1366px 宽度。
          侧边栏默认折叠 / 表格横向滚动 / 表单保持可操作。

□ A.8.3  颜色、字体、间距、圆角、阴影与 UI Design System 一致。
          使用 DevTools 检查 CSS 变量值，不应出现非 Design Token 的硬编码值。

□ A.8.4  所有可点击元素有 hover 和 active 状态反馈。

□ A.8.5  所有 disabled 元素有明显的不可用视觉样式。

□ A.8.6  表单必填字段有红色星号标记。

□ A.8.7  面包屑正确展示当前页面的层级路径。

□ A.8.8  浏览器 Tab 标题为"{页面标题} - 软件实训教学检查评价与报表系统"。
```

## A.9 权限

```
□ A.9.1  路由 meta.roles 已正确配置。

□ A.9.2  菜单项按角色正确显示/隐藏。

□ A.9.3  按钮按权限正确显示（有权限）/ 隐藏（无权限）。

□ A.9.4  手动输入无权限 URL 时正确展示 NoPermission 页面或重定向。
```

## A.10 页面文档

```
□ A.10.1  Page Analysis 已归档到 docs/page-analysis/<page-name>.md。

□ A.10.2  如有新增组件，已回写 Component Library。

□ A.10.3  如有新增 Mock 接口，已回写 API Mock Specification。
```

---

# Part B: Sprint-Level DoD（单 Sprint 完成标准）

每个 Sprint 结束后必须通过以下所有检查项。

## B.1 页面完整性

```
□ B.1.1  Sprint 内所有页面的 Page-Level DoD 全部通过。
          核对 Sprint Spec Section 2 的页面列表。

□ B.1.2  Sprint 内所有 Feature 的 AC 全部通过。
          核对 Sprint Spec Section 3 的 Feature 列表。

□ B.1.3  Sprint 内所有命名组件均已实现。
          核对 Sprint Spec Section 4 的组件列表。
```

## B.2 流程完整性

```
□ B.2.1  Sprint 内的跨页面业务流程可完整走通（Mock 模式下）。
          例如 Sprint 3 教师端：
          登录(教师) → Dashboard → 课程管理 → 创建实训任务 → 查看学生提交 → AI 分析 → 教师复核 → 成绩发布

□ B.2.2  页面间导航正确（路由跳转、面包屑更新、Tab 标签页切换）。

□ B.2.3  KeepAlive 缓存策略正确。
          列表页缓存保留筛选条件和滚动位置。
          详情页不缓存。
```

## B.3 角色一致性

```
□ B.3.1  Sprint 所属角色的所有菜单项正确展示。

□ B.3.2  Sprint 所属角色的所有路由正确注册。

□ B.3.3  Sprint 所属角色的权限矩阵在页面级别正确。
```

## B.4 技术债务

```
□ B.4.1  Sprint 内无 ESLint Warning（Warning 视同阻断）。

□ B.4.2  Sprint 内无 TypeScript 类型错误。

□ B.4.3  Sprint 内无被注释掉的代码（dead code）。

□ B.4.4  Sprint 内无 console.log（除开发调试用 vite-plugin 自动移除）。

□ B.4.5  Sprint 内无 TODO 或 FIXME（如需保留，必须带负责人和日期）。
```

## B.5 Sprint 文档

```
□ B.5.1  Sprint Spec 已更新（如有与实际实现不一致的地方）。

□ B.5.2  Sprint 内所有 Page Analysis 已归档。

□ B.5.3  如有新增全局类型，已更新 types/ 目录。

□ B.5.4  如有新增环境变量，已更新 .env.development 和 .env.production。
```

## B.6 性能基线

```
□ B.6.1  FCP（首屏加载时间）不超过基线值 110%。
          基线：Sprint 1 结束后记录的首屏加载时间。

□ B.6.2  打包体积（gzip 后）不超过基线值 110%。
          基线：Sprint 1 结束后记录的打包体积。

□ B.6.3  无明显的页面性能退化（路由切换时间 < 500ms）。
```

---

# Part C: Project-Level DoD（项目完成标准）

全部 Sprint 完成后，项目交付前必须通过以下所有检查项。

## C.1 功能完整性

```
□ C.1.1  全部 35 个页面通过 Page-Level DoD。

□ C.1.2  三角色全部业务流程可完整走通（联调模式下，关闭 Mock）。
          学生：登录 → 查看课程 → 查看任务 → 提交代码 → 查看成绩
          教师：登录 → 创建课程 → 创建任务 → 查看提交 → AI 分析 → 复核评分 → 发布成绩 → 学院报表 → 评价标准管理
          管理员：登录 → 用户管理 → 系统配置 → 日志查看 → 系统监控

□ C.1.3  AI 模块的流式响应（SSE）在真实后端联调下正常。

□ C.1.4  文件上传/下载功能在真实后端联调下正常。

□ C.1.5  所有权限矩阵在后端联调下验证通过。
```

## C.2 性能

```
□ C.2.1  Lighthouse Performance Score >= 90。

□ C.2.2  FCP < 2s（Lighthouse 测量）。

□ C.2.3  LCP < 3s（Lighthouse 测量）。

□ C.2.4  FID < 100ms（Lighthouse 测量）。

□ C.2.5  CLS < 0.1（Lighthouse 测量）。

□ C.2.6  打包总体积（gzip 后）< 300KB。

□ C.2.7  所有路由组件已配置为异步加载（defineAsyncComponent 或动态 import）。
```

## C.3 兼容性

```
□ C.3.1  Chrome 最新版：全功能正常。

□ C.3.2  Edge 最新版：全功能正常。

□ C.3.3  银河麒麟自带浏览器：核心功能正常（允许 CSS 降级，不要求像素级一致）。
```

## C.4 安全

```
□ C.4.1  Token 不存储在 localStorage。

□ C.4.2  无 v-html 渲染用户输入内容。

□ C.4.3  前端权限仅做 UI 控制，后端是最终权威。

□ C.4.4  生产环境 VITE_USE_MOCK=false。

□ C.4.5  生产环境 console.log 已移除（Vite build 自动处理）。

□ C.4.6  无敏感信息（密码、Token、API Key）出现在前端代码或环境变量文件中。
```

## C.5 部署

```
□ C.5.1  Docker 镜像构建成功。

□ C.5.2  Nginx 配置正确（SPA 路由 fallback、API 代理、gzip、缓存策略）。

□ C.5.3  在目标环境（银河麒麟 + LoongArch）启动成功并可通过浏览器访问。

□ C.5.4  环境变量配置正确（生产环境 API_BASE_URL 指向实际后端）。
```

## C.6 文档

```
□ C.6.1  全部 35 个页面的 Page Analysis 已归档。

□ C.6.2  全部 7 个 Sprint Spec 已归档。

□ C.6.3  FIP v1.0 与实际实施一致（如有差异已更新）。

□ C.6.4  Component Library 已包含所有实际开发的组件。

□ C.6.5  API Mock Specification 已包含所有实际使用的 Mock 接口。

□ C.6.6  用户操作手册已交付（面向三角色）。

□ C.6.7  竞赛文档包已整理（PRD + SDS + Design System + Component Library + Frontend Spec + FIP + Sprint Specs）。
```

---

# DoD 执行规则

1. **页面级 DoD**：每个页面开发完成后（Step 4 之后），开发者自检，Review 者复核。
2. **Sprint 级 DoD**：每个 Sprint 结束后，团队集体检查。所有页面 DoD 通过是 Sprint DoD 的前提。
3. **项目级 DoD**：全部 Sprint 完成后，团队集体检查。所有 Sprint DoD 通过是项目 DoD 的前提。

**DoD 不通过的后果**：

- 页面 DoD 不通过：页面不得标记为"完成"，不得进入下一个页面。
- Sprint DoD 不通过：Sprint 不得标记为"完成"，未通过项作为 bug 在下一 Sprint 优先修复。
- 项目 DoD 不通过：项目不得交付和展示。

**例外流程**：如某项 DoD 确实不适用（如页面无表单，A.3 全部标注 N/A），必须由 Reviewer 确认 N/A 理由成立。
