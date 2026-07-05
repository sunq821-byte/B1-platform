<script setup lang="ts">
import { computed, type Component } from "vue"
import * as LucideIcons from "lucide-vue-next"

const props = withDefaults(defineProps<{
  title: string
  value: number | string
  icon?: string
  iconColor?: string
  loading?: boolean
  suffix?: string
}>(), {
  iconColor: "#3B82F6",
  loading: false,
})

const iconComponent = computed<Component | null>(() => {
  if (!props.icon) return null
  const comp = (LucideIcons as Record<string, unknown>)[props.icon]
  if (!comp) return null
  return comp as Component
})
</script>

<template>
  <div class="stat-card" :class="{ 'stat-card--loading': loading }">
    <div class="stat-card__header">
      <div
        v-if="iconComponent && !loading"
        class="stat-card__icon-circle"
        :style="{ backgroundColor: iconColor + '15', color: iconColor }"
      >
        <component :is="iconComponent" :size="24" />
      </div>
      <div v-else-if="loading" class="stat-card__icon-circle stat-card__skeleton-circle" />

      <div v-if="!loading" class="stat-card__value">
        {{ value }}<span v-if="suffix" class="stat-card__suffix">{{ suffix }}</span>
      </div>
      <div v-else class="stat-card__skeleton-value" />
    </div>
    <div class="stat-card__footer">
      <span v-if="!loading" class="stat-card__title">{{ title }}</span>
      <span v-else class="stat-card__skeleton-title" />
    </div>
  </div>
</template>

<style scoped>
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: box-shadow 200ms ease;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-card__header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-card__icon-circle {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-card__value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.2;
}

.stat-card__suffix {
  font-size: 14px;
  font-weight: 400;
  color: #94a3b8;
  margin-left: 2px;
}

.stat-card__footer {
  display: flex;
  align-items: center;
}

.stat-card__title {
  font-size: 13px;
  color: #64748b;
}

/* ---------- skeleton shimmer ---------- */
.stat-card__skeleton-circle {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.stat-card__skeleton-value {
  width: 80px;
  height: 32px;
  border-radius: 6px;
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.stat-card__skeleton-title {
  width: 56px;
  height: 14px;
  border-radius: 4px;
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>



