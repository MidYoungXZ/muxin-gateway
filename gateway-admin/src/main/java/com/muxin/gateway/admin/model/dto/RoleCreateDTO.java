package com.muxin.gateway.admin.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * 创建角色DTO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class RoleCreateDTO {
    
    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
    @Pattern(regexp = "^[A-Z_]+$", message = "角色编码只能包含大写字母和下划线")
    private String roleCode;
    
    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    private String roleName;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 数据范围：
     * 1-全部数据
     * 2-自定义数据
     * 3-本部门数据
     * 4-本部门及以下数据
     * 5-仅本人数据
     */
    private Integer dataScope;
    
    /**
     * 自定义部门ID列表（dataScope=2时使用）
     */
    private List<Long> deptIds;
    
    /**
     * 菜单ID列表
     */
    private List<Long> menuIds;
} 