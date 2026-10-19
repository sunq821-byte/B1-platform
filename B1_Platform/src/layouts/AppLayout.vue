<script setup lang="ts">
import { computed, onMounted } from "vue"
import { useRoute } from "vue-router"
import { useUserStore } from "@/stores/useUserStore"
import { usePermissionStore } from "@/stores/usePermissionStore"
import Navbar from "@/components/layout/Navbar.vue"

const route = useRoute()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

const hideNavbar = computed(() => route.matched.some((record) => record.meta.hideNavbar === true))

onMounted(() => {
  if (userStore.userRole) {
    permissionStore.generateMenus(userStore.userRole)
  }
})
</script>

<template>
  <div class="app-layout" :class="{ 'app-layout--no-nav': hideNavbar }">
    <Navbar
      v-if="!hideNavbar"
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
  padding: 8px var(--content-pad-x, 28px) 40px;
  box-sizing: border-box;
}

.app-layout--no-nav {
  min-height: 100vh;
}

.app-layout--no-nav .app-layout__main {
  max-width: none;
  margin: 0;
  padding: 0;
  min-height: 100vh;
}

@media (max-width: 767.98px) {
  .app-layout__main {
    padding-bottom: 32px;
  }
}
</style>
