<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from "vue"
import { useRouter, useRoute } from "vue-router"
import { ElMessage } from "element-plus"
import { ArrowLeft, Upload, Code2 } from "lucide-vue-next"
import { useStudentStore } from "@/stores/useStudentStore"
import request from "@/api/request"
import BaseButton from "@/components/base/BaseButton.vue"
import BaseTable from "@/components/base/BaseTable.vue"
import CodeEditor from "@/components/business/CodeEditor.vue"
import LocalFilePreview from "@/components/business/LocalFilePreview.vue"
import TaskStatusBadge from "@/components/business/TaskStatusBadge.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"
import { fetchSubmissionDraft } from "@/api/modules/student"
import { inferCodeLanguage } from "@/utils/codeLanguage"

const router = useRouter()
const route = useRoute()
const store = useStudentStore()

const taskId = computed(() => route.params.taskId as string)
const loading = ref(true)
const loadError = ref("")
const submitting = ref(false)

// --- Mode ---
type SubmitMode = "file" | "code"
const currentMode = ref<SubmitMode>("file")
const allowedModes = ref<SubmitMode[]>(["file", "code"])

const CODE_TEMPLATES: Record<string, string> = {
  java: `public class Main {\n    public static void main(String[] args) {\n        \n    }\n}`,
  python: `def main():\n    pass\n\nif __name__ == "__main__":\n    main()`,
  cpp: `#include <iostream>\nusing namespace std;\n\nint main() {\n    return 0;\n}`,
  javascript: `function main() {\n    \n}\n\nmain()`,
  go: `package main\n\nimport "fmt"\n\nfunc main() {\n    \n}`,
}

function resolveAllowedModes(t?: string): SubmitMode[] {
  const u = (t || "").toUpperCase()
  if (u === "FILE_CODE" || u === "ZIP_CODE") return ["file", "code"]
  if (u.includes("MIX") || u.includes("ALL")) return ["file", "code"]
  if (u === "ONLINE_CODE" || u === "CODE_ONLY") return ["code"]
  if (u === "ZIP_UPLOAD" || u === "ZIP_ONLY" || u === "FILE_UPLOAD") return ["file"]
  if (u.includes("CODE")) return ["code"]
  if (u.includes("ZIP") || u.includes("FILE")) return ["file"]
  return ["file"]
}

function selectMode(mode: SubmitMode) {
  if (!allowedModes.value.includes(mode)) {
    ElMessage.warning("当前任务不支持该提交方式")
    return
  }
  currentMode.value = mode
  if (mode === "code" && !codeContent.value.trim()) {
    codeContent.value = CODE_TEMPLATES[codeLang.value] || ""
  }
}

function goBack() {
  router.push("/student/tasks")
}

const submissionLabel: Record<string, string> = {
  ZIP_UPLOAD: "文件上传",
  ONLINE_CODE: "在线代码",
  ZIP_ONLY: "文件上传",
  FILE_CODE: "文件或在线代码",
}

const canEditSubmit = computed(() => {
  const s = store.currentTask?.mySubmissionStatus ?? "NOT_SUBMITTED"
  return ["NOT_SUBMITTED", "REJECTED", "DRAFT", "AI_READY", "AI_COMPLETED"].includes(s)
})

function viewLatestAI() {
  const sid = store.currentTask?.mySubmissionId
  if (!sid) {
    ElMessage.info("暂无 AI 评分记录")
    return
  }
  router.push(`/student/grades/${sid}`)
}

// --- File ---
const fileInput = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)
const isDragging = ref(false)
const uploadedFileId = ref("")

// --- Code ---
const codeLang = ref("java")
const codeContent = ref("")

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
    const modes = resolveAllowedModes(store.currentTask?.submissionType)
    allowedModes.value = modes
    currentMode.value = modes[0] || "file"
    await Promise.all([loadHistory(), restoreDraft()])
  } catch (err: unknown) {
    const msg = (err as Error)?.message || "加载任务信息失败"
    if (/不存在|已下架|已删除|404/.test(msg)) {
      ElMessage.warning("该实训任务已被删除或下架")
      router.replace("/student/tasks")
      return
    }
    loadError.value = msg
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

async function restoreDraft() {
  try {
    const draft = await fetchSubmissionDraft(taskId.value)
    if (!draft) return

    if (draft.remark) remark.value = draft.remark

    const submitType = (draft.submitType || "").toUpperCase()
    if (submitType.includes("CODE") && draft.onlineCode?.trim()) {
      codeContent.value = draft.onlineCode
      codeLang.value = inferCodeLanguage(draft.onlineCode)
      if (allowedModes.value.includes("code")) {
        currentMode.value = "code"
      }
    }
  } catch {
    /* 无草稿或加载失败时保持默认空状态 */
  }
}

onMounted(() => {
  initPage()
  document.addEventListener("paste", handlePaste)
})

onBeforeUnmount(() => {
  document.removeEventListener("paste", handlePaste)
})

// --- File handlers ---
function onDragOver(e: DragEvent) {
  if (!canEditSubmit.value) return
  e.preventDefault()
  isDragging.value = true
}
function onDragLeave() { isDragging.value = false }

function onDrop(e: DragEvent) {
  if (!canEditSubmit.value) return
  e.preventDefault()
  isDragging.value = false
  const file = e.dataTransfer?.files?.[0]
  if (file) handleFile(file)
}

function triggerFileInput() {
  if (!canEditSubmit.value) return
  fileInput.value?.click()
}

function onFileInputChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (file) handleFile(file)
  if (input) input.value = ""
}

function handleFile(file: File) {
  selectedFile.value = file
}

// 文件上传方式下支持 Ctrl+V 粘贴截图/图片：从剪贴板取出图片，
// 补一个带正确后缀的文件名（后端按扩展名校验类型），再复用 handleFile。
function handlePaste(e: ClipboardEvent) {
  if (currentMode.value !== "file") return
  const items = e.clipboardData?.items
  if (!items) return
  for (let i = 0; i < items.length; i++) {
    const item = items[i]
    if (item.kind === "file" && item.type.startsWith("image/")) {
      const blob = item.getAsFile()
      if (!blob) continue
      const ext = (item.type.split("/")[1] || "png").toLowerCase()
      const named = new File([blob], `pasted-${Date.now()}.${ext}`, { type: item.type })
      handleFile(named)
      ElMessage.success(`已粘贴图片：${named.name}`)
      e.preventDefault()
      return
    }
  }
}

function removeFile() {
  selectedFile.value = null
  uploadedFileId.value = ""
}

// --- Submit → AI 评分 → 跳转结果页确认 ---
async function handleSubmit() {
  if (currentMode.value === "file" && !selectedFile.value) {
    ElMessage.warning("请先选择要上传的文件"); return
  }
  if (currentMode.value === "code" && !codeContent.value.trim()) {
    ElMessage.warning("请先编写代码"); return
  }

  submitting.value = true
  try {
    let res
    if (currentMode.value === "file") {
      const formData = new FormData()
      formData.append("file", selectedFile.value!)
      const uploadRes = await request.post("/api/v1/files/upload", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      }) as { fileId: string }
      uploadedFileId.value = uploadRes.fileId
      res = await store.trialSubmit(taskId.value, {
        submissionType: "ZIP_UPLOAD",
        zipFileId: uploadRes.fileId,
        remark: remark.value || undefined,
      })
    } else if (currentMode.value === "code") {
      const blob = new Blob([codeContent.value], { type: "text/plain" })
      const formData = new FormData()
      formData.append("file", blob, "code.txt")
      const uploadRes = await request.post("/api/v1/files/upload", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      }) as { fileId: string }
      res = await store.trialSubmit(taskId.value, {
        submissionType: "ONLINE_CODE",
        zipFileId: uploadRes.fileId,
        onlineCode: codeContent.value,
        remark: remark.value || undefined,
      })
    } else {
      ElMessage.warning("请先选择文件或填写代码")
      return
    }

    ElMessage.success("提交成功，AI 正在评分...")
    try {
      await store.triggerAIEvaluation(res.submissionId)
    } catch {
      ElMessage.warning("提交成功，但 AI 分析启动较慢，请在结果页查看进度")
    }
    router.push(`/student/grades/${res.submissionId}`)
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
    <LoadingState v-if="loading" text="加载任务信息..." />
    <ErrorState v-else-if="loadError" :message="loadError" @retry="handleRetry" />

    <template v-else-if="store.currentTask">
      <!-- Toolbar -->
      <header class="submit-toolbar">
        <button type="button" class="submit-toolbar__back" title="返回任务列表" @click="goBack">
          <ArrowLeft :size="18" />
          <span>返回列表</span>
        </button>
        <div class="submit-toolbar__title">
          <h1>{{ store.currentTask.taskName }}</h1>
          <p>{{ store.currentTask.courseName }} · 截止 {{ formatDate(store.currentTask.deadline) }}</p>
        </div>
        <TaskStatusBadge :status="store.currentTask.mySubmissionStatus" />
      </header>

      <!-- LeetCode-style workspace -->
      <div class="submit-workspace">
        <!-- Left: problem -->
        <aside class="problem-panel">
          <section class="problem-section">
            <h2 class="problem-section__title">任务信息</h2>
            <dl class="problem-meta">
              <div class="problem-meta__row">
                <dt>授课教师</dt>
                <dd>{{ store.currentTask.teacherName }}</dd>
              </div>
              <div class="problem-meta__row">
                <dt>满分</dt>
                <dd class="mono">{{ store.currentTask.totalScore }}</dd>
              </div>
              <div class="problem-meta__row">
                <dt>提交方式</dt>
                <dd>{{ submissionLabel[store.currentTask.submissionType] || store.currentTask.submissionType }}</dd>
              </div>
              <div class="problem-meta__row">
                <dt>AI 次数</dt>
                <dd class="mono">
                  {{ store.currentTask.mySubmitCount ?? 0 }} / {{ store.currentTask.maxAiAnalyzeCount ?? store.currentTask.maxSubmitCount ?? 3 }}
                </dd>
              </div>
              <div v-if="store.currentTask.myScore != null" class="problem-meta__row">
                <dt>成绩</dt>
                <dd class="mono">{{ store.currentTask.myScore }}</dd>
              </div>
            </dl>
          </section>

          <section class="problem-section">
            <h2 class="problem-section__title">任务描述</h2>
            <div class="problem-body">{{ store.currentTask.description || "暂无描述" }}</div>
          </section>

          <section class="problem-section">
            <h2 class="problem-section__title">任务要求</h2>
            <div class="problem-body">{{ store.currentTask.requirements || "暂无要求" }}</div>
          </section>

          <section v-if="store.currentTask.evaluationDimensions?.length" class="problem-section">
            <h2 class="problem-section__title">评分维度</h2>
            <BaseTable
              :data="store.currentTask.evaluationDimensions as unknown as Record<string, unknown>[]"
              class="problem-table"
            >
              <el-table-column prop="dimensionName" label="维度" min-width="100" />
              <el-table-column prop="weight" label="权重" width="70" align="center">
                <template #default="{ row }">{{ row.weight }}%</template>
              </el-table-column>
              <el-table-column prop="maxScore" label="满分" width="60" align="center" />
            </BaseTable>
          </section>

          <section v-if="history.length" class="problem-section">
            <h2 class="problem-section__title">提交记录</h2>
            <div class="history-list">
              <div v-for="item in history" :key="item.submissionId" class="history-item">
                <span class="history-item__version">v{{ item.version }}</span>
                <span class="history-item__time">{{ formatDate(item.submittedAt) }}</span>
                <TaskStatusBadge :status="item.status" />
                <span v-if="item.aiScore != null" class="history-item__score">{{ item.aiScore }}</span>
              </div>
            </div>
          </section>
        </aside>

        <!-- Right: submit workspace -->
        <main class="workspace-panel">
          <div v-if="allowedModes.length > 1" class="workspace-panel__head">
            <div class="submit-tabs" role="tablist">
              <button
                v-if="allowedModes.includes('file')"
                type="button"
                role="tab"
                :class="['submit-tab', { 'submit-tab--active': currentMode === 'file' }]"
                :aria-selected="currentMode === 'file'"
                @click="selectMode('file')"
              >
                <Upload :size="16" />
                文件上传
              </button>
              <button
                v-if="allowedModes.includes('code')"
                type="button"
                role="tab"
                :class="['submit-tab', { 'submit-tab--active': currentMode === 'code' }]"
                :aria-selected="currentMode === 'code'"
                @click="selectMode('code')"
              >
                <Code2 :size="16" />
                在线代码
              </button>
            </div>
          </div>

          <div class="workspace-panel__body">
            <div
              v-if="!canEditSubmit"
              class="workspace-notice workspace-notice--muted"
            >
              当前状态不可再次提交，可在左侧查看题目要求。
            </div>
            <div
              v-else-if="['AI_READY', 'AI_COMPLETED'].includes(store.currentTask.mySubmissionStatus)"
              class="workspace-notice"
            >
              已有 AI 参考评分，可先查看结果再决定是否提交教师审核。
              <BaseButton size="small" type="primary" @click="viewLatestAI">查看 AI 评分</BaseButton>
            </div>

            <!-- File Upload -->
            <div
              v-show="currentMode === 'file'"
              class="submit-pane submit-pane--file"
              @dragover="onDragOver"
              @dragleave="onDragLeave"
              @drop="onDrop"
            >
              <div
                v-if="!selectedFile"
                :class="['upload-zone', { 'upload-zone--dragging': isDragging, 'upload-zone--disabled': !canEditSubmit }]"
                @click="triggerFileInput"
              >
                <div class="upload-zone__icon">
                  <Upload :size="28" />
                </div>
                <div class="upload-zone__text">点击或拖拽文件到此处</div>
                <div class="upload-zone__hint">支持 .zip .doc .docx .pdf .md .png .jpg .gif · 可 Ctrl+V 粘贴图片 · 最大 50MB</div>
                <input
                  ref="fileInput"
                  type="file"
                  accept=".zip,.doc,.docx,.pdf,.md,.markdown,.png,.jpg,.jpeg,.gif"
                  hidden
                  :disabled="!canEditSubmit"
                  @change="onFileInputChange"
                >
              </div>
              <template v-else>
                <input
                  ref="fileInput"
                  type="file"
                  accept=".zip,.doc,.docx,.pdf,.md,.markdown,.png,.jpg,.jpeg,.gif"
                  hidden
                  :disabled="!canEditSubmit"
                  @change="onFileInputChange"
                >
                <LocalFilePreview
                  :file="selectedFile"
                  :editable="canEditSubmit"
                  @replace="triggerFileInput"
                  @remove="removeFile"
                />
              </template>
            </div>

            <!-- Online Code -->
            <div v-show="currentMode === 'code'" class="submit-pane submit-pane--code">
              <CodeEditor v-model="codeContent" v-model:language="codeLang" :disabled="!canEditSubmit" />
            </div>
          </div>

          <footer class="workspace-panel__footer">
            <textarea
              v-model="remark"
              class="remark-textarea"
              placeholder="提交备注（可选）"
              rows="2"
              :disabled="submitting || !canEditSubmit"
            />
            <div class="workspace-actions">
              <p class="actions-hint">提交后将进行 AI 参考评分，查看结果后可选择是否递交教师审核。</p>
              <div class="actions-row">
                <BaseButton
                  type="primary"
                  size="large"
                  :loading="submitting"
                  :disabled="!canEditSubmit"
                  @click="handleSubmit"
                >
                  提交
                </BaseButton>
                <BaseButton size="large" :disabled="submitting" @click="goBack">返回列表</BaseButton>
              </div>
            </div>
          </footer>
        </main>
      </div>
    </template>
  </div>
</template>

<style scoped>
.submit-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  min-height: 640px;
  padding: 16px 24px 24px;
  box-sizing: border-box;
  background: var(--color-bg, #F5F7FA);
}

.mono {
  font-family: "JetBrains Mono", "SF Mono", monospace;
}

/* --- Toolbar --- */
.submit-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.submit-toolbar__back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 1px solid var(--color-border, #E5E7EB);
  background: var(--color-card, #fff);
  color: var(--color-text-secondary, #64748b);
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: color 150ms, border-color 150ms, background 150ms;
  flex-shrink: 0;
}

.submit-toolbar__back:hover {
  color: var(--color-primary, #1677FF);
  border-color: var(--color-primary-mid, #91CAFF);
  background: var(--color-primary-light, #E6F4FF);
}

.submit-toolbar__title {
  flex: 1;
  min-width: 0;
}

.submit-toolbar__title h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--color-navy, #0B3A5C);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.submit-toolbar__title p {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--color-text-secondary, #64748b);
}

/* --- Workspace --- */
.submit-workspace {
  flex: 1;
  display: grid;
  grid-template-columns: minmax(340px, 42%) minmax(460px, 58%);
  min-height: 0;
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: var(--radius-xl, 16px);
  overflow: hidden;
  background: var(--color-card, #fff);
  box-shadow: var(--shadow-card);
}

/* --- Left panel --- */
.problem-panel {
  border-right: 1px solid var(--color-border, #E5E7EB);
  overflow-y: auto;
  padding: 20px 24px;
  background: var(--color-card, #fff);
}

.problem-section + .problem-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--color-border, #E5E7EB);
}

.problem-section__title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 700;
  color: var(--color-navy, #0B3A5C);
}

.problem-meta {
  margin: 0;
  display: grid;
  gap: 10px;
}

.problem-meta__row {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 8px;
  font-size: 13px;
}

.problem-meta__row dt {
  margin: 0;
  color: var(--color-text-secondary, #64748b);
}

.problem-meta__row dd {
  margin: 0;
  color: var(--color-text-primary, #1e293b);
  font-weight: 500;
}

.problem-body {
  padding: 12px 14px;
  border-radius: 8px;
  background: var(--color-bg, #F5F7FA);
  font-size: 14px;
  line-height: 1.65;
  color: var(--color-text-primary, #1e293b);
  white-space: pre-wrap;
}

.problem-table {
  font-size: 13px;
}

/* --- Right panel --- */
.workspace-panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
  background: var(--color-bg, #F5F7FA);
}

.workspace-panel__head {
  flex-shrink: 0;
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border, #E5E7EB);
  background: var(--color-card, #fff);
}

.workspace-panel__body {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.workspace-panel__footer {
  flex-shrink: 0;
  padding: 12px 16px 16px;
  border-top: 1px solid var(--color-border, #E5E7EB);
  background: var(--color-card, #fff);
}

.workspace-notice {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  padding: 10px 14px;
  border-radius: 8px;
  background: var(--color-primary-light, #E6F4FF);
  border: 1px solid var(--color-primary-mid, #91CAFF);
  font-size: 13px;
  color: var(--color-text-primary, #1e293b);
}

.workspace-notice--muted {
  background: var(--color-bg, #F5F7FA);
  border-color: var(--color-border, #E5E7EB);
  color: var(--color-text-secondary, #64748b);
}

.submit-tabs {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px;
  background: var(--color-bg, #F5F7FA);
  border-radius: var(--radius-md, 8px);
}

.submit-tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: none;
  background: transparent;
  padding: 8px 14px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary, #64748b);
  cursor: pointer;
  transition: color 150ms, background 150ms, box-shadow 150ms;
}

.submit-tab:hover {
  color: var(--color-text-primary, #1e293b);
}

.submit-tab--active {
  background: var(--color-card, #fff);
  color: var(--color-primary, #1677FF);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.08);
  font-weight: 600;
}

.submit-pane {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.submit-pane--file {
  gap: 12px;
}

.submit-pane--code {
  height: 100%;
}

.submit-pane--code :deep(.code-editor-wrapper) {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 320px;
  border-radius: 8px;
}

.submit-pane--code :deep(.ce-body) {
  flex: 1;
  min-height: 0;
  max-height: none;
}

/* --- Upload --- */
.upload-zone {
  border: 1px dashed var(--color-border, #CBD5E1);
  border-radius: var(--radius-lg, 12px);
  padding: 48px 24px;
  text-align: center;
  cursor: pointer;
  transition: border-color 150ms, background 150ms;
  background: var(--color-card, #fff);
}

.upload-zone:hover,
.upload-zone--dragging {
  border-color: var(--color-primary, #1677FF);
  background: var(--color-primary-light, #E6F4FF);
}

.upload-zone--filled {
  border-style: solid;
  border-color: var(--color-primary-mid, #91CAFF);
}

.upload-zone--disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.upload-zone__icon {
  display: flex;
  justify-content: center;
  margin-bottom: 10px;
  color: var(--color-primary, #1677FF);
}

.upload-zone__text {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary, #1e293b);
}

.upload-zone__hint {
  font-size: 12px;
  color: var(--color-text-placeholder, #94a3b8);
  margin-top: 6px;
}

/* --- Git --- */
.git-panel { width: 100%; }

.git-row {
  display: flex;
  gap: var(--spacing-sm, 8px);
}

.git-row > :first-child { flex: 1; }

.git-verify-result {
  margin-top: var(--spacing-md, 16px);
  padding: 10px 14px;
  background: #E6F7EF;
  border: 1px solid #00B96B;
  border-radius: var(--radius-sm, 4px);
  font-size: 13px;
  color: #009A57;
}

.form-group { margin-bottom: var(--spacing-md, 16px); }

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary, #64748b);
  margin-bottom: 6px;
}

/* --- Footer --- */
.remark-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--color-border, #e2e8f0);
  border-radius: var(--radius-sm, 4px);
  font-size: 13px;
  font-family: inherit;
  color: var(--color-text-primary, #1e293b);
  background: var(--color-bg, #F5F7FA);
  resize: vertical;
  outline: none;
  box-sizing: border-box;
  margin-bottom: 10px;
}

.remark-textarea:focus { border-color: var(--color-primary, #1677FF); }
.remark-textarea::placeholder { color: var(--color-text-placeholder, #94a3b8); }
.remark-textarea:disabled { opacity: 0.7; }

.workspace-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.actions-row {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.actions-hint {
  margin: 0;
  font-size: 12px;
  color: var(--color-text-secondary, #64748b);
  line-height: 1.5;
  max-width: 420px;
}

/* --- History --- */
.history-list {
  display: flex;
  flex-direction: column;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid var(--color-border, #e2e8f0);
  font-size: 12px;
}

.history-item:last-child { border-bottom: none; }

.history-item__version {
  font-family: "JetBrains Mono", "SF Mono", monospace;
  color: var(--color-text-placeholder, #94a3b8);
  width: 28px;
}

.history-item__time {
  flex: 1;
  color: var(--color-text-secondary, #64748b);
  font-family: "JetBrains Mono", "SF Mono", monospace;
}

.history-item__score {
  font-family: "JetBrains Mono", "SF Mono", monospace;
  font-weight: 600;
  color: var(--color-primary, #1677FF);
}

@media (max-width: 1023.98px) {
  .submit-page {
    height: auto;
    min-height: 0;
    padding: 12px 16px 24px;
  }

  .submit-workspace {
    grid-template-columns: 1fr;
    overflow: visible;
  }

  .problem-panel {
    border-right: none;
    border-bottom: 1px solid var(--color-border, #E5E7EB);
    max-height: none;
  }

  .workspace-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .actions-row {
    flex-wrap: wrap;
  }

  .actions-hint {
    max-width: none;
  }
}

@media (max-width: 767.98px) {
  .submit-toolbar {
    flex-wrap: wrap;
    gap: 10px;
  }

  .submit-toolbar__title h1 {
    font-size: 17px;
    white-space: normal;
  }

  .actions-row :deep(.base-button),
  .actions-row :deep(button) {
    flex: 1;
    min-width: calc(50% - 5px);
  }
}
</style>
