import { defineStore } from "pinia"
import { ref, computed, watch } from "vue"
import type { IUserInfo, ILoginRequest } from "@/types/user"
import type { UserRole } from "@/constants/role"
import { ROLE_HOME } from "@/constants/role"
import { login as loginApi, logout as logoutApi, refreshToken as refreshTokenApi, fetchUserInfo as fetchUserInfoApi } from "@/api/auth"
import { resetPermissionState } from "@/router/guards"
import { storage } from "@/utils/storage"
import { normalizeUserInfo, type UserProfilePayload } from "@/utils/userInfo"

const STORAGE_KEYS = {
  token: "token",
  refreshToken: "refresh_token",
  userInfo: "user_info",
} as const

export const useUserStore = defineStore("user", () => {
  const token = ref<string | null>(storage.get<string>(STORAGE_KEYS.token))
  const refreshToken = ref<string | null>(storage.get<string>(STORAGE_KEYS.refreshToken))
  const userInfo = ref<IUserInfo | null>(storage.get<IUserInfo>(STORAGE_KEYS.userInfo))

  watch(token, (v) => (v ? storage.set(STORAGE_KEYS.token, v) : storage.remove(STORAGE_KEYS.token)))
  watch(refreshToken, (v) => (v ? storage.set(STORAGE_KEYS.refreshToken, v) : storage.remove(STORAGE_KEYS.refreshToken)))
  watch(userInfo, (v) => (v ? storage.set(STORAGE_KEYS.userInfo, v) : storage.remove(STORAGE_KEYS.userInfo)), { deep: true })

  const isLoggedIn = computed(() => !!token.value)
  const userRole = computed<UserRole | null>(() => {
    const roles = userInfo.value?.roles
    if (roles && roles.length > 0) return roles[0] as UserRole
    return null
  })
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
    userInfo.value = normalizeUserInfo(res.userInfo as UserProfilePayload)
    storage.remove("active_role")
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

