<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from "vue"
import { useRoute, useRouter } from "vue-router"
import { useStudentStore } from "@/stores/useStudentStore"
import { ElMessage } from "element-plus"
import {
  CheckCircle, Clock, Cpu, Lightbulb, BookOpen, FileText, Code2,
  MessageCircle, Award, TrendingUp,
} from "lucide-vue-next"
import PageHeader from "@/components/layout/PageHeader.vue"
import BaseButton from "@/components/base/BaseButton.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"

const route = useRoute()
const router = useRouter()
const store = useStudentStore()

const sid = (route.query.sid as string) || ""
const pageLoading = ref(false)
const loadError = ref("")

const streamCode = ref("")
const streamDoc = ref("")
const streamReq = ref("")
const revealedDeductions = ref<Set<number>>(new Set())
const readItems = ref<Set<number>>(new Set())

let timers: ReturnType<typeof setInterval>[] = []
let timeouts: ReturnType<typeof setTimeout>[] = []

function typewriter(target: "code" | "doc" | "req", text: string, speed: number): Promise<void> {
  return new Promise((resolve) => {
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

async function startAnimations() {
  if (!store.aiResultDetail) return
  await typewriter("code", store.aiResultDetail.codeSummary, 20)
  await typewriter("doc", store.aiResultDetail.docSummary, 20)
  await typewriter("req", store.aiResultDetail.reqSummary, 20)

  store.aiResultDetail.deductions.forEach((_, i) => {
    const t = setTimeout(() => {
      revealedDeductions.value = new Set([...revealedDeductions.value, i])
    }, i * 150)
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
}

onMounted(async () => {
  if (!sid) return
  pageLoading.value = true
  loadError.value = ""
  try {
    await Promise.all([
      store.fetchAIResultDetail(sid),
      store.fetchEvaluation(sid),
    ])
    await nextTick()
    startAnimations()
  } catch (e: unknown) {
    loadError.value = (e as Error)?.message || "加载 AI 诊断失败"
  } finally {
    pageLoading.value = false
  }
})

onUnmounted(cleanup)

// -- computed --
const eval_ = computed(() => store.evaluation)
const teacherEval = computed(() => eval_.value?.teacherEvaluation ?? null)
const hasTeacher = computed(() => !!teacherEval.value)
const isRejected = computed(() => eval_.value?.status === "REJECTED")

const displayScore = computed(() => {
  if (eval_.value?.finalScore != null) return eval_.value.finalScore
  return store.aiResultDetail?.aiScore ?? 0
})

const aiScore = computed(() => store.aiResultDetail?.aiScore ?? 0)

const scoreClass = computed(() => {
  const s = displayScore.value
  if (s >= 80) return "good"
  if (s >= 60) return "warn"
  return "danger"
})

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
  if (!store.aiResultDetail) return []
  const issues = store.aiResultDetail.deductions.map((d) => d.issueType.toLowerCase())
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
  if (u.includes("CODE")) return "代码"
  if (u.includes("DOC")) return "文档"
  if (u.includes("REQ")) return "需求"
  return t || "分析"
}

function handleAskTeacher() {
  ElMessage.info("追问功能将在后续版本开放")
}
</script>

<template>
  <div class="ai-page">
    <LoadingState v-if="pageLoading" text="加载 AI 诊断..." />

    <ErrorState
      v-else-if="loadError"
      :message="loadError"
      @retry="() => { location.reload() }"
    />

    <template v-else-if="!sid">
      <div class="empty-state">
        <div class="empty-state-text">缺少提交参数，请从成绩页进入</div>
        <div class="mt-16">
          <BaseButton type="primary" @click="router.push('/student/tasks')">返回实训任务</BaseButton>
        </div>
      </div>
    </template>

    <template v-else-if="store.aiResultDetail">
      <!-- Header -->
      <div class="ai-header">
        <div>
          <PageHeader
            title="AI 诊断结果"
            :subtitle="`${store.aiResultDetail.taskName} · 提交于 ${store.aiResultDetail.analyzedAt} · 分析完成`"
          />
        </div>
        <span v-if="hasTeacher" class="reviewed-badge">
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
        <!-- Final Score Card -->
        <div class="final-score-card">
          <div class="fs-label">{{ hasTeacher ? '最终得分（教师审核后）' : 'AI 综合评分' }}</div>
          <div class="fs-score" :class="scoreClass">{{ displayScore }}</div>
          <div class="fs-sub">/ 100</div>

          <div v-if="hasTeacher && eval_?.aiEvaluation" class="fs-compare">
            AI 建议：{{ eval_.aiEvaluation.overallScore }} → 教师调整：{{ displayScore }}
            <span v-if="displayScore !== aiScore" class="fs-compare__delta">
              （{{ displayScore > aiScore ? '+' : '' }}{{ displayScore - aiScore }} 分）
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

        <!-- Summary Cards -->
        <div class="summary-cards">
          <div class="card">
            <div class="card-title">
              <Code2 :size="14" class="card-title-icon code-icon" />
              代码分析
            </div>
            <div class="streaming-text">{{ streamCode }}</div>
          </div>
          <div class="card">
            <div class="card-title">
              <FileText :size="14" class="card-title-icon doc-icon" />
              文档分析
            </div>
            <div class="streaming-text">{{ streamDoc }}</div>
          </div>
          <div class="card">
            <div class="card-title">
              <Award :size="14" class="card-title-icon req-icon" />
              需求分析
            </div>
            <div class="streaming-text">{{ streamReq }}</div>
          </div>
        </div>
      </div>

      <!-- Two-column body -->
      <div class="body-grid">
        <!-- Left: Deductions -->
        <div class="card">
          <div class="card-header">
            <Cpu :size="16" class="text-primary" />
            <span class="card-header-title">AI 诊断问题（{{ store.aiResultDetail.deductions.length }} 项）</span>
          </div>
          <div class="deduction-list">
            <div
              v-for="(d, i) in store.aiResultDetail.deductions"
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
                  <span class="ded-points">-{{ d.suggestDeduct }}分</span>
                  <button
                    v-if="!readItems.has(i)"
                    class="mark-read-btn"
                    @click="markRead(i)"
                  >标记已读</button>
                  <span v-else class="read-badge">已读</span>
                </div>
              </div>
              <p class="ded-reason">{{ d.reason }}</p>
              <div class="ded-meta">{{ d.filePath }}:{{ d.lineNumber }} · 置信度 {{ (d.confidence * 100).toFixed(0) }}%</div>
            </div>
          </div>
        </div>

        <!-- Right: Teacher comment + Learning resources -->
        <div class="right-col">
          <!-- Teacher Comment -->
          <div v-if="hasTeacher" class="card">
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
          <div class="growth-tip">
            <TrendingUp :size="16" />
            <span>持续改进，查看 <a class="growth-link" @click="router.push('/student/growth')">成长中心</a> 了解能力变化趋势</span>
          </div>
        </div>
      </div>

      <!-- Actions -->
      <div class="actions-bar">
        <BaseButton v-if="isRejected" type="primary" @click="router.push(`/student/submit/${store.aiResultDetail.taskId}`)">
          重新提交
        </BaseButton>
        <BaseButton @click="router.push(`/student/tasks/${store.aiResultDetail.taskId}`)">
          返回任务详情
        </BaseButton>
        <BaseButton type="primary" @click="router.push('/student/tasks')">
          返回实训任务
        </BaseButton>
      </div>
    </template>

    <div v-else class="empty-state">
      <div class="empty-state-text">暂无 AI 诊断数据</div>
      <div class="mt-16">
        <BaseButton type="primary" @click="router.push('/student/tasks')">返回实训任务</BaseButton>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ai-page { /* width/centering handled by AppLayout */ }

/* Header */
.ai-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
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

.ded-reason { font-size: 12px; color: var(--color-text-secondary, #64748b); margin: 0 0 4px; line-height: 1.5; }
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
.actions-bar { display: flex; gap: 10px; }

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
@media (max-width: 1024px) {
  .score-row { grid-template-columns: 1fr; }
  .body-grid { grid-template-columns: 1fr; }
}

@keyframes blink { 0%,50% { opacity:1; } 51%,100% { opacity:0; } }
</style>
