<script setup lang="ts">
import { computed } from "vue"
import { CheckCircle, XCircle } from "lucide-vue-next"
import type { IAIScoreResult } from "@/types/student"
import ErrorState from "@/components/common/ErrorState.vue"

const props = withDefaults(defineProps<{
  aiResult: IAIScoreResult | null
  isPolling?: boolean
  pollingProgress?: number
  currentDimension?: string
  pollingError?: string
}>(), {
  isPolling: false,
  pollingProgress: 0,
  currentDimension: "",
  pollingError: "",
})

defineEmits<{ retry: [] }>()

const scoreColor = computed(() => {
  const score = props.aiResult?.overallScore ?? 0
  if (score >= 80) return "#10B981"
  if (score >= 60) return "#F59E0B"
  return "#EF4444"
})

const remainingTime = computed(() => {
  if (props.pollingProgress <= 0) return 60
  return Math.max(0, Math.round(60 * (1 - props.pollingProgress / 100)))
})
</script>

<template>
  <div class="ai-score-panel">
    <!-- Polling mode -->
    <div v-if="isPolling" class="ai-score-panel__polling">
      <div class="ai-score-panel__polling-header">
        <div class="ai-score-panel__spinner" />
        <span>AI 正在分析您的代码...</span>
      </div>
      <el-progress
        :percentage="pollingProgress"
        :stroke-width="8"
        :color="pollingProgress >= 100 ? '#10B981' : '#3B82F6'"
      />
      <div class="ai-score-panel__polling-info">
        <span v-if="currentDimension">正在分析：{{ currentDimension }}</span>
        <span>预计还需约 {{ remainingTime }}s</span>
      </div>
    </div>

    <!-- Error mode -->
    <div v-else-if="pollingError" class="ai-score-panel__error">
      <ErrorState :text="pollingError" @retry="$emit('retry')" />
    </div>

    <!-- Completed mode -->
    <template v-else-if="aiResult">
      <div class="ai-score-panel__overall">
        <div
          class="ai-score-panel__score-ring"
          :style="{ color: scoreColor, borderColor: scoreColor }"
        >
          <span class="ai-score-panel__score-value">{{ aiResult.overallScore }}</span>
          <span class="ai-score-panel__score-label">AI 综合评分</span>
        </div>
      </div>

      <div class="ai-score-panel__dimensions">
        <div
          v-for="dim in aiResult.dimensions"
          :key="dim.dimensionName"
          class="ai-score-panel__dim-card"
        >
          <div class="ai-score-panel__dim-header">
            <span class="ai-score-panel__dim-name">{{ dim.dimensionName }}</span>
            <span class="ai-score-panel__dim-weight">{{ dim.weight }}%</span>
          </div>
          <div class="ai-score-panel__dim-score">
            <span class="ai-score-panel__dim-score-value">{{ dim.score }}</span>
            <span class="ai-score-panel__dim-score-max">/ {{ dim.maxScore }}</span>
          </div>
          <p class="ai-score-panel__dim-comment">{{ dim.comment }}</p>
          <div v-if="dim.suggestions.length > 0" class="ai-score-panel__dim-suggestions">
            <div
              v-for="s in dim.suggestions"
              :key="s"
              class="ai-score-panel__suggestion-item"
            >{{ s }}</div>
          </div>
          <div v-if="dim.codeReferences.length > 0" class="ai-score-panel__dim-references">
            <span class="ai-score-panel__references-label">相关代码：</span>
            <code
              v-for="ref in dim.codeReferences"
              :key="ref"
              class="ai-score-panel__code-ref"
            >{{ ref }}</code>
          </div>
        </div>
      </div>

      <div class="ai-score-panel__summary">
        <p class="ai-score-panel__summary-text">{{ aiResult.summary }}</p>

        <div class="ai-score-panel__summary-section">
          <div class="ai-score-panel__summary-title">
            <CheckCircle :size="16" color="#10B981" />
            <span>优势亮点</span>
          </div>
          <ul class="ai-score-panel__list">
            <li v-for="item in aiResult.strengths" :key="item">{{ item }}</li>
          </ul>
        </div>

        <div class="ai-score-panel__summary-section">
          <div class="ai-score-panel__summary-title">
            <XCircle :size="16" color="#EF4444" />
            <span>待改进</span>
          </div>
          <ul class="ai-score-panel__list">
            <li v-for="item in aiResult.weaknesses" :key="item">{{ item }}</li>
          </ul>
        </div>

        <div class="ai-score-panel__summary-section">
          <div class="ai-score-panel__summary-title">
            <span>改进建议</span>
          </div>
          <p class="ai-score-panel__improvement">{{ aiResult.improvementPlan }}</p>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.ai-score-panel { width: 100%; }

.ai-score-panel__polling {
  text-align: center;
  padding: var(--spacing-lg, 24px);
}

.ai-score-panel__polling-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm, 8px);
  margin-bottom: var(--spacing-lg, 24px);
  font-size: var(--font-size-md, 16px);
  color: var(--color-text-primary, #1e293b);
  font-weight: 500;
}

.ai-score-panel__spinner {
  width: 20px;
  height: 20px;
  border: 2px solid var(--color-border, #e2e8f0);
  border-top-color: var(--color-primary, #3b82f6);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.ai-score-panel__polling-info {
  display: flex;
  justify-content: space-between;
  margin-top: var(--spacing-sm, 8px);
  font-size: var(--font-size-sm, 14px);
  color: var(--color-text-secondary, #64748b);
}

.ai-score-panel__error {
  padding: var(--spacing-lg, 24px);
}

.ai-score-panel__overall {
  text-align: center;
  padding: var(--spacing-xl, 32px) 0;
}

.ai-score-panel__score-ring {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  border: 4px solid;
  border-radius: 50%;
}

.ai-score-panel__score-value {
  font-size: 48px;
  font-weight: 700;
  font-family: "JetBrains Mono", "SF Mono", monospace;
  line-height: 1;
}

.ai-score-panel__score-label {
  font-size: var(--font-size-xs, 12px);
  color: var(--color-text-secondary, #64748b);
  margin-top: 4px;
}

.ai-score-panel__dimensions {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md, 16px);
}

.ai-score-panel__dim-card {
  padding: var(--spacing-md, 16px);
  background: var(--color-bg, #f8fafc);
  border-radius: var(--radius-sm, 4px);
}

.ai-score-panel__dim-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xs, 4px);
}

.ai-score-panel__dim-name {
  font-size: var(--font-size-md, 16px);
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
}

.ai-score-panel__dim-weight {
  padding: 2px 8px;
  background: var(--color-primary, #3b82f6);
  color: #fff;
  border-radius: var(--radius-sm, 4px);
  font-size: var(--font-size-xs, 12px);
}

.ai-score-panel__dim-score {
  margin-bottom: var(--spacing-sm, 8px);
}

.ai-score-panel__dim-score-value {
  font-size: 24px;
  font-weight: 700;
  font-family: "JetBrains Mono", "SF Mono", monospace;
  color: var(--color-text-primary, #1e293b);
}

.ai-score-panel__dim-score-max {
  font-size: var(--font-size-sm, 14px);
  color: var(--color-text-secondary, #64748b);
}

.ai-score-panel__dim-comment {
  font-size: var(--font-size-sm, 14px);
  color: var(--color-text-secondary, #64748b);
  margin: 0 0 var(--spacing-sm, 8px) 0;
}

.ai-score-panel__dim-suggestions {
  margin-bottom: var(--spacing-sm, 8px);
}

.ai-score-panel__suggestion-item {
  font-size: var(--font-size-sm, 14px);
  color: var(--color-text-secondary, #64748b);
  padding-left: var(--spacing-md, 16px);
  position: relative;
}

.ai-score-panel__suggestion-item::before {
  content: "\2022";
  position: absolute;
  left: 4px;
}

.ai-score-panel__dim-references {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--spacing-xs, 4px);
}

.ai-score-panel__references-label {
  font-size: var(--font-size-xs, 12px);
  color: var(--color-text-placeholder, #94a3b8);
}

.ai-score-panel__code-ref {
  font-size: var(--font-size-xs, 12px);
  font-family: "JetBrains Mono", "SF Mono", monospace;
  padding: 2px 6px;
  background: #E2E8F0;
  border-radius: 2px;
  color: var(--color-text-primary, #1e293b);
}

.ai-score-panel__summary {
  margin-top: var(--spacing-lg, 24px);
  padding: var(--spacing-md, 16px);
  background: var(--color-bg, #f8fafc);
  border-radius: var(--radius-sm, 4px);
}

.ai-score-panel__summary-text {
  font-size: var(--font-size-sm, 14px);
  color: var(--color-text-primary, #1e293b);
  line-height: 1.6;
  margin: 0 0 var(--spacing-md, 16px) 0;
}

.ai-score-panel__summary-section {
  margin-bottom: var(--spacing-md, 16px);
}

.ai-score-panel__summary-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs, 4px);
  font-size: var(--font-size-sm, 14px);
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
  margin-bottom: var(--spacing-sm, 8px);
}

.ai-score-panel__list {
  margin: 0;
  padding-left: var(--spacing-lg, 24px);
}

.ai-score-panel__list li {
  font-size: var(--font-size-sm, 14px);
  color: var(--color-text-secondary, #64748b);
  line-height: 1.6;
}

.ai-score-panel__improvement {
  font-size: var(--font-size-sm, 14px);
  color: var(--color-text-secondary, #64748b);
  line-height: 1.6;
  margin: 0;
}
</style>
