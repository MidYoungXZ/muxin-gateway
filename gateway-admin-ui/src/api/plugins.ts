import request from '@/utils/request'
import type { PluginInfo } from './routes'

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