/**
 * Mock 数据基础设施
 */
import Mock from "mockjs"
import { APP_CONFIG } from "@/config/app"
import setupAuthMock from "./modules/auth"
import setupStudentMock from "./modules/student"
import setupUserMock from "./modules/user"
import setupFileMock from "./modules/file"
import setupTeacherMock from "./modules/teacher"
import setupAdminMock from "./modules/admin"

Mock.setup({ timeout: "200-400" })

export async function setupMock(): Promise<void> {
  if (!APP_CONFIG.useMock) return

  setupAuthMock(Mock)
  setupStudentMock(Mock)
  setupUserMock(Mock)
  setupFileMock(Mock)
  setupTeacherMock(Mock)
  setupAdminMock(Mock)

  console.log("[Mock] Mock data initialized (auth + student + teacher + user + file + admin)")
}

export default Mock
