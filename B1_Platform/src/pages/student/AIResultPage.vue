<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from "vue"
import { useRoute, useRouter } from "vue-router"
import { useStudentStore } from "@/stores/useStudentStore"
import { ElMessage } from "element-plus"
import {
  CheckCircle, Clock, Cpu, Lightbulb, BookOpen, FileText, Code2,
  MessageCircle, Award, TrendingUp,
} from "lucide-vue-next"
import BaseButton from "@/components/base/BaseButton.vue"
import ErrorState from "@/components/common/ErrorState.vue"
import {
  ANALYSIS_NA,
  mapSubmitTypeToMode,
  mergeScopesWithIssues,
  resolveAnalysisScopes,
} from "@/utils/aiAnalysisScope"
import { shouldUseLiveAIAnalysis } from "@/utils/submissionAIView"
import type { IAIResultDetail } from "@/types/student"

const route = useRoute()
const router = useRouter()
const store = useStudentStore()

const isGradeView = computed(() => route.name === "StudentGrades")
const sid = computed(() => {
  if (isGradeView.value) return (route.params.submissionId as string) || ""
  return (route.query.sid as string) || ""
})
const trialId = computed(() => (route.query.trialId as string) || "")
const scopeMode = ref((route.query.mode as string) || "")
const scopeFileName = ref((route.query.fileName as string) || "")
const taskNameFromQuery = computed(() => (route.query.taskName as string) || "")

function applyScopeFromEvaluation() {
  if (scopeMode.value) return
  const ev = store.evaluation
  if (ev?.submitType) {
    scopeMode.value = mapSubmitTypeToMode(ev.submitType)
  } else {
    scopeMode.value = "file"
  }
  if (!scopeFileName.value && ev?.attachmentFileName) {
    scopeFileName.value = ev.attachmentFileName
  }
}

const loadError = ref("")
const confirming = ref(false)
const pageBooting = ref(true)
const aiPhase = ref<"analyzing" | "streaming" | "ready" | "failed">("ready")
const detail = ref<IAIResultDetail | null>(null)

const streamCode = ref("")
const streamDoc = ref("")
const streamReq = ref("")
const revealedDeductions = ref<Set<number>>(new Set())
const readItems = ref<Set<number>>(new Set())

let timers: ReturnType<typeof setInterval>[] = []
let timeouts: ReturnType<typeof setTimeout>[] = []

const baseScopes = computed(() =>
  resolveAnalysisScopes(scopeMode.value || "file", scopeFileName.value),
)

const analysisScopes = computed(() => {
  if (!detail.value?.deductions?.length) return baseScopes.value
  return mergeScopesWithIssues(baseScopes.value, detail.value.deductions)
})

function typewriter(target: "code" | "doc" | "req", text: string, speed: number): Promise<void> {
  return new Promise((resolve) => {
    if (!text) {
      resolve()
      return
    }
    let i = 0
    const timer = setInterval(() => {
      i++
      if (target === "code") streamCode.value = text.substring(0, i)
      else if (target === "doc") streamDoc.value = text.substring(0, i)
      else streamReq.value = text.substring(0, i)
      if (i >= text.length) { clearInterval(timer); resolve() }
    }, speed)
    timers.push(timer)
  })
}

function setStreamPlaceholder() {
  const scopes = baseScopes.value
  streamCode.value = scopes.code ? "正在分析代码…" : ANALYSIS_NA.code
  streamDoc.value = scopes.doc ? "正在分析文档…" : ANALYSIS_NA.doc
  streamReq.value = scopes.req ? "正在分析需求符合性…" : ANALYSIS_NA.req
}

async function startAnimations() {
  if (!detail.value) return
  const scopes = analysisScopes.value
  revealedDeductions.value = new Set()

  if (scopes.code) {
    const text = detail.value.codeSummary || "未发现明显代码问题。"
    await typewriter("code", text, 18)
  } else {
    streamCode.value = ANALYSIS_NA.code
  }

  if (scopes.doc) {
    const text = detail.value.docSummary || "未发现明显文档问题。"
    await typewriter("doc", text, 18)
  } else {
    streamDoc.value = ANALYSIS_NA.doc
  }

  if (scopes.req) {
    const text = detail.value.reqSummary || "未发现明显需求偏差。"
    await typewriter("req", text, 18)
  } else {
    streamReq.value = ANALYSIS_NA.req
  }

  detail.value.deductions.forEach((_, i) => {
    const t = setTimeout(() => {
      revealedDeductions.value = new Set([...revealedDeductions.value, i])
    }, i * 120)
    timeouts.push(t)
  })
}

function markRead(i: number) {
  readItems.value = new Set([...readItems.value, i])
}

function cleanup() {
  timers.forEach(clearInterval)
  timeouts.forEach(clearTimeout)
  timers = []
  timeouts = []
  store.stopPolling()
}

function applyDetailStatically(data: IAIResultDetail) {
  detail.value = data
  const scopes = mergeScopesWithIssues(
    resolveAnalysisScopes(scopeMode.value || "file", scopeFileName.value),
    data.deductions ?? [],
  )
  streamCode.value = scopes.code
    ? (data.codeSummary || "未发现明显代码问题。")
    : ANALYSIS_NA.code
  streamDoc.value = scopes.doc
    ? (data.docSummary || "未发现明显文档问题。")
    : ANALYSIS_NA.doc
  streamReq.value = scopes.req
    ? (data.reqSummary || "未发现明显需求偏差。")
    : ANALYSIS_NA.req
  revealedDeductions.value = new Set((data.deductions ?? []).map((_, i) => i))
  aiPhase.value = "ready"
}

async function loadExistingResult(id: string): Promise<boolean> {
  if (store.aiResult?.status === "FAILED") {
    aiPhase.value = "failed"
    loadError.value = "AI 分析失败，请返回修改后重新提交"
    return false
  }
  await store.fetchAIResultDetail(id)
  if (store.aiResultDetail) {
    applyDetailStatically(store.aiResultDetail)
    return true
  }
  aiPhase.value = "failed"
  loadError.value = "暂无 AI 分析结果"
  return false
}

async function bootstrap() {
  const id = sid.value
  if (!id) return
  loadError.value = ""
  pageBooting.value = true

  try {
    await store.fetchEvaluation(id).catch(() => {})
    applyScopeFromEvaluation()
    await store.fetchAIResult(id)

    const submissionStatus = store.evaluation?.status ?? ""
    const aiStatus = store.aiResult?.status ?? null

    pageBooting.value = false

    if (!shouldUseLiveAIAnalysis(submissionStatus, aiStatus)) {
      await loadExistingResult(id)
      return
    }

    aiPhase.value = "analyzing"
    setStreamPlaceholder()

    await store.pollAIResult(id)
    await store.fetchAIResultDetail(id)
    detail.value = store.aiResultDetail
    await store.fetchEvaluation(id).catch(() => {})

    if (!detail.value) {
      aiPhase.value = "failed"
      loadError.value = "暂无 AI 分析结果"
      return
    }

    aiPhase.value = "streaming"
    await nextTick()
    await startAnimations()
    aiPhase.value = "ready"
  } catch (e: unknown) {
    if (store.aiResult?.status === "FAILED") {
      aiPhase.value = "failed"
      loadError.value = "AI 分析失败，请返回修改后重新提交"
      return
    }
    try {
      if (await loadExistingResult(id)) return
    } catch { /* ignore */ }
    aiPhase.value = "failed"
    loadError.value = (e as Error)?.message || "AI 分析超时，请稍后刷新页面"
  } finally {
    pageBooting.value = false
  }
}

onMounted(() => { bootstrap() })
onUnmounted(cleanup)

// -- computed --
const eval_ = computed(() => store.evaluation)
const taskId = computed(() => eval_.value?.taskId || (route.query.taskId as string) || "")
const teacherEval = computed(() => eval_.value?.teacherEvaluation ?? null)
const hasTeacher = computed(() => !!teacherEval.value)
const isTeacherConfirmed = computed(() => {
  const s = eval_.value?.status ?? ""
  return ["PUBLISHED", "COMPLETED"].includes(s)
})
const showFinalScore = computed(() => isTeacherConfirmed.value && finalScore.value != null)
const isRejected = computed(() => eval_.value?.status === "REJECTED")

const pageTitle = computed(() => {
  if (isGradeView.value) return eval_.value?.taskName || "我的成绩"
  return taskNameFromQuery.value || eval_.value?.taskName || "AI 诊断"
})
const headerSubtitle = computed(() => {
  if (aiPhase.value === "analyzing") return "AI 正在分析你的提交…"
  if (aiPhase.value === "streaming") return "正在生成诊断报告…"
  const at = detail.value?.analyzedAt
  return at ? `提交于 ${at.replace("T", " ").substring(0, 19)} · 分析完成` : "分析完成"
})

const canConfirmToTeacher = computed(() => {
  if (aiPhase.value !== "ready" || hasTeacher.value || isRejected.value) return false
  const s = eval_.value?.status ?? ""
  if (["CONFIRMED", "REVIEWING", "TEACHER_SCORING", "PUBLISHED", "COMPLETED"].includes(s)) return false
  return s === "AI_READY" || s === "AI_COMPLETED"
})

const isSubmittedToTeacher = computed(() => {
  const s = eval_.value?.status ?? ""
  return ["CONFIRMED", "REVIEWING", "TEACHER_SCORING", "PUBLISHED", "COMPLETED"].includes(s)
})

const aiScore = computed(() => {
  if (aiPhase.value === "analyzing" || aiPhase.value === "streaming") return null
  return detail.value?.aiScore ?? eval_.value?.aiEvaluation?.overallScore ?? 0
})

const finalScore = computed(() => {
  if (!isTeacherConfirmed.value) return null
  if (eval_.value?.finalScore != null) return eval_.value.finalScore
  return teacherEval.value?.overallScore ?? null
})

function scoreLevel(score: number | null): string {
  const s = score ?? 0
  if (s >= 80) return "good"
  if (s >= 60) return "warn"
  return "danger"
}

const aiScoreClass = computed(() => scoreLevel(aiScore.value))
const finalScoreClass = computed(() => scoreLevel(finalScore.value))

const deductions = computed(() => detail.value?.deductions ?? [])
const improvementNotes = computed(() => detail.value?.improvementNotes ?? [])
const totalDeductScore = computed(() =>
  detail.value?.totalDeductScore
  ?? deductions.value.reduce((sum, d) => sum + d.suggestDeduct, 0),
)

const dimBars = computed(() => {
  if (!teacherEval.value) return []
  return teacherEval.value.dimensions.map((d) => ({
    name: d.dimensionName,
    score: d.score,
    max: d.maxScore,
    pct: Math.round((d.score / d.maxScore) * 100),
  }))
})

interface LearningResource {
  title: string
  type: string
  icon: typeof BookOpen
  color: string
  bg: string
  border: string
  target: string
}

const learningResources = computed<LearningResource[]>(() => {
  if (!detail.value || aiPhase.value !== "ready") return []
  const issues = deductions.value.map((d) => d.issueType.toLowerCase())
  const items: LearningResource[] = []
  if (issues.some((s) => s.includes("分页") || s.includes("查询"))) {
    items.push({
      title: "MyBatis-Plus 分页插件使用指南",
      type: "文档",
      icon: BookOpen,
      color: "amber",
      bg: "bg-amber-50",
      border: "border-amber-100",
      target: "解决：分页查询未实现",
    })
  }
  if (issues.some((s) => s.includes("文档") || s.includes("注释") || s.includes("doc"))) {
    items.push({
      title: "软件设计文档规范模板 v2025",
      type: "文档",
      icon: FileText,
      color: "blue",
      bg: "bg-blue-50",
      border: "border-blue-100",
      target: "解决：文档格式不规范",
    })
  }
  if (issues.some((s) => s.includes("代码") || s.includes("规范") || s.includes("命名") || s.includes("异常"))) {
    items.push({
      title: "Java 代码规范手册（阿里巴巴版）",
      type: "文档",
      icon: Code2,
      color: "green",
      bg: "bg-green-50",
      border: "border-green-100",
      target: "解决：代码规范问题",
    })
  }
  return items
})

function agentLabel(t?: string): string {
  const u = (t || "").toUpperCase()
  if (u.includes("RULE")) return "规则"
  if (u.includes("CODE")) return "代码"
  if (u.includes("DOC")) return "文档"
  if (u.includes("REQ")) return "需求"
  return t || "分析"
}

function handleAskTeacher() {
  ElMessage.info("追问功能将在后续版本开放")
}

async function handleConfirmSubmit() {
  if (!taskId.value) {
    ElMessage.warning("缺少任务信息，请从任务详情重新进入")
    return
  }
  confirming.value = true
  try {
    await store.confirmSubmit(taskId.value, trialId.value || undefined)
    ElMessage.success("已提交教师审核")
    await store.fetchEvaluation(sid.value)
  } catch (err: unknown) {
    ElMessage.error((err as Error)?.message || "提交教师审核失败")
  } finally {
    confirming.value = false
  }
}

function handleContinueEdit() {
  if (taskId.value) {
    router.push(`/student/submit/${taskId.value}`)
    return
  }
  router.push("/student/tasks")
}

function reloadPage() {
  cleanup()
  bootstrap()
}

</script>

<template>
  <div class="ai-page">
    <ErrorState
      v-if="loadError && aiPhase === 'failed'"
      :message="loadError"
      @retry="reloadPage"
    />

    <template v-else-if="!sid">
      <div class="empty-state">
        <div class="empty-state-text">
          {{ isGradeView ? "缺少成绩参数，请从实训任务进入" : "缺少提交参数，请从成绩页进入" }}
        </div>
        <div class="mt-16">
          <BaseButton type="primary" @click="router.push('/student/tasks')">返回实训任务</BaseButton>
        </div>
      </div>
    </template>

    <template v-else>
      <div v-if="pageBooting" class="ai-page__booting">正在加载分析结果…</div>

      <template v-else>
      <!-- Header -->
      <div class="ai-header">
        <div class="ai-header__main">
          <div class="ai-header__titles">
            <h1 class="ai-header__title">{{ pageTitle }}</h1>
            <p class="ai-header__subtitle">{{ headerSubtitle }}</p>
          </div>
        </div>
        <span v-if="aiPhase === 'analyzing' || aiPhase === 'streaming'" class="analyzing-badge">
          <span class="analyzing-badge__dot" />
          AI 分析中
        </span>
        <span v-else-if="isTeacherConfirmed && hasTeacher" class="reviewed-badge">
          <CheckCircle :size="16" />
          教师已审核
        </span>
        <span v-else-if="isRejected" class="rejected-badge">已退回</span>
        <span v-else class="pending-badge">
          <Clock :size="16" />
          待教师审核
        </span>
      </div>

      <!-- Score Row -->
      <div class="score-row">
        <div class="final-score-card" :class="{ 'final-score-card--pending': aiScore === null }">
          <div class="score-duo" :class="{ 'score-duo--with-final': showFinalScore }">
            <div class="score-block">
              <div class="fs-label">AI 综合评分</div>
              <div class="fs-score" :class="aiScoreClass">
                <span v-if="aiScore === null" class="fs-score--pending">—</span>
                <span v-else>{{ aiScore }}</span>
              </div>
              <div class="fs-sub">/ 100 · 仅供参考</div>
            </div>

            <div v-if="showFinalScore" class="score-block score-block--final">
              <div class="fs-label">最终评分</div>
              <div class="fs-score" :class="finalScoreClass">
                <span v-if="finalScore === null" class="fs-score--pending">—</span>
                <span v-else>{{ finalScore }}</span>
              </div>
              <div class="fs-sub">/ 100 · 教师评定</div>
            </div>
          </div>

          <div
            v-if="showFinalScore && aiScore != null && finalScore != null && finalScore !== aiScore"
            class="fs-compare"
          >
            教师较 AI 参考分调整
            <span class="fs-compare__delta">
              {{ finalScore > aiScore ? "+" : "" }}{{ finalScore - aiScore }} 分
            </span>
          </div>

          <!-- Dimension breakdown -->
          <div v-if="dimBars.length > 0" class="dim-bars">
            <div v-for="d in dimBars" :key="d.name" class="dim-bar-item">
              <div class="dim-bar-label">
                <span>{{ d.name }}</span>
                <span class="dim-bar-score">{{ d.score }}<span class="dim-bar-max">/{{ d.max }}</span></span>
              </div>
              <div class="dim-bar-track">
                <div
                  class="dim-bar-fill"
                  :class="d.pct >= 80 ? 'fill--good' : d.pct >= 60 ? 'fill--warn' : 'fill--danger'"
                  :style="{ width: d.pct + '%' }"
                />
              </div>
            </div>
          </div>

          <!-- Rejection reason -->
          <div v-if="isRejected && (eval_?.rejectReason || teacherEval?.comment)" class="reject-reason">
            <div class="reject-reason__title">退回原因</div>
            <p>{{ eval_?.rejectReason || teacherEval?.comment }}</p>
          </div>
        </div>

        <div class="summary-cards">
          <div class="card" :class="{ 'card--na': !analysisScopes.code }">
            <div class="card-title">
              <Code2 :size="14" class="card-title-icon code-icon" />
              代码分析
            </div>
            <div
              class="streaming-text"
              :class="{
                'streaming-text--active': aiPhase === 'analyzing' && analysisScopes.code,
                'streaming-text--na': !analysisScopes.code,
              }"
            >{{ streamCode }}</div>
          </div>
          <div class="card" :class="{ 'card--na': !analysisScopes.doc }">
            <div class="card-title">
              <FileText :size="14" class="card-title-icon doc-icon" />
              文档分析
            </div>
            <div
              class="streaming-text"
              :class="{
                'streaming-text--active': aiPhase === 'analyzing' && analysisScopes.doc,
                'streaming-text--na': !analysisScopes.doc,
              }"
            >{{ streamDoc }}</div>
          </div>
          <div class="card" :class="{ 'card--na': !analysisScopes.req }">
            <div class="card-title">
              <Award :size="14" class="card-title-icon req-icon" />
              需求分析
            </div>
            <div
              class="streaming-text"
              :class="{
                'streaming-text--active': aiPhase === 'analyzing' && analysisScopes.req,
                'streaming-text--na': !analysisScopes.req,
              }"
            >{{ streamReq }}</div>
          </div>
        </div>
      </div>

      <!-- Two-column body -->
      <div class="body-grid">
        <!-- Left: Deductions -->
        <div class="card">
          <div class="card-header">
            <Cpu :size="16" class="text-primary" />
            <span class="card-header-title">
              扣分明细（{{ aiPhase === 'ready' ? deductions.length : '…' }} 项
              <template v-if="aiPhase === 'ready' && totalDeductScore > 0">，共 -{{ totalDeductScore }} 分</template>）
            </span>
          </div>
          <div v-if="aiPhase === 'analyzing' || aiPhase === 'streaming'" class="deduction-analyzing">
            <span class="deduction-analyzing__pulse" />
            正在识别问题条目…
          </div>
          <div v-else-if="deductions.length" class="deduction-list">
            <div
              v-for="(d, i) in deductions"
              :key="i"
              class="deduction-item analysis-block"
              :class="{
                revealed: revealedDeductions.has(i),
                'deduction-item--read': readItems.has(i),
              }"
            >
              <div class="ded-top">
                <div class="ded-top-left">
                  <span :class="`ded-tag ded-tag--${(d.agentType || 'CODE').toLowerCase()}`">{{ agentLabel(d.agentType) }}</span>
                  <span class="ded-type">{{ d.issueType }}</span>
                </div>
                <div class="ded-top-right">
                  <span class="ded-points">-{{ d.suggestDeduct }} 分</span>
                  <button
                    v-if="!readItems.has(i)"
                    class="mark-read-btn"
                    @click="markRead(i)"
                  >标记已读</button>
                  <span v-else class="read-badge">已读</span>
                </div>
              </div>
              <p class="ded-reason">{{ d.reason }}</p>
              <div v-if="d.filePath" class="ded-meta">
                {{ d.filePath }}<template v-if="d.lineNumber">:{{ d.lineNumber }}</template>
                · 置信度 {{ (d.confidence * 100).toFixed(0) }}%
              </div>
            </div>
          </div>
          <div v-else class="deduction-empty">暂无扣分明细</div>

          <div v-if="aiPhase === 'ready' && improvementNotes.length" class="improvement-notes">
            <div class="improvement-notes__title">改进建议（不扣分）</div>
            <div
              v-for="(note, ni) in improvementNotes"
              :key="`note-${ni}`"
              class="improvement-note"
            >
              <span :class="`ded-tag ded-tag--${(note.agentType || 'CODE').toLowerCase()}`">{{ agentLabel(note.agentType) }}</span>
              <span class="ded-type">{{ note.issueType }}</span>
              <p class="ded-reason">{{ note.reason }}</p>
            </div>
          </div>
        </div>

        <!-- Right: Teacher comment + Learning resources -->
        <div class="right-col">
          <!-- Teacher Comment -->
          <div v-if="hasTeacher && isTeacherConfirmed" class="card">
            <div class="teacher-comment-header">
              <div class="teacher-avatar">{{ teacherEval!.scoredBy.charAt(0) }}</div>
              <div>
                <div class="teacher-name">{{ teacherEval!.scoredBy }}老师评语</div>
                <div class="teacher-date">{{ teacherEval!.scoredAt }}</div>
              </div>
            </div>
            <div class="teacher-comment-body">
              {{ teacherEval!.comment }}
            </div>
            <button type="button" class="ask-teacher-btn" disabled @click="handleAskTeacher">
              <MessageCircle :size="14" />
              向教师追问
              <span class="ask-hint">后续版本</span>
            </button>
          </div>

          <!-- Teacher not yet reviewed -->
          <div v-else-if="!isRejected" class="card card--muted">
            <div class="card-header">
              <Clock :size="16" class="text-muted" />
              <span class="card-header-title">等待教师审核</span>
            </div>
            <p class="muted-text">AI 分析已完成，教师审核后将在此展示评语和最终得分。</p>
          </div>

          <!-- AI Learning Resources -->
          <div v-if="learningResources.length > 0" class="card">
            <div class="card-header">
              <Lightbulb :size="16" class="text-amber" />
              <span class="card-header-title">AI 推荐学习资源</span>
            </div>
            <div class="resource-list">
              <div
                v-for="(res, ri) in learningResources"
                :key="ri"
                :class="['resource-item', res.bg, res.border]"
              >
                <component :is="res.icon" :size="16" :class="`text-${res.color}`" />
                <div>
                  <div class="resource-title">{{ res.title }}</div>
                  <div class="resource-target">{{ res.target }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- Growth tip -->
          <div v-if="aiPhase === 'ready'" class="growth-tip">
            <TrendingUp :size="16" />
            <span>持续改进，查看 <a class="growth-link" @click="router.push('/student/analytics?tab=growth')">成长轨迹</a> 了解能力变化趋势</span>
          </div>
        </div>
      </div>

      <!-- Confirm Banner -->
      <div v-if="canConfirmToTeacher" class="confirm-banner">
        <div class="confirm-banner__text">
          以上为 AI 参考评分，非最终成绩。若满意可确认提交至教师审核；如需修改可继续编辑后重新提交。
        </div>
      </div>

      <!-- Actions -->
      <div class="actions-bar">
        <BaseButton
          v-if="canConfirmToTeacher"
          type="primary"
          :loading="confirming"
          @click="handleConfirmSubmit"
        >
          确认提交教师审核
        </BaseButton>
        <BaseButton v-if="canConfirmToTeacher" @click="handleContinueEdit">
          继续修改
        </BaseButton>
        <BaseButton v-if="isRejected" type="primary" @click="handleContinueEdit">
          重新提交
        </BaseButton>
        <BaseButton v-if="isSubmittedToTeacher" type="success" disabled>
          已递交教师审核
        </BaseButton>
        <BaseButton @click="router.push(taskId ? `/student/submit/${taskId}` : '/student/tasks')">
          返回任务
        </BaseButton>
        <BaseButton @click="router.push('/student/tasks')">
          返回实训任务
        </BaseButton>
      </div>
      </template>
    </template>
  </div>
</template>

<style scoped>
.ai-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 28px 28px;
  box-sizing: border-box;
  background: var(--color-bg, #F5F7FA);
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 16px;
  text-align: center;
}

.empty-state-text {
  font-size: 14px;
  color: var(--color-text-secondary, #64748b);
}

.mt-16 { margin-top: 16px; }

.ai-page__booting {
  padding: 48px 0;
  text-align: center;
  font-size: 14px;
  color: var(--color-text-secondary, #64748b);
}

.confirm-banner {
  margin-bottom: 16px;
  padding: 14px 18px;
  border-radius: var(--radius-md, 8px);
  background: var(--color-primary-light, #E6F4FF);
  border: 1px solid var(--color-primary-mid, #91CAFF);
}

.confirm-banner__text {
  font-size: 14px;
  line-height: 1.6;
  color: var(--color-text-primary, #1e293b);
}

/* Header */
.ai-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.ai-header__main {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  min-width: 0;
}

.ai-header__titles {
  min-width: 0;
}

.ai-header__title {
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--color-navy, #0B3A5C);
  margin: 0;
  line-height: 1.2;
}

.ai-header__subtitle {
  font-size: 13px;
  color: var(--color-text-secondary, #6B7280);
  margin: 6px 0 0;
}
.reviewed-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  background: #E6F7EF;
  color: #009A57;
  white-space: nowrap;
  margin-top: 4px;
}
.rejected-badge {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  background: #fee2e2;
  color: #991b1b;
  white-space: nowrap;
  margin-top: 4px;
}
.pending-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  background: #fef3c7;
  color: #92400e;
  white-space: nowrap;
  margin-top: 4px;
}

.analyzing-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  background: var(--color-primary-light, #E6F4FF);
  color: var(--color-primary, #1677FF);
  white-space: nowrap;
  margin-top: 4px;
}

.analyzing-badge__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-primary, #1677FF);
  animation: pulse-dot 1.2s ease-in-out infinite;
}

@keyframes pulse-dot {
  0%, 100% { opacity: 0.35; transform: scale(0.85); }
  50% { opacity: 1; transform: scale(1); }
}

.final-score-card--pending .fs-score--pending {
  animation: pulse-dot 1.2s ease-in-out infinite;
}

.card--na {
  background: var(--color-bg, #F5F7FA);
  border-color: var(--color-border, #E5E7EB);
}

.deduction-analyzing {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 28px 16px;
  font-size: 14px;
  color: var(--color-text-secondary, #64748b);
}

.deduction-analyzing__pulse {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--color-primary, #1677FF);
  animation: pulse-dot 1.2s ease-in-out infinite;
}

/* Score Row */
.score-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 24px; }

.final-score-card {
  background: linear-gradient(135deg, #0B3A5C 0%, #1677FF 55%, #722ED1 100%);
  border-radius: var(--radius-xl, 16px);
  padding: 24px;
  color: #fff;
  box-shadow: var(--shadow-card);
}
.fs-label { font-size: 13px; opacity: 0.8; margin-bottom: 4px; }
.score-duo {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.score-duo--with-final {
  flex-direction: row;
  align-items: flex-end;
  gap: 24px;
}
.score-block { flex: 1; min-width: 0; }
.score-block--final {
  padding-left: 24px;
  border-left: 1px solid rgba(255, 255, 255, 0.25);
}
.score-duo--with-final .score-block--final .fs-score {
  font-size: 64px;
}
.fs-score {
  font-family: var(--font-mono);
  font-size: 56px;
  font-weight: 700;
  letter-spacing: -0.03em;
  line-height: 1;
}
.fs-sub { font-size: 13px; opacity: 0.6; margin-top: 2px; }
.fs-compare {
  margin-top: 10px;
  font-size: 12px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  padding: 8px 12px;
  opacity: 0.9;
}
.fs-compare__delta { font-weight: 600; }

/* Dim bars inside score card */
.dim-bars {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.dim-bar-item { display: flex; flex-direction: column; gap: 2px; }
.dim-bar-label {
  display: flex; justify-content: space-between;
  font-size: 11px; opacity: 0.85;
}
.dim-bar-score { font-family: var(--font-mono); font-weight: 500; }
.dim-bar-max { opacity: 0.6; font-weight: 400; }
.dim-bar-track {
  height: 6px; border-radius: 3px;
  background: rgba(255, 255, 255, 0.2);
  overflow: hidden;
}
.dim-bar-fill { height: 100%; border-radius: 3px; background: #fff; transition: width 600ms ease; }
.fill--good { background: #fff; }
.fill--warn { background: rgba(255, 255, 255, 0.7); }
.fill--danger { background: rgba(255, 255, 255, 0.5); }

.reject-reason {
  margin-top: 14px;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  font-size: 12px;
  line-height: 1.6;
}
.reject-reason__title { font-weight: 600; margin-bottom: 4px; }

/* Summary Cards */
.summary-cards { display: flex; flex-direction: column; gap: 10px; }

.card {
  background: var(--color-card, #fff);
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: var(--radius-xl, 16px);
  box-shadow: var(--shadow-card);
  padding: 16px;
}
.card--muted { background: #f8fafc; }
.card-title {
  font-size: 14px;
  font-weight: 700;
  letter-spacing: -0.01em;
  color: var(--color-navy, #0B3A5C);
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.card-title-icon { flex-shrink: 0; }
.code-icon { color: #1677FF; }
.doc-icon { color: #00B96B; }
.req-icon { color: #FAAD14; }

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border, #e2e8f0);
  background: #f8fafc;
  border-radius: var(--radius-lg, 12px) var(--radius-lg, 12px) 0 0;
  margin: -16px -16px 12px;
}
.card-header-title { font-size: 13px; font-weight: 600; color: var(--color-text-primary, #1e293b); }
.text-primary { color: var(--color-primary, #1677FF); }
.text-muted { color: var(--color-text-placeholder, #94a3b8); }
.text-amber { color: #FAAD14; }

/* Streaming text */
.streaming-text {
  font-size: 12.5px;
  color: var(--color-text-secondary, #64748b);
  line-height: 1.7;
  white-space: pre-wrap;
  min-height: 2.8em;
}

.streaming-text--na {
  color: var(--color-text-placeholder, #94a3b8);
  font-style: normal;
}

.streaming-text--active::after {
  content: "▎";
  animation: blink 1s infinite;
  color: var(--color-primary, #1677FF);
  margin-left: 2px;
}

.streaming-text:empty::after {
  content: "▎";
  animation: blink 1s infinite;
  color: var(--color-primary, #1677FF);
}

/* Two-column body */
.body-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 24px; }
.right-col { display: flex; flex-direction: column; gap: 16px; }

/* Deduction List */
.deduction-list { display: flex; flex-direction: column; gap: 0; }
.deduction-empty {
  padding: 24px 16px;
  text-align: center;
  font-size: 14px;
  color: var(--color-text-secondary, #64748b);
}
.deduction-item {
  padding: 14px 16px;
  border-bottom: 1px solid #f1f5f9;
  transition: background 150ms;
}
.deduction-item:last-child { border-bottom: none; }
.deduction-item:hover { background: #f8fafc; }
.deduction-item--read { opacity: 0.7; }

.ded-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; }
.ded-top-left { display: flex; align-items: center; gap: 8px; }
.ded-top-right { display: flex; align-items: center; gap: 8px; }

.ded-tag {
  font-size: 10px; font-weight: 600;
  padding: 2px 6px; border-radius: 3px;
}
.ded-tag--rule { background: #f0f0f0; color: #595959; }
.ded-tag--code { background: #dbeafe; color: #1677FF; }
.ded-tag--doc { background: #E6F7EF; color: #009A57; }
.ded-tag--req { background: #fef3c7; color: #b45309; }

.ded-type { font-size: 13px; font-weight: 500; color: var(--color-text-primary, #1e293b); }
.ded-points { font-family: var(--font-mono); font-size: 13px; font-weight: 600; color: var(--color-danger, #FF4D4F); }
.mark-read-btn {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  border: 1px solid var(--color-border, #e2e8f0);
  background: #fff;
  color: var(--color-text-secondary, #64748b);
  cursor: pointer;
  white-space: nowrap;
}
.mark-read-btn:hover { background: #f1f5f9; }
.read-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #E6F7EF;
  color: #009A57;
}

.ded-reason { font-size: 12px; color: var(--color-text-secondary, #64748b); margin: 0 0 4px; line-height: 1.5; white-space: pre-wrap; }

.improvement-notes {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border, #E5E7EB);
}

.improvement-notes__title {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-secondary, #64748b);
  margin-bottom: 10px;
}

.improvement-note {
  padding: 10px 0;
  border-bottom: 1px solid #f1f5f9;
}

.improvement-note:last-child { border-bottom: none; }

.improvement-note .ded-type { margin-left: 8px; }
.ded-meta { font-size: 11px; color: var(--color-text-placeholder, #94a3b8); }

/* Teacher comment */
.teacher-comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.teacher-avatar {
  width: 32px; height: 32px;
  border-radius: 50%;
  background: #F9F0FF;
  color: #722ED1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}
.teacher-name { font-size: 13px; font-weight: 600; color: var(--color-text-primary, #1e293b); }
.teacher-date { font-size: 11px; color: var(--color-text-placeholder, #94a3b8); }
.teacher-comment-body {
  background: #f8fafc;
  border-radius: 8px;
  padding: 12px;
  font-size: 13px;
  color: var(--color-text-secondary, #64748b);
  line-height: 1.6;
}
.ask-teacher-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  font-size: 12px;
  color: var(--color-text-secondary, #64748b);
  background: #F8FAFC;
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: 10px;
  cursor: not-allowed;
  padding: 8px 12px;
  opacity: 0.9;
}
.ask-hint {
  font-size: 11px;
  color: var(--color-text-placeholder, #94a3b8);
}

/* Muted text */
.muted-text { font-size: 13px; color: var(--color-text-placeholder, #94a3b8); line-height: 1.6; }

/* Learning Resources */
.resource-list { display: flex; flex-direction: column; gap: 8px; }
.resource-item {
  display: flex; align-items: flex-start; gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid;
  cursor: default;
  transition: background 150ms;
  flex-shrink: 0;
}
.resource-item:hover { filter: brightness(0.97); }
.resource-title { font-size: 12px; font-weight: 500; color: var(--color-text-primary, #1e293b); }
.resource-target { font-size: 11px; color: var(--color-text-placeholder, #94a3b8); margin-top: 2px; }

/* Growth tip */
.growth-tip {
  display: flex; align-items: center; gap: 8px;
  padding: 12px 16px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: var(--radius-md, 8px);
  font-size: 12px;
  color: #009A57;
}
.growth-link { color: var(--color-primary, #1677FF); cursor: pointer; font-weight: 500; }
.growth-link:hover { text-decoration: underline; }

/* Actions */
.actions-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: auto;
  padding-top: 24px;
  flex-shrink: 0;
}

/* Animation */
.analysis-block {
  opacity: 0;
  transform: translateY(10px);
  transition: all 0.5s ease;
}
.analysis-block.revealed {
  opacity: 1;
  transform: translateY(0);
}

/* Responsive */
@media (max-width: 1023.98px) {
  .ai-page {
    padding: 16px 20px 24px;
  }

  .score-row { grid-template-columns: 1fr; }
  .body-grid { grid-template-columns: 1fr; }
  .score-duo--with-final { flex-direction: column; align-items: stretch; }
  .score-block--final {
    padding-left: 0;
    padding-top: 16px;
    border-left: none;
    border-top: 1px solid rgba(255, 255, 255, 0.25);
  }
}

@media (max-width: 767.98px) {
  .ai-page {
    padding: 12px 16px 20px;
  }

  .ai-header {
    flex-direction: column;
    align-items: stretch;
  }

  .actions-bar {
    flex-direction: column;
  }

  .actions-bar :deep(.base-button) {
    width: 100%;
  }
}

@keyframes blink { 0%,50% { opacity:1; } 51%,100% { opacity:0; } }
</style>
