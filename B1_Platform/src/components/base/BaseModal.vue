<script setup lang="ts">
withDefaults(defineProps<{
  modelValue: boolean
  title: string
  width?: string
  confirmText?: string
  cancelText?: string
  loading?: boolean
}>(), {
  width: '480px',
  confirmText: '确认',
  cancelText: '取消',
  loading: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: []
  cancel: []
}>()

function handleConfirm() {
  emit('confirm')
}

function handleCancel() {
  emit('update:modelValue', false)
  emit('cancel')
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    :width="width"
    :close-on-click-modal="false"
    v-bind="$attrs"
    @update:model-value="(val: boolean) => emit('update:modelValue', val)"
  >
    <slot />
    <template #footer>
      <slot name="footer">
        <div class="base-modal__footer">
          <el-button @click="handleCancel">
            {{ cancelText }}
          </el-button>
          <el-button type="primary" :loading="loading" @click="handleConfirm">
            {{ confirmText }}
          </el-button>
        </div>
      </slot>
    </template>
  </el-dialog>
</template>

<style scoped>
.base-modal__footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm, 8px);
}
</style>