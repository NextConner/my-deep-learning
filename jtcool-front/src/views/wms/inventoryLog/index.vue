<template>
  <div class="inventory-analysis">
    <!-- 头部 -->
    <div class="header">
      <h2>库存流水三维分析</h2>
      <div class="controls">
        <el-select v-model="queryParams.warehouseId" placeholder="仓库" clearable size="small" @change="handleQuery">
          <el-option v-for="item in warehouseOptions" :key="item.warehouseId" :label="item.warehouseName" :value="item.warehouseId" />
        </el-select>
        <el-select v-model="queryParams.productId" placeholder="产品" clearable filterable size="small" @change="handleQuery">
          <el-option v-for="item in productOptions" :key="item.productId" :label="item.productName" :value="item.productId" />
        </el-select>
        <el-radio-group v-model="timeUnit" size="small" @change="handleQuery">
          <el-radio-button label="hour">小时</el-radio-button>
          <el-radio-button label="day">天</el-radio-button>
        </el-radio-group>
        <el-date-picker v-model="dateRange" type="daterange" size="small" value-format="YYYY-MM-DD" @change="handleQuery" />
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-cards">
      <el-col :span="6">
        <el-statistic title="总入库" :value="stats.totalIn" suffix="件" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="总出库" :value="stats.totalOut" suffix="件" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="净变化" :value="stats.netChange"
          :value-style="{color: stats.netChange >= 0 ? '#10b981' : '#ef4444'}" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="事件总数" :value="stats.eventCount" suffix="次" />
      </el-col>
    </el-row>

    <!-- 维度选择器 -->
    <DimensionSelector v-model="dimension" />

    <!-- 内容区域 -->
    <div class="content-area" v-loading="loading">
      <ProductDimensionView v-if="dimension === 'product'"
        :groupedData="productGroups"
        :timeUnit="timeUnit"
        @eventClick="showEventDetail" />

      <WarehouseDimensionView v-else-if="dimension === 'warehouse'"
        :groupedData="warehouseGroups"
        :timeUnit="timeUnit"
        @eventClick="showEventDetail" />
    </div>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="WmsInventoryLog">
import { computed, getCurrentInstance, reactive, ref, toRefs } from 'vue'
import { listInventoryLog } from '@/api/wms/inventoryLog'
import { listProduct } from '@/api/product/product'
import { listWarehouse } from '@/api/wms/warehouse'
import { groupByProduct, groupByWarehouse, calculateStats } from '@/utils/inventoryAnalysis'
import DimensionSelector from '@/components/wms/DimensionSelector.vue'
import ProductDimensionView from '@/components/wms/ProductDimensionView.vue'
import WarehouseDimensionView from '@/components/wms/WarehouseDimensionView.vue'

const { proxy } = getCurrentInstance()

const logList = ref([])
const productOptions = ref([])
const warehouseOptions = ref([])
const loading = ref(false)
const total = ref(0)
const dateRange = ref([])
const timeUnit = ref('hour')
const dimension = ref('product')

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 100,
    productId: undefined,
    warehouseId: undefined,
    changeType: undefined
  }
})

const { queryParams } = toRefs(data)

const stats = computed(() => calculateStats(logList.value))
const productGroups = computed(() => groupByProduct(logList.value))
const warehouseGroups = computed(() => groupByWarehouse(logList.value))

function getList() {
  loading.value = true
  const params = { ...queryParams.value }
  if (dateRange.value?.length === 2) {
    params.beginTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }
  listInventoryLog(params).then(response => {
    logList.value = response.rows || []
    total.value = response.total || 0
  }).finally(() => loading.value = false)
}

function getProductOptions() {
  listProduct().then(response => {
    productOptions.value = response.rows || []
  })
}

function getWarehouseOptions() {
  listWarehouse().then(response => {
    warehouseOptions.value = response.rows || []
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function showEventDetail(event) {
  proxy.$modal.msgSuccess(`${event.productName} - ${event.warehouseName}: ${event.changeType === 'IN' ? '+' : '-'}${event.changeQuantity}`)
}

getList()
getProductOptions()
getWarehouseOptions()
</script>

<style lang="scss" scoped>
.inventory-analysis {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
  }

  .controls {
    display: flex;
    gap: 12px;
  }
}

.stats-cards {
  margin-bottom: 20px;
}

.content-area {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  min-height: 400px;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .controls {
    flex-wrap: wrap;
    width: 100%;
  }
}
</style>
