<script setup lang="ts">
import { onMounted, computed } from "vue"
import { useStudentStore } from "@/stores/useStudentStore"
import { useUserStore } from "@/stores/useUserStore"
import PageHeader from "@/components/layout/PageHeader.vue"
import LineChart from "@/components/chart/LineChart.vue"
import RadarChart from "@/components/chart/RadarChart.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import EmptyState from "@/components/common/EmptyState.vue"

const store = useStudentStore()
const userStore = useUserStore()

onMounted(async () => {
  await store.fetchGrowth()
})

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
    { name: "我的平均", data: store.growthData.dimensionRadar.map(d => d.myAvg ?? 0), color: "#3B82F6" },
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
    { name: "平均成绩", data: store.growthData.monthlyTrends.map(t => t.avgScore ?? 0), color: "#3B82F6" },
    { name: "提交数", data: store.growthData.monthlyTrends.map(t => t.submissionCount), color: "#10B981" },
  ]
})

function scoreColor(score: number) {
  if (score >= 80) return "var(--color-success, #10b981)"
  if (score >= 60) return "var(--color-warning, #f59e0b)"
  return "var(--color-danger, #ef4444)"
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
      title="成长中心"
      :subtitle="`${userStore.userName} · 能力成长轨迹`"
    />

    <LoadingState
      v-if="store.growthLoading && !store.growthData"
      text="加载成长数据..."
    />

    <template v-if="store.growthData">
      <!-- Stats -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-label">全部任务</div>
          <div class="stat-value">{{ store.growthData.totalTasks }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">已完成</div>
          <div class="stat-value" style="color: var(--color-success, #10B981);">{{ store.growthData.completedTasks }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">平均成绩</div>
          <div class="stat-value">{{ fmtScore(store.growthData.averageScore) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">最高 / 最低</div>
          <div class="stat-value">
            <span style="color: var(--color-success, #10B981);">{{ fmtScore(store.growthData.highestScore) }}</span>
            <span style="font-size:14px;color:var(--color-text-placeholder,#94a3b8);"> / </span>
            <span style="color: var(--color-danger, #EF4444);">{{ fmtScore(store.growthData.lowestScore) }}</span>
          </div>
        </div>
      </div>

      <!-- Charts -->
      <div class="content-grid mt-24">
        <div class="chart-wrap lg">
          <RadarChart
            v-if="radarCategories.length"
            title="能力维度雷达图"
            :categories="radarCategories"
            :series-data="radarSeries"
            height="340px"
          />
          <EmptyState v-else description="暂无维度数据" />
        </div>
        <div class="chart-wrap lg">
          <LineChart
            v-if="trendCategories.length"
            title="月度趋势"
            :x-axis="trendCategories"
            :series-data="trendSeries"
            height="340px"
          />
          <EmptyState v-else description="暂无趋势数据" />
        </div>
      </div>

      <!-- Course Scores -->
      <div class="section mt-24" v-if="store.growthData.courseScores.length">
        <h2 class="section-title">课程成绩分布</h2>
        <div class="data-table mt-12">
          <table>
            <thead>
              <tr>
                <th>课程名称</th>
                <th>平均分</th>
                <th>任务数</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="cs in store.growthData.courseScores" :key="cs.courseId">
                <td>{{ cs.courseName }}</td>
                <td>
                  <span style="font-family:var(--font-mono);" :style="{ color: scoreColor(cs.avgScore ?? 0) }">
                    {{ fmtScore(cs.avgScore) }}
                  </span>
                </td>
                <td>{{ cs.taskCount }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Submission History -->
      <div class="section mt-24">
        <h2 class="section-title">提交历史</h2>
        <div v-if="!store.growthData.submissionHistory.length" class="p-8">
          <EmptyState description="暂无提交记录" />
        </div>
        <div v-else class="data-table mt-12">
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
                  <span v-else class="badge badge--default">{{ sh.result || '处理中' }}</span>
                </td>
                <td style="font-family:var(--font-mono);font-size:12px;">{{ formatDate(sh.submittedAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.p-8 { padding: var(--spacing-2xl, 48px); }
.mt-12 { margin-top: 12px; }
.section-title {
  font-size: var(--font-size-base, 16px);
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
  margin: 0;
}
</style>
