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

const COLOR_PALETTE = ["#1677FF", "#00B96B", "#FAAD14", "#FF4D4F", "#722ED1", "#0B3A5C"]

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
  background: transparent;
  border-radius: var(--radius-lg, 16px);
  padding: 0;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.chart-card__title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--color-text-primary, #0f172a);
  flex-shrink: 0;
}

.chart-card__body {
  flex: 1;
  min-height: 0;
}
</style>
