package com.muxin.gateway.admin.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.muxin.gateway.admin.entity.GwPlugin;
import static com.muxin.gateway.admin.entity.table.GwPluginTableDef.GW_PLUGIN;
import com.muxin.gateway.admin.exception.BusinessException;
import com.muxin.gateway.admin.mapper.PluginMapper;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.service.PluginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PluginServiceImpl extends ServiceImpl<PluginMapper, GwPlugin> implements PluginService {
    
    private final PluginMapper pluginMapper;
    
    @Override
    public List<GwPlugin> getAllPlugins() {
        return pluginMapper.selectListByQuery(
            QueryWrapper.create()
                .where(GW_PLUGIN.DELETED.eq(false))
                .and(GW_PLUGIN.ENABLED.eq(true))
                .orderBy(GW_PLUGIN.PLUGIN_TYPE.asc(), GW_PLUGIN.DEFAULT_PRIORITY.desc())
        );
    }
    
    @Override
    public PageVO<GwPlugin> getPluginsByType(String type, String pluginName, int pageNum, int pageSize) {
        QueryWrapper queryWrapper = QueryWrapper.create()
            .where(GW_PLUGIN.DELETED.eq(false));
        
        if (StringUtils.hasText(type)) {
            queryWrapper.and(GW_PLUGIN.PLUGIN_TYPE.eq(type));
        }
        
        if (StringUtils.hasText(pluginName)) {
            queryWrapper.and(GW_PLUGIN.PLUGIN_NAME.like(pluginName));
        }
        
        queryWrapper.orderBy(GW_PLUGIN.DEFAULT_PRIORITY.desc());
        
        long total = pluginMapper.selectCountByQuery(queryWrapper);
        
        int offset = (pageNum - 1) * pageSize;
        queryWrapper.limit(offset, pageSize);
        
        List<GwPlugin> data = pluginMapper.selectListByQuery(queryWrapper);
        
        int totalPages = (int) Math.ceil((double) total / pageSize);
        
        return PageVO.<GwPlugin>builder()
            .data(data)
            .total(total)
            .pageNum(pageNum)
            .pageSize(pageSize)
            .totalPages(totalPages)
            .build();
    }
    
    @Override
    public GwPlugin getPluginById(Long id) {
        GwPlugin plugin = getById(id);
        if (plugin == null || plugin.getDeleted()) {
            throw new BusinessException("插件不存在");
        }
        return plugin;
    }
    
    @Override
    public GwPlugin getPluginByName(String name) {
        return pluginMapper.selectOneByQuery(
            QueryWrapper.create()
                .where(GW_PLUGIN.PLUGIN_NAME.eq(name))
                .and(GW_PLUGIN.DELETED.eq(false))
                .limit(1)
        );
    }
    
    @Override
    public Long createPlugin(GwPlugin plugin) {
        GwPlugin existing = getPluginByName(plugin.getPluginName());
        if (existing != null) {
            throw new BusinessException("插件名称已存在: " + plugin.getPluginName());
        }
        
        plugin.setDeleted(false);
        plugin.setCreateTime(LocalDateTime.now());
        plugin.setUpdateTime(LocalDateTime.now());
        
        if (plugin.getEnabled() == null) {
            plugin.setEnabled(true);
        }
        if (plugin.getIsSystem() == null) {
            plugin.setIsSystem(false);
        }
        if (plugin.getDefaultPriority() == null) {
            plugin.setDefaultPriority(5000);
        }
        
        save(plugin);
        log.info("[PluginService] 插件创建成功: {}", plugin.getPluginName());
        
        return plugin.getId();
    }
    
    @Override
    public void updatePlugin(GwPlugin plugin) {
        GwPlugin existing = getById(plugin.getId());
        if (existing == null || existing.getDeleted()) {
            throw new BusinessException("插件不存在");
        }
        
        if (existing.getIsSystem()) {
            throw new BusinessException("系统内置插件不允许修改");
        }
        
        plugin.setUpdateTime(LocalDateTime.now());
        updateById(plugin);
        
        log.info("[PluginService] 插件更新成功: {}", plugin.getPluginName());
    }
    
    @Override
    public void deletePlugin(Long id) {
        GwPlugin plugin = getById(id);
        if (plugin == null || plugin.getDeleted()) {
            throw new BusinessException("插件不存在");
        }
        
        if (plugin.getIsSystem()) {
            throw new BusinessException("系统内置插件不允许删除");
        }
        
        plugin.setDeleted(true);
        plugin.setUpdateTime(LocalDateTime.now());
        updateById(plugin);
        
        log.info("[PluginService] 插件删除成功: {}", plugin.getPluginName());
    }
}