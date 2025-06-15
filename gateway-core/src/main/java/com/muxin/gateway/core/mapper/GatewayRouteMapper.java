package com.muxin.gateway.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muxin.gateway.core.entity.GatewayRoute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 路由配置Mapper接口
 * 
 * @author muxin
 */
@Mapper
public interface GatewayRouteMapper extends BaseMapper<GatewayRoute> {
    
    /**
     * 获取最大版本号
     * 
     * @return 最大版本号
     */
    @Select("SELECT MAX(version) FROM gateway_route")
    Integer getMaxVersion();
} 