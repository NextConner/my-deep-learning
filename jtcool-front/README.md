<p align="center">
	<img alt="logo" src="https://oscimg.oschina.net/oscnet/up-d3d0a9303e11d522a06cd263f3079027715.png">
</p>
<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">JtCool v3.9.1</h1>
<h4 align="center">基于SpringBoot+Vue3前后端分离的Java快速开发框架</h4>
<p align="center">
	<a href="https://gitee.com/y_project/JtCool-Vue/stargazers"><img src="https://gitee.com/y_project/JtCool-Vue/badge/star.svg?theme=dark"></a>
	<a href="https://gitee.com/y_project/JtCool-Vue"><img src="https://img.shields.io/badge/JtCool-v3.9.1-brightgreen.svg"></a>
	<a href="https://gitee.com/y_project/JtCool-Vue/blob/master/LICENSE"><img src="https://img.shields.io/github/license/mashape/apistatus.svg"></a>
</p>


## 内置功能

1.  用户管理：用户是系统操作者，该功能主要完成系统用户配置。
2.  部门管理：配置系统组织机构（公司、部门、小组），树结构展现支持数据权限。
3.  岗位管理：配置系统用户所属担任职务。
4.  菜单管理：配置系统菜单，操作权限，按钮权限标识等。
5.  角色管理：角色菜单权限分配、设置角色按机构进行数据范围权限划分。
6.  字典管理：对系统中经常使用的一些较为固定的数据进行维护。
7.  参数管理：对系统动态配置常用参数。
8.  通知公告：系统通知公告信息发布维护。
9.  操作日志：系统正常操作日志记录和查询；系统异常信息日志记录和查询。
10. 登录日志：系统登录日志记录查询包含登录异常。
11. 在线用户：当前系统中活跃用户状态监控。
12. 定时任务：在线（添加、修改、删除)任务调度包含执行结果日志。
13. 代码生成：前后端代码的生成（java、html、xml、sql）支持CRUD下载 。
14. 系统接口：根据业务代码自动生成相关的api接口文档。
15. 服务监控：监视当前系统CPU、内存、磁盘、堆栈等相关信息。
16. 缓存监控：对系统的缓存信息查询，命令统计等。
17. 在线构建器：拖动表单元素生成相应的HTML代码。
18. 连接池监视：监视当前系统数据库连接池状态，可进行分析SQL找出系统性能瓶颈。

## 个人工作台接口文档

首页 `src/views/index.vue` 已调整为个人工作台单屏模式，当前页面需要以下接口支持。

### 1. 工作台概览

- 请求方式：`GET`
- 请求地址：`/workbench/overview`
- 接口说明：返回首页摘要卡片和订单统计信息

响应示例：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "pendingOrderCount": 4,
    "pendingApprovalCount": 3,
    "pendingInventoryCount": 4,
    "yesterdayOrders": 128,
    "yesterdaySales": 186420,
    "yesterdayAvg": 1456,
    "yesterdayCancelled": 6,
    "sevenDayOrders": 863,
    "sevenDaySales": 1268900,
    "sevenDayAverage": 123,
    "fulfillmentRate": 96.4
  }
}
```

字段说明：

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| pendingOrderCount | number | 待处理订单数 |
| pendingApprovalCount | number | 待处理审批数 |
| pendingInventoryCount | number | 待处理出入库单数 |
| yesterdayOrders | number | 昨日订单数 |
| yesterdaySales | number | 昨日成交金额 |
| yesterdayAvg | number | 昨日客单价 |
| yesterdayCancelled | number | 昨日取消单数 |
| sevenDayOrders | number | 近 7 日订单总量 |
| sevenDaySales | number | 近 7 日成交金额 |
| sevenDayAverage | number | 近 7 日日均订单数 |
| fulfillmentRate | number | 订单完成率，返回数值即可，前端格式化百分比 |

### 2. 待处理订单列表

- 请求方式：`GET`
- 请求地址：`/workbench/pendingOrders`
- 接口说明：返回首页预览和弹窗详情使用的待处理订单数据

响应示例：

```json
{
  "code": 200,
  "msg": "操作成功",
  "rows": [
    {
      "orderNo": "SO20260317001",
      "customerName": "华东工业设备有限公司",
      "orderType": "销售订单",
      "amount": 18600,
      "priority": "高",
      "priorityType": "danger",
      "createTime": "2026-03-17 09:10:00"
    }
  ],
  "total": 1
}
```

字段说明：

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| orderNo | string | 订单编号 |
| customerName | string | 客户名称 |
| orderType | string | 订单类型 |
| amount | number | 订单金额 |
| priority | string | 优先级文本，如高、中、低 |
| priorityType | string | 标签类型，建议返回 `danger`、`warning`、`success` |
| createTime | string | 下单时间 |

### 3. 待处理审批列表

- 请求方式：`GET`
- 请求地址：`/workbench/pendingApprovals`
- 接口说明：返回首页预览和弹窗详情使用的审批数据

响应示例：

```json
{
  "code": 200,
  "msg": "操作成功",
  "rows": [
    {
      "id": 1,
      "title": "采购申请审批",
      "applicant": "王晓峰",
      "submitTime": "2026-03-17 09:20:00",
      "level": "紧急",
      "levelType": "danger",
      "amount": 12300
    }
  ],
  "total": 1
}
```

字段说明：

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | number | 审批单主键 |
| title | string | 审批标题 |
| applicant | string | 申请人 |
| submitTime | string | 提交时间 |
| level | string | 审批级别 |
| levelType | string | 标签类型，建议返回 `danger`、`warning`、`primary` |
| amount | number | 关联金额 |

### 4. 待处理出入库单列表

- 请求方式：`GET`
- 请求地址：`/workbench/pendingInventory`
- 接口说明：返回首页预览和弹窗详情使用的出入库待办数据

响应示例：

```json
{
  "code": 200,
  "msg": "操作成功",
  "rows": [
    {
      "docNo": "RK20260317001",
      "bizType": "采购入库",
      "warehouseName": "上海一号仓",
      "sourceNo": "PO20260316008",
      "status": "待收货",
      "statusType": "warning",
      "createTime": "2026-03-17 08:50:00",
      "owner": "刘晨"
    }
  ],
  "total": 1
}
```

字段说明：

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| docNo | string | 单据编号 |
| bizType | string | 业务类型，如采购入库、销售出库、仓库调拨 |
| warehouseName | string | 仓库名称 |
| sourceNo | string | 关联单号 |
| status | string | 当前状态 |
| statusType | string | 标签类型，建议返回 `warning`、`danger`、`primary`、`success` |
| createTime | string | 创建时间 |
| owner | string | 负责人 |
