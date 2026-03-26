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
}

export interface PredicateInfo {
  id: number
  predicateName: string
  predicateType: string
  predicateTypeDesc?: string
  config?: Record<string, any>
}

export interface FilterInfo {
  id: number
  filterName: string
  filterType: string
  config?: Record<string, any>
}

export interface RouteQueryParams {
  routeId?: string
  routeName?: string
  uri?: string
  enabled?: boolean
  pageNum?: number
  pageSize?: number
}

export interface RouteCreateRequest {
  routeId: string
  routeName: string
  description?: string
  uri: string
  predicateIds: number[]
  filterIds?: number[]
  metadata?: Record<string, any>
  order?: number
  loadBalanceStrategy?: string
  enabled?: boolean
}

export interface RouteUpdateRequest {
  routeName: string
  description?: string
  uri: string
  predicateIds: number[]
  filterIds?: number[]
  metadata?: Record<string, any>
  order?: number
  loadBalanceStrategy?: string
  enabled?: boolean
}

export const LOAD_BALANCE_STRATEGIES = [
  { value: 'ROUND_ROBIN', label: '轮询', description: '依次选择可用地址' },
  { value: 'RANDOM', label: '随机', description: '随机选择可用地址' },
  { value: 'WEIGHTED_ROUND_ROBIN', label: '加权轮询', description: '根据权重选择地址' },
  { value: 'LEAST_CONNECTIONS', label: '最少连接', description: '选择连接数最少的地址' }
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