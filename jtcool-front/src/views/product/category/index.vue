<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="分类名称" prop="categoryName">
        <el-input v-model="queryParams.categoryName" placeholder="请输入分类名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Sort" @click="toggleExpandAll">展开/折叠</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-if="refreshTable" v-loading="loading" :data="categoryList" row-key="categoryId" :default-expand-all="isExpandAll" :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
      <el-table-column prop="categoryName" label="分类名称" :show-overflow-tooltip="true" width="260"></el-table-column>
      <el-table-column prop="categoryCode" label="分类编码" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="200"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="200">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)">新增</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
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
            <el-form-item label="显示排序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" controls-position="right" :min="0" />
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

const { proxy } = getCurrentInstance();
const { sys_normal_disable } = proxy.useDict("sys_normal_disable");

const categoryList = ref([]);
const categoryOptions = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const isExpandAll = ref(true);
const refreshTable = ref(true);
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
    sortOrder: [{ required: true, message: "显示排序不能为空", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listCategory(queryParams.value).then(response => {
    categoryList.value = handleTree(response.rows, "categoryId", "parentId");
    loading.value = false;
  });
}

function toggleExpandAll() {
  refreshTable.value = false;
  isExpandAll.value = !isExpandAll.value;
  nextTick(() => {
    refreshTable.value = true;
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
    sortOrder: 0,
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
    data.children = handleTree(response.data, "categoryId", "parentId");
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

getList();
</script>
