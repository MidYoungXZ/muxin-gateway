package com.muxin.gateway.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.muxin.gateway.admin.model.Result;
import com.muxin.gateway.admin.model.dto.PredicateCreateDTO;
import com.muxin.gateway.admin.model.dto.PredicateQueryDTO;
import com.muxin.gateway.admin.model.dto.PredicateUpdateDTO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.PredicateTypeVO;
import com.muxin.gateway.admin.model.vo.PredicateVO;
import com.muxin.gateway.admin.model.vo.RouteSimpleVO;
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
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/predicates")
@RequiredArgsConstructor
public class PredicateController {
    
    private final PredicateService predicateService;
    
    @GetMapping
    @SaCheckPermission("route:predicate:list")
    public Result<PageVO<PredicateVO>> listPredicates(PredicateQueryDTO query) {
        return Result.success(predicateService.pageQuery(query));
    }
    
    @GetMapping("/available")
    @SaCheckPermission("route:predicate:list")
    public Result<List<PredicateVO>> getAvailablePredicates() {
        return Result.success(predicateService.getAvailablePredicates());
    }
    
    @GetMapping("/type/{type}")
    @SaCheckPermission("route:predicate:list")
    public Result<List<PredicateVO>> getPredicatesByType(@PathVariable String type) {
        return Result.success(predicateService.getByType(type));
    }
    
    @GetMapping("/{id}")
    @SaCheckPermission("route:predicate:view")
    public Result<PredicateVO> getPredicate(@PathVariable Long id) {
        return Result.success(predicateService.getPredicateDetail(id));
    }
    
    @PostMapping
    @SaCheckPermission("route:predicate:create")
    public Result<Long> createPredicate(@RequestBody @Valid PredicateCreateDTO dto) {
        return Result.success(predicateService.createPredicate(dto));
    }
    
    @PutMapping("/{id}")
    @SaCheckPermission("route:predicate:update")
    public Result<Void> updatePredicate(@PathVariable Long id, 
                                       @RequestBody @Valid PredicateUpdateDTO dto) {
        predicateService.updatePredicate(id, dto);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    @SaCheckPermission("route:predicate:delete")
    public Result<Void> deletePredicate(@PathVariable Long id) {
        predicateService.deletePredicate(id);
        return Result.success();
    }
    
    @DeleteMapping("/batch")
    @SaCheckPermission("route:predicate:delete")
    public Result<Void> batchDeletePredicates(@RequestBody List<Long> ids) {
        predicateService.batchDelete(ids);
        return Result.success();
    }
    
    @GetMapping("/types")
    public Result<List<PredicateTypeVO>> getPredicateTypes() {
        return Result.success(predicateService.getPredicateTypes());
    }

    @PostMapping("/{id}/enable")
    @SaCheckPermission("route:predicate:update")
    public Result<Void> enablePredicate(@PathVariable Long id) {
        predicateService.enablePredicate(id);
        return Result.success();
    }

    @PostMapping("/{id}/disable")
    @SaCheckPermission("route:predicate:update")
    public Result<Void> disablePredicate(@PathVariable Long id) {
        predicateService.disablePredicate(id);
        return Result.success();
    }

    @GetMapping("/{id}/routes")
    @SaCheckPermission("route:predicate:list")
    public Result<List<RouteSimpleVO>> getUsedRoutes(@PathVariable Long id) {
        return Result.success(predicateService.getUsedRoutes(id));
    }
} 