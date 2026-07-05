import type { RouteRecordRaw } from "vue-router"

const studentRoutes: RouteRecordRaw[] = [
  {
    path: "/student",
    component: () => import("@/layouts/AppLayout.vue"),
    meta: { roles: ["student"], icon: "LayoutDashboard" },
    redirect: "/student/dashboard",
    children: [
      {
        path: "dashboard",
        name: "StudentDashboard",
        component: () => import("@/pages/student/DashboardPage.vue"),
        meta: { title: "仪表盘", icon: "LayoutDashboard", sort: 1, keepAlive: true },
      },
      {
        path: "courses",
        name: "StudentCourseList",
        component: () => import("@/pages/student/CourseListPage.vue"),
        meta: { title: "我的课程", icon: "BookOpen", sort: 2, keepAlive: true },
      },
      {
        path: "courses/:courseId",
        name: "StudentCourseDetail",
        component: () => import("@/pages/student/CourseDetailPage.vue"),
        meta: { title: "课程详情", hidden: true },
      },
      {
        path: "tasks",
        name: "StudentTaskList",
        component: () => import("@/pages/student/TaskListPage.vue"),
        meta: { title: "实训任务", icon: "ClipboardList", sort: 3, keepAlive: true },
      },
      {
        path: "tasks/:taskId",
        name: "StudentTaskDetail",
        component: () => import("@/pages/student/TaskDetailPage.vue"),
        meta: { title: "任务详情", hidden: true },
      },
      {
        path: "submit/:taskId",
        name: "StudentSubmit",
        component: () => import("@/pages/student/SubmitPage.vue"),
        meta: { title: "成果提交", hidden: true },
      },
      {
        path: "grades/:submissionId",
        name: "StudentGrades",
        component: () => import("@/pages/student/GradePage.vue"),
        meta: { title: "我的成绩", hidden: true },
      },
      {
        path: "ai-result",
        name: "StudentAIResult",
        component: () => import("@/pages/student/AIResultPage.vue"),
        meta: { title: "AI 分析结果", hidden: true },
      },
      {
        path: "reports",
        name: "StudentReports",
        component: () => import("@/pages/student/StudentReportsPage.vue"),
        meta: { title: "个人报告", icon: "BarChart3", sort: 4, keepAlive: true },
      },
      {
        path: "growth",
        name: "StudentGrowth",
        component: () => import("@/pages/student/GrowthPage.vue"),
        meta: { title: "成长中心", icon: "TrendingUp", sort: 5, keepAlive: true },
      },
      {
        path: "profile",
        name: "StudentProfile",
        component: () => import("@/pages/student/ProfilePage.vue"),
        meta: { title: "个人中心", icon: "User", sort: 99 },
      },
    ],
  },
]

export default studentRoutes
