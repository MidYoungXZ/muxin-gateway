package com.muxin.gateway.admin.mapper;

import com.mybatisflex.core.BaseMapper;
import com.muxin.gateway.admin.entity.GwServiceNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 服务节点Mapper
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface ServiceNodeMapper extends BaseMapper<GwServiceNode> {
    
    /**
     * 按服务名称聚合统计
     */
    @Select("SELECT service_name as serviceName, " +
            "COUNT(*) as totalNodes, " +
            "SUM(CASE WHEN last_check_result = 1 THEN 1 ELSE 0 END) as healthyNodes, " +
            "SUM(CASE WHEN last_check_result = 0 THEN 1 ELSE 0 END) as unhealthyNodes, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as enabledNodes, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as disabledNodes, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as maintenanceNodes " +
            "FROM gw_service_node " +
            "WHERE deleted = 0 " +
            "GROUP BY service_name " +
            "ORDER BY service_name")
    List<Map<String, Object>> selectServiceStats();
    
    /**
     * 获取所有服务名称
     */
    @Select("SELECT DISTINCT service_name FROM gw_service_node WHERE deleted = 0 ORDER BY service_name")
    List<String> selectServiceNames();
}