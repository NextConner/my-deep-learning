<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
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
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="financeList">
      <el-table-column label="订单号" align="center" prop="orderNo" width="180" />
      <el-table-column label="财务类型" align="center" prop="financeType">
        <template #default="scope">
          <el-tag v-if="scope.row.financeType === 'RECEIVABLE'" type="success">应收</el-tag>
          <el-tag v-else type="warning">应付</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="总金额" align="center" prop="totalAmount" />
      <el-table-column label="已付金额" align="center" prop="paidAmount" />
      <el-table-column label="未付金额" align="center" prop="unpaidAmount" />
      <el-table-column label="支付状态" align="center" prop="paymentStatus">
        <template #default="scope">
          <el-tag v-if="scope.row.paymentStatus === 'UNPAID'" type="danger">未支付</el-tag>
          <el-tag v-else-if="scope.row.paymentStatus === 'PARTIAL_PAID'" type="warning">部分支付</el-tag>
          <el-tag v-else type="success">已支付</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发票状态" align="center" prop="invoiceStatus">
        <template #default="scope">
          <el-tag v-if="scope.row.invoiceStatus === 'NOT_INVOICED'" type="info">未开票</el-tag>
          <el-tag v-else type="success">已开票</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" @click="handlePayment(scope.row)" v-if="scope.row.paymentStatus !== 'PAID'">收付款</el-button>
          <el-button link type="primary" @click="handleInvoice(scope.row)" v-if="scope.row.invoiceStatus === 'NOT_INVOICED'">开票</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="addDialogVisible" width="600px">
      <el-form :model="addForm" :rules="addRules" ref="addRef" label-width="100px">
        <el-form-item label="选择订单" prop="orderId">
          <el-select v-model="addForm.orderId" placeholder="请选择订单" filterable @change="handleOrderChange">
            <el-option v-for="order in orderList" :key="order.orderId" :label="order.orderNo + ' - ' + order.customerName" :value="order.orderId" />
          </el-select>
        </el-form-item>
        <el-form-item label="财务类型" prop="financeType">
          <el-radio-group v-model="addForm.financeType">
            <el-radio label="RECEIVABLE">应收</el-radio>
            <el-radio label="PAYABLE">应付</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="总金额" prop="totalAmount">
          <el-input-number v-model="addForm.totalAmount" :precision="2" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAdd">确 定</el-button>
        <el-button @click="addDialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>

    <el-dialog title="添加收付款记录" v-model="paymentDialogVisible" width="500px">
      <el-form :model="paymentForm" :rules="paymentRules" ref="paymentRef" label-width="100px">
        <el-form-item label="收付款金额" prop="paymentAmount">
          <el-input-number v-model="paymentForm.paymentAmount" :precision="2" :min="0" :max="currentFinance.unpaidAmount" />
        </el-form-item>
        <el-form-item label="收付款方式" prop="paymentMethod">
          <el-select v-model="paymentForm.paymentMethod" placeholder="请选择收付款方式">
            <el-option label="现金" value="CASH" />
            <el-option label="银行转账" value="BANK_TRANSFER" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="微信" value="WECHAT" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="paymentForm.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitPayment">确 定</el-button>
        <el-button @click="paymentDialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>

    <el-dialog title="更新发票信息" v-model="invoiceDialogVisible" width="500px">
      <el-form :model="invoiceForm" :rules="invoiceRules" ref="invoiceRef" label-width="100px">
        <el-form-item label="发票号" prop="invoiceNo">
          <el-input v-model="invoiceForm.invoiceNo" placeholder="请输入发票号" />
        </el-form-item>
        <el-form-item label="开票日期" prop="invoiceDate">
          <el-date-picker v-model="invoiceForm.invoiceDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
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
