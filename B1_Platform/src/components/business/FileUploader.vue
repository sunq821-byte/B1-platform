<script setup lang="ts">
import { ref, computed } from "vue"
import { Upload, FileText, X, AlertCircle, CheckCircle2 } from "lucide-vue-next"
import request from "@/api/request"

const props = withDefaults(defineProps<{
  accept?: string
  maxSize?: number
  disabled?: boolean
}>(), {
  accept: ".zip",
  maxSize: 50,
  disabled: false,
})

const emit = defineEmits<{
  (e: "upload-success", data: { fileId: string; fileName: string; fileSize: number }): void
  (e: "upload-error", message: string): void
  (e: "upload-progress", percent: number): void
  (e: "file-removed"): void
}>()

type UploadState = "idle" | "uploading" | "uploaded" | "error"

const uploadState = ref<UploadState>("idle")
const isDragging = ref(false)
const selectedFile = ref<File | null>(null)
const uploadedFileId = ref("")
const errorMessage = ref("")
const uploadPercent = ref(0)
const fileInput = ref<HTMLInputElement | null>(null)

const fileDisplay = computed(() => {
  if (!selectedFile.value) return null
  return {
    name: selectedFile.value.name,
    size: formatFileSize(selectedFile.value.size),
  }
})

const allowedExtensions = computed(() => {
  return props.accept
    .split(",")
    .map((ext) => ext.trim().toLowerCase())
})

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function getFileExt(file: File): string {
  const parts = file.name.split(".")
  return parts.length > 1 ? "." + parts[parts.length - 1].toLowerCase() : ""
}

function validateFile(file: File): boolean {
  const ext = getFileExt(file)
  if (!allowedExtensions.value.includes(ext)) {
    errorMessage.value = `不支持的文件格式，仅支持: ${props.accept}`
    uploadState.value = "error"
    emit("upload-error", errorMessage.value)
    return false
  }
  if (file.size > props.maxSize * 1024 * 1024) {
    errorMessage.value = `文件大小超出限制，最大 ${props.maxSize}MB`
    uploadState.value = "error"
    emit("upload-error", errorMessage.value)
    return false
  }
  return true
}

async function uploadFile(file: File) {
  uploadState.value = "uploading"
  uploadPercent.value = 0
  errorMessage.value = ""

  const formData = new FormData()
  formData.append("file", file)

  try {
    const res = await request.post("/api/v1/files/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" },
      onUploadProgress: (e: { loaded?: number; total?: number }) => {
        if (e.total && e.loaded != null) {
          uploadPercent.value = Math.round((e.loaded / e.total) * 100)
          emit("upload-progress", uploadPercent.value)
        }
      },
    }) as { fileId: string; fileName: string; fileSize: number; fileUrl: string }

    uploadedFileId.value = res.fileId
    uploadState.value = "uploaded"
    emit("upload-success", {
      fileId: res.fileId,
      fileName: res.fileName,
      fileSize: res.fileSize,
    })
  } catch (err: unknown) {
    const msg = (err as Error)?.message || "文件上传失败"
    errorMessage.value = msg
    uploadState.value = "error"
    emit("upload-error", msg)
  }
}

function handleFile(file: File) {
  if (validateFile(file)) {
    selectedFile.value = file
    uploadFile(file)
  }
}

function onDragOver(e: DragEvent) {
  e.preventDefault()
  if (!props.disabled) isDragging.value = true
}

function onDragLeave() {
  isDragging.value = false
}

function onDrop(e: DragEvent) {
  e.preventDefault()
  isDragging.value = false
  if (props.disabled) return
  const file = e.dataTransfer?.files?.[0]
  if (file) handleFile(file)
}

function triggerFileInput() {
  if (props.disabled) return
  fileInput.value?.click()
}

function onInputChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (file) handleFile(file)
  if (input) input.value = ""
}

function removeFile() {
  selectedFile.value = null
  uploadedFileId.value = ""
  uploadState.value = "idle"
  uploadPercent.value = 0
  errorMessage.value = ""
  emit("file-removed")
}

</script>

<template>
  <div class="file-uploader">
    <!-- Drag-drop zone (idle / error without file) -->
    <div
      v-if="uploadState === 'idle' || (uploadState === 'error' && !selectedFile)"
      class="file-uploader__zone"
      :class="{
        'file-uploader__zone--dragging': isDragging,
        'file-uploader__zone--disabled': disabled,
        'file-uploader__zone--error': uploadState === 'error',
      }"
      @click="triggerFileInput"
      @dragover="onDragOver"
      @dragleave="onDragLeave"
      @drop="onDrop"
    >
      <input
        ref="fileInput"
        type="file"
        hidden
        :accept="accept"
        :disabled="disabled"
        @change="onInputChange"
      />

      <Upload :size="36" class="file-uploader__upload-icon" />
      <p class="file-uploader__zone-text">拖拽文件到此处或点击上传</p>
      <p class="file-uploader__zone-hint">
        支持格式：{{ accept }}，最大 {{ maxSize }}MB
      </p>
    </div>

    <!-- Uploading progress -->
    <div v-if="uploadState === 'uploading'" class="file-uploader__progress">
      <div class="file-uploader__file-info">
        <FileText :size="22" class="file-uploader__file-icon" />
        <div class="file-uploader__file-detail">
          <span class="file-uploader__file-name">{{ fileDisplay?.name }}</span>
          <span class="file-uploader__file-size">{{ fileDisplay?.size }}</span>
        </div>
      </div>
      <div class="file-uploader__progress-bar">
        <div
          class="file-uploader__progress-fill"
          :style="{ width: uploadPercent + '%' }"
        />
      </div>
      <span class="file-uploader__progress-text">上传中 {{ uploadPercent }}%</span>
    </div>

    <!-- Uploaded (success) -->
    <div v-if="uploadState === 'uploaded'" class="file-uploader__uploaded">
      <div class="file-uploader__file-info">
        <CheckCircle2 :size="22" color="#10B981" />
        <div class="file-uploader__file-detail">
          <span class="file-uploader__file-name">{{ fileDisplay?.name }}</span>
          <span class="file-uploader__file-size">{{ fileDisplay?.size }}</span>
        </div>
      </div>
      <button
        class="file-uploader__remove-btn"
        :disabled="disabled"
        @click="removeFile"
      >
        <X :size="18" />
      </button>
    </div>

    <!-- Error with retry -->
    <div v-if="uploadState === 'error' && selectedFile" class="file-uploader__error-box">
      <div class="file-uploader__file-info">
        <AlertCircle :size="22" color="#EF4444" />
        <div class="file-uploader__file-detail">
          <span class="file-uploader__file-name">{{ fileDisplay?.name }}</span>
          <span class="file-uploader__error-text">{{ errorMessage }}</span>
        </div>
      </div>
      <button
        class="file-uploader__remove-btn"
        :disabled="disabled"
        @click="removeFile"
      >
        <X :size="18" />
      </button>
    </div>

    <!-- Error alert for zone-level errors -->
    <div v-if="uploadState === 'error' && !selectedFile" class="file-uploader__alert">
      <AlertCircle :size="16" />
      <span>{{ errorMessage }}</span>
      <X :size="16" class="file-uploader__alert-close" @click="errorMessage = ''" />
    </div>
  </div>
</template>

<style scoped>
.file-uploader {
  width: 100%;
}

/* ---- Drop zone ---- */
.file-uploader__zone {
  border: 2px dashed #e2e8f0;
  border-radius: 12px;
  min-height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 200ms ease, background-color 200ms ease;
  padding: 32px 24px;
}

.file-uploader__zone:hover {
  border-color: #3B82F6;
  background: #f8fafc;
}

.file-uploader__zone--dragging {
  border-color: #3B82F6;
  background: rgba(59, 130, 246, 0.06);
}

.file-uploader__zone--disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.file-uploader__zone--error {
  border-color: #EF4444;
}

.file-uploader__upload-icon {
  color: #94a3b8;
  margin-bottom: 12px;
}

.file-uploader__zone-text {
  margin: 0 0 6px 0;
  font-size: 14px;
  color: #64748b;
}

.file-uploader__zone-hint {
  margin: 0;
  font-size: 12px;
  color: #94a3b8;
}

/* ---- Progress ---- */
.file-uploader__progress {
  padding: 14px 18px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #f8fafc;
}

.file-uploader__progress-bar {
  margin: 10px 0 6px;
  height: 6px;
  background: #e2e8f0;
  border-radius: 3px;
  overflow: hidden;
}

.file-uploader__progress-fill {
  height: 100%;
  background: #3B82F6;
  border-radius: 3px;
  transition: width 300ms ease;
}

.file-uploader__progress-text {
  font-size: 12px;
  color: #94a3b8;
}

/* ---- Uploaded ---- */
.file-uploader__uploaded {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  border: 1px solid #10B981;
  border-radius: 10px;
  background: #f0fdf4;
}

/* ---- Error box ---- */
.file-uploader__error-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  border: 1px solid #FECACA;
  border-radius: 10px;
  background: #FEF2F2;
}

.file-uploader__error-text {
  font-size: 12px;
  color: #EF4444;
}

/* ---- Alert ---- */
.file-uploader__alert {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  padding: 10px 14px;
  background: #FEF2F2;
  border: 1px solid #FECACA;
  border-radius: 8px;
  color: #EF4444;
  font-size: 13px;
}

.file-uploader__alert-close {
  margin-left: auto;
  cursor: pointer;
  opacity: 0.7;
}

.file-uploader__alert-close:hover {
  opacity: 1;
}

/* ---- Shared file info ---- */
.file-uploader__file-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-uploader__file-icon {
  color: #64748b;
  flex-shrink: 0;
}

.file-uploader__file-detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.file-uploader__file-name {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
  word-break: break-all;
}

.file-uploader__file-size {
  font-size: 12px;
  color: #94a3b8;
}

.file-uploader__remove-btn {
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  transition: color 150ms, background 150ms;
}

.file-uploader__remove-btn:hover {
  color: #EF4444;
  background: #fee2e2;
}

.file-uploader__remove-btn:disabled {
  cursor: not-allowed;
  opacity: 0.4;
}
</style>
