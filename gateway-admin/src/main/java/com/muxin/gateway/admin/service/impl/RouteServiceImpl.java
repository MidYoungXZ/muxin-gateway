package com.muxin.gateway.admin.service.impl;

import com.muxin.gateway.admin.exception.BusinessException;
import com.muxin.gateway.admin.service.RouteService;
import com.muxin.gateway.core.route.RouteDefinition;
import com.muxin.gateway.core.route.RouteDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 路由管理服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RouteServiceImpl implements RouteService {

    private final RouteDefinitionRepository routeRepository;

    @Override
    public List<RouteDefinition> getAllRoutes() {
        List<RouteDefinition> routes = new ArrayList<>();
        routeRepository.findAll().forEach(routes::add);
        return routes;
    }

    @Override
    public RouteDefinition getRouteById(String id) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("路由ID不能为空");
        }
        
        // 获取所有路由
        List<RouteDefinition> routes = getAllRoutes();
        
        // 查找指定ID的路由
        Optional<RouteDefinition> optionalRoute = routes.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
                
        return optionalRoute.orElse(null);
    }

    @Override
    public RouteDefinition saveRoute(RouteDefinition routeDefinition) {
        if (routeDefinition == null) {
            throw new BusinessException("路由定义不能为空");
        }
        
        if (!StringUtils.hasText(routeDefinition.getId())) {
            throw new BusinessException("路由ID不能为空");
        }
        
        if (routeDefinition.getUri() == null) {
            throw new BusinessException("路由URI不能为空");
        }
        
        // 保存或更新路由
        RouteDefinition savedRoute = routeRepository.save(routeDefinition);
        
        log.info("保存路由: id={}, uri={}", routeDefinition.getId(), routeDefinition.getUri());
        return savedRoute;
    }

    @Override
    public boolean deleteRoute(String id) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("路由ID不能为空");
        }
        
        // 检查路由是否存在
        RouteDefinition route = getRouteById(id);
        if (route == null) {
            log.warn("删除路由失败：路由不存在, id={}", id);
            return false;
        }
        
        // 删除路由
        routeRepository.deleteById(id);
        
        log.info("删除路由: id={}", id);
        return true;
    }
} 