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
  <div class="review-layout">
    <Navbar
      :menus="permissionStore.menus"
      :username="userStore.userName"
      :role="userStore.userRole"
    />
    <main class="review-layout__main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.review-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--color-bg, #f8fafc);
}

.review-layout__main {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
</style>
