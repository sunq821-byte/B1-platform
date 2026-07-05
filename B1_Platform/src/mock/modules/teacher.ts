import type Mock from "mockjs"

interface CourseData {
  courseId: string
  courseCode: string
  courseName: string
  className: string
  semester: string
  credits: number
}

interface StandardData {
  standardId: string
  name: string
  courseType: string
  dimensionCount: number
  version: string
  status: "published" | "draft"
  updatedAt: string
}

function ok(data: unknown, message?: string) {
  return {
    code: 0,
    message: message || "ok",
    data,
    success: true,
    timestamp: Date.now(),
    traceId: `trace-${Date.now()}`,
  }
}

function err(code: number, message: string) {
  return {
    code,
    message,
    data: null,
    success: false,
    timestamp: Date.now(),
    traceId: `trace-${Date.now()}`,
  }
}

interface TaskData {
  taskId: string; taskName: string; courseId: string; description: string
  dueDate: string; weight: number; priority: string
  status: "published" | "draft"; maxScore: number; createdAt: string
}

interface StudentData {
  userId: string; studentId: string; name: string; className: string; email: string
}

interface SubmissionData {
  submissionId: string; studentId: string; taskId: string; submittedAt: string
  status: "submitted" | "reviewing" | "approved"
  finalScore: number | null; reviewedAt: string | null
  submissionType: "code" | "file"
  fileName?: string; fileSize?: number
}

interface AIData {
  submissionId: string; aiScore: number; totalDeductions: number; totalDeductScore: number
  deductions: Array<{
    issueType: string; agentType: string; reason: string; suggestDeduct: number
    adjustedDeduct?: number; filePath: string; lineNumber: number; confidence: number
    overridden: boolean; overrideAction?: string; overrideReason?: string
  }>
  aiIssueLines: number[]
}

export default function setupTeacherMock(mock: typeof Mock): void {
  // ========== Courses ==========

  const courses: CourseData[] = [
    { courseId: "C001", courseCode: "CS301", courseName: "软件工程综合实训", className: "软件2401", semester: "2025-2026-2", credits: 4 },
    { courseId: "C002", courseCode: "CS302", courseName: "数据库原理与应用", className: "软件2401", semester: "2025-2026-2", credits: 3 },
    { courseId: "C003", courseCode: "CS303", courseName: "Web前端开发", className: "软件2402", semester: "2025-2026-2", credits: 3 },
    { courseId: "C004", courseCode: "CS304", courseName: "人工智能导论", className: "计科2401", semester: "2025-2026-2", credits: 4 },
    { courseId: "C005", courseCode: "CS305", courseName: "操作系统", className: "计科2402", semester: "2025-2026-2", credits: 4 },
  ]

  let nextCourseId = 6

  mock.mock("/api/v1/teacher/courses", "get", () => ok(courses))

  mock.mock("/api/v1/teacher/courses", "post", (options: { body: string }) => {
    const body = JSON.parse(options.body)
    const newCourse: CourseData = {
      courseId: `C${String(nextCourseId++).padStart(3, "0")}`,
      courseCode: body.courseCode,
      courseName: body.courseName,
      className: body.className || "",
      semester: body.semester || "2025-2026-2",
      credits: body.credits || 4,
    }
    courses.push(newCourse)
    return ok(newCourse, "课程创建成功")
  })

  mock.mock(/\/api\/v1\/teacher\/courses\/(?!\?).+/, "put", (options: { url: string; body: string }) => {
    const courseId = options.url.split("/").pop()
    const body = JSON.parse(options.body)
    const c = courses.find((x) => x.courseId === courseId)
    if (!c) return err(4001, "课程不存在")
    if (body.courseCode) c.courseCode = body.courseCode
    if (body.courseName) c.courseName = body.courseName
    if (body.className !== undefined) c.className = body.className
    if (body.semester) c.semester = body.semester
    if (body.credits !== undefined) c.credits = body.credits
    return ok(null, "课程更新成功")
  })

  mock.mock(/\/api\/v1\/teacher\/courses\/.+\?/, "delete", (options: { url: string }) => {
    const url = options.url.split("?")[0]
    const courseId = url.split("/").pop()
    const idx = courses.findIndex((x) => x.courseId === courseId)
    if (idx < 0) return err(4001, "课程不存在")
    courses.splice(idx, 1)
    return ok(null, "课程已删除")
  })

  mock.mock(/\/api\/v1\/teacher\/courses\/(?!\?).+/, "delete", (options: { url: string }) => {
    const courseId = options.url.split("/").pop()
    const idx = courses.findIndex((x) => x.courseId === courseId)
    if (idx < 0) return err(4001, "课程不存在")
    courses.splice(idx, 1)
    return ok(null, "课程已删除")
  })

  // ========== Standards ==========

  const standards: StandardData[] = [
    { standardId: "S001", name: "软件工程实训评价标准", courseType: "软件工程", dimensionCount: 5, version: "v2.1", status: "published", updatedAt: "2025-12-20T10:30:00" },
    { standardId: "S002", name: "Web前端评价标准", courseType: "Web前端", dimensionCount: 4, version: "v1.0", status: "draft", updatedAt: "2026-01-15T14:00:00" },
    { standardId: "S003", name: "数据库课程设计评价标准", courseType: "数据库", dimensionCount: 5, version: "v1.5", status: "published", updatedAt: "2025-11-08T09:15:00" },
  ]

  const dimensionsStore = new Map<string, { name: string; weight: number }[]>([
    ["S001", [
      { name: "功能实现", weight: 30 },
      { name: "代码质量", weight: 25 },
      { name: "文档质量", weight: 20 },
      { name: "创新能力", weight: 15 },
      { name: "答辩表现", weight: 10 },
    ]],
    ["S002", [
      { name: "页面还原度", weight: 30 },
      { name: "代码规范", weight: 25 },
      { name: "交互体验", weight: 25 },
      { name: "性能优化", weight: 20 },
    ]],
    ["S003", [
      { name: "数据库设计", weight: 30 },
      { name: "SQL复杂度", weight: 25 },
      { name: "文档完整性", weight: 20 },
      { name: "索引优化", weight: 15 },
      { name: "安全性", weight: 10 },
    ]],
  ])

  mock.mock("/api/v1/teacher/standards", "get", () => ok(standards))

  mock.mock(/\/api\/v1\/teacher\/standards\/.+\/dimensions$/, "get", (options: { url: string }) => {
    const parts = options.url.split("/")
    const standardId = parts[parts.length - 2]
    const standard = standards.find((s) => s.standardId === standardId)
    if (!standard) return err(4001, "评价标准不存在")
    const dims = dimensionsStore.get(standardId) || []
    return ok({
      standardId: standard.standardId,
      standardName: standard.name,
      dimensions: dims,
    })
  })

  mock.mock(/\/api\/v1\/teacher\/standards\/.+\/dimensions$/, "put", (options: { url: string; body: string }) => {
    const parts = options.url.split("/")
    const standardId = parts[parts.length - 2]
    const standard = standards.find((s) => s.standardId === standardId)
    if (!standard) return err(4001, "评价标准不存在")
    const body = JSON.parse(options.body)
    if (body.dimensions) {
      dimensionsStore.set(standardId, body.dimensions)
      standard.dimensionCount = body.dimensions.length
    }
    return ok(null, "维度配置保存成功")
  })

  // ========== Tasks ==========

  const tasks: TaskData[] = [
    { taskId: "C001_T1", taskName: "用户管理系统API开发", courseId: "C001", description: "设计并实现一个完整的用户管理REST API，包含CRUD操作、分页查询、JWT认证等功能", dueDate: "2026-07-15T23:59:00", weight: 30, priority: "high", status: "published", maxScore: 100, createdAt: "2026-02-20T08:00:00" },
    { taskId: "C001_T2", taskName: "数据库设计与优化", courseId: "C001", description: "完成MySQL数据库表设计，编写存储过程和优化SQL查询", dueDate: "2026-07-01T23:59:00", weight: 25, priority: "high", status: "published", maxScore: 100, createdAt: "2026-03-01T08:00:00" },
    { taskId: "C002_T1", taskName: "ER图绘制与范式分析", courseId: "C002", description: "绘制概念ER图，进行范式分解，编写建表脚本", dueDate: "2026-07-10T23:59:00", weight: 25, priority: "medium", status: "published", maxScore: 100, createdAt: "2026-03-05T08:00:00" },
    { taskId: "C003_T1", taskName: "响应式页面布局实战", courseId: "C003", description: "使用Flexbox/Grid实现一个企业官网首页响应式布局", dueDate: "2026-07-20T23:59:00", weight: 30, priority: "medium", status: "draft", maxScore: 100, createdAt: "2026-03-10T08:00:00" },
    { taskId: "C004_T1", taskName: "机器学习模型训练", courseId: "C004", description: "使用scikit-learn完成数据预处理、模型训练和评估", dueDate: "2026-07-25T23:59:00", weight: 35, priority: "high", status: "published", maxScore: 100, createdAt: "2026-03-15T08:00:00" },
  ]

  mock.mock(/\/api\/v1\/teacher\/tasks(\?.*)?$/, "get", (options: { url: string }) => {
    const params = new URL(`http://localhost${options.url}`).searchParams
    const courseId = params.get("courseId")
    const filtered = courseId ? tasks.filter((t) => t.courseId === courseId) : tasks
    return ok(filtered)
  })

  mock.mock("/api/v1/teacher/tasks", "post", (options: { body: string }) => {
    const body = JSON.parse(options.body)
    const prefix = body.courseId || "C001"
    const count = tasks.filter((t) => t.courseId === prefix).length + 1
    const newTask: TaskData = {
      taskId: `${prefix}_T${count}`,
      taskName: body.taskName,
      courseId: body.courseId,
      description: body.description || "",
      dueDate: new Date(body.dueDate + "T23:59:00").toISOString(),
      weight: body.weight || 25,
      priority: body.priority || "medium",
      status: "published",
      maxScore: 100,
      createdAt: new Date().toISOString(),
    }
    tasks.push(newTask)
    return ok(newTask, "任务创建成功")
  })

  mock.mock(/\/api\/v1\/teacher\/tasks\/.+$/, "put", (options: { url: string; body: string }) => {
    const url = options.url.split("?")[0]
    const taskId = url.split("/").pop() || ""
    const body = JSON.parse(options.body)
    const t = tasks.find((x) => x.taskId === taskId)
    if (!t) return err(4001, "任务不存在")
    if (body.taskName) t.taskName = body.taskName
    if (body.courseId) t.courseId = body.courseId
    if (body.description !== undefined) t.description = body.description
    if (body.dueDate) t.dueDate = new Date(body.dueDate + "T23:59:00").toISOString()
    if (body.weight !== undefined) t.weight = body.weight
    if (body.priority) t.priority = body.priority
    return ok(null, "任务更新成功")
  })

  mock.mock(/\/api\/v1\/teacher\/tasks\/.+\/publish$/, "post", (options: { url: string }) => {
    const parts = options.url.split("/")
    const taskId = parts[parts.length - 2]
    const t = tasks.find((x) => x.taskId === taskId)
    if (!t) return err(4001, "任务不存在")
    t.status = "published"
    return ok(null, "任务已发布")
  })

  mock.mock(/\/api\/v1\/teacher\/tasks\/.+\?/, "delete", (options: { url: string }) => {
    const url = options.url.split("?")[0]
    const taskId = url.split("/").pop()
    const idx = tasks.findIndex((x) => x.taskId === taskId)
    if (idx < 0) return err(4001, "任务不存在")
    tasks.splice(idx, 1)
    return ok(null, "任务已删除")
  })

  mock.mock(/\/api\/v1\/teacher\/tasks\/[^/]+$/, "delete", (options: { url: string }) => {
    const taskId = options.url.split("/").pop()
    const idx = tasks.findIndex((x) => x.taskId === taskId)
    if (idx < 0) return err(4001, "任务不存在")
    tasks.splice(idx, 1)
    return ok(null, "任务已删除")
  })

  // ========== Students ==========

  const students: StudentData[] = [
    { userId: "S001", studentId: "2024001", name: "张三", className: "软件2401", email: "zhangsan@b1.edu.cn" },
    { userId: "S002", studentId: "2024002", name: "李四", className: "软件2401", email: "lisi@b1.edu.cn" },
    { userId: "S003", studentId: "2024003", name: "王五", className: "软件2402", email: "wangwu@b1.edu.cn" },
    { userId: "S004", studentId: "2024004", name: "赵六", className: "软件2402", email: "zhaoliu@b1.edu.cn" },
    { userId: "S005", studentId: "2024005", name: "钱七", className: "计科2401", email: "qianqi@b1.edu.cn" },
    { userId: "S006", studentId: "2024006", name: "孙八", className: "计科2401", email: "sunba@b1.edu.cn" },
    { userId: "S007", studentId: "2024007", name: "周九", className: "计科2402", email: "zhoujiu@b1.edu.cn" },
  ]

  const submissions: SubmissionData[] = [
    { submissionId: "SUB001", studentId: "S001", taskId: "C001_T1", submittedAt: "2026-06-20T10:00:00", status: "submitted", finalScore: null, reviewedAt: null, submissionType: "code" },
    { submissionId: "SUB002", studentId: "S002", taskId: "C001_T1", submittedAt: "2026-06-21T14:30:00", status: "reviewing", finalScore: null, reviewedAt: null, submissionType: "code" },
    { submissionId: "SUB003", studentId: "S003", taskId: "C001_T2", submittedAt: "2026-06-18T09:00:00", status: "submitted", finalScore: null, reviewedAt: null, submissionType: "file", fileName: "数据库设计文档.zip", fileSize: 2457600 },
    { submissionId: "SUB004", studentId: "S001", taskId: "C001_T2", submittedAt: "2026-06-15T16:00:00", status: "approved", finalScore: 88.5, reviewedAt: "2026-06-17T10:00:00", submissionType: "file", fileName: "设计方案.pdf", fileSize: 5120000 },
    { submissionId: "SUB005", studentId: "S004", taskId: "C002_T1", submittedAt: "2026-06-22T11:00:00", status: "submitted", finalScore: null, reviewedAt: null, submissionType: "file", fileName: "ER图与范式分析.docx", fileSize: 1890400 },
    { submissionId: "SUB006", studentId: "S002", taskId: "C002_T1", submittedAt: "2026-06-10T08:00:00", status: "approved", finalScore: 76.0, reviewedAt: "2026-06-12T14:00:00", submissionType: "code" },
    { submissionId: "SUB007", studentId: "S005", taskId: "C004_T1", submittedAt: "2026-06-25T13:00:00", status: "submitted", finalScore: null, reviewedAt: null, submissionType: "code" },
    { submissionId: "SUB008", studentId: "S001", taskId: "C004_T1", submittedAt: "2026-06-01T09:00:00", status: "approved", finalScore: 92.0, reviewedAt: "2026-06-03T11:00:00", submissionType: "file", fileName: "模型训练报告.pdf", fileSize: 3400000 },
  ]

  const aiResults = new Map<string, AIData>()

  function ensureAIResult(submissionId: string, _taskId: string): AIData {
    if (!aiResults.has(submissionId)) {
      aiResults.set(submissionId, {
        submissionId,
        aiScore: 75 + Math.floor(Math.random() * 20),
        totalDeductions: 3,
        totalDeductScore: 12 + Math.floor(Math.random() * 10),
        deductions: [
          { issueType: "异常处理不完整", agentType: "CODE", reason: "catch块中仅打印异常信息，缺少重试机制和用户友好的错误提示", suggestDeduct: 5, adjustedDeduct: undefined, filePath: "src/Main.java", lineNumber: 12, confidence: 0.92, overridden: false, overrideAction: undefined, overrideReason: undefined },
          { issueType: "算法效率不足", agentType: "CODE", reason: "findById使用hashCode进行匹配，存在哈希碰撞风险，建议使用equals比较", suggestDeduct: 3, adjustedDeduct: undefined, filePath: "src/Main.java", lineNumber: 20, confidence: 0.85, overridden: false, overrideAction: undefined, overrideReason: undefined },
          { issueType: "缺少单元测试", agentType: "DOC", reason: "代码提交中未包含单元测试，测试覆盖率不足", suggestDeduct: 4, adjustedDeduct: undefined, filePath: "src/test/", lineNumber: 0, confidence: 0.78, overridden: false, overrideAction: undefined, overrideReason: undefined },
        ],
        aiIssueLines: [12, 20, 24],
      })
    }
    return aiResults.get(submissionId)!
  }

  mock.mock(/\/api\/v1\/teacher\/students(\?.*)?$/, "get", (options: { url: string }) => {
    const params = new URL(`http://localhost${options.url}`).searchParams
    const className = params.get("className")
    const keyword = params.get("keyword")
    let filtered = [...students]
    if (className) filtered = filtered.filter((s) => s.className === className)
    if (keyword) filtered = filtered.filter((s) => s.studentId.includes(keyword) || s.name.includes(keyword))
    const result = filtered.map((s) => {
      const studentSubs = submissions.filter((sub) => sub.studentId === s.userId && sub.finalScore !== null)
      const avgScore = studentSubs.length > 0
        ? studentSubs.reduce((a, b) => a + (b.finalScore || 0), 0) / studentSubs.length
        : null
      return { ...s, completedCount: studentSubs.length, avgScore }
    })
    return ok(result)
  })

  mock.mock(/\/api\/v1\/teacher\/students\/(?!\?).+$/, "get", (options: { url: string }) => {
    const userId = options.url.split("/").pop() || ""
    const s = students.find((x) => x.userId === userId)
    if (!s) return err(4001, "学生不存在")
    const studentSubs = submissions.filter((sub) => sub.studentId === userId)
    const scored = studentSubs.filter((sub) => sub.finalScore !== null)
    const avgScore = scored.length > 0 ? scored.reduce((a, b) => a + (b.finalScore || 0), 0) / scored.length : null
    const grades = scored.map((sub) => {
      const t = tasks.find((x) => x.taskId === sub.taskId)
      return { taskName: t?.taskName || "-", score: sub.finalScore!, reviewedAt: sub.reviewedAt || "" }
    })
    return ok({
      ...s,
      completedCount: scored.length,
      avgScore,
      grades,
    })
  })

  // ========== Submissions / Review ==========

  mock.mock("/api/v1/teacher/submissions", "get", () => {
    const pending = submissions
      .filter((s) => s.status === "submitted" || s.status === "reviewing")
      .map((s) => {
        const stu = students.find((x) => x.userId === s.studentId)
        const t = tasks.find((x) => x.taskId === s.taskId)
        return {
          submissionId: s.submissionId,
          studentId: s.studentId,
          studentName: stu?.name || "Unknown",
          taskId: s.taskId,
          taskName: t?.taskName || "Unknown",
          submittedAt: s.submittedAt,
          status: s.status,
          submissionType: s.submissionType,
          fileName: s.fileName,
          fileSize: s.fileSize,
        }
      })
    return ok(pending)
  })

  mock.mock(/\/api\/v1\/teacher\/submissions\/.+\/diagnosis$/, "get", (options: { url: string }) => {
    const parts = options.url.split("/")
    const submissionId = parts[parts.length - 2]
    const sub = submissions.find((s) => s.submissionId === submissionId)
    if (!sub) return err(4001, "提交不存在")
    const ai = ensureAIResult(submissionId, sub.taskId)
    const t = tasks.find((x) => x.taskId === sub.taskId)
    const student = students.find((x) => x.userId === sub.studentId)

    const codePreview = [
      `// ${t?.taskName || "项目"} - 代码预览`,
      `// 提交者: ${student?.name || ""}`,
      `// 提交时间: ${sub.submittedAt.substring(0, 10)}`,
      "",
      "package com.b1.demo;",
      "",
      "import java.util.*;",
      "import java.io.*;",
      "",
      "public class Main {",
      "    private static List<String> dataList;",
      "    private static DatabaseManager db;",
      "",
      "    public static void main(String[] args) {",
      "        db = new DatabaseManager();",
      "        db.connect();",
      "        dataList = new ArrayList<>();",
      "        loadInitialData();",
      `        System.out.println("系统启动成功");`,
      "    }",
      "",
      "    private static void loadInitialData() {",
      "        try {",
      `            List<String> raw = db.query("SELECT * FROM data");`,
      "            for (String row : raw) {",
      "                dataList.add(processRow(row));",
      "            }",
      "        } catch (Exception e) {",
      `            System.err.println("数据加载失败: " + e.getMessage());`,
      "        }",
      "    }",
      "",
      "    private static String processRow(String row) {",
      "        if (row == null || row.isEmpty()) return \"\";",
      "        return row.trim().toUpperCase();",
      "    }",
      "",
      "    public static String findById(int id) {",
      "        for (String item : dataList) {",
      "            if (item.hashCode() == id) {",
      "                return item;",
      "            }",
      "        }",
      "        return null;",
      "    }",
      "}",
    ].join("\n")

    return ok({ ...ai, codePreview })
  })

  mock.mock(/\/api\/v1\/teacher\/submissions\/.+\/publish$/, "post", (options: { url: string; body: string }) => {
    const parts = options.url.split("/")
    const submissionId = parts[parts.length - 2]
    const sub = submissions.find((s) => s.submissionId === submissionId)
    if (!sub) return err(4001, "提交不存在")
    const body = JSON.parse(options.body)
    sub.status = "approved"
    sub.finalScore = body.finalScore
    sub.reviewedAt = new Date().toISOString()
    return ok(null, `发布成功！总分 ${body.finalScore} 分`)
  })

  // ========== Dashboard ==========

  mock.mock("/api/v1/teacher/dashboard", "get", () => {
    const allSubs = submissions
    const pending = allSubs.filter((s) => s.status === "submitted" || s.status === "reviewing")
    const reviewed = allSubs.filter((s) => s.status === "approved")
    const totalTasks = tasks.length
    const totalStudents = students.length
    const submissionRate = totalStudents > 0 && totalTasks > 0
      ? Math.round((allSubs.length / (totalStudents * totalTasks)) * 100)
      : 0

    const classSet = new Map<string, StudentData[]>()
    students.forEach((s) => {
      if (!classSet.has(s.className)) classSet.set(s.className, [])
      classSet.get(s.className)!.push(s)
    })

    const classNames = Array.from(classSet.keys())
    const submitRateByClass = classNames.map((cls) => {
      const sts = classSet.get(cls)!
      let submitted = 0, total = 0
      sts.forEach((st) => {
        const stSubs = submissions.filter((sub) => sub.studentId === st.userId)
        submitted += stSubs.length
        total += tasks.length
      })
      return total > 0 ? Math.round((submitted / total) * 100) : 0
    })

    const pendingItems = pending.slice(0, 5).map((s) => {
      const stu = students.find((x) => x.userId === s.studentId)
      const t = tasks.find((x) => x.taskId === s.taskId)
      return {
        submissionId: s.submissionId,
        studentName: stu?.name || "Unknown",
        taskName: t?.taskName || "Unknown",
        submittedAt: s.submittedAt,
        status: s.status,
      }
    })

    return ok({
      stats: {
        totalStudents,
        classCount: classNames.length,
        pendingCount: pending.length,
        reviewedCount: reviewed.length,
        submissionRate,
      },
      pendingReviews: pendingItems,
      submitRateByClass: { classNames, values: submitRateByClass },
    })
  })

  // ========== Standards Library ==========

  mock.mock("/api/v1/teacher/standards-library", "get", () => {
    const templates = [
      { id: "TMPL001", name: "综合实训通用标准", type: "综合实训", dims: 5, version: "v2.0", status: "published", updatedAt: "2026-02-15" },
      { id: "TMPL002", name: "课程设计通用标准", type: "课程设计", dims: 4, version: "v1.5", status: "published", updatedAt: "2026-03-01" },
      { id: "TMPL003", name: "毕业设计评审标准", type: "毕业设计", dims: 6, version: "v1.0", status: "draft", updatedAt: "2026-04-20" },
    ]
    return ok(templates)
  })

  // ========== Reports ==========

  mock.mock(/\/api\/v1\/teacher\/reports(\?.*)?$/, "get", (options: { url: string }) => {
    const params = new URL(`http://localhost${options.url}`).searchParams
    const className = params.get("className")

    const filteredStudents = className
      ? students.filter((s) => s.className === className)
      : students

    const studentStats = filteredStudents.map((st) => {
      const subs = submissions.filter((s) => s.studentId === st.userId && s.finalScore !== null)
      const scores = subs.map((s) => s.finalScore!)
      return {
        studentId: st.studentId,
        name: st.name,
        className: st.className,
        completedCount: subs.length,
        avgScore: scores.length > 0 ? scores.reduce((a, b) => a + b, 0) / scores.length : 0,
        maxScore: scores.length > 0 ? Math.max(...scores) : 0,
        minScore: scores.length > 0 ? Math.min(...scores) : 0,
      }
    })

    const totalStudents = filteredStudents.length
    const classAvg = studentStats.length > 0
      ? +(studentStats.reduce((s, st) => s + st.avgScore, 0) / studentStats.length).toFixed(1)
      : 0
    const totalReviewed = studentStats.reduce((s, st) => s + st.completedCount, 0)
    const passCount = studentStats.filter((s) => s.avgScore >= 60).length
    const passRate = studentStats.length > 0 ? Math.round((passCount / studentStats.length) * 100) : 0

    const ranges = ["0-59", "60-69", "70-79", "80-89", "90-100"]
    const bins = [0, 0, 0, 0, 0]
    studentStats.forEach((s) => {
      if (s.avgScore < 60) bins[0]++
      else if (s.avgScore < 70) bins[1]++
      else if (s.avgScore < 80) bins[2]++
      else if (s.avgScore < 90) bins[3]++
      else bins[4]++
    })

    const classAvgsMap = new Map<string, number[]>()
    studentStats.forEach((s) => {
      if (!classAvgsMap.has(s.className)) classAvgsMap.set(s.className, [])
      classAvgsMap.get(s.className)!.push(s.avgScore)
    })
    const courseAvgCategories = Array.from(classAvgsMap.keys())
    const courseAvgValues = courseAvgCategories.map((cn) =>
      +((classAvgsMap.get(cn)!.reduce((a, b) => a + b, 0) / classAvgsMap.get(cn)!.length).toFixed(1)),
    )

    return ok({
      stats: { totalStudents, totalReviewed, classAverage: classAvg, passRate },
      histogram: { categories: ranges, values: bins },
      courseAvgs: { categories: courseAvgCategories, values: courseAvgValues },
      rows: studentStats.sort((a, b) => b.avgScore - a.avgScore),
    })
  })

  // ========== College Reports ==========

  mock.mock("/api/v1/teacher/reports-college", "get", () => {
    const classNames = ["软件2401", "软件2402", "计算机2401", "计算机2402", "人工智能2401", "大数据2401"]
    const crossClassValues = classNames.map((cls) => {
      const sts = students.filter((s) => s.className === cls)
      if (sts.length === 0) return +(40 + Math.random() * 50).toFixed(1)
      const scores: number[] = []
      sts.forEach((st) => {
        submissions
          .filter((sub) => sub.studentId === st.userId && sub.finalScore !== null)
          .forEach((sub) => scores.push(sub.finalScore!))
      })
      return scores.length > 0
        ? +(scores.reduce((a, b) => a + b, 0) / scores.length).toFixed(1)
        : +(40 + Math.random() * 50).toFixed(1)
    })

    return ok({
      crossClass: { classNames, values: crossClassValues },
      semesterTrend: {
        semesters: ["2024-2025-1", "2024-2025-2", "2025-2026-1", "2025-2026-2"],
        series: [
          { name: "软件专业", data: [72, 75, 78, 82] },
          { name: "计算机专业", data: [68, 71, 74, 79] },
          { name: "AI/大数据", data: [65, 69, 73, 76] },
        ],
      },
    })
  })
}
