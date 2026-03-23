import request from '@/utils/request'

// 查询应收应付列表
export function listFinance(query) {
  return request({
    url: '/oms/finance/list',
    method: 'get',
    params: query
  })
}

// 查询应收应付详细
export function getFinance(financeId) {
  return request({
    url: '/oms/finance/' + financeId,
    method: 'get'
  })
}

// 添加收付款记录
export function addPayment(data) {
  return request({
    url: '/oms/finance/payment',
    method: 'post',
    data: data
  })
}

// 更新发票信息
export function updateInvoice(data) {
  return request({
    url: '/oms/finance/invoice',
    method: 'put',
    data: data
  })
}
