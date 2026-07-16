<script setup lang="ts">
import { useRouter, useRoute } from "vue-router"
import { useUserStore } from "@/stores/useUserStore"
import { LogOut, User, ChevronDown } from "lucide-vue-next"
import type { MenuItem } from "@/stores/usePermissionStore"
import NotificationBell from "@/components/layout/NotificationBell.vue"

defineProps<{
  menus: MenuItem[]
  username: string
  role: string | null
}>()

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

function isActive(path: string): boolean {
  return route.path === path || route.path.startsWith(path + "/")
}

function isParentActive(item: MenuItem): boolean {
  if (isActive(item.path)) return true
  if (item.children) {
    return item.children.some((c) => isActive(c.path))
  }
  return false
}

function navigate(path: string) {
  router.push(path)
}

async function handleLogout() {
  await userStore.logout()
  router.push("/login")
}

function getAvatarChar(name: string): string {
  return name?.charAt(0)?.toUpperCase() || "U"
}
</script>

<template>
  <header class="navbar">
    <div class="navbar__left">
      <a class="navbar__brand" @click="navigate('/')">
        <div class="navbar__logo">B1</div>
        <span class="navbar__title">B1 Platform</span>
      </a>

      <nav class="navbar__links">
        <a
          v-for="item in menus"
          :key="item.path"
          class="navbar__item"
          :class="{ 'navbar__item--active': isParentActive(item) }"
          @click="navigate(item.path)"
        >
          {{ item.title }}
        </a>
      </nav>
    </div>

    <div class="navbar__right">
      <NotificationBell />

      <el-dropdown trigger="click">
        <div class="navbar__user">
          <div class="navbar__avatar">
            {{ getAvatarChar(username || "用户") }}
          </div>
          <span class="navbar__username">{{ username || "用户" }}</span>
          <ChevronDown :size="14" class="navbar__chevron" />
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="navigate('/student/profile')">
              <User :size="16" />
              <span style="margin-left: 8px">个人中心</span>
            </el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">
              <LogOut :size="16" />
              <span style="margin-left: 8px">退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: var(--header-height, 52px);
  padding: 0 20px;
  background: var(--color-card, #fff);
  border-bottom: 1px solid var(--color-border, #e2e8f0);
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar__left {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  min-width: 0;
}

.navbar__brand {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 20px;
  flex-shrink: 0;
  cursor: pointer;
  text-decoration: none;
}

.navbar__logo {
  width: 28px;
  height: 28px;
  background: var(--color-primary, #3b82f6);
  border-radius: 7px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  font-size: 13px;
  flex-shrink: 0;
}

.navbar__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
  letter-spacing: -0.01em;
  white-space: nowrap;
}

.navbar__links {
  display: flex;
  align-items: center;
  gap: 2px;
}

.navbar__item {
  position: relative;
  padding: 8px 12px;
  font-size: 13.5px;
  color: var(--color-text-secondary, #64748b);
  cursor: pointer;
  border-radius: var(--radius-sm, 6px);
  transition: all var(--transition-fast, 150ms ease);
  text-decoration: none;
  white-space: nowrap;
  font-weight: 450;
}

.navbar__item:hover {
  background: var(--color-bg, #f8fafc);
  color: var(--color-text-primary, #1e293b);
}

.navbar__item--active {
  color: var(--color-primary, #3b82f6);
  background: transparent;
}

.navbar__item--active::after {
  content: "";
  position: absolute;
  bottom: -1px;
  left: 12px;
  right: 12px;
  height: 2px;
  background: var(--color-primary, #3b82f6);
  border-radius: 1px 1px 0 0;
}

.navbar__right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  margin-left: auto;
}

.navbar__user {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-md, 8px);
  transition: background var(--transition-fast, 150ms ease);
}

.navbar__user:hover {
  background: var(--color-bg, #f8fafc);
}

.navbar__avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--color-primary-light, #eff6ff);
  color: var(--color-primary, #3b82f6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.navbar__username {
  font-size: var(--font-size-sm, 14px);
  color: var(--color-text-primary, #1e293b);
  white-space: nowrap;
}

.navbar__chevron {
  color: var(--color-text-placeholder, #94a3b8);
}
</style>
