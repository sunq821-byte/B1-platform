<script setup lang="ts">
import { ref, computed, onMounted } from "vue"
import { useRoute, useRouter } from "vue-router"
import { useStudentStore } from "@/stores/useStudentStore"
import { ArrowLeft } from "lucide-vue-next"
import { ElMessage } from "element-plus"
import request from "@/api/request"
import PageHeader from "@/components/layout/PageHeader.vue"
import BaseTable from "@/components/base/BaseTable.vue"
import BaseButton from "@/components/base/BaseButton.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"
import TaskStatusBadge from "@/components/business/TaskStatusBadge.vue"
import { navigateStudentTaskAction } from "@/utils/studentTaskNavigation"
interface TrialHistoryItem {
  trialId?: string
  submissionId: string
  status: string
  submitCount: number
  submittedAt: string
  aiScore?: number
}

const route = useRoute()
const router = useRouter()
const store = useStudentStore()

const taskId = computed(() => route.params.taskId as string)
const loading = ref(false)
const loadError = ref("")
const confirming = ref(false)
const history = ref<TrialHistoryItem[]>([])

const actionState = computed(() => {
  const s = store.currentTask?.mySubmissionStatus
  if (s === "NOT_SUBMITTED" || s === "REJECTED" || s === "DRAFT") return "submit"
  if (s === "ANALYZING" || s === "AI_EVALUATING") return "analyzing"
  if (s === "AI_READY" || s === "AI_COMPLETED") return "confirm"
  if (s === "PUBLISHED" || s === "COMPLETED") return "viewGrade"
  if (s === "CONFIRMED" || s === "REVIEWING" || s === "TEACHER_SCORING") return "submitted"
  return "idle"
})

const canConfirm = computed(() =>
  ["AI_READY", "ANALYZING", "DRAFT"].includes(store.currentTask?.mySubmissionStatus || "")
  || history.value.length > 0,
)

function formatDate(dateStr: string) {
  if (!dateStr) return "-"
  return dateStr.replace("T", " ").substring(0, 16)
}

const submissionLabel: Record<string, string> = { ZIP_UPLOAD: "文件上传", ONLINE_CODE: "代码提交", FILE_CODE: "文件或代码", ZIP_ONLY: "文件上传" }

async function loadHistory() {
  try {
    history.value = await request.get(`/api/v1/student/tasks/${taskId.value}/submissions`) as TrialHistoryItem[]
  } catch {
    history.value = []
  }
}

async function loadTaskDetail() {
  loading.value = true
  loadError.value = ""
  try {
    await store.fetchTaskDetail(taskId.value)
    await loadHistory()
  } catch (err: unknown) {
    loadError.value = (err as Error)?.message || "加载任务详情失败"
  } finally {
    loading.value = false
  }
}

function handleAction() {
  const task = store.currentTask
  if (!task) return
  navigateStudentTaskAction(router, {
    taskId: task.taskId,
    status: task.mySubmissionStatus,
    submissionId: task.mySubmissionId,
  })
}

async function handleConfirmSubmit() {
  confirming.value = true
  try {
    const lastTrial = history.value[history.value.length - 1]
    await store.confirmSubmit(taskId.value, lastTrial?.trialId)
    ElMessage.success("已确认提交教师审核")
    await loadTaskDetail()
  } catch (err: unknown) {
    ElMessage.error((err as Error)?.message || "确认提交失败")
  } finally {
    confirming.value = false
  }
}

function viewAIDiagnosis() {
  const sid = store.currentTask?.mySubmissionId
  if (!sid) return
  if (sid) router.push(`/student/grades/${sid}`)
}

onMounted(() => { loadTaskDetail() })
</script>

<template>
  <div class="task-detail-page">
    <div class="task-detail-page__top">
      <span class="task-detail-page__back" @click="router.push('/student/tasks')"><ArrowLeft :size="20" /></span>
      <PageHeader :title="store.currentTask?.taskName || '任务详情'" />
    </div>

    <LoadingState v-if="loading" text="加载任务详情..." />
    <ErrorState v-else-if="loadError" :message="loadError" @retry="loadTaskDetail" />

    <template v-else-if="store.currentTask">
      <!-- Info card -->
      <div class="card">
        <div class="card__title">任务信息</div>
        <div class="info-grid">
          <div class="info-item"><span class="info-item__label">任务名称</span><span class="info-item__val">{{ store.currentTask.taskName }}</span></div>
          <div class="info-item"><span class="info-item__label">所属课程</span><span class="info-item__val">{{ store.currentTask.courseName }}</span></div>
          <div class="info-item"><span class="info-item__label">授课教师</span><span class="info-item__val">{{ store.currentTask.teacherName }}</span></div>
          <div class="info-item"><span class="info-item__label">截止日期</span><span class="info-item__val">{{ formatDate(store.currentTask.deadline) }}</span></div>
          <div class="info-item"><span class="info-item__label">总分</span><span class="info-item__val">{{ store.currentTask.totalScore }}</span></div>
          <div class="info-item"><span class="info-item__label">提交方式</span><span class="info-item__val">{{ submissionLabel[store.currentTask.submissionType] || store.currentTask.submissionType }}</span></div>
          <div class="info-item"><span class="info-item__label">当前状态</span><TaskStatusBadge :status="store.currentTask.mySubmissionStatus" /></div>
          <div class="info-item"><span class="info-item__label">AI 次数</span><span class="info-item__val">{{ store.currentTask.mySubmitCount ?? 0 }} / {{ store.currentTask.maxAiAnalyzeCount ?? store.currentTask.maxSubmitCount ?? 3 }}</span></div>
          <div class="info-item"><span class="info-item__label">成绩</span><span class="info-item__val">{{ store.currentTask.myScore ?? "-" }}</span></div>
        </div>
      </div>

      <!-- Description -->
      <div class="card">
        <div class="card__title">任务描述</div>
        <div class="card__body">{{ store.currentTask.description || "暂无描述" }}</div>
      </div>

      <!-- Requirements -->
      <div class="card">
        <div class="card__title">任务要求</div>
        <div class="card__body">{{ store.currentTask.requirements || "暂无要求" }}</div>
      </div>

      <!-- Evaluation Dimensions -->
      <div class="card">
        <div class="card__title">评分维度</div>
        <BaseTable :data="store.currentTask.evaluationDimensions as unknown as Record<string, unknown>[]">
          <el-table-column prop="dimensionName" label="维度名称" min-width="140" />
          <el-table-column prop="weight" label="权重" width="90" align="center">
            <template #default="{ row }">{{ row.weight }}%</template>
          </el-table-column>
          <el-table-column prop="maxScore" label="满分" width="80" align="center" />
        </BaseTable>
      </div>

      <!-- Trial timeline -->
      <div v-if="history.length" class="card">
        <div class="card__title">试交时间线</div>
        <div class="timeline">
          <div v-for="item in history" :key="item.trialId || item.submitCount" class="timeline__item">
            <div class="timeline__head">
              <span class="timeline__no">第 {{ item.submitCount }} 次试交</span>
              <TaskStatusBadge :status="item.status" />
            </div>
            <div class="timeline__meta">{{ formatDate(item.submittedAt) }}</div>
            <div v-if="item.aiScore != null" class="timeline__score">AI 参考分：{{ item.aiScore }}（非最终成绩）</div>
          </div>
        </div>
        <p class="timeline__hint">试交内容教师不可见；确认提交后才会进入教师审核队列。</p>
      </div>

      <!-- Action -->
      <div class="card">
        <div class="card__actions">
          <BaseButton v-if="actionState === 'submit'" type="primary" @click="handleAction">
            {{ store.currentTask?.mySubmissionStatus === "REJECTED" ? "重新试交" : "试交成果" }}
          </BaseButton>
          <div v-else-if="actionState === 'analyzing'" class="analyzing">
            <span class="analyzing__spinner" />
            <span>AI 正在分析中，请稍候...</span>
          </div>
          <template v-else-if="actionState === 'confirm'">
            <BaseButton type="primary" :loading="confirming" :disabled="!canConfirm" @click="handleConfirmSubmit">
              确认提交教师审核
            </BaseButton>
            <BaseButton @click="handleAction">继续试交</BaseButton>
          </template>
          <BaseButton v-else-if="actionState === 'viewGrade'" type="primary" @click="handleAction">查看成绩</BaseButton>
          <BaseButton
            v-if="(actionState === 'viewGrade' || actionState === 'confirm') && store.currentTask?.mySubmissionId"
            @click="viewAIDiagnosis"
          >查看 AI 诊断</BaseButton>
          <span v-else-if="actionState === 'submitted'" class="idle-text">已递交教师，请等待复核</span>
          <span v-else-if="actionState === 'idle'" class="idle-text">暂无可用操作</span>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.task-detail-page { height: 100%; display: flex; flex-direction: column; overflow-y: auto; padding-bottom: var(--spacing-lg, 24px); }
.task-detail-page__top { display: flex; align-items: center; gap: var(--spacing-sm, 8px); margin-bottom: var(--spacing-lg, 24px); }
.task-detail-page__back { display: flex; align-items: center; justify-content: center; color: var(--color-text-secondary, #64748b); cursor: pointer; padding: var(--spacing-xs, 4px); border-radius: var(--radius-sm, 4px); transition: color .2s, background .2s; flex-shrink: 0; }
.task-detail-page__back:hover { color: var(--color-primary, #1677FF); background: var(--color-bg, #f8fafc); }

.card { background: var(--color-card, #fff); border-radius: var(--radius-md, 8px); padding: var(--spacing-lg, 24px); margin-bottom: var(--spacing-md, 16px); box-shadow: var(--shadow-sm, 0 1px 2px rgba(0,0,0,.05)); }
.card__title { font-size: var(--font-size-md, 16px); font-weight: 600; color: var(--color-text-primary, #1e293b); margin-bottom: var(--spacing-md, 16px); padding-bottom: var(--spacing-sm, 8px); border-bottom: 1px solid var(--color-border, #e2e8f0); }
.card__body { padding: var(--spacing-md, 16px); background: var(--color-bg, #f8fafc); border-radius: var(--radius-sm, 4px); font-size: var(--font-size-sm, 14px); color: var(--color-text-primary, #1e293b); white-space: pre-wrap; line-height: 1.6; }
.card__actions { padding-top: var(--spacing-sm, 8px); display: flex; flex-wrap: wrap; gap: var(--spacing-sm, 8px); align-items: center; }

.info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: var(--spacing-md, 16px); }

@media (max-width: 767.98px) {
  .info-grid { grid-template-columns: 1fr; }
  .timeline__head { flex-direction: column; align-items: flex-start; }
}
.info-item { display: flex; flex-direction: column; gap: var(--spacing-xs, 4px); }
.info-item__label { font-size: var(--font-size-sm, 14px); color: var(--color-text-secondary, #64748b); }
.info-item__val { font-size: var(--font-size-md, 16px); color: var(--color-text-primary, #1e293b); font-weight: 500; }

.timeline { display: flex; flex-direction: column; gap: var(--spacing-md, 16px); }
.timeline__item { padding: var(--spacing-md, 16px); background: var(--color-bg, #f8fafc); border-radius: var(--radius-sm, 4px); border-left: 3px solid var(--color-primary, #1677FF); }
.timeline__head { display: flex; align-items: center; justify-content: space-between; gap: var(--spacing-sm, 8px); margin-bottom: var(--spacing-xs, 4px); }
.timeline__no { font-weight: 600; color: var(--color-text-primary, #1e293b); }
.timeline__meta { font-size: var(--font-size-sm, 14px); color: var(--color-text-secondary, #64748b); }
.timeline__score { margin-top: var(--spacing-xs, 4px); font-size: var(--font-size-sm, 14px); color: var(--color-ai, #722ED1); }
.timeline__hint { margin-top: var(--spacing-md, 16px); font-size: var(--font-size-sm, 14px); color: var(--color-text-secondary, #64748b); }

.analyzing { display: flex; align-items: center; gap: var(--spacing-sm, 8px); font-size: var(--font-size-sm, 14px); color: var(--color-text-secondary, #64748b); }
.analyzing__spinner { width: 16px; height: 16px; border: 2px solid var(--color-border, #e2e8f0); border-top-color: var(--color-primary, #1677FF); border-radius: 50%; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.idle-text { font-size: var(--font-size-sm, 14px); color: var(--color-text-secondary, #64748b); }
</style>
