import type { IAIDeduction } from "@/types/teacher"

const MAX_SCORE = 100

/** 教师终审分：以 AI 参考分为基准，仅叠加教师复核与手动调整 */
export function computeTeacherFinalScore(
  aiScore: number,
  deductions: IAIDeduction[],
  manualAdjustments: Array<{ points: number }>,
): number {
  let score = aiScore

  for (const d of deductions) {
    const suggested = d.suggestDeduct ?? 0
    if (d.overrideAction === "reject") {
      // AI 参考分已含该扣分，驳回则补回
      score += suggested
    } else if (d.overrideAction === "adjust" && d.adjustedDeduct !== undefined) {
      // 调整为更少/更多扣分
      score += suggested - d.adjustedDeduct
    }
    // accept 或未操作：AI 参考分已反映，不重复扣减
  }

  for (const md of manualAdjustments) {
    // 负数扣分、正数加分（与输入框直觉一致）
    score += md.points
  }

  return Math.round(Math.max(0, Math.min(MAX_SCORE, score)) * 10) / 10
}

export function scoreLevel(score: number): "excellent" | "good" | "fair" | "poor" {
  if (score >= 85) return "excellent"
  if (score >= 70) return "good"
  if (score >= 60) return "fair"
  return "poor"
}

export function scoreLevelLabel(score: number): string {
  if (score >= 85) return "优秀"
  if (score >= 70) return "良好"
  if (score >= 60) return "及格"
  return "不及格"
}

export function formatManualAdjustment(points: number): string {
  if (points < 0) return String(points)
  if (points > 0) return `+${points}`
  return "0"
}
