// 分页结果类型
export interface PageResult<T> {
  total: number
  data: T[]
  page: number
  size: number
}

// 分页查询参数基类
export interface PageParams {
  page?: number
  size?: number
}

// 用户相关类型
export interface User {
  id: number
  username: string
  nickname: string
  email: string
  mobile: string
  avatar: string
  deptId: number
  deptName: string
  status: 0 | 1 // 0-禁用，1-启用
  createTime: string
  updateTime: string
  roles: Role[]
}

export interface UserCreateRequest {
  username: string
  password: string
  nickname: string
  email?: string
  mobile?: string
  avatar?: string
  deptId?: number
  status?: 0 | 1
  roleIds?: number[]
}

export interface UserUpdateRequest {
  nickname?: string
  email?: string
  mobile?: string
  avatar?: string
  deptId?: number
  status?: 0 | 1
}

export interface UserQueryParams extends PageParams {
  username?: string
  nickname?: string
  email?: string
  mobile?: string
  deptId?: number
  status?: 0 | 1
  startTime?: string
  endTime?: string
}

export interface PasswordUpdateRequest {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

// 角色相关类型
export interface Role {
  id: number
  roleCode: string
  roleName: string
  description: string
  status: 0 | 1
  createTime: string
  updateTime: string
  menuIds?: number[]
}

export interface RoleCreateRequest {
  roleCode: string
  roleName: string
  description?: string
  status?: 0 | 1
  menuIds?: number[]
}

export interface RoleUpdateRequest {
  roleName?: string
  description?: string
  status?: 0 | 1
  menuIds?: number[]
}

export interface RoleQueryParams extends PageParams {
  roleName?: string
  roleCode?: string
  status?: 0 | 1
}

// 部门相关类型
export interface Department {
  id: number
  parentId: number
  ancestors: string
  deptName: string
  deptCode: string
  leader: string
  phone: string
  email: string
  orderNum: number
  status: 0 | 1
  createTime: string
  updateTime: string
  children?: Department[]
}

export interface DepartmentTree extends Department {
  children?: DepartmentTree[]
}

export interface DepartmentCreateRequest {
  parentId: number
  deptName: string
  deptCode?: string
  leader?: string
  phone?: string
  email?: string
  orderNum?: number
  status?: 0 | 1
}

export interface DepartmentUpdateRequest {
  deptName?: string
  deptCode?: string
  leader?: string
  phone?: string
  email?: string
  orderNum?: number
  status?: 0 | 1
}

// 菜单相关类型
export interface Menu {
  id: number
  parentId: number
  menuName: string
  i18nCode: string
  menuType: 'M' | 'C' | 'F' // M-目录，C-菜单，F-按钮
  path: string
  component: string
  perms: string
  icon: string
  sortOrder: number
  visible: 0 | 1 // 0-隐藏，1-显示
  status: 0 | 1 // 0-禁用，1-启用
  createTime: string
  updateTime: string
  children?: Menu[]
}

export interface MenuTree extends Menu {
  children?: MenuTree[]
}

export interface MenuCreateRequest {
  parentId: number
  menuName: string
  i18nCode?: string
  menuType: 'M' | 'C' | 'F'
  path?: string
  component?: string
  perms?: string
  icon?: string
  sortOrder?: number
  visible?: 0 | 1
  status?: 0 | 1
}

export interface MenuUpdateRequest {
  parentId?: number
  menuName?: string
  i18nCode?: string
  menuType?: 'M' | 'C' | 'F'
  path?: string
  component?: string
  perms?: string
  icon?: string
  sortOrder?: number
  visible?: 0 | 1
  status?: 0 | 1
}

export interface MenuQueryParams extends PageParams {
  menuName?: string
  menuType?: 'M' | 'C' | 'F'
  status?: 0 | 1
  visible?: 0 | 1
  parentId?: number
  perms?: string
}

// 权限相关类型
export interface Permission {
  id: number
  code: string
  name: string
  type: 'menu' | 'button' | 'api'
  parentId: number
  path?: string
  component?: string
  icon?: string
  order: number
  visible: boolean
  cached: boolean
  resources: string[]
}

// 操作日志类型
export interface OperationLog {
  id: number
  module: string
  operation: string
  method: string
  requestUrl: string
  params: string
  result?: string
  error?: string
  duration: number
  operator: string
  operatorId: number
  operatorIp: string
  operatorLocation?: string
  browser?: string
  os?: string
  status: number // 0-失败，1-成功
  statusText: string
  operateTime: string
  // 兼容老字段
  userId?: number
  username?: string
  time?: number
  ip?: string
  location?: string
  errorMsg?: string
  createTime?: string
}

export interface OperationLogQueryParams extends PageParams {
  pageNum?: number
  pageSize?: number
  module?: string
  operation?: string
  operator?: string
  status?: number
  method?: string
  keyword?: string
  startTime?: string
  endTime?: string
  // 兼容老字段
  username?: string
}

// 系统配置类型
export interface SystemConfig {
  id: number
  configName: string
  configKey: string
  configValue: string
  configType: 'system' | 'user'
  description: string
  createTime: string
  updateTime: string
}

export interface SystemConfigUpdateRequest {
  configValue: string
  description?: string
}

// 数据权限类型
export interface DataPermission {
  userId: number
  deptIds: number[]
  roleIds: number[]
  permissions: string[]
}

// 在线用户类型
export interface OnlineUser {
  tokenId: string
  userId: number
  username: string
  nickname: string
  deptName: string
  ip: string
  location: string
  browser: string
  os: string
  loginTime: string
  lastActiveTime: string
}