import request from '@/utils/request'

// 查询货架列表
export function listShelf(query) {
  return request({
    url: '/wms/shelf/list',
    method: 'get',
    params: query
  })
}

// 查询货架详细
export function getShelf(shelfId) {
  return request({
    url: '/wms/shelf/' + shelfId,
    method: 'get'
  })
}

// 按库位查询货架
export function getShelfByLocation(locationId) {
  return request({
    url: '/wms/shelf/location/' + locationId,
    method: 'get'
  })
}

// 新增货架
export function addShelf(data) {
  return request({
    url: '/wms/shelf',
    method: 'post',
    data: data
  })
}

// 修改货架
export function updateShelf(data) {
  return request({
    url: '/wms/shelf',
    method: 'put',
    data: data
  })
}

// 删除货架
export function delShelf(shelfId) {
  return request({
    url: '/wms/shelf/' + shelfId,
    method: 'delete'
  })
}
