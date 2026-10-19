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
    { path: "/student/dashboard", title: "工作台", icon: "LayoutDashboard" },
    { path: "/student/tasks", title: "实训任务", icon: "ClipboardList" },
    { path: "/student/analytics", title: "学情分析", icon: "BarChart3" },
  ],
  teacher: [
    { path: "/teacher/dashboard", title: "工作台", icon: "LayoutDashboard" },
    { path: "/teacher/teaching", title: "课程与任务", icon: "BookOpen" },
    { path: "/teacher/knowledge-base", title: "知识库", icon: "BookMarked" },
    { path: "/teacher/submissions", title: "审核工作台", icon: "FileText" },
    { path: "/teacher/reports", title: "数据报表", icon: "BarChart3" },
  ],
  admin: [
    { path: "/admin/dashboard", title: "系统总览", icon: "LayoutDashboard" },
    { path: "/admin/organization", title: "组织管理", icon: "Users" },
    { path: "/admin/operations", title: "运维中心", icon: "Activity" },
    { path: "/admin/system", title: "系统配置", icon: "Settings" },
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
