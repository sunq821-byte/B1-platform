<script setup lang="ts">
import { ref, onMounted, onActivated, watch } from "vue"
import { useRouter } from "vue-router"
import { useStudentStore } from "@/stores/useStudentStore"
import { Search } from "lucide-vue-next"
import PageHeader from "@/components/layout/PageHeader.vue"
import BaseTable from "@/components/base/BaseTable.vue"
import BasePagination from "@/components/base/BasePagination.vue"
import BaseInput from "@/components/base/BaseInput.vue"
import BaseButton from "@/components/base/BaseButton.vue"
import BaseSelect from "@/components/base/BaseSelect.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"
import EmptyState from "@/components/common/EmptyState.vue"
import TaskStatusBadge from "@/components/business/TaskStatusBadge.vue"
import { navigateStudentTaskAction } from "@/utils/studentTaskNavigation"

const router = useRouter()
const store = useStudentStore()

const currentPage = ref(1)
const pageSize = ref(20)
const statusFilter = ref("")
const keyword = ref("")
const loadError = ref("")

const statusOptions = [
  { label: "全部状态", value: "" },
  { label: "未开始", value: "NOT_SUBMITTED" },
  { label: "试交中", value: "DRAFT" },
  { label: "AI 分析中", value: "ANALYZING" },
  { label: "待确认提交", value: "AI_READY" },
  { label: "已递交教师", value: "CONFIRMED" },
  { label: "教师复核中", value: "REVIEWING" },
  { label: "已发布成绩", value: "PUBLISHED" },
  { label: "未完成", value: "INCOMPLETE" },
  { label: "已驳回", value: "REJECTED" },
]

async function loadTasks() {
  loadError.value = ""
  try {
    await store.fetchTasks({
      page: currentPage.value,
      pageSize: pageSize.value,
      status: statusFilter.value || undefined,
      keyword: keyword.value || undefined,
    })
  } catch (err: unknown) {
    loadError.value = (err as Error)?.message || "加载任务列表失败"
  }
}

function handleSearch() {
  currentPage.value = 1
  loadTasks()
}

function handlePageChange() {
  loadTasks()
}

function handleRowAction(row: {
  taskId: string
  mySubmissionStatus: string
  mySubmissionId?: string | null
}) {
  navigateStudentTaskAction(router, {
    taskId: row.taskId,
    status: row.mySubmissionStatus,
    submissionId: row.mySubmissionId,
  })
}

function canSubmit(status: string): boolean {
  return status === "NOT_SUBMITTED" || status === "REJECTED" || status === "DRAFT"
}

function getActionLabel(status: string): string {
  if (canSubmit(status)) return "去提交"
  if (status === "AI_READY" || status === "AI_COMPLETED") return "查看评分"
  if (status === "PUBLISHED" || status === "COMPLETED") return "查看成绩"
  if (status === "ANALYZING" || status === "AI_EVALUATING") return "查看进度"
  return "进入任务"
}

function formatDate(dateStr: string) {
  if (!dateStr) return "-"
  return dateStr.replace("T", " ").substring(0, 16)
}

function formatScore(score: unknown) {
  if (score === null || score === undefined) return "-"
  return String(score)
}

watch([statusFilter], () => {
  currentPage.value = 1
  loadTasks()
})

onMounted(() => {
  loadTasks()
})

onActivated(() => {
  loadTasks()
})
</script>

<template>
  <div class="task-list-page">
    <PageHeader title="实训任务" subtitle="按状态跟踪提交与反馈进度" />

    <div class="filter-bar">
      <BaseSelect
        v-model="statusFilter"
        :options="statusOptions"
        placeholder="全部状态"
        :style="{ width: '160px' }"
      />
      <BaseInput
        v-model="keyword"
        placeholder="搜索任务名称"
        :style="{ width: '240px' }"
        @keyup.enter="handleSearch"
      />
      <BaseButton type="primary" class="ml-auto" @click="handleSearch">
        <Search :size="16" />
        搜索
      </BaseButton>
    </div>

    <LoadingState v-if="store.tasksLoading" text="加载任务列表..." />

    <ErrorState
      v-else-if="loadError"
      :message="loadError"
      @retry="loadTasks"
    />

    <EmptyState
      v-else-if="store.tasks.length === 0"
      description="暂无实训任务数据"
    />

    <template v-else>
      <div class="mobile-card-list" aria-label="任务卡片列表">
        <button
          v-for="row in store.tasks"
          :key="String(row.taskId)"
          type="button"
          class="mobile-card"
          @click="handleRowAction({
            taskId: row.taskId as string,
            mySubmissionStatus: row.mySubmissionStatus as string,
            mySubmissionId: row.mySubmissionId as string | null,
          })"
        >
          <div class="mobile-card__title">{{ row.taskName }}</div>
          <div class="mobile-card__meta">{{ row.courseName }} · {{ row.teacherName }}</div>
          <div class="mobile-card__row">
            <TaskStatusBadge :status="row.mySubmissionStatus as string" />
            <span class="mono">截止 {{ formatDate(row.deadline as string) }}</span>
          </div>
          <div class="mobile-card__row">
            <span class="mono">成绩 {{ formatScore(row.myScore) }}</span>
            <span class="mobile-card__action">{{ getActionLabel(row.mySubmissionStatus as string) }}</span>
          </div>
        </button>
      </div>

      <div class="desktop-table">
      <BaseTable :data="store.tasks as unknown as Record<string, unknown>[]">
        <el-table-column label="任务名称" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span
              class="task-list-page__link"
              @click="handleRowAction({
                taskId: row.taskId as string,
                mySubmissionStatus: row.mySubmissionStatus as string,
                mySubmissionId: row.mySubmissionId as string | null,
              })"
            >
              {{ row.taskName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="courseName" label="课程名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="教师" width="100" align="center" />
        <el-table-column label="截止日期" width="160" align="center">
          <template #default="{ row }">
            <span class="mono">{{ formatDate(row.deadline as string) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分" width="80" align="center" />
        <el-table-column label="提交状态" width="110" align="center">
          <template #default="{ row }">
            <TaskStatusBadge :status="row.mySubmissionStatus as string" />
          </template>
        </el-table-column>
        <el-table-column label="成绩" width="80" align="center">
          <template #default="{ row }">
            <span class="mono">{{ formatScore(row.myScore) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <BaseButton
              size="small"
              :type="canSubmit(row.mySubmissionStatus as string) ? 'primary' : 'default'"
              @click="handleRowAction({
                taskId: row.taskId as string,
                mySubmissionStatus: row.mySubmissionStatus as string,
                mySubmissionId: row.mySubmissionId as string | null,
              })"
            >
              {{ getActionLabel(row.mySubmissionStatus as string) }}
            </BaseButton>
          </template>
        </el-table-column>
      </BaseTable>
      </div>

      <BasePagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="store.tasksTotal"
        @change="handlePageChange"
      />
    </template>
  </div>
</template>

<style scoped>
.task-list-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.task-list-page__link {
  color: var(--color-primary, #1677FF);
  cursor: pointer;
  font-weight: 500;
}
.ml-auto { margin-left: auto; }

.task-list-page__link:hover {
  color: var(--color-primary-hover, #0958D9);
}

.mono {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--color-text-secondary);
}

.mobile-card-list {
  display: none;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
}

.mobile-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
  text-align: left;
  padding: 14px 16px;
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: var(--radius-xl, 16px);
  background: var(--color-card, #fff);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  font: inherit;
  color: inherit;
}

.mobile-card__title {
  font-size: 15px;
  font-weight: 700;
  color: var(--color-navy, #0B3A5C);
}

.mobile-card__meta {
  font-size: 12px;
  color: var(--color-text-secondary, #6B7280);
}

.mobile-card__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.mobile-card__action {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-primary, #1677FF);
}

@media (max-width: 767.98px) {
  .filter-bar :deep(input),
  .filter-bar :deep(.base-input),
  .filter-bar :deep(.el-input),
  .filter-bar :deep(select) {
    width: 100% !important;
    max-width: 100%;
  }

  .mobile-card-list { display: flex; }
  .desktop-table { display: none; }
}
</style>
