package com.muxin.gateway.admin.mapper;

import com.mybatisflex.core.BaseMapper;
import com.muxin.gateway.admin.entity.GwRoutePredicate;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 路由断言关联Mapper
 *
 * @author muxin
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
} 