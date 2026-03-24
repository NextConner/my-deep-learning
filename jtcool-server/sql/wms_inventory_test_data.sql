-- =============================================
-- wms_inventory 测试数据
-- 基于现有测试主数据生成 100 条库存记录
-- 适用数据库: PostgreSQL
-- 依赖: prd_product、wms_warehouse、wms_area、wms_location 已有测试数据
-- =============================================

INSERT INTO wms_inventory (
    product_id,
    warehouse_id,
    area_id,
    location_id,
    shelf_id,
    quantity,
    locked_quantity,
    available_quantity,
    version,
    del_flag,
    create_time,
    update_time
)
WITH location_slots AS (
    SELECT *
    FROM (
        VALUES
            (1, 1, 1),
            (1, 2, 3),
            (2, 3, 5),
            (2, 4, 7),
            (3, 5, 9)
    ) AS t(warehouse_id, area_id, location_id)
),
seed AS (
    SELECT
        p.product_id,
        s.warehouse_id,
        s.area_id,
        s.location_id,
        ROW_NUMBER() OVER (ORDER BY p.product_id, s.warehouse_id, s.area_id, s.location_id) AS rn
    FROM generate_series(1, 20) AS p(product_id)
    CROSS JOIN location_slots s
)
SELECT
    product_id,
    warehouse_id,
    area_id,
    location_id,
    NULL AS shelf_id,
    60 + ((rn * 13) % 241) AS quantity,
    (rn * 7) % 31 AS locked_quantity,
    (60 + ((rn * 13) % 241)) - ((rn * 7) % 31) AS available_quantity,
    0 AS version,
    '0' AS del_flag,
    TIMESTAMP '2024-03-24 09:00:00' + ((rn - 1) * INTERVAL '10 minute') AS create_time,
    NULL AS update_time
FROM seed
ORDER BY rn;
