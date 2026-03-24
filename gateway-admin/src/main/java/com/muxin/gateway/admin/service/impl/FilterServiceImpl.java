package com.muxin.gateway.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.muxin.gateway.admin.entity.GwFilter;
import static com.muxin.gateway.admin.entity.table.Tables.*;
import com.muxin.gateway.admin.exception.BusinessException;
import com.muxin.gateway.admin.mapper.FilterMapper;
import com.muxin.gateway.admin.mapper.RouteFilterMapper;
import com.muxin.gateway.admin.model.dto.FilterCreateDTO;
import com.muxin.gateway.admin.model.dto.FilterQueryDTO;
import com.muxin.gateway.admin.model.dto.FilterUpdateDTO;
import com.muxin.gateway.admin.model.vo.ConfigFieldVO;
import com.muxin.gateway.admin.model.vo.FilterTypeVO;
import com.muxin.gateway.admin.model.vo.FilterVO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.RouteSimpleVO;
import com.muxin.gateway.admin.service.FilterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 过滤器服务实现
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FilterServiceImpl extends ServiceImpl<FilterMapper, GwFilter> implements FilterService {
    
    private final FilterMapper filterMapper;
    private final RouteFilterMapper routeFilterMapper;
    
    @Override
    public PageVO<FilterVO> pageQuery(FilterQueryDTO query) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(GW_FILTER)
                .where(GW_FILTER.DELETED.eq(false));
        
        if (StringUtils.hasText(query.getFilterName())) {
            wrapper.and(GW_FILTER.FILTER_NAME.like("%" + query.getFilterName() + "%"));
        }
        
        if (StringUtils.hasText(query.getFilterType())) {
            wrapper.and(GW_FILTER.FILTER_TYPE.eq(query.getFilterType()));
        }
        
        if (query.getEnabled() != null) {
            wrapper.and(GW_FILTER.ENABLED.eq(query.getEnabled()));
        }
        
        if (query.getIsSystem() != null) {
            wrapper.and(GW_FILTER.IS_SYSTEM.eq(query.getIsSystem()));
        }
        
        wrapper.orderBy(GW_FILTER.FILTER_TYPE.asc(), 
                       GW_FILTER.ORDER.asc(),
                       GW_FILTER.CREATE_TIME.desc());
        
        com.mybatisflex.core.paginate.Page<GwFilter> page = page(
                new com.mybatisflex.core.paginate.Page<>(query.getPageNum(), query.getPageSize()), 
                wrapper);
        
        List<FilterVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageVO.<FilterVO>builder()
                .data(voList)
                .total(page.getTotalRow())
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize())
                .totalPages((int) page.getTotalPage())
                .build();
    }
    
    @Override
    public List<FilterVO> getAvailableFilters() {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(GW_FILTER)
                .where(GW_FILTER.DELETED.eq(false))
                .and(GW_FILTER.ENABLED.eq(true))
                .orderBy(GW_FILTER.FILTER_TYPE.asc(), 
                         GW_FILTER.ORDER.asc(),
                         GW_FILTER.CREATE_TIME.desc());
        
        List<GwFilter> filters = list(wrapper);
        return filters.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<FilterVO> getByType(String type) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(GW_FILTER)
                .where(GW_FILTER.DELETED.eq(false))
                .and(GW_FILTER.FILTER_TYPE.eq(type))
                .orderBy(GW_FILTER.ORDER.asc(), 
                         GW_FILTER.CREATE_TIME.desc());
        
        List<GwFilter> filters = list(wrapper);
        return filters.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public FilterVO getFilterDetail(Long id) {
        GwFilter filter = getById(id);
        if (filter == null || filter.getDeleted()) {
            throw new BusinessException("过滤器不存在");
        }
        return convertToVO(filter);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFilter(FilterCreateDTO dto) {
        GwFilter filter = new GwFilter();
        BeanUtils.copyProperties(dto, filter);
        filter.setIsSystem(false);
        filter.setEnabled(true);
        filter.setDeleted(false);
        filter.setCreateTime(LocalDateTime.now());
        filter.setUpdateTime(LocalDateTime.now());
        filter.setCreateBy(StpUtil.getLoginIdAsString());
        
        save(filter);
        
        log.info("创建过滤器成功：{}", filter.getFilterName());
        return filter.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFilter(Long id, FilterUpdateDTO dto) {
        GwFilter filter = getById(id);
        if (filter == null || filter.getDeleted()) {
            throw new BusinessException("过滤器不存在");
        }
        
        if (filter.getIsSystem()) {
            throw new BusinessException("系统内置过滤器不允许修改");
        }
        
        BeanUtils.copyProperties(dto, filter);
        filter.setUpdateTime(LocalDateTime.now());
        filter.setUpdateBy(StpUtil.getLoginIdAsString());
        
        updateById(filter);
        
        log.info("更新过滤器成功：{}", filter.getFilterName());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFilter(Long id) {
        GwFilter filter = getById(id);
        if (filter == null || filter.getDeleted()) {
            throw new BusinessException("过滤器不存在");
        }
        
        if (filter.getIsSystem()) {
            throw new BusinessException("系统内置过滤器不允许删除");
        }
        
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(GW_ROUTE_FILTER)
                .where(GW_ROUTE_FILTER.FILTER_ID.eq(id));
        
        long routeCount = routeFilterMapper.selectCountByQuery(wrapper);
        if (routeCount > 0) {
            throw new BusinessException("该过滤器正在被" + routeCount + "个路由使用，无法删除");
        }
        
        filter.setDeleted(true);
        filter.setUpdateTime(LocalDateTime.now());
        filter.setUpdateBy(StpUtil.getLoginIdAsString());
        updateById(filter);
        
        log.info("删除过滤器成功：{}", filter.getFilterName());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        
        for (Long id : ids) {
            deleteFilter(id);
        }
    }
    
    @Override
    public void enableFilter(Long id) {
        updateStatus(id, true);
    }
    
    @Override
    public void disableFilter(Long id) {
        updateStatus(id, false);
    }
    
    @Override
    public List<FilterTypeVO> getFilterTypes() {
        List<FilterTypeVO> types = new ArrayList<>();
        
        types.add(new FilterTypeVO("AddRequestHeader", "添加请求头", "向请求中添加Header",
                Map.of("name", "X-Request-From", "value", "gateway"),
                Arrays.asList(
                        ConfigFieldVO.builder().field("name").label("Header名称").type("string").required(true).placeholder("X-Custom-Header").build(),
                        ConfigFieldVO.builder().field("value").label("Header值").type("string").required(true).placeholder("custom-value").build()
                )));
        
        types.add(new FilterTypeVO("AddResponseHeader", "添加响应头", "向响应中添加Header",
                Map.of("name", "X-Response-From", "value", "gateway"),
                Arrays.asList(
                        ConfigFieldVO.builder().field("name").label("Header名称").type("string").required(true).placeholder("X-Custom-Header").build(),
                        ConfigFieldVO.builder().field("value").label("Header值").type("string").required(true).placeholder("custom-value").build()
                )));
        
        types.add(new FilterTypeVO("RemoveRequestHeader", "移除请求头", "移除请求中的Header",
                Map.of("name", "X-Internal-Header"),
                Arrays.asList(
                        ConfigFieldVO.builder().field("name").label("Header名称").type("string").required(true).placeholder("X-Remove-Header").build()
                )));
        
        types.add(new FilterTypeVO("RemoveResponseHeader", "移除响应头", "移除响应中的Header",
                Map.of("name", "X-Internal-Response"),
                Arrays.asList(
                        ConfigFieldVO.builder().field("name").label("Header名称").type("string").required(true).placeholder("X-Remove-Header").build()
                )));
        
        types.add(new FilterTypeVO("RewritePath", "路径重写", "重写请求路径",
                Map.of("regexp", "/api/v1/(?<segment>.*)", "replacement", "/${segment}"),
                Arrays.asList(
                        ConfigFieldVO.builder().field("regexp").label("匹配正则").type("string").required(true).placeholder("/api/v1/(?<segment>.*)").description("正则表达式，支持命名分组").build(),
                        ConfigFieldVO.builder().field("replacement").label("替换路径").type("string").required(true).placeholder("/${segment}").description("替换后的路径，可使用${group}引用分组").build()
                )));
        
        types.add(new FilterTypeVO("RequestRateLimiter", "请求限流", "限制请求速率",
                Map.of("replenishRate", 10, "burstCapacity", 20),
                Arrays.asList(
                        ConfigFieldVO.builder().field("replenishRate").label("令牌填充速率").type("number").required(true).defaultValue(10).description("每秒填充的令牌数").build(),
                        ConfigFieldVO.builder().field("burstCapacity").label("令牌桶容量").type("number").required(true).defaultValue(20).description("令牌桶最大容量").build()
                )));
        
        types.add(new FilterTypeVO("CircuitBreaker", "熔断器", "提供熔断功能",
                Map.of("name", "myCircuitBreaker", "fallbackUri", "/fallback"),
                Arrays.asList(
                        ConfigFieldVO.builder().field("name").label("熔断器名称").type("string").required(true).placeholder("myCircuitBreaker").build(),
                        ConfigFieldVO.builder().field("fallbackUri").label("降级URI").type("string").required(false).placeholder("/fallback").description("熔断后的降级处理路径").build()
                )));
        
        types.add(new FilterTypeVO("Retry", "重试", "失败重试",
                Map.of("retries", 3, "statuses", List.of("BAD_GATEWAY"), "methods", List.of("GET", "POST")),
                Arrays.asList(
                        ConfigFieldVO.builder().field("retries").label("重试次数").type("number").required(true).defaultValue(3).description("最大重试次数").build(),
                        ConfigFieldVO.builder().field("statuses").label("重试状态码").type("array").required(false).defaultValue(Arrays.asList("BAD_GATEWAY", "SERVICE_UNAVAILABLE")).description("触发重试的HTTP状态码").build(),
                        ConfigFieldVO.builder().field("methods").label("重试方法").type("array").required(false).defaultValue(Arrays.asList("GET", "POST")).description("允许重试的HTTP方法").build()
                )));
        
        return types;
    }

    @Override
    public List<RouteSimpleVO> getUsedRoutes(Long id) {
        GwFilter filter = getById(id);
        if (filter == null || filter.getDeleted()) {
            throw new BusinessException("过滤器不存在");
        }
        
        List<Map<String, Object>> routes = routeFilterMapper.findRoutesByFilterId(id);
        return routes.stream()
                .map(map -> RouteSimpleVO.builder()
                        .id(((Number) map.get("id")).longValue())
                        .routeId((String) map.get("routeId"))
                        .routeName((String) map.get("routeName"))
                        .enabled(toBoolean(map.get("enabled")))
                        .build())
                .collect(Collectors.toList());
    }
    
    private Boolean toBoolean(Object value) {
        if (value == null) return true;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).intValue() != 0;
        return Boolean.parseBoolean(value.toString());
    }
    
    private void updateStatus(Long id, Boolean enabled) {
        GwFilter filter = getById(id);
        if (filter == null || filter.getDeleted()) {
            throw new BusinessException("过滤器不存在");
        }
        
        filter.setEnabled(enabled);
        filter.setUpdateTime(LocalDateTime.now());
        filter.setUpdateBy(StpUtil.getLoginIdAsString());
        updateById(filter);
        
        log.info("更新过滤器状态成功：{}，状态：{}", filter.getFilterName(), enabled ? "启用" : "禁用");
    }
    
    private FilterVO convertToVO(GwFilter filter) {
        FilterVO vo = new FilterVO();
        BeanUtils.copyProperties(filter, vo);
        
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(GW_ROUTE_FILTER)
                .where(GW_ROUTE_FILTER.FILTER_ID.eq(filter.getId()));
        
        vo.setUsageCount(routeFilterMapper.selectCountByQuery(wrapper));
        
        return vo;
    }
} 