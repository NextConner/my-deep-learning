<template>
  <div class="app-container stock-bill-detail-page">
    <section class="detail-hero">
      <div class="detail-hero__main">
        <el-button text class="detail-hero__back" @click="goBack">返回列表</el-button>
        <span class="detail-hero__eyebrow">WMS Bill Detail</span>
        <h2 class="detail-hero__title">{{ billData.billId ? '单据详情' : '新建单据' }}</h2>
        <p class="detail-hero__desc">
          {{ billData.billId ? '查看并校验单据头信息与明细内容。' : '先确定单据方向和仓库，再补充明细产品与数量。' }}
        </p>
      </div>
      <div class="detail-hero__meta">
        <span>{{ billData.billId ? (billData.billNo || '已生成单据') : '未保存单据' }}</span>
        <el-tag :type="billData.billStatus === 'DRAFT' || !billData.billId ? 'info' : 'success'" effect="light">
          {{ billData.billStatus === 'DRAFT' || !billData.billId ? '草稿' : '已确认' }}
        </el-tag>
      </div>
    </section>

    <section class="detail-summary">
      <article v-for="item in summaryCards" :key="item.key" class="summary-tile">
        <span class="summary-tile__label">{{ item.label }}</span>
        <strong class="summary-tile__value">{{ item.value }}</strong>
        <span class="summary-tile__hint">{{ item.hint }}</span>
      </article>
    </section>

    <section class="detail-panel">
      <div class="panel-heading">
        <div>
          <h3>单据信息</h3>
          <p>单据头决定库存流转方向与仓库归属。</p>
        </div>
      </div>

      <el-descriptions :column="2" border v-if="billData.billId" class="detail-descriptions">
        <el-descriptions-item label="单据号">{{ billData.billNo }}</el-descriptions-item>
        <el-descriptions-item label="单据类型">{{ getBillTypeLabel(billData.billType) }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ billData.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="单据状态">
          <el-tag v-if="billData.billStatus === 'DRAFT'" type="info">草稿</el-tag>
          <el-tag v-else type="success">已确认</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-form v-else :model="billData" label-width="100px" class="detail-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="单据类型">
              <el-select v-model="billData.billType" placeholder="请选择单据类型">
                <el-option label="采购入库" value="IN_PURCHASE" />
                <el-option label="退货入库" value="IN_RETURN" />
                <el-option label="销售出库" value="OUT_SALES" />
                <el-option label="退货出库" value="OUT_RETURN" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="仓库">
              <el-select v-model="billData.warehouseId" placeholder="请选择仓库">
                <el-option v-for="item in warehouseOptions" :key="item.warehouseId" :label="item.warehouseName" :value="item.warehouseId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </section>

    <section class="detail-panel">
      <div class="panel-heading panel-heading--split">
        <div>
          <h3>单据明细</h3>
          <p>逐项维护产品与数量，确认前可继续编辑。</p>
        </div>
        <el-button type="primary" icon="Plus" @click="handleAddItem" v-if="!billData.billId || billData.billStatus === 'DRAFT'">添加明细</el-button>
      </div>

      <el-table :data="billData.items" class="detail-table">
        <el-table-column label="产品" min-width="260">
          <template #default="{ row }">
            <el-select v-model="row.productId" placeholder="选择产品" v-if="!billData.billId || billData.billStatus === 'DRAFT'" filterable>
              <el-option v-for="item in productOptions" :key="item.productId" :label="item.productName" :value="item.productId" />
            </el-select>
            <span v-else>{{ row.productName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="160">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" v-if="!billData.billId || billData.billStatus === 'DRAFT'" />
            <span v-else>{{ row.quantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" v-if="!billData.billId || billData.billStatus === 'DRAFT'">
          <template #default="{ $index }">
            <el-button link type="danger" @click="handleRemoveItem($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="billData.items?.length === 0" description="暂无明细，请先添加产品" />
    </section>

    <div class="detail-footer" v-if="!billData.billId || billData.billStatus === 'DRAFT'">
      <el-button type="primary" @click="handleSave">保存</el-button>
      <el-button @click="goBack">取消</el-button>
    </div>
  </div>
</template>

<script setup name="WmsStockBillDetail">
import { getStockBill, addStockBill, updateStockBill } from "@/api/wms/stockBill";
import { listWarehouse } from "@/api/wms/warehouse";
import { listProduct } from "@/api/product/product";
import { useRoute, useRouter } from "vue-router";

const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter();

const billData = ref({ items: [] });
const warehouseOptions = ref([]);
const productOptions = ref([]);

const billTypeMap = {
  'IN_PURCHASE': '采购入库',
  'IN_RETURN': '退货入库',
  'OUT_SALES': '销售出库',
  'OUT_RETURN': '退货出库'
};

const summaryCards = computed(() => [
  {
    key: 'type',
    label: '单据类型',
    value: billData.value.billType ? getBillTypeLabel(billData.value.billType) : '待选择',
    hint: '决定库存流入或流出'
  },
  {
    key: 'warehouse',
    label: '仓库',
    value: billData.value.warehouseName || getWarehouseName(billData.value.warehouseId) || '待选择',
    hint: '库存变化发生的仓库'
  },
  {
    key: 'items',
    label: '明细数量',
    value: billData.value.items?.length || 0,
    hint: '当前单据的产品行数'
  }
])

function getBillTypeLabel(type) {
  return billTypeMap[type] || type;
}

function getWarehouseName(warehouseId) {
  return warehouseOptions.value.find(item => item.warehouseId === warehouseId)?.warehouseName
}

function getBillDetail() {
  const billId = route.params.billId;
  if (billId && billId !== '0') {
    getStockBill(billId).then(response => {
      billData.value = response.data;
    });
  }
}

function getOptions() {
  listWarehouse().then(response => {
    warehouseOptions.value = response.rows;
  });
  listProduct().then(response => {
    productOptions.value = response.rows;
  });
}

function goBack() {
  router.back();
}

function handleAddItem() {
  billData.value.items.push({
    productId: undefined,
    quantity: 1
  });
}

function handleRemoveItem(index) {
  billData.value.items.splice(index, 1);
}

function handleSave() {
  if (!billData.value.billType || !billData.value.warehouseId) {
    proxy.$modal.msgError("请填写完整信息");
    return;
  }
  if (billData.value.items.length === 0) {
    proxy.$modal.msgError("请添加明细");
    return;
  }

  const promise = billData.value.billId ? updateStockBill(billData.value) : addStockBill(billData.value);
  promise.then(() => {
    proxy.$modal.msgSuccess("保存成功");
    router.back();
  });
}

getBillDetail();
getOptions();
</script>

<style lang="scss" scoped>
.stock-bill-detail-page {
  min-height: calc(100vh - 84px);
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(30, 64, 175, 0.08), transparent 30%),
    linear-gradient(180deg, #f4f7fb 0%, #edf2f8 100%);
}

.detail-hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 28px 32px;
  margin-bottom: 18px;
  border-radius: 24px;
  background: linear-gradient(135deg, #172554 0%, #1e3a8a 55%, #1d4ed8 100%);
  color: #fff;
  box-shadow: 0 24px 60px rgba(23, 37, 84, 0.18);
}

.detail-hero__back {
  padding: 0;
  margin-bottom: 12px;
  color: rgba(219, 234, 254, 0.88);
}

.detail-hero__eyebrow {
  display: inline-block;
  margin-bottom: 10px;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: rgba(219, 234, 254, 0.82);
}

.detail-hero__title {
  margin: 0 0 10px;
  font-size: 32px;
  font-weight: 700;
}

.detail-hero__desc {
  max-width: 560px;
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  color: rgba(219, 234, 254, 0.82);
}

.detail-hero__meta {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  align-items: flex-end;
  gap: 10px;
}

.detail-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 18px;
}

.summary-tile {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 20px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.summary-tile__label {
  font-size: 13px;
  color: #64748b;
}

.summary-tile__value {
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.summary-tile__hint {
  font-size: 12px;
  color: #94a3b8;
}

.detail-panel {
  padding: 22px 24px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.05);
}

.detail-panel + .detail-panel {
  margin-top: 18px;
}

.panel-heading {
  margin-bottom: 18px;

  h3 {
    margin: 0 0 6px;
    font-size: 18px;
    font-weight: 600;
    color: #0f172a;
  }

  p {
    margin: 0;
    font-size: 13px;
    color: #64748b;
  }
}

.panel-heading--split {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
}

.detail-descriptions,
.detail-form,
.detail-table {
  width: 100%;
}

.detail-table :deep(.el-table__header th) {
  background: #f8fafc !important;
}

.detail-footer {
  margin-top: 20px;
  text-align: center;
}

@media (max-width: 768px) {
  .stock-bill-detail-page {
    padding: 16px;
  }

  .detail-hero,
  .panel-heading--split {
    flex-direction: column;
    align-items: flex-start;
  }

  .detail-hero {
    padding: 24px 20px;
  }

  .detail-summary {
    grid-template-columns: 1fr;
  }

  .detail-panel {
    padding: 18px;
    border-radius: 20px;
  }
}
</style>
