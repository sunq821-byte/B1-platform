<script setup lang="ts">
import { onMounted } from "vue"
import { useStudentStore } from "@/stores/useStudentStore"
import { useUserStore } from "@/stores/useUserStore"
import PageHeader from "@/components/layout/PageHeader.vue"
import LineChart from "@/components/chart/LineChart.vue"

const store = useStudentStore()
const userStore = useUserStore()

onMounted(() => { store.fetchGrowth() })

const scoreClass = (score: number) => {
  if (score >= 80) return "good"
  if (score >= 60) return "warn"
  return "danger"
}
</script>

<template>
  <div>
    <PageHeader
      title="成长中心"
      :subtitle="`${userStore.userName} · 能力成长轨迹`"
    />

    <template v-if="store.growthData">
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-label">成长记录</div>
          <div class="stat-value">{{ store.growthData.stats.totalEntries }}</div>
          <div class="stat-meta">个成长节点</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">当前能力值</div>
          <div class="stat-value">{{ store.growthData.stats.latestScore }}</div>
          <div class="stat-meta">最新评估</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">初始能力值</div>
          <div class="stat-value">{{ store.growthData.stats.firstScore }}</div>
          <div class="stat-meta">首次评估</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">能力提升</div>
          <div class="stat-value" style="color:var(--color-success, #10b981);">+{{ store.growthData.stats.improvement }}</div>
          <div class="stat-meta">进步显著</div>
        </div>
      </div>

      <div class="content-grid mt-24">
        <div class="chart-wrap lg panel-full">
          <LineChart
            title="能力演变图"
            :x-axis="store.growthData.evolutionData.allDates"
            :series-data="store.growthData.evolutionData.series"
            height="360px"
          />
        </div>
      </div>

      <div class="section mt-24">
        <div class="section-header"><h2 class="section-title">成长时间线</h2></div>
        <div class="card">
          <div class="timeline">
            <div v-for="(entry, i) in store.growthData.entries" :key="i" class="timeline-item">
              <div class="timeline-date">{{ entry.date }}</div>
              <div class="timeline-title">{{ entry.category }}</div>
              <div class="timeline-desc">{{ entry.description }}</div>
              <div class="timeline-score" :class="`item-score ${scoreClass(entry.score)}`">{{ entry.score }} 分</div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.timeline {
  position: relative;
  padding-left: 24px;
}
.timeline::before {
  content: "";
  position: absolute;
  left: 8px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: var(--color-border, #e2e8f0);
}
.timeline-item {
  position: relative;
  padding: 0 0 20px 20px;
}
.timeline-item::before {
  content: "";
  position: absolute;
  left: -20px;
  top: 4px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--color-primary, #3b82f6);
  border: 2px solid var(--color-card, #fff);
}
.timeline-date {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--color-text-tertiary, #94a3b8);
  margin-bottom: 4px;
}
.timeline-title {
  font-size: 13.5px;
  font-weight: 500;
}
.timeline-desc {
  font-size: 12px;
  color: var(--color-text-secondary, #64748b);
}
.timeline-score {
  font-family: var(--font-mono);
  font-size: 14px;
  font-weight: 600;
  margin-top: 4px;
}
</style>
