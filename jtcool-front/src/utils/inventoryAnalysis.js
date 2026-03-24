/**
 * 库存流水数据分析工具
 */

/**
 * 按产品分组
 */
export function groupByProduct(logs) {
  const map = new Map()

  logs.forEach(log => {
    if (!map.has(log.productId)) {
      map.set(log.productId, {
        id: log.productId,
        name: log.productName,
        warehouses: new Map(),
        totalIn: 0,
        totalOut: 0,
        netChange: 0,
        eventCount: 0
      })
    }

    const product = map.get(log.productId)

    if (!product.warehouses.has(log.warehouseId)) {
      product.warehouses.set(log.warehouseId, {
        id: log.warehouseId,
        name: log.warehouseName,
        events: []
      })
    }

    product.warehouses.get(log.warehouseId).events.push(log)

    const qty = Number(log.changeQuantity || 0)
    if (log.changeType === 'IN') {
      product.totalIn += qty
      product.netChange += qty
    } else {
      product.totalOut += qty
      product.netChange -= qty
    }
    product.eventCount++
  })

  return Array.from(map.values()).map(p => ({
    ...p,
    warehouses: Array.from(p.warehouses.values())
  }))
}

/**
 * 按仓库分组
 */
export function groupByWarehouse(logs) {
  const map = new Map()

  logs.forEach(log => {
    if (!map.has(log.warehouseId)) {
      map.set(log.warehouseId, {
        id: log.warehouseId,
        name: log.warehouseName,
        products: new Map(),
        totalIn: 0,
        totalOut: 0,
        netChange: 0,
        eventCount: 0
      })
    }

    const warehouse = map.get(log.warehouseId)

    if (!warehouse.products.has(log.productId)) {
      warehouse.products.set(log.productId, {
        id: log.productId,
        name: log.productName,
        events: []
      })
    }

    warehouse.products.get(log.productId).events.push(log)

    const qty = Number(log.changeQuantity || 0)
    if (log.changeType === 'IN') {
      warehouse.totalIn += qty
      warehouse.netChange += qty
    } else {
      warehouse.totalOut += qty
      warehouse.netChange -= qty
    }
    warehouse.eventCount++
  })

  return Array.from(map.values()).map(w => ({
    ...w,
    products: Array.from(w.products.values())
  }))
}

/**
 * 计算统计指标
 */
export function calculateStats(logs) {
  const stats = logs.reduce((acc, log) => {
    const qty = Number(log.changeQuantity || 0)
    if (log.changeType === 'IN') {
      acc.totalIn += qty
      acc.netChange += qty
    } else {
      acc.totalOut += qty
      acc.netChange -= qty
    }
    acc.eventCount++
    return acc
  }, { totalIn: 0, totalOut: 0, netChange: 0, eventCount: 0 })

  return stats
}

/**
 * 计算时间范围
 */
export function getTimeRange(logs) {
  if (!logs.length) {
    return { min: Date.now(), max: Date.now(), span: 1 }
  }
  const times = logs.map(item => new Date(item.createTime).getTime())
  const min = Math.min(...times)
  const max = Math.max(...times)
  return { min, max, span: Math.max(max - min, 1) }
}

/**
 * 生成时间刻度
 */
export function generateTimeTicks(timeRange, count = 6) {
  const { min, max, span } = timeRange
  return Array.from({ length: count }, (_, i) => {
    const time = min + (span * i / (count - 1))
    return {
      time,
      position: (i / (count - 1)) * 100
    }
  })
}
