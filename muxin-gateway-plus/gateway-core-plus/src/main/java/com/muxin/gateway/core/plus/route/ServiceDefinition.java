package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.message.ProtocolDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务定义配置类
 * 对应YAML配置中的service节点，统一服务相关配置
 * 
 * 支持两种服务类型：
 * - CONFIG: 静态地址配置，需要配置addresses列表
 * - DISCOVERY: 服务发现，从注册中心获取服务实例
 *
 * @author muxin
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
     * 支持的协议配置
     */
    private ProtocolDefinition supportProtocol;
    
    // ========== 地址配置（仅CONFIG类型需要）==========
    
    /**
     * 静态地址列表（仅CONFIG类型）
     */
    private List<AddressDefinition> addresses;
    
    // ========== 扩展配置 ==========
    
    /**
     * 扩展配置参数
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
        if (supportProtocol == null) {
            throw new IllegalArgumentException("service.support-protocol不能为空");
        }
        
        // 验证协议类型
        if (supportProtocol.getType() == null || supportProtocol.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("协议类型不能为空");
        }
        
        // 验证协议版本
        if (supportProtocol.getVersion() == null || supportProtocol.getVersion().trim().isEmpty()) {
            throw new IllegalArgumentException("协议版本不能为空");
        }
        
        // 验证协议类型是否支持
        try {
            supportProtocol.toProtocol();
        } catch (Exception e) {
            throw new IllegalArgumentException("不支持的协议类型: " + supportProtocol.getType(), e);
        }
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 转换为显示字符串
     */
    public String toDisplayString() {
        return String.format("Service[id=%s, name=%s, type=%s, protocol=%s]",
            id, name, type != null ? type.getCode() : "unknown", 
            supportProtocol != null ? supportProtocol.getType() : "unknown");
    }
    
    /**
     * 获取服务的完整描述
     */
    public String getFullDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("服务[").append(name).append("](").append(id).append(")");
        sb.append(" - 类型: ").append(type != null ? type.getDescription() : "未知");
        sb.append(" - 协议: ").append(supportProtocol != null ? supportProtocol.getType() : "未知");
        
        if (isConfigType() && hasAddresses()) {
            sb.append(" - 地址数量: ").append(addresses.size());
        }
        
        return sb.toString();
    }
    
    // ========== 构建器增强 ==========
    
    public static class ServiceDefinitionBuilder {
        
        /**
         * 创建CONFIG类型服务的构建器
         */
        public static ServiceDefinitionBuilder configService(String serviceId, String serviceName) {
            return ServiceDefinition.builder()
                .id(serviceId)
                .name(serviceName)
                .type(ServiceType.CONFIG);
        }
        
        /**
         * 创建DISCOVERY类型服务的构建器
         */
        public static ServiceDefinitionBuilder discoveryService(String serviceId, String serviceName) {
            return ServiceDefinition.builder()
                .id(serviceId)
                .name(serviceName)
                .type(ServiceType.DISCOVERY);
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
}