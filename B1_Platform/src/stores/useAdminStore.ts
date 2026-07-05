import { defineStore } from "pinia"
import { ref } from "vue"
import type {
  IAdminUserItem,
  IAdminUserFormData,
  ISystemConfig,
  IAdminDashboardData,
  IClassItem,
  IClassFormData,
  ILogItem,
  IMonitorData,
} from "@/types/admin"
import * as adminApi from "@/api/modules/admin"

export const useAdminStore = defineStore("admin", () => {
  // Users
  const users = ref<IAdminUserItem[]>([])
  const usersLoading = ref(false)

  // System Config
  const systemConfig = ref<ISystemConfig | null>(null)
  const configLoading = ref(false)

  // Dashboard
  const dashboardData = ref<IAdminDashboardData | null>(null)
  const dashboardLoading = ref(false)

  // Classes
  const classes = ref<IClassItem[]>([])
  const classesLoading = ref(false)

  // Logs
  const logs = ref<ILogItem[]>([])
  const logsLoading = ref(false)

  // Monitor
  const monitorData = ref<IMonitorData | null>(null)
  const monitorLoading = ref(false)

  // === Users ===
  async function fetchUsers(params?: { role?: string; keyword?: string }): Promise<void> {
    usersLoading.value = true
    try { users.value = await adminApi.fetchUsers(params) }
    finally { usersLoading.value = false }
  }
  async function createUser(data: IAdminUserFormData): Promise<IAdminUserItem> {
    const u = await adminApi.createUser(data)
    users.value.push(u)
    return u
  }
  async function updateUser(userId: string, data: IAdminUserFormData): Promise<void> {
    await adminApi.updateUser(userId, data)
    const idx = users.value.findIndex((u) => u.userId === userId)
    if (idx >= 0) users.value[idx] = { ...users.value[idx], ...data }
  }
  async function toggleUserStatus(userId: string): Promise<void> {
    await adminApi.toggleUserStatus(userId)
    const u = users.value.find((x) => x.userId === userId)
    if (u) u.status = u.status === "active" ? "inactive" : "active"
  }

  // === System Config ===
  async function fetchSystemConfig(): Promise<void> {
    configLoading.value = true
    try { systemConfig.value = await adminApi.fetchSystemConfig() }
    finally { configLoading.value = false }
  }
  async function saveSystemConfig(data: ISystemConfig): Promise<void> {
    await adminApi.saveSystemConfig(data)
    systemConfig.value = { ...data }
  }

  // === Dashboard ===
  async function fetchDashboard(): Promise<void> {
    dashboardLoading.value = true
    try { dashboardData.value = await adminApi.fetchDashboard() }
    finally { dashboardLoading.value = false }
  }

  // === Classes ===
  async function fetchClasses(): Promise<void> {
    classesLoading.value = true
    try { classes.value = await adminApi.fetchClasses() }
    finally { classesLoading.value = false }
  }
  async function createClass(data: IClassFormData): Promise<IClassItem> {
    const c = await adminApi.createClass(data)
    classes.value.push(c)
    return c
  }
  async function updateClass(classId: string, data: IClassFormData): Promise<void> {
    await adminApi.updateClass(classId, data)
    const idx = classes.value.findIndex((c) => c.id === classId)
    if (idx >= 0) classes.value[idx] = { ...classes.value[idx], ...data }
  }
  async function deleteClass(classId: string): Promise<void> {
    await adminApi.deleteClass(classId)
    classes.value = classes.value.filter((c) => c.id !== classId)
  }

  // === Logs ===
  async function fetchLogs(params?: { type?: string }): Promise<void> {
    logsLoading.value = true
    try { logs.value = await adminApi.fetchLogs(params) }
    finally { logsLoading.value = false }
  }

  // === Monitor ===
  async function fetchMonitorData(): Promise<void> {
    monitorLoading.value = true
    try { monitorData.value = await adminApi.fetchMonitorData() }
    finally { monitorLoading.value = false }
  }

  function resetStore(): void {
    users.value = []
    systemConfig.value = null
    dashboardData.value = null
    classes.value = []
    logs.value = []
    monitorData.value = null
  }

  return {
    users, usersLoading,
    systemConfig, configLoading,
    dashboardData, dashboardLoading,
    classes, classesLoading,
    logs, logsLoading,
    monitorData, monitorLoading,
    fetchUsers, createUser, updateUser, toggleUserStatus,
    fetchSystemConfig, saveSystemConfig,
    fetchDashboard,
    fetchClasses, createClass, updateClass, deleteClass,
    fetchLogs,
    fetchMonitorData,
    resetStore,
  }
})
