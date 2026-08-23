// ========== User Management ==========
export interface IAdminUserItem {
  userId: string
  studentId?: string
  name: string
  role: "student" | "teacher" | "admin"
  className?: string
  email?: string
  status: "active" | "inactive"
  createdAt: string
}

export interface IAdminUserFormData {
  name: string
  role: "student" | "teacher" | "admin"
  className?: string
  email?: string
  status: "active" | "inactive"
}

// ========== System Config ==========
export interface ISystemConfig {
  systemName: string
  currentSemester: string
  semesterStart: string
  semesterEnd: string
  maxUploadSize: number
  aiModelVersion: string
  autoAnalyze: boolean
  notificationEnabled: boolean
  maintenanceMode: boolean
}

export interface ISemesterItem {
  id: string
  name: string
  start: string
  end: string
  status: "active" | "archived"
}

// ========== Dashboard ==========
export interface ILogEntry {
  type: "info" | "warning" | "error" | "success"
  message: string
  detail: string
  createdAt: string
  operator?: string
  ip?: string
  result?: string
}

export interface IServiceHealth {
  name: string
  status: string
  color: string
  detail?: string
  latencyMs?: number
}

export interface IAdminDashboardStats {
  totalUsers: number
  studentCount: number
  teacherCount: number
  adminCount: number
  todayActive: number
  activeRate: number
  activeCourses: number
  totalSubmissions: number
  completionRate: number
  aiCallsToday: number
  storageUsedGb: number
  storageQuotaGb: number
  alertCount24h: number
  /** 账号启用率 0-100 */
  accountEnableRate: number
  /** 健康服务数 / 总数 */
  healthyServices: number
  totalServices: number
  /** AI 今日配额使用率 0-100 */
  aiQuotaRate: number
  alertDeltaPct: number
  aiCallsDeltaPct: number
  completionDeltaPct: number
}

export interface IAdminPriorityAlert {
  title: string
  summary: string
  priority: string
  source: string
  actionPath: string
}

export interface IAdminRuntimeSummary {
  healthy: number
  degraded: number
  down: number
  aiQueue: number
}

export interface IAdminActivityTrend {
  xAxis: string[]
  activeUsers: number[]
  aiCalls: number[]
}

export interface IAdminDashboardData {
  stats: IAdminDashboardStats
  runtimeSummary: IAdminRuntimeSummary
  activityTrend: IAdminActivityTrend
  priorityAlert: IAdminPriorityAlert | null
  recentLogs: ILogEntry[]
  health: IServiceHealth[]
}

// ========== Classes ==========
export interface IClassItem {
  id: string
  name: string
  studentCount: number
  teacherName: string
  createdAt: string
}

export interface IClassFormData {
  name: string
  teacherName: string
}

// ========== Logs ==========
export interface ILogItem {
  id: string
  type: "info" | "warning" | "error" | "success"
  message: string
  detail: string
  operator: string
  ip?: string
  result?: string
  createdAt: string
}

// ========== Monitor ==========
export interface IMonitorData {
  services: IServiceHealth[]
  cpuUsage: number
  memoryUsage: number
  diskUsage: number
  uptime: string
}
