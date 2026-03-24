<template>
  <div class="timeline-chart">
    <div v-if="!events.length" class="empty">暂无数据</div>

    <div v-else class="chart-wrapper">
      <div class="time-axis">
        <span v-for="tick in timeTicks" :key="tick.time" class="tick" :style="{left: tick.position + '%'}">
          {{ formatTick(tick.time) }}
        </span>
      </div>

      <div class="timeline">
        <div class="baseline"></div>
        <div v-for="event in events" :key="event.logId"
             class="event-marker"
             :class="event.changeType"
             :style="{left: getEventPosition(event) + '%'}"
             @click="handleClick(event)">
          <span class="qty-label">{{ event.changeType === 'IN' ? '+' : '-' }}{{ event.changeQuantity }}</span>
          <div class="triangle"></div>
          <div class="tooltip">
            <div>{{ event.changeType === 'IN' ? '入库' : '出库' }}</div>
            <div>{{ event.changeType === 'IN' ? '+' : '-' }}{{ event.changeQuantity }}</div>
            <div>{{ formatTime(event.createTime) }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, getCurrentInstance } from 'vue'
import { getTimeRange } from '@/utils/inventoryAnalysis'

const props = defineProps({
  events: { type: Array, required: true },
  timeUnit: { type: String, default: 'hour' }
})

const emit = defineEmits(['eventClick'])

const { proxy } = getCurrentInstance()

const timeRange = computed(() => getTimeRange(props.events))

const timeTicks = computed(() => {
  const { min, max, span } = timeRange.value
  const count = 5
  return Array.from({ length: count }, (_, i) => ({
    time: min + (span * i / (count - 1)),
    position: (i / (count - 1)) * 100
  }))
})

function getEventPosition(event) {
  const { min, span } = timeRange.value
  const time = new Date(event.createTime).getTime()
  return ((time - min) / span) * 100
}

function formatTick(time) {
  return props.timeUnit === 'hour'
    ? proxy.parseTime(time, '{m}-{d} {h}:{i}')
    : proxy.parseTime(time, '{m}-{d}')
}

function formatTime(value) {
  return proxy.parseTime(value, '{y}-{m}-{d} {h}:{i}')
}

function handleClick(event) {
  emit('eventClick', event)
}
</script>

<style lang="scss" scoped>
.timeline-chart {
  min-height: 80px;
}

.empty {
  text-align: center;
  padding: 20px;
  color: #999;
}

.chart-wrapper {
  position: relative;
}

.time-axis {
  position: relative;
  height: 20px;
  margin-bottom: 10px;

  .tick {
    position: absolute;
    transform: translateX(-50%);
    font-size: 11px;
    color: #666;
  }
}

.timeline {
  position: relative;
  height: 60px;

  .baseline {
    position: absolute;
    top: 50%;
    left: 0;
    right: 0;
    height: 1px;
    background: #ddd;
  }
}

.event-marker {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  cursor: pointer;

  .qty-label {
    position: absolute;
    bottom: 100%;
    left: 50%;
    transform: translateX(-50%);
    font-size: 11px;
    font-weight: 600;
    white-space: nowrap;
    margin-bottom: 2px;
  }

  &.IN .qty-label {
    color: #10b981;
  }

  &.OUT .qty-label {
    color: #ef4444;
  }

  .triangle {
    width: 0;
    height: 0;
    border-left: 8px solid transparent;
    border-right: 8px solid transparent;
  }

  &.IN .triangle {
    border-bottom: 12px solid #10b981;
  }

  &.OUT .triangle {
    border-top: 12px solid #ef4444;
  }

  &:hover .triangle {
    transform: scale(1.2);
  }

  &:hover .tooltip {
    opacity: 1;
    visibility: visible;
  }
}

.tooltip {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.85);
  color: #fff;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  opacity: 0;
  visibility: hidden;
  transition: all 0.2s;
  margin-bottom: 8px;
  z-index: 10;

  div {
    line-height: 1.5;
  }
}
</style>
