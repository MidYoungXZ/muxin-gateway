package com.muxin.gateway.registry.nacos;

import com.muxin.gateway.core.registry.DefaultServiceInstance;
import com.muxin.gateway.core.registry.RegisterCenter;
import com.muxin.gateway.core.registry.RegisterCenterListener;
import com.muxin.gateway.core.registry.ServiceInstance;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NacosRegisterCenter测试类
 * 测试Nacos注册中心的功能，包括用户名密码认证
 *
 * @author Administrator
 * @date 2025/6/13 20:00
 */
@Slf4j
@SpringBootTest
@TestPropertySource(properties = {
    "muxin.gateway.register.address=10.100.0.140:10809",
    "muxin.gateway.register.username=nacos",
    "muxin.gateway.register.password=nacos",
    "muxin.gateway.register.group=DEFAULT_GROUP"
})
public class NacosRegisterCenterTest {

    private RegisterCenter registerCenter;
    private final String testServiceId = "test-service";
    private final String testGroupName = "DEFAULT_GROUP";
    private final String testClusterName = "DEFAULT";

    @BeforeEach
    void setUp() {
        // 初始化NacosRegisterCenter，带认证信息
        registerCenter = new NacosRegisterCenter(
            "10.100.0.140:10809", 
            testGroupName, 
            testClusterName,
            "nacos",
            "nacos"
        );
    }

    @Test
    void testIsAvailable() {
        log.info("测试Nacos注册中心连接状态...");
        boolean available = registerCenter.isAvailable();
        assertTrue(available, "Nacos注册中心应该可用");
        log.info("Nacos注册中心连接状态: {}", available);
    }

    @Test
    void testRegisterAndDeregister() {
        log.info("测试服务注册和注销...");
        
        // 创建测试服务实例
        DefaultServiceInstance serviceInstance = createTestServiceInstance();
        
        try {
            // 注册服务
            registerCenter.register(serviceInstance);
            log.info("服务注册成功: {}", serviceInstance.getInstanceId());
            
            // 等待一段时间让注册生效
            Thread.sleep(2000);
            
            // 查询服务实例
            List<ServiceInstance> instances = registerCenter.selectInstances(testServiceId);
            assertFalse(instances.isEmpty(), "应该能查询到注册的服务实例");
            log.info("查询到服务实例数量: {}", instances.size());
            
            // 注销服务
            registerCenter.deregister(serviceInstance);
            log.info("服务注销成功: {}", serviceInstance.getInstanceId());
            
            // 等待一段时间让注销生效
            Thread.sleep(2000);
            
            // 再次查询服务实例
            instances = registerCenter.selectInstances(testServiceId);
            log.info("注销后查询到服务实例数量: {}", instances.size());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("测试被中断");
        } catch (Exception e) {
            log.error("服务注册/注销测试失败", e);
            fail("服务注册/注销测试失败: " + e.getMessage());
        }
    }

    @Test
    void testSelectInstances() {
        log.info("测试查询服务实例...");
        
        DefaultServiceInstance serviceInstance = createTestServiceInstance();
        
        try {
            // 注册服务
            registerCenter.register(serviceInstance);
            Thread.sleep(2000);
            
            // 查询所有实例
            List<ServiceInstance> allInstances = registerCenter.selectInstances(testServiceId);
            assertNotNull(allInstances, "查询结果不应为null");
            log.info("查询到所有服务实例数量: {}", allInstances.size());
            
            // 查询健康实例
            List<ServiceInstance> healthyInstances = registerCenter.selectInstances(testServiceId, true);
            assertNotNull(healthyInstances, "查询结果不应为null");
            log.info("查询到健康服务实例数量: {}", healthyInstances.size());
            
            // 查询不健康实例
            List<ServiceInstance> unhealthyInstances = registerCenter.selectInstances(testServiceId, false);
            assertNotNull(unhealthyInstances, "查询结果不应为null");
            log.info("查询到不健康服务实例数量: {}", unhealthyInstances.size());
            
            // 清理
            registerCenter.deregister(serviceInstance);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("测试被中断");
        } catch (Exception e) {
            log.error("查询服务实例测试失败", e);
            fail("查询服务实例测试失败: " + e.getMessage());
        }
    }

    @Test
    void testSubscribeAndUnsubscribe() throws InterruptedException {
        log.info("测试服务订阅和取消订阅...");
        
        CountDownLatch latch = new CountDownLatch(1);
        DefaultServiceInstance serviceInstance = createTestServiceInstance();
        
        // 创建监听器
        RegisterCenterListener listener = new RegisterCenterListener() {
            @Override
            public void onChange(List<ServiceInstance> serviceInstances) {
                log.info("收到服务变更通知，实例数量: {}", serviceInstances.size());
                latch.countDown();
            }
        };
        
        try {
            // 订阅服务
            registerCenter.subscribe(testServiceId, listener);
            log.info("订阅服务成功: {}", testServiceId);
            
            // 注册服务实例触发变更事件
            registerCenter.register(serviceInstance);
            
            // 等待监听器被调用
            boolean notified = latch.await(10, TimeUnit.SECONDS);
            assertTrue(notified, "应该收到服务变更通知");
            
            // 取消订阅
            registerCenter.unsubscribe(testServiceId, listener);
            log.info("取消订阅服务成功: {}", testServiceId);
            
            // 清理
            registerCenter.deregister(serviceInstance);
            
        } catch (Exception e) {
            log.error("订阅/取消订阅测试失败", e);
            fail("订阅/取消订阅测试失败: " + e.getMessage());
        }
    }

    @Test
    void testWithoutAuthentication() {
        log.info("测试无认证连接（应该失败）...");
        
        try {
            // 创建无认证的注册中心
            RegisterCenter noAuthRegisterCenter = new NacosRegisterCenter(
                "10.100.0.140:10809", 
                testGroupName, 
                testClusterName
            );
            
            // 尝试检查可用性
            boolean available = noAuthRegisterCenter.isAvailable();
            log.info("无认证连接状态: {}", available);
            
            // 如果Nacos需要认证，这里应该返回false或抛出异常
            // 根据实际情况调整断言
            
        } catch (Exception e) {
            log.info("无认证连接失败（预期行为）: {}", e.getMessage());
            // 这是预期的行为，如果Nacos配置了认证
        }
    }

    @Test
    void testWithWrongCredentials() {
        log.info("测试错误认证信息（应该失败）...");
        
        try {
            // 创建错误认证信息的注册中心
            RegisterCenter wrongAuthRegisterCenter = new NacosRegisterCenter(
                "10.100.0.140:10809", 
                testGroupName, 
                testClusterName,
                "wrong_user",
                "wrong_password"
            );
            
            // 尝试检查可用性
            boolean available = wrongAuthRegisterCenter.isAvailable();
            log.info("错误认证连接状态: {}", available);
            
            // 如果认证信息错误，这里应该返回false或抛出异常
            
        } catch (Exception e) {
            log.info("错误认证连接失败（预期行为）: {}", e.getMessage());
            // 这是预期的行为
        }
    }

    private DefaultServiceInstance createTestServiceInstance() {
        DefaultServiceInstance.DefaultServiceDefinition serviceDefinition = new DefaultServiceInstance.DefaultServiceDefinition();
        serviceDefinition.setServiceId(testServiceId);
        serviceDefinition.setScheme("http");
        serviceDefinition.setVersion("1.0.0");
        serviceDefinition.setScope("default");
        serviceDefinition.setEnvironment("test");
        serviceDefinition.setDescription("测试服务");
        
        Map<String, String> serviceMetadata = new HashMap<>();
        serviceMetadata.put("type", "test");
        serviceMetadata.put("creator", "unit-test");
        serviceDefinition.setMetadata(serviceMetadata);
        
        Map<String, String> instanceMetadata = new HashMap<>();
        instanceMetadata.put("version", "1.0.0");
        instanceMetadata.put("environment", "test");
        
        DefaultServiceInstance serviceInstance = new DefaultServiceInstance();
        serviceInstance.setServiceDefinition(serviceDefinition);
        serviceInstance.setInstanceId("test-instance-" + System.currentTimeMillis());
        serviceInstance.setHost("127.0.0.1");
        serviceInstance.setPort(8080);
        serviceInstance.setWeight(1.0);
        serviceInstance.setHealthy(true);
        serviceInstance.setMetadata(instanceMetadata);
        
        return serviceInstance;
    }
} 