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
let resizeObserver: ResizeObserver | null = null

const COLOR_PALETTE = ["#1677FF", "#00B96B", "#FAAD14", "#FF4D4F", "#722ED1", "#0B3A5C"]

function hasValidData(): boolean {
  return props.categories.length > 0 && props.seriesData.some(s => s.data.length > 0)
}

function isNarrow(): boolean {
  const w = chartRef.value?.clientWidth ?? 0
  return w > 0 ? w < 480 : false
}

function getOption(): echarts.EChartsOption {
  if (!hasValidData()) {
    return {
      title: { text: "暂无数据", left: "center", top: "center", textStyle: { fontSize: 14, color: "#94a3b8" } },
    }
  }
  const narrow = isNarrow()
  return {
    tooltip: { confine: true },
    legend: {
      bottom: 0,
      textStyle: { fontSize: narrow ? 11 : 12, color: "#64748B" },
    },
    radar: {
      indicator: props.categories.map((name) => ({ name, max: 100 })),
      shape: "polygon",
      radius: narrow ? "58%" : "65%",
      center: ["50%", narrow ? "46%" : "50%"],
      splitArea: {
        areaStyle: { opacity: 0.05 },
      },
      splitLine: {
        lineStyle: { width: 1 },
      },
      axisName: {
        fontSize: narrow ? 10 : 12,
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
  if (!chartInstance) return
  chartInstance.resize()
  chartInstance.setOption(getOption(), { replaceMerge: ["radar", "legend"] })
}

watch(
  () => [props.categories, props.seriesData],
  () => nextTick(disposeAndReinit),
  { deep: true },
)

onMounted(() => {
  nextTick(() => {
    initChart()
    if (chartRef.value && typeof ResizeObserver !== "undefined") {
      resizeObserver = new ResizeObserver(() => handleResize())
      resizeObserver.observe(chartRef.value)
    }
  })
  window.addEventListener("resize", handleResize)
})

onUnmounted(() => {
  window.removeEventListener("resize", handleResize)
  resizeObserver?.disconnect()
  resizeObserver = null
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
  width: 100%;
  max-width: 100%;
  min-width: 0;
  overflow: hidden;
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
  width: 100%;
  min-width: 0;
  min-height: 0;
}
</style>
