package com.muxin.gateway.admin.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class DeptCreateDTO {
    
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 50, message = "部门名称长度不能超过50个字符")
    private String deptName;
    
    private String deptCode;
    
    private Long parentId;
    
    private Integer orderNum;
    
    @Size(max = 50, message = "负责人长度不能超过50个字符")
    private String leader;
    
    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String phone;
    
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;
    
    private Integer status = 1;
}