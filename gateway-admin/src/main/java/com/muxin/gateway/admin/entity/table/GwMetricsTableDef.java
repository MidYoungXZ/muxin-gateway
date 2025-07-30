package com.muxin.gateway.admin.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

/**
 * gw_metrics 表定义
 *
 * @author mybatis-flex-processor
 * @since 2024-01-01
 */
public class GwMetricsTableDef extends TableDef {

    /**
     * 监控指标表
     */
    public static final GwMetricsTableDef GW_METRICS = new GwMetricsTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 路由ID
     */
    public final QueryColumn ROUTE_ID = new QueryColumn(this, "route_id");

    /**
     * 请求总数
     */
    public final QueryColumn REQUEST_COUNT = new QueryColumn(this, "request_count");

    /**
     * 成功请求数
     */
    public final QueryColumn SUCCESS_COUNT = new QueryColumn(this, "success_count");

    /**
     * 失败请求数
     */
    public final QueryColumn FAILURE_COUNT = new QueryColumn(this, "failure_count");

    /**
     * 总耗时（毫秒）
     */
    public final QueryColumn TOTAL_TIME = new QueryColumn(this, "total_time");

    /**
     * 平均耗时（毫秒）
     */
    public final QueryColumn AVG_TIME = new QueryColumn(this, "avg_time");

    /**
     * 最大耗时（毫秒）
     */
    public final QueryColumn MAX_TIME = new QueryColumn(this, "max_time");

    /**
     * 最小耗时（毫秒）
     */
    public final QueryColumn MIN_TIME = new QueryColumn(this, "min_time");

    /**
     * 采集时间
     */
    public final QueryColumn COLLECT_TIME = new QueryColumn(this, "collect_time");

    /**
     * 所有字段
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{
            ID, ROUTE_ID, REQUEST_COUNT, SUCCESS_COUNT, FAILURE_COUNT,
            TOTAL_TIME, AVG_TIME, MAX_TIME, MIN_TIME, COLLECT_TIME
    };

    public GwMetricsTableDef() {
        super("", "gw_metrics");
    }

    private GwMetricsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public GwMetricsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new GwMetricsTableDef("", "gw_metrics", alias));
    }
} 