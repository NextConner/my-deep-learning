import request from '@/utils/request'

// 查询产品列表
export function listProduct(query) {
  return request({
    url: '/product/product/list',
    method: 'get',
    params: query
  })
}

// 查询产品详细
export function getProduct(productId) {
  return request({
    url: '/product/product/' + productId,
    method: 'get'
  })
}

// 新增产品
export function addProduct(data) {
  return request({
    url: '/product/product',
    method: 'post',
    data: data
  })
}

// 修改产品
export function updateProduct(data) {
  return request({
    url: '/product/product',
    method: 'put',
    data: data
  })
}

// 删除产品
export function delProduct(productId) {
  return request({
    url: '/product/product/' + productId,
    method: 'delete'
  })
}
