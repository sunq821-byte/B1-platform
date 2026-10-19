/** 三类 AI 分析维度是否适用于当前提交 */
export interface AnalysisScopes {
  code: boolean
  doc: boolean
  req: boolean
}

export const ANALYSIS_NA = {
  code: "本任务未涉及代码分析。",
  doc: "本任务未提交文档类材料，不涉及文档分析。",
  req: "本任务未涉及需求符合性分析。",
} as const

/** 后端 submitType → 前端 scope mode */
export function mapSubmitTypeToMode(submitType?: string): string {
  switch ((submitType ?? "").toUpperCase()) {
    case "ONLINE_CODE":
      return "code"
    default:
      return "file"
  }
}

const DOC_EXT = new Set(["md", "markdown", "doc", "docx", "pdf"])
const IMG_EXT = new Set(["png", "jpg", "jpeg", "gif", "webp"])
const CODE_EXT = new Set(["java", "py", "vue", "js", "ts", "tsx", "txt", "cpp", "go", "sql", "zip"])

function extOf(fileName?: string): string {
  if (!fileName) return ""
  return fileName.split(".").pop()?.toLowerCase() ?? ""
}

/**
 * 根据提交方式与文件名推断三类分析是否适用。
 * @param mode file | code | git
 */
export function resolveAnalysisScopes(mode?: string, fileName?: string): AnalysisScopes {
  const m = (mode || "file").toLowerCase()
  const ext = extOf(fileName)

  if (m === "code") {
    return { code: true, doc: false, req: false }
  }

  // 文件上传：按扩展名判断；zip 可能含代码与文档，三类均可能涉及
  if (ext === "zip") {
    return { code: true, doc: true, req: false }
  }
  if (DOC_EXT.has(ext)) {
    return { code: false, doc: true, req: false }
  }
  if (IMG_EXT.has(ext)) {
    return { code: false, doc: false, req: true }
  }
  if (CODE_EXT.has(ext)) {
    return { code: true, doc: false, req: false }
  }
  return { code: true, doc: true, req: false }
}

/** AI 完成后按 issue 的 agentType 微调（有结果则标记为涉及） */
export function mergeScopesWithIssues(
  base: AnalysisScopes,
  issues: Array<{ agentType?: string }>,
): AnalysisScopes {
  if (!issues.length) return base
  const agents = new Set(issues.map((i) => (i.agentType ?? "").toUpperCase()))
  return {
    code: base.code || agents.has("CODE"),
    doc: base.doc || agents.has("DOC"),
    req: base.req || agents.has("REQ"),
  }
}
