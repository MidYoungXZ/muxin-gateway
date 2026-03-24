package com.muxin.gateway.admin.service;

import com.muxin.gateway.admin.model.dto.LoadBalanceCreateDTO;
import com.muxin.gateway.admin.model.dto.LoadBalanceQueryDTO;
import com.muxin.gateway.admin.model.dto.LoadBalanceUpdateDTO;
import com.muxin.gateway.admin.model.vo.LoadBalanceStrategyVO;
import com.muxin.gateway.admin.model.vo.LoadBalanceVO;
import com.muxin.gateway.admin.model.vo.PageVO;

import java.util.List;

/**
 * 负载均衡服务接口
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public interface LoadBalanceService {
    
    /**
     * 分页查询负载均衡列表
     */
    PageVO<LoadBalanceVO> pageQuery(LoadBalanceQueryDTO query);
    
    /**
     * 根据路由ID获取负载均衡配置
     */
    LoadBalanceVO getByRouteId(Long routeId);
    
    /**
     * 获取负载均衡详情
     */
    LoadBalanceVO getDetail(Long id);
    
    /**
     * 创建负载均衡配置
     */
    Long create(LoadBalanceCreateDTO dto);
    
    /**
     * 更新负载均衡配置
     */
    void update(Long id, LoadBalanceUpdateDTO dto);
    
    /**
     * 删除负载均衡配置
     */
    void delete(Long id);
    
    /**
     * 启用
     */
    void enable(Long id);
    
    /**
     * 禁用
     */
    void disable(Long id);
    
    /**
     * 获取支持的策略列表
     */
    List<LoadBalanceStrategyVO> getStrategies();
}