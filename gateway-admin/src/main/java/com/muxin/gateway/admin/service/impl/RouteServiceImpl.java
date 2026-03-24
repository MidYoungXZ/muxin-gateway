package com.muxin.gateway.admin.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.muxin.gateway.admin.entity.GwRoute;
import com.muxin.gateway.admin.entity.GwRouteFilter;
import com.muxin.gateway.admin.entity.GwRoutePredicate;
import static com.muxin.gateway.admin.entity.table.GwRouteTableDef.GW_ROUTE;
import com.muxin.gateway.admin.enums.PredicateType;
import com.muxin.gateway.admin.exception.BusinessException;
import com.muxin.gateway.admin.mapper.RouteFilterMapper;
import com.muxin.gateway.admin.mapper.RouteMapper;
import com.muxin.gateway.admin.mapper.RoutePredicateMapper;
import com.muxin.gateway.admin.model.dto.RouteCreateDTO;
import com.muxin.gateway.admin.model.dto.RouteQueryDTO;
import com.muxin.gateway.admin.model.dto.RouteTestDTO;
import com.muxin.gateway.admin.model.dto.RouteUpdateDTO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.PredicateVO;
import com.muxin.gateway.admin.model.vo.FilterVO;
import com.muxin.gateway.admin.model.vo.RouteTestResultVO;
import com.muxin.gateway.admin.model.vo.RouteVO;
import com.muxin.gateway.admin.service.RouteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteServiceImpl extends ServiceImpl<RouteMapper, GwRoute> implements RouteService {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private final RouteMapper routeMapper;
    private final RoutePredicateMapper routePredicateMapper;
    private final RouteFilterMapper routeFilterMapper;
    private final com.muxin.gateway.admin.service.ConfigRefreshService configRefreshService;
    
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
        
        RouteVO vo = convertToVO(route);
        vo.setPredicates(loadPredicates(id));
        vo.setFilters(loadFilters(id));
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRoute(RouteCreateDTO dto) {
        checkRouteIdUnique(dto.getRouteId());
        
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
        
        saveRoutePredicates(route.getId(), dto.getPredicateIds());
        
        if (!CollectionUtils.isEmpty(dto.getFilterIds())) {
            saveRouteFilters(route.getId(), dto.getFilterIds());
        }
        
        configRefreshService.refreshRoutes();
        log.info("[RouteService] 路由创建成功，已同步到 gateway-core: {}", dto.getRouteId());
        
        return route.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoute(Long id, RouteUpdateDTO dto) {
        GwRoute oldRoute = getById(id);
        if (oldRoute == null || oldRoute.getDeleted()) {
            throw new BusinessException("路由不存在");
        }
        
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
        
        routePredicateMapper.deleteByRouteId(id);
        saveRoutePredicates(id, dto.getPredicateIds());
        
        routeFilterMapper.deleteByRouteId(id);
        if (!CollectionUtils.isEmpty(dto.getFilterIds())) {
            saveRouteFilters(id, dto.getFilterIds());
        }
        
        configRefreshService.refreshRoutes();
        log.info("[RouteService] 路由更新成功，已同步到 gateway-core: {}", oldRoute.getRouteId());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoute(Long id) {
        GwRoute route = getById(id);
        if (route == null || route.getDeleted()) {
            throw new BusinessException("路由不存在");
        }
        
        String routeId = route.getRouteId();
        
        routePredicateMapper.deleteByRouteId(id);
        routeFilterMapper.deleteByRouteId(id);
        
        removeById(id);
        
        configRefreshService.refreshRoutes();
        log.info("[RouteService] 路由删除成功，已同步到 gateway-core: {}", routeId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        
        for (Long id : ids) {
            GwRoute route = getById(id);
            if (route != null && !route.getDeleted()) {
                routePredicateMapper.deleteByRouteId(id);
                routeFilterMapper.deleteByRouteId(id);
                removeById(id);
            }
        }
        
        configRefreshService.refreshRoutes();
        log.info("[RouteService] 批量删除路由成功，已同步到 gateway-core，数量: {}", ids.size());
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
        return RouteTestResultVO.builder()
                .matched(false)
                .errorMessage("路由测试功能尚未实现")
                .build();
    }
    
    @Override
    public List<String> getServiceNames() {
        return routeMapper.findAllServiceNames();
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
        
        for (int i = 0; i < predicateIds.size(); i++) {
            GwRoutePredicate rp = new GwRoutePredicate();
            rp.setRouteId(routeId);
            rp.setPredicateId(predicateIds.get(i));
            rp.setSortOrder(i);
            rp.setCreateTime(LocalDateTime.now());
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
        
        for (int i = 0; i < filterIds.size(); i++) {
            GwRouteFilter rf = new GwRouteFilter();
            rf.setRouteId(routeId);
            rf.setFilterId(filterIds.get(i));
            rf.setSortOrder(i);
            rf.setCreateTime(LocalDateTime.now());
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
        
        configRefreshService.refreshRoutes();
        log.info("[RouteService] 路由状态更新成功，已同步到 gateway-core: {} -> enabled={}", route.getRouteId(), enabled);
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
        
        return vo;
    }
    
    private List<PredicateVO> loadPredicates(Long routeId) {
        List<Map<String, Object>> predicates = routePredicateMapper.findPredicatesByRouteId(routeId);
        return predicates.stream()
                .map(map -> {
                    PredicateVO vo = new PredicateVO();
                    vo.setId(((Number) map.get("id")).longValue());
                    vo.setPredicateName((String) map.get("predicateName"));
                    vo.setPredicateType((String) map.get("predicateType"));
                    vo.setConfig(parseConfig(map.get("config")));
                    
                    Arrays.stream(PredicateType.values())
                            .filter(t -> t.getType().equals(vo.getPredicateType()))
                            .findFirst()
                            .ifPresent(t -> vo.setPredicateTypeDesc(t.getName()));
                    
                    return vo;
                })
                .collect(Collectors.toList());
    }
    
    private List<FilterVO> loadFilters(Long routeId) {
        List<Map<String, Object>> filters = routeFilterMapper.findFiltersByRouteId(routeId);
        return filters.stream()
                .map(map -> {
                    FilterVO vo = new FilterVO();
                    vo.setId(((Number) map.get("id")).longValue());
                    vo.setFilterName((String) map.get("filterName"));
                    vo.setFilterType((String) map.get("filterType"));
                    vo.setConfig(parseConfig(map.get("config")));
                    
                    return vo;
                })
                .collect(Collectors.toList());
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseConfig(Object configObj) {
        if (configObj == null) {
            return new HashMap<>();
        }
        if (configObj instanceof Map) {
            return (Map<String, Object>) configObj;
        }
        if (configObj instanceof String) {
            try {
                return OBJECT_MAPPER.readValue((String) configObj, Map.class);
            } catch (Exception e) {
                log.warn("Failed to parse config JSON: {}", configObj, e);
                return new HashMap<>();
            }
        }
        return new HashMap<>();
    }
}