/** 知识库资料类型标签（全平台通用，与学院/课程无关） */
export const KNOWLEDGE_MATERIAL_TYPE_TAGS = [
  { value: "grading", label: "评分细则", color: "#1677FF", bg: "#E6F4FF" },
  { value: "template", label: "提交模板", color: "#722ED1", bg: "#F9F0FF" },
  { value: "faq", label: "FAQ", color: "#00B96B", bg: "#E6F7EF" },
  { value: "standard", label: "规范文档", color: "#6b21a8", bg: "#f3e8ff" },
  { value: "reference", label: "参考资料", color: "#0958D9", bg: "#E6F4FF" },
  { value: "other", label: "其他", color: "#64748b", bg: "#f1f5f9" },
] as const

export type KnowledgeMaterialTypeTag = (typeof KNOWLEDGE_MATERIAL_TYPE_TAGS)[number]["value"]

export const KNOWLEDGE_DEFAULT_TAG: KnowledgeMaterialTypeTag = "reference"

export const KNOWLEDGE_TAG_FILTER_OPTIONS = [
  { value: "all", label: "全部类型" },
  ...KNOWLEDGE_MATERIAL_TYPE_TAGS.map((t) => ({ value: t.value, label: t.label })),
]

export function buildKnowledgeTagMeta(): Record<string, { label: string; color: string; bg: string }> {
  return Object.fromEntries(
    KNOWLEDGE_MATERIAL_TYPE_TAGS.map((t) => [t.value, { label: t.label, color: t.color, bg: t.bg }]),
  )
}
