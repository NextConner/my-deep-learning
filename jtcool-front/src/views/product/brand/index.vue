<template>
  <div class="brand-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">品牌管理</h2>
        <p class="page-desc">管理产品品牌信息</p>
      </div>
      <el-button type="primary" icon="Plus" @click="handleAdd" size="large">新增品牌</el-button>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="品牌名称" prop="brandName">
          <el-input v-model="queryParams.brandName" placeholder="搜索品牌" clearable @keyup.enter="handleQuery" prefix-icon="Search" />
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
    </el-card>

    <el-card shadow="never" class="table-card" v-loading="loading">
      <div class="brand-grid">
        <div v-for="item in brandList" :key="item.brandId" class="brand-card">
          <div class="brand-card__logo">
            <img v-if="item.logoUrl" :src="item.logoUrl" alt="logo" />
            <div v-else class="logo-placeholder">
              <el-icon><Picture /></el-icon>
            </div>
          </div>
          <div class="brand-card__body">
            <h3 class="brand-card__name">{{ item.brandName }}</h3>
            <div class="brand-card__code">编码: {{ item.brandCode }}</div>
            <div class="brand-card__meta">
              <el-tag :type="item.status === '0' ? 'success' : 'info'" size="small">
                {{ item.status === '0' ? '启用' : '停用' }}
              </el-tag>
              <span class="create-time">{{ parseTime(item.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
            </div>
          </div>
          <div class="brand-card__actions">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(item)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(item)">删除</el-button>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && brandList.length === 0" description="暂无品牌数据" />
    </el-card>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="brandRef" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="品牌名称" prop="brandName">
              <el-input v-model="form.brandName" placeholder="请输入品牌名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="品牌编码" prop="brandCode">
              <el-input v-model="form.brandCode" placeholder="请输入品牌编码" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="品牌Logo">
              <image-upload v-model="form.logoUrl"/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="描述">
              <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
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

<script setup name="ProductBrand">
import { listBrand, getBrand, delBrand, addBrand, updateBrand } from "@/api/product/brand";
import { Picture } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { sys_normal_disable } = proxy.useDict("sys_normal_disable");

const brandList = ref([]);
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
    brandName: undefined,
    status: undefined
  },
  rules: {
    brandName: [{ required: true, message: "品牌名称不能为空", trigger: "blur" }],
    brandCode: [{ required: true, message: "品牌编码不能为空", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listBrand(queryParams.value).then(response => {
    brandList.value = response.rows;
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
    brandId: undefined,
    brandName: undefined,
    brandCode: undefined,
    logoUrl: undefined,
    description: undefined,
    status: "0"
  };
  proxy.resetForm("brandRef");
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
  ids.value = selection.map(item => item.brandId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加品牌";
}

function handleUpdate(row) {
  reset();
  const brandId = row.brandId || ids.value;
  getBrand(brandId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改品牌";
  });
}

function submitForm() {
  proxy.$refs["brandRef"].validate(valid => {
    if (valid) {
      if (form.value.brandId != undefined) {
        updateBrand(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addBrand(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

function handleDelete(row) {
  const brandIds = row.brandId || ids.value;
  proxy.$modal.confirm('是否确认删除品牌编号为"' + brandIds + '"的数据项?').then(function() {
    return delBrand(brandIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
</script>

<style scoped lang="scss">
.brand-container {
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

.brand-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.brand-card {
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

.brand-card__logo {
  width: 100%;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  border-radius: 8px;
  background: #f9fafb;
  overflow: hidden;

  img {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
  }
}

.logo-placeholder {
  font-size: 48px;
  color: #d1d5db;
}

.brand-card__name {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.brand-card__code {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 12px;
}

.brand-card__meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
}

.create-time {
  font-size: 12px;
  color: #9ca3af;
}

.brand-card__actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
}
</style>
