import axios from 'axios'
import type { ApiResponse } from '@/types/auth'
import type { 
  Department, 
  DepartmentTree,
  DepartmentCreateRequest, 
  DepartmentUpdateRequest
} from '@/types/system'

const API_BASE = '/api/dept'

export const departmentApi = {
  // 获取部门树
  getTree: () => {
    return axios.get<ApiResponse<DepartmentTree[]>>(`${API_BASE}/tree`)
  },

  // 获取部门详情
  getDetail: (id: number) => {
    return axios.get<ApiResponse<Department>>(`${API_BASE}/${id}`)
  },

  // 获取子部门列表
  getChildren: (parentId: number) => {
    return axios.get<ApiResponse<Department[]>>(`${API_BASE}/children/${parentId}`)
  },

  // 创建部门
  create: (data: DepartmentCreateRequest) => {
    return axios.post<ApiResponse<{ id: number }>>(`${API_BASE}`, data)
  },

  // 更新部门
  update: (id: number, data: DepartmentUpdateRequest) => {
    return axios.put<ApiResponse<void>>(`${API_BASE}/${id}`, data)
  },

  // 删除部门
  delete: (id: number) => {
    return axios.delete<ApiResponse<void>>(`${API_BASE}/${id}`)
  },

  // 启用部门
  enable: (id: number) => {
    return axios.put<ApiResponse<void>>(`${API_BASE}/${id}/enable`)
  },

  // 禁用部门
  disable: (id: number) => {
    return axios.put<ApiResponse<void>>(`${API_BASE}/${id}/disable`)
  },

  // 移动部门
  move: (id: number, targetParentId: number) => {
    return axios.put<ApiResponse<void>>(`${API_BASE}/${id}/move/${targetParentId}`)
  },

  // 检查部门编码是否可用
  checkDeptCode: (deptCode: string, excludeId?: number) => {
    return axios.get<ApiResponse<boolean>>(`${API_BASE}/check-code`, {
      params: { deptCode, excludeId }
    })
  },

  // 获取部门统计信息
  getStats: () => {
    return axios.get<ApiResponse<{
      totalDepts: number
      activeDepts: number
      inactiveDepts: number
      deptUserCounts: Array<{
        deptId: number
        deptName: string
        userCount: number
        level: number
      }>
      maxLevel: number
    }>>(`${API_BASE}/stats`)
  },

  // 获取部门下的用户列表
  getDeptUsers: (deptId: number, params?: {
    page?: number
    size?: number
    username?: string
    includeSubDepts?: boolean // 是否包含子部门
  }) => {
    return axios.get<ApiResponse<{
      total: number
      list: Array<{
        id: number
        username: string
        nickname: string
        email: string
        mobile: string
        status: number
        roles: string[]
      }>
    }>>(`${API_BASE}/${deptId}/users`, { params })
  },

  // 批量移动用户到部门
  moveUsers: (targetDeptId: number, userIds: number[]) => {
    return axios.put<ApiResponse<void>>(`${API_BASE}/${targetDeptId}/move-users`, userIds)
  },

  // 复制部门结构
  copy: (id: number, data: {
    deptName: string
    deptCode?: string
    parentId: number
    copyUsers?: boolean // 是否复制用户
    copySubDepts?: boolean // 是否复制子部门
  }) => {
    return axios.post<ApiResponse<{ id: number }>>(`${API_BASE}/${id}/copy`, data)
  },

  // 导出部门结构
  export: (rootDeptId?: number) => {
    return axios.get<ApiResponse<string>>(`${API_BASE}/export`, {
      params: { rootDeptId },
      responseType: 'blob' as any
    })
  },

  // 导入部门结构
  import: (file: File, options?: {
    mergeMode?: 'overwrite' | 'skip' | 'merge' // 合并模式
  }) => {
    const formData = new FormData()
    formData.append('file', file)
    if (options?.mergeMode) {
      formData.append('mergeMode', options.mergeMode)
    }
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

  // 获取部门路径
  getDeptPath: (id: number) => {
    return axios.get<ApiResponse<Array<{
      id: number
      deptName: string
    }>>>(`${API_BASE}/${id}/path`)
  },

  // 调整部门排序
  updateSort: (items: Array<{
    id: number
    orderNum: number
  }>) => {
    return axios.put<ApiResponse<void>>(`${API_BASE}/sort`, items)
  },

  // 获取可选的父部门列表（排除自己和子部门）
  getSelectableParents: (excludeId?: number) => {
    return axios.get<ApiResponse<DepartmentTree[]>>(`${API_BASE}/selectable-parents`, {
      params: { excludeId }
    })
  }
} 