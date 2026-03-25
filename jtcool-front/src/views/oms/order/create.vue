<template>
  <div class="app-container">
    <el-page-header @back="goBack" content="创建订单" />
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" style="margin-top: 20px">
      <el-card>
        <template #header>基本信息</template>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="订单号" prop="orderNo">
              <el-input v-model="form.orderNo" placeholder="系统自动生成，可手动调整">
                <template #append>
                  <el-button @click="form.orderNo = generateOrderNo()">重生成</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户" prop="customerId">
              <el-select v-model="form.customerId" placeholder="选择客户" filterable style="width: 100%">
                <el-option v-for="item in customerOptions" :key="item.customerId" :label="item.customerName" :value="item.customerId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="订单日期" prop="orderDate">
              <el-date-picker v-model="form.orderDate" type="date" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="交货日期" prop="deliveryDate">
              <el-date-picker v-model="form.deliveryDate" type="date" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优惠金额" prop="discountAmount">
              <el-input-number v-model="form.discountAmount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="3" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <el-card style="margin-top: 20px">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center">
            <span>订单明细</span>
            <el-button type="primary" size="small" @click="handleAddItem">添加产品</el-button>
          </div>
        </template>
        <el-table :data="form.orderItems" border>
          <el-table-column label="产品" prop="productId" width="200">
            <template #default="scope">
              <el-select v-model="scope.row.productId" placeholder="选择产品" filterable @change="handleProductChange(scope.$index)">
                <el-option v-for="item in productOptions" :key="item.productId" :label="item.productName" :value="item.productId" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="规格" prop="specification" />
          <el-table-column label="单位" prop="unit" width="80" />
          <el-table-column label="数量" prop="quantity" width="120">
            <template #default="scope">
              <el-input-number v-model="scope.row.quantity" :min="1" @change="calculateItemTotal(scope.$index)" />
            </template>
          </el-table-column>
          <el-table-column label="单价" prop="unitPrice" width="120">
            <template #default="scope">
              <el-input-number v-model="scope.row.unitPrice" :min="0" :precision="2" @change="calculateItemTotal(scope.$index)" />
            </template>
          </el-table-column>
          <el-table-column label="小计" prop="totalPrice" width="120">
            <template #default="scope">¥{{ scope.row.totalPrice }}</template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="scope">
              <el-button link type="danger" @click="handleDeleteItem(scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top: 20px; text-align: right">
          <el-descriptions :column="1" border style="width: 300px; margin-left: auto">
            <el-descriptions-item label="订单总额">¥{{ form.totalAmount }}</el-descriptions-item>
            <el-descriptions-item label="优惠金额">¥{{ form.discountAmount }}</el-descriptions-item>
            <el-descriptions-item label="实付金额">¥{{ form.finalAmount }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-card>

      <div style="margin-top: 20px; text-align: center">
        <el-button type="primary" @click="submitForm">提交</el-button>
        <el-button @click="goBack">取消</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup name="OmsOrderCreate">
import { addOrder } from "@/api/oms/order";
import { listCustomer } from "@/api/oms/customer";
import { listProduct } from "@/api/product/product";
import { useRouter } from "vue-router";

const { proxy } = getCurrentInstance();
const router = useRouter();

const formRef = ref();
const customerOptions = ref([]);
const productOptions = ref([]);

const form = ref({
  orderNo: '',
  customerId: undefined,
  orderDate: new Date(),
  deliveryDate: undefined,
  discountAmount: 0,
  remark: '',
  orderItems: [],
  totalAmount: 0,
  finalAmount: 0
});

const rules = {
  orderNo: [{ required: true, message: "订单号不能为空", trigger: "blur" }],
  customerId: [{ required: true, message: "请选择客户", trigger: "change" }],
  orderDate: [{ required: true, message: "请选择订单日期", trigger: "change" }],
  deliveryDate: [{ required: true, message: "请选择交货日期", trigger: "change" }]
};

function generateOrderNo() {
  const now = new Date();
  const pad = value => String(value).padStart(2, '0');
  const datePart = `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}`;
  const timePart = `${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}`;
  const randomPart = String(Math.floor(Math.random() * 1000)).padStart(3, '0');
  return `SO${datePart}${timePart}${randomPart}`;
}

function handleAddItem() {
  form.value.orderItems.push({
    productId: undefined,
    productCode: '',
    productName: '',
    specification: '',
    unit: '',
    quantity: 1,
    unitPrice: 0,
    totalPrice: 0
  });
}

function handleDeleteItem(index) {
  form.value.orderItems.splice(index, 1);
  calculateTotal();
}

function handleProductChange(index) {
  const item = form.value.orderItems[index];
  const product = productOptions.value.find(p => p.productId === item.productId);
  if (product) {
    item.productCode = product.productCode;
    item.productName = product.productName;
    item.specification = product.specification;
    item.unit = product.unit;
    item.unitPrice = product.salePrice || 0;
    calculateItemTotal(index);
  }
}

function calculateItemTotal(index) {
  const item = form.value.orderItems[index];
  item.totalPrice = (item.quantity * item.unitPrice).toFixed(2);
  calculateTotal();
}

function calculateTotal() {
  form.value.totalAmount = form.value.orderItems.reduce((sum, item) => sum + parseFloat(item.totalPrice || 0), 0).toFixed(2);
  form.value.finalAmount = (form.value.totalAmount - form.value.discountAmount).toFixed(2);
}

watch(() => form.value.discountAmount, calculateTotal);

function submitForm() {
  if (!form.value.orderNo) {
    form.value.orderNo = generateOrderNo();
  }
  formRef.value.validate(valid => {
    if (valid) {
      if (form.value.orderItems.length === 0) {
        proxy.$modal.msgError("请添加订单明细");
        return;
      }
      addOrder(form.value).then(() => {
        proxy.$modal.msgSuccess("创建成功");
        router.push('/oms/order');
      });
    }
  });
}

function goBack() {
  router.back();
}

listCustomer({}).then(response => {
  customerOptions.value = response.rows;
});

listProduct({}).then(response => {
  productOptions.value = response.rows;
});

form.value.orderNo = generateOrderNo();
</script>
