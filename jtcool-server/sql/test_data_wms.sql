-- =============================================
-- WMS仓库管理模块测试数据
-- =============================================

-- 1. 仓库
INSERT INTO wms_warehouse (warehouse_id, warehouse_code, warehouse_name, warehouse_type, address, manager_id, status, create_by) VALUES
(1, 'WH001', '深圳总仓', 'MAIN', '深圳市宝安区物流园', 2, '0', 'admin'),
(2, 'WH002', '上海分仓', 'BRANCH', '上海市松江区工业园', 2, '0', 'admin'),
(3, 'WH003', '北京分仓', 'BRANCH', '北京市大兴区物流中心', 2, '0', 'admin');

-- 2. 库区
INSERT INTO wms_area (area_id, warehouse_id, area_code, area_name, status, create_by) VALUES
(1, 1, 'A01', 'A区-电子产品', '0', 'admin'),
(2, 1, 'A02', 'B区-配件', '0', 'admin'),
(3, 2, 'B01', 'A区-电子产品', '0', 'admin'),
(4, 3, 'C01', 'A区-电子产品', '0', 'admin');

-- 3. 库位
INSERT INTO wms_location (location_id, area_id, location_code, location_name, status, create_by) VALUES
(1, 1, 'A01-01', 'A区1号位', '0', 'admin'),
(2, 1, 'A01-02', 'A区2号位', '0', 'admin'),
(3, 2, 'A02-01', 'B区1号位', '0', 'admin'),
(4, 3, 'B01-01', '上海A区1号位', '0', 'admin');

-- 4. 货架
INSERT INTO wms_shelf (shelf_id, location_id, shelf_code, shelf_type, status, create_by) VALUES
(1, 1, 'S001', '标准货架', '0', 'admin'),
(2, 1, 'S002', '标准货架', '0', 'admin'),
(3, 2, 'S003', '标准货架', '0', 'admin');

-- 5. 库存
INSERT INTO wms_inventory (product_id, warehouse_id, area_id, location_id, shelf_id, quantity, locked_quantity, available_quantity) VALUES
(1, 1, 1, 1, 1, 50, 10, 40),
(2, 1, 1, 1, 2, 80, 0, 80),
(3, 1, 1, 2, 3, 60, 8, 52),
(4, 1, 1, 2, 3, 45, 0, 45),
(5, 1, 1, 1, 1, 20, 10, 10),
(7, 1, 2, 3, NULL, 150, 5, 145),
(8, 1, 2, 3, NULL, 300, 0, 300);
