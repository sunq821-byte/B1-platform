<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
}>()
</script>

<template>
  <div class="page-header">
    <div class="page-header__left">
      <h1 class="page-header__title">{{ title }}</h1>
      <p v-if="subtitle" class="page-header__subtitle">{{ subtitle }}</p>
    </div>
    <div v-if="$slots.extra" class="page-header__extra">
      <slot name="extra" />
    </div>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: var(--spacing-lg, 24px);
}

.page-header__left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-header__title {
  font-size: var(--font-size-xl, 20px);
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
  margin: 0;
}

.page-header__subtitle {
  font-size: var(--font-size-sm, 14px);
  color: var(--color-text-secondary, #64748b);
  margin: 0;
}

.page-header__extra {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm, 8px);
}
</style>
