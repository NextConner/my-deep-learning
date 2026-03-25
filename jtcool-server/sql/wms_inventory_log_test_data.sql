-- =============================================
-- wms_inventory_log 测试数据
-- 基于现有 wms_stock_bill 生成 100 条库存流水记录
-- 适用数据库: PostgreSQL
-- =============================================

INSERT INTO wms_inventory_log (
    product_id,
    warehouse_id,
    area_id,
    location_id,
    shelf_id,
    bill_id,
    bill_no,
    bill_type,
    change_type,
    change_quantity,
    before_quantity,
    after_quantity,
    operator_id,
    create_time,
    remark
)
WITH selected_bills AS (
    SELECT
        bill_id,
        bill_no,
        bill_type,
        warehouse_id,
        operator_id,
        ROW_NUMBER() OVER (ORDER BY bill_id) AS rn
    FROM wms_stock_bill
    ORDER BY bill_id
    LIMIT 25
),
seed AS (
    SELECT
        b.bill_id,
        b.bill_no,
        b.bill_type,
        b.warehouse_id,
        COALESCE(b.operator_id, 2) AS operator_id,
        b.rn,
        g.n,
        ((b.rn + g.n - 2) % 20) + 1 AS product_id,
        CASE
            WHEN b.bill_type LIKE 'IN_%' THEN 'IN'
            ELSE 'OUT'
        END AS change_type,
        5 + ((b.rn * 3 + g.n * 2) % 21) AS change_quantity,
        120 + ((b.rn * 11 + g.n * 7) % 180) AS before_quantity
    FROM selected_bills b
    CROSS JOIN generate_series(1, 4) AS g(n)
)
SELECT
    s.product_id,
    s.warehouse_id,
    ((s.warehouse_id - 1) * 2 + CASE WHEN s.n % 2 = 1 THEN 1 ELSE 2 END) AS area_id,
    ((s.warehouse_id - 1) * 4 + CASE WHEN s.n % 2 = 1 THEN 1 ELSE 3 END) AS location_id,
    NULL AS shelf_id,
    s.bill_id,
    s.bill_no,
    s.bill_type,
    s.change_type,
    s.change_quantity,
    s.before_quantity,
    CASE
        WHEN s.change_type = 'IN' THEN s.before_quantity + s.change_quantity
        ELSE s.before_quantity - s.change_quantity
    END AS after_quantity,
    s.operator_id,
    TIMESTAMP '2024-03-17 09:00:00'
        + ((s.rn - 1) * INTERVAL '2 hour')
        + ((s.n - 1) * INTERVAL '15 minute') AS create_time,
    CASE
        WHEN s.change_type = 'IN' THEN '测试入库流水-' || s.bill_no || '-' || s.n
        ELSE '测试出库流水-' || s.bill_no || '-' || s.n
    END AS remark
FROM seed s
ORDER BY s.bill_id, s.n;
