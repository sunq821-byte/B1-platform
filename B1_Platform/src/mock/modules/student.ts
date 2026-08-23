import Mock from "mockjs"
import {
  cloneSubmissions,
  cloneTasks,
  DEMO_STUDENT_USER_ID,
  DEMO_TEACHER_EMAIL,
  DEMO_TEACHER_NAME,
  EVAL_DIMENSIONS,
  SEED_COURSES,
  SEED_STUDENTS,
  SEED_SUBMISSIONS,
  SEED_TASKS,
  studentStatusForTask,
  type SeedSubmission,
  type SeedTask,
} from "../seed/teaching"

const aiPollCounters: Map<string, number> = new Map()
const submissions = cloneSubmissions()
const tasks = cloneTasks()

function ok(data: unknown, message = "操作成功") {
  const { Random } = Mock
  return {
    code: 0,
    message,
    data,
    success: true,
    timestamp: Date.now(),
    traceId: Random.guid(),
  }
}

function err(code: number, message: string) {
  const { Random } = Mock
  return {
    code,
    message,
    data: null,
    success: false,
    timestamp: Date.now(),
    traceId: Random.guid(),
  }
}

function courseOf(task: SeedTask) {
  return SEED_COURSES.find((c) => c.courseId === task.courseId)
}

function makeAIScoreResult(aiScore?: number | null) {
  const dims = [
    { name: "代码规范", score: 82 },
    { name: "功能完成度", score: 88 },
    { name: "创新性", score: 75 },
    { name: "文档完整性", score: 80 },
    { name: "Git规范", score: 85 },
  ]
  const weights = [25, 30, 20, 15, 10]
  const overall = aiScore ?? Math.round(dims.reduce((a, d, i) => a + (d.score * weights[i]) / 100, 0))

  return {
    overallScore: overall,
    dimensions: dims.map((d, i) => ({
      dimensionName: d.name,
      score: d.score,
      maxScore: 100,
      weight: weights[i],
      comment: `${d.name}方面表现${d.score >= 80 ? "优秀" : "良好"}。`,
      suggestions: [`建议优化${d.name}相关规范`],
      codeReferences: [`src/modules/demo/index.ts:12-45`],
    })),
    summary: "整体代码质量良好，功能实现完整，但在部分维度还有提升空间。",
    strengths: ["代码结构清晰", "功能实现完整", "注释规范"],
    weaknesses: ["部分变量命名不够语义化", "测试覆盖率偏低", "缺少错误边界处理"],
    improvementPlan: "建议加强单元测试覆盖，完善错误处理机制，统一代码风格。",
  }
}

function demoStudent() {
  return SEED_STUDENTS.find((s) => s.userId === DEMO_STUDENT_USER_ID)!
}

function publishedTasksForStudent(): SeedTask[] {
  // 演示学生：软件2401 课程 + 已有提交的选修课任务
  const myClass = demoStudent().className
  const classCourseIds = new Set(
    SEED_COURSES.filter((c) => c.className === myClass).map((c) => c.courseId),
  )
  const submittedTaskIds = new Set(
    submissions.filter((s) => s.studentUserId === DEMO_STUDENT_USER_ID).map((s) => s.taskId),
  )
  return tasks.filter(
    (t) =>
      t.status !== "DRAFT" &&
      (classCourseIds.has(t.courseId) || submittedTaskIds.has(t.taskId)),
  )
}

function toStudentTaskItem(t: SeedTask) {
  const course = courseOf(t)
  const mine = studentStatusForTask(t.taskId, DEMO_STUDENT_USER_ID, submissions)
  return {
    taskId: t.taskId,
    taskName: t.taskName,
    courseName: course?.courseName ?? "",
    teacherName: course?.teacherName ?? DEMO_TEACHER_NAME,
    deadline: t.deadline.replace("T", " "),
    totalScore: t.totalScore,
    submissionType: t.submissionType,
    status: t.status,
    mySubmissionStatus: mine.status,
    myScore: mine.score,
    createdAt: t.createdAt.replace("T", " "),
  }
}

export default function setupStudentMock(mock: typeof Mock): void {
  // GET /api/v1/student/dashboard
  mock.mock("/api/v1/student/dashboard", "get", () => {
    const myTasks = publishedTasksForStudent()
    const recent = myTasks.map(toStudentTaskItem)
    const pending = recent.filter((t) => t.mySubmissionStatus === "NOT_SUBMITTED" || t.mySubmissionStatus === "REJECTED")
    const completed = recent.filter((t) => t.mySubmissionStatus === "COMPLETED")
    const analyzing = recent.filter((t) => t.mySubmissionStatus === "AI_EVALUATING" || t.mySubmissionStatus === "TEACHER_SCORING")
    const scores = completed.map((t) => t.myScore!).filter((n) => n != null)
    const avg = scores.length ? +(scores.reduce((a, b) => a + b, 0) / scores.length).toFixed(1) : 85.6
    const deadlines = pending
      .map((t) => t.deadline)
      .sort()

    // 成绩趋势：优先用已评分任务；不足时用演示序列补齐，避免空图/全 0
    const scoredPoints = submissions
      .filter((s) => s.studentUserId === DEMO_STUDENT_USER_ID && s.finalScore != null)
      .sort((a, b) => a.submittedAt.localeCompare(b.submittedAt))
      .map((s) => {
        const task = tasks.find((t) => t.taskId === s.taskId)
        return {
          label: (task?.taskName ?? s.taskId).slice(0, 10),
          score: s.finalScore as number,
        }
      })
    const demoTrend = [
      { label: "阶段一", score: 76 },
      { label: "阶段二", score: 82 },
      { label: "数据库设计", score: 88.5 },
      { label: "Web作业", score: 85 },
      { label: "模型训练", score: 92 },
    ]
    const trendPoints = scoredPoints.length >= 2
      ? scoredPoints
      : [...scoredPoints, ...demoTrend.slice(scoredPoints.length)].slice(0, 5)

    return ok({
      stats: {
        pendingTasks: pending.length,
        completedTasks: completed.length,
        averageScore: avg,
        classAverageScore: 78.3,
        analyzingCount: analyzing.length,
        improvementCount: 3,
        nearestDeadline: deadlines[0]?.slice(0, 10) ?? null,
        analyzingEtaMinutes: analyzing.length ? 5 : 0,
      },
      recentTasks: recent.slice(0, 5).map((t) => ({
        taskId: t.taskId,
        taskName: t.taskName,
        courseName: t.courseName,
        deadline: t.deadline,
        status: t.mySubmissionStatus,
        score: t.myScore,
      })),
      scoreTrend: {
        xAxis: trendPoints.map((p) => p.label),
        myScores: trendPoints.map((p) => p.score),
        classAvg: trendPoints.map((_, i) => +(72 + i * 1.5).toFixed(1)),
      },
      radarData: {
        categories: ["代码", "需求", "项目", "协作", "文档", "创新"],
        myScores: [86, 78, 82, 74, 80, 70],
        classAvg: [78, 76, 75, 72, 74, 68],
      },
      aiSuggestions: [
        "异常处理规范：关键路径缺少统一错误码与日志",
        "单元测试编写：核心服务层覆盖率偏低，建议补齐边界用例",
        "接口文档完整性：缺少请求示例与错误响应说明",
      ],
    })
  })

  // GET /api/v1/student/courses
  mock.mock(/\/api\/v1\/student\/courses(\?.*)?$/, "get", (options: { url: string }) => {
    const url = new URL(`http://localhost${options.url}`)
    const keyword = (url.searchParams.get("keyword") || "").toLowerCase()
    const myClass = demoStudent().className
    const enrolledIds = new Set(
      SEED_COURSES.filter((c) => c.className === myClass).map((c) => c.courseId),
    )
    submissions
      .filter((s) => s.studentUserId === DEMO_STUDENT_USER_ID)
      .forEach((s) => {
        const t = tasks.find((x) => x.taskId === s.taskId)
        if (t) enrolledIds.add(t.courseId)
      })

    let courses = SEED_COURSES
      .filter((c) => enrolledIds.has(c.courseId))
      .map((c) => ({
        courseId: c.courseId,
        courseName: c.courseName,
        courseCode: c.courseCode,
        teacherName: c.teacherName,
        teacherEmail: c.teacherEmail,
        semester: c.semester,
        credits: c.credits,
        taskCount: tasks.filter((t) => t.courseId === c.courseId && t.status !== "DRAFT").length,
        description: c.description,
      }))

    if (keyword) {
      courses = courses.filter((c) => c.courseName.toLowerCase().includes(keyword))
    }

    return ok({
      list: courses,
      page: 1,
      pageSize: 20,
      total: courses.length,
      totalPages: 1,
    })
  })

  // GET /api/v1/student/courses/:courseId
  mock.mock(/\/api\/v1\/student\/courses\/(?!\?).+$/, "get", (options: { url: string }) => {
    const path = options.url.split("?")[0]
    const courseId = path.split("/").pop()!
    const course = SEED_COURSES.find((c) => c.courseId === courseId)
    if (!course) return err(4001, "课程不存在")

    const courseTasks = tasks
      .filter((t) => t.courseId === courseId && t.status !== "DRAFT")
      .map((t) => {
        const mine = studentStatusForTask(t.taskId, DEMO_STUDENT_USER_ID, submissions)
        return {
          taskId: t.taskId,
          taskName: t.taskName,
          deadline: t.deadline.replace("T", " "),
          totalScore: t.totalScore,
          mySubmissionStatus: mine.status,
          myScore: mine.score,
        }
      })

    return ok({
      courseId: course.courseId,
      courseName: course.courseName,
      courseCode: course.courseCode,
      teacherName: course.teacherName,
      teacherEmail: course.teacherEmail,
      semester: course.semester,
      credits: course.credits,
      taskCount: courseTasks.length,
      description: course.description,
      tasks: courseTasks,
    })
  })

  // GET /api/v1/student/tasks
  mock.mock(/\/api\/v1\/student\/tasks(\?.*)?$/, "get", (options: { url: string }) => {
    const url = new URL(`http://localhost${options.url}`)
    const status = url.searchParams.get("status") || ""
    const keyword = (url.searchParams.get("keyword") || "").toLowerCase()

    let list = publishedTasksForStudent().map(toStudentTaskItem)
    if (status) list = list.filter((t) => t.mySubmissionStatus === status)
    if (keyword) list = list.filter((t) => t.taskName.toLowerCase().includes(keyword))

    return ok({
      list,
      page: 1,
      pageSize: 20,
      total: list.length,
      totalPages: 1,
    })
  })

  // GET /api/v1/student/tasks/:taskId
  mock.mock(/\/api\/v1\/student\/tasks\/[^/]+$/, "get", (options: { url: string }) => {
    const path = options.url.split("?")[0]
    const taskId = path.split("/").pop()!
    const task = tasks.find((t) => t.taskId === taskId)
    if (!task) return err(4001, "任务不存在")
    const course = courseOf(task)
    const mine = studentStatusForTask(taskId, DEMO_STUDENT_USER_ID, submissions)

    return ok({
      taskId: task.taskId,
      taskName: task.taskName,
      courseName: course?.courseName ?? "",
      teacherName: course?.teacherName ?? DEMO_TEACHER_NAME,
      teacherEmail: course?.teacherEmail ?? DEMO_TEACHER_EMAIL,
      description: task.description,
      requirements: task.requirements,
      deadline: task.deadline.replace("T", " "),
      totalScore: task.totalScore,
      submissionType: task.submissionType,
      submitLimit: task.maxSubmitCount,
      evaluationDimensions: EVAL_DIMENSIONS,
      attachments: [
        {
          fileId: `file-${task.taskId}-spec`,
          fileName: `${task.taskName}-说明.pdf`,
          fileSize: 245760,
          fileType: "application/pdf",
        },
      ],
      mySubmissionStatus: mine.status,
      mySubmissionId: mine.submissionId,
      myScore: mine.score,
      mySubmitCount: mine.submitCount,
      maxSubmitCount: task.maxSubmitCount,
      createdAt: task.createdAt.replace("T", " "),
      updatedAt: task.publishTime?.replace("T", " ") ?? task.createdAt.replace("T", " "),
    })
  })

  // GET /api/v1/student/tasks/:taskId/submissions
  mock.mock(/\/api\/v1\/student\/tasks\/.+\/submissions$/, "get", (options: { url: string }) => {
    const taskId = options.url.split("/")[5]
    const task = tasks.find((t) => t.taskId === taskId)
    const items = submissions
      .filter((s) => s.taskId === taskId && s.studentUserId === DEMO_STUDENT_USER_ID)
      .map((s) => ({
        submissionId: s.submissionId,
        taskId: s.taskId,
        taskName: task?.taskName ?? "",
        submissionType: s.submitType,
        status: s.status === "APPROVED" ? "COMPLETED" : s.status,
        aiScore: s.aiScore,
        submittedAt: s.submittedAt.replace("T", " "),
        remark: s.reviewComment,
      }))
    return ok(items)
  })

  // POST /api/v1/student/tasks/:taskId/submissions
  mock.mock(/\/api\/v1\/student\/tasks\/.+\/submissions$/, "post", (options: { url: string; body: string }) => {
    const taskId = options.url.split("/")[5]
    const task = tasks.find((t) => t.taskId === taskId)
    if (!task) return err(4001, "任务不存在")

    const mine = studentStatusForTask(taskId, DEMO_STUDENT_USER_ID, submissions)
    if (mine.submitCount >= task.maxSubmitCount) {
      return err(1005, `已达到提交次数上限（${task.maxSubmitCount}次），无法再次提交`)
    }
    if (new Date(task.deadline).getTime() < Date.now() && task.status === "CLOSED") {
      return err(1006, "提交失败：该实训任务已截止")
    }

    let body: { submissionType?: string; gitUrl?: string; fileName?: string; fileSize?: number } = {}
    try {
      body = JSON.parse(options.body || "{}")
    } catch {
      body = {}
    }

    const submissionId = `SUB${String(submissions.length + 1).padStart(3, "0")}`
    const newSub: SeedSubmission = {
      submissionId,
      studentUserId: DEMO_STUDENT_USER_ID,
      taskId,
      submittedAt: new Date().toISOString().slice(0, 19),
      status: "SUBMITTED",
      submitType: (body.submissionType as "GIT_URL" | "ZIP_UPLOAD") || task.submissionType,
      submitCount: mine.submitCount + 1,
      isLate: new Date(task.deadline).getTime() < Date.now() ? 1 : 0,
      finalScore: null,
      aiScore: null,
      reviewedAt: null,
      reviewComment: null,
      rejectReason: null,
      gitUrl: body.gitUrl,
      fileName: body.fileName,
      fileSize: body.fileSize,
    }
    submissions.push(newSub)

    return ok({
      submissionId,
      taskId,
      submissionType: newSub.submitType,
      status: "SUBMITTED",
      submitCount: newSub.submitCount,
      maxSubmitCount: task.maxSubmitCount,
      submittedAt: newSub.submittedAt.replace("T", " "),
    }, "提交成功")
  })

  // POST /api/v1/student/tasks/:taskId/git-verify
  mock.mock(/\/api\/v1\/student\/tasks\/.+\/git-verify$/, "post", (options: { body: string }) => {
    const { gitUrl } = JSON.parse(options.body)
    if (gitUrl.includes("private")) return err(8002, "无权限访问该仓库")
    if (gitUrl.includes("invalid") || (!gitUrl.includes("github") && !gitUrl.includes("gitee"))) {
      return err(8001, "仓库不存在或无法访问")
    }
    return ok({
      valid: true,
      repoName: gitUrl.split("/").pop() || "demo-repo",
      defaultBranch: "main",
      branches: ["main", "develop"],
      latestCommit: {
        commitId: "a1b2c3d",
        message: "feat: 提交实训作业",
        author: demoStudent().realName,
        committedAt: "2026-06-20 09:30:00",
      },
    })
  })

  // POST /api/v1/student/submissions/:submissionId/ai-evaluate
  mock.mock(/\/api\/v1\/student\/submissions\/.+\/ai-evaluate$/, "post", (options: { url: string }) => {
    const submissionId = options.url.split("/")[5]
    const sub = submissions.find((s) => s.submissionId === submissionId)
    if (sub) sub.status = "AI_EVALUATING"
    aiPollCounters.set(submissionId, 0)
    return ok({
      analyzeId: `analyze-${submissionId}`,
      submissionId,
      status: "PENDING",
      estimatedSeconds: 30,
    })
  })

  // GET /api/v1/student/submissions/:submissionId/ai-result
  mock.mock(/\/api\/v1\/student\/submissions\/.+\/ai-result$/, "get", (options: { url: string }) => {
    const submissionId = options.url.split("/")[5]
    const sub = submissions.find((s) => s.submissionId === submissionId)
    const count = (aiPollCounters.get(submissionId) || 0) + 1
    aiPollCounters.set(submissionId, count)

    if (count <= 3) {
      const dims = ["代码规范", "功能完成度", "创新性", "文档完整性", "Git规范"]
      return ok({
        analyzeId: `analyze-${submissionId}`,
        status: "PROCESSING",
        progress: count * 30,
        currentDimension: dims[count - 1],
        result: null,
        startedAt: new Date(Date.now() - 30000).toISOString(),
      })
    }

    if (sub) {
      sub.status = "AI_COMPLETED"
      sub.aiScore = sub.aiScore ?? 82
    }

    return ok({
      analyzeId: `analyze-${submissionId}`,
      status: "COMPLETED",
      progress: 100,
      currentDimension: null,
      result: makeAIScoreResult(sub?.aiScore),
      startedAt: new Date(Date.now() - 90000).toISOString(),
      completedAt: new Date().toISOString(),
    })
  })

  // GET /api/v1/student/submissions/:submissionId/ai-result-detail
  mock.mock(/\/api\/v1\/student\/submissions\/.+\/ai-result-detail$/, "get", (options: { url: string }) => {
    const submissionId = options.url.split("/")[5]
    const sub = submissions.find((s) => s.submissionId === submissionId) || SEED_SUBMISSIONS.find((s) => s.submissionId === submissionId)
    const task = tasks.find((t) => t.taskId === sub?.taskId)
    const score = sub?.aiScore ?? 82

    return ok({
      submissionId,
      taskId: sub?.taskId ?? "C001_T1",
      taskName: task?.taskName ?? "用户管理系统 API 开发",
      aiScore: score,
      totalDeductions: 4,
      totalDeductScore: 14,
      modelVersion: "DeepSeek-Coder-V2",
      analyzedAt: "2026-06-20 10:15:00",
      codeSummary: "代码结构整体清晰，模块划分合理。主要问题集中在异常处理不够完善。",
      docSummary: "接口文档基本完整，但缺少部分参数的详细说明。",
      reqSummary: "核心功能需求均已实现，部分非功能性需求尚未覆盖。",
      deductions: [
        {
          agentType: "Code",
          severity: "major",
          issueType: "异常处理不完善",
          suggestDeduct: 5,
          reason: "UserService.createUser 未处理唯一约束冲突",
          filePath: "src/services/UserService.java",
          lineNumber: 45,
          confidence: 0.92,
        },
        {
          agentType: "Code",
          severity: "minor",
          issueType: "命名规范",
          suggestDeduct: 2,
          reason: "变量命名不够语义化",
          filePath: "src/utils/DataHelper.java",
          lineNumber: 128,
          confidence: 0.88,
        },
        {
          agentType: "Doc",
          severity: "major",
          issueType: "接口文档不完整",
          suggestDeduct: 4,
          reason: "权限接口返回值结构未说明",
          filePath: "docs/api.md",
          lineNumber: 88,
          confidence: 0.85,
        },
        {
          agentType: "Req",
          severity: "minor",
          issueType: "日志审计缺失",
          suggestDeduct: 3,
          reason: "敏感操作未记录审计日志",
          filePath: "docs/requirements.md",
          lineNumber: 42,
          confidence: 0.78,
        },
      ],
    })
  })

  // GET /api/v1/student/student-report
  mock.mock("/api/v1/student/student-report", "get", () => {
    const myTasks = publishedTasksForStudent()
    const mySubs = submissions.filter((s) => s.studentUserId === DEMO_STUDENT_USER_ID)
    const scored = mySubs
      .filter((s) => s.finalScore != null)
      .map((s) => {
        const task = tasks.find((t) => t.taskId === s.taskId)
        const course = task ? courseOf(task) : undefined
        return {
          taskName: task?.taskName ?? s.taskId,
          courseName: course?.courseName ?? "",
          aiScore: s.aiScore ?? s.finalScore!,
          finalScore: s.finalScore!,
          reviewedAt: (s.reviewedAt || s.submittedAt).replace("T", " "),
          status: s.status === "REJECTED" ? "REJECTED" : "REVIEWED",
          reviewComment: s.reviewComment,
        }
      })
      .sort((a, b) => a.reviewedAt.localeCompare(b.reviewedAt))

    const finals = scored.map((s) => s.finalScore)
    const avg = finals.length ? +(finals.reduce((a, b) => a + b, 0) / finals.length).toFixed(1) : null

    return ok({
      stats: {
        totalTasks: myTasks.length,
        completedTasks: scored.length,
        totalSubmissions: mySubs.length,
        averageScore: avg,
      },
      scoreTrend: {
        categories: scored.map((s) => s.taskName.slice(0, 8)),
        values: scored.map((s) => s.finalScore),
      },
      radarData: {
        indicators: ["功能实现", "代码质量", "文档质量", "创新能力", "答辩表现"],
        values: [86, 82, 78, 70, 80],
      },
      rows: scored.map((s) => ({
        taskName: s.taskName,
        courseName: s.courseName,
        score: s.finalScore,
        reviewComment: s.reviewComment,
        status: s.status,
      })),
    })
  })

  // GET /api/v1/student/growth-profile  — 对齐 IGrowthData
  mock.mock("/api/v1/student/growth-profile", "get", () => {
    const myTasks = publishedTasksForStudent()
    const mySubs = submissions.filter((s) => s.studentUserId === DEMO_STUDENT_USER_ID)
    const scored = mySubs.filter((s) => s.finalScore != null)
    const scores = scored.map((s) => s.finalScore!)
    const avg = scores.length ? +(scores.reduce((a, b) => a + b, 0) / scores.length).toFixed(1) : null

    const courseMap = new Map<string, { courseId: string; courseName: string; scores: number[]; taskIds: Set<string> }>()
    mySubs.forEach((s) => {
      const task = tasks.find((t) => t.taskId === s.taskId)
      if (!task) return
      const course = courseOf(task)
      if (!course) return
      if (!courseMap.has(course.courseId)) {
        courseMap.set(course.courseId, {
          courseId: course.courseId,
          courseName: course.courseName,
          scores: [],
          taskIds: new Set(),
        })
      }
      const entry = courseMap.get(course.courseId)!
      entry.taskIds.add(task.taskId)
      if (s.finalScore != null) entry.scores.push(s.finalScore)
    })

    const courseScores = Array.from(courseMap.values()).map((c) => ({
      courseId: c.courseId,
      courseName: c.courseName,
      avgScore: c.scores.length ? +(c.scores.reduce((a, b) => a + b, 0) / c.scores.length).toFixed(1) : null,
      taskCount: c.taskIds.size,
    }))

    const monthMap = new Map<string, { scores: number[]; count: number }>()
    mySubs.forEach((s) => {
      const month = s.submittedAt.slice(0, 7)
      if (!monthMap.has(month)) monthMap.set(month, { scores: [], count: 0 })
      const m = monthMap.get(month)!
      m.count += 1
      if (s.finalScore != null) m.scores.push(s.finalScore)
    })
    const monthlyTrends = Array.from(monthMap.entries())
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([month, m]) => ({
        month,
        avgScore: m.scores.length ? +(m.scores.reduce((a, b) => a + b, 0) / m.scores.length).toFixed(1) : null,
        submissionCount: m.count,
      }))

    return ok({
      totalTasks: myTasks.length,
      completedTasks: scored.length,
      totalSubmissions: mySubs.length,
      averageScore: avg,
      highestScore: scores.length ? Math.max(...scores) : null,
      lowestScore: scores.length ? Math.min(...scores) : null,
      courseScores,
      monthlyTrends,
      dimensionRadar: [
        { dimensionName: "代码规范", myAvg: 82, classAvg: 76 },
        { dimensionName: "功能完成度", myAvg: 88, classAvg: 78 },
        { dimensionName: "创新性", myAvg: 75, classAvg: 70 },
        { dimensionName: "文档完整性", myAvg: 80, classAvg: 74 },
        { dimensionName: "Git规范", myAvg: 85, classAvg: 72 },
      ],
      submissionHistory: mySubs
        .slice()
        .sort((a, b) => b.submittedAt.localeCompare(a.submittedAt))
        .map((s) => {
          const task = tasks.find((t) => t.taskId === s.taskId)
          const course = task ? courseOf(task) : undefined
          return {
            submissionId: s.submissionId,
            taskId: s.taskId,
            taskName: task?.taskName ?? s.taskId,
            courseName: course?.courseName ?? "",
            score: s.finalScore,
            result: s.status === "APPROVED" ? "通过" : s.status === "REJECTED" ? "退回" : "进行中",
            submittedAt: s.submittedAt.replace("T", " "),
          }
        }),
    })
  })

  // GET /api/v1/student/submissions/:submissionId/evaluation
  mock.mock(/\/api\/v1\/student\/submissions\/.+\/evaluation$/, "get", (options: { url: string }) => {
    const submissionId = options.url.split("/")[5]
    const sub = submissions.find((s) => s.submissionId === submissionId)
    if (!sub) return err(4001, "提交不存在")
    const task = tasks.find((t) => t.taskId === sub.taskId)
    const course = task ? courseOf(task) : undefined

    if (sub.status === "REJECTED") {
      return ok({
        submissionId,
        taskId: sub.taskId,
        taskName: task?.taskName ?? "",
        courseName: course?.courseName ?? "",
        submittedAt: sub.submittedAt.replace("T", " "),
        aiEvaluation: {
          overallScore: sub.aiScore ?? 68,
          summary: "代码质量一般，功能基本完成，但仍有明显问题需修正。",
          completedAt: (sub.reviewedAt || sub.submittedAt).replace("T", " "),
        },
        teacherEvaluation: null,
        status: "REJECTED",
        finalScore: null,
        rejectReason: sub.rejectReason || "请修改后重新提交。",
      })
    }

    const hasTeacher = sub.status === "APPROVED" && sub.finalScore != null
    return ok({
      submissionId,
      taskId: sub.taskId,
      taskName: task?.taskName ?? "",
      courseName: course?.courseName ?? "",
      submittedAt: sub.submittedAt.replace("T", " "),
      aiEvaluation: {
        overallScore: sub.aiScore ?? 80,
        summary: "代码结构清晰，功能实现完整。",
        completedAt: (sub.reviewedAt || sub.submittedAt).replace("T", " "),
      },
      teacherEvaluation: hasTeacher
        ? {
            overallScore: sub.finalScore!,
            comment: sub.reviewComment || "完成度较高，建议继续保持。",
            dimensions: EVAL_DIMENSIONS.map((d, i) => ({
              dimensionName: d.dimensionName,
              score: Math.min(100, Math.round((sub.finalScore || 80) - 2 + i)),
              maxScore: d.maxScore,
            })),
            scoredBy: DEMO_TEACHER_NAME,
            scoredAt: (sub.reviewedAt || "").replace("T", " "),
            publishedAt: (sub.reviewedAt || "").replace("T", " "),
          }
        : null,
      status: hasTeacher ? "COMPLETED" : sub.status,
      finalScore: sub.finalScore,
    })
  })
}
