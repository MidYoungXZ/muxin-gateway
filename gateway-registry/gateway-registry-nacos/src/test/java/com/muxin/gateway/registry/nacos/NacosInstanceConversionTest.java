package com.muxin.gateway.registry.nacos;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.muxin.gateway.core.registry.ServiceInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Nacos Instance转换测试
 *
 * @author Administrator
 * @date 2025/1/16 20:00
 */
@DisplayName("Nacos Instance转换测试")
public class NacosInstanceConversionTest {

    private NacosRegisterCenter registerCenter;

    @BeforeEach
    void setUp() {
        // 创建一个用于测试的NacosRegisterCenter（不进行实际的Nacos连接）
        registerCenter = new NacosRegisterCenter("localhost:8848", "DEFAULT_GROUP", "DEFAULT") {
            @Override
            protected void init() {
                // 重写init方法，避免实际连接Nacos
                // 这里不做任何操作，避免连接真实的Nacos服务器
            }
        };
    }

    @Test
    @DisplayName("测试通用Nacos Instance转换")
    void testCreateServiceInstanceFromNacosInstance() {
        // 创建一个标准的Nacos Instance
        Instance nacosInstance = new Instance();
        nacosInstance.setInstanceId("test-instance-001");
        nacosInstance.setIp("192.168.1.100");
        nacosInstance.setPort(8080);
        nacosInstance.setServiceName("test-server");
        nacosInstance.setWeight(1.0);
        nacosInstance.setHealthy(true);
        nacosInstance.setEnabled(true);
        nacosInstance.setClusterName("DEFAULT");

        // 设置metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("version", "1.2.0");
        metadata.put("scheme", "http");
        metadata.put("description", "测试服务");
        metadata.put("environment", "dev");
        nacosInstance.setMetadata(metadata);

        // 执行转换
        ServiceInstance serviceInstance = registerCenter.testCreateServiceInstanceFromNacosInstance(nacosInstance);

        // 验证转换结果
        assertNotNull(serviceInstance);
        assertEquals("test-instance-001", serviceInstance.getInstanceId());
        assertEquals("192.168.1.100", serviceInstance.getHost());
        assertEquals(8080, serviceInstance.getPort());
        assertEquals(1.0, serviceInstance.getWeight());
        assertTrue(serviceInstance.isHealthy());
        assertFalse(serviceInstance.isSecure());

        // 验证ServiceDefinition
        assertNotNull(serviceInstance.getServiceDefinition());
        assertEquals("test-server", serviceInstance.getServiceDefinition().getServiceId());
        assertEquals("http", serviceInstance.getServiceDefinition().getScheme());
        assertEquals("1.2.0", serviceInstance.getServiceDefinition().getVersion());
        assertEquals("测试服务", serviceInstance.getServiceDefinition().getDescription());
        assertTrue(serviceInstance.getServiceDefinition().isEnabled());

        // 验证URI
        assertNotNull(serviceInstance.getUri());
        assertEquals("http://192.168.1.100:8080", serviceInstance.getUri().toString());

        // 验证metadata（应该不包含内部使用的metadata key）
        Map<String, String> resultMetadata = serviceInstance.getMetadata();
        assertNotNull(resultMetadata);
        assertEquals("1.2.0", resultMetadata.get("version"));
        assertEquals("http", resultMetadata.get("scheme"));
        assertEquals("测试服务", resultMetadata.get("description"));
        assertEquals("dev", resultMetadata.get("environment"));
        assertFalse(resultMetadata.containsKey("metadata")); // 内部key应该被过滤掉
    }

    @Test
    @DisplayName("测试HTTPS服务转换")
    void testHttpsServiceConversion() {
        Instance nacosInstance = new Instance();
        nacosInstance.setInstanceId("https-service-001");
        nacosInstance.setIp("192.168.1.200");
        nacosInstance.setPort(8443);
        nacosInstance.setServiceName("secure-service");
        nacosInstance.setHealthy(true);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("scheme", "https");
        metadata.put("secure", "true");
        nacosInstance.setMetadata(metadata);

        ServiceInstance serviceInstance = registerCenter.testCreateServiceInstanceFromNacosInstance(nacosInstance);

        assertNotNull(serviceInstance);
        assertEquals("https", serviceInstance.getServiceDefinition().getScheme());
        assertTrue(serviceInstance.isSecure());
        assertEquals("https://192.168.1.200:8443", serviceInstance.getUri().toString());
    }

    @Test
    @DisplayName("测试缺少instanceId的情况")
    void testMissingInstanceId() {
        Instance nacosInstance = new Instance();
        nacosInstance.setIp("192.168.1.300");
        nacosInstance.setPort(9090);
        nacosInstance.setServiceName("test-service");
        nacosInstance.setHealthy(true);

        ServiceInstance serviceInstance = registerCenter.testCreateServiceInstanceFromNacosInstance(nacosInstance);

        assertNotNull(serviceInstance);
        assertNotNull(serviceInstance.getInstanceId());
        assertTrue(serviceInstance.getInstanceId().startsWith("test-service-192.168.1.300-9090"));
    }

    @Test
    @DisplayName("测试最小化Nacos Instance转换")
    void testMinimalNacosInstance() {
        Instance nacosInstance = new Instance();
        nacosInstance.setIp("127.0.0.1");
        nacosInstance.setPort(8080);
        nacosInstance.setServiceName("minimal-service");

        ServiceInstance serviceInstance = registerCenter.testCreateServiceInstanceFromNacosInstance(nacosInstance);

        assertNotNull(serviceInstance);
        assertEquals("127.0.0.1", serviceInstance.getHost());
        assertEquals(8080, serviceInstance.getPort());
        assertEquals("minimal-service", serviceInstance.getServiceDefinition().getServiceId());
        assertEquals("http", serviceInstance.getServiceDefinition().getScheme()); // 默认值
        assertEquals("1.0", serviceInstance.getServiceDefinition().getVersion()); // 默认值
        assertTrue(serviceInstance.isHealthy()); // Instance默认值
        assertEquals(1.0, serviceInstance.getWeight()); // Instance默认值
    }
} 