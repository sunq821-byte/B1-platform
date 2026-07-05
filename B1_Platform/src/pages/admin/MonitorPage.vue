<script setup lang="ts">
import { onMounted } from "vue"
import { useAdminStore } from "@/stores/useAdminStore"
import PageHeader from "@/components/layout/PageHeader.vue"

const store = useAdminStore()

onMounted(() => { store.fetchMonitorData() })
</script>

<template>
  <div>
    <PageHeader title="系统监控" subtitle="服务健康状态与资源使用情况" />

    <template v-if="store.monitorData">
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-label">CPU 使用率</div>
          <div class="stat-value">{{ store.monitorData.cpuUsage }}%</div>
          <div class="stat-meta" style="width:100%;height:4px;background:var(--color-border,#e2e8f0);margin-top:8px;border-radius:2px;">
            <div style="height:100%;background:var(--color-primary,#3b82f6);border-radius:2px;" :style="{ width: store.monitorData.cpuUsage + '%' }"></div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-label">内存使用率</div>
          <div class="stat-value">{{ store.monitorData.memoryUsage }}%</div>
          <div class="stat-meta" style="width:100%;height:4px;background:var(--color-border,#e2e8f0);margin-top:8px;border-radius:2px;">
            <div style="height:100%;background:var(--color-warning,#f59e0b);border-radius:2px;" :style="{ width: store.monitorData.memoryUsage + '%' }"></div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-label">磁盘使用率</div>
          <div class="stat-value">{{ store.monitorData.diskUsage }}%</div>
          <div class="stat-meta" style="width:100%;height:4px;background:var(--color-border,#e2e8f0);margin-top:8px;border-radius:2px;">
            <div style="height:100%;background:var(--color-success,#10b981);border-radius:2px;" :style="{ width: store.monitorData.diskUsage + '%' }"></div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-label">系统运行时间</div>
          <div class="stat-value" style="font-size:22px;">{{ store.monitorData.uptime }}</div>
          <div class="stat-meta">自上次重启</div>
        </div>
      </div>

      <div class="section mt-24">
        <div class="section-header"><h2 class="section-title">服务状态</h2></div>
        <div class="data-table">
          <table>
            <thead><tr><th>服务名称</th><th>状态</th></tr></thead>
            <tbody>
              <tr v-for="(s, i) in store.monitorData.services" :key="i">
                <td>{{ s.name }}</td>
                <td>
                  <span style="display:flex;align-items:center;gap:6px;" :style="{ color: s.color }">
                    <span style="width:6px;height:6px;border-radius:50%;display:inline-block;" :style="{ background: s.color }"></span>
                    {{ s.status }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>
