<template>
  <div class="orders-container">
    <!-- Header Section -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">订单管理</h1>
        <p class="page-subtitle">Order Management System</p>
      </div>
      <el-button type="primary" class="create-btn" @click="handleAdd">
        <span class="btn-icon">+</span>
        创建订单
      </el-button>
    </div>

    <!-- Search Filters -->
    <div class="search-section" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" class="search-form">
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
          <div class="search-actions">
            <el-button type="primary" @click="handleQuery">搜索</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </div>
        </div>
      </el-form>
    </div>

    <!-- Orders Grid -->
    <div class="orders-grid" v-loading="loading">
      <div class="order-card" v-for="order in orderList" :key="order.orderId" @click="handleDetail(order)">
        <div class="card-header">
          <div class="order-number">{{ order.orderNo }}</div>
          <div class="order-status" :class="`status-${order.orderStatus}`">
            {{ getStatusText(order.orderStatus) }}
          </div>
        </div>

        <div class="card-body">
          <div class="customer-info">
            <div class="customer-name">{{ order.customerName }}</div>
            <div class="order-date">{{ formatDate(order.createTime) }}</div>
          </div>

          <div class="amount-section">
            <div class="amount-label">实付金额</div>
            <div class="amount-value">¥{{ Number(order.finalAmount).toFixed(2) }}</div>
          </div>
        </div>

        <div class="card-footer">
          <el-button link type="primary" @click.stop="handleDetail(order)">查看详情</el-button>
          <el-button link type="primary" @click.stop="handleUpdate(order)" v-if="order.orderStatus === 'CREATED'">编辑</el-button>
          <el-button link type="danger" @click.stop="handleDelete(order)" v-if="order.orderStatus === 'CREATED'">删除</el-button>
        </div>
      </div>
    </div>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="OmsOrder">
import { listOrder, getOrder, delOrder, addOrder, updateOrder } from "@/api/oms/order";
import { listCustomer } from "@/api/oms/customer";
import { useRouter } from "vue-router";

const { proxy } = getCurrentInstance();
const router = useRouter();

const orderList = ref([]);
const customerOptions = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    orderNo: undefined,
    customerId: undefined,
    orderStatus: undefined
  }
});

const { queryParams } = toRefs(data);

function getList() {
  loading.value = true;
  listOrder(queryParams.value).then(response => {
    orderList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function getStatusText(status) {
  const statusMap = {
    'CREATED': '已下单',
    'SALES_CONFIRMED': '销售确认',
    'AUDITED': '订单审核',
    'WAREHOUSE_CONFIRMED': '仓库确认',
    'OUT_REGISTERED': '登记出库',
    'SHIPPED': '确认发货',
    'RECEIVED': '客户签收',
    'REJECTED': '已拒绝'
  };
  return statusMap[status] || status;
}

function formatDate(dateStr) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`;
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
  router.push('/oms/order/create');
}

function handleDetail(row) {
  router.push(`/oms/order/detail/${row.orderId}`);
}

function handleUpdate(row) {
  router.push(`/oms/order/edit/${row.orderId}`);
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除订单"' + row.orderNo + '"？').then(() => {
    return delOrder(row.orderId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

listCustomer({}).then(response => {
  customerOptions.value = response.rows;
});

getList();
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@700;900&family=Inter:wght@400;500;600&display=swap');

.orders-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8eef5 100%);
  padding: 2rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 3rem;
  animation: slideDown 0.6s ease-out;
}

.header-content {
  flex: 1;
}

.page-title {
  font-family: 'Playfair Display', serif;
  font-size: 3.5rem;
  font-weight: 900;
  color: #1a1a2e;
  margin: 0;
  letter-spacing: -0.02em;
  line-height: 1;
}

.page-subtitle {
  font-family: 'Inter', sans-serif;
  font-size: 0.875rem;
  color: #6b7280;
  margin-top: 0.5rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.create-btn {
  height: 3.5rem;
  padding: 0 2rem;
  background: #1a1a2e;
  border: none;
  border-radius: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  transition: all 0.3s ease;
}

.create-btn:hover {
  background: #16213e;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(26, 26, 46, 0.3);
}

.btn-icon {
  font-size: 1.5rem;
  margin-right: 0.5rem;
}

.search-section {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  animation: fadeIn 0.6s ease-out 0.1s both;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  align-items: end;
}

.search-actions {
  display: flex;
  gap: 0.75rem;
}

.orders-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.order-card {
  background: white;
  border-radius: 1rem;
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  animation: fadeInUp 0.6s ease-out both;
}

.order-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
  border-color: #1a1a2e;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f0f0f0;
}

.order-number {
  font-family: 'Inter', sans-serif;
  font-size: 1.125rem;
  font-weight: 600;
  color: #1a1a2e;
  letter-spacing: -0.01em;
}

.order-status {
  padding: 0.375rem 0.875rem;
  border-radius: 2rem;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.status-CREATED { background: #e0e7ff; color: #4338ca; }
.status-SALES_CONFIRMED { background: #dbeafe; color: #1e40af; }
.status-AUDITED { background: #d1fae5; color: #065f46; }
.status-WAREHOUSE_CONFIRMED { background: #fef3c7; color: #92400e; }
.status-SHIPPED { background: #fed7aa; color: #9a3412; }
.status-RECEIVED { background: #bbf7d0; color: #14532d; }
.status-REJECTED { background: #fecaca; color: #991b1b; }

.card-body {
  margin-bottom: 1.25rem;
}

.customer-info {
  margin-bottom: 1.5rem;
}

.customer-name {
  font-size: 1rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.25rem;
}

.order-date {
  font-size: 0.875rem;
  color: #9ca3af;
}

.amount-section {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  padding: 1rem;
  border-radius: 0.75rem;
}

.amount-label {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 0.25rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.amount-value {
  font-family: 'Playfair Display', serif;
  font-size: 1.75rem;
  font-weight: 700;
  color: white;
}

.card-footer {
  display: flex;
  gap: 0.75rem;
  padding-top: 1rem;
  border-top: 1px solid #f0f0f0;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>



