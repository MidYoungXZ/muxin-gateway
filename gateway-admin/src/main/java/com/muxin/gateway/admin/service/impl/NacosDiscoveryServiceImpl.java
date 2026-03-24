package com.muxin.gateway.admin.service.impl;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.muxin.gateway.admin.model.dto.DiscoveryConfigDTO;
import com.muxin.gateway.admin.model.vo.DiscoveredNodeVO;
import com.muxin.gateway.admin.model.vo.DiscoveryTestResultVO;
import com.muxin.gateway.admin.service.RegistryDiscoveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NacosDiscoveryServiceImpl implements RegistryDiscoveryService {
    
    private static final String DEFAULT_GROUP = "DEFAULT_GROUP";
    
    @Override
    public String getRegistryType() {
        return "NACOS";
    }
    
    @Override
    public DiscoveryTestResultVO testConnection(DiscoveryConfigDTO config) {
        NamingService namingService = null;
        try {
            namingService = createNamingService(config);
            
            ListView<String> services = namingService.getServicesOfServer(1, 100, getGroup(config));
            
            return DiscoveryTestResultVO.builder()
                    .success(true)
                    .message("连接成功，发现 " + services.getData().size() + " 个服务")
                    .serviceNames(services.getData())
                    .build();
        } catch (Exception e) {
            log.error("[NacosDiscovery] 测试连接失败: {}", e.getMessage());
            return DiscoveryTestResultVO.builder()
                    .success(false)
                    .message("连接失败: " + e.getMessage())
                    .build();
        } finally {
            shutdownNamingService(namingService);
        }
    }
    
    @Override
    public List<DiscoveredNodeVO> discoverNodes(String serviceName, DiscoveryConfigDTO config) {
        NamingService namingService = null;
        try {
            namingService = createNamingService(config);
            
            List<Instance> instances = namingService.getAllInstances(serviceName, getGroup(config));
            
            return instances.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("[NacosDiscovery] 发现服务节点失败: {}", e.getMessage());
            return Collections.emptyList();
        } finally {
            shutdownNamingService(namingService);
        }
    }
    
    private NamingService createNamingService(DiscoveryConfigDTO config) throws NacosException {
        Properties properties = new Properties();
        properties.setProperty("serverAddr", config.getServerAddr());
        
        if (config.getNamespace() != null && !config.getNamespace().isEmpty()) {
            properties.setProperty("namespace", config.getNamespace());
        }
        
        if (config.getUsername() != null && !config.getUsername().isEmpty()) {
            properties.setProperty("username", config.getUsername());
        }
        
        if (config.getPassword() != null && !config.getPassword().isEmpty()) {
            properties.setProperty("password", config.getPassword());
        }
        
        return NamingFactory.createNamingService(properties);
    }
    
    private String getGroup(DiscoveryConfigDTO config) {
        return config.getGroup() != null && !config.getGroup().isEmpty() 
                ? config.getGroup() 
                : DEFAULT_GROUP;
    }
    
    private void shutdownNamingService(NamingService namingService) {
        if (namingService != null) {
            try {
                namingService.shutDown();
            } catch (NacosException e) {
                log.warn("[NacosDiscovery] 关闭 NamingService 失败: {}", e.getMessage());
            }
        }
    }
    
    private DiscoveredNodeVO convertToVO(Instance instance) {
        return DiscoveredNodeVO.builder()
                .instanceId(instance.getInstanceId())
                .address(instance.getIp())
                .port(instance.getPort())
                .weight((int) instance.getWeight())
                .healthy(instance.isHealthy())
                .enabled(instance.isEnabled())
                .metadata(instance.getMetadata())
                .build();
    }
}