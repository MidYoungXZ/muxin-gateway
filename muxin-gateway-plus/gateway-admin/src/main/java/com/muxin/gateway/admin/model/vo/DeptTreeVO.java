package com.muxin.gateway.admin.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 部门树VO
 *
 * @author muxin
 */
@Data
public class DeptTreeVO {
    
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
     * 排序
     */
    private Integer sort;
    
    /**
     * 状态(0:禁用，1:正常)
     */
    private Integer status;
    
    /**
     * 子部门列表
     */
    private List<DeptTreeVO> children;
} 