// ========== Course ==========

export interface ICourseItem {
  courseId: string
  courseCode: string
  courseName: string
  className: string
  semester: string
  credits: number
}

export interface ICourseFormData {
  courseCode: string
  courseName: string
  className: string
  semester: string
  credits: number
}

// ========== Standard ==========

export interface IDimensionItem {
  name: string
  weight: number
}

export interface IStandardItem {
  standardId: string
  name: string
  courseType: string
  dimensionCount: number
  version: string
  status: "published" | "draft"
  updatedAt: string | null
}

export interface IDimensionConfig {
  standardId: string
  standardName: string
  dimensions: IDimensionItem[]
}

// ========== Task ==========

export interface ITeacherTaskItem {
  taskId: string
  taskName: string
  courseId: string
  description: string
  dueDate: string
  weight: number
  priority: "high" | "medium" | "low"
  status: "published" | "draft"
  maxScore: number
  createdAt: string
}

export interface ITaskFormData {
  taskName: string
  courseId: string
  description: string
  dueDate: string
  weight: number
  priority: "high" | "medium" | "low"
}

// ========== Student ==========

export interface IStudentItem {
  userId: string
  studentId: string
  name: string
  className: string
  email: string
  completedCount: number
  avgScore: number | null
}

export interface IStudentDetail {
  userId: string
  studentId: string
  name: string
  className: string
  email: string
  completedCount: number
  avgScore: number | null
  grades: IGradeRecord[]
}

export interface IGradeRecord {
  taskName: string
  score: number
  reviewedAt: string
}

// ========== Submission / Review ==========

export interface IPendingSubmission {
  submissionId: string
  studentId: string
  studentName: string
  taskId: string
  taskName: string
  submittedAt: string
  status: "submitted" | "reviewing"
  submissionType: "code" | "file"
  fileName?: string
  fileSize?: number
}

export interface IAIDeduction {
  issueType: string
  agentType: "CODE" | "DOC" | "REQ"
  reason: string
  suggestDeduct: number
  adjustedDeduct?: number
  filePath: string
  lineNumber: number
  confidence: number
  overridden: boolean
  overrideAction?: "accept" | "reject" | "adjust"
  overrideReason?: string
}

export interface IAIDiagnosis {
  submissionId: string
  aiScore: number
  totalDeductions: number
  totalDeductScore: number
  deductions: IAIDeduction[]
  aiIssueLines: number[]
  codePreview: string
}

export interface IManualDeduction {
  points: number
  reason: string
}

export interface IPublishRequest {
  finalScore: number
  comment: string
  manualDeductions: IManualDeduction[]
  deductionOverrides: Record<number, { action: string; adjustedDeduct?: number; reason?: string }>
}

// ========== Standards Library ==========

export interface IStandardTemplate {
  id: string
  name: string
  type: string
  dims: number
  version: string
  status: "published" | "draft"
  updatedAt: string
}

// ========== Reports ==========

export interface IClassReportStats {
  totalStudents: number
  totalReviewed: number
  classAverage: number
  passRate: number
}

export interface IClassReportRow {
  studentId: string
  name: string
  className: string
  completedCount: number
  avgScore: number
  maxScore: number
  minScore: number
}

export interface IClassReport {
  stats: IClassReportStats
  histogram: { categories: string[]; values: number[] }
  courseAvgs: { categories: string[]; values: number[] }
  rows: IClassReportRow[]
}

// ========== College Reports ==========

export interface ICollegeReport {
  crossClass: {
    classNames: string[]
    values: number[]
  }
  semesterTrend: {
    semesters: string[]
    series: Array<{ name: string; data: number[] }>
  }
}

// ========== Dashboard ==========

export interface IDashboardStats {
  totalStudents: number
  classCount: number
  pendingCount: number
  reviewedCount: number
  submissionRate: number
}

export interface IDashboardPendingItem {
  submissionId: string
  studentName: string
  taskName: string
  submittedAt: string
  status: string
}

export interface IDashboardData {
  stats: IDashboardStats
  pendingReviews: IDashboardPendingItem[]
  submitRateByClass: { classNames: string[]; values: number[] }
}
