<script setup lang="ts">
import { ref } from "vue"
import type { FormInstance, FormRules } from "element-plus"

const props = defineProps<{
  model: Record<string, unknown>
  rules?: FormRules
  labelWidth?: string
  labelPosition?: "top" | "left" | "right"
  size?: "large" | "default" | "small"
}>()

const formRef = ref<FormInstance>()

function validate(): Promise<boolean> {
  return formRef.value?.validate() ?? Promise.resolve(true)
}

function resetFields() {
  formRef.value?.resetFields()
}

function clearValidate() {
  formRef.value?.clearValidate()
}

defineExpose({ validate, resetFields, clearValidate })
</script>

<template>
  <el-form
    ref="formRef"
    :model="props.model"
    :rules="props.rules"
    :label-width="props.labelWidth ?? '100px'"
    :label-position="props.labelPosition ?? 'top'"
    :size="props.size ?? 'default'"
    v-bind="$attrs"
  >
    <slot />
  </el-form>
</template>
