<script setup lang="ts">
import { ref, computed, onMounted } from "vue"
import { useRoute, useRouter } from "vue-router"
import { useStudentStore } from "@/stores/useStudentStore"
import { ArrowLeft } from "lucide-vue-next"
import PageHeader from "@/components/layout/PageHeader.vue"
import BaseTable from "@/components/base/BaseTable.vue"
import BaseButton from "@/components/base/BaseButton.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"
import EmptyState from "@/components/common/EmptyState.vue"
import TaskStatusBadge from "@/components/business/TaskStatusBadge.vue"

const route = useRoute()
const router = useRouter()
const store = useStudentStore()

const courseId = computed(() => route.params.courseId as string)
const loading = ref(false)
const loadError = ref("")

async function loadCourseDetail() {
  loading.value = true
  loadError.value = ""
  try {
    await store.fetchCourseDetail(courseId.value)
  } catch (err: unknown) {
    loadError.value = (err as Error)?.message || "加载课程详情失败"
  } finally {
    loading.value = false
  }
}

function formatDate(dateStr: string) {
  if (!dateStr) return "-"
  return dateStr.replace("T", " ").substring(0, 16)
}

function goToTask(taskId: string) {
  router.push(`/student/submit/${taskId}`)
}

function formatScore(score: unknown) {
  if (score === null || score === undefined) return "-"
  return String(score)
}

onMounted(() => { loadCourseDetail() })
</script>

<template>
  <div class="course-detail-page">
    <button type="button" class="back-btn" @click="router.push('/student/courses')">
      <ArrowLeft :size="16" />
      返回课程列表
    </button>
    <PageHeader :title="store.currentCourse?.courseName ?? '课程详情'" subtitle="课程信息与关联实训任务" />

    <LoadingState v-if="loading" text="加载课程详情..." />
    <ErrorState v-else-if="loadError" :message="loadError" @retry="loadCourseDetail" />

    <template v-else-if="store.currentCourse">
      <div class="course-detail-page__card">
        <div class="course-detail-page__card-title">课程信息</div>
        <div class="course-detail-page__info-grid">
          <div class="course-detail-page__info-item">
            <span class="course-detail-page__label">课程名称</span>
            <span class="course-detail-page__value">{{ store.currentCourse.courseName }}</span>
          </div>
          <div class="course-detail-page__info-item">
            <span class="course-detail-page__label">课程编号</span>
            <span class="course-detail-page__value">{{ store.currentCourse.courseCode }}</span>
          </div>
          <div class="course-detail-page__info-item">
            <span class="course-detail-page__label">授课教师</span>
            <span class="course-detail-page__value">{{ store.currentCourse.teacherName }}</span>
          </div>
          <div class="course-detail-page__info-item">
            <span class="course-detail-page__label">教师邮箱</span>
            <span class="course-detail-page__value">{{ store.currentCourse.teacherEmail }}</span>
          </div>
          <div class="course-detail-page__info-item">
            <span class="course-detail-page__label">学期</span>
            <span class="course-detail-page__value">{{ store.currentCourse.semester }}</span>
          </div>
          <div class="course-detail-page__info-item">
            <span class="course-detail-page__label">学分</span>
            <span class="course-detail-page__value">{{ store.currentCourse.credits }} 学分</span>
          </div>
        </div>
        <div class="course-detail-page__description">{{ store.currentCourse.description }}</div>
      </div>

      <div class="course-detail-page__card">
        <div class="course-detail-page__card-title">实训任务</div>
        <BaseTable
          v-if="store.currentCourse.tasks.length > 0"
          :data="store.currentCourse.tasks as unknown as Record<string, unknown>[]"
        >
          <el-table-column label="任务名称" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="course-detail-page__task-link" @click="goToTask(row.taskId as string)">
                {{ row.taskName }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="截止日期" width="160" align="center">
            <template #default="{ row }">{{ formatDate(row.deadline as string) }}</template>
          </el-table-column>
          <el-table-column prop="totalScore" label="总分" width="80" align="center" />
          <el-table-column label="提交状态" width="110" align="center">
            <template #default="{ row }">
              <TaskStatusBadge :status="row.mySubmissionStatus as string" />
            </template>
          </el-table-column>
          <el-table-column label="我的成绩" width="80" align="center">
            <template #default="{ row }">{{ formatScore(row.myScore) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <BaseButton size="small" @click="goToTask(row.taskId as string)">查看详情</BaseButton>
            </template>
          </el-table-column>
        </BaseTable>
        <EmptyState v-else description="暂无实训任务" />
      </div>
    </template>
  </div>
</template>

<style scoped>
.course-detail-page { height: 100%; display: flex; flex-direction: column; }
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  border: none;
  background: transparent;
  color: var(--color-text-secondary, #64748b);
  font-size: 13px;
  cursor: pointer;
  padding: 0;
  width: fit-content;
}
.back-btn:hover { color: var(--color-primary, #1677FF); }
.course-detail-page__card {
  background: var(--color-card, #fff);
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: var(--radius-xl, 16px);
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: var(--shadow-card, 0 1px 2px rgba(15,23,42,.04));
}
.course-detail-page__card-title {
  font-size: 16px;
  font-weight: 800;
  color: var(--color-navy, #0B3A5C);
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--color-border, #e2e8f0);
}
.course-detail-page__info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }

@media (max-width: 767.98px) {
  .course-detail-page__info-grid { grid-template-columns: 1fr; }
}
.course-detail-page__info-item { display: flex; flex-direction: column; gap: 4px; }
.course-detail-page__label { font-size: 14px; color: var(--color-text-secondary, #64748b); }
.course-detail-page__value { font-size: 16px; color: var(--color-text-primary, #1e293b); font-weight: 500; }
.course-detail-page__description {
  margin-top: 24px;
  padding: 16px;
  background: var(--color-bg, #f8fafc);
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
}
.course-detail-page__task-link { color: var(--color-primary, #1677FF); cursor: pointer; font-weight: 500; }
.course-detail-page__task-link:hover { color: var(--color-primary-hover, #0958D9); }
</style>
