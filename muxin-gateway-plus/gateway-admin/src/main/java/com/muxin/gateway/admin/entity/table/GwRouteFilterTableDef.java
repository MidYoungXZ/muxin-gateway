package com.muxin.gateway.admin.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

/**
 * gw_route_filter 表定义
 *
 * @author mybatis-flex-processor
 * @since 2024-01-01
 */
public class GwRouteFilterTableDef extends TableDef {

    /**
     * 路由过滤器关联表
     */
    public static final GwRouteFilterTableDef GW_ROUTE_FILTER = new GwRouteFilterTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 路由ID
     */
    public final QueryColumn ROUTE_ID = new QueryColumn(this, "route_id");

    /**
     * 过滤器ID
     */
    public final QueryColumn FILTER_ID = new QueryColumn(this, "filter_id");

    /**
     * 排序顺序
     */
    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");

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
            ID, ROUTE_ID, FILTER_ID, SORT_ORDER, CREATE_TIME
    };

    public GwRouteFilterTableDef() {
        super("", "gw_route_filter");
    }

    private GwRouteFilterTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public GwRouteFilterTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new GwRouteFilterTableDef("", "gw_route_filter", alias));
    }
} 