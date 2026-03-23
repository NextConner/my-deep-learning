import request from '@/utils/request'

// 查询库区列表
export function listArea(query) {
  return request({
    url: '/wms/area/list',
    method: 'get',
    params: query
  })
}

// 查询库区详细
export function getArea(areaId) {
  return request({
    url: '/wms/area/' + areaId,
    method: 'get'
  })
}

// 按仓库查询库区
export function getAreaByWarehouse(warehouseId) {
  return request({
    url: '/wms/area/warehouse/' + warehouseId,
    method: 'get'
  })
}

// 新增库区
export function addArea(data) {
  return request({
    url: '/wms/area',
    method: 'post',
    data: data
  })
}

// 修改库区
export function updateArea(data) {
  return request({
    url: '/wms/area',
    method: 'put',
    data: data
  })
}

// 删除库区
export function delArea(areaId) {
  return request({
    url: '/wms/area/' + areaId,
    method: 'delete'
  })
}
