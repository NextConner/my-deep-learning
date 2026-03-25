<template>
  <div class="warehouse-dimension-view">
    <div v-for="warehouse in groupedData" :key="warehouse.id" class="warehouse-card">
      <button class="warehouse-header" type="button" @click="toggleWarehouse(warehouse.id)">
        <div class="warehouse-header__main">
          <strong>{{ warehouse.name }}</strong>
          <span class="warehouse-header__meta">{{ warehouse.products.length }} 个产品发生变更</span>
        </div>
        <div class="tags">
          <el-tag type="success" size="small">入库 +{{ warehouse.totalIn }}</el-tag>
          <el-tag type="danger" size="small">出库 -{{ warehouse.totalOut }}</el-tag>
          <el-tag type="info" size="small">净变化 {{ warehouse.netChange >= 0 ? '+' : '' }}{{ warehouse.netChange }}</el-tag>
          <el-tag size="small">{{ warehouse.eventCount }} 次</el-tag>
          <span class="toggle-text">{{ isExpanded(warehouse.id) ? '收起' : '展开查看产品库存变化' }}</span>
        </div>
      </button>

      <div v-if="isExpanded(warehouse.id)" class="warehouse-body">
        <InventoryChangePanel
          v-for="product in warehouse.products"
          :key="product.id"
          :title="product.name"
          :events="product.events"
          :timeUnit="timeUnit"
          :open="isProductOpen(warehouse.id, product.id)"
          @toggle="toggleProduct(warehouse.id, product.id)"
          @eventClick="handleEventClick"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import InventoryChangePanel from './InventoryChangePanel.vue'

defineProps({
  groupedData: { type: Array, required: true },
  timeUnit: { type: String, default: 'hour' }
})

const emit = defineEmits(['eventClick'])

const expandedWarehouses = reactive({})
const expandedProducts = reactive({})

function isExpanded(warehouseId) {
  return expandedWarehouses[warehouseId] === true
}

function toggleWarehouse(warehouseId) {
  expandedWarehouses[warehouseId] = !expandedWarehouses[warehouseId]
}

function getProductKey(warehouseId, productId) {
  return `${warehouseId}-${productId}`
}

function isProductOpen(warehouseId, productId) {
  return expandedProducts[getProductKey(warehouseId, productId)] === true
}

function toggleProduct(warehouseId, productId) {
  const key = getProductKey(warehouseId, productId)
  expandedProducts[key] = !expandedProducts[key]
}

function handleEventClick(event) {
  emit('eventClick', event)
}
</script>

<style lang="scss" scoped>
.warehouse-dimension-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.warehouse-card {
  border: 1px solid #e5ebf3;
  border-radius: 18px;
  background: linear-gradient(180deg, #fff 0%, #f9fbff 100%);
  overflow: hidden;
}

.warehouse-header {
  width: 100%;
  border: none;
  background: transparent;
  padding: 18px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  text-align: left;
}

.warehouse-header__main {
  display: flex;
  flex-direction: column;
  gap: 6px;

  strong {
    font-size: 16px;
    color: #1f2937;
  }

  .warehouse-header__meta {
    font-size: 12px;
    color: #7b8794;
  }
}

.tags {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.toggle-text {
  color: #409eff;
  font-size: 13px;
  font-weight: 600;
}

.warehouse-body {
  padding: 0 20px 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

@media (max-width: 768px) {
  .warehouse-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .tags {
    justify-content: flex-start;
  }
}
</style>
