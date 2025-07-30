package com.muxin.gateway.admin.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

/**
 * gw_route 表定义
 *
 * @author mybatis-flex-processor
 * @since 2024-01-01
 */
public class GwRouteTableDef extends TableDef {

    /**
     * 路由配置表
     */
    public static final GwRouteTableDef GW_ROUTE = new GwRouteTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 路由ID
     */
    public final QueryColumn ROUTE_ID = new QueryColumn(this, "route_id");

    /**
     * 路由名称
     */
    public final QueryColumn ROUTE_NAME = new QueryColumn(this, "route_name");

    /**
     * 描述
     */
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    /**
     * URI
     */
    public final QueryColumn URI = new QueryColumn(this, "uri");

    /**
     * 元数据
     */
    public final QueryColumn METADATA = new QueryColumn(this, "metadata");

    /**
     * 排序
     */
    public final QueryColumn ORDER = new QueryColumn(this, "order");

    /**
     * 是否启用
     */
    public final QueryColumn ENABLED = new QueryColumn(this, "enabled");

    /**
     * 是否启用灰度
     */
    public final QueryColumn GRAYSCALE_ENABLED = new QueryColumn(this, "grayscale_enabled");

    /**
     * 灰度配置
     */
    public final QueryColumn GRAYSCALE_CONFIG = new QueryColumn(this, "grayscale_config");

    /**
     * 模板ID
     */
    public final QueryColumn TEMPLATE_ID = new QueryColumn(this, "template_id");

    /**
     * 版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 是否删除
     */
    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    /**
     * 创建时间
     */
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    /**
     * 更新时间
     */
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    /**
     * 创建人
     */
    public final QueryColumn CREATE_BY = new QueryColumn(this, "create_by");

    /**
     * 更新人
     */
    public final QueryColumn UPDATE_BY = new QueryColumn(this, "update_by");

    /**
     * 所有字段
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，排除逻辑删除字段
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{
            ID, ROUTE_ID, ROUTE_NAME, DESCRIPTION, URI, METADATA, ORDER, 
            ENABLED, GRAYSCALE_ENABLED, GRAYSCALE_CONFIG, TEMPLATE_ID, 
            VERSION, CREATE_TIME, UPDATE_TIME, CREATE_BY, UPDATE_BY
    };

    public GwRouteTableDef() {
        super("", "gw_route");
    }

    private GwRouteTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public GwRouteTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new GwRouteTableDef("", "gw_route", alias));
    }
} 