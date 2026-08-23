<script setup lang="ts">
import { onMounted } from "vue"
import { useUserStore } from "@/stores/useUserStore"
import { usePermissionStore } from "@/stores/usePermissionStore"
import Navbar from "@/components/layout/Navbar.vue"

const userStore = useUserStore()
const permissionStore = usePermissionStore()

onMounted(() => {
  if (userStore.userRole) {
    permissionStore.generateMenus(userStore.userRole)
  }
})
</script>

<template>
  <div class="app-layout">
    <Navbar
      :menus="permissionStore.menus"
      :username="userStore.userName"
      :role="userStore.userRole"
    />
    <main class="app-layout__main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.app-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--color-bg, #F5F7FA);
}

.app-layout__main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  max-width: var(--content-max, 1280px);
  width: 100%;
  margin: 0 auto;
  padding: 8px 28px 40px;
  box-sizing: border-box;
}

@media (max-width: 768px) {
  .app-layout__main {
    padding: 8px 16px 32px;
  }
}
</style>
