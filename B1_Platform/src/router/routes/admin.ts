import type { RouteRecordRaw } from "vue-router"

const adminRoutes: RouteRecordRaw[] = [
  {
    path: "/admin",
    component: () => import("@/layouts/AppLayout.vue"),
    meta: { roles: ["admin"], icon: "Settings" },
    redirect: "/admin/dashboard",
    children: [
      {
        path: "dashboard",
        name: "AdminDashboard",
        component: () => import("@/pages/admin/DashboardPage.vue"),
        meta: { title: "仪表盘", icon: "LayoutDashboard", sort: 1, keepAlive: true },
      },
      {
        path: "users",
        name: "AdminUsers",
        component: () => import("@/pages/admin/UsersPage.vue"),
        meta: { title: "用户管理", icon: "Users", sort: 2, keepAlive: true },
      },
      {
        path: "system",
        name: "AdminSystem",
        component: () => import("@/pages/admin/SystemPage.vue"),
        meta: { title: "系统配置", icon: "Settings", sort: 3, keepAlive: true },
      },
      {
        path: "classes",
        name: "AdminClasses",
        component: () => import("@/pages/admin/ClassesPage.vue"),
        meta: { title: "班级管理", icon: "School", sort: 4, keepAlive: true },
      },
      {
        path: "logs",
        name: "AdminLogs",
        component: () => import("@/pages/admin/LogsPage.vue"),
        meta: { title: "操作日志", icon: "ScrollText", sort: 5, keepAlive: true },
      },
      {
        path: "monitor",
        name: "AdminMonitor",
        component: () => import("@/pages/admin/MonitorPage.vue"),
        meta: { title: "系统监控", icon: "Activity", sort: 6, keepAlive: true },
      },
    ],
  },
]

export default adminRoutes
