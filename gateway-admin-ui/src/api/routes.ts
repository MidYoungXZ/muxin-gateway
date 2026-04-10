import request from '@/utils/request'

export interface Route {
  id: number
  routeId: string
  routeName: string
  description?: string
  uri: string
  metadata?: Record<string, any>
  order: number
  loadBalanceStrategy?: string
  enabled: boolean
  grayscaleEnabled?: boolean
  version: number
  createTime: string
  updateTime?: string
  predicates?: PredicateInfo[]
  filters?: FilterInfo[]
  plugins?: RoutePluginInfo[]
}

export interface PredicateInfo {
  id?: number
  predicateName?: string
  predicateType: string
  predicateTypeDesc?: string
  args?: Record<string, any>
  config?: Record<string, any>
}

export interface FilterInfo {
  id: number
  filterName: string
  filterType: string
  config?: Record<string, any>
}

export interface RoutePluginInfo {
  id: number
  pluginId: number
  pluginName: string
  pluginType: 'FILTER'
  config: Record<string, any>
  priorityOverride?: number
  defaultPriority: number
  effectivePriority: number
  enabled: boolean
  phase: string
}

export interface RouteQueryParams {
  routeId?: string
  routeName?: string
  uri?: string
  enabled?: boolean
  pageNum?: number
  pageSize?: number
}

export interface HeaderMatch {
  name: string
  value: string
  matchType: 'EXIST' | 'NOT_EXIST' | 'EQUAL' | 'REGEX'
}

export interface QueryMatch {
  name: string
  value: string
  matchType: 'EXIST' | 'NOT_EXIST' | 'EQUAL' | 'REGEX'
}

export interface RouteFormState {
  routeId: string
  routeName: string
  description: string
  order: number
  enabled: boolean
  pathPattern: string
  matchType: 'ANT' | 'REGEX' | 'EXACT'
  ignoreCase: boolean
  methods: string[]
  headers: HeaderMatch[]
  hosts: string[]
  queries: QueryMatch[]
  serviceName: string
  loadBalanceStrategy: LoadBalanceStrategy
  plugins: RoutePlugin[]
}

export interface RoutePlugin {
  pluginId: number
  pluginName: string
  pluginType: 'FILTER'
  config: Record<string, any>
  priorityOverride?: number
  enabled: boolean
}

export type LoadBalanceStrategy = 'ROUND_ROBIN' | 'RANDOM' | 'WEIGHTED_ROUND_ROBIN'

export interface RouteCreateRequest {
  routeId: string
  routeName: string
  description?: string
  uri: string
  order: number
  enabled: boolean
  matching: {
    path: {
      pattern: string
      matchType: string
      ignoreCase: boolean
    }
    methods?: string[]
    headers?: HeaderMatch[]
    hosts?: string[]
    queries?: QueryMatch[]
  }
  loadBalanceStrategy: string
  pathRewrite?: {
    from: string
    to: string
  }
  timeouts?: {
    connect: number
    response: number
  }
  plugins?: {
    pluginId: number
    config?: Record<string, any>
    priorityOverride?: number
    enabled: boolean
  }[]
}

export interface RouteUpdateRequest {
  routeName: string
  description?: string
  uri: string
  order: number
  enabled: boolean
  matching: {
    path: {
      pattern: string
      matchType: string
      ignoreCase: boolean
    }
    methods?: string[]
    headers?: HeaderMatch[]
    hosts?: string[]
    queries?: QueryMatch[]
  }
  loadBalanceStrategy: string
  pathRewrite?: {
    from: string
    to: string
  }
  timeouts?: {
    connect: number
    response: number
  }
  plugins?: {
    pluginId: number
    config?: Record<string, any>
    priorityOverride?: number
    enabled: boolean
  }[]
}

export const LOAD_BALANCE_STRATEGIES = [
  { value: 'ROUND_ROBIN', label: '轮询', description: '依次选择可用地址' },
  { value: 'RANDOM', label: '随机', description: '随机选择可用地址' },
  { value: 'WEIGHTED_ROUND_ROBIN', label: '加权轮询', description: '根据权重选择地址' }
]

export const MATCH_TYPES = [
  { value: 'ANT', label: 'ANT路径模式', description: '支持 **、*、? 通配符' },
  { value: 'REGEX', label: '正则表达式', description: '使用Java正则表达式' },
  { value: 'EXACT', label: '精确匹配', description: '路径必须完全一致' }
]

export const HTTP_METHODS = ['GET', 'POST', 'PUT', 'DELETE', 'HEAD', 'OPTIONS', 'PATCH', 'TRACE']

export const HEADER_MATCH_TYPES = [
  { value: 'EXIST', label: '存在' },
  { value: 'NOT_EXIST', label: '不存在' },
  { value: 'EQUAL', label: '等于' },
  { value: 'REGEX', label: '正则匹配' }
]

export interface PageResult<T> {
  data: T[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

export const routesApi = {
  list(params: RouteQueryParams) {
    return request<{ data: PageResult<Route> }>({
      url: '/api/routes',
      method: 'get',
      params
    })
  },

  detail(id: number) {
    return request<{ data: Route }>({
      url: `/api/routes/${id}`,
      method: 'get'
    })
  },

  create(data: RouteCreateRequest) {
    return request<{ data: number }>({
      url: '/api/routes',
      method: 'post',
      data
    })
  },

  update(id: number, data: RouteUpdateRequest) {
    return request<{ data: void }>({
      url: `/api/routes/${id}`,
      method: 'put',
      data
    })
  },

  delete(id: number) {
    return request<{ data: void }>({
      url: `/api/routes/${id}`,
      method: 'delete'
    })
  },

  batchDelete(ids: number[]) {
    return request<{ data: void }>({
      url: '/api/routes/batch',
      method: 'delete',
      data: ids
    })
  },

  enable(id: number) {
    return request<{ data: void }>({
      url: `/api/routes/${id}/enable`,
      method: 'post'
    })
  },

  disable(id: number) {
    return request<{ data: void }>({
      url: `/api/routes/${id}/disable`,
      method: 'post'
    })
  },

  getServiceNames() {
    return request<{ data: string[] }>({
      url: '/api/routes/services',
      method: 'get'
    })
  }
}

export function getDefaultFormState(): RouteFormState {
  return {
    routeId: '',
    routeName: '',
    description: '',
    order: 100,
    enabled: true,
    pathPattern: '',
    matchType: 'ANT',
    ignoreCase: false,
    methods: [],
    headers: [],
    hosts: [],
    queries: [],
    serviceName: '',
    loadBalanceStrategy: 'ROUND_ROBIN',
    plugins: []
  }
}