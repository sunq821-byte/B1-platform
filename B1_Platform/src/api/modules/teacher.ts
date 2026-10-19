import request from "@/api/request"
import { downloadFile } from "@/api/download"
import type {
  ICourseItem,
  ICourseFormData,
  ITeacherTaskItem,
  ITaskFormData,
  ITaskDetail,
  IStudentItem,
  IStudentDetail,
  IPendingSubmission,
  IAIDiagnosis,
  IPublishRequest,
  IClassReport,
  ICollegeReport,
  IDashboardData,
  IKnowledgeDocument,
  IKnowledgeVisibilityOption,
  IKnowledgeDocumentForm,
} from "@/types/teacher"

// ========== Courses ==========

export function fetchCourses(): Promise<ICourseItem[]> {
  return request.get("/api/v1/teacher/courses") as Promise<ICourseItem[]>
}

export function createCourse(data: ICourseFormData): Promise<ICourseItem> {
  return request.post("/api/v1/teacher/courses", data) as Promise<ICourseItem>
}

export function updateCourse(courseId: string, data: ICourseFormData): Promise<void> {
  return request.put(`/api/v1/teacher/courses/${courseId}`, data) as Promise<void>
}

export function deleteCourse(courseId: string): Promise<void> {
  return request.delete(`/api/v1/teacher/courses/${courseId}`) as Promise<void>
}

// ========== Tasks ==========

/**
 * Map the teacher task form to the backend DTO.
 * The form uses `dueDate` (date-only); the backend expects `endTime` (LocalDateTime).
 * A date-only value is pinned to end-of-day so the whole day counts as before the deadline.
 */
function toTaskPayload(data: ITaskFormData): Record<string, unknown> {
  const { dueDate, roleText, skillText, ruleText, weight, ...rest } = data
  const payload: Record<string, unknown> = { ...rest }
  payload.totalScore = weight
  if (dueDate) {
    payload.endTime = dueDate.length <= 10 ? `${dueDate}T23:59:59` : dueDate
  }
  const parts: string[] = []
  if (roleText?.trim()) parts.push(`Role：${roleText.trim()}`)
  if (skillText?.trim()) parts.push(`Skill：${skillText.trim()}`)
  if (ruleText?.trim()) parts.push(`Rule：${ruleText.trim()}`)
  payload.gradingRule = parts.join("\n")
  return payload
}

export function fetchTeacherTasks(params?: {
  courseId?: string
  pageSize?: number
}): Promise<ITeacherTaskItem[]> {
  return request.get("/api/v1/teacher/tasks", {
    params: { pageSize: 200, ...params },
  }) as Promise<ITeacherTaskItem[]>
}

export function createTask(data: ITaskFormData): Promise<ITeacherTaskItem> {
  return request.post("/api/v1/teacher/tasks", toTaskPayload(data)) as Promise<ITeacherTaskItem>
}

export function updateTask(taskId: string, data: ITaskFormData): Promise<void> {
  return request.put(`/api/v1/teacher/tasks/${taskId}`, toTaskPayload(data)) as Promise<void>
}

export function deleteTask(taskId: string): Promise<void> {
  return request.delete(`/api/v1/teacher/tasks/${taskId}`) as Promise<void>
}

export function publishTask(taskId: string): Promise<void> {
  return request.post(`/api/v1/teacher/tasks/${taskId}/publish`) as Promise<void>
}

export function fetchTaskDetail(taskId: string): Promise<ITaskDetail> {
  return request.get(`/api/v1/teacher/tasks/${taskId}`) as Promise<ITaskDetail>
}

export function restoreAiAnalyzeCount(taskId: string, studentUserId: string): Promise<void> {
  return request.post(
    `/api/v1/teacher/tasks/${taskId}/students/${studentUserId}/restore-ai-count`,
  ) as Promise<void>
}

// ========== Students ==========

export function fetchStudents(params: {
  className?: string
  keyword?: string
}): Promise<IStudentItem[]> {
  return request.get("/api/v1/teacher/students", { params }) as Promise<IStudentItem[]>
}

export function fetchStudentDetail(userId: string): Promise<IStudentDetail> {
  return request.get(`/api/v1/teacher/students/${userId}`) as Promise<IStudentDetail>
}

// ========== Submissions / Review ==========

export function fetchPendingSubmissions(params?: {
  page?: number
  pageSize?: number
  status?: string
}): Promise<IPendingSubmission[]> {
  return request.get("/api/v1/teacher/submissions", {
    params: {
      page: params?.page ?? 1,
      pageSize: params?.pageSize ?? 200,
      status: params?.status ?? "CONFIRMED",
    },
  }) as Promise<IPendingSubmission[]>
}

export function fetchAIDiagnosis(submissionId: string): Promise<IAIDiagnosis> {
  return request.get(`/api/v1/teacher/submissions/${submissionId}/diagnosis`) as Promise<IAIDiagnosis>
}

export function fetchSubmissionDetail(submissionId: string): Promise<{ attachments: Array<{ fileId: string; fileName: string; fileSize: number; fileType: string; downloadUrl: string }> }> {
  return request.get(`/api/v1/teacher/submissions/${submissionId}`) as Promise<{ attachments: Array<{ fileId: string; fileName: string; fileSize: number; fileType: string; downloadUrl: string }> }>
}

export function publishReview(submissionId: string, data: IPublishRequest): Promise<void> {
  return request.post(`/api/v1/teacher/submissions/${submissionId}/publish`, {
    status: data.status,
    teacherComment: data.comment,
    totalScore: data.status === "PUBLISHED" ? data.finalScore : undefined,
  }) as Promise<void>
}

// ========== Dashboard ==========

export function fetchDashboard(): Promise<IDashboardData> {
  return request.get("/api/v1/teacher/dashboard") as Promise<IDashboardData>
}

export function remindTasks(taskIds: string[]): Promise<{ notifiedStudents: number; taskCount: number }> {
  return request.post("/api/v1/teacher/tasks/remind", { taskIds }) as Promise<{ notifiedStudents: number; taskCount: number }>
}

// ========== Reports ==========

export function fetchClassReport(params?: {
  adminClass?: string
  courseName?: string
}): Promise<IClassReport> {
  return request.get("/api/v1/teacher/reports", { params }) as Promise<IClassReport>
}

export function fetchCollegeReport(): Promise<ICollegeReport> {
  return request.get("/api/v1/teacher/reports-college") as Promise<ICollegeReport>
}

export function exportClassReport(
  adminClass: string | undefined,
  courseName: string | undefined,
  format: "xlsx" | "pdf",
): Promise<void> {
  return downloadFile("/api/v1/teacher/reports/export", { adminClass, courseName, format })
}

export function fetchKnowledgeDocuments(params: {
  courseId?: string
  tag?: string
  keyword?: string
  page?: number
  pageSize?: number
}): Promise<{ list: IKnowledgeDocument[]; total: number }> {
  return request.get("/api/v1/teacher/knowledge/documents", { params }) as Promise<{
    list: IKnowledgeDocument[]
    total: number
  }>
}

export function fetchKnowledgeVisibilityOptions(
  courseId: string,
): Promise<IKnowledgeVisibilityOption[]> {
  return request.get("/api/v1/teacher/knowledge/visibility-options", {
    params: { courseId },
  }) as Promise<IKnowledgeVisibilityOption[]>
}

export function createKnowledgeDocument(
  data: IKnowledgeDocumentForm,
): Promise<IKnowledgeDocument> {
  return request.post("/api/v1/teacher/knowledge/documents", data) as Promise<IKnowledgeDocument>
}

export function updateKnowledgeDocument(
  documentId: string,
  data: IKnowledgeDocumentForm,
): Promise<IKnowledgeDocument> {
  return request.put(
    `/api/v1/teacher/knowledge/documents/${documentId}`,
    data,
  ) as Promise<IKnowledgeDocument>
}

export function deleteKnowledgeDocument(documentId: string): Promise<void> {
  return request.delete(`/api/v1/teacher/knowledge/documents/${documentId}`) as Promise<void>
}
