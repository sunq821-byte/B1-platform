<script setup lang="ts">
import { ref, computed, onMounted } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import {
  UploadCloud, FileText, Database, Search, Upload, Eye,
} from "lucide-vue-next"
import PageHeader from "@/components/layout/PageHeader.vue"
import BaseButton from "@/components/base/BaseButton.vue"
import BaseInput from "@/components/base/BaseInput.vue"
import BaseModal from "@/components/base/BaseModal.vue"

// -- Types --
interface KbFile {
  id: string
  name: string
  tag: string
  tagLabel: string
  tagColor: string
  visibility: string
  downloads: number
  uploadedAt: string
  size: string
}

// -- State --
const filterTag = ref("all")
const searchKeyword = ref("")
const loading = ref(false)

const showModal = ref(false)
const isEdit = ref(false)
const editingId = ref("")
const form = ref<{ name: string; visibility: string[]; tag: string }>({ name: "", visibility: ["全院可见"], tag: "java" })

// -- Tag config --
const tags = [
  { value: "all", label: "全部" },
  { value: "java", label: "Java/Spring" },
  { value: "frontend", label: "前端开发" },
  { value: "db", label: "数据库" },
  { value: "doc", label: "文档规范" },
]
const tagMeta: Record<string, { label: string; color: string; bg: string }> = {
  java: { label: "Java/Spring", color: "#c2410c", bg: "#fff7ed" },
  frontend: { label: "前端开发", color: "#1d4ed8", bg: "#eff6ff" },
  db: { label: "数据库", color: "#047857", bg: "#ecfdf5" },
  doc: { label: "文档规范", color: "#6b21a8", bg: "#f3e8ff" },
}

// -- Mock data --
const files = ref<KbFile[]>([
  {
    id: "1", name: "Spring Boot开发规范手册.pdf", tag: "java",
    tagLabel: "Java/Spring", tagColor: "#c2410c",
    visibility: "全院可见", downloads: 234, uploadedAt: "2026-06-10", size: "2.4 MB",
  },
  {
    id: "2", name: "软件设计文档规范模板v2025.docx", tag: "doc",
    tagLabel: "文档规范", tagColor: "#6b21a8",
    visibility: "软件2401班", downloads: 89, uploadedAt: "2026-06-15", size: "1.1 MB",
  },
  {
    id: "3", name: "MySQL优化实践.pdf", tag: "db",
    tagLabel: "数据库", tagColor: "#047857",
    visibility: "全院可见", downloads: 156, uploadedAt: "2026-05-20", size: "3.8 MB",
  },
  {
    id: "4", name: "Vue3 组件设计实战指南.pdf", tag: "frontend",
    tagLabel: "前端开发", tagColor: "#1d4ed8",
    visibility: "软件2401班", downloads: 78, uploadedAt: "2026-06-01", size: "1.6 MB",
  },
  {
    id: "5", name: "MyBatis-Plus分页插件使用指南.docx", tag: "java",
    tagLabel: "Java/Spring", tagColor: "#c2410c",
    visibility: "全院可见", downloads: 312, uploadedAt: "2026-06-20", size: "0.8 MB",
  },
  {
    id: "6", name: "Java代码规范手册（阿里巴巴版）.pdf", tag: "java",
    tagLabel: "Java/Spring", tagColor: "#c2410c",
    visibility: "全院可见", downloads: 187, uploadedAt: "2026-04-15", size: "4.2 MB",
  },
])

// -- Computed --
const filteredFiles = computed(() => {
  return files.value.filter((f) => {
    if (filterTag.value !== "all" && f.tag !== filterTag.value) return false
    if (searchKeyword.value && !f.name.toLowerCase().includes(searchKeyword.value.toLowerCase())) return false
    return true
  })
})

// -- Methods --
function fileIcon(name: string) {
  if (name.endsWith(".pdf")) return { icon: FileText, color: "#ef4444" }
  if (name.endsWith(".docx") || name.endsWith(".doc")) return { icon: FileText, color: "#3b82f6" }
  return { icon: Database, color: "#10b981" }
}

const visibilityOptions = ["全院可见", "软件2401班", "软件2402班", "软件2501班", "仅自己"]

function visibilityArray(str: string): string[] {
  return str ? str.split(",").map((s) => s.trim()).filter(Boolean) : []
}

function openCreate() {
  isEdit.value = false
  editingId.value = ""
  form.value = { name: "", visibility: ["全院可见"], tag: "java" }
  showModal.value = true
}

function openEdit(f: KbFile) {
  isEdit.value = true
  editingId.value = f.id
  form.value = { name: f.name, visibility: visibilityArray(f.visibility), tag: f.tag }
  showModal.value = true
}

async function handleSave() {
  if (!form.value.name.trim()) {
    ElMessage.warning("请输入资料名称")
    return
  }
  if (isEdit.value) {
    const f = files.value.find((x) => x.id === editingId.value)
    if (f) {
      f.name = form.value.name
      f.visibility = form.value.visibility.join(",")
      f.tag = form.value.tag
      f.tagLabel = tagMeta[form.value.tag]?.label ?? form.value.tag
      f.tagColor = tagMeta[form.value.tag]?.color ?? "#64748b"
    }
    ElMessage.success("资料已更新")
  } else {
    const newFile: KbFile = {
      id: `kb-${Date.now()}`,
      name: form.value.name,
      tag: form.value.tag,
      tagLabel: tagMeta[form.value.tag]?.label ?? form.value.tag,
      tagColor: tagMeta[form.value.tag]?.color ?? "#64748b",
      visibility: form.value.visibility.join(","),
      downloads: 0,
      uploadedAt: new Date().toISOString().substring(0, 10),
      size: "0 MB",
    }
    files.value.unshift(newFile)
    ElMessage.success("资料已上传")
  }
  showModal.value = false
}

async function handleDelete(f: KbFile) {
  try {
    await ElMessageBox.confirm(`确定删除「${f.name}」吗？`, "确认删除", { type: "warning" })
    files.value = files.value.filter((x) => x.id !== f.id)
    ElMessage.success("资料已删除")
  } catch { /* cancelled */ }
}

function handleUploadClick() {
  openCreate()
}

onMounted(() => {
  // simulate brief loading
  loading.value = true
  setTimeout(() => { loading.value = false }, 300)
})
</script>

<template>
  <div>
    <PageHeader title="知识库" :subtitle="`共 ${filteredFiles.length} 份资料`">
      <template #extra>
        <BaseButton type="primary" @click="handleUploadClick">
          <Upload :size="16" />
          上传资料
        </BaseButton>
      </template>
    </PageHeader>

    <!-- Tag Filter -->
    <div class="tag-bar">
      <button
        v-for="t in tags"
        :key="t.value"
        :class="['tag-btn', { 'tag-btn--active': filterTag === t.value }]"
        @click="filterTag = t.value"
      >
        {{ t.label }}
      </button>
      <BaseInput
        v-model="searchKeyword"
        placeholder="搜索资料名称..."
        :style="{ width: '220px', marginLeft: 'auto' }"
      />
    </div>

    <!-- Upload Zone -->
    <div class="drop-zone" @click="handleUploadClick">
      <UploadCloud :size="32" class="drop-zone__icon" />
      <div class="drop-zone__text">点击上传资料文件</div>
      <div class="drop-zone__hint">支持 PDF / Word / PPT / Markdown，可设置可见范围</div>
    </div>

    <!-- File Table -->
    <div class="data-table">
      <table>
        <thead>
          <tr>
            <th>资料名称</th>
            <th>标签</th>
            <th style="text-align:center;">可见范围</th>
            <th style="text-align:center;">大小</th>
            <th style="text-align:center;">下载次数</th>
            <th style="text-align:center;">上传时间</th>
            <th style="text-align:center;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="f in filteredFiles" :key="f.id" class="tr-hover">
            <td>
              <div class="file-name-cell">
                <component :is="fileIcon(f.name).icon" :size="16" :style="{ color: fileIcon(f.name).color }" />
                <span class="file-name">{{ f.name }}</span>
              </div>
            </td>
            <td>
              <span class="tag-badge" :style="{ background: tagMeta[f.tag]?.bg, color: f.tagColor }">
                {{ f.tagLabel }}
              </span>
            </td>
            <td style="text-align:center;">
              <div class="vis-tags">
                <span
                  v-for="v in visibilityArray(f.visibility)"
                  :key="v"
                  class="vis-tag"
                  :class="{ 'vis-public': v === '全院可见' }"
                >
                  <Eye v-if="v === '全院可见'" :size="10" />
                  {{ v }}
                </span>
              </div>
            </td>
            <td style="text-align:center;"><span class="mono-sm">{{ f.size }}</span></td>
            <td style="text-align:center;"><span class="mono-sm">{{ f.downloads }}</span></td>
            <td style="text-align:center;"><span class="mono-sm">{{ f.uploadedAt }}</span></td>
            <td style="text-align:center;">
              <div class="actions-cell">
                <BaseButton size="small" @click="openEdit(f)">编辑</BaseButton>
                <BaseButton size="small" type="danger" @click="handleDelete(f)">删除</BaseButton>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Edit / Upload Modal -->
    <BaseModal
      v-model="showModal"
      :title="isEdit ? '编辑资料' : '上传资料'"
      @confirm="handleSave"
    >
      <div class="form-group">
        <label class="form-label">资料名称</label>
        <BaseInput v-model="form.name" placeholder="输入资料名称" />
      </div>
      <div class="form-group">
        <label class="form-label">标签分类</label>
        <select v-model="form.tag" class="form-select">
          <option value="java">Java/Spring</option>
          <option value="frontend">前端开发</option>
          <option value="db">数据库</option>
          <option value="doc">文档规范</option>
        </select>
      </div>
      <div class="form-group">
        <label class="form-label">可见范围</label>
        <el-select v-model="form.visibility" multiple placeholder="选择可见范围" style="width: 100%">
          <el-option v-for="v in visibilityOptions" :key="v" :label="v" :value="v" />
        </el-select>
      </div>
    </BaseModal>
  </div>
</template>

<style scoped>
/* Tag bar */
.tag-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.tag-btn {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid var(--color-border, #e2e8f0);
  background: var(--color-card, #fff);
  color: var(--color-text-secondary, #64748b);
  cursor: pointer;
  transition: all 150ms;
}
.tag-btn:hover { border-color: #c7d2fe; }
.tag-btn--active {
  background: var(--color-primary, #3b82f6);
  color: #fff;
  border-color: var(--color-primary, #3b82f6);
}

/* Drop zone */
.drop-zone {
  border: 2px dashed #c7d2fe;
  border-radius: var(--radius-lg, 12px);
  padding: 24px;
  text-align: center;
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 200ms;
  background: var(--color-card, #fff);
}
.drop-zone:hover {
  border-color: var(--color-primary, #3b82f6);
  background: #eef2ff;
}
.drop-zone__icon { color: #c7d2fe; margin: 0 auto 8px; }
.drop-zone__text { font-size: 14px; color: var(--color-text-secondary, #64748b); margin-bottom: 4px; }
.drop-zone__hint { font-size: 12px; color: var(--color-text-placeholder, #94a3b8); }

/* Table */
.data-table {
  width: 100%;
  background: var(--color-card, #fff);
  border: 1px solid var(--color-border, #e2e8f0);
  border-radius: var(--radius-md, 8px);
  overflow: hidden;
}
.data-table table { width: 100%; border-collapse: collapse; }
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
.data-table td { padding: 11px 16px; font-size: 13px; border-bottom: 1px solid #f1f5f9; }
.data-table tbody tr:last-child td { border-bottom: none; }
.tr-hover:hover td { background: #f8fafc; }

.file-name-cell { display: flex; align-items: center; gap: 8px; }
.file-name { font-weight: 500; color: var(--color-text-primary, #1e293b); }

.tag-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.vis-tags { display: flex; gap: 4px; flex-wrap: wrap; justify-content: center; }
.vis-tag { font-size: 11px; padding: 1px 6px; border-radius: 4px; background: #f1f5f9; color: var(--color-text-secondary, #64748b); display: inline-flex; align-items: center; gap: 3px; white-space: nowrap; }
.vis-public { background: #d1fae5; color: #166534; }

.mono-sm { font-family: var(--font-mono); font-size: 12px; color: var(--color-text-secondary, #64748b); }

.actions-cell { display: flex; gap: 4px; justify-content: center; }

/* Form */
.form-group { margin-bottom: 16px; }
.form-label { display: block; font-size: 13px; font-weight: 500; color: var(--color-text-secondary, #64748b); margin-bottom: 5px; }
.form-select {
  width: 100%;
  padding: 8px 12px;
  font-size: 14px;
  border: 1px solid var(--color-border, #e2e8f0);
  border-radius: var(--radius-sm, 4px);
  background: var(--color-card, #fff);
  color: var(--color-text-primary, #1e293b);
  outline: none;
  box-sizing: border-box;
}
.form-select:focus { border-color: var(--color-primary, #3b82f6); }
</style>
