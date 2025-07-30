// 通用API响应结构
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp?: number
}

export interface LoginForm {
  username: string
  password: string
  captcha?: string
  captchaKey?: string
  rememberMe?: boolean
}

export interface RegisterForm {
  username: string
  password: string
  confirmPassword: string
  email: string
  phone?: string
  captcha: string
  emailCode?: string
  agreement: boolean
}

export interface LoginResponse {
  token: string
  tokenType: string
  expiresIn: number
  userInfo: {
    id: number
    username: string
    nickname: string
    email?: string
    phone?: string
    avatar?: string
    deptId?: number
    deptName?: string
    roles: string[]
    permissions: string[]
  }
}

export interface User {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  roles: Role[]
  department: Department
  status: 'active' | 'inactive' | 'locked'
  lastLoginTime: Date
  createdTime: Date
}

export interface Role {
  id: number
  code: string
  name: string
  description: string
  permissions: Permission[]
  status: boolean
  createdTime: Date
}

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

export interface Department {
  id: number
  name: string
  parentId: number
  order: number
  leader: string
  phone: string
  email: string
  status: boolean
  children?: Department[]
}

export interface AuthRule {
  id: number
  name: string
  description: string
  type: 'JWT' | 'OAuth2' | 'Basic' | 'ApiKey' | 'Custom'
  routePatterns: string[]
  config: {
    jwtSecret?: string
    jwtHeader?: string
    jwtClaims?: string[]
    authorizationUri?: string
    tokenUri?: string
    clientId?: string
    apiKeyHeader?: string
    apiKeyParam?: string
    customScript?: string
  }
  whitelist: {
    ips: string[]
    tokens: string[]
    headers: Record<string, string>
  }
  blacklist: {
    ips: string[]
    tokens: string[]
  }
  priority: number
  enabled: boolean
} 