import request from '@/utils/request'

// 查询库存列表
export function listInventory(query) {
  return request({
    url: '/wms/inventory/list',
    method: 'get',
    params: query
  })
}

// 查询库存详细
export function getInventory(inventoryId) {
  return request({
    url: '/wms/inventory/' + inventoryId,
    method: 'get'
  })
}
