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
}

export interface IServiceHealth {
  name: string
  status: string
  color: string
}

export interface IAdminDashboardData {
  stats: {
    totalUsers: number
    activeCourses: number
    totalSubmissions: number
    completionRate: number
  }
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
