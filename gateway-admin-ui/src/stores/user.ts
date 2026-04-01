import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'

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
  const token = ref<string>('')
  const tokenType = ref<string>('Bearer')
  const userInfo = ref<Partial<UserInfo>>({})
  const permissions = ref<string[]>([])
  const roles = ref<string[]>([])
  
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value.username || '')
  const avatar = computed(() => userInfo.value.avatar || '')
  
  const loginAction = async (loginForm: { username: string; password: string }): Promise<void> => {
    const response = await authApi.login({
      username: loginForm.username,
      password: loginForm.password
    })
    
    if (response.code === 200 && response.data) {
      const { accessToken, tokenType: type, userInfo: info } = response.data
      
      token.value = accessToken || ''
      tokenType.value = type || 'Bearer'
      userInfo.value = info || {}
      permissions.value = info?.permissions || []
      roles.value = info?.roles || []
      
      localStorage.setItem('user-token', token.value)
      localStorage.setItem('user-token-type', tokenType.value)
      localStorage.setItem('user-info', JSON.stringify(userInfo.value))
    } else {
      throw new Error(response.message || '登录失败')
    }
  }
  
  const getUserInfoAction = async (): Promise<void> => {
    const storedToken = localStorage.getItem('user-token')
    const storedTokenType = localStorage.getItem('user-token-type')
    const storedUserInfo = localStorage.getItem('user-info')
    
    if (storedToken) {
      token.value = storedToken
      tokenType.value = storedTokenType || 'Bearer'
      if (storedUserInfo) {
        try {
          userInfo.value = JSON.parse(storedUserInfo)
          permissions.value = userInfo.value.permissions || []
          roles.value = userInfo.value.roles || []
        } catch {
          clearAuth()
        }
      }
    }
  }

  const refreshUserToken = async (): Promise<void> => {
    try {
      await authApi.refreshToken()
    } catch {
      await logout()
      throw new Error('Token refresh failed')
    }
  }

  const logout = async (): Promise<void> => {
    try {
      await authApi.logout()
    } catch {
      // ignore
    }
    
    clearAuth()
  }
  
  const clearAuth = () => {
    token.value = ''
    tokenType.value = 'Bearer'
    userInfo.value = {}
    permissions.value = []
    roles.value = []
    
    localStorage.removeItem('user-token')
    localStorage.removeItem('user-token-type')
    localStorage.removeItem('user-info')
  }
  
  const hasPermission = (permission: string): boolean => {
    if (permissions.value.includes('*:*:*')) return true
    return permissions.value.includes(permission)
  }
  
  const hasRole = (role: string): boolean => {
    return roles.value.includes(role)
  }
  
  const hasAnyPermission = (permissionList: string[]): boolean => {
    return permissionList.some(permission => hasPermission(permission))
  }
  
  const hasAnyRole = (roleList: string[]): boolean => {
    return roleList.some(role => hasRole(role))
  }
  
  const init = async (): Promise<void> => {
    await getUserInfoAction()
  }
  
  return {
    token,
    tokenType,
    userInfo,
    user: userInfo,
    permissions,
    roles,
    isLoggedIn,
    username,
    avatar,
    loginAction,
    getUserInfoAction,
    refreshUserToken,
    logout,
    clearAuth,
    hasPermission,
    hasRole,
    hasAnyPermission,
    hasAnyRole,
    init
  }
})