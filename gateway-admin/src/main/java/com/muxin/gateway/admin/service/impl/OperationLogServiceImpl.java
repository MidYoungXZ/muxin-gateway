package com.muxin.gateway.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.muxin.gateway.admin.entity.SysOperationLog;
import com.muxin.gateway.admin.mapper.OperationLogMapper;
import com.muxin.gateway.admin.model.dto.OperationLogQueryDTO;
import com.muxin.gateway.admin.model.vo.OperationLogVO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



/**
 * 操作日志服务实现
 *
 * @author muxin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, SysOperationLog> implements OperationLogService {

    @Override
    public PageVO<OperationLogVO> pageQuery(OperationLogQueryDTO query) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where("deleted = 0")
                .orderBy("operate_time DESC");
        
        // 添加查询条件
        if (StringUtils.hasText(query.getModule())) {
            wrapper.and("module LIKE CONCAT('%', ?, '%')", query.getModule());
        }
        if (StringUtils.hasText(query.getOperation())) {
            wrapper.and("operation LIKE CONCAT('%', ?, '%')", query.getOperation());
        }
        if (StringUtils.hasText(query.getOperator())) {
            wrapper.and("operator LIKE CONCAT('%', ?, '%')", query.getOperator());
        }
        if (query.getStatus() != null) {
            wrapper.and("status = ?", query.getStatus());
        }
        if (StringUtils.hasText(query.getMethod())) {
            wrapper.and("method = ?", query.getMethod());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and("(operator LIKE CONCAT('%', ?, '%') OR module LIKE CONCAT('%', ?, '%') OR operation LIKE CONCAT('%', ?, '%'))", 
                    query.getKeyword(), query.getKeyword(), query.getKeyword());
        }
        
        // 时间范围查询
        if (StringUtils.hasText(query.getStartTime())) {
            wrapper.and("operate_time >= ?", LocalDateTime.parse(query.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (StringUtils.hasText(query.getEndTime())) {
            wrapper.and("operate_time <= ?", LocalDateTime.parse(query.getEndTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        Page<SysOperationLog> page = Page.of(query.getPageNum(), query.getPageSize());
        Page<SysOperationLog> logPage = page(page, wrapper);
        
        List<OperationLogVO> logVOs = logPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageVO.<OperationLogVO>builder()
                .data(logVOs)
                .total(logPage.getTotalRow())
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize())
                .totalPages((int) Math.ceil((double) logPage.getTotalRow() / query.getPageSize()))
                .hasNext(logPage.getTotalRow() > (long) query.getPageNum() * query.getPageSize())
                .build();
    }
    
    @Override
    public OperationLogVO getLogDetail(Long id) {
        SysOperationLog log = getById(id);
        if (log == null || log.getDeleted() == 1) {
            throw new RuntimeException("操作日志不存在");
        }
        return convertToVO(log);
    }
    
    @Override
    public void recordLog(String module, String operation, String method, String requestUrl,
                         String params, String result, String error, Long duration, Integer status) {
        try {
            SysOperationLog opLog = new SysOperationLog();
            opLog.setModule(module);
            opLog.setOperation(operation);
            opLog.setMethod(method);
            opLog.setRequestUrl(requestUrl);
            opLog.setParams(params);
            opLog.setResult(result);
            opLog.setError(error);
            opLog.setDuration(duration);
            opLog.setStatus(status);
            opLog.setOperateTime(LocalDateTime.now());
            opLog.setDeleted(0);
            
            // 获取当前用户信息
            try {
                if (StpUtil.isLogin()) {
                    opLog.setOperatorId(StpUtil.getLoginIdAsLong());
                    opLog.setOperator(StpUtil.getTokenSession().getString("username"));
                }
            } catch (Exception e) {
                opLog.setOperator("系统");
            }
            
            // 获取请求信息
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    opLog.setOperatorIp(getClientIpAddr(request));
                    opLog.setOperatorLocation("未知");
                    
                    String userAgent = request.getHeader("User-Agent");
                    if (StringUtils.hasText(userAgent)) {
                        opLog.setBrowser(getBrowser(userAgent));
                        opLog.setOs(getOs(userAgent));
                    }
                }
            } catch (Exception e) {
                log.debug("获取请求信息失败", e);
            }
            
            save(opLog);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        // 批量逻辑删除
        for (Long id : ids) {
            SysOperationLog log = getById(id);
            if (log != null) {
                log.setDeleted(1);
                updateById(log);
            }
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearAll() {
        QueryWrapper wrapper = QueryWrapper.create().where("deleted = 0");
        List<SysOperationLog> logs = list(wrapper);
        for (SysOperationLog log : logs) {
            log.setDeleted(1);
            updateById(log);
        }
    }
    
    @Override
    public List<OperationLogVO> exportLogs(OperationLogQueryDTO query) {
        query.setPageNum(1);
        query.setPageSize(Integer.MAX_VALUE);
        
        PageVO<OperationLogVO> result = pageQuery(query);
        return result.getData();
    }
    
    @Override
    public Map<String, Object> getLogStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalCount = count(QueryWrapper.create().where("deleted = 0"));
        stats.put("totalCount", totalCount);
        
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long todayCount = count(QueryWrapper.create()
                .where("deleted = 0")
                .and("operate_time >= ?", todayStart));
        stats.put("todayCount", todayCount);
        
        long successCount = count(QueryWrapper.create()
                .where("deleted = 0")
                .and("status = ?", 1));
        double successRate = totalCount > 0 ? (double) successCount / totalCount * 100 : 0;
        stats.put("successRate", Math.round(successRate * 100.0) / 100.0);
        
        long failureCount = totalCount - successCount;
        stats.put("failureCount", failureCount);
        
        return stats;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanHistoryLogs(int days) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(days);
        
        QueryWrapper wrapper = QueryWrapper.create()
                .where("operate_time < ?", cutoffTime);
        
        List<SysOperationLog> oldLogs = list(wrapper);
        if (!oldLogs.isEmpty()) {
            remove(wrapper);
        }
        
        return oldLogs.size();
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    /**
     * 获取浏览器信息
     */
    private String getBrowser(String userAgent) {
        if (userAgent == null) return "未知";
        
        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) return "Safari";
        if (userAgent.contains("Edge")) return "Edge";
        if (userAgent.contains("Opera")) return "Opera";
        
        return "其他";
    }
    
    /**
     * 获取操作系统信息
     */
    private String getOs(String userAgent) {
        if (userAgent == null) return "未知";
        
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Mac OS")) return "macOS";
        if (userAgent.contains("Linux")) return "Linux";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) return "iOS";
        
        return "其他";
    }
    
    /**
     * 转换为VO
     */
    private OperationLogVO convertToVO(SysOperationLog log) {
        OperationLogVO vo = new OperationLogVO();
        BeanUtils.copyProperties(log, vo);
        vo.setStatusText(log.getStatus() == 1 ? "成功" : "失败");
        return vo;
    }
}
