package com.muxin.gateway.admin.service;

import com.muxin.gateway.admin.model.dto.ServiceCreateDTO;
import com.muxin.gateway.admin.model.dto.ServiceNodeCreateDTO;
import com.muxin.gateway.admin.model.dto.ServiceNodeUpdateDTO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.ServiceNodeVO;
import com.muxin.gateway.admin.model.vo.ServiceStatsVO;

import java.util.List;

/**
 * 服务节点服务接口
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ServiceNodeService {
    
    /**
     * 获取服务统计列表
     */
    List<ServiceStatsVO> getServiceStats(String serviceName);
    
    /**
     * 获取服务下的节点列表
     */
    PageVO<ServiceNodeVO> getNodesByService(String serviceName, int pageNum, int pageSize);
    
    /**
     * 获取节点详情
     */
    ServiceNodeVO getDetail(Long id);
    
    /**
     * 创建服务（同时创建第一个节点）
     */
    Long createService(ServiceCreateDTO dto);
    
    /**
     * 创建节点
     */
    Long create(ServiceNodeCreateDTO dto);
    
    /**
     * 更新节点
     */
    void update(Long id, ServiceNodeUpdateDTO dto);
    
    /**
     * 删除节点
     */
    void delete(Long id);
    
    /**
     * 启用节点
     */
    void enable(Long id);
    
    /**
     * 禁用节点
     */
    void disable(Long id);
    
    /**
     * 设为维护中
     */
    void maintenance(Long id);
    
    /**
     * 获取所有服务名称
     */
    List<String> getServiceNames();
}