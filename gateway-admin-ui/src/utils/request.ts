import axios, { AxiosInstance, AxiosError, AxiosRequestConfig } from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useMenuStore } from '@/stores/menu'

declare module 'axios' {
  export interface AxiosRequestConfig {
    showLoading?: boolean
    showError?: boolean
  }
}

const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000
})

let requestCount = 0
let loadingInstance: ReturnType<typeof ElLoading.service> | null = null

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

const hideLoading = () => {
  requestCount--
  if (requestCount <= 0) {
    requestCount = 0
    loadingInstance?.close()
  }
}

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
  
  if (errorTimer) clearTimeout(errorTimer)
  errorTimer = setTimeout(() => {
    errorMessages.clear()
  }, 5000)
}

request.interceptors.request.use(
  async config => {
    const userStore = useUserStore()
    
    if (config.showLoading !== false) {
      showLoading()
    }
    
    const token = userStore.token || localStorage.getItem('user-token')
    const tokenType = userStore.tokenType || localStorage.getItem('user-token-type') || 'Bearer'
    
    if (token && !config.url?.includes('/auth/login')) {
      config.headers.Authorization = `${tokenType} ${token}`
    }
    
    if (config.method?.toLowerCase() === 'get') {
      config.params = {
        ...config.params,
        _t: Date.now()
      }
    }
    
    if (config.url) {
      config.url = config.url.replace(/^\/api\/?/, '/')
    }
    
    return config
  },
  error => {
    hideLoading()
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    hideLoading()
    const res = response.data
    
    if (response.config.responseType === 'blob') {
      return response
    }
    
    if (res.code === 200) {
      return res
    }
    
    if (response.config.showError !== false) {
      showErrorMessage(res.message || '操作失败')
    }
    return Promise.reject(new Error(res.message || 'Error'))
  },
  async error => {
    hideLoading()
    const { response, config } = error
    const userStore = useUserStore()
    const menuStore = useMenuStore()
    
    if (!response) {
      if (config.showError !== false) {
        showErrorMessage('网络连接失败，请检查网络设置')
      }
      return Promise.reject(error)
    }
    
    switch (response.status) {
      case 401:
        if (config.url?.includes('/auth/login')) {
          const message = response.data?.message || '用户名或密码错误'
          if (config.showError !== false) {
            showErrorMessage(message)
          }
        } else if (!config._retry && userStore.token) {
          config._retry = true
          try {
            await userStore.refreshUserToken()
            return request(config)
          } catch {
            showErrorMessage('登录已过期，请重新登录')
            menuStore.clearMenus()
            await userStore.logout()
          }
        } else {
          menuStore.clearMenus()
          userStore.clearAuth()
        }
        break
        
      case 403:
        if (config.showError !== false) {
          showErrorMessage('抱歉，您没有权限执行此操作')
        }
        break
        
      case 404:
        if (config.showError !== false) {
          showErrorMessage('请求的资源不存在')
        }
        break
        
      default:
        if (config.showError !== false) {
          const message = response.data?.message || `请求失败（${response.status}）`
          showErrorMessage(message)
        }
    }
    
    return Promise.reject(error)
  }
)

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

export default request