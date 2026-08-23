<script setup lang="ts">
import { ref, onMounted, watch } from "vue"
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

const router = useRouter()
const store = useStudentStore()

const currentPage = ref(1)
const pageSize = ref(20)
const statusFilter = ref("")
const keyword = ref("")
const loadError = ref("")

const statusOptions = [
  { label: "全部状态", value: "" },
  { label: "未提交", value: "NOT_SUBMITTED" },
  { label: "已提交", value: "SUBMITTED" },
  { label: "AI分析中", value: "AI_EVALUATING" },
  { label: "AI已完成", value: "AI_COMPLETED" },
  { label: "教师评分中", value: "TEACHER_SCORING" },
  { label: "已完成", value: "COMPLETED" },
  { label: "已退回", value: "REJECTED" },
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

function viewDetail(taskId: string) {
  router.push(`/student/tasks/${taskId}`)
}

function goSubmit(taskId: string) {
  router.push(`/student/submit/${taskId}`)
}

function canSubmit(status: string): boolean {
  return status === "NOT_SUBMITTED" || status === "REJECTED"
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
      <BaseTable :data="store.tasks as unknown as Record<string, unknown>[]">
        <el-table-column label="任务名称" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span
              class="task-list-page__link"
              @click="viewDetail(row.taskId as string)"
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
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <div class="ops">
              <BaseButton size="small" @click="viewDetail(row.taskId as string)">详情</BaseButton>
              <BaseButton
                v-if="canSubmit(row.mySubmissionStatus as string)"
                size="small"
                type="primary"
                @click="goSubmit(row.taskId as string)"
              >去提交</BaseButton>
            </div>
          </template>
        </el-table-column>
      </BaseTable>

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
.ops { display: flex; gap: 6px; justify-content: center; flex-wrap: wrap; }

.task-list-page__link:hover {
  color: var(--color-primary-hover, #0958D9);
}

.mono {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--color-text-secondary);
}
</style>
