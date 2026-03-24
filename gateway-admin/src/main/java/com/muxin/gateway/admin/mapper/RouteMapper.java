package com.muxin.gateway.admin.mapper;

import com.mybatisflex.core.BaseMapper;
import com.muxin.gateway.admin.entity.GwRoute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RouteMapper extends BaseMapper<GwRoute> {
    
    @Select("SELECT DISTINCT service_name FROM gw_service_node WHERE deleted = 0 ORDER BY service_name")
    List<String> findAllServiceNames();
} 