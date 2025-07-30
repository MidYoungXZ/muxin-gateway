import request from '@/utils/request'

const API_BASE = '/api/dept'

// 部门类型定义
export interface Department {
  id: number
  parentId: number
  deptName: string
  orderNum: number
  leader?: string
  phone?: string
  email?: string
  status: number
  createTime: string
  updateTime: string
  children?: Department[]
}

export interface DepartmentTree {
  id: number
  deptName: string
  parentId: number
  children?: DepartmentTree[]
}

export interface DepartmentCreateRequest {
  parentId: number
  deptName: string
  orderNum: number
  leader?: string
  phone?: string
  email?: string
  status: number
}

export interface DepartmentUpdateRequest {
  deptName: string
  orderNum: number
  leader?: string
  phone?: string
  email?: string
  status: number
}

export const departmentApi = {
  // 获取部门树
  getTree: () => {
    return request({
      url: '/api/dept/tree',
      method: 'get'
    })
  },

  // 获取部门列表
  list: (params?: { deptName?: string; status?: number }) => {
    return request({
      url: '/api/dept',
      method: 'get',
      params
    })
  },

  // 获取部门详情
  getDetail: (id: number) => {
    return request({
      url: `/api/dept/${id}`,
      method: 'get'
    })
  },

  // 创建部门
  create: (data: DepartmentCreateRequest) => {
    return request({
      url: '/api/dept',
      method: 'post',
      data
    })
  },

  // 更新部门
  update: (id: number, data: DepartmentUpdateRequest) => {
    return request({
      url: `/api/dept/${id}`,
      method: 'put',
      data
    })
  },

  // 删除部门
  delete: (id: number) => {
    return request({
      url: `/api/dept/${id}`,
      method: 'delete'
    })
  },

  // 获取子部门
  getChildren: (id: number) => {
    return request({
      url: `/api/dept/children/${id}`,
      method: 'get'
    })
  },

  // 移动部门
  move: (id: number, targetParentId: number) => {
    return request({
      url: `/api/dept/${id}/move/${targetParentId}`,
      method: 'put'
    })
  },

  // 检查部门名称是否可用
  checkDeptName: (deptName: string, parentId: number, excludeId?: number) => {
    return request({
      url: '/api/dept/check-name',
      method: 'get',
      params: { deptName, parentId, excludeId }
    })
  },

  // 启用部门
  enable: (id: number) => {
    return request({
      url: `/api/dept/${id}/enable`,
      method: 'put'
    })
  },

  // 禁用部门
  disable: (id: number) => {
    return request({
      url: `/api/dept/${id}/disable`,
      method: 'put'
    })
  },

  // 检查部门编码是否可用
  checkDeptCode: (deptCode: string, excludeId?: number) => {
    return request({
      url: '/api/dept/check-code',
      method: 'get',
      params: { deptCode, excludeId }
    })
  },

  // 获取部门统计信息
  getStats: () => {
    return request({
      url: '/api/dept/stats',
      method: 'get'
    })
  },

  // 获取部门下的用户列表
  getDeptUsers: (deptId: number, params?: {
    page?: number
    size?: number
    username?: string
    includeSubDepts?: boolean // 是否包含子部门
  }) => {
    return request({
      url: `/api/dept/${deptId}/users`,
      method: 'get',
      params
    })
  },

  // 批量移动用户到部门
  moveUsers: (targetDeptId: number, userIds: number[]) => {
    return request({
      url: `/api/dept/${targetDeptId}/move-users`,
      method: 'put',
      data: userIds
    })
  },

  // 复制部门结构
  copy: (id: number, data: {
    deptName: string
    deptCode?: string
    parentId: number
    copyUsers?: boolean // 是否复制用户
    copySubDepts?: boolean // 是否复制子部门
  }) => {
    return request({
      url: `/api/system/departments/${id}/copy`,
      method: 'post',
      data
    })
  },

  // 导出部门结构
  export: (rootDeptId?: number) => {
    return request({
      url: '/api/system/departments/export',
      method: 'get',
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
    return request({
      url: '/api/system/departments/import',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 获取部门路径
  getDeptPath: (id: number) => {
    return request({
      url: `/api/system/departments/${id}/path`,
      method: 'get'
    })
  },

  // 调整部门排序
  updateSort: (items: Array<{
    id: number
    orderNum: number
  }>) => {
    return request({
      url: '/api/system/departments/sort',
      method: 'put',
      data: items
    })
  },

  // 获取可选的父部门列表（排除自己和子部门）
  getSelectableParents: (excludeId?: number) => {
    return request({
      url: '/api/system/departments/selectable-parents',
      method: 'get',
      params: { excludeId }
    })
  }
} 