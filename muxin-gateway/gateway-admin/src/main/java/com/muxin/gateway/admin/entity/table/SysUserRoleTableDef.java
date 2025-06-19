package com.muxin.gateway.admin.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

/**
 * sys_user_role 表定义
 *
 * @author mybatis-flex-processor
 * @since 2024-01-01
 */
public class SysUserRoleTableDef extends TableDef {

    /**
     * 用户角色关联表
     */
    public static final SysUserRoleTableDef SYS_USER_ROLE = new SysUserRoleTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 用户ID
     */
    public final QueryColumn USER_ID = new QueryColumn(this, "user_id");

    /**
     * 角色ID
     */
    public final QueryColumn ROLE_ID = new QueryColumn(this, "role_id");

    /**
     * 创建时间
     */
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    /**
     * 所有字段
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{
            ID, USER_ID, ROLE_ID, CREATE_TIME
    };

    public SysUserRoleTableDef() {
        super("", "sys_user_role");
    }

    private SysUserRoleTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public SysUserRoleTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new SysUserRoleTableDef("", "sys_user_role", alias));
    }
} 