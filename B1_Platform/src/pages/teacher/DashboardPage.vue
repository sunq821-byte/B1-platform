<script setup lang="ts">
import { ref, computed, onMounted } from "vue"
import { useRouter } from "vue-router"
import { useTeacherStore } from "@/stores/useTeacherStore"
import { useUserStore } from "@/stores/useUserStore"
import { ElMessage } from "element-plus"
import {
  BookOpen, PlayCircle, Inbox, Percent, AlertTriangle,
  Plus, BookMarked, ClipboardList, ArrowRight, GraduationCap,
} from "lucide-vue-next"
import BarChart from "@/components/chart/BarChart.vue"
import DonutChart from "@/components/chart/DonutChart.vue"
import BaseButton from "@/components/base/BaseButton.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"

const store = useTeacherStore()
const userStore = useUserStore()
const router = useRouter()

const loadError = ref("")
const checkedTasks = ref<Set<string>>(new Set())
const selectAll = ref(false)
const notifying = ref(false)

const today = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`
})

const data = computed(() => store.dashboardData)
const upcomingTasks = computed(() => data.value?.upcomingTasks ?? [])
const activeTrainingCount = computed(() =>
  (data.value?.stats.pendingCount ?? 0) + (data.value?.stats.reviewedCount ?? 0),
)

const reviewSegments = computed(() => {
  const r = data.value?.reviewStatus
  if (!r) return []
  return [
    { name: "待 AI 初评", value: r.pendingAi, color: "#91CAFF" },
    { name: "待教师复核", value: r.pendingTeacher, color: "#722ED1" },
    { name: "已终审通过", value: r.approved, color: "#00B96B" },
    { name: "退回重提", value: r.returned, color: "#FAAD14" },
  ]
})

const reviewTotal = computed(() =>
  reviewSegments.value.reduce((sum, s) => sum + s.value, 0),
)

const aiConfidence = computed(() => data.value?.aiConfidence)
const aiBarStyle = computed(() => {
  const a = aiConfidence.value
  if (!a || a.total === 0) return { high: "0%", mid: "0%", low: "0%" }
  return {
    high: `${Math.round((a.high / a.total) * 100)}%`,
    mid: `${Math.round((a.mid / a.total) * 100)}%`,
    low: `${Math.round((a.low / a.total) * 100)}%`,
  }
})

const classAvgScore = computed(() => {
  const scores = data.value?.submitRateByClass.avgScores ?? []
  if (!scores.length) return "—"
  return (scores.reduce((a, b) => a + b, 0) / scores.length).toFixed(1)
})

function toggleAllTasks() {
  checkedTasks.value = selectAll.value
    ? new Set(upcomingTasks.value.map((t) => t.taskId))
    : new Set()
}

function toggleTask(id: string) {
  const next = new Set(checkedTasks.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  checkedTasks.value = next
  selectAll.value = upcomingTasks.value.length > 0
    && next.size === upcomingTasks.value.length
}

const hasChecked = computed(() => checkedTasks.value.size > 0)

function formatDeadline(dt: string): string {
  if (!dt) return "—"
  return dt.replace("T", " ").substring(0, 16)
}

async function batchNotify() {
  if (checkedTasks.value.size === 0) return
  notifying.value = true
  try {
    const res = await store.remindTasks([...checkedTasks.value])
    if (res.notifiedStudents > 0) {
      ElMessage.success(`已向未提交学生发送 ${res.notifiedStudents} 条催交通知`)
    } else {
      ElMessage.info("所选任务的学生均已提交，无需催交")
    }
    checkedTasks.value = new Set()
    selectAll.value = false
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || "催交失败")
  } finally {
    notifying.value = false
  }
}

function goTo(path: string) {
  router.push(path)
}

async function initPage() {
  loadError.value = ""
  try {
    await store.fetchDashboard()
  } catch (e: unknown) {
    loadError.value = (e as Error)?.message || "加载工作台失败"
  }
}

onMounted(() => { initPage() })
</script>

<template>
  <div class="dash">
    <div class="dash-hero">
      <div>
        <h1 class="dash-hero__title">欢迎回来，{{ userStore.userName || "教师" }}!</h1>
        <p class="dash-hero__sub">{{ today }} · 教师工作台 · 今日优先处理审核队列</p>
      </div>
      <button type="button" class="cta-primary" @click="goTo('/teacher/tasks')">
        <Plus :size="16" />
        新建实训任务
      </button>
    </div>

    <div v-if="store.dashboardLoading" class="dash-state">
      <LoadingState text="加载教师工作台..." />
    </div>
    <div v-else-if="loadError" class="dash-state">
      <ErrorState :message="loadError" @retry="initPage" />
    </div>

    <template v-else-if="data">
      <div class="stat-grid">
        <section class="stat-group">
          <h2 class="section-label">教学概览</h2>
          <div class="stat-pair">
            <article class="stat-card" @click="goTo('/teacher/courses')">
              <div class="stat-card__top">
                <div>
                  <div class="stat-label">在教课程</div>
                  <div class="stat-value">{{ data.stats.classCount }}</div>
                </div>
                <div class="stat-icon g"><BookOpen :size="18" /></div>
              </div>
              <div class="stat-foot">
                <span>{{ data.stats.totalStudents }} 名学生</span>
              </div>
            </article>
            <article class="stat-card" @click="goTo('/teacher/tasks')">
              <div class="stat-card__top">
                <div>
                  <div class="stat-label">进行中实训</div>
                  <div class="stat-value">{{ activeTrainingCount }}</div>
                </div>
                <div class="stat-icon b"><PlayCircle :size="18" /></div>
              </div>
              <div class="stat-foot">
                <span class="warn">{{ data.stats.pendingCount }} 个待审核</span>
              </div>
            </article>
          </div>
        </section>

        <section class="stat-group">
          <h2 class="section-label">提交与审核</h2>
          <div class="stat-triple">
            <article class="stat-card" @click="goTo('/teacher/submissions')">
              <div class="stat-card__top">
                <div>
                  <div class="stat-label">待审核提交</div>
                  <div class="stat-value danger">{{ data.stats.pendingCount }}</div>
                </div>
                <div class="stat-icon r"><Inbox :size="18" /></div>
              </div>
              <div class="stat-foot">
                <span class="danger">需尽快处理</span>
              </div>
            </article>
            <article class="stat-card" @click="goTo('/teacher/reports')">
              <div class="stat-card__top">
                <div>
                  <div class="stat-label">班级提交率</div>
                  <div class="stat-value success">{{ data.stats.submissionRate }}%</div>
                </div>
                <div class="stat-icon g"><Percent :size="18" /></div>
              </div>
              <div class="stat-foot">
                <span class="success">已评阅 {{ data.stats.reviewedCount }}</span>
              </div>
            </article>
            <article class="stat-card" @click="goTo('/teacher/submissions')">
              <div class="stat-card__top">
                <div>
                  <div class="stat-label">AI 低置信</div>
                  <div class="stat-value">{{ aiConfidence?.low ?? 0 }}</div>
                </div>
                <div class="stat-icon y"><AlertTriangle :size="18" /></div>
              </div>
              <div class="stat-foot">
                <span>建议人工复核</span>
              </div>
            </article>
          </div>
        </section>
      </div>

      <div class="mid-grid">
        <section class="panel chart-panel">
          <div class="panel__head">
            <h3 class="panel__title">班级提交率</h3>
            <div class="legend">
              <span><i class="dot solid" />提交率</span>
              <span><i class="dot soft" />平均成绩</span>
            </div>
          </div>
          <BarChart
            :x-axis="data.submitRateByClass.classNames"
            :series-data="[
              { name: '提交率(%)', data: data.submitRateByClass.values, color: '#1677FF' },
              { name: '平均成绩', data: data.submitRateByClass.avgScores, color: '#00B96B' },
            ]"
            height="280px"
          />
        </section>

        <section class="panel">
          <div class="panel__head">
            <h3 class="panel__title">审核状态</h3>
            <span class="panel__chip">本学期</span>
          </div>
          <DonutChart
            :segments="reviewSegments"
            :center-value="reviewTotal"
            center-label="提交总量"
            height="190px"
          />
          <div class="gauge-legend">
            <span v-for="s in reviewSegments" :key="s.name">
              <i class="dot" :style="{ background: s.color }" />
              {{ s.name }}
              <b>{{ s.value }}</b>
            </span>
          </div>
        </section>
      </div>

      <div class="bottom-grid">
        <section class="panel">
          <div class="panel__head stack">
            <h3 class="panel__title">AI 评价概况</h3>
            <p class="panel__sub">本学期 {{ aiConfidence?.total ?? 0 }} 份诊断报告</p>
          </div>
          <div class="progress">
            <i class="seg-high" :style="{ width: aiBarStyle.high }" />
            <i class="seg-mid" :style="{ width: aiBarStyle.mid }" />
            <i class="seg-low" :style="{ width: aiBarStyle.low }" />
          </div>
          <div class="legend wrap">
            <span><i class="dot" style="background:#00B96B" />高置信 {{ aiConfidence?.high ?? 0 }}</span>
            <span><i class="dot" style="background:#FAAD14" />中置信 {{ aiConfidence?.mid ?? 0 }}</span>
            <span><i class="dot" style="background:#FF4D4F" />低置信 {{ aiConfidence?.low ?? 0 }}</span>
          </div>
          <p class="ai-note">AI 仅提供建议分与依据，终评由教师确认</p>
        </section>

        <section class="panel">
          <div class="panel__head">
            <h3 class="panel__title">快截止任务</h3>
            <label v-if="upcomingTasks.length" class="select-all">
              <input v-model="selectAll" type="checkbox" @change="toggleAllTasks" />
              全选
            </label>
          </div>
          <div class="task-list">
            <div
              v-for="item in upcomingTasks"
              :key="item.taskId"
              class="task-item"
              :class="{ urgent: item.unsubmittedCount > 0 }"
            >
              <input
                type="checkbox"
                :checked="checkedTasks.has(item.taskId)"
                @change="toggleTask(item.taskId)"
              />
              <div class="task-item__body">
                <div class="task-item__title">{{ item.taskName }}</div>
                <div class="task-item__meta">
                  <span>{{ item.courseName }}</span>
                  <span>{{ formatDeadline(item.deadline) }}</span>
                </div>
              </div>
              <span class="badge" :class="item.unsubmittedCount > 0 ? 'danger' : 'ok'">
                未提交 {{ item.unsubmittedCount }}
              </span>
            </div>
            <div v-if="!upcomingTasks.length" class="empty">近 7 天暂无临近截止的任务</div>
          </div>
          <div v-if="hasChecked" class="batch">
            <BaseButton size="small" type="primary" :loading="notifying" @click="batchNotify">
              批量催交（{{ checkedTasks.size }}）
            </BaseButton>
          </div>
        </section>

        <section class="panel">
          <div class="panel__head">
            <h3 class="panel__title">快捷指标</h3>
          </div>
          <div class="quick-grid">
            <button type="button" class="mini" @click="goTo('/teacher/tasks')">
              <div class="mini__k"><ClipboardList :size="14" />本学期实训</div>
              <div class="mini__v">{{ data.semesterTaskCount }}</div>
            </button>
            <button type="button" class="mini" @click="goTo('/teacher/knowledge-base')">
              <div class="mini__k"><BookMarked :size="14" />知识库条目</div>
              <div class="mini__v">{{ data.knowledgeEntryCount }}</div>
            </button>
            <button type="button" class="mini" @click="goTo('/teacher/reports')">
              <div class="mini__k"><GraduationCap :size="14" />班级均分</div>
              <div class="mini__v">{{ classAvgScore }}</div>
            </button>
            <button type="button" class="mini link" @click="goTo('/teacher/reports-college')">
              <div class="mini__k">学院报告</div>
              <div class="mini__v"><ArrowRight :size="18" /></div>
            </button>
          </div>
        </section>
      </div>
    </template>
  </div>
</template>

<style scoped>
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
  background: var(--color-primary, #1677FF);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 8px 18px rgba(22, 119, 255, 0.25);
  transition: background var(--transition-fast), transform var(--transition-fast);
}
.cta-primary:hover {
  background: var(--color-primary-hover, #0958D9);
  transform: translateY(-1px);
}

.dash-state { padding: 48px 0; }

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
}
.stat-icon.g { background: var(--color-success-light, #E6F7EF); color: var(--color-success-hover, #009A57); }
.stat-icon.b { background: var(--color-primary-light, #E6F4FF); color: var(--color-primary, #1677FF); }
.stat-icon.y { background: var(--color-warning-light, #FFF7E6); color: #D48806; }
.stat-icon.r { background: var(--color-danger-light, #FFF1F0); color: var(--color-danger, #FF4D4F); }
.stat-foot {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 14px;
  font-size: 12px;
  color: var(--color-text-placeholder);
}
.stat-foot.between { justify-content: space-between; }
.stat-foot .warn { color: #d97706; }
.stat-foot .danger { color: #CF1322; }
.stat-foot .success { color: #059669; }

.avatars { display: flex; }
.avatars i {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid #fff;
  margin-left: -6px;
  background: linear-gradient(135deg, #91CAFF, #7AD9A8);
  display: inline-block;
}
.avatars i:first-child { margin-left: 0; }

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
.spark.amber b { background: #FFE7BA; }

.mid-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 18px;
  margin-bottom: 18px;
}
.bottom-grid {
  display: grid;
  grid-template-columns: 1.15fr 1.2fr 0.95fr;
  gap: 18px;
}

.panel { padding: 20px; }
.panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}
.panel__head.stack {
  flex-direction: column;
  align-items: flex-start;
}
.panel__title {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--color-text-primary);
}
.panel__sub {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--color-text-placeholder);
}
.panel__chip {
  font-size: 12px;
  color: var(--color-text-secondary);
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 6px 10px;
  background: #f8fafc;
}
.chart-panel :deep(.chart-card) {
  background: transparent;
  padding: 0;
  box-shadow: none;
  border-radius: 0;
}
.chart-panel :deep(.chart-card__title) { display: none; }

.legend {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  font-size: 12px;
  color: var(--color-text-secondary);
}
.legend.wrap { gap: 12px 16px; }
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 6px;
  vertical-align: middle;
}
.dot.solid { background: var(--color-primary, #1677FF); }
.dot.soft { background: var(--color-success, #00B96B); }

.gauge-legend {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 12px;
  margin-top: 8px;
  font-size: 12px;
  color: var(--color-text-secondary);
}
.gauge-legend b {
  font-family: var(--font-mono);
  color: var(--color-text-primary);
  margin-left: 4px;
}

.progress {
  height: 12px;
  border-radius: 999px;
  overflow: hidden;
  display: flex;
  background: #eef2f7;
  margin: 8px 0 14px;
}
.progress i { display: block; height: 100%; }
.seg-high { background: var(--color-success, #00B96B); }
.seg-mid { background: var(--color-warning, #FAAD14); }
.seg-low { background: var(--color-danger, #FF4D4F); }
.ai-note {
  margin: 14px 0 0;
  padding-top: 12px;
  border-top: 1px dashed var(--color-border);
  font-size: 11px;
  color: var(--color-text-placeholder);
}

.select-all {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-text-placeholder);
  cursor: pointer;
}
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
.task-item.urgent { background: #fef2f2; border-color: #fecaca; }
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
  padding: 3px 8px;
  border-radius: 999px;
}
.badge.danger { background: #fef2f2; color: #FF4D4F; }
.badge.ok { background: #E6F7EF; color: #009A57; }
.empty {
  text-align: center;
  padding: 28px 0;
  font-size: 13px;
  color: var(--color-text-placeholder);
}
.batch {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
}

.quick-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.mini {
  border: 1px solid var(--color-border);
  border-radius: 14px;
  padding: 14px;
  background: #f8fafc;
  cursor: pointer;
  min-height: 88px;
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
  margin-bottom: 10px;
}
.mini__v {
  font-family: var(--font-mono);
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text-primary);
}
.mini.link .mini__v {
  color: var(--color-primary);
  display: flex;
  align-items: center;
}

@media (max-width: 1100px) {
  .stat-grid, .mid-grid, .bottom-grid { grid-template-columns: 1fr; }
  .stat-triple { grid-template-columns: 1fr 1fr; }
}
@media (max-width: 640px) {
  .dash-hero { flex-direction: column; align-items: flex-start; }
  .dash-hero__title { font-size: 26px; }
  .stat-pair, .stat-triple { grid-template-columns: 1fr; }
}
</style>
