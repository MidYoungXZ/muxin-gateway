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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PredicateServiceImpl extends ServiceImpl<PredicateMapper, GwPredicate> implements PredicateService {
    
    private final RoutePredicateMapper routePredicateMapper;
    
    @Override
    public PageVO<PredicateVO> pageQuery(PredicateQueryDTO query) {
        // 构建查询条件
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(GW_PREDICATE)
                .where(GW_PREDICATE.DELETED.eq(false));
        
        // 动态条件
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
        
        // 排序
        wrapper.orderBy(GW_PREDICATE.PREDICATE_TYPE.asc(), 
                       GW_PREDICATE.CREATE_TIME.desc());
        
        // 分页查询
        com.mybatisflex.core.paginate.Page<GwPredicate> page = page(
                new com.mybatisflex.core.paginate.Page<>(query.getPageNum(), query.getPageSize()), 
                wrapper);
        
        // 转换为VO
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
        // 1. 验证断言类型
        validatePredicateType(dto.getPredicateType());
        
        // 2. 验证配置格式
        validatePredicateConfig(dto.getPredicateType(), dto.getConfig());
        
        // 3. 创建断言
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
        // 1. 获取原断言
        GwPredicate predicate = getById(id);
        if (predicate == null || predicate.getDeleted()) {
            throw new BusinessException("断言不存在");
        }
        
        // 2. 系统内置断言不允许修改
        if (Boolean.TRUE.equals(predicate.getIsSystem())) {
            throw new BusinessException("系统内置断言不允许修改");
        }
        
        // 3. 验证配置格式
        validatePredicateConfig(predicate.getPredicateType(), dto.getConfig());
        
        // 4. 更新断言
        predicate.setPredicateName(dto.getPredicateName());
        predicate.setDescription(dto.getDescription());
        predicate.setConfig(dto.getConfig());
        predicate.setEnabled(dto.getEnabled());
        
        updateById(predicate);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePredicate(Long id) {
        // 1. 获取断言
        GwPredicate predicate = getById(id);
        if (predicate == null || predicate.getDeleted()) {
            throw new BusinessException("断言不存在");
        }
        
        // 2. 系统内置断言不允许删除
        if (Boolean.TRUE.equals(predicate.getIsSystem())) {
            throw new BusinessException("系统内置断言不允许删除");
        }
        
        // 3. 检查是否被使用
        long usageCount = routePredicateMapper.countByPredicateId(id);
        if (usageCount > 0) {
            throw new BusinessException("断言正在被" + usageCount + "个路由使用，无法删除");
        }
        
        // 4. 逻辑删除
        removeById(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        // 批量检查
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
        
        // 批量删除
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
    
    /**
     * 验证断言类型
     */
    private void validatePredicateType(String type) {
        boolean valid = Arrays.stream(PredicateType.values())
                .anyMatch(t -> t.getType().equals(type));
        
        if (!valid) {
            throw new BusinessException("不支持的断言类型: " + type);
        }
    }
    
    /**
     * 验证断言配置
     */
    private void validatePredicateConfig(String type, Map<String, Object> config) {
        if (config == null || config.isEmpty()) {
            throw new BusinessException("断言配置不能为空");
        }
        
        // TODO: 根据不同的断言类型验证配置格式
    }
    
    /**
     * 转换为VO
     */
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
        
        // 设置类型描述
        Arrays.stream(PredicateType.values())
                .filter(t -> t.getType().equals(predicate.getPredicateType()))
                .findFirst()
                .ifPresent(t -> vo.setPredicateTypeDesc(t.getName()));
        
        // TODO: 设置使用次数
        vo.setUsageCount(0);
        
        return vo;
    }
} 