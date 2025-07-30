import { useUserStore } from '@/stores/user'

// 检查是否有权限
export function hasPermission(permissionCode: string): boolean {
  const userStore = useUserStore()
  return userStore.permissions.some((p: any) => p.code === permissionCode)
}

// 检查是否有任一权限
export function hasAnyPermission(permissionCodes: string[]): boolean {
  return permissionCodes.some(code => hasPermission(code))
}

// 检查是否有所有权限
export function hasAllPermissions(permissionCodes: string[]): boolean {
  return permissionCodes.every(code => hasPermission(code))
}

// Token管理
export class TokenManager {
  private refreshing = false
  
  // 获取Token
  getToken(): string | null {
    return localStorage.getItem('token')
  }
  
  // 刷新Token
  async refreshToken(): Promise<void> {
    if (this.refreshing) return
    
    this.refreshing = true
    try {
      const userStore = useUserStore()
      await userStore.refreshUserToken()
    } finally {
      this.refreshing = false
    }
  }
  
  // 检查Token是否过期
  isTokenExpired(): boolean {
    const token = this.getToken()
    if (!token) return true
    
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      return payload.exp * 1000 < Date.now()
    } catch {
      return true
    }
  }
}

export const tokenManager = new TokenManager() 