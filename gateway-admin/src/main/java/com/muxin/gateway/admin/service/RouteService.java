package com.muxin.gateway.admin.service;

import com.muxin.gateway.admin.model.dto.RouteCreateDTO;
import com.muxin.gateway.admin.model.dto.RouteQueryDTO;
import com.muxin.gateway.admin.model.dto.RouteTestDTO;
import com.muxin.gateway.admin.model.dto.RouteUpdateDTO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.RouteTestResultVO;
import com.muxin.gateway.admin.model.vo.RouteVO;

import java.util.List;

public interface RouteService {
    
    PageVO<RouteVO> pageQuery(RouteQueryDTO query);
    
    RouteVO getRouteDetail(Long id);
    
    Long createRoute(RouteCreateDTO dto);
    
    void updateRoute(Long id, RouteUpdateDTO dto);
    
    void deleteRoute(Long id);
    
    void batchDelete(List<Long> ids);
    
    void enableRoute(Long id);
    
    void disableRoute(Long id);
    
    RouteTestResultVO testRoute(RouteTestDTO dto);
    
    List<String> getServiceNames();
} 