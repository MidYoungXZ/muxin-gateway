/**
 * API路径工具函数
 * 智能处理路径拼接，避免重复
 */

/**
 * 规范化路径，移除开头和结尾的斜杠
 */
export function normalizePath(path: string): string {
  if (!path) return ''
  return path.replace(/^\/+|\/+$/g, '')
}

/**
 * 连接路径片段，确保路径正确且不重复
 */
export function joinPath(...segments: string[]): string {
  // 过滤空值并规范化每个片段
  const normalizedSegments = segments
    .filter(segment => segment && segment.trim())
    .map(segment => normalizePath(segment))
    .filter(segment => segment)

  // 拼接路径
  return '/' + normalizedSegments.join('/')
}

/**
 * 智能创建API路径
 * 自动处理baseURL和path的拼接，避免重复
 */
export function createApiPath(baseURL: string, path: string): string {
  const normalizedBase = normalizePath(baseURL)
  const normalizedPath = normalizePath(path)
  
  // 检查是否有重复的 api 片段
  const baseSegments = normalizedBase.split('/').filter(s => s)
  const pathSegments = normalizedPath.split('/').filter(s => s)
  
  // 如果 baseURL 已经包含 api，且 path 也以 api 开头，则去除 path 中的 api
  if (baseSegments.includes('api') && pathSegments[0] === 'api') {
    pathSegments.shift() // 移除第一个 'api'
  }
  
  // 重新组合路径
  const allSegments = [...baseSegments, ...pathSegments]
  return '/' + allSegments.join('/')
}

/**
 * API配置类
 * 统一管理API路径配置
 */
export class ApiConfig {
  private static instance: ApiConfig
  private baseURL: string
  private apiPrefix: string

  private constructor() {
    // 强制开发环境配置，彻底解决路径重复问题
    const isDev = import.meta.env.DEV
    
    if (isDev) {
      // 开发环境：baseURL必须为空，让Vite代理处理
      this.baseURL = ''
      this.apiPrefix = '/api'
    } else {
      // 生产环境
      this.baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
      this.apiPrefix = '/api'
    }
    
    console.log(`[ApiConfig] 开发环境: ${isDev}, baseURL: "${this.baseURL}", apiPrefix: "${this.apiPrefix}"`)
  }

  public static getInstance(): ApiConfig {
    if (!ApiConfig.instance) {
      ApiConfig.instance = new ApiConfig()
    }
    return ApiConfig.instance
  }

  /**
   * 获取完整的API URL
   */
  public getApiUrl(path: string): string {
    // 移除path开头的斜杠
    const cleanPath = path.replace(/^\/+/, '')
    
    // 如果baseURL为空（开发环境），直接返回带api前缀的路径
    if (!this.baseURL) {
      return joinPath(this.apiPrefix, cleanPath)
    }
    
    // 检查baseURL是否已经包含api前缀
    const baseUrlLower = this.baseURL.toLowerCase()
    const hasApiInBase = baseUrlLower.includes('/api')
    
    if (hasApiInBase) {
      // baseURL已包含api，直接拼接
      return joinPath(this.baseURL, cleanPath)
    } else {
      // baseURL不包含api，添加api前缀
      return joinPath(this.baseURL, this.apiPrefix, cleanPath)
    }
  }

  /**
   * 获取基础URL
   */
  public getBaseURL(): string {
    return this.baseURL
  }

  /**
   * 设置基础URL（用于动态配置）
   */
  public setBaseURL(baseURL: string): void {
    this.baseURL = baseURL
  }
}

// 导出单例实例
export const apiConfig = ApiConfig.getInstance() 