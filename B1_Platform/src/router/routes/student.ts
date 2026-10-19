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
        meta: { title: "工作台", icon: "LayoutDashboard", sort: 1 },
      },
      {
        path: "analytics",
        name: "StudentAnalytics",
        component: () => import("@/pages/student/StudentAnalyticsHubPage.vue"),
        meta: { title: "学情分析", icon: "BarChart3", sort: 3 },
      },
      {
        path: "courses",
        name: "StudentCourseList",
        component: () => import("@/pages/student/CourseListPage.vue"),
        meta: { title: "我的课程", hidden: true },
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
        meta: { title: "实训任务", icon: "ClipboardList", sort: 2 },
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
        meta: { title: "成果提交", hidden: true, hideNavbar: true },
      },
      {
        path: "grades/:submissionId",
        name: "StudentGrades",
        component: () => import("@/pages/student/AIResultPage.vue"),
        meta: { title: "我的成绩", hidden: true, hideNavbar: true },
      },
      {
        path: "ai-result",
        redirect: (to) => {
          const sid = to.query.sid
          if (typeof sid === "string" && sid) {
            return { path: `/student/grades/${sid}`, query: { ...to.query, sid: undefined } }
          }
          return "/student/tasks"
        },
      },
      {
        path: "reports",
        redirect: (to) => ({ path: "/student/analytics", query: { ...to.query, tab: "reports" } }),
      },
      {
        path: "growth",
        redirect: (to) => ({ path: "/student/analytics", query: { ...to.query, tab: "growth" } }),
      },
      {
        path: "profile",
        name: "StudentProfile",
        component: () => import("@/pages/student/ProfilePage.vue"),
        meta: { title: "个人中心", hidden: true },
      },
    ],
  },
]

export default studentRoutes
