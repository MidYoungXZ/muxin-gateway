package com.muxin.gateway.admin.service;

import com.muxin.gateway.admin.model.dto.FilterCreateDTO;
import com.muxin.gateway.admin.model.dto.FilterQueryDTO;
import com.muxin.gateway.admin.model.dto.FilterUpdateDTO;
import com.muxin.gateway.admin.model.vo.FilterTypeVO;
import com.muxin.gateway.admin.model.vo.FilterVO;
import com.muxin.gateway.admin.model.vo.PageVO;

import java.util.List;

/**
 * 过滤器服务接口
 *
 * @author muxin
 */
public interface FilterService {
    
    /**
     * 分页查询过滤器列表
     */
    PageVO<FilterVO> pageQuery(FilterQueryDTO query);
    
    /**
     * 获取所有可用过滤器
     */
    List<FilterVO> getAvailableFilters();
    
    /**
     * 根据类型获取过滤器列表
     */
    List<FilterVO> getByType(String type);
    
    /**
     * 获取过滤器详情
     */
    FilterVO getFilterDetail(Long id);
    
    /**
     * 创建过滤器
     */
    Long createFilter(FilterCreateDTO dto);
    
    /**
     * 更新过滤器
     */
    void updateFilter(Long id, FilterUpdateDTO dto);
    
    /**
     * 删除过滤器
     */
    void deleteFilter(Long id);
    
    /**
     * 批量删除过滤器
     */
    void batchDelete(List<Long> ids);
    
    /**
     * 启用过滤器
     */
    void enableFilter(Long id);
    
    /**
     * 禁用过滤器
     */
    void disableFilter(Long id);
    
    /**
     * 获取过滤器类型列表
     */
    List<FilterTypeVO> getFilterTypes();
} 