<script setup lang="ts">
defineProps<{
  data: Record<string, unknown>[]
  loading?: boolean
  emptyText?: string
  stripe?: boolean
  border?: boolean
  height?: string | number
  maxHeight?: string | number
}>()
</script>

<template>
  <el-table
    v-loading="loading"
    :data="data"
    :stripe="stripe"
    :border="border"
    :height="height"
    :max-height="maxHeight"
    v-bind="$attrs"
  >
    <template v-if="data.length === 0 && !loading" #empty>
      <span>{{ emptyText ?? "暂无数据" }}</span>
    </template>
    <slot />
  </el-table>
</template>
