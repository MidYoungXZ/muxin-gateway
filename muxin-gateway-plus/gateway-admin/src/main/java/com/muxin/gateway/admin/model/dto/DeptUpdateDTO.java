package com.muxin.gateway.admin.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 部门更新DTO
 *
 * @author muxin
 */
@Data
public class DeptUpdateDTO {
    
    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    private String deptName;
    
    /**
     * 父部门ID
     */
    private Long parentId;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 负责人
     */
    private String leader;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 状态(0:禁用，1:正常)
     */
    private Integer status;
    
    /**
     * 备注
     */
    private String remark;
} 