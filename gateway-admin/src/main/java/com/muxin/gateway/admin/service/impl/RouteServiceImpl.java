package com.muxin.gateway.admin.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.muxin.gateway.admin.entity.GwPredicate;
import com.muxin.gateway.admin.entity.GwPlugin;
import com.muxin.gateway.admin.entity.GwRoute;
import com.muxin.gateway.admin.entity.GwRoutePlugin;
import com.muxin.gateway.admin.entity.GwRoutePredicate;
import static com.muxin.gateway.admin.entity.table.GwRouteTableDef.GW_ROUTE;
import com.muxin.gateway.admin.exception.BusinessException;
import com.muxin.gateway.admin.mapper.PluginMapper;
import com.muxin.gateway.admin.mapper.PredicateMapper;
import com.muxin.gateway.admin.mapper.RouteMapper;
import com.muxin.gateway.admin.mapper.RoutePluginMapper;
import com.muxin.gateway.admin.mapper.RoutePredicateMapper;
import com.muxin.gateway.admin.model.dto.*;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.PluginVO;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteServiceImpl extends ServiceImpl<RouteMapper, GwRoute> implements RouteService {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private final RouteMapper routeMapper;
    private final RoutePluginMapper routePluginMapper;
    private final RoutePredicateMapper routePredicateMapper;
    private final PluginMapper pluginMapper;
    private final PredicateMapper predicateMapper;
    private final com.muxin.gateway.admin.service.ConfigRefreshService configRefreshService;
    
    @Override
    public PageVO<RouteVO> pageQuery(RouteQueryDTO query) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(GW_ROUTE)
                .where(GW_ROUTE.DELETED.eq(false));
        
        if (StringUtils.hasText(query.getRouteName())) {
            wrapper.and(GW_ROUTE.ROUTE_NAME.like("%" + query.getRouteName() + "%"));
        }
        
        if (StringUtils.hasText(query.getUri())) {
            wrapper.and(GW_ROUTE.URI.like("%" + query.getUri() + "%"));
        }
        
        if (query.getEnabled() != null) {
            wrapper.and(GW_ROUTE.ENABLED.eq(query.getEnabled()));
        }
        
        wrapper.orderBy(GW_ROUTE.ORDER.asc(), GW_ROUTE.CREATE_TIME.desc());
        
        com.mybatisflex.core.paginate.Page<GwRoute> page = page(
                new com.mybatisflex.core.paginate.Page<>(query.getPageNum(), query.getPageSize()), 
                wrapper);
        
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
        vo.setPlugins(loadPlugins(id));
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
        route.setOrder(dto.getOrder() != null ? dto.getOrder() : 0);
        route.setLoadBalanceStrategy(dto.getLoadBalanceStrategy() != null ? dto.getLoadBalanceStrategy() : "ROUND_ROBIN");
        route.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        route.setTemplateId(dto.getTemplateId());
        route.setVersion(1);
        route.setDeleted(false);
        
        save(route);
        
        if (dto.getMatching() != null) {
            saveRouteMatching(route.getId(), dto.getMatching());
        }
        
        if (!CollectionUtils.isEmpty(dto.getPlugins())) {
            saveRoutePlugins(route.getId(), dto.getPlugins());
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
        route.setLoadBalanceStrategy(dto.getLoadBalanceStrategy());
        route.setEnabled(dto.getEnabled());
        route.setGrayscaleEnabled(dto.getGrayscaleEnabled());
        route.setVersion(oldRoute.getVersion() + 1);
        
        updateById(route);
        
        routePluginMapper.deleteByRouteId(id);
        if (!CollectionUtils.isEmpty(dto.getPlugins())) {
            saveRoutePlugins(id, dto.getPlugins());
        }
        
        routePredicateMapper.deleteByRouteId(id);
        if (dto.getMatching() != null) {
            saveRouteMatching(id, dto.getMatching());
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
        
        routePluginMapper.deleteByRouteId(id);
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
                routePluginMapper.deleteByRouteId(id);
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
    
    private void saveRoutePlugins(Long routeId, List<RoutePluginDTO> plugins) {
        for (int i = 0; i < plugins.size(); i++) {
            RoutePluginDTO pluginDTO = plugins.get(i);
            
            GwPlugin plugin = pluginMapper.selectOneById(pluginDTO.getPluginId());
            if (plugin == null || plugin.getDeleted()) {
                log.warn("[RouteService] 插件不存在，跳过: {}", pluginDTO.getPluginId());
                continue;
            }
            
            GwRoutePlugin rp = new GwRoutePlugin();
            rp.setRouteId(routeId);
            rp.setPluginId(pluginDTO.getPluginId());
            rp.setConfig(pluginDTO.getConfig());
            rp.setPriorityOverride(pluginDTO.getPriorityOverride());
            rp.setEnabled(pluginDTO.getEnabled() != null ? pluginDTO.getEnabled() : true);
            rp.setSortOrder(i);
            rp.setCreateTime(LocalDateTime.now());
            
            routePluginMapper.insert(rp);
        }
    }
    
    private void saveRouteMatching(Long routeId, RouteMatchingDTO matching) {
        int sortOrder = 0;
        LocalDateTime now = LocalDateTime.now();
        
        if (matching.getPath() != null && StringUtils.hasText(matching.getPath().getPattern())) {
            GwPredicate predicate = new GwPredicate();
            predicate.setPredicateName("Path");
            predicate.setPredicateType("PATH");
            predicate.setDescription("路径匹配");
            
            Map<String, Object> args = new HashMap<>();
            args.put("pattern", matching.getPath().getPattern());
            if (matching.getPath().getMatchType() != null) {
                args.put("matchType", matching.getPath().getMatchType());
            }
            if (matching.getPath().getIgnoreCase() != null) {
                args.put("ignoreCase", matching.getPath().getIgnoreCase());
            }
            predicate.setArgs(args);
            predicate.setIsSystem(false);
            predicate.setEnabled(true);
            predicate.setDeleted(false);
            predicate.setCreateTime(now);
            predicate.setUpdateTime(now);
            
            predicateMapper.insert(predicate);
            
            GwRoutePredicate rp = new GwRoutePredicate();
            rp.setRouteId(routeId);
            rp.setPredicateId(predicate.getId());
            rp.setSortOrder(sortOrder++);
            rp.setCreateTime(now);
            routePredicateMapper.insert(rp);
        }
        
        if (!CollectionUtils.isEmpty(matching.getMethods())) {
            GwPredicate predicate = new GwPredicate();
            predicate.setPredicateName("Method");
            predicate.setPredicateType("METHOD");
            predicate.setDescription("方法匹配");
            
            Map<String, Object> args = new HashMap<>();
            args.put("methods", matching.getMethods());
            predicate.setArgs(args);
            predicate.setIsSystem(false);
            predicate.setEnabled(true);
            predicate.setDeleted(false);
            predicate.setCreateTime(now);
            predicate.setUpdateTime(now);
            
            predicateMapper.insert(predicate);
            
            GwRoutePredicate rp = new GwRoutePredicate();
            rp.setRouteId(routeId);
            rp.setPredicateId(predicate.getId());
            rp.setSortOrder(sortOrder++);
            rp.setCreateTime(now);
            routePredicateMapper.insert(rp);
        }
        
        if (!CollectionUtils.isEmpty(matching.getHosts())) {
            GwPredicate predicate = new GwPredicate();
            predicate.setPredicateName("Host");
            predicate.setPredicateType("HOST");
            predicate.setDescription("Host匹配");
            
            Map<String, Object> args = new HashMap<>();
            args.put("hosts", matching.getHosts());
            predicate.setArgs(args);
            predicate.setIsSystem(false);
            predicate.setEnabled(true);
            predicate.setDeleted(false);
            predicate.setCreateTime(now);
            predicate.setUpdateTime(now);
            
            predicateMapper.insert(predicate);
            
            GwRoutePredicate rp = new GwRoutePredicate();
            rp.setRouteId(routeId);
            rp.setPredicateId(predicate.getId());
            rp.setSortOrder(sortOrder++);
            rp.setCreateTime(now);
            routePredicateMapper.insert(rp);
        }
        
        if (!CollectionUtils.isEmpty(matching.getHeaders())) {
            GwPredicate predicate = new GwPredicate();
            predicate.setPredicateName("Header");
            predicate.setPredicateType("HEADER");
            predicate.setDescription("Header匹配");
            
            Map<String, Object> args = new HashMap<>();
            args.put("headers", matching.getHeaders());
            predicate.setArgs(args);
            predicate.setIsSystem(false);
            predicate.setEnabled(true);
            predicate.setDeleted(false);
            predicate.setCreateTime(now);
            predicate.setUpdateTime(now);
            
            predicateMapper.insert(predicate);
            
            GwRoutePredicate rp = new GwRoutePredicate();
            rp.setRouteId(routeId);
            rp.setPredicateId(predicate.getId());
            rp.setSortOrder(sortOrder++);
            rp.setCreateTime(now);
            routePredicateMapper.insert(rp);
        }
        
        if (!CollectionUtils.isEmpty(matching.getQueries())) {
            GwPredicate predicate = new GwPredicate();
            predicate.setPredicateName("Query");
            predicate.setPredicateType("QUERY");
            predicate.setDescription("Query参数匹配");
            
            Map<String, Object> args = new HashMap<>();
            args.put("queries", matching.getQueries());
            predicate.setArgs(args);
            predicate.setIsSystem(false);
            predicate.setEnabled(true);
            predicate.setDeleted(false);
            predicate.setCreateTime(now);
            predicate.setUpdateTime(now);
            
            predicateMapper.insert(predicate);
            
            GwRoutePredicate rp = new GwRoutePredicate();
            rp.setRouteId(routeId);
            rp.setPredicateId(predicate.getId());
            rp.setSortOrder(sortOrder++);
            rp.setCreateTime(now);
            routePredicateMapper.insert(rp);
        }
    }
    
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
    
    private RouteVO convertToVO(GwRoute route) {
        RouteVO vo = new RouteVO();
        vo.setId(route.getId());
        vo.setRouteId(route.getRouteId());
        vo.setRouteName(route.getRouteName());
        vo.setDescription(route.getDescription());
        vo.setUri(route.getUri());
        vo.setMetadata(route.getMetadata());
        vo.setOrder(route.getOrder());
        vo.setLoadBalanceStrategy(route.getLoadBalanceStrategy());
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
    
    private List<PluginVO> loadPlugins(Long routeId) {
        List<Map<String, Object>> plugins = routePluginMapper.findPluginsByRouteId(routeId);
        return plugins.stream()
                .map(map -> {
                    PluginVO vo = new PluginVO();
                    vo.setId(getLong(map, "route_plugin_id"));
                    vo.setPluginId(getLong(map, "plugin_id"));
                    vo.setPluginName((String) map.get("plugin_name"));
                    vo.setPluginType((String) map.get("plugin_type"));
                    vo.setConfig(parseConfig(map.get("config")));
                    vo.setPriorityOverride(getInteger(map, "priority_override"));
                    vo.setDefaultPriority(getInteger(map, "default_priority"));
                    
                    Integer priorityOverride = getInteger(map, "priority_override");
                    Integer defaultPriority = getInteger(map, "default_priority");
                    vo.setEffectivePriority(priorityOverride != null ? priorityOverride : 
                            (defaultPriority != null ? defaultPriority : 5000));
                    
                    Boolean enabled = (Boolean) map.get("enabled");
                    vo.setEnabled(enabled != null ? enabled : true);
                    vo.setPhase((String) map.get("phase"));
                    
                    return vo;
                })
                .collect(Collectors.toList());
    }
    
    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }
    
    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        return null;
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