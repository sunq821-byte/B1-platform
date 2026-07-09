<script setup lang="ts">
import { ref, onMounted, computed } from "vue"
import { useRouter } from "vue-router"
import { useStudentStore } from "@/stores/useStudentStore"
import { useUserStore } from "@/stores/useUserStore"
import StatCard from "@/components/business/StatCard.vue"
import TaskStatusBadge from "@/components/business/TaskStatusBadge.vue"
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

function handleTaskClick(taskId: string) {
  router.push(`/student/tasks/${taskId}`)
}

const statValues = computed(() => {
  const d = studentStore.dashboardData
  if (!d) return null
  return {
    totalCourses: d.totalCourses,
    totalSubmissions: d.totalSubmissions,
    pendingReviewCount: d.pendingReviewCount,
    averageScore: d.averageScore ?? 0,
  }
})

const statIcons = ["BookOpen", "FileText", "Clock", "Award"]
const statColors = ["#3B82F6", "#10B981", "#F59E0B", "#8B5CF6"]
const statLabels: Record<string, string> = {
  totalCourses: "在学课程",
  totalSubmissions: "已提交作业",
  pendingReviewCount: "待批阅",
  averageScore: "平均成绩",
}

const activityTypeLabels: Record<string, string> = {
  SUBMISSION: "提交了作业",
  AI_COMPLETE: "AI 分析完成",
  TEACHER_REVIEW: "教师评阅完成",
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ""
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}月${d.getDate()}日 ${d.getHours().toString().padStart(2, "0")}:${d.getMinutes().toString().padStart(2, "0")}`
}
</script>

<template>
  <div class="dashboard">
    <PageHeader
      :title="`欢迎回来，${userStore.userName}`"
      :description="today"
    />

    <div v-if="studentStore.dashboardLoading" class="dashboard__loading">
      <LoadingState text="正在加载仪表盘数据..." />
    </div>

    <div v-else-if="loadError" class="dashboard__error">
      <ErrorState :message="loadError" @retry="handleRetry" />
    </div>

    <template v-else-if="studentStore.dashboardData">
      <!-- Stats -->
      <div class="dashboard__stats">
        <StatCard
          v-for="(val, key, idx) in statValues"
          :key="key"
          :title="statLabels[key as string] ?? key"
          :value="val"
          :icon="statIcons[idx]"
          :icon-color="statColors[idx]"
          :suffix="key === 'averageScore' ? '分' : ''"
        />
      </div>

      <div class="dashboard__grid">
        <!-- Upcoming Deadlines -->
        <div class="dashboard__section">
          <h3 class="dashboard__section-title">即将截止</h3>
          <div v-if="studentStore.dashboardData.upcomingDeadlines.length === 0">
            <EmptyState description="暂无即将截止的任务" />
          </div>
          <el-table
            v-else
            :data="studentStore.dashboardData.upcomingDeadlines"
            stripe
            @row-click="(row: any) => handleTaskClick(row.taskId)"
          >
            <el-table-column prop="taskName" label="任务名称" />
            <el-table-column prop="courseName" label="所属课程" />
            <el-table-column prop="deadline" label="截止日期" width="180">
              <template #default="{ row }">
                {{ formatDate(row.deadline) }}
              </template>
            </el-table-column>
            <el-table-column label="剩余" width="100">
              <template #default="{ row }">
                <span
                  class="dashboard__remaining"
                  :class="{ 'dashboard__remaining--urgent': row.remainingDays <= 1 }"
                >
                  {{ row.remainingDays <= 0 ? '今天截止' : `剩余 ${row.remainingDays} 天` }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="130">
              <template #default="{ row }">
                <TaskStatusBadge :status="row.myStatus" />
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- Recent Activities -->
        <div class="dashboard__section">
          <h3 class="dashboard__section-title">最近动态</h3>
          <div v-if="studentStore.dashboardData.recentActivities.length === 0">
            <EmptyState description="暂无动态" />
          </div>
          <div v-else class="dashboard__activities">
            <div
              v-for="(item, idx) in studentStore.dashboardData.recentActivities"
              :key="idx"
              class="dashboard__activity-item"
            >
              <div class="dashboard__activity-dot" :class="`dashboard__activity-dot--${item.type}`" />
              <div class="dashboard__activity-content">
                <div class="dashboard__activity-title">
                  {{ activityTypeLabels[item.type] || item.title }}
                </div>
                <div class="dashboard__activity-desc">{{ item.description }}</div>
                <div class="dashboard__activity-time">{{ formatDate(item.occurredAt) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
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

.dashboard__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-md, 16px);
}

.dashboard__section {
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

.dashboard__remaining {
  font-weight: 500;
  color: var(--color-primary, #3b82f6);
}

.dashboard__remaining--urgent {
  color: var(--color-danger, #ef4444);
  font-weight: 600;
}

.dashboard__activities {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md, 16px);
}

.dashboard__activity-item {
  display: flex;
  gap: var(--spacing-sm, 12px);
  align-items: flex-start;
}

.dashboard__activity-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
  background: var(--color-primary, #3b82f6);
}

.dashboard__activity-dot--AI_COMPLETE {
  background: var(--color-success, #10b981);
}

.dashboard__activity-dot--SUBMISSION {
  background: var(--color-primary, #3b82f6);
}

.dashboard__activity-dot--TEACHER_REVIEW {
  background: var(--color-warning, #f59e0b);
}

.dashboard__activity-content {
  flex: 1;
}

.dashboard__activity-title {
  font-size: var(--font-size-sm, 14px);
  font-weight: 500;
  color: var(--color-text-primary, #1e293b);
}

.dashboard__activity-desc {
  font-size: var(--font-size-xs, 13px);
  color: var(--color-text-secondary, #64748b);
  margin-top: 2px;
}

.dashboard__activity-time {
  font-size: var(--font-size-xs, 12px);
  color: var(--color-text-placeholder, #94a3b8);
  margin-top: 4px;
}

@media (max-width: 1366px) {
  .dashboard__stats {
    grid-template-columns: repeat(2, 1fr);
  }
  .dashboard__grid {
    grid-template-columns: 1fr;
  }
}
</style>
