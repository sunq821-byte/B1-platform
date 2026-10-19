import type { UserRole } from "@/constants/role"
import type { IUserInfo } from "@/types/user"

/** 后端 UserProfileVO / UserInfoVO 通用字段 */
export interface UserProfilePayload {
  id: number | string
  username: string
  realName: string
  email?: string
  phone?: string
  avatar?: string
  avatarUrl?: string
  roles?: UserRole[]
  status?: number | string
  lastLoginTime?: string
}

/** 统一 avatarUrl → avatar 等前后端字段差异 */
export function normalizeUserInfo(payload: UserProfilePayload): IUserInfo {
  return {
    id: payload.id,
    username: payload.username,
    realName: payload.realName,
    roles: payload.roles ?? [],
    avatar: payload.avatar ?? payload.avatarUrl,
    email: payload.email,
    phone: payload.phone,
    status: payload.status,
    lastLoginTime: payload.lastLoginTime,
  }
}
