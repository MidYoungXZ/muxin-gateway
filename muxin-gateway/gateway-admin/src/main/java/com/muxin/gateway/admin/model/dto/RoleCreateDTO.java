package com.muxin.gateway.admin.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * 创建角色DTO
 *
 * @author muxin
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
     * 菜单ID列表
     */
    private List<Long> menuIds;
} 