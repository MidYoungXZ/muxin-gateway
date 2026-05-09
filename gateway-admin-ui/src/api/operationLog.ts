import request from '@/utils/request'
import type { OperationLogQueryParams, OperationLog } from '@/types/system'

export const operationLogApi = {
  // 分页查询操作日志
  getOperationLogs(params: OperationLogQueryParams) {
    return request({
      url: '/api/system/logs/operation',
      method: 'get',
      params
    })
  },

  // 获取操作日志详情
  getOperationLogDetail(id: number) {
    return request({
      url: `/api/system/logs/operation/${id}`,
      method: 'get'
    })
  },

  // 批量删除操作日志
  batchDeleteOperationLogs(ids: number[]) {
    return request({
      url: '/api/system/logs/operation',
      method: 'delete',
      data: ids
    })
  },

  // 清空操作日志
  clearAllOperationLogs() {
    return request({
      url: '/api/system/logs/operation/clear',
      method: 'delete'
    })
  },

  // 导出操作日志
  exportOperationLogs(params: OperationLogQueryParams) {
    return request({
      url: '/api/system/logs/operation/export',
      method: 'post',
      data: params,
      responseType: 'blob'
    })
  },

  // 获取操作日志统计
  getOperationLogStats() {
    return request({
      url: '/api/system/logs/operation/stats',
      method: 'get'
    })
  },

  // 清理历史日志
  cleanHistoryLogs(days: number = 30) {
    return request({
      url: `/api/system/logs/operation/clean?days=${days}`,
      method: 'post'
    })
  }
} 