<script setup lang="ts">
import { computed, type Component } from "vue"
import * as LucideIcons from "lucide-vue-next"

const props = defineProps<{
  modelValue: string
  placeholder?: string
  type?: string
  disabled?: boolean
  showPassword?: boolean
  prefixIcon?: string
  size?: "large" | "default" | "small"
}>()

const emit = defineEmits<{
  "update:modelValue": [value: string]
}>()

const iconComponent = computed<Component | undefined>(() => {
  if (!props.prefixIcon) return undefined
  const icons = LucideIcons as unknown as Record<string, Component>
  return icons[props.prefixIcon] ?? undefined
})
</script>

<template>
  <el-input
    :model-value="modelValue"
    :placeholder="placeholder"
    :type="type ?? 'text'"
    :disabled="disabled"
    :show-password="showPassword"
    :size="size ?? 'default'"
    v-bind="$attrs"
    @update:model-value="(val: string) => emit('update:modelValue', val)"
  >
    <template v-if="iconComponent" #prefix>
      <component :is="iconComponent" :size="16" />
    </template>
  </el-input>
</template>
