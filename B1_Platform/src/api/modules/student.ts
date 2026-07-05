import request from "@/api/request"
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

export function fetchAIResultDetail(submissionId: string): Promise<IAIResultDetail> {
  return request.get(`/api/v1/student/submissions/${submissionId}/ai-result-detail`) as Promise<IAIResultDetail>
}

export function fetchStudentReport(): Promise<IStudentReportData> {
  return request.get("/api/v1/student/reports") as Promise<IStudentReportData>
}

export function fetchGrowth(): Promise<IGrowthData> {
  return request.get("/api/v1/student/growth") as Promise<IGrowthData>
}
