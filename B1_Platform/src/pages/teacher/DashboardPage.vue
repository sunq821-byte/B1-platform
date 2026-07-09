<script setup lang="ts">
import { ref, computed, onMounted } from "vue"
import { useRouter } from "vue-router"
import { useTeacherStore } from "@/stores/useTeacherStore"
import { useUserStore } from "@/stores/useUserStore"
import { ElMessage } from "element-plus"
import {
  BookOpen, PlayCircle, Inbox, Star, Plus, Sliders,
  Download, Send, University,
} from "lucide-vue-next"
import PageHeader from "@/components/layout/PageHeader.vue"
import BarChart from "@/components/chart/BarChart.vue"
import BaseButton from "@/components/base/BaseButton.vue"

const store = useTeacherStore()
const userStore = useUserStore()
const router = useRouter()

const checkedTodos = ref<Set<string>>(new Set())
const selectAll = ref(false)

const today = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`
})

function toggleAllTodos() {
  if (selectAll.value) {
    const ids = store.dashboardData?.pendingReviews.map((p) => p.submissionId) ?? []
    checkedTodos.value = new Set(ids)
  } else {
    checkedTodos.value = new Set()
  }
}

function toggleTodo(id: string) {
  const next = new Set(checkedTodos.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  checkedTodos.value = next
  selectAll.value = store.dashboardData
    ? next.size === store.dashboardData.pendingReviews.length
    : false
}

const hasChecked = computed(() => checkedTodos.value.size > 0)

function batchProcess() {
  ElMessage.success(`已标记 ${checkedTodos.value.size} 项为已处理`)
  checkedTodos.value = new Set()
  selectAll.value = false
}

function batchNotify() {
  ElMessage.success(`已向 ${checkedTodos.value.size} 名学生发送催交通知`)
  checkedTodos.value = new Set()
  selectAll.value = false
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

      <!-- 3-col Content -->
      <div class="content-grid content-grid--3 mt-24">
        <!-- Todo List -->
        <div class="card">
          <div class="card__header">
            <h3 class="card__title">待办事项</h3>
            <label class="select-all">
              <input
                v-model="selectAll"
                type="checkbox"
                class="select-all__cb"
                @change="toggleAllTodos"
              />
              <span>全选</span>
            </label>
          </div>
          <div class="todo-list">
            <div
              v-for="item in store.dashboardData.pendingReviews"
              :key="item.submissionId"
              class="todo-item"
              :class="{
                'todo-item--urgent': item.status === 'submitted',
                'todo-item--warn': item.status === 'reviewing',
              }"
            >
              <input
                type="checkbox"
                class="todo-item__cb"
                :checked="checkedTodos.has(item.submissionId)"
                @change="toggleTodo(item.submissionId)"
              />
              <div class="todo-item__body">
                <div class="todo-item__title">{{ item.studentName }} - {{ item.taskName }}</div>
                <div class="todo-item__meta">{{ item.submittedAt.substring(0, 10) }}</div>
              </div>
              <button class="todo-item__action" @click="goTo('/teacher/submissions')">处理</button>
            </div>
            <div v-if="store.dashboardData.pendingReviews.length === 0" class="todo-empty">
              暂无待办事项
            </div>
          </div>
          <div v-if="hasChecked" class="batch-actions">
            <BaseButton size="small" type="primary" @click="batchNotify">批量发送催交</BaseButton>
            <BaseButton size="small" @click="batchProcess">标记已处理</BaseButton>
          </div>
        </div>

        <!-- Grade Distribution Chart -->
        <div class="chart-wrap lg">
          <BarChart
            title="班级提交率分布"
            :x-axis="store.dashboardData.submitRateByClass.classNames"
            :series-data="[{ name: '提交率(%)', data: store.dashboardData.submitRateByClass.values }]"
            height="320px"
          />
        </div>

        <!-- Quick Actions -->
        <div class="card">
          <h3 class="card__title mb-12">快速入口</h3>
          <div class="quick-actions">
            <button class="quick-btn quick-btn--brand" @click="goTo('/teacher/tasks')">
              <Sliders :size="16" />
              新建实训任务
            </button>
            <button class="quick-btn" @click="goTo('/teacher/reports')">
              <Download :size="16" />
              导出班级报表
            </button>
            <button class="quick-btn" @click="batchNotify()">
              <Send :size="16" />
              批量发送催交通知
            </button>
            <button class="quick-btn" @click="goTo('/teacher/reports/college')">
              <University :size="16" />
              查看全院对比视图
            </button>
          </div>
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

/* Todo List */
.todo-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.todo-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}
.todo-item--urgent { background: #fef2f2; border-color: #fecaca; }
.todo-item--warn { background: #fffbeb; border-color: #fde68a; }

.todo-item__cb {
  accent-color: var(--color-primary, #3B82F6);
  width: 16px;
  height: 16px;
  margin-top: 2px;
  flex-shrink: 0;
}
.todo-item__body { flex: 1; min-width: 0; }
.todo-item__title { font-size: 12px; font-weight: 500; color: var(--color-text-primary, #1e293b); }
.todo-item__meta { font-size: 11px; color: var(--color-text-placeholder, #94a3b8); margin-top: 2px; }
.todo-item__action {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--color-primary, #3B82F6);
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}
.todo-item__action:hover { text-decoration: underline; }
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

/* Quick Actions */
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.quick-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid var(--color-border, #e2e8f0);
  background: var(--color-card, #fff);
  color: var(--color-text-primary, #1e293b);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 150ms;
}
.quick-btn:hover { background: #f8fafc; }
.quick-btn--brand {
  border-color: #bfdbfe;
  background: #eff6ff;
  color: var(--color-primary, #3B82F6);
}
.quick-btn--brand:hover { background: #dbeafe; }

/* 3-col grid */
.content-grid--3 {
  grid-template-columns: 1fr 1fr 1fr;
}

@media (max-width: 1366px) {
  .content-grid--3 {
    grid-template-columns: 1fr;
  }
}
</style>
