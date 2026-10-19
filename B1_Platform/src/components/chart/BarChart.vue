<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from "vue"
import * as echarts from "echarts"

const props = withDefaults(defineProps<{
  xAxis: string[]
  seriesData: Array<{ name: string; data: number[]; color?: string }>
  height?: string
  title?: string
  horizontal?: boolean
  /** 单系列时，每根柱子使用不同颜色 */
  multiColor?: boolean
  /** 按柱子顺序指定颜色（优先级高于 multiColor） */
  itemColors?: string[]
  /** 双系列叠柱：第 1 系列为前景，第 2 系列为背景轨道（类似完成度图） */
  overlayTrack?: boolean
  yMax?: number
}>(), {
  height: "320px",
  horizontal: false,
  multiColor: false,
  overlayTrack: false,
})

const chartRef = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts | null = null

const COLOR_PALETTE = ["#1677FF", "#00B96B", "#FAAD14", "#FF4D4F", "#722ED1", "#0B3A5C"]

function resolveBarColor(dataIndex: number, seriesIndex: number, seriesColor?: string): string {
  if (props.itemColors?.[dataIndex]) return props.itemColors[dataIndex]
  if (props.multiColor) return COLOR_PALETTE[dataIndex % COLOR_PALETTE.length]
  return seriesColor || COLOR_PALETTE[seriesIndex % COLOR_PALETTE.length]
}

function mapBarData(values: number[], seriesIndex: number, seriesColor?: string) {
  return values.map((value, dataIndex) => ({
    value,
    itemStyle: {
      color: resolveBarColor(dataIndex, seriesIndex, seriesColor),
      borderRadius: props.horizontal ? [0, 4, 4, 0] : [4, 4, 0, 0],
    },
  }))
}

function getOption(): echarts.EChartsOption {
  const categoryAxis: Record<string, unknown> = {
    type: "category",
    data: props.xAxis,
    axisLine: { lineStyle: { color: "#E2E8F0" } },
    axisLabel: { color: "#94A3B8", fontSize: 12 },
  }
  const valueAxis: Record<string, unknown> = {
    type: "value",
    max: props.yMax,
    splitLine: { lineStyle: { color: "#F1F5F9" } },
    axisLabel: { color: "#94A3B8", fontSize: 12 },
  }

  const hideLegend = props.multiColor || (props.itemColors?.length ?? 0) > 0

  let series: echarts.SeriesOption[]
  if (props.overlayTrack && props.seriesData.length >= 2) {
    const [foreground, track] = props.seriesData
    series = [
      {
        name: track.name,
        type: "bar",
        data: mapBarData(track.data, 0, track.color || "#E8EEF5"),
        barWidth: 28,
        z: 1,
        itemStyle: { borderRadius: props.horizontal ? [0, 4, 4, 0] : [4, 4, 0, 0] },
      },
      {
        name: foreground.name,
        type: "bar",
        data: mapBarData(foreground.data, 1, foreground.color || "#1677FF"),
        barWidth: 18,
        barGap: "-100%",
        z: 2,
        itemStyle: { borderRadius: props.horizontal ? [0, 4, 4, 0] : [4, 4, 0, 0] },
      },
    ]
  } else {
    series = props.seriesData.map((s, i) => ({
      name: s.name,
      type: "bar",
      data: mapBarData(s.data, i, s.color),
      barGap: "30%",
    }))
  }

  return {
    tooltip: { trigger: "axis" },
    legend: hideLegend && !props.overlayTrack
      ? { show: false }
      : {
          bottom: 0,
          textStyle: { fontSize: 12, color: "#64748B" },
        },
    grid: {
      top: 60,
      left: 50,
      right: 20,
      bottom: hideLegend && !props.overlayTrack ? 24 : 50,
    },
    xAxis: props.horizontal ? valueAxis : categoryAxis,
    yAxis: props.horizontal ? categoryAxis : valueAxis,
    series,
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
  () => [props.xAxis, props.seriesData, props.horizontal, props.multiColor, props.itemColors, props.overlayTrack, props.yMax],
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
  width: 100%;
  max-width: 100%;
  min-width: 0;
  overflow: hidden;
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
  width: 100%;
  min-width: 0;
  min-height: 0;
}
</style>
