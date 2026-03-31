package com.muxin.gateway.admin.service;

import com.muxin.gateway.admin.entity.GwPlugin;

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
    
    List<GwPlugin> getPluginsByType(String type);
    
    GwPlugin getPluginById(Long id);
    
    GwPlugin getPluginByName(String name);
    
    Long createPlugin(GwPlugin plugin);
    
    void updatePlugin(GwPlugin plugin);
    
    void deletePlugin(Long id);
}