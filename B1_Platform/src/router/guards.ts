import type { Router } from "vue-router"
import { useUserStore } from "@/stores/useUserStore"
import { usePermissionStore } from "@/stores/usePermissionStore"

/** 权限加载状态 */
let isPermissionLoaded = false

export function setupAuthGuard(router: Router) {
  router.beforeEach(async (to, _from, next) => {
    // 公开页面直接放行
    if (to.meta.public) return next()

    const userStore = useUserStore()

    // 未登录 → 跳转登录页
    if (!userStore.isLoggedIn) {
      return next({ path: "/login", query: { redirect: to.fullPath } })
    }

    // 已登录但未加载权限 → 动态添加路由
    if (!isPermissionLoaded) {
      try {
        // Only fetch user info if not already loaded (login response already provides it)
        if (!userStore.userInfo) {
          await userStore.fetchUserInfo()
        }
        const permissionStore = usePermissionStore()
        const role = userStore.userRole
        if (role) {
          permissionStore.generateMenus(role)
        }
        isPermissionLoaded = true
      } catch {
        await userStore.logout()
        return next({ path: "/login", query: { redirect: to.fullPath } })
      }
    }

    next()
  })
}

export function setupPermissionGuard(router: Router) {
  router.beforeEach((to, _from, next) => {
    if (to.meta.public) return next()

    // meta.roles: 允许访问的角色数组
    const allowedRoles = to.meta.roles as string[] | undefined
    if (!allowedRoles || allowedRoles.length === 0) return next()

    const userStore = useUserStore()
    const userRole = userStore.userRole

    if (!userRole || !allowedRoles.includes(userRole)) {
      return next({ path: "/403" })
    }

    next()
  })
}

export function setupRedirectGuard(router: Router) {
  router.beforeEach((to, _from, next) => {
    if (to.path === "/login") {
      const userStore = useUserStore()
      if (userStore.isLoggedIn) {
        return next(userStore.homePath)
      }
    }
    next()
  })
}

/** 重置权限加载状态（登出时调用） */
export function resetPermissionState() {
  isPermissionLoaded = false
}
