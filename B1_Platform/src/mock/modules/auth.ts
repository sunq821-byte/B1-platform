import type Mock from "mockjs"

/** 认证接口 Mock 数据 */
export default function setupAuthMock(mock: typeof Mock): void {
  const { Random } = mock

  const mockUsers = [
    { id: 1, username: "student01", password: "password123", realName: "张三", role: "student", avatar: "", email: "zhangsan@example.com", phone: "13800138001", status: "active" },
    { id: 2, username: "teacher01", password: "password123", realName: "李四", role: "teacher", avatar: "", email: "lisi@example.com", phone: "13800138002", status: "active" },
    { id: 3, username: "admin01", password: "password123", realName: "王五", role: "admin", avatar: "", email: "wangwu@example.com", phone: "13800138003", status: "active" },
    { id: 4, username: "locked01", password: "locked123", realName: "锁定用户", role: "student", avatar: "", email: "locked@example.com", phone: "13800138004", status: "locked" },
    { id: 5, username: "disabled01", password: "disabled123", realName: "禁用用户", role: "student", avatar: "", email: "disabled@example.com", phone: "13800138005", status: "disabled" },
  ]

  const currentTokens: Map<string, { token: string; refreshToken: string; user: typeof mockUsers[0] }> = new Map()

  // 登录接口 — 500ms 延迟
  mock.mock("/api/v1/auth/login", "post", (options: { body: string }) => {
    const { username, password } = JSON.parse(options.body)

    const user = mockUsers.find(u => u.username === username)

    if (!user || user.password !== password) {
      return {
        code: 2004,
        message: "用户名或密码错误",
        data: null,
        success: false,
        timestamp: Date.now(),
        traceId: Random.guid(),
      }
    }

    if (user.status === "locked") {
      return {
        code: 2005,
        message: "账号已被锁定，请联系管理员",
        data: null,
        success: false,
        timestamp: Date.now(),
        traceId: Random.guid(),
      }
    }

    if (user.status === "disabled") {
      return {
        code: 2006,
        message: "账号已被禁用，请联系管理员",
        data: null,
        success: false,
        timestamp: Date.now(),
        traceId: Random.guid(),
      }
    }

    const token = `mock-token-${Random.guid()}`
    const refreshTokenStr = `mock-refresh-${Random.guid()}`

    currentTokens.set(token, {
      token,
      refreshToken: refreshTokenStr,
      user,
    })

    return {
      code: 0,
      message: "登录成功",
      data: {
        token,
        refreshToken: refreshTokenStr,
        userInfo: {
          id: user.id,
          username: user.username,
          realName: user.realName,
          role: user.role,
          avatar: user.avatar,
          email: user.email,
          phone: user.phone,
        },
      },
      success: true,
      timestamp: Date.now(),
      traceId: Random.guid(),
    }
  })

  // 刷新 Token
  mock.mock("/api/v1/auth/refresh", "post", (options: { body: string }) => {
    const { refreshToken: rt } = JSON.parse(options.body)

    let found: { token: string; refreshToken: string; user: typeof mockUsers[0] } | undefined
    for (const entry of currentTokens.values()) {
      if (entry.refreshToken === rt) {
        found = entry
        break
      }
    }

    if (!found) {
      return {
        code: 2008,
        message: "刷新Token已过期，请重新登录",
        data: null,
        success: false,
        timestamp: Date.now(),
        traceId: Random.guid(),
      }
    }

    const newToken = `mock-token-${Random.guid()}`
    const newRefreshToken = `mock-refresh-${Random.guid()}`

    currentTokens.delete(found.token)
    currentTokens.set(newToken, {
      token: newToken,
      refreshToken: newRefreshToken,
      user: found.user,
    })

    return {
      code: 0,
      message: "Token刷新成功",
      data: {
        token: newToken,
        refreshToken: newRefreshToken,
      },
      success: true,
      timestamp: Date.now(),
      traceId: Random.guid(),
    }
  })

  // 退出登录
  mock.mock("/api/v1/auth/logout", "post", () => {
    return {
      code: 0,
      message: "退出成功",
      data: null,
      success: true,
      timestamp: Date.now(),
      traceId: Random.guid(),
    }
  })

  // 获取当前用户信息（Mock模式下返回最近登录的用户，因为Mock.js不支持读取headers）
  mock.mock("/api/v1/auth/me", "get", () => {
    // 取最后登录的用户
    const entries = Array.from(currentTokens.values())
    const entry = entries[entries.length - 1]
    if (!entry) {
      return {
        code: 2001,
        message: "未登录",
        data: null,
        success: false,
        timestamp: Date.now(),
        traceId: Random.guid(),
      }
    }

    return {
      code: 0,
      message: "获取成功",
      data: {
        id: entry.user.id,
        username: entry.user.username,
        realName: entry.user.realName,
        role: entry.user.role,
        avatar: entry.user.avatar,
        email: entry.user.email,
        phone: entry.user.phone,
      },
      success: true,
      timestamp: Date.now(),
      traceId: Random.guid(),
    }
  })
}

