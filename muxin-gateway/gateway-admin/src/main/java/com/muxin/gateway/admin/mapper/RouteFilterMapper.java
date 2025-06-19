package com.muxin.gateway.admin.mapper;

import com.mybatisflex.core.BaseMapper;
import com.muxin.gateway.admin.entity.GwRouteFilter;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 路由过滤器关联Mapper
 *
 * @author muxin
 */
@Mapper
public interface RouteFilterMapper extends BaseMapper<GwRouteFilter> {
    
    /**
     * 根据路由ID删除关联
     */
    @Delete("DELETE FROM gw_route_filter WHERE route_id = #{routeId}")
    void deleteByRouteId(@Param("routeId") Long routeId);
} 