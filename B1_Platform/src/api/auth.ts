import request from "./request"
import type { ILoginRequest, ILoginResponse, IRefreshTokenResponse, IUserInfo } from "@/types/user"

/** 用户登录 */
export function login(data: ILoginRequest): Promise<ILoginResponse> {
  return request.post("/api/v1/auth/login", data)
}

/** 刷新 Token */
export function refreshToken(refreshToken: string): Promise<IRefreshTokenResponse> {
  return request.post("/api/v1/auth/refresh", { refreshToken })
}

/** 退出登录 */
export function logout(): Promise<void> {
  return request.post("/api/v1/auth/logout")
}

/** 获取当前用户信息 */
export function fetchUserInfo(): Promise<IUserInfo> {
  return request.get("/api/v1/auth/me")
}
