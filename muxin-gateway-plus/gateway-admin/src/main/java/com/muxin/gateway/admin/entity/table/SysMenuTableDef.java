package com.muxin.gateway.admin.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

/**
 * sys_menu 表定义
 *
 * @author mybatis-flex-processor
 * @since 2024-01-01
 */
public class SysMenuTableDef extends TableDef {

    /**
     * 菜单表
     */
    public static final SysMenuTableDef SYS_MENU = new SysMenuTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 父菜单ID
     */
    public final QueryColumn PARENT_ID = new QueryColumn(this, "parent_id");

    /**
     * 菜单名称
     */
    public final QueryColumn MENU_NAME = new QueryColumn(this, "menu_name");

    /**
     * 国际化编码
     */
    public final QueryColumn I18N_CODE = new QueryColumn(this, "i18n_code");

    /**
     * 菜单类型
     */
    public final QueryColumn MENU_TYPE = new QueryColumn(this, "menu_type");

    /**
     * 路由地址
     */
    public final QueryColumn PATH = new QueryColumn(this, "path");

    /**
     * 组件路径
     */
    public final QueryColumn COMPONENT = new QueryColumn(this, "component");

    /**
     * 权限标识
     */
    public final QueryColumn PERMS = new QueryColumn(this, "perms");

    /**
     * 菜单图标
     */
    public final QueryColumn ICON = new QueryColumn(this, "icon");

    /**
     * 排序
     */
    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");

    /**
     * 是否显示
     */
    public final QueryColumn VISIBLE = new QueryColumn(this, "visible");

    /**
     * 状态
     */
    public final QueryColumn STATUS = new QueryColumn(this, "status");

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
     * 是否删除
     */
    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    /**
     * 所有字段
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，排除逻辑删除字段
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{
            ID, PARENT_ID, MENU_NAME, I18N_CODE, MENU_TYPE, PATH, COMPONENT,
            PERMS, ICON, SORT_ORDER, VISIBLE, STATUS,
            CREATE_TIME, UPDATE_TIME, CREATE_BY, UPDATE_BY
    };

    public SysMenuTableDef() {
        super("", "sys_menu");
    }

    private SysMenuTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public SysMenuTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new SysMenuTableDef("", "sys_menu", alias));
    }
} 