package com.muxin.gateway.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muxin.gateway.core.entity.GatewayAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作审计日志Mapper接口
 * 
 * @author muxin
 */
@Mapper
public interface GatewayAuditLogMapper extends BaseMapper<GatewayAuditLog> {
} 