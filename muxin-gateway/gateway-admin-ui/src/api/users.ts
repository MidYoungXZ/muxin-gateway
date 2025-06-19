import axios from 'axios'
import type { ApiResponse } from '@/types/auth'
import type { 
  User, 
  UserCreateRequest, 
  UserUpdateRequest,
  UserQueryParams,
  PageResult,
  PasswordUpdateRequest
} from '@/types/system'

const API_BASE = '/api/users'

export const userApi = {
  // 获取用户列表
  list: (params?: UserQueryParams) => {
    return axios.get<ApiResponse<PageResult<User>>>(`${API_BASE}`, { params })
  },

  // 获取用户详情
  getDetail: (id: number) => {
    return axios.get<ApiResponse<User>>(`${API_BASE}/${id}`)
  },

  // 获取当前用户信息
  getCurrentUser: () => {
    return axios.get<ApiResponse<User>>(`${API_BASE}/current`)
  },

  // 创建用户
  create: (data: UserCreateRequest) => {
    return axios.post<ApiResponse<{ id: number }>>(`${API_BASE}`, data)
  },

  // 更新用户
  update: (id: number, data: UserUpdateRequest) => {
    return axios.put<ApiResponse<void>>(`${API_BASE}/${id}`, data)
  },

  // 删除用户
  delete: (id: number) => {
    return axios.delete<ApiResponse<void>>(`${API_BASE}/${id}`)
  },

  // 批量删除用户
  batchDelete: (ids: number[]) => {
    return axios.delete<ApiResponse<void>>(`${API_BASE}/batch`, { data: ids })
  },

  // 启用用户
  enable: (id: number) => {
    return axios.post<ApiResponse<void>>(`${API_BASE}/${id}/enable`)
  },

  // 禁用用户
  disable: (id: number) => {
    return axios.post<ApiResponse<void>>(`${API_BASE}/${id}/disable`)
  },

  // 重置密码
  resetPassword: (id: number, newPassword: string) => {
    return axios.post<ApiResponse<void>>(`${API_BASE}/${id}/reset-password`, null, {
      params: { newPassword }
    })
  },

  // 修改密码
  updatePassword: (id: number, data: PasswordUpdateRequest) => {
    return axios.put<ApiResponse<void>>(`${API_BASE}/${id}/password`, data)
  },

  // 分配角色
  assignRoles: (id: number, roleIds: number[]) => {
    return axios.put<ApiResponse<void>>(`${API_BASE}/${id}/roles`, roleIds)
  },

  // 获取用户的角色ID列表
  getUserRoleIds: (id: number) => {
    return axios.get<ApiResponse<number[]>>(`${API_BASE}/${id}/role-ids`)
  },

  // 批量操作用户状态
  batchUpdateStatus: (ids: number[], status: 0 | 1) => {
    return axios.patch<ApiResponse<void>>(`${API_BASE}/batch/status`, { ids, status })
  },

  // 导出用户数据
  export: (params?: UserQueryParams) => {
    return axios.get<ApiResponse<string>>(`${API_BASE}/export`, { 
      params,
      responseType: 'blob' as any
    })
  },

  // 导入用户数据
  import: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return axios.post<ApiResponse<{
      successCount: number
      failedCount: number
      errors?: string[]
    }>>(`${API_BASE}/import`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 获取用户统计信息
  getStats: () => {
    return axios.get<ApiResponse<{
      totalUsers: number
      activeUsers: number
      inactiveUsers: number
      todayNewUsers: number
      usersByDept: Array<{
        deptName: string
        userCount: number
      }>
      usersByRole: Array<{
        roleName: string
        userCount: number
      }>
    }>>(`${API_BASE}/stats`)
  },

  // 检查用户名是否可用
  checkUsername: (username: string, excludeId?: number) => {
    return axios.get<ApiResponse<boolean>>(`${API_BASE}/check-username`, {
      params: { username, excludeId }
    })
  },

  // 获取用户操作日志
  getUserLogs: (userId: number, params?: {
    page?: number
    size?: number
    startTime?: string
    endTime?: string
  }) => {
    return axios.get<ApiResponse<PageResult<{
      id: number
      operation: string
      details: string
      ip: string
      userAgent: string
      createTime: string
    }>>>(`${API_BASE}/${userId}/logs`, { params })
  }
} 