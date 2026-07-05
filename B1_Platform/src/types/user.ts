// Re-export from constants for backward compatibility
export type { UserRole } from "@/constants/role"

/** 用户信息 */
export interface IUserInfo {
  id: number
  username: string
  realName: string
  role: import("@/constants/role").UserRole
  avatar: string
  email: string
  phone: string
}

/** @deprecated 使用 IUserInfo */
export type UserInfo = IUserInfo

/** 登录请求 */
export interface ILoginRequest {
  username: string
  password: string
}

/** 登录响应 */
export interface ILoginResponse {
  token: string
  refreshToken: string
  userInfo: IUserInfo
}

/** Token 刷新响应 */
export interface IRefreshTokenResponse {
  token: string
  refreshToken: string
}
