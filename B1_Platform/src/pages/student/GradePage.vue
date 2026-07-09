<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue"
import { useRoute, useRouter } from "vue-router"
import { useStudentStore } from "@/stores/useStudentStore"
import AIScorePanel from "@/components/business/AIScorePanel.vue"
import PageHeader from "@/components/layout/PageHeader.vue"
import BaseButton from "@/components/base/BaseButton.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"

const route = useRoute()
const router = useRouter()
const store = useStudentStore()

const submissionId = computed(() => route.params.submissionId as string)
const loading = ref(true)
const loadError = ref("")
const pollingError = ref("")

const isRejected = computed(() => store.evaluation?.status === "REJECTED")

const isPolling = computed(() => store.aiPollingStatus === "polling")
const pollingProgress = computed(() => store.aiResult?.progress ?? 0)
const currentDimension = computed(() => store.aiResult?.currentDimension ?? "")

async function initPage() {
  loading.value = true
  loadError.value = ""
  pollingError.value = ""

  try {
    await store.fetchAIResult(submissionId.value)
    const status = store.aiResult?.status
    if (!status || status === "NOT_STARTED" || status === "FAILED") {
      await store.triggerAIEvaluation(submissionId.value)
      loading.value = false
      try {
        await store.pollAIResult(submissionId.value)
      } catch (err: unknown) {
        pollingError.value = (err as Error)?.message || "AI分析超时"
        return
      }
    } else if (status !== "COMPLETED") {
      loading.value = false
      try {
        await store.pollAIResult(submissionId.value)
      } catch (err: unknown) {
        pollingError.value = (err as Error)?.message || "AI分析超时"
        return
      }
    }
    await store.fetchEvaluation(submissionId.value)
  } catch (err: unknown) {
    loadError.value = (err as Error)?.message || "加载成绩失败"
  } finally {
    loading.value = false
  }
}

function handleRetry() {
  store.stopPolling()
  initPage()
}

onMounted(() => { initPage() })
onUnmounted(() => { store.stopPolling() })
</script>

<template>
  <div class="grade-page">
    <PageHeader title="我的成绩">
      <template #extra>
        <BaseButton size="small" @click="router.back()">返回</BaseButton>
      </template>
    </PageHeader>

    <div v-if="loading" class="grade-page__state">
      <LoadingState text="正在加载成绩数据..." />
    </div>

    <div v-else-if="loadError" class="grade-page__state">
      <ErrorState :message="loadError" @retry="handleRetry" />
    </div>

    <template v-else>
      <AIScorePanel
        :ai-result="store.aiResult?.result ?? null"
        :is-polling="isPolling"
        :polling-progress="pollingProgress"
        :current-dimension="currentDimension"
        :polling-error="pollingError"
        @retry="handleRetry"
      />

      <!-- Rejected state -->
      <div v-if="isRejected" class="grade-page__reject">
        <h3 class="grade-page__section-title">作业已退回</h3>
        <p v-if="store.evaluation?.rejectReason || store.evaluation?.teacherEvaluation?.comment" class="grade-page__reject-text">
          {{ store.evaluation?.rejectReason || store.evaluation?.teacherEvaluation?.comment }}
        </p>
        <div class="grade-page__reject-actions">
          <BaseButton type="primary" @click="router.push(`/student/submit/${store.evaluation?.taskId}`)">
            重新提交
          </BaseButton>
        </div>
      </div>

      <!-- Teacher Evaluation -->
      <div v-else-if="store.evaluation?.teacherEvaluation" class="grade-page__teacher-review">
        <h3 class="grade-page__section-title">教师评审</h3>
        <div class="grade-page__teacher-score">
          <span class="grade-page__teacher-label">教师评分</span>
          <span class="grade-page__teacher-value">{{ store.evaluation.teacherEvaluation.overallScore }} 分</span>
        </div>
        <p v-if="store.evaluation.teacherEvaluation.comment" class="grade-page__teacher-comment">
          {{ store.evaluation.teacherEvaluation.comment }}
        </p>
        <p class="grade-page__reviewer">
          评审人：{{ store.evaluation.teacherEvaluation.scoredBy }}
          · {{ store.evaluation.teacherEvaluation.scoredAt }}
        </p>
      </div>
    </template>
  </div>
</template>

<style scoped>
.grade-page { max-width: 900px; margin: 0 auto; }
.grade-page__state { padding: var(--spacing-2xl, 48px) 0; }

.grade-page__section-title {
  font-size: var(--font-size-base, 16px);
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
  margin: 0 0 var(--spacing-md, 16px) 0;
}

.grade-page__teacher-review {
  background: var(--color-card, #fff);
  border-radius: var(--radius-lg, 12px);
  box-shadow: var(--shadow-sm);
  padding: var(--spacing-lg, 24px);
  margin-top: var(--spacing-md, 16px);
}

.grade-page__teacher-score {
  display: flex;
  align-items: center;
  gap: var(--spacing-md, 16px);
  margin-bottom: var(--spacing-md, 16px);
}

.grade-page__teacher-label {
  font-size: var(--font-size-sm, 14px);
  color: var(--color-text-secondary, #64748b);
}

.grade-page__teacher-value {
  font-size: var(--font-size-2xl, 24px);
  font-weight: 700;
  color: var(--color-primary, #3b82f6);
}

.grade-page__teacher-comment {
  font-size: var(--font-size-sm, 14px);
  color: var(--color-text-secondary, #64748b);
  line-height: 1.7;
  background: var(--color-bg, #f8fafc);
  padding: var(--spacing-md, 16px);
  border-radius: var(--radius-md, 8px);
  border-left: 4px solid var(--color-primary, #3b82f6);
}

.grade-page__reviewer {
  font-size: var(--font-size-xs, 12px);
  color: var(--color-text-placeholder, #94a3b8);
  margin-top: var(--spacing-sm, 8px);
}

.grade-page__reject {
  background: var(--color-card, #fff);
  border-radius: var(--radius-lg, 12px);
  box-shadow: var(--shadow-sm);
  padding: var(--spacing-lg, 24px);
  margin-top: var(--spacing-md, 16px);
}

.grade-page__reject-text {
  font-size: var(--font-size-sm, 14px);
  color: var(--color-danger, #EF4444);
  line-height: 1.6;
  margin: 0 0 var(--spacing-md, 16px) 0;
}

.grade-page__reject-actions {
  display: flex;
  gap: 12px;
}
</style>
