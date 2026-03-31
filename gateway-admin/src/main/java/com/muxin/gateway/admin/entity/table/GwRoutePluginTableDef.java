package com.muxin.gateway.admin.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

/**
 * gw_route_plugin 表定义
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
public class GwRoutePluginTableDef extends TableDef {

    public static final GwRoutePluginTableDef GW_ROUTE_PLUGIN = new GwRoutePluginTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn ROUTE_ID = new QueryColumn(this, "route_id");
    public final QueryColumn PLUGIN_ID = new QueryColumn(this, "plugin_id");
    public final QueryColumn CONFIG = new QueryColumn(this, "config");
    public final QueryColumn PRIORITY_OVERRIDE = new QueryColumn(this, "priority_override");
    public final QueryColumn ENABLED = new QueryColumn(this, "enabled");
    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public GwRoutePluginTableDef() {
        super("", "gw_route_plugin");
    }

    private GwRoutePluginTableDef(String schema, String name, String alias) {
        super(schema, name, alias);
    }

    public GwRoutePluginTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new GwRoutePluginTableDef("", "gw_route_plugin", alias));
    }
}