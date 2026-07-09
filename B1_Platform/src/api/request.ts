import axios, {
  type AxiosInstance,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from "axios"
import { ElMessage } from "element-plus"
import type { ApiResponse } from "@/types/common"

const instance: AxiosInstance = axios.create({
  baseURL: (import.meta.env.VITE_API_BASE_URL as string) || undefined,
  timeout: 30000,
  headers: { "Content-Type": "application/json" },
})

let isRefreshing = false
let refreshSubscribers: ((token: string) => void)[] = []

function onRefreshed(token: string) {
  refreshSubscribers.forEach((cb) => cb(token))
  refreshSubscribers = []
}

// 请求拦截器：从 Pinia store 注入 Token
instance.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    const { useUserStore } = await import("@/stores/useUserStore")
    const userStore = useUserStore()
    const t = userStore.token
    if (t && config.headers) {
      config.headers.Authorization = `Bearer ${t}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// 响应拦截器：统一处理
instance.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { code, data, message } = response.data

    if (code === 0) {
      return data as unknown as AxiosResponse
    }

    // 2002: Token 过期 → 尝试 refresh
    if (code === 2002) {
      return handleTokenRefresh(response.config) as Promise<AxiosResponse>
    }

    // 2001: 未登录 / 2008: 刷新Token过期 → 清除状态跳转
    if (code === 2001 || code === 2008) {
      handleLogoutAndRedirect()
      return Promise.reject(new Error(message || "登录已过期，请重新登录"))
    }

    // 403
    if (code === 3001) {
      ElMessage.error(message || "无权限访问该资源")
      return Promise.reject(new Error(message || "无权限访问该资源"))
    }

    // 500
    if (code === 5001) {
      ElMessage.error(message || "服务器异常，请稍后重试")
      return Promise.reject(new Error(message || "服务器异常，请稍后重试"))
    }

    // 其他业务错误
    ElMessage.error(message || "请求失败")
    return Promise.reject(new Error(message || "请求失败"))
  },
  async (error) => {
    if (!error.response) {
      ElMessage.error("网络连接异常，请检查网络")
      return Promise.reject(new Error("网络连接异常，请检查网络"))
    }

    const status = error.response.status

    if (status === 401) {
      return handleTokenRefresh(error.config) as Promise<AxiosResponse>
    }

    const messages: Record<number, string> = {
      400: "请求参数有误",
      403: "无权限访问该资源",
      404: "请求的资源不存在",
      500: "服务器内部错误",
      502: "网关错误",
      503: "服务暂时不可用",
    }

    ElMessage.error(messages[status] || `请求失败 (${status})`)
    return Promise.reject(
      new Error(messages[status] || `请求失败 (${status})`),
    )
  },
)

async function handleLogoutAndRedirect() {
  const { useUserStore } = await import("@/stores/useUserStore")
  const userStore = useUserStore()
  await userStore.logout()
  const { default: router } = await import("@/router")
  router.push("/login")
}

async function handleTokenRefresh(config: InternalAxiosRequestConfig): Promise<AxiosResponse> {
  const { useUserStore } = await import("@/stores/useUserStore")
  const userStore = useUserStore()

  if (!isRefreshing) {
    isRefreshing = true
    try {
      await userStore.refreshAccessToken()
      const newToken = userStore.token
      if (newToken && config.headers) {
        config.headers.Authorization = `Bearer ${newToken}`
      }
      onRefreshed(newToken ?? "")
      return instance(config)
    } catch {
      await userStore.logout()
      const { default: router } = await import("@/router")
      router.push("/login")
      return Promise.reject(new Error("登录已过期，请重新登录"))
    } finally {
      isRefreshing = false
    }
  } else {
    return new Promise<AxiosResponse>((resolve) => {
      refreshSubscribers.push((token: string) => {
        if (config.headers) {
          config.headers.Authorization = `Bearer ${token}`
        }
        resolve(instance(config))
      })
    })
  }
}

export default instance
