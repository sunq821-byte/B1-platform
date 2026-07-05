/** 通用状态枚举 */
export const COMMON_STATUS = {
  ACTIVE: "active",
  INACTIVE: "inactive",
  DISABLED: "disabled",
  DELETED: "deleted",
} as const

export type CommonStatus = (typeof COMMON_STATUS)[keyof typeof COMMON_STATUS]

/** 实训任务状态 */
export const TASK_STATUS = {
  DRAFT: "draft",
  PUBLISHED: "published",
  CLOSED: "closed",
} as const

export type TaskStatus = (typeof TASK_STATUS)[keyof typeof TASK_STATUS]

/** 提交状态 */
export const SUBMISSION_STATUS = {
  NOT_SUBMITTED: "not_submitted",
  SUBMITTED: "submitted",
  ANALYZING: "analyzing",
  COMPLETED: "completed",
  REVIEWED: "reviewed",
} as const

export type SubmissionStatus = (typeof SUBMISSION_STATUS)[keyof typeof SUBMISSION_STATUS]

/** AI 分析状态 */
export const AI_STATUS = {
  PENDING: "pending",
  PROCESSING: "processing",
  COMPLETED: "completed",
  FAILED: "failed",
  CANCELLED: "cancelled",
  REVIEWED: "reviewed",
} as const

export type AiStatus = (typeof AI_STATUS)[keyof typeof AI_STATUS]
