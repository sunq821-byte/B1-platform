<script setup lang="ts">
import { ref, watch, onBeforeUnmount } from "vue"
import { FileWarning, Package, RefreshCw, Trash2 } from "lucide-vue-next"
import {
  loadLocalFilePreview,
  type PreviewKind,
} from "@/utils/filePreview"

const props = withDefaults(defineProps<{
  file: File | null
  editable?: boolean
}>(), {
  editable: true,
})

const emit = defineEmits<{
  replace: []
  remove: []
}>()

const loading = ref(false)
const previewKind = ref<PreviewKind>("unknown")
const blobUrl = ref("")
const textContent = ref("")
const markdownHtml = ref("")
const wordHtml = ref("")
const previewError = ref("")

function revokeBlob() {
  if (blobUrl.value) {
    URL.revokeObjectURL(blobUrl.value)
    blobUrl.value = ""
  }
}

function resetPreview() {
  revokeBlob()
  previewKind.value = "unknown"
  textContent.value = ""
  markdownHtml.value = ""
  wordHtml.value = ""
  previewError.value = ""
}

async function loadPreview(file: File) {
  loading.value = true
  resetPreview()
  try {
    const result = await loadLocalFilePreview(file)
    previewKind.value = result.kind
    if (result.blobUrl) blobUrl.value = result.blobUrl
    textContent.value = result.textContent ?? ""
    markdownHtml.value = result.markdownHtml ?? ""
    wordHtml.value = result.wordHtml ?? ""
    previewError.value = result.error ?? ""
  } catch (err: unknown) {
    previewError.value = (err as Error)?.message || "预览加载失败"
    previewKind.value = "unknown"
  } finally {
    loading.value = false
  }
}

watch(
  () => props.file,
  (file) => {
    if (file) loadPreview(file)
    else resetPreview()
  },
  { immediate: true },
)

onBeforeUnmount(resetPreview)

function formatFileSize(bytes: number) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}
</script>

<template>
  <div v-if="file" class="local-preview">
    <div class="local-preview__head">
      <div class="local-preview__meta">
        <span class="local-preview__name">{{ file.name }}</span>
        <span class="local-preview__size">{{ formatFileSize(file.size) }}</span>
      </div>
      <div v-if="editable" class="local-preview__actions">
        <button type="button" class="local-preview__btn" @click="emit('replace')">
          <RefreshCw :size="14" />
          更换文件
        </button>
        <button type="button" class="local-preview__btn local-preview__btn--danger" @click="emit('remove')">
          <Trash2 :size="14" />
          移除
        </button>
      </div>
    </div>

    <div v-if="loading" class="local-preview__loading">
      <div class="local-preview__skeleton" />
    </div>

    <div v-else class="local-preview__body">
      <div v-if="previewError" class="local-preview__warn">
        <FileWarning :size="18" />
        <span>{{ previewError }}</span>
      </div>

      <div v-else-if="previewKind === 'markdown'" class="local-preview__document md-document" v-html="markdownHtml" />

      <div v-else-if="previewKind === 'text'" class="local-preview__document plain-document">
        <pre>{{ textContent }}</pre>
      </div>

      <div v-else-if="previewKind === 'image'" class="local-preview__media">
        <img :src="blobUrl" :alt="file.name" />
      </div>

      <div v-else-if="previewKind === 'pdf'" class="local-preview__media local-preview__media--pdf">
        <iframe :src="blobUrl" title="PDF 预览" />
      </div>

      <div v-else-if="previewKind === 'word'" class="local-preview__document word-document" v-html="wordHtml" />

      <div v-else class="local-preview__fallback">
        <Package :size="32" />
        <p class="local-preview__fallback-title">
          {{ previewKind === "archive" ? "压缩包文件" : "暂不支持在线预览" }}
        </p>
        <p class="local-preview__fallback-desc">
          {{ previewKind === "archive"
            ? "请确认压缩包内容完整；提交后教师将下载查看。"
            : "当前格式无法预览，可更换文件或直接提交。" }}
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.local-preview {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: var(--radius-lg, 12px);
  background: var(--color-card, #fff);
  overflow: hidden;
}

.local-preview__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 14px;
  border-bottom: 1px solid var(--color-border, #E5E7EB);
  background: #f8fafc;
  flex-shrink: 0;
}

.local-preview__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex-wrap: wrap;
}

.local-preview__name {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
  word-break: break-all;
}

.local-preview__size {
  font-size: 11px;
  color: var(--color-text-placeholder, #94a3b8);
  font-family: var(--font-mono, "JetBrains Mono", monospace);
}

.local-preview__actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.local-preview__btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: 6px;
  background: #fff;
  font-size: 12px;
  color: var(--color-text-secondary, #64748b);
  cursor: pointer;
  transition: border-color 150ms, color 150ms, background 150ms;
}

.local-preview__btn:hover {
  border-color: var(--color-primary, #1677FF);
  color: var(--color-primary, #1677FF);
}

.local-preview__btn--danger:hover {
  border-color: var(--color-danger, #FF4D4F);
  color: var(--color-danger, #FF4D4F);
}

.local-preview__loading {
  flex: 1;
  padding: 16px;
}

.local-preview__skeleton {
  height: 100%;
  min-height: 240px;
  border-radius: 8px;
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: preview-shimmer 1.2s infinite;
}

@keyframes preview-shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.local-preview__body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.local-preview__warn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px;
  font-size: 13px;
  color: #b45309;
  background: #fffbeb;
}

.local-preview__document {
  flex: 1;
  overflow: auto;
}

.md-document,
.word-document {
  padding: 28px 32px;
  font-size: 15px;
  line-height: 1.75;
  color: var(--color-text-primary, #1e293b);
}

.md-document :deep(h1) {
  font-size: 1.5em;
  font-weight: 700;
  margin: 0 0 0.75em;
  color: var(--color-navy, #0B3A5C);
}

.md-document :deep(h2) {
  font-size: 1.25em;
  font-weight: 700;
  margin: 1.25em 0 0.5em;
}

.md-document :deep(p) {
  margin: 0 0 0.85em;
}

.plain-document {
  padding: 20px 24px;
}

.plain-document pre {
  margin: 0;
  font-family: var(--font-mono, "JetBrains Mono", monospace);
  font-size: 13px;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
}

.local-preview__media {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 0;
  overflow: auto;
  padding: 16px;
  background: #f8fafc;
}

.local-preview__media img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(15, 23, 42, 0.08);
}

.local-preview__media--pdf {
  padding: 0;
}

.local-preview__media--pdf iframe {
  width: 100%;
  height: 100%;
  min-height: 320px;
  border: none;
}

.local-preview__fallback {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 32px 24px;
  text-align: center;
  color: var(--color-primary, #1677FF);
}

.local-preview__fallback-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
}

.local-preview__fallback-desc {
  margin: 0;
  max-width: 320px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--color-text-secondary, #64748b);
}
</style>
