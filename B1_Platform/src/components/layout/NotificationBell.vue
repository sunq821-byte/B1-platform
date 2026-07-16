<script setup lang="ts">
import { ref, computed, onMounted } from "vue"
import { useRouter } from "vue-router"
import { Bell } from "lucide-vue-next"
import { ElMessage } from "element-plus"
import { useUserStore } from "@/stores/useUserStore"
import {
  fetchNotifications,
  markNotificationRead,
  markAllNotificationsRead,
} from "@/api/modules/student"
import type { INotificationItem } from "@/types/student"

const userStore = useUserStore()
const router = useRouter()
const popoverRef = ref<{ hide: () => void }>()

const isStudent = computed(() => userStore.userRole === "student")
const items = ref<INotificationItem[]>([])
const loading = ref(false)

const unreadCount = computed(() => items.value.filter((n) => n.isRead !== 1).length)
const badgeText = computed(() => (unreadCount.value > 99 ? "99+" : String(unreadCount.value)))

async function load(): Promise<void> {
  if (!isStudent.value) return
  loading.value = true
  try {
    const res = await fetchNotifications({ page: 1, pageSize: 20 })
    items.value = res.list ?? []
  } catch {
    /* silent — bell simply shows no data */
  } finally {
    loading.value = false
  }
}

async function onItemClick(item: INotificationItem): Promise<void> {
  if (item.isRead !== 1) {
    try {
      await markNotificationRead(item.notificationId)
      item.isRead = 1
    } catch { /* keep optimistic UI unchanged on failure */ }
  }
  if (item.relatedTaskId) {
    popoverRef.value?.hide()
    router.push(`/student/tasks/${item.relatedTaskId}`)
  }
}

async function onMarkAll(): Promise<void> {
  if (unreadCount.value === 0) return
  try {
    await markAllNotificationsRead()
    items.value = items.value.map((n) => ({ ...n, isRead: 1 }))
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || "操作失败")
  }
}

function formatTime(dt: string): string {
  if (!dt) return ""
  return dt.replace("T", " ").substring(0, 16)
}

onMounted(load)
</script>

<template>
  <el-popover
    ref="popoverRef"
    placement="bottom-end"
    :width="340"
    trigger="click"
    :show-arrow="false"
    popper-class="notif-popover"
    @show="load"
  >
    <template #reference>
      <button class="notif-bell" title="消息通知">
        <Bell :size="18" />
        <span v-if="unreadCount > 0" class="notif-badge">{{ badgeText }}</span>
      </button>
    </template>

    <div class="notif-panel">
      <div class="notif-panel__header">
        <span class="notif-panel__title">消息通知</span>
        <button class="notif-panel__mark" :disabled="unreadCount === 0" @click="onMarkAll">
          全部已读
        </button>
      </div>
      <div class="notif-panel__list">
        <div
          v-for="item in items"
          :key="item.notificationId"
          class="notif-item"
          :class="{ 'notif-item--unread': item.isRead !== 1 }"
          @click="onItemClick(item)"
        >
          <span class="notif-item__dot" />
          <div class="notif-item__body">
            <div class="notif-item__title">{{ item.title }}</div>
            <div class="notif-item__content">{{ item.content }}</div>
            <div class="notif-item__time">{{ formatTime(item.sentAt) }}</div>
          </div>
        </div>
        <div v-if="items.length === 0" class="notif-empty">暂无通知</div>
      </div>
    </div>
  </el-popover>
</template>

<style scoped>
.notif-bell {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: none;
  border-radius: var(--radius-sm, 6px);
  background: transparent;
  color: var(--color-text-secondary, #64748b);
  cursor: pointer;
  transition: background var(--transition-fast, 150ms ease);
}
.notif-bell:hover {
  background: var(--color-bg, #f8fafc);
}
.notif-badge {
  position: absolute;
  top: 1px;
  right: 1px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  box-sizing: border-box;
  border-radius: 100px;
  background: var(--color-danger, #ef4444);
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  line-height: 16px;
  text-align: center;
}
</style>

<!-- Panel content is teleported to body by el-popover, so it must be styled globally, namespaced under the popper-class. -->
<style>
.notif-popover.el-popover.el-popper {
  padding: 0;
  border-radius: 10px;
}
.notif-popover .notif-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-bottom: 1px solid var(--color-border, #e2e8f0);
}
.notif-popover .notif-panel__title {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
}
.notif-popover .notif-panel__mark {
  border: none;
  background: transparent;
  color: var(--color-primary, #3b82f6);
  font-size: 12px;
  cursor: pointer;
  padding: 0;
}
.notif-popover .notif-panel__mark:disabled {
  color: var(--color-text-placeholder, #94a3b8);
  cursor: not-allowed;
}
.notif-popover .notif-panel__list {
  max-height: 360px;
  overflow-y: auto;
}
.notif-popover .notif-item {
  display: flex;
  gap: 8px;
  padding: 10px 14px;
  cursor: pointer;
  border-bottom: 1px solid var(--color-border-light, #f1f5f9);
  transition: background var(--transition-fast, 150ms ease);
}
.notif-popover .notif-item:hover {
  background: var(--color-bg, #f8fafc);
}
.notif-popover .notif-item__dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
  background: transparent;
}
.notif-popover .notif-item--unread .notif-item__dot {
  background: var(--color-danger, #ef4444);
}
.notif-popover .notif-item__body {
  flex: 1;
  min-width: 0;
}
.notif-popover .notif-item__title {
  font-size: 13px;
  color: var(--color-text-primary, #1e293b);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.notif-popover .notif-item--unread .notif-item__title {
  font-weight: 600;
}
.notif-popover .notif-item__content {
  font-size: 12px;
  color: var(--color-text-secondary, #64748b);
  margin-top: 2px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.notif-popover .notif-item__time {
  font-size: 11px;
  color: var(--color-text-placeholder, #94a3b8);
  margin-top: 4px;
  font-family: var(--font-mono, monospace);
}
.notif-popover .notif-empty {
  text-align: center;
  padding: 32px 0;
  font-size: 13px;
  color: var(--color-text-placeholder, #94a3b8);
}
</style>
