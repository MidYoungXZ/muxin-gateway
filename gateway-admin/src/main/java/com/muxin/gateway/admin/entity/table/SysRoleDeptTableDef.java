package com.muxin.gateway.admin.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

/**
 * sys_role_dept 表定义
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public class SysRoleDeptTableDef extends TableDef {

    public static final SysRoleDeptTableDef SYS_ROLE_DEPT = new SysRoleDeptTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn ROLE_ID = new QueryColumn(this, "role_id");
    public final QueryColumn DEPT_ID = new QueryColumn(this, "dept_id");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{
            ID, ROLE_ID, DEPT_ID, CREATE_TIME
    };

    public SysRoleDeptTableDef() {
        super("", "sys_role_dept");
    }

    private SysRoleDeptTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public SysRoleDeptTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new SysRoleDeptTableDef("", "sys_role_dept", alias));
    }
}