import axios from 'axios'
import type { ApiResponse } from '@/types/auth'
import type { 
  RouteTemplate, 
  RouteTemplateCreateRequest, 
  RouteTemplateUpdateRequest,
  TemplateVariable 
} from '@/types/route'

const API_BASE = '/api/templates'

export const templateApi = {
  // 获取模板列表
  list: (params?: {
    page?: number
    size?: number
    category?: string
    isSystem?: boolean
    enabled?: boolean
    keyword?: string
  }) => {
    return axios.get<ApiResponse<{
      total: number
      list: RouteTemplate[]
    }>>(`${API_BASE}`, { params })
  },

  // 获取模板详情
  getDetail: (id: number) => {
    return axios.get<ApiResponse<RouteTemplate>>(`${API_BASE}/${id}`)
  },

  // 创建模板
  create: (data: RouteTemplateCreateRequest) => {
    return axios.post<ApiResponse<{ id: number }>>(`${API_BASE}`, data)
  },

  // 更新模板
  update: (id: number, data: RouteTemplateUpdateRequest) => {
    return axios.put<ApiResponse<void>>(`${API_BASE}/${id}`, data)
  },

  // 删除模板
  delete: (id: number) => {
    return axios.delete<ApiResponse<void>>(`${API_BASE}/${id}`)
  },

  // 启用/禁用模板
  toggleEnabled: (id: number, enabled: boolean) => {
    return axios.patch<ApiResponse<void>>(`${API_BASE}/${id}/enabled`, { enabled })
  },

  // 获取模板分类列表
  getCategories: () => {
    return axios.get<ApiResponse<string[]>>(`${API_BASE}/categories`)
  },

  // 从现有路由创建模板
  createFromRoute: (routeId: number, data: {
    templateName: string
    description?: string
    category?: string
    variables?: TemplateVariable[]
  }) => {
    return axios.post<ApiResponse<{ id: number }>>(`${API_BASE}/from-route/${routeId}`, data)
  },

  // 应用模板到路由
  applyToRoute: (templateId: number, data: {
    routeName: string
    description?: string
    uri: string
    variables?: Record<string, any>
  }) => {
    return axios.post<ApiResponse<{ routeId: number }>>(`${API_BASE}/${templateId}/apply`, data)
  },

  // 预览模板效果
  preview: (templateId: number, variables?: Record<string, any>) => {
    return axios.post<ApiResponse<{
      predicates: any[]
      filters: any[]
      metadata?: Record<string, any>
    }>>(`${API_BASE}/${templateId}/preview`, { variables })
  },

  // 获取模板使用统计
  getUsageStats: (templateId: number) => {
    return axios.get<ApiResponse<{
      usageCount: number
      recentUsage: Array<{
        routeId: string
        routeName: string
        createTime: string
      }>
    }>>(`${API_BASE}/${templateId}/stats`)
  },

  // 导入模板（批量）
  import: (templates: RouteTemplateCreateRequest[]) => {
    return axios.post<ApiResponse<{
      successCount: number
      failedCount: number
      errors?: string[]
    }>>(`${API_BASE}/import`, { templates })
  },

  // 导出模板
  export: (ids: number[]) => {
    return axios.post<ApiResponse<RouteTemplate[]>>(`${API_BASE}/export`, { ids })
  }
} 