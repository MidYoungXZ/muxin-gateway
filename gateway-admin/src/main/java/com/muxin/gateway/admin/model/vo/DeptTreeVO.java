package com.muxin.gateway.admin.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class DeptTreeVO {
    
    private Long id;
    
    private String deptName;
    
    private String deptCode;
    
    private Long parentId;
    
    private Integer orderNum;
    
    private String leader;
    
    private String phone;
    
    private String email;
    
    private Integer status;
    
    private String createTime;
    
    private List<DeptTreeVO> children;
}