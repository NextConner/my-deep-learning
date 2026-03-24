<template>
  <div class="app-container location-page">
    <section class="page-header">
      <div>
        <h2 class="page-title">库位管理</h2>
        <p class="page-desc">统一查看仓库、库区、库位与货架的层级结构，快速补齐节点并处理停用项。</p>
      </div>
      <div class="page-meta">
        <span>当前视图 {{ locationList.length }} 个仓库根节点</span>
        <span>{{ queryWarehouseName || '全部仓库' }}</span>
      </div>
    </section>

    <section class="location-summary">
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

    <section class="location-panel" v-show="showSearch">
      <div class="panel-heading">
        <div>
          <h3>筛选范围</h3>
          <p>按仓库聚焦结构树，缩小当前管理范围。</p>
        </div>
      </div>
      <el-form :model="queryParams" ref="queryRef" :inline="true" class="filter-form">
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="queryParams.warehouseId" placeholder="请选择仓库" clearable>
            <el-option v-for="item in warehouseOptions" :key="item.warehouseId" :label="item.warehouseName" :value="item.warehouseId" />
          </el-select>
        </el-form-item>
        <el-form-item class="filter-form__actions">
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="location-panel">
      <div class="panel-heading panel-heading--split">
        <div>
          <h3>层级结构</h3>
          <p>树状展示仓库到货架的完整路径，直接在对应层级追加节点。</p>
        </div>
        <div class="panel-actions">
          <el-button type="primary" icon="Plus" @click="handleAdd">新增节点</el-button>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </div>
      </div>

      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="locationList"
        row-key="id"
        :tree-props="{ children: 'children' }"
        :row-class-name="getRowClassName"
        class="location-table"
      >
        <el-table-column label="节点" min-width="280" class-name="node-column" label-class-name="node-column__header">
          <template #default="{ row }">
            <div class="node-cell">
              <button
                class="node-cell__icon"
                :class="[`node-cell__icon--${row.type}`, { 'node-cell__icon--expandable': hasChildren(row) }]"
                type="button"
                @click.stop="handleToggleExpand(row)"
              >
                <el-icon><component :is="getTypeIcon(row.type)" /></el-icon>
              </button>
              <div class="node-cell__body">
                <strong>{{ row.name }}</strong>
                <span>{{ row.code }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" effect="light">{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="结构信息" min-width="220">
          <template #default="{ row }">
            <div class="meta-cell">
              <span>子节点 {{ row.children?.length || 0 }}</span>
              <span>{{ row.type === 'warehouse' ? '根节点' : `父级 ${getTypeLabel(row.parentType) || '已关联'}` }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <dict-tag :options="sys_normal_disable" :value="row.status" />
          </template>
        </el-table-column>

        <el-table-column label="操作" align="right" width="220">
          <template #default="{ row }">
            <div class="action-links">
              <el-button link type="primary" icon="Plus" @click="handleAdd(row)" v-if="row.type !== 'shelf'">新增</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(row)" v-if="row.type !== 'warehouse'">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(row)" v-if="row.type !== 'warehouse'">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && locationList.length === 0" description="暂无库位结构数据" />
    </section>

    <el-dialog :title="title" v-model="open" width="620px" class="location-dialog">
      <div class="dialog-intro">
        <span>{{ form.parentName ? `当前父级：${form.parentName}` : '未指定父级，新增时请确认层级类型。' }}</span>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="类型" prop="type">
              <el-radio-group v-model="form.type" :disabled="form.parentId">
                <el-radio label="area" v-if="!form.parentId || form.parentType === 'warehouse'">库区</el-radio>
                <el-radio label="location" v-if="!form.parentId || form.parentType === 'area'">库位</el-radio>
                <el-radio label="shelf" v-if="!form.parentId || form.parentType === 'location'">货架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
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
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WmsLocation">
import { listWarehouse } from "@/api/wms/warehouse";
import { listArea, addArea, updateArea, delArea } from "@/api/wms/area";
import { listLocation, addLocation, updateLocation, delLocation } from "@/api/wms/location";
import { listShelf, addShelf, updateShelf, delShelf } from "@/api/wms/shelf";
import { Box, Grid, Location, OfficeBuilding } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { sys_normal_disable } = proxy.useDict("sys_normal_disable");

const locationList = ref([]);
const warehouseOptions = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const title = ref("");
const tableRef = ref()

const queryWarehouseName = computed(() => {
  const current = warehouseOptions.value.find(item => item.warehouseId === queryParams.value.warehouseId)
  return current?.warehouseName
})

const summaryCards = computed(() => {
  let warehouseCount = 0
  let areaCount = 0
  let locationCount = 0
  let shelfCount = 0
  let disabledCount = 0

  const walk = (nodes) => {
    nodes.forEach(node => {
      if (node.type === 'warehouse') warehouseCount += 1
      if (node.type === 'area') areaCount += 1
      if (node.type === 'location') locationCount += 1
      if (node.type === 'shelf') shelfCount += 1
      if (node.status !== '0') disabledCount += 1
      if (node.children?.length) walk(node.children)
    })
  }

  walk(locationList.value)

  return [
    { key: 'warehouse', label: '仓库', value: warehouseCount, hint: '当前视图的仓库根节点', icon: OfficeBuilding },
    { key: 'area', label: '库区', value: areaCount, hint: '仓库下的区域划分', icon: Grid },
    { key: 'location', label: '库位', value: locationCount, hint: '实际存放单元', icon: Location },
    { key: 'shelf', label: '货架', value: shelfCount, hint: `停用节点 ${disabledCount} 个`, icon: Box }
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
  title.value = "新增";
}

function handleUpdate(row) {
  reset();
  form.value = { ...row };
  open.value = true;
  title.value = "修改";
}

function hasChildren(row) {
  return Array.isArray(row.children) && row.children.length > 0
}

function handleToggleExpand(row) {
  if (!hasChildren(row)) return
  tableRef.value?.toggleRowExpansion(row)
}

function getRowClassName({ row }) {
  return `location-row--level-${row.level || 0}`
}

function getTypeLabel(type) {
  if (type === 'warehouse') return '仓库'
  if (type === 'area') return '库区'
  if (type === 'location') return '库位'
  if (type === 'shelf') return '货架'
  return '节点'
}

function getTypeTagType(type) {
  if (type === 'warehouse') return ''
  if (type === 'area') return 'success'
  if (type === 'location') return 'warning'
  return 'info'
}

function getTypeIcon(type) {
  if (type === 'warehouse') return OfficeBuilding
  if (type === 'area') return Grid
  if (type === 'location') return Location
  return Box
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
  proxy.$modal.confirm('是否确认删除?').then(() => {
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

<style lang="scss" scoped>
.location-page {
  min-height: calc(100vh - 84px);
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(12, 74, 110, 0.08), transparent 30%),
    linear-gradient(180deg, #f3f8fb 0%, #edf3f7 100%);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
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

.page-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
}

.location-summary {
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
  transition: transform 0.25s ease, box-shadow 0.25s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 18px 40px rgba(15, 23, 42, 0.1);
  }
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

.summary-tile--warehouse .summary-tile__icon {
  background: linear-gradient(135deg, #0f766e 0%, #14b8a6 100%);
}

.summary-tile--area .summary-tile__icon {
  background: linear-gradient(135deg, #16a34a 0%, #4ade80 100%);
}

.summary-tile--location .summary-tile__icon {
  background: linear-gradient(135deg, #ea580c 0%, #fb923c 100%);
}

.summary-tile--shelf .summary-tile__icon {
  background: linear-gradient(135deg, #2563eb 0%, #60a5fa 100%);
}

.summary-tile__body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.summary-tile__label {
  font-size: 13px;
  color: #64748b;
}

.summary-tile__value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.1;
  color: #0f172a;
}

.summary-tile__hint {
  font-size: 12px;
  color: #94a3b8;
}

.location-panel {
  padding: 22px 24px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.05);
}

.location-panel + .location-panel {
  margin-top: 18px;
}

.panel-heading {
  margin-bottom: 18px;

  h3 {
    margin: 0 0 6px;
    font-size: 18px;
    font-weight: 600;
    color: #0f172a;
  }

  p {
    margin: 0;
    font-size: 13px;
    color: #64748b;
  }
}

.panel-heading--split {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
}

.panel-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-form {
  margin-bottom: -18px;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.filter-form__actions :deep(.el-form-item__content) {
  display: flex;
  gap: 10px;
}

.location-table :deep(.el-table__header th) {
  background: #f8fafc !important;
  color: #475569;
  font-weight: 600;
}

.location-table :deep(.el-table__placeholder) {
  display: none;
}

.location-table :deep(.el-table__expand-icon) {
  display: none !important;
}

.location-table :deep(.el-table__indent) {
  width: 32px !important;
}

.location-table :deep(.cell.node-column__header) {
  padding-left: 18px;
}

.location-table :deep(.location-row--level-0 > td) {
  background: #f8fbfc;
}

.location-table :deep(.location-row--level-1 > td) {
  background: #f4faf7;
}

.location-table :deep(.location-row--level-2 > td) {
  background: #fff8f1;
}

.location-table :deep(.location-row--level-3 > td) {
  background: #f5f9ff;
}

.location-table :deep(.el-table__row:hover > td) {
  background: #f6fbff !important;
}

.node-cell {
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 48px;
}

.location-table :deep(.location-row--level-1 .node-cell) {
  padding-left: 18px;
}

.location-table :deep(.location-row--level-2 .node-cell) {
  padding-left: 42px;
}

.location-table :deep(.location-row--level-3 .node-cell) {
  padding-left: 66px;
}

.node-cell__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  padding: 0;
  border: 0;
  border-radius: 14px;
  outline: none;
  cursor: default;
  color: #fff;
  font-size: 18px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.node-cell__icon--expandable {
  cursor: pointer;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 10px 18px rgba(15, 23, 42, 0.14);
  }
}

.node-cell__icon--warehouse {
  background: linear-gradient(135deg, #0f766e 0%, #14b8a6 100%);
}

.node-cell__icon--area {
  background: linear-gradient(135deg, #16a34a 0%, #4ade80 100%);
}

.node-cell__icon--location {
  background: linear-gradient(135deg, #ea580c 0%, #fb923c 100%);
}

.node-cell__icon--shelf {
  background: linear-gradient(135deg, #2563eb 0%, #60a5fa 100%);
}

.node-cell__body {
  display: flex;
  flex-direction: column;
  gap: 4px;

  strong {
    color: #0f172a;
  }

  span {
    font-size: 12px;
    color: #94a3b8;
  }
}

.meta-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: #64748b;
}

.action-links {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  align-items: center;
}

.location-dialog :deep(.el-dialog) {
  border-radius: 24px;
  overflow: hidden;
}

.location-dialog :deep(.el-dialog__header) {
  padding: 22px 24px 14px;
}

.location-dialog :deep(.el-dialog__body) {
  padding: 0 24px 20px;
}

.location-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px 24px;
}

.dialog-intro {
  margin-bottom: 18px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #f8fafc;
  font-size: 13px;
  color: #64748b;
}

@media (max-width: 1200px) {
  .location-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .location-page {
    padding: 16px;
  }

  .page-header,
  .panel-heading--split {
    flex-direction: column;
    align-items: flex-start;
  }

  .page-meta {
    align-items: flex-start;
  }

  .location-summary {
    grid-template-columns: 1fr;
  }

  .location-panel {
    padding: 18px;
    border-radius: 20px;
  }

  .panel-actions {
    width: 100%;
    justify-content: space-between;
  }

  .action-links {
    justify-content: flex-start;
  }
}
</style>
