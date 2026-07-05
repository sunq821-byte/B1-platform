// ========== Dashboard ==========
export interface IDashboardStats {
  pendingTasks: number
  completedTasks: number
  averageScore: number
  analyzingCount: number
}

export interface IRecentTask {
  taskId: string
  taskName: string
  courseName: string
  deadline: string
  status: string
  score: number | null
}

export interface IScoreTrend {
  xAxis: string[]
  myScores: number[]
  classAvg: number[]
}

export interface IRadarData {
  categories: string[]
  myScores: number[]
  classAvg: number[]
}

export interface IDashboardData {
  stats: IDashboardStats
  recentTasks: IRecentTask[]
  scoreTrend: IScoreTrend
  radarData: IRadarData
}

// ========== Course ==========
export interface ICourseItem {
  courseId: string
  courseName: string
  courseCode: string
  teacherName: string
  semester: string
  credits: number
  taskCount: number
  description: string
}

export interface ICourseTaskItem {
  taskId: string
  taskName: string
  deadline: string
  totalScore: number
  mySubmissionStatus: string
  myScore: number | null
}

export interface ICourseDetail extends ICourseItem {
  teacherEmail: string
  tasks: ICourseTaskItem[]
}

// ========== Task ==========
export interface ITaskItem {
  taskId: string
  taskName: string
  courseName: string
  teacherName: string
  deadline: string
  totalScore: number
  submissionType: string
  status: string
  mySubmissionStatus: string
  myScore: number | null
  createdAt: string
}

export interface IEvaluationDimension {
  dimensionName: string
  weight: number
  maxScore: number
}

export interface ITaskAttachment {
  fileId: string
  fileName: string
  fileSize: number
  fileType: string
}

export interface ITaskDetail {
  taskId: string
  taskName: string
  courseName: string
  teacherName: string
  teacherEmail: string
  description: string
  requirements: string
  deadline: string
  totalScore: number
  submissionType: string
  submitLimit: number
  evaluationDimensions: IEvaluationDimension[]
  attachments: ITaskAttachment[]
  mySubmissionStatus: string
  mySubmissionId: string | null
  myScore: number | null
  mySubmitCount: number
  maxSubmitCount: number
  createdAt: string
  updatedAt: string
}

// ========== Submission ==========
export interface ISubmitRequest {
  submissionType: "GIT_URL" | "ZIP_UPLOAD"
  gitUrl?: string
  gitBranch?: string
  zipFileId?: string
  remark?: string
}

export interface ISubmitResponse {
  submissionId: string
  taskId: string
  submissionType: string
  status: string
  submitCount: number
  maxSubmitCount: number
  submittedAt: string
}

export interface IGitVerifyRequest {
  gitUrl: string
  gitBranch?: string
  accessToken?: string
}

export interface IGitVerifyResponse {
  valid: boolean
  repoName: string
  defaultBranch: string
  branches: string[]
  latestCommit: {
    commitId: string
    message: string
    author: string
    committedAt: string
  }
}

export interface ISubmissionHistoryItem {
  submissionId: string
  taskId: string
  taskName: string
  submissionType: string
  status: string
  gitUrl?: string
  fileName?: string
  fileSize?: number
  submittedAt: string
  remark?: string
}

// ========== AI / Evaluation ==========
export interface IAIDimensionScore {
  dimensionName: string
  score: number
  maxScore: number
  weight: number
  comment: string
  suggestions: string[]
  codeReferences: string[]
}

export interface IAIScoreResult {
  overallScore: number
  dimensions: IAIDimensionScore[]
  summary: string
  strengths: string[]
  weaknesses: string[]
  improvementPlan: string
}

export interface IAIResult {
  analyzeId: string
  status: "PENDING" | "PROCESSING" | "COMPLETED"
  progress: number
  currentDimension: string | null
  result: IAIScoreResult | null
  startedAt: string
  completedAt?: string
}

export interface ITeacherDimensionScore {
  dimensionName: string
  score: number
  maxScore: number
}

export interface ITeacherEvaluation {
  overallScore: number
  comment: string
  dimensions: ITeacherDimensionScore[]
  scoredBy: string
  scoredAt: string
  publishedAt: string
}

export interface IEvaluationDetail {
  submissionId: string
  taskName: string
  courseName: string
  submittedAt: string
  aiEvaluation: {
    overallScore: number
    summary: string
    completedAt: string
  }
  teacherEvaluation: ITeacherEvaluation | null
  status: string
  finalScore: number | null
  rejectReason?: string
}

// ========== AI Result Detail ==========
export interface IAIDeductionItem {
  agentType: string
  severity: "critical" | "major" | "minor"
  issueType: string
  suggestDeduct: number
  reason: string
  filePath: string
  lineNumber: number
  confidence: number
}

export interface IAIResultDetail {
  submissionId: string
  taskId: string
  taskName: string
  aiScore: number
  totalDeductions: number
  totalDeductScore: number
  modelVersion: string
  analyzedAt: string
  codeSummary: string
  docSummary: string
  reqSummary: string
  deductions: IAIDeductionItem[]
}

// ========== Student Report ==========
export interface IStudentReportRow {
  taskName: string
  courseName: string
  aiScore: number | null
  finalScore: number
  reviewedAt: string
  status: string
}

export interface IStudentReportData {
  stats: {
    completedTasks: number
    averageScore: number | string
    maxScore: number | string
    minScore: number | string
  }
  scoreTrend: {
    categories: string[]
    values: number[]
  }
  radarData: {
    indicators: string[]
    values: number[]
  }
  rows: IStudentReportRow[]
}

// ========== Growth ==========
export interface IGrowthEntry {
  date: string
  category: string
  description: string
  score: number
}

export interface IGrowthData {
  stats: {
    totalEntries: number
    latestScore: number
    firstScore: number
    improvement: number
  }
  entries: IGrowthEntry[]
  evolutionData: {
    categories: string[]
    allDates: string[]
    series: Array<{ name: string; data: number[] }>
  }
}
