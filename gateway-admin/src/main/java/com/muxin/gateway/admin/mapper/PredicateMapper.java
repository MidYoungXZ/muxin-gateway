package com.muxin.gateway.admin.mapper;

import com.mybatisflex.core.BaseMapper;
import com.muxin.gateway.admin.entity.GwPredicate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 断言Mapper
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface PredicateMapper extends BaseMapper<GwPredicate> {
    
    @Select("SELECT * FROM gw_predicate WHERE predicate_name = #{predicateName} AND deleted = 0 LIMIT 1")
    GwPredicate findByPredicateName(@Param("predicateName") String predicateName);
} 