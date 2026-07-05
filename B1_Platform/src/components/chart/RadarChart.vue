<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from "vue"
import * as echarts from "echarts"

const props = withDefaults(defineProps<{
  categories: string[]
  seriesData: Array<{ name: string; data: number[]; color?: string }>
  height?: string
  title?: string
}>(), {
  height: "320px",
})

const chartRef = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts | null = null

const COLOR_PALETTE = ["#3B82F6", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6", "#EC4899"]

function getMaxValue(): number {
  const allValues = props.seriesData.flatMap((s) => s.data)
  return allValues.length > 0 ? Math.ceil(Math.max(...allValues) / 10) * 10 : 100
}

function getOption(): echarts.EChartsOption {
  const maxVal = getMaxValue()
  return {
    tooltip: {},
    legend: {
      bottom: 0,
      textStyle: { fontSize: 12, color: "#64748B" },
    },
    radar: {
      indicator: props.categories.map((name) => ({ name, max: maxVal })),
      shape: "polygon",
      radius: "65%",
      splitArea: {
        areaStyle: { opacity: 0.05 },
      },
      splitLine: {
        lineStyle: { width: 1 },
      },
      axisName: {
        fontSize: 12,
        color: "#64748B",
      },
    },
    series: [
      {
        type: "radar",
        data: props.seriesData.map((s, i) => ({
          name: s.name,
          value: s.data,
          areaStyle: { color: s.color || COLOR_PALETTE[i % COLOR_PALETTE.length], opacity: 0.1 },
          lineStyle: { color: s.color || COLOR_PALETTE[i % COLOR_PALETTE.length] },
          itemStyle: { color: s.color || COLOR_PALETTE[i % COLOR_PALETTE.length] },
        })),
      },
    ],
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
  () => [props.categories, props.seriesData],
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
