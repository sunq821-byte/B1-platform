<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from "vue"
import * as echarts from "echarts"

const props = withDefaults(defineProps<{
  xAxis: string[]
  seriesData: Array<{ name: string; data: (number | null)[]; color?: string }>
  height?: string
  title?: string
  smooth?: boolean
  yMin?: number | "dataMin"
  yMax?: number | "dataMax"
  /** 稀疏数据时留出类目边距，避免折点贴边 */
  boundaryGap?: boolean
  /** 单点/少点时放大标记 */
  largeSymbols?: boolean
}>(), {
  height: "320px",
  smooth: true,
  boundaryGap: false,
  largeSymbols: false,
})

const chartRef = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts | null = null
let resizeObserver: ResizeObserver | null = null

const COLOR_PALETTE = ["#1677FF", "#00B96B", "#FAAD14", "#FF4D4F", "#722ED1", "#0B3A5C"]

function isNarrow(): boolean {
  const w = chartRef.value?.clientWidth ?? 0
  return w > 0 ? w < 480 : false
}

function getOption(): echarts.EChartsOption {
  const symbolSize = props.largeSymbols ? 9 : 6
  const narrow = isNarrow()
  const manyLabels = props.xAxis.length > 5
  return {
    tooltip: {
      trigger: "axis",
      confine: true,
      valueFormatter: (value) => (value == null || value === "" ? "—" : `${value} 分`),
    },
    legend: {
      bottom: 0,
      type: "scroll",
      textStyle: { fontSize: narrow ? 11 : 12, color: "#64748B" },
    },
    grid: {
      top: narrow ? 28 : 48,
      left: narrow ? 36 : 50,
      right: narrow ? 10 : 20,
      bottom: narrow ? (manyLabels ? 64 : 52) : 56,
      containLabel: true,
    },
    xAxis: {
      type: "category",
      boundaryGap: props.boundaryGap,
      data: props.xAxis,
      axisLine: { lineStyle: { color: "#E2E8F0" } },
      axisLabel: {
        color: "#94A3B8",
        fontSize: narrow ? 10 : 12,
        hideOverlap: true,
        interval: narrow && manyLabels ? "auto" : 0,
        rotate: narrow && manyLabels ? 28 : 0,
      },
    },
    yAxis: {
      type: "value",
      min: props.yMin,
      max: props.yMax,
      scale: props.yMin != null && props.yMax != null,
      splitLine: { lineStyle: { color: "#F1F5F9" } },
      axisLabel: { color: "#94A3B8", fontSize: narrow ? 10 : 12, formatter: "{value}" },
    },
    series: props.seriesData.map((s, i) => ({
      name: s.name,
      type: "line",
      data: s.data,
      smooth: props.smooth,
      connectNulls: false,
      showSymbol: true,
      symbol: "circle",
      symbolSize: narrow ? Math.max(4, symbolSize - 2) : symbolSize,
      lineStyle: { color: s.color || COLOR_PALETTE[i % COLOR_PALETTE.length], width: 2 },
      itemStyle: { color: s.color || COLOR_PALETTE[i % COLOR_PALETTE.length] },
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
  if (!chartInstance) return
  chartInstance.resize()
  chartInstance.setOption(getOption(), { replaceMerge: ["xAxis", "grid", "legend"] })
}

watch(
  () => [props.xAxis, props.seriesData, props.yMin, props.yMax, props.boundaryGap, props.largeSymbols],
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
