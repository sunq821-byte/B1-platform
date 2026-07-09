<script setup lang="ts">
defineProps<{
  status: string
}>()

const STATUS_MAP: Record<string, { bg: string; color: string; label: string }> = {
  NOT_SUBMITTED: { bg: "#FEF3C7", color: "#92400E", label: "待提交" },
  SUBMITTED: { bg: "#DBEAFE", color: "#1E40AF", label: "已提交" },
  AI_EVALUATING: { bg: "#EDE9FE", color: "#6D28D9", label: "AI分析中" },
  AI_COMPLETED: { bg: "#D1FAE5", color: "#065F46", label: "待评分" },
  TEACHER_SCORING: { bg: "#FFF7ED", color: "#9A3412", label: "评分中" },
  COMPLETED: { bg: "#D1FAE5", color: "#065F46", label: "已完成" },
  REVIEWED: { bg: "#D1FAE5", color: "#065F46", label: "已完成" },
  REJECTED: { bg: "#FEE2E2", color: "#991B1B", label: "已退回" },
}

function getConfig(status: string) {
  return STATUS_MAP[status] || { bg: "#F1F5F9", color: "#64748B", label: status || "未知" }
}
</script>

<template>
  <span
    class="task-status-badge"
    :style="{
      backgroundColor: getConfig(status).bg,
      color: getConfig(status).color,
    }"
  >
    {{ getConfig(status).label }}
  </span>
</template>

<style scoped>
.task-status-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 100px;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
}
</style>
