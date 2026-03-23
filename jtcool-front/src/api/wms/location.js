import request from '@/utils/request'

// 查询库位列表
export function listLocation(query) {
  return request({
    url: '/wms/location/list',
    method: 'get',
    params: query
  })
}

// 查询库位详细
export function getLocation(locationId) {
  return request({
    url: '/wms/location/' + locationId,
    method: 'get'
  })
}

// 按库区查询库位
export function getLocationByArea(areaId) {
  return request({
    url: '/wms/location/area/' + areaId,
    method: 'get'
  })
}

// 新增库位
export function addLocation(data) {
  return request({
    url: '/wms/location',
    method: 'post',
    data: data
  })
}

// 修改库位
export function updateLocation(data) {
  return request({
    url: '/wms/location',
    method: 'put',
    data: data
  })
}

// 删除库位
export function delLocation(locationId) {
  return request({
    url: '/wms/location/' + locationId,
    method: 'delete'
  })
}
