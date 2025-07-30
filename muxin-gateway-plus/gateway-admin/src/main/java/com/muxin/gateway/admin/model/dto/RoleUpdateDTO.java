package com.muxin.gateway.admin.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 更新角色DTO
 *
 * @author muxin
 */
@Data
public class RoleUpdateDTO {
    
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