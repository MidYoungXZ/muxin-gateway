/**
 * API路径工具函数
 */

export function normalizePath(path: string): string {
  if (!path) return ''
  return path.replace(/^\/+|\/+$/g, '')
}

export function joinPath(...segments: string[]): string {
  const normalizedSegments = segments
    .filter(segment => segment && segment.trim())
    .map(segment => normalizePath(segment))
    .filter(segment => segment)
  return '/' + normalizedSegments.join('/')
}

export class ApiConfig {
  private static instance: ApiConfig
  private baseURL: string

  private constructor() {
    this.baseURL = ''
    console.log(`[ApiConfig] baseURL: "${this.baseURL}"`)
  }

  public static getInstance(): ApiConfig {
    if (!ApiConfig.instance) {
      ApiConfig.instance = new ApiConfig()
    }
    return ApiConfig.instance
  }

  public getApiUrl(path: string): string {
    let cleanPath = path.replace(/^\/+/, '')
    
    if (cleanPath.startsWith('api/')) {
      return '/' + cleanPath
    }
    
    if (!cleanPath.startsWith('api')) {
      return '/api/' + cleanPath
    }
    
    return '/' + cleanPath
  }

  public getBaseURL(): string {
    return this.baseURL
  }

  public setBaseURL(baseURL: string): void {
    this.baseURL = baseURL
  }
}

export const apiConfig = ApiConfig.getInstance()