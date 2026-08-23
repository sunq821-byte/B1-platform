<script setup lang="ts">
import { ref, computed, onMounted } from "vue"
import { useAdminStore } from "@/stores/useAdminStore"
import PageHeader from "@/components/layout/PageHeader.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"

const store = useAdminStore()
const filterType = ref("")
const searchKeyword = ref("")
const loadError = ref("")

const typeLabels: Record<string, string> = {
  error: "错误",
  warning: "警告",
  success: "成功",
  info: "信息",
}
const typeBadges: Record<string, string> = {
  error: "badge--danger",
  warning: "badge--warning",
  success: "badge--success",
  info: "badge--info",
}

const filteredLogs = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase()
  if (!kw) return store.logs
  return store.logs.filter((l) =>
    (l.message || "").toLowerCase().includes(kw)
    || (l.detail || "").toLowerCase().includes(kw)
    || (l.operator || "").toLowerCase().includes(kw)
    || (l.ip || "").toLowerCase().includes(kw),
  )
})

async function loadLogs() {
  loadError.value = ""
  try {
    await store.fetchLogs({ type: filterType.value || undefined })
  } catch (e: unknown) {
    loadError.value = (e as Error)?.message || "加载日志失败"
  }
}

onMounted(() => { loadLogs() })
</script>

<template>
  <div>
    <PageHeader title="操作日志" subtitle="系统关键操作审计 · 可按类型筛选" />

    <LoadingState v-if="store.logsLoading" text="加载日志..." />
    <ErrorState v-else-if="loadError" :message="loadError" @retry="loadLogs" />

    <template v-else>
      <div class="filter-bar">
        <select v-model="filterType" class="form-select w-type" @change="loadLogs">
          <option value="">全部类型</option>
          <option value="info">信息</option>
          <option value="warning">警告</option>
          <option value="error">错误</option>
          <option value="success">成功</option>
        </select>
        <input
          v-model="searchKeyword"
          class="form-input w-search"
          placeholder="搜索操作 / 用户..."
        />
      </div>

      <div class="data-table">
        <table>
          <thead>
            <tr>
              <th>时间</th>
              <th>用户</th>
              <th>操作</th>
              <th>IP</th>
              <th>结果</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="l in filteredLogs" :key="l.id">
              <td><span class="mono">{{ l.createdAt }}</span></td>
              <td>{{ l.operator || "-" }}</td>
              <td>
                <div class="op">{{ l.message }}</div>
                <div v-if="l.detail" class="op-detail">{{ l.detail }}</div>
              </td>
              <td><span class="mono">{{ l.ip || "-" }}</span></td>
              <td>
                <span :class="`badge ${typeBadges[l.type] || 'badge--default'}`">
                  {{ l.result || typeLabels[l.type] || l.type }}
                </span>
              </td>
            </tr>
            <tr v-if="!filteredLogs.length">
              <td colspan="5" class="empty">暂无日志</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>

<style scoped>
.w-type { width: 160px; }
.w-search { width: 240px; }
.mono {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--color-text-secondary);
}
.op {
  font-size: 13px;
  font-weight: 550;
  color: var(--color-text-primary);
}
.op-detail {
  margin-top: 2px;
  font-size: 12px;
  color: var(--color-text-placeholder);
}
.empty {
  text-align: center;
  padding: 28px 0;
  color: var(--color-text-placeholder);
}
</style>
