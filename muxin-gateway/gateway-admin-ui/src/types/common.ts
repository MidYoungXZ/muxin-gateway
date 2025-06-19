// 统一响应格式
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 分页请求参数
export interface PageParams {
  page: number
  size: number
  sort?: string
  order?: 'asc' | 'desc'
}

// 分页响应数据
export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

// 树形结构
export interface TreeNode<T = any> {
  id: number | string
  label: string
  value: number | string
  children?: TreeNode<T>[]
  data?: T
}

// 选项结构
export interface Option {
  label: string
  value: string | number
  disabled?: boolean
} 