package com.muxin.gateway.admin.mapper;

import com.mybatisflex.core.BaseMapper;
import com.muxin.gateway.admin.entity.GwFilter;
import org.apache.ibatis.annotations.Mapper;

/**
 * 过滤器Mapper
 *
 * @author muxin
 */
@Mapper
public interface FilterMapper extends BaseMapper<GwFilter> {
}