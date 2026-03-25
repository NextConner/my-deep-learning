<template>
  <div class="category-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">分类管理</h2>
        <p class="page-desc">用分层卡片视图管理产品分类和子分类结构</p>
      </div>
      <el-button type="primary" icon="Plus" size="large" @click="handleAdd">新增分类</el-button>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input
            v-model="queryParams.categoryName"
            placeholder="搜索分类"
            clearable
            prefix-icon="Search"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="状态" clearable>
            <el-option
              v-for="dict in sys_normal_disable"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card" v-loading="loading">
      <div class="category-overview">
        <div class="overview-item">
          <span class="overview-label">顶级分类</span>
          <strong class="overview-value">{{ categoryList.length }}</strong>
        </div>
        <div class="overview-item">
          <span class="overview-label">分类总数</span>
          <strong class="overview-value">{{ totalCategories }}</strong>
        </div>
        <div class="overview-item">
          <span class="overview-label">叶子节点</span>
          <strong class="overview-value">{{ leafCategories }}</strong>
        </div>
      </div>

      <CategoryBranch
        v-if="!loading && categoryList.length"
        :nodes="categoryList"
        @add="handleAdd"
        @edit="handleUpdate"
        @delete="handleDelete"
      />
      <el-empty v-else-if="!loading" description="暂无分类数据" />
    </el-card>

    <el-dialog :title="title" v-model="open" width="600px" append-to-body class="modern-dialog">
      <el-form ref="categoryRef" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="24" v-if="form.parentId !== 0">
            <el-form-item label="上级分类" prop="parentId">
              <el-tree-select v-model="form.parentId" :data="categoryOptions" :props="{ value: 'categoryId', label: 'categoryName', children: 'children' }" value-key="categoryId" placeholder="选择上级分类" check-strictly />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="分类名称" prop="categoryName">
              <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="分类编码" prop="categoryCode">
              <el-input v-model="form.categoryCode" placeholder="请输入分类编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示排序" prop="orderNum">
              <el-input-number v-model="form.orderNum" controls-position="right" :min="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
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

<script setup name="ProductCategory">
import { listCategory, getCategory, delCategory, addCategory, updateCategory } from "@/api/product/category";
import { handleTree } from "@/utils/jtcool";
import CategoryBranch from "./CategoryBranch.vue";

const { proxy } = getCurrentInstance();
const { sys_normal_disable } = proxy.useDict("sys_normal_disable");

const categoryList = ref([]);
const categoryOptions = ref([]);
const open = ref(false);
const loading = ref(true);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    categoryName: undefined,
    status: undefined
  },
  rules: {
    categoryName: [{ required: true, message: "分类名称不能为空", trigger: "blur" }],
    categoryCode: [{ required: true, message: "分类编码不能为空", trigger: "blur" }],
    orderNum: [{ required: true, message: "显示排序不能为空", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

const totalCategories = computed(() => countCategories(categoryList.value));
const leafCategories = computed(() => countLeaves(categoryList.value));

function getList() {
  loading.value = true;
  listCategory(queryParams.value).then(response => {
    categoryList.value = handleTree(response.rows, "categoryId", "parentId");
    loading.value = false;
  });
}

function cancel() {
  open.value = false;
  reset();
}

function reset() {
  form.value = {
    categoryId: undefined,
    parentId: 0,
    categoryName: undefined,
    categoryCode: undefined,
    orderNum: 0,
    status: "0"
  };
  proxy.resetForm("categoryRef");
}

function handleQuery() {
  getList();
}

function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

function handleAdd(row) {
  reset();
  getTreeselect();
  if (row != null && row.categoryId) {
    form.value.parentId = row.categoryId;
  } else {
    form.value.parentId = 0;
  }
  open.value = true;
  title.value = "添加产品分类";
}

function getTreeselect() {
  listCategory().then(response => {
    categoryOptions.value = [];
    const data = { categoryId: 0, categoryName: '主类目', children: [] };
    data.children = handleTree(response.rows, "categoryId", "parentId");
    categoryOptions.value.push(data);
  });
}

function handleUpdate(row) {
  reset();
  getTreeselect();
  getCategory(row.categoryId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改产品分类";
  });
}

function submitForm() {
  proxy.$refs["categoryRef"].validate(valid => {
    if (valid) {
      if (form.value.categoryId != undefined) {
        updateCategory(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addCategory(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除名称为"' + row.categoryName + '"的数据项?').then(function() {
    return delCategory(row.categoryId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

function countCategories(nodes = []) {
  return nodes.reduce((total, item) => {
    return total + 1 + countCategories(item.children || []);
  }, 0);
}

function countLeaves(nodes = []) {
  return nodes.reduce((total, item) => {
    if (item.children?.length) {
      return total + countLeaves(item.children);
    }
    return total + 1;
  }, 0);
}

getList();
</script>

<style scoped lang="scss">
.category-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
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

.search-card,
.table-card {
  margin-bottom: 16px;
  border-radius: 12px;
  border: 0;
}

.category-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 14px;
  margin-bottom: 20px;
}

.overview-item {
  padding: 16px 18px;
  border-radius: 14px;
  background: linear-gradient(135deg, #f8fafc 0%, #eef5ff 100%);
  border: 1px solid #e2e8f0;
}

.overview-label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  color: #64748b;
}

.overview-value {
  font-size: 28px;
  line-height: 1;
  color: #0f172a;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
