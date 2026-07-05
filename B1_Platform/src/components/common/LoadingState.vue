<script setup lang="ts">
withDefaults(defineProps<{
  text?: string
}>(), {
  text: "加载中...",
})
</script>

<template>
  <div class="loading-state">
    <div class="loading-state__spinner">
      <div class="loading-state__dot" />
      <div class="loading-state__dot" />
      <div class="loading-state__dot" />
    </div>
    <p class="loading-state__text">{{ text }}</p>
  </div>
</template>

<style scoped>
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-2xl, 48px) var(--spacing-lg, 24px);
}

.loading-state__spinner {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: var(--spacing-md, 16px);
}

.loading-state__dot {
  width: 10px;
  height: 10px;
  border-radius: var(--radius-full, 9999px);
  background: var(--color-primary, #3B82F6);
  animation: loading-bounce 1.4s ease-in-out infinite both;
}

.loading-state__dot:nth-child(1) {
  animation-delay: -0.32s;
}

.loading-state__dot:nth-child(2) {
  animation-delay: -0.16s;
}

.loading-state__text {
  font-size: var(--font-size-sm, 14px);
  color: var(--color-text-secondary, #64748B);
  margin: 0;
}

@keyframes loading-bounce {
  0%, 80%, 100% {
    opacity: 0.4;
    transform: scale(0.8);
  }
  40% {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
