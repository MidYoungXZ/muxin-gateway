import request from '@/utils/request'
import type { OperationLogQueryParams, OperationLog } from '@/types/system'

export const operationLogApi = {
  // 分页查询操作日志
  getOperationLogs(params: OperationLogQueryParams) {
    console.log('🚀 [OPERATION_LOG] 获取操作日志列表:', params)
    return request({
      url: '/api/system/logs/operation',
      method: 'get',
      params
    })
  },

  // 获取操作日志详情
  getOperationLogDetail(id: number) {
    console.log('🚀 [OPERATION_LOG] 获取操作日志详情:', id)
    return request({
      url: `/api/system/logs/operation/${id}`,
      method: 'get'
    })
  },

  // 批量删除操作日志
  batchDeleteOperationLogs(ids: number[]) {
    console.log('🚀 [OPERATION_LOG] 批量删除操作日志:', ids)
    return request({
      url: '/api/system/logs/operation',
      method: 'delete',
      data: ids
    })
  },

  // 清空操作日志
  clearAllOperationLogs() {
    console.log('🚀 [OPERATION_LOG] 清空所有操作日志')
    return request({
      url: '/api/system/logs/operation/clear',
      method: 'delete'
    })
  },

  // 导出操作日志
  exportOperationLogs(params: OperationLogQueryParams) {
    console.log('🚀 [OPERATION_LOG] 导出操作日志:', params)
    return request({
      url: '/api/system/logs/operation/export',
      method: 'post',
      data: params,
      responseType: 'blob'
    })
  },

  // 获取操作日志统计
  getOperationLogStats() {
    console.log('🚀 [OPERATION_LOG] 获取操作日志统计')
    return request({
      url: '/api/system/logs/operation/stats',
      method: 'get'
    })
  },

  // 清理历史日志
  cleanHistoryLogs(days: number = 30) {
    console.log('🚀 [OPERATION_LOG] 清理历史日志:', days)
    return request({
      url: `/api/system/logs/operation/clean?days=${days}`,
      method: 'post'
    })
  }
} 