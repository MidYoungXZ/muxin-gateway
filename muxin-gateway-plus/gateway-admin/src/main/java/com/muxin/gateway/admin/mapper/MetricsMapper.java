package com.muxin.gateway.admin.mapper;

import com.mybatisflex.core.BaseMapper;
import com.muxin.gateway.admin.entity.GwMetrics;
import org.apache.ibatis.annotations.Mapper;

/**
 * 监控指标Mapper
 *
 * @author muxin
 */
@Mapper
public interface MetricsMapper extends BaseMapper<GwMetrics> {
} 