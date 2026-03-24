package com.muxin.gateway.admin.service;

import com.mybatisflex.core.service.IService;
import com.muxin.gateway.admin.entity.GwPredicate;
import com.muxin.gateway.admin.model.dto.PredicateCreateDTO;
import com.muxin.gateway.admin.model.dto.PredicateQueryDTO;
import com.muxin.gateway.admin.model.dto.PredicateUpdateDTO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.PredicateTypeVO;
import com.muxin.gateway.admin.model.vo.PredicateVO;
import com.muxin.gateway.admin.model.vo.RouteSimpleVO;

import java.util.List;

/**
 * 断言服务接口
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PredicateService extends IService<GwPredicate> {
    
    /**
     * 分页查询断言列表
     */
    PageVO<PredicateVO> pageQuery(PredicateQueryDTO query);
    
    /**
     * 获取所有可用断言
     */
    List<PredicateVO> getAvailablePredicates();
    
    /**
     * 根据类型获取断言列表
     */
    List<PredicateVO> getByType(String type);
    
    /**
     * 获取断言详情
     */
    PredicateVO getPredicateDetail(Long id);
    
    /**
     * 创建断言
     */
    Long createPredicate(PredicateCreateDTO dto);
    
    /**
     * 更新断言
     */
    void updatePredicate(Long id, PredicateUpdateDTO dto);
    
    /**
     * 删除断言
     */
    void deletePredicate(Long id);
    
    /**
     * 批量删除断言
     */
    void batchDelete(List<Long> ids);
    
    /**
     * 获取断言类型列表
     */
    List<PredicateTypeVO> getPredicateTypes();

    /**
     * 启用断言
     */
    void enablePredicate(Long id);

    /**
     * 禁用断言
     */
    void disablePredicate(Long id);

    /**
     * 获取断言使用的路由列表
     */
    List<RouteSimpleVO> getUsedRoutes(Long id);
} 