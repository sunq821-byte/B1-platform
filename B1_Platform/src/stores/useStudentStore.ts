import { defineStore } from "pinia"
import { ref } from "vue"
import type {
  IDashboardData,
  ICourseItem,
  ICourseDetail,
  ITaskItem,
  ITaskDetail,
  ISubmitRequest,
  ISubmitResponse,
  IGitVerifyRequest,
  IGitVerifyResponse,
  IAIResult,
  IEvaluationDetail,
  IAIResultDetail,
  IStudentReportData,
  IGrowthData,
} from "@/types/student"
import * as studentApi from "@/api/modules/student"

export const useStudentStore = defineStore("student", () => {
  // Dashboard
  const dashboardData = ref<IDashboardData | null>(null)
  const dashboardLoading = ref(false)

  // Courses
  const courses = ref<ICourseItem[]>([])
  const coursesTotal = ref(0)
  const coursesLoading = ref(false)
  const currentCourse = ref<ICourseDetail | null>(null)

  // Tasks
  const tasks = ref<ITaskItem[]>([])
  const tasksTotal = ref(0)
  const tasksLoading = ref(false)
  const currentTask = ref<ITaskDetail | null>(null)

  // AI
  const aiResult = ref<IAIResult | null>(null)
  const aiPollingStatus = ref<"idle" | "polling" | "completed" | "timeout">("idle")
  const evaluation = ref<IEvaluationDetail | null>(null)
  const aiResultDetail = ref<IAIResultDetail | null>(null)

  // Reports & Growth
  const studentReport = ref<IStudentReportData | null>(null)
  const studentReportLoading = ref(false)
  const growthData = ref<IGrowthData | null>(null)
  const growthLoading = ref(false)

  let pollingTimer: ReturnType<typeof setInterval> | null = null
  let pollingAttempts = 0
  const MAX_ATTEMPTS = 40

  // === Dashboard ===
  async function fetchDashboard(): Promise<void> {
    dashboardLoading.value = true
    try {
      dashboardData.value = await studentApi.fetchDashboard()
    } finally {
      dashboardLoading.value = false
    }
  }

  // === Courses ===
  async function fetchCourses(params: {
    page: number
    pageSize: number
    keyword?: string
  }): Promise<void> {
    coursesLoading.value = true
    try {
      const res = await studentApi.fetchCourses(params)
      courses.value = res.list
      coursesTotal.value = res.total
    } finally {
      coursesLoading.value = false
    }
  }

  async function fetchCourseDetail(courseId: string): Promise<void> {
    currentCourse.value = await studentApi.fetchCourseDetail(courseId)
  }

  // === Tasks ===
  async function fetchTasks(params: {
    page: number
    pageSize: number
    status?: string
    keyword?: string
  }): Promise<void> {
    tasksLoading.value = true
    try {
      const res = await studentApi.fetchTasks(params)
      tasks.value = res.list
      tasksTotal.value = res.total
    } finally {
      tasksLoading.value = false
    }
  }

  async function fetchTaskDetail(taskId: string): Promise<void> {
    currentTask.value = await studentApi.fetchTaskDetail(taskId)
  }

  // === Submission ===
  async function submitTask(
    taskId: string,
    data: ISubmitRequest,
  ): Promise<ISubmitResponse> {
    return studentApi.submitTask(taskId, data)
  }

  async function verifyGit(
    taskId: string,
    data: IGitVerifyRequest,
  ): Promise<IGitVerifyResponse> {
    return studentApi.verifyGit(taskId, data)
  }

  // === AI ===
  async function triggerAIEvaluation(submissionId: string): Promise<void> {
    await studentApi.triggerAIEvaluation(submissionId)
  }

  async function fetchAIResult(submissionId: string): Promise<void> {
    aiResult.value = await studentApi.fetchAIResult(submissionId)
  }

  async function pollAIResult(submissionId: string): Promise<void> {
    stopPolling()
    aiPollingStatus.value = "polling"
    pollingAttempts = 0

    return new Promise((resolve, reject) => {
      pollingTimer = setInterval(async () => {
        pollingAttempts++
        try {
          await fetchAIResult(submissionId)
          if (aiResult.value?.status === "COMPLETED") {
            aiPollingStatus.value = "completed"
            stopPolling()
            resolve()
          } else if (pollingAttempts >= MAX_ATTEMPTS) {
            aiPollingStatus.value = "timeout"
            stopPolling()
            reject(new Error("AI 分析超时，请联系教师"))
          }
        } catch {
          stopPolling()
          reject(new Error("获取 AI 分析结果失败"))
        }
      }, 3000)
    })
  }

  function stopPolling(): void {
    if (pollingTimer) {
      clearInterval(pollingTimer)
      pollingTimer = null
    }
    pollingAttempts = 0
  }

  // === Evaluation ===
  async function fetchEvaluation(submissionId: string): Promise<void> {
    evaluation.value = await studentApi.fetchEvaluation(submissionId)
  }

  // === AI Result Detail ===
  async function fetchAIResultDetail(submissionId: string): Promise<void> {
    aiResultDetail.value = await studentApi.fetchAIResultDetail(submissionId)
  }

  // === Reports & Growth ===
  async function fetchStudentReport(): Promise<void> {
    studentReportLoading.value = true
    try { studentReport.value = await studentApi.fetchStudentReport() }
    finally { studentReportLoading.value = false }
  }

  async function fetchGrowth(): Promise<void> {
    growthLoading.value = true
    try { growthData.value = await studentApi.fetchGrowth() }
    finally { growthLoading.value = false }
  }

  function resetStore(): void {
    stopPolling()
    dashboardData.value = null
    courses.value = []
    coursesTotal.value = 0
    currentCourse.value = null
    tasks.value = []
    tasksTotal.value = 0
    currentTask.value = null
    aiResult.value = null
    aiPollingStatus.value = "idle"
    evaluation.value = null
    aiResultDetail.value = null
    studentReport.value = null
    growthData.value = null
  }

  return {
    dashboardData,
    dashboardLoading,
    courses,
    coursesTotal,
    coursesLoading,
    currentCourse,
    tasks,
    tasksTotal,
    tasksLoading,
    currentTask,
    aiResult,
    aiPollingStatus,
    evaluation,
    aiResultDetail,
    studentReport,
    studentReportLoading,
    growthData,
    growthLoading,
    fetchDashboard,
    fetchCourses,
    fetchCourseDetail,
    fetchTasks,
    fetchTaskDetail,
    submitTask,
    verifyGit,
    triggerAIEvaluation,
    fetchAIResult,
    pollAIResult,
    stopPolling,
    fetchEvaluation,
    fetchAIResultDetail,
    fetchStudentReport,
    fetchGrowth,
    resetStore,
  }
})
