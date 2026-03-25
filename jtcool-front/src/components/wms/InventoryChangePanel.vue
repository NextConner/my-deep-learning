<template>
  <section class="change-panel" :class="{ 'is-open': open }">
    <button class="change-panel__summary" type="button" @click="emit('toggle')">
      <div class="change-panel__identity">
        <div class="change-panel__title">{{ title }}</div>
        <div class="change-panel__meta">
          <span>{{ events.length }} 次变更</span>
          <span v-if="lastTime">最近 {{ formatTime(lastTime) }}</span>
        </div>
      </div>

      <div class="change-panel__stats">
        <span class="stat-chip stat-chip--in">入库 +{{ totalIn }}</span>
        <span class="stat-chip stat-chip--out">出库 -{{ totalOut }}</span>
        <span class="stat-chip" :class="netChange >= 0 ? 'stat-chip--positive' : 'stat-chip--negative'">
          净变化 {{ netChange >= 0 ? '+' : '' }}{{ netChange }}
        </span>
        <span class="change-panel__trigger">{{ open ? '收起变化' : '展开查看变化' }}</span>
      </div>
    </button>

    <div v-if="open" class="change-panel__detail">
      <div class="change-panel__chart">
        <TimelineChart :events="sortedEvents" :timeUnit="timeUnit" @eventClick="handleEventClick" />
      </div>

      <div class="change-panel__table">
        <div class="detail-title">最近库存变化</div>
        <el-table :data="recentEvents" size="small" stripe @row-click="handleEventClick">
          <el-table-column label="时间" min-width="170">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="类型" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.changeType === 'IN' ? 'success' : 'danger'" effect="light">
                {{ row.changeType === 'IN' ? '入库' : '出库' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="90" align="center">
            <template #default="{ row }">
              <span :class="row.changeType === 'IN' ? 'qty qty--in' : 'qty qty--out'">
                {{ row.changeType === 'IN' ? '+' : '-' }}{{ row.changeQuantity }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="仓库/产品" min-width="140" :show-overflow-tooltip="true">
            <template #default="{ row }">
              {{ row.warehouseName || row.productName || '--' }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, getCurrentInstance } from 'vue'
import TimelineChart from './TimelineChart.vue'

const props = defineProps({
  title: { type: String, required: true },
  events: { type: Array, default: () => [] },
  timeUnit: { type: String, default: 'hour' },
  open: { type: Boolean, default: false }
})

const emit = defineEmits(['toggle', 'eventClick'])

const { proxy } = getCurrentInstance()

const sortedEvents = computed(() => [...props.events].sort((a, b) => new Date(a.createTime) - new Date(b.createTime)))
const recentEvents = computed(() => [...sortedEvents.value].reverse().slice(0, 6))
const totalIn = computed(() => sortedEvents.value
  .filter(item => item.changeType === 'IN')
  .reduce((sum, item) => sum + Number(item.changeQuantity || 0), 0))
const totalOut = computed(() => sortedEvents.value
  .filter(item => item.changeType !== 'IN')
  .reduce((sum, item) => sum + Number(item.changeQuantity || 0), 0))
const netChange = computed(() => totalIn.value - totalOut.value)
const lastTime = computed(() => recentEvents.value[0]?.createTime)

function formatTime(value) {
  return proxy.parseTime(value, '{y}-{m}-{d} {h}:{i}:{s}')
}

function handleEventClick(event) {
  emit('eventClick', event)
}
</script>

<style lang="scss" scoped>
.change-panel {
  border: 1px solid #e7edf5;
  border-radius: 16px;
  background: linear-gradient(180deg, #fff 0%, #fbfcff 100%);
  overflow: hidden;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;

  &.is-open {
    border-color: #bfd7ff;
    box-shadow: 0 14px 30px rgba(64, 158, 255, 0.08);
  }
}

.change-panel__summary {
  width: 100%;
  border: none;
  background: transparent;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  text-align: left;
  cursor: pointer;
}

.change-panel__identity {
  min-width: 0;
}

.change-panel__title {
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
}

.change-panel__meta {
  margin-top: 6px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  font-size: 12px;
  color: #7b8794;
}

.change-panel__stats {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
}

.stat-chip {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: #f3f4f6;
  color: #4b5563;
  font-size: 12px;
  font-weight: 600;
}

.stat-chip--in {
  background: #ecfdf3;
  color: #047857;
}

.stat-chip--out {
  background: #fef2f2;
  color: #b91c1c;
}

.stat-chip--positive {
  background: #eff6ff;
  color: #1d4ed8;
}

.stat-chip--negative {
  background: #fff7ed;
  color: #c2410c;
}

.change-panel__trigger {
  color: #409eff;
  font-size: 13px;
  font-weight: 600;
}

.change-panel__detail {
  padding: 0 20px 20px;
  border-top: 1px solid #eef2f7;
}

.change-panel__chart {
  padding: 18px 0 8px;
}

.change-panel__table {
  border-top: 1px dashed #e5e7eb;
  padding-top: 16px;
}

.detail-title {
  margin-bottom: 12px;
  font-size: 13px;
  font-weight: 700;
  color: #4b5563;
}

.qty {
  font-weight: 700;
}

.qty--in {
  color: #059669;
}

.qty--out {
  color: #dc2626;
}

@media (max-width: 768px) {
  .change-panel__summary {
    flex-direction: column;
    align-items: flex-start;
  }

  .change-panel__stats {
    justify-content: flex-start;
  }
}
</style>
