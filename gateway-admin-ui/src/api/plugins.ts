import request from '@/utils/request'

export interface PluginInfo {
  id: number
  pluginName: string
  pluginType: 'FILTER'
  description: string
  schema: Record<string, any>
  defaultConfig: Record<string, any>
  defaultPriority: number
  phase: string
  icon: string
  isSystem: boolean
  enabled: boolean
}

export const pluginsApi = {
  list(params?: { type?: string }) {
    return request<{ data: PluginInfo[] }>({
      url: '/api/plugins',
      method: 'get',
      params
    })
  },

  detail(id: number) {
    return request<{ data: PluginInfo }>({
      url: `/api/plugins/${id}`,
      method: 'get'
    })
  },

  create(data: Partial<PluginInfo>) {
    return request<{ data: number }>({
      url: '/api/plugins',
      method: 'post',
      data
    })
  },

  update(id: number, data: Partial<PluginInfo>) {
    return request<{ data: void }>({
      url: `/api/plugins/${id}`,
      method: 'put',
      data
    })
  },

  delete(id: number) {
    return request<{ data: void }>({
      url: `/api/plugins/${id}`,
      method: 'delete'
    })
  }
}