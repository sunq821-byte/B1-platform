<script setup lang="ts">
import { ref, computed, inject } from "vue"
import { ElMessage } from "element-plus"
import { useStudentStore } from "@/stores/useStudentStore"
import { useUserStore } from "@/stores/useUserStore"
import { exportStudentReport } from "@/api/modules/student"
import { useReportRefresh } from "@/composables/useReportRefresh"
import { useBreakpoint } from "@/composables/useBreakpoint"
import PageHeader from "@/components/layout/PageHeader.vue"
import LineChart from "@/components/chart/LineChart.vue"
import RadarChart from "@/components/chart/RadarChart.vue"
import EmptyState from "@/components/common/EmptyState.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"

const embedded = inject("tabHubEmbedded", false)
const store = useStudentStore()
const userStore = useUserStore()
const { isXs } = useBreakpoint()
const chartH = computed(() => (isXs.value ? "240px" : "340px"))
const exporting = ref(false)
const loadError = ref("")

const subtitle = computed(() => {
  const name = userStore.userName || "学生"
  return `${name} · 本学期学情报告`
})

async function initPage() {
  loadError.value = ""
  try {
    await store.fetchStudentReport()
  } catch (e: unknown) {
    loadError.value = (e as Error)?.message || "加载个人报告失败"
  }
}

useReportRefresh(initPage)

async function handleExport(format: "xlsx" | "pdf") {
  if (!store.studentReport || store.studentReport.rows.length === 0) {
    ElMessage.warning("当前无数据可导出")
    return
  }
  exporting.value = true
  try {
    await exportStudentReport(format)
    ElMessage.success("导出成功")
  } catch { /* surfaced */ }
  finally { exporting.value = false }
}

function fmtScore(s: number | null): string {
  if (s === null || s === undefined) return "-"
  return s.toFixed(1)
}

function scoreColor(score: number) {
  if (score >= 80) return "var(--color-success, #00B96B)"
  if (score >= 60) return "var(--color-warning, #FAAD14)"
  return "var(--color-danger, #FF4D4F)"
}

function statusLabel(status: string): string {
  const map: Record<string, string> = {
    REVIEWED: "已评阅",
    REJECTED: "已退回",
    SUBMITTED: "已提交",
    PUBLISHED: "已发布",
    INCOMPLETE: "未完成",
    CONFIRMED: "已递交",
    DRAFT: "试交中",
  }
  return map[status] || status || "-"
}

function statusBadgeClass(status: string): string {
  if (status === "REVIEWED" || status === "PUBLISHED") return "badge badge--success"
  if (status === "REJECTED" || status === "INCOMPLETE") return "badge badge--danger"
  if (status === "SUBMITTED" || status === "CONFIRMED") return "badge badge--info"
  return "badge badge--default"
}
</script>

<template>
  <div>
    <PageHeader v-if="!embedded" title="个人报告" :subtitle="subtitle" />

    <div class="filter-bar">
      <span class="sem-chip">本学期</span>
      <div class="export-actions ml-auto">
        <button type="button" class="btn btn-outline btn-sm" :disabled="exporting" @click="handleExport('xlsx')">导出 Excel</button>
        <button type="button" class="btn btn-outline btn-sm" :disabled="exporting" @click="handleExport('pdf')">导出 PDF</button>
      </div>
    </div>

    <LoadingState v-if="store.studentReportLoading" text="加载个人报告..." />
    <ErrorState v-else-if="loadError" :message="loadError" @retry="initPage" />

    <template v-else-if="store.studentReport">
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-label">全部任务</div>
          <div class="stat-value">{{ store.studentReport.stats.totalTasks }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">已完成</div>
          <div class="stat-value" style="color:var(--color-success, #00B96B);">{{ store.studentReport.stats.completedTasks }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">提交次数</div>
          <div class="stat-value">{{ store.studentReport.stats.totalSubmissions }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">平均成绩</div>
          <div class="stat-value">{{ fmtScore(store.studentReport.stats.averageScore) }}</div>
        </div>
      </div>

      <div class="content-grid mt-24">
        <div class="chart-wrap lg">
          <LineChart
            title="分数趋势"
            :x-axis="store.studentReport.scoreTrend.categories"
            :series-data="[{ name: '我的成绩', data: store.studentReport.scoreTrend.values }]"
            :height="chartH"
          />
        </div>
        <div class="chart-wrap lg">
          <RadarChart
            v-if="store.studentReport.radarData.indicators.length"
            title="能力雷达"
            :categories="store.studentReport.radarData.indicators"
            :series-data="[{ name: '个人', data: store.studentReport.radarData.values, color: '#1677FF' }]"
            :height="chartH"
          />
          <EmptyState v-else description="暂无雷达数据" />
        </div>
      </div>

      <div class="section mt-24">
        <div class="section-header">
          <h2 class="section-title">成绩明细</h2>
        </div>
        <div v-if="!store.studentReport.rows.length">
          <EmptyState description="暂无成绩记录" />
        </div>
        <template v-else>
          <div class="mobile-score-list" aria-label="成绩明细卡片">
            <article
              v-for="(row, idx) in store.studentReport.rows"
              :key="idx"
              class="mobile-score-card"
            >
              <div class="mobile-score-card__top">
                <div class="mobile-score-card__title">{{ row.taskName }}</div>
                <span :class="statusBadgeClass(row.status)">{{ statusLabel(row.status) }}</span>
              </div>
              <div class="mobile-score-card__meta">{{ row.courseName }}</div>
              <div class="mobile-score-card__row">
                <span
                  v-if="row.score != null"
                  class="mono"
                  :style="{ color: scoreColor(row.score) }"
                >{{ row.score.toFixed(1) }} 分</span>
                <span v-else class="muted">暂无成绩</span>
              </div>
              <p v-if="row.reviewComment" class="mobile-score-card__comment">{{ row.reviewComment }}</p>
            </article>
          </div>

          <div class="data-table desktop-score-table">
            <table>
              <thead>
                <tr>
                  <th>任务名称</th>
                  <th>课程</th>
                  <th>成绩</th>
                  <th>评语</th>
                  <th>状态</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, idx) in store.studentReport.rows" :key="idx">
                  <td>{{ row.taskName }}</td>
                  <td><span class="muted">{{ row.courseName }}</span></td>
                  <td>
                    <span
                      v-if="row.score != null"
                      class="mono"
                      :style="{ color: scoreColor(row.score) }"
                    >{{ row.score.toFixed(1) }}</span>
                    <span v-else class="muted">-</span>
                  </td>
                  <td class="cell-wrap"><span class="muted sm">{{ row.reviewComment || '-' }}</span></td>
                  <td>
                    <span :class="statusBadgeClass(row.status)">{{ statusLabel(row.status) }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>
      </div>
    </template>
    <EmptyState v-else description="暂无个人报告数据" />
  </div>
</template>

<style scoped>
.sem-chip {
  font-size: 13px;
  color: var(--color-text-secondary);
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: 10px;
  background: #fff;
}
.ml-auto { margin-left: auto; }
.export-actions { display: flex; gap: 8px; }
.mt-24 { margin-top: 24px; }
.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  color: var(--color-navy, #0B3A5C);
}
.mono { font-family: var(--font-mono); font-weight: 500; }
.muted { color: var(--color-text-secondary, #64748b); }
.muted.sm { font-size: 13px; }

.mobile-score-list {
  display: none;
  flex-direction: column;
  gap: 10px;
}

.mobile-score-card {
  background: var(--color-card, #fff);
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: 14px;
  padding: 14px 16px;
  box-shadow: var(--shadow-sm);
}

.mobile-score-card__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.mobile-score-card__title {
  font-size: 15px;
  font-weight: 700;
  color: var(--color-navy, #0B3A5C);
  line-height: 1.35;
}

.mobile-score-card__meta {
  margin-top: 6px;
  font-size: 12px;
  color: var(--color-text-secondary, #6B7280);
}

.mobile-score-card__row {
  margin-top: 10px;
  font-size: 16px;
}

.mobile-score-card__comment {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--color-text-secondary, #64748b);
  line-height: 1.45;
}

@media (max-width: 767.98px) {
  .mobile-score-list { display: flex; }
  .desktop-score-table { display: none; }
}
</style>
