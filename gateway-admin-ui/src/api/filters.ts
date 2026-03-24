import request from '@/utils/request'

export interface Filter {
  id: number
  filterName: string
  filterType: string
  description?: string
  config?: Record<string, any>
  order: number
  isSystem: boolean
  enabled: boolean
  usageCount?: number
  createTime: string
  updateTime?: string
}

export interface FilterQueryParams {
  filterName?: string
  filterType?: string
  enabled?: boolean
  isSystem?: boolean
  pageNum?: number
  pageSize?: number
}

export interface FilterCreateRequest {
  filterName: string
  filterType: string
  description?: string
  config?: Record<string, any>
  order: number
}

export interface FilterUpdateRequest {
  filterName: string
  description?: string
  config?: Record<string, any>
  order: number
}

export interface FilterType {
  value: string
  label: string
  description: string
  configTemplate: Record<string, any>
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

export const filtersApi = {
  list(params: FilterQueryParams) {
    return request<{ data: PageResult<Filter> }>({
      url: '/api/filters',
      method: 'get',
      params
    })
  },

  getAvailable() {
    return request<{ data: Filter[] }>({
      url: '/api/filters/available',
      method: 'get'
    })
  },

  getByType(type: string) {
    return request<{ data: Filter[] }>({
      url: `/api/filters/type/${type}`,
      method: 'get'
    })
  },

  detail(id: number) {
    return request<{ data: Filter }>({
      url: `/api/filters/${id}`,
      method: 'get'
    })
  },

  create(data: FilterCreateRequest) {
    return request<{ data: number }>({
      url: '/api/filters',
      method: 'post',
      data
    })
  },

  update(id: number, data: FilterUpdateRequest) {
    return request<{ data: void }>({
      url: `/api/filters/${id}`,
      method: 'put',
      data
    })
  },

  delete(id: number) {
    return request<{ data: void }>({
      url: `/api/filters/${id}`,
      method: 'delete'
    })
  },

  batchDelete(ids: number[]) {
    return request<{ data: void }>({
      url: '/api/filters/batch',
      method: 'delete',
      data: ids
    })
  },

  enable(id: number) {
    return request<{ data: void }>({
      url: `/api/filters/${id}/enable`,
      method: 'post'
    })
  },

  disable(id: number) {
    return request<{ data: void }>({
      url: `/api/filters/${id}/disable`,
      method: 'post'
    })
  },

  getTypes() {
    return request<{ data: FilterType[] }>({
      url: '/api/filters/types',
      method: 'get'
    })
  },

  getUsedRoutes(id: number) {
    return request<{ data: RouteSimple[] }>({
      url: `/api/filters/${id}/routes`,
      method: 'get'
    })
  }
}