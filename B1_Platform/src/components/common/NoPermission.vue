<script setup lang="ts">
import { Lock } from "lucide-vue-next"

withDefaults(defineProps<{
  message?: string
}>(), {
  message: "抱歉，您没有权限访问此内容",
})
</script>

<template>
  <div class="no-permission">
    <Lock class="no-permission__icon" :size="52" />
    <p class="no-permission__message">{{ message }}</p>
  </div>
</template>

<style scoped>
.no-permission {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-2xl, 48px) var(--spacing-lg, 24px);
}

.no-permission__icon {
  color: var(--color-text-placeholder, #94A3B8);
  margin-bottom: var(--spacing-md, 16px);
}

.no-permission__message {
  font-size: var(--font-size-sm, 14px);
  color: var(--color-text-secondary, #64748B);
  margin: 0;
}
</style>
