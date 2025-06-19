export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>('')
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
    token.value = 'mock-token'
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
    localStorage.setItem('user-info', JSON.stringify(userInfo.value))
  }
  
  // 获取用户信息
  const getUserInfoAction = async (): Promise<void> => {
    // 从 localStorage 恢复
    const storedToken = localStorage.getItem('user-token')
    const storedUserInfo = localStorage.getItem('user-info')
    
    if (storedToken && storedUserInfo) {
      token.value = storedToken
      userInfo.value = JSON.parse(storedUserInfo)
      permissions.value = userInfo.value.permissions || []
      roles.value = userInfo.value.roles || []
    }
  }
  
  // 登出
  const logoutAction = async (): Promise<void> => {
    // 清除状态
    token.value = ''
    userInfo.value = {}
    permissions.value = []
    roles.value = []
    
    // 清除 localStorage
    localStorage.removeItem('user-token')
    localStorage.removeItem('user-info')
  }
  
  // 检查权限
  const hasPermission = (permission: string): boolean => {
    return permissions.value.includes(permission) || permissions.value.includes('*:*:*')
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
    userInfo,
    permissions,
    roles,
    
    // 计算属性
    isLoggedIn,
    username,
    avatar,
    
    // 方法
    loginAction,
    getUserInfoAction,
    logoutAction,
    hasPermission,
    hasRole,
    hasAnyPermission,
    hasAnyRole,
    init
  }
}) 