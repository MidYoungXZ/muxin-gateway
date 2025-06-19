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
 * 部门控制器
 *
 * @author muxin
 */
@Slf4j
@RestController
@RequestMapping("/api/dept")
@RequiredArgsConstructor
@Tag(name = "部门管理", description = "部门管理相关接口")
public class DeptController {
    
    private final DeptService deptService;
    
    @GetMapping("/tree")
    @Operation(summary = "获取部门树", description = "获取部门树形结构")
    @SaCheckPermission("system:dept:list")
    public Result<List<DeptTreeVO>> getDeptTree() {
        List<DeptTreeVO> tree = deptService.getDeptTree();
        return Result.success(tree);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取部门详情", description = "根据ID获取部门详情")
    @Parameter(name = "id", description = "部门ID", required = true)
    @SaCheckPermission("system:dept:query")
    public Result<DeptVO> getDeptDetail(@PathVariable Long id) {
        DeptVO dept = deptService.getDeptDetail(id);
        return Result.success(dept);
    }
    
    @GetMapping("/children/{parentId}")
    @Operation(summary = "获取子部门列表", description = "根据父部门ID获取子部门列表")
    @Parameter(name = "parentId", description = "父部门ID", required = true)
    @SaCheckPermission("system:dept:list")
    public Result<List<DeptVO>> getChildrenDepts(@PathVariable Long parentId) {
        List<DeptVO> children = deptService.getChildrenDepts(parentId);
        return Result.success(children);
    }
    
    @PostMapping
    @Operation(summary = "创建部门", description = "创建新部门")
    @SaCheckPermission("system:dept:add")
    public Result<Long> createDept(@RequestBody @Validated DeptCreateDTO dto) {
        Long deptId = deptService.createDept(dto);
        return Result.success(deptId);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新部门", description = "更新部门信息")
    @Parameter(name = "id", description = "部门ID", required = true)
    @SaCheckPermission("system:dept:edit")
    public Result<Void> updateDept(@PathVariable Long id, @RequestBody @Validated DeptUpdateDTO dto) {
        deptService.updateDept(id, dto);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门", description = "删除部门")
    @Parameter(name = "id", description = "部门ID", required = true)
    @SaCheckPermission("system:dept:remove")
    public Result<Void> deleteDept(@PathVariable Long id) {
        deptService.deleteDept(id);
        return Result.success();
    }
    
    @PutMapping("/{id}/enable")
    @Operation(summary = "启用部门", description = "启用部门")
    @Parameter(name = "id", description = "部门ID", required = true)
    @SaCheckPermission("system:dept:edit")
    public Result<Void> enableDept(@PathVariable Long id) {
        deptService.enableDept(id);
        return Result.success();
    }
    
    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用部门", description = "禁用部门")
    @Parameter(name = "id", description = "部门ID", required = true)
    @SaCheckPermission("system:dept:edit")
    public Result<Void> disableDept(@PathVariable Long id) {
        deptService.disableDept(id);
        return Result.success();
    }
    
    @PutMapping("/{id}/move/{targetParentId}")
    @Operation(summary = "移动部门", description = "移动部门到新的父部门下")
    @Parameter(name = "id", description = "部门ID", required = true)
    @Parameter(name = "targetParentId", description = "目标父部门ID", required = true)
    @SaCheckPermission("system:dept:edit")
    public Result<Void> moveDept(@PathVariable Long id, @PathVariable Long targetParentId) {
        deptService.moveDept(id, targetParentId);
        return Result.success();
    }
    
    @GetMapping("/check-name")
    @Operation(summary = "检查部门名称", description = "检查部门名称是否可用")
    @SaCheckPermission("system:dept:query")
    public Result<Boolean> checkDeptName(@RequestParam String deptName, 
                                        @RequestParam Long parentId,
                                        @RequestParam(required = false) Long excludeId) {
        boolean available = deptService.checkDeptNameAvailable(deptName, parentId, excludeId);
        return Result.success(available);
    }
    
    @GetMapping("/check-code")
    @Operation(summary = "检查部门编码", description = "检查部门编码是否可用")
    @SaCheckPermission("system:dept:query")
    public Result<Boolean> checkDeptCode(@RequestParam String deptCode,
                                        @RequestParam(required = false) Long excludeId) {
        boolean available = deptService.checkDeptCodeAvailable(deptCode, excludeId);
        return Result.success(available);
    }
    
    @GetMapping("/stats")
    @Operation(summary = "获取部门统计", description = "获取部门统计信息")
    @SaCheckPermission("system:dept:query")
    public Result<Object> getDeptStats() {
        return Result.success(deptService.getDeptStats());
    }
} 