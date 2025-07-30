package com.muxin.gateway.admin.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

/**
 * sys_role_menu 表定义
 *
 * @author mybatis-flex-processor
 * @since 2024-01-01
 */
public class SysRoleMenuTableDef extends TableDef {

    /**
     * 角色菜单关联表
     */
    public static final SysRoleMenuTableDef SYS_ROLE_MENU = new SysRoleMenuTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 角色ID
     */
    public final QueryColumn ROLE_ID = new QueryColumn(this, "role_id");

    /**
     * 菜单ID
     */
    public final QueryColumn MENU_ID = new QueryColumn(this, "menu_id");

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
            ID, ROLE_ID, MENU_ID, CREATE_TIME
    };

    public SysRoleMenuTableDef() {
        super("", "sys_role_menu");
    }

    private SysRoleMenuTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public SysRoleMenuTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new SysRoleMenuTableDef("", "sys_role_menu", alias));
    }
} 