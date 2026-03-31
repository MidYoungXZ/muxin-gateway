package com.muxin.gateway.admin.mapper;

import com.mybatisflex.core.BaseMapper;
import com.muxin.gateway.admin.entity.GwFilter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 过滤器Mapper
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface FilterMapper extends BaseMapper<GwFilter> {
    
    @Select("SELECT * FROM gw_filter WHERE filter_name = #{filterName} AND deleted = 0 LIMIT 1")
    GwFilter findByFilterName(@Param("filterName") String filterName);
}