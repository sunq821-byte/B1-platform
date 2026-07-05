<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue"
import { ElMessage } from "element-plus"
import { useTeacherStore } from "@/stores/useTeacherStore"
import PageHeader from "@/components/layout/PageHeader.vue"
import BarChart from "@/components/chart/BarChart.vue"

const store = useTeacherStore()
const filterClass = ref("all")

const classes = computed(() => {
  const set = new Set(store.students.map((s) => s.className))
  return Array.from(set).sort()
})

async function loadReport() {
  await store.fetchClassReport(filterClass.value === "all" ? undefined : filterClass.value)
}

async function initPage() {
  await store.fetchStudents()
  await loadReport()
}

watch(filterClass, () => {
  loadReport()
})

onMounted(() => { initPage() })

const scoreColor = (score: number) => {
  if (score >= 80) return "good"
  if (score >= 60) return "warn"
  return "danger"
}

function handleExport() {
  if (!store.classReport || store.classReport.rows.length === 0) {
    ElMessage.warning("当前无数据可导出")
    return
  }

  const className = filterClass.value === "all" ? "全部班级" : filterClass.value
  const BOM = "﻿"
  const header = "学号,姓名,班级,已完成,平均分,最高分,最低分"
  const lines = store.classReport.rows.map((r) =>
    `${r.studentId},${r.name},${r.className},${r.completedCount},${r.avgScore.toFixed(1)},${r.maxScore.toFixed(1)},${r.minScore.toFixed(1)}`,
  )

  // Stats summary rows
  const s = store.classReport.stats
  const summary = [
    "",
    `班级人数:,${s.totalStudents}`,
    `已评阅数:,${s.totalReviewed}`,
    `班级均分:,${s.classAverage}`,
    `及格率:,${s.passRate}%`,
  ]

  const csv = BOM + header + "\n" + lines.join("\n") + "\n" + summary.join("\n")
  const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" })
  const url = URL.createObjectURL(blob)

  const a = document.createElement("a")
  a.href = url
  a.download = `成绩报表_${className}_${new Date().toISOString().substring(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)

  ElMessage.success(`已导出 ${store.classReport.rows.length} 条成绩数据`)
}

const subtitle = computed(() => {
  const cn = filterClass.value === "all" ? "全部班级" : filterClass.value
  return `${cn}成绩统计与分析`
})
</script>

<template>
  <div>
    <PageHeader title="班级报告" :subtitle="subtitle" />

    <div class="filter-bar">
      <select v-model="filterClass" class="form-select" style="width: 180px;">
        <option value="all">全部班级</option>
        <option v-for="c in classes" :key="c" :value="c">{{ c }}</option>
      </select>
      <button class="btn btn-outline btn-sm" style="margin-left: auto;" @click="handleExport">📥 导出成绩</button>
    </div>

    <template v-if="store.classReport">
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-label">班级人数</div>
          <div class="stat-value">{{ store.classReport.stats.totalStudents }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">已评阅数</div>
          <div class="stat-value">{{ store.classReport.stats.totalReviewed }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">班级均分</div>
          <div class="stat-value">{{ store.classReport.stats.classAverage }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">及格率</div>
          <div class="stat-value">{{ store.classReport.stats.passRate }}%</div>
        </div>
      </div>

      <div class="content-grid mt-24">
        <div class="chart-wrap lg">
          <BarChart
            title="分数分布直方图"
            :x-axis="store.classReport.histogram.categories"
            :series-data="[{ name: '人数', data: store.classReport.histogram.values }]"
            height="360px"
          />
        </div>
        <div class="chart-wrap lg">
          <BarChart
            title="课程均分对比"
            :x-axis="store.classReport.courseAvgs.categories"
            :series-data="[{ name: '均分', data: store.classReport.courseAvgs.values }]"
            height="360px"
          />
        </div>
      </div>

      <div class="section mt-24">
        <div class="section-header">
          <h2 class="section-title">班级成绩表</h2>
        </div>
        <div class="data-table">
          <table>
            <thead>
              <tr>
                <th>学号</th>
                <th>姓名</th>
                <th>班级</th>
                <th>已完成</th>
                <th>平均分</th>
                <th>最高分</th>
                <th>最低分</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in store.classReport.rows" :key="row.studentId">
                <td><span style="font-family: var(--font-mono); font-size: 11px;">{{ row.studentId }}</span></td>
                <td>{{ row.name }}</td>
                <td>{{ row.className }}</td>
                <td>{{ row.completedCount }}</td>
                <td><span class="item-score" :class="scoreColor(row.avgScore)">{{ row.avgScore.toFixed(1) }}</span></td>
                <td><span class="item-score good">{{ row.maxScore.toFixed(1) }}</span></td>
                <td><span class="item-score danger">{{ row.minScore.toFixed(1) }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.form-select {
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
</style>
