<script setup lang="ts">
import { ref, computed, onMounted } from "vue"
import { useRouter } from "vue-router"
import { useTeacherStore } from "@/stores/useTeacherStore"
import { useUserStore } from "@/stores/useUserStore"
import { ElMessage } from "element-plus"
import { BookOpen, PlayCircle, Inbox, Star, Plus } from "lucide-vue-next"
import PageHeader from "@/components/layout/PageHeader.vue"
import BarChart from "@/components/chart/BarChart.vue"
import BaseButton from "@/components/base/BaseButton.vue"

const store = useTeacherStore()
const userStore = useUserStore()
const router = useRouter()

const checkedTasks = ref<Set<string>>(new Set())
const selectAll = ref(false)
const notifying = ref(false)

const today = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`
})

const upcomingTasks = computed(() => store.dashboardData?.upcomingTasks ?? [])

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

onMounted(() => {
  store.fetchDashboard()
})
</script>

<template>
  <div>
    <PageHeader title="工作台">
      <template #subtitle>
        <span>{{ today }} · 欢迎回来，{{ userStore.userName || '教师' }}</span>
      </template>
      <template #extra>
        <BaseButton type="primary" @click="goTo('/teacher/training')">
          <Plus :size="16" />
          新建实训任务
        </BaseButton>
      </template>
    </PageHeader>

    <template v-if="store.dashboardData">
      <!-- Stat Cards -->
      <div class="stats-row">
        <div class="stat-card stat-card--brand" @click="goTo('/teacher/courses')">
          <div class="stat-card__top">
            <span class="stat-label">在教课程</span>
            <div class="stat-icon stat-icon--brand">
              <BookOpen :size="16" />
            </div>
          </div>
          <div class="stat-value">{{ store.dashboardData.stats.classCount }}</div>
          <div class="stat-meta">{{ store.dashboardData.stats.totalStudents }} 名学生</div>
        </div>
        <div class="stat-card stat-card--amber" @click="goTo('/teacher/training')">
          <div class="stat-card__top">
            <span class="stat-label">进行中实训</span>
            <div class="stat-icon stat-icon--amber">
              <PlayCircle :size="16" />
            </div>
          </div>
          <div class="stat-value">{{ store.dashboardData.stats.pendingCount + store.dashboardData.stats.reviewedCount }}</div>
          <div class="stat-meta meta--warn">{{ store.dashboardData.stats.pendingCount }} 个待审核</div>
        </div>
        <div class="stat-card stat-card--red" @click="goTo('/teacher/submissions')">
          <div class="stat-card__top">
            <span class="stat-label">待审核提交</span>
            <div class="stat-icon stat-icon--red">
              <Inbox :size="16" />
            </div>
          </div>
          <div class="stat-value clr-danger">{{ store.dashboardData.stats.pendingCount }}</div>
          <div class="stat-meta meta--danger">需尽快处理</div>
        </div>
        <div class="stat-card stat-card--green" @click="goTo('/teacher/reports')">
          <div class="stat-card__top">
            <span class="stat-label">班级提交率</span>
            <div class="stat-icon stat-icon--green">
              <Star :size="16" />
            </div>
          </div>
          <div class="stat-value clr-success">{{ store.dashboardData.stats.submissionRate }}%</div>
          <div class="stat-meta meta--success">已评阅 {{ store.dashboardData.stats.reviewedCount }}</div>
        </div>
      </div>

      <!-- 2-col Content -->
      <div class="dash-grid mt-24">
        <!-- Upcoming deadline tasks -->
        <div class="card">
          <div class="card__header">
            <h3 class="card__title">快截止实训任务</h3>
            <label v-if="upcomingTasks.length" class="select-all">
              <input
                v-model="selectAll"
                type="checkbox"
                class="select-all__cb"
                @change="toggleAllTasks"
              />
              <span>全选</span>
            </label>
          </div>
          <div class="task-list">
            <div
              v-for="item in upcomingTasks"
              :key="item.taskId"
              class="task-item"
              :class="{ 'task-item--urgent': item.unsubmittedCount > 0 }"
            >
              <input
                type="checkbox"
                class="task-item__cb"
                :checked="checkedTasks.has(item.taskId)"
                @change="toggleTask(item.taskId)"
              />
              <div class="task-item__body">
                <div class="task-item__title">{{ item.taskName }}</div>
                <div class="task-item__meta">
                  <span>{{ item.courseName }}</span>
                  <span class="task-item__due">截止 {{ formatDeadline(item.deadline) }}</span>
                </div>
              </div>
              <span
                class="task-item__badge"
                :class="item.unsubmittedCount > 0 ? 'badge--danger' : 'badge--ok'"
              >未提交 {{ item.unsubmittedCount }} 人</span>
            </div>
            <div v-if="upcomingTasks.length === 0" class="todo-empty">
              近 7 天暂无临近截止的任务
            </div>
          </div>
          <div v-if="hasChecked" class="batch-actions">
            <BaseButton size="small" type="primary" :loading="notifying" @click="batchNotify">
              批量催交（{{ checkedTasks.size }}）
            </BaseButton>
          </div>
        </div>

        <!-- Submission rate + average score chart -->
        <div class="chart-wrap lg">
          <BarChart
            title="班级提交率分布"
            :x-axis="store.dashboardData.submitRateByClass.classNames"
            :series-data="[
              { name: '提交率(%)', data: store.dashboardData.submitRateByClass.values },
              { name: '平均成绩', data: store.dashboardData.submitRateByClass.avgScores, color: '#10B981' },
            ]"
            height="320px"
          />
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
/* Stat Cards */
.stat-card {
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.stat-card::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
}
.stat-card--brand::after { background: var(--color-primary, #3B82F6); }
.stat-card--amber::after { background: var(--color-warning, #F59E0B); }
.stat-card--red::after { background: var(--color-danger, #EF4444); }
.stat-card--green::after { background: var(--color-success, #10B981); }

.stat-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.stat-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-icon--brand { background: #eff6ff; color: var(--color-primary, #3B82F6); }
.stat-icon--amber { background: #fffbeb; color: var(--color-warning, #F59E0B); }
.stat-icon--red { background: #fef2f2; color: var(--color-danger, #EF4444); }
.stat-icon--green { background: #ecfdf5; color: var(--color-success, #10B981); }

.meta--warn { color: var(--color-warning, #F59E0B); }
.meta--danger { color: var(--color-danger, #EF4444); }
.meta--success { color: var(--color-success, #10B981); }

.clr-danger { color: var(--color-danger, #EF4444); }
.clr-success { color: var(--color-success, #10B981); }

/* Card */
.card {
  background: var(--color-card, #fff);
  border: 1px solid var(--color-border, #e2e8f0);
  border-radius: var(--radius-lg, 12px);
  padding: 20px;
}
.card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.card__title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
  margin: 0;
}
.mb-12 { margin-bottom: 12px; }

/* Select all */
.select-all {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--color-text-placeholder, #94a3b8);
  cursor: pointer;
}
.select-all__cb {
  accent-color: var(--color-primary, #3B82F6);
  width: 14px;
  height: 14px;
}

/* Task List */
.task-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.task-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 8px;
  border: 1px solid var(--color-border, #e2e8f0);
}
.task-item--urgent { background: #fef2f2; border-color: #fecaca; }
.task-item__cb {
  accent-color: var(--color-primary, #3B82F6);
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}
.task-item__body { flex: 1; min-width: 0; }
.task-item__title {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-primary, #1e293b);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.task-item__meta {
  display: flex;
  gap: 10px;
  margin-top: 3px;
  font-size: 11px;
  color: var(--color-text-placeholder, #94a3b8);
}
.task-item__due { font-family: var(--font-mono, monospace); }
.task-item__badge {
  flex-shrink: 0;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 100px;
  white-space: nowrap;
}
.badge--danger { background: #fef2f2; color: var(--color-danger, #EF4444); }
.badge--ok { background: #ecfdf5; color: #047857; }
.todo-empty {
  text-align: center;
  padding: 24px 0;
  font-size: 13px;
  color: var(--color-text-placeholder, #94a3b8);
}

/* Batch actions */
.batch-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border, #e2e8f0);
}

/* Dashboard 2-col grid */
.dash-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.5fr);
  gap: 16px;
}

@media (max-width: 992px) {
  .dash-grid {
    grid-template-columns: 1fr;
  }
}
</style>
