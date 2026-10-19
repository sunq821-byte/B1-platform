<script setup lang="ts">
import { ref, computed, onMounted, inject } from "vue"
import { useAdminStore } from "@/stores/useAdminStore"
import { Cpu, Database, HardDrive, Timer } from "lucide-vue-next"
import PageHeader from "@/components/layout/PageHeader.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"

const store = useAdminStore()
const embedded = inject("tabHubEmbedded", false)
const loadError = ref("")

const showExtraCols = computed(() => {
  const list = store.monitorData?.services ?? []
  return list.some((s) => s.detail != null || s.latencyMs != null)
})

function barColor(pct: number, kind: "cpu" | "mem" | "disk") {
  if (pct >= 85) return "var(--color-danger, #FF4D4F)"
  if (pct >= 70) return "var(--color-warning, #FAAD14)"
  if (kind === "disk") return "var(--color-success, #00B96B)"
  if (kind === "mem") return "var(--color-warning, #FAAD14)"
  return "var(--color-primary, #1677FF)"
}

async function loadMonitor() {
  loadError.value = ""
  try {
    await store.fetchMonitorData()
  } catch (e: unknown) {
    loadError.value = (e as Error)?.message || "加载监控数据失败"
  }
}

onMounted(() => { loadMonitor() })
</script>

<template>
  <div>
    <PageHeader v-if="!embedded" title="运行监控" subtitle="服务健康状态与资源使用情况" />

    <LoadingState v-if="store.monitorLoading" text="加载监控数据..." />
    <ErrorState v-else-if="loadError" :message="loadError" @retry="loadMonitor" />

    <template v-else-if="store.monitorData">
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-card__top">
            <div class="stat-label">CPU 使用率</div>
            <div class="stat-icon b"><Cpu :size="16" /></div>
          </div>
          <div class="stat-value">{{ store.monitorData.cpuUsage }}%</div>
          <div class="bar">
            <i :style="{ width: store.monitorData.cpuUsage + '%', background: barColor(store.monitorData.cpuUsage, 'cpu') }" />
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-card__top">
            <div class="stat-label">内存使用率</div>
            <div class="stat-icon y"><Database :size="16" /></div>
          </div>
          <div class="stat-value">{{ store.monitorData.memoryUsage }}%</div>
          <div class="bar">
            <i :style="{ width: store.monitorData.memoryUsage + '%', background: barColor(store.monitorData.memoryUsage, 'mem') }" />
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-card__top">
            <div class="stat-label">磁盘使用率</div>
            <div class="stat-icon g"><HardDrive :size="16" /></div>
          </div>
          <div class="stat-value">{{ store.monitorData.diskUsage }}%</div>
          <div class="bar">
            <i :style="{ width: store.monitorData.diskUsage + '%', background: barColor(store.monitorData.diskUsage, 'disk') }" />
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-card__top">
            <div class="stat-label">系统运行时间</div>
            <div class="stat-icon n"><Timer :size="16" /></div>
          </div>
          <div class="stat-value sm">{{ store.monitorData.uptime }}</div>
          <div class="stat-meta">自上次重启</div>
        </div>
      </div>

      <div class="section mt-24">
        <div class="section-header">
          <h2 class="section-title">服务状态</h2>
        </div>
        <div class="data-table">
          <table>
            <thead>
              <tr>
                <th>服务名称</th>
                <th>状态</th>
                <th v-if="showExtraCols">详情</th>
                <th v-if="showExtraCols">延迟</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(s, i) in store.monitorData.services" :key="i">
                <td>{{ s.name }}</td>
                <td>
                  <span class="svc" :style="{ color: s.color }">
                    <i :style="{ background: s.color }" />
                    {{ s.status }}
                  </span>
                </td>
                <td v-if="showExtraCols"><span class="mono">{{ s.detail || "-" }}</span></td>
                <td v-if="showExtraCols">
                  <span class="mono">{{ s.latencyMs != null ? `${s.latencyMs} ms` : "-" }}</span>
                </td>
              </tr>
              <tr v-if="!store.monitorData.services?.length">
                <td :colspan="showExtraCols ? 4 : 2" class="empty">暂无服务状态</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.stat-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}
.stat-icon {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: grid;
  place-items: center;
}
.stat-icon.b { background: var(--color-primary-light, #E6F4FF); color: var(--color-primary, #1677FF); }
.stat-icon.y { background: var(--color-warning-light, #FFF7E6); color: #D48806; }
.stat-icon.g { background: var(--color-success-light, #E6F7EF); color: var(--color-success, #00B96B); }
.stat-icon.n { background: var(--color-navy-light, #E8F1F7); color: var(--color-navy, #0B3A5C); }
.stat-value.sm { font-size: 18px; }
.stat-meta {
  margin-top: 10px;
  font-size: 11px;
  color: var(--color-text-placeholder);
}
.bar {
  width: 100%;
  height: 6px;
  margin-top: 12px;
  border-radius: 999px;
  background: #eef2f7;
  overflow: hidden;
}
.bar i {
  display: block;
  height: 100%;
  border-radius: 999px;
}
.section-title {
  font-size: 18px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--color-navy, #0B3A5C);
  margin: 0;
}
.svc {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 13px;
}
.svc i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  display: inline-block;
}
.mono {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--color-text-secondary);
}
.empty {
  text-align: center;
  padding: 28px 0;
  color: var(--color-text-placeholder);
}
.mt-24 { margin-top: 24px; }
</style>
