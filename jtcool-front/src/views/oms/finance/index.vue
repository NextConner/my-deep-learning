<template>
  <div class="app-container finance-page">
    <section class="finance-hero">
      <div class="finance-hero__main">
        <span class="finance-hero__eyebrow">OMS Finance Workspace</span>
        <h2 class="finance-hero__title">应收应付</h2>
        <p class="finance-hero__desc">
          聚焦当前页财务记录的回款、付款与开票节奏，先看风险，再处理动作。
        </p>
      </div>
      <div class="finance-hero__meta">
        <span>当前页 {{ financeList.length }} 条记录</span>
        <span>累计 {{ total }} 条</span>
      </div>
    </section>

    <section class="finance-summary">
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

    <section class="finance-panel finance-panel--filters" v-show="showSearch">
      <div class="panel-heading">
        <div>
          <h3>筛选条件</h3>
          <p>按订单、财务类型与支付状态快速缩小处理范围。</p>
        </div>
      </div>
      <el-form :model="queryParams" ref="queryRef" :inline="true" class="filter-form">
        <el-form-item label="订单号" prop="orderNo">
          <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="财务类型" prop="financeType">
          <el-select v-model="queryParams.financeType" placeholder="财务类型" clearable>
            <el-option label="应收" value="RECEIVABLE" />
            <el-option label="应付" value="PAYABLE" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态" prop="paymentStatus">
          <el-select v-model="queryParams.paymentStatus" placeholder="支付状态" clearable>
            <el-option label="未支付" value="UNPAID" />
            <el-option label="部分支付" value="PARTIAL_PAID" />
            <el-option label="已支付" value="PAID" />
          </el-select>
        </el-form-item>
        <el-form-item class="filter-form__actions">
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="finance-panel finance-panel--table">
      <div class="panel-heading panel-heading--split">
        <div>
          <h3>财务记录</h3>
          <p>金额、支付进度和开票状态集中展示，方便逐笔判断下一步动作。</p>
        </div>
        <div class="panel-actions">
          <el-button type="primary" icon="Plus" @click="handleAdd">新增记录</el-button>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </div>
      </div>

      <el-table v-loading="loading" :data="financeList" class="finance-table">
        <el-table-column label="订单信息" min-width="220">
          <template #default="{ row }">
            <div class="order-cell">
              <strong>{{ row.orderNo }}</strong>
              <span>ID: {{ row.financeId }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="类型 / 状态" min-width="180">
          <template #default="{ row }">
            <div class="status-stack">
              <el-tag :type="row.financeType === 'RECEIVABLE' ? 'success' : 'warning'" effect="light">
                {{ getFinanceTypeLabel(row.financeType) }}
              </el-tag>
              <el-tag :type="getPaymentStatusType(row.paymentStatus)" effect="light">
                {{ getPaymentStatusLabel(row.paymentStatus) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="金额概览" min-width="300">
          <template #default="{ row }">
            <div class="amount-cell">
              <div class="amount-cell__row">
                <span>总金额</span>
                <strong>{{ formatAmount(row.totalAmount) }}</strong>
              </div>
              <div class="amount-cell__row amount-cell__row--sub">
                <span>已付 {{ formatAmount(row.paidAmount) }}</span>
                <span>未付 {{ formatAmount(row.unpaidAmount) }}</span>
              </div>
              <el-progress
                :percentage="getPaymentProgress(row)"
                :stroke-width="8"
                :show-text="false"
                :color="getProgressColor(row.paymentStatus)"
              />
            </div>
          </template>
        </el-table-column>

        <el-table-column label="开票状态" min-width="160">
          <template #default="{ row }">
            <div class="invoice-cell">
              <el-tag :type="row.invoiceStatus === 'NOT_INVOICED' ? 'info' : 'success'" effect="light">
                {{ row.invoiceStatus === 'NOT_INVOICED' ? '未开票' : '已开票' }}
              </el-tag>
              <span>{{ row.invoiceStatus === 'NOT_INVOICED' ? '待补充发票信息' : '发票已登记完成' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" align="right" width="180" class-name="small-padding fixed-width">
          <template #default="{ row }">
            <div class="action-links">
              <el-button link type="primary" @click="handlePayment(row)" v-if="row.paymentStatus !== 'PAID'">收付款</el-button>
              <el-button link type="primary" @click="handleInvoice(row)" v-if="row.invoiceStatus === 'NOT_INVOICED'">开票</el-button>
              <span class="action-links__done" v-if="row.paymentStatus === 'PAID' && row.invoiceStatus !== 'NOT_INVOICED'">已完成</span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && financeList.length === 0" description="暂无财务记录" />

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </section>

    <el-dialog :title="title" v-model="addDialogVisible" width="640px" class="finance-dialog">
      <div class="dialog-intro">
        <span>创建新的应收或应付记录，金额可按订单自动带出后再调整。</span>
      </div>
      <el-form :model="addForm" :rules="addRules" ref="addRef" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="选择订单" prop="orderId">
              <el-select v-model="addForm.orderId" placeholder="请选择订单" filterable @change="handleOrderChange">
                <el-option
                  v-for="order in orderList"
                  :key="order.orderId"
                  :label="order.orderNo + ' - ' + order.customerName"
                  :value="order.orderId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="财务类型" prop="financeType">
              <el-radio-group v-model="addForm.financeType">
                <el-radio label="RECEIVABLE">应收</el-radio>
                <el-radio label="PAYABLE">应付</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总金额" prop="totalAmount">
              <el-input-number v-model="addForm.totalAmount" :precision="2" :min="0" :controls="false" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAdd">确 定</el-button>
        <el-button @click="addDialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>

    <el-dialog title="添加收付款记录" v-model="paymentDialogVisible" width="560px" class="finance-dialog">
      <div class="dialog-intro dialog-intro--accent">
        <span>当前未付金额 {{ formatAmount(currentFinance.unpaidAmount) }}，建议按实际到账或付款金额登记。</span>
      </div>
      <el-form :model="paymentForm" :rules="paymentRules" ref="paymentRef" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="收付款金额" prop="paymentAmount">
              <el-input-number
                v-model="paymentForm.paymentAmount"
                :precision="2"
                :min="0"
                :max="currentFinance.unpaidAmount"
                :controls="false"
                class="w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收付款方式" prop="paymentMethod">
              <el-select v-model="paymentForm.paymentMethod" placeholder="请选择收付款方式">
                <el-option label="现金" value="CASH" />
                <el-option label="银行转账" value="BANK_TRANSFER" />
                <el-option label="支付宝" value="ALIPAY" />
                <el-option label="微信" value="WECHAT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="paymentForm.remark" type="textarea" :rows="4" placeholder="补充到账说明、付款备注或对账信息" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitPayment">确 定</el-button>
        <el-button @click="paymentDialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>

    <el-dialog title="更新发票信息" v-model="invoiceDialogVisible" width="560px" class="finance-dialog">
      <div class="dialog-intro">
        <span>完善开票编号与开票日期，便于财务流转留痕。</span>
      </div>
      <el-form :model="invoiceForm" :rules="invoiceRules" ref="invoiceRef" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="发票号" prop="invoiceNo">
              <el-input v-model="invoiceForm.invoiceNo" placeholder="请输入发票号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开票日期" prop="invoiceDate">
              <el-date-picker v-model="invoiceForm.invoiceDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitInvoice">确 定</el-button>
        <el-button @click="invoiceDialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="OmsFinance">
import { listFinance, addFinance, addPayment, updateInvoice } from "@/api/oms/finance";
import { listOrder } from "@/api/oms/order";
import { CollectionTag, CreditCard, DocumentChecked, Wallet } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();

const financeList = ref([]);
const orderList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const title = ref("");
const addDialogVisible = ref(false);
const paymentDialogVisible = ref(false);
const invoiceDialogVisible = ref(false);
const currentFinance = ref({});

const summaryCards = computed(() => {
  const receivableCount = financeList.value.filter(item => item.financeType === 'RECEIVABLE').length;
  const payableCount = financeList.value.filter(item => item.financeType === 'PAYABLE').length;
  const pendingAmount = financeList.value.reduce((sum, item) => sum + Number(item.unpaidAmount || 0), 0);
  const invoicedCount = financeList.value.filter(item => item.invoiceStatus !== 'NOT_INVOICED').length;

  return [
    {
      key: 'receivable',
      label: '应收笔数',
      value: receivableCount,
      hint: '当前页待跟进回款记录',
      icon: Wallet
    },
    {
      key: 'payable',
      label: '应付笔数',
      value: payableCount,
      hint: '当前页待安排付款记录',
      icon: CreditCard
    },
    {
      key: 'pending',
      label: '待处理金额',
      value: formatAmount(pendingAmount),
      hint: '当前页未结清金额合计',
      icon: CollectionTag
    },
    {
      key: 'invoice',
      label: '已开票数',
      value: invoicedCount,
      hint: '已完成开票登记的记录',
      icon: DocumentChecked
    }
  ];
});

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    orderNo: undefined,
    financeType: undefined,
    paymentStatus: undefined
  },
  addForm: {},
  paymentForm: {},
  invoiceForm: {},
  addRules: {
    orderId: [{ required: true, message: "请选择订单", trigger: "change" }],
    financeType: [{ required: true, message: "请选择财务类型", trigger: "change" }],
    totalAmount: [{ required: true, message: "总金额不能为空", trigger: "blur" }]
  },
  paymentRules: {
    paymentAmount: [{ required: true, message: "收付款金额不能为空", trigger: "blur" }],
    paymentMethod: [{ required: true, message: "收付款方式不能为空", trigger: "change" }]
  },
  invoiceRules: {
    invoiceNo: [{ required: true, message: "发票号不能为空", trigger: "blur" }],
    invoiceDate: [{ required: true, message: "开票日期不能为空", trigger: "change" }]
  }
});

const { queryParams, addForm, paymentForm, invoiceForm, addRules, paymentRules, invoiceRules } = toRefs(data);

function formatAmount(value) {
  return `¥${Number(value || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })}`;
}

function getFinanceTypeLabel(type) {
  return type === 'RECEIVABLE' ? '应收' : '应付';
}

function getPaymentStatusLabel(status) {
  if (status === 'UNPAID') return '未支付';
  if (status === 'PARTIAL_PAID') return '部分支付';
  return '已支付';
}

function getPaymentStatusType(status) {
  if (status === 'UNPAID') return 'danger';
  if (status === 'PARTIAL_PAID') return 'warning';
  return 'success';
}

function getPaymentProgress(row) {
  const totalAmount = Number(row.totalAmount || 0);
  const paidAmount = Number(row.paidAmount || 0);
  if (!totalAmount) return 0;
  return Math.min(100, Math.round((paidAmount / totalAmount) * 100));
}

function getProgressColor(status) {
  if (status === 'UNPAID') return '#f87171';
  if (status === 'PARTIAL_PAID') return '#f59e0b';
  return '#34c759';
}

function getList() {
  loading.value = true;
  listFinance(queryParams.value).then(response => {
    financeList.value = response.rows;
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
  title.value = "新增应收应付";
  addForm.value = {
    orderId: undefined,
    financeType: 'RECEIVABLE',
    totalAmount: 0
  };
  getOrderList();
  addDialogVisible.value = true;
}

function getOrderList() {
  listOrder({ pageNum: 1, pageSize: 100 }).then(response => {
    orderList.value = response.rows;
  });
}

function handleOrderChange(orderId) {
  const order = orderList.value.find(o => o.orderId === orderId);
  if (order) {
    addForm.value.totalAmount = order.finalAmount;
  }
}

function submitAdd() {
  proxy.$refs["addRef"].validate(valid => {
    if (valid) {
      addFinance(addForm.value).then(() => {
        proxy.$modal.msgSuccess("新增成功");
        addDialogVisible.value = false;
        getList();
      });
    }
  });
}

function handlePayment(row) {
  currentFinance.value = row;
  paymentForm.value = {
    financeId: row.financeId,
    orderId: row.orderId,
    paymentAmount: 0,
    paymentMethod: undefined,
    remark: undefined
  };
  paymentDialogVisible.value = true;
}

function submitPayment() {
  proxy.$refs["paymentRef"].validate(valid => {
    if (valid) {
      addPayment(paymentForm.value).then(() => {
        proxy.$modal.msgSuccess("添加成功");
        paymentDialogVisible.value = false;
        getList();
      });
    }
  });
}

function handleInvoice(row) {
  invoiceForm.value = {
    financeId: row.financeId,
    invoiceNo: undefined,
    invoiceDate: undefined
  };
  invoiceDialogVisible.value = true;
}

function submitInvoice() {
  proxy.$refs["invoiceRef"].validate(valid => {
    if (valid) {
      updateInvoice(invoiceForm.value).then(() => {
        proxy.$modal.msgSuccess("更新成功");
        invoiceDialogVisible.value = false;
        getList();
      });
    }
  });
}

getList();
</script>

<style lang="scss" scoped>
.finance-page {
  min-height: calc(100vh - 84px);
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(15, 23, 42, 0.06), transparent 32%),
    linear-gradient(180deg, #f4f7fb 0%, #eef2f7 100%);
}

.finance-hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 28px 32px;
  margin-bottom: 18px;
  border-radius: 24px;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 55%, #334155 100%);
  color: #fff;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.18);
}

.finance-hero__eyebrow {
  display: inline-block;
  margin-bottom: 10px;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: rgba(226, 232, 240, 0.82);
}

.finance-hero__title {
  margin: 0 0 10px;
  font-size: 34px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.finance-hero__desc {
  max-width: 560px;
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  color: rgba(226, 232, 240, 0.82);
}

.finance-hero__meta {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  align-items: flex-end;
  gap: 10px;
  min-width: 180px;
  font-size: 13px;
  color: rgba(226, 232, 240, 0.72);
}

.finance-summary {
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
  background: rgba(255, 255, 255, 0.9);
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

.summary-tile--receivable .summary-tile__icon {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
}

.summary-tile--payable .summary-tile__icon {
  background: linear-gradient(135deg, #f59e0b 0%, #fb923c 100%);
}

.summary-tile--pending .summary-tile__icon {
  background: linear-gradient(135deg, #ef4444 0%, #f97316 100%);
}

.summary-tile--invoice .summary-tile__icon {
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
}

.summary-tile__body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
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

.finance-panel {
  padding: 22px 24px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.05);
}

.finance-panel + .finance-panel {
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

.finance-table {
  margin-bottom: 18px;
}

.finance-table :deep(.el-table__header th) {
  background: #f8fafc !important;
  color: #475569;
  font-weight: 600;
}

.finance-table :deep(.el-table__row) {
  transition: background-color 0.2s ease;
}

.finance-table :deep(.el-table__row:hover > td) {
  background: #f8fbff !important;
}

.order-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;

  strong {
    font-size: 15px;
    color: #0f172a;
  }

  span {
    font-size: 12px;
    color: #94a3b8;
  }
}

.status-stack {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.amount-cell {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.amount-cell__row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  font-size: 13px;
  color: #475569;

  strong {
    font-size: 18px;
    color: #0f172a;
  }
}

.amount-cell__row--sub {
  color: #64748b;
}

.invoice-cell {
  display: flex;
  flex-direction: column;
  gap: 8px;

  span {
    font-size: 12px;
    color: #94a3b8;
  }
}

.action-links {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  align-items: center;
}

.action-links__done {
  font-size: 12px;
  color: #94a3b8;
}

.finance-dialog :deep(.el-dialog) {
  border-radius: 24px;
  overflow: hidden;
}

.finance-dialog :deep(.el-dialog__header) {
  padding: 22px 24px 14px;
}

.finance-dialog :deep(.el-dialog__body) {
  padding: 0 24px 20px;
}

.finance-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px 24px;
}

.dialog-intro {
  margin-bottom: 18px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #f8fafc;
  font-size: 13px;
  color: #64748b;
}

.dialog-intro--accent {
  background: rgba(59, 130, 246, 0.08);
  color: #1d4ed8;
}

.w-full {
  width: 100%;
}

@media (max-width: 1200px) {
  .finance-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .finance-page {
    padding: 16px;
  }

  .finance-hero,
  .panel-heading--split {
    flex-direction: column;
    align-items: flex-start;
  }

  .finance-hero {
    padding: 24px 20px;
  }

  .finance-hero__title {
    font-size: 28px;
  }

  .finance-hero__meta {
    align-items: flex-start;
  }

  .finance-summary {
    grid-template-columns: 1fr;
  }

  .finance-panel {
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
