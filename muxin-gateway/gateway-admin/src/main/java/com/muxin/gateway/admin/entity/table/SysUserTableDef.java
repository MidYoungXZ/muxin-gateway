package com.muxin.gateway.admin.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

/**
 * sys_user 表定义
 *
 * @author mybatis-flex-processor
 * @since 2024-01-01
 */
public class SysUserTableDef extends TableDef {

    /**
     * 用户表
     */
    public static final SysUserTableDef SYS_USER = new SysUserTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 用户名
     */
    public final QueryColumn USERNAME = new QueryColumn(this, "username");

    /**
     * 密码
     */
    public final QueryColumn PASSWORD = new QueryColumn(this, "password");

    /**
     * 昵称
     */
    public final QueryColumn NICKNAME = new QueryColumn(this, "nickname");

    /**
     * 邮箱
     */
    public final QueryColumn EMAIL = new QueryColumn(this, "email");

    /**
     * 手机号
     */
    public final QueryColumn MOBILE = new QueryColumn(this, "mobile");

    /**
     * 头像
     */
    public final QueryColumn AVATAR = new QueryColumn(this, "avatar");

    /**
     * 部门ID
     */
    public final QueryColumn DEPT_ID = new QueryColumn(this, "dept_id");

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
     * 默认字段，排除逻辑删除字段和密码
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{
            ID, USERNAME, NICKNAME, EMAIL, MOBILE, AVATAR, DEPT_ID, STATUS,
            CREATE_TIME, UPDATE_TIME, CREATE_BY, UPDATE_BY
    };

    public SysUserTableDef() {
        super("", "sys_user");
    }

    private SysUserTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public SysUserTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new SysUserTableDef("", "sys_user", alias));
    }
} 