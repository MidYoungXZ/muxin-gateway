package com.muxin.gateway.admin.mapper;

import com.mybatisflex.core.BaseMapper;
import com.muxin.gateway.admin.entity.GwRoutePredicate;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 路由断言关联Mapper
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface RoutePredicateMapper extends BaseMapper<GwRoutePredicate> {
    
    /**
     * 根据断言ID查询使用次数
     */
    @Select("SELECT COUNT(1) FROM gw_route_predicate WHERE predicate_id = #{predicateId}")
    long countByPredicateId(@Param("predicateId") Long predicateId);
    
    /**
     * 根据路由ID删除关联
     */
    @Delete("DELETE FROM gw_route_predicate WHERE route_id = #{routeId}")
    void deleteByRouteId(@Param("routeId") Long routeId);

    /**
     * 根据断言ID查询使用的路由列表
     */
    @Select("SELECT r.id, r.route_id as routeId, r.route_name as routeName, r.enabled " +
            "FROM gw_route r " +
            "INNER JOIN gw_route_predicate rp ON r.id = rp.route_id " +
            "WHERE rp.predicate_id = #{predicateId} AND r.deleted = 0 " +
            "ORDER BY r.create_time DESC")
    List<Map<String, Object>> findRoutesByPredicateId(@Param("predicateId") Long predicateId);
    
    @Select("SELECT p.id, p.predicate_name as predicateName, p.predicate_type as predicateType, p.args, rp.sort_order as sortOrder " +
            "FROM gw_predicate p " +
            "INNER JOIN gw_route_predicate rp ON p.id = rp.predicate_id " +
            "WHERE rp.route_id = #{routeId} AND p.deleted = 0 " +
            "ORDER BY rp.sort_order ASC")
    List<Map<String, Object>> findPredicatesByRouteId(@Param("routeId") Long routeId);
} 