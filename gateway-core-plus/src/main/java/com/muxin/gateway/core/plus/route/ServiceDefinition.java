package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.message.ProtocolDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务定义配置类（v2.0）
 * 对应YAML配置中的service节点，统一服务相关配置
 * 
 * 支持两种服务类型：
 * - CONFIG: 静态地址配置，需要配置addresses列表
 * - DISCOVERY: 服务发现，从注册中心获取服务实例
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDefinition {
    
    // ========== 服务标识信息 ==========
    
    /**
     * 服务唯一标识
     */
    private String id;
    
    /**
     * 服务显示名称
     */
    private String name;
    
    /**
     * 服务类型: CONFIG/DISCOVERY
     */
    private ServiceType type;
    
    // ========== 协议配置 ==========
    
    /**
     * 支持的协议类型列表
     * 从 YAML 中的 supported-protocols 加载
     * 例如: [HTTP, GRPC]
     */
    private List<String> supportedProtocols;
    
    // ========== 地址配置（仅CONFIG类型需要）==========
    
    /**
     * 静态地址列表（仅CONFIG类型）
     */
    private List<AddressDefinition> addresses;
    
    // ========== 扩展配置 ==========
    
    /**
     * 扩展配置参数
     * 包含 health-check、registry、cache-expire-time 等
     */
    private Map<String, Object> config;
    
    // ========== 服务类型判断 ==========
    
    /**
     * 是否为CONFIG类型服务
     */
    public boolean isConfigType() {
        return type == ServiceType.CONFIG;
    }
    
    /**
     * 是否为DISCOVERY类型服务
     */
    public boolean isDiscoveryType() {
        return type == ServiceType.DISCOVERY;
    }
    
    // ========== 协议配置管理 ==========
    
    /**
     * 获取支持的协议列表
     */
    public List<String> getSupportedProtocols() {
        return supportedProtocols != null ? supportedProtocols : new ArrayList<>();
    }
    
    /**
     * 获取主要协议类型（第一个协议）
     */
    public String getPrimaryProtocol() {
        if (supportedProtocols == null || supportedProtocols.isEmpty()) {
            return "HTTP";
        }
        return supportedProtocols.get(0);
    }
    
    /**
     * 是否支持指定协议
     */
    public boolean supportsProtocol(String protocol) {
        if (supportedProtocols == null || protocol == null) {
            return false;
        }
        return supportedProtocols.stream()
                .anyMatch(p -> p.equalsIgnoreCase(protocol));
    }
    
    /**
     * 获取协议定义（向后兼容）
     */
    public ProtocolDefinition getSupportProtocol() {
        String primaryProtocol = getPrimaryProtocol();
        return ProtocolDefinition.builder()
                .type(primaryProtocol)
                .version("1.0")
                .build();
    }
    
    // ========== 地址管理（仅CONFIG类型）==========
    
    /**
     * 获取地址列表（仅CONFIG类型）
     */
    public List<AddressDefinition> getAddresses() {
        if (!isConfigType()) {
            throw new IllegalStateException("只有CONFIG类型服务才有addresses配置");
        }
        return addresses;
    }
    
    /**
     * 是否配置了地址
     */
    public boolean hasAddresses() {
        return addresses != null && !addresses.isEmpty();
    }
    
    // ========== 配置参数管理 ==========
    
    /**
     * 获取配置参数
     */
    public Object getConfigValue(String key) {
        return config != null ? config.get(key) : null;
    }
    
    /**
     * 获取配置参数（带默认值和类型检查）
     */
    public <T> T getConfigValue(String key, T defaultValue, Class<T> type) {
        if (config == null) {
            return defaultValue;
        }
        
        Object value = config.get(key);
        if (value == null) {
            return defaultValue;
        }
        
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        
        throw new IllegalArgumentException(String.format(
                "配置参数 %s 的类型不匹配，期望: %s, 实际: %s", 
                key, type.getSimpleName(), value.getClass().getSimpleName()
        ));
    }
    
    /**
     * 设置配置参数
     */
    public void setConfigValue(String key, Object value) {
        if (config == null) {
            config = new HashMap<>();
        }
        config.put(key, value);
    }
    
    // ========== 健康检查配置 ==========
    
    /**
     * 获取健康检查配置
     */
    public Map<String, Object> getHealthCheckConfig() {
        @SuppressWarnings("unchecked")
        Map<String, Object> healthCheck = (Map<String, Object>) getConfigValue("health-check");
        return healthCheck != null ? healthCheck : new HashMap<>();
    }
    
    /**
     * 健康检查是否启用
     */
    public boolean isHealthCheckEnabled() {
        Map<String, Object> healthCheck = getHealthCheckConfig();
        Object enabled = healthCheck.get("enabled");
        return enabled != null && Boolean.parseBoolean(enabled.toString());
    }
    
    // ========== 配置验证 ==========
    
    /**
     * 验证配置的完整性和正确性
     */
    public void validate() {
        // 基础字段验证
        validateBasicFields();
        
        // 服务类型特定验证
        switch (type) {
            case CONFIG:
                validateConfigTypeService();
                break;
            case DISCOVERY:
                validateDiscoveryTypeService();
                break;
            default:
                throw new IllegalArgumentException("不支持的服务类型: " + type);
        }
        
        // 协议配置验证
        validateProtocolConfig();
    }
    
    /**
     * 验证基础字段
     */
    private void validateBasicFields() {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("service.id不能为空");
        }
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("service.name不能为空");
        }
        
        if (type == null) {
            throw new IllegalArgumentException("service.type不能为空");
        }
    }
    
    /**
     * 验证CONFIG类型服务配置
     */
    private void validateConfigTypeService() {
        if (addresses == null || addresses.isEmpty()) {
            throw new IllegalArgumentException("CONFIG类型服务必须配置addresses");
        }
        
        // 验证每个地址的有效性
        for (int i = 0; i < addresses.size(); i++) {
            AddressDefinition address = addresses.get(i);
            if (address == null) {
                throw new IllegalArgumentException("addresses[" + i + "]不能为空");
            }
            try {
                address.validate();
            } catch (Exception e) {
                throw new IllegalArgumentException("addresses[" + i + "]配置无效: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 验证DISCOVERY类型服务配置
     */
    private void validateDiscoveryTypeService() {
        if (addresses != null && !addresses.isEmpty()) {
            throw new IllegalArgumentException("DISCOVERY类型服务不应该配置addresses");
        }
        
        // DISCOVERY类型通过service.name从注册中心获取实例
        // 不需要验证addresses
    }
    
    /**
     * 验证协议配置
     */
    private void validateProtocolConfig() {
        if (supportedProtocols == null || supportedProtocols.isEmpty()) {
            throw new IllegalArgumentException("service.supported-protocols不能为空");
        }
        
        // 验证协议类型是否支持
        for (String protocol : supportedProtocols) {
            try {
                ProtocolType.fromCode(protocol);
            } catch (Exception e) {
                throw new IllegalArgumentException("不支持的协议类型: " + protocol, e);
            }
        }
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 转换为显示字符串
     */
    public String toDisplayString() {
        return String.format("Service[id=%s, name=%s, type=%s, protocols=%s]",
                id, name, type != null ? type.getCode() : "unknown",
                getSupportedProtocols());
    }
    
    /**
     * 获取服务的完整描述
     */
    public String getFullDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("服务[").append(name).append("](").append(id).append(")");
        sb.append(" - 类型: ").append(type != null ? type.getDescription() : "未知");
        sb.append(" - 协议: ").append(getSupportedProtocols());
        
        if (isConfigType() && hasAddresses()) {
            sb.append(" - 地址数量: ").append(addresses.size());
        }
        
        if (isHealthCheckEnabled()) {
            sb.append(" - 健康检查: 启用");
        }
        
        return sb.toString();
    }
    
    // ========== 构建器增强 ==========
    
    /**
     * 创建CONFIG类型服务的构建器
     */
    public static ServiceDefinitionBuilder configService(String serviceId, String serviceName) {
        return ServiceDefinition.builder()
                .id(serviceId)
                .name(serviceName)
                .type(ServiceType.CONFIG)
                .supportedProtocols(List.of("HTTP"));
    }
    
    /**
     * 创建DISCOVERY类型服务的构建器
     */
    public static ServiceDefinitionBuilder discoveryService(String serviceId, String serviceName) {
        return ServiceDefinition.builder()
                .id(serviceId)
                .name(serviceName)
                .type(ServiceType.DISCOVERY)
                .supportedProtocols(List.of("HTTP"));
    }
    
    /**
     * 构建并验证配置
     */
    public ServiceDefinition buildAndValidate() {
        ServiceDefinition definition = build();
        definition.validate();
        return definition;
    }
}