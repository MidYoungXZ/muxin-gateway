package com.muxin.gateway.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.muxin.gateway.admin.entity.GwServiceNode;
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
    public List<ServiceStatsVO> getServiceStats(String serviceName) {
        List<Map<String, Object>> stats = mapper.selectServiceStats();
        
        return stats.stream()
                .filter(map -> !StringUtils.hasText(serviceName) || 
                        map.get("serviceName").toString().contains(serviceName))
                .map(map -> ServiceStatsVO.builder()
                        .serviceName((String) map.get("serviceName"))
                        .totalNodes(((Number) map.get("totalNodes")).intValue())
                        .healthyNodes(((Number) map.get("healthyNodes")).intValue())
                        .unhealthyNodes(((Number) map.get("unhealthyNodes")).intValue())
                        .enabledNodes(((Number) map.get("enabledNodes")).intValue())
                        .disabledNodes(((Number) map.get("disabledNodes")).intValue())
                        .maintenanceNodes(((Number) map.get("maintenanceNodes")).intValue())
                        .build())
                .collect(Collectors.toList());
    }
    
    @Override
    public PageVO<ServiceNodeVO> getNodesByService(String serviceName, int pageNum, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where("service_name = '" + serviceName + "'")
                .and("deleted = 0")
                .orderBy("create_time DESC");
        
        com.mybatisflex.core.paginate.Page<GwServiceNode> page = 
                page(new com.mybatisflex.core.paginate.Page<>(pageNum, pageSize), wrapper);
        
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
        if (entity == null || entity.getDeleted()) {
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
                .where("service_name = '" + serviceName + "'")
                .and("deleted = 0");
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
        entity.setDeleted(false);
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
        
        RegistryDiscoveryService discoveryService = getDiscoveryService(config.getRegistryType());
        List<DiscoveredNodeVO> discoveredNodes = discoveryService.discoverNodes(serviceName, config);
        
        if (CollectionUtils.isEmpty(discoveredNodes)) {
            throw new BusinessException("未在注册中心发现服务: " + serviceName);
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
            entity.setDeleted(false);
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
        entity.setDeleted(false);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setCreateBy(StpUtil.getLoginIdAsString());
        return entity;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ServiceNodeCreateDTO dto) {
        QueryWrapper existWrapper = QueryWrapper.create()
                .where("node_id = '" + dto.getNodeId() + "'");
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
        entity.setDeleted(false);
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
        if (entity == null || entity.getDeleted()) {
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
        if (entity == null || entity.getDeleted()) {
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
        return mapper.selectServiceNames();
    }
    
    @Override
    public List<RouteSimpleVO> getRoutesByServiceName(String serviceName) {
        List<Map<String, Object>> routes = routeMapper.findRoutesByServiceName(serviceName);
        return routes.stream()
                .map(map -> RouteSimpleVO.builder()
                        .id(((Number) map.get("id")).longValue())
                        .routeId((String) map.get("routeId"))
                        .routeName((String) map.get("routeName"))
                        .enabled(toBoolean(map.get("enabled")))
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
                .where("service_name = '" + serviceName + "'")
                .and("deleted = 0");
        
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
        if (entity == null || entity.getDeleted()) {
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