<script setup lang="ts">
interface SelectOption {
  label: string
  value: string | number
  disabled?: boolean
}

defineProps<{
  modelValue: string | number | undefined
  options: SelectOption[]
  placeholder?: string
  disabled?: boolean
  clearable?: boolean
  filterable?: boolean
  size?: "large" | "default" | "small"
}>()

const emit = defineEmits<{
  "update:modelValue": [value: string | number]
}>()
</script>

<template>
  <el-select
    :model-value="modelValue"
    :placeholder="placeholder"
    :disabled="disabled"
    :clearable="clearable"
    :filterable="filterable"
    :size="size ?? 'default'"
    v-bind="$attrs"
    @update:model-value="(val: string | number) => emit('update:modelValue', val)"
  >
    <el-option
      v-for="opt in options"
      :key="opt.value"
      :label="opt.label"
      :value="opt.value"
      :disabled="opt.disabled"
    />
  </el-select>
</template>
