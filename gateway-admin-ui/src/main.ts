import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// Element Plus - 统一导入
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

// Element Plus 图标 - 按需导入常用图标
import {
  User,
  Lock,
  Menu,
  Setting,
  Monitor,
  Connection,
  House,
  Expand,
  Fold,
  SwitchButton,
  Bell,
  Search,
  Plus,
  Edit,
  Delete,
  View,
  RefreshRight,
  ArrowRight,
  ArrowLeft,
  ArrowUp,
  ArrowDown,
  Close,
  Check,
  Warning,
  InfoFilled,
  SuccessFilled,
  CircleClose,
  QuestionFilled,
  FolderRemove,
  Back,
  Right
} from '@element-plus/icons-vue'

// 全局样式
import './styles/index.scss'

// 自定义指令
import permission from './directives/permission'

const app = createApp(App)
const pinia = createPinia()

// 注册常用图标组件
const icons = {
  User,
  Lock,
  Menu,
  Setting,
  Monitor,
  Connection,
  House,
  Expand,
  Fold,
  SwitchButton,
  Bell,
  Search,
  Plus,
  Edit,
  Delete,
  View,
  RefreshRight,
  ArrowRight,
  ArrowLeft,
  ArrowUp,
  ArrowDown,
  Close,
  Check,
  Warning,
  InfoFilled,
  SuccessFilled,
  CircleClose,
  QuestionFilled,
  FolderRemove,
  Back,
  Right
}

// 注册图标
Object.entries(icons).forEach(([key, component]) => {
  app.component(key, component)
})

// 配置 Element Plus
app.use(ElementPlus, {
  locale: zhCn,
  size: 'default',
})

app.use(pinia)
app.use(router)

// 注册自定义指令
app.directive('permission', permission)

// 初始化主题
import { initTheme } from '@/composables/useTheme'
initTheme()

// 初始化用户状态
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()
userStore.init()

// 开发环境信息
if (import.meta.env.DEV) {
  console.log('🚀 Muxin Gateway 管理系统启动完成')
  console.log('📊 当前环境:', import.meta.env.MODE)
  console.log('🎨 主题模式:', localStorage.getItem('theme') || 'light')
}

app.mount('#app') 