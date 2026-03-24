<template>
  <div class="customer-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">客户管理</h2>
        <p class="page-desc">管理客户信息和信用额度</p>
      </div>
      <el-button type="primary" icon="Plus" @click="handleAdd" size="large">新增客户</el-button>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="queryParams.customerName" placeholder="搜索客户" clearable @keyup.enter="handleQuery" prefix-icon="Search" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="queryParams.contactPerson" placeholder="联系人" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card" v-loading="loading">
      <div class="customer-grid">
        <div v-for="item in customerList" :key="item.customerId" class="customer-card">
          <div class="customer-card__header">
            <div class="customer-icon">
              <el-icon><User /></el-icon>
            </div>
            <el-tag :type="item.status === '0' ? 'success' : 'info'" size="small">
              {{ item.status === '0' ? '启用' : '停用' }}
            </el-tag>
          </div>
          <h3 class="customer-name">{{ item.customerName }}</h3>
          <div class="customer-code">编码: {{ item.customerCode }}</div>
          <div class="customer-info">
            <div class="info-item">
              <span>联系人</span>
              <strong>{{ item.contactPerson }}</strong>
            </div>
            <div class="info-item">
              <span>电话</span>
              <strong>{{ item.contactPhone }}</strong>
            </div>
            <div class="info-item">
              <span>信用额度</span>
              <strong class="credit">¥{{ item.creditLimit }}</strong>
            </div>
          </div>
          <div class="customer-actions">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(item)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(item)">删除</el-button>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && customerList.length === 0" description="暂无客户数据" />
    </el-card>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="customerRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="客户编码" prop="customerCode">
              <el-input v-model="form.customerCode" placeholder="请输入客户编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户名称" prop="customerName">
              <el-input v-model="form.customerName" placeholder="请输入客户名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系邮箱">
              <el-input v-model="form.contactEmail" placeholder="请输入联系邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="信用额度" prop="creditLimit">
              <el-input-number v-model="form.creditLimit" :precision="2" :min="0" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="地址">
              <el-input v-model="form.address" placeholder="请输入地址" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="OmsCustomer">
import { listCustomer, getCustomer, delCustomer, addCustomer, updateCustomer } from "@/api/oms/customer";
import { User } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { sys_normal_disable } = proxy.useDict("sys_normal_disable");

const customerList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    customerName: undefined,
    contactPerson: undefined
  },
  rules: {
    customerCode: [{ required: true, message: "客户编码不能为空", trigger: "blur" }],
    customerName: [{ required: true, message: "客户名称不能为空", trigger: "blur" }],
    contactPerson: [{ required: true, message: "联系人不能为空", trigger: "blur" }],
    contactPhone: [{ required: true, message: "联系电话不能为空", trigger: "blur" }],
    creditLimit: [{ required: true, message: "信用额度不能为空", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listCustomer(queryParams.value).then(response => {
    customerList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function cancel() {
  open.value = false;
  reset();
}

function reset() {
  form.value = {
    customerId: undefined,
    customerCode: undefined,
    customerName: undefined,
    contactPerson: undefined,
    contactPhone: undefined,
    contactEmail: undefined,
    address: undefined,
    creditLimit: 0,
    status: "0"
  };
  proxy.resetForm("customerRef");
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.customerId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加客户";
}

function handleUpdate(row) {
  reset();
  const customerId = row.customerId || ids.value;
  getCustomer(customerId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改客户";
  });
}

function submitForm() {
  proxy.$refs["customerRef"].validate(valid => {
    if (valid) {
      if (form.value.customerId != undefined) {
        updateCustomer(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addCustomer(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

function handleDelete(row) {
  const customerIds = row.customerId || ids.value;
  proxy.$modal.confirm('是否确认删除客户编号为"' + customerIds + '"的数据项?').then(function() {
    return delCustomer(customerIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
</script>

<style scoped lang="scss">
.customer-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
.search-card, .table-card {
  margin-bottom: 16px;
  border-radius: 12px;
  border: 0;
}
.customer-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}
.customer-card {
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
.customer-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.customer-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #fff;
  font-size: 24px;
}
.customer-name {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}
.customer-code {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 16px;
}
.customer-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 16px 0;
  border-top: 1px solid #f3f4f6;
  border-bottom: 1px solid #f3f4f6;
}
.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  span {
    font-size: 12px;
    color: #9ca3af;
  }
  strong {
    font-size: 14px;
    color: #1f2937;
  }
  .credit {
    color: #10b981;
    font-size: 16px;
  }
}
.customer-actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}
</style>
