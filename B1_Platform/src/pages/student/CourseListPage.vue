<script setup lang="ts">
import { ref, onMounted } from "vue"
import { useRouter } from "vue-router"
import { useStudentStore } from "@/stores/useStudentStore"
import PageHeader from "@/components/layout/PageHeader.vue"
import BaseTable from "@/components/base/BaseTable.vue"
import BasePagination from "@/components/base/BasePagination.vue"
import BaseInput from "@/components/base/BaseInput.vue"
import BaseButton from "@/components/base/BaseButton.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"
import EmptyState from "@/components/common/EmptyState.vue"

const router = useRouter()
const store = useStudentStore()

const currentPage = ref(1)
const pageSize = ref(20)
const keyword = ref("")
const loadError = ref("")

async function loadCourses() {
  loadError.value = ""
  try {
    await store.fetchCourses({
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
    })
  } catch (err: unknown) {
    loadError.value = (err as Error)?.message || "加载课程列表失败"
  }
}

function onSearch() {
  currentPage.value = 1
  loadCourses()
}

function handlePageChange() {
  loadCourses()
}

onMounted(() => { loadCourses() })
</script>

<template>
  <div class="course-list-page">
    <PageHeader title="我的课程" subtitle="本学期在学课程与实训进度入口" />

    <div class="filter-bar">
      <BaseInput
        v-model="keyword"
        placeholder="搜索课程名称或编号..."
        clearable
        :style="{ width: '280px' }"
        @keyup.enter="onSearch"
        @clear="onSearch"
      />
      <BaseButton type="primary" class="ml-auto" @click="onSearch">搜索</BaseButton>
    </div>

    <LoadingState v-if="store.coursesLoading" text="加载课程列表..." />

    <ErrorState
      v-else-if="loadError"
      :message="loadError"
      @retry="loadCourses"
    />

    <EmptyState
      v-else-if="store.courses.length === 0"
      description="未找到匹配的课程"
    />

    <template v-else>
      <div class="mobile-card-list" aria-label="课程卡片列表">
        <button
          v-for="row in store.courses"
          :key="String(row.courseId)"
          type="button"
          class="mobile-card"
          @click="router.push(`/student/courses/${row.courseId}`)"
        >
          <div class="mobile-card__title">{{ row.courseName }}</div>
          <div class="mobile-card__meta">{{ row.courseCode }} · {{ row.teacherName }}</div>
          <div class="mobile-card__row">
            <span>{{ row.semester }}</span>
            <span>{{ row.taskCount }} 个任务</span>
          </div>
        </button>
      </div>

      <div class="desktop-table">
      <BaseTable :data="store.courses as unknown as Record<string, unknown>[]">
        <el-table-column label="课程名称" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span
              class="course-list-page__course-name"
              @click="router.push(`/student/courses/${row.courseId}`)"
            >
              {{ row.courseName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="courseCode" label="课程编号" width="120" align="center" />
        <el-table-column prop="teacherName" label="授课教师" width="120" align="center" />
        <el-table-column prop="semester" label="学期" width="140" align="center" />
        <el-table-column prop="credits" label="学分" width="80" align="center" />
        <el-table-column prop="taskCount" label="任务数" width="80" align="center" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <BaseButton size="small" @click="router.push(`/student/courses/${row.courseId}`)">
              查看详情
            </BaseButton>
          </template>
        </el-table-column>
      </BaseTable>
      </div>

      <BasePagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="store.coursesTotal"
        @change="handlePageChange"
      />
    </template>
  </div>
</template>

<style scoped>
.course-list-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.course-list-page__course-name {
  color: var(--color-primary, #1677FF);
  cursor: pointer;
  font-weight: 500;
}

.course-list-page__course-name:hover {
  color: var(--color-primary-hover, #0958D9);
}
.ml-auto { margin-left: auto; }

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
  justify-content: space-between;
  font-size: 13px;
  color: var(--color-text-secondary, #6B7280);
}

@media (max-width: 767.98px) {
  .filter-bar :deep(input),
  .filter-bar :deep(.el-input) {
    width: 100% !important;
    max-width: 100%;
  }
  .mobile-card-list { display: flex; }
  .desktop-table { display: none; }
}
</style>
