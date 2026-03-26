package com.muxin.gateway.admin.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * <p>
 * 标注在 Mapper 方法或 Service 方法上，自动注入数据权限 SQL 条件
 * </p>
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    
    /**
     * 部门表的别名
     */
    String deptAlias() default "d";
    
    /**
     * 用户表的别名
     */
    String userAlias() default "u";
    
    /**
     * 部门ID字段名
     */
    String deptIdField() default "dept_id";
    
    /**
     * 用户ID字段名（用于"仅本人数据"权限）
     */
    String userIdField() default "create_by";
    
    /**
     * 是否启用数据权限（默认启用）
     */
    boolean enabled() default true;
}