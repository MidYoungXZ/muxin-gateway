<template>
  <div class="dashboard-container">
    <!-- 现代化欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-card animate-fade-in">
        <div class="welcome-content">
          <div class="welcome-text">
            <h1 class="welcome-title">
              <span class="greeting">{{ greeting }}，</span>
              <span class="username">{{ userStore.user?.nickname || '管理员' }}！</span>
            </h1>
            <p class="welcome-subtitle">
              今天是 {{ formatDate(new Date()) }}，让我们开始高效的网关管理之旅
            </p>
          </div>
          <div class="welcome-stats">
            <div class="stat-item">
              <div class="stat-value">{{ formatUptime(systemUptime) }}</div>
              <div class="stat-label">系统运行时间</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ onlineUsers }}</div>
              <div class="stat-label">在线用户</div>
            </div>
          </div>
        </div>
        <div class="welcome-decoration">
          <div class="deco-circle deco-1"></div>
          <div class="deco-circle deco-2"></div>
          <div class="deco-circle deco-3"></div>
        </div>
      </div>
    </div>

    <!-- 核心统计卡片 -->
    <div class="stats-grid">
      <div 
        v-for="(stat, index) in coreStats" 
        :key="stat.key"
        class="stats-card"
        :class="`stats-card-${index + 1}`"
        :style="{ animationDelay: `${index * 100}ms` }"
      >
        <div class="stats-header">
          <div class="stats-icon" :style="{ background: stat.gradient }">
            <component :is="stat.icon" />
          </div>
          <div class="stats-trend" :class="stat.trendType">
            <el-icon>
              <component :is="stat.trendType === 'up' ? 'TrendCharts' : 'Bottom'" />
            </el-icon>
            <span>{{ stat.change }}</span>
          </div>
        </div>
        <div class="stats-body">
          <div class="stats-value" :data-target="stat.value">{{ animatedValues[stat.key] }}</div>
          <div class="stats-label">{{ stat.label }}</div>
          <div class="stats-description">{{ stat.description }}</div>
        </div>
        <div class="stats-footer">
          <div class="mini-chart" :ref="el => setMiniChartRef(stat.key, el)"></div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="content-left">
        <!-- 实时监控图表 -->
        <div class="chart-card modern-card">
          <div class="card-header">
            <h3 class="card-title">
              <el-icon><TrendCharts /></el-icon>
              实时请求监控
            </h3>
            <div class="card-actions">
              <el-button-group size="small">
                <el-button 
                  v-for="period in timePeriods" 
                  :key="period.value"
                  :type="selectedPeriod === period.value ? 'primary' : 'default'"
                  @click="selectedPeriod = period.value"
                >
                  {{ period.label }}
                </el-button>
              </el-button-group>
              <el-button size="small" @click="toggleFullscreen">
                <el-icon><FullScreen /></el-icon>
              </el-button>
            </div>
          </div>
          <div class="card-body">
            <div ref="mainChartRef" class="main-chart"></div>
          </div>
        </div>

        <!-- 服务状态监控 -->
        <div class="service-monitor modern-card">
          <div class="card-header">
            <h3 class="card-title">
              <el-icon><Monitor /></el-icon>
              服务健康状况
            </h3>
            <el-tag :type="overallStatus.type" size="small">
              {{ overallStatus.text }}
            </el-tag>
          </div>
          <div class="card-body">
            <div class="service-grid">
              <div 
                v-for="service in services" 
                :key="service.id"
                class="service-item"
                :class="service.status"
              >
                <div class="service-info">
                  <div class="service-name">{{ service.name }}</div>
                  <div class="service-url">{{ service.url }}</div>
                </div>
                <div class="service-metrics">
                  <div class="metric">
                    <span class="metric-label">响应时间</span>
                    <span class="metric-value">{{ service.responseTime }}ms</span>
                  </div>
                  <div class="metric">
                    <span class="metric-label">成功率</span>
                    <span class="metric-value">{{ service.successRate }}%</span>
                  </div>
                </div>
                <div class="service-status">
                  <el-icon :class="`status-${service.status}`">
                    <component :is="service.status === 'healthy' ? 'CircleCheck' : 'CircleClose'" />
                  </el-icon>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="content-right">
        <!-- 系统资源使用情况 -->
        <div class="resource-card modern-card">
          <div class="card-header">
            <h3 class="card-title">
              <el-icon><Cpu /></el-icon>
              系统资源
            </h3>
          </div>
          <div class="card-body">
            <div class="resource-item" v-for="resource in systemResources" :key="resource.name">
              <div class="resource-info">
                <span class="resource-name">{{ resource.name }}</span>
                <span class="resource-value">{{ resource.value }}%</span>
              </div>
              <div class="resource-bar">
                <div 
                  class="resource-progress" 
                  :style="{ 
                    width: `${resource.value}%`,
                    background: getResourceColor(resource.value)
                  }"
                ></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 最近活动 -->
        <div class="activity-card modern-card">
          <div class="card-header">
            <h3 class="card-title">
              <el-icon><Bell /></el-icon>
              最近活动
            </h3>
            <el-button text size="small" @click="viewAllActivities">
              查看全部
            </el-button>
          </div>
          <div class="card-body">
            <div class="activity-list">
              <div 
                v-for="activity in recentActivities" 
                :key="activity.id"
                class="activity-item"
              >
                <div class="activity-icon" :class="activity.type">
                  <el-icon>
                    <component :is="activity.icon" />
                  </el-icon>
                </div>
                <div class="activity-content">
                  <div class="activity-title">{{ activity.title }}</div>
                  <div class="activity-desc">{{ activity.description }}</div>
                  <div class="activity-time">{{ formatTime(activity.time) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div class="quick-actions modern-card">
          <div class="card-header">
            <h3 class="card-title">
              <el-icon><Operation /></el-icon>
              快捷操作
            </h3>
          </div>
          <div class="card-body">
            <div class="actions-grid">
              <div 
                v-for="action in quickActions" 
                :key="action.key"
                class="action-item"
                @click="handleQuickAction(action.key)"
              >
                <div class="action-icon" :style="{ background: action.color }">
                  <el-icon>
                    <component :is="action.icon" />
                  </el-icon>
                </div>
                <div class="action-label">{{ action.label }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const selectedPeriod = ref('1h')
const systemUptime = ref(15 * 24 * 60 * 60 * 1000) // 15天
const onlineUsers = ref(156)
const mainChartRef = ref<HTMLElement>()
const miniChartRefs = ref<Map<string, HTMLElement>>(new Map())

// 时间问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 17) return '下午好'
  if (hour < 19) return '傍晚好'
  if (hour < 22) return '晚上好'
  return '夜深了'
})

// 时间周期选项
const timePeriods = [
  { label: '1小时', value: '1h' },
  { label: '6小时', value: '6h' },
  { label: '24小时', value: '24h' },
  { label: '7天', value: '7d' }
]

// 核心统计数据
const coreStats = ref([
  {
    key: 'requests',
    label: '总请求数',
    value: 1234567,
    change: '+12.5%',
    trendType: 'up',
    description: '相比昨天',
    icon: 'TrendCharts',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    key: 'routes',
    label: '活跃路由',
    value: 42,
    change: '+3',
    trendType: 'up',
    description: '当前在线',
    icon: 'Connection',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    key: 'errorRate',
    label: '错误率',
    value: 0.5,
    change: '-0.2%',
    trendType: 'down',
    description: '相比昨天',
    icon: 'Warning',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    key: 'responseTime',
    label: '平均响应时间',
    value: 125,
    change: '-15ms',
    trendType: 'down',
    description: '相比昨天',
    icon: 'Timer',
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  }
])

// 动画数值
const animatedValues = ref<Record<string, string>>({})

// 系统资源
const systemResources = ref([
  { name: 'CPU使用率', value: 25.6 },
  { name: '内存使用率', value: 68.2 },
  { name: '磁盘使用率', value: 45.8 },
  { name: '网络I/O', value: 32.1 }
])

// 服务状态
const services = ref([
  {
    id: 1,
    name: '用户服务',
    url: 'http://user-service:8080',
    status: 'healthy',
    responseTime: 45,
    successRate: 99.9
  },
  {
    id: 2,
    name: '订单服务',
    url: 'http://order-service:8080',
    status: 'healthy',
    responseTime: 67,
    successRate: 98.5
  },
  {
    id: 3,
    name: '支付服务',
    url: 'http://payment-service:8080',
    status: 'unhealthy',
    responseTime: 1250,
    successRate: 85.2
  }
])

// 整体服务状态
const overallStatus = computed(() => {
  const healthyCount = services.value.filter(s => s.status === 'healthy').length
  const total = services.value.length
  const healthyPercent = (healthyCount / total) * 100
  
  if (healthyPercent === 100) {
    return { type: 'success', text: '全部正常' }
  } else if (healthyPercent >= 80) {
    return { type: 'warning', text: '部分异常' }
  } else {
    return { type: 'danger', text: '严重异常' }
  }
})

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    type: 'info',
    icon: 'User',
    title: '新用户注册',
    description: '用户 张三 完成注册',
    time: new Date(Date.now() - 5 * 60 * 1000)
  },
  {
    id: 2,
    type: 'warning',
    icon: 'Warning',
    title: '路由配置更新',
    description: '订单服务路由规则已更新',
    time: new Date(Date.now() - 15 * 60 * 1000)
  },
  {
    id: 3,
    type: 'success',
    icon: 'CircleCheck',
    title: '系统备份完成',
    description: '定时备份任务执行成功',
    time: new Date(Date.now() - 2 * 60 * 60 * 1000)
  },
  {
    id: 4,
    type: 'error',
    icon: 'CircleClose',
    title: '服务异常告警',
    description: '支付服务响应时间过长',
    time: new Date(Date.now() - 3 * 60 * 60 * 1000)
  }
])

// 快捷操作
const quickActions = ref([
  {
    key: 'add-route',
    label: '添加路由',
    icon: 'Plus',
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    key: 'view-logs',
    label: '查看日志',
    icon: 'Document',
    color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    key: 'system-settings',
    label: '系统设置',
    icon: 'Setting',
    color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    key: 'user-management',
    label: '用户管理',
    icon: 'UserFilled',
    color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  }
])

// 工具函数
const formatDate = (date: Date) => {
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
}

const formatUptime = (ms: number) => {
  const days = Math.floor(ms / (24 * 60 * 60 * 1000))
  const hours = Math.floor((ms % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000))
  return `${days}天${hours}小时`
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

const getResourceColor = (value: number) => {
  if (value < 50) return 'linear-gradient(90deg, #43e97b 0%, #38f9d7 100%)'
  if (value < 80) return 'linear-gradient(90deg, #f093fb 0%, #f5576c 100%)'
  return 'linear-gradient(90deg, #ff6b6b 0%, #ff8e53 100%)'
}

// 设置迷你图表引用
const setMiniChartRef = (key: string, el: HTMLElement | null) => {
  if (el) {
    miniChartRefs.value.set(key, el)
  }
}

// 数值动画
const animateValue = (key: string, target: number, suffix = '') => {
  const start = 0
  const duration = 1000
  const startTime = Date.now()
  
  const update = () => {
    const progress = Math.min((Date.now() - startTime) / duration, 1)
    const easeOutQuart = 1 - Math.pow(1 - progress, 4)
    const current = Math.floor(start + (target - start) * easeOutQuart)
    
    if (key === 'errorRate') {
      animatedValues.value[key] = (current / 100).toFixed(1) + '%'
    } else if (key === 'responseTime') {
      animatedValues.value[key] = current + 'ms'
    } else {
      animatedValues.value[key] = current.toLocaleString() + suffix
    }
    
    if (progress < 1) {
      requestAnimationFrame(update)
    }
  }
  
  requestAnimationFrame(update)
}

// 初始化图表
const initMainChart = () => {
  if (!mainChartRef.value) return
  
  const chart = echarts.init(mainChartRef.value)
  
  // 生成模拟数据
  const data = []
  const now = new Date()
  for (let i = 59; i >= 0; i--) {
    const time = new Date(now.getTime() - i * 60 * 1000)
    data.push([
      time,
      Math.floor(Math.random() * 1000) + 500
    ])
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'var(--card-bg)',
      borderColor: 'var(--border-primary)',
      textStyle: {
        color: 'var(--text-primary)'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'time',
      boundaryGap: false,
      axisLine: {
        lineStyle: {
          color: 'var(--border-primary)'
        }
      },
      axisLabel: {
        color: 'var(--text-secondary)'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: 'var(--border-primary)'
        }
      },
      axisLabel: {
        color: 'var(--text-secondary)'
      },
      splitLine: {
        lineStyle: {
          color: 'var(--border-primary)',
          type: 'dashed'
        }
      }
    },
    series: [{
      name: 'QPS',
      type: 'line',
      smooth: true,
      symbol: 'none',
      lineStyle: {
        color: '#667eea',
        width: 3
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
            { offset: 1, color: 'rgba(102, 126, 234, 0.05)' }
          ]
        }
      },
      data
    }]
  }
  
  chart.setOption(option)
  
  // 自动更新数据
  setInterval(() => {
    const newData = option.series[0].data.slice(1)
    newData.push([
      new Date(),
      Math.floor(Math.random() * 1000) + 500
    ])
    
    chart.setOption({
      series: [{
        data: newData
      }]
    })
  }, 5000)
}

// 快捷操作处理
const handleQuickAction = (key: string) => {
  switch (key) {
    case 'add-route':
      router.push('/routes/list')
      break
    case 'view-logs':
      router.push('/monitor/logs')
      break
    case 'system-settings':
      router.push('/system/settings')
      break
    case 'user-management':
      router.push('/system/users')
      break
  }
}

const toggleFullscreen = () => {
  // 全屏功能实现
}

const viewAllActivities = () => {
  router.push('/system/logs')
}

// 生命周期
onMounted(() => {
  // 启动数值动画
  coreStats.value.forEach(stat => {
    animateValue(stat.key, stat.value)
  })
  
  // 初始化图表
  nextTick(() => {
    initMainChart()
  })
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 0;
  
  // 欢迎区域
  .welcome-section {
    margin-bottom: var(--space-6);
  }
  
  .welcome-card {
    position: relative;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: var(--radius-2xl);
    padding: var(--space-8);
    color: white;
    overflow: hidden;
    
    .welcome-content {
      position: relative;
      z-index: 2;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .welcome-text {
      flex: 1;
    }
    
    .welcome-title {
      font-size: var(--text-4xl);
      font-weight: var(--font-bold);
      margin-bottom: var(--space-2);
      
      .greeting {
        opacity: 0.9;
      }
      
      .username {
        background: linear-gradient(45deg, #fff, #f0f0f0);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }
    }
    
    .welcome-subtitle {
      font-size: var(--text-lg);
      opacity: 0.8;
      font-weight: var(--font-light);
    }
    
    .welcome-stats {
      display: flex;
      gap: var(--space-8);
    }
    
    .stat-item {
      text-align: center;
      
      .stat-value {
        font-size: var(--text-2xl);
        font-weight: var(--font-bold);
        margin-bottom: var(--space-1);
      }
      
      .stat-label {
        font-size: var(--text-sm);
        opacity: 0.8;
      }
    }
    
    .welcome-decoration {
      position: absolute;
      inset: 0;
      z-index: 1;
      overflow: hidden;
    }
    
    .deco-circle {
      position: absolute;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.1);
      
      &.deco-1 {
        width: 200px;
        height: 200px;
        top: -100px;
        right: -50px;
        animation: float 20s ease-in-out infinite;
      }
      
      &.deco-2 {
        width: 150px;
        height: 150px;
        bottom: -75px;
        left: -25px;
        animation: float 25s ease-in-out infinite reverse;
      }
      
      &.deco-3 {
        width: 100px;
        height: 100px;
        top: 50%;
        left: 20%;
        animation: float 30s ease-in-out infinite;
      }
    }
  }
  
  // 统计卡片网格
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: var(--space-6);
    margin-bottom: var(--space-8);
  }
  
  .stats-card {
    background: var(--card-bg);
    border: 1px solid var(--card-border);
    border-radius: var(--radius-xl);
    padding: var(--space-6);
    box-shadow: var(--card-shadow);
    transition: all var(--transition-base);
    position: relative;
    overflow: hidden;
    animation: slideUp var(--duration-500) var(--ease-out) both;
    
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 4px;
      background: var(--gradient-primary);
      opacity: 0;
      transition: opacity var(--transition-base);
    }
    
    &:hover {
      transform: translateY(-8px);
      box-shadow: var(--shadow-xl);
      
      &::before {
        opacity: 1;
      }
    }
    
    .stats-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: var(--space-4);
    }
    
    .stats-icon {
      width: 56px;
      height: 56px;
      border-radius: var(--radius-xl);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: var(--text-2xl);
    }
    
    .stats-trend {
      display: flex;
      align-items: center;
      gap: var(--space-1);
      font-size: var(--text-sm);
      font-weight: var(--font-semibold);
      padding: var(--space-1) var(--space-2);
      border-radius: var(--radius-full);
      
      &.up {
        color: var(--success-color);
        background: var(--success-50);
      }
      
      &.down {
        color: var(--error-color);
        background: var(--error-50);
      }
    }
    
    .stats-body {
      margin-bottom: var(--space-4);
    }
    
    .stats-value {
      font-size: var(--text-4xl);
      font-weight: var(--font-black);
      color: var(--text-primary);
      margin-bottom: var(--space-1);
      font-variant-numeric: tabular-nums;
    }
    
    .stats-label {
      font-size: var(--text-base);
      color: var(--text-secondary);
      font-weight: var(--font-semibold);
      margin-bottom: var(--space-1);
    }
    
    .stats-description {
      font-size: var(--text-sm);
      color: var(--text-tertiary);
    }
    
    .stats-footer {
      height: 60px;
    }
    
    .mini-chart {
      width: 100%;
      height: 100%;
    }
  }
  
  // 主要内容区域
  .main-content {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: var(--space-6);
  }
  
  .content-left,
  .content-right {
    display: flex;
    flex-direction: column;
    gap: var(--space-6);
  }
  
  // 卡片通用样式
  .modern-card {
    background: var(--card-bg);
    border: 1px solid var(--card-border);
    border-radius: var(--radius-xl);
    box-shadow: var(--card-shadow);
    transition: all var(--transition-base);
    overflow: hidden;
    
    &:hover {
      box-shadow: var(--card-hover-shadow);
    }
    
    .card-header {
      padding: var(--space-6) var(--space-6) var(--space-4);
      border-bottom: 1px solid var(--border-primary);
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .card-title {
      font-size: var(--text-lg);
      font-weight: var(--font-semibold);
      color: var(--text-primary);
      display: flex;
      align-items: center;
      gap: var(--space-2);
    }
    
    .card-body {
      padding: var(--space-6);
    }
  }
  
  // 图表卡片
  .chart-card {
    flex: 1;
    
    .main-chart {
      width: 100%;
      height: 400px;
    }
    
    .card-actions {
      display: flex;
      gap: var(--space-2);
      align-items: center;
    }
  }
  
  // 服务监控
  .service-monitor {
    .service-grid {
      display: flex;
      flex-direction: column;
      gap: var(--space-4);
    }
    
    .service-item {
      padding: var(--space-4);
      border: 1px solid var(--border-primary);
      border-radius: var(--radius-lg);
      display: flex;
      align-items: center;
      gap: var(--space-4);
      transition: all var(--transition-fast);
      
      &.healthy {
        border-color: var(--success-color);
        background: var(--success-50);
      }
      
      &.unhealthy {
        border-color: var(--error-color);
        background: var(--error-50);
      }
      
      &:hover {
        transform: translateX(4px);
      }
    }
    
    .service-info {
      flex: 1;
      
      .service-name {
        font-weight: var(--font-semibold);
        color: var(--text-primary);
      }
      
      .service-url {
        font-size: var(--text-sm);
        color: var(--text-tertiary);
        font-family: 'Monaco', 'Consolas', monospace;
      }
    }
    
    .service-metrics {
      display: flex;
      gap: var(--space-4);
      
      .metric {
        text-align: center;
        
        .metric-label {
          display: block;
          font-size: var(--text-xs);
          color: var(--text-tertiary);
        }
        
        .metric-value {
          font-weight: var(--font-semibold);
          color: var(--text-primary);
        }
      }
    }
    
    .service-status {
      .status-healthy {
        color: var(--success-color);
      }
      
      .status-unhealthy {
        color: var(--error-color);
      }
    }
  }
  
  // 系统资源
  .resource-card {
    .resource-item {
      margin-bottom: var(--space-4);
      
      &:last-child {
        margin-bottom: 0;
      }
    }
    
    .resource-info {
      display: flex;
      justify-content: space-between;
      margin-bottom: var(--space-2);
      
      .resource-name {
        font-weight: var(--font-medium);
        color: var(--text-primary);
      }
      
      .resource-value {
        font-weight: var(--font-semibold);
        color: var(--text-secondary);
      }
    }
    
    .resource-bar {
      height: 8px;
      background: var(--bg-tertiary);
      border-radius: var(--radius-full);
      overflow: hidden;
    }
    
    .resource-progress {
      height: 100%;
      border-radius: var(--radius-full);
      transition: width var(--transition-base);
    }
  }
  
  // 活动列表
  .activity-card {
    .activity-list {
      max-height: 300px;
      overflow-y: auto;
    }
    
    .activity-item {
      display: flex;
      gap: var(--space-3);
      padding: var(--space-3) 0;
      border-bottom: 1px solid var(--border-primary);
      
      &:last-child {
        border-bottom: none;
      }
    }
    
    .activity-icon {
      width: 40px;
      height: 40px;
      border-radius: var(--radius-full);
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
    
    .activity-content {
      flex: 1;
      
      .activity-title {
        font-weight: var(--font-semibold);
        color: var(--text-primary);
        margin-bottom: var(--space-1);
      }
      
      .activity-desc {
        font-size: var(--text-sm);
        color: var(--text-secondary);
        margin-bottom: var(--space-1);
      }
      
      .activity-time {
        font-size: var(--text-xs);
        color: var(--text-tertiary);
      }
    }
  }
  
  // 快捷操作
  .quick-actions {
    .actions-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: var(--space-3);
    }
    
    .action-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: var(--space-4);
      border: 1px solid var(--border-primary);
      border-radius: var(--radius-lg);
      cursor: pointer;
      transition: all var(--transition-fast);
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: var(--shadow-md);
        border-color: var(--primary-color);
      }
    }
    
    .action-icon {
      width: 48px;
      height: 48px;
      border-radius: var(--radius-lg);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      margin-bottom: var(--space-2);
      font-size: var(--text-xl);
    }
    
    .action-label {
      font-size: var(--text-sm);
      font-weight: var(--font-medium);
      color: var(--text-primary);
      text-align: center;
    }
  }
}

// 动画
@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  33% {
    transform: translateY(-10px) rotate(120deg);
  }
  66% {
    transform: translateY(10px) rotate(240deg);
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 响应式适配
@media (max-width: 1200px) {
  .dashboard-container {
    .main-content {
      grid-template-columns: 1fr;
    }
    
    .welcome-stats {
      flex-direction: column;
      gap: var(--space-4) !important;
    }
  }
}

@media (max-width: 768px) {
  .dashboard-container {
    .welcome-card {
      padding: var(--space-6);
      
      .welcome-content {
        flex-direction: column;
        gap: var(--space-4);
      }
      
      .welcome-title {
        font-size: var(--text-2xl);
      }
    }
    
    .stats-grid {
      grid-template-columns: 1fr;
    }
    
    .quick-actions .actions-grid {
      grid-template-columns: 1fr;
    }
  }
}

@media (max-width: 480px) {
  .dashboard-container {
    .welcome-card {
      padding: var(--space-4);
    }
    
    .modern-card {
      .card-header,
      .card-body {
        padding: var(--space-4);
      }
    }
  }
}
</style> 