<script setup lang="ts">
import { ref, computed, onMounted } from "vue"
import { useTeacherStore } from "@/stores/useTeacherStore"
import PageHeader from "@/components/layout/PageHeader.vue"
import BarChart from "@/components/chart/BarChart.vue"
import LineChart from "@/components/chart/LineChart.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"

const store = useTeacherStore()
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
  if (!report) return []
  return report.crossClass.classNames.map((name, i) => ({
    className: name,
    avg: report.crossClass.values[i] ?? 0,
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

onMounted(() => { initPage() })
</script>

<template>
  <div>
    <PageHeader title="学院报告" subtitle="跨班级对比分析与学期趋势" />

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
            title="跨班对比"
            :x-axis="store.collegeReport.crossClass.classNames"
            :series-data="[{ name: '班级均分', data: store.collegeReport.crossClass.values }]"
            height="360px"
          />
        </div>
        <div class="chart-wrap lg">
          <LineChart
            title="学期趋势"
            :x-axis="store.collegeReport.semesterTrend.semesters"
            :series-data="store.collegeReport.semesterTrend.series"
            height="360px"
            :smooth="true"
          />
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
                <th>均分</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in detailRows" :key="row.className">
                <td>{{ row.className }}</td>
                <td><span class="mono">{{ row.avg.toFixed(1) }}</span></td>
              </tr>
              <tr v-if="!detailRows.length">
                <td colspan="2" class="empty">暂无跨班数据</td>
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
.empty {
  text-align: center;
  padding: 28px 0;
  color: var(--color-text-placeholder);
}
</style>
