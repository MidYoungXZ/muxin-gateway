package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.connect.ClientConnection;
import com.muxin.gateway.core.plus.connect.ServerConnection;
import com.muxin.gateway.core.plus.message.Message;
import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.message.ServerExchange;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 默认请求上下文实现
 * 提供完整的请求生命周期管理、连接管理、路由信息管理和协议转换检测
 *
 * @author muxin
 */
@Slf4j
public class DefaultRequestContext implements RequestContext {

    // ========== 请求ID生成器 ==========
    private static final AtomicLong REQUEST_COUNTER = new AtomicLong(0);
    
    // ========== 核心标识 ==========
    private final String requestId;
    private final long startTime;
    
    // ========== 核心组件 ==========
    private final ServerExchange<? extends Message, ? extends Message> exchange;
    private volatile ServerConnection serverConnection;
    private volatile ClientConnection clientConnection;
    
    // ========== 路由信息 ==========
    private volatile Route matchedRoute;
    private volatile EndpointAddress selectedEndpoint;
    
    // ========== 状态管理 ==========
    private final AtomicBoolean completed = new AtomicBoolean(false);
    private volatile Throwable error;
    
    // ========== 属性存储 ==========
    private final Map<String, Object> attributes = new ConcurrentHashMap<>(16);
    
    // ========== 生命周期时间点 ==========
    private volatile long routeMatchedTime;
    private volatile long endpointSelectedTime;
    private volatile long completedTime;
    
    // ========== 协议转换缓存 ==========
    private volatile Boolean protocolConversionNeeded;
    
    // ========== 构造函数 ==========
    
    /**
     * 创建请求上下文
     * @param exchange 服务器交换对象
     */
    public DefaultRequestContext(ServerExchange<? extends Message, ? extends Message> exchange) {
        this.exchange = Objects.requireNonNull(exchange, "ServerExchange不能为空");
        this.startTime = System.currentTimeMillis();
        this.requestId = generateRequestId();
        
        log.debug("创建请求上下文: {} (协议: {})", requestId, exchange.protocol().type());
    }
    
    /**
     * 创建请求上下文并设置服务器连接
     * @param exchange 服务器交换对象
     * @param serverConnection 服务器连接
     */
    public DefaultRequestContext(ServerExchange<? extends Message, ? extends Message> exchange, 
                               ServerConnection serverConnection) {
        this(exchange);
        this.serverConnection = serverConnection;
    }
    
    // ========== RequestContext 接口实现 ==========
    
    @Override
    public String requestId() {
        return requestId;
    }
    
    @Override
    public ServerExchange<? extends Message, ? extends Message> exchange() {
        return exchange;
    }
    
    @Override
    public ServerConnection serverConnection() {
        return serverConnection;
    }
    
    @Override
    public void setServerConnection(ServerConnection connection) {
        this.serverConnection = connection;
        log.debug("设置服务器连接: {} -> {}", requestId, 
                connection != null ? connection.getConnectionId() : "null");
    }
    
    @Override
    public ClientConnection clientConnection() {
        return clientConnection;
    }
    
    @Override
    public void setClientConnection(ClientConnection connection) {
        this.clientConnection = connection;
        log.debug("设置客户端连接: {} -> {}", requestId, 
                connection != null ? connection.getConnectionId() : "null");
    }
    
    @Override
    public Route getMatchedRoute() {
        return matchedRoute;
    }
    
    @Override
    public void setMatchedRoute(Route route) {
        this.matchedRoute = route;
        this.routeMatchedTime = System.currentTimeMillis();
        // 重置协议转换缓存
        this.protocolConversionNeeded = null;
        
        log.debug("设置匹配路由: {} -> {}", requestId, 
                route != null ? route.getId() : "null");
    }
    
    @Override
    public EndpointAddress getSelectedEndpoint() {
        return selectedEndpoint;
    }
    
    @Override
    public void setSelectedEndpoint(EndpointAddress endpoint) {
        this.selectedEndpoint = endpoint;
        this.endpointSelectedTime = System.currentTimeMillis();
        // 重置协议转换缓存
        this.protocolConversionNeeded = null;
        
        log.debug("设置选中端点: {} -> {}", requestId, 
                endpoint != null ? endpoint.toUri() : "null");
    }
    
    @Override
    public boolean needsProtocolConversion() {
        // 使用缓存避免重复计算
        if (protocolConversionNeeded != null) {
            return protocolConversionNeeded;
        }
        
        synchronized (this) {
            if (protocolConversionNeeded != null) {
                return protocolConversionNeeded;
            }
            
            protocolConversionNeeded = calculateProtocolConversion();
            return protocolConversionNeeded;
        }
    }
    
    @Override
    public long getStartTime() {
        return startTime;
    }
    
    @Override
    public void markComplete() {
        if (completed.compareAndSet(false, true)) {
            completedTime = System.currentTimeMillis();
            
            long totalTime = completedTime - startTime;
            log.debug("请求完成: {} (耗时: {}ms)", requestId, totalTime);
            
            // 执行资源清理
            performCleanup();
            
            // 记录性能指标
            recordPerformanceMetrics(totalTime);
        } else {
            log.warn("重复标记请求完成: {}", requestId);
        }
    }
    
    @Override
    public boolean isCompleted() {
        return completed.get();
    }
    
    @Override
    public Throwable getError() {
        return error;
    }
    
    @Override
    public void setError(Throwable error) {
        this.error = error;
        
        if (error != null) {
            log.warn("请求发生错误: {} - {}", requestId, error.getMessage());
            
            // 如果设置了错误且请求未完成，自动标记完成
            if (!isCompleted()) {
                markComplete();
            }
        }
    }
    
    @Override
    public boolean hasError() {
        return error != null;
    }
    
    // ========== AttributesHolder 接口实现 ==========
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    // ========== 扩展方法 ==========
    
    /**
     * 获取路由匹配时间
     */
    public long getRouteMatchedTime() {
        return routeMatchedTime;
    }
    
    /**
     * 获取端点选择时间
     */
    public long getEndpointSelectedTime() {
        return endpointSelectedTime;
    }
    
    /**
     * 获取完成时间
     */
    public long getCompletedTime() {
        return completedTime;
    }
    
    /**
     * 计算路由匹配耗时
     */
    public long getRouteMatchingDuration() {
        return routeMatchedTime > 0 ? routeMatchedTime - startTime : -1;
    }
    
    /**
     * 计算端点选择耗时
     */
    public long getEndpointSelectionDuration() {
        return endpointSelectedTime > 0 && routeMatchedTime > 0 ? 
                endpointSelectedTime - routeMatchedTime : -1;
    }
    
    /**
     * 计算总处理时间
     */
    public long getTotalDuration() {
        return completedTime > 0 ? completedTime - startTime : 
                System.currentTimeMillis() - startTime;
    }
    
    /**
     * 检查请求是否超时
     */
    public boolean isTimeout(long timeoutMs) {
        return getTotalDuration() > timeoutMs;
    }
    
    /**
     * 获取请求状态描述
     */
    public String getStatusDescription() {
        if (hasError()) {
            return "ERROR: " + error.getMessage();
        } else if (isCompleted()) {
            return "COMPLETED";
        } else if (selectedEndpoint != null) {
            return "ENDPOINT_SELECTED";
        } else if (matchedRoute != null) {
            return "ROUTE_MATCHED";
        } else {
            return "PROCESSING";
        }
    }
    
    // ========== 内部方法 ==========
    
    /**
     * 生成唯一请求ID
     */
    private String generateRequestId() {
        long timestamp = System.currentTimeMillis();
        long nanoTime = System.nanoTime() % 10000; // 取后4位避免过长
        long counter = REQUEST_COUNTER.incrementAndGet() % 1000; // 循环计数
        
        return String.format("%d-%04d-%03d", timestamp, nanoTime, counter);
    }
    
    /**
     * 计算是否需要协议转换
     */
    private boolean calculateProtocolConversion() {
        if (matchedRoute == null || selectedEndpoint == null) {
            return false;
        }
        
        Protocol inboundProtocol = exchange.protocol();
        Protocol outboundProtocol = selectedEndpoint.getProtocol();
        
        if (inboundProtocol == null || outboundProtocol == null) {
            return false;
        }
        
        // 检查协议兼容性
        boolean compatible = isProtocolCompatible(inboundProtocol, outboundProtocol);
        
        log.debug("协议转换检测: {} -> {} = {}", 
                inboundProtocol.type(), 
                outboundProtocol.type(), 
                compatible ? "不需要" : "需要");
        
        return !compatible;
    }
    
    /**
     * 检查协议兼容性
     */
    private boolean isProtocolCompatible(Protocol inbound, Protocol outbound) {
        // 协议类型必须相同
        if (!Objects.equals(inbound.type(), outbound.type())) {
            return false;
        }
        
        // 同类型协议的版本兼容性检查
        switch (inbound.type().toUpperCase()) {
            case "HTTP":
                return isHttpVersionCompatible(inbound.getVersion(), outbound.getVersion());
            case "DUBBO":
                return isDubboVersionCompatible(inbound.getVersion(), outbound.getVersion());
            default:
                // 未知协议类型，默认需要相同版本
                return Objects.equals(inbound.getVersion(), outbound.getVersion());
        }
    }
    
    /**
     * HTTP版本兼容性检查
     */
    private boolean isHttpVersionCompatible(String inboundVersion, String outboundVersion) {
        // HTTP/1.0, HTTP/1.1, HTTP/2.0 基本兼容
        return true; // 简化实现，后续可以添加更复杂的兼容性逻辑
    }
    
    /**
     * Dubbo版本兼容性检查
     */
    private boolean isDubboVersionCompatible(String inboundVersion, String outboundVersion) {
        // Dubbo版本通常向后兼容
        return true; // 简化实现
    }
    
    /**
     * 执行资源清理
     */
    private void performCleanup() {
        try {
            // 清理非核心属性，保留重要信息用于日志和监控
            // 这里可以添加特定的清理逻辑
            
            log.debug("请求上下文资源清理完成: {}", requestId);
        } catch (Exception e) {
            log.warn("请求上下文清理异常: {} - {}", requestId, e.getMessage());
        }
    }
    
    /**
     * 记录性能指标
     */
    private void recordPerformanceMetrics(long totalTime) {
        // 这里可以集成监控系统，记录关键性能指标
        setAttribute("performance.totalTime", totalTime);
        setAttribute("performance.routeMatchingTime", getRouteMatchingDuration());
        setAttribute("performance.endpointSelectionTime", getEndpointSelectionDuration());
        setAttribute("performance.status", getStatusDescription());
        
        // 性能警告
        if (totalTime > 5000) { // 5秒
            log.warn("请求处理超时: {} ({}ms)", requestId, totalTime);
        } else if (totalTime > 1000) { // 1秒
            log.info("请求处理较慢: {} ({}ms)", requestId, totalTime);
        }
    }
    
    // ========== 调试和监控 ==========
    
    @Override
    public String toString() {
        return String.format(
                "DefaultRequestContext{requestId='%s', status='%s', protocol='%s', route='%s', endpoint='%s', duration=%dms}",
                requestId,
                getStatusDescription(),
                exchange.protocol().type(),
                matchedRoute != null ? matchedRoute.getId() : "null",
                selectedEndpoint != null ? selectedEndpoint.toUri() : "null",
                getTotalDuration()
        );
    }
    
    /**
     * 获取详细的调试信息
     */
    public String toDetailString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RequestContext Debug Info ===\n");
        sb.append("Request ID: ").append(requestId).append("\n");
        sb.append("Status: ").append(getStatusDescription()).append("\n");
        sb.append("Protocol: ").append(exchange.protocol().type()).append("\n");
        sb.append("Start Time: ").append(new java.util.Date(startTime)).append("\n");
        
        if (matchedRoute != null) {
            sb.append("Matched Route: ").append(matchedRoute.getId()).append("\n");
            sb.append("Route Matching Time: ").append(getRouteMatchingDuration()).append("ms\n");
        }
        
        if (selectedEndpoint != null) {
            sb.append("Selected Endpoint: ").append(selectedEndpoint.toUri()).append("\n");
            sb.append("Endpoint Selection Time: ").append(getEndpointSelectionDuration()).append("ms\n");
        }
        
        if (isCompleted()) {
            sb.append("Completed Time: ").append(new java.util.Date(completedTime)).append("\n");
            sb.append("Total Duration: ").append(getTotalDuration()).append("ms\n");
        }
        
        if (hasError()) {
            sb.append("Error: ").append(error.getClass().getSimpleName())
              .append(" - ").append(error.getMessage()).append("\n");
        }
        
        sb.append("Protocol Conversion Needed: ").append(needsProtocolConversion()).append("\n");
        sb.append("Attributes Count: ").append(attributes.size()).append("\n");
        sb.append("=============================");
        
        return sb.toString();
    }
} 