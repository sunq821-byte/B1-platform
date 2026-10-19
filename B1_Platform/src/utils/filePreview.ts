import type { AxiosResponse } from "axios"
import request from "@/api/request"
import { renderMarkdownHtml } from "@/utils/markdown"

export type PreviewKind = "text" | "markdown" | "pdf" | "image" | "word" | "archive" | "unknown"

export interface LocalPreviewResult {
  kind: PreviewKind
  blobUrl?: string
  textContent?: string
  markdownHtml?: string
  wordHtml?: string
  error?: string
}

export function detectPreviewKind(fileName: string, fileType?: string): PreviewKind {
  const type = (fileType ?? "").toLowerCase()
  const ext = (fileName ?? "").split(".").pop()?.toLowerCase() ?? ""

  if (type.includes("markdown") || type === "text/markdown") return "markdown"
  if (type.startsWith("image/")) return "image"
  if (type === "application/pdf") return "pdf"
  if (type.includes("word") || type.includes("document")) return "word"
  if (type.startsWith("text/") && !type.includes("html")) return "text"
  if (type === "application/zip" || type.includes("zip")) return "archive"

  if (["md", "markdown"].includes(ext)) return "markdown"
  if (["png", "jpg", "jpeg", "gif", "webp", "svg"].includes(ext)) return "image"
  if (ext === "pdf") return "pdf"
  if (["doc", "docx"].includes(ext)) return "word"
  if (["txt", "java", "js", "ts", "tsx", "vue", "sql", "json", "xml", "yml", "yaml"].includes(ext)) return "text"
  if (["zip", "rar", "7z", "tar", "gz"].includes(ext)) return "archive"
  return "unknown"
}

export function resolvePreviewUrl(previewUrl?: string, downloadUrl?: string): string {
  if (previewUrl) return previewUrl
  if (downloadUrl) return downloadUrl.replace("/download", "/preview")
  return ""
}

export function pickPreviewableIndex(
  attachments: Array<{ fileName: string; fileType?: string; previewUrl?: string; downloadUrl?: string }>,
): number {
  const priority: PreviewKind[] = ["markdown", "pdf", "image", "word", "text"]
  for (const kind of priority) {
    const idx = attachments.findIndex((a) => {
      if (!resolvePreviewUrl(a.previewUrl, a.downloadUrl)) return false
      return detectPreviewKind(a.fileName, a.fileType) === kind
    })
    if (idx >= 0) return idx
  }
  return 0
}

export async function fetchFileBlob(url: string): Promise<Blob> {
  const res = (await request.get(url, { responseType: "blob" })) as unknown as AxiosResponse<Blob>
  const blob = res.data
  if (blob.type?.includes("application/json")) {
    const text = await blob.text()
    let msg = "文件加载失败"
    try {
      const parsed = JSON.parse(text) as { message?: string }
      msg = parsed.message || msg
    } catch { /* keep default */ }
    throw new Error(msg)
  }
  return blob
}

export async function loadTextContent(url: string): Promise<string> {
  const blob = await fetchFileBlob(url)
  return blob.text()
}

export async function loadWordHtml(url: string, fileName?: string): Promise<string> {
  const ext = (fileName ?? url).split(".").pop()?.toLowerCase() ?? ""
  if (ext === "doc") {
    throw new Error("暂不支持 .doc 在线预览，请下载后查看")
  }
  const mammoth = await import("mammoth")
  const blob = await fetchFileBlob(url)
  const arrayBuffer = await blob.arrayBuffer()
  const result = await mammoth.convertToHtml({ arrayBuffer })
  return result.value
}

/** 从本地 File 对象加载预览内容（试交页选文件后立即预览） */
export async function loadLocalFilePreview(file: File): Promise<LocalPreviewResult> {
  const kind = detectPreviewKind(file.name, file.type)

  try {
    switch (kind) {
      case "image":
      case "pdf":
        return { kind, blobUrl: URL.createObjectURL(file) }
      case "markdown": {
        const text = await file.text()
        return { kind, markdownHtml: renderMarkdownHtml(text) }
      }
      case "text":
        return { kind, textContent: await file.text() }
      case "word": {
        const ext = file.name.split(".").pop()?.toLowerCase() ?? ""
        if (ext === "doc") {
          return { kind, error: "暂不支持 .doc 在线预览，请更换为 .docx 后重试" }
        }
        const mammoth = await import("mammoth")
        const result = await mammoth.convertToHtml({ arrayBuffer: await file.arrayBuffer() })
        return { kind, wordHtml: result.value }
      }
      case "archive":
      case "unknown":
        return { kind }
    }
  } catch (err: unknown) {
    return {
      kind: "unknown",
      error: (err as Error)?.message || "预览加载失败",
    }
  }
}

/** 从服务端 URL 加载预览（知识库、审核页等） */
export async function loadRemoteFilePreview(
  downloadUrl: string,
  fileName: string,
  fileType?: string,
  previewUrl?: string,
): Promise<LocalPreviewResult> {
  const url = resolvePreviewUrl(previewUrl, downloadUrl)
  if (!url) {
    return { kind: "unknown", error: "文件链接不可用" }
  }

  const kind = detectPreviewKind(fileName, fileType)

  try {
    switch (kind) {
      case "image":
      case "pdf": {
        const blob = await fetchFileBlob(url)
        const mime = blob.type.toLowerCase()
        if (kind === "pdf" && mime.includes("pdf")) {
          return { kind: "pdf", blobUrl: URL.createObjectURL(blob) }
        }
        if (mime.includes("markdown") || mime.startsWith("text/") || mime === "application/octet-stream") {
          const text = await blob.text()
          if (text.trimStart().startsWith("#") || text.includes("\n## ") || mime.includes("markdown")) {
            return { kind: "markdown", markdownHtml: renderMarkdownHtml(text) }
          }
        }
        if (kind === "pdf") {
          return { kind: "pdf", blobUrl: URL.createObjectURL(blob) }
        }
        return { kind: "image", blobUrl: URL.createObjectURL(blob) }
      }
      case "markdown": {
        const md = await loadTextContent(url)
        return { kind, markdownHtml: renderMarkdownHtml(md) }
      }
      case "text":
        return { kind, textContent: await loadTextContent(url) }
      case "word":
        return { kind, wordHtml: await loadWordHtml(url, fileName) }
      case "archive":
      case "unknown":
        return { kind }
    }
  } catch (err: unknown) {
    return {
      kind: "unknown",
      error: (err as Error)?.message || "预览加载失败",
    }
  }
}

export const KNOWLEDGE_UPLOAD_ACCEPT = ".pdf,.md,.markdown,.docx"

export function isKnowledgeUploadFile(file: File): boolean {
  const name = file.name.toLowerCase()
  return [".pdf", ".md", ".markdown", ".docx"].some((ext) => name.endsWith(ext))
}
