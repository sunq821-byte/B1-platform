<script setup lang="ts">
defineProps<{
  description?: string
  actionText?: string
}>()

const emit = defineEmits<{
  action: []
}>()
</script>

<template>
  <div class="empty-state">
    <svg
      class="empty-state__icon"
      width="64"
      height="64"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="1.5"
      stroke-linecap="round"
      stroke-linejoin="round"
    >
      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
      <polyline points="17 8 12 3 7 8" />
      <line x1="12" y1="3" x2="12" y2="15" />
    </svg>
    <p class="empty-state__text">{{ description ?? "暂无数据" }}</p>
    <el-button
      v-if="actionText"
      type="primary"
      size="small"
      @click="emit('action')"
    >
      {{ actionText }}
    </el-button>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-2xl, 48px) var(--spacing-lg, 24px);
}

.empty-state__icon {
  color: var(--color-text-placeholder, #94a3b8);
  margin-bottom: var(--spacing-md, 16px);
}

.empty-state__text {
  font-size: var(--font-size-sm, 14px);
  color: var(--color-text-secondary, #64748b);
  margin: 0 0 var(--spacing-md, 16px) 0;
}
</style>
