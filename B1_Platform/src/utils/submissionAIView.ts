/** 确认递交教师后的提交状态（只读查看 AI 结果，不再触发分析流程） */
export const POST_CONFIRM_SUBMISSION_STATUSES = [
  "CONFIRMED",
  "REVIEWING",
  "TEACHER_SCORING",
  "PUBLISHED",
  "COMPLETED",
  "REVIEWED",
] as const

export function isPostConfirmSubmission(status: string): boolean {
  return (POST_CONFIRM_SUBMISSION_STATUSES as readonly string[]).includes(status)
}

/**
 * 是否应走「实时分析」流程（轮询 + 流式动画）。
 * 确认提交后、已退回、或 AI 结果已产出时，直接静态展示缓存结果。
 */
export function shouldUseLiveAIAnalysis(
  submissionStatus: string,
  aiResultStatus?: string | null,
): boolean {
  if (isPostConfirmSubmission(submissionStatus)) return false
  if (submissionStatus === "REJECTED") return false
  if (aiResultStatus === "COMPLETED") return false
  return true
}
