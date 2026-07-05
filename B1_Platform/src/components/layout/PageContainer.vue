<script setup lang="ts">
</script>

<template>
  <main class="page-container">
    <slot />
  </main>
</template>

<style scoped>
.page-container {
  flex: 1;
  padding: var(--spacing-lg, 24px);
  overflow-y: auto;
  min-height: 0;
}
</style>
