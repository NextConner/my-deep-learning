<template>
  <div class="app-container modern-page">
    <div class="search-card">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
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
    </div>

    <div class="action-bar">
    <el-row :gutter="10">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    </div>

    <div class="table-card">
    <el-table v-loading="loading" :data="locationList" row-key="id" :tree-props="{children: 'children'}">
      <el-table-column label="名称" prop="name" width="200" />
      <el-table-column label="编码" prop="code" />
      <el-table-column label="类型" prop="type">
        <template #default="scope">
          <el-tag v-if="scope.row.type === 'warehouse'">仓库</el-tag>
          <el-tag v-else-if="scope.row.type === 'area'" type="success">库区</el-tag>
          <el-tag v-else-if="scope.row.type === 'location'" type="warning">库位</el-tag>
          <el-tag v-else type="info">货架</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200">
        <template #default="scope">
          <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)">新增</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>

    <el-dialog :title="title" v-model="open" width="600px" class="modern-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type" :disabled="form.parentId">
            <el-radio label="area" v-if="!form.parentId || form.parentType === 'warehouse'">库区</el-radio>
            <el-radio label="location" v-if="!form.parentId || form.parentType === 'area'">库位</el-radio>
            <el-radio label="shelf" v-if="!form.parentId || form.parentType === 'location'">货架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入编码" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
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

const { proxy } = getCurrentInstance();
const { sys_normal_disable } = proxy.useDict("sys_normal_disable");

const locationList = ref([]);
const warehouseOptions = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const title = ref("");

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
    name: w.warehouseName,
    code: w.warehouseCode,
    type: 'warehouse',
    status: w.status,
    warehouseId: w.warehouseId,
    children: []
  }));

  areas.forEach(a => {
    const warehouse = tree.find(w => w.warehouseId === a.warehouseId);
    if (warehouse) {
      warehouse.children.push({
        id: 'a-' + a.areaId,
        name: a.areaName,
        code: a.areaCode,
        type: 'area',
        status: a.status,
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
          name: l.locationCode,
          code: l.locationCode,
          type: 'location',
          status: l.status,
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
            name: s.shelfCode,
            code: s.shelfCode,
            type: 'shelf',
            status: s.status,
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
@import "@/assets/styles/modern-page.scss";
</style>

