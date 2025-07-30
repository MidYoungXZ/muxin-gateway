import request from '@/utils/request'
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
    return request({
      url: `${API_BASE}/user-tree`,
      method: 'get'
    })
  },

  // 获取所有菜单树（用于权限分配）
  getMenuTree: () => {
    return request({
      url: `${API_BASE}/tree`,
      method: 'get'
    })
  },

  // 获取所有菜单树（旧方法名兼容）
  getAllMenuTree: () => {
    return request({
      url: `${API_BASE}/tree`,
      method: 'get'
    })
  },

  // 分页查询菜单列表
  list: (params?: MenuQueryParams) => {
    return request({
      url: API_BASE,
      method: 'get',
      params
    })
  },

  // 获取菜单详情
  getDetail: (id: number) => {
    return request({
      url: `${API_BASE}/${id}`,
      method: 'get'
    })
  },

  // 创建菜单
  create: (data: MenuCreateRequest) => {
    return request({
      url: API_BASE,
      method: 'post',
      data
    })
  },

  // 更新菜单
  update: (id: number, data: MenuUpdateRequest) => {
    return request({
      url: `${API_BASE}/${id}`,
      method: 'put',
      data
    })
  },

  // 删除菜单
  delete: (id: number) => {
    return request({
      url: `${API_BASE}/${id}`,
      method: 'delete'
    })
  },

  // 批量删除菜单
  batchDelete: (ids: number[]) => {
    return request({
      url: `${API_BASE}/batch`,
      method: 'delete',
      data: ids
    })
  },

  // 启用菜单
  enable: (id: number) => {
    return request({
      url: `${API_BASE}/${id}/enable`,
      method: 'post'
    })
  },

  // 禁用菜单
  disable: (id: number) => {
    return request({
      url: `${API_BASE}/${id}/disable`,
      method: 'post'
    })
  }
} 