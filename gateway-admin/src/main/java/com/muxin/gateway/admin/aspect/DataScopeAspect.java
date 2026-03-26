package com.muxin.gateway.admin.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.muxin.gateway.admin.annotation.DataScope;
import com.muxin.gateway.admin.context.DataScopeContext;
import com.muxin.gateway.admin.util.DataScopeHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

import static com.muxin.gateway.admin.entity.table.Tables.*;

/**
 * 数据权限切面
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DataScopeAspect {
    
    private final DataScopeHelper dataScopeHelper;
    
    @Around("@annotation(dataScope)")
    public Object around(ProceedingJoinPoint point, DataScope dataScope) throws Throwable {
        if (!dataScope.enabled()) {
            return point.proceed();
        }
        
        if (!StpUtil.isLogin()) {
            return point.proceed();
        }
        
        DataScopeContext context = dataScopeHelper.getCurrentUserContext();
        DataScopeContext.set(context);
        
        try {
            Object[] args = point.getArgs();
            if (args != null && args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof QueryWrapper wrapper) {
                        injectDataScope(wrapper, dataScope, context);
                        args[i] = wrapper;
                    }
                }
            }
            
            return point.proceed(args);
        } finally {
            DataScopeContext.clear();
        }
    }
    
    private void injectDataScope(QueryWrapper wrapper, DataScope dataScope, DataScopeContext context) {
        Integer dataScopeValue = context.getDataScope();
        if (dataScopeValue == null) {
            dataScopeValue = 4;
        }
        
        switch (dataScopeValue) {
            case 1:
                break;
            case 2:
                List<Long> deptIds = context.getDeptIds();
                if (deptIds != null && !deptIds.isEmpty()) {
                    wrapper.and(SYS_USER.DEPT_ID.in(deptIds));
                } else {
                    wrapper.and(SYS_USER.DEPT_ID.eq(-1L));
                }
                break;
            case 3:
                if (context.getDeptId() != null) {
                    wrapper.and(SYS_USER.DEPT_ID.eq(context.getDeptId()));
                } else {
                    wrapper.and(SYS_USER.DEPT_ID.eq(-1L));
                }
                break;
            case 4:
                List<Long> deptAndChildrenIds = context.getDeptAndChildrenIds();
                if (deptAndChildrenIds != null && !deptAndChildrenIds.isEmpty()) {
                    wrapper.and(SYS_USER.DEPT_ID.in(deptAndChildrenIds));
                } else if (context.getDeptId() != null) {
                    wrapper.and(SYS_USER.DEPT_ID.eq(context.getDeptId()));
                } else {
                    wrapper.and(SYS_USER.DEPT_ID.eq(-1L));
                }
                break;
            case 5:
                wrapper.and(SYS_USER.ID.eq(context.getUserId()));
                break;
            default:
                if (context.getDeptId() != null) {
                    wrapper.and(SYS_USER.DEPT_ID.eq(context.getDeptId()));
                }
        }
    }
}