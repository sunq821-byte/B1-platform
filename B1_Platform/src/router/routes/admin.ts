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
        meta: { title: "系统总览", icon: "LayoutDashboard", sort: 1 },
      },
      {
        path: "organization",
        name: "AdminOrganization",
        component: () => import("@/pages/admin/AdminOrganizationHubPage.vue"),
        meta: { title: "组织管理", icon: "Users", sort: 2 },
      },
      {
        path: "users",
        redirect: (to) => ({ path: "/admin/organization", query: { ...to.query, tab: "users" } }),
      },
      {
        path: "classes",
        redirect: (to) => ({ path: "/admin/organization", query: { ...to.query, tab: "classes" } }),
      },
      {
        path: "operations",
        name: "AdminOperations",
        component: () => import("@/pages/admin/AdminOperationsHubPage.vue"),
        meta: { title: "运维中心", icon: "Activity", sort: 3 },
      },
      {
        path: "monitor",
        redirect: (to) => ({ path: "/admin/operations", query: { ...to.query, tab: "monitor" } }),
      },
      {
        path: "logs",
        redirect: (to) => ({ path: "/admin/operations", query: { ...to.query, tab: "logs" } }),
      },
      {
        path: "system",
        name: "AdminSystem",
        component: () => import("@/pages/admin/SystemPage.vue"),
        meta: { title: "系统配置", icon: "Settings", sort: 4 },
      },
      {
        path: "profile",
        name: "AdminProfile",
        component: () => import("@/pages/student/ProfilePage.vue"),
        meta: { title: "个人中心", hidden: true },
      },
    ],
  },
]

export default adminRoutes
