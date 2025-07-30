import request from '@/utils/request'
import type { PageParams, PageResult } from '@/types/common'

export const predicatesApi = {
  // 获取断言列表
  getPredicates(params: PageParams & {
    routeId?: number
    type?: string
  }) {
    console.log('🚀 [PREDICATES] 获取断言列表: /api/predicates', params)
    return request({
      url: '/api/predicates',
      method: 'get',
      params
    })
  },

  // 获取断言详情
  getPredicate(id: number) {
    console.log('🚀 [PREDICATES] 获取断言详情: /api/predicates/' + id)
    return request({
      url: `/api/predicates/${id}`,
      method: 'get'
    })
  },

  // 创建断言
  createPredicate(data: {
    routeId: number
    type: string
    name: string
    config: any
    enabled?: boolean
  }) {
    console.log('🚀 [PREDICATES] 创建断言: /api/predicates', data)
    return request({
      url: '/api/predicates',
      method: 'post',
      data
    })
  },

  // 更新断言
  updatePredicate(id: number, data: any) {
    console.log('🚀 [PREDICATES] 更新断言: /api/predicates/' + id, data)
    return request({
      url: `/api/predicates/${id}`,
      method: 'put',
      data
    })
  },

  // 删除断言
  deletePredicate(id: number) {
    console.log('🚀 [PREDICATES] 删除断言: /api/predicates/' + id)
    return request({
      url: `/api/predicates/${id}`,
      method: 'delete'
    })
  },

  // 批量删除断言
  batchDelete(ids: number[]) {
    console.log('🚀 [PREDICATES] 批量删除断言: /api/predicates/batch', ids)
    return request({
      url: '/api/predicates/batch',
      method: 'delete',
      data: ids
    })
  },

  // 切换断言状态
  togglePredicate(id: number, enabled: boolean) {
    console.log(`🚀 [PREDICATES] 切换断言状态: /api/predicates/${id}`, { enabled })
    return request({
      url: `/api/predicates/${id}`,
      method: 'put',
      data: { enabled }
    })
  },

  // 获取所有可用的断言类型
  getPredicateTypes() {
    console.log('🚀 [PREDICATES] 获取断言类型: /api/predicates/types')
    return request({
      url: '/api/predicates/types',
      method: 'get'
    })
  },

  // 获取路由的断言列表（在路由管理中使用）
  getRoutePredicates(routeId: number) {
    console.log('🚀 [PREDICATES] 获取路由断言列表: /api/routes/' + routeId + '/predicates')
    return request({
      url: `/api/routes/${routeId}/predicates`,
      method: 'get'
    })
  }
} 