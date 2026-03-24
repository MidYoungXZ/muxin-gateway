package com.muxin.gateway.admin.service;

import com.muxin.gateway.admin.model.dto.DiscoveryConfigDTO;
import com.muxin.gateway.admin.model.dto.DiscoveryRequestDTO;
import com.muxin.gateway.admin.model.dto.ServiceCreateDTO;
import com.muxin.gateway.admin.model.dto.ServiceNodeCreateDTO;
import com.muxin.gateway.admin.model.dto.ServiceNodeUpdateDTO;
import com.muxin.gateway.admin.model.vo.DiscoveredNodeVO;
import com.muxin.gateway.admin.model.vo.DiscoveryTestResultVO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.RouteSimpleVO;
import com.muxin.gateway.admin.model.vo.ServiceNodeVO;
import com.muxin.gateway.admin.model.vo.ServiceStatsVO;

import java.util.List;

public interface ServiceNodeService {
    
    List<ServiceStatsVO> getServiceStats(String serviceName);
    
    PageVO<ServiceNodeVO> getNodesByService(String serviceName, int pageNum, int pageSize);
    
    ServiceNodeVO getDetail(Long id);
    
    Long createService(ServiceCreateDTO dto);
    
    Long create(ServiceNodeCreateDTO dto);
    
    void update(Long id, ServiceNodeUpdateDTO dto);
    
    void delete(Long id);
    
    void enable(Long id);
    
    void disable(Long id);
    
    void maintenance(Long id);
    
    List<String> getServiceNames();
    
    List<RouteSimpleVO> getRoutesByServiceName(String serviceName);
    
    void deleteService(String serviceName);
    
    DiscoveryTestResultVO testDiscoveryConnection(DiscoveryConfigDTO config);
    
    List<DiscoveredNodeVO> discoverNodes(DiscoveryRequestDTO dto);
}