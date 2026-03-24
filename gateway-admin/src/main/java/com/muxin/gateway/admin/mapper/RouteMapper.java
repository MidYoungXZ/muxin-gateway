package com.muxin.gateway.admin.mapper;

import com.mybatisflex.core.BaseMapper;
import com.muxin.gateway.admin.entity.GwRoute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface RouteMapper extends BaseMapper<GwRoute> {
    
    @Select("SELECT DISTINCT service_name FROM gw_service_node WHERE deleted = 0 ORDER BY service_name")
    List<String> findAllServiceNames();
    
    @Select("SELECT id, route_id as routeId, route_name as routeName, enabled FROM gw_route WHERE uri LIKE CONCAT('lb://', #{serviceName}) AND deleted = 0 ORDER BY create_time DESC")
    List<Map<String, Object>> findRoutesByServiceName(@Param("serviceName") String serviceName);
} 