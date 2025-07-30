package com.muxin.gateway.admin.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 *
 * @author muxin
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    
    /**
     * 模块名称
     */
    String module() default "";
    
    /**
     * 操作类型
     */
    String operation() default "";
    
    /**
     * 是否记录请求参数
     */
    boolean includeParams() default true;
    
    /**
     * 是否记录返回结果
     */
    boolean includeResult() default true;
} 