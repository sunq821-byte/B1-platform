<script setup lang="ts">
import { AlertCircle } from "lucide-vue-next"

withDefaults(defineProps<{
  message?: string
  showRetry?: boolean
}>(), {
  message: "加载失败，请稍后重试",
  showRetry: true,
})

defineEmits<{
  retry: []
}>()
</script>

<template>
  <div class="error-state">
    <AlertCircle class="error-state__icon" :size="48" />
    <p class="error-state__message">{{ message }}</p>
    <el-button v-if="showRetry" type="primary" plain @click="$emit('retry')">
      重试
    </el-button>
  </div>
</template>

<style scoped>
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-2xl, 48px) var(--spacing-lg, 24px);
}

.error-state__icon {
  color: var(--color-danger, #EF4444);
  margin-bottom: var(--spacing-md, 16px);
}

.error-state__message {
  font-size: var(--font-size-sm, 14px);
  color: var(--color-text-secondary, #64748B);
  margin: 0 0 var(--spacing-lg, 24px) 0;
}
</style>
