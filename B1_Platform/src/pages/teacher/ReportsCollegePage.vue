<script setup lang="ts">
import { onMounted } from "vue"
import { useTeacherStore } from "@/stores/useTeacherStore"
import PageHeader from "@/components/layout/PageHeader.vue"
import BarChart from "@/components/chart/BarChart.vue"
import LineChart from "@/components/chart/LineChart.vue"

const store = useTeacherStore()

onMounted(() => {
  store.fetchCollegeReport()
})
</script>

<template>
  <div>
    <PageHeader title="学院报告" subtitle="跨班级对比分析与学期趋势" />

    <template v-if="store.collegeReport">
      <div class="content-grid">
        <div class="chart-wrap lg">
          <BarChart
            title="跨班对比分组柱状图"
            :x-axis="store.collegeReport.crossClass.classNames"
            :series-data="[{ name: '班级均分', data: store.collegeReport.crossClass.values }]"
            height="360px"
          />
        </div>
        <div class="chart-wrap lg">
          <LineChart
            title="学期趋势多折线图"
            :x-axis="store.collegeReport.semesterTrend.semesters"
            :series-data="store.collegeReport.semesterTrend.series"
            height="360px"
            :smooth="true"
          />
        </div>
      </div>
    </template>
  </div>
</template>
