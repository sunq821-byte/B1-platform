<script setup lang="ts">
import { ref, onMounted } from "vue"
import { useAdminStore } from "@/stores/useAdminStore"
import PageHeader from "@/components/layout/PageHeader.vue"
import LoadingState from "@/components/common/LoadingState.vue"

const store = useAdminStore()
const filterType = ref("")

const typeIcons: Record<string, string> = { error: "❌", warning: "⚠️", success: "✅", info: "ℹ️" }
const typeLabels: Record<string, string> = { error: "错误", warning: "警告", success: "成功", info: "信息" }
const typeBadges: Record<string, string> = { error: "badge--danger", warning: "badge--warning", success: "badge--success", info: "badge--default" }

async function loadLogs() {
  await store.fetchLogs({ type: filterType.value || undefined })
}

onMounted(() => { loadLogs() })
</script>

<template>
  <div>
    <PageHeader title="操作日志" subtitle="系统操作审计记录" />

    <LoadingState v-if="store.logsLoading" text="加载日志..." />

    <template v-else>
    <div class="filter-bar">
      <select v-model="filterType" class="form-select" style="width:140px;" @change="loadLogs">
        <option value="">全部类型</option>
        <option value="info">信息</option>
        <option value="warning">警告</option>
        <option value="error">错误</option>
        <option value="success">成功</option>
      </select>
    </div>

    <div class="data-table">
      <table>
        <thead><tr><th>类型</th><th>操作内容</th><th>详情</th><th>操作人</th><th>时间</th></tr></thead>
        <tbody>
          <tr v-for="l in store.logs" :key="l.id">
            <td>
              <span :class="`badge ${typeBadges[l.type]}`" style="font-size:11px;">
                {{ typeIcons[l.type] }} {{ typeLabels[l.type] }}
              </span>
            </td>
            <td>{{ l.message }}</td>
            <td><span style="color:var(--color-text-secondary,#64748b);font-size:12px;">{{ l.detail }}</span></td>
            <td>{{ l.operator }}</td>
            <td><span style="font-family:var(--font-mono);font-size:11px;">{{ l.createdAt }}</span></td>
          </tr>
        </tbody>
      </table>
    </div>
    </template>
  </div>
</template>
