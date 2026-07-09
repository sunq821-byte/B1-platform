<script setup lang="ts">
import { onMounted } from "vue"
import { ElMessage } from "element-plus"
import { useStudentStore } from "@/stores/useStudentStore"
import { useUserStore } from "@/stores/useUserStore"
import PageHeader from "@/components/layout/PageHeader.vue"
import LineChart from "@/components/chart/LineChart.vue"
import RadarChart from "@/components/chart/RadarChart.vue"
import EmptyState from "@/components/common/EmptyState.vue"

const store = useStudentStore()
const userStore = useUserStore()

onMounted(() => { store.fetchStudentReport() })

function handleExport() {
  ElMessage.success("导出功能演示：Excel 文件将包含当前页面数据")
}

function fmtScore(s: number | null): string {
  if (s === null || s === undefined) return "-"
  return s.toFixed(1)
}

function scoreColor(score: number) {
  if (score >= 80) return "var(--color-success, #10b981)"
  if (score >= 60) return "var(--color-warning, #f59e0b)"
  return "var(--color-danger, #ef4444)"
}
</script>

<template>
  <div>
    <PageHeader
      title="个人报告"
      :subtitle="`${userStore.userName} · 2025-2026 学年第2学期`"
    />

    <template v-if="store.studentReport">
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-label">全部任务</div>
          <div class="stat-value">{{ store.studentReport.stats.totalTasks }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">已完成</div>
          <div class="stat-value" style="color:var(--color-success, #10b981);">{{ store.studentReport.stats.completedTasks }}</div>
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
            height="340px"
          />
        </div>
        <div class="chart-wrap lg">
          <RadarChart
            v-if="store.studentReport.radarData.indicators.length"
            title="能力雷达图"
            :categories="store.studentReport.radarData.indicators"
            :series-data="[{ name: '个人', data: store.studentReport.radarData.values }]"
            height="340px"
          />
          <EmptyState v-else description="暂无雷达数据" />
        </div>
      </div>

      <div class="section mt-24">
        <div class="section-header">
          <h2 class="section-title">成绩明细</h2>
          <button class="btn btn-outline btn-sm" @click="handleExport">导出 Excel</button>
        </div>
        <div v-if="store.studentReport.rows.length === 0" class="p-8">
          <EmptyState description="暂无成绩记录" />
        </div>
        <div v-else class="data-table">
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
                <td><span style="color:var(--color-text-secondary,#64748b);">{{ row.courseName }}</span></td>
                <td>
                  <span
                    v-if="row.score != null"
                    style="font-family:var(--font-mono);font-weight:500;"
                    :style="{ color: scoreColor(row.score) }"
                  >{{ row.score.toFixed(1) }}</span>
                  <span v-else style="color:var(--color-text-placeholder,#94a3b8);">-</span>
                </td>
                <td>
                  <span style="font-size:13px;color:var(--color-text-secondary,#64748b);">
                    {{ row.reviewComment || '-' }}
                  </span>
                </td>
                <td>
                  <span v-if="row.status === 'REVIEWED'" class="badge badge--success">已评阅</span>
                  <span v-else-if="row.status === 'REJECTED'" class="badge badge--danger">已退回</span>
                  <span v-else-if="row.status === 'SUBMITTED'" class="badge badge--info">已提交</span>
                  <span v-else class="badge badge--default">{{ row.status }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>
