import request from '@/utils/request'

export interface Predicate {
  id: number
  predicateName: string
  predicateType: string
  predicateTypeDesc?: string
  description?: string
  args?: Record<string, any>
  isSystem: boolean
  enabled: boolean
  usageCount?: number
  createTime: string
  updateTime?: string
}

export interface PredicateQueryParams {
  predicateName?: string
  predicateType?: string
  enabled?: boolean
  isSystem?: boolean
  pageNum?: number
  pageSize?: number
}

export interface PredicateCreateRequest {
  predicateName: string
  predicateType: string
  description?: string
  args: Record<string, any>
  enabled?: boolean
}

export interface PredicateUpdateRequest {
  predicateName: string
  description?: string
  args: Record<string, any>
  enabled?: boolean
}

export interface PredicateType {
  type: string
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

export interface RouteSimple {
  id: number
  routeId: string
  routeName: string
  enabled: boolean
}

export interface PageResult<T> {
  data: T[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

export const predicatesApi = {
  list(params: PredicateQueryParams) {
    return request<{ data: PageResult<Predicate> }>({
      url: '/api/predicates',
      method: 'get',
      params
    })
  },

  getAvailable() {
    return request<{ data: Predicate[] }>({
      url: '/api/predicates/available',
      method: 'get'
    })
  },

  getByType(type: string) {
    return request<{ data: Predicate[] }>({
      url: `/api/predicates/type/${type}`,
      method: 'get'
    })
  },

  detail(id: number) {
    return request<{ data: Predicate }>({
      url: `/api/predicates/${id}`,
      method: 'get'
    })
  },

  create(data: PredicateCreateRequest) {
    return request<{ data: number }>({
      url: '/api/predicates',
      method: 'post',
      data
    })
  },

  update(id: number, data: PredicateUpdateRequest) {
    return request<{ data: void }>({
      url: `/api/predicates/${id}`,
      method: 'put',
      data
    })
  },

  delete(id: number) {
    return request<{ data: void }>({
      url: `/api/predicates/${id}`,
      method: 'delete'
    })
  },

  batchDelete(ids: number[]) {
    return request<{ data: void }>({
      url: '/api/predicates/batch',
      method: 'delete',
      data: ids
    })
  },

  enable(id: number) {
    return request<{ data: void }>({
      url: `/api/predicates/${id}/enable`,
      method: 'post'
    })
  },

  disable(id: number) {
    return request<{ data: void }>({
      url: `/api/predicates/${id}/disable`,
      method: 'post'
    })
  },

  getTypes() {
    return request<{ data: PredicateType[] }>({
      url: '/api/predicates/types',
      method: 'get'
    })
  },

  getUsedRoutes(id: number) {
    return request<{ data: RouteSimple[] }>({
      url: `/api/predicates/${id}/routes`,
      method: 'get'
    })
  }
}