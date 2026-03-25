<template>
  <div class="location-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">库位管理</h2>
        <p class="page-desc">管理仓库、库区、库位与货架的层级结构</p>
      </div>
      <el-button type="primary" icon="Plus" size="large" @click="handleAdd">新增节点</el-button>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="queryParams.warehouseId" placeholder="请选择仓库" clearable>
            <el-option v-for="item in warehouseOptions" :key="item.warehouseId" :label="item.warehouseName" :value="item.warehouseId" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card" v-loading="loading">
      <div class="location-overview">
        <div class="overview-item">
          <span class="overview-label">仓库</span>
          <strong class="overview-value">{{ summaryCards[0].value }}</strong>
        </div>
        <div class="overview-item">
          <span class="overview-label">库区</span>
          <strong class="overview-value">{{ summaryCards[1].value }}</strong>
        </div>
        <div class="overview-item">
          <span class="overview-label">库位</span>
          <strong class="overview-value">{{ summaryCards[2].value }}</strong>
        </div>
        <div class="overview-item">
          <span class="overview-label">货架</span>
          <strong class="overview-value">{{ summaryCards[3].value }}</strong>
        </div>
      </div>

      <LocationBranch
        v-if="!loading && locationList.length"
        :nodes="locationList"
        @add="handleAdd"
        @edit="handleUpdate"
        @delete="handleDelete"
      />
      <el-empty v-else-if="!loading" description="暂无库位数据" />
    </el-card>

    <el-dialog :title="title" v-model="open" width="600px" append-to-body class="modern-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="24" v-if="form.parentName">
            <el-form-item label="父级节点">
              <el-input v-model="form.parentName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="类型" prop="type">
              <el-radio-group v-model="form.type" :disabled="!!form.parentId">
                <el-radio label="area" v-if="!form.parentId || form.parentType === 'warehouse'">库区</el-radio>
                <el-radio label="location" v-if="!form.parentId || form.parentType === 'area'">库位</el-radio>
                <el-radio label="shelf" v-if="!form.parentId || form.parentType === 'location'">货架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="编码" prop="code">
              <el-input v-model="form.code" placeholder="请输入编码" />
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

<script setup name="WmsLocation">
import { listWarehouse } from "@/api/wms/warehouse";
import { listArea, addArea, updateArea, delArea } from "@/api/wms/area";
import { listLocation, addLocation, updateLocation, delLocation } from "@/api/wms/location";
import { listShelf, addShelf, updateShelf, delShelf } from "@/api/wms/shelf";
import LocationBranch from "./LocationBranch.vue";

const { proxy } = getCurrentInstance();
const { sys_normal_disable } = proxy.useDict("sys_normal_disable");

const locationList = ref([]);
const warehouseOptions = ref([]);
const open = ref(false);
const loading = ref(true);
const title = ref("");

const summaryCards = computed(() => {
  let warehouseCount = 0
  let areaCount = 0
  let locationCount = 0
  let shelfCount = 0

  const walk = (nodes) => {
    nodes.forEach(node => {
      if (node.type === 'warehouse') warehouseCount += 1
      if (node.type === 'area') areaCount += 1
      if (node.type === 'location') locationCount += 1
      if (node.type === 'shelf') shelfCount += 1
      if (node.children?.length) walk(node.children)
    })
  }

  walk(locationList.value)

  return [
    { key: 'warehouse', label: '仓库', value: warehouseCount },
    { key: 'area', label: '库区', value: areaCount },
    { key: 'location', label: '库位', value: locationCount },
    { key: 'shelf', label: '货架', value: shelfCount }
  ]
})

const data = reactive({
  form: {},
  queryParams: { warehouseId: undefined },
  rules: {
    type: [{ required: true, message: "类型不能为空", trigger: "change" }],
    name: [{ required: true, message: "名称不能为空", trigger: "blur" }],
    code: [{ required: true, message: "编码不能为空", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  Promise.all([
    listWarehouse(),
    listArea(queryParams.value),
    listLocation(queryParams.value),
    listShelf(queryParams.value)
  ]).then(([warehouses, areas, locations, shelves]) => {
    warehouseOptions.value = warehouses.rows;
    buildTree(warehouses.rows, areas.rows, locations.rows, shelves.rows);
    loading.value = false;
  });
}

function buildTree(warehouses, areas, locations, shelves) {
  const tree = warehouses.map(w => ({
    id: 'w-' + w.warehouseId,
    level: 0,
    name: w.warehouseName,
    code: w.warehouseCode,
    type: 'warehouse',
    status: w.status,
    parentType: undefined,
    warehouseId: w.warehouseId,
    children: []
  }));

  areas.forEach(a => {
    const warehouse = tree.find(w => w.warehouseId === a.warehouseId);
    if (warehouse) {
      warehouse.children.push({
        id: 'a-' + a.areaId,
        level: 1,
        name: a.areaName,
        code: a.areaCode,
        type: 'area',
        status: a.status,
        parentType: 'warehouse',
        parentName: warehouse.name,
        areaId: a.areaId,
        warehouseId: a.warehouseId,
        children: []
      });
    }
  });

  locations.forEach(l => {
    tree.forEach(w => {
      const area = w.children.find(a => a.areaId === l.areaId);
      if (area) {
        area.children.push({
          id: 'l-' + l.locationId,
          level: 2,
          name: l.locationCode,
          code: l.locationCode,
          type: 'location',
          status: l.status,
          parentType: 'area',
          parentName: area.name,
          locationId: l.locationId,
          areaId: l.areaId,
          children: []
        });
      }
    });
  });

  shelves.forEach(s => {
    tree.forEach(w => {
      w.children.forEach(a => {
        const location = a.children.find(l => l.locationId === s.locationId);
        if (location) {
          location.children.push({
            id: 's-' + s.shelfId,
            level: 3,
            name: s.shelfCode,
            code: s.shelfCode,
            type: 'shelf',
            status: s.status,
            parentType: 'location',
            parentName: location.name,
            shelfId: s.shelfId,
            locationId: s.locationId
          });
        }
      });
    });
  });

  locationList.value = tree;
}

function cancel() {
  open.value = false;
  reset();
}

function reset() {
  form.value = { type: 'area', status: "0" };
  proxy.resetForm("formRef");
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
  if (row) {
    form.value.parentId = row.id;
    form.value.parentType = row.type;
    form.value.parentName = row.name;
    if (row.type === 'warehouse') {
      form.value.type = 'area';
      form.value.warehouseId = row.warehouseId;
    } else if (row.type === 'area') {
      form.value.type = 'location';
      form.value.areaId = row.areaId;
    } else if (row.type === 'location') {
      form.value.type = 'shelf';
      form.value.locationId = row.locationId;
    }
  }
  open.value = true;
  title.value = "新增库位节点";
}

function handleUpdate(row) {
  reset();
  form.value = { ...row };
  open.value = true;
  title.value = "修改库位节点";
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      const data = { ...form.value };
      let promise;
      if (form.value.type === 'area') {
        promise = form.value.areaId ? updateArea(data) : addArea(data);
      } else if (form.value.type === 'location') {
        promise = form.value.locationId ? updateLocation(data) : addLocation(data);
      } else {
        promise = form.value.shelfId ? updateShelf(data) : addShelf(data);
      }
      promise.then(() => {
        proxy.$modal.msgSuccess(form.value.id ? "修改成功" : "新增成功");
        open.value = false;
        getList();
      });
    }
  });
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除名称为"' + row.name + '"的数据项?').then(() => {
    let promise;
    if (row.type === 'area') promise = delArea(row.areaId);
    else if (row.type === 'location') promise = delLocation(row.locationId);
    else promise = delShelf(row.shelfId);
    return promise;
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
</script>

<style scoped lang="scss">
.location-container {
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

.location-overview {
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
