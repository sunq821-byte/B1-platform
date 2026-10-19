<script setup lang="ts">
import TabHub from "@/components/layout/TabHub.vue"
import CourseListPage from "@/pages/teacher/CourseListPage.vue"
import TrainingPage from "@/pages/teacher/TrainingPage.vue"
import StudentsPage from "@/pages/teacher/StudentsPage.vue"

const tabs = [
  { key: "courses", label: "课程" },
  { key: "tasks", label: "实训任务" },
  { key: "progress", label: "提交进度" },
]
</script>

<template>
  <TabHub title="课程与任务" subtitle="课程管理、任务发布与学生提交进度" :tabs="tabs">
    <template #default="{ activeTab }">
      <CourseListPage v-if="activeTab === 'courses'" />
      <TrainingPage v-else-if="activeTab === 'tasks'" />
      <StudentsPage v-else-if="activeTab === 'progress'" />
    </template>
  </TabHub>
</template>
