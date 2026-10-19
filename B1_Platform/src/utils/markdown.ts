import { marked } from "marked"

marked.setOptions({
  gfm: true,
  breaks: true,
})

export function renderMarkdownHtml(source: string): string {
  if (!source.trim()) return "<p>（文档为空）</p>"
  return marked.parse(source, { async: false }) as string
}
