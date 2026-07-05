import { defineStore } from "pinia"
import { ref, computed } from "vue"
import type { IUserInfo, ILoginRequest } from "@/types/user"
import type { UserRole } from "@/constants/role"
import { ROLE_HOME } from "@/constants/role"
import { login as loginApi, logout as logoutApi, refreshToken as refreshTokenApi, fetchUserInfo as fetchUserInfoApi } from "@/api/auth"
import { resetPermissionState } from "@/router/guards"

export const useUserStore = defineStore("user", () => {
  const token = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)
  const userInfo = ref<IUserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const userRole = computed<UserRole | null>(() => userInfo.value?.role ?? null)
  const userName = computed(() => userInfo.value?.realName ?? "")
  const homePath = computed(() => {
    const r = userRole.value
    return r ? ROLE_HOME[r] : "/login"
  })

  function hasRole(roles: UserRole[]): boolean {
    if (!userRole.value) return false
    return roles.includes(userRole.value)
  }

  async function login(data: ILoginRequest): Promise<void> {
    const res = await loginApi(data)
    token.value = res.token
    refreshToken.value = res.refreshToken
    userInfo.value = res.userInfo
  }

  async function logout(): Promise<void> {
    token.value = null
    refreshToken.value = null
    userInfo.value = null
    logoutApi().catch(() => { /* fire-and-forget */ })
    // Reset permission store and guard state
    const { usePermissionStore } = await import("@/stores/usePermissionStore")
    usePermissionStore().clearPermissions()
    resetPermissionState()
  }

  async function refreshAccessToken(): Promise<void> {
    if (!refreshToken.value) {
      throw new Error("无刷新令牌")
    }
    const res = await refreshTokenApi(refreshToken.value)
    token.value = res.token
    refreshToken.value = res.refreshToken
  }

  async function fetchUserInfo(): Promise<IUserInfo> {
    const info = await fetchUserInfoApi()
    userInfo.value = info
    return info
  }

  async function updateProfile(data: { realName: string; email: string; phone: string }): Promise<void> {
    const { updateProfile: updateProfileApi } = await import("@/api/modules/user")
    await updateProfileApi(data)
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...data }
    }
  }

  return {
    token,
    refreshToken,
    userInfo,
    isLoggedIn,
    userRole,
    userName,
    homePath,
    hasRole,
    login,
    logout,
    refreshAccessToken,
    fetchUserInfo,
    updateProfile,
  }
})

