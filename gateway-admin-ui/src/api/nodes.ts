import request from '@/utils/request'

export interface ServiceStats {
  serviceName: string
  totalNodes: number
  healthyNodes: number
  unhealthyNodes: number
  enabledNodes: number
  disabledNodes: number
  maintenanceNodes: number
}

export interface ServiceNode {
  id: number
  nodeId: string
  serviceName: string
  nodeName: string
  address: string
  port: number
  weight: number
  maxFails: number
  failTimeout: number
  backup: boolean
  healthCheckEnabled: boolean
  healthCheckInterval: number
  healthCheckTimeout: number
  healthCheckPath: string
  healthCheckExpectedStatus: number[]
  status: number
  statusDesc: string
  healthy: boolean
  lastCheckTime?: string
  lastCheckResult?: number
}

export interface ServiceNodeDTO {
  nodeName?: string
  address?: string
  port?: number
  weight?: number
  maxFails?: number
  failTimeout?: number
  backup?: boolean
  healthCheckEnabled?: boolean
  healthCheckInterval?: number
  healthCheckTimeout?: number
  healthCheckPath?: string
  healthCheckExpectedStatus?: number[]
}

export interface DiscoveryConfig {
  registryType: string
  serverAddr: string
  namespace?: string
  username?: string
  password?: string
  group?: string
}

export interface ServiceCreateRequest {
  serviceName: string
  createMode: string
  nodes?: ServiceNodeDTO[]
  discoveryConfig?: DiscoveryConfig
}

export interface ServiceNodeCreateRequest {
  nodeId: string
  serviceName: string
  nodeName: string
  address: string
  port: number
  weight?: number
  maxFails?: number
  failTimeout?: number
  backup?: boolean
  healthCheckEnabled?: boolean
  healthCheckInterval?: number
  healthCheckTimeout?: number
  healthCheckPath?: string
  healthCheckExpectedStatus?: number[]
}

export interface ServiceNodeUpdateRequest {
  nodeName: string
  address: string
  port?: number
  weight?: number
  maxFails?: number
  failTimeout?: number
  backup?: boolean
  healthCheckEnabled?: boolean
  healthCheckInterval?: number
  healthCheckTimeout?: number
  healthCheckPath?: string
  healthCheckExpectedStatus?: number[]
}

export interface PageResult<T> {
  data: T[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

export interface RouteSimple {
  id: number
  routeId: string
  routeName: string
  enabled: boolean
}

export interface DiscoveryTestResult {
  success: boolean
  message: string
  serviceNames?: string[]
}

export interface DiscoveredNode {
  instanceId: string
  address: string
  port: number
  weight: number
  healthy: boolean
  enabled: boolean
  metadata?: Record<string, string>
}

export interface DiscoveryRequest {
  registryType: string
  serverAddr: string
  serviceName: string
  namespace?: string
  username?: string
  password?: string
  group?: string
}

export const nodesApi = {
  getServiceStats(serviceName?: string, pageNum: number = 1, pageSize: number = 20) {
    return request<{ data: PageResult<ServiceStats> }>({
      url: '/api/nodes/services',
      method: 'get',
      params: { serviceName, pageNum, pageSize }
    })
  },

  getNodesByService(serviceName: string, pageNum: number = 1, pageSize: number = 20) {
    return request<{ data: PageResult<ServiceNode> }>({
      url: `/api/nodes/services/${serviceName}/nodes`,
      method: 'get',
      params: { pageNum, pageSize }
    })
  },

  getDetail(id: number) {
    return request<{ data: ServiceNode }>({
      url: `/api/nodes/${id}`,
      method: 'get'
    })
  },

  create(data: ServiceNodeCreateRequest) {
    return request<{ data: number }>({
      url: '/api/nodes',
      method: 'post',
      data
    })
  },

  update(id: number, data: ServiceNodeUpdateRequest) {
    return request<{ data: void }>({
      url: `/api/nodes/${id}`,
      method: 'put',
      data
    })
  },

  delete(id: number) {
    return request<{ data: void }>({
      url: `/api/nodes/${id}`,
      method: 'delete'
    })
  },

  enable(id: number) {
    return request<{ data: void }>({
      url: `/api/nodes/${id}/enable`,
      method: 'post'
    })
  },

  disable(id: number) {
    return request<{ data: void }>({
      url: `/api/nodes/${id}/disable`,
      method: 'post'
    })
  },

  maintenance(id: number) {
    return request<{ data: void }>({
      url: `/api/nodes/${id}/maintenance`,
      method: 'post'
    })
  },

  getServiceNames() {
    return request<{ data: string[] }>({
      url: '/api/nodes/service-names',
      method: 'get'
    })
  },

  createService(data: ServiceCreateRequest) {
    return request<{ data: number }>({
      url: '/api/nodes/services',
      method: 'post',
      data
    })
  },

  getServiceRoutes(serviceName: string) {
    return request<{ data: RouteSimple[] }>({
      url: `/api/nodes/services/${serviceName}/routes`,
      method: 'get'
    })
  },

  deleteService(serviceName: string) {
    return request<{ data: void }>({
      url: `/api/nodes/services/${serviceName}`,
      method: 'delete'
    })
  },

  testDiscoveryConnection(config: DiscoveryConfig) {
    return request<{ data: DiscoveryTestResult }>({
      url: '/api/nodes/discovery/test',
      method: 'post',
      data: config
    })
  },

  discoverNodes(data: DiscoveryRequest) {
    return request<{ data: DiscoveredNode[] }>({
      url: '/api/nodes/discovery/discover',
      method: 'post',
      data
    })
  }
}