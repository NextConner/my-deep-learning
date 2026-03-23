<template>
  <div class="app-container">
    <el-page-header @back="goBack" content="订单详情" />
    <el-card style="margin-top: 20px">
      <template #header>
        <span>订单信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ orderData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ orderData.customerName }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag v-if="orderData.orderStatus === 'CREATED'" type="info">已下单</el-tag>
          <el-tag v-else-if="orderData.orderStatus === 'SALES_CONFIRMED'">销售确认</el-tag>
          <el-tag v-else-if="orderData.orderStatus === 'AUDITED'" type="success">订单审核</el-tag>
          <el-tag v-else-if="orderData.orderStatus === 'SHIPPED'" type="warning">确认发货</el-tag>
          <el-tag v-else-if="orderData.orderStatus === 'RECEIVED'" type="success">客户签收</el-tag>
          <el-tag v-else-if="orderData.orderStatus === 'REJECTED'" type="danger">已拒绝</el-tag>
          <el-tag v-else>{{ orderData.orderStatus }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="销售人员">{{ orderData.salesUserName }}</el-descriptions-item>
        <el-descriptions-item label="订单日期">{{ parseTime(orderData.orderDate, '{y}-{m}-{d}') }}</el-descriptions-item>
        <el-descriptions-item label="交货日期">{{ parseTime(orderData.deliveryDate, '{y}-{m}-{d}') }}</el-descriptions-item>
        <el-descriptions-item label="订单总额">¥{{ orderData.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="优惠金额">¥{{ orderData.discountAmount }}</el-descriptions-item>
        <el-descriptions-item label="实付金额">¥{{ orderData.finalAmount }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(orderData.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ orderData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>订单明细</span>
      </template>
      <el-table :data="orderData.orderItems" border>
        <el-table-column label="产品名称" prop="productName">
          <template #default="scope">
            <el-popover placement="top" :width="300" trigger="hover">
              <template #reference>
                <el-link type="primary" @click="goToProduct(scope.row.productId)">{{ scope.row.productName }}</el-link>
              </template>
              <div>
                <p><strong>产品编码：</strong>{{ scope.row.productCode }}</p>
                <p><strong>规格：</strong>{{ scope.row.specification || '-' }}</p>
                <p><strong>单位：</strong>{{ scope.row.unit }}</p>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column label="产品编码" prop="productCode" />
        <el-table-column label="规格" prop="specification" />
        <el-table-column label="单位" prop="unit" align="center" />
        <el-table-column label="数量" prop="quantity" align="center" />
        <el-table-column label="单价" prop="unitPrice" align="center">
          <template #default="scope">¥{{ scope.row.unitPrice }}</template>
        </el-table-column>
        <el-table-column label="小计" prop="totalPrice" align="center">
          <template #default="scope">¥{{ scope.row.totalPrice }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>订单流程</span>
      </template>
      <el-steps :active="currentStep" finish-status="success" align-center>
        <el-step title="已下单" />
        <el-step title="销售确认" />
        <el-step title="订单审核" />
        <el-step title="仓库确认" />
        <el-step title="登记出库" />
        <el-step title="确认发货" />
        <el-step title="客户签收" />
      </el-steps>
      <div style="margin-top: 20px; text-align: center">
        <el-button type="primary" @click="handleConfirm" v-if="orderData.orderStatus === 'CREATED'">销售确认</el-button>
        <el-button type="success" @click="handleAudit" v-if="orderData.orderStatus === 'SALES_CONFIRMED'">订单审核</el-button>
        <el-button type="warning" @click="handleShip" v-if="orderData.orderStatus === 'OUT_REGISTERED'">确认发货</el-button>
        <el-button type="primary" @click="handleReceive" v-if="orderData.orderStatus === 'SHIPPED'">客户签收</el-button>
        <el-button type="danger" @click="handleReject" v-if="orderData.orderStatus !== 'RECEIVED' && orderData.orderStatus !== 'REJECTED'">拒绝</el-button>
      </div>
    </el-card>

    <el-dialog title="确认发货" v-model="shipDialogVisible" width="500px">
      <el-form :model="shipForm" label-width="100px">
        <el-form-item label="发货图片">
          <image-upload v-model="shipForm.shippingPhotos" :limit="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitShip">确 定</el-button>
        <el-button @click="shipDialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>

    <el-dialog title="拒绝原因" v-model="rejectDialogVisible" width="500px">
      <el-input v-model="rejectReason" type="textarea" rows="4" placeholder="请输入拒绝原因" />
      <template #footer>
        <el-button type="primary" @click="submitReject">确 定</el-button>
        <el-button @click="rejectDialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="OmsOrderDetail">
import { getOrder, confirmOrder, auditOrder, rejectOrder, shipOrder, receiveOrder } from "@/api/oms/order";
import { useRoute, useRouter } from "vue-router";

const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter();

const orderData = ref({});
const currentStep = ref(0);
const shipDialogVisible = ref(false);
const rejectDialogVisible = ref(false);
const shipForm = ref({ shippingPhotos: [] });
const rejectReason = ref("");

const statusStepMap = {
  'CREATED': 0,
  'SALES_CONFIRMED': 1,
  'AUDITED': 2,
  'WAREHOUSE_CONFIRMED': 3,
  'OUT_REGISTERED': 4,
  'SHIPPED': 5,
  'RECEIVED': 6
};

function getOrderDetail() {
  const orderId = route.params.orderId;
  getOrder(orderId).then(response => {
    orderData.value = response.data;
    currentStep.value = statusStepMap[response.data.orderStatus] || 0;
  });
}

function goBack() {
  router.back();
}

function handleConfirm() {
  confirmOrder(orderData.value.orderId).then(() => {
    proxy.$modal.msgSuccess("销售确认成功");
    getOrderDetail();
  });
}

function handleAudit() {
  auditOrder(orderData.value.orderId).then(() => {
    proxy.$modal.msgSuccess("订单审核成功");
    getOrderDetail();
  });
}

function handleShip() {
  shipDialogVisible.value = true;
}

function submitShip() {
  shipOrder(orderData.value.orderId, shipForm.value).then(() => {
    proxy.$modal.msgSuccess("发货成功");
    shipDialogVisible.value = false;
    getOrderDetail();
  });
}

function handleReceive() {
  receiveOrder(orderData.value.orderId).then(() => {
    proxy.$modal.msgSuccess("签收成功");
    getOrderDetail();
  });
}

function handleReject() {
  rejectDialogVisible.value = true;
}

function submitReject() {
  if (!rejectReason.value) {
    proxy.$modal.msgError("请输入拒绝原因");
    return;
  }
  rejectOrder(orderData.value.orderId, { rejectReason: rejectReason.value }).then(() => {
    proxy.$modal.msgSuccess("拒绝成功");
    rejectDialogVisible.value = false;
    getOrderDetail();
  });
}

function goToProduct(productId) {
  router.push({ path: '/product/product', query: { productId } });
}

getOrderDetail();
</script>
