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
    <PageHeader title="实训任务">
      <template #extra>
        <div class="task-list-page__filters">
          <BaseSelect
            v-model="statusFilter"
            :options="statusOptions"
            placeholder="全部状态"
            :style="{ width: '140px' }"
          />
          <BaseInput
            v-model="keyword"
            placeholder="搜索任务名称"
            :style="{ width: '220px' }"
            @keyup.enter="handleSearch"
          />
          <BaseButton @click="handleSearch">
            <Search :size="16" />
          </BaseButton>
        </div>
      </template>
    </PageHeader>

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
            {{ formatDate(row.deadline as string) }}
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
            {{ formatScore(row.myScore) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <BaseButton size="small" @click="viewDetail(row.taskId as string)">
              查看详情
            </BaseButton>
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

.task-list-page__filters {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm, 8px);
}

.task-list-page__link {
  color: var(--color-primary, #3b82f6);
  cursor: pointer;
  font-weight: 500;
}

.task-list-page__link:hover {
  color: var(--color-primary-hover, #2563eb);
}
</style>
