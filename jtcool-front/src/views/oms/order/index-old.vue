<template>
  <div class="app-container">
    <el-card class="search-card mb8" shadow="never">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
        <el-form-item label="订单号" prop="orderNo">
          <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="queryParams.customerId" placeholder="请选择客户" clearable filterable>
            <el-option v-for="item in customerOptions" :key="item.customerId" :label="item.customerName" :value="item.customerId" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单状态" prop="orderStatus">
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
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div class="table-header-container">
        <div class="left-actions">
          <el-button type="primary" icon="Plus" @click="handleAdd" class="add-btn">新增订单</el-button>
        </div>
        <div class="right-actions">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </div>
      </div>

      <el-table v-loading="loading" :data="orderList" class="custom-table" :header-cell-class-name="headerCellClassName" stripe>
        <el-table-column label="订单号" align="center" prop="orderNo" width="180">
          <template #default="scope">
            <span class="order-no-text">{{ scope.row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="客户名称" align="center" prop="customerName" :show-overflow-tooltip="true" />
        <el-table-column label="订单状态" align="center" prop="orderStatus">
          <template #default="scope">
            <el-tag v-if="scope.row.orderStatus === 'CREATED'" type="info" effect="light" round>已下单</el-tag>
            <el-tag v-else-if="scope.row.orderStatus === 'SALES_CONFIRMED'" effect="light" round>销售确认</el-tag>
            <el-tag v-else-if="scope.row.orderStatus === 'AUDITED'" type="success" effect="light" round>订单审核</el-tag>
            <el-tag v-else-if="scope.row.orderStatus === 'WAREHOUSE_CONFIRMED'" type="primary" effect="light" round>仓库确认</el-tag>
            <el-tag v-else-if="scope.row.orderStatus === 'SHIPPED'" type="warning" effect="light" round>确认发货</el-tag>
            <el-tag v-else-if="scope.row.orderStatus === 'RECEIVED'" type="success" effect="dark" round>客户签收</el-tag>
            <el-tag v-else-if="scope.row.orderStatus === 'REJECTED'" type="danger" effect="dark" round>已拒绝</el-tag>
            <el-tag v-else round>{{ scope.row.orderStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="订单金额" align="center" prop="totalAmount">
          <template #default="scope">
            <span class="amount-text">￥{{ Number(scope.row.totalAmount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实付金额" align="center" prop="finalAmount">
          <template #default="scope">
            <span class="final-amount-text font-bold">￥{{ Number(scope.row.finalAmount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="160">
          <template #default="scope">
            <div class="time-cell">
              <el-icon><Calendar /></el-icon>
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="240" class-name="small-padding fixed-width">
          <template #default="scope">
            <div class="action-btns">
              <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
              <el-divider direction="vertical" v-if="scope.row.orderStatus === 'CREATED'" />
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-if="scope.row.orderStatus === 'CREATED'">修改</el-button>
              <el-divider direction="vertical" v-if="scope.row.orderStatus === 'CREATED'" />
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.orderStatus === 'CREATED'">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <el-dialog :title="title" v-model="open" width="900px" append-to-body custom-class="custom-dialog">
      <el-form ref="orderRef" :model="form" :rules="rules" label-width="100px" class="order-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户名称" prop="customerId">
              <el-select v-model="form.customerId" placeholder="请选择客户" filterable class="w-full">
                <el-option v-for="item in customerOptions" :key="item.customerId" :label="item.customerName" :value="item.customerId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-divider content-position="left">订单明细</el-divider>
            <div class="mb10">
              <el-button type="primary" size="small" icon="Plus" @click="handleAddItem">添加产品</el-button>
            </div>
            <el-table :data="form.items" border style="width: 100%">
              <el-table-column label="产品" prop="productId" min-width="200">
                <template #default="scope">
                  <el-select v-model="scope.row.productId" placeholder="选择产品" filterable class="w-full" @change="handleProductChange(scope.row)">
                    <el-option v-for="item in productOptions" :key="item.productId" :label="item.productName" :value="item.productId" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="数量" prop="quantity" width="150" align="center">
                <template #default="scope">
                  <el-input-number v-model="scope.row.quantity" :min="1" size="small" @change="calculateItemTotal(scope.row)" />
                </template>
              </el-table-column>
              <el-table-column label="单价(元)" prop="unitPrice" width="150" align="center">
                <template #default="scope">
                  <el-input-number v-model="scope.row.unitPrice" :precision="2" :min="0" size="small" @change="calculateItemTotal(scope.row)" />
                </template>
              </el-table-column>
              <el-table-column label="小计(元)" prop="totalPrice" width="120" align="center">
                <template #default="scope">
                  <span class="text-danger font-bold">{{ scope.row.totalPrice }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center">
                <template #default="scope">
                  <el-button link type="danger" icon="Delete" @click="handleRemoveItem(scope.$index)"></el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-col>
          <el-col :span="24" class="mt20">
            <el-form-item label="备注说明">
              <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入订单备注信息" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel" plain>取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="OmsOrder">
import { listOrder, getOrder, delOrder, addOrder, updateOrder } from "@/api/oms/order";
import { listCustomer } from "@/api/oms/customer";
import { listProduct } from "@/api/product/product";
import { useRouter } from "vue-router";

const { proxy } = getCurrentInstance();
const router = useRouter();

const orderList = ref([]);
const customerOptions = ref([]);
const productOptions = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: { items: [] },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    orderNo: undefined,
    customerId: undefined,
    orderStatus: undefined
  },
  rules: {
    customerId: [{ required: true, message: "客户不能为空", trigger: "change" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listOrder(queryParams.value).then(response => {
    orderList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function getCustomerOptions() {
  listCustomer().then(response => {
    customerOptions.value = response.rows;
  });
}

function getProductOptions() {
  listProduct().then(response => {
    productOptions.value = response.rows;
  });
}

function cancel() {
  open.value = false;
  reset();
}

function reset() {
  form.value = {
    orderId: undefined,
    customerId: undefined,
    remark: undefined,
    items: []
  };
  proxy.resetForm("orderRef");
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
  reset();
  open.value = true;
  title.value = "创建订单";
}

function handleUpdate(row) {
  reset();
  getOrder(row.orderId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改订单";
  });
}

function handleDetail(row) {
  router.push({ path: '/oms/order/detail/' + row.orderId });
}

function handleAddItem() {
  form.value.items.push({
    productId: undefined,
    quantity: 1,
    unitPrice: 0,
    totalPrice: 0
  });
}

function handleRemoveItem(index) {
  form.value.items.splice(index, 1);
}

function handleProductChange(row) {
  const product = productOptions.value.find(p => p.productId === row.productId);
  if (product) {
    row.unitPrice = product.standardPrice;
    calculateItemTotal(row);
  }
}

function calculateItemTotal(row) {
  row.totalPrice = (row.quantity * row.unitPrice).toFixed(2);
}

function submitForm() {
  proxy.$refs["orderRef"].validate(valid => {
    if (valid) {
      if (form.value.orderId != undefined) {
        updateOrder(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addOrder(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除订单号为"' + row.orderNo + '"的数据项?').then(function() {
    return delOrder(row.orderId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
getCustomerOptions();
getProductOptions();
</script>

<style scoped>
.app-container {
  padding: 24px;
  background-color: #f8fafc;
  min-height: calc(100vh - 84px);
}

.search-card {
  border-radius: 12px;
  border: none;
  background-color: #ffffff;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.05), 0 1px 2px 0 rgba(0, 0, 0, 0.03) !important;
}

.table-card {
  border-radius: 12px;
  border: none;
  background-color: #ffffff;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03) !important;
}

.table-header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 4px;
}

.left-actions {
  display: flex;
  gap: 12px;
}

.right-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.add-btn {
  border-radius: 8px;
  padding: 10px 24px;
  font-weight: 600;
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 1px;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(64, 158, 255, 0.35);
}

.font-bold {
  font-weight: 600;
}

.order-no-text {
  color: #409eff;
  font-weight: 600;
  background: #ecf5ff;
  padding: 6px 12px;
  border-radius: 8px;
  font-family: 'Courier New', Courier, monospace;
  font-size: 14px;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
}

.order-no-text:hover {
  background: #409eff;
  color: #ffffff;
  cursor: pointer;
}

.amount-text {
  color: #475569;
  font-weight: 600;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.final-amount-text {
  color: #ef4444;
  font-size: 1.2em;
  text-shadow: 0 1px 2px rgba(239, 68, 68, 0.1);
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.time-cell {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #64748b;
  background: #f8fafc;
  padding: 6px 10px;
  border-radius: 8px;
  border: 1px solid #f1f5f9;
  font-size: 13px;
}

.action-btns {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.w-full {
  width: 100%;
}

.mb8 {
  margin-bottom: 16px;
}

.mb10 {
  margin-bottom: 16px;
}

.mt20 {
  margin-top: 24px;
}

:deep(.custom-table) {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #f1f5f9;
}

:deep(.custom-table th.el-table__cell) {
  background-color: #f8fafc !important;
  color: #334155;
  font-weight: 600;
  height: 54px;
  border-bottom: 2px solid #e2e8f0;
}

:deep(.custom-table td.el-table__cell) {
  border-bottom: 1px solid #f1f5f9;
  padding: 12px 0;
}

:deep(.custom-table .el-table__row--striped td.el-table__cell) {
  background-color: #fcfcfd !important;
}

:deep(.custom-table .el-table__row:hover > td.el-table__cell) {
  background-color: #f1f5f9 !important;
}

:deep(.pagination-container) {
  margin-top: 24px;
  padding: 10px 0 0 0;
}

:deep(.custom-dialog) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

:deep(.custom-dialog .el-dialog__header) {
  background-color: #f8fafc;
  margin-right: 0;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
}

:deep(.custom-dialog .el-dialog__title) {
  font-weight: 600;
  color: #0f172a;
  font-size: 18px;
}

:deep(.custom-dialog .el-dialog__body) {
  padding: 30px 24px;
  background-color: #ffffff;
}

:deep(.custom-dialog .el-dialog__footer) {
  background-color: #f8fafc;
  border-top: 1px solid #e2e8f0;
  padding: 16px 24px;
  margin-top: 0;
}
</style>
