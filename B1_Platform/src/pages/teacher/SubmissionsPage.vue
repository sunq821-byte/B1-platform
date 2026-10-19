<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { FileText, Download, Package, FileWarning, Inbox, Code2, GitBranch, ArrowLeft } from "lucide-vue-next"
import { useTeacherStore } from "@/stores/useTeacherStore"
import { downloadFile } from "@/api/download"
import * as teacherApi from "@/api/modules/teacher"
import {
  detectPreviewKind,
  loadTextContent,
  loadWordHtml,
  pickPreviewableIndex,
  resolvePreviewUrl,
  fetchFileBlob,
  type PreviewKind,
} from "@/utils/filePreview"
import { renderMarkdownHtml } from "@/utils/markdown"
import { inferCodeLanguage } from "@/utils/codeLanguage"
import {
  isGitSubmit,
  isOnlineCodeSubmit,
  submitTypeLabel,
  submitTypeTagVariant,
} from "@/utils/submitTypeLabel"
import {
  computeTeacherFinalScore,
  formatManualAdjustment,
  scoreLevel,
  scoreLevelLabel,
} from "@/utils/teacherScoring"
import type { IAIDeduction, IManualDeduction, IAttachment, IPendingSubmission, IPublishRequest, ITeacherTaskItem, ITaskDetail, ITaskStudentProgress } from "@/types/teacher"
import BaseButton from "@/components/base/BaseButton.vue"
import CodeEditor from "@/components/business/CodeEditor.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"
import { useBreakpoint } from "@/composables/useBreakpoint"

const store = useTeacherStore()
const { isMdDown } = useBreakpoint()
const mobilePane = ref<"list" | "work">("list")
const mobileWorkTab = ref<"preview" | "score">("preview")
const loadError = ref("")
const listKeyword = ref("")
const courseFilter = ref("all")
const taskFilter = ref("all")
const currentIndex = ref(-1)
const teacherComment = ref("")
const manualDeductions = ref<IManualDeduction[]>([])
const manualPoints = ref(0)
const manualReason = ref("")
const showAdjust = ref(false)
const adjustIdx = ref(-1)
const adjPoints = ref(0)
const adjReason = ref("")
const previewLoading = ref(false)
const previewLoadToken = ref(0)
const activeSubmissionId = ref("")
const previewAttachments = ref<IAttachment[]>([])
const submissionSummary = ref("")
const textPreviewContent = ref("")
const markdownHtml = ref("")
const wordPreviewHtml = ref("")
const previewBlobUrl = ref("")
const previewKind = ref<PreviewKind>("unknown")
const previewError = ref("")
const emptyStateKind = ref<"none" | "no-attachment" | "incomplete">("none")
const selectedAttachmentIndex = ref(0)
const activeSubmitType = ref("")
const gitPreviewUrl = ref("")
const gitPreviewBranch = ref("")
const onlineCodeContent = ref("")
const onlineCodeLang = ref("java")

const showAiRestore = ref(false)
const restoreTasks = ref<ITeacherTaskItem[]>([])
const restoreTaskId = ref("")
const restoreTaskDetail = ref<ITaskDetail | null>(null)
const restoreLoading = ref(false)
const restoreSubmitting = ref<string | null>(null)

const TRIAL_STATUSES = new Set(["DRAFT", "ANALYZING", "AI_READY", "REJECTED"])

const restoreCandidates = computed(() => {
  const students = restoreTaskDetail.value?.students ?? []
  return students.filter((s) => TRIAL_STATUSES.has(s.latestStatus))
})

function statusLabel(status: string) {
  const map: Record<string, string> = {
    DRAFT: "草稿",
    ANALYZING: "AI 分析中",
    AI_READY: "待确认",
    REJECTED: "已驳回",
    CONFIRMED: "待审核",
    PUBLISHED: "已发布",
    INCOMPLETE: "未完成",
    NOT_SUBMITTED: "未提交",
  }
  return map[status] ?? status
}

function canRestoreAiCount(row: ITaskStudentProgress) {
  return TRIAL_STATUSES.has(row.latestStatus) && row.aiAnalyzeUsedCount > 0
}

async function openAiRestoreDialog() {
  showAiRestore.value = true
  restoreTaskId.value = ""
  restoreTaskDetail.value = null
  restoreLoading.value = true
  try {
    await store.fetchTasks()
    restoreTasks.value = store.tasks
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || "加载任务列表失败")
  } finally {
    restoreLoading.value = false
  }
}

async function loadRestoreTaskDetail(taskId: string) {
  if (!taskId) {
    restoreTaskDetail.value = null
    return
  }
  restoreLoading.value = true
  try {
    restoreTaskDetail.value = await teacherApi.fetchTaskDetail(taskId)
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || "加载任务详情失败")
    restoreTaskDetail.value = null
  } finally {
    restoreLoading.value = false
  }
}

async function handleRestoreAiCount(row: ITaskStudentProgress) {
  if (!restoreTaskId.value || !canRestoreAiCount(row)) return
  try {
    await ElMessageBox.confirm(
      `将 ${row.realName} 的 AI 分析次数还原为 0（已用 ${row.aiAnalyzeUsedCount}/${row.maxAiAnalyzeCount}）。\n不会驳回提交，学生可继续试交。`,
      "还原 AI 次数",
      { type: "warning", confirmButtonText: "确认还原", cancelButtonText: "取消" },
    )
  } catch { return }

  restoreSubmitting.value = row.userId
  try {
    await teacherApi.restoreAiAnalyzeCount(restoreTaskId.value, row.userId)
    ElMessage.success(`${row.realName} 的 AI 次数已还原`)
    await loadRestoreTaskDetail(restoreTaskId.value)
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || "还原失败")
  } finally {
    restoreSubmitting.value = null
  }
}

const currentSub = computed(() => {
  if (currentIndex.value < 0 || currentIndex.value >= filteredPending.value.length) return null
  return filteredPending.value[currentIndex.value]
})

interface ReviewFilterOption {
  id: string
  label: string
  count: number
}

function buildCourseOptions(list: IPendingSubmission[]): ReviewFilterOption[] {
  const map = new Map<string, { name: string; count: number }>()
  for (const s of list) {
    const id = s.courseId || "__unknown__"
    const name = s.courseName
      || store.courses.find((c) => c.courseId === s.courseId)?.courseName
      || "未分类"
    const prev = map.get(id)
    if (prev) prev.count += 1
    else map.set(id, { name, count: 1 })
  }
  return Array.from(map.entries())
    .map(([id, { name, count }]) => ({
      id,
      label: `${name} (${count})`,
      count,
    }))
    .sort((a, b) => a.label.localeCompare(b.label, "zh-CN"))
}

function buildTaskOptions(list: IPendingSubmission[]): ReviewFilterOption[] {
  const map = new Map<string, { name: string; count: number }>()
  for (const s of list) {
    const id = s.taskId || "__unknown__"
    const name = s.taskName || "未命名任务"
    const prev = map.get(id)
    if (prev) prev.count += 1
    else map.set(id, { name, count: 1 })
  }
  return Array.from(map.entries())
    .map(([id, { name, count }]) => ({
      id,
      label: `${name} (${count})`,
      count,
    }))
    .sort((a, b) => a.label.localeCompare(b.label, "zh-CN"))
}

const courseOptions = computed(() => buildCourseOptions(store.pendingSubmissions))

const courseScopedSubmissions = computed(() => {
  if (courseFilter.value === "all") return store.pendingSubmissions
  return store.pendingSubmissions.filter((s) => s.courseId === courseFilter.value)
})

const taskOptions = computed(() => buildTaskOptions(courseScopedSubmissions.value))

const filteredPending = computed(() => {
  let list = store.pendingSubmissions
  if (courseFilter.value !== "all") {
    list = list.filter((s) => s.courseId === courseFilter.value)
  }
  if (taskFilter.value !== "all") {
    list = list.filter((s) => s.taskId === taskFilter.value)
  }
  const kw = listKeyword.value.trim().toLowerCase()
  if (!kw) return list
  return list.filter((s) =>
    (s.studentName || "").toLowerCase().includes(kw)
    || (s.taskName || "").toLowerCase().includes(kw)
    || (s.courseName || "").toLowerCase().includes(kw),
  )
})

const quickTags = ["代码规范良好", "功能实现完整", "文档需完善", "创新性强", "答辩表现优秀", "需要继续优化"]

function buildDeductionOverrides(): IPublishRequest["deductionOverrides"] {
  const overrides: IPublishRequest["deductionOverrides"] = {}
  const list = store.currentDiagnosis?.deductions ?? []
  list.forEach((d, idx) => {
    if (!d.overridden) return
    overrides[idx] = {
      action: d.overrideAction || "adjust",
      adjustedDeduct: d.adjustedDeduct,
      reason: d.overrideReason,
    }
  })
  return overrides
}

const attachments = computed<IAttachment[]>(() => {
  if (!currentSub.value || activeSubmissionId.value !== currentSub.value.submissionId) return []
  return previewAttachments.value
})

const currentAttachment = computed(() => attachments.value[selectedAttachmentIndex.value] ?? null)

const showPreviewPanel = computed(() =>
  Boolean(currentSub.value)
  && activeSubmissionId.value === currentSub.value?.submissionId
  && !previewLoading.value,
)

const isOnlineCodeView = computed(() => isOnlineCodeSubmit(activeSubmitType.value))
const isGitView = computed(() => isGitSubmit(activeSubmitType.value))

function tabIconKind(att: IAttachment): PreviewKind {
  return detectPreviewKind(att.fileName, att.fileType)
}

function resetPreviewState() {
  revokePreviewUrls()
  previewAttachments.value = []
  activeSubmissionId.value = ""
  activeSubmitType.value = ""
  submissionSummary.value = ""
  gitPreviewUrl.value = ""
  gitPreviewBranch.value = ""
  onlineCodeContent.value = ""
  onlineCodeLang.value = "java"
  textPreviewContent.value = ""
  markdownHtml.value = ""
  wordPreviewHtml.value = ""
  previewError.value = ""
  previewKind.value = "unknown"
  emptyStateKind.value = "none"
  selectedAttachmentIndex.value = 0
}

async function loadPdfPreview(url: string) {
  const blob = await fetchFileBlob(url)
  const mime = blob.type.toLowerCase()
  if (mime.includes("pdf")) {
    previewBlobUrl.value = URL.createObjectURL(blob)
    previewKind.value = "pdf"
    return
  }
  if (mime.includes("markdown") || mime.startsWith("text/") || mime === "application/octet-stream") {
    const text = await blob.text()
    if (text.trimStart().startsWith("#") || text.includes("\n## ") || mime.includes("markdown")) {
      markdownHtml.value = renderMarkdownHtml(text)
      previewKind.value = "markdown"
      return
    }
  }
  previewBlobUrl.value = URL.createObjectURL(blob)
  previewKind.value = "pdf"
}

function buildEffectiveManualAdjustments(forPreview = false): IManualDeduction[] {
  const list = [...manualDeductions.value]
  const pts = manualPoints.value
  if (!pts || Number.isNaN(pts)) return list
  const reason = manualReason.value.trim()
  if (forPreview || reason) {
    list.push({ points: pts, reason: reason || "（待确认）" })
  }
  return list
}

function patchDeduction(idx: number, patch: Partial<IAIDeduction>) {
  if (!store.currentDiagnosis) return
  const deductions = store.currentDiagnosis.deductions.map((d, i) =>
    i === idx ? { ...d, ...patch } : d,
  )
  store.currentDiagnosis = { ...store.currentDiagnosis, deductions }
}

const effectiveDeductions = computed(() => {
  const base = store.currentDiagnosis?.deductions ?? []
  if (!showAdjust.value || adjustIdx.value < 0) return base
  return base.map((d, i) => {
    if (i !== adjustIdx.value) return d
    return {
      ...d,
      overridden: true,
      overrideAction: "adjust" as const,
      adjustedDeduct: adjPoints.value,
    }
  })
})

const finalScore = computed(() => {
  if (!store.currentDiagnosis) return 100
  return computeTeacherFinalScore(
    store.currentDiagnosis.aiScore,
    effectiveDeductions.value,
    buildEffectiveManualAdjustments(true),
  )
})

const aiScoreRingClass = computed(() => {
  const s = store.currentDiagnosis?.aiScore ?? 0
  return `ring-${scoreLevel(s)}`
})

const finalScoreRingClass = computed(() => `ring-${scoreLevel(finalScore.value)}`)

const gradeLabel = computed(() => scoreLevelLabel(finalScore.value))

function formatFileSize(bytes?: number) {
  if (!bytes) return "-"
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function mapAttachments(raw: Array<Record<string, unknown>>): IAttachment[] {
  return raw.map((a) => {
    const downloadUrl = String(a.downloadUrl ?? "")
    const previewUrl = String(a.previewUrl ?? resolvePreviewUrl(undefined, downloadUrl))
    return {
      fileId: String(a.fileId ?? ""),
      fileName: String(a.fileName ?? ""),
      fileSize: Number(a.fileSize ?? 0),
      fileType: String(a.fileType ?? ""),
      previewUrl,
      downloadUrl,
    }
  })
}

function revokePreviewUrls() {
  if (previewBlobUrl.value) {
    URL.revokeObjectURL(previewBlobUrl.value)
    previewBlobUrl.value = ""
  }
}

async function loadAttachmentPreview(att: IAttachment) {
  revokePreviewUrls()
  previewError.value = ""
  textPreviewContent.value = ""
  markdownHtml.value = ""
  wordPreviewHtml.value = ""
  emptyStateKind.value = "none"

  const url = resolvePreviewUrl(att.previewUrl, att.downloadUrl)
  if (!url) {
    previewError.value = "文件链接不可用，请稍后重试"
    previewKind.value = "unknown"
    return
  }

  const kind = detectPreviewKind(att.fileName, att.fileType)
  previewKind.value = kind

  try {
    switch (kind) {
      case "image":
      case "pdf": {
        if (kind === "pdf") {
          await loadPdfPreview(url)
        } else {
          const blob = await fetchFileBlob(url)
          previewBlobUrl.value = URL.createObjectURL(blob)
        }
        break
      }
      case "markdown": {
        const md = await loadTextContent(url)
        markdownHtml.value = renderMarkdownHtml(md)
        break
      }
      case "text":
        textPreviewContent.value = await loadTextContent(url)
        break
      case "word":
        wordPreviewHtml.value = await loadWordHtml(url, att.fileName)
        break
      case "archive":
      case "unknown":
        break
    }
  } catch (e: unknown) {
    previewError.value = (e as Error)?.message || "预览加载失败"
    previewKind.value = "unknown"
  }
}

async function loadSubmissionPreview(sub: IPendingSubmission) {
  const token = ++previewLoadToken.value
  resetPreviewState()
  previewLoading.value = true

  try {
    const detail = await teacherApi.fetchSubmissionDetail(sub.submissionId) as unknown as {
      attachments?: Array<Record<string, unknown>>
      summary?: string
      submitType?: string
      status?: string
      gitUrl?: string
      gitBranch?: string
    }
    if (token !== previewLoadToken.value) return

    activeSubmissionId.value = sub.submissionId
    submissionSummary.value = detail.summary ?? ""
    const submitType = detail.submitType ?? sub.submitType ?? ""
    activeSubmitType.value = submitType
    gitPreviewUrl.value = detail.gitUrl ?? ""
    gitPreviewBranch.value = detail.gitBranch ?? "main"
    const mapped = mapAttachments(detail.attachments ?? [])
    previewAttachments.value = mapped

    const idx2 = store.pendingSubmissions.findIndex((s) => s.submissionId === sub.submissionId)
    if (idx2 >= 0) {
      store.pendingSubmissions[idx2].attachments = mapped
      if (detail.submitType) {
        store.pendingSubmissions[idx2].submitType = detail.submitType
        const st = detail.submitType.toUpperCase()
        store.pendingSubmissions[idx2].submissionType =
          st === "ONLINE_CODE" ? "code" : st === "GIT_URL" ? "git" : "file"
      }
    }

    const status = (detail.status ?? sub.status ?? "").toUpperCase()

    if (isOnlineCodeSubmit(submitType)) {
      if (!mapped.length) {
        emptyStateKind.value = status === "INCOMPLETE" ? "incomplete" : "no-attachment"
        return
      }
      try {
        const att = mapped[0]
        const url = resolvePreviewUrl(att.previewUrl, att.downloadUrl)
        onlineCodeContent.value = await loadTextContent(url)
        onlineCodeLang.value = inferCodeLanguage(onlineCodeContent.value)
      } catch (e: unknown) {
        previewError.value = (e as Error)?.message || "代码内容加载失败"
      }
      return
    }

    if (isGitSubmit(submitType)) {
      return
    }

    if (!mapped.length) {
      emptyStateKind.value = status === "INCOMPLETE" ? "incomplete" : "no-attachment"
      return
    }

    selectedAttachmentIndex.value = pickPreviewableIndex(mapped)
    await loadAttachmentPreview(mapped[selectedAttachmentIndex.value])
  } catch (e: unknown) {
    if (token !== previewLoadToken.value) return
    previewError.value = (e as Error)?.message || "加载提交内容失败"
  } finally {
    if (token === previewLoadToken.value) previewLoading.value = false
  }
}

async function selectAttachment(idx: number) {
  selectedAttachmentIndex.value = idx
  const att = attachments.value[idx]
  if (!att) return
  previewLoading.value = true
  try {
    await loadAttachmentPreview(att)
  } finally {
    previewLoading.value = false
  }
}

onBeforeUnmount(() => {
  revokePreviewUrls()
})

async function selectSubmission(idx: number) {
  currentIndex.value = idx
  const sub = filteredPending.value[idx]
  if (!sub) return
  teacherComment.value = ""
  manualDeductions.value = []
  store.currentDiagnosis = null
  mobileWorkTab.value = "preview"
  if (isMdDown.value) mobilePane.value = "work"

  await loadSubmissionPreview(sub)

  try { await store.fetchAIDiagnosis(sub.submissionId) }
  catch { /* 右侧可显示暂无 AI 诊断 */ }
}

function backToMobileList() {
  mobilePane.value = "list"
}

function nextSubmission() {
  if (currentIndex.value < filteredPending.value.length - 1) {
    selectSubmission(currentIndex.value + 1)
  }
}

function prevSubmission() {
  if (currentIndex.value > 0) selectSubmission(currentIndex.value - 1)
}

async function handleDownload(attachment: IAttachment) {
  if (!attachment.downloadUrl) return
  try {
    await downloadFile(attachment.downloadUrl, undefined, attachment.fileName)
  } catch { /* downloadFile 已提示 */ }
}

function insertTag(tag: string) {
  teacherComment.value = teacherComment.value + (teacherComment.value ? "；" : "") + tag
}

function addManual() {
  if (!manualPoints.value || Number.isNaN(manualPoints.value)) {
    ElMessage.warning("请填写非零分值（负数扣分，正数加分）")
    return
  }
  if (!manualReason.value.trim()) { ElMessage.warning("请填写原因"); return }
  manualDeductions.value.push({ points: manualPoints.value, reason: manualReason.value.trim() })
  manualPoints.value = 0; manualReason.value = ""
}

function removeManual(idx: number) { manualDeductions.value.splice(idx, 1) }

function acceptDeduction(idx: number) {
  patchDeduction(idx, { overridden: true, overrideAction: "accept" })
}

function rejectDeduction(idx: number) {
  patchDeduction(idx, { overridden: true, overrideAction: "reject" })
}

function openAdjust(idx: number) {
  if (!store.currentDiagnosis) return
  const d = store.currentDiagnosis.deductions[idx]
  adjustIdx.value = idx; adjPoints.value = d.adjustedDeduct || d.suggestDeduct
  adjReason.value = d.overrideReason || ""; showAdjust.value = true
}

function confirmAdjust() {
  if (!store.currentDiagnosis) return
  if (!adjReason.value.trim()) { ElMessage.warning("请填写调整理由"); return }
  patchDeduction(adjustIdx.value, {
    overridden: true,
    overrideAction: "adjust",
    adjustedDeduct: adjPoints.value,
    overrideReason: adjReason.value,
  })
  showAdjust.value = false
  ElMessage.success("扣分已调整")
}

async function handlePublish() {
  if (!currentSub.value) return
  if (manualPoints.value && !Number.isNaN(manualPoints.value) && !manualReason.value.trim()) {
    ElMessage.warning("手动调整已填分值，请填写原因或清空分值")
    return
  }
  const final = parseFloat(finalScore.value.toFixed(1))
  const comment = teacherComment.value.trim()
  try {
    await ElMessageBox.confirm(
      `确认发布终审成绩？\n总分: ${final} 分\n评语: ${comment || "无"}\n此操作不可逆。`,
      "发布成绩",
      { type: "warning", confirmButtonText: "确认发布", cancelButtonText: "取消" },
    )
  } catch { return }
  try {
    const adjustments = buildEffectiveManualAdjustments()
    await store.publishReview(currentSub.value.submissionId, {
      status: "PUBLISHED",
      finalScore: final, comment,
      manualDeductions: adjustments,
      deductionOverrides: buildDeductionOverrides(),
    })
    ElMessage.success(`发布成功！总分 ${final} 分`)
    if (filteredPending.value.length > 0) {
      currentIndex.value = Math.min(currentIndex.value, filteredPending.value.length - 1)
      if (currentIndex.value >= 0) {
        teacherComment.value = ""
        manualDeductions.value = []
        manualPoints.value = 0
        manualReason.value = ""
        await selectSubmission(currentIndex.value)
      }
    } else { currentIndex.value = -1 }
  } catch (e: unknown) { ElMessage.error((e as Error)?.message || "发布失败") }
}

async function handleReject() {
  if (!currentSub.value) return
  const comment = teacherComment.value.trim()
  if (!comment) { ElMessage.warning("打回时请填写打回原因"); return }
  try {
    await ElMessageBox.confirm(
      `确认打回该提交？\n原因: ${comment}\n此操作不可逆。`,
      "打回提交",
      { type: "warning", confirmButtonText: "确认打回", cancelButtonText: "取消" },
    )
  } catch { return }
  try {
    await store.publishReview(currentSub.value.submissionId, {
      status: "REJECTED",
      finalScore: 0, comment,
      manualDeductions: [],
      deductionOverrides: buildDeductionOverrides(),
    })
    ElMessage.warning("已打回")
    if (filteredPending.value.length > 0) {
      currentIndex.value = Math.min(currentIndex.value, filteredPending.value.length - 1)
      if (currentIndex.value >= 0) {
        teacherComment.value = ""; manualDeductions.value = []
        await selectSubmission(currentIndex.value)
      }
    } else { currentIndex.value = -1 }
  } catch (e: unknown) { ElMessage.error((e as Error)?.message || "操作失败") }
}

async function initPage() {
  loadError.value = ""
  try {
    await Promise.all([
      store.fetchPendingSubmissions(),
      store.fetchCourses(),
    ])
    if (filteredPending.value.length > 0 && currentIndex.value < 0) {
      await selectSubmission(0)
    }
  }
  catch (e: unknown) { loadError.value = (e as Error)?.message || "加载失败" }
}

async function reselectAfterFilterChange() {
  currentIndex.value = -1
  if (filteredPending.value.length > 0) {
    await selectSubmission(0)
  }
}

watch(courseFilter, async () => {
  taskFilter.value = "all"
  await reselectAfterFilterChange()
})

watch(taskFilter, reselectAfterFilterChange)

watch(courseOptions, (options) => {
  if (courseFilter.value !== "all" && !options.some((o) => o.id === courseFilter.value)) {
    courseFilter.value = "all"
    taskFilter.value = "all"
  }
})

watch(taskOptions, (options) => {
  if (taskFilter.value !== "all" && !options.some((o) => o.id === taskFilter.value)) {
    taskFilter.value = "all"
  }
})

onMounted(() => { initPage() })
</script>

<template>
  <div class="review-root">
    <LoadingState v-if="store.submissionsLoading" text="加载待审核列表..." />
    <ErrorState v-else-if="loadError" :message="loadError" @retry="initPage" />

    <template v-else>
      <!-- Three-panel area -->
      <div
        class="review-wb"
        :class="{
          'review-wb--mobile-list': isMdDown && mobilePane === 'list',
          'review-wb--mobile-work': isMdDown && mobilePane === 'work',
        }"
      >
        <!-- LEFT: Student list -->
        <div class="review-left">
          <div class="panel-title">待审核提交 ({{ filteredPending.length }})</div>
          <select v-model="courseFilter" class="list-course-select">
            <option value="all">全部课程 ({{ store.pendingSubmissions.length }})</option>
            <option v-for="c in courseOptions" :key="c.id" :value="c.id">
              {{ c.label }}
            </option>
          </select>
          <select v-model="taskFilter" class="list-course-select">
            <option value="all">全部任务 ({{ courseScopedSubmissions.length }})</option>
            <option v-for="t in taskOptions" :key="t.id" :value="t.id">
              {{ t.label }}
            </option>
          </select>
          <input
            v-model="listKeyword"
            class="list-search"
            placeholder="搜索学生 / 任务..."
            @input="currentIndex = -1"
          />
          <div class="student-list">
            <div
              v-for="(sub, idx) in filteredPending"
              :key="sub.submissionId"
              :class="['student-item', { 'student-item--active': idx === currentIndex }]"
              @click="selectSubmission(idx)"
            >
              <div class="si-name">{{ sub.studentName }}</div>
              <div v-if="courseFilter === 'all' && sub.courseName" class="si-course">{{ sub.courseName }}</div>
              <div class="si-task">{{ sub.taskName }}</div>
              <div class="si-meta">
                <span :class="['si-type-tag', `si-type-tag--${submitTypeTagVariant(sub.submitType)}`]">
                  {{ submitTypeLabel(sub.submitType) }}
                </span>
                <span class="si-time">{{ sub.submittedAt?.substring(0, 16).replace('T', ' ') }}</span>
              </div>
            </div>
          </div>
          <div v-if="filteredPending.length === 0" class="empty-hint">暂无待审核提交</div>
        </div>

        <div v-if="isMdDown && mobilePane === 'work'" class="mobile-work-chrome">
          <button type="button" class="mobile-back-btn" @click="backToMobileList">
            <ArrowLeft :size="16" />
            返回列表
          </button>
          <div class="mobile-work-tabs" role="tablist">
            <button
              type="button"
              class="mobile-work-tab"
              :class="{ 'mobile-work-tab--active': mobileWorkTab === 'preview' }"
              @click="mobileWorkTab = 'preview'"
            >
              预览
            </button>
            <button
              type="button"
              class="mobile-work-tab"
              :class="{ 'mobile-work-tab--active': mobileWorkTab === 'score' }"
              @click="mobileWorkTab = 'score'"
            >
              评分
            </button>
          </div>
        </div>

        <!-- CENTER: Preview -->
        <div
          class="review-center"
          :class="{ 'review-center--mobile-show': isMdDown && mobilePane === 'work' && mobileWorkTab === 'preview' }"
        >
          <div v-if="currentSub" class="preview-header">
            <div>
              <div class="preview-header__title">{{ currentSub.taskName }} · {{ currentSub.studentName }}</div>
              <div v-if="submissionSummary" class="preview-header__summary">{{ submissionSummary }}</div>
            </div>
          </div>
          <div v-else class="panel-title">请选择学生</div>

          <div v-if="!currentSub" class="preview-empty">
            <Inbox :size="40" class="preview-empty__icon" />
            <p>从左侧选择一份提交开始审核</p>
          </div>

          <div v-else-if="previewLoading" class="preview-skeleton">
            <div class="preview-skeleton__bar" />
            <div class="preview-skeleton__body" />
          </div>

          <div v-else-if="showPreviewPanel" class="preview-panel">
            <div v-if="emptyStateKind === 'no-attachment'" class="preview-empty">
              <Inbox :size="40" class="preview-empty__icon" />
              <p class="preview-empty__title">尚未上传内容</p>
              <p class="preview-empty__desc">
                {{ isOnlineCodeView ? "学生尚未提交在线代码，请与学生核实" : "学生确认提交时未附带 ZIP 或报告文件，请与学生核实" }}
              </p>
            </div>

            <div v-else-if="emptyStateKind === 'incomplete'" class="preview-empty">
              <FileWarning :size="40" class="preview-empty__icon" />
              <p class="preview-empty__title">截止未完成提交</p>
              <p class="preview-empty__desc">该记录为截止前的草稿状态，内容可能不完整</p>
              <p v-if="submissionSummary" class="preview-empty__note">{{ submissionSummary }}</p>
            </div>

            <template v-else>
              <!-- 在线代码：与学生端一致的编辑器只读预览 -->
              <div v-if="isOnlineCodeView" class="online-code-preview">
                <div class="online-code-preview__tab">
                  <Code2 :size="14" />
                  <span>在线代码</span>
                </div>
                <div v-if="previewError" class="preview-warn online-code-preview__error">
                  <FileWarning :size="18" />
                  <span>{{ previewError }}</span>
                </div>
                <CodeEditor
                  v-else
                  v-model="onlineCodeContent"
                  v-model:language="onlineCodeLang"
                  disabled
                  fill-height
                />
              </div>

              <!-- Git 仓库提交 -->
              <div v-else-if="isGitView" class="git-preview">
                <div class="online-code-preview__tab online-code-preview__tab--git">
                  <GitBranch :size="14" />
                  <span>Git 仓库</span>
                </div>
                <div class="git-preview__body">
                  <div class="git-preview__row">
                    <span class="git-preview__label">仓库地址</span>
                    <a
                      v-if="gitPreviewUrl"
                      :href="gitPreviewUrl"
                      class="git-preview__link"
                      target="_blank"
                      rel="noopener noreferrer"
                    >{{ gitPreviewUrl }}</a>
                    <span v-else class="git-preview__empty">未填写</span>
                  </div>
                  <div class="git-preview__row">
                    <span class="git-preview__label">分支</span>
                    <span class="git-preview__value">{{ gitPreviewBranch || "main" }}</span>
                  </div>
                  <p class="git-preview__hint">请在 Git 平台打开仓库，对照任务要求与 AI 诊断进行审核。</p>
                </div>
              </div>

              <template v-else>
              <div v-if="attachments.length" class="attachment-tabs">
                <button
                  v-for="(att, i) in attachments"
                  :key="att.fileId || i"
                  type="button"
                  :class="['attachment-tab', { 'attachment-tab--active': i === selectedAttachmentIndex }]"
                  @click="selectAttachment(i)"
                >
                  <span class="attachment-tab__dot" :data-kind="tabIconKind(att)" />
                  {{ att.fileName }}
                </button>
              </div>

              <div class="preview-body">
                <div v-if="previewError" class="preview-warn">
                  <FileWarning :size="18" />
                  <span>{{ previewError }}</span>
                  <button v-if="currentAttachment" type="button" class="preview-warn__link" @click="handleDownload(currentAttachment)">尝试下载</button>
                </div>

                <div v-else-if="previewKind === 'markdown'" class="md-document" v-html="markdownHtml" />

                <div v-else-if="previewKind === 'text'" class="plain-document">
                  <pre>{{ textPreviewContent }}</pre>
                </div>

                <div v-else-if="previewKind === 'image'" class="preview-media">
                  <img :src="previewBlobUrl" :alt="currentAttachment?.fileName" />
                </div>

                <div v-else-if="previewKind === 'pdf'" class="preview-media preview-media--pdf">
                  <iframe :src="previewBlobUrl" title="PDF 预览" />
                </div>

                <div v-else-if="previewKind === 'word'" class="word-document" v-html="wordPreviewHtml" />

                <div v-else-if="previewKind === 'archive'" class="download-inspect-card">
                  <div class="download-inspect-card__icon"><Package :size="32" /></div>
                  <div class="download-inspect-card__main">
                    <div class="download-inspect-card__title">压缩包提交</div>
                    <div class="download-inspect-card__name">{{ currentAttachment?.fileName }}</div>
                    <div class="download-inspect-card__meta">
                      {{ formatFileSize(currentAttachment?.fileSize) }}
                      <span v-if="currentAttachment?.fileType"> · {{ currentAttachment.fileType }}</span>
                    </div>
                    <p class="download-inspect-card__hint">
                      需下载后在本地解压查看源码，建议对照实训报告与任务要求逐项复核。
                    </p>
                    <BaseButton v-if="currentAttachment" type="primary" size="small" @click="handleDownload(currentAttachment)">
                      <Download :size="14" />
                      <span style="margin-left: 4px">下载并检查</span>
                    </BaseButton>
                  </div>
                </div>

                <div v-else-if="previewKind === 'unknown'" class="download-inspect-card">
                  <div class="download-inspect-card__icon"><FileText :size="32" /></div>
                  <div class="download-inspect-card__main">
                    <div class="download-inspect-card__title">需下载查看</div>
                    <div class="download-inspect-card__name">{{ currentAttachment?.fileName }}</div>
                    <p class="download-inspect-card__hint">该格式暂不支持在线预览，请下载后在本地打开。</p>
                    <BaseButton v-if="currentAttachment" type="primary" size="small" @click="handleDownload(currentAttachment)">
                      <Download :size="14" />
                      <span style="margin-left: 4px">下载并检查</span>
                    </BaseButton>
                  </div>
                </div>
              </div>

              <div v-if="currentAttachment && previewKind !== 'archive' && previewKind !== 'unknown'" class="preview-footer">
                <span class="preview-footer__meta">
                  {{ currentAttachment.fileName }} · {{ formatFileSize(currentAttachment.fileSize) }}
                </span>
                <BaseButton type="default" size="small" @click="handleDownload(currentAttachment)">
                  <Download :size="14" />
                  <span style="margin-left: 4px">下载</span>
                </BaseButton>
              </div>
              </template>
            </template>
          </div>
        </div>

        <!-- RIGHT: AI diagnosis + scoring -->
        <div
          class="review-right"
          :class="{ 'review-right--mobile-show': isMdDown && mobilePane === 'work' && mobileWorkTab === 'score' }"
        >
          <div v-if="!currentSub" class="empty-hint">请先选择学生</div>
          <div v-else-if="!store.currentDiagnosis" class="empty-hint">暂无 AI 诊断数据</div>
          <template v-else>
            <div class="ai-score-stack">
            <!-- Score ring -->
            <div class="score-header">
              <div :class="['score-ring', aiScoreRingClass]">{{ store.currentDiagnosis.aiScore.toFixed(1) }}</div>
              <div>
                <div class="grade-label">AI 参考分</div>
                <div class="grade-meta">共 {{ store.currentDiagnosis.totalDeductions }} 项扣分明细 · 合计 -{{ store.currentDiagnosis.totalDeductScore }}</div>
              </div>
            </div>

            <!-- Deduction items -->
            <div class="deductions">
              <div
                v-for="(d, i) in store.currentDiagnosis.deductions"
                :key="i"
                :class="['deduction-item', { 'deduction-item--rejected': d.overridden && d.overrideAction === 'reject' }]"
              >
                <div class="ded-header">
                  <span :class="['ded-tag', `ded-tag--${(d.agentType || 'CODE').toLowerCase()}`]">{{ d.agentType || 'CODE' }}</span>
                  <span class="ded-type">{{ d.issueType }}</span>
                  <span class="ded-score">
                    <template v-if="d.overridden && d.overrideAction === 'reject'">已驳回</template>
                    <template v-else>-{{ d.adjustedDeduct !== undefined ? d.adjustedDeduct : d.suggestDeduct }}</template>
                  </span>
                </div>
                <div class="ded-reason">{{ d.reason }}</div>
                <div class="ded-file">{{ d.filePath }}:{{ d.lineNumber }} · 置信度 {{ (d.confidence * 100).toFixed(0) }}%</div>
                <div class="ded-actions">
                  <button type="button" :class="['ded-btn', { 'ded-btn--active': d.overridden && d.overrideAction === 'accept' }]" @click="acceptDeduction(i)">采纳</button>
                  <button type="button" :class="['ded-btn', { 'ded-btn--active': d.overridden && d.overrideAction === 'reject' }]" @click="rejectDeduction(i)">驳回</button>
                  <button type="button" class="ded-btn" @click="openAdjust(i)">调整</button>
                </div>
              </div>
            </div>
            </div>
          </template>

          <!-- Teacher scoring -->
          <div class="score-zone">
            <div class="score-zone__title">教师评语</div>
            <textarea v-model="teacherComment" class="score-zone__textarea" placeholder="输入评语..." />
            <div class="quick-tags">
              <span v-for="tag in quickTags" :key="tag" class="quick-tag" @click="insertTag(tag)">{{ tag }}</span>
            </div>
            <div class="manual-row">
              <span class="manual-label">手动调整</span>
              <input v-model.number="manualPoints" class="manual-points" type="number" placeholder="负数扣分，正数加分" min="-100" max="100" step="0.5" />
              <input v-model="manualReason" class="manual-reason" placeholder="调整原因" />
              <button class="manual-add" @click="addManual">+</button>
            </div>
            <div v-for="(md, i) in manualDeductions" :key="i" class="manual-item">
              <span :class="['manual-item__pts', md.points > 0 ? 'manual-item__pts--bonus' : '']">{{ formatManualAdjustment(md.points) }}</span>
              <span class="manual-item__reason">{{ md.reason }}</span>
              <span class="manual-item__remove" @click="removeManual(i)">×</span>
            </div>
          </div>

          <!-- Final score -->
          <div class="final-score">
            最终得分: <span :class="['fs-val', finalScoreRingClass]">{{ finalScore.toFixed(1) }}</span>
            <span class="final-grade">{{ gradeLabel }}</span>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div v-if="currentSub" class="review-footer">
        <div class="footer-nav">
          <BaseButton :disabled="currentIndex <= 0" @click="prevSubmission">← 上一个</BaseButton>
          <BaseButton :disabled="currentIndex >= filteredPending.length - 1" @click="nextSubmission">下一个 →</BaseButton>
        </div>
        <div class="footer-actions">
          <BaseButton type="danger" @click="handleReject">打回</BaseButton>
          <BaseButton type="primary" @click="handlePublish">确认发布</BaseButton>
          <BaseButton type="default" @click="openAiRestoreDialog">还原 AI 次数</BaseButton>
        </div>
      </div>
    </template>

    <!-- Adjust modal -->
    <teleport to="body">
      <div v-if="showAdjust" class="adj-overlay" @click.self="showAdjust = false">
        <div class="adj-modal">
          <div class="adj-modal__title">调整扣分</div>
          <div class="form-group"><label class="form-label">调整分值</label><input v-model.number="adjPoints" class="form-input" type="number" min="0" max="100" step="0.5" /></div>
          <div class="form-group"><label class="form-label">调整理由</label><textarea v-model="adjReason" class="form-textarea" rows="2" placeholder="请输入调整理由" /></div>
          <div class="adj-modal__btns">
            <BaseButton @click="showAdjust = false">取消</BaseButton>
            <BaseButton type="primary" @click="confirmAdjust">确认调整</BaseButton>
          </div>
        </div>
      </div>

      <div v-if="showAiRestore" class="adj-overlay" @click.self="showAiRestore = false">
        <div class="adj-modal ai-restore-modal">
          <div class="adj-modal__title">还原 AI 分析次数</div>
          <p class="ai-restore-hint">不驳回提交，仅将已用次数清零，学生可继续试交并触发 AI 分析。</p>
          <div class="form-group">
            <label class="form-label">选择任务</label>
            <select
              v-model="restoreTaskId"
              class="form-input"
              :disabled="restoreLoading"
              @change="loadRestoreTaskDetail(restoreTaskId)"
            >
              <option value="">请选择任务</option>
              <option v-for="t in restoreTasks" :key="t.taskId" :value="t.taskId">
                {{ t.taskName }}（{{ t.courseName }}）
              </option>
            </select>
          </div>
          <div v-if="restoreLoading" class="ai-restore-loading">加载中...</div>
          <div v-else-if="restoreTaskId && restoreCandidates.length === 0" class="ai-restore-empty">
            该任务暂无试交阶段学生
          </div>
          <div v-else-if="restoreCandidates.length" class="ai-restore-table-wrap">
            <table class="ai-restore-table">
              <thead>
                <tr>
                  <th>学生</th>
                  <th>状态</th>
                  <th>AI 次数</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in restoreCandidates" :key="row.userId">
                  <td>{{ row.realName }}</td>
                  <td>{{ statusLabel(row.latestStatus) }}</td>
                  <td class="mono">{{ row.aiAnalyzeUsedCount }} / {{ row.maxAiAnalyzeCount }}</td>
                  <td>
                    <BaseButton
                      type="primary"
                      size="small"
                      :disabled="!canRestoreAiCount(row)"
                      :loading="restoreSubmitting === row.userId"
                      @click="handleRestoreAiCount(row)"
                    >
                      还原
                    </BaseButton>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="adj-modal__btns">
            <BaseButton @click="showAiRestore = false">关闭</BaseButton>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.review-root {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  padding: 0 12px;
}

/* === Three-panel === */
.review-wb {
  display: flex;
  flex: 1;
  min-height: 0;
  margin: 4px 0 0;
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: var(--radius-xl, 16px);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  background: var(--color-card, #fff);
}

.list-course-select {
  width: 100%;
  margin: 0 0 8px;
  height: 34px;
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: 10px;
  padding: 0 10px;
  font-size: 12px;
  outline: none;
  box-sizing: border-box;
  background: var(--color-card, #fff);
  color: var(--color-text-primary, #1e293b);
  cursor: pointer;
}
.list-course-select:focus { border-color: var(--color-primary, #1677FF); }
.list-search {
  width: 100%;
  margin: 0 0 10px;
  height: 34px;
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: 10px;
  padding: 0 10px;
  font-size: 12px;
  outline: none;
  box-sizing: border-box;
}
.list-search:focus { border-color: var(--color-primary, #1677FF); }

/* LEFT */
.review-left {
  width: 280px;
  border-right: 1px solid var(--color-border, #E5E7EB);
  overflow-y: auto;
  padding: 16px;
  flex-shrink: 0;
  background: var(--color-card, #fff);
  border-radius: var(--radius-xl, 16px) 0 0 var(--radius-xl, 16px);
}

/* CENTER */
.review-center {
  flex: 1;
  overflow-y: auto;
  border-right: 1px solid var(--color-border, #E5E7EB);
  display: flex;
  flex-direction: column;
  background: var(--color-bg, #F5F7FA);
}

/* RIGHT */
.review-right {
  width: 380px;
  overflow-y: auto;
  padding: 16px;
  flex-shrink: 0;
  background: var(--color-card, #fff);
  border-radius: 0 var(--radius-xl, 16px) var(--radius-xl, 16px) 0;
}

.panel-title {
  font-weight: 800;
  font-size: 14px;
  letter-spacing: -0.02em;
  margin-bottom: 12px;
  padding: 0 4px;
  color: var(--color-navy, #0B3A5C);
}
.empty-hint { color: var(--color-text-placeholder, #94a3b8); text-align: center; padding: 80px 0; font-size: 13px; }

/* Student list */
.student-list { display: flex; flex-direction: column; gap: 2px; }
.student-item { padding: 12px; border-radius: 12px; cursor: pointer; transition: background 0.1s; border-left: 3px solid transparent; }
.student-item:hover { background: var(--color-bg, #f8fafc); }
.student-item--active { background: var(--color-primary-light, #E6F4FF); border-left-color: var(--color-primary, #1677FF); }
.si-name { font-size: 14px; font-weight: 500; color: var(--color-text-primary, #1e293b); }
.si-course { font-size: 11px; color: var(--color-primary, #1677FF); margin-top: 2px; }
.si-task { font-size: 12px; color: var(--color-text-placeholder, #94a3b8); margin-top: 2px; }
.si-meta { display: flex; align-items: center; gap: 8px; margin-top: 4px; }
.si-type-tag { font-size: 10px; font-weight: 600; padding: 1px 6px; border-radius: 3px; }
.si-type-tag--code { background: var(--color-primary-light, #E6F4FF); color: var(--color-primary, #1677FF); }
.si-type-tag--file { background: var(--color-success-light, #E6F7EF); color: #009A57; }
.si-type-tag--git { background: #F3E8FF; color: #722ED1; }
.si-time { font-size: 11px; color: var(--color-text-placeholder, #94a3b8); font-family: var(--font-mono); }

/* 在线代码预览（对齐学生端 Submit 页） */
.online-code-preview {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 0;
  background: var(--color-card, #fff);
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: 12px;
  overflow: hidden;
}
.online-code-preview__tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  align-self: flex-start;
  margin: 12px 12px 0;
  padding: 6px 12px;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-primary, #1677FF);
  background: var(--color-primary-light, #E6F4FF);
  border: 1px solid #91CAFF;
  border-radius: 8px;
}
.online-code-preview__tab--git {
  color: #722ED1;
  background: #F9F0FF;
  border-color: #D3ADF7;
}
.online-code-preview :deep(.code-editor-wrapper) {
  margin: 12px;
  border-radius: 8px;
}
.online-code-preview__error {
  margin: 12px;
}
.git-preview {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 280px;
  background: var(--color-card, #fff);
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: 12px;
}
.git-preview__body {
  flex: 1;
  padding: 16px 20px 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.git-preview__row {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.git-preview__label {
  font-size: 12px;
  color: var(--color-text-secondary, #64748b);
}
.git-preview__link {
  font-size: 13px;
  color: var(--color-primary, #1677FF);
  word-break: break-all;
  text-decoration: none;
}
.git-preview__link:hover { text-decoration: underline; }
.git-preview__value {
  font-size: 14px;
  font-family: var(--font-mono);
  color: var(--color-text-primary, #1e293b);
}
.git-preview__empty { font-size: 13px; color: var(--color-text-placeholder, #94a3b8); }
.git-preview__hint {
  margin-top: auto;
  font-size: 12px;
  color: var(--color-text-secondary, #64748b);
  line-height: 1.6;
}

/* Code preview */
.code-preview {
  font-family: var(--font-mono); font-size: 13px; line-height: 1.7;
  white-space: pre-wrap; background: #1e293b; color: #e2e8f0;
  padding: 16px; margin: 0; flex: 1; overflow: auto;
}
.code-line { display: flex; }
.code-line--issue { background: rgba(239, 68, 68, 0.12); }
.code-ln { width: 36px; text-align: right; color: #64748b; padding-right: 12px; user-select: none; flex-shrink: 0; }
.code-line--issue .code-ln::after { content: " ⚡"; font-size: 10px; }
.code-txt { flex: 1; }

/* === Preview panel === */
.preview-header {
  padding: 16px 20px 12px;
  background: var(--color-card, #fff);
  border-bottom: 1px solid var(--color-border, #E5E7EB);
  flex-shrink: 0;
}
.preview-header__title { font-size: 15px; font-weight: 700; color: var(--color-navy, #0B3A5C); }
.preview-header__summary { margin-top: 6px; font-size: 13px; color: var(--color-text-secondary, #64748b); line-height: 1.5; }
.preview-panel { flex: 1; display: flex; flex-direction: column; min-height: 0; padding: 12px 16px 16px; gap: 12px; }
.preview-body {
  flex: 1; min-height: 280px; display: flex; flex-direction: column;
  background: var(--color-card, #fff); border: 1px solid var(--color-border, #E5E7EB);
  border-radius: 12px; overflow: hidden;
}
.preview-skeleton { flex: 1; padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.preview-skeleton__bar, .preview-skeleton__body {
  border-radius: 8px;
  background: linear-gradient(90deg, #eef2f6 25%, #f8fafc 50%, #eef2f6 75%);
  background-size: 200% 100%;
  animation: preview-shimmer 1.2s infinite;
}
.preview-skeleton__bar { height: 32px; }
.preview-skeleton__body { flex: 1; border-radius: 12px; }
@keyframes preview-shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
.preview-empty {
  flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 8px; padding: 48px 24px; text-align: center; color: var(--color-text-secondary, #64748b); font-size: 13px;
}
.preview-empty__icon { color: var(--color-text-placeholder, #94a3b8); opacity: 0.7; }
.preview-empty__title { font-size: 15px; font-weight: 600; color: var(--color-text-primary, #1e293b); margin: 0; }
.preview-empty__desc { margin: 0; max-width: 360px; line-height: 1.6; }
.preview-empty__note { margin: 8px 0 0; padding: 10px 14px; background: var(--color-bg, #F5F7FA); border-radius: 8px; font-size: 12px; }
.preview-warn {
  flex: 1; display: flex; align-items: center; justify-content: center; gap: 8px; padding: 24px;
  font-size: 13px; color: #b45309; background: #fffbeb;
}
.preview-warn__link { border: none; background: none; color: var(--color-primary, #1677FF); cursor: pointer; text-decoration: underline; }
.attachment-tabs { display: flex; flex-wrap: wrap; gap: 8px; flex-shrink: 0; }
.attachment-tab {
  display: inline-flex; align-items: center; gap: 6px; font-size: 12px; padding: 6px 12px;
  border-radius: 999px; border: 1px solid var(--color-border, #E5E7EB); background: #fff;
  cursor: pointer; color: var(--color-text-secondary, #64748b); transition: all 0.15s;
}
.attachment-tab:hover { border-color: var(--color-primary, #1677FF); color: var(--color-primary, #1677FF); }
.attachment-tab--active {
  background: var(--color-primary-light, #E6F4FF); border-color: var(--color-primary, #1677FF);
  color: var(--color-primary, #1677FF); font-weight: 600;
}
.attachment-tab__dot { width: 6px; height: 6px; border-radius: 50%; background: #94a3b8; flex-shrink: 0; }
.attachment-tab__dot[data-kind="markdown"] { background: #1677FF; }
.attachment-tab__dot[data-kind="pdf"] { background: #FF4D4F; }
.attachment-tab__dot[data-kind="image"] { background: #00B96B; }
.attachment-tab__dot[data-kind="archive"] { background: #FAAD14; }
.preview-footer {
  display: flex; align-items: center; justify-content: space-between; gap: 12px;
  padding: 10px 12px; background: #fff; border: 1px solid var(--color-border, #E5E7EB); border-radius: 10px;
}
.preview-footer__meta { font-size: 12px; color: var(--color-text-secondary, #64748b); }
.preview-media { flex: 1; display: flex; align-items: center; justify-content: center; min-height: 0; overflow: auto; padding: 16px; background: #f8fafc; }
.preview-media img { max-width: 100%; max-height: 100%; object-fit: contain; border-radius: 8px; box-shadow: 0 1px 4px rgba(15,23,42,0.08); }
.preview-media--pdf { padding: 0; }
.preview-media--pdf iframe { width: 100%; height: 100%; min-height: 420px; border: none; }
.md-document, .word-document { flex: 1; overflow: auto; padding: 28px 32px; font-size: 15px; line-height: 1.75; color: var(--color-text-primary, #1e293b); }
.md-document :deep(h1) { font-size: 1.5em; font-weight: 700; margin: 0 0 0.75em; color: var(--color-navy, #0B3A5C); }
.md-document :deep(h2) { font-size: 1.25em; font-weight: 700; margin: 1.25em 0 0.5em; }
.md-document :deep(h3) { font-size: 1.1em; font-weight: 600; margin: 1em 0 0.4em; }
.md-document :deep(p) { margin: 0 0 0.85em; }
.md-document :deep(ul), .md-document :deep(ol) { margin: 0 0 0.85em; padding-left: 1.4em; }
.md-document :deep(code) { font-family: var(--font-mono); font-size: 0.9em; padding: 0.1em 0.35em; background: #F5F7FA; border-radius: 4px; }
.md-document :deep(pre) { padding: 14px 16px; background: #f8fafc; border: 1px solid #E5E7EB; border-radius: 8px; overflow: auto; margin: 0 0 1em; }
.md-document :deep(table),
.word-document :deep(table) {
  border-collapse: collapse;
  width: max-content;
  min-width: 100%;
  margin: 0 0 1em;
}
.md-document :deep(th),
.md-document :deep(td),
.word-document :deep(th),
.word-document :deep(td) {
  border: 1px solid var(--color-border, #E5E7EB);
  padding: 8px 12px;
  white-space: nowrap;
  vertical-align: top;
}
.plain-document { flex: 1; overflow: auto; padding: 20px 24px; }
.plain-document pre { margin: 0; font-family: var(--font-mono); font-size: 13px; line-height: 1.65; white-space: pre-wrap; word-break: break-word; }
.download-inspect-card {
  flex: 1; display: flex; align-items: center; justify-content: center; gap: 20px; padding: 40px 32px;
}
.download-inspect-card__icon {
  width: 64px; height: 64px; border-radius: 16px; background: var(--color-primary-light, #E6F4FF);
  color: var(--color-primary, #1677FF); display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.download-inspect-card__title { font-size: 13px; font-weight: 600; color: var(--color-text-secondary, #64748b); margin-bottom: 4px; }
.download-inspect-card__name { font-size: 16px; font-weight: 700; color: var(--color-text-primary, #1e293b); margin-bottom: 6px; word-break: break-all; }
.download-inspect-card__meta { font-size: 12px; color: var(--color-text-placeholder, #94a3b8); margin-bottom: 12px; }
.download-inspect-card__hint { font-size: 13px; color: var(--color-text-secondary, #64748b); line-height: 1.6; margin: 0 0 16px; max-width: 360px; }

/* Score ring */
.score-header { display: flex; align-items: center; gap: 14px; margin-bottom: 16px; }
.score-ring { width: 56px; height: 56px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-family: var(--font-mono); font-size: 16px; font-weight: 700; flex-shrink: 0; }
.ring-excellent { background: #E6F7EF; color: #009A57; border: 3px solid var(--color-success, #00B96B); }
.ring-good { background: #E6F4FF; color: #0958D9; border: 3px solid var(--color-primary, #1677FF); }
.ring-fair { background: #fef3c7; color: #92400e; border: 3px solid var(--color-warning, #FAAD14); }
.ring-poor { background: #fee2e2; color: #991b1b; border: 3px solid var(--color-danger, #FF4D4F); }
.grade-label { font-size: 13px; font-weight: 500; color: var(--color-text-primary, #1e293b); }
.grade-meta { font-size: 11px; color: var(--color-text-placeholder, #94a3b8); }

/* Deductions */
.deductions { margin-bottom: 16px; }
.deduction-item { padding: 10px 12px; border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-sm, 4px); margin-bottom: 6px; background: var(--color-card, #fff); }
.deduction-item--rejected { opacity: 0.5; background: var(--color-bg, #f8fafc); }
.deduction-item--rejected .ded-reason { text-decoration: line-through; }
.ded-header { display: flex; align-items: center; gap: 6px; margin-bottom: 4px; }
.ded-tag { font-size: 10px; font-weight: 600; padding: 1px 6px; border-radius: 3px; text-transform: uppercase; }
.ded-tag--code { background: var(--color-primary-light, #E6F4FF); color: var(--color-primary, #1677FF); }
.ded-tag--doc { background: var(--color-success-light, #E6F7EF); color: #009A57; }
.ded-tag--req { background: var(--color-warning-light, #fffbeb); color: #b45309; }
.ded-type { font-size: 12px; font-weight: 500; flex: 1; color: var(--color-text-primary, #1e293b); }
.ded-score { font-family: var(--font-mono); font-size: 13px; font-weight: 600; color: var(--color-danger, #FF4D4F); margin-left: auto; }
.ded-reason { font-size: 12px; color: var(--color-text-secondary, #64748b); line-height: 1.4; }
.ded-file { font-size: 11px; color: var(--color-text-placeholder, #94a3b8); font-family: var(--font-mono); margin-top: 2px; }
.ded-actions { display: flex; gap: 4px; margin-top: 6px; }
.ded-btn { font-size: 11px; padding: 2px 8px; border-radius: 3px; border: 1px solid var(--color-border, #e2e8f0); background: var(--color-bg, #f8fafc); cursor: pointer; color: var(--color-text-secondary, #64748b); }
.ded-btn:hover { background: #f1f5f9; }
.ded-btn--active { background: #E6F7EF; border-color: #00B96B; color: #009A57; }

/* Score zone */
.score-zone { background: var(--color-bg, #f8fafc); border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-md, 8px); padding: 16px; margin-top: 0; }
.score-zone__title { font-size: 13px; font-weight: 600; margin-bottom: 8px; color: var(--color-text-primary, #1e293b); }
.score-zone__textarea { width: 100%; min-height: 80px; resize: vertical; padding: 8px 12px; font-size: 13px; font-family: inherit; border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-sm, 4px); background: var(--color-card, #fff); color: var(--color-text-primary, #1e293b); outline: none; box-sizing: border-box; }
.score-zone__textarea:focus { border-color: var(--color-primary, #1677FF); }
.quick-tags { display: flex; flex-wrap: wrap; gap: 4px; margin-top: 8px; }
.quick-tag { font-size: 11px; padding: 2px 8px; border-radius: 12px; border: 1px solid var(--color-border, #e2e8f0); background: var(--color-card, #fff); cursor: pointer; color: var(--color-text-secondary, #64748b); transition: all var(--transition-fast, 150ms); }
.quick-tag:hover { background: var(--color-primary-light, #E6F4FF); border-color: var(--color-primary, #1677FF); color: var(--color-primary, #1677FF); }
.manual-row { display: flex; gap: 6px; align-items: center; margin-top: 10px; }
.manual-label { font-size: 12px; color: var(--color-text-placeholder, #94a3b8); white-space: nowrap; }
.manual-points { width: 120px; padding: 5px 8px; font-size: 12px; border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-sm, 4px); background: var(--color-card, #fff); outline: none; box-sizing: border-box; }
.manual-reason { flex: 1; padding: 5px 8px; font-size: 12px; border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-sm, 4px); background: var(--color-card, #fff); outline: none; box-sizing: border-box; }
.manual-add { padding: 5px 10px; font-size: 13px; border: 1px solid var(--color-border, #e2e8f0); background: var(--color-bg, #f8fafc); border-radius: var(--radius-sm, 4px); cursor: pointer; color: var(--color-text-secondary, #64748b); }
.manual-item { display: flex; align-items: center; gap: 8px; padding: 6px 0; border-bottom: 1px solid #f1f5f9; font-size: 12px; }
.manual-item__pts { font-family: var(--font-mono); color: var(--color-danger, #FF4D4F); }
.manual-item__pts--bonus { color: var(--color-success, #00B96B); }
.manual-item__reason { flex: 1; color: var(--color-text-secondary, #64748b); }
.manual-item__remove { color: var(--color-danger, #FF4D4F); cursor: pointer; font-size: 14px; }

/* Final score */
.final-score { text-align: center; margin-top: 16px; font-size: 12px; color: var(--color-text-placeholder, #94a3b8); }
.final-grade { margin-left: 8px; font-size: 13px; color: var(--color-text-secondary, #64748b); }
.fs-val { font-family: var(--font-mono); font-size: 28px; font-weight: 700; }

/* Footer */
.review-footer {
  border-top: 1px solid var(--color-border, #e2e8f0);
  padding: 10px 8px;
  margin-top: 8px; display: flex; justify-content: space-between; align-items: center; flex-shrink: 0; background: var(--color-card, #fff); }
.footer-nav { display: flex; gap: 8px; }
.footer-actions { display: flex; gap: 8px; }

/* Adjust modal */
.adj-overlay { position: fixed; inset: 0; background: rgba(15, 23, 42, 0.4); z-index: 300; display: flex; align-items: center; justify-content: center; }
.adj-modal { background: var(--color-card, #fff); border-radius: var(--radius-lg, 12px); box-shadow: var(--shadow-xl); width: 400px; padding: 24px; }
.adj-modal__title { font-size: 15px; font-weight: 600; margin-bottom: 16px; color: var(--color-text-primary, #1e293b); }
.adj-modal__btns { display: flex; justify-content: flex-end; gap: 8px; margin-top: 16px; }
.form-group { margin-bottom: 16px; }
.form-label { display: block; font-size: 13px; font-weight: 500; color: var(--color-text-secondary, #64748b); margin-bottom: 5px; }
.form-input { width: 100%; padding: 8px 12px; font-size: 14px; border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-sm, 4px); background: var(--color-card, #fff); color: var(--color-text-primary, #1e293b); outline: none; box-sizing: border-box; }
.form-input:focus { border-color: var(--color-primary, #1677FF); }

.ai-restore-modal { width: 560px; max-width: calc(100vw - 32px); }
.ai-restore-hint { font-size: 13px; color: var(--color-text-secondary, #64748b); margin: 0 0 16px; line-height: 1.5; }
.ai-restore-loading, .ai-restore-empty { font-size: 13px; color: var(--color-text-placeholder, #94a3b8); padding: 12px 0 20px; }
.ai-restore-table-wrap { max-height: 320px; overflow: auto; margin-bottom: 8px; }
.ai-restore-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.ai-restore-table th, .ai-restore-table td { padding: 8px 10px; border-bottom: 1px solid var(--color-border, #e2e8f0); text-align: left; }
.ai-restore-table th { color: var(--color-text-secondary, #64748b); font-weight: 500; }
.ai-restore-table .mono { font-family: var(--font-mono); }
.form-textarea { width: 100%; padding: 8px 12px; font-size: 14px; border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-sm, 4px); background: var(--color-card, #fff); color: var(--color-text-primary, #1e293b); resize: vertical; outline: none; font-family: inherit; box-sizing: border-box; }
.form-textarea:focus { border-color: var(--color-primary, #1677FF); }

.mobile-work-chrome {
  display: none;
}

.mobile-back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: var(--touch-min, 44px);
  padding: 0 12px;
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: 10px;
  background: #fff;
  color: var(--color-text-secondary, #64748b);
  font-size: 13px;
  cursor: pointer;
}

.mobile-work-tabs {
  display: flex;
  gap: 4px;
  padding: 4px;
  background: #fff;
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: var(--radius-full, 999px);
}

.mobile-work-tab {
  min-height: 36px;
  padding: 0 14px;
  border: none;
  border-radius: var(--radius-full, 999px);
  background: transparent;
  font-size: 13px;
  color: var(--color-text-secondary, #6B7280);
  cursor: pointer;
}

.mobile-work-tab--active {
  background: var(--color-primary-light, #E6F4FF);
  color: var(--color-primary, #1677FF);
  font-weight: 700;
}

@media (max-width: 1023.98px) {
  .review-root {
    padding: 0;
  }

  .review-wb {
    flex-direction: column;
    overflow: hidden;
    min-height: calc(100vh - 120px);
  }

  .review-left,
  .review-right {
    width: 100%;
    border-right: none;
    border-radius: 0;
  }

  .review-left {
    border-bottom: 1px solid var(--color-border, #E5E7EB);
  }

  .mobile-work-chrome {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 10px 12px;
    border-bottom: 1px solid var(--color-border, #E5E7EB);
    background: var(--color-card, #fff);
    flex-shrink: 0;
  }

  .mobile-work-tabs {
    flex: 1;
    justify-content: stretch;
  }

  .mobile-work-tab {
    flex: 1;
    min-height: 40px;
  }

  .review-wb--mobile-list .review-center,
  .review-wb--mobile-list .review-right,
  .review-wb--mobile-list .mobile-work-chrome {
    display: none;
  }

  .review-wb--mobile-work .review-left {
    display: none;
  }

  .review-wb--mobile-work .review-center:not(.review-center--mobile-show) {
    display: none;
  }

  .review-wb--mobile-work .review-right:not(.review-right--mobile-show) {
    display: none;
  }

  /* 关键：必须 column，否则评分区 AI / 评语会左右并排挤成竖排字 */
  .review-wb--mobile-work .review-center--mobile-show,
  .review-wb--mobile-work .review-right--mobile-show {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-height: 0;
    width: 100%;
    overflow-x: hidden;
    overflow-y: auto;
  }

  .review-right--mobile-show {
    padding: 12px 12px 20px;
    gap: 12px;
  }

  .ai-score-stack {
    display: flex;
    flex-direction: column;
    gap: 4px;
    width: 100%;
    min-width: 0;
  }

  .score-header {
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 4px;
    padding: 14px;
    background: var(--color-card, #fff);
    border: 1px solid var(--color-border, #E5E7EB);
    border-radius: 14px;
  }

  .score-header > div:last-child {
    flex: 1;
    min-width: 0;
  }

  .score-ring {
    width: 64px;
    height: 64px;
    font-size: 18px;
  }

  .grade-label {
    font-size: 14px;
    font-weight: 700;
  }

  .grade-meta {
    margin-top: 4px;
    font-size: 12px;
    line-height: 1.45;
    white-space: normal;
    word-break: break-word;
  }

  .deductions {
    margin-bottom: 4px;
  }

  .deduction-item {
    padding: 12px 14px;
    border-radius: 12px;
    margin-bottom: 10px;
  }

  .ded-header {
    flex-wrap: wrap;
    gap: 8px;
  }

  .ded-type {
    font-size: 13px;
  }

  .ded-reason {
    font-size: 13px;
    line-height: 1.5;
    margin-top: 4px;
  }

  .ded-actions {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    gap: 8px;
    margin-top: 12px;
  }

  .ded-btn {
    min-height: var(--touch-min, 44px);
    padding: 8px 10px;
    font-size: 13px;
    font-weight: 600;
    border-radius: 10px;
  }

  .score-zone {
    border-radius: 14px;
    padding: 14px;
  }

  .score-zone__textarea {
    min-height: 100px;
    font-size: 14px;
  }

  .quick-tags {
    gap: 8px;
  }

  .quick-tag {
    min-height: 36px;
    padding: 8px 12px;
    font-size: 12px;
    display: inline-flex;
    align-items: center;
  }

  .manual-row {
    flex-wrap: wrap;
    gap: 8px;
  }

  .manual-label {
    width: 100%;
  }

  .manual-points,
  .manual-reason {
    width: 100%;
    min-height: var(--touch-min, 44px);
    box-sizing: border-box;
  }

  .manual-add {
    min-width: var(--touch-min, 44px);
    min-height: var(--touch-min, 44px);
  }

  .final-score {
    margin-top: 4px;
    padding: 16px;
    border-radius: 14px;
    background: var(--color-card, #fff);
    border: 1px solid var(--color-border, #E5E7EB);
  }

  .preview-header {
    padding: 12px 14px;
  }

  .preview-header__title {
    font-size: 14px;
    line-height: 1.4;
    word-break: break-word;
  }

  .preview-panel {
    padding: 10px 12px 16px;
  }

  .preview-body {
    min-height: 200px;
  }

  .md-document,
  .word-document {
    padding: 16px;
  }

  .attachment-tab {
    min-height: 40px;
    padding: 8px 12px;
  }

  .preview-footer {
    flex-wrap: wrap;
  }

  .review-footer {
    flex-wrap: wrap;
    gap: 10px;
    padding: 10px 12px calc(10px + env(safe-area-inset-bottom, 0px));
  }
}

@media (max-width: 767.98px) {
  .review-footer {
    flex-direction: column-reverse;
    align-items: stretch;
  }

  .footer-nav,
  .footer-actions {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }

  .footer-actions {
    grid-template-columns: 1fr 1.35fr;
  }

  .footer-actions :deep(.base-button:last-child),
  .footer-actions > :last-child {
    grid-column: 1 / -1;
  }

  .footer-nav :deep(button),
  .footer-actions :deep(button),
  .footer-nav :deep(.base-button),
  .footer-actions :deep(.base-button) {
    width: 100%;
    min-height: var(--touch-min, 44px);
  }
}
</style>
