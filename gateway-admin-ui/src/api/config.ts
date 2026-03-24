import request from '@/utils/request'

export interface Config {
  id: number
  configKey: string
  configValue: string
  configName: string
  description: string
  status: number
  statusText: string
  createTime: string
  updateTime: string
}

export interface ConfigQueryParams {
  configKey?: string
  configName?: string
  status?: number
  pageNum?: number
  pageSize?: number
}

export interface ConfigCreateParams {
  configKey: string
  configValue: string
  configName: string
  description?: string
  status?: number
}

export interface ConfigUpdateParams {
  configValue?: string
  configName?: string
  description?: string
  status?: number
}

export const configApi = {
  list: (params: ConfigQueryParams) => 
    request.get('/api/configs', { params }),
  
  getAll: () => 
    request.get('/api/configs/all'),
  
  getDetail: (id: number) => 
    request.get(`/api/configs/${id}`),
  
  getByKey: (configKey: string) => 
    request.get(`/api/configs/key/${configKey}`),
  
  create: (data: ConfigCreateParams) => 
    request.post('/api/configs', data),
  
  update: (id: number, data: ConfigUpdateParams) => 
    request.put(`/api/configs/${id}`, data),
  
  delete: (id: number) => 
    request.delete(`/api/configs/${id}`),
  
  batchDelete: (ids: number[]) => 
    request.delete('/api/configs/batch', { data: ids }),
  
  enable: (id: number) => 
    request.post(`/api/configs/${id}/enable`),
  
  disable: (id: number) => 
    request.post(`/api/configs/${id}/disable`),
  
  checkKey: (configKey: string, excludeId?: number) => 
    request.get('/api/configs/check-key', { params: { configKey, excludeId } }),
  
  refreshCache: () => 
    request.post('/api/configs/refresh-cache')
}