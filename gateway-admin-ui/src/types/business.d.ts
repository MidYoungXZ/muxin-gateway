// 业务相关类型定义
export interface LoginForm {
  username: string
  password: string
  captcha?: string
  captchaId?: string
  rememberMe?: boolean
}

export interface LoginResponse {
  token: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  userInfo: UserInfo
}

// 路由配置相关类型
export interface RoutePredicate {
  name: string
  args: Record<string, any>
}

export interface RouteFilter {
  name: string
  args: Record<string, any>
  order?: number
}

export interface RouteConfig {
  id?: number
  routeId: string
  routeName: string
  description?: string
  uri: string
  predicates: RoutePredicate[]
  filters: RouteFilter[]
  metadata?: Record<string, any>
  order: number
  enabled: boolean
  grayscaleEnabled?: boolean
  grayscaleConfig?: GrayscaleConfig
  templateId?: number
  version?: number
  createTime?: string
  updateTime?: string
  createBy?: string
  updateBy?: string
}

// 服务节点类型
export interface ServiceNode {
  id?: number
  serviceId: string
  nodeId: string
  address: string
  port: number
  weight: number
  status: 'UP' | 'DOWN' | 'UNKNOWN'
  healthCheckUrl?: string
  lastHeartbeat?: string
  metadata?: Record<string, any>
  createTime?: string
  updateTime?: string
}

// 负载均衡配置
export interface LoadBalanceConfig {
  routeId: string
  strategy: 'ROUND_ROBIN' | 'WEIGHTED' | 'LEAST_CONN' | 'IP_HASH' | 'RANDOM'
  nodes: ServiceNode[]
  healthCheck: {
    enabled: boolean
    interval: number
    timeout: number
    path: string
    expectedStatus: number[]
  }
}

// 灰度发布配置
export interface GrayscaleConfig {
  enabled: boolean
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
  startTime?: string
  endTime?: string
  autoPromote: boolean
  rollbackOnError: boolean
}

// 监控数据类型
export interface RealtimeStats {
  timestamp: number
  metrics: {
    qps: number
    totalRequests: number
    avgResponseTime: number
    errorRate: number
    activeConnections: number
  }
  statusCodeDistribution: {
    '2xx': number
    '3xx': number
    '4xx': number
    '5xx': number
  }
}

export interface RouteMetrics {
  routeId: string
  routeName: string
  callCount: number
  avgResponseTime: number
  p95ResponseTime: number
  p99ResponseTime: number
  errorCount: number
  errorRate: number
  lastCallTime: string
}

export interface FilterMetrics {
  filterId: string
  filterName: string
  filterType: 'PRE' | 'POST' | 'ERROR'
  hitCount: number
  blockCount: number
  avgProcessTime: number
  lastHitTime: string
}

// 鉴权规则类型
export interface AuthRule {
  id?: number
  name: string
  description?: string
  type: 'JWT' | 'OAuth2' | 'Basic' | 'ApiKey' | 'Custom'
  routePatterns: string[]
  config: {
    jwtSecret?: string
    jwtHeader?: string
    jwtClaims?: string[]
    authorizationUri?: string
    tokenUri?: string
    clientId?: string
    apiKeyHeader?: string
    apiKeyParam?: string
    customScript?: string
  }
  whitelist: {
    ips: string[]
    tokens: string[]
    headers: Record<string, string>
  }
  blacklist: {
    ips: string[]
    tokens: string[]
  }
  priority: number
  enabled: boolean
  createTime?: string
  updateTime?: string
}

// 路由模板类型
export interface RouteTemplate {
  id?: number
  templateName: string
  description?: string
  category?: string
  config: Partial<RouteConfig>
  variables: {
    name: string
    type: 'string' | 'number' | 'boolean'
    defaultValue: any
    required: boolean
    description: string
  }[]
  isSystem: boolean
  usageCount: number
  createTime?: string
  updateTime?: string
  createBy?: string
}

// 系统管理相关类型
export interface Role {
  id?: number
  roleCode: string
  roleName: string
  description?: string
  sort: number
  status: number
  permissions?: Permission[]
  createTime?: string
  updateTime?: string
}

export interface Permission {
  id?: number
  parentId: number
  permType: 'menu' | 'button' | 'api'
  permCode: string
  permName: string
  path?: string
  component?: string
  icon?: string
  sort: number
  visible: boolean
  status: number
  children?: Permission[]
  createTime?: string
  updateTime?: string
}

export interface Department {
  id?: number
  parentId: number
  deptName: string
  deptCode?: string
  description?: string
  sort: number
  status: number
  leader?: string
  phone?: string
  email?: string
  children?: Department[]
  createTime?: string
  updateTime?: string
}

// 操作日志类型
export interface OperationLog {
  id?: number
  module: string
  operation: string
  method: string
  params?: string
  result?: string
  error?: string
  duration: number
  operator: string
  operatorIp: string
  operateTime: string
}

// 登录日志类型
export interface LoginLog {
  id?: number
  username: string
  loginType: string
  loginIp: string
  loginLocation?: string
  browser?: string
  os?: string
  status: number
  message?: string
  loginTime: string
}

// 系统配置类型
export interface SystemConfig {
  id?: number
  configKey: string
  configValue: string
  configName: string
  configType: 'text' | 'number' | 'boolean' | 'json'
  description?: string
  sort: number
  createTime?: string
  updateTime?: string
}

// 文件信息类型
export interface FileInfo {
  id?: number
  fileName: string
  originalName: string
  filePath: string
  fileSize: number
  fileType: string
  contentType: string
  md5: string
  uploadTime: string
  uploader: string
}

// 通知消息类型
export interface NotificationMessage {
  id?: number
  type: 'info' | 'success' | 'warning' | 'error'
  title: string
  content: string
  target: 'all' | 'user' | 'role'
  targetIds?: string[]
  isRead: boolean
  createTime: string
  readTime?: string
} 