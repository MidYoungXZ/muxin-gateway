import request from '@/utils/request'

export interface LoadBalance {
  id: number
  routeId: number
  routeName?: string
  strategy: string
  strategyDesc?: string
  config?: Record<string, any>
  enabled: boolean
  createTime?: string
  updateTime?: string
}

export interface LoadBalanceQueryParams {
  routeId?: number
  strategy?: string
  enabled?: boolean
  pageNum?: number
  pageSize?: number
}

export interface LoadBalanceCreateRequest {
  routeId: number
  strategy: string
  config?: Record<string, any>
  enabled?: boolean
}

export interface LoadBalanceUpdateRequest {
  strategy: string
  config?: Record<string, any>
  enabled?: boolean
}

export interface LoadBalanceStrategy {
  code: string
  name: string
  description: string
  configFields: ConfigField[]
}

export interface ConfigField {
  field: string
  label: string
  type: string
  required: boolean
  defaultValue?: any
  placeholder?: string
  description?: string
}

export interface PageResult<T> {
  data: T[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

export const loadBalanceApi = {
  list(params: LoadBalanceQueryParams) {
    return request<{ data: PageResult<LoadBalance> }>({
      url: '/api/load-balance',
      method: 'get',
      params
    })
  },

  getByRouteId(routeId: number) {
    return request<{ data: LoadBalance }>({
      url: `/api/load-balance/route/${routeId}`,
      method: 'get'
    })
  },

  getDetail(id: number) {
    return request<{ data: LoadBalance }>({
      url: `/api/load-balance/${id}`,
      method: 'get'
    })
  },

  create(data: LoadBalanceCreateRequest) {
    return request<{ data: number }>({
      url: '/api/load-balance',
      method: 'post',
      data
    })
  },

  update(id: number, data: LoadBalanceUpdateRequest) {
    return request<{ data: void }>({
      url: `/api/load-balance/${id}`,
      method: 'put',
      data
    })
  },

  delete(id: number) {
    return request<{ data: void }>({
      url: `/api/load-balance/${id}`,
      method: 'delete'
    })
  },

  enable(id: number) {
    return request<{ data: void }>({
      url: `/api/load-balance/${id}/enable`,
      method: 'post'
    })
  },

  disable(id: number) {
    return request<{ data: void }>({
      url: `/api/load-balance/${id}/disable`,
      method: 'post'
    })
  },

  getStrategies() {
    return request<{ data: LoadBalanceStrategy[] }>({
      url: '/api/load-balance/strategies',
      method: 'get'
    })
  }
}