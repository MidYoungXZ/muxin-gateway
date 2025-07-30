import axios from 'axios'
import type { LoginForm, RegisterForm, LoginResponse, ApiResponse } from '@/types/auth'

export const authApi = {
  // 登录
  async login(data: LoginForm): Promise<ApiResponse<LoginResponse>> {
    console.log('🚀 [LOGIN] 正在请求: /api/auth/login')
    try {
      const response = await axios.post<ApiResponse<LoginResponse>>('/api/auth/login', data)
      console.log('✅ [LOGIN] 登录响应:', response.data)
      return response.data
    } catch (error: any) {
      console.error('❌ [LOGIN] 登录失败:', error.response?.data || error.message)
      throw error
    }
  },

  // 注册 - 后端暂未实现
  register(data: RegisterForm) {
    // 模拟数据
    return Promise.resolve({
      code: 200,
      message: '注册成功',
      data: {
        id: Date.now(),
        username: data.username
      }
    })
  },

  // 退出登录
  async logout() {
    console.log('🚀 [LOGOUT] 正在请求: /api/auth/logout')
    try {
      const response = await axios.post('/api/auth/logout')
      console.log('✅ [LOGOUT] 登出成功')
      return response.data
    } catch (error: any) {
      console.error('❌ [LOGOUT] 登出失败:', error)
      throw error
    }
  },

  // 刷新Token
  async refreshToken(): Promise<ApiResponse<LoginResponse>> {
    try {
      const response = await axios.post<ApiResponse<LoginResponse>>('/api/auth/refresh-token')
      return response.data
    } catch (error: any) {
      console.error('❌ [REFRESH] 刷新Token失败:', error)
      throw error
    }
  },

  // 获取用户信息
  async getUserInfo(): Promise<ApiResponse<any>> {
    try {
      const response = await axios.get<ApiResponse<any>>('/api/auth/user-info')
      return response.data
    } catch (error: any) {
      console.error('❌ [USER_INFO] 获取用户信息失败:', error)
      throw error
    }
  },

  // 获取验证码 - 后端暂未实现
  async getCaptcha() {
    // 模拟数据
    return Promise.resolve({
      code: 200,
      message: 'success',
      data: {
        captchaId: 'mock-captcha-id',
        captchaImage: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjQwIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjx0ZXh0IHg9IjEwIiB5PSIzMCIgZm9udC1zaXplPSIyMCI+QUJDREU8L3RleHQ+PC9zdmc+'
      }
    })
  },

  // 检查用户名是否可用 - 后端暂未实现
  checkUsername(username: string) {
    // 模拟数据
    return Promise.resolve({
      code: 200,
      message: 'success',
      data: username !== 'admin' // admin已被占用
    })
  },

  // 发送验证码 - 后端暂未实现
  sendCode(type: 'email' | 'phone', target: string) {
    // 模拟数据
    return Promise.resolve({
      code: 200,
      message: '验证码已发送',
      data: true
    })
  },

  // 重置密码 - 后端暂未实现
  resetPassword(data: {
    account: string
    verifyCode: string
    newPassword: string
  }) {
    // 模拟数据
    return Promise.resolve({
      code: 200,
      message: '密码重置成功',
      data: true
    })
  }
}