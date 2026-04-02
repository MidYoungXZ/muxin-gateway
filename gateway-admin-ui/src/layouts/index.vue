<template>
  <el-container class="layout-container">
    <!-- 现代化侧边栏 -->
    <el-aside :width="isCollapse ? '72px' : '280px'" class="layout-aside">
      <div class="sidebar-content">
        <!-- Logo区域 -->
        <div class="logo-section">
          <logo :logo-only="isCollapse" />
          <div v-if="!isCollapse" class="logo-subtitle">
            智能网关管理平台
          </div>
        </div>
        
        <!-- 导航菜单 -->
        <div class="nav-section">
          <el-scrollbar class="menu-scrollbar">
            <el-menu
              :default-active="activeMenu"
              :collapse="isCollapse"
              :unique-opened="false"
              :collapse-transition="false"
              mode="vertical"
              router
              class="sidebar-menu"
              @select="handleMenuSelect"
            >
              <sidebar-item
                v-for="route in menuRoutes"
                :key="route.path || route.name"
                :item="route"
                :base-path="''"
              />
            </el-menu>
          </el-scrollbar>
        </div>
      </div>
    </el-aside>
    
    <el-container>
      <!-- 现代化顶部栏 -->
      <el-header class="layout-header">
        <div class="header-left">
          <!-- 折叠按钮 -->
          <el-button
            text
            class="collapse-btn"
            @click="toggleCollapse"
          >
            <el-icon :size="20">
              <component :is="isCollapse ? 'Expand' : 'Fold'" />
            </el-icon>
          </el-button>
          
          <!-- 面包屑导航 -->
          <breadcrumb class="header-breadcrumb" />
        </div>
        
        <div class="header-center">
          <!-- 全局搜索 -->
          <div class="global-search">
            <el-input
              v-model="searchQuery"
              placeholder="搜索功能、页面或文档 (Ctrl+K)"
              prefix-icon="Search"
              class="search-input"
              clearable
              @keyup.enter="handleSearch"
              @focus="showSearchPanel = true"
            />
            <!-- 搜索建议面板 -->
            <div v-if="showSearchPanel && searchSuggestions.length" class="search-panel">
              <div class="search-suggestions">
                <div
                  v-for="suggestion in searchSuggestions"
                  :key="suggestion.id"
                  class="suggestion-item"
                  @click="handleSuggestionClick(suggestion)"
                >
                  <el-icon class="suggestion-icon">
                    <component :is="suggestion.icon" />
                  </el-icon>
                  <span class="suggestion-text">{{ suggestion.text }}</span>
                  <span class="suggestion-category">{{ suggestion.category }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="header-right">
          <!-- 通知中心 -->
          <el-badge :value="unreadNotifications" :hidden="unreadNotifications === 0" class="notification-badge">
            <el-button text class="header-action-btn" @click="showNotifications = true">
              <el-icon><Bell /></el-icon>
            </el-button>
          </el-badge>
          
          <!-- 主题切换 -->
          <theme-toggle />
          
          <!-- 用户信息 -->
          <el-dropdown trigger="click" placement="bottom-end">
            <div class="user-dropdown-trigger">
              <el-avatar :src="userStore.user?.avatar" :size="32" class="header-avatar">
                {{ userStore.user?.nickname?.charAt(0) || 'A' }}
              </el-avatar>
              <span class="user-name-header">{{ userStore.user?.nickname || '管理员' }}</span>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleProfile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 主内容区 -->
      <el-main class="layout-main">
        <!-- 页面标签栏 -->
        <div class="page-tabs">
          <div class="tabs-wrapper">
            <div
              v-for="tab in openTabs"
              :key="tab.name"
              class="tab-item"
              :class="{ active: activeTab === tab.name }"
              @click="handleTabItemClick(tab)"
              @contextmenu.prevent="openContextMenu($event, tab)"
            >
              <span class="tab-title">{{ tab.title }}</span>
              <el-icon
                v-if="openTabs.length > 1"
                class="tab-close"
                @click.stop="removeTab(tab.name)"
              >
                <Close />
              </el-icon>
            </div>
          </div>
        </div>
        
        <!-- 右键菜单 -->
        <teleport to="body">
          <div
            v-if="contextMenuVisible"
            class="context-menu"
            :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }"
          >
            <div class="context-menu-item" @click="refreshTab">
              <el-icon><RefreshRight /></el-icon>
              <span>刷新当前页面</span>
            </div>
            <div
              v-if="openTabs.length > 1"
              class="context-menu-item"
              @click="closeCurrentTab"
            >
              <el-icon><Close /></el-icon>
              <span>关闭当前标签</span>
            </div>
            <div class="context-menu-divider"></div>
            <div
              v-if="openTabs.length > 1"
              class="context-menu-item"
              @click="closeOtherTabs"
            >
              <el-icon><FolderRemove /></el-icon>
              <span>关闭其他标签</span>
            </div>
            <div
              v-if="currentTabIndex > 0"
              class="context-menu-item"
              @click="closeLeftTabs"
            >
              <el-icon><Back /></el-icon>
              <span>关闭左侧标签</span>
            </div>
            <div
              v-if="currentTabIndex < openTabs.length - 1"
              class="context-menu-item"
              @click="closeRightTabs"
            >
              <el-icon><Right /></el-icon>
              <span>关闭右侧标签</span>
            </div>
            <div class="context-menu-divider"></div>
            <div
              v-if="openTabs.length > 1"
              class="context-menu-item"
              @click="closeAllTabs"
            >
              <el-icon><CircleClose /></el-icon>
              <span>关闭所有标签</span>
            </div>
          </div>
        </teleport>
        
        <!-- 路由视图 -->
        <div class="content-wrapper">
          <router-view v-slot="{ Component }">
            <transition name="page" mode="out-in">
              <keep-alive :include="cachedViews">
                <component :is="Component" :key="routerViewKey" />
              </keep-alive>
            </transition>
          </router-view>
        </div>
      </el-main>
    </el-container>
    
    <!-- 通知抽屉 -->
    <el-drawer
      v-model="showNotifications"
      title="通知中心"
      direction="rtl"
      size="360px"
      class="notification-drawer"
    >
      <div class="notifications-content">
        <div class="notification-tabs">
          <el-button
            v-for="tab in notificationTabs"
            :key="tab.key"
            :type="activeNotificationTab === tab.key ? 'primary' : 'default'"
            text
            @click="activeNotificationTab = tab.key"
          >
            {{ tab.label }}
            <el-badge v-if="tab.count" :value="tab.count" />
          </el-button>
        </div>
        
        <div class="notification-list">
          <div
            v-for="notification in filteredNotifications"
            :key="notification.id"
            class="notification-item"
            :class="{ unread: !notification.read }"
            @click="markAsRead(notification.id)"
          >
            <div class="notification-icon" :class="notification.type">
              <el-icon>
                <component :is="notification.icon" />
              </el-icon>
            </div>
            <div class="notification-content">
              <div class="notification-title">{{ notification.title }}</div>
              <div class="notification-desc">{{ notification.description }}</div>
              <div class="notification-time">{{ formatTime(notification.time) }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </el-container>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useMenuStore, type MenuItem } from '@/stores/menu'
import { ElMessageBox } from 'element-plus'
import SidebarItem from './components/SidebarItem.vue'
import Breadcrumb from './components/Breadcrumb.vue'
import ThemeToggle from '@/components/ThemeToggle.vue'
import Logo from '@/components/Logo.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const menuStore = useMenuStore()

interface TabItem {
  name: string
  title: string
  path: string
}

// 响应式数据
const isCollapse = ref(false)
const cachedViews = ref<string[]>([])
const searchQuery = ref('')
const showSearchPanel = ref(false)
const showNotifications = ref(false)
const unreadNotifications = ref(5)
const activeNotificationTab = ref('all')
const activeTab = ref('Dashboard')
const openTabs = ref<TabItem[]>([{ name: 'Dashboard', title: '首页', path: '/dashboard' }])

// 右键菜单相关
const contextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)
const currentContextMenuTab = ref<TabItem | null>(null)
const routerViewKey = ref(0)

// 计算属性
const activeMenu = computed(() => route.path)
const menuRoutes = computed(() => {
  const homeMenu: MenuItem = {
    id: 0, parentId: 0, menuName: '首页', menuType: 'C',
    path: '/dashboard', icon: 'House', visible: 1
  }
  const userMenus = menuStore.menus.filter((m: MenuItem) => m.menuType !== 'F' && m.visible === 1)
  return userMenus.length > 0 ? [homeMenu, ...userMenus] : [homeMenu]
})

// 当前右键菜单标签的索引
const currentTabIndex = computed(() => {
  if (!currentContextMenuTab.value) return -1
  return openTabs.value.findIndex(tab => tab.name === currentContextMenuTab.value?.name)
})

// 搜索建议
const searchSuggestions = ref([
  { id: 1, text: '添加路由', category: '功能', icon: 'Plus', path: '/routes/add' },
  { id: 2, text: '用户管理', category: '页面', icon: 'User', path: '/system/users' },
  { id: 3, text: '系统监控', category: '页面', icon: 'Monitor', path: '/monitor/realtime' },
  { id: 4, text: '日志查看', category: '功能', icon: 'Document', path: '/monitor/logs' }
])

// 通知标签页
const notificationTabs = ref([
  { key: 'all', label: '全部', count: 5 },
  { key: 'system', label: '系统', count: 2 },
  { key: 'security', label: '安全', count: 1 },
  { key: 'operation', label: '操作', count: 2 }
])

// 通知列表
const notifications = ref([
  {
    id: 1,
    type: 'warning',
    icon: 'Warning',
    title: '系统资源告警',
    description: 'CPU使用率超过80%，请及时处理',
    time: new Date(Date.now() - 5 * 60 * 1000),
    read: false,
    category: 'system'
  },
  {
    id: 2,
    type: 'info',
    icon: 'User',
    title: '新用户注册',
    description: '用户"张三"完成注册验证',
    time: new Date(Date.now() - 15 * 60 * 1000),
    read: false,
    category: 'operation'
  },
  {
    id: 3,
    type: 'success',
    icon: 'CircleCheck',
    title: '备份完成',
    description: '系统数据备份已成功完成',
    time: new Date(Date.now() - 2 * 60 * 60 * 1000),
    read: true,
    category: 'system'
  },
  {
    id: 4,
    type: 'error',
    icon: 'Warning',
    title: '登录异常',
    description: '检测到异常登录行为',
    time: new Date(Date.now() - 3 * 60 * 60 * 1000),
    read: false,
    category: 'security'
  }
])

// 过滤通知
const filteredNotifications = computed(() => {
  if (activeNotificationTab.value === 'all') {
    return notifications.value
  }
  return notifications.value.filter(n => n.category === activeNotificationTab.value)
})

// 响应式控制
const isMobile = ref(false)

// 方法
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
  // 在移动端自动折叠
  if (isMobile.value) {
    isCollapse.value = true
  }
}

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    // 执行搜索逻辑
    console.log('搜索:', searchQuery.value)
    showSearchPanel.value = false
  }
}

const handleSuggestionClick = (suggestion: any) => {
  router.push(suggestion.path)
  showSearchPanel.value = false
  searchQuery.value = ''
}

const markAsRead = (id: number) => {
  const notification = notifications.value.find(n => n.id === id)
  if (notification && !notification.read) {
    notification.read = true
    unreadNotifications.value--
  }
}

const handleProfile = () => {
  router.push('/profile')
}

const handleMenuSelect = (index: string) => {
  if (index && index !== route.path) router.push(index)
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    await userStore.logout()
    ElMessage.success('退出登录成功')
    await router.push('/login')
  } catch (error) {
    if (error === 'cancel') return
    // 网络错误时也允许退出登录（已由 userStore.logout 处理）
    if (!userStore.token) {
      ElMessage.success('退出登录成功')
      await router.push('/login')
    }
  }
}

const removeTab = (targetName: string) => {
  const tabs = openTabs.value
  let activeName = activeTab.value
  
  if (activeName === targetName) {
    tabs.forEach((tab, index) => {
      if (tab.name === targetName) {
        const nextTab = tabs[index + 1] || tabs[index - 1]
        if (nextTab) {
          activeName = nextTab.name
        }
      }
    })
  }
  
  activeTab.value = activeName
  openTabs.value = tabs.filter(tab => tab.name !== targetName)
  
  if (activeTab.value !== targetName) {
    const activeTabInfo = openTabs.value.find(tab => tab.name === activeTab.value)
    if (activeTabInfo) {
      router.push(activeTabInfo.path)
    }
  }
}

// 标签页点击
const handleTabItemClick = (tab: TabItem) => {
  activeTab.value = tab.name
  router.push(tab.path)
}

// 打开右键菜单
const openContextMenu = (e: MouseEvent, tab: TabItem) => {
  currentContextMenuTab.value = tab
  contextMenuX.value = e.clientX
  contextMenuY.value = e.clientY
  contextMenuVisible.value = true
}

// 关闭右键菜单
const closeContextMenu = () => {
  contextMenuVisible.value = false
  currentContextMenuTab.value = null
}

// 刷新当前页面
const refreshTab = () => {
  closeContextMenu()
  routerViewKey.value++
}

// 关闭当前标签
const closeCurrentTab = () => {
  if (currentContextMenuTab.value) {
    removeTab(currentContextMenuTab.value.name)
  }
  closeContextMenu()
}

// 关闭其他标签
const closeOtherTabs = () => {
  if (currentContextMenuTab.value) {
    openTabs.value = openTabs.value.filter(
      tab => tab.name === currentContextMenuTab.value?.name
    )
    activeTab.value = currentContextMenuTab.value.name
    router.push(currentContextMenuTab.value.path)
  }
  closeContextMenu()
}

// 关闭左侧标签
const closeLeftTabs = () => {
  if (currentContextMenuTab.value) {
    const index = openTabs.value.findIndex(tab => tab.name === currentContextMenuTab.value?.name)
    if (index > 0) {
      openTabs.value = openTabs.value.slice(index)
    }
  }
  closeContextMenu()
}

// 关闭右侧标签
const closeRightTabs = () => {
  if (currentContextMenuTab.value) {
    const index = openTabs.value.findIndex(tab => tab.name === currentContextMenuTab.value?.name)
    if (index < openTabs.value.length - 1) {
      openTabs.value = openTabs.value.slice(0, index + 1)
    }
  }
  closeContextMenu()
}

// 关闭所有标签
const closeAllTabs = () => {
  openTabs.value = [{ name: 'Dashboard', title: '首页', path: '/dashboard' }]
  activeTab.value = 'Dashboard'
  router.push('/dashboard')
  closeContextMenu()
}

const formatTime = (date: Date) => {
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / (60 * 1000))
  const hours = Math.floor(minutes / 60)
  
  if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

// 监听路由变化，添加标签页
watch(route, (newRoute) => {
  const routeMeta = newRoute.meta as any
  if (routeMeta?.title && newRoute.matched.some(r => r.components)) {
    if (!openTabs.value.find(tab => tab.path === newRoute.path)) {
      openTabs.value.push({ name: newRoute.name as string, title: routeMeta.title, path: newRoute.path })
    }
  }
  if (newRoute.name) activeTab.value = newRoute.name as string
})

// 监听窗口大小变化
const handleResize = () => {
  isMobile.value = window.innerWidth < 768
  if (isMobile.value) {
    isCollapse.value = true
  }
}

// 全局键盘快捷键
const handleKeydown = (e: KeyboardEvent) => {
  // Ctrl+K 打开搜索
  if (e.ctrlKey && e.key === 'k') {
    e.preventDefault()
    showSearchPanel.value = true
    nextTick(() => {
      const searchInput = document.querySelector('.search-input input') as HTMLInputElement
      searchInput?.focus()
    })
  }
}

// 点击外部关闭搜索面板和右键菜单
const handleClickOutside = (e: MouseEvent) => {
  const target = e.target as HTMLElement
  if (!target.closest('.global-search')) {
    showSearchPanel.value = false
  }
  if (!target.closest('.context-menu') && !target.closest('.tab-item')) {
    closeContextMenu()
  }
}

// 生命周期
onMounted(async () => {
  handleResize()
  window.addEventListener('resize', handleResize)
  document.addEventListener('keydown', handleKeydown)
  document.addEventListener('click', handleClickOutside)
  
  // 初始化当前路由的标签页
  const routeMeta = route.meta as any
  if (routeMeta?.title && route.matched.some(r => r.components)) {
    if (!openTabs.value.find(tab => tab.path === route.path)) {
      openTabs.value.push({ name: route.name as string, title: routeMeta.title, path: route.path })
    }
    if (route.name) activeTab.value = route.name as string
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  document.removeEventListener('keydown', handleKeydown)
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  background-color: var(--bg-secondary);
  
  // 现代化侧边栏
  .layout-aside {
    background: var(--bg-sidebar);
    border-right: 1px solid var(--border-primary);
    box-shadow: var(--shadow-sm);
    transition: width var(--transition-base);
    position: relative;
    z-index: 100;
    
    .sidebar-content {
      height: 100%;
      display: flex;
      flex-direction: column;
    }
    
    .logo-section {
      height: calc(var(--header-height) + 40px);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      border-bottom: 1px solid var(--border-primary);
      text-align: center;
      padding: 0 var(--space-4);
      
      .logo-subtitle {
        font-size: var(--text-xs);
        color: var(--text-tertiary);
        margin-top: var(--space-1);
        font-weight: var(--font-medium);
      }
    }
    
    .nav-section {
      flex: 1;
      overflow: hidden;
      
      .menu-scrollbar {
        height: 100%;
        
        :deep(.el-scrollbar__view) {
          padding: var(--space-4) 0;
        }
      }
      
      .sidebar-menu {
        border: none;
        background: transparent;
        
        :deep(.el-menu-item),
        :deep(.el-sub-menu__title) {
          margin: var(--space-1) var(--space-3);
          border-radius: var(--radius-lg);
          color: var(--text-secondary);
          transition: all var(--transition-fast);
          position: relative;
          overflow: hidden;
          font-weight: var(--font-medium);
          font-size: 15px;
          box-shadow: none;
          
          &:hover {
            background: var(--card-bg);
            color: var(--primary-color);
            box-shadow: var(--shadow-sm);
            transform: translateX(4px);
          }
          
          &.is-active {
            color: white;
            background: linear-gradient(135deg, #b8a4fb 0%, #9b7af7 100%);
            font-weight: var(--font-semibold);
            box-shadow: var(--shadow-md);
            transform: translateX(4px);
          }
          
          .el-icon {
            font-size: var(--text-lg);
            margin-right: var(--space-3);
          }
        }
        
        :deep(.el-sub-menu) {
          .el-menu {
            background: transparent;
            border-radius: var(--radius-md);
            margin: 0 var(--space-3);
            
            .el-menu-item {
              margin: var(--space-1);
              padding-left: var(--space-12) !important;
              font-size: var(--text-sm);
              border-radius: var(--radius-lg);
              color: var(--text-secondary);
              
              &:hover {
                background: var(--card-bg);
                color: var(--primary-color);
                box-shadow: var(--shadow-xs);
                transform: translateX(2px);
              }
              
              &.is-active {
                color: white;
                background: linear-gradient(135deg, #b8a4fb 0%, #9b7af7 100%);
                font-weight: var(--font-semibold);
                box-shadow: var(--shadow-sm);
              }
            }
          }
        }
        
        // 折叠状态样式
        &.el-menu--collapse {
          :deep(.el-menu-item) {
            margin: var(--space-2) auto;
            padding: 0 !important;
            width: 48px;
            height: 48px;
            display: flex;
            align-items: center;
            justify-content: center;
            transform: none !important;
            
            .el-icon {
              margin: 0;
              font-size: 20px;
            }
            
            .menu-title {
              display: none;
            }
            
            &:hover,
            &.is-active {
              transform: none !important;
            }
          }
          
          :deep(.el-sub-menu) {
            .el-sub-menu__title {
              margin: var(--space-2) auto;
              padding: 0 !important;
              width: 48px;
              height: 48px;
              display: flex;
              align-items: center;
              justify-content: center;
              transform: none !important;
              
              .el-icon {
                margin: 0;
                font-size: 20px;
              }
              
              .menu-title {
                display: none;
              }
              
              &:hover,
              &.is-active {
                transform: none !important;
              }
            }
            
            .el-sub-menu__icon-arrow {
              display: none;
            }
          }
        }
      }
    }
    
    .user-section {
      padding: var(--space-4);
      border-top: 1px solid var(--border-primary);
      
      .user-card {
        display: flex;
        align-items: center;
        gap: var(--space-3);
        padding: var(--space-3);
        border-radius: var(--radius-lg);
        background: var(--bg-secondary);
        border: 1px solid var(--border-primary);
        transition: all var(--transition-fast);
        
        &:hover {
          background: var(--card-bg);
          box-shadow: var(--shadow-sm);
          border-color: var(--primary-color);
        }
        
        .user-avatar {
          flex-shrink: 0;
          border: 2px solid var(--primary-color);
          box-shadow: 0 0 0 3px var(--primary-100);
        }
        
        .user-info {
          flex: 1;
          min-width: 0;
          
          .user-name {
            font-weight: var(--font-semibold);
            color: var(--text-primary);
            font-size: var(--text-sm);
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
          
          .user-role {
            font-size: var(--text-xs);
            color: var(--text-tertiary);
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
        }
        
        .user-menu-btn {
          flex-shrink: 0;
          color: var(--text-tertiary);
          
          &:hover {
            color: var(--primary-color);
          }
        }
      }
    }
  }
  
  // 现代化顶部栏
  .layout-header {
    height: var(--header-height);
    background: var(--card-bg);
    border-bottom: 1px solid var(--border-primary);
    box-shadow: var(--shadow-xs);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 var(--space-6);
    position: relative;
    z-index: 99;
    
    .header-left {
      display: flex;
      align-items: center;
      gap: var(--space-4);
      flex: 1;
      min-width: 0;
      
      .collapse-btn {
        color: var(--text-secondary);
        padding: var(--space-2);
        border-radius: var(--radius-md);
        transition: all var(--transition-fast);
        
        &:hover {
          color: var(--primary-color);
          background: var(--primary-50);
        }
      }
      
      .header-breadcrumb {
        flex: 1;
        min-width: 0;
      }
    }
    
    .header-center {
      flex: 1;
      max-width: 400px;
      margin: 0 var(--space-6);
      
      .global-search {
        position: relative;
        
        .search-input {
          :deep(.el-input__wrapper) {
            border-radius: var(--radius-full);
            background: var(--bg-secondary);
            border: 1px solid transparent;
            transition: all var(--transition-fast);
            
            &:hover {
              background: var(--bg-primary);
              border-color: var(--border-hover);
            }
            
            &.is-focus {
              background: var(--bg-primary);
              border-color: var(--primary-color);
              box-shadow: 0 0 0 3px var(--primary-50);
            }
          }
        }
        
        .search-panel {
          position: absolute;
          top: 100%;
          left: 0;
          right: 0;
          background: var(--card-bg);
          border: 1px solid var(--border-primary);
          border-radius: var(--radius-lg);
          box-shadow: var(--shadow-lg);
          margin-top: var(--space-2);
          z-index: 1000;
          overflow: hidden;
          
          .search-suggestions {
            max-height: 300px;
            overflow-y: auto;
            
            .suggestion-item {
              display: flex;
              align-items: center;
              gap: var(--space-3);
              padding: var(--space-3) var(--space-4);
              cursor: pointer;
              transition: background var(--transition-fast);
              
              &:hover {
                background: var(--primary-50);
              }
              
              .suggestion-icon {
                color: var(--text-tertiary);
                font-size: var(--text-base);
              }
              
              .suggestion-text {
                flex: 1;
                color: var(--text-primary);
                font-weight: var(--font-medium);
              }
              
              .suggestion-category {
                font-size: var(--text-xs);
                color: var(--text-tertiary);
                background: var(--bg-tertiary);
                padding: var(--space-1) var(--space-2);
                border-radius: var(--radius-sm);
              }
            }
          }
        }
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
      gap: var(--space-1);
      
      .header-action-btn {
        width: 36px;
        height: 36px;
        border-radius: var(--radius-md);
        color: var(--text-secondary);
        transition: all var(--transition-fast);
        
        &:hover {
          color: var(--primary-color);
          background: var(--primary-50);
        }
      }
      
      .notification-badge {
        :deep(.el-badge__content) {
          background: var(--error-color);
          border: 2px solid var(--card-bg);
        }
      }
      
      .user-dropdown-trigger {
        display: flex;
        align-items: center;
        gap: var(--space-2);
        padding: var(--space-1) var(--space-2);
        border-radius: var(--radius-md);
        cursor: pointer;
        transition: all var(--transition-fast);
        
        &:hover {
          background: var(--bg-secondary);
        }
        
        .header-avatar {
          background: var(--primary-color);
          color: white;
        }
        
        .user-name-header {
          font-size: var(--text-sm);
          color: var(--text-primary);
          max-width: 100px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .dropdown-arrow {
          font-size: 12px;
          color: var(--text-tertiary);
          transition: transform var(--transition-fast);
        }
      }
    }
  }
  
  // 主内容区
  .layout-main {
    background: var(--bg-secondary);
    padding: 0;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    
    .page-tabs {
      background: var(--card-bg);
      padding: 0 var(--space-4);
      height: 40px;
      display: flex;
      align-items: center;
      border-bottom: 1px solid var(--border-primary);
      
      .tabs-wrapper {
        display: flex;
        align-items: center;
        gap: var(--space-2);
        height: 100%;
        overflow-x: auto;
        
        &::-webkit-scrollbar {
          height: 0;
        }
        
        .tab-item {
          display: flex;
          align-items: center;
          gap: var(--space-1);
          padding: var(--space-1) var(--space-3);
          border-radius: var(--radius-md);
          color: var(--text-secondary);
          font-size: var(--text-xs);
          cursor: pointer;
          transition: all var(--transition-fast);
          white-space: nowrap;
          user-select: none;
          background: var(--bg-secondary);
          border: 1px solid var(--border-primary);
          height: 28px;
          
          &:hover {
            color: var(--primary-color);
            background: var(--primary-50);
            border-color: var(--primary-color);
          }
          
          &.active {
            color: var(--primary-color);
            background: var(--primary-100);
            border-color: var(--primary-color);
            font-weight: var(--font-medium);
          }
          
          .tab-close {
            font-size: 10px;
            opacity: 0.6;
            transition: opacity var(--transition-fast);
            
            &:hover {
              opacity: 1;
            }
          }
        }
      }
    }
    
    .content-wrapper {
      flex: 1;
      overflow: auto;
      padding: var(--space-6);
      
      // 页面切换动画
      .page-enter-active,
      .page-leave-active {
        transition: all var(--transition-base);
      }
      
      .page-enter-from {
        opacity: 0;
        transform: translateY(20px);
      }
      
      .page-leave-to {
        opacity: 0;
        transform: translateY(-20px);
      }
    }
  }
}

// 右键菜单
.context-menu {
  position: fixed;
  background: var(--card-bg);
  border: 1px solid var(--border-primary);
  border-radius: var(--radius-lg);
  padding: var(--space-2);
  min-width: 180px;
  z-index: 3000;
  box-shadow: var(--shadow-xl);
  
  .context-menu-item {
    display: flex;
    align-items: center;
    gap: var(--space-2);
    padding: var(--space-2) var(--space-3);
    border-radius: var(--radius-md);
    color: var(--text-primary);
    font-size: var(--text-sm);
    cursor: pointer;
    transition: all var(--transition-fast);
    
    &:hover {
      background: var(--primary-50);
      color: var(--primary-color);
    }
    
    .el-icon {
      font-size: 14px;
    }
  }
  
  .context-menu-divider {
    height: 1px;
    background: var(--border-primary);
    margin: var(--space-1) 0;
  }
}

// 通知抽屉
.notification-drawer {
  :deep(.el-drawer__body) {
    padding: 0;
  }
  
  .notifications-content {
    height: 100%;
    display: flex;
    flex-direction: column;
    
    .notification-tabs {
      padding: var(--space-4);
      border-bottom: 1px solid var(--border-primary);
      display: flex;
      gap: var(--space-2);
    }
    
    .notification-list {
      flex: 1;
      overflow-y: auto;
      padding: var(--space-4);
      
      .notification-item {
        display: flex;
        gap: var(--space-3);
        padding: var(--space-4);
        border-radius: var(--radius-lg);
        cursor: pointer;
        transition: all var(--transition-fast);
        margin-bottom: var(--space-3);
        
        &:hover {
          background: var(--bg-secondary);
        }
        
        &.unread {
          background: var(--primary-50);
          border-left: 3px solid var(--primary-color);
        }
        
        .notification-icon {
          width: 40px;
          height: 40px;
          border-radius: var(--radius-lg);
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
          
          &.info {
            background: var(--info-50);
            color: var(--info-color);
          }
          
          &.warning {
            background: var(--warning-50);
            color: var(--warning-color);
          }
          
          &.success {
            background: var(--success-50);
            color: var(--success-color);
          }
          
          &.error {
            background: var(--error-50);
            color: var(--error-color);
          }
        }
        
        .notification-content {
          flex: 1;
          
          .notification-title {
            font-weight: var(--font-semibold);
            color: var(--text-primary);
            margin-bottom: var(--space-1);
          }
          
          .notification-desc {
            font-size: var(--text-sm);
            color: var(--text-secondary);
            margin-bottom: var(--space-2);
            line-height: var(--leading-relaxed);
          }
          
          .notification-time {
            font-size: var(--text-xs);
            color: var(--text-tertiary);
          }
        }
      }
    }
  }
}

// 响应式适配
@media (max-width: 1200px) {
  .layout-container {
    .layout-header {
      .header-center {
        max-width: 300px;
      }
    }
  }
}

@media (max-width: 768px) {
  .layout-container {
    .layout-aside {
      position: fixed;
      height: 100vh;
      z-index: 1000;
      
      &:not(.collapsed) {
        box-shadow: var(--shadow-2xl);
      }
    }
    
    .layout-header {
      padding: 0 var(--space-4);
      
      .header-center {
        display: none;
      }
      
      .header-left {
        gap: var(--space-2);
      }
      
      .header-right {
        gap: var(--space-1);
      }
    }
    
    .layout-main {
      .page-tabs {
        display: none;
      }
      
      .content-wrapper {
        padding: var(--space-4);
      }
    }
  }
}

@media (max-width: 480px) {
  .layout-container {
    .layout-header {
      .header-left {
        .header-breadcrumb {
          display: none;
        }
      }
      
      .header-right {
        .header-action-btn {
          width: 36px;
          height: 36px;
        }
      }
    }
    
    .layout-main {
      .content-wrapper {
        padding: var(--space-3);
      }
    }
  }
}

// 暗色模式样式
.dark {
  .layout-container {
    .layout-aside {
      .sidebar-menu {
        :deep(.el-menu-item),
        :deep(.el-sub-menu__title) {
          &.is-active {
            // 暗色模式下使用更柔和的紫色
            background: linear-gradient(135deg, rgba(139, 92, 246, 0.15) 0%, rgba(124, 58, 237, 0.25) 100%);
            color: #c4b5fd;
            border: 1px solid rgba(139, 92, 246, 0.3);
            box-shadow: 0 2px 8px rgba(139, 92, 246, 0.2);
          }
        }
        
        :deep(.el-sub-menu) {
          .el-menu {
            .el-menu-item {
              &.is-active {
                // 子菜单选中状态
                background: linear-gradient(135deg, rgba(139, 92, 246, 0.15) 0%, rgba(124, 58, 237, 0.25) 100%);
                color: #c4b5fd;
                border: 1px solid rgba(139, 92, 246, 0.3);
                box-shadow: 0 1px 4px rgba(139, 92, 246, 0.15);
              }
            }
          }
        }
      }
    }
    
    .layout-main {
      .page-tabs {
        .tab-item {
          &.active {
            // Tab选中状态 - 暗色模式使用柔和紫色
            background: rgba(139, 92, 246, 0.15);
            border-color: rgba(139, 92, 246, 0.4);
            color: #c4b5fd;
          }
        }
      }
    }
  }
}
</style> 