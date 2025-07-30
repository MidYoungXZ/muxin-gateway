import axios from 'axios'
import type { ApiResponse } from '@/types/auth'
import type { 
  ServiceNode, 
  ServiceNodeCreateRequest, 
  ServiceNodeUpdateRequest,
  NodeHealthStatus,
  HealthCheckLog 
} from '@/types/route'

const API_BASE = '/api/nodes'

export const nodeApi = {
  // 获取节点列表
  list: (params?: {
    page?: number
    size?: number
    serviceName?: string
    status?: number
    keyword?: string
  }) => {
    return axios.get<ApiResponse<{
      total: number
      list: ServiceNode[]
    }>>(`${API_BASE}`, { params })
  },

  // 获取节点详情
  getDetail: (id: number) => {
    return axios.get<ApiResponse<ServiceNode>>(`${API_BASE}/${id}`)
  },

  // 创建节点
  create: (data: ServiceNodeCreateRequest) => {
    return axios.post<ApiResponse<{ id: number }>>(`${API_BASE}`, data)
  },

  // 更新节点
  update: (id: number, data: ServiceNodeUpdateRequest) => {
    return axios.put<ApiResponse<void>>(`${API_BASE}/${id}`, data)
  },

  // 删除节点
  delete: (id: number) => {
    return axios.delete<ApiResponse<void>>(`${API_BASE}/${id}`)
  },

  // 启用/禁用节点
  toggleStatus: (id: number, status: 0 | 1 | 2) => {
    return axios.patch<ApiResponse<void>>(`${API_BASE}/${id}/status`, { status })
  },

  // 批量操作节点状态
  batchUpdateStatus: (ids: number[], status: 0 | 1 | 2) => {
    return axios.patch<ApiResponse<void>>(`${API_BASE}/batch/status`, { ids, status })
  },

  // 手动触发健康检查
  triggerHealthCheck: (id: number) => {
    return axios.post<ApiResponse<{
      success: boolean
      responseTime: number
      statusCode?: number
      errorMessage?: string
    }>>(`${API_BASE}/${id}/health-check`)
  },

  // 批量健康检查
  batchHealthCheck: (ids: number[]) => {
    return axios.post<ApiResponse<Array<{
      nodeId: number
      success: boolean
      responseTime: number
      statusCode?: number
      errorMessage?: string
    }>>>(`${API_BASE}/batch/health-check`, { ids })
  },

  // 获取节点健康状态
  getHealthStatus: (nodeId?: string, serviceName?: string) => {
    return axios.get<ApiResponse<NodeHealthStatus[]>>(`${API_BASE}/health-status`, {
      params: { nodeId, serviceName }
    })
  },

  // 获取健康检查日志
  getHealthLogs: (nodeId: string, params?: {
    page?: number
    size?: number
    startTime?: string
    endTime?: string
  }) => {
    return axios.get<ApiResponse<{
      total: number
      list: HealthCheckLog[]
    }>>(`${API_BASE}/${nodeId}/health-logs`, { params })
  },

  // 获取服务名称列表
  getServiceNames: () => {
    return axios.get<ApiResponse<string[]>>(`${API_BASE}/service-names`)
  },

  // 按服务分组获取节点
  getNodesByService: () => {
    return axios.get<ApiResponse<Record<string, ServiceNode[]>>>(`${API_BASE}/by-service`)
  },

  // 导入节点配置
  import: (nodes: ServiceNodeCreateRequest[]) => {
    return axios.post<ApiResponse<{
      successCount: number
      failedCount: number
      errors?: string[]
    }>>(`${API_BASE}/import`, { nodes })
  },

  // 导出节点配置
  export: (ids?: number[], serviceName?: string) => {
    return axios.post<ApiResponse<ServiceNode[]>>(`${API_BASE}/export`, { ids, serviceName })
  },

  // 获取节点统计信息
  getStats: () => {
    return axios.get<ApiResponse<{
      totalNodes: number
      activeNodes: number
      healthyNodes: number
      unhealthyNodes: number
      maintenanceNodes: number
      serviceStats: Array<{
        serviceName: string
        totalNodes: number
        healthyNodes: number
        unhealthyNodes: number
      }>
    }>>(`${API_BASE}/stats`)
  },

  // 复制节点配置
  clone: (id: number, data: {
    nodeId: string
    nodeName: string
    address?: string
    port?: number
  }) => {
    return axios.post<ApiResponse<{ id: number }>>(`${API_BASE}/${id}/clone`, data)
  },

  // 测试节点连通性
  testConnection: (data: {
    address: string
    port: number
    path?: string
    timeout?: number
  }) => {
    return axios.post<ApiResponse<{
      success: boolean
      responseTime: number
      statusCode?: number
      errorMessage?: string
    }>>(`${API_BASE}/test-connection`, data)
  }
} 