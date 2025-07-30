import request from '@/utils/request'

const API_BASE = '/api/roles'

// 角色类型定义
export interface Role {
  id: number
  roleCode: string
  roleName: string
  description: string
  status: number
  createTime: string
  updateTime: string
}

export interface RoleCreateRequest {
  roleCode: string
  roleName: string
  description?: string
  status: number
}

export interface RoleUpdateRequest {
  roleName: string
  description?: string
  status: number
}

export interface RoleQueryParams {
  roleName?: string
  roleCode?: string
  status?: number
  page?: number
  size?: number
}

export const roleApi = {
  // 获取角色列表（分页）
  list: (params?: RoleQueryParams) => {
    return request({
      url: '/api/roles',
      method: 'get',
      params
    })
  },

  // 获取所有角色列表（不分页）
  listAll: () => {
    return request({
      url: '/api/roles/all',
      method: 'get'
    })
  },

  // 获取角色详情
  getDetail: (id: number) => {
    return request({
      url: `/api/roles/${id}`,
      method: 'get'
    })
  },

  // 创建角色
  create: (data: RoleCreateRequest) => {
    return request({
      url: '/api/roles',
      method: 'post',
      data
    })
  },

  // 更新角色
  update: (id: number, data: RoleUpdateRequest) => {
    return request({
      url: `/api/roles/${id}`,
      method: 'put',
      data
    })
  },

  // 删除角色
  delete: (id: number) => {
    return request({
      url: `/api/roles/${id}`,
      method: 'delete'
    })
  },

  // 批量删除角色
  batchDelete: (ids: number[]) => {
    return request({
      url: '/api/roles/batch',
      method: 'delete',
      data: ids
    })
  },

  // 启用角色
  enable: (id: number) => {
    return request({
      url: `/api/roles/${id}/enable`,
      method: 'post'
    })
  },

  // 禁用角色
  disable: (id: number) => {
    return request({
      url: `/api/roles/${id}/disable`,
      method: 'post'
    })
  },

  // 分配菜单权限
  assignMenus: (id: number, menuIds: number[]) => {
    return request({
      url: `/api/roles/${id}/menus`,
      method: 'put',
      data: menuIds
    })
  },

  // 获取角色的菜单ID列表
  getRoleMenuIds: (id: number) => {
    return request({
      url: `/api/roles/${id}/menu-ids`,
      method: 'get'
    })
  },

  // 检查角色编码是否可用
  checkRoleCode: (roleCode: string, excludeId?: number) => {
    return request({
      url: '/api/roles/check-code',
      method: 'get',
      params: { roleCode, excludeId }
    })
  },

  // 获取角色统计信息
  getStats: () => {
    return request({
      url: '/api/roles/stats',
      method: 'get'
    })
  },

  // 复制角色
  copy: (id: number, data: {
    roleCode: string
    roleName: string
    description?: string
  }) => {
    return request({
      url: `/api/roles/${id}/copy`,
      method: 'post',
      data
    })
  },

  // 导出角色配置
  export: (ids?: number[]) => {
    return request({
      url: '/api/roles/export',
      method: 'post',
      data: { ids },
      responseType: 'blob' as any
    })
  },

  // 导入角色配置
  import: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/api/roles/import',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 获取角色权限树
  getPermissionTree: (id: number) => {
    return request({
      url: `/api/roles/${id}/permission-tree`,
      method: 'get'
    })
  },

  // 批量分配用户到角色
  assignUsers: (roleId: number, userIds: number[]) => {
    return request({
      url: `/api/roles/${roleId}/users`,
      method: 'put',
      data: userIds
    })
  },

  // 获取角色下的用户列表
  getRoleUsers: (roleId: number, params?: {
    page?: number
    size?: number
    username?: string
  }) => {
    return request({
      url: `/api/roles/${roleId}/users`,
      method: 'get',
      params
    })
  },

  // 更新角色权限
  updatePermissions: (id: number, permissionIds: number[]) => {
    return request({
      url: `/api/roles/${id}/permissions`,
      method: 'put',
      data: permissionIds
    })
  },

  // 获取角色权限ID列表
  getPermissionIds: (id: number) => {
    return request({
      url: `/api/roles/${id}/permissions`,
      method: 'get'
    })
  }
} 