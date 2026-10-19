<script setup lang="ts">
import TabHub from "@/components/layout/TabHub.vue"
import StudentReportsPage from "@/pages/student/StudentReportsPage.vue"
import GrowthPage from "@/pages/student/GrowthPage.vue"

const tabs = [
  { key: "reports", label: "成绩报告" },
  { key: "growth", label: "成长轨迹" },
]
</script>

<template>
  <TabHub title="学情分析" subtitle="成绩汇总、能力雷达与成长时间线" :tabs="tabs">
    <template #default="{ activeTab }">
      <StudentReportsPage v-if="activeTab === 'reports'" />
      <GrowthPage v-else-if="activeTab === 'growth'" />
    </template>
  </TabHub>
</template>
