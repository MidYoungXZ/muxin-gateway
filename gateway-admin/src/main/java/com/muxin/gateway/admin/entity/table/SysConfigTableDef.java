package com.muxin.gateway.admin.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class SysConfigTableDef extends TableDef {
    
    public static final SysConfigTableDef SYS_CONFIG = new SysConfigTableDef();
    
    public final QueryColumn ID = new QueryColumn(this, "id");
    
    public final QueryColumn CONFIG_KEY = new QueryColumn(this, "config_key");
    
    public final QueryColumn CONFIG_VALUE = new QueryColumn(this, "config_value");
    
    public final QueryColumn CONFIG_NAME = new QueryColumn(this, "config_name");
    
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");
    
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");
    
    public final QueryColumn CREATE_BY = new QueryColumn(this, "create_by");
    
    public final QueryColumn UPDATE_BY = new QueryColumn(this, "update_by");
    
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{
            ID, CONFIG_KEY, CONFIG_VALUE, CONFIG_NAME, DESCRIPTION, STATUS,
            CREATE_TIME, UPDATE_TIME, CREATE_BY, UPDATE_BY
    };
    
    public SysConfigTableDef() {
        super("", "sys_config");
    }
    
    private SysConfigTableDef(String schema, String name, String alias) {
        super(schema, name, alias);
    }
    
    public SysConfigTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new SysConfigTableDef("", "sys_config", alias));
    }
}