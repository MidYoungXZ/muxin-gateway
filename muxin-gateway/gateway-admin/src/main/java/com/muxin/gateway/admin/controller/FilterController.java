package com.muxin.gateway.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.muxin.gateway.admin.model.Result;
import com.muxin.gateway.admin.model.dto.FilterCreateDTO;
import com.muxin.gateway.admin.model.dto.FilterQueryDTO;
import com.muxin.gateway.admin.model.dto.FilterUpdateDTO;
import com.muxin.gateway.admin.model.vo.FilterTypeVO;
import com.muxin.gateway.admin.model.vo.FilterVO;
import com.muxin.gateway.admin.model.vo.PageVO;
import com.muxin.gateway.admin.service.FilterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 过滤器管理控制器
 *
 * @author muxin
 */
@Slf4j
@RestController
@RequestMapping("/api/filters")
@RequiredArgsConstructor
public class FilterController {
    
    private final FilterService filterService;
    
    /**
     * 分页查询过滤器列表
     */
    @GetMapping
    @SaCheckPermission("filter:list")
    public Result<PageVO<FilterVO>> listFilters(FilterQueryDTO query) {
        return Result.success(filterService.pageQuery(query));
    }
    
    /**
     * 获取所有可用过滤器（用于路由配置时选择）
     */
    @GetMapping("/available")
    @SaCheckPermission("filter:list")
    public Result<List<FilterVO>> getAvailableFilters() {
        return Result.success(filterService.getAvailableFilters());
    }
    
    /**
     * 根据类型获取过滤器列表
     */
    @GetMapping("/type/{type}")
    @SaCheckPermission("filter:list")
    public Result<List<FilterVO>> getFiltersByType(@PathVariable String type) {
        return Result.success(filterService.getByType(type));
    }
    
    /**
     * 获取过滤器详情
     */
    @GetMapping("/{id}")
    @SaCheckPermission("filter:view")
    public Result<FilterVO> getFilter(@PathVariable Long id) {
        return Result.success(filterService.getFilterDetail(id));
    }
    
    /**
     * 创建过滤器
     */
    @PostMapping
    @SaCheckPermission("filter:create")
    public Result<Long> createFilter(@RequestBody @Valid FilterCreateDTO dto) {
        return Result.success(filterService.createFilter(dto));
    }
    
    /**
     * 更新过滤器
     */
    @PutMapping("/{id}")
    @SaCheckPermission("filter:update")
    public Result<Void> updateFilter(@PathVariable Long id, 
                                   @RequestBody @Valid FilterUpdateDTO dto) {
        filterService.updateFilter(id, dto);
        return Result.success();
    }
    
    /**
     * 删除过滤器
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("filter:delete")
    public Result<Void> deleteFilter(@PathVariable Long id) {
        filterService.deleteFilter(id);
        return Result.success();
    }
    
    /**
     * 批量删除过滤器
     */
    @DeleteMapping("/batch")
    @SaCheckPermission("filter:delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        filterService.batchDelete(ids);
        return Result.success();
    }
    
    /**
     * 启用过滤器
     */
    @PostMapping("/{id}/enable")
    @SaCheckPermission("filter:update")
    public Result<Void> enableFilter(@PathVariable Long id) {
        filterService.enableFilter(id);
        return Result.success();
    }
    
    /**
     * 禁用过滤器
     */
    @PostMapping("/{id}/disable")
    @SaCheckPermission("filter:update")
    public Result<Void> disableFilter(@PathVariable Long id) {
        filterService.disableFilter(id);
        return Result.success();
    }
    
    /**
     * 获取过滤器类型列表
     */
    @GetMapping("/types")
    @SaCheckPermission("filter:list")
    public Result<List<FilterTypeVO>> getFilterTypes() {
        return Result.success(filterService.getFilterTypes());
    }
} 