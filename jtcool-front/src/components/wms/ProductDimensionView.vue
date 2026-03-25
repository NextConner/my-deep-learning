<template>
  <div class="product-dimension-view">
    <div v-for="product in groupedData" :key="product.id" class="product-card">
      <button class="product-header" type="button" @click="toggleProduct(product.id)">
        <div class="product-header__main">
          <strong>{{ product.name }}</strong>
          <span class="product-header__meta">{{ product.warehouses.length }} 个仓库发生变更</span>
        </div>
        <div class="tags">
          <el-tag type="success" size="small">入库 +{{ product.totalIn }}</el-tag>
          <el-tag type="danger" size="small">出库 -{{ product.totalOut }}</el-tag>
          <el-tag type="info" size="small">净变化 {{ product.netChange >= 0 ? '+' : '' }}{{ product.netChange }}</el-tag>
          <el-tag size="small">{{ product.eventCount }} 次</el-tag>
          <span class="toggle-text">{{ isExpanded(product.id) ? '收起' : '展开查看产品库存变化' }}</span>
        </div>
      </button>

      <div v-if="isExpanded(product.id)" class="product-body">
        <InventoryChangePanel
          v-for="warehouse in product.warehouses"
          :key="warehouse.id"
          :title="warehouse.name"
          :events="warehouse.events"
          :timeUnit="timeUnit"
          :open="isWarehouseOpen(product.id, warehouse.id)"
          @toggle="toggleWarehouse(product.id, warehouse.id)"
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

const expandedProducts = reactive({})
const expandedWarehouses = reactive({})

function isExpanded(productId) {
  return expandedProducts[productId] === true
}

function toggleProduct(productId) {
  expandedProducts[productId] = !expandedProducts[productId]
}

function getWarehouseKey(productId, warehouseId) {
  return `${productId}-${warehouseId}`
}

function isWarehouseOpen(productId, warehouseId) {
  return expandedWarehouses[getWarehouseKey(productId, warehouseId)] === true
}

function toggleWarehouse(productId, warehouseId) {
  const key = getWarehouseKey(productId, warehouseId)
  expandedWarehouses[key] = !expandedWarehouses[key]
}

function handleEventClick(event) {
  emit('eventClick', event)
}
</script>

<style lang="scss" scoped>
.product-dimension-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-card {
  border: 1px solid #e5ebf3;
  border-radius: 18px;
  background: linear-gradient(180deg, #fff 0%, #f9fbff 100%);
  overflow: hidden;
}

.product-header {
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

.product-header__main {
  display: flex;
  flex-direction: column;
  gap: 6px;

  strong {
    font-size: 16px;
    color: #1f2937;
  }

  .product-header__meta {
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

.product-body {
  padding: 0 20px 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

@media (max-width: 768px) {
  .product-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .tags {
    justify-content: flex-start;
  }
}
</style>
