<script setup lang="ts">
import { ref, computed, onMounted } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { useTeacherStore } from "@/stores/useTeacherStore"
import type { ICourseFormData } from "@/types/teacher"
import PageHeader from "@/components/layout/PageHeader.vue"
import BaseButton from "@/components/base/BaseButton.vue"
import BaseInput from "@/components/base/BaseInput.vue"
import BaseModal from "@/components/base/BaseModal.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"

const store = useTeacherStore()
const loadError = ref("")

const showModal = ref(false)
const isEdit = ref(false)
const editingId = ref("")
const saving = ref(false)
const form = ref<ICourseFormData>({
  courseCode: "",
  courseName: "",
  className: "",
  semester: "2025-2026-2",
  credits: 4,
})

const modalTitle = computed(() => (isEdit.value ? "编辑课程" : "新建课程"))

function resetForm() {
  form.value = { courseCode: "", courseName: "", className: "", semester: "2025-2026-2", credits: 4 }
  isEdit.value = false
  editingId.value = ""
}

function openCreate() {
  resetForm()
  showModal.value = true
}

function openEdit(course: { courseId: string; courseCode: string; courseName: string; className: string; semester: string; credits: number }) {
  isEdit.value = true
  editingId.value = course.courseId
  form.value = {
    courseCode: course.courseCode,
    courseName: course.courseName,
    className: course.className,
    semester: course.semester,
    credits: course.credits,
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

async function handleSave() {
  if (!form.value.courseCode.trim() || !form.value.courseName.trim()) {
    ElMessage.warning("请填写课程编号和名称")
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await store.updateCourse(editingId.value, { ...form.value })
      ElMessage.success("课程已更新")
    } else {
      await store.createCourse({ ...form.value })
      ElMessage.success("课程已创建")
    }
    closeModal()
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || "操作失败")
  } finally {
    saving.value = false
  }
}

async function handleDelete(courseId: string) {
  try {
    await ElMessageBox.confirm("确定删除该课程吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    })
    await store.deleteCourse(courseId)
    ElMessage.success("课程已删除")
  } catch {
    // cancelled
  }
}

async function initPage() {
  loadError.value = ""
  try {
    await store.fetchCourses()
  } catch (e: unknown) {
    loadError.value = (e as Error)?.message || "加载课程列表失败"
  }
}

onMounted(() => { initPage() })
</script>

<template>
  <div class="page">
    <PageHeader title="课程管理" :subtitle="`共 ${store.courses.length} 门课程`" />

    <LoadingState v-if="store.coursesLoading" />
    <ErrorState v-else-if="loadError" :message="loadError" @retry="initPage" />

    <template v-else>
      <div class="toolbar">
        <BaseButton type="primary" @click="openCreate">+ 新建课程</BaseButton>
      </div>

      <div class="data-table">
        <table>
          <thead>
            <tr>
              <th>课程编号</th>
              <th>课程名称</th>
              <th>班级</th>
              <th>学期</th>
              <th>学生数</th>
              <th>学分</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="c in store.courses" :key="c.courseId">
              <td><span class="mono">{{ c.courseCode }}</span></td>
              <td>{{ c.courseName }}</td>
              <td>{{ c.className || c.courseName }}</td>
              <td><span class="mono">{{ c.semester }}</span></td>
              <td>{{ c.studentCount }}</td>
              <td>{{ c.credits }}</td>
              <td class="actions-cell">
                <BaseButton size="small" @click="openEdit(c)">编辑</BaseButton>
                <BaseButton size="small" type="danger" @click="handleDelete(c.courseId)">删除</BaseButton>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <BaseModal v-model="showModal" :title="modalTitle" :loading="saving" @confirm="handleSave" @cancel="closeModal">
        <div class="form-group">
          <label class="form-label">课程编号</label>
          <BaseInput v-model="form.courseCode" placeholder="如 CS301" />
        </div>
        <div class="form-group">
          <label class="form-label">课程名称</label>
          <BaseInput v-model="form.courseName" placeholder="如 软件工程综合实训" />
        </div>
        <div class="form-group">
          <label class="form-label">班级</label>
          <BaseInput v-model="form.className" placeholder="如 软件2401" />
        </div>
        <div class="form-group">
          <label class="form-label">学期</label>
          <BaseInput v-model="form.semester" placeholder="如 2025-2026-2" />
        </div>
        <div class="form-group">
          <label class="form-label">学分</label>
          <BaseInput :model-value="String(form.credits)" @update:model-value="(v: string) => (form.credits = Number(v) || 0)" type="number" />
        </div>
      </BaseModal>
    </template>
  </div>
</template>

<style scoped>
.page {
  /* width/centering handled by AppLayout */
}

.toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

/* data-table (mirrors prototype global.css) */
.data-table {
  width: 100%;
  background: var(--color-card, #fff);
  border: 1px solid var(--color-border, #e2e8f0);
  border-radius: var(--radius-md, 8px);
  overflow: hidden;
}

.data-table table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  text-align: left;
  padding: 10px 16px;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-placeholder, #94a3b8);
  text-transform: uppercase;
  letter-spacing: 0.03em;
  background: #f8fafc;
  border-bottom: 1px solid var(--color-border, #e2e8f0);
}

.data-table td {
  padding: 11px 16px;
  font-size: 14px;
  border-bottom: 1px solid #f1f5f9;
}

.data-table tbody tr:last-child td {
  border-bottom: none;
}

.data-table tbody tr:hover td {
  background: #f8fafc;
}

.mono {
  font-family: var(--font-mono);
  font-size: 12px;
}

.actions-cell {
  display: flex;
  gap: 4px;
}

/* form */
.form-group {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary, #64748b);
  margin-bottom: 5px;
}
</style>
