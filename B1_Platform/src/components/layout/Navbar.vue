<script setup lang="ts">
import { useRouter, useRoute } from "vue-router"
import { useUserStore } from "@/stores/useUserStore"
import { LogOut, User, ChevronDown, Settings } from "lucide-vue-next"
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
    <a class="navbar__brand" @click="navigate('/')">
      <div class="navbar__logo">B1</div>
      <span class="navbar__title">B1 Platform</span>
    </a>

    <nav class="navbar__pill" aria-label="主导航">
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

    <div class="navbar__right">
      <button
        v-if="role === 'teacher' || role === 'admin'"
        type="button"
        class="icon-btn"
        title="设置"
        @click="navigate(role === 'admin' ? '/admin/system' : '/teacher/knowledge-base')"
      >
        <Settings :size="16" />
      </button>
      <NotificationBell />
      <el-dropdown trigger="click">
        <button type="button" class="navbar__user" :aria-label="username || '用户'">
          <span class="navbar__avatar">{{ getAvatarChar(username || "用户") }}</span>
          <span class="navbar__username">{{ username || "用户" }}</span>
          <ChevronDown :size="14" class="navbar__chevron" />
        </button>
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
  display: grid;
  grid-template-columns: minmax(140px, 1fr) auto minmax(140px, 1fr);
  align-items: center;
  gap: 16px;
  min-height: var(--header-height, 64px);
  padding: 12px 28px;
  background: transparent;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar__brand {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-self: start;
  cursor: pointer;
  text-decoration: none;
}

.navbar__logo {
  position: relative;
  width: 30px;
  height: 30px;
  background: var(--color-primary, #1677FF);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 800;
  font-size: 11px;
  flex-shrink: 0;
  box-shadow: 0 6px 14px rgba(22, 119, 255, 0.28);
}

.navbar__logo::after {
  content: "";
  position: absolute;
  top: -2px;
  right: -2px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-brand-accent, #E03C31);
  border: 2px solid #fff;
}

.navbar__title {
  font-size: 17px;
  font-weight: 800;
  color: var(--color-navy, #0B3A5C);
  letter-spacing: -0.02em;
  white-space: nowrap;
}

.navbar__pill {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 2px;
  padding: 4px;
  background: #fff;
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-card);
  max-width: min(920px, 100%);
}

.navbar__item {
  padding: 9px 13px;
  font-size: 13px;
  color: var(--color-text-secondary, #6B7280);
  cursor: pointer;
  border-radius: var(--radius-full);
  transition: all var(--transition-fast);
  text-decoration: none;
  white-space: nowrap;
  font-weight: 500;
}

.navbar__item:hover {
  background: var(--color-primary-light, #E6F4FF);
  color: var(--color-primary, #1677FF);
}

.navbar__item--active {
  color: var(--color-primary, #1677FF);
  background: var(--color-primary-light, #E6F4FF);
  font-weight: 700;
}

.navbar__right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  justify-self: end;
}

.icon-btn,
.navbar__user {
  border: 1px solid var(--color-border, #E5E7EB);
  background: #fff;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.icon-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: var(--color-text-secondary, #6B7280);
}

.icon-btn:hover {
  color: var(--color-primary, #1677FF);
  border-color: var(--color-primary-mid, #91CAFF);
  background: var(--color-primary-light, #E6F4FF);
}

.navbar__user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 3px 10px 3px 3px;
  border-radius: var(--radius-full);
  font: inherit;
  color: inherit;
}

.navbar__user:hover {
  box-shadow: var(--shadow-md);
}

.navbar__avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, #91CAFF, #E6F7EF);
  color: var(--color-navy, #0B3A5C);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.navbar__username {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-primary);
  white-space: nowrap;
}

.navbar__chevron {
  color: var(--color-text-placeholder);
}

@media (max-width: 1100px) {
  .navbar {
    grid-template-columns: 1fr auto;
    grid-template-areas:
      "brand right"
      "pill pill";
    padding: 12px 16px;
  }
  .navbar__brand { grid-area: brand; }
  .navbar__right { grid-area: right; }
  .navbar__pill {
    grid-area: pill;
    max-width: none;
    justify-content: flex-start;
  }
  .navbar__username { display: none; }
}
</style>
