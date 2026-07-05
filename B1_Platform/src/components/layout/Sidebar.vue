<script setup lang="ts">
import { useRoute, useRouter } from "vue-router"
import { ChevronLeft, ChevronRight } from "lucide-vue-next"
import * as LucideIcons from "lucide-vue-next"
import type { Component } from "vue"
import type { MenuItem } from "@/stores/usePermissionStore"

defineProps<{
  collapsed: boolean
  menus: MenuItem[]
}>()

const emit = defineEmits<{
  toggle: []
}>()

const router = useRouter()
const route = useRoute()

function handleMenuClick(path: string) {
  router.push(path)
}

function isActive(path: string): boolean {
  return route.path === path
}

function getIcon(name?: string): Component | undefined {
  if (!name) return undefined
  const icons = LucideIcons as unknown as Record<string, Component>
  return icons[name] ?? undefined
}
</script>

<template>
  <aside class="sidebar" :class="{ 'sidebar--collapsed': collapsed }">
    <div class="sidebar__brand">
      <div class="sidebar__logo">B1</div>
      <span v-if="!collapsed" class="sidebar__name">实训平台</span>
    </div>

    <nav class="sidebar__nav">
      <button
        v-for="item in menus"
        :key="item.path"
        :title="item.title"
        class="sidebar__item"
        :class="{ 'sidebar__item--active': isActive(item.path) }"
        @click="handleMenuClick(item.path)"
      >
        <span class="sidebar__item-icon">
          <component
            :is="getIcon(item.icon)"
            v-if="getIcon(item.icon)"
            :size="18"
          />
          <span v-else class="sidebar__item-dot" />
        </span>
        <span v-if="!collapsed" class="sidebar__item-label">{{ item.title }}</span>
      </button>
    </nav>

    <button
      :title="collapsed ? '展开' : '折叠'"
      class="sidebar__collapse-btn"
      @click="emit('toggle')"
    >
      <ChevronLeft v-if="!collapsed" :size="18" />
      <ChevronRight v-else :size="18" />
    </button>
  </aside>
</template>

<style scoped>
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: var(--sidebar-width, 240px);
  background: var(--color-card, #fff);
  border-right: 1px solid var(--color-border, #e2e8f0);
  display: flex;
  flex-direction: column;
  transition: width var(--transition-normal, 250ms ease);
  z-index: 100;
  overflow: hidden;
}

.sidebar--collapsed {
  width: var(--sidebar-collapsed-width, 64px);
}

.sidebar__brand {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm, 8px);
  height: var(--header-height, 56px);
  padding: 0 var(--spacing-md, 16px);
  border-bottom: 1px solid var(--color-border, #e2e8f0);
  flex-shrink: 0;
}

.sidebar__logo {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md, 8px);
  background: var(--color-primary, #3b82f6);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.sidebar__name {
  font-size: var(--font-size-base, 16px);
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
  white-space: nowrap;
}

.sidebar__nav {
  flex: 1;
  padding: var(--spacing-sm, 8px);
  overflow-y: auto;
}

.sidebar__item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm, 8px);
  width: 100%;
  padding: 10px var(--spacing-sm, 8px);
  border: none;
  border-radius: var(--radius-md, 8px);
  background: transparent;
  color: var(--color-text-secondary, #64748b);
  font-size: var(--font-size-sm, 14px);
  cursor: pointer;
  transition: all var(--transition-fast, 150ms ease);
  margin-bottom: 2px;
  text-align: left;
}

.sidebar__item:hover {
  background: var(--color-bg, #f8fafc);
  color: var(--color-text-primary, #1e293b);
}

.sidebar__item--active {
  background: var(--color-primary-light, #eff6ff);
  color: var(--color-primary, #3b82f6);
}

.sidebar__item-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.sidebar__item-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.sidebar__item-label {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar__collapse-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  border: none;
  border-top: 1px solid var(--color-border, #e2e8f0);
  background: transparent;
  color: var(--color-text-secondary, #64748b);
  cursor: pointer;
  transition: color var(--transition-fast, 150ms ease);
  flex-shrink: 0;
}

.sidebar__collapse-btn:hover {
  color: var(--color-primary, #3b82f6);
}
</style>
