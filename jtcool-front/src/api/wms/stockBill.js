import request from '@/utils/request'

// 查询出入库单列表
export function listStockBill(query) {
  return request({
    url: '/wms/stockBill/list',
    method: 'get',
    params: query
  })
}

// 查询出入库单详细
export function getStockBill(billId) {
  return request({
    url: '/wms/stockBill/' + billId,
    method: 'get'
  })
}

// 新增出入库单
export function addStockBill(data) {
  return request({
    url: '/wms/stockBill',
    method: 'post',
    data: data
  })
}

// 修改出入库单
export function updateStockBill(data) {
  return request({
    url: '/wms/stockBill',
    method: 'put',
    data: data
  })
}

// 删除出入库单
export function delStockBill(billId) {
  return request({
    url: '/wms/stockBill/' + billId,
    method: 'delete'
  })
}

// 确认出入库
export function confirmStockBill(billId, operatorId) {
  return request({
    url: '/wms/stockBill/confirm/' + billId,
    method: 'post',
    params: { operatorId }
  })
}
