<template>
  <div class="app-container stock-bill-page">
    <section class="page-header">
      <div>
        <h2 class="page-title">出入库单据</h2>
        <p class="page-desc">集中查看库存流转单据，先识别待确认草稿，再处理详情、确认与删除动作。</p>
      </div>
      <div class="page-meta">
        <span>当前页 {{ stockBillList.length }} 条</span>
        <span>累计 {{ total }} 条</span>
      </div>
    </section>

    <section class="stock-bill-summary">
      <article v-for="item in summaryCards" :key="item.key" class="summary-tile" :class="`summary-tile--${item.key}`">
        <div class="summary-tile__icon">
          <el-icon><component :is="item.icon" /></el-icon>
        </div>
        <div class="summary-tile__body">
          <span class="summary-tile__label">{{ item.label }}</span>
          <strong class="summary-tile__value">{{ item.value }}</strong>
          <span class="summary-tile__hint">{{ item.hint }}</span>
        </div>
      </article>
    </section>

    <section class="stock-bill-panel" v-show="showSearch">
      <div class="panel-heading">
        <div>
          <h3>筛选条件</h3>
          <p>按单据号和单据类型缩小检索范围。</p>
        </div>
      </div>
      <el-form :model="queryParams" ref="queryRef" :inline="true" class="filter-form">
        <el-form-item label="单据号" prop="billNo">
          <el-input v-model="queryParams.billNo" placeholder="请输入单据号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="单据类型" prop="billType">
          <el-select v-model="queryParams.billType" placeholder="单据类型" clearable>
            <el-option label="采购入库" value="IN_PURCHASE" />
            <el-option label="退货入库" value="IN_RETURN" />
            <el-option label="销售出库" value="OUT_SALES" />
            <el-option label="退货出库" value="OUT_RETURN" />
          </el-select>
        </el-form-item>
        <el-form-item class="filter-form__actions">
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="stock-bill-panel">
      <div class="panel-heading panel-heading--split">
        <div>
          <h3>单据列表</h3>
          <p>突出单据方向、仓库归属和状态，草稿单据优先处理。</p>
        </div>
        <div class="panel-actions">
          <el-button type="primary" icon="Plus" @click="handleAdd">新增单据</el-button>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </div>
      </div>

      <el-table v-loading="loading" :data="stockBillList" class="stock-bill-table">
        <el-table-column label="单据" min-width="220">
          <template #default="{ row }">
            <div class="bill-cell">
              <strong>{{ row.billNo }}</strong>
              <span>{{ parseTime(row.createTime) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="流转方向" min-width="180">
          <template #default="{ row }">
            <div class="direction-cell">
              <el-tag :type="row.billType.startsWith('IN') ? 'success' : 'warning'" effect="light">
                {{ row.billType.startsWith('IN') ? '入库' : '出库' }}
              </el-tag>
              <span>{{ getBillTypeLabel(row.billType) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="仓库" min-width="180">
          <template #default="{ row }">
            <div class="warehouse-cell">
              <strong>{{ row.warehouseName || '未指定仓库' }}</strong>
              <span>{{ row.billStatus === 'DRAFT' ? '待确认库存变更' : '已完成库存变更' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.billStatus === 'DRAFT' ? 'info' : 'success'" effect="light">
              {{ row.billStatus === 'DRAFT' ? '草稿' : '已确认' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" align="right" width="220">
          <template #default="{ row }">
            <div class="action-links">
              <el-button link type="primary" icon="View" @click="handleDetail(row)">详情</el-button>
              <el-button link type="primary" @click="handleConfirm(row)" v-if="row.billStatus === 'DRAFT'">确认</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(row)" v-if="row.billStatus === 'DRAFT'">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && stockBillList.length === 0" description="暂无库存单据" />

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </section>
  </div>
</template>

<script setup name="WmsStockBill">
import { listStockBill, delStockBill, confirmStockBill } from "@/api/wms/stockBill";
import { useRouter } from "vue-router";
import { CircleCheck, Document, Download, Upload } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const router = useRouter();

const stockBillList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    billNo: undefined,
    billType: undefined
  }
});

const { queryParams } = toRefs(data);

const billTypeMap = {
  'IN_PURCHASE': '采购入库',
  'IN_RETURN': '退货入库',
  'IN_OTHER': '其他入库',
  'OUT_SALES': '销售出库',
  'OUT_RETURN': '退货出库',
  'OUT_LOSS': '损耗出库',
  'OUT_OTHER': '其他出库'
};

const summaryCards = computed(() => {
  const inCount = stockBillList.value.filter(item => item.billType?.startsWith('IN')).length
  const outCount = stockBillList.value.filter(item => item.billType?.startsWith('OUT')).length
  const draftCount = stockBillList.value.filter(item => item.billStatus === 'DRAFT').length
  const confirmedCount = stockBillList.value.filter(item => item.billStatus !== 'DRAFT').length

  return [
    { key: 'in', label: '入库单', value: inCount, hint: '当前页入库方向单据', icon: Download },
    { key: 'out', label: '出库单', value: outCount, hint: '当前页出库方向单据', icon: Upload },
    { key: 'draft', label: '待确认', value: draftCount, hint: '确认后才会触发库存变更', icon: Document },
    { key: 'confirmed', label: '已确认', value: confirmedCount, hint: '已完成库存变更处理', icon: CircleCheck }
  ]
})

function getBillTypeLabel(type) {
  return billTypeMap[type] || type;
}

function getList() {
  loading.value = true;
  listStockBill(queryParams.value).then(response => {
    stockBillList.value = response.rows;
    total.value = response.total;
    loading.value = false;
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

function handleAdd() {
  router.push({ path: '/wms/stockBill/detail/0' });
}

function handleDetail(row) {
  router.push({ path: '/wms/stockBill/detail/' + row.billId });
}

function handleConfirm(row) {
  proxy.$modal.confirm('确认后将触发库存变更，是否继续?').then(() => {
    return confirmStockBill(row.billId, proxy.$store.state.user.userId);
  }).then(() => {
    proxy.$modal.msgSuccess("确认成功");
    getList();
  }).catch(() => {});
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除单据号为"' + row.billNo + '"的数据项?').then(() => {
    return delStockBill(row.billId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
</script>

<style lang="scss" scoped>
.stock-bill-page {
  min-height: calc(100vh - 84px);
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(30, 64, 175, 0.08), transparent 30%),
    linear-gradient(180deg, #f4f7fb 0%, #edf2f8 100%);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
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

.page-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
}

.stock-bill-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 18px;
}

.summary-tile {
  display: flex;
  gap: 16px;
  padding: 20px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
  transition: transform 0.25s ease, box-shadow 0.25s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 18px 40px rgba(15, 23, 42, 0.1);
  }
}

.summary-tile__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  font-size: 20px;
  color: #fff;
}

.summary-tile--in .summary-tile__icon {
  background: linear-gradient(135deg, #16a34a 0%, #4ade80 100%);
}

.summary-tile--out .summary-tile__icon {
  background: linear-gradient(135deg, #ea580c 0%, #fb923c 100%);
}

.summary-tile--draft .summary-tile__icon {
  background: linear-gradient(135deg, #64748b 0%, #94a3b8 100%);
}

.summary-tile--confirmed .summary-tile__icon {
  background: linear-gradient(135deg, #2563eb 0%, #60a5fa 100%);
}

.summary-tile__body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.summary-tile__label {
  font-size: 13px;
  color: #64748b;
}

.summary-tile__value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.1;
  color: #0f172a;
}

.summary-tile__hint {
  font-size: 12px;
  color: #94a3b8;
}

.stock-bill-panel {
  padding: 22px 24px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.05);
}

.stock-bill-panel + .stock-bill-panel {
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

.panel-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-form {
  margin-bottom: -18px;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.filter-form__actions :deep(.el-form-item__content) {
  display: flex;
  gap: 10px;
}

.stock-bill-table {
  margin-bottom: 18px;
}

.stock-bill-table :deep(.el-table__header th) {
  background: #f8fafc !important;
  color: #475569;
  font-weight: 600;
}

.stock-bill-table :deep(.el-table__row:hover > td) {
  background: #f6faff !important;
}

.bill-cell,
.warehouse-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.bill-cell strong,
.warehouse-cell strong {
  color: #0f172a;
}

.bill-cell span,
.warehouse-cell span {
  font-size: 12px;
  color: #94a3b8;
}

.direction-cell {
  display: flex;
  flex-direction: column;
  gap: 8px;

  span {
    font-size: 13px;
    color: #475569;
  }
}

.action-links {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  align-items: center;
}

@media (max-width: 1200px) {
  .stock-bill-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .stock-bill-page {
    padding: 16px;
  }

  .page-header,
  .panel-heading--split {
    flex-direction: column;
    align-items: flex-start;
  }

  .page-meta {
    align-items: flex-start;
  }

  .stock-bill-summary {
    grid-template-columns: 1fr;
  }

  .stock-bill-panel {
    padding: 18px;
    border-radius: 20px;
  }

  .panel-actions {
    width: 100%;
    justify-content: space-between;
  }

  .action-links {
    justify-content: flex-start;
  }
}
</style>
