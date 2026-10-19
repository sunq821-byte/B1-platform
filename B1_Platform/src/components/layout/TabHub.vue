<script setup lang="ts">
import { computed, provide } from "vue"
import { useRoute, useRouter } from "vue-router"
import PageHeader from "@/components/layout/PageHeader.vue"

export interface TabHubItem {
  key: string
  label: string
}

const props = defineProps<{
  title: string
  subtitle?: string
  tabs: TabHubItem[]
  queryKey?: string
}>()

const route = useRoute()
const router = useRouter()
const queryKey = computed(() => props.queryKey ?? "tab")

provide("tabHubEmbedded", true)

const activeTab = computed(() => {
  const q = route.query[queryKey.value]
  if (typeof q === "string" && props.tabs.some((t) => t.key === q)) return q
  return props.tabs[0]?.key ?? ""
})

function selectTab(key: string) {
  router.replace({ query: { ...route.query, [queryKey.value]: key } })
}
</script>

<template>
  <div class="tab-hub">
    <PageHeader :title="title" :subtitle="subtitle" />
    <nav class="tab-hub__bar" aria-label="分区切换">
      <button
        v-for="item in tabs"
        :key="item.key"
        type="button"
        class="tab-hub__item"
        :class="{ 'tab-hub__item--active': activeTab === item.key }"
        @click="selectTab(item.key)"
      >
        {{ item.label }}
      </button>
    </nav>
    <div class="tab-hub__body">
      <slot :active-tab="activeTab" />
    </div>
  </div>
</template>

<style scoped>
.tab-hub__bar {
  display: flex;
  gap: 4px;
  padding: 4px;
  margin-bottom: 20px;
  background: #fff;
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: var(--radius-full, 999px);
  box-shadow: var(--shadow-sm);
  max-width: 100%;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}

.tab-hub__bar::-webkit-scrollbar {
  display: none;
}

.tab-hub__item {
  padding: 8px 16px;
  min-height: var(--touch-min, 44px);
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary, #6B7280);
  background: transparent;
  border: none;
  border-radius: var(--radius-full, 999px);
  cursor: pointer;
  transition: all var(--transition-fast, 0.15s);
  white-space: nowrap;
  flex-shrink: 0;
}

.tab-hub__item:hover {
  color: var(--color-primary, #1677FF);
  background: var(--color-primary-light, #E6F4FF);
}

.tab-hub__item--active {
  color: var(--color-primary, #1677FF);
  background: var(--color-primary-light, #E6F4FF);
  font-weight: 700;
}
</style>
