package com.muxin.gateway.admin.entity;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门实体
 *
 * @author muxin
 */
@Data
@Table("sys_dept")
public class SysDept {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    /**
     * 父部门ID
     */
    private Long parentId;
    
    /**
     * 部门名称
     */
    private String deptName;
    
    /**
     * 祖级列表
     */
    private String ancestors;
    
    /**
     * 部门编码
     */
    private String deptCode;
    
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
     * 排序
     */
    @Column("order_num")
    private Integer orderNum;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
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
    
    /**
     * 是否删除：0-否，1-是
     */
    @Column(isLogicDelete = true)
    private Integer deleted;
} 