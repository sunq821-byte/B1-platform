import type { RouteRecordRaw } from "vue-router"

const teacherRoutes: RouteRecordRaw[] = [
  // 审核工作台 — 全宽布局，不使用 AppLayout 的 content wrapper
  {
    path: "/teacher/submissions",
    component: () => import("@/layouts/ReviewLayout.vue"),
    meta: { roles: ["teacher"] },
    children: [
      {
        path: "",
        name: "TeacherSubmissions",
        component: () => import("@/pages/teacher/SubmissionsPage.vue"),
        meta: { title: "审核工作台", icon: "FileText", sort: 6 },
      },
    ],
  },
  // 其他教师页面 — 标准 AppLayout
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
        meta: { title: "仪表盘", icon: "LayoutDashboard", sort: 1, keepAlive: true },
      },
      {
        path: "courses",
        name: "TeacherCourseList",
        component: () => import("@/pages/teacher/CourseListPage.vue"),
        meta: { title: "课程管理", icon: "BookOpen", sort: 2 },
      },
      {
        path: "standards",
        name: "TeacherStandards",
        component: () => import("@/pages/teacher/StandardsPage.vue"),
        meta: { title: "评价标准", icon: "ClipboardCheck", sort: 3 },
      },
      {
        path: "tasks",
        name: "TeacherTaskList",
        component: () => import("@/pages/teacher/TrainingPage.vue"),
        meta: { title: "实训任务管理", icon: "ClipboardList", sort: 4 },
      },
      {
        path: "students",
        name: "TeacherStudents",
        component: () => import("@/pages/teacher/StudentsPage.vue"),
        meta: { title: "学生管理", icon: "Users", sort: 5 },
      },
      {
        path: "standards-library",
        name: "TeacherStandardsLibrary",
        component: () => import("@/pages/teacher/StandardsLibraryPage.vue"),
        meta: { title: "标准库", icon: "Library", sort: 6 },
      },
      {
        path: "reports",
        name: "TeacherReports",
        component: () => import("@/pages/teacher/ReportsPage.vue"),
        meta: { title: "班级报告", icon: "BarChart3", sort: 7 },
      },
      {
        path: "reports-college",
        name: "TeacherReportsCollege",
        component: () => import("@/pages/teacher/ReportsCollegePage.vue"),
        meta: { title: "学院报告", icon: "TrendingUp", sort: 8 },
      },
    ],
  },
]

export default teacherRoutes
