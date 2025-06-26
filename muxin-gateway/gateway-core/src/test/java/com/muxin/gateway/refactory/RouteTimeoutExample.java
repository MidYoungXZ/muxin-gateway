package com.muxin.gateway.refactory;

import com.muxin.gateway.refactory.message.http.HttpProtocol;
import com.muxin.gateway.refactory.route.*;

import java.time.Duration;
import java.util.Arrays;

/**
 * 路由超时配置使用示例
 * 演示方案A：配置驱动实现的使用方法
 *
 * @author muxin
 */
public class RouteTimeoutExample {
    
    public static void main(String[] args) {
        demonstrateRouteTimeoutConfig();
    }
    
    /**
     * 演示路由超时配置的使用
     */
    public static void demonstrateRouteTimeoutConfig() {
        System.out.println("=== 路由超时配置示例 ===\n");
        
        // 示例1：使用构建器创建快速API路由
        UniversalRoute fastApiRoute = createFastApiRoute();
        printRouteTimeoutInfo("快速API路由", fastApiRoute);
        
        // 示例2：使用构建器创建慢速API路由
        UniversalRoute slowApiRoute = createSlowApiRoute();
        printRouteTimeoutInfo("慢速API路由", slowApiRoute);
        
        // 示例3：使用构建器创建文件上传路由
        UniversalRoute fileUploadRoute = createFileUploadRoute();
        printRouteTimeoutInfo("文件上传路由", fileUploadRoute);
        
        // 示例4：使用RouteConfig创建自定义超时路由
        UniversalRoute customRoute = createCustomTimeoutRoute();
        printRouteTimeoutInfo("自定义超时路由", customRoute);
        
        // 示例5：验证超时配置合理性
        validateTimeoutConfig();
    }
    
    /**
     * 创建快速API路由（低延迟配置）
     */
    private static UniversalRoute createFastApiRoute() {
        RouteTarget target = new SimpleRouteTarget("http://fast-api.example.com");
        
        return UniversalRouteBuilder.create()
                .id("fast-api-route")
                .name("快速API路由")
                .description("用于快速响应的API服务")
                .addProtocol(new HttpProtocol())
                .target(target)
                .fastApi() // 使用预设的快速API超时配置
                .build();
    }
    
    /**
     * 创建慢速API路由（高延迟配置）
     */
    private static UniversalRoute createSlowApiRoute() {
        RouteTarget target = new SimpleRouteTarget("http://slow-api.example.com");
        
        return UniversalRouteBuilder.create()
                .id("slow-api-route")
                .name("慢速API路由")
                .description("用于处理时间较长的API服务")
                .addProtocol(new HttpProtocol())
                .target(target)
                .slowApi() // 使用预设的慢速API超时配置
                .build();
    }
    
    /**
     * 创建文件上传路由
     */
    private static UniversalRoute createFileUploadRoute() {
        RouteTarget target = new SimpleRouteTarget("http://file-service.example.com");
        
        return UniversalRouteBuilder.create()
                .id("file-upload-route")
                .name("文件上传路由")
                .description("用于处理文件上传的服务")
                .addProtocol(new HttpProtocol())
                .target(target)
                .fileUploadApi() // 使用预设的文件上传超时配置
                .build();
    }
    
    /**
     * 创建自定义超时路由
     */
    private static UniversalRoute createCustomTimeoutRoute() {
        RouteTarget target = new SimpleRouteTarget("http://custom-api.example.com");
        
        return UniversalRouteBuilder.create()
                .id("custom-timeout-route")
                .name("自定义超时路由")
                .description("使用自定义超时配置的路由")
                .addProtocol(new HttpProtocol())
                .target(target)
                // 自定义超时配置
                .connectionTimeout(Duration.ofSeconds(3))
                .requestTimeout(Duration.ofSeconds(15))
                .totalTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(20))
                .writeTimeout(Duration.ofSeconds(5))
                .circuitBreakerTimeout(Duration.ofSeconds(90))
                .build();
    }
    
    /**
     * 演示使用RouteConfig直接创建路由
     */
    private static UniversalRoute createRouteWithConfig() {
        RouteTarget target = new SimpleRouteTarget("http://config-api.example.com");
        
        RouteConfig config = RouteConfig.builder()
                .id("config-route")
                .name("配置驱动路由")
                .description("使用RouteConfig创建的路由")
                .supportedProtocols(Arrays.asList(new HttpProtocol()))
                .target(target)
                .connectionTimeout(Duration.ofSeconds(8))
                .requestTimeout(Duration.ofSeconds(25))
                .totalTimeout(Duration.ofSeconds(50))
                .readTimeout(Duration.ofSeconds(25))
                .writeTimeout(Duration.ofSeconds(8))
                .circuitBreakerTimeout(Duration.ofSeconds(75))
                .timeoutEnabled(true)
                .build();
        
        return new ConfigurableUniversalRoute(config);
    }
    
    /**
     * 打印路由超时信息
     */
    private static void printRouteTimeoutInfo(String routeType, UniversalRoute route) {
        System.out.println("--- " + routeType + " ---");
        System.out.println("路由ID: " + route.getId());
        System.out.println("路由名称: " + route.getName());
        System.out.println("连接超时: " + route.getConnectionTimeout());
        System.out.println("请求超时: " + route.getRequestTimeout());
        System.out.println("总超时: " + route.getTotalTimeout());
        System.out.println("读取超时: " + route.getReadTimeout());
        System.out.println("写入超时: " + route.getWriteTimeout());
        System.out.println("熔断器超时: " + route.getCircuitBreakerTimeout());
        System.out.println("超时控制启用: " + route.isTimeoutEnabled());
        
        // 演示获取指定类型的超时
        System.out.println("指定类型超时 - 连接: " + route.getTimeout(TimeoutType.CONNECTION));
        System.out.println("指定类型超时 - 请求: " + route.getTimeout(TimeoutType.REQUEST));
        
        System.out.println();
    }
    
    /**
     * 验证超时配置合理性
     */
    private static void validateTimeoutConfig() {
        System.out.println("--- 超时配置验证 ---");
        
        // 创建一个配置不合理的路由（连接超时大于请求超时）
        try {
            RouteTarget target = new SimpleRouteTarget("http://test.example.com");
            
            UniversalRoute invalidRoute = UniversalRouteBuilder.create()
                    .id("invalid-route")
                    .name("配置不合理的路由")
                    .addProtocol(new HttpProtocol())
                    .target(target)
                    .connectionTimeout(Duration.ofSeconds(60)) // 连接超时60秒
                    .requestTimeout(Duration.ofSeconds(30))    // 请求超时30秒（不合理）
                    .totalTimeout(Duration.ofSeconds(90))
                    .build();
            
            // 对于ConfigurableUniversalRoute，我们可以验证配置
            if (invalidRoute instanceof ConfigurableUniversalRoute) {
                ConfigurableUniversalRoute configurableRoute = (ConfigurableUniversalRoute) invalidRoute;
                boolean isValid = configurableRoute.validateTimeoutConfig();
                System.out.println("超时配置是否合理: " + isValid);
            }
            
        } catch (Exception e) {
            System.out.println("创建路由时发生错误: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * 演示动态修改超时配置（适用于支持的路由类型）
     */
    public static void demonstrateDynamicTimeoutChange() {
        System.out.println("--- 动态超时配置演示 ---");
        
        // 这里可以演示如果需要动态修改超时配置的场景
        // 由于我们的实现是不可变的，通常需要重新创建路由
        RouteTarget target = new SimpleRouteTarget("http://dynamic.example.com");
        
        // 原始路由
        UniversalRoute originalRoute = UniversalRouteBuilder.create()
                .id("dynamic-route")
                .name("动态路由")
                .addProtocol(new HttpProtocol())
                .target(target)
                .fastApi()
                .build();
        
        System.out.println("原始配置:");
        printRouteTimeoutInfo("动态路由", originalRoute);
        
        // 基于原始路由创建新的路由（修改超时配置）
        UniversalRoute updatedRoute = UniversalRouteBuilder.from(originalRoute)
                .requestTimeout(Duration.ofSeconds(45)) // 修改请求超时
                .totalTimeout(Duration.ofSeconds(80))   // 修改总超时
                .build();
        
        System.out.println("更新后配置:");
        printRouteTimeoutInfo("动态路由", updatedRoute);
    }
    
    /**
     * 演示不同超时类型的使用场景
     */
    public static void demonstrateTimeoutTypeUseCases() {
        System.out.println("--- 超时类型使用场景 ---");
        
        for (TimeoutType type : TimeoutType.values()) {
            System.out.println(type.getDescription() + " (" + type.getKey() + "):");
            System.out.println("  - 使用场景: " + getTimeoutUseCase(type));
            System.out.println("  - 推荐值: " + getRecommendedTimeout(type));
            System.out.println();
        }
    }
    
    private static String getTimeoutUseCase(TimeoutType type) {
        return switch (type) {
            case CONNECTION -> "建立TCP连接的最大等待时间，影响初始连接速度";
            case REQUEST -> "单次请求的最大处理时间，包含业务逻辑执行";
            case TOTAL -> "包含重试在内的总体超时时间，用于整体SLA控制";
            case READ -> "从服务端读取响应数据的超时时间";
            case WRITE -> "向服务端写入请求数据的超时时间";
            case CIRCUIT_BREAKER -> "熔断器的超时时间，用于故障恢复";
        };
    }
    
    private static String getRecommendedTimeout(TimeoutType type) {
        return switch (type) {
            case CONNECTION -> "快速API: 2-5秒, 慢速API: 5-10秒";
            case REQUEST -> "快速API: 5-15秒, 慢速API: 30-120秒";
            case TOTAL -> "通常是请求超时的1.5-2倍";
            case READ -> "与请求超时相近或略长";
            case WRITE -> "通常较短，5-15秒";
            case CIRCUIT_BREAKER -> "较长，60-300秒";
        };
    }
} 