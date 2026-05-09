import request from '@/utils/request'
import type { LoginForm, RegisterForm, LoginResponse, ApiResponse } from '@/types/auth'

export const authApi = {
  async login(data: LoginForm): Promise<ApiResponse<LoginResponse>> {
    return request.post<ApiResponse<LoginResponse>>('/api/auth/login', data) as any
  },

  async logout() {
    return request.post('/api/auth/logout')
  },

  async refreshToken(): Promise<ApiResponse<LoginResponse>> {
    return request.post<ApiResponse<LoginResponse>>('/api/auth/refresh-token') as any
  },

  async getUserInfo(): Promise<ApiResponse<any>> {
    return request.get<ApiResponse<any>>('/api/auth/user-info') as any
  },

  async getCaptcha() {
    return request.get('/auth/captcha')
  },

  async checkUsername(username: string) {
    return request.get('/auth/check-username', { params: { username } })
  },

  async sendCode(type: 'email' | 'phone', target: string) {
    return request.post('/auth/send-code', { type, target })
  },

  async resetPassword(data: {
    account: string
    verifyCode: string
    newPassword: string
  }) {
    return request.post('/auth/reset-password', data)
  }
}