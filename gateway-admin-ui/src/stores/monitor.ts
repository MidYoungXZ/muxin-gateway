import { defineStore } from 'pinia'
// 使用自动导入的 ref
import type { RealtimeStats } from '@/types/monitor'

// 模拟数据模式
const MOCK_MODE = true

export const useMonitorStore = defineStore('monitor', () => {
  const realtimeStats = ref<RealtimeStats | null>(null)
  const ws = ref<WebSocket | null>(null)
  
  // 生成模拟数据
  const generateMockData = (): RealtimeStats => {
    return {
      timestamp: Date.now(),
      metrics: {
        requestsPerSecond: Math.floor(Math.random() * 1000) + 100,
        totalRequests: Math.floor(Math.random() * 1000000) + 100000,
        avgResponseTime: Math.floor(Math.random() * 200) + 50,
        errorRate: Math.random() * 5,
        activeConnections: Math.floor(Math.random() * 500) + 100
      },
      statusCodeDistribution: {
        '2xx': Math.floor(Math.random() * 10000) + 5000,
        '3xx': Math.floor(Math.random() * 1000) + 100,
        '4xx': Math.floor(Math.random() * 500) + 50,
        '5xx': Math.floor(Math.random() * 100) + 10
      }
    }
  }
  
  const connectWebSocket = () => {
    if (MOCK_MODE) {
      // 模拟WebSocket数据更新
      const updateData = () => {
        realtimeStats.value = generateMockData()
      }
      
      // 初始化数据
      updateData()
      
      // 每秒更新一次
      const interval = setInterval(updateData, 1000)
      
      // 模拟 WebSocket 对象，保存 interval 以便清理
      ws.value = {
        close: () => {
          clearInterval(interval)
        }
      } as any
      
      return
    }
    
    const wsUrl = import.meta.env.VITE_WS_URL + '/monitor'
    ws.value = new WebSocket(wsUrl)
    
    ws.value.onmessage = (event: any) => {
      const data = JSON.parse(event.data)
      realtimeStats.value = data
    }
    
    ws.value.onclose = () => {
      // 断线重连
      setTimeout(() => {
        if (ws.value?.readyState === WebSocket.CLOSED) {
          connectWebSocket()
        }
      }, 5000)
    }
    
    ws.value.onerror = (error: any) => {
      console.error('WebSocket error:', error)
    }
  }
  
  const disconnectWebSocket = () => {
    if (ws.value) {
      ws.value.close()
      ws.value = null
    }
  }
  
  return {
    realtimeStats,
    connectWebSocket,
    disconnectWebSocket
  }
}) 