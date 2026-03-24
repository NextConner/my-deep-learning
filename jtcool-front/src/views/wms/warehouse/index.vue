<template>
  <div class="warehouse-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">仓库管理</h2>
        <p class="page-desc">管理所有仓库信息和状态</p>
      </div>
      <el-button type="primary" icon="Plus" @click="handleAdd" size="large">新增仓库</el-button>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="仓库名称" prop="warehouseName">
          <el-input v-model="queryParams.warehouseName" placeholder="搜索仓库名称" clearable @keyup.enter="handleQuery" prefix-icon="Search" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card" v-loading="loading">
      <div class="warehouse-grid">
        <div v-for="item in warehouseList" :key="item.warehouseId" class="warehouse-card">
          <div class="warehouse-card__header">
            <div class="warehouse-card__icon">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <el-tag :type="item.status === '0' ? 'success' : 'info'" size="small">
              {{ item.status === '0' ? '启用' : '停用' }}
            </el-tag>
          </div>
          <div class="warehouse-card__body">
            <h3 class="warehouse-card__name">{{ item.warehouseName }}</h3>
            <div class="warehouse-card__code">编码: {{ item.warehouseCode }}</div>
            <div class="warehouse-card__address" v-if="item.address">
              <el-icon><Location /></el-icon>
              <span>{{ item.address }}</span>
            </div>
          </div>
          <div class="warehouse-card__footer">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(item)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(item)">删除</el-button>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && warehouseList.length === 0" description="暂无仓库数据" />
    </el-card>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="540px" :close-on-click-modal="false">
      <el-form ref="warehouseRef" :model="form" :rules="rules" label-width="90px" label-position="top">
        <el-form-item label="仓库编码" prop="warehouseCode">
          <el-input v-model="form.warehouseCode" placeholder="请输入仓库编码" size="large" />
        </el-form-item>
        <el-form-item label="仓库名称" prop="warehouseName">
          <el-input v-model="form.warehouseName" placeholder="请输入仓库名称" size="large" />
        </el-form-item>
        <el-form-item label="仓库地址">
          <el-input v-model="form.address" placeholder="请输入仓库地址" size="large" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status" size="large">
            <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancel" size="large">取消</el-button>
        <el-button type="primary" @click="submitForm" size="large">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WmsWarehouse">
import { listWarehouse, getWarehouse, delWarehouse, addWarehouse, updateWarehouse } from "@/api/wms/warehouse";
import { OfficeBuilding, Location } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { sys_normal_disable } = proxy.useDict("sys_normal_disable");

const warehouseList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    warehouseName: undefined
  },
  rules: {
    warehouseCode: [{ required: true, message: "仓库编码不能为空", trigger: "blur" }],
    warehouseName: [{ required: true, message: "仓库名称不能为空", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listWarehouse(queryParams.value).then(response => {
    warehouseList.value = response.rows;
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
    warehouseId: undefined,
    warehouseCode: undefined,
    warehouseName: undefined,
    address: undefined,
    status: "0"
  };
  proxy.resetForm("warehouseRef");
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
  reset();
  open.value = true;
  title.value = "添加仓库";
}

function handleUpdate(row) {
  reset();
  getWarehouse(row.warehouseId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改仓库";
  });
}

function submitForm() {
  proxy.$refs["warehouseRef"].validate(valid => {
    if (valid) {
      if (form.value.warehouseId != undefined) {
        updateWarehouse(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addWarehouse(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除仓库"' + row.warehouseName + '"?').then(() => {
    return delWarehouse(row.warehouseId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
</script>

<style scoped lang="scss">
.warehouse-container {
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

.search-card {
  margin-bottom: 16px;
  border-radius: 12px;
  border: 0;
}

.table-card {
  border-radius: 12px;
  border: 0;
  margin-bottom: 16px;
}

.warehouse-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.warehouse-card {
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

.warehouse-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.warehouse-card__icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 24px;
}

.warehouse-card__body {
  margin-bottom: 16px;
}

.warehouse-card__name {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.warehouse-card__code {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.warehouse-card__address {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
  margin-top: 8px;
}

.warehouse-card__footer {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}
</style>
