<script setup lang="ts">
withDefaults(defineProps<{
  currentPage: number
  pageSize: number
  total: number
  pageSizes?: number[]
  layout?: string
  background?: boolean
}>(), {
  pageSizes: () => [10, 20, 50, 100],
  layout: 'total, sizes, prev, pager, next, jumper',
  background: true,
})

const emit = defineEmits<{
  'update:currentPage': [value: number]
  'update:pageSize': [value: number]
  change: []
}>()

function handleSizeChange(val: number) {
  emit('update:pageSize', val)
  emit('change')
}

function handleCurrentChange(val: number) {
  emit('update:currentPage', val)
  emit('change')
}
</script>

<template>
  <div class="base-pagination">
    <el-pagination
      :current-page="currentPage"
      :page-size="pageSize"
      :total="total"
      :page-sizes="pageSizes"
      :layout="layout"
      :background="background"
      v-bind="$attrs"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<style scoped>
.base-pagination {
  display: flex;
  justify-content: flex-end;
  padding: var(--spacing-md, 16px) 0;
}
</style>