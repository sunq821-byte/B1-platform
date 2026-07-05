import request from "@/api/request"

export function updateProfile(data: {
  realName: string
  email: string
  phone: string
}): Promise<{ id: number; realName: string; email: string; phone: string }> {
  return request.put("/api/v1/user/profile", data) as Promise<{
    id: number
    realName: string
    email: string
    phone: string
  }>
}

export function changePassword(data: {
  oldPassword: string
  newPassword: string
}): Promise<void> {
  return request.put("/api/v1/user/password", data) as Promise<void>
}

export function uploadAvatar(
  file: File,
): Promise<{ avatar: string; fileId: string }> {
  const formData = new FormData()
  formData.append("file", file)
  return request.post("/api/v1/user/avatar", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  }) as Promise<{ avatar: string; fileId: string }>
}
