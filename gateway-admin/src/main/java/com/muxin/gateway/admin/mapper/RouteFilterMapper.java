package com.muxin.gateway.admin.mapper;

import com.mybatisflex.core.BaseMapper;
import com.muxin.gateway.admin.entity.GwRouteFilter;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 路由过滤器关联Mapper
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface RouteFilterMapper extends BaseMapper<GwRouteFilter> {
    
    /**
     * 根据路由ID删除关联
     */
    @Delete("DELETE FROM gw_route_filter WHERE route_id = #{routeId}")
    void deleteByRouteId(@Param("routeId") Long routeId);

    /**
     * 根据过滤器ID查询使用的路由列表
     */
    @Select("SELECT r.id, r.route_id as routeId, r.route_name as routeName, r.enabled " +
            "FROM gw_route r " +
            "INNER JOIN gw_route_filter rf ON r.id = rf.route_id " +
            "WHERE rf.filter_id = #{filterId} AND r.deleted = 0 " +
            "ORDER BY r.create_time DESC")
    List<Map<String, Object>> findRoutesByFilterId(@Param("filterId") Long filterId);
    
    @Select("SELECT f.id, f.filter_name as filterName, f.filter_type as filterType, f.config, rf.sort_order as sortOrder " +
            "FROM gw_filter f " +
            "INNER JOIN gw_route_filter rf ON f.id = rf.filter_id,om " +
            "WHERE rf.route_id = #{routeId} AND f.deleted = 0 " +
            "ORDER BY rf.sort_order ASC")
    List<Map<String, Object>> findFiltersByRouteId(@Param("routeId") Long routeId);
} 