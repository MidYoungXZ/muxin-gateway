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

  // 切换断言状态 - 后端未实现
  togglePredicate(id: number, enabled: boolean) {
    const action = enabled ? 'enable' : 'disable'
    console.log(`⚠️ [PREDICATES] 断言状态切换后端未实现，返回模拟数据`)
    // 模拟数据
    return Promise.resolve({
      code: 200,
      message: `${action} 成功（模拟）`,
      data: null
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

  // 获取路由的断言列表 - 后端未实现
  getRoutePredicates(routeId: number) {
    console.log('⚠️ [PREDICATES] 路由断言列表后端未实现，返回模拟数据')
    // 模拟数据
    return Promise.resolve({
      code: 200,
      message: '获取成功（模拟）',
      data: [
        { id: 1, type: 'Path', config: { pattern: '/api/**' }, enabled: true },
        { id: 2, type: 'Method', config: { methods: ['GET', 'POST'] }, enabled: true }
      ]
    })
  },

  // 测试断言 - 后端未实现
  testPredicate(data: {
    type: string
    config: any
    testRequest: {
      method: string
      path: string
      headers: Record<string, string>
      query: Record<string, string>
    }
  }) {
    console.log('⚠️ [PREDICATES] 断言测试后端未实现，返回模拟数据')
    // 模拟数据
    return Promise.resolve({
      code: 200,
      message: '测试成功（模拟）',
      data: {
        matched: true,
        reason: '模拟测试结果'
      }
    })
  }
} 