import type {
  IAIDeductionItem,
  IAIDimensionScore,
  IAIIssue,
  IAIScoreResult,
} from "@/types/student"

const DEFAULT_MAX_SCORE = 100

function parseDeduct(value: unknown): number {
  const n = Number(value)
  if (!Number.isFinite(n) || n <= 0) return 0
  return Math.round(n * 10) / 10
}

function mapIssueSeverity(sev?: string): IAIDeductionItem["severity"] {
  const u = (sev ?? "").toUpperCase()
  if (u === "CRITICAL") return "critical"
  if (u === "MAJOR") return "major"
  return "minor"
}

function severityFromDeduct(deduct: number): IAIDeductionItem["severity"] {
  if (deduct >= 10) return "critical"
  if (deduct >= 5) return "major"
  return "minor"
}

function mapToDeductionItem(d: {
  agentType?: string
  issueType?: string
  suggestDeduct?: unknown
  comment?: string
  reason?: string
  filePath?: string
  lineNumber?: number
  confidence?: number
  severity?: string
}): IAIDeductionItem {
  const deduct = parseDeduct(d.suggestDeduct)
  return {
    agentType: (d.agentType ?? "CODE").toUpperCase(),
    severity: deduct > 0 ? severityFromDeduct(deduct) : mapIssueSeverity(d.severity),
    issueType: d.issueType || "待分类问题",
    suggestDeduct: deduct,
    reason: d.reason ?? d.comment ?? "",
    filePath: d.filePath ?? "",
    lineNumber: d.lineNumber ?? 0,
    confidence: Number(d.confidence) || 0,
  }
}

function collectRawItems(result: IAIScoreResult | null | undefined): IAIDeductionItem[] {
  const issues = result?.issues ?? []
  if (issues.length > 0) {
    return issues.map((i: IAIIssue) => mapToDeductionItem(i))
  }
  return (result?.dimensions ?? [])
    .filter((d) => !!d.issueType || !!d.comment)
    .map((d) => mapToDeductionItem(d))
}

function dimensionGapItems(dimensions: IAIDimensionScore[]): IAIDeductionItem[] {
  return dimensions
    .filter((d) => d.maxScore > 0 && d.score < d.maxScore)
    .map((d) => {
      const deduct = Math.round((d.maxScore - d.score) * 10) / 10
      return {
        agentType: (d.agentType ?? "CODE").toUpperCase(),
        severity: severityFromDeduct(deduct),
        issueType: d.dimensionName || d.issueType || "维度失分",
        suggestDeduct: deduct,
        reason: d.comment || `${d.dimensionName}维度扣 ${deduct} 分（${d.score}/${d.maxScore}）`,
        filePath: d.filePath ?? "",
        lineNumber: d.lineNumber ?? 0,
        confidence: Number(d.confidence) || 0,
      }
    })
}

function severityWeight(item: IAIDeductionItem): number {
  if (item.severity === "critical") return 3
  if (item.severity === "major") return 2
  return 1
}

/** 改进项参与扣分的权重；明显为正面评价的条目权重为 0 */
function suggestionWeight(item: IAIDeductionItem): number {
  const text = `${item.issueType} ${item.reason}`
  if (/完全|正确|清晰|无问题|完整实现|符合要求|逻辑清晰|可靠/.test(text)) return 0
  const type = item.issueType.toUpperCase()
  if (/DOCUMENT|STYLE|注释|文档|规范|缺失|不足/.test(type + text)) return 3
  return severityWeight(item)
}

function allocateGapToSuggestions(
  suggestions: IAIDeductionItem[],
  remaining: number,
): IAIDeductionItem[] {
  if (remaining <= 0 || suggestions.length === 0) return []

  const weights = suggestions.map(suggestionWeight)
  const totalWeight = weights.reduce((sum, w) => sum + w, 0)
  if (totalWeight <= 0) return []

  let assigned = 0

  return suggestions.map((item, index) => {
    let deduct: number
    if (index === suggestions.length - 1) {
      deduct = Math.round((remaining - assigned) * 10) / 10
    } else {
      deduct = Math.round(((remaining * weights[index]) / totalWeight) * 10) / 10
      assigned += deduct
    }
    return {
      ...item,
      suggestDeduct: Math.max(0, deduct),
      severity: deduct >= 5 ? "major" : item.severity,
    }
  }).filter((d) => d.suggestDeduct > 0)
}

function mergeUniqueScoring(existing: IAIDeductionItem[], extra: IAIDeductionItem[]): IAIDeductionItem[] {
  const merged = [...existing]
  for (const item of extra) {
    const dup = merged.find(
      (m) => m.issueType === item.issueType && m.suggestDeduct === item.suggestDeduct,
    )
    if (!dup) merged.push(item)
  }
  return merged
}

export interface AIDeductionView {
  deductions: IAIDeductionItem[]
  improvementNotes: IAIDeductionItem[]
  totalDeductScore: number
  scoreGap: number
}

export function buildAIDeductionView(
  result: IAIScoreResult | null | undefined,
  maxScore = DEFAULT_MAX_SCORE,
): AIDeductionView {
  const overallScore = result?.overallScore ?? maxScore
  const scoreGap = Math.max(0, Math.round((maxScore - overallScore) * 10) / 10)

  const rawItems = collectRawItems(result)
  let scoring = rawItems.filter((d) => d.suggestDeduct > 0)
  let suggestions = rawItems.filter((d) => d.suggestDeduct <= 0 && (d.reason || d.issueType))

  scoring = mergeUniqueScoring(scoring, dimensionGapItems(result?.dimensions ?? []))

  let listedSum = scoring.reduce((sum, d) => sum + d.suggestDeduct, 0)
  let remaining = Math.round((scoreGap - listedSum) * 10) / 10

  if (remaining > 0 && suggestions.length > 0) {
    const allocated = allocateGapToSuggestions(suggestions, remaining)
    const allocatedKeys = new Set(allocated.map((a) => `${a.issueType}::${a.reason}`))
    scoring = mergeUniqueScoring(scoring, allocated)
    suggestions = suggestions.filter((s) => !allocatedKeys.has(`${s.issueType}::${s.reason}`))
    listedSum = scoring.reduce((sum, d) => sum + d.suggestDeduct, 0)
    remaining = Math.round((scoreGap - listedSum) * 10) / 10
  }

  if (remaining > 0) {
    const reasonParts = [
      ...scoring.map((d) => `• ${d.issueType}（-${d.suggestDeduct}分）：${d.reason}`),
      ...suggestions.map((d) => `• ${d.issueType}：${d.reason}`),
    ]
    scoring.push({
      agentType: "CODE",
      severity: severityFromDeduct(remaining),
      issueType: "综合扣减",
      suggestDeduct: remaining,
      reason: reasonParts.length > 0
        ? reasonParts.join("\n")
        : (result?.summary || "详见 AI 分析摘要"),
      filePath: "",
      lineNumber: 0,
      confidence: 0,
    })
    suggestions = []
  }

  const totalDeductScore = scoring.reduce((sum, d) => sum + d.suggestDeduct, 0)

  return {
    deductions: scoring,
    improvementNotes: suggestions,
    totalDeductScore,
    scoreGap,
  }
}
