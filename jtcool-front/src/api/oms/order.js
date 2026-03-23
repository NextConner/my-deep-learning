import request from '@/utils/request'

// 查询订单列表
export function listOrder(query) {
  return request({
    url: '/oms/order/list',
    method: 'get',
    params: query
  })
}

// 查询订单详细
export function getOrder(orderId) {
  return request({
    url: '/oms/order/' + orderId,
    method: 'get'
  })
}

// 新增订单
export function addOrder(data) {
  return request({
    url: '/oms/order',
    method: 'post',
    data: data
  })
}

// 修改订单
export function updateOrder(data) {
  return request({
    url: '/oms/order',
    method: 'put',
    data: data
  })
}

// 删除订单
export function delOrder(orderId) {
  return request({
    url: '/oms/order/' + orderId,
    method: 'delete'
  })
}

// 销售确认
export function confirmOrder(orderId) {
  return request({
    url: '/oms/order/confirm/' + orderId,
    method: 'post'
  })
}

// 订单审核
export function auditOrder(orderId) {
  return request({
    url: '/oms/order/audit/' + orderId,
    method: 'post'
  })
}

// 拒绝订单
export function rejectOrder(orderId, data) {
  return request({
    url: '/oms/order/reject/' + orderId,
    method: 'post',
    data: data
  })
}

// 确认发货
export function shipOrder(orderId, data) {
  return request({
    url: '/oms/order/ship/' + orderId,
    method: 'post',
    data: data
  })
}

// 客户签收
export function receiveOrder(orderId) {
  return request({
    url: '/oms/order/receive/' + orderId,
    method: 'post'
  })
}
