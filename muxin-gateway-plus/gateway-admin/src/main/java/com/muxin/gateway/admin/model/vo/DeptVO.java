package com.muxin.gateway.admin.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门VO
 *
 * @author muxin
 */
@Data
public class DeptVO {
    
    /**
     * 部门ID
     */
    private Long id;
    
    /**
     * 部门名称
     */
    private String deptName;
    
    /**
     * 父部门ID
     */
    private Long parentId;
    
    /**
     * 父部门名称
     */
    private String parentName;
    
    /**
     * 祖级列表
     */
    private String ancestors;
    
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
     * 状态文本
     */
    private String statusText;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 创建人
     */
    private String createBy;
    
    /**
     * 更新人
     */
    private String updateBy;
} 