<script setup lang="ts">
import TabHub from "@/components/layout/TabHub.vue"
import MonitorPage from "@/pages/admin/MonitorPage.vue"
import LogsPage from "@/pages/admin/LogsPage.vue"

const tabs = [
  { key: "monitor", label: "运行监控" },
  { key: "logs", label: "操作日志" },
]
</script>

<template>
  <TabHub title="运维中心" subtitle="服务健康状态与审计日志查询" :tabs="tabs">
    <template #default="{ activeTab }">
      <MonitorPage v-if="activeTab === 'monitor'" />
      <LogsPage v-else-if="activeTab === 'logs'" />
    </template>
  </TabHub>
</template>
