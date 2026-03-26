package com.muxin.gateway.admin.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色VO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class RoleVO {
    
    /**
     * 角色ID
     */
    private Long id;
    
    /**
     * 角色编码
     */
    private String roleCode;
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 状态文本
     */
    private String statusText;
    
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
     * 数据范围文本
     */
    private String dataScopeText;
    
    /**
     * 自定义部门ID列表（dataScope=2时使用）
     */
    private List<Long> deptIds;
    
    /**
     * 用户数量
     */
    private Long userCount;
    
    /**
     * 菜单列表
     */
    private List<MenuVO> menus;
    
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