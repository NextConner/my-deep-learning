<template>
  <div class="timeline-chart">
    <div v-if="!events.length" class="empty">暂无数据</div>
    <div v-else ref="chartRef" class="chart-wrapper"></div>
  </div>
</template>

<script setup>
import { computed, getCurrentInstance, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  events: { type: Array, required: true },
  timeUnit: { type: String, default: 'hour' }
})

const emit = defineEmits(['eventClick'])

const { proxy } = getCurrentInstance()
const chartRef = ref(null)
let chartInstance
const HOUR_MS = 60 * 60 * 1000

function getAxisStartTime(value) {
  return new Date(value).getTime() - (6 * HOUR_MS)
}

const sortedEvents = computed(() => [...props.events].sort((a, b) => new Date(a.createTime) - new Date(b.createTime)))

const axisStartTime = computed(() => {
  if (!sortedEvents.value.length) {
    return null
  }
  return getAxisStartTime(sortedEvents.value[0].createTime)
})

const axisEndTime = computed(() => {
  if (!sortedEvents.value.length) {
    return null
  }
  return new Date(sortedEvents.value[sortedEvents.value.length - 1].createTime).getTime() + (6 * HOUR_MS)
})

const axisMaxHour = computed(() => {
  if (axisStartTime.value === null || axisEndTime.value === null) {
    return 0
  }
  return Number(((axisEndTime.value - axisStartTime.value) / HOUR_MS).toFixed(2))
})

const yAxisRange = computed(() => {
  if (!seriesData.value.length) {
    return { min: 0, max: 10 }
  }

  const values = seriesData.value.map(item => item.value[1])
  const minValue = Math.min(...values)
  const maxValue = Math.max(...values)
  const span = Math.max(maxValue - minValue, 1)
  const topPadding = Math.max(Math.ceil(span * 0.3), 2)
  const bottomPadding = Math.max(Math.ceil(span * 0.12), 1)

  return {
    min: minValue - bottomPadding,
    max: maxValue + topPadding
  }
})

const seriesData = computed(() => {
  if (!sortedEvents.value.length || axisStartTime.value === null) {
    return []
  }

  let runningValue = 0

  return sortedEvents.value.map(event => {
    const quantity = Number(event.changeQuantity || 0)
    runningValue += event.changeType === 'IN' ? quantity : -quantity
    return {
      value: [
        Number(((new Date(event.createTime).getTime() - axisStartTime.value) / HOUR_MS).toFixed(2)),
        runningValue
      ],
      event,
      changeLabel: `${event.changeType === 'IN' ? '+' : '-'}${quantity}`
    }
  })
})

function formatTooltipTime(value) {
  return proxy.parseTime(value, '{y}-{m}-{d} {h}:{i}:{s}')
}

function formatRelativeHour(value) {
  return `+${value}`
}

function formatAxisTick(value) {
  if (axisStartTime.value === null) {
    return ''
  }
  return proxy.parseTime(axisStartTime.value + (value * HOUR_MS), '{m}-{d} {h}:{i}')
}

function renderChart() {
  if (!chartRef.value || !seriesData.value.length) {
    return
  }

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
    chartInstance.on('click', params => {
      const event = params.data?.event
      if (event) {
        emit('eventClick', event)
      }
    })
  }

  chartInstance.setOption({
    grid: {
      left: 48,
      right: 24,
      top: 44,
      bottom: 36
    },
    tooltip: {
      trigger: 'axis',
      formatter: items => {
        const current = items?.[0]?.data
        if (!current?.event) {
          return ''
        }
        const { event, value } = current
        const sign = event.changeType === 'IN' ? '+' : '-'
        return [
          formatTooltipTime(event.createTime),
          `相对起点 ${formatRelativeHour(Math.floor(value[0]))}h`,
          `${event.productName || ''} ${event.warehouseName || ''}`.trim(),
          `${event.changeType === 'IN' ? '入库' : '出库'} ${sign}${event.changeQuantity}`,
          `累计净变化 ${value[1]}`
        ].join('<br/>')
      }
    },
    xAxis: {
      type: 'value',
      min: 0,
      max: axisMaxHour.value,
      interval: 6,
      boundaryGap: false,
      name: '时间轴（小时）',
      axisLabel: {
        color: '#666',
        formatter: value => formatAxisTick(value)
      },
      splitLine: {
        lineStyle: {
          color: '#edf2f7',
          type: 'dashed'
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '累计净变化',
      min: yAxisRange.value.min,
      max: yAxisRange.value.max,
      axisLabel: {
        color: '#666'
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0'
        }
      }
    },
    series: [
      {
        name: '库存变化',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 3,
          color: '#409eff'
        },
        itemStyle: {
          color: params => params.data.event.changeType === 'IN' ? '#10b981' : '#ef4444'
        },
        label: {
          show: true,
          formatter: params => params.data.changeLabel,
          position: 'top',
          distance: 10,
          color: params => params.data.event.changeType === 'IN' ? '#16a34a' : '#dc2626',
          fontSize: 12,
          fontWeight: 700,
          textBorderColor: 'rgba(255, 255, 255, 0.95)',
          textBorderWidth: 3
        },
        labelLayout: {
          hideOverlap: false,
          moveOverlap: 'shiftY'
        },
        areaStyle: {
          color: 'rgba(64, 158, 255, 0.12)'
        },
        encode: {
          x: 0,
          y: 1
        },
        data: seriesData.value
      }
    ]
  })

  chartInstance.resize()
}

function handleResize() {
  chartInstance?.resize()
}

watch(() => [props.events, props.timeUnit], async () => {
  if (!props.events.length) {
    chartInstance?.dispose()
    chartInstance = null
    return
  }
  await nextTick()
  renderChart()
}, { deep: true, immediate: true })

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
  chartInstance = null
})
</script>

<style lang="scss" scoped>
.timeline-chart {
  min-height: 320px;
}

.empty {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.chart-wrapper {
  height: 320px;
}
</style>
