import type Mock from "mockjs"

export default function setupUserMock(mock: typeof Mock): void {
  const { Random } = mock

  mock.mock("/api/v1/user/profile", "put", (options: { body: string }) => {
    const { realName, email, phone } = JSON.parse(options.body)

    if (!realName || !email || !phone) {
      return {
        code: 4001,
        message: "请填写完整的个人信息",
        data: null,
        success: false,
        timestamp: Date.now(),
        traceId: Random.guid(),
      }
    }

    if (!email.includes("@")) {
      return {
        code: 4001,
        message: "邮箱格式不正确",
        data: null,
        success: false,
        timestamp: Date.now(),
        traceId: Random.guid(),
      }
    }

    return {
      code: 0,
      message: "个人信息更新成功",
      data: { id: 1, realName, email, phone },
      success: true,
      timestamp: Date.now(),
      traceId: Random.guid(),
    }
  })

  mock.mock("/api/v1/user/password", "put", (options: { body: string }) => {
    const { oldPassword } = JSON.parse(options.body)

    if (oldPassword !== "password123") {
      return {
        code: 2009,
        message: "原密码错误",
        data: null,
        success: false,
        timestamp: Date.now(),
        traceId: Random.guid(),
      }
    }

    return {
      code: 0,
      message: "密码修改成功",
      data: null,
      success: true,
      timestamp: Date.now(),
      traceId: Random.guid(),
    }
  })

  mock.mock("/api/v1/user/avatar", "post", () => {
    return {
      code: 0,
      message: "头像上传成功",
      data: {
        avatar: "/mock/avatars/default.jpg",
        fileId: `avatar_${Random.guid()}`,
      },
      success: true,
      timestamp: Date.now(),
      traceId: Random.guid(),
    }
  })
}
