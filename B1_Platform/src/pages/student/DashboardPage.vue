<script setup lang="ts">
import { ref, onMounted, computed } from "vue"
import { useRouter } from "vue-router"
import { useStudentStore } from "@/stores/useStudentStore"
import { useUserStore } from "@/stores/useUserStore"
import {
  ClipboardList, CheckCircle2, Award, Sparkles, AlertTriangle,
  Upload, ArrowRight, BookOpen,
} from "lucide-vue-next"
import LineChart from "@/components/chart/LineChart.vue"
import RadarChart from "@/components/chart/RadarChart.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"
import { navigateStudentTaskAction } from "@/utils/studentTaskNavigation"
import { useBreakpoint } from "@/composables/useBreakpoint"

const router = useRouter()
const studentStore = useStudentStore()
const userStore = useUserStore()
const { isXs } = useBreakpoint()

const chartH = computed(() => (isXs.value ? "220px" : "280px"))
const radarH = computed(() => (isXs.value ? "240px" : "260px"))

const loadError = ref("")

const today = computed(() => {
  const d = new Date()
  const weekDays = ["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"]
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 ${weekDays[d.getDay()]}`
})

const data = computed(() => studentStore.dashboardData)
const stats = computed(() => data.value?.stats)

const scoreSpark = computed(() => data.value?.scoreTrend?.myScores?.slice(-7) ?? [])

const firstPendingTaskId = computed(() => {
  const tasks = data.value?.recentTasks ?? []
  const pending = tasks.find((t) =>
    t.status === "NOT_SUBMITTED" || t.status === "REJECTED",
  )
  return pending?.taskId ?? tasks[0]?.taskId
})

async function loadDashboard() {
  loadError.value = ""
  try {
    await studentStore.fetchDashboard()
  } catch (err: unknown) {
    loadError.value = (err as Error)?.message || "加载仪表盘数据失败"
  }
}

onMounted(() => { loadDashboard() })

function goTo(path: string) {
  router.push(path)
}

function goSubmitCta() {
  if (firstPendingTaskId.value) {
    goTo(`/student/submit/${firstPendingTaskId.value}`)
  } else {
    goTo("/student/tasks")
  }
}

function formatDeadline(dt: string): string {
  if (!dt) return "—"
  return dt.replace("T", " ").substring(0, 16)
}

function formatNearest(dateStr: string | null): string {
  if (!dateStr) return "暂无临近截止"
  const m = dateStr.match(/(\d{2})-(\d{2})$/) || dateStr.match(/(\d{2})-(\d{2})/)
  if (m) return `最近截止 ${m[1]}-${m[2]}`
  return `最近截止 ${dateStr}`
}

function statusMeta(status: string): { label: string; action: string; tone: "danger" | "ok" | "ai" | "muted" } {
  switch (status) {
    case "NOT_SUBMITTED":
      return { label: "进行中 · 未提交", action: "去提交", tone: "danger" }
    case "DRAFT":
      return { label: "试交中", action: "继续试交", tone: "ai" }
    case "REJECTED":
      return { label: "已退回", action: "去修改", tone: "danger" }
    case "ANALYZING":
    case "AI_EVALUATING":
      return { label: "AI 分析中", action: "查看进度", tone: "ai" }
    case "AI_READY":
    case "AI_COMPLETED":
      return { label: "待确认提交", action: "去确认", tone: "ok" }
    case "CONFIRMED":
      return { label: "已递交教师", action: "查看详情", tone: "muted" }
    case "REVIEWING":
    case "TEACHER_SCORING":
      return { label: "教师复核中", action: "查看详情", tone: "muted" }
    case "PUBLISHED":
    case "COMPLETED":
      return { label: "已发布成绩", action: "查看成绩", tone: "ok" }
    case "INCOMPLETE":
      return { label: "未完成", action: "查看详情", tone: "danger" }
    default:
      return { label: status || "未知", action: "查看", tone: "muted" }
  }
}

function taskAction(item: { taskId: string; status: string; submissionId?: string | null }) {
  navigateStudentTaskAction(router, {
    taskId: item.taskId,
    status: item.status,
    submissionId: item.submissionId,
  })
}
</script>

<template>
  <div class="dash">
    <div class="dash-hero">
      <div>
        <h1 class="dash-hero__title">欢迎回来，{{ userStore.userName || "同学" }}!</h1>
        <p class="dash-hero__sub">{{ today }} · 继续推进进行中的实训任务</p>
      </div>
      <button type="button" class="cta-primary" @click="goSubmitCta">
        <Upload :size="16" />
        去提交成果
      </button>
    </div>

    <div v-if="studentStore.dashboardLoading" class="dash-state">
      <LoadingState text="正在加载仪表盘数据..." />
    </div>
    <div v-else-if="loadError" class="dash-state">
      <ErrorState :message="loadError" @retry="loadDashboard" />
    </div>

    <template v-else-if="data && stats">
      <div class="stat-grid">
        <section class="stat-group">
          <h2 class="section-label">我的任务</h2>
          <div class="stat-pair">
            <article class="stat-card" @click="goTo('/student/tasks')">
              <div class="stat-card__top">
                <div>
                  <div class="stat-label">待完成任务</div>
                  <div class="stat-value" :class="{ danger: stats.pendingTasks > 0 }">
                    {{ stats.pendingTasks }}
                  </div>
                </div>
                <div class="stat-icon y"><ClipboardList :size="18" /></div>
              </div>
              <div class="stat-foot">
                <span>{{ formatNearest(stats.nearestDeadline) }}</span>
              </div>
            </article>
            <article class="stat-card" @click="goTo('/student/tasks')">
              <div class="stat-card__top">
                <div>
                  <div class="stat-label">已完成任务</div>
                  <div class="stat-value success">{{ stats.completedTasks }}</div>
                </div>
                <div class="stat-icon g"><CheckCircle2 :size="18" /></div>
              </div>
              <div class="stat-foot">
                <span>本学期累计</span>
              </div>
            </article>
          </div>
        </section>

        <section class="stat-group">
          <h2 class="section-label">学习状态</h2>
          <div class="stat-triple">
            <article class="stat-card" @click="goTo('/student/analytics?tab=reports')">
              <div class="stat-card__top">
                <div>
                  <div class="stat-label">平均成绩</div>
                  <div class="stat-value">
                    {{ stats.averageScore != null ? stats.averageScore.toFixed(1) : "—" }}
                  </div>
                </div>
                <div class="stat-icon b"><Award :size="18" /></div>
              </div>
              <div class="stat-foot between">
                <span>
                  班级均值
                  {{ stats.classAverageScore != null ? stats.classAverageScore.toFixed(1) : "—" }}
                </span>
                <div v-if="scoreSpark.length" class="spark">
                  <b
                    v-for="(h, i) in scoreSpark"
                    :key="i"
                    :style="{ height: Math.max(20, Math.min(100, Number(h) || 0)) + '%' }"
                  />
                </div>
              </div>
            </article>
            <article class="stat-card" @click="goTo('/student/tasks')">
              <div class="stat-card__top">
                <div>
                  <div class="stat-label">AI 分析中</div>
                  <div class="stat-value">{{ stats.analyzingCount }}</div>
                </div>
                <div class="stat-icon ai"><Sparkles :size="18" /></div>
              </div>
              <div class="stat-foot">
                <span>预计 {{ stats.analyzingEtaMinutes }} 分钟</span>
              </div>
            </article>
            <article class="stat-card" @click="goTo('/student/analytics?tab=growth')">
              <div class="stat-card__top">
                <div>
                  <div class="stat-label">待改进项</div>
                  <div class="stat-value" :class="{ danger: stats.improvementCount > 0 }">
                    {{ stats.improvementCount }}
                  </div>
                </div>
                <div class="stat-icon r"><AlertTriangle :size="18" /></div>
              </div>
              <div class="stat-foot">
                <span>来自最近 AI 建议</span>
              </div>
            </article>
          </div>
        </section>
      </div>

      <div class="mid-grid">
        <section class="panel chart-panel">
          <div class="panel__head">
            <h3 class="panel__title">成绩趋势</h3>
            <div class="legend">
              <span><i class="dot solid" />我的成绩</span>
              <span><i class="dot soft" />班级平均</span>
            </div>
          </div>
          <LineChart
            :x-axis="data.scoreTrend?.xAxis ?? []"
            :y-min="0"
            :y-max="100"
            :series-data="[
              { name: '我的成绩', data: data.scoreTrend?.myScores ?? [], color: '#1677FF' },
              { name: '班级平均', data: data.scoreTrend?.classAvg ?? [], color: '#9CA3AF' },
            ]"
            :height="chartH"
          />
        </section>

        <section class="panel chart-panel">
          <div class="panel__head">
            <h3 class="panel__title">能力维度</h3>
            <span class="panel__chip">本学期</span>
          </div>
          <RadarChart
            :categories="data.radarData?.categories ?? []"
            :series-data="[
              { name: '我的能力', data: data.radarData?.myScores ?? [], color: '#1677FF' },
              { name: '班级均值', data: data.radarData?.classAvg ?? [], color: '#00B96B' },
            ]"
            :height="radarH"
          />
        </section>
      </div>

      <div class="bottom-grid">
        <section class="panel tasks-panel">
          <div class="panel__head">
            <h3 class="panel__title">近期任务</h3>
            <button type="button" class="link-all" @click="goTo('/student/tasks')">
              查看全部 <ArrowRight :size="14" />
            </button>
          </div>
          <div class="task-list">
            <div
              v-for="item in (data.recentTasks ?? [])"
              :key="item.taskId"
              class="task-item"
              :class="{ urgent: item.status === 'NOT_SUBMITTED' || item.status === 'REJECTED' }"
            >
              <div class="task-item__body">
                <div class="task-item__title">{{ item.taskName }}</div>
                <div class="task-item__meta">
                  <span>{{ item.courseName }}</span>
                  <span>截止 {{ formatDeadline(item.deadline) }}</span>
                  <span>{{ statusMeta(item.status).label }}</span>
                  <span v-if="item.score != null">得分 {{ item.score }}</span>
                </div>
              </div>
              <button
                type="button"
                class="badge"
                :class="statusMeta(item.status).tone"
                @click="taskAction(item)"
              >
                {{ statusMeta(item.status).action }}
              </button>
            </div>
            <div v-if="!(data.recentTasks?.length)" class="empty">暂无近期任务</div>
          </div>
        </section>

        <section class="panel">
          <div class="panel__head">
            <h3 class="panel__title">AI 学习建议</h3>
            <span class="panel__chip ai">AI</span>
          </div>
          <ul class="suggest-list">
            <li v-for="(tip, i) in (data.aiSuggestions ?? [])" :key="i">{{ tip }}</li>
          </ul>
          <div v-if="!(data.aiSuggestions?.length)" class="empty">暂无建议，完成提交后生成</div>
          <p class="ai-note">仅建议，不构成最终成绩 · 终评由教师确认</p>
          <div class="quick-links">
            <button type="button" class="mini" @click="goTo('/student/courses')">
              <div class="mini__k"><BookOpen :size="14" />我的课程</div>
              <div class="mini__v linkish">进入</div>
            </button>
            <button type="button" class="mini" @click="goTo('/student/analytics?tab=growth')">
              <div class="mini__k"><Sparkles :size="14" />成长档案</div>
              <div class="mini__v linkish">查看</div>
            </button>
          </div>
        </section>
      </div>
    </template>
  </div>
</template>

<style scoped>
.dash {
  width: 100%;
  min-width: 0;
}

.dash-state { padding: 48px 0; }

.dash-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin: 4px 0 28px;
}
.dash-hero__title {
  margin: 0;
  font-size: var(--font-size-hero, 30px);
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--color-navy, #0B3A5C);
  line-height: 1.15;
}
.dash-hero__sub {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--color-text-secondary);
}
.cta-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: none;
  border-radius: 10px;
  padding: 12px 16px;
  min-height: var(--touch-min, 44px);
  background: var(--color-primary, #1677FF);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 8px 18px rgba(22, 119, 255, 0.25);
  transition: background var(--transition-fast), transform var(--transition-fast);
  flex-shrink: 0;
}
.cta-primary:hover {
  background: var(--color-primary-hover, #0958D9);
  transform: translateY(-1px);
}

.section-label {
  margin: 0 0 12px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--color-text-placeholder);
}

.stat-grid {
  display: grid;
  grid-template-columns: 1.05fr 1.55fr;
  gap: 18px;
  margin-bottom: 18px;
}
.stat-pair, .stat-triple {
  display: grid;
  gap: 14px;
}
.stat-pair { grid-template-columns: 1fr 1fr; }
.stat-triple { grid-template-columns: repeat(3, 1fr); }

.stat-card, .panel {
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl, 20px);
  box-shadow: var(--shadow-card);
  min-width: 0;
}
.stat-card {
  cursor: pointer;
  padding: 18px;
  min-height: 128px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: box-shadow var(--transition-fast), transform var(--transition-fast);
}
.stat-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
}
.stat-card__top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}
.stat-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
}
.stat-value {
  margin-top: 8px;
  font-family: var(--font-mono);
  font-size: 30px;
  font-weight: 700;
  letter-spacing: -0.04em;
  color: var(--color-text-primary);
  line-height: 1;
}
.stat-value.danger { color: var(--color-danger); }
.stat-value.success { color: var(--color-success); }
.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  flex-shrink: 0;
}
.stat-icon.g { background: var(--color-success-light, #E6F7EF); color: var(--color-success-hover, #009A57); }
.stat-icon.b { background: var(--color-primary-light, #E6F4FF); color: var(--color-primary, #1677FF); }
.stat-icon.y { background: var(--color-warning-light, #FFF7E6); color: #D48806; }
.stat-icon.r { background: var(--color-danger-light, #FFF1F0); color: var(--color-danger, #FF4D4F); }
.stat-icon.ai { background: var(--color-ai-light, #F9F0FF); color: var(--color-ai, #722ED1); }
.stat-foot {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 14px;
  font-size: 12px;
  color: var(--color-text-placeholder);
}
.stat-foot.between { justify-content: space-between; }

.spark {
  display: flex;
  align-items: flex-end;
  gap: 3px;
  height: 22px;
}
.spark b {
  width: 4px;
  border-radius: 2px;
  background: var(--color-primary-mid, #91CAFF);
  display: block;
}

.mid-grid {
  display: grid;
  grid-template-columns: 1.35fr 1fr;
  gap: 18px;
  margin-bottom: 18px;
}
.bottom-grid {
  display: grid;
  grid-template-columns: 1.75fr 1fr;
  gap: 18px;
}

.panel { padding: 20px; overflow: hidden; }
.panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}
.panel__title {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--color-text-primary);
}
.panel__chip {
  font-size: 12px;
  color: var(--color-text-secondary);
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 6px 10px;
  background: #f8fafc;
}
.panel__chip.ai {
  background: var(--color-ai-light, #F9F0FF);
  color: var(--color-ai, #722ED1);
  border-color: transparent;
}
.chart-panel :deep(.chart-card) {
  background: transparent;
  padding: 0;
  box-shadow: none;
  border-radius: 0;
  width: 100%;
  max-width: 100%;
  min-width: 0;
}
.chart-panel :deep(.chart-card__title) { display: none; }

.legend {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  font-size: 12px;
  color: var(--color-text-secondary);
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 6px;
  vertical-align: middle;
}
.dot.solid { background: var(--color-primary, #1677FF); }
.dot.soft { background: #9CA3AF; }

.link-all {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: transparent;
  color: var(--color-text-placeholder);
  font-size: 12px;
  cursor: pointer;
  padding: 0;
}
.link-all:hover { color: var(--color-primary, #1677FF); }

.task-list { display: flex; flex-direction: column; gap: 8px; }
.task-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 14px;
  border: 1px solid var(--color-border);
  background: #f8fafc;
}
.task-item.urgent {
  background: #FFF1F0;
  border-color: #FFCCC7;
}
.task-item__body { flex: 1; min-width: 0; }
.task-item__title {
  font-size: 13px;
  font-weight: 650;
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.task-item__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 3px;
  font-size: 11px;
  color: var(--color-text-placeholder);
  font-family: var(--font-mono);
}
.badge {
  flex-shrink: 0;
  font-size: 11px;
  font-weight: 650;
  padding: 6px 10px;
  border-radius: 999px;
  border: none;
  cursor: pointer;
  font-family: inherit;
}
.badge.danger { background: #FFF1F0; color: #FF4D4F; }
.badge.ok { background: #E6F7EF; color: #009A57; }
.badge.ai { background: #F9F0FF; color: #722ED1; }
.badge.muted { background: #F3F4F6; color: #6B7280; }

.suggest-list {
  margin: 0;
  padding: 0 0 0 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 13px;
  line-height: 1.55;
  color: var(--color-text-secondary);
}
.suggest-list li::marker { color: var(--color-ai, #722ED1); }

.ai-note {
  margin: 14px 0 0;
  padding-top: 12px;
  border-top: 1px dashed var(--color-border);
  font-size: 11px;
  color: var(--color-text-placeholder);
}

.quick-links {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 14px;
}
.mini {
  border: 1px solid var(--color-border);
  border-radius: 14px;
  padding: 12px;
  background: #f8fafc;
  cursor: pointer;
  text-align: left;
  font: inherit;
  transition: box-shadow var(--transition-fast), background var(--transition-fast);
}
.mini:hover {
  background: #fff;
  box-shadow: var(--shadow-md);
}
.mini__k {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}
.mini__v {
  font-family: var(--font-mono);
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text-primary);
}
.mini__v.linkish { color: var(--color-primary, #1677FF); }

.empty {
  text-align: center;
  padding: 28px 0;
  font-size: 13px;
  color: var(--color-text-placeholder);
}

@media (max-width: 1023.98px) {
  .stat-grid,
  .mid-grid,
  .bottom-grid {
    grid-template-columns: 1fr;
  }
  .stat-triple {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 767.98px) {
  .dash-hero {
    flex-direction: column;
    align-items: stretch;
    gap: 14px;
    margin-bottom: 20px;
  }
  .dash-hero__title {
    font-size: 22px;
  }
  .dash-hero__sub {
    font-size: 12px;
    line-height: 1.45;
  }
  .cta-primary {
    width: 100%;
    justify-content: center;
  }

  /* 分区标题 + 卡片落入同一 2 列网格，符合手机扫读习惯 */
  .stat-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 10px;
    margin-bottom: 14px;
  }
  .stat-group,
  .stat-pair,
  .stat-triple {
    display: contents;
  }
  .section-label {
    grid-column: 1 / -1;
    margin: 4px 0 0;
    font-size: 11px;
  }
  .stat-group + .stat-group .section-label {
    margin-top: 8px;
  }

  .stat-card {
    min-height: 0;
    padding: 12px;
    border-radius: 14px;
  }
  .stat-card:hover {
    transform: none;
  }
  .stat-label {
    font-size: 12px;
  }
  .stat-value {
    margin-top: 6px;
    font-size: 22px;
  }
  .stat-icon {
    width: 28px;
    height: 28px;
    border-radius: 8px;
  }
  .stat-icon :deep(svg) {
    width: 14px;
    height: 14px;
  }
  .stat-foot {
    margin-top: 8px;
    font-size: 11px;
  }
  .spark {
    display: none;
  }

  .mid-grid,
  .bottom-grid {
    gap: 12px;
    margin-bottom: 12px;
  }
  .panel {
    padding: 14px;
    border-radius: 14px;
  }
  .panel__title {
    font-size: 16px;
  }
  .panel__head {
    margin-bottom: 10px;
    flex-wrap: wrap;
  }

  .task-item {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  .task-item__title {
    white-space: normal;
  }
  .badge {
    align-self: stretch;
    text-align: center;
    min-height: var(--touch-min, 44px);
    display: inline-flex;
    align-items: center;
    justify-content: center;
  }

  .quick-links {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
