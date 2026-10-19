<script setup lang="ts">
import { ref, watch } from "vue"
import { useRouter, useRoute } from "vue-router"
import { LogOut, User, ChevronDown, Settings, Menu } from "lucide-vue-next"
import type { MenuItem } from "@/stores/usePermissionStore"
import { useUserStore } from "@/stores/useUserStore"
import { useBreakpoint } from "@/composables/useBreakpoint"
import NotificationBell from "@/components/layout/NotificationBell.vue"

const props = defineProps<{
  menus: MenuItem[]
  username: string
  role: string | null
}>()

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { isMdDown } = useBreakpoint()
const drawerOpen = ref(false)

watch(
  () => route.path,
  () => {
    drawerOpen.value = false
  },
)

function profilePath(): string {
  const role = userStore.userRole
  if (role === "teacher") return "/teacher/profile"
  if (role === "admin") return "/admin/profile"
  return "/student/profile"
}

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
  drawerOpen.value = false
  router.push(path)
}

async function handleLogout() {
  drawerOpen.value = false
  await userStore.logout()
  router.push("/login")
}

function getAvatarChar(name: string): string {
  return name?.charAt(0)?.toUpperCase() || "U"
}
</script>

<template>
  <header class="navbar" :class="{ 'navbar--compact': isMdDown }">
    <div class="navbar__leading">
      <button
        v-if="isMdDown"
        type="button"
        class="icon-btn navbar__menu-btn"
        aria-label="打开导航菜单"
        @click="drawerOpen = true"
      >
        <Menu :size="18" />
      </button>
      <a class="navbar__brand" @click="navigate('/')">
        <img class="navbar__logo" src="/logo.svg" alt="衡迹" />
      </a>
    </div>

    <nav v-if="!isMdDown" class="navbar__pill" aria-label="主导航">
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
        v-if="role === 'admin'"
        type="button"
        class="icon-btn"
        title="系统配置"
        @click="navigate('/admin/system')"
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
            <el-dropdown-item @click="navigate(profilePath())">
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

  <el-drawer
    v-model="drawerOpen"
    direction="ltr"
    size="280px"
    :with-header="true"
    title="导航"
    class="navbar-drawer"
  >
    <nav class="navbar-drawer__nav" aria-label="移动端主导航">
      <button
        v-for="item in props.menus"
        :key="item.path"
        type="button"
        class="navbar-drawer__item"
        :class="{ 'navbar-drawer__item--active': isParentActive(item) }"
        @click="navigate(item.path)"
      >
        {{ item.title }}
      </button>
    </nav>
  </el-drawer>
</template>

<style scoped>
.navbar {
  display: grid;
  grid-template-columns: minmax(140px, 1fr) auto minmax(140px, 1fr);
  align-items: center;
  gap: 16px;
  min-height: var(--header-height, 64px);
  padding: 12px var(--content-pad-x, 28px);
  background: transparent;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar--compact {
  grid-template-columns: 1fr auto;
  padding-top: 8px;
  padding-bottom: 8px;
}

.navbar__leading {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-self: start;
  min-width: 0;
}

.navbar__menu-btn {
  flex-shrink: 0;
  width: var(--touch-min, 44px);
  height: var(--touch-min, 44px);
}

.navbar__brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  text-decoration: none;
}

.navbar__logo {
  width: 38px;
  height: 38px;
  flex-shrink: 0;
  object-fit: contain;
  border-radius: 8px;
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
  min-width: var(--touch-min, 44px);
  min-height: var(--touch-min, 44px);
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: var(--color-text-secondary, #6B7280);
  box-sizing: border-box;
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
  min-height: var(--touch-min, 44px);
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

.navbar-drawer__nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.navbar-drawer__item {
  display: block;
  width: 100%;
  text-align: left;
  padding: 12px 14px;
  min-height: var(--touch-min, 44px);
  border: none;
  border-radius: 10px;
  background: transparent;
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-primary, #1F2937);
  cursor: pointer;
}

.navbar-drawer__item--active {
  background: var(--color-primary-light, #E6F4FF);
  color: var(--color-primary, #1677FF);
  font-weight: 700;
}

@media (max-width: 1279.98px) {
  .navbar__username {
    display: none;
  }
}
</style>
