package com.muxin.gateway.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.muxin.gateway.admin.model.Result;
import com.muxin.gateway.admin.model.dto.FilterCreateDTO;
import com.muxin.gateway.admin.model.dto.FilterQueryDTO;
import com.muxin.gateway.admin.model.dto.FilterUpdateDTO;
import com.muxin.gateway.admin.model.vo.FilterTypeVO;
import com.muxin.gateway.admin.model.vo.FilterVO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.model.vo.RouteSimpleVO;
import com.muxin.gateway.admin.service.FilterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 过滤器管理控制器.
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/filters")
@RequiredArgsConstructor
public class FilterController {

    private final FilterService filterService;

    @GetMapping
    @SaCheckPermission("route:filter:list")
    public Result<PageVO<FilterVO>> listFilters(FilterQueryDTO query) {
        return Result.success(filterService.pageQuery(query));
    }

    @GetMapping("/available")
    @SaCheckPermission("route:filter:list")
    public Result<List<FilterVO>> getAvailableFilters() {
        return Result.success(filterService.getAvailableFilters());
    }

    @GetMapping("/type/{type}")
    @SaCheckPermission("route:filter:list")
    public Result<List<FilterVO>> getFiltersByType(@PathVariable String type) {
        return Result.success(filterService.getByType(type));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("route:filter:view")
    public Result<FilterVO> getFilter(@PathVariable Long id) {
        return Result.success(filterService.getFilterDetail(id));
    }

    @PostMapping
    @SaCheckPermission("route:filter:create")
    public Result<Long> createFilter(@RequestBody @Valid FilterCreateDTO dto) {
        return Result.success(filterService.createFilter(dto));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("route:filter:update")
    public Result<Void> updateFilter(@PathVariable Long id,
                                   @RequestBody @Valid FilterUpdateDTO dto) {
        filterService.updateFilter(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("route:filter:delete")
    public Result<Void> deleteFilter(@PathVariable Long id) {
        filterService.deleteFilter(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @SaCheckPermission("route:filter:delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        filterService.batchDelete(ids);
        return Result.success();
    }

    @PostMapping("/{id}/enable")
    @SaCheckPermission("route:filter:update")
    public Result<Void> enableFilter(@PathVariable Long id) {
        filterService.enableFilter(id);
        return Result.success();
    }

    @PostMapping("/{id}/disable")
    @SaCheckPermission("route:filter:update")
    public Result<Void> disableFilter(@PathVariable Long id) {
        filterService.disableFilter(id);
        return Result.success();
    }

    @GetMapping("/types")
    @SaCheckPermission("route:filter:list")
    public Result<List<FilterTypeVO>> getFilterTypes() {
        return Result.success(filterService.getFilterTypes());
    }

    @GetMapping("/{id}/routes")
    @SaCheckPermission("route:filter:list")
    public Result<List<RouteSimpleVO>> getUsedRoutes(@PathVariable Long id) {
        return Result.success(filterService.getUsedRoutes(id));
    }
} 