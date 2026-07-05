<script setup lang="ts">
import { ref, onMounted, computed } from "vue"
import { useRouter } from "vue-router"
import { useStudentStore } from "@/stores/useStudentStore"
import { useUserStore } from "@/stores/useUserStore"
import StatCard from "@/components/business/StatCard.vue"
import TaskStatusBadge from "@/components/business/TaskStatusBadge.vue"
import LineChart from "@/components/chart/LineChart.vue"
import RadarChart from "@/components/chart/RadarChart.vue"
import PageHeader from "@/components/layout/PageHeader.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"
import EmptyState from "@/components/common/EmptyState.vue"

const router = useRouter()
const studentStore = useStudentStore()
const userStore = useUserStore()

const loadError = ref("")

const today = computed(() => {
  const d = new Date()
  const weekDays = ["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"]
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 ${weekDays[d.getDay()]}`
})

onMounted(async () => {
  try {
    await studentStore.fetchDashboard()
  } catch (err: unknown) {
    loadError.value = (err as Error)?.message || "加载仪表盘数据失败"
  }
})

function handleRetry() {
  loadError.value = ""
  studentStore.fetchDashboard().catch((err: unknown) => {
    loadError.value = (err as Error)?.message || "加载仪表盘数据失败"
  })
}

function handleTaskClick(row: { taskId: string; status: string; score: number | null }) {
  if (["COMPLETED", "AI_COMPLETED", "TEACHER_SCORING"].includes(row.status) && row.score !== null) {
    router.push(`/student/grades/SUB${row.taskId}`)
  }
}

const scoreTrendSeries = computed(() => {
  if (!studentStore.dashboardData) return []
  return [
    { name: "我的成绩", data: studentStore.dashboardData.scoreTrend.myScores, color: "#3B82F6" },
    { name: "班级平均", data: studentStore.dashboardData.scoreTrend.classAvg, color: "#94A3B8" },
  ]
})

const radarSeries = computed(() => {
  if (!studentStore.dashboardData) return []
  return [
    { name: "我的能力", data: studentStore.dashboardData.radarData.myScores, color: "#3B82F6" },
    { name: "班级平均", data: studentStore.dashboardData.radarData.classAvg, color: "#94A3B8" },
  ]
})

const statIcons = ["Clock", "CheckCircle", "Award", "Cpu"]
const statColors = ["#F59E0B", "#10B981", "#3B82F6", "#8B5CF6"]
</script>

<template>
  <div class="dashboard">
    <PageHeader
      :title="`欢迎回来，${userStore.userName}`"
      :description="today"
    />

    <!-- Stats -->
    <div v-if="studentStore.dashboardLoading" class="dashboard__loading">
      <LoadingState text="正在加载仪表盘数据..." />
    </div>

    <div v-else-if="loadError" class="dashboard__error">
      <ErrorState :message="loadError" @retry="handleRetry" />
    </div>

    <template v-else-if="studentStore.dashboardData">
      <div class="dashboard__stats">
        <StatCard
          v-for="(val, key, idx) in studentStore.dashboardData.stats"
          :key="key"
          :title="{ pendingTasks: '待完成任务', completedTasks: '已完成任务', averageScore: '平均成绩', analyzingCount: 'AI分析中' }[key as string] ?? key"
          :value="val"
          :icon="statIcons[idx]"
          :icon-color="statColors[idx]"
          :suffix="key === 'averageScore' ? '分' : key === 'analyzingCount' ? '个' : ''"
        />
      </div>

      <!-- Charts -->
      <div class="dashboard__charts">
        <LineChart
          title="成绩趋势"
          :x-axis="studentStore.dashboardData.scoreTrend.xAxis"
          :series-data="scoreTrendSeries"
        />
        <RadarChart
          title="能力维度"
          :categories="studentStore.dashboardData.radarData.categories"
          :series-data="radarSeries"
        />
      </div>

      <!-- Recent Tasks -->
      <div class="dashboard__tasks">
        <h3 class="dashboard__section-title">近期任务</h3>
        <div v-if="studentStore.dashboardData.recentTasks.length === 0">
          <EmptyState description="暂无任务" />
        </div>
        <el-table
          v-else
          :data="studentStore.dashboardData.recentTasks"
          stripe
          @row-click="handleTaskClick"
        >
          <el-table-column prop="taskName" label="任务名称" />
          <el-table-column prop="courseName" label="所属课程" />
          <el-table-column prop="deadline" label="截止日期" width="160" />
          <el-table-column label="状态" width="130">
            <template #default="{ row }">
              <TaskStatusBadge :status="row.status" />
            </template>
          </el-table-column>
          <el-table-column label="成绩" width="80" align="center">
            <template #default="{ row }">
              <span v-if="row.score !== null" class="dashboard__score">{{ row.score }}</span>
              <span v-else class="dashboard__no-score">-</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </template>
  </div>
</template>

<style scoped>
.dashboard {
  /* width/centering handled by AppLayout */
}

.dashboard__loading,
.dashboard__error {
  padding: var(--spacing-2xl, 48px) 0;
}

.dashboard__stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-md, 16px);
  margin-bottom: var(--spacing-lg, 24px);
}

.dashboard__charts {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--spacing-md, 16px);
  margin-bottom: var(--spacing-lg, 24px);
}

.dashboard__tasks {
  background: var(--color-card, #fff);
  border-radius: var(--radius-lg, 12px);
  box-shadow: var(--shadow-sm);
  padding: var(--spacing-lg, 24px);
}

.dashboard__section-title {
  font-size: var(--font-size-base, 16px);
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
  margin: 0 0 var(--spacing-md, 16px) 0;
}

.dashboard__score {
  font-weight: 600;
  color: var(--color-primary, #3b82f6);
}

.dashboard__no-score {
  color: var(--color-text-placeholder, #94a3b8);
}

@media (max-width: 1366px) {
  .dashboard__stats {
    grid-template-columns: repeat(2, 1fr);
  }
  .dashboard__charts {
    grid-template-columns: 1fr;
  }
}
</style>

