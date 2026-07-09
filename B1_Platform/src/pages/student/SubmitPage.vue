<script setup lang="ts">
import { ref, computed, onMounted } from "vue"
import { useRouter, useRoute } from "vue-router"
import { ElMessage } from "element-plus"
import { FileText, X } from "lucide-vue-next"
import { useStudentStore } from "@/stores/useStudentStore"
import request from "@/api/request"
import BaseInput from "@/components/base/BaseInput.vue"
import BaseButton from "@/components/base/BaseButton.vue"
import PageHeader from "@/components/layout/PageHeader.vue"
import CodeEditor from "@/components/business/CodeEditor.vue"
import TaskStatusBadge from "@/components/business/TaskStatusBadge.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"

const router = useRouter()
const route = useRoute()
const store = useStudentStore()

const taskId = computed(() => route.params.taskId as string)
const loading = ref(true)
const loadError = ref("")
const submitting = ref(false)

// --- Mode ---
type SubmitMode = "file" | "code" | "git"
const currentMode = ref<SubmitMode>("file")

// --- File ---
const fileInput = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)
const isDragging = ref(false)
const uploadedFileId = ref("")

// --- Code ---
const codeLang = ref("java")
const codeContent = ref("")

// --- Git ---
const gitUrl = ref("")
const gitBranch = ref("main")
const gitVerifying = ref(false)
const gitVerified = ref(false)
const gitVerifyResult = ref<{ repoName: string; branch: string; lastCommitTime: string } | null>(null)

// --- Remark ---
const remark = ref("")

// --- History ---
const history = ref<Array<{
  submissionId: string
  version: number
  submittedAt: string
  status: string
  aiScore: number | null
}>>([])

// --- Init ---
async function initPage() {
  loading.value = true
  loadError.value = ""
  try {
    await store.fetchTaskDetail(taskId.value)
  } catch (err: unknown) {
    loadError.value = (err as Error)?.message || "加载任务信息失败"
  } finally {
    loading.value = false
  }
}

async function loadHistory() {
  try {
    const data = await request.get(
      `/api/v1/student/tasks/${taskId.value}/submissions`,
    ) as Array<{
      submissionId: string
      submittedAt: string
      status: string
      aiScore: number | null
    }>
    history.value = data
      .sort((a, b) => new Date(a.submittedAt).getTime() - new Date(b.submittedAt).getTime())
      .map((s, i) => ({ ...s, version: i + 1 }))
  } catch {
    history.value = []
  }
}

onMounted(() => { initPage() })

// --- File handlers ---
function onDragOver(e: DragEvent) { e.preventDefault(); isDragging.value = true }
function onDragLeave() { isDragging.value = false }

function onDrop(e: DragEvent) {
  e.preventDefault()
  isDragging.value = false
  const file = e.dataTransfer?.files?.[0]
  if (file) handleFile(file)
}

function triggerFileInput() { fileInput.value?.click() }

function onFileInputChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (file) handleFile(file)
  if (input) input.value = ""
}

function handleFile(file: File) {
  selectedFile.value = file
}

function removeFile() {
  selectedFile.value = null
  uploadedFileId.value = ""
}

function formatFileSize(bytes: number) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

// --- Git ---
async function handleGitVerify() {
  if (!gitUrl.value) { ElMessage.warning("请输入Git仓库地址"); return }
  gitVerifying.value = true
  try {
    const res = await store.verifyGit(taskId.value, { gitUrl: gitUrl.value })
    if (res.valid) {
      gitVerified.value = true
      gitVerifyResult.value = {
        repoName: res.repoName,
        branch: res.defaultBranch,
        lastCommitTime: res.latestCommit.committedAt,
      }
      ElMessage.success("Git仓库验证通过")
    } else {
      ElMessage.error("Git仓库验证失败")
    }
  } catch (err: unknown) {
    ElMessage.error((err as Error)?.message || "验证失败")
  } finally {
    gitVerifying.value = false
  }
}

// --- Submit ---
async function handleSubmit() {
  if (currentMode.value === "file" && !selectedFile.value) {
    ElMessage.warning("请先选择要上传的文件"); return
  }
  if (currentMode.value === "code" && !codeContent.value.trim()) {
    ElMessage.warning("请先编写代码"); return
  }
  if (currentMode.value === "git" && !gitUrl.value.trim()) {
    ElMessage.warning("请输入 Git 仓库地址"); return
  }

  submitting.value = true
  try {
    let submissionId = ""
    if (currentMode.value === "file") {
      const formData = new FormData()
      formData.append("file", selectedFile.value!)
      const uploadRes = await request.post("/api/v1/files/upload", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      }) as { fileId: string }
      uploadedFileId.value = uploadRes.fileId
      const res = await store.submitTask(taskId.value, {
        submissionType: "ZIP_UPLOAD",
        zipFileId: uploadRes.fileId,
        remark: remark.value || undefined,
      })
      submissionId = res.submissionId
    } else if (currentMode.value === "code") {
      const blob = new Blob([codeContent.value], { type: "text/plain" })
      const formData = new FormData()
      formData.append("file", blob, "code.txt")
      const uploadRes = await request.post("/api/v1/files/upload", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      }) as { fileId: string }
      const res = await store.submitTask(taskId.value, {
        submissionType: "ZIP_UPLOAD",
        zipFileId: uploadRes.fileId,
        remark: remark.value || undefined,
      })
      submissionId = res.submissionId
    } else {
      const res = await store.submitTask(taskId.value, {
        submissionType: "GIT_URL",
        gitUrl: gitUrl.value,
        gitBranch: gitBranch.value || undefined,
        remark: remark.value || undefined,
      })
      submissionId = res.submissionId
    }

    ElMessage.success("提交成功，正在启动 AI 分析...")
    try {
      await store.triggerAIEvaluation(submissionId)
      router.push(`/student/grades/${submissionId}`)
    } catch {
      ElMessage.warning("提交成功，但 AI 分析启动失败，请稍后手动重试")
      router.push(`/student/grades/${submissionId}`)
    }
  } catch (err: unknown) {
    ElMessage.error((err as Error)?.message || "提交失败")
  } finally {
    submitting.value = false
  }
}

function handleRetry() { initPage() }

function formatDate(dateStr: string) {
  if (!dateStr) return "-"
  return dateStr.replace("T", " ").substring(0, 16)
}
</script>

<template>
  <div class="submit-page">
    <PageHeader
      :title="`${store.currentTask?.taskName ?? '成果提交'}`"
      :subtitle="store.currentTask ? `${store.currentTask.courseName} · 截止 ${formatDate(store.currentTask.deadline)}` : ''"
    />

    <LoadingState v-if="loading" text="加载任务信息..." />
    <ErrorState v-else-if="loadError" :message="loadError" @retry="handleRetry" />

    <template v-else-if="store.currentTask">
      <!-- Task Summary Card -->
      <div class="card">
        <div class="card__title">任务摘要</div>
        <table class="summary-table">
          <tbody>
            <tr>
              <td class="summary-label">任务名称</td>
              <td class="summary-value"><b>{{ store.currentTask.taskName }}</b></td>
            </tr>
            <tr>
              <td class="summary-label">课程</td>
              <td class="summary-value">{{ store.currentTask.courseName }}</td>
            </tr>
            <tr>
              <td class="summary-label">截止日期</td>
              <td class="summary-value mono">{{ formatDate(store.currentTask.deadline) }}</td>
            </tr>
            <tr>
              <td class="summary-label">满分</td>
              <td class="summary-value mono">{{ store.currentTask.totalScore }}</td>
            </tr>
            <tr v-if="store.currentTask.mySubmissionStatus !== 'NOT_SUBMITTED'">
              <td class="summary-label">当前状态</td>
              <td class="summary-value">
                <TaskStatusBadge :status="store.currentTask.mySubmissionStatus" />
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Mode Selection -->
      <div class="card">
        <div class="card__title">提交方式</div>

        <div class="mode-cards">
          <div
            :class="['mode-card', { 'mode-card--active': currentMode === 'file' }]"
            @click="currentMode = 'file'"
          >
            <div class="mode-card__icon">&#x1F4C1;</div>
            <div class="mode-card__label">文件上传</div>
            <div class="mode-card__desc">文档 / 截图</div>
          </div>
          <div
            :class="['mode-card', { 'mode-card--active': currentMode === 'code' }]"
            @click="currentMode = 'code'"
          >
            <div class="mode-card__icon">&#x1F4BB;</div>
            <div class="mode-card__label">代码提交</div>
            <div class="mode-card__desc">在线编辑器</div>
          </div>
          <div
            :class="['mode-card', { 'mode-card--active': currentMode === 'git' }]"
            @click="currentMode = 'git'"
          >
            <div class="mode-card__icon">&#x1F517;</div>
            <div class="mode-card__label">Git 仓库</div>
            <div class="mode-card__desc">URL 提交</div>
          </div>
        </div>

        <!-- File Upload Panel -->
        <div v-show="currentMode === 'file'">
          <div
            :class="['upload-zone', { 'upload-zone--dragging': isDragging }]"
            @click="triggerFileInput"
            @dragover="onDragOver"
            @dragleave="onDragLeave"
            @drop="onDrop"
          >
            <div class="upload-zone__icon">&#x1F4C2;</div>
            <div class="upload-zone__text">点击或拖拽文件到此处</div>
            <div class="upload-zone__hint">支持 .zip .doc .docx .pdf .png .jpg .gif  最大 50MB</div>
            <div class="upload-zone__tags">
              <span class="upload-zone__tag">.zip</span>
              <span class="upload-zone__tag">.doc</span>
              <span class="upload-zone__tag">.pdf</span>
              <span class="upload-zone__tag">.png</span>
              <span class="upload-zone__tag">.jpg</span>
            </div>
            <input
              ref="fileInput"
              type="file"
              accept=".zip,.doc,.docx,.pdf,.png,.jpg,.jpeg,.gif"
              hidden
              @change="onFileInputChange"
            >
          </div>

          <div v-if="selectedFile" class="upload-file-info">
            <FileText :size="18" />
            <span>{{ selectedFile.name }} ({{ formatFileSize(selectedFile.size) }})</span>
            <X :size="16" class="upload-file-info__remove" @click="removeFile" />
          </div>
        </div>

        <!-- Code Editor Panel -->
        <div v-show="currentMode === 'code'">
          <CodeEditor v-model="codeContent" v-model:language="codeLang" />
        </div>

        <!-- Git Panel -->
        <div v-show="currentMode === 'git'" class="git-panel">
          <div class="form-group">
            <label class="form-label">Git 仓库地址</label>
            <div class="git-row">
              <BaseInput v-model="gitUrl" placeholder="https://github.com/username/repo.git" :disabled="submitting" />
              <BaseButton :loading="gitVerifying" :disabled="!gitUrl || submitting" @click="handleGitVerify">
                验证仓库
              </BaseButton>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">分支名称（可选）</label>
            <BaseInput v-model="gitBranch" placeholder="main" :disabled="submitting" />
          </div>
          <div v-if="gitVerified && gitVerifyResult" class="git-verify-result">
            仓库 {{ gitVerifyResult.repoName }} · 分支 {{ gitVerifyResult.branch }}
          </div>
        </div>
      </div>

      <!-- Remark -->
      <div class="card">
        <div class="card__title">提交备注</div>
        <textarea
          v-model="remark"
          class="remark-textarea"
          placeholder="可选：说明本次提交的内容、完成情况、遇到的问题等..."
          rows="4"
          :disabled="submitting"
        />
      </div>

      <!-- Submit History -->
      <div v-if="history.length > 0" class="card">
        <div class="card__title">提交历史</div>
        <div class="history-list">
          <div v-for="item in history" :key="item.submissionId" class="history-item">
            <span class="history-item__version">v{{ item.version }}</span>
            <span class="history-item__time">{{ formatDate(item.submittedAt) }}</span>
            <TaskStatusBadge :status="item.status" />
            <span v-if="item.aiScore != null" class="history-item__score">{{ item.aiScore }}</span>
          </div>
        </div>
      </div>

      <!-- Actions -->
      <div class="actions">
        <BaseButton type="primary" size="large" :loading="submitting" @click="handleSubmit">
          确认提交
        </BaseButton>
        <BaseButton size="large" @click="router.back()">取消</BaseButton>
      </div>
    </template>
  </div>
</template>

<style scoped>
.submit-page {
  /* width/centering handled by AppLayout (.app-layout__main = prototype .content) */
}

/* --- Card --- */
.card {
  background: var(--color-card, #fff);
  border-radius: var(--radius-md, 8px);
  padding: var(--spacing-lg, 24px);
  margin-bottom: var(--spacing-md, 16px);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.card__title {
  font-size: var(--font-size-base, 16px);
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
  margin-bottom: var(--spacing-md, 16px);
}

/* --- Summary Table --- */
.summary-table { width: 100%; font-size: 14px; }
.summary-label { padding: 6px 0; color: var(--color-text-secondary, #64748b); width: 80px; vertical-align: top; }
.summary-value { padding: 6px 0; color: var(--color-text-primary, #1e293b); }
.mono { font-family: "JetBrains Mono", "SF Mono", monospace; }

/* --- Mode Cards --- */
.mode-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.mode-card {
  border: 2px solid var(--color-border, #e2e8f0);
  border-radius: var(--radius-md, 8px);
  padding: 20px 16px;
  text-align: center;
  cursor: pointer;
  transition: border-color 150ms, background 150ms, box-shadow 150ms;
  background: var(--color-bg, #f8fafc);
}

.mode-card:hover {
  border-color: var(--color-primary, #3b82f6);
  background: var(--color-primary-light, #eff6ff);
}

.mode-card--active {
  border-color: var(--color-primary, #3b82f6);
  background: var(--color-primary-light, #eff6ff);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
}

.mode-card__icon { font-size: 32px; margin-bottom: 8px; }
.mode-card__label { font-size: 14px; font-weight: 600; margin-bottom: 4px; color: var(--color-text-primary, #1e293b); }
.mode-card__desc { font-size: 12px; color: var(--color-text-placeholder, #94a3b8); }

/* --- Upload Zone --- */
.upload-zone {
  border: 2px dashed var(--color-border, #e2e8f0);
  border-radius: var(--radius-md, 8px);
  padding: 40px 24px;
  text-align: center;
  cursor: pointer;
  transition: border-color 150ms, background 150ms;
  background: var(--color-bg, #f8fafc);
}

.upload-zone:hover,
.upload-zone--dragging {
  border-color: var(--color-primary, #3b82f6);
  background: var(--color-primary-light, #eff6ff);
}

.upload-zone__icon { font-size: 40px; margin-bottom: 10px; }
.upload-zone__text { font-size: 14px; font-weight: 500; color: var(--color-text-primary, #1e293b); }
.upload-zone__hint { font-size: 12px; color: var(--color-text-placeholder, #94a3b8); margin-top: 6px; }

.upload-zone__tags {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.upload-zone__tag {
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 100px;
  background: var(--color-bg, #f8fafc);
  border: 1px solid var(--color-border, #e2e8f0);
  color: var(--color-text-secondary, #64748b);
}

.upload-file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--color-primary-light, #eff6ff);
  border: 1px solid var(--color-primary, #3b82f6);
  border-radius: var(--radius-sm, 4px);
  padding: 10px 14px;
  margin-top: 12px;
  font-size: 13px;
  color: var(--color-primary, #3b82f6);
}

.upload-file-info__remove {
  margin-left: auto;
  cursor: pointer;
  opacity: 0.7;
}

.upload-file-info__remove:hover { opacity: 1; }

/* --- Git Panel --- */
.git-panel { margin-top: 0; }

.git-row {
  display: flex;
  gap: var(--spacing-sm, 8px);
}

.git-row > :first-child { flex: 1; }

.git-verify-result {
  margin-top: var(--spacing-md, 16px);
  padding: 10px 14px;
  background: #ecfdf5;
  border: 1px solid #10b981;
  border-radius: var(--radius-sm, 4px);
  font-size: 13px;
  color: #065f46;
}

.form-group { margin-bottom: var(--spacing-md, 16px); }

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary, #64748b);
  margin-bottom: 6px;
}

/* --- Remark --- */
.remark-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--color-border, #e2e8f0);
  border-radius: var(--radius-sm, 4px);
  font-size: 14px;
  font-family: inherit;
  color: var(--color-text-primary, #1e293b);
  background: var(--color-card, #fff);
  resize: vertical;
  outline: none;
  box-sizing: border-box;
}

.remark-textarea:focus { border-color: var(--color-primary, #3b82f6); }
.remark-textarea::placeholder { color: var(--color-text-placeholder, #94a3b8); }
.remark-textarea:disabled { background: var(--color-bg, #f8fafc); }

/* --- History --- */
.history-list {
  display: flex;
  flex-direction: column;
}

.history-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md, 16px);
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border, #e2e8f0);
  font-size: 13px;
}

.history-item:last-child { border-bottom: none; }

.history-item__version {
  font-family: "JetBrains Mono", "SF Mono", monospace;
  font-size: 12px;
  color: var(--color-text-placeholder, #94a3b8);
  width: 32px;
}

.history-item__time {
  flex: 1;
  color: var(--color-text-secondary, #64748b);
  font-family: "JetBrains Mono", "SF Mono", monospace;
  font-size: 12px;
}

.history-item__score {
  font-family: "JetBrains Mono", "SF Mono", monospace;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-primary, #3b82f6);
  width: 40px;
  text-align: right;
}

/* --- Actions --- */
.actions {
  display: flex;
  gap: 10px;
  margin-top: var(--spacing-md, 16px);
}
</style>
