import request from '@/utils/request'
import type { LoginForm, RegisterForm, LoginResponse, ApiResponse } from '@/types/auth'

export const authApi = {
  async login(data: LoginForm): Promise<ApiResponse<LoginResponse>> {
    console.log('🚀 [LOGIN] 正在请求: /api/auth/login')
    try {
      const response = await request.post<ApiResponse<LoginResponse>>('/api/auth/login', data)
      console.log('✅ [LOGIN] 登录响应:', response)
      return response as any
    } catch (error: any) {
      console.error('❌ [LOGIN] 登录失败:', error.response?.data || error.message)
      throw error
    }
  },

  async logout() {
    console.log('🚀 [LOGOUT] 正在请求: /api/auth/logout')
    try {
      const response = await request.post('/api/auth/logout')
      console.log('✅ [LOGOUT] 登出成功')
      return response
    } catch (error: any) {
      console.error('❌ [LOGOUT] 登出失败:', error)
      throw error
    }
  },

  async refreshToken(): Promise<ApiResponse<LoginResponse>> {
    try {
      const response = await request.post<ApiResponse<LoginResponse>>('/api/auth/refresh-token')
      return response as any
    } catch (error: any) {
      console.error('❌ [REFRESH] 刷新Token失败:', error)
      throw error
    }
  },

  async getUserInfo(): Promise<ApiResponse<any>> {
    try {
      const response = await request.get<ApiResponse<any>>('/api/auth/user-info')
      return response as any
    } catch (error: any) {
      console.error('❌ [USER_INFO] 获取用户信息失败:', error)
      throw error
    }
  },

  async getCaptcha() {
    return Promise.resolve({
      code: 200,
      message: 'success',
      data: {
        captchaId: 'mock-captcha-id',
        captchaImage: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjQwIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjx0ZXh0IHg9IjEwIiB5PSIzMCIgZm9udC1zaXplPSIyMCI+QUJDREU8L3RleHQ+PC9zdmc+'
      }
    })
  },

  checkUsername(username: string) {
    return Promise.resolve({
      code: 200,
      message: 'success',
      data: username !== 'admin'
    })
  },

  sendCode(type: 'email' | 'phone', target: string) {
    return Promise.resolve({
      code: 200,
      message: '验证码已发送',
      data: true
    })
  },

  resetPassword(data: {
    account: string
    verifyCode: string
    newPassword: string
  }) {
    return Promise.resolve({
      code: 200,
      message: '密码重置成功',
      data: true
    })
  }
}