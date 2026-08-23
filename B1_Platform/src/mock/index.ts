/**
 * Mock 基础设施（默认关闭，见 .env.development 中 VITE_USE_MOCK）
 * 需要本地 Mock 时设为 true，并在 modules/ 下补充接口实现。
 */
import Mock from "mockjs"
import { APP_CONFIG } from "@/config/app"

export async function setupMock(): Promise<void> {
  if (!APP_CONFIG.useMock) return

  Mock.setup({ timeout: "200-400" })
  console.warn("[Mock] VITE_USE_MOCK=true 但未配置 Mock 数据，请求将走真实后端")
}

export default Mock
