<script setup lang="ts">
import { onMounted } from "vue"
import { ElMessage } from "element-plus"
import { useStudentStore } from "@/stores/useStudentStore"
import { useUserStore } from "@/stores/useUserStore"
import PageHeader from "@/components/layout/PageHeader.vue"
import LineChart from "@/components/chart/LineChart.vue"
import RadarChart from "@/components/chart/RadarChart.vue"

const store = useStudentStore()
const userStore = useUserStore()

onMounted(() => { store.fetchStudentReport() })

function handleExport() {
  ElMessage.success("导出功能演示：Excel 文件将包含当前页面数据")
}

const scoreColor = (score: number) => {
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
          <div class="stat-label">已完成任务</div>
          <div class="stat-value">{{ store.studentReport.stats.completedTasks }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">平均成绩</div>
          <div class="stat-value">{{ store.studentReport.stats.averageScore }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">最高分</div>
          <div class="stat-value" style="color:var(--color-success, #10b981);">{{ store.studentReport.stats.maxScore }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">最低分</div>
          <div class="stat-value" style="color:var(--color-danger, #ef4444);">{{ store.studentReport.stats.minScore }}</div>
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
            title="能力雷达图"
            :categories="store.studentReport.radarData.indicators"
            :series-data="[{ name: '个人', data: store.studentReport.radarData.values }]"
            height="340px"
          />
        </div>
      </div>

      <div class="section mt-24">
        <div class="section-header">
          <h2 class="section-title">成绩明细</h2>
          <button class="btn btn-outline btn-sm" @click="handleExport">导出 Excel</button>
        </div>
        <div class="data-table">
          <table>
            <thead>
              <tr>
                <th>任务名称</th>
                <th>课程</th>
                <th>AI评分</th>
                <th>最终成绩</th>
                <th>评价时间</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in store.studentReport.rows" :key="row.taskName + row.reviewedAt">
                <td>{{ row.taskName }}</td>
                <td><span style="color:var(--color-text-secondary,#64748b);">{{ row.courseName }}</span></td>
                <td><span style="font-family:var(--font-mono);">{{ row.aiScore != null ? row.aiScore.toFixed(1) : '-' }}</span></td>
                <td><span style="font-family:var(--font-mono);font-weight:500;" :style="{ color: scoreColor(row.finalScore) }">{{ row.finalScore.toFixed(1) }}</span></td>
                <td><span style="font-family:var(--font-mono);font-size:11px;">{{ row.reviewedAt.substring(0, 10) }}</span></td>
                <td>
                  <span v-if="row.status === 'approved'" class="badge badge--success">已通过</span>
                  <span v-else class="badge badge--danger">已驳回</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>
