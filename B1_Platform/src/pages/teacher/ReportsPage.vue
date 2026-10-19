<script setup lang="ts">
import { ref, computed, watch, inject } from "vue"
import { ElMessage } from "element-plus"
import { useTeacherStore } from "@/stores/useTeacherStore"
import { exportClassReport } from "@/api/modules/teacher"
import { useReportRefresh } from "@/composables/useReportRefresh"
import PageHeader from "@/components/layout/PageHeader.vue"
import BarChart from "@/components/chart/BarChart.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"

const store = useTeacherStore()
const embedded = inject("tabHubEmbedded", false)
const filterAdminClass = ref("all")
const filterCourse = ref("all")
const exporting = ref(false)
const loadError = ref("")

const adminClassOptions = computed(() => {
  const set = new Set<string>()
  for (const c of store.courses) {
    if (c.className) set.add(c.className)
  }
  return Array.from(set).sort()
})

const courseOptions = computed(() => {
  const set = new Set<string>()
  for (const c of store.courses) {
    if (filterAdminClass.value !== "all" && c.className !== filterAdminClass.value) continue
    if (c.courseName) set.add(c.courseName)
  }
  return Array.from(set).sort()
})

const taskCompletionChart = computed(() => {
  const tc = store.classReport?.taskCompletion
  if (!tc?.categories.length) return null
  return {
    xAxis: tc.categories.map((name) => (name.length > 10 ? `${name.slice(0, 10)}…` : name)),
    series: [
      { name: "已完成", data: tc.completedCount, color: "#1677FF" },
      { name: "应提交", data: tc.totalStudents, color: "#E2E8F0" },
    ],
  }
})

const dimensionChart = computed(() => {
  const dim = store.classReport?.dimensionAvgs
  if (!dim?.categories.length) return null
  return {
    xAxis: dim.categories,
    data: dim.values,
  }
})

function reportParams() {
  return {
    adminClass: filterAdminClass.value === "all" ? undefined : filterAdminClass.value,
    courseName: filterCourse.value === "all" ? undefined : filterCourse.value,
  }
}

async function loadReport() {
  loadError.value = ""
  try {
    await store.fetchClassReport(reportParams())
  } catch (e: unknown) {
    loadError.value = (e as Error)?.message || "加载班级报告失败"
  }
}

async function initPage() {
  loadError.value = ""
  try {
    await Promise.all([store.fetchCourses(), store.fetchStudents()])
    await loadReport()
  } catch (e: unknown) {
    loadError.value = (e as Error)?.message || "加载失败"
  }
}

watch(filterAdminClass, () => {
  if (filterCourse.value !== "all" && !courseOptions.value.includes(filterCourse.value)) {
    filterCourse.value = "all"
  }
  loadReport()
})
watch(filterCourse, () => { loadReport() })
useReportRefresh(initPage)

const scoreColor = (score: number) => {
  if (score >= 80) return "good"
  if (score >= 60) return "warn"
  return "danger"
}

async function handleExport(format: "xlsx" | "pdf") {
  if (!store.classReport || store.classReport.rows.length === 0) {
    ElMessage.warning("当前无数据可导出")
    return
  }
  exporting.value = true
  try {
    const p = reportParams()
    await exportClassReport(p.adminClass, p.courseName, format)
    ElMessage.success("导出成功")
  } catch { /* surfaced by helper */ }
  finally { exporting.value = false }
}

const subtitle = computed(() => {
  const parts: string[] = []
  if (filterAdminClass.value !== "all") parts.push(filterAdminClass.value)
  if (filterCourse.value !== "all") parts.push(filterCourse.value)
  const label = parts.length ? parts.join(" · ") : "全部班级"
  return `${label} 成绩统计与分析`
})

/** 分数段语义色：低分→高分 */
const histogramColors = ["#FF4D4F", "#FAAD14", "#1677FF", "#00B96B", "#722ED1"]
</script>

<template>
  <div>
    <PageHeader v-if="!embedded" title="班级报告" :subtitle="subtitle" />

    <div class="filter-bar">
      <select v-model="filterAdminClass" class="form-select w-filter">
        <option value="all">全部行政班</option>
        <option v-for="c in adminClassOptions" :key="c" :value="c">{{ c }}</option>
      </select>
      <select v-model="filterCourse" class="form-select w-filter">
        <option value="all">全部课程</option>
        <option v-for="c in courseOptions" :key="c" :value="c">{{ c }}</option>
      </select>
      <div class="export-actions ml-auto">
        <button type="button" class="btn btn-outline btn-sm" :disabled="exporting" @click="handleExport('xlsx')">导出 Excel</button>
        <button type="button" class="btn btn-outline btn-sm" :disabled="exporting" @click="handleExport('pdf')">导出 PDF</button>
      </div>
    </div>

    <LoadingState v-if="store.classReportLoading" text="加载班级报告..." />
    <ErrorState v-else-if="loadError" :message="loadError" @retry="initPage" />

    <template v-else-if="store.classReport">
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-label">班级人数</div>
          <div class="stat-value">{{ store.classReport.stats.totalStudents }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">实训任务</div>
          <div class="stat-value">{{ store.classReport.stats.totalTasks }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">任务完成率</div>
          <div class="stat-value">{{ store.classReport.stats.taskCompletionRate }}%</div>
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
            :item-colors="histogramColors"
            height="360px"
          />
        </div>
        <div class="chart-wrap lg">
          <BarChart
            title="课程均分对比"
            :x-axis="store.classReport.courseAvgs.categories"
            :series-data="[{ name: '均分', data: store.classReport.courseAvgs.values }]"
            multi-color
            height="360px"
          />
        </div>
      </div>

      <div v-if="taskCompletionChart || dimensionChart" class="content-grid mt-24">
        <div v-if="taskCompletionChart" class="chart-wrap lg">
          <BarChart
            title="任务提交完成度"
            :x-axis="taskCompletionChart.xAxis"
            :series-data="taskCompletionChart.series"
            overlay-track
            height="360px"
          />
        </div>
        <div v-if="dimensionChart" class="chart-wrap lg">
          <BarChart
            title="班级维度均分"
            :x-axis="dimensionChart.xAxis"
            :series-data="[{ name: '均分', data: dimensionChart.data, color: '#722ED1' }]"
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
                <th>课程</th>
                <th>已完成</th>
                <th>平均分</th>
                <th>最高分</th>
                <th>最低分</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in store.classReport.rows" :key="row.studentId">
                <td><span class="mono">{{ row.studentId }}</span></td>
                <td>{{ row.name }}</td>
                <td>{{ row.className }}</td>
                <td>{{ row.courseName || "-" }}</td>
                <td>{{ row.completedCount }}</td>
                <td><span class="item-score" :class="scoreColor(row.avgScore)">{{ row.avgScore.toFixed(1) }}</span></td>
                <td><span class="item-score good">{{ row.maxScore.toFixed(1) }}</span></td>
                <td><span class="item-score danger">{{ row.minScore.toFixed(1) }}</span></td>
              </tr>
              <tr v-if="!store.classReport.rows.length">
                <td colspan="8" class="empty">暂无成绩数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
    <div v-else class="empty-block">暂无班级报告数据</div>
  </div>
</template>

<style scoped>
.w-filter { width: 200px; }
.ml-auto { margin-left: auto; }
.export-actions { display: flex; gap: 8px; }
.mono {
  font-family: var(--font-mono);
  font-size: 11px;
}
.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  color: var(--color-navy, #0B3A5C);
}
.mt-24 { margin-top: 24px; }
.empty, .empty-block {
  text-align: center;
  padding: 28px 0;
  color: var(--color-text-placeholder);
}
</style>
