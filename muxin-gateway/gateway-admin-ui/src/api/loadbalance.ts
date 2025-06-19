import axios from 'axios'
import type { ApiResponse } from '@/types/auth'
import type { 
  LoadBalanceRule, 
  LoadBalanceCreateRequest, 
  LoadBalanceUpdateRequest,
  LoadBalanceRuleConfig 
} from '@/types/route'

const API_BASE = '/api/loadbalance'

export const loadBalanceApi = {
  // 获取负载均衡规则列表
  list: (params?: {
    page?: number
    size?: number
    routeId?: number
    strategy?: string
    enabled?: boolean
  }) => {
    return axios.get<ApiResponse<{
      total: number
      list: LoadBalanceRule[]
    }>>(`${API_BASE}`, { params })
  },

  // 获取负载均衡规则详情
  getDetail: (id: number) => {
    return axios.get<ApiResponse<LoadBalanceRule>>(`${API_BASE}/${id}`)
  },

  // 根据路由ID获取负载均衡配置
  getByRouteId: (routeId: number) => {
    return axios.get<ApiResponse<LoadBalanceRule>>(`${API_BASE}/route/${routeId}`)
  },

  // 创建负载均衡规则
  create: (data: LoadBalanceCreateRequest) => {
    return axios.post<ApiResponse<{ id: number }>>(`${API_BASE}`, data)
  },

  // 更新负载均衡规则
  update: (id: number, data: LoadBalanceUpdateRequest) => {
    return axios.put<ApiResponse<void>>(`${API_BASE}/${id}`, data)
  },

  // 删除负载均衡规则
  delete: (id: number) => {
    return axios.delete<ApiResponse<void>>(`${API_BASE}/${id}`)
  },

  // 启用/禁用负载均衡规则
  toggleEnabled: (id: number, enabled: boolean) => {
    return axios.patch<ApiResponse<void>>(`${API_BASE}/${id}/enabled`, { enabled })
  },

  // 获取支持的负载均衡策略
  getStrategies: () => {
    return axios.get<ApiResponse<Array<{
      code: string
      name: string
      description: string
      configSchema?: Record<string, any>
    }>>>(`${API_BASE}/strategies`)
  },

  // 测试负载均衡策略
  testStrategy: (data: {
    strategy: string
    nodes: Array<{
      nodeId: string
      weight: number
      enabled: boolean
    }>
    requestCount?: number
  }) => {
    return axios.post<ApiResponse<{
      distribution: Record<string, number>
      totalRequests: number
      strategy: string
    }>>(`${API_BASE}/test-strategy`, data)
  },

  // 获取负载均衡统计
  getStats: (routeId?: number, timeRange?: string) => {
    return axios.get<ApiResponse<{
      totalRequests: number
      nodeDistribution: Record<string, {
        requests: number
        percentage: number
        avgResponseTime: number
        errorRate: number
      }>
      strategyStats: {
        strategy: string
        effectiveness: number // 负载均衡效果评分
        variance: number // 分布方差
      }
    }>>(`${API_BASE}/stats`, { params: { routeId, timeRange } })
  },

  // 获取实时负载分布
  getRealTimeDistribution: (routeId: number) => {
    return axios.get<ApiResponse<{
      timestamp: number
      nodes: Array<{
        nodeId: string
        nodeName: string
        currentRequests: number
        totalRequests: number
        avgResponseTime: number
        status: 'healthy' | 'unhealthy' | 'disabled'
      }>
    }>>(`${API_BASE}/realtime/${routeId}`)
  },

  // 重新平衡负载
  rebalance: (routeId: number, strategy?: string) => {
    return axios.post<ApiResponse<{
      success: boolean
      message: string
      affectedNodes: number
    }>>(`${API_BASE}/rebalance/${routeId}`, { strategy })
  },

  // 获取配置模板
  getConfigTemplates: (strategy: string) => {
    return axios.get<ApiResponse<Array<{
      name: string
      description: string
      config: LoadBalanceRuleConfig
      scenario: string // 适用场景
    }>>>(`${API_BASE}/templates/${strategy}`)
  },

  // 应用配置模板
  applyTemplate: (routeId: number, templateName: string, strategy: string) => {
    return axios.post<ApiResponse<void>>(`${API_BASE}/apply-template`, {
      routeId,
      templateName,
      strategy
    })
  },

  // 预览配置效果
  previewConfig: (data: {
    strategy: string
    config: LoadBalanceRuleConfig
    nodeIds: string[]
  }) => {
    return axios.post<ApiResponse<{
      estimatedDistribution: Record<string, number>
      recommendations: string[]
      warnings: string[]
    }>>(`${API_BASE}/preview`, data)
  },

  // 导出负载均衡配置
  export: (routeIds: number[]) => {
    return axios.post<ApiResponse<LoadBalanceRule[]>>(`${API_BASE}/export`, { routeIds })
  },

  // 导入负载均衡配置
  import: (configs: LoadBalanceCreateRequest[]) => {
    return axios.post<ApiResponse<{
      successCount: number
      failedCount: number
      errors?: string[]
    }>>(`${API_BASE}/import`, { configs })
  }
} 