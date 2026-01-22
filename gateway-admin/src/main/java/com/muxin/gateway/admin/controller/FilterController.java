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
 * 过滤器管理控制器.
 * <p>
 * 该控制器负责处理网关过滤器管理的HTTP请求，包括：
 * <ul>
 *     <li>过滤器的查询（分页、按类型、可用过滤器）</li>
 *     <li>过滤器的创建、更新、删除</li>
 *     <li>过滤器的启用、禁用</li>
 *     <li>过滤器类型查询</li>
 * </ul>
 * </p>
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

    /**
     * 分页查询过滤器列表.
     * <p>
     * 支持按条件分页查询过滤器列表。
     * </p>
     *
     * @param query 查询条件对象
     * @return 分页结果
     */
    @GetMapping
    @SaCheckPermission("filter:list")
    public Result<PageVO<FilterVO>> listFilters(FilterQueryDTO query) {
        return Result.success(filterService.pageQuery(query));
    }

    /**
     * 获取所有可用过滤器.
     * <p>
     * 获取所有已启用的过滤器列表，用于路由配置时选择。
     * </p>
     *
     * @return 可用过滤器列表
     */
    @GetMapping("/available")
    @SaCheckPermission("filter:list")
    public Result<List<FilterVO>> getAvailableFilters() {
        return Result.success(filterService.getAvailableFilters());
    }

    /**
     * 根据类型获取过滤器列表.
     * <p>
     * 获取指定类型的所有过滤器。
     * </p>
     *
     * @param type 过滤器类型（Pre、Post、Route等）
     * @return 指定类型的过滤器列表
     */
    @GetMapping("/type/{type}")
    @SaCheckPermission("filter:list")
    public Result<List<FilterVO>> getFiltersByType(@PathVariable String type) {
        return Result.success(filterService.getByType(type));
    }

    /**
     * 获取过滤器详情.
     * <p>
     * 根据ID获取过滤器的详细信息。
     * </p>
     *
     * @param id 过滤器ID
     * @return 过滤器详细信息
     */
    @GetMapping("/{id}")
    @SaCheckPermission("filter:view")
    public Result<FilterVO> getFilter(@PathVariable Long id) {
        return Result.success(filterService.getFilterDetail(id));
    }

    /**
     * 创建过滤器.
     * <p>
     * 创建新的网关过滤器配置。
     * </p>
     *
     * @param dto 过滤器创建数据传输对象
     * @return 创建的过滤器ID
     */
    @PostMapping
    @SaCheckPermission("filter:create")
    public Result<Long> createFilter(@RequestBody @Valid FilterCreateDTO dto) {
        return Result.success(filterService.createFilter(dto));
    }

    /**
     * 更新过滤器.
     * <p>
     * 更新指定过滤器的配置信息。
     * </p>
     *
     * @param id 过滤器ID
     * @param dto 过滤器更新数据传输对象
     * @return 空响应结果，表示更新成功
     */
    @PutMapping("/{id}")
    @SaCheckPermission("filter:update")
    public Result<Void> updateFilter(@PathVariable Long id,
                                   @RequestBody @Valid FilterUpdateDTO dto) {
        filterService.updateFilter(id, dto);
        return Result.success();
    }

    /**
     * 删除过滤器.
     * <p>
     * 删除指定的过滤器。系统内置过滤器不允许删除。
     * </p>
     *
     * @param id 过滤器ID
     * @return 空响应结果，表示删除成功
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("filter:delete")
    public Result<Void> deleteFilter(@PathVariable Long id) {
        filterService.deleteFilter(id);
        return Result.success();
    }

    /**
     * 批量删除过滤器.
     * <p>
     * 批量删除多个过滤器。
     * </p>
     *
     * @param ids 过滤器ID列表
     * @return 空响应结果，表示删除成功
     */
    @DeleteMapping("/batch")
    @SaCheckPermission("filter:delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        filterService.batchDelete(ids);
        return Result.success();
    }

    /**
     * 启用过滤器.
     * <p>
     * 将已禁用的过滤器重新启用。
     * </p>
     *
     * @param id 过滤器ID
     * @return 空响应结果，表示启用成功
     */
    @PostMapping("/{id}/enable")
    @SaCheckPermission("filter:update")
    public Result<Void> enableFilter(@PathVariable Long id) {
        filterService.enableFilter(id);
        return Result.success();
    }

    /**
     * 禁用过滤器.
     * <p>
     * 禁用指定的过滤器，禁用后不会被应用到路由。
     * </p>
     *
     * @param id 过滤器ID
     * @return 空响应结果，表示禁用成功
     */
    @PostMapping("/{id}/disable")
    @SaCheckPermission("filter:update")
    public Result<Void> disableFilter(@PathVariable Long id) {
        filterService.disableFilter(id);
        return Result.success();
    }

    /**
     * 获取过滤器类型列表.
     * <p>
     * 获取所有支持的过滤器类型。
     * </p>
     *
     * @return 过滤器类型列表
     */
    @GetMapping("/types")
    @SaCheckPermission("filter:list")
    public Result<List<FilterTypeVO>> getFilterTypes() {
        return Result.success(filterService.getFilterTypes());
    }
} 