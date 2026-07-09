import request from "@/api/request"
import { downloadFile } from "@/api/download"
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
}): Promise<ITeacherTaskItem[]> {
  return request.get("/api/v1/teacher/tasks", { params }) as Promise<ITeacherTaskItem[]>
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

export function fetchPendingSubmissions(): Promise<IPendingSubmission[]> {
  return request.get("/api/v1/teacher/submissions") as Promise<IPendingSubmission[]>
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
  }) as Promise<void>
}

// ========== Dashboard ==========

export function fetchDashboard(): Promise<IDashboardData> {
  return request.get("/api/v1/teacher/dashboard") as Promise<IDashboardData>
}

// ========== Reports ==========

export function fetchClassReport(params?: {
  className?: string
}): Promise<IClassReport> {
  return request.get("/api/v1/teacher/reports", { params }) as Promise<IClassReport>
}

export function fetchCollegeReport(): Promise<ICollegeReport> {
  return request.get("/api/v1/teacher/reports-college") as Promise<ICollegeReport>
}

export function exportClassReport(className: string | undefined, format: "xlsx" | "pdf"): Promise<void> {
  return downloadFile("/api/v1/teacher/reports/export", { className, format })
}
