<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from "vue"
import * as echarts from "echarts"

const props = withDefaults(defineProps<{
  xAxis: string[]
  seriesData: Array<{ name: string; data: number[]; color?: string }>
  height?: string
  title?: string
  horizontal?: boolean
}>(), {
  height: "320px",
  horizontal: false,
})

const chartRef = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts | null = null

const COLOR_PALETTE = ["#3B82F6", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6", "#EC4899"]

function getOption(): echarts.EChartsOption {
  const categoryAxis: Record<string, unknown> = {
    type: "category",
    data: props.xAxis,
    axisLine: { lineStyle: { color: "#E2E8F0" } },
    axisLabel: { color: "#94A3B8", fontSize: 12 },
  }
  const valueAxis: Record<string, unknown> = {
    type: "value",
    splitLine: { lineStyle: { color: "#F1F5F9" } },
    axisLabel: { color: "#94A3B8", fontSize: 12 },
  }

  return {
    tooltip: { trigger: "axis" },
    legend: {
      bottom: 0,
      textStyle: { fontSize: 12, color: "#64748B" },
    },
    grid: { top: 60, left: 50, right: 20, bottom: 50 },
    xAxis: props.horizontal ? valueAxis : categoryAxis,
    yAxis: props.horizontal ? categoryAxis : valueAxis,
    series: props.seriesData.map((s, i) => ({
      name: s.name,
      type: "bar",
      data: s.data,
      barGap: "30%",
      itemStyle: {
        color: s.color || COLOR_PALETTE[i % COLOR_PALETTE.length],
        borderRadius: [4, 4, 0, 0],
      },
    })),
  }
}

function initChart() {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption(getOption())
}

function disposeAndReinit() {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  initChart()
}

function handleResize() {
  chartInstance?.resize()
}

watch(
  () => [props.xAxis, props.seriesData, props.horizontal],
  () => nextTick(disposeAndReinit),
  { deep: true },
)

onMounted(() => {
  nextTick(initChart)
  window.addEventListener("resize", handleResize)
})

onUnmounted(() => {
  window.removeEventListener("resize", handleResize)
  chartInstance?.dispose()
  chartInstance = null
})
</script>

<template>
  <div class="chart-card" :style="{ height: height }">
    <h4 v-if="title" class="chart-card__title">{{ title }}</h4>
    <div ref="chartRef" class="chart-card__body" />
  </div>
</template>

<style scoped>
.chart-card {
  background: var(--color-card);
  border-radius: 12px;
  padding: 16px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.chart-card__title {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: inherit;
  flex-shrink: 0;
}

.chart-card__body {
  flex: 1;
  min-height: 0;
}
</style>
