import type { Router } from "vue-router"
import { ElMessage } from "element-plus"

export interface StudentTaskActionInput {
  taskId: string
  status: string
  submissionId?: string | null
}

export function navigateStudentTaskAction(router: Router, input: StudentTaskActionInput): void {
  const { taskId, status, submissionId } = input
  const sid = submissionId?.trim()

  if (status === "NOT_SUBMITTED" || status === "REJECTED" || status === "DRAFT") {
    router.push(`/student/submit/${taskId}`)
    return
  }

  if (status === "AI_READY" || status === "AI_COMPLETED" || status === "ANALYZING" || status === "AI_EVALUATING") {
    if (sid) router.push(`/student/grades/${sid}`)
    else router.push(`/student/submit/${taskId}`)
    return
  }

  if (status === "PUBLISHED" || status === "COMPLETED") {
    if (sid) router.push(`/student/grades/${sid}`)
    else {
      ElMessage.warning("暂无成绩记录")
      router.push(`/student/tasks/${taskId}`)
    }
    return
  }

  if (status === "CONFIRMED" || status === "REVIEWING" || status === "TEACHER_SCORING") {
    router.push(`/student/tasks/${taskId}`)
    return
  }

  router.push(`/student/tasks/${taskId}`)
}
