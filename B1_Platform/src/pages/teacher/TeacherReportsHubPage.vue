<script setup lang="ts">
import TabHub from "@/components/layout/TabHub.vue"
import ReportsPage from "@/pages/teacher/ReportsPage.vue"
import ReportsCollegePage from "@/pages/teacher/ReportsCollegePage.vue"

const tabs = [
  { key: "class", label: "班级报告" },
  { key: "college", label: "学院报告" },
]
</script>

<template>
  <TabHub title="数据报表" subtitle="班级成绩统计与跨班对比分析" :tabs="tabs">
    <template #default="{ activeTab }">
      <ReportsPage v-if="activeTab === 'class'" />
      <ReportsCollegePage v-else-if="activeTab === 'college'" />
    </template>
  </TabHub>
</template>
