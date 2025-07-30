import request from '@/utils/request'
import type { PageParams, PageResult } from '@/types/common'

// 过滤器类型定义
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
  createBy?: string
  updateBy?: string
}

// 过滤器查询参数
export interface FilterQueryParams extends PageParams {
  filterName?: string
  filterType?: string
  enabled?: boolean
  isSystem?: boolean
}

// 过滤器创建参数
export interface FilterCreateRequest {
  filterName: string
  filterType: string
  description?: string
  config?: Record<string, any>
  order: number
}

// 过滤器更新参数
export interface FilterUpdateRequest {
  filterName: string
  description?: string
  config?: Record<string, any>
  order: number
}

// 过滤器类型定义
export interface FilterType {
  value: string
  label: string
  description: string
  configTemplate: Record<string, any>
}

export const filtersApi = {
  // 分页查询过滤器列表
  list(params: FilterQueryParams) {
    return request<PageResult<Filter>>({
      url: '/api/filters',
      method: 'get',
      params
    })
  },

  // 获取所有可用过滤器
  getAvailable() {
    return request<Filter[]>({
      url: '/api/filters/available',
      method: 'get'
    })
  },

  // 根据类型获取过滤器列表
  getByType(type: string) {
    return request<Filter[]>({
      url: `/api/filters/type/${type}`,
      method: 'get'
    })
  },

  // 获取过滤器详情
  detail(id: number) {
    return request<Filter>({
      url: `/api/filters/${id}`,
      method: 'get'
    })
  },

  // 创建过滤器
  create(data: FilterCreateRequest) {
    return request<number>({
      url: '/api/filters',
      method: 'post',
      data
    })
  },

  // 更新过滤器
  update(id: number, data: FilterUpdateRequest) {
    return request<void>({
      url: `/api/filters/${id}`,
      method: 'put',
      data
    })
  },

  // 删除过滤器
  delete(id: number) {
    return request<void>({
      url: `/api/filters/${id}`,
      method: 'delete'
    })
  },

  // 批量删除过滤器
  batchDelete(ids: number[]) {
    return request<void>({
      url: '/api/filters/batch',
      method: 'delete',
      data: ids
    })
  },

  // 启用过滤器
  enable(id: number) {
    return request<void>({
      url: `/api/filters/${id}/enable`,
      method: 'post'
    })
  },

  // 禁用过滤器
  disable(id: number) {
    return request<void>({
      url: `/api/filters/${id}/disable`,
      method: 'post'
    })
  },

  // 获取过滤器类型列表
  getTypes() {
    return request<FilterType[]>({
      url: '/api/filters/types',
      method: 'get'
    })
  }
} 