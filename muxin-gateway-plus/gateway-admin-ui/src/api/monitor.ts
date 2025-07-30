import request from '@/utils/request'

export const monitorApi = {
  // 获取实时指标（对应后端的监控概览）
  getRealtimeMetrics() {
    console.log('🚀 [MONITOR] 获取实时指标: /api/metrics/overview')
    return request({
      url: '/api/metrics/overview',
      method: 'get'
    })
  },

  // 获取历史指标（对应后端的趋势分析）
  getHistoryMetrics(params: {
    startTime: string
    endTime: string
    interval?: string
    routeId?: string
  }) {
    console.log('🚀 [MONITOR] 获取历史指标: /api/metrics/trends')
    return request({
      url: '/api/metrics/trends',
      method: 'get',
      params
    })
  },

  // 获取系统健康状态（暂时使用测试接口）
  getHealthStatus() {
    console.log('🚀 [MONITOR] 获取健康状态: /api/test/check-user/admin')
    // 临时使用已存在的测试接口
    return request({
      url: '/api/test/check-user/admin',
      method: 'get'
    })
  },

  // 获取日志列表 - 后端暂未实现
  getLogs(params: {
    startTime?: string
    endTime?: string
    level?: string
    routeId?: string
    keyword?: string
    page?: number
    pageSize?: number
  }) {
    // 模拟数据
    return Promise.resolve({
      code: 200,
      message: 'success',
      data: {
        total: 100,
        list: Array.from({ length: 10 }, (_, i) => ({
          id: i + 1,
          timestamp: new Date().toISOString(),
          level: ['INFO', 'WARN', 'ERROR'][Math.floor(Math.random() * 3)],
          message: `示例日志消息 ${i + 1}`,
          routeId: params.routeId || 'route-001',
          details: {}
        }))
      }
    })
  },

  // 导出日志 - 后端暂未实现
  exportLogs(params: any) {
    // 模拟下载
    const blob = new Blob(['日志数据导出'], { type: 'text/plain' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `logs-export-${Date.now()}.txt`
    a.click()
    URL.revokeObjectURL(url)
    return Promise.resolve(blob)
  }
} 