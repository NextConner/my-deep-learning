<template>
  <div class="inventory-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">库存管理</h2>
        <p class="page-desc">实时查看产品库存状态和预警信息</p>
      </div>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="产品" prop="productId">
          <el-select v-model="queryParams.productId" placeholder="选择产品" clearable filterable size="large">
            <el-option v-for="item in productOptions" :key="item.productId" :label="item.productName" :value="item.productId" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="queryParams.warehouseId" placeholder="选择仓库" clearable size="large">
            <el-option v-for="item in warehouseOptions" :key="item.warehouseId" :label="item.warehouseName" :value="item.warehouseId" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card" v-loading="loading">
      <div class="inventory-grid">
        <div v-for="item in inventoryList" :key="item.productId + '-' + item.warehouseId" class="inventory-card">
          <div class="inventory-card__header">
            <div class="inventory-card__icon">
              <el-icon><Box /></el-icon>
            </div>
            <el-tag v-if="item.availableQuantity < item.warningStock" type="danger" size="small">低库存</el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </div>
          <div class="inventory-card__body">
            <h3 class="inventory-card__name">{{ item.productName }}</h3>
            <div class="inventory-card__warehouse">
              <el-icon><OfficeBuilding /></el-icon>
              <span>{{ item.warehouseName }}</span>
            </div>
            <div class="inventory-stats">
              <div class="stat-item">
                <span class="stat-label">总数量</span>
                <strong class="stat-value">{{ item.quantity }}</strong>
              </div>
              <div class="stat-item">
                <span class="stat-label">锁定</span>
                <strong class="stat-value stat-value--locked">{{ item.lockedQuantity }}</strong>
              </div>
              <div class="stat-item">
                <span class="stat-label">可用</span>
                <strong class="stat-value" :class="{'stat-value--warning': item.availableQuantity < item.warningStock}">
                  {{ item.availableQuantity }}
                </strong>
              </div>
            </div>
            <div class="inventory-card__time">更新: {{ parseTime(item.updateTime) }}</div>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && inventoryList.length === 0" description="暂无库存数据" />
    </el-card>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="WmsInventory">
import { listInventory } from "@/api/wms/inventory";
import { listWarehouse } from "@/api/wms/warehouse";
import { listProduct } from "@/api/product/product";
import { Box, OfficeBuilding } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();

const inventoryList = ref([]);
const warehouseOptions = ref([]);
const productOptions = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    productId: undefined,
    warehouseId: undefined
  }
});

const { queryParams } = toRefs(data);

function getList() {
  loading.value = true;
  listInventory(queryParams.value).then(response => {
    inventoryList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function getOptions() {
  listWarehouse().then(response => {
    warehouseOptions.value = response.rows;
  });
  listProduct().then(response => {
    productOptions.value = response.rows;
  });
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

getList();
getOptions();
</script>

<style scoped lang="scss">
.inventory-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  margin: 0 0 6px;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.page-desc {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.search-card {
  margin-bottom: 16px;
  border-radius: 12px;
  border: 0;
}

.table-card {
  border-radius: 12px;
  border: 0;
  margin-bottom: 16px;
}

.inventory-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.inventory-card {
  padding: 20px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #fff;
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
  }
}

.inventory-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.inventory-card__icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);
  color: #fff;
  font-size: 24px;
}

.inventory-card__body {
  margin-bottom: 0;
}

.inventory-card__name {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.inventory-card__warehouse {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 16px;
}

.inventory-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding: 16px 0;
  border-top: 1px solid #f3f4f6;
  border-bottom: 1px solid #f3f4f6;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 6px;
}

.stat-value {
  display: block;
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
}

.stat-value--locked {
  color: #f59e0b;
}

.stat-value--warning {
  color: #ef4444;
}

.inventory-card__time {
  margin-top: 12px;
  font-size: 12px;
  color: #9ca3af;
}
</style>
