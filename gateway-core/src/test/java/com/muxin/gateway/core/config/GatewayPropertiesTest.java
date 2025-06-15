package com.muxin.gateway.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GatewayProperties 配置测试类
 */
@SpringBootTest(classes = {GatewayProperties.class})
@EnableConfigurationProperties(GatewayProperties.class)
@TestPropertySource(properties = {
        "muxin.gateway.netty.server.port=8080",
        "muxin.gateway.netty.server.event-loop-group-boss-num=1",
        "muxin.gateway.netty.server.event-loop-group-worker-num=8",
        "muxin.gateway.netty.client.http-connect-timeout=10000",
        "muxin.gateway.netty.client.http-request-timeout=30000",
        
        "muxin.gateway.register.type=nacos",
        "muxin.gateway.register.address=localhost:8848",
        "muxin.gateway.register.username=nacos",
        "muxin.gateway.register.password=nacos",
        "muxin.gateway.register.group=DEFAULT_GROUP",
        "muxin.gateway.register.namespace=DEFAULT",
        
        "muxin.gateway.register.gateway.enabled=true",
        "muxin.gateway.register.gateway.weight=1.0",
        "muxin.gateway.register.gateway.version=1.0",
        "muxin.gateway.register.gateway.description=Test Gateway",
        "muxin.gateway.register.gateway.service-id=test-gateway",
        "muxin.gateway.register.gateway.health-check-url=/health",
        "muxin.gateway.register.gateway.health-check-interval=30",
        "muxin.gateway.register.gateway.status=UP",
        "muxin.gateway.register.gateway.secure=false",
        "muxin.gateway.register.gateway.metadata.env=test",
        "muxin.gateway.register.gateway.metadata.version=1.0",
        

})
class GatewayPropertiesTest {

    @Autowired
    private GatewayProperties gatewayProperties;

    @Test
    void testNettyConfiguration() {
        assertNotNull(gatewayProperties.getNetty());
        
        // 测试服务器配置
        NettyHttpServerProperties server = gatewayProperties.getNetty().getServer();
        assertNotNull(server);
        assertEquals(8080, server.getPort());
        assertEquals(1, server.getEventLoopGroupBossNum());
        assertEquals(8, server.getEventLoopGroupWorkerNum());
        
        // 测试客户端配置
        NettyHttpClientProperties client = gatewayProperties.getNetty().getClient();
        assertNotNull(client);
        assertEquals(10000, client.getHttpConnectTimeout());
        assertEquals(30000, client.getHttpRequestTimeout());
    }

    @Test
    void testRegisterConfiguration() {
        assertNotNull(gatewayProperties.getRegister());
        
        GatewayProperties.RegisterProperties register = gatewayProperties.getRegister();
        assertEquals("nacos", register.getType());
        assertEquals("localhost:8848", register.getAddress());
        assertEquals("nacos", register.getUsername());
        assertEquals("nacos", register.getPassword());
        assertEquals("DEFAULT_GROUP", register.getGroup());
        assertEquals("DEFAULT", register.getNamespace());
        
        // 测试网关自注册配置
        GatewayProperties.GatewayRegisterProperties gateway = register.getGateway();
        assertNotNull(gateway);
        assertTrue(gateway.isEnabled());
        assertEquals(1.0, gateway.getWeight());
        assertEquals("1.0", gateway.getVersion());
        assertEquals("Test Gateway", gateway.getDescription());
        assertEquals("test-gateway", gateway.getServiceId());
        assertEquals("/health", gateway.getHealthCheckUrl());
        assertEquals(30, gateway.getHealthCheckInterval());
        assertEquals("UP", gateway.getStatus());
        assertFalse(gateway.isSecure());
        
        // 测试元数据
        assertNotNull(gateway.getMetadata());
        assertEquals("test", gateway.getMetadata().get("env"));
        assertEquals("1.0", gateway.getMetadata().get("version"));
    }



    @Test
    void testDefaultValues() {
        // 创建新的空配置来测试默认值
        GatewayProperties empty = new GatewayProperties();
        
        assertNotNull(empty.getRoutes());
        assertTrue(empty.getRoutes().isEmpty());
        
        assertNotNull(empty.getDefaultFilters());
        assertTrue(empty.getDefaultFilters().isEmpty());
        
        assertNotNull(empty.getNetty());
        assertNotNull(empty.getNetty().getServer());
        assertNotNull(empty.getNetty().getClient());
        
        assertNotNull(empty.getRegister());
        assertEquals("nacos", empty.getRegister().getType());
        assertEquals("localhost:8848", empty.getRegister().getAddress());
        assertEquals("DEFAULT_GROUP", empty.getRegister().getGroup());
        
        assertNotNull(empty.getRegister().getGateway());
        assertTrue(empty.getRegister().getGateway().isEnabled());
        assertEquals(1.0, empty.getRegister().getGateway().getWeight());
    }
} 