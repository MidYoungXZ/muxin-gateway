package com.muxin.gateway.admin.mapper;

import com.mybatisflex.core.BaseMapper;
import com.muxin.gateway.admin.entity.GwLoadBalance;
import org.apache.ibatis.annotations.Mapper;

/**
 * 负载均衡Mapper
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface LoadBalanceMapper extends BaseMapper<GwLoadBalance> {
}