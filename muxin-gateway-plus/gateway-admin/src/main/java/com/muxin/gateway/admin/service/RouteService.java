package com.muxin.gateway.admin.service;

import com.muxin.gateway.admin.model.dto.RouteCreateDTO;
import com.muxin.gateway.admin.model.dto.RouteQueryDTO;
import com.muxin.gateway.admin.model.dto.RouteTestDTO;
import com.muxin.gateway.admin.model.dto.RouteUpdateDTO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.RouteTestResultVO;
import com.muxin.gateway.admin.model.vo.RouteVO;

/**
 * 路由服务接口
 *
 * @author muxin
 */
public interface RouteService {
    
    /**
     * 分页查询路由列表
     */
    PageVO<RouteVO> pageQuery(RouteQueryDTO query);
    
    /**
     * 获取路由详情
     */
    RouteVO getRouteDetail(Long id);
    
    /**
     * 创建路由
     */
    Long createRoute(RouteCreateDTO dto);
    
    /**
     * 更新路由
     */
    void updateRoute(Long id, RouteUpdateDTO dto);
    
    /**
     * 删除路由
     */
    void deleteRoute(Long id);
    
    /**
     * 启用路由
     */
    void enableRoute(Long id);
    
    /**
     * 禁用路由
     */
    void disableRoute(Long id);
    
    /**
     * 测试路由
     */
    RouteTestResultVO testRoute(RouteTestDTO dto);
} 