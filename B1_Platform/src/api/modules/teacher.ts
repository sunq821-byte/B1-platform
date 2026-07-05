import request from "@/api/request"
import type {
  ICourseItem,
  ICourseFormData,
  IStandardItem,
  IDimensionConfig,
  IDimensionItem,
  ITeacherTaskItem,
  ITaskFormData,
  IStudentItem,
  IStudentDetail,
  IPendingSubmission,
  IAIDiagnosis,
  IPublishRequest,
  IStandardTemplate,
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

// ========== Standards ==========

export function fetchStandards(): Promise<IStandardItem[]> {
  return request.get("/api/v1/teacher/standards") as Promise<IStandardItem[]>
}

export function fetchStandardDimensions(standardId: string): Promise<IDimensionConfig> {
  return request.get(`/api/v1/teacher/standards/${standardId}/dimensions`) as Promise<IDimensionConfig>
}

export function updateStandardDimensions(
  standardId: string,
  dimensions: IDimensionItem[],
): Promise<void> {
  return request.put(
    `/api/v1/teacher/standards/${standardId}/dimensions`,
    { dimensions },
  ) as Promise<void>
}

// ========== Tasks ==========

export function fetchTeacherTasks(params?: {
  courseId?: string
}): Promise<ITeacherTaskItem[]> {
  return request.get("/api/v1/teacher/tasks", { params }) as Promise<ITeacherTaskItem[]>
}

export function createTask(data: ITaskFormData): Promise<ITeacherTaskItem> {
  return request.post("/api/v1/teacher/tasks", data) as Promise<ITeacherTaskItem>
}

export function updateTask(taskId: string, data: ITaskFormData): Promise<void> {
  return request.put(`/api/v1/teacher/tasks/${taskId}`, data) as Promise<void>
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

export function publishReview(submissionId: string, data: IPublishRequest): Promise<void> {
  return request.post(`/api/v1/teacher/submissions/${submissionId}/publish`, data) as Promise<void>
}

// ========== Standards Library ==========

export function fetchStandardTemplates(): Promise<IStandardTemplate[]> {
  return request.get("/api/v1/teacher/standards-library") as Promise<IStandardTemplate[]>
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
