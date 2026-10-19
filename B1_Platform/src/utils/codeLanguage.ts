/** 从代码内容粗略推断语言（教师端只读预览用） */
export function inferCodeLanguage(code: string): string {
  const sample = code.slice(0, 2000)
  if (/^\s*#include\s/m.test(sample) || /\bstd::\b/.test(sample)) return "cpp"
  if (/^\s*def\s+\w+/m.test(sample) || /^\s*import\s+\w+/m.test(sample)) return "python"
  if (/^\s*package\s+\w+/m.test(sample) || /public\s+class\s/.test(sample)) return "java"
  if (/^\s*func\s+\w+/m.test(sample) || /package\s+main/.test(sample)) return "go"
  if (/function\s+\w+|const\s+\w+\s*=|console\.log/.test(sample)) return "javascript"
  return "java"
}
