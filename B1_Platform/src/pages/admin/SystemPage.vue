<script setup lang="ts">
import { ref, onMounted } from "vue"
import { ElMessage } from "element-plus"
import { useAdminStore } from "@/stores/useAdminStore"
import PageHeader from "@/components/layout/PageHeader.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"

const store = useAdminStore()
const loadError = ref("")

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

/** 只读展示：与当前表单学期对齐，不提供新建入口 */
const semesters = ref([
  { id: "SEM001", name: "2025-2026-2", start: "2026-02-24", end: "2026-07-10", status: "active" as const },
  { id: "SEM002", name: "2025-2026-1", start: "2025-09-01", end: "2026-01-17", status: "archived" as const },
  { id: "SEM003", name: "2024-2025-2", start: "2025-02-24", end: "2025-07-11", status: "archived" as const },
])

async function loadConfig() {
  loadError.value = ""
  try {
    await store.fetchSystemConfig()
    if (store.systemConfig) {
      form.value = { ...store.systemConfig }
      const current = semesters.value.find((s) => s.name === form.value.currentSemester)
      if (current) {
        semesters.value = semesters.value.map((s) => ({
          ...s,
          status: s.name === form.value.currentSemester ? "active" : "archived",
          start: s.name === form.value.currentSemester ? form.value.semesterStart || s.start : s.start,
          end: s.name === form.value.currentSemester ? form.value.semesterEnd || s.end : s.end,
        }))
      }
    }
  } catch (e: unknown) {
    loadError.value = (e as Error)?.message || "加载系统配置失败"
  }
}

async function handleSave() {
  await store.saveSystemConfig({
    ...form.value,
    aiModelVersion: store.systemConfig?.aiModelVersion || "v2.1.0",
  })
  ElMessage.success("系统配置已保存")
  await loadConfig()
}

onMounted(() => { loadConfig() })
</script>

<template>
  <div>
    <PageHeader title="系统配置" subtitle="管理平台全局参数" />

    <LoadingState v-if="store.configLoading" text="加载系统配置..." />
    <ErrorState v-else-if="loadError" :message="loadError" @retry="loadConfig" />

    <template v-else>
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
            <div class="form-group flex-1">
              <label class="form-label">学期开始</label>
              <input v-model="form.semesterStart" type="date" class="form-input" />
            </div>
            <div class="form-group flex-1">
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
          <div class="card-title mb-16">AI 与运行开关</div>
          <div class="form-group">
            <label class="form-label">AI 模型版本</label>
            <input
              class="form-input"
              :value="store.systemConfig?.aiModelVersion || 'v2.1.0'"
              disabled
            />
          </div>
          <label class="check">
            <input v-model="form.autoAnalyze" type="checkbox" />
            <span>提交后自动 AI 分析</span>
          </label>
          <label class="check">
            <input v-model="form.notificationEnabled" type="checkbox" />
            <span>启用系统通知</span>
          </label>
          <label class="check is-danger">
            <input v-model="form.maintenanceMode" type="checkbox" />
            <span>维护模式</span>
          </label>
          <button type="button" class="btn btn-primary mt-16" @click="handleSave">
            保存配置
          </button>
        </div>
      </div>

      <div class="section mt-24">
        <div class="section-header">
          <h2 class="section-title">学期记录（只读）</h2>
        </div>
        <div class="data-table">
          <table>
            <thead>
              <tr>
                <th>学期</th>
                <th>开始日期</th>
                <th>结束日期</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="s in semesters" :key="s.id">
                <td>{{ s.name }}</td>
                <td><span class="mono">{{ s.start }}</span></td>
                <td><span class="mono">{{ s.end }}</span></td>
                <td>
                  <span v-if="s.status === 'active'" class="badge badge--success">当前</span>
                  <span v-else class="badge badge--default">已归档</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <p class="hint">
          学期切换请在上方「当前学期」中修改并保存；不提供独立「新建学期」入口。
        </p>
      </div>
    </template>
  </div>
</template>

<style scoped>
.flex-1 { flex: 1; }
.section-title {
  font-size: 18px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--color-navy, #0B3A5C);
  margin: 0;
}
.mono {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--color-text-secondary);
}
.hint {
  margin: 12px 0 0;
  font-size: 12px;
  color: var(--color-text-placeholder);
}
.check {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 13px;
  cursor: pointer;
  color: var(--color-text-primary);
}
.check.is-danger {
  color: var(--color-danger, #FF4D4F);
  font-weight: 650;
}
.mt-16 { margin-top: 16px; }
.mt-24 { margin-top: 24px; }
.mb-16 { margin-bottom: 16px; }
</style>
