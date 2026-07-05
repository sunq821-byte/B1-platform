/** API 基础路径 */
export const API_PREFIX = "/api/v1"

/** 认证相关接口 */
export const AUTH_API = {
  LOGIN: `${API_PREFIX}/auth/login`,
  LOGOUT: `${API_PREFIX}/auth/logout`,
  REFRESH: `${API_PREFIX}/auth/refresh`,
  CURRENT_USER: `${API_PREFIX}/auth/me`,
} as const

/** 学生端接口 */
export const STUDENT_API = {
  DASHBOARD: `${API_PREFIX}/student/dashboard`,
  COURSES: `${API_PREFIX}/student/courses`,
  TASKS: `${API_PREFIX}/student/tasks`,
  SUBMISSIONS: `${API_PREFIX}/student/submissions`,
  GRADES: `${API_PREFIX}/student/grades`,
  GROWTH: `${API_PREFIX}/student/growth`,
} as const

/** 教师端接口 */
export const TEACHER_API = {
  DASHBOARD: `${API_PREFIX}/teacher/dashboard`,
  COURSES: `${API_PREFIX}/teacher/courses`,
  TASKS: `${API_PREFIX}/teacher/tasks`,
  STUDENTS: `${API_PREFIX}/teacher/students`,
  SUBMISSIONS: `${API_PREFIX}/teacher/submissions`,
  REVIEW: `${API_PREFIX}/teacher/review`,
  GRADES: `${API_PREFIX}/teacher/grades`,
  REPORTS: `${API_PREFIX}/teacher/reports`,
} as const

/** 管理员接口 */
export const ADMIN_API = {
  DASHBOARD: `${API_PREFIX}/admin/dashboard`,
  USERS: `${API_PREFIX}/admin/users`,
  CLASSES: `${API_PREFIX}/admin/classes`,
  SYSTEM: `${API_PREFIX}/admin/system`,
  LOGS: `${API_PREFIX}/admin/logs`,
  MONITOR: `${API_PREFIX}/admin/monitor`,
} as const
