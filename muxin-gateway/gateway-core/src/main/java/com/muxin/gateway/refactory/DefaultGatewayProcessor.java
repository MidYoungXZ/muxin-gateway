package com.muxin.gateway.refactory;

import java.util.concurrent.CompletableFuture;

/**
 * 默认网关处理器实现
 * 负责协调各层组件完成请求处理流程
 * 
 * @author muxin
 */
public class DefaultGatewayProcessor implements GatewayProcessor {
    
    private final RouteManager routeManager;
    private final FilterManager filterManager;
    private final LoadBalanceManager loadBalanceManager;
    private final NodeManager nodeManager;
    
    public DefaultGatewayProcessor(RouteManager routeManager, 
                                 FilterManager filterManager,
                                 LoadBalanceManager loadBalanceManager,
                                 NodeManager nodeManager) {
        this.routeManager = routeManager;
        this.filterManager = filterManager;
        this.loadBalanceManager = loadBalanceManager;
        this.nodeManager = nodeManager;
    }
    
    @Override
    public CompletableFuture<Void> processRequest(UniversalRequestContext context) {
        return CompletableFuture.runAsync(() -> {
            try {
                System.out.println("[GATEWAY_PROCESSOR] 开始处理请求: " + context.getInboundMessage().getMessageId());
                
                // 1. 应用前置过滤器
                applyPreFilters(context);
                
                // 2. 路由匹配
                UniversalRoute matchedRoute = routeManager.matchRoute(context);
                if (matchedRoute == null) {
                    throw new RuntimeException("未找到匹配的路由");
                }
                context.setMatchedRoute(matchedRoute);
                
                // 3. 应用路由过滤器
                applyRouteFilters(context, matchedRoute);
                
                // 4. 负载均衡选择目标
                selectTargetNode(context, matchedRoute);
                
                // 5. 调用后端服务
                invokeBackendService(context);
                
                System.out.println("[GATEWAY_PROCESSOR] 请求处理完成");
                
            } catch (Exception e) {
                context.setError(e);
                processError(context, e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> processResponse(UniversalRequestContext context) {
        return CompletableFuture.runAsync(() -> {
            try {
                System.out.println("[GATEWAY_PROCESSOR] 开始处理响应");
                
                // 应用后置过滤器
                applyPostFilters(context);
                
                // 标记处理完成
                context.markComplete();
                
                System.out.println("[GATEWAY_PROCESSOR] 响应处理完成");
                
            } catch (Exception e) {
                context.setError(e);
                processError(context, e);
            }
        });
    }
    
    @Override
    public void processError(UniversalRequestContext context, Exception exception) {
        System.err.println("[GATEWAY_PROCESSOR] 处理错误: " + exception.getMessage());
        
        try {
            // 应用错误过滤器
            Protocol protocol = context.getInboundProtocol();
            if (protocol != null) {
                UniversalFilterChain errorChain = filterManager.createFilterChain(protocol, FilterType.ERROR);
                errorChain.filter(context);
            }
        } catch (Exception e) {
            System.err.println("[GATEWAY_PROCESSOR] 错误过滤器执行失败: " + e.getMessage());
        }
        
        // 标记处理完成
        context.markComplete();
    }
    
    @Override
    public RouteManager getRouteManager() {
        return routeManager;
    }
    
    @Override
    public FilterManager getFilterManager() {
        return filterManager;
    }
    
    @Override
    public LoadBalanceManager getLoadBalanceManager() {
        return loadBalanceManager;
    }
    
    @Override
    public NodeManager getNodeManager() {
        return nodeManager;
    }
    
    /**
     * 应用前置过滤器
     */
    private void applyPreFilters(UniversalRequestContext context) {
        Protocol protocol = context.getInboundProtocol();
        if (protocol != null) {
            UniversalFilterChain preChain = filterManager.createFilterChain(protocol, FilterType.PRE);
            preChain.filter(context);
        }
    }
    
    /**
     * 应用路由过滤器
     */
    private void applyRouteFilters(UniversalRequestContext context, UniversalRoute route) {
        // 先应用全局路由过滤器
        Protocol protocol = context.getInboundProtocol();
        if (protocol != null) {
            UniversalFilterChain routeChain = filterManager.createFilterChain(protocol, FilterType.ROUTE);
            routeChain.filter(context);
        }
        
        // 再应用路由特定的过滤器
        for (UniversalFilter filter : route.getFilters()) {
            try {
                if (filter.isEnabled() && filter.getSupportedProtocols().contains(protocol)) {
                    filter.filter(context, null); // 单个过滤器执行
                }
            } catch (Exception e) {
                System.err.println("路由过滤器执行失败: " + filter.getName() + ", 错误: " + e.getMessage());
            }
        }
    }
    
    /**
     * 选择目标节点
     */
    private void selectTargetNode(UniversalRequestContext context, UniversalRoute route) {
        RouteTarget target = route.getTarget();
        if (target == null) {
            throw new RuntimeException("路由目标为空");
        }
        
        // 获取可用节点
        java.util.List<EndpointAddress> availableTargets = target.getTargetAddresses();
        if (availableTargets.isEmpty()) {
            throw new RuntimeException("没有可用的目标节点");
        }
        
        // 负载均衡选择
        EndpointAddress selectedTarget = loadBalanceManager.selectTarget(
            "default-service", availableTargets, context);
        
        if (selectedTarget == null) {
            throw new RuntimeException("负载均衡未能选择目标节点");
        }
        
        context.setSelectedNode(selectedTarget);
    }
    
    /**
     * 调用后端服务
     */
    private void invokeBackendService(UniversalRequestContext context) {
        EndpointAddress target = (EndpointAddress) context.getSelectedNode();
        if (target == null) {
            throw new RuntimeException("目标节点为空");
        }
        
        System.out.println("[GATEWAY_PROCESSOR] 调用后端服务: " + target.toUri());
        
        // 模拟创建连接和发送请求
        Message request = context.getInboundMessage();
        try {
            // 模拟网络延迟
            Thread.sleep(10);
            
            // 模拟创建响应消息
            Message response = request.createResponse();
            context.setOutboundMessage(response);
            
            System.out.println("[GATEWAY_PROCESSOR] 后端服务调用成功");
            
        } catch (Exception e) {
            throw new RuntimeException("后端服务调用失败", e);
        }
    }
    
    /**
     * 应用后置过滤器
     */
    private void applyPostFilters(UniversalRequestContext context) {
        Protocol protocol = context.getInboundProtocol();
        if (protocol != null) {
            UniversalFilterChain postChain = filterManager.createFilterChain(protocol, FilterType.POST);
            postChain.filter(context);
        }
    }
} 