package com.muxin.gateway.admin.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.muxin.gateway.admin.entity.GwRoute;
import com.muxin.gateway.admin.entity.GwRouteFilter;
import com.muxin.gateway.admin.entity.GwRoutePredicate;
import static com.muxin.gateway.admin.entity.table.GwRouteTableDef.GW_ROUTE;
import com.muxin.gateway.admin.exception.BusinessException;
import com.muxin.gateway.admin.mapper.RouteFilterMapper;
import com.muxin.gateway.admin.mapper.RouteMapper;
import com.muxin.gateway.admin.mapper.RoutePredicateMapper;
import com.muxin.gateway.admin.model.dto.RouteCreateDTO;
import com.muxin.gateway.admin.model.dto.RouteQueryDTO;
import com.muxin.gateway.admin.model.dto.RouteTestDTO;
import com.muxin.gateway.admin.model.dto.RouteUpdateDTO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.RouteTestResultVO;
import com.muxin.gateway.admin.model.vo.RouteVO;
import com.muxin.gateway.admin.service.RouteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 路由服务实现
 *
 * @author muxin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteServiceImpl extends ServiceImpl<RouteMapper, GwRoute> implements RouteService {
    
    private final RouteMapper routeMapper;
    private final RoutePredicateMapper routePredicateMapper;
    private final RouteFilterMapper routeFilterMapper;
    
    @Override
    public PageVO<RouteVO> pageQuery(RouteQueryDTO query) {
        // 构建查询条件
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(GW_ROUTE)
                .where(GW_ROUTE.DELETED.eq(false));
        
        // 动态条件
        if (StringUtils.hasText(query.getRouteName())) {
            wrapper.and(GW_ROUTE.ROUTE_NAME.like("%" + query.getRouteName() + "%"));
        }
        
        if (StringUtils.hasText(query.getUri())) {
            wrapper.and(GW_ROUTE.URI.like("%" + query.getUri() + "%"));
        }
        
        if (query.getEnabled() != null) {
            wrapper.and(GW_ROUTE.ENABLED.eq(query.getEnabled()));
        }
        
        // 排序
        wrapper.orderBy(GW_ROUTE.ORDER.asc(), 
                       GW_ROUTE.CREATE_TIME.desc());
        
        // 分页查询
        com.mybatisflex.core.paginate.Page<GwRoute> page = page(
                new com.mybatisflex.core.paginate.Page<>(query.getPageNum(), query.getPageSize()), 
                wrapper);
        
        // 转换为VO
        List<RouteVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageVO.<RouteVO>builder()
                .data(voList)
                .total(page.getTotalRow())
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize())
                .totalPages((int) page.getTotalPage())
                .build();
    }
    
    @Override
    public RouteVO getRouteDetail(Long id) {
        GwRoute route = getById(id);
        if (route == null || route.getDeleted()) {
            throw new BusinessException("路由不存在");
        }
        
        // TODO: 加载关联的断言和过滤器
        return convertToVO(route);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRoute(RouteCreateDTO dto) {
        // 1. 验证路由ID唯一性
        checkRouteIdUnique(dto.getRouteId());
        
        // 2. 创建路由基本信息
        GwRoute route = new GwRoute();
        route.setRouteId(dto.getRouteId());
        route.setRouteName(dto.getRouteName());
        route.setDescription(dto.getDescription());
        route.setUri(dto.getUri());
        route.setMetadata(dto.getMetadata());
        route.setOrder(dto.getOrder());
        route.setEnabled(dto.getEnabled());
        route.setTemplateId(dto.getTemplateId());
        route.setVersion(1);
        route.setDeleted(false);
        
        save(route);
        
        // 3. 保存路由断言关联
        saveRoutePredicates(route.getId(), dto.getPredicateIds());
        
        // 4. 保存路由过滤器关联
        if (!CollectionUtils.isEmpty(dto.getFilterIds())) {
            saveRouteFilters(route.getId(), dto.getFilterIds());
        }
        
        // 5. 发布配置变更事件
        // TODO: publishRouteChangeEvent(route, OperationType.CREATE);
        
        return route.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoute(Long id, RouteUpdateDTO dto) {
        // 1. 获取原路由信息
        GwRoute oldRoute = getById(id);
        if (oldRoute == null || oldRoute.getDeleted()) {
            throw new BusinessException("路由不存在");
        }
        
        // 2. 更新路由基本信息
        GwRoute route = new GwRoute();
        route.setId(id);
        route.setRouteName(dto.getRouteName());
        route.setDescription(dto.getDescription());
        route.setUri(dto.getUri());
        route.setMetadata(dto.getMetadata());
        route.setOrder(dto.getOrder());
        route.setEnabled(dto.getEnabled());
        route.setGrayscaleEnabled(dto.getGrayscaleEnabled());
        route.setVersion(oldRoute.getVersion() + 1);
        
        updateById(route);
        
        // 3. 更新断言关联（先删除再添加）
        routePredicateMapper.deleteByRouteId(id);
        saveRoutePredicates(id, dto.getPredicateIds());
        
        // 4. 更新过滤器关联（先删除再添加）
        routeFilterMapper.deleteByRouteId(id);
        if (!CollectionUtils.isEmpty(dto.getFilterIds())) {
            saveRouteFilters(id, dto.getFilterIds());
        }
        
        // 5. 发布配置变更事件
        // TODO: publishRouteChangeEvent(route, OperationType.UPDATE);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoute(Long id) {
        GwRoute route = getById(id);
        if (route == null || route.getDeleted()) {
            throw new BusinessException("路由不存在");
        }
        
        // 逻辑删除
        removeById(id);
        
        // 发布配置变更事件
        // TODO: publishRouteChangeEvent(route, OperationType.DELETE);
    }
    
    @Override
    public void enableRoute(Long id) {
        updateRouteStatus(id, true);
    }
    
    @Override
    public void disableRoute(Long id) {
        updateRouteStatus(id, false);
    }
    
    @Override
    public RouteTestResultVO testRoute(RouteTestDTO dto) {
        // TODO: 实现路由测试逻辑
        return RouteTestResultVO.builder()
                .matched(false)
                .errorMessage("路由测试功能尚未实现")
                .build();
    }
    
    /**
     * 检查路由ID唯一性
     */
    private void checkRouteIdUnique(String routeId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(GW_ROUTE)
                .where(GW_ROUTE.ROUTE_ID.eq(routeId))
                .and(GW_ROUTE.DELETED.eq(false));
        
        long count = count(wrapper);
        if (count > 0) {
            throw new BusinessException("路由ID已存在: " + routeId);
        }
    }
    
    /**
     * 保存路由断言关联
     */
    private void saveRoutePredicates(Long routeId, List<Long> predicateIds) {
        if (CollectionUtils.isEmpty(predicateIds)) {
            return;
        }
        
        List<GwRoutePredicate> routePredicates = new ArrayList<>();
        for (int i = 0; i < predicateIds.size(); i++) {
            GwRoutePredicate rp = new GwRoutePredicate();
            rp.setRouteId(routeId);
            rp.setPredicateId(predicateIds.get(i));
            rp.setSortOrder(i);
            routePredicates.add(rp);
        }
        
        for (GwRoutePredicate rp : routePredicates) {
            routePredicateMapper.insert(rp);
        }
    }
    
    /**
     * 保存路由过滤器关联
     */
    private void saveRouteFilters(Long routeId, List<Long> filterIds) {
        if (CollectionUtils.isEmpty(filterIds)) {
            return;
        }
        
        List<GwRouteFilter> routeFilters = new ArrayList<>();
        for (int i = 0; i < filterIds.size(); i++) {
            GwRouteFilter rf = new GwRouteFilter();
            rf.setRouteId(routeId);
            rf.setFilterId(filterIds.get(i));
            rf.setSortOrder(i);
            routeFilters.add(rf);
        }
        
        for (GwRouteFilter rf : routeFilters) {
            routeFilterMapper.insert(rf);
        }
    }
    
    /**
     * 更新路由状态
     */
    private void updateRouteStatus(Long id, boolean enabled) {
        GwRoute route = getById(id);
        if (route == null || route.getDeleted()) {
            throw new BusinessException("路由不存在");
        }
        
        route.setEnabled(enabled);
        updateById(route);
        
        // 发布配置变更事件
        // TODO: publishRouteChangeEvent(route, OperationType.UPDATE);
    }
    
    /**
     * 转换为VO
     */
    private RouteVO convertToVO(GwRoute route) {
        RouteVO vo = new RouteVO();
        vo.setId(route.getId());
        vo.setRouteId(route.getRouteId());
        vo.setRouteName(route.getRouteName());
        vo.setDescription(route.getDescription());
        vo.setUri(route.getUri());
        vo.setMetadata(route.getMetadata());
        vo.setOrder(route.getOrder());
        vo.setEnabled(route.getEnabled());
        vo.setGrayscaleEnabled(route.getGrayscaleEnabled());
        vo.setGrayscaleConfig(route.getGrayscaleConfig());
        vo.setTemplateId(route.getTemplateId());
        vo.setVersion(route.getVersion());
        vo.setCreateTime(route.getCreateTime());
        vo.setUpdateTime(route.getUpdateTime());
        vo.setCreateBy(route.getCreateBy());
        vo.setUpdateBy(route.getUpdateBy());
        
        // TODO: 加载关联的断言和过滤器
        
        return vo;
    }
}