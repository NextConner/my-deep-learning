-- =============================================
-- OMS订单管理模块测试数据
-- =============================================

-- 1. 客户
INSERT INTO oms_customer (customer_id, customer_code, customer_name, customer_type, contact_person, contact_phone, contact_email, address, credit_limit, status, create_by) VALUES
(1, 'CUS001', '阿里巴巴集团', '1', '马云', '13900139001', 'mayun@alibaba.com', '杭州市余杭区', 5000000.00, '0', 'admin'),
(2, 'CUS002', '腾讯科技', '1', '马化腾', '13900139002', 'pony@tencent.com', '深圳市南山区', 3000000.00, '0', 'admin'),
(3, 'CUS003', '字节跳动', '1', '张一鸣', '13900139003', 'zhang@bytedance.com', '北京市海淀区', 2000000.00, '0', 'admin'),
(4, 'CUS004', '个人客户-张三', '2', '张三', '13900139004', 'zhangsan@qq.com', '上海市浦东新区', 50000.00, '0', 'admin');

-- 2. 订单主表
INSERT INTO oms_order (order_id, order_no, customer_id, order_date, delivery_date, total_amount, discount_amount, final_amount, order_status, sales_user_id, create_by) VALUES
(1, 'ORD20260301001', 1, '2026-03-01', '2026-03-05', 159990.00, 5000.00, 154990.00, 'RECEIVED', 2, 'admin'),
(2, 'ORD20260305001', 2, '2026-03-05', '2026-03-10', 89970.00, 2000.00, 87970.00, 'SHIPPED', 2, 'admin'),
(3, 'ORD20260310001', 3, '2026-03-10', '2026-03-15', 47994.00, 0.00, 47994.00, 'AUDITED', 2, 'admin'),
(4, 'ORD20260315001', 4, '2026-03-15', '2026-03-20', 9898.00, 100.00, 9798.00, 'CREATED', 2, 'admin');

-- 3. 订单明细
INSERT INTO oms_order_item (order_id, product_id, product_code, product_name, specification, unit, quantity, unit_price, total_price) VALUES
(1, 5, 'PRD005', 'MacBook Pro', 'M3 16GB 512GB', '台', 10, 15999.00, 159990.00),
(2, 1, 'PRD001', 'iPhone 15 Pro', '256GB 深空黑', '台', 10, 7999.00, 79990.00),
(2, 7, 'PRD007', 'AirPods Pro', '第二代', '副', 5, 1899.00, 9495.00),
(3, 4, 'PRD004', '小米14 Ultra', '16GB+512GB', '台', 8, 5999.00, 47992.00),
(4, 2, 'PRD002', 'iPhone 15', '128GB 白色', '台', 1, 5999.00, 5999.00),
(4, 7, 'PRD007', 'AirPods Pro', '第二代', '副', 2, 1899.00, 3798.00);

-- 4. 应收应付
INSERT INTO oms_finance (finance_id, order_id, finance_type, total_amount, paid_amount, unpaid_amount, payment_status, create_by) VALUES
(1, 1, '1', 154990.00, 154990.00, 0.00, 'PAID', 'admin'),
(2, 2, '1', 87970.00, 50000.00, 37970.00, 'PARTIAL', 'admin'),
(3, 3, '1', 47994.00, 0.00, 47994.00, 'UNPAID', 'admin'),
(4, 4, '1', 9798.00, 0.00, 9798.00, 'UNPAID', 'admin');

-- 5. 收付款记录
INSERT INTO oms_payment (finance_id, order_id, payment_amount, payment_date, payment_method, create_by) VALUES
(1, 1, 154990.00, '2026-03-08', '银行转账', 'admin'),
(2, 2, 50000.00, '2026-03-12', '支付宝', 'admin');
