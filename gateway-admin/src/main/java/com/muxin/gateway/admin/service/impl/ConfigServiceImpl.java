package com.muxin.gateway.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.muxin.gateway.admin.entity.SysConfig;
import com.muxin.gateway.admin.exception.BusinessException;
import com.muxin.gateway.admin.mapper.ConfigMapper;
import com.muxin.gateway.admin.model.dto.ConfigCreateDTO;
import com.muxin.gateway.admin.model.dto.ConfigQueryDTO;
import com.muxin.gateway.admin.model.dto.ConfigUpdateDTO;
import com.muxin.gateway.admin.model.vo.ConfigVO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.muxin.gateway.admin.entity.table.Tables.SYS_CONFIG;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, SysConfig> implements ConfigService {
    
    private final ConfigMapper configMapper;
    
    @Override
    public PageVO<ConfigVO> pageQuery(ConfigQueryDTO query) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_CONFIG);
        
        if (StringUtils.hasText(query.getConfigKey())) {
            wrapper.and(SYS_CONFIG.CONFIG_KEY.like("%" + query.getConfigKey() + "%"));
        }
        
        if (StringUtils.hasText(query.getConfigName())) {
            wrapper.and(SYS_CONFIG.CONFIG_NAME.like("%" + query.getConfigName() + "%"));
        }
        
        if (query.getStatus() != null) {
            wrapper.and(SYS_CONFIG.STATUS.eq(query.getStatus()));
        }
        
        wrapper.orderBy(SYS_CONFIG.CREATE_TIME.desc());
        
        com.mybatisflex.core.paginate.Page<SysConfig> page = page(
                new com.mybatisflex.core.paginate.Page<>(query.getPageNum(), query.getPageSize()),
                wrapper);
        
        List<ConfigVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageVO.<ConfigVO>builder()
                .data(voList)
                .total(page.getTotalRow())
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize())
                .totalPages((int) page.getTotalPage())
                .build();
    }
    
    @Override
    public ConfigVO getByKey(String configKey) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_CONFIG)
                .where(SYS_CONFIG.CONFIG_KEY.eq(configKey))
                .and(SYS_CONFIG.STATUS.eq(1));
        
        SysConfig config = getOne(wrapper);
        return config != null ? convertToVO(config) : null;
    }
    
    @Override
    public ConfigVO getDetail(Long id) {
        SysConfig config = getById(id);
        if (config == null) {
            throw new BusinessException("配置不存在");
        }
        return convertToVO(config);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ConfigCreateDTO dto) {
        if (!checkKeyAvailable(dto.getConfigKey(), null)) {
            throw new BusinessException("配置键已存在");
        }
        
        SysConfig config = new SysConfig();
        BeanUtils.copyProperties(dto, config);
        config.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        config.setCreateBy(StpUtil.getLoginIdAsString());
        
        save(config);
        log.info("创建配置成功：{}", config.getConfigKey());
        return config.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ConfigUpdateDTO dto) {
        SysConfig config = getById(id);
        if (config == null) {
            throw new BusinessException("配置不存在");
        }
        
        if (dto.getConfigValue() != null) {
            config.setConfigValue(dto.getConfigValue());
        }
        if (dto.getConfigName() != null) {
            config.setConfigName(dto.getConfigName());
        }
        if (dto.getDescription() != null) {
            config.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            config.setStatus(dto.getStatus());
        }
        
        config.setUpdateTime(LocalDateTime.now());
        config.setUpdateBy(StpUtil.getLoginIdAsString());
        
        updateById(config);
        log.info("更新配置成功：{}", config.getConfigKey());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysConfig config = getById(id);
        if (config == null) {
            throw new BusinessException("配置不存在");
        }
        
        removeById(id);
        
        log.info("删除配置成功：{}", config.getConfigKey());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        for (Long id : ids) {
            delete(id);
        }
    }
    
    @Override
    public void enable(Long id) {
        updateStatus(id, 1);
    }
    
    @Override
    public void disable(Long id) {
        updateStatus(id, 0);
    }
    
    @Override
    public boolean checkKeyAvailable(String configKey, Long excludeId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_CONFIG)
                .where(SYS_CONFIG.CONFIG_KEY.eq(configKey));
        
        if (excludeId != null) {
            wrapper.and(SYS_CONFIG.ID.ne(excludeId));
        }
        
        return count(wrapper) == 0;
    }
    
    @Override
    public void refreshCache() {
        log.info("刷新配置缓存");
    }
    
    @Override
    public List<ConfigVO> getAllConfigs() {
        QueryWrapper wrapper = QueryWrapper.create()
                .select()
                .from(SYS_CONFIG)
                .where(SYS_CONFIG.STATUS.eq(1));
        
        return list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    private void updateStatus(Long id, Integer status) {
        SysConfig config = getById(id);
        if (config == null) {
            throw new BusinessException("配置不存在");
        }
        
        config.setStatus(status);
        config.setUpdateTime(LocalDateTime.now());
        config.setUpdateBy(StpUtil.getLoginIdAsString());
        updateById(config);
        
        log.info("更新配置状态成功：{}，状态：{}", config.getConfigKey(), status);
    }
    
    private ConfigVO convertToVO(SysConfig config) {
        ConfigVO vo = new ConfigVO();
        BeanUtils.copyProperties(config, vo);
        vo.setStatusText(config.getStatus() == 1 ? "启用" : "禁用");
        return vo;
    }
}