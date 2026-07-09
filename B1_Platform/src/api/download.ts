import type { AxiosResponse } from "axios"
import { ElMessage } from "element-plus"
import request from "@/api/request"

function parseFileName(disposition: string | undefined, fallback: string): string {
  if (!disposition) return fallback
  const utf8Match = disposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Match) {
    try {
      return decodeURIComponent(utf8Match[1])
    } catch {
      return fallback
    }
  }
  const plainMatch = disposition.match(/filename="?([^";]+)"?/i)
  return plainMatch ? plainMatch[1] : fallback
}

async function readBlobAsText(blob: Blob): Promise<string> {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result ?? ""))
    reader.onerror = () => resolve("")
    reader.readAsText(blob)
  })
}

/**
 * 以 blob 方式请求后端并触发浏览器下载。
 * 当后端返回 JSON 错误体（responseType=blob 时错误也会被当作 blob）时，
 * 解析出错误信息并抛出，避免下载到内容为错误 JSON 的文件。
 */
export async function downloadFile(
  url: string,
  params?: Record<string, unknown>,
  fallbackName = "download",
): Promise<void> {
  const res = (await request.get(url, {
    params,
    responseType: "blob",
  })) as unknown as AxiosResponse<Blob>

  const blob = res.data

  if (blob.type && blob.type.includes("application/json")) {
    const text = await readBlobAsText(blob)
    let msg = "导出失败"
    try {
      const parsed = JSON.parse(text)
      msg = parsed.message || msg
    } catch { /* keep default */ }
    ElMessage.error(msg)
    throw new Error(msg)
  }

  const fileName = parseFileName(res.headers["content-disposition"], fallbackName)
  const objectUrl = URL.createObjectURL(blob)
  const a = document.createElement("a")
  a.href = objectUrl
  a.download = fileName
  a.click()
  URL.revokeObjectURL(objectUrl)
}
