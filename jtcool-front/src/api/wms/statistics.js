import request from '@/utils/request'

// 查询出入库统计列表
export function listStockStatistics(query) {
  return request({
    url: '/wms/statistics/list',
    method: 'get',
    params: query
  })
}
