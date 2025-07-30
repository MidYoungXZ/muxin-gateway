import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LoginResponse } from '@/types/auth'

// 简化的用户信息类型
interface UserInfo {
  id?: number
  username?: string
  nickname?: string
  email?: string
  avatar?: string
  roles?: string[]
  permissions?: string[]
}

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>('')
  const tokenType = ref<string>('Bearer')
  const userInfo = ref<Partial<UserInfo>>({})
  const permissions = ref<string[]>([])
  const roles = ref<string[]>([])
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value.username || '')
  const avatar = computed(() => userInfo.value.avatar || '')
  
  // 登录
  const loginAction = async (loginForm: any): Promise<void> => {
    // 临时实现 - 后续需要连接真实 API
    token.value = 'mock-token-' + Date.now()
    tokenType.value = 'Bearer'
    userInfo.value = {
      id: 1,
      username: loginForm.username,
      nickname: 'Admin User',
      roles: ['admin'],
      permissions: ['*:*:*']
    }
    permissions.value = ['*:*:*']
    roles.value = ['admin']
    
    // 存储到 localStorage
    localStorage.setItem('user-token', token.value)
    localStorage.setItem('user-token-type', tokenType.value)
    localStorage.setItem('user-info', JSON.stringify(userInfo.value))
  }
  
  // 获取用户信息方法
  const getUserInfoAction = async (): Promise<void> => {
    // 从 localStorage 恢复
    const storedToken = localStorage.getItem('user-token')
    const storedTokenType = localStorage.getItem('user-token-type')
    const storedUserInfo = localStorage.getItem('user-info')
    
    if (storedToken && storedUserInfo) {
      token.value = storedToken
      tokenType.value = storedTokenType || 'Bearer'
      try {
        userInfo.value = JSON.parse(storedUserInfo)
        permissions.value = userInfo.value.permissions || []
        roles.value = userInfo.value.roles || []
      } catch (error) {
        console.error('解析用户信息失败:', error)
        // 清除无效数据
        localStorage.removeItem('user-token')
        localStorage.removeItem('user-token-type')
        localStorage.removeItem('user-info')
      }
    }
  }

  // 刷新Token
  const refreshUserToken = async (): Promise<void> => {
    // TODO: 实现token刷新逻辑
    console.log('刷新token...')
  }

  // 登出
  const logout = async (): Promise<void> => {
    // 清除状态
    token.value = ''
    tokenType.value = 'Bearer'
    userInfo.value = {}
    permissions.value = []
    roles.value = []
    
    // 清除 localStorage
    localStorage.removeItem('user-token')
    localStorage.removeItem('user-token-type')
    localStorage.removeItem('user-info')
  }
  
  // 检查权限
  const hasPermission = (permission: string): boolean => {
    // 简化的权限检查，实际项目中应该检查用户的permissions数组
    // 这里临时返回true，让所有元素都显示
    return true
  }
  
  // 检查角色
  const hasRole = (role: string): boolean => {
    return roles.value.includes(role)
  }
  
  // 检查多个权限（任意一个）
  const hasAnyPermission = (permissionList: string[]): boolean => {
    return permissionList.some(permission => hasPermission(permission))
  }
  
  // 检查多个角色（任意一个）
  const hasAnyRole = (roleList: string[]): boolean => {
    return roleList.some(role => roles.value.includes(role))
  }
  
  // 初始化
  const init = async (): Promise<void> => {
    await getUserInfoAction()
  }
  
  return {
    // 状态
    token,
    tokenType,
    userInfo,
    user: userInfo, // 别名，保持向后兼容
    permissions,
    roles,
    
    // 计算属性
    isLoggedIn,
    username,
    avatar,
    
    // 方法
    loginAction,
    getUserInfoAction,
    refreshUserToken,
    logout,
    hasPermission,
    hasRole,
    hasAnyPermission,
    hasAnyRole,
    init
  }
}) 