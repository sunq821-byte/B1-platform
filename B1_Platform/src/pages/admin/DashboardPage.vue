<script setup lang="ts">
import { onMounted } from "vue"
import { useAdminStore } from "@/stores/useAdminStore"
import PageHeader from "@/components/layout/PageHeader.vue"

const store = useAdminStore()

onMounted(() => { store.fetchDashboard() })

const typeIcons: Record<string, string> = {
  error: "❌",
  warning: "⚠️",
  success: "✅",
  info: "ℹ️",
}

function relativeTime(iso: string): string {
  const diff = Date.now() - new Date(iso).getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return "刚刚"
  if (mins < 60) return `${mins} 分钟前`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours} 小时前`
  return `${Math.floor(hours / 24)} 天前`
}
</script>

<template>
  <div>
    <PageHeader title="管理仪表盘" subtitle="系统运行概况" />

    <template v-if="store.dashboardData">
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-label">总用户数</div>
          <div class="stat-value">{{ store.dashboardData.stats.totalUsers }}</div>
          <div class="stat-meta">学生 + 教师 + 管理员</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">活跃课程</div>
          <div class="stat-value">{{ store.dashboardData.stats.activeCourses }}</div>
          <div class="stat-meta">当前学期</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">总提交数</div>
          <div class="stat-value">{{ store.dashboardData.stats.totalSubmissions }}</div>
          <div class="stat-meta">所有提交记录</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">完成率</div>
          <div class="stat-value">{{ store.dashboardData.stats.completionRate }}%</div>
          <div class="stat-meta">已评阅比例</div>
        </div>
      </div>

      <div class="content-grid mt-24">
        <div class="card">
          <div class="card-title">近期操作日志</div>
          <div class="item-list mt-12">
            <div v-for="(l, i) in store.dashboardData.recentLogs" :key="i" class="item-row" style="cursor:default;">
              <span>{{ typeIcons[l.type] || "ℹ️" }}</span>
              <span class="item-name">{{ l.message }} - {{ l.detail }}</span>
              <span class="item-meta">{{ relativeTime(l.createdAt) }}</span>
            </div>
          </div>
        </div>
        <div class="card">
          <div class="card-title">服务健康状态</div>
          <div class="item-list">
            <div
              v-for="(s, i) in store.dashboardData.health"
              :key="i"
              class="item-row"
              style="cursor:default;"
            >
              <span class="item-name">{{ s.name }}</span>
              <span class="item-meta" :style="{ color: s.color }">
                <span class="health-dot" :style="{ background: s.color }"></span>
                {{ s.status }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
