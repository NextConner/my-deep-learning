-- 修正菜单component字段，使其与前端实际文件路径匹配
UPDATE sys_menu SET component = 'oms/statistics/order' WHERE menu_id = 1503;
UPDATE sys_menu SET component = 'wms/statistics/stock' WHERE menu_id = 1605;
