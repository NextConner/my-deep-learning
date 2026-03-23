import request from '@/utils/request'

// 查询库存流水列表
export function listInventoryLog(query) {
  return request({
    url: '/wms/inventoryLog/list',
    method: 'get',
    params: query
  })
}
