import request from '@/utils/request'

// 查询订单统计列表
export function listOrderStatistics(query) {
  return request({
    url: '/oms/statistics/list',
    method: 'get',
    params: query
  })
}
