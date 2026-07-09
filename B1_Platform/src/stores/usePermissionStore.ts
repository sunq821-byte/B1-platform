import { defineStore } from "pinia"
import { ref, computed } from "vue"
import type { UserRole } from "@/constants/role"
import { useUserStore } from "./useUserStore"

/** 菜单项 */
export interface MenuItem {
  path: string
  title: string
  icon?: string
  children?: MenuItem[]
}

/** 角色菜单配置 */
const ROLE_MENUS: Record<string, MenuItem[]> = {
  student: [
    { path: "/student/dashboard", title: "仪表盘", icon: "LayoutDashboard" },
    { path: "/student/courses", title: "我的课程", icon: "BookOpen" },
    { path: "/student/tasks", title: "实训任务", icon: "ClipboardList" },
    { path: "/student/reports", title: "个人报告", icon: "BarChart3" },
    { path: "/student/growth", title: "成长中心", icon: "TrendingUp" },
    { path: "/student/profile", title: "个人中心", icon: "User" },
  ],
  teacher: [
    { path: "/teacher/dashboard", title: "仪表盘", icon: "LayoutDashboard" },
    { path: "/teacher/courses", title: "课程管理", icon: "BookOpen" },
    { path: "/teacher/standards", title: "评价标准", icon: "ClipboardCheck" },
    { path: "/teacher/standards-library", title: "标准库", icon: "Library" },
    { path: "/teacher/knowledge-base", title: "知识库", icon: "BookMarked" },
    { path: "/teacher/tasks", title: "实训任务管理", icon: "ClipboardList" },
    { path: "/teacher/students", title: "学生管理", icon: "Users" },
    { path: "/teacher/submissions", title: "审核工作台", icon: "FileText" },
    { path: "/teacher/reports", title: "班级报告", icon: "BarChart3" },
    { path: "/teacher/reports-college", title: "学院报告", icon: "TrendingUp" },
  ],
  admin: [
    { path: "/admin/dashboard", title: "仪表盘", icon: "LayoutDashboard" },
    { path: "/admin/users", title: "用户管理", icon: "Users" },
    { path: "/admin/classes", title: "班级管理", icon: "School" },
    { path: "/admin/system", title: "系统配置", icon: "Settings" },
    { path: "/admin/logs", title: "操作日志", icon: "ScrollText" },
    { path: "/admin/monitor", title: "系统监控", icon: "Activity" },
  ],
}

export const usePermissionStore = defineStore("permission", () => {
  const menus = ref<MenuItem[]>([])
  const permissions = ref<string[]>([])

  const userStore = useUserStore()

  const currentRole = computed(() => userStore.userRole)

  /** 生成当前角色菜单 */
  function generateMenus(role: UserRole) {
    menus.value = ROLE_MENUS[role] ?? []
  }

  /** 设置权限点列表 */
  function setPermissions(perms: string[]) {
    permissions.value = perms
  }

  /** 检查是否拥有指定权限点 */
  function hasPermission(permission: string): boolean {
    if (permissions.value.length > 0) {
      return permissions.value.includes(permission)
    }
    const role = currentRole.value
    if (!role) return false
    const rolePrefix = `${role}:`
    return permission.startsWith(rolePrefix)
  }

  /** 检查是否拥有指定角色 */
  function hasRole(roles: UserRole[]): boolean {
    return userStore.hasRole(roles)
  }

  /** 清除权限（登出时调用） */
  function clearPermissions() {
    menus.value = []
    permissions.value = []
  }

  return {
    menus,
    permissions,
    currentRole,
    generateMenus,
    setPermissions,
    hasPermission,
    hasRole,
    clearPermissions,
  }
})
