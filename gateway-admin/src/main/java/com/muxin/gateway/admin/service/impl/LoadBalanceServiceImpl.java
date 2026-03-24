package com.muxin.gateway.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.muxin.gateway.admin.entity.GwLoadBalance;
import com.muxin.gateway.admin.entity.GwRoute;
import com.muxin.gateway.admin.exception.BusinessException;
import com.muxin.gateway.admin.mapper.LoadBalanceMapper;
import com.muxin.gateway.admin.mapper.RouteMapper;
import com.muxin.gateway.admin.model.dto.LoadBalanceCreateDTO;
import com.muxin.gateway.admin.model.dto.LoadBalanceQueryDTO;
import com.muxin.gateway.admin.model.dto.LoadBalanceUpdateDTO;
import com.muxin.gateway.admin.model.vo.ConfigFieldVO;
import com.muxin.gateway.admin.model.vo.LoadBalanceStrategyVO;
import com.muxin.gateway.admin.model.vo.LoadBalanceVO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.service.LoadBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 负载均衡服务实现
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoadBalanceServiceImpl extends ServiceImpl<LoadBalanceMapper, GwLoadBalance> implements LoadBalanceService {
    
    private final RouteMapper routeMapper;
    
    private static final Map<String, String> STRATEGY_DESC = Map.of(
            "ROUND_ROBIN", "轮询负载均衡，依次选择可用地址",
            "RANDOM", "随机负载均衡，随机选择可用地址",
            "WEIGHTED_ROUND_ROBIN", "加权轮询负载均衡，根据权重选择地址",
            "LEAST_CONNECTIONS", "最少连接负载均衡，选择连接数最少的地址"
    );
    
    @Override
    public PageVO<LoadBalanceVO> pageQuery(LoadBalanceQueryDTO query) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(GwLoadBalance.class);
        
        if (query.getRouteId() != null) {
            wrapper.and("route_id = " + query.getRouteId());
        }
        
        if (query.getStrategy() != null && !query.getStrategy().isEmpty()) {
            wrapper.and("strategy = '" + query.getStrategy() + "'");
        }
        
        if (query.getEnabled() != null) {
            wrapper.and("enabled = " + (query.getEnabled() ? 1 : 0));
        }
        
        wrapper.orderBy("create_time DESC");
        
        com.mybatisflex.core.paginate.Page<GwLoadBalance> page = 
                page(new com.mybatisflex.core.paginate.Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        
        List<LoadBalanceVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageVO.<LoadBalanceVO>builder()
                .data(voList)
                .total(page.getTotalRow())
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize())
                .totalPages((int) page.getTotalPage())
                .build();
    }
    
    @Override
    public LoadBalanceVO getByRouteId(Long routeId) {
        GwRoute route = routeMapper.selectOneById(routeId);
        if (route == null || route.getDeleted()) {
            throw new BusinessException("路由不存在");
        }
        
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(GwLoadBalance.class)
                .where("route_id = " + routeId);
        
        GwLoadBalance entity = mapper.selectOneByQuery(wrapper);
        if (entity == null) {
            return null;
        }
        return convertToVO(entity);
    }
    
    @Override
    public LoadBalanceVO getDetail(Long id) {
        GwLoadBalance entity = getById(id);
        if (entity == null) {
            throw new BusinessException("负载均衡配置不存在");
        }
        return convertToVO(entity);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(LoadBalanceCreateDTO dto) {
        GwRoute route = routeMapper.selectOneById(dto.getRouteId());
        if (route == null || route.getDeleted()) {
            throw new BusinessException("路由不存在");
        }
        
        QueryWrapper existWrapper = QueryWrapper.create()
                .where("route_id = " + dto.getRouteId());
        
        GwLoadBalance exist = mapper.selectOneByQuery(existWrapper);
        if (exist != null) {
            throw new BusinessException("该路由已配置负载均衡，请直接编辑");
        }
        
        validateStrategy(dto.getStrategy());
        
        GwLoadBalance entity = new GwLoadBalance();
        entity.setRouteId(dto.getRouteId());
        entity.setStrategy(dto.getStrategy());
        entity.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setCreateBy(StpUtil.getLoginIdAsString());
        
        if (dto.getConfig() != null) {
            GwLoadBalance.LoadBalanceConfig config = new GwLoadBalance.LoadBalanceConfig();
            config.setStrategyConfig(dto.getConfig());
            entity.setConfig(config);
        }
        
        save(entity);
        
        log.info("创建负载均衡配置成功：routeId={}", dto.getRouteId());
        return entity.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, LoadBalanceUpdateDTO dto) {
        GwLoadBalance entity = getById(id);
        if (entity == null) {
            throw new BusinessException("负载均衡配置不存在");
        }
        
        validateStrategy(dto.getStrategy());
        
        entity.setStrategy(dto.getStrategy());
        entity.setEnabled(dto.getEnabled());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateBy(StpUtil.getLoginIdAsString());
        
        if (dto.getConfig() != null) {
            GwLoadBalance.LoadBalanceConfig config = entity.getConfig();
            if (config == null) {
                config = new GwLoadBalance.LoadBalanceConfig();
            }
            config.setStrategyConfig(dto.getConfig());
            entity.setConfig(config);
        }
        
        updateById(entity);
        
        log.info("更新负载均衡配置成功：id={}", id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        GwLoadBalance entity = getById(id);
        if (entity == null) {
            throw new BusinessException("负载均衡配置不存在");
        }
        
        removeById(id);
        
        log.info("删除负载均衡配置成功：id={}", id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(Long id) {
        GwLoadBalance entity = getById(id);
        if (entity == null) {
            throw new BusinessException("负载均衡配置不存在");
        }
        entity.setEnabled(true);
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateBy(StpUtil.getLoginIdAsString());
        updateById(entity);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        GwLoadBalance entity = getById(id);
        if (entity == null) {
            throw new BusinessException("负载均衡配置不存在");
        }
        entity.setEnabled(false);
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateBy(StpUtil.getLoginIdAsString());
        updateById(entity);
    }
    
    @Override
    public List<LoadBalanceStrategyVO> getStrategies() {
        List<LoadBalanceStrategyVO> strategies = new ArrayList<>();
        
        strategies.add(LoadBalanceStrategyVO.builder()
                .code("ROUND_ROBIN")
                .name("轮询")
                .description("依次选择可用地址，适用于服务器性能相近的场景")
                .configFields(List.of(
                        ConfigFieldVO.builder().field("resetPeriod").label("重置周期(秒)").type("number")
                                .defaultValue(3600).description("计数器重置周期").build()
                ))
                .build());
        
        strategies.add(LoadBalanceStrategyVO.builder()
                .code("RANDOM")
                .name("随机")
                .description("随机选择可用地址，实现简单，分布较均匀")
                .configFields(List.of())
                .build());
        
        strategies.add(LoadBalanceStrategyVO.builder()
                .code("WEIGHTED_ROUND_ROBIN")
                .name("加权轮询")
                .description("根据权重选择地址，适用于服务器性能差异较大的场景")
                .configFields(List.of(
                        ConfigFieldVO.builder().field("resetPeriod").label("重置周期(秒)").type("number")
                                .defaultValue(3600).description("计数器重置周期").build()
                ))
                .build());
        
        strategies.add(LoadBalanceStrategyVO.builder()
                .code("LEAST_CONNECTIONS")
                .name("最少连接")
                .description("选择连接数最少的地址，适用于长连接场景")
                .configFields(List.of(
                        ConfigFieldVO.builder().field("connectionTimeout").label("连接超时(ms)").type("number")
                                .defaultValue(5000).description("连接超时时间").build()
                ))
                .build());
        
        return strategies;
    }
    
    private void validateStrategy(String strategy) {
        if (!STRATEGY_DESC.containsKey(strategy)) {
            throw new BusinessException("不支持的负载均衡策略: " + strategy);
        }
    }
    
    private LoadBalanceVO convertToVO(GwLoadBalance entity) {
        LoadBalanceVO vo = new LoadBalanceVO();
        vo.setId(entity.getId());
        vo.setRouteId(entity.getRouteId());
        vo.setStrategy(entity.getStrategy());
        vo.setStrategyDesc(STRATEGY_DESC.get(entity.getStrategy()));
        vo.setEnabled(entity.getEnabled());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        
        if (entity.getConfig() != null) {
            vo.setConfig(entity.getConfig().getStrategyConfig());
        }
        
        GwRoute route = routeMapper.selectOneById(entity.getRouteId());
        if (route != null) {
            vo.setRouteName(route.getRouteName());
        }
        
        return vo;
    }
}