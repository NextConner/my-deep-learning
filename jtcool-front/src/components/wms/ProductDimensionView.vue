<template>
  <div class="product-dimension-view">
    <el-collapse v-model="activeNames">
      <el-collapse-item v-for="product in groupedData" :key="product.id" :name="product.id">
        <template #title>
          <div class="product-header">
            <strong>{{ product.name }}</strong>
            <div class="tags">
              <el-tag type="success" size="small">入库 +{{ product.totalIn }}</el-tag>
              <el-tag type="danger" size="small">出库 -{{ product.totalOut }}</el-tag>
              <el-tag type="info" size="small">净变化 {{ product.netChange >= 0 ? '+' : '' }}{{ product.netChange }}</el-tag>
              <el-tag size="small">{{ product.eventCount }} 次</el-tag>
            </div>
          </div>
        </template>

        <div v-for="warehouse in product.warehouses" :key="warehouse.id" class="warehouse-section">
          <h4>{{ warehouse.name }}</h4>
          <TimelineChart :events="warehouse.events" :timeUnit="timeUnit" @eventClick="handleEventClick" />
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
.product-dimension-view {
  background: #fff;
  border-radius: 8px;
}

.product-header {
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

.warehouse-section {
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
