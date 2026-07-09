import { defineStore } from "pinia"
import { ref } from "vue"
import type {
  ICourseItem,
  ICourseFormData,
  ITeacherTaskItem,
  ITaskFormData,
  IStudentItem,
  IStudentDetail,
  IPendingSubmission,
  IAIDiagnosis,
  IPublishRequest,
  IClassReport,
  ICollegeReport,
  IDashboardData,
} from "@/types/teacher"
import * as teacherApi from "@/api/modules/teacher"

export const useTeacherStore = defineStore("teacher", () => {
  function extractList<T>(data: unknown, mapper: (raw: Record<string, unknown>) => T): T[] {
    if (!data) return []
    if (Array.isArray(data)) return data.map(mapper)
    const list = (data as Record<string, unknown>).list
    if (Array.isArray(list)) return list.map((item) => mapper(item as Record<string, unknown>))
    return []
  }

  // Courses
  const courses = ref<ICourseItem[]>([])
  const coursesLoading = ref(false)

  // Tasks
  const tasks = ref<ITeacherTaskItem[]>([])
  const tasksLoading = ref(false)

  // Students
  const students = ref<IStudentItem[]>([])
  const studentsLoading = ref(false)
  const currentStudent = ref<IStudentDetail | null>(null)

  // Submissions
  const pendingSubmissions = ref<IPendingSubmission[]>([])
  const submissionsLoading = ref(false)
  const submissionsTotal = ref(0)
  const currentDiagnosis = ref<IAIDiagnosis | null>(null)
  const currentSubmissionId = ref("")

  // Dashboard
  const dashboardData = ref<IDashboardData | null>(null)
  const dashboardLoading = ref(false)

  // Reports
  const classReport = ref<IClassReport | null>(null)
  const classReportLoading = ref(false)
  const collegeReport = ref<ICollegeReport | null>(null)
  const collegeReportLoading = ref(false)

  // === Courses ===
  function mapCourse(raw: Record<string, unknown>): ICourseItem {
    return {
      courseId: String(raw.courseId ?? ''),
      courseCode: String(raw.courseCode ?? ''),
      courseName: String(raw.courseName ?? ''),
      className: String(raw.className || raw.courseName || ''),
      semester: String(raw.semester ?? ''),
      credits: Number(raw.credits ?? 0),
      studentCount: Number(raw.studentCount ?? 0),
      taskCount: Number(raw.taskCount ?? 0),
      status: String(raw.status ?? ''),
    }
  }

  async function fetchCourses(): Promise<void> {
    coursesLoading.value = true
    try {
      courses.value = extractList(await teacherApi.fetchCourses(), mapCourse)
    } finally { coursesLoading.value = false }
  }

  async function createCourse(data: ICourseFormData): Promise<ICourseItem> {
    const c = mapCourse(await teacherApi.createCourse(data) as unknown as Record<string, unknown>)
    courses.value.push(c)
    return c
  }

  async function updateCourse(courseId: string, data: ICourseFormData): Promise<void> {
    await teacherApi.updateCourse(courseId, data)
    const idx = courses.value.findIndex((c) => c.courseId === courseId)
    if (idx >= 0) courses.value[idx] = { ...courses.value[idx], ...data }
  }

  async function deleteCourse(courseId: string): Promise<void> {
    await teacherApi.deleteCourse(courseId)
    courses.value = courses.value.filter((c) => c.courseId !== courseId)
  }

  // === Tasks ===
  function mapTask(raw: Record<string, unknown>): ITeacherTaskItem {
    const status = String(raw.status ?? 'DRAFT')
    return {
      taskId: String(raw.taskId ?? ''),
      taskName: String(raw.taskName ?? ''),
      courseName: String(raw.courseName ?? ''),
      description: String(raw.description ?? ''),
      deadline: raw.deadline ? String(raw.deadline) : '',
      totalScore: Number(raw.totalScore ?? 0),
      priority: 'medium',
      status: status.toLowerCase(),
      maxScore: Number(raw.totalScore ?? 100),
      createdAt: raw.publishTime ? String(raw.publishTime) : String(raw.createTime ?? ''),
      submissionType: String(raw.submissionType ?? ''),
      submissionCount: Number(raw.submissionCount ?? 0),
      reviewedCount: Number(raw.reviewedCount ?? 0),
      gradingRule: raw.gradingRule ? String(raw.gradingRule) : undefined,
    }
  }

  async function fetchTasks(params?: { courseId?: string }): Promise<void> {
    tasksLoading.value = true
    try {
      tasks.value = extractList(await teacherApi.fetchTeacherTasks(params), mapTask)
    } finally { tasksLoading.value = false }
  }

  async function createTask(data: ITaskFormData): Promise<ITeacherTaskItem> {
    const t = await teacherApi.createTask(data)
    tasks.value.push(t)
    return t
  }

  async function updateTask(taskId: string, data: ITaskFormData): Promise<void> {
    await teacherApi.updateTask(taskId, data)
    const idx = tasks.value.findIndex((t) => t.taskId === taskId)
    if (idx >= 0) {
      const updated = { ...tasks.value[idx] }
      if (data.taskName !== undefined) updated.taskName = data.taskName
      if (data.description !== undefined) updated.description = data.description
      if (data.dueDate !== undefined) updated.deadline = data.dueDate
      if (data.priority !== undefined) updated.priority = data.priority
      if (data.courseId !== undefined) {
        const course = courses.value.find((c) => c.courseId === data.courseId)
        if (course) updated.courseName = course.courseName
      }
      tasks.value[idx] = updated
    }
  }

  async function deleteTask(taskId: string): Promise<void> {
    await teacherApi.deleteTask(taskId)
    tasks.value = tasks.value.filter((t) => t.taskId !== taskId)
  }

  async function publishTask(taskId: string): Promise<void> {
    await teacherApi.publishTask(taskId)
    const t = tasks.value.find((x) => x.taskId === taskId)
    if (t) t.status = "published"
  }

  // === Students ===
  function mapStudent(raw: Record<string, unknown>): IStudentItem {
    const userId = String(raw.userId ?? '')
    const realName = String(raw.realName ?? '')
    return {
      userId,
      studentId: userId,
      name: realName,
      realName,
      className: String(raw.className || ''),
      email: String(raw.email ?? ''),
      phone: String(raw.phone ?? ''),
      completedCount: Number(raw.submissionCount ?? ''),
      submissionCount: Number(raw.submissionCount ?? 0),
      avgScore: null,
    }
  }

  async function fetchStudents(params?: { className?: string; keyword?: string }): Promise<void> {
    studentsLoading.value = true
    try {
      students.value = extractList(await teacherApi.fetchStudents(params || {}), mapStudent)
    } finally { studentsLoading.value = false }
  }

  async function fetchStudentDetail(userId: string): Promise<void> {
    currentStudent.value = await teacherApi.fetchStudentDetail(userId)
  }

  // === Submissions ===
  function mapSubmission(raw: Record<string, unknown>): IPendingSubmission {
    const submitType = String(raw.submitType ?? 'FILE')
    return {
      submissionId: String(raw.submissionId ?? ''),
      studentName: String(raw.studentName ?? ''),
      studentUserId: String(raw.studentUserId ?? ''),
      studentEmail: String(raw.studentEmail ?? ''),
      taskId: String(raw.taskId ?? ''),
      taskName: String(raw.taskName ?? ''),
      submittedAt: raw.submittedAt ? String(raw.submittedAt) : '',
      status: String(raw.status ?? 'SUBMITTED').toLowerCase(),
      submissionType: submitType === 'GIT_URL' ? 'code' : 'file',
      submitType,
      submitCount: Number(raw.submitCount ?? 1),
      isLate: Number(raw.isLate ?? 0),
      hasReview: Boolean(raw.hasReview ?? false),
      attachments: [],
    }
  }

  async function fetchPendingSubmissions(): Promise<void> {
    submissionsLoading.value = true
    try {
      const page = await teacherApi.fetchPendingSubmissions() as unknown as { list: Array<Record<string, unknown>>; total: number }
      pendingSubmissions.value = (page?.list ?? []).map(mapSubmission)
      submissionsTotal.value = page?.total ?? 0
    } finally { submissionsLoading.value = false }
  }

  async function fetchAIDiagnosis(submissionId: string): Promise<void> {
    const raw = await teacherApi.fetchAIDiagnosis(submissionId) as unknown as Record<string, unknown>
    currentSubmissionId.value = submissionId
    if (!raw || raw.status !== "COMPLETED" || !raw.result) {
      currentDiagnosis.value = null
      return
    }
    const result = raw.result as Record<string, unknown>
    const dims = (result.dimensions ?? []) as Array<Record<string, unknown>>
    currentDiagnosis.value = {
      submissionId,
      aiScore: (result.overallScore as number) ?? 0,
      totalDeductions: dims.length,
      totalDeductScore: dims.reduce((sum: number, d) => sum + ((d.suggestDeduct as number) ?? 0), 0),
      deductions: dims.map((d) => ({
        agentType: (d.agentType as string) ?? "AI",
        issueType: (d.issueType as string) ?? "",
        suggestDeduct: (d.suggestDeduct as number) ?? 0,
        reason: (d.comment as string) ?? "",
        filePath: (d.filePath as string) ?? "",
        lineNumber: (d.lineNumber as number) ?? 0,
        confidence: (d.confidence as number) ?? 0,
        overridden: false,
      })),
      aiIssueLines: dims.map((d) => (d.lineNumber as number) ?? 0).filter((n: number) => n > 0),
      codePreview: (result.summary as string) ?? "",
    }
  }

  async function fetchSubmissionDetail(submissionId: string): Promise<void> {
    try {
      const detail = await teacherApi.fetchSubmissionDetail(submissionId) as unknown as { attachments: Array<{ fileId: string; fileName: string; fileSize: number; fileType: string; downloadUrl: string }> }
      const idx = pendingSubmissions.value.findIndex((s) => s.submissionId === submissionId)
      if (idx >= 0 && detail?.attachments) {
        pendingSubmissions.value[idx] = {
          ...pendingSubmissions.value[idx],
          attachments: detail.attachments.map((a) => ({
            fileId: String(a.fileId ?? ""),
            fileName: String(a.fileName ?? ""),
            fileSize: Number(a.fileSize ?? 0),
            fileType: String(a.fileType ?? ""),
            downloadUrl: String(a.downloadUrl ?? ""),
          })),
        }
      }
    } catch { /* ignore */ }
  }

  async function publishReview(submissionId: string, data: IPublishRequest): Promise<void> {
    await teacherApi.publishReview(submissionId, data)
    pendingSubmissions.value = pendingSubmissions.value.filter((s) => s.submissionId !== submissionId)
    currentDiagnosis.value = null
    currentSubmissionId.value = ""
  }

  // === Dashboard ===
  async function fetchDashboard(): Promise<void> {
    dashboardLoading.value = true
    try { dashboardData.value = await teacherApi.fetchDashboard() }
    finally { dashboardLoading.value = false }
  }

  // === Reports ===
  async function fetchClassReport(className?: string): Promise<void> {
    classReportLoading.value = true
    try { classReport.value = await teacherApi.fetchClassReport({ className }) }
    finally { classReportLoading.value = false }
  }

  async function fetchCollegeReport(): Promise<void> {
    collegeReportLoading.value = true
    try { collegeReport.value = await teacherApi.fetchCollegeReport() }
    finally { collegeReportLoading.value = false }
  }

  // === Reset ===
  function resetStore(): void {
    courses.value = []
    tasks.value = []
    students.value = []
    pendingSubmissions.value = []
    dashboardData.value = null
    classReport.value = null
    collegeReport.value = null
    currentStudent.value = null
    currentDiagnosis.value = null
  }

  return {
    courses, coursesLoading,
    tasks, tasksLoading,
    students, studentsLoading, currentStudent,
    pendingSubmissions, submissionsLoading, submissionsTotal, currentDiagnosis, currentSubmissionId,
    dashboardData, dashboardLoading,
    classReport, classReportLoading,
    collegeReport, collegeReportLoading,
    fetchDashboard,
    fetchCourses, createCourse, updateCourse, deleteCourse,
    fetchTasks, createTask, updateTask, deleteTask, publishTask,
    fetchStudents, fetchStudentDetail,
    fetchPendingSubmissions, fetchSubmissionDetail, fetchAIDiagnosis, publishReview,
    fetchClassReport, fetchCollegeReport,
    resetStore,
  }
})
