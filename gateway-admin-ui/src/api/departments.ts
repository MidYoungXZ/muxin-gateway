import request from '@/utils/request'

export interface Department {
  id: number
  parentId: number
  deptName: string
  deptCode?: string
  orderNum: number
  leader?: string
  phone?: string
  email?: string
  status: number
  createTime: string
  updateTime: string
  children?: Department[]
}

export interface DepartmentCreateRequest {
  parentId: number
  deptName: string
  deptCode?: string
  orderNum?: number
  leader?: string
  phone?: string
  email?: string
  status?: number
}

export interface DepartmentUpdateRequest {
  deptName: string
  deptCode?: string
  parentId?: number
  orderNum?: number
  leader?: string
  phone?: string
  email?: string
  status?: number
}

export const departmentApi = {
  getTree: (params?: { deptName?: string; status?: number }) => 
    request.get('/api/dept/tree', { params }),
  
  getOptions: () => 
    request.get('/api/dept/options'),
  
  getDetail: (id: number) => 
    request.get(`/api/dept/${id}`),
  
  getChildren: (parentId: number) => 
    request.get(`/api/dept/children/${parentId}`),
  
  create: (data: DepartmentCreateRequest) => 
    request.post('/api/dept', data),
  
  update: (id: number, data: DepartmentUpdateRequest) => 
    request.put(`/api/dept/${id}`, data),
  
  delete: (id: number) => 
    request.delete(`/api/dept/${id}`),
  
  enable: (id: number) => 
    request.put(`/api/dept/${id}/enable`),
  
  disable: (id: number) => 
    request.put(`/api/dept/${id}/disable`),
  
  move: (id: number, targetParentId: number) => 
    request.put(`/api/dept/${id}/move/${targetParentId}`),
  
  checkName: (deptName: string, parentId: number, excludeId?: number) => 
    request.get('/api/dept/check-name', { params: { deptName, parentId, excludeId } }),
  
  checkCode: (deptCode: string, excludeId?: number) => 
    request.get('/api/dept/check-code', { params: { deptCode, excludeId } }),
  
  getStats: () => 
    request.get('/api/dept/stats')
}