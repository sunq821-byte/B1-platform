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
  IGitVerifyRequest,
  IGitVerifyResponse,
  IAIResult,
  IAIDimensionScore,
  IAIResultDetail,
  IAIDeductionItem,
  IEvaluationDetail,
  IStudentReportData,
  IGrowthData,
} from "@/types/student"

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

export function submitTask(
  taskId: string,
  data: ISubmitRequest,
): Promise<ISubmitResponse> {
  return request.post(`/api/v1/student/tasks/${taskId}/submissions`, data) as Promise<ISubmitResponse>
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

function severityFromDeduct(deduct: number): IAIDeductionItem["severity"] {
  if (deduct >= 10) return "critical"
  if (deduct >= 5) return "major"
  return "minor"
}

function summaryByAgent(dimensions: IAIDimensionScore[], agent: string, fallback: string): string {
  const matched = dimensions.filter((d) => (d.agentType ?? "").toUpperCase() === agent)
  const text = matched.map((d) => d.comment).filter(Boolean).join(" ")
  return text || fallback
}

function toDeductions(dimensions: IAIDimensionScore[]): IAIDeductionItem[] {
  return dimensions
    .filter((d) => !!d.issueType)
    .map((d) => {
      const deduct = d.suggestDeduct ?? 0
      return {
        agentType: d.agentType ?? "CODE",
        severity: severityFromDeduct(deduct),
        issueType: d.issueType ?? "",
        suggestDeduct: deduct,
        reason: d.comment ?? "",
        filePath: d.filePath ?? "",
        lineNumber: d.lineNumber ?? 0,
        confidence: d.confidence ?? 0,
      }
    })
}

export async function fetchAIResultDetail(submissionId: string): Promise<IAIResultDetail> {
  const raw = await fetchAIResult(submissionId)
  const result = raw.result
  const dimensions = result?.dimensions ?? []
  const deductions = toDeductions(dimensions)
  const totalDeductScore = deductions.reduce((sum, d) => sum + d.suggestDeduct, 0)
  const fallback = result?.summary ?? ""
  return {
    submissionId,
    taskId: "",
    taskName: "",
    aiScore: result?.overallScore ?? 0,
    totalDeductions: deductions.length,
    totalDeductScore,
    modelVersion: "",
    analyzedAt: raw.completedAt ?? raw.startedAt ?? "",
    codeSummary: summaryByAgent(dimensions, "CODE", fallback),
    docSummary: summaryByAgent(dimensions, "DOC", fallback),
    reqSummary: summaryByAgent(dimensions, "REQ", fallback),
    deductions,
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
