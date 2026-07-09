<script setup lang="ts">
import { ref, computed, onMounted } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { useTeacherStore } from "@/stores/useTeacherStore"
import type { ITeacherTaskItem } from "@/types/teacher"
import PageHeader from "@/components/layout/PageHeader.vue"
import BaseButton from "@/components/base/BaseButton.vue"
import BaseInput from "@/components/base/BaseInput.vue"
import BaseModal from "@/components/base/BaseModal.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"

const store = useTeacherStore()
const loadError = ref("")
const filterCourse = ref("all")

const filteredTasks = computed(() => {
  if (filterCourse.value === "all") return store.tasks
  return store.tasks.filter((t) => t.courseName === filterCourse.value)
})

const showModal = ref(false)
const isEdit = ref(false)
const editingId = ref("")
const saving = ref(false)
const form = ref({
  taskName: "",
  courseId: "",
  description: "",
  dueDate: "2026-07-15",
  weight: 25,
  priority: "medium" as "high" | "medium" | "low",
})

const modalTitle = computed(() => (isEdit.value ? "编辑任务" : "新建任务"))

function resetForm() {
  form.value = { taskName: "", courseId: store.courses[0]?.courseId || "", description: "", dueDate: "2026-07-15", weight: 25, priority: "medium" }
  isEdit.value = false
  editingId.value = ""
}

function openCreate() {
  resetForm()
  showModal.value = true
}

function openEdit(task: ITeacherTaskItem) {
  isEdit.value = true
  editingId.value = task.taskId
  form.value = {
    taskName: task.taskName,
    courseId: store.courses.find((c) => c.courseName === task.courseName)?.courseId || "",
    description: task.description,
    dueDate: task.deadline ? task.deadline.substring(0, 10) : "",
    weight: task.totalScore,
    priority: task.priority,
  }
  showModal.value = true
}

async function handleSave() {
  if (!form.value.taskName.trim()) { ElMessage.warning("请输入任务名称"); return }
  saving.value = true
  try {
    if (isEdit.value) {
      await store.updateTask(editingId.value, { ...form.value })
      ElMessage.success("任务已更新")
    } else {
      await store.createTask({ ...form.value })
      ElMessage.success("任务已创建")
    }
    showModal.value = false
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || "操作失败")
  } finally { saving.value = false }
}

async function handlePublish() {
  saving.value = true
  try {
    await store.publishTask(editingId.value)
    ElMessage.success("任务已发布")
    showModal.value = false
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || "发布失败")
  } finally { saving.value = false }
}

async function handleDelete(taskId: string) {
  try {
    await ElMessageBox.confirm("确定删除该任务吗？", "提示", { confirmButtonText: "确定", cancelButtonText: "取消", type: "warning" })
    await store.deleteTask(taskId)
    ElMessage.success("任务已删除")
  } catch { /* cancelled */ }
}

function statusBadge(status: string) {
  return status.toLowerCase() === "published" ? "已发布" : "草稿"
}

async function initPage() {
  loadError.value = ""
  try {
    await Promise.all([store.fetchCourses(), store.fetchTasks()])
  } catch (e: unknown) {
    loadError.value = (e as Error)?.message || "加载失败"
  }
}

onMounted(() => { initPage() })
</script>

<template>
  <div class="page">
    <PageHeader title="实训任务管理" :subtitle="`共 ${filteredTasks.length} 个任务`" />

    <LoadingState v-if="store.tasksLoading" />
    <ErrorState v-else-if="loadError" :message="loadError" @retry="initPage" />

    <template v-else>
      <div class="filter-bar">
        <select v-model="filterCourse" class="form-select">
          <option value="all">全部课程</option>
          <option v-for="c in store.courses" :key="c.courseId" :value="c.courseName">{{ c.courseName }}</option>
        </select>
      </div>
      <div class="toolbar">
        <BaseButton type="primary" @click="openCreate">+ 新建任务</BaseButton>
      </div>

      <div class="data-table">
        <table>
          <thead>
            <tr><th>任务编号</th><th>任务名称</th><th>课程</th><th>截止日期</th><th>权重</th><th>状态</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="t in filteredTasks" :key="t.taskId">
              <td><span class="mono-sm">{{ t.taskId }}</span></td>
              <td>{{ t.taskName }}</td>
              <td>{{ t.courseName }}</td>
              <td><span class="mono-sm">{{ t.deadline ? t.deadline.substring(0, 10) : '-' }}</span></td>
              <td>{{ t.totalScore }}</td>
              <td>
                <span :class="['badge', t.status.toLowerCase() === 'published' ? 'badge--pub' : 'badge--draft']">{{ statusBadge(t.status) }}</span>
              </td>
              <td class="actions-cell">
                <BaseButton size="small" @click="openEdit(t)">编辑</BaseButton>
                <BaseButton size="small" type="danger" @click="handleDelete(t.taskId)">删除</BaseButton>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <BaseModal v-model="showModal" :title="modalTitle" :loading="saving" @confirm="handleSave">
        <div class="form-group">
          <label class="form-label">任务名称</label>
          <BaseInput v-model="form.taskName" placeholder="如 用户管理系统API开发" />
        </div>
        <div class="form-group">
          <label class="form-label">所属课程</label>
          <select v-model="form.courseId" class="form-select">
            <option v-for="c in store.courses" :key="c.courseId" :value="String(c.courseId)">{{ c.courseName }}</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">任务描述</label>
          <textarea v-model="form.description" class="form-textarea" rows="3" placeholder="描述任务内容..." />
        </div>
        <div class="form-group">
          <label class="form-label">截止日期</label>
          <BaseInput v-model="form.dueDate" type="date" />
        </div>
        <div class="form-group">
          <label class="form-label">权重 (%)</label>
          <BaseInput :model-value="String(form.weight)" @update:model-value="(v: string) => (form.weight = Number(v) || 0)" type="number" />
        </div>
        <div class="form-group">
          <label class="form-label">优先级</label>
          <select v-model="form.priority" class="form-select">
            <option value="high">高</option>
            <option value="medium">中</option>
            <option value="low">低</option>
          </select>
        </div>
        <template v-if="isEdit" #footer>
          <BaseButton @click="showModal = false">取消</BaseButton>
          <BaseButton type="primary" :loading="saving" @click="handlePublish">发布任务</BaseButton>
          <BaseButton type="primary" :loading="saving" @click="handleSave">保存</BaseButton>
        </template>
      </BaseModal>
    </template>
  </div>
</template>

<style scoped>
.page { /* width/centering handled by AppLayout */ }

.filter-bar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }

.toolbar { display: flex; justify-content: flex-end; margin-bottom: 16px; }

.data-table {
  width: 100%; background: var(--color-card, #fff);
  border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-md, 8px); overflow: hidden;
}
.data-table table { width: 100%; border-collapse: collapse; }
.data-table th { text-align: left; padding: 10px 16px; font-size: 12px; font-weight: 600; color: var(--color-text-placeholder, #94a3b8); text-transform: uppercase; letter-spacing: 0.03em; background: #f8fafc; border-bottom: 1px solid var(--color-border, #e2e8f0); }
.data-table td { padding: 11px 16px; font-size: 14px; border-bottom: 1px solid #f1f5f9; }
.data-table tbody tr:last-child td { border-bottom: none; }
.data-table tbody tr:hover td { background: #f8fafc; }

.mono-sm { font-family: var(--font-mono); font-size: 12px; }

.badge { display: inline-flex; align-items: center; font-size: 11px; padding: 2px 8px; border-radius: 4px; font-weight: 500; }
.badge--pub { background: #d1fae5; color: #047857; }
.badge--draft { background: #fef3c7; color: #b45309; }

.actions-cell { display: flex; gap: 4px; }

.form-group { margin-bottom: 16px; }
.form-label { display: block; font-size: 13px; font-weight: 500; color: var(--color-text-secondary, #64748b); margin-bottom: 5px; }
.form-select { width: 100%; padding: 8px 12px; font-size: 14px; border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-sm, 4px); background: var(--color-card, #fff); color: var(--color-text-primary, #1e293b); outline: none; }
.form-select:focus { border-color: var(--color-primary, #3b82f6); }
.form-textarea { width: 100%; padding: 8px 12px; font-size: 14px; border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-sm, 4px); background: var(--color-card, #fff); color: var(--color-text-primary, #1e293b); resize: vertical; outline: none; font-family: inherit; box-sizing: border-box; }
.form-textarea:focus { border-color: var(--color-primary, #3b82f6); }
</style>
