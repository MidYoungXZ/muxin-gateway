package com.muxin.gateway.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.muxin.gateway.admin.model.Result;
import com.muxin.gateway.admin.model.dto.PredicateCreateDTO;
import com.muxin.gateway.admin.model.dto.PredicateQueryDTO;
import com.muxin.gateway.admin.model.dto.PredicateUpdateDTO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.PredicateTypeVO;
import com.muxin.gateway.admin.model.vo.PredicateVO;
import com.muxin.gateway.admin.service.PredicateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 断言管理控制器
 *
 * @author muxin
 */
@Slf4j
@RestController
@RequestMapping("/api/predicates")
@RequiredArgsConstructor
public class PredicateController {
    
    private final PredicateService predicateService;
    
    /**
     * 分页查询断言列表
     */
    @GetMapping
    @SaCheckPermission("predicate:list")
    public Result<PageVO<PredicateVO>> listPredicates(PredicateQueryDTO query) {
        return Result.success(predicateService.pageQuery(query));
    }
    
    /**
     * 获取所有可用断言（用于路由配置时选择）
     */
    @GetMapping("/available")
    @SaCheckPermission("predicate:list")
    public Result<List<PredicateVO>> getAvailablePredicates() {
        return Result.success(predicateService.getAvailablePredicates());
    }
    
    /**
     * 根据类型获取断言列表
     */
    @GetMapping("/type/{type}")
    @SaCheckPermission("predicate:list")
    public Result<List<PredicateVO>> getPredicatesByType(@PathVariable String type) {
        return Result.success(predicateService.getByType(type));
    }
    
    /**
     * 获取断言详情
     */
    @GetMapping("/{id}")
    @SaCheckPermission("predicate:view")
    public Result<PredicateVO> getPredicate(@PathVariable Long id) {
        return Result.success(predicateService.getPredicateDetail(id));
    }
    
    /**
     * 创建断言
     */
    @PostMapping
    @SaCheckPermission("predicate:create")
    public Result<Long> createPredicate(@RequestBody @Valid PredicateCreateDTO dto) {
        return Result.success(predicateService.createPredicate(dto));
    }
    
    /**
     * 更新断言
     */
    @PutMapping("/{id}")
    @SaCheckPermission("predicate:update")
    public Result<Void> updatePredicate(@PathVariable Long id, 
                                       @RequestBody @Valid PredicateUpdateDTO dto) {
        predicateService.updatePredicate(id, dto);
        return Result.success();
    }
    
    /**
     * 删除断言
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("predicate:delete")
    public Result<Void> deletePredicate(@PathVariable Long id) {
        predicateService.deletePredicate(id);
        return Result.success();
    }
    
    /**
     * 批量删除断言
     */
    @DeleteMapping("/batch")
    @SaCheckPermission("predicate:delete")
    public Result<Void> batchDeletePredicates(@RequestBody List<Long> ids) {
        predicateService.batchDelete(ids);
        return Result.success();
    }
    
    /**
     * 获取断言类型列表
     */
    @GetMapping("/types")
    public Result<List<PredicateTypeVO>> getPredicateTypes() {
        return Result.success(predicateService.getPredicateTypes());
    }
} 