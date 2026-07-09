// ========== Course ==========

export interface ICourseItem {
  courseId: string
  courseCode: string
  courseName: string
  className: string  // derived: same as courseName when backend doesn't provide it
  semester: string
  credits: number
  studentCount: number
  taskCount: number
  status: string
}

export interface ICourseFormData {
  courseCode: string
  courseName: string
  className: string
  semester: string
  credits: number
}

// ========== Task ==========

export interface ITeacherTaskItem {
  taskId: string
  taskName: string
  courseName: string
  description: string
  deadline: string
  totalScore: number
  priority: "high" | "medium" | "low"
  status: string           // "PUBLISHED" | "DRAFT"
  maxScore: number
  createdAt: string
  submissionType: string
  submissionCount: number
  reviewedCount: number
  gradingRule?: string
}

export interface ITaskFormData {
  taskName: string
  courseId: string
  description: string
  dueDate: string
  weight: number
  priority: "high" | "medium" | "low"
  roleText: string
  skillText: string
  ruleText: string
}

// ========== Student ==========

export interface IStudentItem {
  userId: string
  studentId: string        // derived from userId
  name: string             // derived from realName
  realName: string
  className: string
  email: string
  phone: string
  completedCount: number   // derived from submissionCount
  submissionCount: number
  avgScore: number | null
}

export interface IStudentDetail {
  userId: string
  studentId: string
  name: string
  realName: string
  className: string
  email: string
  phone: string
  completedCount: number
  submissionCount: number
  avgScore: number | null
  grades: IGradeRecord[]
}

export interface IGradeRecord {
  taskName: string
  score: number
  reviewedAt: string
}

// ========== Submission / Review ==========

export interface IAttachment {
  fileId: string
  fileName: string
  fileSize: number
  fileType: string
  downloadUrl: string
}

export interface IPendingSubmission {
  submissionId: string
  studentName: string
  studentUserId: string
  studentEmail: string
  taskId: string
  taskName: string
  submittedAt: string
  status: string
  submissionType: "code" | "file"  // derived from submitType
  submitType: string
  submitCount: number
  isLate: number
  hasReview: boolean
  attachments: IAttachment[]
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
  status: "PUBLISHED" | "REJECTED"
  finalScore: number
  comment: string
  manualDeductions: IManualDeduction[]
  deductionOverrides: Record<number, { action: string; adjustedDeduct?: number; reason?: string }>
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
