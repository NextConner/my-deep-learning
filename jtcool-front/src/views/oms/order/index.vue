<template>
  <div class="orders-page">
    <section class="hero-panel">
      <div class="hero-copy">
        <p class="hero-kicker">OMS / 订单列表</p>
        <h2 class="hero-title">订单管理</h2>
        <p class="hero-desc">按订单号、客户与状态快速筛选，直接在列表中处理详情、编辑和删除操作。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" class="create-btn" @click="handleAdd">创建订单</el-button>
      </div>
    </section>

    <section class="filter-panel" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" class="filter-form">
        <div class="filter-grid">
          <el-form-item label="订单号" prop="orderNo">
            <el-input v-model="queryParams.orderNo" placeholder="搜索订单号" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="客户" prop="customerId">
            <el-select v-model="queryParams.customerId" placeholder="选择客户" clearable filterable>
              <el-option v-for="item in customerOptions" :key="item.customerId" :label="item.customerName" :value="item.customerId" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="orderStatus">
            <el-select v-model="queryParams.orderStatus" placeholder="订单状态" clearable>
              <el-option label="已下单" value="CREATED" />
              <el-option label="销售确认" value="SALES_CONFIRMED" />
              <el-option label="订单审核" value="AUDITED" />
              <el-option label="仓库确认" value="WAREHOUSE_CONFIRMED" />
              <el-option label="登记出库" value="OUT_REGISTERED" />
              <el-option label="确认发货" value="SHIPPED" />
              <el-option label="客户签收" value="RECEIVED" />
              <el-option label="已拒绝" value="REJECTED" />
            </el-select>
          </el-form-item>
          <div class="filter-actions">
            <el-button type="primary" @click="handleQuery">搜索</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </div>
        </div>
      </el-form>
    </section>

    <section class="summary-strip">
      <div class="summary-item">
        <span class="summary-label">当前页订单</span>
        <strong class="summary-value">{{ orderList.length }}</strong>
      </div>
      <div class="summary-item">
        <span class="summary-label">待推进</span>
        <strong class="summary-value accent">{{ pendingCount }}</strong>
      </div>
      <div class="summary-item">
        <span class="summary-label">已完成</span>
        <strong class="summary-value success">{{ completedCount }}</strong>
      </div>
      <div class="summary-item">
        <span class="summary-label">当前页实付</span>
        <strong class="summary-value amount">¥{{ currentPageAmount }}</strong>
      </div>
    </section>

    <section class="list-panel" v-loading="loading">
      <div class="list-toolbar">
        <div class="toolbar-copy">
          <h3>订单列表</h3>
          <p>列表视图优先展示关键字段，减少跳转和卡片占位。</p>
        </div>
        <div class="toolbar-meta">
          <span class="meta-chip">共 {{ total }} 条</span>
        </div>
      </div>

      <el-table :data="orderList" class="orders-table" row-key="orderId" @row-click="handleDetail">
        <el-table-column label="订单信息" min-width="280">
          <template #default="scope">
            <div class="primary-cell">
              <button class="order-link" @click.stop="handleDetail(scope.row)">{{ scope.row.orderNo }}</button>
              <div class="sub-line">
                <span>{{ scope.row.customerName || '未关联客户' }}</span>
                <span>{{ formatDate(scope.row.createTime) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="150" align="center">
          <template #default="scope">
            <span class="status-pill" :class="`status-${scope.row.orderStatus}`">{{ getStatusText(scope.row.orderStatus) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="订单金额" min-width="160" align="right">
          <template #default="scope">
            <div class="amount-cell">
              <span class="amount-main">¥{{ formatAmount(scope.row.totalAmount) }}</span>
              <span class="amount-sub">原始金额</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="实付金额" min-width="180" align="right">
          <template #default="scope">
            <div class="amount-cell emphasize">
              <span class="amount-main">¥{{ formatAmount(scope.row.finalAmount) }}</span>
              <span class="amount-sub">最终成交</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="scope">
            <div class="action-row">
              <el-button link type="primary" @click.stop="handleDetail(scope.row)">详情</el-button>
              <el-button link type="primary" @click.stop="handleUpdate(scope.row)" v-if="scope.row.orderStatus === 'CREATED'">编辑</el-button>
              <el-button link type="danger" @click.stop="handleDelete(scope.row)" v-if="scope.row.orderStatus === 'CREATED'">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </section>
  </div>
</template>

<script setup name="OmsOrder">
import { computed, getCurrentInstance, reactive, ref, toRefs } from 'vue'
import { delOrder, listOrder } from '@/api/oms/order'
import { listCustomer } from '@/api/oms/customer'
import { useRouter } from 'vue-router'

const { proxy } = getCurrentInstance()
const router = useRouter()

const orderList = ref([])
const customerOptions = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    orderNo: undefined,
    customerId: undefined,
    orderStatus: undefined
  }
})

const { queryParams } = toRefs(data)

const pendingStatuses = ['CREATED', 'SALES_CONFIRMED', 'AUDITED', 'WAREHOUSE_CONFIRMED', 'OUT_REGISTERED', 'SHIPPED']

const pendingCount = computed(() => orderList.value.filter(item => pendingStatuses.includes(item.orderStatus)).length)
const completedCount = computed(() => orderList.value.filter(item => item.orderStatus === 'RECEIVED').length)
const currentPageAmount = computed(() => orderList.value.reduce((sum, item) => sum + Number(item.finalAmount || 0), 0).toFixed(2))

function getList() {
  loading.value = true
  listOrder(queryParams.value).then(response => {
    orderList.value = response.rows || []
    total.value = response.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function getStatusText(status) {
  const statusMap = {
    CREATED: '已下单',
    SALES_CONFIRMED: '销售确认',
    AUDITED: '订单审核',
    WAREHOUSE_CONFIRMED: '仓库确认',
    OUT_REGISTERED: '登记出库',
    SHIPPED: '确认发货',
    RECEIVED: '客户签收',
    REJECTED: '已拒绝'
  }
  return statusMap[status] || status
}

function formatDate(dateStr) {
  if (!dateStr) return '--'
  return proxy.parseTime(dateStr, '{y}-{m}-{d} {h}:{i}')
}

function formatAmount(value) {
  return Number(value || 0).toFixed(2)
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleAdd() {
  router.push('/oms/order/create')
}

function handleDetail(row) {
  router.push(`/oms/order/detail/${row.orderId}`)
}

function handleUpdate(row) {
  router.push(`/oms/order/edit/${row.orderId}`)
}

function handleDelete(row) {
  proxy.$modal.confirm(`确认删除订单"${row.orderNo}"？`).then(() => {
    return delOrder(row.orderId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

listCustomer({}).then(response => {
  customerOptions.value = response.rows || []
})

getList()
</script>

<style scoped>
.orders-page {
  min-height: calc(100vh - 84px);
  padding: 20px;
  background:
    radial-gradient(circle at top left, rgba(64, 158, 255, 0.1), transparent 28%),
    linear-gradient(180deg, #f4f7fb 0%, #eef3f8 100%);
}

.hero-panel,
.filter-panel,
.summary-strip,
.list-panel {
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(215, 224, 234, 0.9);
  border-radius: 18px;
  backdrop-filter: blur(10px);
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.05);
}

.hero-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 24px 28px;
  margin-bottom: 16px;
}

.hero-kicker {
  margin: 0 0 8px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
  color: #409eff;
}

.hero-title {
  margin: 0 0 8px;
  font-size: 28px;
  line-height: 1.1;
  color: #172033;
}

.hero-desc {
  margin: 0;
  font-size: 14px;
  color: #5b6b82;
}

.create-btn {
  min-width: 116px;
  height: 42px;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  box-shadow: 0 10px 18px rgba(64, 158, 255, 0.22);
}

.filter-panel {
  padding: 18px 20px 2px;
  margin-bottom: 16px;
}

.filter-grid {
  display: grid;
  grid-template-columns: 1.1fr 1fr 1fr auto;
  gap: 16px;
  align-items: end;
}

.filter-actions {
  display: flex;
  gap: 10px;
  padding-bottom: 18px;
}

.summary-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0;
  overflow: hidden;
  margin-bottom: 16px;
}

.summary-item {
  padding: 18px 22px;
  border-right: 1px solid #e8eef5;
}

.summary-item:last-child {
  border-right: none;
}

.summary-label {
  display: block;
  margin-bottom: 10px;
  font-size: 12px;
  color: #7a8798;
}

.summary-value {
  font-size: 24px;
  line-height: 1;
  color: #172033;
}

.summary-value.accent {
  color: #409eff;
}

.summary-value.success {
  color: #18a058;
}

.summary-value.amount {
  color: #d9485f;
}

.list-panel {
  padding: 18px 20px 10px;
}

.list-toolbar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.toolbar-copy h3 {
  margin: 0 0 6px;
  font-size: 18px;
  color: #172033;
}

.toolbar-copy p {
  margin: 0;
  font-size: 13px;
  color: #6b7788;
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  background: #eef5ff;
  color: #2f6cb3;
  font-size: 12px;
  font-weight: 600;
}

.primary-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 4px 0;
}

.order-link {
  width: fit-content;
  padding: 0;
  border: none;
  background: transparent;
  color: #1f5eff;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
}

.sub-line {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  font-size: 12px;
  color: #7a8798;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 92px;
  height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.status-CREATED { background: #eef2ff; color: #4f46e5; }
.status-SALES_CONFIRMED { background: #e0f2fe; color: #0369a1; }
.status-AUDITED { background: #ecfdf3; color: #15803d; }
.status-WAREHOUSE_CONFIRMED { background: #fff7e8; color: #b76e00; }
.status-OUT_REGISTERED { background: #fff1f2; color: #be123c; }
.status-SHIPPED { background: #fff3e8; color: #c2410c; }
.status-RECEIVED { background: #ecfdf3; color: #047857; }
.status-REJECTED { background: #fef2f2; color: #b91c1c; }

.amount-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.amount-cell.emphasize .amount-main {
  color: #d9485f;
}

.amount-main {
  font-size: 16px;
  font-weight: 700;
  color: #172033;
}

.amount-sub {
  font-size: 12px;
  color: #8b97a8;
}

.action-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

:deep(.filter-form .el-form-item) {
  margin-bottom: 16px;
}

:deep(.filter-form .el-form-item__label) {
  color: #526174;
  font-weight: 600;
}

:deep(.filter-form .el-input__wrapper),
:deep(.filter-form .el-select__wrapper) {
  min-height: 40px;
  border-radius: 10px;
}

:deep(.orders-table) {
  width: 100%;
  border: 1px solid #e8eef5;
  border-radius: 16px;
  overflow: hidden;
}

:deep(.orders-table th.el-table__cell) {
  height: 48px;
  background: #f7f9fc !important;
  color: #4a5768;
  font-weight: 700;
  border-bottom: 1px solid #e8eef5;
}

:deep(.orders-table td.el-table__cell) {
  padding: 16px 0;
  border-bottom: 1px solid #eef2f6;
}

:deep(.orders-table .el-table__row) {
  cursor: pointer;
  transition: background-color 0.2s ease;
}

:deep(.orders-table .el-table__row:hover > td.el-table__cell) {
  background: #f8fbff !important;
}

:deep(.pagination-container) {
  margin-top: 18px;
  padding-top: 8px;
}

@media (max-width: 1100px) {
  .filter-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .summary-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .summary-item:nth-child(2) {
    border-right: none;
  }

  .summary-item:nth-child(-n + 2) {
    border-bottom: 1px solid #e8eef5;
  }
}

@media (max-width: 768px) {
  .orders-page {
    padding: 14px;
  }

  .hero-panel {
    flex-direction: column;
    align-items: flex-start;
    padding: 20px;
  }

  .hero-title {
    font-size: 24px;
  }

  .filter-grid,
  .summary-strip {
    grid-template-columns: 1fr;
  }

  .summary-item {
    border-right: none;
    border-bottom: 1px solid #e8eef5;
  }

  .summary-item:last-child {
    border-bottom: none;
  }

  .list-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .action-row {
    gap: 6px;
    flex-wrap: wrap;
  }
}
</style>
