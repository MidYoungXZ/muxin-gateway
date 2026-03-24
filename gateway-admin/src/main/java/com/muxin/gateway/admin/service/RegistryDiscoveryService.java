package com.muxin.gateway.admin.service;

import com.muxin.gateway.admin.model.dto.DiscoveryConfigDTO;
import com.muxin.gateway.admin.model.vo.DiscoveredNodeVO;
import com.muxin.gateway.admin.model.vo.DiscoveryTestResultVO;

import java.util.List;

public interface RegistryDiscoveryService {
    
    String getRegistryType();
    
    DiscoveryTestResultVO testConnection(DiscoveryConfigDTO config);
    
    List<DiscoveredNodeVO> discoverNodes(String serviceName, DiscoveryConfigDTO config);
}