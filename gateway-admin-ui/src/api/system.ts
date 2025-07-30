import request from '@/utils/request'

// 操作日志类型定义
export interface OperationLog {
  id: number
  module: string
  operation: string
  method: string
  params: string
  result: string
  error: string
  duration: number
  operator: string
  operatorIp: string
  operateTime: string
}

// 用户类型定义
export interface User {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  gender: number
  status: number
  deptId: number
  roles: Role[]
  createTime: string
  updateTime: string
}

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

// 权限类型定义
export interface Permission {
  id: number
  parentId: number
  permType: string
  permCode: string
  permName: string
  path: string
  component: string
  icon: string
  sort: number
  visible: boolean
  status: number
  children?: Permission[]
}

// 部门类型定义
export interface Department {
  id: number
  parentId: number
  deptName: string
  orderNum: number
  leader: string
  phone: string
  email: string
  status: number
  children?: Department[]
}

// 操作日志相关接口
export const getOperationLogs = (params: any) => {
  return request({
    url: '/api/system/logs/operation',
    method: 'get',
    params
  })
}

export const deleteOperationLogs = (ids: number[]) => {
  return request({
    url: '/api/system/logs/operation',
    method: 'delete',
    data: ids
  })
}

// 用户管理相关接口
export const getUsers = (params: any) => {
  return request({
    url: '/api/system/users',
    method: 'get',
    params
  })
}

export const getUser = (id: number) => {
  return request({
    url: `/api/system/users/${id}`,
    method: 'get'
  })
}

export const createUser = (data: any) => {
  return request({
    url: '/api/system/users',
    method: 'post',
    data
  })
}

export const updateUser = (id: number, data: any) => {
  return request({
    url: `/api/system/users/${id}`,
    method: 'put',
    data
  })
}

export const deleteUser = (id: number) => {
  return request({
    url: `/api/system/users/${id}`,
    method: 'delete'
  })
}

export const resetPassword = (id: number, password: string) => {
  return request({
    url: `/api/system/users/${id}/reset-password`,
    method: 'put',
    data: { password }
  })
}

// 角色管理相关接口
export const getRoles = (params?: any) => {
  return request({
    url: '/api/system/roles',
    method: 'get',
    params
  })
}

export const getRole = (id: number) => {
  return request({
    url: `/api/system/roles/${id}`,
    method: 'get'
  })
}

export const createRole = (data: any) => {
  return request({
    url: '/api/system/roles',
    method: 'post',
    data
  })
}

export const updateRole = (id: number, data: any) => {
  return request({
    url: `/api/system/roles/${id}`,
    method: 'put',
    data
  })
}

export const deleteRole = (id: number) => {
  return request({
    url: `/api/system/roles/${id}`,
    method: 'delete'
  })
}

export const updateRolePermissions = (roleId: number, permissionIds: number[]) => {
  return request({
    url: `/api/system/roles/${roleId}/permissions`,
    method: 'put',
    data: permissionIds
  })
}

// 权限管理相关接口
export const getPermissions = (params?: any) => {
  return request({
    url: '/api/system/permissions',
    method: 'get',
    params
  })
}

export const getPermission = (id: number) => {
  return request({
    url: `/api/system/permissions/${id}`,
    method: 'get'
  })
}

export const createPermission = (data: any) => {
  return request({
    url: '/api/system/permissions',
    method: 'post',
    data
  })
}

export const updatePermission = (id: number, data: any) => {
  return request({
    url: `/api/system/permissions/${id}`,
    method: 'put',
    data
  })
}

export const deletePermission = (id: number) => {
  return request({
    url: `/api/system/permissions/${id}`,
    method: 'delete'
  })
}

// 部门管理相关接口
export const getDepartments = (params?: any) => {
  return request({
    url: '/api/system/departments',
    method: 'get',
    params
  })
}

export const getDepartment = (id: number) => {
  return request({
    url: `/api/system/departments/${id}`,
    method: 'get'
  })
}

export const createDepartment = (data: any) => {
  return request({
    url: '/api/system/departments',
    method: 'post',
    data
  })
}

export const updateDepartment = (id: number, data: any) => {
  return request({
    url: `/api/system/departments/${id}`,
    method: 'put',
    data
  })
}

export const deleteDepartment = (id: number) => {
  return request({
    url: `/api/system/departments/${id}`,
    method: 'delete'
  })
}

// 系统配置相关接口
export const getSystemConfig = () => {
  return request({
    url: '/api/system/config',
    method: 'get'
  })
}

export const updateSystemConfig = (data: any) => {
  return request({
    url: '/api/system/config',
    method: 'put',
    data
  })
}

export const refreshConfig = () => {
  return request({
    url: '/api/system/config/refresh',
    method: 'post'
  })
} 