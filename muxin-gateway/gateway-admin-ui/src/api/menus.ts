import axios from 'axios'
import type { ApiResponse } from '@/types/auth'
import type { 
  Menu, 
  MenuTree,
  MenuCreateRequest, 
  MenuUpdateRequest,
  MenuQueryParams,
  PageResult
} from '@/types/system'

const API_BASE = '/api/menus'

export const menuApi = {
  // 获取当前用户菜单树
  getUserMenuTree: () => {
    return axios.get<ApiResponse<MenuTree[]>>(`${API_BASE}/user-tree`)
  },

  // 获取所有菜单树
  getAllMenuTree: () => {
    return axios.get<ApiResponse<MenuTree[]>>(`${API_BASE}/tree`)
  },

  // 分页查询菜单列表
  list: (params?: MenuQueryParams) => {
    return axios.get<ApiResponse<PageResult<Menu>>>(`${API_BASE}`, { params })
  },

  // 获取菜单详情
  getDetail: (id: number) => {
    return axios.get<ApiResponse<Menu>>(`${API_BASE}/${id}`)
  },

  // 创建菜单
  create: (data: MenuCreateRequest) => {
    return axios.post<ApiResponse<{ id: number }>>(`${API_BASE}`, data)
  },

  // 更新菜单
  update: (id: number, data: MenuUpdateRequest) => {
    return axios.put<ApiResponse<void>>(`${API_BASE}/${id}`, data)
  },

  // 删除菜单
  delete: (id: number) => {
    return axios.delete<ApiResponse<void>>(`${API_BASE}/${id}`)
  },

  // 批量删除菜单
  batchDelete: (ids: number[]) => {
    return axios.delete<ApiResponse<void>>(`${API_BASE}/batch`, { data: ids })
  },

  // 启用菜单
  enable: (id: number) => {
    return axios.post<ApiResponse<void>>(`${API_BASE}/${id}/enable`)
  },

  // 禁用菜单
  disable: (id: number) => {
    return axios.post<ApiResponse<void>>(`${API_BASE}/${id}/disable`)
  }
} 