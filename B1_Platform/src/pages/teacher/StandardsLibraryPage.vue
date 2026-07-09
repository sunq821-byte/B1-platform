<script setup lang="ts">
import { ref, onMounted, computed, reactive } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { useTeacherStore } from "@/stores/useTeacherStore"
import type { IStandardTemplate } from "@/types/teacher"
import PageHeader from "@/components/layout/PageHeader.vue"
import BaseButton from "@/components/base/BaseButton.vue"
import BaseInput from "@/components/base/BaseInput.vue"
import BaseModal from "@/components/base/BaseModal.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"
import EmptyState from "@/components/common/EmptyState.vue"

const store = useTeacherStore()
const loadError = ref("")
const copyingId = ref("")
const saving = ref(false)

const subtitle = computed(() => `共 ${store.standardTemplates.length} 个模板`)

// Preview state
const previewVisible = ref(false)
const previewTemplate = ref<IStandardTemplate | null>(null)
const previewLoading = ref(false)
const previewDimensions = ref<Array<{ dimName: string; weight: number; maxScore: number }>>([])

// New template state
const newVisible = ref(false)
const newForm = reactive({
  standardName: "",
  description: "",
  courseType: "通用",
})
const newDimensionName = ref("")
const newDimensionWeight = ref(25)
const newDimensionMaxScore = ref(100)
const newDimensions = ref<Array<{ dimName: string; weight: number; maxScore: number; sortOrder: number }>>([])

// Edit dimensions in "standards" list
const editDimVisible = ref(false)
const editingStandardId = ref("")
const editingDimName = ref("")
const editingDimWeight = ref(25)
const editingDimMaxScore = ref(100)
const editingDimensions = ref<Array<{ dimName: string; weight: number; maxScore: number; sortOrder: number }>>([])
const editingDimLoading = ref(false)

async function initPage() {
  loadError.value = ""
  try {
    await store.fetchStandardTemplates()
  } catch (e: unknown) {
    loadError.value = (e as Error)?.message || "加载标准库失败"
  }
}

async function handleCopy(tpl: IStandardTemplate) {
  try {
    await ElMessageBox.confirm(
      `确定将模板「${tpl.name}」复制到你的评价标准列表吗？`,
      "复制模板",
      { confirmButtonText: "确定", cancelButtonText: "取消", type: "info" },
    )
    copyingId.value = tpl.id
    const result = await store.copyStandardTemplate(tpl.id)
    ElMessage.success(`模板「${tpl.name}」已复制到你的标准列表`)
  } catch {
    // cancelled or error
  } finally {
    copyingId.value = ""
  }
}

async function handlePreview(tpl: IStandardTemplate) {
  previewTemplate.value = tpl
  previewVisible.value = true
  previewLoading.value = true
  previewDimensions.value = []
  try {
    const detail = await store.fetchStandardDimensions(tpl.id) as unknown as { dimensions?: Array<{ dimName: string; weight: number; maxScore: number }> }
    previewDimensions.value = detail?.dimensions ?? []
  } catch {
    previewDimensions.value = []
  } finally {
    previewLoading.value = false
  }
}

function handleNewTemplate() {
  newForm.standardName = ""
  newForm.description = ""
  newForm.courseType = "通用"
  newDimensions.value = []
  newVisible.value = true
}

function addNewDimension() {
  const name = newDimensionName.value.trim()
  if (!name) { ElMessage.warning("请输入维度名称"); return }
  newDimensions.value.push({
    dimName: name,
    weight: newDimensionWeight.value,
    maxScore: newDimensionMaxScore.value,
    sortOrder: newDimensions.value.length + 1,
  })
  newDimensionName.value = ""
  newDimensionWeight.value = 25
  newDimensionMaxScore.value = 100
}

function removeNewDimension(idx: number) {
  newDimensions.value.splice(idx, 1)
}

async function handleCreateTemplate() {
  if (!newForm.standardName.trim()) { ElMessage.warning("请输入模板名称"); return }
  if (newDimensions.value.length === 0) { ElMessage.warning("请至少添加一个评价维度"); return }
  saving.value = true
  try {
    await store.createStandardTemplate({
      ...newForm,
      dimensions: newDimensions.value,
    })
    ElMessage.success("模板已创建")
    newVisible.value = false
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || "创建失败")
  } finally { saving.value = false }
}

async function handleLoadDimensions(standardId: string) {
  editingStandardId.value = standardId
  editingDimensions.value = []
  editingDimLoading.value = true
  editDimVisible.value = true
  try {
    const detail = await store.fetchStandardDimensions(standardId) as unknown as { dimensions?: Array<{ dimName: string; weight: number; maxScore: number; sortOrder: number }> }
    editingDimensions.value = (detail?.dimensions ?? []).map((d, i) => ({ ...d, sortOrder: d.sortOrder ?? i + 1 }))
  } catch {
    editingDimensions.value = []
  } finally {
    editingDimLoading.value = false
  }
}

function addEditDimension() {
  const name = editingDimName.value.trim()
  if (!name) { ElMessage.warning("请输入维度名称"); return }
  editingDimensions.value.push({
    dimName: name,
    weight: editingDimWeight.value,
    maxScore: editingDimMaxScore.value,
    sortOrder: editingDimensions.value.length + 1,
  })
  editingDimName.value = ""
  editingDimWeight.value = 25
  editingDimMaxScore.value = 100
}

function removeEditDimension(idx: number) {
  editingDimensions.value.splice(idx, 1)
}

async function handleSaveDimensions() {
  saving.value = true
  try {
    await store.saveDimensions(editingStandardId.value, editingDimensions.value.map((d, i) => ({
      ...d,
      sortOrder: i + 1,
    })) as unknown as Parameters<typeof store.saveDimensions>[1])
    ElMessage.success("维度已更新")
    editDimVisible.value = false
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || "保存失败")
  } finally { saving.value = false }
}

onMounted(() => { initPage() })
</script>

<template>
  <div>
    <PageHeader title="标准库" :subtitle="subtitle">
      <template #extra>
        <BaseButton type="primary" @click="handleNewTemplate">+ 新建模板</BaseButton>
      </template>
    </PageHeader>

    <LoadingState v-if="store.templatesLoading" text="加载标准模板..." />
    <ErrorState v-else-if="loadError" :message="loadError" @retry="initPage" />
    <EmptyState
      v-else-if="store.standardTemplates.length === 0"
      description="暂无标准模板，点击右上角创建"
    />

    <template v-else>
      <div class="data-table">
        <table>
          <thead>
            <tr>
              <th>模板名称</th>
              <th>适用类型</th>
              <th>维度数</th>
              <th>版本</th>
              <th>状态</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="tpl in store.standardTemplates" :key="tpl.id">
              <td>{{ tpl.name }}</td>
              <td>{{ tpl.type }}</td>
              <td>
                <span class="dim-count-link" @click="handleLoadDimensions(tpl.id)">{{ tpl.dims }}</span>
              </td>
              <td><span class="mono-sm">{{ tpl.version }}</span></td>
              <td>
                <span
                  class="badge"
                  :class="tpl.status === 'published' ? 'badge--success' : 'badge--default'"
                >
                  {{ tpl.status === "published" ? "已发布" : "草稿" }}
                </span>
              </td>
              <td><span class="mono-sm">{{ tpl.updatedAt }}</span></td>
              <td class="actions-cell">
                <BaseButton size="small" :loading="copyingId === tpl.id" @click="handleCopy(tpl)">
                  复制
                </BaseButton>
                <BaseButton size="small" @click="handlePreview(tpl)">预览</BaseButton>
                <BaseButton size="small" @click="handleLoadDimensions(tpl.id)">维度</BaseButton>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- Preview Modal -->
    <BaseModal v-model="previewVisible" title="模板预览" :show-footer="false">
      <div v-if="previewLoading" class="dim-loading">加载中...</div>
      <template v-else-if="previewTemplate">
        <div class="preview-header">
          <div class="preview-row"><span class="preview-label">名称</span><span>{{ previewTemplate.name }}</span></div>
          <div class="preview-row"><span class="preview-label">类型</span><span>{{ previewTemplate.type }}</span></div>
          <div class="preview-row"><span class="preview-label">版本</span><span>{{ previewTemplate.version }}</span></div>
        </div>
        <div class="dim-section">
          <div class="dim-section-title">评价维度 ({{ previewDimensions.length }})</div>
          <table class="dim-table" v-if="previewDimensions.length > 0">
            <thead>
              <tr><th>#</th><th>维度名称</th><th>权重</th><th>满分</th></tr>
            </thead>
            <tbody>
              <tr v-for="(d, i) in previewDimensions" :key="i">
                <td>{{ i + 1 }}</td>
                <td>{{ d.dimName }}</td>
                <td>{{ d.weight }}%</td>
                <td>{{ d.maxScore }}</td>
              </tr>
            </tbody>
          </table>
          <div v-else class="dim-empty">暂无维度信息</div>
        </div>
      </template>
    </BaseModal>

    <!-- New Template Modal -->
    <BaseModal v-model="newVisible" title="新建模板" :loading="saving" @confirm="handleCreateTemplate">
      <div class="form-group">
        <label class="form-label">模板名称</label>
        <BaseInput v-model="newForm.standardName" placeholder="如 Java实训评价标准" />
      </div>
      <div class="form-group">
        <label class="form-label">适用类型</label>
        <select v-model="newForm.courseType" class="form-select">
          <option value="通用">通用</option>
          <option value="Java实训">Java实训</option>
          <option value="前端实训">前端实训</option>
          <option value="数据库实训">数据库实训</option>
          <option value="软件工程">软件工程</option>
        </select>
      </div>
      <div class="form-group">
        <label class="form-label">描述</label>
        <textarea v-model="newForm.description" class="form-textarea" rows="2" placeholder="模板描述（可选）" />
      </div>
      <div class="dim-section">
        <div class="dim-section-title">评价维度</div>
        <div class="dim-list" v-if="newDimensions.length > 0">
          <div v-for="(d, i) in newDimensions" :key="i" class="dim-row">
            <span class="dim-idx">{{ i + 1 }}</span>
            <span class="dim-name-text">{{ d.dimName }}</span>
            <span class="dim-meta">权重 {{ d.weight }}% / 满分 {{ d.maxScore }}分</span>
            <BaseButton size="small" type="danger" @click="removeNewDimension(i)">移除</BaseButton>
          </div>
        </div>
        <div class="dim-add-row">
          <BaseInput v-model="newDimensionName" placeholder="维度名称" :style="{ width: '140px' }" />
          <BaseInput :model-value="String(newDimensionWeight)" @update:model-value="(v: string) => newDimensionWeight = Number(v) || 0" type="number" placeholder="权重%" :style="{ width: '80px' }" />
          <BaseInput :model-value="String(newDimensionMaxScore)" @update:model-value="(v: string) => newDimensionMaxScore = Number(v) || 0" type="number" placeholder="满分" :style="{ width: '80px' }" />
          <BaseButton size="small" @click="addNewDimension">添加</BaseButton>
        </div>
      </div>
    </BaseModal>

    <!-- Edit Dimensions Modal -->
    <BaseModal v-model="editDimVisible" title="编辑评价维度" :loading="saving" @confirm="handleSaveDimensions">
      <div v-if="editingDimLoading" class="dim-loading">加载中...</div>
      <template v-else>
        <div class="dim-list" v-if="editingDimensions.length > 0">
          <div v-for="(d, i) in editingDimensions" :key="i" class="dim-row">
            <span class="dim-idx">{{ i + 1 }}</span>
            <span class="dim-name-text">{{ d.dimName }}</span>
            <span class="dim-meta">权重 {{ d.weight }}% / 满分 {{ d.maxScore }}分</span>
            <BaseButton size="small" type="danger" @click="removeEditDimension(i)">移除</BaseButton>
          </div>
        </div>
        <div class="dim-add-row">
          <BaseInput v-model="editingDimName" placeholder="维度名称" :style="{ width: '140px' }" />
          <BaseInput :model-value="String(editingDimWeight)" @update:model-value="(v: string) => editingDimWeight = Number(v) || 0" type="number" placeholder="权重%" :style="{ width: '80px' }" />
          <BaseInput :model-value="String(editingDimMaxScore)" @update:model-value="(v: string) => editingDimMaxScore = Number(v) || 0" type="number" placeholder="满分" :style="{ width: '80px' }" />
          <BaseButton size="small" @click="addEditDimension">添加</BaseButton>
        </div>
      </template>
    </BaseModal>
  </div>
</template>

<style scoped>
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
.data-table td { padding: 11px 16px; font-size: 14px; border-bottom: 1px solid #f1f5f9; }
.data-table tbody tr:last-child td { border-bottom: none; }
.data-table tbody tr:hover td { background: #f8fafc; }

.mono-sm { font-family: var(--font-mono); font-size: 12px; }

.badge {
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}
.badge--success { background: #d1fae5; color: #047857; }
.badge--default { background: #f1f5f9; color: #475569; }

.actions-cell { display: flex; gap: 6px; }

.dim-count-link { color: var(--color-primary, #3b82f6); cursor: pointer; text-decoration: underline; }

/* Preview */
.preview-header { margin-bottom: 16px; }
.preview-row { display: flex; gap: 12px; padding: 6px 0; font-size: 14px; }
.preview-label { color: var(--color-text-secondary, #64748b); width: 60px; flex-shrink: 0; }

/* Dimensions */
.dim-section { margin-top: 16px; }
.dim-section-title { font-size: 13px; font-weight: 600; color: var(--color-text-primary, #1e293b); margin-bottom: 10px; }
.dim-loading { text-align: center; padding: 20px; font-size: 13px; color: var(--color-text-placeholder, #94a3b8); }
.dim-empty { text-align: center; padding: 16px; font-size: 13px; color: var(--color-text-placeholder, #94a3b8); }
.dim-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.dim-table th { text-align: left; padding: 6px 10px; font-size: 11px; color: var(--color-text-placeholder, #94a3b8); border-bottom: 1px solid #e2e8f0; }
.dim-table td { padding: 7px 10px; border-bottom: 1px solid #f1f5f9; }

.dim-list { margin-bottom: 12px; }
.dim-row { display: flex; align-items: center; gap: 8px; padding: 8px 10px; background: #f8fafc; border-radius: 6px; margin-bottom: 6px; font-size: 13px; }
.dim-idx { color: var(--color-text-placeholder, #94a3b8); font-family: var(--font-mono); font-size: 12px; min-width: 20px; }
.dim-name-text { flex: 1; font-weight: 500; }
.dim-meta { color: var(--color-text-secondary, #64748b); font-size: 12px; }

.dim-add-row { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }

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
.form-textarea {
  width: 100%;
  padding: 8px 12px;
  font-size: 14px;
  border: 1px solid var(--color-border, #e2e8f0);
  border-radius: var(--radius-sm, 4px);
  background: var(--color-card, #fff);
  color: var(--color-text-primary, #1e293b);
  resize: vertical;
  outline: none;
  font-family: inherit;
  box-sizing: border-box;
}
.form-textarea:focus { border-color: var(--color-primary, #3b82f6); }
</style>
