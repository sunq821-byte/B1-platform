import request from "./request"
import type { ILoginRequest, ILoginResponse, IRefreshTokenResponse, IUserInfo } from "@/types/user"
import { normalizeUserInfo, type UserProfilePayload } from "@/utils/userInfo"

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
export async function fetchUserInfo(): Promise<IUserInfo> {
  const data = (await request.get("/api/v1/user/profile")) as unknown as UserProfilePayload
  return normalizeUserInfo(data)
}
