export interface RouteConfig {
  id: number
  routeId: string
  name: string
  description: string
  uri: string
  predicates: RoutePredicate[]
  filters: RouteFilter[]
  metadata: Record<string, any>
  order: number
  enabled: boolean
  grayscaleEnabled: boolean
  grayscaleConfig?: GrayscaleConfig
  templateId?: number
  createdTime: Date
  updatedTime: Date
}

export interface RoutePredicate {
  type: string
  args: Record<string, any>
}

export interface RouteFilter {
  id: string
  name: string
  type: string
  order: number
  enabled: boolean
  config: Record<string, any>
}

export interface GrayscaleConfig {
  type: 'percentage' | 'whitelist' | 'header' | 'weight'
  config: {
    percentage?: number
    whitelist?: {
      userIds?: string[]
      ips?: string[]
      regions?: string[]
    }
    headerRules?: {
      header: string
      pattern: string
      matchType: 'exact' | 'regex' | 'contains'
    }[]
    weights?: {
      version: string
      weight: number
    }[]
  }
  startTime?: Date
  endTime?: Date
  autoPromote: boolean
  rollbackOnError: boolean
}

export interface LoadBalanceConfig {
  routeId: string
  strategy: 'ROUND_ROBIN' | 'WEIGHTED' | 'LEAST_CONN' | 'IP_HASH' | 'RANDOM'
  nodes: {
    nodeId: string
    address: string
    weight: number
    maxFails: number
    failTimeout: number
    backup: boolean
  }[]
  healthCheck: {
    enabled: boolean
    interval: number
    timeout: number
    path: string
    expectedStatus: number[]
  }
}

export interface RouteTemplate {
  id: number
  name: string
  description: string
  category: string
  config: {
    predicates: RoutePredicate[]
    filters: RouteFilter[]
    metadata: Record<string, any>
  }
  variables: {
    name: string
    type: 'string' | 'number' | 'boolean'
    defaultValue: any
    required: boolean
    description: string
  }[]
  isSystem: boolean
  usageCount: number
}

// 新增：路由模板相关类型
export interface RouteTemplateCreateRequest {
  templateName: string
  description?: string
  category?: string
  config: TemplateConfig
  variables?: TemplateVariable[]
}

export interface RouteTemplateUpdateRequest {
  templateName?: string
  description?: string
  category?: string
  config?: TemplateConfig
  variables?: TemplateVariable[]
  enabled?: boolean
}

export interface TemplateConfig {
  predicates: Array<{
    type: string
    args: Record<string, any>
  }>
  filters: Array<{
    type: string
    args: Record<string, any>
  }>
  metadata?: Record<string, any>
}

export interface TemplateVariable {
  name: string
  type: 'string' | 'number' | 'boolean'
  defaultValue?: any
  required: boolean
  description: string
}

// 新增：服务节点相关类型
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
  status: 0 | 1 | 2 // 0-禁用，1-启用，2-维护中
  lastCheckTime?: Date
  lastCheckResult?: 0 | 1 // 0-失败，1-成功
  metadata?: Record<string, any>
  createTime: Date
  updateTime: Date
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
  metadata?: Record<string, any>
}

export interface ServiceNodeUpdateRequest {
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
  status?: 0 | 1 | 2
  metadata?: Record<string, any>
}

// 新增：负载均衡相关类型
export interface LoadBalanceRule {
  id: number
  routeId: number
  strategy: 'ROUND_ROBIN' | 'WEIGHTED' | 'LEAST_CONN' | 'IP_HASH' | 'RANDOM'
  config: LoadBalanceRuleConfig
  enabled: boolean
  createTime: Date
  updateTime: Date
}

export interface LoadBalanceRuleConfig {
  healthCheck?: {
    enabled: boolean
    interval: number
    timeout: number
    path: string
    expectedStatus: number[]
  }
  strategyConfig?: Record<string, any>
}

export interface LoadBalanceCreateRequest {
  routeId: number
  strategy: string
  config?: LoadBalanceRuleConfig
}

export interface LoadBalanceUpdateRequest {
  strategy?: string
  config?: LoadBalanceRuleConfig
  enabled?: boolean
}

// 新增：路由测试相关类型
export interface RouteTestRequest {
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
  path: string
  headers?: Record<string, string>
  queryParams?: Record<string, string>
  body?: string
  routeId?: string
}

export interface RouteTestResponse {
  matched: boolean
  matchedRoute?: {
    routeId: string
    routeName: string
    uri: string
  }
  predicateResults: Array<{
    type: string
    matched: boolean
    message?: string
  }>
  filterChain: Array<{
    name: string
    type: string
    executed: boolean
    result?: string
  }>
  finalRequest?: {
    method: string
    url: string
    headers: Record<string, string>
    body?: string
  }
  response?: {
    status: number
    headers: Record<string, string>
    body?: string
    responseTime: number
  }
  error?: string
}

// 新增：节点健康检查相关类型
export interface NodeHealthStatus {
  nodeId: string
  serviceName: string
  nodeName: string
  status: 'healthy' | 'unhealthy' | 'unknown'
  lastCheckTime?: Date
  lastCheckResult?: boolean
  responseTime?: number
  errorMessage?: string
}

export interface HealthCheckLog {
  id: number
  nodeId: string
  checkTime: Date
  result: boolean
  responseTime: number
  statusCode?: number
  errorMessage?: string
} 