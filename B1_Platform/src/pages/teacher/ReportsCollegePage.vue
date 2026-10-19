<script setup lang="ts">
import { ref, computed, inject } from "vue"
import { useTeacherStore } from "@/stores/useTeacherStore"
import { useReportRefresh } from "@/composables/useReportRefresh"
import PageHeader from "@/components/layout/PageHeader.vue"
import BarChart from "@/components/chart/BarChart.vue"
import LineChart from "@/components/chart/LineChart.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"

const store = useTeacherStore()
const embedded = inject("tabHubEmbedded", false)
const loadError = ref("")

const kpi = computed(() => {
  const values = store.collegeReport?.crossClass.values ?? []
  if (!values.length) {
    return { classCount: 0, avg: "—", max: "—", min: "—" }
  }
  const sum = values.reduce((a, b) => a + b, 0)
  return {
    classCount: values.length,
    avg: (sum / values.length).toFixed(1),
    max: Math.max(...values).toFixed(1),
    min: Math.min(...values).toFixed(1),
  }
})

const detailRows = computed(() => {
  const report = store.collegeReport
  if (!report?.classDetails?.length) {
    if (!report) return []
    return report.crossClass.classNames.map((name, i) => ({
      className: name,
      courseName: "-",
      semester: "-",
      studentCount: 0,
      avg: report.crossClass.values[i] ?? 0,
      passRate: 0,
      maxScore: 0,
    }))
  }
  return report.classDetails.map((d) => ({
    className: d.className,
    courseName: d.courseName,
    semester: d.semester,
    studentCount: d.studentCount,
    avg: d.avgScore,
    passRate: d.passRate,
    maxScore: d.maxScore,
  }))
})

async function initPage() {
  loadError.value = ""
  try {
    await store.fetchCollegeReport()
  } catch (e: unknown) {
    loadError.value = (e as Error)?.message || "加载学院报告失败"
  }
}

const TREND_COLORS = ["#1677FF", "#00B96B", "#FAAD14", "#722ED1", "#0B3A5C"]

/** 跨班对比：均分叠在满分轨道之上（同任务完成度图样式） */
const crossClassChart = computed(() => {
  const cc = store.collegeReport?.crossClass
  const details = store.collegeReport?.classDetails
  if (!cc?.classNames.length) return null

  const avgs = cc.values.map((v) => Number(v))
  const benchmarks = cc.classNames.map((_, i) => {
    const max = details?.[i]?.maxScore
    return max != null && max > 0 ? Number(max) : 100
  })

  return {
    xAxis: cc.classNames.map((name) => (name.length > 14 ? `${name.slice(0, 12)}…` : name)),
    series: [
      { name: "班级均分", data: avgs, color: "#1677FF" },
      { name: "班级最高分", data: benchmarks, color: "#E8EEF5" },
    ],
  }
})

const semesterTrendChart = computed(() => {
  const trend = store.collegeReport?.semesterTrend
  if (!trend?.semesters.length || !trend.series.length) return null

  const numericValues = trend.series.flatMap((s) =>
    s.data.filter((v): v is number => v != null).map(Number),
  )
  const dataMin = numericValues.length ? Math.min(...numericValues) : 60
  const dataMax = numericValues.length ? Math.max(...numericValues) : 100
  const yMin = Math.max(0, Math.floor((dataMin - 8) / 5) * 5)
  const yMax = Math.min(100, Math.ceil((dataMax + 5) / 5) * 5)

  return {
    xAxis: trend.semesters,
    series: trend.series.map((s, i) => ({
      name: s.name,
      data: s.data.map((v) => (v == null ? null : Number(v))),
      color: TREND_COLORS[i % TREND_COLORS.length],
    })),
    yMin,
    yMax,
  }
})

useReportRefresh(initPage)
</script>

<template>
  <div>
    <PageHeader v-if="!embedded" title="学院报告" subtitle="跨班级对比分析与学期趋势" />

    <LoadingState v-if="store.collegeReportLoading" text="加载学院报告..." />
    <ErrorState v-else-if="loadError" :message="loadError" @retry="initPage" />

    <template v-else-if="store.collegeReport">
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-label">对比班级数</div>
          <div class="stat-value">{{ kpi.classCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">全院均分</div>
          <div class="stat-value">{{ kpi.avg }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">最高班均分</div>
          <div class="stat-value">{{ kpi.max }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">最低班均分</div>
          <div class="stat-value">{{ kpi.min }}</div>
        </div>
      </div>

      <div class="content-grid mt-24">
        <div class="chart-wrap lg">
          <BarChart
            v-if="crossClassChart"
            title="跨班对比"
            :x-axis="crossClassChart.xAxis"
            :series-data="crossClassChart.series"
            overlay-track
            :y-max="100"
            height="360px"
          />
        </div>
        <div class="chart-wrap lg">
          <LineChart
            v-if="semesterTrendChart"
            title="学期趋势（按行政班）"
            :x-axis="semesterTrendChart.xAxis"
            :series-data="semesterTrendChart.series"
            :y-min="semesterTrendChart.yMin"
            :y-max="semesterTrendChart.yMax"
            height="360px"
            :smooth="true"
            :boundary-gap="true"
            :large-symbols="true"
          />
          <div v-else class="chart-empty">暂无学期趋势数据</div>
        </div>
      </div>

      <div class="section mt-24">
        <div class="section-header">
          <h2 class="section-title">跨班明细</h2>
        </div>
        <div class="data-table">
          <table>
            <thead>
              <tr>
                <th>班级</th>
                <th>课程</th>
                <th>学期</th>
                <th>人数</th>
                <th>均分</th>
                <th>及格率</th>
                <th>最高分</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in detailRows" :key="`${row.className}-${row.courseName}`">
                <td>{{ row.className }}</td>
                <td>{{ row.courseName }}</td>
                <td><span class="mono">{{ row.semester }}</span></td>
                <td>{{ row.studentCount }}</td>
                <td><span class="mono">{{ row.avg.toFixed(1) }}</span></td>
                <td>{{ row.passRate.toFixed(1) }}%</td>
                <td><span class="mono good">{{ row.maxScore.toFixed(1) }}</span></td>
              </tr>
              <tr v-if="!detailRows.length">
                <td colspan="7" class="empty">暂无跨班数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
    <div v-else class="empty">暂无学院报告数据</div>
  </div>
</template>

<style scoped>
.mt-24 { margin-top: 24px; }
.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  color: var(--color-navy, #0B3A5C);
}
.mono {
  font-family: var(--font-mono);
  font-weight: 650;
}
.good { color: var(--color-success, #00B96B); }
.empty, .chart-empty {
  text-align: center;
  padding: 28px 0;
  color: var(--color-text-placeholder);
}
.chart-empty {
  min-height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-card, #fff);
  border-radius: 12px;
}
</style>
