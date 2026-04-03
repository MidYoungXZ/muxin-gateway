package com.muxin.gateway.admin.service;

import com.muxin.gateway.admin.entity.GwPlugin;
import com.muxin.gateway.admin.model.vo.PageVO;

import java.util.List;

/**
 * 插件服务接口
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PluginService {
    
    List<GwPlugin> getAllPlugins();
    
    PageVO<GwPlugin> getPluginsByType(String type, String pluginName, int pageNum, int pageSize);
    
    GwPlugin getPluginById(Long id);
    
    GwPlugin getPluginByName(String name);
    
    Long createPlugin(GwPlugin plugin);
    
    void updatePlugin(GwPlugin plugin);
    
    void deletePlugin(Long id);
    
    void enablePlugin(Long id);
    
    void disablePlugin(Long id);
}