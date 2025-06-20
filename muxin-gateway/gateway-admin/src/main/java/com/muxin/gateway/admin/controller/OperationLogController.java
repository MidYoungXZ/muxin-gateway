package com.muxin.gateway.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.muxin.gateway.admin.model.Result;
import com.muxin.gateway.admin.model.dto.OperationLogQueryDTO;
import com.muxin.gateway.admin.model.vo.OperationLogVO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 操作日志管理控制器
 *
 * @author muxin
 */
@Slf4j
@RestController
@RequestMapping("/api/system/logs/operation")
@RequiredArgsConstructor
public class OperationLogController {
    
    private final OperationLogService operationLogService;
    
    /**
     * 分页查询操作日志
     */
    @GetMapping
    @SaCheckPermission("system:log:operation:list")
    public Result<PageVO<OperationLogVO>> listOperationLogs(OperationLogQueryDTO query) {
        return Result.success(operationLogService.pageQuery(query));
    }
    
    /**
     * 获取操作日志详情
     */
    @GetMapping("/{id}")
    @SaCheckPermission("system:log:operation:view")
    public Result<OperationLogVO> getOperationLog(@PathVariable Long id) {
        return Result.success(operationLogService.getLogDetail(id));
    }
    
    /**
     * 批量删除操作日志
     */
    @DeleteMapping
    @SaCheckPermission("system:log:operation:delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        operationLogService.batchDelete(ids);
        return Result.success();
    }
    
    /**
     * 清空所有操作日志
     */
    @DeleteMapping("/clear")
    @SaCheckPermission("system:log:operation:clear")
    public Result<Void> clearAll() {
        operationLogService.clearAll();
        return Result.success();
    }
    
    /**
     * 导出操作日志
     */
    @PostMapping("/export")
    @SaCheckPermission("system:log:operation:export")
    public Result<List<OperationLogVO>> exportLogs(@RequestBody OperationLogQueryDTO query) {
        return Result.success(operationLogService.exportLogs(query));
    }
    
    /**
     * 获取操作日志统计信息
     */
    @GetMapping("/stats")
    @SaCheckPermission("system:log:operation:stats")
    public Result<Map<String, Object>> getLogStats() {
        return Result.success(operationLogService.getLogStats());
    }
    
    /**
     * 清理历史日志
     *
     * @param days 保留天数
     */
    @PostMapping("/clean")
    @SaCheckPermission("system:log:operation:clean")
    public Result<Integer> cleanHistoryLogs(@RequestParam(defaultValue = "30") int days) {
        int cleanCount = operationLogService.cleanHistoryLogs(days);
        return Result.success(cleanCount);
    }
} 