package com.muxin.gateway.admin.entity.table;

/**
 * 所有表定义的统一管理类
 *
 * @author mybatis-flex
 * @since 2024-01-01
 */
public final class Tables {
    
    /**
     * 路由配置表
     */
    public static final GwRouteTableDef GW_ROUTE = GwRouteTableDef.GW_ROUTE;
    
    /**
     * 过滤器配置表
     */
    public static final GwFilterTableDef GW_FILTER = GwFilterTableDef.GW_FILTER;
    
    /**
     * 断言配置表
     */
    public static final GwPredicateTableDef GW_PREDICATE = GwPredicateTableDef.GW_PREDICATE;
    
    /**
     * 路由过滤器关联表
     */
    public static final GwRouteFilterTableDef GW_ROUTE_FILTER = GwRouteFilterTableDef.GW_ROUTE_FILTER;
    
    /**
     * 路由断言关联表
     */
    public static final GwRoutePredicateTableDef GW_ROUTE_PREDICATE = GwRoutePredicateTableDef.GW_ROUTE_PREDICATE;
    
    /**
     * 监控指标表
     */
    public static final GwMetricsTableDef GW_METRICS = GwMetricsTableDef.GW_METRICS;
    
    /**
     * 用户表
     */
    public static final SysUserTableDef SYS_USER = SysUserTableDef.SYS_USER;
    
    /**
     * 角色表
     */
    public static final SysRoleTableDef SYS_ROLE = SysRoleTableDef.SYS_ROLE;
    
    /**
     * 用户角色关联表
     */
    public static final SysUserRoleTableDef SYS_USER_ROLE = SysUserRoleTableDef.SYS_USER_ROLE;
    
    /**
     * 角色菜单关联表
     */
    public static final SysRoleMenuTableDef SYS_ROLE_MENU = SysRoleMenuTableDef.SYS_ROLE_MENU;
    
    /**
     * 菜单表
     */
    public static final SysMenuTableDef SYS_MENU = SysMenuTableDef.SYS_MENU;
    
    /**
     * 部门表
     */
    public static final SysDeptTableDef SYS_DEPT = SysDeptTableDef.SYS_DEPT;
    
    private Tables() {
        // 私有构造函数，防止实例化
    }
} 