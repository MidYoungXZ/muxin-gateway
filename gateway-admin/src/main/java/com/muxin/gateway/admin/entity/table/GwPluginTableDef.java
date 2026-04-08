package com.muxin.gateway.admin.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

/**
 * gw_plugin 表定义
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public class GwPluginTableDef extends TableDef {

    public static final GwPluginTableDef GW_PLUGIN = new GwPluginTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn PLUGIN_NAME = new QueryColumn(this, "plugin_name");
    public final QueryColumn PLUGIN_TYPE = new QueryColumn(this, "plugin_type");
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");
    public final QueryColumn SCHEMA = new QueryColumn(this, "schema");
    public final QueryColumn DEFAULT_CONFIG = new QueryColumn(this, "default_config");
    public final QueryColumn DEFAULT_PRIORITY = new QueryColumn(this, "default_priority");
    public final QueryColumn PHASE = new QueryColumn(this, "phase");
    public final QueryColumn ICON = new QueryColumn(this, "icon");
    public final QueryColumn IS_SYSTEM = new QueryColumn(this, "is_system");
    public final QueryColumn ENABLED = new QueryColumn(this, "enabled");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");
    public final QueryColumn CREATE_BY = new QueryColumn(this, "create_by");
    public final QueryColumn UPDATE_BY = new QueryColumn(this, "update_by");
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public GwPluginTableDef() {
        super("", "gw_plugin");
    }

    private GwPluginTableDef(String schema, String name, String alias) {
        super(schema, name, alias);
    }

    public GwPluginTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new GwPluginTableDef("", "gw_plugin", alias));
    }
}