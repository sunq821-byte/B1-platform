import type { RouteRecordRaw } from "vue-router"

const authRoutes: RouteRecordRaw[] = [
  {
    path: "/",
    redirect: "/login",
  },
  {
    path: "/login",
    name: "Login",
    component: () => import("@/pages/auth/LoginPage.vue"),
    meta: { title: "登录", public: true, hidden: true },
  },
  {
    path: "/403",
    name: "NoPermission",
    component: () => import("@/pages/common/NoPermission.vue"),
    meta: { title: "无权限", public: true, hidden: true },
  },
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: () => import("@/pages/common/NotFound.vue"),
    meta: { title: "页面不存在", public: true, hidden: true },
  },
]

export default authRoutes
