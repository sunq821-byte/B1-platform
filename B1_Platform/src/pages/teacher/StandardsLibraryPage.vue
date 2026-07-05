<script setup lang="ts">
import { onMounted, computed } from "vue"
import { ElMessage } from "element-plus"
import { useTeacherStore } from "@/stores/useTeacherStore"
import PageHeader from "@/components/layout/PageHeader.vue"

const store = useTeacherStore()

const subtitle = computed(() => `共 ${store.standardTemplates.length} 个模板`)

onMounted(() => {
  store.fetchStandardTemplates()
})

function handleCopy(name: string) {
  ElMessage.success(`模板「${name}」已复制到你的标准列表`)
}

function handlePreview() {
  ElMessage.info("预览功能将在后续版本开放")
}

function handleNewTemplate() {
  ElMessage.info("新建模板功能将在后续版本开放")
}
</script>

<template>
  <div>
    <PageHeader title="标准库" :subtitle="subtitle" />

    <div class="section">
      <div class="section-header">
        <h2 class="section-title">模板列表</h2>
        <button class="btn btn-outline btn-sm" @click="handleNewTemplate">+ 新建模板</button>
      </div>
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
              <td>{{ tpl.dims }}</td>
              <td><span style="font-family: var(--font-mono); font-size: 11px;">{{ tpl.version }}</span></td>
              <td>
                <span class="badge" :class="tpl.status === 'published' ? 'badge--success' : 'badge--default'">
                  {{ tpl.status === 'published' ? '已发布' : '草稿' }}
                </span>
              </td>
              <td>{{ tpl.updatedAt }}</td>
              <td style="display: flex; gap: 6px;">
                <button class="btn btn-ghost btn-sm" @click="handleCopy(tpl.name)">复制</button>
                <button class="btn btn-ghost btn-sm" @click="handlePreview">预览</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
