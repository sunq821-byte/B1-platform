import request from "@/api/request"
import { downloadFile } from "@/api/download"
import type {
  IDashboardData,
  ICourseItem,
  ICourseDetail,
  ITaskItem,
  ITaskDetail,
  ISubmitRequest,
  ISubmitResponse,
  ISubmissionDraft,
  IGitVerifyRequest,
  IGitVerifyResponse,
  IAIResult,
  IAIDimensionScore,
  IAIIssue,
  IAIResultDetail,
  IEvaluationDetail,
  IStudentReportData,
  IGrowthData,
  INotificationItem,
} from "@/types/student"
import { buildAIDeductionView } from "@/utils/aiDeductions"

export function fetchDashboard(): Promise<IDashboardData> {
  return request.get("/api/v1/student/dashboard") as Promise<IDashboardData>
}

export function fetchCourses(params: {
  page: number
  pageSize: number
  keyword?: string
}): Promise<{ list: ICourseItem[]; page: number; pageSize: number; total: number; totalPages: number }> {
  return request.get("/api/v1/student/courses", { params }) as Promise<{
    list: ICourseItem[]
    page: number
    pageSize: number
    total: number
    totalPages: number
  }>
}

export function fetchCourseDetail(courseId: string): Promise<ICourseDetail> {
  return request.get(`/api/v1/student/courses/${courseId}`) as Promise<ICourseDetail>
}

export function fetchTasks(params: {
  page: number
  pageSize: number
  status?: string
  keyword?: string
}): Promise<{ list: ITaskItem[]; page: number; pageSize: number; total: number; totalPages: number }> {
  return request.get("/api/v1/student/tasks", { params }) as Promise<{
    list: ITaskItem[]
    page: number
    pageSize: number
    total: number
    totalPages: number
  }>
}

export function fetchTaskDetail(taskId: string): Promise<ITaskDetail> {
  return request.get(`/api/v1/student/tasks/${taskId}`) as Promise<ITaskDetail>
}

export function fetchSubmissionDraft(taskId: string): Promise<ISubmissionDraft | null> {
  return request.get(`/api/v1/student/tasks/${taskId}/draft`) as Promise<ISubmissionDraft | null>
}

export function trialSubmit(
  taskId: string,
  data: ISubmitRequest,
): Promise<ISubmitResponse> {
  return request.post(`/api/v1/student/tasks/${taskId}/trials`, data) as Promise<ISubmitResponse>
}

export function confirmSubmit(
  taskId: string,
  trialId?: string,
): Promise<ISubmitResponse> {
  return request.post(`/api/v1/student/tasks/${taskId}/confirm`, trialId ? { trialId } : {}) as Promise<ISubmitResponse>
}

/** @deprecated 使用 trialSubmit */
export function submitTask(
  taskId: string,
  data: ISubmitRequest,
): Promise<ISubmitResponse> {
  return trialSubmit(taskId, data)
}

export function verifyGit(
  taskId: string,
  data: IGitVerifyRequest,
): Promise<IGitVerifyResponse> {
  return request.post(`/api/v1/student/tasks/${taskId}/git-verify`, data) as Promise<IGitVerifyResponse>
}

export function triggerAIEvaluation(
  submissionId: string,
): Promise<{ analyzeId: string; submissionId: string; status: string; estimatedSeconds: number }> {
  return request.post(`/api/v1/student/submissions/${submissionId}/ai-evaluate`) as Promise<{
    analyzeId: string
    submissionId: string
    status: string
    estimatedSeconds: number
  }>
}

export function fetchAIResult(submissionId: string): Promise<IAIResult> {
  return request.get(`/api/v1/student/submissions/${submissionId}/ai-result`) as Promise<IAIResult>
}

export function fetchEvaluation(submissionId: string): Promise<IEvaluationDetail> {
  return request.get(`/api/v1/student/submissions/${submissionId}/evaluation`) as Promise<IEvaluationDetail>
}

function summaryByAgent(dimensions: IAIDimensionScore[], agent: string, fallback: string): string {
  const matched = dimensions.filter((d) => (d.agentType ?? "").toUpperCase() === agent)
  const text = matched.map((d) => d.comment).filter(Boolean).join(" ")
  return text || fallback
}

function summaryByAgentFromIssues(
  issues: IAIIssue[],
  agent: string,
): string {
  const matched = issues.filter((i) => (i.agentType ?? "").toUpperCase() === agent)
  return matched
    .map((i) => i.reason)
    .filter(Boolean)
    .join(" ")
}

export async function fetchAIResultDetail(submissionId: string): Promise<IAIResultDetail> {
  const raw = await fetchAIResult(submissionId)
  const result = raw.result
  const dimensions = result?.dimensions ?? []
  const issues = result?.issues ?? []
  const deductionView = buildAIDeductionView(result)
  const fallback = result?.summary ?? ""
  return {
    submissionId,
    taskId: "",
    taskName: "",
    aiScore: result?.overallScore ?? 0,
    totalDeductions: deductionView.deductions.length,
    totalDeductScore: deductionView.totalDeductScore,
    scoreGap: deductionView.scoreGap,
    modelVersion: "",
    analyzedAt: raw.completedAt ?? raw.startedAt ?? "",
    codeSummary: issues.length
      ? summaryByAgentFromIssues(issues, "CODE")
      : summaryByAgent(dimensions, "CODE", fallback),
    docSummary: issues.length
      ? summaryByAgentFromIssues(issues, "DOC")
      : summaryByAgent(dimensions, "DOC", fallback),
    reqSummary: issues.length
      ? summaryByAgentFromIssues(issues, "REQ")
      : summaryByAgent(dimensions, "REQ", fallback),
    deductions: deductionView.deductions,
    improvementNotes: deductionView.improvementNotes,
  }
}

export function fetchStudentReport(): Promise<IStudentReportData> {
  return request.get("/api/v1/student/student-report") as Promise<IStudentReportData>
}

export function exportStudentReport(format: "xlsx" | "pdf"): Promise<void> {
  return downloadFile("/api/v1/student/student-report/export", { format })
}

export function fetchGrowth(): Promise<IGrowthData> {
  return request.get("/api/v1/student/growth-profile") as Promise<IGrowthData>
}

// ========== Notifications ==========

export function fetchNotifications(params: {
  page: number
  pageSize: number
}): Promise<{ list: INotificationItem[]; page: number; pageSize: number; total: number; totalPages: number }> {
  return request.get("/api/v1/student/notifications", { params }) as Promise<{
    list: INotificationItem[]
    page: number
    pageSize: number
    total: number
    totalPages: number
  }>
}

export function markNotificationRead(id: string): Promise<void> {
  return request.put(`/api/v1/student/notifications/${id}/read`) as Promise<void>
}

export function markAllNotificationsRead(): Promise<void> {
  return request.put("/api/v1/student/notifications/read-all") as Promise<void>
}
