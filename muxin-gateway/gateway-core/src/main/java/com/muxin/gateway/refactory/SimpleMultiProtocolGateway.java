package com.muxin.gateway.refactory;

import com.muxin.gateway.refactory.connect.Connection;
import com.muxin.gateway.refactory.filter.FilterManager;
import com.muxin.gateway.refactory.filter.UniversalFilterManager;
import com.muxin.gateway.refactory.loadbalance.DefaultLoadBalanceManager;
import com.muxin.gateway.refactory.loadbalance.LoadBalanceManager;
import com.muxin.gateway.refactory.message.Message;
import com.muxin.gateway.refactory.message.NodeManager;
import com.muxin.gateway.refactory.message.http.HttpProtocol;
import com.muxin.gateway.refactory.message.http.HttpProtocolAdapter;
import com.muxin.gateway.refactory.node.DefaultNodeManager;
import com.muxin.gateway.refactory.route.RouteManager;
import com.muxin.gateway.refactory.route.SimpleRouteManager;
import com.muxin.gateway.refactory.route.UniversalRequestContext;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单多协议网关实现
 * 
 * @author muxin
 */
public class SimpleMultiProtocolGateway implements MultiProtocolGateway {
    
    private final Map<Protocol, ProtocolAdapter> protocolAdapters;
    private final Map<Protocol, Object> protocolListeners;
    private final Map<Protocol, Object> protocolStats;
    private final GatewayProcessor gatewayProcessor;
    
    private volatile boolean running = false;
    
    public SimpleMultiProtocolGateway() {
        this.protocolAdapters = new ConcurrentHashMap<>();
        this.protocolListeners = new ConcurrentHashMap<>();
        this.protocolStats = new ConcurrentHashMap<>();
        
        // 初始化网关处理器
        RouteManager routeManager = new SimpleRouteManager();
        FilterManager filterManager = new UniversalFilterManager();
        LoadBalanceManager loadBalanceManager = new DefaultLoadBalanceManager();
        NodeManager nodeManager = new DefaultNodeManager();
        
        this.gatewayProcessor = new EnhancedGatewayProcessor(
            routeManager, filterManager, loadBalanceManager, nodeManager);
        
        // 注册默认协议适配器
        registerProtocolAdapter(new HttpProtocolAdapter());
    }
    
    @Override
    public CompletableFuture<Message> handleInbound(Message inboundMessage, Connection inboundConnection) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("[MULTI_PROTOCOL_GATEWAY] 处理入站消息: " + inboundMessage.getMessageId());
                
                // 创建请求上下文
                UniversalRequestContext context = new DefaultRequestContext(inboundMessage, inboundConnection);
                
                // 处理请求
                gatewayProcessor.processRequest(context).get();
                
                // 处理响应
                gatewayProcessor.processResponse(context).get();
                
                // 返回响应消息
                Message response = context.getOutboundMessage();
                if (response == null) {
                    // 创建默认响应
                    response = inboundMessage.createResponse();
                }
                
                System.out.println("[MULTI_PROTOCOL_GATEWAY] 完成处理: " + response.getMessageId());
                return response;
                
            } catch (Exception e) {
                System.err.println("[MULTI_PROTOCOL_GATEWAY] 处理失败: " + e.getMessage());
                throw new RuntimeException("网关处理失败", e);
            }
        });
    }
    
    @Override
    public Set<Protocol> getSupportedProtocols() {
        return new HashSet<>(protocolAdapters.keySet());
    }
    
    @Override
    public void registerProtocolAdapter(ProtocolAdapter adapter) {
        if (adapter != null && adapter.getSupportedProtocol() != null) {
            Protocol protocol = adapter.getSupportedProtocol();
            protocolAdapters.put(protocol, adapter);
            
            // 初始化协议统计
            protocolStats.put(protocol, createProtocolStats(protocol));
            
            System.out.println("[MULTI_PROTOCOL_GATEWAY] 注册协议适配器: " + protocol.getName());
        }
    }
    
    @Override
    public void startProtocolListener(Protocol protocol, int port, Map<String, Object> config) {
        if (!protocolAdapters.containsKey(protocol)) {
            throw new IllegalArgumentException("未注册的协议: " + protocol.getName());
        }
        
        try {
            // 创建并启动协议监听器
            ProtocolListener listener = createProtocolListener(protocol, port, config);
            listener.start();
            
            protocolListeners.put(protocol, listener);
            
            System.out.println(String.format("[MULTI_PROTOCOL_GATEWAY] 启动协议监听器: %s 端口: %d", 
                protocol.getName(), port));
                
        } catch (Exception e) {
            System.err.println("启动协议监听器失败: " + e.getMessage());
            throw new RuntimeException("启动协议监听器失败", e);
        }
    }
    
    @Override
    public void stopProtocolListener(Protocol protocol) {
        Object listener = protocolListeners.remove(protocol);
        if (listener instanceof ProtocolListener) {
            try {
                ((ProtocolListener) listener).stop();
                System.out.println("[MULTI_PROTOCOL_GATEWAY] 停止协议监听器: " + protocol.getName());
            } catch (Exception e) {
                System.err.println("停止协议监听器失败: " + e.getMessage());
            }
        }
    }
    
    @Override
    public Map<Protocol, Object> getProtocolStats() {
        return new HashMap<>(protocolStats);
    }
    
    @Override
    public void start() {
        if (running) {
            return;
        }
        
        System.out.println("[MULTI_PROTOCOL_GATEWAY] 启动网关");
        
        // 启动默认HTTP监听器
        Protocol httpProtocol = new HttpProtocol();
        Map<String, Object> httpConfig = new HashMap<>();
        httpConfig.put("maxConnections", 1000);
        httpConfig.put("readTimeout", 30000);
        
        startProtocolListener(httpProtocol, 8080, httpConfig);
        
        running = true;
        System.out.println("[MULTI_PROTOCOL_GATEWAY] 网关启动完成");
    }
    
    @Override
    public void stop() {
        if (!running) {
            return;
        }
        
        System.out.println("[MULTI_PROTOCOL_GATEWAY] 停止网关");
        
        // 停止所有协议监听器
        for (Protocol protocol : new HashSet<>(protocolListeners.keySet())) {
            stopProtocolListener(protocol);
        }
        
        // 关闭节点管理器
        if (gatewayProcessor.getNodeManager() instanceof DefaultNodeManager) {
            gatewayProcessor.getNodeManager().stop();
        }
        
        running = false;
        System.out.println("[MULTI_PROTOCOL_GATEWAY] 网关停止完成");
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    /**
     * 获取网关处理器
     */
    public GatewayProcessor getGatewayProcessor() {
        return gatewayProcessor;
    }
    
    /**
     * 获取协议适配器
     */
    public ProtocolAdapter getProtocolAdapter(Protocol protocol) {
        return protocolAdapters.get(protocol);
    }
    
    /**
     * 创建协议监听器
     */
    private ProtocolListener createProtocolListener(Protocol protocol, int port, Map<String, Object> config) {
        return new DefaultProtocolListener(protocol, port, config, this);
    }
    
    /**
     * 创建协议统计信息
     */
    private Map<String, Object> createProtocolStats(Protocol protocol) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("protocol", protocol.getName());
        stats.put("version", protocol.getVersion());
        stats.put("requestCount", 0L);
        stats.put("responseCount", 0L);
        stats.put("errorCount", 0L);
        stats.put("avgResponseTime", 0.0);
        return stats;
    }
    
    /**
     * 协议监听器接口
     */
    public interface ProtocolListener {
        void start() throws Exception;
        void stop() throws Exception;
        Protocol getProtocol();
        int getPort();
        boolean isRunning();
    }
    
    /**
     * 默认协议监听器实现
     */
    private static class DefaultProtocolListener implements ProtocolListener {
        private final Protocol protocol;
        private final int port;
        private final Map<String, Object> config;
        private final SimpleMultiProtocolGateway gateway;
        private volatile boolean running = false;
        
        public DefaultProtocolListener(Protocol protocol, int port, 
                                     Map<String, Object> config, 
                                     SimpleMultiProtocolGateway gateway) {
            this.protocol = protocol;
            this.port = port;
            this.config = config;
            this.gateway = gateway;
        }
        
        @Override
        public void start() throws Exception {
            if (running) {
                return;
            }
            
            // 模拟启动监听器
            System.out.println(String.format("[PROTOCOL_LISTENER] 启动 %s 监听器，端口: %d", 
                protocol.getName(), port));
            
            running = true;
        }
        
        @Override
        public void stop() throws Exception {
            if (!running) {
                return;
            }
            
            System.out.println(String.format("[PROTOCOL_LISTENER] 停止 %s 监听器", protocol.getName()));
            running = false;
        }
        
        @Override
        public Protocol getProtocol() {
            return protocol;
        }
        
        @Override
        public int getPort() {
            return port;
        }
        
        @Override
        public boolean isRunning() {
            return running;
        }
    }
} 