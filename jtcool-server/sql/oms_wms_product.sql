-- -- =============================================
-- -- OMS & WMS & Product Archive Database Schema
-- -- PostgreSQL
-- -- =============================================

-- -- =============================================
-- -- 1. 产品档案模块 (Product Archive)
-- -- =============================================

-- -- 1.1 产品分类表
-- CREATE TABLE prd_category (
--     category_id BIGSERIAL PRIMARY KEY,
--     parent_id BIGINT DEFAULT 0,
--     ancestors VARCHAR(500) DEFAULT '',
--     category_name VARCHAR(100) NOT NULL,
--     category_code VARCHAR(50) UNIQUE,
--     order_num INT DEFAULT 0,
--     status CHAR(1) DEFAULT '0',
--     del_flag CHAR(1) DEFAULT '0',
--     create_by VARCHAR(64),
--     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     update_by VARCHAR(64),
--     update_time TIMESTAMP,
--     remark VARCHAR(500)
-- );

-- COMMENT ON TABLE prd_category IS '产品分类表';
-- COMMENT ON COLUMN prd_category.parent_id IS '父分类ID';
-- COMMENT ON COLUMN prd_category.ancestors IS '祖级列表';
-- COMMENT ON COLUMN prd_category.status IS '状态(0正常 1停用)';
-- COMMENT ON COLUMN prd_category.del_flag IS '删除标志(0存在 2删除)';

-- -- 1.2 品牌表
-- CREATE TABLE prd_brand (
--     brand_id BIGSERIAL PRIMARY KEY,
--     brand_name VARCHAR(100) NOT NULL,
--     brand_code VARCHAR(50) UNIQUE,
--     logo_url VARCHAR(500),
--     status CHAR(1) DEFAULT '0',
--     del_flag CHAR(1) DEFAULT '0',
--     create_by VARCHAR(64),
--     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     update_by VARCHAR(64),
--     update_time TIMESTAMP,
--     remark VARCHAR(500)
-- );

-- COMMENT ON TABLE prd_brand IS '品牌表';

-- -- 1.3 供应商表
-- CREATE TABLE prd_supplier (
--     supplier_id BIGSERIAL PRIMARY KEY,
--     supplier_name VARCHAR(200) NOT NULL,
--     supplier_code VARCHAR(50) UNIQUE,
--     contact_person VARCHAR(100),
--     contact_phone VARCHAR(20),
--     contact_email VARCHAR(100),
--     address VARCHAR(500),
--     status CHAR(1) DEFAULT '0',
--     del_flag CHAR(1) DEFAULT '0',
--     create_by VARCHAR(64),
--     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     update_by VARCHAR(64),
--     update_time TIMESTAMP,
--     remark VARCHAR(500)
-- );

-- COMMENT ON TABLE prd_supplier IS '供应商表';

-- -- 1.4 产品主表
-- CREATE TABLE prd_product (
--     product_id BIGSERIAL PRIMARY KEY,
--     product_code VARCHAR(50) UNIQUE NOT NULL,
--     product_name VARCHAR(200) NOT NULL,
--     category_id BIGINT,
--     brand_id BIGINT,
--     supplier_id BIGINT,
--     specification VARCHAR(200),
--     unit VARCHAR(20),
--     standard_price DECIMAL(10,2),
--     cost_price DECIMAL(10,2),
--     min_stock INT DEFAULT 0,
--     max_stock INT DEFAULT 0,
--     warning_stock INT DEFAULT 0,
--     product_image VARCHAR(500),
--     status CHAR(1) DEFAULT '0',
--     del_flag CHAR(1) DEFAULT '0',
--     create_by VARCHAR(64),
--     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     update_by VARCHAR(64),
--     update_time TIMESTAMP,
--     remark VARCHAR(500),
--     FOREIGN KEY (category_id) REFERENCES prd_category(category_id),
--     FOREIGN KEY (brand_id) REFERENCES prd_brand(brand_id),
--     FOREIGN KEY (supplier_id) REFERENCES prd_supplier(supplier_id)
-- );

-- COMMENT ON TABLE prd_product IS '产品主表';
-- COMMENT ON COLUMN prd_product.warning_stock IS '库存预警值';

-- CREATE INDEX idx_product_category ON prd_product(category_id);
-- CREATE INDEX idx_product_brand ON prd_product(brand_id);
-- CREATE INDEX idx_product_supplier ON prd_product(supplier_id);

-- -- =============================================
-- -- 2. OMS 订单管理模块
-- -- =============================================

-- -- 2.1 客户表
-- CREATE TABLE oms_customer (
--     customer_id BIGSERIAL PRIMARY KEY,
--     customer_code VARCHAR(50) UNIQUE NOT NULL,
--     customer_name VARCHAR(200) NOT NULL,
--     customer_type CHAR(1) DEFAULT '1',
--     contact_person VARCHAR(100),
--     contact_phone VARCHAR(20),
--     contact_email VARCHAR(100),
--     address VARCHAR(500),
--     credit_limit DECIMAL(12,2) DEFAULT 0,
--     status CHAR(1) DEFAULT '0',
--     del_flag CHAR(1) DEFAULT '0',
--     create_by VARCHAR(64),
--     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     update_by VARCHAR(64),
--     update_time TIMESTAMP,
--     remark VARCHAR(500)
-- );

-- COMMENT ON TABLE oms_customer IS '客户表';
-- COMMENT ON COLUMN oms_customer.customer_type IS '客户类型(1企业 2个人)';
-- COMMENT ON COLUMN oms_customer.credit_limit IS '信用额度';

-- -- 2.2 订单主表
-- CREATE TABLE oms_order (
--     order_id BIGSERIAL PRIMARY KEY,
--     order_no VARCHAR(50) UNIQUE NOT NULL,
--     customer_id BIGINT NOT NULL,
--     order_date DATE NOT NULL,
--     delivery_date DATE,
--     total_amount DECIMAL(12,2) DEFAULT 0,
--     discount_amount DECIMAL(12,2) DEFAULT 0,
--     final_amount DECIMAL(12,2) DEFAULT 0,
--     order_status VARCHAR(20) DEFAULT 'CREATED',
--     sales_user_id BIGINT,
--     require_shipping_photo CHAR(1) DEFAULT '0',
--     del_flag CHAR(1) DEFAULT '0',
--     create_by VARCHAR(64),
--     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     update_by VARCHAR(64),
--     update_time TIMESTAMP,
--     remark VARCHAR(500),
--     FOREIGN KEY (customer_id) REFERENCES oms_customer(customer_id),
--     FOREIGN KEY (sales_user_id) REFERENCES sys_user(user_id)
-- );

-- COMMENT ON TABLE oms_order IS '订单主表';
-- COMMENT ON COLUMN oms_order.order_status IS '订单状态';
-- COMMENT ON COLUMN oms_order.require_shipping_photo IS '是否需要发货图片(0否 1是)';

-- CREATE INDEX idx_order_customer ON oms_order(customer_id);
-- CREATE INDEX idx_order_sales ON oms_order(sales_user_id);
-- CREATE INDEX idx_order_status ON oms_order(order_status);
-- CREATE INDEX idx_order_date ON oms_order(order_date);

-- -- 2.3 订单明细表
-- CREATE TABLE oms_order_item (
--     item_id BIGSERIAL PRIMARY KEY,
--     order_id BIGINT NOT NULL,
--     product_id BIGINT NOT NULL,
--     product_code VARCHAR(50),
--     product_name VARCHAR(200),
--     specification VARCHAR(200),
--     unit VARCHAR(20),
--     quantity INT NOT NULL,
--     unit_price DECIMAL(10,2) NOT NULL,
--     total_price DECIMAL(12,2) NOT NULL,
--     del_flag CHAR(1) DEFAULT '0',
--     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     FOREIGN KEY (order_id) REFERENCES oms_order(order_id),
--     FOREIGN KEY (product_id) REFERENCES prd_product(product_id)
-- );

-- COMMENT ON TABLE oms_order_item IS '订单明细表';

-- CREATE INDEX idx_order_item_order ON oms_order_item(order_id);
-- CREATE INDEX idx_order_item_product ON oms_order_item(product_id);

-- -- 2.4 订单流程记录表
-- CREATE TABLE oms_order_flow (
--     flow_id BIGSERIAL PRIMARY KEY,
--     order_id BIGINT NOT NULL,
--     flow_status VARCHAR(20) NOT NULL,
--     action_type VARCHAR(20) NOT NULL,
--     operator_id BIGINT,
--     operator_name VARCHAR(100),
--     action_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     reject_reason VARCHAR(500),
--     shipping_photos VARCHAR(2000),
--     remark VARCHAR(500),
--     FOREIGN KEY (order_id) REFERENCES oms_order(order_id)
-- );

-- COMMENT ON TABLE oms_order_flow IS '订单流程记录表';
-- COMMENT ON COLUMN oms_order_flow.action_type IS '操作类型(CONFIRM/REJECT/APPROVE等)';
-- COMMENT ON COLUMN oms_order_flow.shipping_photos IS '发货图片JSON数组';

-- CREATE INDEX idx_order_flow_order ON oms_order_flow(order_id);

-- -- 2.5 应收应付表
-- CREATE TABLE oms_finance (
--     finance_id BIGSERIAL PRIMARY KEY,
--     order_id BIGINT NOT NULL,
--     finance_type CHAR(1) NOT NULL,
--     total_amount DECIMAL(12,2) NOT NULL,
--     paid_amount DECIMAL(12,2) DEFAULT 0,
--     unpaid_amount DECIMAL(12,2) NOT NULL,
--     payment_status VARCHAR(20) DEFAULT 'UNPAID',
--     invoice_status VARCHAR(20) DEFAULT 'NOT_ISSUED',
--     invoice_no VARCHAR(100),
--     invoice_date DATE,
--     invoice_amount DECIMAL(12,2),
--     del_flag CHAR(1) DEFAULT '0',
--     create_by VARCHAR(64),
--     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     update_by VARCHAR(64),
--     update_time TIMESTAMP,
--     remark VARCHAR(500),
--     FOREIGN KEY (order_id) REFERENCES oms_order(order_id)
-- );

-- COMMENT ON TABLE oms_finance IS '应收应付表';
-- COMMENT ON COLUMN oms_finance.finance_type IS '财务类型(1应收 2应付)';
-- COMMENT ON COLUMN oms_finance.payment_status IS '付款状态(UNPAID/PARTIAL/PAID)';
-- COMMENT ON COLUMN oms_finance.invoice_status IS '发票状态(NOT_ISSUED/PARTIAL_ISSUED/ISSUED)';

-- CREATE INDEX idx_finance_order ON oms_finance(order_id);

-- -- 2.6 收付款记录表
-- CREATE TABLE oms_payment (
--     payment_id BIGSERIAL PRIMARY KEY,
--     finance_id BIGINT NOT NULL,
--     order_id BIGINT NOT NULL,
--     payment_amount DECIMAL(12,2) NOT NULL,
--     payment_date DATE NOT NULL,
--     payment_method VARCHAR(20),
--     payment_account VARCHAR(100),
--     voucher_no VARCHAR(100),
--     del_flag CHAR(1) DEFAULT '0',
--     create_by VARCHAR(64),
--     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     remark VARCHAR(500),
--     FOREIGN KEY (finance_id) REFERENCES oms_finance(finance_id),
--     FOREIGN KEY (order_id) REFERENCES oms_order(order_id)
-- );

-- COMMENT ON TABLE oms_payment IS '收付款记录表';

-- CREATE INDEX idx_payment_finance ON oms_payment(finance_id);
-- CREATE INDEX idx_payment_order ON oms_payment(order_id);

-- -- 2.7 订单统计汇总表
-- CREATE TABLE oms_order_statistics (
--     stat_id BIGSERIAL PRIMARY KEY,
--     stat_date DATE NOT NULL,
--     stat_type VARCHAR(20) NOT NULL,
--     customer_id BIGINT,
--     sales_user_id BIGINT,
--     order_count INT DEFAULT 0,
--     total_amount DECIMAL(15,2) DEFAULT 0,
--     paid_amount DECIMAL(15,2) DEFAULT 0,
--     unpaid_amount DECIMAL(15,2) DEFAULT 0,
--     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     UNIQUE(stat_date, stat_type, customer_id, sales_user_id)
-- );

-- COMMENT ON TABLE oms_order_statistics IS '订单统计汇总表';
-- COMMENT ON COLUMN oms_order_statistics.stat_type IS '统计类型(DAILY/MONTHLY/QUARTERLY/YEARLY)';

-- CREATE INDEX idx_stat_date ON oms_order_statistics(stat_date);
-- CREATE INDEX idx_stat_type ON oms_order_statistics(stat_type);

-- -- =============================================
-- -- 3. WMS 仓库管理模块
-- -- =============================================

-- -- 3.1 仓库表
-- CREATE TABLE wms_warehouse (
--     warehouse_id BIGSERIAL PRIMARY KEY,
--     warehouse_code VARCHAR(50) UNIQUE NOT NULL,
--     warehouse_name VARCHAR(200) NOT NULL,
--     warehouse_type VARCHAR(20),
--     address VARCHAR(500),
--     manager_id BIGINT,
--     status CHAR(1) DEFAULT '0',
--     del_flag CHAR(1) DEFAULT '0',
--     create_by VARCHAR(64),
--     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     update_by VARCHAR(64),
--     update_time TIMESTAMP,
--     remark VARCHAR(500),
--     FOREIGN KEY (manager_id) REFERENCES sys_user(user_id)
-- );

-- COMMENT ON TABLE wms_warehouse IS '仓库表';

-- -- 3.2 库区表
-- CREATE TABLE wms_area (
--     area_id BIGSERIAL PRIMARY KEY,
--     warehouse_id BIGINT NOT NULL,
--     area_code VARCHAR(50) NOT NULL,
--     area_name VARCHAR(200) NOT NULL,
--     status CHAR(1) DEFAULT '0',
--     del_flag CHAR(1) DEFAULT '0',
--     create_by VARCHAR(64),
--     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     update_by VARCHAR(64),
--     update_time TIMESTAMP,
--     remark VARCHAR(500),
--     FOREIGN KEY (warehouse_id) REFERENCES wms_warehouse(warehouse_id),
--     UNIQUE(warehouse_id, area_code)
-- );

-- COMMENT ON TABLE wms_area IS '库区表';

-- -- 3.3 库位表
-- CREATE TABLE wms_location (
--     location_id BIGSERIAL PRIMARY KEY,
--     area_id BIGINT NOT NULL,
--     location_code VARCHAR(50) NOT NULL,
--     location_name VARCHAR(200),
--     status CHAR(1) DEFAULT '0',
--     del_flag CHAR(1) DEFAULT '0',
--     create_by VARCHAR(64),
--     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     update_by VARCHAR(64),
--     update_time TIMESTAMP,
--     remark VARCHAR(500),
--     FOREIGN KEY (area_id) REFERENCES wms_area(area_id),
--     UNIQUE(area_id, location_code)
-- );

-- COMMENT ON TABLE wms_location IS '库位表';

-- -- 3.4 货架/托盘表
-- CREATE TABLE wms_shelf (
--     shelf_id BIGSERIAL PRIMARY KEY,
--     location_id BIGINT NOT NULL,
--     shelf_code VARCHAR(50) NOT NULL,
--     shelf_type VARCHAR(20),
--     status CHAR(1) DEFAULT '0',
--     del_flag CHAR(1) DEFAULT '0',
--     create_by VARCHAR(64),
--     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     update_by VARCHAR(64),
--     update_time TIMESTAMP,
--     remark VARCHAR(500),
--     FOREIGN KEY (location_id) REFERENCES wms_location(location_id),
--     UNIQUE(location_id, shelf_code)
-- );

-- COMMENT ON TABLE wms_shelf IS '货架/托盘表';

-- -- 3.5 库存主表
-- CREATE TABLE wms_inventory (
--     inventory_id BIGSERIAL PRIMARY KEY,
--     product_id BIGINT NOT NULL,
--     warehouse_id BIGINT NOT NULL,
--     area_id BIGINT NOT NULL,
--     location_id BIGINT,
--     shelf_id BIGINT,
--     quantity INT DEFAULT 0,
--     locked_quantity INT DEFAULT 0,
--     available_quantity INT DEFAULT 0,
--     version INT DEFAULT 0,
--     del_flag CHAR(1) DEFAULT '0',
--     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     update_time TIMESTAMP,
--     FOREIGN KEY (product_id) REFERENCES prd_product(product_id),
--     FOREIGN KEY (warehouse_id) REFERENCES wms_warehouse(warehouse_id),
--     FOREIGN KEY (area_id) REFERENCES wms_area(area_id),
--     FOREIGN KEY (location_id) REFERENCES wms_location(location_id),
--     FOREIGN KEY (shelf_id) REFERENCES wms_shelf(shelf_id),
--     UNIQUE(product_id, warehouse_id, area_id, location_id, shelf_id)
-- );

-- COMMENT ON TABLE wms_inventory IS '库存主表';
-- COMMENT ON COLUMN wms_inventory.locked_quantity IS '锁定数量';
-- COMMENT ON COLUMN wms_inventory.available_quantity IS '可用数量';
-- COMMENT ON COLUMN wms_inventory.version IS '版本号(乐观锁)';

-- CREATE INDEX idx_inventory_product ON wms_inventory(product_id);
-- CREATE INDEX idx_inventory_warehouse ON wms_inventory(warehouse_id);
-- CREATE INDEX idx_inventory_area ON wms_inventory(area_id);

-- -- 3.6 出入库单主表
-- CREATE TABLE wms_stock_bill (
--     bill_id BIGSERIAL PRIMARY KEY,
--     bill_no VARCHAR(50) UNIQUE NOT NULL,
--     bill_type VARCHAR(20) NOT NULL,
--     bill_date DATE NOT NULL,
--     warehouse_id BIGINT NOT NULL,
--     related_order_id BIGINT,
--     customer_id BIGINT,
--     supplier_id BIGINT,
--     bill_status VARCHAR(20) DEFAULT 'DRAFT',
--     operator_id BIGINT,
--     del_flag CHAR(1) DEFAULT '0',
--     create_by VARCHAR(64),
--     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     update_by VARCHAR(64),
--     update_time TIMESTAMP,
--     remark VARCHAR(500),
--     FOREIGN KEY (warehouse_id) REFERENCES wms_warehouse(warehouse_id),
--     FOREIGN KEY (related_order_id) REFERENCES oms_order(order_id),
--     FOREIGN KEY (customer_id) REFERENCES oms_customer(customer_id),
--     FOREIGN KEY (supplier_id) REFERENCES prd_supplier(supplier_id)
-- );

-- COMMENT ON TABLE wms_stock_bill IS '出入库单主表';
-- COMMENT ON COLUMN wms_stock_bill.bill_type IS '单据类型(IN_PURCHASE/OUT_SALES等)';
-- COMMENT ON COLUMN wms_stock_bill.bill_status IS '单据状态(DRAFT/CONFIRMED/COMPLETED)';

-- CREATE INDEX idx_bill_warehouse ON wms_stock_bill(warehouse_id);
-- CREATE INDEX idx_bill_type ON wms_stock_bill(bill_type);
-- CREATE INDEX idx_bill_date ON wms_stock_bill(bill_date);

-- -- 3.7 出入库单明细表
-- CREATE TABLE wms_stock_bill_item (
--     item_id BIGSERIAL PRIMARY KEY,
--     bill_id BIGINT NOT NULL,
--     product_id BIGINT NOT NULL,
--     area_id BIGINT NOT NULL,
--     location_id BIGINT,
--     shelf_id BIGINT,
--     quantity INT NOT NULL,
--     del_flag CHAR(1) DEFAULT '0',
--     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     FOREIGN KEY (bill_id) REFERENCES wms_stock_bill(bill_id),
--     FOREIGN KEY (product_id) REFERENCES prd_product(product_id),
--     FOREIGN KEY (area_id) REFERENCES wms_area(area_id),
--     FOREIGN KEY (location_id) REFERENCES wms_location(location_id),
--     FOREIGN KEY (shelf_id) REFERENCES wms_shelf(shelf_id)
-- );

-- COMMENT ON TABLE wms_stock_bill_item IS '出入库单明细表';

-- CREATE INDEX idx_bill_item_bill ON wms_stock_bill_item(bill_id);
-- CREATE INDEX idx_bill_item_product ON wms_stock_bill_item(product_id);

-- -- 3.8 库存流水表
-- CREATE TABLE wms_inventory_log (
--     log_id BIGSERIAL PRIMARY KEY,
--     product_id BIGINT NOT NULL,
--     warehouse_id BIGINT NOT NULL,
--     area_id BIGINT NOT NULL,
--     location_id BIGINT,
--     shelf_id BIGINT,
--     bill_id BIGINT,
--     bill_no VARCHAR(50),
--     bill_type VARCHAR(20),
--     change_type VARCHAR(20) NOT NULL,
--     change_quantity INT NOT NULL,
--     before_quantity INT,
--     after_quantity INT,
--     operator_id BIGINT,
--     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     remark VARCHAR(500),
--     FOREIGN KEY (product_id) REFERENCES prd_product(product_id),
--     FOREIGN KEY (bill_id) REFERENCES wms_stock_bill(bill_id)
-- );

-- COMMENT ON TABLE wms_inventory_log IS '库存流水表';
-- COMMENT ON COLUMN wms_inventory_log.change_type IS '变更类型(IN/OUT)';

-- CREATE INDEX idx_log_product ON wms_inventory_log(product_id);
-- CREATE INDEX idx_log_bill ON wms_inventory_log(bill_id);
-- CREATE INDEX idx_log_time ON wms_inventory_log(create_time);

-- -- 3.9 出入库统计汇总表
-- CREATE TABLE wms_stock_statistics (
--     stat_id BIGSERIAL PRIMARY KEY,
--     stat_date DATE NOT NULL,
--     stat_type VARCHAR(20) NOT NULL,
--     warehouse_id BIGINT,
--     area_id BIGINT,
--     location_id BIGINT,
--     product_id BIGINT,
--     customer_id BIGINT,
--     sales_user_id BIGINT,
--     in_quantity INT DEFAULT 0,
--     out_quantity INT DEFAULT 0,
--     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     UNIQUE(stat_date, stat_type, warehouse_id, area_id, location_id, product_id, customer_id, sales_user_id)
-- );

-- COMMENT ON TABLE wms_stock_statistics IS '出入库统计汇总表';

-- CREATE INDEX idx_wms_stat_date ON wms_stock_statistics(stat_date);
-- CREATE INDEX idx_wms_stat_type ON wms_stock_statistics(stat_type);
