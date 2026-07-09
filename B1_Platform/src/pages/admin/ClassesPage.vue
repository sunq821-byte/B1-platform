<script setup lang="ts">
import { ref, onMounted } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { useAdminStore } from "@/stores/useAdminStore"
import type { IClassItem } from "@/types/admin"
import PageHeader from "@/components/layout/PageHeader.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"

const store = useAdminStore()
const loadError = ref("")

const modalVisible = ref(false)
const modalTitle = ref("新建班级")
const editingId = ref<string | null>(null)
const form = ref({ name: "", teacherName: "" })

function openCreate() {
  editingId.value = null
  modalTitle.value = "新建班级"
  form.value = { name: "", teacherName: "" }
  modalVisible.value = true
}
function openEdit(c: IClassItem) {
  editingId.value = c.id
  modalTitle.value = "编辑班级"
  form.value = { name: c.name, teacherName: c.teacherName }
  modalVisible.value = true
}
async function handleSave() {
  if (!form.value.name.trim()) { ElMessage.warning("请输入班级名称"); return }
  if (editingId.value) {
    await store.updateClass(editingId.value, { ...form.value })
    ElMessage.success("班级已更新")
  } else {
    await store.createClass({ ...form.value })
    ElMessage.success("班级已创建")
  }
  modalVisible.value = false
}
async function handleDelete(c: IClassItem) {
  await ElMessageBox.confirm(`确定要删除班级 ${c.name} 吗？`, "确认删除", { type: "warning" })
  await store.deleteClass(c.id)
  ElMessage.success("班级已删除")
}

onMounted(() => { store.fetchClasses() })
</script>

<template>
  <div>
    <PageHeader title="班级管理" :subtitle="`共 ${store.classes.length} 个班级`" />

    <LoadingState v-if="store.classesLoading" text="加载班级列表..." />

    <template v-else>
    <div class="filter-bar">
      <button class="btn btn-primary" style="margin-left:auto;" @click="openCreate">+ 新建班级</button>
    </div>

    <div class="data-table">
      <table>
        <thead><tr><th>班级编号</th><th>班级名称</th><th>学生人数</th><th>班主任</th><th>创建时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="c in store.classes" :key="c.id">
            <td><span style="font-family:var(--font-mono);font-size:11px;">{{ c.id }}</span></td>
            <td>{{ c.name }}</td>
            <td>{{ c.studentCount }}</td>
            <td>{{ c.teacherName }}</td>
            <td><span style="font-family:var(--font-mono);font-size:11px;">{{ c.createdAt }}</span></td>
            <td>
              <button class="btn btn-ghost btn-sm" @click="openEdit(c)">编辑</button>
              <button class="btn btn-ghost btn-sm" style="color:var(--color-danger,#ef4444);" @click="handleDelete(c)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="modalVisible" class="modal-overlay" @click.self="modalVisible = false">
      <div class="modal">
        <div class="modal-header">
          <h3 class="modal-title">{{ modalTitle }}</h3>
          <span class="modal-close" style="cursor:pointer;font-size:18px;" @click="modalVisible = false">&times;</span>
        </div>
        <div class="form-group"><label class="form-label">班级名称</label><input v-model="form.name" class="form-input" placeholder="如 软件2401" /></div>
        <div class="form-group"><label class="form-label">班主任</label><input v-model="form.teacherName" class="form-input" placeholder="教师姓名" /></div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="modalVisible = false">取消</button>
          <button class="btn btn-primary" @click="handleSave">保存</button>
        </div>
      </div>
    </div>
    </template>
  </div>
</template>
