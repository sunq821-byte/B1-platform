/** 权限点命名规范: {模块}:{操作} */

/** 学生权限 */
export const STUDENT_PERMISSIONS = {
  DASHBOARD_VIEW: "student:dashboard:view",
  COURSE_VIEW: "student:course:view",
  TASK_VIEW: "student:task:view",
  SUBMISSION_CREATE: "student:submission:create",
  SUBMISSION_VIEW: "student:submission:view",
  GRADE_VIEW: "student:grade:view",
  GROWTH_VIEW: "student:growth:view",
} as const

/** 教师权限 */
export const TEACHER_PERMISSIONS = {
  DASHBOARD_VIEW: "teacher:dashboard:view",
  COURSE_MANAGE: "teacher:course:manage",
  TASK_MANAGE: "teacher:task:manage",
  STUDENT_VIEW: "teacher:student:view",
  SUBMISSION_VIEW: "teacher:submission:view",
  REVIEW_CREATE: "teacher:review:create",
  GRADE_MANAGE: "teacher:grade:manage",
  REPORT_VIEW: "teacher:report:view",
} as const

/** 管理员权限 */
export const ADMIN_PERMISSIONS = {
  DASHBOARD_VIEW: "admin:dashboard:view",
  USER_MANAGE: "admin:user:manage",
  CLASS_MANAGE: "admin:class:manage",
  SYSTEM_CONFIG: "admin:system:config",
  LOG_VIEW: "admin:log:view",
  MONITOR_VIEW: "admin:monitor:view",
} as const

/** 路由权限点映射 */
export const ROUTE_PERMISSIONS: Record<string, string> = {
  "/student/dashboard": STUDENT_PERMISSIONS.DASHBOARD_VIEW,
  "/teacher/dashboard": TEACHER_PERMISSIONS.DASHBOARD_VIEW,
  "/admin/dashboard": ADMIN_PERMISSIONS.DASHBOARD_VIEW,
}
