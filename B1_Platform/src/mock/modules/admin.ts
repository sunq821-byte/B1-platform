import Mock from "mockjs"

function ok(data: unknown, message = "操作成功") {
  const { Random } = Mock
  return { code: 0, message, data, success: true, timestamp: Date.now(), traceId: Random.guid() }
}
function err(code: number, message: string) {
  const { Random } = Mock
  return { code, message, data: null, success: false, timestamp: Date.now(), traceId: Random.guid() }
}

function getUsers() {
  return {
    students: [
      { userId: "S001", studentId: "20241001", name: "张三", role: "student", className: "软件2401", email: "zhangsan@example.com", status: "active", createdAt: "2025-09-01" },
      { userId: "S002", studentId: "20241002", name: "李四", role: "student", className: "软件2401", email: "lisi@example.com", status: "active", createdAt: "2025-09-01" },
      { userId: "S003", studentId: "20241003", name: "王五", role: "student", className: "软件2402", email: "wangwu@example.com", status: "active", createdAt: "2025-09-01" },
      { userId: "S004", studentId: "20241004", name: "赵六", role: "student", className: "软件2402", email: "zhaoliu@example.com", status: "inactive", createdAt: "2025-09-01" },
      { userId: "S005", studentId: "20241005", name: "孙七", role: "student", className: "计科2401", email: "sunqi@example.com", status: "active", createdAt: "2025-09-01" },
    ],
    teachers: [
      { userId: "T001", name: "李四", role: "teacher", className: "软件工程系", email: "lisi_teacher@example.com", status: "active", createdAt: "2025-08-15" },
      { userId: "T002", name: "赵六", role: "teacher", className: "计算机科学系", email: "zhaoliu_teacher@example.com", status: "active", createdAt: "2025-08-15" },
      { userId: "T003", name: "钱七", role: "teacher", className: "软件工程系", email: "qianqi_teacher@example.com", status: "active", createdAt: "2025-08-15" },
    ],
    admins: [
      { userId: "A001", name: "管理员", role: "admin", className: "信息中心", email: "admin@example.com", status: "active", createdAt: "2025-08-01" },
    ],
  }
}

const classes = [
  { id: "CLS001", name: "软件2401", studentCount: 32, teacherName: "李四", createdAt: "2025-09-01" },
  { id: "CLS002", name: "软件2402", studentCount: 28, teacherName: "赵六", createdAt: "2025-09-01" },
  { id: "CLS003", name: "计科2401", studentCount: 35, teacherName: "钱七", createdAt: "2025-09-01" },
  { id: "CLS004", name: "计科2402", studentCount: 30, teacherName: "李四", createdAt: "2025-09-01" },
]

let systemConfig = {
  systemName: "B1 Platform", currentSemester: "2025-2026-2", semesterStart: "2026-02-24", semesterEnd: "2026-07-10",
  maxUploadSize: 50, aiModelVersion: "v2.1.0", autoAnalyze: true, notificationEnabled: true, maintenanceMode: false,
}

export default function setupAdminMock(mock: typeof Mock): void {
  const { Random } = mock

  // GET /api/v1/admin/users
  mock.mock(/\/api\/v1\/admin\/users(\?.*)?$/, "get", (options: { url: string }) => {
    const url = new URL(`http://localhost${options.url}`)
    const role = url.searchParams.get("role") || ""
    const keyword = (url.searchParams.get("keyword") || "").toLowerCase()
    const { students, teachers, admins } = getUsers()
    let all = [...students, ...teachers, ...admins]
    if (role && role !== "all") all = all.filter((u) => u.role === role)
    if (keyword) all = all.filter((u) => u.name.toLowerCase().includes(keyword) || ((u as Record<string, string>).studentId || "").includes(keyword))
    return ok(all)
  })

  // POST /api/v1/admin/users
  mock.mock("/api/v1/admin/users", "post", (options: { body: string }) => {
    const body = JSON.parse(options.body)
    if (!body.name) return err(1001, "姓名不能为空")
    const prefix = body.role === "student" ? "S" : (body.role === "teacher" ? "T" : "A")
    const newUser: Record<string, unknown> = {
      userId: `${prefix}${Random.integer(100, 999)}`, name: body.name, role: body.role,
      className: body.className || "", email: body.email || "", status: body.status || "active",
      createdAt: new Date().toISOString().substring(0, 10),
    }
    if (body.role === "student") newUser.studentId = `2024${Random.integer(1000, 9999)}`
    return ok(newUser, "用户创建成功")
  })

  // PUT /api/v1/admin/users/:userId
  mock.mock(/\/api\/v1\/admin\/users\/[^/]+$/, "put", (options: { url: string; body: string }) => {
    return ok({ userId: options.url.split("/").pop(), ...JSON.parse(options.body) }, "用户已更新")
  })

  // PATCH /api/v1/admin/users/:userId/toggle-status
  mock.mock(/\/api\/v1\/admin\/users\/.+\/toggle-status$/, "patch", (options: { url: string }) => {
    return ok({ userId: options.url.split("/")[5], status: "active" }, "状态已切换")
  })

  // GET /api/v1/admin/system-config
  mock.mock("/api/v1/admin/system-config", "get", () => ok({ ...systemConfig }))
  // PUT /api/v1/admin/system-config
  mock.mock("/api/v1/admin/system-config", "put", (options: { body: string }) => {
    systemConfig = { ...systemConfig, ...JSON.parse(options.body) }
    return ok(null, "系统配置已保存")
  })

  // GET /api/v1/admin/dashboard
  mock.mock("/api/v1/admin/dashboard", "get", () => {
    const { students, teachers, admins } = getUsers()
    const allUsers = [...students, ...teachers, ...admins]
    const courses = ["软件工程实践", "数据库原理", "Web前端开发", "计算机网络", "移动应用开发", "人工智能导论"]
    const totalSubs = allUsers.filter((u) => u.role === "student").length * Random.integer(2, 4)

    return ok({
      stats: {
        totalUsers: allUsers.length,
        activeCourses: courses.length,
        totalSubmissions: totalSubs,
        completionRate: Random.integer(55, 85),
      },
      recentLogs: [
        { type: "success", message: "教师 李四 发布新任务", detail: "实训任务: 实现用户管理系统", createdAt: new Date(Date.now() - 120000).toISOString() },
        { type: "info", message: "学生 张三 提交作业", detail: "Git 仓库提交", createdAt: new Date(Date.now() - 300000).toISOString() },
        { type: "warning", message: "AI 分析超时告警", detail: "提交 sub-005 分析超过 60s", createdAt: new Date(Date.now() - 600000).toISOString() },
        { type: "info", message: "管理员修改系统配置", detail: "更新学期结束日期", createdAt: new Date(Date.now() - 900000).toISOString() },
        { type: "error", message: "文件上传失败", detail: "学生 王五 上传文件超出大小限制", createdAt: new Date(Date.now() - 1800000).toISOString() },
        { type: "success", message: "批量导入学生成功", detail: "成功导入 30 名学生到软件2401", createdAt: new Date(Date.now() - 3600000).toISOString() },
      ],
      health: [
        { name: "AI 分析服务", status: "运行中", color: "var(--color-success, #10b981)" },
        { name: "数据库服务", status: "运行中", color: "var(--color-success, #10b981)" },
        { name: "文件存储", status: "运行中", color: "var(--color-success, #10b981)" },
        { name: "消息队列", status: "运行中", color: "var(--color-success, #10b981)" },
        { name: "缓存服务", status: "异常", color: "var(--color-warning, #f59e0b)" },
      ],
    })
  })

  // GET /api/v1/admin/classes
  mock.mock("/api/v1/admin/classes", "get", () => ok(classes))
  // POST /api/v1/admin/classes
  mock.mock("/api/v1/admin/classes", "post", (options: { body: string }) => {
    const body = JSON.parse(options.body)
    const newClass = { id: `CLS${Random.integer(100, 999)}`, name: body.name, studentCount: 0, teacherName: body.teacherName, createdAt: new Date().toISOString().substring(0, 10) }
    classes.push(newClass)
    return ok(newClass, "班级创建成功")
  })
  // PUT /api/v1/admin/classes/:classId
  mock.mock(/\/api\/v1\/admin\/classes\/[^/]+$/, "put", (options: { url: string; body: string }) => {
    const id = options.url.split("/").pop()!
    const body = JSON.parse(options.body)
    const idx = classes.findIndex((c) => c.id === id)
    if (idx >= 0) classes[idx] = { ...classes[idx], ...body }
    return ok(null, "班级已更新")
  })
  // DELETE /api/v1/admin/classes/:classId
  mock.mock(/\/api\/v1\/admin\/classes\/[^/]+$/, "delete", (options: { url: string }) => {
    const id = options.url.split("/").pop()!
    const idx = classes.findIndex((c) => c.id === id)
    if (idx >= 0) classes.splice(idx, 1)
    return ok(null, "班级已删除")
  })

  // GET /api/v1/admin/logs
  mock.mock(/\/api\/v1\/admin\/logs(\?.*)?$/, "get", (options: { url: string }) => {
    const url = new URL(`http://localhost${options.url}`)
    const type = url.searchParams.get("type") || ""
    const allLogs = Array.from({ length: 20 }, () => {
      const t = Random.pick(["info", "warning", "error", "success"])
      return {
        id: `log-${Random.guid()}`,
        type: t,
        message: Random.pick(["用户登录", "任务提交", "AI 分析完成", "配置更新", "文件上传", "批量导入", "成绩发布", "审核完成"]) + Random.cword(2, 4),
        detail: Random.csentence(5, 15),
        operator: Random.pick(["管理员", "李四", "张三", "系统"]),
        createdAt: Random.datetime("yyyy-MM-dd HH:mm:ss"),
      }
    })
    allLogs.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    const filtered = type ? allLogs.filter((l) => l.type === type) : allLogs
    return ok(filtered)
  })

  // GET /api/v1/admin/monitor
  mock.mock("/api/v1/admin/monitor", "get", () => {
    return ok({
      services: [
        { name: "AI 分析服务", status: "运行中", color: "var(--color-success, #10b981)" },
        { name: "数据库服务", status: "运行中", color: "var(--color-success, #10b981)" },
        { name: "文件存储", status: "运行中", color: "var(--color-success, #10b981)" },
        { name: "消息队列", status: "运行中", color: "var(--color-success, #10b981)" },
        { name: "缓存服务", status: "异常", color: "var(--color-warning, #f59e0b)" },
        { name: "通知服务", status: "运行中", color: "var(--color-success, #10b981)" },
      ],
      cpuUsage: Random.integer(20, 65),
      memoryUsage: Random.integer(40, 75),
      diskUsage: Random.integer(30, 55),
      uptime: `${Random.integer(1, 30)} 天 ${Random.integer(0, 23)} 小时`,
    })
  })
}
