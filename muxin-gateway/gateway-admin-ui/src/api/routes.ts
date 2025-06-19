import request from '@/utils/request'
import type { 
  RouteConfig, 
  RouteCreateRequest, 
  RouteUpdateRequest, 
  RouteQueryParams,
  PageResult,
  ApiResponse 
} from '@/types/route'

export const routeApi = {
  // 分页查询路由
  getRoutes(params: RouteQueryParams) {
    console.log('🚀 [ROUTE] 获取路由列表:', params)
    return request({
      url: '/api/routes',
      method: 'get',
      params
    })
  },

  // 获取路由详情
  getRouteDetail(id: string) {
    console.log('🚀 [ROUTE] 获取路由详情:', id)
    return request({
      url: `/api/routes/${id}`,
      method: 'get'
    })
  },

  // 创建路由
  createRoute(data: RouteCreateRequest) {
    console.log('🚀 [ROUTE] 创建路由:', data)
    return request({
      url: '/api/routes',
      method: 'post',
      data
    })
  },

  // 更新路由
  updateRoute(id: string, data: RouteUpdateRequest) {
    console.log('🚀 [ROUTE] 更新路由:', id, data)
    return request({
      url: `/api/routes/${id}`,
      method: 'put',
      data
    })
  },

  // 删除路由
  deleteRoute(id: string) {
    console.log('🚀 [ROUTE] 删除路由:', id)
    return request({
      url: `/api/routes/${id}`,
      method: 'delete'
    })
  },

  // 启用路由
  enableRoute(id: string) {
    console.log('🚀 [ROUTE] 启用路由:', id)
    return request({
      url: `/api/routes/${id}/enable`,
      method: 'put'
    })
  },

  // 禁用路由
  disableRoute(id: string) {
    console.log('🚀 [ROUTE] 禁用路由:', id)
    return request({
      url: `/api/routes/${id}/disable`,
      method: 'put'
    })
  },

  // 批量删除路由
  batchDeleteRoutes(ids: string[]) {
    console.log('🚀 [ROUTE] 批量删除路由:', ids)
    return request({
      url: '/api/routes/batch',
      method: 'delete',
      data: ids
    })
  },

  // 批量启用路由
  batchEnableRoutes(ids: string[]) {
    console.log('🚀 [ROUTE] 批量启用路由:', ids)
    return request({
      url: '/api/routes/batch/enable',
      method: 'put',
      data: ids
    })
  },

  // 批量禁用路由
  batchDisableRoutes(ids: string[]) {
    console.log('🚀 [ROUTE] 批量禁用路由:', ids)
    return request({
      url: '/api/routes/batch/disable',
      method: 'put',
      data: ids
    })
  },

  // 测试路由
  testRoute(routeId: string, testData: any) {
    console.log('🚀 [ROUTE] 测试路由:', routeId, testData)
    return request({
      url: `/api/routes/${routeId}/test`,
      method: 'post',
      data: testData
    })
  },

  // 获取路由统计
  getRouteStats() {
    console.log('🚀 [ROUTE] 获取路由统计')
    return request({
      url: '/api/routes/stats',
      method: 'get'
    })
  },

  // 导出路由配置
  exportRoutes(params: RouteQueryParams) {
    console.log('🚀 [ROUTE] 导出路由配置:', params)
    return request({
      url: '/api/routes/export',
      method: 'post',
      data: params,
      responseType: 'blob'
    })
  },

  // 导入路由配置
  importRoutes(file: File) {
    console.log('🚀 [ROUTE] 导入路由配置:', file.name)
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/api/routes/import',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 复制路由
  copyRoute(id: string, newRouteId: string) {
    console.log('🚀 [ROUTE] 复制路由:', id, newRouteId)
    return request({
      url: `/api/routes/${id}/copy`,
      method: 'post',
      data: { newRouteId }
    })
  },

  // 获取路由测试历史
  getTestHistory(routeId: string, limit: number = 10) {
    console.log('🚀 [ROUTE] 获取测试历史:', routeId, limit)
    return request({
      url: '/api/routes/test-history',
      method: 'get',
      params: { routeId, limit }
    })
  },

  // 清除路由缓存
  clearRouteCache(routeId?: string) {
    console.log('🚀 [ROUTE] 清除路由缓存:', routeId)
    return request({
      url: '/api/routes/cache/clear',
      method: 'post',
      data: routeId ? { routeId } : {}
    })
  },

  // 获取路由版本历史
  getRouteVersions(routeId: string) {
    console.log('🚀 [ROUTE] 获取路由版本历史:', routeId)
    return request({
      url: `/api/routes/${routeId}/versions`,
      method: 'get'
    })
  },

  // 路由版本对比
  compareRouteVersions(routeId: string, fromVersion: number, toVersion: number) {
    console.log('🚀 [ROUTE] 路由版本对比:', routeId, fromVersion, toVersion)
    return request({
      url: `/api/routes/${routeId}/compare/${fromVersion}/${toVersion}`,
      method: 'get'
    })
  },

  // 回滚路由版本
  rollbackRouteVersion(routeId: string, version: number) {
    console.log('🚀 [ROUTE] 回滚路由版本:', routeId, version)
    return request({
      url: `/api/routes/${routeId}/rollback/${version}`,
      method: 'post'
    })
  }
} 