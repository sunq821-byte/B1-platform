<script setup lang="ts">
import { ref, watch, onBeforeUnmount } from "vue"
import { FileWarning, Package } from "lucide-vue-next"
import {
  loadRemoteFilePreview,
  type PreviewKind,
} from "@/utils/filePreview"

const props = defineProps<{
  downloadUrl: string
  previewUrl?: string
  fileName: string
  fileType?: string
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

async function loadPreview() {
  if (!props.downloadUrl) {
    previewError.value = "文件链接不可用"
    return
  }
  loading.value = true
  resetPreview()
  try {
    const result = await loadRemoteFilePreview(
      props.downloadUrl,
      props.fileName,
      props.fileType,
      props.previewUrl,
    )
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
  () => [props.downloadUrl, props.fileName, props.previewUrl] as const,
  () => loadPreview(),
  { immediate: true },
)

onBeforeUnmount(resetPreview)
</script>

<template>
  <div class="remote-preview">
    <div v-if="loading" class="remote-preview__loading">
      <div class="remote-preview__skeleton" />
    </div>

    <div v-else class="remote-preview__body">
      <div v-if="previewError" class="remote-preview__warn">
        <FileWarning :size="18" />
        <span>{{ previewError }}</span>
      </div>

      <div v-else-if="previewKind === 'markdown'" class="remote-preview__document md-document" v-html="markdownHtml" />

      <div v-else-if="previewKind === 'text'" class="remote-preview__document plain-document">
        <pre>{{ textContent }}</pre>
      </div>

      <div v-else-if="previewKind === 'image'" class="remote-preview__media">
        <img :src="blobUrl" :alt="fileName" />
      </div>

      <div v-else-if="previewKind === 'pdf'" class="remote-preview__media remote-preview__media--pdf">
        <iframe :src="blobUrl" title="PDF 预览" />
      </div>

      <div v-else-if="previewKind === 'word'" class="remote-preview__document word-document" v-html="wordHtml" />

      <div v-else class="remote-preview__fallback">
        <Package :size="32" />
        <p class="remote-preview__fallback-title">暂不支持在线预览</p>
        <p class="remote-preview__fallback-desc">请下载后查看</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.remote-preview {
  min-height: 420px;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: var(--radius-lg, 12px);
  background: var(--color-card, #fff);
  overflow: hidden;
}

.remote-preview__loading {
  flex: 1;
  padding: 16px;
}

.remote-preview__skeleton {
  height: 100%;
  min-height: 380px;
  border-radius: 8px;
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: preview-shimmer 1.2s infinite;
}

@keyframes preview-shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.remote-preview__body {
  flex: 1;
  min-height: 420px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.remote-preview__warn {
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

.remote-preview__document {
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

.remote-preview__media {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 0;
  overflow: auto;
  padding: 16px;
  background: #f8fafc;
}

.remote-preview__media img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.remote-preview__media--pdf {
  padding: 0;
}

.remote-preview__media--pdf iframe {
  width: 100%;
  height: 100%;
  min-height: 420px;
  border: none;
}

.remote-preview__fallback {
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

.remote-preview__fallback-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
}

.remote-preview__fallback-desc {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-secondary, #64748b);
}
</style>
