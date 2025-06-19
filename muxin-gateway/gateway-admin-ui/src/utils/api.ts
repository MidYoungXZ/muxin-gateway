import request from './request'
import { apiConfig } from './path'

/**
 * 统一的API请求函数
 * 自动处理路径拼接，避免重复
 */

/**
 * GET请求
 */
export function apiGet<T = any>(path: string, params?: any): Promise<T> {
  const url = apiConfig.getApiUrl(path)
  return request.get(url, { params })
}

/**
 * POST请求
 */
export function apiPost<T = any>(path: string, data?: any, config?: any): Promise<T> {
  const url = apiConfig.getApiUrl(path)
  return request.post(url, data, config)
}

/**
 * PUT请求
 */
export function apiPut<T = any>(path: string, data?: any, config?: any): Promise<T> {
  const url = apiConfig.getApiUrl(path)
  return request.put(url, data, config)
}

/**
 * DELETE请求
 */
export function apiDelete<T = any>(path: string, config?: any): Promise<T> {
  const url = apiConfig.getApiUrl(path)
  return request.delete(url, config)
}

/**
 * PATCH请求
 */
export function apiPatch<T = any>(path: string, data?: any, config?: any): Promise<T> {
  const url = apiConfig.getApiUrl(path)
  return request.patch(url, data, config)
}

/**
 * 直接使用完整URL的请求（用于特殊情况）
 */
export function apiRequest<T = any>(config: any): Promise<T> {
  return request(config)
}

/**
 * 获取完整的API URL（用于调试）
 */
export function getApiUrl(path: string): string {
  return apiConfig.getApiUrl(path)
}

/**
 * 调试函数：打印API URL
 */
export function debugApiUrl(path: string): void {
  const fullUrl = apiConfig.getApiUrl(path)
  const baseURL = apiConfig.getBaseURL()
  console.log(`[API Debug] 输入路径: "${path}"`)
  console.log(`[API Debug] baseURL: "${baseURL}"`)
  console.log(`[API Debug] 最终URL: "${fullUrl}"`)
  console.log(`[API Debug] 当前环境: ${import.meta.env.DEV ? '开发' : '生产'}`)
  console.log('---')
} 