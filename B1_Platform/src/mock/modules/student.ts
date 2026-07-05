import Mock from "mockjs"

const aiPollCounters: Map<string, number> = new Map()
const taskCache: Map<string, Record<string, unknown>> = new Map()

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

function makeDimensions() {
  return [
    { dimensionName: "代码规范", weight: 25, maxScore: 100 },
    { dimensionName: "功能完成度", weight: 30, maxScore: 100 },
    { dimensionName: "创新性", weight: 20, maxScore: 100 },
    { dimensionName: "文档完整性", weight: 15, maxScore: 100 },
    { dimensionName: "Git规范", weight: 10, maxScore: 100 },
  ]
}

function makeAIScoreResult() {
  const { Random } = Mock
  const dims = [
    { name: "代码规范", score: Random.integer(60, 95) },
    { name: "功能完成度", score: Random.integer(55, 92) },
    { name: "创新性", score: Random.integer(50, 90) },
    { name: "文档完整性", score: Random.integer(55, 88) },
    { name: "Git规范", score: Random.integer(60, 95) },
  ]
  const weights = [25, 30, 20, 15, 10]
  const overall = Math.round(dims.reduce((a, d, i) => a + (d.score * weights[i]) / 100, 0))

  return {
    overallScore: overall,
    dimensions: dims.map((d, i) => ({
      dimensionName: d.name,
      score: d.score,
      maxScore: 100,
      weight: weights[i],
      comment: `${d.name}方面表现${d.score >= 80 ? "优秀" : d.score >= 60 ? "良好" : "一般"}。`,
      suggestions: [`建议优化${d.name}相关规范`, `${d.name}可以进一步提高`],
      codeReferences: [
        `src/modules/${d.name.toLowerCase().replace(/\s/g, "-")}/index.ts:12-45`,
        `src/utils/helper.ts:88-120`,
      ],
    })),
    summary: "整体代码质量良好，功能实现完整，但在部分维度还有提升空间。",
    strengths: ["代码结构清晰", "功能实现完整", "注释规范"],
    weaknesses: ["部分变量命名不够语义化", "测试覆盖率偏低", "缺少错误边界处理"],
    improvementPlan: "建议在后续版本中加强单元测试覆盖，完善错误处理机制，统一代码风格。",
  }
}

export default function setupStudentMock(mock: typeof Mock): void {
  const { Random } = mock

  // GET /api/v1/student/dashboard
  mock.mock("/api/v1/student/dashboard", "get", () => {
    return ok({
      stats: {
        pendingTasks: Random.integer(0, 5),
        completedTasks: Random.integer(0, 8),
        averageScore: Random.float(60, 95, 1, 1),
        analyzingCount: Random.integer(0, 2),
      },
      recentTasks: Array.from({ length: Random.integer(1, 5) }, () => ({
        taskId: `task-${Random.guid()}`,
        taskName: Random.ctitle(5, 15),
        courseName: Random.pick(["软件工程实践", "数据库原理", "Web前端开发"]),
        deadline: Random.datetime("yyyy-MM-dd HH:mm:ss"),
        status: "PUBLISHED",
        score: Random.boolean() ? Random.integer(60, 95) : null,
      })),
      scoreTrend: {
        xAxis: ["2026-03", "2026-04", "2026-05", "2026-06"],
        myScores: [Random.integer(65, 78), Random.integer(70, 82), Random.integer(72, 85), Random.integer(75, 88)],
        classAvg: [Random.integer(68, 75), Random.integer(70, 76), Random.integer(70, 78), Random.integer(72, 80)],
      },
      radarData: {
        categories: ["代码规范", "功能完成度", "创新性", "文档完整性", "Git规范"],
        myScores: [Random.integer(60, 90), Random.integer(60, 90), Random.integer(50, 85), Random.integer(55, 88), Random.integer(60, 92)],
        classAvg: [Random.integer(65, 80), Random.integer(65, 80), Random.integer(60, 78), Random.integer(60, 78), Random.integer(65, 80)],
      },
    })
  })

  // GET /api/v1/student/courses
  mock.mock(/\/api\/v1\/student\/courses(\?.*)?$/, "get", (options: { url: string }) => {
    const url = new URL(`http://localhost${options.url}`)
    const keyword = (url.searchParams.get("keyword") || "").toLowerCase()

    const allCourses = [
      { courseName: "软件工程实践", courseCode: "CS301", teacherName: "李四", teacherEmail: "lisi@example.com", semester: "2025-2026-2", credits: 3, taskCount: 4, description: "本课程旨在通过实际项目训练学生的软件开发能力。" },
      { courseName: "数据库原理", courseCode: "CS201", teacherName: "赵六", teacherEmail: "zhaoliu@example.com", semester: "2025-2026-2", credits: 3, taskCount: 3, description: "学习关系数据库的设计与优化。" },
      { courseName: "Web前端开发", courseCode: "CS310", teacherName: "钱七", teacherEmail: "qianqi@example.com", semester: "2025-2026-2", credits: 2, taskCount: 2, description: "掌握现代前端框架开发技能。" },
      { courseName: "移动应用开发", courseCode: "CS320", teacherName: "李四", teacherEmail: "lisi@example.com", semester: "2026-2027-1", credits: 3, taskCount: 1, description: "学习移动端应用开发技术。" },
      { courseName: "人工智能导论", courseCode: "CS401", teacherName: "赵六", teacherEmail: "zhaoliu@example.com", semester: "2026-2027-1", credits: 4, taskCount: 5, description: "人工智能基础知识与实践。" },
      { courseName: "计算机网络", courseCode: "CS210", teacherName: "钱七", teacherEmail: "qianqi@example.com", semester: "2025-2026-2", credits: 3, taskCount: 3, description: "计算机网络协议与架构。" },
    ]

    let courses = allCourses.map((c) => ({
      ...c,
      courseId: `course-${Random.guid()}`,
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
  mock.mock(/\/api\/v1\/student\/courses\/(?!\?).+/, "get", (options: { url: string }) => {
    const path = options.url.split("?")[0]
    const courseId = path.split("/").pop()!

    return ok({
      courseId,
      courseName: "软件工程实践",
      courseCode: "CS301",
      teacherName: "李四",
      teacherEmail: "lisi@example.com",
      semester: "2025-2026-2",
      credits: 3,
      taskCount: 3,
      description: "本课程旨在通过实际项目训练学生的软件开发能力，涵盖需求分析、设计、编码、测试全流程。",
      tasks: Array.from({ length: Random.integer(1, 4) }, () => ({
        taskId: `task-${Random.guid()}`,
        taskName: Random.ctitle(5, 15),
        deadline: Random.datetime("yyyy-MM-dd HH:mm:ss"),
        totalScore: Random.integer(60, 100),
        mySubmissionStatus: Random.pick(["NOT_SUBMITTED", "SUBMITTED", "AI_COMPLETED", "COMPLETED"]),
        myScore: Random.boolean() ? Random.integer(60, 95) : null,
      })),
    })
  })

  // GET /api/v1/student/tasks
  mock.mock(/\/api\/v1\/student\/tasks(\?.*)?$/, "get", (options: { url: string }) => {
    const url = new URL(`http://localhost${options.url}`)
    const status = url.searchParams.get("status") || ""
    const keyword = (url.searchParams.get("keyword") || "").toLowerCase()
    const statuses = ["NOT_SUBMITTED", "SUBMITTED", "AI_EVALUATING", "AI_COMPLETED", "TEACHER_SCORING", "COMPLETED", "REJECTED"]
    const courses = ["软件工程实践", "数据库原理", "Web前端开发", "移动应用开发", "人工智能导论", "计算机网络"]
    const teachers = ["李四", "赵六", "钱七"]

    let tasks = Array.from({ length: Random.integer(8, 12) }, (_, i) => {
      const submissionStatus = statuses[Math.floor(Math.random() * 7)]
      const completed = ["AI_COMPLETED", "COMPLETED", "TEACHER_SCORING"].includes(submissionStatus)
      const taskId = `task-${Random.guid()}`
      const item = {
        taskId,
        taskName: `实训任务${i + 1}: ${Random.ctitle(5, 15)}`,
        courseName: courses[Math.floor(Math.random() * courses.length)],
        teacherName: teachers[Math.floor(Math.random() * teachers.length)],
        deadline: Random.datetime("yyyy-MM-dd HH:mm:ss"),
        totalScore: Random.integer(60, 100),
        submissionType: Random.pick(["GIT_URL", "ZIP_UPLOAD"]),
        status: Random.pick(["PUBLISHED", "PUBLISHED", "PUBLISHED", "CLOSED"]),
        mySubmissionStatus: submissionStatus,
        myScore: completed ? Random.integer(60, 100) : null,
        createdAt: Random.datetime("yyyy-MM-dd HH:mm:ss"),
      }
      taskCache.set(taskId, item as Record<string, unknown>)
      return item
    })

    if (status) {
      tasks = tasks.filter((t) => t.mySubmissionStatus === status)
    }
    if (keyword) {
      tasks = tasks.filter((t) => t.taskName.toLowerCase().includes(keyword))
    }

    return ok({
      list: tasks,
      page: 1,
      pageSize: 20,
      total: tasks.length,
      totalPages: 1,
    })
  })

  // GET /api/v1/student/tasks/:taskId
  mock.mock(/\/api\/v1\/student\/tasks\/[^/]+$/, "get", (options: { url: string }) => {
    const path = options.url.split("?")[0]
    const taskId = path.split("/").pop()!

    // Use cached task data if available (from list endpoint)
    const cached = taskCache.get(taskId) as Record<string, unknown> | undefined
    const submissionStatus = (cached?.mySubmissionStatus as string) ||
      Random.pick(["NOT_SUBMITTED", "SUBMITTED", "AI_EVALUATING", "AI_COMPLETED", "COMPLETED", "REJECTED"])

    return ok({
      taskId,
      taskName: cached?.taskName || "实现用户管理系统",
      courseName: cached?.courseName || "软件工程实践",
      teacherName: cached?.teacherName || "李四",
      teacherEmail: cached?.teacherEmail as string || "lisi@example.com",
      description: "实现一个完整的用户管理系统，包括用户注册、登录、权限管理等功能。",
      requirements: "1. 使用 Spring Boot 3 框架\n2. 集成 MySQL 数据库\n3. 实现 JWT 认证\n4. 单元测试覆盖率 ≥ 60%\n5. 提交源码及设计文档",
      deadline: (cached?.deadline as string) || Random.datetime("yyyy-MM-dd HH:mm:ss"),
      totalScore: cached?.totalScore || 100,
      submissionType: cached?.submissionType || Random.pick(["GIT_URL", "ZIP_UPLOAD"]),
      submitLimit: 3,
      evaluationDimensions: makeDimensions(),
      attachments: Array.from({ length: Random.integer(0, 2) }, () => ({
        fileId: `file-${Random.guid()}`,
        fileName: `${Random.word(5, 10)}.${Random.pick(["pdf", "docx", "zip"])}`,
        fileSize: Random.integer(1024, 10485760),
        fileType: Random.pick(["application/pdf", "application/zip", "application/msword"]),
      })),
      mySubmissionStatus: submissionStatus,
      mySubmissionId: submissionStatus === "NOT_SUBMITTED" ? null : `sub-${Random.guid()}`,
      myScore: cached?.myScore as number | null ?? (Random.boolean() ? Random.integer(60, 100) : null),
      mySubmitCount: Random.integer(0, 2),
      maxSubmitCount: 3,
      createdAt: (cached?.createdAt as string) || Random.datetime("yyyy-MM-dd HH:mm:ss"),
      updatedAt: Random.datetime("yyyy-MM-dd HH:mm:ss"),
    })
  })

  // GET /api/v1/student/tasks/:taskId/submissions — submission history
  mock.mock(/\/api\/v1\/student\/tasks\/.+\/submissions$/, "get", (options: { url: string }) => {
    const taskId = options.url.split("/")[5]
    const count = Random.integer(0, 3)
    const items = Array.from({ length: count }, () => ({
      submissionId: `sub-${Random.guid()}`,
      taskId,
      taskName: "实现用户管理系统",
      submissionType: Random.pick(["GIT_URL", "ZIP_UPLOAD"]),
      status: Random.pick(["AI_COMPLETED", "COMPLETED", "TEACHER_SCORING", "REJECTED"]),
      aiScore: Random.boolean() ? Random.integer(60, 95) : null,
      submittedAt: Random.datetime("yyyy-MM-dd HH:mm:ss"),
      remark: Random.boolean() ? Random.csentence(8, 20) : null,
    }))
    return ok(items)
  })

  // POST /api/v1/student/tasks/:taskId/submissions
  mock.mock(/\/api\/v1\/student\/tasks\/.+\/submissions$/, "post", (options: { url: string }) => {
    const taskId = options.url.split("/")[5]

    // Simulate submit limit (20% chance)
    if (Math.random() < 0.2) {
      return err(1005, "已达到提交次数上限（3次），无法再次提交")
    }

    // Simulate deadline passed (10% chance)
    if (Math.random() < 0.1) {
      return err(1006, "提交失败：该实训任务已截止")
    }

    return ok({
      submissionId: `sub-${Random.guid()}`,
      taskId,
      submissionType: Random.pick(["GIT_URL", "ZIP_UPLOAD"]),
      status: "SUBMITTED",
      submitCount: 1,
      maxSubmitCount: 3,
      submittedAt: Random.datetime("yyyy-MM-dd HH:mm:ss"),
    }, "提交成功")
  })

  // POST /api/v1/student/tasks/:taskId/git-verify
  mock.mock(/\/api\/v1\/student\/tasks\/.+\/git-verify$/, "post", (options: { body: string }) => {
    const { gitUrl } = JSON.parse(options.body)

    if (gitUrl.includes("private")) {
      return err(8002, "无权限访问该仓库")
    }
    if (gitUrl.includes("invalid") || !gitUrl.includes("github")) {
      return err(8001, "仓库不存在或无法访问")
    }

    return ok({
      valid: true,
      repoName: Random.pick(["user-management-system", "spring-boot-demo", "vue-admin-template"]),
      defaultBranch: "main",
      branches: ["main", "develop", "feature/auth"],
      latestCommit: {
        commitId: Random.guid().substring(0, 7),
        message: Random.pick(["feat: 添加用户管理模块", "fix: 修复登录bug", "docs: 更新README"]),
        author: "张三",
        committedAt: Random.datetime("yyyy-MM-dd HH:mm:ss"),
      },
    })
  })

  // POST /api/v1/student/submissions/:submissionId/ai-evaluate
  mock.mock(/\/api\/v1\/student\/submissions\/.+\/ai-evaluate$/, "post", (options: { url: string }) => {
    const submissionId = options.url.split("/")[5]
    aiPollCounters.set(submissionId, 0)

    return ok({
      analyzeId: `analyze-${Random.guid()}`,
      submissionId,
      status: "PENDING",
      estimatedSeconds: 30,
    })
  })

  // GET /api/v1/student/submissions/:submissionId/ai-result (polling)
  mock.mock(/\/api\/v1\/student\/submissions\/.+\/ai-result$/, "get", (options: { url: string }) => {
    const submissionId = options.url.split("/")[5]
    const count = (aiPollCounters.get(submissionId) || 0) + 1
    aiPollCounters.set(submissionId, count)

    if (count <= 3) {
      const progress = count * 30
      const dims = ["代码规范", "功能完成度", "创新性", "文档完整性", "Git规范"]
      return ok({
        analyzeId: `analyze-${submissionId}`,
        status: "PROCESSING",
        progress,
        currentDimension: dims[count - 1],
        result: null,
        startedAt: new Date(Date.now() - 30000).toISOString(),
      })
    }

    return ok({
      analyzeId: `analyze-${submissionId}`,
      status: "COMPLETED",
      progress: 100,
      currentDimension: null,
      result: makeAIScoreResult(),
      startedAt: new Date(Date.now() - 90000).toISOString(),
      completedAt: new Date().toISOString(),
    })
  })

  // GET /api/v1/student/submissions/:submissionId/ai-result-detail
  mock.mock(/\/api\/v1\/student\/submissions\/.+\/ai-result-detail$/, "get", (options: { url: string }) => {
    const submissionId = options.url.split("/")[5]
    const score = Random.integer(55, 98)
    const deductScore = Random.integer(2, 15)

    return ok({
      submissionId,
      taskId: "task-001",
      taskName: "实现用户管理系统",
      aiScore: score,
      totalDeductions: Random.integer(2, 5),
      totalDeductScore: deductScore,
      modelVersion: "DeepSeek-Coder-V2",
      analyzedAt: Random.datetime("yyyy-MM-dd HH:mm:ss"),
      codeSummary: "代码结构整体清晰，模块划分合理。主要问题集中在异常处理不够完善，部分函数圈复杂度过高。命名规范基本符合要求，但仍有少量变量使用拼音命名。单元测试覆盖率约45%，建议提升至60%以上。",
      docSummary: "接口文档基本完整，但缺少部分参数的详细说明。README文件结构清晰，包含了项目介绍、技术栈和快速开始指南。API文档中部分返回值示例不完整。建议补充错误码说明和更多的使用示例。",
      reqSummary: "核心功能需求均已实现，包括用户注册、登录、权限管理和基本CRUD操作。但部分非功能性需求（如日志审计、操作记录）尚未覆盖。建议在下个迭代中完善安全相关的需求实现。",
      deductions: [
        {
          agentType: "Code",
          severity: "major",
          issueType: "异常处理不完善",
          suggestDeduct: 5,
          reason: "UserService.createUser 方法中未处理数据库唯一约束冲突异常，可能导致500错误直接暴露给前端",
          filePath: "src/services/UserService.java",
          lineNumber: 45,
          confidence: 0.92,
        },
        {
          agentType: "Code",
          severity: "minor",
          issueType: "命名规范",
          suggestDeduct: 2,
          reason: "变量 shuJu 使用拼音命名，应改为 data 或更语义化的英文名称",
          filePath: "src/utils/DataHelper.java",
          lineNumber: 128,
          confidence: 0.88,
        },
        {
          agentType: "Doc",
          severity: "major",
          issueType: "接口文档不完整",
          suggestDeduct: 4,
          reason: "/api/users/{id}/permissions 接口的返回值结构未在文档中说明",
          filePath: "docs/api.md",
          lineNumber: 88,
          confidence: 0.85,
        },
        {
          agentType: "Req",
          severity: "minor",
          issueType: "日志审计缺失",
          suggestDeduct: 3,
          reason: "需求规格说明书要求所有敏感操作需记录审计日志，但代码中未找到相关实现",
          filePath: "docs/requirements.md",
          lineNumber: 42,
          confidence: 0.78,
        },
      ],
    })
  })

  // GET /api/v1/student/reports
  mock.mock("/api/v1/student/reports", "get", () => {
    const scores = Array.from({ length: Random.integer(3, 6) }, () => ({
      taskName: Random.ctitle(5, 15),
      courseName: Random.pick(["软件工程实践", "数据库原理", "Web前端开发", "计算机网络"]),
      aiScore: Random.float(60, 95, 1, 1),
      finalScore: Random.integer(60, 95),
      reviewedAt: Random.datetime("yyyy-MM-dd HH:mm:ss"),
      status: Random.pick(["approved", "approved", "approved", "rejected"]),
    }))

    scores.sort((a, b) => new Date(a.reviewedAt).getTime() - new Date(b.reviewedAt).getTime())

    const finalScores = scores.map((s) => s.finalScore)
    const avg = finalScores.length > 0 ? (finalScores.reduce((a, b) => a + b, 0) / finalScores.length).toFixed(1) : "-"
    const maxS = finalScores.length > 0 ? Math.max(...finalScores).toFixed(1) : "-"
    const minS = finalScores.length > 0 ? Math.min(...finalScores).toFixed(1) : "-"

    return ok({
      stats: {
        completedTasks: scores.length,
        averageScore: avg,
        maxScore: maxS,
        minScore: minS,
      },
      scoreTrend: {
        categories: scores.map((s) => s.taskName.substring(0, 6)),
        values: scores.map((s) => s.finalScore),
      },
      radarData: {
        indicators: ["功能实现", "代码质量", "文档质量", "创新能力", "答辩表现"],
        values: [Random.integer(65, 90), Random.integer(60, 88), Random.integer(55, 85), Random.integer(50, 80), Random.integer(60, 85)],
      },
      rows: scores,
    })
  })

  // GET /api/v1/student/growth
  mock.mock("/api/v1/student/growth", "get", () => {
    const categories = ["代码能力", "文档能力", "工程规范", "创新能力", "团队协作"]
    const entries: Array<{ date: string; category: string; description: string; score: number }> = []

    const baseDates = ["2026-03-15", "2026-04-10", "2026-05-08", "2026-05-28", "2026-06-15"]
    const descs: Record<string, string[]> = {
      "代码能力": ["首次代码评审通过，代码规范良好", "代码重构后圈复杂度降低30%"],
      "文档能力": ["完成接口文档初版编写", "文档补充了错误码和使用示例"],
      "工程规范": ["开始使用Git Flow分支管理", "单元测试覆盖率达到65%"],
      "创新能力": ["提出UI交互优化方案被采纳", "引入自动化部署流程"],
      "团队协作": ["首次Code Review参与", "主导技术分享会"],
    }

    categories.forEach((cat) => {
      const baseScore = Random.integer(45, 65)
      entries.push({
        date: baseDates[0],
        category: cat,
        description: descs[cat][0],
        score: baseScore,
      })
      entries.push({
        date: baseDates[Random.integer(2, 4)],
        category: cat,
        description: descs[cat][1],
        score: Math.min(baseScore + Random.integer(8, 20), 95),
      })
    })

    entries.sort((a, b) => a.date.localeCompare(b.date))

    const allDates = [...new Set(entries.map((e) => e.date))].sort()
    const latestScore = entries[entries.length - 1]?.score || 0
    const firstScore = entries[0]?.score || 0

    return ok({
      stats: {
        totalEntries: entries.length,
        latestScore,
        firstScore,
        improvement: latestScore - firstScore,
      },
      entries,
      evolutionData: {
        categories,
        allDates,
        series: categories.map((cat) => {
          const catEntries = entries.filter((e) => e.category === cat).sort((a, b) => a.date.localeCompare(b.date))
          return {
            name: cat,
            data: allDates.map((d) => {
              const entry = catEntries.find((e) => e.date === d)
              return entry ? entry.score : null
            }),
          }
        }),
      },
    })
  })

  // GET /api/v1/student/submissions/:submissionId/evaluation
  mock.mock(/\/api\/v1\/student\/submissions\/.+\/evaluation$/, "get", (options: { url: string }) => {
    const submissionId = options.url.split("/")[5]
    const hasTeacher = Random.boolean()
    const rejected = Math.random() < 0.1

    if (rejected) {
      return ok({
        submissionId,
        taskName: "实现用户管理系统",
        courseName: "软件工程实践",
        submittedAt: Random.datetime("yyyy-MM-dd HH:mm:ss"),
        aiEvaluation: {
          overallScore: Random.integer(60, 85),
          summary: "代码质量良好，功能基本完成。",
          completedAt: Random.datetime("yyyy-MM-dd HH:mm:ss"),
        },
        teacherEvaluation: null,
        status: "REJECTED",
        finalScore: null,
        rejectReason: "代码中存在部分逻辑缺陷，请修复后重新提交。具体问题包括：用户登录验证逻辑不完善，权限控制模块缺少边界条件判断。",
      })
    }

    const aiScores = [Random.integer(60, 90), Random.integer(60, 90), Random.integer(50, 85), Random.integer(55, 88), Random.integer(60, 92)]

    return ok({
      submissionId,
      taskName: "实现用户管理系统",
      courseName: "软件工程实践",
      submittedAt: Random.datetime("yyyy-MM-dd HH:mm:ss"),
      aiEvaluation: {
        overallScore: Math.round(aiScores.reduce((a, s, i) => a + s * [25, 30, 20, 15, 10][i] / 100, 0)),
        summary: "代码结构清晰，功能实现完整。",
        completedAt: Random.datetime("yyyy-MM-dd HH:mm:ss"),
      },
      teacherEvaluation: hasTeacher ? {
        overallScore: Random.integer(70, 95),
        comment: "该同学项目完成度较高，代码规范良好，创新性较强。建议继续保持并加强文档编写能力。",
        dimensions: [
          { dimensionName: "代码规范", score: Random.integer(70, 92), maxScore: 100 },
          { dimensionName: "功能完成度", score: Random.integer(75, 95), maxScore: 100 },
          { dimensionName: "创新性", score: Random.integer(65, 88), maxScore: 100 },
          { dimensionName: "文档完整性", score: Random.integer(60, 85), maxScore: 100 },
          { dimensionName: "Git规范", score: Random.integer(70, 90), maxScore: 100 },
        ],
        scoredBy: "李四",
        scoredAt: Random.datetime("yyyy-MM-dd HH:mm:ss"),
        publishedAt: Random.datetime("yyyy-MM-dd HH:mm:ss"),
      } : null,
      status: hasTeacher ? "COMPLETED" : "AI_COMPLETED",
      finalScore: hasTeacher ? Random.integer(70, 95) : null,
    })
  })
}
