<template>
  <div class="app-container supplier-page">
    <section class="supplier-hero">
      <div class="supplier-hero__main">
        <span class="supplier-hero__eyebrow">Supplier Workspace</span>
        <h2 class="supplier-hero__title">供应商管理</h2>
        <p class="supplier-hero__desc">统一查看供应商档案、联系人和状态，优先维护在用供应链关系。</p>
      </div>
      <div class="supplier-hero__meta">
        <span>当前页 {{ supplierList.length }} 个供应商</span>
        <span>累计 {{ total }} 条</span>
      </div>
    </section>

    <section class="supplier-summary">
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

    <section class="supplier-panel" v-show="showSearch">
      <div class="panel-heading">
        <div>
          <h3>筛选条件</h3>
          <p>按名称、联系人和状态筛选供应商。</p>
        </div>
      </div>
      <el-form :model="queryParams" ref="queryRef" :inline="true" class="filter-form">
        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="queryParams.supplierName" placeholder="请输入供应商名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="queryParams.contactPerson" placeholder="请输入联系人" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="状态" clearable>
            <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item class="filter-form__actions">
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="supplier-panel">
      <div class="panel-heading panel-heading--split">
        <div>
          <h3>供应商列表</h3>
          <p>聚焦供应商身份、联系方式与状态，支持批量删除。</p>
        </div>
        <div class="panel-actions">
          <el-button type="primary" icon="Plus" @click="handleAdd">新增供应商</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">删除</el-button>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </div>
      </div>

      <el-table v-loading="loading" :data="supplierList" @selection-change="handleSelectionChange" class="supplier-table">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="供应商" min-width="220">
          <template #default="{ row }">
            <div class="supplier-cell">
              <strong>{{ row.supplierName }}</strong>
              <span>{{ row.supplierCode }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="联系人" min-width="180">
          <template #default="{ row }">
            <div class="contact-cell">
              <strong>{{ row.contactPerson }}</strong>
              <span>{{ row.contactPhone }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="联系邮箱" min-width="200" prop="contactEmail" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <dict-tag :options="sys_normal_disable" :value="row.status"/>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="right" width="160">
          <template #default="{ row }">
            <div class="action-links">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && supplierList.length === 0" description="暂无供应商数据" />

      <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </section>

    <el-dialog :title="title" v-model="open" width="700px" append-to-body class="supplier-dialog">
      <div class="dialog-intro">
        <span>维护供应商主体、联系人和地址信息，建议优先保证联系方式完整。</span>
      </div>
      <el-form ref="supplierRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="供应商名称" prop="supplierName">
              <el-input v-model="form.supplierName" placeholder="请输入供应商名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商编码" prop="supplierCode">
              <el-input v-model="form.supplierCode" placeholder="请输入供应商编码" />
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
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="地址">
              <el-input v-model="form.address" placeholder="请输入地址" />
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

<script setup name="ProductSupplier">
import { listSupplier, getSupplier, delSupplier, addSupplier, updateSupplier } from "@/api/product/supplier";
import { OfficeBuilding, Phone, SetUp, User } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { sys_normal_disable } = proxy.useDict("sys_normal_disable");

const supplierList = ref([]);
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
    supplierName: undefined,
    contactPerson: undefined,
    status: undefined
  },
  rules: {
    supplierName: [{ required: true, message: "供应商名称不能为空", trigger: "blur" }],
    supplierCode: [{ required: true, message: "供应商编码不能为空", trigger: "blur" }],
    contactPerson: [{ required: true, message: "联系人不能为空", trigger: "blur" }],
    contactPhone: [{ required: true, message: "联系电话不能为空", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

const summaryCards = computed(() => {
  const activeCount = supplierList.value.filter(item => item.status === '0').length
  const disabledCount = supplierList.value.filter(item => item.status !== '0').length
  const contactCount = supplierList.value.filter(item => item.contactPerson && item.contactPhone).length
  const emailCount = supplierList.value.filter(item => item.contactEmail).length

  return [
    { key: 'active', label: '启用供应商', value: activeCount, hint: '当前页在用供应商', icon: OfficeBuilding },
    { key: 'disabled', label: '停用供应商', value: disabledCount, hint: '当前页停用供应商', icon: SetUp },
    { key: 'contact', label: '联系人完整', value: contactCount, hint: '联系人和电话已填写', icon: User },
    { key: 'email', label: '邮箱已填', value: emailCount, hint: '已配置联系邮箱', icon: Phone }
  ]
})

function getList() {
  loading.value = true;
  listSupplier(queryParams.value).then(response => {
    supplierList.value = response.rows;
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
    supplierId: undefined,
    supplierName: undefined,
    supplierCode: undefined,
    contactPerson: undefined,
    contactPhone: undefined,
    contactEmail: undefined,
    address: undefined,
    status: "0"
  };
  proxy.resetForm("supplierRef");
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
  ids.value = selection.map(item => item.supplierId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加供应商";
}

function handleUpdate(row) {
  reset();
  const supplierId = row.supplierId || ids.value;
  getSupplier(supplierId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改供应商";
  });
}

function submitForm() {
  proxy.$refs["supplierRef"].validate(valid => {
    if (valid) {
      if (form.value.supplierId != undefined) {
        updateSupplier(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addSupplier(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

function handleDelete(row) {
  const supplierIds = row.supplierId || ids.value;
  proxy.$modal.confirm('是否确认删除供应商编号为"' + supplierIds + '"的数据项?').then(function() {
    return delSupplier(supplierIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
</script>

<style lang="scss" scoped>
.supplier-page {
  min-height: calc(100vh - 84px);
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(83, 24, 92, 0.08), transparent 30%),
    linear-gradient(180deg, #f8f5fa 0%, #f2edf5 100%);
}

.supplier-hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 28px 32px;
  margin-bottom: 18px;
  border-radius: 24px;
  background: linear-gradient(135deg, #3b0764 0%, #581c87 55%, #7e22ce 100%);
  color: #fff;
  box-shadow: 0 24px 60px rgba(59, 7, 100, 0.18);
}

.supplier-hero__eyebrow {
  display: inline-block;
  margin-bottom: 10px;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: rgba(243, 232, 255, 0.82);
}

.supplier-hero__title {
  margin: 0 0 10px;
  font-size: 34px;
  font-weight: 700;
}

.supplier-hero__desc {
  max-width: 560px;
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  color: rgba(243, 232, 255, 0.82);
}

.supplier-hero__meta {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  align-items: flex-end;
  gap: 10px;
  font-size: 13px;
  color: rgba(243, 232, 255, 0.76);
}

.supplier-summary {
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

.summary-tile--active .summary-tile__icon { background: linear-gradient(135deg, #2563eb 0%, #60a5fa 100%); }
.summary-tile--disabled .summary-tile__icon { background: linear-gradient(135deg, #64748b 0%, #94a3b8 100%); }
.summary-tile--contact .summary-tile__icon { background: linear-gradient(135deg, #10b981 0%, #34d399 100%); }
.summary-tile--email .summary-tile__icon { background: linear-gradient(135deg, #f59e0b 0%, #fb923c 100%); }

.summary-tile__body { display: flex; flex-direction: column; gap: 4px; }
.summary-tile__label { font-size: 13px; color: #64748b; }
.summary-tile__value { font-size: 28px; font-weight: 700; color: #0f172a; line-height: 1.1; }
.summary-tile__hint { font-size: 12px; color: #94a3b8; }

.supplier-panel {
  padding: 22px 24px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.05);
}

.supplier-panel + .supplier-panel { margin-top: 18px; }

.panel-heading {
  margin-bottom: 18px;

  h3 { margin: 0 0 6px; font-size: 18px; font-weight: 600; color: #0f172a; }
  p { margin: 0; font-size: 13px; color: #64748b; }
}

.panel-heading--split {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
}

.panel-actions { display: flex; gap: 12px; align-items: center; }
.filter-form { margin-bottom: -18px; }
.filter-form :deep(.el-form-item) { margin-bottom: 18px; }
.filter-form__actions :deep(.el-form-item__content) { display: flex; gap: 10px; }

.supplier-table { margin-bottom: 18px; }
.supplier-table :deep(.el-table__header th) { background: #f8fafc !important; color: #475569; font-weight: 600; }
.supplier-table :deep(.el-table__row:hover > td) { background: #faf7ff !important; }

.supplier-cell, .contact-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.supplier-cell strong, .contact-cell strong { color: #0f172a; }
.supplier-cell span, .contact-cell span { font-size: 12px; color: #94a3b8; }

.action-links { display: flex; justify-content: flex-end; gap: 12px; align-items: center; }

.supplier-dialog :deep(.el-dialog) { border-radius: 24px; overflow: hidden; }
.supplier-dialog :deep(.el-dialog__header) { padding: 22px 24px 14px; }
.supplier-dialog :deep(.el-dialog__body) { padding: 0 24px 20px; }
.supplier-dialog :deep(.el-dialog__footer) { padding: 16px 24px 24px; }

.dialog-intro {
  margin-bottom: 18px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #f8fafc;
  font-size: 13px;
  color: #64748b;
}

@media (max-width: 1200px) {
  .supplier-summary { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

@media (max-width: 768px) {
  .supplier-page { padding: 16px; }
  .supplier-hero, .panel-heading--split { flex-direction: column; align-items: flex-start; }
  .supplier-hero { padding: 24px 20px; }
  .supplier-hero__title { font-size: 28px; }
  .supplier-hero__meta { align-items: flex-start; }
  .supplier-summary { grid-template-columns: 1fr; }
  .supplier-panel { padding: 18px; border-radius: 20px; }
  .panel-actions { width: 100%; justify-content: space-between; }
  .action-links { justify-content: flex-start; }
}
</style>
