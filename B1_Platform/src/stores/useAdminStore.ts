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
    try {
      const raw = await adminApi.fetchDashboard() as IAdminDashboardData & Record<string, unknown>
      const s = (raw.stats ?? {}) as Partial<IAdminDashboardData["stats"]>
      const runtime = (raw.runtimeSummary ?? {}) as Partial<IAdminDashboardData["runtimeSummary"]>
      const trend = (raw.activityTrend ?? {}) as Partial<IAdminDashboardData["activityTrend"]>
      const alert = raw.priorityAlert as IAdminDashboardData["priorityAlert"] | undefined

      const storageUsed = Number(s.storageUsedGb ?? 0)
      const storageQuota = Number(s.storageQuotaGb ?? 100) || 100
      const healthyServices = Number(s.healthyServices ?? 0)
      const totalServices = Number(s.totalServices ?? (Array.isArray(raw.health) ? raw.health.length : 0))

      dashboardData.value = {
        stats: {
          totalUsers: Number(s.totalUsers ?? 0),
          studentCount: Number(s.studentCount ?? 0),
          teacherCount: Number(s.teacherCount ?? 0),
          adminCount: Number(s.adminCount ?? 0),
          todayActive: Number(s.todayActive ?? 0),
          activeRate: Number(s.activeRate ?? 0),
          activeCourses: Number(s.activeCourses ?? 0),
          totalSubmissions: Number(s.totalSubmissions ?? 0),
          completionRate: Number(s.completionRate ?? 0),
          aiCallsToday: Number(s.aiCallsToday ?? 0),
          storageUsedGb: storageUsed,
          storageQuotaGb: storageQuota,
          alertCount24h: Number(s.alertCount24h ?? 0),
          accountEnableRate: Number(s.accountEnableRate ?? 0),
          healthyServices,
          totalServices,
          aiQuotaRate: Number(s.aiQuotaRate ?? 0),
          alertDeltaPct: Number(s.alertDeltaPct ?? 0),
          aiCallsDeltaPct: Number(s.aiCallsDeltaPct ?? 0),
          completionDeltaPct: Number(s.completionDeltaPct ?? 0),
        },
        runtimeSummary: {
          healthy: Number(runtime.healthy ?? healthyServices),
          degraded: Number(runtime.degraded ?? 0),
          down: Number(runtime.down ?? 0),
          aiQueue: Number(runtime.aiQueue ?? 0),
        },
        activityTrend: {
          xAxis: Array.isArray(trend.xAxis) ? trend.xAxis : [],
          activeUsers: Array.isArray(trend.activeUsers) ? trend.activeUsers.map(Number) : [],
          aiCalls: Array.isArray(trend.aiCalls) ? trend.aiCalls.map(Number) : [],
        },
        priorityAlert: alert && typeof alert === "object"
          ? {
              title: String(alert.title ?? "优先处理"),
              summary: String(alert.summary ?? ""),
              priority: String(alert.priority ?? "中"),
              source: String(alert.source ?? ""),
              actionPath: String(alert.actionPath ?? "/admin/monitor"),
            }
          : null,
        recentLogs: Array.isArray(raw.recentLogs) ? raw.recentLogs : [],
        health: Array.isArray(raw.health) ? raw.health : [],
      }
    } finally {
      dashboardLoading.value = false
    }
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
