import request from '@/utils/request'
import type { PageParams, PageResult } from '@/types/common'

// 用户数据类型
export interface User {
  id: number
  username: string
  nickname: string
  email: string
  mobile: string
  avatar?: string
  deptId?: number
  deptName?: string
  status: 0 | 1
  roles?: any[]
  createTime: string
  updateTime?: string
  createBy?: string
  updateBy?: string
}

// 用户查询参数
export interface UserQueryParams extends PageParams {
  username?: string
  nickname?: string
  email?: string
  mobile?: string
  status?: 0 | 1
  deptId?: number
}

// 用户创建参数
export interface UserCreateRequest {
  username: string
  password: string
  nickname: string
  email?: string
  mobile?: string
  status?: 0 | 1
  deptId?: number
}

// 用户更新参数
export interface UserUpdateRequest {
  nickname: string
  email?: string
  mobile?: string
  status?: 0 | 1
  deptId?: number
}

export const userApi = {
  // 获取用户列表
  list: (params?: UserQueryParams) => {
    return request<PageResult<User>>({
      url: '/api/users',
      method: 'get',
      params
    })
  },

  // 获取用户详情
  getDetail: (id: number) => {
    return request<User>({
      url: `/api/users/${id}`,
      method: 'get'
    })
  },

  // 获取当前用户信息
  getCurrentUser: () => {
    return request<User>({
      url: '/api/users/current',
      method: 'get'
    })
  },

  // 创建用户
  create: (data: UserCreateRequest) => {
    return request<number>({
      url: '/api/users',
      method: 'post',
      data
    })
  },

  // 更新用户
  update: (id: number, data: UserUpdateRequest) => {
    return request<void>({
      url: `/api/users/${id}`,
      method: 'put',
      data
    })
  },

  // 删除用户
  delete: (id: number) => {
    return request({
      url: `/api/users/${id}`,
      method: 'delete'
    })
  },

  // 批量删除用户
  batchDelete: (ids: number[]) => {
    return request({
      url: '/api/users/batch',
      method: 'delete',
      data: ids
    })
  },

  // 启用用户
  enable: (id: number) => {
    return request({
      url: `/api/users/${id}/enable`,
      method: 'post'
    })
  },

  // 禁用用户
  disable: (id: number) => {
    return request({
      url: `/api/users/${id}/disable`,
      method: 'post'
    })
  },

  // 重置密码
  resetPassword: (id: number, newPassword: string) => {
    return request({
      url: `/api/users/${id}/reset-password`,
      method: 'post',
      params: { newPassword }
    })
  },

  // 修改密码
  updatePassword: (id: number, data: any) => {
    return request({
      url: `/api/users/${id}/password`,
      method: 'put',
      data
    })
  },

  // 分配角色
  assignRoles: (id: number, roleIds: number[]) => {
    return request({
      url: `/api/users/${id}/roles`,
      method: 'put',
      data: roleIds
    })
  },

  // 获取用户的角色ID列表
  getUserRoleIds: (id: number) => {
    return request({
      url: `/api/users/${id}/role-ids`,
      method: 'get'
    })
  },

  // 批量操作用户状态
  batchUpdateStatus: (ids: number[], status: 0 | 1) => {
    return request({
      url: '/api/users/batch/status',
      method: 'patch',
      data: { ids, status }
    })
  },

  // 导出用户数据
  export: (params?: any) => {
    return request({
      url: '/api/users/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  },

  // 导入用户数据
  import: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/api/users/import',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 获取用户统计信息
  getStats: () => {
    return request({
      url: '/api/users/stats',
      method: 'get'
    })
  },

  // 检查用户名是否可用
  checkUsername: (username: string, excludeId?: number) => {
    return request({
      url: '/api/users/check-username',
      method: 'get',
      params: { username, excludeId }
    })
  },

  // 获取用户操作日志
  getUserLogs: (userId: number, params?: any) => {
    return request({
      url: `/api/users/${userId}/logs`,
      method: 'get',
      params
    })
  }
} 