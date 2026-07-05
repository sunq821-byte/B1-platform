<script setup lang="ts">
import { ref, onMounted } from "vue"
import { ElMessage } from "element-plus"
import { useAdminStore } from "@/stores/useAdminStore"
import PageHeader from "@/components/layout/PageHeader.vue"

const store = useAdminStore()

const form = ref({
  systemName: "",
  currentSemester: "",
  semesterStart: "",
  semesterEnd: "",
  maxUploadSize: 50,
  autoAnalyze: true,
  notificationEnabled: true,
  maintenanceMode: false,
})

const semesters = ref([
  { id: "SEM001", name: "2025-2026-2", start: "2026-02-24", end: "2026-07-10", status: "active" as const },
  { id: "SEM002", name: "2025-2026-1", start: "2025-09-01", end: "2026-01-17", status: "archived" as const },
  { id: "SEM003", name: "2024-2025-2", start: "2025-02-24", end: "2025-07-11", status: "archived" as const },
])

onMounted(async () => {
  await store.fetchSystemConfig()
  if (store.systemConfig) {
    form.value = { ...store.systemConfig }
  }
})

async function handleSave() {
  await store.saveSystemConfig({
    ...form.value,
    aiModelVersion: store.systemConfig?.aiModelVersion || "v2.1.0",
  })
  ElMessage.success("系统配置已保存")
}
</script>

<template>
  <div>
    <PageHeader title="系统配置" subtitle="管理平台全局参数" />

    <div class="content-grid">
      <div class="card">
        <div class="card-title mb-16">基本设置</div>
        <div class="form-group">
          <label class="form-label">系统名称</label>
          <input v-model="form.systemName" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">当前学期</label>
          <input v-model="form.currentSemester" class="form-input" />
        </div>
        <div class="form-row">
          <div class="form-group" style="flex:1;">
            <label class="form-label">学期开始</label>
            <input v-model="form.semesterStart" type="date" class="form-input" />
          </div>
          <div class="form-group" style="flex:1;">
            <label class="form-label">学期结束</label>
            <input v-model="form.semesterEnd" type="date" class="form-input" />
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">最大上传大小 (MB)</label>
          <input v-model.number="form.maxUploadSize" type="number" class="form-input" />
        </div>
      </div>

      <div class="card">
        <div class="card-title mb-16">AI 设置</div>
        <div class="form-group">
          <label class="form-label">AI 模型版本</label>
          <input class="form-input" :value="store.systemConfig?.aiModelVersion || 'v2.1.0'" disabled />
        </div>
        <div class="form-group">
          <label style="display:flex;align-items:center;gap:8px;cursor:pointer;">
            <input v-model="form.autoAnalyze" type="checkbox" />
            <span class="form-label" style="margin:0;">提交后自动AI分析</span>
          </label>
        </div>
        <div class="form-group">
          <label style="display:flex;align-items:center;gap:8px;cursor:pointer;">
            <input v-model="form.notificationEnabled" type="checkbox" />
            <span class="form-label" style="margin:0;">启用系统通知</span>
          </label>
        </div>
        <div class="form-group">
          <label style="display:flex;align-items:center;gap:8px;cursor:pointer;">
            <input v-model="form.maintenanceMode" type="checkbox" />
            <span class="form-label" style="margin:0;color:var(--color-danger,#ef4444);">维护模式</span>
          </label>
        </div>
        <button class="btn btn-primary mt-16" @click="handleSave">保存配置</button>
      </div>
    </div>

    <div class="section mt-24">
      <div class="section-header">
        <h2 class="section-title">学期管理</h2>
        <button class="btn btn-outline btn-sm">+ 新建学期</button>
      </div>
      <div class="data-table">
        <table>
          <thead>
            <tr>
              <th>学期</th>
              <th>开始日期</th>
              <th>结束日期</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="sem in semesters" :key="sem.id">
              <td><span style="font-family:var(--font-mono);font-size:12px;">{{ sem.name }}</span></td>
              <td>{{ sem.start }}</td>
              <td>{{ sem.end }}</td>
              <td>
                <span v-if="sem.status === 'active'" class="badge badge--success">当前</span>
                <span v-else class="badge badge--default">已归档</span>
              </td>
              <td><button class="btn btn-ghost btn-sm">详情</button></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
