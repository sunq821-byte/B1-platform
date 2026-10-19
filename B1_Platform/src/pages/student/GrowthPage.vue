<script setup lang="ts">
import { computed, ref, inject } from "vue"
import { useStudentStore } from "@/stores/useStudentStore"
import { useUserStore } from "@/stores/useUserStore"
import { useReportRefresh } from "@/composables/useReportRefresh"
import { useBreakpoint } from "@/composables/useBreakpoint"
import PageHeader from "@/components/layout/PageHeader.vue"
import LineChart from "@/components/chart/LineChart.vue"
import RadarChart from "@/components/chart/RadarChart.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import EmptyState from "@/components/common/EmptyState.vue"
import ErrorState from "@/components/common/ErrorState.vue"

const embedded = inject("tabHubEmbedded", false)
const store = useStudentStore()
const userStore = useUserStore()
const { isXs } = useBreakpoint()
const chartH = computed(() => (isXs.value ? "240px" : "340px"))
const loadError = ref("")

async function initPage() {
  loadError.value = ""
  try {
    await store.fetchGrowth()
  } catch (e: unknown) {
    loadError.value = (e as Error)?.message || "加载成长数据失败"
  }
}

useReportRefresh(initPage)

function fmtScore(s: number | null): string {
  if (s === null || s === undefined) return "-"
  return s.toFixed(1)
}

const radarCategories = computed(() => {
  if (!store.growthData?.dimensionRadar) return []
  return store.growthData.dimensionRadar.map(d => d.dimensionName)
})

const radarSeries = computed(() => {
  if (!store.growthData?.dimensionRadar) return []
  return [
    { name: "我的平均", data: store.growthData.dimensionRadar.map(d => d.myAvg ?? 0), color: "#1677FF" },
    { name: "班级平均", data: store.growthData.dimensionRadar.map(d => d.classAvg ?? 0), color: "#94A3B8" },
  ]
})

const trendCategories = computed(() => {
  if (!store.growthData?.monthlyTrends) return []
  return store.growthData.monthlyTrends.map(t => t.month)
})

const trendSeries = computed(() => {
  if (!store.growthData?.monthlyTrends) return []
  return [
    { name: "平均成绩", data: store.growthData.monthlyTrends.map(t => t.avgScore ?? 0), color: "#1677FF" },
    { name: "提交数", data: store.growthData.monthlyTrends.map(t => t.submissionCount), color: "#00B96B" },
  ]
})

function scoreColor(score: number) {
  if (score >= 80) return "var(--color-success, #00B96B)"
  if (score >= 60) return "var(--color-warning, #FAAD14)"
  return "var(--color-danger, #FF4D4F)"
}

function resultLabel(result: string | null): string {
  if (result === "PASS") return "通过"
  if (result === "REJECT") return "驳回"
  if (result === "PENDING") return "处理中"
  return result || "处理中"
}

function timelineIcon(type: string): string {
  if (type === "score") return "score"
  if (type === "reject") return "reject"
  return "submit"
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ""
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${(d.getMonth()+1).toString().padStart(2,'0')}-${d.getDate().toString().padStart(2,'0')}`
}
</script>

<template>
  <div>
    <PageHeader
      v-if="!embedded"
      title="成长中心"
      :subtitle="`${userStore.userName} · 能力成长轨迹`"
    />

    <LoadingState
      v-if="store.growthLoading && !store.growthData"
      text="加载成长数据..."
    />
    <ErrorState v-else-if="loadError" :message="loadError" @retry="initPage" />

    <template v-else-if="store.growthData">
      <!-- Stats -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-label">全部任务</div>
          <div class="stat-value">{{ store.growthData.totalTasks }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">已完成</div>
          <div class="stat-value" style="color: var(--color-success, #00B96B);">{{ store.growthData.completedTasks }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">完成率</div>
          <div class="stat-value">{{ store.growthData.completionRate ?? 0 }}%</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">平均成绩</div>
          <div class="stat-value">{{ fmtScore(store.growthData.averageScore) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">最高 / 最低</div>
          <div class="stat-value">
            <span style="color: var(--color-success, #00B96B);">{{ fmtScore(store.growthData.highestScore) }}</span>
            <span style="font-size:14px;color:var(--color-text-placeholder,#94a3b8);"> / </span>
            <span style="color: var(--color-danger, #FF4D4F);">{{ fmtScore(store.growthData.lowestScore) }}</span>
          </div>
        </div>
      </div>

      <div v-if="store.growthData.dimensionInsight" class="insight-card mt-24">
        <div class="insight-card__title">能力诊断</div>
        <div class="insight-tags">
          <span class="insight-tag insight-tag--strong">优势 · {{ store.growthData.dimensionInsight.strongest }}</span>
          <span class="insight-tag insight-tag--weak">待提升 · {{ store.growthData.dimensionInsight.weakest }}</span>
        </div>
        <p class="insight-summary">{{ store.growthData.dimensionInsight.summary }}</p>
      </div>

      <!-- Charts -->
      <div class="content-grid mt-24">
        <div class="chart-wrap lg">
          <RadarChart
            v-if="radarCategories.length"
            title="能力维度雷达图"
            :categories="radarCategories"
            :series-data="radarSeries"
            :height="chartH"
          />
          <EmptyState v-else description="暂无维度数据" />
        </div>
        <div class="chart-wrap lg">
          <LineChart
            v-if="trendCategories.length"
            title="月度趋势"
            :x-axis="trendCategories"
            :series-data="trendSeries"
            :height="chartH"
          />
          <EmptyState v-else description="暂无趋势数据" />
        </div>
      </div>

      <!-- Course Scores -->
      <div class="section mt-24" v-if="store.growthData.courseScores.length">
        <h2 class="section-title">课程成绩分布</h2>
        <div class="course-grid mt-12">
          <div v-for="cs in store.growthData.courseScores" :key="cs.courseId" class="course-card">
            <div class="course-card__name">{{ cs.courseName }}</div>
            <div class="course-card__score" :style="{ color: scoreColor(cs.avgScore ?? 0) }">
              {{ fmtScore(cs.avgScore) }}
            </div>
            <div class="course-card__meta">{{ cs.taskCount }} 项任务</div>
          </div>
        </div>
      </div>

      <!-- Growth Timeline -->
      <div v-if="store.growthData.growthTimeline?.length" class="section mt-24">
        <h2 class="section-title">成长时间线</h2>
        <div class="timeline mt-12">
          <div
            v-for="(item, idx) in store.growthData.growthTimeline"
            :key="`${item.date}-${idx}`"
            class="timeline-item"
            :class="`timeline-item--${timelineIcon(item.type)}`"
          >
            <div class="timeline-item__dot" />
            <div class="timeline-item__body">
              <div class="timeline-item__head">
                <span class="timeline-item__title">{{ item.title }}</span>
                <span class="timeline-item__date">{{ item.date }}</span>
              </div>
              <p class="timeline-item__desc">{{ item.description }}</p>
              <span
                v-if="item.score != null"
                class="timeline-item__score"
                :style="{ color: scoreColor(item.score) }"
              >
                {{ item.score.toFixed(1) }} 分
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Submission History -->
      <div class="section mt-24">
        <h2 class="section-title">提交历史</h2>
        <div v-if="!store.growthData.submissionHistory.length" class="p-8">
          <EmptyState description="暂无提交记录" />
        </div>
        <div v-else class="mt-12">
          <div class="mobile-history-list" aria-label="提交历史卡片">
            <article
              v-for="sh in store.growthData.submissionHistory"
              :key="sh.submissionId"
              class="mobile-history-card"
            >
              <div class="mobile-history-card__top">
                <div class="mobile-history-card__title">{{ sh.taskName }}</div>
                <span v-if="sh.result === 'PASS'" class="badge badge--success">通过</span>
                <span v-else-if="sh.result === 'REJECT'" class="badge badge--danger">驳回</span>
                <span v-else class="badge badge--default">{{ resultLabel(sh.result) }}</span>
              </div>
              <div class="mobile-history-card__meta">{{ sh.courseName }}</div>
              <div class="mobile-history-card__row">
                <span
                  v-if="sh.score != null"
                  class="mono"
                  :style="{ color: scoreColor(sh.score) }"
                >{{ sh.score.toFixed(1) }} 分</span>
                <span v-else class="muted">暂无成绩</span>
                <span class="mobile-history-card__date">{{ formatDate(sh.submittedAt) }}</span>
              </div>
            </article>
          </div>

          <div class="data-table desktop-history-table">
            <table>
              <thead>
                <tr>
                  <th>任务名称</th>
                  <th>课程</th>
                  <th>成绩</th>
                  <th>结果</th>
                  <th>提交时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="sh in store.growthData.submissionHistory" :key="sh.submissionId">
                  <td>{{ sh.taskName }}</td>
                  <td style="color:var(--color-text-secondary,#64748b);">{{ sh.courseName }}</td>
                  <td>
                    <span v-if="sh.score != null" style="font-family:var(--font-mono);font-weight:500;" :style="{ color: scoreColor(sh.score) }">
                      {{ sh.score.toFixed(1) }}
                    </span>
                    <span v-else style="color:var(--color-text-placeholder,#94a3b8);">-</span>
                  </td>
                  <td>
                    <span v-if="sh.result === 'PASS'" class="badge badge--success">通过</span>
                    <span v-else-if="sh.result === 'REJECT'" class="badge badge--danger">驳回</span>
                    <span v-else class="badge badge--default">{{ resultLabel(sh.result) }}</span>
                  </td>
                  <td style="font-family:var(--font-mono);font-size:12px;">{{ formatDate(sh.submittedAt) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </template>
    <EmptyState v-else-if="!store.growthLoading" description="暂无成长数据" />
  </div>
</template>

<style scoped>
.p-8 { padding: var(--spacing-2xl, 48px); }
.mt-12 { margin-top: 12px; }
.mt-24 { margin-top: 24px; }
.section-title {
  font-size: 18px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--color-navy, #0B3A5C);
  margin: 0;
}
.insight-card {
  background: var(--color-card, #fff);
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: var(--radius-xl, 16px);
  padding: 20px 24px;
  box-shadow: var(--shadow-card, 0 1px 2px rgba(15,23,42,.04));
}
.insight-card__title {
  font-size: 16px;
  font-weight: 800;
  color: var(--color-navy, #0B3A5C);
  margin-bottom: 12px;
}
.insight-tags { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 10px; }
.insight-tag {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  font-weight: 600;
}
.insight-tag--strong { background: rgba(0,185,107,.1); color: #00B96B; }
.insight-tag--weak { background: rgba(250,173,20,.12); color: #D48806; }
.insight-summary {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-secondary, #64748b);
  line-height: 1.6;
}
.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}
.course-card {
  background: var(--color-card, #fff);
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: 12px;
  padding: 16px;
}
.course-card__name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
  margin-bottom: 8px;
}
.course-card__score {
  font-family: var(--font-mono);
  font-size: 28px;
  font-weight: 700;
  line-height: 1.1;
}
.course-card__meta {
  margin-top: 6px;
  font-size: 12px;
  color: var(--color-text-placeholder, #94a3b8);
}
.timeline { display: flex; flex-direction: column; gap: 0; }
.timeline-item {
  display: flex;
  gap: 16px;
  padding-bottom: 20px;
  position: relative;
}
.timeline-item:not(:last-child)::before {
  content: "";
  position: absolute;
  left: 7px;
  top: 18px;
  bottom: 0;
  width: 2px;
  background: var(--color-border, #e2e8f0);
}
.timeline-item__dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 4px;
  background: #1677FF;
  border: 3px solid #fff;
  box-shadow: 0 0 0 1px #1677FF;
}
.timeline-item--score .timeline-item__dot { background: #00B96B; box-shadow: 0 0 0 1px #00B96B; }
.timeline-item--reject .timeline-item__dot { background: #FF4D4F; box-shadow: 0 0 0 1px #FF4D4F; }
.timeline-item__body { flex: 1; min-width: 0; }
.timeline-item__head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: baseline;
}
.timeline-item__title {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-primary, #1e293b);
}
.timeline-item__date {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--color-text-placeholder, #94a3b8);
  flex-shrink: 0;
}
.timeline-item__desc {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--color-text-secondary, #64748b);
}
.timeline-item__score {
  display: inline-block;
  margin-top: 6px;
  font-family: var(--font-mono);
  font-size: 13px;
  font-weight: 600;
}

.mono { font-family: var(--font-mono); font-weight: 500; }
.muted { color: var(--color-text-secondary, #64748b); }

.mobile-history-list {
  display: none;
  flex-direction: column;
  gap: 10px;
}

.mobile-history-card {
  background: var(--color-card, #fff);
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: 14px;
  padding: 14px 16px;
  box-shadow: var(--shadow-sm);
}

.mobile-history-card__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.mobile-history-card__title {
  font-size: 15px;
  font-weight: 700;
  color: var(--color-navy, #0B3A5C);
  line-height: 1.35;
}

.mobile-history-card__meta {
  margin-top: 6px;
  font-size: 12px;
  color: var(--color-text-secondary, #6B7280);
}

.mobile-history-card__row {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.mobile-history-card__date {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--color-text-placeholder, #94a3b8);
}

@media (max-width: 767.98px) {
  .course-grid {
    grid-template-columns: 1fr 1fr;
    gap: 10px;
  }
  .timeline-item__head {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  .insight-card {
    padding: 14px 16px;
  }
  .mobile-history-list { display: flex; }
  .desktop-history-table { display: none; }
}
</style>
