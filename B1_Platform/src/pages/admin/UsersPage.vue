<script setup lang="ts">
import { ref, computed, onMounted } from "vue"
import { ElMessage } from "element-plus"
import { useAdminStore } from "@/stores/useAdminStore"
import type { IAdminUserItem, IAdminUserFormData } from "@/types/admin"
import PageHeader from "@/components/layout/PageHeader.vue"

const store = useAdminStore()

const filterRole = ref("all")
const searchKeyword = ref("")
const modalVisible = ref(false)
const modalTitle = ref("新建用户")
const editingUserId = ref<string | null>(null)

const form = ref<IAdminUserFormData>({
  name: "",
  role: "student",
  className: "",
  email: "",
  status: "active",
})

const filteredUsers = computed(() => {
  let list = store.users
  if (filterRole.value !== "all") {
    list = list.filter((u) => u.role === filterRole.value)
  }
  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.trim().toLowerCase()
    list = list.filter((u) => u.name.toLowerCase().includes(kw) || (u.userId || "").toLowerCase().includes(kw))
  }
  return list
})

function roleLabel(role: string) {
  const map: Record<string, string> = { student: "学生", teacher: "教师", admin: "管理员" }
  return map[role] || role
}

function roleBadgeClass(role: string) {
  const map: Record<string, string> = { student: "badge--success", teacher: "badge--info", admin: "badge--warning" }
  return map[role] || "badge--default"
}

async function loadUsers() {
  await store.fetchUsers({ role: filterRole.value === "all" ? undefined : filterRole.value, keyword: searchKeyword.value.trim() || undefined })
}

function openCreateModal() {
  editingUserId.value = null
  modalTitle.value = "新建用户"
  form.value = { name: "", role: "student", className: "", email: "", status: "active" }
  modalVisible.value = true
}

function openEditModal(user: IAdminUserItem) {
  editingUserId.value = user.userId
  modalTitle.value = "编辑用户"
  form.value = {
    name: user.name,
    role: user.role,
    className: user.className || "",
    email: user.email || "",
    status: user.status,
  }
  modalVisible.value = true
}

async function handleSave() {
  if (!form.value.name.trim()) {
    ElMessage.warning("请输入姓名")
    return
  }
  if (editingUserId.value) {
    await store.updateUser(editingUserId.value, { ...form.value })
    ElMessage.success("用户已更新")
  } else {
    await store.createUser({ ...form.value })
    ElMessage.success("用户已创建")
  }
  modalVisible.value = false
  loadUsers()
}

async function handleToggleStatus(user: IAdminUserItem) {
  await store.toggleUserStatus(user.userId)
  ElMessage.success("用户状态已更新")
}

onMounted(() => { loadUsers() })
</script>

<template>
  <div>
    <PageHeader title="用户管理" :subtitle="`共 ${filteredUsers.length} 个用户`" />

    <div class="filter-bar">
      <select v-model="filterRole" class="form-select" style="width:140px;" @change="loadUsers">
        <option value="all">全部角色</option>
        <option value="student">学生</option>
        <option value="teacher">教师</option>
        <option value="admin">管理员</option>
      </select>
      <input
        v-model="searchKeyword"
        class="form-input"
        placeholder="搜索姓名..."
        style="width:200px;"
        @input="loadUsers"
      />
      <button class="btn btn-primary" style="margin-left:auto;" @click="openCreateModal">+ 新建用户</button>
    </div>

    <div class="data-table">
      <table>
        <thead>
          <tr>
            <th>用户ID</th>
            <th>姓名</th>
            <th>角色</th>
            <th>班级/部门</th>
            <th>邮箱</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in filteredUsers" :key="u.userId">
            <td><span style="font-family:var(--font-mono);font-size:11px;">{{ u.userId }}</span></td>
            <td>{{ u.name }}</td>
            <td><span :class="`badge ${roleBadgeClass(u.role)}`">{{ roleLabel(u.role) }}</span></td>
            <td>{{ u.className || "-" }}</td>
            <td><span style="color:var(--color-text-tertiary,#94a3b8);font-size:12px;">{{ u.email || "-" }}</span></td>
            <td>
              <span v-if="u.status === 'active'" class="badge badge--success">启用</span>
              <span v-else class="badge badge--danger">禁用</span>
            </td>
            <td><span style="font-family:var(--font-mono);font-size:11px;">{{ u.createdAt ? u.createdAt.substring(0, 10) : "-" }}</span></td>
            <td>
              <button class="btn btn-ghost btn-sm" @click="openEditModal(u)">编辑</button>
              <button
                class="btn btn-ghost btn-sm"
                :style="{ color: u.status === 'active' ? 'var(--color-danger,#ef4444)' : 'var(--color-success,#10b981)' }"
                @click="handleToggleStatus(u)"
              >{{ u.status === "active" ? "禁用" : "启用" }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal -->
    <div v-if="modalVisible" class="modal-overlay" @click.self="modalVisible = false">
      <div class="modal">
        <div class="modal-header">
          <h3 class="modal-title">{{ modalTitle }}</h3>
          <span class="modal-close" style="cursor:pointer;font-size:18px;" @click="modalVisible = false">&times;</span>
        </div>
        <div class="form-group">
          <label class="form-label">姓名</label>
          <input v-model="form.name" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">角色</label>
          <select v-model="form.role" class="form-select">
            <option value="student">学生</option>
            <option value="teacher">教师</option>
            <option value="admin">管理员</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">班级</label>
          <input v-model="form.className" class="form-input" placeholder="如 软件2401" />
        </div>
        <div class="form-group">
          <label class="form-label">邮箱</label>
          <input v-model="form.email" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">状态</label>
          <select v-model="form.status" class="form-select">
            <option value="active">启用</option>
            <option value="inactive">禁用</option>
          </select>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="modalVisible = false">取消</button>
          <button class="btn btn-primary" @click="handleSave">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>
