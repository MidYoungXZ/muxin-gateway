package com.muxin.gateway.refactory;

import com.muxin.gateway.refactory.connect.ConnectionFactoryManager;
import com.muxin.gateway.refactory.filter.FilterManager;
import com.muxin.gateway.refactory.loadbalance.LoadBalanceManager;
import com.muxin.gateway.refactory.message.Message;
import com.muxin.gateway.refactory.message.MessageType;
import com.muxin.gateway.refactory.message.Protocol;
import com.muxin.gateway.refactory.message.ProtocolConverterManager;
import com.muxin.gateway.refactory.node.EndpointAddress;
import com.muxin.gateway.refactory.node.NodeManager;
import com.muxin.gateway.refactory.route.RouteManager;
import com.muxin.gateway.refactory.route.UniversalRequestContext;
import com.muxin.gateway.refactory.route.UniversalRoute;
import lombok.extern.slf4j.Slf4j;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.Collections;
import java.util.List;

/**
 * 重构后的网关处理器实现
 * 继承 GatewayProcessor 抽象类，使用模板方法定义的处理流程
 *
 * @author muxin
 */
@Slf4j
public class EnhancedGatewayProcessor extends GatewayProcessor {

    public EnhancedGatewayProcessor(RouteManager routeManager,
                                    FilterManager filterManager,
                                    LoadBalanceManager loadBalanceManager,
                                    NodeManager nodeManager,
                                    ProtocolConverterManager converterManager,
                                    ConnectionFactoryManager connectionFactoryManager) {
        super(routeManager, filterManager, loadBalanceManager, nodeManager, 
              converterManager, connectionFactoryManager);
        
        log.info("[EnhancedGatewayProcessor] 网关处理器初始化完成");
    }
    
    @Override
    protected Protocol detectProtocol(Object protocolSpecific, UniversalRequestContext context) {
        if (protocolSpecific instanceof FullHttpRequest) {
            return new Protocol.HttpProtocol();
        }
        throw new GatewayException("不支持的协议类型: " + protocolSpecific.getClass().getName());
    }
    
    @Override
    protected Protocol determineTargetProtocol(UniversalRequestContext context) {
        // 对于HTTP网关，通常返回HTTP协议
        // 可以根据路由配置或其他条件来决定目标协议
        return new Protocol.HttpProtocol();
    }

    @Override
    public UniversalRoute matchRoute(UniversalRequestContext context) {
        if (routeManager == null) {
            throw new GatewayException("RouteManager 未配置");
        }
        
        UniversalRoute route = routeManager.matchRoute(context);
        log.debug("[EnhancedGatewayProcessor] 路由匹配结果: {}", 
            route != null ? route.getId() : "无匹配路由");
        return route;
    }

    @Override
    public EndpointAddress selectTargetNode(UniversalRequestContext context, UniversalRoute route) {
        if (loadBalanceManager == null) {
            throw new GatewayException("LoadBalanceManager 未配置");
        }
        
        List<EndpointAddress> targets = getRouteTargets(route);
        if (targets.isEmpty()) {
            throw new GatewayException("路由 " + route.getId() + " 没有可用的目标节点");
        }
        
        EndpointAddress selected = loadBalanceManager.selectTarget(route.getId(), targets, context);
        log.debug("[EnhancedGatewayProcessor] 负载均衡选择节点: {}", 
            selected != null ? selected.toUri() : "null");
        return selected;
    }

    @Override
    public Message invokeBackendService(UniversalRequestContext context, EndpointAddress target, Message request) {
        try {
            log.debug("[EnhancedGatewayProcessor] 调用后端服务: {} -> {}", 
                request.getMessageId(), target.toUri());
            
            Message response = createMockResponse(request);
            log.debug("[EnhancedGatewayProcessor] 后端服务调用完成");
            return response;

        } catch (Exception e) {
            throw new GatewayException("后端服务调用失败: " + e.getMessage(), e);
        }
    }
    

    
    @Override
    public Message createErrorResponse(UniversalRequestContext context, ErrorType errorType, Throwable ex) {
        String errorMessage = buildErrorMessage(errorType, ex);
        int statusCode = getStatusCode(errorType);
        
        Message errorResponse = createSimpleResponse(statusCode, errorMessage);
        log.debug("[EnhancedGatewayProcessor] 创建错误响应 - Type: {}, Code: {}", 
            errorType, statusCode);
        return errorResponse;
    }
    
    // 组件访问器方法从父类继承
    
    // 辅助方法
    private Message createMessageFromHttpRequest(FullHttpRequest request, UniversalRequestContext context) {
        return new SimpleMessage(
            "msg-" + System.nanoTime(),
            MessageType.REQUEST,
            new Protocol.HttpProtocol(),
            request.uri(),
            extractRequestBody(request)
        );
    }
    
    private Message createMockResponse(Message request) {
        return new SimpleMessage(
            "resp-" + request.getMessageId(),
            MessageType.RESPONSE,
            request.getProtocol(),
            "/mock/response",
            "{\"status\":\"success\",\"message\":\"Mock response from enhanced gateway\"}"
        );
    }
    
    private Message createSimpleResponse(int statusCode, String content) {
        return new SimpleMessage(
            "err-" + System.nanoTime(),
            MessageType.RESPONSE,
            new Protocol.HttpProtocol(),
            "/error",
            content
        );
    }
    
    private List<EndpointAddress> getRouteTargets(UniversalRoute route) {
        if (route.getTarget() != null && route.getTarget().getTargetAddresses() != null) {
            return route.getTarget().getTargetAddresses();
        }
        return Collections.emptyList();
    }
    
    private String extractRequestBody(FullHttpRequest request) {
        if (request.content().readableBytes() > 0) {
            byte[] bytes = new byte[request.content().readableBytes()];
            request.content().readBytes(bytes);
            return new String(bytes);
        }
        return "";
    }
    
    private String buildErrorMessage(ErrorType errorType, Throwable ex) {
        switch (errorType) {
            case ROUTE_NOT_FOUND:
                return "路由未找到";
            case TARGET_NOT_FOUND:
                return "目标节点未找到";
            case LOAD_BALANCE_FAILED:
                return "负载均衡失败";
            case BACKEND_INVOCATION_FAILED:
                return "后端服务调用失败: " + ex.getMessage();
            default:
                return "网关内部错误: " + ex.getMessage();
        }
    }
    
    private int getStatusCode(ErrorType errorType) {
        switch (errorType) {
            case ROUTE_NOT_FOUND:
            case TARGET_NOT_FOUND:
                return 404;
            case LOAD_BALANCE_FAILED:
            case BACKEND_INVOCATION_FAILED:
                return 502;
            default:
                return 500;
        }
    }
    
    // 简单的 Message 实现
    private static class SimpleMessage implements Message {
        private final String messageId;
        private final MessageType type;
        private final Protocol protocol;
        private final String path;
        private final String content;
        
        public SimpleMessage(String messageId, MessageType type, Protocol protocol, String path, String content) {
            this.messageId = messageId;
            this.type = type;
            this.protocol = protocol;
            this.path = path;
            this.content = content;
        }
        
        @Override
        public String getMessageId() {
            return messageId;
        }
        
        @Override
        public MessageType getType() {
            return type;
        }
        
        @Override
        public Protocol getProtocol() {
            return protocol;
        }
        
        @Override
        public com.muxin.gateway.refactory.message.MessageHeaders getHeaders() {
            return new SimpleMessageHeaders();
        }
        
        @Override
        public com.muxin.gateway.refactory.message.MessageBody getBody() {
            return new SimpleMessageBody(content);
        }
        
        @Override
        public com.muxin.gateway.refactory.message.MessageMetadata getMetadata() {
            return null;
        }
        
        @Override
        public Message createResponse() {
            return new SimpleMessage(
                "resp-" + messageId,
                MessageType.RESPONSE,
                protocol,
                path,
                "{\"response\":\"created from " + messageId + "\"}"
            );
        }
        
        @Override
        public Message copy() {
            return new SimpleMessage(messageId, type, protocol, path, content);
        }
    }
    
    // 简单的 MessageHeaders 实现
    private static class SimpleMessageHeaders implements com.muxin.gateway.refactory.message.MessageHeaders {
        private final java.util.Map<String, Object> headers = new java.util.HashMap<>();
        
        @Override
        public void set(String name, Object value) {
            headers.put(name, value);
        }
        
        @Override
        public <T> T get(String name, Class<T> type) {
            Object value = headers.get(name);
            return type.isInstance(value) ? type.cast(value) : null;
        }

    @Override
        public <T> java.util.Optional<T> getOptional(String name, Class<T> type) {
            return java.util.Optional.ofNullable(get(name, type));
    }

    @Override
        public boolean contains(String name) {
            return headers.containsKey(name);
    }

    @Override
        public void remove(String name) {
            headers.remove(name);
    }

    @Override
        public java.util.Set<String> getNames() {
            return headers.keySet();
        }
        
        @Override
        public java.util.Map<String, Object> asMap() {
            return new java.util.HashMap<>(headers);
        }
        
        @Override
        public void setProtocolHeaders(java.util.Map<String, Object> headers) {
            this.headers.putAll(headers);
        }
        
        @Override
        public java.util.Map<String, Object> getProtocolHeaders() {
            return asMap();
        }
    }
    
    // 简单的 MessageBody 实现
    private static class SimpleMessageBody implements com.muxin.gateway.refactory.message.MessageBody {
        private final String content;
        
        public SimpleMessageBody(String content) {
            this.content = content != null ? content : "";
        }
        
        @Override
        public byte[] getBytes() {
            return content.getBytes();
        }
        
        @Override
        public String getString() {
            return content;
        }
        
        @Override
        public <T> T getContent(Class<T> type) {
            if (type == String.class) {
                return type.cast(content);
            }
            return null;
        }
        
        @Override
        public java.io.InputStream getInputStream() {
            return new java.io.ByteArrayInputStream(getBytes());
        }
        
        @Override
        public boolean isEmpty() {
            return content.isEmpty();
        }
        
        @Override
        public long getContentLength() {
            return content.length();
        }
        
        @Override
        public String getContentType() {
            return "application/json";
        }
        
        @Override
        public boolean isStreaming() {
            return false;
        }
    }
} 