<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from "vue"
import { useRoute, useRouter } from "vue-router"
import { useStudentStore } from "@/stores/useStudentStore"
import PageHeader from "@/components/layout/PageHeader.vue"

const route = useRoute()
const router = useRouter()
const store = useStudentStore()

const sid = (route.query.sid as string) || ""

const streamCode = ref("")
const streamDoc = ref("")
const streamReq = ref("")
const revealedDeductions = ref<Set<number>>(new Set())

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
      if (i >= text.length) {
        clearInterval(timer)
        resolve()
      }
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

function cleanup() {
  timers.forEach(clearInterval)
  timeouts.forEach(clearTimeout)
  timers = []
  timeouts = []
}

onMounted(async () => {
  if (!sid) return
  await store.fetchAIResultDetail(sid)
  await nextTick()
  startAnimations()
})

onUnmounted(cleanup)

const scoreClass = computed(() => {
  const s = store.aiResultDetail?.aiScore ?? 0
  if (s >= 80) return "good"
  if (s >= 60) return "warn"
  return "danger"
})

const gradeLabel = computed(() => {
  const s = store.aiResultDetail?.aiScore ?? 0
  if (s >= 90) return "优秀"
  if (s >= 80) return "良好"
  if (s >= 60) return "及格"
  return "不及格"
})

const sevLabels: Record<string, string> = { critical: "严重", major: "主要", minor: "轻微" }
</script>

<template>
  <div>
    <PageHeader
      title="AI 分析结果"
      :subtitle="store.aiResultDetail
        ? `${store.aiResultDetail.taskName} · 分析时间 ${store.aiResultDetail.analyzedAt}`
        : '加载中...'"
    />

    <template v-if="store.aiResultDetail">
      <div class="content-grid mb-24">
        <!-- Score Card -->
        <div class="ai-score-card">
          <div style="font-size:12px;color:var(--color-text-tertiary,#94a3b8);text-transform:uppercase;letter-spacing:0.04em;margin-bottom:4px;">AI 综合评分</div>
          <div class="ai-score-big" :class="scoreClass">{{ store.aiResultDetail.aiScore.toFixed(1) }}</div>
          <div style="margin-top:4px;">
            <span :class="`badge badge--${scoreClass === 'good' ? 'success' : scoreClass === 'warn' ? 'warning' : 'danger'}`">{{ gradeLabel }}</span>
          </div>
          <div style="margin-top:12px;font-size:12px;color:var(--color-text-tertiary,#94a3b8);">共发现 {{ store.aiResultDetail.totalDeductions }} 个问题</div>
          <div style="margin-top:4px;font-size:12px;color:var(--color-text-tertiary,#94a3b8);">扣分合计: -{{ store.aiResultDetail.totalDeductScore }} 分</div>
          <div style="margin-top:12px;font-size:11px;color:var(--color-text-tertiary,#94a3b8);">模型: {{ store.aiResultDetail.modelVersion }}</div>
        </div>

        <!-- Summary Cards -->
        <div style="display:flex;flex-direction:column;gap:12px;">
          <div class="card"><div class="card-title">代码分析</div><div class="streaming-text" style="margin-top:6px;">{{ streamCode }}</div></div>
          <div class="card"><div class="card-title">文档分析</div><div class="streaming-text" style="margin-top:6px;">{{ streamDoc }}</div></div>
          <div class="card"><div class="card-title">需求分析</div><div class="streaming-text" style="margin-top:6px;">{{ streamReq }}</div></div>
        </div>
      </div>

      <!-- Deduction List -->
      <div class="section mb-24">
        <div class="section-header">
          <h2 class="section-title">AI 扣分明细</h2>
          <span style="font-size:12px;color:var(--color-text-tertiary,#94a3b8);">共 {{ store.aiResultDetail.deductions.length }} 项</span>
        </div>
        <div class="deduction-list">
          <div
            v-for="(d, i) in store.aiResultDetail.deductions"
            :key="i"
            class="deduction-item analysis-block"
            :class="{ revealed: revealedDeductions.has(i) }"
          >
            <div class="deduction-header">
              <span :class="`deduction-agent ${d.agentType.toLowerCase()}`">{{ d.agentType }}</span>
              <div :class="`severity-dot ${d.severity}`"></div>
              <span style="font-size:12px;font-weight:500;">{{ sevLabels[d.severity] || d.severity }}</span>
              <span style="font-size:12px;color:var(--color-text-secondary,#64748b);">{{ d.issueType }}</span>
              <span style="margin-left:auto;font-family:var(--font-mono);font-size:14px;font-weight:600;color:var(--color-danger,#ef4444);">- {{ d.suggestDeduct }}</span>
            </div>
            <div style="font-size:12.5px;color:var(--color-text-secondary,#64748b);margin-bottom:4px;">{{ d.reason }}</div>
            <div style="font-size:11px;color:var(--color-text-tertiary,#94a3b8);">{{ d.filePath }}:{{ d.lineNumber }} · 置信度 {{ (d.confidence * 100).toFixed(0) }}%</div>
          </div>
        </div>
      </div>

      <!-- Actions -->
      <div style="display:flex;gap:10px;">
        <button class="btn btn-outline" @click="router.push(`/student/tasks/${store.aiResultDetail.taskId}`)">返回任务详情</button>
        <button class="btn btn-ghost" @click="router.push('/student/tasks')">返回任务中心</button>
      </div>
    </template>

    <div v-else-if="!sid" class="empty-state">
      <div class="empty-state-text">缺少提交参数，请从任务详情进入</div>
      <div class="mt-16"><button class="btn btn-primary" @click="router.push('/student/tasks')">返回任务中心</button></div>
    </div>
  </div>
</template>

<style scoped>
.ai-score-card {
  text-align: center;
  padding: 24px;
  background: var(--color-card, #fff);
  border: 1px solid var(--color-border, #e2e8f0);
  border-radius: var(--radius-md, 8px);
}
.ai-score-big {
  font-family: var(--font-mono);
  font-size: 48px;
  font-weight: 700;
  letter-spacing: -0.03em;
}
.ai-score-big.good { color: var(--color-success, #10b981); }
.ai-score-big.warn { color: var(--color-warning, #f59e0b); }
.ai-score-big.danger { color: var(--color-danger, #ef4444); }

.deduction-list { margin-top: 16px; }
.deduction-item {
  background: var(--color-card, #fff);
  border: 1px solid var(--color-border, #e2e8f0);
  border-radius: var(--radius-sm, 4px);
  padding: 12px 16px;
  margin-bottom: 8px;
  transition: box-shadow 0.2s;
}
.deduction-item:hover { box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.1)); }
.deduction-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.deduction-agent {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 3px;
}
.deduction-agent.code { background: #dbeafe; color: #3b82f6; }
.deduction-agent.doc { background: #d1fae5; color: #047857; }
.deduction-agent.req { background: #fef3c7; color: #b45309; }

.severity-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.severity-dot.critical { background: var(--color-danger, #ef4444); }
.severity-dot.major { background: var(--color-warning, #f59e0b); }
.severity-dot.minor { background: var(--color-text-tertiary, #94a3b8); }

.streaming-text {
  font-size: 13px;
  color: var(--color-text-secondary, #64748b);
  line-height: 1.7;
  white-space: pre-wrap;
}
.streaming-text:empty::after {
  content: "▎";
  animation: blink 1s infinite;
  color: var(--color-primary, #3b82f6);
}

.analysis-block {
  opacity: 0;
  transform: translateY(10px);
  transition: all 0.5s ease;
}
.analysis-block.revealed {
  opacity: 1;
  transform: translateY(0);
}

.card { background: var(--color-card, #fff); border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-md, 8px); padding: 16px; }
.card-title { font-size: 14px; font-weight: 600; margin-bottom: 0; }

@keyframes blink { 0%,50% { opacity:1; } 51%,100% { opacity:0; } }
</style>
