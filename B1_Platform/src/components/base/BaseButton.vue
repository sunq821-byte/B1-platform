<script setup lang="ts">
defineProps<{
  type?: "primary" | "default" | "danger" | "text"
  size?: "small" | "default" | "large"
  loading?: boolean
  disabled?: boolean
  block?: boolean
}>()

defineEmits<{
  click: []
}>()
</script>

<template>
  <el-button
    :type="type === 'primary' ? 'primary' : type === 'danger' ? 'danger' : type === 'text' ? 'text' : 'default'"
    :size="size === 'small' ? 'small' : size === 'large' ? 'large' : 'default'"
    :loading="loading"
    :disabled="disabled"
    :class="{ 'base-btn--block': block }"
    @click="$emit('click')"
  >
    <slot />
  </el-button>
</template>

<style scoped>
.base-btn--block {
  width: 100%;
}
</style>
