<template>
  <div class="warehouse-dimension-view">
    <el-collapse v-model="activeNames">
      <el-collapse-item v-for="warehouse in groupedData" :key="warehouse.id" :name="warehouse.id">
        <template #title>
          <div class="warehouse-header">
            <strong>{{ warehouse.name }}</strong>
            <div class="tags">
              <el-tag type="success" size="small">入库 +{{ warehouse.totalIn }}</el-tag>
              <el-tag type="danger" size="small">出库 -{{ warehouse.totalOut }}</el-tag>
              <el-tag type="info" size="small">净变化 {{ warehouse.netChange >= 0 ? '+' : '' }}{{ warehouse.netChange }}</el-tag>
              <el-tag size="small">{{ warehouse.eventCount }} 次</el-tag>
            </div>
          </div>
        </template>

        <div v-for="product in warehouse.products" :key="product.id" class="product-section">
          <h4>{{ product.name }}</h4>
          <TimelineChart :events="product.events" :timeUnit="timeUnit" @eventClick="handleEventClick" />
        </div>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import TimelineChart from './TimelineChart.vue'

defineProps({
  groupedData: { type: Array, required: true },
  timeUnit: { type: String, default: 'hour' }
})

const emit = defineEmits(['eventClick'])

const activeNames = ref([])

function handleEventClick(event) {
  emit('eventClick', event)
}
</script>

<style lang="scss" scoped>
.warehouse-dimension-view {
  background: #fff;
  border-radius: 8px;
}

.warehouse-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 16px;

  strong {
    font-size: 15px;
  }

  .tags {
    display: flex;
    gap: 8px;
  }
}

.product-section {
  padding: 16px;
  border-top: 1px solid #f0f0f0;

  &:first-child {
    border-top: none;
  }

  h4 {
    margin: 0 0 12px;
    font-size: 14px;
    color: #666;
  }
}
</style>
