<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { FileText, Database, Upload, Eye } from "lucide-vue-next"
import PageHeader from "@/components/layout/PageHeader.vue"
import BaseButton from "@/components/base/BaseButton.vue"
import BaseInput from "@/components/base/BaseInput.vue"
import BaseModal from "@/components/base/BaseModal.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import LocalFilePreview from "@/components/business/LocalFilePreview.vue"
import RemoteFilePreview from "@/components/business/RemoteFilePreview.vue"
import { downloadFile } from "@/api/download"
import request from "@/api/request"
import {
  KNOWLEDGE_UPLOAD_ACCEPT,
  isKnowledgeUploadFile,
} from "@/utils/filePreview"
import {
  fetchCourses,
  fetchKnowledgeDocuments,
  fetchKnowledgeVisibilityOptions,
  createKnowledgeDocument,
  updateKnowledgeDocument,
  deleteKnowledgeDocument,
} from "@/api/modules/teacher"
import type {
  ICourseItem,
  IKnowledgeDocument,
  IKnowledgeVisibilityOption,
} from "@/types/teacher"

import {
  KNOWLEDGE_DEFAULT_TAG,
  KNOWLEDGE_MATERIAL_TYPE_TAGS,
  KNOWLEDGE_TAG_FILTER_OPTIONS,
  buildKnowledgeTagMeta,
} from "@/constants/knowledgeTags"

const tags = KNOWLEDGE_TAG_FILTER_OPTIONS
const tagMeta = buildKnowledgeTagMeta()
const materialTypeTags = KNOWLEDGE_MATERIAL_TYPE_TAGS

const courses = ref<ICourseItem[]>([])
const courseFilter = ref("all")
const filterTag = ref("all")
const searchKeyword = ref("")
const loading = ref(false)
const documents = ref<IKnowledgeDocument[]>([])
const total = ref(0)
let loadSeq = 0
let searchTimer: ReturnType<typeof setTimeout> | undefined

const showModal = ref(false)
const showPreviewModal = ref(false)
const previewDoc = ref<IKnowledgeDocument | null>(null)
const isEdit = ref(false)
const editingId = ref("")
const visibilityOptions = ref<IKnowledgeVisibilityOption[]>([])
const selectedFile = ref<File | null>(null)

const form = ref({
  courseId: "",
  title: "",
  tag: KNOWLEDGE_DEFAULT_TAG as string,
  visibilityScope: "COURSE" as "COURSE" | "CLASS",
  visibilityClassIds: [] as string[],
  fileName: "",
  fileSize: "",
})

const filteredSubtitle = computed(() => {
  const courseText = courseFilter.value === "all" ? "全部课程" : "当前课程"
  return `共 ${total.value} 份资料 · ${courseText}`
})

function fileIcon(name?: string) {
  const n = (name ?? "").toLowerCase()
  if (n.endsWith(".pdf")) return { icon: FileText, color: "#FF4D4F" }
  if (n.endsWith(".docx")) return { icon: FileText, color: "#1677FF" }
  if (n.endsWith(".md") || n.endsWith(".markdown")) return { icon: FileText, color: "#00B96B" }
  return { icon: Database, color: "#64748b" }
}

function resolveDocDownloadUrl(doc: IKnowledgeDocument): string {
  return doc.downloadUrl || `/api/v1/files/${doc.fileStorageId}/download`
}

function resolveDocPreviewUrl(doc: IKnowledgeDocument): string | undefined {
  return doc.previewUrl || resolveDocDownloadUrl(doc).replace("/download", "/preview")
}

function openPreview(doc: IKnowledgeDocument) {
  previewDoc.value = doc
  showPreviewModal.value = true
}

async function handleDownload(doc: IKnowledgeDocument) {
  try {
    await downloadFile(resolveDocDownloadUrl(doc), undefined, doc.fileName || doc.title)
  } catch { /* downloadFile 已提示 */ }
}

function extractPageList<T>(data: unknown): { list: T[]; total: number } {
  if (!data) return { list: [], total: 0 }
  if (Array.isArray(data)) return { list: data as T[], total: data.length }
  const page = data as { list?: T[]; total?: number }
  const list = page.list ?? []
  return { list, total: page.total ?? list.length }
}

function formatDate(value?: string) {
  if (!value) return "-"
  return value.length >= 10 ? value.substring(0, 10) : value
}

async function loadCourses() {
  const res = await fetchCourses()
  courses.value = Array.isArray(res) ? res : (res as { list?: ICourseItem[] }).list ?? []
}

async function loadDocuments() {
  const seq = ++loadSeq
  loading.value = true
  try {
    const res = await fetchKnowledgeDocuments({
      courseId: courseFilter.value === "all" ? undefined : courseFilter.value,
      tag: filterTag.value === "all" ? undefined : filterTag.value,
      keyword: searchKeyword.value.trim() || undefined,
      page: 1,
      pageSize: 100,
    })
    if (seq !== loadSeq) return
    const page = extractPageList<IKnowledgeDocument>(res)
    documents.value = page.list
    total.value = page.total
  } catch {
    if (seq !== loadSeq) return
    documents.value = []
    total.value = 0
  } finally {
    if (seq === loadSeq) loading.value = false
  }
}

async function loadVisibilityOptions(courseId: string) {
  if (!courseId) {
    visibilityOptions.value = []
    return
  }
  visibilityOptions.value = await fetchKnowledgeVisibilityOptions(courseId)
}

function resetForm() {
  form.value = {
    courseId: courses.value[0]?.courseId ?? "",
    title: "",
    tag: KNOWLEDGE_DEFAULT_TAG,
    visibilityScope: "COURSE",
    visibilityClassIds: [],
    fileName: "",
    fileSize: "",
  }
  selectedFile.value = null
}

function openCreate() {
  isEdit.value = false
  editingId.value = ""
  resetForm()
  if (form.value.courseId) {
    loadVisibilityOptions(form.value.courseId)
  }
  showModal.value = true
}

function openEdit(doc: IKnowledgeDocument) {
  isEdit.value = true
  editingId.value = doc.id
  selectedFile.value = null
  form.value = {
    courseId: doc.courseId,
    title: doc.title,
    tag: doc.tag,
    visibilityScope: doc.visibilityScope,
    visibilityClassIds: doc.visibilityClassIds?.map(String) ?? [],
    fileName: doc.fileName,
    fileSize: doc.fileSizeDisplay,
  }
  loadVisibilityOptions(doc.courseId)
  showModal.value = true
}

function onFilePicked(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!isKnowledgeUploadFile(file)) {
    ElMessage.warning("仅支持 PDF、Markdown（.md）和 Word（.docx）")
    input.value = ""
    return
  }
  selectedFile.value = file
  form.value.fileName = file.name
  form.value.fileSize = `${(file.size / (1024 * 1024)).toFixed(1)} MB`
  if (!form.value.title.trim()) form.value.title = file.name.replace(/\.[^.]+$/, "")
}

function clearSelectedFile() {
  selectedFile.value = null
  form.value.fileName = ""
  form.value.fileSize = ""
}

async function uploadSelectedFile(): Promise<string | undefined> {
  if (!selectedFile.value) return undefined
  const formData = new FormData()
  formData.append("file", selectedFile.value)
  const res = await request.post("/api/v1/files/upload?bucket=knowledge", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  }) as { fileId: string | number }
  return String(res.fileId)
}

async function handleSave() {
  if (!form.value.courseId) {
    ElMessage.warning("请选择所属课程")
    return
  }
  if (!form.value.title.trim()) {
    ElMessage.warning("请输入资料名称")
    return
  }
  if (!isEdit.value && !selectedFile.value) {
    ElMessage.warning("请选择要上传的文件")
    return
  }

  const payload = {
    courseId: form.value.courseId,
    title: form.value.title.trim(),
    tag: form.value.tag,
    visibilityScope: form.value.visibilityScope,
    visibilityClassIds:
      form.value.visibilityScope === "CLASS"
        ? form.value.visibilityClassIds.map(Number)
        : [],
  }

  if (form.value.visibilityScope === "CLASS" && payload.visibilityClassIds.length === 0) {
    ElMessage.warning("请选择可见班级")
    return
  }

  try {
    if (isEdit.value) {
      const fileStorageId = await uploadSelectedFile()
      await updateKnowledgeDocument(editingId.value, {
        ...payload,
        fileStorageId,
      })
      ElMessage.success("资料已更新")
    } else {
      const fileStorageId = await uploadSelectedFile()
      if (!fileStorageId) {
        ElMessage.warning("文件上传失败")
        return
      }
      await createKnowledgeDocument({ ...payload, fileStorageId })
      ElMessage.success("资料已上传")
    }
    showModal.value = false
    await loadDocuments()
  } catch {
    ElMessage.error("保存失败，请稍后重试")
  }
}

async function handleDelete(doc: IKnowledgeDocument) {
  try {
    await ElMessageBox.confirm(`确定删除「${doc.title}」吗？`, "确认删除", { type: "warning" })
    await deleteKnowledgeDocument(doc.id)
    ElMessage.success("资料已删除")
    await loadDocuments()
  } catch {
    /* cancelled or failed */
  }
}

watch(
  () => form.value.courseId,
  (courseId) => {
    if (courseId) loadVisibilityOptions(courseId)
  },
)

watch([courseFilter, filterTag], () => {
  loadDocuments()
})

watch(searchKeyword, () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => loadDocuments(), 300)
})

onMounted(async () => {
  try {
    await loadCourses()
  } catch {
    courses.value = []
  }
  await loadDocuments()
})
</script>

<template>
  <div>
    <PageHeader title="知识库" :subtitle="filteredSubtitle">
      <template #extra>
        <button type="button" class="btn btn-primary" @click="openCreate">
          <Upload :size="16" />
          上传资料
        </button>
      </template>
    </PageHeader>

    <LoadingState v-if="loading" text="加载知识库..." />

    <template v-else>
      <div class="filter-bar">
        <select v-model="courseFilter" class="form-select w-course">
          <option value="all">全部课程</option>
          <option v-for="c in courses" :key="c.courseId" :value="c.courseId">
            {{ c.courseName }}
          </option>
        </select>
        <select v-model="filterTag" class="form-select w-tag">
          <option v-for="t in tags" :key="t.value" :value="t.value">{{ t.label }}</option>
        </select>
        <input
          v-model="searchKeyword"
          class="form-input w-search"
          placeholder="搜索资料名称..."
        />
      </div>

      <div class="data-table">
        <table>
          <thead>
            <tr>
              <th>资料名称</th>
              <th>所属课程</th>
              <th>资料类型</th>
              <th style="text-align:center;">可见范围</th>
              <th style="text-align:center;">大小</th>
              <th style="text-align:center;">下载次数</th>
              <th style="text-align:center;">上传时间</th>
              <th style="text-align:center;">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="doc in documents" :key="doc.id" class="tr-hover">
              <td>
                <div class="file-name-cell">
                  <component
                    :is="fileIcon(doc.fileName).icon"
                    :size="16"
                    :style="{ color: fileIcon(doc.fileName).color }"
                  />
                  <button type="button" class="file-name file-name--link" @click="openPreview(doc)">
                    {{ doc.title }}
                  </button>
                </div>
              </td>
              <td><span class="course-name">{{ doc.courseName }}</span></td>
              <td>
                <span
                  class="tag-badge"
                  :style="{ background: tagMeta[doc.tag]?.bg, color: doc.tagColor }"
                >
                  {{ doc.tagLabel }}
                </span>
              </td>
              <td style="text-align:center;">
                <div class="vis-tags">
                  <span
                    v-for="label in doc.visibilityLabels"
                    :key="label"
                    class="vis-tag"
                    :class="{ 'vis-public': label === '本课程全体' }"
                  >
                    <Eye v-if="label === '本课程全体'" :size="10" />
                    {{ label }}
                  </span>
                </div>
              </td>
              <td style="text-align:center;"><span class="mono-sm">{{ doc.fileSizeDisplay }}</span></td>
              <td style="text-align:center;"><span class="mono-sm">{{ doc.downloadCount }}</span></td>
              <td style="text-align:center;">
                <span class="mono-sm">{{ formatDate(doc.uploadedAt) }}</span>
              </td>
              <td style="text-align:center;">
                <div class="actions-cell">
                  <BaseButton size="small" @click="openPreview(doc)">查看</BaseButton>
                  <BaseButton size="small" @click="openEdit(doc)">编辑</BaseButton>
                  <BaseButton size="small" type="danger" @click="handleDelete(doc)">删除</BaseButton>
                </div>
              </td>
            </tr>
            <tr v-if="!documents.length">
              <td colspan="8" class="empty">暂无资料，点击右上角上传</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <BaseModal
      v-model="showModal"
      :title="isEdit ? '编辑资料' : '上传资料'"
      @confirm="handleSave"
    >
      <div class="form-group">
        <label class="form-label">所属课程 *</label>
        <select v-model="form.courseId" class="form-select" :disabled="isEdit">
          <option value="">请选择课程</option>
          <option v-for="c in courses" :key="c.courseId" :value="c.courseId">
            {{ c.courseName }}
          </option>
        </select>
      </div>
      <div v-if="!isEdit" class="form-group">
        <label class="form-label">选择文件 *</label>
        <input
          type="file"
          :accept="KNOWLEDGE_UPLOAD_ACCEPT"
          @change="onFilePicked"
        />
        <p class="file-hint">支持 PDF、Markdown（.md）、Word（.docx）</p>
        <LocalFilePreview
          v-if="selectedFile"
          :file="selectedFile"
          :editable="true"
          class="upload-preview"
          @replace="clearSelectedFile"
          @remove="clearSelectedFile"
        />
      </div>
      <div v-if="isEdit" class="form-group">
        <label class="form-label">替换文件（可选）</label>
        <input
          type="file"
          :accept="KNOWLEDGE_UPLOAD_ACCEPT"
          @change="onFilePicked"
        />
        <p class="file-hint">支持 PDF、Markdown（.md）、Word（.docx）</p>
        <LocalFilePreview
          v-if="selectedFile"
          :file="selectedFile"
          :editable="true"
          class="upload-preview"
          @replace="clearSelectedFile"
          @remove="clearSelectedFile"
        />
      </div>
      <div class="form-group">
        <label class="form-label">资料名称 *</label>
        <BaseInput v-model="form.title" placeholder="输入资料名称" />
      </div>
      <div class="form-group">
        <label class="form-label">资料类型</label>
        <select v-model="form.tag" class="form-select">
          <option v-for="t in materialTypeTags" :key="t.value" :value="t.value">
            {{ t.label }}
          </option>
        </select>
      </div>
      <div class="form-group">
        <label class="form-label">可见范围</label>
        <select v-model="form.visibilityScope" class="form-select">
          <option value="COURSE">本课程全体</option>
          <option value="CLASS">指定班级</option>
        </select>
      </div>
      <div v-if="form.visibilityScope === 'CLASS'" class="form-group">
        <label class="form-label">选择班级</label>
        <el-select v-model="form.visibilityClassIds" multiple placeholder="选择班级" style="width: 100%">
          <el-option
            v-for="opt in visibilityOptions.filter((o) => o.type === 'class')"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </div>
    </BaseModal>

    <el-dialog
      v-model="showPreviewModal"
      :title="previewDoc?.title || '资料预览'"
      width="860px"
      destroy-on-close
      class="preview-dialog"
    >
      <RemoteFilePreview
        v-if="previewDoc"
        :download-url="resolveDocDownloadUrl(previewDoc)"
        :preview-url="resolveDocPreviewUrl(previewDoc)"
        :file-name="previewDoc.fileName || previewDoc.title"
        :file-type="previewDoc.fileType"
      />
      <template #footer>
        <div class="preview-dialog__footer">
          <BaseButton @click="showPreviewModal = false">关闭</BaseButton>
          <BaseButton v-if="previewDoc" type="primary" @click="handleDownload(previewDoc)">下载</BaseButton>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.w-course { width: 200px; }
.w-tag { width: 140px; }
.w-search { width: 220px; }
.file-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--color-text-secondary);
}
.empty {
  text-align: center;
  padding: 28px 0;
  color: var(--color-text-placeholder);
}
.course-name {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.data-table {
  width: 100%;
  background: var(--color-card, #fff);
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: var(--radius-xl, 16px);
  box-shadow: var(--shadow-card);
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
.file-name {
  font-weight: 500;
  color: var(--color-text-primary, #1e293b);
}
.file-name--link {
  padding: 0;
  border: none;
  background: none;
  font: inherit;
  text-align: left;
  cursor: pointer;
}
.file-name--link:hover {
  color: var(--color-primary, #1677FF);
  text-decoration: underline;
}

.upload-preview {
  margin-top: 12px;
  min-height: 280px;
  max-height: 360px;
}

.preview-dialog__footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

:deep(.preview-dialog .el-dialog__body) {
  padding-top: 8px;
}

.tag-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.vis-tags { display: flex; gap: 4px; flex-wrap: wrap; justify-content: center; }
.vis-tag {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
  background: #f1f5f9;
  color: var(--color-text-secondary, #64748b);
  display: inline-flex;
  align-items: center;
  gap: 3px;
  white-space: nowrap;
}
.vis-public { background: #E6F7EF; color: #009A57; }

.mono-sm { font-family: var(--font-mono); font-size: 12px; color: var(--color-text-secondary, #64748b); }
.actions-cell { display: flex; gap: 4px; justify-content: center; }

.form-group { margin-bottom: 16px; }
.form-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary, #64748b);
  margin-bottom: 5px;
}
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
.form-select:focus { border-color: var(--color-primary, #1677FF); }
</style>
