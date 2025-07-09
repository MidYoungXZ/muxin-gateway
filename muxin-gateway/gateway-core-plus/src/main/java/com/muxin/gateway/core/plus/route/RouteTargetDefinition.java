package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.message.ProtocolDefinition;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 路由目标配置定义
 * 纯配置数据对象，不包含业务逻辑
 *
 * @author muxin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteTargetDefinition {
    
    /**
     * 目标类型
     */
    private TargetType type;
    
    /**
     * 出站协议配置
     */
    private ProtocolDefinition outboundProtocol;
    
    /**
     * 地址配置列表
     */
    private List<AddressDefinition> addresses;
    
    /**
     * 负载均衡配置
     */
    private LoadBalanceDefinition loadBalance;
    
    /**
     * 扩展配置
     */
    private Map<String, Object> config;
    
    /**
     * 获取配置参数
     */
    public Object getConfigValue(String key) {
        return config != null ? config.get(key) : null;
    }
    
    /**
     * 获取配置参数（带默认值）
     */
    public <T> T getConfigValue(String key, T defaultValue) {
        if (config == null) {
            return defaultValue;
        }
        
        @SuppressWarnings("unchecked")
        T value = (T) config.get(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 设置配置参数
     */
    public void setConfigValue(String key, Object value) {
        if (config == null) {
            config = new java.util.HashMap<>();
        }
        config.put(key, value);
    }
    
    /**
     * 获取服务名称（仅适用于DISCOVERY类型）
     */
    public String getServiceName() {
        if (type != TargetType.DISCOVERY) {
            throw new IllegalStateException("只有DISCOVERY类型才能获取服务名称");
        }
        
        if (addresses == null || addresses.isEmpty()) {
            throw new IllegalStateException("DISCOVERY类型必须配置地址");
        }
        
        AddressDefinition address = addresses.get(0);
        if (!address.isDiscoveryAddress()) {
            throw new IllegalStateException("DISCOVERY类型必须使用lb://协议");
        }
        
        return address.getServiceName();
    }
    
    /**
     * 检查是否为静态类型
     */
    public boolean isStatic() {
        return type == TargetType.STATIC;
    }
    
    /**
     * 检查是否为服务发现类型
     */
    public boolean isDiscovery() {
        return type == TargetType.DISCOVERY;
    }
    
    /**
     * 获取负载均衡策略名称
     */
    public String getLoadBalanceStrategy() {
        return loadBalance != null ? loadBalance.getStrategy() : "ROUND_ROBIN";
    }
} 