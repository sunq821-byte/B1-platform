const SUBMIT_TYPE_LABEL: Record<string, string> = {
  ONLINE_CODE: "在线代码",
  ZIP_UPLOAD: "文件附件",
  ZIP_ONLY: "文件附件",
  FILE_CODE: "文件或代码",
}

export type SubmitTypeTagVariant = "code" | "file"

export function submitTypeLabel(submitType?: string): string {
  const key = (submitType ?? "").toUpperCase()
  if (key.includes("GIT")) return "文件附件"
  return SUBMIT_TYPE_LABEL[key] ?? "文件提交"
}

export function submitTypeTagVariant(submitType?: string): SubmitTypeTagVariant {
  const key = (submitType ?? "").toUpperCase()
  if (key === "ONLINE_CODE") return "code"
  return "file"
}

export function isOnlineCodeSubmit(submitType?: string): boolean {
  return (submitType ?? "").toUpperCase() === "ONLINE_CODE"
}

export function isGitSubmit(_submitType?: string): boolean {
  return false
}
