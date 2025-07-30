import axios, { AxiosInstance, AxiosError } from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { apiConfig } from './path'

// 创建axios实例，baseURL由ApiConfig智能管理
const request: AxiosInstance = axios.create({
  baseURL: apiConfig.getBaseURL(),
  timeout: 10000
})

// 输出axios配置用于调试
console.log(`[Axios] baseURL配置: "${apiConfig.getBaseURL()}"`)
console.log(`[Axios] 环境: ${import.meta.env.DEV ? '开发' : '生产'}`)

// 请求计数器，用于管理全局loading
let requestCount = 0
let loadingInstance: ReturnType<typeof ElLoading.service> | null = null

// 显示loading
const showLoading = () => {
  if (requestCount === 0) {
    loadingInstance = ElLoading.service({
      lock: true,
      text: '加载中...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
  }
  requestCount++
}

// 隐藏loading
const hideLoading = () => {
  requestCount--
  if (requestCount <= 0) {
    requestCount = 0
    loadingInstance?.close()
  }
}

// 错误消息防重复
const errorMessages = new Set<string>()
let errorTimer: ReturnType<typeof setTimeout> | null = null

const showErrorMessage = (message: string) => {
  if (errorMessages.has(message)) return
  
  errorMessages.add(message)
  ElMessage.error({
    message,
    duration: 3000,
    showClose: true,
    onClose: () => {
      errorMessages.delete(message)
    }
  })
  
  // 清理定时器
  if (errorTimer) clearTimeout(errorTimer)
  errorTimer = setTimeout(() => {
    errorMessages.clear()
  }, 5000)
}

// 请求拦截器
request.interceptors.request.use(
  async config => {
    const userStore = useUserStore()
    
    // 是否显示loading（默认显示，可通过config.showLoading=false关闭）
    if (config.showLoading !== false) {
      showLoading()
    }
    
    // 添加Token（登录接口除外）
    if (userStore.token && !config.url?.includes('/auth/login')) {
      config.headers.Authorization = `${userStore.tokenType} ${userStore.token}`
    }
    
    // 添加请求时间戳，防止缓存
    if (config.method?.toLowerCase() === 'get') {
      config.params = {
        ...config.params,
        _t: Date.now()
      }
    }
    
    return config
  },
  error => {
    hideLoading()
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    hideLoading()
    const res = response.data
    
    // 文件下载等二进制数据直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    // 正常响应
    if (res.code === 200) {
      return res
    }
    
    // 业务错误
    showErrorMessage(res.message || '操作失败')
    return Promise.reject(new Error(res.message || 'Error'))
  },
  async error => {
    hideLoading()
    const { response, config } = error
    const userStore = useUserStore()
    
    // 网络错误
    if (!response) {
      showErrorMessage('网络连接失败，请检查网络设置')
      return Promise.reject(error)
    }
    
    // HTTP状态码错误处理
    switch (response.status) {
      case 401:
        // 如果是登录接口，显示具体错误信息
        if (config.url?.includes('/auth/login')) {
          const message = response.data?.message || '用户名或密码错误'
          showErrorMessage(message)
        } else if (!config._retry && userStore.token) {
          // Token过期，尝试刷新
          config._retry = true
          
          try {
            await userStore.refreshUserToken()
            // 重新发送原请求
            return request(config)
          } catch {
            // 刷新失败，跳转登录
            showErrorMessage('登录已过期，请重新登录')
            await userStore.logout()
          }
        } else {
          showErrorMessage('未登录或登录已过期')
          await userStore.logout()
        }
        break
        
      case 403:
        showErrorMessage('抱歉，您没有权限执行此操作')
        break
        
      case 404:
        showErrorMessage('请求的资源不存在')
        break
        
      case 408:
        showErrorMessage('请求超时，请稍后重试')
        break
        
      case 429:
        showErrorMessage('请求过于频繁，请稍后再试')
        break
        
      case 500:
        showErrorMessage('服务器内部错误，请联系管理员')
        break
        
      case 502:
        showErrorMessage('网关错误，请稍后重试')
        break
        
      case 503:
        showErrorMessage('服务暂时不可用，请稍后重试')
        break
        
      case 504:
        showErrorMessage('网关超时，请稍后重试')
        break
        
      default:
        // 其他错误，显示详细信息
        const message = response.data?.message || `请求失败（${response.status}）`
        showErrorMessage(message)
    }
    
    return Promise.reject(error)
  }
)

// 导出请求方法
export const get = <T = any>(url: string, params?: any, config?: any) => {
  return request.get<T, T>(url, { params, ...config })
}

export const post = <T = any>(url: string, data?: any, config?: any) => {
  return request.post<T, T>(url, data, config)
}

export const put = <T = any>(url: string, data?: any, config?: any) => {
  return request.put<T, T>(url, data, config)
}

export const del = <T = any>(url: string, config?: any) => {
  return request.delete<T, T>(url, config)
}

// 扩展axios配置类型
declare module 'axios' {
  export interface AxiosRequestConfig {
    showLoading?: boolean
  }
}

export default request 