# OMS & WMS & 产品档案模块实现文档 v1.0.1

## 版本信息

- **版本号**: v1.0.1
- **完成日期**: 2026-03-23
- **状态**: 后端已完成，待前端开发

---

## 一、模块概览

本次实现新增三大业务模块：

1. **产品档案模块 (Product Archive)** - 产品分类、品牌、供应商、产品主档
2. **OMS 订单管理模块** - 订单全流程、客户管理、应收应付、财务集成
3. **WMS 仓库管理模块** - 仓库层级、库存管理、出入库单、分布式锁

---

## 二、技术栈

- **后端框架**: Spring Boot 3.5.11 + Java 21
- **ORM**: MyBatis 3.0.5
- **数据库**: PostgreSQL
- **缓存**: Redis
- **分布式锁**: Redisson 3.23.5
- **定时任务**: Quartz
- **连接池**: Druid

---

## 三、数据库设计

### 3.1 产品档案模块（4张表）

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `prd_category` | 产品分类表 | category_id, parent_id, category_name, category_code |
| `prd_brand` | 品牌表 | brand_id, brand_name, brand_code, logo_url |
| `prd_supplier` | 供应商表 | supplier_id, supplier_name, contact_person, contact_phone |
| `prd_product` | 产品主表 | product_id, product_code, product_name, category_id, brand_id, supplier_id, standard_price, cost_price, warning_stock |

### 3.2 OMS 订单管理模块（7张表）

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `oms_customer` | 客户表 | customer_id, customer_code, customer_name, credit_limit |
| `oms_order` | 订单主表 | order_id, order_no, customer_id, order_status, sales_user_id, total_amount, final_amount |
| `oms_order_item` | 订单明细表 | item_id, order_id, product_id, quantity, unit_price, total_price |
| `oms_order_flow` | 订单流程记录表 | flow_id, order_id, flow_status, action_type, operator_id, shipping_photos |
| `oms_finance` | 应收应付表 | finance_id, order_id, finance_type, total_amount, paid_amount, unpaid_amount, payment_status, invoice_status |
| `oms_payment` | 收付款记录表 | payment_id, finance_id, order_id, payment_amount, payment_date, payment_method |
| `oms_order_statistics` | 订单统计汇总表 | stat_id, stat_date, stat_type, customer_id, sales_user_id, order_count, total_amount |

### 3.3 WMS 仓库管理模块（9张表）

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `wms_warehouse` | 仓库表 | warehouse_id, warehouse_code, warehouse_name, manager_id |
| `wms_area` | 库区表 | area_id, warehouse_id, area_code, area_name |
| `wms_location` | 库位表 | location_id, area_id, location_code |
| `wms_shelf` | 货架/托盘表 | shelf_id, location_id, shelf_code |
| `wms_inventory` | 库存主表 | inventory_id, product_id, warehouse_id, quantity, locked_quantity, available_quantity, version（乐观锁） |
| `wms_stock_bill` | 出入库单主表 | bill_id, bill_no, bill_type, warehouse_id, related_order_id, bill_status |
| `wms_stock_bill_item` | 出入库单明细表 | item_id, bill_id, product_id, area_id, location_id, shelf_id, quantity |
| `wms_inventory_log` | 库存流水表 | log_id, product_id, bill_id, change_type, change_quantity, before_quantity, after_quantity |
| `wms_stock_statistics` | 出入库统计汇总表 | stat_id, stat_date, stat_type, warehouse_id, product_id, in_quantity, out_quantity |

---

## 四、核心业务流程

### 4.1 订单完整流程

```
下单(CREATED)
  ↓
销售确认(SALES_CONFIRMED)
  ↓
订单审核(AUDITED) → 自动生成应收记录
  ↓
仓库确认(WAREHOUSE_CONFIRMED)
  ↓
登记出库(OUT_REGISTERED) → 创建出库单
  ↓
确认发货(SHIPPED) → 上传发货图片
  ↓
客户签收(RECEIVED)
```

**支持场景**:
- 任意环节可拒绝 → 状态变为 `REJECTED`
- 拒绝原因记录在 `oms_order_flow.reject_reason`
- 发货图片存储在 `oms_order_flow.shipping_photos`（JSON数组）

### 4.2 库存扣减流程（分布式锁 + 乐观锁）

```java
// 1. 获取分布式锁
String lockKey = "inventory:{productId}:{warehouseId}:{areaId}:{locationId}:{shelfId}";
redissonLockUtil.executeWithLock(lockKey, 5, 30, TimeUnit.SECONDS, () -> {
    // 2. 查询库存（SELECT FOR UPDATE）
    WmsInventory inventory = mapper.selectWmsInventoryByKey(...);

    // 3. 校验库存
    if (inventory.getAvailableQuantity() < quantity) {
        throw new RuntimeException("库存不足");
    }

    // 4. 扣减库存（乐观锁）
    int rows = mapper.deductInventory(inventoryId, quantity, version);
    if (rows == 0) {
        throw new RuntimeException("库存更新失败，请重试");
    }

    // 5. 记录库存流水
    mapper.insertWmsInventoryLog(log);
});
```

### 4.3 应收应付流程

1. **订单审核通过** → 自动生成应收记录（`oms_finance`）
2. **收款** → 插入 `oms_payment`，更新 `paid_amount` / `unpaid_amount`
3. **收款完成** → `payment_status` 变为 `PAID`
4. **开具发票** → 更新 `invoice_status` / `invoice_no` / `invoice_date`

---

## 五、后端接口清单

### 5.1 产品档案模块

#### 产品分类
- `GET /product/category/list` - 查询分类列表
- `GET /product/category/{categoryId}` - 查询分类详情
- `POST /product/category` - 新增分类
- `PUT /product/category` - 修改分类
- `DELETE /product/category/{categoryIds}` - 删除分类

#### 品牌管理
- `GET /product/brand/list` - 查询品牌列表
- `GET /product/brand/{brandId}` - 查询品牌详情
- `POST /product/brand` - 新增品牌
- `PUT /product/brand` - 修改品牌
- `DELETE /product/brand/{brandIds}` - 删除品牌

#### 供应商管理
- `GET /product/supplier/list` - 查询供应商列表
- `GET /product/supplier/{supplierId}` - 查询供应商详情
- `POST /product/supplier` - 新增供应商
- `PUT /product/supplier` - 修改供应商
- `DELETE /product/supplier/{supplierIds}` - 删除供应商

#### 产品档案
- `GET /product/product/list` - 查询产品列表
- `GET /product/product/{productId}` - 查询产品详情
- `POST /product/product` - 新增产品
- `PUT /product/product` - 修改产品
- `DELETE /product/product/{productIds}` - 删除产品

### 5.2 OMS 订单管理模块

#### 客户管理
- `GET /oms/customer/list` - 查询客户列表
- `GET /oms/customer/{customerId}` - 查询客户详情
- `POST /oms/customer` - 新增客户
- `PUT /oms/customer` - 修改客户
- `DELETE /oms/customer/{customerIds}` - 删除客户

#### 订单管理
- `GET /oms/order/list` - 查询订单列表
- `GET /oms/order/{orderId}` - 查询订单详情
- `POST /oms/order` - 创建订单
- `PUT /oms/order` - 修改订单
- `DELETE /oms/order/{orderIds}` - 删除订单
- `POST /oms/order/confirm/{orderId}` - 销售确认
- `POST /oms/order/audit/{orderId}` - 订单审核
- `POST /oms/order/reject/{orderId}` - 拒绝订单
- `POST /oms/order/ship/{orderId}` - 确认发货（含图片上传）
- `POST /oms/order/receive/{orderId}` - 客户签收

#### 应收应付管理
- `GET /oms/finance/list` - 查询应收应付列表
- `GET /oms/finance/{financeId}` - 查询应收应付详情
- `POST /oms/finance/payment` - 添加收付款记录
- `PUT /oms/finance/invoice` - 更新发票信息

### 5.3 WMS 仓库管理模块

#### 仓库管理
- `GET /wms/warehouse/list` - 查询仓库列表
- `GET /wms/warehouse/{warehouseId}` - 查询仓库详情
- `POST /wms/warehouse` - 新增仓库
- `PUT /wms/warehouse` - 修改仓库
- `DELETE /wms/warehouse/{warehouseIds}` - 删除仓库

#### 库区管理
- `GET /wms/area/list` - 查询库区列表
- `GET /wms/area/{areaId}` - 查询库区详情
- `GET /wms/area/warehouse/{warehouseId}` - 按仓库查询库区
- `POST /wms/area` - 新增库区
- `PUT /wms/area` - 修改库区
- `DELETE /wms/area/{areaIds}` - 删除库区

#### 库位管理
- `GET /wms/location/list` - 查询库位列表
- `GET /wms/location/{locationId}` - 查询库位详情
- `GET /wms/location/area/{areaId}` - 按库区查询库位
- `POST /wms/location` - 新增库位
- `PUT /wms/location` - 修改库位
- `DELETE /wms/location/{locationIds}` - 删除库位

#### 货架管理
- `GET /wms/shelf/list` - 查询货架列表
- `GET /wms/shelf/{shelfId}` - 查询货架详情
- `GET /wms/shelf/location/{locationId}` - 按库位查询货架
- `POST /wms/shelf` - 新增货架
- `PUT /wms/shelf` - 修改货架
- `DELETE /wms/shelf/{shelfIds}` - 删除货架

#### 库存管理
- `GET /wms/inventory/list` - 查询库存列表
- `GET /wms/inventory/{inventoryId}` - 查询库存详情

#### 出入库单管理
- `GET /wms/stockBill/list` - 查询出入库单列表
- `GET /wms/stockBill/{billId}` - 查询出入库单详情
- `POST /wms/stockBill` - 创建出入库单
- `PUT /wms/stockBill` - 修改出入库单
- `DELETE /wms/stockBill/{billIds}` - 删除出入库单
- `POST /wms/stockBill/confirm/{billId}` - 确认出入库（触发库存变更）

#### 库存流水
- `GET /wms/inventoryLog/list` - 查询库存流水

---

## 六、枚举定义

### 6.1 订单状态枚举 (OrderStatusEnum)

| 枚举值 | 说明 |
|--------|------|
| `CREATED` | 已下单 |
| `SALES_CONFIRMED` | 销售确认 |
| `AUDITED` | 订单审核通过 |
| `WAREHOUSE_CONFIRMED` | 仓库确认 |
| `OUT_REGISTERED` | 登记出库 |
| `SHIPPED` | 确认发货 |
| `RECEIVED` | 客户签收 |
| `REJECTED` | 已拒绝 |
| `CANCELLED` | 已取消 |

### 6.2 单据类型枚举 (BillTypeEnum)

| 枚举值 | 说明 |
|--------|------|
| `IN_PURCHASE` | 采购入库 |
| `IN_RETURN` | 退货入库 |
| `IN_OTHER` | 其他入库 |
| `OUT_SALES` | 销售出库 |
| `OUT_RETURN` | 退货出库 |
| `OUT_LOSS` | 损耗出库 |
| `OUT_OTHER` | 其他出库 |

---

## 七、定时任务

### 7.1 OMS 订单统计任务

**任务类**: `com.jtcool.quartz.task.OmsOrderStatisticsJob`

**方法**:
- `aggregateDaily()` - 每日统计（建议 cron: `0 0 2 * * ?`）
- `aggregateMonthly()` - 每月统计（建议 cron: `0 0 2 1 * ?`）
- `aggregateQuarterly()` - 每季统计
- `aggregateYearly()` - 每年统计

**统计维度**:
- 按客户分组
- 按销售人员分组
- 统计指标：订单数、总金额、已收金额、未收金额

### 7.2 WMS 出入库统计任务

**任务类**: `com.jtcool.quartz.task.WmsStockStatisticsJob`

**方法**:
- `aggregateDaily()` - 每日统计（建议 cron: `0 0 3 * * ?`）
- `aggregateMonthly()` - 每月统计（建议 cron: `0 0 3 1 * ?`）
- `aggregateQuarterly()` - 每季统计
- `aggregateYearly()` - 每年统计

**统计维度**:
- 按仓库分组
- 按库区分组
- 按库位分组
- 按产品分组
- 按客户分组
- 按销售人员分组
- 统计指标：入库数量、出库数量

---

## 八、核心工具类

### 8.1 RedissonLockUtil

**位置**: `com.jtcool.common.utils.RedissonLockUtil`

**核心方法**:
```java
// 尝试获取锁
boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit)

// 释放锁
void unlock(String lockKey)

// 带锁执行回调
<T> T executeWithLock(String lockKey, long waitTime, long leaseTime,
                      TimeUnit unit, LockCallback<T> callback)
```

**使用示例**:
```java
redissonLockUtil.executeWithLock("inventory:123:1:1:1:1", 5, 30, TimeUnit.SECONDS, () -> {
    // 业务逻辑
    return result;
});
```

---

## 九、前端开发指南

### 9.1 需要开发的页面

#### 产品档案模块
1. **产品分类管理** (`views/product/category/index.vue`)
   - 树形结构展示
   - 支持新增、编辑、删除

2. **品牌管理** (`views/product/brand/index.vue`)
   - 列表展示
   - 支持品牌logo上传

3. **供应商管理** (`views/product/supplier/index.vue`)
   - 列表展示
   - 联系人信息维护

4. **产品档案** (`views/product/product/index.vue`)
   - 列表展示
   - 关联分类、品牌、供应商
   - 产品图片上传
   - 库存预警设置

#### OMS 订单管理模块
1. **客户管理** (`views/oms/customer/index.vue`)
   - 客户列表
   - 信用额度管理

2. **订单管理** (`views/oms/order/index.vue`)
   - 订单列表（支持状态筛选）
   - 订单详情页（含明细、流程记录）
   - 订单创建（选择客户、产品、数量）
   - 状态操作按钮（确认、审核、拒绝、发货、签收）
   - 发货图片上传组件

3. **应收应付管理** (`views/oms/finance/index.vue`)
   - 应收应付列表
   - 收付款记录
   - 发票信息维护

4. **订单统计报表** (`views/oms/statistics/order.vue`)
   - 日报、月报、季报、年报切换
   - 按客户、销售人员维度查询
   - 图表展示（订单数、金额趋势）

#### WMS 仓库管理模块
1. **仓库管理** (`views/wms/warehouse/index.vue`)
   - 仓库列表
   - 仓库管理员设置

2. **库区/库位/货架管理** (`views/wms/area/index.vue`)
   - 层级联动展示
   - 支持按仓库筛选

3. **库存查询** (`views/wms/inventory/index.vue`)
   - 多维度查询（产品、仓库、库区、库位）
   - 显示可用库存、锁定库存

4. **出入库单管理** (`views/wms/stockBill/index.vue`)
   - 出入库单列表
   - 单据创建（选择单据类型、产品、数量、库位）
   - 单据确认（触发库存变更）

5. **库存流水** (`views/wms/inventoryLog/index.vue`)
   - 流水查询
   - 显示变更前后数量

6. **出入库统计报表** (`views/wms/statistics/stock.vue`)
   - 日报、月报、季报、年报切换
   - 按仓库、产品、客户维度查询
   - 图表展示（入库、出库趋势）

### 9.2 关键前端组件

#### 订单状态流程组件
```vue
<template>
  <el-steps :active="currentStep" finish-status="success">
    <el-step title="已下单" />
    <el-step title="销售确认" />
    <el-step title="订单审核" />
    <el-step title="仓库确认" />
    <el-step title="登记出库" />
    <el-step title="确认发货" />
    <el-step title="客户签收" />
  </el-steps>
</template>
```

#### 发货图片上传组件
```vue
<el-upload
  action="/common/upload"
  list-type="picture-card"
  :on-success="handleUploadSuccess"
  multiple
>
  <i class="el-icon-plus"></i>
</el-upload>
```

#### 库位级联选择器
```vue
<el-cascader
  v-model="selectedLocation"
  :options="warehouseTree"
  :props="{ value: 'id', label: 'name', children: 'children' }"
  placeholder="选择仓库/库区/库位/货架"
/>
```

### 9.3 API 调用示例

```javascript
// 查询订单列表
export function listOrder(query) {
  return request({
    url: '/oms/order/list',
    method: 'get',
    params: query
  })
}

// 确认发货
export function shipOrder(orderId, data) {
  return request({
    url: `/oms/order/ship/${orderId}`,
    method: 'post',
    data: data
  })
}

// 确认出入库单
export function confirmStockBill(billId, operatorId) {
  return request({
    url: `/wms/stockBill/confirm/${billId}`,
    method: 'post',
    params: { operatorId }
  })
}
```

---

## 十、测试要点

### 10.1 订单流程测试
- 完整流程：创建 → 确认 → 审核 → 发货 → 签收
- 拒绝场景：各环节拒绝后状态正确
- 发货图片：上传、显示、删除

### 10.2 库存并发测试
- 使用 JMeter 模拟 100 并发扣减同一产品
- 验证最终库存数量准确
- 验证无超卖现象

### 10.3 应收应付测试
- 订单审核后自动生成应收
- 多次收款金额累加正确
- 收款完成后状态更新为 PAID

### 10.4 统计报表测试
- 手动执行定时任务
- 对比汇总数据与原始数据
- 验证多维度统计准确性

---

## 十一、已完成的 Git 提交

```
a484c7c feat: 完成WMS模块Mapper/Service/Controller及修复Redisson依赖
dfef46a feat: 添加OMS/WMS统计报表定时任务
74de9a4 feat: 完成OMS模块Mapper/Service/Controller
17103de feat: 完成产品档案模块Mapper/Service/Controller
780f59e feat: 添加Product/OMS/WMS模块核心实体类
598b53f feat: 添加OMS/WMS/产品档案模块基础架构
```

---

## 十二、后续工作

### 前端开发任务
1. 创建 Vue 页面（参考第九章页面清单）
2. 实现表单验证
3. 集成文件上传组件
4. 实现图表展示（ECharts）
5. 优化用户体验（加载状态、错误提示）

### 集成测试
1. 端到端订单流程测试
2. 库存并发压力测试
3. 统计报表数据准确性验证
4. 性能优化

---

**文档维护**: 如有接口变更或新增功能，请及时更新本文档。
