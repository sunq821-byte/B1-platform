/**
 * 师生端统一 Mock 种子数据
 * - 课程 / 任务 / 学生 / 提交使用同一套稳定 ID
 * - 演示账号：student01 → S001 张三；teacher01 → 李四（教授 C001–C003）
 */

export const DEMO_STUDENT_USER_ID = "S001"
export const DEMO_TEACHER_NAME = "李四"
export const DEMO_TEACHER_EMAIL = "lisi@example.com"

export interface SeedCourse {
  courseId: string
  courseCode: string
  courseName: string
  className: string
  semester: string
  credits: number
  teacherName: string
  teacherEmail: string
  description: string
  status: "ACTIVE" | "ARCHIVED"
}

export interface SeedTask {
  taskId: string
  taskName: string
  courseId: string
  description: string
  requirements: string
  deadline: string
  totalScore: number
  weight: number
  priority: "high" | "medium" | "low"
  status: "DRAFT" | "PUBLISHED" | "CLOSED"
  submissionType: "GIT_URL" | "ZIP_UPLOAD"
  maxSubmitCount: number
  gradingRule?: string
  createdAt: string
  publishTime?: string
}

export interface SeedStudent {
  userId: string
  studentId: string
  realName: string
  className: string
  email: string
  phone: string
}

export type SeedSubmissionStatus =
  | "SUBMITTED"
  | "AI_EVALUATING"
  | "AI_COMPLETED"
  | "TEACHER_SCORING"
  | "APPROVED"
  | "REJECTED"

export interface SeedSubmission {
  submissionId: string
  studentUserId: string
  taskId: string
  submittedAt: string
  status: SeedSubmissionStatus
  submitType: "GIT_URL" | "ZIP_UPLOAD"
  submitCount: number
  isLate: number
  finalScore: number | null
  aiScore: number | null
  reviewedAt: string | null
  reviewComment: string | null
  rejectReason: string | null
  fileName?: string
  fileSize?: number
  gitUrl?: string
}

export const SEED_COURSES: SeedCourse[] = [
  {
    courseId: "C001",
    courseCode: "CS301",
    courseName: "软件工程综合实训",
    className: "软件2401",
    semester: "2025-2026-2",
    credits: 4,
    teacherName: DEMO_TEACHER_NAME,
    teacherEmail: DEMO_TEACHER_EMAIL,
    description: "通过完整项目覆盖需求分析、设计、编码、测试与答辩全流程。",
    status: "ACTIVE",
  },
  {
    courseId: "C002",
    courseCode: "CS302",
    courseName: "数据库原理与应用",
    className: "软件2401",
    semester: "2025-2026-2",
    credits: 3,
    teacherName: DEMO_TEACHER_NAME,
    teacherEmail: DEMO_TEACHER_EMAIL,
    description: "关系数据库设计、SQL 优化与课程设计实践。",
    status: "ACTIVE",
  },
  {
    courseId: "C003",
    courseCode: "CS303",
    courseName: "Web前端开发",
    className: "软件2402",
    semester: "2025-2026-2",
    credits: 3,
    teacherName: DEMO_TEACHER_NAME,
    teacherEmail: DEMO_TEACHER_EMAIL,
    description: "现代前端工程化与响应式页面实战。",
    status: "ACTIVE",
  },
  {
    courseId: "C004",
    courseCode: "CS304",
    courseName: "人工智能导论",
    className: "计科2401",
    semester: "2025-2026-2",
    credits: 4,
    teacherName: "赵六",
    teacherEmail: "zhaoliu@example.com",
    description: "机器学习基础与模型训练实践。",
    status: "ACTIVE",
  },
]

export const SEED_TASKS: SeedTask[] = [
  {
    taskId: "C001_T1",
    taskName: "用户管理系统 API 开发",
    courseId: "C001",
    description: "设计并实现用户管理 REST API，含 CRUD、分页与 JWT 认证。",
    requirements: "1. Spring Boot 3\n2. MySQL\n3. JWT 认证\n4. 单元测试覆盖率 ≥ 60%\n5. 提交源码与设计文档",
    deadline: "2026-08-15T23:59:00",
    totalScore: 100,
    weight: 30,
    priority: "high",
    status: "PUBLISHED",
    submissionType: "GIT_URL",
    maxSubmitCount: 3,
    gradingRule: "AI_ASSIST",
    createdAt: "2026-02-20T08:00:00",
    publishTime: "2026-02-21T09:00:00",
  },
  {
    taskId: "C001_T2",
    taskName: "数据库设计与优化",
    courseId: "C001",
    description: "完成 MySQL 表设计、存储过程与查询优化。",
    requirements: "1. ER 图与建表脚本\n2. 至少 3 个优化案例\n3. 提交 PDF/ZIP 文档包",
    deadline: "2026-07-01T23:59:00",
    totalScore: 100,
    weight: 25,
    priority: "high",
    status: "PUBLISHED",
    submissionType: "ZIP_UPLOAD",
    maxSubmitCount: 3,
    gradingRule: "AI_ASSIST",
    createdAt: "2026-03-01T08:00:00",
    publishTime: "2026-03-02T09:00:00",
  },
  {
    taskId: "C001_T3",
    taskName: "软件工程实训 · 阶段三",
    courseId: "C001",
    description: "完成系统联调、缺陷修复与答辩材料准备。",
    requirements: "1. 联调报告\n2. 缺陷清单与修复说明\n3. 答辩 PPT 大纲",
    deadline: "2026-08-20T23:59:00",
    totalScore: 100,
    weight: 25,
    priority: "high",
    status: "PUBLISHED",
    submissionType: "GIT_URL",
    maxSubmitCount: 3,
    gradingRule: "AI_ASSIST",
    createdAt: "2026-06-01T08:00:00",
    publishTime: "2026-06-02T09:00:00",
  },
  {
    taskId: "C002_T1",
    taskName: "ER 图绘制与范式分析",
    courseId: "C002",
    description: "绘制概念 ER 图，进行范式分解并编写建表脚本。",
    requirements: "1. 概念/逻辑 ER 图\n2. 3NF 分解说明\n3. 建表 SQL",
    deadline: "2026-08-10T23:59:00",
    totalScore: 100,
    weight: 25,
    priority: "medium",
    status: "PUBLISHED",
    submissionType: "ZIP_UPLOAD",
    maxSubmitCount: 3,
    createdAt: "2026-03-05T08:00:00",
    publishTime: "2026-03-06T09:00:00",
  },
  {
    taskId: "C003_T1",
    taskName: "响应式页面布局实战",
    courseId: "C003",
    description: "使用 Flexbox/Grid 实现企业官网首页响应式布局。",
    requirements: "1. 桌面/平板/手机三断点\n2. 语义化 HTML\n3. 可访问性基础要求",
    deadline: "2026-08-25T23:59:00",
    totalScore: 100,
    weight: 30,
    priority: "medium",
    status: "DRAFT",
    submissionType: "ZIP_UPLOAD",
    maxSubmitCount: 3,
    createdAt: "2026-03-10T08:00:00",
  },
  {
    taskId: "C004_T1",
    taskName: "机器学习模型训练",
    courseId: "C004",
    description: "使用 scikit-learn 完成数据预处理、模型训练与评估。",
    requirements: "1. 数据清洗说明\n2. 至少两种模型对比\n3. 评估指标与结论",
    deadline: "2026-08-28T23:59:00",
    totalScore: 100,
    weight: 35,
    priority: "high",
    status: "PUBLISHED",
    submissionType: "GIT_URL",
    maxSubmitCount: 2,
    createdAt: "2026-03-15T08:00:00",
    publishTime: "2026-03-16T09:00:00",
  },
]

/** 软件2401 含演示学生张三；其余班级用于教师学生列表与报表 */
export const SEED_STUDENTS: SeedStudent[] = [
  { userId: "S001", studentId: "2024001", realName: "张三", className: "软件2401", email: "zhangsan@example.com", phone: "13800138001" },
  { userId: "S002", studentId: "2024002", realName: "李明", className: "软件2401", email: "liming@b1.edu.cn", phone: "13800138011" },
  { userId: "S003", studentId: "2024003", realName: "王五", className: "软件2402", email: "wangwu@b1.edu.cn", phone: "13800138012" },
  { userId: "S004", studentId: "2024004", realName: "赵丽", className: "软件2402", email: "zhaoli@b1.edu.cn", phone: "13800138013" },
  { userId: "S005", studentId: "2024005", realName: "钱七", className: "计科2401", email: "qianqi@b1.edu.cn", phone: "13800138014" },
  { userId: "S006", studentId: "2024006", realName: "孙八", className: "计科2401", email: "sunba@b1.edu.cn", phone: "13800138015" },
  { userId: "S007", studentId: "2024007", realName: "周九", className: "计科2402", email: "zhoujiu@b1.edu.cn", phone: "13800138016" },
]

/**
 * 提交记录（跨角色一致）
 * 张三(S001)：有待审、已完成、未提交任务，便于学生端全流程演示
 */
export const SEED_SUBMISSIONS: SeedSubmission[] = [
  {
    submissionId: "SUB001",
    studentUserId: "S001",
    taskId: "C001_T1",
    submittedAt: "2026-06-20T10:00:00",
    status: "TEACHER_SCORING",
    submitType: "GIT_URL",
    submitCount: 2,
    isLate: 0,
    finalScore: null,
    aiScore: 82,
    reviewedAt: null,
    reviewComment: null,
    rejectReason: null,
    gitUrl: "https://github.com/zhangsan/user-management-api",
  },
  {
    submissionId: "SUB002",
    studentUserId: "S002",
    taskId: "C001_T1",
    submittedAt: "2026-06-21T14:30:00",
    status: "AI_COMPLETED",
    submitType: "GIT_URL",
    submitCount: 1,
    isLate: 0,
    finalScore: null,
    aiScore: 78,
    reviewedAt: null,
    reviewComment: null,
    rejectReason: null,
    gitUrl: "https://github.com/liming/user-api",
  },
  {
    submissionId: "SUB003",
    studentUserId: "S003",
    taskId: "C001_T2",
    submittedAt: "2026-06-18T09:00:00",
    status: "SUBMITTED",
    submitType: "ZIP_UPLOAD",
    submitCount: 1,
    isLate: 0,
    finalScore: null,
    aiScore: null,
    reviewedAt: null,
    reviewComment: null,
    rejectReason: null,
    fileName: "数据库设计文档.zip",
    fileSize: 2457600,
  },
  {
    submissionId: "SUB004",
    studentUserId: "S001",
    taskId: "C001_T2",
    submittedAt: "2026-06-15T16:00:00",
    status: "APPROVED",
    submitType: "ZIP_UPLOAD",
    submitCount: 1,
    isLate: 0,
    finalScore: 88.5,
    aiScore: 86,
    reviewedAt: "2026-06-17T10:00:00",
    reviewComment: "设计完整，优化案例充分，文档结构清晰。",
    rejectReason: null,
    fileName: "设计方案.pdf",
    fileSize: 5120000,
  },
  {
    submissionId: "SUB005",
    studentUserId: "S004",
    taskId: "C002_T1",
    submittedAt: "2026-06-22T11:00:00",
    status: "SUBMITTED",
    submitType: "ZIP_UPLOAD",
    submitCount: 1,
    isLate: 0,
    finalScore: null,
    aiScore: null,
    reviewedAt: null,
    reviewComment: null,
    rejectReason: null,
    fileName: "ER图与范式分析.docx",
    fileSize: 1890400,
  },
  {
    submissionId: "SUB006",
    studentUserId: "S002",
    taskId: "C002_T1",
    submittedAt: "2026-06-10T08:00:00",
    status: "APPROVED",
    submitType: "ZIP_UPLOAD",
    submitCount: 1,
    isLate: 0,
    finalScore: 76,
    aiScore: 74,
    reviewedAt: "2026-06-12T14:00:00",
    reviewComment: "范式分析基本正确，建表脚本可再补充索引说明。",
    rejectReason: null,
    fileName: "ER分析.zip",
    fileSize: 1200000,
  },
  {
    submissionId: "SUB007",
    studentUserId: "S005",
    taskId: "C004_T1",
    submittedAt: "2026-06-25T13:00:00",
    status: "SUBMITTED",
    submitType: "GIT_URL",
    submitCount: 1,
    isLate: 0,
    finalScore: null,
    aiScore: null,
    reviewedAt: null,
    reviewComment: null,
    rejectReason: null,
    gitUrl: "https://github.com/qianqi/ml-lab",
  },
  {
    submissionId: "SUB008",
    studentUserId: "S001",
    taskId: "C004_T1",
    submittedAt: "2026-06-01T09:00:00",
    status: "APPROVED",
    submitType: "GIT_URL",
    submitCount: 1,
    isLate: 0,
    finalScore: 92,
    aiScore: 90,
    reviewedAt: "2026-06-03T11:00:00",
    reviewComment: "模型对比充分，结论清晰。",
    rejectReason: null,
    gitUrl: "https://github.com/zhangsan/ml-intro",
  },
  {
    submissionId: "SUB009",
    studentUserId: "S001",
    taskId: "C002_T1",
    submittedAt: "2026-07-02T15:20:00",
    status: "REJECTED",
    submitType: "ZIP_UPLOAD",
    submitCount: 1,
    isLate: 0,
    finalScore: null,
    aiScore: 68,
    reviewedAt: "2026-07-03T09:30:00",
    reviewComment: null,
    rejectReason: "ER 图缺少关系基数标注，请补全后重新提交。",
    fileName: "ER初稿.zip",
    fileSize: 980000,
  },
]

export const EVAL_DIMENSIONS = [
  { dimensionName: "代码规范", weight: 25, maxScore: 100 },
  { dimensionName: "功能完成度", weight: 30, maxScore: 100 },
  { dimensionName: "创新性", weight: 20, maxScore: 100 },
  { dimensionName: "文档完整性", weight: 15, maxScore: 100 },
  { dimensionName: "Git规范", weight: 10, maxScore: 100 },
]

export function courseById(courseId: string): SeedCourse | undefined {
  return SEED_COURSES.find((c) => c.courseId === courseId)
}

export function taskById(taskId: string): SeedTask | undefined {
  return SEED_TASKS.find((t) => t.taskId === taskId)
}

export function studentById(userId: string): SeedStudent | undefined {
  return SEED_STUDENTS.find((s) => s.userId === userId)
}

/** 运行时可变状态（师生 mock 共享，切换角色后数据一致） */
export const runtimeCourses: SeedCourse[] = SEED_COURSES.map((c) => ({ ...c }))
export const runtimeTasks: SeedTask[] = SEED_TASKS.map((t) => ({ ...t }))
export const runtimeStudents: SeedStudent[] = SEED_STUDENTS.map((s) => ({ ...s }))
export const runtimeSubmissions: SeedSubmission[] = SEED_SUBMISSIONS.map((s) => ({ ...s }))

export function cloneCourses(): SeedCourse[] {
  return runtimeCourses
}

export function cloneTasks(): SeedTask[] {
  return runtimeTasks
}

export function cloneStudents(): SeedStudent[] {
  return runtimeStudents
}

export function cloneSubmissions(): SeedSubmission[] {
  return runtimeSubmissions
}

/** 学生端：任务对应的本人提交状态 */
export function studentStatusForTask(
  taskId: string,
  studentUserId = DEMO_STUDENT_USER_ID,
  submissions: SeedSubmission[] = SEED_SUBMISSIONS,
): { status: string; submissionId: string | null; score: number | null; submitCount: number } {
  const mine = submissions
    .filter((s) => s.taskId === taskId && s.studentUserId === studentUserId)
    .sort((a, b) => b.submittedAt.localeCompare(a.submittedAt))
  if (mine.length === 0) {
    return { status: "NOT_SUBMITTED", submissionId: null, score: null, submitCount: 0 }
  }
  const latest = mine[0]
  const statusMap: Record<SeedSubmissionStatus, string> = {
    SUBMITTED: "SUBMITTED",
    AI_EVALUATING: "AI_EVALUATING",
    AI_COMPLETED: "AI_COMPLETED",
    TEACHER_SCORING: "TEACHER_SCORING",
    APPROVED: "COMPLETED",
    REJECTED: "REJECTED",
  }
  return {
    status: statusMap[latest.status],
    submissionId: latest.submissionId,
    score: latest.finalScore,
    submitCount: latest.submitCount,
  }
}

/** 教师端待审：尚未最终发布的提交 */
export function isPendingTeacherReview(status: SeedSubmissionStatus): boolean {
  return ["SUBMITTED", "AI_EVALUATING", "AI_COMPLETED", "TEACHER_SCORING"].includes(status)
}
