<template>
  <div class="app-container product-page">
    <section class="product-hero">
      <div class="product-hero__main">
        <span class="product-hero__eyebrow">Product Workspace</span>
        <h2 class="product-hero__title">产品管理</h2>
        <p class="product-hero__desc">集中管理产品资料、价格与预警库存，先筛选范围，再处理上下架与档案维护。</p>
      </div>
      <div class="product-hero__meta">
        <span>当前页 {{ productList.length }} 个产品</span>
        <span>累计 {{ total }} 条</span>
      </div>
    </section>

    <section class="product-summary">
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

    <section class="product-panel" v-show="showSearch">
      <div class="panel-heading">
        <div>
          <h3>筛选条件</h3>
          <p>按名称、分类、品牌和状态快速筛选产品。</p>
        </div>
      </div>
      <el-form :model="queryParams" ref="queryRef" :inline="true" class="filter-form">
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="queryParams.productName" placeholder="请输入产品名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="产品分类" prop="categoryId">
          <el-tree-select v-model="queryParams.categoryId" :data="categoryOptions" :props="{ value: 'categoryId', label: 'categoryName', children: 'children' }" placeholder="请选择产品分类" clearable />
        </el-form-item>
        <el-form-item label="品牌" prop="brandId">
          <el-select v-model="queryParams.brandId" placeholder="请选择品牌" clearable>
            <el-option v-for="item in brandOptions" :key="item.brandId" :label="item.brandName" :value="item.brandId" />
          </el-select>
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

    <section class="product-panel">
      <div class="panel-heading panel-heading--split">
        <div>
          <h3>产品列表</h3>
          <p>把产品身份、供应关系和价格库存放在同一视图中处理。</p>
        </div>
        <div class="panel-actions">
          <el-button type="primary" icon="Plus" @click="handleAdd">新增产品</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">删除</el-button>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </div>
      </div>

      <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange" class="product-table">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="产品" min-width="240">
          <template #default="{ row }">
            <div class="product-cell">
              <strong>{{ row.productName }}</strong>
              <span>{{ row.productCode }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分类 / 品牌" min-width="180">
          <template #default="{ row }">
            <div class="meta-cell">
              <span>{{ row.categoryName || '未分类' }}</span>
              <strong>{{ row.brandName || '未设置品牌' }}</strong>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格" min-width="180">
          <template #default="{ row }">
            <div class="price-cell">
              <span>标准价 {{ formatAmount(row.standardPrice) }}</span>
              <strong>成本价 {{ formatAmount(row.costPrice) }}</strong>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="预警库存" width="120" align="center" prop="warningStock" />
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

      <el-empty v-if="!loading && productList.length === 0" description="暂无产品数据" />

      <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </section>

    <el-dialog :title="title" v-model="open" width="860px" append-to-body class="product-dialog">
      <div class="dialog-intro">
        <span>维护产品基础资料、价格与供应关系，尽量保证分类、品牌、供应商信息完整。</span>
      </div>
      <el-form ref="productRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="产品编码" prop="productCode">
              <el-input v-model="form.productCode" placeholder="请输入产品编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品名称" prop="productName">
              <el-input v-model="form.productName" placeholder="请输入产品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品分类" prop="categoryId">
              <el-tree-select v-model="form.categoryId" :data="categoryOptions" :props="{ value: 'categoryId', label: 'categoryName', children: 'children' }" placeholder="请选择产品分类" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌" prop="brandId">
              <el-select v-model="form.brandId" placeholder="请选择品牌">
                <el-option v-for="item in brandOptions" :key="item.brandId" :label="item.brandName" :value="item.brandId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="form.supplierId" placeholder="请选择供应商">
                <el-option v-for="item in supplierOptions" :key="item.supplierId" :label="item.supplierName" :value="item.supplierId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位">
              <el-input v-model="form.unit" placeholder="请输入单位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标准价" prop="standardPrice">
              <el-input-number v-model="form.standardPrice" :precision="2" :min="0" controls-position="right" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成本价" prop="costPrice">
              <el-input-number v-model="form.costPrice" :precision="2" :min="0" controls-position="right" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预警库存" prop="warningStock">
              <el-input-number v-model="form.warningStock" :min="0" controls-position="right" class="w-full" />
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
            <el-form-item label="规格型号">
              <el-input v-model="form.specifications" placeholder="请输入规格型号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="产品图片">
              <image-upload v-model="form.imageUrl"/>
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

<script setup name="Product">
import { listProduct, getProduct, delProduct, addProduct, updateProduct } from "@/api/product/product";
import { listCategory } from "@/api/product/category";
import { listBrand } from "@/api/product/brand";
import { listSupplier } from "@/api/product/supplier";
import { handleTree } from "@/utils/jtcool";
import { Box, Coin, SetUp, Warning } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { sys_normal_disable } = proxy.useDict("sys_normal_disable");

const productList = ref([]);
const categoryOptions = ref([]);
const brandOptions = ref([]);
const supplierOptions = ref([]);
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
    productName: undefined,
    categoryId: undefined,
    brandId: undefined,
    status: undefined
  },
  rules: {
    productCode: [{ required: true, message: "产品编码不能为空", trigger: "blur" }],
    productName: [{ required: true, message: "产品名称不能为空", trigger: "blur" }],
    categoryId: [{ required: true, message: "产品分类不能为空", trigger: "change" }],
    standardPrice: [{ required: true, message: "标准价不能为空", trigger: "blur" }],
    costPrice: [{ required: true, message: "成本价不能为空", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

const summaryCards = computed(() => {
  const activeCount = productList.value.filter(item => item.status === '0').length
  const disabledCount = productList.value.filter(item => item.status !== '0').length
  const averagePrice = productList.value.length
    ? productList.value.reduce((sum, item) => sum + Number(item.standardPrice || 0), 0) / productList.value.length
    : 0
  const warningCount = productList.value.filter(item => Number(item.warningStock || 0) > 0).length

  return [
    { key: 'active', label: '启用产品', value: activeCount, hint: '当前页可用产品数量', icon: Box },
    { key: 'disabled', label: '停用产品', value: disabledCount, hint: '当前页停用产品数量', icon: SetUp },
    { key: 'price', label: '均价', value: formatAmount(averagePrice), hint: '当前页标准价平均值', icon: Coin },
    { key: 'warning', label: '设有预警', value: warningCount, hint: '已配置预警库存的产品', icon: Warning }
  ]
})

function formatAmount(value) {
  return `¥${Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

function getList() {
  loading.value = true;
  listProduct(queryParams.value).then(response => {
    productList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function getCategoryOptions() {
  listCategory().then(response => {
    categoryOptions.value = handleTree(response.data, "categoryId", "parentId");
  });
}

function getBrandOptions() {
  listBrand().then(response => {
    brandOptions.value = response.rows;
  });
}

function getSupplierOptions() {
  listSupplier().then(response => {
    supplierOptions.value = response.rows;
  });
}

function cancel() {
  open.value = false;
  reset();
}

function reset() {
  form.value = {
    productId: undefined,
    productCode: undefined,
    productName: undefined,
    categoryId: undefined,
    brandId: undefined,
    supplierId: undefined,
    standardPrice: 0,
    costPrice: 0,
    warningStock: 0,
    unit: undefined,
    specifications: undefined,
    imageUrl: undefined,
    status: "0"
  };
  proxy.resetForm("productRef");
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
  ids.value = selection.map(item => item.productId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加产品";
}

function handleUpdate(row) {
  reset();
  const productId = row.productId || ids.value;
  getProduct(productId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改产品";
  });
}

function submitForm() {
  proxy.$refs["productRef"].validate(valid => {
    if (valid) {
      if (form.value.productId != undefined) {
        updateProduct(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addProduct(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

function handleDelete(row) {
  const productIds = row.productId || ids.value;
  proxy.$modal.confirm('是否确认删除产品编号为"' + productIds + '"的数据项?').then(function() {
    return delProduct(productIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
getCategoryOptions();
getBrandOptions();
getSupplierOptions();
</script>

<style lang="scss" scoped>
.product-page {
  min-height: calc(100vh - 84px);
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(6, 95, 70, 0.08), transparent 30%),
    linear-gradient(180deg, #f5f8f7 0%, #edf4f2 100%);
}

.product-hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 28px 32px;
  margin-bottom: 18px;
  border-radius: 24px;
  background: linear-gradient(135deg, #052e2b 0%, #065f46 55%, #0f766e 100%);
  color: #fff;
  box-shadow: 0 24px 60px rgba(5, 46, 43, 0.18);
}

.product-hero__eyebrow {
  display: inline-block;
  margin-bottom: 10px;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: rgba(204, 251, 241, 0.82);
}

.product-hero__title {
  margin: 0 0 10px;
  font-size: 34px;
  font-weight: 700;
}

.product-hero__desc {
  max-width: 560px;
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  color: rgba(204, 251, 241, 0.82);
}

.product-hero__meta {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  align-items: flex-end;
  gap: 10px;
  font-size: 13px;
  color: rgba(204, 251, 241, 0.76);
}

.product-summary {
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

.summary-tile--active .summary-tile__icon { background: linear-gradient(135deg, #10b981 0%, #34d399 100%); }
.summary-tile--disabled .summary-tile__icon { background: linear-gradient(135deg, #64748b 0%, #94a3b8 100%); }
.summary-tile--price .summary-tile__icon { background: linear-gradient(135deg, #f59e0b 0%, #fb923c 100%); }
.summary-tile--warning .summary-tile__icon { background: linear-gradient(135deg, #ef4444 0%, #f97316 100%); }

.summary-tile__body { display: flex; flex-direction: column; gap: 4px; }
.summary-tile__label { font-size: 13px; color: #64748b; }
.summary-tile__value { font-size: 28px; font-weight: 700; color: #0f172a; line-height: 1.1; }
.summary-tile__hint { font-size: 12px; color: #94a3b8; }

.product-panel {
  padding: 22px 24px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.05);
}

.product-panel + .product-panel { margin-top: 18px; }

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

.product-table { margin-bottom: 18px; }
.product-table :deep(.el-table__header th) { background: #f8fafc !important; color: #475569; font-weight: 600; }
.product-table :deep(.el-table__row:hover > td) { background: #f6fbf9 !important; }

.product-cell, .meta-cell, .price-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.product-cell strong, .meta-cell strong, .price-cell strong { color: #0f172a; }
.product-cell span, .meta-cell span, .price-cell span { font-size: 12px; color: #94a3b8; }

.action-links { display: flex; justify-content: flex-end; gap: 12px; align-items: center; }

.product-dialog :deep(.el-dialog) { border-radius: 24px; overflow: hidden; }
.product-dialog :deep(.el-dialog__header) { padding: 22px 24px 14px; }
.product-dialog :deep(.el-dialog__body) { padding: 0 24px 20px; }
.product-dialog :deep(.el-dialog__footer) { padding: 16px 24px 24px; }

.dialog-intro {
  margin-bottom: 18px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #f8fafc;
  font-size: 13px;
  color: #64748b;
}

.w-full { width: 100%; }

@media (max-width: 1200px) {
  .product-summary { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

@media (max-width: 768px) {
  .product-page { padding: 16px; }
  .product-hero, .panel-heading--split { flex-direction: column; align-items: flex-start; }
  .product-hero { padding: 24px 20px; }
  .product-hero__title { font-size: 28px; }
  .product-hero__meta { align-items: flex-start; }
  .product-summary { grid-template-columns: 1fr; }
  .product-panel { padding: 18px; border-radius: 20px; }
  .panel-actions { width: 100%; justify-content: space-between; }
  .action-links { justify-content: flex-start; }
}
</style>
