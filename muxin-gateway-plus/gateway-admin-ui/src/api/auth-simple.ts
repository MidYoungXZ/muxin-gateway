import axios from 'axios'
import type { LoginForm, LoginResponse } from '@/types/auth'

// 终极简化版本 - 直接解决问题
const API_BASE = '/api'

export const authApiSimple = {
  async login(data: LoginForm) {
    try {
      console.log('🚀 直接登录请求:', `${API_BASE}/auth/login`)
      const response = await axios.post(`${API_BASE}/auth/login`, data)
      console.log('✅ 登录成功:', response.data)
      return response.data
    } catch (error: any) {
      console.error('❌ 登录失败:', error.response?.data || error.message)
      throw error
    }
  },

  async logout() {
    try {
      const response = await axios.post(`${API_BASE}/auth/logout`)
      return response.data
    } catch (error: any) {
      console.error('❌ 登出失败:', error)
      throw error
    }
  }
} 