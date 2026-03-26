package com.muxin.gateway.admin.context;

import lombok.Data;

import java.util.List;

/**
 * 数据权限上下文
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class DataScopeContext {
    
    /**
     * 数据范围类型
     * 1-全部数据
     * 2-自定义数据
     * 3-本部门数据
     * 4-本部门及以下数据
     * 5-仅本人数据
     */
    private Integer dataScope;
    
    /**
     * 当前用户ID
     */
    private Long userId;
    
    /**
     * 当前用户部门ID
     */
    private Long deptId;
    
    /**
     * 自定义部门ID列表（dataScope=2时使用）
     */
    private List<Long> deptIds;
    
    /**
     * 部门及其子部门ID列表（dataScope=4时使用）
     */
    private List<Long> deptAndChildrenIds;
    
    private static final ThreadLocal<DataScopeContext> CONTEXT = new ThreadLocal<>();
    
    public static void set(DataScopeContext context) {
        CONTEXT.set(context);
    }
    
    public static DataScopeContext get() {
        return CONTEXT.get();
    }
    
    public static void clear() {
        CONTEXT.remove();
    }
}