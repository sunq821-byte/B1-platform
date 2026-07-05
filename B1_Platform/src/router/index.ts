import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router"
import authRoutes from "./routes/auth"
import studentRoutes from "./routes/student"
import teacherRoutes from "./routes/teacher"
import adminRoutes from "./routes/admin"
import { setupAuthGuard, setupPermissionGuard, setupRedirectGuard } from "./guards"

const routes: RouteRecordRaw[] = [
  ...authRoutes,
  ...studentRoutes,
  ...teacherRoutes,
  ...adminRoutes,
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

setupAuthGuard(router)
setupPermissionGuard(router)
setupRedirectGuard(router)

export default router
