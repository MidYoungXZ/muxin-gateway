import axios from 'axios'
import type { ApiResponse } from '@/types/auth'
import type { 
  Role, 
  RoleCreateRequest, 
  RoleUpdateRequest,
  RoleQueryParams,
  PageResult
} from '@/types/system'

const API_BASE = '/api/roles'

export const roleApi = {
  // 获取角色列表（分页）
  list: (params?: RoleQueryParams) => {
    return axios.get<ApiResponse<PageResult<Role>>>(`${API_BASE}`, { params })
  },

  // 获取所有角色列表（不分页）
  listAll: () => {
    return axios.get<ApiResponse<Role[]>>(`${API_BASE}/all`)
  },

  // 获取角色详情
  getDetail: (id: number) => {
    return axios.get<ApiResponse<Role>>(`${API_BASE}/${id}`)
  },

  // 创建角色
  create: (data: RoleCreateRequest) => {
    return axios.post<ApiResponse<{ id: number }>>(`${API_BASE}`, data)
  },

  // 更新角色
  update: (id: number, data: RoleUpdateRequest) => {
    return axios.put<ApiResponse<void>>(`${API_BASE}/${id}`, data)
  },

  // 删除角色
  delete: (id: number) => {
    return axios.delete<ApiResponse<void>>(`${API_BASE}/${id}`)
  },

  // 批量删除角色
  batchDelete: (ids: number[]) => {
    return axios.delete<ApiResponse<void>>(`${API_BASE}/batch`, { data: ids })
  },

  // 启用角色
  enable: (id: number) => {
    return axios.post<ApiResponse<void>>(`${API_BASE}/${id}/enable`)
  },

  // 禁用角色
  disable: (id: number) => {
    return axios.post<ApiResponse<void>>(`${API_BASE}/${id}/disable`)
  },

  // 分配菜单权限
  assignMenus: (id: number, menuIds: number[]) => {
    return axios.put<ApiResponse<void>>(`${API_BASE}/${id}/menus`, menuIds)
  },

  // 获取角色的菜单ID列表
  getRoleMenuIds: (id: number) => {
    return axios.get<ApiResponse<number[]>>(`${API_BASE}/${id}/menu-ids`)
  },

  // 检查角色编码是否可用
  checkRoleCode: (roleCode: string, excludeId?: number) => {
    return axios.get<ApiResponse<boolean>>(`${API_BASE}/check-code`, {
      params: { roleCode, excludeId }
    })
  },

  // 获取角色统计信息
  getStats: () => {
    return axios.get<ApiResponse<{
      totalRoles: number
      activeRoles: number
      inactiveRoles: number
      roleUserCounts: Array<{
        roleId: number
        roleName: string
        userCount: number
      }>
    }>>(`${API_BASE}/stats`)
  },

  // 复制角色
  copy: (id: number, data: {
    roleCode: string
    roleName: string
    description?: string
  }) => {
    return axios.post<ApiResponse<{ id: number }>>(`${API_BASE}/${id}/copy`, data)
  },

  // 导出角色配置
  export: (ids?: number[]) => {
    return axios.post<ApiResponse<string>>(`${API_BASE}/export`, { ids }, {
      responseType: 'blob' as any
    })
  },

  // 导入角色配置
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

  // 获取角色权限树
  getPermissionTree: (id: number) => {
    return axios.get<ApiResponse<{
      checkedKeys: number[]
      halfCheckedKeys: number[]
      expandedKeys: number[]
    }>>(`${API_BASE}/${id}/permission-tree`)
  },

  // 批量分配用户到角色
  assignUsers: (roleId: number, userIds: number[]) => {
    return axios.put<ApiResponse<void>>(`${API_BASE}/${roleId}/users`, userIds)
  },

  // 获取角色下的用户列表
  getRoleUsers: (roleId: number, params?: {
    page?: number
    size?: number
    username?: string
  }) => {
    return axios.get<ApiResponse<PageResult<{
      id: number
      username: string
      nickname: string
      email: string
      deptName: string
      status: number
    }>>>(`${API_BASE}/${roleId}/users`, { params })
  }
} 