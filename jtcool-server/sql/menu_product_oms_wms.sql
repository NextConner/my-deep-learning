-- 产品档案模块菜单
insert into sys_menu values('14', '产品档案', '0', '14', 'product', null, '', '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', CURRENT_TIMESTAMP, '', null, '产品档案目录');
insert into sys_menu values('1400', '产品分类', '14', '1', 'category', 'product/category/index', '', '', 1, 0, 'C', '0', '0', 'product:category:list', 'tree', 'admin', CURRENT_TIMESTAMP, '', null, '产品分类菜单');
insert into sys_menu values('1401', '品牌管理', '14', '2', 'brand', 'product/brand/index', '', '', 1, 0, 'C', '0', '0', 'product:brand:list', 'star', 'admin', CURRENT_TIMESTAMP, '', null, '品牌管理菜单');
insert into sys_menu values('1402', '供应商管理', '14', '3', 'supplier', 'product/supplier/index', '', '', 1, 0, 'C', '0', '0', 'product:supplier:list', 'peoples', 'admin', CURRENT_TIMESTAMP, '', null, '供应商管理菜单');
insert into sys_menu values('1403', '产品档案', '14', '4', 'productlist', 'product/product/index', '', '', 1, 0, 'C', '0', '0', 'product:product:list', 'shopping', 'admin', CURRENT_TIMESTAMP, '', null, '产品档案菜单');

-- OMS订单管理模块菜单
insert into sys_menu values('15', '订单管理', '0', '15', 'oms', null, '', '', 1, 0, 'M', '0', '0', '', 'list', 'admin', CURRENT_TIMESTAMP, '', null, '订单管理目录');
insert into sys_menu values('1500', '客户管理', '15', '1', 'customer', 'oms/customer/index', '', '', 1, 0, 'C', '0', '0', 'oms:customer:list', 'user', 'admin', CURRENT_TIMESTAMP, '', null, '客户管理菜单');
insert into sys_menu values('1501', '订单管理', '15', '2', 'order', 'oms/order/index', '', '', 1, 0, 'C', '0', '0', 'oms:order:list', 'list', 'admin', CURRENT_TIMESTAMP, '', null, '订单管理菜单');
insert into sys_menu values('1502', '应收应付', '15', '3', 'finance', 'oms/finance/index', '', '', 1, 0, 'C', '0', '0', 'oms:finance:list', 'money', 'admin', CURRENT_TIMESTAMP, '', null, '应收应付菜单');
insert into sys_menu values('1503', '订单统计', '15', '4', 'statistics/order', 'oms/statistics/order', '', '', 1, 0, 'C', '0', '0', 'oms:statistics:order', 'chart', 'admin', CURRENT_TIMESTAMP, '', null, '订单统计菜单');

-- WMS仓库管理模块菜单
insert into sys_menu values('16', '仓库管理', '0', '16', 'wms', null, '', '', 1, 0, 'M', '0', '0', '', 'example', 'admin', CURRENT_TIMESTAMP, '', null, '仓库管理目录');
insert into sys_menu values('1600', '仓库管理', '16', '1', 'warehouse', 'wms/warehouse/index', '', '', 1, 0, 'C', '0', '0', 'wms:warehouse:list', 'example', 'admin', CURRENT_TIMESTAMP, '', null, '仓库管理菜单');
insert into sys_menu values('1601', '库位管理', '16', '2', 'location', 'wms/location/index', '', '', 1, 0, 'C', '0', '0', 'wms:location:list', 'tree-table', 'admin', CURRENT_TIMESTAMP, '', null, '库位管理菜单');
insert into sys_menu values('1602', '库存查询', '16', '3', 'inventory', 'wms/inventory/index', '', '', 1, 0, 'C', '0', '0', 'wms:inventory:list', 'search', 'admin', CURRENT_TIMESTAMP, '', null, '库存查询菜单');
insert into sys_menu values('1603', '出入库单', '16', '4', 'stockBill', 'wms/stockBill/index', '', '', 1, 0, 'C', '0', '0', 'wms:stockBill:list', 'form', 'admin', CURRENT_TIMESTAMP, '', null, '出入库单菜单');
insert into sys_menu values('1604', '库存流水', '16', '5', 'inventoryLog', 'wms/inventoryLog/index', '', '', 1, 0, 'C', '0', '0', 'wms:inventoryLog:list', 'log', 'admin', CURRENT_TIMESTAMP, '', null, '库存流水菜单');
insert into sys_menu values('1605', '出入库统计', '16', '6', 'statistics/stock', 'wms/statistics/stock', '', '', 1, 0, 'C', '0', '0', 'wms:statistics:stock', 'chart', 'admin', CURRENT_TIMESTAMP, '', null, '出入库统计菜单');
