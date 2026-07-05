/** 用户角色枚举 */
export const USER_ROLES = {
  STUDENT: "student",
  TEACHER: "teacher",
  ADMIN: "admin",
} as const

export type UserRole = (typeof USER_ROLES)[keyof typeof USER_ROLES]

/** 角色显示名称 */
export const ROLE_LABELS: Record<UserRole, string> = {
  student: "学生",
  teacher: "教师",
  admin: "管理员",
}

/** 角色默认首页路由 */
export const ROLE_HOME: Record<UserRole, string> = {
  student: "/student/dashboard",
  teacher: "/teacher/dashboard",
  admin: "/admin/dashboard",
}
