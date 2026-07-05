<script setup lang="ts">
</script>

<template>
  <div class="error-page">
    <h1>404</h1>
    <p>抱歉，您访问的页面不存在</p>
    <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
  </div>
</template>

<style scoped>
.error-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: var(--spacing-md, 16px);
}

.error-page h1 {
  font-size: 72px;
  font-weight: 700;
  color: var(--color-text-placeholder, #94a3b8);
  margin: 0;
}

.error-page p {
  font-size: var(--font-size-base, 16px);
  color: var(--color-text-secondary, #64748b);
  margin: 0;
}
</style>
