package com.muxin.gateway.admin.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解.
 * <p>
 * 该注解用于标记需要记录操作日志的方法，支持自定义模块名称、操作类型，
 * 以及是否记录请求参数和返回结果。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * @OperationLog(module = "用户管理", operation = "创建用户")
 * public Result<User> createUser(@RequestBody UserCreateDTO dto) {
 *     // 方法实现
 * }
 * }</pre>
 * </p>
 *
 * @author muxin
 * @since 1.0.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 模块名称.
     * <p>
     * 用于标识操作所属的功能模块，如"用户管理"、"角色管理"等。
     * </p>
     *
     * @return 模块名称，默认为空字符串
     */
    String module() default "";

    /**
     * 操作类型.
     * <p>
     * 用于描述具体的操作动作，如"创建用户"、"更新角色"、"删除权限"等。
     * </p>
     *
     * @return 操作类型描述，默认为空字符串
     */
    String operation() default "";

    /**
     * 是否记录请求参数.
     * <p>
     * 设置为true时，会将方法的请求参数记录到操作日志中。
     * </p>
     *
     * @return true表示记录请求参数，false表示不记录，默认为true
     */
    boolean includeParams() default true;

    /**
     * 是否记录返回结果.
     * <p>
     * 设置为true时，会将方法的返回结果记录到操作日志中。
     * </p>
     *
     * @return true表示记录返回结果，false表示不记录，默认为true
     */
    boolean includeResult() default true;
} 