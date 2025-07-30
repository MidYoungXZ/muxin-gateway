/**
 * 断言配置
 */
export interface Predicate {
  id: number
  predicateName: string
  predicateType: string
  predicateTypeDesc?: string
  description?: string
  config: Record<string, any>
  isSystem: boolean
  enabled: boolean
  createTime: string
  updateTime: string
  usageCount?: number
}

/**
 * 断言类型定义
 */
export interface PredicateType {
  type: string
  name: string
  description: string
  configFields: ConfigField[]
}

/**
 * 配置字段定义
 */
export interface ConfigField {
  field: string
  label: string
  type: 'string' | 'number' | 'boolean' | 'array' | 'datetime'
  required: boolean
  defaultValue?: any
  placeholder?: string
  description?: string
}

/**
 * 断言类型枚举
 */
export enum PredicateTypeEnum {
  PATH = 'Path',
  METHOD = 'Method',
  HEADER = 'Header',
  QUERY = 'Query',
  COOKIE = 'Cookie',
  HOST = 'Host',
  REMOTE_ADDR = 'RemoteAddr',
  BETWEEN = 'Between'
} 