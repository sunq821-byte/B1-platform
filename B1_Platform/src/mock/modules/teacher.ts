import type Mock from "mockjs"
import {
  cloneCourses,
  cloneStudents,
  cloneSubmissions,
  cloneTasks,
  EVAL_DIMENSIONS,
  isPendingTeacherReview,
  type SeedCourse,
  type SeedStudent,
  type SeedSubmission,
  type SeedTask,
} from "../seed/teaching"

function ok(data: unknown, message = "ok") {
  return {
    code: 0,
    message,
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

function studentCountForClass(className: string, students: SeedStudent[]): number {
  return students.filter((s) => s.className === className).length
}

function enrichCourse(c: SeedCourse, tasks: SeedTask[], students: SeedStudent[]) {
  return {
    ...c,
    studentCount: studentCountForClass(c.className, students),
    taskCount: tasks.filter((t) => t.courseId === c.courseId).length,
  }
}

function enrichTask(t: SeedTask, courses: SeedCourse[], submissions: SeedSubmission[]) {
  const course = courses.find((c) => c.courseId === t.courseId)
  const taskSubs = submissions.filter((s) => s.taskId === t.taskId)
  const reviewed = taskSubs.filter((s) => s.status === "APPROVED" || s.status === "REJECTED")
  return {
    taskId: t.taskId,
    taskName: t.taskName,
    courseId: t.courseId,
    courseName: course?.courseName ?? "",
    description: t.description,
    deadline: t.deadline,
    totalScore: t.totalScore,
    weight: t.weight,
    priority: t.priority,
    status: t.status,
    submissionType: t.submissionType,
    maxSubmitCount: t.maxSubmitCount,
    gradingRule: t.gradingRule,
    createTime: t.createdAt,
    publishTime: t.publishTime,
    submissionCount: taskSubs.length,
    reviewedCount: reviewed.length,
  }
}

function buildDiagnosisResult(sub: SeedSubmission, task: SeedTask | undefined, student: SeedStudent | undefined) {
  const dims = [
    {
      agentType: "CODE",
      issueType: "异常处理不完整",
      suggestDeduct: 5,
      comment: "catch 块仅打印异常，缺少重试与友好错误提示",
      filePath: "src/Main.java",
      lineNumber: 12,
      confidence: 0.92,
    },
    {
      agentType: "CODE",
      issueType: "算法效率不足",
      suggestDeduct: 3,
      comment: "findById 使用 hashCode 匹配，存在碰撞风险，建议 equals",
      filePath: "src/Main.java",
      lineNumber: 20,
      confidence: 0.85,
    },
    {
      agentType: "DOC",
      issueType: "缺少单元测试",
      suggestDeduct: 4,
      comment: "提交中未包含单元测试，覆盖率不足",
      filePath: "src/test/",
      lineNumber: 0,
      confidence: 0.78,
    },
  ]
  const aiScore = sub.aiScore ?? 80
  const summary = [
    `// ${task?.taskName ?? "项目"} - 代码预览`,
    `// 提交者: ${student?.realName ?? ""}`,
    `// 提交时间: ${sub.submittedAt.slice(0, 10)}`,
    "",
    "package com.b1.demo;",
    "",
    "public class Main {",
    "    public static void main(String[] args) {",
    "        // ... 演示代码片段 ...",
    "    }",
    "}",
  ].join("\n")

  return {
    status: "COMPLETED",
    result: {
      overallScore: aiScore,
      dimensions: dims,
      summary,
      strengths: ["结构清晰", "核心功能已实现"],
      weaknesses: ["异常处理不足", "测试覆盖偏低"],
      improvementPlan: "补齐单元测试与统一错误处理。",
    },
  }
}

export default function setupTeacherMock(mock: typeof Mock): void {
  const courses = cloneCourses()
  const tasks = cloneTasks()
  const students = cloneStudents()
  const submissions = cloneSubmissions()
  let nextCourseSeq = courses.length + 1

  // ========== Courses ==========

  mock.mock("/api/v1/teacher/courses", "get", () =>
    ok(courses.map((c) => enrichCourse(c, tasks, students))),
  )

  mock.mock("/api/v1/teacher/courses", "post", (options: { body: string }) => {
    const body = JSON.parse(options.body)
    const newCourse: SeedCourse = {
      courseId: `C${String(nextCourseSeq++).padStart(3, "0")}`,
      courseCode: body.courseCode,
      courseName: body.courseName,
      className: body.className || "",
      semester: body.semester || "2025-2026-2",
      credits: body.credits || 4,
      teacherName: "李四",
      teacherEmail: "lisi@example.com",
      description: body.description || "",
      status: "ACTIVE",
    }
    courses.push(newCourse)
    return ok(enrichCourse(newCourse, tasks, students), "课程创建成功")
  })

  mock.mock(/\/api\/v1\/teacher\/courses\/(?!\?).+$/, "put", (options: { url: string; body: string }) => {
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

  mock.mock(/\/api\/v1\/teacher\/courses\/(?!\?).+$/, "delete", (options: { url: string }) => {
    const courseId = options.url.split("/").pop()
    const idx = courses.findIndex((x) => x.courseId === courseId)
    if (idx < 0) return err(4001, "课程不存在")
    courses.splice(idx, 1)
    return ok(null, "课程已删除")
  })

  // ========== Standards ==========

  const standards = [
    { standardId: "S001", name: "软件工程实训评价标准", courseType: "软件工程", dimensionCount: 5, version: "v2.1", status: "published", updatedAt: "2025-12-20T10:30:00" },
    { standardId: "S002", name: "Web前端评价标准", courseType: "Web前端", dimensionCount: 4, version: "v1.0", status: "draft", updatedAt: "2026-01-15T14:00:00" },
    { standardId: "S003", name: "数据库课程设计评价标准", courseType: "数据库", dimensionCount: 5, version: "v1.5", status: "published", updatedAt: "2025-11-08T09:15:00" },
  ]

  const dimensionsStore = new Map<string, { name: string; weight: number }[]>([
    ["S001", EVAL_DIMENSIONS.map((d) => ({ name: d.dimensionName, weight: d.weight }))],
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
    return ok({
      standardId: standard.standardId,
      standardName: standard.name,
      dimensions: dimensionsStore.get(standardId) || [],
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

  mock.mock(/\/api\/v1\/teacher\/tasks(\?.*)?$/, "get", (options: { url: string }) => {
    const params = new URL(`http://localhost${options.url}`).searchParams
    const courseId = params.get("courseId")
    const filtered = courseId ? tasks.filter((t) => t.courseId === courseId) : tasks
    return ok(filtered.map((t) => enrichTask(t, courses, submissions)))
  })

  mock.mock("/api/v1/teacher/tasks", "post", (options: { body: string }) => {
    const body = JSON.parse(options.body)
    const prefix = body.courseId || "C001"
    const count = tasks.filter((t) => t.courseId === prefix).length + 1
    const newTask: SeedTask = {
      taskId: `${prefix}_T${count}`,
      taskName: body.taskName,
      courseId: body.courseId,
      description: body.description || "",
      requirements: body.requirements || "",
      deadline: body.deadline
        ? (String(body.deadline).includes("T") ? body.deadline : `${body.deadline}T23:59:00`)
        : "2026-09-01T23:59:00",
      totalScore: body.totalScore || 100,
      weight: body.weight || 25,
      priority: body.priority || "medium",
      status: "DRAFT",
      submissionType: body.submissionType || "ZIP_UPLOAD",
      maxSubmitCount: body.maxSubmitCount || 3,
      gradingRule: body.gradingRule,
      createdAt: new Date().toISOString(),
    }
    tasks.push(newTask)
    return ok(enrichTask(newTask, courses, submissions), "任务创建成功")
  })

  mock.mock(/\/api\/v1\/teacher\/tasks\/[^/]+$/, "put", (options: { url: string; body: string }) => {
    const url = options.url.split("?")[0]
    const taskId = url.split("/").pop() || ""
    const body = JSON.parse(options.body)
    const t = tasks.find((x) => x.taskId === taskId)
    if (!t) return err(4001, "任务不存在")
    if (body.taskName) t.taskName = body.taskName
    if (body.courseId) t.courseId = body.courseId
    if (body.description !== undefined) t.description = body.description
    if (body.requirements !== undefined) t.requirements = body.requirements
    if (body.deadline) {
      t.deadline = String(body.deadline).includes("T") ? body.deadline : `${body.deadline}T23:59:00`
    }
    if (body.totalScore !== undefined) t.totalScore = body.totalScore
    if (body.weight !== undefined) t.weight = body.weight
    if (body.priority) t.priority = body.priority
    if (body.submissionType) t.submissionType = body.submissionType
    if (body.maxSubmitCount !== undefined) t.maxSubmitCount = body.maxSubmitCount
    if (body.gradingRule !== undefined) t.gradingRule = body.gradingRule
    return ok(null, "任务更新成功")
  })

  mock.mock(/\/api\/v1\/teacher\/tasks\/.+\/publish$/, "post", (options: { url: string }) => {
    const parts = options.url.split("/")
    const taskId = parts[parts.length - 2]
    const t = tasks.find((x) => x.taskId === taskId)
    if (!t) return err(4001, "任务不存在")
    t.status = "PUBLISHED"
    t.publishTime = new Date().toISOString()
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

  mock.mock(/\/api\/v1\/teacher\/students(\?.*)?$/, "get", (options: { url: string }) => {
    const params = new URL(`http://localhost${options.url}`).searchParams
    const className = params.get("className")
    const keyword = params.get("keyword")
    let filtered = [...students]
    if (className) filtered = filtered.filter((s) => s.className === className)
    if (keyword) {
      filtered = filtered.filter(
        (s) => s.studentId.includes(keyword) || s.realName.includes(keyword) || s.userId.includes(keyword),
      )
    }
    const result = filtered.map((s) => {
      const studentSubs = submissions.filter((sub) => sub.studentUserId === s.userId)
      const scored = studentSubs.filter((sub) => sub.finalScore !== null)
      const avgScore = scored.length > 0
        ? scored.reduce((a, b) => a + (b.finalScore || 0), 0) / scored.length
        : null
      return {
        userId: s.userId,
        studentId: s.studentId,
        realName: s.realName,
        name: s.realName,
        className: s.className,
        email: s.email,
        phone: s.phone,
        submissionCount: studentSubs.length,
        completedCount: scored.length,
        avgScore,
      }
    })
    return ok(result)
  })

  mock.mock(/\/api\/v1\/teacher\/students\/(?!\?).+$/, "get", (options: { url: string }) => {
    const userId = options.url.split("/").pop() || ""
    const s = students.find((x) => x.userId === userId || x.studentId === userId)
    if (!s) return err(4001, "学生不存在")
    const studentSubs = submissions.filter((sub) => sub.studentUserId === s.userId)
    const scored = studentSubs.filter((sub) => sub.finalScore !== null)
    const avgScore = scored.length > 0
      ? scored.reduce((a, b) => a + (b.finalScore || 0), 0) / scored.length
      : null
    const grades = scored.map((sub) => {
      const t = tasks.find((x) => x.taskId === sub.taskId)
      return { taskName: t?.taskName || "-", score: sub.finalScore!, reviewedAt: sub.reviewedAt || "" }
    })
    return ok({
      userId: s.userId,
      studentId: s.studentId,
      realName: s.realName,
      name: s.realName,
      className: s.className,
      email: s.email,
      phone: s.phone,
      submissionCount: studentSubs.length,
      completedCount: scored.length,
      avgScore,
      grades,
    })
  })

  // ========== Submissions / Review ==========

  mock.mock("/api/v1/teacher/submissions", "get", () => {
    const pending = submissions
      .filter((s) => isPendingTeacherReview(s.status))
      .map((s) => {
        const stu = students.find((x) => x.userId === s.studentUserId)
        const t = tasks.find((x) => x.taskId === s.taskId)
        return {
          submissionId: s.submissionId,
          studentName: stu?.realName || "Unknown",
          studentUserId: s.studentUserId,
          studentEmail: stu?.email || "",
          taskId: s.taskId,
          taskName: t?.taskName || "Unknown",
          submittedAt: s.submittedAt,
          status: s.status,
          submitType: s.submitType,
          submitCount: s.submitCount,
          isLate: s.isLate,
          hasReview: s.aiScore != null,
          fileName: s.fileName,
          fileSize: s.fileSize,
        }
      })
    return ok({ list: pending, total: pending.length })
  })

  mock.mock(/\/api\/v1\/teacher\/submissions\/[^/]+$/, "get", (options: { url: string }) => {
    const submissionId = options.url.split("/").pop() || ""
    const sub = submissions.find((s) => s.submissionId === submissionId)
    if (!sub) return err(4001, "提交不存在")
    const attachments = sub.fileName
      ? [{
          fileId: `file-${sub.submissionId}`,
          fileName: sub.fileName,
          fileSize: sub.fileSize || 0,
          fileType: "application/zip",
          downloadUrl: `#mock-download/${sub.submissionId}`,
        }]
      : sub.gitUrl
        ? [{
            fileId: `git-${sub.submissionId}`,
            fileName: "repository.git",
            fileSize: 0,
            fileType: "text/uri-list",
            downloadUrl: sub.gitUrl,
          }]
        : []
    return ok({
      submissionId: sub.submissionId,
      taskId: sub.taskId,
      status: sub.status,
      submittedAt: sub.submittedAt,
      attachments,
    })
  })

  mock.mock(/\/api\/v1\/teacher\/submissions\/.+\/diagnosis$/, "get", (options: { url: string }) => {
    const parts = options.url.split("/")
    const submissionId = parts[parts.length - 2]
    const sub = submissions.find((s) => s.submissionId === submissionId)
    if (!sub) return err(4001, "提交不存在")
    const t = tasks.find((x) => x.taskId === sub.taskId)
    const student = students.find((x) => x.userId === sub.studentUserId)
    return ok(buildDiagnosisResult(sub, t, student))
  })

  mock.mock(/\/api\/v1\/teacher\/submissions\/.+\/publish$/, "post", (options: { url: string; body: string }) => {
    const parts = options.url.split("/")
    const submissionId = parts[parts.length - 2]
    const sub = submissions.find((s) => s.submissionId === submissionId)
    if (!sub) return err(4001, "提交不存在")
    const body = JSON.parse(options.body)
    if (body.status === "REJECTED") {
      sub.status = "REJECTED"
      sub.finalScore = null
      sub.rejectReason = body.comment || "请修改后重新提交"
    } else {
      sub.status = "APPROVED"
      sub.finalScore = body.finalScore
      sub.reviewComment = body.comment || null
    }
    sub.reviewedAt = new Date().toISOString()
    return ok(null, body.status === "REJECTED" ? "已退回" : `发布成功！总分 ${body.finalScore} 分`)
  })

  // ========== Dashboard ==========

  mock.mock("/api/v1/teacher/dashboard", "get", () => {
    const pending = submissions.filter((s) => isPendingTeacherReview(s.status))
    const pendingAi = pending.filter((s) => s.status === "AI_EVALUATING" || s.status === "SUBMITTED")
    const pendingTeacher = pending.filter((s) => s.status === "AI_COMPLETED" || s.status === "TEACHER_SCORING")
    const reviewed = submissions.filter((s) => s.status === "APPROVED")
    const returned = submissions.filter((s) => s.status === "REJECTED")
    const totalTasks = tasks.filter((t) => t.status === "PUBLISHED").length
    const totalStudents = students.length
    const submissionRate = totalStudents > 0 && totalTasks > 0
      ? Math.round((submissions.length / (totalStudents * totalTasks)) * 100)
      : 0

    const classSet = new Map<string, SeedStudent[]>()
    students.forEach((s) => {
      if (!classSet.has(s.className)) classSet.set(s.className, [])
      classSet.get(s.className)!.push(s)
    })
    const classNames = Array.from(classSet.keys())
    const submitRateByClass = classNames.map((cls) => {
      const sts = classSet.get(cls)!
      let submitted = 0
      let total = 0
      sts.forEach((st) => {
        submitted += submissions.filter((sub) => sub.studentUserId === st.userId).length
        total += totalTasks
      })
      return total > 0 ? Math.round((submitted / total) * 100) : 0
    })
    const avgScoresByClass = classNames.map((cls) => {
      const sts = classSet.get(cls)!
      const scores: number[] = []
      sts.forEach((st) => {
        submissions
          .filter((sub) => sub.studentUserId === st.userId && sub.finalScore !== null)
          .forEach((sub) => scores.push(sub.finalScore as number))
      })
      if (scores.length === 0) return 0
      return Math.round(scores.reduce((a, b) => a + b, 0) / scores.length)
    })

    const pendingItems = pending.slice(0, 5).map((s) => {
      const stu = students.find((x) => x.userId === s.studentUserId)
      const t = tasks.find((x) => x.taskId === s.taskId)
      return {
        submissionId: s.submissionId,
        studentName: stu?.realName || "Unknown",
        taskName: t?.taskName || "Unknown",
        submittedAt: s.submittedAt,
        status: s.status,
      }
    })

    const now = Date.now()
    const weekMs = 7 * 24 * 60 * 60 * 1000
    const upcomingTasks = tasks
      .filter((t) => t.status === "PUBLISHED")
      .map((t) => {
        const course = courses.find((c) => c.courseId === t.courseId)
        const classStudents = students.filter((s) => s.className === (course?.className || ""))
        const submittedIds = new Set(
          submissions.filter((s) => s.taskId === t.taskId).map((s) => s.studentUserId),
        )
        return {
          taskId: t.taskId,
          taskName: t.taskName,
          courseName: course?.courseName || "未命名课程",
          deadline: t.deadline,
          unsubmittedCount: classStudents.filter((s) => !submittedIds.has(s.userId)).length,
          totalStudents: classStudents.length,
          _due: new Date(t.deadline).getTime(),
        }
      })
      .filter((t) => t._due >= now - weekMs && t._due <= now + weekMs * 4)
      .sort((a, b) => a._due - b._due)
      .slice(0, 6)
      .map(({ _due, ...rest }) => rest)

    return ok({
      stats: {
        totalStudents,
        classCount: classNames.length,
        pendingCount: pending.length,
        reviewedCount: reviewed.length,
        submissionRate,
      },
      pendingReviews: pendingItems,
      submitRateByClass: {
        classNames,
        values: submitRateByClass,
        avgScores: avgScoresByClass,
      },
      upcomingTasks,
      reviewStatus: {
        pendingAi: pendingAi.length,
        pendingTeacher: pendingTeacher.length,
        approved: reviewed.length,
        returned: returned.length,
      },
      aiConfidence: { high: 3, mid: 2, low: 1, total: 6 },
      semesterTaskCount: totalTasks,
      knowledgeEntryCount: 86,
    })
  })

  // ========== Standards Library ==========

  mock.mock("/api/v1/teacher/standards-library", "get", () => {
    return ok([
      { id: "TMPL001", name: "综合实训通用标准", type: "综合实训", dims: 5, version: "v2.0", status: "published", updatedAt: "2026-02-15" },
      { id: "TMPL002", name: "课程设计通用标准", type: "课程设计", dims: 4, version: "v1.5", status: "published", updatedAt: "2026-03-01" },
      { id: "TMPL003", name: "毕业设计评审标准", type: "毕业设计", dims: 6, version: "v1.0", status: "draft", updatedAt: "2026-04-20" },
    ])
  })

  // ========== Reports ==========

  mock.mock(/\/api\/v1\/teacher\/reports(\?.*)?$/, "get", (options: { url: string }) => {
    const params = new URL(`http://localhost${options.url}`).searchParams
    const className = params.get("className")
    const filteredStudents = className
      ? students.filter((s) => s.className === className)
      : students

    const studentStats = filteredStudents.map((st) => {
      const subs = submissions.filter((s) => s.studentUserId === st.userId && s.finalScore !== null)
      const scores = subs.map((s) => s.finalScore!)
      return {
        studentId: st.studentId,
        name: st.realName,
        className: st.className,
        completedCount: subs.length,
        avgScore: scores.length > 0 ? +(scores.reduce((a, b) => a + b, 0) / scores.length).toFixed(1) : 0,
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

  mock.mock("/api/v1/teacher/reports-college", "get", () => {
    const classNames = ["软件2401", "软件2402", "计科2401", "计科2402"]
    const crossClassValues = classNames.map((cls) => {
      const sts = students.filter((s) => s.className === cls)
      const scores: number[] = []
      sts.forEach((st) => {
        submissions
          .filter((sub) => sub.studentUserId === st.userId && sub.finalScore !== null)
          .forEach((sub) => scores.push(sub.finalScore!))
      })
      return scores.length > 0
        ? +(scores.reduce((a, b) => a + b, 0) / scores.length).toFixed(1)
        : 72
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
