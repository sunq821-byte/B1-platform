<script setup lang="ts">
import { ref, computed, onMounted } from "vue"
import { ElMessage } from "element-plus"
import { useTeacherStore } from "@/stores/useTeacherStore"
import type { IStandardItem, IDimensionItem } from "@/types/teacher"
import PageHeader from "@/components/layout/PageHeader.vue"
import BaseButton from "@/components/base/BaseButton.vue"
import BaseInput from "@/components/base/BaseInput.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"

const store = useTeacherStore()
const loadError = ref("")
const editingDimensions = ref<IDimensionItem[]>([])
const saving = ref(false)

const editingStandardName = computed(() => store.currentDimensions?.standardName ?? "")

const weightTotal = computed(() => editingDimensions.value.reduce((s, d) => s + (Number(d.weight) || 0), 0))
const weightValid = computed(() => weightTotal.value === 100)

function openDimensions(std: IStandardItem) {
  store.fetchStandardDimensions(std.standardId).then(() => {
    editingDimensions.value = store.currentDimensions!.dimensions.map((d) => ({ ...d }))
  }).catch((e: unknown) => {
    ElMessage.error((e as Error)?.message || "加载维度配置失败")
  })
}

function closeEdit() {
  store.resetDimensions()
  editingDimensions.value = []
}

function addDimension() {
  editingDimensions.value.push({ name: "新维度", weight: 10 })
}

function updateDim(index: number, field: keyof IDimensionItem, value: string | number) {
  if (field === "weight") {
    editingDimensions.value[index].weight = Number(value) || 0
  } else {
    editingDimensions.value[index].name = String(value)
  }
}

function removeDim(index: number) {
  editingDimensions.value.splice(index, 1)
}

async function handleSaveDimensions() {
  if (!weightValid.value) {
    ElMessage.warning("权重合计需等于 100%")
    return
  }
  saving.value = true
  try {
    await store.saveDimensions(store.currentDimensions!.standardId, editingDimensions.value)
    ElMessage.success("维度配置保存成功")
    closeEdit()
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || "保存失败")
  } finally {
    saving.value = false
  }
}

async function initPage() {
  loadError.value = ""
  try {
    await store.fetchStandards()
  } catch (e: unknown) {
    loadError.value = (e as Error)?.message || "加载评价标准列表失败"
  }
}

onMounted(() => { initPage() })
</script>

<template>
  <div class="page">
    <PageHeader title="评价标准" subtitle="管理课程评价维度与评分规则" />

    <LoadingState v-if="store.standardsLoading" />
    <ErrorState v-else-if="loadError" :message="loadError" @retry="initPage" />

    <template v-else>
      <div class="data-table">
        <table>
          <thead>
            <tr>
              <th>标准名称</th>
              <th>课程类型</th>
              <th>维度数</th>
              <th>版本</th>
              <th>状态</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in store.standards" :key="s.standardId">
              <td>{{ s.name }}</td>
              <td>{{ s.courseType }}</td>
              <td>{{ s.dimensionCount }}</td>
              <td><span class="mono-sm">{{ s.version }}</span></td>
              <td>
                <span :class="['badge', s.status === 'published' ? 'badge--published' : 'badge--draft']">
                  {{ s.status === "published" ? "已发布" : "草稿" }}
                </span>
              </td>
              <td><span class="mono-sm">{{ s.updatedAt ? s.updatedAt.substring(0, 10) : "-" }}</span></td>
              <td>
                <BaseButton size="small" @click="openDimensions(s)">编辑维度</BaseButton>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Dimensions editor -->
      <div v-if="store.isEditingDimensions" class="dim-section">
        <div class="section-header">
          <h2 class="section-title">{{ editingStandardName }} - 维度配置</h2>
        </div>
        <div class="card">
          <div v-for="(dim, i) in editingDimensions" :key="i" class="dim-row">
            <span class="dim-num">#{{ i + 1 }}</span>
            <BaseInput :model-value="dim.name" @update:model-value="(v: string) => updateDim(i, 'name', v)" />
            <BaseInput
              :model-value="String(dim.weight)"
              type="number"
              class="dim-weight-input"
              @update:model-value="(v: string) => updateDim(i, 'weight', v)"
            />
            <span class="dim-unit">%</span>
            <BaseButton size="small" type="danger" @click="removeDim(i)">删除</BaseButton>
          </div>

          <div class="dim-footer">
            <div :class="['dim-total', weightValid ? 'dim-total--ok' : 'dim-total--err']">
              <span class="dim-total__label">权重合计：</span>
              <span class="dim-total__value">{{ weightTotal }}%</span>
              <span v-if="weightValid" class="dim-total__check">&#x2713;</span>
              <span v-else class="dim-total__hint">（需等于 100%）</span>
            </div>
          </div>

          <div class="dim-actions">
            <BaseButton @click="addDimension">+ 添加维度</BaseButton>
            <BaseButton type="primary" :loading="saving" :disabled="!weightValid" @click="handleSaveDimensions">
              保存配置
            </BaseButton>
            <BaseButton @click="closeEdit">取消</BaseButton>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.page {
  /* width/centering handled by AppLayout */
}

/* data-table */
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

.data-table tbody tr:last-child td { border-bottom: none; }
.data-table tbody tr:hover td { background: #f8fafc; }

.mono-sm {
  font-family: var(--font-mono);
  font-size: 12px;
}

/* badge */
.badge {
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
  white-space: nowrap;
}

.badge--published { background: #d1fae5; color: #047857; }
.badge--draft { background: #fef3c7; color: #b45309; }

/* dimensions section */
.dim-section {
  margin-top: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0;
  color: var(--color-text-primary, #1e293b);
}

.card {
  background: var(--color-card, #fff);
  border: 1px solid var(--color-border, #e2e8f0);
  border-radius: var(--radius-md, 8px);
  padding: 16px 20px;
}

.dim-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f1f5f9;
}

.dim-row > :nth-child(2) {
  flex: 1;
}

.dim-weight-input {
  width: 80px;
}

.dim-unit {
  font-size: 12px;
  color: var(--color-text-placeholder, #94a3b8);
}

.dim-num {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--color-text-placeholder, #94a3b8);
  min-width: 24px;
}

.dim-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  padding: 8px 0;
  font-size: 12px;
  margin-top: 4px;
}

.dim-total__label {
  color: var(--color-text-placeholder, #94a3b8);
}

.dim-total__value {
  font-family: var(--font-mono);
  font-weight: 600;
}

.dim-total--ok .dim-total__value { color: var(--color-success, #10b981); }
.dim-total--err .dim-total__value { color: var(--color-danger, #ef4444); }

.dim-total__check {
  color: var(--color-success, #10b981);
  font-size: 12px;
}

.dim-total__hint {
  color: var(--color-danger, #ef4444);
  font-size: 11px;
}

.dim-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}
</style>
