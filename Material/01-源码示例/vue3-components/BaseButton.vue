<script setup lang="ts">
defineProps<{
  type?: "primary" | "default"
  disabled?: boolean
}>()
</script>

<template>
  <button class="b1-btn" :class="[`b1-btn--${type || 'default'}`]" :disabled="disabled">
    <slot />
  </button>
</template>

<style scoped>
.b1-btn { padding: 8px 16px; border-radius: 8px; border: 1px solid #d9d9d9; cursor: pointer; }
.b1-btn--primary { background: #1677ff; color: #fff; border-color: #1677ff; }
.b1-btn:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
