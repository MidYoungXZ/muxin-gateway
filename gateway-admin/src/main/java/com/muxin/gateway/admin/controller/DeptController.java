package com.muxin.gateway.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.muxin.gateway.admin.model.Result;
import com.muxin.gateway.admin.model.dto.DeptCreateDTO;
import com.muxin.gateway.admin.model.dto.DeptUpdateDTO;
import com.muxin.gateway.admin.model.vo.DeptTreeVO;
import com.muxin.gateway.admin.model.vo.DeptVO;
import com.muxin.gateway.admin.service.DeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器.
 * <p>
 * 该控制器负责处理部门管理的HTTP请求，包括：
 * <ul>
 *     <li>部门树的查询</li>
 *     <li>部门的创建、更新、删除</li>
 *     <li>部门的启用、禁用</li>
 *     <li>部门的移动和排序</li>
 *     <li>部门名称和编码的唯一性检查</li>
 * </ul>
 * </p>
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/dept")
@RequiredArgsConstructor
@Tag(name = "部门管理", description = "部门管理相关接口")
public class DeptController {

    private final DeptService deptService;

    /**
     * 获取部门树.
     * <p>
     * 返回完整的部门树形结构，包含所有部门及其层级关系。
     * </p>
     *
     * @return 部门树形结构列表
     */
    @GetMapping("/tree")
    @Operation(summary = "获取部门树", description = "获取部门树形结构")
    @SaCheckPermission("system:dept:list")
    public Result<List<DeptTreeVO>> getDeptTree() {
        List<DeptTreeVO> tree = deptService.getDeptTree();
        return Result.success(tree);
    }

    /**
     * 获取部门选项列表.
     * <p>
     * 返回启用的部门树形结构，用于下拉选择器。
     * 无需权限校验，登录用户即可访问。
     * </p>
     *
     * @return 部门选项列表
     */
    @GetMapping("/options")
    @Operation(summary = "获取部门选项", description = "获取部门选项列表（用于下拉选择）")
    public Result<List<DeptTreeVO>> getDeptOptions() {
        List<DeptTreeVO> options = deptService.getDeptOptions();
        return Result.success(options);
    }

    /**
     * 获取部门详情.
     * <p>
     * 根据部门ID获取部门的详细信息。
     * </p>
     *
     * @param id 部门ID
     * @return 部门详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取部门详情", description = "根据ID获取部门详情")
    @Parameter(name = "id", description = "部门ID", required = true)
    @SaCheckPermission("system:dept:query")
    public Result<DeptVO> getDeptDetail(@PathVariable Long id) {
        DeptVO dept = deptService.getDeptDetail(id);
        return Result.success(dept);
    }

    /**
     * 获取子部门列表.
     * <p>
     * 根据父部门ID获取其直接子部门列表（不包含孙部门）。
     * </p>
     *
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    @GetMapping("/children/{parentId}")
    @Operation(summary = "获取子部门列表", description = "根据父部门ID获取子部门列表")
    @Parameter(name = "parentId", description = "父部门ID", required = true)
    @SaCheckPermission("system:dept:list")
    public Result<List<DeptVO>> getChildrenDepts(@PathVariable Long parentId) {
        List<DeptVO> children = deptService.getChildrenDepts(parentId);
        return Result.success(children);
    }

    /**
     * 创建部门.
     * <p>
     * 创建新的部门，支持设置父部门以建立层级关系。
     * </p>
     *
     * @param dto 部门创建数据传输对象
     * @return 创建的部门ID
     */
    @PostMapping
    @Operation(summary = "创建部门", description = "创建新部门")
    @SaCheckPermission("system:dept:add")
    public Result<Long> createDept(@RequestBody @Validated DeptCreateDTO dto) {
        Long deptId = deptService.createDept(dto);
        return Result.success(deptId);
    }

    /**
     * 更新部门信息.
     * <p>
     * 更新指定部门的基本信息，不包括父部门关系。
     * </p>
     *
     * @param id 部门ID
     * @param dto 部门更新数据传输对象
     * @return 空响应结果，表示更新成功
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新部门", description = "更新部门信息")
    @Parameter(name = "id", description = "部门ID", required = true)
    @SaCheckPermission("system:dept:edit")
    public Result<Void> updateDept(@PathVariable Long id, @RequestBody @Validated DeptUpdateDTO dto) {
        deptService.updateDept(id, dto);
        return Result.success();
    }

    /**
     * 删除部门.
     * <p>
     * 删除指定部门。如果部门下有子部门或关联用户，则不允许删除。
     * </p>
     *
     * @param id 部门ID
     * @return 空响应结果，表示删除成功
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门", description = "删除部门")
    @Parameter(name = "id", description = "部门ID", required = true)
    @SaCheckPermission("system:dept:remove")
    public Result<Void> deleteDept(@PathVariable Long id) {
        deptService.deleteDept(id);
        return Result.success();
    }

    /**
     * 启用部门.
     * <p>
     * 将已禁用的部门重新启用，使其可用于用户分配。
     * </p>
     *
     * @param id 部门ID
     * @return 空响应结果，表示启用成功
     */
    @PutMapping("/{id}/enable")
    @Operation(summary = "启用部门", description = "启用部门")
    @Parameter(name = "id", description = "部门ID", required = true)
    @SaCheckPermission("system:dept:edit")
    public Result<Void> enableDept(@PathVariable Long id) {
        deptService.enableDept(id);
        return Result.success();
    }

    /**
     * 禁用部门.
     * <p>
     * 禁用部门，禁用后的部门不能用于新用户分配，但现有用户不受影响。
     * </p>
     *
     * @param id 部门ID
     * @return 空响应结果，表示禁用成功
     */
    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用部门", description = "禁用部门")
    @Parameter(name = "id", description = "部门ID", required = true)
    @SaCheckPermission("system:dept:edit")
    public Result<Void> disableDept(@PathVariable Long id) {
        deptService.disableDept(id);
        return Result.success();
    }

    /**
     * 移动部门.
     * <p>
     * 将部门移动到新的父部门下，用于调整部门层级结构。
     * </p>
     *
     * @param id 部门ID
     * @param targetParentId 目标父部门ID
     * @return 空响应结果，表示移动成功
     */
    @PutMapping("/{id}/move/{targetParentId}")
    @Operation(summary = "移动部门", description = "移动部门到新的父部门下")
    @Parameter(name = "id", description = "部门ID", required = true)
    @Parameter(name = "targetParentId", description = "目标父部门ID", required = true)
    @SaCheckPermission("system:dept:edit")
    public Result<Void> moveDept(@PathVariable Long id, @PathVariable Long targetParentId) {
        deptService.moveDept(id, targetParentId);
        return Result.success();
    }

    /**
     * 检查部门名称是否可用.
     * <p>
     * 在创建或更新部门时，检查部门名称在同级父部门下是否唯一。
     * </p>
     *
     * @param deptName 部门名称
     * @param parentId 父部门ID
     * @param excludeId 排除的部门ID（更新时使用，排除自身）
     * @return true表示名称可用，false表示名称已存在
     */
    @GetMapping("/check-name")
    @Operation(summary = "检查部门名称", description = "检查部门名称是否可用")
    @SaCheckPermission("system:dept:query")
    public Result<Boolean> checkDeptName(@RequestParam String deptName,
                                        @RequestParam Long parentId,
                                        @RequestParam(required = false) Long excludeId) {
        boolean available = deptService.checkDeptNameAvailable(deptName, parentId, excludeId);
        return Result.success(available);
    }

    /**
     * 检查部门编码是否可用.
     * <p>
     * 在创建或更新部门时，检查部门编码在全局是否唯一。
     * </p>
     *
     * @param deptCode 部门编码
     * @param excludeId 排除的部门ID（更新时使用，排除自身）
     * @return true表示编码可用，false表示编码已存在
     */
    @GetMapping("/check-code")
    @Operation(summary = "检查部门编码", description = "检查部门编码是否可用")
    @SaCheckPermission("system:dept:query")
    public Result<Boolean> checkDeptCode(@RequestParam String deptCode,
                                        @RequestParam(required = false) Long excludeId) {
        boolean available = deptService.checkDeptCodeAvailable(deptCode, excludeId);
        return Result.success(available);
    }

    /**
     * 获取部门统计信息.
     * <p>
     * 返回部门的统计数据，如部门总数、用户数量等。
     * </p>
     *
     * @return 部门统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取部门统计", description = "获取部门统计信息")
    @SaCheckPermission("system:dept:query")
    public Result<Object> getDeptStats() {
        return Result.success(deptService.getDeptStats());
    }
} 