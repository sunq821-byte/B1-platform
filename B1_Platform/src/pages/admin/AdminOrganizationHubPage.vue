<script setup lang="ts">
import TabHub from "@/components/layout/TabHub.vue"
import UsersPage from "@/pages/admin/UsersPage.vue"
import ClassesPage from "@/pages/admin/ClassesPage.vue"

const tabs = [
  { key: "users", label: "用户管理" },
  { key: "classes", label: "班级管理" },
]
</script>

<template>
  <TabHub title="组织管理" subtitle="用户账号与班级信息维护" :tabs="tabs">
    <template #default="{ activeTab }">
      <UsersPage v-if="activeTab === 'users'" />
      <ClassesPage v-else-if="activeTab === 'classes'" />
    </template>
  </TabHub>
</template>
