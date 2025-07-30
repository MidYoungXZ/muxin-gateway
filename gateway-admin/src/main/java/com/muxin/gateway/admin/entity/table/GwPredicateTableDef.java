package com.muxin.gateway.admin.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

/**
 * gw_predicate 表定义
 *
 * @author mybatis-flex-processor
 * @since 2024-01-01
 */
public class GwPredicateTableDef extends TableDef {

    /**
     * 断言配置表
     */
    public static final GwPredicateTableDef GW_PREDICATE = new GwPredicateTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 断言名称
     */
    public final QueryColumn PREDICATE_NAME = new QueryColumn(this, "predicate_name");

    /**
     * 断言类型
     */
    public final QueryColumn PREDICATE_TYPE = new QueryColumn(this, "predicate_type");

    /**
     * 描述
     */
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    /**
     * 配置
     */
    public final QueryColumn CONFIG = new QueryColumn(this, "config");

    /**
     * 是否系统内置
     */
    public final QueryColumn IS_SYSTEM = new QueryColumn(this, "is_system");

    /**
     * 是否启用
     */
    public final QueryColumn ENABLED = new QueryColumn(this, "enabled");

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
            ID, PREDICATE_NAME, PREDICATE_TYPE, DESCRIPTION, CONFIG, 
            IS_SYSTEM, ENABLED, CREATE_TIME, UPDATE_TIME, CREATE_BY, UPDATE_BY
    };

    public GwPredicateTableDef() {
        super("", "gw_predicate");
    }

    private GwPredicateTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public GwPredicateTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new GwPredicateTableDef("", "gw_predicate", alias));
    }
} 