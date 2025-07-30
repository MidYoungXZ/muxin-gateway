export interface RealtimeStats {
  timestamp: number
  metrics: {
    requestsPerSecond: number
    totalRequests: number
    avgResponseTime: number
    errorRate: number
    activeConnections: number
  }
  statusCodeDistribution: {
    '2xx': number
    '3xx': number
    '4xx': number
    '5xx': number
  }
}

export interface RouteMetrics {
  routeId: string
  routeName: string
  callCount: number
  avgResponseTime: number
  p95ResponseTime: number
  p99ResponseTime: number
  errorCount: number
  errorRate: number
  lastCallTime: Date
}

export interface FilterMetrics {
  filterId: string
  filterName: string
  filterType: 'PRE' | 'POST' | 'ERROR'
  hitCount: number
  blockCount: number
  avgProcessTime: number
  lastHitTime: Date
}

export interface MonitorData {
  requestCount: number
  avgResponseTime: number
  errorRate: number
  statusDistribution: {
    '2xx': number
    '4xx': number
    '5xx': number
  }
} 