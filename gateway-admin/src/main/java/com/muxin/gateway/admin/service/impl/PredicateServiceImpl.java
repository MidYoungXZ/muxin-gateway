package com.muxin.gateway.admin.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.muxin.gateway.admin.entity.GwPredicate;
import static com.muxin.gateway.admin.entity.table.GwPredicateTableDef.GW_PREDICATE;
import com.muxin.gateway.admin.enums.PredicateType;
import com.muxin.gateway.admin.exception.BusinessException;
import com.muxin.gateway.admin.mapper.PredicateMapper;
import com.muxin.gateway.admin.mapper.RoutePredicateMapper;
import com.muxin.gateway.admin.model.dto.PredicateCreateDTO;
import com.muxin.gateway.admin.model.dto.PredicateQueryDTO;
import com.muxin.gateway.admin.model.dto.PredicateUpdateDTO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.PredicateTypeVO;
import com.muxin.gateway.admin.model.vo.PredicateVO;
import com.muxin.gateway.admin.model.vo.RouteSimpleVO;
import com.muxin.gateway.admin.service.PredicateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 断言服务实现
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PredicateServiceImpl extends ServiceImpl<PredicateMapper, GwPredicate> implements PredicateService {
    
    private final RoutePredicateMapper routePredicateMapper;
    
    @Override
    public PageVO<PredicateVO> pageQuery(PredicateQueryDTO query) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(GW_PREDICATE)
                .where(GW_PREDICATE.DELETED.eq(false));
        
        if (StringUtils.hasText(query.getPredicateName())) {
            wrapper.and(GW_PREDICATE.PREDICATE_NAME.like("%" + query.getPredicateName() + "%"));
        }
        
        if (StringUtils.hasText(query.getPredicateType())) {
            wrapper.and(GW_PREDICATE.PREDICATE_TYPE.eq(query.getPredicateType()));
        }
        
        if (query.getEnabled() != null) {
            wrapper.and(GW_PREDICATE.ENABLED.eq(query.getEnabled()));
        }
        
        if (query.getIsSystem() != null) {
            wrapper.and(GW_PREDICATE.IS_SYSTEM.eq(query.getIsSystem()));
        }
        
        wrapper.orderBy(GW_PREDICATE.PREDICATE_TYPE.asc(), 
                       GW_PREDICATE.CREATE_TIME.desc());
        
        com.mybatisflex.core.paginate.Page<GwPredicate> page = page(
                new com.mybatisflex.core.paginate.Page<>(query.getPageNum(), query.getPageSize()), 
                wrapper);
        
        List<PredicateVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageVO.<PredicateVO>builder()
                .data(voList)
                .total(page.getTotalRow())
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize())
                .totalPages((int) page.getTotalPage())
                .build();
    }
    
    @Override
    public List<PredicateVO> getAvailablePredicates() {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(GW_PREDICATE)
                .where(GW_PREDICATE.ENABLED.eq(true))
                .and(GW_PREDICATE.DELETED.eq(false))
                .orderBy(GW_PREDICATE.PREDICATE_TYPE.asc(),
                        GW_PREDICATE.ID.asc());
        
        List<GwPredicate> predicates = list(wrapper);
        
        return predicates.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PredicateVO> getByType(String type) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(GW_PREDICATE)
                .where(GW_PREDICATE.PREDICATE_TYPE.eq(type))
                .and(GW_PREDICATE.ENABLED.eq(true))
                .and(GW_PREDICATE.DELETED.eq(false))
                .orderBy(GW_PREDICATE.CREATE_TIME.desc());
        
        List<GwPredicate> predicates = list(wrapper);
        
        return predicates.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public PredicateVO getPredicateDetail(Long id) {
        GwPredicate predicate = getById(id);
        if (predicate == null || predicate.getDeleted()) {
            throw new BusinessException("断言不存在");
        }
        return convertToVO(predicate);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPredicate(PredicateCreateDTO dto) {
        validatePredicateType(dto.getPredicateType());
        validatePredicateConfig(dto.getPredicateType(), dto.getConfig());
        
        GwPredicate predicate = new GwPredicate();
        predicate.setPredicateName(dto.getPredicateName());
        predicate.setPredicateType(dto.getPredicateType());
        predicate.setDescription(dto.getDescription());
        predicate.setConfig(dto.getConfig());
        predicate.setEnabled(dto.getEnabled());
        predicate.setIsSystem(false);
        predicate.setDeleted(false);
        
        save(predicate);
        
        return predicate.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePredicate(Long id, PredicateUpdateDTO dto) {
        GwPredicate predicate = getById(id);
        if (predicate == null || predicate.getDeleted()) {
            throw new BusinessException("断言不存在");
        }
        
        if (Boolean.TRUE.equals(predicate.getIsSystem())) {
            throw new BusinessException("系统内置断言不允许修改");
        }
        
        validatePredicateConfig(predicate.getPredicateType(), dto.getConfig());
        
        predicate.setPredicateName(dto.getPredicateName());
        predicate.setDescription(dto.getDescription());
        predicate.setConfig(dto.getConfig());
        predicate.setEnabled(dto.getEnabled());
        
        updateById(predicate);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePredicate(Long id) {
        GwPredicate predicate = getById(id);
        if (predicate == null || predicate.getDeleted()) {
            throw new BusinessException("断言不存在");
        }
        
        if (Boolean.TRUE.equals(predicate.getIsSystem())) {
            throw new BusinessException("系统内置断言不允许删除");
        }
        
        long usageCount = routePredicateMapper.countByPredicateId(id);
        if (usageCount > 0) {
            throw new BusinessException("断言正在被" + usageCount + "个路由使用，无法删除");
        }
        
        removeById(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        for (Long id : ids) {
            GwPredicate predicate = getById(id);
            if (predicate != null && !predicate.getDeleted()) {
                if (Boolean.TRUE.equals(predicate.getIsSystem())) {
                    throw new BusinessException("包含系统内置断言，无法批量删除");
                }
                
                long usageCount = routePredicateMapper.countByPredicateId(id);
                if (usageCount > 0) {
                    throw new BusinessException("断言[" + predicate.getPredicateName() + "]正在被使用，无法删除");
                }
            }
        }
        
        removeByIds(ids);
    }
    
    @Override
    public List<PredicateTypeVO> getPredicateTypes() {
        return Arrays.stream(PredicateType.values())
                .map(type -> PredicateTypeVO.builder()
                        .type(type.getType())
                        .name(type.getName())
                        .description(type.getDescription())
                        .configFields(type.getConfigFields())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enablePredicate(Long id) {
        GwPredicate predicate = getById(id);
        if (predicate == null || predicate.getDeleted()) {
            throw new BusinessException("断言不存在");
        }
        predicate.setEnabled(true);
        updateById(predicate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disablePredicate(Long id) {
        GwPredicate predicate = getById(id);
        if (predicate == null || predicate.getDeleted()) {
            throw new BusinessException("断言不存在");
        }
        predicate.setEnabled(false);
        updateById(predicate);
    }

    @Override
    public List<RouteSimpleVO> getUsedRoutes(Long id) {
        GwPredicate predicate = getById(id);
        if (predicate == null || predicate.getDeleted()) {
            throw new BusinessException("断言不存在");
        }
        
        List<Map<String, Object>> routes = routePredicateMapper.findRoutesByPredicateId(id);
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
    
    private void validatePredicateType(String type) {
        boolean valid = Arrays.stream(PredicateType.values())
                .anyMatch(t -> t.getType().equals(type));
        
        if (!valid) {
            throw new BusinessException("不支持的断言类型: " + type);
        }
    }
    
    private void validatePredicateConfig(String type, Map<String, Object> config) {
        if (config == null || config.isEmpty()) {
            throw new BusinessException("断言配置不能为空");
        }
    }
    
    private PredicateVO convertToVO(GwPredicate predicate) {
        PredicateVO vo = new PredicateVO();
        vo.setId(predicate.getId());
        vo.setPredicateName(predicate.getPredicateName());
        vo.setPredicateType(predicate.getPredicateType());
        vo.setDescription(predicate.getDescription());
        vo.setConfig(predicate.getConfig());
        vo.setIsSystem(predicate.getIsSystem());
        vo.setEnabled(predicate.getEnabled());
        vo.setCreateTime(predicate.getCreateTime());
        vo.setUpdateTime(predicate.getUpdateTime());
        
        Arrays.stream(PredicateType.values())
                .filter(t -> t.getType().equals(predicate.getPredicateType()))
                .findFirst()
                .ifPresent(t -> vo.setPredicateTypeDesc(t.getName()));
        
        long usageCount = routePredicateMapper.countByPredicateId(predicate.getId());
        vo.setUsageCount((int) usageCount);
        
        return vo;
    }
} 