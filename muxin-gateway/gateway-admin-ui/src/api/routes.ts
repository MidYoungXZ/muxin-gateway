import { apiGet, apiPost, apiPut, apiDelete, debugApiUrl } from '@/utils/api'
import type { RouteConfig, LoadBalanceConfig, RouteTemplate } from '@/types/route'
import type { PageParams, PageResult } from '@/types/common'

export const routesApi = {
  // 获取路由列表
  getRoutes(params: PageParams & {
    keyword?: string
    enabled?: boolean
  }) {
    debugApiUrl('routes') // 调试用，生产环境可删除
    return apiGet<PageResult<RouteConfig>>('routes', params)
  },

  // 获取路由详情
  getRoute(id: number) {
    debugApiUrl(`routes/${id}`) // 调试用，生产环境可删除
    return apiGet<RouteConfig>(`routes/${id}`)
  },

  // 创建路由
  createRoute(data: Partial<RouteConfig>) {
    debugApiUrl('routes') // 调试用，生产环境可删除
    return apiPost<RouteConfig>('routes', data)
  },

  // 更新路由
  updateRoute(id: number, data: Partial<RouteConfig>) {
    debugApiUrl(`routes/${id}`) // 调试用，生产环境可删除
    return apiPut<RouteConfig>(`routes/${id}`, data)
  },

  // 删除路由
  deleteRoute(id: number) {
    debugApiUrl(`routes/${id}`) // 调试用，生产环境可删除
    return apiDelete(`routes/${id}`)
  },

  // 启用/禁用路由
  toggleRoute(id: number, enabled: boolean) {
    const action = enabled ? 'enable' : 'disable'
    debugApiUrl(`routes/${id}/${action}`) // 调试用，生产环境可删除
    return apiPost(`routes/${id}/${action}`)
  },

  // 测试路由
  testRoute(data: {
    method: string
    path: string
    headers: Record<string, string>
    body?: any
  }) {
    debugApiUrl('routes/test') // 调试用，生产环境可删除
    return apiPost('routes/test', data)
  },

  // 导出路由配置 - 后端未实现
  exportRoutes(ids?: number[]) {
    console.log('⚠️ [ROUTES] 导出功能后端未实现，返回模拟数据')
    // 模拟数据
    return Promise.resolve({
      code: 200,
      message: '导出成功（模拟）',
      data: new Blob(['模拟路由配置数据'], { type: 'application/json' })
    })
  },

  // 导入路由配置 - 后端未实现
  importRoutes(file: File) {
    console.log('⚠️ [ROUTES] 导入功能后端未实现，返回模拟数据')
    // 模拟数据
    return Promise.resolve({
      code: 200,
      message: '导入成功（模拟）',
      data: { importedCount: 5 }
    })
  },

  // 获取节点列表 - 后端未实现
  getNodes(routeId?: string) {
    console.log('⚠️ [NODES] 节点管理后端未实现，返回模拟数据')
    // 模拟数据
    return Promise.resolve({
      code: 200,
      message: '获取成功（模拟）',
      data: [
        { id: '1', address: 'localhost:8081', status: 'UP', weight: 1 },
        { id: '2', address: 'localhost:8082', status: 'DOWN', weight: 1 }
      ]
    })
  },

  // 更新节点状态 - 后端未实现
  updateNodeStatus(nodeId: string, enabled: boolean) {
    console.log('⚠️ [NODES] 节点状态更新后端未实现，返回模拟数据')
    // 模拟数据
    return Promise.resolve({
      code: 200,
      message: '更新成功（模拟）',
      data: null
    })
  },

  // 获取负载均衡配置 - 后端未实现
  getLoadBalanceConfig(routeId: string) {
    console.log('⚠️ [LOADBALANCE] 负载均衡配置后端未实现，返回模拟数据')
    // 模拟数据
    return Promise.resolve({
      code: 200,
      message: '获取成功（模拟）',
      data: {
        strategy: 'ROUND_ROBIN',
        nodes: [
          { address: 'localhost:8081', weight: 1, enabled: true }
        ]
      }
    })
  },

  // 更新负载均衡配置 - 后端未实现
  updateLoadBalanceConfig(routeId: string, data: LoadBalanceConfig) {
    console.log('⚠️ [LOADBALANCE] 负载均衡配置更新后端未实现，返回模拟数据')
    // 模拟数据
    return Promise.resolve({
      code: 200,
      message: '更新成功（模拟）',
      data: null
    })
  },

  // 获取配置模板列表 - 后端未实现
  getTemplates() {
    console.log('⚠️ [TEMPLATES] 配置模板后端未实现，返回模拟数据')
    // 模拟数据
    return Promise.resolve({
      code: 200,
      message: '获取成功（模拟）',
      data: [
        { id: 1, name: '基础HTTP模板', description: '标准HTTP路由模板' },
        { id: 2, name: '微服务模板', description: '微服务路由模板' }
      ]
    })
  },

  // 创建配置模板 - 后端未实现
  createTemplate(data: Partial<RouteTemplate>) {
    console.log('⚠️ [TEMPLATES] 模板创建后端未实现，返回模拟数据')
    // 模拟数据
    return Promise.resolve({
      code: 200,
      message: '创建成功（模拟）',
      data: { id: Date.now() }
    })
  },

  // 应用配置模板 - 后端未实现
  applyTemplate(routeId: number, templateId: number, variables: Record<string, any>) {
    console.log('⚠️ [TEMPLATES] 模板应用后端未实现，返回模拟数据')
    // 模拟数据
    return Promise.resolve({
      code: 200,
      message: '应用成功（模拟）',
      data: null
    })
  },

  // 路由测试增强版
  testRouteAdvanced(data: {
    method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
    path: string
    headers?: Record<string, string>
    queryParams?: Record<string, string>
    body?: string
    routeId?: string
  }) {
    debugApiUrl('routes/test-advanced')
    return apiPost('routes/test-advanced', data)
  },

  // 获取路由测试历史
  getTestHistory(routeId?: string, limit?: number) {
    debugApiUrl('routes/test-history')
    return apiGet('routes/test-history', { routeId, limit })
  },

  // 复制路由
  cloneRoute(routeId: number, data: {
    routeId: string
    routeName: string
    description?: string
  }) {
    debugApiUrl(`routes/${routeId}/clone`)
    return apiPost(`routes/${routeId}/clone`, data)
  },

  // 获取路由配置版本历史
  getVersionHistory(routeId: number) {
    debugApiUrl(`routes/${routeId}/versions`)
    return apiGet(`routes/${routeId}/versions`)
  },

  // 回滚到指定版本
  rollbackToVersion(routeId: number, version: number) {
    debugApiUrl(`routes/${routeId}/rollback/${version}`)
    return apiPost(`routes/${routeId}/rollback/${version}`)
  },

  // 比较版本差异
  compareVersions(routeId: number, fromVersion: number, toVersion: number) {
    debugApiUrl(`routes/${routeId}/compare/${fromVersion}/${toVersion}`)
    return apiGet(`routes/${routeId}/compare/${fromVersion}/${toVersion}`)
  },

  // 保存为模板
  saveAsTemplate(routeId: number, data: {
    templateName: string
    description?: string
    category?: string
    variables?: Array<{
      name: string
      type: 'string' | 'number' | 'boolean'
      required: boolean
      description: string
    }>
  }) {
    debugApiUrl(`routes/${routeId}/save-template`)
    return apiPost(`routes/${routeId}/save-template`, data)
  },

  // 批量操作路由状态
  batchToggleRoutes(ids: number[], enabled: boolean) {
    const action = enabled ? 'enable' : 'disable'
    debugApiUrl(`routes/batch/${action}`)
    return apiPost(`routes/batch/${action}`, { ids })
  },

  // 批量删除路由
  batchDeleteRoutes(ids: number[]) {
    debugApiUrl('routes/batch')
    return apiDelete('routes/batch', { ids })
  }
} 