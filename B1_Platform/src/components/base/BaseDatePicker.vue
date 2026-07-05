<script setup lang="ts">
withDefaults(defineProps<{
  modelValue: string | Date | [string, string] | [Date, Date]
  type?: string
  placeholder?: string
  disabled?: boolean
  clearable?: boolean
  rangeSeparator?: string
  startPlaceholder?: string
  endPlaceholder?: string
  format?: string
  valueFormat?: string
}>(), {
  type: 'date',
  placeholder: '请选择日期',
  disabled: false,
  clearable: false,
  rangeSeparator: '至',
  startPlaceholder: '开始日期',
  endPlaceholder: '结束日期',
  format: 'YYYY-MM-DD',
  valueFormat: 'YYYY-MM-DD',
})

const emit = defineEmits<{
  'update:modelValue': [value: string | Date | [string, string] | [Date, Date]]
}>()
</script>

<template>
  <el-date-picker
    :model-value="modelValue"
    :type="type"
    :placeholder="placeholder"
    :disabled="disabled"
    :clearable="clearable"
    :range-separator="rangeSeparator"
    :start-placeholder="startPlaceholder"
    :end-placeholder="endPlaceholder"
    :format="format"
    :value-format="valueFormat"
    v-bind="$attrs"
    @update:model-value="(val: any) => emit('update:modelValue', val)"
  />
</template>