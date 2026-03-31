package com.muxin.gateway.admin.mapper;

import com.mybatisflex.core.BaseMapper;
import com.muxin.gateway.admin.entity.GwRoutePlugin;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 路由插件关联Mapper
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface RoutePluginMapper extends BaseMapper<GwRoutePlugin> {
    
    @Delete("DELETE FROM gw_route_plugin WHERE route_id = #{routeId}")
    int deleteByRouteId(@Param("routeId") Long routeId);
    
    @Select("SELECT rp.id as route_plugin_id, rp.config, rp.priority_override, rp.enabled, rp.sort_order, " +
            "p.id as plugin_id, p.plugin_name, p.plugin_type, p.default_priority, p.phase, p.schema " +
            "FROM gw_route_plugin rp " +
            "JOIN gw_plugin p ON rp.plugin_id = p.id " +
            "WHERE rp.route_id = #{routeId} AND p.deleted = 0 " +
            "ORDER BY COALESCE(rp.priority_override, p.default_priority) DESC")
    List<Map<String, Object>> findPluginsByRouteId(@Param("routeId") Long routeId);
}