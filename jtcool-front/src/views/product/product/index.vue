<template>
  <div class="app-container modern-page">
    <div class="search-card">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
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
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    </div>

    <div class="action-bar">
    <el-row :gutter="10">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    </div>

    <div class="table-card">
    <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="产品编码" align="center" prop="productCode" width="120" />
      <el-table-column label="产品名称" align="center" prop="productName" :show-overflow-tooltip="true" />
      <el-table-column label="分类" align="center" prop="categoryName" />
      <el-table-column label="品牌" align="center" prop="brandName" />
      <el-table-column label="标准价" align="center" prop="standardPrice" />
      <el-table-column label="成本价" align="center" prop="costPrice" />
      <el-table-column label="预警库存" align="center" prop="warningStock" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog :title="title" v-model="open" width="800px" append-to-body class="modern-dialog">
      <el-form ref="productRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
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
              <el-input-number v-model="form.standardPrice" :precision="2" :min="0" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成本价" prop="costPrice">
              <el-input-number v-model="form.costPrice" :precision="2" :min="0" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预警库存" prop="warningStock">
              <el-input-number v-model="form.warningStock" :min="0" controls-position="right" />
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
        updateProduct(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addProduct(form.value).then(response => {
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
@import "@/assets/styles/modern-page.scss";
</style>

