<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from "vue"
import * as echarts from "echarts"

const props = withDefaults(defineProps<{
  segments: Array<{ name: string; value: number; color: string }>
  centerValue: string | number
  centerLabel?: string
  height?: string
}>(), {
  centerLabel: "",
  height: "200px",
})

const chartRef = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts | null = null

function getOption(): echarts.EChartsOption {
  const total = props.segments.reduce((sum, s) => sum + s.value, 0)
  return {
    tooltip: {
      trigger: "item",
      formatter: "{b}: {c} ({d}%)",
    },
    series: [
      {
        type: "pie",
        radius: ["62%", "88%"],
        center: ["50%", "52%"],
        startAngle: 210,
        endAngle: -30,
        padAngle: 2,
        itemStyle: {
          borderRadius: 4,
        },
        label: { show: false },
        data: total === 0
          ? [{ name: "暂无数据", value: 1, itemStyle: { color: "#E2E8F0" } }]
          : props.segments.map((s) => ({
              name: s.name,
              value: s.value,
              itemStyle: { color: s.color },
            })),
      },
    ],
    graphic: [
      {
        type: "text",
        left: "center",
        top: "42%",
        style: {
          text: String(props.centerValue),
          fill: "#1E293B",
          fontSize: 28,
          fontWeight: 700,
          fontFamily: "JetBrains Mono, SF Mono, Consolas, monospace",
        },
      },
      {
        type: "text",
        left: "center",
        top: "58%",
        style: {
          text: props.centerLabel,
          fill: "#94A3B8",
          fontSize: 12,
        },
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
  () => [props.segments, props.centerValue, props.centerLabel],
  () => nextTick(disposeAndReinit),
  { deep: true },
)

onMounted(() => {
  initChart()
  window.addEventListener("resize", handleResize)
})

onUnmounted(() => {
  window.removeEventListener("resize", handleResize)
  chartInstance?.dispose()
  chartInstance = null
})
</script>

<template>
  <div ref="chartRef" class="donut-chart" :style="{ height }" />
</template>

<style scoped>
.donut-chart {
  width: 100%;
}
</style>
