-- =============================================
-- 产品档案模块测试数据
-- =============================================

-- 1. 产品分类
INSERT INTO prd_category (category_id, parent_id, ancestors, category_name, category_code, order_num, status, create_by) VALUES
(1, 0, '0', '电子产品', 'ELECTRONICS', 1, '0', 'admin'),
(2, 0, '0', '家居用品', 'HOME', 2, '0', 'admin'),
(3, 0, '0', '食品饮料', 'FOOD', 3, '0', 'admin'),
(11, 1, '0,1', '手机', 'PHONE', 1, '0', 'admin'),
(12, 1, '0,1', '电脑', 'COMPUTER', 2, '0', 'admin'),
(13, 1, '0,1', '配件', 'ACCESSORY', 3, '0', 'admin'),
(21, 2, '0,2', '厨房用品', 'KITCHEN', 1, '0', 'admin'),
(22, 2, '0,2', '卫浴用品', 'BATHROOM', 2, '0', 'admin');

-- 2. 品牌
INSERT INTO prd_brand (brand_id, brand_name, brand_code, status, create_by) VALUES
(1, '苹果', 'APPLE', '0', 'admin'),
(2, '华为', 'HUAWEI', '0', 'admin'),
(3, '小米', 'XIAOMI', '0', 'admin'),
(4, '联想', 'LENOVO', '0', 'admin'),
(5, '戴尔', 'DELL', '0', 'admin');

-- 3. 供应商
INSERT INTO prd_supplier (supplier_id, supplier_name, supplier_code, contact_person, contact_phone, contact_email, address, status, create_by) VALUES
(1, '深圳科技有限公司', 'SUP001', '张三', '13800138001', 'zhangsan@example.com', '深圳市南山区科技园', '0', 'admin'),
(2, '上海电子贸易公司', 'SUP002', '李四', '13800138002', 'lisi@example.com', '上海市浦东新区', '0', 'admin'),
(3, '北京数码批发中心', 'SUP003', '王五', '13800138003', 'wangwu@example.com', '北京市海淀区中关村', '0', 'admin');

-- 4. 产品
INSERT INTO prd_product (product_id, product_code, product_name, category_id, brand_id, supplier_id, specification, unit, standard_price, cost_price, min_stock, max_stock, warning_stock, status, create_by) VALUES
(1, 'PRD001', 'iPhone 15 Pro', 11, 1, 1, '256GB 深空黑', '台', 7999.00, 6500.00, 10, 100, 20, '0', 'admin'),
(2, 'PRD002', 'iPhone 15', 11, 1, 1, '128GB 白色', '台', 5999.00, 4800.00, 10, 100, 20, '0', 'admin'),
(3, 'PRD003', 'Mate 60 Pro', 11, 2, 1, '512GB 黑色', '台', 6999.00, 5600.00, 10, 100, 20, '0', 'admin'),
(4, 'PRD004', '小米14 Ultra', 11, 3, 2, '16GB+512GB', '台', 5999.00, 4800.00, 10, 100, 20, '0', 'admin'),
(5, 'PRD005', 'MacBook Pro', 12, 1, 1, 'M3 16GB 512GB', '台', 15999.00, 13000.00, 5, 50, 10, '0', 'admin'),
(6, 'PRD006', 'ThinkPad X1', 12, 4, 3, 'i7 16GB 1TB', '台', 12999.00, 10500.00, 5, 50, 10, '0', 'admin'),
(7, 'PRD007', 'AirPods Pro', 13, 1, 1, '第二代', '副', 1899.00, 1500.00, 20, 200, 40, '0', 'admin'),
(8, 'PRD008', '小米充电宝', 13, 3, 2, '20000mAh', '个', 149.00, 100.00, 50, 500, 100, '0', 'admin');
