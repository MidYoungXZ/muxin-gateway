package com.muxin.gateway.admin.service;

import com.mybatisflex.core.service.IService;
import com.muxin.gateway.admin.entity.SysOperationLog;
import com.muxin.gateway.admin.model.dto.OperationLogQueryDTO;
import com.muxin.gateway.admin.model.vo.OperationLogVO;
import com.muxin.gateway.admin.model.vo.PageVO;

import java.util.List;
import java.util.Map;

/**
 * 操作日志服务接口
 *
 * @author muxin
 */
public interface OperationLogService extends IService<SysOperationLog> {
    
    /**
     * 分页查询操作日志
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageVO<OperationLogVO> pageQuery(OperationLogQueryDTO query);
    
    /**
     * 获取操作日志详情
     *
     * @param id 日志ID
     * @return 日志详情
     */
    OperationLogVO getLogDetail(Long id);
    
    /**
     * 记录操作日志
     *
     * @param module 模块名称
     * @param operation 操作类型
     * @param method 请求方法
     * @param requestUrl 请求URL
     * @param params 请求参数
     * @param result 返回结果
     * @param error 异常信息
     * @param duration 执行时长
     * @param status 操作状态
     */
    void recordLog(String module, String operation, String method, String requestUrl,
                   String params, String result, String error, Long duration, Integer status);
    
    /**
     * 批量删除操作日志
     *
     * @param ids 日志ID列表
     */
    void batchDelete(List<Long> ids);
    
    /**
     * 清空操作日志
     */
    void clearAll();
    
    /**
     * 导出操作日志
     *
     * @param query 查询条件
     * @return 导出数据
     */
    List<OperationLogVO> exportLogs(OperationLogQueryDTO query);
    
    /**
     * 获取操作日志统计
     *
     * @return 统计数据
     */
    Map<String, Object> getLogStats();
    
    /**
     * 根据天数清理历史日志
     *
     * @param days 保留天数
     * @return 清理数量
     */
    int cleanHistoryLogs(int days);
} 