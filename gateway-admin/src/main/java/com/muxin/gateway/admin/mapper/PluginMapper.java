package com.muxin.gateway.admin.mapper;

import com.mybatisflex.core.BaseMapper;
import com.muxin.gateway.admin.entity.GwPlugin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 插件Mapper
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface PluginMapper extends BaseMapper<GwPlugin> {
    
    @Select("SELECT * FROM gw_plugin WHERE plugin_name = #{pluginName} AND deleted = 0 LIMIT 1")
    GwPlugin findByPluginName(@Param("pluginName") String pluginName);
    
    @Select("SELECT * FROM gw_plugin WHERE plugin_type = #{pluginType} AND deleted = 0 AND enabled = 1 ORDER BY default_priority DESC")
    List<GwPlugin> findByType(@Param("pluginType") String pluginType);
    
    @Select("SELECT * FROM gw_plugin WHERE deleted = 0 AND enabled = 1 ORDER BY plugin_type, default_priority DESC")
    List<GwPlugin> findAllEnabled();
}