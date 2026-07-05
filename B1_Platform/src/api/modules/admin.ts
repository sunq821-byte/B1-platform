import request from "@/api/request"
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

// === Users ===
export function fetchUsers(params?: { role?: string; keyword?: string }): Promise<IAdminUserItem[]> {
  return request.get("/api/v1/admin/users", { params }) as Promise<IAdminUserItem[]>
}
export function createUser(data: IAdminUserFormData): Promise<IAdminUserItem> {
  return request.post("/api/v1/admin/users", data) as Promise<IAdminUserItem>
}
export function updateUser(userId: string, data: IAdminUserFormData): Promise<void> {
  return request.put(`/api/v1/admin/users/${userId}`, data) as Promise<void>
}
export function toggleUserStatus(userId: string): Promise<IAdminUserItem> {
  return request.patch(`/api/v1/admin/users/${userId}/toggle-status`) as Promise<IAdminUserItem>
}

// === System Config ===
export function fetchSystemConfig(): Promise<ISystemConfig> {
  return request.get("/api/v1/admin/system-config") as Promise<ISystemConfig>
}
export function saveSystemConfig(data: ISystemConfig): Promise<void> {
  return request.put("/api/v1/admin/system-config", data) as Promise<void>
}

// === Dashboard ===
export function fetchDashboard(): Promise<IAdminDashboardData> {
  return request.get("/api/v1/admin/dashboard") as Promise<IAdminDashboardData>
}

// === Classes ===
export function fetchClasses(): Promise<IClassItem[]> {
  return request.get("/api/v1/admin/classes") as Promise<IClassItem[]>
}
export function createClass(data: IClassFormData): Promise<IClassItem> {
  return request.post("/api/v1/admin/classes", data) as Promise<IClassItem>
}
export function updateClass(classId: string, data: IClassFormData): Promise<void> {
  return request.put(`/api/v1/admin/classes/${classId}`, data) as Promise<void>
}
export function deleteClass(classId: string): Promise<void> {
  return request.delete(`/api/v1/admin/classes/${classId}`) as Promise<void>
}

// === Logs ===
export function fetchLogs(params?: { type?: string }): Promise<ILogItem[]> {
  return request.get("/api/v1/admin/logs", { params }) as Promise<ILogItem[]>
}

// === Monitor ===
export function fetchMonitorData(): Promise<IMonitorData> {
  return request.get("/api/v1/admin/monitor") as Promise<IMonitorData>
}
