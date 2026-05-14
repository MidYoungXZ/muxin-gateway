package com.muxin.gateway.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.muxin.gateway.admin.entity.GwRoute;
import com.muxin.gateway.admin.entity.GwServiceNode;
import static com.muxin.gateway.admin.entity.table.GwRouteTableDef.GW_ROUTE;
import com.muxin.gateway.admin.exception.BusinessException;
import com.muxin.gateway.admin.mapper.RouteMapper;
import com.muxin.gateway.admin.mapper.ServiceNodeMapper;
import com.muxin.gateway.admin.model.dto.DiscoveryConfigDTO;
import com.muxin.gateway.admin.model.dto.DiscoveryRequestDTO;
import com.muxin.gateway.admin.model.dto.ServiceCreateDTO;
import com.muxin.gateway.admin.model.dto.ServiceNodeCreateDTO;
import com.muxin.gateway.admin.model.dto.ServiceNodeDTO;
import com.muxin.gateway.admin.model.dto.ServiceNodeUpdateDTO;
import com.muxin.gateway.admin.model.vo.DiscoveredNodeVO;
import com.muxin.gateway.admin.model.vo.DiscoveryTestResultVO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.RouteSimpleVO;
import com.muxin.gateway.admin.model.vo.ServiceNodeVO;
import com.muxin.gateway.admin.model.vo.ServiceStatsVO;
import com.muxin.gateway.admin.service.ConfigRefreshService;
import com.muxin.gateway.admin.service.RegistryDiscoveryService;
import com.muxin.gateway.admin.service.ServiceNodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceNodeServiceImpl extends ServiceImpl<ServiceNodeMapper, GwServiceNode> implements ServiceNodeService {
    
    private static final Map<Integer, String> STATUS_DESC = Map.of(
            0, "禁用",
            1, "启用",
            2, "维护中"
    );
    
    private final RouteMapper routeMapper;
    private final ConfigRefreshService configRefreshService;
    private final List<RegistryDiscoveryService> discoveryServices;
    
    @Override
    public PageVO<ServiceStatsVO> getServiceStats(String serviceName, int pageNum, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create()
                .from(GwServiceNode.class);
        
        List<GwServiceNode> nodes = mapper.selectListByQuery(wrapper);
        
        Map<String, ServiceStatsVO.ServiceStatsVOBuilder> statsBuilders = new java.util.LinkedHashMap<>();
        
        for (GwServiceNode node : nodes) {
            String name = node.getServiceName();
            if (!StringUtils.hasText(name)) continue;
            if (StringUtils.hasText(serviceName) && !name.contains(serviceName)) continue;
            
            ServiceStatsVO.ServiceStatsVOBuilder builder = statsBuilders.computeIfAbsent(
                name, k -> ServiceStatsVO.builder()
                    .serviceName(k)
                    .totalNodes(0)
                    .healthyNodes(0)
                    .unhealthyNodes(0)
                    .enabledNodes(0)
                    .disabledNodes(0)
                    .maintenanceNodes(0)
            );
            
            ServiceStatsVO stats = builder.build();
            statsBuilders.put(name, builder
                .totalNodes(stats.getTotalNodes() + 1)
                .healthyNodes(stats.getHealthyNodes() + (Boolean.TRUE.equals(node.getLastCheckResult()) ? 1 : 0))
                .unhealthyNodes(stats.getUnhealthyNodes() + (Boolean.FALSE.equals(node.getLastCheckResult()) ? 1 : 0))
                .enabledNodes(stats.getEnabledNodes() + (Integer.valueOf(1).equals(node.getStatus()) ? 1 : 0))
                .disabledNodes(stats.getDisabledNodes() + (Integer.valueOf(0).equals(node.getStatus()) ? 1 : 0))
                .maintenanceNodes(stats.getMaintenanceNodes() + (Integer.valueOf(2).equals(node.getStatus()) ? 1 : 0))
            );
        }
        
        List<ServiceStatsVO> allStats = statsBuilders.values().stream()
                .map(ServiceStatsVO.ServiceStatsVOBuilder::build)
                .sorted((a, b) -> a.getServiceName().compareTo(b.getServiceName()))
                .collect(Collectors.toList());
        
        long total = allStats.size();
        int totalPages = (int) Math.ceil((double) total / pageSize);
        int offset = (pageNum - 1) * pageSize;
        int end = Math.min(offset + pageSize, allStats.size());
        
        List<ServiceStatsVO> pageData = allStats.subList(offset, end);
        
        return PageVO.<ServiceStatsVO>builder()
                .data(pageData)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .build();
    }
    
    @Override
    public PageVO<ServiceNodeVO> getNodesByService(String serviceName, int pageNum, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create()
                .from(GwServiceNode.class)
                .where(GwServiceNode::getServiceName).eq(serviceName)
                .orderBy(GwServiceNode::getCreateTime, false);
        
        com.mybatisflex.core.paginate.Page<GwServiceNode> page = 
                mapper.paginate(pageNum, pageSize, wrapper);
        
        List<ServiceNodeVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageVO.<ServiceNodeVO>builder()
                .data(voList)
                .total(page.getTotalRow())
                .pageNum(pageNum)
                .pageSize(pageSize)
                .totalPages((int) page.getTotalPage())
                .build();
    }
    
    @Override
    public ServiceNodeVO getDetail(Long id) {
        GwServiceNode entity = getById(id);
        if (entity == null) {
            throw new BusinessException("节点不存在");
        }
        return convertToVO(entity);
    }
    
    @Override
    public Long createService(ServiceCreateDTO dto) {
        String serviceName = dto.getServiceName();
        
        checkServiceExists(serviceName);
        
        Long firstId;
        if (ServiceCreateDTO.MODE_DISCOVERY.equals(dto.getCreateMode())) {
            firstId = createServiceFromDiscovery(serviceName, dto.getDiscoveryConfig());
        } else {
            firstId = createServiceManual(serviceName, dto.getNodes());
        }
        
        configRefreshService.refreshServices();
        return firstId;
    }
    
    private void checkServiceExists(String serviceName) {
        QueryWrapper existWrapper = QueryWrapper.create()
                .from(GwServiceNode.class)
                .where(GwServiceNode::getServiceName).eq(serviceName);
        long existCount = mapper.selectCountByQuery(existWrapper);
        if (existCount > 0) {
            throw new BusinessException("服务已存在: " + serviceName);
        }
    }
    
    private Long createServiceManual(String serviceName, List<ServiceNodeDTO> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return createDefaultNode(serviceName);
        }
        
        Long firstId = null;
        int index = 1;
        for (ServiceNodeDTO nodeDTO : nodes) {
            GwServiceNode entity = createNodeEntity(serviceName, nodeDTO, index++);
            save(entity);
            if (firstId == null) {
                firstId = entity.getId();
            }
        }
        
        log.info("[ServiceNodeService] 手动创建服务成功: {}, 节点数: {}", serviceName, nodes.size());
        return firstId;
    }
    
    private Long createDefaultNode(String serviceName) {
        GwServiceNode entity = new GwServiceNode();
        entity.setNodeId(serviceName + "-node-1");
        entity.setServiceName(serviceName);
        entity.setNodeName(serviceName + "-节点1");
        entity.setAddress("127.0.0.1");
        entity.setPort(8080);
        entity.setWeight(100);
        entity.setMaxFails(3);
        entity.setFailTimeout(30);
        entity.setBackup(false);
        entity.setHealthCheckEnabled(true);
        entity.setHealthCheckInterval(30);
        entity.setHealthCheckTimeout(5);
        entity.setHealthCheckPath("/health");
        entity.setHealthCheckExpectedStatus(Arrays.asList(200, 201));
        entity.setStatus(1);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setCreateBy(StpUtil.getLoginIdAsString());
        
        save(entity);
        
        log.info("[ServiceNodeService] 创建服务成功（默认节点）: {}", serviceName);
        return entity.getId();
    }
    
    private Long createServiceFromDiscovery(String serviceName, DiscoveryConfigDTO config) {
        if (config == null) {
            throw new BusinessException("注册中心配置不能为空");
        }
        
        String discoveryName = config.getDiscoveryServiceName() != null 
                && !config.getDiscoveryServiceName().isBlank() 
                ? config.getDiscoveryServiceName() : serviceName;
        RegistryDiscoveryService discoveryService = getDiscoveryService(config.getRegistryType());
        List<DiscoveredNodeVO> discoveredNodes = discoveryService.discoverNodes(serviceName, config);
        
        if (CollectionUtils.isEmpty(discoveredNodes)) {
            throw new BusinessException("未在注册中心发现服务: " + discoveryName);
        }
        
        Long firstId = null;
        int index = 1;
        for (DiscoveredNodeVO node : discoveredNodes) {
            GwServiceNode entity = new GwServiceNode();
            entity.setNodeId(serviceName + "-node-" + index);
            entity.setServiceName(serviceName);
            entity.setNodeName(serviceName + "-节点" + index);
            entity.setAddress(node.getAddress());
            entity.setPort(node.getPort());
            entity.setWeight(node.getWeight() != null ? node.getWeight() : 100);
            entity.setMaxFails(3);
            entity.setFailTimeout(30);
            entity.setBackup(false);
            entity.setHealthCheckEnabled(true);
            entity.setHealthCheckInterval(30);
            entity.setHealthCheckTimeout(5);
            entity.setHealthCheckPath("/health");
            entity.setHealthCheckExpectedStatus(Arrays.asList(200, 201));
entity.setStatus(node.getHealthy() != null && node.getHealthy() ? 1 : 0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setCreateBy(StpUtil.getLoginIdAsString());
            
            save(entity);
            if (firstId == null) {
                firstId = entity.getId();
            }
            index++;
        }
        
        log.info("[ServiceNodeService] 从注册中心创建服务成功: {}, 节点数: {}", serviceName, discoveredNodes.size());
        return firstId;
    }
    
    private GwServiceNode createNodeEntity(String serviceName, ServiceNodeDTO dto, int index) {
        GwServiceNode entity = new GwServiceNode();
        entity.setNodeId(serviceName + "-node-" + index);
        entity.setServiceName(serviceName);
        entity.setNodeName(StringUtils.hasText(dto.getNodeName()) ? dto.getNodeName() : serviceName + "-节点" + index);
        entity.setAddress(StringUtils.hasText(dto.getAddress()) ? dto.getAddress() : "127.0.0.1");
        entity.setPort(dto.getPort() != null ? dto.getPort() : 8080);
        entity.setWeight(dto.getWeight() != null ? dto.getWeight() : 100);
        entity.setMaxFails(dto.getMaxFails() != null ? dto.getMaxFails() : 3);
        entity.setFailTimeout(dto.getFailTimeout() != null ? dto.getFailTimeout() : 30);
        entity.setBackup(dto.getBackup() != null ? dto.getBackup() : false);
        entity.setHealthCheckEnabled(dto.getHealthCheckEnabled() != null ? dto.getHealthCheckEnabled() : true);
        entity.setHealthCheckInterval(dto.getHealthCheckInterval() != null ? dto.getHealthCheckInterval() : 30);
        entity.setHealthCheckTimeout(dto.getHealthCheckTimeout() != null ? dto.getHealthCheckTimeout() : 5);
        entity.setHealthCheckPath(StringUtils.hasText(dto.getHealthCheckPath()) ? dto.getHealthCheckPath() : "/health");
        entity.setHealthCheckExpectedStatus(dto.getHealthCheckExpectedStatus() != null ? dto.getHealthCheckExpectedStatus() : Arrays.asList(200, 201));
        entity.setStatus(1);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setCreateBy(StpUtil.getLoginIdAsString());
        return entity;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ServiceNodeCreateDTO dto) {
        QueryWrapper existWrapper = QueryWrapper.create()
                .from(GwServiceNode.class)
                .where(GwServiceNode::getNodeId).eq(dto.getNodeId());
        GwServiceNode exist = mapper.selectOneByQuery(existWrapper);
        if (exist != null) {
            throw new BusinessException("节点ID已存在: " + dto.getNodeId());
        }
        
        GwServiceNode entity = new GwServiceNode();
        entity.setNodeId(dto.getNodeId());
        entity.setServiceName(dto.getServiceName());
        entity.setNodeName(dto.getNodeName());
        entity.setAddress(dto.getAddress());
        entity.setPort(dto.getPort());
        entity.setWeight(dto.getWeight() != null ? dto.getWeight() : 100);
        entity.setMaxFails(dto.getMaxFails() != null ? dto.getMaxFails() : 3);
        entity.setFailTimeout(dto.getFailTimeout() != null ? dto.getFailTimeout() : 30);
        entity.setBackup(dto.getBackup() != null ? dto.getBackup() : false);
        entity.setHealthCheckEnabled(dto.getHealthCheckEnabled() != null ? dto.getHealthCheckEnabled() : true);
        entity.setHealthCheckInterval(dto.getHealthCheckInterval() != null ? dto.getHealthCheckInterval() : 30);
        entity.setHealthCheckTimeout(dto.getHealthCheckTimeout() != null ? dto.getHealthCheckTimeout() : 5);
        entity.setHealthCheckPath(dto.getHealthCheckPath() != null ? dto.getHealthCheckPath() : "/health");
        entity.setHealthCheckExpectedStatus(dto.getHealthCheckExpectedStatus() != null ? 
                dto.getHealthCheckExpectedStatus() : Arrays.asList(200, 201));
        entity.setStatus(1);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setCreateBy(StpUtil.getLoginIdAsString());
        
        save(entity);
        
        log.info("创建服务节点成功：{} - {}", entity.getServiceName(), entity.getNodeId());
        return entity.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ServiceNodeUpdateDTO dto) {
        GwServiceNode entity = getById(id);
        if (entity == null) {
            throw new BusinessException("节点不存在");
        }
        
        entity.setNodeName(dto.getNodeName());
        entity.setAddress(dto.getAddress());
        if (dto.getPort() != null) {
            entity.setPort(dto.getPort());
        }
        if (dto.getWeight() != null) {
            entity.setWeight(dto.getWeight());
        }
        if (dto.getMaxFails() != null) {
            entity.setMaxFails(dto.getMaxFails());
        }
        if (dto.getFailTimeout() != null) {
            entity.setFailTimeout(dto.getFailTimeout());
        }
        if (dto.getBackup() != null) {
            entity.setBackup(dto.getBackup());
        }
        if (dto.getHealthCheckEnabled() != null) {
            entity.setHealthCheckEnabled(dto.getHealthCheckEnabled());
        }
        if (dto.getHealthCheckInterval() != null) {
            entity.setHealthCheckInterval(dto.getHealthCheckInterval());
        }
        if (dto.getHealthCheckTimeout() != null) {
            entity.setHealthCheckTimeout(dto.getHealthCheckTimeout());
        }
        if (dto.getHealthCheckPath() != null) {
            entity.setHealthCheckPath(dto.getHealthCheckPath());
        }
        if (dto.getHealthCheckExpectedStatus() != null) {
            entity.setHealthCheckExpectedStatus(dto.getHealthCheckExpectedStatus());
        }
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateBy(StpUtil.getLoginIdAsString());
        
        updateById(entity);
        
        log.info("更新服务节点成功：{}", entity.getNodeId());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        GwServiceNode entity = getById(id);
        if (entity == null) {
            throw new BusinessException("节点不存在");
        }
        
        removeById(id);
        
        log.info("删除服务节点成功：{}", entity.getNodeId());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(Long id) {
        updateStatus(id, 1);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        updateStatus(id, 0);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void maintenance(Long id) {
        updateStatus(id, 2);
    }
    
    @Override
    public List<String> getServiceNames() {
        QueryWrapper wrapper = QueryWrapper.create()
                .select(GwServiceNode::getServiceName)
                .from(GwServiceNode.class)
                .orderBy(GwServiceNode::getServiceName, true);
        
        return mapper.selectListByQuery(wrapper).stream()
                .map(GwServiceNode::getServiceName)
                .filter(name -> name != null && !name.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
    
    @Override
    public List<RouteSimpleVO> getRoutesByServiceName(String serviceName) {
        List<GwRoute> routes = routeMapper.selectListByQuery(
            QueryWrapper.create()
                .select()
                .where(GW_ROUTE.URI.like("lb://" + serviceName))
                .orderBy(GW_ROUTE.CREATE_TIME.desc())
        );
        
        return routes.stream()
                .map(route -> RouteSimpleVO.builder()
                        .id(route.getId())
                        .routeId(route.getRouteId())
                        .routeName(route.getRouteName())
                        .enabled(route.getEnabled())
                        .build())
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteService(String serviceName) {
        List<RouteSimpleVO> routes = getRoutesByServiceName(serviceName);
        if (!routes.isEmpty()) {
            throw new BusinessException("服务被路由引用，无法删除。引用的路由: " + 
                    routes.stream().map(RouteSimpleVO::getRouteName).collect(Collectors.joining(", ")));
        }
        
        QueryWrapper wrapper = QueryWrapper.create()
                .from(GwServiceNode.class)
                .where(GwServiceNode::getServiceName).eq(serviceName);
        
        List<GwServiceNode> nodes = mapper.selectListByQuery(wrapper);
        if (nodes.isEmpty()) {
            throw new BusinessException("服务不存在: " + serviceName);
        }
        
        mapper.deleteByQuery(wrapper);
        
        configRefreshService.refreshServices();
        log.info("[ServiceNodeService] 删除服务成功: {}, 删除节点数: {}", serviceName, nodes.size());
    }
    
    @Override
    public DiscoveryTestResultVO testDiscoveryConnection(DiscoveryConfigDTO config) {
        if (config == null || !StringUtils.hasText(config.getRegistryType())) {
            return DiscoveryTestResultVO.builder()
                    .success(false)
                    .message("注册中心类型不能为空")
                    .build();
        }
        
        RegistryDiscoveryService discoveryService = getDiscoveryService(config.getRegistryType());
        return discoveryService.testConnection(config);
    }
    
    @Override
    public List<DiscoveredNodeVO> discoverNodes(DiscoveryRequestDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getRegistryType())) {
            throw new BusinessException("注册中心类型不能为空");
        }
        
        RegistryDiscoveryService discoveryService = getDiscoveryService(dto.getRegistryType());
        
        DiscoveryConfigDTO config = new DiscoveryConfigDTO();
        config.setRegistryType(dto.getRegistryType());
        config.setServerAddr(dto.getServerAddr());
        config.setNamespace(dto.getNamespace());
        config.setUsername(dto.getUsername());
        config.setPassword(dto.getPassword());
        config.setGroup(dto.getGroup());
        
        return discoveryService.discoverNodes(dto.getServiceName(), config);
    }
    
    private RegistryDiscoveryService getDiscoveryService(String registryType) {
        if (discoveryServices == null || discoveryServices.isEmpty()) {
            throw new BusinessException("未找到注册中心发现服务");
        }
        
        return discoveryServices.stream()
                .filter(s -> s.getRegistryType().equalsIgnoreCase(registryType))
                .findFirst()
                .orElseThrow(() -> new BusinessException("不支持的注册中心类型: " + registryType));
    }
    
    private Boolean toBoolean(Object value) {
        if (value == null) return true;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).intValue() != 0;
        return Boolean.parseBoolean(value.toString());
    }
    
    private void updateStatus(Long id, Integer status) {
        GwServiceNode entity = getById(id);
        if (entity == null) {
            throw new BusinessException("节点不存在");
        }
        entity.setStatus(status);
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateBy(StpUtil.getLoginIdAsString());
        updateById(entity);
    }
    
    private ServiceNodeVO convertToVO(GwServiceNode entity) {
        ServiceNodeVO vo = new ServiceNodeVO();
        vo.setId(entity.getId());
        vo.setNodeId(entity.getNodeId());
        vo.setServiceName(entity.getServiceName());
        vo.setNodeName(entity.getNodeName());
        vo.setAddress(entity.getAddress());
        vo.setPort(entity.getPort());
        vo.setWeight(entity.getWeight());
        vo.setMaxFails(entity.getMaxFails());
        vo.setFailTimeout(entity.getFailTimeout());
        vo.setBackup(entity.getBackup());
        vo.setHealthCheckEnabled(entity.getHealthCheckEnabled());
        vo.setHealthCheckInterval(entity.getHealthCheckInterval());
        vo.setHealthCheckTimeout(entity.getHealthCheckTimeout());
        vo.setHealthCheckPath(entity.getHealthCheckPath());
        vo.setHealthCheckExpectedStatus(entity.getHealthCheckExpectedStatus());
        vo.setStatus(entity.getStatus());
        vo.setStatusDesc(STATUS_DESC.get(entity.getStatus()));
        vo.setLastCheckTime(entity.getLastCheckTime());
        vo.setLastCheckResult(entity.getLastCheckResult());
        vo.setHealthy(entity.getLastCheckResult() != null && entity.getLastCheckResult() == 1);
        return vo;
    }
}