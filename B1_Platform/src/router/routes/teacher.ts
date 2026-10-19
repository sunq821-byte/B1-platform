import type { RouteRecordRaw } from "vue-router"

const teacherRoutes: RouteRecordRaw[] = [
  // ????? ? ???????? AppLayout ? content wrapper
  {
    path: "/teacher/submissions",
    component: () => import("@/layouts/ReviewLayout.vue"),
    meta: { roles: ["teacher"] },
    children: [
      {
        path: "",
        name: "TeacherSubmissions",
        component: () => import("@/pages/teacher/SubmissionsPage.vue"),
        meta: { title: "?????", icon: "FileText", sort: 4 },
      },
    ],
  },
  // ?????? ? ?? AppLayout
  {
    path: "/teacher",
    component: () => import("@/layouts/AppLayout.vue"),
    meta: { roles: ["teacher"], icon: "BookOpen" },
    redirect: "/teacher/dashboard",
    children: [
      {
        path: "dashboard",
        name: "TeacherDashboard",
        component: () => import("@/pages/teacher/DashboardPage.vue"),
        meta: { title: "???", icon: "LayoutDashboard", sort: 1 },
      },
      {
        path: "teaching",
        name: "TeacherTeaching",
        component: () => import("@/pages/teacher/TeacherTeachingHubPage.vue"),
        meta: { title: "?????", icon: "BookOpen", sort: 2 },
      },
      {
        path: "courses",
        redirect: (to) => ({ path: "/teacher/teaching", query: { ...to.query, tab: "courses" } }),
      },
      {
        path: "tasks",
        redirect: (to) => ({ path: "/teacher/teaching", query: { ...to.query, tab: "tasks" } }),
      },
      {
        path: "students",
        redirect: (to) => ({ path: "/teacher/teaching", query: { ...to.query, tab: "progress" } }),
      },
      {
        path: "reports",
        name: "TeacherReports",
        component: () => import("@/pages/teacher/TeacherReportsHubPage.vue"),
        meta: { title: "????", icon: "BarChart3", sort: 5 },
      },
      {
        path: "reports-college",
        redirect: (to) => ({ path: "/teacher/reports", query: { ...to.query, tab: "college" } }),
      },
      {
        path: "knowledge-base",
        name: "TeacherKnowledgeBase",
        component: () => import("@/pages/teacher/KnowledgeBasePage.vue"),
        meta: { title: "???", icon: "BookMarked", sort: 3 },
      },
      {
        path: "profile",
        name: "TeacherProfile",
        component: () => import("@/pages/student/ProfilePage.vue"),
        meta: { title: "????", hidden: true },
      },
    ],
  },
]

export default teacherRoutes
